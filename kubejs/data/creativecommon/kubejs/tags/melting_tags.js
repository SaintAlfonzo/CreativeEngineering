// Creative Common melting tags (melting/ package, feature_writeup/melting.md, plan_bugfix_1114.md 3.8).
// Sources: salt melts adjacent targets, fire within 3, lava within 4 (ranges in [melting], creativecommon-common.toml).
// Targets: meltable (ice, snow) and meltable_slow (packed and blue ice, one tier down). Every mod's lines are guarded by
// global.has, so the tags hold only loaded blocks; the Java side works with any of them empty.
ServerEvents.tags('block', e => {
	var b = global.blocks
	var t = global.tags

	// ---------------- salt ----------------
	if (global.has('salt')) t.add(e, b.t_melt_salt, [b.salt_salt_block, b.raw_rock_salt_block])
	if (global.has('vintagedelight')) t.addMatching(e, b.t_melt_salt, [b.vintage_salt_blocks, b.vintage_evaporator])
	// Hearth and Harvest salt lick: every licked stage and the waxed ones (HHSaltRegistry)
	if (global.has('hearthandharvest')) t.addMatching(e, b.t_melt_salt, [b.hh_salt_lick_stages, b.hh_waxed_salt_block])

	// ---------------- fire ----------------
	// A block with a lit property counts only while lit (campfires, braziers, candelabras). Magma gives light 3, so it
	// melts from its own random tick instead of being found by the target's scan (Melting.java, source-driven).
	t.add(e, b.t_melt_fire, [b.t_fire, b.t_campfires, b.magma_block,
		b.torch, b.wall_torch, b.soul_torch, b.soul_wall_torch,
		b.lantern, b.soul_lantern, b.jack_o_lantern, b.glowstone])
	// Left out on purpose: redstone torches, sea lanterns, shroomlights, candles, jelly torches, electric lanterns.
	if (global.has('supplementaries')) t.addMatching(e, b.t_melt_fire, [b.melt_fire_supplementaries_sconces, b.melt_fire_supplementaries_lanterns])
	if (global.has('everythingcopper')) t.addMatching(e, b.t_melt_fire, b.melt_fire_everythingcopper_lanterns)
	if (global.has('redeco')) t.addMatching(e, b.t_melt_fire, b.melt_fire_redeco_lanterns)
	if (global.has('upgrade_aquatic')) t.add(e, b.t_melt_fire, b.melt_fire_ua_tooth_lantern)
	if (global.has('framedblocks')) t.add(e, b.t_melt_fire, [b.melt_fire_framed_torch, b.melt_fire_framed_wall_torch,
		b.melt_fire_framed_soul_torch, b.melt_fire_framed_soul_wall_torch])
	if (global.has('fairylights')) t.add(e, b.t_melt_fire, [b.melt_fire_fairylights_candle_lantern, b.melt_fire_fairylights_oil_lantern,
		b.melt_fire_fairylights_paper_lantern, b.melt_fire_fairylights_jack_o_lantern])
	if (global.has('architects_palette')) t.add(e, b.t_melt_fire, [b.melt_fire_ap_nether_brass_torch, b.melt_fire_ap_nether_brass_wall_torch,
		b.melt_fire_ap_nether_brass_lantern])
	// Glowstone cage lantern left out (SQ-MELT-10): light 3 and no random tick, so it could never melt anything.
	if (global.has('twigs')) t.addMatching(e, b.t_melt_fire, b.melt_fire_twigs_paper_lanterns)
	if (global.has('quark')) t.add(e, b.t_melt_fire, [b.blaze_lantern, b.melt_fire_quark_paper_lantern,
		b.melt_fire_quark_paper_lantern_sakura, b.melt_fire_quark_duskbound_lantern])
	if (global.has('dustrial_decor')) t.add(e, b.t_melt_fire, [b.melt_fire_dd_large_lantern, b.melt_fire_dd_large_soul_lantern])
	if (global.has('alexsabnormalities')) t.addMatching(e, b.t_melt_fire, b.melt_fire_aa_tooth_lanterns)
	if (global.has('securitycraft')) t.add(e, b.t_melt_fire, [b.melt_fire_sc_reinforced_lantern, b.melt_fire_sc_reinforced_soul_lantern])
	if (global.has('immersiveengineering')) t.add(e, b.t_melt_fire, b.melt_fire_ie_lantern)
	if (global.has('decorative_blocks')) t.add(e, b.t_melt_fire, [b.melt_fire_db_brazier, b.melt_fire_db_soul_brazier])
	if (global.has('blocky_siege')) t.add(e, b.t_melt_fire, b.melt_fire_bs_brazier_on)
	if (global.has('nethersdelight')) t.add(e, b.t_melt_fire, [b.melt_fire_nd_propelplant_torch, b.melt_fire_nd_propelplant_wall_torch])
	if (global.has('fantasyfurniture')) t.add(e, b.t_melt_fire, [b.melt_fire_ff_necrolord_candelabra, b.melt_fire_ff_royal_candelabra])
	if (global.has('domesticationinnovation')) t.add(e, b.t_melt_fire, b.melt_fire_di_wayward_lantern)

	// ---------------- lava ----------------
	// Any block holding lava fluid (source or flowing) counts without a tag; the cauldron holds no fluid state.
	t.add(e, b.t_melt_lava, b.lava_cauldron)

	// ---------------- targets ----------------
	t.add(e, b.t_meltable, [b.ice, b.frosted_ice, b.snow, b.snow_block, b.powder_snow])
	if (global.has(b.rare_ice)) t.add(e, b.t_meltable, b.rare_ice)
	t.add(e, b.t_meltable_slow, [b.packed_ice, b.blue_ice])
	t.add(e, b.t_melt_ultrawarm_instant, [b.snow, b.snow_block, b.powder_snow])
})
