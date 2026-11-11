// requires: biomesoplenty
// Visceral Heap (feature_writeup/visceral_heap.md): BOP's own mineable tags are hidden with the rest of its data.

ServerEvents.tags('block', e => {
	var b = global.blocks
	global.tags.add(e, b.t_mineable_axe, [b.flesh, b.flesh_tendons, b.flesh_tendons_strand, b.eyebulb])
	// climb at ladder speed (plan_flesh_recipes.md 2.1); not villager ladders, villagers keep off the Heap
	global.tags.add(e, b.t_climbable, [b.flesh_tendons, b.flesh_tendons_strand])
})
