#!/usr/bin/env python3
"""Copy Fungi Delight 1.3's assets and data into the port and convert them to 1.19.2 Forge formats.

    python3 tools/port_resources.py            (run from anywhere; rewrites src/main/resources/{assets,data})

Source: the upstream jar (MIT), read directly. Adapted from development/creativeDelight/members/veggies/tools/port_resources.py.
What changes on the way (feature_writeup/fungi_delight.md 4 step 1 and 5.1):

  * left out: advancements (fungidelight and the vanilla suspicious stew override); biome modifiers and placed
    features (the pack places everything itself); the biome tags only those used; the Botany Pots recipes; tags of
    mods not in the pack (Autumnity, Spawn); 1.20-only block tags (enchantment_power_transmitter, replaceable,
    replaceable_by_trees, sword_efficient) and sniffer_food; forge:alcohol (Weak Stomach reads the pack's
    creativedelight:food_type/drink/alcohol instead)
  * kept: the 8 configured features under their upstream ids (the pack's placed features reference them)
  * patch_inky_cap_mushroom reads #fungidelight:inky_cap_placeable_on (upstream read the morel tag)
  * the three suspicious stew recipes become fungidelight:effect_stew recipes that name the effect by registry id
    (upstream stored numeric EffectIds 15 / 23 / 35, which are registry slots)
  * recipes lose the 1.20 'category' field; loot tables lose 'random_sequence'; the 1.20 loot condition 'any_of'
    becomes 1.19.2's 'alternative'; lang gets the 1.19.2 creative tab key
At the end it lists every item id and tag a recipe names that the pack's last dumps do not know (kubejs/exported).
"""
import json
import os
import re
import shutil
import sys
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
MOD = os.path.dirname(HERE)
INSTANCE = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(MOD))))
JAR = os.path.expanduser('~/.local/share/atlauncher/downloads/FungiDelight-1.20.1-1.3.jar')
OUT = os.path.join(MOD, 'src', 'main', 'resources')

# Suspicious stews (fungi_delight.md 5.1.5): the effect follows each mushroom's dish; 160 ticks as upstream.
STEW_EFFECTS = {
    'suspicious_stew_from_inky_cap_mushroom': 'minecraft:luck',
    'suspicious_stew_from_morel_mushroom': 'farmersdelight:comfort',
    'suspicious_stew_from_truffle': 'minecraft:haste',
}
STEW_DURATION = 160

SKIP_PREFIXES = (
    'META-INF/', 'net/', '.cache/', 'pack.mcmeta', 'fungidelight.mixins.json', 'fungidelight.refmap.json',
    'data/fungidelight/advancements/', 'data/minecraft/advancements/',
    'data/fungidelight/forge/biome_modifier/',
    'data/fungidelight/worldgen/placed_feature/',
    'data/fungidelight/tags/worldgen/',
    'data/fungidelight/recipes/integration/botany/',
    'data/autumnity/', 'data/spawn/',
)
SKIP_FILES = {
    'data/minecraft/tags/blocks/enchantment_power_transmitter.json',
    'data/minecraft/tags/blocks/replaceable.json',
    'data/minecraft/tags/blocks/replaceable_by_trees.json',
    'data/minecraft/tags/blocks/sword_efficient.json',
    'data/minecraft/tags/items/sniffer_food.json',
    'data/forge/tags/items/alcohol.json',
}


def skipped(path):
    return path.endswith('/') or path.startswith(SKIP_PREFIXES) or path in SKIP_FILES


def convert_recipe(name, d):
    d.pop('category', None)
    if name in STEW_EFFECTS:
        return {
            'type': 'fungidelight:effect_stew',
            'ingredients': d['ingredients'],
            'result': {'item': 'minecraft:suspicious_stew'},
            'effect': STEW_EFFECTS[name],
            'duration': STEW_DURATION,
        }
    if d.get('type') == 'farmersdelight:cooking':
        d.setdefault('cookingtime', 200)
    return d


def convert_loot(d):
    d.pop('random_sequence', None)
    text = json.dumps(d).replace('"minecraft:any_of"', '"minecraft:alternative"')
    return json.loads(text)


def convert_configured_feature(name, d):
    if name == 'patch_inky_cap_mushroom':
        text = json.dumps(d).replace('"fungidelight:morel_placeable_on"', '"fungidelight:inky_cap_placeable_on"')
        d = json.loads(text)
    return d


def convert_lang(d):
    out = {k: v for k, v in d.items() if '.advancement.' not in k and not k.startswith('advancement')}
    if 'creativetab.fungidelight_tab' in out:
        out['itemGroup.fungidelight_tab'] = out.pop('creativetab.fungidelight_tab')
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
    java = open(os.path.join(MOD, 'src/main/java/net/mehdinoui/fungidelight/common/registry/ModItems.java')).read()
    own_items = {'fungidelight:' + m for m in re.findall(r'ITEMS\.register\(\s*"([a-z_]+)"', java)}
    own_tags = set()
    tag_root = os.path.join(OUT, 'data')
    for root, _, files in os.walk(tag_root):
        if '/tags/items' in root:
            for f in files:
                ns = os.path.relpath(root, tag_root).split(os.sep)[0]
                sub = os.path.relpath(os.path.join(root, f), os.path.join(tag_root, ns, 'tags', 'items'))
                own_tags.add(f'{ns}:{sub[:-5]}')
    missing = []
    recipes = os.path.join(OUT, 'data/fungidelight/recipes')
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
            if not path.endswith('.json'):
                out = os.path.join(OUT, path)
                os.makedirs(os.path.dirname(out), exist_ok=True)
                open(out, 'wb').write(raw)
            elif '/recipes/' in path:
                write_json(path, convert_recipe(name, json.loads(raw)))
            elif '/loot_tables/' in path:
                write_json(path, convert_loot(json.loads(raw)))
            elif '/configured_feature/' in path:
                write_json(path, convert_configured_feature(name, json.loads(raw)))
            elif '/lang/' in path:
                write_json(path, convert_lang(json.loads(raw)))
            else:
                write_json(path, json.loads(raw))
            counts[kind] = counts.get(kind, 0) + 1
    print('written:', ', '.join(f'{k} {v}' for k, v in sorted(counts.items())))
    leftovers = []
    for root, _, files in os.walk(os.path.join(OUT, 'data')):
        for f in files:
            text = open(os.path.join(root, f)).read() if f.endswith('.json') else ''
            if '"category"' in text or 'random_sequence' in text or 'EffectId' in text or 'any_of' in text:
                leftovers.append(os.path.relpath(os.path.join(root, f), OUT))
    if leftovers:
        print('STILL HAS A 1.20 FIELD:', *leftovers, sep='\n  ')
    missing = check_refs()
    if missing:
        print('UNKNOWN to the pack dumps (dead recipe inputs or outputs):', *missing, sep='\n  ')
    return 1 if leftovers else 0


if __name__ == '__main__':
    sys.exit(main())
