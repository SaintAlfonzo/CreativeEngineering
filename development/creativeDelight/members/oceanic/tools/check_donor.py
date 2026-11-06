#!/usr/bin/env python3
"""Offline check of the Oceanic Delight rebuild against its donor jar (feature_writeup/ocean_delight.md 5.6).

    python3 development/creativeDelight/members/oceanic/tools/check_donor.py

Reports, and exits 1 on any problem:
  - item ids the last /dumpall saw from the old repack (kubejs/exported/items_by_mod/oceanic_delight.json) and the
    donor's lang / item models that the rebuild does not register, and the reverse;
  - every oceanic_delight id the kept donor data names (item, block, effect, potion, loot table) that does not exist;
  - blockstate variant keys of the donor that name a property or value the rebuild's blocks do not have;
  - in the built Creative Delight jar: the nested portedOceanic jar holding a donor file (same bytes) or a class of the
    upstream package, and the five fishing net tables missing.
"""
import hashlib
import io
import json
import os
import re
import sys
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
MOD = os.path.dirname(HERE)
INSTANCE = os.path.abspath(os.path.join(MOD, '..', '..', '..', '..'))
DONOR = os.path.join(INSTANCE, 'oceanic_upstream', 'oceanic_delight-1.0.1-forge-1.19.2.jar')
JAVA = os.path.join(MOD, 'src', 'main', 'java', 'com', 'SaintAlfonzo', 'creativedelight', 'oceanic', 'registry')
DUMP = os.path.join(INSTANCE, 'kubejs', 'exported', 'items_by_mod', 'oceanic_delight.json')
CD_LIBS = os.path.join(INSTANCE, 'development', 'creativeDelight', 'build', 'libs')
NS = 'oceanic_delight'
# OceanicTranslator.skipData
SKIP = ('data/oceanic_delight/advancements/', 'data/oceanic_delight/loot_tables/gameplay/fishing_net_')
# Block state properties the rebuild's blocks have (name -> values), per block
FACING = {'north', 'east', 'south', 'west'}
WL = {'waterlogged': {'true', 'false'}}
PROPS = {
    'paella_plate': {'facing': FACING, 'servings': {str(i) for i in range(5)}, **WL},
    'stuffed_pufferfish': {'facing': FACING, 'servings': {str(i) for i in range(5)}, **WL},
    'sea_grape_plant': {'position': {'0', '1'}, **WL},
    'sea_grape_fruit': dict(WL), 'wild_sea_grape': dict(WL),
    'sponge_cake': {'bites': {str(i) for i in range(4)}},
    'sea_pickle_crate': {}, 'sea_grape_crate': {},
}

problems = []


def registered(name):
    text = open(os.path.join(JAVA, name), encoding='utf8').read()
    return set(re.findall(r'(?:register|food|block)\("([a-z0-9_]+)"', text))


def main():
    items = registered('OceanicItems.java')
    blocks = registered('OceanicBlocks.java')
    effects_potions = registered('OceanicEffects.java') | registered('OceanicLootModifiers.java')
    donor = zipfile.ZipFile(DONOR)
    names = donor.namelist()

    upstream_items = {n.split('/')[-1][:-5] for n in names if n.startswith(f'assets/{NS}/models/item/') and n.endswith('.json')}
    lang = json.loads(donor.read(f'assets/{NS}/lang/en_us.json'))
    upstream_items |= {k.split('.')[2] for k in lang if k.startswith((f'item.{NS}.', f'block.{NS}.'))}
    if os.path.exists(DUMP):
        upstream_items |= {i.split(':')[1] for i in json.load(open(DUMP))['items']}
    for i in sorted(upstream_items - items):
        problems.append(f'upstream item {NS}:{i} not registered')
    for i in sorted(items - upstream_items):
        problems.append(f'registered item {NS}:{i} unknown upstream')
    upstream_blocks = {n.split('/')[-1][:-5] for n in names if n.startswith(f'assets/{NS}/blockstates/') and n.endswith('.json')}
    for b in sorted(upstream_blocks ^ blocks):
        problems.append(f'block {NS}:{b} only on one side (upstream {b in upstream_blocks}, rebuild {b in blocks})')

    for b in sorted(upstream_blocks & set(PROPS)):
        state = json.loads(donor.read(f'assets/{NS}/blockstates/{b}.json'))
        for key in state.get('variants', {}):
            for part in filter(None, key.split(',')):
                prop, _, value = part.partition('=')
                if value not in PROPS[b].get(prop, ()):
                    problems.append(f'blockstate {b}: variant "{key}" names {prop}={value}, not a state of the rebuild')

    tables = {n[len(f'data/{NS}/loot_tables/'):-5] for n in names if n.startswith(f'data/{NS}/loot_tables/')}
    modifiers = {n.split('/')[-1][:-5] for n in names if n.startswith(f'data/{NS}/loot_modifiers/') and n.endswith('.json')}
    known = items | blocks | effects_potions | {f'blocks/{b}' for b in blocks} | tables | modifiers
    for n in names:
        if not n.startswith('data/') or n.endswith('/') or n.startswith(SKIP) or not n.endswith('.json'):
            continue
        for ref in set(re.findall(rf'"{NS}:([a-z0-9_/]+)"', donor.read(n).decode('utf8'))):
            if ref not in known and not ref.startswith(('patch_', 'wild_sea_grape')) and f'data/{NS}/worldgen/' not in n:
                if ref.split('/')[-1] not in known:
                    problems.append(f'{n} names {NS}:{ref}, which the rebuild does not have')

    jars = sorted(f for f in os.listdir(CD_LIBS) if re.fullmatch(r'creativedelight-[0-9.]+\.jar', f)) if os.path.isdir(CD_LIBS) else []
    if not jars:
        problems.append('no built creativedelight jar in development/creativeDelight/build/libs')
    else:
        cd = zipfile.ZipFile(os.path.join(CD_LIBS, jars[-1]))
        nested = [n for n in cd.namelist() if re.search(r'META-INF/jarjar/portedOceanic-.*\.jar$', n)]
        if len(nested) != 1:
            problems.append(f'{jars[-1]}: {len(nested)} nested portedOceanic jars')
        else:
            donor_hashes = {hashlib.sha1(donor.read(n)).hexdigest(): n for n in names if not n.endswith('/')}
            inner = zipfile.ZipFile(io.BytesIO(cd.read(nested[0])))
            for n in inner.namelist():
                if n.endswith('/'):
                    continue
                if n.startswith('net/mcreator/'):
                    problems.append(f'{nested[0]}: upstream class {n}')
                h = hashlib.sha1(inner.read(n)).hexdigest()
                if h in donor_hashes:
                    problems.append(f'{nested[0]}: {n} has the bytes of donor file {donor_hashes[h]}')
            print(f'{nested[0]}: {len(inner.namelist())} entries checked')
        for t in ('kelp', 'sea_grass', 'coral', 'ocean_treasure', 'junk'):
            if f'data/creativedelight/loot_tables/gameplay/fishing_net/{t}.json' not in cd.namelist():
                problems.append(f'{jars[-1]}: fishing net table {t} missing')

    print(f'items {len(items)}, blocks {len(blocks)}, effects+potions {len(effects_potions)}; upstream items {len(upstream_items)}, blocks {len(upstream_blocks)}')
    for p in problems:
        print('PROBLEM', p)
    print(f'{len(problems)} problems')
    return 1 if problems else 0


if __name__ == '__main__':
    sys.exit(main())
