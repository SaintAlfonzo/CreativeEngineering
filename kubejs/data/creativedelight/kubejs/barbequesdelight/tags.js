// requires: barbequesdelight
// Tags of the Barbeque's Delight port (Creative Delight member barbequesdelight, barbeques_delight.md 4 step 2). The port's
// own data has raw_skewers / grilled_skewers and the mineable tags; the skewer family and saltable tags wait for the
// skewer fold (O1, dishes/meals #46). 1.3.0's CarryOn blacklist is dropped (no CarryOn in the pack).
ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('barbequesdelight')) return
	var t = global.tags
	var i = global.item

	t.add(e, i.t_raw_skewers, [i.raw_cod_skewer, i.raw_salmon_skewer, i.raw_chicken_skewer, i.raw_mushroom_skewer,
		i.raw_beef_skewer, i.raw_lamb_skewer, i.raw_rabbit_skewer, i.raw_pork_sausage_skewer, i.raw_potato_skewer,
		i.raw_vegetable_skewer])
	t.add(e, i.t_grilled_skewers, [i.grilled_cod_skewer, i.grilled_salmon_skewer, i.grilled_chicken_skewer,
		i.grilled_mushroom_skewer, i.grilled_beef_skewer, i.grilled_lamb_skewer, i.grilled_rabbit_skewer,
		i.grilled_pork_sausage_skewer, i.grilled_potato_skewer, i.grilled_vegetable_skewer])
})

ServerEvents.tags('block', e => {
	if (!global.CD || !global.CD.on('barbequesdelight')) return
	var t = global.tags
	var b = global.blocks

	// The grill's built-in campfire goes out in rain by the pack rule (3.4.1): GrillBlockEntity checks this tag, as
	// Creative Compat's CampfireRain does for the plain campfire
	t.add(e, b.t_campfire_rain_douses, b.bbq_grill)

	// BlockRunner walk speed (step 17): the grill is metal, the basin and tray wood
	t.add(e, b.t_blockrunner_metal, b.bbq_grill)
	t.add(e, b.t_blockrunner_wood, [b.bbq_basin, b.bbq_tray])
})
