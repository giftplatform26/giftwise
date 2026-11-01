#!/usr/bin/env python3
"""Source inventory only. Runtime route/schema export is a separate isolated test."""
from pathlib import Path
import re,json,hashlib,subprocess,datetime
ROOT=Path(__file__).resolve().parents[3];REPO=ROOT.parent;OUT=ROOT/'docs/application/catalogue'
files=sorted((ROOT/'src/main/java').rglob('*.java'))
def source(p):return str(p.relative_to(REPO))
def redact(value):
 value=re.sub(r'sk-[A-Za-z0-9_-]{12,}','<redacted-secret>',value)
 return value
# Mask lexical strings/comments without shifting positions, so semicolons inside messages are not terminators.
def mask(text):
 pat=r'"""[\s\S]*?"""|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\n]*|/\*[\s\S]*?\*/'
 return re.sub(pat,lambda m:''.join('\n' if c=='\n' else ' ' for c in m.group()),text)
rules=[];classes=[];config={};integrations=[];annotations=[]
sensitive=re.compile(r'password|secret|api.?key|private.?key|encryption.?key|client.?id|credential|username|mail\.from|fingerprint|hmac|auth.?key|source.?account',re.I)
def setting(key,value,location,kind):
 value=value.strip();row=config.setdefault(key,{'name':key,'declarations':[],'environmentVariables':[],'sensitive':bool(sensitive.search(key))})
 for env in re.findall(r'\$\{([A-Z][A-Z0-9_]*)',value):
  if env not in row['environmentVariables']:row['environmentVariables'].append(env)
 if row['sensitive']:
  value=re.sub(r'(\$\{[^}:]+):[^}]*}',r'\1:<redacted>}',value) if value.startswith('${') else '<redacted configured value>' if value else '<empty>'
 row['declarations'].append({'source':location,'kind':kind,'value':redact(value)})
for p in files:
 text=p.read_text();masked=mask(text);src=source(p)
 module=p.relative_to(ROOT/'src/main/java/com/giftplatform').parts[0] if 'com/giftplatform' in str(p) else 'application'
 classes.append({'source':src,'module':module,'sha256':hashlib.sha256(text.encode()).hexdigest(),'lines':len(text.splitlines()),'annotations':sorted(set(re.findall(r'@(\w+)',text)))})
 for m in re.finditer(r'\bthrow\b',masked):
  end=masked.find(';',m.start());end=len(text) if end<0 else end
  line=text.count('\n',0,m.start())+1
  prefix=text[max(0,m.start()-350):m.start()].strip()
  rules.append({'id':f'RULE-{len(rules)+1:04}','source':src,'line':line,'module':module,'guardContext':redact(prefix),'failure':redact(text[m.start():end+1].strip())})
 for m in re.finditer(r'@Value\(\s*"((?:\\.|[^"\\])*)"\s*\)',text):
  expression=m.group(1)
  if expression.startswith('${'):
   body=expression[2:-1];key=body.split(':',1)[0];setting(key,expression,f'{src}:{text.count(chr(10),0,m.start())+1}','@Value injection')
 for m in re.finditer(r'https?://[^\s"<>\\]+',text):
  url=m.group().rstrip(');,')
  if not any(s in url for s in ['schema.org','example','localhost','127.0.0.1','java.sun','www.w3.org']):integrations.append({'url':redact(url),'source':src,'line':text.count('\n',0,m.start())+1})
 for m in re.finditer(r'@(NotNull|NotBlank|NotEmpty|Size|Min|Max|Positive|PositiveOrZero|DecimalMin|DecimalMax|Email|Pattern|AssertTrue)(?:\([^\n]*?\))?',text):annotations.append({'source':src,'line':text.count('\n',0,m.start())+1,'constraint':m.group()})
 # Configuration referenced through Environment and binding classes is not limited to @Value.
 for m in re.finditer(r'getProperty\(\s*"([a-z][a-z0-9_.-]+)"(?:\s*,\s*([^)]*))?\)',text):
  setting(m.group(1),m.group(2) or '<no source default>',f'{src}:{text.count(chr(10),0,m.start())+1}','Environment.getProperty')
 bound=re.search(r'@ConfigurationProperties\(prefix\s*=\s*"([^\"]+)"\)',text)
 if bound:
  record=re.search(r'public record \w+\(([\s\S]*?)\)\s*\{',text)
  if record:
   for item in record.group(1).split(','):
    member=item.strip().split()[-1];member=re.sub(r'(?<!^)(?=[A-Z])','-',member).lower();setting(bound.group(1)+'.'+member,'<binding default not declared; inspect safe accessor>',f'{src}:1','@ConfigurationProperties record')
 for m in re.finditer(r'@ConditionalOnProperty\(([\s\S]*?)\)',text):
  body=m.group(1);prefix=re.search(r'prefix\s*=\s*"([^\"]+)"',body);name=re.search(r'name\s*=\s*"([^\"]+)"',body)
  if name:
   key=(prefix.group(1)+'.' if prefix else '')+name.group(1);setting(key,body,f'{src}:{text.count(chr(10),0,m.start())+1}','conditional bean registration')
