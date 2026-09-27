ServerEvents.recipes(e=>{
console.log('end engineering starting')

const R = global.NCH
var i = global.item;

i.ender_shard = 'endersdelight:ender_shard';

// ============================================================
// Ender pearls
// ============================================================

R.createCrushing([Item.of(i.ender_shard, 2), Item.of(i.ender_shard, 1).withChance(0.50)], i.ender_pearl, e)
R.ieCrushing(Item.of(i.ender_shard, 3), i.ender_pearl, e, undefined, [{ chance: 0.75, output: Item.of(i.ender_shard, 1)}])

console.log('end engineering done')
})
