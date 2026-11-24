# Publish readiness: Creative Common, Creative Compat, Creative Delight, Raspberry Pie

Analysis of 2026-11-22 (owner request, chat 2026-11-22): what stands between the four mods and publishing each on its
own, outside the pack. Read-only: nothing was built, booted, fixed or re-statused. Findings carry ids (`PR-<area>-NN`)
so they can be ruled, turned into TODO rows or dropped by id. No TODO row exists for any finding yet; rows are created
after the owner rules on section 2.

## 0. At a glance

| Mod | Version | Verdict | Blockers | High | What decides it |
|---|---|---|---|---|---|
| Creative Common | 1.3.0 | Not ready | 1 | 5 | Pack lists and pack defaults ship in the library; the project page understates what the jar does by default |
| Creative Compat | 1.0.0 | Not ready | 1 | 6 | The jar carries pack tuning and a starter kit that can delete items; most upstream versions are not range-gated; the writeup is far behind the code |
| Creative Delight | 1.1.0 | Not ready | 1 | 8 | A hard link to Quark, mandatory dependencies outside the four, donor jars that stop the game when missing, one license question (Fruits Delight derived art) |
| Raspberry Pie | 1.0.0 | Closest | 2 | 4 | Code for the standalone plan is built; no isolation test has run, a clean clone cannot build it, and the installed jar is a dirty build |
| Repository (shared by all four) | | Not ready to go public | 1 | 6 | Repo visibility unrecorded, personal data in tracked files and in 19 commits, no clean-clone build |