for p in sorted((ROOT/'src/main/resources').glob('application*.properties*')):
 if p.name.endswith(('.bak','.backup')):continue
 pending='';line_start=1
 for number,line in enumerate(p.read_text().splitlines(),1):
  if not pending:line_start=number
  pending+=line.rstrip('\\')
  if line.endswith('\\'):continue
  if pending.strip() and not pending.lstrip().startswith(('#','!')) and '=' in pending:
   key,value=pending.split('=',1);setting(key.strip(),value,f'{source(p)}:{line_start}',p.name)
  pending=''
for p in sorted((ROOT/'src/main/resources').glob('application*.yml')):
 # Inventory the simple YAML example without requiring a non-project YAML dependency.
 stack=[]
 for number,line in enumerate(p.read_text().splitlines(),1):
  match=re.match(r'^(\s*)([A-Za-z0-9_.-]+):\s*(.*?)\s*$',line)
  if not match:continue
  indent,key,value=match.groups();level=len(indent)
  while stack and stack[-1][0]>=level:stack.pop()
  path='.'.join([v[1] for v in stack]+[key])
  if not value or value.startswith('#'):stack.append((level,key))
  else:setting(path,value,f'{source(p)}:{number}',p.name)
migrations=[]
for p in sorted((ROOT/'src/main/resources/db/migration').glob('V*.sql'),key=lambda p:int(re.search(r'V(\d+)',p.name).group(1))):
 text=p.read_text();migrations.append({'version':int(re.search(r'V(\d+)',p.name).group(1)),'name':p.name,'source':source(p),'sha256':hashlib.sha256(text.encode()).hexdigest(),'objects':sorted(set(re.findall(r'(?i)(?:CREATE\s+(?:OR\s+REPLACE\s+)?(?:TABLE|VIEW|FUNCTION|TRIGGER|INDEX)(?:\s+IF\s+NOT\s+EXISTS)?|ALTER\s+TABLE)\s+([a-z_][a-z_0-9]*)',text)))})
source_endpoints=[];source_records=[]
for p in files:
 text=p.read_text();masked=mask(text);src=source(p)
 for record in re.finditer(r'\brecord\s+(\w+)\s*\(',masked):
  start=record.end();depth=1;end=start
  while end<len(masked) and depth:
   depth+=(masked[end]=='(')-(masked[end]==')');end+=1
  source_records.append({'source':src,'line':text.count('\n',0,record.start())+1,'name':record.group(1),'components':text[start:end-1]})
 if '@RestController' not in text and '@Controller' not in text:continue
 package=re.search(r'package\s+([\w.]+);',text);owner=(package.group(1)+'.' if package else '')+p.stem
 class_at=re.search(r'\bclass\s+',masked).start();prefixes=['']
 pattern=r'@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping|RequestMapping)\b'
 for m in re.finditer(pattern,masked):
  end=m.end();args=''
  if end<len(masked) and masked[end]=='(':
   depth=1;start=end+1;end=start
   while end<len(masked) and depth:depth+=(masked[end]=='(')-(masked[end]==')');end+=1
   args=text[start:end-1]
  selected=re.search(r'(?:value|path)\s*=\s*(\{[^}]*\}|"[^\"]*")',args)
  paths=re.findall(r'"([^\"]*)"',selected.group(1)) if selected else re.findall(r'^\s*"([^\"]*)"',args)
  paths=paths or ['']
  if m.start()<class_at:prefixes=paths;continue
  signature=re.search(r'(?:public|protected|private)\s+([\w.$<>?,\[\]\s]+?)\s+(\w+)\s*\(',masked[end:end+1200])
  if not signature:continue
  name=signature.group(2)
  verbs=[m.group(1).replace('Mapping','').upper()] if m.group(1)!='RequestMapping' else re.findall(r'RequestMethod\.(\w+)',args) or ['ANY']
  source_endpoints.append({'controller':owner,'method':name,'paths':[re.sub('/+','/',a+'/'+b).rstrip('/') or '/' for a in prefixes for b in paths],'verbs':verbs,'source':src,'line':text.count('\n',0,m.start())+1,'mappingArguments':args,'response':signature.group(1).strip(),'conditional':re.findall(r'@ConditionalOnProperty\([\s\S]*?\)',text)})
result={'sourceEndpoints':source_endpoints,'sourceRecords':source_records,'snapshotDate':str(datetime.date.today()),'baselineCommit':subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip(),'baselineIsDirty':bool(subprocess.check_output(['git','status','--porcelain'],cwd=ROOT,text=True).strip()),'javaFiles':classes,'guards':rules,'validationAnnotations':annotations,'configuration':sorted(config.values(),key=lambda x:x['name']),'externalUrlDeclarations':integrations,'migrations':migrations}
OUT.mkdir(exist_ok=True);(OUT/'backend-source.json').write_text(json.dumps(result,indent=2))
print(json.dumps({'javaFiles':len(classes),'guardSites':len(rules),'configKeys':len(config),'migrations':len(migrations),'validationAnnotations':len(annotations)}))
