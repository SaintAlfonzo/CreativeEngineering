// Meat cultures — raw lab-grown precursor for each cultivated-meat line (recipes that
// grow/cook these into their vanilla-ish meat counterparts: kubejs/data/creative_delight/kubejs/creativeengineering/cultivated_meat.js
// — still WIP over there). Registered here since none of the 7 had an e.create() anywhere
// despite already having textures/models under assets/creativeengineering/{textures,models}/item/.
//
// Plain items for now, no .food() — add nutrition once the cooking recipes are designed.
//
// Culture blocks (<animal>_culture_block) are 9-culture storage blocks, same stats as
// prepared_flesh_block (misc_items.js): clay material, mud sound, hand-breakable. Blockstate,
// block model and item model are hand-authored on disk under
// assets/creativeengineering/{blockstates,models/block,models/item}/<animal>_culture_block.json
// (cube_all, texture at creativeengineering:block/culture_block/<animal>_culture_block.png).
// Compact/uncompact recipes: kubejs/data/creative_delight/kubejs/creativeengineering/cultivated_meat.js.

StartupEvents.registry('item', e => {
    e.create('creativeengineering:culture_beef').displayName('Beef Culture')
    e.create('creativeengineering:culture_pork').displayName('Pork Culture')
    e.create('creativeengineering:culture_mutton').displayName('Mutton Culture')
    e.create('creativeengineering:culture_poultry').displayName('Poultry Culture')
    e.create('creativeengineering:culture_rabbit').displayName('Rabbit Culture')
    e.create('creativeengineering:culture_cod').displayName('Cod Culture')
    e.create('creativeengineering:culture_salmon').displayName('Salmon Culture')
})

StartupEvents.registry('block', e => {
    e.create('creativeengineering:beef_culture_block').displayName('Beef Culture Block')
        .material('clay').soundType('mud').hardness(0.8).resistance(0.8)
        .model('creativeengineering:block/beef_culture_block')
    e.create('creativeengineering:pork_culture_block').displayName('Pork Culture Block')
        .material('clay').soundType('mud').hardness(0.8).resistance(0.8)
        .model('creativeengineering:block/pork_culture_block')
    e.create('creativeengineering:mutton_culture_block').displayName('Mutton Culture Block')
        .material('clay').soundType('mud').hardness(0.8).resistance(0.8)
        .model('creativeengineering:block/mutton_culture_block')
    e.create('creativeengineering:poultry_culture_block').displayName('Poultry Culture Block')
        .material('clay').soundType('mud').hardness(0.8).resistance(0.8)
        .model('creativeengineering:block/poultry_culture_block')
    e.create('creativeengineering:rabbit_culture_block').displayName('Rabbit Culture Block')
        .material('clay').soundType('mud').hardness(0.8).resistance(0.8)
        .model('creativeengineering:block/rabbit_culture_block')
    e.create('creativeengineering:cod_culture_block').displayName('Cod Culture Block')
        .material('clay').soundType('mud').hardness(0.8).resistance(0.8)
        .model('creativeengineering:block/cod_culture_block')
    e.create('creativeengineering:salmon_culture_block').displayName('Salmon Culture Block')
        .material('clay').soundType('mud').hardness(0.8).resistance(0.8)
        .model('creativeengineering:block/salmon_culture_block')
})