What is in good shape across all four: no network access, no update-check URL, no IO outside the game folder, commands
permission-gated, packets validated, no personal data or dev leftovers in any built jar, LICENSE and NOTICE in every
jar and nested jar, every dependent on Common `[1.3.0,)`, and the headless suite at 42 of 44 passing (the two failures:
one dated comment in Compat's `PottableTooltip.java`, two non-ISO dates in the texture tools).

## 1. Method and coverage

| Part | How | Size |
|---|---|---|
| Tooling baseline | `tools/check_all.py`, `tools/design_rules_audit.py -v`, `namespace_lint.py`, `jarstamp.py pending` | 44 checks |
| Creative Common | every mixin and event handler read, the rest skimmed; 36 writeup claims checked | 146 Java files |
| Creative Compat | all 32 ungated mixins, every auto-subscribed class, the plugin; about 95 gated mixins by target and gate only; 35 writeup claims | 328 Java files |
| Creative Delight core | every mixin, plugin, event handler, block entity; 33 writeup claims | `development/creativeDelight/src` |
| Creative Delight members | 20 members: license evidence, nested jar listing against source for 10, scripts, data; about 95 writeup claims and paths | `members/`, `kubejs/data/creativedelight/` |
| Raspberry Pie | all 63 Java files, resources, built jar, plan and rows; about 40 writeup claims | `development/solRaspberryPie` |
| Repo, metadata, jars | tracked files and added-path history for personal data, `mods.toml` of every jar, built-jar contents, status docs | whole repo |

Evidence column in the tables: **C** = confirmed by a second read of the code after the review; **R** = read once by
the review; **I** = inferred (reasoned from the code, not run). Nothing here was run in game. Mixin targets were not
checked against the third-party jars. License rows are a reading of the files in the tree, not legal advice.

## 2. Decisions for the owner

These shape the fix list; the agent default is given where one is obvious.

| # | Decision | Why it matters | Agent default (unconfirmed) |
|---|---|---|---|
| D1 | Is Creative Compat published as "the pack's tweaks" (keep the content, fix the description, add switches) or as compatibility fixes only (pack tuning and the starter kit move out)? | PR-CP-01 to -04 are fixed in opposite ways under the two answers | Compatibility fixes only; pack tuning moves to the pack folder |
| D2 | Does Creative Common keep pack defaults (honeycomb x2, three effect swaps, six category overrides, recipe unlocks off) or ship vanilla defaults with the pack's values in the pack config? | PR-CC-02, -04, -05, -06 | Vanilla defaults; the page already promises them |
| D3 | Creative Delight's minimum set: do Creative Calendar and Creative Loot join the publish wave, or do they become optional? Does `kubejs_create` (and so Create) stay mandatory? | "On its own" is not possible today (PR-CD-05) | Calendar and Loot join the wave; `kubejs_create` optional |
| D4 | Fruits Delight derived art (174 Vintage textures and the core lemonade textures): rederive from an LGPL build, build at run time on the client, ask the author, or accept? | The only license finding that could block Delight (PR-CM-01) | Ask the author; hold Delight until answered |
| D5 | Donor jars: must a missing donor stop the game, or disable that member with a warning? | Six members throw at load without seven pinned jars (PR-CM-04) | Disable the member with one warning |
| D6 | A loose original (Farmer's Delight and others) beside the bundle: fail with a message, or keep the single log line? | Overlays go missing silently (PR-CM-03) | Fail with a message naming the jar |
| D7 | Repository: make the existing repo public (with or without a history rewrite for the 19 commits carrying a personal address), or publish from a fresh export? | PR-RE-01, -02 | Fresh export or rewritten history, after the tracked-file cleanup |
| D8 | Which versions of Common were uploaded to CurseForge (1.1.0, 1.2.0), and is the project public? | The 1.3.0 changelog cut-off and the version rule depend on it (PR-RE-10) | none |
| D9 | Raspberry Pie open defaults: the three added default unlocks (Jump Boost II, Haste II, Slow Falling), the `diversityIconFallback` list, the shared Diet Log icon, CurseForge or Modrinth | Flagged unconfirmed in plan_rp_standalone.md sections 4, 8, 9 | none |
| D10 | Rule exemptions proposed in section 8 (DR-C2, DR-C7, DR-C10) | Otherwise they stay open violations | Record the exemptions |

## 2a. Owner rulings (2026-11-22)

Wave 1 is Common, Compat and Raspberry Pie. Creative Delight is dropped from the first wave (D3), so D4 to D6 are
deferred with it (TODO `publish-readiness #7`, Pinned). TODO rows exist for every wave-1 finding group:
`creative-common-publish #10` to `#15`, `creative-compat-publish #1` to `#6`, `raspberry-pie-publish #1` to `#7`,
`publish-readiness #1` to `#6`.

| # | Ruling |
|---|---|
| D1 | Compat publishes compatibility fixes only; pack tuning and the starter kit move to the pack folder |
| D2 | Common ships vanilla defaults; the pack's values move to the pack config |
| D3 | Delight is dropped from the initial publish list; focus on Common, Compat and Raspberry Pie |
| D4 | Deferred with Delight. Derived art: Vintage jam, jello, jar and interior textures and the core lemonade textures are palette swaps of Fruits Delight 0.5.11 (All Rights Reserved); generated PNGs are tracked in git |
| D5, D6 | Deferred with Delight |
| D7 | The repository is currently public; rewrite history (the 19 commits with a personal address) |
| D8 | Common 1.2.0 is published; 1.3.0 is the next upload |
| D9 | Keep the three added default unlocks; CurseForge now, Modrinth possibly later |
| D10 | Raspberry Pie's DR-C7 exception is permanent (recorded in design_rules.md). The other proposed exemptions are temporary and stay TODOs to fix (`publish-readiness #6`) |
| P3 | PR-CP-04: every one of the 32 ungated Compat mixins gets an enable key, default on (built 2026-11-22: `[mixins]` in creativecompat-common.toml) |
| P4 | PR-CP-17: drop the old config migrations (built 2026-11-22). DR-C11: the NBT and SavedData names (`CENightstandItems`, `CE` keys) stay; accepted exemption, since renaming them would need a conversion (recorded in design_rules.md) |
| PR-CP-13 | The weathered cardboard box texture is owner-only: no agent redo; listed in texture_sources/art_needed_2026-11-22.csv |
| P1 | PR-CC-03: Common ships the Forge global loot modifier list entry (`replace: false`) for glass shards [superseded 2026-11-24: the modifier and its list entry moved to Creative Compat, wave 2 carve, `creative-common-publish #17`]; Creative Loot also adds glass shards to the mid-tier chest tables, mostly dirty or clear, a random chance of colored (owner, 2026-11-22). Built 2026-11-22; the DR-P15 lint exempts Common's list |
| P2 | PR-CC-11: the potions of 12 vanilla effects stay in Common and are documented (project page, mods.toml description) (owner, 2026-11-22) |
| P7 | TODO `food-tuning #47`: the Wither Immunity effect is registered in Creative Common, `creativecommon:wither_immunity` (owner, 2026-11-22) |

## 3. Creative Common 1.3.0

Paths: `CC/` = `development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/`, `RES/` =
`development/creativeCommon/src/main/resources/`.

| Id | Sev | Area | Where | Finding | Ev | Fix |
|---|---|---|---|---|---|---|
| PR-CC-01 | Blocker | standalone | `RES/kubejs_startup/creativecommon/_lib/removed_items.js` (402 lines); `kubejs/data/creativecommon/kubejs/_lib/removed_item_tags.js:11-12` | The jar ships the pack's removed-item list and a server script that clears every item tag from those items on each tag load. In another pack with the same mods, Common strips their tags | C | Move the list and the strip script to the pack; Common keeps an empty `global.removedItems` and the helper. `removed_woods.js` (358 lines, no reader in Common) moves with it |
| PR-CC-02 | High | standalone, writeup | `CC/mixin/NoRecipeUnlockMixin.java:26-34` | Both recipe-award methods are canceled unconditionally: no recipe book entries, no `/recipe give`, no config key. Not on the project page | C | Config switch with the vanilla default (the pack sets it), and a page line |
| PR-CC-03 | High | standalone, rules (DR-C7) | `RES/data/creativecommon/glm_entries.json`; `CC/loot/GlassShardsModifier.java:50-51` | Glass shards are listed only in a fragment that Creative Loot merges; Common has no Forge loot modifier list, so alone the modifier never loads. The page lists it under "works without any other Creative mod" | C | Ship the Forge list entry in Common (`replace: false`), or move the feature to Creative Loot and fix the page and changelog |
| PR-CC-04 | High | standalone | `CC/CommonConfig.java:249` | `[bees] honeycombMultiplier` defaults to 2 ("the pack's setting") | C | Default 1 |
| PR-CC-05 | High | standalone, writeup | `CC/CommonConfig.java:342-348, 375-378` | `[effects] remap` defaults to three swaps and `categoryOverrides` to six entries; the page says no effect is swapped by default | C | Empty defaults, or correct the page |
| PR-CC-06 | High | writeup | `development/creativeCommon/gradle.properties:25`; `project_pages/creative_common.md:3, 51` | The description says "adds no blocks or items of its own" and names one default-on limit. The jar registers 12 composters and about 30 potions; melting, End fire rule, level caps and idle random ticks are on by default | R | Rewrite the description and the page's default list from the config spec |
| PR-CC-07 | Medium | privacy | `CC/CreativeCommon.java:217-218` | Client setup logs "HELLO FROM CLIENT SETUP" and the Minecraft account name at INFO (template leftovers) | C | Delete both lines |
| PR-CC-08 | Medium | standalone, rules (DR-C7) | `CC/config/OwnConfigLogic.java:30-38`; `OwnConfigWatcher.java:65-72` | `OWN_MOD_IDS` hard-codes upstream ids (farmersdelight, vinery, salt, exposure and others); with the real upstream mods Common takes over their config reload | R | Own jars register with the watcher, or detect them by `META-INF/ce-build.json` [fixed 2026-11-22 with the Creative Common 1.3.0 publish fixes: `OwnConfigLogic.isOwn` reads the build stamp; checked 2026-11-23] |
| PR-CC-09 | Medium | stability | `CC/util/StaleJarHandles.java:50-96` | Closes jar file systems through Unsafe on every client launch on every OS with no switch; the crash it fixes is Linux with 420 or more jars | R (risk I) | Gate on Linux and a threshold; add a JVM flag |
| PR-CC-10 | Medium | standalone | `CC/composter/WoodComposters.java:39-53` | Four composters (Architect's Palette, Quark woods) register with those mods absent; recipes are gated, so they show as uncraftable | R | Register only when the mod is loaded |
| PR-CC-11 | Medium | standalone | `RES/kubejs_startup/creativecommon/effects/potions.js:12-30` | Potions of 12 vanilla effects register under `creativecommon:`; their brewing mixes are in Creative Brewing, so alone they are creative-only | R | Move to Brewing with a swap list, or document |
| PR-CC-12 | Medium | rules (DR-C19) | `RES/credit.txt`; `NOTICE-thirdparty-various.txt`; `CREDITS.txt` | `credit.txt` ships a Numismatics LGPL notice with no traced content; the NOTICE does not mention the composter textures (Quark and Architect's Palette plank palettes); the root `CREDITS.txt` is the Forge template | R | Drop the two stale files; add a texture paragraph; owner ruling on the palettes (DR-C20) |
| PR-CC-13 | Medium | standalone | `RES/resourcepacks/loading_tips/` | All 532 upstream tips ship unfiltered; the 1.19.2 filter lives in the pack's Creative Tips override. The page says "over 300" | R (which tips are wrong: I) | Ship the remove list in the jar; fix the count |
| PR-CC-14 | Medium | performance | `CC/swap/ItemMerges.java:80, 340, 364-369` | `STAMPED` (chunk positions per dimension) only grows until server stop | R | Trim on unload; skip both maps when there are no merges |
| PR-CC-15 | Medium | performance | `CC/effect/EffectRules.java:147-172`; `MobEffectInstanceLevelCapMixin.java:36` | Every `getAmplifier()` above 0 does a registry key lookup plus a map lookup | R | Cache the cap per effect |
| PR-CC-16 | Medium | performance | `kubejs/data/creativecommon/kubejs/effects/armor_effects.js:49-71` | The player tick handler reads four armor slots and builds strings for every player every tick even with nothing registered | R | Early exit when no pieces or sets exist |

Low (all R unless marked): Rare Ice has no optional dependency row (PR-CC-17); creative_engineering.md 7.6 names the
NOTICE files without their license suffix and its 7.1 table is from before the carve (PR-CC-18); `SneakPickup.java:69-70`
clears the block entity before the block removal can fail (PR-CC-19); the mixin config has no `defaultRequire`
(PR-CC-20, I); `WorldEditSkipStateIdInitMixin` targets anonymous class `Capability$4` over a whole version range
(PR-CC-21, I); `SwapRemapper` static init throws instead of degrading (PR-CC-22); permanent hooks on `ItemStack.of` and
`Potion.byName` (PR-CC-23); `TradeSync` pre-sizes lists from server counts (PR-CC-24); `[tooltips] paletteDemo` has no
reader in the jar and `orphanTabItems` defaults are five Abnormals Delight ids (PR-CC-25); the "Caffeinated -> Phantom
Bane" tooltip row is hard-coded but applied by Delight (PR-CC-26); Common names its dependents by string (merge group,
config carry-over, recipe types) (PR-CC-27) [Delight, Brewing and Loot names fixed 2026-11-23: the `[effects]` carry-over
from Brewing's file and the treated-stick tomato idle tick rule moved to those jars; Engineering-era file names stay as
pack history]; `creativeengineering$` handler names, `[CE potions]` log tag, `ce$` fields
(PR-CC-28, DR-C11) [fixed 2026-11-23 in Common: `creativecommon$`, `[creativecommon potions]`]; history comments, `pack.mcmeta` description "creativeengineering resources", unused imports
(PR-CC-29, DR-C26); javadoc pointing at classes no longer in Common (PR-CC-30).

Writeup claims: 36 checked, 28 match, 6 mismatch (PR-CC-02 to -06, -18), 2 imprecise.

## 4. Creative Compat 1.0.0

Paths: `CP/` = `development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/`, `KJ/` =
`kubejs/data/creativecompat/kubejs/`.

| Id | Sev | Area | Where | Finding | Ev | Fix |
|---|---|---|---|---|---|---|
| PR-CP-01 | Blocker | standalone | `KJ/other/starter_kit.js:27-91` | Ships in the jar and runs for any player without its marker tag. It clears the whole inventory but saves and returns only slots 0 to 35, so worn armor and the offhand item are deleted. On an existing world outside the pack every existing player loses them on first join. No switch | C | Per D1: take it out of Compat, or default it off and keep slots 36 to 40 |
| PR-CP-02 | High | standalone | `KJ/other/starter_kit.js:19-20` | The Engineer kit uses pack-only dict keys (copper shield, pickaxe, axe) | C | With PR-CP-01 |
| PR-CP-03 | High | standalone | `KJ/other/feature_ports.js`, `misc_mods.js`, `dead_recipe_removal.js`, `economy/coin.js`, `botany_pots.js` | Pack balance ships with no toggle: recipes rebuilt from retired mods (311 AE2 cable recipes among them), recipe removals for Supplementaries, Lightman's Currency and Vintage Improvements, Botany Pots crops 8 times slower | R | Per D1 |
| PR-CP-04 | High | standalone, writeup | `development/creativeCompat/gradle.properties:9`; `CP/CreativeCompat.java:118-171` | The description says each tweak applies only when its mod is installed; 32 of 148 mixins are ungated and change vanilla (slowdown tiers, falling snow, golem cooldown, boat ice friction, Nether fog, sign text distance, toast filter, shrapnel) | R | Correct the description; add an enable key where none exists |
| PR-CP-05 | High | stability | `CP/compat/CompatMixinPlugin.java:164-175`; `mods.toml` (optional rows all `*`) | Only 11 mods have a tested version range. Mixins with `require = 1`, shadows or accessors against Corpse, TrashSlot, Distant Horizons, Xaero, GeckoLib, Quark, Supplementaries and others have none; the config is `required: true`, so another version fails the boot | R (crash I) | `MOD_RANGE` rows for every such mod, as was done for JEI |
| PR-CP-06 | High | stability | `CP/mixin/SupplementariesBreezyCompatMixin.java` | The only `@Overwrite`, written against Breezy 1.0.8 to fix Supplementaries 2.4.20, gated on presence only | R | Gate on exact versions of both |
| PR-CP-07 | High | stability | `CP/compat/CompatMixinPlugin.java:85`; `CP/cats/CatFeather.java` | `CozyHomeGoalFeatherMixin` is gated on Cozy's Improved Cats only, but its handler calls `CatFeather`, which uses a Domestication Innovation class. With Cozy and without Domestication Innovation a `NoClassDefFoundError` in a cat goal is likely | C (gate), I (crash) | Gate on both mods |
| PR-CP-08 | Medium | stability | `CP/surface/CamoSpeed.java` | Create's `CopycatBlockEntity` is tested with no Create check | R | A Create flag and holder class |
| PR-CP-09 | Medium | stability | about 20 `catch (Throwable ignored) {}` in per-tick and per-frame hooks | A persistent fault becomes a silent exception per entity per tick; save hooks can lose state unseen | R | Log once per hook |
| PR-CP-10 | Medium | consistency | `development/creativeCompat/build.gradle:25-29`; `kubejs/data/creativecompat/recipes/`, `tags/` | Only the script tree is copied into the jar. The 41 Botany Pots crop recipes and the two dimension tags the Java reads stay in the pack folder, so in a published jar the dimension rule does nothing | C | Move them into the jar's resources |
| PR-CP-11 | Medium | rules (DR-C13, DR-C7) | `CP/CompatConfig.java` `[jei]` | Four keys (`seasoningCategory`, `canCategories`, `potionBucketCategory`, `tippedArrowCategory`) are read only by Delight and Brewing | R | Move each to its owner with a carry-over [fixed 2026-11-23 (owner ruling: stays on Compat, generic): one `[jei] pagesOff` list of page ids, the old keys carried over once; creative_compat.md 1] |
| PR-CP-12 | Medium | rules (DR-C7) | `VineShearing.java:52`, `StuckInBlock.java:58`, `CompoundEffectTooltip.java:78`, `CorpseDecayMixin` | Compat names classes, lang keys and globals of jars above it (Delight, Brewing, Loot) | R | Upper jars register through a Common hook [fixed 2026-11-23: Delight, Brewing and Loot register through Compat and Common hooks, Loot's corpse names in Compat's lang; creative_compat.md 2; DR-C7 audit check `sideways`] |
| PR-CP-13 | Medium | rules (DR-C18) | `texture_sources/sources.csv:29`; `weathered_cardboard_box.png` | The shipped texture is logged as made by an ad-hoc script, status `redo`; `moss_block_tinted.png` has no row found by name | R | Redo with an owner tool; add the moss row |
| PR-CP-14 | Medium | rules (DR-C19) | `NOTICE-thirdparty-various.txt` | The NOTICE covers art only; recipe sets rebuilt from four retired mods and the Starter Kit port are uncredited | R (their licenses I) | Credit or remove (D1) |
| PR-CP-15 | Medium | performance | `CP/surface/SinkingRender.java` | Per rendered entity per frame: two block and collision-shape lookups with allocations before the `sinking` tag is tested | R | Test the tag first; cache per tick |
| PR-CP-16 | Medium | standalone | `CP/compat/guititle/TitlePlaques.java:78` | `[gui_title] logOverflow` defaults to true and writes a developer report into every install | R | Default false |
| PR-CP-17 | Medium | standalone | `CP/CreativeCompat.java:88-101` | The constructor carries sections from `creativeengineering-common.toml` and `creativecommon-common.toml`: pack history in a public 1.0.0 | R | Guard or drop the migrations that predate any public file |
| PR-CP-18 | Medium | writeup | `creative_compat.md:37-63, 123-144, 373` | Config section lists 5 tables, the spec has 17 more; tier names and neighbor modes are the old ones; "Edges to Common" predates the carve; 32 ungated mixins described as 3; namespace and lang counts stale (13 keys written, 134 present) | R | Rewrite sections 2 to 5 from the code. Done 2026-11-23: rewritten in plan_ready_now P3, checked and two counts fixed in plan_wave1_publish.md W-P2 |

Low (R): status line "nothing booted" beside a recorded run, typed install state, NOTICE file name (PR-CP-19); stale
`mods.toml` header and 19 touched mods with no optional row (PR-CP-20); `build.gradle` comment names moved classes
(PR-CP-21); 23 unused `CommonConfig` imports (PR-CP-22); `ce$` members, NBT `CENightstandItems`, 104 lang keys in other
jars' key spaces (PR-CP-23, DR-C11); 44 literal block ids in `passthrough_tags.js` (the 139 entity ids have no dict and
are legitimate) (PR-CP-24, DR-C2) [fixed 2026-11-23: 41 uses of 35 block ids -> new `block_dict.js` keys]; `PassArrows` `WeakHashMap` touched from two threads in single player (PR-CP-25);
`bamboo_leaves_full` registers without Twigs, vanilla vines not shearable without Quark (PR-CP-26); `DustParticles`
scans every client tick with an empty tag (PR-CP-27); `CompoundEffectTooltip` looks up two methods per call
(PR-CP-28); `[paintings] extraSources` reads paths outside the game folder if an admin sets them and logs the absolute
path (PR-CP-29); a global log filter, Nether Distant Horizons rules and fog color with no config key (PR-CP-30).

Writeup claims: 35 checked, 24 correct, 11 mismatched. Features in code with no mention in creative_compat.md: golem
cooldown, boat ice friction, giant squid radius, Nether fog, Distant Horizons rules, sign text distance, toast filter,
screen dim, title plaques, all JEI work, the 14 WTHIT plugins, tooltip fixes, Immersive Portals and Portal Gun, Ore
Excavation undo, shrapnel, BlockRunner surface speeds, falling layers, Simple Hats slot.

## 5. Creative Delight 1.1.0: core

Paths: `CD/` = `development/creativeDelight/src/main/java/com/SaintAlfonzo/creativedelight/`.

| Id | Sev | Area | Where | Finding | Ev | Fix |
|---|---|---|---|---|---|---|
| PR-CD-01 | Blocker | stability | `CD/CreativeDelight.java:173`; `CD/crops/HoeHarvestArea.java:50` | `HoeHarvestTooltip` is registered with no mod check and calls code that uses Quark's `ModuleLoader` and `HoeHarvestingModule`. Quark is not a dependency of Delight or of anything it requires, so the first item tooltip without Quark throws `NoClassDefFoundError` | C (link), I (crash) | Register only when Quark is loaded; add Quark as optional |
| PR-CD-02 | High | stability | `creativedelight.mixins.json`; `CD/compat/CDMixinPlugin.java:54, 78-80` | Only Fruits Delight and one Farmer's Respite mixin are version gated. Quark (6 mixins), Cooking for Blockheads (11), Respite (17), Alex's Mobs (5), Farmer's Delight (11) and others apply at any version with `require` 1 | R (crash I) | A tested range per compartment, skip with one warning outside it |
| PR-CD-03 | High | stability | `McaHarvestAnyFarmlandMixin.java:36`, `SugarGliderForageBananaMixin.java:20`, `LunchboxFoodOnlyMixin.java` | Targets are compiler-numbered lambdas and an anonymous class; they change on any upstream recompile and would fail in play | R | `require = 0` with a warning, or a range gate |
| PR-CD-04 | High | standalone | `CD/mixin/farmersrespite/CoffeeBushRemovedMixin.java:31-41` | With Farmer's Respite present every coffee bush and stem breaks itself on random tick. The only gate is a nested member that is always loaded; no switch | C (code), R (gate) | A config switch, off outside the pack |
| PR-CD-05 | High | standalone | `META-INF/mods.toml:22-90` | Mandatory: Common, Compat, Calendar (needs Serene Seasons), Loot (needs LootJS), `kubejs_create` (needs Create), `kubejsdelight`, `lootjs`. The description names three of them | C | D3 |
| PR-CD-06 | Medium | consistency (DR-C4) | `CD/cooking/RemainderBuffer.java:59`; `CD/qol/RemainderBufferQol.java:19-23` | Two switches for one feature: the oven reads `[cooking] remainderBuffer`, the pot and kettle `[qol] remainderBuffer` | R | One switch with a carry-over |
| PR-CD-07 | Medium | performance | `CD/compat/farmersrespite/KettleDrinks.java:25-28, 84-104` | One static cache keyed by the last recipe manager; in single player client and server alternate, each flip rescanning all recipes, and the maps are read outside the lock | R (frequency I) | Cache per manager, publish through one volatile holder |
| PR-CD-08 | Medium | stability (item loss) | `CD/cooking/CookwareContents.java:57-83` | "Keep contents" removes the inventory from the drops and trusts the loot table to produce the item; with no drop (gamerule, replaced table) the contents are gone | R (paths I) | Decide at the point the item is produced |
| PR-CD-09 | Medium | standalone | `AttachedStemExtraFruitMixin.java:39-62`, `KettleBlockMixin.java:38-43`, `CfbOvenLitMixin.java` | Block state definitions of vanilla and third-party blocks change with no config escape | R | State on the page as a both-sides requirement |
| PR-CD-10 | Medium | stability | `CD/compat/CornDelightCompat.java:86-118` | `sun.misc.Unsafe` rewrites two static final villager fields | R | Access transformer or a mutable accessor mixin |
| PR-CD-11 | Medium | writeup | `creative_delight.md:633-635, 816-818, 1301, 1930-1990` | 11 of 33 claims stale: mandatory list, classes described as Common's, renamed classes (Seasoning -> Flavor), `creativeengineering` namespace and lang path, a retired id | R | Refresh sections 12, 14.2, 22.1, 34 |
| PR-CD-12 | Medium | writeup (DR-W2) | `creative_delight.md` | Not the standard layout; no current file map for the core jar; two "## 30." headings | R | Add a file map section; renumber |
| PR-CD-13 | Medium | repo | `development/creativeDelight/moved_code_backup/` | 132 tracked files of pre-move copies, two built jars among them. Not in the jar | C | Remove before a public source release |

Low (R): a `[members] displaydelight` switch asked before the config loads, so it always answers true (PR-CD-14);
`LAST_PUNCH` map never cleared (PR-CD-15); cookware pickup can lose contents when the block removal fails (PR-CD-16);
corner storage drops slots 27 to 53 when Cooking for Blockheads' large counters are turned off (PR-CD-17); a bucket
poured into a partly filled kettle voids the rest (PR-CD-18); the oven client menu throws when the block entity is
missing (PR-CD-19); a silent `catch (Throwable)` on the eat path (PR-CD-20); per-frame allocations in
`FlavoredItemModel` and the cookware glint hook, per-tick allocations in the oven and remainder buffer (PR-CD-21);
`WaterOnSoil` has no switch and no interact check (PR-CD-22); 15 lang keys and about 30 mixin handlers still
`creativeengineering`, SavedData and NBT names with `CE` (PR-CD-23, DR-C9, DR-C11); stale `mods.toml` comments
(PR-CD-24); rack textures recolored at run time from Cooking for Blockheads art, already TODO `licence/cd #1` (PR-CD-25).

Writeup claims: 33 checked, 22 correct, 11 mismatched.

## 6. Creative Delight 1.1.0: members and distribution

| Id | Sev | Area | Where | Finding | Ev | Fix |
|---|---|---|---|---|---|---|
| PR-CM-01 | High | license | `members/vintage/tools/gen_jam_art.py:5-11`; `texture_sources/sources.csv:31-36`; `members/vintage/NOTICE-upstream-MIT.txt` | 174 Vintage textures (jam jars, jam and jello blocks) are palette swaps of Fruits Delight art, tracked in git and shipped in the Vintage member. The generator's own header calls Fruits Delight All Rights Reserved; the 0.5.11 jar's `mods.toml` says "All rights reserved"; the NOTICE does not mention it. The core lemonade textures have the same source (`sources.csv:7`) | C | D4 |
| PR-CM-02 | High | standalone | `members/cannedgoods/.../CannedGolemDye.java:31`; `kubejs/assets/creativedelight/textures/entity/canned_golem/` | The 15 dyed golem textures and one lunchbox model live only in the pack folder; no jar has them | C | Move into Delight's resources |
| PR-CM-03 | High | standalone | `CD/CreativeDelight.java:241-254` | A loose original jar beside the bundle wins over the nested member; the overlays are then absent and the only reaction is one log line | C (code), I (effect) | D6 |
| PR-CM-04 | High | distribution | `development/shared/upstream/ce/shared/DonorJars.java:41-59` | Six members throw at load unless seven pinned donor jars sit in six folders; the `[members]` switch does not skip the check; the planned auto-filing (`creativedelight/framework #9`) is not built | C (throw), R | D5; put the donor table on the page |
| PR-CM-05 | Medium | build | `build-logic/.../ce.forge-repack.gradle:31, 106`; `.gitignore` | A clean clone cannot build Delight: 9 repack jars and 2 donors in `upstream/`, `libs/` jars, three `devmods/` jars; no list of sources for the build-time jars | R | A generated table (file, sha1, source) or a fetch script |
| PR-CM-06 | Medium | rules (DR-P15) | `members/ends/src/main/resources/assets/endersdelight/lang/en_us.json` | Same asset path as the installed Ender's Delight jar; one of the two lang files is lost | R (effect I) | Move the 11 keys under the member's own namespace |
| PR-CM-07 | Medium | license | `members/hearth/NOTICE-upstream-MIT.txt:7-14` | The NOTICE and description list only the raspberry bush and watering can; the member now tracks 252 textures, 299 models and 86 data files from upstream | R | Rewrite both |
| PR-CM-08 | Medium | standalone | `kubejs/assets/{farmersdelight,vintagedelight,salt,canned_goods,displaydelight}/`, `kubejs/data/{salt,miners_delight}/` | 71 asset and 4 data files under member namespaces live only in the pack folder | R | Move the non-ARR ones into the member overlays; the Ender's retextures stay out of any jar |
| PR-CM-09 | Medium | rules (DR-C18) | `texture_sources/ledger.csv` | In member trees: 23 `confirm`, 3 `redo`, three `logo.png` with no row | R | Owner look-check; redo the 3; add the rows |
| PR-CM-10 | Medium | metadata | nested `mods.toml` of display, veggies, delightful, farmers, nethers, oceans, salt | `issueTrackerURL` still points at the upstream authors although the jars are modified | R | Point at the pack's tracker; keep upstream in credits |
| PR-CM-11 | Medium | rules (DR-C14) | nested repacks | 405 upstream advancement files ship unchanged (365 recipe unlocks) | R (effect I) | Confirm the drop works standalone, or exclude them in the repack |
| PR-CM-12 | Medium | license | `members/createsalt/`, `members/cannedgoods/`, `members/oceans/` NOTICE files | License evidenced only by the `mods.toml` string (no repository, no LICENSE file) | R | Author confirmation as for Rustic, or accept in writing |
| PR-CM-13 | Medium | writeup | `creative_delight.md:3-8, 106, 2306, 2314` | Typed install state names `creativedelight-1.0.0.jar` with 22 members (is 1.1.0, 20); the Rustic license row says "unconfirmed" though the author confirmed on 2026-11-10; the Ender's donor row names the wrong version | R | Replace with the generated block; fix the rows |

Low (R): own mod id `enders_additions` has an underscore (PR-CM-14, DR-C10) [fixed 2026-11-23: mod id `endersadditions`; config file and `[members]` switch carried over, item and block ids were always `endersdelight:`]; `tools/.nested_jars/*.jar` tracked
(PR-CM-15); five wordings of `mod_license` and two LICENSE file names across members (PR-CM-16); Barbeques imports
Delight classes but declares only Common (PR-CM-17); stale build numbers in display_delight.md, rustic_delight.md,
miners_delight.md and counts in texture_sources.md (PR-CM-18); a horseshoe modifier re-added every tick per horse
(PR-CM-19).

Per-member verdict: publishable as is: farmers, oceans, oceanic, nethers, rustic, delightful, roll, largemeals,
barbeques, veggies, fungi, miners, display, salt, createsalt, golden. Needs a fix first: ends (PR-CM-06), hearth
(PR-CM-07), vintage (PR-CM-01), cannedgoods (PR-CM-02). The five donor members ship classes only; no ARR file is
tracked or in the bundle.

## 7. Raspberry Pie 1.0.0

Paths: `RP/` = `development/solRaspberryPie/src/main/java/com/SaintAlfonzo/solraspberrypie/`.

Phase state against [plan_rp_standalone.md](plan_rp_standalone.md):

| Phase | TODO row, status word | Real state |
|---|---|---|
| P0 license check | `food-tuning #37` Complete | Done (recorded) |
| P1 bundle | `#38` Planned | Built; open: NOTICE file name, no fetch step for `libs/` |
| P2 default ladder | `#39` Planned | Built (47 rungs, 4 slots, 11 pool effects) |
| P3 table loader | `#40` Planned | Built; no table in the jar |
| P4 names and recipe | `#41` Planned | Built |
| P5 standalone defaults | `#42` Planned | Built |
| P6 Fleet-Footed guard | `#43` Planned | Built (moved to Creative Compat 2026-11-24, `raspberry-pie-publish #25`) |
| P7 pack ladder | `#44` Planned | Built (81 rungs, 5 slots, 37 pool effects) |
| P8 pack table | `#45` Planned | Built (1,658 values in Creative Delight) |
| P9 tests | `#46` Planned | Not started: every `RPS` row is "Not run" |
| Satiated exception | `#48` Planned | Built |
| `#47`, `#49` to `#58` | Planned | Not started, matches the code |

| Id | Sev | Area | Where | Finding | Ev | Fix |
|---|---|---|---|---|---|---|
| PR-RP-01 | Blocker | tests | `testing/rows/RPS.qb` | No isolation row has run: "boots with Forge and this jar only", the loose-jar duplicate case and the absent-mod paths are unverified | R | Run SQ-RPS-01, -04, -06, -08 and CQ-RPS-02, -03, -05 |
| PR-RP-02 | Blocker | build | `.gitignore:77`; `gradle.properties:25-26` | `libs/` is git-ignored and the bundle is pinned to the Apple Pie and Carrot jars in it; a clone cannot build the jar | C | A sha1-checked fetch task or script, and build steps |
| PR-RP-03 | High | status | `todo/rows/food_rules.csv` (`food-tuning #38` to `#45`, `#48`) | Nine built rows say Planned; the plan says "Nothing in this plan is built" | C | `tools/todo.py set ... status=Testing`; fix plan line 32 |
| PR-RP-04 | High | stability | `RP/logic/Diversity.java:124-168` | `use()` has no error handling. A table that is valid JSON but the wrong shape throws on load; the review reports the table then stays unset and every later lookup throws. Any mod's jar can ship a table. Values are not checked for NaN or negatives | C (no handling), R (follow-on) | Parse each source in a try, skip the bad file with one error line, always assign the table |
| PR-RP-05 | High | stability | `RP/client/DietLogPages.java:779-815`; `FoodBookScreenMixin.java:120` | A "TEMPORARY hover debug" class is still called from the Diet Log screen and logs while the mouse moves | C | Delete before release |
| PR-RP-06 | High | release | `mods/solRaspberryPie-1.0.0.jar` | Stamp `1.0.0+c2d33f73.dirty-940bce`: built from an uncommitted tree, installed with checks skipped, older than the last source commit | R | Release jar from a clean commit with checks on |
| PR-RP-07 | Medium | license (DR-C19) | `development/solRaspberryPie/NOTICE` | Not in the `NOTICE-<scope>-<licence>.txt` form; ships with no `.txt`; mixes own copyright with third-party notices | C (name) | Rename; update the credits text |
| PR-RP-08 | Medium | license | `NOTICE:17-18, 25-26` | The LGPL source offer is a link to third-party repos; Carrot has no tag | R | Mirror both sources at the pinned commits or attach source zips |
| PR-RP-09 | Medium | stability | `GuiMixin.java:16`; `EffectRenderingInventoryScreenMixin.java:16` | `@Redirect` on vanilla `getActiveEffects()` with `defaultRequire` 1; another mod redirecting the same call fails the boot | R (conflict I) | `require = 0` or a list filter |
| PR-RP-10 | Medium | stability | `RP/logic/ServerEvents.java:44, 163-179` | At login every attribute modifier whose name matches a broad pattern is removed, on every attribute; other mods' modifiers of that shape would go too | R | Match only own names, or run once behind a saved flag |
| PR-RP-11 | Medium | consistency | `default_ladder.json` | Four default unlocks can never be used (Speed I, Haste I and II, Strength I): a flat rung of the same effect is active before the first slot | R | Swap them or move the flats; warn on "unlock always hidden" |
| PR-RP-12 | Medium | standalone | `data/solraspberrypie/tags/items/diet_log_foods.json` | Outside the pack the Diet Log recipe takes only 17 listed vanilla foods; Apple Pie's own recipe is removed | R | Match any edible item not excluded [fixed 2026-11-23: ingredient `solraspberrypie:edible`, raspberry_pie.md 12.15] |
| PR-RP-13 | Medium | performance | `RP/net/S2CState.java:61`; `RP/logic/SlotActions.java:230-250` | Every state sync recomputes drop counts (about 8,000 lookups at pack settings) and resends up to 256 drink ids; syncs fire on every food and twice per slot action | R | Compute on demand or cache per stamp [fixed 2026-11-23: counts cached per window, one sync per tick, drink list only when changed; raspberry_pie.md 12.15] |
| PR-RP-14 | Medium | writeup | `raspberry_pie.md:3, 190-203` | Typed install state names an older jar; section 4 describes the pack pool as the default and lists Fire Resistance as not slottable | R | Pointer to the generated status; rewrite section 4 for the two ladders |
| PR-RP-15 | Medium | standalone | `RP/RpConfig.java:82-84, 111-115` | Client config defaults name pack mods' items | R | Vanilla defaults in the jar, pack values in `defaultconfigs` |

Low (R): optional `#creativedelight:` entries in the jar's tags (PR-RP-16); tables are read separately on client and
server with no sync (PR-RP-17) [fixed 2026-11-23: the server's table is sent to clients, raspberry_pie.md 12.15]; a new jar default ladder never reaches existing worlds (PR-RP-18) [built 2026-11-23: unedited copies auto-update, `/solrp ladder reset` for edited ones]; `refreshInterval`
may exceed `effectDuration` (PR-RP-19); a class lock on every complexity lookup, per-frame allocations in three client
classes (PR-RP-20); `RpData.get` returns a throwaway object when the capability is missing (PR-RP-21); INFO lines with
player names and the absolute game path (PR-RP-22) [fixed 2026-11-23]; `itemIcon` is not a Forge key, no optional rows for AppleSkin and
Alex's Mobs (PR-RP-23); key defaults chosen for the pack (PR-RP-24); history comments, SPDX header on 5 of 63 files
(PR-RP-25, DR-C26); four stale writeup lines and three stale plan lines, `RPI.qb` named for `RP.qb` (PR-RP-26). Rows: `raspberry-pie-publish #11` (18, 22, built), `#12` to `#20` (16, 17, 19, 20, 21, 23 to 26; owner 2026-11-23).

Checked with no finding: server validation of every client action (a client cannot grant itself an effect or level it
has not unlocked), `/solrp` at permission 2, no advancements, the Liquid Diet Log texture row in `sources.csv`, lang
path, the ladder format as parsed, documented and generated.

Writeup claims: about 40 checked, 7 mismatches in the writeup, 3 in the plan.

## 8. Repository, privacy, build and release records

Values are masked on purpose; file paths are enough to find them.

| Id | Sev | Area | Where | Finding | Ev | Fix |
|---|---|---|---|---|---|---|
| PR-RE-01 | Blocker | metadata | `agent_notes/git-repo-setup.md:7`; `mods.toml` of Common and Raspberry Pie; `solRaspberryPie/NOTICE:3` | The only recorded repo state is "private" (2026-09-27). Both mods link to it and Raspberry Pie names it as the GPL source location | R (visibility not checked) | D7; record visibility in one doc. Done 2026-11-22: public, recorded in `agent_notes/git-repo-setup.md` |
| PR-RE-02 | High | privacy | git history | 19 commits carry a personal mail address as author; the other 604 use noreply addresses | C | D7 |
| PR-RE-03 | High | privacy | `agent_notes/git-repo-setup.md:7` | Names the owner's separate global git identity | R | Drop the clause |
| PR-RE-04 | High | privacy | `config/jei/world/` (14 tracked files); `configs_edited/config_26_09_26.zip` | Bookmark folders named after servers and worlds joined; one folder name carries two first names | C (count), R | Untrack and ignore the folder; rebuild or drop the zip |
| PR-RE-05 | High | privacy | `.claude/settings.json` (6 lines); about 50 more lines in `tools/scratch_query/batches/`, queue rows, run docs, three tools | Absolute home paths and the Unix user name in tracked text. None in the four mods' trees or jars | C (settings), R | Relative or `Path.home()` values; scrub pasted listings |
| PR-RE-06 | High | build | each mod's `build.gradle`; `README.md:121-123` | A clean clone can build none of the four: Common compiles against 91 untracked jars in its `libs/`, with no list of names, versions or sources | R | A tracked manifest per mod (file, version, sha1, source) and a fetch script |
| PR-RE-07 | Medium | privacy | `development/creativeCommon/changelog.txt` | The Forge template changelog with 12 third-party contributor mail addresses | C (tracked) | Delete |
| PR-RE-08 | Medium | privacy | `communications/` (one image); `server/perf/reports/` (three zips) | A third party's permission reply as a screenshot; reports with hardware details and the home path | C (tracked) | Keep permission evidence out of the public repo and cite it in the NOTICE; untrack the reports |
| PR-RE-09 | Medium | build | `gradle.properties:8`; `build-logic/build.gradle:8-9` | A Linux-only `org.gradle.java.home`; dynamic plugin versions; no wrapper checksum | C (path), R | Remove the line; pin the versions |
| PR-RE-10 | High | records | `creative_engineering.md:486`; `plan_cc_publish_1_3.md:25, 39`; `creative_common_changelog_1_3_0.md:3` | Upload state of Common is recorded nowhere; the plan's cut-off is 1.2.0 and the changelog says it covers everything since 1.1.0 | R | D8; a release ledger (version, tag, sha1, date, site); started 2026-11-22 as [release_ledger.csv](release_ledger.csv) |
| PR-RE-11 | Medium | metadata | `mods.toml` of Compat and Delight | No `displayURL` or `issueTrackerURL`; no `logoFile` in any of the four; both descriptions describe the pack, and Compat's "every other Creative mod requires it" is false | R | Add; rewrite for a standalone reader |
| PR-RE-12 | Medium | docs | `project_pages/` | Project page and file changelog exist for Common only; no README in any of the four mod folders | R | Three pages, three first changelogs, a README each |
| PR-RE-13 | Medium | records | `todo/rows/creative_mods.csv` (`creativecommon/publish #1` to `#5`, `creativecompat/publish #1`, `licence/compat #1` to `#3`) | Rows in Testing since 2026-11-05 to 2026-11-16 with stale text (a tag listed as open that exists; "NOT installed" beside an install of 2026-11-22). No publish family for Delight or Raspberry Pie | R | Refresh or close; add the families |
| PR-RE-14 | Medium | records | `jar_archive/installed.csv` | `creativecommon-1.3.0.jar` was built and installed with four stamps on 2026-11-22 under one file name; Compat has been 1.0.0 since the carve | R | Upload only the jar whose stamp equals the tag commit; record its sha1 |
| PR-RE-15 | Medium | repo | `development/creativeCommon/working/` | Two PneumaticCraft source copies and a scratch zip tracked in Common's root | C (tracked) | Delete or move out |
| PR-RE-16 | Medium | records | `pack/MODLIST.md:137`; `pack/manifest.json`; `status/features.csv:30, 38, 145` | The mod list names Common 1.1.0; feature notes carry typed versions and install words | R | Next deploy; strip the notes |

Low (R): `pack.mcmeta` and the hard-coded Minecraft range in Common (PR-RE-17); root LICENSE and the mod copies differ
in four lines (PR-RE-18); a leftover `maven-publish` block and second wrapper in Common (PR-RE-19); "Licence" in own
prose and "GNU GPL v3" for GPL-3.0-only on the page (PR-RE-20, DR-L7); 632 tracked client config files, 120 `.bak`
among them (PR-RE-21).

Clean: no token, key, password or real server address in tracked files; no account cache, crash report or save tracked
or ever added; no personal data, source file or dev leftover in the four jars or their 22 nested jars; no
`updateJSONURL` anywhere; the complete runtime grep (HTTP, sockets, environment, process, desktop) has no hit beyond
two JVM feature flags, server-log lines naming a player, and click-to-open links in the changelog viewer.

## 9. Design rules

Lints clean for the four trees: namespace, id owner, dict, spelling, ISO dates, tab indent, license fields, trades.
Judged hits and proposed exemptions (D10):

| Rule | Hit | Judgment |
|---|---|---|
| DR-C2 | `neoCraftHelper.js` 74 literals | Legitimate: recipe type ids and NBT keys have no dict [fixed 2026-11-23: the item, tag and fluid ids (air, empty fluid, stick, `AXE`) use keys; 66 left are recipe types, ingredient types and the potion NBT, no dict] |
| DR-C2 | generated `can_types.js` files (196, 58, 55) | Generator output; propose exempting generated files as DR-C21 does [fixed 2026-11-23: `gen_can_types.py` writes `global.effect` keys for all 355 effect ids (new keys `phasing`, `rage_aura`); the texture paths left have no dict] |
| DR-C2 | `passthrough_tags.js` 198 | 139 entity ids legitimate (no dict); 44 block ids are a real, small break (PR-CP-24) [fixed 2026-11-23: block ids use `block_dict.js` keys; 157 left are the entity ids, tag names and patterns] |
| DR-C2 | `salt/create_salting.js` 130 | A lookup table of recipe ids; legitimate as data [fixed 2026-11-23: the 65 food ids use item keys; the 65 recipe ids stay as data] |
| DR-C1 | 21 raw Create calls in two Delight scripts | Step lists passed to the sequenced-assembly helper; legitimate under the current helper |
| DR-C10 | five underscored mixin packages in Delight | Functional: the plugin reads the package segment as the mod id. Propose an exemption [fixed 2026-11-23: packages without underscores, `CDMixinPlugin` maps the five segments to their mod ids] |
| DR-C10 | six underscored member ids | Upstream ids kept by ports; `enders_additions` is the one own id (PR-CM-14) [fixed 2026-11-23: renamed `endersadditions`; the upstream member ids stay] |
| DR-C7 | Common and Compat naming higher jars by string | Real (PR-CC-08, -27, PR-CP-11, -12) [fixed 2026-11-23: `design_rules_audit.py sideways` reports 0]; Raspberry Pie having no Common dependency matches its plan and needs one exemption line |
| DR-C11 | `creativeengineering$` and `ce$` names in Common, Compat, Delight | Real, low (PR-CC-28, PR-CP-23, PR-CD-23); NBT and SavedData names: accepted exemption (owner, 2026-11-22), they keep their names since renaming needs a conversion; member prefixes stay a TODO |
| DR-C14 | 405 upstream advancement files in repacks | PR-CM-11 |
| DR-C18 | one ad-hoc texture in Compat, 26 unconfirmed or redo in members | PR-CP-13, PR-CM-09 |
| DR-C19 | Raspberry Pie NOTICE name; stale notices in Common, Compat, Hearth | PR-RP-07, PR-CC-12, PR-CP-14, PR-CM-07 |
| DR-C26 | one dated comment (fails the lint), history comments in Common and Raspberry Pie | PR-CC-29, PR-RP-25 |
| DR-P15 | Ender's lang path; the Oceans loot modifier list | PR-CM-06; the list is server data with `replace: false`, not an asset: no change |

Lint gap worth closing: neither `optional_ref_audit.py` nor `guard_check.py` sees Java type references to optional
mods, which is how PR-CD-01, PR-CP-07 and PR-CP-08 pass. A class-reference pass (third-party imports outside gated
packages reached only through a loaded check) would have caught all three.

## 10. Suggested order

1. Owner rulings D1 to D10; then TODO rows per finding id through `tools/todo.py`.
2. Crash and data-loss fixes that do not depend on a ruling: PR-CD-01, PR-RP-04, PR-RP-05, PR-CP-07, PR-CC-07,
   PR-CM-02, PR-CP-10; status rows PR-RP-03.
3. Repository cleanup (PR-RE-03 to -09, -15, PR-CD-13), then D7.
4. Common: PR-CC-01 to -06 (it is first in the upload order and everything depends on it).
5. Raspberry Pie: PR-RP-02, -06, -07, then the isolation rows (PR-RP-01).
6. Compat per D1; version ranges PR-CP-05, PR-CD-02, PR-CD-03.
7. Delight per D3 to D6; member fixes PR-CM-06 to -08.
8. Writeups, project pages and changelogs (PR-CP-18, PR-CD-11, -12, PR-CM-13, PR-RP-14, PR-RE-11, -12).
9. Clean-clone build manifests (PR-RE-06, PR-CM-05), release jars from tagged clean commits, release ledger.

## 11. Not covered

- Nothing was built or booted; every "crash" marked I is reasoned from the code.
- Mixin targets against the third-party jars; about 95 gated Compat mixins beyond target and gate; Compat's JEI code
  (4.7k lines), `itemphysic/`, `portals/`, `portalgun/`, `shrapnel/`; Delight's JEI classes and block classes; member
  Java beyond tick handlers and mixin configs; the dict contents and helper bodies in Common.
- Whether the GitHub repo or the CurseForge project is public; upstream license terms at their source sites; file
  contents in git history (names and metadata only).
- The other own jars a publish wave would pull in (Calendar, Loot), beyond their dependency ranges.

## 12. History

- 2026-11-22: owner rulings D1 to D10 recorded in section 2a; TODO rows created.
- 2026-11-22: analysis written (owner request). Six parallel read-only reviews plus the tooling baseline; blocker and
  high findings marked C were re-read in the code afterwards.
- 2026-11-23: wave 1 execution plan and rulings W1 to W12 (composter palettes, cardboard box, tips, clean-clone build, Raspberry Pie rows): [plan_wave1_publish.md](plan_wave1_publish.md).
- 2026-11-23: wave 2 plan: gameplay moves out of Common to Compat, Delight and Loot, per-feature page layout, Raspberry Pie fixes (rulings W2-1 to W2-9): [plan_wave2_publish.md](plan_wave2_publish.md).
