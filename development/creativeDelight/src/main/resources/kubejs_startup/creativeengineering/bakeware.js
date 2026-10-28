// Moved from the pack's startup_scripts/cooking/bakeware.js 2026-10-25, rows unchanged.
// Creative Delight startup script, loaded from the jar after the pack's startup_scripts, so the dicts exist here.
// Restart to apply.
//
// Bakeware — the oven's tool-slot items, and the mortar and pestle (below) (feature_writeup/oven.md 2.5). A baking recipe can name one in
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

    // Mortar and pestle: the food grinding tool (NCH.mortar / foodGrinding, TODO cooking/kitchen #10; moved from the
    // pack's startup_scripts/items/misc_items.js 2026-10-27, id unchanged). Texture copied from Old Guns'
    // mortar_and_pestle (AGPL-3.0), whose item is renamed Powder Pestle and stays out of food recipes. maxDamage makes
    // it unstackable; the recipe's damageIngredient takes 1 per craft and hands it back. Recipe:
    // kubejs/data/creative_delight/kubejs/creativeengineering/recipes.js.
    e.create(i.mortar_and_pestle).displayName('Mortar and Pestle').maxDamage(512)
})
