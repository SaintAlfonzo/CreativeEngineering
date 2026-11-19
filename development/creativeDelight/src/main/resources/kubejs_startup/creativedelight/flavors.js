// Creative Delight startup script, loaded from the jar after the pack's startup_scripts, so the dicts exist here.
// Restart to apply.
//
// Pack flavors (Creative Delight food/Flavors; the mechanic was called seasonings until food/seasoning #25,
// plan_food_batch_1119.md wave E): a flavor goes on a food as NBT (Flavors:[...]) by a creativedelight:flavor craft
// (global.NCH.flavor, kubejs/data/creativedelight/kubejs/creativedelight/flavor.js), and eating the food adds the
// flavor's bonus on top of the food's own. Two kinds: a seasoning (a dust or a whole spice) and a sauce. Salt (the Salt
// mod's Salted) is separate and not listed here; it only takes part in a pair (below). Pre-rename seasoned food keeps
// no flavor (owner R4: the old Seasonings NBT is not read).
//
// Values (owner R3, R20): every flavor gives +2 saturation points and nothing else from nutrition or healing; its
// effects and kinds stay. Grilled gives no saturation: it adds flavor slots (creativedelight-common.toml [food]
// grilledFlavorSlots, default 2) past [food] maxFlavors (default 2). Pairs (global.flavorPairs) give 1 more saturation
// point together (0.5 a flavor), so a pair is 5.
//
// key: {
//   id         flavor id (NBT value, lang key flavor.<ns>.<path>)
//   name       display name (assets/creativedelight/kubejs/creativedelight/flavor_lang.js)
//   kind       'seasoning' (default) or 'sauce'
//   item       item_dict key value of what you put in the grid (item, tag, or a list of them)
//   nutrition  hunger points added                     (optional; none since R3)
//   saturation saturation points added, capped at food (optional)
//   heal       health points healed, 2 = one heart     (optional; none since R3)
//   effects    [{ effect, duration (ticks), amplifier }] (optional)
//   Kinds from the Barbeque's Delight fold (food/seasoning #11), all optional:
//   fast         true: eaten fast (16 ticks)
//   alwaysEdible true: edible when full
//   damage       health points of damage on eating; damageType 'fire' (default), 'magic' or 'generic'
//   transform    { chance, duration, amplifier }: each of the food's own effects gets chance x chance, duration x
//                duration, amplifier + amplifier. fast, alwaysEdible and transform reach a food only if its item asks
//                food/Flavors.foodProperties (the Barbeque's Delight skewers and combos do)
//   station      true: put on by a station only (the Grill's grilled), never crafted; counts toward the cap only
//                with creativedelight-common.toml [food] grilledCountsTowardMax
//   addsSlots    true: a food carrying it takes [food] grilledFlavorSlots more flavors (Grilled)
//   combinesAll  true: shares a food with any flavor
//   combines   keys of flavors that may share a food with this one; either side listing the other is enough.
//              Unlisted pairs don't combine.
//   Which foods take it (the tag #<ns>:flavorable/<path>, built by kubejs/data/creativedelight/kubejs/creativedelight/flavor_tags.js):
//   families   food families (paths in tools/food_families.py, e.g. 'snack', 'meat/cooked'), each with its
//              subfamilies: the bulk of the foods
//   add        item_dict values (items or tags) added after the families
//   remove     item_dict values (items) taken out last
//   A flavor with no foods gets no craft.
//   needs      Creative Delight member (or mod) id the flavor's item comes from; the flavor's tag and crafts are
//              skipped while it is off (optional)
//   icon: the overlay drawn on flavored food is <ns>:item/seasoned/<path> (textureswapper/seasoning/make_seasoning.py)
// }
var i = global.item
var ef = global.effect
var popcorn = global.has(i.popcorn) ? [i.popcorn] : []
var grilledSkewers = global.has('barbequesdelight') ? [i.t_grilled_skewers] : []

