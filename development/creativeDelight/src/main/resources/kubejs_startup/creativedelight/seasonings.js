// Moved from the pack's startup_scripts/cooking/seasonings.js 2026-10-25, rows unchanged.
// Creative Delight startup script, loaded from the jar after the pack's startup_scripts, so the dicts exist here.
// Restart to apply.
//
// Pack seasonings (patcherMod food/Seasonings): a seasoning goes on a food as NBT (Seasonings:[...]) by a
// creativedelight:seasoning craft (global.NCH.season, kubejs/data/creativedelight/kubejs/creativedelight/seasoning.js), and eating the food adds
// the seasoning's bonus on top of the food's own. Salt (the Salt mod's Salted) is separate and not listed here.
//
// key: {
//   id         seasoning id (NBT value, lang key seasoning.<ns>.<path>)
//   name       display name (assets/creativedelight/kubejs/creativedelight/seasoning_lang.js)
//   item       item_dict key value of what you put in the grid (item or tag)
//   nutrition  hunger points added                     (optional)
//   saturation saturation points added, capped at food (optional)
//   heal       health points healed, 2 = one heart     (optional)
//   effects    [{ effect, duration (ticks), amplifier }] (optional)
//   Kinds from the Barbeque's Delight fold (food/seasoning #11), all optional:
//   fast         true: eaten fast (16 ticks)
//   alwaysEdible true: edible when full
//   damage       health points of damage on eating; damageType 'fire' (default), 'magic' or 'generic'
//   transform    { chance, duration, amplifier }: each of the food's own effects gets chance x chance, duration x
//                duration, amplifier + amplifier. fast, alwaysEdible and transform reach a food only if its item asks
//                food/Seasonings.foodProperties (the Barbeque's Delight skewers and combos do)
//   station      true: put on by a station only (the Grill's grilled), never crafted; counts toward the cap only
//                with creativedelight-common.toml [food] grilledCountsTowardMax
//   combinesAll  true: shares a food with any seasoning
//   The cap on seasonings per food is creativedelight-common.toml [food] maxSeasonings (default 2).
//   combines   keys of seasonings that may share a food with this one; either side listing the other is enough.
//              Unlisted pairs don't combine.
//   Which foods take it (the tag #<ns>:seasonable/<path>, built by kubejs/data/creativedelight/kubejs/creativedelight/seasoning_tags.js):
//   families   food families (paths in tools/food_families.py, e.g. 'snack', 'meat/cooked'), each with its
//              subfamilies: the bulk of the foods
//   add        item_dict values (items or tags) added after the families
//   remove     item_dict values (items) taken out last
//   A seasoning with no foods gets no craft.
//   needs      Creative Delight member (or mod) id the seasoning's item comes from; the seasoning's tag and crafts are
//              skipped while it is off (optional)
//   icon: the overlay drawn on seasoned food is creativedelight:item/seasoned/<path> (textureswapper/seasoning/make_seasoning.py)
// }
// Startup script, read on both sides: restart to apply. Values are first test values (food/seasoning #3).
var i = global.item
var ef = global.effect

