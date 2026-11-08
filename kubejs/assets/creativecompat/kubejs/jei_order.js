// Hands the pack's JEI order (global.jeiOrder, startup_scripts/jei/order.js) to Creative Compat's sorter,
// compat/jei/PackIngredientOrder. Runs at client script load (before JEI starts on joining a world) and
// again on /kubejs reload client_scripts, which re-sorts a running JEI. Tags still need /reload.
// No binding for the class: this Rhino throws "redeclaration of var" for a top-level const inside a try block.
// Only with JEI (optional for Compat), and only when the pack defines an order: a game without the pack's
// startup_scripts/jei/order.js has an empty definition and keeps JEI's own order. The call goes through
// JeiOrderBridge (no JEI types), which checks the JEI version range before it touches the sorter.
if (Platform.isLoaded('jei')) {
	var jeiTypes = global.jeiOrder ? global.jeiOrder.definition().types : null
	if (jeiTypes && jeiTypes.length > 0) {
		try {
			Java.loadClass('com.SaintAlfonzo.creativecompat.compat.jei.JeiOrderBridge').define(global.jeiOrder.toJson())
		} catch (err) {
			console.error('[jei_order] could not hand the JEI order to Creative Compat: ' + err)
		}
	} else {
		console.info('[jei_order] no order defined (startup_scripts/jei/order.js), JEI keeps its own order')
	}
}
