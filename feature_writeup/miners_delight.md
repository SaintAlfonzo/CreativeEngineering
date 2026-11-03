# Miner's Delight: merged backport plan

> **Merge note (2026-10-25):** this port is to be merged with the other Farmer's Delight add-on backports into a single jar, **Creative Delight**. The inventory summary, cross-port interactions and the merge process are in [creative_delight.md](creative_delight.md). This file stays the detailed record for Miner's Delight; its registry namespace and test prefix do not change.

Plan written 2026-10-24, revised the same day with the owner's answers. **The port is BUILT 2026-10-24**
(`development/creativeDelight/members/miners`, jar `backportedMiners-1.4.5-ce-19.2-01.jar`, offline checks pass, §7) **installed in `mods/`
and shipped in 1.1.03** (deployed to server and players 2026-10-26; boot check SQ-MDP-01 PASSED 2026-10-25). The cave carrot worldgen is built too (§4.3). Goal (owner): one Forge 1.19.2 jar that holds all content of the old Miner's Delight (1.1.2,
live on the server since 1.1.02) and the new one (1.4.5). Some content will be dropped later, but only after it has
been seen in game: the first build ships everything, and the drops are decided in a review test (§3.3, `MND-40`).
Test prefix `MND-`.

**Licence (owner, 2026-10-24): all rights reserved.** So the port is done **like the Vinery port**
([alcohol.md](alcohol.md) Part B, memory `backported-vinery`): the pack never redistributes upstream art, data or code.
The unmodified upstream jar travels as a resource donor in its own folder, the port reads art and data out of it at
load time (sha1-pinned), and everything the port ships is our own GPLv3 code. See D2, D12 and D13.

**Scope rule:** the port adds no recipe or mechanic that is in neither version. Where both versions have the same id,
1.4.5 wins (art, recipe, food values, behaviour), the same rule as the Brewin' port. Content only in 1.1.2 is restored.

**Overlap marker:** every item or system that duplicates something the pack already has carries the text
`*overlaps w/ <what>*` (§3). The overlaps all ship; which side stays is decided after play.

## 0. At a glance

| Part | Where | Status |
|---|---|---|
| Source builds and why the base is the Forge 1.20.1 build | §0.1 | reference, checked 2026-10-24 |
| Decisions | §1 | D2, D3, D6, D9, D11 settled by the owner 2026-10-24; D12 settled by the Modrinth find (P1); D1, D4, D5, D10, D13 confirmed 2026-11-02; D7 and D8 changed 2026-11-02 (below); D3's cup and D9 closed by the cup removal |
| Merged inventory: 69 items, 10 blocks, 4 potions | §2 | reference |
| Overlap list | §3 | analysis; every overlap ships, review after play |
| Port work, pack integration, cave carrot worldgen | §4 | port BUILT 2026-10-24, shipped in 1.1.03; port `-02` BUILT 2026-11-01 (Copper Pot leftover slot I8, four textures in the 1.1.2 look §5, split confirmed by the owner, verify in game), installed 2026-11-01 16:38; I2, I4, I5, I7, I9 done; I3 partly (cans built 2026-11-01; names audit waits for a client dump); I1, I6 open; cave carrot worldgen BUILT 2026-10-24 (§4.3). **Deep scan 2026-11-02:** port `-05` is a nested Creative Delight member (`members/miners`), in the installed `mods/creativedelight-1.0.0.jar` (2026-11-02 14:01); the 21:10 build (copper cup family removed) reuses the `-05` name and is not installed; `server_mods/` still `-01` |
| Tests | §8 | MND-01 to MND-41 drafted, none ticked; queued in scratch_queue.md. Deep scan 2026-11-02: r1102 (nested `-02` / `-03`) SQ-MDP-01, -03, -04a/b, -05, -08a/b/d to h, -09, -11, -12, -15, -17 PASS, SQ-MD-01 PASS (4412 wild cave carrots, max Y 45: MND-32 server half), FAIL SQ-MDP-08c, -13, -14, -16 (fixed 11-02, TODO `creativedelight/members #38` to `#41`; reruns queued in "SQ-CDJ5 fix rerun", not in the r1102b scope), r1102b SQ-MDP-18 PASS; CQ-MND client rows open |

### 0.1 The four builds on disk

| Build | File | Loader / MC | Mod id | Licence (as declared) | Content |
|---|---|---|---|---|---|
| Old, installed, **live** | `mods/miners_delight-1.19.2-1.1.2.jar` (in the 1.1.02 payload) | Forge 1.19.2 | `miners_delight` | All rights reserved (`mods.toml`) | 37 items, 7 blocks, 54 recipe files, 10 loot modifiers |
| Forge 1.20.1, 1.2.3 | `~/.local/share/atlauncher/downloads/miners_delight-1.20.1-1.2.3.jar` | Forge 1.20.1 | `miners_delight` | All rights reserved | 57 items (a middle step; not needed) |
| **Forge 1.20.1, 1.4.5** | `miners_upstream/miners_delight-1.20.1-1.4.5-backport.jar` (the donor; official Modrinth release `1.20.1-1.4.5-backport`, sha1 `638438679800711939e7a8029ae3d5340f8c17bd`, manifest vendor Sammy Semicolon, built 2026-05-27) | Forge 1.20.1 | `miners_delight` | All rights reserved (same `mods.toml` text as 1.1.2, likely a stale template) | 69 items, 10 blocks, 113 recipe files, 21 loot modifiers, 4 potions |
| New, NeoForge | `DecompiledMods/minersdelight-1.21.1-1.4.5.jar` | NeoForge 1.21.1 (needs Lodestone) | `minersdelight` | `neoforge.mods.toml` says MIT; **owner rules it all rights reserved**, treated as such | 69 items, 10 blocks, 116 recipe files |

Checked facts:

- The Forge 1.20.1 1.4.5 build has **every item and every recipe name** of the NeoForge 1.21.1 build (none missing
  either way), with the 1.1.2 mod id. Its 97 shared textures are byte-identical to the 1.21.1 jar's.
- Against 1.1.2 it lacks only `hot_cocoa_cup` (item, `HotCocoaCupItem`, its cup conversion row). Every 1.1.2 recipe
  name exists in 1.4.5. 13 of 1.1.2's textures are redrawn in 1.4.5 (cave carrot stages, wild cave carrots,
  gossypium, weird caviar, bowl of stuffed squid, tentacles, baked tentacles, cave carrot, baked cave carrot,
  sticky basket side).
- Shared food values are unchanged between 1.1.2 and 1.4.5 (the diff only adds entries).
- It needs no Lodestone (it bundles a repackaged Registrate under `com.sammy.minersdelight.repack`).
- Ten cooking recipes sit under a stray `data/minersdelight/` namespace (`takoyaki`, `bat_soup`, `insect_stew`, ...);
  their results are `miners_delight:` ids, so they load as they are. The port moves them under `miners_delight/`.
