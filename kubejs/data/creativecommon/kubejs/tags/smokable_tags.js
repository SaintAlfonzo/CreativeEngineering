// Creative Common; moved from Creative Engineering's tags/wood_tags.js 2026-11-10 (plan_cd_namespace.md, owner decision).
ServerEvents.tags('item', e => {
var i = global.item;
var t = global.tags

// ============================================================
// Smokable wood: vanilla overworld woods, per block shape (smoking recipes in wood/smoked_wood.js)
// ============================================================
// Allow-list (decided 2026-11-01, engineering/dreams-and-desires #10): only the seven vanilla overworld woods smoke.
// Every modded wood (Botania, driftwood, Quark ...) and the nether woods (crimson, warped) are left out. The item_dict
// keys are <shape>_<wood>, with stripped logs / wood as log_<wood>_stripped / wood_<wood>_stripped.
var smokeWoods = ['oak', 'spruce', 'birch', 'jungle', 'acacia', 'dark_oak', 'mangrove']
var smokeShapes = {
	log: w => 'log_' + w,
	wood: w => 'wood_' + w,
	log_stripped: w => 'log_' + w + '_stripped',
	wood_stripped: w => 'wood_' + w + '_stripped',
	planks: w => 'planks_' + w,
	slab: w => 'slab_' + w,
	stairs: w => 'stairs_' + w,
	fence: w => 'fence_' + w,
	fencegate: w => 'fencegate_' + w,
	door: w => 'door_' + w,
	trapdoor: w => 'trapdoor_' + w,
	button: w => 'button_' + w,
	pressureplate: w => 'pressureplate_' + w,
}
Object.keys(smokeShapes).forEach(shape => {
	var ids = smokeWoods.map(w => i[smokeShapes[shape](w)])
	if (ids.some(id => !id)) console.warn('wood_tags: missing item_dict key for creativecommon:smokable/' + shape)
	t.add(e, 'creativecommon:smokable/' + shape, ids.filter(id => id))
})
})
