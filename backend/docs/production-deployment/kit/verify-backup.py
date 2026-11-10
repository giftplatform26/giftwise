#!/usr/bin/env python3
"""Hashes detect corruption, not malicious modification; protect/encrypt backups separately."""
import argparse,hashlib,json,pathlib,sys,datetime,tarfile
parser=argparse.ArgumentParser();parser.add_argument('--create',action='store_true');parser.add_argument('directory',type=pathlib.Path);args=parser.parse_args();root=args.directory
files=['database.dump','media.tar']
def digest(path):
 h=hashlib.sha256()
 with path.open('rb') as source:
  for block in iter(lambda:source.read(1024*1024),b''):h.update(block)
 return h.hexdigest()
try:
 for name in files:
  p=root/name
  if p.is_symlink() or not p.is_file() or p.stat().st_size==0:raise ValueError('Missing or invalid '+name)
 with (root/'database.dump').open('rb') as dump:
  if dump.read(5)!=b'PGDMP':raise ValueError('Not a PostgreSQL custom-format dump')
 with tarfile.open(root/'media.tar') as archive:
  for item in archive:
   path=pathlib.PurePosixPath(item.name)
   if path.is_absolute() or '..' in path.parts or not(item.isfile() or item.isdir()):raise ValueError('Unsafe media archive')
 hashes={name:digest(root/name) for name in files}
 if args.create:
  with (root/'manifest.json').open('x') as out:json.dump({'format':1,'createdUtc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'sha256':hashes,'writesStopped':True},out,indent=2)
  with (root/'COMPLETE').open('x') as out:out.write('1\n')
 else:
  if (root/'COMPLETE').read_text()!='1\n':raise ValueError('Incomplete backup')
  manifest=json.loads((root/'manifest.json').read_text())
  if manifest.get('format')!=1 or manifest.get('writesStopped') is not True or manifest.get('sha256')!=hashes:raise ValueError('Manifest or checksum mismatch')
 print('Backup integrity verified' if not args.create else 'Manifest recorded')
except (OSError,ValueError,tarfile.TarError) as error:print(str(error),file=sys.stderr);sys.exit(1)
