#!/usr/bin/env python3
"""Stack size policy: resolve the rules below into kubejs/generation_scripts/stacks/stack_sizes.json.

kubejs/startup_scripts/changes/stack_sizes.js reads that file on ItemEvents.modification (startup) and calls
setMaxStackSize on every listed item. Entries are item_dict keys where the item has one (the same convention as
food_types.json), raw `ns:path` ids otherwise.

The rules here are the policy. Edit them, rerun, restart the game:

    python3 tools/gen_stack_sizes.py            write the JSON, print a summary
    python3 tools/gen_stack_sizes.py --list     also print every assigned item, by size and rule

Rules never look at an item's current stack size, so the output does not shift after the game has applied it.
Source data (all under kubejs/exported/, from /dumpall):
  items_by_mod/*.json            every item id, per mod
  food/foods.json, food/drinks.json   edible items (remainder container)
  tags/item_tags.json            #forge:eggs
and kubejs/generation_scripts/food/food_types.json (food type -> item_dict keys) + startup_scripts/_lib/item_dict.js.
"""
import glob
import json
import re
import sys
from collections import OrderedDict, defaultdict
from pathlib import Path

INSTANCE = Path(__file__).resolve().parent.parent
EXPORT = INSTANCE / 'kubejs/exported'
FOOD_TYPES = INSTANCE / 'kubejs/generation_scripts/food/food_types.json'
ITEM_DICT = INSTANCE / 'kubejs/startup_scripts/_lib/item_dict.js'
OUT = INSTANCE / 'kubejs/generation_scripts/stacks/stack_sizes.json'

# Namespaces whose items_by_mod file is left over from a removed mod.
STALE_NAMESPACES = {'refinedstorage', 'domum_ornamentum', 'tobacconistmod', 'structurize'}

# --- food ------------------------------------------------------------------------------------------------------

GOLDEN_TYPES = {'golden', 'golden/enchanted'}                      # uncanned golden foods
BOWL_TYPES = {'meal', 'meal/feast', 'soup', 'salad'}               # bowl meals, salads, feast plates
DRINK_TYPES = {'drink', 'drink/alcohol'}
HANDHELD_TYPES = {'handheld'}                                      # corndogs, sandwiches, burgers
INGREDIENT_TYPES = {'ingredient'}                                  # cooking oil, jams, batter, sauces

# Canned drinks and the three "meal" items that are not bowl food stay at 64.
FOOD_LEAVE_ALONE = {
    'canned_goods:soda', 'canned_goods:reef_cola', 'canned_goods:oculemonade',
    'moredelight:omelette', 'alexsmobs:shrimp_fried_rice', 'corn_delight:caramel_popcorn',
}
# Specific foods that beat their type's size.
FOOD_OVERRIDES = {
    'farmersdelight:bone_broth': 64,      # an ingredient, not a bowl meal
    'farmersdelight:cooked_rice': 64,     # cooking ingredient
    'minecraft:honey_bottle': 64,         # honey bottles are 64 even though the catalog calls them drinks
    'rusticdelight:chocolate_pancakes': 32,
    'rusticdelight:honey_pancakes': 32,
    'rusticdelight:vegetable_pancakes': 32,
    'fruitsdelight:fig_pudding': 8,       # a placed pudding block: counts as a placeable cake
}
# Sweets sized by name: ice cream, pudding and custard (the catalog's `sweet` type holds 185 items).
SWEET_32 = re.compile(r'(ice_cream|pudding|custard)$')

# --- placeable cakes and feasts (display-delight plated versions are separate blocks and are not touched) -------

