#!/usr/bin/env python3
"""Copy Vintage Delight 0.1.6's assets and data into the port and convert them to 1.19.2 Forge formats.

    python3 tools/port_resources.py            (run from anywhere; rewrites src/main/resources/{assets,data})

Sources: the upstream 0.1.6 jar (MIT), read directly, plus three files from the KOTS 0.3.1 jar (GPL-3.0): the
Cucumber Dough texture and model (its recipes are written below). Plan: feature_writeup/vintage_delight.md.

  * purged: every file and entry naming the gearo berry, the Dehydrated effect (lang, effect icon) or the pitcher
    plant (S1, S3, S8); the 1.20-only sniffer_diggable_block tag; the Galosphere / Spelunkery recipes; the Diet tags
  * not shipped: the biome modifiers of the four wild plants (the pack places them; salt_patch stays); the seven
    chest-loot GLMs and their global_loot_modifiers entries (the tables ship, wired to nothing; the straw modifier
    stays); the chests/mixed_seeds table loses its 1.20 armor trim pool
  * KOTS jar ids: the separate mason_jar / *_mason_jar items are gone; the jar item is the block's own item, so every
    reference is renamed to the block id (vintagedelight:mason_jar -> empty_mason_jar, relish_mason_jar -> relish_jar,
    sweet_berry_mason_jar -> sweet_berry_jam_jar, ...)
  * KOTS Cucumber Dough: cucumber + #forge:dough -> 2 dough; the knife cuts the dough (not the cucumber) into noodles
  * recipes: century egg takes #forge:eggs (was the sniffer egg); Goblin's Piss takes a fermented spider eye (was a
    gearo berry); no 1.20 'category' / 'show_notification'
  * upstream id bugs fixed: Frost Fermenta's minecraft:rotten_tomato -> farmersdelight:rotten_tomato; the
    pickled_ghost_pepper potato cannon type names vintagedelight:pickled_pepper
  * worldgen: wild cucumbers stand on #minecraft:dirt (was dirt, grass, coarse dirt, sand, red sand); the magic vine
    tree uses KOTS's switchable vintagedelight:configurable_alter_ground decorator and 1.19.2 leaf states
  * Moonlight soft fluids: 1.19.2 texture ids (no 'block/'), KOTS jar ids, no gearo jam
  * advancements: 1.20 placed_block location lists -> the 1.19.2 'block' field; purged items left out
  * loot tables lose 'random_sequence'; lang: purged keys out, jar item keys moved to the block keys, the fermenting
    jar is named Pickling Jar (S7), 1.19.2 creative tab key, KOTS keys (cucumber dough, ghost pepper soil tooltip)
  * new: block tag #vintagedelight:ghost_pepper_soil (soul soil; the pack adds the rest)
At the end it lists every item id and tag a recipe, loot table or tag names that the pack's last dumps do not know
(kubejs/exported), and fails if a purged name is left anywhere.
"""
import json
import os
import re
import shutil
import sys
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
MOD = os.path.dirname(HERE)
INSTANCE = os.path.dirname(os.path.dirname(MOD))
JAR = os.path.expanduser('~/.local/share/atlauncher/downloads/vintagedelight-0.1.6.jar')
KOTS = os.path.expanduser('~/.local/share/atlauncher/downloads/vintagedelight-kots-0.3.1.jar')
OUT = os.path.join(MOD, 'src', 'main', 'resources')
NS = 'vintagedelight'

PURGED = re.compile(r'gearo|dehydrat|pitcher|sniffer', re.I)

# KOTS jar rename: upstream item id -> the jar block's id (the one item per jar)
JAR_IDS = {
    'mason_jar': 'empty_mason_jar',
    'relish_mason_jar': 'relish_jar',
    'apple_sauce_mason_jar': 'apple_sauce_jar',
    'honey_mason_jar': 'honey_jar',
    'sweet_berry_mason_jar': 'sweet_berry_jam_jar',
    'glow_berry_mason_jar': 'glow_berry_jam_jar',
    'pepper_jam_mason_jar': 'pepper_jam_jar',
    'nut_mash_mason_jar': 'nut_mash_jar',
    'vinegar_mason_jar': 'vinegar_jar',
}

SKIP_PREFIXES = (
    'META-INF/', 'net/', '.cache/', 'pack.mcmeta', 'logo.png', 'vintagedelight.',
    'data/diet/',
    'data/vintagedelight/recipes/integration/',  # Galosphere and Spelunkery only
    'assets/vintagedelight/textures/mob_effect/',  # the Dehydrated icon
)
SKIP_FILES = {
    'data/minecraft/tags/blocks/sniffer_diggable_block.json',  # 1.20 tag
    'data/vintagedelight/recipes/cutting/cucumber_cutting.json',  # KOTS: the dough is cut, not the cucumber
    # the empty jar's sprite is under empty_mason_jar already
    'assets/vintagedelight/models/item/mason_jar.json',
    # upstream leftovers that name nothing registered: a typo'd model, a stray blockstate, models of unshipped ores
    'assets/vintagedelight/models/item/vinega_jar.json',
    'assets/vintagedelight/blockstates/nut_mash_mason_jar.json',
    'assets/vintagedelight/models/item/salt_ore.json',
    'assets/vintagedelight/models/item/deepslate_salt_ore.json',
}
KEEP_MODIFIERS = {'salt_patch'}


