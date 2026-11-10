#!/usr/bin/env node
const fs=require('fs'),path=require('path'),crypto=require('crypto');
const root=path.resolve(__dirname,'..'),web=path.resolve(root,'../web'),ts=require(path.join(web,'node_modules/typescript'));
const apply=process.argv.includes('--apply');const fileName=path.join(root,'src/main/resources/localization/en.json');const messages=JSON.parse(fs.readFileSync(fileName));
function register(text){if(!/\p{L}/u.test(text)||text.length>10000)return;const id='ui.'+crypto.createHash('sha256').update(text).digest('hex').slice(0,16);messages[id]=text;}
function files(dir){return fs.readdirSync(dir,{withFileTypes:true}).flatMap(item=>item.isDirectory()?files(path.join(dir,item.name)):[path.join(dir,item.name)])}
const report={formattedFiles:[],staticFormatters:[],dynamicLabelFiles:[]};
for(const file of [...files(path.join(web,'app')),...files(path.join(web,'components')),...files(path.join(web,'lib'))].filter(p=>/\.(?:tsx|mjs)$/.test(p)&&!p.endsWith('/localization.mjs'))){
 let original=fs.readFileSync(file,'utf8'),edits=[],usesLocale=false,usesText=false;const ast=ts.createSourceFile(file,original,ts.ScriptTarget.Latest,true,file.endsWith('.tsx')?ts.ScriptKind.TSX:ts.ScriptKind.JS);
 function visit(node){
  if(ts.isStringLiteral(node)||ts.isNoSubstitutionTemplateLiteral(node)){
   const text=node.text;let p=node.parent;
   if((ts.isNewExpression(p)||ts.isCallExpression(p))&&p.expression.getText(ast)==='Error')register(text);
   if(ts.isPropertyAssignment(p)&&['label','placeholder','caption','summary'].includes(p.name.getText(ast))&&!/^[A-Z0-9_]+$|^[./#]/.test(text))register(text);
   // Static form helpers receive their display label as a literal argument.
   if(ts.isCallExpression(p)&&/^(?:input|textInput|renderInput|renderField|fieldInput)$/.test(p.expression.getText(ast))&&p.arguments.indexOf(node)>0)register(text);
  }
  if(ts.isTemplateExpression(node)&&((ts.isNewExpression(node.parent)||ts.isCallExpression(node.parent))&&node.parent.expression.getText(ast)==='Error')){let text=node.head.text;node.templateSpans.forEach((part,i)=>text+=`{{value${i}}}`+part.literal.text);register(text);}
  if((ts.isNewExpression(node)||ts.isCallExpression(node))&&node.arguments?.length){const callee=node.expression.getText(ast),first=node.arguments[0];if(/(?:Intl\.(?:DateTimeFormat|NumberFormat)|\.toLocale(?:DateString|TimeString|String))$/.test(callee)&&ts.isStringLiteral(first)&&/^en(?:-IN|-US|-GB)?$/.test(first.text)){
   let parent=node.parent;while(parent&&!ts.isFunctionLike(parent))parent=parent.parent;
   if(!parent){report.staticFormatters.push(path.relative(web,file));}else{edits.push({start:first.getStart(ast),end:first.getEnd(),text:`activeLocale(${JSON.stringify(first.text)})`});usesLocale=true;}
  }}
  if(file.endsWith('.tsx')&&ts.isJsxExpression(node)&&node.expression){const expression=node.expression,text=expression.getText(ast);if(!/^localize(?:Text)?\(/.test(text)){
   let attribute=null;for(let p=node.parent;p;p=p.parent){if(ts.isJsxAttribute(p)){attribute=p.name.getText(ast);break;}if(ts.isJsxElement(p)||ts.isJsxFragment(p))break;}
   const status=/^(?:[A-Za-z_$][\w$]*\.)+(?:status|paymentEvidence|readinessStatus|allocationStatus|verificationStatus|occasionType)$/.test(text);
   const typedLabel=/\b(?:occasionType|occasion|status|paymentEvidence|role)\b.*\.replaceAll\(/.test(text);
   const literalMap=(ts.isElementAccessExpression(expression)||ts.isAsExpression(expression))&&/^\(?\{/.test(text);
   let hasJsx=false;function hasMarkup(child){if(ts.isJsxElement(child)||ts.isJsxSelfClosingElement(child)||ts.isJsxFragment(child))hasJsx=true;ts.forEachChild(child,hasMarkup)}hasMarkup(expression);
   if(attribute===null&&!hasJsx&&(status||typedLabel||literalMap)){edits.push({start:expression.getStart(ast),end:expression.getStart(ast),text:'localizeText('},{start:expression.getEnd(),end:expression.getEnd(),text:')'});usesText=true;report.dynamicLabelFiles.push(path.relative(web,file));}
  }}
  ts.forEachChild(node,visit);
 }
 visit(ast);
 if(edits.length&&apply){edits.sort((a,b)=>b.start-a.start||b.end-a.end);for(const edit of edits)original=original.slice(0,edit.start)+edit.text+original.slice(edit.end);let relative=path.relative(path.dirname(file),path.join(web,'lib/localization.mjs')).replaceAll('\\','/');if(!relative.startsWith('.'))relative='./'+relative;
  const names=[usesLocale?'activeLocale':null,usesText?'localizeText':null].filter(name=>name&&!new RegExp(`import\\s*\\{[^}]*\\b${name}\\b[^}]*}`).test(original));
  if(names.length){let position=0;const directive=ast.statements.find(n=>ts.isExpressionStatement(n)&&ts.isStringLiteral(n.expression)&&n.expression.text==='use client');if(directive)position=directive.getEnd();original=original.slice(0,position)+`\nimport {${names.join(',')}} from '${relative}';\n`+original.slice(position);}
  const parsed=ts.createSourceFile(file,original,ts.ScriptTarget.Latest,true,file.endsWith('.tsx')?ts.ScriptKind.TSX:ts.ScriptKind.JS);if(parsed.parseDiagnostics.length)throw Error('Invalid localized formatter '+file);fs.writeFileSync(file,original);report.formattedFiles.push(path.relative(web,file));
 }
}
for(const label of ['WEDDING','BIRTHDAY','BABY_SHOWER','HOUSEWARMING','ANNIVERSARY','FAREWELL','RETIREMENT','REUNION','OTHER','DRAFT','PUBLISHED','COMPLETED','CANCELLED','PENDING','APPROVED','HIDDEN','CAPTURED','FAILED','VERIFIED','UNVERIFIED','REPORTED_UNVERIFIED','REJECTED','AWAITING_VERIFICATION'])register(label);
if(apply)fs.writeFileSync(fileName,JSON.stringify(messages,null,2)+'\n');report.sourceMessages=Object.keys(messages).length;report.staticFormatters=[...new Set(report.staticFormatters)];report.dynamicLabelFiles=[...new Set(report.dynamicLabelFiles)];fs.writeFileSync(path.join(root,'docs/application/catalogue/localization-completeness.json'),JSON.stringify(report,null,2)+'\n');console.log(JSON.stringify(report));
