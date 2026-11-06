// requires: creativedevtools
// priority: 1000
// Creative Devtools server scripts (feature_writeup/creative_devtools.md 1). Authored here in the pack folder (loads on
// /reload when the Devtools jar is installed); the Devtools build copies this tree into the jar at the same path
// (data/creativedevtools/kubejs/). Never shipped to players. Folders: dump/ (/dumpall, the /dump* commands,
// /recipescan), create/ (test recipes), flowerworld/ (the Flower Test world).
//
// The pack's two dump helpers stay in its own script pack (server_scripts/_lib/java_guard.js, export_io.js: the loot
// scripts use them at runtime). A script pack only sees another pack's globals, so these wrappers give the Devtools
// scripts the same two names, resolved when called (after every pack has loaded).
function guardThrowable(fn) {
	return global.guardThrowable(fn)
}

function writeExportJson(path, data) {
	return global.writeExportJson(path, data)
}

ServerEvents.loaded(e => {
	if (!global.CDV) {
		console.error('[creativedevtools] global.CDV missing: the Devtools jar\'s startup script did not run ' +
			'(patcherMod without the startup loader, or no creativedevtools jar in mods/)')
		return
	}
	if (!global.guardThrowable || !global.writeExportJson) {
		console.error('[creativedevtools] global.guardThrowable / global.writeExportJson missing: the pack\'s ' +
			'server_scripts/_lib/java_guard.js or export_io.js did not run; the dumps will fail')
	}
	console.info('[creativedevtools] server scripts ready')
})
