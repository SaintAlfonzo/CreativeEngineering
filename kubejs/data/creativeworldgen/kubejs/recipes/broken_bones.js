// requires: philipsruins
// SPDX-License-Identifier: GPL-3.0-only
// Philips Ruins' Broken Bone and Bone Chunk (Ancient Remains drops in the Visceral Heap, visceral_heap.md 6) in the
// pack's machines (TODO worldgen/biomes #15, plan_easy_fixes_1122.md D2): milled or crushed into bone meal, and a bone
// pressed into a Broken Bone. A bone gives less bone meal this way than milled whole, so there is no loop. The NCH
// helpers skip themselves without Create or Immersive Engineering.

ServerEvents.recipes(e => {
	const R = global.NCH.forPack('creativeworldgen')
	var i = global.item

	R.withNamespace('creativeworldgen', () => {
		;[
			['broken_bone', i.broken_bone, 2],
			['bone_chunk', i.bone_chunk, 1],
		].forEach(([id, input, meal]) => {
			R.createMilling(Item.of(i.bonemeal, meal), input, e, 'bones/' + id)
			R.createCrushing([Item.of(i.bonemeal, meal), Item.of(i.bonemeal, 1).withChance(0.25)], input, e, 'bones/' + id)
			R.ieCrushing(Item.of(i.bonemeal, meal), input, e, 'bones/' + id, [{ output: i.bonemeal, chance: 0.25 }], 1600)
		})

		R.createPressing(Item.of(i.broken_bone, 1), i.bone, e, 'bones/bone')
	})
})
