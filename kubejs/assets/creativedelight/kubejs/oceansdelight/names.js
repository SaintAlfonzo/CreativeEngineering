// requires: creativedelight
// requires: oceansdelight
// Ocean's Delight names. The whole guardian a guardian drops is the Guardian Shell (dishes/ocean #11): it is cut into
// raw guardian fillets (oceansdelight/recipes.js). The id stays oceansdelight:guardian, the sprite too.
ClientEvents.lang('en_us', e => {
	if (!global.CD || !global.CD.on('oceansdelight')) return
	e.renameItem(global.item.guardian_shell, 'Guardian Shell')
})
