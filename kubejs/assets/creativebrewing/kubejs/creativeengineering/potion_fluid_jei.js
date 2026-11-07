// requires: creativebrewing
// Moved from the pack's client_scripts/fluid_jei.js 2026-11-01 (creative_brewing.md 4.5), unchanged. Runs after the
// pack's own JEIEvents.addFluids pass (all registered fluids), as it did before.
// create:potion is a special case: the fluid identity that actually matters is its NBT
// (`Potion` / `Bottle`), so add one REGULAR-bottle stack per valid potion instead of a bare
// no-NBT one. Same "every non-empty potion in registry order" loop used to generate the
// create:potion bottling recipes - see ../../../data/creativebrewing/kubejs/creativeengineering/potion_bottling.js.
JEIEvents.addFluids(e => {
	if (!global.has('create')) return
	const POTIONS = Java.loadClass('net.minecraft.core.Registry').POTION

	let n = 0
	const it = POTIONS.keySet().iterator()
	while (it.hasNext()) {
		var pid = it.next().toString()   // var: a loop-body const is stuck on its first value in this Rhino
		if (pid === 'minecraft:empty') continue

		// REGULAR bottle type - the base fluid representation of this potion. Splash/
		// lingering are the same colour/tooltip and only differ by an internal NBT key, so
		// they'd just be visually-duplicate ingredient-list entries.
		e.add(Fluid.of('create:potion', 1000, '{Bottle:"REGULAR",Potion:"' + pid + '"}'))
		n++
	}

	console.log(`[fluid_jei] added ${n} create:potion fluid stacks to JEI`)
})
