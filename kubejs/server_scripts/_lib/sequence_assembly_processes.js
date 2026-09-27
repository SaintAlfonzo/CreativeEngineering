// JEI showcase / test recipe: every process this pack can legally use as a Create
// Sequenced Assembly step, all in one chain, nether_star -> nether_star.
//
// "Legal" here means the recipe class implements both Create's ProcessingRecipe and its
// IAssemblyRecipe interface (verified by decompiling the installed jars, not guessed) —
// that's the only real gate; SequencedRecipe.fromJson checks exactly those two things and
// otherwise throws "Not a supported recipe type". Only 3 jars in this whole modpack
// contain any IAssemblyRecipe implementer: create, createaddition and vintageimprovements.
// That's 15 processes total, all included below:
//   Create (4):          Cutting, Deploying, Pressing, Filling
//   Create Addition (1): Rolling
//   Vintage Improvements (10): Polishing, Laser Cutting, Vibrating, Turning,
//                               Vacuumizing, Pressurizing, Curving, Hammering,
//                               Coiling, Centrifugation
//
// Explicitly NOT included because they can't be, regardless of effort — confirmed by
// checking their recipe classes' superclass, not just "no helper exists":
//   Create:            Milling, Crushing, Mixing, Compacting, Mechanical Crafting,
//                       Emptying, Splashing, Haunting, Blasting, Smoking, Sand Paper
//                       Polishing, Toolbox Dyeing — none implement IAssemblyRecipe.
//   Create Addition:    Charging, Liquid Burning — ChargingRecipe implements vanilla
//                       Recipe directly, never extends ProcessingRecipe at all, so no
//                       interface could make it eligible.
//   Immersive Engineering: Metal Press, Crusher, Alloy, Arc Furnace, Squeezer, Bottling
//                       Machine, Sawmill, Blueprint — zero Create compat in the jar.
//
// Per-step helper usage note: several NCH helpers (createDeploying, createFilling,
// createRolling, viLaserCutting, viVibrating) call .id() on the recipe themselves and
// return undefined, because they're meant to register a *standalone* top-level recipe.
// That means they can't be dropped into a sequence array (it would push `undefined`).
// Only viPolishing was written to return the bare recipe object for this exact purpose
// (see its comment in neoCraftHelper.js) — it's the one helper usable inline below.
// Everywhere else the step is written the same way this file's existing sequences
// already do it: call the bare `e.recipes.create.X(...)` builder or a raw `e.custom({...})`
// directly, with no `.id()` — the outer createSequencedAssembly call ids each step itself.
//
// Transitional item: this file lives in server_scripts, so it runs after the startup
// registry phase and can't call StartupEvents.registry to make its own incomplete sprite.
// It falls back to the shared generic 'kubejs:incomplete_item' (i.incomplete) instead —
// expect NCH's "no incomplete item registered" WARN in the log for this one recipe; it's
// harmless and expected for a throwaway JEI test chain.

var R = global.NCH
console.log('Hello! The sequence assembly test event has started!')

ServerEvents.recipes(e => {
	const i = global.item
	const f = global.fluid
	const inc = i.incomplete

    // TODO: change helper to return recipe for all valid sequence processes, make comment explaining in each helper func

	R.createSequencedAssembly(Item.of(i.nether_star, 1), i.nether_star, [

		// ── Create (native, 4) ──────────────────────────────────────────────
		// Cutting/Pressing have no NCH wrapper at all; Deploying/Filling have one
		// (createDeploying/createFilling) but it calls .id() itself, so — like every
		// other sequence in this pack — call the bare builder here instead.
		e.recipes.create.cutting(inc, inc),
		e.recipes.create.deploying(inc, [inc, i.nether_star]),
		e.recipes.create.filling(inc, [inc, Fluid.of(f.water, 250)]),
		e.recipes.create.pressing(inc, inc),

		// ── Create Addition (1) ──────────────────────────────────────────────
		// createRolling exists but also calls .id() itself — raw json, same shape it builds.
		e.custom({ type: 'createaddition:rolling', input: { item: inc }, result: { item: inc } }),

		// ── Vintage Improvements (10) ────────────────────────────────────────
		// viPolishing is the only VI helper written to return the bare recipe - use it directly.
		R.viPolishing(inc, inc, e),
		// viLaserCutting / viVibrating exist but call .id() themselves - raw json instead
		// (identical shape to what those helpers build).
		e.custom({ type: 'vintageimprovements:laser_cutting', ingredients: [{ item: inc }], results: [{ item: inc }] }),
		e.custom({ type: 'vintageimprovements:vibrating', ingredients: [{ item: inc }], results: [{ item: inc }] }),
		// No NCH helper exists yet for any of the following 7 - raw json for all of them,
		// field shapes taken from vintageimprovements' own shipped datapack recipes.
		e.custom({ type: 'vintageimprovements:turning', ingredients: [{ item: inc }], results: [{ item: inc }] }),
		e.custom({ type: 'vintageimprovements:vacuumizing', ingredients: [{ item: inc }], results: [{ item: inc }], processingTime: 100 }),
		e.custom({ type: 'vintageimprovements:pressurizing', ingredients: [{ item: inc }], results: [{ item: inc }], processingTime: 100 }),
		// mode is required (curving press has multiple shape modes) - 1 matches a real
		// shipped recipe (curving/iron_sheet.json); meaning of the int isn't documented.
		e.custom({ type: 'vintageimprovements:curving', mode: 1, ingredients: [{ item: inc }], results: [{ item: inc }] }),
		// hammerBlows is required (how many Helve Hammer hits) - 1 is the minimum.
		e.custom({ type: 'vintageimprovements:hammering', hammerBlows: 1, ingredients: [{ item: inc }], results: [{ item: inc }] }),
		// springColor is required - reuse the incomplete item's own default grey tint.
		e.custom({ type: 'vintageimprovements:coiling', springColor: '808080', ingredients: [{ item: inc }], results: [{ item: inc }] }),
		e.custom({ type: 'vintageimprovements:centrifugation', ingredients: [{ item: inc }], results: [{ item: inc }] })

	], null, e, 'seq_test_star_all_processes', 1)
});

console.log('Hello! The sequence assembly test event has ended!')
