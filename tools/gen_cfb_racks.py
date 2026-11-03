#!/usr/bin/env python3
"""Resources of the Cooking for Blockheads wood racks and wood fruit baskets and the fridge fill (Creative Delight member blockheadsAdditions,
feature_writeup/kitchen.md 9).

Writes into development/creativeDelight/members/blockheads/src/main/resources/:
  - textures: assets/cookingforblockheads/textures/block/rack/<wood>_spice_rack.png and <wood>_tool_rack.png, copied
    from textureswapper/rack/swapped_spicerack/ and textureswapper/rack/toolrack_final/ (the owner's art: crops of
    each wood's planks with the rack's hook pixels);
  - per rack: blockstate, block model (parent = CfB's spice_rack / tool_rack model, only the texture swapped; CfB's
    models are named, never copied), item model, loot table (drops itself, keeps a custom name like CfB's);
  - assets/blockheads_additions/lang/en_us.json (block names; a lang file loads from every namespace, so CfB's own
    lang path is not repeated);
  - assets/blockheads_additions/models/block/fridge_top_fill.json, the box the client adds on top of the 2-tall
    fridge (client/FridgeTopFill.java).

The wood list must match rack/RackWoods.java. Run from anywhere: python3 tools/gen_cfb_racks.py [--check]
(--check: exit 1 if any output would change).
"""
import json
import os
import shutil
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, 'development', 'creativeDelight', 'members', 'blockheads', 'src', 'main', 'resources')
SRC = os.path.join(ROOT, 'textureswapper', 'rack')
NS = 'cookingforblockheads'

# wood key -> (display name, texture file stem in textureswapper/rack/<folder>/)
# immersiveengineering__treated_wood.png is the same picture as _horizontal (and IE has no plain treated_wood
# planks block), so it is not used.
WOODS = {
    'oak': ('Oak', 'minecraft__oak_planks'),
    'spruce': ('Spruce', 'minecraft__spruce_planks'),
    'birch': ('Birch', 'minecraft__birch_planks'),
    'jungle': ('Jungle', 'minecraft__jungle_planks'),
    'acacia': ('Acacia', 'minecraft__acacia_planks'),
    'dark_oak': ('Dark Oak', 'minecraft__dark_oak_planks'),
    'mangrove': ('Mangrove', 'minecraft__mangrove_planks'),
    'crimson': ('Crimson', 'minecraft__crimson_planks'),
    'warped': ('Warped', 'minecraft__warped_planks'),
    'twisted': ('Twisted', 'architects_palette__twisted_planks'),
    'dreamwood': ('Dreamwood', 'botania__dreamwood_planks'),
    'livingwood': ('Livingwood', 'botania__livingwood_planks'),
    'smoked': ('Smoked', 'create_dd__smoked_planks'),
    'treated_wood': ('Treated Wood', 'immersiveengineering__treated_wood_horizontal'),
    'vertical_treated_wood': ('Vertical Treated Wood', 'immersiveengineering__treated_wood_vertical'),
    'rubberwood': ('Rubberwood', 'myrtrees__rubberwood_planks'),
    'ancient': ('Ancient', 'quark__ancient_planks'),
    'bamboo': ('Bamboo', 'quark__bamboo_planks'),
    'driftwood': ('Driftwood', 'upgrade_aquatic__driftwood_planks'),
}

RACKS = {
    # rack id suffix -> (CfB model, art folder, display suffix)
    'spice_rack': ('spice_rack', 'swapped_spicerack', 'Spice Rack'),
    'tool_rack': ('tool_rack', 'toolrack_final', 'Tool Rack'),
}

FILL = {
    'credit': 'Creative Delight (blockheadsAdditions): added on top of the 2-tall Cooking for Blockheads fridge, '
              'client/FridgeTopFill.java (TODO cooking/kitchen #6)',
    'textures': {
        'particle': 'cookingforblockheads:block/fridge_side',
        'fridge_side': 'cookingforblockheads:block/fridge_side',
    },
    'elements': [{
        'name': 'TopFill',
        'from': [1, 13, 1],
        'to': [15, 16, 15],
        'faces': {
            **{d: {'uv': [1, 1, 15, 4], 'texture': '#fridge_side', 'tintindex': 0}
               for d in ('north', 'east', 'south', 'west')},
            'up': {'uv': [1, 1, 15, 15], 'texture': '#fridge_side', 'tintindex': 0, 'cullface': 'up'},
            'down': {'uv': [1, 1, 15, 15], 'texture': '#fridge_side', 'tintindex': 0},
        },
    }],
}


