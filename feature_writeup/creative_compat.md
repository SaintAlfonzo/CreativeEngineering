# Creative Compat

Written 2026-11-01 (Creative Split wave 3, [creative_split.md](creative_split.md) 7.6; TODO `creativesplit/compat #5`).
Publishing as 1.0.0: [project page](project_pages/creative_compat.md),
[file changelog](project_pages/creative_compat_changelog_1_0_0.md), [plan_wave1_publish.md](plan_wave1_publish.md). The
page was checked against the `1.0.0+db212e956` jar on 2026-11-24.

Written for: the owner and agents working on the split. Own mod `creativecompat` (development/creativeCompat,
`creativecompat-1.0.0.jar`, side both): the pack's tweaks to third-party mods that have no wider system of their own,
plus the features moved from Creative Common and Raspberry Pie in wave 2 (section 1).

## 0. At a glance

<!-- generated:status_index begin open_work sha1=9b6031705ee1 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 6 open TODO rows (Testing 6), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `creative-common-publish #17` | Testing | S | Wave 2 carve to Creative Compat: melting (with End no-fire and the Saltable tooltip), Quark glass shard drops, sack sneak pick-up, Create: Gravity low gravity, server-start fix and ItemPhysic tie; [perf] gravityLimitDimensions stays in Common (W2-4); one-time config carry-over (W2-8) |
| `creativecompat/publish #1` | Testing | C S | Compat crashes at load without the Tempad upstream jar |
| `creativesplit/compat #5` | Testing | S C | Carve Creative Compat |
| `licence/compat #1` | Testing | C | Paintings out of the Compat jar |
| `licence/compat #2` | Testing | C | Banner-derived textures and MineColonies leftovers |
| `licence/compat #3` | Testing | C | Compat: cardboard notice, moss texture, TrashSlot overwrite |
<!-- generated:status_index end open_work -->

## 1. What it holds

