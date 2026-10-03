# 1.0.4: 1.0.3 deploy fixes, Exposure Extras, crop parity, hoe areas

IN PROGRESS, not deployed (started 2026-10-02, last updated 2026-10-03). 1.0.4 is everything built after the
1.0.3 deploy (2026-10-02 15:55, commit `d791885`, the last push). There was no plan up front. This file
records what went in, what is still open, and the checks to run before deploying. It follows
[plan_1.0.3.md](plan_1.0.3.md): each piece links to its own writeup, and this file keeps only the status line
and the summary.

## 0. At a glance

| ID | Change | Kind | State | Writeup |
|---|---|---|---|---|
| XK | Join kick on the dedicated server ("input is null", login packet) | fix, 1.0.3 bug | BUILT, exposureBackport `-02` in `mods/`; needs a real client joining a dedicated server | [exposure.md](exposure.md), this file 1.1 |
| RF | Client startup crash (polaroid refmap missing from the jar) | fix, 1.0.3 bug | BUILT, `-02` in `mods/`; a throwaway client reached the main menu | [exposure.md](exposure.md), this file 1.2 |
| CB | Camera bags moved from patcherMod to Exposure Extras (`exposure_extras`) | migration | IN PROGRESS: built and in `mods/`, not boot-tested since the move, remap untested | none yet; this file 2 |
| CP | Crop parity layer: tobacco on Rich Farmland / Fertile Soil, Quark replant and hoe harvesting | feature | BUILT, patcherMod 1.0.4 in `mods/`, not boot-tested; CR-01/02/21-25 pending | [crops.md](crops.md) 2.6 (`CR-`) |
| HA | Harvest area per hoe (1x1 to 9x9) + tooltip | feature | BUILT, patcherMod 1.0.4 in `mods/`; scratch boots clean before the bump; CR-26..30 pending | [crops.md](crops.md) 2.3 (`CR-`) |
| CG | Canned golem can be dyed (16 colours) | feature | BUILT, patcherMod 1.0.4 in `mods/`, not tested in game | none yet; this file 4 |
| DD | Display Delight: missing-texture icons in WTHIT and JEI, empty WTHIT icon for item-less plates | fix | BUILT: kubejs models + patcherMod 1.0.4 in `mods/`; R4-08..10 pending | this file 4b |
| NM | New mods: Blue Flame Burning, Fruits Delight, Improved Pillager Outpost, Well | mods | in `mods/`; `mod_sides.md` rows added 2026-10-02 (all `both`), in the payload | this file 5 |
| DR | Data refresh for every mod added since the last dumps: `/dumpall` + `/dumpfood`, all generators, food types, compost, item_dict, BlockRunner tiers, JEI coverage, writeups | data + scripts | BUILT 2026-10-02; scratch server loads it clean; your client needs a restart (new item_dict keys) | this file 5b |
| WG | Worldgen: Improved Pillager Outpost as a 1-in-8 tower (not a replacement), IPO template-name fix, only water structures in oceans/rivers (biome rules), driftwood x0.25 | feature + fix | BUILT 2026-10-03, patcherMod 1.0.4 in `mods/` (md5 ab58494b, since replaced by the WT build); scratch dedicated server applied it; ST-44..48 pending | [structure_tuning.md](structure_tuning.md) 3.12 (`ST-`) |
| CF | Config: Quark tobacco line, Every Compat fruit leaves, JEI sort order, T&T waystones patch zip gone | config | live | this file 5 |
| WT | Wandering Trader + Winemaker: independent spawn timers in ticks (96000 each), 25 + 5 offers, stock rework in KubeJS (`global.trades`), Vinery `wine_trader_chance` 0; catalog | feature + config | BUILT 2026-10-03, patcherMod 1.0.4 in `mods/` (md5 37ce1023) + KubeJS; scratch dedicated server checked it; WT-01..16 pending | [wandering_trader.md](wandering_trader.md) (`WT-`) |
| WC | Wild crops: every wild crop and fruit tree moved to the biomes you specified (`wild_crops.json`, 46 rules over 146 classified biomes), oak/dark oak leaves drop no apples, Alex's Mobs leaf bananas off, Farmer's Respite Nether coffee off; CEConfig `wildCrops` switch | feature + config | BUILT 2026-10-03, offline-tested; patcherMod 1.0.4 rebuilt (md5 09733ca8) but NOT copied to `mods/` (game was running); CR-47..58 pending | [crops.md](crops.md) 2.8 (`CR-`) |
| VR | Version records: in-tree jars bumped, jar archive, 1.0.3 snapshot | bookkeeping | DONE 2026-10-02 | [deploy.md](deploy.md), this file 6 |

