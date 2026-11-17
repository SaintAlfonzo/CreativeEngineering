// Power Grid (development/backportedPowerGrid, mod id powergrid): pack-side recipe changes. The port jar keeps
// upstream's data; pack changes go here (plan_bugfix_1114.md 8.3, TODO largecontent/mod-additions #82).
//
//   acid  redstone + blaze powder + rotten flesh + 250 mB water, heated -> 250 mB acid
//         (upstream: no rotten flesh). The flesh tells it apart from Creative Delight's heated water -> salt in JEI and
//         when items go in first. Water poured into a heated basin before any item still becomes salt.
//   acid  rotten flesh + spider eye + 250 mB water, no heat -> 250 mB acid (plan_post_scratch_1115.md D4, SQ-PWG-80b).
//         Unheated, so a plain basin of water takes the items without Creative Delight's heated water -> salt running
//         first; Create: The Salt's flesh salting needs salt, so it does not match either.

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
	R.createMixing(Fluid.of(ACID, 250), [i.rotten_flesh, i.spider_eye, Fluid.of(f.water, 250)], e,
		'powergrid_acid_in_water')

	console.log('power grid recipes done')
})
