// requires: creativecompat
// Creative Compat: block tags for the mod tweaks; moved from kubejs/server_scripts/tags/blocktags.js on 2026-11-01.

ServerEvents.tags('block', e => {
	var t = global.tags
	var b = global.blocks
	var i = global.item

	// Monster box (mob_block_tweaks.md 2.9): mined like obsidian; a contraption must not carry the vault off.
	t.mineable(e, 'pickaxe', 'diamond', b.monster_box, { exclusive: true })
	t.add(e, b.t_create_non_movable, b.monster_box)

	// Rubidium blends the vanilla grass block's tint per vertex across neighbours (biome blend > 0) but colours
	// modded blocks flat per block, so Vinery's grass slab came out a shade off next to grass. This tag opts it in.
	t.add(e, b.t_blendable_blocks, i.grass_slab)

	// Campfire rain (campfire/CampfireRain.java, config [campfire]). Unlit Campfire burns campfires forever; rain puts
	// out the ones in rain_douses unless the block below is in rain_proof_fuel.
	// Soul campfires stay out of rain_douses; Tinted Campfires aren't CampfireBlocks, so never ticked.
	t.add(e, b.t_campfire_rain_douses, b.campfire)
	t.add(e, b.t_campfire_rain_proof_fuel, [b.coal_block, b.quark_charcoal_block, b.architects_palette_charcoal_block])

	// In-world dyeing (Supplementaries [tweaks.dye_blocks], SupplementariesDyeBlocksMixin). Blocks with a block entity
	// never dye in world (sacks lost their contents, canvas signs their text); no_world_dye is for plain blocks the
	// dyeing audit takes out too (TODO blocks/dyeing #1). Empty until then.
	t.add(e, b.t_no_world_dye, [])

	// Immersive Portals mirrors (plan_portal_changes.md 4, IPMirrorGlassMixin): IP takes only GlassBlock, stained glass
	// and IronBarsBlock panes, so these glass blocks and panes are added. Frosty glass: sneak + flint and steel (plain use
	// melts the frost).
	var colours = ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray', 'light_gray', 'cyan',
		'purple', 'blue', 'brown', 'green', 'red', 'black']
	t.add(e, b.t_mirror_glass, [i.tinted_glass, i.reinforced_tinted_glass,
		i.tinted_framed_glass, i.horizontal_tinted_framed_glass, i.vertical_tinted_framed_glass,
		i.createframed_tinted_framed_glass, i.createframed_horizontal_tinted_framed_glass,
		i.createframed_vertical_tinted_framed_glass, i.createframed_tinted_tiled_glass,
		i.quark_framed_glass, i.dirty_glass, i.frosty_glass, i.rainbow_glass]
		.concat(colours.map(c => i[c + '_framed_glass'])))
	// Framed Blocks' panes and bars extend IronBarsBlock, which Immersive Portals accepts; camouflage blocks are never mirrors
	t.add(e, b.t_not_mirror_glass, [i.framed_pane, i.framed_bars])
	t.add(e, b.t_mirror_glass_panes, [i.createframed_tinted_framed_glass_pane,
		i.createframed_horizontal_tinted_framed_glass_pane, i.createframed_vertical_tinted_framed_glass_pane,
		i.createframed_tinted_tiled_glass_pane, i.framed_glass_pane, i.dirty_glass_pane, i.frosty_glass_pane]
		.concat(colours.map(c => i[c + '_framed_glass_pane'])))
})
