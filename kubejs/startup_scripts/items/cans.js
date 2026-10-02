// Food cans made in server_scripts/cooking/canning.js (ingredient + empty can, or fewer ingredients + an additive).
// Every can reuses Canned Goods' beetroot can sprite (canned_goods:items/canned_beets) until they get their own.
//
// Food values are one serving (one meal). The number of servings per can lives in
// generation_scripts/food/can_servings.json (patcherMod food.CanServings), not here:
//   raw veggie  2 servings of a Baked Potato meal (5 hunger, 6 sat = 0.6 modifier)
//   raw fruit   3 servings of a Fruit Meal        (5 hunger, 3 sat = 0.3 modifier)
//   raw meat    4 servings of a Meat Meal         (5 hunger, 3 sat = 0.3 modifier), meat() so wolves eat them
// KubeJS saturation() is the modifier: saturation points = hunger x modifier x 2.
// Dict keys: startup_scripts/_lib/item_dict.js "canning" section. Food types: generation_scripts/food/food_types.json.

StartupEvents.registry('item', e => {
    var can = 'canned_goods:items/canned_beets'
    var veggie = f => f.hunger(5).saturation(0.6)
    var fruit = f => f.hunger(5).saturation(0.3)
    var meat = f => f.hunger(5).saturation(0.3).meat()

    // Raw veggie
    e.create('creativeengineering:canned_beetroot').displayName('Canned Beetroot').texture(can).food(veggie)
    e.create('creativeengineering:canned_cabbage').displayName('Canned Cabbage').texture(can).food(veggie)
    e.create('creativeengineering:canned_carrot').displayName('Canned Carrot').texture(can).food(veggie)
    e.create('creativeengineering:canned_onion').displayName('Canned Onion').texture(can).food(veggie)
    e.create('creativeengineering:canned_potato').displayName('Canned Potato').texture(can).food(veggie)
    e.create('creativeengineering:canned_tomato').displayName('Canned Tomato').texture(can).food(veggie)
    e.create('creativeengineering:canned_bell_pepper').displayName('Canned Bell Pepper').texture(can).food(veggie)
    e.create('creativeengineering:canned_cactus').displayName('Canned Cactus').texture(can).food(veggie)
    e.create('creativeengineering:canned_gongylidia').displayName('Canned Gongylidia').texture(can).food(veggie)
    e.create('creativeengineering:canned_mint').displayName('Canned Mint').texture(can).food(veggie)
    e.create('creativeengineering:canned_pumpkin').displayName('Canned Pumpkin').texture(can).food(veggie)

    // Raw fruit
    e.create('creativeengineering:canned_acorn').displayName('Canned Acorns').texture(can).food(fruit)
    e.create('creativeengineering:canned_apple').displayName('Canned Apple').texture(can).food(fruit)
    e.create('creativeengineering:canned_banana').displayName('Canned Banana').texture(can).food(fruit)
    e.create('creativeengineering:canned_cantaloupe').displayName('Canned Cantaloupe').texture(can).food(fruit)
    e.create('creativeengineering:canned_cherry').displayName('Canned Cherries').texture(can).food(fruit)
    e.create('creativeengineering:canned_glow_berry').displayName('Canned Glow Berries').texture(can).food(fruit)
    e.create('creativeengineering:canned_melon').displayName('Canned Melon').texture(can).food(fruit)
    e.create('creativeengineering:canned_mulberry').displayName('Canned Mulberries').texture(can).food(fruit)
    e.create('creativeengineering:canned_grape').displayName('Canned Grapes').texture(can).food(fruit)
    e.create('creativeengineering:canned_salmonberry').displayName('Canned Salmonberries').texture(can).food(fruit)
    e.create('creativeengineering:canned_strawberry').displayName('Canned Strawberries').texture(can).food(fruit)
    e.create('creativeengineering:canned_sweet_berry').displayName('Canned Sweet Berries').texture(can).food(fruit)

    // Raw meat
    e.create('creativeengineering:canned_raw_beef').displayName('Canned Raw Beef').texture(can).food(meat)
    e.create('creativeengineering:canned_raw_chicken').displayName('Canned Raw Chicken').texture(can).food(meat)
    e.create('creativeengineering:canned_raw_mutton').displayName('Canned Raw Mutton').texture(can).food(meat)
    e.create('creativeengineering:canned_raw_pork').displayName('Canned Raw Pork').texture(can).food(meat)
    e.create('creativeengineering:canned_raw_rabbit').displayName('Canned Raw Rabbit').texture(can).food(meat)
    e.create('creativeengineering:canned_raw_ham').displayName('Canned Raw Ham').texture(can).food(meat)
    e.create('creativeengineering:canned_raw_chevon').displayName('Canned Raw Chevon').texture(can).food(meat)
    e.create('creativeengineering:canned_raw_hoglin').displayName('Canned Raw Hoglin').texture(can).food(meat)
    e.create('creativeengineering:canned_raw_kangaroo').displayName('Canned Raw Kangaroo').texture(can).food(meat)
    e.create('creativeengineering:canned_raw_moose').displayName('Canned Raw Moose').texture(can).food(meat)
    e.create('creativeengineering:canned_raw_bison').displayName('Canned Raw Bison').texture(can).food(meat)
    e.create('creativeengineering:canned_raw_bunfungus').displayName('Canned Raw Bunfungus').texture(can).food(meat)
    e.create('creativeengineering:canned_mite_crust').displayName('Canned Mite Crust').texture(can).food(fruit) // not meat (no meat flag on mite crust)
})
