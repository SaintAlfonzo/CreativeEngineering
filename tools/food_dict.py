#!/usr/bin/env python3
"""Food dict (global.food, alias f): lint, coverage and reverse index for development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/food_dict.js.

The food dict is a mask over the general dict (creative_delight.md 13): one line per food, `f.<key> = i.<general key>;`,
key `<category>[_<form>][_<state>]_<name>` from the lists in _lib/food_grammar.js. The file is kept by hand; this tool
only checks it and proposes keys for foods it lacks, using the same rules that seeded it.

What counts as a food: every edible item and food block in the /dumpfood dump, every drinkable item, and the kitchen
ingredients in INGREDIENTS (flour, dough, raw grains, eggs, salt, tea leaves: the non-edible half of cooking), minus
removed and merged items (food_types.json `excluded`, removed_items.js, item_merges.js) and potions.

Category and form come from tools/food_families.py classify() (FAMILY_KEY maps a family to its key parts); the name is
the id path less the words the category, form and state already say.

    python3 tools/food_dict.py            lint, write the reverse index, report foods with no key (exit 1 if any)
    python3 tools/food_dict.py --check    lint only; exit 1 on any problem
    python3 tools/food_dict.py --append   also append the proposed lines to food_dict.js, to sort into place
    python3 tools/food_dict.py --seed     write food_dict.js from scratch (refuses if it exists; --force to overwrite)

Source data: kubejs/exported/food/{catalog,foods,food_blocks,drinks}.json (/dumpfood), item_dict.js, food_types.json,
removed_items.js, item_merges.js. Output: kubejs/generation_scripts/food/food_dict_index.json (item id -> key).
"""
import argparse
import json
import re
import sys
from collections import OrderedDict, defaultdict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import pack_paths as P  # noqa: E402
import food_catalog as fc  # noqa: E402  (item_dict parsing)
import food_families as ff  # noqa: E402  (family per food)

INSTANCE = Path(__file__).resolve().parent.parent
LIB = P.STARTUP_LIB
GRAMMAR = LIB / 'food_grammar.js'
DICT = LIB / 'food_dict.js'
INDEX = INSTANCE / 'kubejs/generation_scripts/food/food_dict_index.json'
EXPORT = INSTANCE / 'kubejs/exported/food'
APPEND_MARK = '\t// ---- proposed by tools/food_dict.py: sort these into their section'

# Drinkable items that are not food.
# Fantasy Furniture's food platters are decoration blocks.
NOT_FOOD = {'minecraft:potion', 'minecraft:splash_potion', 'minecraft:lingering_potion', 'botania:brew_flask',
            'botania:brew_vial', 'potionbundles:potion_bundle', 'potionbundles:splash_potion_bundle',
            'potionbundles:lingering_potion_bundle'}

