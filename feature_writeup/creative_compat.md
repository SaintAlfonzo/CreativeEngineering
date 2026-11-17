# Creative Compat

Written 2026-11-01 (Creative Split wave 3, [creative_split.md](creative_split.md) 7.6; TODO `creativesplit/compat #5`).
Status: **carved 2026-11-01, installed; scratch SQ-CMP queued, nothing booted.**

Written for: the owner and agents working on the split. Own mod `creativecompat` (development/creativeCompat,
`creativecompat-1.0.0.jar`, side both): the pack's tweaks to third-party mods that have no wider system of their own.
Every other Creative jar requires it (Common <- Compat <- content jars). Same behavior as before the carve.

## 0. At a glance

<!-- generated:status_index begin open_work sha1=4d8fa8602f82 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 5 open TODO rows (Testing 5), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `creativecompat/publish #1` | Testing | C S | Compat crashes at load without the Tempad upstream jar |
| `creativesplit/compat #5` | Testing | S C | Carve Creative Compat |
| `licence/compat #1` | Testing | C | Paintings out of the Compat jar |
| `licence/compat #2` | Testing | C | Banner-derived textures and MineColonies leftovers |
| `licence/compat #3` | Testing | C | Compat: cardboard notice, moss texture, TrashSlot overwrite |
<!-- generated:status_index end open_work -->

## 1. What it holds

- **Java** (`com.SaintAlfonzo.creativecompat`, same subpackages as in patcherMod): `compat/` (banner pattern atlas,
  botanist pot, Polymorph in Sophisticated / vanilla crafting, Supplementaries jar mixing, jar fluid pipes (JarFluidPipes, 2026-11-02), Handcrafted nightstands,
  plant box models, Quark safe sort, sack bundle and scroll, spout gun farmland, Supplementaries flower box, TrashSlot),
  `banners/` (Bedspreads and Customizable Elytra pattern textures built at load, 6.3), `itemphysic/` (split, burn transform, burn blast damage, Allurement curses),
  `block/` (MossTint, VineShearing), `mobs/MonsterBoxVault`, and the JEI world rows `compat/jei/BurnRows`. The campfire
  code (`campfire/`, `CampfireRainMixin`, `CampfireRows`, `[campfire]`) moved to Creative Delight on 2026-11-04
  (creative_split.md 7.9 step 1).
  Since 2026-11-01 also `cats/` (CatFeather, FeatherAttackGoal, CozyCats: Domestication Innovation's Feather on a
  Stick and Cozy's Improved Cats, [cozy_cats.md](cozy_cats.md) 10) with 5 more gated common mixins (DiFeatherEntity,
  FishingHookFeatherPull, CatCozyCommand, CozyHomeGoalFeather, CozyMessageGender) and a new `[cats]` config table
  (not carried from the old file).
- **Mixins:** `creativecompat.mixins.json`, 32 common + 14 client (2026-11-07: `XaeroAtlasColourMixin`, gated on `xaeroworldmap`, perf.md 18; 2026-11-04: the two client additions are `JetpackItemTooltipMixin`, gated on
  `create_dd`, and the ungated `FallingBlockWaterSplashMixin`, both from Common, member prefix `creativecompat$`), SRG names, `remap = false`, member prefixes still
  `creativeengineering$`. `compat/CompatMixinPlugin` (a `GatedMixinPlugin`) applies each only when its mod is loaded:
  an any-of table plus an all-of table (`NEEDS_ALL`) for the Polymorph pairs (with sophisticatedcore, IE, Pretty Pipes),
  Breezy with Supplementaries, and the Create breaker with Quark. Ungated vanilla targets: own-stack fill, botanist pot
  template, vine shear.
- **Config:** `config/creativecompat-common.toml` (`CompatConfig`): `[leaf_piles]` (until 2026-11-15, see below), `[moss]`, `[vine_shearing]`,
  `[monster_box]`, `[trash_slot]`, keys, comments and defaults unchanged. On the first boot after the
  carve, Common's `ConfigRename.carrySections` copies those tables with their values from
  `creativeengineering-common.toml` into the new file (only when the new file does not exist yet); patcherMod's own
  config correction then drops them from its file. If nothing could be carried, Compat warns about the leftover
  section headers (Calendar's pattern).
- **Leaf piles (2026-11-15, pass-through phase 3, [plan_passthrough.md](plan_passthrough.md) 3.1):** `LeafPileSlowMixin`
  and `surface/LeafPileSlow` are gone; IW leaf piles and Twigs bamboo leaves are the `leaf_pile` pass-through tier,
  threshold `[surface.passthrough.leaf_pile] minLayers` (default 4). `surface/LeafPileMigration` moves an old
  `[leaf_piles] slowFromLayers` value there on boot (the old value wins unless it is the default 4 and `minLayers`
  exists) and drops the section. IW's `leaf_piles_slow` no longer sets the slowdown (the tier cancels IW's
  `makeStuckInBlock`), but it still gates IW's spruce thorn damage, which sits in the same branch: thorns need
  `leaf_piles_slow = true` and, with the mixin gone, IW's own 4+ layers (6+ for damage), whatever `minLayers` says.
