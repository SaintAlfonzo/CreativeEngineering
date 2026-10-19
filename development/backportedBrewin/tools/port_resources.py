#!/usr/bin/env python3
"""Copy Brewin' And Chewin' 4.5.0's assets and data into the port and convert them to 1.19.2 Forge formats.

    python3 tools/port_resources.py            (run from anywhere; rewrites src/main/resources/{assets,data})

Source: DecompiledMods/BrewinAndChewin-neoforge-4.5.0+1.21.1-src (assets/, data/). EMI files and 1.21-only data
(damage types, data maps, 1.20+ block tags) are left out. What changes on the way (alcohol.md 3.1 step 5):

  * folders: recipe -> recipes, loot_table -> loot_tables, advancement -> advancements, tags/block -> tags/blocks (and
    item/fluid/entity_type); data/neoforge/loot_modifiers -> data/forge/loot_modifiers
  * item stacks: {"id", "count", "components"} -> {"item", "count", "nbt"}; components that 1.19.2 keeps in NBT are
    translated (potion_contents -> Potion), the port's own JSON readers take "id" too
  * c: tags -> the forge: tag 1.19.2 mods use (C_TAGS below); neoforge:conditions -> Forge "conditions"
  * advancements: 1.20.5 item predicates and placed_block loot conditions -> 1.19.2 predicates; the temperature-near-keg
    advancement becomes impossible criteria that BnCCommonEvents awards from code
  * the port's deviations: honey pours from create:honey when Create is loaded (4.1), Dread Nog takes a poison potion
    (4.4), kombucha ferments green tea (4.5)
  * GUI: the four recipe-book filter sprites are packed into one 256x256 sheet (1.19.2 has no GUI sprite atlas); the
    keg screen gets two more slot frames, fluid in and empty container out (alcohol.md 8.4)
  * pack changes (alcohol.md 8): the three jams are left out (8.5), cooking-pot recipes get FD's default cookingtime so
    KubeJS can read them (8.1), and #minecraft:undead (a 1.21 tag) becomes 1.19.2's undead mobs (8.1)
"""
import copy
import json
import os
import shutil
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
MOD = os.path.dirname(HERE)
INSTANCE = os.path.dirname(os.path.dirname(MOD))
SRC = os.path.join(INSTANCE, 'DecompiledMods', 'BrewinAndChewin-neoforge-4.5.0+1.21.1-src')
OUT = os.path.join(MOD, 'src', 'main', 'resources')

