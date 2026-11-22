// requires: creativedelight
// priority: -90
// Potato and sweet potato (TODO dishes/ingredients #21): a dish that takes a potato takes a sweet potato, raw or baked.
// Non-member file. Late by priority so the member scripts' swaps (Farmer's rabbit stew baked -> raw potato) are in.
//
// - Raw: dish recipes naming the potato (minecraft:potato or #forge:crops/potato) take #forge:vegetables/potato (potato,
//   sweet potato, both slices; Veggies Delight's and Rustic Delight's tags). Data recipes by id below (recipe dump of the
//   jars); the pack's own script recipes name i.any_potato themselves.
// - Baked: #creativedelight:baked_potatoes (baked potato, baked sweet potato), the same way.
// Not swapped (owner: the mashed family keeps the two apart; a potato's own lines stay its own): mashed potatoes,
// potato slices and baked potato slices cutting, potato dough, crates, the Stuffed Potato (made on the baked potato), the
// golden baked potato, salting, cans, gnocchi, non-food recipes (Botania, Quark, Power Grid, machines' fuels).
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var i = global.item
	global.tags.add(e, i.t_baked_potatoes, [i.baked_potato].concat(global.CD.on('veggiesdelight') ? [i.baked_sweet_potato] : []))
})

ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('veggiesdelight')) return
	var i = global.item
	var R = global.NCH.forPack('creativedelight')

	// Pot recipes also get Slice & Dice's heated mixing copy (sliceanddice:cooking/<namespace>/<path>)
	var withMixer = ids => ids.concat(ids.filter(id => id.indexOf('cooking/') >= 0)
		.map(id => 'sliceanddice:cooking/' + id.replace(':', '/'))) // G5 (R.replaceInput ids)
	var RAW = withMixer([ // G5 begin (R.replaceInput ids)
		'farmersdelight:cooking/baked_cod_stew', 'farmersdelight:cooking/beef_stew', 'farmersdelight:cooking/mushroom_rice',
		'farmersdelight:cooking/rabbit_stew', 'farmersdelight:cooking/stuffed_pumpkin_block', 'farmersdelight:cooking/vegetable_soup',
		'ends_delight:food/dragon_meat_stew_block', 'veggiesdelight:coleslaw', 'veggiesdelight:vegetarian_patty',
		'vintagedelight:cooking/pickle_soup', 'abnormals_delight:autumnity/cooking/stuffed_pumpkin_block',
		'abnormals_delight:passion_fruit_glazed_duck', 'brewinandchewin:cooking/fiery_fondue_pot',
		'brewinandchewin:cooking/scarlet_pierogi',
	]).map(id => ({ id: id })) // G5 end
	var BAKED = withMixer([ // G5 begin (R.replaceInput ids)
		'farmersdelight:roast_chicken_block', 'farmersdelight:shepherds_pie_block', 'farmersdelight:steak_and_potatoes',
		'abnormals_delight:environmental/cooking/truffle_mash', 'corn_delight:cooking/cornbread_stuffing',
		'biomesoplenty:rabbit_stew_from_toadstool',
	]).map(id => ({ id: id })) // G5 end

	R.replaceInput(RAW, i.minecraft_potato, i.any_potato, e)
	R.replaceInput(RAW, i.potato, i.any_potato, e)
	R.replaceInput(BAKED, i.baked_potato, i.t_baked_potatoes, e)
})
