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
		// Item: ground sugar (TODO dishes/ingredients #23, 2026-10-29). Create crushing and Millstone: 1 sugar -> 1 plus a
		// 50% second (1.5 on average); mortar: 3 sugar -> 4. Counts as sugar (#forge:sugar, tags/cookingtags.js) and seasons
		// like sugar (development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/seasonings.js).
		R.foodGrinding([i.powdered_sugar, Item.of(i.powdered_sugar, 1).withChance(0.5)], i.sugar, e, 'sugar_powdered_sugar',
			{ mortar: { count: 4, inputs: 3 } })

		// Block and bucket (patcherMod food/PowderedSugarRegistry): the Vacuum Chamber (evacuating, 600 ticks, as Vintage
		// Improvements' powder snow) packs bucket + 4 sugar into a powdered sugar bucket, the best rate (2 powdered sugar a
		// sugar). The bucket places the block and an empty bucket picks it up, as powder snow. In the grid the bucket holds
		// 8 powdered sugar: emptying it gives the 8 (the bucket stays), and bucket + 8 powdered sugar fills it.
		R.viVacuumizing(i.powdered_sugar_bucket, [i.bucket, Item.of(i.sugar, 4)], e, 'powdered_sugar_bucket')
		R.shapeless(Item.of(i.powdered_sugar, 8), [i.powdered_sugar_bucket], e, 'powdered_sugar_from_bucket')
		R.shapeless(i.powdered_sugar_bucket, [i.bucket, Item.of(i.powdered_sugar, 8)], e, 'powdered_sugar_bucket_from_powder')
		// The Tactical Fishing catch of the bucket is removed in ../vintagedelight/recipes.js (with the salt bucket's).
	})
})
