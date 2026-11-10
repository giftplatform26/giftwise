#!/usr/bin/env python3
"""Open this release in nonpayment mode after the runbook acceptance checks."""
import argparse,json,os,re,subprocess,urllib.request
from pathlib import Path
from server_configure import env_read

def main():
 ap=argparse.ArgumentParser(description=__doc__);ap.add_argument('--confirm',required=True);args=ap.parse_args()
 if os.geteuid()!=0 or args.confirm!='OPEN_NONPAYMENT_SITE':raise SystemExit('Run as root with --confirm OPEN_NONPAYMENT_SITE after completing the acceptance checklist.')
 env=env_read('/etc/giftwise/backend.env');metadata=json.loads(Path('/srv/giftwise/current/release.json').read_text())
 if metadata.get('releasePurpose')!='PRODUCTION_CANDIDATE':raise SystemExit('A QA artifact cannot be published.')
 for endpoint in ['http://127.0.0.1:8080/actuator/health/readiness','http://127.0.0.1:3000/login']:
  with urllib.request.urlopen(endpoint,timeout=15) as response:
   if response.status!=200:raise SystemExit('Application readiness check failed.')
 pg={**os.environ,'PGHOST':'127.0.0.1','PGPORT':'5432','PGUSER':'giftwise_app','PGDATABASE':'giftwise_prod','PGPASSWORD':env['SPRING_DATASOURCE_PASSWORD']}
 def scalar(sql):return subprocess.run(['psql','-XAt','-v','ON_ERROR_STOP=1','-c',sql],env=pg,capture_output=True,text=True,check=True).stdout.strip()
 if scalar("SELECT count(*) FROM payment_channel_controls WHERE scope='PLATFORM' AND enabled AND channel IN ('UPI','BANK','RAZORPAY','ROUTING')")!='0':raise SystemExit('Disable all platform payment channels in Admin Payment controls for this release. Live Razorpay is not implemented.')
 if not scalar("SELECT support_email FROM platform_branding WHERE id=1"):raise SystemExit('Set the real support email in Admin Branding and appearance.')
 if env.get('GIFT_OTP_EMAIL_PROVIDER')!='resend' or not env.get('GIFT_OTP_EMAIL_RESEND_API_KEY') or not env.get('GIFT_OTP_EMAIL_FROM'):raise SystemExit('Configure and test email delivery before public registration.')
 if scalar("SELECT count(*) FROM platform_verification_settings WHERE id=1 AND email_verification_enabled")!='1':raise SystemExit('Enable and test email verification in Admin Authentication and policies.')
 config=Path('/etc/caddy/Caddyfile');original=config.read_text()
 updated=re.sub(r'    # GIFTWISE_SETUP_ACCESS_START\n.*?    # GIFTWISE_SETUP_ACCESS_END\n','',original,flags=re.S)
 if updated==original:raise SystemExit('Setup marker not found. Review the Caddy configuration manually.')
 Path('/etc/giftwise/Caddyfile.before-public').write_text(original)
 try:
  config.write_text(updated);subprocess.run(['caddy','validate','--config',str(config)],check=True)
  subprocess.run(['systemctl','reload','caddy'],check=True)
 except Exception:config.write_text(original);raise
 print('Public access opened in nonpayment mode. Verify from a second network and monitor logs.')
if __name__=='__main__':main()
