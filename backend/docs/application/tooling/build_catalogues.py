#!/usr/bin/env python3
"""Build reviewable reference appendices from versioned metadata exports. No production access."""
import json,re,os,collections,hashlib
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3];REPO=ROOT.parent;DOC=ROOT/'docs/application';CAT=DOC/'catalogue'
load=lambda f:json.loads((CAT/f).read_text())
S=load('database.json');A=load('api-runtime.json');B=load('backend-source.json');F=load('frontend.json');D=load('dependencies.json')
def esc(s):return str(s if s is not None else '—').replace('|','\\|').replace('\n','<br>').replace('\r','')
def inline(s):return '`'+str(s).replace('`','\u2032').replace('\n',' ')+'`'
def link(source,line=1,document='x.md'):
 p=REPO/source;dest=os.path.relpath(p,(DOC/document).parent);return f'[{source}:{line}]({dest}#L{line})'
def write(name,text): (DOC/name).write_text(text.rstrip()+'\n')
def table(headers,rows):return '| '+' | '.join(headers)+' |\n| '+' | '.join('---' for _ in headers)+' |\n'+''.join('| '+' | '.join(esc(x) for x in row)+' |\n' for row in rows)
# Complete physical schema, not a reconstruction of ORM annotations.
lines=['# Database catalogue','',f'Fresh PostgreSQL migration snapshot: V{S["migration_version"]}. BASE TABLE and VIEW entries are distinguished. Column defaults are SQL expressions, not application defaults. FK rules below are the actual database-enforced relationships; logical associations are described in Document 1. No customer rows were exported.','']
columns=collections.defaultdict(list);constraints=collections.defaultdict(list);indexes=collections.defaultdict(list)
for x in S['columns']:columns[x['table_name']].append(x)
for x in S['constraints']:constraints[x['table_name']].append(x)
for x in S['indexes']:indexes[x['table_name']].append(x)
def table_purpose(name):
 if name=='flyway_schema_history':return 'Flyway infrastructure: migration execution and checksum history.'
 if name in ['users','events','gifts','contributions']:return {'users':'Authenticated account identity and credential metadata.','events':'Host-owned celebration, invitation identity, lifecycle and delivery details.','gifts':'Host wishlist gift and its publication/funding metadata.','contributions':'Confirmed or recorded payment contribution entities; inspect allocation/ledger records for financial effects.'}[name]
 if name.startswith(('platform_','payment_control','payment_channel')):return 'Administrative configuration, grants, revision controls or change history; use the documented admin workflow.'
 if name.startswith(('gift_group','group_','guest_gift')):return 'Invitation/group/individual gift coordination, contribution reporting or private gift progress. Monetary reporting is distinct from verified settlement.'
 if name.startswith(('payment_','wallet_','contribution_','host_payout','payout_','hybrid_')):return 'Payment, accounting, pricing, reconciliation or blocked-payout workflow. State and idempotency constraints are listed below.'
 if name.startswith(('host_','event_payment')):return 'Host receiving destinations, event-scoped account selection or independent account-verification attempts.'
 if name.startswith(('account_','phone_','otp_','signup_','username_','credential_')):return 'Authentication, contact verification, account recovery, throttling or session invalidation metadata.'
 if name.startswith(('commercial_','affiliate_','routing_')):return 'Monetization, commercial access, attribution, provider-reported statements or routing-policy history.'
 if name.startswith(('invitation_','qr_','gift_photo','gift_retailer')):return 'Invitation/media/AI request metadata or validated retailer lookup tracking.'
 if name.startswith('event_'):return 'Event-scoped guest responses, guestbook, notification preferences or submission limits.'
 if name.startswith(('notification_','payment_notification')):return 'Notification intent/status history; a stored row does not establish transport delivery.'
 if name=='audit_logs':return 'Application audit history for actor/resource changes.'
 return 'Persisted application object; exact structural contract follows. Narrative ownership beyond its FK/consumer code requires review.'