def dump(obj):
    return json.dumps(obj, indent=2) + '\n'


def outputs():
    """Yield (relative path, text or source file path)."""
    lang = {}
    for wood, (name, stem) in WOODS.items():
        # fruit basket: CfB's model with its oak planks texture swapped for this wood's planks (plain planks art)
        fid = f'{wood}_fruit_basket'
        ftex = f'{NS}:block/fruit_basket/{wood}'
        yield f'assets/{NS}/textures/block/fruit_basket/{wood}.png', ('file', os.path.join(SRC, stem + '.png'))
        yield f'assets/{NS}/blockstates/{fid}.json', dump({'variants': {'': {'model': f'{NS}:block/{fid}'}}})
        yield f'assets/{NS}/models/block/{fid}.json', dump({
            'parent': f'{NS}:block/fruit_basket', 'textures': {'particle': ftex, 'texture': ftex}})
        yield f'assets/{NS}/models/item/{fid}.json', dump({'parent': f'{NS}:block/{fid}'})
        yield f'data/{NS}/loot_tables/blocks/{fid}.json', dump({
            'type': 'minecraft:block',
            'pools': [{
                'rolls': 1,
                'entries': [{'type': 'minecraft:item', 'name': f'{NS}:{fid}',
                             'functions': [{'function': 'minecraft:copy_name', 'source': 'block_entity'}]}],
                'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
        lang[f'block.{NS}.{fid}'] = f'{name} Fruit Basket'
        for rack, (cfb_model, folder, label) in RACKS.items():
            rid = f'{wood}_{rack}'
            tex = f'{NS}:block/rack/{rid}'
            yield f'assets/{NS}/textures/block/rack/{rid}.png', ('file', os.path.join(SRC, folder, stem + '.png'))
            yield f'assets/{NS}/blockstates/{rid}.json', dump({'variants': {
                f'facing={f}': ({'model': f'{NS}:block/{rid}'} | ({'y': y} if y else {}))
                for f, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270))}})
            yield f'assets/{NS}/models/block/{rid}.json', dump({
                'parent': f'{NS}:block/{cfb_model}',
                'render_type': 'minecraft:cutout',
                'textures': {'particle': tex, 'texture': tex}})
            yield f'assets/{NS}/models/item/{rid}.json', dump({'parent': f'{NS}:block/{rid}'})
            yield f'data/{NS}/loot_tables/blocks/{rid}.json', dump({
                'type': 'minecraft:block',
                'pools': [{
                    'rolls': 1,
                    'entries': [{'type': 'minecraft:item', 'name': f'{NS}:{rid}',
                                 'functions': [{'function': 'minecraft:copy_name', 'source': 'block_entity'}]}],
                    'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
            lang[f'block.{NS}.{rid}'] = f'{name} {label}'
    yield 'assets/blockheads_additions/lang/en_us.json', dump(lang)
    yield 'assets/blockheads_additions/models/block/fridge_top_fill.json', dump(FILL)


def main():
    check = '--check' in sys.argv
    changed = []
    for rel, content in outputs():
        path = os.path.join(RES, rel)
        if isinstance(content, tuple):
            src = content[1]
            if not os.path.isfile(src):
                sys.exit(f'missing art: {src}')
            same = os.path.isfile(path) and open(src, 'rb').read() == open(path, 'rb').read()
            if not same:
                changed.append(rel)
                if not check:
                    os.makedirs(os.path.dirname(path), exist_ok=True)
                    shutil.copyfile(src, path)
        else:
            same = os.path.isfile(path) and open(path, encoding='utf-8').read() == content
            if not same:
                changed.append(rel)
                if not check:
                    os.makedirs(os.path.dirname(path), exist_ok=True)
                    with open(path, 'w', encoding='utf-8') as f:
                        f.write(content)
    print(f'{len(changed)} file(s) {"would change" if check else "written"}')
    if check and changed:
        for c in changed[:20]:
            print('  ' + c)
        sys.exit(1)


if __name__ == '__main__':
    main()
