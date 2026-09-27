ServerEvents.recipes(e => {

console.log('Hello! The music event has started!')

const R = global.NCH
var i = global.item;

// ############################## Note Block ##############################
//Amethyst shard recipe
R.remove({id:'minecraft:note_block'}, e)
R.shaped(Item.of(i.noteblock, 1), ['AAA','ABA', 'ACA'], {A: i.planks, B: i.amethyst_shard, C: i.redstone}, e)
R.ieBlueprint(Item.of(i.noteblock, 1), [i.amethyst_shard, i.redstone, i.planks, i.planks, i.planks], 'redstonecomponents', e)
 
// ############################## Juke Box ##############################
//New noteblock recipe
R.remove({id:'minecraft:jukebox'}, e)
R.shaped(Item.of(i.jukebox, 1), ['AAA','ABA', 'ACA'], {A: i.planks, B: i.diamond, C: i.noteblock}, e)
R.ieBlueprint(Item.of(i.jukebox, 1), [i.diamond, i.noteblock, i.planks, i.planks, i.planks], 'redstonecomponents', e)
 
// ############################## Radio ##############################
// Add transmitter to radio
R.replaceInput(
  { id: 'etched:radio' },
  i.ingot_copper,
  i.transmitter, e
)
 
// ############################## Speaker ##############################
R.remove({id:'supplementaries:speaker_block'}, e)
R.remove({id:'create_things_and_misc:brass_speaker_craft'}, e)
R.shaped(Item.of(i.speaker, 2), [' C ','FMF', ' C '], {C: i.railway_casing, F: i.cr_filter, M: i.vibration_mechanism}, e)

console.log('The music event has finished!')
})
