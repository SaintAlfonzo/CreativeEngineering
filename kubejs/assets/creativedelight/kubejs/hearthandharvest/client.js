// requires: creativedelight
// requires: hearthandharvest
// Tooltip lines of Hearth and Harvest rules that live on other mods' items (hearth_and_harvest.md 4.2 step 18).
ItemEvents.tooltip(e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return
	var i = global.item
	var hat = Text.gray('Grants XP when harvesting crops')
	;[i.farmers_hat_helmet, i.straw_hat, i.farmerbrim].forEach(id => { if (global.has(id)) e.add(id, hat) })
	if (global.has(i.seed_pouch)) {
		e.add(i.seed_pouch, Text.gray('Tops up from seeds picked up while on the hotbar'))
		// Dispenser planting (plan_bugfix_1114 1.2; hearth gear/SeedContainerDispense.java).
		e.add(i.seed_pouch, Text.gray('In a dispenser: plants up to 25 seeds in a 5x5'))
		e.add(i.seed_pouch, Text.gray('Facing air: plants on the blocks below; facing soil: plants on top'))
		e.add(i.seed_pouch, Text.gray('Stays in the dispenser'))
	}
	e.add(i.bundle, Text.gray('In a dispenser: plants its first seed; stays in the dispenser'))
	// End stone tills only with a netherite or better hoe, into Ender Crop's tilled end stone (plan_bugfix_1114 3.2;
	// hearth gear/TillingEvents.java gates Ender Crop's own hoe path too).
	if (global.has.mod('endercrop')) {
		var till = Text.gray('Till end stone with a netherite hoe')
		;[i.end_stone, i.tilled_end_stone].forEach(id => e.add(id, till))
	}
	// Raspberry Pie pun (raspberry_pie.md 12.4, TODO food/types-and-tuning #29).
	var pi = Text.gray("Doesn't run programs").italic()
	;[i.raspberry_pie, i.raspberry_pie_slice].forEach(id => e.add(id, pi))
})
