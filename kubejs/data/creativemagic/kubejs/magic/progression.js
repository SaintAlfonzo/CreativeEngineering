// requires: creativemagic
// Creative Magic; moved from kubejs/server_scripts/magic/progression.js on 2026-11-04 (creative_split.md 7.6, wave 6), rows unchanged.
// Recipe ids are creativemagic:<type>/<suffix> (global.CMG.recipes).
ServerEvents.recipes(e => {
global.CMG.recipes(() => {
console.log('magic progression starting')

var i = global.item
var f = global.fluid
var R = global.NCH

R.shapeless(Item.of(i.flint_and_terrasteel, 1), [i.flint, i.terrasteel_ingot], e)

R.remove({id: 'explorerscompass:explorers_compass'}, e)
R.replaceInput({ id: 'naturescompass:natures_compass' },i.any_log,i.terrasteel_ingot, e)

console.log('magic progression done')
})
})
