// requires: hostiledelight
// Hostile Delight item changes. Creative Delight startup script, loaded from the jar after the pack's startup_scripts,
// so global.item and global.removedItems exist here. Restart to apply.

// Prepared Sea Pickle is out (TODO dishes/pickles-and-ferments #12): hidden in JEI, tags stripped. Its recipes
// (upstream's broken one and the pack's cutting board one) are removed in
// data/creativedelight/kubejs/hostiledelight/recipes.js.
global.removedItems.push(global.item.preparedseapickle)

// Rotten foods (largecontent/mod-additions #48, plan_owner_rows_1120.md R1): food type hazard like rotten flesh and the
// rotten apple (food_types.json), each with a 20% chance of Poison II for 5 s. Hunger and saturation stay.
ItemEvents.modification(e => {
	var i = global.item
	;[i.rottenham, i.cookedrottenham, i.rottenpie, i.greenpie, i.friedturtleegg, i.suspiciousbaconandaggs].forEach(id =>
		e.modify(id, item => item.setFoodProperties(fb => fb.effect(global.effect.poison, 5 * 20, 1, 0.2))))
})
