# Fixes and compat: small patches to other mods and to vanilla

A catalogue: one entry per fix, each with the symptom, the cause, the fix and where it lives. Fixes that belong to a
feature are documented with that feature and only pointed to here.

## 0. At a glance

| # | Fix | Where | Status |
|---|---|---|---|
| 2.1 | Forgiving Void: each void fall lands you closer to (or farther from) an anchor | `ForgivingVoidMixin`, `[forgiving_void]` | DEPLOYED (since 1.0.3) |
| 2.2 | Banner patterns from every mod on Bedspreads beds and Customizable Elytra wings | `BedspreadsBannerPatternMixin`, `CustomizableElytraBannerPatternMixin`, `BannerPatternAtlasCompat`, 2 generators | DEPLOYED |
| 2.3 | Create: Dreams & Desires drill and deforester saw crash dedicated servers | `ExcavationDrillItemMixin`, `DeforesterSawItemMixin` | DEPLOYED |
| 2.4 | Client never exits after quitting (NVIDIA + Distant Horizons) | `SyncGlDebugOutputMixin` | DEPLOYED |
| 2.5 | Join kick from Nock Enough Arrows' recipe-book tab | `NockRecipeBookSync` | DEPLOYED, see [archery.md](archery.md) (AR-17) |
| 2.6 | No axe effective on anything (D&D's ghost entries in `mineable/axe`) | [blocktags.js](../kubejs/server_scripts/tags/blocktags.js) | DEPLOYED |
| 2.7 | Detail Armor Bar: icons for unsupported armor; rows past 20 armor | `DetailArmorBarCompat`, `ArmorOverflowRenderer`, `[armor_overflow]` | DEPLOYED |
| 2.8 | Create's config screen can edit text values | `SubMenuConfigScreenMixin`, `StringEntry` | DEPLOYED |
| 2.9 | Bonemeal charm: chance to refund bone meal used with a charm in the other hand | `BonemealCharmMixin`, `#bonemeal_charms`, `[bonemeal_charm]` | DEPLOYED |
| 2.10 | Ghost blocks on the dedicated server (block-state ids past 2^19) | `SectionUpdateEncodeFixMixin` + client guards, `[debug.block_state_sync]` | DEPLOYED with 1.0.6; long-shift encoder (no 2^20 ceiling) BUILT 2026-10-26, untested (FX-47) |
| 2.11 | Supplementaries wind vane crash with Breezy 1.0.8 | `SupplementariesBreezyCompatMixin` | DEPLOYED with 1.0.6 |
| 2.12 | "Accessing LegacyRandomSource from multiple threads" server crash | `LevelThreadSafeRandomMixin` | DEPLOYED (since 1.0.3) |
| 2.13 | Server freeze from modded explorer-map trades (Lightman's Currency, Cloud Storage) | `AsyncMapTrades` + 2 mixins | DEPLOYED with 1.0.3 |
| 2.14 | Exposure login kick and missing refmap | exposureBackport `-02` | DEPLOYED with 1.0.4, see [exposure.md](exposure.md) |
| 2.15 | Lightman's Currency tooltips lose their item (AppleSkin bars missing) | `LightmansTooltipStacks` + 2 mixins | DEPLOYED with 1.0.6, see [tooltips.md](tooltips.md) (TT-18) |
| 2.16 | AppleSkin bars in JEI slots | JEI tooltip mixins | DEPLOYED with 1.0.6, see [tooltips.md](tooltips.md) (TT-17) |
| 2.17 | Legendary Tooltips underline drawn through the item model | `IcebergTitleBreakMixin` | DEPLOYED with 1.0.6, see [tooltips.md](tooltips.md) (TT-16) |
| 2.18 | GUI title plaque too short for long container titles | `TitlePlaques`, `ContainerTitlePlaqueMixin`, `[gui_title]` | DEPLOYED with 1.0.6 |
| 2.19 | Others: creative inventory off, XP fluid merge, IE recycling tag order, copper pickaxe tier | various | DEPLOYED |
| 2.20 | Quark's sort button deletes items from containers holding stacks over the max size | `QuarkSafeSort`, `QuarkSortingHandlerMixin` | DEPLOYED with 1.0.7, see [testing/fridge_sort/plan.md](testing/fridge_sort/plan.md) (FS-01 to FS-05) |
| 2.21 | Exposure viewfinder outlives its player and eats clicks inside screens | exposureBackport `-04` | DEPLOYED with 1.0.7, see [exposure.md](exposure.md) |
| 2.22 | IE Engineer's Hammer rotates the FD cutting board instead of running a hammer cutting recipe | `IECompat.registerHammerNoRotate`, `#creativedelight:hammer_no_rotate` | DEPLOYED with 1.0.8, see [oven.md](oven.md) 2.5 |
| 2.23 | Server crash: bonemealing a Vinery grass slab with Immersive Weathering (`fertile` property) | Vinery port `SpreadableGrassSlabBlock` | DEPLOYED with 1.0.7 (hotfix), see [alcohol.md](alcohol.md) Part B |
| 2.24 | AppleSkin bars missing in cooking/meal slots, keg tank, recipe books, TrashSlot, Pretty Pipes, FTB Quests, Wares, Patchouli, AE2 | `MealSlotTooltip`, `FtbTooltipStacks`, `AeTooltipStacks` + client mixins; Brewin' and Vinery ports | BUILT 2026-10-20, not tested, see [tooltips.md](tooltips.md) (TT-19 to TT-30) |
| 2.25 | Botania Trinket Case holds any item; Quark sort, transfer and search in its screen | `TrinketCaseRules`, 3 `TrinketCase*Mixin`s, `ItemBackedInventoryAccessor`, `[botania]`, Quark `Allowed Screens` | built, scratch server PASSED 2026-10-22 (rules, slots, shift-click, sort, config off); FX-21 to FX-23 client halves in game |
| 2.26 | Ore Excavation undo: cooldown, denied blocks, drops into the inventory, XP taken once | `excavation/VeinUndo`, 5 `OreExcavation*Mixin`s, `#creativecompat:vein_undo_denied`, `[vein_undo]`, OE `Auto Pickup = true` | built 2026-10-22, patcherMod 1.1.01 in `mods/`, built, scratch server PASSED 2026-10-22 for the refusal rules, cooldown, Auto Pickup, the offline guard and both config keys (SQ-GM-08, SQ-GM-05); FX-29 to FX-33 in game |
| 2.27 | Config-only (2026-10-22): JEI tooltip search behind `$`, Breezy wind particles x3, every Chalk symbol unlocked; Cosmetic Armor keep-on-death checked, unchanged | `jei-client.ini`, `breezy-client.toml`, `chalk-common.toml` (all in `configs_edited/root_config/`) | done, not deployed, untested: FX-25, FX-26 client only; FX-27 server half scratch server PASSED 2026-10-22 (SQ-GM-06); FX-28 in game |
| 2.28 | Polymorph recipe choice in the Sophisticated Backpacks crafting upgrade | `compat/SophisticatedPolymorphCompat`, `compat/SophisticatedPolymorphClient`, `SophisticatedCraftingPolymorphMixin`, `SophisticatedUpgradeOpenMixin`, `PolymorphRecipeSelectionPacketMixin` | built 2026-10-22, jar not yet in `mods/`, untested: FX-35 to FX-38 in game |
| 2.29 | Polymorph recipe choice in the IE Engineer's Crafting Table and the Pretty Pipes Crafting Terminal | `compat/CraftingMenuPolymorphCompat`, `compat/CraftingMenuPolymorphClient`, `IECraftingTablePolymorphMixin`, `PrettyPipesCraftingTerminalPolymorphMixin` | built 2026-10-22, jar not yet in `mods/`, untested: FX-39 to FX-42 in game |
| 2.31 | Upgrade Aquatic coral grows like vanilla coral (Immersive Weathering growths) | [tools/gen_ua_coral_growths.py](../tools/gen_ua_coral_growths.py), `kubejs/data/immersive_weathering/block_growths/ua_*` | BUILT 2026-10-28; scratch r1031 (2026-10-31): growth files parse (SQ-UC-01), prismarine grows in a cold ocean (SQ-UC-03, weak PASS); acan growth unproven, the setup grew no vanilla coral either (SQ-UC-02 client check pending); FX-48 |
| 2.32 | Falling blocks (anvil, sand, gravel) splash on entering water via Effective | `mixin/FallingBlockWaterSplashMixin` | BUILT 2026-10-28, untested (FX-49) |
| 2.33 | Opening chat with / (or T) sometimes typed the key into the box ("//"), MC-122477 | `ChatOpenArmMixin`, `ChatOpenCharDropMixin`, `util.ChatOpenKeyGuard` | BUILT 2026-11-12, in `mods/`, untested (FX-50) |

- **Pending:** nothing built and unshipped (2.22 went out with 1.0.8, 2026-10-19). FX-01 to FX-20 are new (the entries pointing elsewhere keep their tests
  there). Findings F1 to F6 (section 4); none needs a decision.
- **Knobs:** `config/creativeengineering-common.toml`: `[forgiving_void]`, `[armor_overflow]`, `[bonemeal_charm]`,
  `[gui_title]`, `[debug.block_state_sync]`. All live.

## 1. Overview

Most of these are crashes, freezes or visual bugs in third-party mods (or vanilla) that only show in this pack's mix
of mods or only on a dedicated server, fixed with a targeted mixin rather than by dropping the mod. Each class carries
a long javadoc with the investigation; this file summarises.

## 2. Reference

### 2.1 Forgiving Void

- **Before:** Forgiving Void (7.0.0) catches a player falling out of the world and drops them back at a fixed height
  with the same X/Z, so a player can void-fall forever, and ride repeated falls across the End.
- **Fix:** a redirect on the mod's one `teleportTo` call moves the landing spot 20-25 blocks (`minDistance`,
  `maxDistance`) toward (`direction = "TOWARD"`) or away from an anchor (`defaultAnchorX/Z` 0, 0; per-dimension
  overrides for Overworld, Nether and End, all off). Every fall now costs ground.
- **Code:** `mixin/ForgivingVoidMixin`, `forgivingvoid/ForgivingVoidBridge` (reads `CEConfig.FORGIVING_VOID`).

### 2.2 Banner patterns on beds and elytra

- **Before:** Bedspreads and Customizable Elytra only know the patterns they bundled a texture for (vanilla, Alex's
  Mobs and a few more). Patterns from Botania, Buzzier Bees, IE and MineColonies rendered as nothing. Using the
  pattern's real banner texture is wrong: both mods draw on their own model layouts, so their bundled textures are
  re-cropped copies; the real texture lands in the wrong place (an earlier version of both mixins did that and broke
  every bed).
- **Fix:** compat textures for every other installed pattern, following each mod's own cropping convention: first
  generated offline into the jar by two tools/ scripts; since 2026-11-06 built at load from the installed jars as a
  hidden client resource pack (Compat `banners/BannerPatternPack`, [creative_compat.md](creative_compat.md) 6.3).
  `BannerPatternAtlasCompat` stitches them into the block atlas; the two mixins use the mod's own texture when it
  exists and the generated one otherwise.

### 2.3 Create: Dreams & Desires drill and saw crash

- **Before:** D&D 1.2c Beta Mid-Dev's excavation drill runs, for **every** block any player breaks, a client-only
  packet send and a client-config read (`DesiresPackets...sendToServer`, `DesiresConfigs.client()`). On a dedicated
  server that crashes (the client class can't load). The deforester saw does the same when felling a tree.
- **Fix:** on a dedicated server both calls are neutralised: the packet is dropped and the "invert" flag reads as its
  default (false: drill bores and the saw fells whole trees unless sneaking). Single player is unchanged. `require` is
  set so a D&D update that moves the calls fails loudly at launch.

### 2.4 Client exit hang

- **Before:** after quitting, the window closes but the Java process stays (0 % CPU) and ATLauncher waits on it.
  Diagnosed with gdb: Distant Horizons turns on asynchronous GL debug output, the NVIDIA driver calls it from its own
  thread, LWJGL attaches that thread to the JVM, and at exit the driver joins the thread while it waits to detach from
  a JVM that's gone. Deadlock.
- **Fix:** turn on `GL_DEBUG_OUTPUT_SYNCHRONOUS` at renderer init (`RenderSystem.initRenderer`), so callbacks run on the
  render thread and no driver thread is ever attached.

### 2.5 Recipe-book join kick

See [archery.md](archery.md) 3 (Nock Enough Arrows' fletching recipe-book type created on the dedicated server).

### 2.6 Axe tag ghosts

D&D ships a `minecraft:mineable/axe` tag file naming 7 `create_dd:creative_*` blocks its jar never registers. One
missing entry makes vanilla drop the whole merged tag, so no axe was effective on anything (wood at fist speed, WTHIT
showed no tool). `blocktags.js` removes the 7 ids before vanilla resolves the tag (literal ids, since the blocks don't
exist).

### 2.7 Detail Armor Bar

- **Icons:** `DetailArmorBarCompat` registers recoloured icons (the plain iron icon tinted) or custom sheet icons
  through Detail Armor Bar's public API for modded armor sets it doesn't know (felsic, echo, dragon, Botania's manasteel /
  elementium / terrasteel / manaweave, IE faraday, bone, slime, wither, prismarine and a few recoloured ones), which
  otherwise draw as plain grey.
- **Overflow:** Detail Armor Bar draws one row of 10 icons and collapses anything past 20 points into 3 px tick marks.
  `ArmorOverflowRenderer` draws real extra rows above it with each piece's icon (`[armor_overflow] enabled`, `compact`
  tucks rows behind each other, `compactRowSpacing`), and `ArmorBarRendererOverflowTickMixin` hides the tick marks.
- **Without Detail Armor Bar:** client setup checks `ModList` before touching `DetailArmorBarCompat` (its static fields
  are detailab types, so loading the class alone threw NoClassDefFoundError; found 2026-11-06 in the minimal
  CEPortTesting instance). The overflow renderer and both mixins were already gated.

### 2.8 Create config text fields

Create's Configs screen (pause menu, used for every mod's Forge config including CEConfig) only had widgets for
booleans, enums and numbers; text values showed "Impl missing". `SubMenuConfigScreenMixin` adds a text field
(`StringEntry`) in the screen's private builder lambda (`lambda$init$12` in Create 0.5.1.i). It has no `require`, so if Create renumbers the lambda the fix silently
stops (text values show "Impl missing" again); only a changed local list fails, when the screen opens. Recheck on a
Create update: [deploy.md](deploy.md) "Mod updates". Used by e.g. `[crafting.felsic_*] potion` and the shield effect strings.

### 2.9 Bonemeal charm

Lets Do: Vinery's Winemaker set refunds bone meal; the pack generalises it to one held item. Using bone meal with an
item from `#creativeequipment:bonemeal_charms` in the **other** hand: `chance` 0.5 to give the bone meal back,
`damageChance` 0.1 to take `damagePerUse` 1 durability from the charm. Today the tag holds only the dragon hoe
([gear_tuning.md](gear_tuning.md)). Any error falls back to vanilla bone meal.

### 2.10 Ghost blocks (block-state id overflow)

- **Before:** on the dedicated server only, some block changes never showed (ghost blocks) and the client logged
  "Error executing task on Client". Vanilla packs each entry of a multi-block update as `stateId << 12 | pos` in int
  maths; for ids from 2^19 (524,288) that goes negative. This pack has 761,052 states, so the last ~237,000 (about
  4,700 blocks from Every Compat, Quark, Upgrade Aquatic, Neapolitan, Supplementaries...) broke, and the error dropped
  the rest of the packet. Single player never encodes packets, so it never showed.
- **Fix:** the server writes each entry with the shift done in long (`SectionUpdateEncodeFixMixin` replaces `write`,
  same layout), which the vanilla reader decodes for any id, so there is no state-count ceiling (since 2026-10-26;
  before, it masked vanilla's value to 32 bits, which only worked below 2^20 states). The client adds 2^20 back to a
  negative id (`SectionUpdateNullStateMixin`, `BlockUpdateNullStateMixin`), which covers an unpatched server below
  2^20 states.
- **Diagnostics:** `[debug.block_state_sync] enabled` (off) logs every id the client still can't resolve and dumps each
  block's id range on both sides for `tools/diff_block_state_ranges.py` (a block with a different number of states on
  client and server shifts every later id).

### 2.11 Supplementaries wind vane with Breezy

Supplementaries 2.4.20's Breezy integration was built against an older Breezy; with Breezy 1.0.8 the first wind vane to
tick on a client threw `NoClassDefFoundError`. `SupplementariesBreezyCompatMixin` reimplements that one method against
Breezy 1.0.8's API (wind angle per height layer, turned into a yaw).

### 2.12 Thread-safe `Level.random`

`Level#random` is a plain random that throws "Accessing LegacyRandomSource from multiple threads" if two threads use it
at once, a hard crash (Regrowth was the usual victim, the culprit some off-thread code, likely Distant Horizons'
feature generation). `LevelThreadSafeRandomMixin` builds it with vanilla's thread-safe variant instead, which retries
instead of throwing.

### 2.13 Explorer-map trades through Async Locator

Lightman's Currency's cashier maps and Cloud Storage's balloon salesman maps searched for structures on the server
thread when the offer was made: a 60 s freeze until the watchdog killed the server on a scratch run. `AsyncMapTrades`
(with `LCItemsForMapTradeMixin`, `CSSellingMapMixin`) does what Async Locator does for vanilla maps: hand out a
"Working..." map at once, search on Async Locator's thread, then fill the map in (and apply each mod's lock or
colour). See [locate.md](locate.md) and [plan_1.0.3.md](plan_1.0.3.md) AM.

