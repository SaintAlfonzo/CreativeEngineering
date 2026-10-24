#!/usr/bin/env python3
"""Offline check of the donor jar's data against what the port registers and the pack's last dumps.

    python3 development/backportedMiners/tools/check_donor.py

Reads the upstream jar in miners_upstream/ (as MinersTranslator does: the stray minersdelight namespace moved to
miners_delight, files naming a REMOVED id dropped), the port's ids from MDItems / MDBlocks / MDPotions, and
kubejs/exported/ (items_by_mod, fluids.json, tags/*_tags.json, the last /dumpall). Reports every item, block, tag,
loot type or condition that a recipe, loot table, loot modifier, tag or cup conversion names but nothing provides, and
any 1.20-only loot condition name left after translation. Recipes guarded by forge:mod_loaded for a mod the pack
doesn't have are skipped, as Forge skips them. The miners_delight ids come from the port's source, since the dumps were
made with the 1.1.2 jar.
"""
import glob
import json
import os
import re
import sys
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
MOD = os.path.dirname(HERE)
INSTANCE = os.path.dirname(os.path.dirname(MOD))
EXPORTED = os.path.join(INSTANCE, 'kubejs', 'exported')
DONOR = os.path.join(INSTANCE, 'miners_upstream', 'miners_delight-1.20.1-1.4.5-backport.jar')
JAVA = os.path.join(MOD, 'src', 'main', 'java', 'com', 'sammy', 'minersdelight')
NS = 'miners_delight'
REMOVED = {'hot_cocoa_cup'}
MISSING_BOWLS = {'farmersdelight:onion_soup'}

# Loot types a 1.19.2 pack with Forge, Farmer's Delight and this port knows.
KNOWN_CONDITIONS = {
    'minecraft:inverted', 'minecraft:alternative', 'minecraft:random_chance', 'minecraft:random_chance_with_looting',
    'minecraft:entity_properties', 'minecraft:killed_by_player', 'minecraft:entity_scores', 'minecraft:block_state_property',
    'minecraft:match_tool', 'minecraft:table_bonus', 'minecraft:survives_explosion', 'minecraft:damage_source_properties',
    'minecraft:location_check', 'minecraft:weather_check', 'minecraft:reference', 'minecraft:time_check',
    'minecraft:value_check', 'forge:loot_table_id', 'forge:can_tool_perform_action', NS + ':block_tag',
}
KNOWN_MODIFIERS = {'farmersdelight:add_item', 'farmersdelight:add_loot_table', NS + ':add_items', NS + ':replace_items'}
KNOWN_FUNCTIONS_PREFIX = ('minecraft:', NS + ':copy_meal')


def own_ids(path, pattern):
    found = set()
    for m in re.findall(pattern, open(path).read()):
        found.update(g for g in (m if isinstance(m, tuple) else (m,)) if g)
    return {NS + ':' + m for m in found}


def pack_items():
    items = set()
    for f in glob.glob(os.path.join(EXPORTED, 'items_by_mod', '*.json')):
        items.update(json.load(open(f)).get('items', []))
    return {i for i in items if not i.startswith(NS + ':')}


def donor_data():
    """path -> parsed JSON, translated like MinersTranslator.data()."""
    out = {}
    with zipfile.ZipFile(DONOR) as jar:
        for name in jar.namelist():
            if not name.startswith('data/') or not name.endswith('.json'):
                continue
            path = name.replace('data/minersdelight/', 'data/miners_delight/', 1)
            text = jar.read(name).decode('utf-8')
            if any(f'"{NS}:{r}"' in text for r in REMOVED) and '/tags/' not in path and '/cup_conversion/' not in path:
                continue
            if '/loot_modifiers/' in path or '/loot_tables/' in path:
                text = text.replace('"minecraft:any_of"', '"minecraft:alternative"')
            out[path] = json.loads(text)
    return out


