// requires: creativeengineering
// Creative Engineering; moved from kubejs/server_scripts/engineering/computing.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
ServerEvents.recipes(e => {

console.log('Hello! The computing event has started!')

const R = global.NCH
var i = global.item;

// ############################## CC ##############################  

//Speaker craft change
R.replaceInput(
  { id: 'computercraft:speaker' },
  i.noteblock,
  i.speaker, e
)
 
console.log('The computing event has finished!')
})

