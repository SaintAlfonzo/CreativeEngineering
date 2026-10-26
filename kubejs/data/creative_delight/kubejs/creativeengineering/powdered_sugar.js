// Powdered sugar: grinding, block and bucket. Moved from the pack's kubejs/server_scripts/cooking/powdered_sugar.js
// 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	const R = global.NCH
	var i = global.item

	global.CD.recipes(() => {

		// ============================================================
		// Powdered sugar (TODO dishes/ingredients #16, feature_writeup/food.md 3.13)
		// ============================================================
		// Item: ground sugar, 1 -> 1 (Create crushing, Millstone, mortar). Counts as sugar (#forge:sugar, tags/cookingtags.js)
		// and seasons like sugar (development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/seasonings.js).
		R.foodGrinding(i.powdered_sugar, i.sugar, e, 'sugar_powdered_sugar')

		// Block and bucket (patcherMod food/PowderedSugarRegistry): mirrors Vintage Improvements' powder snow (bucket + snow
		// block, vacuumizing 600 ticks), with 3 sugar for the snow block. The bucket places the block and an empty bucket
		// picks it up, as powder snow. Emptying the bucket in the grid gives the 3 back as powdered sugar (the bucket stays).
		R.viVacuumizing(i.powdered_sugar_bucket, [i.bucket, Item.of(i.sugar, 3)], e, 'powdered_sugar_bucket')
		R.shapeless(Item.of(i.powdered_sugar, 3), [i.powdered_sugar_bucket], e, 'powdered_sugar_from_bucket')
	})
})
