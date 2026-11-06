// Creative Engineering; moved from kubejs/server_scripts/engineering/pneumatic.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
ServerEvents.recipes(e => {

console.log('Hello! The pneumatic event has started!')

var i = global.item
var R = global.NCH

// Compressed iron to steel swap
R.remove({id: 'pneumaticcraft:compressed_iron_ingot_from_block'}, e)
R.remove({id: 'pneumaticcraft:compressed_iron_block_from_ingot'}, e)
R.remove({id: 'immersiveengineering:crafting/storage_steel_to_ingot_steel'}, e)
R.remove({id: 'immersiveengineering:crafting/ingot_steel_to_storage_steel'}, e)
R.shapeless(Item.of(i.block_steel, 1), [Item.of(i.ingot_steel, 9)], e)
R.shapeless(Item.of(i.ingot_steel, 9), [Item.of(i.block_steel, 1)], e)
R.replaceInput(
  { id: 'immersiveengineering:crafting/storage_steel_to_slab' },
  i.ie_block_steel,
  i.block_steel, e
)
R.replaceOutput(
  { id: 'immersiveengineering:crafting/storage_steel_from_slab' },
  i.ie_block_steel,
  i.block_steel, e
)

R.remove({id: 'pneumaticcraft:explosion_crafting/compressed_iron_ingot'}, e)
R.remove({id: 'pneumaticcraft:pressure_chamber/compressed_iron_ingot'}, e)
R.pcPressureChamber(i.ingot_steel, i.ingot_iron, 6, e)
R.remove({id: 'pneumaticcraft:explosion_crafting/compressed_iron_block'}, e)
R.remove({id: 'pneumaticcraft:pressure_chamber/compressed_iron_block'}, e)
R.pcPressureChamber(i.block_steel, i.block_iron, 6, e)

//Guide book
R.remove({id: 'pneumaticcraft:patchouli_book_crafting'}, e)
R.shapeless(Item.of(i.guide_book, 1, {'patchouli:book': 'pneumaticcraft:book'}), [i.book, i.ingot_steel], e)

console.log('The pneumatic event has finished!')
})
