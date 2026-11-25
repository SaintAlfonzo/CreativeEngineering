// requires: creativedelight
// Rabbit trap port (TODO mod-integration #10, feature_writeup/farming_for_blockheads.md 2.3; CD farm/rabbittrap/, ids
// under the farmingforblockheads namespace). Upstream's recipe shape (planks, sticks, one carrot; nothing is consumed by
// the trap), the axe tag and the catch list (2.3.1, ruling D11: the 18 types; widening it is mod-integration #12).
// tools/rabbit_trap_catches.py keeps the same 18 types for the unattended biome tables; the trap checks this tag too.

ServerEvents.tags('block', e => {
	if (!global.CD) return
	global.tags.add(e, global.blocks.t_mineable_axe, [global.blocks.rabbit_trap])
})

ServerEvents.tags('entity_type', e => {
	if (!global.CD) return
	var en = global.entity
	global.tags.add(e, en.t_rabbit_trap_capturable, [
		en.rabbit, en.chicken, en.frog, en.cat, en.improved_cat, en.ocelot, en.duck, en.crab,
		en.jerboa, en.platypus, en.skunk, en.roadrunner, en.banana_slug, en.rain_frog, en.mudskipper, en.tasmanian_devil,
		en.raccoon, en.warped_toad
	])
})

ServerEvents.recipes(e => {
	if (!global.CD) return
	var i = global.item
	var R = global.NCH.forPack('creativedelight')

	global.CD.recipes(() => {
		R.shaped(i.rabbit_trap, ['PP ', 'SCP'], { P: i.planks, S: i.stick, C: i.minecraft_carrot }, e, 'rabbit_trap')
	})
})
