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
// (startup_scripts/jei/order.js), so the order report counts them as hidden by the pack. global.jeiOrder is
// Creative Compat's; without Compat no fluid is skipped.
// Only with JEI: JEIEvents does not exist without it (Common alone).
if (Platform.isLoaded('jei')) {
JEIEvents.addFluids(e => {
	const DEAD_FLUIDS = global.jeiOrder ? global.jeiOrder.packHidden('fluid') : []
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
		if (rl.getNamespace() === 'create' && rl.getPath() === 'potion') return
		if (DEAD_FLUIDS.indexOf(id) !== -1) return

		e.add(Fluid.of(id, 1000))
		n++
	})

	console.log(`[fluid_jei] added ${n} fluids to JEI`)
})
}

// The create:potion stacks (one REGULAR bottle per potion): Creative Brewing,
// kubejs/assets/creativebrewing/kubejs/creativebrewing/potion_fluid_jei.js (moved 2026-11-01).
