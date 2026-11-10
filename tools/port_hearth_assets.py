#!/usr/bin/env python3
"""One-shot porter: copies the kept Hearth and Harvest 1.4.1 assets and loot tables into the backportedHearth member.

Not a /cdevgen generator. Source is the unpacked MIT jar in DecompiledMods/. Re-run after extending KEEP; existing
files are overwritten, the member's own lang keys are kept. Vintage (cask) variants are stripped, per
feature_writeup/hearth_and_harvest.md (the cask is dropped).
"""
import json
import os
import re
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
UP = os.path.join(ROOT, 'DecompiledMods/hearthandharvest-1.21.1-1.4.1')
UA = os.path.join(UP, 'assets/hearthandharvest')
UD = os.path.join(UP, 'data/hearthandharvest')
OUT = os.path.join(ROOT, 'development/creativeDelight/members/hearth/src/main/resources')
OA = os.path.join(OUT, 'assets/hearthandharvest')
OD = os.path.join(OUT, 'data/hearthandharvest')

MUMS = [c + '_mum' for c in 'white red orange yellow light_blue blue purple pink'.split()]
WOODS = 'oak spruce birch jungle acacia dark_oak mangrove crimson warped'.split()
ITEMS = '''hops corn_husk crow_feather wet_manure_brick manure_brick butter manure baked_apple biscuits_and_gravy candy_corn
cider_donut cooked_sausage croissant glazed_carrots hops_salad hot_dog macaroni_and_cheese peanut_butter_cookie
pickled_carrots pickled_potatoes raisin_cookie raisins raspberry_scone raw_sausage street_corn sunflower_seeds tamale
trail_mix goat_cheese_slice chocolate_milk_bottle goat_milk_bottle charred_marshmallow_stick raspberry_pie_slice
grape_pie_slice peanut_butter_pie_slice blueberry_juice raspberry_juice cherry_juice sweet_berry_juice glow_berry_juice
blueberry_wine raspberry_wine sweet_berry_wine glow_berry_wine melon_wine lemon_wine root_beer moonshine brine_bottle
brine_bucket goat_milk_bucket sap_bucket flint_cleaver iron_cleaver golden_cleaver diamond_cleaver netherite_cleaver
pitchfork'''.split()
BLOCKS = '''raspberry_pie grape_pie peanut_butter_pie waffle goat_cheese_wheel unripe_goat_cheese_wheel hay_rug straw_rug
hops_bag manure_bag manure_block manure_bricks manure_brick_slab manure_brick_stairs manure_brick_wall polished_manure
polished_manure_slab polished_manure_stairs polished_manure_wall corn_husk_bundle rope_coil spool crimson_fungus_crate
warped_fungus_crate glistering_melon_crate poisonous_potato_crate rotten_tomato_crate wild_hops tree_tapper'''.split()
BLOCKS += MUMS + [w + '_half_cabinet' for w in WOODS]
POTS = ['potted_' + m for m in MUMS]
BLOCK_ONLY = ['sap_cauldron']  # no item of its own
FLUIDS = '''goat_milk brine sap blueberry_juice raspberry_juice cherry_juice sweet_berry_juice glow_berry_juice
blueberry_wine raspberry_wine sweet_berry_wine glow_berry_wine melon_wine lemon_wine root_beer moonshine'''.split()
EFFECTS = 'pungent tempting prickly clarity pinned'.split()
# Display names that differ from upstream (hearth_and_harvest.md 2.1 and 3.1).
RENAMES = {
    'peanut_butter_cookie': 'Nut Mash Cookie',
    'peanut_butter_pie': 'Nut Mash Pie',
    'peanut_butter_pie_slice': 'Slice of Nut Mash Pie',
}
LEMON_WINE_NAME = 'Lemon Wine'

copied = set()


def copy(src, dst):
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    shutil.copyfile(src, dst)


