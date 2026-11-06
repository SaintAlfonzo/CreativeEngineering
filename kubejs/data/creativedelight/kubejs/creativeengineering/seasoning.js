// The pack's seasoning crafts on foods. Moved from the pack's kubejs/server_scripts/cooking/seasoning.js 2026-10-25,
// rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	const R = global.NCH

	global.CD.recipes(() => {

		// ============================================================
		// Pack seasonings on foods (development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/seasonings.js holds the seasonings themselves)
		// ============================================================
		// One craft per seasoning: any food in its seasonable tag (seasoning_tags.js: families, then single adds and
		// removals) + the seasoning item -> the same food carrying it. Plus one craft per pair that combines, both at
		// once (the craft checks the food is in both tags). A food takes a seasoning once; seasonings that don't combine
		// can't share a food, so garlic popcorn + onion, onion popcorn + garlic, or popcorn + both give the same stack.
		var S = global.seasoning
		var seasonable = key => {
			var id = S[key].id
			return '#' + id.substring(0, id.indexOf(':')) + ':seasonable/' + id.substring(id.indexOf(':') + 1)
		}
		var hasFoods = key => (S[key].families || []).length + (S[key].add || []).length > 0 &&
			(!S[key].needs || global.CD.on(S[key].needs))
		var keys = Object.keys(S).filter(hasFoods)
		keys.forEach(key => R.season(seasonable(key), key, e, key))
		keys.forEach((a, n) => keys.slice(n + 1).forEach(b => {
			if ((S[a].combines || []).indexOf(b) < 0 && (S[b].combines || []).indexOf(a) < 0) return
			var pair = [a, b].sort()
			R.season(seasonable(pair[0]), pair, e, pair.join('_'))
		}))
	})
})
