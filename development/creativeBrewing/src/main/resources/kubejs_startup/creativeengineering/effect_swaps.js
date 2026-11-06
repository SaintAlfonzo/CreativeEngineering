// Moved from the pack's startup_scripts/cooking/food_values.js 2026-11-01 (creative_brewing.md 4.5).
// Startup script in the CB jar: runs after the pack folder and CD; CB rebuild + restart.
// Food effect changes (effect swaps, Farmer's Respite cake slice effects). The food value copies
// (global.foodValueCopies) moved to Creative Delight 2026-10-25: applier creativeengineering/food_value_copies.js,
// rows in delightful/delightful_foods.js and minecraft/food_values.js (CD's kubejs_startup/). These swaps now run
// after CD's food edits; none of those touch the swapped items (checked 2026-11-01).
// ItemEvents.modification runs at startup: restart to apply.

// Effect swaps: on each food, the `from` effects go and `to` takes the first one's duration and level. Other effects,
// hunger and flags stay.
//   Rustic Delight's coffees: Speed + Haste -> Caffeinated (Farmer's Respite's), the pack's only coffee family since
//   Respite's coffee merged into it (item_merges.js). Coffee 1:30, milk 1:00, honey / chocolate 0:45, dark II 0:45;
//   syrup / pumpkin / cherry blossom 0:45 like the other bean + milk + flavour coffees (owner 2026-11-01, TODO
//   creativedelight/members #11; the durations are set in the CD Rustic member's RDFoods).
//   Neapolitan's Agility -> Vinery's Climbing, same durations (potions/effects #9). The banana cake blocks and anything
//   else that gives Agility are swapped when applied by Creative Brewing ([effects] remap); this fixes the tooltips.
var ef = global.effect
global.foodEffectSwaps = []
if (global.has('farmersrespite')) {
	global.foodEffectSwaps.push({ items: ['coffee', 'milk_coffee', 'honey_coffee', 'chocolate_coffee', 'dark_coffee',
		'syrup_coffee', 'pumpkin_coffee', 'cherry_blossom_coffee'],
		from: [ef.speed, ef.haste], to: ef.caffeinated })
}
if (global.has('neapolitan')) {
	global.foodEffectSwaps.push({ items: ['neapolitan_banana', 'banana_bread', 'dried_banana', 'banana_ice_cream',
		'banana_milkshake', 'strawberry_banana_smoothie', 'adzuki_curry'],
		from: [ef.agility], to: ef.climbing })
}

ItemEvents.modification(e => {
	var i = global.item
	var $SwapRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	var $SwapRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
	var done = 0
	global.foodEffectSwaps.forEach(swap => {
		var fromEffects = swap.from.map(id => $SwapRegistries.MOB_EFFECTS.getValue(new $SwapRL(id)))
		swap.items.forEach(key => {
			var id = i[key]
			var item = typeof id === 'string' ? $SwapRegistries.ITEMS.getValue(new $SwapRL(id)) : null
			var food = item ? item.getFoodProperties() : null
			if (!food) { console.warn('[food values] effect swap: ' + key + ' is not a food'); return }
			var first = null
			food.getEffects().forEach(pair => {
				var inst = pair.getFirst()
				if (!first && inst.getEffect().equals(fromEffects[0])) first = inst
			})
			if (!first) { console.warn('[food values] effect swap: ' + id + ' has no ' + swap.from[0]); return }
			var ticks = first.getDuration()
			var amp = first.getAmplifier()
			e.modify(id, it => it.setFoodProperties(fb => {
				fromEffects.forEach(fx => fb.removeEffect(fx))
				fb.effect(swap.to, ticks, amp, 1.0)
			}))
			done++
		})
	})
	console.log('[food values] ' + done + ' effect swaps')
})

// Farmer's Respite 2.1's cake slice effects on the installed 2.0 (feature_writeup/farmers_respite.md 2.3): Slice of
// Coffee Cake loses Speed and its Caffeinated goes from 20 s to 30 s; Rose Hip Pie Slice loses Speed. Hunger and
// saturation unchanged.
ItemEvents.modification(e => {
	if (!global.has('farmersrespite')) return
	var i = global.item
	var $SliceRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	var $SliceRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
	var effect = id => $SliceRegistries.MOB_EFFECTS.getValue(new $SliceRL(id))
	e.modify(i.coffee_cake_slice, it => it.setFoodProperties(fb => {
		fb.removeEffect(effect('minecraft:speed'))
		fb.removeEffect(effect('farmersrespite:caffeinated'))
		fb.effect('farmersrespite:caffeinated', 600, 0, 1.0)
	}))
	e.modify(i.rose_hip_pie_slice, it => it.setFoodProperties(fb => fb.removeEffect(effect('minecraft:speed'))))
})
