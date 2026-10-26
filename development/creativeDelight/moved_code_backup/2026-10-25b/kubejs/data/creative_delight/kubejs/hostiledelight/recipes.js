// requires: hostiledelight
// Hostile Delight recipe changes (feature_writeup/hostile_delight.md). Non-member file: recipe ids are
// creative_delight:<type>/<suffix> (global.CD.recipes).
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Upstream bugs #########################
		// B2: ppicklerecp outputs `preapredseapickle` (typo), so Prepared Sea Pickle had no working recipe
		R.remove({ id: 'hostiledelight:ppicklerecp' }, e)
		R.fdCutting(i.sea_pickle, i.any_knife, i.preparedseapickle, e, 'prepared_sea_pickle')

		//######################### Cooking trio #########################
		// Plain cooked meat and eggs are made in all three of smoker, furnace and campfire (food_families.md 3.1
		// `meat/cooked`, `meat/cooked_cut`, `egg/cooked`). Hostile ships smoker and furnace; the campfire takes the
		// same input and xp at FD's campfire time.
		R.campfireCooking(i.grilledghast, i.ghasttentacle, e, 'grilled_ghast', 1, 600)
		R.campfireCooking(i.cookedrottenham, i.rottenham, e, 'cooked_rotten_ham', 1, 600)
		R.campfireCooking(i.friedturtleegg, i.turtle_egg, e, 'fried_turtle_egg', 1, 600)
	})
})
