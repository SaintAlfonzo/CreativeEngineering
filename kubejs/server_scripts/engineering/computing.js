ServerEvents.recipes(e => {

console.log('Hello! The computing event has started!')

const R = global.NCH
var i = global.item;

// ############################## CC ##############################  

//Speaker craft change
R.replaceInput(
  { id: 'computercraft:speaker' },
  i.note_block,
  i.speaker, e
)
 
console.log('The computing event has finished!')
})

