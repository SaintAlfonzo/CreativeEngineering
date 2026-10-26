// requires: rusticdelight
// Rustic Delight 1.7.0 tag rows (feature_writeup/rustic_delight.md 5): the six new pepper colours join the pack's
// roasted pepper and cooked veggie tags beside the three base colours (tags/cookingtags.js).
ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('rusticdelight')) return
	var i = global.item
	var t = global.tags
	var roasted = ['orange', 'white', 'pink', 'blue', 'purple', 'black'].map(c => i['roasted_bell_pepper_' + c])
	t.add(e, i.t_roasted_bell_pepper, roasted)
	t.add(e, i.t_soup_veggie_cooked, roasted)
})