def texture(ref):
    if ':' not in ref:
        return  # no namespace means minecraft
    ns, path = ref.split(':', 1)
    if ns != 'hearthandharvest':
        return
    if path.startswith('#') or path in copied:
        return
    copied.add(path)
    src = f'{UA}/textures/{path}.png'
    if not os.path.exists(src):
        print('MISSING texture', ref)
        return
    copy(src, f'{OA}/textures/{path}.png')
    if os.path.exists(src + '.mcmeta'):
        copy(src + '.mcmeta', f'{OA}/textures/{path}.png.mcmeta')


def model(ref):
    """Copy a hearthandharvest model, its parents and its textures."""
    if ':' in ref:
        ns, path = ref.split(':', 1)
        if ns != 'hearthandharvest':
            return
    else:
        return
    key = 'model:' + path
    if key in copied:
        return
    copied.add(key)
    src = f'{UA}/models/{path}.json'
    if not os.path.exists(src):
        print('MISSING model', ref)
        return
    j = json.load(open(src))
    j.pop('overrides', None) if any('vintage' in json.dumps(o) for o in j.get('overrides', [])) else None
    os.makedirs(os.path.dirname(f'{OA}/models/{path}.json'), exist_ok=True)
    json.dump(j, open(f'{OA}/models/{path}.json', 'w'), indent=2)
    if 'parent' in j:
        model(j['parent'])
    for v in j.get('textures', {}).values():
        texture(v)
    if j.get('loader') == 'neoforge:composite':
        j['loader'] = 'forge:composite'
        json.dump(j, open(f'{OA}/models/{path}.json', 'w'), indent=2)
        for child in j.get('children', {}).values():
            if 'parent' in child:
                model(child['parent'])
            for v in child.get('textures', {}).values():
                texture(v)


def pitchfork_model():
    """The 3D pitchfork in hand, flat in the GUI, on the ground and in frames."""
    j = json.load(open(f'{UA}/models/item/pitchfork.json'))
    j['loader'] = 'forge:separate_transforms'
    os.makedirs(f'{OA}/models/item', exist_ok=True)
    json.dump(j, open(f'{OA}/models/item/pitchfork.json', 'w'), indent=2)
    model('hearthandharvest:item/pitchfork_in_hand')
    model('hearthandharvest:item/2d_pitchfork')
    texture('hearthandharvest:entity/pitchfork')


def item_model(name):
    if name == 'pitchfork':
        pitchfork_model()
        return
    src = f'{UA}/models/item/{name}.json'
    j = json.load(open(src)) if os.path.exists(src) else None
    if j is None or j.get('loader', '').endswith('separate_transforms'):
        # Placed wine bottles and pickle jars are not ported: the item uses the flat in-hand model.
        flat = json.load(open(f'{UA}/models/item/2d_{name}.json'))
        flat.pop('overrides', None)
        os.makedirs(f'{OA}/models/item', exist_ok=True)
        json.dump(flat, open(f'{OA}/models/item/{name}.json', 'w'), indent=2)
        for v in flat.get('textures', {}).values():
            texture(v)
        if 'parent' in flat:
            model(flat['parent'])
        return
    model('hearthandharvest:item/' + name)


def strip_vintage(name, j):
    if 'variants' in j:
        out = {}
        for k, v in j['variants'].items():
            if 'vintage=' in k:
                if 'vintage=0' not in k:
                    continue
                k = ','.join(p for p in k.split(',') if not p.startswith('vintage='))
            out[k] = v
        j['variants'] = out
    return j


def blockstate(name):
    src = f'{UA}/blockstates/{name}.json'
    if not os.path.exists(src):
        print('MISSING blockstate', name)
        return
    j = strip_vintage(name, json.load(open(src)))
    if name == 'tree_tapper':  # the port has no waterlogged state
        j['variants'] = {k.replace(',waterlogged=false', ''): v for k, v in j['variants'].items() if 'waterlogged=true' not in k}
    os.makedirs(f'{OA}/blockstates', exist_ok=True)
    json.dump(j, open(f'{OA}/blockstates/{name}.json', 'w'), indent=2)
    def refs(o):
        if isinstance(o, dict):
            if 'model' in o:
                yield o['model']
            for v in o.values():
                yield from refs(v)
        elif isinstance(o, list):
            for v in o:
                yield from refs(v)
    for m in set(refs(j)):
        model(m)


