// priority: -100000
// Rows skipped for mods that are not loaded (plan_cb_publish.md M2 rule G5). global.NCH and global.tags drop a recipe
// or tag entry whose id belongs to an absent mod without an error each; this prints one line per mod, then resets the
// counts for the next /reload. A missing id of a mod that IS loaded is not counted here: NCH still logs it as an error.
//
// Runs as the last ServerEvents.recipes handler (lowest priority header, so it registers after every other recipe
// script). Tags reload before recipes, so the tag count is already complete; the recipe count is complete because every
// NCH call runs inside an earlier recipes handler. KubeJS 1902.6.2-build.73 never posts ServerEvents.afterRecipes, so
// the print cannot live there (plan_cb_polish.md 3, scratch r1106cbo).

ServerEvents.recipes(event => {
	var sources = [['recipe', global.NCH && global.NCH._absentCounts], ['tag entry', global.tags && global.tags._absentCounts]]
	var lines = 0
	sources.forEach(src => {
		var counts = src[1]
		if (!counts) return
		Object.keys(counts).sort().forEach(ns => {
			console.info('[absent mods] skipped ' + Math.round(counts[ns]) + ' ' + src[0] + ' rows for absent ' + ns)
			delete counts[ns]
			lines++
		})
	})
	// always one line, so a log without it means this listener never ran (scratch r1106cba SQ-CBA-18 could not tell)
	if (lines === 0) console.info('[absent mods] none skipped')
})
