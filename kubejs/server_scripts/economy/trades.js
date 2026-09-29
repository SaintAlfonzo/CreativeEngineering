console.log('Hello! The trade event has started!')

var i = global.item
var f = global.fluid
var v = {};

v.mechanical_engineer = 'dynamicvillage:mechanical_engineer';
v.hydraulic_engineer = 'dynamicvillage:hydraulic_engineer';
v.chef = 'chefsdelight:delightchef';
v.cleric = 'minecraft:cleric'

MoreJSEvents.villagerTrades((e) => {
  // Fires at server start, long after load: re-read the dict, top-level `i` is shared by every server script
  var i = global.item

  // Mechanical Engineer
  e.removeModdedTrades([v.mechanical_engineer], [1, 5])
  // Level 1
  e.addTrade(v.mechanical_engineer, 1, [Item.of(i.emerald, 2)], Item.of(i.andesite_alloy, 8)).maxUses(8).villagerExperience(8).priceMultiplier(0.02)
  e.addTrade(v.mechanical_engineer, 1, [Item.of(i.coal, 20)], Item.of(i.emerald, 1)).maxUses(8).villagerExperience(8).priceMultiplier(0.01)
  e.addTrade(v.mechanical_engineer, 1, [Item.of(i.emerald, 1)], Item.of(i.plate_iron, 4)).maxUses(10).villagerExperience(8).priceMultiplier(0.02)
  e.addTrade(v.mechanical_engineer, 1, [Item.of(i.raw_zinc, 6)], Item.of(i.emerald, 1)).maxUses(10).villagerExperience(8).priceMultiplier(0.02)
  e.addTrade(v.mechanical_engineer, 1, [Item.of(i.emerald, 5)], Item.of(i.wrench, 1)).maxUses(3).villagerExperience(24).priceMultiplier(0.04)
  // Level 2
  e.addTrade(v.mechanical_engineer, 2, [Item.of(i.emerald, 3)], Item.of(i.mechanical_bearing, 1)).maxUses(8).villagerExperience(8).priceMultiplier(0.02)
  e.addTrade(v.mechanical_engineer, 2, [Item.of(i.emerald, 6)], Item.of(i.goggles, 1)).maxUses(3).villagerExperience(32).priceMultiplier(0.1)
  e.addTrade(v.mechanical_engineer, 2, [Item.of(i.cog, 14)], Item.of(i.emerald, 1)).maxUses(10).villagerExperience(8).priceMultiplier(0.02)
  e.addTrade(v.mechanical_engineer, 2, [Item.of(i.emerald, 3)], Item.of(i.water_wheel, 4)).maxUses(10).villagerExperience(12).priceMultiplier(0.1)
  e.addTrade(v.mechanical_engineer, 2, [Item.of(i.emerald, 2)], Item.of(i.basin, 1)).maxUses(10).villagerExperience(12).priceMultiplier(0.1)
  e.addTrade(v.mechanical_engineer, 2, [Item.of(i.emerald, 2)], Item.of(i.depot, 1)).maxUses(10).villagerExperience(10).priceMultiplier(0.1)
  // Level 3
  e.addTrade(v.mechanical_engineer, 3, [Item.of(i.belt_connector, 10)], Item.of(i.emerald, 3)).maxUses(10).villagerExperience(10).priceMultiplier(0.01)
  e.addTrade(v.mechanical_engineer, 3, [Item.of(i.emerald, 6)], Item.of(i.stressometer, 1)).maxUses(3).villagerExperience(16).priceMultiplier(0.01)
  // Level 4
  e.addTrade(v.mechanical_engineer, 4, [Item.of(i.emerald, 8)], Item.of(i.speedometer, 1)).maxUses(4).villagerExperience(38).priceMultiplier(0.01)
  e.addTrade(v.mechanical_engineer, 4, [Item.of(i.large_water_wheel, 4)], Item.of(i.emerald, 1)).maxUses(6).villagerExperience(10).priceMultiplier(0.01)
  e.addTrade(v.mechanical_engineer, 4, [Item.of(i.cr_chute, 3)], Item.of(i.emerald, 1)).maxUses(10).villagerExperience(10).priceMultiplier(0.01)
  // Level 5
  e.addTrade(v.mechanical_engineer, 5, [Item.of(i.emerald, 9)], Item.of(i.mechanical_roller, 2)).maxUses(2).villagerExperience(24).priceMultiplier(0.01)
  e.addTrade(v.mechanical_engineer, 5, [Item.of(i.emerald, 10)], Item.of(i.linked_controller, 1)).maxUses(3).villagerExperience(40).priceMultiplier(0.01)
  e.addTrade(v.mechanical_engineer, 5, [Item.of(i.emerald, 3)], Item.of(i.display_link, 2)).maxUses(8).villagerExperience(12).priceMultiplier(0.01)

  //Hydraulic Engineer
  e.removeModdedTrades([v.hydraulic_engineer], [1, 5])
  // Level 1
  e.addTrade(v.hydraulic_engineer, 1, [Item.of(i.emerald, 2)], Item.of(i.plate_copper, 8)).maxUses(8).villagerExperience(8).priceMultiplier(0.02)
  e.addTrade(v.hydraulic_engineer, 1, [Item.of(i.ingot_copper, 20)], Item.of(i.emerald, 1)).maxUses(8).villagerExperience(8).priceMultiplier(0.01)
  e.addTrade(v.hydraulic_engineer, 1, [Item.of(i.emerald, 2)], Item.of(i.cr_pipe, 6)).maxUses(10).villagerExperience(8).priceMultiplier(0.02)
  e.addTrade(v.hydraulic_engineer, 1, [Item.of(i.raw_copper, 6)], Item.of(i.emerald, 1)).maxUses(10).villagerExperience(8).priceMultiplier(0.02)
  e.addTrade(v.hydraulic_engineer, 1, [Item.of(i.emerald, 3)], Item.of(i.copper_diving_helmet, 1)).maxUses(3).villagerExperience(24).priceMultiplier(0.04)
  // Level 2
  e.addTrade(v.hydraulic_engineer, 2, [Item.of(i.emerald, 2)], Item.of(i.copper_casing, 1)).maxUses(8).villagerExperience(8).priceMultiplier(0.1)
  e.addTrade(v.hydraulic_engineer, 2, [Item.of(i.emerald, 4)], Item.of(i.spout, 1)).maxUses(10).villagerExperience(12).priceMultiplier(0.1)
  e.addTrade(v.hydraulic_engineer, 2, [Item.of(i.emerald, 2)], Item.of(i.cr_tank, 1)).maxUses(10).villagerExperience(10).priceMultiplier(0.1)
  e.addTrade(v.hydraulic_engineer, 2, [Item.of(i.emerald, 3)], Item.of(i.mechanical_pump, 2)).maxUses(10).villagerExperience(10).priceMultiplier(0.1)
  // Level 3
  e.addTrade(v.hydraulic_engineer, 3, [Item.of(i.copper_casing, 4)], Item.of(i.emerald, 1)).maxUses(10).villagerExperience(8).priceMultiplier(0.02)
  e.addTrade(v.hydraulic_engineer, 3, [Item.of(i.emerald, 1)], Item.of(i.cr_tank, 3)).maxUses(10).villagerExperience(10).priceMultiplier(0.01)
  e.addTrade(v.hydraulic_engineer, 3, [Item.of(i.emerald, 4)], Item.of(i.mechanical_mixer, 1)).maxUses(8).villagerExperience(12).priceMultiplier(0.01)
  // Level 4
  e.addTrade(v.hydraulic_engineer, 4, [Item.of(i.emerald, 2)], Item.of(i.cr_fluid_valve, 3)).maxUses(4).villagerExperience(16).priceMultiplier(0.01)
  e.addTrade(v.hydraulic_engineer, 4, [Item.of(i.emerald, 1)], Item.of(i.large_water_wheel, 3)).maxUses(6).villagerExperience(10).priceMultiplier(0.01)
  // Level 5
  e.addTrade(v.hydraulic_engineer, 5, [Item.of(i.emerald, 1)], Item.of(i.honeyed_apple, 8)).maxUses(3).villagerExperience(16).priceMultiplier(0.01)
  e.addTrade(v.hydraulic_engineer, 5, [Item.of(i.emerald, 4)], Item.of(i.copper_diving_boots, 1)).maxUses(3).villagerExperience(16).priceMultiplier(0.02)
  e.addTrade(v.hydraulic_engineer, 5, [Item.of(i.emerald, 7)], Item.of(i.copper_backtank, 1)).maxUses(3).villagerExperience(16).priceMultiplier(0.02)

  //Chef
  e.removeModdedTrades([v.chef], [1, 5])
  e.addTrade(v.chef, 1, [Item.of(i.fd_rope, 9)], Item.of(i.emerald, 1)).maxUses(12).villagerExperience(2).priceMultiplier(0.05)
  e.addTrade(v.chef, 1, [Item.of(i.onion_farmersdelight, 9)], Item.of(i.emerald, 1)).maxUses(12).villagerExperience(2).priceMultiplier(0.05)
  e.addTrade(v.chef, 1, [Item.of(i.emerald, 1)], Item.of(i.mixed_salad, 1)).maxUses(12).villagerExperience(3).priceMultiplier(0.05)
  e.addTrade(v.chef, 1, [Item.of(i.emerald, 1)], Item.of(i.bacon_and_eggs, 1)).maxUses(12).villagerExperience(3).priceMultiplier(0.05)
  e.addTrade(v.chef,4,[Item.of(i.in_dough, 9)],Item.of(i.emerald))

  //Cleric
  // ender pearl trade still present, unwanted
  e.removeVanillaTrades([v.cleric], [1, 5])
  e.removeModdedTrades([v.cleric], [1, 5])
  // Level 1 - Novice
  e.addTrade(v.cleric, 1, [Item.of(i.rotten_flesh, 32)], Item.of(i.emerald, 1)).maxUses(16).villagerExperience(2).priceMultiplier(0.05)
  e.addTrade(v.cleric, 1, [Item.of(i.emerald, 1)], Item.of(i.redstone, 2)).maxUses(12).villagerExperience(1).priceMultiplier(0.05)
  // Level 2 - Apprentice
  e.addTrade(v.cleric, 2, [Item.of(i.ingot_gold, 3)], Item.of(i.emerald, 1)).maxUses(12).villagerExperience(10).priceMultiplier(0.05)
  e.addTrade(v.cleric, 2, [Item.of(i.emerald, 1)], Item.of(i.lapis, 1)).maxUses(12).villagerExperience(5).priceMultiplier(0.05)
  // Level 3 - Journeyman
  e.addTrade(v.cleric, 3, [Item.of(i.rabbit_foot, 2)], Item.of(i.emerald, 1)).maxUses(12).villagerExperience(20).priceMultiplier(0.05)
  e.addTrade(v.cleric, 3, [Item.of(i.emerald, 4)], Item.of(i.glowstone_block, 1)).maxUses(12).villagerExperience(10).priceMultiplier(0.05)
  // Level 4 - Expert
  e.addTrade(v.cleric, 4, [Item.of(i.scute, 4)], Item.of(i.emerald, 1)).maxUses(12).villagerExperience(30).priceMultiplier(0.05)
  e.addTrade(v.cleric, 4, [Item.of(i.glass_bottle, 9)], Item.of(i.emerald, 1)).maxUses(12).villagerExperience(30).priceMultiplier(0.05)
  // Level 5 - Master
  e.addTrade(v.cleric, 5, [Item.of(i.nether_wart, 22)], Item.of(i.emerald, 1)).maxUses(12).villagerExperience(30).priceMultiplier(0.05)
  e.addTrade(v.cleric, 5, [Item.of(i.emerald, 3)], Item.of(i.experience_bottle, 1)).maxUses(12).villagerExperience(30).priceMultiplier(0.05)
  e.addTrade(v.cleric, 5, [Item.of(i.emerald, 4)], Item.of(i.echo_shard, 1)).maxUses(16).villagerExperience(6).priceMultiplier(0.1)
})

console.log('The trade event has finished!')
