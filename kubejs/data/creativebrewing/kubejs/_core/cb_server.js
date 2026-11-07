// requires: creativebrewing
// priority: 1000
// Creative Brewing server scripts (feature_writeup/creative_brewing.md 2.4). Authored here in the pack folder (loads on
// /reload); the CB build copies this tree into the jar at the same path (data/creativebrewing/kubejs/). Ships in the
// jar only. One folder per mod below this one (vinery/, brewinandchewin/, minecraft/ for vanilla potions, ...), files
// headed `// requires: <modid>`; pack-native systems (potion machines, effect text) in creativeengineering/.
//
// This file only checks that the startup half is there: global.CB comes from the CB jar's startup script.
ServerEvents.loaded(e => {
	if (!global.CB) {
		console.error('[creativebrewing] global.CB missing: the CB jar\'s startup scripts did not run ' +
			'(patcherMod without the startup loader, or no creativebrewing jar in mods/)')
		return
	}
	console.info('[creativebrewing] server scripts ready, members on: ' +
		global.CB.members().filter(m => global.CB.on(m)).join(', '))
})
