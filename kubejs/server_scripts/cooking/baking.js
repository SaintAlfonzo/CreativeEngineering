// Oven: creativeengineering:baking recipes (NCH.ceBaking) and the oven / bakeware recipes.
// Block, GUI and recipe type: patcherMod oven/ package. Design and tests: feature_writeup/oven.md.
//
// Every baking recipe mirrors an existing pot or crafting recipe for the same baked good: same ingredients,
// counts, result count and plating, written at the original's full cost. The originals stay. A repeated
// ingredient is written once with a count, n(id, 3). With CEConfig [cooking] ovenIngredientCounts off (the
// default) NCH.ceBaking turns that into 3 slots of 1, and a recipe over the oven's 6 slots drops one item at a
// time from its largest repeated group (ties: the group listed first). So counted recipes list filler (milk,
// sugar, wheat) first and the defining ingredient last. Drops and their savings: feature_writeup/oven.md 2.6.
// With the option on, the count stays and the oven takes 3 from one slot (exact cost). Sugar is #forge:sugar,
// as the pack-wide sugar swap in cooking.js gives the originals.
//
// On top of the mirror, the cooking.md "baked" changes (oven.md 2.6): wheat -> flour (cookies, pies; pies also take
// cornmeal), flour instead of dough in muffins, a pie crust in every pie, and Create Crafts & Additions' cake base
// (egg, 2 sugar, dough) in every cake in place of its egg, sugar and wheat/dough.
//
// Bakeware (the oven's tool slot, never used up):
//   baking sheet  cookies, tarts
//   pie tin       pies
//   cake pan      cakes, cheesecake, cake base
//   muffin tin    muffins, plated on paper

