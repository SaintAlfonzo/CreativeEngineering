// Creative Engineering; moved from kubejs/server_scripts/engineering/power.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
ServerEvents.recipes(e=>{
console.log('Power starting')

const R = global.NCH
var i = global.item;

// Create addition removals
R.remove({id:'createaddition:crafting/gold_spool'}, e)
R.remove({id:'createaddition:crafting/electrum_spool'}, e)
R.remove({id:'createaddition:crafting/redstone_relay'}, e)
R.remove({id:'createaddition:crafting/small_connector_copper'}, e)
R.remove({id:'createaddition:crafting/small_light_connector'}, e)
R.remove({id:'createaddition:crafting/large_connector_gold'}, e)
R.remove({id:'createaddition:crafting/large_connector_electrum'}, e)

//Light bulb carbon yield lower, steel alt recipe higher yield
R.remove({id:'immersiveengineering:blueprint/light_bulb'}, e)
R.ieBlueprint(Item.of(i.light_bulb, 1), [i.carbon_filament, i.bulb_glass, i.plate_copper], 'components', e, 'light_bulb_carbon')
R.ieBlueprint(Item.of(i.light_bulb, 3), [i.small_spring_steel, i.bulb_glass, i.plate_copper], 'components', e, 'light_bulb_steel_wire')

//Vacuum tube rebuild
R.remove({id:'immersiveengineering:blueprint/electron_tube'}, e)
R.ieBlueprint(Item.of(i.vacuum_tube, 3), [i.plate_nickel, i.bulb_glass, i.wire_copper, i.redstone], 'components', e)

//Capacitor rebuild
R.remove({id:'createaddition:crafting/capacitor_1'}, e)
R.remove({id:'createaddition:crafting/capacitor_2'}, e)
R.shaped(Item.of(i.capacitor, 1), ['E', 'C', 'T'], {E: i.plate_electrum, C: i.plate_constantan, T: i.electron_tube}, e)
R.shaped(Item.of(i.capacitor, 1), ['C', 'E', 'T'], {E: i.plate_electrum, C: i.plate_constantan, T: i.electron_tube}, e, 'capacitor_alt')

//Portable energy interface rebuild
R.remove({id:'createaddition:crafting/portable_energy_interface'}, e)
R.shaped(Item.of(i.portable_energy_interface, 2), ['BFC'], {B: i.brass_casing, C: i.coil_block_copper, F: i.cr_chute}, e)

//Modular accumulator upgrade
R.remove({id:'createaddition:crafting/modular_accumulator_electrum'}, e)
R.remove({id:'createaddition:crafting/modular_accumulator_gold'}, e)
R.remove({id:'createaddition:crafting/accumulator_conversion'}, e)
R.shaped(Item.of(i.modular_accumulator, 1), ['BRB','EAC', 'CWE'], {B: i.ingot_brass, R: i.rod_copper, A: i.hv_accumulator, E: i.coil_block_electrum, C: i.coil_block_copper, W: [i.wire_electrum, i.wire_brass]}, e)

console.log('Power done')
})
