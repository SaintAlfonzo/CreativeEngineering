#!/usr/bin/env python3
"""Offline check of the Golden Food rebuild against its donor jar (feature_writeup/golden_food.md 6).

    python3 development/creativeDelight/members/golden/tools/check_donor.py

Reports, and exits 1 on any problem:
  - the donor jar missing or not the pinned file (sha1 in upstream/UpstreamJars.java);
  - goldenfood item ids the last /dumpall saw from the upstream jar (kubejs/exported/items_by_mod/goldenfood.json), the
    donor's item models and its lang that the rebuild does not register, and the reverse; the three vanilla ids
    upstream replaces (golden_apple, enchanted_golden_apple, golden_carrot) registered under minecraft;
  - blocks only on one side, and donor blockstate variants naming a property or value the rebuild's cakes do not have;
  - no enchantment registered (upstream's gf_enchantment is removed) and that id in CD swaps/removed.json;
  - every enchanted golden food but the cake made by an F.goldenEnchant assembly in CD goldenfood/golden.js (or
    minecraft/golden.js for the apple), each with its SEQUENCED_ASSEMBLIES row in transition_items.js;
  - every goldenfood id the kept donor data names that does not exist (the village tables are not served);
  - in the built portedGolden jar (and, when built, the one nested in the Creative Delight jar): a donor file (same
    bytes), a class of the upstream package, or any assets/ or data/ entry.
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
SRC = os.path.join(MOD, 'src', 'main', 'java', 'com', 'SaintAlfonzo', 'creativedelight', 'golden')
DONOR = os.path.join(INSTANCE, 'goldenfood_upstream', 'goldenfood-1.19.2-2.0.0.jar')
DUMP = os.path.join(INSTANCE, 'kubejs', 'exported', 'items_by_mod', 'goldenfood.json')
OWN_LIBS = os.path.join(MOD, 'build', 'libs')
CD_LIBS = os.path.join(INSTANCE, 'development', 'creativeDelight', 'build', 'libs')
NS = 'goldenfood'
# upstream's replacements of vanilla items (GFItems registers them under minecraft)
VANILLA = {'golden_apple', 'enchanted_golden_apple', 'golden_carrot'}
# upstream item models / lang keys with no registered item (gluttony: a stray model; the three: lang for the vanilla ids)
NOT_ITEMS = {'gluttony'} | VANILLA
# GoldenTranslator.skipData: the 16 village chest copies (vanilla plus the removed enchanted book)
SKIP_DATA = ('data/minecraft/loot_tables/chests/village/',)
REMOVED = os.path.join(INSTANCE, 'development', 'creativeDelight', 'src', 'main', 'resources', 'data', 'creativedelight', 'swaps', 'removed.json')
KJS = os.path.join(INSTANCE, 'kubejs')
GOLDEN_JS = [os.path.join(KJS, 'data', 'creativedelight', 'kubejs', d, 'golden.js') for d in ('goldenfood', 'minecraft')]
TRANSITION = os.path.join(KJS, 'startup_scripts', 'changes', 'transition_items.js')
# cake block state properties of the rebuild (vanilla CakeBlock)
PROPS = {'bites': {str(i) for i in range(7)}}

problems = []


def source(*parts):
    return open(os.path.join(SRC, *parts), encoding='utf8').read()


def registered():
    """({goldenfood item ids}, {minecraft item ids}, {block ids}, {enchantment ids})"""
    items_src = source('registry', 'GoldenItems.java')
    items = set(re.findall(r'\bITEMS\.register\("([a-z0-9_]+)",', items_src))
    vanilla = set(re.findall(r'VANILLA_ITEMS\.register\("([a-z0-9_]+)"', items_src))
    foods = re.findall(r'\b(?:plain|pair)\("([a-z0-9_]+)"', items_src)
    for arr in re.findall(r'new String\[\]\{([^}]*)\}', items_src):
        foods += re.findall(r'"([a-z0-9_]+)"', arr)
    for f in foods:
        items |= {f'golden_{f}', f'enchanted_golden_{f}'}
    blocks = set(re.findall(r'BLOCKS\.register\("([a-z0-9_]+)"', source('registry', 'GoldenBlocks.java')))
    ench = set()
    for root, _, files in os.walk(SRC):
        for f in files:
            if 'ForgeRegistries.ENCHANTMENTS' in open(os.path.join(root, f), encoding='utf8').read():
                ench.add(f)
    return items, vanilla, blocks, ench


def check_jar(label, jar, donor, donor_hashes):
    for n in jar.namelist():
        if n.endswith('/'):
            continue
        if n.startswith('net/ggwpgaming/'):
            problems.append(f'{label}: upstream class {n}')
        if n.startswith(('assets/', 'data/')):
            problems.append(f'{label}: resource {n} (art and data must come from the donor at runtime)')
        h = hashlib.sha1(jar.read(n)).hexdigest()
        if h in donor_hashes:
            problems.append(f'{label}: {n} has the bytes of donor file {donor_hashes[h]}')
    print(f'{label}: {len([n for n in jar.namelist() if not n.endswith("/")])} entries checked')


def main():
    if not os.path.exists(DONOR):
        print(f'PROBLEM donor jar missing: {DONOR}')
        return 1
    pin = dict(re.findall(r'new Jar\("([^"]+)", "([0-9a-f]{40})"', source('upstream', 'UpstreamJars.java')))
    sha1 = hashlib.sha1(open(DONOR, 'rb').read()).hexdigest()
    if pin.get(os.path.basename(DONOR)) != sha1:
        problems.append(f'donor sha1 {sha1}, UpstreamJars pins {pin}')

    items, vanilla, blocks, ench = registered()
    donor = zipfile.ZipFile(DONOR)
    names = donor.namelist()

    upstream_items = {n.split('/')[-1][:-5] for n in names if n.startswith(f'assets/{NS}/models/item/') and n.endswith('.json')}
    lang = json.loads(donor.read(f'assets/{NS}/lang/en_us.json'))
    upstream_items |= {k.split('.')[2] for k in lang if k.startswith((f'item.{NS}.', f'block.{NS}.'))}
    upstream_items -= NOT_ITEMS
    if os.path.exists(DUMP):
        dumped = {i.split(':')[1] for i in json.load(open(DUMP))['items'] if i.startswith(NS + ':')}
        upstream_items |= dumped
    else:
        print(f'note: {DUMP} missing, compared against the donor models and lang only')
    for i in sorted(upstream_items - items):
        problems.append(f'upstream item {NS}:{i} not registered')
    for i in sorted(items - upstream_items):
        problems.append(f'registered item {NS}:{i} unknown upstream')
    gfitems = donor.read('net/ggwpgaming/goldenfood/item/GFItems.class')
    for v in sorted(VANILLA):
        if v.encode() not in gfitems:
            problems.append(f'VANILLA lists minecraft:{v}, which upstream GFItems does not name')
    if b'minecraft' not in gfitems:
        problems.append('upstream GFItems has no minecraft register (VANILLA out of date)')
    if vanilla != VANILLA:
        problems.append(f'vanilla replacements: rebuild {sorted(vanilla)}, upstream {sorted(VANILLA)}')

    upstream_blocks = {n.split('/')[-1][:-5] for n in names if n.startswith(f'assets/{NS}/blockstates/') and n.endswith('.json')}
    for b in sorted(upstream_blocks ^ blocks):
        problems.append(f'block {NS}:{b} only on one side (upstream {b in upstream_blocks}, rebuild {b in blocks})')
    for b in sorted(upstream_blocks):
        state = json.loads(donor.read(f'assets/{NS}/blockstates/{b}.json'))
        for key in state.get('variants', {}):
            for part in filter(None, key.split(',')):
                prop, _, value = part.partition('=')
                if value not in PROPS.get(prop, ()):
                    problems.append(f'blockstate {b}: variant "{key}" names {prop}={value}, not a state of the rebuild')

    upstream_ench = {k.split('.')[2] for k in lang if k.startswith(f'enchantment.{NS}.')}
    if ench:
        problems.append(f'enchantments registered: {sorted(ench)} (upstream {sorted(upstream_ench)} is removed)')
    removed = set(json.load(open(REMOVED)).get('removed', [])) if os.path.exists(REMOVED) else set()
    for e in sorted(upstream_ench):
        if f'{NS}:{e}' not in removed:
            problems.append(f'{NS}:{e} missing from {os.path.relpath(REMOVED, INSTANCE)} "removed"')

    # Enchanted golden foods: one F.goldenEnchant(i.<golden>, i.<enchanted>) each, plus an incomplete item row
    calls = set()
    for js in GOLDEN_JS:
        if os.path.exists(js):
            calls |= set(re.findall(r'goldenEnchant\(\s*i\.([a-z0-9_]+)\s*,\s*i\.([a-z0-9_]+)\s*\)', open(js, encoding='utf8').read()))
    made = {out for _, out in calls}
    rows = set(re.findall(r"\[\s*'([a-z0-9_]+)'", open(TRANSITION, encoding='utf8').read()))
    wanted = {i for i in items if i.startswith('enchanted_golden_') and i != 'enchanted_golden_cake'} | {'enchanted_golden_apple'}
    for w in sorted(wanted - made):
        problems.append(f'{w}: no F.goldenEnchant call in goldenfood/golden.js')
    for w in sorted(wanted - rows):
        problems.append(f'{w}: no SEQUENCED_ASSEMBLIES row in transition_items.js')
    for g, out in sorted(calls):
        if out.replace('enchanted_', '', 1) != g:
            problems.append(f'goldenEnchant(i.{g}, i.{out}): input is not the golden form of the output')

    known = items | blocks
    for n in names:
        if not n.startswith('data/') or n.endswith('/') or not n.endswith('.json') or n.startswith(SKIP_DATA):
            continue
        for ref in set(re.findall(rf'"{NS}:([a-z0-9_/]+)"', donor.read(n).decode('utf8'))):
            if ref not in known:
                problems.append(f'{n} names {NS}:{ref}, which the rebuild does not have')

    donor_hashes = {hashlib.sha1(donor.read(n)).hexdigest(): n for n in names if not n.endswith('/')}
    own = sorted(f for f in os.listdir(OWN_LIBS) if re.fullmatch(r'portedGolden-.*\.jar', f)) if os.path.isdir(OWN_LIBS) else []
    if not own:
        problems.append('no built portedGolden jar in members/golden/build/libs (./gradlew :portedGolden:build --offline)')
    else:
        check_jar(own[-1], zipfile.ZipFile(os.path.join(OWN_LIBS, own[-1])), donor, donor_hashes)
    cds = sorted(f for f in os.listdir(CD_LIBS) if re.fullmatch(r'creativedelight-[0-9.]+\.jar', f)) if os.path.isdir(CD_LIBS) else []
    if cds:
        cd = zipfile.ZipFile(os.path.join(CD_LIBS, cds[-1]))
        nested = [n for n in cd.namelist() if re.search(r'META-INF/jarjar/portedGolden-.*\.jar$', n)]
        if len(nested) == 1:
            check_jar(f'{cds[-1]}!{nested[0]}', zipfile.ZipFile(io.BytesIO(cd.read(nested[0]))), donor, donor_hashes)
        else:
            print(f'note: {cds[-1]} nests {len(nested)} portedGolden jars (not yet in bundle_members, or not rebuilt)')

    print(f'items {len(items)} goldenfood + {len(vanilla)} minecraft, blocks {len(blocks)}, enchantments {len(ench)}, '
          f'assemblies {len(calls)}; upstream items {len(upstream_items)}, blocks {len(upstream_blocks)}')
    for p in problems:
        print('PROBLEM', p)
    print(f'{len(problems)} problems')
    return 1 if problems else 0


if __name__ == '__main__':
    sys.exit(main())