- Own code is about 4,950 lines (110 classes decompiled with `tools/decompile_mod.py`, Registrate excluded); about
  820 of those are datagen (`data/` package), which the port drops because the jar already holds the generated JSON.
- Recipe JSON is in the FD 1.19.2 format already (`cookingtime`, `recipe_book_tab`, `container`; cutting `tool` tag).
- **1.1.2 is live.** The owner says 1.1.02 (with the 1.1.2 jar) went to players when the snapshot was made. (The repo
  still reads `deploy/DEPLOYED.json` 1.1.01 and has no frozen `changelog/1.1.02.md`; the snapshot record is
  missing here, see §9.) Worlds can hold 1.1.2 items, so every 1.1.2 id except `hot_cocoa_cup` must keep working.

So the behaviour reference is the Forge 1.20.1 1.4.5 build (closest API, same mod id, full 1.4.5 content), cross-checked
against the 1.21.1 jar. Because of the licence (D2) neither is a code or asset source for the port, only for reading.

## 1. Decisions (proposed 2026-10-24; all confirmed or changed by the owner 2026-11-02)

| # | Proposal | Why |
|---|---|---|
| D1 | **Mod id `miners_delight`**, Java package `com.sammy.minersdelight` kept, class names `CopperPotBlockEntity` and `StickyBasketBlockEntity` kept | Create Central Kitchen 1.3.11.c has three mixins into it: `CopperPotBlockEntityMixin` (inject at the `setRecipeUsed(Recipe)` call inside `processCooking`), `StickyBasketBlockEntityMixin` (HEAD of `setTransferCooldown`) and an invoker for `isOnTransferCooldown`. Same trap as the Brewin' keg: keep those methods' shape. The 1.1.2 id also keeps any item already in a test world |
| D2 | **Licence: all rights reserved (owner, SETTLED).** Vinery method: the port's jar holds only our own GPLv3 code and no upstream asset or data file; the upstream jar is a donor read at load time (D12). Hot cocoa cup falls out of this (D3) |
| D3 | **Union of ids, minus the hot cocoa cup (owner, SETTLED):** the 69 1.4.5 items, 10 blocks, 4 potions. Same id in both versions: 1.4.5 wins. `hot_cocoa_cup` (1.1.2 only) is **dropped**: it is not in 1.4.5, so the donor has no art for it. Stacks already in a world vanish when the registry entry goes (a Forge missing-entry warning, no conversion: the pack has no cross-id conversions) |
| D4 | **Nothing is dropped before play.** Every overlap ships marked (§3); drops are decided in `MND-40` and done as a separate change | Owner: "some things will be dropped, but they need to be tested in game first" |
| D5 | Plain `DeferredRegister`, no Registrate; recipes, loot, tags and worldgen come from the donor (D12), not from JSON in our jar | The 1.20 Registrate repack does not run on 1.19.2, and shipping the generated JSON would redistribute data |
| D6 | **Cave carrot worldgen (owner, SETTLED, BUILT 2026-10-24, §4.3):** a pack rule in the underground block of `mushroom_spawns.json`, three Y bands: very common around Y 0 (core, Y -24 to 24, 1 in 2 chunks), less common but not rare at Y 25 to 44 (1 in 8) and Y -64 to -25 (1 in 5), nothing above Y 48. The mod's own biome modifier is switched off | Same system as the cave mushroom colonies |
| D7 | **Haste potions ship** (`miners_delight:haste`, `long_haste`, `strong_haste` from awkward + Copper Carrot; `mining_fatigue` from fermented spider eye) | *overlaps w/ Rustic Delight* `rusticdelight:haste` / `long_haste` / `strong_haste` (memory `rustic-delight-haste-potions`). Two potion ids with the same effect; review in `MND-40` **CONFIRMED then CHANGED 2026-11-02 (owner): every haste potion, Rustic's and Miner's, runs through the Creative Engineering common effect system; neither mod keeps its own haste potion lines (TODO `potions/effects #17`). `mining_fatigue` (fermented spider eye) not yet ruled on.** |
| D8 | **All loot modifiers ship** (built: the donor has 18 modifier files; its global list names 5 more that have no file, cooked bat wing / squid / tentacles / arthropods and raw arthropods, and the translator drops those 5 entries, so no fire-aspect cooked drops for those mobs) (bat wing, squid, glow squid, tentacles, spider, cave spider, spider leg, spider web, silverfish, silverfish eggs, arthropods, their cooked variants, infested blocks, fishing squid / glow squid, mineshaft chest) | They are the only source of most 1.4.5 ingredients. Squid ones overlap the ocean mods (§3) **CHANGED 2026-11-02 (owner): not shipped as the donor has them. All Miner's loot drops are rebuilt on the pack's own loot system (`kubejs/server_scripts/loot/`, tuning and rules), so rates are tunable and fire aspect cooked drops are done there (also covers the 5 donor entries with no file). Squid, glow squid and tentacles stay under the squid chain (TODO `dishes/ocean #3`, `#4`); check fished squid.** |
| D9 | **Onion soup cup (owner asked for the equivalent, SETTLED):** the equivalent is Brewin' and Chewin's **Creamy Onion Soup** (`brewinandchewin:creamy_onion_soup`, a bowl food from the port; its canned version is `creativeengineering:canned_creamy_onion_soup`). It is the only onion soup in the pack (checked by id and by display name over `kubejs/exported/display_names.json` and every jar). 1.4.5's cup conversion row is `farmersdelight:onion_soup` -> `onion_soup_cup`, and FD 1.19.2-1.2.4 has no such item, so the port adds a pack data file with the row `brewinandchewin:creamy_onion_soup` -> `miners_delight:onion_soup_cup` (§4.1 P9) | Noted for the review `MND-40`: a cup of creamy soup is the only way to get the cup |
| D10 | **Advancements** stay in the jar and are dropped at load with every other mod's | Pack rule |
| D11 | **1.1.02 is deployed (owner, SETTLED): the port replaces the live 1.1.2 jar in a later pack version.** The old jar leaves through the normal removal diff; the port's `-01` jar and the donor jar arrive in the same payload (the donor in its own category, `vinery_upstream` style, D12). Players get a changelog line for the hot cocoa cup loss | The earlier proposal (pull 1.1.2 out of 1.1.02) is moot |
| D12 | **Donor jar (SETTLED 2026-10-24 by P1):** the Forge 1.20.1 1.4.5 jar is the official Modrinth release `1.20.1-1.4.5-backport` (project `qMxbM4BQ`, file `https://cdn.modrinth.com/data/qMxbM4BQ/versions/p0INUam7/miners_delight-1.20.1-1.4.5-backport.jar`, sha1 matches the Downloads copy), pinned in `UpstreamJars`. Original proposal: the unmodified upstream jar, in its own folder `miners_upstream/` on server and client (FML would reject or clash with it in `mods/`: it has a `mods.toml` with the id `miners_delight`), sha1-pinned in `UpstreamJars`, copied into the deploy payload as the Vinery jars are, downloaded from source on publish. Recommended donor: the **Forge 1.20.1 1.4.5 jar** (data already in `recipes/`, `tags/items`, `loot_tables`, the 1.20 JSON shapes, same ids, so the translator is tiny). Fallback: the official NeoForge 1.21.1 jar (`minersdelight` namespace, `recipe/` `tags/item/` `loot_table/` folders, data maps), which needs a Vinery-size translator. Open: where the 1.20.1 1.4.5 jar comes from (it sat in Downloads as `miners_delight-1.20.1-1.4.5-backport.jar`, vendor Sammy Semicolon, built 2026-05-27, no source recorded); it must be a download the publish step can fetch | Checked: its recipes/loot/worldgen match the 1.21.1 jar item for item |
| D13 | **Code is ours, written from behaviour, not translated.** Unlike Vinery (MIT code, ARR assets), Miner's code is also all rights reserved, so the Java in the port is a fresh implementation of the behaviour the donor's data expects (registry ids, block entity types, menu and recipe interplay, the 21 loot modifier types and 4 loot functions/conditions the data names), not a line-for-line port of the decompile. The decompile under the scratchpad is read for behaviour only. About 3,100 lines remain after datagen and Registrate go (copper pot ~1,200, sticky basket ~460, cups ~350, JEI ~170, loot ~180, the rest registry) | Same effect as the Brewin' / Vinery work, with a clean boundary: nothing copied |

