#!/usr/bin/env python3
"""Copy the art and data of the backported Delightful 3.8.1 items into the overlay (feature_writeup/delightful.md 3).

Reads the donor jar (upstream/donors/Delightful-1.20.1-3.8.1.jar, MIT) and the upstream 3.4.1 jar, writes under
src/main/resources/:
  - blockstates, block and item models, textures of ITEMS and BLOCKS, following model parents and texture refs
    (only files the donor has and 3.4.1 does not; parents from Farmer's Delight or vanilla stay where they are)
  - block loot tables, without 1.20's random_sequence field
  - assets/delightful/lang/en_us.json: 3.4.1's file plus the new names (listed in replaced_resources)
No cup items: the pack has no Copper Cup (TODO dishes/meals #53), so 3.8.1's Cactus Soup Cup and its cup_conversion
row are not taken.
Run from anywhere: python3 tools/port_resources.py
"""
import json
import re
import zipfile
from pathlib import Path

MEMBER = Path(__file__).resolve().parent.parent
CD = MEMBER.parent.parent
DONOR = zipfile.ZipFile(CD / 'upstream/donors/Delightful-1.20.1-3.8.1.jar')
UPSTREAM = zipfile.ZipFile(CD / 'upstream/Delightful-1.19.2-3.4.1.jar')
OUT = MEMBER / 'src/main/resources'

ITEMS = [
    'cantaloupe_seeds', 'cantaloupe_bread', 'wrapped_cantaloupe', 'cantaloupe_popsicle', 'stuffed_cantaloupe_block',
    'stuffed_cantaloupe', 'cantaloupe_gummy', 'cactus_chili', 'cactus_soup', 'baklava',
    'baklava_slice', 'matcha_milkshake', 'salmonberry_milkshake', 'matcha_gummy', 'roasted_acorn', 'nut_dough',
    'aluminum_knife', 'certus_quartz_knife', 'fluix_knife',
]
BLOCKS = [
    'cantaloupe_plant', 'stuffed_cantaloupe_block', 'baklava', 'matcha_milkshake_cauldron',
    'salmonberry_milkshake_cauldron',
]
# The Fluix Knife tooltip (3.8.1 builds it in code, untranslated)
EXTRA_LANG = {'item.delightful.fluix_knife.tooltip': 'Always has at least %s'}

donor_names = set(DONOR.namelist())
upstream_names = set(UPSTREAM.namelist())
written = []


def put(path, data):
    target = OUT / path
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(data)
    written.append(path)


def res(ref, kind, ext):
    ns, p = ref.split(':', 1) if ':' in ref else ('minecraft', ref)
    return f'assets/{ns}/{kind}/{p}{ext}'


def model(path, seen=set()):
    if path in seen or path not in donor_names:
        return
    seen.add(path)
    data = DONOR.read(path)
    put(path, data)
    d = json.loads(data)
    if 'parent' in d and not d['parent'].startswith('builtin'):
        model(res(d['parent'], 'models', '.json'))
    for t in d.get('textures', {}).values():
        tex = res(t, 'textures', '.png')
        # 3.4.1's own art stays (3.8.1 redrew the cantaloupe; the stuffed cantaloupe uses 3.4.1's then)
        if not t.startswith('#') and tex in donor_names and tex not in upstream_names:
            put(tex, DONOR.read(tex))
            if tex + '.mcmeta' in donor_names:
                put(tex + '.mcmeta', DONOR.read(tex + '.mcmeta'))


for b in BLOCKS:
    state = f'assets/delightful/blockstates/{b}.json'
    data = DONOR.read(state)
    put(state, data)
    for m in sorted(set(re.findall(r'"model"\s*:\s*"([^"]+)"', data.decode()))):
        model(res(m, 'models', '.json'))
    loot = json.loads(DONOR.read(f'data/delightful/loot_tables/blocks/{b}.json'))
    loot.pop('random_sequence', None)
    put(f'data/delightful/loot_tables/blocks/{b}.json', (json.dumps(loot, indent=2) + '\n').encode())

for i in ITEMS:
    model(f'assets/delightful/models/item/{i}.json')

lang = json.loads(UPSTREAM.read('assets/delightful/lang/en_us.json'))
donor_lang = json.loads(DONOR.read('assets/delightful/lang/en_us.json'))
for key, value in donor_lang.items():
    tail = key.split('.')[-1]
    if key.split('.')[0] in ('item', 'block') and tail in ITEMS + BLOCKS and key not in lang:
        lang[key] = value
lang['jei.delightful.info.cantaloupe_seeds'] = donor_lang['jei.delightful.info.cantaloupe_seeds']
lang.update(EXTRA_LANG)
put('assets/delightful/lang/en_us.json', (json.dumps(lang, indent=2, ensure_ascii=False) + '\n').encode())

print(f'{len(written)} files written under {OUT.relative_to(MEMBER)}')
