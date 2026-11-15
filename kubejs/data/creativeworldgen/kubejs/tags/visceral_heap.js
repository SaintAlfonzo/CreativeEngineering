// requires: biomesoplenty
// Visceral Heap (feature_writeup/visceral_heap.md): BOP's own mineable tags are hidden with the rest of its data.

ServerEvents.tags('block', e => {
	var b = global.blocks
	global.tags.add(e, b.t_mineable_axe, [b.flesh, b.flesh_tendons, b.flesh_tendons_strand, b.eyebulb])
	// climb at ladder speed (plan_flesh_recipes.md 2.1); not villager ladders, villagers keep off the Heap
	global.tags.add(e, b.t_climbable, [b.flesh_tendons, b.flesh_tendons_strand])
	// The Heap is flesh at every depth (plan_bugfix_1114.md 6.4): Nether carvers cut only this tag, so without flesh in it
	// the Heap would have no caves. Flesh stays out of minecraft:base_stone_nether, so Nether ores do not place in it.
	global.tags.add(e, b.t_nether_carver_replaceables, [b.flesh, b.porous_flesh])
})
