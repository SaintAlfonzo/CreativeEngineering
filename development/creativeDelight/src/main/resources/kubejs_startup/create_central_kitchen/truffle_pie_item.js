// requires: create_central_kitchen
// Truffle Pie item (fungi_delight.md 5.1.6, TODO baking/baked-goods). Create Central Kitchen registers the truffle pie
// block with no item: the placeable pie comes from Environmental, which the pack lacks. This registers a block item
// for CCK's block under CCK's id, so the oven bakes it from Fungi Delight's truffle (CD creativedelight/baking.js)
// and the placed pie is eaten by the slice as CCK's other pies. Restart to apply.
// Art: placeholder, Farmer's Delight's chocolate pie sprite (assets/create_central_kitchen/models/item/truffle_pie.json);
// own art is TODO largecontent/mod-additions (Environmental integration). Name: assets kubejs/create_central_kitchen/names.js.
StartupEvents.registry('item', e => {
	var $BlockItem = Java.loadClass('net.minecraft.world.item.BlockItem')
	var $Properties = Java.loadClass('net.minecraft.world.item.Item$Properties')
	var $CreativeModeTab = Java.loadClass('net.minecraft.world.item.CreativeModeTab')
	var $ForgeRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	var ID = 'create_central_kitchen:truffle_pie'
	e.createCustom(ID, () => new $BlockItem(
		$ForgeRegistries.BLOCKS.getValue(new ResourceLocation(ID)),
		new $Properties().tab($CreativeModeTab.TAB_FOOD)))
})
