// Creative Engineering; moved from kubejs/server_scripts/engineering/mob_resources.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
ServerEvents.recipes(e => {

    console.log('Hello! The mob resources event has started!')

    const R = global.NCH
    var i = global.item;
    var f = global.fluid;

    // ############################## Leather ##############################

    // Ersatz leather waxing recipe balance
    R.remove({id: 'immersiveengineering:crafting/ersatz_leather'}, e)
    R.shapeless(Item.of(i.ersatz_leather, 1), [i.honeycomb, i.hemp_fabric], e)
    R.createDeploying(Item.of(i.ersatz_leather, 1), [i.hemp_fabric, i.honeycomb], e)

    // ############################## Rotten Flesh ##############################

    //Creosote + smoking leather conversion
    R.createFilling(i.prepared_flesh, [Fluid.of(f.creosote, 250), i.rotten_flesh], e)
    R.createFilling(i.prepared_flesh_block, [Fluid.of(f.creosote, 1000), i.rotten_flesh_block], e)

    R.ieBottling(i.prepared_flesh, f.t_creosote, i.rotten_flesh, e, 'prepared_flesh_from_rotten_flesh_creosote', 200)
    R.ieBottling(i.prepared_flesh_block, f.t_creosote, i.rotten_flesh_block, e, 'prepared_flesh_block_from_rotten_flesh_block_creosote', 750)

    R.smoking(i.leather, i.prepared_flesh, e)
    R.smoking(i.leather_block, i.prepared_flesh_block, e)

    //Prepared flesh
	R.shapeless(Item.of(i.prepared_flesh_block, 1), [Item.of(i.prepared_flesh, 9)], e)
    R.shapeless(Item.of(i.prepared_flesh, 9), [Item.of(i.prepared_flesh_block, 1)], e)

    console.log('The mob resources event has finished!')
})
