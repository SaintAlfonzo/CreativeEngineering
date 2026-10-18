// Bakeware — the oven's tool-slot items (feature_writeup/oven.md 2.5). A baking recipe can name one in
// its `tool` field (#creativeengineering:bakeware/<kind>); it stays in the slot and is never used up, so
// these are plain items with no durability. Tags: server_scripts/tags/cookingtags.js. Recipes:
// server_scripts/cooking/baking.js.
//
// Placeholder look: every piece uses Immersive Engineering's iron plate texture (the pack's iron plate,
// i.plate_iron) until bakeware gets its own art.

StartupEvents.registry('item', e => {
    var i = global.item
    var IRON_PLATE_TEXTURE = 'immersiveengineering:item/metal_plate_iron'

    e.create(i.baking_sheet).displayName('Baking Sheet').texture(IRON_PLATE_TEXTURE)
    e.create(i.cake_pan).displayName('Cake Pan').texture(IRON_PLATE_TEXTURE)
    e.create(i.pie_tin).displayName('Pie Tin').texture(IRON_PLATE_TEXTURE)
    e.create(i.muffin_tin).displayName('Muffin Tin').texture(IRON_PLATE_TEXTURE)
})