for obj in S['tables']:
 name=obj['table_name'];lines+=['## '+name,'',f'Type: **{obj["table_type"]}**. Purpose: {table_purpose(name)}','',table(['Column','PostgreSQL type','Nullable','SQL default','Length / precision / scale','Description / validation'],[(x['column_name'],x['data_type']+' / '+x['udt_name'],x['is_nullable'],inline(x['column_default']) if x['column_default'] is not None else 'No SQL default',f'{x["character_maximum_length"]} / {x["numeric_precision"]} / {x["numeric_scale"]}', 'PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. '+('Identifier/reference.' if x['column_name'].endswith('_id') or x['column_name']=='id' else 'State value; see CHECK constraints.' if 'status' in x['column_name'] or x['column_name']=='state' else 'Sensitive representation; do not expose plaintext or hashes.' if re.search('password|cipher|secret|hash',x['column_name']) else 'Money in integer paise; preserve distinction between reported, confirmed and settled.' if 'paise' in x['column_name'] else 'Stored '+x['column_name'].replace('_',' ')+'.')) for x in columns[name]]),'']
 if constraints[name]:lines+=['### Constraints','',table(['Name','Type (p primary, f foreign, u unique, c check)','Exact definition'],[(x['name'],x['type'],inline(x['definition'])) for x in constraints[name]]),'']
 if indexes[name]:lines+=['### Indexes','',table(['Index','Definition'],[(x['name'],inline(x['definition'])) for x in indexes[name]]),'']
 consumers=[]
 for f in B['javaFiles']:
  p=REPO/f['source']
  if re.search(r'\b'+re.escape(name)+r'\b',p.read_text()):consumers.append(link(f['source'],1,'DATABASE_CATALOGUE.md'))
 lines+=['Consumers: '+('; '.join(consumers) if consumers else 'No direct Java literal reference found; migrations, views, triggers or indirect table selection may be the consumer.'),'']
for kind in ['views','triggers','routines']:
 lines+=['# '+kind.title(),'']
 for obj in S[kind]:lines+=['## '+obj['name'],'',('Table: '+obj['table_name'] if 'table_name' in obj else ''),'```sql',obj['definition'].strip(),'```','']
lines+=['# Row-level security','',table(['Table','RLS enabled','RLS forced'],[(x['table_name'],x['enabled'],x['forced']) for x in S['row_security']]),'',f'Policies exported: {len(S["policies"])}. Row-level ownership is principally enforced in the application. Do not describe PostgreSQL RLS as deployed where the catalogue shows false.']
write('DATABASE_CATALOGUE.md','\n'.join(lines))
write('RELATIONSHIP_MATRIX.md','# Database relationship matrix\n\nEvery row is a foreign-key column. Composite FKs contribute multiple rows. Deletion/update behavior is the exact SQL definition.\n\n'+table(['Constraint','From table.column','To table.column','Definition'],[(x['name'],x['source_table']+'.'+x['source_column'],x['target_table']+'.'+x['target_column'],inline(x['definition'])) for x in S['relationships']]))
er=['erDiagram'];base_tables={x['table_name'] for x in S['tables'] if x['table_type']=='BASE TABLE'}
for name in sorted(base_tables):
 er.append('  '+name+' {')
 for column in columns[name]:
  typ=re.sub(r'[^a-zA-Z0-9_]','_',column['udt_name']);er.append('    '+typ+' '+column['column_name'])
 er.append('  }')
seen=set()
for r in S['relationships']:
 key=(r['source_table'],r['target_table'],r['name'])
 if key in seen:continue
 seen.add(key);er.append('  '+r['target_table']+' ||--o{ '+r['source_table']+' : "'+r['name']+'"')