# Kitchen ingredients that are not edible themselves (audit 2026-10-25: inputs of food recipes and the food mods'
# item lists). id -> key.
INGREDIENTS = {
    # grain and flour
    'minecraft:wheat': 'grain_wheat', 'farmersdelight:rice': 'grain_rice', 'farmersdelight:rice_panicle': 'grain_rice_panicle',
    'vintagedelight:oat': 'grain_oat', 'ends_delight:ender_pearl_grain': 'grain_ender_pearl',
    'create:wheat_flour': 'grain_flour_wheat', 'pneumaticcraft:wheat_flour': 'grain_flour_wheat_pneumaticcraft',
    'tmted:wheat_flour': 'grain_flour_wheat_tmted', 'creativeengineering:cornmeal': 'grain_flour_corn',
    # dough and bases
    'create:dough': 'dough_wheat', 'creativeengineering:cookie_dough': 'dough_cookie',
    'create_confectionery:gingerdough': 'dough_ginger', 'pneumaticcraft:sourdough': 'dough_sourdough',
    'create_factory:uncooked_waffle': 'dough_waffle', 'create_factory:cake_paste': 'dough_cake_paste',
    'createaddition:cake_base': 'dough_cake_base', 'createaddition:cake_base_baked': 'cake_cooked_base',
    # eggs
    'minecraft:egg': 'egg_chicken', 'minecraft:turtle_egg': 'egg_turtle', 'untitledduckmod:duck_egg': 'egg_duck',
    'untitledduckmod:goose_egg': 'egg_goose', 'alexsmobs:emu_egg': 'egg_emu', 'alexsmobs:crocodile_egg': 'egg_crocodile',
    'alexsmobs:terrapin_egg': 'egg_terrapin', 'quark:egg_parrot_red_blue': 'egg_parrot_red_blue',
    'quark:egg_parrot_blue': 'egg_parrot_blue', 'quark:egg_parrot_green': 'egg_parrot_green',
    'quark:egg_parrot_yellow_blue': 'egg_parrot_yellow_blue', 'quark:egg_parrot_grey': 'egg_parrot_grey',
    'ends_delight:non_hatchable_dragon_egg': 'egg_dragon', 'vintagedelight:golden_egg': 'golden_egg',
    # dairy
    'brewinandchewin:flaxen_cheese_wheel': 'dairy_wheel_flaxen_cheese',
    'brewinandchewin:unripe_flaxen_cheese_wheel': 'dairy_wheel_unripe_flaxen_cheese',
    'brewinandchewin:scarlet_cheese_wheel': 'dairy_wheel_scarlet_cheese',
    'brewinandchewin:unripe_scarlet_cheese_wheel': 'dairy_wheel_unripe_scarlet_cheese',
    'minecraft:milk_bucket': 'dairy_milk_bucket', 'farmersdelight:milk_bottle': 'dairy_milk_bottle',
    # salt, sugar, spice
    'salt:salt': 'spice_salt', 'vintagedelight:salt_dust': 'spice_salt_dust', 'vintagedelight:salt': 'spice_salt_vintagedelight',
    'minecraft:sugar': 'spice_sugar', 'creativeengineering:powdered_sugar': 'spice_powdered_sugar',
    'supplementaries:sugar_cube': 'spice_sugar_cube', 'minecraft:cocoa_beans': 'spice_cocoa_beans',
    'create_confectionery:crushed_cocoa': 'spice_crushed_cocoa', 'create_confectionery:cocoa_powder': 'spice_cocoa_powder',
    'neapolitan:vanilla_pods': 'spice_vanilla_pods', 'delightful:matcha': 'spice_matcha',
    # fats, sauces, toppings
    'create_confectionery:cocoa_butter': 'sauce_cocoa_butter', 'delightful:animal_oil_bottle': 'sauce_animal_oil',
    'immersive_weathering:tallow': 'sauce_tallow', 'tmted:tomato_sauce': 'sauce_tomato_tmted',
    'creativeengineering:frosting': 'sauce_frosting',
    # whole produce that is cut before eating, and raw produce that is only cooked with
    'minecraft:melon': 'fruit_melon', 'minecraft:pumpkin': 'fruit_pumpkin', 'fruitsdelight:pineapple': 'fruit_pineapple',
    'fruitsdelight:durian': 'fruit_durian', 'fruitsdelight:hamimelon': 'fruit_hamimelon', 'delightful:cantaloupe': 'fruit_cantaloupe',
    'delightful:mini_melon': 'fruit_mini_melon', 'neapolitan:banana_bunch': 'fruit_banana_bunch',
    'oceanic_delight:sea_grape_fruit': 'fruit_sea_grape', 'farmersrespite:rose_hips': 'fruit_rose_hips',
    'minecraft:popped_chorus_fruit': 'fruit_cooked_chorus', 'ends_delight:dried_chorus_flower': 'fruit_dried_chorus_flower',
    'veggiesdelight:garlic': 'veggie_garlic', 'veggiesdelight:mature_dandelion': 'veggie_dandelion',
    'minecraft:kelp': 'veggie_kelp', 'minecraft:sea_pickle': 'veggie_sea_pickle', 'neapolitan:adzuki_beans': 'veggie_adzuki_beans',
    'upgrade_aquatic:blue_pickerelweed': 'veggie_blue_pickerelweed', 'upgrade_aquatic:purple_pickerelweed': 'veggie_purple_pickerelweed',
    'minecraft:brown_mushroom': 'mushroom_brown', 'minecraft:red_mushroom': 'mushroom_red',
    'minecraft:crimson_fungus': 'mushroom_crimson_fungus', 'minecraft:warped_fungus': 'mushroom_warped_fungus',
    'fungidelight:morel_mushroom': 'mushroom_morel', 'fungidelight:inky_cap_mushroom': 'mushroom_inky_cap',
    'quark:glow_shroom': 'mushroom_glow_shroom',
    # drink makings
    'farmersrespite:green_tea_leaves': 'drink_leaf_green_tea', 'farmersrespite:yellow_tea_leaves': 'drink_leaf_yellow_tea',
    'farmersrespite:black_tea_leaves': 'drink_leaf_black_tea', 'rusticdelight:coffee_beans': 'drink_coffee_beans',
    'rusticdelight:roasted_coffee_beans': 'drink_roasted_coffee_beans',
    # whole feasts that are not in the food block dump (they are cooked, then placed)
    'nethersdelight:stuffed_hoglin': 'feast_stuffed_hoglin', 'nethersdelight:raw_stuffed_hoglin': 'feast_raw_stuffed_hoglin',
    'endersdelight:stuffed_shulker': 'feast_stuffed_shulker', 'oceanic_delight:stuffed_pufferfish': 'feast_stuffed_pufferfish',
    'oceanic_delight:paella_plate': 'feast_paella',
    'create_confectionery:full_chocolate_bar': 'candy_full_chocolate_bar',
    'create_confectionery:full_black_chocolate_bar': 'candy_full_black_chocolate_bar',
    'create_confectionery:full_white_chocolate_bar': 'candy_full_white_chocolate_bar',
    'create_confectionery:full_ruby_chocolate_bar': 'candy_full_ruby_chocolate_bar',
    'farmersdelight:rotten_tomato': 'hazard_rotten_tomato',
}

