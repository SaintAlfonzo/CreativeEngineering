#!/usr/bin/env python3
"""Generate the per-type food cans (raw fruit/meat/fish, hazard, special, cookies, cooked veggie/meat/fish, bread,
salad, soup, meal, pie, feast, drink, golden). The hand-written raw veggie/fruit/meat cans stay in cans.js.

Two kinds of section (SECTIONS below, mirroring the Goal comments in kubejs/server_scripts/cooking/canning.js):
  mult  a meal is the food it holds x mult (hunger and saturation points); every source shares one recipe.
  meal  a meal is a fixed size (hunger, saturation points), like the hand-written raw cans. Each source gets its own
        recipe sized by its value (alt inputs: sliced forms). When the fair count does not fit the grid (8), the recipe
        takes 8 and the can comes out partly full (CanServings NBT).
Effects are copied with scaled durations, except for Canned Goods cans whose own code applies them (code_fx).
Cans without their own sprite (kubejs/assets/creativeengineering/textures/item/<key>.png) get the section's group
sprite (GROUP_SPRITE) or Canned Goods' beetroot can.

Input (run /dumpfood in game first):  kubejs/exported/food/catalog.json, foods.json
Output:
  kubejs/generation_scripts/food/can_types.json   every can: values, effects, servings, recipes (read by
                                                  startup_scripts/items/cans.js and server_scripts/cooking/canning.js)
  kubejs/startup_scripts/_lib/item_dict.js        block between the BEGIN/END GENERATED CANS markers (new cans only)
  kubejs/generation_scripts/food/can_servings.json  servings for every generated can (other entries kept)
  kubejs/generation_scripts/food/food_types.json  new cans added to types.canned and the canned map

    python3 tools/gen_can_types.py            write everything
    python3 tools/gen_can_types.py --check    print what would change, write nothing

"existing" cans are Canned Goods items (canned_goods:*): no new item, their food values are overridden at startup.
Everything else is a creativeengineering: item. Coverage worksheet: feature_writeup/can_parity.md.
Restart the game after a run (new items, new dict keys); /reload alone only applies recipe changes.
"""
import argparse
import json
import re
import sys
from collections import OrderedDict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import food_catalog  # noqa: E402  (write_types keeps food_types.json's one-line-per-type layout)

INSTANCE = Path(__file__).resolve().parent.parent
EXPORT = INSTANCE / 'kubejs/exported/food'
GEN = INSTANCE / 'kubejs/generation_scripts/food'
DICT = INSTANCE / 'kubejs/startup_scripts/_lib/item_dict.js'
OUT = GEN / 'can_types.json'
SERVINGS = GEN / 'can_servings.json'
TYPES = GEN / 'food_types.json'
SPRITES = INSTANCE / 'kubejs/assets/creativeengineering/textures/item'

BEGIN = '// ===== BEGIN GENERATED CANS (tools/gen_can_types.py, do not edit) ====='
END = '// ===== END GENERATED CANS ====='
ANCHOR = "i.canned_raw_rabbit             = 'creativeengineering:canned_raw_rabbit';"

