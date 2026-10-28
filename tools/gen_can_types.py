#!/usr/bin/env python3
"""Generate every food can (raw veggie/fruit/meat/fish, hazard, special, cookies, cooked veggie/meat/fish, bread,
salad, soup, meal, pie, feast, drink, golden, dairy, preserve, dessert, cake), plus the food values of Canned Goods'
own cans that no recipe makes (norecipe rows), so none keeps the mod's broken saturation modifiers.

Two kinds of section (SECTIONS below, mirroring the Goal comments in kubejs/data/creative_delight/kubejs/creativeengineering/canning.js):
  mult  a meal is the food it holds x mult (hunger and saturation points); every source shares one recipe.
  meal  a meal is a fixed size (hunger, saturation points), e.g. the Baked Potato meal of raw veggies. The recipe is
        sized by the first (whole) source's value, at most 8 (the grid less the can); the other sources are its
        sliced forms and share the same slots and count. No recipe makes a partly full can.
Effects are copied with scaled durations, except for Canned Goods cans whose own code applies them (code_fx).
Cans without their own sprite (kubejs/assets/creativeengineering/textures/item/<key>.png) get their family's sprite
(GROUP_SPRITE, one per section).

Input (run /dumpfood in game first):  kubejs/exported/food/catalog.json, foods.json
Output:
  kubejs/generation_scripts/food/can_types.json   every can: values, effects, servings, recipes (read by
                                                  development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/cans.js and creative_delight/kubejs/creativeengineering/canning.js)
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
COMFORT_NOURISHMENT = GEN / 'comfort_nourishment.json'
SPRITES = INSTANCE / 'kubejs/assets/creativeengineering/textures/item'

BEGIN = '// ===== BEGIN GENERATED CANS (tools/gen_can_types.py, do not edit) ====='
END = '// ===== END GENERATED CANS ====='
ANCHOR = "i.any_bell_pepper               = '#forge:crops/bell_pepper';       // green, red, yellow"

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
# pot       the recipes are Cooking Pot recipes (the empty can goes in the container slot, at most 6 inputs)
SALT = ['salt', 'bone_broth']
SWEET = ['sugar', 'honey_bottle']
VINEGAR = ['vinegar']   # Vintage Delight's vinegar bottle, the pack vinegar (vintage_delight.md S9, dishes/ingredients #6)
SECTIONS = OrderedDict([
    ('raw_veggie',   dict(meal=(5, 6), servings=2, target=0.8, adv=0.7, dur=1.0, fast=False, extra=[('_salt', SALT, 0, 1), ('_vinegar', VINEGAR, 0, 1)])),
    ('raw_fruit',    dict(meal=(5, 3), servings=3, target=0.8, adv=0.7, dur=1.0, fast=False, extra=[('_sugar', SWEET, 0, 1), ('_vinegar', VINEGAR, 0, 1)])),
    ('raw_meat',     dict(meal=(5, 3), servings=4, target=0.8, adv=0.8, dur=1.0, fast=False, meat=True,
                          extra=[('_broth', SALT, 0, 1), ('_vinegar', VINEGAR, 0, 1)])),
    ('raw_fish',     dict(meal=(4, 2), servings=4, target=0.8, adv=0.8, dur=1.0, fast=False, extra=[('_broth', SALT, 0, 1), ('_vinegar', VINEGAR, 0, 1)])),
    ('hazard',       dict(meal=(5, 3), servings=4, target=0.8, adv=0.8, dur=1.0, fast=False, extra=[])),
    ('special',      dict(meal=(5, 3), servings=3, target=0.8, adv=0.7, dur=1.0, fast=False, extra=[('_sugar', SWEET, 0, 1)])),
    ('cookies',      dict(mult=2,    servings=5,  n=8, dur=1.0,  fast=True,  extra=[('_sugar', ['sugar'], 6, 1)])),
    ('cooked_veggie', dict(mult=2,   servings=4,  n=6, dur=1.0,  fast=False, extra=[('_salt', ['salt'], 5, 1)])),
    ('cooked_meat',  dict(mult=2,    servings=3,  n=5, dur=1.0,  fast=False, extra=[('_broth', SALT, 4, 1)])),
    ('bread',        dict(mult=2,    servings=4,  n=6, dur=1.0,  fast=False, extra=[])),
    ('salad',        dict(mult=1.25, servings=8,  n=6, dur=0.75, fast=True,  extra=[('_vinegar', VINEGAR, 5, 1)])),
    ('soup',         dict(mult=1.25, servings=8,  n=6, dur=0.75, fast=False, extra=[('_broth', SALT, 3, 1)])),
    # placeholder goals (canning.js says TBD): pasta 2 bowls -> 4 meals of 0.75 bowl; pie 4 slices -> 4 slice meals
    # (+ pie crust turns a full can back into the pie); feast 1 placed feast -> 6 servings; drink 4 -> 4
    ('meal',         dict(mult=0.75, servings=4,  n=2, dur=0.75, fast=False, extra=[])),
    ('pie',          dict(mult=1,    servings=4,  n=4, dur=1.0,  fast=False, extra=[])),
    ('feast',        dict(mult=1,    servings=6,  n=1, dur=1.0,  fast=False, extra=[])),
    ('drink',        dict(mult=1,    servings=4,  n=4, dur=1.0,  fast=False, extra=[])),
    ('golden',       dict(mult=0.25, servings=10, n=2, dur=0.25, fast=False, extra=[])),   # honey is required, see below
    # 2026-10-21 (can_parity 2.20-2.23): cheese is processed in the Cooking Pot; tomato sauce and dough keep their
    # value; desserts like soups; cake: 1 cake -> 4 servings of 1.75 slices (7 slices a cake), see the cake recipes below
    ('dairy',        dict(mult=1.25, servings=4,  n=4, dur=1.0,  fast=False, extra=[], pot=True)),
    ('preserve',     dict(mult=1,    servings=4,  n=4, dur=1.0,  fast=False, extra=[])),
    ('dessert',      dict(mult=1.25, servings=8,  n=6, dur=0.75, fast=False, extra=[('_sugar', SWEET, 3, 1)])),
    ('cake',         dict(mult=1.75, servings=4,  n=1, dur=1.0,  fast=True,  extra=[])),
])

# Family sprite for cans without their own (texture ids): a drawn CE can or the Canned Goods can of the same family.
# Canned Goods texture names differ from some item ids (canned_rabbit_stew -> items/canned_rabit_stew).
GROUP_SPRITE = {
    'raw_veggie': 'creativeengineering:item/canned_carrot',
    'raw_fruit': 'creativeengineering:item/canned_apple',
    'special': 'creativeengineering:item/canned_apple',
    'raw_meat': 'creativeengineering:item/canned_raw_beef',
    'raw_fish': 'creativeengineering:item/canned_raw_salmon',
    'hazard': 'canned_goods:items/canned_rotten_flesh',
    'cookies': 'canned_goods:items/canned_cookies',
    'cooked_veggie': 'canned_goods:items/canned_baked_potato',
    'cooked_meat': 'canned_goods:items/canned_beef',
    'bread': 'canned_goods:items/canned_bread',
    'salad': 'creativeengineering:item/canned_cabbage',
    'soup': 'canned_goods:items/canned_rabit_stew',
    'meal': 'canned_goods:items/canned_food',
    'pie': 'canned_goods:items/canned_pumpkin_pie',
    'feast': 'canned_goods:items/canned_food',
    'drink': 'canned_goods:items/canned_honey_bottles',
    'golden': 'canned_goods:items/canned_golden_apples',
    'golden/enchanted': 'canned_goods:items/canned_notch_apples',   # glowing golden cans
    'dairy': 'canned_goods:items/canned_food',
    'preserve': 'canned_goods:items/canned_food',
    'dessert': 'canned_goods:items/canned_cake',
    'cake': 'canned_goods:items/canned_cake',
}

# explicit cans: (section, can key, display name, source keys (first = value source), existing Canned Goods can?[, opts])
# opts: input = recipe input keys instead of the sources (tags, feast blocks); pie = whole pie a full can + pie crust
#       makes; code_fx = Canned Goods applies the effects itself (no copy); values = fixed meal for a food not in the
#       catalog (hunger, modifier, meat, always, clear_effects); meal = this can's own (hunger, saturation points) meal
#       in a meal section, instead of the section's; adv / n / servings = this can's own additive ratio, recipe count,
#       servings; counts = (basic, additive) item counts in a meal section instead of the sizing rule; fx_ticks = {effect id: ticks} fixed effect lengths; food = False: not edible (an ingredient can);
#       norecipe = values only (Canned Goods cans that no recipe makes: a pack can of the same food has the recipe)
EXPLICIT = [
    # raw veggie: Baked Potato meals. Whole and sliced share a tag (cabbage leaf, potato slices) or the slots (kernels)
    ('raw_veggie', 'canned_beetroot', 'Canned Beetroot', ['minecraft_beetroot'], False, dict(input=['beetroot'])),
    ('raw_veggie', 'canned_cabbage', 'Canned Cabbage', ['farmersdelight_cabbage', 'cabbage_leaf'], False,
     dict(input=['cabbage'])),
    ('raw_veggie', 'canned_carrot', 'Canned Carrot', ['minecraft_carrot'], False, dict(input=['carrot'])),
    ('raw_veggie', 'canned_onion', 'Canned Onion', ['onion_farmersdelight'], False, dict(input=['onion'])),
    ('raw_veggie', 'canned_potato', 'Canned Potato', ['minecraft_potato', 'potato_slices'], False,
     dict(input=['any_potato'])),
    ('raw_veggie', 'canned_tomato', 'Canned Tomato', ['farmersdelight_tomato'], False, dict(input=['tomato'])),
    ('raw_veggie', 'canned_bell_pepper', 'Canned Bell Pepper', ['bell_pepper_green', 'bell_pepper_red', 'bell_pepper_yellow'],
     False, dict(input=['any_bell_pepper'])),
    ('raw_veggie', 'canned_cactus_flesh', 'Canned Cactus Flesh', ['cactus_flesh'], False),
    ('raw_veggie', 'canned_gongylidia', 'Canned Gongylidia', ['gongylidia'], False),
    ('raw_veggie', 'canned_corn', 'Canned Corn', ['corn'], False),   # maize only: kernels (4 a maize) are not canned
    # Veggies Delight and Vintage Delight (2026-10-23): a whole veggie and its 2 cut pieces share the slots, as cabbage.
    # Not canned: garlic and dandelion leaves (seasoning), raw oats (a grain, as corn kernels)
    ('raw_veggie', 'canned_broccoli', 'Canned Broccoli', ['broccoli'], False),
    ('raw_veggie', 'canned_cauliflower', 'Canned Cauliflower', ['cauliflower', 'cauliflower_floret'], False),
    ('raw_veggie', 'canned_sweet_potato', 'Canned Sweet Potato', ['sweet_potato'], False),
    ('raw_veggie', 'canned_turnip', 'Canned Turnip', ['turnip'], False),
    ('raw_veggie', 'canned_zucchini', 'Canned Zucchini', ['zucchini', 'zucchini_slice'], False),
    ('raw_veggie', 'canned_cucumber', 'Canned Cucumber', ['cucumber'], False),
    ('raw_veggie', 'canned_ghost_pepper', 'Canned Ghost Pepper', ['ghost_pepper'], False),
    # Fungi Delight (fungi_delight.md step 8): the truffle and its slices share the slots. Not canned: the cleaned
    # inky cap (raw, Weak Stomach), its cooked form fills its own can
    ('raw_veggie', 'canned_truffle', 'Canned Truffle', ['truffle', 'truffle_slice'], False),

    # raw fruit: Fruit Meals; sliced citrus shares the whole fruit's count
    ('raw_fruit', 'canned_apple', 'Canned Apple', ['apple'], False),
    ('raw_fruit', 'canned_banana', 'Canned Banana', ['neapolitan_banana'], False),   # Alex's Mobs bananas not canned
    ('raw_fruit', 'canned_cantaloupe', 'Canned Cantaloupe', ['cantaloupe_slice'], False),
    ('raw_fruit', 'canned_cherry', 'Canned Cherries', ['cherry'], False),
    ('raw_fruit', 'canned_glow_berry', 'Canned Glow Berries', ['glow_berries'], False),
    ('raw_fruit', 'canned_melon', 'Canned Melon', ['melon'], False),
    ('raw_fruit', 'canned_mulberry', 'Canned Mulberries', ['mulberry'], False),
    ('raw_fruit', 'canned_pumpkin', 'Canned Pumpkin', ['pumpkin_slice'], False),   # slices only, the pumpkin is a block
    ('raw_fruit', 'canned_salmonberry', 'Canned Salmonberries', ['salmonberries'], False),
    ('raw_fruit', 'canned_strawberry', 'Canned Strawberries', ['strawberries'], False),
    # the old 3 / 2 + sugar recipe with a much bigger serving than the Fruit Meal (can_parity 4: 140 % hunger in -> out,
    # the family's usual is 125 %)
    ('raw_fruit', 'canned_white_strawberry', 'Canned White Strawberries', ['white_strawberries'], False,
     dict(meal=(7, 5), counts=(3, 2))),
    ('raw_fruit', 'canned_sweet_berry', 'Canned Sweet Berries', ['sweet_berries'], False),
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
    # the berries' Wither (80 %) is kept but cut to 2 s a serving; Caffeinated as the berries
    ('raw_fruit', 'canned_coffee_berries', 'Canned Coffee Berries', ['coffee_berries'], False,
     dict(fx_ticks={'minecraft:wither': 40})),

    # raw meat: Meat Meals; the forge:raw_* tags hold the whole meat and its cut
    ('raw_meat', 'canned_raw_beef', 'Canned Raw Beef', ['beef'], False, dict(input=['raw_beef'])),
    ('raw_meat', 'canned_raw_chicken', 'Canned Raw Chicken', ['chicken'], False, dict(input=['raw_chicken'])),
    ('raw_meat', 'canned_raw_mutton', 'Canned Raw Mutton', ['mutton'], False, dict(input=['raw_mutton'])),
    ('raw_meat', 'canned_raw_pork', 'Canned Raw Pork', ['porkchop'], False, dict(input=['raw_pork'])),
    ('raw_meat', 'canned_raw_rabbit', 'Canned Raw Rabbit', ['rabbit'], False),
    # Shulker: a low value meat (1.2 a mollusk), so its own small meal (2, 1) x 4 keeps a full can at 8 mollusks
    ('raw_meat', 'canned_shulker', 'Canned Shulker Flesh', ['shulker_mollusk', 'shulker_filet'], False,
     dict(meal=(2, 1))),

    # raw fish: sliced cuts share the whole fish's count
    ('raw_fish', 'canned_blobfish', 'Canned Blobfish', ['blobfish'], False),
    ('raw_fish', 'canned_flying_fish', 'Canned Flying Fish', ['flying_fish'], False),
    ('raw_fish', 'canned_raw_lobster', 'Canned Lobster Tail', ['lobster_tail'], False),
    ('raw_fish', 'canned_raw_calamari', 'Canned Raw Calamari', ['calamari', 'calamari_slice'], False),
    ('raw_fish', 'canned_raw_catfish', 'Canned Raw Catfish', ['catfish', 'raw_catfish_slice'], False),
    ('raw_fish', 'canned_raw_cod', 'Canned Raw Cod', ['cod', 'cod_slice'], False),
    ('raw_fish', 'canned_raw_crab', 'Canned Raw Crab Leg', ['crab'], False),
    ('raw_fish', 'canned_raw_lionfish', 'Canned Raw Lionfish', ['lionfish'], False),
    ('raw_fish', 'canned_raw_perch', 'Canned Raw Perch', ['perch', 'perch_slice'], False),
    ('raw_fish', 'canned_raw_pike', 'Canned Raw Pike', ['pike', 'pike_slice'], False),
    ('raw_fish', 'canned_raw_salmon', 'Canned Raw Salmon', ['salmon', 'salmon_slice'], False),
    ('raw_fish', 'canned_tropical_fish', None, ['tropical_fish'], True),

    # Canned Goods' hazard cans: its code applies the bad effects on eating
    ('hazard', 'canned_pufferfishs', None, ['pufferfish'], True, dict(code_fx=True, meal=(4, 2))),   # a fish: Fish Meal
    ('hazard', 'canned_posionous_potatos', None, ['poisonous_potato'], True, dict(code_fx=True)),
    ('hazard', 'canned_spider_eyes', None, ['spider_eye'], True, dict(code_fx=True)),
    ('hazard', 'canned_rotten_flesh', None, ['rotten_flesh'], True, dict(code_fx=True)),
    ('hazard', 'canned_maggots', 'Canned Maggots', ['maggot'], False, dict(meal=(2, 1))),   # small meal, like shulker

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
    ('cookies', 'canned_oatmeal_cookie', 'Canned Oatmeal Cookies', ['oatmeal_cookie'], False),   # Vintage Delight

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
    # Vintage Delight's kimchi and pickle are the same foods as Brewin's (vintage_delight.md 3.1, both ship until VTD-40):
    # they fill the same can; the value source stays Brewin's
    ('cooked_veggie', 'canned_kimchi', 'Canned Kimchi', ['kimchi', 'vintagedelight_kimchi'], False),
    ('cooked_veggie', 'canned_pickled_pickles', 'Canned Pickles', ['pickle', 'pickled_sea_pickle'], False),   # pickled pickles merged (dishes/pickles-and-ferments #1)
    ('cooked_veggie', 'canned_roasted_adzuki_beans', 'Canned Roasted Adzuki Beans', ['roasted_adzuki_beans'], False),
    ('cooked_veggie', 'canned_chips', 'Canned Chips', ['chips'], False),
    ('cooked_veggie', 'canned_grilled_corn', 'Canned Grilled Corn', ['grilled_corn'], False),
    ('cooked_veggie', 'canned_tortilla_chip', 'Canned Tortilla Chips', ['tortilla_chip'], False),
    # Veggies Delight and Vintage Delight (2026-10-23); roasted florets alone (no whole roasted cauliflower). Not canned:
    # the vegetarian patty (patties are not canned cooked), roasted garlic (seasoning), honey roasted peanuts (a sweet)
    ('cooked_veggie', 'canned_baked_sweet_potato', 'Canned Baked Sweet Potato', ['baked_sweet_potato'], False),
    ('cooked_veggie', 'canned_roasted_zucchini', 'Canned Roasted Zucchini', ['roasted_zucchini'], False),
    ('cooked_veggie', 'canned_roasted_cauliflower', 'Canned Roasted Cauliflower', ['roasted_cauliflower_floret'], False),
    ('cooked_veggie', 'canned_pickled_beetroot', 'Canned Pickled Beetroot', ['pickled_beetroot'], False),
    ('cooked_veggie', 'canned_pickled_onion', 'Canned Pickled Onions', ['pickled_onion'], False),
    ('cooked_veggie', 'canned_pickled_pepper', 'Canned Pickled Peppers', ['pickled_pepper'], False),
    ('cooked_veggie', 'canned_roasted_peanut', 'Canned Roasted Peanuts', ['roasted_peanut'], False),
    ('cooked_veggie', 'canned_cooked_inky_cap', 'Canned Cooked Inky Cap', ['cooked_cleaned_cap'], False),   # Fungi Delight

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
    ('cooked_meat', 'canned_surstromming', 'Canned Surströmming', ['surstromming'], False),

    ('soup', 'canned_rabbit_stew', None, ['rabbit_stew'], True),
    ('soup', 'canned_mushrooms', None, ['mushroom_stew'], True),

    ('bread', 'canned_bread', None, ['minecraft_bread'], True),
    ('bread', 'canned_sweet_roll', 'Canned Sweet Rolls', ['sweet_roll'], False),

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
    ('meal', 'canned_creamy_chicken_pasta', 'Canned Creamy Pasta with Chicken', ['creamy_pasta_with_chicken_cuts'], False),
    ('meal', 'canned_creamy_ham_pasta', 'Canned Creamy Pasta with Ham', ['creamy_pasta_with_ham'], False),
    ('meal', 'canned_cornbread_stuffing', 'Canned Cornbread Stuffing', ['cornbread_stuffing'], False),
    ('meal', 'canned_creamed_corn', 'Canned Creamed Corn', ['creamed_corn'], False),
    ('meal', 'canned_dumplings', 'Canned Dumplings', ['dumplings'], False),
    # Veggies Delight and Vintage Delight pastas and noodles (2026-10-23)
    ('meal', 'canned_pasta_with_broccoli', 'Canned Pasta with Broccoli', ['pasta_with_broccoli'], False),
    ('meal', 'canned_potato_noodles', 'Canned Potato Noodles', ['potato_noodles'], False),
    ('meal', 'canned_cheese_pasta', 'Canned Cheese Pasta', ['cheese_pasta'], False),
    ('meal', 'canned_pad_thai', 'Canned Pad Thai', ['pad_thai'], False),
    ('meal', 'canned_truffle_pasta', 'Canned Truffle Pasta', ['truffle_pasta'], False),   # Fungi Delight

    # pie filling: 4 slices (or 4 fruitsdelight pies). Slices whose whole pie comes from a mod not in the pack get no
    # can: cherry and truffle (Central Kitchen, pies from Environmental), blueberry, crimson berry, green apple and
    # nightshade berry (Delightful, pies from BYG), gloomgourd (Undergarden), source berry (Ars Nouveau).
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
    # Farmer's Delight's sweet berry cheesecake, renamed Sweet Berry Pie (namechanges.js)
    ('pie', 'canned_sweet_berry_pie_filling', 'Canned Sweet Berry Pie Filling', ['sweet_berry_cheesecake_slice'], False,
     dict(pie='sweet_berry_cheesecake')),
    ('pie', 'canned_durian_pie_filling', 'Canned Durian Pie Filling', ['durian_pie'], False),
    ('pie', 'canned_pineapple_pie_filling', 'Canned Pineapple Pie Filling', ['pineapple_pie'], False),
    ('pie', 'canned_sweet_potato_pie_filling', 'Canned Sweet Potato Pie Filling', ['sweet_potato_pie_slice'], False,
     dict(pie='sweet_potato_pie')),   # Veggies Delight
    # tarts are pies to be (renamed and retextured later): 4 tarts fill a can, no tart back
    ('pie', 'canned_fig_pie_filling', 'Canned Fig Pie Filling', ['fig_tart'], False),
    ('pie', 'canned_lemon_pie_filling', 'Canned Lemon Pie Filling', ['lemon_tart'], False),

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

    # cheese wedges: 4 in a Cooking Pot, the empty can in its container slot
    ('dairy', 'canned_flaxen_cheese', 'Canned Flaxen Cheese', ['flaxen_cheese_wedge'], False),
    ('dairy', 'canned_scarlet_cheese', 'Canned Scarlet Cheese', ['scarlet_cheese_wedge'], False),
    ('dairy', 'canned_cheese_slice', 'Canned Cheese Slices', ['cheese_slice'], False),   # Vintage Delight
    ('preserve', 'canned_tomato_sauce', 'Canned Tomato Sauce', ['tomato_sauce'], False),
    # Create's dough, 8 a can; not a food (like the dough): an ingredient can for crafts that take it a serving at a time
    ('preserve', 'canned_dough', 'Canned Dough', ['in_dough'], False, dict(food=False, n=8, servings=8)),
    ('dessert', 'canned_pear_with_rock_sugar', 'Canned Pears in Rock Sugar', ['pear_with_rock_sugar'], False),
    # Canned Goods' cake can: 1 cake (or 4 slices + sugar / honey, added below)
    ('cake', 'canned_cake', None, ['cake_slice'], True, dict(input=['cake'])),

    # Canned Goods' cans that no recipe makes (still obtainable from trades, loot and creative): the values of their
    # family, from the food each one holds (food_types.json canned map; the mod's names are crossed, its sprites follow
    # the ids). Their own saturation modifiers are saturation points (1 to 42).
    ('raw_veggie', 'canned_beets', None, ['minecraft_beetroot'], True, dict(norecipe=True)),
    ('raw_veggie', 'canned_potatos', None, ['minecraft_potato'], True, dict(norecipe=True)),
    ('raw_fruit', 'canned_apples', None, ['apple'], True, dict(norecipe=True)),
    ('raw_fruit', 'canned_glow_berries', None, ['glow_berries'], True, dict(norecipe=True, code_fx=True)),
    ('raw_fruit', 'canned_melon_slice', None, ['melon'], True, dict(norecipe=True)),
    ('raw_fruit', 'canned_sweet_berries', None, ['sweet_berries'], True, dict(norecipe=True)),
    ('pie', 'canned_carrots', None, ['delightful_pumpkin_pie_slice'], True, dict(norecipe=True)),   # pumpkin pies
    ('golden', 'canned_pumpkin_pie', None, ['golden_carrot'], True, dict(norecipe=True)),        # golden carrots
    ('golden', 'canned_golden_carrot', None, ['golden_carrot'], True, dict(norecipe=True)),
    ('golden', 'canned_golden_apples', None, ['golden_apple'], True, dict(norecipe=True, code_fx=True)),
    ('golden', 'canned_enchanted_apples', None, ['enchanted_golden_apple'], True, dict(norecipe=True, code_fx=True)),
    ('soup', 'canned_beetroot_soup', None, ['beetroot_soup'], True, dict(norecipe=True)),
    ('soup', 'canned_suspicious_stew', None, ['suspicious_stew'], True, dict(norecipe=True, code_fx=True)),
    # Canned Food and the Hot Hot Hotpot are removed from the pack (startup_scripts/_lib/removed_items.js)
]

# soup keys that are not canned; salads without a can
SKIP = {
    'salad': {'maggot_salad', 'potato_salad', 'moredelight_potato_salad'},
    'soup': {'cavern_explorer_stew', 'chorus_stew_wood', 'endermite_stew_wood', 'suspicious_stew'},
    'bread': {'strawberry_scones', 'bread_slice', 'toast', 'tortilla'},
}
# Mod namespaces installed but not yet in a /dumpfood: their cans are configured above and skipped (listed as waiting)
# until the catalog has their foods. Empty this once the dump is taken. Vintage Delight: vintage_delight.md 4.0 step 8;
# Fungi Delight: fungi_delight.md step 8.
AWAIT_DUMP = set()   # emptied 2026-10-25: Vintage and Fungi Delight are in the 2026-10-24 /dumpfood
# Mods whose foods are typed but whose cans are not decided yet (each mod page's "Cans" row: hostile_delight.md step
# 12, ends_delight.md, ocean_delight.md, miners_delight.md I3 "cups are not cans"). The type loops skip their foods;
# explicit cans above still apply. Take a mod out once its cans are decided.
UNDECIDED_MODS = {'hostiledelight', 'ends_delight', 'miners_delight', 'oceanic_delight', 'oceansdelight'}
WAITING = []
# sources that already have an explicit can above (not repeated by the type loops)
STRIP_NAME = re.compile(r'^(Bowl of|Plate of|Slice of|Bar of)\s+')
CAN_SPRITE_NOTE = 'canned_goods:items/canned_food'   # fallback sprite (a section without a GROUP_SPRITE entry)
# Canned Goods cans removed from the pack (removed_items.js): dropped from can_servings.json and the canned type
REMOVED = ['canned_food', 'hot_hot_hotpot']


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


def scaled_effects(src, dur, fixed_ticks=None):
    out = []
    for fx in src.get('effects') or []:
        ticks = (fixed_ticks or {}).get(fx['effect']) or max(1, int(round(fx['seconds'] * dur * 20)))
        out.append(OrderedDict([('effect', fx['effect']), ('ticks', ticks), ('amp', fx['amplifier']),
                                ('chance', fx['chance'])]))
    return out


def value(src):
    """Hunger + saturation points of one item (what the sized recipes compare)."""
    points = src['saturation'] if src.get('saturation') is not None else src['nutrition'] * src['saturationModifier'] * 2
    return src['nutrition'] + points


def sized_recipes(spec, sources, srcs):
    """Recipes for a fixed-meal section: the count of the first (whole) source whose value is `target` of the can's
    (at most 8, the grid less the can). The other sources are its sliced forms: they share the same slots and count,
    so slicing first is the cheaper route. Every recipe makes a full can. `sources` are the recipe keys, `srcs` the
    catalog foods that size it."""
    meal_h, meal_p = spec['meal']
    can_value = spec['servings'] * (meal_h + meal_p)
    n0 = min(8, max(1, int(can_value * spec['target'] / value(srcs[0]) + 0.5)))
    if 'counts' in spec:
        n0 = spec['counts'][0]
    recipes = [OrderedDict([('suffix', ''), ('inputs', [OrderedDict([('any', list(sources)), ('n', n0)])])])]
    for suffix, add_keys, _n_src, n_add in spec['extra']:
        na = spec['counts'][1] if 'counts' in spec else max(1, int(n0 * spec['adv'] + 0.4))
        recipes.append(OrderedDict([('suffix', suffix), ('inputs', [
            OrderedDict([('any', list(sources)), ('n', na)]),
            OrderedDict([('any', add_keys), ('n', n_add)])])]))
    return recipes


def apply_comfort_nourishment(foods, keymap):
    """Give every source food the Comfort / Nourishment the game sets at startup (tools/gen_comfort_nourishment.py,
    read the same way by Creative Delight's comfort_nourishment.js), so cans follow without a fresh /dumpfood."""
    if not COMFORT_NOURISHMENT.exists():
        return
    data = load_json(COMFORT_NOURISHMENT)
    comfort, nourishment = 'farmersdelight:comfort', 'farmersdelight:nourishment'
    id_of = lambda entry: entry if ':' in entry else keymap.get(entry)  # noqa: E731
    rules = {id_of(k): v for k, v in data['set'].items()}
    keep = {id_of(k) for k in data['keep']}
    strip = {id_of(k) for k in data['strip']}
    for fid, f in foods.items():
        if fid in keep:
            continue
        fx = f.get('effects') or []
        if fid in rules or fid in strip:
            fx = [e for e in fx if e['effect'] not in (comfort, nourishment)]
            rule = rules.get(fid)
            if rule and rule['effect'] != 'none':
                fx.append(OrderedDict([('effect', 'farmersdelight:' + rule['effect']), ('amplifier', 0),
                                       ('seconds', rule['seconds']), ('chance', 1)]))
        else:
            fx = [e for e in fx if e['effect'] != nourishment]
        f['effects'] = fx


def build(foods, names, keymap_all, keymap_base, types):
    ids_of = lambda key: keymap_all[key]  # noqa: E731
    cans = OrderedDict()
    problems = []

    def add(section, can_key, display, sources, existing, opts=None):
        opts = opts or {}
        spec = SECTIONS[section]
        if 'meal' in opts:
            spec = dict(spec, meal=opts['meal'])   # this can's own meal size in a meal section
        for k in ('adv', 'n', 'servings', 'counts'):
            if k in opts:
                spec = dict(spec, **{k: opts[k]})
        for k in sources + opts.get('input', []) + ([opts['pie']] if 'pie' in opts else []):
            if k not in keymap_all:
                problems.append('%s: key %s is not in item_dict' % (can_key, k))
                return
        fixed = opts.get('values')
        nofood = opts.get('food') is False
        # mods whose foods are not in the catalog yet: a can whose value source waits is left out (WAITING), an
        # extra source that waits is dropped from this run
        pending = [k for k in sources if keymap_all[k].split(':')[0] in AWAIT_DUMP and keymap_all[k] not in foods]
        if pending and not fixed and not nofood:
            if sources[0] in pending:
                WAITING.append(can_key)
                return None
            sources = [k for k in sources if k not in pending]
            WAITING.append('%s (extra source %s)' % (can_key, ', '.join(pending)))
        if not fixed and not nofood:
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
        if nofood:
            c['food'] = False
            c['glow'] = False
            c['effects'] = []
        elif fixed:
            c['hunger'] = fixed['hunger']
            c['modifier'] = fixed['modifier']
            c['meat'] = bool(spec.get('meat') or fixed.get('meat'))
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
            c['effects'] = [] if opts.get('code_fx') else scaled_effects(src, spec['dur'], opts.get('fx_ticks', {}))
        if opts.get('code_fx'):
            c['code_fx'] = True
        c['sources'] = sources
        inputs = opts.get('input', sources)
        if opts.get('norecipe'):
            recipes = []
        elif 'meal' in spec:
            recipes = sized_recipes(spec, inputs, [foods[ids_of(k)] for k in sources])
        else:
            recipes = [OrderedDict([('suffix', ''), ('inputs', [OrderedDict([('any', inputs), ('n', spec['n'])])])])]
            for suffix, add_keys, n_src, n_add in spec['extra']:
                recipes.append(OrderedDict([('suffix', suffix), ('inputs', [
                    OrderedDict([('any', inputs), ('n', n_src)]),
                    OrderedDict([('any', add_keys), ('n', n_add)])])]))
        c['recipes'] = recipes
        if 'pie' in opts:
            c['pie'] = opts['pie']
        if spec.get('pot') or opts.get('pot'):
            c['pot'] = True
        if not existing:
            if (SPRITES / (can_key + '.png')).exists():
                c['texture'] = None
            else:
                family = 'golden/enchanted' if c['glow'] else section
                c['texture'] = GROUP_SPRITE.get(family, CAN_SPRITE_NOTE)
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
        if add(section, key, display, sources, existing, opts) and not (opts or {}).get('norecipe'):
            taken.update(sources)   # a values-only can leaves its food to the pack can that has the recipe

    def loop(section, type_names, skip=()):
        for tname in type_names:
            for src_key in types[tname]:
                if src_key in skip or src_key in taken:
                    continue
                if keymap_all.get(src_key, '').split(':')[0] in UNDECIDED_MODS:
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
    if 'canned_cake' in cans:
        cans['canned_cake']['recipes'].append(OrderedDict([('suffix', '_slices'), ('inputs', [
            OrderedDict([('any', ['cake_slice']), ('n', 4)]),
            OrderedDict([('any', SWEET), ('n', 1)])])]))
    for key, c in cans.items():
        if c['section'] == 'golden' and c['recipes']:
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
    lines.append('// %d cans: raw veggie/fruit/meat/fish, hazard, special, cookies, cooked, bread, salad, soup, meal, pie,'
                 ' feast, drink, golden, dairy, preserve, dessert (kubejs/generation_scripts/food/can_types.json)' % len(new))
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
    apply_comfort_nourishment(foods, keymap_base)

    cans, problems = build(foods, names, keymap_base, keymap_base, types)
    if WAITING:
        print('waiting for /dumpfood (%s): %s' % (', '.join(sorted(AWAIT_DUMP)), ', '.join(WAITING)))
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
        'Read by development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/cans.js (items, overrides) and kubejs/data/creative_delight/kubejs/creativeengineering/canning.js (recipes).',
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
    for k in stale + REMOVED:
        servings['servings'].pop(k, None)
    for k, c in cans.items():
        servings['servings'][k] = c['servings']
    new_servings = json.dumps(servings, indent=1, ensure_ascii=False)

    # food types: every new edible can is a canned food made from its sources
    for k in stale + REMOVED:
        if k in types['canned']:
            types['canned'].remove(k)
        types_json['canned'].pop(k, None)
    for k, c in cans.items():
        if c['existing']:
            continue
        if c.get('food') is False:   # an ingredient can is not a food
            if k in types['canned']:
                types['canned'].remove(k)
            types_json['canned'].pop(k, None)
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