# Seed decisions for foods the rules name badly. id -> key.
OVERRIDES = {
    'minecraft:cookie': 'cookie_chocolate_chip', 'minecraft:cake': 'cake_plain', 'minecraft:bread': 'grain_bread',
    'minecraft:apple': 'fruit_apple', 'minecraft:golden_apple': 'golden_apple',
    'minecraft:enchanted_golden_apple': 'golden_enchanted_apple', 'minecraft:golden_carrot': 'golden_carrot',
    'farmersdelight:cooked_rice': 'grain_cooked_rice', 'minecraft:honey_bottle': 'spice_honey',
    'vintagedelight:pickle': 'veggie_pickled_cucumber', 'vintagedelight:pickled_egg': 'egg_pickled',
    'vintagedelight:raw_oats': 'grain_oats', 'farmersdelight:cake_slice': 'cake_slice_plain',
    'create_things_and_misc:slime_cake': 'cake_slime', 'splash_milk:milk_bottle': 'dairy_milk_bottle_splash',
}

# family -> (category, form). None = take the form from the name (FORM_WORDS).
FAMILY_KEY = {
    'fruit/whole': ('fruit', ''), 'fruit/berry': ('fruit', ''), 'fruit/nut': ('nut', ''), 'fruit/slice': ('fruit', 'slice'),
    'fruit/cooked': ('fruit', ''),
    'veggie/raw': ('veggie', ''), 'veggie/raw_cut': ('veggie', None), 'veggie/cooked': ('veggie', ''),
    'veggie/cooked_cut': ('veggie', None),
    'meat/raw': ('meat', ''), 'meat/raw_cut': ('meat', None), 'meat/cooked': ('meat', ''), 'meat/cooked_cut': ('meat', None),
    'fish/raw': ('fish', ''), 'fish/raw_cut': ('fish', None), 'fish/cooked': ('fish', ''), 'fish/cooked_cut': ('fish', None),
    'egg/cooked': ('egg', ''), 'egg/dish': ('egg', ''), 'dairy': ('dairy', None),
    'bread/loaf': ('grain', ''), 'bread/slice': ('grain', 'slice'), 'bread/flat': ('grain', ''),
    'ingredient/dough': ('dough', ''), 'ingredient/sauce': ('sauce', ''), 'ingredient/other': ('ingredient', None),
    'preserve/jam': ('fruit', 'jam'), 'preserve/spread': ('sauce', ''), 'preserve/pickle': ('veggie', ''),
    'jello': ('dessert', ''), 'dessert/pudding': ('dessert', ''), 'dessert/other': ('dessert', ''), 'frozen/popsicle': ('dessert', ''), 'frozen/shaved_ice': ('dessert', ''),
    'frozen/ice_cream': ('ice_cream', ''),
    'pie/whole': ('pie', ''), 'pie/slice': ('pie', 'slice'), 'pie/small': ('pie', ''),
    'cake/whole': ('cake', ''), 'cake/slice': ('cake', 'slice'), 'cake/muffin': ('cake', ''), 'dessert/brownie': ('cake', ''),
    'cookie': ('cookie', ''),
    'dessert/waffle': ('pastry', ''), 'dessert/roll': ('pastry', ''), 'dessert/pastry': ('pastry', ''), 'dessert/pancake': ('pastry', ''),
    'candy/bar': ('candy', ''), 'candy/piece': ('candy', ''), 'candy/coated': ('candy', ''), 'candy/marshmallow': ('candy', ''),
    'sandwich/square': ('sandwich', 'square'), 'sandwich/roll': ('sandwich', 'roll'), 'sandwich/burger': ('sandwich', 'burger'),
    'sandwich/wrap': ('sandwich', 'wrap'), 'sandwich/toast': ('sandwich', 'toast'),
    'skewer': ('dish', ''), 'roll': ('dish', ''), 'stuffed': ('dish', ''), 'pizza/whole': ('dish', ''),
    'pizza/slice': ('dish', 'slice'), 'quiche/whole': ('dish', ''), 'quiche/slice': ('dish', 'slice'),
    'lasagna/whole': ('dish', ''), 'lasagna/serving': ('dish', ''), 'snack': ('snack', ''), 'salad': ('salad', ''),
    'soup/soup': ('soup', ''), 'soup/stew': ('soup', ''),
    'meal/rice': ('dish', ''), 'meal/pasta': ('dish', ''), 'meal/fried': ('dish', ''), 'meal/plate': ('dish', ''),
    'feast/whole': ('feast', ''), 'feast/serving': ('feast', 'serving'),
    'drink/juice': ('fruit', 'juice'), 'drink/tea': ('drink', ''), 'drink/coffee': ('drink', ''), 'drink/milk': ('drink', ''),
    'drink/soda': ('drink', ''), 'drink/other': ('drink', ''),
    'alcohol/wine': ('alcohol', ''), 'alcohol/brew': ('alcohol', ''), 'alcohol/spirit': ('alcohol', ''),
    'alcohol/mixed': ('alcohol', ''),
    'golden': ('golden', ''), 'golden/enchanted': ('golden', 'enchanted'), 'can': ('can', ''), 'hazard': ('hazard', ''),
    ff.NOFAM: ('dish', ''), ff.UNSORTED: ('dish', ''),
}
COOKED_FAMILIES = {'fruit/cooked', 'veggie/cooked', 'veggie/cooked_cut', 'meat/cooked', 'meat/cooked_cut', 'fish/cooked',
                   'fish/cooked_cut'}
