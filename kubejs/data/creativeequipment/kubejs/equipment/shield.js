// requires: creativeequipment
// Creative Equipment; moved from kubejs/server_scripts/equipment/shield.js on 2026-11-04 (creative_split.md 7.6, wave 7), rows unchanged.
// Recipe ids are creativeequipment:<type>/<suffix> (global.CEQ.recipes).

ServerEvents.recipes(e => {
global.CEQ.recipes(() => {

console.log('Hello! The shield event has started!')

const R = global.NCH
var f = global.fluid
var i = global.item;

// ============================================================
// Removals
// ============================================================
R.bulk('remove', [
	[{ id: 'minecraft:shield' }, e],
]);

// ============================================================
// Shaped Crafts
// ============================================================
R.bulk('shaped', [
    [Item.of(i.ce_copper_shield, 1), ['RWR','WPW', 'RWR'], {W: i.planks, R: i.rod_copper, P: i.plate_copper}, e],
	[Item.of(i.shield, 1), ['RWR','WPW', 'RWR'], {W: i.planks, R: i.rod_iron, P: i.plate_iron}, e],
    [Item.of(i.ce_gold_shield, 1), ['RWR','WPW', 'RWR'], {W: i.planks, R: i.any_rod_gold, P: i.any_plate_gold}, e],
]);

// ============================================================
// Smithing
// ============================================================
R.bulk('smithing', [
    [Item.of(i.shield, 1), i.ce_copper_shield, i.ingot_iron, e],
    [Item.of(i.ce_gold_shield, 1), i.ce_copper_shield, i.any_ingot_gold, e],
    [Item.of(i.ce_diamond_shield, 1), i.shield, i.diamond, e],
    [Item.of(i.ce_diamond_shield, 1), i.ce_gold_shield, i.diamond, e],
    [Item.of(i.ce_netherite_shield, 1), i.ce_diamond_shield, i.ingot_netherite, e],
]);

console.log('The shield event has finished!')
})
})
