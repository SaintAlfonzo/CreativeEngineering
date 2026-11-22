// requires: creativedelight
// priority: -90
// requires: farmersdelight
// Patty rule (TODO dishes/meals #55): any cooked patty fills a beef patty slot. #creativedelight:patty holds Farmer's
// Delight's beef patty, Veggies Delight's cooked vegetarian patty and Miner's Delight's vegan patty; every recipe that
// takes the beef patty takes the tag instead, data and script-added (late by priority so the CD scripts' recipes are
// already in). Not swapped: recipes that make a beef patty (cooking, salting it back into itself).
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item

	t.add(e, i.t_patty, i.beef_patty)
	if (global.CD.on('veggiesdelight')) t.add(e, i.t_patty, i.cooked_vegetarian_patty)
	if (global.CD.on('miners_delight')) t.add(e, i.t_patty, i.vegan_patty)
	// Tofu (TODO dishes/ingredients #17, owner): stands in for the cooked vegetarian patty only, not the beef patty:
	// #creativedelight:veggie_patty holds both and replaces the vegetarian patty in recipes below
	if (global.CD.on('veggiesdelight')) t.add(e, i.t_veggie_patty, [i.cooked_vegetarian_patty, i.tofu])
	// The Horror Lasagna's meat tag named the beef patty
	if (global.has('brewinandchewin')) t.add(e, 'brewinandchewin:cooked_horror_lasagna_meats', i.t_patty)
})

ServerEvents.recipes(e => {
	if (!global.CD) return
	var i = global.item
	var R = global.NCH.forPack('creativedelight')

	// Data recipes: all but the ones that make a beef patty. Script-added recipes (none makes a beef patty): every one;
	// _replaceInAdded takes a single-key filter only, so it is called on its own
	R.replaceInput({ input: i.beef_patty, not: { output: i.beef_patty } }, i.beef_patty, i.t_patty, e)
	R._replaceInAdded({ input: i.beef_patty }, 'input', i.beef_patty, i.t_patty, e)

	// Tofu: Cooking Pot, any 2 overworld mushrooms + nut milk (bottle or jar) -> 1 (as the nut milk pot recipe); then
	// data recipes naming the cooked vegetarian patty take #creativedelight:veggie_patty. Not the slots that already
	// took any patty (the beef patty swap above made them #creativedelight:patty, which holds the vegetarian patty: a
	// swap would drop the beef patty). Script-added: the Vegetarian Burger names the tag itself (veggiesdelight/recipes.js)
	if (!global.CD.on('veggiesdelight') || !global.CD.on('vintagedelight')) return
	global.CD.recipes(() => {
		var MUSHROOM = [i.brown_mushroom, i.red_mushroom, i.t_mushrooms_inky_cap, i.t_mushrooms_morel, i.t_mushrooms_truffle]
		R.fdCooking([MUSHROOM, MUSHROOM, [i.nut_milk_bottle, i.nut_milk_jar]], i.tofu, e, 'tofu', 0.35, 200)
	})
	R.replaceInput({ input: i.cooked_vegetarian_patty, not: { input: i.beef_patty } }, i.cooked_vegetarian_patty, i.t_veggie_patty, e)
})
