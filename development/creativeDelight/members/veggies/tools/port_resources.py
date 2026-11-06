#!/usr/bin/env python3
"""Copy Veggies Delight 1.9.2's assets and data into the port and convert them to 1.19.2 Forge formats.

    python3 tools/port_resources.py            (run from anywhere; rewrites src/main/resources/{assets,data})

Source: the upstream jar (MIT), read directly. What changes on the way (feature_writeup/veggies_delight.md 4.1):

  * left out: the bell pepper line (item, seeds, crop, crate, wild patch, smoked pepper) and every file named after
    it; advancements; the chest-loot GLMs and their 9 tables; biome modifiers (the pack places wild plants through
    wild_crops.json / wild_flowers.json); compat village houses; recipes for mods not in the pack (Cultural,
    Ube's, Botany Pots) and the FD compat vegetable soup; tags for mods not in the pack (Diet, Naturalist, Tinkers')
  * kept: the 5 vanilla depot houses and the 5 wagon chest tables their chests use; the pastry-slicing modifiers
  * recipe inputs: VD bell pepper -> #forge:crops/bell_pepper (Rustic); smoked bell pepper -> any Rustic roasted pepper
  * wraps: only the Corn Delight tortilla recipes (the dough ones are dropped); fermented garlic honey only in the
    Vintage Delight port's Pickling Jar (the 22 compat/vintagedelight recipes ship, vintage_delight.md 3.4)
  * placed features: VD's config-driven rarity -> vanilla rarity_filter; wild turnips grow on sand or dirt (the pack's
    sea beet rule); no mature dandelion feature (wild_flowers.json places it)
  * mature dandelion drops: always 1 dandelion and 0 to 3 dandelion leaves; shears give the block
  * structures: DataVersion set to 1.19.2's; the desert house's VD bell pepper crate becomes Rustic's red one
  * recipes and loot tables lose the 1.20 'category' and 'random_sequence' fields; lang drops removed keys and gets the
    1.19.2 creative tab key
  * from upstream 1.9.3: carrot cake slices 1 to 6 show carrot_cake_inner on the cut face (1.9.2 showed the top)
At the end it lists every item id and tag a recipe names that the pack's last dumps do not know (kubejs/exported).
"""
import gzip
import json
import os
import re
import shutil
import sys
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
MOD = os.path.dirname(HERE)
INSTANCE = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(MOD))))
JAR = os.path.expanduser('~/.local/share/atlauncher/downloads/VeggiesDelight-1.20.1-1.9.2.jar')
OUT = os.path.join(MOD, 'src', 'main', 'resources')

DATA_VERSION_1192 = 3120
RUSTIC_ROASTED = [{'item': f'rusticdelight:roasted_bell_pepper_{c}'} for c in ('red', 'green', 'yellow')]
PEPPER_TAG = {'tag': 'forge:crops/bell_pepper'}

# Wild patch rarity, one patch in N chunks (veggies_delight.md 4.3)
RARITY = {
    'patch_wild_broccoli': 240, 'patch_wild_cauliflowers': 240, 'patch_wild_garlic': 240,
    'patch_wild_sweet_potatoes': 100, 'patch_wild_zucchinis': 100, 'patch_wild_turnips': 30,
}

SKIP_PREFIXES = (
    'META-INF/', 'net/', '.cache/', 'pack.mcmeta',
    'data/veggiesdelight/advancements/',
    'data/veggiesdelight/forge/biome_modifier/',
    'data/veggiesdelight/structures/compat/',
    'data/veggiesdelight/recipes/compat/culturaldelight/',
    'data/veggiesdelight/recipes/compat/ubesdelight/',
    'data/veggiesdelight/recipes/compat/farmersdelight/',
    'data/veggiesdelight/recipes/integration/botany/',
    'data/diet/', 'data/naturalist/', 'data/tconstruct/',
)
SKIP_FILES = {
    # dough wraps: the Corn Delight tortilla versions are the only ones
    'data/veggiesdelight/recipes/chicken_fajitas_wrap.json',
    'data/veggiesdelight/recipes/vegetable_wrap.json',
    # by hand upstream; the pack makes it in Vintage's Pickling Jar (compat/vintagedelight/fermenting/fermented_garlic.json)
    'data/veggiesdelight/recipes/fermented_garlic_honey.json',
    # placed by wild_flowers.json
    'data/veggiesdelight/worldgen/configured_feature/patch_mature_dandelion.json',
    'data/veggiesdelight/worldgen/placed_feature/patch_mature_dandelion.json',
}


def is_pepper(path):
    """Files of the dropped bell pepper line; the stuffed bell peppers (feast and serving) are kept."""
    base = path.rsplit('/', 1)[-1]
    return 'bellpepper' in base and not base.startswith('stuffed_bellpepper')


def skipped(path):
    if path.endswith('/') or path.startswith(SKIP_PREFIXES) or path in SKIP_FILES or is_pepper(path):
        return True
    if path.startswith('data/veggiesdelight/loot_modifiers/add_loot_'):
        return True
    if path.startswith('data/veggiesdelight/loot_tables/chests/') and '/vd_wagon_' not in path:
        return True
    return False


