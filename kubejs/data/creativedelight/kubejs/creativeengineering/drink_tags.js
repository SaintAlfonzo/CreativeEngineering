// Input tags and fluid tags of the drink rules (feature_writeup/creative_delight_drinks.md, TODO food/drinks #15).
// Recipes: creativeengineering/drinks.js. Every input with a slice form is accepted whole or sliced (section 0).
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item

	t.add(e, i.t_drink_fruits_lemon, [i.t_fruits_lemon, i.lemon_slice])
	t.add(e, i.t_drink_fruits_orange, [i.t_fruits_orange, i.orange_slice])
	t.add(e, i.t_drink_fruits_hamimelon, [i.hamimelon_slice, i.t_fruits_cantaloupe])
	t.add(e, i.t_sea_pickles, [i.sea_pickle, i.sea_pickle_slices])
	// Azalea tea: the flowering bush or its leaves; Twigs' and Immersive Weathering's azalea flowers count as one
	// (flowers #5: the recipe side of the merge)
	t.add(e, i.t_flowering_azalea, [i.flowering_azalea, i.flowering_azalea_leaves])
	t.add(e, i.t_azalea_flowers, [i.azalea_flowers, i.immersive_weathering_azalea_flowers])
	// Lavender tea: 2 pink flowers + 2 of amethyst shard or lavender blossom leaves (#forge:lavender)
	t.add(e, i.t_pink_flowers, [i.pink_tulip, i.peony, i.lilac, i.allium])
	t.add(e, i.t_lavender_tea_herbs, [i.amethyst_shard, i.t_lavender])
	// Coffee flavours: chocolate = cocoa or a chocolate bar, pumpkin = a pumpkin or a slice
	t.add(e, i.t_coffee_chocolate, [i.cocoa_beans, i.t_bars_chocolate])
	t.add(e, i.t_coffee_pumpkin, [i.pumpkin, i.pumpkin_slice])
})

ServerEvents.tags('fluid', e => {
	if (!global.CD) return
	var t = global.tags
	var f = global.fluid
	;['kiwi_tea', 'turnip_tea', 'bayberry_tea', 'hamimelon_tea', 'carrot_tea', 'lemon_tea', 'melon_tea', 'orange_tea',
		'pear_tea', 'chorus_tea', 'lychee_juice', 'hawberry_juice', 'mango_juice', 'mangosteen_juice', 'peach_juice',
		'sea_grape_juice', 'pickerelweed_juice'].forEach(n => t.addFluid(e, 'forge:fluid/' + n, f[n]))
})
