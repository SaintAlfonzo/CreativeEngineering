// requires: buzzier_bees
// Buzzier Bees food recipe removals. Non-member file: recipe ids are creative_delight:<type>/<suffix>
// (global.CD.recipes). Moved from the pack's kubejs/server_scripts/cooking/cooking.js 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	var R = global.NCH

	global.CD.recipes(() => {

		//Honey
		//Honey item removals
		R.remove({ id: 'buzzier_bees:honey/honey_apple' }, e)
	})
})
