// requires: creativedevtools
// /dumpbiomes  ->  writes kubejs/exported/biome_data.json
// per biome: which dimensions it can generate in, temperature, downfall, precipitation

ServerEvents.commandRegistry(event => {
	const { commands: Commands } = event

	event.register(
		Commands.literal('dumpbiomes')
			.requires(src => src.hasPermission(2))
			.executes(ctx => {
				const n = dumpBiomes(ctx.source.getServer())
				ctx.source.sendSuccess(Text.green(`Dumped ${n} biomes to kubejs/exported/biome_data.json`), false)
				return 1
			})
	)
})

function dumpBiomes(server) {
	const Registry = Java.loadClass('net.minecraft.core.Registry')
	const biomeReg = server.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY)

	// biome id -> [dimension ids]  (from each loaded dimension's biome source)
	const biomeDims = {}
	server.getAllLevels().forEach(level => {
		// level.getDimension(), not level.dimension(): KubeJS kjs$getDimension() turns `dimension` into a
		// bean property (a ResourceLocation) that shadows the vanilla method -> "not a function"
		const dim = String(level.getDimension())
		let possible
		try {
			possible = level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes()
		} catch (err) {
			console.warn(`[dumpbiomes] no biome source for ${dim}: ${err}`)
			return
		}
		possible.forEach(holder => {
			let bid
			try { bid = String(biomeReg.getKey(holder.value())) } catch (err) { return }
			if (!bid) return
			const list = biomeDims[bid] || (biomeDims[bid] = [])
			if (list.indexOf(dim) === -1) list.push(dim)
		})
	})

	const out = {}
	const ids = []
	biomeReg.keySet().forEach(rl => ids.push(rl.toString()))
	ids.sort()

	ids.forEach(id => {
		const biome = biomeReg.get(Utils.id(id))
		if (!biome) return

		let temperature = null, downfall = null, precipitation = 'UNKNOWN'
		try { temperature   = biome.getBaseTemperature() }      catch (err) {}
		try { downfall      = biome.getDownfall() }             catch (err) {}
		try { precipitation = String(biome.getPrecipitation()) } catch (err) {}

		out[id] = {
			dimensions: (biomeDims[id] || []).sort(),
			temperature: temperature,
			downfall: downfall,
			precipitation: precipitation,          // NONE | RAIN | SNOW
			snows: temperature != null && temperature < 0.15,
			arid:  downfall != null && downfall < 0.3
		}
	})

	writeExportJson('kubejs/exported/biome_data.json', out)
	console.log(`[dumpbiomes] wrote ${Object.keys(out).length} biomes`)
	return Object.keys(out).length
}