# ---------------------------------------------------------------------------------------------------------------
# Spec
# ---------------------------------------------------------------------------------------------------------------
# mult      hunger and saturation points of one meal = source x mult
# meal      instead of mult: fixed (hunger, saturation points) per meal; recipes sized per source, see sized_recipes()
# target    meal sections: the basic recipe's sources are worth this share of the can (0.8, like the raw cans)
# adv       meal sections: additive recipe count = basic count x adv
# servings  meals per can (can_servings.json)
# n         mult sections: sources per can in the basic recipe
# extra     additive recipes: list of (suffix, additive keys (alternatives), sources used, additive count);
#           meal sections only use the suffix and additive keys (counts come from adv)
# dur       effect duration multiplier
# fast      every can in the section is fast to eat
# meat      meal sections: every can counts as meat (wolves); otherwise the source's flag decides
SALT = ['salt', 'bone_broth']
SWEET = ['sugar', 'honey_bottle']
SECTIONS = OrderedDict([
    ('raw_fruit',    dict(meal=(5, 3), servings=3, target=0.8, adv=0.7, dur=1.0, fast=False, extra=[('_sugar', SWEET, 0, 1)])),
    ('raw_meat',     dict(meal=(5, 3), servings=4, target=0.8, adv=0.8, dur=1.0, fast=False, meat=True,
                          extra=[('_broth', SALT, 0, 1)])),
    ('raw_fish',     dict(meal=(5, 3), servings=4, target=0.8, adv=0.8, dur=1.0, fast=False, extra=[('_broth', SALT, 0, 1)])),
    ('hazard',       dict(meal=(5, 3), servings=4, target=0.8, adv=0.8, dur=1.0, fast=False, extra=[])),
    ('special',      dict(meal=(5, 3), servings=3, target=0.8, adv=0.7, dur=1.0, fast=False, extra=[('_sugar', SWEET, 0, 1)])),
    ('cookies',      dict(mult=2,    servings=5,  n=8, dur=1.0,  fast=True,  extra=[('_sugar', ['sugar'], 6, 1)])),
    ('cooked_veggie', dict(mult=2,   servings=4,  n=6, dur=1.0,  fast=False, extra=[('_salt', ['salt'], 5, 1)])),
    ('cooked_meat',  dict(mult=2,    servings=3,  n=5, dur=1.0,  fast=False, extra=[('_broth', SALT, 4, 1)])),
    ('bread',        dict(mult=2,    servings=4,  n=6, dur=1.0,  fast=False, extra=[])),
    ('salad',        dict(mult=1.25, servings=8,  n=6, dur=0.75, fast=True,  extra=[])),   # vinegar additive comes later
    ('soup',         dict(mult=1.25, servings=8,  n=6, dur=0.75, fast=False, extra=[('_broth', SALT, 3, 1)])),
    # placeholder goals (canning.js says TBD): pasta 2 bowls -> 4 meals of 0.75 bowl; pie 4 slices -> 4 slice meals
    # (+ pie crust turns a full can back into the pie); feast 1 placed feast -> 6 servings; drink 4 -> 4
    ('meal',         dict(mult=0.75, servings=4,  n=2, dur=0.75, fast=False, extra=[])),
    ('pie',          dict(mult=1,    servings=4,  n=4, dur=1.0,  fast=False, extra=[])),
    ('feast',        dict(mult=1,    servings=6,  n=1, dur=1.0,  fast=False, extra=[])),
    ('drink',        dict(mult=1,    servings=4,  n=4, dur=1.0,  fast=False, extra=[])),
    ('golden',       dict(mult=0.25, servings=10, n=2, dur=0.25, fast=False, extra=[])),   # honey is required, see below
])

# Sprite for cans without their own, per section (texture ids). Raw veggie/fruit/meat cans in cans.js use the same
# carrot / apple / raw beef sprites.
GROUP_SPRITE = {
    'raw_fruit': 'creativeengineering:item/canned_apple',
    'special': 'creativeengineering:item/canned_apple',
    'raw_meat': 'creativeengineering:item/canned_raw_beef',
    'raw_fish': 'creativeengineering:item/canned_raw_salmon',
}

