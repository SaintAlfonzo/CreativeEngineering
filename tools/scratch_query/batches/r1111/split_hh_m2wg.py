#!/usr/bin/env python3
"""r1111: splits hh_m2wg_raw.qb (SQ-HH-274 to -329, from hh_queue_to_qb.py) into per-boot batches and applies the
batch fixes: tag and feature namespaces moved 11-09 (wild_flowers -> creativeworldgen, crop_seasons ->
creativecalendar, land/plains read in both), SQ-HH-308 expanded from prose.
  hh_wga.qb   group A rows on Machines boot 1 (same server as SQ-HH-201)
  hh_m2.qb    boot 2 (SQ-HH-274 toml edits): 274 to 277, 318, 319
  hh_b3.qb    boot 3 (saltCaves, cornMaze false): 320
  hh_wgb.qb   fresh group B: 304, 307, 314, 328
  hh_wgc.qb   fresh group B with salt toml: 321, 322"""
import re, pathlib
D = pathlib.Path(__file__).parent
txt = (D / 'hh_m2wg_raw.qb').read_text()
txt = txt.replace('creativeengineering:wild_flowers/', 'creativeworldgen:wild_flowers/')
txt = txt.replace('creativeengineering:crop_seasons/', 'creativecalendar:crop_seasons/')
rows, cur, order = {}, None, []
for l in txt.split('\n'):
    m = re.match(r'## SQ-HH-(\d+) ', l)
    if m: cur = int(m.group(1)); order.append(cur); rows[cur] = [l]; continue
    if cur: rows[cur].append(l)
cols = ['white','yellow','orange','red','pink','purple','blue','light_blue']
c = []
for n, col in enumerate(cols):
    x = 4482 + 2*n
    c += [f'> setblock {x} -60 32 minecraft:flower_pot', f'> pbot hold hb1 hearthandharvest:{col}_mum',
          f'> pbot use hb1 {x} -60 32 up', f'> execute if block {x} -60 32 hearthandharvest:potted_{col}_mum']
rows[308] = [rows[308][0]] + c + [l for l in rows[308] if l.startswith('look')] + ['']
r = rows[327]; i = next(k for k, l in enumerate(r) if 'land/plains' in l)
r.insert(i+1, r[i].replace('creativeengineering:land/plains', 'creativeworldgen:land/plains'))
groups = {'hh_wga': [301,302,303,305,306,308,309,310,311,312,313,315,316,317,323,324,325,326,327,329],
          'hh_m2': [274,275,276,277,318,319], 'hh_b3': [320], 'hh_wgb': [304,307,314,328], 'hh_wgc': [321,322]}
for name, ids in groups.items():
    (D / f'{name}.qb').write_text('pace 1\n\n' + '\n'.join(l for i in ids for l in rows[i]) + '\n')
    print(name, len(ids))
