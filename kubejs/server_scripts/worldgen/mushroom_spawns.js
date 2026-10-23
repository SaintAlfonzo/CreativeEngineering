// Mushroom spawns: where wild brown, red, inky cap and morel mushrooms, their colonies and the huge Fungi Delight
// mushrooms generate, at the surface and in caves (feature_writeup/fungi_delight.md section 5.1.1).
//
// Edit kubejs/generation_scripts/worldgen/mushroom_spawns.json, then rejoin the world (biome modifiers are read
// at world load, not on /reload). Only chunks generated afterwards change. Resolved lists:
// feature_writeup/crops_by_biome.md (python3 tools/gen_crop_worldgen_table.py evaluates the same rules offline).
//
// Same machinery as wild_crops.js (_biome_rules.js, global.biomeRules) and the same biome words: the "biomes"
// table of wild_crops.json. Per rule key: biome tag creativeengineering:mushroom_spawns/<key>, one forge:add_features
// modifier creativeengineering:mushroom_spawns/<key>, the mods' own modifiers in disable_modifiers replaced by
// forge:none. Each rule has a mode:
//   - surface: vegetal_decoration; biomes marked ocean or cave never match (as in wild_crops.json);
//   - cave: underground_decoration; every biome can match (the placed features search a cave floor below sea level,
//     at least 13 blocks under the surface, so they stay out of the light).
// Rarity lives in the placed features (kubejs/data/creativeengineering/worldgen/placed_feature/wild/mushrooms/).
// Vanilla's own brown and red patches and huge mushrooms are not touched.
//
// CEConfig [worldgen.systems] mushroomSpawns = false: nothing here runs. Farmer's Delight colonies come back on
// mycelium only; Fungi Delight (the pack's port ships no biome modifiers) then generates no mushrooms at all.
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {

	var sys = null

	// built on first use, so the load order against _biome_rules.js does not matter
	function S() {
		return sys || (sys = global.biomeRules.system({
			name: 'mushroom-spawns',
			configPath: 'kubejs/generation_scripts/worldgen/mushroom_spawns.json',
			biomesPath: 'kubejs/generation_scripts/worldgen/wild_crops.json',
			rulesKey: 'rules',
			ruleWord: 'mushroom',
			tagPrefix: 'creativeengineering:mushroom_spawns/',
			modifierPrefix: 'creativeengineering:forge/biome_modifier/mushroom_spawns/',
			offMessage: 'off in CEConfig [worldgen.systems] mushroomSpawns: mushrooms spawn where their mods put them',
			enabled: enabled,
			never: function (rule) { return rule.mode === 'cave' ? {} : { ocean: true, cave: true } },
			step: function (rule) { return rule.mode === 'cave' ? 'underground_decoration' : 'vegetal_decoration' },
			reportUnclassified: false,   // wild_crops.js reports the biome table
		}))
	}

	// CEConfig is read directly (WorldgenConfig has no getter for this switch yet); any failure (config not loaded,
	// older jar without the field) counts as on, like WorldgenConfig's fallbacks.
	function enabled() {
		try {
			return Java.loadClass('com.SaintAlfonzo.creativeengineering.CEConfig').WORLDGEN.systems.mushroomSpawns.get() == true
		} catch (err) {
			return true
		}
	}

	ServerEvents.tags('worldgen/biome', function (event) { S().tags(event) })
	ServerEvents.highPriorityData(function (event) { S().data(event) })
	ServerEvents.loaded(function (event) { S().loaded(event) })
})()
