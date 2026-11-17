// requires: creativeequipment
// Creative Equipment. Recipe ids are creativeequipment:<type>/<suffix> (global.CEQ.recipes).
ServerEvents.recipes(e => {
global.CEQ.recipes(() => {

const R = global.NCH.forPack('creativeequipment')
var i = global.item;

// ============================================================
// Armor Piercing
// ============================================================

R.ieBlueprint({ item: i.armor_piercing, count: 64 }, [Item.of(i.empty_casing, 64), Item.of(i.gunpowder, 32), i.dragon_scale], 'bullet', e, 'armor_piercing_dragon_scale');

})
})
