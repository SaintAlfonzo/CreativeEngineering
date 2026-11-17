// Creative Delight startup script, loaded from the jar after the pack's startup_scripts, so the dicts exist here.
//
// Food values copied from one food onto another: target key -> source key (item_dict). The target takes the source's
// whole FoodProperties (hunger, saturation, effects, meat/fast/always flags), so anything the target had of its own is
// dropped. `{ from: key, effects: false }` copies everything but the effects; a non-food target becomes edible.
// Absolute rows set values instead of copying them (plan_rules_fixes.md section 12, V-36; food_tuning.md):
//   global.foodValueCopies.ghost_charcoal = { absolute: { nutrition: 0, saturationMod: 0, alwaysEat: true,
//       effects: [{ effect: 'wither', ticks: 100 }, { effect: 'fire_resistance', ticks: 3600 }] } }
// Fields of `absolute`: nutrition, saturationMod (the modifier), meat, alwaysEat, fast, effects, keepEffects,
// dropEffects, dedupeEffects, requireEffects. A field left out keeps the target's own value (no food: 0 and false).
// `effects` is a list of { effect: id or global.effect key, ticks, amplifier (default 0), chance (default 1),
// visible (default true; false also makes it non-ambient and hides the particles), unlessHas: [effect, ...] (skip it
// when the list so far holds one of these) }, added after the target's own effects when keepEffects is true (the
// default when effects is left out), else in their place. dropEffects removes those effects from the target's own
// list; dedupeEffects then keeps the longest entry of each effect, in the original order. requireEffects: when one of
// these effects is not registered the row is skipped and the target keeps its food.
// Two files may set fields of one row (Creative Brewing's alcohol_drinks.js adds to Fruits Delight's bellini_cocktail):
// write rows with global.foodValueCopies.absolute(key, fields), which merges into an existing absolute row.
// ItemEvents.modification runs at startup: restart to apply.
// This file is the one applier. The rows live in the file of the mod that owns the target item, each adding to
// global.foodValueCopies (created with `global.foodValueCopies = global.foodValueCopies || {}`):
//   delightful/delightful_foods.js   cantaloupe_slice; absolute: the 3.8.1 hunger and saturation (delightfulFoodValues)
//   fruitsdelight/fruits_foods.js    absolute: bellini_cocktail (dedupeEffects)
//   vintagedelight/vintage_items.js  absolute: ghost_charcoal
//   Creative Brewing creativebrewing/alcohol_drinks.js  absolute: every alcoholic drink (global.alcohol)
//   minecraft/food_values.js         glistering_melon_slice
// The effect swaps and Farmer's Respite cake slice effects: Creative Brewing's kubejs_startup/creativeengineering/
// effect_swaps.js (moved from the pack's cooking/food_values.js).
global.foodValueCopies = global.foodValueCopies || {}
Object.defineProperty(global.foodValueCopies, 'absolute', {
	enumerable: false,
	value: (key, fields) => {
		var row = global.foodValueCopies[key]
		if (!row || typeof row !== 'object' || !row.absolute) row = global.foodValueCopies[key] = { absolute: {} }
		Object.keys(fields).forEach(f => { row.absolute[f] = fields[f] })
		return row
	},
})