- **Pass-through config (2026-11-15, [plan_passthrough.md](plan_passthrough.md), feature doc [blockrunner.md](blockrunner.md) 3.10):**

  | Table | Keys (default) | Side |
  |---|---|---|
  | `[surface.passthrough]` | `enabled` (true; false = every block back to its own slowdown), `arrowLifeTicks` (12000), `arrowLongLifeCap` (128) | common; the server's values are sent to clients on join and save |
  | `[surface.passthrough.<tier>]`, one per tier: `dense_crop`, `crop`, `wild_crop`, `vine`, `tall_plant`, `cane`, `rope`, `cobweb`, `bush`, `leaf_pile`, `grass` | `walk`, `fall`, `growth` (ANY / HALF / TALL_HALF), `neighbours` (NONE / RING / CUBE), `denseFrom`, `denseWalk`, `denseFall`, `denseResetsFall`, `surrounded` (NONE / WEB), `minLayers` (1-9, 9 = never), `walkOnlyMoving`, `items` (NONE / STICK), `itemStickChance`, `projectiles`, `depthStriderPasses`; defaults per tier in blockrunner.md 3.10 | common, synced |
  | `[surface.wthit] passThrough` | true: Walk Through / Fall Through / Neighbors lines | client |
  | `[debug.passthrough] timing` | false; true logs a `[passthrough-timing]` line per side every 200 ticks (`tools/perf/passthrough_timing.py` reads them) | both, applies on save |

- **Resources in the jar** (paths and ids keep the `creativeengineering` namespace): moss tinted models and texture,
  flower box models, `tags/items/physics/`, 13
  lang keys (MCA eyes, monster box), no banner pattern textures (since 2026-11-06 the Bedspreads and Customizable
  Elytra set is built at load from the installed jars, `banners/BannerPatternPack`, 6.3), no painting art (since 2026-11-05 the Immersive Paintings set
  is built at load from the installed jars, `paintings/PaintingPack`, 6.2), the monster box reward loot table, the weathered cardboard box assets.
- **Scripts:** startup `kubejs_startup/creativecompat/` (`item_physics.js`, `decor_blocks.js` weathered cardboard
  box); server `kubejs/data/creativecompat/kubejs/` (coin, dead recipe removal, feature ports, flowers, misc mods,
  starter kit, physics tags, and the Compat rows of curios, feature-port, block and misc tags); client
  `kubejs/assets/creativecompat/kubejs/` (flower pot names, the Misc Mods, Quark, Supplementaries, Framed / Copycat and
  flower pot tooltips). Each tree opens with `_compat_core.js`. The Compat tooltips call the pack's tooltip helpers
  through `global.tooltips` (published by `client_scripts/tooltips.js`), at event time.

## 2. Edges to Common (Common never imports Compat)

| Common reader | Hook Compat fills |
|---|---|
| JEI world tabs (`WorldInteractionJEI`) | `WorldRows.provider(BURNING, ...)` from Compat's constructor when JEI is loaded (CAMPFIRE: Creative Delight since 2026-11-04) |
| `WthitCampfire` | filled by Creative Delight since 2026-11-04 (`campfire/CampfireRainInfo` suppliers); `CampfireRainTicks` accessor stays Common |
| `WthitHolderContents` | `compat/JarContents` kinds / total hooks (fallback: per-slot list) |
| `WthitPotContents` | `compat/PottedPlantHolder`, implemented by the botanist pot block entity |
| `surface/StuckInBlock` (Engineering) | `surface/LeafPileSlow` live reader (default 4); gone 2026-11-15 with the leaf pile mixin (pass-through phase 3) |
| Compat `ItemBurnTransform` -> shrapnel (Engineering) | `util/BurnBursts`, registered by the shrapnel side |

## 3. Left in the pack folder or elsewhere on purpose

- Third-party asset overrides `kubejs/assets/{beautify,supplementaries,lightmanscurrency,wares,prettypipes}`: a mod jar
  cannot reliably sort above the target mod's own jar; they stay in the KubeJS resource pack until a top-position
  built-in pack is set up.
- TrinketCaseRules, its 4 mixins and `[botania]` (Creative Magic, wave 6); salt (Creative Delight, wave 4);
  `layered_obsidian` (Engineering); Botania tooltips; the Vinery flower box tooltip (Creative Brewing).
- Open question: the archery arrow tooltip list moved with Misc Mods; it may belong to Equipment.

## 4. Tests

Scratch SQ-CMP-01 to -08 ([scratch_queue.md](testing/scratch_queue.md)). In game: CMP-01 open a Supplementaries jar
with two potions and read the WTHIT line; CMP-02 (campfire) moved to SQ-CFR / CQ-CFR-01 with the code; CMP-03 TrashSlot
and Polymorph in a Sophisticated crafting upgrade; CMP-04 MCA comb eye colors and a Bedspreads / Elytra banner.

## 5. History

- 2026-11-01: carved (wave 3). patcherMod 1.1.06 loses the classes, mixins, sections and resources; Calendar,
  Devtools, Creative Delight and Creative Brewing require `creativecompat`. Added ModList gates that were missing for
  the spout gun farmland, nightstands, sack scroll, banner atlas and monster box listeners.
