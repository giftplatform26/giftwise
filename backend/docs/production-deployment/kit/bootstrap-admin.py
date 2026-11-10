#!/usr/bin/env python3
"""Create the first admin through the application's provisioning runner, not direct SQL."""
import argparse,getpass,os,pwd,re,subprocess,time
from pathlib import Path
from server_configure import env_read

def main():
 ap=argparse.ArgumentParser(description=__doc__);ap.add_argument('--email',required=True);args=ap.parse_args()
 if os.geteuid()!=0:raise SystemExit('Run with sudo.')
 if not re.fullmatch(r'[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}',args.email):raise SystemExit('Supply a valid administrator email.')
 if subprocess.run(['systemctl','is-active','--quiet','giftwise-backend']).returncode==0:raise SystemExit('Stop giftwise-backend before provisioning.')
 values=env_read('/etc/giftwise/backend.env');pg={'PATH':os.environ.get('PATH','/usr/bin:/bin'),'PGHOST':'127.0.0.1','PGPORT':'5432','PGDATABASE':'giftwise_prod','PGUSER':'giftwise_app','PGPASSWORD':values['SPRING_DATASOURCE_PASSWORD']}
 query="SELECT count(*) FROM users WHERE role='PLATFORM_ADMIN'"
 def count():return int(subprocess.run(['psql','-XAt','-v','ON_ERROR_STOP=1','-c',query],env=pg,check=True,capture_output=True,text=True).stdout.strip())
 if count()!=0:raise SystemExit('A platform administrator already exists. No second administrator was created. Use account recovery for an existing admin.')
 password=getpass.getpass('New administrator password (16+ characters): ')
 if password!=getpass.getpass('Confirm password: '):raise SystemExit('Passwords do not match.')
 if len(password)<16 or len(password.encode())>72 or not all(re.search(p,password) for p in ['[A-Z]','[a-z]','[0-9]',r'[!-/:-@\[-`{-~]']):raise SystemExit('Require 16+ characters, uppercase, lowercase, number, punctuation, at most 72 UTF-8 bytes.')
 env={'PATH':'/usr/bin:/bin','LANG':'C.UTF-8',**values,'SPRING_PROFILES_ACTIVE':'admin-provision','ADMIN_PROVISION_CONFIRM':'YES','ADMIN_PROVISION_EMAIL':args.email,'ADMIN_PROVISION_PASSWORD':password}
 account=pwd.getpwnam('giftwise')
 def restrict():os.setgid(account.pw_gid);os.setuid(account.pw_uid)
 log=Path('/var/log/giftwise/bootstrap.log')
 with log.open('ab') as output:
  process=subprocess.Popen(['/usr/bin/java','-Djava.awt.headless=true','-jar','/srv/giftwise/current/backend/backend.jar','--spring.profiles.active=admin-provision','--server.address=127.0.0.1','--server.port=18080','--gift.otp.email.provider=disabled','--gift.otp.sms.provider=disabled'],env=env,stdout=output,stderr=subprocess.STDOUT,preexec_fn=restrict)
  try:
   for _ in range(120):
    time.sleep(2)
    if count()==1:
     print('First administrator created. Sign in using the administrator email and the password you entered.');return
    if process.poll() is not None:raise SystemExit('Provisioning stopped. Read /var/log/giftwise/bootstrap.log; no password is printed by this helper.')
   raise SystemExit('Provisioning timed out. Inspect the bootstrap log and database before retrying.')
  finally:
   if process.poll() is None:
    process.terminate()
    try:process.wait(timeout=60)
    except subprocess.TimeoutExpired:process.kill();process.wait()
if __name__=='__main__':main()
