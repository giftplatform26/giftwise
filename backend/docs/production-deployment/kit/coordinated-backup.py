#!/usr/bin/env python3
"""Run the existing coordinated backup while services are stopped. No deletion or restore."""
import os,subprocess
from pathlib import Path
from server_configure import env_read

def main():
 if os.geteuid()!=0:raise SystemExit('Run with sudo.')
 for service in ['giftwise-web','giftwise-backend']:
  if subprocess.run(['systemctl','is-active','--quiet',service]).returncode==0:raise SystemExit('Stop '+service+' before taking a coordinated database and media backup.')
 values=env_read('/etc/giftwise/backend.env');here=Path(__file__).resolve().parent
 env={**os.environ,'PGHOST':'127.0.0.1','PGPORT':'5432','PGUSER':'giftwise_app','PGDATABASE':'giftwise_prod','PGPASSWORD':values['SPRING_DATASOURCE_PASSWORD'],'GIFT_WRITES_STOPPED':'YES','GIFT_MEDIA_DIR':'/var/lib/giftwise/media','GIFT_BACKUP_DIR':'/var/backups/giftwise'}
 subprocess.run(['bash',str(here/'backup.sh')],env=env,check=True)
if __name__=='__main__':main()
