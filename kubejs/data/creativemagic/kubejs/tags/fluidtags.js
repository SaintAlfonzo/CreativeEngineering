// Creative Magic; moved from kubejs/server_scripts/tags/fluidtags.js on 2026-11-04 (creative_split.md 7.6, wave 6), rows
// unchanged: the experience fluid tags and the XP fluid merge.
ServerEvents.tags('fluid', e => {
	var t = global.tags
	var f = global.fluid

	t.addFluid(e, 'forge:fluid/xp', f.experience)
	t.addFluid(e, 'forge:fluid/hyper_xp', f.hyper_experience)

	// ── XP fluid merge: Create's Liquid Experience is the ONLY #forge:experience ──
	t.add(e, 'forge:experience', [f.experience, f.flowing_experience])
	t.remove(e, 'forge:experience', [f.memory_essence, f.flowing_memory_essence, f.xp_still, f.flowing_xp_still])
})