- 2026-11-04: campfire rain, Comfort, the Campfire Rules JEI rows and `[campfire]` moved to Creative Delight
  (creative_split.md 7.9 step 1); Compat 33 -> 32 common mixins. Tests SQ-CFR.
- 2026-11-04 (creative_split.md 7.14): from Creative Common, the Create: Dreams & Desires jetpack tooltip null-level
  guard (`JetpackItemTooltipMixin`) and the falling-block water splash (`FallingBlockWaterSplashMixin`): third-party
  and vanilla behavior fixes, not interface or perf. Common keeps its interface, API and engine/perf rows (2.4).
- 2026-11-14 to 2026-11-15: pass-through blocks (blockrunner.md 3.10): `surface/Pass*`, 13 new mixins (`WallSlideMixin`
  renamed `InsideBlocksMixin`), `[surface.passthrough]`, `[surface.wthit] passThrough`, `[debug.passthrough]`; the
  tier tags `kubejs/data/creativecompat/kubejs/tags/passthrough_tags.js` ship in the jar from the 2026-11-15 00:40
  build; `[leaf_piles]` and `LeafPileSlowMixin` removed (migration above). Built, not installed.

Lines moved out of code comments (2026-11-17, plan_rules_fixes.md section 17):

- 2026-09-09: if-chain, not a switch: an enum switch compiles to a synthetic ItemPhysicSplit$1 that loads lazily and was missing at runtime (NoClassDefFoundError, crashes 2026-09-09 and 2026-09-17) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/itemphysic/ItemPhysicSplit.java)
- 2026-10-05: <p><b>Which recipes need a template.</b> Armor Trims' table ({@code SmithingMenuNew#createResult}, bytecode-read 2026-10-05) refuses a template-less recipe when the material is in a tag listed in {@code ArmortrimsApi.upgradeBaseBlockedConditions} whose condition is on - by default {@code #forge:ingots/netherite}, switched by {@code "Disable Vanilla Netherite Upgrading"} in {@code config/armor_trims.toml} (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/compat/jei/ArmorTrimsSmithing.java)
- 2026-10-05: JEI 11.8 matches on both, throws "no recipe category registered" on the first {@code addRecipes}, and both pages stay empty (seen in logs/latest.log 2026-10-05) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/compat/jei/ArmorTrimsSmithing.java)
- 2026-10-20: Which blocks qualify is {@link BlockModelIcons#verdict} (any namespace since 2026-10-20; {@code [wthit] iconExclude} opts out) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/compat/WthitBlockModelIcon.java)
- 2026-10-20: Starter Kit port (feature_writeup/feature_porting.md 1, Starter Kit 7.0 + Collective gear functions, jar retired 2026-10-20) (kubejs/data/creativecompat/kubejs/other/starter_kit.js)
- 2026-10-20: The jars are in disabledmods/ (2026-10-20) (kubejs/data/creativecompat/kubejs/other/feature_ports.js)
- 2026-10-20: A block with no pick item (the 2026-10-20 audit found 272: 70 Display Delight plates, Colored Water's 64 cauldrons, Immersive Posts' 30 post parts, Delightful's and Central Kitchen's pies, Create belt funnels...) leaves the icon empty, and an item whose model is missing shows the purple/black cube (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/compat/WthitBlockModelIcon.java)
- 2026-11-01: Creative Compat: item tags for the flower box, planter box, chalk and microblock tweaks; moved from kubejs/server_scripts/tags/misctags.js on 2026-11-01 (kubejs/data/creativecompat/kubejs/tags/misctags.js)
- 2026-11-01: Creative Compat (feature_writeup/creative_compat.md): the pack's tweaks to third-party mods that have no wider system of their own, carved out of patcherMod on 2026-11-01 (creative_split.md 7.5, 7.6) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CreativeCompat.java)
- 2026-11-01: Creative Compat; moved from kubejs/server_scripts/other/starter_kit.js on 2026-11-01 (kubejs/data/creativecompat/kubejs/other/starter_kit.js)
- 2026-11-01: Creative Compat: tags of the ported tier-1 mods (feature_writeup/feature_porting.md 1; recipes in other/feature_ports.js); moved from kubejs/server_scripts/tags/feature_port_tags.js on 2026-11-01 (kubejs/data/creativecompat/kubejs/tags/feature_port_tags.js)
- 2026-11-01: Creative Compat; moved from kubejs/server_scripts/tags/item_physics_tags.js on 2026-11-01 (kubejs/data/creativecompat/kubejs/tags/item_physics_tags.js)
- 2026-11-01: Creative Compat; moved from kubejs/server_scripts/other/flowers.js on 2026-11-01 (kubejs/data/creativecompat/kubejs/other/flowers.js)
- 2026-11-01: Creative Compat; moved from kubejs/server_scripts/other/feature_ports.js on 2026-11-01 (kubejs/data/creativecompat/kubejs/other/feature_ports.js)
- 2026-11-01: Creative Compat: decor blocks with no system of their own (feature_writeup/creative_split.md 7.6); moved from kubejs/startup_scripts/items/decor_blocks.js on 2026-11-01 (development/creativeCompat/src/main/resources/kubejs_startup/creativecompat/decor_blocks.js)
- 2026-11-01: Creative Compat: Curios slot membership for the pack's own slots (data: kubejs/data/creativeengineering/curios/; feature_writeup/curios_slots.md); moved from kubejs/server_scripts/tags/curiostags.js on 2026-11-01 (kubejs/data/creativecompat/kubejs/tags/curiostags.js)
- 2026-11-01: cropSeasons, cropOutOfSeason: creativecalendar-common.toml [wthit] (Creative Calendar, 2026-11-01) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CompatConfig.java)
- 2026-11-01: Creative Compat; moved from kubejs/server_scripts/economy/coin.js on 2026-11-01 (kubejs/data/creativecompat/kubejs/economy/coin.js)
- 2026-11-01: Creative Compat; moved from kubejs/server_scripts/other/dead_recipe_removal.js on 2026-11-01 (kubejs/data/creativecompat/kubejs/other/dead_recipe_removal.js)
- 2026-11-01: The sections moved out of patcherMod's creativeengineering-common.toml on 2026-11-01 with keys, comments and defaults unchanged; the first boot with Compat copies a player's values over ({@code swap.ConfigRename.carrySections}, called from {@link CreativeCompat}). {@code [shrapnel]}, {@code [surface]} and {@code [vein_undo]} came from Creative Engineering's file (same name, creativeengineering-common.toml) on 2026-11-06 with the TNT shrapnel, walk speed and Ore Excavation undo code; keys, comments and defaults unchanged, carried or merged the same way ({@link #ENGINEERING_SECTIONS}) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CompatConfig.java)
- 2026-11-01: Creative Compat: display names; moved from kubejs/client_scripts/namechanges.js on 2026-11-01 (kubejs/assets/creativecompat/kubejs/names.js)
- 2026-11-01: Creative Compat: block tags for the mod tweaks; moved from kubejs/server_scripts/tags/blocktags.js on 2026-11-01 (kubejs/data/creativecompat/kubejs/tags/blocktags.js)
- 2026-11-01: Creative Compat startup script (loaded from the Compat jar by the startup loader); moved from kubejs/startup_scripts/func/item_physics.js on 2026-11-01 (development/creativeCompat/src/main/resources/kubejs_startup/creativecompat/item_physics.js)
- 2026-11-01: creativeCompat: Creative Compat (mod id creativecompat, feature_writeup/creative_compat.md), carved out of patcherMod on 2026-11-01 (creative_split.md 7.5, 7.6) (development/creativeCompat/build.gradle)
- 2026-11-01: Creative Compat; moved from kubejs/server_scripts/other/misc_mods.js on 2026-11-01 (kubejs/data/creativecompat/kubejs/other/misc_mods.js)
- 2026-11-01: Creative Compat: item tooltips for the mod tweaks (misc mods, Quark, Supplementaries, Framed and copycat blocks, flower pots); moved from kubejs/client_scripts/tooltips.js on 2026-11-01, rows unchanged (kubejs/assets/creativecompat/kubejs/tooltips.js)
- 2026-11-01: The toml tables carried over from creativeengineering-common.toml, in order. {@code [cats]} (added 2026-11-01, after the carve) and {@code [giant_squid]} (2026-11-02) are new in this file and are not carried. {@code [campfire]} moved on to Creative Delight's file on 2026-11-04 (creative_split.md 7.9 step 1), which carries it from here. {@code [leaf_piles]} has no spec since 2026-11-15: carried from an old file, {@code surface.LeafPileMigration} then moves its value to {@code [surface.passthrough.leaf_pile] minLayers} and drops it (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CompatConfig.java)
- 2026-11-04: JEI World Interactions: Burning rows (Campfire Rules moved to Creative Delight 2026-11-04) (WorldRows is JEI-free; JEI loads its plugin later) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CreativeCompat.java)
- 2026-11-04: Create: Dreams & Desires jetpack tooltip null-level guard (moved from Creative Common 2026-11-04) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/compat/CompatMixinPlugin.java)
- 2026-11-06: Walking on blocks (surface/, from Creative Engineering 2026-11-06; [surface]) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CreativeCompat.java)
- 2026-11-06: c: tag -> forge: tag of this pack (checked against kubejs/exported/tags, 2026-11-06) (development/backportedTempad/src/main/java/earth/terrarium/tempad/forge/UpstreamTranslator.java)
- 2026-11-06: The TNT shrapnel entity (shrapnel/, from Creative Engineering 2026-11-06, id creativecompat:shrapnel): fired on primed-TNT-entity detonation, and for the itemphysic.ItemBurnTransform burn-explosion rule through Common's util.BurnBursts (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CreativeCompat.java)
- 2026-11-06: [shrapnel], [surface] and [vein_undo] came from Creative Engineering's file of the same name (2026-11-06), after this file's first release: carried into a new file, or merged into an existing one (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CreativeCompat.java)
- 2026-11-06: The tables that came from Creative Engineering's creativeengineering-common.toml on 2026-11-06 (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CompatConfig.java)
- 2026-11-07: from creativecommon-common.toml (interface carve, 2026-11-07) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CompatConfig.java)
- 2026-11-07: Distant Horizons' FEATURES generator hands features smaller regions than vanilla's 3x3, so every basalt column it generates in the Nether warns: 440,000 lines in 15 minutes on 2026-11-07 (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/compat/LogSpamFilter.java)
- 2026-11-07: Tested version range of the interface mods (moved with their mixins from Creative Common's CommonMixinPlugin, 2026-11-07): outside it the mod's mixins are skipped with one warning instead of crashing the boot (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/compat/CompatMixinPlugin.java)
- 2026-11-07: - no nested members (Tempad was one until 2026-11-07; now its own jar, development/backportedTempad), so Compat needs no donor jar (development/creativeCompat/build.gradle)
- 2026-11-07: One fog colour for the whole Nether, a deep red ({@link #RGB}), instead of each biome's own (perf.md 19.3, owner 2026-11-07) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/compat/NetherFogColour.java)
- 2026-11-07: In Creative Compat since 2026-11-07 (was Common func/) (development/creativeCompat/src/main/resources/kubejs_startup/creativecompat/func/jei_order.js)
- 2026-11-07: interface mixins from Creative Common (JEI, tooltips, GUI titles, toasts, effect icons; 2026-11-07) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/compat/CompatMixinPlugin.java)
- 2026-11-07: --- interface carve from Creative Common, 2026-11-07 --- WTHIT plugins: state lines, holder and pot contents, hives, campfires, coasters, tanks, remainder buffer, silk touch, sheared, operator blocks, block model icons (development/creativeCompat/src/main/resources/META-INF/mods.toml)
- 2026-11-07: [jei], [gui_title], [toasts], [tooltips], [coaster], [wthit] came from Creative Common's file with the interface code (2026-11-07); before the 2026-11-04 swap they were in patcherMod's creativeengineering-common.toml (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CreativeCompat.java)
- 2026-11-07: The saved per-portal flag (IP default on) is ignored, so the wrong values saved on Portal Gun portals since 2026-11-07 are harmless (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/mixin/IPExitLiftMixin.java)
- 2026-11-07: Sections taken over from Creative Common's creativecommon-common.toml (interface carve, 2026-11-07) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CompatConfig.java)
- 2026-11-07: Immersive Portals and the Portal Gun (feature_writeup/plan_portal_changes.md, 2026-11-07) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CompatConfig.java)
- 2026-11-07: Nether terrain under the roof is mostly hidden by fog anyway, and its generation (basalt deltas especially) was a large part of DH's world-gen thread time on 2026-11-07 (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/compat/DhDimensions.java)
- 2026-11-07: Vanilla measures and draws every line of every sign in block entity range on every frame: 1.8% of the render thread on 2026-11-07 (perf.md 19.3), mostly line widths and glyphs, for text too small to read (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/mixin/SignTextDistanceMixin.java)
- 2026-11-10: Ids follow the owner (feature_writeup/namespace_move.md): the shrapnel entity, the botanist pot block entity, the cardboard box, models, textures, tags and loot tables use this mod's namespace ({@link #NS}); until 2026-11-10 they were {@code creativeengineering}, and data/creativecompat/swaps/owner_namespace.json carries old saves (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CreativeCompat.java)
- 2026-11-10: The pack's generated paintings moved from {@code creativeengineering:paintings/...} to {@code creativecompat:paintings/...} on 2026-11-10 (feature_writeup/namespace_move.md, D-PAINT); a painting saved before that is pointed at the new id when it loads, so it keeps its picture and saves under the new id (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/mixin/ImmersivePaintingMotiveAliasMixin.java)
- 2026-11-10: Each variant {@code ns:path} becomes {@code data/creativecompat/paintings/<ns>/<path>.png} + .json (until 2026-11-10 under creativeengineering; mixin/ImmersivePaintingMotiveAliasMixin moves placed paintings that still name the old id) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/paintings/PaintingArt.java)
- 2026-11-10: The namespace of every id this mod uses (was creativeengineering until the 2026-11-10 namespace move) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CreativeCompat.java)
- 2026-11-14: Supplementaries flax is out (owner, 2026-11-14) (kubejs/data/creativecompat/kubejs/tags/passthrough_tags.js)
- 2026-11-14: Left out on purpose (they keep their own slowdown, owner 2026-11-14): Floral Enchantment bushes, Farmer's Respite wither roots, Neapolitan beanstalk thorns, Upgrade Aquatic pickerelweed (kubejs/data/creativecompat/kubejs/tags/passthrough_tags.js)
- 2026-11-14: Was WallSlideMixin until the pass-through phase 1 (2026-11-14) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/mixin/InsideBlocksMixin.java)
- 2026-11-15: Two more hooks in {@code Entity#move} ({@code m_6478_}, scratch fixes 2026-11-15): at HEAD an item or orb held in a web skips the whole move ({@link PassItems#heldMove}: only its web is checked), and the fast-fall reset clip ({@code Level#clip}, {@code m_45547_}) leaves out webs whose fall factor applies ({@link PassThrough#fallResetClip}); {@code require = 0} so another mod's redirect of the same call wins quietly (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/mixin/InsideBlocksMixin.java)
- 2026-11-15: developer switches (plan_passthrough.md 7.3, 2026-11-15) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CompatConfig.java)
- 2026-11-15: other blocks that slow you from inside, display only (owner 2026-11-15) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/surface/StuckInBlock.java)
- 2026-11-15: [leaf_piles] slowFromLayers -> [surface.passthrough.leaf_pile] minLayers (plan_passthrough.md 3.1, 2026-11-15), then the neighbour model and the layer tier (plan_post_scratch_1115b.md C1, D: surface/PassConfigMigration) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CreativeCompat.java)
- 2026-11-15: Channel {@code creativecompat:surface}, message 1 (format {@link PassValues#FORMAT}, 2026-11-15; message 0 was the first layout and is no longer registered, so an older client drops the new message and keeps its own values, and a newer reader of another format switches the system off on the client); a side without the channel is accepted (the client then uses its own values) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/surface/PassSync.java)
- 2026-11-15: <p>Since pass-through phase 5 (2026-11-15) only untiered blocks are listed: cobwebs, barbed wire, berry bushes, propelplant, leaf piles and bamboo leaves moved to the tiers (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/surface/StuckInBlock.java)
- 2026-11-15: Botania flowers and mushrooms (owner 2026-11-15); floating flowers are solid, left out (kubejs/data/creativecompat/kubejs/tags/passthrough_tags.js)
- 2026-11-15: <p>Was: neighbour counts of the current tick, per block position (plan_passthrough.md 7.2 fallback, scratch fixes 2026-11-15): a herd in a field or a crowd in a web block shares cells, so the second entity in a cell reads the count instead of doing the 8 or 26 block lookups again (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/surface/PassCountCache.java)
- 2026-11-15: One-time config move (plan_passthrough.md 3.1): the old {@code [leaf_piles] slowFromLayers} (the Immersive Weathering leaf pile mixin, removed 2026-11-15) becomes {@code [surface.passthrough.leaf_pile] minLayers}, shared by IW leaf piles and Twigs bamboo leaves (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/surface/LeafPileMigration.java)
- 2026-11-15: Gives every block state the pass-through tier slot ({@link PassTierSlot}, plan_passthrough.md 7.2 fallback, scratch fixes 2026-11-15) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/mixin/BlockStatePassMixin.java)
- 2026-11-15: <p>Why (E1, 2026-11-15): Forge's watcher is night-config 3.6.7's {@code FileWatcher}, which only acts on an {@code ENTRY_MODIFY} event whose count is 1 (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CompatConfigReload.java)
- 2026-11-15: razor wire: owner 2026-11-15 (kubejs/data/creativecompat/kubejs/tags/passthrough_tags.js)
- 2026-11-15: One-time config move to the 2026-11-15 pass-through model (plan_post_scratch_1115b.md C1 and D), on the toml text before Forge loads the file (like {@link LeafPileMigration}, which runs first): <ul> (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/surface/PassConfigMigration.java)
- 2026-11-15: salmonberry, cranberry: owner 2026-11-15 (kubejs/data/creativecompat/kubejs/tags/passthrough_tags.js)
- 2026-11-15: The {@code Level#clip} in {@code Entity#move} ({@code mixin/InsideBlocksMixin}, scratch fixes 2026-11-15, SQ-PTH-03): moving a block or more in one tick with some fall distance, vanilla resets it when the path crosses a {@code #minecraft:fall_damage_resetting} block (cobweb, sweet berry bush, climbables) or water (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/surface/PassThrough.java)
- 2026-11-15: Layer height blocks (plan_post_scratch_1115b.md D1, 2026-11-15): one rule by height for every stacked-layer block (kubejs/data/creativecompat/kubejs/tags/passthrough_tags.js)
- 2026-11-15: HEAD of {@code Entity#move} for a stuck item or orb (scratch fixes 2026-11-15, SQ-PTH-28 PT04: the zero-length moves of 500 held items cost about 3 ms a tick in entity collision queries) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/surface/PassItems.java)
- 2026-11-15: Superseded 2026-11-15 by the counts baked into the chunk ({@link PassCounts}, plan_post_scratch_1115b.md C2): movement no longer reads this table (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/surface/PassCountCache.java)
- 2026-11-16: No per-block overrides by default (leaf layers use the passthrough_layers_open tag, owner 2026-11-16) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/surface/PassValues.java)
- 2026-11-16: golem farm cooldown and boat ice friction (plan_small_fixes_1116.md R2, R3; 2026-11-16) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CompatConfig.java)
- 2026-11-16: Layer height blocks (owner 2026-11-16): snow layers, every Immersive Weathering leaf pile (azalea flower pile included) and Twigs bamboo leaves (kubejs/data/creativecompat/kubejs/tags/passthrough_tags.js)
- 2026-11-16: Layer blocks that never act whole (leaf piles, bamboo leaves; owner 2026-11-16) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/surface/PassThrough.java)
- 2026-11-16: Leaf layers never act whole (owner 2026-11-16): no solidBelowFull for them (kubejs/data/creativecompat/kubejs/tags/passthrough_tags.js)
- 2026-11-16: Layer height blocks (plan_post_scratch_1115b.md D, owner 2026-11-16): snow layers, leaf piles, bamboo leaves (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/surface/PassTier.java)


