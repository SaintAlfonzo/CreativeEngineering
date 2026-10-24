// Seasoning names for the food tooltip lines (patcherMod food/SeasoningTooltip): lang key seasoning.<ns>.<path>
// from each seasoning's id and name in startup_scripts/cooking/seasonings.js.
ClientEvents.lang('en_us', e => {
	Object.keys(global.seasoning || {}).forEach(key => {
		var s = global.seasoning[key]
		var colon = s.id.indexOf(':')
		e.add('seasoning.' + s.id.substring(0, colon) + '.' + s.id.substring(colon + 1), s.name)
	})
})
