// requires: oceansdelight
// Ocean's / Oceanic Delight tag rows (feature_writeup/ocean_delight.md). Oceanic Delight's squid tentacles are removed
// (startup_scripts/_lib/removed_items.js, dishes/ocean #4), which empties the squid tentacle tag its sea pickle roll,
// shrimp rice, squid salad and takoyaki use. Raw calamari (plain, glowing and sliced) fills the tag so those dishes
// take calamari like the rest of the tentacle dishes. Miner's takoyaki (forge:foods/tentacles) gets the same.
ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('oceansdelight')) return
	var i = global.item
	if (global.CD.on('oceanic_delight')) global.tags.add(e, i.t_squid_tentacles, i.t_raw_calamari)
	// Miner's takoyaki takes two of its tentacle tag, also empty with the tentacles gone
	if (global.CD.on('miners_delight')) global.tags.add(e, i.t_foods_tentacles, i.t_raw_calamari)
})