**Payload as of now** (`build_deploy.py --version 1.0.4 --dry-run`, 2026-10-02 late): update from 1.0.3, 139
files, 10.7 MB, including the 4 new mod jars. Each side removes the two old in-tree jars. The only preflight
warning left is uncommitted changes.

## 1. Fixes for the 1.0.3 deploy

Both bugs were in exposureBackport `-01`, so 1.0.3 was unplayable on the live server: dedicated-server
joins were kicked, and real clients crashed on startup. Both fixes ship in exposureBackport `1.9.19-ce-19.2-02`,
which has to go to the server and every client together, because the version is pinned in all five
`[[mods]]` entries.

### 1.1 XK: join kick

- **Symptom:** every player was kicked on join: `EncoderException: NullPointerException: Cannot invoke
  JsonElement.getAsJsonPrimitive() because input is null`. The trace is in the **server** log under "Error
  receiving packet 37" (the login packet). The client log shows nothing.
- **Cause:** the backport syncs the `lens` and `filter` datapack registries in the login packet. Their item
  predicate codec wraps vanilla `ItemPredicate.serializeToJson`, which writes `"count": null` and similar.
  JsonOps passes those on as Java nulls, so converting to NBT throws. Single player and scratch-server boots
  never encode that packet, so only a real client joining a dedicated server hits it.
- **Fix:** `BackportCodecs.jsonBacked` strips JSON nulls (`withoutNulls`, recursive), and a whole-value
  null becomes `{}`. A headless test showed the old path throwing and the new path round-tripping through
  NBT bytes.
- **Where:** [BackportCodecs.java](../development/exposureBackport/src/main/java/io/github/mortuusars/exposure/backport/codec/BackportCodecs.java).
- **Rule going forward:** any codec built on 1.19.2 `serializeToJson` output that reaches a synced registry
  or packet must strip nulls.

### 1.2 RF: client startup crash

- **Symptom:** real (non-dev) clients crashed at startup with `NoClassDefFoundError RenderType`. The log
  shows the polaroid `ItemRendererMixin` finding no targets and `exposure_polaroid.refmap.json` unreadable.
- **Cause:** MixinGradle packs each source set's refmap from a temporary folder that Gradle doesn't track.
  The file went missing while the compile task was up to date, so the jar shipped without it. Dev runs use
  Mojang names and servers never load ItemRenderer, which is why nothing caught it.
- **Fix** in [build.gradle](../development/exposureBackport/build.gradle): each compile task declares its
  refmap as an output, so a missing one re-runs the compile. The jar task fails if a mixin config that lists
  mixins has no refmap in the jar. Both checks were proven by deleting the refmap and by skipping the
  polaroid compile. The 1.0.4 jar holds `exposure.refmap.json` and `exposure_polaroid.refmap.json`, and
  `tools/check_jar.py` passes.

## 2. CB: camera bags move to Exposure Extras (in progress)

**Why:** the bags are Exposure content, so they now live with Exposure instead of in patcherMod. The
exposureBackport jar gained a fifth mod: `exposure_extras` ("Exposure Extras"). It is the pack's own code, not
an upstream addon. The base mod's display name is now **Exposure (Backport & Extras)**. New Exposure
content of our own goes in `src/extras` from now on, while the upstream source sets stay a faithful port.