# Words in a path that give a cut form (families with form None).
FORM_WORDS = OrderedDict([
    ('slice', ('slice', 'slices', 'sliced')), ('diced', ('diced', 'chopped', 'cubes', 'cubed')),
    ('ground', ('minced', 'mince', 'ground')), ('cut', ('cuts', 'cut')), ('chop', ('chop', 'chops')),
    ('fillet', ('fillet', 'fillets')), ('strip', ('strip', 'strips')), ('leaf', ('leaf', 'leaves')),
    ('patty', ('patty', 'patties')), ('wedge', ('wedge', 'wedges')), ('wheel', ('wheel',)),
])
STATE_WORDS = {'dried': ('dried',), 'smoked': ('smoked',), 'pickled': ('pickled',), 'fermented': ('fermented',)}
COOKED_WORDS = ('cooked', 'baked', 'roasted')
# A cooked food whose name says how (boiled corn, glazed porkchop) keeps the word and gets no cooked state.
HOW_WORDS = ('boiled', 'grilled', 'glazed', 'fried', 'steamed', 'toasted', 'seared', 'popped', 'grilledghast')
MUSHROOM_WORDS = ('mushroom', 'fungus', 'morel', 'truffle', 'shroom', 'inky')
NUT_WORDS = ('acorn', 'peanut', 'walnut', 'almond', 'pecan', 'hazelnut', 'chestnut', 'cashew', 'pistachio')
# Words the category already says (dropped from the name while something is left).
CATEGORY_WORDS = {
    'mushroom': ('mushroom',), 'cake': ('cake',), 'pie': ('pie',), 'cookie': ('cookie', 'cookies'), 'soup': ('soup',),
    'salad': ('salad',), 'sandwich': ('sandwich',), 'ice_cream': ('ice', 'cream'), 'golden': ('golden', 'enchanted'),
    'can': ('canned',), 'feast': ('block', 'feast'), 'dough': ('dough',), 'meat': ('meat',), 'sauce': ('sauce',), 'egg': ('egg', 'eggs'), 'fruit': ('jam', 'jelly', 'juice'),
    'drink': ('juice',),
}
CONTAINER_WORDS = ('bowl', 'of', 'plate', 'glass', 'mug', 'bottle', 'serving', 'raw')
LINK_WORDS = ('with', 'and', 'of')
# Owner order of the namespace that keeps a bare key when two items want the same one; the other gets _<namespace>.
FIRST = ['minecraft', 'farmersdelight', 'creativeengineering']


