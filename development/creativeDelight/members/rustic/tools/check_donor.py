#!/usr/bin/env python3
"""Offline check of the Rustic Delight donor jar against what the port registers and the pack's last dumps.

    python3 development/creativeDelight/members/rustic/tools/check_donor.py

Reads the upstream jar in rustic_upstream/ and the port's ids from RDBlocks / RDItems / RDPotions (string literals and
the nine-colour patterns). Reports:
  - ids upstream registers (lang keys, blockstates, item models) that the port does not, and the reverse;
  - every rusticdelight id the kept data names that the port does not register;
  - items from other mods named by kept recipes that the last /dumpall does not have (RusticTranslator drops those
    recipes at load; listed so the drop is a known one);
  - loot condition and function names 1.19.2 does not know, after the translator's renames.
Folders and files the translator drops (advancements, biome modifiers, FD recipe edits, chest swaps) are skipped.
"""
import json
import os
import re
import sys
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
MOD = os.path.dirname(HERE)
INSTANCE = os.path.abspath(os.path.join(MOD, '..', '..', '..', '..'))
DONOR = os.path.join(INSTANCE, 'rustic_upstream', 'rusticdelight-forge-1.20.1-1.7.0.jar')
JAVA = os.path.join(MOD, 'src', 'main', 'java', 'com', 'SaintAlfonzo', 'creativedelight', 'rustic', 'registry')
ITEMS_BY_MOD = os.path.join(INSTANCE, 'kubejs', 'exported', 'items_by_mod')
NS = 'rusticdelight'
REMOVED = {'fruit_beignet'}
COLOURS = ['green', 'yellow', 'red', 'orange', 'white', 'pink', 'blue', 'purple', 'black']
DROPPED = ('/advancements/', 'data/rusticdelight/forge/biome_modifier/', 'data/farmersdelight/recipes/')
DROPPED_MODIFIERS = {'coffee_beans_from_jungle_temple', 'pale_bell_pepper_seeds_from_jungle_temple',
                     'dark_bell_pepper_seeds_from_jungle_temple', 'cotton_seeds_from_village_taiga',
                     'cotton_seeds_from_village_snowy', 'cotton_seeds_from_village_savanna',
                     'cotton_seeds_from_abandoned_mineshaft', 'cotton_seeds_from_shipwreck_supply'}
KNOWN_CONDITIONS = {'minecraft:inverted', 'minecraft:alternative', 'minecraft:random_chance', 'minecraft:block_state_property',
                    'minecraft:match_tool', 'minecraft:survives_explosion', 'minecraft:table_bonus', 'forge:loot_table_id',
                    'minecraft:killed_by_player', 'minecraft:entity_properties', 'minecraft:random_chance_with_looting'}
RENAMED_CONDITIONS = {'minecraft:any_of', 'minecraft:all_of'}


def port_ids():
    ids = set()
    for name in ('RDBlocks.java', 'RDItems.java'):
        text = open(os.path.join(JAVA, name)).read()
        ids.update(re.findall(r'(?<!\.get)\(\s*"([a-z_]+)"\s*[,)]', text))
        for prefix, suffix in re.findall(r'"([a-z_]+)" \+ colour(?: \+ "([a-z_]+)")?', text):
            ids.update(prefix + c + suffix for c in COLOURS)
    return ids - {NS} - set(COLOURS)


def main():
    if not os.path.isfile(DONOR):
        sys.exit('donor jar missing: ' + DONOR)
    zf = zipfile.ZipFile(DONOR)
    names = [n for n in zf.namelist() if not n.endswith('/')]
    ours = port_ids()
    problems = 0

    lang = json.loads(zf.read('assets/rusticdelight/lang/en_us.json'))
    upstream = {k.split('.', 2)[2] for k in lang if k.startswith(('item.rusticdelight.', 'block.rusticdelight.')) and k.count('.') == 2}
    upstream |= {n.rsplit('/', 1)[1][:-5] for n in names if n.startswith('assets/rusticdelight/blockstates/')}
    upstream -= REMOVED
    for label, ids in (('upstream registers, port does not', upstream - ours), ('port registers, upstream has no name/state for', ours - upstream)):
        if ids:
            problems += len(ids)
            print(label + ':', ' '.join(sorted(ids)))

    others = set()
    for f in os.listdir(ITEMS_BY_MOD):
        if f.endswith('.json') and f != NS + '.json':
            others.update(json.load(open(os.path.join(ITEMS_BY_MOD, f))).get('items', []))

    # Tags and data types under the rusticdelight namespace, named by recipes but not registry ids.
    not_ids = {n.split('/', 4)[4][:-5] for n in names if n.startswith('data/rusticdelight/tags/')} | {'config_boolean'}
    missing_ns, missing_other, bad_loot = {}, {}, {}
    for n in names:
        if not n.startswith('data/') or not n.endswith('.json') or any(d in n for d in DROPPED):
            continue
        if n.startswith('data/rusticdelight/loot_modifiers/') and n.rsplit('/', 1)[1][:-5] in DROPPED_MODIFIERS:
            continue
        text = zf.read(n).decode()
        if any('"%s:%s"' % (NS, r) in text for r in REMOVED) and '/tags/' not in n:
            continue
        for ref in set(re.findall(r'"rusticdelight:([a-z_/]+)"', text)):
            if '/' not in ref and ref not in ours and ref not in not_ids and '/worldgen/' not in n and '/loot_modifiers/' not in n:
                missing_ns.setdefault(ref, n)
        if '/recipes/' in n:
            for ref in set(re.findall(r'"item"\s*:\s*"([a-z0-9_]+:[a-z0-9_/]+)"', text)):
                if not ref.startswith(NS + ':') and not ref.startswith('minecraft:') and ref not in others:
                    missing_other.setdefault(ref, []).append(n.rsplit('/', 1)[1])
        if '/loot_tables/' in n or '/loot_modifiers/' in n:
            for kind, ref in re.findall(r'"(condition|function)"\s*:\s*"([a-z_:]+)"', text):
                if kind == 'condition' and ref not in KNOWN_CONDITIONS | RENAMED_CONDITIONS:
                    bad_loot.setdefault(ref, n)
                if kind == 'function' and not ref.startswith('minecraft:'):
                    bad_loot.setdefault(ref, n)

    for label, found in (('rusticdelight ids named by data, not registered', missing_ns),
                         ('loot condition/function names 1.19.2 lacks', bad_loot)):
        if found:
            problems += len(found)
            print(label + ':')
            for k, v in sorted(found.items()):
                print('  ', k, '<-', v)
    if missing_other:
        print('recipes the translator drops (item not in the last /dumpall):')
        for k, v in sorted(missing_other.items()):
            print('  ', k, '<-', ', '.join(sorted(v)[:4]) + (' ...' if len(v) > 4 else ''))
    print('port ids: %d, upstream ids: %d, problems: %d' % (len(ours), len(upstream), problems))
    sys.exit(1 if problems else 0)


if __name__ == '__main__':
    main()
