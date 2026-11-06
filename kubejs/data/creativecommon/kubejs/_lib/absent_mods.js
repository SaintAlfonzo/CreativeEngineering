// Rows skipped for mods that are not loaded (plan_cb_publish.md M2 rule G5). global.NCH and global.tags drop a recipe
// or tag entry whose id belongs to an absent mod without an error each; this prints one line per mod once the recipe
// event is over (tags load before recipes, so both counts are complete), then resets them for the next /reload.
// A missing id of a mod that IS loaded is not counted here: NCH still logs it as an error.

ServerEvents.afterRecipes(event => {
	var sources = [['recipe', global.NCH && global.NCH._absentCounts], ['tag entry', global.tags && global.tags._absentCounts]]
	sources.forEach(src => {
		var counts = src[1]
		if (!counts) return
		Object.keys(counts).sort().forEach(ns => {
			console.info('[absent mods] skipped ' + counts[ns] + ' ' + src[0] + ' rows for absent ' + ns)
			delete counts[ns]
		})
	})
})
