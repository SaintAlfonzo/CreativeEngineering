ServerEvents.recipes(e => {
	console.log('Smoked wood starting')

	const R = global.NCH
	var i = global.item
	var f = global.fluid

	// ── Smoking: every Dreams & Desires smoked block from any wood of the same shape ──
	// (smoker, campfire and the Create encased fan with a Smoking Sail all read smoking recipes;
	// the wood tags are built in tags/wood_tags.js)
	var smokedFrom = [
		[i.smoked_log, i.t_smokable_log],
		[i.smoked_wood, i.t_smokable_wood],
		[i.smoked_log_stripped, i.t_smokable_log_stripped],
		[i.smoked_wood_stripped, i.t_smokable_wood_stripped],
		[i.smoked_planks, i.t_smokable_planks],
		[i.smoked_slab, i.t_smokable_slab],
		[i.smoked_stairs, i.t_smokable_stairs],
		[i.smoked_fence, i.t_smokable_fence],
		[i.smoked_fencegate, i.t_smokable_fencegate],
		[i.smoked_door, i.t_smokable_door],
		[i.smoked_trapdoor, i.t_smokable_trapdoor],
		[i.smoked_button, i.t_smokable_button],
		[i.smoked_pressureplate, i.t_smokable_pressureplate],
	]
	R.bulk('smoking', smokedFrom.map(p => [p[0], p[1], e, undefined, 0, 200]))

	// ── Treated wood: creosote filling, Create and IE ──
	// Createaddition's single tag recipe (125 mB, any planks) is replaced by the same recipe over the
	// planks tag minus smoked planks, so a smoked plank can only match the cheaper 100 mB recipe below.
	R.remove({ id: 'createaddition:filling/treated_wood_planks' }, e)

	R.createFilling(i.treated_wood_horizontal, [Fluid.of(f.creosote, 125), i.t_unsmoked_planks], e, 'treated_wood_planks')
	R.ieBottling(i.treated_wood_horizontal, f.t_creosote, i.t_unsmoked_planks, e, 'treated_wood_planks', 125)

	R.createFilling(i.treated_wood_horizontal, [Fluid.of(f.creosote, 100), i.smoked_planks], e, 'treated_wood_smoked_planks')
	R.ieBottling(i.treated_wood_horizontal, f.t_creosote, i.smoked_planks, e, 'treated_wood_smoked_planks', 100)

	console.log('Smoked wood finished')
})
