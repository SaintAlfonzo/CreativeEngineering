// requires: creativeequipment
// Creative Equipment; moved from kubejs/server_scripts/equipment/camera_bag.js on 2026-11-04 (creative_split.md 7.6, wave 7), rows unchanged.
// Recipe ids are creativeequipment:<type>/<suffix> (global.CEQ.recipes).
// Camera bags (exposureBackport src/extras, mod exposureextras): leather -> iron -> netherite.
// The upgrades are smithing recipes on purpose: smithing copies the base item's NBT onto the
// result, and the bag keeps its contents there, so an upgrade keeps everything inside (and the
// custom name). A crafting-table upgrade would empty the bag.
// Slot counts per tier: config/exposureextras-common.toml [camera_bag]. Allowed items:
// #exposureextras:camera_equipment (tags/equipmenttags.js).

ServerEvents.recipes(e => {
global.CEQ.recipes(() => {
	const R = global.NCH
	var i = global.item

	R.shaped(i.leather_camera_bag, [
		' S ',
		'L L',
		'LLL'
	], {
		S: i.string,
		L: i.leather
	}, e)

	R.smithing(i.iron_camera_bag, i.leather_camera_bag, i.block_iron, e)
	R.smithing(i.netherite_camera_bag, i.iron_camera_bag, i.ingot_netherite, e)
})
})
