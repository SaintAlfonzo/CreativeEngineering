#!/usr/bin/env python3
"""Effect audit: the mechanical columns of feature_writeup/effects.md, checked offline.

For every registered mob effect it reports the display name, the JEED description and where it comes from, the
potions that carry it, and whether each potion's tipped arrow has a name. It then lists the problems the writeup
tracks: effects with no name, no description or a "TODO" placeholder, placeholders for effects that no longer exist,
and potions whose potion / splash / lingering / tipped-arrow name key is missing (the raw key shows in game).

Source data:
  kubejs/exported/mob_effects.json, potions.json    /dumppotions
  mods/*.jar, vinery_upstream/*-1.5.5.jar           every en_us lang table the client loads (the Vinery port reads
  kubejs/assets/**/lang/en_us.json                  its lang from the donor jar), then enabled resource-pack zips
  resourcepacks/*.zip (options.txt order)           on top, as the client stacks them
  feature_writeup/effects.md                        for EFFECT_OVERRIDES below: effects the ports add or drop

Vanilla's own lang lives in the client jar, so vanilla potions are skipped in the name check.

    python3 tools/effect_audit.py           summary + problems
    python3 tools/effect_audit.py --json    the full per-effect / per-potion table as JSON
"""
import json
import re
import sys
import zipfile
from pathlib import Path

INSTANCE = Path(__file__).resolve().parent.parent
EXPORTED = INSTANCE / 'kubejs/exported'
DONOR_JARS = [INSTANCE / 'vinery_upstream/letsdo-vinery-fabric-1.5.5.jar']
PLACEHOLDER = 'TODO: write a description'

# The last /dumppotions ran before the Vinery port went into mods/ (2026-10-19): it dropped these three
# (alcohol.md B7.3). Remove an entry once a fresh dump agrees.
EFFECT_OVERRIDES = {
    'vinery:health_effect': None,
    'vinery:luck_effect': None,
    'vinery:resistance_effect': None,
}
# Pack-original effects named in code rather than by a lang key (same list as gen_effect_descriptions.py).
CODE_NAMES = {'creativeengineering:magic_resistance': 'Magic Resistance'}
POTION_KINDS = ('potion', 'splash_potion', 'lingering_potion', 'tipped_arrow')


def load_lang():
    lang = {}

    def add(raw, src):
        try:
            obj = json.loads(raw.decode('utf-8-sig'))
        except ValueError:
            return
        for k, v in obj.items():
            lang[k] = (v, src)

    def add_zip(path, src):
        try:
            with zipfile.ZipFile(path) as z:
                for n in z.namelist():
                    if re.match(r'assets/[^/]+/lang/en_us\.json$', n):
                        add(z.read(n), src)
        except (zipfile.BadZipFile, FileNotFoundError):
            pass

    for jar in sorted((INSTANCE / 'mods').glob('*.jar')):
        add_zip(jar, jar.name)
    for jar in DONOR_JARS:
        add_zip(jar, jar.name)
    for f in sorted((INSTANCE / 'kubejs/assets').glob('*/lang/en_us.json')):
        add(f.read_bytes(), str(f.relative_to(INSTANCE)))
    options = (INSTANCE / 'options.txt').read_text(errors='replace')
    packs = json.loads(re.search(r'^resourcePacks:(.*)$', options, re.M).group(1))
    for p in packs:
        if p.startswith('file/'):
            add_zip(INSTANCE / 'resourcepacks' / p[5:], p)
    return lang


def main():
    lang = load_lang()
    effects = {}
    for ns in json.loads((EXPORTED / 'mob_effects.json').read_text()).values():
        effects.update(ns)
    for eid, row in EFFECT_OVERRIDES.items():
        if row is None:
            effects.pop(eid, None)
        else:
            effects[eid] = row
    potions = {}
    for ns in json.loads((EXPORTED / 'potions.json').read_text()).values():
        potions.update(ns)

    table = {}
    for eid in sorted(effects):
        mod, path = eid.split(':')
        key = f'effect.{mod}.{path}'
        # Alex's Mobs overrides getDescriptionId to "alexsmobs.potion.<path>"; vanilla names are in the client jar
        name = (lang.get(key) or lang.get(f'{mod}.potion.{path}') or (CODE_NAMES.get(eid), None))[0]
        if mod == 'minecraft':
            name = name or effects[eid]['name']
        desc, src = lang.get(key + '.description', (None, None))
        table[eid] = {'name': name, 'description': desc, 'description_source': src, 'potions': {}}
    for pid, p in sorted(potions.items()):
        names = {k: lang.get(f'item.minecraft.{k}.effect.{p["name"]}', (None, None))[0] for k in POTION_KINDS}
        for e in p['effects']:
            if e['effect'] in table:
                table[e['effect']]['potions'][pid] = names

    if '--json' in sys.argv:
        print(json.dumps(table, indent=1, ensure_ascii=False))
        return

    problems = []
    for eid, row in table.items():
        if not row['name'] and eid not in CODE_NAMES:
            problems.append(f'no name          {eid}')
        if not row['description']:
            problems.append(f'no description   {eid}')
        elif row['description'].startswith(PLACEHOLDER):
            problems.append(f'placeholder      {eid}')
        for pid, names in row['potions'].items():
            if pid.startswith('minecraft:'):
                continue
            for kind, n in names.items():
                if not n:
                    problems.append(f'unnamed {kind:<16} {pid}')
    for key, (value, src) in sorted(lang.items()):
        m = re.match(r'^effect\.([a-z0-9_]+)\.([a-z0-9_]+)\.description$', key)
        if m and value.startswith(PLACEHOLDER) and f'{m.group(1)}:{m.group(2)}' not in table:
            problems.append(f'stale placeholder {m.group(1)}:{m.group(2)}  ({src})')

    with_potion = sum(1 for r in table.values() if r['potions'])
    print(f'{len(table)} effects, {with_potion} carried by a potion, {len(potions)} potions')
    print(f'{len(problems)} problems')
    for p in problems:
        print('  ' + p)


if __name__ == '__main__':
    main()
