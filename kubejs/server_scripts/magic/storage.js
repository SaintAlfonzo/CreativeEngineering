ServerEvents.recipes(e=>{
console.log('Magic storage starting')

const R = global.NCH
var f = global.fluid
var i = global.item;

// ============================================================
// Cloud storage
// ============================================================
R.bulk('replaceInput', [
	[{ id: 'cloudstorage:cloud_chest' }, i.wooden_chest, i.ender_chest, e],
	[{ id: 'cloudstorage:static_cloud_chest' }, i.wooden_chest, i.cloud_chest, e],
	[{ id: 'minecraft:ender_chest' }, i.ender_eye, i.third_eye, e],
]);

// ============================================================
// Botania
// ============================================================

R.bulk('replaceInput', [
	[{ id: 'botania:third_eye' }, i.ender_eye, i.ender_pearl, e]
]);

// ============================================================
// Ender storage
// ============================================================

//Rebuilt enderstorage crafts for variable input
e.remove({ id: 'enderstorage:ender_pouch' })
e.custom({
  type: 'enderstorage:create_recipe',
  result: { item: i.es_ender_pouch },
  pattern: ["BLB", "LPL", "BWB"],
  key: {
    P: { tag: global.tags._clean(i.t_ender_pearls) },
    L: { tag: global.tags._clean(i.t_leather) },
    B: { item: i.blaze_powder },
    W: [{ item: i.white_wool }, { item: i.hemp_fabric }]}})
e.remove({ id: 'enderstorage:ender_chest' })
e.custom({
  type: 'enderstorage:create_recipe',
  result: { item: i.es_ender_chest },
  pattern: ["BWB", "OCO", "BPB"],
  key: {
    P: { tag: global.tags._clean(i.t_ender_pearls) },
    O: { tag: global.tags._clean(i.t_obsidian) },
    C: { tag: global.tags._clean(i.t_chest_wooden) },
    B: { item: i.blaze_rod },
    W: [{ item: i.white_wool }, { item: i.hemp_fabric }]}})
e.remove({ id: 'enderstorage:ender_tank' })
e.custom({
  type: 'enderstorage:create_recipe',
  result: { item: i.es_ender_tank },
  pattern: ["BWB", "OCO", "BPB"],
  key: {
    P: { tag: global.tags._clean(i.t_ender_pearls) },
    O: { tag: global.tags._clean(i.t_obsidian) },
    C: { item: i.cauldron },
    B: { item: i.blaze_rod },
    W: [{ item: i.white_wool }, { item: i.hemp_fabric }]}})

	console.log('Magic storage done')
})
