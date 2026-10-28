// priority: 1000
// Creative Delight core: global.CD (creative_delight.md 10.6, 8.1). Startup script in the CD jar, loaded by
// patcherMod's startup loader after the pack's startup_scripts folder, so global.NCH, global.tags and the dicts
// already exist here. Every other CD script reads global.CD.
//
//   global.CD.recipes(() => { R.shapeless(...) })   every recipe id the NCH helpers write inside is
//                                                  creative_delight:<type>/<suffix> (Q24)
//   global.CD.on('oceansdelight')                  mod loaded and, for a nested member, its [members] switch on (a
//                                                  port not nested yet has no switch: loaded = on); read it inside
//                                                  an event callback (the config loads after startup scripts)
//   global.CD.qol('compost')                       [qol] switch on (creative_delight.md 8); same timing rule
//   global.CD.worldgen('wildCrops')                [worldgen] switch on (creative_delight.md 24); same timing rule
//   global.CD.members()                            the nested members' mod ids
console.info('[creative_delight] core starting')

var $CDConfig = Java.loadClass('com.SaintAlfonzo.creativedelight.CDConfig')

global.CD = {
	modId: 'creative_delight',

	members() {
		// Array.from on a Java List throws in this Rhino (JavaIteratorWrapper); copy it by hand.
		let ids = []
		$CDConfig.members().forEach(id => ids.push(String(id)))
		return ids
	},

	on(member) {
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
}

console.info('[creative_delight] core finished, members: ' + global.CD.members().join(', '))
