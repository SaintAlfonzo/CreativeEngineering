// Wild crops: which biomes each wild crop and fruit tree generates in (Creative Delight, creative_delight.md 24).
//
// Edit wild_crops.json next to this file, then rejoin the world (biome modifiers are read at world load, not on
// /reload). Only chunks generated afterwards change. Reference + tests: feature_writeup/crops.md section 2.8;
// resolved lists: feature_writeup/crops_by_biome.md (python3 tools/gen_crop_worldgen_table.py evaluates the same
// rules offline; tools/test_wild_crops.sh runs this file in Rhino).
//
// Per crop key in the config: biome tag creative_delight:wild_crops/<key>, one forge:add_features modifier
// creative_delight:wild_crops/<key> at the crop's step ("step", default vegetal_decoration; the truffle ground
// rules use underground_ores), the mods' own modifiers in disable_modifiers replaced by forge:none, and
// remove_features in one forge:remove_features modifier. The CD features it adds (creative_delight:wild/*) are in
// kubejs/data/creative_delight/worldgen/. The machinery is the pack's _biome_rules.js (global.biomeRules, shared
// with mushroom_spawns.js); the biome traits are the pack's biome_traits.json. Biomes marked ocean or cave never
// match a wild crop rule. ServerEvents.loaded compares the live tags with the rules and lists overworld biomes the
// trait table doesn't classify.
//
// The rules are read from the pack folder copy while it exists (dev), else from the jar copy (CDResources): KubeJS's
// JsonIO only reads files.
//
// creative_delight-common.toml [worldgen] wildCrops = false: nothing here runs, every crop as its mod ships it.
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {
	if (!global.CD) return

	var RULES = 'data/creative_delight/kubejs/_worldgen/wild_crops.json'
	var sys = null

	// built on first use, so the load order against _biome_rules.js does not matter
	function S() {
		return sys || (sys = global.biomeRules.system({
			name: 'wild-crops',
			configPath: RULES,
			biomesPath: 'kubejs/generation_scripts/worldgen/biome_traits.json',
			readConfig: readRules,
			rulesKey: 'crops',
			ruleWord: 'crop',
			tagPrefix: 'creative_delight:wild_crops/',
			modifierPrefix: 'creative_delight:forge/biome_modifier/wild_crops/',
			offMessage: 'off in creative_delight-common.toml [worldgen] wildCrops: every crop spawns where its mod puts it',
			enabled: function () { return global.CD.worldgen ? global.CD.worldgen('wildCrops') : true },   // older jar: no switch
			never: function () { return { ocean: true, cave: true } },
			step: function (crop) { return crop.step || 'vegetal_decoration' },
			reportUnclassified: true,
		}))
	}

	function readRules() {
		var text = String(JsonIO.readString('kubejs/' + RULES))
		if (text !== 'null') return text
		var jar = Java.loadClass('com.SaintAlfonzo.creativedelight.CDResources').text(RULES)
		return jar == null ? null : String(jar)
	}

	ServerEvents.tags('worldgen/biome', function (event) { S().tags(event) })
	ServerEvents.highPriorityData(function (event) { S().data(event) })
	ServerEvents.loaded(function (event) { S().loaded(event) })
})()
