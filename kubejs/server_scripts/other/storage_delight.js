// requires: storagedelight
// Storage Delight half cabinets (repackedStorage, storage_delight.md section 6; TODO kitchen #41): every cabinet splits
// into two half cabinets (shapeless) and two half cabinets stacked in a column make the cabinet again.
ServerEvents.recipes(e => {
	var i = global.item
	// recipe ids under the owning repack's namespace (ids follow the owning mod)
	var R = global.NCH.forPack('storagedelight')

	var woods = ['oak', 'spruce', 'birch', 'jungle', 'acacia', 'dark_oak', 'mangrove', 'crimson', 'warped', 'bamboo']
	var stones = ['polished_deepslate', 'polished_andesite', 'polished_blackstone', 'polished_diorite', 'stone_bricks', 'polished_tuff']
	woods.forEach(w => {
		var cabinets = ['glass_' + w + '_cabinet', w + '_cabinet_with_glass_doors', w + '_single_door_cabinet']
			.concat(stones.map(s => w + '_cabinet_with_' + s + '_countertop'))
		cabinets.forEach(key => {
			// the half's dict key: the first "cabinet" in the id becomes "half_cabinet" (BlockRegistry.halfName)
			var halfKey = key.replace('cabinet', 'half_cabinet')
			var whole = i[key]
			var half = i[halfKey]
			R.shapeless(Item.of(half, 2), [whole], e, halfKey + '_from_cabinet')
			R.shaped(whole, ['H', 'H'], { H: half }, e, key + '_from_half_cabinets')
		})
	})
})