- **Java** (`com.SaintAlfonzo.creativecompat`, same subpackages as in Creative Common): `compat/` (banner pattern atlas,
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
- **Mixins:** `creativecompat.mixins.json`, 86 common + 62 client (counted from the jar's list 2026-11-22; SQ-CMP-01 compares the list to what applies instead of a typed count; per-change counts in the history; 2026-11-07: `XaeroAtlasColourMixin`, gated on `xaeroworldmap`, perf.md 18; 2026-11-04: the two client additions are `JetpackItemTooltipMixin`, gated on
  `create_dd`, and the ungated `FallingBlockWaterSplashMixin`, both from Common, member prefix `creativecompat$`), SRG names, `remap = false`, member prefixes still
  `creativeengineering$`. `compat/CompatMixinPlugin` (a `GatedMixinPlugin`) applies each only when its mod is loaded:
  an any-of table plus an all-of table (`NEEDS_ALL`) for the Polymorph pairs (with sophisticatedcore, IE, Pretty Pipes),
  Breezy with Supplementaries, and the Create breaker with Quark. Since 2026-11-22 (publish_readiness.md PR-CP-04 to
  -07): every gated mod has a tested version range in `MOD_RANGE` (installed jar up to its next minor; outside it the
  mod's mixins are skipped with one warning); the Breezy `@Overwrite` applies only at Supplementaries 1.19.2-2.4.20 and
  Breezy 1.0.8 exactly (`EXACT`); `CozyHomeGoalFeatherMixin` needs both Cozy's Improved Cats and Domestication
  Innovation. The 32 mixins that change vanilla with no mod to gate them each have an enable key in `[mixins]`
  (`compat/MixinSwitches`, all default true, read by the plugin as text at start, so a change needs a restart; owner
  ruling P3, 2026-11-22). Pass-through code checks its duck interfaces with `instanceof`, so any of those keys can be
  off without a crash. Hook faults in per-tick and per-frame mixins log once each (`compat/HookFaults`, PR-CP-09).
- **Config:** `config/creativecompat-common.toml` (`CompatConfig`). Since 2026-11-22 there is no carry-over from older
  files: the section copies from `creativeengineering-common.toml` and `creativecommon-common.toml`, the leaf pile
  migration and the pass-through table rewrite were dropped before the first public release (PR-CP-17; owner,
  2026-11-22). The one carry-over left (2026-11-23, PR-CP-11): the four old `[jei]` page switches
  (`seasoningCategory`, `canCategories`, `potionBucketCategory`, `tippedArrowCategory`) are read once into `[jei]
  pagesOff` and removed (`JeiPageKeys`, before the config is registered). Top-level tables in the spec (23, from
  `CompatConfig` 2026-11-24; `[giant_squid]` removed, ruling W17): `[moss]`, `[vine_shearing]`, `[monster_box]`, `[trash_slot]`, `[cats]`,
  `[golems]`, `[boat_friction]`, `[paintings]`, `[shrapnel]`, `[surface]` (subtables `speed`, `sticky`, `camouflage`,
  `sinking`, `passthrough`, `wthit`), `[vein_undo]`, `[jei]`, `[gui_title]`, `[toasts]`, `[screens]`, `[tooltips]`,
  `[coaster]`, `[wthit]` (`state_lines`), `[immersive_portals]` (`fog`), `[portalgun]`, `[corpses]`, `[debug]`
  (`passthrough`), `[mixins]` (one key per ungated mixin, see Mixins). `[jei] pagesOff` (default empty) lists the
  grouped recipe page ids to leave off; the jars that add the pages ask `CompatConfig.JEI.pageOn(id)`: `seasoning`,
  `cans` (Creative Delight), `potion_buckets`, `tipped_arrows` (Creative Brewing). `[gui_title] logOverflow` defaults
  to false (PR-CP-16; the pack's own config file keeps its value).
- **Leaf piles (2026-11-15, pass-through phase 3, [plan_passthrough.md](plan_passthrough.md) 3.1):** `LeafPileSlowMixin`
  and `surface/LeafPileSlow` are gone; IW leaf piles and Twigs bamboo leaves are the `leaf_pile` pass-through tier,
  threshold `[surface.passthrough.leaf_pile] minLayers` (default 4). `surface/LeafPileMigration` moves an old
  `[leaf_piles] slowFromLayers` value there on boot (the old value wins unless it is the default 4 and `minLayers`
  exists) and drops the section (dropped 2026-11-22 with the other migrations). IW's `leaf_piles_slow` no longer sets the slowdown (the tier cancels IW's
  `makeStuckInBlock`), but it still gates IW's spruce thorn damage, which sits in the same branch: thorns need
  `leaf_piles_slow = true` and, with the mixin gone, IW's own 4+ layers (6+ for damage), whatever `minLayers` says.
- **Pass-through config (2026-11-15, [plan_passthrough.md](plan_passthrough.md), feature doc [blockrunner.md](blockrunner.md) 3.10):**

  | Table | Keys (default) | Side |
  |---|---|---|
  | `[surface.passthrough]` | `enabled` (true; false = every block back to its own slowdown), `arrowLifeTicks` (12000), `arrowLongLifeCap` (128) | common; the server's values are sent to clients on join and save |
  | `[surface.passthrough.<tier>]`, one per tier (`surface/PassTier`): `dense_crop`, `crop`, `wild_crop`, `vine`, `tall_plant`, `cane`, `rope`, `cobweb`, `bush`, `layers`, `grass` | `walk`, `fall`, `growth` (ANY / HALF / TALL_HALF), `neighbours` (NONE / SIDES / FACES; the old RING / CUBE still read as SIDES / FACES), `denseFrom`, `denseWalk`, `denseFall`, `denseResetsFall`, `surrounded` (NONE / WEB), `minLayers` (1-9, 9 = never), `walkOnlyMoving`, `items` (NONE / STICK), `itemStickChance`, `projectiles`, `depthStriderPasses`; defaults per tier in blockrunner.md 3.10 | common, synced |
  | `[surface.wthit] passThrough` | true: Walk Through / Fall Through / Neighbors lines | client |
  | `[debug.passthrough] timing` | false; true logs a `[passthrough-timing]` line per side every 200 ticks (`tools/perf/passthrough_timing.py` reads them) | both, applies on save |

- **Corpse decay (2026-11-18, [plan_corpses.md](plan_corpses.md) 3.1, TODO `corpses #1`, `corpses #6`):** `corpse/`
  (`CorpseDecay` stage rules, `CorpseState` synced flags, `CorpseRender` client draw) with `CorpseDecayMixin` (common) and
  `CorpseRendererMixin` (client), both gated on `corpse`. The Corpse mod's loaded-tick clock is switched off (its
  `skeleton_time` and empty `despawn.time` are ignored while `[corpses] enabled`; `force_time` still applies, on world
  time). Compat compares game time with the death and emptied times once a second, so unloaded corpses catch up on load.

  | Rule | Normal | Nether (`netherDimensions`) | End (`noDecayDimensions`) |
  |---|---|---|---|
  | Holds items | skeleton 7 days after death (`skeletonTime` 168000) | skeleton after 1 day, wither skeleton 1 day later (`netherSkeletonTime`, `netherWitherTime` 24000) | player model, never despawns |
  | Emptied | skeleton after 30 s (`emptySkeletonTime` 600), gone after 5 min (`emptyDespawnTime` 6000) | skeleton after 15 s, wither skeleton 30 s later, gone after 5 min | player model, gone after 5 min |

  A stage never goes back; putting items back into an emptied corpse resets its 5 min. The model wears only the armor,
  offhand and held item still inside (`followContents`; the client re-dresses its dummy each frame). The wither look
  is the vanilla `textures/entity/skeleton/wither_skeleton.png` on the normal skeleton model, read from its vanilla
  path (no copied file). State is saved in the entity compound `creativecompat:corpse` (`DeathTime`, `EmptiedAt`,
  `Stage`, `Ancient`, `Remains`); corpses from before the change take `DeathTime` = now minus the mod's loaded age.
  `Ancient` (skeleton from the start, "Ancient Corpse") and `Remains` (never decays or despawns, "Ancient Remains") are
  set by Creative Loot through that compound (`corpses #2`, `#7`); their names are Compat's own lang keys
  (`entity.creativecompat.ancient_corpse`, `ancient_remains`; moved from Creative Loot 2026-11-23, PR-CP-12).
  Skeletons sink (`skeletonsSink`, `sinkSpeed` 0.1 blocks a tick; ruling 27, TODO `corpses #9`): a skeleton-stage
  corpse (player, ancient or remains) in water or lava speeds down to `sinkSpeed` instead of the Corpse mod's upward
  drift (a `@ModifyArg` on the y of its one `setDeltaMovement` in `tick`); player-model corpses keep floating.
- **Corpse screen sections (round 1 of TODO `corpses #4`, [plan_corpses.md](plan_corpses.md) 3.6, rulings 10, 31, 32):**
  `corpse/CorpseSections` (layout, tags, the server slot view), `CorpseCurios` (the only Curios API caller) and
  `CorpseSectionsClient` (frames, tooltips, model). Cosmetic armor and curios stay in the Corpse mod's additional items
  (so a corpse holding only them is not empty) but show in their own slots: 4 cosmetic slots on the top row between the
  real armor and the offhand (helmet leftmost; the offhand slot moves from x 98 to x 152), and the curios in a
  Curios-style panel left of the window, one column of 12, a second column further left past 12, in slot type, cosmetic,
  index order. The Additional Items pages, their button and their auto-open leave those stacks out. Section slots are
  take-only; taking (click, shift-click, hotbar swap, Transfer Items) strips the tags so the item stacks normally.
  Curio origin: a `LivingDropsEvent` listener at HIGHEST snapshots the dead player's curio and cosmetic curio stacks,
  one at LOW tags the matching drops `creativecompat:curio` {`Slot`, `Index`, `Cosmetic`} (Curios drops them at NORMAL,
  the Corpse mod collects at LOWEST). Transfer Items first puts each tagged curio back into that exact slot when it is
  empty, then the Corpse mod, Corpse Curios Compat (any free slot of the type) and Cosmetic Armor x Corpse Compat run as
  before. The slot count is the same on both sides from a second synced corpse value (`Cap`, plus the 4 cosmetic stacks
  for the model), since the client corpse holds no items. The corpse's player model wears its own cosmetic armor:
  Cosmetic Armor Reworked dresses any player by UUID, and the Corpse mod's dummy player carries the dead player's UUID,
  so the corpse showed the live player's current cosmetics; a `RenderPlayerEvent.Pre` listener at NORMAL (after that
  swap) writes the corpse's own stacks, the Post listener puts the real armor back. Config `[corpses]`:
  `sectionedScreen` (true), `curioCap` (24, 0 to 48; only the server's value counts). Mixins (gated on `corpse`):
  `CorpseInventoryContainerMixin` (`<init>` TAIL adds the slots; redirects of `isAdditionalInventoryEmpty` in
  `m_38946_` and of `NonNullList.isEmpty` in `transferItems`; `transferItems` RETURN strips the tags),
  `CorpseContainerBaseMixin` (`m_7648_` HEAD, shift-click), `CorpseAdditionalContainerMixin` (`<init>` TAIL, filtered
  view and `setSlots(0)`), `CorpseGuisMixin` (redirect in `lambda$openCorpseGUI$1`, the button flag),
  `CorpseTransferMessageMixin` (`executeServerSide` before `transferItems`; also gated on `curios`) and the client
  `CorpseScreenMixin` (`ScreenBase.m_7286_` TAIL).
- **Corpse models (round 2 of TODO `corpses #4`, [plan_corpses.md](plan_corpses.md) 3.6, rulings 4, 10, 35):** both
  corpse models show the dead player's curios and cosmetic armor, following the contents. The synced section tag also
  carries `Curios` (each curio-tagged stack still in the corpse: slot, index, cosmetic, `Hidden` when the player had its
  render toggled off; recorded at death by `CorpseCurios`), rebuilt once a second. Player model: the Corpse mod's dummy
  player is a client player with a Curios handler that is never synced; `CorpseCuriosClient.fill` (from the
  `RenderPlayerEvent.Pre` listener) builds its slot types from the list, so Curios' own layer and Elytra Slot's layer
  draw curios and a back-slot elytra as on the player. Skeleton (plain and wither look): `CorpseSkeletonLayer`, added
  to the vanilla skeleton renderer (mod-bus `EntityRenderersEvent.AddLayers`) and to the wither look, acts only on the
  Corpse mod's `DummySkeleton` while its corpse draws: each shown curio (cosmetic over real, one per slot index) through
  its `ICurioRenderer` (a renderer that throws is skipped with one log warning), and an elytra in a curio slot with no
  curio renderer through a vanilla elytra layer (vanilla wings texture); since 2026-11-22 a Customizable Elytra elytra
  goes through that mod's own layer instead, so its dye colors and banner patterns show (`corpse/CorpseCustomElytra`;
  owner ruling M2, 2026-11-22). The skeleton is dressed with the cosmetic armor
  over its real armor (`CorpseSectionsClient.withCosmetics`). Curios API calls only in `CorpseCurios` and
  `CorpseCuriosClient`.
