// requires: hostiledelight
// Hostile Delight recipe changes (feature_writeup/hostile_delight.md). Non-member file: recipe ids are
// creativedelight:<type>/<suffix> (global.CD.recipes).
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {
		var F = global.CD.food(e)

		//######################### Bread ####################
		// Their sandwiches and burger took only vanilla bread; any bread works (creativeengineering/bread_tags.js)
		R.replaceInput([{ id: 'hostiledelight:gburgerrecp' }, { id: 'hostiledelight:gsandrecp' }, { id: 'hostiledelight:teggsandrecp' }],
			i.minecraft_bread, i.t_bread, e)

		//######################### Upstream bugs #########################
		// B2: ppicklerecp outputs `preapredseapickle` (typo), so Prepared Sea Pickle had no working recipe
		R.remove({ id: 'hostiledelight:ppicklerecp' }, e)
		R.fdCutting(i.sea_pickle, i.any_knife, i.preparedseapickle, e, 'prepared_sea_pickle')

		//######################### Cooking trio #########################
		// Plain cooked meat and eggs are made in all three of smoker, furnace and campfire (food_families.md 3.1
		// `meat/cooked`, `meat/cooked_cut`, `egg/cooked`). The family calls (CD.FOOD values, 2026-11-01) replace Hostile's
		// smoker and furnace recipes (1 xp) with all three.
		F.meatCook(i.ghasttentacle, i.grilledghast)
		F.meatCook(i.rottenham, i.cookedrottenham)
		F.eggCook(i.turtle_egg, i.friedturtleegg)
	})
})
