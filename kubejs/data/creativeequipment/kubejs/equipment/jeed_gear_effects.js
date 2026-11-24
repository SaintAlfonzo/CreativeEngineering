// requires: creativeequipment
// requires: jeed
// Creative Equipment. Recipe ids are creativeequipment:<type>/<suffix> (global.CEQ.recipes). JEED effect providers for
// effects that only gear gives (effect-text #1): the effect's JEI page lists every armor piece whose set or piece row
// in gear_effects.js (Equipment startup) grants it. The rows are read from global.armorEffects at load, so a gear row
// added or removed there shows up here with no second list. JEED's own providers (food, potions) and Creative Brewing's
// jeed_providers.js cover the other sources.
ServerEvents.recipes(e => {
if (!global.CEQ || !global.armorEffects) return
global.CEQ.recipes(() => {
	var R = global.NCH.forPack('creativeequipment')
	var ef = global.effect

	// Effects whose gear sources go on their JEED page. Magic Resistance has no potion, food or mob source.
	var EFFECTS = [ef.magic_resistance]

	EFFECTS.forEach(effect => {
		var items = []
		var push = id => { if (id && items.indexOf(String(id)) < 0) items.push(String(id)) }
		var grants = list => (list || []).some(spec => String(spec.effect) === String(effect))
		global.armorEffects.SETS.forEach(set => { if (grants(set.effects)) set.ids.forEach(push) })
		Object.keys(global.armorEffects.PIECES).forEach(id => { if (grants(global.armorEffects.PIECES[id])) push(id) })
		if (items.length) R.jeedProvider(effect, items, e)
	})
})
})
