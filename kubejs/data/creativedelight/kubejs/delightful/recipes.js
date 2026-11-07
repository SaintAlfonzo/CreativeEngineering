// requires: creativedelight
// requires: delightful
// Recipes of the Delightful 3.8.1 items backported into the repack (feature_writeup/delightful.md 3.2). Creative
// Delight member script: recipe ids are creativedelight:<type>/<suffix> (global.CD.recipes). The 3.8.1 recipes are
// read from the donor jar and rebuilt here; only the ones whose inputs the pack has. The new items are registered
// only by the repack, so this script waits for it (the 3.4.1 jar has no cantaloupe_seeds).
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('delightful') || !Item.exists(global.item.cantaloupe_seeds)) return

	var i = global.item
	var R = global.NCH
	var n = (id, count) => ({ id: id, count: count })
	// as cooking/baking.js
	var PIE = { exp: 0.1, time: 200, tool: i.t_bakeware_pie_tin }
	var SWEET = i.any_sweetener

	global.CD.recipes(() => {
		var F = global.CD.food(e)

		//######################### Cantaloupe #########################
		// Cantaloupe merge (item_merges.md 2.10): this Cantaloupe is the Mini Cantaloupe, its seeds the Mini Cantaloupe
		// Seeds; the slice gives Fruits Delight's seeds. This popsicle is the one popsicle (fruitsdelight/item_merges.js)
		R.shaped(i.cantaloupe, ['cc', 'cc'], { c: i.cantaloupe_slice }, e, 'mini_cantaloupe')
		R.shapeless(i.cantaloupe_bread, [i.any_dough, i.t_fruits_cantaloupe, i.any_sweetener], e, 'cantaloupe_bread')
		R.shapeless(i.wrapped_cantaloupe, [i.t_fruits_cantaloupe, i.cooked_bacon], e, 'wrapped_cantaloupe')
		R.shaped(i.cantaloupe_popsicle, [' cc', 'icc', 'si '], { c: i.t_fruits_cantaloupe, i: i.t_ice_cubes, s: i.any_stick }, e, 'cantaloupe_popsicle')
		R.shaped(i.cantaloupe_popsicle, ['im', 'si'], { m: i.cantaloupe, i: i.t_ice_cubes, s: i.any_stick }, e, 'cantaloupe_popsicle_from_cantaloupe')
		// Served from a whole Cantaloupe (the pot's container slot), placed as a feast of 4
		R.fdCooking([i.rice, i.cooked_mutton, i.onion, i.t_berries, i.t_seeds], i.stuffed_cantaloupe_block, e, 'stuffed_cantaloupe', 0.35, 400, i.cantaloupe)

		//######################### Field salad #########################
		// (TODO dishes/meals #39) Upstream: 2 salad ingredients, cactus steak, carrot, salmonberries, acorn, bowl. Each
		// slot takes a set (delightful/tags.js): any berry; carrot, onion, beetroot, corn or turnip; acorn or peanut;
		// cactus steak, broccoli, cauliflower, zucchini, cucumber or green bell pepper. A second recipe takes 2 flowers
		// in place of the 2 salad ingredients (cabbage, lettuce, dandelion leaves).
		R.remove({ id: 'delightful:food/field_salad' }, e)
		var FIELD_SALAD = [i.t_field_salad_green, i.t_field_salad_root, i.t_berries, i.t_field_salad_nut, i.bowl]
		R.shapeless(i.field_salad, [i.salad_ingredients, i.salad_ingredients].concat(FIELD_SALAD), e, 'field_salad_manual_only')
		R.shapeless(i.field_salad, [i.t_flowers, i.t_flowers].concat(FIELD_SALAD), e, 'field_salad_flowers_manual_only')

		//######################### Gummies #########################
		// Pot: 1 each; heated mixer with a honey block: 9
		var GUMMIES = [[i.cantaloupe_gummy, i.t_fruits_cantaloupe], [i.matcha_gummy, i.t_matcha]]
		GUMMIES.forEach(g => {
			var name = g[0].split(':')[1]
			R.fdCooking([g[1], i.any_sweetener, i.honey_bottle, i.kelp], g[0], e, name, 1.0, 200)
			R.createMixing(Item.of(g[0], 9), [g[1], i.any_sweetener, i.honey_block, i.kelp], e, name, 'heated')
		})

		//######################### Cactus #########################
		// corn comes from Corn Delight (#forge:crops/corn); without it the recipe would name a tag that does not exist
		if (global.has('corn_delight')) R.fdCooking([i.raw_pork, i.t_crops_cactus, i.tomato_sauce, i.t_hot_spices, i.t_crops_corn], i.cactus_chili, e, 'cactus_chili', 1.0, 200, i.bowl)
		// The Cactus Soup Cup is the Copper Pot's serving (cup_conversion in the repack)
		R.fdCooking([i.t_crops_cactus, i.rice, i.tomato, i.onion], i.cactus_soup, e, 'cactus_soup', 1.0, 200, i.bowl)

		//######################### Baklava #########################
		// A pie here (oven.md 2.6): pie crust + 2 roasted nuts + honey + sweet in the oven, pie tin. Upstream's grid
		// recipe (dough, nuts, honey, citrus or sugar, hot spice) is not added.
		R.ceBaking([i.pie_crust, n(i.t_cooked_nuts, 2), i.honey_bottle, SWEET], Item.of(i.baklava, 1), e, 'baklava', PIE)
		R.fdCutting(i.baklava, i.any_knife, Item.of(i.baklava_slice, 4), e, 'baklava')
		R.shapeless(i.baklava, [i.baklava_slice, i.baklava_slice, i.baklava_slice, i.baklava_slice], e, 'baklava_from_slices')

		//######################### Milkshakes #########################
		// Grid: 3 bottles + ice cream + milk -> 3. The cauldrons (milk cauldron + ice cream) are Neapolitan's way, in the repack.
		R.shapeless(Item.of(i.matcha_milkshake, 3), [i.glass_bottle, i.glass_bottle, i.glass_bottle, i.matcha_ice_cream, i.milk], e, 'matcha_milkshake')
		R.shapeless(Item.of(i.salmonberry_milkshake, 3), [i.glass_bottle, i.glass_bottle, i.glass_bottle, i.salmonberry_ice_cream, i.milk], e, 'salmonberry_milkshake')

		//######################### Nuts #########################
		// Nuts cook in the fruit family's trio (food type raw/fruit holds nuts; CD.FOOD values)
		F.fruitCook(i.t_nuts_acorn, i.roasted_acorn)
		// Nut mash (merges/dishes #7, owner 2026-11-02): nut dough is removed (vintage_items.js) and has no recipe; nut
		// butter and the Nut Butter and Jelly Sandwich merge into Vintage's nut mash jar and sandwich (swaps
		// merges_delightful.json). Their own recipes go first, so the merge does not carry them over; the nut mash and
		// sandwich recipes are in vintagedelight/recipes.js and cooking.js.
		;['delightful:food/cooking/nut_butter_bottle', 'delightful:food/nut_butter_and_jelly_sandwich',
			'delightful:food/nut_butter_and_jelly_sandwich_from_bread_slice'].forEach(id => R.remove({ id: id }, e))

		//######################### Knives #########################
		R.shaped(i.aluminum_knife, ['m', 's'], { m: i.ingot_aluminum, s: i.any_stick }, e, 'aluminum_knife')
		// certus quartz and fluix are Applied Energistics 2's
		if (global.has('ae2')) {
			R.shaped(i.certus_quartz_knife, ['m', 's'], { m: i.t_gems_certus_quartz, s: i.any_stick }, e, 'certus_quartz_knife')
			// 1.20's smithing template is not in AE2 1.19.2: 1.19.2 smithing, Certus Quartz Knife + Fluix Block
			R.smithing(i.fluix_knife, i.certus_quartz_knife, i.fluix_block, e, 'fluix_knife')
		}

		// The rows below moved from the pack's kubejs/server_scripts/cooking/cooking.js 2026-10-25, rows unchanged.

		//######################### Cooked rice #########################
		// Shapeless craft rice dishes w/ cooked_rice (heatless meal rule, farmersdelight/recipes.js "Meals")
		F.mealCraft([i.cooked_rice, i.cooked_fish, i.cooked_fish, i.cooked_pork, i.tomato_sauce, i.onion, i.fruit], i.sinigang)

		//######################### Berry matcha latte #########################
		// With Farmer's Respite loaded Delightful drops its grid lattes, so the berry latte had only a grid recipe that ate
		// an extra bottle (TODO food/drinks #5). Kettle: a tier like FR's, berries on the Matcha Latte in the tank (Creative
		// Delight farmersrespite/kettle.js, needs its Kettle tank). Grid: latte + berries + ice; the latte's bottle comes back.
		R.remove({ id: 'delightful:food/berry_matcha_latte_from_matcha_latte_neapolitan' }, e)
		// Its grid recipe (Ice Berry Matcha): creativeengineering/drinks.js (food/drinks #15)
	})
})
