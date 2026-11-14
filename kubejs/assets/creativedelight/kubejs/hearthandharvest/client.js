// requires: creativedelight
// requires: hearthandharvest
// Tooltip lines of Hearth and Harvest rules that live on other mods' items (hearth_and_harvest.md 4.2 step 18).
ItemEvents.tooltip(e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return
	var i = global.item
	var hat = Text.gray('Grants XP when harvesting crops')
	;[i.farmers_hat_helmet, i.straw_hat, i.farmerbrim].forEach(id => e.add(id, hat))
	e.add(i.seed_pouch, Text.gray('Tops up from seeds picked up while on the hotbar'))
	e.add(i.seed_pouch, Text.gray('Plants in a dispenser'))
	// Raspberry Pie pun (raspberry_pie.md 12.4, TODO food/types-and-tuning #29).
	var pi = Text.gray("Doesn't run programs").italic()
	;[i.raspberry_pie, i.raspberry_pie_slice].forEach(id => e.add(id, pi))
})
