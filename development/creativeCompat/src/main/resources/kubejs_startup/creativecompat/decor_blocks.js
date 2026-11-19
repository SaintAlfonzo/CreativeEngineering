// Creative Compat: decor blocks with no system of their own (feature_writeup/creative_split.md 7.6). Item dict key
// weathered_cardboard_box. KubeJS always generates a cube_all block model at block/<id>, and its generated pack beats
// this jar's assets, so the box's own model ships under another name (block/weathered_cardboard_box_wrapped: Design
// and Decor's cardboard_box/block.json geometry with the weathered texture) and the blockstate is set here.

StartupEvents.registry('block', e => {
	// Weathered Cardboard Box: stats of Dustrial Decor's cardboard box (strength 1.0, blast resistance 0.3,
	// axe). Walk speed (soft tier) and the visual sink come from blocktags.js. Texture is the weathered
	// recolor of Design and Decor's box sheet (textureswapper/cardboard).
	var box = e.create('creativecompat:weathered_cardboard_box')
		.displayName('Weathered Cardboard Box')
		.material('wood')
		.soundType('wool')
		.hardness(1.0)
		.resistance(0.3)
		.model('creativecompat:block/weathered_cardboard_box_wrapped')
	// Upright, as Design and Decor's box placed facing north: tape on top, handles north and south
	box.blockstateJson = {variants: {'': {model: 'creativecompat:block/weathered_cardboard_box_wrapped', x: 90}}}
	// Mining tag (axe): Creative Compat's tags/blocktags.js
})
