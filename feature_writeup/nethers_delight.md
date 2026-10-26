# Nether's Delight 4.0 update and My Nether's Delight

**Nothing integrated.** Installed: `NethersDelight-1.19-3.1.jar` (`mods/`, `server_mods/`). Two jars were dropped into
`DecompiledMods/` on 2026-10-25: `nethersdelight-1.20.1-4.0.jar` (the next Nether's Delight, 1.20.1 / Forge 47) and
`MyNethersDelight-1.20.1-1.10.4-backport.1.jar` (a separate, larger mod, mod id `mynethersdelight`). Neither runs on 1.19.2
as is. Plan row: `largecontent/mod-additions #16`, `#17` in [todo/TODO.md](todo/TODO.md).

**Licence:** Nether's Delight is MIT (owner-supplied 2026-10-25). My Nether's Delight: not stated.

## 1. Decisions (user, 2026-10-25)

- **My Nether's Delight is not ported and not added.** Its overlap with Nether's Delight (fungus colonies, trophies,
  stuffed hoglin, hoglin hide / loin, strider slice, propelplant vs powdery cannon, stoves, soils) is not resolved by
  porting it; the jar stays in `DecompiledMods/` for reference only. No `mod_sides.md` row.
- **Nether's Delight 3.1 stays the installed mod**; the 4.0 changes below are carried over to it by data and code.
- **To do:** add Potted Mimicarnation; apply the tag changes; execute the file moves; apply the code-only changes.
- **2026-10-25:** the owner asked for the execution to be documented, not built. Plan in section 4; both questions in it
  (delivery path, renames) are still open.

## 2. What 4.0 changed against 3.1

| Kind | Change |
|---|---|
| New block | `potted_mimicarnation` (block + `minecraft:flower_pots` tag entry; model gains `render_type cutout`). Not yet added |
| Rename | `hoglin_mount` -> `hoglin_trophy` (block, item, blockstate, models, texture, recipe, loot, `mineable/axe` tag). Pack reference: `kubejs/server_scripts/engineering/fabric.js` (`replaceInput`) |
| Rename | worldgen feature `patch_propelplant` -> `propelplant_patch` (now data: configured feature, placed feature, biome modifier; count 8, crimson forest, vegetal decoration). Pack reference: `tools/gen_crop_worldgen_table.py` |
| Tag | `nethersdelight:meal_item` gains `farmersdelight:stuffed_potato` |
| Tag | `minecraft:flower_pots` gains `nethersdelight:potted_mimicarnation` |
| Tag | `minecraft:mineable/axe` lists `hoglin_trophy` instead of `hoglin_mount` |
| Tag | `nethersdelight:tools/machetes` order only (iron, golden, diamond, netherite) |
| Move | `stuffed_hoglin` cooking recipe: `nethersdelight/recipes/cooking/` -> `farmersdelight/recipes/cooking/` |
| Move | `netherite_machete_smithing` recipe: `nethersdelight/` -> `minecraft/` |
| Move | recipe advancements regrouped into category folders (the pack drops all advancements, so no effect) |
| Behaviour | Fungus Colony (section 4.4): growth chance 1 in 5 -> 1 in 4 and runs through the Forge crop-growth hook; off rich soul soil a colony needs light below 13 to stay. Missed by the first survey, which filed it as code only |
| Code only | Blackstone Stove now extends new `AbstractStoveBlock` / `AbstractStoveBlockEntity`; registry split into `NDFeatures`, `NDConfiguredFeatures`, `NDPlacements`, `NDBiomeModifiers`; `NDRecipes` inner classes; creative tab class `NDCreativeTab`; shared `PropelplantBlock`; Fungus Colony own survive check (behaviour, see the row above); stove inventory saved as vanilla `Items` instead of Forge `Inventory` (matters only to a jar swap) |
| 1.20 syntax | loot modifier condition `minecraft:alternative` -> `alternatives` (not applicable to 1.19.2) |

No food value, item, loot-modifier or vanilla loot-table change; `minecraft:entities/hoglin` is identical to 3.1.

## 3. My Nether's Delight (reference only)

About 120 blocks and items (Blazier, ghast foods, eggs, bread and toast, sausages, hotdogs, magma cake, stone cabinets,
powdery cannon wood set, 2 effects, Curse of Poaching, piglin and bastion loot, config screen). Needs Farmer's Delight
1.3.2; Miner's Delight, Brewin' and Chewin' and Farmer's Respite optional. Its tags list Nether's Delight items as optional,
so it is meant to run beside it. If both ran: hoglin and strider drops would double up (Nether's Delight replaces the vanilla
tables, MND adds its own through loot modifiers), two hoglin hides, two strider slices, two explosive cane patches in
crimson forests. Not pursued.

## 4. Execution plan (documented 2026-10-25, not built)

Owner, 2026-10-25: document only, build nothing. Two questions decide the build and are open:

- **Delivery path.** (A) An overlay on 3.1 (patcherMod plus KubeJS, recommended): small, one patcherMod rebuild. (B) A
  backport jar of 4.0 (MIT, so allowed) replacing 3.1, like Backported Vinery: every change comes native, but 4.0's colony
  extends Farmer's Delight 1.3's `MushroomColonyBlock` (the pack runs FD 1.2.4), so that class needs a copy or shim, and
  the stove's save format change loses the items in existing stoves (rule: no conversions).
- **Renames.** Recommended: keep the 3.1 ids and change only the display name. The alternative, the 4.0 ids with no
  conversion, makes existing Hoglin Mounts in worlds vanish.

The steps below assume (A) and display-name-only renames.

### 4.1 Potted Mimicarnation

3.1 already ships `blockstates/potted_mimicarnation.json`, `models/block/potted_mimicarnation.json` and
`loot_tables/blocks/potted_mimicarnation.json`, but registers no block. Steps:

1. patcherMod: a `DeferredRegister<Block>` under namespace `nethersdelight` registers `potted_mimicarnation` as
   `new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, NDBlocks.MIMICARNATION, copy of Blocks.POTTED_POPPY)`.
   Forge logs one "alternative prefix" warning for it. In common setup, call
   `((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(mimicarnation id, potted)` inside `enqueueWork`.
   Guard it with `ModList.isLoaded("nethersdelight")`.
2. Asset override: the 4.0 block model (`"render_type": "minecraft:cutout"`), placed in a pack resource location that
   wins over the jar (memory `resource-pack-lang-priority`).
3. Tag through `global.tags`: `minecraft:flower_pots` + `nethersdelight:potted_mimicarnation`.
4. Lang: 3.1's `en_us.json` has no key for it; add `block.nethersdelight.potted_mimicarnation` = "Potted Mimicarnation"
   in `namechanges.js`.
5. `/dumpall` and the generators afterwards (new block; memory `dumpall-refresh-workflow`). No `mod_sides.md` change.

### 4.2 Tags (through `global.tags`)

- `nethersdelight:meal_item` + `farmersdelight:stuffed_potato`.
- `minecraft:mineable/axe` `hoglin_trophy`: nothing to do while the id stays `hoglin_mount` (3.1 already tags it).
- `nethersdelight:tools/machetes` order: nothing to do.

### 4.3 File moves (recipe ids)

No player effect; do them only for parity with 4.0. In `global.NCH`: remove `nethersdelight:cooking/stuffed_hoglin` and
re-add the same recipe as `farmersdelight:cooking/stuffed_hoglin`; the same for `nethersdelight:netherite_machete_smithing`
-> `minecraft:netherite_machete_smithing`. Check first that nothing in `kubejs/` or the Creative Delight members refers to
the old ids (grep). The advancement regroup is moot (the pack drops advancements).

### 4.4 Fungus Colony (behaviour, mixin on `umpaz.nethersdelight.common.block.FungusColonyBlock`)

| | 3.1 | 4.0 |
|---|---|---|
| Placeable on | nylium, mycelium, soul soil, rich soul soil, `minecraft:dirt`, farmland | same |
| Stays (`canSurvive`) | whenever the block below is placeable-on | floor in `nethersdelight:fungus_colony_growable_on` (rich soul soil): always; else raw light at the block < 13 and `canSustainPlant` |
| Growth per random tick (on rich soul soil) | 1 in 5 | 1 in 4, through `ForgeHooks.onCropsGrowPre` / `Post` |

Mixin: inject `canSurvive` (return the 4.0 rule) and overwrite the random-tick roll. Through the crop hook, Serene Seasons
and growth accelerators can now act on colonies: check that colonies are not in a seasonal crop tag before shipping.
Player-visible, so it gets a changelog bullet when built.

### 4.5 Renames

- `hoglin_mount` -> `hoglin_trophy`: lang only, `block.nethersdelight.hoglin_mount` = "Hoglin Trophy" in
  `namechanges.js`. `fabric.js` line 89 (`replaceInput` on `nethersdelight:hoglin_mount`) stays as is.
- `patch_propelplant` -> `propelplant_patch`: nothing (the feature id is not visible; 4.0 keeps count 8 in crimson forest).
  `tools/gen_crop_worldgen_table.py` keeps the 3.1 id.

### 4.6 Skipped under (A)

Stove base classes, feature registry split, `NDRecipes` inner classes, `NDCreativeTab`, shared `PropelplantBlock`: refactors
with no behaviour change. 1.20 loot condition syntax: not applicable to 1.19.2.

### 4.7 Tests (to write when built)

`ND-` prefix: pot a Mimicarnation (place, break drops pot + flower, cutout render); `meal_item` tag on Stuffed Potato;
JEI shows Stuffed Hoglin and the netherite machete under the new ids; colony on dirt in daylight breaks, under light < 13
stays; colony on rich soul soil grows; Hoglin Mount reads "Hoglin Trophy" and existing ones stay in the world.