# 1.21 convention tag -> 1.19.2 Forge tag (a list expands to several entries). Fluid tags first: the same name can mean
# a fluid (#c:water in a fluid ingredient) or an item tag.
C_FLUID_TAGS = {
    'c:water': 'minecraft:water',
    'c:milk': 'forge:milk',
    'c:honey': 'forge:honey',
}
C_TAGS = {
    'c:tools/knife': 'forge:tools/knives',
    'c:crops/potato': 'forge:crops/potato',
    'c:crops/cabbage': 'forge:crops/cabbage',
    'c:crops/beetroot': 'forge:crops/beetroot',
    'c:crops/tomato': 'forge:crops/tomato',
    'c:crops/rice': 'forge:crops/rice',
    'c:crops/carrot': 'forge:crops/carrot',
    'c:foods/safe_raw_fish': 'forge:raw_fishes',
    'c:foods/vegetable': 'forge:vegetables',
    'c:foods/pasta': 'forge:pasta',
    'c:foods/onion': 'forge:crops/onion',
    'c:foods/bread': 'forge:bread',
    'c:foods/tomato': 'forge:crops/tomato',
    'c:foods/cabbage': 'forge:crops/cabbage',
    'c:foods/dough': 'forge:dough',
    'c:foods/raw_chicken': 'forge:raw_chicken',
    'c:foods/raw_beef': 'forge:raw_beef',
    'c:foods/raw_meat': ['forge:raw_beef', 'forge:raw_pork', 'forge:raw_mutton', 'forge:raw_chicken'],
    'c:foods/cooked_pork': 'forge:cooked_pork',
    'c:foods/cooked_mutton': 'forge:cooked_mutton',
    'c:foods/cooked_cod': 'forge:cooked_fishes/cod',
    'c:foods/cooked_beef': 'forge:cooked_beef',
    'c:foods/cooked_bacon': 'forge:cooked_bacon',
    'c:eggs': 'forge:eggs',
    'c:drinks/milk': 'forge:milk',
}
# Farmer's Delight 1.21 (MIT), for the one texture 4.5.0 borrows from it that FD 1.2.4 doesn't have (coaster tray side).
FD_121_JAR = os.path.join(os.path.dirname(os.path.dirname(INSTANCE)), 'downloads', 'FarmersDelight-1.21.1-1.3.2.jar')
# 1.21 model references -> what Farmer's Delight 1.2.4 has instead.
MODEL_REFS = {
    'farmersdelight:block/template_pie': 'farmersdelight:block/pie',
    'farmersdelight:block/template_pie_slice1': 'farmersdelight:block/pie_slice1',
    'farmersdelight:block/template_pie_slice2': 'farmersdelight:block/pie_slice2',
    'farmersdelight:block/template_pie_slice3': 'farmersdelight:block/pie_slice3',
    'farmersdelight:block/tray': 'brewinandchewin:block/fd_tray',
}
TAG_DIRS = {'block': 'blocks', 'item': 'items', 'fluid': 'fluids', 'entity_type': 'entity_types', 'mob_effect': 'mob_effect'}
DATA_DIRS = {'recipe': 'recipes', 'loot_table': 'loot_tables', 'advancement': 'advancements', 'loot_modifiers': 'loot_modifiers'}
# 1.21-only data the port has no use for.
SKIP_DATA = (
    'brewinandchewin/damage_type/', 'brewinandchewin/tags/damage_type/', 'minecraft/tags/damage_type/',
    'minecraft/tags/block/combination_step_sound_blocks.json', 'neoforge/data_maps/',
    'c/tags/item/',  # c:foods, c:foods/soup, c:foods/edible_when_placed: no 1.19.2 consumer
)
POISONS = ['minecraft:poison', 'minecraft:long_poison', 'minecraft:strong_poison']
# alcohol.md 8.5: Fruits Delight covers jams, so these leave the port (every file named after them, lang keys, and their
# criteria in chef_of_the_ages).
JAMS = ('sweet_berry_jam', 'glow_berry_marmalade', 'apple_jelly', 'jam_jar')
# alcohol.md 8.1: 1.19.2 has no #minecraft:undead entity tag; these are the vanilla mobs with MobType.UNDEAD.
# BnCCommonEvents also checks the mob type, so modded undead are covered without being listed.
UNDEAD = [
    'minecraft:zombie', 'minecraft:husk', 'minecraft:drowned', 'minecraft:zombie_villager', 'minecraft:zombified_piglin',
    'minecraft:zoglin', 'minecraft:skeleton', 'minecraft:stray', 'minecraft:wither_skeleton', 'minecraft:skeleton_horse',
    'minecraft:zombie_horse', 'minecraft:phantom', 'minecraft:wither',
]
# Lang lines for the port's own additions (alcohol.md 8), added to en_us.json.
PORT_LANG = {
    'brewinandchewin.tooltip.keg.pipe_drain_idle': "Pipes can only drain it while it isn't fermenting",
}
# Farmer's Delight's CookingPotRecipe default; 4.5.0's files leave it out, which KubeJS's recipe schema rejects.
FD_COOKING_TIME = 200

warnings = []


def warn(msg):
    warnings.append(msg)


def is_jam_file(rel):
    stem = os.path.splitext(os.path.basename(rel))[0]
    return any(j in stem for j in JAMS)