write('complete-er.mmd','\n'.join(er))
# Request/response records and enums are obtained by reflection, including conditional controllers.
models=A['models'];model_lines=['# API data models','', 'Field annotations are the actual reflected Java constraints. A field without an annotation is not thereby optional: handler/service guards and database constraints may still require it. Nullable/default/business meaning absent from the declaration is explicitly unspecified. Money fields named *Paise are integer paise; gift targetAmount is a decimal rupee value where declared BigDecimal.','']
for name,model in sorted(models.items()):
 model_lines+=['## '+name,'',f'Kind: {model["kind"]}.','']
 if 'values' in model:model_lines+=['Allowed values: '+', '.join(map(inline,model['values'])),'']
 else:model_lines +=[table(['Field','Java/JSON type','Validation / annotations','Default / optionality'],[(x['name'],x['type'],'; '.join(map(inline,x['annotations'])) or 'No field annotation', 'Not declared; constructor, handler, service and DB may supply a value. See source/rules.') for x in model.get('fields',[])]),'']
write('API_MODELS.md','\n'.join(model_lines))
# Expand each route/method pair. Separate registration from source declaration.
runtime={(x['controller'],x['method'],p,v) for x in A['endpoints'] for p in x['paths'] for v in (x['verbs'] or ['ANY'])}
declarations=A.get('declaredEndpoints',A['endpoints']);source_map={(x['controller'],x['method']):x for x in B['sourceEndpoints']}
operations=[]
for item in declarations:
 for p in item['paths']:
  p=p.rstrip('/') or '/'
  for v in item['verbs'] or ['ANY']:
   key=(item['controller'],item['method']);src=source_map.get(key,{});row=dict(item,path=p,verb=v,source=src.get('source','backend/src/main/java/'+item['controller'].replace('.','/')+'.java'),line=src.get('line',1),registered=(item['controller'],item['method'],p,v) in runtime or (item['controller'],item['method'],p+'/',v) in runtime,conditional=src.get('conditional',[]));operations.append(row)
operations.sort(key=lambda x:(x['path'],x['verb']))
UUID='123e4567-e89b-42d3-a456-426614174000'
def sample(typ,name='',depth=0):
 if depth>4:return '<nested '+typ+'>'
 if typ in models:
  m=models[typ]
  if 'values' in m:return m['values'][0] if m['values'] else '<enum>'
  return {f['name']:sample(f['type'],f['name'],depth+1) for f in m.get('fields',[]) if not any('JsonIgnore' in a for a in f['annotations'])}
 if re.search(r'(List|Set|Collection)<',typ):return [sample(typ.split('<',1)[1][:-1],name,depth+1)]
 if 'ResponseEntity<' in typ:return sample(typ.split('<',1)[1][:-1],name,depth+1)
 if 'Map<' in typ or typ=='java.lang.Object':return {'_contract':'Dynamic response keys: inspect linked handler and module narrative; no fixed schema is asserted.'}
 if 'UUID' in typ:return UUID
 if 'LocalDate' in typ:return '2027-02-14'
 if any(x in typ for x in ['Instant','DateTime','Timestamp']):return '2027-02-14T12:00:00Z'
 if typ in ['boolean','java.lang.Boolean']:return True
 if re.search('int$|long$|Integer|Long|BigDecimal|double|Double',typ):return 10000 if 'paise' in name.lower() else 1
 if typ in ['void','java.lang.Void']:return None
 if 'byte[]' in typ:return '<binary bytes: use response Content-Type>'
 if 'email' in name.lower():return 'guest@example.test'
 if 'phone' in name.lower() or 'mobile' in name.lower():return '+919000000001'
 if 'password' in name.lower():return 'ExamplePassword123!'
 if name.lower().endswith('url') or 'link' in name.lower():return 'https://shop.example.test/product'
 return '<'+(name or typ)+': validated value>'
