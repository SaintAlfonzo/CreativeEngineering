// requires: creativedelight
// requires: domesticationinnovation
// Domestication Innovation food tooltips. A tooltip line in a col role (global.tooltips, Creative Common's client
// _lib; read at event time)
var tipLine = (text, role, mods) => Text.of(global.tooltips.buildTooltipLine([text, global.tooltips.col[role], mods]))

ItemEvents.tooltip(e => {
	if (!global.CD) return
	var i = global.item
	e.add(i.rotten_apple, tipLine('Turns a horse into a zombie horse', 'uncanny'))
	e.add(i.sinister_carrot, tipLine('Turns a zombie horse into a skeleton horse', 'uncanny'))
})