def map_tag(name, fluid=False):
    """'c:x' or '#c:x' -> forge name(s), keeping the '#'. Returns a str or a list."""
    hashed = name.startswith('#')
    bare = name[1:] if hashed else name
    if not bare.startswith('c:'):
        return name
    table = C_FLUID_TAGS if fluid else C_TAGS
    mapped = table.get(bare) or (C_TAGS.get(bare) if fluid else C_FLUID_TAGS.get(bare))
    if mapped is None:
        warn(f'unmapped tag {name}')
        return name
    if isinstance(mapped, list):
        return [('#' if hashed else '') + m for m in mapped]
    return ('#' if hashed else '') + mapped


def components_to_nbt(components, where):
    nbt = {}
    for key, value in components.items():
        if key == 'minecraft:potion_contents':
            potion = value if isinstance(value, str) else value.get('potion')
            if potion:
                nbt['Potion'] = potion
        else:
            warn(f'{where}: dropped component {key}')
    return nbt


def item_stack(obj, where, legacy_key=True):
    """1.21 stack -> 1.19.2 {"item", "count", "nbt"} (vanilla/FD readers want "item")."""
    out = {'item': obj.get('id', obj.get('item'))}
    if obj.get('count', 1) != 1:
        out['count'] = obj['count']
    if 'components' in obj:
        nbt = components_to_nbt(obj['components'], where)
        if nbt:
            out['nbt'] = nbt
    if 'nbt' in obj:
        out['nbt'] = obj['nbt']
    return out


def ingredient(obj, where):
    """An item ingredient (dict, list or []); maps c: tags."""
    if isinstance(obj, list):
        out = []
        for entry in obj:
            converted = ingredient(entry, where)
            out.extend(converted if isinstance(converted, list) else [converted])
        return out
    obj = dict(obj)
    if 'tag' in obj:
        mapped = map_tag(obj['tag'])
        if isinstance(mapped, list):
            return [{'tag': m} for m in mapped]
        obj['tag'] = mapped
    if 'components' in obj:
        nbt = components_to_nbt(obj.pop('components'), where)
        if nbt:
            obj = {'type': 'forge:partial_nbt', 'item': obj['item'], 'nbt': nbt}
    return obj


def conditions(obj):
    """neoforge:conditions -> Forge 1.19.2 "conditions"; fabric conditions dropped."""
    obj.pop('fabric:load_conditions', None)
    conds = obj.pop('neoforge:conditions', None)
    if conds:
        out = []
        for cond in conds:
            if cond.get('type') == 'neoforge:mod_loaded':
                out.append({'type': 'forge:mod_loaded', 'modid': cond['modid']})
            else:
                warn(f'unknown condition {cond}')
        obj['conditions'] = out
    return obj


def fluid_stack(obj):
    """Fluid stacks keep 4.5.0's {"id", "amount", "unit"} (the port reads them), components -> nbt."""
    obj = dict(obj)
    if 'components' in obj:
        nbt = components_to_nbt(obj.pop('components'), 'fluid')
        if nbt:
            obj['nbt'] = nbt
    return obj


