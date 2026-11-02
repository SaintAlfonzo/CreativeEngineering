// requires: pneumaticcraft
// PneumaticCraft item changes. Creative Delight startup script, loaded from the jar after the pack's startup_scripts,
// so global.item and global.removedItems exist here. Restart to apply.

// Chips (shown as Fries) are out (owner 2026-11-01, TODO merges/dishes #1): hidden in JEI, tags stripped. Both of
// PneumaticCraft's recipes (thermo plant, Cod 'n' Chips) and the pack's frying recipe are removed in
// data/creativedelight/kubejs/pneumaticcraft/recipes.js; Canned Chips left with them (tools/gen_can_types.py).
global.removedItems.push(global.item.chips)
