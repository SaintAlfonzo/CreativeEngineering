// Power Grid (development/backportedPowerGrid, mod id powergrid): pack-side recipe changes. The port jar keeps
// upstream's data; pack changes go here (plan_bugfix_1114.md 8.3, TODO largecontent/mod-additions #82).
//
//   acid  redstone + blaze powder + rotten flesh + 250 mB water, heated -> 250 mB acid
//         (upstream: no rotten flesh).
//   Water poured first no longer turns to salt: Creative Delight's heated water -> salt needs a Rock Salt Block in
//   the basin (rock_salt_water_salt, plan_post_scratch_1115 G2c), so one acid recipe is enough. The spider eye
//   in-water variant (powergrid_acid_in_water) was dropped.

ServerEvents.recipes(e => {
	if (!global.has('powergrid')) return
	console.log('power grid recipes starting')

	var i = global.item
	var f = global.fluid
	var R = global.NCH

	// Power Grid's acid fluid has no fluid_dict key yet (f.acid to be added in Creative Common's fluid_dict.js).
	var ACID = f.acid ?? f.acid

	R.remove({ id: 'powergrid:mixing/acid' }, e)
	R.createMixing(Fluid.of(ACID, 250), [i.redstone, i.blaze_powder, i.rotten_flesh, Fluid.of(f.water, 250)], e,
		'powergrid_acid', 'heated')

	console.log('power grid recipes done')
})
