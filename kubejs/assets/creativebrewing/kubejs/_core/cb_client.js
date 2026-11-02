// priority: 1000
// Creative Brewing client scripts (feature_writeup/creative_brewing.md 2.4). Authored here in the pack folder (reloads
// on F3+T); the CB build copies this tree into the jar at assets/creativebrewing/kubejs/. Ships in the jar only. One
// folder per mod below this one, files headed `// requires: <modid>`: effect tooltips, drink lines, JEED text.
ClientEvents.loggedIn(e => {
	if (!global.CB) console.error('[creativebrewing] global.CB missing on the client: the CB jar\'s startup scripts did not run')
})
