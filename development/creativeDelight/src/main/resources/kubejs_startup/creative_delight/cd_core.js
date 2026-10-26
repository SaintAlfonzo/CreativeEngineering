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
	apiVersion: 1,
	modId: 'creative_delight',
	// Lowest global.NCH.apiVersion CD's scripts need (1: withNamespace).
	needsNchApi: 1,

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

if (!global.NCH || !(global.NCH.apiVersion >= global.CD.needsNchApi)) {
	console.error('[creative_delight] global.NCH apiVersion ' + (global.NCH ? global.NCH.apiVersion : 'missing') +
		', need ' + global.CD.needsNchApi + ': install the patcherMod built with this Creative Delight jar')
}
console.info('[creative_delight] core finished, members: ' + global.CD.members().join(', '))
