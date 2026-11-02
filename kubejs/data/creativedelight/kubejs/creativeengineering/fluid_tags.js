// Food and drink fluid subtags (forge:fluid + forge:fluid/<subtag>).
// Moved from the pack's server_scripts/tags/fluidtags.js 2026-11-01 (creative_split.md 7.6, wave 4), rows
// unchanged. Only adds: running after the pack's tag scripts gives the same contents (nothing removes from these tags).
// The pack's dead-placeholder clearAll there names none of these fluids.
ServerEvents.tags('fluid', e => {
	if (!global.CD) return
	var t = global.tags
	var f = global.fluid

	;[
		['forge:fluid/tea', f.tea],
		['forge:fluid/apple_cider', f.apple_cider],
		['forge:fluid/long_apple_cider', f.long_apple_cider],
		['forge:fluid/strong_apple_cider', f.strong_apple_cider],
		['forge:fluid/honey', f.honey],
		['forge:fluid/chocolate/milk', f.chocolate],
		['forge:fluid/chocolate/dark', f.black_chocolate],
		['forge:fluid/chocolate/white', f.white_chocolate],
		['forge:fluid/chocolate/ruby', f.ruby_chocolate],
		['forge:fluid/hot_chocolate', f.hot_chocolate],
		['forge:fluid/caramel', f.caramel],
		['forge:fluid/tomato_sauce', f.tomato_sauce],
		['forge:fluid/melon_juice', f.melon_juice],
		['forge:fluid/chorus_juice', f.chorus_juice],
		['forge:fluid/carrot_juice', f.carrot_juice],
		['forge:fluid/dandelion_juice', f.dandelion_juice],
		['forge:fluid/turnip_water', f.turnip_water],
		['forge:fluid/egg_solution', f.egg_solution],
		['forge:fluid/frosting', f.frosting],
		['forge:fluid/milk_coffee', f.milk_coffee],
		['forge:fluid/honey_coffee', f.honey_coffee],
		['forge:fluid/chocolate_coffee', f.chocolate_coffee],
		['forge:fluid/dark_coffee', f.dark_coffee],
		['forge:fluid/syrup_coffee', f.syrup_coffee],
		['forge:fluid/pumpkin_coffee', f.pumpkin_coffee],
		['forge:fluid/cherry_blossom_coffee', f.cherry_blossom_coffee],
		['forge:fluid/lavender_tea', f.lavender_tea],
		['forge:fluid/matcha_latte', f.matcha_latte],
		['forge:fluid/berry_matcha_latte', f.berry_matcha_latte],
		['forge:fluid/yeast_culture', f.yeast_culture],
	].forEach(([tag, fluid]) => t.addFluid(e, tag, fluid))
})
