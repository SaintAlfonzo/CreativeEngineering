# Vine shearing

## 0. At a glance

| Part | Status | Files | Knobs |
|---|---|---|---|
| Shears stop modded vines (Floral Enchantment, Botania solid vine, any later `VineBlock`) | BUILT 2026-10-31, patcherMod 1.1.04 installed 2026-10-31 00:44; scratch server PASSED r1031 (2026-10-31, SQ-VS-01 to -08) | `block/VineShearing`, `mixin/VineBlockShearMixin` | `[vine_shearing] enabled`, `cutBelow` |
| Shears stop weeping, twisting, cave vine and kelp tips (1.20 behaviour) | BUILT 2026-10-31, same jar | `block/VineShearing` | `[vine_shearing] enabled` |
| Trimmed look for sheared modded vines (Quark's cut vine look) | BUILT 2026-10-31, same jar | `block/VineShearingClient` | `[vine_shearing] trimmedLook` (client) |
| WTHIT lines "Sheared, stops growing" / "Stopped growing" | BUILT 2026-10-31, same jar | `compat/WthitSheared` | `[wthit] sheared` |

TODO row: `potions/enchanting #5`. Related: sheared grass ([wthit_lines.md](wthit_lines.md) 2.10, TODO `misc #6`).

## 1. Overview

Shears already stopped some blocks spreading: Immersive Weathering's soils, the Vintage lush grass and Vinery grass slab
(sheared = `fertile=false`, no random ticks), Quark's Shear Vines (vanilla vine -> `quark:cut_vine`), IW's ivy and
Vinery's jungle grape vine. This feature covers every other vine in the pack, so any vine can be sheared to stop it
growing.

## 2. Reference: every vine in the pack (jar scan 2026-10-31)

Scan: every class in `mods/` whose superclass chain reaches `VineBlock`, `GrowingPlantHeadBlock` (or its vanilla
subclasses) or `MultifaceBlock`. `client_mods/` is empty.

| Vine | Class | Grows by | Before this feature | Now |
|---|---|---|---|---|
| Vanilla vine | `VineBlock` | random tick spread | Quark: shears -> `quark:cut_vine` (never grows), same vine below removed | unchanged (Quark) |
| Floral Enchantment flower vines (16 `*_vine` + wither rose vine) | `VineBaseBlock`, `WitherVineBaseBlock` extends `VineBlock` | vanilla vine spread (no override) | nothing | shears -> `fertile=false` |
| Botania solid vine | `SolidVineBlock` extends `VineBlock` | vanilla vine spread (Botania empties `tick`, not `randomTick`) | nothing | shears -> `fertile=false` |
| Quark cut vine | `CutVineBlock` extends `QuarkVineBlock` | never (its `randomTick` calls the empty `tick`) | - | unchanged |
| Vinery jungle grape vines | `GrapeVineBlock` extends `VineBlock` | ripening + vanilla spread | shears toggle `sterilized` (no random ticks) | unchanged; WTHIT line added |
| Weeping, twisting, cave vines, kelp, Upgrade Aquatic kelps | `GrowingPlantHeadBlock` (tip) | tip age < 25 random-ticks | nothing (1.20 added shears) | shears on the tip -> age 25 |
| IW ivy | `IvyBlock` extends `MultifaceBlock` | age < 10 random-ticks | shears -> age 10 (IW) | unchanged; WTHIT line added |
| Glow lichen, IW frost and soot, leaf piles, azalea flowers, skunk spray | `MultifaceBlock` | no random spread | - | out of scope |

IW's `block_growths` for weeping and twisting vines start new vines under nether wart blocks; they never extend an
existing vine, so a sheared tip stays put.

## 3. What the pack changes

### 3.1 Modded vines (`fertile` state)

- `mixin/VineBlockShearMixin` adds `VineShearing.FERTILE` (`fertile`, the same name and sense as the sheared grass) in
  `VineBlock.createBlockStateDefinition` (`m_7926_`) for every subclass except vanilla's vine, Quark's vines and
  Vinery's grape vine (`VineShearing.addsFertile`, by class name). Default true: it is the property's first value,
  which the vine constructors' `stateDefinition.any()` picks.
- The same mixin cancels `VineBlock.randomTick` (`m_213898_`) when `fertile=false`.
- `VineShearing.onRightClickBlock` (Forge `RightClickBlock`, both sides; any item with the `shears_carve` tool action,
  as Quark checks): sets `fertile=false`, plays Quark's sound (sheep shear, 0.5), costs 1 durability, fires the
  `shear` game event, and removes the same vine hanging straight below (no drops, as Quark) unless `cutBelow` is off.
- New growth from an unsheared vine is unsheared (vanilla spreading builds from `defaultBlockState()`). An unsheared
  vine above may still add faces to a sheared one below it (vanilla spreads into the same block), but the sheared one
  never spreads further, so it caps the column the way Quark's cut vine does.
- No undo in place: break it with shears and place it again (placed vines are unsheared), as with grass.

### 3.2 Growing-plant tips

Shears on a `GrowingPlantHeadBlock` tip that is not at its last age set it there (`getMaxAgeState`), which never
random-ticks. Sound `growing_plant_crop`, 1 durability, `block_change` game event: 1.20's `ShearsItem.useOn`. Body
blocks are left alone (1.20 does the same). A cave vine tip with glow berries is skipped so its berries are picked
as before (1.20 runs the block's own use first). Bone meal still grows a capped tip, and the new tip keeps the last age.
There is no new state and no look change, so a tip that reached age 25 on its own looks and behaves the same.

### 3.3 The trimmed look (texture investigation)

Question asked: can Quark's trimmed vine texture be used by other modded vines?

- Quark's `cut_vine.png` is vanilla's `vine.png` with every pixel in rows 11 to 15 cleared (row 10 partly); no pixel
  is recoloured (pixel diff 2026-10-31). Its 11 models are the old 1.12 vine model set (`_1`, `_1u`, `_2`, ...).
- Floral Enchantment's 17 vine textures are pre-coloured (flowers drawn in) but share vanilla's silhouette in rows 0
  to 9, and its models are the same 11-model set with `tintindex 0`. Botania's solid vine uses `minecraft:block/vine`.
- Using Quark's texture directly would draw a plain green vine (Botania: fine; Floral Enchantment: loses the flowers).
  Making cut copies of the Floral Enchantment textures would work visually, but the mod is All Rights Reserved, so
  the pack would ship derived art.
- Chosen: `block/VineShearingClient` wraps the baked model of every `fertile=false` state and moves the lower vertices
  of each wall quad up by 5/16 of its height, with the UVs following. The vine's own texture shows with its bottom
  5 rows cut, matching Quark's cut vine, for any vine, coloured or tinted, with no texture or blockstate file shipped.
  Ceiling faces stay whole (the cut is where the vine hangs). The blockstate files need no `fertile` key: variant
  keys that do not name a property match both values.
- Rejected: per-mod blockstate overrides with new models (one set per mod, ARR blockstate paths to override in
  `kubejs/assets`); a runtime-generated cut texture pack (more code, same result).

### 3.4 WTHIT

`compat/WthitSheared` now also shows "Sheared, stops growing" on a `VineBlock` with `fertile=false` or Vinery's
`sterilized=true`, and "Stopped growing" on a growing-plant tip at its last age and IW ivy at age 10 (sheared or grown
that far; the two are the same state). Grass keeps "Sheared, prevents spreading and converting to dirt". The WTHIT
config label is now "Sheared Grass and Vines".

## 4. Limits and decisions

- Vanilla vine is left to Quark (its cut vine has its own block and texture); adding `fertile` to it too would double
  its states for no gain. With Quark's Shear Vines off, vanilla vines would have no shearing.
- `cutBelow` mirrors Quark (removes, no drops). Set it false to shear one block at a time like grass.
- The `fertile` state is registered whatever the config says (states are built at registration, before configs load);
  `enabled` only turns the shears interaction off.
- Adventure-mode or claim checks: none, the same as the grass ports and Quark.

## 5. File map

- [VineShearing.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/block/VineShearing.java)
- [VineBlockShearMixin.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/VineBlockShearMixin.java)
- [VineShearingClient.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/block/VineShearingClient.java)
- [WthitSheared.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/compat/WthitSheared.java)
- [CEConfig.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CEConfig.java) `VineShearing`, `Wthit.sheared`

## 6. Offline checks (2026-10-31, all PASSED)

- `./gradlew :creativeCommon:build --offline`; reobf jar `javap`: calls are SRG (`GrowingPlantHeadBlock.m_187440_`
  isMaxAge, `m_187438_` getMaxAgeState, `CaveVines.m_152951_`).
- Headless harness (scratchpad `trimtest`): `VineShearingClient.trim` on a wall quad in all 4 vertex orders (bottom
  vertices to y 5/16, v 31% of the way up, u and x unchanged, top untouched), ceiling quad returned as is, tint kept;
  `addsFertile` false for `VineBlock`, Quark's vine and a subclass of it, Vinery's grape vine, true for a plain
  subclass; `FERTILE`'s first value is true.

## 7. In-game tests

Scratch-server checks: [scratch_queue.md](testing/scratch_queue.md) "Vine shearing" (SQ-VS-01 to 04), plus the
fake-player shears rows SQ-VS-05 to -08 (server halves of VS-01, VS-03, VS-05 to VS-07): all PASSED r1031 (2026-10-31).
The looks, WTHIT lines and VS-09 to VS-11 are still in game only.

- [ ] **VS-01 Flower vine:** shears on a Floral Enchantment poppy vine hanging 4 blocks → sheep-shear sound, 1 durability, the vines below it vanish, the cut block shows its bottom trimmed like Quark's cut vine (flowers kept above the cut), WTHIT "Sheared, stops growing".
- [ ] **VS-02 Stays put:** `/gamerule randomTickSpeed 300` near a sheared and an unsheared flower vine for a minute → the sheared one does not spread; the unsheared one does.
- [ ] **VS-03 Second shear:** shears again on a sheared vine → nothing (no sound, no durability).
- [ ] **VS-04 Botania solid vine:** shear it → trimmed look in vanilla green (tinted), stops spreading.
- [ ] **VS-05 Weeping vine tip:** shears on the bottom tip → crop sound, 1 durability, WTHIT "Stopped growing", no new growth at high tick speed; bone meal still adds blocks.
- [ ] **VS-06 Kelp and twisting vine tips:** same as VS-05 (kelp under water; twisting tip at the top).
- [ ] **VS-07 Cave vine with berries:** shears on a berry tip → berries picked, tip not capped; shears again (no berries) → capped.
- [ ] **VS-08 Vanilla vine:** shears → Quark's cut vine as before (no `fertile` line).
- [ ] **VS-09 Grape vine and ivy:** jungle grape vine sheared → "Sheared, stops growing"; IW ivy sheared → "Stopped growing".
- [ ] **VS-10 Look toggle:** `[vine_shearing] trimmedLook = false`, F3+T → sheared flower vines draw whole.
- [ ] **VS-11 Breaking:** break a sheared flower vine with shears and place it → grows again (unsheared).

## 8. History

- 2026-10-31: built (patcherMod 1.1.04; installed 00:44). Jar scan of every vine class, texture investigation (3.3),
  quad-trim look chosen over copied textures (Floral Enchantment is ARR).
