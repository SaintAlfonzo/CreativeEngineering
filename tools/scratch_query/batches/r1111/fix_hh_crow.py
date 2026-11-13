#!/usr/bin/env python3
"""r1111: hh_crow_raw.qb -> hh_crow_d.qb (Boot D: 401 to 463, 465), hh_crow_b.qb (464, group B world),
hh_crow_f.qb (Boot F, 466 to 502; toml edits done by the runner before that boot). Drops bare directive words the
generator took from prose (a 'sleep' or 'shell' line with no argument)."""
import re, pathlib
D = pathlib.Path(__file__).parent
rows, cur, order = {}, None, []
for l in (D / 'hh_crow_raw.qb').read_text().split('\n'):
    if l.strip() in ('sleep', 'waitlog', 'expect', 'reject', 'count', 'capture', 'shell', 'look', 'pace'): continue
    m = re.match(r'## SQ-HH-(\d+) ', l)
    if m: cur = int(m.group(1)); order.append(cur); rows[cur] = [l]; continue
    if cur: rows[cur].append(l)
groups = {'hh_crow_d': [i for i in order if i <= 465 and i != 464], 'hh_crow_b': [464], 'hh_crow_f': [i for i in order if i >= 466]}
for name, ids in groups.items():
    (D / f'{name}.qb').write_text('pace 1\n\n' + '\n'.join(l for i in ids for l in rows[i]) + '\n'); print(name, len(ids))
