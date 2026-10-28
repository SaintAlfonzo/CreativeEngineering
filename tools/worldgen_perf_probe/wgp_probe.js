// Scratch-server probe: worldgen hot-spot changes of 2026-10-27 (perf.md 10.7 options 1-4, queue entries SQ-WGP in
// feature_writeup/testing/scratch_queue.md). Copy into a SCRATCH server's kubejs/server_scripts/ as zz_wgp_probe.js
// (never the pack's). Prefix WGP_ (all server scripts share one scope).
//   wgp cliffs   the loaded tectonic:overworld/legacy/cliffs density function, encoded back to JSON: how many
//                flat_cache wraps it has. The encoder inlines referenced functions, so the counts are of the whole
//                tree: 10 flat_cache / 5 interpolated with the pack override in kubejs/data, 7 / 2 with Tectonic
//                2.3.5a's own file (measured 2026-10-27, wgp_new vs wgp_old)
//   wgp dims     per dimension: how many structures DimensionFilter skips there, and the first few ids
//                (all 0 with [worldgen.systems] dimensionFilter = false)
// Every result is a `[wgp] ...` line in logs/latest.log; checks print PASS / FAIL.

var WGP_Registry = Java.loadClass('net.minecraft.core.Registry')
var WGP_RL = Java.loadClass('net.minecraft.resources.ResourceLocation')
var WGP_DF = Java.loadClass('net.minecraft.world.level.levelgen.DensityFunction')
var WGP_JsonOps = Java.loadClass('com.mojang.serialization.JsonOps')
var WGP_Filter = Java.loadClass('com.SaintAlfonzo.creativeengineering.worldgen.DimensionFilter')

function wgpCheck(name, ok, detail) {
	console.info('[wgp] ' + (ok ? 'PASS' : 'FAIL') + ' ' + name + ': ' + detail)
}

function wgpCount(text, part) { return text.split(part).length - 1 }

function wgpCliffs(server) {
	var reg = server.registryAccess().registryOrThrow(WGP_Registry.DENSITY_FUNCTION_REGISTRY)
	var df = reg.get(new WGP_RL('tectonic:overworld/legacy/cliffs'))
	if (df == null) { wgpCheck('cliffs', false, 'tectonic:overworld/legacy/cliffs is not registered'); return }
	var json = String(WGP_DF.DIRECT_CODEC.encodeStart(WGP_JsonOps.INSTANCE, df).result().orElse(null))
	var cached = wgpCount(json, 'minecraft:flat_cache'), rarity = wgpCount(json, 'tectonic:cliffs/rarity')
	console.info('[wgp] cliffs flat_cache ' + cached + ', interpolated ' + wgpCount(json, 'minecraft:interpolated')
		+ ', rarity noise ' + rarity + ', offset noises ' + wgpCount(json, 'tectonic:offset/'))
	var interp = wgpCount(json, 'minecraft:interpolated')
	wgpCheck('cliffs override', cached == 10 && interp == 5, cached == 10 && interp == 5 ? 'pack override loaded'
		: cached == 7 && interp == 2 ? 'Tectonic\'s own file won' : 'unknown tree (Tectonic updated?): compare with a world without the override')
}

function wgpDims(server) {
	var reg = server.registryAccess().registryOrThrow(WGP_Registry.STRUCTURE_REGISTRY)
	server.getAllLevels().forEach(level => {
		var gen = level.getChunkSource().getGenerator(), skipped = []
		reg.entrySet().forEach(e => { if (WGP_Filter.impossible(gen, e.getValue())) skipped.push(String(e.getKey().location())) })
		skipped.sort()
		console.info('[wgp] dims ' + level.dimension + ': ' + skipped.length + ' of ' + reg.size()
			+ ' structures skipped; first: ' + skipped.slice(0, 12).join(', '))
		if (String(level.dimension) == 'minecraft:overworld')
			wgpCheck('obelisk skipped in the Overworld', skipped.indexOf('twigs:bloodstone_obelisk') >= 0, String(skipped.length) + ' skipped')
		if (String(level.dimension) == 'minecraft:the_nether')
			wgpCheck('obelisk tried in the Nether', skipped.indexOf('twigs:bloodstone_obelisk') < 0, String(skipped.length) + ' skipped')
	})
}

ServerEvents.commandRegistry(event => {
	var C = event.commands
	var root = C.literal('wgp')
	function sub(name, fn) {
		root.then(C.literal(name).executes(ctx => { try { fn(ctx); return 1 } catch (e) { console.info('[wgp] ERROR ' + name + ': ' + e); return 0 } }))
	}
	sub('cliffs', ctx => wgpCliffs(ctx.source.server))
	sub('dims', ctx => wgpDims(ctx.source.server))
	event.register(root)
})
