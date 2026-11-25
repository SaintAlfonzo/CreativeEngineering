// requires: creativedelight
// Shipping Bin port (TODO mod-integration #7, feature_writeup/farming_for_blockheads.md; CD farm/shippingbin/, ids under
// the farmingforblockheads namespace). The bin's crafting recipe (upstream's shape: iron, any log, emerald, green wool),
// the temporary Courier Spawn Point recipe (ruling D8, final recipe TODO mod-integration #9: a chest in 8 trapdoors of any
// kind), the 12 vanilla value rows (rulings D3: upstream's values; points per item, a load is [shipping_bin] capacity,
// default 128 = 1 emerald through the payout table farmingforblockheads:gameplay/shipping_bin) and the axe tags.

// item dict key -> points per item (section 2.1 table)
var CD_SHIPPING_VALUES = {
	wheat: 4, minecraft_carrot: 5, minecraft_potato: 5, minecraft_beetroot: 6, pumpkin: 8, melon_block: 8, melon: 2,
	sugar_cane: 2, glow_berries: 6, nether_wart: 6, kelp: 1, sweet_berries: 1
}

ServerEvents.tags('block', e => {
	if (!global.CD) return
	var b = global.blocks
	global.tags.add(e, b.t_mineable_axe, [b.shipping_bin, b.courier_spawn])
})

ServerEvents.recipes(e => {
	if (!global.CD) return
	var i = global.item
	var R = global.NCH.forPack('creativedelight')

	global.CD.recipes(() => {
		R.shaped(i.shipping_bin, ['IWI', 'WEW', 'IGI'], { I: i.ingot_iron, W: i.any_log, E: i.emerald, G: i.green_wool }, e, 'shipping_bin')
		R.shaped(i.courier_spawn, ['TTT', 'TCT', 'TTT'], { T: i.any_trapdoor, C: i.chest }, e, 'courier_spawn')
		Object.keys(CD_SHIPPING_VALUES).forEach(k => R.shippingBin(i[k], CD_SHIPPING_VALUES[k], e))
	})
})