# Upstream mistakes and names from newer mod versions, fixed on the way
INGREDIENT_FIXES = {
    ('item', 'fveggiesdelight:wild_broccolis'): ('item', 'veggiesdelight:wild_broccoli'),  # IE crusher typo
}


def map_ingredient(ing):
    """Returns a list of ingredient objects (a smoked pepper expands to the three roasted Rustic peppers)."""
    if isinstance(ing, list):
        out = []
        for i in ing:
            out.extend(map_ingredient(i))
        return out
    if ing.get('item') == 'veggiesdelight:bellpepper' or ing.get('tag') in ('forge:crops/bellpepper', 'forge:vegetables/bellpepper'):
        return [dict(PEPPER_TAG)]
    for (kind, old), (new_kind, new) in INGREDIENT_FIXES.items():
        if ing.get(kind) == old:
            return [{new_kind: new}]
    if ing.get('item') == 'veggiesdelight:smoked_bellpepper':
        return [dict(i) for i in RUSTIC_ROASTED]
    return [ing]


def one(ing):
    out = map_ingredient(ing)
    return out[0] if len(out) == 1 else out


def convert_recipe(d):
    d.pop('category', None)
    if 'ingredients' in d:
        d['ingredients'] = [one(i) for i in d['ingredients']]
    for key in ('ingredient', 'input'):
        if isinstance(d.get(key), (dict, list)):
            d[key] = one(d[key])
    if 'key' in d:
        d['key'] = {k: one(v) for k, v in d['key'].items()}
    if d.get('type') == 'farmersdelight:cooking':
        d.setdefault('cookingtime', 200)
    return d


def convert_tag(d):
    def keep(v):
        name = v if isinstance(v, str) else v.get('id', '')
        return not is_pepper(name.split(':')[-1])
    d['values'] = [v for v in d['values'] if keep(v)]
    return d


LOOT_SWAP = {
    'veggiesdelight:bellpepper': 'rusticdelight:bell_pepper_red',
    'veggiesdelight:bellpepper_seeds': 'rusticdelight:bell_pepper_seeds',
    'veggiesdelight:smoked_bellpepper': 'rusticdelight:roasted_bell_pepper_red',
    'veggiesdelight:bellpepper_crate': 'rusticdelight:bell_pepper_red_crate',
}


def convert_loot(d):
    d.pop('random_sequence', None)
    text = json.dumps(d)
    for old, new in LOOT_SWAP.items():
        text = text.replace(f'"{old}"', f'"{new}"')
    return json.loads(text)


