// requires: creativemagic
// Creative Magic; moved from kubejs/server_scripts/magic/xp.js on 2026-11-04 (creative_split.md 7.6, wave 6), rows unchanged.
// Recipe ids are creativemagic:<type>/<suffix> (global.CMG.recipes).
ServerEvents.recipes(e => {
global.CMG.recipes(() => {
console.log('XP starting')

const R = global.NCH
var i = global.item;

// ============================================================
// XP
// ============================================================

R.remove({id:'compressedcreativity:mixing/memory_essence'}, e)

R.remove({id:'create_enchantment_industry:crafting/disenchanter'}, e)
R.shaped(Item.of(i.disenchanter, 1), [' X ','XSX', ' D '], {S: i.any_sandpaper, D: i.item_drain, X: i.xp_block}, e)

R.remove({id:'create_enchantment_industry:crafting/experience_rotor'}, e)
R.shaped(Item.of(i.experience_rotor, 1), [' X ','XPX', ' X '], {P: i.propeller, X: i.xp_block}, e)

R.remove({id:'create_enchantment_industry:crafting/printer'}, e)
R.shaped(Item.of(i.printer, 1), [' T ',' K ', 'PXP'], {T: i.cr_tank, K: i.dried_kelp, P: i.plate_iron, X: i.xp_block}, e)

console.log('xp done')
})
})
