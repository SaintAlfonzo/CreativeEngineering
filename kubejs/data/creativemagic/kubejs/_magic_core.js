// priority: 1000
// requires: creativemagic
// Creative Magic server scripts (feature_writeup/creative_magic.md, creative_split.md 3.9, 7.6, wave 6). Authored here
// in the pack folder (loads on /reload); the Magic build copies this tree into the jar at the same path
// (data/creativemagic/kubejs/). Folders keep the pack's layout: magic/ (enchantment filters, Flint and Terrasteel and
// the compass gate, sculk, Ender and Cloud Storage, Create Enchantment Industry machines), tags/ (the XP fluid merge).
// Recipes are written inside global.CMG.recipes, so their ids are creativemagic:<type>/<suffix>.
// Each script pack has its own scope: these scripts read only global.* (NCH, tags, item, fluid) at event time.
ServerEvents.loaded(e => {
	if (!global.CMG) console.error('[creativemagic] global.CMG missing: the Magic jar\'s startup script did not run ' +
		'(patcherMod without the startup loader, or no creativemagic jar in mods/)')
})