def main():
    items = pack_items() | own_ids(os.path.join(JAVA, 'registry', 'MDItems.java'), r'(?:food|bowlFood|cupFood)\("([a-z_]+)"|ITEMS\.register\("([a-z_]+)"')
    # block(MDBlocks.X, ...) items take the block's name.
    items |= {NS + ':' + m.lower() for m in re.findall(r'block\(MDBlocks\.([A-Z_]+)', open(os.path.join(JAVA, 'registry', 'MDItems.java')).read())}
    blocks = own_ids(os.path.join(JAVA, 'registry', 'MDBlocks.java'), r'BLOCKS\.register\("([a-z_]+)"')
    items.add('minecraft:air')
    # Only tagged blocks are in the dump; vanilla 1.19.2 blocks (the infested set) are taken as present.
    pack_blocks = set(json.load(open(os.path.join(EXPORTED, 'tags', 'block_tags_by_block.json'))))
    pack_blocks |= {f'minecraft:{b}' for b in ('infested_stone', 'infested_cobblestone', 'infested_stone_bricks', 'infested_cracked_stone_bricks',
                                               'infested_mossy_stone_bricks', 'infested_chiseled_stone_bricks', 'infested_deepslate')}
    item_tags = set(json.load(open(os.path.join(EXPORTED, 'tags', 'item_tags.json'))))
    block_tags = set(json.load(open(os.path.join(EXPORTED, 'tags', 'block_tags.json'))))
    entity_tags = set(json.load(open(os.path.join(EXPORTED, 'tags', 'entity_type_tags.json'))))
    fluids = set(json.load(open(os.path.join(EXPORTED, 'fluids.json'))))
    mods = {i.split(':')[0] for i in items}
    data = donor_data()
    # Tags the donor defines count as present.
    for path in data:
        m = re.match(r'data/([^/]+)/tags/(items|blocks|entity_types)/(.+)\.json$', path)
        if m:
            {'items': item_tags, 'blocks': block_tags, 'entity_types': entity_tags}[m.group(2)].add(m.group(1) + ':' + m.group(3))

    problems = []
    print(f'own items: {len([i for i in items if i.startswith(NS + ":")])}, own blocks: {len(blocks)}')

    def check_item(i, where):
        if not isinstance(i, str):
            return
        if i.startswith('#'):
            if i[1:] not in item_tags:
                problems.append(f'{where}: unknown item tag {i}')
        elif i not in items:
            problems.append(f'{where}: unknown item {i}')

    def walk(node, where, kind):
        """kind: 'recipe' or 'loot' -- decides what a bare "tag" or "name" means."""
        if isinstance(node, list):
            for n in node:
                walk(n, where, kind)
            return
        if not isinstance(node, dict):
            return
        for key, value in node.items():
            if key == 'item' and isinstance(value, str):
                check_item(value, where)
            elif key == 'tag' and isinstance(value, str) and kind in ('recipe', 'loot'):
                if value not in item_tags:
                    problems.append(f'{where}: unknown item tag #{value}')
            elif key == 'fluid' and isinstance(value, str) and value not in fluids:
                problems.append(f'{where}: unknown fluid {value}')
            elif key == 'block' and isinstance(value, str) and kind == 'loot' and value not in blocks and value not in pack_blocks:
                problems.append(f'{where}: unknown block {value}')
            elif key == 'condition' and kind == 'loot' and value not in KNOWN_CONDITIONS:
                problems.append(f'{where}: loot condition {value} is not known on 1.19.2')
            elif key == 'function' and kind == 'loot' and not str(value).startswith(KNOWN_FUNCTIONS_PREFIX):
                problems.append(f'{where}: unknown loot function {value}')
            elif key == 'name' and kind == 'loot' and node.get('type') == 'minecraft:item':
                check_item(value, where)
            elif key == 'result' and isinstance(value, str):
                check_item(value, where)
            walk(value, where, kind)

    def mod_loaded_ok(js):
        for c in js.get('conditions', []) if isinstance(js.get('conditions'), list) else []:
            if isinstance(c, dict) and c.get('type') == 'forge:mod_loaded' and c.get('modid') not in mods:
                return False
        return True

    counts = {}
    for path, js in sorted(data.items()):
        where = path
        if '/advancements/' in path:
            continue  # the pack drops every advancement at load (D10)
        if '/recipes/' in path:
            counts['recipes'] = counts.get('recipes', 0) + 1
            if mod_loaded_ok(js):
                walk(js, where, 'recipe')
        elif '/loot_tables/' in path:
            counts['loot tables'] = counts.get('loot tables', 0) + 1
            walk(js, where, 'loot')
        elif '/loot_modifiers/' in path and not path.endswith('global_loot_modifiers.json'):
            counts['loot modifiers'] = counts.get('loot modifiers', 0) + 1
            if js.get('type') not in KNOWN_MODIFIERS:
                problems.append(f'{where}: unknown loot modifier type {js.get("type")}')
            walk(js, where, 'loot')
            if 'lootTable' in js:
                table = js['lootTable'].split(':')
                if f'data/{table[0]}/loot_tables/{table[1]}.json' not in data:
                    problems.append(f'{where}: loot table {js["lootTable"]} is not in the donor')
        elif path.endswith('global_loot_modifiers.json'):
            # MinersTranslator drops entries without a file; report how many.
            dangling = [e for e in js['entries'] if f'data/{e.split(":")[0]}/loot_modifiers/{e.split(":")[1]}.json' not in data]
            print(f'global_loot_modifiers: {len(js["entries"]) - len(dangling)} listed with a file, {len(dangling)} without (dropped): '
                  + ', '.join(dangling))
        elif '/tags/' in path:
            counts['tags'] = counts.get('tags', 0) + 1
            kind = path.split('/')[3]
            for v in js.get('values', []):
                vid = v['id'] if isinstance(v, dict) else v
                required = not (isinstance(v, dict) and v.get('required') is False)
                if not required:
                    continue
                if kind == 'items':
                    check_item(vid, where)
                elif kind == 'blocks':
                    if vid.startswith('#'):
                        if vid[1:] not in block_tags:
                            problems.append(f'{where}: unknown block tag {vid}')
                    elif vid not in blocks and vid not in pack_blocks:
                        problems.append(f'{where}: unknown block {vid}')
        elif '/cup_conversion/' in path:
            for row in js['entries']:
                if row['bowl'] in MISSING_BOWLS or any(f'{NS}:{r}' in (row['bowl'], row['cup']) for r in REMOVED):
                    continue
                check_item(row['bowl'], where)
                check_item(row['cup'], where)
    print('checked: ' + ', '.join(f'{v} {k}' for k, v in sorted(counts.items())))
    for p in problems:
        print('  ' + p)
    print(f'{len(problems)} problem(s)')
    return 1 if problems else 0


if __name__ == '__main__':
    sys.exit(main())