def fix_loot(o):
    if isinstance(o, dict):
        o.pop('random_sequence', None)
        for k, v in list(o.items()):
            if k == 'condition' and v == 'minecraft:any_of':
                o[k] = 'minecraft:alternative'
            fix_loot(v)
    elif isinstance(o, list):
        for v in o:
            fix_loot(v)


def loot(name):
    src = f'{UD}/loot_table/blocks/{name}.json'
    if not os.path.exists(src):
        return
    j = json.load(open(src))
    fix_loot(j)
    text = json.dumps(j)
    # 1.21 match_tool item predicates: tag and item-list strings become the 1.19.2 object form.
    text = text.replace('"items": "#c:tools/shear"', '"tag": "forge:shears"').replace('"items": "#c:tools/shears"', '"tag": "forge:shears"')
    text = text.replace('{"ability": "shears_dig", "condition": "neoforge:can_item_perform_ability"}', '{"condition": "forge:can_tool_perform_action", "action": "shears_dig"}')
    text = text.replace('{"predicates": {"minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}', '{"enchantments": [{"enchantment": "minecraft:silk_touch", "levels": {"min": 1}}]}')
    text = text.replace('{"function": "minecraft:copy_components", "include": ["minecraft:custom_name"], "source": "block_entity"}', '{"function": "minecraft:copy_name", "source": "block_entity"}')
    j = json.loads(text)
    if 'predicates' in text or 'copy_components' in text or '"items": "#' in text:
        print('REVIEW loot (1.21 predicate form)', name)
    os.makedirs(f'{OD}/loot_tables/blocks', exist_ok=True)
    json.dump(j, open(f'{OD}/loot_tables/blocks/{name}.json', 'w'), indent=2)


def main():
    for n in ITEMS + BLOCKS:
        item_model(n)
    for n in BLOCKS + POTS + BLOCK_ONLY:
        blockstate(n)
        loot(n)
    for f in FLUIDS:
        for suf in ('still', 'flow'):
            texture(f'hearthandharvest:block/fluid/{f}_{suf}')
    for e in EFFECTS:
        texture(f'hearthandharvest:mob_effect/{e}')
    up = json.load(open(f'{UA}/lang/en_us.json'))
    p = f'{OA}/lang/en_us.json'
    lang = json.load(open(p))
    wanted = set(ITEMS + BLOCKS + POTS + BLOCK_ONLY)
    for k, v in up.items():
        parts = k.split('.')
        if len(parts) >= 3 and parts[1] == 'hearthandharvest':
            if parts[0] in ('item', 'block') and parts[2] in wanted and len(parts) == 3:
                lang[k] = v
            elif parts[0] == 'effect' and parts[2] in EFFECTS:
                lang[k] = v
            elif parts[0] == 'fluid_type' and parts[2] in FLUIDS:
                lang[k] = v
            elif parts[0] == 'tooltip' and parts[2] == 'cleaver':
                lang[k] = v
    for name, text in RENAMES.items():
        for prefix in ('item', 'block'):
            if f'{prefix}.hearthandharvest.{name}' in lang:
                lang[f'{prefix}.hearthandharvest.{name}'] = text
    lang['item.hearthandharvest.lemon_wine'] = LEMON_WINE_NAME
    lang['fluid_type.hearthandharvest.lemon_wine'] = LEMON_WINE_NAME
    json.dump(lang, open(p, 'w'), indent=2, ensure_ascii=False)
    # The one sound file in use so far (HHSounds).
    copy(f'{UA}/sounds/entities/fart/fart.ogg', f'{OA}/sounds/entities/fart/fart.ogg')
    json.dump({'entities.fart': {'sounds': ['hearthandharvest:entities/fart/fart']}},
              open(f'{OA}/sounds.json', 'w'), indent=2)
    # Blocks' item models: the block items reuse block model parents, already copied above.
    missing = [k for k in wanted if not any(f'{x}.hearthandharvest.{k}' in lang for x in ('item', 'block'))]
    print('lang keys', len(lang), 'no name:', missing)


if __name__ == '__main__':
    main()
