// SPDX-License-Identifier: GPL-3.0-only
// Creative Delight startup script, loaded from the jar after the pack's startup_scripts, so the dicts exist here.
// Restart to apply.
//
// Produce items (plan_unblocked_batch_1121.md P2):
// - Potato Dough (TODO dishes/ingredients #27): Veggies Delight's sweet potato dough made with potatoes; a knife cuts it
//   into 2 gnocchi. Plain item like the sweet potato dough.
// - Tofu (TODO dishes/ingredients #17): Cooking Pot, 2 overworld mushrooms + nut milk; stands in for the cooked
//   vegetarian patty (#creativedelight:veggie_patty). Food values as the cooked vegetarian patty (6, 0.8).
// Both read another mod's sprite in place until the owner picks a palette for their own (no copied or edited file):
// potato dough Farmer's Delight's wheat dough, tofu Delightful's animal fat (the owner's named source for its recolor).
// Recipes and tags: data creativedelight/kubejs/veggiesdelight/recipes.js (dough), creativedelight/patties.js (tofu).
var reg = global.reg.forPack('creativedelight')
StartupEvents.registry('item', e => {
	var i = global.item
	reg.item(e, i.potato_dough).displayName('Potato Dough').texture('farmersdelight:item/wheat_dough')
	reg.item(e, i.tofu).displayName('Tofu').texture('delightful:item/animal_fat')
		.food(food => { food.hunger(6); food.saturation(0.8) })
})
