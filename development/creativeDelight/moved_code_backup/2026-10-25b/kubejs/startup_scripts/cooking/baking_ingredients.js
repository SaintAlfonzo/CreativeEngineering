// Baking ingredients of the baking overhaul (feature_writeup/oven.md 2.6): frosting tops every cake, cookie dough is
// the base of every cookie, powdered sugar is ground sugar. Plain items, not food. Own textures (sources in
// textureswapper/frosting.png, textureswapper/baking/dough/, textureswapper/powdered_sugar/). Recipes:
// server_scripts/cooking/baking.js, server_scripts/cooking/powdered_sugar.js. The powdered sugar block and bucket are
// patcherMod (food/PowderedSugarRegistry.java).

StartupEvents.registry('item', e => {
    var i = global.item
    e.create(i.frosting).displayName('Frosting').texture('creativeengineering:item/frosting')
    e.create(i.cookie_dough).displayName('Cookie Dough').texture('creativeengineering:item/cookie_dough')
    e.create(i.powdered_sugar).displayName('Powdered Sugar').texture('creativeengineering:item/powdered_sugar')
})

// Dough blocks (TODO baking/baked-goods #27): 9-dough storage blocks with Create Compressed's dough block stats (sand,
// slime block sounds, strength 0.5 / 10). Blockstate / block model / block-item model are hand-authored under
// kubejs/assets/creativeengineering/ (cube_all, texture at creativeengineering:block/dough_block/<id>.png, made in
// textureswapper/baking/dough/out/). Recipes: server_scripts/cooking/cooking.js.
StartupEvents.registry('block', e => {
    var i = global.item
    var doughBlocks = [
        [i.gingerdough_block, 'Gingerdough Block'],
        [i.cookie_dough_block, 'Cookie Dough Block'],
        [i.sweet_potato_dough_block, 'Sweet Potato Dough Block'],
        [i.oat_dough_block, 'Oat Dough Block'],
        [i.cucumber_dough_block, 'Cucumber Dough Block'],
        [i.sourdough_block, 'Sourdough Block'],
    ]
    doughBlocks.forEach(d => {
        e.create(d[0]).displayName(d[1])
            .material('sand').soundType('slime_block').hardness(0.5).resistance(10.0)
            .model(d[0].replace(':', ':block/'))
    })
})
