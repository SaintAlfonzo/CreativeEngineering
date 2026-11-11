#!/usr/bin/env python3
"""Food types: classify every food in the pack into one type, keep the assignments, and check them.

The assignments live in kubejs/generation_scripts/food/food_types.json (type -> item_dict keys). Every write also
splits them by item namespace into Creative Delight member scripts, kubejs/data/creativedelight/kubejs/<namespace>/
food_types.js (`// requires: <namespace>`, outRoot in tools/generators.json, creative_delight.md 28.2); the tag script
Creative Delight creativeengineering/food_type_tags.js merges those into #creativedelight:food_type/<type> item
tags, and every nested type also lands in its parents (raw/meat/sliced -> raw/meat -> raw -> food_type). The game
never reads the JSON.

Source data is the /dumpfood export (kubejs/exported/food/*.json): what the game itself reports after
every mod and KubeJS edit. Run /dumpfood in game first, then:

    python3 tools/food_catalog.py            classify new foods, update food_types.json + item_dict keys, check
    python3 tools/food_catalog.py --check    only check (exit 1 on problems, or member scripts not matching the JSON)
    python3 tools/food_catalog.py --members  only rewrite the member scripts from food_types.json (after a hand edit)

Hand edits in food_types.json are kept: an item already listed stays where it is. Only foods that are in
no type yet get the automatic rules below (and are printed so you can move them). A food that has no
item_dict key gets one in the generated "Food mods" block of item_dict.js.
"""
import argparse
import json
import re
import sys
from collections import OrderedDict, defaultdict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import pack_paths  # noqa: E402
import gen_out  # noqa: E402

INSTANCE = Path(__file__).resolve().parent.parent
EXPORT = INSTANCE / 'kubejs/exported/food'
DICT = pack_paths.ITEM_DICT
TYPES_JSON = INSTANCE / 'kubejs/generation_scripts/food/food_types.json'

TAG_ROOT = 'creativedelight:food_type'
CANNED_TAG = 'creativedelight:canned_source'

# Every type, in display order. A '/' nests a type under its parent (the tag chain follows it).
TYPES = OrderedDict([
    ('raw/veggie', 'Raw vegetable, herb, fungus or grain'),
    ('raw/veggie/sliced', 'Cut raw vegetable (cabbage leaf, potato slices, pumpkin slice)'),
    ('raw/fruit', 'Raw fruit, berry or nut'),
    ('raw/meat', 'Raw meat, whole cut'),
    ('raw/meat/sliced', 'Raw meat, cut small (cuts, chops, mince, bacon, fillet, shank)'),
    ('raw/fish', 'Raw fish or seafood, whole'),
    ('raw/fish/sliced', 'Raw fish or seafood, sliced'),
    ('cooked/veggie', 'Cooked, dried, pickled or roasted vegetable (incl. cooked rice)'),
    ('cooked/veggie/sliced', 'Cooked cut vegetable'),
    ('cooked/fruit', 'Cooked or dried fruit'),
    ('cooked/meat', 'Cooked meat, whole cut (incl. patties, jerky, glazed cuts)'),
    ('cooked/meat/sliced', 'Cooked meat, cut small'),
    ('cooked/fish', 'Cooked fish or seafood, whole'),
    ('cooked/fish/sliced', 'Cooked fish or seafood, sliced'),
    ('egg', 'Cooked egg dish on its own (fried, boiled, scrambled)'),
    ('baked', 'Bread and buns'),
    ('dairy', 'Cheese'),
    ('sweet', 'Desserts and confectionery: cake/pie slices, cookies, candy, chocolate, ice cream'),
    ('sweet/pancake', 'Pancakes eaten as items: single pancakes and pancake dishes (food_families.md dessert/pancake)'),
    ('handheld', 'Sandwiches, burgers, wraps, rolls, skewers, stuffed vegetables, pizza/quiche slices'),
    ('salad', 'Salads'),
    ('soup', 'Soups, stews and broths'),
    ('meal', 'Plated meal (bowl or plate)'),
    ('meal/feast', 'Serving from a placed feast'),
    ('drink', 'Non-alcoholic drink'),
    ('drink/alcohol', 'Wine, beer, spirits'),
    ('golden', 'Golden food (golden apple tier)'),
    ('golden/enchanted', 'Enchanted golden food (notch apple tier)'),
    ('ingredient', 'Edible intermediate (dough, batter, sauce, oil, spreads, beans)'),
    ('hazard', 'Eaten for its downside or in an emergency (rotten, poisonous, soap)'),
    ('special', 'Magic, joke or utility food (chorus fruit, mana cookie, icicle, dog food)'),
    ('canned', 'Canned Goods can'),
])

# Edible in code but not food for the tuning system: never tagged.
EXCLUDE_PREFIX = {
    'create_central_kitchen:incomplete_': 'sequenced-assembly intermediate',
}
EXCLUDE = {
    'solapplepie:lunchbox': 'food container', 'solapplepie:lunchbag': 'food container',
    'solapplepie:golden_lunchbox': 'food container',
}

