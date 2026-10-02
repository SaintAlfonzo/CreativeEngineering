// /dumpclimate [columns] [radius]  ->  kubejs/generation_scripts/worldgen/climate/<ns>__<dim>.json.gz
//
// The samples ship with the pack (BiomeLayoutTuner reads them whenever a world is created), so they're
// stored small: compact JSON, one point per line, integer climate values (the game quantizes to x10000
// longs anyway), gzipped. About 0.2 MB for all three dimensions instead of 3 MB pretty-printed.
// Read one with `zcat <file> | less`. With a pre-1.0.3 patcherMod jar (no WorldgenFiles.writeGzipText)
// it falls back to the old uncompressed kubejs/exported/worldgen/climate/<ns>__<dim>.json, which that
// jar's tuner reads.
//
// Climate-noise sample set for every multi_noise dimension, the data source for the biome
// frequency tuner (patcherMod BiomeLayoutTuner via kubejs/server_scripts/worldgen/biome_overrides.js,
// which picks up a new dump on the next /reload or world load). The tuner needs
// to know how often each climate value actually occurs to turn "make X twice as common" into
// offset numbers, and those noise distributions come from the noise router (Terralith +
// Tectonic's density functions here), which only the running game evaluates faithfully.
//
// Pure noise evaluation: getBaseHeight / sampler.sample / getNoiseBiome never load or generate
// chunks, so this is safe to run anywhere at any radius -- it just costs server-thread time
// (~a few seconds for the default 3000 columns).
//
// Per column (random, deterministic positions within +-radius of 0,0):
//   surface layer     - the climate at the noise-estimated surface height (for the Nether, which
//                       has a roof, a few fixed y levels instead; for End void columns, y=64)
//   underground layer - 24/48/80/128 blocks below that surface, where there is terrain
// Each point also records the biome the LIVE biome source picks there, so the tuner can check its
// own nearest-entry model against the game ("model_agreement_pct" in biome_config.json). The
// climate itself doesn't depend on the biome layout at all, only on the seed + noise settings --
// one dump stays valid after you retune biomes.

