// Alex's Mobs Lava Vision base brew.
//
// AM registers it as  alexsmobs:lava_bottle + alexsmobs:bone_serpent_tooth -> Potion of Lava Vision
// (a Forge BrewingRecipe). The "from" is a lava-bottle ITEM, not a potion, so neither the Create
// nor the IE mechanical mixer can generate it. patcherMod's PotionRecipeGeneratorsMixin cancels
// the broken IE auto-generated recipe and adds the real IE-Mixer one (1500 mB lava + bone serpent
// tooth). This file only adds the Create Mechanical Mixer version.
//
// (The IE side can't be an `e.custom` recipe — kubejs-immersive-engineering ships a MixerRecipeSchema
// that strips the create:potion NBT off the result — so it lives in patcherMod instead.)
//
// Once regular Lava Vision fluid exists, splash / lingering / long_lava_vision all flow from the
// normal container + modifier parity, so nothing else is needed.

ServerEvents.recipes(e => {
    const R = global.NCH
    const f = global.fluid

    const LAVA_VISION = '{Bottle:"REGULAR",Potion:"alexsmobs:lava_vision"}'

    // Create Mechanical Mixer — 1000 mB lava + bone serpent tooth -> Lava Vision fluid, heated.
    R.createMixing(
        Fluid.of(f.create_potion, 1000, LAVA_VISION),
        ['alexsmobs:bone_serpent_tooth', Fluid.of(f.lava, 1000)],
        e, 'lava_vision_mixing', 'heated'
    )
})