def skipped(path):
    base = path.rsplit('/', 1)[-1]
    if path.endswith('/') or path.startswith(SKIP_PREFIXES) or path in SKIP_FILES:
        return True
    if PURGED.search(base):
        return True
    if path.startswith('data/vintagedelight/forge/biome_modifier/') and base[:-5] not in KEEP_MODIFIERS:
        return True
    if path.startswith('data/vintagedelight/loot_modifiers/add_loot_'):
        return True
    return False


def out_path(path):
    """Item models of the old *_mason_jar items become the jar block item's model (same 2D sprite)."""
    if path.startswith('assets/vintagedelight/models/item/'):
        base = path.rsplit('/', 1)[-1][:-5]
        if base in JAR_IDS and base != 'mason_jar':
            return f'assets/vintagedelight/models/item/{JAR_IDS[base]}.json'
    return path


def rename_ids(text):
    for old, new in JAR_IDS.items():
        text = text.replace(f'"{NS}:{old}"', f'"{NS}:{new}"')
    return text


def drop_purged(value):
    """Removes list entries (and dict values) that name a purged thing; returns None if the whole value goes."""
    if isinstance(value, str):
        return None if PURGED.search(value) else value
    if isinstance(value, list):
        out = []
        for v in value:
            r = drop_purged(v)
            if r is not None:
                out.append(r)
        return out
    if isinstance(value, dict):
        if any(isinstance(v, str) and PURGED.search(v) for v in value.values()):
            return None
        out = {}
        for k, v in value.items():
            r = drop_purged(v)
            if r is None:
                return None
            out[k] = r
        return out
    return value


def convert_recipe(name, d):
    d.pop('category', None)
    d.pop('show_notification', None)
    if name == 'fermenting/century_egg_from_fermenting':
        d['ingredients'] = [{'tag': 'forge:eggs'} if i.get('item') == 'minecraft:sniffer_egg' else i for i in d['ingredients']]
    if name == 'fermenting/goblins_piss':
        d['ingredients'] = [{'item': 'minecraft:fermented_spider_eye'} if i.get('item') == f'{NS}:gearo_berry' else i
                            for i in d['ingredients']]
    if name == 'fermenting/frost_fermenta':
        # upstream names minecraft:rotten_tomato, which does not exist; FD's rotten tomato is meant
        d['ingredients'] = [{'item': 'farmersdelight:rotten_tomato'} if i.get('item') == 'minecraft:rotten_tomato' else i
                            for i in d['ingredients']]
    if d.get('type') == 'farmersdelight:cooking':
        d.setdefault('cookingtime', 200)
    return d


def convert_tag(d):
    d['values'] = [v for v in d['values'] if not PURGED.search(v if isinstance(v, str) else v.get('id', ''))]
    # upstream's #forge:protein names #forge:protein/peanut, which nothing defines; a missing required reference would
    # fail the whole forge:protein tag for every mod, so the reference is optional
    d['values'] = [{'id': v, 'required': False} if v == '#forge:protein/peanut' else v for v in d['values']]
    return d if d['values'] else None


def convert_loot(name, d):
    d.pop('random_sequence', None)
    if name == 'chests/mixed_seeds':
        # the second pool is a single 1.20 eye armor trim template
        d['pools'] = [p for p in d['pools'] if 'armor_trim' not in json.dumps(p)]
    return d


def convert_advancement(d):
    d.pop('sends_telemetry_event', None)
    criteria = {}
    for key, c in d['criteria'].items():
        if PURGED.search(json.dumps(c)):
            continue
        cond = c.get('conditions', {})
        if c.get('trigger') == 'minecraft:placed_block' and isinstance(cond.get('location'), list):
            blocks = [x['block'] for x in cond['location'] if x.get('condition') == 'minecraft:block_state_property']
            c['conditions'] = {'block': blocks[0]}
        if cond.get('items') == [{'tag': f'{NS}:jam_jars'}]:  # upstream names a tag that does not exist
            cond['items'] = [{'tag': 'forge:jam_jars'}]
        criteria[key] = c
    d['criteria'] = criteria
    if 'requirements' in d:
        d['requirements'] = [[k for k in r if k in criteria] for r in d['requirements']]
        d['requirements'] = [r for r in d['requirements'] if r]
    icon = d.get('display', {}).get('icon', {})
    if PURGED.search(icon.get('item', '')):
        icon['item'] = f'{NS}:sweet_berry_jam_jar'
    return d