def auth(op):
 p=op['path'];c=op['controller']
 if p.startswith('/api/admin') or 'Admin' in c:return 'Authenticated PLATFORM_ADMIN; controller/service admin check and CSRF on mutations.'
 if '/webhooks/' in p:return 'Provider signature verification; no host login. This webhook is explicitly exempted from CSRF.'
 if p.startswith('/api/public/groups'):
  return 'Invitation token plus endpoint-specific creator/member/individual browser session. Registration/login and allowed guest contribution routes do not require a registered host. Creator mutations use X-Group-Page-Id. Consult handler/session guards.'
 if p.startswith('/api/public/') or p.startswith('/api/auth/') and any(x in p for x in ['login','register','csrf','signup-email','recovery/request','recovery/complete','username/request']):return 'Public boundary subject to route-specific validation, published-event/module gates and rate limits. CSRF may still be required; public does not mean exempt.'
 return 'Authenticated principal; ownership/role is enforced by route filter and controller/service resource checks. An authenticated user cannot assume access to another host’s UUID.'
api_lines=['# Complete API operation catalogue','',f'{len(operations)} expanded source-declared operations. Runtime registration is recorded separately; conditional routes can be absent unless their property/profile is enabled. Backend paths below exclude the frontend `/backend` reverse-proxy prefix.','', 'Examples are illustrative schema shapes, not fixture-backed execution transcripts. Placeholder identifiers must be replaced by owned, correctly scoped resources; business enum values and validation annotations are authoritative in [API models](API_MODELS.md). Dynamic maps and polymorphic responses are explicitly labelled rather than inventing a contract. Read [Document 1](01_TECHNICAL_SPECIFICATION.md) for common error, security, money and retry contracts.','',table(['ID','Method','Path','Handler','Registered in export'],[(f'API-{i:04}',op['verb'],inline(op['path']),op['controller'].split('.')[-1]+'.'+op['method'],op['registered']) for i,op in enumerate(operations,1)]),'']
for i,op in enumerate(operations,1):
 op['id']=f'API-{i:04}';body=[p for p in op['parameters'] if any('RequestBody' in a for a in p['annotations'])];parameters=[p for p in op['parameters'] if not any(x in p['type'] for x in ['HttpServletRequest','HttpServletResponse','HttpSession','CsrfToken'])]
 source_file=REPO/op['source'];source_text=source_file.read_text() if source_file.exists() else ''
 dbobjects=sorted(t for t in base_tables if re.search(r'\b'+re.escape(t)+r'\b',source_text))
 response=sample(op['response']);request=sample(body[0]['type']) if body else None
 status='201' if any('CREATED' in a for a in op.get('annotations',[])) else '204' if any('NO_CONTENT' in a for a in op.get('annotations',[])) else '200 unless the handler constructs another ResponseEntity status'
 api_lines +=[f'## {op["id"]} — {op["verb"]} {op["path"]}','',f'Handler: {op["controller"]}.{op["method"]}. Source: {link(op["source"],op["line"],"API_CATALOGUE.md")}.','',f'Purpose: invoke the `{op["method"]}` action in the `{op["controller"].split(".")[-1]}` module; the module workflow describes its business intent.', '', '**Access:** '+auth(op),'',f'**Registration:** {"registered in isolated snapshot" if op["registered"] else "source-declared; not registered in exported application context"}. Conditional registration: '+('; '.join(map(inline,op['conditional'])) or 'No class-level conditional declaration found; service-level gates can still apply.'),'', '**Headers:** `Accept` appropriate to the response; `Content-Type: application/json` for JSON bodies; multipart uses a browser-generated boundary. Send the same-origin session cookie where required. For protected mutations bootstrap `/api/auth/csrf`, send `X-XSRF-TOKEN`; creator sessions additionally require `X-Group-Page-Id`. Never place API keys in browser requests.','',table(['Parameter','Location / annotation','Type','Required / default'],[(p['name'],'; '.join(p['annotations']) or 'Framework-bound/unannotated; inspect signature',p['type'],'As declared by the annotation; no additional undocumented default is asserted.') for p in parameters]),'', '**Request example / shape:**','```json',json.dumps(request if body else {'pathAndQueryParameters':{p['name']:sample(p['type'],p['name']) for p in parameters}},indent=2),'```','',f'**Success:** {status}. Declared response `{op["response"]}`. Produces: {", ".join(op.get("produces",[])) or "handler content type / negotiated JSON"}.','```json',json.dumps(response,indent=2),'```','', '**Validation/business/server errors:** 400 malformed/binding/validation input; 401 no authenticated session where required; 403 CSRF/role policy; 404 absent or inaccessible resource/disabled feature; 409 conflicting state/idempotency/revision; 413/415 upload size/type where used; 422 AI refusal where used; 429 explicit limits; 5xx unavailable provider/configuration/storage. These are a common catalogue, not a claim that every handler returns every code. Exact guard conditions and messages are linked by handler source and [guard catalogue](BUSINESS_RULE_CATALOGUE.md). Error envelopes are not globally standardized; representative shapes follow.','```json',json.dumps({'problemDetailExample':{'status':409,'title':'Conflict','detail':'<safe handler-defined reason>'},'springErrorExample':{'status':400,'error':'Bad Request','path':op['path']}},indent=2),'```','', '**Database effects:** '+(', '.join(map(inline,dbobjects))+' are directly referenced in this controller source; delegated services may reference additional objects. GET must not be assumed side-effect-free solely from its name; inspect the handler and service. ' if dbobjects else 'The controller delegates to services or does not declare SQL; follow the linked handler into its injected services. ')+ 'Use idempotency, status checks and transactions exactly as implemented; object presence is not proof of payment or notification delivery.','', '**Rate / timeout / retry:** consult the request limiter/HTTP client in the source call chain and the full [configuration catalogue](CONFIGURATION_CATALOGUE.md). No universal per-endpoint rate, timeout or retry policy exists. Retry reads conservatively; never replay a write or payment with a new idempotency key after an uncertain result.','']
