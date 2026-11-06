// requires: rusticdelight
// Glowing calamari line (TODO dishes/ocean #3): a glow squid cuts to glowing calamari, which slices and rolls like Rustic
// Delight's calamari and cooks into its Cooked Calamari. Creative Delight startup script, loaded from the jar after the
// pack's startup_scripts, so global.item exists here. Restart to apply. Recipes:
// data/creativedelight/kubejs/rusticdelight/recipes.js.
//
// Art: kubejs/assets/creativeengineering/textures/item/glowing_calamari*.png, Rustic's calamari sprites palette-swapped to
// the glow ink sac (notice beside them; Rustic's licence still to confirm).
// Values: the plain calamari line's (hunger, saturation modifier), plus Glowing for 30 s as Miner's Delight's glow squid gives.
StartupEvents.registry('item', e => {
	var i = global.item
	var GLOW = 600   // ticks of Glowing

	;[
		[i.glowing_calamari, 'Glowing Calamari', 2, 0.2],
		[i.glowing_calamari_slice, 'Glowing Calamari Slice', 1, 0.1],
		[i.glowing_calamari_roll, 'Glowing Calamari Roll', 7, 0.6],
	].forEach(c => {
		e.create(c[0]).displayName(c[1]).food(f => {
			f.hunger(c[2]).saturation(c[3]).effect('minecraft:glowing', GLOW, 0, 1.0)
		})
	})
})
