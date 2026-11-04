# Nether's Delight 4.0 update and My Nether's Delight

**BUILT 2026-11-01, installed in `mods/` with the CD jar (2026-11-02 14:01; `NethersDelight-1.19-3.1.jar` gone from `mods/`, still in
`server_mods/`), not deployed; scratch r1102b 2026-11-02: SQ-NDM-01 to -04, -06, -07 PASSED, -05 open (colony drop), -08 (3.1
save) not run; not played** (section 4.9; deep scan 2026-11-02): Nether's Delight 3.1 repacked as Creative Delight member
`repackedNethers` with the 4.0 carry-overs. Two jars were dropped into
`DecompiledMods/` on 2026-10-25: `nethersdelight-1.20.1-4.0.jar` (the next Nether's Delight, 1.20.1 / Forge 47) and
`MyNethersDelight-1.20.1-1.10.4-backport.1.jar` (a separate, larger mod, mod id `mynethersdelight`). Neither runs on 1.19.2
as is. Plan row: `largecontent/mod-additions #16`, `#17` in [todo/TODO.md](todo/TODO.md).

**Licence:** Nether's Delight is MIT (owner-supplied 2026-10-25; checked 2026-11-01 against the repository's 1.19 branch `LICENSE`, "Copyright (c) 2022 Umpaz", whole repository, no separate asset licence). My Nether's Delight: not stated.

## 1. Decisions (user, 2026-10-25)

- **My Nether's Delight is not ported and not added.** Its overlap with Nether's Delight (fungus colonies, trophies,
  stuffed hoglin, hoglin hide / loin, strider slice, propelplant vs powdery cannon, stoves, soils) is not resolved by
  porting it; the jar stays in `DecompiledMods/` for reference only. No `mod_sides.md` row.
- **Nether's Delight 3.1 stays the installed mod**; the 4.0 changes below are carried over to it by data and code.
- **2026-11-01 (owner): Nether's Delight ships as a member of Creative Delight** (not patcherMod), pointed at the current
  Farmer's Delight systems (the `farmers` member and the pack's FD 1.2.4 + 1.3.4-backport setup). The 3.1 jar stays the
  installed mod (MIT, never repacked); the member is an additions overlay like `enders_additions`: Java (the flower pot
  block, the colony mixin) in a new member `nethersAdditions`, scripts in CD `kubejs_startup/nethersdelight/` (a
  `compost_values.js` already lives there). Section 4.8 records the FD wiring; sections 4.1 to 4.7 still describe the
  changes, with "patcherMod" read as "the CD member".
- **Superseded 2026-11-01 (owner, later the same day): Nether's Delight 3.1 is repacked into Creative Delight** as member
  `repackedNethers` (like End's `repackedEnds`; MIT allows it), and the separate 3.1 jar leaves `mods/`. The 4.0 changes
  go into the repack overlay and CD's `nethersdelight/` scripts. Plan: [creative_split.md](creative_split.md) 7.9 step 2.
- **To do:** add Potted Mimicarnation; apply the tag changes; execute the file moves; apply the code-only changes.
- **2026-10-25:** the owner asked for the execution to be documented, not built. Plan in section 4.
- **Delivery path: settled** by the 2026-11-01 repack decision above (built as `repackedNethers`, 4.9).
- **Renames (default applied 2026-11-01, owner may overturn):** display name only. `hoglin_mount` keeps its id and reads
  "Hoglin Trophy"; `patch_propelplant` keeps its id (not visible). Existing Hoglin Mounts stay in worlds.

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

- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): PASS SQ-NDM-01, -02, -03, -04, -06, -07; OPEN (no verdict, rerun row written) SQ-NDM-05.
- **r1103 2026-11-03** (scratch server; verdicts in `perf/runs/r1103/` FINAL.md and the slice judged.md): PASS SQ-NDM-05.

`ND-` prefix: pot a Mimicarnation (place, break drops pot + flower, cutout render); `meal_item` tag on Stuffed Potato;
JEI shows Stuffed Hoglin and the netherite machete under the new ids; colony on dirt in daylight breaks, under light < 13
stays; colony on rich soul soil grows; Hoglin Mount reads "Hoglin Trophy" and existing ones stay in the world.