ItemEvents.modification(e => {
	var i = global.item
	var $FoodValuesRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	var $FoodValuesRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
	var $FoodValuesBuilder = Java.loadClass('net.minecraft.world.food.FoodProperties$Builder')
	var done = 0
	var $FoodValuesEffect = Java.loadClass('net.minecraft.world.effect.MobEffectInstance')
	var same = (inst, ids) => (ids || []).some(x => {
		var me = global.effect.resolve(x)
		return me && inst.getEffect().equals(me)
	})
	var absolute = (key, id, spec) => {
		var missing = (spec.requireEffects || []).filter(x => !global.effect.resolve(x))
		if (missing.length) {
			console.error('[food values] ' + key + ': ' + missing.join(', ') + ' not registered, food left unchanged')
			return false
		}
		e.modify(id, item => {
			var old = item.getFoodProperties()
			var pick = (field, fromOld, none) => spec[field] !== undefined ? spec[field] : (old ? fromOld() : none)
			var b = new $FoodValuesBuilder()
				.nutrition(pick('nutrition', () => old.getNutrition(), 0))
				.saturationMod(pick('saturationMod', () => old.getSaturationModifier(), 0))
			if (pick('meat', () => old.isMeat(), false)) b.meat()
			if (pick('alwaysEat', () => old.canAlwaysEat(), false)) b.alwaysEat()
			if (pick('fast', () => old.isFastFood(), false)) b.fast()
			var pairs = []
			var keep = spec.keepEffects !== undefined ? spec.keepEffects : spec.effects === undefined
			if (keep && old) {
				var own = old.getEffects()
				for (var n = 0; n < own.size(); n++) {
					if (!same(own.get(n).getFirst(), spec.dropEffects)) pairs.push({ inst: own.get(n).getFirst(), chance: own.get(n).getSecond() })
				}
			}
			if (spec.dedupeEffects) {
				var longest = []
				pairs.forEach(p => {
					var same = -1
					for (var k = 0; k < longest.length; k++) if (longest[k].inst.getEffect().equals(p.inst.getEffect())) same = k
					if (same < 0) longest.push(p)
					else if (p.inst.getDuration() > longest[same].inst.getDuration()) longest[same] = p
				})
				pairs = longest
			}
			;(spec.effects || []).forEach(fx => {
				if (fx.unlessHas && pairs.some(p => same(p.inst, fx.unlessHas))) return
				var me = global.effect.resolve(fx.effect)
				if (!me) { console.warn('[food values] ' + key + ': effect ' + fx.effect + ' skipped'); return }
				var inst = fx.visible === false
					? new $FoodValuesEffect(me, fx.ticks, fx.amplifier || 0, false, false)
					: new $FoodValuesEffect(me, fx.ticks, fx.amplifier || 0)
				pairs.push({ inst: inst, chance: fx.chance === undefined ? 1.0 : fx.chance })
			})
			pairs.forEach(p => b.effect(p.inst, p.chance))
			item.setFoodProperties(b.build())
		})
	}
	Object.keys(global.foodValueCopies).forEach(key => {
		var entry = global.foodValueCopies[key]
		if (entry && typeof entry === 'object' && entry.absolute) {
			var target = i[key]
			if (typeof target !== 'string') { console.warn('[food values] ' + key + ': not in item_dict'); return }
			try {
				if (absolute(key, target, entry.absolute) !== false) done++
			} catch (err) {
				console.error('[food values] could not set food on ' + target + ': ' + err)
			}
			return
		}
		var srcKey = typeof entry === 'string' ? entry : entry.from
		var keepEffects = typeof entry === 'string' || entry.effects !== false
		var id = i[key]
		var srcId = i[srcKey]
		if (typeof id !== 'string' || typeof srcId !== 'string') {
			console.warn('[food values] ' + key + ' <- ' + srcKey + ': not in item_dict')
			return
		}
		var src = $FoodValuesRegistries.ITEMS.getValue(new $FoodValuesRL(srcId))
		var food = src ? src.getFoodProperties() : null
		if (!food) { console.warn('[food values] ' + srcId + ' is not a food'); return }
		if (!keepEffects) {
			var b = new $FoodValuesBuilder().nutrition(food.getNutrition()).saturationMod(food.getSaturationModifier())
			if (food.isMeat()) b.meat()
			if (food.canAlwaysEat()) b.alwaysEat()
			if (food.isFastFood()) b.fast()
			food = b.build()
		}
		try {
			e.modify(id, item => item.setFoodProperties(food))
			done++
		} catch (err) {
			console.error('[food values] could not set food on ' + id + ': ' + err)
		}
	})
	console.log('[food values] ' + done + ' foods copied or set')
})
