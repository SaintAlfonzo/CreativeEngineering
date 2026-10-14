// Season types: biome tags creativeengineering:seasons/{arid,frozen,dry,tropical,temperate,cold,adaptive} and
// creativeengineering:seasons/permanent_{spring,summer,autumn,winter}.
// Snow seasons: biome tags creativeengineering:snow/no_<sub_season>, one per sub-season a biome may not snow in
// (season_types.json snow_seasons lists the sub-seasons it may snow in; [] = never).
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
	var TYPES = ['arid', 'frozen', 'dry', 'tropical', 'temperate', 'cold', 'adaptive',
		'permanent_spring', 'permanent_summer', 'permanent_autumn', 'permanent_winter']
	var SEASONS = ['spring', 'summer', 'autumn', 'winter']
	var SUB_SEASONS = []
	SEASONS.forEach(function (season) {
		['early_', 'mid_', 'late_'].forEach(function (part) { SUB_SEASONS.push(part + season) })
	})

	// 'winter' -> its three sub-seasons; a sub-season name -> itself; anything else -> null
	function expand(name) {
		if (SUB_SEASONS.indexOf(name) >= 0) return [name]
		if (SEASONS.indexOf(name) >= 0) return SUB_SEASONS.filter(function (sub) { return sub.slice(-name.length - 1) == '_' + name })
		return null
	}

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

		var snowSeasons = cfg.snow_seasons || {}
		var blocked = {}
		SUB_SEASONS.forEach(function (sub) { blocked[sub] = [] })
		Object.keys(snowSeasons).forEach(function (biome) {
			var allowed = []
			snowSeasons[biome].forEach(function (name) {
				var subs = expand(String(name))
				if (subs == null) console.error('[season-types] snow_seasons ' + biome + ': unknown season ' + name)
				else allowed = allowed.concat(subs)
			})
			SUB_SEASONS.forEach(function (sub) {
				if (allowed.indexOf(sub) < 0) blocked[sub].push(biome)
			})
		})
		SUB_SEASONS.forEach(function (sub) {
			if (blocked[sub].length) global.tags.add(event, 'creativeengineering:snow/no_' + sub, blocked[sub])
		})
	})

})()
