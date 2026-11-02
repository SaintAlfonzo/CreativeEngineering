// requires: quark
// Quark food storage block removals (chorus fruit block, crop crates). Non-member file: recipe ids are
// creativedelight:<type>/<suffix> (global.CD.recipes). Moved from the pack's kubejs/server_scripts/cooking/cooking.js
// 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Ender #########################
		R.remove({ id: 'quark:building/crafting/compressed/chorus_fruit_block' }, e)
		R.remove({ id: 'quark:building/crafting/compressed/chorus_fruit_block_uncompress' }, e)

		//######################### Misc #########################
		//Crates
		//Remove redundant crates
		R.remove({ id: 'quark:building/crafting/compressed/carrot_crate' }, e)
		R.remove({ id: 'quark:building/crafting/compressed/carrot_crate_uncompress' }, e)
		R.remove({ id: 'quark:building/crafting/compressed/beetroot_crate' }, e)
		R.remove({ id: 'quark:building/crafting/compressed/beetroot_crate_uncompress' }, e)
		R.remove({ id: 'quark:building/crafting/compressed/potato_crate' }, e)
		R.remove({ id: 'quark:building/crafting/compressed/potato_crate_uncompress' }, e)
		R.remove({ id: 'quark:building/crafting/compressed/apple_crate' }, e)
		R.remove({ id: 'quark:building/crafting/compressed/apple_crate_uncompress' }, e)
	})
})
