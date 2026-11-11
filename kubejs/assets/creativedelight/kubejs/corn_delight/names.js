// requires: creativedelight
// requires: corn_delight
// Corn Delight names (TODO farming/crops #47): the corn item is called Corn, not Maize; the id stays corn_delight:corn.
// The mod's lang file ships "Maize" for it. The advancement lines are dropped by the pack (no advancements).
ClientEvents.lang('en_us', e => {
	if (!global.CD || !global.CD.on('corn_delight')) return
	e.renameItem(global.item.corn, 'Corn')
})
