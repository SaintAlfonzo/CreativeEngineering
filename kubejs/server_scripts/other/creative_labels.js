// Creative Labels (development/creativeLabels, feature_writeup/sticky_notes.md 3.6 and 3.13): the pack's own sticky
// notes and labels, one face_paper block. The donor jars (Sticky It!, Labels) stay installed for their art until
// phase 5 with their gameplay off: every recipe of theirs goes here, their three items are hidden from JEI and
// flagged (removed_items.js) and kept out of the creative tabs (the mod's ItemHiddenMixin). The donors' recipes
// come back below making our items, plus the four Java special recipes (dye, special, copy, erase), whose JSON the
// mod does not ship (NCH creativeLabelsSpecial).
//
//   sticky note  4 paper + 3 iron nuggets -> 2     (Sticky It's pattern)
//   eraser       7 dried kelp                      (Sticky It's pattern)
//   label        8 paper around antique ink -> 16  (Labels' Supplementaries pattern)

ServerEvents.recipes(e => {
	console.log('creative labels starting')

	var i = global.item
	var R = global.NCH

	// ============================================================
	// Donor recipes off (3.13)
	// ============================================================
	R.remove({ mod: 'sticky_it' }, e)
	R.remove({ mod: 'labels' }, e)

	// ============================================================
	// The donors' recipes, now making our items (3.6)
	// ============================================================
	R.shaped(Item.of(i.cl_sticky_note, 2), [' aa', 'bba', 'bb '], { a: i.nugget_iron, b: i.paper }, e, 'sticky_note')
	R.shaped(Item.of(i.cl_eraser), [' aa', 'aaa', 'aa '], { a: i.dried_kelp }, e, 'eraser')
	R.shaped(Item.of(i.cl_label, 16), ['aaa', 'aba', 'aaa'], { a: i.paper, b: i.antique_ink }, e, 'label')

	// ============================================================
	// Note special recipes (Java CustomRecipes, matched in code; 3.6)
	// ============================================================
	R.creativeLabelsSpecial('note_dye', e)
	R.creativeLabelsSpecial('note_special', e)
	R.creativeLabelsSpecial('note_copy', e)
	R.creativeLabelsSpecial('note_erase', e)

	console.log('creative labels done')
})
