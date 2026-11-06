// priority: 1000
// requires: creativedevtools
// Creative Devtools core: global.CDV (feature_writeup/creative_devtools.md 1). Startup script in the Devtools jar,
// loaded by patcherMod's startup loader after the pack's startup_scripts folder (Devtools' mods.toml orders it AFTER
// creativeengineering), so global.NCH, the dicts and global.removedItems already exist here. Every other Devtools
// script reads global.CDV.
//
//   global.CDV.recipes(() => { R.createMechanicalCrafting(...) })   every recipe id the NCH helpers write inside is
//                                                                  creativedevtools:<type>/<suffix>
//   global.CDV.on('create')              mod loaded (Devtools has no members with switches; the nested perf bot and
//                                        capture mods are inert unless their system property is set)
//   global.CDV.feature('<id>')           a [features] switch of creativedevtools-common.toml; read it inside an event
//                                        callback (the config loads after startup scripts)
//   global.CDV.recipeScanAtLoad()        [debug.recipe_scan] runAtLoad
console.info('[creativedevtools] core starting')

var $DevtoolsConfig = Java.loadClass('com.SaintAlfonzo.creativedevtools.DevtoolsConfig')

global.CDV = {
	modId: 'creativedevtools',

	on(modId) {
		return Platform.isLoaded(modId)
	},

	feature(id) {
		return !!$DevtoolsConfig.feature(id)
	},

	recipeScanAtLoad() {
		return !!$DevtoolsConfig.recipeScanAtLoad()
	},

	recipes(fn) {
		return global.NCH.withNamespace(global.CDV.modId, fn)
	},
}

console.info('[creativedevtools] core finished')
