#!/usr/bin/env python3
import argparse,hashlib,os,subprocess,tarfile
from pathlib import Path,PurePosixPath
from server_configure import env_read

def digest(stream):
 h=hashlib.sha256()
 for data in iter(lambda:stream.read(1024*1024),b''):h.update(data)
 return h.hexdigest()
def main():
 ap=argparse.ArgumentParser(description='Restore only into an existing empty *_restore database and a new private directory.')
 ap.add_argument('--backup-dir',type=Path,required=True);ap.add_argument('--restore-db',required=True);ap.add_argument('--media-dir',type=Path,required=True);ap.add_argument('--confirm',required=True);args=ap.parse_args()
 if os.geteuid()!=0 or args.confirm!='RESTORE_DISPOSABLE_DATABASE':raise SystemExit('Root and explicit disposable-restore confirmation required.')
 values=env_read('/etc/giftwise/backend.env');here=Path(__file__).resolve().parent
 env={**os.environ,'PGHOST':'127.0.0.1','PGPORT':'5432','PGUSER':'giftwise_app','PGDATABASE':args.restore_db,'PGPASSWORD':values['SPRING_DATASOURCE_PASSWORD'],'GIFT_BACKUP_PATH':str(args.backup_dir.resolve()),'GIFT_RESTORE_MEDIA_DIR':str(args.media_dir.resolve()),'GIFT_RESTORE_CONFIRM':args.confirm}
 subprocess.run(['bash',str(here/'restore-drill.sh')],env=env,check=True)
 with tarfile.open(args.backup_dir/'media.tar') as archive:
  for item in archive.getmembers():
   if item.isfile():
    relative=PurePosixPath(item.name);destination=args.media_dir/relative
    with archive.extractfile(item) as original,destination.open('rb') as restored:
     if digest(original)!=digest(restored):raise SystemExit('Restored photo mismatch: '+item.name)
 subprocess.run(['psql','-X','-v','ON_ERROR_STOP=1','-c',"SELECT (SELECT count(*) FROM users) AS users,(SELECT count(*) FROM platform_branding) AS branding,(SELECT count(*) FROM platform_languages) AS languages;"],env=env,check=True)
 print('Disposable database restore and all restored media file hashes verified. Production database was not changed.')
if __name__=='__main__':main()
