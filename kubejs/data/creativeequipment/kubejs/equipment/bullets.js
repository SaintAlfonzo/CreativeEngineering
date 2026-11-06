// Creative Equipment; moved from kubejs/server_scripts/equipment/bullets.js on 2026-11-04 (creative_split.md 7.6, wave 7), rows unchanged.
// Recipe ids are creativeequipment:<type>/<suffix> (global.CEQ.recipes).
ServerEvents.recipes(e => {
global.CEQ.recipes(() => {

const R = global.NCH
var i = global.item;

// ============================================================
// Armor Piercing
// ============================================================

R.ieBlueprint({ item: i.armor_piercing, count: 64 }, [Item.of(i.empty_casing, 64), Item.of(i.gunpowder, 32), i.dragon_scale], 'bullet', e, 'armor_piercing_dragon_scale');

})
})
