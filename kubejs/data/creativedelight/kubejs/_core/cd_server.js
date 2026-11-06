// priority: 1000
// Creative Delight server scripts (creative_delight.md 10.3, 10.4). Authored here in the pack folder (loads on
// /reload); the CD build copies this tree into the jar at the same path (data/creativedelight/kubejs/). Ships
// in the jar only (Q25). One folder per member below this one, files headed `// requires: <modid>`; rows for a
// non-member mod in their own file with that mod's `// requires:` too (section 12).
//
// This file only checks that the startup half is there: global.CD comes from the CD jar's startup script.
ServerEvents.loaded(e => {
	if (!global.CD) {
		console.error('[creativedelight] global.CD missing: the CD jar\'s startup scripts did not run ' +
			'(patcherMod without the startup loader, or no creativedelight jar in mods/)')
		return
	}
	console.info('[creativedelight] server scripts ready, members on: ' +
		global.CD.members().filter(m => global.CD.on(m)).join(', '))
})