# Where the rules below guess wrong. Checked before any rule.
OVERRIDES = {
    # Hearth and Harvest (hearth_and_harvest.md 4.2 step 12): the regexes put these in the wrong type.
    'hearthandharvest:cider_donut': 'sweet',
    'hearthandharvest:croissant': 'baked',
    'hearthandharvest:raspberry_scone': 'baked',
    'hearthandharvest:hot_dog': 'handheld',
    'hearthandharvest:street_corn': 'handheld',
    'hearthandharvest:tamale': 'handheld',
    'hearthandharvest:trail_mix': 'sweet',
    'hearthandharvest:cooked_sausage': 'cooked/meat',
    'hearthandharvest:raw_sausage': 'raw/meat',
    'hearthandharvest:raisins': 'cooked/fruit',
    'hearthandharvest:goat_cheese_slice': 'dairy',
    'hearthandharvest:pickled_carrots': 'cooked/veggie',
    'hearthandharvest:pickled_potatoes': 'cooked/veggie',
    'hearthandharvest:waffle': 'sweet/pancake',
    'hearthandharvest:charred_marshmallow_stick': 'hazard',
    'hearthandharvest:moonshine': 'drink/alcohol',
    'hearthandharvest:blueberry_wine': 'drink/alcohol',
    'hearthandharvest:raspberry_wine': 'drink/alcohol',
    'hearthandharvest:sweet_berry_wine': 'drink/alcohol',
    'hearthandharvest:glow_berry_wine': 'drink/alcohol',
    'hearthandharvest:melon_wine': 'drink/alcohol',
    'hearthandharvest:lemon_wine': 'drink/alcohol',
    'farmersdelight:cooked_rice': 'cooked/veggie',
    'farmersdelight:dog_food': 'special',
    'farmersdelight:apple_cider': 'drink',
    'farmersdelight:kelp_roll_slice': 'handheld',
    'farmersdelight:gleaming_salad': 'salad',   # golden, ruled by GOLDEN_EXTRA in gen_comfort_nourishment.py (plan_scratch_bugfix_1103 5.4)
    'quarkdelight:bowl_of_exotic_fruits': 'salad',
    'quarkdelight:cooked_whole_crab': 'meal',
    'quarkdelight:miso_with_bamboo_sprouts': 'soup',
    'quarkdelight:crab_bars': 'handheld',
    'abnormals_delight:escargot': 'meal',
    'abnormals_delight:maple_glazed_bacon': 'cooked/meat',
    'buzzier_bees:glazed_porkchop': 'cooked/meat',
    'buzzier_bees:honey_apple': 'sweet',
    'brewinandchewin:kimchi': 'cooked/veggie',
    'creativedelight:pickled_sea_pickle': 'cooked/veggie',
    'brewinandchewin:kippers': 'cooked/fish',
    'brewinandchewin:jerky': 'cooked/meat',
    'pneumaticcraft:chips': 'cooked/veggie',
    'pneumaticcraft:cod_n_chips': 'handheld',
    'delightful:chopped_clover': 'ingredient',
    'delightful:chunk_nugget': 'handheld',
    'delightful:acorn': 'raw/fruit',
    'delightful:cantaloupe_slice': 'raw/fruit',
    'delightful:cactus_flesh': 'raw/veggie',
    'delightful:cactus_steak': 'cooked/veggie',
    'delightful:ender_nectar': 'drink',
    'endersdelight:shulker_mollusk': 'raw/fish',
    'endersdelight:shulker_filet': 'raw/fish/sliced',
    'endersdelight:mite_crust': 'raw/meat',
    'alexsmobs:gongylidia': 'raw/veggie',
    'alexsmobs:blobfish': 'raw/fish',
    'neapolitan:mint_leaves': 'raw/veggie',
    'neapolitan:roasted_adzuki_beans': 'cooked/veggie',
    'neapolitan:dried_banana': 'cooked/fruit',
    'neapolitan:strawberry_scones': 'baked',
    'nethersdelight:hoglin_ear': 'raw/meat/sliced',
    'nethersdelight:hoglin_sirloin': 'cooked/meat',
    'nethersdelight:ground_strider': 'raw/meat/sliced',
    'nethersdelight:strider_slice': 'raw/meat/sliced',
    'nethersdelight:warped_moldy_meat': 'meal',
    'nethersdelight:plate_of_stuffed_hoglin_ham': 'meal/feast',
    'nethersdelight:plate_of_stuffed_hoglin_roast': 'meal/feast',
    'nethersdelight:plate_of_stuffed_hoglin_snout': 'meal/feast',
    'rusticdelight:baked_potato_slices': 'cooked/veggie/sliced',
    'upgrade_aquatic:boiled_blue_pickerelweed': 'cooked/veggie',
    'upgrade_aquatic:boiled_purple_pickerelweed': 'cooked/veggie',
    'canned_goods:soda': 'drink', 'canned_goods:reef_cola': 'drink', 'canned_goods:oculemonade': 'drink',
    'canned_goods:hot_hot_hotpot': 'canned',
    'minecraft:honey_bottle': 'drink',
    'minecraft:suspicious_stew': 'soup',
    'minecraft:dried_kelp': 'cooked/veggie',
    'minecraft:baked_potato': 'cooked/veggie',
}

HAZARD = {
    'minecraft:rotten_flesh', 'minecraft:spider_eye', 'minecraft:poisonous_potato', 'minecraft:pufferfish',
    'domesticationinnovation:rotten_apple', 'domesticationinnovation:sinister_carrot', 'vinery:rotten_cherry',
    'supplementaries:soap', 'immersive_weathering:pond_water', 'everythingcopper:copper_nugget',
    'alexsmobs:maggot',
}
SPECIAL = {
    'minecraft:chorus_fruit', 'quark:ancient_fruit', 'nethersdelight:propelpearl', 'nethersdelight:magma_gelatin',
    'botania:mana_cookie', 'botania:mana_bottle', 'alexsmobs:cosmic_cod', 'alexsmobs:rainbow_jelly',
    'alexsmobs:fish_oil', 'immersive_weathering:icicle', 'immersive_weathering:ice_sickle',
    'immersive_weathering:azalea_flowers', 'immersive_weathering:moss_clump', 'neapolitan:ice_cubes',
}

R_SOUP = re.compile(r'(soup|stew|broth|chowder|sinigang|chili|gumbo|^sopa_)')
R_DRINK = re.compile(r'((^|_)(juice|tea|latte|coffee|milkshake|smoothie|cider|lemonade|cola|soda|nectar)$|hot_chocolate)')
R_SWEET = re.compile(r'(cake_slice|gateau_slice|pie_slice|cheesecake|cookie|cookies|candy|candies|chocolate|fudge|marshmallow|'
                     r'marshmellow|ice_cream|popsicle|gummy|custard|pudding|gelatine|bonbon|eclair|glazed_berries|caramel|'
                     r'(caramel|chocolate|honey|sweet)_roll$|waffle|beignet|gingerbread|smore|honeyed_apple|candy_cane|glazed_walnut|glowgurt)')
R_HANDHELD = re.compile(r'(sandwich|burger|wrap$|_roll$|_rolls$|skewer|barbecue_stick|kebab|dumplings|stuffed_potato|'
                        r'stuffed_bell_pepper|pizza_slice|quiche_slice|bruschetta|chunkwich|rangoon|meatballs|tempura|'
                        r'chunk_nugget)')
R_MEAL = re.compile(r'(pasta|noodles|lasagna|paella|curry|fried_rice|mushroom_rice|ratatouille|platter|fondue|pierogies|'
                    r'omelet|steak_and_potatoes|shepherds_pie|stuffed_pumpkin|roast_chicken|honey_glazed_ham|roasted_mutton|'
                    r'grilled_salmon|glazed_duck|seared_|with_)')