PLACED_FOOD_8 = [
    'minecraft:cake',
    'neapolitan:adzuki_cake', 'neapolitan:banana_cake', 'neapolitan:chocolate_cake', 'neapolitan:mint_cake',
    'neapolitan:strawberry_cake', 'neapolitan:vanilla_cake',
    'goldenfood:golden_cake', 'goldenfood:enchanted_golden_cake',
    'farmersrespite:coffee_cake',
    'createaddition:chocolate_cake', 'createaddition:honey_cake',
    'farmersdelight:honey_glazed_ham_block', 'farmersdelight:rice_roll_medley_block',
    'farmersdelight:roast_chicken_block', 'farmersdelight:shepherds_pie_block',
    'farmersdelight:stuffed_pumpkin_block',
    'fruitsdelight:pineapple_fried_rice',                          # PineappleRiceBlock feast
    'brewinandchewin:pizza',
    # every pie, plus the nachos and popcorn box blocks
    'minecraft:pumpkin_pie', 'delightful:salmonberry_pie', 'endersdelight:chorus_pie', 'farmersdelight:apple_pie',
    'farmersdelight:chocolate_pie', 'farmersdelight:sweet_berry_cheesecake', 'farmersrespite:rose_hip_pie',
    'upgrade_aquatic:mulberry_pie', 'corn_delight:nachos_block', 'corn_delight:popcorn_box',
]

# --- everything else ---------------------------------------------------------------------------------------------

BOTTLES_16 = [
    'alexsmobs:lava_bottle', 'botania:slime_bottle',
    'buzzier_bees:bee_bottle', 'buzzier_bees:endermite_bottle', 'buzzier_bees:silverfish_bottle',
    'cloudstorage:happy_cloud_in_a_bottle', 'cloudstorage:angry_cloud_in_a_bottle',
]
STATIONS_64 = [
    'appliedcooking:kitchen_station', 'brewinandchewin:keg', 'brewinandchewin:fiery_fondue_pot',
]
MISC_64 = [
    'canned_goods:canned_golem_item', 'canned_goods:canned_golem_spawn_egg',
    'minecraft:bucket',                                               # the empty bucket
    'neapolitan:chimpanzee_banner_pattern',
    'createaddition:biomass', 'createaddition:straw',
]
# Hat bags, scraps and display blocks are not hats.
HAT_NOT = re.compile(r'^simplehats:(hatbag_|hatscraps_|hatdisplay$|haticon$)')

# Filled buckets (fluid, mob, milk, potion, golden milk): anything named *_bucket or bucket_*, except these.
BUCKET_NOT = {'minecraft:bucket', 'myrtrees:wooden_bucket', 'botania:open_bucket'}
BUCKET_RE = re.compile(r'_bucket$|:bucket_of_|:bucket_f_')

SIGN_RE = re.compile(r':(?:[\w/]*[_/])?(?:hanging_)?sign(?:_item)?$|:[\w/]*hanging_sign_[\w]+$|:secret_[\w]*sign_item$')
# Decor blocks named *_sign (already 64), not sign items.
SIGN_NOT = re.compile(r'^(design_decor|davebuildingmod|architects_palette):')
BANNER_RE = re.compile(r'^minecraft:\w+_banner$')


def load_dict_keys():
    """item id -> first item_dict key."""
    rev = {}
    pat = re.compile(r"^\s*i\.(\w+)\s*=\s*'([^'#]*)'")
    for line in ITEM_DICT.read_text().splitlines():
        m = pat.match(line)
        if m and m.group(1) not in ('na', 'none'):
            rev.setdefault(m.group(2), m.group(1))
    return rev


def load_universe():
    ids = set()
    for f in glob.glob(str(EXPORT / 'items_by_mod/*.json')):
        ns = Path(f).stem
        if ns in STALE_NAMESPACES:
            continue
        ids.update(json.load(open(f))['items'])
    return ids


