// requires: creativeequipment
// Creative Equipment (plan_cb_polish.md 1, 2026-11-06): JEED effect providers for the gear, moved from Creative
// Brewing's jeed_providers.js with the gear rows. Reads the frameworks (global.armorEffects / global.toolEffects,
// Creative Common) after every jar's startup rows are in; recipe ids are creativeequipment:jeed_provider/... One
// provider recipe per effect from this jar; Creative Brewing writes its own for the other sources, and JEED shows both.

ServerEvents.recipes(e => {
global.CEQ.recipes(() => {
	var i = global.item
	var R = global.NCH

	var providers = {}
	var add = (effect, items) => {
		var list = providers[effect] || (providers[effect] = [])
		var incoming = Array.isArray(items) ? items : [items]
		incoming.forEach(item => {
			var key = typeof item === 'string' ? item : JSON.stringify(item)
			if (!list.some(x => (typeof x === 'string' ? x : JSON.stringify(x)) === key)) list.push(item)
		})
	}

	//######################### Pack gear (armor and tool effect frameworks) #########################
	var addEffects = (effects, items) => (effects || []).forEach(spec => add(String(spec.effect), items))
	var A = global.armorEffects, T = global.toolEffects
	if (A) {
		Object.keys(A.PIECES).forEach(id => addEffects(A.PIECES[id], id))
		A.SETS.forEach(set => addEffects(set.effects, set.ids))
		Object.keys(A.MOUNTS).forEach(id => addEffects(A.MOUNTS[id], id))
	}
	if (T) {
		Object.keys(T.ON_HIT).forEach(id => addEffects(T.ON_HIT[id], id))
		Object.keys(T.HELD).forEach(id => addEffects(T.HELD[id], id))
	}
	// Echo armor: CDD's own set bonus, redirected by Equipment's EchoArmorEffectProcedureMixin (memory echo-tier-gear)
	var echoArmor = [i.echo_helmet, i.echo_chestplate, i.echo_leggings, i.echo_boots]
	add('minecraft:night_vision', echoArmor)
	add('better_beacons:phantom_bane', echoArmor)

	// NCH drops a provider or an effect of an absent mod by itself (G5)
	Object.keys(providers).sort().forEach(effect => R.jeedProvider(effect, providers[effect], e, 'gear_' + String(effect).replace(':', '_')))
})
})