R_EGG = re.compile(r'(^|_)(egg|eggs)$')
R_BAKED = re.compile(r'(bread$|_bun$|scones|sourdough|baguette|biscuit|toast$)')
R_DAIRY = re.compile(r'(cheese_wedge|cheese$)')
R_INGREDIENT = re.compile(r'((^|_)dough$|batter|raw_pasta|pie_crust|tomato_sauce|cooking_oil|animal_fat|nut_butter_bottle|jelly_bottle|'
                          r'jam_bottle|sweet_berry_mince|coffee_beans|vanilla_pods|tea_leaf|apple_mash)')
R_COOKED = re.compile(r'(^cooked_|_cooked$|^roasted_|^grilled_|^baked_|^fried_|^boiled_|^smoked_|^seared_|^dried_|_patty$|'
                      r'jerky|kippers|_steak$|sirloin)')
R_SLICED = re.compile(r'(slice|slices|_cuts$|_chops$|mince|minced|fillet|filet|shank|shanks|drumstick|_rib$|_leaf$|^bacon$|'
                      r'_bacon$|_ear$)')
R_FRUIT = re.compile(r'(apple|berry|berries|melon|banana|cherry|grape|mulberr|strawberr|cantaloupe|acorn|walnut|lemon|'
                     r'passion|coconut)')
R_FISH = re.compile(r'(cod|salmon|tropical_fish|pufferfish|catfish|lobster|crab|shrimp|perch|pike|lionfish|calamari|squid|'
                    r'blobfish|flying_fish|fish)')
R_MEAT = re.compile(r'(beef|pork|chicken|mutton|rabbit|bacon|ham$|^ham|venison|goat|duck|bison|kangaroo|moose|hoglin|'
                    r'strider|mint_chops|centipede|bunfungus|escargot|minced|cuts$|chops$|fillet|shank|rib|loin|drumstick|'
                    r'patty|meat)')
R_ALCOHOL_CLASS = re.compile(r'(Booze|DrinkBlock|Wodka)')

COOK_TYPES = {'minecraft:smelting', 'minecraft:smoking', 'minecraft:campfire_cooking'}
CUT_TYPES = {'farmersdelight:cutting', 'create:cutting'}
BOWLS = {'minecraft:bowl', 'endersdelight:shulker_bowl'}

# Canned Goods 3 crossed its ids: what a can holds is what its name, its values and (mostly) its recipe
# say, not its id. canned_carrots is "Canned Pumpkin Pies" (2 pies' worth; the 2-carrot recipe also makes
# it, so carrots have no can of their own), canned_pumpkin_pie is "Canned Golden Carrots", canned_golden_carrot
# is a second golden carrot can with no recipe, and canned_mushrooms is "Canned Mushroom Stew" (2 stews'
# worth, made from 1 brown + 1 red mushroom). These seed the canned map by content, not by recipe input.
CANNED_SOURCE_FIX = {
    'canned_goods:canned_carrots': ['minecraft:pumpkin_pie'],
    'canned_goods:canned_pumpkin_pie': ['minecraft:golden_carrot'],
    'canned_goods:canned_golden_carrot': ['minecraft:golden_carrot'],
    'canned_goods:canned_mushrooms': ['minecraft:mushroom_stew'],
}
# Not food, but the canning recipes will need them: they get item_dict keys too.
EXTRA_IDS = ['canned_goods:empty_can', 'canned_goods:crushed_empty_can']


# ── data loading ─────────────────────────────────────────────────────────────

def load_export():
    need = ['foods.json', 'food_blocks.json', 'recipes.json']
    missing = [n for n in need if not (EXPORT / n).exists()]
    if missing:
        sys.exit(f'missing {", ".join(missing)} in {EXPORT} - run /dumpfood in game first')
    foods = json.loads((EXPORT / 'foods.json').read_text())['foods']
    blocks = json.loads((EXPORT / 'food_blocks.json').read_text())['blocks']
    recipes = json.loads((EXPORT / 'recipes.json').read_text())['recipes']
    return foods, blocks, recipes


def ingredient_items(raw):
    """Item ids and '#tags' named by one ingredient's JSON (lists and compound forms included)."""
    try:
        j = json.loads(raw)
    except (ValueError, TypeError):
        return []
    out = []

    def walk(x):
        if isinstance(x, dict):
            if isinstance(x.get('item'), str):
                out.append(x['item'])
            if isinstance(x.get('tag'), str):
                out.append('#' + x['tag'])
            for k in ('children', 'ingredients', 'values', 'base', 'items'):
                if k in x:
                    walk(x[k])
        elif isinstance(x, list):
            for y in x:
                walk(y)
    walk(j)
    return out


def recipe_links(recipes):
    """cooked output -> inputs, cut output -> inputs, canned item -> sources (Canned Goods '2x X + iron')."""
    cooked, cut, canned = defaultdict(set), defaultdict(set), defaultdict(set)
    for r in recipes:
        ins = [i for s in r.get('in', []) for i in ingredient_items(s)]
        outs = [o['id'] for o in r.get('out', []) if o.get('id')]
        if r['type'] in COOK_TYPES:
            for o in outs:
                cooked[o].update(ins)
        elif r['type'] in CUT_TYPES:
            for o in outs:
                cut[o].update(ins)
        if r['id'].startswith('canned_goods:') and outs and outs[0].startswith('canned_goods:canned_'):
            src = [i for i in ins if i != 'minecraft:iron_ingot']
            canned[outs[0]].update(src)
    return cooked, cut, canned


def parse_dict():
    """item_dict.js -> (source, key -> value with the last assignment winning, item id -> [keys]): the shared
    parser in tools/pack_paths.py."""
    src, key_val, by_id = pack_paths.parse_item_dict(DICT)
    return src, key_val, defaultdict(list, by_id)


def best_key(item_id, keys):
    ns, path = item_id.split(':', 1)
    for want in (path, f'{ns}_{path}'):
        if want in keys:
            return want
    with_path = sorted((k for k in keys if path in k), key=len)
    return with_path[0] if with_path else sorted(keys, key=len)[0]


def new_key(item_id, taken):
    ns, path = item_id.split(':', 1)
    path = path.replace('/', '_')
    for k in (path, f'{ns}_{path}'):
        if re.match(r'^[A-Za-z_]\w*$', k) and k not in taken:
            return k
    raise SystemExit(f'no free item_dict key for {item_id}')