write('API_CATALOGUE.md','\n'.join(api_lines));(CAT/'api-operations.json').write_text(json.dumps(operations,indent=2))
# UI fields, buttons, dialogs and navigation controls, preserving runtime-dependent expressions.
ui=['# Complete UI control catalogue','',f'{len(F["controls"])} JSX control declarations across {len(F["files"])} TSX files. A generated/repeated control is one declaration instantiated for its data collection. Dynamic collections and conditional expressions are preserved below and in frontend.json; a static scan cannot enumerate future guests, gifts or administrator-created options. Shared controls are specified once and referenced by route composition.','', 'Native field constraints and React bindings are source facts. A missing HTML required/min/max attribute is not a promise that the API accepts the omission. Type coercion, paise conversion, authorization and DB constraints remain server responsibilities. Unresolved semantic mappings are not guessed; the binding, component requests, API records and DB catalogue provide the traceability chain.','']
files_by_source={x['source']:x for x in F['files']};groups=collections.defaultdict(list)
for c in F['controls']:groups[c['source']].append(c)
for source,controls in sorted(groups.items()):
 ui+=['## '+source,'',link(source,1,'UI_CONTROLS.md'),'']
 f=files_by_source[source];requests=[r for r in F['requests'] if r['source']==source]
 ui+=['### API/action references in this component','',table(['Line','Call','Arguments'],[(r['line'],inline(r['callee']),'; '.join(map(inline,r['arguments']))) for r in requests]),'']
 if f['states']:ui+=['### Initial state declarations','',table(['Binding','Initial value / expression','Declared type','Source line'],[(x['name'],inline(x['initial']),x['type'],x['line']) for x in f['states']]),'']
 for c in controls:
  attrs=c['attributes'];kind='Field' if c['tag'] in ['input','textarea','select'] else 'Button/action' if c['tag']=='button' else 'Navigation/dialog/form/progress'
  name=attrs.get('name') or attrs.get('id') or attrs.get('value') or attrs.get('checked') or c['label'] or c['tag'];default='Not statically resolved; inspect the initial state declarations and passed props.'
  bound=attrs.get('value') or attrs.get('checked') or ''
  state_match=re.fullmatch(r'\{(\w+)(?:\.(\w+))?\}',bound)
  if state_match:
   st=next((x for x in f['states'] if x['name']==state_match.group(1)),None)
   if st:default=(st.get('members') or {}).get(state_match.group(2),st['initial']) if state_match.group(2) else st['initial']
  dtype='boolean' if attrs.get('type')=='"checkbox"' else 'Numeric input text; check explicit Number/paise conversion in action' if attrs.get('type')=='"number"' else 'String / selected value; dynamic props may supply another type'
  security='Untrusted input: server validation and output escaping required; UI visibility is not authorization.'
  alltext=(str(name)+' '+str(c['label'])).lower()
  if re.search('password|pin|otp|secret',alltext):security+=' Credential/verification material: never log or expose plaintext; use the scoped authentication/verification service.'
  if re.search('email|phone|mobile|account|upi|bank',alltext):security+=' Contact or payment instruction data: preserve owner/session scoping and masking/encryption where applicable.'
  ui+=[f'<a id="{c["id"].lower()}"></a>',f'### {c["id"]} — {kind}: {c["label"] or c["tag"]}','',link(source,c['line'],'UI_CONTROLS.md'),'',table(['Specification','Observed value / unresolved contract'],[
   ('Field/button name',inline(name)),('Display label',c['label'] or 'No static text; dynamic expression/source required'),('Control type',c['tag']+' '+str(attrs.get('type',''))),('Data type',dtype),('UI state / API binding',inline(bound or attrs.get('onClick') or c['formSubmit'] or 'See attributes')),
   ('Database mapping','No implicit ORM mapping for a UI control. Use the verified module field map in Document 1, API models and database catalogue; unresolved per-control cross-module mappings are a documented gap.'),('API mapping','Component call references above; submit handler '+inline(c['formSubmit'] or attrs.get('onClick') or 'none declared on this node')),('Required / optional',attrs.get('required','No native required attribute; server may still require it')),('Input format / validation',attrs.get('pattern','No HTML pattern; consult type, handler guards and API model')),('Min length / max length',str(attrs.get('minLength','not declared'))+' / '+str(attrs.get('maxLength','not declared'))),('Numeric min / max / step',str(attrs.get('min','not declared'))+' / '+str(attrs.get('max','not declared'))+' / '+str(attrs.get('step','not declared'))),('Default value',inline(default)),('Allowed values','Select options below or dynamic collection source; otherwise controlled by the declared API/DB enum/validation.'),('Error messages','Native browser validation is locale-dependent. Application alerts are rendered by the component’s error/notice state, populated by the calls above; server messages are in the guard catalogue.'),('Visibility conditions',' AND / nested: '.join(map(inline,c['conditions'])) or 'No enclosing JSX conditional found; the parent component may be conditional.'),('Editability conditions','disabled='+str(attrs.get('disabled','not declared'))+'; readOnly='+str(attrs.get('readOnly','not declared'))),('Location',source+':'+str(c['line'])),('Action / validation triggered',inline(attrs.get('onClick') or attrs.get('onChange') or c['formSubmit'] or attrs.get('href') or 'Container / display control')),('Database changes','Determined by the invoked API/service transaction, not by this JSX declaration. See API catalogue and workflow steps.'),('Success / failure behavior','Inspect the exact action expression and component state transitions; reference Document 2 common and route-specific states. No unverified success is asserted.'),('Security considerations',security)
  ]),'', '**Complete JSX attributes**','```json',json.dumps(attrs,indent=2),'```','']
  if c['options']:ui+=[table(['Option value','Exact option declaration'],[(x['value'],inline(x['text'])) for x in c['options']]),'']
