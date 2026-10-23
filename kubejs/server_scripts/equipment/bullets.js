ServerEvents.recipes(e => {

const R = global.NCH
var i = global.item;

// ============================================================
// Armor Piercing
// ============================================================

R.ieBlueprint({ item: i.armor_piercing, count: 64 }, [Item.of(i.empty_casing, 64), Item.of(i.gunpowder, 32), i.dragon_scale], 'bullet', e, 'armor_piercing_dragon_scale');

})