def type_tag_key(t):
    return 't_food_' + t.replace('/', '_')


# ── classification ───────────────────────────────────────────────────────────

def excluded_reason(fid):
    if fid in EXCLUDE:
        return EXCLUDE[fid]
    for p, why in EXCLUDE_PREFIX.items():
        if fid.startswith(p):
            return why
    return None


def classify(f, cooked_from, cut_from, feast_servings):
    fid = f['id']
    ns, p = fid.split(':', 1)
    cls = f['classes'][0].rsplit('.', 1)[-1] if f.get('classes') else ''
    tags = set(f.get('tags', []))
    if fid in OVERRIDES:
        return OVERRIDES[fid]
    if ns == 'canned_goods' and p.startswith('canned_'):
        return 'canned'
    if 'enchanted_golden' in p:
        return 'golden/enchanted'
    if p.startswith('golden_') and ns in ('goldenfood', 'minecraft', 'immersive_weathering'):
        return 'golden'
    if fid in HAZARD:
        return 'hazard'
    if fid in SPECIAL:
        return 'special'
    if fid in feast_servings:
        return 'meal/feast'
    if R_ALCOHOL_CLASS.search(cls) or 'vinery:wine' in tags:
        return 'drink/alcohol'
    if R_INGREDIENT.search(p):
        return 'ingredient'
    if R_SOUP.search(p):
        return 'soup'
    if f.get('useAnim') == 'DRINK' or R_DRINK.search(p):
        return 'drink'
    if 'salad' in p:
        return 'salad'
    if 'pancake' in p:   # the pancake family, every mod (food_families.md 4.7)
        return 'sweet/pancake'
    if R_SWEET.search(p):
        return 'sweet'
    if R_HANDHELD.search(p) and f.get('remainder') not in BOWLS:
        return 'handheld'
    if R_EGG.search(p) and 'and_egg' not in p:
        return 'egg'
    if R_MEAL.search(p) or (f.get('remainder') in BOWLS):
        return 'meal'
    if R_BAKED.search(p):
        return 'baked'
    if R_DAIRY.search(p):
        return 'dairy'
    # raw / cooked, by group
    cooked = bool(R_COOKED.search(p)) or fid in cooked_from
    if R_FRUIT.search(p) or 'forge:fruits' in tags:
        group = 'fruit'
    elif R_FISH.search(p) or 'minecraft:fishes' in tags:
        group = 'fish'
    elif f.get('meat') or R_MEAT.search(p):
        group = 'meat'
    else:
        group = 'veggie'
    base = ('cooked/' if cooked else 'raw/') + group
    sliced = bool(R_SLICED.search(p)) or any(not i.startswith('#') and i != fid for i in cut_from.get(fid, ()))
    if sliced and group in ('meat', 'fish', 'veggie') and (base + '/sliced') in TYPES:
        # whole foods a cutting board also yields (minecraft:beef from a culture) aren't slices
        if R_SLICED.search(p):
            return base + '/sliced'
    return base


# ── food_types.json ──────────────────────────────────────────────────────────

README = [
    'Food type assignments: every food in exactly one type, as item_dict keys (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js).',
    'creativedelight creativeengineering/food_type_tags.js turns each type into #creativedelight:food_type/<type>; a nested type',
    '(raw/meat/sliced) is also in its parents (raw/meat, raw, and #creativedelight:food_type).',
    'Move an item by moving its key. python3 tools/food_catalog.py adds foods that are in no type yet and checks',
    'the rest; it never moves an item you placed. Reference: feature_writeup/food.md.',
    'excluded: edible items that are not food for tuning (never tagged). canned: can -> the foods it is made from',
    '(those land in #creativedelight:canned_source).',
]


def load_types():
    if not TYPES_JSON.exists():
        return None
    return json.loads(TYPES_JSON.read_text(), object_pairs_hook=OrderedDict)


def write_types(data):
    # one key list per line keeps diffs readable
    lines = ['{', '  "_readme": ' + json.dumps(data['_readme']) + ',', '  "types": {']
    tnames = list(data['types'].keys())
    for n, t in enumerate(tnames):
        keys = data['types'][t]
        body = ', '.join(json.dumps(k) for k in keys)
        lines.append(f'    {json.dumps(t)}: [{body}]' + (',' if n < len(tnames) - 1 else ''))
    lines.append('  },')
    lines.append('  "excluded": ' + json.dumps(data['excluded'], sort_keys=True) + ',')
    lines.append('  "canned": {')
    cnames = sorted(data['canned'].keys())
    for n, c in enumerate(cnames):
        lines.append(f'    {json.dumps(c)}: {json.dumps(data["canned"][c])}' + (',' if n < len(cnames) - 1 else ''))
    lines.append('  }')
    lines.append('}')
    TYPES_JSON.write_text('\n'.join(lines) + '\n')


def type_member_parts(data):
    """food_types.json data -> {namespace: {types: {type: [keys]}, canned: {can: [source keys]}}}. The
    creativeengineering part (always loaded) names every type; a can goes with its first source's namespace."""
    _, key_val, _ = pack_paths.parse_item_dict()
    parts = {ns: {'types': p} for ns, p in gen_out.split_lists(data['types'], key_val, 'creativeengineering').items()}
    for can in sorted(data['canned']):
        srcs = data['canned'][can]
        ns = gen_out.namespace_of(srcs[0], key_val) if srcs else 'creativeengineering'
        parts.setdefault(ns, {'types': {}}).setdefault('canned', {})[can] = srcs
    for p in parts.values():
        p.setdefault('canned', {})
    return parts


def write_type_members(check=False):
    """Member scripts from food_types.json as it is on disk. True when something differs (check) or changed."""
    data = json.loads(TYPES_JSON.read_text())
    changed, stale = gen_out.write_member_scripts('food_catalog', 'food_types', type_member_parts(data),
                                                  gen_out.INSTANCE / gen_out.DATA_ROOT, 'Food types', check)
    return gen_out.report_member_scripts('food_types', changed, stale, check)


# ── item_dict generated block ────────────────────────────────────────────────

BLOCK_START = '// ############################## Food mods (generated by tools/food_catalog.py) ##############################'
BLOCK_END = '// ===== end food mods (generated) ====='


