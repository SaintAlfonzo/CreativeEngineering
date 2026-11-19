// requires: creativedelight
// requires: farmersdelight
// Fruit slice yields and reassembly (TODO fruit #3, plan_food_batch_1119.md A6): one table for the big fruits that cut
// into slices. Cut (knife, and the Create saw where the fruit had one) and whole-from-slices (shapeless; the shaped 3x3
// and 2x2 versions go). The extra slice on the way back is the loss of cutting. Slice & Dice copies the knife cuts.
// Loot: the pumpkin's knife break (farmersdelight/loot.js) and the mini cantaloupe's (delightful/loot.js) match the cut.
ServerEvents.recipes(e => {
	if (!global.CD) return
	var i = global.item
	var R = global.NCH.forPack('creativedelight')

	global.CD.recipes(() => {
		var saw = Platform.isLoaded('create')
		var ring = (slice, count) => { var a = []; for (var k = 0; k < count; k++) a.push(slice); return a }
		// [whole (ingredient), whole (item), slice, cut count, extra cut outputs, from-slices count, saw?, ids to remove]
		var FRUITS = [ // G5 begin (the rows only reach NCH calls, which skip absent mods' ids)
			[i.melon_block, i.melon_block, i.melon, 8, [], 9, false, ['farmersdelight:cutting/melon', 'minecraft:melon']],
			[i.pumpkin, i.pumpkin, i.pumpkin_slice, 8, [], 9, false, ['farmersdelight:cutting/pumpkin', 'farmersdelight:pumpkin_from_slices']],
		]
		if (global.has('fruitsdelight')) FRUITS.push(
			// the large cantaloupe (Fruits Delight's hamimelon)
			[i.hamimelon, i.hamimelon, i.hamimelon_slice, 8, [], 9, true, ['fruitsdelight:hamimelon_cutting', 'fruitsdelight:cutting/hamimelon_slice']],
			// lemon: was 8 + seeds (farming/trees #3); the tree's drops are unchanged
			[i.t_fruits_lemon, i.lemon, i.lemon_slice, 5, [i.lemon_seeds], 6, true, ['fruitsdelight:lemon_cutting', 'fruitsdelight:cutting/lemon_slice']],
			[i.t_fruits_orange, i.orange, i.orange_slice, 5, [], 6, true, ['fruitsdelight:orange_cutting', 'fruitsdelight:cutting/orange_slice']],
			// pineapple: 6 and its saplings by knife and saw alike (the saw's 8 without saplings went)
			[i.pineapple, i.pineapple, i.pineapple_slice, 6, [i.pineapple_sapling, Item.of(i.pineapple_sapling).withChance(0.5)], 9, true,
				['fruitsdelight:pineapple_cutting', 'fruitsdelight:cutting/pineapple_slice']])
		// G5 end
		if (global.CD.on('delightful')) FRUITS.push(
			// the mini cantaloupe (Delightful's cantaloupe); its 1 -> 3 hand split stays
			[i.cantaloupe, i.cantaloupe, i.cantaloupe_slice, 4, [], 4, false, ['delightful:cutting/cantaloupe']])

		FRUITS.forEach(r => {
			var whole = r[0], wholeItem = r[1], slice = r[2], cut = r[3], extra = r[4], back = r[5], name = slice.split(':')[1]
			r[7].forEach(id => R.remove({ id: id }, e))
			R.fdCutting(whole, i.any_knife, [Item.of(slice, cut)].concat(extra), e, name + '_from_whole')
			if (r[6] && saw) R.createSaw([Item.of(slice, cut)].concat(extra), whole, e, name + '_from_whole')
			R.shapeless(wholeItem, ring(slice, back), e, name.replace(/_slice$/, '') + '_from_slices')
		})
	})
})
