// requires: creativedevtools
// /dumpstructuregen  ->  kubejs/exported/worldgen/structuregen.json
//
// Full generation values for every structure_set + structure the LIVE server knows about
// (post-merge: vanilla + every mod's + every datapack's overrides, exactly as this world
// will actually generate them) — not just the id lists /dumpstructures gives you.
//
// Per structure_set: placement type, salt, frequency, spacing/separation (random_spread) or
// distance/spread/count (concentric_rings), and locate_offset (the [x,y,z] nudge every
// placement type supports — this is the "xyz coords" knob).
// Per structure: resolved biome list (+ the tag it came from, if tag-backed), generation step,
// terrain adaptation.
//
// Verification tool for the structure framework (kubejs/generation_scripts/worldgen/, applied by
// kubejs/server_scripts/worldgen/structure_overrides.js): compare these live values with
// structure_tag_report.md. The editable config's baseline comes from tools/gen_structure_config.py
// (jar scan), not from this dump.
//
// Every accessor below is wrapped individually so one unexpected/renamed method never blanks
// the whole dump — see rhino-fieldandmethods-gotcha in project memory: some real methods are
// invisible to Rhino's reflection outright, so failures here are expected to be silent misses,
// not crashes. Missing fields show up as "_errors" entries per id; check the log.

