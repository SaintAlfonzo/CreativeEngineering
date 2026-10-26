// requires: create_confectionery
// requires: create
// Ruby chocolate from dragon's breath fluid in a heated mixer. Moved from the pack's
// kubejs/server_scripts/cooking/chocolate.js 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	const R = global.NCH
	var f = global.fluid
	var i = global.item

	global.CD.recipes(() => {

		// ============================================================
		// Ruby
		// ============================================================

		//Dragons breath fluid in mixing
		R.remove({ id: 'create_confectionery:ruby_chocolate_recipe' }, e)
		R.createMixing(Fluid.of(f.ruby_chocolate, 250), [Fluid.of(f.milk, 250), Fluid.of(f.dragon_breath, 250), i.cocoa_beans, i.any_sugar], e, 'ruby_chocolate_mixing', 'heated')
	})
})
