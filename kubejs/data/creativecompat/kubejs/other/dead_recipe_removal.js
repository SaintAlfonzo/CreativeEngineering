// Creative Compat; moved from kubejs/server_scripts/other/dead_recipe_removal.js on 2026-11-01.
ServerEvents.recipes(e => {

console.log('dead recipe removal starting')

var i = global.item
var f = global.fluid
var R = global.NCH

R.bulk('remove', [
    [{ id: 'vintageimprovements:craft/vanadium_nuggets_to_ingot' }, e],
    [{ id: 'vintageimprovements:craft/vanadium_block_to_ingots' }, e],
    [{ id: 'vintageimprovements:craft/vanadium_ingot_to_nuggets' }, e],
    [{ id: 'vintageimprovements:craft/vanadium_ingots_to_block' }, e],
    [{ id: 'vintageimprovements:rolling/vanadium_plate' }, e],
    [{ id: 'vintageimprovements:pressing/vanadium_ingot' }, e],
    [{ id: 'vintageimprovements:rolling/vanadium_ingot' }, e],
    [{ id: 'vintageimprovements:coiling/vanadium_wire' }, e],
    [{ id: 'vintageimprovements:coiling/vanadium_rod' }, e],
    [{ id: 'vintageimprovements:coiling/shadow_steel_rod' }, e],
    [{ id: 'vintageimprovements:coiling/shadow_steel_wire' }, e],
    [{ id: 'vintageimprovements:coiling/uranium_wire' }, e],
    [{ id: 'vintageimprovements:coiling/netherite_wire' }, e],
    [{ id: 'vintageimprovements:coiling/netherite_rod' }, e],
    [{ id: 'vintageimprovements:coiling/refined_radiance_rod' }, e],
    [{ id: 'vintageimprovements:coiling/refined_radiance_wire' }, e],
    [{ id: 'vintageimprovements:coiling/andesite_wire' }, e],
    [{ id: 'vintageimprovements:coiling/andesite_rod' }, e],
    [{ id: 'vintageimprovements:coiling/cast_iron_wire' }, e],
    [{ id: 'vintageimprovements:coiling/cast_iron_rod' }, e],
    [{ id: 'vintageimprovements:pressurizing/sulfur_trioxide' }, e],
]);

console.log('dead recipe removal finished')
})
