#!/usr/bin/env python3
"""First-server setup. Run as root only on a dedicated Debian 13 server."""
import argparse,base64,datetime,hashlib,ipaddress,os,pwd,re,secrets,shutil,subprocess
from pathlib import Path

def run(args,**kw):return subprocess.run(args,check=True,**kw)
def env_read(path):
 result={}
 for line in Path(path).read_text().splitlines():
  if line.strip() and not line.lstrip().startswith('#'):
   key,value=line.split('=',1);result[key]=value.strip().strip('"').strip("'")
 return result

def main():
 ap=argparse.ArgumentParser(description=__doc__);ap.add_argument('--domain',required=True);ap.add_argument('--tls-email',required=True);ap.add_argument('--setup-cidr',required=True);ap.add_argument('--resume',action='store_true');args=ap.parse_args()
 if os.geteuid()!=0:raise SystemExit('Run with sudo.')
 os_release=Path('/etc/os-release').read_text()
 if not re.search(r'^ID=debian$',os_release,re.M) or not re.search(r'^VERSION_ID="?13"?$',os_release,re.M):raise SystemExit('This runbook targets Debian 13. Do not run this installer on another OS.')
 setup_cidr=str(ipaddress.ip_network(args.setup_cidr,strict=False))
 if setup_cidr in ['0.0.0.0/0','::/0']:raise SystemExit('Use the operator address or approved narrow setup network.')
 domain=args.domain.lower()
 if not re.fullmatch(r'[a-z0-9](?:[a-z0-9.-]{0,251}[a-z0-9])?',domain) or '.' not in domain or domain.endswith(('.example','.invalid','.test','.localhost')) or domain=='example.com':raise SystemExit('Supply the real public hostname without https, port or path.')
 if not re.fullmatch(r'[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}',args.tls_email):raise SystemExit('Supply a valid TLS contact email.')
 for exe in ['java','node','psql','magick','caddy','systemctl']:
  if not shutil.which(exe):raise SystemExit('Missing '+exe+'; complete the runtime installation step.')
 version=run(['node','-p','process.versions.node'],capture_output=True,text=True).stdout.strip()
 if not version.startswith('24.'):raise SystemExit('Install Node 24 using the runbook; found '+version)
 if 'ImageMagick 7' not in run(['magick','-version'],capture_output=True,text=True).stdout:raise SystemExit('ImageMagick 7 required.')
 if not re.search(r'version "21[.\"]',run(['java','-version'],capture_output=True,text=True).stderr):raise SystemExit('Java 21 required.')
 try:account=pwd.getpwnam('giftwise')
 except KeyError:
  run(['useradd','--system','--user-group','--home-dir','/nonexistent','--shell','/usr/sbin/nologin','giftwise']);account=pwd.getpwnam('giftwise')
 for path,mode,owner in [('/etc/giftwise',0o700,None),('/srv/giftwise',0o755,None),('/srv/giftwise/releases',0o755,None),('/srv/giftwise/incoming',0o700,None),('/var/lib/giftwise/media',0o700,account),('/var/cache/giftwise',0o750,account),('/var/backups/giftwise',0o700,None),('/var/log/giftwise',0o700,None)]:
  p=Path(path);p.mkdir(parents=True,exist_ok=True);p.chmod(mode)
  if owner:os.chown(p,owner.pw_uid,owner.pw_gid)
 psql=['runuser','-u','postgres','--','psql','-X','-v','ON_ERROR_STOP=1','-d','postgres']
 def scalar(sql):return run(psql+['-At','-c',sql],capture_output=True,text=True).stdout.strip()
 has_db=scalar("SELECT count(*) FROM pg_database WHERE datname='giftwise_prod'")=='1'
 has_role=scalar("SELECT count(*) FROM pg_roles WHERE rolname='giftwise_app'")=='1'
 if (has_db or has_role) and not args.resume:raise SystemExit('Database or role already exists. Use --resume only for an interrupted first setup. Nothing was replaced.')
 file=Path('/etc/giftwise/backend.env')
 if file.exists():
  if not args.resume:raise SystemExit('Configuration already exists. Use --resume only to complete an interrupted first setup; existing secrets are retained.')
  env=env_read(file)
  if env.get('GIFT_PRODUCTION_PUBLIC_ORIGIN')!='https://'+domain:raise SystemExit('Domain differs from the saved configuration; edit it explicitly after reviewing the change.')
 else:
  if args.resume:raise SystemExit('No saved configuration to resume.')
  b64=lambda:base64.b64encode(secrets.token_bytes(32)).decode()
  env={'SPRING_PROFILES_ACTIVE':'production','SPRING_DATASOURCE_URL':'jdbc:postgresql://127.0.0.1:5432/giftwise_prod','SPRING_DATASOURCE_USERNAME':'giftwise_app','SPRING_DATASOURCE_PASSWORD':secrets.token_hex(32),'SERVER_ADDRESS':'127.0.0.1','SERVER_PORT':'8080','SERVER_FORWARD_HEADERS_STRATEGY':'framework','GIFT_PRODUCTION_PUBLIC_ORIGIN':'https://'+domain,'GIFT_PUBLIC_BASE_URL':'https://'+domain,'GIFT_PRODUCTION_INSTANCE_COUNT':'1','GIFT_GUESTBOOK_PHOTOS_DIR':'/var/lib/giftwise/media','GIFT_OTP_SECRET_BASE64':b64(),'GIFT_OTP_HMAC_KEY_BASE64':b64(),'GIFT_PAYMENT_ENCRYPTION_KEY_BASE64':b64(),'GIFT_PAYMENT_FINGERPRINT_KEY_BASE64':b64(),'GIFT_OTP_EMAIL_PROVIDER':'disabled','GIFT_OTP_EMAIL_RESEND_API_KEY':'','GIFT_OTP_EMAIL_FROM':'','GIFT_OTP_SMS_PROVIDER':'disabled','GIFT_INVITATION_AI_ENABLED':'false','GIFT_DIRECT_TRANSFERS_ENABLED':'false'}
  file.write_text(''.join(k+'='+v+'\n' for k,v in env.items()));file.chmod(0o600)
 if has_db:
  if not args.resume:raise SystemExit('giftwise_prod already exists. No database was replaced.')
  check_env={**os.environ,'PGHOST':'127.0.0.1','PGPORT':'5432','PGDATABASE':'giftwise_prod','PGUSER':'giftwise_app','PGPASSWORD':env['SPRING_DATASOURCE_PASSWORD']}
  run(['psql','-XAt','-v','ON_ERROR_STOP=1','-c','SELECT 1'],env=check_env,capture_output=True,text=True)
  print('Existing database retained and saved credentials verified. No schemas or data were reset.')
 else:
  if has_role and not args.resume:raise SystemExit('giftwise_app already exists. Review the interrupted setup; no existing role was altered.')
  if not has_role:
   password=env['SPRING_DATASOURCE_PASSWORD']
   if not re.fullmatch(r'[a-f0-9]{64}',password):raise SystemExit('Unexpected saved database password format.')
   run(psql,input="CREATE ROLE giftwise_app LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE PASSWORD '"+password+"';\n",text=True)
  run(psql,input='CREATE DATABASE giftwise_prod OWNER giftwise_app;\n',text=True)
 def write(path,text):p=Path(path);p.write_text(text);p.chmod(0o644)
 web_env=Path('/etc/giftwise/web.env')
 if not web_env.exists():
  web_env.write_text('GIFTWISE_PUBLIC_ORIGIN=https://'+domain+'\nGIFTWISE_BACKEND_ORIGIN=http://127.0.0.1:8080\n');web_env.chmod(0o600)
 elif env_read(web_env).get('GIFTWISE_PUBLIC_ORIGIN')!='https://'+domain:
  raise SystemExit('Web origin differs from the requested domain. Review web.env explicitly.')
 here=Path(__file__).resolve().parent
 for name in ['giftwise-backend.service','giftwise-web.service']:shutil.copy2(here/name,Path('/etc/systemd/system')/name)
 write('/etc/caddy/Caddyfile','{\n    email '+args.tls_email+'\n}\n'+domain+' {\n    encode zstd gzip\n    header -Server\n    # GIFTWISE_SETUP_ACCESS_START\n    @setupBlocked not remote_ip '+setup_cidr+' 127.0.0.1 ::1\n    respond @setupBlocked "GiftWise setup in progress" 503\n    # GIFTWISE_SETUP_ACCESS_END\n    reverse_proxy 127.0.0.1:3000\n}\n')
 run(['caddy','validate','--config','/etc/caddy/Caddyfile']);run(['systemctl','daemon-reload'])
 print('Configuration saved. No application services were started.')
 print('Database: giftwise_prod; role: giftwise_app; origin: https://'+domain)
 print('Secrets remain in /etc/giftwise/backend.env. Back them up securely; never regenerate them for an upgrade.')
if __name__=='__main__':main()