### 4.8 Pointing the member at the Farmer's Delight systems (owner, 2026-11-01; plan, nothing built)

Checked against the repo 2026-11-01: the pack's FD 1.2.4 jar contains `MushroomColonyBlock` and `MushroomColonyItem`, so the
4.0 colony needs no shim under the overlay route; the (B) shim problem does not arise.

- **Colony base.** Nether's Fungus Colony keeps extending FD's `MushroomColonyBlock`; the mixin overrides only `canSurvive`
  and the random-tick roll. FD 1.3.4's `mushroom_colony_growable_on` tag is skipped by the pack (FD 1.2.4 reads none of the
  new tags, farmers_delight.md), so 4.0's own `nethersdelight:fungus_colony_growable_on` (rich soul soil) is the one rule.
- **Growth hook.** The colony roll goes through `ForgeHooks.onCropsGrowPre/Post`; Creative Calendar's
  `CropSeasonGrowHookMixin` and the crop seasons then apply. Keep colonies out of the seasonal tags (4.4) and check the
  hook does not pick the colonies up by block class.
- **Recipes by FD route.** `stuffed_hoglin` moves to `farmersdelight:cooking/stuffed_hoglin` (4.3) and is emitted with
  `global.NCH` like the other FD cooking recipes; its pot, Slice and Dice and can/oven handling follow the FD families in
  food_families.md rather than a Nether-only route. `stuffed_potato` into `nethersdelight:meal_item` uses `global.tags`.
- **Rich soil and farmland.** FD's rich soil mixins (no trample, villagers work it) live in the `farmers` member. Nether's
  rich soul soil is its own block and is not covered by them: decide whether villagers or the trample rule should treat it
  as rich soil (open, owner).
- **Item and dict.** Nether's ids go through `global.item` / the food dict like other CD members (no literal
  `nethersdelight:` strings in scripts); the Hoglin Trophy rename is lang only (4.5).
- **Test prefix.** `ND-` rows (4.7), plus one FD row: Stuffed Hoglin craftable in the FD pot and visible in JEI under the
  new id.

Open items for the build session: the member name and gradle wiring (`:creativeDelight:` nests it), the rich soul soil
question above, and the seasonal tag check.

### 4.9 As built (2026-11-01; installed 2026-11-02, scratch tested r1102b)

Route: the 2026-11-01 repack decision (section 1, creative_split.md 7.9 step 2), which supersedes the additions overlay of
4.8; the "patcherMod" of 4.1 to 4.4 is the repack overlay. Ids are 3.1's throughout; renames are display names only.

