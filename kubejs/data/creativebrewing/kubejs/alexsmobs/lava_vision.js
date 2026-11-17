// requires: creativebrewing
// requires: alexsmobs
// Recipe ids are creativebrewing:<type>/<suffix> (were creativeengineering:). Alex's Mobs Lava Vision base brew.
//
// AM registers it as  alexsmobs:lava_bottle + alexsmobs:bone_serpent_tooth -> Potion of Lava Vision
// (a Forge BrewingRecipe). The "from" is a lava-bottle ITEM, not a potion, so neither the Create
// nor the IE mechanical mixer can generate it. Creative Common's PotionRecipeGeneratorsMixin cancels
// the broken IE auto-generated recipe and adds the real IE-Mixer one (1500 mB lava + bone serpent
// tooth). This file only adds the Create Mechanical Mixer version.
//
// (The IE side can't be an `e.custom` recipe — kubejs-immersive-engineering ships a MixerRecipeSchema
// that strips the create:potion NBT off the result — so it lives in Creative Common instead.)
//
// Once regular Lava Vision fluid exists, splash / lingering / long_lava_vision all flow from the
// normal container + modifier parity, so nothing else is needed.

ServerEvents.recipes(e => {
if (!global.CB) return
global.CB.recipes(() => {
	const R = global.NCH.forPack('creativebrewing')
	const f = global.fluid

	const LAVA_VISION = '{Bottle:"REGULAR",Potion:"alexsmobs:lava_vision"}'

	// Create Mechanical Mixer — 1000 mB lava + bone serpent tooth -> Lava Vision fluid, heated.
	R.createMixing(
		Fluid.of(f.create_potion, 1000, LAVA_VISION),
		[global.item.bone_serpent_tooth, Fluid.of(f.lava, 1000)],
		e, 'lava_vision_mixing', 'heated'
	)
})
})
