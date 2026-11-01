const fs=require('fs'),path=require('path');
const root=path.resolve(__dirname,'../../..'),web=path.resolve(root,'../web');
const ts=require(path.join(web,'node_modules/typescript'));
function walk(dir){return fs.readdirSync(dir,{withFileTypes:true}).flatMap(x=>x.isDirectory()?walk(path.join(dir,x.name)):x.name.endsWith('.tsx')?[path.join(dir,x.name)]:[])}
const files=[...walk(path.join(web,'app')),...walk(path.join(web,'components'))];
const catalogue={files:[],routes:[],controls:[],requests:[],dynamicSources:[]};let count=0;
const rel=p=>path.relative(path.dirname(root),p).split(path.sep).join('/');
const clean=s=>s.replace(/sk-[a-zA-Z0-9_-]{12,}/g,'<redacted-secret>');
for(const file of files){
 const layout={};const source=fs.readFileSync(file,'utf8'),tree=ts.createSourceFile(file,source,ts.ScriptTarget.Latest,true,ts.ScriptKind.TSX),imports=[],states=[];
 const position=node=>tree.getLineAndCharacterOfPosition(node.getStart(tree)).line+1;
 const raw=node=>clean(node?.getText(tree)||'');
 function attr(open,name){const item=open.attributes?.properties.find(x=>ts.isJsxAttribute(x)&&x.name.getText(tree)===name);return item?(item.initializer?raw(item.initializer):'true'):null}
 function tag(open){return open.tagName.getText(tree)}
 function ancestor(node,predicate){for(let p=node.parent;p;p=p.parent)if(predicate(p))return p;return null}
 function label(node,open){let explicit=attr(open,'aria-label');if(explicit)return explicit;let wrapper=ancestor(node,n=>ts.isJsxElement(n)&&n.openingElement.tagName.getText(tree)==='label');if(wrapper)return clean(wrapper.children.filter(n=>!ts.isJsxElement(n)&&!ts.isJsxSelfClosingElement(n)).map(raw).join(' ').replace(/\s+/g,' ').trim());return attr(open,'name')||attr(open,'id')||'No static label; inspect component context';}
 function conditions(node){let result=[];for(let p=node.parent;p;p=p.parent){if(ts.isConditionalExpression(p))result.push(raw(p.condition));else if(ts.isBinaryExpression(p)&&p.operatorToken.kind===ts.SyntaxKind.AmpersandAmpersandToken)result.push(raw(p.left));}return [...new Set(result)];}
 function visit(node){
  if((ts.isImportDeclaration(node)||ts.isExportDeclaration(node))&&node.moduleSpecifier&&ts.isStringLiteral(node.moduleSpecifier))imports.push(node.moduleSpecifier.text);
  if(ts.isCallExpression(node)&&node.expression.kind===ts.SyntaxKind.ImportKeyword&&node.arguments[0]&&ts.isStringLiteral(node.arguments[0]))imports.push(node.arguments[0].text);
  if(ts.isVariableDeclaration(node)&&ts.isArrayBindingPattern(node.name)&&node.initializer&&ts.isCallExpression(node.initializer)&&raw(node.initializer.expression)==='useState'){
   const name=raw(node.name.elements[0]);let initial=raw(node.initializer.arguments[0]);if(/password|secret|apiKey/i.test(name)&&initial!=="''"&&initial!=='\"\"')initial='<sensitive initial expression withheld>';
   const init=node.initializer.arguments[0];const members=init&&ts.isObjectLiteralExpression(init)?Object.fromEntries(init.properties.filter(ts.isPropertyAssignment).map(p=>[raw(p.name),/password|secret|apiKey/i.test(raw(p.name))?'<redacted>':raw(p.initializer)])):null;
   states.push({name,initial,members,type:node.initializer.typeArguments?.map(raw).join(',')||'inferred',line:position(node)});
  }
  if(ts.isJsxOpeningElement(node)||ts.isJsxSelfClosingElement(node)){
   let name=tag(node);if(['header','footer','nav','aside','main','section','dialog','details','summary','article','table'].includes(name))layout[name]=(layout[name]||0)+1;if((['input','textarea','select','button','a','Link','dialog','form','fieldset','progress'].includes(name)||['"dialog"','"alertdialog"','"alert"','"status"'].includes(attr(node,'role')))){
    const attrs={};for(const a of node.attributes.properties)if(ts.isJsxAttribute(a))attrs[raw(a.name)]=a.initializer?raw(a.initializer):'true';
    const element=ts.isJsxOpeningElement(node)?node.parent:node;
    const form=ancestor(node,n=>ts.isJsxElement(n)&&n.openingElement.tagName.getText(tree)==='form');
    let content=ts.isJsxElement(element)?element.children.filter(x=>ts.isJsxText(x)||ts.isJsxExpression(x)).map(raw).join(' ').replace(/\s+/g,' ').trim():'';
    let options=[];if(name==='select'){function optionsVisit(n){if(ts.isJsxElement(n)&&n.openingElement.tagName.getText(tree)==='option')options.push({value:attr(n.openingElement,'value'),text:raw(n)});ts.forEachChild(n,optionsVisit)}optionsVisit(element);}
    catalogue.controls.push({id:'UI-'+String(++count).padStart(5,'0'),source:rel(file),line:position(node),tag:name,label:['input','textarea','select'].includes(name)?label(node,node):content,attributes:attrs,conditions:conditions(node),formSubmit:form?attr(form.openingElement,'onSubmit'):null,options});
   }
  }
  if(ts.isCallExpression(node)){
   const callee=raw(node.expression);
   if(['confirm','window.confirm','alert','window.alert','prompt','window.prompt'].includes(callee))catalogue.controls.push({id:'UI-'+String(++count).padStart(5,'0'),source:rel(file),line:position(node),tag:'native-dialog',label:raw(node.arguments[0]),attributes:{trigger:callee,message:raw(node.arguments[0])},conditions:conditions(node),formSubmit:null,options:[]});
   if(callee==='fetch'||/^(hostApi|giftApi|api|call|request|hostRequest|aiDesign|generateAiInvitation|paymentsApi|commerceApi)/.test(callee))catalogue.requests.push({source:rel(file),line:position(node),callee,arguments:node.arguments.map(raw)});
   if(callee.endsWith('.map'))catalogue.dynamicSources.push({source:rel(file),line:position(node),collection:raw(node.expression.expression),parameters:node.arguments[0]?.parameters?.map(raw)||[]});
  }
  ts.forEachChild(node,visit);
 }
 visit(tree);
 const local=[];for(const dependency of imports.filter(s=>s.startsWith('.'))){const base=path.resolve(path.dirname(file),dependency);for(const suffix of ['.tsx','.ts','.mjs','.js','/index.tsx'])if(fs.existsSync(base+suffix)){local.push(rel(base+suffix));break;}}
 catalogue.files.push({source:rel(file),imports,localDependencies:local,states,layout});
 if(path.basename(file)==='page.tsx')catalogue.routes.push({route:'/'+path.relative(path.join(web,'app'),path.dirname(file)).split(path.sep).join('/'),source:rel(file)});
}
const map=new Map(catalogue.files.map(f=>[f.source,f]));
for(const route of catalogue.routes){const seen=new Set();function include(file){if(seen.has(file))return;seen.add(file);for(const dep of map.get(file)?.localDependencies||[])include(dep)}include(route.source);include('web/app/layout.tsx');route.composition=[...seen].filter(f=>map.has(f));route.controls=catalogue.controls.filter(c=>seen.has(c.source)).map(c=>c.id);}
fs.writeFileSync(path.join(root,'docs/application/catalogue/frontend.json'),JSON.stringify(catalogue,null,2));
console.log(JSON.stringify({routes:catalogue.routes.length,components:catalogue.files.length,controls:catalogue.controls.length,requests:catalogue.requests.length}));