## 6. License audit (2026-11-04, plan_cb_publish.md Step 2)

Audit of `creativecompat-1.0.0.jar` (23:34). Summary row in [creative_brewing.md](creative_brewing.md) 9.2. mods.toml
`GPL-3.0-only`, `creativecompat-LICENSE.txt` present, no NOTICE.

The 950 painting files (475 PNG + 475 JSON from `gen_immersive_paintings.py`; removed 2026-11-05, 6.2): every PNG is another mod's or vanilla's
art upscaled (474 exact after downscaling, one stretched). None is own art; each JSON names the artist.

| Part | Source license | Verdict |
|---|---|---|
| paintings `rebirth_paintings` (250) | Academic Free License 3.0 (GPL-incompatible per FSF) | fix (notice) or out of the jar |
| paintings `toomanypaintings` (65) | MIT | fix (notice) |
| paintings `darkpaintings` (30) | LGPL 2.1 | fix (notice + license text) |
| paintings `alexsmobs` (2), `cccbridge` (1, Apache-2.0), `create_things_and_misc` (1, AFL 3.0) | as named | fix (notice) |
| paintings `paintings_pack1` (49) | CC BY-NC 3.0 | remove |
| paintings `handcrafted` (28) | Terrarium License | remove |
| paintings `minecraft_style_paintings` (12) | none stated (ARR) | remove |
| paintings `supplementaries` (1) | ARR | remove |
| paintings `buzzier_bees`, `clayworks`, `neapolitan`, `upgrade_aquatic` (6) | Abnormals license (URL only, not read) | remove unless it allows |
| paintings `minecraft` (30) | Mojang | remove |
| elytra banner and bedspread textures (73) | derived from Botania, IE (Blu's), Alex's Mobs (LGPL), Buzzier Bees banner patterns | built 2026-11-06: built at load from the installed jars, none ship (6.3) |
| MineColonies banner textures (25) | MineColonies not in the pack; leftover output | removed 2026-11-06 (6.3) |
| `weathered_cardboard_box.png` + models | Design and Decor (MIT) recolour | OK: MIT notice added 2026-11-05 |
| `moss_block_tinted.png` | Mojang `moss_block` greyscaled | low ([creative_brewing.md](creative_brewing.md) 9.3) |
| flower box models (3) | reference Beautify / Quark textures, none copied | OK |
| Java, 79 classes (45 mixins), scripts, lang, tags, loot | own | OK |
| `TrashSlotGuiHandlerMixin` `@Overwrite onScreenInit` (about 33 lines) | follows TrashSlot's ARR method | built 2026-11-06: four redirects and one inject, no overwrite (6.4) |
| `SupplementariesBreezyCompatMixin` `@Overwrite` (4 lines) | own formula | OK |

Fixes: TODO `licence/compat #1` (paintings), `#2` (banner textures, MineColonies leftovers), `#3` (cardboard notice,
moss, TrashSlot overwrite).

2026-11-05: `NOTICE-thirdparty.txt` added (in the jar as `creativecompat-NOTICE-thirdparty.txt`): credit and license for
every painting source and banner-pattern source, the Design and Decor MIT notice, and the full MIT, Apache 2.0, AFL 3.0,
LGPL 2.1 and LGPL 3 texts. Credit does not grant redistribution: the remove rows above stand (`licence/compat #1`, `#2`).


### 6.1 Painting options (2026-11-05, for the owner's pick; TODO `licence/compat #1`)

Immersive Paintings reads `data/<ns>/paintings/*.png` as **server data** and sends the images to clients, so whatever
route is picked runs where the datapacks load (the dedicated server, or the integrated server in single player). Every
source mod is installed on both sides ("both" in mod_sides.md), and each jar carries its own `textures/painting/` art, so
Compat can build the set in memory at load from the installed jars (the same idea as CD 40.1, as a data pack). The
dedicated server jar has no vanilla textures, so vanilla paintings can't be read there.

| Source | Paintings | License | Ship in a datapack | Build from the installed jar |
|---|---|---|---|---|
| rebirth_paintings | 250 | Academic Free License 3.0 | yes, with notice; keep out of the GPL jar (AFL is GPL-incompatible) | yes |
| toomanypaintings | 65 | MIT | yes, with notice | yes |
| darkpaintings | 30 | LGPL 2.1 | yes, with notice and license text | yes |
| alexsmobs | 2 | LGPL | yes, with notice | yes |
| cccbridge | 1 | Apache 2.0 | yes, with notice | yes |
| create_things_and_misc | 1 | Academic Free License 3.0 | yes, with notice (not in the GPL jar) | yes |
| paintings_pack1 | 49 | CC BY-NC 3.0 | only non-commercial with credit; CurseForge rewards make it doubtful | yes |
| handcrafted | 28 | Terrarium License | no | yes |
| minecraft_style_paintings | 12 | none stated | no | yes |
| supplementaries | 1 | ARR | no | yes |
| buzzier_bees 1, clayworks 1, neapolitan 3, upgrade_aquatic 1 | 6 | Abnormals license (not read) | no until read | yes |
| minecraft | 30 | Mojang | pack-folder datapack only (low, 9.3 of creative_brewing.md), never a published jar | single player only (no textures in the server jar) |

Totals: 349 can ship with notices; 96 can't ship (49 of them NC-doubtful); all 445 modded ones can be built from the
installed jars, which needs no notices and no license reading. Vanilla's 30: drop from the set, or keep in a pack-folder
datapack.

### 6.2 Owner pick (2026-11-05; TODO `licence/compat #1`)

Status: **built 2026-11-05, NOT installed** (details and tests in [immersive_paintings.md](immersive_paintings.md)).

- **Modded paintings: built from the installed jars.** No modded painting PNG ships in the Compat jar or the pack.
  At load, Compat scans each installed mod jar's `assets/<ns>/textures/painting/` for registered variants and serves
  them to Immersive Paintings as an in-memory data pack (`data/<ns>/paintings/<id>.png` + json), doing what
  `gen_immersive_paintings.py` does now: upscale to 32 px per block, sizes from the registry, authors from lang /
  DarkPaintings credits / mod name.
- **Extra sources by config.** A list in `creativecompat-common.toml` names more jars or zips to read painting art
  from: painting mods and other jars (for example a resource pack that retextures paintings, or a painting jar not
  installed as a mod). Same layout rule (`assets/<ns>/textures/painting/`); later entries win, as resource packs do.
- **Vanilla's 30: a pack-folder datapack.** Generated pack-side (the generator limited to the `minecraft`
  namespace), never inside a published jar. The dedicated server has no vanilla textures, so it can't be built there.