- **Item Trash Can screen (2026-11-23, [plan_ready_now_1123.md](plan_ready_now_1123.md) 8, ruling R1, TODO
  `storage-and-item-loss #17`):** Trash Cans 1.0.18. A plain right-click on `trashcans:item_trash_can` opens a
  three-row chest screen ("Trash Can (deleted on close)", `container.creativecompat.trash_can`) over a fresh 27-slot
  container that is never saved and is cleared when the screen closes, as FTB Essentials' trash can; shift-click from
  the inventory works (vanilla ChestMenu), a stack still on the cursor at close goes back to the inventory. A sneak
  right-click with an empty hand opens the mod's own filter screen (vanilla only calls block use while sneaking when
  both hands are empty). The Liquid, Energy and Ultimate Trash Cans share the block class and are unchanged; hopper and
  pipe insertion still deletes through the block entity. `TrashCansItemCanUseMixin` (HEAD of Core Lib's
  `BaseBlock.interact` on `TrashCanBlock`, `remap = false`, gated on `trashcans` in `[1.0.18,1.1)`) and
  `compat/TrashCanScreen`; compiles against `trashcans` and `supermartijn642corelib` in Creative Common's `libs/`.
- **Resources in the jar** (some paths and ids keep the `creativeengineering` namespace): moss tinted models and texture,
  flower box models, `tags/items/physics/`, 136
  lang keys (counted 2026-11-23: MCA eyes, monster box, pass-through and WTHIT lines, falling layer, ancient corpse names), no banner pattern textures (since 2026-11-06 the Bedspreads and Customizable
  Elytra set is built at load from the installed jars, `banners/BannerPatternPack`, 6.3), no painting art (since 2026-11-05 the Immersive Paintings set
  is built at load from the installed jars, `paintings/PaintingPack`, 6.2), the monster box reward loot table. The
  weathered cardboard box left for Creative Engineering on 2026-11-24 (ruling W16, `creative-compat-publish #8`;
  [creative_engineering.md](creative_engineering.md) 4.2).
