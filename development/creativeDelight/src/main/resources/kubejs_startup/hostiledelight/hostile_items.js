// requires: hostiledelight
// Hostile Delight item changes. Creative Delight startup script, loaded from the jar after the pack's startup_scripts,
// so global.item and global.removedItems exist here. Restart to apply.

// Prepared Sea Pickle is out (TODO dishes/pickles-and-ferments #12, 2026-11-02): hidden in JEI, tags stripped. Its
// recipes (upstream's broken one and the pack's cutting board one) are removed in data/creativedelight/kubejs/hostiledelight/recipes.js.
global.removedItems.push(global.item.preparedseapickle)
