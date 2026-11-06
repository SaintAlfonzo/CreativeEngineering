// Creative Engineering; moved from kubejs/server_scripts/tags/wood_tags.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
ServerEvents.tags('item', e => {
console.log('wood tag started')

var i = global.item;
var t = global.tags

// ============================================================
// Mulch
// ============================================================

t.add(e, 'forge:mulch/brown', [i.sawdust, i.straw])
t.add(e, 'forge:mulch/bone', [i.bone, i.bonemeal])
t.add(e, 'forge:mulch/waste', [i.rotten_flesh, i.spider_eye, i.poisonous_potato])
t.add(e, 'forge:mulch/green', [i.moss, i.moss_clump, i.moss_paste, i.grass, i.tall_grass, i.fern, i.large_fern])
t.add(e, 'forge:mulch/fungus', [i.red_mushroom, i.brown_mushroom, i.red_mushroom_colony, i.brown_mushroom_colony])
t.add(e, 'forge:mulch/fertilizer', [i.ash, i.dust_saltpeter, i.fertilizer])

// Seeds the millstone turns into bone meal (wood/compost.js). Corn kernels mill into cornmeal and rice is food. The
// '#' entries are expanded to their members here, so a seed added to #forge:seeds by a later script is not copied.
t.addMatching(e, 'creativeengineering:millable_seeds', [i.t_seeds, i.t_seeds_c], { except: [i.t_seeds_corn, i.t_seeds_rice] })

// ============================================================
// Wooden barrel
// ============================================================
t.add(e, ['vinery:barrel', 'vinery:wooden_barrels'], i.barrel)

// ============================================================
// Treated wood filling: planks that are not smoked
// ============================================================
// the smoked planks have their own cheaper creosote recipe (wood/smoked_wood.js); expanded from #minecraft:planks
// as it stands when this runs
t.addMatching(e, 'creativeengineering:unsmoked_planks', '#minecraft:planks', { except: i.smoked_planks })

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
	if (ids.some(id => !id)) console.warn('wood_tags: missing item_dict key for creativeengineering:smokable/' + shape)
	t.add(e, 'creativeengineering:smokable/' + shape, ids.filter(id => id))
})

console.log('wood tag finished')
})
