// requires: creativedelight
// requires: oceansdelight
// requires: hostiledelight
// Guardian Shell (dishes/ocean #11, plan_owner_rows_1120.md D12, ocean_delight.md 9): a guardian drops Ocean's whole
// guardian item, shown as Guardian Shell, in place of Hostile Delight's raw fillets; the fillets come from cutting the
// shell (oceansdelight/recipes.js). Hostile replaces minecraft:entities/guardian outright (2 rolls of fillet 1-3,
// Prismarine Shard 1-4, Prismarine Crystals 0-2); the fillet entry becomes 1 Guardian Shell and the shard and crystal
// entries stay as they are. Ocean's own guardian_drop loot modifier is left out of its global list in the repack
// (members/oceans), so there is no second shell. Elder guardians are untouched.
//
// Pre-roll, through Creative Loot's drops engine (loot/09_drops.js, L.rewrite), queued with global.LOOT.rules.
(function () {
	var SKIP = '[skip] guardian shell'   // the table has no Hostile fillet entry: left as it is

	global.LOOT.rules(function (L) {
		if (!global.CD || !global.CD.on('oceansdelight') || !global.CD.on('hostiledelight')) return
		var i = global.item
		L.rewrite('minecraft:entities/guardian', function (pools) {
			var changed = false
			pools.forEach(function (pool) {
				;(pool.entries || []).forEach(function (entry) {
					if (entry.name !== i.guardianfillet) return
					entry.name = i.guardian_shell
					delete entry.functions   // set_count 1-3: one shell
					changed = true
				})
			})
			if (!changed) throw new Error(SKIP)
			return true
		}, 'guardian shell')
	})
})()
