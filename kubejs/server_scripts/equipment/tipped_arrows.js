ServerEvents.recipes(e => {
console.log('tipped arrow tipping starting')

var i = global.item
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
// One pair per registered potion that has effects, matching how JEI expands the vanilla special
// recipe (and how potion_bottling.js walks the registry).

var POTIONS = Java.loadClass('net.minecraft.core.Registry').POTION

var n = 0
var it = POTIONS.keySet().iterator()
while (it.hasNext()) {
	var potionKey = it.next()
	var pid = potionKey.toString()
	// no tipped arrow for a potion with no effects (water, awkward, thick, mundane)
	if (pid === 'minecraft:empty' || POTIONS.get(potionKey).getEffects().isEmpty()) continue
	var idBase = pid.split(':').join('_').split('/').join('_')

	var lingeringNbt = '{Bottle:"LINGERING",Potion:"' + pid + '"}'
	var resultNbt = '{Potion:"' + pid + '"}'

	// IE Bottling Machine — one arrow per operation
	R.ieBottling(Item.of(i.tipped_arrow, resultNbt), { tag: f.t_potion, amount: 20, nbt: lingeringNbt }, i.arrow, e, 'tipped_arrow/' + idBase)

	// Create Spout / filling — one arrow per operation
	R.createFilling(
		Item.of(i.tipped_arrow, resultNbt),
		[i.arrow, Fluid.of(f.create_potion, 25, lingeringNbt)],
		e,
		'tipped_arrow/' + idBase
	)

	n++
}

console.log('tipped arrow tipping done (' + n + ' potions x 2 recipes)')
})
