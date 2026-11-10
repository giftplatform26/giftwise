#!/usr/bin/env python3
"""Verify and activate a runtime release without touching the database."""
import argparse,hashlib,json,os,pwd,re,shutil,subprocess,tarfile
from pathlib import Path,PurePosixPath

def sha(p):
 h=hashlib.sha256()
 with p.open('rb') as f:
  for b in iter(lambda:f.read(1024*1024),b''):h.update(b)
 return h.hexdigest()
def main():
 ap=argparse.ArgumentParser(description=__doc__);ap.add_argument('--archive',type=Path,required=True);ap.add_argument('--sha256',required=True);args=ap.parse_args()
 if os.geteuid()!=0:raise SystemExit('Run with sudo.')
 if not re.fullmatch('[a-f0-9]{64}',args.sha256) or sha(args.archive)!=args.sha256:raise SystemExit('Release SHA256 mismatch. Nothing was activated.')
 for service in ['giftwise-web','giftwise-backend']:
  if subprocess.run(['systemctl','is-active','--quiet',service]).returncode==0:raise SystemExit('Stop '+service+' before activation. Take a coordinated backup before upgrading an existing database.')
 base=Path('/srv/giftwise');releases=base/'releases';releases.mkdir(parents=True,exist_ok=True)
 with tarfile.open(args.archive,'r:gz') as tar:
  items=tar.getmembers();seen=set();total=0
  for item in items:
   name=PurePosixPath(item.name)
   if name.is_absolute() or '..' in name.parts or not name.parts or name.parts[0] not in ['backend','frontend','release.json','SHA256SUMS'] or not (item.isfile() or item.isdir()) or item.name in seen:raise SystemExit('Unexpected or unsafe archive member: '+item.name)
   seen.add(item.name);total+=item.size
  if total>2*1024**3:raise SystemExit('Unexpected archive size. Review before installing.')
  metadata=json.load(tar.extractfile('release.json'));rid=metadata.get('releaseId','')
  if not re.fullmatch(r'[A-Za-z0-9][A-Za-z0-9._-]{0,60}',rid) or metadata.get('platform')!='linux/amd64' or metadata.get('releasePurpose')!='PRODUCTION_CANDIDATE':raise SystemExit('Unsupported release identifier or platform.')
  existing=base/'current/release.json'
  if existing.is_file():
   previous_meta=json.loads(existing.read_text())
   if previous_meta.get('legalVersion')==metadata.get('legalVersion') and any(previous_meta.get(k)!=metadata.get(k) for k in ['privacySha256','termsSha256']):raise SystemExit('Legal text changed without a new consent version. Update the application legal version before this release.')
  target=releases/rid
  if target.exists():raise SystemExit('Release directory already exists. Nothing was replaced.')
  target.mkdir(mode=0o755);tar.extractall(target,filter='data')
 try:
  listed=set()
  for line in (target/'SHA256SUMS').read_text().splitlines():
   digest,relative=line.split('  ',1);path=target/relative
   if not path.resolve().is_relative_to(target.resolve()) or relative in listed or not re.fullmatch('[a-f0-9]{64}',digest) or sha(path)!=digest:raise ValueError('Invalid runtime checksum: '+relative)
   listed.add(relative)
  actual={p.relative_to(target).as_posix() for p in target.rglob('*') if p.is_file() and p.relative_to(target).as_posix()!='SHA256SUMS'}
  if actual!=listed:raise ValueError('Unlisted or missing runtime files.')
  if not (target/'backend/backend.jar').is_file() or not (target/'frontend/server.js').is_file():raise ValueError('Application runtime files missing.')
 except Exception:
  shutil.rmtree(target);raise
 for p in target.rglob('*'):
  os.chown(p,0,0);p.chmod(0o755 if p.is_dir() else 0o644)
 account=pwd.getpwnam('giftwise');cache=Path('/var/cache/giftwise')/rid;cache.mkdir(mode=0o750,parents=True,exist_ok=True);os.chown(cache,account.pw_uid,account.pw_gid)
 cache_link=target/'frontend/.next/cache'
 if cache_link.exists():shutil.rmtree(cache_link)
 cache_link.symlink_to(cache)
 current=base/'current'
 if current.exists():
  previous=current.resolve()
  if not previous.is_relative_to(releases.resolve()):raise SystemExit('Current release points outside the expected release directory.')
  prev=base/'previous'
  if prev.is_symlink():prev.unlink()
  prev.symlink_to(previous)
 next_link=base/'current.next'
 if next_link.is_symlink():next_link.unlink()
 next_link.symlink_to(target);os.replace(next_link,current)
 print('Activated filesystem release:',rid)
 print('Database unchanged. Start the backend, verify readiness, then start the frontend.')
if __name__=='__main__':main()
