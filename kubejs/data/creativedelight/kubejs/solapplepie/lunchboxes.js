// requires: creativedelight
// requires: solapplepie
// Spice of Life lunchboxes: Lunchbag (paper, 5 slots) -> Copper Lunchbox (9) -> Iron Lunchbox (14) -> Netherite
// Lunchbox (18, the pack's tier). Ids keep Spice of Life's names (lunchbox = copper, golden_lunchbox = iron).
// Every upgrade, skin, dye and soap recipe keeps the box's contents (patcherMod lunchbox/; Spice of Life keeps the
// food in a capability, which vanilla results drop). Non-member file: recipe ids are creativedelight:<type>/<suffix>
// (global.CD.recipes). Doc and tests: feature_writeup/lunchboxes.md.
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Tiers #########################
		// The lunchbag keeps Spice of Life's paper recipe. Copper starts from a bundle, not the lunchbag.
		R.remove({ id: 'solapplepie:lunchbox' }, e)
		R.remove({ id: 'solapplepie:golden_lunchbox' }, e)
		R.lunchboxShaped(i.copper_lunchbox, [
			' A ',
			'ABA',
			'AAA'
		], { A: i.ingot_copper, B: i.bundle }, e)
		R.lunchboxShaped(i.iron_lunchbox, [
			' A ',
			'ABA',
			'AAA'
		], { A: i.ingot_iron, B: i.copper_lunchbox }, e)
		// Smithing like the camera bags; Armor Trims asks for the Netherite Upgrade template
		R.lunchboxSmithing(i.netherite_lunchbox, i.iron_lunchbox, i.ingot_netherite, e)

		//######################### Looks #########################
		// Skin: smith an empty lower-tier box or a Fantasy Furniture berry basket onto a box (used up).
		// Dye: box + dyes in the grid, mixed like leather armour. Soap: dye off first, then the skin.
		R.lunchboxSkin(e)
		R.lunchboxDye(e)
		if (Platform.isLoaded('supplementaries')) R.lunchboxSoap(e)
	})
})