ui+=['# Dynamic control sources','',table(['Source','Line','Collection expression','Iterator parameters'],[(x['source'],x['line'],inline(x['collection']),', '.join(x['parameters'])) for x in F['dynamicSources']])]
write('UI_CONTROLS.md','\n'.join(ui))
# Rule inventory: all source throw guards and all declarative validations, without inventing rationales.
rules=['# Business-rule and validation guard catalogue','', 'A guard is an observed enforcement point, not necessarily a complete product requirement. Why a developer chose a threshold, legal exceptions, or support override policy may be unspecified. Preserve these guards until a reviewed business change updates code, tests, diagrams and this catalogue. General rationale: prevent the rejected condition from entering the corresponding operation; exact business rationale and permitted exceptions are not inferred. For every entry, applicability is the linked guard context, failure is the exact throw, exceptions are only those explicitly bypassed by code, and edge cases include boundary/null/concurrent/replay values.','']
for module in sorted(set(x['module'] for x in B['guards'])):
 rules+=['## '+module,'']
 for r in [x for x in B['guards'] if x['module']==module]:rules+=[f'### {r["id"]} — {r["source"]}:{r["line"]}','',link(r['source'],r['line'],'BUSINESS_RULE_CATALOGUE.md'),'','When / guard context:','```java',r['guardContext'],'```','Failure / enforcement:','```java',r['failure'],'```','Rationale beyond this enforcement is not declared at this site. There is no documented administrative exception unless the surrounding source defines one. Test null/boundary values and transaction/retry behavior before altering the rule.','']
