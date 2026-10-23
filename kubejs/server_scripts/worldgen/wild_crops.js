// Wild crops: which biomes each wild crop and fruit tree generates in.
//
// Edit kubejs/generation_scripts/worldgen/wild_crops.json, then rejoin the world (biome modifiers are read
// at world load, not on /reload). Only chunks generated afterwards change. Reference + tests:
// feature_writeup/crops.md section 2.8; resolved lists: feature_writeup/crops_by_biome.md
// (python3 tools/gen_crop_worldgen_table.py evaluates the same rules offline).
//
// Per crop key in the config: biome tag creativeengineering:wild_crops/<key>, one forge:add_features modifier
// creativeengineering:wild_crops/<key> at the crop's step ("step", default vegetal_decoration; the truffle ground
// rules use underground_ores), the mods' own modifiers in disable_modifiers replaced by forge:none, and
// remove_features in one forge:remove_features modifier. The machinery is shared with mushroom_spawns.js and
// lives in _biome_rules.js (global.biomeRules). Biomes marked ocean or cave never match a wild crop rule.
// ServerEvents.loaded compares the live tags with the rules and lists overworld biomes the config doesn't classify.
//
// CEConfig [worldgen.systems] wildCrops = false: nothing here runs, every crop as its mod ships it.
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {

	var sys = null

	// built on first use, so the load order against _biome_rules.js does not matter
	function S() {
		return sys || (sys = global.biomeRules.system({
			name: 'wild-crops',
			configPath: 'kubejs/generation_scripts/worldgen/wild_crops.json',
			rulesKey: 'crops',
			ruleWord: 'crop',
			tagPrefix: 'creativeengineering:wild_crops/',
			modifierPrefix: 'creativeengineering:forge/biome_modifier/wild_crops/',
			offMessage: 'off in CEConfig [worldgen.systems] wildCrops: every crop spawns where its mod puts it',
			enabled: enabled,
			never: function () { return { ocean: true, cave: true } },
			step: function (crop) { return crop.step || 'vegetal_decoration' },
			reportUnclassified: true,
		}))
	}

	function enabled() {
		try {
			return Java.loadClass('com.SaintAlfonzo.creativeengineering.worldgen.WorldgenConfig').wildCropsEnabled() == true
		} catch (err) {
			return true   // older jar: no switch
		}
	}

	ServerEvents.tags('worldgen/biome', function (event) { S().tags(event) })
	ServerEvents.highPriorityData(function (event) { S().data(event) })
	ServerEvents.loaded(function (event) { S().loaded(event) })
})()
