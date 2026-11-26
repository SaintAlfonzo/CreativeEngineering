#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""SQ-CMP-01 check (2026-11-22; per target since 2026-11-24): Compat's common mixins on a server, without a hard-coded count.

Run in a scratch server folder (srv/): cmp_mixin_check.py [REPO]. Reads the "mixins" list of the creativecompat jar in
mods/, the "Mixing X from creativecompat.mixins.json" lines of logs/debug.log and the gate tables of
CompatMixinPlugin.java in REPO (default: this script's repo). Exit 0 when
  - the apply lines name no mixin twice for the same target class (one line per target is normal) and none that Creative Engineering also applies (no double apply),
  - every applied mixin is listed in the jar, and
  - every listed mixin that was NOT applied has a gate entry in the plugin (skipped because its mod is absent or out
    of range) instead of failing to apply.
Prints listed / applied / skipped counts and the skipped names."""
import json
import re
import sys
import zipfile
from pathlib import Path

repo = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).resolve().parents[2]
jars = sorted(Path('mods').glob('creativecompat-*.jar'))
if not jars:
	print('FAIL: no creativecompat jar in mods/')
	sys.exit(1)
with zipfile.ZipFile(jars[-1]) as z:
	listed = [m.split('.')[-1] for m in json.loads(z.read('creativecompat.mixins.json'))['mixins']]
log = Path('logs/debug.log').read_text(errors='replace')
# one apply line per (mixin, target class): a mixin with several targets (LowGravityPullMixin) logs one line per target
mix = re.findall(r'Mixing (\S+) from (creativecompat|creativeengineering)\.mixins\.json(?: into (\S+))?', log)
cmp_pairs = [(n.split('.')[-1], t) for n, j, t in mix if j == 'creativecompat']
ce = {n.split('.')[-1] for n, j, t in mix if j == 'creativeengineering'}
applied = {n for n, _ in cmp_pairs}
gates = set(re.findall(r'Map\.entry\("(\w+)"', (repo / 'development/creativeCompat/src/main/java/com/SaintAlfonzo/'
													   'creativecompat/compat/CompatMixinPlugin.java').read_text()))
errs = []
dup = sorted({'%s -> %s' % p for p in cmp_pairs if cmp_pairs.count(p) > 1})
if dup:
	errs.append('applied twice to the same target: %s' % dup)
if applied & ce:
	errs.append('also applied from creativeengineering.mixins.json: %s' % sorted(applied & ce))
if applied - set(listed):
	errs.append('applied but not listed in the jar: %s' % sorted(applied - set(listed)))
skipped = sorted(set(listed) - applied)
ungated = [n for n in skipped if n not in gates]
if ungated:
	errs.append('listed, not applied, no gate entry in CompatMixinPlugin: %s' % ungated)
print('listed %d, applied %d, skipped by gate %d' % (len(listed), len(applied), len(skipped) - len(ungated)))
print('skipped: %s' % ', '.join(skipped))
for e in errs:
	print('FAIL: ' + e)
sys.exit(1 if errs else 0)
