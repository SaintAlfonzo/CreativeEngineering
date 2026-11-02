// requires: pneumaticcraft
// PneumaticCraft food client rows, moved from the pack's client_scripts/ 2026-10-27, rows unchanged.
ClientEvents.lang('en_us', e => {
	if (!global.CD) return
	var i = global.item
	// PneumaticCraft fried food, now fried in the pot with any fish (TODO dishes/meals #7)
	e.renameItem(i.salmon_tempura, 'Fish Tempura')
	e.renameItem(i.raw_salmon_tempura, 'Raw Fish Tempura')
})

// Cod 'n' Chips merged into Veggies Delight's Fish and Chips (TODO merges/dishes #1); the tooltip moved with it
ItemEvents.tooltip(e => {
	if (!global.CD || !Platform.isLoaded('veggiesdelight')) return
	e.add(global.item.fish_and_chips, Text.green('The chips are fries. Don\'t tell the British.'))
})
