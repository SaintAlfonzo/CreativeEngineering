// requires: miners_delight
// Miner's Delight recipe changes (feature_writeup/miners_delight.md). Not a nested member yet: recipe ids are
// creative_delight:<type>/<suffix> (global.CD.recipes).
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Cooking trio #########################
		// Plain cooked meat is made in all three of smoker, furnace and campfire (food_families.md 3.1 `meat/cooked`).
		// The port ships the furnace only; smoker and campfire at FD's times and xp.
		R.smoking(i.cooked_arthropod, i.arthropod, e, 'cooked_arthropod', 0.35, 100)
		R.campfireCooking(i.cooked_arthropod, i.arthropod, e, 'cooked_arthropod', 0.35, 600)

		//######################### Squid to calamari #########################
		// The squid chain (TODO dishes/ocean #3): a squid drops its squid item (miners_delight/loot.js) and the knife cuts
		// it to calamari and its ink. Calamari 1 + 50 % a second (Rustic's 1-2 squid drop); ink as Miner's own cut
		// (1 + 50 % two more). A glow squid gives glowing calamari and glow ink; a baked squid cooked calamari. Replaces
		// Miner's tentacle cuts.
		if (global.CD.on('rusticdelight')) {
			;[
				[i.squid, i.calamari, i.ink_sac, 'squid'],
				[i.glow_squid, i.glowing_calamari, i.glow_ink, 'glow_squid'],
				[i.baked_squid, i.cooked_calamari, null, 'baked_squid'],
			].forEach(c => {
				R.remove({ type: 'farmersdelight:cutting', input: c[0] }, e)
				var out = [Item.of(c[1]), Item.of(c[1]).withChance(0.5)]
				if (c[2]) out.push(Item.of(c[2]), Item.of(c[2], 2).withChance(0.5))
				R.fdCutting(c[0], i.any_knife, out, e, c[3] + '_to_calamari')
			})
		}

		//######################### Tentacles removed #########################
		// Miner's tentacles, baked tentacles and tentacles on a stick (dishes/ocean #4); dishes take calamari instead
		;[i.tentacles, i.baked_tentacles, i.tentacles_on_a_stick].forEach(t => {
			R.remove({ output: t }, e)
			R.remove({ input: t }, e)
		})

		//######################### Water cup #########################
		// Spout and Item Drain: copper cup + 250 mB water <-> water cup (farmers_respite.md K12)
		R.createBottle(i.water_cup, global.fluid.water, e, 'water_cup', { container: i.copper_cup })
	})
})