def convert_configured_feature(name, d):
    if name == 'patch_wild_cucumbers':
        for part in ('primary_feature', 'secondary_feature'):
            preds = d['config'][part]['placement'][0]['predicate']['predicates']
            d['config'][part]['placement'][0]['predicate']['predicates'] = [
                {'type': 'minecraft:matching_block_tag', 'tag': 'minecraft:dirt', 'offset': [0, -1, 0]}
                if p.get('offset') == [0, -1, 0] else p for p in preds]
    if name == 'magic_vine_tree':
        # 1.19.2 leaves have no waterlogged property
        d['config']['foliage_provider']['state'].get('Properties', {}).pop('waterlogged', None)
        for dec in d['config']['decorators']:
            if dec['type'] == 'minecraft:alter_ground':
                dec['type'] = f'{NS}:configurable_alter_ground'
    return d


def convert_soft_fluid(d):
    for key in ('still_texture', 'flowing_texture'):
        if key in d:
            d[key] = d[key].replace('moonlight:block/soft_fluids/', 'moonlight:soft_fluids/')
    return d


def load_lang(raw):
    text = raw.decode('utf-8')
    text = '\n'.join(line for line in text.splitlines() if not line.strip().startswith('//'))
    text = re.sub(r',(\s*})', r'\1', text)
    return json.loads(text)


def convert_lang(d, english):
    out = {}
    for k, v in d.items():
        if PURGED.search(k) or k.startswith('item.minecraft.'):
            continue
        m = re.fullmatch(r'item\.vintagedelight\.([a-z_]+)', k)
        if m and m.group(1) in JAR_IDS:
            if m.group(1) == 'mason_jar':
                continue  # block.vintagedelight.empty_mason_jar names the empty jar
            k = f'block.{NS}.{JAR_IDS[m.group(1)]}'
        if english:
            v = v.replace('Fermenting Jar', 'Pickling Jar')
        out[k] = v
    if 'creativetab.vintage_tab' in out:
        out[f'itemGroup.{NS}_tab'] = out.pop('creativetab.vintage_tab')
    if english:
        out[f'item.{NS}.cucumber_dough'] = 'Cucumber Dough'
        out[f'item.{NS}.ghost_pepper.planting_soil'] = 'Plant on Soul Soil, Rich Soul Soil or Nulch'
    return out