- Done with it: the 950 files left the Compat jar; `NOTICE-thirdparty.txt` lost the painting credits and the MIT
  (Too Many Paintings), Apache, AFL and LGPL 2.1 texts (only paintings needed them); scratch rows SQ-PA-01 to -04,
  CQ-PA-01, -02. A config-listed source can override art but can't add variants (those need the mod's registry entry).

### 6.3 Banner pattern textures built at load (2026-11-06; TODO `licence/compat #2`)

Status: **built 2026-11-06 13:17, NOT installed** (`creativecompat-1.0.0.jar` in build/libs). Owner pick 2026-11-05
(plan_cb_publish.md 13, N2).

- **What:** Bedspreads and Customizable Elytra draw banner patterns from their own bundled, re-placed copies; for
  patterns they lack, Compat used to ship 73 generated textures (Botania 15/13, IE 9/9, Alex's Mobs 0/1, Buzzier Bees
  1/1, bed/elytra; the elytra ones twice, full and `_low`) plus 25 MineColonies leftovers. All 98 left the jar.
- **Now:** `banners/BannerPatternArt` (pure Java) does what `gen_bedspreads_pattern_compat.py` and
  `gen_elytra_pattern_compat.py` did: first glyph of each `assets/<ns>/textures/entity/banner/<id>.png`, placed on each
  mod's canvas, Lanczos over premultiplied alpha with Pillow's fixed-point rounding. `banners/BannerPatternPack` serves
  the result as the hidden client resource pack `creativecompat_banner_patterns` (bottom of the stack, built once on the
  first resource load) from every installed mod jar (file name order, Minecraft last, first wins), only on the client
  and only with Bedspreads or Customizable Elytra installed. Paths stay `creativeengineering:textures/entity/...`, so
  `BannerPatternAtlasCompat` and the two mixins are unchanged. No config (nothing to choose; resource packs can still
  override any path). Log: `[banner_patterns] <n> Bedspreads and <m> Customizable Elytra pattern textures built`.
