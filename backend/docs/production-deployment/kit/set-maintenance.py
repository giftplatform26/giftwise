#!/usr/bin/env python3
import argparse,ipaddress,os,subprocess
from pathlib import Path

def main():
 ap=argparse.ArgumentParser(description='Restrict the site to the operator before a planned change.');ap.add_argument('--setup-cidr',required=True);args=ap.parse_args()
 if os.geteuid()!=0:raise SystemExit('Run with sudo.')
 cidr=str(ipaddress.ip_network(args.setup_cidr,strict=False))
 if cidr in ['0.0.0.0/0','::/0']:raise SystemExit('Use a narrow operator network.')
 path=Path('/etc/caddy/Caddyfile');old=path.read_text()
 if 'GIFTWISE_SETUP_ACCESS_START' in old:raise SystemExit('Setup access restriction already exists. Review the current operator CIDR before changing it.')
 needle='    reverse_proxy 127.0.0.1:3000'
 if old.count(needle)!=1:raise SystemExit('Unexpected Caddy configuration. Review manually.')
 guard='    # GIFTWISE_SETUP_ACCESS_START\n    @setupBlocked not remote_ip '+cidr+' 127.0.0.1 ::1\n    respond @setupBlocked "GiftWise maintenance in progress" 503\n    # GIFTWISE_SETUP_ACCESS_END\n'
 Path('/etc/giftwise/Caddyfile.before-maintenance').write_text(old)
 try:
  path.write_text(old.replace(needle,guard+needle));subprocess.run(['caddy','validate','--config',str(path)],check=True);subprocess.run(['systemctl','reload','caddy'],check=True)
 except Exception:path.write_text(old);raise
 print('Site restricted to the operator. Check that the operator can still sign in before stopping services.')
if __name__=='__main__':main()
