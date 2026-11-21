// requires: creativedelight
// requires: create
// requires: farmersdelight
// Cooking Pot versions of the Create filling food recipes (plan_food_diversity_1121.md I9, R3). The spout uses
// 250 mB of coating per item, which is one coating item (a bar, or a honey bottle); the pot takes 1.5x that, batched:
// 2 bases + 3 coating items -> 2 dishes. Milk and jelly fills (sweet roll, jelly bread) are not coatings and stay
// spout only. Diversity of the results comes from the pot rule (tools/food_diversity.py).
ServerEvents.recipes(e => {
	if (!global.CD) return

	const R = global.NCH.forPack('creativedelight')
	var i = global.item

	var milk = i.bar_of_chocolate, honey = i.honey_bottle
	var rows = [
		// Create
		[i.honeyed_apple, i.apple, honey],
		[i.chocolate_glazed_berries, i.sweet_berries, milk],
	]
	if (global.has('create_factory')) {
		rows.push([i.chocolate_apple, i.apple, milk], [i.chocolate_waffle, i.waffle, milk], [i.chocolate_roll, i.t_bread, milk],
			[i.honey_waffle, i.waffle, honey], [i.honey_roll, i.t_bread, honey], [i.honey_glazed_berries, i.sweet_berries, honey])
		if (global.has('create_confectionery')) {
			var dark = i.bar_of_black_chocolate, ruby = i.bar_of_ruby_chocolate, white = i.bar_of_white_chocolate, caramel = i.bar_of_caramel
			rows.push([i.dark_chocolate_apple, i.apple, dark], [i.ruby_chocolate_apple, i.apple, ruby],
				[i.white_chocolate_apple, i.apple, white], [i.caramel_apple, i.apple, caramel],
				[i.dark_chocolate_waffle, i.waffle, dark], [i.ruby_chocolate_waffle, i.waffle, ruby],
				[i.white_chocolate_waffle, i.waffle, white], [i.caramel_waffle, i.waffle, caramel],
				[i.dark_chocolate_roll, i.t_bread, dark], [i.ruby_chocolate_roll, i.t_bread, ruby],
				[i.white_chocolate_roll, i.t_bread, white], [i.caramel_roll, i.t_bread, caramel])
		}
	}
	if (global.has('create_confectionery')) {
		var dark = i.bar_of_black_chocolate, ruby = i.bar_of_ruby_chocolate, white = i.bar_of_white_chocolate, caramel = i.bar_of_caramel
		// glazed berries and marshmallows
		rows.push([i.black_chocolate_glazed_berries, i.sweet_berries, dark], [i.ruby_chocolate_glazed_berries, i.sweet_berries, ruby],
			[i.white_chocolate_glazed_berries, i.sweet_berries, white], [i.caramel_glazed_berries, i.sweet_berries, caramel],
			[i.chocolate_glazed_marshmallow, i.marshmallow, milk], [i.black_chocolate_glazed_marshmallow, i.marshmallow, dark],
			[i.ruby_chocolate_glazed_marshmallow, i.marshmallow, ruby], [i.white_chocolate_glazed_marshmallow, i.marshmallow, white])
		// candies: a filling bar coated in a chocolate (no suffix = caramel filling)
		rows.push([i.chocolate_candy, caramel, milk], [i.chocolate_candy_1, white, milk], [i.chocolate_candy_2, dark, milk], [i.chocolate_candy_3, ruby, milk],
			[i.black_chocolate_candy, caramel, dark], [i.black_chocolate_candy_1, i.t_bars_chocolate, dark], [i.black_chocolate_candy_2, white, dark], [i.black_chocolate_candy_3, ruby, dark],
			[i.ruby_chocolate_candy, caramel, ruby], [i.ruby_chocolate_candy_1, i.t_bars_chocolate, ruby], [i.ruby_chocolate_candy_2, white, ruby], [i.ruby_chocolate_candy_3, dark, ruby],
			[i.white_chocolate_candy, caramel, white], [i.white_chocolate_candy_1, i.t_bars_chocolate, white], [i.white_chocolate_candy_2, dark, white], [i.white_chocolate_candy_3, ruby, white])
	}
	if (global.has('buzzier_bees')) {
		rows.push([i.glazed_porkchop, i.porkchop_cooked, honey], [i.honey_bread, i.t_bread, honey])
	}

	global.CD.recipes(() => {
		rows.forEach(row => {
			var out = row[0], base = row[1], coat = row[2]
			if (!out || !base || !coat) return
			var name = String(out).split(':')[1]
			R.fdCooking([base, base, coat, coat, coat], Item.of(out, 2), e, 'pot_mirror/' + name, 0.35, 200)
		})
	})
})