| Plan step | As built | Files |
|---|---|---|
| Member | `repackedNethers` (`ce.forge-repack`, copy of the `ends` shape), version `1.19-3.1-ce-19.2-01`, display name "Nether's Delight (Creative Delight)", upstream `NethersDelight-1.19-3.1.jar` sha1 `844fcf67...` in `development/creativeDelight/upstream/` (git-ignored). Root `settings.gradle` member map, CD `bundle_members`, CD `replaces_jars` + `NethersDelight-*.jar`, `repackedNethers-*.jar` (installToMods moves the 3.1 jar out of `mods/`) | `development/creativeDelight/members/nethers/` |
| 4.1 Potted Mimicarnation | Block registered by the overlay (`PottedMimicarnation`, a MOD-bus subscriber inside the repack's mod file, so the id is `nethersdelight:potted_mimicarnation` with no prefix warning). Empty pot = vanilla flower pot, plant added with `addPlant` in common setup (4.0 passes no empty pot, so emptying its pot leaves the potted block: not copied). 3.1's blockstate and loot table are used; the model is 4.0's (`render_type cutout`, `replaced_resources`). `minecraft:flower_pots` tag through `global.tags`; name through CD `client.js` | `members/nethers/src/main/java/.../nethers/PottedMimicarnation.java`, `kubejs/data/creativedelight/kubejs/nethersdelight/tags.js`, `kubejs/assets/creativedelight/kubejs/nethersdelight/client.js`, `block_dict.js` (`b.potted_mimicarnation`, `b.t_flower_pots`) |
| 4.2 Tags | `nethersdelight:meal_item` + Stuffed Potato through `global.tags` (`i.t_nd_meal_item`). `mineable/axe` and machete order: nothing (as planned) | `tags.js`, `item_dict.js` |
| 4.3 File moves | Done in the repack, not through `global.NCH`: NCH ids are always `<ns>:<type>/<suffix>`, so it cannot give `farmersdelight:cooking/stuffed_hoglin` or `minecraft:netherite_machete_smithing`. 3.1's two files are left out (`replaced_resources`) and shipped unchanged at 4.0's paths (the machete file keeps 3.1's 1.19.2 smithing format). No pack script named the old ids (grep). The old recipe advancements stay (dropped at load) | `members/nethers/src/main/resources/data/{farmersdelight,minecraft}/recipes/` |
| 4.4 Fungus Colony | Mixin `FungusColonyRulesMixin` (`nethersdelight.ce.mixins.json`, beside upstream's `nethersdelight.mixins.json` in the manifest): growth bound 5 -> 4 (`@ModifyConstant` on the tick's only int 5), and 4.0's `canSurvive` added to the class (growable-on floor: always; else raw light < 13 and `canSustainPlant`). The crop-grow event: Creative Calendar's `CropSeasonGrowHookMixin` already fires it at the head of the colony tick, so the mixin does not fire it again (4.0 passes the roll as the event default; here the roll follows an allowed event). Seasonal check: both colonies are crop season type `nether` in `crop_seasons.json` (no calendar effect): PASS | `members/nethers/src/main/java/.../nethers/mixin/FungusColonyRulesMixin.java` |
| 4.5 Renames | `block.nethersdelight.hoglin_mount` = "Hoglin Trophy" (CD `client.js`, 3-argument `e.add` in the mod's namespace). `fabric.js` `replaceInput` keeps the id, now through `i.hoglin_mount`. `gen_crop_worldgen_table.py` keeps `patch_propelplant` (comment added) | `client.js`, `kubejs/server_scripts/engineering/fabric.js`, `tools/gen_crop_worldgen_table.py` |
| 4.6 Skipped | As planned (refactors; 1.20 syntax). The stove save format stays 3.1's, so stove contents survive | none |
| 4.8 FD systems | Colony base: 3.1's class (extends `BushBlock`, not FD's `MushroomColonyBlock`) is kept; no FD class is needed. Rich soul soil villagers / trample: still open (owner) | none |

Offline checks 2026-11-01: `:creativeDelight:build --offline` passes; the nested `repackedNethers` jar holds 58 upstream
classes, no file at the two old recipe paths, both mixin configs in the manifest; `javap` shows the mixin's `canSurvive` as
`m_7898_` and SRG names on every vanilla call; JSON files parse; `node --check` on the changed scripts.

Install (game closed): `./gradlew :creativeDelight:installToMods --offline` moves `NethersDelight-1.19-3.1.jar` out of
`mods/`; remove it from `server_mods/` as well. Rename `Nether's Delight` to `Nether's Delight (Creative Delight)` in
`config/jei/ingredient-list-mod-sort-order.ini` (and its `configs_edited/root_config/jei/` copy) so JEI keeps the sort
position. Then `/dumpall` and the generators (new block).

Install status (deep scan 2026-11-02): the CD jar in `mods/` nests `repackedNethers-1.19-3.1-ce-19.2-01`; still to do: remove the 3.1
jar from `server_mods/`, the JEI sort-order rename (the ini still says `Nether's Delight`), the `mod_sides.md` row (still names
the 3.1 jar), `/dumpall` and the generators.

Tests: SQ-NDM in testing/scratch_queue.md (the 4.7 rows plus the load check of creative_split.md 7.9 step 2). r1102b
2026-11-02 (`perf/runs/r1102b/s3/judged.md`): SQ-NDM-01, -02, -03, -04 (real PASS), -06, -07 PASS; -05 OPEN (the broken
open-sky colony's drop not proven; hand or probe addition); -08 (3.1 save) not run. The mod-list name is a client check: CQ-NDM-01 (names, potted render, JEI; deep scan 2026-11-02).

