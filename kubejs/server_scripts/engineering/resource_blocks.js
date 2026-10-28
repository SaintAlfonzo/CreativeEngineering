ServerEvents.recipes(e => {

console.log('Hello! The resource block event has started!')

const R = global.NCH
var i = global.item;
var f = global.fluid;
var u = undefined;

// ############################## Resource stones ##############################
/*
Ore Stone ratios:
	Compacting: 250mb Lava, 2 Gravel, 6 Nuggets
	
	IE Crush: 5 nugs
	CR Crush: .519 crushed ore
	CR Crush - Smelt: .519 ingots (4.66 nugs) 
	CR Crush - IE Crush - Smelt: .692 ingots (6.22 nugs)
	CR Crush - IE Arc: .77777 ingots (7 nugs)
	
Ore yields:
	Smelt (Crushed ore, grit): 1x
	CR Crush (Orestone): 1x
	IE Crusher (Orestone, crushed ore): 1.333x
	IE Arc Smelter (Crushed ore, grit): 1.5x
*/

//Veridium (Copper)
R.remove({id:'create_ultimate_factory:f_compacting_veridium'}, e)
R.remove({id:'create:crushing/veridium'}, e)
R.remove({id:'create:crushing/veridium_recycling'}, e)
R.createCompacting(i.veridium, [Item.of(i.nugget_copper, 6), Item.of(i.gravel, 2), Fluid.of(f.lava, 250)], e, 'veridium_compacting')
R.createCrushing([Item.of(i.crushed_copper, 1).withChance(0.52)], i.veridium, e)
R.createCrushing([Item.of(i.crushed_copper, 1).withChance(0.52)], i.veridium_stone, e)
R.ieCrushing(Item.of(i.nugget_copper, 5),i.veridium, e)
R.ieCrushing(Item.of(i.nugget_copper, 5),i.veridium_stone, e)
//Extruding
R.createExtruding(i.veridium, [{ fluid: f.lava, amount: 1000 }, { fluid: f.water, amount: 1000 }], e, 'veridium_extruding', i.dust_block_copper, 10)

//Crimsite (Iron)
R.remove({id:'create_ultimate_factory:f_compacting_crimsite'}, e)
R.remove({id:'create:crushing/crimsite'}, e)
R.remove({id:'create:crushing/crimsite_recycling'}, e)
R.createCompacting(i.crimsite, [Item.of(i.nugget_iron, 6), Item.of(i.gravel, 2), Fluid.of(f.lava, 250)], e, 'crimsite_compacting')
R.createCrushing([Item.of(i.crushed_iron, 1).withChance(0.52)], i.crimsite, e)
R.createCrushing([Item.of(i.crushed_iron, 1).withChance(0.52)], i.crimsite_stone, e)
R.ieCrushing(Item.of(i.nugget_iron, 5), i.crimsite, e)
R.ieCrushing(Item.of(i.nugget_iron, 5), i.crimsite_stone, e)
R.createExtruding(i.crimsite, [{ fluid: f.lava, amount: 1000 }, { fluid: f.water, amount: 1000 }], e, 'crimsite_extruding', i.dust_block_iron, 10)

//Ochrum (Gold)
R.remove({id:'create_ultimate_factory:f_compacting_ochrum'}, e)
R.remove({id:'create:crushing/ochrum'}, e)
R.remove({id:'create:crushing/ochrum_recycling'}, e)
R.createCompacting(i.ochrum, [Item.of(i.nugget_gold, 6), Item.of(i.gravel, 2), Fluid.of(f.lava, 250)], e, 'ochrum_compacting')
R.createCrushing([Item.of(i.crushed_gold, 1).withChance(0.52)], i.ochrum, e)
R.createCrushing([Item.of(i.crushed_gold, 1).withChance(0.52)], i.ochrum_stone, e)
R.ieCrushing(Item.of(i.nugget_gold, 5), i.ochrum, e)
R.ieCrushing(Item.of(i.nugget_gold, 5), i.ochrum_stone, e)
R.createExtruding(i.ochrum, [{ fluid: f.lava, amount: 1000 }, { fluid: f.water, amount: 1000 }], e, 'ochrum_extruding', i.dust_block_gold, 10)

//Azurine (Zinc)
R.remove({id:'create_ultimate_factory:f_compacting_asurine'}, e)
R.remove({id:'create:crushing/asurine'}, e)
R.remove({id:'create:crushing/asurine_recycling'}, e)
R.createCompacting(i.asurine, [Item.of(i.nugget_zinc, 6), Item.of(i.gravel, 2), Fluid.of(f.lava, 250)], e, 'asurine_compacting')
R.createCrushing([Item.of(i.crushed_zinc, 1).withChance(0.52)], i.asurine, e)
R.createCrushing([Item.of(i.crushed_zinc, 1).withChance(0.52)], i.asurine_stone, e)
R.ieCrushing(Item.of(i.nugget_zinc, 5), i.asurine, e)
R.ieCrushing(Item.of(i.nugget_zinc, 5), i.asurine_stone, e)
R.createExtruding(i.asurine, [{ fluid: f.lava, amount: 1000 }, { fluid: f.water, amount: 1000 }], e, 'asurine_extruding', i.dust_block_zinc, 10)

//Limestone
R.remove({id:'create_ultimate_factory:crushing_limestone'}, e)
R.createCrushing([Item.of(i.quartz, 1).withChance(0.25), Item.of(i.lapis, 1).withChance(0.15)], i.limestone, e)
R.ieCrushing(Item.of(i.clay_ball, 2), i.limestone, e, u, [{ chance: 0.50, output: Item.of(i.quartz, 1) }])
R.ieCrushing(Item.of(i.clay_ball, 2), i.limestone_stone, e, u, [{ chance: 0.50, output: Item.of(i.quartz, 1) }])
//Implement 3 heat state recipes, superheated +5 limestone bonus
R.createMixing([Item.of(i.limestone, 1), Fluid.of(f.lava, 100)], [Fluid.of(f.honey, 100), Fluid.of(f.lava, 100)], e, "limestone_generation_unheated", "n/a")
R.createMixing([Item.of(i.limestone, 5), Fluid.of(f.lava, 500)], [Fluid.of(f.honey, 500), Fluid.of(f.lava, 500)], e, "limestone_generation_heated", "heated")
R.createMixing([Item.of(i.limestone, 15), Fluid.of(f.lava, 1000)], [Fluid.of(f.honey, 1000), Fluid.of(f.lava, 1000)], e, "limestone_generation_superheated", "superheated")
// Alt limestone mixing
R.createMixing(Item.of(i.limestone, 4), [Item.of(i.calcite, 2), Item.of(i.bonemeal, 1), Item.of(i.clay_ball, 3)], e, u, "heated")
//Limestone haunting
R.createHaunting(Item.of(i.basalt, 1), Item.of(i.limestone, 1), e)

//Scoria
//Implement 3 heat state recipes, superheated +5 scoria bonus
R.createMixing([Item.of(i.scoria, 1), Fluid.of(f.lava, 100)], [Fluid.of(f.chocolate, 100), Fluid.of(f.lava, 100)], e, "scoria_generation_unheated", "n/a")
R.createMixing([Item.of(i.scoria, 5), Fluid.of(f.lava, 500)], [Fluid.of(f.chocolate, 500), Fluid.of(f.lava, 500)], e, "scoria_generation_heated", "heated")
R.createMixing([Item.of(i.scoria, 15), Fluid.of(f.lava, 1000)], [Fluid.of(f.chocolate, 1000), Fluid.of(f.lava, 1000)], e, "scoria_generation_superheated", "superheated")
//Replace crushing recipes
R.remove({ id: 'create_ultimate_factory:crushing_scoria' }, e)
R.createCrushing([Item.of(i.blaze_powder, 1).withChance(0.25), Item.of(i.blaze_rod, 1).withChance(0.02)], i.scoria, e)
R.ieCrushing(Item.of(i.dust_sulfur, 1), i.scoria, e, u, [{ chance: 0.50, output: Item.of(i.blaze_powder, 1) }])

//Scorchia
//Replace dye recipe w/ coke mixing 
R.remove({ id: 'create:crafting/palettes/scorchia' }, e)
R.shaped(Item.of(i.scorchia, 8), ['SSS', 'SCS', 'SSS'], {S: i.scoria, C: [i.coal_coke, i.dust_coke, i.petcoke, i.dust_petcoke]}, e) 
//Crushing recipe
R.createCrushing([Item.of(i.dust_coke, 1).withChance(.25), Item.of(i.nugget_lead, 1).withChance(.1),Item.of(i.nugget_uranium, 1).withChance(.1)], i.scorchia, e)
R.ieCrushing(Item.of(i.dust_coke, 1), i.scorchia, e, u, [{ chance: 0.2, output: Item.of(i.nugget_lead, 1) }, { chance: 0.2, output: Item.of(i.nugget_uranium, 1) }])

//Granite
//Remove botania infusion, milling recipies
R.remove({ id: 'create:milling/granite' }, e)
R.remove({ id: 'botania:mana_infusion/granite_to_andesite' }, e)
//Remove create compacting recipe
R.remove({ id: 'create:compaction/granite_from_flint' }, e)
//Crush to red sand, redstone
R.createCrushing([Item.of(i.red_sand, 1).withChance(0.75), Item.of(i.redstone, 2).withChance(0.30)], i.granite, e)
R.ieCrushing(Item.of(i.red_sand, 1), i.granite, e, u, [{ chance: 0.75, output: Item.of(i.redstone, 2) }])
R.createCrushing([Item.of(i.red_sand, 1).withChance(0.75), Item.of(i.redstone, 2).withChance(0.30)], i.granite_stone, e)
R.ieCrushing(Item.of(i.red_sand, 1), i.granite_stone, e, u, [{ chance: 0.75, output: Item.of(i.redstone, 2) }])

//Diorite
R.ieCrushing(Item.of(i.quartz, 1), i.diorite, e)

//Andesite
//Andesite mixing, 1.5x bonus when superheated
R.createMixing(Item.of(i.andesite, 2), [Item.of(i.cobblestone, 1), Item.of(i.diorite, 1)], e, "andesite_generation_unheated", "n/a")
R.createMixing(Item.of(i.andesite, 32), [Item.of(i.cobblestone, 16), Item.of(i.diorite, 16)], e, "andesite_generation_heated", "heated")
R.createMixing(Item.of(i.andesite, 48), [Item.of(i.cobblestone, 16), Item.of(i.diorite, 16)], e, "andesite_generation_superheated", "superheated")
//Crush to cobble
R.ieCrushing(Item.of(i.cobblestone, 1), i.andesite, e)

//Teracotta
//Efficient IE crusher recipe
R.ieCrushing(Item.of(i.red_sand, 1), i.terracotta, e, u, [{ chance: 0.75, output: Item.of(i.red_sand, 1) }])

//Calcite
//IE crushing extra bonemeal
R.ieCrushing(Item.of(i.bonemeal, 1), i.calcite, e, u, [{ chance: 0.5, output: Item.of(i.bonemeal, 1) }])

//Dripstone
//IE crushing
R.ieCrushing(Item.of(i.clay_ball, 2), i.dripstone, e)

//Tuff
//Replace tuff crushing
R.remove({ id: 'create:crushing/tuff' }, e)
R.remove({ id: 'create:crushing/tuff_recycling' }, e)
R.createCrushing([Item.of(i.flint, 1).withChance(0.25), Item.of(i.nugget_copper, 1).withChance(0.1), Item.of(i.nugget_iron, 1).withChance(0.1), Item.of(i.nugget_gold, 1).withChance(0.1), Item.of(i.nugget_zinc, 1).withChance(0.1), Item.of(i.nugget_rose_gold, 1).withChance(0.1)], i.tuff, e)
R.ieCrushing(Item.of(i.flint, 2), i.tuff, e, u, [{ chance: 0.4, output: Item.of(i.nugget_iron, 3) }, { chance: 0.25, output: Item.of(i.nugget_zinc, 3) }])
//Ash compacting
R.remove({ id: 'immersive_weathering:tuff' }, e)
R.createCompacting(Item.of(i.tuff, 1), [Item.of(i.ash, 16)], e, u, 'none')

//Obsidian
//Remove duplicate powdered obsidian block
R.remove({ id: 'create_things_and_misc:powder_obsi_c_raft' }, e)
// Haunt obsidian to crying obsidian
R.createHaunting(Item.of(i.crying_obsidian, 1), Item.of(i.obsidian, 1), e)
//IE crushing recipes, obsidian powder and crying obsidian
R.ieCrushing(Item.of(i.powdered_obsidian, 2), i.obsidian, e, u, [{ chance: 0.5, output: Item.of(i.obsidian, 1) }])
R.ieCrushing(Item.of(i.powdered_obsidian, 1), i.crying_obsidian, e, u, [{ chance: 0.75, output: Item.of(i.powdered_obsidian, 1) }, { chance: 0.75, output: Item.of(i.amethyst_shard, 1) }])
// Galcite crushing
R.createCrushing([Item.of(i.powdered_obsidian, 2), Item.of(i.powdered_obsidian, 1).withChance(0.5), Item.of(i.obsidian, 1).withChance(0.5)], i.galcite, e)
R.ieCrushing(Item.of(i.powdered_obsidian, 4), i.galcite, e, u, [{ chance: 0.75, output: Item.of(i.powdered_obsidian, 1) }, { chance: 0.75, output: Item.of(i.obsidian, 1) }])
// Layered obsidian crushing
R.createCrushing([Item.of(i.powdered_obsidian, 3), Item.of(i.powdered_obsidian, 1).withChance(0.5)], i.layered_obsidian, e)
R.ieCrushing(Item.of(i.powdered_obsidian, 4), i.layered_obsidian, e, u, [{ chance: 0.75, output: Item.of(i.powdered_obsidian, 1) }])

//Magma block
//Redundant recipe removal    
R.remove({ id: 'create:crushing/misc/scrap_magma_block' }, e)
//IE Crushing
R.ieCrushing(Item.of(i.crushed_magma, 3), i.magma_block, e, u, [{ chance: 0.75, output: Item.of(i.crushed_magma, 1) }, { chance: 0.333, output: Item.of(i.scoria, 1) }])

//Basalt
//Implement 3 heat state recipes, more lava efficient at higher heat, superheated ice -1 bonus
R.remove({ id: 'create_netherless:basalt_by_mixing' }, e)
R.createMixing([Item.of(i.basalt, 1), Item.of(i.soul_soil, 1), Fluid.of(f.lava, 50)], [Item.of(i.packed_ice, 1), Item.of(i.soul_soil, 1), Fluid.of(f.lava, 100)], e, "basalt_generation_unheated", "n/a")
R.createMixing([Item.of(i.basalt, 5), Item.of(i.soul_soil, 1), Fluid.of(f.lava, 450)], [Item.of(i.packed_ice, 5), Item.of(i.soul_soil, 5), Fluid.of(f.lava, 500)], e, "basalt_generation_heated", "heated")
R.createMixing([Item.of(i.basalt, 10), Item.of(i.soul_soil, 1), Fluid.of(f.lava, 950)], [Item.of(i.blue_ice, 1), Item.of(i.soul_soil, 10), Fluid.of(f.lava, 1000)], e, "basalt_generation_superheated", "superheated")
//Remove basalt crushing
R.remove({ id: 'vintageimprovements:crushing/basalt' }, e)
R.remove({ id: 'vintageimprovements:crushing/basalt_recycling' }, e)
//Replace crushing w/ IE ore nuggets
R.createCrushing([Item.of(i.blackstone, 1), Item.of(i.nugget_aluminum, 1).withChance(.1), Item.of(i.nugget_silver, 1).withChance(.1), Item.of(i.nugget_nickel, 1).withChance(.1)], i.basalt_like, e)
R.ieCrushing(Item.of(i.blackstone, 1), i.basalt_like, e, u, [{ chance: 0.2, output: Item.of(i.nugget_aluminum, 1) }, { chance: 0.2, output: Item.of(i.nugget_silver, 1) }, { chance: 0.2, output: Item.of(i.nugget_nickel, 1) }])

//Netherrack
//Netherrack IE crushing
R.ieCrushing(Item.of(i.cinder_flour, 2), Item.of(i.netherrack, 1), e, u, [{ chance: 0.25, output: Item.of(i.cinder_flour, 1) }])
//Netherbrick crushing, replace netherite scrap
R.remove({ id: 'create_ultimate_factory:crushing_netherite' }, e)
R.ieCrushing(Item.of(i.nether_brick, 2), Item.of(i.nether_bricks, 1), e, u, [{ chance: 0.5, output: Item.of(i.nether_brick, 1)}, { chance: 0.01, output: Item.of(i.fragment_netherite, 1) }])
R.createCrushing([Item.of(i.nether_brick, 1), Item.of(i.nether_brick, 1).withChance(0.25), Item.of(i.fragment_netherite, 1).withChance(0.005)], Item.of(i.nether_bricks), e)

//Soul sand
//Add IE Crush soul sandstone to glowstone recipe
R.ieCrushing(Item.of(i.glowstone, 1), Item.of(i.soul_sandstone, 1), e)
//Remove netherless recipe
R.remove({ id: 'create_netherless:soulsand_by_deploying' }, e)
R.remove({ id: 'create_netherless:soulsand_by_manual_application' }, e)

//Blackstone
//Remove CR crushing recipe
R.remove({ id: 'create_ultimate_factory:crushing_blackstone' }, e)
//Efficient byproductless IE crushing
R.ieCrushing(Item.of(i.coal, 1), i.blackstone, e)

//Stone
//Implement 3 heat state recipes, equal ratios
R.createMixing([Item.of(i.stone, 1), Fluid.of(f.lava, 100)], [Fluid.of(f.water, 100), Fluid.of(f.lava, 100)], e, "stone_generation_unheated", "n/a")
R.createMixing([Item.of(i.stone, 5), Fluid.of(f.lava, 500)], [Fluid.of(f.water, 500), Fluid.of(f.lava, 500)], e, "stone_generation_heated", "heated")
R.createMixing([Item.of(i.stone, 10), Fluid.of(f.lava, 1000)], [Fluid.of(f.water, 1000), Fluid.of(f.lava, 1000)], e, "stone_generation_superheated", "superheated")

//Replace stone recipes w/ stone tag
R.replaceInput({ id: i.stonecutter },
	i.stone, i.anystone, e)
R.replaceInput({ id: i.comparator },
	i.stone, i.anystone, e)
R.replaceInput({ id: i.repeater },
	i.stone, i.anystone, e)
R.replaceInput({ id: 'quark:tweaks/crafting/utility/misc/repeater' },
	i.stone, i.anystone, e)
R.replaceInput({ id: 'quark:automation/crafting/redstone_randomizer' },
	i.stone, i.anystone, e)
R.replaceInput({ mod: 'create_things_and_misc' },
	i.stone, i.anystone, e)
R.replaceInput({ mod: 'gates' },
	i.stone, i.anystone, e)

//Cobblestone
//Implement 3 heat state recipes, equal ratios
R.createMixing([Item.of(i.cobblestone, 1), Fluid.of(f.lava, 100)], [Fluid.of(f.water, 100), Fluid.of(f.lava, 100)], e, "cobblestone_generation_unheated", "n/a")
R.createMixing([Item.of(i.cobblestone, 5), Fluid.of(f.lava, 500)], [Fluid.of(f.water, 500), Fluid.of(f.lava, 500)], e, "cobblestone_generation_heated", "heated")
R.createMixing([Item.of(i.cobblestone, 10), Fluid.of(f.lava, 1000)], [Fluid.of(f.water, 1000), Fluid.of(f.lava, 1000)], e, "cobblestone_generation_superheated", "superheated")

//Obsidian
//Water + lava, both used up, heated (was create_generators:generator_obsidian)
R.createMixing(Item.of(i.obsidian, 1), [Fluid.of(f.water, 250), Fluid.of(f.lava, 250)], e, 'obsidian_generation_heated', "heated")

//Deepslate
//Remove useless botania conversion
R.remove({ id:'botania:mana_infusion/calcite_to_deepslate' }, e)
// Deepslate compacting recipes
R.remove({ id: 'create_netherless:deepslate_from_compacting' }, e)
R.remove({ id: 'create_netherless:deep_slate_from_compacting_charcoal' }, e)
//Different efficiencies
R.createCompacting(Item.of(i.deepslate, 8), [Item.of(i.cobblestone, 8), Fluid.of(f.lava, 300)], e, u, "none")
R.createCompacting(Item.of(i.deepslate, 32), [Item.of(i.cobblestone, 32), Fluid.of(f.lava, 1000)], e, 'deepslate_bulk_heated', "heated")
R.createCompacting(Item.of(i.deepslate, 48), [Item.of(i.cobblestone, 32), Fluid.of(f.lava, 1000)], e, 'deepslate_bulk_superheated', "superheated")
//Deepslate crushing
//Deepslate -> cobble crush step, dscobble crush to gravel
R.ieCrushing(Item.of(i.deepslate_cobble, 1), Item.of(i.deepslate, 1), e)
R.remove({ id: 'create:crushing/scrap_cobblestone_small' }, e)
R.createCrushing(i.gravel, i.deepslate_cobble, e)

console.log('The resource block event has finished!')
})
