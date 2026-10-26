// requires: farmersdelight
// Farmer's Delight 1.3.4 tag changes on the repacked 1.2.4 (feature_writeup/farmers_delight.md 3).

// Cabbage Rolls take any raw meat, raw fish, vegetable or mushroom (1.3.4 inlined these four in the recipe, no
// eggs). The pack's raw meat and fish food types stand in for #forge:raw_meat, which here holds only two Miner's
// Delight items. Entries other scripts and mods put in the tag stay.
ServerEvents.tags('item', e => {
	var i = global.item
	var t = global.tags
	t.add(e, i.cabbage_roll_veg, [i.t_food_raw_meat, i.t_food_raw_fish, i.vegetables, i.mushrooms])
	t.remove(e, i.cabbage_roll_veg, i.eggs)

	if (!global.CD) return
	// the rope fence set (1.3.4 forge tags; not minecraft:fences, so vanilla fences and leads treat it as its own)
	t.add(e, i.t_forge_fences, i.rope_fence)
	t.add(e, i.t_forge_fence_gates, i.rope_fence_gate)
})

ServerEvents.tags('block', e => {
	if (!global.CD) return
	var b = global.blocks
	var t = global.tags
	t.add(e, b.t_forge_fences, b.rope_fence)
	t.add(e, b.t_forge_fence_gates, b.rope_fence_gate)
	t.add(e, b.t_fd_mineable_knife, b.fd_pumpkin_pie)
})