# explicit cans: (section, can key, display name, source keys (first = value source), existing Canned Goods can?[, opts])
# opts: input = recipe input keys instead of the sources (feast blocks); pie = whole pie a full can + pie crust makes;
#       code_fx = Canned Goods applies the effects itself (no copy); values = fixed meal for a food not in the catalog
#       (hunger, modifier, always, clear_effects)
EXPLICIT = [
    # raw fruit (the first 12 are hand-written in cans.js); sliced citrus is an alt input
    ('raw_fruit', 'canned_bayberry', 'Canned Bayberries', ['bayberry'], False),
    ('raw_fruit', 'canned_blueberry', 'Canned Blueberries', ['blueberry'], False),
    ('raw_fruit', 'canned_cranberry', 'Canned Cranberries', ['cranberry'], False),
    ('raw_fruit', 'canned_durian', 'Canned Durian', ['durian_flesh'], False),
    ('raw_fruit', 'canned_fig', 'Canned Figs', ['fig'], False),
    ('raw_fruit', 'canned_hamimelon', 'Canned Hamimelon', ['hamimelon_slice'], False),
    ('raw_fruit', 'canned_hawberry', 'Canned Hawberries', ['hawberry'], False),
    ('raw_fruit', 'canned_kiwi', 'Canned Kiwi', ['kiwi'], False),
    ('raw_fruit', 'canned_lemon', 'Canned Lemon', ['lemon', 'lemon_slice'], False),
    ('raw_fruit', 'canned_lychee', 'Canned Lychee', ['lychee'], False),
    ('raw_fruit', 'canned_mango', 'Canned Mango', ['mango'], False),
    ('raw_fruit', 'canned_mangosteen', 'Canned Mangosteen', ['mangosteen'], False),
    ('raw_fruit', 'canned_orange', 'Canned Orange', ['orange', 'orange_slice'], False),
    ('raw_fruit', 'canned_peach', 'Canned Peach', ['peach'], False),
    ('raw_fruit', 'canned_pear', 'Canned Pear', ['pear'], False),
    ('raw_fruit', 'canned_persimmon', 'Canned Persimmon', ['persimmon'], False),
    ('raw_fruit', 'canned_pineapple', 'Canned Pineapple', ['pineapple_slice'], False),

    # raw meat (beef, chicken, mutton, pork, rabbit are hand-written in cans.js)
    ('raw_meat', 'canned_shulker', 'Canned Shulker Flesh', ['shulker_mollusk', 'shulker_filet'], False),

    # raw fish: sliced cuts are alt inputs
    ('raw_fish', 'canned_blobfish', 'Canned Blobfish', ['blobfish'], False),
    ('raw_fish', 'canned_flying_fish', 'Canned Flying Fish', ['flying_fish'], False),
    ('raw_fish', 'canned_raw_lobster', 'Canned Lobster Tail', ['lobster_tail'], False),
    ('raw_fish', 'canned_raw_calamari', 'Canned Raw Calamari', ['calamari', 'calamari_slice'], False),
    ('raw_fish', 'canned_raw_catfish', 'Canned Raw Catfish', ['catfish', 'raw_catfish_slice'], False),
    ('raw_fish', 'canned_raw_cod', 'Canned Raw Cod', ['cod', 'cod_slice'], False),
    ('raw_fish', 'canned_raw_crab', 'Canned Raw Crab Leg', ['raw_crab'], False),
    ('raw_fish', 'canned_raw_lionfish', 'Canned Raw Lionfish', ['lionfish'], False),
    ('raw_fish', 'canned_raw_perch', 'Canned Raw Perch', ['perch', 'perch_slice'], False),
    ('raw_fish', 'canned_raw_pike', 'Canned Raw Pike', ['pike', 'pike_slice'], False),
    ('raw_fish', 'canned_raw_salmon', 'Canned Raw Salmon', ['salmon', 'salmon_slice'], False),
    ('raw_fish', 'canned_tropical_fish', None, ['tropical_fish'], True),

    # Canned Goods' hazard cans: its code applies the bad effects on eating
    ('hazard', 'canned_pufferfishs', None, ['pufferfish'], True, dict(code_fx=True)),
    ('hazard', 'canned_posionous_potatos', None, ['poisonous_potato'], True, dict(code_fx=True)),
    ('hazard', 'canned_spider_eyes', None, ['spider_eye'], True, dict(code_fx=True)),
    ('hazard', 'canned_rotten_flesh', None, ['rotten_flesh'], True, dict(code_fx=True)),

    ('special', 'canned_chorus_fruits', None, ['chorus_fruit'], True, dict(code_fx=True)),   # teleports in code
    ('special', 'canned_ancient_fruit', 'Canned Ancient Fruit', ['ancient_fruit'], False),

    ('cookies', 'canned_cookies', None, ['cookie'], True),
    ('cookies', 'canned_bayberry_cookie', 'Canned Bayberry Cookies', ['bayberry_cookie'], False),
    ('cookies', 'canned_cherry_cookie', 'Canned Cherry Cookies', ['cherry_cookie'], False),
    ('cookies', 'canned_cranberry_cookie', 'Canned Cranberry Cookies', ['cranberry_cookie'], False),
    ('cookies', 'canned_green_tea_cookie', 'Canned Green Tea Cookies', ['green_tea_cookie'], False),
    ('cookies', 'canned_honey_cookie', 'Canned Honey Cookies', ['honey_cookie'], False),
    ('cookies', 'canned_lemon_cookie', 'Canned Lemon Cookies', ['lemon_cookie'], False),
    ('cookies', 'canned_maple_cookie', 'Canned Maple Cookies', ['maple_cookie'], False),
    ('cookies', 'canned_mulberry_cookie', 'Canned Mulberry Cookies', ['mulberry_cookie'], False),
    ('cookies', 'canned_persimmon_cookie', 'Canned Persimmon Cookies', ['persimmon_cookie'], False),
    ('cookies', 'canned_sweet_berry_cookie', 'Canned Sweet Berry Cookies', ['sweet_berry_cookie'], False),
    ('cookies', 'canned_uncanny_cookie', 'Canned Uncanny Cookies', ['uncanny_cookies'], False),

    ('cooked_veggie', 'canned_baked_potato', None, ['baked_potato', 'baked_potato_slices'], True),
    ('cooked_veggie', 'canned_cooked_cactus_steak', 'Canned Cactus Steak', ['cactus_steak'], False),
    ('cooked_veggie', 'canned_cooked_rice', 'Canned Cooked Rice', ['cooked_rice'], False),
    ('cooked_veggie', 'canned_dried_kelp', None, ['dried_kelp'], True),
    ('cooked_veggie', 'canned_roasted_bell_pepper', 'Canned Roasted Bell Pepper',
     ['roasted_bell_pepper_green', 'roasted_bell_pepper_red', 'roasted_bell_pepper_yellow'], False),
    ('cooked_veggie', 'canned_baked_durian', 'Canned Baked Durian', ['baked_durian'], False),
    ('cooked_veggie', 'canned_baked_pear', 'Canned Baked Pear', ['baked_pear'], False),
    ('cooked_veggie', 'canned_boiled_blue_pickerelweed', 'Canned Boiled Blue Pickerelweed', ['boiled_blue_pickerelweed'], False),
    ('cooked_veggie', 'canned_boiled_purple_pickerelweed', 'Canned Boiled Purple Pickerelweed', ['boiled_purple_pickerelweed'], False),
    ('cooked_veggie', 'canned_boiled_corn', 'Canned Boiled Corn', ['boiled_corn'], False),
    ('cooked_veggie', 'canned_dried_banana', 'Canned Dried Banana', ['dried_banana'], False),
    ('cooked_veggie', 'canned_dried_persimmon', 'Canned Dried Persimmon', ['dried_persimmon'], False),
    ('cooked_veggie', 'canned_kimchi', 'Canned Kimchi', ['kimchi'], False),
    ('cooked_veggie', 'canned_pickled_pickles', 'Canned Pickled Pickles', ['pickled_pickles'], False),
    ('cooked_veggie', 'canned_roasted_adzuki_beans', 'Canned Roasted Adzuki Beans', ['roasted_adzuki_beans'], False),

    # meat: whole cut, or whole + sliced. Patties, bacon, bison, smoked ham, chevon, hoglin, kangaroo, moose,
    # bunfungus, lobster and crab are not canned cooked (can_parity.md Want /, 2026-10-13)
    ('cooked_meat', 'canned_beef', None, ['beef_cooked'], True),
    ('cooked_meat', 'canned_pork', None, ['porkchop_cooked'], True),
    ('cooked_meat', 'canned_chicken', None, ['chicken_cooked', 'chicken_cuts_cooked'], True),
    ('cooked_meat', 'canned_cooked_mutten', None, ['mutton_cooked', 'cooked_mutton_chops'], True),
    ('cooked_meat', 'canned_cooked_rabbit', None, ['rabbit_cooked'], True),
    ('cooked_meat', 'canned_glazed_porkchop', 'Canned Honey Glazed Porkchop', ['glazed_porkchop'], False),
    ('cooked_meat', 'canned_jerky', 'Canned Jerky', ['jerky'], False),
    ('cooked_meat', 'canned_maple_glazed_bacon', 'Canned Maple Glazed Bacon', ['maple_glazed_bacon'], False),
    # fish: all accept the sliced cut
    ('cooked_meat', 'canned_fish', None, ['cod_cooked', 'cod_slice_cooked'], True),
    ('cooked_meat', 'canned_cooked_salmon', None, ['salmon_cooked', 'salmon_slice_cooked'], True),
    ('cooked_meat', 'canned_cooked_calamari', 'Canned Cooked Calamari', ['cooked_calamari', 'cooked_calamari_slice'], False),
    ('cooked_meat', 'canned_cooked_catfish', 'Canned Cooked Catfish', ['catfish_cooked_mob', 'catfish_slice_cooked'], False),
    ('cooked_meat', 'canned_cooked_perch', 'Canned Cooked Perch', ['cooked_perch', 'cooked_perch_slice'], False),
    ('cooked_meat', 'canned_cooked_pike', 'Canned Cooked Pike', ['cooked_pike', 'cooked_pike_slice'], False),
    ('cooked_meat', 'canned_cooked_lionfish', 'Canned Cooked Lionfish', ['cooked_lionfish'], False),

    ('soup', 'canned_rabbit_stew', None, ['rabbit_stew'], True),
    ('soup', 'canned_mushrooms', None, ['mushroom_stew'], True),

    ('bread', 'canned_bread', None, ['minecraft_bread'], True),

    # pastas (duck and vegetable noodles are canned as pasta)
    ('meal', 'canned_bell_pepper_pasta', 'Canned Bell Pepper Pasta', ['bell_pepper_pasta'], False),
    ('meal', 'canned_cheesy_pasta', 'Canned Cheesy Pasta', ['cheesy_pasta'], False),
    ('meal', 'canned_crab_pasta', 'Canned Crab Pasta', ['crab_pasta'], False),
    ('meal', 'canned_kangaroo_pasta', 'Canned Kangaroo Pasta', ['kangaroo_pasta'], False),
    ('meal', 'canned_lobster_pasta', 'Canned Lobster Pasta', ['lobster_pasta'], False),
    ('meal', 'canned_duck_pasta', 'Canned Pasta with Duck', ['duck_noodles'], False),
    ('meal', 'canned_pasta_with_meatballs', 'Canned Pasta with Meatballs', ['pasta_with_meatballs'], False),
    ('meal', 'canned_pasta_with_mutton', 'Canned Pasta with Mutton Chop', ['pasta_with_mutton'], False),
    ('meal', 'canned_pearl_pasta', 'Canned Pearl Pasta', ['pearl_pasta'], False),
    ('meal', 'canned_squid_ink_pasta', 'Canned Squid Ink Pasta', ['squid_ink_pasta'], False),
    ('meal', 'canned_vegetable_pasta', 'Canned Vegetable Pasta', ['vegetable_noodles'], False),

    # pie filling: 4 slices (or 4 fruitsdelight pies). Blueberry, cherry, crimson berry, green apple, nightshade and
    # truffle slices have no source in the pack (no whole pie), so no can yet.
    ('pie', 'canned_apple_pie_filling', 'Canned Apple Pie Filling', ['apple_pie_slice'], False, dict(pie='apple_pie')),
    ('pie', 'canned_chocolate_pie_filling', 'Canned Chocolate Pie Filling', ['chocolate_pie_slice'], False,
     dict(pie='chocolate_pie')),
    ('pie', 'canned_chorus_pie_filling', 'Canned Chorus Pie Filling', ['chorus_pie_slice'], False, dict(pie='chorus_pie')),
    ('pie', 'canned_mulberry_pie_filling', 'Canned Mulberry Pie Filling', ['mulberry_pie_slice'], False,
     dict(pie='mulberry_pie')),
    ('pie', 'canned_pumpkin_pie_filling', 'Canned Pumpkin Pie Filling',
     ['delightful_pumpkin_pie_slice', 'pumpkin_pie_slice'], False, dict(pie='pumpkin_pie')),
    ('pie', 'canned_rose_hip_pie_filling', 'Canned Rose Hip Pie Filling', ['rose_hip_pie_slice'], False,
     dict(pie='rose_hip_pie')),
    ('pie', 'canned_salmonberry_pie_filling', 'Canned Salmonberry Pie Filling', ['salmonberry_pie_slice'], False,
     dict(pie='salmonberry_pie')),
    ('pie', 'canned_durian_pie_filling', 'Canned Durian Pie Filling', ['durian_pie'], False),
    ('pie', 'canned_pineapple_pie_filling', 'Canned Pineapple Pie Filling', ['pineapple_pie'], False),

    # feasts: one placed feast block -> a can of its servings (value source = the serving)
    ('feast', 'canned_caramel_popcorn', 'Canned Caramel Popcorn', ['caramel_popcorn'], False, dict(input=['popcorn_box'])),
    ('feast', 'canned_hoglin_feast', 'Canned Hoglin Feast', ['plate_of_stuffed_hoglin_ham'], False,
     dict(input=['stuffed_hoglin'])),
    ('feast', 'canned_honey_glazed_ham', 'Canned Honey Glazed Ham', ['honey_glazed_ham'], False,
     dict(input=['honey_glazed_ham_block'])),
    ('feast', 'canned_nachos', 'Canned Nachos', ['nachos_bowl'], False, dict(input=['nachos_block'])),
    ('feast', 'canned_pineapple_fried_rice', 'Canned Pineapple Fried Rice', ['bowl_of_pineapple_fried_rice'], False,
     dict(input=['pineapple_fried_rice'])),
    ('feast', 'canned_roast_chicken', 'Canned Roast Chicken', ['roast_chicken'], False, dict(input=['roast_chicken_block'])),
    ('feast', 'canned_shepherds_pie', "Canned Shepherd's Pie", ['shepherds_pie'], False, dict(input=['shepherds_pie_block'])),
    ('feast', 'canned_stuffed_pumpkin', 'Canned Stuffed Pumpkin', ['stuffed_pumpkin'], False,
     dict(input=['stuffed_pumpkin_block'])),
    ('feast', 'canned_stuffed_shulker', 'Canned Stuffed Shulker', ['stuffed_shulker_bowl'], False,
     dict(input=['stuffed_shulker'])),

    # drinks: honey clears poison in Canned Goods' code; milk is not a food, the can clears effects itself
    ('drink', 'canned_honey_bottles', None, ['honey_bottle'], True, dict(code_fx=True)),
    ('drink', 'canned_powdered_milk', 'Canned Powdered Milk', ['minecraft_milk_bucket'], False,
     dict(values=dict(hunger=1, modifier=0.1, always=True, clear_effects=True))),
]

