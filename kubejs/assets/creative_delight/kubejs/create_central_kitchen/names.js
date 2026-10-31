// requires: create_central_kitchen
// Create Central Kitchen names. The truffle pie item is the pack's (CD kubejs_startup/create_central_kitchen/truffle_pie_item.js);
// CCK's lang has no name for the block.
ClientEvents.lang('en_us', e => {
	if (!global.CD || !global.CD.on('create_central_kitchen')) return
	e.renameItem(global.item.truffle_pie, 'Truffle Pie')
})
