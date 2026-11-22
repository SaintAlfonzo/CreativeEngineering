// requires: creativeengineering
// requires: jei
// requires: immersiveengineering
// SPDX-License-Identifier: GPL-3.0-only
// JEI information pages for the Immersive Engineering Sawmill blades (TODO engineering/sawmill-and-components #3, owner
// ruling D5 in plan_easy_fixes_1122.md; engineering.md F2). Recipes: data/creativeengineering/kubejs/wood/wood_types.js
// (blade field), precision sawblade blueprints: engineering/blueprints.js. JEI shows every Profile of the precision
// sawblade as one item, so it gets one page listing them all.
JEIEvents.information(e => {
	var i = global.item

	e.addItem(i.sawblade, [
		Text.of('Goes in the Sawmill.'),
		Text.of('Cuts logs and wood into 6 planks, with bark when it strips them and sawdust; stripped logs and wood into 6 planks; planks into 2 slabs.'),
		Text.of('Every other plank shape needs a precision sawblade.'),
	])
	e.addItem(i.precision_sawblade, [
		Text.of('Goes in the Sawmill in place of the sawblade. Each one is set to one shape and cuts planks only into that shape: stairs, fences, fence gates, doors, trapdoors, ladders, buttons, pressure plates, signs, railings, vertical planks or vertical slabs.'),
		Text.of('Made at the Engineer\'s Workbench from a blueprint: a sawblade, one wooden block of the shape and 2 steel plates.'),
		Text.of('It wears out and cannot be repaired.'),
	])
})