# soup keys that are not canned; salads without a can
SKIP = {
    'salad': {'maggot_salad', 'potato_salad', 'moredelight_potato_salad'},
    'soup': {'cavern_explorer_stew', 'chorus_stew_wood', 'endermite_stew_wood', 'suspicious_stew'},
    'bread': {'strawberry_scones', 'bread_slice', 'toast', 'tortilla'},
}
# sources that already have an explicit can above (not repeated by the type loops)
STRIP_NAME = re.compile(r'^(Bowl of|Plate of|Slice of|Bar of)\s+')
CAN_SPRITE_NOTE = 'canned_goods:items/canned_beets'   # fallback sprite


def load_json(path):
    with open(path, encoding='utf-8') as f:
        return json.load(f, object_pairs_hook=OrderedDict)


def parse_dict(text):
    return {m.group(1): m.group(2) for m in re.finditer(r"^i\.(\w+)\s*=\s*'([^']+)'", text, re.M)}


def strip_generated(text):
    a = text.find(BEGIN)
    b = text.find(END)
    if a >= 0 and b > a:
        return text[:a].rstrip('\n') + '\n' + text[b + len(END):].lstrip('\n')
    return text


def title_from_id(item_id):
    path = item_id.split(':', 1)[1]
    return ' '.join(w.capitalize() for w in path.split('_'))


