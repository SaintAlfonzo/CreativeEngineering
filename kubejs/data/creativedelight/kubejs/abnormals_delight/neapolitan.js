// requires: abnormals_delight
// requires: neapolitan
// Abnormals Delight x Neapolitan recipe changes (abnormals_delight.md 3.2, 3.4). Non-member file: recipe ids are
// creativedelight:<type>/<suffix> (global.CD.recipes).
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Adzuki Curry #########################
		// 4.1.2's grid craft took the raw beans and banana, like the Cooking Pot recipe. 6.0.3 takes the cooked ones,
		// as the pack's other heatless crafts of pot dishes do; the Cooking Pot recipe keeps the raw ones.
		R.remove({ id: 'abnormals_delight:neapolitan/adzuki_curry' }, e)
		R.shapeless(i.adzuki_curry, [i.roasted_adzuki_beans, i.dried_banana, i.carrot, i.pumpkin_slice, i.bowl], e, 'adzuki_curry_cooked_manual_only')

		//######################### Milk Bottles #########################
		// Abnormals Delight turns Farmer's Delight's bucket <-> bottle recipes off while Neapolitan is loaded, so
		// Neapolitan's 3 per bucket ran. Both FD versions and Create's spout and drain (250 mB) use 4, as 6.0.3 does.
		// The bottles are Farmer's Delight's (Neapolitan's merges into it, item_merges.md MG-06).
		R.remove({ id: 'neapolitan:milk/milk_bucket_from_bottles' }, e)
		R.remove({ id: 'neapolitan:milk/milk_bottles_from_bucket' }, e)
		R.shapeless(Item.of(i.farmersdelight_milk_bottle, 4), [i.t_buckets_milk, i.glass_bottle, i.glass_bottle, i.glass_bottle, i.glass_bottle], e, 'milk_bottles_from_bucket')
		R.shapeless(i.minecraft_milk_bucket, [i.bucket, i.t_bottles_milk, i.t_bottles_milk, i.t_bottles_milk, i.t_bottles_milk], e, 'milk_bucket_from_bottles')
	})
})
