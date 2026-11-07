// priority: -50
// requires: goldenfood
// Golden and gilded food values (food.md 3.16, golden_food.md 3.6; TODO food/types-and-tuning #3, owner 2026-11-02).
// A golden food's hunger is its input food's times the golden multiplier, a gilded (enchanted golden) food's times the
// gilded multiplier, capped at maxHunger; the saturation modifier stays the input's, so saturation points scale with
// hunger. Multipliers: kubejs/generation_scripts/food/food_tuning.json `golden`. Effects, flags (always edible) and
// stack sizes are kept. Replaces upstream's flat 4 / 1.2 (golden) and 8 / 8.0 (enchanted).
// Not here: the golden and gilded milk buckets (milk is no food; as upstream their values never apply) and the two
// cakes (block slices, GoldenCakeBlock). Creative Delight startup script, loaded from the jar after the pack's
// startup_scripts; priority -50 so the input foods' own changes are in first (comfort_nourishment.js, -100, only
// touches Comfort / Nourishment). Restart to apply.
ItemEvents.modification(e => {
	var i = global.item
	var path = 'kubejs/generation_scripts/food/food_tuning.json'
	var cfg = null
	// The instance file, else the copy shipped in the Creative Delight jar (global.genText); skip with a warning if neither.
	var raw = global.genText(path)
	var json = null
	try { json = raw == null ? null : JSON.parse(String(raw)) } catch (err) {
		console.error('[golden values] could not read ' + path + ': ' + err)
		return
	}
	if (json == null) { console.warn('[golden values] ' + path + ' not found - golden and gilded values left as upstream'); return }
	cfg = json.golden
	if (!cfg) { console.error('[golden values] ' + path + ' has no golden section'); return }
	var $GoldenRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	var $GoldenRL = Java.loadClass('net.minecraft.resources.ResourceLocation')

	// item_dict key of the input food -> the golden suffix of goldenfood's ids (golden_<s>, enchanted_golden_<s>)
	var INPUTS = {
		apple: 'apple', minecraft_carrot: 'carrot', melon: 'melon_slice', sweet_berries: 'sweet_berries',
		glow_berries: 'glow_berries', chorus_fruit: 'chorus_fruit', baked_potato: 'baked_potato',
		minecraft_beetroot: 'beetroot', dried_kelp: 'dried_kelp', beef_cooked: 'cooked_beef',
		porkchop_cooked: 'cooked_porkchop', mutton_cooked: 'cooked_mutton', chicken_cooked: 'cooked_chicken',
		rabbit_cooked: 'cooked_rabbit', cod_cooked: 'cooked_cod', salmon_cooked: 'cooked_salmon',
		minecraft_bread: 'bread', cookie: 'cookie', pumpkin_pie: 'pumpkin_pie', mushroom_stew: 'mushroom_stew',
		beetroot_soup: 'beetroot_soup', rabbit_stew: 'rabbit_stew', honey_bottle: 'honey_bottle',
	}
	var done = 0
	var apply = (inputId, targetKey, mult) => {
		var id = i[targetKey]
		if (typeof id !== 'string') { console.warn('[golden values] ' + targetKey + ' is not in item_dict'); return }
		e.modify(id, item => {
			var src = $GoldenRegistries.ITEMS.getValue(new $GoldenRL(inputId))
			var food = src ? src.getFoodProperties() : null
			if (!food) { console.warn('[golden values] ' + inputId + ' is not a food'); return }
			var hunger = Math.min(cfg.maxHunger, Math.round(food.getNutrition() * mult))
			var modifier = food.getSaturationModifier()
			item.setFoodProperties(fb => { fb.hunger(hunger); fb.saturation(modifier) })
		})
		done++
	}
	// Golden foods of other mods: item_dict key of the input food -> item_dict key of the golden food (golden multiplier
	// only). Golden Roll on the Rice Roll (plan_meals_rolls_bbq.md B3: 20 / 0.6); its slice keeps the member's 10 / 0.5.
	var PAIRS = {
		rice_roll: 'golden_roll',
	}
	Object.keys(PAIRS).forEach(inKey => {
		var inputId = i[inKey]
		if (typeof inputId !== 'string') { console.warn('[golden values] ' + inKey + ' is not in item_dict'); return }
		if (!Platform.isLoaded(String(inputId).split(':')[0])) return
		apply(inputId, PAIRS[inKey], cfg.golden)
	})
	Object.keys(INPUTS).forEach(inKey => {
		var inputId = i[inKey]
		if (typeof inputId !== 'string') { console.warn('[golden values] ' + inKey + ' is not in item_dict'); return }
		apply(inputId, 'golden_' + INPUTS[inKey], cfg.golden)
		apply(inputId, 'enchanted_golden_' + INPUTS[inKey], cfg.gilded)
	})
	console.log('[golden values] ' + done + ' golden and gilded foods queued (x' + cfg.golden + ', x' + cfg.gilded + ', hunger cap ' + cfg.maxHunger + ')')
})
