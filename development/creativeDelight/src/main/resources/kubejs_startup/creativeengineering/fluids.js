// Moved from the pack's startup_scripts/cooking/fluids.js 2026-10-25, rows unchanged.
// Creative Delight startup script, loaded from the jar after the pack's startup_scripts, so the dicts exist here.
// Restart to apply.
//
// Pack drink and cooking fluids. Like Central Kitchen's drink fluids they are "virtual": no placed block and no bucket, they live
// in tanks, basins and pipes only. Ids in _lib/fluid_dict.js ("Pack fluids"), forge:fluid/<name> tags in
// server_scripts/tags/fluidtags.js, recipes in server_scripts/cooking/cooking.js ("Fluids"). JEI lists every
// registered fluid (creativecommon client _lib/fluid_jei.js) and shows the bottle's effects on the fluid through its Create
// emptying recipe (patcherMod DrinkFluidEffects).

StartupEvents.registry('fluid', e => {
    var f = global.fluid
    // Ender's Delight's chorus juice (TODO food/drinks #4)
    e.create(f.chorus_juice).displayName('Chorus Juice').thinTexture(0xB283C9).bucketColor(0xB283C9).noBucket().noBlock()
    // Veggies Delight's juices (feature_writeup/veggies_delight.md 4.9)
    e.create(f.carrot_juice).displayName('Carrot Juice').thinTexture(0xF08A24).bucketColor(0xF08A24).noBucket().noBlock()
    e.create(f.dandelion_juice).displayName('Dandelion Juice').thinTexture(0xEFCF3A).bucketColor(0xEFCF3A).noBucket().noBlock()
    e.create(f.turnip_water).displayName('Turnip Juice').thinTexture(0xE3D5E6).bucketColor(0xE3D5E6).noBucket().noBlock()
    // Cooking fluids (TODO dishes/ingredients #2): egg solution stands in for egg + milk, frosting for the frosting item
    e.create(f.egg_solution).displayName('Egg Solution').thinTexture(0xF3D97A).bucketColor(0xF3D97A).noBucket().noBlock()
    e.create(f.frosting).displayName('Frosting').thinTexture(0xFFF4F0).bucketColor(0xFFF4F0).noBucket().noBlock()
    // Kettle tank drinks (feature_writeup/farmers_respite.md K9): Rustic Delight's coffees past plain Coffee (that one
    // is Central Kitchen's coffee fluid) and Delightful's Kettle drinks. Bottle <-> 250 mB in Creative Delight's
    // farmersrespite/kettle.js; the Kettle reads those pairs to fill and empty bottles.
    var drink = (id, name, color) => e.create(id).displayName(name).thinTexture(color).bucketColor(color).noBucket().noBlock()
    drink(f.milk_coffee, 'Milk Coffee', 0xB58560)
    drink(f.honey_coffee, 'Honey Coffee', 0xC08A3E)
    drink(f.chocolate_coffee, 'Chocolate Coffee', 0x5E3B23)
    drink(f.dark_coffee, 'Dark Coffee', 0x2E1C12)
    drink(f.syrup_coffee, 'Syrup Coffee', 0x9C5F2C)
    drink(f.pumpkin_coffee, 'Pumpkin Coffee', 0xC27A34)
    drink(f.cherry_blossom_coffee, 'Cherry Blossom Coffee', 0xD39AA4)
    drink(f.lavender_tea, 'Lavender Tea', 0xA98CCB)
    drink(f.matcha_latte, 'Matcha Latte', 0x9DBE78)
    drink(f.berry_matcha_latte, 'Ice Berry Matcha', 0xBF6E8C)   // renamed (food/drinks #15)
})
