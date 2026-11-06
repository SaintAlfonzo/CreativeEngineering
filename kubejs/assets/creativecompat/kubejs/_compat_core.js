// priority: 1000
// Creative Compat client scripts (feature_writeup/creative_split.md 3.9, 7.6). Authored here in the pack folder
// (reloads on F3+T); the Compat build copies this tree into the jar at assets/creativecompat/kubejs/. tooltips.js
// (misc mods, Quark, Supplementaries, Framed, flower pots) reads the pack's tooltip helpers through global.tooltips;
// names.js holds the flower pot names.
ClientEvents.loggedIn(e => {
	if (!global.itemPhysics) console.error('[creativecompat] global.itemPhysics missing on the client: the Compat jar\'s startup scripts did not run')
})
