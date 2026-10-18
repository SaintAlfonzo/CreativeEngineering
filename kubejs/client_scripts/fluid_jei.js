// Make every fluid in the game browsable/searchable in JEI's fluid ingredient list.
//
// JEI does NOT auto-populate that list from the fluid registry - only a fluid whose mod
// explicitly registers it as a JEI ingredient shows up there, and Create (for one) doesn't:
// its own JEI plugin only calls registerCategories/Recipes/RecipeCatalysts/FluidSubtypes/
// GuiHandlers, never registerIngredients. A fluid otherwise only appears via its bucket
// ITEM (if it has one) - a bucket-less fluid like create:potion or dragon's breath is
// completely invisible in JEI without this. JEIEvents.addFluids runs against the live
// IJeiRuntime (addIngredientsAtRuntime), so it can inject entries here without needing a
// full custom IModPlugin.
// Known-dead placeholder fluids - tags.js already strips these down to nothing
// (server_scripts/tags/fluidtags.js: "dead placeholder fluids"), so don't resurrect them
// into JEI as if they were real/usable. Listed as the JEI order's packHidden fluids
// (startup_scripts/jei/order.js), so the order report counts them as hidden by the pack.
const DEAD_FLUIDS = global.jeiOrder.packHidden('fluid')

JEIEvents.addFluids(e => {
	const ForgeRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')

	// same registry-iteration idiom as the working /dumpfluids command (server_scripts/
	// _lib/dump_fluid.js) - ForgeRegistries.FLUIDS.getKeys() over Registry.keySet(), proven
	// to enumerate every fluid id including modded ones.
	let n = 0
	ForgeRegistries.FLUIDS.getKeys().forEach(rl => {
		const id = rl.toString()
		if (id === 'minecraft:empty') return
		// every "flowing_x" id is the moving duplicate of a still "x" fluid (universal
		// Forge/vanilla naming convention, confirmed against this pack's own
		// kubejs/exported/fluids.json) - skip it, the still form is the one worth browsing.
		if (rl.getPath().startsWith('flowing_')) return
		// create:potion needs one entry per potion (with NBT) below, not one bare/empty one
		if (id === 'create:potion') return
		if (DEAD_FLUIDS.indexOf(id) !== -1) return

		e.add(Fluid.of(id, 1000))
		n++
	})

	console.log(`[fluid_jei] added ${n} fluids to JEI`)
})

// create:potion is a special case: the fluid identity that actually matters is its NBT
// (`Potion` / `Bottle`), so add one REGULAR-bottle stack per valid potion instead of a bare
// no-NBT one. Same "every non-empty potion in registry order" loop used to generate the
// create:potion bottling recipes - see server_scripts/engineering/potion_bottling.js.
JEIEvents.addFluids(e => {
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
