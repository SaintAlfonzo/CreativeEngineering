// requires: creativedelight
// requires: solapplepie
// requires: solraspberrypie
// Diet Log (Apple Pie's food book): Spice of Life: Raspberry Pie Addition crafts it from a book + any food that is not a
// drink (its own recipe, which also leaves book + carrot to the Food Dictionary), so Apple Pie's book + potato recipe
// goes. Book + any drink makes the Liquid Diet Log (Raspberry Pie). Doc: feature_writeup/raspberry_pie.md 7.
ServerEvents.recipes(e => {
	if (!global.CD) return
	global.CD.recipes(() => {
		global.NCH.remove({ id: 'solapplepie:food_book' }, e)
	})
})
