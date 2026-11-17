# rustic-delight-port

> Rustic Delight 1.7.0 port nested in Creative Delight, MIT files baked in since 11-10 (bake_upstream.py); 1.20->1.19.2 gotchas beyond Miner's

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

2026-10-25: Rustic Delight ported as CD member `backportedRustic` (`development/creativeDelight/members/rustic`).
2026-11-10: MIT confirmed; the translator was replaced by `members/rustic/tools/bake_upstream.py`, which bakes the donor
(dev-only `rustic_upstream/`) into `src/main/resources` with a manifest; edit rules there and rebake, never hand-edit a
baked file (own files at baked paths go in `tools/own_overrides.json`). No runtime donor, no folder on clients.
State: `feature_writeup/rustic_delight.md` 5.14. Earlier method as [miners-delight-backport-plan](miners-delight-backport-plan.md).
- A removed id's file name can also be a shared parent (models/coaster/pancake.json is the six flavoured coasters'
  parent): drop by file name only after checking parents (`KEPT_SHARED`).

Gotchas found (add to the Miner's list):
- 1.20 loot also uses `minecraft:all_of`; 1.19.2 has none: translate to inverted(alternative(inverted...)).
- 1.19.2 rejects a whole tag for one unknown entry: make other-namespace entries `required: false`.
- Assets pass through too: 1.20 donors name FD 1.20 models/textures (`template_crop_cross` = 1.19.2 `crop_cross`;
  `platter.png` absent, now shipped in repackedFarmers). Check every non-own-namespace parent/texture in donor models.
- The recipe check (item/serializer missing) is now static against the last /dumpall: drop if the mod is in the pack, mod_loaded condition if not.
- FD 1.19.2 `FeastBlock` builds state with its own SERVINGS: a subclass with another range overrides
  `createBlockStateDefinition`, `getShape`, `getServingsProperty`, `getMaxServings` (props as static fields).
- Static init order: RDBlocks must read RDItems only inside suppliers (RDItems registers block items from RDBlocks).
- New ids only exist with the CD jar, so pack rows naming them go in the CD tree; a wild_crops.json row needing such a
  feature uses the `requires` key (`_biome_rules.js`).
- The 1.3.0-id `kubejs/data/rusticdelight/worldgen/*_feature.json` overrides and the pack recolours left kubejs on 11-10.

**Why:** owner asked to fold Rustic into CD (the 1.3.0 jar is out of `mods/`; state in rustic_delight.md). **How to apply:** check
rustic_delight.md 5.7 (cutover) before touching Rustic rows.
