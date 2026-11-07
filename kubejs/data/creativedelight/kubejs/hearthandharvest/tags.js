// requires: creativedelight
// requires: hearthandharvest
// Item tags of the Hearth and Harvest content ported into the Creative Delight member hearthandharvest
// (hearth_and_harvest.md 4.1). 1.4.1's c: tags as forge: tags; the pack's plural #forge:fruits/raspberries is the fruit
// tag (it already existed, empty). Seasons: kubejs/generation_scripts/worldgen/crop_seasons.json, not Serene Seasons tags.
ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return
	var t = global.tags
	var i = global.item

	// addTag: also the parent tag (#forge:fruits, #forge:crops, #forge:seeds)
	t.addTag(e, i.t_fruits_raspberries, i.raspberry)
	t.addTag(e, i.t_crops_raspberry, i.raspberry)
	t.addTag(e, i.t_seeds_raspberry, i.raspberry_pips)
	t.add(e, [i.t_berries, i.t_fruits_berries, i.t_fox_food], i.raspberry)

	// Watering can fertiliser, loaded from the off hand (4.0). 1.4.1 also lists its Manure (not ported yet).
	t.add(e, i.t_bonemeal_substitutes, [i.bonemeal])
})
