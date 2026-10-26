// requires: fruitsdelight
// Fruits Delight recipe changes. Non-member file: recipe ids are creative_delight:<type>/<suffix> (global.CD.recipes).
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Cooked fruit #########################
		// Dried or baked fruit is made in all three of smoker, furnace and campfire (food_families.md 3.1
		// `fruit/cooked`). Fruits Delight ships smoker and campfire only; the furnace takes the smoker's input and xp
		// at twice its time.
		R.smelting(i.baked_durian, i.durian_flesh, e, 'baked_durian_from_durian_flesh', 0.1, 200)
		R.smelting(i.baked_pear, i.t_fruits_pear, e, 'baked_pear_from_pear', 0.1, 200)
		R.smelting(i.dried_persimmon, i.t_fruits_persimmon, e, 'dried_persimmon_from_persimmon', 0.1, 200)

		//######################### 1.1.3 carry-overs (fruits_delight.md 2.2) #########################
		// Fig Pudding takes any egg
		R.replaceInput({ id: 'fruitsdelight:fig_pudding' }, i.minecraft_egg, i.eggs, e)
	})
})
