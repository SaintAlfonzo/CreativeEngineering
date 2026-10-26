// JEI information pages: mechanics that are block or entity interactions, not recipes (jei_coverage.md 4.11).
// The pack's first info pages; add others here.
JEIEvents.information(e => {
	var i = global.item

	// Fungi Delight port (fungi_delight.md 5.1.1, step 18)
	e.addItem(Ingredient.of([i.brown_mushroom, i.red_mushroom, i.inky_cap_mushroom, i.morel_mushroom]), [
		Text.of('Mushroom colonies'),
		Text.of('A mushroom placed on rich soil slowly grows into a colony.'),
	])
	e.addItem(i.truffle, [
		Text.of('Pigs dig now and then in podzol, mud, rooted dirt and truffle dirt. Truffle dirt gives truffles; the other blocks give a truffle, stick, bone meal or clay ball. The dug block turns into coarse dirt. Pigs do not dig when mob griefing is off.'),
		Text.of('Use a truffle on a tamed wolf (it is not used up): for 2 minutes the wolf looks for truffle dirt nearby and scratches the ground above it.'),
		Text.of('Pigs bred with truffles sometimes have extra piglets.'),
	])
	e.addItem(Ingredient.of([i.cleaned_cap, i.cooked_cleaned_cap, i.inky_caps_salad, i.inky_cap_scrambled_eggs]), [
		Text.of('Causes Weak Stomach: drinking alcohol or a potion while it lasts gives Nausea, Poison and Weakness.'),
	])
})
