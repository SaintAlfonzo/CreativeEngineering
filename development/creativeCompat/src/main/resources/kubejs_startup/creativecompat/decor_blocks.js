// Creative Compat: decor blocks with no system of their own (feature_writeup/creative_split.md 7.6); moved from
// kubejs/startup_scripts/items/decor_blocks.js on 2026-11-01. The id stays in the creativeengineering namespace
// (item dict key weathered_cardboard_box); blockstate, models and texture ship in this jar under
// assets/creativeengineering/ (the on-disk files win over the generated blockstate).

StartupEvents.registry('block', e => {
	// Weathered Cardboard Box: stats of Dustrial Decor's cardboard box (strength 1.0, blast resistance 0.3,
	// axe). Walk speed (soft tier) and the visual sink come from blocktags.js. Texture is the weathered
	// recolour of Design and Decor's box sheet (textureswapper/cardboard).
	e.create('creativeengineering:weathered_cardboard_box')
		.displayName('Weathered Cardboard Box')
		.material('wood')
		.soundType('wool')
		.hardness(1.0)
		.resistance(0.3)
		.model('creativeengineering:block/weathered_cardboard_box')
		.tagBlock('minecraft:mineable/axe')
})