global.flavor = {
	// popcorn only for now; open them up by family later
	cheese: { id: 'creativedelight:cheese', name: 'Cheese', item: i.t_cheese_wedge, saturation: 2,
		families: [], add: popcorn },
	// whole onion and garlic stay; their milled powders do the same (food/seasoning #28, R6)
	onion:  { id: 'creativedelight:onion',  name: 'Onion',  item: [i.onion, i.onion_powder], saturation: 2, combines: ['garlic'],
		families: [], add: popcorn },
	garlic: { id: 'creativedelight:garlic', name: 'Garlic', item: [i.garlic, i.garlic_powder], saturation: 2,
		effects: [{ effect: ef.speed, duration: 200, amplifier: 0 }],
		families: [], add: popcorn },
	// sugar or powdered sugar (not the rest of #forge:sugar: glycerol). Popcorn + sugar is Corn Delight's caramel popcorn,
	// so no foods yet (was waffles).
	sugar:  { id: 'creativedelight:sugar',  name: 'Sugar',  item: [i.sugar, i.powdered_sugar], saturation: 2, combines: ['ghost_pepper'],
		families: [], add: [] },
	// Ghost pepper powder, milled from Vintage Delight's ghost pepper (food/seasoning #28); not Barbeque's chili powder
	ghost_pepper: { id: 'creativedelight:ghost_pepper', name: 'Ghost Pepper', item: i.ghost_pepper_powder, saturation: 2,
		combines: ['sugar'], needs: 'vintagedelight', families: [], add: popcorn },
	// Ender's Delight's crushed void pepper
	void_pepper: { id: 'creativedelight:void_pepper', name: 'Void Pepper', item: i.crushed_voidpepper, // G5 (needs: its tag and crafts are skipped while enders_additions is off)
		saturation: 2, needs: 'enders_additions', families: [], add: popcorn },

	// Barbeque's Delight (food/seasoning #11, barbeques_delight.md 4.1, seasonings.md): the three powders are seasonings,
	// the three sauces sauces (R5). Kinds as 1.3.0; cumin's heal became the +2 saturation (R3). Powders go on raw and
	// cooked BBQ skewers (a raw skewer's flavors carry over when it is grilled); sauces on cooked ones only (O28). One
	// item per craft: a trade gives 64 powders or 16 sauces (O31). Foods by tag: the raw_skewers tag holds the 8 raw
	// pack skewers too (dishes/meals #50, plan_meals_rolls_bbq.md C3).
	cumin: { id: 'barbequesdelight:cumin', name: 'Cumin', item: i.cumin_powder, saturation: 2,
		needs: 'barbequesdelight', families: [], add: [i.t_raw_skewers, i.t_grilled_skewers] },
	pepper: { id: 'barbequesdelight:pepper', name: 'Pepper', item: i.pepper_powder, saturation: 2, fast: true,
		needs: 'barbequesdelight', families: [], add: [i.t_raw_skewers, i.t_grilled_skewers] },
	chili: { id: 'barbequesdelight:chili', name: 'Chili', item: i.chili_powder, saturation: 2, alwaysEdible: true, damage: 2,
		needs: 'barbequesdelight', families: [], add: [i.t_raw_skewers, i.t_grilled_skewers] },
	honey_mustard: { id: 'barbequesdelight:honey_mustard', name: 'Honey Mustard', kind: 'sauce', item: i.honey_mustard_sauce,
		saturation: 2, transform: { chance: 2, duration: 0.5 }, needs: 'barbequesdelight', families: [], add: [i.t_grilled_skewers] },
	buffalo: { id: 'barbequesdelight:buffalo', name: 'Buffalo', kind: 'sauce', item: i.buffalo_sauce, saturation: 2, damage: 2,
		transform: { duration: 0.5, amplifier: 1 }, needs: 'barbequesdelight', families: [], add: [i.t_grilled_skewers] },
	barbeque: { id: 'barbequesdelight:barbeque', name: 'Barbeque', kind: 'sauce', item: i.barbeque_sauce, saturation: 2,
		transform: { duration: 2 }, needs: 'barbequesdelight', families: [], add: [i.t_grilled_skewers] },
	// Sauces from other mods' items (R5): End's Delight chorus sauce, Hostile Delight glowing and ardent sauce, Vintage
	// Delight relish. On cooked skewers like the Barbeque's Delight sauces, while that member is on.
	chorus_sauce: { id: 'creativedelight:chorus_sauce', name: 'Chorus Sauce', kind: 'sauce', item: i.chorus_sauce, // G5 (needs)
		saturation: 2, needs: 'ends_delight', families: [], add: grilledSkewers },
	glowing_sauce: { id: 'creativedelight:glowing_sauce', name: 'Glowing Sauce', kind: 'sauce', item: i.glowingsauce, // G5 (needs)
		saturation: 2, needs: 'hostiledelight', families: [], add: grilledSkewers },
	ardent_sauce: { id: 'creativedelight:ardent_sauce', name: 'Ardent Sauce', kind: 'sauce', item: i.ardentsauce, // G5 (needs)
		saturation: 2, needs: 'hostiledelight', families: [], add: grilledSkewers },
	relish: { id: 'creativedelight:relish', name: 'Relish', kind: 'sauce', item: [i.relish_jar, i.relish_bottle],
		saturation: 2, needs: 'vintagedelight', families: [], add: grilledSkewers },
	// Grilled (food/seasoning #13): put on only by the Grill when a flipped raw skewer finishes (station: no craft,
	// no foods). No saturation of its own (R20): it adds [food] grilledFlavorSlots flavor slots. Does not count toward
	// [food] maxFlavors unless grilledCountsTowardMax. Combines with any flavor. Combos (wrap, sandwich, bibimbap) drop
	// it from the skewers they hold (O35).
	grilled: { id: 'barbequesdelight:grilled', name: 'Grilled', station: true, addsSlots: true, combinesAll: true,
		needs: 'barbequesdelight', families: [], add: [] },
}

