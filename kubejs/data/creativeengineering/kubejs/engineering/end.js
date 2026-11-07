// requires: creativeengineering
// Creative Engineering; moved from kubejs/server_scripts/engineering/end.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
ServerEvents.recipes(e=>{
console.log('end engineering starting')

const R = global.NCH
var i = global.item;

// ============================================================
// Ender pearls
// ============================================================

R.createCrushing([Item.of(i.ender_shard, 2), Item.of(i.ender_shard, 1).withChance(0.50)], i.ender_pearl, e)
R.ieCrushing(Item.of(i.ender_shard, 3), i.ender_pearl, e, undefined, [{ chance: 0.75, output: Item.of(i.ender_shard, 1)}])

console.log('end engineering done')
})