def convert_recipe(rel, d):
    d = conditions(copy.deepcopy(d))
    t = d.get('type')
    if t == 'brewinandchewin:fermenting':
        d['ingredients'] = [ingredient(i, rel) if i != [] else [] for i in d['ingredients']]
        if 'fermentingtime' in d:  # 4.5.0 kombucha's misspelt key
            d['fermenting_time'] = d.pop('fermentingtime')
        bf = d.get('base_fluid')
        if bf:
            ing = bf['ingredient']
            if 'tag' in ing:
                ing['tag'] = map_tag(ing['tag'] if ing['tag'].startswith('#') else '#' + ing['tag'], fluid=True)
        res = d['result']
        if 'amount' in res:
            d['result'] = fluid_stack(res)
        else:
            d['result'] = item_stack(res, rel)
    elif t == 'brewinandchewin:keg_pouring':
        d['fluid'] = fluid_stack(d['fluid'])
        d['output'] = item_stack(d['output'], rel)
        if 'container' in d:
            d['container'] = item_stack(d['container'], rel)
    elif t == 'brewinandchewin:create_potion_pouring':
        d['output'] = item_stack(d['output'], rel)
        if 'container' in d:
            d['container'] = item_stack(d['container'], rel)
    elif t == 'farmersdelight:cooking':
        d['ingredients'] = [ingredient(i, rel) for i in d['ingredients']]
        d['result'] = item_stack(d['result'], rel)
        if 'container' in d:
            d['container'] = item_stack(d['container'], rel)
            d['container'].pop('count', None)
        d.setdefault('cookingtime', FD_COOKING_TIME)
    elif t == 'farmersdelight:cutting':
        d['ingredients'] = [ingredient(i, rel) for i in d['ingredients']]
        d['tool'] = ingredient(d['tool'], rel)
        d['result'] = [item_stack(r['item'] if isinstance(r.get('item'), dict) else r, rel) for r in d['result']]
    elif t in ('minecraft:crafting_shaped', 'minecraft:crafting_shapeless'):
        if 'key' in d:
            d['key'] = {k: ingredient(v, rel) for k, v in d['key'].items()}
        if 'ingredients' in d:
            d['ingredients'] = [ingredient(i, rel) for i in d['ingredients']]
        d['result'] = item_stack(d['result'], rel)
        d.pop('category', None)
    else:
        warn(f'{rel}: unknown recipe type {t}')
    return d


def apply_deviations(rel, d):
    """The port's recipe deviations (alcohol.md 4.1, 4.4, 4.5). Returns a dict of extra files {rel: json}."""
    extra = {}
    name = os.path.basename(rel)
    if rel.endswith('recipes/pouring/honey_bottle.json'):
        # 4.1: with Create the pack's create:honey is the one honey; brewinandchewin:honey exists only without Create.
        own = copy.deepcopy(d)
        own['conditions'] = [{'type': 'forge:not', 'value': {'type': 'forge:mod_loaded', 'modid': 'create'}}]
        extra[rel.replace('honey_bottle.json', 'honey_bottle_without_create.json')] = own
        d['fluid']['id'] = 'create:honey'
        d['conditions'] = [{'type': 'forge:mod_loaded', 'modid': 'create'}]
    elif name == 'dread_nog_from_egg_grog.json':
        # 4.4: a poison potion (any of the three) in place of the ominous bottle; the keg gives the bottle back.
        d['ingredients'] = [
            [{'type': 'forge:partial_nbt', 'item': 'minecraft:potion', 'nbt': {'Potion': p}} for p in POISONS]
            if i == {'item': 'minecraft:ominous_bottle'} else i
            for i in d['ingredients']
        ]
    elif name == 'kombucha.json' and 'fermenting' in rel:
        # 4.5: green tea (Create Central Kitchen's fluid, 250 mB per Farmer's Respite green tea) in place of vodka.
        d['base_fluid'] = {'amount': 1000, 'ingredient': {'id': 'create_central_kitchen:green_tea'}, 'unit': 'millibuckets'}
        d['fermenting_time'] = 9600
        d['conditions'] = [
            {'type': 'forge:mod_loaded', 'modid': 'farmersrespite'},
            {'type': 'forge:mod_loaded', 'modid': 'create_central_kitchen'},
        ]
    return extra


def item_predicate(p):
    """1.20.5 ItemPredicate {"items": "id" | "#tag" | [..]} -> 1.19.2 {"items": [..]} or {"tag": ".."}."""
    p = dict(p)
    items = p.get('items')
    if isinstance(items, str):
        if items.startswith('#'):
            p.pop('items')
            p['tag'] = map_tag(items[1:])
        else:
            p['items'] = [items]
    return p