# ---------------------------------------------------------------- grammar and file parsing

def grammar():
    src = GRAMMAR.read_text()
    out = {}
    for part in ('categories', 'forms', 'states'):
        m = re.search(part + r":\s*\[([^\]]*)\]", src)
        if not m:
            sys.exit(f'{GRAMMAR.name}: no {part} list')
        out[part] = re.findall(r"'([a-z0-9_]+)'", m.group(1))
    return out


def parse_key(key, g):
    """Same as global.subdict.parse in food_grammar.js."""
    def take(rest, words):
        hits = [w for w in words if rest.startswith(w + '_')]
        return max(hits, key=len) if hits else None
    cat = take(key, g['categories'])
    if cat is None:
        return None
    rest = key[len(cat) + 1:]
    form = take(rest, g['forms'])
    if form:
        rest = rest[len(form) + 1:]
    state = take(rest, g['states'])
    if state:
        rest = rest[len(state) + 1:]
    if not re.fullmatch(r'[a-z0-9]+(_[a-z0-9]+)*', rest):
        return None
    return dict(category=cat, form=form, state=state, name=rest)


LINE = re.compile(r"^\s*f\.(\w+)\s*=\s*(.+?);?\s*(//.*)?$")


def read_dict():
    """food_dict.js -> list of (line number, food key, right-hand side)."""
    if not DICT.exists():
        return []
    out = []
    for n, line in enumerate(DICT.read_text().splitlines(), 1):
        m = LINE.match(line)
        if m:
            out.append((n, m.group(1), m.group(2).strip().rstrip(';').strip()))
    return out


