// Creative Delight scratch test suite, client half (SQ-CDT): a client script from the CD tree (folder or jar).
console.info('[cdtest] client suite loaded, global.CD ' + typeof global.CD)
ItemEvents.tooltip(e => {
	e.add(global.item.fishing_net, '[cdtest] tooltip from the Creative Delight client tree')
})
