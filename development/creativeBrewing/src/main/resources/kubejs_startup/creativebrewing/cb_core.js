// priority: 1000
// Creative Brewing core: global.CB (feature_writeup/creative_brewing.md 2.4). Startup script in the CB jar, loaded by
// patcherMod's startup loader after the pack's startup_scripts folder and after Creative Delight's pack (CB's mods.toml
// orders it AFTER creativedelight), so global.NCH, global.tags, the dicts and global.CD already exist here. Every
// other CB script reads global.CB.
//
//   global.CB.recipes(() => { R.shapeless(...) })   every recipe id the NCH helpers write inside is
//                                                  creativebrewing:<type>/<suffix>
//   global.CB.on('vinery')                         mod loaded and, for a nested member, its [members] switch on; read it
//                                                  inside an event callback (the config loads after startup scripts)
//   global.CB.feature('<id>')                      a [brewing] / [alcohol] / [effects] switch on; same timing rule
//   global.CB.members()                            the nested members' mod ids
console.info('[creativebrewing] core starting')

var $CBConfig = Java.loadClass('com.SaintAlfonzo.creativebrewing.CBConfig')

global.CB = {
	modId: 'creativebrewing',

	members() {
		// Array.from on a Java List throws in this Rhino (JavaIteratorWrapper); copy it by hand.
		let ids = []
		$CBConfig.members().forEach(id => ids.push(String(id)))
		return ids
	},

	on(member) {
		if (!Platform.isLoaded(member)) return false
		return !$CBConfig.isMember(member) || !!$CBConfig.member(member)
	},

	feature(id) {
		return !!$CBConfig.feature(id)
	},

	recipes(fn) {
		return global.NCH.withNamespace(global.CB.modId, fn)
	},
}

if (!global.CD) console.error('[creativebrewing] global.CD missing: Creative Delight\'s startup pack did not run first')
console.info('[creativebrewing] core finished, members: ' + global.CB.members().join(', '))