**What moved:**

| Piece | 1.0.3 (shipped) | 1.0.4 |
|---|---|---|
| Item ids | `creativeengineering:{leather,iron,netherite}_camera_bag` | `exposure_extras:{leather,iron,netherite}_camera_bag` |
| Java | patcherMod `camerabag/` | exposureBackport [src/extras](../development/exposureBackport/src/extras/) (`com.SaintAlfonzo.exposure_extras`) |
| Slot counts | `creativeengineering-common.toml` `[camera_bag]` | `exposure_extras-common.toml` `[camera_bag]` (same 5 / 9 / 14, range 1-27) |
| Accepted items | `#creativeengineering:camera_equipment`, all in kubejs | `#exposure_extras:camera_equipment`: defaults in the jar's [camera_equipment.json](../development/exposureBackport/src/extras/resources/data/exposure_extras/tags/items/camera_equipment.json), pack add/remove lists in [equipmenttags.js](../kubejs/server_scripts/tags/equipmenttags.js) |
| Lang, item models | patcherMod assets | `assets/exposure_extras/` (models still point at Spice of Life textures) |
| Recipes | [equipment/camera_bag.js](../kubejs/server_scripts/equipment/camera_bag.js) | unchanged; item_dict keys `i.*_camera_bag` now point at the new ids |

**Existing worlds:** the live server ran 1.0.3, so players may already own `creativeengineering:` bags. A
`MissingMappingsEvent` handler in [ExposureExtras.java](../development/exposureBackport/src/extras/java/com/SaintAlfonzo/exposure_extras/ExposureExtras.java)
remaps the three old ids to the new items. The contents are in the stack's NBT (`CameraBag.Items`) and
come along unchanged. Old `[camera_bag]` values in `creativeengineering-common.toml` are **not** carried over.
Forge drops the obsolete section (the dev instance's `creativeengineering-common-1.toml.bak` is that
correction). Anyone who changed slot counts on the server sets them again in the new file.

**Done:**
- The source move: the six classes, three models, lang and the tag, plus `ExtrasConfig`, the remap and the
  new mods.toml entry with its dependencies on exposure, polaroid and expanded.
- patcherMod no longer registers the bags. The CEConfig `[camera_bag]` section and the patcherMod lang
  keys are gone.
- kubejs: item_dict ids, the tag's add/remove lists and the recipe comments are updated.
- `mod_sides.md`, the exposureBackport README and the JEI mod sort order are updated.
- Both jars build. The `-02` jar contains every extras class, the tag and the models.

**Still open:**
1. Boot test with both 1.0.4 jars (scratch server, then the dev client). Since the move, the items,
   config, tag and recipes have only been checked by the build, not by a boot.
2. Remap test on a world that has old bags (R4-04 below). This is the one check that protects players on
   the live server.
3. The in-game checks that were never run for the bags at all: the GUI, slot locking, smithing keeping the
   contents, the tooltip, overflow spill.
4. The iron (lunchbox) and netherite (golden lunchbox) textures are still placeholders.
5. Flashes stay out of the default tag unless you want them in (the example line in `equipmenttags.js`).
6. A writeup: a Camera bags / Extras section in [exposure.md](exposure.md) with its own tests.

## 3. Crops and hoes

**CP: crop parity layer.** Tobacco now acts like wheat:
- It grows on Rich Farmland and Fertile Soil.
- Farmland under it doesn't dry back to dirt.
- Soaked Fertile Soil bone-meals it.
- Quark right-click replant and hoe area break include it.

The pieces are generic, so the next crop only needs tags and config:
- block tags `creativeengineering:crops/farmland_soils` and `crops/farmland_crops`, in
  [blocktags.js](../kubejs/server_scripts/tags/blocktags.js)
- one Quark "Harvestable Blocks" line, backed up in `configs_edited/`
- patcherMod `crops/CropParity` and four mixins: `FarmBlockUnderCrops`, `MulchBlockCropGrowth`,
  `QuarkHoeHarvest`, plus `TobaccoPlacement` (the only per-crop one)

