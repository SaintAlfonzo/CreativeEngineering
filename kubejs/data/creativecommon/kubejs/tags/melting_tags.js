// Creative Common melting tags (melting/ package, feature_writeup/melting.md, plan_bugfix_1114.md 3.8,
// plan_post_scratch_1115 E1, G11). Sources: salt melts adjacent targets, fire within 3, lava within 4 (ranges in
// [melting], creativecommon-common.toml). Targets: meltable (ice, snow, frozen decor) and meltable_slow (packed and
// blue ice, IW cryosol and permafrost, AP packed ice; one tier down, only lava and magma heat them). Every mod's lines
// are guarded by global.has, so the tags hold only loaded blocks; the Java side works with any of them empty.
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
	// IW soils mirror the ice tiers (plan_post_scratch_1115 G6-5): permafrost like blue ice, cryosol like packed ice.
	// G11: packed ice and cryosol melt from magma and lava within [melting] slowMagmaRange / slowLavaRange and from
	// salt; blue ice and permafrost (lava_only) only touching lava, never from salt.
	if (global.has('immersive_weathering')) t.add(e, b.t_meltable_slow, [b.iw_cryosol, b.iw_permafrost])
	t.add(e, b.t_melt_lava_only, b.blue_ice)
	if (global.has('immersive_weathering')) t.add(e, b.t_melt_lava_only, b.iw_permafrost)
	t.add(e, b.t_melt_ultrawarm_instant, [b.snow, b.snow_block, b.powder_snow])

	// Frozen decor (B8, E1). Products are [melting] meltProducts rows (snowy -> plain stone, same shape and facing;
	// frosty -> glass, pane, grass, fern; thin ice -> water; coarse snow -> gravel); no row -> air (frost, icicle,
	// snow bricks). Snow bricks melt at sluggishMeltFactor (0.25x) and vanish in the Nether like snow. Philips Ruins
	// frozen prismarine never melts (left out).
	if (global.has('immersive_weathering')) {
		t.addMatching(e, [b.t_meltable, b.t_melt_saltable], [b.melt_iw_snowy, b.melt_iw_frosty, b.melt_iw_snow_bricks])
		t.add(e, [b.t_meltable, b.t_melt_saltable], [b.iw_frost, b.thin_ice, b.iw_icicle])
		t.addMatching(e, [b.t_melt_sluggish, b.t_melt_ultrawarm_instant], b.melt_iw_snow_bricks)
	}
	if (global.has('architects_palette')) {
		t.add(e, [b.t_meltable, b.t_melt_saltable], b.ap_coarse_snow)
		// AP packed ice is packed ice: slow tier, saltable; full blocks step to ice (slowSteps), shaped ones to air
		t.addMatching(e, [b.t_meltable_slow, b.t_melt_saltable], b.melt_ap_packed_ice)
	}

	// Salt melts only these (G7.11; the "Saltable" tooltip reads it). Not saltable: blue ice, IW permafrost.
	t.add(e, b.t_melt_saltable, [b.ice, b.frosted_ice, b.snow, b.snow_block, b.powder_snow, b.packed_ice])
	if (global.has(b.rare_ice)) t.add(e, b.t_melt_saltable, b.rare_ice)
	if (global.has('immersive_weathering')) t.add(e, b.t_melt_saltable, b.iw_cryosol)
	// Snow on these resists heat and light, not salt (G6 4b)
	t.add(e, b.t_melt_keeps_snow, b.blue_ice)
	if (global.has('immersive_weathering')) t.add(e, b.t_melt_keeps_snow, b.iw_permafrost)
	// Magma: snow within 2 melts at once; no snowfall or ice forms within [melting] fireRange (G7.2, G7.13)
	t.add(e, b.t_melt_magma, b.magma_block)
	// Modded magma-like blocks (G11; in the block dump the only one is AP Molten Nether Bricks, light 3; it random
	// ticks through [melting] extraRandomTickBlocks, so it scans like magma). Melting treats the magma tag as fire too.
	if (global.has('architects_palette')) t.add(e, [b.t_melt_fire, b.t_melt_magma], b.ap_molten_nether_bricks)
})

// Hot biomes (G11): ice and snow there melt on their own melt ticks with no source ([melting] hotMeltTickInterval).
// The arid and tropical season types of Creative Calendar (kubejs/generation_scripts/worldgen/season_types.json).
ServerEvents.tags('worldgen/biome', e => {
	if (!global.has('creativecalendar')) return
	global.tags.add(e, global.blocks.t_melt_hot_biomes, [global.blocks.t_biome_seasons_arid, global.blocks.t_biome_seasons_tropical])
})
