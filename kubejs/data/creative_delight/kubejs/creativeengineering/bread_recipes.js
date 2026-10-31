// Any bread in any bread recipe (TODO baking/baked-goods #28). Every loaded recipe slot that takes vanilla bread or a
// bread slice (minecraft:bread, #forge:bread/wheat, #forge:bread_slices, the slice item) takes any bread
// (#forge:bread: the loaves, the flavored breads, bread slice and toast; creativeengineering/bread_tags.js). No burger,
// bun or square-slice restrictions. A slot that names toast alone (food with toast in its name) does not hold the
// slice, so it stays toast. Skipped, so the wider tag cannot copy items: recipes that make a bread or a slice (slicing,
// toasting, garlic bread, the dough bakes), bread blocks, and cans (a can opens back into its bread).
// Creative Delight runs after every pack script, so this walks the loaded data recipes of every mod; the pack's own
// script-added bread recipes are written with i.t_bread where they are made.
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {
		var keep = [
			{ output: i.t_bread }, { output: i.t_bread_loaf }, { output: i.bread_slice }, { output: i.toast },
			{ output: i.bread_block }, { output: i.crustless_bread_block }, { output: i.bread_slab }, { output: i.bread_nub },
			{ output: i.t_cans },
		]
		;[i.minecraft_bread, i.bread_slice].forEach(old => {
			R.replaceInput({ input: old, not: keep }, old, i.t_bread, e)
		})
	})
})