rules+=['# Declarative Java validations','',table(['Source','Line','Annotation'],[(x['source'],x['line'],inline(x['constraint'])) for x in B['validationAnnotations']]),'','# Database-enforced rules','', 'All primary/foreign keys, checks, unique constraints and immutable-history triggers are enumerated in [Database catalogue](DATABASE_CATALOGUE.md); those are additional business/data-integrity enforcement points, not replacements for request validation.']
write('BUSINESS_RULE_CATALOGUE.md','\n'.join(rules))
# Safe configuration catalogue.
def config_purpose(key):
 prefixes=[('spring.datasource','JDBC connectivity and DB credentials'),('spring.jpa','ORM/schema validation and persistence behavior'),('spring.flyway','Versioned migration execution'),('spring.servlet','HTTP multipart request limits'),('management.','Actuator exposure/latency metrics'),('gift.payment.razorpayx','Independent bank-account validation configuration; not live payouts'),('gift.payment.razorpay','Razorpay contribution/commercial checkout and webhook configuration'),('gift.payment.encryption','Receiving-account field encryption key'),('gift.payment.fingerprint','Stable receiving-account duplicate fingerprint key'),('gift.payment.settlement','Conditional blocked settlement/payout UI API, not provider dispatch'),('gift.payment.','Receiving destination, manual contribution or account-verification gate'),('gift.invitation.ai','AI invitation provider gate, credential or model'),('gift.otp','Scoped OTP or email/SMS verification and delivery'),('gift.sms','Phone-OTP SMS transport selection'),('gift.verification','Default verification channel behavior; runtime platform controls also apply'),('gift.cashfree','Cashfree Secure ID sandbox adapter gates/credentials'),('gift.guestbook','Private normalized guest photo storage'),('gift.guest-base','Public invitation link base'),('spring.profiles','Environment/profile selection; local simulators must not be used in production')]
 return next((v for p,v in prefixes if key.startswith(p)),'Framework/application property; inspect the declaration for its exact consumer')
conf=['# Configuration catalogue','',f'{len(B["configuration"])} source-declared property keys; values in private configuration are redacted. This is not a dump of a deployed environment. Properties can be supplied by Spring relaxed environment binding even when no explicit environment name appears below. For example `gift.payment.profiles.enabled` can be supplied as `GIFT_PAYMENT_PROFILES_ENABLED`; check the deployed configuration rather than assuming an example file is active.','']
for c in B['configuration']:
 key=c['name'];conf+=['## '+key,'',table(['Attribute','Specification'],[('Purpose',config_purpose(key)),('Environments','Development/test/staging/production as selected by Spring profiles and deployment configuration.'),('Explicit environment names',', '.join(c['environmentVariables']) or 'No explicit placeholder name found; Spring relaxed binding may apply.'),('Conventional environment name',key.upper().replace('.','_').replace('-','_')),('Sensitivity','Sensitive; use a secret reference, never a checked-in production value.' if c['sensitive'] else 'Not classified as a secret by the catalogue; review deployment exposure.'),('Production value','Deployment secret reference; provision and rotation-test before enabling the dependent feature.' if c['sensitive'] else 'Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3.'),('Dependencies','Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely.'),('Default value','See each declaration: injection fallback, example file and active local property are separate layers and may conflict.')]),'',table(['Source','Declaration kind','Safe observed value / expression'],[(x['source'],x['kind'],inline(x['value'])) for x in c['declarations']]),'']
