// Creative Engineering.
var reg = global.reg.forPack('creativeengineering')
StartupEvents.registry('item', e => {
	reg.item(e, 'wood_pellets')
		.displayName('Wood Pellets')
		.burnTime(3200)
})
