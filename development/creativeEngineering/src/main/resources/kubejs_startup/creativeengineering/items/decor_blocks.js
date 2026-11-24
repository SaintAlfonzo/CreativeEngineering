// Creative Engineering: decor blocks with no system of their own. Item dict key weathered_cardboard_box.
// Back from Creative Compat (wave 1 ruling W16, creative-compat-publish #8); old creativecompat ids in saves
// turn into this one through data/creativeengineering/swaps/compat_return.json.
// KubeJS always generates a cube_all block model at block/<id>, and its generated pack beats this jar's assets, so the
// box's own model ships under another name (block/weathered_cardboard_box_wrapped: Design and Decor's
// cardboard_box/block.json geometry with the weathered texture) and the blockstate is set here.

var reg = global.reg.forPack('creativeengineering')
StartupEvents.registry('block', e => {
	// Weathered Cardboard Box: stats of Dustrial Decor's cardboard box (strength 1.0, blast resistance 0.3,
	// axe). Walk speed (soft tier) and the visual sink come from the pack's blocktags.js. Texture is the weathered
	// recolor of Design and Decor's box sheet (textureswapper/cardboard).
	var box = reg.block(e, 'weathered_cardboard_box')
		.displayName('Weathered Cardboard Box')
		.material('wood')
		.soundType('wool')
		.hardness(1.0)
		.resistance(0.3)
		.model('creativeengineering:block/weathered_cardboard_box_wrapped')
	// Upright, as Design and Decor's box placed facing north: tape on top, handles north and south
	box.blockstateJson = {variants: {'': {model: 'creativeengineering:block/weathered_cardboard_box_wrapped', x: 90}}}
	// Mining tag (axe): kubejs/data/creativeengineering/kubejs/tags/
})
