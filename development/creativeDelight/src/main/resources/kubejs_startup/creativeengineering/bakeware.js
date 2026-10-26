// Moved from the pack's startup_scripts/cooking/bakeware.js 2026-10-25, rows unchanged.
// Creative Delight startup script, loaded from the jar after the pack's startup_scripts, so the dicts exist here.
// Restart to apply.
//
// Bakeware — the oven's tool-slot items (feature_writeup/oven.md 2.5). A baking recipe can name one in
// its `tool` field (#creativeengineering:bakeware/<kind>); it stays in the slot and is never used up, so
// these are plain items with no durability. Tags: server_scripts/tags/cookingtags.js. Recipes:
// kubejs/data/creative_delight/kubejs/creativeengineering/baking.js (hammered from one iron plate: baking sheet -> muffin tin -> pie tin -> cake pan).
//
// Textures: assets/creativeengineering/textures/item/<id>.png. The muffin tin borrows the baking sheet's until it
// gets its own.

StartupEvents.registry('item', e => {
    var i = global.item
    e.create(i.baking_sheet).displayName('Baking Sheet').texture('creativeengineering:item/baking_sheet')
    e.create(i.cake_pan).displayName('Cake Pan').texture('creativeengineering:item/cake_pan')
    e.create(i.pie_tin).displayName('Pie Tin').texture('creativeengineering:item/pie_tin')
    e.create(i.muffin_tin).displayName('Muffin Tin').texture('creativeengineering:item/baking_sheet')
})