def render_block(existing_block_entries, new_entries, tag_entries, comments=None):
    """existing/new: [(key, id)], tag_entries: [(key, '#tag')], comments: key -> trailing '  // ...'.
    Returns the block text."""
    items = sorted(set(existing_block_entries) | set(new_entries), key=lambda kv: (kv[1], kv[0]))
    out = [BLOCK_START,
           '// Food items from namespaces the dict had no entries for, plus the food type tag keys.',
           '// Re-run python3 tools/food_catalog.py to add new foods; keys already here never change.',
           '']
    comments = comments or {}
    by_ns = OrderedDict()
    for k, v in items:
        by_ns.setdefault(v.split(':', 1)[0], []).append((k, v))
    for ns, rows in by_ns.items():
        out.append(f'// ===== {ns} ({len(rows)}) =====')
        w = max(len(k) for k, _ in rows)
        for k, v in rows:
            out.append(f"i.{k.ljust(w)} = '{v}';" + comments.get(k, ''))
        out.append('')
    out.append('// ===== food type tags (creativedelight creativeengineering/food_type_tags.js) =====')
    w = max(len(k) for k, _ in tag_entries)
    for k, v in tag_entries:
        out.append(f"i.{k.ljust(w)} = '{v}';")
    out.append(BLOCK_END)
    return '\n'.join(out)


ROW_RE = r"^i\.(\w+)\s*=\s*'([^'#][^']*)';([ \t]*//.*)?$"


def block_entries(src):
    s, e = src.find(BLOCK_START), src.find(BLOCK_END)
    if s < 0 or e < 0:
        return []
    return [(m.group(1), m.group(2)) for m in re.finditer(ROW_RE, src[s:e], re.M)]


def block_comments(src):
    """Hand-added trailing comments in the generated block (key -> '  // ...'), so a rerun keeps them."""
    s, e = src.find(BLOCK_START), src.find(BLOCK_END)
    if s < 0 or e < 0:
        return {}
    return {m.group(1): m.group(3) for m in re.finditer(ROW_RE, src[s:e], re.M) if m.group(3)}


def write_dict_block(src, block):
    s, e = src.find(BLOCK_START), src.find(BLOCK_END)
    if s >= 0 and e >= 0:
        return src[:s] + block + src[e + len(BLOCK_END):]
    anchor = src.find('// ############################## Validation')
    if anchor < 0:
        raise SystemExit('item_dict.js: no Validation header to insert before')
    return src[:anchor] + block + '\n\n' + src[anchor:]


# ── main ─────────────────────────────────────────────────────────────────────

def all_type_paths():
    """every type plus every parent path it implies (raw/meat/sliced -> raw/meat, raw)."""
    out = []
    for t in TYPES:
        parts = t.split('/')
        for n in range(1, len(parts) + 1):
            q = '/'.join(parts[:n])
            if q not in out:
                out.append(q)
    return out


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('--check', action='store_true', help='report only; write nothing; exit 1 on problems')
    ap.add_argument('--members', action='store_true', help='only rewrite the member scripts from food_types.json')
    args = ap.parse_args()
    if args.members:
        write_type_members()
        return

    foods, blocks, recipes = load_export()
    food_ids = {f['id'] for f in foods}
    cooked_from, cut_from, canned_src = recipe_links(recipes)
    feast_servings = {b['serving'] for b in blocks if b.get('serving')} - {'farmersdelight:kelp_roll_slice'}
    src, key_val, by_id = parse_dict()
    taken = set(key_val)

    # canned: only cans that are food, with the crossed/missing recipes fixed by name
    canned_src = {c: set(s) for c, s in canned_src.items() if c in food_ids}
    canned_src.update({c: set(s) for c, s in CANNED_SOURCE_FIX.items()})

    # keys: existing where the dict has one, new ones for the rest
    key_of, new_entries = {}, []
    wanted_ids = sorted(food_ids | {s for v in canned_src.values() for s in v if not s.startswith('#')}
                        | set(canned_src) | set(EXTRA_IDS))
    for fid in wanted_ids:
        if by_id.get(fid):
            key_of[fid] = best_key(fid, by_id[fid])
        else:
            k = new_key(fid, taken)
            taken.add(k)
            key_of[fid] = k
            new_entries.append((k, fid))
    id_of_key = {k: v for k, v in key_val.items()}
    id_of_key.update({k: v for k, v in new_entries})

    data = load_types() or OrderedDict([('_readme', README), ('types', OrderedDict()), ('excluded', {}), ('canned', {})])
    data['_readme'] = README
    for t in TYPES:
        data['types'].setdefault(t, [])
    problems = []

    placed = {}
    for t, keys in data['types'].items():
        if t not in TYPES:
            problems.append(f'unknown type "{t}" in food_types.json (add it to TYPES in this tool first)')
        for k in keys:
            if k in placed:
                problems.append(f'{k} is in both "{placed[k]}" and "{t}"')
            placed[k] = t
            if k not in id_of_key:
                problems.append(f'{k} ("{t}") is not an item_dict key')
    excluded_ids = {id_of_key.get(k) for k in data['excluded']}
    placed_ids = {id_of_key.get(k) for k in placed}   # by id: a second dict key for a food mustn't re-place it

    added = []
    for f in foods:
        fid = f['id']
        k = key_of[fid]
        if fid in placed_ids or fid in excluded_ids:
            continue
        why = excluded_reason(fid)
        if why:
            data['excluded'][k] = why
            added.append((fid, 'excluded: ' + why))
            continue
        t = classify(f, cooked_from, cut_from, feast_servings)
        data['types'][t].append(k)
        placed[k] = t
        added.append((fid, t))

    # every placed key still a food in the export?
    for k, t in placed.items():
        fid = id_of_key.get(k)
        if fid and fid not in food_ids:
            problems.append(f'{k} -> {fid} ("{t}") is no longer edible / installed')

    # canned: can key -> source keys (from the recipes; name-matched where a recipe is crossed)
    canned = OrderedDict()
    for can, srcs in sorted(canned_src.items()):
        canned[key_of[can]] = sorted(key_of[s] for s in srcs if s in key_of)
    for k, v in data.get('canned', {}).items():   # hand edits win
        canned[k] = v
    data['canned'] = canned

    for t in data['types']:
        data['types'][t] = sorted(set(data['types'][t]))

    counts = {t: len(v) for t, v in data['types'].items()}
    print(f'{len(foods)} foods: {sum(counts.values())} typed, {len(data["excluded"])} excluded, '
          f'{len(new_entries)} new item_dict keys')
    for fid, t in added:
        print(f'  new  {fid:<55} -> {t}')
    for p in problems:
        print('  PROBLEM ' + p)

    if args.check:
        if added or new_entries:
            print(f'{len(added)} food(s) in no type and {len(new_entries)} without an item_dict key - run without --check')
        stale = write_type_members(check=True)
        sys.exit(1 if (problems or added or new_entries or stale) else 0)

    write_types(data)
    tag_entries = [(type_tag_key(t), '#' + TAG_ROOT + '/' + t) for t in all_type_paths()]
    tag_entries = [('t_food_type', '#' + TAG_ROOT)] + tag_entries + [('t_canned_source', '#' + CANNED_TAG)]
    block = render_block(block_entries(src), new_entries, tag_entries, block_comments(src))
    DICT.write_text(write_dict_block(src, block))
    print(f'wrote {TYPES_JSON.relative_to(INSTANCE)} and the generated block in {DICT.relative_to(INSTANCE)}')
    write_type_members()
    write_catalog(foods, blocks, recipes, data, id_of_key, cooked_from, cut_from)
    sys.exit(1 if problems else 0)


