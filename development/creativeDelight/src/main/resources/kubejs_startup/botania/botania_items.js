// requires: botania
// Botania item changes. Creative Delight startup script, loaded from the jar after the pack's startup_scripts, so
// global.item and global.removedItems exist here. Restart to apply.

// Bowl of Water is out (TODO dishes/ingredients #11): only Garden of Glass makes it (a bowl used on water), and that is
// not in the pack; no recipe or loot names it. Hidden in JEI (the only item browser: the creative menu is off) and its
// tags stripped.
global.removedItems.push(global.item.water_bowl)
