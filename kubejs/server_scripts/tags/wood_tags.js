ServerEvents.tags('item', e => {
console.log('wood tag started')

var i = global.item;

i.barrel = 'minecraft:barrel';

// ============================================================
// Mulch
// ============================================================

e.add('forge:mulch/brown', i.sawdust)
e.add('forge:mulch/brown', i.straw)

e.add('forge:mulch/bone', i.bone)
e.add('forge:mulch/bone', i.bonemeal)

e.add('forge:mulch/waste', i.rotten_flesh)
e.add('forge:mulch/waste', i.spider_eye)
e.add('forge:mulch/waste', i.poisonous_potato)

e.add('forge:mulch/green', i.moss)
e.add('forge:mulch/green', i.moss_clump)
e.add('forge:mulch/green', i.moss_paste)
e.add('forge:mulch/green', i.grass)
e.add('forge:mulch/green', i.tall_grass)
e.add('forge:mulch/green', i.fern)
e.add('forge:mulch/green', i.large_fern)

e.add('forge:mulch/fungus', i.red_mushroom)
e.add('forge:mulch/fungus', i.brown_mushroom)
e.add('forge:mulch/fungus', i.red_mushroom_colony)
e.add('forge:mulch/fungus', i.brown_mushroom_colony)

e.add('forge:mulch/fertilizer', i.ash)
e.add('forge:mulch/fertilizer', i.dust_saltpeter)
e.add('forge:mulch/fertilizer', i.fertilizer)

// ============================================================
// Wooden barrel
// ============================================================
e.add('vinery:barrel', i.barrel)
e.add('vinery:wooden_barrels', i.barrel)

console.log('wood tag finished')
})