**Licence consequences (replaces the earlier L1 check, which the owner settled):**

- The jar we ship contains no upstream file: no model, texture, lang, recipe, loot modifier, tag, worldgen or class text.
- Everything the pack changes in upstream data (the onion cup row, haste potion review, loot values) goes through a
  translator (`REMOVED` ids, `patchData`, lang overrides), as in `UpstreamTranslator` for Vinery, never through a file
  at an upstream path. Vinery's gotcha applies: `checkNoUpstreamResources` forbids shadowing, and pack order between
  our mod pack and the donor pack is not guaranteed.
- Lang is the one piece we must own: names for the 69 items come from the donor's `en_us.json` at load, and the pack's
  own overrides (`names.js`, namechanges) still apply on top.
- The 1.1.2 jar is not touched by this plan until the port replaces it; both can never be loaded together (same mod id).

## 2. Merged inventory

### 2.1 Items (69)

| Group | In both versions | New in 1.4.5 | Only in 1.1.2 |
|---|---|---|---|
| Cave carrots | `cave_carrot`, `baked_cave_carrot`, `wild_cave_carrots`, `cave_carrot_crate` | `copper_carrot` (brews haste) | |
| Squid | `squid`, `glow_squid`, `tentacles`, `baked_tentacles`, `baked_squid`, `stuffed_squid` (feast), `bowl_of_stuffed_squid` | `tentacles_on_a_stick`, `glow_ink_pasta`, `squid_sandwich`, `takoyaki` | |
| Bat | `bat_wing`, `smoked_bat_wing` | `bat_cookie`, `bat_rolls`, `bat_soup`, `bat_soup_cup` | |
| Spider and insects | `silverfish_eggs`, `weird_caviar` | `spider_leg`, `baked_spider_leg`, `arthropod`, `cooked_arthropod`, `seasoned_arthropods`, `insect_stew`, `insect_stew_cup`, `insect_sandwich`, `insect_wrap`, `glazed_arachnid_limbs` (feast), `plate_of_glazed_arachnid_limbs` | |
| Vegan | `pasta_with_veggieballs` | `vegan_patty`, `vegan_hamburger`, `vegan_wrap`, `vegan_steak_and_potatoes`, `fake_meatloaf` (feast), `plate_of_fake_meatloaf` | |
| Other food | `cave_soup`, `cave_soup_cup`, `improvised_barbecue_stick` | `cave_hamburger`, `crunchy_bar`, `nutritional_bar`, `golden_nutritional_bar`, `moss` | |
| Cups (copper cup + soup/drink cups) | `copper_cup`, `milk_cup`, `water_cup`, `powder_snow_cup`, `baked_cod_stew_cup`, `beef_stew_cup`, `beetroot_soup_cup`, `chicken_soup_cup`, `fish_stew_cup`, `mushroom_stew_cup`, `noodle_soup_cup`, `pumpkin_soup_cup`, `rabbit_stew_cup`, `vegetable_soup_cup` | `bone_broth_cup`, `onion_soup_cup` (D9) | `hot_cocoa_cup` (**dropped**, D3) |
| Blocks and stations | `copper_pot`, `sticky_basket`, `gossypium` | `potted_gossypium` (block only) | |

### 2.2 Systems

- **Copper pot:** a small cooking pot (its own menu, screen, recipe book component, JEI category, meal copy loot
  function); cooks FD `farmersdelight:cooking` recipes that fit its slots and serves into cups.
- **Cups:** `cup_conversion/default_conversions.json` pairs a bowl food with its cup (the cups have no recipes, which is
  why JEI shows none: TODO `jei #14`). Cauldron interactions fill and empty the water and powder snow cups.
- **Sticky basket:** an FD-basket-like collector; CCK hooks its transfer cooldown (D1).
- **Food mixin:** `FoodPropertiesMixin` (the only mixin, same in both versions).
- **Feasts:** stuffed squid, fake meatloaf, glazed arachnid limbs (FD `FeastBlock`).
- **Potions:** 4 (D7). **Loot:** 21 modifiers (D8). **Worldgen:** wild cave carrots (D6). **Tags:** 44 data tag files
  (forge, farmersdelight, minecraft, brewinandchewin).

## 3. Overlaps with the pack

Batch sibling (not installed): [carnivore.md](archive/carnivore.md) section 2 lists where Carnivore's mob meats overlap this mod's drops.

Nothing is settled here. The ocean add-ons and Hostile Delight were added after the last `/dumpall`, so this list
comes from [ocean_delight.md](ocean_delight.md) and the hostile plan; `MND-01` refreshes the dumps.

### 3.1 Items and drops

| Miner's item | *overlaps w/* | Pack work already planned |
|---|---|---|
| `tentacles`, `baked_tentacles`, `tentacles_on_a_stick` | Ocean's Delight `tentacles`, `tentacle_on_a_stick`; Oceanic Delight `squid_tentacles`; Rustic Delight calamari | TODO `dishes/ocean #4` removes every tentacle item; tentacles cut to calamari |
| `squid`, `glow_squid` (drop from the mobs) | Rustic Delight calamari drops from both squids | TODO `dishes/ocean #3` keeps these two items; a cut squid gives calamari and ink sacs |
| Squid / glow squid / tentacle loot modifiers | Ocean's and Oceanic squid drop modifiers | `dishes/ocean #3`, `#4` decide one drop |
| `takoyaki` | Oceanic Delight `takoyaki` | |
| `glow_ink_pasta` | FD `squid_ink_pasta` (a variant, not a copy) | |
| `spider_leg`, `baked_spider_leg`, spider loot | Hostile Delight spider line ([hostile_delight.md](hostile_delight.md)) | |
| `arthropod` line, silverfish loot | Hostile Delight endermite / arthropod meats | |
| `vegan_hamburger`, `cave_hamburger`, `vegan_patty` | FD `hamburger`, `beef_patty`; More Delight hamburgers | |
| `vegan_wrap`, `insect_wrap` | the pack's tortilla wraps (Veggies port) | |
| `moss` (food) | vanilla moss block (name only) | |
| Soup cups | each soup's bowl and its canned version (`creativeengineering:canned_*`) | |
| `onion_soup_cup` | Brewin' `creamy_onion_soup` | D9 |
| Haste potions | Rustic Delight haste potions | D7 |

