// requires: creativedelight
// requires: hostiledelight
// Rotten meat (TODO largecontent/mod-additions #47, owner 2026-11-04): one item tag for everything rotten and meaty.
// Rotten flesh is added in minecraft/tags.js. The hams are food type hazard (generated food_types.js), not raw or cooked meat.
ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('hostiledelight')) return
	var i = global.item
	global.tags.add(e, 'forge:rotten_meat', [i.rottenham, i.cookedrottenham])
})