# ── catalog (feature_writeup/food_catalog.md + kubejs/exported/food/catalog.json) ──────────────

CATALOG_MD = INSTANCE / 'feature_writeup/food_catalog.md'
CATALOG_JSON = EXPORT / 'catalog.json'

# Effects Canned Goods applies in code on top of the food properties (javap of its procedures, 2026-09-28;
# every one runs in finishUsingItem). The dump only sees food properties, so these are listed by hand.
CANNED_CODE_EFFECTS = {
    'canned_goods:canned_golden_apples': 'regeneration III 5s, absorption II 120s',
    'canned_goods:canned_enchanted_apples': 'regeneration III 20s, resistance II 300s, fire_resistance II 300s, absorption V 120s',
    'canned_goods:canned_glow_berries': 'glowing II 3s',
    'canned_goods:canned_honey_bottles': 'clears poison',
    'canned_goods:canned_posionous_potatos': 'poison II 5s (always)',
    'canned_goods:canned_pufferfishs': 'hunger IV 15s, nausea II 15s, poison III 60s',
    'canned_goods:canned_rotten_flesh': 'hunger II 30s (always)',
    'canned_goods:canned_spider_eyes': 'poison II 5s',
    'canned_goods:canned_suspicious_stew': 'night_vision II, fire_resistance II, blindness II, saturation II, 11s each',
    'canned_goods:canned_chorus_fruits': 'random teleport (like chorus fruit)',
    'canned_goods:hot_hot_hotpot': 'regeneration II, speed II, fire_resistance II 75s, sets you on fire 5s',
    'canned_goods:soda': 'speed II 30s',
    'canned_goods:oculemonade': 'speed II 30s (reuses the soda procedure)',
    'canned_goods:reef_cola': 'water_breathing II 30s',
}
# Placed food the source cans are made from: total hunger / saturation points of one whole block.
PLACED_TOTALS = {
    'minecraft:cake': (14, 2.8, '7 bites of 2 hunger, 0.1 mod'),
    'minecraft:pumpkin_pie': (12, 7.2, '4 slices of create_central_kitchen:pumpkin_pie_slice'),
}
CANNED_RECIPE_NOTES = {
    'canned_goods:canned_carrots': 'id says carrots, name and values say 2 pumpkin pies; the 2-carrot recipe makes it too',
    'canned_goods:canned_pumpkin_pie': 'id says pumpkin pie, name, values and recipe say 2 golden carrots',
    'canned_goods:canned_golden_carrot': 'duplicate golden carrot can, no recipe',
    'canned_goods:canned_mushrooms': 'recipe is 1 brown + 1 red mushroom, values are 2 mushroom stews',
    'canned_goods:canned_beetroot_soup': 'named "Canned Bread" in its lang file',
    'canned_goods:canned_food': 'no recipe: butcher trade',
    'canned_goods:canned_cookies': 'flagged as meat',
}
R_COMPRESSED = re.compile(r'(crate|sack|basket|punnet|_block$)')
ROMAN = ['', '', ' II', ' III', ' IV', ' V', ' VI', ' VII', ' VIII', ' IX', ' X']


def sol_formula(n, s):
    """Spice of Life: Sweet Potato's own complexity for a food not in its config (FoodList.getComplexity)."""
    import math
    q = (n + n * s) / 2.0
    return q / 5.0 if q < 5 else 1 + 4 * math.log10(q - 4)


def fmt_num(x):
    return ('%.2f' % x).rstrip('0').rstrip('.') if isinstance(x, float) else str(x)


def fmt_effects(f):
    out = []
    for e in f.get('effects', []):
        name = (e['effect'] or '?').split(':')[-1]
        lvl = ROMAN[e['amplifier'] + 1] if e['amplifier'] + 1 < len(ROMAN) else f' {e["amplifier"] + 1}'
        s = f'{name}{lvl} {fmt_num(float(e["seconds"]))}s'
        if e['chance'] < 1:
            s += f' @{int(round(e["chance"] * 100))}%'
        out.append(s)
    return ', '.join(out)


def flags(f):
    return ''.join(c for c, on in (('M', f.get('meat')), ('A', f.get('alwaysEat')), ('F', f.get('fast')),
                                   ('D', f.get('useAnim') == 'DRINK')) if on)


def median(xs):
    xs = sorted(xs)
    if not xs:
        return 0
    m = len(xs) // 2
    return xs[m] if len(xs) % 2 else (xs[m - 1] + xs[m]) / 2


def md_cell(s):
    return str(s).replace('|', '/')


