// requires: creativedelight
// requires: vintagedelight
// Tooltip lines on Vintage Delight items. Tropical Pizza pun (TODO dishes/meals #27, plan_food_batch_1119.md C2).
// A tooltip line in a col role (global.tooltips, Creative Common's client _lib; read at event time)
var tipLine = (text, role, mods) => Text.of(global.tooltips.buildTooltipLine([text, global.tooltips.col[role], mods]))

ItemEvents.tooltip(e => {
	if (!global.CD || !global.CD.on('vintagedelight')) return
	var i = global.item
	var pun = tipLine('Misnomer, invented in Canada', 'text', { italic: true })
	;[i.tropical_pizza, i.tropical_pizza_slice].forEach(id => { if (global.has(id)) e.add(id, pun) })
})