def food_name(item_id, names):
    n = names.get(item_id) or ''
    if not n or '.' in n or n == item_id:
        return title_from_id(item_id)
    return n


def meal_values(src, mult):
    """(hunger, modifier) for a meal worth `mult` x the source: saturation points scale with the same factor."""
    hunger = max(1, int(src['nutrition'] * mult + 0.5))
    points = src['saturation'] * mult if src.get('saturation') is not None else src['nutrition'] * src['saturationModifier'] * 2 * mult
    modifier = round(points / (2 * hunger), 3)
    return hunger, modifier


def scaled_effects(src, dur):
    out = []
    for fx in src.get('effects') or []:
        ticks = max(1, int(round(fx['seconds'] * dur * 20)))
        out.append(OrderedDict([('effect', fx['effect']), ('ticks', ticks), ('amp', fx['amplifier']),
                                ('chance', fx['chance'])]))
    return out


def value(src):
    """Hunger + saturation points of one item (what the sized recipes compare)."""
    points = src['saturation'] if src.get('saturation') is not None else src['nutrition'] * src['saturationModifier'] * 2
    return src['nutrition'] + points


def sized_recipes(spec, sources, srcs):
    """Recipes for a fixed-meal section: per source, the count whose value is `target` of the can's (at most 8, the
    grid less the can). Alt sources are sized against the first source's recipe; one that would need more than 8 takes
    8 and makes a partly full can (servings scaled by value)."""
    meal_h, meal_p = spec['meal']
    can_value = spec['servings'] * (meal_h + meal_p)
    v0 = value(srcs[0])
    n0 = min(8, max(1, int(can_value * spec['target'] / v0 + 0.5)))
    recipes = []

    def basic_and_additive(key, n, tag, servings=None):
        r = OrderedDict([('suffix', tag), ('inputs', [OrderedDict([('any', [key]), ('n', n)])])])
        if servings:
            r['servings'] = servings
        recipes.append(r)
        if servings:
            return   # partial cans: no additive variant
        for suffix, add_keys, _n_src, n_add in spec['extra']:
            na = max(1, int(n * spec['adv'] + 0.4))
            recipes.append(OrderedDict([('suffix', tag + suffix), ('inputs', [
                OrderedDict([('any', [key]), ('n', na)]),
                OrderedDict([('any', add_keys), ('n', n_add)])])]))

    basic_and_additive(sources[0], n0, '')
    for key, src in zip(sources[1:], srcs[1:]):
        fair = n0 * v0 / value(src)
        if fair <= 8.5:
            basic_and_additive(key, max(1, int(fair + 0.5)), '_' + key)
        else:
            servings = max(1, int(spec['servings'] * 8 / fair + 0.5))
            basic_and_additive(key, 8, '_' + key, servings)
    return recipes