- **Offline check:** against `mods/` the builder reproduces all 73 old files pixel for pixel, and adds 4 for Neapolitan
  (`chimpanzee`, `mca_chimpanzee`; installed after the generators last ran): 27 bed and 25 elytra patterns.
- **Generators retired:** both scripts deleted from `tools/` and from `tools/generators.json`'s hand-run note.
- **Notice:** `NOTICE-thirdparty-various.txt` says no banner art ships and credits the pattern sources (Botania, IE, Alex's Mobs,
  Buzzier Bees and Neapolitan) and the layouts measured from Bedspreads and Customizable Elytra.
- Tests: CQ-BPT-01 to -03, SQ-BPT-01 (scratch_queue.md).

### 6.4 TrashSlot screen setup without an overwrite (2026-11-06; TODO `licence/compat #3`)

Status: **built 2026-11-06 13:17, NOT installed.** `TrashSlotGuiHandlerMixin` no longer overwrites `onScreenInit`
(which restated TrashSlot 12.1.0's all-rights-reserved method). TrashSlot's own body runs, with:

| Change | How |
|---|---|
| Settings from `TrashSlotCompat.settingsFor` | `@Redirect` of `TrashSlotSaveState.getSettings` |
| Creative inventory screen gets a slot | `@Redirect` of the second `event.getScreen()` (its only use is the `instanceof CreativeModeInventoryScreen` skip) to `null` |
| Survival inventory of a creative player gets a slot | `@Redirect` of the `Abilities.instabuild` read to `false` |
| Spectator: previous screen's slot cleared | `@Redirect` of `Player.isSpectator()`, clears when true |
| Server without TrashSlot (first screen): previous slot cleared | `@Inject` at the `sentMissingMessage` write |

Behavior is the same as the overwrite. The one path that differs in principle: TrashSlot builds the slot component only
when the settings are not `NONE`, the overwrite always did; `settingsFor` never returns `NONE`, so the outcome is equal.
The `onMouseClick`, `runKeyBindings` and `onScreenDrawn` redirects are unchanged. Tests: CQ-TSL-01, -02 (with the
[trashslot.md](trashslot.md) 7 list).
