#!/usr/bin/env bash
# Local release gate. No deployment and no production/provider operations.
set -euo pipefail
backend_dir=$(cd "$(dirname "$0")/.." && pwd)
web_dir=${GIFT_WEB_DIR:-"$backend_dir/../web"}
cd "$backend_dir"
python3 ops/check-source-shadowing.py
python3 ops/test_backup_integrity.py
./mvnw --batch-mode clean verify
(cd "$web_dir" && npm test && ./node_modules/.bin/tsc --noEmit --incremental false && npm run build)
python3 docs/application/tooling/validate_package.py
./mvnw --batch-mode -Pproduction-release -Dmaven.test.skip=true package
python3 ops/check-release-artifact.py target/production-release/backend-0.0.1-SNAPSHOT.jar
printf '%s\n' 'Automated checks passed. Browser UAT, restore drill, security review and provider approval remain separate release gates.'
