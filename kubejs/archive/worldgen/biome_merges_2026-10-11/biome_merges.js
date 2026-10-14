// Structure biome-pool merges: adds modded biomes to the biome tags structures spawn in.
//
// Edit kubejs/generation_scripts/worldgen/biome_merges.json ("tags" -> tag id -> "add": biome
// ids), then /reload or rejoin. Additive only: the mod's own tag contents are kept. Only chunks
// generated afterwards are affected. The "proposals" / "structures" / "also_affects" fields are
// read-only notes (also_affects = other structures that reference the same tag, so they gain
// these biomes too).
//
// The tags handler can't tell whether a biome exists: KubeJS runs it once at script load as a
// "pre" pass (PreTagEventJS: add() only queues the action, totalAdded never moves, no registry
// yet) and replays the queued adds on the real tag load without calling this script again. So
// the check runs on ServerEvents.loaded instead, against the live biome registry + final tags:
// every entry is reported as missing (biome not registered, e.g. a typo) or not applied.
//
// CEConfig [worldgen.systems] biomeMerges = false skips every merge (the mods' own tags only); read when the
// scripts load, so /reload after changing it.
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {

	var MERGES_PATH = 'kubejs/generation_scripts/worldgen/biome_merges.json'
	var TAG = '[biome-merges] '

	var $Registry = Java.loadClass('net.minecraft.core.Registry')
	var $TagKey = Java.loadClass('net.minecraft.tags.TagKey')
	var $ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')

	function mergesEnabled() {
		try {
			return Java.loadClass('com.SaintAlfonzo.creativeengineering.worldgen.WorldgenConfig').biomeMergesEnabled() == true
		} catch (err) {
			return true   // older jar: no switch
		}
	}

	function readMerges() {
		if (!mergesEnabled()) {
			console.info(TAG + 'off in CEConfig [worldgen.systems] biomeMerges: structures spawn in their mods\' own biome tags')
			return null
		}
		try {
			return JSON.parse(String(JsonIO.readString(MERGES_PATH))).tags
		} catch (err) {
			console.error(TAG + 'could not read ' + MERGES_PATH + ': ' + err)
			return null
		}
	}

	function idSet(iterable, toId) {
		var out = {}
		var it = iterable.iterator()
		while (it.hasNext()) {
			var id = toId(it.next())
			if (id) out[id] = true
		}
		return out
	}

	function holderId(holder) {
		var key = holder.unwrapKey()
		return key.isPresent() ? String(key.get().location().toString()) : null
	}

	ServerEvents.tags('worldgen/biome', function (event) {
		var merges = readMerges()
		if (!merges) return
		Object.keys(merges).forEach(function (tagId) {
			global.tags.add(event, tagId, merges[tagId].add || [])
		})
	})

	ServerEvents.loaded(function (event) {
		var merges = readMerges()
		if (!merges) return
		var reg
		var known
		try {
			reg = event.server.registryAccess().registryOrThrow($Registry.BIOME_REGISTRY)
			known = idSet(reg.keySet(), function (rl) { return String(rl.toString()) })
		} catch (err) {
			console.error(TAG + 'could not read the biome registry, merges unverified: ' + err)
			return
		}
		var added = 0
		var missing = []
		var notApplied = []
		Object.keys(merges).forEach(function (tagId) {
			var members
			try {
				members = idSet(reg.getTagOrEmpty($TagKey.create($Registry.BIOME_REGISTRY, new $ResourceLocation(tagId))), holderId)
			} catch (err) {
				console.error(TAG + 'could not read #' + tagId + ': ' + err)
				members = {}
			}
			(merges[tagId].add || []).forEach(function (biome) {
				if (!known[biome]) missing.push(biome + ' -> #' + tagId)
				else if (!members[biome]) notApplied.push(biome + ' -> #' + tagId)
				else added++
			})
		})
		missing.forEach(function (m) { console.warn(TAG + 'no such biome, skipped: ' + m) })
		notApplied.forEach(function (m) { console.error(TAG + 'biome exists but is not in the tag: ' + m) })
		console.info(TAG + added + ' biomes merged across ' + Object.keys(merges).length + ' tags'
			+ (missing.length ? ', ' + missing.length + ' unknown' : '')
			+ (notApplied.length ? ', ' + notApplied.length + ' NOT applied' : ''))
	})
})()
