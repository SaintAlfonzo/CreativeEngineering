// Creative Engineering; moved from kubejs/server_scripts/engineering/laser_cutting.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
ServerEvents.recipes(e => {

console.log('Hello! The laser event has started!')

const R = global.NCH
var i = global.item;

//Bottles
//Laser cutting
R.viLaserCutting(Item.of(i.glass_bottle, 3),i.glass,e, 'glass_bottle_laser_cutting',4000, 100)

console.log('The laser event has finished!')
})
