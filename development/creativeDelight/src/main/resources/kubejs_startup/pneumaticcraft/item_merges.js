// requires: pneumaticcraft
// requires: veggiesdelight
// Food item merge (feature_writeup/item_merges.md 2.13; TODO merges/dishes #1, Veggies Delight M1), Creative Delight's
// group: the server half is data/creativedelight/kubejs/_core/item_merges.js and pneumaticcraft/recipes.js (one
// frying route and one grid route for the survivor). Restart to apply.

var i = global.item
global.addItemMerges('creativedelight', [
	// PneumaticCraft's Cod 'n' Chips goes, Veggies Delight's Fish and Chips stays
	[i.cod_n_chips, i.fish_and_chips],
])
