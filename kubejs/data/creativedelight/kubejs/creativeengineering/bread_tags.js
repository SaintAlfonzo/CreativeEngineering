// requires: creativedelight
// Bread tags. #forge:bread (i.t_bread) is "any bread" for every recipe that asks for a general bread: loaves, the
// flavored breads, bread slices and plain toast all count. Filled breads (honey, mulberry, glow, jelly) are not bread:
// they have a filling. Specific breads keep their own recipes (sourdough and the
// vanilla-only conversions such as golden bread), and a food with toast in its name needs
// toast (cheese toast, caviar toast, garlic bread, toast with ...), which are not in the tag.
// #creativeengineering:bread_loaves (i.t_bread_loaf) is the plain loaves only, for recipes that turn a bread into more
// bread (garlic bread, slicing): with the flavored breads in #forge:bread those would copy items.
// Mods add their own loaves to #forge:bread (vanilla, corn, cauliflower, sourdough); this adds the rest.
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item

	var general = [ // G5 begin (filtered by mod, then global.tags)
		i.garlic_bread, i.banana_bread, i.cantaloupe_bread, i.bread_slice, i.toast,
	] // G5 end
	t.add(e, i.t_bread, general.filter(id => Platform.isLoaded(String(id).split(':')[0])))

	t.add(e, i.t_bread_loaf, ['#forge:bread/wheat', '#forge:bread/corn', '#forge:bread/cauliflower', i.sourdough_bread])
})
