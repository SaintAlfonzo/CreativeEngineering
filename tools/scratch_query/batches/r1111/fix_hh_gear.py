#!/usr/bin/env python3
"""r1111: writes hh_gear_a.qb (SQ-HH-1500 to -1507) and hh_gear_b.qb (-1508 on, after the restart) from
hh_gear_raw.qb, with the prose rows written out: -1545 datapack, -1554 young hops, -1573 / -1586 / -1587 greps
(cwd is srv/), -1575 pouch, -1596 trades. Level name passed as argv[1] (datapack path)."""
import re, sys, pathlib
D = pathlib.Path(__file__).parent
LEVEL = sys.argv[1] if len(sys.argv) > 1 else 'hhm'
rows, cur, order = {}, None, []
for l in (D / 'hh_gear_raw.qb').read_text().split('\n'):
    m = re.match(r'## SQ-HH-(\d+) ', l)
    if m: cur = int(m.group(1)); order.append(cur); rows[cur] = [l]; continue
    if cur: rows[cur].append(l)
def looks(i): return [l for l in rows[i] if l.startswith(('look', 'expect', 'reject', 'count', 'kjs')) and 'UNPARSED' not in l]
def put(i, cmds): rows[i] = [rows[i][0]] + cmds + looks(i) + ['']
dp = f'{LEVEL}/datapacks/zz_hh1545'
put(1545, ['> setblock 509 -61 510 hearthandharvest:tilled_end_stone[moisture=7]', '> pbot hold G minecraft:wheat_seeds 1',
    '> pbot use G 509 -61 510 up', '> execute if block 509 -60 510 minecraft:air',
    f'shell mkdir -p {dp}/data/hearthandharvest/tags/blocks && echo \'{{"pack":{{"pack_format":10,"description":"t"}}}}\' > {dp}/pack.mcmeta && echo \'{{"values":["minecraft:wheat"]}}\' > {dp}/data/hearthandharvest/tags/blocks/grows_on_tilled_end_stone.json',
    '> reload', 'sleep 8', '> pbot hold G minecraft:wheat_seeds 1', '> pbot use G 509 -61 510 up',
    '> execute if block 509 -60 510 minecraft:wheat', f'shell rm -rf {dp}', '> reload', 'sleep 8'])
r = rows[1554]; body = [l for l in r[1:] if l.startswith('>')]
put(1554, body + [b.replace('age=4', 'age=1').replace('514 -60 520', '516 -60 520') for b in body])
put(1573, ['shell grep -n "Shift Range" config/quark-common.toml'])
put(1586, ['shell grep -cE "CE potions\\] mix skipped.*(hearthandharvest|bad_omen)" logs/kubejs/startup.log; true'])
r = rows[1587]; put(1587, [l for l in r[1:] if l.startswith('>')] + ['shell grep -E "potion bottling done|tipped arrow tipping done" logs/kubejs/server.log'])
put(1575, ['> fill 538 -61 549 542 -61 553 minecraft:farmland', '> setblock 540 -58 551 minecraft:dispenser[facing=down]',
    '> data merge block 540 -58 551 {Items:[{Slot:0b,id:"quark:seed_pouch",Count:1b,tag:{storedItem:{id:"minecraft:cobblestone",Count:1b},itemCount:20}}]}',
    '> setblock 541 -58 551 minecraft:redstone_block', 'sleep 2', '> zq count 538 -60 549 542 -60 553 minecraft:wheat',
    '> data get block 540 -58 551 Items[0].tag.itemCount'])
put(1596, ['> dumptrades 200', 'sleep 5', 'shell grep -c "hearthandharvest:" kubejs/exported/villager_trades.json',
    'shell python3 -c \'import json;d=json.load(open("kubejs/exported/villager_trades.json"));p=d.get("professions",d);' +
    'print("farmer1",[t for t in json.dumps(p.get("minecraft:farmer",{})).split("},") if "hearthandharvest" in t or "sunflower" in t][:8]);' +
    'print("botanist",json.dumps(p.get("beautify:botanist",{})).count("_mum"));' +
    'w=d.get("wandering_trader",{});print("wander",[x for x in json.dumps(w).split("},") if "hearthandharvest" in x or "sunflower" in x or "blueberr" in x][:8])\''])
a = [i for i in order if i < 1508]; b = [i for i in order if i >= 1508]
for name, ids in (('hh_gear_a', a), ('hh_gear_b', b)):
    (D / f'{name}.qb').write_text('pace 1\n\n' + '\n'.join(l for i in ids for l in rows[i]) + '\n'); print(name, len(ids))