Details and the checklist for further crops: [crops.md](crops.md) 2.6. Gaps G1-G3 are fixed, G4-G11 are open.

**HA: harvest area per hoe.** Item tags `creativeengineering:hoe_harvest/<N>x<N>` (N = 1 to 9, the largest
tag wins) set the square that Quark's hoe right-click and area break cover:

| Area | Hoes |
|---|---|
| 1x1 | wood, stone |
| 2x2 | copper, gold |
| 3x3 | iron, IE steel, manasteel |
| 4x4 | diamond, netherite, elementium |
| 5x5 | echo, felsic, dragon |

An even size reaches one block further toward where the player looks. A hoe with no tag keeps Quark's
configured radius. The tooltip line ("Harvest area: 3x3") is generated from the tags. The tags are in
[equipmenttags.js](../kubejs/server_scripts/tags/equipmenttags.js); the code is patcherMod `crops/HoeHarvestArea`
and `HoeHarvestTooltip` plus two Quark mixins. To change a size, edit the tags, not Java. Details:
[crops.md](crops.md) 2.3.

**WC: wild crops.** `kubejs/generation_scripts/worldgen/wild_crops.json` classifies every overworld biome by traits
and gives each wild crop a rule ("warm wet", "cold grassy", "sandy beach or cliff"...). `worldgen/wild_crops.js` applies
it at world load: the mods' own biome modifiers become `forge:none` and the pack adds its own per crop. Pack features
cover the cases where a mod's ground test would block the biome (cabbages on cliffs, colonies in dark forests,
hamimelons on terracotta, cocoa trees in swamps). Side changes: oak/dark oak leaves drop no apples
(`loot/08_leaf_apples.js`), `config/alexsmobs.toml` `bananasDropFromLeaves = false` (backed up), no Farmer's Respite
coffee in Basalt Deltas. The CEConfig switch needs the rebuilt patcherMod jar in `mods/` (install with
`./gradlew :patcherMod:installToMods` once the game is closed); without it the scripts default to on. Open: densities
moved a lot (V13), FR coffee has no source now (V18). Details: [crops.md](crops.md) 2.8.

## 4. CG: dyeable canned golem

Right-clicking Canned Goods' canned golem with any dye (including `#forge:dyes/*`) recolours it. It uses one
dye unless you're in creative. The default is red, the mod's own texture.
- The colour is synced entity data, saved as NBT `Color`: patcherMod `mixin/CannedGolemColorMixin`.
- The client texture is swapped by `CannedGolemRendererMixin`.
- The other 15 textures are in `kubejs/assets/creativeengineering/textures/entity/canned_golem/`.

The textures ship with the kubejs payload (the 15 added files). Not tested in game.

## 4b. DD: Display Delight icons

**Symptom:** some placed Display Delight foods showed the purple/black missing-model cube as the WTHIT icon
(and in JEI); others showed no icon at all.

**Cause:** WTHIT's core icon is `Block#getCloneItemStack(level, pos, state)`, the vanilla 3-arg method (DD
only overrides Forge's pick-block one), so it shows the DD block's own item. DD 1.2.0 registers 121 block
items but ships item models for 32 of them. The plated items lost theirs in a rename (the jar has
`models/item/hamburger.json`, not `plated_hamburger.json`), and the newer stews, drinks and compat foods never
had one, so 89 items show the missing model. Another 70 of the 191 blocks have no item at all (most stackable
plates: cookies, pumpkin pie, the `ad_`/`cd_`/`df_` plates), so WTHIT gets an empty stack. 20 of those are
reachable with the food mods in this pack. Separately, the pumpkin soup, cactus soup and pufferfish broth
models texture four faces with the undefined `#missing`.

