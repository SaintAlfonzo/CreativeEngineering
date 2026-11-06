// priority: 1000
// Creative Worldgen core: global.CWG (feature_writeup/creative_split.md 3.9, wave 5). Startup script in the Worldgen jar,
// loaded by patcherMod's startup loader after the pack's startup_scripts folder, so global.NCH, global.tags and the
// dicts already exist here. The server scripts are in kubejs/data/creativeworldgen/kubejs/ (_worldgen_core.js).
//
//   global.CWG.on('more_mooblooms')     mod loaded; read it inside an event callback
//   global.CWG.recipes(() => { ... })   every recipe id the NCH helpers write inside is creativeworldgen:<type>/<suffix>
//                                       (no Worldgen script writes recipes yet)
//   global.CWG.lootRules(L => { ... })  queue chest loot rules for the loot engine (loot/05_tuning.js, global.lootTuning).
//                                       For scripts in other packs (Creative Delight's loot.js files): script packs load
//                                       in namespace hash order, so such a pack can load before 05_tuning.js has made
//                                       the current reload's engine. 05_tuning.js runs the queue in highPriorityData,
//                                       before the tag event that captures '#tag' members, and empties it.
console.info('[creativeworldgen] core starting')

global.CWG = {
	modId: 'creativeworldgen',

	on(member) {
		return Platform.isLoaded(member)
	},

	recipes(fn) {
		return global.NCH.withNamespace(global.CWG.modId, fn)
	},

	_lootQueue: [],

	lootRules(fn) {
		global.CWG._lootQueue.push(fn)
	},
}

// The config sections that moved from CEConfig at the Worldgen carve are CWGConfig's: NCH.cfg finds them by section name.
global.NCH.registerConfig(global.CWG.modId, 'com.SaintAlfonzo.creativeworldgen.CWGConfig')

console.info('[creativeworldgen] core finished')
