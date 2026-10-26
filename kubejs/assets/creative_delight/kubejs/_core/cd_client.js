// priority: 1000
// Creative Delight client scripts (creative_delight.md 10.3, 10.4): tooltips, JEI info pages, names for members.
// Authored here in the pack folder (loads on F3+T); the CD build copies this tree into the jar at the same path
// (assets/creative_delight/kubejs/). Ships in the jar only (Q25).
//
// This file only checks that the startup half is there: global.CD comes from the CD jar's startup script.
ClientEvents.loggedIn(e => {
	if (!global.CD) console.error('[creative_delight] global.CD missing on the client: the CD jar\'s startup scripts did not run')
})
