// requires: veggiesdelight
// Veggies Delight item changes. Creative Delight startup script, loaded from the jar after the pack's startup_scripts,
// so global.item and global.removedItems exist here. Restart to apply.

// Mhadjeb is out (TODO dishes/meals #32; ends veggies_delight.md 5.2's "until dough unification"): hidden in JEI, tags
// stripped; its recipes are removed in data/creative_delight/kubejs/veggiesdelight/recipes.js.
global.removedItems.push(global.item.uncooked_mhadjeb, global.item.mhadjeb)