def main():
    list_all = '--list' in sys.argv
    dict_key = load_dict_keys()
    universe = load_universe()

    # food type of every typed food, via item_dict keys
    key_to_id = {k: v for v, k in dict_key.items()}
    types = json.load(open(FOOD_TYPES))['types']
    food_type = {}
    for t, keys in types.items():
        for k in keys:
            if k in key_to_id:
                food_type[key_to_id[k]] = t
    tags = json.load(open(EXPORT / 'tags/item_tags.json'))
    eggs = set(tags.get('forge:eggs', []))

    assigned = OrderedDict()      # id -> (size, rule)
    missing = []

    def give(i, size, rule):
        if i not in assigned:
            assigned[i] = (size, rule)

    def require(ids, size, rule):
        for i in ids:
            if i in universe:
                give(i, size, rule)
            else:
                missing.append((rule, i))

    # Order matters: the first rule that claims an item wins.
    for i in sorted(universe):
        if i in FOOD_LEAVE_ALONE:
            assigned[i] = (64, 'food kept at 64')

    # filled buckets beat golden food (golden milk bucket is a bucket), empty bucket is 64 below
    for i in sorted(universe):
        if i not in BUCKET_NOT and not i.startswith('simplehats:') and BUCKET_RE.search(i):
            give(i, 8, 'filled bucket')

    require(PLACED_FOOD_8, 8, 'placed cake / feast')
    require(BOTTLES_16, 16, 'special bottle')

    for i, size in FOOD_OVERRIDES.items():
        if i in universe:
            give(i, size, 'food override')
        else:
            missing.append(('food override', i))

    for i, t in sorted(food_type.items()):
        if i not in universe:
            continue
        if t in GOLDEN_TYPES:
            give(i, 16, 'golden food')
        elif t in BOWL_TYPES:
            give(i, 32, 'bowl meal / soup / salad / feast plate')
        elif t in DRINK_TYPES:
            give(i, 32, 'drink')
        elif t in HANDHELD_TYPES:
            give(i, 64, 'handheld food')
        elif t in INGREDIENT_TYPES:
            give(i, 64, 'cooking ingredient / jam / honey')
        elif t == 'sweet' and SWEET_32.search(i):
            give(i, 32, 'ice cream / pudding / custard')

    for i in sorted(universe):
        if i.startswith('simplehats:') and not HAT_NOT.match(i):
            give(i, 8, 'simple hat')

    # 64: items other mods ship at 1-16
    require(STATIONS_64, 64, 'crafting station')
    require(MISC_64, 64, 'raised to 64')
    for i in sorted(universe):
        if i in eggs:
            give(i, 64, 'egg')
        elif i.startswith('fairylights:'):
            give(i, 64, 'fairy light')
        elif i.startswith('armor_trims:') or i.endswith('_armor_trim_smithing_template'):
            give(i, 64, 'armor trim')
        elif SIGN_RE.search(i) and not SIGN_NOT.match(i):
            give(i, 64, 'sign')
        elif BANNER_RE.match(i):
            give(i, 64, 'banner')
        elif i.startswith('supplementaries:flag_'):
            give(i, 64, 'flag')
        elif re.match(r'^exposure(_expanded)?:\w+_film$', i) and 'developed' not in i:
            give(i, 64, 'unused film')

    by_size = defaultdict(list)
    for i, (size, rule) in assigned.items():
        if size is not None:
            by_size[size].append(i)

    sizes = OrderedDict()
    for size in sorted(by_size):
        sizes[str(size)] = sorted(dict_key.get(i, i) for i in by_size[size])

    out = OrderedDict()
    out['_readme'] = ('Generated by python3 tools/gen_stack_sizes.py; edit the rules there, not this file. '
                      'size -> item_dict keys (raw ids where the item has no key). Read by '
                      'startup_scripts/changes/stack_sizes.js; restart to apply.')
    out['sizes'] = sizes
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(out, indent='\t') + '\n')

    rule_count = defaultdict(int)
    for i, (size, rule) in assigned.items():
        if size is not None:
            rule_count[(size, rule)] += 1
    print(f'wrote {OUT.relative_to(INSTANCE)}: {sum(len(v) for v in by_size.values())} items')
    for (size, rule), n in sorted(rule_count.items()):
        print(f'  {size:>3}  {n:>4}  {rule}')
    if missing:
        print('ids named in the rules but not in the item export:')
        for rule, i in missing:
            print(f'  [{rule}] {i}')
    if list_all:
        for i, (size, rule) in assigned.items():
            print(f'{size!s:>4} {i}  ({rule})')


if __name__ == '__main__':
    main()
