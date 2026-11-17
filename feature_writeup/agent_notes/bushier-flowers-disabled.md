# bushier-flowers-disabled

> Bushier Flowers growth/worldgen disabled in favor of Flower Patch; grown bushes crafted from 4 loose flowers (2026-09-29)

Moved from agent memory 2026-11-16 (last edited there 2026-09-30). Repo status docs win over any status line here.

User prefers Flower Patch's mechanics over Bushier Flowers (both installed). 2026-09-29:
- `mixin/BushierFlowersDisableMixin` (Creative Common, jar 1.0.2 rebuilt in place, not bumped) empties
  `ModBlocks.validFlowers` / `grownFlowers` at `<clinit>` TAIL (@Shadow @Mutable). Both of the mod's
  growth paths (its BoneMealItem.useOn HEAD inject + bone-meal dispenser behavior) key off those
  lists, so they fall through to vanilla and Flower Patch. `bushierflowers-0.0.2-1.19.2.jar` added to creativeCommon/libs.
- Worldgen: `kubejs/data/bushierflowers/forge/biome_modifier/add_grown_flower_*.json` (5) overridden with `forge:none`.
- Recipes: `kubejs/server_scripts/other/flowers.js`, one R.bulk('shapeless') of 4 loose -> 1 grown; `i.grown_*` keys in item_dict.
- Scratch-server verified: mixin applies, dispensed bone meal turns a poppy into `flowerpatch:poppy_patch`, grown poppy no longer pops a flower.
  Not verified: worldgen absence (forge:none is standard), the recipes in JEI.

**Why:** the Flower Patch mechanics work better. **How to apply:** don't re-enable bushier growth; the grown blocks stay registered for existing bushes, dye recipes and pots. Related: [patchermod-coremod](patchermod-coremod.md), memory `scratch-server-verify`, [deploy-pipeline](deploy-pipeline.md).