conf+=['# Database-backed configuration','', 'These runtime tables also contain production configuration, grants, or commercial policy. Their fields/defaults/constraints are part of the [database catalogue](DATABASE_CATALOGUE.md), and changes belong to authenticated admin/owner APIs rather than environment substitution.','',table(['Configuration table','Purpose'],[(n,table_purpose(n)) for n in sorted(base_tables) if re.search(r'platform_|policy|settings|permissions|controls|revision|event_payment_account_links',n)]),'','# Deployment-layer settings missing from source','', 'TLS termination, canonical domains, DNS, trusted forwarded headers, session cookie Secure/SameSite/domain, external session store, JVM/container CPU/memory, DB pools/HA/PITR, storage mount replication, backup schedule, alert endpoints, WAF/CDN policy and telemetry retention are not completely declared here. Proposed values and unresolved decisions are in Document 3. JVM/AWS SDK environment credentials also come from their runtime credential chain and are not literal application secrets in this export.']
write('CONFIGURATION_CATALOGUE.md','\n'.join(conf))
write('MIGRATION_CATALOGUE.md','# Migration catalogue\n\nAll migrations are versioned source inputs. Never edit an already-applied migration to repair production state. Add a reviewed forward migration.\n\n'+table(['Version','Migration','Objects declared/altered','SHA-256'],[(x['version'],link(x['source'],1,'MIGRATION_CATALOGUE.md'),', '.join(x['objects']),x['sha256']) for x in B['migrations']]))
write('DEPENDENCY_CATALOGUE.md','# Dependency catalogue\n\nSource target Java 21. Maven parent Spring Boot 4.1.1. A missing explicit Maven version means parent/dependency-management resolution, not a pinned version verified by this catalogue. npm versions below are resolved package-lock values; semver ranges in package.json are not the same thing.\n\n## Maven declared dependencies\n\n'+table(['Group','Artifact','Declared version','Scope'],[(x['groupId'],x['artifactId'],x['version'] or 'Managed by parent/BOM',x['scope'] or 'compile/default') for x in D['mavenDeclared']])+'\n## npm lockfile packages\n\n'+table(['Lockfile path','Resolved version','Development','Declared license'],[(x['path'],x['version'],x['dev'],x['license']) for x in D['npmResolved']]))
summary={'javaFiles':len(B['javaFiles']),'pageRoutes':len(F['routes']),'tsxFiles':len(F['files']),'jsxControlDeclarations':len(F['controls']),'fieldDeclarations':sum(x['tag'] in ['input','textarea','select'] for x in F['controls']),'buttonDeclarations':sum(x['tag']=='button' for x in F['controls']),'runtimeHandlerMappings':len(A['endpoints']),'sourceHandlerMappings':len(B['sourceEndpoints']),'expandedApiOperations':len(operations),'conditionalUnregisteredOperations':sum(not x['registered'] for x in operations),'apiModels':len(models),'baseTables':len(base_tables),'views':len(S['views']),'columnsIncludingViews':len(S['columns']),'constraints':len(S['constraints']),'foreignKeyColumns':len(S['relationships']),'indexes':len(S['indexes']),'triggers':len(S['triggers']),'routines':len(S['routines']),'guardSites':len(B['guards']),'validationAnnotationSites':len(B['validationAnnotations']),'configurationKeys':len(B['configuration']),'migrations':len(B['migrations'])}
(CAT/'coverage.json').write_text(json.dumps(summary,indent=2));print(json.dumps(summary,indent=2))
