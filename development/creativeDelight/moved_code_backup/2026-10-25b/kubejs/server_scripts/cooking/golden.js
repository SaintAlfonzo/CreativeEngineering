ServerEvents.recipes(e => {
    console.log('Golden food starting')

    const R = global.NCH
    const i = global.item
    const f = global.fluid

    // The forge gold tags also hold rose gold (nugget from metaltags.js, ingot and block from MCA); all of it counts.
    // Machines save 2 of the grid's 8 (mixer) or 3 (arc furnace); revisit with food tuning (food/types-and-tuning #1, #3).

    //Golden Carrot
    //Replace default
    R.remove({ id: 'minecraft:golden_carrot' }, e)
    R.shaped(Item.of(i.golden_carrot, 1), ['NNN','NCN', 'NNN'], {C: i.carrot, N: i.any_nugget_gold}, e)
    //Efficient recipes
    R.createMixing(i.golden_carrot, [i.carrot, Ingredient.of([i.any_nugget_gold]).withCount(6)], e, 'golden_carrot_gilding', 'heated')
    R.ieArcFurnace(i.golden_carrot, i.carrot, e, 'golden_carrot_arc_gilding', [], [i.any_nugget_gold, i.any_nugget_gold, i.any_nugget_gold, i.any_nugget_gold, i.any_nugget_gold])

    //Golden Apple
    R.createMixing(i.golden_apple, [i.apple, Ingredient.of([i.any_ingot_gold]).withCount(6)], e, 'golden_apple_gilding', 'heated')
    R.ieArcFurnace(i.golden_apple, i.apple, e, 'golden_apple_arc_gilding', [], [i.any_ingot_gold, i.any_ingot_gold, i.any_ingot_gold, i.any_ingot_gold, i.any_ingot_gold])

    //Enchanted Golden Apple
    const incEnchGoldApple = R.incomplete('enchanted_golden_apple')
    R.createSequencedAssembly(Item.of(i.enchanted_golden_apple, 1), i.golden_apple, [
        e.recipes.create.deploying(incEnchGoldApple, [incEnchGoldApple, i.any_block_gold]),
        e.recipes.create.filling(incEnchGoldApple, [incEnchGoldApple, Fluid.of(f.xp, 250)]),
        e.recipes.create.pressing(incEnchGoldApple, incEnchGoldApple)
    ], ['enchanted_golden_apple'], e, 'enchanted_golden_sequenced_assembly', 4)

    console.log('Golden food done')
})
