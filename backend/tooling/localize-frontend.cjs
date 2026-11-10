#!/usr/bin/env node
/* Source-preserving i18n codemod. It never translates input values, API data, names, URLs, styles or business comparisons. */
const fs=require('fs'),path=require('path'),crypto=require('crypto');
const backend=path.resolve(__dirname,'..'),web=path.resolve(backend,'../web');
const ts=require(path.join(web,'node_modules/typescript'));
const apply=process.argv.includes('--apply');
const sources={},locations={},report={files:[],unresolvedServerAttributes:[],componentSubscriptions:0};
function walk(dir){return fs.readdirSync(dir,{withFileTypes:true}).flatMap(item=>item.isDirectory()?walk(path.join(dir,item.name)):[path.join(dir,item.name)])}
const files=[...walk(path.join(web,'app')),...walk(path.join(web,'components'))].filter(p=>p.endsWith('.tsx'));
function sourceKey(text,file){const key='ui.'+crypto.createHash('sha256').update(text).digest('hex').slice(0,16);if(sources[key]&&sources[key]!==text)throw Error('Message key collision');sources[key]=text;(locations[key]??=new Set()).add(path.relative(web,file));return key;}
const entities={amp:'&',lt:'<',gt:'>',quot:'"',apos:"'",nbsp:'\u00a0',rarr:'→',larr:'←',ndash:'–',mdash:'—',hearts:'♥',hellip:'…'};
function decode(value){return value.replace(/&(#x[0-9a-f]+|#\d+|[a-z]+);/gi,(all,part)=>part[0]==='#'?String.fromCodePoint(parseInt(part.slice(part[1]?.toLowerCase()==='x'?2:1),part[1]?.toLowerCase()==='x'?16:10)):(entities[part]??all))}
function jsxText(value){let lines=value.split(/\r\n|\n|\r/),last=0;lines.forEach((line,i)=>{if(/[^ \t]/.test(line))last=i});let result='';lines.forEach((line,i)=>{line=line.replace(/\t/g,' ');if(i)line=line.replace(/^ +/,'');if(i!==lines.length-1)line=line.replace(/ +$/,'');if(line){result+=line;if(i!==last)result+=' '}});return decode(result)}
function human(value){return /\p{L}/u.test(value)&&value.length<=10000&&!/^https?:\/\//i.test(value)}
function call(text,file,values=''){const key=sourceKey(text,file);return `localize(${JSON.stringify(key)},{defaultValue:${JSON.stringify(text)}${values}})`;}
for(const file of files){
 if(file.endsWith('LocalizationProvider.tsx')){} // The provider uses the same source mechanism as all other controls.
 const original=fs.readFileSync(file,'utf8');if(original.includes('/* localization:instrumented */'))continue;
 const ast=ts.createSourceFile(file,original,ts.ScriptTarget.Latest,true,ts.ScriptKind.TSX);const clientDirective=ast.statements.find(node=>ts.isExpressionStatement(node)&&ts.isStringLiteral(node.expression)&&node.expression.text==='use client');const client=!!clientDirective;
 let edits=[],usesLocalize=false,usesText=false,usesLocalized=false;
 const replacements=new Set();
 function add(node,text){const start=node.getStart(ast),end=node.getEnd();const id=start+':'+end;if(replacements.has(id))return;replacements.add(id);edits.push({start,end,text})}
 function parentAttribute(node){for(let p=node.parent;p;p=p.parent){if(ts.isJsxAttribute(p))return p.name.getText(ast);if(ts.isJsxElement(p)||ts.isJsxSelfClosingElement(p)||ts.isJsxFragment(p))return null;}return null}
 function existingLocalization(node){for(let p=node.parent;p;p=p.parent){if(ts.isCallExpression(p)&&/^(?:localize|localizeText|t)$/.test(p.expression.getText(ast)))return true;}return false}
 function jsxContext(node){for(let p=node.parent;p;p=p.parent){if(ts.isJsxExpression(p))return true;if(ts.isBlock(p)||ts.isVariableDeclaration(p)||ts.isFunctionDeclaration(p))return false;}return false}
 function comparison(node){return node.parent&&((ts.isBinaryExpression(node.parent)&&[ts.SyntaxKind.EqualsEqualsEqualsToken,ts.SyntaxKind.ExclamationEqualsEqualsToken,ts.SyntaxKind.EqualsEqualsToken,ts.SyntaxKind.ExclamationEqualsToken].includes(node.parent.operatorToken.kind))||ts.isPropertyAssignment(node.parent)&&node.parent.name===node)}
 function template(node){let text=node.head.text,values='';node.templateSpans.forEach((span,i)=>{text+=`{{value${i}}}`+span.literal.text;values+=`,value${i}:(${span.expression.getText(ast)})`});return {text,values}}
 function visit(node){
  if(ts.isJsxText(node)){const text=jsxText(node.text);if(human(text)){if(client){add(node,'{'+call(text,file)+'}');usesLocalize=true;}else{const key=sourceKey(text,file);add(node,`<Localized id=${JSON.stringify(key)} text=${JSON.stringify(text)}/>`);usesLocalized=true;}}return;}
  if(ts.isJsxAttribute(node)&&['placeholder','title','aria-label','alt'].includes(node.name.getText(ast))&&node.initializer&&ts.isStringLiteral(node.initializer)){
   const text=decode(node.initializer.text);if(human(text)){if(client){add(node.initializer,'{'+call(text,file)+'}');usesLocalize=true;}else report.unresolvedServerAttributes.push({file:path.relative(web,file),text});}return;
  }
  if((ts.isStringLiteral(node)||ts.isNoSubstitutionTemplateLiteral(node)||ts.isTemplateExpression(node))&&!existingLocalization(node)){
   const text=ts.isTemplateExpression(node)?template(node).text:node.text;
   const attribute=parentAttribute(node),allowedAttribute=attribute===null||['placeholder','title','aria-label','alt'].includes(attribute);
   if(client&&human(text)&&jsxContext(node)&&allowedAttribute&&!comparison(node)){
    // Only expression output, not API arguments, class names, enum comparisons or JSX prop values.
    let unsafe=false;for(let p=node.parent;p&&!ts.isJsxExpression(p);p=p.parent){if(ts.isCallExpression(p)||ts.isPropertyAssignment(p)||ts.isElementAccessExpression(p)){unsafe=true;break;}}
    if(!unsafe){const values=ts.isTemplateExpression(node)?template(node).values:'';add(node,call(text,file,values));usesLocalize=true;return;}
   }
   // Register feedback and static presentation maps; translation occurs when rendered, never in stored form state.
   let feedback=false;for(let p=node.parent;p;p=p.parent){if(ts.isCallExpression(p)||ts.isNewExpression(p)){const name=p.expression.getText(ast);if(/^set(?:Error|Message|Notice|.*Result|.*Feedback)$/.test(name)||name==='Error')feedback=true;break;}if(ts.isJsxElement(p)||ts.isBlock(p))break;}
   if(human(text)&&feedback&&!comparison(node))sourceKey(text,file);
  }
  if(client&&ts.isJsxExpression(node)&&node.expression&&!existingLocalization(node)){
   const expression=node.expression;const text=expression.getText(ast),attribute=parentAttribute(node);
   if(attribute===null&&/^(?:error|notice|rsvpResult|wishResult|label|feedback|statusMessage)$/.test(text)){add(expression,`localizeText(${text})`);usesText=true;return;}
   if(attribute===null&&/^modules\[/.test(text)){add(expression,`localizeText(${text})`);usesText=true;return;}
   // Message state is safe to translate only inside a feedback region, not guest-authored message forms.
   if(attribute===null&&text==='message'){let safe=false;for(let p=node.parent;p;p=p.parent){if(ts.isJsxElement(p)){safe=p.openingElement.attributes.properties.some(a=>ts.isJsxAttribute(a)&&a.name.getText(ast)==='role'&&a.initializer&&['status','alert'].includes(a.initializer.text));break;}}if(safe){add(expression,'localizeText(message)');usesText=true;return;}}
  }
  ts.forEachChild(node,visit);
 }
 visit(ast);
 if(usesLocalize||usesText){
  function registerMaps(node){
   if(ts.isStringLiteral(node)&&human(node.text)){
    let inPresentation=false;for(let p=node.parent;p;p=p.parent){if(ts.isVariableDeclaration(p)){inPresentation=/^(?:modules|questions|styles|symbols|themes|names|tabs|sections|labels|items|steps|options|actions|statuses|choices|occasionTitles|occasionNotes)$/.test(p.name.getText(ast));break;}if(ts.isBlock(p))break;}
    // JSX-local arrays often use [value, icon, human label]. Values and identifiers remain unmodified.
    if(!inPresentation&&node.parent&&ts.isArrayLiteralExpression(node.parent)&&node.parent.elements.indexOf(node)>0){let p=node.parent;while(p&&!ts.isJsxExpression(p)&&!ts.isBlock(p)&&!ts.isVariableDeclaration(p))p=p.parent;inPresentation=!!p&&ts.isJsxExpression(p);}
    if(inPresentation&&!/^[A-Z0-9_]+$/.test(node.text)&&!/^[/#.]|^\w+-\w+-?/.test(node.text))sourceKey(node.text,file);
   }
   ts.forEachChild(node,registerMaps);
  }registerMaps(ast);
  // Subscribe real component functions, never nested event handlers or map callbacks.
  function subscribe(node){
   let component=false;if(ts.isFunctionDeclaration(node)){component=!!node.name&&/^[A-Z]/.test(node.name.text)||!!node.modifiers?.some(m=>m.kind===ts.SyntaxKind.DefaultKeyword);}
   if((ts.isArrowFunction(node)||ts.isFunctionExpression(node))&&ts.isVariableDeclaration(node.parent))component=/^[A-Z]/.test(node.parent.name.getText(ast));
   if(component&&node.body){let hasJsx=false;function check(child){if(ts.isJsxElement(child)||ts.isJsxSelfClosingElement(child)||ts.isJsxFragment(child))hasJsx=true;ts.forEachChild(child,check)}check(node.body);if(hasJsx){if(ts.isBlock(node.body))edits.push({start:node.body.getStart(ast)+1,end:node.body.getStart(ast)+1,text:'useLocalizationRender();'});else{edits.push({start:node.body.getStart(ast),end:node.body.getStart(ast),text:'{useLocalizationRender();return ('},{start:node.body.getEnd(),end:node.body.getEnd(),text:');}'});}report.componentSubscriptions++;}}
   ts.forEachChild(node,subscribe);
  }subscribe(ast);
 }
 if(!edits.length)continue;
 edits.sort((a,b)=>b.start-a.start||b.end-a.end);let output=original,last=Infinity;for(const edit of edits){if(edit.end>last&&edit.start!==edit.end)throw Error('Overlapping localization edit: '+file);output=output.slice(0,edit.start)+edit.text+output.slice(edit.end);last=edit.start;}
 let runtime=path.relative(path.dirname(file),path.join(web,'lib/localization.mjs')).replaceAll('\\','/');if(!runtime.startsWith('.'))runtime='./'+runtime;
 let provider=path.relative(path.dirname(file),path.join(web,'components/LocalizationProvider')).replaceAll('\\','/');if(!provider.startsWith('.'))provider='./'+provider;
 const imports=[];if(usesLocalize||usesText)imports.push(`import {${[usesLocalize?'localize':null,usesText?'localizeText':null].filter(Boolean).join(',')}} from '${runtime}';`);if(usesLocalize||usesText||usesLocalized)imports.push(`import {${[client?'useLocalizationRender':null,usesLocalized?'Localized':null].filter(Boolean).join(',')}} from '${provider}';`);
 // The provider cannot import itself; its subscription function is already in scope.
 const filtered=imports.filter(line=>!file.endsWith('LocalizationProvider.tsx')||!line.includes("from './LocalizationProvider'"));
 const marker='\n/* localization:instrumented */\n'+filtered.join('\n')+'\n';
 if(client){const insertion=clientDirective.getEnd();output=output.slice(0,insertion)+marker+output.slice(insertion);}else output=marker+output;
 const parsed=ts.createSourceFile(file,output,ts.ScriptTarget.Latest,true,ts.ScriptKind.TSX);if(parsed.parseDiagnostics.length)throw Error('Invalid transformed TSX: '+file+' '+parsed.parseDiagnostics.map(x=>x.messageText).join(';'));
 report.files.push({file:path.relative(web,file),edits:edits.length});if(apply)fs.writeFileSync(file,output);
}
// Retain prior keys on incremental runs; source changes create new immutable keys.
const catalogFile=path.join(backend,'src/main/resources/localization/en.json');const prior=JSON.parse(fs.readFileSync(catalogFile,'utf8'));const merged={...prior,...sources};
if(apply)fs.writeFileSync(catalogFile,JSON.stringify(merged,null,2)+'\n');
report.sourceMessages=Object.keys(merged).length;report.applied=apply;
fs.writeFileSync(path.join(backend,'docs/application/catalogue/localization-extraction.json'),JSON.stringify({...report,locations:Object.fromEntries(Object.entries(locations).map(([key,value])=>[key,[...value]]))},null,2)+'\n');
console.log(JSON.stringify({files:report.files.length,messages:report.sourceMessages,subscriptions:report.componentSubscriptions,unresolvedServerAttributes:report.unresolvedServerAttributes.length,applied:apply}));