# ---------------------------------------------------------------- what is a food

def excluded_ids(key_val):
    out = set()
    types = json.loads((INSTANCE / 'kubejs/generation_scripts/food/food_types.json').read_text())
    out |= {key_val.get(k) for k in types['excluded']}
    removed = (LIB / 'removed_items.js').read_text()
    body = removed[removed.index('global.removedItems'):]
    body = body[:body.index(']')]
    out |= {key_val.get(k) for k in re.findall(r'i\.(\w+)', body)}
    # merge pairs: the pack's list (dict keys), and Creative Delight's swap lists (ids; item_merges.md 1.1, 6)
    p = LIB / 'item_merges.js'
    out |= {key_val.get(k) for k in re.findall(r'\[\s*i\.(\w+)\s*,\s*i\.\w+\s*\]', p.read_text())}
    swaps = INSTANCE / 'development/creativeDelight/src/main/resources/data/creativedelight/swaps'
    for f in sorted(swaps.glob('merges_*.json')):
        out |= {old for olds in json.loads(f.read_text()).get('merges', {}).values() for old in olds}
    return out - {None}


def food_rows():
    """item id -> family row for every food (edible, food block, drink), plus the ingredients."""
    cat = {c['id']: c for c in ff.load_json(EXPORT / 'catalog.json', 'foods')}
    foods = ff.load_json(EXPORT / 'foods.json', 'foods')
    blocks = ff.load_json(EXPORT / 'food_blocks.json', 'blocks')
    rows = ff.build_rows(cat, foods, blocks)
    for d in ff.load_json(EXPORT / 'drinks.json', 'drinks'):
        if d['id'] not in rows:
            name = ff.clean(d.get('name') or d['id'])
            rows[d['id']] = dict(id=d['id'], name=name, n=name.lower(), p=d['id'].split(':', 1)[1],
                                 mod=d['id'].split(':', 1)[0], t='drink', whole=False, food=None)
    return rows


# ---------------------------------------------------------------- naming

def words_of(path):
    path = path.replace('grapejuice', 'grape_juice')   # Vinery
    return [w for w in re.split(r'[_/]', path) if w]


def drop(words, gone):
    left = [w for w in words if w not in gone]
    return left if left else words


def propose(r, fam):
    """Family row -> (key parts) without collision handling: (category, form, state, name words)."""
    cat, form = FAMILY_KEY.get(fam, ('dish', ''))
    w = words_of(r['p'])
    state = ''
    if cat == 'veggie' and any(x in r['p'] for x in MUSHROOM_WORDS):
        cat = 'mushroom'
    if cat == 'fruit' and any(x in r['p'] for x in NUT_WORDS):
        cat = 'nut'
    if cat == 'dough' and any(x in r['p'] for x in ('pasta', 'noodle')):
        cat = 'grain'
    if form is None:
        form = ''
        for f, ws in FORM_WORDS.items():
            if any(x in w for x in ws):
                form, w = f, drop(w, ws)
                break
    elif form:
        w = drop(w, FORM_WORDS.get(form, (form,)))
    for s, ws in STATE_WORDS.items():
        if any(x in w for x in ws):
            state, w = s, drop(w, ws)
            break
    if fam == 'preserve/pickle':
        state, w = 'pickled', drop(w, ('pickled',))
    if not state and fam in COOKED_FAMILIES:
        w = [x[6:] if x.startswith('cooked') and len(x) > 9 else x for x in w]   # fused ids: cookedrottenham
        if not any(x in w for x in HOW_WORDS):
            state = 'cooked'
            w = drop(w, COOKED_WORDS)
    if fam != 'egg/dish':
        w = drop(w, CATEGORY_WORDS.get(cat, ()))
    w = drop(w, CONTAINER_WORDS)
    while len(w) > 1 and w[0] in LINK_WORDS:
        w = w[1:]
    return cat, form, state, w


