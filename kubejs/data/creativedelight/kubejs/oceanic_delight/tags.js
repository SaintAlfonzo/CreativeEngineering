// requires: creativedelight
// requires: oceanic_delight
// Oceanic Delight block tags (ocean_delight.md 3.1, 7; TODO farming/crops #27): changes from the newer Oceanic jar
// (1.0.4) rebuilt on the 1.0.1 port. The fishing net tags are in fishing_net_tags.js.
ServerEvents.tags('block', e => {
	if (!global.CD || !global.CD.on('oceanic_delight')) return
	var i = global.item
	// 1.0.4: the sea grape plant breaks faster with a hoe, as its fruit already did
	global.tags.mineable(e, 'hoe', null, i.sea_grape_plant)
})
