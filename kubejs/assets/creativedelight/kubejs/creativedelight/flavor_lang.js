// requires: creativedelight
// Flavor names for the food tooltip lines (Creative Delight food/FlavorTooltip): lang key flavor.<ns>.<path> from each
// flavor's id and name in Creative Delight's kubejs_startup/creativedelight/flavors.js (flavors were seasonings until
// food/seasoning #25).
ClientEvents.lang('en_us', e => {
	if (!global.CD || !global.flavor) return
	Object.keys(global.flavor).forEach(key => {
		var s = global.flavor[key]
		var colon = s.id.indexOf(':')
		e.add('flavor.' + s.id.substring(0, colon) + '.' + s.id.substring(colon + 1), s.name)
	})
})