def write_catalog(foods, blocks, recipes, data, id_of_key, cooked_from, cut_from):
    from datetime import datetime
    by_id = {f['id']: f for f in foods}
    type_of = {}
    for t, keys in data['types'].items():
        for k in keys:
            if id_of_key.get(k):
                type_of[id_of_key[k]] = t
    canned_by = defaultdict(list)     # source id -> can ids
    for can_k, src_keys in data['canned'].items():
        for sk in src_keys:
            if id_of_key.get(sk) and id_of_key.get(can_k):
                canned_by[id_of_key[sk]].append(id_of_key[can_k])

    def label(fid):
        f = by_id.get(fid)
        if not f:
            return f'`{fid}`'
        return f'{f["name"]} ({f["nutrition"]}/{fmt_num(f["saturationModifier"])})'

    # ── recipe relations for the chains
    tags = {}
    tag_file = INSTANCE / 'kubejs/exported/tags/item_tags.json'
    if tag_file.exists():
        tags = json.loads(tag_file.read_text())
    compressed = defaultdict(set)
    uses = defaultdict(int)
    for r in recipes:
        ins = [ingredient_items(s) for s in r.get('in', [])]
        flat = set()
        for alts in ins:
            for a in alts:
                if a.startswith('#'):
                    flat.update(tags.get(a[1:], []))
                else:
                    flat.add(a)
        for x in flat:
            uses[x] += 1
        if r['type'] in ('minecraft:crafting_shaped', 'minecraft:crafting_shapeless') and len(ins) >= 4:
            singles = [a[0] for a in ins if len(a) == 1 and not a[0].startswith('#')]
            out = r['out'][0] if r.get('out') else None
            if len(singles) == len(ins) and len(set(singles)) == 1 and out and out['n'] == 1 \
                    and (R_COMPRESSED.search(out['id'].split(':')[1]) or out['id'] == 'minecraft:melon'):
                compressed[singles[0]].add(f'{out["id"]} ({len(ins)}x)')

    def cooks(x):
        return sorted(o for o, ins in cooked_from.items() if x in ins and o in by_id)

    def cuts(x):
        return sorted(o for o, ins in cut_from.items() if x in ins and o in by_id)

    bases = [fid for fid, t in type_of.items() if t in ('raw/veggie', 'raw/fruit', 'raw/meat', 'raw/fish')]
    extra_bases = set()
    for o, ins in list(cooked_from.items()) + list(cut_from.items()):
        if type_of.get(o, '').startswith(('raw/', 'cooked/')):
            for x in ins:
                if not x.startswith('#') and x not in by_id and not x.startswith('creativeengineering:culture_') \
                        and 'wild_' not in x and 'dough' not in x:
                    extra_bases.add(x)
    extra_bases.add('minecraft:egg')

    # ── rows
    lines = []
    w = lines.append
    stamp = datetime.fromtimestamp((EXPORT / 'foods.json').stat().st_mtime).strftime('%Y-%m-%d %H:%M')
    w('# Food catalog (generated)')
    w('')
    w(f'Generated by `python3 tools/food_catalog.py` from the `/dumpfood` export of {stamp}: {len(foods)} edible items, '
      f'{sum(len(v) for v in data["types"].values())} typed, {len(data["excluded"])} excluded. Don\'t edit this file: '
      'types live in [food_types.json](../kubejs/generation_scripts/food/food_types.json), values in the mods (and later the '
      'tuning config). How it all fits together: [food.md](food.md).')
    w('')
    w('**Columns.** Hunger: nutrition, in half shanks. Mod: saturation modifier. Sat: saturation points added '
      '(hunger x mod x 2, never more than the food bar). SoL: Spice of Life complexity now, what one eat adds to food '
      'diversity before the age penalty (`*` = set in the SoL config, otherwise its own formula). Canned: the Canned '
      'Goods can made from it. Flags: M meat, A always edible, F fast (16 ticks instead of 32), D drink animation. '
      'Effects: level, seconds, and the chance when under 100%. Leaves: item handed back.')
    w('')
    w('## Summary by type')
    w('')
    w('| Type | Tag (`#creativedelight:food_type/...`) | Foods | Hunger min / median / max | Mod min / median / max | SoL median | Has a can |')
    w('|---|---|---:|---|---|---:|---:|')
    for t, desc in TYPES.items():
        fs = [by_id[i] for i, tt in type_of.items() if tt == t and i in by_id]
        if not fs:
            w(f'| {t} | `{t}` | 0 | | | | |')
            continue
        n = [f['nutrition'] for f in fs]
        s = [f['saturationModifier'] for f in fs]
        c = [f['sol']['complexity'] for f in fs if f.get('sol')]
        w(f'| {t} | `{t}` | {len(fs)} | {min(n)} / {fmt_num(float(median(n)))} / {max(n)} | '
          f'{fmt_num(min(s))} / {fmt_num(float(median(s)))} / {fmt_num(max(s))} | {fmt_num(float(median(c)))} | '
          f'{sum(1 for f in fs if canned_by.get(f["id"]))} |')
    w('')

    for t, desc in TYPES.items():
        fs = sorted((by_id[i] for i, tt in type_of.items() if tt == t and i in by_id), key=lambda f: (f['name'], f['id']))
        w(f'## {t} ({len(fs)})')
        w('')
        w(f'{desc}. Tag `#creativedelight:food_type/{t}`, key `i.{type_tag_key(t)}`.')
        w('')
        if not fs:
            continue
        w('| Food | Id | Hunger | Mod | Sat | Flags | Effects | Leaves | SoL | Canned |')
        w('|---|---|---:|---:|---:|---|---|---|---:|---|')
        for f in fs:
            sol = f.get('sol') or {}
            solc = fmt_num(sol.get('complexity', 0.0)) + ('*' if sol.get('fromConfig') else '')
            eff = fmt_effects(f)
            if f['id'] in CANNED_CODE_EFFECTS:
                eff = (eff + '; ' if eff else '') + 'code: ' + CANNED_CODE_EFFECTS[f['id']]
            cans = ', '.join(c.split(':')[1] for c in canned_by.get(f['id'], []))
            w(f'| {md_cell(f["name"])} | `{f["id"]}` | {f["nutrition"]} | {fmt_num(f["saturationModifier"])} | '
              f'{fmt_num(f["saturation"])} | {flags(f)} | {md_cell(eff)} | {(f.get("remainder") or "").split(":")[-1]} | '
              f'{solc} | {cans} |')
        w('')

    # ── chains
    w('## Base ingredients: recipe chains')
    w('')
    w('One row per raw base (every raw/* food that isn\'t a cut, plus the inedible raw inputs of cooking and cutting '
      'recipes). Compressed: 9 (or 4) into one storage block. Cooked: furnace, smoker or campfire. Sliced: cutting '
      'board or Create saw. Sliced, cooked: a cooked slice, from either direction. Values are hunger/mod. Used in: '
      'recipes that take it directly or through a tag.')
    w('')
    w('| Base | Type | Compressed | Cooked | Sliced | Sliced, cooked | Canned | Used in |')
    w('|---|---|---|---|---|---|---|---:|')
    rows = []
    for b in sorted(set(bases) | extra_bases, key=lambda x: (type_of.get(x, 'zz'), x)):
        cooked = cooks(b)
        sliced = [x for x in cuts(b) if type_of.get(x, '').startswith('raw/')]
        sliced_cooked = sorted(set([x for c in cooked for x in cuts(c)] + [x for s in sliced for x in cooks(s)]))
        cans = sorted(set(canned_by.get(b, []) + [c for x in cooked for c in canned_by.get(x, [])]))
        if b in extra_bases and not (cooked or sliced):
            continue
        rows.append((b, cooked, sliced, sliced_cooked, cans))
    for b, cooked, sliced, sliced_cooked, cans in rows:
        w(f'| {label(b)} | {type_of.get(b, "not edible")} | {", ".join(sorted(compressed.get(b, []))) } | '
          f'{", ".join(label(x) for x in cooked)} | {", ".join(label(x) for x in sliced)} | '
          f'{", ".join(label(x) for x in sliced_cooked)} | {", ".join(c.split(":")[1] for c in cans)} | {uses.get(b, 0)} |')
    w('')

    # ── canned parity
    w('## Canned Goods: can vs what goes in')
    w('')
    w('Every can is 2 of its source plus an iron ingot, and gives the empty can back. Parity means a can is worth '
      'what went into it: "2x source" is the hunger and saturation points of the two sources eaten one after the '
      'other. Sat pts are uncapped here, so values above 20 show how far past the food bar a can goes.')
    w('')
    w('| Can | Source | Can hunger / mod / sat pts | 2x source hunger / sat pts | Hunger ratio | Sat ratio | Can effects | Source effects | Notes |')
    w('|---|---|---|---|---:|---:|---|---|---|')
    for can_k in sorted(data['canned']):
        can = id_of_key.get(can_k)
        cf = by_id.get(can)
        if not cf:
            continue
        srcs = [id_of_key.get(k) for k in data['canned'][can_k]]
        sh, ss, seff = 0, 0.0, []
        for s in srcs:
            if s in by_id:
                sh += by_id[s]['nutrition']
                ss += by_id[s]['saturation']
                seff.append(fmt_effects(by_id[s]))
            elif s in PLACED_TOTALS:
                sh += PLACED_TOTALS[s][0]
                ss += PLACED_TOTALS[s][1]
                seff.append(PLACED_TOTALS[s][2])
        if len(srcs) == 1:
            sh, ss = sh * 2, ss * 2
        can_sat = cf['nutrition'] * cf['saturationModifier'] * 2
        ceff = fmt_effects(cf)
        if can in CANNED_CODE_EFFECTS:
            ceff = (ceff + '; ' if ceff else '') + 'code: ' + CANNED_CODE_EFFECTS[can]
        hr = fmt_num(cf['nutrition'] / sh) if sh else '-'
        sr = fmt_num(can_sat / ss) if ss else '-'
        w(f'| {cf["name"]} | {", ".join(label(s) if s in by_id else "`" + s + "`" for s in srcs)} | '
          f'{cf["nutrition"]} / {fmt_num(cf["saturationModifier"])} / {fmt_num(can_sat)} | {sh} / {fmt_num(ss)} | {hr} | {sr} | '
          f'{md_cell(ceff)} | {md_cell("; ".join(x for x in seff if x))} | {CANNED_RECIPE_NOTES.get(can, "")} |')
    for can in sorted(i for i in by_id if i.startswith('canned_goods:') and type_of.get(i) in ('canned', 'drink')):
        if any(id_of_key.get(k) == can for k in data['canned']):
            continue
        cf = by_id[can]
        ceff = fmt_effects(cf)
        if can in CANNED_CODE_EFFECTS:
            ceff = (ceff + '; ' if ceff else '') + 'code: ' + CANNED_CODE_EFFECTS[can]
        w(f'| {cf["name"]} | (none) | {cf["nutrition"]} / {fmt_num(cf["saturationModifier"])} / '
          f'{fmt_num(cf["nutrition"] * cf["saturationModifier"] * 2)} | | | | {md_cell(ceff)} | | '
          f'{CANNED_RECIPE_NOTES.get(can, "own recipe, not a canned food")} |')
    w('')

    # ── placed food
    w('## Placed food (cakes, pies, feasts)')
    w('')
    w('Not edible as items; eaten or served from the placed block. Display Delight\'s 100 display blocks are left out.')
    w('')
    w('| Block | Id | Kind | Serves |')
    w('|---|---|---|---|')
    for b in sorted(blocks, key=lambda b: b['id']):
        if b['mod'] == 'displaydelight' or 'fantasyfurniture' in b['id'] or b['mod'] == 'oldguns':
            continue
        kind = b['blockClasses'][0].rsplit('.', 1)[-1] if b.get('blockClasses') else ''
        serves = ''
        if b.get('serving'):
            serves = f'{b.get("servings", "?")} x {label(b["serving"])}'
        elif b.get('slice'):
            serves = f'4 x {label(b["slice"])}'
        elif 'Cake' in kind:
            serves = '7 bites'
        w(f'| {md_cell(b["name"])} | `{b["id"]}` | {kind} | {serves} |')
    w('')

    w('## Excluded (edible in code, not food for tuning)')
    w('')
    for k, why in sorted(data['excluded'].items()):
        w(f'- `{id_of_key.get(k, k)}`: {why}')
    w('')
    CATALOG_MD.write_text('\n'.join(lines))

    cat = []
    for f in foods:
        cat.append({'id': f['id'], 'type': type_of.get(f['id']), 'nutrition': f['nutrition'],
                    'saturationModifier': f['saturationModifier'], 'saturation': f['saturation'],
                    'flags': flags(f), 'effects': f.get('effects', []),
                    'codeEffects': CANNED_CODE_EFFECTS.get(f['id']), 'remainder': f.get('remainder'),
                    'sol': f.get('sol'), 'solFormula': round(sol_formula(f['nutrition'], f['saturationModifier']), 4),
                    'canned': canned_by.get(f['id'], []), 'class': (f.get('classes') or [''])[0]})
    CATALOG_JSON.write_text(json.dumps({'foods': cat}, indent=1))
    print(f'wrote {CATALOG_MD.relative_to(INSTANCE)} and {CATALOG_JSON.relative_to(INSTANCE)}')


if __name__ == '__main__':
    main()