### 2.14 to 2.17

- **2.14 Exposure:** login kick ("input is null") and a missing Polaroid refmap; fixed in the exposureBackport `-02` jar
  ([exposure.md](exposure.md), [plan_1.0.4.md](plan_1.0.4.md) XK, RF).
- **2.15 Lightman's Currency tooltips:** LC passes tooltips around as plain text lines, so AppleSkin (which needs the
  item) drew nothing. `LightmansTooltipStacks` tags each line with its stack and hands it back when LC draws it
  ([tooltips.md](tooltips.md)).
- **2.16 AppleSkin in JEI:** JEI's recipe slots drew tooltips without the item; the JEI tooltip mixins pass it
  ([tooltips.md](tooltips.md)).
- **2.17 Legendary Tooltips underline:** depending on listener order, Iceberg's title break landed before Legendary
  Tooltips' model spacer and the underline sat one line too high, through the item model; `IcebergTitleBreakMixin`
  moves it after the spacer ([tooltips.md](tooltips.md)).

### 2.18 GUI title plaque

GUI resource packs (Engineers-style screens) draw a fixed-width plaque behind the container title; long titles
(renamed chests, Quark chests, modded minecarts, horse names) ran past it. `ContainerTitlePlaqueMixin` and
`RenderSystemShaderTextureMixin` capture the screen's background texture, `TitlePlaques` / `TitlePlaqueLayout` find the
plaque under the title and draw it stretched, and a title wider than the whole GUI is cut short. Only screens that use
vanilla's label drawing are touched. `[gui_title] stretchPlaque`, `logOverflow` (logs every title that needed it).

