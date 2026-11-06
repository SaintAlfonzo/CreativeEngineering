// priority: 1000
// requires: creativeequipment
// Creative Equipment server scripts (feature_writeup/creative_equipment.md, creative_split.md 3.9, 7.6, wave 7). Authored
// here in the pack folder (loads on /reload); the Equipment build copies this tree into the jar at the same path
// (data/creativeequipment/kubejs/). Folders keep the pack's layout: equipment/ (archery, bullets, camera bags, End gear,
// shields, stepstools, dragon autorepair), other/ (backpack skins, harvest tiers), tags/ (gear tags, the stepstool
// Curios slot). Recipes are written inside global.CEQ.recipes, so their ids are creativeequipment:<type>/<suffix>.
// Each script pack has its own scope: these scripts read only global.* (NCH, tags, item, fluid) at event time.
ServerEvents.loaded(e => {
	if (!global.CEQ) console.error('[creativeequipment] global.CEQ missing: the Equipment jar\'s startup scripts did not run ' +
		'(patcherMod without the startup loader, or no creativeequipment jar in mods/)')
})