ServerEvents.recipes(e => {
	var i = global.item
	var R = global.NCH
	// One oven slot holding `count` of an item or tag.
	var n = (id, count) => ({ id: id, count: count })

	var SHEET = { exp: 0.1, time: 200, tool: i.t_bakeware_baking_sheet }
	var PIE = { exp: 0.1, time: 200, tool: i.t_bakeware_pie_tin }
	var CAKE = { exp: 0.1, time: 200, tool: i.t_bakeware_cake_pan }
	var MUFFIN = { exp: 0.1, time: 200, tool: i.t_bakeware_muffin_tin, container: i.paper }

	//######################### Muffins (muffin tin, paper) #########################
	// Pot recipes in cooking.js (Fruits Delight's), flour instead of dough: fruit, milk, egg, sugar, flour.
	R.ceBaking([n(i.t_fruits_blueberry, 2), i.flour, i.t_milk_bottle, i.eggs, i.any_sugar],
		Item.of(i.blueberry_muffin, 2), e, 'blueberry_muffin', MUFFIN)
	R.ceBaking([n(i.t_fruits_cranberry, 2), i.flour, i.t_milk_bottle, i.eggs, i.any_sugar],
		Item.of(i.cranberry_muffin, 2), e, 'cranberry_muffin', MUFFIN)

	//######################### Cookies (baking sheet) #########################
	// Crafting recipes, 8 cookies each, wheat -> flour.
	R.ceBaking([n(i.flour, 2), i.cocoa_beans], Item.of(i.cookie, 8), e, 'cookie', SHEET)                // minecraft
	R.ceBaking([n(i.flour, 2), i.honey_bottle], Item.of(i.honey_cookie, 8), e, 'honey_cookie', SHEET)  // farmersdelight
	R.ceBaking([n(i.flour, 2), i.sweet_berries], Item.of(i.sweet_berry_cookie, 8), e, 'sweet_berry_cookie', SHEET)
	R.ceBaking([n(i.flour, 2), i.green_tea_leaves], Item.of(i.green_tea_cookie, 8), e, 'green_tea_cookie', SHEET) // farmersrespite
	R.ceBaking([n(i.flour, 2), i.t_fruits_bayberry], Item.of(i.bayberry_cookie, 8), e, 'bayberry_cookie', SHEET) // fruitsdelight
	R.ceBaking([n(i.flour, 2), i.t_fruits_cranberry], Item.of(i.cranberry_cookie, 8), e, 'cranberry_cookie', SHEET)
	R.ceBaking([n(i.flour, 2), i.dried_persimmon], Item.of(i.persimmon_cookie, 8), e, 'persimmon_cookie', SHEET)
	R.ceBaking([n(i.flour, 2), i.t_fruits_lemon, i.t_milk_bottle], Item.of(i.lemon_cookie, 8), e, 'lemon_cookie', SHEET)
	R.ceBaking([n(i.flour, 2), i.mulberry], Item.of(i.mulberry_cookie, 8), e, 'mulberry_cookie', SHEET) // abnormals_delight
	R.ceBaking([n(i.flour, 2), i.mite_crust], Item.of(i.uncanny_cookies, 8), e, 'uncanny_cookies', SHEET) // endersdelight

	//######################### Tarts (baking sheet) #########################
	// Fruits Delight pot recipes.
	R.ceBaking([i.t_fruits_lemon, i.pie_crust, i.eggs, i.any_sugar, i.t_milk_bottle],
		Item.of(i.lemon_tart, 2), e, 'lemon_tart', SHEET)
	R.ceBaking([i.t_fruits_fig, i.pie_crust, i.eggs, i.any_sugar, i.t_milk_bottle],
		Item.of(i.fig_tart, 2), e, 'fig_tart', SHEET)

	//######################### Pies (pie tin) #########################
	// Wheat -> flour (or cornmeal), and every pie has a crust. Crafting: the "3 flour, 2 sugar, crust, 3 fruit" family.
	R.ceBaking([n(i.t_flour_or_cornmeal, 3), n(i.any_sugar, 2), i.pie_crust, n(i.apple, 3)],
		Item.of(i.apple_pie, 1), e, 'apple_pie', PIE)                                      // farmersdelight
	R.ceBaking([n(i.t_flour_or_cornmeal, 3), n(i.any_sugar, 2), i.pie_crust, n(i.chorus_fruit, 3)],
		Item.of(i.chorus_pie, 1), e, 'chorus_pie', PIE)                                    // endersdelight
	R.ceBaking([n(i.t_flour_or_cornmeal, 3), n(i.any_sugar, 2), i.pie_crust, n(i.t_fruits_salmonberries, 3)],
		Item.of(i.salmonberry_pie, 1), e, 'salmonberry_pie', PIE)                          // delightful
	R.ceBaking([n(i.t_flour_or_cornmeal, 3), n(i.any_sugar, 2), i.pie_crust, n(i.mulberry, 3)],
		Item.of(i.mulberry_pie, 1), e, 'mulberry_pie', PIE)                                // create_central_kitchen
	R.ceBaking([n(i.milk, 3), n(i.any_sugar, 2), i.pie_crust, n(i.t_bars_chocolate, 3)],
		Item.of(i.chocolate_pie, 1), e, 'chocolate_pie', PIE)                              // farmersdelight (Delightful's bar version)
	R.ceBaking([n(i.milk, 2), i.honey_bottle, n(i.any_sugar, 2), i.pie_crust, n(i.rose_hips, 3)],
		Item.of(i.rose_hip_pie, 1), e, 'rose_hip_pie', PIE)                                // farmersrespite
	// Pumpkin pie: all three live recipes.
	R.ceBaking([n(i.t_flour_or_cornmeal, 3), i.any_sugar, i.pie_crust, i.eggs, n(i.pumpkin, 3)],
		Item.of(i.pumpkin_pie, 1), e, 'pumpkin_pie', PIE)                                  // delightful
	R.ceBaking([n(i.any_sugar, 2), i.pie_crust, n(i.pumpkin_slice, 6)],
		Item.of(i.pumpkin_pie, 1), e, 'pumpkin_pie_from_slices', PIE)                      // create_central_kitchen
	// These two have no crust in their crafting recipe; the oven adds one.
	R.ceBaking([i.any_sugar, i.duck_egg, i.pie_crust, i.pumpkin],
		Item.of(i.pumpkin_pie, 1), e, 'pumpkin_pie_duck_egg', PIE)                         // untitledduckmod
	R.ceBaking([i.any_sugar, i.eggs, i.pie_crust, i.mulberry],
		Item.of(i.mulberry_pie, 1), e, 'mulberry_pie_simple', PIE)                         // upgrade_aquatic
	// Fruits Delight pot recipes.
	R.ceBaking([n(i.durian_flesh, 2), i.pie_crust, i.eggs, i.any_sugar],
		Item.of(i.durian_pie, 2), e, 'durian_pie', PIE)
	R.ceBaking([n(i.pineapple_slice, 2), i.pie_crust, i.eggs, i.any_sugar],
		Item.of(i.pineapple_pie, 2), e, 'pineapple_pie', PIE)

	//######################### Cakes (cake pan) #########################
	// Every cake starts from a cake base (egg, 2 sugar, dough), which replaces the original's egg, sugar and
	// wheat/dough. That leaves the milk and the flavour, so recipes that only differed in those collapse into one
	// per cake, keeping the main mod's milk count: Create's milk-bucket cake and Create Central Kitchen's dough
	// cakes (3 milk) fold into FD's / Neapolitan's / Farmer's Respite's.
	R.ceBaking([n(i.milk, 3), i.cake_base], Item.of(i.cake, 1), e, 'cake', CAKE)          // farmersdelight, neapolitan
	var FLAVOURED_CAKES = [
		['adzuki_cake', i.roasted_adzuki_beans], ['banana_cake', i.t_fruits_banana],
		['neapolitan_chocolate_cake', i.t_bars_chocolate], ['mint_cake', i.mint_leaves],    // CCK's any-bar chocolate
		['strawberry_cake', i.t_fruits_strawberry], ['vanilla_cake', i.dried_vanilla_pods],
		['coffee_cake', i.farmersrespite_coffee_beans],                                     // farmersrespite
	]
	FLAVOURED_CAKES.forEach(c => {
		R.ceBaking([n(i.milk, 2), i.cake_base, n(c[1], 2)], Item.of(i[c[0]], 1), e, c[0], CAKE) // neapolitan
	})
	// Cake base in the oven: egg, 2 sugar, 2 flour (or cornmeal). A cake from it (+ 3 milk) costs a little less than
	// the 3 wheat craft; Create's compacted base (egg, 2 sugar, 1 dough) stays the cheapest.
	R.ceBaking([i.eggs, n(i.any_sugar, 2), n(i.t_flour_or_cornmeal, 2)], Item.of(i.cake_base, 1), e, 'cake_base', CAKE)
	// The cheesecake is built on a pie crust, not a cake base.
	R.ceBaking([n(i.milk, 2), i.pie_crust, n(i.sweet_berries, 6)],
		Item.of(i.sweet_berry_cheesecake, 1), e, 'sweet_berry_cheesecake', CAKE)           // farmersdelight
	R.ceBaking([i.cake_base, n(i.t_fruits_mangosteen, 2)],
		Item.of(i.mangosteen_cake, 1), e, 'mangosteen_cake',
		{ exp: 0.1, time: 200, tool: i.t_bakeware_cake_pan, container: i.bowl })           // fruitsdelight pot
	// Create Crafts & Additions bakes its cake base in a smoker (100 ticks, no xp).
	R.ceBaking([i.cake_base], Item.of(i.cake_base_baked, 1), e, 'cake_base_baked',
		{ exp: 0, time: 100, tool: i.t_bakeware_cake_pan })

	//######################### Oven and bakeware #########################
	// Oven: an iron-plate top over a hollow brick box.
	R.shaped(i.oven, ['PPP', 'B B', 'BBB'], { P: i.any_plate_iron, B: i.bricks }, e, 'oven')

	// Bakeware: one iron plate hammered on, step by step (Engineer's Hammer, on a cutting board or in the grid):
	// iron plate -> baking sheet -> muffin tin -> pie tin -> cake pan.
	R.hammering(i.baking_sheet, i.any_plate_iron, e, 'baking_sheet')
	R.hammering(i.muffin_tin, i.baking_sheet, e, 'muffin_tin')
	R.hammering(i.pie_tin, i.muffin_tin, e, 'pie_tin')
	R.hammering(i.cake_pan, i.pie_tin, e, 'cake_pan')
})
