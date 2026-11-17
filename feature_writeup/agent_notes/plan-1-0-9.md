# plan-1-0-9

> 1.0.9 release contents: old TODO 3 single-fix bugs fixed (patcherMod 1.0.9), rsec removed, duplicate-jar preflight; plan in feature_writeup/plan_1.0.9.md, deploy state in deploy.md

Moved from agent memory 2026-11-16 (last edited there 2026-10-19). Repo status docs win over any status line here.

2026-10-19, user: "fix the known bugs in the todo section 3, only ones with a single valid fix, not reworks or testing; check the Vinery/Brewin bugs still exist in the port first; tipped arrows have multiple efficiencies (intended); potion buckets show the duration applied to the player; leave cans alone".

Done (table in feature_writeup/plan_1.0.9.md 1; TODO.md 3 has a "Fixed 2026-10-19" paragraph): oven JEI [+] (SlotItemHandler.mayPickup false on empty slots), NEA isDiscoverable -> m_6592_, CRAFTING.FELSIC_SHIELD, air brake config read per use, DamageFlag forceLoad, ItemPhysicSplit if-chain, potion bucket tooltip x effectMult (global.potionArea.scaled), tipped-arrow comment, iron set dup, elytra tag helper, i.catfish_slice / i.bacon (now raw), zinc hammer-crushing, copper plate block, EVENT logs, sawdust planks, rsec -> disabledmods/, build_deploy dup mod id error. Brewin keg bottle and Vinery JEI 1800 bugs checked: gone in the ports.

Skipped as decisions: Spice of Life speed benefit (per-world mod default, value unclear), felsic potionMb, AP bullet category, engineering comments/recipes, Terralith caves, recipe scan rework.

**How to apply:** DEPLOYED 2026-10-19 15:54 (server + clients); nothing run in game yet (TODO `pack/release #4`); tests listed in plan_1.0.9.md 2. Related: [plan-1-0-8](plan-1-0-8.md), [deploy-pipeline](deploy-pipeline.md).
- 2026-10-19 15:50: rows QS (Quark spire tp mixin, scratch-tested) and EF (earth fill footprint share, campsite sunk starts, avoidCoasts) added to plan_1.0.9.md 0; mods/ jar now b82483aa (replaced the 13:50 5b7bb926). See [earth-fill-system](earth-fill-system.md), [meta-structures](meta-structures.md).
