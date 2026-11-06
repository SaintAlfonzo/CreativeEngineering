// priority: 1000
// Creative Magic core: global.CMG (feature_writeup/creative_magic.md, creative_split.md 3.9, wave 6). Startup script in
// the Magic jar, loaded by patcherMod's startup loader after the pack's startup_scripts folder, so global.NCH,
// global.tags and the dicts already exist here. The server scripts are in kubejs/data/creativemagic/kubejs/
// (_magic_core.js), the client scripts in kubejs/assets/creativemagic/kubejs/.
//
//   global.CMG.on('botania')            mod loaded; read it inside an event callback
//   global.CMG.recipes(() => { ... })   every recipe id the NCH helpers write inside is creativemagic:<type>/<suffix>
console.info('[creativemagic] core starting')

global.CMG = {
	modId: 'creativemagic',

	on(member) {
		return Platform.isLoaded(member)
	},

	recipes(fn) {
		return global.NCH.withNamespace(global.CMG.modId, fn)
	},
}

// [botania] moved from CEConfig at the Magic carve and is CMGConfig's: NCH.cfg finds it by section name.
global.NCH.registerConfig(global.CMG.modId, 'com.SaintAlfonzo.creativemagic.CMGConfig')

console.info('[creativemagic] core finished')
