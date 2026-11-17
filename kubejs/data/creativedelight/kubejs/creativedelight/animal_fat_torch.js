// requires: creativedelight
// The animal-fat torch: Delightful's animal oil bottle torch is replaced by 12 torches from any animal fat over a
// stick. Non-member file: recipe ids are creativedelight:<type>/<suffix> (global.CD.recipes; was creativeengineering:).
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH.forPack('creativedelight')

	global.CD.recipes(() => {
		if (global.CD.on('delightful')) R.remove({ id: 'delightful:torch_from_animal_oil_bottle' }, e)
		R.shaped(Item.of(i.torch, 12), ['f', 's'], { s: i.stick, f: i.t_animal_fat }, e, 'torch_crafting_animal_fat')
	})
})
