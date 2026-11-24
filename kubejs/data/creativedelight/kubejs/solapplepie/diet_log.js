// SPDX-License-Identifier: GPL-3.0-only
// requires: creativedelight
// requires: solraspberrypie
// The Diet Log recipe takes any edible item outside #solraspberrypie:diet_log_excluded (raspberry_pie.md 12.15). In the
// pack it takes foods only (owner ruling R5, plan_ready_now_1123.md section 0): drinks (the jar's tag already holds
// #solraspberrypie:drinks) and edible non-foods are excluded: hazards (rotten flesh, spider eye, poisonous potato,
// pufferfish and the modded ones), no-diet items, lunchboxes, pet food, copper nuggets and Create Central Kitchen's
// unfinished assembly items (edible, outside #creativedelight:food_type in the food dump).
var i = global.item

ServerEvents.tags('item', e => {
	global.tags.add(e, 'solraspberrypie:diet_log_excluded', ['#creativedelight:food_type/drink', '#creativedelight:food_type/hazard',
		'#solraspberrypie:no_diet', i.lunchbag, i.lunchbox, i.golden_lunchbox, i.netherite_lunchbox, i.cat_food, i.nugget_copper_ec,
		i.incomplete_apple_pie, i.incomplete_bacon_sandwich, i.incomplete_cherry_pie, i.incomplete_chicken_sandwich,
		i.incomplete_chorus_flower_pie, i.incomplete_chorus_fruit_pie, i.incomplete_egg_sandwich, i.incomplete_hamburger,
		i.incomplete_mulberry_pie, i.incomplete_mutton_wrap, i.incomplete_pumpkin_pie, i.incomplete_sweet_berry_cheesecake,
		i.incomplete_truffle_pie])
})
