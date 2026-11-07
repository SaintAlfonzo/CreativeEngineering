// requires: creativedelight
// requires: immersiveengineering
// Immersive Engineering machine-only routes for the pack's food items (cornmeal in the crusher). Non-member file:
// recipe ids are creativedelight:<type>/<suffix> (global.CD.recipes). Moved from the pack's
// kubejs/server_scripts/cooking/cooking.js 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Cornmeal #########################
		// The pack's cornmeal (creativeengineering/recipes.js has the rule and the grinding).
		// IE crusher: corn 2 cornmeal + 0.5 kernels + 0.1 Corncob (replaces Corn Delight's corn -> kernels + straw; the cob is
		// corn_delight.md 2.1), kernels 0.5 cornmeal.
		// The crusher takes one item and can't roll its main output, so single kernels have no recipe; the bag does.
		if (Platform.isLoaded('corn_delight')) {
			R.remove({ id: 'corn_delight:integration/immersiveengineering/crusher/corn' }, e)
			R.ieCrushing(Item.of(i.cornmeal, 2), i.t_crops_corn, e, 'corn_cornmeal',
				[{ chance: 0.5, output: i.corn_seeds }, { chance: 0.1, output: i.corncob }], 3200)
			R.ieCrushing(Item.of(i.cornmeal, 18), i.corn_crate, e, 'corn_crate_cornmeal',
				[{ chance: 1, output: Item.of(i.corn_seeds, 4) }, { chance: 0.5, output: i.corn_seeds }], 3200)
			R.ieCrushing(Item.of(i.cornmeal, 4), i.corn_kernel_bag, e, 'corn_kernel_bag_cornmeal',
				[{ chance: 0.5, output: i.cornmeal }], 3200)
			// Corncob: 4 kernels + 0.5 cornmeal, as the Create machines (creativeengineering/recipes.js)
			R.ieCrushing(Item.of(i.corn_seeds, 4), i.corncob, e, 'corncob_kernels', [{ chance: 0.5, output: i.cornmeal }], 3200)
		}
	})
})
