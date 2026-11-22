// requires: creativedelight
// Any milk (plan_wood_bucket_batch_1121.md R1, R11; feature_writeup/creative_delight_drinks.md 9): cow, goat and nut milk
// count as milk, in a bucket (iron or wooden), a bottle or a pipe. Non-member file: recipe ids are
// creativedelight:<type>/<suffix> (global.CD.recipes).
//
// - Fluid tag #forge:milk: minecraft:milk (Forge), goat milk (hearthandharvest/tags.js) and nut milk (here). Every pack
//   recipe that takes milk fluid takes the tag (f.t_milk through R.createFluidTag / bncFermenting); the Kettle tank
//   matches one fluid, so its milk recipes come once per milk (farmersrespite/kettle.js, NCH frBrewing base list).
//   Slice & Dice's mixer copies of pot recipes take the tag through Creative Delight's SliceAndDiceMilkTagMixin.
// - Item tag #forge:milk (every #forge:milk recipe input: pot, grid, Kettle, basin): the three wooden milk buckets join
//   the iron ones and the bottles. A wooden bucket's remainder is the empty wooden bucket, so it comes back.
// - Recipes that named the vanilla Milk Bucket take #creativedelight:milk_buckets (any full milk bucket), Splash Milk
//   only cow milk (#forge:buckets/milk). Not swapped: the Golden Milk Bucket recipes, where the bucket itself becomes
//   the golden (iron) bucket. Canned Powdered Milk takes the tag in its generator row (tools/gen_can_types.py).
// - Cheese takes animal milk only (TODO dishes/ingredients #35): #creativedelight:animal_milk (fluid: cow and goat) in
//   Brewin's cheese keg recipes (Creative Brewing brewinandchewin/alcohol.js), #creativedelight:animal_milk_buckets
//   (any full milk bucket but nut milk) in Vintage Delight's cheese curds. Goat cheese already takes goat milk only.
ServerEvents.tags('fluid', e => {
	if (!global.CD) return
	var f = global.fluid
	if (f.nut_milk) global.tags.addFluid(e, f.t_milk, f.nut_milk)
	global.tags.addFluid(e, f.t_animal_milk, [f.milk].concat(global.has('hearthandharvest') ? [f.goat_milk] : []))
})

ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item
	var wooden = [i.wooden_milk_bucket, i.wooden_goat_milk_bucket, i.wooden_nut_milk_bucket]
	t.add(e, i.milk, wooden)
	t.add(e, [i.t_buckets_milk, i.t_cow_milk], i.wooden_milk_bucket)
	t.add(e, i.t_milk_buckets, [i.minecraft_milk_bucket, i.goat_milk_bucket].concat(wooden))
	t.add(e, i.t_animal_milk_buckets, [i.minecraft_milk_bucket, i.goat_milk_bucket, i.wooden_milk_bucket, i.wooden_goat_milk_bucket])
})

ServerEvents.recipes(e => {
	if (!global.CD) return
	var i = global.item
	var f = global.fluid
	var R = global.NCH.forPack('creativedelight')

	global.CD.recipes(() => {
		// Nut Milk bottle <-> 250 mB nut milk (Spout and Item Drain; the Kettle reads the pair)
		if (global.CD.on('vintagedelight') && f.nut_milk) R.createBottle(i.nut_milk_bottle, f.nut_milk, e, 'nut_milk')

		// Recipes that named the vanilla Milk Bucket (recipe dump). Slice & Dice's copy of the cheese curds pot recipe
		// follows the pot recipe. An absent mod's id matches nothing.
		R.replaceInput([
			{ id: 'cookingforblockheads:milk_jar' },
			{ id: 'engineersdecor:independent/small_milking_machine_recipe' },
			{ id: 'hostiledelight:mbutterrecp' },
		], i.minecraft_milk_bucket, i.t_milk_buckets, e)
		R.replaceInput({ id: 'vintagedelight:cooking/cheese_curds' }, i.minecraft_milk_bucket, i.t_animal_milk_buckets, e)
		R.replaceInput({ id: 'slpash_milk:milk_bottle' }, i.minecraft_milk_bucket, i.t_buckets_milk, e)   // (sic) the mod's id
	})
})
