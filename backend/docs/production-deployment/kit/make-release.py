#!/usr/bin/env python3
"""Produce a runtime-only Linux amd64 release. Does not upload or deploy."""
import argparse,datetime,hashlib,json,os,re,shutil,subprocess,tarfile,tempfile,zipfile
from pathlib import Path

def run(args,cwd=None):subprocess.run(args,cwd=cwd,check=True)
def sha(path):
 h=hashlib.sha256()
 with path.open('rb') as f:
  for b in iter(lambda:f.read(1024*1024),b''):h.update(b)
 return h.hexdigest()
def valid_asset(path):
 name=path.name.lower()
 return not (name.startswith('.env') or any(x in name for x in ['.backup','.bak','.before-','.pre-']) or name.endswith(('~','.log','.tsbuildinfo','.zip','.md','.pem','.key')))
def legal(text,label):
 if len(text.strip())<200 or any(x in text.lower() for x in ['draft for operator review','todo','replace_me','example.com']):raise SystemExit(label+' must be the complete owner-approved text, without placeholder markers.')
 return text.strip()
def main():
 ap=argparse.ArgumentParser(description=__doc__)
 ap.add_argument('--source-root',type=Path,required=True);ap.add_argument('--output-dir',type=Path,required=True)
 ap.add_argument('--release-id',required=True);ap.add_argument('--privacy-text',type=Path,required=True);ap.add_argument('--terms-text',type=Path,required=True)
 ap.add_argument('--qa-only',action='store_true');ap.add_argument('--legal-approved',action='store_true');ap.add_argument('--legal-effective-date',required=True)
 args=ap.parse_args();root=args.source_root.resolve();out=args.output_dir.resolve();rid=args.release_id
 if not re.fullmatch(r'[A-Za-z0-9][A-Za-z0-9._-]{0,60}',rid):raise SystemExit('Use a simple release ID, such as 2026-11-05-r1.')
 if not args.legal_approved and not args.qa_only:raise SystemExit('Owner approval of the supplied legal texts is required before packaging.')
 datetime.date.fromisoformat(args.legal_effective_date)
 privacy=args.privacy_text.read_text().strip() if args.qa_only else legal(args.privacy_text.read_text(),'Privacy policy');terms=args.terms_text.read_text().strip() if args.qa_only else legal(args.terms_text.read_text(),'Terms')
 for path in [root/'backend/pom.xml',root/'web/package-lock.json']:
  if not path.is_file():raise SystemExit('Missing source file: '+str(path))
 out.mkdir(parents=True,exist_ok=True);target=out/(('giftwise-qa-' if args.qa_only else 'giftwise-')+rid+'.tar.gz')
 if target.exists():raise SystemExit('Release already exists; use a new release ID. No file was replaced.')
 run(['docker','info','--format','{{.ServerVersion}}'])
 backend=root/'backend'
 # Tests are an explicit prior release gate, not silently reported as passed here.
 run(['./mvnw','-B','-Pproduction-release','-Dmaven.test.skip=true','package'],backend)
 jar=backend/'target/production-release/backend-0.0.1-SNAPSHOT.jar'
 run(['python3',str(backend/'ops/check-release-artifact.py'),str(jar)],backend)
 with tempfile.TemporaryDirectory(prefix='giftwise-release-') as tmp:
  tmp=Path(tmp);context=tmp/'web';context.mkdir();snapshot=hashlib.sha256()
  for folder in ['app','components','lib','public']:
   for source in sorted((root/'web'/folder).rglob('*')):
    if source.is_symlink():raise SystemExit('Refusing source symlink: '+str(source))
    if source.is_file() and valid_asset(source):
     rel=source.relative_to(root/'web');dest=context/rel;dest.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(source,dest)
     snapshot.update(str(rel).encode()+b'\0'+bytes.fromhex(sha(source)))
  for name in ['package.json','package-lock.json','tsconfig.json','next-env.d.ts','middleware.ts']:
   shutil.copy2(root/'web'/name,context/name)
  (context/'next.config.js').write_text("module.exports={output:'standalone',async rewrites(){return [{source:'/backend/:path*',destination:'http://127.0.0.1:8080/:path*'}]}};\n")
  # Override only the temporary deployment copy. The working source remains unchanged.
  for route,title,text in [('privacy','Privacy Policy',privacy),('terms','Terms and Conditions',terms)]:
   paragraphs=json.dumps(text.split('\n\n'),ensure_ascii=False)
   (context/'app'/route/'page.tsx').write_text("const paragraphs="+paragraphs+";\nexport default function Legal(){return <main style={{maxWidth:800,margin:'3rem auto',padding:24,lineHeight:1.7}}><h1>"+title+"</h1><p>Version 2026-11-05 · Effective "+args.legal_effective_date+"</p>{paragraphs.map((p,i)=><p key={i} style={{whiteSpace:'pre-wrap'}}>{p}</p>)}</main>}\n")
  shutil.copy2(Path(__file__).with_name('frontend-build.Dockerfile'),context/'Dockerfile')
  stage=tmp/'bundle';stage.mkdir()
  run(['docker','buildx','build','--platform','linux/amd64','--target','export','--output','type=local,dest='+str(stage),str(context)])
  if not (stage/'frontend/server.js').is_file():raise SystemExit('Standalone server.js missing; release was not produced.')
  (stage/'backend').mkdir();shutil.copy2(jar,stage/'backend/backend.jar')
  # The runtime does not consume project documentation. Preserve dependency licenses.
  with zipfile.ZipFile(stage/'backend/backend.jar','r') as original:
   entries=[(item,original.read(item.filename)) for item in original.infolist() if item.filename!='BOOT-INF/classes/email-templates/README.md']
  with zipfile.ZipFile(stage/'backend/backend.jar','w') as clean:
   for item,data in entries:clean.writestr(item,data)
  run(['python3',str(backend/'ops/check-release-artifact.py'),str(stage/'backend/backend.jar')])
  git=subprocess.run(['git','rev-parse','HEAD'],cwd=root,capture_output=True,text=True)
  metadata={'releaseId':rid,'releasePurpose':'QA_ONLY' if args.qa_only else 'PRODUCTION_CANDIDATE','builtAtUtc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'platform':'linux/amd64','java':21,'nodeMajor':24,'legalVersion':'2026-11-05','legalEffectiveDate':args.legal_effective_date,'privacySha256':hashlib.sha256(privacy.encode()).hexdigest(),'termsSha256':hashlib.sha256(terms.encode()).hexdigest(),'frontendSourceFingerprint':snapshot.hexdigest(),'sourceCommit':git.stdout.strip() if git.returncode==0 else None,'backendOriginalSha256':sha(jar),'tests':'Run release-check.sh separately and retain its successful report.'}
  (stage/'release.json').write_text(json.dumps(metadata,indent=2)+'\n')
  paths=sorted(p for p in stage.rglob('*') if p.is_file())
  for p in paths:
   rel=p.relative_to(stage)
   if '.git' in rel.parts or p.name.startswith('.env') or any(x in p.name for x in ['.backup','.bak']):raise SystemExit('Unexpected runtime file: '+str(rel))
  (stage/'SHA256SUMS').write_text(''.join(sha(p)+'  '+p.relative_to(stage).as_posix()+'\n' for p in paths))
  with tarfile.open(target,'w:gz') as archive:
   for p in sorted(stage.rglob('*')):
    if p.is_symlink():raise SystemExit('Unexpected runtime symlink: '+str(p))
    archive.add(p,arcname=p.relative_to(stage),recursive=False)
  digest=sha(target);target.with_suffix(target.suffix+'.sha256').write_text(digest+'  '+target.name+'\n')
  print('Release created:',target);print('Release SHA256:',digest)
if __name__=='__main__':main()
