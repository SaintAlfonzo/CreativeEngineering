# 1.0.5: nests mod, More Mooblooms, worldgen systems, season types, shields, tooltip fixes

**NEVER DEPLOYED: folded whole into 1.0.6** ([plan_1.0.6.md](plan_1.0.6.md), staged 2026-10-14). Was STAGED 2026-10-11
(restaged; never copied to the server or clients). 1.0.5 is everything built after the 1.0.4
deploy (2026-10-04 13:54, commit `334c76b`): commits `eebaf63` "New nests, stack size changes", `0142efa` "1.0.5",
`52b2c63` "More mooblooms, passive mob spawning, climate change (formally global warming), structure beard tuning", plus
the working tree on 2026-10-11. A first 1.0.5 was staged on 2026-10-05 (22 files) but never copied out, so it was
rebuilt in place with `--version 1.0.5`; the 2026-10-05 contents are all still in it. There was no plan up front:
this file records what went in, what is still open, and where each piece is written up. It follows
[plan_1.0.4.md](plan_1.0.4.md).

## 0. At a glance

Kind: feature, fix, tuning, data, config, mods, bookkeeping. "Boots" = the staged jars and scripts booted in single
player on 2026-10-11 14:42 (section 2) with no mixin or KubeJS errors; nothing below has been checked on the
dedicated server.

