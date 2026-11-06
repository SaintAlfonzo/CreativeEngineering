// Baking audit (TODO cooking/oven #6): no baked good or baking dough takes raw wheat. Every remaining recipe that
// asked for wheat (the item or #forge:grain/wheat) now asks for wheat flour. Members of the oven tables in baking.js
// (Hostile, End's and Miner's cookies and pies among them) are rebuilt on flour there and their mods' recipes swept;
// the foods below have no oven recipe yet and are swapped in place. Not baked, so left on wheat: Brewin' vodka, the
// sweet gelatine bucket, fried shrimp, turnip cake, the nutritional bar. Creative Delight runs after every pack script,
// so this walks the loaded data recipes of every mod; a mod that is not loaded matches nothing.
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Breads, buns, scones, eclair #########################
		var BREADS = ['minecraft:bread', 'quark:tweaks/crafting/utility/bent/bread', 'hostiledelight:gbread', // G5 begin (recipe ids, a filter for R.replaceInput)
			'neapolitan:adzuki/adzuki_bun', 'neapolitan:banana/banana_bread', 'neapolitan:strawberry/strawberry_scones',
			'endersdelight:strange_eclair']
		// G5 end
		R.replaceInput(BREADS.map(id => ({ id: id })), i.wheat, i.flour, e)

		//######################### Doughs and batter #########################
		// Sweet potato dough (tag form) and Rustic's batter: pot, and Slice & Dice's mixer copy of it. The pack's own
		// egg solution batter is written the same way in rusticdelight/recipes.js. Batter takes wheat flour, cornmeal or
		// rice in each grain slot (TODO dishes/ingredients #24, 2026-10-29; was wheat flour, before that wheat).
		R.replaceInput({ id: /^veggiesdelight:sweet_potato_dough_from_/ }, i.t_grain_wheat, i.flour, e)
		R.replaceInput([{ id: 'rusticdelight:cooking/batter' }, { id: 'sliceanddice:cooking/rusticdelight/cooking/batter' }], i.wheat,
			[i.t_flour_or_cornmeal, i.rice], e)
	})
})
