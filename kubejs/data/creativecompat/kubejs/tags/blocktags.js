// requires: creativecompat
// Creative Compat: block tags for the mod tweaks.

ServerEvents.tags('block', e => {
	var t = global.tags
	var b = global.blocks
	var i = global.item

	// Weathered Cardboard Box (startup decor_blocks.js in this jar): an axe block
	t.mineable(e, 'axe', null, i.weathered_cardboard_box)

	// Monster box (mob_block_tweaks.md 2.9): mined like obsidian; a contraption must not carry the vault off.
	t.mineable(e, 'pickaxe', 'diamond', b.monster_box, { exclusive: true })
	t.add(e, b.t_create_non_movable, b.monster_box)

	// Rubidium blends the vanilla grass block's tint per vertex across neighbors (biome blend > 0) but colors
	// modded blocks flat per block, so Vinery's grass slab came out a shade off next to grass. This tag opts it in.
	t.add(e, b.t_blendable_blocks, i.grass_slab)

	// Dust particles (surface/DustParticles, plan_owner_notes_1119.md ruling 2): walked in or on, these puff dust in the
	// block's color like Supplementaries ash (ash itself has its own particles, so it is not listed). Dust layer piles,
	// full powder blocks and the concrete powders.
	t.addMatching(e, 'creativecompat:dust_particles', [
		'creativeengineering:*_dust_pile', i.immersiveengineering_sawdust, i.ash_layer_block, i.ash_block,
		i.powdered_obsidian_block, i.create_things_and_misc_powdered_obsidian_block, b.powdered_sugar_block,
		'mna:vinteum_dust_block', 'minecraft:*_concrete_powder'])

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

	// BlockRunner hooks: facing-aware sticky tier, camo-aware camouflage tier, visual sinking (surface/StickyFacing,
	// CamoSpeed, SinkingRender). The tiers are filled in the pack's server_scripts/tags/blocktags.js.
	var tier = name => '#blockrunner:surface/' + name
	t.add(e, 'creativecompat:blockrunner_face_up', tier('sticky'))
	t.add(e, 'creativecompat:camo_speed', tier('camouflage'))
	t.add(e, 'creativecompat:sinking', ['soft', 'dust', 'spongy', 'very_sticky'].map(tier))
	// Dropped items lie on top of sinking blocks at their real height; blocks listed here swallow them
	// (the item sinks like any other entity). Empty for now: every sinking block shows its items.
	t.add(e, 'creativecompat:sinking_hides_items', [])
	// Surface layers (surface/SurfaceLayers): layer blocks that walk at their own tier and sink
	// like a full block at every layer count, on their visible top. Leaf piles, bamboo leaves and snow are walk-through
	// (creativecompat:passthrough_layers) instead.
	t.add(e, 'creativecompat:surface_layers', [i.sand_layer_block, i.red_sand_layer_block, i.ash_layer_block, i.ash,
		i.vintagedelight_salt, i.immersiveengineering_sawdust, i.fodder])
	// Layers a placed block replaces like grass, up to [surface.passthrough.layers] replaceMaxLayers (surface/SurfaceLayers):
	// the walk-through layers (snow, leaf piles, bamboo leaves). Every other layer is never replaced.
	t.add(e, 'creativecompat:replaceable_layers', '#creativecompat:passthrough_layers')
	// Honey's wall slide on blocks that aren't honey (surface/WallSlide; honey and Fruits Delight jelly blocks already
	// slide, they extend HoneyBlock)
	t.add(e, 'creativecompat:wall_slide', [b.porous_flesh, i.banana_slug_slime_block])
})
