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
// Startup values: restart to apply.

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

// The food values are absolute rows of Creative Delight's applier (creativedelight/food_value_copies.js, V-36): hunger
// and saturation set, the drink's own effects kept but Nausea, then Intoxication (hidden) unless it has one, then
// Nausea unless it has Tipsy. Rows merge into an existing row of the same drink (Fruits Delight's bellini_cocktail).
global.alcohol.drinks.forEach(key => {
	var A = global.alcohol
	var effects = [{ effect: 'brewinandchewin:intoxication', ticks: A.intoxicationTicks, visible: false,
		unlessHas: ['brewinandchewin:intoxication'] }]
	if (A.ownDrunkEffect.indexOf(key) < 0) {
		effects.push({ effect: global.effect.nausea, ticks: A.mixed.indexOf(key) >= 0 ? A.mixedNauseaTicks : A.nauseaTicks,
			unlessHas: ['brewinandchewin:tipsy'] })
	}
	global.foodValueCopies.absolute(key, {
		nutrition: A.hunger, saturationMod: 0, alwaysEat: true,
		keepEffects: true, dropEffects: [global.effect.nausea], effects: effects,
		requireEffects: ['brewinandchewin:intoxication', 'brewinandchewin:tipsy'],
	})
})

// The count line the tests read: drinks whose food now carries Intoxication. Runs after the applier (this jar's
// startup pack loads after Creative Delight's).
ItemEvents.modification(e => {
	var A = global.alcohol
	var $AlcoholRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	var $AlcoholRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
	var intox = global.effect.resolve('brewinandchewin:intoxication')
	var done = 0
	A.drinks.forEach(key => {
		var id = global.item[key]
		if (typeof id !== 'string') { console.warn('[alcohol] ' + key + ' has no item_dict key'); return }
		var item = $AlcoholRegistries.ITEMS.getValue(new $AlcoholRL(id))
		var food = item ? item.getFoodProperties() : null
		if (!food || !intox) return
		var fx = food.getEffects()
		for (var n = 0; n < fx.size(); n++) {
			if (fx.get(n).getFirst().getEffect().equals(intox)) { done++; return }
		}
	})
	console.log('[alcohol] food values set on ' + done + ' of ' + A.drinks.length + ' drinks')
})
