// requires: roll_delight
// Roll Delight 1.3.1 port recipes (feature_writeup/roll_delight.md 3.1, 3.2, 4 step 8). Creative Delight member script:
// recipe ids are creativedelight:<type>/<suffix> (global.CD.recipes). The member loads none of the donor's recipe
// files, so every Roll Delight recipe is here. Port changes against upstream: Ancient Roll takes Ancient Fruit and a
// Chorus Flower (B2), Golden Roll is gilded from the Rice Roll (2.3, upstream iron and gold recipe dropped), Nether
// Roll takes #roll_delight:nether_roll_meat (tags.js), Cooked Nether Wart is boiled with a bowl (as Cooked Rice), and
// every roll and both medleys get one sequenced assembly (recipes_cooking.md 2.3, the FD roll rule).
// Slice & Dice copies the Cooking Pot recipe into heated mixing after KubeJS runs (its usual mirror).
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('roll_delight')) return

	var i = global.item
	var R = global.NCH
	var F = global.CD.food(e)

	global.CD.recipes(() => {

		//######################### Rolls #########################
		// Shaped as upstream; the assembly starts on the first cooked rice and deploys the rest of the grid's inputs,
		// so both routes cost the same (roll values follow input hunger, 3.1.1).
		R.shaped(i.rice_roll, ['RRR', 'KKK'], { R: i.cooked_rice, K: i.dried_kelp }, e, 'rice_roll')
		F.assemble(i.cooked_rice, [i.cooked_rice, i.cooked_rice, i.dried_kelp, i.dried_kelp, i.dried_kelp], i.rice_roll)

		// Salmon Maki Roll (registry id amazing_salmon_roll, D4)
		R.shaped(i.amazing_salmon_roll, ['SSS', 'RSR', 'KKK'], { S: i.salmon_slice, R: i.cooked_rice, K: i.dried_kelp }, e,
			'amazing_salmon_roll')
		F.assemble(i.cooked_rice, [i.salmon_slice, i.salmon_slice, i.salmon_slice, i.salmon_slice, i.cooked_rice,
			i.dried_kelp, i.dried_kelp, i.dried_kelp], i.amazing_salmon_roll)

		R.shaped(i.mushroom_roll, [' C ', 'RMR', 'KKK'], { C: i.cabbage, R: i.cooked_rice, M: i.brown_mushroom_colony,
			K: i.dried_kelp }, e, 'mushroom_roll')
		F.assemble(i.cooked_rice, [i.cabbage, i.brown_mushroom_colony, i.cooked_rice, i.dried_kelp, i.dried_kelp,
			i.dried_kelp], i.mushroom_roll)

		// Ancient Roll: Ancient Fruit where the pitcher plant was, Chorus Flower where the torchflower was (D2)
		R.shaped(i.ancient_roll, [' A ', 'RCR', 'KKK'], { A: i.ancient_fruit, R: i.cooked_rice, C: i.chorus_flower,
			K: i.dried_kelp }, e, 'ancient_roll')
		F.assemble(i.cooked_rice, [i.ancient_fruit, i.chorus_flower, i.cooked_rice, i.dried_kelp, i.dried_kelp, // G5 (family call)
			i.dried_kelp], i.ancient_roll)

		// Golden Roll: the golden food rule on the Rice Roll (ring of 8 gold nuggets, heated mixer, Arc Furnace)
		F.goldenGild(i.rice_roll, i.golden_roll, i.any_nugget_gold)

		// Tobiko Roll: frogspawn (shears drop, 2.4) + 3 Rice Roll Slices -> 3; the assembly starts on a slice
		R.shapeless(Item.of(i.tobiko_roll, 3), [i.frogspawn, i.rice_roll_slice, i.rice_roll_slice, i.rice_roll_slice], e,
			'tobiko_roll')
		F.assemble(i.rice_roll_slice, [i.rice_roll_slice, i.rice_roll_slice, i.frogspawn], Item.of(i.tobiko_roll, 3))

		// Nether Roll: 2 raw pork or Prepared Flesh (mixed allowed) + Cooked Nether Wart -> 2; the grid hands the bowl back
		R.shapeless(Item.of(i.nether_roll, 2), [i.t_nether_roll_meat, i.t_nether_roll_meat, i.cooked_nether_wart], e,
			'nether_roll')
		F.assemble(i.cooked_nether_wart, [i.t_nether_roll_meat, i.t_nether_roll_meat], Item.of(i.nether_roll, 2))

		//######################### Slices #########################
		// Cutting board: each whole roll -> 3 slices (upstream counts)
		;[
			[i.rice_roll, i.rice_roll_slice],
			[i.amazing_salmon_roll, i.amazing_salmon_roll_slice],
			[i.mushroom_roll, i.mushroom_roll_slice],
			[i.ancient_roll, i.ancient_roll_slice],
			[i.golden_roll, i.golden_roll_slice],
		].forEach(c => R.fdCutting(c[0], i.any_knife, Item.of(c[1], 3), e, String(c[1]).split(':')[1]))

		//######################### Cooked Nether Wart #########################
		// Cooking Pot as upstream (100 ticks, 0.35 xp), served in a bowl as Cooked Rice is (D14)
		R.fdCooking([i.t_crops_nether_wart], i.cooked_nether_wart, e, 'cooked_nether_wart', 0.35, 100, i.bowl)

		//######################### Medleys #########################
		// Shaped as upstream (servings in the top rows, bowl in the middle of the bottom row) plus one assembly on the
		// bowl deploying the same 8 servings, as the FD Rice Roll Medley has.
		R.shaped(i.legendary_roll_medley, ['MMM', 'SSS', 'TBT'], { M: i.mushroom_roll_slice, S: i.amazing_salmon_roll_slice,
			T: i.tobiko_roll, B: i.bowl }, e, 'legendary_roll_medley')
		F.assemble(i.bowl, [i.mushroom_roll_slice, i.mushroom_roll_slice, i.mushroom_roll_slice, i.amazing_salmon_roll_slice,
			i.amazing_salmon_roll_slice, i.amazing_salmon_roll_slice, i.tobiko_roll, i.tobiko_roll], i.legendary_roll_medley)

		R.shaped(i.mythical_roll_medley, ['AAA', 'GGG', 'NBN'], { A: i.ancient_roll_slice, G: i.golden_roll_slice,
			N: i.nether_roll, B: i.bowl }, e, 'mythical_roll_medley')
		F.assemble(i.bowl, [i.ancient_roll_slice, i.ancient_roll_slice, i.ancient_roll_slice, i.golden_roll_slice,
			i.golden_roll_slice, i.golden_roll_slice, i.nether_roll, i.nether_roll], i.mythical_roll_medley)

		//######################### Chopsticks #########################
		R.shapeless(i.chopsticks, [i.stick, i.stick], e, 'chopsticks')
		R.shapeless(i.wrapped_chopsticks, [i.chopsticks, i.paper, i.any_dye_red, i.any_dye_yellow], e, 'wrapped_chopsticks')
	})
})
