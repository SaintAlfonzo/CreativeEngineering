# forge-missing-mappings-staging

> Forge 1.19.2 MissingMappingsEvent comes from the STAGING registry; resolve remap targets in RegistryManager.ACTIVE; chunk palettes are by name

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

On a world load Forge fires `MissingMappingsEvent` from `GameData.injectSnapshot`'s STAGING registry copy, which then
holds only the save's snapshot ids. `mapping.getRegistry().containsKey(newId)` is false for any id new in this version,
so a remapper must look the survivor up in `RegistryManager.ACTIVE.getRegistry(key)` (Forge's `processMissingEvent`
resolves the target through the ACTIVE pool anyway). Found when patcherMod 1.1.06 `SwapRemapper` left the renamed
treated-stick blocks missing; fixed in 1.1.07.

Blocks in chunk sections are saved by NAME. A world opened once without the remap drops the old id from level.dat's
snapshot while the palettes keep it, so no later boot fires the event for it: test a rename on a fresh old-id save.

**Why:** this cost a full scratch rerun on 2026-11-01. **How to apply:** any id rename or swap (memory `creative-delight-routing`
swappers), and scratch tests of it (memory `scratch-server-verify`).