### 2.19 Others

| Fix | What |
|---|---|
| `CreativeMenuDisableMixin` | creative players get the survival inventory: the creative tab screen builds a cache of every item on first open (a multi-second freeze in this pack); JEI's cheat mode covers giving items |
| XP fluid merge: `XpHelperMixin`, `XpPumpUpgradeWrapperMixin`, `XPFluidManagerMixin`, `MemoryStickItemMixin`, `PneumaticCraftUtilsMixin` | every liquid XP is Create: Enchantment Industry's experience (Sophisticated Backpacks' ratio 20 → 1 mB per point, its XP pump fills the CEI fluid, PneumaticCraft's memory stick and orb absorber use it without dividing by zero) |
| `IETagUtilsMixin` | IE's recycling picked a material from an item's tags in hash order, so `forge:rods/all_metal` could win over `forge:rods/iron` and recycle to air; now the first candidate with a real ingot tag wins |
| `CopperPickaxeItemMixin` | Everything Copper's copper pickaxe gets the pack's copper tier instead of stone (`CEQTiers.COPPER`) |

### 2.20 to 2.23

Each is documented where it belongs; one line each here.

- **2.20 Quark sort:** Quark 3.4 empties every slot and re-inserts through Forge's `InvWrapper`, which caps a slot at
  the item's max stack size. Stacks from before the 1.0.5 stack-size policy are bigger than that, so the insert fails,
  Quark's restore fails on the same stack, and the overflow plus every later slot is lost. The mixin splits stacks to
  the max size, checks that everything fits first, and does nothing when it wouldn't.
- **2.21 Exposure viewfinder:** the viewfinder is static and survived respawn, dimension change and rejoin; Forge's
  `MouseButton.Pre` also fires before an open screen gets the click. The port now drops the viewfinder when the
  ticking player has no active camera or logs out, and ignores clicks while a screen is open.
- **2.22 IE hammer:** the hammer's `onItemUseFirst` rotates any block without an IE block entity before the block's
  own `use()`. Blocks in `#creativedelight:hammer_no_rotate` are added to IE's rotation blacklist.
- **2.23 Grass slab bonemeal:** the port forwarded the slab's own state to `Blocks.GRASS_BLOCK`, which Immersive
  Weathering replaces with a block that sets `fertile`; the slab has no such property. It now passes
  `GRASS_BLOCK.defaultBlockState()`. Logs: `debug/10_18/`.

### 2.25 Trinket Case: any item, sort and search

- **Symptom:** the Trinket Case (`botania:bauble_box`, 24 slots) takes only trinkets, rods and mana items, and its
  screen has no Quark sort / transfer buttons or search bar.
- **Cause:** three Botania checks call `EquipmentHandler.isAccessory`: the case's item-backed inventory
  (`BaubleBoxItem$1.canPlaceItem`, used when a stack is clicked onto the case item and by Quark's wrappers), the menu
  slots (`BaubleBoxContainer$1.mayPlace`) and shift-click (`BaubleBoxContainer.quickMoveStack`). Quark shows its
  buttons and search bar only when the screen class is in `Allowed Screens` and the menu has 27+ non-player slots or
  implements `IQuarkButtonAllowed`; its keybinds check only the screen list.
- **Fix:** `compat/TrinketCaseRules.allowed` (config `[botania] trinketCaseAnyItem`, default on): any non-empty item,
  shulker boxes and other Trinket Cases included (the case works as a chest; user decision 2026-10-22). The one
  refusal, in both config modes, is the open case's own stack (`TrinketCaseRules.isOpenCase`, an identity check on
  the stack behind the case's `ItemBackedInventory`, read through `mixin/ItemBackedInventoryAccessor`): Botania
  doesn't override `clicked`, so a hotbar-key swap onto an empty case slot would otherwise move the open case into
  itself and lose it with its contents. The two anonymous-class mixins return false at HEAD for the open case, true
  when `allowed`, else Botania's check runs; the menu mixin redirects the shift-click check (never the menu's `box`)
  and adds `IQuarkButtonAllowed`. With the config off, shulker boxes still fit: Shulker Box Slot makes them curios
  and Botania takes every curio. `vazkii.botania.client.gui.box.BaubleBoxGui` is
  added to Quark's `Allowed Screens` (`quark-common.toml`, backed up in `configs_edited/`). Sorting runs through the
  pack's safe sort (2.20); the case item in the hotbar is a locked slot and Quark's player sort skips the hotbar.