def mature_dandelion_loot():
    shears = {'condition': 'minecraft:match_tool', 'predicate': {'tag': 'forge:shears'}}
    not_shears = {'condition': 'minecraft:inverted', 'term': shears}
    return {
        'type': 'minecraft:block',
        'pools': [
            {'rolls': 1, 'entries': [{'type': 'minecraft:alternatives', 'children': [
                {'type': 'minecraft:item', 'name': 'veggiesdelight:mature_dandelion', 'conditions': [shears]},
                {'type': 'minecraft:item', 'name': 'minecraft:dandelion'},
            ]}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]},
            {'rolls': 1, 'conditions': [not_shears], 'entries': [{
                'type': 'minecraft:item', 'name': 'veggiesdelight:dandelion_leaf',
                'functions': [{'function': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': 0, 'max': 3}},
                              {'function': 'minecraft:explosion_decay'}]}]},
        ],
    }


def convert_configured_feature(name, d):
    if name == 'patch_wild_turnips':
        # sea beet rule: sand or dirt below (beaches and cliff tops)
        pred = d['config']['primary_feature']['placement'][0]['predicate']['predicates']
        pred[1] = {'type': 'minecraft:any_of', 'predicates': [
            {'type': 'minecraft:matching_block_tag', 'offset': [0, -1, 0], 'tag': 'minecraft:sand'},
            {'type': 'minecraft:matching_block_tag', 'offset': [0, -1, 0], 'tag': 'minecraft:dirt'}]}
    return d


def convert_placed_feature(name, d):
    d['placement'] = [{'type': 'minecraft:rarity_filter', 'chance': RARITY[name]}
                      if p.get('type') == 'veggiesdelight:configurable_rarity_filter' else p for p in d['placement']]
    return d


def nbt_string(s):
    b = s.encode('utf-8')
    return len(b).to_bytes(2, 'big') + b


def convert_structure(raw):
    data = gzip.decompress(raw)
    for old, new in LOOT_SWAP.items():
        data = data.replace(nbt_string(old), nbt_string(new))
    key = b'\x03' + nbt_string('DataVersion')
    i = data.find(key)
    if i >= 0:
        j = i + len(key)
        data = data[:j] + DATA_VERSION_1192.to_bytes(4, 'big') + data[j + 4:]
    return gzip.compress(data, mtime=0)


def convert_lang(d):
    out = {}
    for k, v in d.items():
        if '.advancement.' in k or k.startswith('advancement') or is_pepper(k.split('.')[-1]):
            continue
        out[k] = v
    if 'creativetab.veggiesdelight_tab' in out:
        out['itemGroup.veggiesdelight_tab'] = out.pop('creativetab.veggiesdelight_tab')
    return out


def write_json(rel, d):
    path = os.path.join(OUT, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w') as f:
        json.dump(d, f, indent=2)
        f.write('\n')


def check_refs():
    """Item ids and tags named in recipes that neither the pack's dumps nor this port provide."""
    entries = json.load(open(os.path.join(INSTANCE, 'kubejs/exported/display_names.json')))['entries']
    names = {e['id'] for group in entries.values() for e in group}
    tags = set(json.load(open(os.path.join(INSTANCE, 'kubejs/exported/tags/item_tags.json'))))
    own_items = set()
    for line in open(os.path.join(MOD, 'src/main/java/net/mehdinoui/veggiesdelight/common/registry/ModItems.java')):
        m = re.search(r'ITEMS\.register\("([a-z_]+)"', line)
        if m:
            own_items.add('veggiesdelight:' + m.group(1))
    own_tags = set()
    tag_root = os.path.join(OUT, 'data')
    for root, _, files in os.walk(tag_root):
        if '/tags/items' in root:
            for f in files:
                ns = os.path.relpath(root, tag_root).split(os.sep)[0]
                sub = os.path.relpath(os.path.join(root, f), os.path.join(tag_root, ns, 'tags', 'items'))
                own_tags.add(f'{ns}:{sub[:-5]}')
    missing = []
    recipes = os.path.join(OUT, 'data/veggiesdelight/recipes')
    for root, _, files in os.walk(recipes):
        for f in files:
            rel = os.path.relpath(os.path.join(root, f), recipes)
            text = open(os.path.join(root, f)).read()
            for kind, ref in re.findall(r'"(item|tag)":\s*"([^"]+)"', text):
                known = (ref in names or ref in own_items) if kind == 'item' else (ref in tags or ref in own_tags)
                if not known:
                    missing.append(f'{rel}: {kind} {ref}')
    return missing


def main():
    for sub in ('assets', 'data'):
        shutil.rmtree(os.path.join(OUT, sub), ignore_errors=True)
    counts = {}
    with zipfile.ZipFile(JAR) as jar:
        for path in jar.namelist():
            if skipped(path):
                continue
            raw = jar.read(path)
            kind = path.split('/')[2] if path.startswith('data/') and path.count('/') > 2 else path.split('/')[0]
            name = path.rsplit('/', 1)[-1][:-5]
            if path.startswith('data/forge/loot_modifiers/'):
                d = json.loads(raw)
                d['entries'] = [e for e in d['entries'] if ':slicing_' in e]
                write_json(path, d)
            elif path.endswith('.nbt'):
                out = os.path.join(OUT, path)
                os.makedirs(os.path.dirname(out), exist_ok=True)
                open(out, 'wb').write(convert_structure(raw))
            elif not path.endswith('.json'):
                out = os.path.join(OUT, path)
                os.makedirs(os.path.dirname(out), exist_ok=True)
                open(out, 'wb').write(raw)
            elif '/recipes/' in path:
                write_json(path, convert_recipe(json.loads(raw)))
            elif '/tags/' in path:
                write_json(path, convert_tag(json.loads(raw)))
            elif '/loot_tables/' in path or '/loot_modifiers/' in path:
                d = mature_dandelion_loot() if path.endswith('blocks/mature_dandelion.json') else convert_loot(json.loads(raw))
                write_json(path, d)
            elif '/configured_feature/' in path:
                write_json(path, convert_configured_feature(name, json.loads(raw)))
            elif '/placed_feature/' in path:
                write_json(path, convert_placed_feature(name, json.loads(raw)))
            elif '/lang/' in path:
                write_json(path, convert_lang(json.loads(raw)))
            elif re.search(r'/models/block/carrot_cake_slice[1-6]\.json$', path):
                d = json.loads(raw)
                d['textures']['inside'] = 'veggiesdelight:block/carrot_cake_inner'
                write_json(path, d)
            else:
                write_json(path, json.loads(raw))
            counts[kind] = counts.get(kind, 0) + 1
    print('written:', ', '.join(f'{k} {v}' for k, v in sorted(counts.items())))
    leftovers = []
    for root, _, files in os.walk(OUT):
        for f in files:
            if f.endswith('.json') and os.path.join(root, f).find('/lang/') < 0:
                if re.search(r'veggiesdelight:(smoked_)?bellpepper(_seeds|_crate|_crop)?"', open(os.path.join(root, f)).read()):
                    leftovers.append(os.path.relpath(os.path.join(root, f), OUT))
    if leftovers:
        print('STILL NAMES A DROPPED PEPPER:', *leftovers, sep='\n  ')
    missing = check_refs()
    if missing:
        print('UNKNOWN to the pack dumps (dead recipe inputs or outputs):', *missing, sep='\n  ')
    return 1 if leftovers else 0


if __name__ == '__main__':
    sys.exit(main())
