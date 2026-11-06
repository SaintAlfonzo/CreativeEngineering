// priority: 1000
// Creative Magic client scripts (feature_writeup/creative_magic.md, creative_split.md 3.9, 7.6, wave 6). Authored here
// in the pack folder (reloads on F3+T); the Magic build copies this tree into the jar at assets/creativemagic/kubejs/.
// names.js: Flint and Terrasteel and Dark Fire; tooltips.js: the Botania lines, through the pack's tooltip helpers
// (global.tooltips); jei.js: the red line on enchanted books holding the three Nock Enough Arrows enchantments.
ClientEvents.loggedIn(e => {
	if (!global.CMG) console.error('[creativemagic] global.CMG missing on the client: the Magic jar\'s startup script did not run')
})
