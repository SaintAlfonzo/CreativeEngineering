// requires: creativeequipment
// Creative Equipment; moved from kubejs/server_scripts/other/backpack_skins.js on 2026-11-04 (creative_split.md 7.6, wave 7), rows unchanged.
// Recipe ids are creativeequipment:<type>/<suffix> (global.CEQ.recipes).
// Backpack skins: smith a block from #creativeequipment:backpack_skins onto a Sophisticated Backpack to change its
// look; craft a skinned backpack alone to take the skin off. Code: patcherMod backpackskin/ and mixin/BackpackSkin*.
// Doc and tests: feature_writeup/backpack_skins.md. Add one block at a time, after checking its look in game
// (blocks drawn by their own renderer, such as chests, draw as nothing).
const BACKPACK_SKIN_TAG = 'creativeequipment:backpack_skins'

ServerEvents.tags('item', e => {
	global.tags.add(e, BACKPACK_SKIN_TAG, [global.item.basket])
})

ServerEvents.recipes(e => {
global.CEQ.recipes(() => {
	global.NCH.backpackSkin(BACKPACK_SKIN_TAG, e)
	global.NCH.backpackSkinClear(e)
})
})
