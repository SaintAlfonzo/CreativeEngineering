// Kitchen block tags: oven heat, bakeware hammering, dough storage blocks.
// Moved from the pack's server_scripts/tags/blocktags.js and storageblocktags.js 2026-11-01 (wave 4), rows
// unchanged. Only adds: running after the pack's tag scripts gives the same contents (nothing removes from these tags).
;(function () {
	// dough blocks: shovel, like Create Compressed's dough block
	var i = global.item
	var doughStorage = {
		gingerdough: [i.gingerdough_block],
		cookie_dough: [i.cookie_dough_block],
		sweet_potato_dough: [i.sweet_potato_dough_block],
		oat_dough: [i.oat_dough_block],
		cucumber_dough: [i.cucumber_dough_block],
		sourdough: [i.sourdough_block],
	}

	ServerEvents.tags('block', e => {
		if (!global.CD) return
		var t = global.tags
		var b = global.blocks

		// Oven (patcherMod oven/, feature_writeup/oven.md 2.4, 3.1). What heats it from below: FD's open-fire
		// sources (fire, campfires, lava; nested, so it stays live) + magma, lava cauldron, blaze burners and
		// the blaze lantern. Every stove and oven is left out on purpose ("non-stove" rule). OvenHeat gates
		// lit blocks by `lit` and idle blaze burners by heat level.
		t.add(e, b.t_oven_heat_sources, [
			b.t_fd_tray_heat_sources, b.magma_block, b.lava_cauldron, b.blaze_burner, b.lit_blaze_burner, b.blaze_lantern,
		])
		// Stove heat on top: FD's lit gate means a pot or skillet on the oven only heats while the oven is heated.
		t.add(e, b.t_fd_heat_sources, b.oven)
		t.mineable(e, 'pickaxe', null, b.oven)

		// Bakeware is hammered on the cutting board (NCH.hammering); without this the IE hammer rotates the board
		// instead of cutting (patcherMod IECompat.registerHammerNoRotate).
		t.add(e, b.t_hammer_no_rotate, b.cutting_board)

		// Cookware picked up by sneak right-click, handle toggled by sneak left-click (TODO cooking/kitchen #27, CD Java
		// cooking/CookwareHandling, QoL-7). Each mod's block only while it is loaded.
		t.add(e, b.t_cookware_pickup, [b.cooking_pot, b.skillet])
		if (Platform.isLoaded('miners_delight')) t.add(e, b.t_cookware_pickup, b.copper_pot)
		if (Platform.isLoaded('farmersrespite')) t.add(e, b.t_cookware_pickup, b.kettle)

		for (var mat in doughStorage) t.storageBlock(e, mat, doughStorage[mat], { tool: 'shovel' })
	})

	ServerEvents.tags('item', e => {
		if (!global.CD) return
		var t = global.tags
		for (var mat in doughStorage) t.storageBlock(e, mat, doughStorage[mat])
	})
})()
