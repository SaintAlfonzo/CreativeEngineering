ServerEvents.recipes(e => {
console.log('tipped arrow tipping starting')

var f = global.fluid
var R = global.NCH

// ── Machine tipping of arrows ────────────────────────────────────────────────
// Vanilla tips arrows in a crafting table: 8 arrows ringed around 1 lingering
// potion -> 8 tipped arrows (crafting_special_tippedarrow, still enabled). This
// mirrors that per-potion for the IE Bottling Machine and Create's Spout, using
// create:potion (Bottle:LINGERING) fluid, at the SAME potion cost as the hand
// craft: 8 tipped arrows per 250 mB (= one lingering bottle).
//
//   IE Bottler : 8 arrows + 250 mB LINGERING potion -> 8 tipped arrows   (exact)
//   Create Spout: 1 arrow  +  31 mB LINGERING potion -> 1 tipped arrow    (the
//     spout fills one item per operation, so 250/8 = 31.25 mB is floored to 31;
//     ~248 mB per 8, i.e. within a whisker of the hand craft.)
//
// One pair per registered potion, matching how JEI expands the vanilla special
// recipe (and how potion_bottling.js walks the registry).

var POTIONS = Java.loadClass('net.minecraft.core.Registry').POTION

var n = 0
var it = POTIONS.keySet().iterator()
while (it.hasNext()) {
	var pid = it.next().toString()
	if (pid === 'minecraft:empty') continue
	var idBase = pid.split(':').join('_').split('/').join('_')

	var lingeringNbt = '{Bottle:"LINGERING",Potion:"' + pid + '"}'
	var resultNbt = '{Potion:"' + pid + '"}'

	// IE Bottling Machine — batch of 8, exact hand-craft ratio
	e.custom({
		type: 'immersiveengineering:bottling_machine',
		fluid: { tag: 'forge:fluid/potion', amount: 20, nbt: lingeringNbt },
		inputs: [{ item: 'minecraft:arrow', count: 1 }],
		results: [{ item: 'minecraft:tipped_arrow', count: 1, nbt: resultNbt }]
	}).id('creativeengineering:bottling/tipped_arrow/' + idBase)

	// Create Spout / filling — one arrow per operation
	R.createFilling(
		Item.of('minecraft:tipped_arrow', resultNbt),
		[Item.of('minecraft:arrow'), Fluid.of(f.create_potion, 25, lingeringNbt)],
		e,
		'tipped_arrow/' + idBase
	)

	n++
}

console.log('tipped arrow tipping done (' + n + ' potions x 2 recipes)')
})
