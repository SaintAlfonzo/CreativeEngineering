// priority: 1000
// Villager profession ids (global.profession), same role as item_dict.js for items: trade scripts reference
// global.profession.<key> (or pass the key to global.trades.villager) instead of repeating 'namespace:id' strings
// (plan_rules_fixes.md section 6 and 12c).
global.profession = {};

var pr = global.profession;

// ############################## Vanilla ##############################
pr.armorer = 'minecraft:armorer';
pr.butcher = 'minecraft:butcher';
pr.cartographer = 'minecraft:cartographer';
pr.cleric = 'minecraft:cleric';
pr.farmer = 'minecraft:farmer';
pr.fisherman = 'minecraft:fisherman';
pr.fletcher = 'minecraft:fletcher';
pr.leatherworker = 'minecraft:leatherworker';
pr.librarian = 'minecraft:librarian';
pr.mason = 'minecraft:mason';
pr.shepherd = 'minecraft:shepherd';
pr.toolsmith = 'minecraft:toolsmith';
pr.weaponsmith = 'minecraft:weaponsmith';

// ############################## Modded ##############################
pr.mechanical_engineer = 'dynamicvillage:mechanical_engineer';
pr.hydraulic_engineer = 'dynamicvillage:hydraulic_engineer';
pr.chef = 'chefsdelight:delightchef';
pr.botanist = 'beautify:botanist';
