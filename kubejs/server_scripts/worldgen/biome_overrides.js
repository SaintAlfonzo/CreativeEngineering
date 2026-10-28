// Biome frequency tuning: multiplier / replace_with per biome, per multi_noise dimension.
//
// Edit kubejs/generation_scripts/worldgen/biome_config.json, then create a NEW world: every world
// stores its biome layout in level.dat at creation, so existing saves keep theirs. Multipliers
// other than 0/1 need a climate sample first: run /dumpclimate in any world with this pack, then
// /reload (or rejoin) so the solver sees it before you create the world. Refresh the baseline
// after mod updates with `python3 tools/gen_biome_config.py` (keeps your edits).
//
// The solver is patcherMod's BiomeLayoutTuner (Java: it brute-forces ~1700 climate entries against
// every sample, too slow for Rhino). It runs here, in ServerEvents.highPriorityData, which KubeJS
// fires while the world's datapacks load (world creation included), and the edited dimension
// layouts go into KubeJS's top-priority virtual datapack. Results are cached in memory and on disk
// (generation_scripts/worldgen/biome_layout_cache.json.gz, ships with the pack), so /reload and a new
// launch with nothing edited don't re-solve. Solver stats (share_pct / result_pct / note) are written back
// into biome_config.json when they change.
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {

	var TAG = '[biome-tuning] '
	var Tuner = Java.loadClass('com.SaintAlfonzo.creativeengineering.worldgen.BiomeLayoutTuner')

	// Plain size()/get() on the Java lists: no reliance on Rhino's function -> Consumer conversion.
	function each(list, fn) {
		for (var i = 0; i < list.size(); i++) fn(String(list.get(i)))
	}

	ServerEvents.highPriorityData(function (event) {
		var result = Tuner.run()
		if (result.isFromCache()) {
			console.info(TAG + 'inputs unchanged, reusing the last solve (' + result.getCacheSource() + ')')
		} else {
			each(result.getMessages(), function (m) { console.info(TAG + m) })
		}
		each(result.getErrors(), function (e) { console.error(TAG + e) })

		var it = result.getFiles().entrySet().iterator()
		while (it.hasNext()) {
			var entry = it.next()
			event.add(entry.getKey(), entry.getValue())
		}
	})
})()
