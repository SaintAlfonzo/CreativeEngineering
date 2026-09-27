ServerEvents.recipes(e=>{
console.log('magic progression starting')

var i = global.item
var f = global.fluid
var R = global.NCH

R.shapeless(Item.of(i.flint_and_terrasteel, 1), [i.flint, i.terrasteel_ingot], e)

R.remove({id: 'explorerscompass:explorers_compass'}, e)
R.replaceInput({ id: 'naturescompass:natures_compass' },i.any_log,i.terrasteel_ingot, e)

console.log('magic progression done')
})
