// priority: 1000
// requires: creativeengineering
// Creative Engineering server scripts (feature_writeup/creative_engineering.md, creative_split.md 3.9, 7.6, wave 8).
// Authored here in the pack folder (loads on /reload); the Engineering build copies this tree into the jar at the same
// path (data/creativeengineering/kubejs/). Folders keep the pack's layout: engineering/ (components, blueprints,
// machines, redstone, transport, computing), metal/ (base materials, building, misc crafts), wood/ (types, bark,
// sawdust, smoked wood, ash; removed_woods.js stays in the pack, it runs before item_merges.js), tags/ (engineering, fluid, metal, storage block and wood tags). Recipe ids
// stay creativeengineering:<type>/<suffix>: NCH's default namespace is this jar's own.
// Each script pack has its own scope: these scripts read only global.* (NCH, tags, item, fluid) at event time.
ServerEvents.loaded(e => {
	if (!global.CEN) console.error('[creativeengineering] global.CEN missing: the Engineering jar\'s startup scripts did not ' +
		'run (Creative Common without the startup loader, or no creativeengineering jar in mods/)')
})
