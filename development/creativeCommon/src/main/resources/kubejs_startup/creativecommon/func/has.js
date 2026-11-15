// priority: 2000
// global.has(...modsOrIds): true when every argument's mod is loaded (plan_cb_publish.md M2, rule G3).
// An argument is a mod id ('alexsmobs'), a namespaced id or dict value ('alexsmobs:bear_dust', i.bear_dust), a tag
// ('#alexsmobs:x'), or an array of those. undefined or null (an unset dict key) counts as absent. The tag-only
// namespaces c, forge and minecraft always count as loaded.
//   if (global.has('alexsmobs')) { ... }            // rows that need Alex's Mobs
//   if (global.has(i.bear_dust, i.emu_egg)) { ... }  // resolve by the ids themselves, not the folder name
// First of Creative Common's startup files (priority 2000), so the dicts and every later pack can call it at top level.

global.has = function () {
	for (var k = 0; k < arguments.length; k++) {
		var a = arguments[k]
		if (Array.isArray(a)) {
			if (!global.has.apply(null, a)) return false
			continue
		}
		var ns = global.has.namespace(a)
		if (ns === null || !global.has.mod(ns)) return false
	}
	return true
}

// Namespace (mod id) of one argument, or null for undefined / empty.
global.has.namespace = function (a) {
	if (a === undefined || a === null) return null
	var s = String(a)
	// '#tag' and '@mod' (KubeJS's whole-mod entry) name their mod after the sigil; without the '@' strip the
	// tag helper's absent-mod filter dropped every '@mod' entry (BlockRunner camouflage lost Framed Blocks)
	if (s.charAt(0) === '#' || s.charAt(0) === '@') s = s.substring(1)
	if (s === '') return null
	var colon = s.indexOf(':')
	return colon < 0 ? s : s.substring(0, colon)
}

global.has._always = { c: true, forge: true, minecraft: true }
global.has._cache = {}

// Data namespaces that differ from the owning mod's id (plan_bugfix_1114.md 5.1, SQ-DCM-02): without these the
// worldgen datapack mods below read as absent and their tags and recipes were skipped. Namespace -> mod id, read from
// each jar's mods.toml and data/assets folders (2026-11-14). Add a row when a jar's namespace is not its mod id.
global.has._modOf = {
	'towns_and_towers': 't_and_t',
	'hopo': 'hopour',
	'unwrecked_ships': 'unwrecked_ships_plus',
	'yuushya': 'yuushya_modelling',
	'cozys-improved-cats': 'cozys_improved_cats',
	'rare-ice': 'rare_ice'
}

// Whether the mod owning one namespace is loaded (cached: the mod list does not change after startup). A namespace
// that is itself a mod id needs no row in _modOf.
global.has.mod = function (ns) {
	if (global.has._always[ns]) return true
	var hit = global.has._cache[ns]
	if (hit === undefined) {
		var owner = global.has._modOf[ns]
		hit = !!Platform.isLoaded(ns) || (owner !== undefined && !!Platform.isLoaded(owner))
		global.has._cache[ns] = hit
	}
	return hit
}