def convert_advancement(rel, d):
    d = copy.deepcopy(d)
    d.pop('sends_telemetry_event', None)
    disp = d.get('display')
    if disp and 'icon' in disp:
        disp['icon'] = {'item': disp['icon'].get('id', disp['icon'].get('item'))}
    for name, crit in d['criteria'].items():
        trig = crit.get('trigger')
        c = crit.get('conditions', {})
        if trig == 'minecraft:inventory_changed' and 'items' in c:
            c['items'] = [item_predicate(p) for p in c['items']]
        elif trig == 'minecraft:consume_item' and 'item' in c:
            c['item'] = item_predicate(c['item'])
        elif trig == 'minecraft:placed_block' and not rel.endswith('place_temperature_block_near_keg.json'):
            loc = c.get('location', [])
            if len(loc) == 1 and loc[0].get('condition') == 'minecraft:block_state_property':
                crit['conditions'] = {'block': loc[0]['block']}
            else:
                warn(f'{rel}: placed_block criterion {name} left as is')
    if rel.endswith('place_temperature_block_near_keg.json'):
        for crit in d['criteria'].values():
            crit.clear()
            crit['trigger'] = 'minecraft:impossible'
    jam_criteria = [name for name in d['criteria'] if any(j in name for j in JAMS)]
    for name in jam_criteria:
        del d['criteria'][name]
    if jam_criteria and 'requirements' in d:
        d['requirements'] = [[c for c in req if c not in jam_criteria] for req in d['requirements']]
        d['requirements'] = [req for req in d['requirements'] if req]
    return d


def convert_loot(obj):
    """Recursively: any_of -> alternative, match_tool item predicates, c: tags."""
    if isinstance(obj, list):
        return [convert_loot(o) for o in obj]
    if not isinstance(obj, dict):
        return obj
    obj = {k: convert_loot(v) for k, v in obj.items()}
    if obj.get('condition') == 'minecraft:any_of':
        obj['condition'] = 'minecraft:alternative'
    if obj.get('condition') == 'minecraft:match_tool' and 'predicate' in obj:
        pred = item_predicate(obj['predicate'])
        if 'tag' in pred:
            pred['tag'] = map_tag(pred['tag'])
        obj['predicate'] = pred
    obj.pop('random_sequence', None)
    return obj


def convert_tag(rel, d, fluid):
    values = []
    for v in d['values']:
        entry = v if isinstance(v, str) else v['id']
        if entry == '#minecraft:undead' and rel.split('/')[2] == 'entity_type':
            values.extend(UNDEAD)
            continue
        required = True if isinstance(v, str) else v.get('required', True)
        mapped = map_tag(entry, fluid=fluid)
        names = mapped if isinstance(mapped, list) else [mapped]
        for n in names:
            if n != entry or not required:
                values.append({'id': n, 'required': False})
            else:
                values.append(n)
    out = {'values': values}
    if d.get('replace'):
        out['replace'] = True
    return out


def out_path_for_data(rel):
    """data-relative path in 1.21 layout -> 1.19.2 layout."""
    parts = rel.split('/')
    ns, kind = parts[0], parts[1]
    if ns == 'neoforge' and kind == 'loot_modifiers':
        return '/'.join(['forge'] + parts[1:])
    if ns == 'c':
        ns = 'forge'
    if kind == 'tags':
        reg = parts[2]
        return '/'.join([ns, 'tags', TAG_DIRS.get(reg, reg)] + parts[3:])
    return '/'.join([ns, DATA_DIRS.get(kind, kind)] + parts[2:])


def write_json(path, d):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w') as f:
        json.dump(d, f, indent=2)
        f.write('\n')


