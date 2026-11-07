// requires: creativedelight
// Mulch tags (what the compost and fertile soil recipes take, Creative Engineering wood/sawdust.js) and the seeds the
// millstone turns into bone meal (../create/seed_milling.js). Moved from Creative Engineering's server scripts
// (tags/wood_tags.js) 2026-11-06, rows and tag ids unchanged.
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item

	t.add(e, 'forge:mulch/brown', [i.sawdust, i.straw])
	t.add(e, 'forge:mulch/bone', [i.bone, i.bonemeal])
	t.add(e, 'forge:mulch/waste', [i.rotten_flesh, i.spider_eye, i.poisonous_potato])
	t.add(e, 'forge:mulch/green', [i.moss, i.moss_clump, i.moss_paste, i.grass, i.tall_grass, i.fern, i.large_fern])
	t.add(e, 'forge:mulch/fungus', [i.red_mushroom, i.brown_mushroom, i.red_mushroom_colony, i.brown_mushroom_colony])
	t.add(e, 'forge:mulch/fertilizer', [i.ash, i.dust_saltpeter, i.fertilizer])

	// Seeds the millstone turns into bone meal. Corn kernels mill into cornmeal and rice is food. The '#' entries are
	// expanded to their members here, so a seed added to #forge:seeds by a later script is not copied.
	t.addMatching(e, 'creativeengineering:millable_seeds', [i.t_seeds, i.t_seeds_c], { except: [i.t_seeds_corn, i.t_seeds_rice] })
})
