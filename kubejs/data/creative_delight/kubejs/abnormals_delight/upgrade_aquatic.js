// requires: abnormals_delight
// requires: upgrade_aquatic
// Abnormals Delight x Upgrade Aquatic recipe changes. Non-member file: recipe ids are creative_delight:<type>/<suffix>
// (global.CD.recipes).
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Perch and pike slice XP #########################
		// Abnormals Delight 4.1.2 smelts the slices for 1.0 xp, its smoking and campfire copies for 0.35, as do Farmer's
		// Delight's cod and salmon slices in every version (6.0.3 moved the smelting to 0.35, so 4.1.2's is the outlier).
		// Same input, output and 200 tick time; only the xp drops.
		R.remove({ id: 'abnormals_delight:upgrade_aquatic/cooked_perch_slice' }, e)
		R.remove({ id: 'abnormals_delight:upgrade_aquatic/cooked_pike_slice' }, e)
		R.smelting(i.cooked_perch_slice, i.perch_slice, e, 'cooked_perch_slice', 0.35, 200)
		R.smelting(i.cooked_pike_slice, i.pike_slice, e, 'cooked_pike_slice', 0.35, 200)

		//######################### Pickerelweed Juice, Pike with Beetroot #########################
		// 4.1.2 needs one blue and one purple pickerelweed; 6.0.3 takes two of its single pickerelweed. Here two of
		// either colour (upgrade_aquatic/tags.js). Results, 1.0 xp and 200 ticks as 4.1.2.
		R.remove({ id: 'abnormals_delight:pickerelweed_juice' }, e)
		R.remove({ id: 'abnormals_delight:pike_with_beetroot' }, e)
		R.fdCooking([i.t_pickerelweed, i.t_pickerelweed, i.any_sweetener_drink], i.pickerelweed_juice, e, 'pickerelweed_juice', 1.0, 200)
		R.fdCooking([i.raw_pike, i.t_pickerelweed, i.t_pickerelweed, i.beetroot], i.pike_with_beetroot, e, 'pike_with_beetroot', 1.0, 200)
	})
})
