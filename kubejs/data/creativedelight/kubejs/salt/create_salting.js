// requires: creativedelight
// requires: salt
// requires: create__the_salt
// Create salting for every saltable food (salt.md 6 S5, TODO food/seasoning #1, 2026-11-03).
// Create: The Salt ships one unheated mixing (food + salt -> the food {Salted:1b}) for 65 vanilla and Farmer's
// Delight foods (COVERED below, read from the 1.19.2-1.1.0 jar, pinned by sha1 in the member's gradle.properties).
// Here: every food in #salt:can_be_salted it does not cover gets the same mix, and its mixes for foods not in the tag
// (bone broth, cooked rice, tomato sauce, the raw crops) are removed, so Create salts exactly what the grid salts.
// The plain item input also matches already-salted food (the salt is wasted); NBT ingredients are avoided.
ServerEvents.recipes(e => {
	if (!global.CD) return

	const R = global.NCH
	var i = global.item

	global.CD.recipes(() => {
		var COVERED = {   // output -> Create: The Salt recipe id
			'farmersdelight:bacon_and_eggs': 'create__the_salt:baconandeggs',
			'farmersdelight:bacon_sandwich': 'create__the_salt:baconsandwich',
			'minecraft:baked_potato': 'create__the_salt:baked_potato',
			'farmersdelight:baked_cod_stew': 'create__the_salt:bakedcodstew',
			'farmersdelight:barbecue_stick': 'create__the_salt:barbecuestick',
			'farmersdelight:beef_patty': 'create__the_salt:beefpatty',
			'farmersdelight:beef_stew': 'create__the_salt:beefstew',
			'minecraft:beetroot_soup': 'create__the_salt:beetroot_soup',
			'farmersdelight:bone_broth': 'create__the_salt:bonebroth',
			'minecraft:bread': 'create__the_salt:bread',
			'farmersdelight:cabbage': 'create__the_salt:cabbage',
			'farmersdelight:cabbage_leaf': 'create__the_salt:cabbageleaf',
			'farmersdelight:cabbage_rolls': 'create__the_salt:cabbagerolls',
			'farmersdelight:chicken_sandwich': 'create__the_salt:chickensandwich',
			'farmersdelight:chicken_soup': 'create__the_salt:chickensoup',
			'farmersdelight:cod_roll': 'create__the_salt:codroll',
			'minecraft:cooked_beef': 'create__the_salt:cooked_beef_salted',
			'minecraft:cooked_chicken': 'create__the_salt:cooked_chiken_salted',
			'minecraft:cooked_cod': 'create__the_salt:cooked_cod',
			'minecraft:cooked_mutton': 'create__the_salt:cooked_mutton_salted',
			'minecraft:cooked_porkchop': 'create__the_salt:cooked_porkchop',
			'minecraft:cooked_rabbit': 'create__the_salt:cooked_rabit_salted',
			'minecraft:cooked_salmon': 'create__the_salt:cooked_salmon',
			'farmersdelight:cooked_salmon_slice': 'create__the_salt:cooked_salmon_slice',
			'farmersdelight:cooked_bacon': 'create__the_salt:cookedbacon',
			'farmersdelight:cooked_chicken_cuts': 'create__the_salt:cookedchikencuts',
			'farmersdelight:cooked_cod_slice': 'create__the_salt:cookedcodslice',
			'farmersdelight:cooked_mutton_chops': 'create__the_salt:cookedmuttonchops',
			'farmersdelight:cooked_rice': 'create__the_salt:cookedrice',
			'farmersdelight:dumplings': 'create__the_salt:dumplings',
			'farmersdelight:egg_sandwich': 'create__the_salt:egg_sandwich',
			'farmersdelight:fish_stew': 'create__the_salt:fishstew',
			'farmersdelight:fried_egg': 'create__the_salt:fried_egg_salted',
			'farmersdelight:fried_rice': 'create__the_salt:friedrice',
			'farmersdelight:grilled_salmon': 'create__the_salt:grilledsalmon',
			'farmersdelight:hamburger': 'create__the_salt:hamburger',
			'farmersdelight:honey_glazed_ham': 'create__the_salt:honeyglazedham',
			'farmersdelight:kelp_roll': 'create__the_salt:kelproll',
			'farmersdelight:kelp_roll_slice': 'create__the_salt:kelprollslice',
			'farmersdelight:mixed_salad': 'create__the_salt:mixed_salad',
			'minecraft:mushroom_stew': 'create__the_salt:mushroom_stew',
			'farmersdelight:mushroom_rice': 'create__the_salt:mushroomrice',
			'farmersdelight:mutton_wrap': 'create__the_salt:muttonwrap',
			'farmersdelight:nether_salad': 'create__the_salt:nethersalad',
			'farmersdelight:noodle_soup': 'create__the_salt:noodlesoup',
			'farmersdelight:onion': 'create__the_salt:onion',
			'farmersdelight:pasta_with_meatballs': 'create__the_salt:pastawithmeatballs',
			'farmersdelight:pasta_with_mutton_chop': 'create__the_salt:pastawithmuttonchop',
			'farmersdelight:pumpkin_soup': 'create__the_salt:pumpkinsoup',
			'minecraft:rabbit_stew': 'create__the_salt:rabbit_stew',
			'farmersdelight:ratatouille': 'create__the_salt:ratatouille',
			'farmersdelight:roast_chicken': 'create__the_salt:roastchicken',
			'farmersdelight:roasted_mutton_chops': 'create__the_salt:roastedmuttonchops',
			'minecraft:rotten_flesh': 'create__the_salt:rotten_flesh',
			'farmersdelight:salmon_roll': 'create__the_salt:salmonroll',
			'farmersdelight:shepherds_pie': 'create__the_salt:shepherdspie',
			'farmersdelight:smoked_ham': 'create__the_salt:smoked_ham',
			'farmersdelight:squid_ink_pasta': 'create__the_salt:squidinkpasta',
			'farmersdelight:steak_and_potatoes': 'create__the_salt:steakandpotatoes',
			'farmersdelight:stuffed_potato': 'create__the_salt:stuffedpotato',
			'farmersdelight:stuffed_pumpkin': 'create__the_salt:stuffedpumpkin',
			'farmersdelight:tomato': 'create__the_salt:tomato',
			'farmersdelight:tomato_sauce': 'create__the_salt:tomato_sauce',
			'farmersdelight:vegetable_noodles': 'create__the_salt:vegetablenoodles',
			'farmersdelight:vegetable_soup': 'create__the_salt:vegetablesoup',
		}

		var saltable = {}
		var n = 0
		Ingredient.of(i.t_can_be_salted).itemIds.forEach(id => { saltable[String(id)] = true; n++ })
		if (n === 0) {
			console.warn('[salt] #salt:can_be_salted is empty in the recipe event; Create salting skipped')
			return
		}

		var removed = 0
		Object.keys(COVERED).forEach(id => {
			if (!saltable[id]) { R.remove({ id: COVERED[id] }, e); removed++ }
		})

		var added = 0
		Object.keys(saltable).forEach(id => {
			if (COVERED[id]) return
			R.createMixing(Item.of(id, '{Salted:1b}'), [id, i.t_salts], e, 'salting/' + id.replace(':', '_'))
			added++
		})
		console.info('[salt] Create salting: ' + added + ' mixes added, ' + removed + ' Create: The Salt mixes removed')
	})
})
