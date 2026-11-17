// priority: 1000
// Creative Worldgen core: global.CWG (feature_writeup/creative_split.md 3.9, wave 5). Startup script in the Worldgen jar,
// loaded by Creative Common's startup loader after the pack's startup_scripts folder, so global.NCH, global.tags and the
// dicts already exist here. The server scripts are in kubejs/data/creativeworldgen/kubejs/ (_worldgen_core.js).
//
//   global.CWG.on('moremooblooms')     mod loaded; read it inside an event callback
//   global.CWG.recipes(() => { ... })   every recipe id the NCH helpers write inside is creativeworldgen:<type>/<suffix>
//                                       (no Worldgen script writes recipes yet)
// Loot rules go to Creative Loot's engine: global.LOOT.rules(L => { ... }) (kubejs_startup/creativeloot/loot_core.js).
console.info('[creativeworldgen] core starting')

global.CWG = {
	modId: 'creativeworldgen',

	on(member) {
		return Platform.isLoaded(member)
	},

	recipes(fn) {
		return global.NCH.withNamespace(global.CWG.modId, fn)
	},
}

// The config sections that moved from CEConfig at the Worldgen carve are CWGConfig's: NCH.cfg finds them by section name.
global.NCH.registerConfig(global.CWG.modId, 'com.SaintAlfonzo.creativeworldgen.CWGConfig')

console.info('[creativeworldgen] core finished')
