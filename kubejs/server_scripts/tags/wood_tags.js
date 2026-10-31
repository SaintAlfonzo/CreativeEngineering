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
// Smokable wood: every wood type, per block shape (smoking recipes in wood/smoked_wood.js)
// ============================================================
// Expanded from the vanilla / forge wood tags as they stand when this runs. Left out: the smoked blocks themselves,
// polished and vertical variants, beams, Botania's magic woods and, for logs, the other half of the log / wood pair.
var smokeExcept = ['*:*smoked*', '*:*polished*', '*:*vertical*']
t.addMatching(e, 'creativeengineering:smokable/log', '#minecraft:logs', { except: smokeExcept.concat(['*:*stripped*','*:*_wood','*:*_hyphae','*:*_beam','botania:*','*:driftwood']) })
t.addMatching(e, 'creativeengineering:smokable/wood', '#minecraft:logs', { except: smokeExcept.concat(['*:*stripped*','*:*_log','*:*_stem','*:*_beam','botania:*','*:driftwood','*:driftwood_log']) })
t.addMatching(e, 'creativeengineering:smokable/log_stripped', '#forge:stripped_logs', { except: smokeExcept })
t.addMatching(e, 'creativeengineering:smokable/wood_stripped', '#forge:stripped_wood', { except: smokeExcept })
t.addMatching(e, 'creativeengineering:smokable/planks', '#minecraft:planks', { except: smokeExcept })
t.addMatching(e, 'creativeengineering:smokable/slab', '#minecraft:wooden_slabs', { except: smokeExcept })
t.addMatching(e, 'creativeengineering:smokable/stairs', '#minecraft:wooden_stairs', { except: smokeExcept })
t.addMatching(e, 'creativeengineering:smokable/fence', '#minecraft:wooden_fences', { except: smokeExcept })
t.addMatching(e, 'creativeengineering:smokable/fencegate', '#forge:fence_gates/wooden', { except: smokeExcept })
t.addMatching(e, 'creativeengineering:smokable/door', '#minecraft:wooden_doors', { except: smokeExcept })
t.addMatching(e, 'creativeengineering:smokable/trapdoor', '#minecraft:wooden_trapdoors', { except: smokeExcept })
t.addMatching(e, 'creativeengineering:smokable/button', '#minecraft:wooden_buttons', { except: smokeExcept })
t.addMatching(e, 'creativeengineering:smokable/pressureplate', '#minecraft:wooden_pressure_plates', { except: smokeExcept })

console.log('wood tag finished')
})
