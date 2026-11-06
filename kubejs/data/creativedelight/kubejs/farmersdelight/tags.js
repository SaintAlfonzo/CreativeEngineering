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

	// Moved from the pack's server_scripts/tags/cookingtags.js 2026-10-25, rows unchanged; shared tags are split by
	// the mod owning each entry (the other entries are in that mod's tags.js beside this folder).
	// flour / dough cleanup (the PneumaticCraft flour: pneumaticcraft/tags.js)
	t.clearAll(e, i.out_dough)

	// cooked meat
	t.add(e, 'forge:cooked_meat', [
		i.beef_patty,
		i.cooked_bacon,
		i.chicken_cuts_cooked,
		i.cooked_mutton_chops,
		i.cod_slice_cooked,
		i.salmon_slice_cooked,
	])
})

ServerEvents.tags('block', e => {
	if (!global.CD) return
	var b = global.blocks
	var t = global.tags
	t.add(e, b.t_forge_fences, b.rope_fence)
	t.add(e, b.t_forge_fence_gates, b.rope_fence_gate)
	t.add(e, b.t_fd_mineable_knife, b.fd_pumpkin_pie)
	t.add(e, 'minecraft:mineable/axe', b.hanging_canvas_sign)
	// Tomato vines pick with their own right-click; Quark's hoe area clicks each, up ropes and sticks (farming/crops #29)
	t.add(e, b.t_own_pick_crops, [b.tomatoes, b.stick_tomatoes, b.rope_tomatoes, b.treated_stick_tomatoes])
})
