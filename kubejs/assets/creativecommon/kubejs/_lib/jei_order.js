// Hands the pack's JEI order (global.jeiOrder, startup_scripts/jei/order.js) to patcherMod's sorter,
// compat/jei/PackIngredientOrder. Runs at client script load (before JEI starts on joining a world) and
// again on /kubejs reload client_scripts, which re-sorts a running JEI. Tags still need /reload.
// No binding for the class: this Rhino throws "redeclaration of var" for a top-level const inside a try block.
try {
	Java.loadClass('com.SaintAlfonzo.creativecommon.compat.jei.PackIngredientOrder').define(global.jeiOrder.toJson())
} catch (err) {
	console.error('[jei_order] could not hand the JEI order to patcherMod: ' + err)
}
