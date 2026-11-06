// Creative Engineering; moved from kubejs/server_scripts/engineering/vibrating.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
ServerEvents.recipes(e => {

console.log('Hello! The vibrating event has started!')

var i = global.item
var R = global.NCH

//Sand to shells and heart of the sea
R.viVibrating([
	{ item: i.tangerine_seashell, count: 1, chance: 0.225 },
	{ item: i.bronzed_seashell, count: 1, chance: 0.225 },
	{ item: i.opaline_seashell, count: 1, chance: 0.225 },
	{ item: i.roseate_seashell, count: 1, chance: 0.225 },
	{ item: i.nautilus_shell, count: 1, chance: 0.09 },
	{ item: i.heart_of_the_sea, count: 1, chance: 0.01 }
], i.sand, e, 'sand_vibrating_to_shells', 300)

console.log('The vibrating event has finished!')
})