def port_data():
    src = os.path.join(SRC, 'data')
    out = os.path.join(OUT, 'data')
    shutil.rmtree(out, ignore_errors=True)
    count = 0
    for root, _dirs, files in os.walk(src):
        for fn in sorted(files):
            full = os.path.join(root, fn)
            rel = os.path.relpath(full, src).replace(os.sep, '/')
            if any(rel.startswith(s) for s in SKIP_DATA) or is_jam_file(rel):
                continue
            if not fn.endswith('.json'):
                warn(f'non-json data file {rel} skipped')
                continue
            d = json.load(open(full))
            new_rel = out_path_for_data(rel)
            parts = rel.split('/')
            extra = {}
            if parts[1] == 'recipe':
                d = convert_recipe(rel, d)
                extra = apply_deviations(new_rel, d)
            elif parts[1] == 'advancement':
                d = convert_advancement(new_rel, d)
            elif parts[1] in ('loot_table', 'loot_modifiers'):
                d = convert_loot(d)
            elif parts[1] == 'tags':
                d = convert_tag(rel, d, fluid=(parts[2] == 'fluid'))
            write_json(os.path.join(out, new_rel), d)
            count += 1
            for erel, ed in extra.items():
                write_json(os.path.join(out, erel), ed)
                count += 1
    # Upstream gap: every drink has a fluid tag except kombucha, which default_displays.json still names.
    kombucha = os.path.join(out, 'brewinandchewin', 'tags', 'fluids', 'kombucha.json')
    if not os.path.exists(kombucha):
        write_json(kombucha, {'values': ['brewinandchewin:kombucha', 'brewinandchewin:flowing_kombucha']})
        count += 1
    # 4.1: the port's own honey (only registered without Create) joins forge:honey, as upstream's c:honey.
    honey = os.path.join(out, 'forge', 'tags', 'fluids', 'honey.json')
    if os.path.exists(honey):
        d = json.load(open(honey))
        ids = {v['id'] if isinstance(v, dict) else v for v in d['values']}
        for fid in ('brewinandchewin:honey', 'brewinandchewin:flowing_honey'):
            if fid not in ids:
                d['values'].append({'id': fid, 'required': False})
        d['values'] = [v if isinstance(v, dict) else {'id': v, 'required': False} for v in d['values']]
        write_json(honey, d)
    return count


def convert_fluid_display(d):
    out = {}
    for key, value in d.items():
        new_key = key
        if key.startswith('#'):
            mapped = map_tag(key, fluid=True)
            new_key = mapped if isinstance(mapped, str) else mapped[0]
        if isinstance(value, dict):
            value = dict(value)
            if 'base' in value:  # remapper form (create:potion) -> copy the fluid's potion NBT onto the bottle
                base = value.pop('base')
                value.pop('remaps', None)
                value = {'id': base['id'], 'copy_nbt': ['Potion', 'CustomPotionEffects'], **value}
            if 'components' in value:
                nbt = components_to_nbt(value.pop('components'), 'fluid display')
                if nbt:
                    value['nbt'] = nbt
        out[new_key] = value
    return out


def port_assets():
    src = os.path.join(SRC, 'assets', 'brewinandchewin')
    out = os.path.join(OUT, 'assets', 'brewinandchewin')
    shutil.rmtree(out, ignore_errors=True)
    count = 0
    for root, _dirs, files in os.walk(src):
        rel_root = os.path.relpath(root, src).replace(os.sep, '/')
        if rel_root.startswith('textures/gui/emi'):
            continue
        for fn in files:
            full = os.path.join(root, fn)
            rel = (rel_root + '/' + fn) if rel_root != '.' else fn
            if is_jam_file(rel):
                continue
            dest = os.path.join(out, rel)
            os.makedirs(os.path.dirname(dest), exist_ok=True)
            if rel.startswith('brewinandchewin/fluid_item_displays/'):
                write_json(dest, convert_fluid_display(json.load(open(full))))
            elif rel.startswith('lang/') and fn.endswith('.json'):
                lang = json.load(open(full, encoding='utf-8'))
                lang = {k: v for k, v in lang.items() if not any(j in k for j in JAMS)}
                if fn == 'en_us.json':
                    lang.update(PORT_LANG)
                with open(dest, 'w', encoding='utf-8') as f:
                    json.dump(lang, f, indent=2, ensure_ascii=False)
                    f.write('\n')
            elif rel.startswith('models/') and fn.endswith('.json'):
                text = open(full).read()
                for old, new in MODEL_REFS.items():
                    text = text.replace('"' + old + '"', '"' + new + '"')
                open(dest, 'w').write(text)
            else:
                shutil.copyfile(full, dest)
            count += 1
    copy_fd_tray(os.path.join(out, 'textures', 'block', 'fd_tray.png'))
    add_keg_fluid_slots(os.path.join(out, 'textures', 'gui', 'keg.png'))
    build_filter_sheet(os.path.join(src, 'textures', 'gui', 'sprites', 'recipe_book'), os.path.join(out, 'textures', 'gui', 'keg_recipe_book.png'))
    return count


