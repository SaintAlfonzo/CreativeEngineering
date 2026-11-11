// requires: creativedelight
// requires: goldenfood
// Golden foods (food.md 3.16, golden_food.md 3.4): every goldenfood golden food is its input food in a ring of 8 gold
// nuggets (global.CD.food goldenGild, which also adds the heated mixer and Arc Furnace routes), replacing upstream's
// ring of 8 gold ingots (owner 2026-11-02, TODO food/types-and-tuning #22). The golden carrot's call is in
// minecraft/golden.js (vanilla id).
// Gilded (enchanted golden) foods (food_families.md `golden/enchanted`): the gilded apple's Create sequenced assembly
// (goldenEnchant: 1 gold ingot, 125 mB liquid experience, press, 8 loops; owner 2026-11-03) on every other golden
// food. Replaces upstream's anvil recipe with the Golden Food Enchantment book, which is removed (owner, 2026-11-02;
// creativedelight members #50). The golden cake has no gilded recipe (upstream had none either).
// Each output's incomplete item is a row in startup_scripts/changes/transition_items.js.
ServerEvents.recipes(e => {
	if (!global.CD) return

	const i = global.item

	global.CD.recipes(() => {
		var F = global.CD.food(e)

		// input food -> golden food, 8 gold nuggets (forge tag; rose gold counts, as vanilla's golden carrot)
		var gild = (food, golden) => F.goldenGild(food, golden, i.any_nugget_gold)
		gild(i.melon, i.golden_melon_slice)
		gild(i.sweet_berries, i.golden_sweet_berries)
		gild(i.glow_berries, i.golden_glow_berries)
		gild(i.chorus_fruit, i.golden_chorus_fruit)
		gild(i.baked_potato, i.golden_baked_potato)
		gild(i.minecraft_beetroot, i.golden_beetroot)
		gild(i.dried_kelp, i.golden_dried_kelp)
		gild(i.beef_cooked, i.golden_cooked_beef)
		gild(i.porkchop_cooked, i.golden_cooked_porkchop)
		gild(i.mutton_cooked, i.golden_cooked_mutton)
		gild(i.chicken_cooked, i.golden_cooked_chicken)
		gild(i.rabbit_cooked, i.golden_cooked_rabbit)
		gild(i.cod_cooked, i.golden_cooked_cod)
		gild(i.salmon_cooked, i.golden_cooked_salmon)
		gild(i.minecraft_bread, i.golden_bread)
		gild(i.cookie, i.golden_cookie)
		gild(i.pumpkin_pie, i.golden_pumpkin_pie)
		gild(i.minecraft_milk_bucket, i.golden_milk_bucket)
		gild(i.honey_bottle, i.golden_honey_bottle)
		gild(i.cake, i.golden_cake)

		// minecraft:golden_carrot (replaced by Golden Food) -> goldenfood:enchanted_golden_carrot
		F.goldenEnchant(i.golden_carrot, i.enchanted_golden_carrot)
		F.goldenEnchant(i.golden_melon_slice, i.enchanted_golden_melon_slice)
		F.goldenEnchant(i.golden_sweet_berries, i.enchanted_golden_sweet_berries)
		F.goldenEnchant(i.golden_glow_berries, i.enchanted_golden_glow_berries)
		F.goldenEnchant(i.golden_chorus_fruit, i.enchanted_golden_chorus_fruit)
		F.goldenEnchant(i.golden_baked_potato, i.enchanted_golden_baked_potato)
		F.goldenEnchant(i.golden_beetroot, i.enchanted_golden_beetroot)
		F.goldenEnchant(i.golden_dried_kelp, i.enchanted_golden_dried_kelp)
		F.goldenEnchant(i.golden_cooked_beef, i.enchanted_golden_cooked_beef)
		F.goldenEnchant(i.golden_cooked_porkchop, i.enchanted_golden_cooked_porkchop)
		F.goldenEnchant(i.golden_cooked_mutton, i.enchanted_golden_cooked_mutton)
		F.goldenEnchant(i.golden_cooked_chicken, i.enchanted_golden_cooked_chicken)
		F.goldenEnchant(i.golden_cooked_rabbit, i.enchanted_golden_cooked_rabbit)
		F.goldenEnchant(i.golden_cooked_cod, i.enchanted_golden_cooked_cod)
		F.goldenEnchant(i.golden_cooked_salmon, i.enchanted_golden_cooked_salmon)
		F.goldenEnchant(i.golden_bread, i.enchanted_golden_bread)
		F.goldenEnchant(i.golden_cookie, i.enchanted_golden_cookie)
		F.goldenEnchant(i.golden_pumpkin_pie, i.enchanted_golden_pumpkin_pie)
		F.goldenEnchant(i.golden_milk_bucket, i.enchanted_golden_milk_bucket)
		F.goldenEnchant(i.golden_honey_bottle, i.enchanted_golden_honey_bottle)
	})
})
