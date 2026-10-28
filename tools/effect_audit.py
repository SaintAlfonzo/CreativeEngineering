#!/usr/bin/env python3
"""Effect audit: the mechanical columns of feature_writeup/effects.md, checked offline.

For every registered mob effect it reports the display name, the JEED description and where it comes from, the
potions that carry it, and whether each potion's tipped arrow has a name. It then lists the problems the writeup
tracks: effects with no name, no description or a "TODO" placeholder, placeholders for effects that no longer exist,
potions whose potion / splash / lingering / tipped-arrow name key is missing (the raw key shows in game), and effects
with no item at all on their JEI page (JEED's own sources plus the pack's provider recipes, below).

Source data:
  kubejs/exported/mob_effects.json, potions.json    /dumppotions
  lang_index.build()                                every en_us lang table the client loads: vanilla and Forge
                                                    jars, *_upstream/*.jar (the ports read their lang there),
                                                    mods/*.jar with their jar-in-jar members, kubejs/assets, client
                                                    script renames, then enabled resource-pack zips on top
  feature_writeup/effects.md                        for EFFECT_OVERRIDES below: effects the ports add or drop
  kubejs/exported/food/foods.json                   /dumpfood: FoodProperties effects, which JEED lists by itself
  mods/jeed-*.jar data/jeed/recipes                 JEED's own effect_provider recipes
  kubejs/server_scripts/other/jeed_providers.js     the pack's provider recipes (effect ids as string literals),
  kubejs/startup_scripts/func/{armor,tool}_effects.js   plus the gear registries and the Vinery [wine] effects
  config/vinery-common.toml, config/jeed-client.toml    config it reads; hidden_effects need no source

Vanilla potions are skipped in the name check (the game names them itself).

    python3 tools/effect_audit.py           summary + problems
    python3 tools/effect_audit.py --json    the full per-effect / per-potion table as JSON
"""
import json
import re
import sys
import zipfile
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import lang_index  # noqa: E402

INSTANCE = lang_index.INSTANCE
EXPORTED = INSTANCE / 'kubejs/exported'
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
# Vanilla beacon effects: JEED lists the beacon for these by itself (BeaconBlockEntity.BEACON_EFFECTS)
BEACON_EFFECTS = {'minecraft:speed', 'minecraft:haste', 'minecraft:resistance', 'minecraft:jump_boost',
                  'minecraft:strength', 'minecraft:regeneration'}
EFFECT_ID = re.compile(r"'([a-z0-9_]+:[a-z0-9_]+)'")


def load_lang():
    """key -> (value, source label): every en_us key the client sees (tools/lang_index.py, which also reads the
    vanilla and Forge jars, the jar-in-jar members and the upstream jars the ports read)."""
    names, source = lang_index.build()
    return {k: (v, source[k]) for k, v in names.items()}


def jei_sources(potions):
    """effect id -> the kinds of item JEI shows on its JEED page."""
    src = {}

    def add(eid, kind):
        src.setdefault(eid, set()).add(kind)

    for food in json.loads((EXPORTED / 'food/foods.json').read_text())['foods']:
        for e in food['effects']:
            add(e['effect'], 'food')
    for p in potions.values():
        for e in p['effects']:
            add(e['effect'], 'potion')
    for eid in BEACON_EFFECTS:
        add(eid, 'beacon')
    for jar in (INSTANCE / 'mods').glob('jeed-*.jar'):
        with zipfile.ZipFile(jar) as z:
            for n in z.namelist():
                if n.startswith('data/jeed/recipes/') and n.endswith('.json'):
                    eff = json.loads(z.read(n)).get('effect')
                    if isinstance(eff, str):
                        add(eff, 'jeed')
    providers = (INSTANCE / 'kubejs/server_scripts/other/jeed_providers.js').read_text()
    for eid in EFFECT_ID.findall(providers):
        add(eid, 'provider')
    for f in ('armor_effects.js', 'tool_effects.js'):
        text = (INSTANCE / 'kubejs/startup_scripts/func' / f).read_text()
        for eid in re.findall(r"effect:\s*'([a-z0-9_]+:[a-z0-9_]+)'", text):
            add(eid, 'gear')
    vinery = (INSTANCE / 'config/vinery-common.toml').read_text()
    for eid in re.findall(r'"[a-z0-9_]+:[a-z0-9_]+\|([a-z0-9_]+:[a-z0-9_]+)\|', vinery):
        add(eid, 'wine')
    return src


def hidden_effects():
    m = re.search(r'^hidden_effects\s*=\s*(\[.*\])', (INSTANCE / 'config/jeed-client.toml').read_text(), re.M)
    return set(json.loads(m.group(1))) if m else set()


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
        table[eid] = {'name': name, 'category': effects[eid].get('category'), 'description': desc,
                      'description_source': src, 'potions': {}, 'jei_sources': []}
    for pid, p in sorted(potions.items()):
        names = {k: lang.get(f'item.minecraft.{k}.effect.{p["name"]}', (None, None))[0] for k in POTION_KINDS}
        for e in p['effects']:
            if e['effect'] in table:
                table[e['effect']]['potions'][pid] = names

    sources = jei_sources(potions)
    hidden = hidden_effects()
    for eid, row in table.items():
        row['jei_sources'] = sorted(sources.get(eid, ()))
        row['hidden_in_jei'] = eid in hidden

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
        if not row['jei_sources'] and not row['hidden_in_jei']:
            problems.append(f'no JEI source    {eid}')
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
