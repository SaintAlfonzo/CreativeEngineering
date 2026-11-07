// requires: creativedelight
// requires: nethersdelight
// Nether's Delight client rows (Creative Delight member repackedNethers; nethers_delight.md 4.1, 4.5): the 4.0 names.
// Ids stay 3.1's; only the display names change.
ClientEvents.lang('en_us', e => {
	if (!global.CD) return
	// set in the nethersdelight namespace, where the mod's lang sets it
	e.add('nethersdelight', 'block.nethersdelight.hoglin_mount', 'Hoglin Trophy')
	e.add('nethersdelight', 'block.nethersdelight.potted_mimicarnation', 'Potted Mimicarnation')
})
