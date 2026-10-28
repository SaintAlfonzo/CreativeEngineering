ServerEvents.tags('item', e => {
console.log('wood tag started')

var i = global.item;
var t = global.tags

// ============================================================
// Mulch
// ============================================================

t.add(e, 'forge:mulch/brown', [i.sawdust, i.straw])
t.add(e, 'forge:mulch/bone', [i.bone, i.bonemeal])
t.add(e, 'forge:mulch/waste', [i.rotten_flesh, i.spider_eye, i.poisonous_potato])
t.add(e, 'forge:mulch/green', [i.moss, i.moss_clump, i.moss_paste, i.grass, i.tall_grass, i.fern, i.large_fern])
t.add(e, 'forge:mulch/fungus', [i.red_mushroom, i.brown_mushroom, i.red_mushroom_colony, i.brown_mushroom_colony])
t.add(e, 'forge:mulch/fertilizer', [i.ash, i.dust_saltpeter, i.fertilizer])

// ============================================================
// Wooden barrel
// ============================================================
t.add(e, ['vinery:barrel', 'vinery:wooden_barrels'], i.barrel)

console.log('wood tag finished')
})
