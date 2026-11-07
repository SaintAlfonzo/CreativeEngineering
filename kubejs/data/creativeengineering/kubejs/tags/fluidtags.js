// requires: creativeengineering
// Creative Engineering; moved from kubejs/server_scripts/tags/fluidtags.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
ServerEvents.tags('fluid', e => {
	var t = global.tags
	var f = global.fluid

	// ── forge:fluid + a category subtag ────────────────────────────────────
	// [ subtag , fluid(s) ]
	;[
		// vanilla
		['forge:fluid/water', f.water],
		['forge:fluid/lava', f.lava],
		['forge:fluid/milk', f.milk],

		// food / drink: Creative Delight (creativeengineering/fluid_tags.js)

		// plant growth
		['forge:fluid/diluted_bonemeal', f.diluted_bonemeal],
		['forge:fluid/fertilizer', f.fertilizer],
		['forge:fluid/herbicide', f.herbicide],

		// crafting / industrial
		['forge:fluid/slime', f.slime],
		['forge:fluid/acid/redstone', f.redstone_acid],
		['forge:fluid/acid/etching', f.etching_acid],
		['forge:fluid/acetaldehyde', f.acetaldehyde],
		['forge:fluid/phenolic_resin', f.phenolic_resin],
		['forge:fluid/concrete', f.concrete],
		['forge:fluid/plastic', f.plastic],

 		// creosote
		['forge:fluid/creosote', f.creosote],

		// oil chain
		['forge:fluid/plant_oil', f.plantoil],
		['forge:fluid/diesel_sulfur', f.diesel_sulfur],
		['forge:fluid/lubricant_cracked', f.lubricant_cracked],
		['forge:fluid/gasoline_additives', f.gasoline_additives],
		['forge:fluid/naphtha', f.naphtha],
		['forge:fluid/naphtha_cracked', f.naphtha_cracked],
		['forge:fluid/napalm', f.napalm],
		['forge:fluid/benzene', f.benzene],
		['forge:fluid/propylene', f.propylene],
		['forge:fluid/ethylene', f.ethylene],
		['forge:fluid/lpg', f.lpg],

		// magic (potion and dragon's breath rows: Creative Brewing, kubejs/data/creativebrewing/kubejs/creativeengineering/fluid_tags.js;
		// the xp tags and the XP fluid merge: Creative Magic, kubejs/data/creativemagic/kubejs/tags/fluidtags.js)
		['forge:fluid/ink', f.ink],
	].forEach(([tag, fluid]) => t.addFluid(e, tag, fluid))


	// ── forge:fluid only (no meaningful subtag) ────────────────────────────
	/*T.addFluid(e, 'forge:fluid', [
		f.seed_oil, f.vegetable_oil,
		f.bioethanol, f.ie_biodiesel, f.pc_biodiesel,
		f.ie_ethanol, f.pc_ethanol,
		f.crude_oil, f.pc_oil,
		f.ip_kerosene, f.pc_kerosene,
		f.ip_diesel, f.pc_diesel,
		f.ip_lubricant, f.pc_lubricant,
		f.ip_gasoline, f.pc_gasoline,
		f.experience, f.memory_essence,
	])*/

	// ── fixes / removals ──────────────────────────────────────────────────
	var realWater = [f.water, f.flowing_water]
	var badWaterTagged = e.get('minecraft:water').getObjectIds()
		.map(rl => rl.toString())
		.filter(id => realWater.indexOf(id) === -1)

	if (badWaterTagged.length > 0) {
		t.remove(e, 'minecraft:water', badWaterTagged)
		console.log('[fluidtags] stripped minecraft:water tag from: ' + badWaterTagged.join(', '))
	}

	t.clearAll(e, [f.tmted_melon_juice, f.tmted_tomato_sauce, f.tmted_apple_cider, f.thatch]) // dead placeholder fluids
})
