// Creative Delight startup script, loaded from the jar after the pack's startup_scripts, so the dicts exist here.
// Restart to apply.
//
// Pack drink and cooking fluids. Like Central Kitchen's drink fluids they are "virtual": no placed block and no bucket, they live
// in tanks, basins and pipes only. Ids in _lib/fluid_dict.js ("Pack fluids"), forge:fluid/<name> tags in
// server_scripts/tags/fluidtags.js, recipes in server_scripts/cooking/cooking.js ("Fluids"). JEI lists every
// registered fluid (creativecommon client _lib/fluid_jei.js) and shows the bottle's effects on the fluid through its Create
// emptying recipe (Creative Engineering's DrinkFluidEffects).

var reg = global.reg.forPack('creativedelight')
StartupEvents.registry('fluid', e => {
	var f = global.fluid
	// Ender's Delight's chorus juice (TODO food/drinks #4)
	reg.fluid(e, f.chorus_juice).displayName('Chorus Juice').thinTexture(0xB283C9).bucketColor(0xB283C9).noBucket().noBlock()
	// Veggies Delight's juices (feature_writeup/veggies_delight.md 4.9)
	reg.fluid(e, f.carrot_juice).displayName('Carrot Juice').thinTexture(0xF08A24).bucketColor(0xF08A24).noBucket().noBlock()
	reg.fluid(e, f.dandelion_juice).displayName('Dandelion Juice').thinTexture(0xEFCF3A).bucketColor(0xEFCF3A).noBucket().noBlock()
	reg.fluid(e, f.turnip_water).displayName('Turnip Juice').thinTexture(0xE3D5E6).bucketColor(0xE3D5E6).noBucket().noBlock()
	// Cooking fluids (TODO dishes/ingredients #2): egg solution stands in for egg + milk, frosting for the frosting item
	reg.fluid(e, f.egg_solution).displayName('Egg Solution').thinTexture(0xF3D97A).bucketColor(0xF3D97A).noBucket().noBlock()
	reg.fluid(e, f.frosting).displayName('Frosting').thinTexture(0xFFF4F0).bucketColor(0xFFF4F0).noBucket().noBlock()
	// Bone broth (dishes/ingredients #36): Farmer's Delight's Bone Broth bowl as a fluid, 250 mB a bowl (Spout and Item
	// Drain in Creative Delight's farmersdelight/recipes.js); the soupCraft basin recipes take it (cd_food.js). The
	// Moonlight soft fluid stays for jars; this one uses that soft fluid's own textures and tint (Moonlight's soup and
	// potion sprites, in the block atlas). Skipped until Creative Common's fluid dict has f.bone_broth.
	// Without Moonlight it falls back to KubeJS's thin texture in the same tint.
	if (f.bone_broth) {
		var broth = reg.fluid(e, f.bone_broth).displayName('Bone Broth').bucketColor(0xC8B48A).noBucket().noBlock()
		if (Platform.isLoaded('moonlight')) {
			broth.stillTexture('moonlight:soft_fluids/soup_liquid').flowingTexture('moonlight:soft_fluids/potion_flow').color(0xC8B48A)
		} else {
			broth.thinTexture(0xC8B48A)
		}
	}
	// Syrup (largecontent/mod-additions #72b): Rustic Delight's Syrup bottle as a fluid, 250 mB a bottle. Hearth and Harvest
	// 1.4.1's own syrup fluid textures, untinted (copied into the hearthandharvest member). Recipes in Creative Delight's
	// hearthandharvest/recipes.js. Skipped until Creative Common's fluid dict has f.syrup.
	if (f.syrup)
		reg.fluid(e, f.syrup).displayName('Syrup').stillTexture('hearthandharvest:block/fluid/syrup_still')
			.flowingTexture('hearthandharvest:block/fluid/syrup_flow').noBucket().noBlock()
	// Kettle tank drinks (feature_writeup/farmers_respite.md K9): Rustic Delight's coffees past plain Coffee (that one
	// is Central Kitchen's coffee fluid) and Delightful's Kettle drinks. Bottle <-> 250 mB in Creative Delight's
	// farmersrespite/kettle.js; the Kettle reads those pairs to fill and empty bottles.
	var drink = (id, name, color) => reg.fluid(e, id).displayName(name).thinTexture(color).bucketColor(color).noBucket().noBlock()
	drink(f.milk_coffee, 'Milk Coffee', 0xB58560)
	drink(f.honey_coffee, 'Honey Coffee', 0xC08A3E)
	drink(f.chocolate_coffee, 'Chocolate Coffee', 0x5E3B23)
	drink(f.dark_coffee, 'Dark Coffee', 0x2E1C12)
	drink(f.syrup_coffee, 'Syrup Coffee', 0x9C5F2C)
	drink(f.pumpkin_coffee, 'Pumpkin Coffee', 0xC27A34)
	drink(f.cherry_blossom_coffee, 'Cherry Blossom Coffee', 0xD39AA4)
	drink(f.lavender_tea, 'Lavender Tea', 0xA98CCB)
	drink(f.matcha_latte, 'Matcha Latte', 0x9DBE78)
	drink(f.berry_matcha_latte, 'Ice Berry Matcha', 0xBF6E8C)   // renamed (food/drinks #15)
})