### 3.2 Systems

| Miner's system | *overlaps w/* | Note |
|---|---|---|
| Copper pot | FD cooking pot; the pack's remainder buffer (pot and kettle mixins only) | The copper pot gets no leftover slots unless the buffer is extended (I8) |
| Cups | Supplementaries jar soups (`mixed_jars.md` §9), cans | A cup is a third serving form beside bowl and can |
| Sticky basket | FD basket | |

### 3.3 How the overlaps get settled

After the first session with the port in game (`MND-40`), each row of §3.1 and §3.2 is looked at once: both sides in
JEI, as food (values, AppleSkin), and as drops. The outcome per row is "keep both", "keep one" or "merge", written back
to the table. A drop is then a separate change: removed in the port's registry (with an id remap only if a test world
holds it) and in the data the port ships.

## 4. Work

### 4.1 Port (development/creativeDelight/members/miners)

| Step | Work |
|---|---|
| P1 | Pick the donor (D12): confirm where the Forge 1.20.1 1.4.5 jar is downloaded from, record its sha1 and download URL for the publish step |
| P2 | New project `development/creativeDelight/members/miners` like `backportedVintage`: `gradle.properties` (`mod_id=miners_delight`, `mod_version=1.4.5-ce-19.2-01`, `jar_name=backportedMiners`, GPLv3, **no** upstream notice file because nothing upstream is shipped), `settings.gradle` include, `installToMods`. Copy the three generic donor classes from the Vinery port's `forge/upstream` (`UpstreamJars` sha1 pin, `MemoryPackResources`, `UpstreamPacks`) |
| P3 | `upstream/MinersTranslator`: the in-memory view of the donor: lists the donor's assets and data as a `PackResources` (client resource pack and server data pack, both sides), drops `hot_cocoa_cup` and every file naming it, moves the 10 stray `data/minersdelight/` recipes under `miners_delight/`, applies `REMOVED` ids and `patchData` for later drops, takes lang from the donor `en_us.json`. Fail fast at startup if the donor jar is missing or its sha1 differs (Vinery behaviour). A tool `tools/check_donor.py` reports every id the donor's data names against the registered ids |
| P4 | Java, written from behaviour (D13): `DeferredRegister` for 69 items / 10 blocks / 4 potions, copper pot (block, entity, menu, screen, recipe book, result slot, item handler), sticky basket, cups (`CopperCupItem`, `SolidCupItem`, `MilkCupItem`, `CopperCupFoodItem`, `HoneyBarItem`, `SilverfishEggsItem`), the 3 feast blocks, wild cave carrot and gossypium blocks, loot modifier types (`AddSeveralItemsModifier`, `ReplaceDroppedLootModifier`, block tag condition, copy-meal function), cup conversion reload listener, cauldron interactions, composting, food values (read from the donor's behaviour, kept equal), the one `FoodPropertiesMixin`, JEI category for JEI 11. 1.19.2 API from the start (no `GuiGraphics`, `getResultItem()`, a `CreativeModeTab` subclass); this is where the 1.20 differences found by grep do not matter any more, because nothing is translated |
| P5 | Farmer's Delight 1.19.2-1.2.4 gaps: `BlockShapes` (used by the two new feast blocks; copy the shapes) and `PlaceableItem` (`MDBlocks`; a small `BlockItem` subclass) are missing; every other FD class the code imports exists |
| P6 | Keep the CCK hook points (D1): `processCooking` calls `setRecipeUsed(Recipe)` (CCK injects there and calls `BlazeStoveBlock.startSignal` on the block below); `StickyBasketBlockEntity.setTransferCooldown(int)` and `isOnTransferCooldown()` keep their signatures. Check against CCK's bytecode as for Brewin' |
| P7 | Cut: the hot cocoa cup is dropped (D3), no class, no row |
| P8 | Build, then the offline checks (§7); swap the jar: 1.1.2 to `jar_archive/displaced/`, port `-01` into `mods/`, donor into `miners_upstream/` (server and client), `mod_sides.md` rows for both, `build_deploy.py` category for the donor (pin its sha1 beside `vinery_upstream_jars()`), `tools/dump_server_mods.py`. Never swap while the game runs |
| P9 | Pack data file for D9: `data/creativeengineering/miners_delight/cup_conversion/creamy_onion_soup.json` (the listener reads every `cup_conversion/*.json` of every namespace, to be confirmed in the decompile of `CupConversionReloadListener`; if it reads only `miners_delight/cup_conversion`, the translator appends the row instead) |

**As built (2026-10-24):** P1 to P7 and P9 are done; P8 is built and checked but the jar swap waits for the game to
close.

- P1: donor = the Modrinth release (D12); the jar is copied to `miners_upstream/`, which the pack's `.gitignore`
  allow-list keeps out of git like `vinery_upstream/`.
- P2: `gradle.properties` has `replaces_jars=miners_delight-*.jar`, so `installToMods` moves the 1.1.2 jar to
  `jar_archive/displaced/` together with any older port jar. No upstream notice file.
- P3: `MinersTranslator` also renames the 1.20 loot condition `minecraft:any_of` to `minecraft:alternative` (4 loot
  modifiers use it; 1.19.2 would reject them) and drops the 5 dangling entries of `global_loot_modifiers.json` (D8). The
  check tool is `development/creativeDelight/members/miners/tools/check_donor.py` (0 problems: 63 recipes, 11 loot tables, 18 loot
  modifiers, 35 tags, the cup rows).
- P4: 49 classes. The pot and basket follow Farmer's Delight's own pot and basket (MIT), which the upstream classes
  mirror. Differences from upstream behaviour are listed in `development/creativeDelight/members/miners/README.md` (the empty cup
  milks only cows and goats, no Forge fluid capability on cups, two no-op brewing steps left out, "Served on" tooltip
  on the meal display slot, cup table sent to clients). The cup foods read their bowl's effects through the accessor
  mixin (`FoodPropertiesAccessor`) so FD's effects resolve only when eaten; `PotionBrewing.addMix` is opened by the
  access transformer.
- P5: `TrayFeastBlock` defines the tray shape (FD 1.21's numbers); feast items are plain `BlockItem`s.
- P6: checked with `javap`: `processCooking(CookingPotRecipe, CopperPotBlockEntity)Z` calls `m_6029_`
  (`setRecipeUsed`, the name CCK's refmap expects), `setTransferCooldown(I)V` public, `isOnTransferCooldown()Z`.
- P8 done: installed with `installToMods`, shipped in 1.1.03 (2026-10-26); `MND-01` server half passed as SQ-MDP-01. `mod_sides.md`
  row updated (one row: matched by mod id). `tools/build_deploy.py` ships and sha1-checks `miners_upstream/` like
  `vinery_upstream/` (`UPSTREAM_DONORS`). `tools/dump_server_mods.py` copies only `mods/`, as for Vinery; the server
  needs `miners_upstream/` copied by hand.
- P9: the listener reads `data/<any namespace>/cup_conversion/*.json`, so the row is
  `kubejs/data/creativedelight/cup_conversion/creamy_onion_soup.json`, shipped in the Creative Delight jar (moved from
  `creativeengineering` 2026-10-25, creative_delight.md 27; not the path first planned). Onion soup cup
  food = half of FD 1.21's onion soup (12, 0.8, Nourishment 3 min): 6, 0.4, Nourishment 1:30.

### 4.2 Pack integration (after the build, before `MND-40` where it changes what is tested)

| Step | Work |
|---|---|
| I1 | `/dumpall` and the generators (memory `dumpall-refresh-workflow`). The 2026-10-30 client dump already has the port (69 items); I3 to I9 below were checked against it 2026-11-01. A fresh `/dumpall` (with `/cedumpnames`) is still needed for the names audit |
| I2 | **DONE 2026-10-24:** `item_dict.js` block `miners_delight (69)`; bare keys except `miners_delight_moss` (`moss` is the moss block). Until the port is installed, startup warns for the 32 ids the 1.1.2 jar lacks |
| I3 | Food data. **Checked 2026-11-01 on the 10-30 dump:** `food_types.json` types all 57 Miner's foods (`food_catalog.py --check` names none); compost needs nothing (rules cover cave carrot, baked cave carrot, bat cookie, gossypium, moss, vegan patty, wild cave carrots; the bars and spider leg stay out on purpose, `SKIP_ORIGINAL`); stack sizes need nothing (`gen_stack_sizes.py` gives every Miner's food a rule: soups, cups and plates 32, handhelds 64, golden bar 16; the file did not change). **Cans DECIDED and BUILT 2026-11-01 (owner):** Canned Bat Soup, Cave Soup, Insect Stew and Golden Nutritional Bar, as explicit rows in `gen_can_types.py` (run); Miner's stays in `UNDECIDED_MODS`, so no other Miner's food and no cup is canned (SQ-VDF-09). **Open:** Names audit: `kubejs/exported/display_names.json` is from 2026-10-20 and has no `miners_delight`, so it waits for a client `/cedumpnames` (CQ-MND-NAMES) |
| I4 | **DONE by the port (P4), checked 2026-11-01:** the Copper Pot category (`jei/CopperPotCategory`) shows every FD cooking recipe of up to four ingredients as the pot serves it, so a soup with a cup row shows its cup (2 per bowl) as the output and the Copper Cup as container; R on a soup cup finds it. All 15 cup rows have such a recipe in the 10-30 dump (the bone broth through the pack's own `bone_to_2_bone_broth` row, the onion soup cup through Brewin' Creamy Onion Soup). The Copper Pot is the catalyst of its own category, not of FD's (FD's has recipes of five and six ingredients the pot cannot cook). TODO `jei #14`; tests MND-15, MND-18 |
| I5 | Wild cave carrots: **DONE 2026-10-24** (§4.3). **Done 2026-11-01:** crops loot: `L.pack(cave_carrot_crate, cave_carrot)` in CD `miners_delight/loot.js` (the mineshaft cave carrots and copper carrots stay Miner's own chest loot modifier, D8); WTHIT growth line: `cave_carrots` is a `CropBlock` and WTHIT-native (10-30 crop growth audit), no rule needed; gossypium and the wild patch do not grow |
| I6 | Shared tags through `global.tags`: tentacles, spider meat, insects, cups by soup. **2026-11-01:** tentacles moot (removed 2026-10-29, `mod-additions #5`); spider meat and insects are Miner's own `raw_insect_meat` / `cooked_insect_meat` tags; joining them with Hostile Delight's spider line or Alex's Mobs `insect_items` is an overlap merge, so it waits for `MND-40`; cups by soup: no recipe or system reads such a tag yet, so none is made until one does |
| I7 | **DONE (checked 2026-11-01):** the pot's `isHeated` is FD's `HeatableBlockEntity` default, the same code and tags as the FD pot (`#farmersdelight:heat_sources`, tray heat sources, the lit gate), so the pack's extras (the oven in `heat_sources`, CD `kitchen_tags.js`) apply. MND-19 |
| I8 | **BUILT 2026-11-01 (port `-02`, native, Vintage jar pattern):** leftover slot 7 right of the output (frame copied from the output's), take-only, extracted from the bottom face, dropped when the pot breaks; old 7-slot saves widened on load; config `config/miners_delight-common.toml` `[copper_pot] remainderBuffer` and `stallWhenFull` (both on): the pot waits when a meal's leftovers would not fit. Only crafting remainders (bucket, bottle, bowl), as the pot threw before. Not covered: the WTHIT "Holding" line (patcherMod `WthitRemainderBuffer` knows only the FD pot, kettle and oven). MND-18 |
| I9 | **DONE (since 1.1.03):** `gen_soup_soft_fluids.py` gives every `food_type/soup` item a soft fluid, so the 15 cups and bat soup, cave soup, insect stew are jar liquids (`kubejs/data/creativeengineering/moonlight/soft_fluids/*_cup.json`, the cup as container, 2 per jar slot) |

### 4.3 Cave carrot worldgen (BUILT 2026-10-24, owner request)

Wild cave carrots follow the cave mushroom colonies: a rule `cave_carrots` in the underground block of
[mushroom_spawns.json](../kubejs/generation_scripts/worldgen/mushroom_spawns.json) (`mode: cave`,
`underground_decoration`, every overworld biome), applied by `mushroom_spawns.js` (CEConfig `[worldgen.systems]
mushroomSpawns`, new chunks only). The mod's own modifier `miners_delight:cave_carrots` is switched off in the same
rule (`forge:none`); it is skipped without the mod.

| Band | Placed feature | Origin Y | Rarity | Per Y layer vs core |
|---|---|---|---|---|
| core (very common) | `creativeengineering:wild/cave_carrots/core` | -24 to 24 | 1 in 2 chunks | 1.0 |
| deep | `.../cave_carrots/deep` | -64 to -25 | 1 in 5 | about 0.5 |
| high | `.../cave_carrots/high` | 25 to 44 | 1 in 8 | about 0.5 |

- A rolled chunk tries 8 cave floor spots (random Y in the band, scan down 16 for a floor, one block up, 13+ blocks below
  the surface, air above stone or dirt); each spot is a patch of 64 tries, spread 6 wide and 3 up
  (`creativeengineering:wild/cave_carrots`, same patch as the mod's, on `#minecraft:base_stone_overworld` or
  `#minecraft:dirt`, deepslate included).
- **Carver caves count:** the carrot features accept `air` and `cave_air` (the cave mushrooms accept only `air`, so they
  grow in noise caves only: [fungi_delight.md](fungi_delight.md) 5.1.1). Without `cave_air` the classic caves around Y 0
  would stay empty and "very common" could not hold.
- **Nothing above Y 48:** an origin is at most Y 44, +1 for the scan offset, +3 for the patch spread = Y 48.
  `tools/test_mushroom_spawns.sh` fails if any carrot band's height range tops 44.
- The rates are a first guess from the owner's words (very common near 0, less common but not rare near 32 to 48
  and -64), compared with the mod's own 1 in 3 chunks over Y -54 to 48. `MND-32` tunes them.
- The wild carrots are still the mod's own block: with the 1.1.2 jar the rule works now, and keeps working after the
  port (same block id `miners_delight:wild_cave_carrots`).

## 5. Open questions

- D12 is settled (Modrinth). D1, D4, D5, D7, D8, D10, D13 are the plan's proposals, built as written; D2, D3, D6, D9,
  D11 are the owner's.
- Old art: 1.4.5 redrew 13 textures. DECIDED 2026-11-01 (owner): new art for the cave carrot, weird caviar, stuffed squid bowl and gossypium; the rest keep the 1.1.2 look (TODO `creativedelight/members #31`). BUILT 2026-11-01 (port `-02`): the 1.1.2 jar is a second donor in `miners_upstream/` (`miners_delight-1.19.2-1.1.2.jar`, sha1 `23701e0772676ff74c0290387c90fd9365987673`, CurseForge project 689630 file 4738272), pinned in `UpstreamJars.MINERS_112` and read for four files only (`MinersTranslator.OLD_LOOK`): `block/sticky_basket_side`, `item/baked_cave_carrot`, `item/tentacles`, `item/baked_tentacles`. The other nine redraws stay 1.4.5's (`cave_carrots_0` to `_3`, `wild_cave_carrots`, `item/cave_carrot`, `gossypium`, `weird_caviar`, `bowl_of_stuffed_squid`); the TODO's "other nine" counted these the other way round. `build_deploy.py` ships and sha1-checks the new jar through `UPSTREAM_DONORS` (it reads every `new Jar` of `UpstreamJars.java`); the server needs it copied into `miners_upstream/` by hand, like the 1.4.5 jar. The game stops at load when it is missing. MND-05
- Takes in TODO `dishes/ocean #3` and `#4` change what the port's squid and tentacle items do; doing the port first
  and the ocean merge after keeps both pieces of work simple.

## 6. File map

| File | Role |
|---|---|
| `development/creativeDelight/members/miners/` | The port (new) |
| `development/creativeDelight/members/miners/.../upstream/` | Donor view: sha1 pin (`UpstreamJars`), translator (nothing upstream in the jar); the jar reading and the in-memory packs are the shared `DonorJars` / `BuiltInPacks` / `MemoryPackResources` ([pack_tooling.md](pack_tooling.md) 2.8), registered in `MinersDelightMod` |
| `miners_upstream/` | The donor jar (server and client), like `vinery_upstream/` |
| `settings.gradle` | Include |
| `kubejs/startup_scripts/_lib/item_dict.js` | Keys |
| `kubejs/server_scripts/` (via `global.NCH`, `global.tags`) | Recipes and tags the integration needs |
| `kubejs/generation_scripts/food/`, `worldgen/wild_crops.json` | Food data, wild cave carrots |
| `mod_sides.md` | Row: `both`, jar name changed |

## 7. Offline checks (all passed 2026-10-24)

Results: build passes; the jar holds `META-INF/{MANIFEST.MF,mods.toml,accesstransformer.cfg}`, `pack.mcmeta`, the
mixin config and refmap, the licence and 49 classes under `com/sammy/minersdelight/`, nothing else; no `GuiGraphics` or
`RegistryAccess`; `check_donor.py` 0 problems; CCK targets as in P6. A headless run of the real `MinersTranslator`
over the donor (javac harness in the scratchpad) gives 182 data files and 228 assets, all JSON valid, no `any_of` left,
no stray namespace, 18 loot modifier entries, 14 cup rows (onion row dropped).


- `./gradlew :backportedMiners:build --offline` passes.
- The built jar holds no upstream file: no `assets/miners_delight` model/texture/lang, no recipe/tag/loot/worldgen JSON, no class copied from the donor (`jar tf` shows only our classes, `mods.toml`, `pack.mcmeta`, mixin config). `javap` over the classes shows no `GuiGraphics` or `RegistryAccess`.
- Every id named in the jar's recipes, tags, loot modifiers and cup conversions resolves to a registered id or a pack
  id in `kubejs/exported/` (a `check_data.py` like Brewin's).
- CCK mixin targets exist with the right descriptors (javap on the built `CopperPotBlockEntity` and
  `StickyBasketBlockEntity`).

## 8. In-game test suite (drafted)

- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): PASS SQ-MDP-18, -18b.

Boot and inventory

- [ ] MND-01 The game boots with the port and without the 1.1.2 jar; no registry, mixin or recipe error naming
  `miners_delight` in `latest.log`; CCK loads its three Miner's mixins (no "target not found"). `/dumpall` after.
- [ ] MND-02 The creative tab holds 69 items; every item has a name and texture (no purple-black, no raw key).
- [ ] MND-03 A world that had 1.1.2 items (if any) keeps them after the swap.
- [ ] MND-04 With `miners_upstream/` renamed away the game stops at load with the message naming the folder and the
  Modrinth link; with it back, `latest.log` has `Miner's Delight upstream data: 182 files` and `Cup conversions: 15
  bowl foods`. Port `-02`: the same with only the 1.1.2 jar moved away (message names it and the CurseForge link);
  `upstream assets: N files (4 in the 1.1.2 look)`.
- [ ] MND-05 (port `-02`) Sticky Basket sides, Baked Cave Carrot, Tentacles and Baked Tentacles (`/give`; the tentacles
  are removed items) show the 1.1.2 art; cave carrot crop stages, wild cave carrots, cave carrot item, gossypium, weird
  caviar and the stuffed squid bowl show the 1.4.5 art.

Copper pot and cups

- [ ] MND-10 The copper pot cooks a 1.1.2 recipe (cave soup) and a 1.4.5 one (takoyaki) over a heat source; the meal
  slot and container rules work; the recipe book and JEI [+] fill it.
- [ ] MND-11 With Create Central Kitchen: a copper pot on a Blaze Stove; when the pot finishes a meal the stove gets
  its signal (CCK's `processCooking` hook), as with the FD pot.
- [ ] MND-12 Cup conversions: a bowl soup in the copper pot serves as its cup; each cup drinks, gives its effects and
  returns the copper cup; the water and powder snow cups fill and empty at cauldrons; (hot cocoa cup dropped, D3)

- [ ] MND-13 The onion soup cup: a bowl of Brewin' Creamy Onion Soup in the copper pot serves an onion soup cup (D9, P9); the cup drinks and returns the copper cup. No `farmersdelight:onion_soup` row is read.
- [ ] MND-14 Sticky basket collects items; with CCK, its transfer cooldown behaves as before.
- [ ] MND-15 On the dedicated server: the copper pot's recipe book ghost and JEI show cup results (e.g. 2 cave soup
  cups) and a Copper Cup as container, so the cup table reached the client.
- [ ] MND-16 Empty copper cup: right-click water source = water cup, a cow or goat = milk cup, a fish or axolotl does
  nothing (the fish stays, the cup stays); water cup pours a source; powder snow cup places powder snow.
- [ ] MND-18 (port `-02`, I8, `jei #14`) Leftover slot: cook a recipe with a milk or water bucket or a honey bottle
  (any FD pot recipe of up to four ingredients) in the Copper Pot: the empty bucket or bottle lands in the slot right of
  the output, not on the floor; it cannot take items by hand; shift-click and a hopper under the pot take it out; with
  the slot full of another item the pot does not start the meal; breaking the pot drops it. A pot placed with `-01` and
  holding items keeps them after the swap. With `remainderBuffer = false` leftovers fly out as before. Single player:
  R on a soup cup in JEI opens the Copper Pot recipe that serves it.
- [ ] MND-19 (I7) The Copper Pot cooks on the pack's heat sources the FD pot uses (campfire, lit oven below), and not
  on an unlit oven.
- [ ] MND-17 Cups stack to 64 (empty, water, milk, powder snow; soup cups 32 by the pack's stack policy [upstream 16]). With a stack of 5 powder snow cups: place one, the other 4 stay and an empty cup lands in the inventory. A stack of water cups emptied on the ground and filled at a cauldron keeps the rest of the stack. A stack of milk cups drinks one at a time and returns empty cups. The craft: 3 copper ingots (`X X` over ` X `) makes a Copper Cup.

Food and feasts

- [ ] MND-20 Each feast (stuffed squid, fake meatloaf, glazed arachnid limbs) places, serves its portions with the right
  container and drops its leftovers.
- [ ] MND-21 The 1.4.5 foods eat with their effects (nutritional bars, golden nutritional bar, bat cookie fast eat).
- [ ] MND-22 Haste and mining fatigue potions brew (Copper Carrot into awkward; redstone, glowstone, fermented spider
  eye variants).

Drops and worldgen

- [ ] MND-30 With a knife: bat (bat wing), squid and glow squid (squid items, tentacles), spider and cave spider (spider
  legs), silverfish (eggs, arthropods); fire-aspect kills give the cooked versions; without a knife, only the
  knifeless modifiers fire. Infested blocks drop silverfish eggs.
- [ ] MND-31 Fishing can catch a squid and a glow squid item.
- [ ] MND-32 Wild cave carrots (cave rule, §4.3; runs with the 1.1.2 jar now): in new chunks only, carrots appear in caves
  of every overworld biome, **very common around Y 0**, clearly less common but still easy to find at Y 25 to 48 and at
  Y -64 to -25, and **never above Y 48** (check with `F3` at the highest patches, and `/ceplace`-style counting or a
  Y histogram of `wild_cave_carrots` in a loaded area). None on the surface. The mod's own spawn (1 in 3, Y -54 to 48)
  is off: `latest.log` has no error for `miners_delight:cave_carrots`. Cave carrots plant and grow; WTHIT shows growth.
- [ ] MND-33 Abandoned mineshaft chests can hold Miner's loot.
- [ ] MND-34 (I5) `/loottuning` (or the log) lists the cave carrot crate pack; `/loot spawn` on a table with cave carrots (Miner's `chests/md_abandoned_mineshaft`) sometimes gives Cave Carrot Crates instead.

Review (decides the drops)

- [ ] MND-40 Overlap review: every row of §3.1 and §3.2 gets keep both / keep one / merge, written into §3. Includes
  D7 (haste potions), D9 (onion soup cup) and the 13 redrawn textures.
- [ ] MND-41 After the drops are built: the dropped ids are gone from JEI, loot and worldgen; nothing in `latest.log`
  names them.

## 9. History

- 2026-10-24: plan written. Base chosen (Forge 1.20.1 1.4.5 build, mod id `miners_delight`); four builds compared;
  CCK hook points and FD 1.19.2 gaps found; nothing built.
- 2026-10-24 later (owner answers): licence is all rights reserved, so the port follows the Vinery donor-jar method
  (D2, D12, D13); hot cocoa cup dropped (D3); 1.1.02 with the 1.1.2 jar is deployed (D11); onion soup cup equivalent is
  Brewin' Creamy Onion Soup (D9); cave carrot worldgen built as a cave rule (D6, §4.3). Plan now counts 69 items.
- 2026-10-24 build: D12 settled (the donor is the Modrinth release); port built as planned (P1 to P7, P9), offline
  checks pass (§7), I2 done; jar swap (P8) waits for the game to close. Found while building: 1.20's `any_of` loot
  condition and 5 dangling global loot modifier entries (both handled in the translator), and the cup conversion data
  path (`cup_conversion/` at the namespace root).
- **2026-11-02:** nested in Creative Delight (`creativedelight/members #1`): `bundled_in=creativeDelight`, jar `backportedMiners-1.4.5-ce-19.2-03` inside the CD jar, "Miner's Delight (Creative Delight)" (also in the donor-missing message); folder stays `development/creativeDelight/members/miners`. `miners_upstream/` is unchanged (both jars, next to `mods/` on server and clients); `build_deploy.py` now ties it to the CD jar. `miners_delight-*.jar` moved to CD's `replaces_jars`.

## 10. Original survey (2026-10-23, before the port plan)

Moved here from the former combined add-ons survey; the survey that started the port. Row: `largecontent/mod-additions #7`.
Miner's Delight 1.1.2 is `miners_delight-1.19.2-1.1.2.jar` (37 items; squid, glow squid, bat wing, silverfish eggs drops). The
sections above supersede its conclusions (the merged backport replaces the "backport, not update" open question).

The installed `miners_delight-1.19.2-1.1.2.jar` is the **OLD** version. The newer line is in
`DecompiledMods/minersdelight-1.21.1-1.4.5.jar` (NeoForge 1.21.1, mod id `minersdelight`, needs Lodestone, Farmer's
Delight). Not a drop-in: different mod id (`minersdelight` vs `miners_delight`), different loader, so a newer
Miner's Delight on 1.19.2 would be a backport in the Veggies / Vintage style, not an update.

**2026-10-24: the merged backport is this file** (behaviour reference: the Forge 1.20.1
build of 1.4.5, mod id `miners_delight`; licence all rights reserved, so the Vinery donor-jar method; all 1.1.2 and 1.4.5 content except
the hot cocoa cup, overlaps reviewed in game before any drop; onion soup cup = Brewin' Creamy Onion Soup; cave carrots now placed by the
cave mushroom system).

| | Old (1.1.2, 1.19.2) | New (1.4.5, 1.21.1) |
|---|---|---|
| Items | 37 | 69 |
| Blocks (blockstates) | 7 | 10 |
| Loot modifiers | 8 (bat wing, squid, glow squid, silverfish eggs, infested blocks) | 9+ (adds spider web, spider, arthropods, cooked squid / bat wing / spider leg) |
| Worldgen | none | 3 files |
| Tags | 1 (milk) | 44 |

Content in the old version that the new one still has: copper pot and cup, cave carrot (+ wild + crate), bat wing,
squid / glow squid / tentacles line, silverfish eggs, weird caviar, noodle / vegetable / beef / chicken / rabbit /
mushroom / beetroot / pumpkin soup cups, milk / water / powder snow cups, sticky basket, gossypium, improvised
barbecue stick, pasta with veggieballs.

Only in the old version: `hot_cocoa_cup` (1).

New in 1.4.5 (33 items): bat line (`bat_cookie`, `bat_rolls`, `bat_soup` + cup), spider and arthropod line
(`spider_leg`, `baked_spider_leg`, `arthropod`, `cooked_arthropod`, `seasoned_arthropods`, `insect_stew` + cup,
`insect_sandwich`, `insect_wrap`, `glazed_arachnid_limbs` + plate), vegan line (`vegan_patty`, `vegan_hamburger`,
`vegan_wrap`, `vegan_steak_and_potatoes`, `fake_meatloaf` + plate), `cave_hamburger`, `copper_carrot`, `crunchy_bar`,
`nutritional_bar`, `golden_nutritional_bar`, `moss`, `onion_soup_cup`, `bone_broth_cup`, `glow_ink_pasta`,
`squid_sandwich`, `takoyaki`, `tentacles_on_a_stick`.

Overlap of the new additions with the pack: onion soup (Veggies Delight port has onions and soups: check), vegan
hamburger / wrap (Farmer's Delight `hamburger`, wraps), `takoyaki` (Oceanic Delight), `tentacles_on_a_stick` (Ocean's
Delight `tentacle_on_a_stick`), `glow_ink_pasta` (Farmer's Delight `squid_ink_pasta`), spider/arthropod meat
(Hostile Delight, Unusual Delight `spider_leg`, `spiderbarbecue`).

### 10.1 Soup cup finding (2026-10-24)

`largecontent/mod-additions #7`: gains an investigation of the soup cup. Finding so far: the cups come from the
  data file `miners_delight/cup_conversion/default_conversions.json` (bowl -> cup pairs), not from recipes, which is why JEI
  shows none.

- 2026-11-01: open items run (TODO `mod-additions #7`, `jei #14`, `creativedelight/members #31`): port `-02` built
  (`./gradlew :backportedMiners:build --offline`, `check_donor.py` 0 problems): Copper Pot leftover slot and config
  (I8), the 1.1.2 jar as a second sha1-pinned donor for the four textures that keep their old look (§5). Cave carrot
  crate split in the loot framework (I5). I3, I4, I7, I9 checked against the 10-30 dump (§4.2). Not installed:
  `-02` sits in `build/libs/`; `miners_upstream/` now also holds the 1.1.2 jar (copied from `jar_archive/displaced/`).

## 11. Before and after comparison graphic (2026-11-01)

Owner asked for a picture of what the 1.1.2 to 1.4.5 swap changes. Made from the displaced 1.1.2 jar and the donor jar:
13 textures redrawn (cave carrot stages, gossypium, wild cave carrots, sticky basket, baked cave carrot, tentacles, baked
tentacles, stuffed squid bowl, weird caviar), 62 textures new, none removed (the hot cocoa cup has no texture in either
folder). Magenta cells are masks and overlays that are not standalone art. Generated into the session scratchpad
(`mj/miners_before_after.png`, script `mj/make.py`), deliberately not in the repo: the art is all rights reserved.
Haste: one Creative Engineering effect shared by all sources (`potions/effects #17`, D7 overlap with Rustic's haste).
- **2026-11-02 (afternoon):** folder moved to `development/creativeDelight/members/miners` (plan_creative_delight.md I8),
  port `-04`. Fixes after scratch r1102: Cave Hamburger recipe back with calamari or brown mushroom in the former
  baked-tentacle slots (I3, CD `miners_delight/recipes.js`); the Copper Pot no longer cooks into a full leftover slot when
  the meal slot is empty (I4, `canCook`); the cave carrot crate rule now reaches `md_abandoned_mineshaft` because
  `tools/gen_loot_config.py` reads the 1.4.5 donor jar (I5); `scavenging_spider` gets the upstream-missing `is_on_fire:false`
  through `MinersTranslator.PATCH_DATA` (I7). Reruns: SQ-MDP-13, -14, -16 / -09, new SQ-MDP-18.

## Copper cup family removed (2026-11-02, TODO `dishes/meals #53`; SQ-CUP-01 to -05, CQ-CUP-01)

First pass the same day hid the 15 soup and stew cups in KubeJS and emptied the cup table; the owner then asked for the
whole cup to go. Now:
- The port registers no cup: `copper_cup`, `water_cup`, `milk_cup`, `powder_snow_cup` and the 15 soup and stew cups are
  gone from `MDItems` (50 items left), with `CopperCupItem`, `SolidCupItem`, `MilkCupItem`, `CupConversions` (reload
  listener, client sync channel) and the cauldron rules in `MDSetup`. `MDFoods.cup()` and the four cup-only food values
  are gone. No conversion: cups in old worlds drop on load (as the hot cocoa cup, D3).
- `MinersTranslator.REMOVED` lists all 20 cup ids (hot cocoa included): their models, textures, lang keys, the cup
  recipe and advancement, the Create fill/empty cup recipes drop; `#forge:milk` loses the milk cup; `cup_conversion/`
  is not read. The Copper Pot recipe keeps its Water Bucket and loses the Water Cup alternative (`PATCH_DATA`, applied
  before the removed-id check). `tools/check_donor.py` mirrors this.
- The Copper Pot serves every meal in its recipe's container (`servedResult` / `servedContainer` are the recipe's own);
  shift-click sends only bowls to the container slot.
- Delightful's Cactus Soup Cup (served by the pot from Cactus Soup) is gone too: item, `CupItem`, food value, art, lang,
  its cup conversion row; `port_resources.py` no longer copies it.
- Pack side: the KubeJS hide script (CD `miners_delight/miners_items.js`), the empty `kubejs/data/miners_delight/
  cup_conversion/` override and the Spout/Drain water cup recipe are deleted; cup keys left `item_dict`, `food_dict`,
  `tool_dict`, the food types, stack sizes, comfort/nourishment, physics and food family data; the 15 cup jar fluids
  and their JEI jar rows are gone; `gen_comfort_nourishment.py` drops its copper cup rule.

D9 (onion soup cup from Creamy Onion Soup) and P9 are retired. MND-12, MND-13, MND-15, MND-17 and the cup half of
MND-02 / MND-10 no longer apply.
