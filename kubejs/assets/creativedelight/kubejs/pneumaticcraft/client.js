// requires: creativedelight
// requires: pneumaticcraft
// PneumaticCraft food client rows. The Fish Tempura renames went with the tempuras (removed items,
// TODO merges/dishes #11)

// Cod 'n' Chips merged into Veggies Delight's Fish and Chips (TODO merges/dishes #1); the tooltip moved with it
// A tooltip line in a col role (global.tooltips, Creative Common's client _lib; read at event time)
var tipLine = (text, role, mods) => Text.of(global.tooltips.buildTooltipLine([text, global.tooltips.col[role], mods]))

ItemEvents.tooltip(e => {
	if (!global.CD || !global.CD.on('veggiesdelight')) return
	e.add(global.item.fish_and_chips, tipLine('The chips are fries. Don\'t tell the British.', 'hint'))
})
