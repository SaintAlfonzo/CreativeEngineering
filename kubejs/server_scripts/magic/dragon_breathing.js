ServerEvents.recipes(e => {
    console.log('Dragon breathing starting')

    const R = global.NCH
    const i = global.item

    
    R.dragonBreathing(Item.of(i.shulker_shell, 1).withChance(0.25), i.bucket, e)
    R.dragonBreathing(i.dragon_breath, i.thick_potion, e, 'thick_potion_to_dragon_breath')
    // Halitosis mirror
    R.dragonBreathing(i.chorus_fruit, i.apple, e)
    R.dragonBreathing(i.end_rod, i.blaze_rod, e)
    R.dragonBreathing(i.end_stone, i.cobblestone, e)
    R.dragonBreathing(i.ender_pearl, i.prismarine_crystals, e)
    R.dragonBreathing(i.end_stone_bricks, i.stone_bricks, e)
    R.dragonBreathing(i.end_stone_brick_slab, i.stone_brick_slab, e)
    R.dragonBreathing(i.end_stone_brick_stairs, i.stone_brick_stairs, e)
    R.dragonBreathing(i.end_stone_brick_wall, i.stone_brick_wall, e)
    R.dragonBreathing(i.dusky_myalite, i.dark_prismarine, e)
    R.dragonBreathing(i.myalite, i.prismarine, e)
    R.dragonBreathing(i.myalite_crystal, i.any_corundum, e)
    R.dragonBreathing(i.end_stone_brick_vertical_slab, i.stone_brick_vertical_slab, e)

    console.log('Dragon breathing done')
})
