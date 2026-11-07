// requires: creativeequipment
// priority: 1000
// Creative Equipment client scripts (feature_writeup/creative_equipment.md, creative_split.md 3.9, 7.6, wave 7). Authored
// here in the pack folder (reloads on F3+T); the Equipment build copies this tree into the jar at
// assets/creativeequipment/kubejs/. tooltips.js (stepstools, End gear, elytra, air brake) reads the pack's tooltip
// helpers through global.tooltips; names.js holds the Iron Shield name.
ClientEvents.loggedIn(e => {
	if (!global.CEQ) console.error('[creativeequipment] global.CEQ missing on the client: the Equipment jar\'s startup scripts did not run')
})
