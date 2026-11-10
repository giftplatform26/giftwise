"""Snapshot the authorized sibling web sources without copying secrets or build artifacts.

Run explicitly to refresh; never silently overwrite mobile branding changes.
"""
from pathlib import Path
import shutil, json, hashlib
root=Path(__file__).resolve().parents[1]
source=root.parents[1]/'web'
target=root/'web'
if target.exists():
    raise SystemExit('web/ already exists. Reconcile changes before importing again; refusing overwrite.')
target.mkdir()
manifest={}
for folder in ('app','components','lib','public','tests'):
    for p in (source/folder).rglob('*'):
        if not p.is_file() or p.name.startswith('.') or p.suffix in ('.log',): continue
        rel=p.relative_to(source); dest=target/rel; dest.parent.mkdir(parents=True,exist_ok=True)
        shutil.copyfile(p,dest)
        manifest[str(rel)]=hashlib.sha256(p.read_bytes()).hexdigest()
for name in ('package.json','package-lock.json','tsconfig.json','next-env.d.ts','next.config.ts'):
    shutil.copyfile(source/name,target/name)
(root/'brand/web-source-snapshot.json').write_text(json.dumps({'source':'gift-platform/web','files':manifest},indent=2)+'\n')
print(f'Copied {len(manifest)} web source/assets/tests into isolated mobile web package.')
