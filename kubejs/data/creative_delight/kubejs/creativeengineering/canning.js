// Cans: the generated food cans (tools/gen_can_types.py -> kubejs/generation_scripts/food/can_types.json, read from
// the pack folder), the empty can recipes and the Canned Goods removals. Moved from the pack's
// server_scripts/cooking/canning.js 2026-10-25, rows unchanged; recipe ids are creative_delight:<type>/<suffix>
// (global.CD.recipes). canSection() serves every mod's cans, so the file stays whole. Items: startup cans.js.

ServerEvents.recipes(e=>{
if (!global.CD) return
console.log('canning starting')

const R = global.NCH
var f = global.fluid
var i = global.item;

global.CD.recipes(() => {

	// Generated cans (tools/gen_can_types.py -> generation_scripts/food/can_types.json): one meal = the food it holds x the
	// section multiplier, effects copied with scaled durations. Each can has its recipes in the json: n of the source(s) (any
	// of the listed alternatives per slot) + an empty can, plus the additive variants. canSection('x') makes the recipes of one
	// section; recipe ids are the can key plus the variant suffix (canned_cookies, canned_cookies_sugar). Cans are only ever
	// crafted full (sliced inputs share the whole food's count). A can with "pie" also turns back into that pie with a pie crust:
	// a full can only, by hand only (NCH cans { whole }: an opened can, Create's mixer / mechanical crafter and other mods'
	// auto-crafters get nothing). The same filling + crust also bakes in the oven, and fillings also come from the Cooking
	// Pot: creativeengineering/baking.js PIES.
	// A can with "pot" is made in the Cooking Pot instead, the empty can in its container slot.
	// Every can is generated, the raw veggie / fruit / meat ones too: the hand-written numbers below are what the generator's
	// sizing gives.
	// Cans with no recipe (Canned Goods' own cans the pack doesn't make) only get their food values (cans.js).
	var canTypes = {}
	try {
		canTypes = JSON.parse(String(JsonIO.readString('kubejs/generation_scripts/food/can_types.json'))).cans
	} catch (err) {
		console.error('[canning] could not read can_types.json, run tools/gen_can_types.py: ' + err)
	}
	function canSection(section) {
		var made = 0
		Object.keys(canTypes).forEach(key => {
			var can = canTypes[key]
			if (can.section !== section) return
			can.recipes.forEach(rec => {
				try {
					var input = can.pot ? [] : [i.empty_can]
					rec.inputs.forEach(inp => {
						if (can.pot) { // the pot takes one item per slot
							for (var p = 0; p < inp.n; p++) input.push(inp.any.length === 1 ? i[inp.any[0]] : inp.any.map(k => i[k]))
							return
						}
						var ids = inp.any.map(k => i[k])
						ids.forEach((id, slot) => {
							if (typeof id !== 'string') throw new Error(inp.any[slot] + ' is not in item_dict')
						})
						if (ids.length === 1) {
							input.push(inp.n > 1 ? Item.of(ids[0], inp.n) : ids[0])
						} else {
							for (var n = 0; n < inp.n; n++) input.push(ids) // every slot takes any of the alternatives
						}
					})
					if (typeof i[key] !== 'string') throw new Error(key + ' is not in item_dict')
					var out = Item.of(i[key], 1)
					if (can.pot) {
						input.forEach((id, slot) => { if (typeof id !== 'string' && !Array.isArray(id)) throw new Error('pot slot ' + slot + ' is not in item_dict') })
						R.fdCooking(input, out, e, key + rec.suffix, 1.0, 200, i.empty_can)
					}
					else R.shapeless(out, input, e, key + rec.suffix)
					made++
				} catch (err) {
					console.warn('[canning] ' + key + rec.suffix + ' skipped: ' + err)
				}
			})
			if (can.pie) {
				if (typeof i[can.pie] !== 'string') console.warn('[canning] ' + key + '_pie skipped: ' + can.pie + ' is not in item_dict')
				else { R.shapeless(Item.of(i[can.pie], 1), [i[key], i.pie_crust], e, key + '_pie', null, null, { whole: i[key] }); made++ }
			}
		})
		console.log('[canning] ' + section + ': ' + made + ' recipes')
	}

	// ============================================================
	// Other Changes TODO
	// ============================================================

	// BUILT (patcherMod food.CanServings, feature_writeup/food.md 3.6):
	// - Cans hold several servings: per can in generation_scripts/food/can_servings.json -> #creativeengineering:cans/servings/<n>
	//   (tags/cantags.js). Servings left are NBT, not durability, so full cans still stack to 64 and can't be enchanted;
	//   a partly eaten can shows a durability-style bar and "Servings: x / y".
	// - The last serving rolls an empty can / crushed can / nothing: CEConfig [canning] emptyCanChance, crushedCanChance.
	// - In recipes: R.shaped(..., cans) / R.shapeless(..., cans), cans = true -> each can loses one serving per craft
	//   (same roll on the last one); cans = { whole: id } -> the can is used up and must be full. Example:
	//   R.shapeless(Item.of(i.beef_cooked), [i.canned_beef], e, 'uncan_beef', null, null, true)
	//   Every recipe that takes a can goes through this argument: NCH makes it manual only (_manual_only id, no result on
	//   machine crafters), so Create, IE and AE2 never use up a whole can.

	// TODO 
	// Canning automation

	// Not canned (removed 2026-10-13): mint, acorn, grapes, mite crust, raw ham/chevon/hoglin/kangaroo/moose/bison/bunfungus.
	// Coverage worksheet: feature_writeup/can_parity.md
	// ensure that food effects carry over from the ingredients or can be added manually, this will be more important later. verify the bowl from bone broth is returned. 

	// Design:
	// Cans hold multiple meals, increasing meals per stack
	// Raw, low hp, low sat food can be canned with many ingredients to make can with fewer, high value meals
		// Encourages canning to increase value
	// Cooked, mid value (high tier vanilla) foods can with several ingredients to get can with several valuable meals, slightly increased value
		// Benefits from stack increase, slight value increase, mostly static
	// Prepared meals, high value foods can with few ingredients to get can with many valueable meals, higher value overall
		// Used for rationing high value meals, stackable effect foods, lower SOL value overall

	// ============================================================
	// Removals
	// ============================================================

	// Every Canned Goods food can (2x food + iron ingot, plus the Hot Hot Hotpot), its own can
	// recipes, and can -> iron ingot recycling (with 4+ cans per ingot below, that would duplicate iron).
	// Food canning comes back later, built on the #creativeengineering:food_type tags (feature_writeup/food.md).
	// Kept: Soda, Reef-Cola, Oculemonade, Mini Fizz (drinks made in a can), canned dirt, canned golem.
	R.bulk('remove', [
		// food cans
		'aaa', 'recipe_canned_apples', 'recipe_canned_beef', 'recipe_canned_beets', 'recipe_canned_carrots',
		'recipe_canned_chicken', 'recipe_canned_chorus_fruit', 'recipe_canned_fish', 'recipe_canned_glow_berries',
		'recipe_canned_mushrooms', 'recipe_canned_pork', 'recipe_canned_sweet_berries', 'recipe_dried', 'recipebp',
		'recipebrs', 'recipecakje', 'recipecie', 'recipecm', 'recipecr', 'recipecs', 'recipeg_a', 'recipegc', 'recipehb',
		'recipems', 'recipena', 'recipep', 'recipepp', 'recipepuffer', 'recipepumpkin', 'reciperf', 'recipers', 'recipese',
		'recipesusstew', 'recipetrop', 'hot_hot_hotpot_recipe',
		// can crafting
		'ironintocan', 'irontocrushedcan',
		// can -> iron ingot
		'fullemptycan', 'recycling_cans',
	].map(id => [{ id: 'canned_goods:' + id }, e]))

	// ============================================================
	// Cans
	// ============================================================

	// Cans per iron ingot: stonecutter 4, crafting 6, deployer 6, Engineer's Workbench 8 (a plate is one ingot).
	R.stonecutting(Item.of(i.empty_can, 4), i.ingot_iron, e, 'empty_can')
	R.shaped(Item.of(i.empty_can, 12), ['P', 'P'], { P: i.any_plate_iron }, e, 'empty_can')
	R.createDeploying(Item.of(i.empty_can, 12), [i.any_plate_iron, i.any_plate_iron], e, 'empty_can')
	R.ieBlueprint(Item.of(i.empty_can, 32), [Item.of(i.plate_iron, 4)], 'components', e, 'empty_can')

	// Can Recycle
	// WIP

	// ============================================================
	// Canning
	// ============================================================

	// Can be done with ingredient + can
	// Ingredient + can + additive stretches ingredient, better yield
	// Veggie
	    // Raw - Salt, Bone Broth, Vinegar
	// Fruit
	    // Raw - Sugar, Honey (return bottle), Vinegar
	// Meat
	    // Raw, cooked - Salt, Bone Broth, Vinegar

	// Vinegar added later

	// ============================================================
	// Raw Veggie
	// ============================================================

	// Goal
	// (Baked Potato- 5, 6) sized meals

	// Food- Hunger Points, Saturation
	// Ingredient to Baked Potato Ratio
	// Basic Canning Ratio
	// Advanced Canning Ratio

	// Beetroot- 1, 1.2, 2 Meals/Can
	// 5x ==  1BP            
	// 8x --  1BP, 2M 
	// 6x* -> 1BP, 2M

	// Cabbage- 2, 1.6
	// 3x  ~= 1BP
	// 5x  -- 1BP, 2M
	// 3x* -> 1BP, 2M

	// Carrot- 3, 3.6
	// 2x  ~= 1BP
	// 3x  -- 1BP, 2M
	// 2x* -> 1BP, 2M

	// Onion- 2, 1.6
	// 3x  ~= 1BP
	// 5x  -- 1BP, 2M
	// 3x* -> 1BP, 2M

	// Potato- 1, 0.6
	// 7x  ~= 1BP
	// 8x  -- 1BP, 2M
	// 6x* -> 1BP, 2M

	// Tomato- 1, 0.6
	// 7x  ~= 1BP
	// 8x  -- 1BP, 2M
	// 6x* -> 1BP, 2M

	// Bell Pepper (Green, Red, Yellow)- 1, 0.2
	// 9x  ~= 1BP
	// 8x  -- 1BP, 2M
	// 6x* -> 1BP, 2M

	// Cactus Flesh- 3, 1.8
	// 2x  ~= 1BP
	// 4x  -- 1BP, 2M
	// 3x* -> 1BP, 2M

	// Gongylidia- 3, 7.2
	// 1x  ~= 1BP
	// 2x  -- 1BP, 2M
	// 1x* -> 1BP, 2M

	// Maize- 2, 0.8 (maize only: a maize cuts into 4 kernels, which are not canned)
	// 6x  -- 1BP, 2M
	// 4x* -> 1BP, 2M

	// Generated (section raw_veggie): the counts above are the sizing rule's (basic recipe about 0.8 of the can, the
	// salt / bone broth one 0.7 of that).
	canSection('raw_veggie')

	// ============================================================
	// Raw Fruit
	// ============================================================

	// Goal
	// (Fruit Meal- 5, 3) sized meals, 3 Meals/Can
	// Largest meal where the lowest value fruit (2, 0.4) still fits the grid at the basic ratio

	// Food- Hunger Points, Saturation
	// Ingredient to Fruit Meal Ratio
	// Basic Canning Ratio
	// Advanced Canning Ratio

	// Apple- 4, 2.4
	// 1x  ~= 1FM
	// 3x  -- 1FM, 3M
	// 2x* -> 1FM, 3M

	// Banana (Neapolitan only; Alex's Mobs bananas are not canned, can_parity.md)- 2, 0.8
	// 3x  ~= 1FM
	// 7x  -- 1FM, 3M
	// 5x* -> 1FM, 3M

	// Cantaloupe Slice- 3, 1.8 (set to the Pumpkin Slice's values, startup_scripts/cooking/food_values.js)
	// Slices only: the whole cantaloupe can't be eaten. Same for pumpkin, melon, hamimelon and pineapple.
	// 2x  ~= 1FM
	// 4x  -- 1FM, 3M
	// 3x* -> 1FM, 3M

	// Cherry- 2, 1.2
	// 3x  ~= 1FM
	// 6x  -- 1FM, 3M
	// 4x* -> 1FM, 3M

	// Glow Berries- 2, 0.4
	// 3x  ~= 1FM
	// 8x  -- 1FM, 3M
	// 6x* -> 1FM, 3M

	// Melon Slice- 2, 1.2
	// 3x  ~= 1FM
	// 6x  -- 1FM, 3M
	// 4x* -> 1FM, 3M

	// Mulberry- 3, 0.6
	// 2x  ~= 1FM
	// 5x  -- 1FM, 3M
	// 3x* -> 1FM, 3M

	// Pumpkin Slice- 3, 1.8 (slices only, the whole pumpkin is a block)
	// 2x  ~= 1FM
	// 4x  -- 1FM, 3M
	// 3x* -> 1FM, 3M

	// Salmonberries- 2, 2.4
	// 2x  ~= 1FM
	// 4x  -- 1FM, 3M
	// 3x* -> 1FM, 3M

	// Strawberries- 3, 0.3
	// 2x  ~= 1FM
	// 6x  -- 1FM, 3M
	// 4x* -> 1FM, 3M

	// White Strawberries- 5, 0.5
	// 1x  ~= 1FM
	// 3x  -- 1WM, 3M (own White Strawberry Meal- 7, 5: a much bigger serving, 140% of the berries' hunger)
	// 2x* -> 1WM, 3M

	// Sweet Berries- 2, 0.4
	// 3x  ~= 1FM
	// 8x  -- 1FM, 3M
	// 6x* -> 1FM, 3M

	// The fruits above and the other fruits (fruitsdelight etc.) are generated with the same Fruit Meal and the same sizing
	// rule: the basic recipe is worth about 0.8 of the can, the sugar/honey one 0.7 of that. Sliced lemon/orange take the
	// whole fruit's slots and count. Coffee berries keep their Wither, cut to 2 s a serving.
	canSection('raw_fruit')

	// ============================================================
	// Raw Meat
	// Sliced cuts share the whole meat's recipe: the forge:raw_* tags hold exactly the whole meat and its cut (minced
	// beef, chicken cuts, mutton chops, bacon), so the count is the whole meat's and a can is always full.
	// ============================================================

	// Goal
	// (Meat Meal- 5, 3) sized meals, 4 Meals/Can
	// Largest meal where the lowest value meat (2, 1.2) still fits the grid at the basic ratio

	// Food- Hunger Points, Saturation
	// Ingredient to Meat Meal Ratio
	// Basic Canning Ratio
	// Advanced Canning Ratio

	// Beef- 3, 1.8
	// 2x  ~= 1MM
	// 5x  -- 1MM, 4M
	// 4x* -> 1MM, 4M

	// Chicken- 2, 1.2
	// 3x  ~= 1MM
	// 8x  -- 1MM, 4M
	// 6x* -> 1MM, 4M

	// Mutton- 2, 1.2
	// 3x  ~= 1MM
	// 8x  -- 1MM, 4M
	// 6x* -> 1MM, 4M

	// Porkchop- 3, 1.8
	// 2x  ~= 1MM
	// 5x  -- 1MM, 4M
	// 4x* -> 1MM, 4M

	// Rabbit- 3, 1.8
	// 2x  ~= 1MM
	// 5x  -- 1MM, 4M
	// 4x* -> 1MM, 4M

	// Generated with the meats above: same Meat Meal and sizing rule. Shulker has its own small meal (Shulker Filet is an
	// alt input).
	canSection('raw_meat')

	// ============================================================
	// Raw Fish
	// ============================================================

	// Goal: (Fish Meal- 4, 2) sized meals, 4 Meals/Can, salt or bone broth (since 2026-10-20; was the Meat Meal 5, 3). Every sliced fish takes the whole fish's
	// slots and count (a full can either way). Tropical Fish reuses Canned Goods' can.
	canSection('raw_fish')

	// ============================================================
	// Hazard
	// ============================================================

	// Canned Goods' own cans (Pufferfish, Poisonous Potatos, Night Berries = spider eyes, Tough Jerky = rotten flesh):
	// Meat Meal sized, no additive. Canned Goods' code applies the bad effects when a serving is eaten. Canned Maggots: its own
	// small meal (2, 1), like shulker.
	canSection('hazard')

	// ============================================================
	// Cookies
	// ============================================================

	// Goal
	// (Cookie Type x2) sized meals, 5 Meals/Can
	// All fast
	// Sugar additive

	// Food- Hunger Points, Saturation

	// Cookie- 2, 1.2
	// (4, 2.4) x5
	canSection('cookies')

	// ============================================================
	// Cooked Veggies
	// ============================================================

	// Goal
	// (Veggie Item x2) -> 4 Meals/can
	// 6 Veggies per can craft, salt additive for 5 veggies

	// Food- Hunger Points, Saturation

	// Baked Potato (accepts slices)
	// Cactus Steak
	// Cooked Rice
	// Dried Kelp
	// Roasted Bell Pepper (all colors)

	// Meal = the item x2 (hunger and saturation points), so a can keeps the item's modifier. The sliced baked potato counts as a
	// baked potato. Generated: can_types.json, section cooked_veggie.
	canSection('cooked_veggie')

	// ============================================================
	// Cooked Meat - all fish accept sliced
	// Not canned cooked: patties, bacon, bison, smoked ham, chevon, hoglin, kangaroo, moose, bunfungus, lobster, crab
	// ============================================================

	// Goal
	// (Meat Item x2) -> 3 Meals/can
	// 5 Meat per can craft, salt additive for 4 meat

	// Meal = the item x2. Whole and sliced cuts share a can for chicken, mutton and every fish that has a sliced form.
	// Honey Glazed Porkchop, Jerky and Maple Glazed Bacon get their own cans. The additive is salt or bone broth, like the
	// raw meat cans. Fish are in the same section (cooked_meat) of can_types.json.
	canSection('cooked_meat')

	// ============================================================
	// Bread
	// ============================================================

	// Goal
	// (Bread Type x2) sized meals, 4 Meals/Can
	// 6 Bread / can
	// No additives, special honey bread additive recipe, All bread items but scones, bread slice, toast, tortilla

	// Food- Hunger Points, Saturation 

	// Bread- 5, 6
	// (10, 12) x4 meals
	// Canned Honey Bread also has a second recipe: 5 bread + a honey bottle.
	canSection('bread')

	// ============================================================
	// Salad
	// ============================================================

	// Goal
	// (Salad Type x1.25) -> 8 Meals/can 
	// 6 Salads / can
	// Vinegar, apple cider vinegar additive (to be added) for 5 salads
	// All fast, copy effect with shorter duration (.75x)

	// Food- Hunger Points, Saturation

	// All salads but potato salad, maggot salad

	// Meal = the salad x1.25, fast, effects at 0.75x duration, 8 meals per can. No additive yet (vinegar comes later:
	// 5 salads + vinegar). Generated: can_types.json, section salad.
	canSection('salad')

	// ============================================================
	// Soup
	// ============================================================

	// Goal
	// (Soup Type x1.25) sized meals, 8 Meals/can
	// 6 Soups / can
	// Salt, bone broth additive for 3 soups
	// Copy effect with shorter duration (.75x)

	// Food- Hunger Points, Saturation

	// All soups but cave explorer and the wooden soups that were removed

	// Meal = the soup x1.25, effects at 0.75x duration, 8 meals per can; 3 soups + salt or bone broth also works.
	// Suspicious stew is left out: its effects come from its NBT, a can would lose them. Generated: can_types.json, section soup.
	canSection('soup')

	// ============================================================
	// Meals
	// ============================================================

	// Goal
	// TBD

	// Food- Hunger Points, Saturation

	// Pastas (Bell pepper, cheesy, crab, kangaroo, lobster, meatballs, mutton chops, pearl[not wood], squid ink)
		// include [veg noodles, duck noodles], rename to veg pasta and pasta w/ duck

	// Your suggestions for other meals in alignment with irl canning and the other cans in this mod?

	// Placeholder until the goal is set (gen_can_types.py SECTIONS 'meal'): 2 pastas -> 4 meals of 0.75 pasta, effects
	// 0.75x. Duck and vegetable noodles can as Pasta with Duck / Vegetable Pasta.
	canSection('meal')

	// ============================================================
	// Pie
	// ============================================================

	// Goal
	// 1 Can = 1 Pie
	// (1 Pie Type Slice) sized meals, 4 Meals/can
	// Effect same as pie slice
	// Pie crust replacement recipe, generated from existing pie recipes dynamically
	// 4 pie slices + can to canned pie
	// Can + pie crust to pie

	// Rename canned to 'pie filling'

	// Food- Hunger Points, Saturation

	// All pie (pie filling), pie + pie crust recipe

	// Generated (section pie): 4 slices -> filling, slice-sized meals, effects as the slice. Fruitsdelight's durian and
	// pineapple pies and the fig and lemon tarts (pies to be) are single items, 4 of them fill a can. Pot fillings and the
	// crust + filling oven recipes: creativeengineering/baking.js. Sweet Berry Pie is Farmer's Delight's cheesecake, renamed. Slices whose
	// whole pie is from a mod not in the pack get no can (gen_can_types.py lists them).
	canSection('pie')

	// ============================================================
	// Feasts
	// ============================================================

	// Goal
	// 1~2 feasts -> 6-8 meals

	// Food- Hunger Points, Saturation

	// All, including stuffed shulker, combine nether delight into single canned hoglin feast

	// Placeholder (section feast): one placed feast block -> 6 servings worth the feast's serving. The stuffed hoglin's
	// three plates share Canned Hoglin Feast (valued as the ham plate).
	canSection('feast')

	// ============================================================
	// Drinks
	// ============================================================

	// Goal
	// TBD

	// Food- Hunger Points, Saturation

	// Honey bottles, milk (powdered milk)

	// Placeholder (section drink): 4 -> 4 servings. Honey reuses Canned Goods' can (its code clears poison); Canned Powdered
	// Milk clears every effect like milk (cans.js), 1 hunger. Buckets and bottles are given back by the crafting grid.
	canSection('drink')

	// ============================================================
	// Golden, Enchanted Golden
	// ============================================================

	// Goal
	// (Golden Type x0.25) -> 10 Meals/can
	// 2 golden -> canned golden
	// Copy effect with shorter duration (.25x)
	// Required honey additive

	// Food- Hunger Points, Saturation

	// All

	// Meal = the golden food x0.25 (1 hunger for a golden apple), effects at 0.25x duration, 10 meals per can.
	// 2 golden foods + a honey bottle (returns the bottle). New cans: Canned Goods' own golden apple / carrot / notch apple
	// cans add code effects on top of their food, so they are not reused. Generated: can_types.json, section golden.
	canSection('golden')

	// ============================================================
	// Special
	// ============================================================

	// Goal
	// TBD

	// Food- Hunger Points, Saturation

	// Chorus fruit, ancient fruit

	// Fruit Meal sized like raw fruit. Chorus reuses Canned Goods' can (its code teleports).
	canSection('special')

	// ============================================================
	// Egg and Dairy
	// ============================================================

	// Goal
	// (Cheese Wedge x1.25) sized meals, 4 Meals/can, processed cheese
	// 4 wedges in the Cooking Pot, the empty can in the container slot

	// Flaxen Cheese Wedge, Scarlet Cheese Wedge
	canSection('dairy')

	// ============================================================
	// Preserves
	// ============================================================

	// Goal
	// (Item x1) sized meals, 4 Meals/can
	// 4 items / can

	// Tomato Sauce (the bowls come back)
	// Dough: 8 Create dough / can, 8 servings, not edible; an ingredient can for later crafts (NCH cans arg, a serving per craft)
	canSection('preserve')

	// ============================================================
	// Desserts
	// ============================================================

	// Goal
	// (Dessert Type x1.25) sized meals, 8 Meals/can, like soups
	// 6 desserts / can, sugar or honey additive for 3
	// Copy effect with shorter duration (.75x)

	// Pear with Rock Sugar (the bowls come back)
	canSection('dessert')

	// ============================================================
	// Cake
	// ============================================================

	// Goal
	// 1 Can = 1 Cake
	// (Cake Slice x1.75) sized meals, 4 Meals/can (a cake is 7 slices), fast
	// 1 cake, or 4 slices + sugar / honey

	// Canned Goods' own Canned Cake
	canSection('cake')


})
console.log('canning done')
})
