// priority: 1000
// Creative Compat server scripts (feature_writeup/creative_split.md 3.9, 7.6). Authored here in the pack folder (loads
// on /reload); the Compat build copies this tree into the jar at the same path (data/creativecompat/kubejs/). Folders
// keep the pack's layout: economy/ (Lightman's Currency), other/ (recipe tweaks to third-party mods, feature ports,
// Starter Kit), tags/ (item physics, curios, flower box, campfire rain and other mod tags).
// The startup half (item_physics.js, decor_blocks.js) ships in the jar's kubejs_startup/creativecompat/.
// Each script pack has its own scope: these scripts read only global.* (NCH, tags, item, blocks, fluid) at event time.
ServerEvents.loaded(e => {
	if (!global.itemPhysics) console.error('[creativecompat] global.itemPhysics missing: the Compat jar\'s startup ' +
		'scripts did not run (patcherMod without the startup loader, or no creativecompat jar in mods/)')
})
