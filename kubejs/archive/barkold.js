ServerEvents.recipes(e=>{
console.log('StartingBarkServerScript')

const CE = global.CE

// Removing existing create log/wood cutting, replace with addition of bark
CE.remove({id:'create:cutting/oak_log'}, e)
CE.createSaw(['minecraft:stripped_oak_log',Item.of('immersive_weathering:oak_bark').withChance(0.5)],'minecraft:oak_log', e)
CE.remove({id:'create:cutting/oak_wood'}, e)
CE.createSaw(['minecraft:stripped_oak_wood',Item.of('immersive_weathering:oak_bark').withChance(0.75)],'minecraft:oak_wood', e)

CE.remove({id:'create:cutting/spruce_log'}, e)
CE.createSaw(['minecraft:stripped_spruce_log',Item.of('immersive_weathering:spruce_bark').withChance(0.5)],'minecraft:spruce_log', e)
CE.remove({id:'create:cutting/spruce_wood'}, e)
CE.createSaw(['minecraft:stripped_spruce_wood',Item.of('immersive_weathering:spruce_bark').withChance(0.75)],'minecraft:spruce_wood', e)

CE.remove({id:'create:cutting/birch_log'}, e)
CE.createSaw(['minecraft:stripped_birch_log',Item.of('immersive_weathering:birch_bark').withChance(0.5)],'minecraft:birch_log', e)
CE.remove({id:'create:cutting/birch_wood'}, e)
CE.createSaw(['minecraft:stripped_birch_wood',Item.of('immersive_weathering:birch_bark').withChance(0.75)],'minecraft:birch_wood', e)

CE.remove({id:'create:cutting/dark_oak_log'}, e)
CE.createSaw(['minecraft:stripped_dark_oak_log',Item.of('immersive_weathering:dark_oak_bark').withChance(0.5)],'minecraft:dark_oak_log', e)
CE.remove({id:'create:cutting/dark_oak_wood'}, e)
CE.createSaw(['minecraft:stripped_dark_oak_wood',Item.of('immersive_weathering:dark_oak_bark').withChance(0.75)],'minecraft:dark_oak_wood', e)

CE.remove({id:'create:cutting/acacia_log'}, e)
CE.createSaw(['minecraft:stripped_acacia_log',Item.of('immersive_weathering:acacia_bark').withChance(0.5)],'minecraft:acacia_log', e)
CE.remove({id:'create:cutting/acacia_wood'}, e)
CE.createSaw(['minecraft:stripped_acacia_wood',Item.of('immersive_weathering:acacia_bark').withChance(0.75)],'minecraft:acacia_wood', e)

CE.remove({id:'create:cutting/jungle_log'}, e)
CE.createSaw(['minecraft:stripped_jungle_log',Item.of('immersive_weathering:jungle_bark').withChance(0.5)],'minecraft:jungle_log', e)
CE.remove({id:'create:cutting/jungle_wood'}, e)
CE.createSaw(['minecraft:stripped_jungle_wood',Item.of('immersive_weathering:jungle_bark').withChance(0.75)],'minecraft:jungle_wood', e)

CE.remove({id:'create:cutting/mangrove_log'}, e)
CE.createSaw(['minecraft:stripped_mangrove_log',Item.of('immersive_weathering:mangrove_bark').withChance(0.5)],'minecraft:mangrove_log', e)
CE.remove({id:'create:cutting/mangrove_wood'}, e)
CE.createSaw(['minecraft:stripped_mangrove_wood',Item.of('immersive_weathering:mangrove_bark').withChance(0.75)],'minecraft:mangrove_wood', e)

CE.remove({id:'create:cutting/crimson_stem'}, e)
CE.createSaw(['minecraft:stripped_crimson_stem',Item.of('immersive_weathering:crimson_scales').withChance(0.5)],'minecraft:crimson_stem', e)
CE.remove({id:'create:cutting/crimson_hyphae'}, e)
CE.createSaw(['minecraft:stripped_crimson_hyphae',Item.of('immersive_weathering:crimson_scales').withChance(0.75)],'minecraft:crimson_hyphae', e)

CE.remove({id:'create:cutting/warped_stem'}, e)
CE.createSaw(['minecraft:stripped_warped_stem',Item.of('immersive_weathering:warped_scales').withChance(0.5)],'minecraft:warped_stem', e)
CE.remove({id:'create:cutting/warped_hyphae'}, e)
CE.createSaw(['minecraft:stripped_warped_hyphae',Item.of('immersive_weathering:warped_scales').withChance(0.75)],'minecraft:warped_hyphae', e)

CE.remove({id:'create:cutting/compat/architects_palette/twisted_log'}, e)
CE.createSaw(['architects_palette:stripped_twisted_log',Item.of('immersive_weathering:architects_palette/twisted_bark').withChance(0.5)],'architects_palette:twisted_log', e)
CE.remove({id:'create:cutting/compat/architects_palette/twisted_wood'}, e)
CE.createSaw(['architects_palette:stripped_twisted_wood',Item.of('immersive_weathering:architects_palette/twisted_bark').withChance(0.75)],'architects_palette:twisted_wood', e)

CE.remove({id:'create:cutting/compat/quark/blossom_log'}, e)
CE.createSaw(['quark:stripped_blossom_log',Item.of('immersive_weathering:quark/blossom_bark').withChance(0.5)],'quark:blossom_log', e)
CE.remove({id:'create:cutting/compat/quark/blossom_wood'}, e)
CE.createSaw(['quark:stripped_blossom_wood',Item.of('immersive_weathering:quark/blossom_bark').withChance(0.75)],'quark:blossom_wood', e)

CE.remove({id:'create:cutting/compat/quark/ancient_log'}, e)
CE.createSaw(['quark:stripped_ancient_log',Item.of('immersive_weathering:quark/ancient_bark').withChance(0.5)],'quark:ancient_log', e)
CE.remove({id:'create:cutting/compat/quark/ancient_wood'}, e)
CE.createSaw(['quark:stripped_ancient_wood',Item.of('immersive_weathering:quark/ancient_bark').withChance(0.75)],'quark:ancient_wood', e)

CE.remove({id:'create:cutting/compat/quark/azalea_log'}, e)
CE.createSaw(['quark:stripped_azalea_log',Item.of('immersive_weathering:quark/azalea_bark').withChance(0.5)],'quark:azalea_log', e)
CE.remove({id:'create:cutting/compat/quark/azalea_wood'}, e)
CE.createSaw(['quark:stripped_azalea_wood',Item.of('immersive_weathering:quark/azalea_bark').withChance(0.75)],'quark:azalea_wood', e)

CE.createSaw(['quark:stripped_bamboo_block',Item.of('immersive_weathering:quark/bamboo_bark').withChance(0.50)],'quark:bamboo_block', e)

// Add shapeless recipes to reattatch bark to logs
CE.shapeless(Item.of('minecraft:mangrove_log',1),['immersive_weathering:mangrove_bark','minecraft:stripped_mangrove_log'], e)
CE.shapeless(Item.of('minecraft:mangrove_wood',1),['immersive_weathering:mangrove_bark','minecraft:stripped_mangrove_wood'], e)
CE.shapeless(Item.of('minecraft:mangrove_wood',1),['immersive_weathering:mangrove_bark','minecraft:mangrove_log'], e, 'mangrove_log_reattatch')

CE.shapeless(Item.of('architects_palette:twisted_log',1),['immersive_weathering:architects_palette/twisted_bark','architects_palette:stripped_twisted_log'], e)
CE.shapeless(Item.of('architects_palette:twisted_wood',1),['immersive_weathering:architects_palette/twisted_bark','architects_palette:stripped_twisted_wood'], e)
CE.shapeless(Item.of('architects_palette:twisted_wood',1),['immersive_weathering:architects_palette/twisted_bark','architects_palette:twisted_log'], e, 'twisted_log_reattatch')

CE.shapeless(Item.of('quark:blossom_log',1),['immersive_weathering:quark/blossom_bark','quark:stripped_blossom_log'], e)
CE.shapeless(Item.of('quark:blossom_wood',1),['immersive_weathering:quark/blossom_bark','quark:stripped_blossom_wood'], e)
CE.shapeless(Item.of('quark:blossom_wood',1),['immersive_weathering:quark/blossom_bark','quark:blossom_log'], e, 'blossom_log_reattatch')

CE.shapeless(Item.of('quark:ancient_log',1),['immersive_weathering:quark/ancient_bark','quark:stripped_ancient_log'], e)
CE.shapeless(Item.of('quark:ancient_wood',1),['immersive_weathering:quark/ancient_bark','quark:stripped_ancient_wood'], e)
CE.shapeless(Item.of('quark:ancient_wood',1),['immersive_weathering:quark/ancient_bark','quark:ancient_log'], e, 'ancient_log_reattatch')

CE.shapeless(Item.of('quark:azalea_log',1),['immersive_weathering:quark/azalea_bark','quark:stripped_azalea_log'], e)
CE.shapeless(Item.of('quark:azalea_wood',1),['immersive_weathering:quark/azalea_bark','quark:stripped_azalea_wood'], e)
CE.shapeless(Item.of('quark:azalea_wood',1),['immersive_weathering:quark/azalea_bark','quark:azalea_log'], e, 'azalea_log_reattatch')

CE.shapeless(Item.of('quark:bamboo_block',1),['immersive_weathering:quark/bamboo_bark','quark:stripped_bamboo_block'], e)

// Remove farmers delight log/wood cutting, replace with addition of specific bark
CE.remove({id:'farmersdelight:cutting/oak_log'}, e)
CE.remove({id:'farmersdelight:cutting/oak_wood'}, e)
CE.fdCutting('minecraft:oak_log',CE.AXE,['minecraft:stripped_oak_log',Item.of('immersive_weathering:oak_bark').withChance(0.50)], e)
CE.fdCutting('minecraft:oak_wood',CE.AXE,['minecraft:stripped_oak_wood',Item.of('immersive_weathering:oak_bark').withChance(0.75)], e)

CE.remove({id:'farmersdelight:cutting/spruce_log'}, e)
CE.remove({id:'farmersdelight:cutting/spruce_wood'}, e)
CE.fdCutting('minecraft:spruce_log',CE.AXE,['minecraft:stripped_spruce_log',Item.of('immersive_weathering:spruce_bark').withChance(0.50)], e)
CE.fdCutting('minecraft:spruce_wood',CE.AXE,['minecraft:stripped_spruce_wood',Item.of('immersive_weathering:spruce_bark').withChance(0.75)], e)

CE.remove({id:'farmersdelight:cutting/birch_log'}, e)
CE.remove({id:'farmersdelight:cutting/birch_wood'}, e)
CE.fdCutting('minecraft:birch_log',CE.AXE,['minecraft:stripped_birch_log',Item.of('immersive_weathering:birch_bark').withChance(0.50)], e)
CE.fdCutting('minecraft:birch_wood',CE.AXE,['minecraft:stripped_birch_wood',Item.of('immersive_weathering:birch_bark').withChance(0.75)], e)

CE.remove({id:'farmersdelight:cutting/dark_oak_log'}, e)
CE.remove({id:'farmersdelight:cutting/dark_oak_wood'}, e)
CE.fdCutting('minecraft:dark_oak_log',CE.AXE,['minecraft:stripped_dark_oak_log',Item.of('immersive_weathering:dark_oak_bark').withChance(0.50)], e)
CE.fdCutting('minecraft:dark_oak_wood',CE.AXE,['minecraft:stripped_dark_oak_wood',Item.of('immersive_weathering:dark_oak_bark').withChance(0.75)], e)

CE.remove({id:'farmersdelight:cutting/jungle_log'}, e)
CE.remove({id:'farmersdelight:cutting/jungle_wood'}, e)
CE.fdCutting('minecraft:jungle_log',CE.AXE,['minecraft:stripped_jungle_log',Item.of('immersive_weathering:jungle_bark').withChance(0.50)], e)
CE.fdCutting('minecraft:jungle_wood',CE.AXE,['minecraft:stripped_jungle_wood',Item.of('immersive_weathering:jungle_bark').withChance(0.75)], e)

CE.remove({id:'farmersdelight:cutting/acacia_log'}, e)
CE.remove({id:'farmersdelight:cutting/acacia_wood'}, e)
CE.fdCutting('minecraft:acacia_log',CE.AXE,['minecraft:stripped_acacia_log',Item.of('immersive_weathering:acacia_bark').withChance(0.50)], e)
CE.fdCutting('minecraft:acacia_wood',CE.AXE,['minecraft:stripped_acacia_wood',Item.of('immersive_weathering:acacia_bark').withChance(0.75)], e)

CE.remove({id:'farmersdelight:cutting/mangrove_log'}, e)
CE.remove({id:'farmersdelight:cutting/mangrove_wood'}, e)
CE.fdCutting('minecraft:mangrove_log',CE.AXE,['minecraft:stripped_mangrove_log',Item.of('immersive_weathering:mangrove_bark').withChance(0.50)], e)
CE.fdCutting('minecraft:mangrove_wood',CE.AXE,['minecraft:stripped_mangrove_wood',Item.of('immersive_weathering:mangrove_bark').withChance(0.75)], e)

CE.remove({id:'farmersdelight:cutting/crimson_stem'}, e)
CE.remove({id:'farmersdelight:cutting/crimson_hyphae'}, e)
CE.fdCutting('minecraft:crimson_stem',CE.AXE,['minecraft:stripped_crimson_stem',Item.of('immersive_weathering:crimson_scales').withChance(0.50)], e)
CE.fdCutting('minecraft:crimson_hyphae',CE.AXE,['minecraft:stripped_crimson_hyphae',Item.of('immersive_weathering:crimson_scales').withChance(0.75)], e)

CE.remove({id:'farmersdelight:cutting/warped_stem'}, e)
CE.remove({id:'farmersdelight:cutting/warped_hyphae'}, e)
CE.fdCutting('minecraft:warped_stem',CE.AXE,['minecraft:stripped_warped_stem',Item.of('immersive_weathering:warped_scales').withChance(0.50)], e)
CE.fdCutting('minecraft:warped_hyphae',CE.AXE,['minecraft:stripped_warped_hyphae',Item.of('immersive_weathering:warped_scales').withChance(0.75)], e)

CE.fdCutting('architects_palette:twisted_log',CE.AXE,['architects_palette:stripped_twisted_log',Item.of('immersive_weathering:architects_palette/twisted_bark').withChance(0.50)], e)
CE.fdCutting('architects_palette:twisted_wood',CE.AXE,['architects_palette:stripped_twisted_log',Item.of('immersive_weathering:architects_palette/twisted_bark').withChance(0.75)], e)

CE.remove({id:'quarkdelight:cutting/axe/blossom_log'}, e)
CE.remove({id:'quarkdelight:cutting/axe/blossom_wood'}, e)
CE.fdCutting('quark:blossom_log',CE.AXE,['quark:stripped_blossom_log',Item.of('immersive_weathering:quark/blossom_bark').withChance(0.50)], e)
CE.fdCutting('quark:blossom_wood',CE.AXE,['quark:stripped_blossom_wood',Item.of('immersive_weathering:quark/blossom_bark').withChance(0.75)], e)

CE.remove({id:'quarkdelight:cutting/axe/ancient_log'}, e)
CE.remove({id:'quarkdelight:cutting/axe/ancient_wood'}, e)
CE.fdCutting('quark:ancient_log',CE.AXE,['quark:stripped_ancient_log',Item.of('immersive_weathering:quark/ancient_bark').withChance(0.50)], e)
CE.fdCutting('quark:ancient_wood',CE.AXE,['quark:stripped_ancient_wood',Item.of('immersive_weathering:quark/ancient_bark').withChance(0.75)], e)

CE.remove({id:'quarkdelight:cutting/axe/azalea_log'}, e)
CE.remove({id:'quarkdelight:cutting/axe/azalea_wood'}, e)
CE.fdCutting('quark:azalea_log',CE.AXE,['quark:stripped_azalea_log',Item.of('immersive_weathering:quark/azalea_bark').withChance(0.50)], e)
CE.fdCutting('quark:azalea_wood',CE.AXE,['quark:stripped_azalea_wood',Item.of('immersive_weathering:quark/azalea_bark').withChance(0.75)], e)

// Remove quark hollow log cutting, replace with addition of specific bark
CE.remove({id:'quarkdelight:cutting/axe/hollow_logs'}, e)

CE.fdCutting('quark:hollow_oak_log',CE.AXE,'immersive_weathering:oak_bark', e)
CE.fdCutting('quark:hollow_spruce_log',CE.AXE,'immersive_weathering:spruce_bark', e)
CE.fdCutting('quark:hollow_birch_log',CE.AXE,'immersive_weathering:birch_bark', e)
CE.fdCutting('quark:hollow_dark_oak_log',CE.AXE,'immersive_weathering:dark_oak_bark', e)
CE.fdCutting('quark:hollow_jungle_log',CE.AXE,'immersive_weathering:jungle_bark', e)
CE.fdCutting('quark:hollow_acacia_log',CE.AXE,'immersive_weathering:acacia_bark', e)
CE.fdCutting('quark:hollow_mangrove_log',CE.AXE,'immersive_weathering:mangrove_bark', e)
CE.fdCutting('quark:hollow_crimson_stem',CE.AXE,'immersive_weathering:crimson_scales', e)
CE.fdCutting('quark:hollow_warped_stem',CE.AXE,'immersive_weathering:warped_scales', e)

CE.fdCutting('everycomp:q/architects_palette/hollow_twisted_log',CE.AXE,'immersive_weathering:architects_palette/twisted_bark', e)
CE.fdCutting('quark:hollow_ancient_log',CE.AXE,'immersive_weathering:quark/ancient_bark', e)
CE.fdCutting('quark:hollow_blossom_log',CE.AXE,'immersive_weathering:quark/blossom_bark', e)
CE.fdCutting('quark:hollow_azalea_log',CE.AXE,'immersive_weathering:quark/azalea_bark', e)

// Remove IE sawmill recipes, add own in CreativeEngineering jar file
CE.remove({id:'immersiveengineering:sawmill/oak_log'}, e)
CE.remove({id:'immersiveengineering:sawmill/spruce_log'}, e)
CE.remove({id:'immersiveengineering:sawmill/birch_log'}, e)
CE.remove({id:'immersiveengineering:sawmill/dark_oak_log'}, e)
CE.remove({id:'immersiveengineering:sawmill/jungle_log'}, e)
CE.remove({id:'immersiveengineering:sawmill/acacia_log'}, e)
CE.remove({id:'immersiveengineering:sawmill/mangrove_log'}, e)
CE.remove({id:'immersiveengineering:sawmill/crimson_log'}, e)
CE.remove({id:'immersiveengineering:sawmill/warped_log'}, e)
CE.remove({id:'immersiveengineering:sawmill/stripped_crimson_log'}, e)
CE.remove({id:'immersiveengineering:sawmill/stripped_warped_log'}, e)

// Replace FD bark recipes with IW
CE.remove({id:'farmersdelight:paper_from_tree_bark'}, e)

console.log('BarkServerScriptComplete')
})
