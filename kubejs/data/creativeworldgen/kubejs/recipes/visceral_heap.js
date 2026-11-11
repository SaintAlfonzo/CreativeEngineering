// requires: biomesoplenty
// Visceral Heap recipes (feature_writeup/plan_flesh_recipes.md, visceral_heap.md). BOP ships no recipes for any Heap
// block. Crushing turns the blocks into drops, heated mixing renders them to blood, and two mixing recipes cultivate
// flesh and porous flesh. Values and the no-loop check: plan_flesh_recipes.md sections 3 and 4. The flesh crushes use
// result pools, which need Creative Engineering (fan/ResultPools); without it every item rolls on its own.

ServerEvents.recipes(e => {
	const R = global.NCH
	var i = global.item
	var f = global.fluid

	R.withNamespace('creativeworldgen', () => {

		// ============================================================
		// Crushing (wheels, with a millstone mirror)
		// ============================================================

		// Result pools (plan section 3): per crush each pool gives its whole amount in picks plus one more at the
		// fraction, each pick one item by weight. A chance here is the item's overall chance per crush (pool amount
		// times its weight), so a pool's chances add up to its amount: flesh 1 meat, 0.75 bone, 0.25 eye, 0.25 misc;
		// porous flesh 1.5 meat, 1 eye, 0.5 misc, no bone.
		var crushPools = (id, input, pools) => {
			var outputs = []
			var indices = pools.map(pool => pool.map(entry => {
				outputs.push(Item.of(entry[0], 1).withChance(entry[1]))
				return outputs.length - 1
			}))
			R.createCrushing(outputs, input, e, 'visceral_heap/' + id, false, { pools: indices })
			R.createMilling(outputs, input, e, 'visceral_heap/' + id, { pools: indices })
		}

		crushPools('flesh', i.flesh, [
			[[i.rotten_flesh, 0.35], [i.rottenham, 0.20], [i.leather, 0.15], [i.spider_leg, 0.10], [i.hoglin_ear, 0.10],
				[i.hoglin_loin, 0.10]],
			[[i.bone, 0.30], [i.bonemeal, 0.25], [i.broken_bone, 0.10], [i.bone_chunk, 0.08], [i.bone_serpent_tooth, 0.02]],
			[[i.spider_eye, 0.12], [i.eyebulb, 0.08], [i.fermented_spider_eye, 0.05]],
			[[i.pus_bubble, 0.08], [i.flesh_tendons, 0.09], [i.hair, 0.08]]
		])
		crushPools('porous_flesh', i.porous_flesh, [
			[[i.rotten_flesh, 0.525], [i.rottenham, 0.30], [i.leather, 0.225], [i.spider_leg, 0.15], [i.hoglin_ear, 0.15],
				[i.hoglin_loin, 0.15]],
			[[i.spider_eye, 0.50], [i.eyebulb, 0.30], [i.fermented_spider_eye, 0.20]],
			[[i.pus_bubble, 0.17], [i.flesh_tendons, 0.18], [i.hair, 0.15]]
		])

		// plain chance outputs, no pools; nothing drops on a failed roll (plan 2.4)
		R.createCrushing(Item.of(i.slime, 1).withChance(0.25), i.pus_bubble, e, 'visceral_heap/pus_bubble')
		R.createMilling(Item.of(i.slime, 1).withChance(0.25), i.pus_bubble, e, 'visceral_heap/pus_bubble')
		R.createCrushing(Item.of(i.string, 1).withChance(0.5), i.hair, e, 'visceral_heap/hair')
		R.createMilling(Item.of(i.string, 1).withChance(0.5), i.hair, e, 'visceral_heap/hair')

		// ============================================================
		// Blood (heated mixing; 250 mB blood = 1 flesh of blood)
		// ============================================================

		R.createMixing([Item.of(i.bone, 1).withChance(0.25), Fluid.of(f.blood, 250)], i.flesh, e,
			'visceral_heap/flesh_blood', 'heated')
		R.createMixing(Fluid.of(f.blood, 500), i.porous_flesh, e, 'visceral_heap/porous_flesh_blood', 'heated')
		R.createMixing(Fluid.of(f.blood, 250), i.flesh_tendons, e, 'visceral_heap/flesh_tendons_blood', 'heated')

		// ============================================================
		// Cultivation
		// ============================================================

		// the 2 porous flesh are a catalyst: 1000 mB of any plant oil nets 1 flesh
		R.createMixing([Item.of(i.porous_flesh, 2), Item.of(i.flesh, 3)],
			[Item.of(i.porous_flesh, 2), Item.of(i.flesh, 2), R.createFluidTag(f.t_plantoil, 1000)], e,
			'visceral_heap/flesh_cultivation')
		R.createMixing(Item.of(i.porous_flesh, 1), [Item.of(i.flesh_tendons, 2), i.flesh, Fluid.of(f.blood, 250)], e,
			'visceral_heap/porous_flesh_cultivation')
	})
})
