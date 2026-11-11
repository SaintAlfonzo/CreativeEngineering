// priority: -100
// requires: farmersdelight
// Comfort / Nourishment split (feature_writeup/comfort_nourishment.md section 9 and appendix C). Creative Delight
// startup script; priority -100 so it runs after every other food rebuild in this pack. Restart to apply.
//
// The rules are in tools/gen_comfort_nourishment.py and its output, generation_scripts/food/comfort_nourishment.json
// (/cdevgen run comfort_nourishment). Edit the rules and rerun the tool; don't edit the JSON.
//   set    bowls, mason jars, shulker bowls and copper cup meals (120 s under 10 hunger, 240 s from 10 up, Comfort
//          at 7 or less 60 s), golden foods (Nourishment 120 s, enchanted 240 s) and kept Comfort at 7 or less
//          (60 s): both effects go, the listed one is added ("none" = neither)
//   keep   ruled foods with no recipe found: left as they are (hand check, appendix B.2)
//   strip  bottles: both effects go
//   every other food loses Nourishment and keeps Comfort. Cans (creativeengineering, canned_goods) are skipped:
//   tools/gen_can_types.py gives them their source food's result.
// Only Comfort and Nourishment change; other effects, hunger and flags stay.

ItemEvents.modification(e => {
	var i = global.item
	var path = 'kubejs/generation_scripts/food/comfort_nourishment.json'
	var data = null
	// The instance file, else the copy shipped in the Creative Delight jar (global.genText); skip with a warning if neither.
	var raw = global.genText(path)
	try { data = raw == null ? null : JSON.parse(String(raw)) } catch (err) {
		console.error('[comfort_nourishment] could not read ' + path + ': ' + err)
		return
	}
	if (data == null) { console.warn('[comfort_nourishment] ' + path + ' not found - Comfort / Nourishment left as upstream'); return }
	var $CNRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	var $CNRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
	var COMFORT = 'farmersdelight:comfort'
	var NOURISHMENT = 'farmersdelight:nourishment'
	var comfort = $CNRegistries.MOB_EFFECTS.getValue(new $CNRL(COMFORT))
	var nourishment = $CNRegistries.MOB_EFFECTS.getValue(new $CNRL(NOURISHMENT))
	var idOf = entry => entry.indexOf(':') >= 0 ? entry : i[entry]
	var unknown = []
	var handled = {}

	var has = (food, effect) => {
		var found = false
		food.getEffects().forEach(pair => { if (pair.getFirst().getEffect().equals(effect)) found = true })
		return found
	}
	var foodOf = id => {
		var item = $CNRegistries.ITEMS.getValue(new $CNRL(id))
		return item ? item.getFoodProperties() : null
	}

	var set = 0
	Object.keys(data.set).forEach(entry => {
		var id = idOf(entry)
		if (typeof id !== 'string') { unknown.push(entry); return }
		handled[id] = true
		if (!foodOf(id)) return
		var rule = data.set[entry]
		e.modify(id, it => it.setFoodProperties(fb => {
			fb.removeEffect(comfort)
			fb.removeEffect(nourishment)
			if (rule.effect !== 'none') fb.effect('farmersdelight:' + rule.effect, rule.seconds * 20, 0, 1.0)
		}))
		set++
	})
	data.keep.forEach(entry => {
		var id = idOf(entry)
		if (typeof id === 'string') handled[id] = true
		else unknown.push(entry)
	})

	var stripped = 0
	data.strip.forEach(entry => {
		var id = idOf(entry)
		if (typeof id !== 'string') { unknown.push(entry); return }
		handled[id] = true
		var food = foodOf(id)
		if (!food || !(has(food, comfort) || has(food, nourishment))) return
		e.modify(id, it => it.setFoodProperties(fb => {
			fb.removeEffect(comfort)
			fb.removeEffect(nourishment)
		}))
		stripped++
	})

	// every other food: Nourishment removed
	var others = []
	$CNRegistries.ITEMS.getKeys().forEach(rl => {
		var id = String(rl)
		var ns = id.split(':')[0]
		if (handled[id] || ns === 'creativedelight' || ns === 'canned_goods') return
		var food = foodOf(id)
		if (food && has(food, nourishment)) others.push(id)
	})
	others.forEach(id => e.modify(id, it => it.setFoodProperties(fb => fb.removeEffect(nourishment))))

	console.info('[comfort_nourishment] ' + set + ' ruled foods set, ' + stripped + ' bottles stripped, '
		+ others.length + ' other foods lost Nourishment')
	if (unknown.length) console.warn('[comfort_nourishment] no item_dict key for: ' + unknown.join(', ')
		+ ' - rerun tools/gen_comfort_nourishment.py')
})
