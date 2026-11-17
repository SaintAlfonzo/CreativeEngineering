# miners-delight-backport-plan

> Miner's Delight merged backport: license is ARR so use the Vinery donor-jar method; hot cocoa cup and (2026-11-02) the whole copper cup family dropped; base facts

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

2026-10-24: plan in `feature_writeup/miners_delight.md` (state lives there). Rules the owner set:
- Miner's Delight is **all rights reserved** (code and assets, even though the 1.21.1 toml says MIT): backport like Vinery ([backported-vinery](backported-vinery.md)): upstream jar stays unmodified in its own folder as a donor, our jar ships only our own code written from behavior, never translated from the decompile.
- `hot_cocoa_cup` (only in 1.1.2) is dropped on purpose because of the license; no conversion to another id.
- 2026-11-02 owner: the Copper Cup and every cup (water, milk, powder snow, 15 soups, Delightful's cactus soup cup) are removed outright, no conversion; ids in `MinersTranslator.REMOVED`. Do not reintroduce cup serving.
- Miner's Delight 1.1.2 reached players with pack 1.1.02 (`deploy/DEPLOY_LOG.md`), so every other 1.1.2 id must keep working.
- The donor is the official Modrinth file `miners_delight-1.20.1-1.4.5-backport.jar` (project qMxbM4BQ), copied to `miners_upstream/` (Forge 1.20.1, all 1.4.5 content, mod id `miners_delight`, no Lodestone). Port: `development/creativeDelight/members/miners` (a CD member; folder moved 2026-11-02) (state in `feature_writeup/miners_delight.md`). Create Central Kitchen has 3 mixins into `CopperPotBlockEntity` (`processCooking` -> `setRecipeUsed`) and `StickyBasketBlockEntity` (`setTransferCooldown`, `isOnTransferCooldown`): keep those shapes.
- Placed features that match only `air` miss carver caves (`cave_air`); the cave carrots accept both, the cave mushrooms do not.

Gotchas found building it (2026-10-24), useful for any 1.20 -> 1.19.2 donor port:
- 1.20 loot data uses `minecraft:any_of`; 1.19.2 only knows `minecraft:alternative` (translator renames it).
- Upstream `global_loot_modifiers.json` can list modifiers with no file; drop dangling entries.
- `SimpleJsonResourceReloadListener("cup_conversion")` reads `data/<ns>/cup_conversion/`, not `data/<ns>/miners_delight/...`.
- Calling `FoodProperties.getEffects()` while items register resolves FD effect suppliers too early; read the raw `effects` field via an accessor mixin.
- Upstream Miner's pot/basket mirror FD's MIT pot/basket, so building on FD's classes keeps the code ours.

**Why:** the owner decides drops only after seeing overlaps in game. **How to apply:** start from the plan's D-table and P-steps; do not ship upstream files.
