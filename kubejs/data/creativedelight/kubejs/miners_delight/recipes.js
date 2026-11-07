// requires: creativedelight
// requires: miners_delight
// Miner's Delight recipe changes (feature_writeup/miners_delight.md). Not a nested member yet: recipe ids are
// creativedelight:<type>/<suffix> (global.CD.recipes).
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {
		var F = global.CD.food(e)

		//######################### Cooking trio #########################
		// Plain cooked meat is made in all three of smoker, furnace and campfire (food_families.md 3.1 `meat/cooked`).
		// The port ships the furnace only (0.25 xp); the family call (CD.FOOD values, 2026-11-01) replaces it with all three.
		F.meatCook(i.arthropod, i.cooked_arthropod)

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
		// Cave Hamburger (members #38): the removal above also took upstream's recipe. Same shape as upstream (bread,
		// crunchy bar or vegan patty, smoked bat wing, two of baked tentacles or brown mushroom); calamari replaces the
		// baked tentacles in both slots.
		var caveFilling = global.CD.on('rusticdelight') ? Ingredient.of([i.calamari, i.brown_mushroom]) : i.brown_mushroom
		R.shapeless(i.cave_hamburger, [i.t_bread, Ingredient.of([i.crunchy_bar, i.vegan_patty]), i.smoked_bat_wing,
			caveFilling, caveFilling], e, 'cave_hamburger')
	})
})
