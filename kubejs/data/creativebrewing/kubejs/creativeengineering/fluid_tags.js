// Potion and dragon's breath fluid tags. Moved from the pack's server_scripts/tags/fluidtags.js 2026-11-01
// (creative_brewing.md 4.5), rows unchanged; ink and XP stay there. Nothing in the pack or CD adds IE's potion fluid to
// #forge:fluid/potion, so running after the pack's tag scripts gives the same contents.
ServerEvents.tags('fluid', e => {
	if (!global.CB) return
	var t = global.tags
	var f = global.fluid

	;[
		['forge:fluid/dragon_breath', f.dragon_breath], // G5
		['forge:fluid/potion', f.create_potion], // G5
	].forEach(([tag, fluid]) => t.addFluid(e, tag, fluid))

	// Remove IE potion tag
	t.remove(e, 'forge:fluid/potion', f.ie_potion)
})
