// Moved from the pack's startup_scripts/cooking/alcohol_drinks.js 2026-11-01 (creative_brewing.md 4.5).
// Startup script in the CB jar: runs after the pack folder and CD; CB rebuild + restart.
// Alcoholic drinks (alcohol.md 11). Every drink listed here:
//   - fills half a shank (hunger 1) and gives no saturation; AppleSkin draws the shank rotten, since Intoxication is
//     a harmful effect
//   - can be drunk at any time (alwaysEat)
//   - gives Brewin' and Chewin's Intoxication (no saturation from food while it lasts), hidden like Brewin's own.
//     Brewin's drinks keep their own duration; the rest get intoxicationTicks
//   - gives Nausea unless it has a Tipsy / drunk effect already: nauseaTicks, or mixedNauseaTicks for drinks made from
//     other alcoholic drinks. A Tipsy food effect is found on its own; ownDrunkEffect lists drinks whose drunk effect
//     is applied outside their food values
// Farmer's Delight / Farmer's Respite apple ciders aren't alcoholic and stay as they are.
// Vinery wines only get the food side with the rebuilt port -03 (DrinkBlockItem eats instead of shrinking).
// Quark's saturation word on these drinks reads "Intoxicating Drink" (Creative Brewing alcohol.IntoxicatingDrinkTooltip picks
// the drinks by their Intoxication food effect, so this list is the single source).
// ItemEvents.modification runs at startup: restart to apply.

global.alcohol = {
	hunger: 1,
	intoxicationTicks: 90 * 20,     // Beer's
	nauseaTicks: 5 * 20,
	mixedNauseaTicks: 10 * 20,
	drinks: [
		// Brewin' and Chewin' (all Tipsy)
		'beer', 'vodka', 'brewinandchewin_mead', 'rice_wine', 'pale_jane', 'egg_grog', 'glittering_grenadine',
		'saccharine_rum', 'salty_folly', 'bloody_mary', 'red_rum', 'strongroot_ale', 'steel_toe_stout', 'dread_nog',
		'withering_dross', 'kombucha',
		// Vinery
		'apple_cider', 'apple_wine', 'glowing_wine', 'solaris_wine', 'kelp_cider', 'eiswein', 'aegis_wine',
		'villagers_fright', 'clark_wine', 'jellie_wine', 'noir_wine', 'red_wine', 'strad_wine', 'cherry_wine',
		'cristel_wine', 'lilitu_wine', 'jo_special_mixture', 'bolvar_wine', 'magnetic_wine', 'stal_wine', 'chenet_wine',
		'bottle_mojang_noir', 'chorus_wine', 'creepers_crush', 'mellohi_wine', 'praetorian_wine',
		// Fruits Delight, Engineer's Delight
		'bellini_cocktail', 'bottle_of_wodka',
		// Hearth and Harvest (hearth_and_harvest.md D16). Root Beer is a soft drink and stays out.
		'blueberry_wine', 'raspberry_wine', 'sweet_berry_wine', 'glow_berry_wine', 'melon_wine', 'lemon_wine', 'moonshine',
	],
	// fermented from other wines: Jellie (Apple, Chenet, Bolvar), Mojang Noir (Red Wine)
	mixed: ['jellie_wine', 'bottle_mojang_noir'],
	// Bottle of Wodka gives Nausea 60 s from its own code
	ownDrunkEffect: ['bottle_of_wodka'],
}

ItemEvents.modification(e => {
	var i = global.item
	var A = global.alcohol
	var $AlcoholFoodBuilder = Java.loadClass('net.minecraft.world.food.FoodProperties$Builder')
	var $AlcoholEffectInstance = Java.loadClass('net.minecraft.world.effect.MobEffectInstance')
	var $AlcoholRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	var $AlcoholRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
	var mobEffect = id => $AlcoholRegistries.MOB_EFFECTS.getValue(new $AlcoholRL(id))
	var INTOXICATION = mobEffect('brewinandchewin:intoxication')
	var TIPSY = mobEffect('brewinandchewin:tipsy')
	var NAUSEA = mobEffect('minecraft:nausea')
	if (!INTOXICATION || !TIPSY) {
		console.error('[alcohol] Brewin\' and Chewin\' effects missing, drinks left unchanged')
		return
	}

	var done = 0
	A.drinks.forEach(key => {
		var id = i[key]
		if (typeof id !== 'string') { console.warn('[alcohol] ' + key + ' has no item_dict key'); return }
		try {
			e.modify(id, item => {
				var old = item.getFoodProperties()
				var food = new $AlcoholFoodBuilder().nutrition(A.hunger).saturationMod(0).alwaysEat()
				if (old && old.isMeat()) food.meat()
				if (old && old.isFastFood()) food.fast()
				var hasIntox = false
				var drunk = A.ownDrunkEffect.indexOf(key) >= 0
				var effects = old ? old.getEffects() : null
				for (var n = 0; effects && n < effects.size(); n++) {
					var pair = effects.get(n)
					var inst = pair.getFirst()
					var effect = inst.getEffect()
					if (effect.equals(INTOXICATION)) hasIntox = true
					if (effect.equals(TIPSY)) drunk = true
					// Nausea is set below (Bellini's own 5 s would otherwise stack with it)
					if (!effect.equals(NAUSEA)) food.effect(inst, pair.getSecond())
				}
				if (!hasIntox) food.effect(new $AlcoholEffectInstance(INTOXICATION, A.intoxicationTicks, 0, false, false), 1.0)
				if (!drunk) {
					var ticks = A.mixed.indexOf(key) >= 0 ? A.mixedNauseaTicks : A.nauseaTicks
					food.effect(new $AlcoholEffectInstance(NAUSEA, ticks, 0), 1.0)
				}
				item.setFoodProperties(food.build())
				done++
			})
		} catch (err) {
			console.error('[alcohol] could not set food on ' + id + ': ' + err)
		}
	})
	console.log('[alcohol] food values set on ' + done + ' of ' + A.drinks.length + ' drinks')
})