- **Limits:** only real trinkets equip from the case (Botania's quick-swap). The `$1` class names are compiler output;
  a Botania update can move them and the mixins then fail at launch.

### 2.26 Ore Excavation undo rules

- **Symptom:** Ore Excavation's `/undo_excavation` (any player, last 3 vein mines) had no cooldown and undid any
  vein mine, chests included: a restored chest keeps its unrolled loot table, so mine-and-undo rerolled structure
  loot. The undo needs the exact drops back in the inventory, but the drops landed at the first block mined, often
  out of reach or in a spot being restored (which counts as obstructed). A restore spread over several ticks by the
  TPS guard took the XP again each tick, and a player logging out mid-restore crashed the server tick.
- **Fix:** `mixin/OreExcavationCommandUndoMixin` runs `excavation/VeinUndo.refusal` at the head of the command:
  a per-player cooldown after each successful undo (`[vein_undo] cooldownSeconds`, default 5; a normal return from
  OE's command is a success, every failure throws) and a refusal when the newest vein mine broke a block in
  `#creativecompat:vein_undo_denied` (KubeJS blocktags.js: `#forge:ores`, `#forge:storage_blocks`, ancient
  debris, spawner, every `*:budding_*`) or, with `denyBlockEntities` (default on), any block with a block entity.
  The message names the block. OE's `Auto Pickup = true` (`oreexcavation-common.toml`, backed up in
  `configs_edited/`); OE alone only spawns the drops at the player's feet, so `mixin/OreExcavationMiningAgentMixin`
  redirects the spawn in `MiningAgent.lambda$dropEverything$1` to `Inventory.add` and spawns only the remainder.
  `mixin/OreExcavationHistoryMixin` zeroes the history's XP after the first take and makes `tickRestore` wait
  (return false) while the player is offline.
- **Access:** OE's obfuscator marks every field synthetic, which javac won't reference and the Mixin processor
  can't check (build warnings "Cannot find target for @Shadow field"). Instance fields come through `@Shadow` in
  `OreExcavationSchedulerMixin` / `HistoryMixin` / `BlockHistoryMixin` (matched by name and type at runtime);
  `MiningScheduler.INSTANCE` and `ExcavationSettings.autoPickup` through reflection in `excavation/VeinUndoAccess`.
  OE's jar is in `development/creativeCommon/libs/` for compiling.
- **Limits:** only the newest vein mine is checked; a refused one stays on top of the stack, so older ones can't be
  undone until 3 newer vein mines push it out. The cooldown is in memory (resets on restart, like OE's history).
  Drops that don't fit the inventory lie at the player's feet and must be picked up before an undo. Items a
  Sophisticated Backpack pickup upgrade would catch go to the main inventory instead (direct insert, no pickup
  event). The D&D drill, PneumaticCraft drill and TreeChop have no undo.

### 2.27 Config-only changes (2026-10-22)

- **JEI tooltip search** (`ui/misc #3`): `jei-client.ini` `TooltipSearchMode = REQUIRE_PREFIX` (was `ENABLED`): a plain
  search matches names only, tooltip text needs `$`. Every other JEI search mode was already prefix-only. Client.
- **Breezy wind particles** (`gameplay/misc #11`): `breezy-client.toml` `low / medium / high_wind_frequency` 0.015 /
  0.03 / 0.045 (was 0.005 / 0.01 / 0.015). Client.
- **Chalk symbols** (`gameplay/misc #8`): `chalk-common.toml` `Skull / Heart / Pickaxe / House UnlockAdvancement = ""`
  (Cross and Check were already empty). Chalk's `SymbolUnlocking.getUnlockedSymbols` adds a symbol whose advancement
  string is empty without asking for the advancement, so every symbol is on for everyone. The pack drops every
  advancement at load, so the four could never unlock before. Server side (the server sends the list), common config.
- **Cosmetic armor on death** (`gameplay/misc #3`): checked, no change (history 2026-10-22, FX-28).

### 2.28 Polymorph in the Sophisticated crafting upgrade

Polymorph (0.46.6) only hooks the vanilla crafting table, inventory grid, smithing table and furnaces. Sophisticated
Core's crafting upgrade (`CraftingUpgradeContainer`, 0.6.4) takes the first matching recipe through its own
`RecipeHelper.safeGetRecipeFor`, and keeps it in `lastRecipe` while it still matches, so a conflicting recipe could
never be picked. Polymorph's fallback widget did attach to the backpack screen (it finds the first `ResultContainer`
slot), but at the slot's hidden position (-100, -100) and with nothing on the server listening to it.

- **Server:** `SophisticatedCraftingPolymorphMixin` makes `updateCraftingResult` skip the `lastRecipe` shortcut and
  ask `RecipeSelection.getPlayerRecipe` (the same call Polymorph puts in `CraftingMenu`). `lastRecipe` is still
  set from the answer, so crafting remainders on take are unchanged. The result is recomputed (Polymorph then sends the
  candidate list) after a selection packet (`PolymorphRecipeSelectionPacketMixin`, Polymorph's handler only calls
  `slotsChanged`, which the storage menu does not pass on), when a crafting tab opens (`SophisticatedUpgradeOpenMixin`,
  a backpack can hold several crafting upgrades), and on `PlayerContainerEvent.Open` (the upgrade's first result is
  worked out before the menu is the player's open menu, and Polymorph only sends to the open menu).
- **Client:** `SophisticatedPolymorphClient` registers a widget for every `StorageContainerMenuBase` screen, before
  Polymorph's fallback. It follows the open crafting tab's result slot each tick, sits just right of the result frame
  (above the slot it would cover the grid's bottom row), and draws and takes clicks only while a crafting tab is open.
- The choice lives in Polymorph's per-player data, as at a crafting table; shift-click crafting repeats it.
- Applies to every Sophisticated Core storage screen (backpacks; Sophisticated Storage is not installed).

### 2.29 Polymorph in the IE Engineer's Crafting Table and the Pretty Pipes Crafting Terminal

Both menus (IE `CraftingTableMenu` 9.2.4, Pretty Pipes `CraftingTerminalContainer` 1.13.6) look their crafting recipe up
themselves in `slotsChanged`, so Polymorph never chose for them; its fallback button did show above the result slot,
but changed nothing on the server.

- **Server:** each mixin redirects that `getRecipeFor` to `RecipeSelection.getPlayerRecipe`, passing the menu itself
  (Polymorph only sends the recipe list for the player's open menu). IE's lookup sits in the `ContainerLevelAccess`
  lambda `lambda$slotsChanged$0`. Polymorph's selection packet already calls `slotsChanged`, so a pick shows at once.
  `CraftingMenuPolymorphCompat` recomputes on `PlayerContainerEvent.Open`: the Crafting Terminal's grid is kept in the
  block and its first result is worked out in the menu's constructor, before the menu is open.
- **Client:** `CraftingMenuPolymorphClient` registers a widget for both menus (marker interface `PolymorphCraftingMenu`
  added by the mixins, so no class from either mod is referenced outside the mixins). The button sits 23px right of
  the result slot, beside its 26px frame; Polymorph's default spot (22px above) covers the Crafting Terminal's grid and
  the Engineer's table's title.
- The Crafting Terminal's grid is shared by everyone looking at it; each viewer's result follows their own pick.
- Remainders on take come from the first matching recipe, as at the vanilla table (Polymorph does not change
  `ResultSlot.onTake`); only matters if two conflicting recipes have different remainders.
- Not covered (audit 2026-10-22, bytecode scan of every jar for own recipe lookups): Curios and Cosmetic Armor screens
  (own 2x2 grids), Create Crafting Blueprint, the Engineers Decor lab and electrical furnaces, the Cooking for
  Blockheads and Fantasy Furniture ovens; automation (mechanical crafter, Crafty Crate, turtles, drones, compacting
  drawers). AE2 terminals are covered by Polymorphic Energistics (installed). No Forge 1.19.2 addon found for the rest.

### 2.30 Biome-tinted moss

Moss Block and Moss Carpet take the biome's grass colour (`gameplay/misc #13`), client only. Vanilla moss has no tint
index, so with `[moss] biomeTint` on (default) `block/MossTint` swaps every moss block / carpet state and item model at
bake time for `creativecompat:block/moss_block_tinted` / `moss_carpet_tinted` (tint index 0 on a grayscale copy of
the vanilla texture). Off: nothing is swapped, moss is vanilla. Read at model load (F3+T or restart).

- Tint = `0x738E3A` (the fit that turns the grayscale texture back into vanilla moss, max 13 per channel off on the
  6 texture colours) times biome grass / Lush Caves grass per channel, scaled by `[moss] tintStrength` (0-1, default
  1). Moss in Lush Caves stays vanilla green; elsewhere it moves as far as grass does (yellower in deserts and
  savannas, darker in swamps). Uses `BiomeColors.getAverageGrassColor`, so the biome blend radius and anything that
  recolours grass applies.
- Items and other places without a position get the base colour.
- No jar or resource pack in the pack overrides the vanilla moss models (checked 2026-10-23); a later pack that does
  is replaced while `biomeTint` is on.

### 2.31 Upgrade Aquatic coral growth

Immersive Weathering grows fans, wall fans, coral and rarely wet sponge into the water around each vanilla coral
block (`gameplay/misc #15`). It does this with one data file per kind, `block_growths/<kind>_coral.json`, and the five
files differ only in the kind name. `tools/gen_ua_coral_growths.py` (`/cdevgen run ua_coral_growths`) copies IW's
`brain_coral.json` once per living UA coral kind into `kubejs/data/immersive_weathering/block_growths/ua_<kind>_coral.json`.
It finds the kinds from the UA jar: every kind with a block, coral, fan and wall fan, dead kinds excluded. That gives 11
kinds: acan, branch, chrome, finger, moss, petal, pillow, prismarine, rock, silk, star.

- IW's numbers are unchanged: chance 0.0005 per random tick; no growth with 6 or more matching blocks within 2;
  sides give a wall fan 50 : wet sponge 1; up gives fan 25 : coral 25 : wet sponge 1 and is picked 3x as often as each
  side. Only water is replaced, with a 0.8 chance.
- Biome: the 10 reef kinds keep IW's `minecraft:warm_ocean`. UA 1.19.2 has no reef worldgen for them (no feature, no
  biome modifier, no mixin), so they only exist where players place them. Prismarine coral generates in every ocean
  (UA's `prismarine_coral` feature, `#minecraft:is_ocean`), so its growth uses that tag.
- Recipes: UA already turns 4 corals or fans of a kind into its block, so IW's coral block recipes are not copied.
- Dead coral blocks do not grow, as in IW. Elder prismarine coral is prismarine coral's dead form (user 2026-10-28), so
  it gets no growth either (`DEAD` in the generator).
- The Weathering and Growth JEI tab picks the files up after `/cdevgen run jei_world` (82 rows before this).

### 2.32 Falling block water splash

Entities splash when they enter water through `Entity.doWaterSplashEffect`, which the Effective mod hooks to draw its own
splash model and droplets. `FallingBlockEntity.tick` never calls `baseTick`, so the water check never runs and anvils, sand,
gravel and concrete powder (and any modded falling block) entered water silently. A scan of the 1.19.2 client jar found
the same gap only in `EndCrystal`, `HangingEntity` and `Marker`; those are not fixed. `ExperienceOrb` overrides the
splash as a no-op by design.

- `mixin/FallingBlockWaterSplashMixin` (client list): at the tail of the tick it reads the fluid at the block position
  and calls `doWaterSplashEffect` on the first tick in water. It does not call the vanilla water-state method, so falling
  blocks get no water-current pushing. Size and sound scale with the block's speed as for other entities.
- Effective's config: `splashes` in `config/effective_fg-client.toml` turns its model off; the vanilla splash sound and
  bubbles still play.

### 2.33 Chat key typed into the chat box (MC-122477)

Pressing `/` to open a command sometimes opened chat with `//` instead of `/`, with no change in timing, sometimes
back to back, sometimes alternating (T does the same with `t`). Vanilla bug MC-122477, seen on Linux/X11: chat opens
on the client tick after the key press, and GLFW normally delivers the key's character in the same event poll, while
no screen is open, so it is ignored. When the character arrives one poll later, the chat box already exists and types
it. WorldEdit's `//` command name only makes the result look like a WorldEdit command; it is not the cause
(WorldEdit registers a command literally named `/`, alias `,`; left alone).

`ChatOpenArmMixin` (HEAD of `Minecraft.openChatScreen`, SRG `m_91326_`) arms `ChatOpenKeyGuard` with the key bound
to Command (prefix `/`) or Chat. `ChatOpenCharDropMixin` disarms on any key press or repeat (`keyPress`, `m_90893_`)
and drops the first character (`charTyped`, `m_90889_`) only if: chat is open, under 250 ms since opening, the opening
key is still held, and the character is the one that key produces (`glfwGetKeyName`, so any layout and any rebind
works). Every other character types normally; holding `/` still repeats as in vanilla. Client only, no config.

## 3. What the pack changes

Every entry above is a patcherMod mixin or class except 2.6 (KubeJS tag script), 2.14 (exposureBackport) and 2.27 (config only). Mixins on
mod classes use `remap = false` with the mod's own names; mixins on vanilla classes use SRG names (the pack runs
reobfuscated with no refmap).

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1.** FIXED 2026-10-19 (javadoc names `[forgiving_void]` in CEConfig). **Stale javadoc in `ForgivingVoidMixin`:** it says the dials come from
  `config/creativeengineering/forgiving_void.json` via KubeJS; they're `[forgiving_void]` in CEConfig (the bridge says
  so).
- **F2. The ghost-block fix had a ceiling.** It worked for ids below 2^20 (1,048,576); the pack has 761,052 states.
  FIXED 2026-10-26: the server now shifts in long (2.10), so the count no longer matters with the pack's server.
  Only the client-side guard against an unpatched server keeps the 2^20 limit. FX-47.
- **F3.** FIXED 2026-10-26 (checklist in [deploy.md](deploy.md) "Mod updates", plus a preflight warning when a pinned
  jar in `mods/` changes; [TODO](todo/TODO.md) `pack/server #7`). **Version-pinned fixes.** The D&D fixes (1.2c Beta Mid-Dev), the Breezy compat
  (Supplementaries 2.4.20 + Breezy 1.0.8), the config text field (Create 0.5.1.i lambda name) and the Easy Elytra
  Takeoff slot ([gear_tuning.md](gear_tuning.md) F11) each target one mod version. Most fail loudly at launch on an
  update (`require`); a checklist in [deploy.md](deploy.md) for mod updates would catch the rest.
- **F4. Thread-safe random hides the culprit.** The off-thread code using `Level.random` was never identified; the fix
  makes it safe, but the same code may misbehave elsewhere. Hunt tool added 2026-10-26: config
  `debug.level_random.probe` (default off) swaps in `LevelRandomProbe`, which logs `[LevelRandomProbe]` stack traces of
  off-thread callers (max 20 distinct). Test SQ-LR-01 in [scratch_queue.md](testing/scratch_queue.md). Close F4 once a
  caller is named and fixed, or a long probe session logs none.
- **F5. Banner compat textures need a jar rebuild** for a new pattern mod: both generators write patcherMod resources,
  so they're deliberately not in `/cdevgen` (`generators.json` `_readme`).
- **F6.** FIXED 2026-10-19 (javadoc added). **`CopperPickaxeItemMixin` had no javadoc**, unlike every other mixin.

### Known limits

- **D&D on a dedicated server:** the drill's and saw's "invert" client option is ignored; the default applies.
- **Creative inventory** is gone for everyone (2.19), by design.

### Rejected options

- **Dropping the broken mods:** each is used; a mixin was cheaper.
- **Real banner textures on beds and elytra:** wrong placement (2.2).

## 5. File map

All in patcherMod `src/main/java/com/SaintAlfonzo/creativeengineering/` unless noted:
- 2.32 `mixin/FallingBlockWaterSplashMixin`
- 2.1 `mixin/ForgivingVoidMixin`, `forgivingvoid/ForgivingVoidBridge`; 2.2 `mixin/BedspreadsBannerPatternMixin`, `mixin/CustomizableElytraBannerPatternMixin`, `compat/BannerPatternAtlasCompat`, [tools/gen_bedspreads_pattern_compat.py](../tools/gen_bedspreads_pattern_compat.py), [tools/gen_elytra_pattern_compat.py](../tools/gen_elytra_pattern_compat.py); 2.3 `mixin/ExcavationDrillItemMixin`, `mixin/DeforesterSawItemMixin`; 2.4 `mixin/SyncGlDebugOutputMixin`.
- 2.6 [blocktags.js](../kubejs/server_scripts/tags/blocktags.js) lines 6-16; 2.7 `compat/DetailArmorBarCompat`, `compat/ArmorOverflowRenderer`, `mixin/ArmorBarRendererAccessor`, `mixin/ArmorBarRendererOverflowTickMixin`; 2.8 `mixin/SubMenuConfigScreenMixin`, `compat/StringEntry`; 2.9 `mixin/BonemealCharmMixin`, `util/BonemealCharms`, [equipmenttags.js](../kubejs/server_scripts/tags/equipmenttags.js).
- 2.10 `util/BlockStateIdOverflow`, `mixin/SectionUpdateEncodeFixMixin`, `mixin/SectionUpdateNullStateMixin`, `mixin/BlockUpdateNullStateMixin`, `devtools/BlockStateSyncDebug`, [tools/diff_block_state_ranges.py](../tools/diff_block_state_ranges.py); 2.11 `mixin/SupplementariesBreezyCompatMixin`; 2.12 `mixin/LevelThreadSafeRandomMixin`; 2.13 `compat/AsyncMapTrades`, `compat/AsyncLocatorBridge`, `mixin/LCItemsForMapTradeMixin`, `mixin/CSSellingMapMixin`.
- 2.15 `compat/LightmansTooltipStacks`, `mixin/LCEasyScreenHelperMixin`, `mixin/LCEasyGuiGraphicsMixin`; 2.16 `mixin/JeiRecipeLayoutTooltipMixin`, `mixin/JeiRecipesGuiTooltipMixin`; 2.17 `mixin/IcebergTitleBreakMixin`; 2.18 `compat/guititle/`, `mixin/ContainerTitlePlaqueMixin`, `mixin/RenderSystemShaderTextureMixin`.
- 2.25 `compat/TrinketCaseRules`, `mixin/TrinketCaseInventoryMixin`, `mixin/TrinketCaseSlotMixin`, `mixin/TrinketCaseMenuMixin`, `mixin/ItemBackedInventoryAccessor`, [kubejs/client_scripts/tooltips.js](../kubejs/client_scripts/tooltips.js) (Botania section), `config/quark-common.toml` `Allowed Screens`.
- 2.19 `mixin/CreativeMenuDisableMixin`, `mixin/XpHelperMixin`, `mixin/XpPumpUpgradeWrapperMixin`, `mixin/XPFluidManagerMixin`, `mixin/MemoryStickItemMixin`, `mixin/PneumaticCraftUtilsMixin`, `mixin/IETagUtilsMixin`, `mixin/CopperPickaxeItemMixin`, `CEQTiers`.
- 2.28 `compat/SophisticatedPolymorphCompat`, `compat/SophisticatedPolymorphClient`, `mixin/SophisticatedCraftingPolymorphMixin`, `mixin/SophisticatedUpgradeOpenMixin`, `mixin/PolymorphRecipeSelectionPacketMixin`; compile dep `development/creativeCommon/libs/polymorph-forge-0.46.6+1.19.2.jar` (copied from `mods/`, git-ignored).
- 2.29 `compat/CraftingMenuPolymorphCompat`, `compat/CraftingMenuPolymorphClient`, `mixin/IECraftingTablePolymorphMixin`, `mixin/PrettyPipesCraftingTerminalPolymorphMixin`; compile deps `libs/ImmersiveEngineering-1.19.2-9.2.4-170.jar`, `libs/PrettyPipes-1.13.6.jar`.
- 2.30 `block/MossTint`, `CEConfig.Moss`; `assets/creativeengineering/models/block/moss_block_tinted.json`, `moss_carpet_tinted.json`, `textures/block/moss_block_tinted.png` (grayscale made with a one-off PIL script: luminance / brightest pixel).
- 2.31 [tools/gen_ua_coral_growths.py](../tools/gen_ua_coral_growths.py) writes `kubejs/data/immersive_weathering/block_growths/ua_*_coral.json` (generated, do not edit).

## 6. Offline checks

- `python3 tools/diff_block_state_ranges.py` compares the client and server block-state range dumps (2.10).
- The rest need the game; most mixins fail loudly at launch if their target moved.

## 7. In-game test suite

- [ ] **FX-01 Forgiving void:** jump into the void at 1000, 0, 1000 in the Overworld → land 20-25 blocks closer to 0, 0;
  repeat → closer again.
- [ ] **FX-02 Forgiving void config:** `direction = "AWAY"` → each fall lands farther. Put it back.
- [ ] **FX-03 Bed patterns:** a bed decorated with a Botania and an IE banner pattern → the patterns show on the bed; a
  vanilla creeper pattern still looks right.
- [ ] **FX-04 Elytra patterns:** a Customizable Elytra with a Buzzier Bees banner → the pattern shows on the wings.
- [ ] **FX-05 D&D drill on the server:** on the dedicated server, break any block, then mine with the excavation drill
  → no crash; the drill vein-mines unless sneaking.
- [ ] **FX-06 D&D saw on the server:** fell a tree with the deforester saw on the dedicated server → whole tree, no crash.
- [ ] **FX-07 Client exits:** quit the game (NVIDIA, Distant Horizons on) → the Java process ends and ATLauncher comes back.
- [ ] **FX-08 Axes work:** an iron axe on an oak log → fast; WTHIT shows the axe.
- [ ] **FX-09 Armor bar icons:** wear terrasteel armor → coloured icons, not grey.
- [ ] **FX-10 Armor overflow:** a full dragon set → a second row of armor icons above the first; `compact = true` tucks it.
- [ ] **FX-11 Config text field:** Create's config screen, `[crafting.felsic_sword] potion` → editable text.
- [ ] **FX-12 Bonemeal charm:** dragon hoe in the off hand, bone meal on saplings 20 times → about half refunded; the hoe
  loses a little durability.
- [ ] **FX-13 Ghost blocks:** on the dedicated server, break and place Quark and Every Compat blocks in a section with
  other changes (an explosion near them) → every change shows; no "Error executing task on Client" in the client log.
- [ ] **FX-14 Wind vane:** place a Supplementaries wind vane with Breezy installed → it turns, no crash.
- [ ] **FX-15 Random:** a few hours on the server with Distant Horizons generating → no LegacyRandomSource crash.
- [ ] **FX-16 Map trades:** level a Lightman's Currency cashier to its map trade in unexplored land → "Working..." map,
  then a real map, no server freeze.
- [ ] **FX-17 Title plaque:** rename a chest to a 30-character name, open it with the GUI pack → the plaque stretches;
  `latest.log` has the overflow line.
- [ ] **FX-18 Creative inventory:** creative mode, press E → the survival inventory, no freeze.
- [ ] **FX-19 XP fluid:** fill a Sophisticated Backpack XP pump, drain to a Create tank → Create's liquid experience, 1 mB
  per point.
- [ ] **FX-20 IE recycling:** recycle an iron rod in the IE arc furnace (or JEI) → iron, never air.
- [ ] **FX-21 Trinket case, any item:** put cobblestone, a sword and a stack of food in a Trinket Case by clicking in
  its screen, by shift-click and by clicking the stack onto the case item in the inventory → all accepted, a shulker
  box and a second Trinket Case too. With the case open, hover an empty case slot and press the hotbar key of the
  case itself → nothing moves. Close and reopen → contents kept. (Scratch server 2026-10-22: canPlaceItem, slot
  mayPlace and shift-click PASS for every item, the open case refused; the clicks themselves need a client.)
- [ ] **FX-22 Trinket case sort and search:** open the case with mixed items → Quark's sort button and search bar
  show; sort → items grouped, none lost; the sort keybind works; search dims non-matching slots; the deposit /
  restock buttons move items in and out. (Scratch server: menu is `IQuarkButtonAllowed`, Quark's transfer handler
  accepts it, a sort keeps every count and packs to the front, NBT read back 132 of 132: PASS.)
- [ ] **FX-23 Trinket case equip:** a trinket in the case still quick-equips from its screen; cobblestone doesn't.
- [x] **FX-24 Config off:** `trinketCaseAnyItem = false`, restart → only trinkets, rods, mana items and curios
  (shulker boxes, through Shulker Box Slot) fit; the open case still refused. Scratch server PASS 2026-10-22.
- [ ] **FX-25 JEI tooltip search needs `$`** (client only): in JEI type `edible`: no items match by tooltip text alone; `$edible`
  matches. Name search still works without a prefix. `jei-client.ini TooltipSearchMode = REQUIRE_PREFIX`
  (2026-10-22).
- [ ] **FX-26 Breezy wind particles** (client only): above y 83 in the open (sea level + 20), wind particles show about 3 times as
  often as before (`breezy-client.toml` frequencies x3: 0.015 / 0.03 / 0.045, 2026-10-22).
- [ ] **FX-27 Chalk symbols unlocked** (the server's unlock list: scratch server PASSED 2026-10-22, SQ-GM-06, every special symbol unlocked for a new fake player; the symbol screen and drawing:
  client): a new player with no advancements opens the chalk symbol menu (sneak-use
  chalk): Skull, Cross, Heart, Check, Pickaxe and House are all selectable and draw. `chalk-common.toml` every
  `*UnlockAdvancement = ""` (2026-10-22).
- [ ] **FX-28 Cosmetic armor kept on death** (real player death, in game): wear a cosmetic helmet, die: it is still in the cosmetic slot after
  respawn, not in the corpse. Then set `CosArmorKeepThroughDeath = false`, restart, die: it is in the corpse and goes
  back to the cosmetic slot when the corpse is opened (no config change shipped; checks the existing setup).
- [ ] **FX-29 Drops into the inventory** (vein mining needs the client keybind: in game; Auto Pickup on: scratch server PASSED 2026-10-22,
  SQ-GM-08): vein-mine 20 stone or dirt with room in the inventory: every drop is in the
  inventory, none on the ground. With a full inventory: the drops lie at your feet.
- [ ] **FX-30 Undo works** (in game): right after FX-29 (inventory case), `/undo_excavation`: the blocks come back and the
  drops leave the inventory; message "Excavation undone" (OE's text).
- [ ] **FX-31 Cooldown** (rule and message: scratch server PASSED 2026-10-22, SQ-GM-08; the real command: in game): vein-mine twice, undo, undo again at once: refused, "You can undo another vein mine in 5 s"
  (or 4). After 5 s it works. A refused undo (FX-32) starts no cooldown.
- [ ] **FX-32 Denied blocks** (rule and message per block: scratch server PASSED 2026-10-22, SQ-GM-08, iron ore, deepslate coal ore, diamond block, budding amethyst, spawner, ancient debris, chest refused; the real command: in game): vein-mine iron ore (or coal ore, deepslate ores), undo: refused, "This vein mine can't
  be undone: it broke Iron Ore." Same with a few Blocks of Iron, budding amethyst (silk-touch-free mining, it
  drops nothing), and two chests side by side.
- [ ] **FX-33 XP once** (in game; the offline-player guard on the same restore: scratch server PASSED 2026-10-22, SQ-GM-08): note your XP, vein-mine sculk without Silk Touch (not denied, drops XP; ores are denied),
  note the gain, undo: your XP drops by that gain once, not more (a large vein mine restores over several ticks).
- [x] **FX-34 Config** (both keys: scratch server PASSED 2026-10-22, SQ-GM-05, chest allowed and no cooldown after the restart): `[vein_undo] cooldownSeconds = 0` in `creativeengineering-common.toml`, restart: undos back to
  back work. `denyBlockEntities = false`: two chests can be undone.
- [ ] **FX-35 Backpack Polymorph choice** (in game): backpack with a crafting upgrade, open its tab, fill the grid with
  a recipe that has two outputs (e.g. a conflicting plank or slab recipe; JEI shows both). The Polymorph button
  shows right of the result slot; pick the other output: the result slot changes, taking it gives that item and
  uses the grid as that recipe does. Shift-click crafts the picked one repeatedly.
- [ ] **FX-36 Button follows the tab** (in game): close the crafting tab: the button is gone and its spot takes no
  clicks. Reopen: button back next to the result slot. With an unambiguous recipe the button stays hidden.
- [ ] **FX-37 Reopen and second upgrade** (in game): leave a conflicting recipe in the grid, close and reopen the
  backpack: button and list come back without touching the grid. With two crafting upgrades holding different
  conflicting grids, switching tabs shows each grid's own list.
- [ ] **FX-38 No regressions** (in game): normal crafting in the upgrade (including a recipe with a remainder, e.g. a
  bucket recipe), the vanilla crafting table's Polymorph button, and JEI transfer into the upgrade grid all work.
- [ ] **FX-39 Engineer's Crafting Table choice** (in game): fill the IE Engineer's Crafting Table grid with a recipe
  that has two outputs (JEI shows both). The Polymorph button shows right of the result frame, clear of the title;
  pick the other output: the result changes, taking it gives that item. Shift-click crafts the picked one repeatedly.
  Close and reopen with the grid still filled: the button and list come back without touching the grid.
- [ ] **FX-40 Crafting Terminal choice** (in game): same in a Pretty Pipes Crafting Terminal (needs a pipe network).
  The button sits right of the result frame and covers no grid slot or the Request button. Reopen with the grid
  filled: the list comes back.
- [ ] **FX-41 Shared terminal** (in game, two players): both open one Crafting Terminal with a conflicting grid; each
  picks a different output: each sees and takes their own pick.
- [ ] **FX-42 No regressions** (in game): plain crafting in both (no button with an unambiguous recipe), the vanilla
  crafting table's Polymorph button, JEI transfer into both grids, Crafting Terminal ghost items and Request button.
- [ ] **FX-43 Moss follows the biome** (in game, client): place moss block and moss carpet in Plains, Desert, Swamp
  and Lush Caves: they match the grass shade of each (desert yellower, swamp darker, Lush Caves the vanilla green).
  Across a biome border the colour blends like grass. Breaking particles share the tint.
- [ ] **FX-44 Moss items and off switch** (in game, client): moss in the inventory, JEI and in hand is the vanilla
  green, not gray. Set `[moss] biomeTint = false`, F3+T: placed moss is vanilla everywhere. `tintStrength = 0.5`, F3+A:
  moss shifts half as far.
- [ ] **FX-45 Moss renders with shaders and Embeddium** (in game, client): no z-fighting, no missing texture, carpet
  sides still 1 px.
- [ ] **FX-47 Ghost blocks after the long-shift encoder** (scratch server + client, then the live server): repeat
  FX-13 (an explosion among Quark / Every Compat blocks, high state ids) and a few vanilla blocks (low ids): every
  change shows; no "Error executing task on Client"; with `[debug.block_state_sync] enabled` no unresolved id.
- [ ] **FX-48 Upgrade Aquatic coral grows** (scratch server, then in game): in a warm ocean, `/gamerule randomTickSpeed
  1000`, place an acan coral block under water with open water around it: within a few minutes acan wall fans appear
  on its sides and acan fans or acan coral on top, rarely a wet sponge; growth stops once 6 or more surround it. A
  prismarine coral block in a cold or plain ocean grows prismarine fans the same way; an acan block there does not.
  A dead acan coral block and an elder prismarine coral block grow nothing. After `/cdevgen run jei_world`, the Weathering and Growth tab has a row per UA
  kind.
- [ ] **FX-49 Falling block splash** (in game, client): over a pool, drop an anvil from height (`/summon falling_block ~ ~20 ~ {BlockState:{Name:"minecraft:anvil"},HurtEntities:1b}`) and sand from 2 blocks up. Each splashes once on entering the water (Effective splash model, droplets, splash sound) and the high drop splashes larger. No splash when it lands on dry ground; the block is not pushed sideways by the water.
- [ ] **FX-50 Chat key not typed** (in game, client): press `/` 30 times to open chat (Escape between), at normal
  and fast tapping. Chat always opens with a single `/`. Same with T: always empty. Type `/` again in an open chat:
  a second `/` appears. Hold `/` in an open chat: it repeats. Rebind Command to `.` in Controls: `.` opens chat with
  `/` and no `.`. No error naming `ChatOpen` in `logs/latest.log`.

## 8. History

- By 2026-09-27 (initial git commit): forgiving void, banner patterns, D&D crash fixes, client exit hang, axe ghosts,
  armor bar, config text field, bonemeal charm, thread-safe random, creative menu, XP merge, IE tags, copper pickaxe;
  shipped with 1.0.3.
- 2026-10-02: map trades through Async Locator (1.0.3); Exposure kick and refmap (1.0.4).
- 2026-10-05 to 10-11: ghost blocks, Breezy compat, tooltip fixes, title plaque (staged for 1.0.5, shipped in 1.0.6).
- 2026-10-18: this writeup; findings F1 to F6; tests FX-01 to FX-20. No code changed.
- 2026-10-19: catalogue entries 2.20 to 2.23 for the fixes in 1.0.7 (Quark sort, Exposure viewfinder, grass slab
  crash) and 1.0.8 (IE hammer); each keeps its tests in its own writeup.
- 2026-10-19: F1 and F6 fixed (ForgivingVoidMixin javadoc, CopperPickaxeItemMixin javadoc; source only, ship with the next patcherMod build).
- 2026-10-22: 2.25 Trinket Case any item plus Quark sort / search (`recipes/magic #3`); tests FX-21 to FX-24.
- 2026-10-22: 2.25 scratch server run (`tools/trinket_case_probe.js`): shulker boxes were accepted even with the
  exclusion, through Botania's curio check. User decision: any item, shulker boxes and other cases included. The open
  case's own stack is refused instead (hotbar-swap loss). patcherMod 1.1.01 md5 37bb7049, all checks PASS, config
  on and off.
- 2026-10-22: chalk symbols always unlocked (`gameplay/misc #8`): Chalk's unlock advancements never fire because the
  pack drops every advancement, so Skull, Heart, Pickaxe and House could not be unlocked. Config only; FX-27.
- 2026-10-22: cosmetic armor on death (`gameplay/misc #3`) checked, no change: Cosmetic Armor's
  `CosArmorKeepThroughDeath` (on) is the keep option; when off, its drops join `LivingDropsEvent` before Corpse
  collects at LOWEST, and Cosmetic Armor x Corpse Compat puts them back in the cosmetic slots. User decision: no
  second "corpse takes it anyway" option, keep the default. FX-28.
- 2026-10-22: 2.26 Ore Excavation undo rules (`gameplay/misc #12`): cooldown, denied-block tag, block-entity
  refusal, Auto Pickup on with drops inserted straight into the inventory, XP taken once, offline-player crash
  guard. Built, not run in game; FX-29 to FX-34.
- 2026-10-22: 2.27 written up (the JEI, Breezy and Chalk config changes had only tests and history lines); test
  lines marked client / scratch / in game; scratch queue entries SQ-GM-06 and SQ-GM-08 (`tools/gear_misc_probe.js`).
- 2026-10-22 (second bulk run): SQ-GM-05, SQ-GM-06, SQ-GM-08 PASS on the scratch server. FX-34 done; FX-27, FX-29 to
  FX-33 keep their in-game halves.
- 2026-10-22: 2.28 Polymorph in the Sophisticated crafting upgrade; tests FX-35 to FX-38. Built, not run in game.
- 2026-10-22: 2.29 Polymorph in the IE Engineer's Crafting Table and the Pretty Pipes Crafting Terminal, after an audit of
  crafting screens Polymorph misses (gaps listed in 2.29); tests FX-39 to FX-42. Built, not run in game.
- 2026-10-23: 2.30 biome-tinted moss (patcherMod 1.1.02, in `mods/`); tests FX-43 to FX-45. Built, not run in game.
- 2026-10-24: planned from the scratchpad: IW growths for Upgrade Aquatic coral (`gameplay/misc #15`). No code changed.
- 2026-10-26: F2 fixed (TODO `server #6`): `SectionUpdateEncodeFixMixin` writes the multi-block packet with a long
  shift instead of masking, no 2^20 ceiling. patcherMod 1.1.03 in `mods/`, not run; FX-47.
- 2026-10-26: F3 closed: version-pinned fixes tracked in [deploy.md](deploy.md) "Mod updates" plus a staging warning; 2.8 corrected (the text-field mixin is silent on a renamed lambda, not loud).
- 2026-10-28: 2.31 Upgrade Aquatic coral growth (`gameplay/misc #15`): 11 IW growth files from a `/cdevgen` generator; prismarine in every ocean, elder prismarine treated as dead. Data only, not run; FX-48.
- 2026-10-28: 2.32 falling block water splash: client mixin calls `doWaterSplashEffect` once on entering water (Effective draws it); FallingBlockEntity only. Built, not run; FX-49.
- 2026-11-12: 2.33 chat key typed into the chat box (scratchpad "annoying"): MC-122477, not WorldEdit. Client guard drops the stale opening-key character. Creative Common built and installed, not run; FX-50.

## Owner ideas (scratchpad 2026-10-26)

- Symbols on Xaero's maps: `ui/misc #5` (no detail yet).

## Client checks after 1.1.02 (N-01 to N-09, moved from the scratchpad 2026-10-26)

Checks for the uncommitted work after the 1.1.02 commit. Startup scripts and the removed-items list changed: restart the
client, `/reload` is not enough. `[-]` and `[/]` are the owner's partial marks, kept as written.

- [-] **N-01 Egg Fried Rice name:** Farmer's Delight's Fried Rice reads "Egg Fried Rice" in the inventory, JEI and WTHIT; the
  Display Delight placed copy reads "Placed Egg Fried Rice".
- [-] **N-02 Glycerol tooltip:** hover Glycerol with no Shift: the aqua line "Tastes sweet!" shows and the "Hold Shift" hint is gone.
  Other PneumaticCraft items still show "Hold Shift"; the Liquid Experience Bucket tooltip still reads "Experience in liquid form."
- [-] **N-03 Bedrolls gone:** search JEI and every creative tab for "bedroll": nothing. No Leatherworker offers one (check a few).
- [-] **N-04 Abnormals Delight leftovers gone:** JEI and creative: no Passion Aloe Nectar, Cherry Cream Soda, Dune Platter,
  Yucca Gateau Slice, Fruit Beignet; no Fruit Beignet recipe page.
- [ ] **N-05 Luck brewing in JEI:** Potion of Luck and Potion of Bad Luck (and their long forms) each list one pack route
  (clover or silverfish bottle on awkward, redstone to lengthen); no Buzzier Bees duplicate, no strong or corruption route
  (compare EF-05 in [effects.md](../effects.md)).
- [ ] **N-06 No-effect potions:** JEI shows no bucket or tipped-arrow recipe for awkward, thick, mundane or water; bottling
  recipes for awkward, thick and mundane remain. Right-click an awkward potion bucket on the ground: nothing spawns and the bucket is kept.
  An effect bucket (e.g. Speed) still spawns rings, becomes an empty bucket and goes on a 1 second cooldown.
- [ ] **N-07 Driftwood bark:** JEI: driftwood log and wood stripping on the cutting board give Immersive Weathering's driftwood bark
  (not Abnormals Delight's); Create cutting and the pack sawmill still list their driftwood recipes.
- [ ] **N-08 Quark Engineering stairs:** the sawmill recipes for the quark trio (blossom, ancient, azalea) stairs in JEI are the
  pack's only ones; the bookshelf recipes remain.
- [ ] **N-09 Food types:** Cheese Pasta and the sweet entries changed in `food_types.json`: hover them, the type line and AppleSkin
  values match their neighbours (spot check three).
