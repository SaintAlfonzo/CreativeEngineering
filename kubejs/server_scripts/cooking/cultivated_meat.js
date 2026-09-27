ServerEvents.recipes(e=>{
console.log('cultivated meat starting')

const R = global.NCH
var f = global.fluid
var i = global.item;

// ============================================================
// Syringe
// ============================================================

R.shaped(Item.of(i.sample_syringe, 1), ['RG ','G G', ' GP'], {R: i.rod_iron, G: i.glass_pane, P: i.plate_iron}, e)

// ============================================================
// Sample recipes
// ============================================================

i.culture_base = '#creativeengineering:culture_base';

R.shapeless(Item.of(i.culture_beef, 1), [i.cow_sample_syringe, i.culture_base, i.slime], e, 'beef_sample')
R.shapeless(Item.of(i.culture_pork, 1), [i.pig_sample_syringe, i.culture_base, i.slime], e, 'pork_sample')
R.shapeless(Item.of(i.culture_poultry, 1), [i.chicken_sample_syringe, i.culture_base, i.slime], e, 'poultry_sample')
R.shapeless(Item.of(i.culture_mutton, 1), [i.sheep_sample_syringe, i.culture_base, i.slime], e, 'mutton_sample')
R.shapeless(Item.of(i.culture_rabbit, 1), [i.rabbit_sample_syringe, i.culture_base, i.slime], e, 'rabbit_sample')
R.shapeless(Item.of(i.culture_cod, 1), [i.cod_sample_syringe, i.culture_base, i.slime], e, 'cod_sample')
R.shapeless(Item.of(i.culture_salmon, 1), [i.salmon_sample_syringe, i.culture_base, i.slime], e, 'salmon_sample')

R.createMixing(Item.of(i.culture_beef, 3), [i.culture_beef, Fluid.of(f.diluted_bonemeal, 250), Fluid.of(f.plantoil, 250)], e, 'beef_culture_mixing', 'heated')
R.createMixing(Item.of(i.culture_pork, 3), [i.culture_pork, Fluid.of(f.diluted_bonemeal, 250), Fluid.of(f.plantoil, 250)], e, 'pork_culture_mixing', 'heated')
R.createMixing(Item.of(i.culture_poultry, 3), [i.culture_poultry, Fluid.of(f.diluted_bonemeal, 250), Fluid.of(f.plantoil, 250)], e, 'poultry_culture_mixing', 'heated')
R.createMixing(Item.of(i.culture_mutton, 3), [i.culture_mutton, Fluid.of(f.diluted_bonemeal, 250), Fluid.of(f.plantoil, 250)], e, 'mutton_culture_mixing', 'heated')
R.createMixing(Item.of(i.culture_rabbit, 3), [i.culture_rabbit, Fluid.of(f.diluted_bonemeal, 250), Fluid.of(f.plantoil, 250)], e, 'rabbit_culture_mixing', 'heated')
R.createMixing(Item.of(i.culture_cod, 3), [i.culture_cod, Fluid.of(f.diluted_bonemeal, 250), Fluid.of(f.plantoil, 250)], e, 'cod_culture_mixing', 'heated')
R.createMixing(Item.of(i.culture_salmon, 3), [i.culture_salmon, Fluid.of(f.diluted_bonemeal, 250), Fluid.of(f.plantoil, 250)], e, 'salmon_culture_mixing', 'heated')

R.fdCutting(i.culture_beef, i.any_knife, Item.of(i.raw_beef, 4), e)
R.fdCutting(i.culture_pork, i.any_knife, Item.of(i.raw_pork, 4), e)
R.fdCutting(i.culture_poultry, i.any_knife, Item.of(i.raw_chicken, 3), e)
R.fdCutting(i.culture_mutton, i.any_knife, Item.of(i.raw_mutton, 4), e)
R.fdCutting(i.culture_rabbit, i.any_knife, Item.of(i.rabbit, 3), e)
R.fdCutting(i.culture_cod, i.any_knife, Item.of(i.cod_slice, 8), e)
R.fdCutting(i.culture_salmon, i.any_knife, Item.of(i.salmon_slice, 8), e)

console.log('cultivated meat done')
})
