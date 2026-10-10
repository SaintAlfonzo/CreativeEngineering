// Season types: biome tags creativeengineering:seasons/{arid,frozen,dry,tropical,temperate,adaptive}.
//
// The lists live in kubejs/generation_scripts/worldgen/season_types.json (its _readme says what each type
// does); patcherMod seasons/SeasonTypes reads the tags and steers Serene Seasons and SeasonHUD with them.
// Edit the json, then /reload. Reference + tests: feature_writeup/weather.md; per-biome result:
// feature_writeup/weather_by_biome.md (python3 tools/gen_weather_table.py checks the same file offline:
// unknown ids, a biome in two lists, cold or dry biomes left out).
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {

	var CONFIG_PATH = 'kubejs/generation_scripts/worldgen/season_types.json'
	var TYPES = ['arid', 'frozen', 'dry', 'tropical', 'temperate', 'adaptive']

	ServerEvents.tags('worldgen/biome', function (event) {
		var cfg
		try {
			cfg = JSON.parse(String(JsonIO.readString(CONFIG_PATH)))
		} catch (err) {
			console.error('[season-types] could not read ' + CONFIG_PATH + ', no season tags: ' + err)
			return
		}
		TYPES.forEach(function (type) {
			var biomes = cfg[type] || []
			if (biomes.length) global.tags.add(event, 'creativeengineering:seasons/' + type, biomes)
		})
	})

})()
