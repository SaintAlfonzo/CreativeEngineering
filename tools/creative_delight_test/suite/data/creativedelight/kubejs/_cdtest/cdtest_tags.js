// Scratch test suite (SQ-CDT): the tag helper (moved into patcherMod's jar) called from a CD server script.
ServerEvents.tags('item', e => {
	global.tags.add(e, 'creativedelight:cdtest/members', [global.item.fishing_net, global.item.wheat])
})