def write_json(rel, d):
    path = os.path.join(OUT, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(d, f, indent=2, ensure_ascii=False)
        f.write('\n')


def kots_files():
    with zipfile.ZipFile(KOTS) as jar:
        for rel in ('assets/vintagedelight/textures/item/cucumber_dough.png', 'assets/vintagedelight/models/item/cucumber_dough.json'):
            out = os.path.join(OUT, rel)
            os.makedirs(os.path.dirname(out), exist_ok=True)
            open(out, 'wb').write(jar.read(rel))
    write_json(f'data/{NS}/recipes/cucumber_dough.json', {
        'type': 'minecraft:crafting_shapeless',
        'ingredients': [{'item': f'{NS}:cucumber'}, {'tag': 'forge:dough'}],
        'result': {'item': f'{NS}:cucumber_dough', 'count': 2},
    })
    write_json(f'data/{NS}/recipes/cutting/cucumber_dough_cutting.json', {
        'type': 'farmersdelight:cutting',
        'ingredients': [{'item': f'{NS}:cucumber_dough'}],
        'result': [{'item': f'{NS}:cucumber_noodles'}],
        'tool': {'tag': 'forge:tools/knives'},
    })
    write_json(f'data/{NS}/tags/blocks/ghost_pepper_soil.json', {'replace': False, 'values': ['minecraft:soul_soil']})


def own_ids():
    items, blocks = set(), set()
    src = os.path.join(MOD, 'src/main/java/net/ribs/vintagedelight')
    for line in open(os.path.join(src, 'item/ModItems.java')):
        for m in re.finditer(r'ITEMS\.register\("([a-z_]+)"', line):
            items.add(f'{NS}:' + m.group(1))
        m = re.search(r'hat\("([a-z_]+)"\)', line)
        if m and 'private' not in line:
            items.add(f'{NS}:{m.group(1)}_chefs_hat')
    for line in open(os.path.join(src, 'block/ModBlocks.java')):
        m = re.search(r'(registerBlock|BLOCKS\.register)\("([a-z_]+)"', line)
        if m:
            blocks.add(f'{NS}:' + m.group(2))
            if m.group(1) == 'registerBlock':
                items.add(f'{NS}:' + m.group(2))
    return items, blocks


def own_tags(kind):
    tags = set()
    root = os.path.join(OUT, 'data')
    for ns in os.listdir(root):
        base = os.path.join(root, ns, 'tags', kind)
        for r, _, files in os.walk(base):
            for f in files:
                tags.add(f'{ns}:' + os.path.relpath(os.path.join(r, f), base)[:-5])
    return tags


def check_refs():
    """Item ids and tags named in recipes, loot tables and item tags that neither the pack's dumps nor the port know."""
    exported = os.path.join(INSTANCE, 'kubejs/exported')
    entries = json.load(open(os.path.join(exported, 'display_names.json')))['entries']
    names = {e['id'] for group in entries.values() for e in group}
    item_tags = set(json.load(open(os.path.join(exported, 'tags/item_tags.json'))))
    items, blocks = own_ids()
    item_tags |= own_tags('items')
    missing = []
    data = os.path.join(OUT, 'data')
    for root, _, files in os.walk(data):
        rel_root = os.path.relpath(root, data)
        if not any(s in rel_root for s in ('/recipes', '/loot_tables', '/tags/items', 'potato_cannon', 'moonlight')):
            continue
        for f in files:
            rel = os.path.join(rel_root, f)
            text = open(os.path.join(root, f)).read()
            conditional = '"forge:mod_loaded"' in text
            refs = re.findall(r'"(item|tag|name|id)":\s*"([^"]+)"', text)
            if '/tags/items' in rel_root or 'potato_cannon' in rel_root or 'moonlight' in rel_root:
                refs += [('item', v) for v in re.findall(r'"([a-z0-9_]+:[a-z0-9_/]+)"', text)]
            for kind, ref in refs:
                if kind == 'name' and ('/loot_tables' not in rel_root or ':' not in ref):
                    continue
                if kind == 'id' or ':' not in ref:
                    continue
                if ref.startswith('#'):
                    kind, ref = 'tag', ref[1:]
                known = (ref in item_tags) if kind == 'tag' else (ref in names or ref in items)
                if not known and ref.startswith(('moonlight:', 'minecraft:soft_fluids')):
                    known = True
                if not known:
                    missing.append(f'{rel}: {kind} {ref}' + (' (mod_loaded-gated)' if conditional else ''))
    return sorted(set(missing))


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
                counts[kind] = counts.get(kind, 0) + 1
                continue
            if '/lang/' in path:
                write_json(path, convert_lang(load_lang(raw), path.endswith('en_us.json')))
                counts[kind] = counts.get(kind, 0) + 1
                continue
            d = json.loads(rename_ids(raw.decode('utf-8')))
            path = out_path(path)
            if path == 'data/forge/loot_modifiers/global_loot_modifiers.json':
                d['entries'] = [e for e in d['entries'] if 'add_loot_' not in e]
            elif '/recipes/' in path:
                rel = path.split('/recipes/', 1)[1][:-5]
                d = convert_recipe(rel, d)
            elif '/tags/' in path:
                d = convert_tag(d)
                if d is None:  # only purged entries (forge:berries, forge:fruits, minecraft:fox_food)
                    continue
            elif '/loot_tables/' in path:
                d = convert_loot(path.split('/loot_tables/', 1)[1][:-5], d)
            elif '/advancements/' in path:
                d = convert_advancement(d)
            elif '/configured_feature/' in path:
                d = convert_configured_feature(name, d)
            elif '/soft_fluids/' in path:
                d = convert_soft_fluid(d)
            elif '/potato_cannon_projectile_types/' in path:
                # upstream names vintagedelight:pickled_ghost_pepper, which does not exist; the item is pickled_pepper
                d['items'] = [f'{NS}:pickled_pepper' if i == f'{NS}:pickled_ghost_pepper' else i for i in d['items']]
            write_json(path, d)
            counts[kind] = counts.get(kind, 0) + 1
    kots_files()
    print('written:', ', '.join(f'{k} {v}' for k, v in sorted(counts.items())))
    leftovers = []
    for root, _, files in os.walk(OUT):
        for f in files:
            full = os.path.join(root, f)
            if f.endswith('.json') and (PURGED.search(open(full, encoding='utf-8').read()) or re.search(r'vintagedelight:[a-z_]*mason_jar"', open(full, encoding='utf-8').read().replace('vintagedelight:empty_mason_jar"', ''))):
                leftovers.append(os.path.relpath(full, OUT))
            elif PURGED.search(f):
                leftovers.append(os.path.relpath(full, OUT))
    if leftovers:
        print('STILL NAMES A PURGED THING OR AN OLD JAR ID:', *leftovers, sep='\n  ')
    missing = check_refs()
    if missing:
        print('UNKNOWN to the pack dumps (dead inputs or outputs):', *missing, sep='\n  ')
    return 1 if leftovers else 0


if __name__ == '__main__':
    sys.exit(main())