global.seasoning = {
	// popcorn only for now; open them up by family later
	cheese: { id: 'creativedelight:cheese', name: 'Cheese', item: i.t_cheese_wedge, saturation: 2,
		families: [], add: global.has(i.popcorn) ? [i.popcorn] : [] },
	onion:  { id: 'creativedelight:onion',  name: 'Onion',  item: i.onion,  nutrition: 2, combines: ['garlic'],
		families: [], add: global.has(i.popcorn) ? [i.popcorn] : [] },
	garlic: { id: 'creativedelight:garlic', name: 'Garlic', item: i.garlic,
		effects: [{ effect: ef.speed, duration: 200, amplifier: 0 }],
		families: [], add: global.has(i.popcorn) ? [i.popcorn] : [] },
	// sugar or powdered sugar (not the rest of #forge:sugar: glycerol). Popcorn + sugar is Corn Delight's caramel popcorn,
	// so no foods yet (was waffles).
	sugar:  { id: 'creativedelight:sugar',  name: 'Sugar',  item: [i.sugar, i.powdered_sugar], nutrition: 1,
		families: [], add: [] },
	// Ender's Delight's crushed void pepper, mirroring Salt's bonus: +2 nutrition and a 0.1 saturation modifier
	// (config/salt-common.toml SaltingNutrition / SaltingSaturationModifier; absolute saturation = nutrition * modifier * 2).
	void_pepper: { id: 'creativedelight:void_pepper', name: 'Void Pepper', item: i.crushed_voidpepper, // G5 (needs: its tag and crafts are skipped while enders_additions is off)
		nutrition: 2, saturation: 0.4, needs: 'enders_additions', families: [], add: global.has(i.popcorn) ? [i.popcorn] : [] },

	// Barbeque's Delight (food/seasoning #11, barbeques_delight.md 4.1, seasonings.md): the six upstream seasonings,
	// values as 1.3.0. Powders go on raw and cooked BBQ skewers (a raw skewer's seasonings carry over when it is
	// grilled); sauces on cooked ones only (O28). One item per craft: a trade gives 64 powders or 16 sauces (O31).
	// No pairs among the six (upstream allowed one per skewer); the proposed pairs of seasonings.md 4 are not decided.
	// Foods by tag: the raw_skewers tag holds the 8 raw pack skewers too (dishes/meals #50, plan_meals_rolls_bbq.md C3).
	cumin: { id: 'barbequesdelight:cumin', name: 'Cumin', item: i.cumin_powder, heal: 2,
		needs: 'barbequesdelight', families: [], add: [i.t_raw_skewers, i.t_grilled_skewers] },
	pepper: { id: 'barbequesdelight:pepper', name: 'Pepper', item: i.pepper_powder, fast: true,
		needs: 'barbequesdelight', families: [], add: [i.t_raw_skewers, i.t_grilled_skewers] },
	chili: { id: 'barbequesdelight:chili', name: 'Chili', item: i.chili_powder, alwaysEdible: true, damage: 2,
		needs: 'barbequesdelight', families: [], add: [i.t_raw_skewers, i.t_grilled_skewers] },
	honey_mustard: { id: 'barbequesdelight:honey_mustard', name: 'Honey Mustard', item: i.honey_mustard_sauce,
		transform: { chance: 2, duration: 0.5 }, needs: 'barbequesdelight', families: [], add: [i.t_grilled_skewers] },
	buffalo: { id: 'barbequesdelight:buffalo', name: 'Buffalo', item: i.buffalo_sauce, damage: 2,
		transform: { duration: 0.5, amplifier: 1 }, needs: 'barbequesdelight', families: [], add: [i.t_grilled_skewers] },
	barbeque: { id: 'barbequesdelight:barbeque', name: 'Barbeque', item: i.barbeque_sauce,
		transform: { duration: 2 }, needs: 'barbequesdelight', families: [], add: [i.t_grilled_skewers] },
	// Grilled (food/seasoning #13): put on only by the Grill when a flipped raw skewer finishes (station: no craft,
	// no foods). +2 saturation points; does not count toward [food] maxSeasonings unless grilledCountsTowardMax.
	// Combines with any seasoning. Combos (wrap, sandwich, bibimbap) drop it from the skewers they hold (O35).
	grilled: { id: 'barbequesdelight:grilled', name: 'Grilled', saturation: 2, station: true, combinesAll: true,
		needs: 'barbequesdelight', families: [], add: [] },
}

;(function () {
	var $Seasonings
	try {
		$Seasonings = Java.loadClass('com.SaintAlfonzo.creativedelight.food.Seasonings')
	} catch (err) {
		console.warn('[seasoning] patcherMod Seasonings missing, seasonings not defined: ' + err)
		return
	}
	Object.keys(global.seasoning).forEach(key => {
		var s = global.seasoning[key]
		var combines = (s.combines || []).map(k => {
			if (!global.seasoning[k]) throw new Error('[seasoning] ' + key + ' combines with unknown seasoning ' + k)
			return global.seasoning[k].id
		})
		$Seasonings.define(JSON.stringify({
			id: s.id,
			name: s.name,
			nutrition: s.nutrition || 0,
			saturation: s.saturation || 0,
			heal: s.heal || 0,
			effects: s.effects || [],
			combines: combines,
			fast: !!s.fast,
			alwaysEdible: !!s.alwaysEdible,
			damage: s.damage || 0,
			damageType: s.damageType || 'fire',
			transform: s.transform || {},
			station: !!s.station,
			combinesAll: !!s.combinesAll,
		}))
	})
})()