| ID | Change | Kind | State | Writeup |
|---|---|---|---|---|
| IA | Nests in their own mod, **Incubation Additions** (`incubation_additions` 1.0.0, `development/incubationAdditions`): emu, duck and goose nests, the egg-layer mixins, assets, loot tables; old `creativeengineering:` ids remapped on load; patcherMod no longer has nest code | refactor | STAGED (first release of the jar); scratch-server verified 2026-10-05; boots; NE-12 | [nests.md](nests.md) |
| WN | Duck and goose nests (`WaterfowlNests`) for the Untitled Duck mod, in Incubation Additions | feature | STAGED; the 2026-10-05 staging with old patcherMod ids never shipped | [nests.md](nests.md) |
| IG | Incubation Additions per-mod gating (content + built-in pack per bird mod), duck and goose egg crates, duck mod egg sprites, nest recipes + `bird_nests` tag moved from KubeJS into the mod | feature | STAGED; client sprite swap not seen (NE-13) | [nests.md](nests.md) |
| MM | **More Mooblooms** (`more_mooblooms` 1.0.0, `development/moreMooblooms`): own moobloom with 11 data-driven variants, two-stage shearing, flower breeding, pack-controlled spawning by flower biome, azaleablooms on moss in lush caves; Buzzier Bees' moobloom, buttercup and Sunny removed; pack spawn rules, cow modifiers, syringe line, JEI sort, Quark restock ignore | feature (own mod) | STAGED (first release); boots: 11 variants, 37 spawn rules, 10 breeding rules loaded; MB-20 to MB-71, MB-97, MB-98 | [mooblooms.md](mooblooms.md) |
| SS | Stack size policy: `tools/gen_stack_sizes.py` -> `stack_sizes.json` -> `changes/stack_sizes.js`; replaces the egg-only `item_properties.js` (removed on both sides) | feature + tuning | STAGED; boots: `set 1052 stack sizes`, no unknown keys | [food.md](food.md) 3.9; reference [stack_sizes.md](stack_sizes.md) is from before the policy |
| GC | Generated per-type cans (cooked, bread, salad, soup, golden): `can_types.json` -> `items/cans.js`, recipes from `canning.js` | feature | STAGED; boots: `registered 113 generated cans`; recipes not checked | [food.md](food.md) |
| GL | WTHIT growth line for the 62 plants WTHIT skips, `[crop_growth]`, `/dumpcropgrowth` | feature | STAGED in patcherMod 1.0.5; CR-59 to CR-66 | [crops.md](crops.md) 2.9 |
| IW | Wild plants, bushes and trees also generate on Immersive Weathering soils (65 `kubejs/data` overrides, tag `worldgen/grass_like_soils`) | fix | STAGED; scratch-server tested 2026-10-05; new chunks only; CR-67, CR-68 | [crops.md](crops.md) 2.8.1 |
| GB | Ghost blocks on a dedicated server (block-state ids past 2^19 overflowed vanilla's section packet), null-state packet guards, `[debug.block_state_sync]` diagnostics (off) + `tools/diff_block_state_ranges.py` | fix + dev tool | STAGED in patcherMod 1.0.5; not yet seen on a live server | [crops.md](crops.md) history |
| SB | Supplementaries vs Breezy wind vane crash (`SupplementariesBreezyCompatMixin`) | fix | STAGED in patcherMod 1.0.5 | none (backlog #10) |
| SH | Shields: mob axes disable every shield (20-tick cooldown), banners on every tier, burn ticks only from netherite up, thorn ticks only from diamond up, effect ticks unblockable, JEI shows template-only netherite smithing only under Armor Trims' pages (which it now fills) | feature + fix | STAGED in patcherMod 1.0.5; boots (no `InvalidInjectionException` from the 3 new vanilla mixins); SH-14, SH-19, SH-33 to SH-43 | [shields.md](shields.md) |
| PL | Placeable food tooltips ("Placeable", "(while sneaking)", "on plate", "on small plate") on 164 foods; DD and Fruits Delight placement lines removed; `/dumpplaceable` | feature | STAGED; PL-01 to PL-09 | [placeable_food.md](placeable_food.md), [tooltips.md](tooltips.md) |
| TF | Tooltip fixes: Legendary Tooltips underline no longer crosses the item model (`IcebergTitleBreakMixin`); AppleSkin food bars in JEI recipe slots/catalysts and Lightman's Currency trades/storage | fix | STAGED in patcherMod 1.0.5; TT-16 to TT-18 | [tooltips.md](tooltips.md) |
| JC | JEI craftable-first recipe order (JEI 19's sort backported to 11.8.1), `[jei] craftableFirst` | feature | STAGED in patcherMod 1.0.5, on in the shipped config; JEI-01 to JEI-06 | [jei_craftable_first_port.md](jei_craftable_first_port.md) |
| GT | GUI title plaque: Engineers-style container screens grow the title plaque for long titles, overflow logged (`ContainerTitlePlaqueMixin`, `RenderSystemShaderTextureMixin`, `[gui_title]`) | fix | STAGED in patcherMod 1.0.5; boot-verified 2026-10-05 | none (backlog #10) |
| BC | Bubble column immunity: squid, dolphins, turtles, Alex's Mobs whales/sharks/seals etc. are not pushed by bubble columns (`BubbleColumnImmunityMixin`, entity tag `creativeengineering:bubble_column_immune` in `entitytags.js`) | tweak | STAGED | none (backlog #19) |
| EX | Exposure backport `-03`: album note/title text boxes take keys again | fix | STAGED (`-02` removed on both sides); EX-13 | [exposure.md](exposure.md) |
| WF | Wild flowers: Botania palettes per biome, pack flower patches for bare biomes, existing Terralith features rescaled/removed, Flower Patch blocks mixed in (`wild_flowers.json`, snapshot `flower_features.json`) | feature + worldgen | STAGED; boots: `99 Botania palette features ... 378 files`; new chunks only; FL-01 to FL-14 | [flowers.md](flowers.md) |
| BN | Bee nests: per-biome nest chance on generated trees, scale, extra bee-nest trees (`bee_nests.json`, starter rules), `/dumpbeenests` | feature + worldgen | STAGED; boots: `6 rules, 44 biomes tuned`; new chunks only; BN-01 to BN-10 | [bee_nests.md](bee_nests.md) |
| MS | Mob spawns: `/dumpspawns` + generated tables, `mob_spawns.json` tuning (12 rules, section 9 first block), spawn conditions (`flying_birds`, `chicken_shores`, `waterfowl_ponds`), spawn ground for animals (walk-target mixins), Quark Shiba off | feature + tuning | STAGED; boots: `12 rules, 94 biomes changed, 286 changes`; applies in loaded chunks too; MS-01 to MS-12 | [mob_spawns.md](mob_spawns.md) |
| SE | Season types for Serene Seasons ("climate change"): arid, frozen, dry, tropical, temperate, adaptive rivers/beaches; SeasonHUD names and day of year, tint, fertility; Pretty Rain follows the types (`PrettyRainSeasonMixin`) | feature | STAGED in patcherMod 1.0.5; server side scratch-checked; client side not run; WE-01 to WE-25 | [weather.md](weather.md) |
| WA | Water fill: cancel a water structure whose water would replace too much dry ground or air | worldgen | STAGED; defaults not calibrated; ST-56 to ST-60 | [structure_tuning.md](structure_tuning.md) |
| TA | Terrain adaptation by tag (none / bury / beard_thin / beard_box / soft), soft beard over the built footprint, template trim of pads and air notches ("structure beard tuning") | worldgen | STAGED; ST-61 to ST-68 | [structure_tuning.md](structure_tuning.md) 3.15, [terrain_adaptation_scan.md](terrain_adaptation_scan.md) |
| MT | Meta structures (15): villages with towers, ruin clusters, sunken ruins, wrecks, rafts, pillager towers/forts/camps with siege weapons, a pillager fleet, a driftwood flotilla; slot conversion keeps every rate; `/locate` finds parts inside metas | worldgen | STAGED; boots (`MetaStructure`, `MetaSlots` loaded); scratch-server verified; ST-69 to ST-76, ST-84 to ST-92 | [structure_tuning.md](structure_tuning.md) 3.16 |
| BF | Biome fits: towers in every biome their wood belongs to, Terralith included (`biome_fits.json`, `tower/*` tags, `--fit-rates`) | worldgen | STAGED; scratch-server: 12 + 5 towers located, all in fitted biomes; ST-77 to ST-83 | [structure_tuning.md](structure_tuning.md) 3.17 |
| WG | World gen data refresh: structure config/groups/tags, pool config, biome config, wild crops, climate samples, reports | data | STAGED; `[structure-tuning] 10 groups, 151 structure_sets written` in single player | [structure_tuning.md](structure_tuning.md) |
| NM | New mods: Breedable Killer Rabbit (server), Cool Rain (both: strict network channel), MEED (both: JEED effect-provider data), Pretty Rain (client); JEED moved client -> both | mods | STAGED; mod_sides rows added 2026-10-11 (section 3) | this file 3 |
| RP | Resource pack `SquirmingSilverfish [1.13 - 26.3]   (v2).zip` | client | STAGED (clients must enable it themselves, `options.txt` is not shipped) | none |
| DR | Data: item/block dict keys for nests, cans and mooblooms; compost values; `blocktags.js` (terrain adaptation, earth/water fill, ground tags); `entitytags.js`; `misc_mods.js` (emu nest recipe moved to the mod); sample syringe takes mooblooms | data | STAGED | this file 1 |
| CF | Config: `creativeengineering-common.toml` (new `[debug.block_state_sync]`, `[worldgen.water_fill]`, `[worldgen.terrain_adaptation]`, `[worldgen.meta_structures]`, `[jei]`, `[crop_growth]`, `[gui_title]`, `[seasons]`; every `[worldgen.systems]` switch on); `more_mooblooms-common.toml`, `prettyrain.json5`, `breedablekillerrabbit.json5` (new); Quark (Shiba off, Quark buttons on CFB fridge/counter/basket, IE crates, Ender Storage, framed storage, cloud chest; restock ignores the moobloom egg); Create Central Kitchen (Miner's Delight copper pot boosts cooking); WorldEdit wand = Vintage Improvements netherite rod; JEI sort orders; `rare-ice.properties` (rewritten by the game, timestamp only) | config | STAGED | this file 1 |
| VR | Version records: patcherMod 1.0.5 (unchanged number, rebuilt 2026-10-11), exposureBackport `-03`, incubationAdditions and moreMooblooms 1.0.0 (first release), jar archive, `pack/manifest.json` regenerated, 4 mod_sides rows + JEED side, 9 `KUBEJS_CLASSES` rules; then patcherMod `mod_version` 1.0.6 for the next build | bookkeeping | DONE 2026-10-11 | [deploy.md](deploy.md), this file 4 |

## 1. What the payload contains

Update from 1.0.4: 146 files, 13.7 MB (mods 8, client_mods 1, kubejs 126, config 10, resourcepacks 1). The full list is
the 1.0.5 entry in [deploy/DEPLOY_LOG.md](../deploy/DEPLOY_LOG.md).

- **mods (server + client):** `creativeengineering-1.0.5.jar` (md5 68509043, replaces 1.0.4),
  `exposureBackport-1.9.19-ce-19.2-03.jar` (md5 2e546a5e, replaces `-02`), new `incubationAdditions-1.0.0.jar`
  (md5 269ff081), new `moreMooblooms-1.0.0.jar` (md5 9ced3342), new `breedablekillerrabbit-1.19.2-3.5.jar`,
  `coolrain-1.19.2-Forge-1.0.2.jar`, `MEED-1.19.2-7.1.1.jar`, and `jeed-1.19.2-2.1.4.jar` (already on clients; new on
  the server).
- **client_mods:** `Pretty Rain-1.19.2-Forge-1.1.1.jar`.
- **kubejs:** 104 added (mostly worldgen data: wild flower, crop, soil and meta-structure features, biome modifiers;
  the new runtime json files; `placeable_tooltip.js`, `stack_sizes.js`, `terrain_adaptation.js`, `water_fill.js`,
  `wild_flowers.js`, `bee_nests.js`, `mob_spawns.js`), 22 changed, 1 removed (`startup_scripts/changes/item_properties.js`).
- **config:** 3 added, 7 changed (section 0, CF). `DistantHorizons.toml` is not shipped (player config since 1.0.4).
- **removals, both sides:** `mods/creativeengineering-1.0.4.jar`, `mods/exposureBackport-1.9.19-ce-19.2-02.jar`,
  `kubejs/startup_scripts/changes/item_properties.js`.

## 2. Checks and open items

**Boot check (2026-10-11):** the client launched at 14:42:10 with exactly the staged jars in `mods/` (patcherMod
installed 14:42:02; the in-tree jars equal their builds and no source is newer) and a world ran 14:47 to 15:40.
`logs/latest.log`: no `InvalidInjectionException` or mixin apply errors. `logs/kubejs/{startup,server,client}.log`: 0
error lines; every worldgen system logged its apply line (structure tuning, wild flowers, wild crops, bee nests, mob
spawns). Only game-rewritten configs changed after that boot.

Open:
- [x] ~~Copy out and `--snapshot 1.0.5`~~: superseded 2026-10-14, 1.0.5 ships inside 1.0.6 instead.
- [ ] Optional before copying: a scratch dedicated-server boot from `deploy/latest/` (nothing in 1.0.5 has run on a
  dedicated server; JEED, MEED and Cool Rain are new there).
- [ ] Worldgen systems (WF, BN, WA, TA, MT, BF, IW, WG) change only chunks generated after the update on the server's
  existing world. Mob spawns (MS) and season types (SE) apply everywhere.
- [ ] **Cool Rain vs season types (WE-25):** Cool Rain plays rain sounds from its own client mixin and reads the
  biome's precipitation directly, so it may play rain over arid rivers. Untested; a `PrettyRainSeasonMixin`-style
  fix would go in patcherMod 1.0.6.
- [ ] Wild crops warning in single player: 5 Terralith cave biomes (`cave/crystal_caves`, `cave/desert_caves`, ...)
  are not in `wild_crops.json` and get no wild crops. Add them (or mark them deliberate).
- [ ] [stack_sizes.md](stack_sizes.md) is from the 2026-10-02 `/dumpstacks`, before the stack size policy: rerun
  `/dumpstacks` + `python3 tools/stack_sizes.py`.
- [ ] `kubejs/assets/*bloom.png` (11 files, top level) are byte-identical copies of More Mooblooms' jar textures at
  a path nothing reads; they ship as client files. Delete or move them out of `kubejs/` if they are only source art.
- [ ] Incubation Additions license note (nests.md section 4): the hay-nest texture copies conflict with the
  Abnormals License; repaint or reference before sharing the jar outside the pack.
- [ ] Tests listed per row in section 0; the 1.0.4 R4- checks and earlier CR-/ST-/WT-/VN- tests are still unrun.
- [ ] Commit: 25 shipped files are uncommitted (plus writeups, tools and dev sources).

## 3. New mods and sides

Added to [mod_sides.md](../mod_sides.md) on 2026-10-11 (each read from the jar):

| Mod | Side | Why |
|---|---|---|
| Breedable Killer Rabbit (`breedablekillerrabbit`) | server | event logic only (taming + breeding killer rabbits), no blocks, items or network; needs Collective (already both) |
| Cool Rain (`coolrain`) | both | rain sounds are client-side, but it registers `coolrain:main` with version "1" required both ways and ships block tags as data: like w2w2, a client that has it can't join a server that doesn't |
| MEED (`meed`) | both | data only: `jeed:effect_provider` recipes for other mods' effects. Recipes reach players from the server |
| Pretty Rain (`particlerain`) | client | particles and sounds only, no channel; patcherMod's `PrettyRainSeasonMixin` is in the mixin config's client list |
| JEED (`jeed`), was client | both | registers its recipe types and serializers on both sides (only the JEI plugin is client-gated). On a dedicated server without it, its own and MEED's effect providers never reach players. JEI is already on the server |

## 4. Version records

- patcherMod stays **1.0.5** for this release (the 2026-10-05 staging was never distributed, so the number was not
  burned). The shipped jar (md5 68509043, 2026-10-11 14:42 build) is in `development/patcherMod/jar_archive/`.
- exposureBackport `1.9.19-ce-19.2-03`, incubationAdditions 1.0.0, moreMooblooms 1.0.0: as built; the last two are new
  in `IN_TREE_MODS` and archived on staging.
- After staging: patcherMod `mod_version` = **1.0.6** in `development/patcherMod/gradle.properties` (next build; not built).
  When the 1.0.6 jar is installed: `installToMods` moves 1.0.5 to `jar_archive/displaced/`, then update the
  `mod_sides.md` Jar column, `instance.json`, `.claude/settings*.json` jar entries, the `item_physics.js` header,
  README.md, and run `tools/gen_pack_manifest.py`. The other in-tree mods bump when they next change.