The ~620 "missing model for variant" / `_2`/`_3` "Unable to load model" log lines are for stack counts above
each block's max, which can't be placed. Every reachable in-world state has a model (checked against each
block's max stacks).

**Fix:**
- `tools/gen_display_delight_models.py` (in `generators.json`, group `client`) writes the 89 item models to
  `kubejs/assets/displaydelight/models/item/`, parented on the block's default-state model. Two plated items
  whose models have no gui transform get the usual plated one. It also writes the 3 soup models with each
  `#missing` face retextured from its element's top face. Item ids come from `/dumpitemsbymod displaydelight`.
  `--check` exits 1 when anything is stale (e.g. after a DD update).
- patcherMod `compat/WthitBlockModelIcon` (a WTHIT plugin in `waila_plugins.json`, required mod
  `displaydelight`): for a `displaydelight` block whose clone stack is empty or has the missing model, the icon
  is the block's own model, facing north, with the real stack count. Priority 1000 runs before WTHIT's core
  icon (1100); it returns null otherwise.

The models ship with the kubejs payload and the plugin ships with the patcherMod jar.

## 5. New mods and config

**NM: new mods (in `mods/`, not in the payload yet):**
- **Blue Flame Burning** `blueflame-1.19.2-0.1.0.2.jar`: entities set on fire by soul fire burn with blue
  flames. Its mods.toml says `BOTH`.
- **Fruits Delight** `fruitsdelight-0.5.11.jar`: Farmer's Delight fruit trees and foods. L2Library 1.9.7
  comes bundled inside it, which is where `config/l2library-client.toml` comes from. It added
  `config/fruitsdelight-common.toml` and 13 fruit leaf types to `everycomp-wood_types.toml`.
- **Improved Pillager Outpost** `ImprovedPillagerOutpost-v5.jar` (`ipo`): data only. Replaces vanilla's pillager
  outpost templates and `features` pool and adds 6 `ipo:` pools; the outpost structure set is unchanged. Since
  2026-10-03 (WG) it is a 1-in-8 pick next to the vanilla watchtower instead of replacing it, and its misnamed
  templates are fixed: [structure_tuning.md](structure_tuning.md) 3.12.
- **Well** `well-1.19.2-1.0.0.jar`: 17 well blocks (plain + 16 dyed) that pump water.
- `mod_sides.md` rows for all four were added 2026-10-02 (`both`); `/dumpservermods` and `build_deploy.py` no
  longer stop. `pack/` regenerated.

## 5b. DR: data refresh after the new mods (2026-10-02)

`/dumpall` ran in your client at 21:54 (16 of 17; `/dumpservermods` failed on the missing rows above). `/dumpfood`
wasn't part of `/dumpall`; it ran on a scratch server and is now in the list. Then every generator was re-run:

| Area | What changed | Writeup |
|---|---|---|
| Food | 856 foods (was 629): 227 new ones typed, 74 moved by hand to match earlier calls; 842 typed, 76 canned sources | [food.md](food.md) (FO-02/06/08 numbers updated), [food_catalog.md](food_catalog.md) |
| Compost | `gen_compost_values.py --merge` (new mode): 103 new-mod compostables at their mod's chance, 17 of which the rebuild was dropping (corn foods, durian parts, coffee berries, rose hips, fruit slices). Wild corn kept with the crops | none (memory only) |
| item_dict | 263 keys from the food and compost tools, plus 173 for every item of Fruits Delight, Farmer's Respite, Corn Delight, More Delight, Untitled Duck Mod and Well. 0 duplicates | |
| BlockRunner | crates, sacks, placed food, hamimelon, jelly/jello blocks tiered; 6355 ids, 0 overlaps | [blockrunner.md](blockrunner.md) (BR-52) |
| Loot | index 383 tables / 659 items (corn, Exposure films/filters); census and container audit; sim `rewrote 184 tables` | [loot.md](loot.md) |
| Structures | no new sets (IPO swaps pools only); climate re-sampled, biome stats and `structures_by_biome.md` regenerated | [structure_tuning.md](structure_tuning.md) |
| Crops | corn, tea, coffee, berry/lemon bushes, pineapple, hamimelon, 13 fruit trees documented; G12-G15 | [crops.md](crops.md) (CR-31..37) |
| Names | real `/dumpnames` catalog; triage redone; only Untitled Duck Mod adds gaps | [names.md](names.md) |
| JEI | Farmer's Respite kettle pouring is the one new hidden crafting recipe | [jei_coverage.md](jei_coverage.md) |
| Stack sizes | regenerated from `/dumpstacks` (2141 items) | [stack_sizes.md](stack_sizes.md) |

Verified on a scratch server with the new scripts: `[food types] 842 foods in 31 types, 76 canned sources`,
`[compost] 1019 compostable (684 from values, 335 from rules)`, `[loot-tuning] 4 rules -> rewrote 184 tables`, no
new `[item_dict] Missing/invalid` lines.

**CF: config changes:**
- `quark-common.toml`: the tobacco "Harvestable Blocks" line (CP).
- `everycomp-wood_types.toml`: the Fruits Delight leaves.
- `jei/ingredient-list-mod-sort-order.ini`: the renamed Exposure entry and the new Exposure Extras entry.
- `rare-ice.properties`: only the timestamp changed.
- `towns_and_towers/resources/t_and_t_waystones_patch_1.19.2.zip` was deleted from the dev instance, so the
  update **removes it on both sides**. Confirm that was intended (Towns and Towers may regenerate it at
  launch) before staging.
- `trashslot-common.toml` stops shipping, as planned: it was a one-off through 1.0.3 (player config class).

## 6. VR: versions and records (done 2026-10-02)

| What | Before | After |
|---|---|---|
| patcherMod `mod_version` | 1.0.3 (the post-deploy work was rebuilt under that name) | **1.0.4** |
| exposureBackport `mod_version` | `1.9.19-ce-19.2-01` (same) | **`1.9.19-ce-19.2-02`** |
| `mods/` | `creativeengineering-1.0.3.jar`, `exposureBackport-...-01.jar` (both post-deploy rebuilds) | `creativeengineering-1.0.4.jar`, `exposureBackport-...-02.jar` (via `installToMods`) |
| `jar_archive/` | 1.0.3 and `-01` = the shipped jars (sha1 matches `history/1.0.3.json`) | unchanged, plus the 1.0.4 and `-02` builds (the next staging refreshes them if the jars change) |
| `jar_archive/displaced/` | none | the two post-deploy rebuilds, renamed `*-postrelease-dev.jar`, so they can't be confused with the shipped 1.0.3 jars |
| `deploy/DEPLOYED.json` | server 1.0.1, client 1.0.2 (the 1.0.3 deploy was never snapshotted) | server and client **1.0.3**, 2026-10-02 15:55 (taken from the commit time) |
| `mod_sides.md` | the old jar names | the new jar names |

Version rule from here: bump patcherMod's patch version and exposureBackport's port build once per pack
release. Rebuilds before a release keep the number.

## 7. Before staging 1.0.4

1. DONE 2026-10-02: `mod_sides.md` rows for Blue Flame Burning, Fruits Delight, Improved Pillager Outpost and
   Well (all `both`), then `tools/gen_pack_manifest.py`.
2. OPEN: confirm the T&T waystones patch zip removal (5).
3. OPEN: a boot test with both 1.0.4 jars and the new mods (scratch server, then the dev client), then the
   CB remap check (R4-04).
4. OPEN: commit (the dry run counts 14 uncommitted files in shipped folders).
5. Then stage: `python3 tools/build_deploy.py --version 1.0.4 --notes "..."`. After copying it out, run
   `--snapshot 1.0.4`.

## 8. Release checks (`R4-`)

- [ ] **R4-01 join:** a real client joins the updated dedicated server → no kick; the server log has no
  "Error receiving packet".
- [ ] **R4-02 client start:** a non-dev client with the payload starts → main menu; latest.log has no "could not
  find any targets" and no "refmap ... could not be read" for `exposure` or `exposure_polaroid`.
- [ ] **R4-03 polaroid render:** hold an instant camera and a developed instant slide → they render (this is what
  the missing refmap broke).
- [ ] **R4-04 old bag remap:** on a copy of a world with a 1.0.3 `creativeengineering:` camera bag holding items →
  after the update it is the same tier of `exposure_extras` bag with the same contents, and the log has no
  missing-registry warning for it.
- [ ] **R4-05 bag basics:** craft a leather bag, put in a camera and film, upgrade to iron and then netherite →
  contents kept; a photograph or another bag won't go in.
- [ ] **R4-06 bag slots:** set `exposure_extras-common.toml` `leatherSlots` to 3 with 5 items inside, reopen → 2
  items move to the inventory (or drop).
- [ ] **R4-07 golem dye:** right-click a canned golem with blue dye → it turns blue; it is still blue after a
  relog; one dye used in survival.
- [ ] **R4-08 DD item icons:** look at a placed Plated Hamburger, Rabbit Stew and a `df_` stew → WTHIT shows the
  3D plate, not the purple/black cube; JEI shows the same for `displaydelight:plated_hamburger`.
- [ ] **R4-09 DD item-less plates:** place 1 and then 3 cookies on a food plate, and a pumpkin pie → WTHIT shows a
  3D icon of the plate with the right number of cookies (it was blank before).
- [ ] **R4-10 DD soups:** a placed pumpkin soup has no purple/black patches inside the bowl.
- Crops and hoes: CR-01, CR-02, CR-21 to CR-37 in [crops.md](crops.md).
- Wild crops (WC): CR-47 to CR-58 in [crops.md](crops.md).
- Worldgen (WG): ST-44 to ST-48 in [structure_tuning.md](structure_tuning.md).
- Wandering Trader (WT): WT-01 to WT-16 (not WT-06) in [wandering_trader.md](wandering_trader.md).
- Data refresh (DR): FO-02, FO-06, FO-08 in [food.md](food.md); BR-52 in [blockrunner.md](blockrunner.md).

## 9. History

- 2026-10-02: file written. Everything after the 1.0.3 deploy (commit `d791885`) is 1.0.4. Bumped patcherMod
  to 1.0.4 and exposureBackport to `-02`, rebuilt both, installed them to `mods/` and set the jar archive
  straight (above). Recorded the 1.0.3 deploy with `--snapshot`. Moved the post-deploy items (CP and the
  camera bag move) out of [plan_1.0.3.md](plan_1.0.3.md); that plan is now marked DEPLOYED.
- 2026-10-02 (late): DR data refresh (5b) after `/dumpall`. `mod_sides.md` rows for the four new mods, so the deploy
  is no longer blocked; `/dumpfood` added to `/dumpall`; every generator re-run and the writeups brought up to date.
- 2026-10-03: WG worldgen changes: IPO is a 1-in-8 tower, IPO template-name fix, a water biome rule, and driftwood
  at x0.25 (see the table). patcherMod was rebuilt at 1.0.4 (a pre-release rebuild, so the number stays).
- 2026-10-03: WT Wandering Trader spawn rate and offer counts (`[wandering_trader]`), patcherMod rebuilt at 1.0.4 and
  installed (md5 37f69263); catalog and gap analysis in [wandering_trader.md](wandering_trader.md).
- 2026-10-03 (later): WT rework: tick-based independent trader/Winemaker spawners, 25 + 5 offers, stock edited in
  KubeJS (`economy/wandering_trades.js`, `global.trades`), Vinery `wine_trader_chance` 0 (configs_edited backed up).
  patcherMod rebuilt at 1.0.4 and installed (md5 37ce1023).
- 2026-10-03: WC wild crop placement built from the user's spec (see the table). patcherMod rebuilt at 1.0.4 for the
  `wildCrops` switch (md5 09733ca8), not installed: a game process was running.
