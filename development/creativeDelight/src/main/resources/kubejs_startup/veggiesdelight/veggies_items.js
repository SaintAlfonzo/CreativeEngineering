// requires: veggiesdelight
// Veggies Delight item changes. Creative Delight startup script, loaded from the jar after the pack's startup_scripts,
// so global.item and global.removedItems exist here. Restart to apply.

// Mhadjeb is out (TODO dishes/meals #32; ends veggies_delight.md 5.2's "until dough unification"): hidden in JEI, tags
// stripped; its recipes are removed in data/creativedelight/kubejs/veggiesdelight/recipes.js.
global.removedItems.push(global.item.uncooked_mhadjeb, global.item.mhadjeb)

// Stuffed Bell Peppers out (TODO merges/dishes #3, veggies_delight.md 5.1 M3): the feast and its serving; only Rustic
// Delight's stuffed bell peppers stay. Hidden in JEI, tags stripped; the feast recipe is removed in
// data/creativedelight/kubejs/veggiesdelight/recipes.js.
global.removedItems.push(global.item.stuffed_bellpeppers_block, global.item.stuffed_bellpepper)

// Sliced Sweet Potato (TODO dishes/ingredients #26): a sweet potato cuts into 3 on the cutting board and the Slice & Dice
// slicer, as a potato into 3 Rustic Delight potato slices (recipe: data/creativedelight/kubejs/veggiesdelight/recipes.js).
// It fills the sliced potato tags More Delight's dishes and the pack's frying read (veggiesdelight/tags.js there).
// Food values copy the potato slices: 1 hunger, modifier 0.3. No own art: the model reads Rustic Delight's potato slices
// sprite in place (not copied; Rustic is ARR), so the sprite comes with the Rustic member.
StartupEvents.registry('item', e => {
	e.create(global.item.sliced_sweet_potato).displayName('Sliced Sweet Potato').texture('rusticdelight:item/potato_slices')
		.food(f => {
			f.hunger(1).saturation(0.3)
		})
})
