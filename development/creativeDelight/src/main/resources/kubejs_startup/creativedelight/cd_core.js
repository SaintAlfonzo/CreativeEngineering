// priority: 1000
// Creative Delight core: global.CD (creative_delight.md 10.6, 8.1). Startup script in the CD jar, loaded by
// patcherMod's startup loader after the pack's startup_scripts folder, so global.NCH, global.tags and the dicts
// already exist here. Every other CD script reads global.CD.
//
//   global.CD.recipes(() => { R.shapeless(...) })   every recipe id the NCH helpers write inside is
//                                                  creativedelight:<type>/<suffix> (Q24)
//   global.CD.on('oceansdelight')                  mod loaded and, for a nested member, its [members] switch on (a
//                                                  port not nested yet has no switch: loaded = on); read it inside
//                                                  an event callback (the config loads after startup scripts)
//   global.CD.on('cookingforblockheads')           a compartment (also 'incubation'): its mods loaded and its
//                                                  [compartments] switch on (read at startup, so safe at top level)
//   global.CD.qol('compost')                       [qol] switch on (creative_delight.md 8); same timing rule
//   global.CD.worldgen('wildCrops')                [worldgen] switch on (creative_delight.md 24); same timing rule
//   global.CD.members()                            the nested members' mod ids
console.info('[creativedelight] core starting')

var $CDConfig = Java.loadClass('com.SaintAlfonzo.creativedelight.CDConfig')

global.CD = {
	modId: 'creativedelight',

	members() {
		// Array.from on a Java List throws in this Rhino (JavaIteratorWrapper); copy it by hand.
		let ids = []
		$CDConfig.members().forEach(id => ids.push(String(id)))
		return ids
	},

	on(member) {
		// The optional compartments (plan_cb_publish.md 3b): their mods loaded and their [compartments] switch on at startup
		if ($CDConfig.isCompartment(member)) return !!$CDConfig.compartment(member)
		if (!Platform.isLoaded(member)) return false
		return !$CDConfig.isMember(member) || !!$CDConfig.member(member)
	},

	qol(id) {
		return !!$CDConfig.qol(id)
	},

	worldgen(id) {
		return !!$CDConfig.worldgen(id)
	},

	recipes(fn) {
		return global.NCH.withNamespace(global.CD.modId, fn)
	},

	// Generated data (creative_delight.md 28.2, framework #7): a generator writes one script per member,
	// <member>/<name>.js headed `// requires: <member>`, that calls genPut(name, member, data). Startup parts
	// (can_types) are put once at startup, server parts (food_types, placeable, mortar_catalog) on every load.
	_gen: {},

	genPut(name, member, data) {
		var g = global.CD._gen
		if (!g[name]) g[name] = {}
		g[name][member] = data
	},

	// Every member's part merged in member order: arrays are joined, objects merged one level down (an array
	// under a key is joined too), so { types: { 'raw/fruit': [...] } } parts become one map of joined lists.
	genMerge(name) {
		var g = global.CD._gen[name] || {}
		var out = {}
		Object.keys(g).sort().forEach(member => {
			var part = g[member]
			Object.keys(part).forEach(field => {
				var v = part[field]
				if (Array.isArray(v)) { out[field] = (out[field] || []).concat(v); return }
				if (v === null || typeof v !== 'object') { out[field] = v; return }
				if (!out[field]) out[field] = {}
				Object.keys(v).forEach(k => {
					var x = v[k]
					out[field][k] = Array.isArray(x) ? (out[field][k] || []).concat(x) : x
				})
			})
		})
		return out
	},

	genMembers(name) {
		return Object.keys(global.CD._gen[name] || {}).sort()
	},
}

// The dial sections that moved from CEConfig at the CD carve ([cooking], [canning], [crop_drops], [crop_growth],
// [stem_fruit], [dandelion_maturing]; creative_delight.md 34) are CDConfig's: NCH.cfg finds them by section name.
global.NCH.registerConfig(global.CD.modId, 'com.SaintAlfonzo.creativedelight.CDConfig')

console.info('[creativedelight] core finished, members: ' + global.CD.members().join(', '))
