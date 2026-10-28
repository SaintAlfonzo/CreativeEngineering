// Moved from the pack's startup_scripts/cooking/seasonings.js 2026-10-25, rows unchanged.
// Creative Delight startup script, loaded from the jar after the pack's startup_scripts, so the dicts exist here.
// Restart to apply.
//
// Pack seasonings (patcherMod food/Seasonings): a seasoning goes on a food as NBT (Seasonings:[...]) by a
// creativeengineering:seasoning craft (global.NCH.season, kubejs/data/creative_delight/kubejs/creativeengineering/seasoning.js), and eating the food adds
// the seasoning's bonus on top of the food's own. Salt (the Salt mod's Salted) is separate and not listed here.
//
// key: {
//   id         seasoning id (NBT value, lang key seasoning.<ns>.<path>)
//   name       display name (assets/creative_delight/kubejs/creativeengineering/seasoning_lang.js)
//   item       item_dict key value of what you put in the grid (item or tag)
//   nutrition  hunger points added                     (optional)
//   saturation saturation points added, capped at food (optional)
//   heal       health points healed, 2 = one heart     (optional)
//   effects    [{ effect, duration (ticks), amplifier }] (optional)
//   combines   keys of seasonings that may share a food with this one; either side listing the other is enough.
//              Unlisted pairs don't combine.
//   Which foods take it (the tag #<ns>:seasonable/<path>, built by kubejs/data/creative_delight/kubejs/creativeengineering/seasoning_tags.js):
//   families   food families (paths in tools/food_families.py, e.g. 'snack', 'meat/cooked'), each with its
//              subfamilies: the bulk of the foods
//   add        item_dict values (items or tags) added after the families
//   remove     item_dict values (items) taken out last
//   A seasoning with no foods gets no craft.
//   needs      Creative Delight member (or mod) id the seasoning's item comes from; the seasoning's tag and crafts are
//              skipped while it is off (optional)
//   icon: the overlay drawn on seasoned food is creativeengineering:item/seasoned/<path> (textureswapper/seasoning/make_seasoning.py)
// }
// Startup script, read on both sides: restart to apply. Values are first test values (food/seasoning #3).
var i = global.item
var ef = global.effect

global.seasoning = {
	// popcorn only for now; open them up by family later
	cheese: { id: 'creativeengineering:cheese', name: 'Cheese', item: i.t_cheese_wedge, saturation: 2,
		families: [], add: [i.popcorn] },
	onion:  { id: 'creativeengineering:onion',  name: 'Onion',  item: i.onion,  nutrition: 2, combines: ['garlic'],
		families: [], add: [i.popcorn] },
	garlic: { id: 'creativeengineering:garlic', name: 'Garlic', item: i.garlic,
		effects: [{ effect: ef.speed, duration: 200, amplifier: 0 }],
		families: [], add: [i.popcorn] },
	// sugar or powdered sugar (not the rest of #forge:sugar: glycerol). Popcorn + sugar is Corn Delight's caramel popcorn,
	// so no foods yet (was waffles).
	sugar:  { id: 'creativeengineering:sugar',  name: 'Sugar',  item: [i.sugar, i.powdered_sugar], nutrition: 1,
		families: [], add: [] },
	// Ender's Delight's crushed void pepper, mirroring Salt's bonus: +2 nutrition and a 0.1 saturation modifier
	// (config/salt-common.toml SaltingNutrition / SaltingSaturationModifier; absolute saturation = nutrition * modifier * 2).
	void_pepper: { id: 'creativeengineering:void_pepper', name: 'Void Pepper', item: i.crushed_voidpepper,
		nutrition: 2, saturation: 0.4, needs: 'enders_additions', families: [], add: [i.popcorn] },
}

;(function () {
	var $Seasonings
	try {
		$Seasonings = Java.loadClass('com.SaintAlfonzo.creativeengineering.food.Seasonings')
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
			nutrition: s.nutrition || 0,
			saturation: s.saturation || 0,
			heal: s.heal || 0,
			effects: s.effects || [],
			combines: combines,
		}))
	})
})()
