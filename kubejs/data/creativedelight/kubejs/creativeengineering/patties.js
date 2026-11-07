// requires: creativedelight
// priority: -90
// requires: farmersdelight
// Patty rule (TODO dishes/meals #55, 2026-11-02): any cooked patty fills a beef patty slot. #creativeengineering:patty
// holds Farmer's Delight's beef patty, Veggies Delight's cooked vegetarian patty and Miner's Delight's vegan patty; every
// recipe that takes the beef patty takes the tag instead, data and script-added (late by priority so the CD scripts'
// recipes are already in). Not swapped: recipes that make a beef patty (cooking, salting it back into itself).
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item

	t.add(e, i.t_patty, i.beef_patty)
	if (Platform.isLoaded('veggiesdelight')) t.add(e, i.t_patty, i.cooked_vegetarian_patty)
	if (Platform.isLoaded('miners_delight')) t.add(e, i.t_patty, i.vegan_patty)
	// The Horror Lasagna's meat tag named the beef patty
	if (Platform.isLoaded('brewinandchewin')) t.add(e, 'brewinandchewin:cooked_horror_lasagna_meats', i.t_patty)
})

ServerEvents.recipes(e => {
	if (!global.CD) return
	var i = global.item
	var R = global.NCH

	// Data recipes: all but the ones that make a beef patty. Script-added recipes (none makes a beef patty): every one;
	// _replaceInAdded takes a single-key filter only, so it is called on its own
	R.replaceInput({ input: i.beef_patty, not: { output: i.beef_patty } }, i.beef_patty, i.t_patty, e)
	R._replaceInAdded({ input: i.beef_patty }, 'input', i.beef_patty, i.t_patty, e)
})
