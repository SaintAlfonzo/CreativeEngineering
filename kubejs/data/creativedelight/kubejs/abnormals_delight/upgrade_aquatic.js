// requires: creativedelight
// requires: abnormals_delight
// requires: upgrade_aquatic
// Abnormals Delight x Upgrade Aquatic recipe changes. Non-member file: recipe ids are creativedelight:<type>/<suffix>
// (global.CD.recipes).
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {
		var F = global.CD.food(e)

		//######################### Perch and pike slices #########################
		// Abnormals Delight 4.1.2 smelts the slices for 1.0 xp, its smoking and campfire copies for 0.35, as do Farmer's
		// Delight's cod and salmon slices in every version (6.0.3 moved the smelting to 0.35, so 4.1.2's is the outlier).
		// The fish cooking trio (CD.FOOD: 0.35 xp, FD's times) replaces all three.
		F.fishCook(i.perch_slice, i.cooked_perch_slice)
		F.fishCook(i.pike_slice, i.cooked_pike_slice)

		//######################### Pickerelweed Juice, Pike with Beetroot #########################
		// 4.1.2 needs one blue and one purple pickerelweed; 6.0.3 takes two of its single pickerelweed. Here two of
		// either colour (upgrade_aquatic/tags.js). Results, 1.0 xp and 200 ticks as 4.1.2.
		R.remove({ id: 'abnormals_delight:pickerelweed_juice' }, e)
		R.remove({ id: 'abnormals_delight:pike_with_beetroot' }, e)
		// Pickerelweed Juice: pot and Kettle by the drink rules (creativeengineering/drinks.js, food/drinks #15)
		R.fdCooking([i.raw_pike, i.t_pickerelweed, i.t_pickerelweed, i.beetroot], i.pike_with_beetroot, e, 'pike_with_beetroot', 1.0, 200)
	})
})
