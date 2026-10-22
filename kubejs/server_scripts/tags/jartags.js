// Supplementaries jars: what goes in one, and what may share one (feature_writeup/mixed_jars.md).
//   #supplementaries:cookies                    a jar takes the item at all (the mods only tag their own few)
//   #creativeengineering:jar_family/<name>      items that may be mixed in one jar (patcherMod compat/JarMixing.java:
//                                               an item joins a jar when one family holds it and everything inside)
// Every family is nested into #supplementaries:cookies, so putting an item in a family is enough. An item that is
// jarable but in no family (moredelight:toast, the Hyperexperience bottle) still fills a jar, on its own.
// Removed items (honey apple, chocolate coins) are left out; see the writeup for what was considered and skipped.

ServerEvents.tags('item', e => {
	var t = global.tags
	var i = global.item

	var families = {
		// every cookie, plus the Botania biscuit and gingerbread men
		cookies: [
			i.cookie, i.honey_cookie, i.sweet_berry_cookie, i.green_tea_cookie, i.mulberry_cookie, i.cherry_cookie,
			i.maple_cookie, i.bayberry_cookie, i.cranberry_cookie, i.lemon_cookie, i.persimmon_cookie, i.golden_cookie,
			i.enchanted_golden_cookie, i.uncanny_cookies, i.mana_cookie, i.gingerbread_man,
		],
		// honeyed, caramel and chocolate apples
		candied_apples: [
			i.honeyed_apple, i.caramel_apple, i.chocolate_apple, i.dark_chocolate_apple, i.ruby_chocolate_apple,
			i.white_chocolate_apple,
		],
		// glazed berries and chocolate strawberries
		candied_berries: [
			i.chocolate_glazed_berries, i.black_chocolate_glazed_berries, i.caramel_glazed_berries,
			i.ruby_chocolate_glazed_berries, i.white_chocolate_glazed_berries, i.honey_glazed_berries,
			i.chocolate_strawberries,
		],
		// wrapped and chocolate candies (all four Confectionery shapes), candy canes, gummies, bonbons, fudge
		candies: [
			i.candy, i.honey_candy, i.candy_cane, i.chocolate_candy, i.chocolate_candy_1, i.chocolate_candy_2,
			i.chocolate_candy_3, i.black_chocolate_candy, i.black_chocolate_candy_1, i.black_chocolate_candy_2,
			i.black_chocolate_candy_3, i.ruby_chocolate_candy, i.ruby_chocolate_candy_1, i.ruby_chocolate_candy_2,
			i.ruby_chocolate_candy_3, i.white_chocolate_candy, i.white_chocolate_candy_1, i.white_chocolate_candy_2,
			i.white_chocolate_candy_3, i.rock_candy, i.salmonberry_gummy, i.mint_candies, i.strawberry_bean_bonbons,
			i.cocoa_fudge, i.vanilla_fudge,
		],
		// plain and glazed
		marshmallows: [
			i.marshmallow, i.chocolate_glazed_marshmallow, i.black_chocolate_glazed_marshmallow,
			i.ruby_chocolate_glazed_marshmallow, i.white_chocolate_glazed_marshmallow,
		],
		// bars and chocolate fingers
		chocolate_bars: [
			i.bar_of_chocolate, i.bar_of_black_chocolate, i.bar_of_ruby_chocolate, i.bar_of_white_chocolate,
			i.bar_of_caramel, i.chocolate_bar, i.mint_chocolate, i.vanilla_chocolate_fingers,
		],
		// plain, golden, enchanted
		apples: [
			i.apple, i.golden_apple, i.enchanted_golden_apple,
		],
		// berries, strawberries, mulberries, cherries, grapes (golden ones too)
		berries: [
			i.sweet_berries, i.glow_berries, i.golden_sweet_berries, i.enchanted_golden_sweet_berries,
			i.golden_glow_berries, i.enchanted_golden_glow_berries, i.blueberry, i.cranberry, i.bayberry, i.hawberry,
			i.salmonberries, i.strawberries, i.white_strawberries, i.mulberry, i.cherry, i.red_grape, i.white_grape,
			i.savanna_grapes_red, i.savanna_grapes_white, i.taiga_grapes_red, i.taiga_grapes_white, i.jungle_grapes_red,
			i.jungle_grapes_white,
		],
		dried_fruit: [
			i.dried_persimmon, i.dried_banana,
		],
	}
	Object.keys(families).forEach(name => {
		var tag = i['t_jar_family_' + name]
		t.add(e, tag, families[name])
		t.add(e, i.t_jar_cookies, tag)
	})

	// jarable, mixes with nothing else
	t.add(e, i.t_jar_cookies, [i.hyper_experience_bottle])
})
