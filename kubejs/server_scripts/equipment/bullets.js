
ServerEvents.recipes(e => {

console.log('Hello! The bullets event has started!')

const R = global.NCH
var f = global.fluid
var i = global.item;

// ============================================================
// Removals
// ============================================================

// ============================================================
// Armor Piercing
// ============================================================

R.ieBlueprint({ item: i.armor_piercing, count: 64 }, [Item.of(i.empty_casing, 64), Item.of(i.gunpowder, 32), i.dragon_scale], 'projectiles', e, 'armor_piercing_dragon_scale');

console.log('The bullets event has finished!')
})