(() => {

	const $Types = Java.loadClass('net.minecraft.world.level.levelgen.Heightmap$Types')
	const $Random = Java.loadClass('java.util.Random')
	const $WorldgenFiles = Java.loadClass('com.SaintAlfonzo.creativeengineering.worldgen.WorldgenFiles')

	// JSON.stringify is Gson-backed and prints every number as a double ("675.0"); the points are
	// joined by hand so the integers stay integers. Array.join uses JS number formatting.
	function compactJson(header, points) {
		const head = Object.keys(header).map(k => JSON.stringify(k) + ':' + JSON.stringify(header[k]))
		return '{' + head.join(',') + ',"points":[\n' + points.map(p => '[' + p.join(',') + ']').join(',\n') + '\n]}\n'
	}

	function safe(fn) {
		try { return fn() } catch (e) { return undefined }
	}

	function dumpLevel(level, columns, radius) {
		const dimId = String(level.getDimension())   // not level.dimension(): see dump_biome.js
		const chunkSource = level.getChunkSource()
		const generator = chunkSource.getGenerator()
		const biomeSource = generator.getBiomeSource()
		// Blueprint (Team Abnormals) wraps the overworld/End source in its ModdedBiomeSource (saved as
		// "blueprint:modded" in level.dat), which hides the multi_noise original - it's private, no getter.
		// The climate itself comes from randomState.sampler(), not the biome source, so sampling through
		// the wrapper is fine; only live_biome can differ (Blueprint slice biomes), which just lowers
		// model_agreement_pct. Wrapping a non-multi_noise source (e.g. vanilla End) gives unused samples.
		const sourceClass = String(biomeSource.getClass().getName())
		if (!/\.(MultiNoiseBiomeSource|ModdedBiomeSource)$/.test(sourceClass)) {
			console.info(`[dumpclimate] skipping ${dimId}: biome source ${sourceClass} is not multi_noise`)
			return null
		}

		const randomState = chunkSource.randomState()
		const sampler = randomState.sampler()
		const minY = level.getMinBuildHeight()
		const hasCeiling = !!level.dimensionType().hasCeiling()

		const biomeIndex = {}
		const biomes = []
		const points = []
		let heightFailures = 0

		function record(layer, x, y, z) {
			const qx = x >> 2, qy = y >> 2, qz = z >> 2
			const tp = sampler.sample(qx, qy, qz)
			const holder = biomeSource.getNoiseBiome(qx, qy, qz, sampler)
			const key = holder.unwrapKey()
			const id = key.isPresent() ? String(key.get().location().toString()) : '?'
			if (!(id in biomeIndex)) { biomeIndex[id] = biomes.length; biomes.push(id) }
			points.push([layer,
				Number(tp.temperature()), Number(tp.humidity()), Number(tp.continentalness()),
				Number(tp.erosion()), Number(tp.depth()), Number(tp.weirdness()),
				biomeIndex[id]])
		}

		const random = new $Random(45326) // fixed seed so repeated dumps sample the same columns
		for (let i = 0; i < columns; i++) {
			var x = Math.floor((random.nextDouble() * 2 - 1) * radius)
			var z = Math.floor((random.nextDouble() * 2 - 1) * radius)
			if (hasCeiling) {
				;[16, 40, 64, 88, 112].forEach(y => record(0, x, y, z))
				continue
			}
			var h = safe(() => Number(generator.getBaseHeight(x, z, $Types.WORLD_SURFACE_WG, level, randomState)))
			if (h === undefined) heightFailures++
			if (h === undefined || h <= minY + 32) {
				record(0, x, 64, z)
				continue
			}
			record(0, x, h, z)
			;[24, 48, 80, 128].forEach(dy => { if (h - dy >= minY + 4) record(1, x, h - dy, z) })
		}

		const [ns, path] = dimId.split(':')
		const name = `${ns}__${path.replace(/\//g, '__')}.json`
		const header = {
			dimension: dimId,
			biome_source: sourceClass,
			seed: String(level.getSeed()),
			generated: new Date().toISOString(),
			columns: columns,
			radius: radius,
			layers: ['surface', 'underground'],
			fields: ['layer', 'temperature', 'humidity', 'continentalness', 'erosion', 'depth', 'weirdness', 'live_biome'],
			biomes: biomes,
		}
		var out
		// Rhino throws, rather than returning undefined, for a static member the class doesn't have
		if (safe(() => typeof $WorldgenFiles.writeGzipText === 'function')) {
			out = `kubejs/generation_scripts/worldgen/climate/${name}.gz`
			$WorldgenFiles.writeGzipText(out, compactJson(header, points))
		} else {
			out = `kubejs/exported/worldgen/climate/${name}`
			header.points = points
			writeExportJson(out, header)
		}
		if (heightFailures) console.warn(`[dumpclimate] ${dimId}: getBaseHeight failed on ${heightFailures} columns (sampled at y=64 instead)`)
		return { dim: dimId, points: points.length, biomes: biomes.length, out: out }
	}

	function run(source, columns, radius) {
		const started = Date.now()
		const results = []
		const it = source.getServer().getAllLevels().iterator()
		while (it.hasNext()) {
			var level = it.next()   // var, not const: Rhino re-declares a loop-body const each pass -> "redeclaration of var"
			try {
				var r = dumpLevel(level, columns, radius)
				if (r) results.push(r)
			} catch (e) {
				var msg = `[dumpclimate] ${level.getDimension()} failed: ${e}`
				console.error(msg)
				source.sendFailure(Text.red(msg))
			}
		}
		const secs = ((Date.now() - started) / 1000).toFixed(1)
		results.forEach(r => {
			const msg = `${r.dim}: ${r.points} points, ${r.biomes} live biomes -> ${r.out}`
			source.sendSuccess(Text.green(msg), false)
			console.info('[dumpclimate] ' + msg)
		})
		if (!results.length) {
			source.sendFailure(Text.red('dumpclimate: no multi_noise dimension found - see the log for each skipped biome source'))
			return 0
		}
		source.sendSuccess(Text.gray(`dumpclimate done in ${secs}s (${columns} columns, radius ${radius})`), false)
		return results.length
	}

	ServerEvents.commandRegistry(event => {
		const Commands = Java.loadClass('net.minecraft.commands.Commands')
		const IntArg = Java.loadClass('com.mojang.brigadier.arguments.IntegerArgumentType')
		event.register(
			Commands.literal('dumpclimate')
				.requires(src => src.hasPermission(2))
				.executes(ctx => run(ctx.source, 3000, 20000))
				.then(Commands.argument('columns', IntArg.integer(100, 100000))
					.executes(ctx => run(ctx.source, IntArg.getInteger(ctx, 'columns'), 20000))
					.then(Commands.argument('radius', IntArg.integer(500, 1000000))
						.executes(ctx => run(ctx.source, IntArg.getInteger(ctx, 'columns'), IntArg.getInteger(ctx, 'radius')))))
		)
	})

})()
