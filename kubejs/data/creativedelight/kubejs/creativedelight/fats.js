// requires: creativedelight
// requires: farmersdelight
// Fats and oils (TODO dishes/ingredients #10, plan_food_batch_1119.md wave D, R12, R19, R22). Items: CD startup
// creativedelight/fats.js. Each part sits behind its [fats] switch in creativedelight-common.toml (global.CD.fats):
//   blubber       the Alex's Mobs cachalot whale drops 3-6 Blubber; a Knife cuts one into 2 animal fat; it is a
//                 #forge:animal_fat (soap: Compat other/misc_mods.js)
//   whaleOil      Blubber renders into Whale Oil: heated Create mixer (1 blubber -> 250 mB), Cooking Pot (2 blubber + a
//                 bottle -> 1); bottle <-> 250 mB by Spout and Item Drain; the fluid is a #forge:lubricant
//   animalFatOil  2 animal fat + a bottle cook into Cooking Oil (Delightful's animal oil bottle is merged into Cooking
//                 Oil: swaps/merges_delightful_oil.json)
//   seedOil       any standard seed (#forge:seeds) is a cooking oil ingredient (Rustic's pot and mixer recipes); a
//                 Spout fills a bottle with 250 mB of Crafts & Additions' seed oil into Cooking Oil
//   fishOil       Alex's Mobs fish oil is a #rusticdelight:cooking_oil; 2 raw fish + a bottle cook down to 1 (its
//                 4-blobfish craft stays)
// The plan's cauldron route for whale oil became the Cooking Pot recipe (plan build notes, wave D).
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item
	if (global.CD.fats('blubber')) t.add(e, i.t_animal_fat, i.blubber)
	if (global.CD.fats('seedOil')) t.add(e, i.t_cooking_oil_ingredients, i.t_seeds)
	if (global.CD.fats('fishOil') && global.has(i.fish_oil)) t.add(e, i.t_cooking_oil, i.fish_oil)
})

ServerEvents.tags('fluid', e => {
	if (!global.CD || !global.CD.fats('whaleOil')) return
	global.tags.addFluid(e, global.fluid.t_lubricant, global.fluid.whale_oil)
})

ServerEvents.recipes(e => {
	if (!global.CD) return
	var i = global.item
	var f = global.fluid
	var R = global.NCH.forPack('creativedelight')

	global.CD.recipes(() => {
		if (global.CD.fats('blubber'))
			R.fdCutting(i.blubber, i.any_knife, Item.of(i.animal_fat, 2), e, 'animal_fat_from_blubber')
		if (global.CD.fats('whaleOil')) {
			R.createMixing(Fluid.of(f.whale_oil, 250), [i.blubber], e, 'whale_oil', 'heated')
			R.fdCooking([i.blubber, i.blubber], Item.of(i.whale_oil, 1), e, 'whale_oil_manual_only', 0.35, 200, i.glass_bottle)
			R.createBottle(i.whale_oil, f.whale_oil, e, 'whale_oil')
		}
		if (global.CD.fats('animalFatOil')) {
			R.remove({ id: 'delightful:cooking/animal_oil_bottle' }, e)
			R.fdCooking([i.animal_fat, i.animal_fat], Item.of(i.cooking_oil, 1), e, 'cooking_oil_from_animal_fat', 0.35, 200, i.glass_bottle)
		}
		if (global.CD.fats('seedOil') && global.has(f.seed_oil))
			R.createFilling(i.cooking_oil, [i.glass_bottle, Fluid.of(f.seed_oil, 250)], e, 'cooking_oil_from_seed_oil')
		if (global.CD.fats('fishOil') && global.has(i.fish_oil))
			R.fdCooking([i.t_raw_fishes, i.t_raw_fishes], Item.of(i.fish_oil, 1), e, 'fish_oil_from_raw_fish', 0.35, 200, i.glass_bottle)
	})
})

// Blubber from the cachalot whale (its own table is empty): a Creative Loot drop rule (loot/09_drops.js)
global.LOOT.rules(function (L) {
	if (!global.CD || !global.CD.fats('blubber')) return
	if (!global.has('alexsmobs')) return
	L.drop(['alexsmobs:entities/cachalot_whale'], global.item.blubber, { count: [3, 6] })
})