// Flavor pairs (R3): [flavor key or 'salt' (Salt's Salted), flavor key, extra saturation points]. Both on one food
// give the extra (0.5 a flavor: 2 + 2 + 1 = 5). The pack flavors of a pair also need each other in `combines`.
global.flavorPairs = [
	['onion', 'garlic', 1],
	['salt', 'pepper', 1],
	['sugar', 'ghost_pepper', 1],
]

StartupEvents.registry('item', e => {
	// Milled powders (food/seasoning #28): Creative Delight's own items. Art: textureswapper/palette_swap.py on Barbeque's
	// Delight's chili powder with each crop's palette (texture_sources/sources.csv).
	var reg = global.reg.forPack('creativedelight')
	reg.item(e, i.onion_powder).displayName('Onion Powder')
	reg.item(e, i.garlic_powder).displayName('Garlic Powder')
	reg.item(e, i.ghost_pepper_powder).displayName('Ghost Pepper Powder')
})

;(function () {
	var $Flavors
	try {
		$Flavors = Java.loadClass('com.SaintAlfonzo.creativedelight.food.Flavors')
	} catch (err) {
		console.warn('[flavor] Creative Delight Flavors missing, flavors not defined: ' + err)
		return
	}
	Object.keys(global.flavor).forEach(key => {
		var s = global.flavor[key]
		var combines = (s.combines || []).map(k => {
			if (!global.flavor[k]) throw new Error('[flavor] ' + key + ' combines with unknown flavor ' + k)
			return global.flavor[k].id
		})
		$Flavors.define(JSON.stringify({
			id: s.id,
			name: s.name,
			kind: s.kind || 'seasoning',
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
			addsSlots: !!s.addsSlots,
			combinesAll: !!s.combinesAll,
		}))
	})
	global.flavorPairs.forEach(p => {
		var id = k => k === 'salt' ? 'salt:salted' : global.flavor[k].id
		$Flavors.pair(id(p[0]), id(p[1]), p[2])
	})
})()
