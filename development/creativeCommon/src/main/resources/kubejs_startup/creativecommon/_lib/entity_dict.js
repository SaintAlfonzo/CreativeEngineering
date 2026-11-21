// priority: 1000
// SPDX-License-Identifier: GPL-3.0-only
// Entity type ids (global.entity), same role as item_dict.js for items and profession_dict.js for professions:
// scripts reference global.entity.<key> instead of repeating 'namespace:id' strings (mobs/tweaks #4).
// An id whose mod is absent stays in the dict; consumers guard with global.has(<mod>) where it matters.
global.entity = {};

var en = global.entity;

// ############################## Vanilla ##############################
en.chicken = 'minecraft:chicken';
en.cod = 'minecraft:cod';
en.cow = 'minecraft:cow';
en.dolphin = 'minecraft:dolphin';
en.glow_squid = 'minecraft:glow_squid';
en.pig = 'minecraft:pig';
en.rabbit = 'minecraft:rabbit';
en.salmon = 'minecraft:salmon';
en.sheep = 'minecraft:sheep';
en.squid = 'minecraft:squid';
en.turtle = 'minecraft:turtle';

// ############################## Alex's Mobs ##############################
en.cachalot_whale = 'alexsmobs:cachalot_whale';
en.frilled_shark = 'alexsmobs:frilled_shark';
en.giant_squid = 'alexsmobs:giant_squid';
en.giant_squid_part = 'alexsmobs:giant_squid_part';
en.hammerhead_shark = 'alexsmobs:hammerhead_shark';
en.orca = 'alexsmobs:orca';
en.seal = 'alexsmobs:seal';

// ############################## More Mooblooms ##############################
en.moobloom = 'moremooblooms:moobloom';
