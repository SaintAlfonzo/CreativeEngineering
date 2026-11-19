// Creative Delight startup script, loaded from the jar after the pack's startup_scripts, so the dicts exist here.
// Restart to apply.
//
// Fats and oils (TODO dishes/ingredients #10, plan_food_batch_1119.md wave D, R12, R19): Blubber, Whale Oil and the
// whale oil fluid. Both items read another mod's sprite in place (no copied file): Blubber is Delightful's animal fat,
// Whale Oil Rustic Delight's cooking oil bottle; the fluid uses Create Crafts & Additions' seed oil textures, untinted.
// Recipes, drop and tags: data creativedelight/kubejs/creativedelight/fats.js, behind the [fats] switches
// (global.CD.fats). The items always register, so a switch can be turned back on without a missing-item save.
var reg = global.reg.forPack('creativedelight')
StartupEvents.registry('item', e => {
	var i = global.item
	reg.item(e, i.blubber).displayName('Blubber').texture('delightful:item/animal_fat')
	reg.item(e, i.whale_oil).displayName('Whale Oil').texture('rusticdelight:item/cooking_oil').maxStackSize(16)
		.containerItem(i.glass_bottle)
})

StartupEvents.registry('fluid', e => {
	var f = global.fluid
	var oil = reg.fluid(e, f.whale_oil).displayName('Whale Oil').noBucket().noBlock()
	if (Platform.isLoaded('createaddition')) {
		oil.stillTexture('createaddition:fluid/seed_oil_still').flowingTexture('createaddition:fluid/seed_oil_flow')
	}
})