- **Scripts:** startup `kubejs_startup/creativecompat/` (`item_physics.js`); server `kubejs/data/creativecompat/kubejs/` (flowers, Botany Pots, physics tags, and the Compat rows of curios, block
  and misc tags). The pack balance (Starter Kit, feature ports, misc mods, dead recipe removal, coin, feature-port tags)
  moved to the pack's `kubejs/server_scripts/` on 2026-11-22 (publish_readiness.md D1: Compat publishes compatibility
  fixes only); recipe ids keep the `creativecompat` namespace. The Botany Pots crop recipes and the two dimension tags
  (`kubejs/data/creativecompat/recipes/`, `tags/`, written by `tools/gen_botany_crops.py`) are copied into the jar since
  2026-11-22 (PR-CP-10); client
  `kubejs/assets/creativecompat/kubejs/` (flower pot names, the Misc Mods, Quark, Supplementaries, Framed / Copycat and
  flower pot tooltips). Each tree opens with `_compat_core.js`. The Compat tooltips call the pack's tooltip helpers
  through `global.tooltips` (published by `client_scripts/tooltips.js`), at event time.

- **Wave 2 carve from Creative Common (2026-11-24; [plan_wave2_publish.md](plan_wave2_publish.md), TODO `creative-common-publish #17`, `raspberry-pie-publish #25`):**
  - Melting ([melting.md](melting.md)): `melting/` (Melting, MeltTags, SaltableTooltip), mixins `MeltingBlockStateMixin`, `BiomeMeltMixin`, `VanillaLightMeltMixin`, `RareIceLightMeltMixin` (rare_ice) and `EndFireMixin`; `[melting]` in `creativecompat-common.toml` (all keys, `endNoFire` included); tags `creativecompat:melting/*`; lang key `creativecompat.tooltip.saltable`; enable keys `[mixins] meltingBlockState`, `biomeMelt`, `vanillaLightMelt`, `endFire`. Creative Delight's salt code calls `creativecompat.melting.Melting`.
  - Glass shards ([glass_shards.md](glass_shards.md)): `loot/GlassShardsModifier`, serializer `creativecompat:glass_shards`, definition, Forge list entry (replace false) and the `glm_entries.json` fragment (Common ships none).
  - Sack sneak pick-up ([storage_audit.md](storage_audit.md) 7): `storage/SneakPickup`, tag `#creativecompat:sneak_pickup`, script `tags/sneak_pickup_tags.js`.
  - Create: Gravity low gravity ([creative_engineering.md](creative_engineering.md) 7): `gravity/LowGravity` and the `LowGravity*` mixins for items, blocks, projectiles and boats, `CreateGravityConfigAccessor`, `LowGravityItemPhysicMixin` (needs ItemPhysic and Create Gravity) and `CreateGravityServerStartMixin` (the dedicated-server start fix, split from Common's `CreateGravityDimensionMixin`); gated on create_gravity [1.2.0,1.3). Common keeps `CreateGravityDimensionMixin` and `[perf] gravityLimitDimensions` / `gravityDimensions` (W2-4), exposed as `perf/GravityDimensions`, which `LowGravity` asks.
  - Fleet-Footed ([raspberry_pie.md](raspberry_pie.md) 12.2; from Raspberry Pie): `FleetFootedMixin` (alexsmobs [1.21.1,1.22) plus the member check, in `CompatMixinPlugin`) and `mobs/FleetFootedFov`; client `[fleet_footed] fov` (0.25, 0 to 1). Raspberry Pie alone has no Fleet-Footed fix (W2-3).
  - Carry-over (`MovedKeys`, runs before the config registers): `[melting]` from `creativecommon-common.toml` (whole section copied when Compat's file is new, merged when it lacks `[melting]`; one WARN when nothing was copied and Common's file still has it); `fleetFootedFov` from `solraspberrypie-client.toml` `[effects]` into `[fleet_footed] fov`. Log lines `[swaps] config sections [melting] carried over|merged`, `[swaps] config [effects] fleetFootedFov = X carried over`. No registered ids moved, so no swap entries. Optional mods.toml rows added: create_gravity, alexsmobs, rare_ice.

## 2. Edges to Common and hooks for the jars above (Common never imports Compat)

Compat requires Common; Common names no Compat class. Since 2026-11-23 (PR-CP-12, owner ruling on
`creative-compat-publish #7`) Compat and Common hold no class name, lang key, namespace or config key of Creative
Delight, Creative Brewing or Creative Loot: each of those jars registers what Compat or Common needs from its own
constructor. `tools/design_rules_audit.py sideways` (DR-C7) checks it.

| Reader | Hook | Filled by |
|---|---|---|
| Common JEI world tabs (`WorldInteractionJEI`, now Compat's) | `WorldRows.provider(BURNING, ...)` | Compat's constructor when JEI is loaded (CAMPFIRE: Creative Delight) |
| `WthitCampfire` | `campfire/CampfireRainInfo` suppliers; `CampfireRainTicks` accessor (`creativecommon$getRainTicks`) in Common | Creative Delight |
| `WthitHolderContents` | `compat/JarContents` kinds / total hooks (fallback: per-slot list) | Compat |
| `WthitPotContents` | `compat/PottedPlantHolder`, implemented by the botanist pot block entity | Compat |
| `ItemBurnTransform` -> shrapnel | `util/BurnBursts` | the shrapnel side |
| `block/VineShearing` (no `fertile` state for vines with their own shears handling) | `VineShearing.keepOwnHandling(className)`; Quark's and Vinery's vines built in | Creative Delight (Hearth and Harvest hops vine) |
| `surface/StuckInBlock` (WTHIT speed line of untiered blocks) | `StuckInBlock.register(className, multiplier)` | Creative Delight (powdered sugar block, 0.9) |
| `compat/CompoundEffectTooltip` (parts under "When Applied:") | `CompoundEffectTooltip.addEffectSource(item -> effects)` | Creative Brewing (`WineTooltipEffects`, the Vinery port's wine table) |
| `compat/JarMixing` (mixed jar families) | `JarMixing.addFamilyNamespace(ns)`: `<ns>:jar_family/<name>` item tags | Creative Delight |
| `compat/jei/world/WorldTab.SAMPLE_SYRINGE` icon and catalyst | `WorldTab.addTool(tabId, itemOrTag)` (icons may be tags) | Creative Delight (`sample_syringe`) |
| `mixin/CorpseDecayMixin` names | Compat lang `entity.creativecompat.ancient_corpse` / `ancient_remains` | Creative Loot sets the `Ancient` / `Remains` flags |
| `[jei] pagesOff` | `CompatConfig.JEI.pageOn(id)` | Creative Delight (`seasoning`, `cans`), Creative Brewing (`potion_buckets`, `tipped_arrows`) |
| Common `perf/IdleTickRules` | `IdleTickRules.addModRule(line)`, applied with `[perf] idleRandomTickRules` | Creative Delight (treated-stick tomatoes) |
| Common `[effects]`, `[magic_resistance]` | `ConfigRename.carrySections` / `mergeSections` into `creativecommon-common.toml` | Creative Brewing, from its own old file |

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
Item Trash Can screen (2026-11-23, queue section 6): SQ-CMP-34 (mixin applies, hopper insertion still deletes), -35
(boots without Trash Cans), CQ-CMP-36 (27 slots, shift-click, deleted on close), -37 (sneak opens the filter, other cans
unchanged).
Naming hooks (2026-11-23, queue section 5): SQ-CMP-27 (`[jei]` carry-over), -28 (vine shearing), -29 (idle tick
rule), CQ-CMP-30 (JEI pages), -31 (stuck-in-block line), -32 (wine tooltip), -33 (corpse names, jar families, syringe
tab). Corpse decay: SQ-CMP-10 to -13 and CQ-CMP-14 (queue section 3); sinking skeletons SQ-CMP-16; corpse screen sections
SQ-CMP-17 and CQ-CMP-18 (screenshot look-check), -19, -20.
Scratch run r1122cc (2026-11-22, [scratch_run_2026-11-22.md](testing/scratch_run_2026-11-22.md) 8, Compat 23c5050d): SQ-CMP-03 to -08, -11 to -13 PASS; SQ-CMP-01 BATCH-FAULT (typed mixin count; fixed 2026-11-22, `cmp_mixin_check.py`); sand-layer recipes without Immersive Weathering FAIL (SQ-CC-03, SQ-CC12-02, SQ-CPP-05, TODO `blockrunner #18`; guarded 2026-11-22, rerun after the Compat rebuild).
Scratch run r1123cx (2026-11-23, [scratch_run_2026-11-23.md](testing/scratch_run_2026-11-23.md) 10, Compat 2e68d8807, publish gate): 34 rows, PASS 30; SQ-CMP-01 (86 listed, 76 applied, 10 gated), -03 to -08, -11 to -13, -23 to -25, SQ-SMK-18 to -20, SQ-PA-01 to -04, SQ-MT-01, -02, SQ-BPT-01, CQ-BPT-01, -03 PASS; SQ-NS-01 FAIL (nightstand with a hopper below takes nothing, TODO `decor-and-framed-blocks #3`); SQ-SG-02, -03 FAIL (no grass spreads, controls included, TODO `creative-calendar #2`); CQ-TSL-02 shots for the owner.

Box move and squid (2026-11-24, `creative-compat-publish #8`, `#9`): SQ-CMP-09s and CQ-CMP-09 superseded by SQ-CEN-09 and
CQ-CEN-02 (the box is Creative Engineering's, [creative_engineering.md](creative_engineering.md) 4.2); squid and whales
SQ-MT-04 to -06 ([mob_block_tweaks.md](mob_block_tweaks.md) 2.10; SQ-PSM-08 superseded).

Wave 2 carve (2026-11-24, queue): SQ-MELT-21 and -22 (carry-over, merge and new file), -23 (new tag namespace), -24 (End no fire), CQ-MELT-02 (tooltip); SQ-GSH-10 to -12; SQ-SNP-06; SQ-CGE-17, -18; CQ-RPI-94 (fov carry-over), CQ-RPI-95 (Fleet-Footed fix and fov), SQ-RPI-96 (Raspberry Pie alone). Headless: `tools/test_melting.sh` 34/34.

## 5. History

- 2026-11-01: carved (wave 3). Creative Common 1.1.06 loses the classes, mixins, sections and resources; Calendar,
  Devtools, Creative Delight and Creative Brewing require `creativecompat`. Added ModList gates that were missing for
  the spout gun farmland, nightstands, sack scroll, banner atlas and monster box listeners.
- 2026-11-22: publish pass (publish_readiness.md section 4, TODO `creative-compat-publish #1` to `#6`): pack balance
  out of the jar, description corrected, `[mixins]` enable keys, version ranges for every gated mod, exact Breezy gate,
  Cozy feather gate, Create copycat holder (`surface/CreateCamo`), sinking render tag test first, hook faults logged
  once, config migrations dropped, `logOverflow` off by default, crop recipes and dimension tags in the jar; corpse
  skeletons draw Customizable Elytra. Sections 1 to 5 checked against the code and corrected 2026-11-23 (PR-CP-18: 24 top-level config tables, 86 common + 62 client mixins, 32 with an enable key, 11 pass-through tiers, hooks table).
- 2026-11-04: campfire rain, Comfort, the Campfire Rules JEI rows and `[campfire]` moved to Creative Delight
  (creative_split.md 7.9 step 1); Compat 33 -> 32 common mixins. Tests SQ-CFR.
- 2026-11-04 (creative_split.md 7.14): from Creative Common, the Create: Dreams & Desires jetpack tooltip null-level
  guard (`JetpackItemTooltipMixin`) and the falling-block water splash (`FallingBlockWaterSplashMixin`): third-party
  and vanilla behavior fixes, not interface or perf. Common keeps its interface, API and engine/perf rows (2.4).
- 2026-11-14 to 2026-11-15: pass-through blocks (blockrunner.md 3.10): `surface/Pass*`, 13 new mixins (`WallSlideMixin`
  renamed `InsideBlocksMixin`), `[surface.passthrough]`, `[surface.wthit] passThrough`, `[debug.passthrough]`; the
  tier tags `kubejs/data/creativecompat/kubejs/tags/passthrough_tags.js` ship in the jar from the 2026-11-15 00:40
  build; `[leaf_piles]` and `LeafPileSlowMixin` removed (migration above). Built, not installed.

- 2026-11-17: the flower tags' item side (removal of wild plants, Botania special flowers, the flower crown and the azalea piles from the flower tags, with the floating, planter box and flower box tags that keep them) moved from the pack folder's `animal_tags.js` into Compat's `kubejs/data/creativecompat/kubejs/tags/flower_tags.js`: one handler reads the members, adds them straight and removes them, so the order is inside Compat (plan_rules_fixes_followup.md F7; closes plan_rules_fixes.md owner item 3). Same tag content (SQ-RFX-30, -31). Built, not installed until the next Compat install.

Lines moved out of code comments (2026-11-17, plan_rules_fixes.md section 17):

- 2026-09-09: if-chain, not a switch: an enum switch compiles to a synthetic ItemPhysicSplit$1 that loads lazily and was missing at runtime (NoClassDefFoundError, crashes 2026-09-09 and 2026-09-17) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/itemphysic/ItemPhysicSplit.java)
- 2026-10-05: <p><b>Which recipes need a template.</b> Armor Trims' table ({@code SmithingMenuNew#createResult}, bytecode-read 2026-10-05) refuses a template-less recipe when the material is in a tag listed in {@code ArmortrimsApi.upgradeBaseBlockedConditions} whose condition is on - by default {@code #forge:ingots/netherite}, switched by {@code "Disable Vanilla Netherite Upgrading"} in {@code config/armor_trims.toml} (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/compat/jei/ArmorTrimsSmithing.java)
- 2026-10-05: JEI 11.8 matches on both, throws "no recipe category registered" on the first {@code addRecipes}, and both pages stay empty (seen in logs/latest.log 2026-10-05) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/compat/jei/ArmorTrimsSmithing.java)
- 2026-10-20: Which blocks qualify is {@link BlockModelIcons#verdict} (any namespace since 2026-10-20; {@code [wthit] iconExclude} opts out) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/compat/WthitBlockModelIcon.java)
- 2026-10-20: Starter Kit port (feature_writeup/feature_porting.md 1, Starter Kit 7.0 + Collective gear functions, jar retired 2026-10-20) (kubejs/data/creativecompat/kubejs/other/starter_kit.js)
- 2026-10-20: The jars are in disabledmods/ (2026-10-20) (kubejs/data/creativecompat/kubejs/other/feature_ports.js)
- 2026-10-20: A block with no pick item (the 2026-10-20 audit found 272: 70 Display Delight plates, Colored Water's 64 cauldrons, Immersive Posts' 30 post parts, Delightful's and Central Kitchen's pies, Create belt funnels...) leaves the icon empty, and an item whose model is missing shows the purple/black cube (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/compat/WthitBlockModelIcon.java)
- 2026-11-01: Creative Compat: item tags for the flower box, planter box, chalk and microblock tweaks; moved from kubejs/server_scripts/tags/misctags.js on 2026-11-01 (kubejs/data/creativecompat/kubejs/tags/misctags.js)
- 2026-11-01: Creative Compat (feature_writeup/creative_compat.md): the pack's tweaks to third-party mods that have no wider system of their own, carved out of Creative Common on 2026-11-01 (creative_split.md 7.5, 7.6) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CreativeCompat.java)
- 2026-11-01: Creative Compat; moved from kubejs/server_scripts/other/starter_kit.js on 2026-11-01 (kubejs/data/creativecompat/kubejs/other/starter_kit.js)
- 2026-11-01: Creative Compat: tags of the ported tier-1 mods (feature_writeup/feature_porting.md 1; recipes in other/feature_ports.js); moved from kubejs/server_scripts/tags/feature_port_tags.js on 2026-11-01 (kubejs/data/creativecompat/kubejs/tags/feature_port_tags.js)
- 2026-11-01: Creative Compat; moved from kubejs/server_scripts/tags/item_physics_tags.js on 2026-11-01 (kubejs/data/creativecompat/kubejs/tags/item_physics_tags.js)
- 2026-11-01: Creative Compat; moved from kubejs/server_scripts/other/flowers.js on 2026-11-01 (kubejs/data/creativecompat/kubejs/other/flowers.js)
- 2026-11-01: Creative Compat; moved from kubejs/server_scripts/other/feature_ports.js on 2026-11-01 (kubejs/data/creativecompat/kubejs/other/feature_ports.js)
- 2026-11-01: Creative Compat: decor blocks with no system of their own (feature_writeup/creative_split.md 7.6); moved from kubejs/startup_scripts/items/decor_blocks.js on 2026-11-01 (development/creativeCompat/src/main/resources/kubejs_startup/creativecompat/decor_blocks.js)
- 2026-11-19: corpse screen sections: cosmetic armor and curio slots, curio origin at death, Transfer Items to the exact curio slot, the corpse model's own cosmetic armor (plan_corpses.md 3.6, TODO `corpses #4` round 1; SQ-CMP-17, CQ-CMP-18 to -20) (corpse/CorpseSections, CorpseCurios, CorpseSectionsClient, six Corpse*Mixin, CompatConfig.Corpses)
- 2026-11-18: skeleton corpses sink in water and lava (`[corpses] skeletonsSink`, `sinkSpeed`; TODO `corpses #9`; SQ-CMP-16); the sealed mark is gone (ruling 24)
- 2026-11-18: corpse decay on world time with Nether and End rules, armor shown follows contents, wither skeleton look (plan_corpses.md 3.1, TODO `corpses #1`, `corpses #6`; SQ-CMP-10 to -13, CQ-CMP-14) (corpse/, CorpseDecayMixin, CorpseRendererMixin, CompatConfig.Corpses)
- 2026-11-01: Creative Compat: Curios slot membership for the pack's own slots (data: kubejs/data/creativeengineering/curios/; feature_writeup/curios_slots.md); moved from kubejs/server_scripts/tags/curiostags.js on 2026-11-01 (kubejs/data/creativecompat/kubejs/tags/curiostags.js)
- 2026-11-01: cropSeasons, cropOutOfSeason: creativecalendar-common.toml [wthit] (Creative Calendar, 2026-11-01) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CompatConfig.java)
- 2026-11-01: Creative Compat; moved from kubejs/server_scripts/economy/coin.js on 2026-11-01 (kubejs/data/creativecompat/kubejs/economy/coin.js)
- 2026-11-01: Creative Compat; moved from kubejs/server_scripts/other/dead_recipe_removal.js on 2026-11-01 (kubejs/data/creativecompat/kubejs/other/dead_recipe_removal.js)
- 2026-11-01: The sections moved out of Creative Common's creativeengineering-common.toml on 2026-11-01 with keys, comments and defaults unchanged; the first boot with Compat copies a player's values over ({@code swap.ConfigRename.carrySections}, called from {@link CreativeCompat}). {@code [shrapnel]}, {@code [surface]} and {@code [vein_undo]} came from Creative Engineering's file (same name, creativeengineering-common.toml) on 2026-11-06 with the TNT shrapnel, walk speed and Ore Excavation undo code; keys, comments and defaults unchanged, carried or merged the same way ({@link #ENGINEERING_SECTIONS}) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CompatConfig.java)
- 2026-11-01: Creative Compat: display names; moved from kubejs/client_scripts/namechanges.js on 2026-11-01 (kubejs/assets/creativecompat/kubejs/names.js)
- 2026-11-01: Creative Compat: block tags for the mod tweaks; moved from kubejs/server_scripts/tags/blocktags.js on 2026-11-01 (kubejs/data/creativecompat/kubejs/tags/blocktags.js)
- 2026-11-01: Creative Compat startup script (loaded from the Compat jar by the startup loader); moved from kubejs/startup_scripts/func/item_physics.js on 2026-11-01 (development/creativeCompat/src/main/resources/kubejs_startup/creativecompat/item_physics.js)
- 2026-11-01: creativeCompat: Creative Compat (mod id creativecompat, feature_writeup/creative_compat.md), carved out of Creative Common on 2026-11-01 (creative_split.md 7.5, 7.6) (development/creativeCompat/build.gradle)
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
- 2026-11-06: From Creative Engineering 2026-11-06: Ore Excavation undo rules (excavation/) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/compat/CompatMixinPlugin.java)
- 2026-11-07: from creativecommon-common.toml (interface carve, 2026-11-07) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CompatConfig.java)
- 2026-11-07: Distant Horizons' FEATURES generator hands features smaller regions than vanilla's 3x3, so every basalt column it generates in the Nether warns: 440,000 lines in 15 minutes on 2026-11-07 (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/compat/LogSpamFilter.java)
- 2026-11-07: Tested version range of the interface mods (moved with their mixins from Creative Common's CommonMixinPlugin, 2026-11-07): outside it the mod's mixins are skipped with one warning instead of crashing the boot (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/compat/CompatMixinPlugin.java)
- 2026-11-07: - no nested members (Tempad was one until 2026-11-07; now its own jar, development/backportedTempad), so Compat needs no donor jar (development/creativeCompat/build.gradle)
- 2026-11-07: One fog color for the whole Nether, a deep red ({@link #RGB}), instead of each biome's own (perf.md 19.3, owner 2026-11-07) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/compat/NetherFogColour.java)
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
- 2026-11-15: other blocks that slow you from inside, display only (owner, 2026-11-15) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/surface/StuckInBlock.java)
- 2026-11-15: [leaf_piles] slowFromLayers -> [surface.passthrough.leaf_pile] minLayers (plan_passthrough.md 3.1, 2026-11-15), then the neighbor model and the layer tier (plan_post_scratch_1115b.md C1, D: surface/PassConfigMigration) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CreativeCompat.java)
- 2026-11-15: Channel {@code creativecompat:surface}, message 1 (format {@link PassValues#FORMAT}, 2026-11-15; message 0 was the first layout and is no longer registered, so an older client drops the new message and keeps its own values, and a newer reader of another format switches the system off on the client); a side without the channel is accepted (the client then uses its own values) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/surface/PassSync.java)
- 2026-11-15: <p>Since pass-through phase 5 (2026-11-15) only untiered blocks are listed: cobwebs, barbed wire, berry bushes, propelplant, leaf piles and bamboo leaves moved to the tiers (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/surface/StuckInBlock.java)
- 2026-11-15: Botania flowers and mushrooms (owner, 2026-11-15); floating flowers are solid, left out (kubejs/data/creativecompat/kubejs/tags/passthrough_tags.js)
- 2026-11-15: <p>Was: neighbor counts of the current tick, per block position (plan_passthrough.md 7.2 fallback, scratch fixes 2026-11-15): a herd in a field or a crowd in a web block shares cells, so the second entity in a cell reads the count instead of doing the 8 or 26 block lookups again (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/surface/PassCountCache.java)
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
- 2026-11-15: The toml tables carried over from creativeengineering-common.toml, in order. {@code [cats]} and {@code [giant_squid]} are new in this file and are not carried. {@code [campfire]} moved on to Creative Delight's file (creative_split.md 7.9 step 1), which carries it from here. {@code [leaf_piles]} has no spec since 2026-11-15: carried from an old file, {@code surface.LeafPileMigration} then moves its value to {@code [surface.passthrough.leaf_pile] minLayers} and drops it (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CompatConfig.java)
- 2026-11-15: owner ruling 2026-11-15: bushes never cancel fall damage (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/surface/PassThrough.java)
- 2026-11-15: The block's tier scales the fall distance itself (fall below 1.0) and the block is not dense with {@code denseResetsFall} (that one resets anyway), or it is a bush (owner ruling 2026-11-15: no reset, sweet berry bush included): vanilla's fast-fall reset must not fire on it (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/surface/PassThrough.java)
- 2026-11-16: No per-block overrides by default (leaf layers use the passthrough_layers_open tag, owner 2026-11-16) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/surface/PassValues.java)
- 2026-11-16: golem farm cooldown and boat ice friction (plan_small_fixes_1116.md R2, R3; 2026-11-16) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CompatConfig.java)
- 2026-11-16: Layer height blocks (owner, 2026-11-16): snow layers, every Immersive Weathering leaf pile (azalea flower pile included) and Twigs bamboo leaves (kubejs/data/creativecompat/kubejs/tags/passthrough_tags.js)
- 2026-11-16: Layer blocks that never act whole (leaf piles, bamboo leaves; owner 2026-11-16) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/surface/PassThrough.java)
- 2026-11-16: Leaf layers never act whole (owner, 2026-11-16): no solidBelowFull for them (kubejs/data/creativecompat/kubejs/tags/passthrough_tags.js)
- 2026-11-16: Layer height blocks (plan_post_scratch_1115b.md D, owner 2026-11-16): snow layers, leaf piles, bamboo leaves (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/surface/PassTier.java)
- 2026-11-17: Tempad was a Compat nested member until 2026-11-07, now its own jar (development/backportedTempad); the Vinery taiga grape bush entry of StuckInBlock was dropped (no entityInside in the port) (build.gradle, StuckInBlock)
- 2026-11-18: Weathered Cardboard Box renders its own model again: KubeJS generated a cube_all model at `block/weathered_cardboard_box` that outranked the jar's JSON (the whole sheet on every face). The model ships as `block/weathered_cardboard_box_wrapped` (Design and Decor's `cardboard_box/block.json` geometry) and `decor_blocks.js` sets the blockstate (`x: 90`, Design and Decor's facing-north box); the shadowed blockstate and item model left the jar (plan_bugfix_1117 A3, TODO `textures/merges #11`; CQ-CMP-09) (development/creativeCompat/src/main/resources/kubejs_startup/creativecompat/decor_blocks.js)

Lines moved out of code comments (2026-11-22, plan_rules_fixes.md section 17):

- 2026-11-21: How much taller the second pot is than the first (owner 2026-11-21: 2 px) (development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/compat/DoublePot.java)
- 2026-11-23: Compat and Common stop naming the jars above them (plan_ready_now_1123.md 4, PR-CP-11, PR-CP-12,
  PR-CC-27, PR-CC-28): the four `[jei]` page switches became `[jei] pagesOff` (carried over once); VineShearing,
  StuckInBlock, CompoundEffectTooltip, JarMixing and the Sample Syringe world tab take their Delight and Brewing
  entries from registrations (section 2); the ancient corpse names moved to Compat's lang; Common's idle tick rule for
  treated-stick tomatoes and its `[effects]` carry-over from Brewing's file moved to those jars; Common's mixin members
  and the campfire rain accessor are `creativecommon$`, its potion log tag `[creativecommon potions]`.
- 2026-11-24: Weathered Cardboard Box moved back to Creative Engineering (ruling W16, `creative-compat-publish #8`):
  startup script, model, texture, axe tag and the Design n' Decor notice left this jar; its swap list no longer maps the
  box. Squid and whales (ruling W17, `#9`): `mobs/SquidsAndWhales`, `AmGiantSquidAvoidWhalesMixin` now cancels the
  giant squid's flee goal; `[giant_squid]` removed.
- 2026-11-24: wave 2 carve (section 1, TODO `creative-common-publish #17`, `raspberry-pie-publish #25`): melting, glass shards, sack sneak pick-up and Create: Gravity low gravity from Creative Common; Fleet-Footed fix and view setting from Raspberry Pie. Ruling P1 of `creative-common-publish #12` (Forge list entry in Common) is superseded: the entry ships in Compat.
- 2026-11-23: Item Trash Can screen (plan_ready_now_1123.md 8, R1, TODO `storage-and-item-loss #17`): right-click
  opens a 27-slot delete-on-close chest screen, sneak keeps the filter (`TrashCansItemCanUseMixin`, `TrashCanScreen`;
  section 1).



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
| `weathered_cardboard_box.png` + models | Design and Decor (MIT) recolor | moved to Creative Engineering with its notice 2026-11-24 |
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
