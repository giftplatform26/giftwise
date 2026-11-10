#!/usr/bin/env python3
"""Fail on missing discovered items, broken local targets or credential-like literals."""
import json,re,sys,hashlib
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3];DOC=ROOT/'docs/application';REPO=ROOT.parent;CAT=DOC/'catalogue'
read=lambda n:json.loads((CAT/n).read_text())
F=read('frontend.json');B=read('backend-source.json');S=read('database.json');A=read('api-runtime.json');O=read('api-operations.json');C=read('coverage.json')
errors=[];checks=[]
def check(label,result,detail=''):
 checks.append({'check':label,'passed':bool(result),'detail':detail})
 if not result:errors.append(label+': '+detail)
ui=(DOC/'UI_CONTROLS.md').read_text();screens=(DOC/'SCREEN_CATALOGUE.md').read_text();api=(DOC/'API_CATALOGUE.md').read_text();database=(DOC/'DATABASE_CATALOGUE.md').read_text();rules=(DOC/'BUSINESS_RULE_CATALOGUE.md').read_text();configuration=(DOC/'CONFIGURATION_CATALOGUE.md').read_text()
check('every page route represented',all(r['route'] in screens for r in F['routes']),str(len(F['routes'])))
check('every JSX/native control has a stable anchor',all(f'id="{c["id"].lower()}"' in ui for c in F['controls']),str(len(F['controls'])))
check('all source handler declarations reflected',set((x['controller'],x['method']) for x in B['sourceEndpoints'])==set((x['controller'],x['method']) for x in A['declaredEndpoints']),str(len(B['sourceEndpoints'])))
check('all declared API operations documented',all(('## '+x['id']+' —') in api for x in O),str(len(O)))
check('all table/view objects documented',all(('## '+x['table_name']+'\n') in database for x in S['tables']),str(len(S['tables'])))
check('all guard sites documented',all(('### '+x['id']+' —') in rules for x in B['guards']),str(len(B['guards'])))
check('all source config keys documented',all(('## '+x['name']+'\n') in configuration for x in B['configuration']),str(len(B['configuration'])))
latest_migration=max(int(re.match(r'V(\d+)',p.name).group(1)) for p in (ROOT/'src/main/resources/db/migration').glob('V*__*.sql'))
check('schema export matches latest source migration',str(S['migration_version'])==str(latest_migration),S['migration_version'])
for entry in B['javaFiles']:
 p=REPO/entry['source'];check('source hash '+entry['source'],p.exists() and hashlib.sha256(p.read_bytes()).hexdigest()==entry['sha256'])
link_count=0
for p in DOC.rglob('*.md'):
 text=p.read_text();without_code=re.sub(r'```[\s\S]*?```','',text)
 for target in re.findall(r'\]\(([^)]+)\)',without_code):
  target=target.strip().strip('<>')
  if re.match(r'^(https?://|mailto:|#)',target):continue
  path=target.split('#',1)[0]
  if not path:continue
  link_count+=1;check('local link '+str(p.relative_to(DOC))+' -> '+target,((p.parent/path).resolve()==(CAT/'validation.json').resolve()) or (p.parent/path).exists())
 for pattern in [r'sk-[A-Za-z0-9_-]{12,}',r'AKIA[A-Z0-9]{16}',r'-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----']:
  check('no credential pattern in '+str(p.relative_to(DOC)),re.search(pattern,text) is None)
for p in CAT.glob('*.json'):
 text=p.read_text()
 for pattern in [r'sk-[A-Za-z0-9_-]{12,}',r'AKIA[A-Z0-9]{16}',r'-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----']:
  check('no credential pattern in '+str(p.relative_to(DOC)),re.search(pattern,text) is None)
report={'status':'PASS' if not errors else 'FAIL','coverage':C,'localLinksChecked':link_count,'checks':checks,'errors':errors,'limitations':['Structural source coverage is not product/semantic completeness.','Dynamic response/field mappings and operational unknowns remain explicitly identified in Document 3.','Schema comes from an isolated migrated DB; no live DB drift or deployment was attested.']}
(CAT/'validation.json').write_text(json.dumps(report,indent=2))
print(json.dumps({'status':report['status'],'checks':len(checks),'links':link_count,'errors':errors[:30]},indent=2))
sys.exit(1 if errors else 0)
