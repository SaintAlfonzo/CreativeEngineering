// requires: miners_delight
// Miner's Delight recipe changes (feature_writeup/miners_delight.md). Not a nested member yet: recipe ids are
// creative_delight:<type>/<suffix> (global.CD.recipes).
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH
	var F = global.foodFamily(e)

	global.CD.recipes(() => {

		//######################### Cooking trio #########################
		// Plain cooked meat is made in all three of smoker, furnace and campfire (food_families.md 3.1 `meat/cooked`).
		// The port ships the furnace only; smoker and campfire at FD's times and xp.
		R.smoking(i.cooked_arthropod, i.arthropod, e, 'cooked_arthropod', 0.35, 100)
		R.campfireCooking(i.cooked_arthropod, i.arthropod, e, 'cooked_arthropod', 0.35, 600)

		//######################### Water cup #########################
		// Spout and Item Drain: copper cup + 250 mB water <-> water cup (farmers_respite.md K12)
		R.createBottle(i.water_cup, global.fluid.water, e, 'water_cup', { container: i.copper_cup })
	})
})
