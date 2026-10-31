// requires: alexsmobs
// Alex's Mobs food value changes. Creative Delight startup script, loaded from the jar after the pack's
// startup_scripts. Hunger, saturation modifier and effects not named here stay. Restart to apply.
ItemEvents.modification(e => {
	var i = global.item

	// Shrimp Fried Rice (TODO dishes/meals #10): set from its pot recipe (CD alexsmobs/recipes.js), Fried Rice 14 / 21
	// + 2 shrimp 4 / 0.8 each = 22 / 22.6. Hunger 20 (the full bar), modifier 0.6 = 24 saturation, Comfort 240 s
	// as the pack's Fried Rice (comfort_nourishment.json). Upstream 12 / 1.0 (24 saturation), no effect.
	e.modify(i.shrimp_fried_rice, it => it.setFoodProperties(fb => {
		fb.hunger(20)
		fb.saturation(0.6)
		fb.effect('farmersdelight:comfort', 240 * 20, 0, 1.0)
	}))
})