def join(cat, form, state, words):
    return '_'.join(x for x in [cat, form, state] + list(words) if x)


def name_all(rows, fams, taken=()):
    """id -> key for every row, collisions settled: FIRST namespaces keep the bare key, the rest get _<namespace>."""
    order = sorted(rows, key=lambda i: (FIRST.index(i.split(':')[0]) if i.split(':')[0] in FIRST else len(FIRST), i))
    used = set(taken)
    out = {}
    for i in order:
        if i in OVERRIDES:
            k = OVERRIDES[i]
        elif i in INGREDIENTS:
            k = INGREDIENTS[i]
        else:
            k = join(*propose(rows[i], fams[i]))
        if k in used:
            k = f'{k}_{i.split(":")[0]}'
        n = 2
        while k in used:
            k = f'{k}_{n}'
            n += 1
        used.add(k)
        out[i] = k
    return out


# ---------------------------------------------------------------- main parts

def foods_wanted(key_val):
    rows = food_rows()
    excl = excluded_ids(key_val)
    fams = {}
    for i, r in rows.items():
        if i in excl or i in NOT_FOOD or i.startswith('fantasyfurniture:'):
            continue
        r['excluded'] = False
        f = ff.classify(r)
        if f is ff.SKIP:
            continue
        fams[i] = f
    rows = {i: rows[i] for i in fams}
    for i in INGREDIENTS:
        if i not in rows and i not in excl:
            rows[i] = dict(id=i, p=i.split(':', 1)[1], mod=i.split(':', 1)[0])
    return rows, fams, excl


def lint(entries, g, key_val, by_id):
    problems = []
    seen_key, seen_val = {}, {}
    for n, k, rhs in entries:
        m = re.fullmatch(r'i\.(\w+)', rhs)
        if not m:
            problems.append(f'line {n}: f.{k}: right-hand side must be i.<key>, not {rhs}')
            continue
        gk = m.group(1)
        if gk not in key_val:
            problems.append(f'line {n}: f.{k} = i.{gk}: no such general key')
            continue
        if k in seen_key:
            problems.append(f'line {n}: f.{k} defined twice (line {seen_key[k]})')
        seen_key[k] = n
        v = key_val[gk]
        if v in seen_val:
            problems.append(f'line {n}: f.{k}: {v} already named f.{seen_val[v]}')
        seen_val[v] = k
        if parse_key(k, g) is None:
            problems.append(f'line {n}: f.{k}: does not fit <category>[_<form>][_<state>]_<name>')
    if DICT.exists() and re.search(r"^\s*f\.\w+\s*=\s*['\"]", DICT.read_text(), re.M):
        problems.append('literal id in a mask line (use i.<key>)')
    return problems, seen_val


def render(named, by_id, g):
    """Full food_dict.js text from id -> key."""
    groups = defaultdict(list)
    for i, k in named.items():
        groups[parse_key(k, g)['category']].append((k, fc.best_key(i, by_id[i]), i))
    width = max(len(k) for k in named.values()) + 2
    head = [
        '// priority: 980',
        '// Food dict: global.food (alias f), a mask over global.item (creative_delight.md 13). Every edible and drinkable',
        '// item and every kitchen ingredient gets one development name; the value is always i.<key>, so items stay defined',
        '// once in item_dict.js. Key: <category>[_<form>][_<state>]_<name>, parts from food_grammar.js; raw is implicit.',
        '//',
        '// Kept by hand. Seeded 2026-10-25 by tools/food_dict.py --seed; run tools/food_dict.py (or /cdevgen run food_dict)',
        '// after a /dumpfood to lint this file and list foods with no key, with a proposed key for each.',
        '//',
        '// Read it as global.food. Creative Delight scripts (their own scope) can alias it f; the pack\'s startup and server',
        '// folders already use f for the fluid dict (fluid_dict.js), so the body is wrapped in a function and pack scripts',
        '// alias it fd.',
        '// Check on load (food_grammar.js): a missing general key, a value not in global.item, an item named twice or a key',
        '// outside the grammar throws. global.foodKey is the reverse index (item id -> food key).',
        'global.food = {}',
        ';(function (f, i) {',
    ]
    body = []
    for c in g['categories']:
        if not groups.get(c):
            continue
        body.append('')
        body.append(f'\t// ---- {c}')
        for k, gk, i in sorted(groups[c]):
            body.append(f'\t{("f." + k).ljust(width + 2)}= i.{gk};')
    tail = [
        '',
        "\tglobal.foodKey = global.subdict.check(f, 'food', global.foodGrammar)",
        '})(global.food, global.item)',
        '',
    ]
    return '\n'.join(head + body + tail)


