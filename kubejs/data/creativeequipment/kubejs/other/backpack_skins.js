// requires: creativeequipment
// Creative Equipment. Recipe ids are creativeequipment:<type>/<suffix> (global.CEQ.recipes). Backpack skins: smith a
// block from #creativeequipment:backpack_skins onto a Sophisticated Backpack to change its look; craft a skinned
// backpack alone to take the skin off. Code: patcherMod backpackskin/ and mixin/BackpackSkin*. Doc and tests:
// feature_writeup/backpack_skins.md. Add one block at a time, after checking its look in game (blocks drawn by their
// own renderer, such as chests, draw as nothing).
const BACKPACK_SKIN_TAG = 'creativeequipment:backpack_skins'

ServerEvents.tags('item', e => {
	global.tags.add(e, BACKPACK_SKIN_TAG, [global.item.basket])
})

ServerEvents.recipes(e => {
global.CEQ.recipes(() => {
	var R = global.NCH.forPack('creativeequipment')
	R.backpackSkin(BACKPACK_SKIN_TAG, e)
	R.backpackSkinClear(e)
})
})
