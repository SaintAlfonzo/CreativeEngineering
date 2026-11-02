// requires: cozys_improved_cats
// Food item merge (feature_writeup/item_merges.md 2.12, cozy_cats.md 10), Creative Delight's group: the server half is
// data/creativedelight/kubejs/_core/item_merges.js and cozys_improved_cats/item_merges.js. Restart to apply.

// Cozy's Improved Cats' ancient fish go, the vanilla fish stay: cats tame with vanilla cod and salmon
// (allowVanillaTaming in config/cozys_improved_cats-common.toml), so the ancient fish have no job left. The Training
// String recipe then reads cod + salmon + string. Fishing swap, mob drops (config) and fisherman trades
// (Creative Compat cats.CozyCats) are off at the source too.
var i = global.item
global.addItemMerges('creativedelight', [
	[i.ancient_cod, i.cod],
	[i.ancient_salmon, i.salmon],
	[i.ancient_cooked_cod, i.cod_cooked],
	[i.ancient_cooked_salmon, i.salmon_cooked],
])
