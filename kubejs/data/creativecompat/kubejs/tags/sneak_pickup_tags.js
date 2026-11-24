// Sneak pickup (TODO items/storage-and-item-loss #12, feature_writeup/storage_audit.md 7): blocks in
// #creativecommon:sneak_pickup go to the hand with their contents on a sneak right-click with an empty main hand
// (Creative Common storage/SneakPickup). The block's own loot table must give the block back with its contents;
// Supplementaries' sack tables (and Supplementaries Squared's dyed sacks) do (set_contents).
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {
ServerEvents.tags('block', e => {
	var b = global.blocks
	var t = global.tags
	if (global.has('supplementaries')) t.add(e, b.t_sneak_pickup, b.sack)
	if (global.has('suppsquared')) t.addMatching(e, b.t_sneak_pickup, b.suppsquared_sacks)
})
})()
