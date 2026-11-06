// Creative Magic; moved from kubejs/server_scripts/magic/storage.js on 2026-11-04 (creative_split.md 7.6, wave 6), rows unchanged.
// Recipe ids are creativemagic:<type>/<suffix> (global.CMG.recipes).
ServerEvents.recipes(e => {
global.CMG.recipes(() => {
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
const ES_WOOL = [i.white_wool, i.hemp_fabric]
R.remove({ id: 'enderstorage:ender_pouch' }, e)
R.enderStorage(i.es_ender_pouch, ['BLB', 'LPL', 'BWB'],
	{ P: i.t_ender_pearls, L: i.t_leather, B: i.blaze_powder, W: ES_WOOL }, e, 'ender_pouch')
R.remove({ id: 'enderstorage:ender_chest' }, e)
R.enderStorage(i.es_ender_chest, ['BWB', 'OCO', 'BPB'],
	{ P: i.t_ender_pearls, O: i.t_obsidian, C: i.wooden_chest, B: i.blaze_rod, W: ES_WOOL }, e, 'ender_chest')
R.remove({ id: 'enderstorage:ender_tank' }, e)
R.enderStorage(i.es_ender_tank, ['BWB', 'OCO', 'BPB'],
	{ P: i.t_ender_pearls, O: i.t_obsidian, C: i.cauldron, B: i.blaze_rod, W: ES_WOOL }, e, 'ender_tank')

	console.log('Magic storage done')
})
})
