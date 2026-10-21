ServerEvents.recipes(e => {
console.log('tipped arrow tipping starting')

var f = global.fluid
var R = global.NCH

// ── Machine tipping of arrows ────────────────────────────────────────────────
// Vanilla tips arrows in a crafting table: 8 arrows ringed around 1 lingering
// potion -> 8 tipped arrows (crafting_special_tippedarrow, still enabled). This
// mirrors that per-potion for the IE Bottling Machine and Create's Spout, using
// create:potion (Bottle:LINGERING) fluid. Each route has its own efficiency on
// purpose: the machines are cheaper per arrow than the grid (31.25 mB an arrow).
//
//   Crafting grid: 8 arrows + 1 lingering bottle (250 mB) -> 8 tipped arrows (31.25 mB each)
//   Create Spout : 1 arrow  + 25 mB LINGERING potion      -> 1 tipped arrow  (20% cheaper)
//   IE Bottler   : 1 arrow  + 20 mB LINGERING potion      -> 1 tipped arrow  (36% cheaper)
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

	// IE Bottling Machine — one arrow per operation
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
