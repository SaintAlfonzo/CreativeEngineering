#!/usr/bin/env python3
"""r1111: writes hh_mach.qb from r1108hh/mach_a.qb with the batch fixes of the r1108hh resume point
(scratch_run_2026-11-08.md 8): bot starter kit cleared, 16 cows, tp for -231, crate for -255, fixtures for
-264 and -268 to -273 written out from the queue prose."""
import re, pathlib
D = pathlib.Path(__file__).parent
src = (D.parent / 'r1108hh/mach_a.qb').read_text().split('\n')
rows, cur = {}, None
order, head = [], []
for l in src:
    m = re.match(r'## (SQ-HH-\d+) ', l)
    if m: cur = m.group(1); order.append(cur); rows[cur] = [l]; continue
    (rows[cur] if cur else head).append(l)
def form4(x, z):
    out = ['> pbot hold hb1 hearthandharvest:stomping_basin 4']
    for dx, dz in ((0,0),(1,0),(0,1),(1,1)): out.append(f'> pbot use hb1 {x+dx} -61 {z+dz} up')
    return out
def body(rid, new):  # keep title line and look/expect lines, replace commands
    keep = [l for l in rows[rid][1:] if l.startswith(('look', 'expect', 'reject', 'count', 'kjs')) and 'UNPARSED' not in l]
    rows[rid] = [rows[rid][0]] + new + keep + ['']
r = rows['SQ-HH-201']; i = next(k for k, l in enumerate(r) if l.startswith('> pbot spawn'))
r[i+1:i+1] = ['sleep 3', '> clear hb1']
body('SQ-HH-226', ['> setblock 176 -60 80 hearthandharvest:trough', 'repeat 8', '> summon minecraft:cow 178 -60 80 {NoAI:1b}',
    '> summon minecraft:cow 174 -60 80 {NoAI:1b}', 'end', '> pbot hold hb1 minecraft:wheat 16', '> pbot use hb1 176 -60 80 up',
    'sleep 9', '> execute if entity @e[type=minecraft:cow,x=176,y=-60,z=80,distance=..16,nbt=!{InLove:0}]',
    '> data get block 176 -60 80 Items'])
r = rows['SQ-HH-231']; r.insert(1, '> tp hb1 17.5 -59 113.5')
r = rows['SQ-HH-255']; r.insert(1, '> setblock 144 -60 176 hearthandharvest:crate[type=bottom,facing=north]')
ins = ['fruitsdelight:blueberry 4','hearthandharvest:raspberry 4','minecraft:sweet_berries 4','vinery:cherry 4',
       'minecraft:glow_berries 4','minecraft:melon_slice 4','corn_delight:corn 4','vintagedelight:peanut 4','minecraft:wheat_seeds 8']
c = []
for n, it in enumerate(ins):
    x = 114 + 2*n  # every other block, so basins do not group
    c += [f'> setblock {x} -60 208 hearthandharvest:stomping_basin[facing=north]', f'> zhh2 insert {x} -60 208 {it}']
c.append('> summon minecraft:villager 112.5 -59.9 210.5 {NoAI:1b,Tags:["st264"]}')
for n in range(len(ins)):
    x = 114 + 2*n
    c += [f'> tp @e[tag=st264] {x}.5 -59.9 208.5', f'> zhh2 stomp {x} -60 208', f'> zhh2 tank {x} -60 208']
body('SQ-HH-264', c)
r = rows['SQ-HH-268']; r[1:1] = form4(240, 208)
r = rows['SQ-HH-269']; r[1:1] = form4(272, 208)
r = rows['SQ-HH-270']; r[1:1] = form4(304, 208)
body('SQ-HH-270', form4(304, 208) + ['> zhh2 insert 304 -60 208 minecraft:sweet_berries 4',
    '> summon minecraft:villager 305.5 -59.9 209.5 {NoAI:1b}', '> zhh2 stomp 305 -60 209', '> zhh2 tank 304 -60 208',
    '> zhh2 basin 304 -60 208'])
for rid, (x, z) in (('SQ-HH-271', (16, 240)), ('SQ-HH-272', (48, 240)), ('SQ-HH-273', (80, 240))):
    rows[rid][1:1] = form4(x, z)
out = head + [l for rid in order for l in rows[rid]]
(D / 'hh_mach.qb').write_text('\n'.join(out))
print(len(order), 'rows')
