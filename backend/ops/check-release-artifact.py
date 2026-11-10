#!/usr/bin/env python3
"""Check the executable release's configuration, not a substitute for secret scanning/SBOM."""
import pathlib,sys,zipfile
artifact=pathlib.Path(sys.argv[1])
try:
 with zipfile.ZipFile(artifact) as archive:
  manifest=archive.read('META-INF/MANIFEST.MF').decode()
  if 'Main-Class: org.springframework.boot.loader.launch.JarLauncher' not in manifest or 'Start-Class: com.giftplatform.BackendApplication' not in manifest:raise ValueError('Executable Boot launcher/main class missing')
  if 'BOOT-INF/classes/com/giftplatform/BackendApplication.class' not in archive.namelist():raise ValueError('Application class missing')
  name='BOOT-INF/classes/application.properties'
  content=archive.read(name).decode()
  if 'gift.build.configuration=environment-only-release' not in content:raise ValueError('Release config marker missing')
  if 'spring.profiles.active=${SPRING_PROFILES_ACTIVE:production}' not in content:raise ValueError('Production profile default missing')
  if any(n.startswith('BOOT-INF/classes/') and ('application.properties.example' in n or 'application-sms-example.yml' in n) for n in archive.namelist()):raise ValueError('Local/example configuration bundled')
  import re
  for name in archive.namelist():
   if not(name.startswith('BOOT-INF/classes/') and name.endswith('.properties')):continue
   for line in archive.read(name).decode().splitlines():
    if line.lstrip().startswith('#') or '=' not in line:continue
    key,value=line.split('=',1);value=value.strip()
    if re.search(r'password|secret|api.?key|encryption.?key|fingerprint.?key|hmac.?key|auth.?key',key,re.I) and value and not value.startswith('${'):raise ValueError('Literal sensitive configuration in '+name+': '+key)
 print('Executable release configuration checks passed; no provider/deployment readiness is asserted')
except (OSError,ValueError,KeyError,zipfile.BadZipFile) as error:print(str(error),file=sys.stderr);sys.exit(1)
