// Vanilla food and kitchen recipe changes. Non-member file: recipe ids are creative_delight:<type>/<suffix>
// (global.CD.recipes). Moved from the pack's kubejs/server_scripts/cooking/cooking.js 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Tools #########################
		// Vanilla's cobblestone smoker removed: the smoker comes from Quark's deepslate / blackstone furnace + 4 logs
		// (quark:building/crafting/furnaces/{deepslate,blackstone}_smoker, Variant Furnaces on). Same rule as the blast furnace.
		R.remove({ id: 'minecraft:smoker' }, e)
		// Bamboo bowl: the vanilla bowl shape in bamboo, 1 bowl (planks give 4, so bamboo isn't the cheap route).
		// TODO cooking/kitchen #10
		R.shaped(i.bowl, ['B B', ' B '], { B: i.bamboo }, e, 'bowl_from_bamboo')

		//######################### Sugar #########################
		// Replace sugar recipes w/ sugar forge tag, allows glyceral (the sugar cube is in supplementaries/recipes.js).
		// Runs after every pack script now; it walks only the recipes loaded from data, as before.
		R.replaceInput({ input: i.sugar }, i.sugar, i.any_sugar, e)

		//######################### Sweets #########################
		// Cakes, pies, cookies, pie crust and cake base: all in creativeengineering/baking.js (baking overhaul,
		// feature_writeup/oven.md 2.6).
		//Remove redundant pie
		R.remove({ id: 'create_central_kitchen:cutting/pumpkin_pie_slice' }, e)
		R.remove({ id: 'create_central_kitchen:cutting/pumpkin_pie_slice_using_deployer' }, e)
		R.remove({ id: 'create_central_kitchen:crafting/pumpkin_pie_from_slices' }, e)
	})
})
