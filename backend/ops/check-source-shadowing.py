#!/usr/bin/env python3
"""Tests must exercise real production classes, using explicit test bean overrides instead."""
from pathlib import Path
import sys
root=Path(__file__).resolve().parent.parent
production=root/'src/main/java';tests=root/'src/test/java'
collisions=sorted(str(p.relative_to(tests)) for p in tests.rglob('*.java') if (production/p.relative_to(tests)).is_file())
if collisions:print('Test sources shadow production classes:\n'+'\n'.join(collisions),file=sys.stderr);sys.exit(1)
print('No production classes shadowed by test sources')
