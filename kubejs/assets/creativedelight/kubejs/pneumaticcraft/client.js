// requires: creativedelight
// requires: pneumaticcraft
// PneumaticCraft food client rows, moved from the pack's client_scripts/ 2026-10-27, rows unchanged.
// The Fish Tempura renames went with the tempuras (removed items, TODO merges/dishes #11)

// Cod 'n' Chips merged into Veggies Delight's Fish and Chips (TODO merges/dishes #1); the tooltip moved with it
ItemEvents.tooltip(e => {
	if (!global.CD || !Platform.isLoaded('veggiesdelight')) return
	e.add(global.item.fish_and_chips, Text.green('The chips are fries. Don\'t tell the British.'))
})