def build(foods, names, keymap_all, keymap_base, types):
    ids_of = lambda key: keymap_all[key]  # noqa: E731
    cans = OrderedDict()
    problems = []

    def add(section, can_key, display, sources, existing, opts=None):
        opts = opts or {}
        spec = SECTIONS[section]
        for k in sources + opts.get('input', []) + ([opts['pie']] if 'pie' in opts else []):
            if k not in keymap_all:
                problems.append('%s: key %s is not in item_dict' % (can_key, k))
                return
        fixed = opts.get('values')
        if not fixed:
            for k in sources:
                if keymap_all[k] not in foods:
                    problems.append('%s: %s (%s) is not in the food catalog' % (can_key, k, keymap_all[k]))
                    return
        c = OrderedDict()
        c['section'] = section
        c['existing'] = existing
        if not existing:
            if not display:
                display = 'Canned ' + STRIP_NAME.sub('', food_name(ids_of(sources[0]), names))
            c['name'] = display
        c['servings'] = spec['servings']
        if fixed:
            c['hunger'] = fixed['hunger']
            c['modifier'] = fixed['modifier']
            c['meat'] = False
            c['fast'] = bool(spec['fast'])
            c['always'] = bool(fixed.get('always'))
            c['glow'] = False
            c['effects'] = []
            if fixed.get('clear_effects'):
                c['clear_effects'] = True
        else:
            srcs = [foods[ids_of(k)] for k in sources]
            src = srcs[0]
            flags = src.get('flags') or ''
            if 'meal' in spec:
                hunger = spec['meal'][0]
                modifier = round(spec['meal'][1] / (2.0 * hunger), 3)
            else:
                hunger, modifier = meal_values(src, spec['mult'])
            c['hunger'] = hunger
            c['modifier'] = modifier
            c['meat'] = bool(spec.get('meat') or 'M' in flags)
            c['fast'] = bool(spec['fast'] or ('F' in flags and 'meal' not in spec))
            c['always'] = 'A' in flags
            c['glow'] = section == 'golden' and sources[0].startswith('enchanted_')
            c['effects'] = [] if opts.get('code_fx') else scaled_effects(src, spec['dur'])
        if opts.get('code_fx'):
            c['code_fx'] = True
        c['sources'] = sources
        if 'meal' in spec:
            recipes = sized_recipes(spec, sources, [foods[ids_of(k)] for k in sources])
        else:
            inputs = opts.get('input', sources)
            recipes = [OrderedDict([('suffix', ''), ('inputs', [OrderedDict([('any', inputs), ('n', spec['n'])])])])]
            for suffix, add_keys, n_src, n_add in spec['extra']:
                recipes.append(OrderedDict([('suffix', suffix), ('inputs', [
                    OrderedDict([('any', inputs), ('n', n_src)]),
                    OrderedDict([('any', add_keys), ('n', n_add)])])]))
        c['recipes'] = recipes
        if 'pie' in opts:
            c['pie'] = opts['pie']
        if not existing:
            if (SPRITES / (can_key + '.png')).exists():
                c['texture'] = None
            else:
                c['texture'] = GROUP_SPRITE.get(section, CAN_SPRITE_NOTE)
        if can_key in cans:
            problems.append('duplicate can ' + can_key)
        cans[can_key] = c
        return c

    taken = set()
    for row in EXPLICIT:
        section, key, display, sources, existing = row[:5]
        opts = row[5] if len(row) > 5 else None
        if existing and key not in keymap_base:
            problems.append('existing can %s is not in item_dict' % key)
            continue
        if not existing and key in keymap_base:
            problems.append('new can %s collides with an existing dict key' % key)
            continue
        if add(section, key, display, sources, existing, opts):
            taken.update(sources)

    def loop(section, type_names, skip=()):
        for tname in type_names:
            for src_key in types[tname]:
                if src_key in skip or src_key in taken:
                    continue
                can_key = 'canned_' + src_key
                if can_key in keymap_base:
                    can_key = 'canned_ce_' + src_key
                add(section, can_key, None, [src_key], False)
                taken.add(src_key)

    loop('bread', ['baked'], SKIP['bread'])
    loop('salad', ['salad'], SKIP['salad'])
    loop('soup', ['soup'], SKIP['soup'])
    loop('golden', ['golden', 'golden/enchanted'])

    # honey bread from plain bread + honey, golden cans need honey in the can
    if 'canned_honey_bread' in cans:
        cans['canned_honey_bread']['recipes'].append(OrderedDict([('suffix', '_honey'), ('inputs', [
            OrderedDict([('any', ['minecraft_bread']), ('n', 5)]),
            OrderedDict([('any', ['honey_bottle']), ('n', 1)])])]))
    for key, c in cans.items():
        if c['section'] == 'golden':
            c['recipes'] = [OrderedDict([('suffix', ''), ('inputs', [
                OrderedDict([('any', c['sources']), ('n', SECTIONS['golden']['n'])]),
                OrderedDict([('any', ['honey_bottle']), ('n', 1)])])])]

    # display names must be unique among the new cans
    seen = {}
    for key, c in cans.items():
        if c['existing']:
            continue
        if c['name'] in seen:
            problems.append('duplicate name %r: %s and %s' % (c['name'], seen[c['name']], key))
        seen[c['name']] = key
    return cans, problems


