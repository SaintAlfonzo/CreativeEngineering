// requires: creativedelight
// requires: rusticdelight
// Rustic Delight block and item names, moved from the pack's client_scripts/namechanges.js 2026-10-27, rows unchanged.
ClientEvents.lang('en_us', e => {
	if (!global.CD || !global.CD.on('rusticdelight')) return
	e.add('block.rusticdelight.bell_peppers', 'Bell Peppers')
	e.add('block.rusticdelight.cotton', 'Cotton')
	// Rustic Delight 1.7.0's names for the boll and its crate (rustic_delight.md 5)
	e.add('item.rusticdelight.cotton_boll', 'Cotton')
	e.add('block.rusticdelight.cotton_boll_crate', 'Cotton Crate')
	e.add('block.rusticdelight.coffee', 'Coffee Plant')
	// The port's own crate (creativedelight/members #25)
	e.add('block.rusticdelight.glowing_calamari_crate', 'Glowing Calamari Crate')
	e.add('block.rusticdelight.potted_wild_bell_peppers', 'Potted Wild Bell Peppers')
	e.add('block.rusticdelight.potted_wild_coffee', 'Potted Wild Coffee')
	e.add('block.rusticdelight.potted_wild_cotton', 'Potted Wild Cotton')
})
