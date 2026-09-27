
ServerEvents.recipes(e => {

console.log('Hello! The pneumatic event has started!')

const CE = global.CE
const R = global.NCH

// Compressed iron to steel swap
R.remove({id: 'pneumaticcraft:compressed_iron_ingot_from_block'}, e)
R.remove({id: 'pneumaticcraft:compressed_iron_block_from_ingot'}, e)
R.replaceInput(
  { mod: 'pneumaticcraft' },
  'pneumaticcraft:ingot_iron_compressed',
  'immersiveengineering:ingot_steel', e
)
/*CE.replaceInput(
  { mod: 'pneumaticcraft' },
  '#forge:ingots/compressed_iron',
  '#forge:ingots/steel', e
)*/
R.replaceInput(
  { input: 'immersiveengineering:storage_steel' },
  'immersiveengineering:storage_steel',
  'pneumaticcraft:compressed_iron_block', e
)
R.replaceOutput(
  { output: 'immersiveengineering:storage_steel' },
  'immersiveengineering:storage_steel',
  'pneumaticcraft:compressed_iron_block', e
)

R.remove({id: 'pneumaticcraft:explosion_crafting/compressed_iron_ingot'}, e)
R.remove({id: 'pneumaticcraft:pressure_chamber/compressed_iron_ingot'}, e)
//CE.pcExplosionCrafting('immersiveengineering:ingot_steel', 'minecraft:iron_ingot', 80, e)
R.pcPressureChamber('immersiveengineering:ingot_steel', 'minecraft:iron_ingot', 6, e)
R.remove({id: 'pneumaticcraft:explosion_crafting/compressed_iron_block'}, e)
//CE.pcExplosionCrafting('pneumaticcraft:compressed_iron_block', 'minecraft:iron_block', 80, e)
R.remove({id: 'pneumaticcraft:pressure_chamber/compressed_iron_block'}, e)
R.pcPressureChamber('pneumaticcraft:compressed_iron_block', 'minecraft:iron_block', 6, e)

//Guide book
R.remove({id: 'pneumaticcraft:patchouli_book_crafting'}, e)
R.shapeless(Item.of('patchouli:guide_book',1, {'patchouli:book':'pneumaticcraft:book'}),['minecraft:book','immersiveengineering:ingot_steel'], e)
    
console.log('The pneumatic event has finished!')
})