def copy_fd_tray(dest):
    import zipfile
    if not os.path.exists(FD_121_JAR):
        warn(f'{FD_121_JAR} missing: coaster tray side texture not copied')
        return
    with zipfile.ZipFile(FD_121_JAR) as jar:
        with open(dest, 'wb') as f:
            f.write(jar.read('assets/farmersdelight/textures/block/tray.png'))


# alcohol.md 8.4: the keg's fluid slot pair (KegMenu slots 6 and 7, items at x 8, y 17 and 53), lined up with the player
# inventory's first column and the recipe-book button between them. The frames are copies of the container slot's
# (item at 91, 55); the arrow between them only shows when the recipe book is turned off.
KEG_SLOT_FRAME = (90, 54)
KEG_FLUID_SLOTS = ((7, 16), (7, 52))
KEG_ARROW = (139, 139, 139, 255)


def add_keg_fluid_slots(path):
    try:
        from PIL import Image
    except ImportError:
        warn('PIL missing: keg.png has no fluid slot frames')
        return
    im = Image.open(path).convert('RGBA')
    fx, fy = KEG_SLOT_FRAME
    frame = im.crop((fx, fy, fx + 18, fy + 18))
    for pos in KEG_FLUID_SLOTS:
        im.paste(frame, pos)
    cx = KEG_FLUID_SLOTS[0][0] + 8
    for y in range(37, 46):
        im.putpixel((cx, y), KEG_ARROW)
        im.putpixel((cx + 1, y), KEG_ARROW)
    for i, y in enumerate(range(46, 49)):
        for x in range(cx - 2 + i, cx + 4 - i):
            im.putpixel((x, y), KEG_ARROW)
    im.save(path)


def build_filter_sheet(sprite_dir, dest):
    """StateSwitchingButton#initTextureValues(0, 0, 28, 18): (0,0) off, (28,0) on, (0,18) off+hover, (28,18) on+hover."""
    try:
        from PIL import Image
    except ImportError:
        warn('PIL missing: keg_recipe_book.png not built')
        return
    sheet = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
    layout = {
        'keg_filter_disabled': (0, 0),
        'keg_filter_enabled': (28, 0),
        'keg_filter_disabled_highlighted': (0, 18),
        'keg_filter_enabled_highlighted': (28, 18),
    }
    for name, pos in layout.items():
        sprite = Image.open(os.path.join(sprite_dir, name + '.png')).convert('RGBA')
        sheet.paste(sprite, pos)
    os.makedirs(os.path.dirname(dest), exist_ok=True)
    sheet.save(dest)


def main():
    if not os.path.isdir(SRC):
        print(f'missing {SRC}', file=sys.stderr)
        sys.exit(1)
    n_assets = port_assets()
    n_data = port_data()
    print(f'assets: {n_assets} files, data: {n_data} files')
    for w in warnings:
        print('warning:', w)


if __name__ == '__main__':
    main()
