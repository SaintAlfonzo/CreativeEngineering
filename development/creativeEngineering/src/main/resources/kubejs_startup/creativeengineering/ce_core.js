// priority: 1000
// Creative Engineering core: global.CEN (feature_writeup/creative_engineering.md, creative_split.md 3.9, wave 8).
// Startup scripts in the Engineering jar, loaded by Creative Common's startup loader after the pack's startup_scripts
// folder, so global.NCH, global.tags and the dicts already exist here. This pack also holds the engineering items made
// in KubeJS (items/: components, coils, plates, dust piles, misc parts, wood pellets, amethyst dust and lens) and the
// sequenced assembly transition items (changes/transition_items.js). The server scripts are in
// kubejs/data/creativeengineering/kubejs/ (_engineering_core.js).
//
//   global.CEN.on('immersiveengineering')  mod loaded; read it inside an event callback
//   global.CEN.recipes(() => { ... })      recipe ids written inside are creativeengineering:<type>/<suffix>; that is
//                                          NCH's default namespace, so the moved recipe files need no wrapper
console.info('[creativeengineering] core starting')

global.CEN = {
	modId: 'creativeengineering',

	on(member) {
		return Platform.isLoaded(member)
	},

	recipes(fn) {
		return global.NCH.withNamespace(global.CEN.modId, fn)
	},
}

// [shrapnel], [surface] and [vein_undo] are this jar's CEConfig (creativeengineering-common.toml); every other table
// NCH.cfg reads is Creative Common's CommonConfig or another carved jar's class.
global.NCH.registerConfig(global.CEN.modId, 'com.SaintAlfonzo.creativeengineering.CEConfig')

console.info('[creativeengineering] core finished')
