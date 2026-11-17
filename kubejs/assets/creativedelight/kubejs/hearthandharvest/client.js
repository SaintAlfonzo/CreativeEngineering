// requires: creativedelight
// requires: hearthandharvest
// Tooltip lines of Hearth and Harvest rules that live on other mods' items (hearth_and_harvest.md 4.2 step 18).
// A tooltip line in a col role (global.tooltips, Creative Common's client _lib; read at event time)
var tipLine = (text, role, mods) => Text.of(global.tooltips.buildTooltipLine([text, global.tooltips.col[role], mods]))

ItemEvents.tooltip(e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return
	var i = global.item
	var hat = tipLine('Grants XP when harvesting crops', 'text')
	;[i.farmers_hat_helmet, i.straw_hat, i.farmerbrim].forEach(id => { if (global.has(id)) e.add(id, hat) })
	if (global.has(i.seed_pouch)) {
		e.add(i.seed_pouch, tipLine('Tops up from seeds picked up while on the hotbar', 'text'))
		// Dispenser planting (plan_bugfix_1114 1.2; hearth gear/SeedContainerDispense.java).
		e.add(i.seed_pouch, tipLine('In a dispenser: plants up to 25 seeds in a 5x5', 'text'))
		e.add(i.seed_pouch, tipLine('Facing air: plants on the blocks below; facing soil: plants on top', 'text'))
		e.add(i.seed_pouch, tipLine('Stays in the dispenser', 'text'))
	}
	e.add(i.bundle, tipLine('In a dispenser: plants its first seed; stays in the dispenser', 'text'))
	// End stone tills only with a netherite or better hoe, into Ender Crop's tilled end stone (plan_bugfix_1114 3.2;
	// hearth gear/TillingEvents.java gates Ender Crop's own hoe path too).
	if (global.has.mod('endercrop')) {
		var till = tipLine('Till end stone with a netherite hoe', 'text')
		;[i.end_stone, i.tilled_end_stone].forEach(id => e.add(id, till))
	}
	// Raspberry Pie pun (raspberry_pie.md 12.4, TODO food/types-and-tuning #29).
	var pi = tipLine("Doesn't run programs", 'text', { italic: true })
	;[i.raspberry_pie, i.raspberry_pie_slice].forEach(id => e.add(id, pi))
})
