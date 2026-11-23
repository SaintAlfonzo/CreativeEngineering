// Creative Delight startup script, loaded from the jar after the pack's startup_scripts, so the dicts exist here.
// Restart to apply.
//
// Foods and kitchen items of owner rulings S2 and F10. Every texture is a placeholder copied as-is from
// the art named below (no palette swap, no edit) into assets/creativedelight/textures/; the owner's art replaces it later
// (feature_writeup/texture_sources/art_needed_2026-11-22.csv, sources.csv). Food values follow the nearest existing
// food of the family. Recipes: data creativedelight/kubejs/creativedelight/new_foods.js; bakeware tag and oven slot:
// creativedelight/tags.js.
//   plain_donut               baked/misc beside the Hearth & Harvest cider donut (baking/baked-goods #39, D15); cider donut art
//   <fruit>_scone (7)         fruit scones (baking/baked-goods #40); Hearth & Harvest raspberry scone art and values
//   orange_cranberry_muffin,  the decided effect muffins (baking/baked-goods #2): Fruits Delight cranberry / blueberry
//   lemon_poppy_muffin        muffin art, their 5 / 0.4
//   carrot/zucchini/pumpkin_muffin  veggie muffins (baking/baked-goods #2, #32): sweet potato muffin art and its 6 / 0.5
//   oatmeal_raisin_cookie     (baking/baked-goods #23): Vintage Delight oatmeal cookie art; raisin cookie values + 1 hunger
//   pickled_garlic            (dishes/pickles-and-ferments #17): Hearth & Harvest pickled carrots art and values
//   baking_dish               glass bakeware (baking/baked-goods #30), no durability: cake pan art
//   boxed_raw_pasta           9 raw pasta in a block (dishes/ingredients #8): Farmer's Delight crate bottom on every face
var reg = global.reg.forPack('creativedelight')

StartupEvents.registry('item', e => {
	var i = global.item
	var ef = global.effect
	var tex = id => 'creativedelight:item/' + id.split(':')[1]
	var food = (id, name, hunger, sat, more) => reg.item(e, id).displayName(name).texture(tex(id)).food(f => {
		f.hunger(hunger).saturation(sat)
		if (more) more(f)
	})

	food(i.plain_donut, 'Plain Donut', 4, 0.5)

	;[
		[i.blueberry_scone, 'Blueberry Scone'], [i.cranberry_scone, 'Cranberry Scone'], [i.lemon_scone, 'Lemon Scone'],
		[i.orange_scone, 'Orange Scone'], [i.cherry_scone, 'Cherry Scone'], [i.apple_scone, 'Apple Scone'],
		[i.peach_scone, 'Peach Scone'],
	].forEach(s => food(s[0], s[1], 6, 0.3))

	// Effects as decided 2026-11-18 (round 5): orange cranberry Regeneration 10 s + Shrinking 40 s, lemon poppy Haste
	// 10 s + Refreshing 10 s
	// (Shrinking and Refreshing are Fruits Delight's effects: left out without it)
	food(i.orange_cranberry_muffin, 'Orange Cranberry Muffin', 5, 0.4, f => {
		f.effect(ef.regeneration, 200, 0, 1.0)
		if (Platform.isLoaded('fruitsdelight')) {
			f.effect(ef.shrinking, 800, 0, 1.0)
		}
	})
	food(i.lemon_poppy_muffin, 'Lemon Poppy Muffin', 5, 0.4, f => {
		f.effect(ef.haste, 200, 0, 1.0)
		if (Platform.isLoaded('fruitsdelight')) {
			f.effect(ef.refreshing, 200, 0, 1.0)
		}
	})
	;[[i.carrot_muffin, 'Carrot Muffin'], [i.zucchini_muffin, 'Zucchini Muffin'], [i.pumpkin_muffin, 'Pumpkin Muffin']]
		.forEach(m => food(m[0], m[1], 6, 0.5))

	food(i.oatmeal_raisin_cookie, 'Oatmeal Raisin Cookie', 3, 0.2, f => f.fastToEat(true))
	food(i.pickled_garlic, 'Pickled Garlic', 4, 0.5)

	reg.item(e, i.baking_dish).displayName('Baking Dish').texture(tex(i.baking_dish))
})

StartupEvents.registry('block', e => {
	var i = global.item
	reg.block(e, i.boxed_raw_pasta).displayName('Boxed Raw Pasta')
		.material('wood').soundType('wood').hardness(1.0).resistance(1.0)
		.textureAll('creativedelight:block/boxed_raw_pasta')
		.tagBlock('minecraft:mineable/axe')
})
