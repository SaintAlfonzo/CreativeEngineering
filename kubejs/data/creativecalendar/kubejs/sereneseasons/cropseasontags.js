// requires: sereneseasons
// Creative Calendar (feature_writeup/creative_calendar.md 4.4); moved from kubejs/server_scripts/tags/ on 2026-11-01.
// Crop growing season types: block and item tags creativeengineering:crop_seasons/<type>.
//
// The lists live in kubejs/generation_scripts/worldgen/crop_seasons.json (its _readme says what each type does);
// Creative Calendar seasons/CropSeasons reads the tags for growth, the planting-item tooltip and WTHIT. Edit the json,
// then /reload. Also extends #sereneseasons:greenhouse_glass from the json's greenhouse_glass list.
// Reference: feature_writeup/crops.md 2.12.1; python3 tools/crop_season_audit.py checks the result
// (a block in two types, an audited plant left out).
//
// Wrapped in an IIFE: all server scripts of a pack share one top-level scope.

(function () {

	var CONFIG_PATH = 'kubejs/generation_scripts/worldgen/crop_seasons.json'
	var TYPES = ['temperate', 'cold', 'warm_dry', 'warm_wet', 'autumn_fruit', 'grass', 'nether', 'end', 'cave', 'mushroom',
		'grape_temperate', 'grape_cold', 'grape_warm_dry', 'grape_warm_wet', 'grape', 'none']
	var ForgeRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	var ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')

	function readConfig() {
		try {
			return JSON.parse(String(JsonIO.readString(CONFIG_PATH)))
		} catch (err) {
			console.error('[crop-seasons] could not read ' + CONFIG_PATH + ', no crop season tags: ' + err)
			return null
		}
	}

	// one type per id; unregistered ids skipped (the mod is not in the pack)
	function fill(event, lists, registry, what) {
		if (!lists) return
		var seen = {}
		var skipped = 0
		Object.keys(lists).forEach(function (type) {
			if (TYPES.indexOf(type) < 0) {
				console.error('[crop-seasons] ' + what + ': unknown type ' + type)
				return
			}
			var present = lists[type].filter(function (id) {
				if (seen[id]) {
					console.error('[crop-seasons] ' + what + ' ' + id + ' is in ' + seen[id] + ' and ' + type + ', kept in ' + seen[id])
					return false
				}
				seen[id] = type
				if (registry.containsKey(new ResourceLocation(id))) return true
				skipped++
				return false
			})
			if (present.length) global.tags.add(event, 'creativeengineering:crop_seasons/' + type, present)
		})
		if (skipped) console.info('[crop-seasons] ' + skipped + ' listed ' + what + 's are not registered')
	}

	ServerEvents.tags('block', function (event) {
		var cfg = readConfig()
		if (!cfg) return
		fill(event, cfg.blocks, ForgeRegistries.BLOCKS, 'block')
		// greenhouse glass: '#tags' and '/regex/' pass through, plain ids only when registered
		var glass = ((cfg.greenhouse_glass || {}).add || []).filter(function (id) {
			return id.charAt(0) == '#' || id.charAt(0) == '/' || ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(id))
		})
		if (glass.length) global.tags.addMatching(event, 'sereneseasons:greenhouse_glass', glass)
	})

	ServerEvents.tags('item', function (event) {
		var cfg = readConfig()
		if (cfg) fill(event, cfg.items, ForgeRegistries.ITEMS, 'item')
	})
})()
