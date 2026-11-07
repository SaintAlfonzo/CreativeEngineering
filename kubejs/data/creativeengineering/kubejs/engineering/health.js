// requires: creativeengineering
// Creative Engineering; moved from kubejs/server_scripts/engineering/health.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
ServerEvents.recipes(e=>{
console.log('health engineering starting')

var i = global.item
var f = global.fluid
var R = global.NCH

// Replace bandage recipe, efficient alt bottling recipe
R.remove({ id: 'pneumaticcraft:bandage' }, e)
R.shapeless(Item.of(i.bandage, 1), [[i.canvas, i.hemp_fabric], Item.of(i.honey_bottle, 4)], e, 'bandage_honey_bottle_shapeless')
R.shapeless(Item.of(i.bandage, 1), [[i.canvas, i.hemp_fabric], Item.of(i.glycerol, 4)], e, 'bandage_glycerol_shapeless')
R.ieBottling(i.bandage, f.t_honey, i.canvas, e, 'bandage_honey_bottling', 500)
R.ieBottling(i.bandage, f.t_honey, i.hemp_fabric, e, 'bandage_honey_bottling_2', 500)
R.createFilling(i.bandage, [Fluid.of(f.honey, 750), [i.canvas, i.hemp_fabric]], e, 'bandage_honey_filling')

R.ieBottling(Item.of(i.bandage, 2), f.t_phenolic_resin, i.hemp_fabric, e, 'bandage_phenolic_bottling', 200)
R.ieBottling(Item.of(i.bandage, 2), f.t_phenolic_resin, i.canvas, e, 'bandage_phenolic_bottling_2', 200)
R.createFilling(Item.of(i.bandage, 2), [Fluid.of(f.phenolic_resin, 250), [i.canvas, i.hemp_fabric]], e, 'bandage_phenolic_filling')

console.log('health engineering done')
})