def dict_block(cans):
    lines = [BEGIN]
    new = [(k, c) for k, c in cans.items() if not c['existing']]
    width = max(len(k) for k, _ in new) + 2
    lines.append('// %d cans: raw fruit/meat/fish, special, cookies, cooked, bread, salad, soup, meal, pie, feast, drink, golden'
                 ' (kubejs/generation_scripts/food/can_types.json)' % len(new))
    for k, _ in new:
        lines.append("i.%s = 'creativeengineering:%s';" % (k.ljust(width), k))
    lines.append(END)
    return '\n'.join(lines) + '\n'


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--check', action='store_true')
    args = ap.parse_args()

    catalog = load_json(EXPORT / 'catalog.json')['foods']
    foods = {f['id']: f for f in catalog}
    names = {f['id']: f.get('name') for f in load_json(EXPORT / 'foods.json')['foods']}
    dict_text = DICT.read_text(encoding='utf-8')
    base_text = strip_generated(dict_text)
    keymap_base = parse_dict(base_text)
    types_json = load_json(TYPES)
    types = types_json['types']

    cans, problems = build(foods, names, keymap_base, keymap_base, types)
    if problems:
        print('PROBLEMS:')
        for p in problems:
            print('  ' + p)
        sys.exit(1)

    by_section = OrderedDict()
    for k, c in cans.items():
        by_section.setdefault(c['section'], []).append(k)
    for s, ks in by_section.items():
        new = [k for k in ks if not cans[k]['existing']]
        print('%-14s %3d cans (%d new)' % (s, len(ks), len(new)))

    out = OrderedDict()
    out['_readme'] = [
        'Generated by tools/gen_can_types.py from kubejs/exported/food/catalog.json. Do not edit by hand, edit SECTIONS / EXPLICIT in the script.',
        'cans: can item_dict key -> values per meal, effects (ticks) and recipes. existing = a Canned Goods can whose food values are overridden at startup.',
        'recipes: inputs are {any: item_dict keys (alternatives), n: how many}; a recipe always also takes an empty can.',
        'Read by startup_scripts/items/cans.js (items, overrides) and server_scripts/cooking/canning.js (recipes).',
    ]
    out['cans'] = cans
    new_out = json.dumps(out, indent=1, ensure_ascii=False) + '\n'

    # item_dict block
    block = dict_block(cans)
    if ANCHOR not in base_text:
        print('anchor line not found in item_dict.js: ' + ANCHOR)
        sys.exit(1)
    new_dict = base_text.replace(ANCHOR, ANCHOR + '\n' + block.rstrip('\n'))

    # cans generated last run but gone now: drop their servings and canned-food entries
    stale = []
    if OUT.exists():
        prev = load_json(OUT).get('cans', {})
        stale = [k for k, c in prev.items() if not c.get('existing') and k not in cans]
    if stale:
        print('removed cans: ' + ', '.join(stale))

    # servings
    servings = load_json(SERVINGS)
    for k in stale:
        servings['servings'].pop(k, None)
    for k, c in cans.items():
        servings['servings'][k] = c['servings']
    new_servings = json.dumps(servings, indent=1, ensure_ascii=False)

    # food types: every new can is a canned food made from its sources
    for k in stale:
        if k in types['canned']:
            types['canned'].remove(k)
        types_json['canned'].pop(k, None)
    for k, c in cans.items():
        if c['existing']:
            continue
        if k not in types['canned']:
            types['canned'].append(k)
        types_json['canned'][k] = c['sources']

    if args.check:
        print('check only, nothing written')
        return
    OUT.write_text(new_out, encoding='utf-8')
    DICT.write_text(new_dict, encoding='utf-8')
    SERVINGS.write_text(new_servings, encoding='utf-8')
    food_catalog.write_types(types_json)
    print('wrote %s, %s, %s, %s' % (OUT.name, DICT.name, SERVINGS.name, TYPES.name))


if __name__ == '__main__':
    main()