(() => {

	const $Registry = Java.loadClass('net.minecraft.core.Registry')
	const $PlacementInfo = Java.loadClass('com.SaintAlfonzo.creativeengineering.worldgen.PlacementInfo')

	function safe(fn, onError) {
		try { return fn() } catch (e) { onError && onError(e); return undefined }
	}

	function toArray(javaIterable) {
		const out = []
		const it = javaIterable.iterator()
		while (it.hasNext()) out.push(it.next())
		return out
	}

	// Holder<Biome> -> "namespace:path", via unwrapKey() so this works whether the holder is
	// backed by a registry reference or a direct value.
	function biomeId(holder) {
		return safe(() => {
			const key = holder.unwrapKey()
			if (!key.isPresent()) return null
			return String(key.get().location().toString())
		})
	}

	// HolderSet<Biome> -> { tag: "ns:tag" | null, resolved: ["ns:biome", ...] }
	function dumpBiomeSet(holderSet) {
		const tag = safe(() => {
			const key = holderSet.unwrapKey()
			return key.isPresent() ? String(key.get().location().toString()) : null
		})
		const resolved = safe(() => {
			const ids = []
			const it = holderSet.iterator()
			while (it.hasNext()) {
				var id = biomeId(it.next())   // var: a loop-body const is stuck on its first value in this Rhino
				if (id) ids.push(id)
			}
			return ids.sort()
		}, () => []) || []
		return { tag: tag || null, resolved: resolved }
	}

	// StructurePlacement -> plain object mirroring the datapack "placement" block.
	function dumpPlacement(placement, errors) {
		const className = safe(() => String(placement.getClass().getSimpleName())) || 'unknown'
		const out = { placement_type: className }

		// locateOffset/frequencyReductionMethod/frequency/salt/exclusionZone are protected: Rhino can't call them,
		// so patcherMod's worldgen.PlacementInfo reads the fields.
		const P = $PlacementInfo

		out.locate_offset = safe(() => {
			var offset = P.locateOffset(placement)
			return [offset[0], offset[1], offset[2]]
		}, e => errors.push('locate_offset: ' + e)) || [0, 0, 0]

		out.frequency_reduction_method = safe(
			() => String(P.frequencyReductionMethod(placement)),
			e => errors.push('frequency_reduction_method: ' + e)
		)

		out.frequency = safe(() => {
			const f = P.frequency(placement)
			return f == null ? undefined : Number(f)
		}, e => errors.push('frequency: ' + e))
		if (out.frequency === 1) delete out.frequency // datapack omits the field when it's 1.0 (no reduction)

		out.salt = safe(() => Number(P.salt(placement)), e => errors.push('salt: ' + e))

		// var + unique names: a block const here hit "redeclaration of var" in this Rhino
		var exclusionText = safe(() => P.exclusionZone(placement), e => errors.push('exclusion_zone: ' + e))
		if (exclusionText) {
			var exclusionParts = String(exclusionText).split(' ')
			out.exclusion_zone = { other_set: exclusionParts[0], chunk_count: Number(exclusionParts[1]) }
		}

		if (className === 'RandomSpreadStructurePlacement') {
			out.spacing = safe(() => Number(placement.spacing()), e => errors.push('spacing: ' + e))
			out.separation = safe(() => Number(placement.separation()), e => errors.push('separation: ' + e))
			out.spread_type = safe(() => String(placement.spreadType().name()).toLowerCase(), e => errors.push('spread_type: ' + e))
		} else if (className === 'ConcentricRingsStructurePlacement') {
			out.distance = safe(() => Number(placement.distance()), e => errors.push('distance: ' + e))
			out.spread = safe(() => Number(placement.spread()), e => errors.push('spread: ' + e))
			out.count = safe(() => Number(placement.count()), e => errors.push('count: ' + e))
			out.preferred_biomes = safe(() => dumpBiomeSet(placement.preferredBiomes()), e => errors.push('preferred_biomes: ' + e))
		}

		return out
	}

	function dumpStructureSets(registryAccess) {
		const reg = registryAccess.registryOrThrow($Registry.STRUCTURE_SET_REGISTRY)
		const out = {}
		toArray(reg.keySet()).forEach(rl => {
			const id = String(rl.toString())
			const value = reg.get(rl)
			const errors = []
			const entry = { id: id }

			entry.placement = safe(() => dumpPlacement(value.placement(), errors), e => errors.push('placement: ' + e))

			entry.structures = safe(() => {
				return toArray(value.structures()).map(sel => {
					const structureId = safe(() => {
						const key = sel.structure().unwrapKey()
						return key.isPresent() ? String(key.get().location().toString()) : null
					})
					const weight = safe(() => Number(sel.weight()))
					return { structure: structureId, weight: weight }
				})
			}, e => errors.push('structures: ' + e)) || []

			if (errors.length) entry._errors = errors
			out[id] = entry
		})
		return out
	}

	function dumpStructures(registryAccess) {
		const reg = registryAccess.registryOrThrow($Registry.STRUCTURE_REGISTRY)
		const out = {}
		toArray(reg.keySet()).forEach(rl => {
			const id = String(rl.toString())
			const value = reg.get(rl)
			const errors = []
			const entry = { id: id }

			entry.biomes = safe(() => dumpBiomeSet(value.biomes()), e => errors.push('biomes: ' + e))
			entry.step = safe(() => String(value.step().name()).toLowerCase(), e => errors.push('step: ' + e))
			entry.terrain_adaptation = safe(() => String(value.terrainAdaptation().name()).toLowerCase(), e => errors.push('terrain_adaptation: ' + e))

			if (errors.length) entry._errors = errors
			out[id] = entry
		})
		return out
	}

	function dumpStructureGen(source) {
		const server = source.getServer()
		const registryAccess = server.registryAccess()

		const structureSets = dumpStructureSets(registryAccess)
		const structures = dumpStructures(registryAccess)

		const errorIds = Object.keys(structureSets).filter(k => structureSets[k]._errors)
			.concat(Object.keys(structures).filter(k => structures[k]._errors))

		const result = {
			generated: new Date().toISOString(),
			counts: {
				structure_sets: Object.keys(structureSets).length,
				structures: Object.keys(structures).length,
				entries_with_errors: errorIds.length,
			},
			structure_sets: structureSets,
			structures: structures,
		}

		writeExportJson('kubejs/exported/worldgen/structuregen.json', result)

		const msg = `Dumped ${result.counts.structure_sets} structure_sets + ${result.counts.structures} structures `
			+ `(${errorIds.length} with field errors — check log) -> kubejs/exported/worldgen/structuregen.json`
		source.sendSuccess(Text.green(msg), false)
		console.info(msg)
		if (errorIds.length) console.warn('[dumpstructuregen] entries with field errors: ' + errorIds.join(', '))
		return 1
	}

	ServerEvents.commandRegistry(event => {
		const Commands = Java.loadClass('net.minecraft.commands.Commands')
		event.register(
			Commands.literal('dumpstructuregen')
				.requires(src => src.hasPermission(2))
				.executes(ctx => dumpStructureGen(ctx.source))
		)
	})

})()
