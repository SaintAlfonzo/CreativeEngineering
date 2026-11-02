// requires: miners_delight
// Miner's Delight item changes. Creative Delight startup script, loaded from the jar after the pack's startup_scripts,
// so global.item and global.removedItems exist here. Restart to apply.

// Copper cup foods are out (TODO dishes/meals #53, 2026-11-02): the 15 soup and stew cups are hidden in JEI and their
// tags stripped. The Copper Pot serves every meal in its bowl: the cup conversion table is emptied by
// kubejs/data/miners_delight/cup_conversion/default_conversions.json (the pack's data sits above the donor's), and the
// pack's own Creamy Onion Soup row is gone. The copper cup, water cup, milk cup and powder snow cup stay (containers).
var i = global.item
;[
	i.baked_cod_stew_cup, i.beef_stew_cup, i.beetroot_soup_cup, i.chicken_soup_cup, i.fish_stew_cup, i.mushroom_stew_cup,
	i.noodle_soup_cup, i.pumpkin_soup_cup, i.rabbit_stew_cup, i.vegetable_soup_cup, i.bone_broth_cup, i.cave_soup_cup,
	i.bat_soup_cup, i.insect_stew_cup, i.onion_soup_cup,
].forEach(id => global.removedItems.push(id))