def main():
    ap = argparse.ArgumentParser(description=__doc__.split('\n')[0])
    ap.add_argument('--check', action='store_true', help='lint only')
    ap.add_argument('--append', action='store_true', help='append proposed lines for foods with no key')
    ap.add_argument('--seed', action='store_true', help='write food_dict.js from scratch')
    ap.add_argument('--force', action='store_true', help='with --seed: overwrite an existing food_dict.js')
    a = ap.parse_args()

    g = grammar()
    _, key_val, by_id = fc.parse_dict()

    if a.seed:
        if DICT.exists() and not a.force:
            sys.exit(f'{DICT.relative_to(INSTANCE)} exists; it is kept by hand now (--force to overwrite)')
        rows, fams, _ = foods_wanted(key_val)
        missing = sorted(i for i in rows if i not in by_id)
        for i in missing:
            print(f'  ! {i}: not in item_dict.js, skipped')
            rows.pop(i)
        named = name_all(rows, fams)
        bad = [(i, k) for i, k in named.items() if parse_key(k, g) is None]
        if bad:
            sys.exit('keys outside the grammar: ' + ', '.join(f'{i} -> {k}' for i, k in bad))
        DICT.write_text(render(named, by_id, g))
        print(f'wrote {DICT.relative_to(INSTANCE)}: {len(named)} keys')

    entries = read_dict()
    problems, named_ids = lint(entries, g, key_val, by_id)
    for p in problems:
        print('  ! ' + p)
    print(f'{DICT.name}: {len(entries)} keys, {len(problems)} problem(s)')
    if a.check:
        sys.exit(1 if problems else 0)

    INDEX.write_text(json.dumps(OrderedDict(sorted(named_ids.items())), indent=1) + '\n')
    print(f'wrote {INDEX.relative_to(INSTANCE)}')

    rows, fams, excl = foods_wanted(key_val)
    stale = sorted(i for i in named_ids if i in excl)
    for i in stale:
        print(f'  ! f.{named_ids[i]}: {i} is removed or merged; drop the line')
    todo = {i: r for i, r in rows.items() if i not in named_ids and i in by_id}
    no_general = sorted(i for i in rows if i not in by_id)
    for i in no_general:
        print(f'  ! {i}: food with no general key; add it to item_dict.js first')
    proposed = name_all(todo, fams, taken=[k for _, k, _ in entries])
    lines = [f'\t{("f." + k).ljust(40)}= i.{fc.best_key(i, by_id[i])};  // {fams.get(i, "ingredient")}'
             for i, k in sorted(proposed.items(), key=lambda x: x[1])]
    if lines:
        print(f'{len(lines)} food(s) with no key; proposed:')
        print('\n'.join(lines))
    if a.append and lines:
        text = DICT.read_text()
        cut = text.rindex("\tglobal.foodKey")
        block = (APPEND_MARK + '\n' if APPEND_MARK not in text else '') + '\n'.join(lines) + '\n\n'
        DICT.write_text(text[:cut] + block + text[cut:])
        print(f'appended {len(lines)} line(s) to {DICT.relative_to(INSTANCE)}')
    sys.exit(1 if problems or lines or stale or no_general else 0)


if __name__ == '__main__':
    main()
