// requires: creativeengineering
// Creative Engineering.
ServerEvents.recipes(e => {

console.log('Hello! The computing event has started!')

const R = global.NCH.forPack('creativeengineering')
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

