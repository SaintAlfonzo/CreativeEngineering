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
| 2.10 | Ghost blocks on the dedicated server (block-state ids past 2^19) | `SectionUpdateEncodeFixMixin` + client guards, `[debug.block_state_sync]` | DEPLOYED with 1.0.6 |
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
| 2.22 | IE Engineer's Hammer rotates the FD cutting board instead of running a hammer cutting recipe | `IECompat.registerHammerNoRotate`, `#creativeengineering:hammer_no_rotate` | DEPLOYED with 1.0.8, see [oven.md](oven.md) 2.5 |
| 2.23 | Server crash: bonemealing a Vinery grass slab with Immersive Weathering (`fertile` property) | Vinery port `SpreadableGrassSlabBlock` | DEPLOYED with 1.0.7 (hotfix), see [alcohol.md](alcohol.md) Part B |
| 2.24 | AppleSkin bars missing in cooking/meal slots, keg tank, recipe books, TrashSlot, Pretty Pipes, FTB Quests, Wares, Patchouli, AE2 | `MealSlotTooltip`, `FtbTooltipStacks`, `AeTooltipStacks` + client mixins; Brewin' and Vinery ports | BUILT 2026-10-20, not tested, see [tooltips.md](tooltips.md) (TT-19 to TT-30) |
| 2.25 | Botania Trinket Case holds any item; Quark sort, transfer and search in its screen | `TrinketCaseRules`, 3 `TrinketCase*Mixin`s, `[botania]`, Quark `Allowed Screens` | built, scratch test queued (SQ-TC-00 to SQ-TC-02); FX-21 to FX-24 in game |

- **Pending:** nothing built and unshipped (2.22 went out with 1.0.8, 2026-10-19). FX-01 to FX-20 are new (the entries pointing elsewhere keep their tests
  there). Findings F1 to F6 (section 4); none needs a decision except F3.
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
- **Fix:** `tools/gen_bedspreads_pattern_compat.py` and `tools/gen_elytra_pattern_compat.py` generate compat textures
  for every other installed pattern, following each mod's own cropping convention, into patcherMod's assets.
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

### 2.8 Create config text fields

Create's Configs screen (pause menu, used for every mod's Forge config including CEConfig) only had widgets for
booleans, enums and numbers; text values showed "Impl missing". `SubMenuConfigScreenMixin` adds a text field
(`StringEntry`) in the screen's private builder lambda (`lambda$init$12` in Create 0.5.1.i; fails loudly if Create
renames it). Used by e.g. `[crafting.felsic_*] potion` and the shield effect strings.

### 2.9 Bonemeal charm

Lets Do: Vinery's Winemaker set refunds bone meal; the pack generalises it to one held item. Using bone meal with an
item from `#creativeengineering:bonemeal_charms` in the **other** hand: `chance` 0.5 to give the bone meal back,
`damageChance` 0.1 to take `damagePerUse` 1 durability from the charm. Today the tag holds only the dragon hoe
([gear_tuning.md](gear_tuning.md)). Any error falls back to vanilla bone meal.

### 2.10 Ghost blocks (block-state id overflow)

- **Before:** on the dedicated server only, some block changes never showed (ghost blocks) and the client logged
  "Error executing task on Client". Vanilla packs each entry of a multi-block update as `stateId << 12 | pos` in int
  maths; for ids from 2^19 (524,288) that goes negative. This pack has 761,052 states, so the last ~237,000 (about
  4,700 blocks from Every Compat, Quark, Upgrade Aquatic, Neapolitan, Supplementaries...) broke, and the error dropped
  the rest of the packet. Single player never encodes packets, so it never showed.
- **Fix:** the server masks the packed value to 32 bits (`SectionUpdateEncodeFixMixin`); the client adds 2^20 back to a
  negative id (`SectionUpdateNullStateMixin`, `BlockUpdateNullStateMixin`), so either side alone fixes it.
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
| `CopperPickaxeItemMixin` | Everything Copper's copper pickaxe gets the pack's copper tier instead of stone (`CETiers.COPPER`) |

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
  own `use()`. Blocks in `#creativeengineering:hammer_no_rotate` are added to IE's rotation blacklist.
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
- **Fix:** `compat/TrinketCaseRules.allowed` (config `[botania] trinketCaseAnyItem`, default on): any non-empty item
  except another Trinket Case and items with `canFitInsideContainerItems() == false` (shulker boxes). The two
  anonymous-class mixins return true at HEAD when it allows the stack, else Botania's check runs; the menu mixin
  redirects the shift-click check and adds `IQuarkButtonAllowed`. `vazkii.botania.client.gui.box.BaubleBoxGui` is
  added to Quark's `Allowed Screens` (`quark-common.toml`, backed up in `configs_edited/`). Sorting runs through the
  pack's safe sort (2.20); the case item in the hotbar is a locked slot and Quark's player sort skips the hotbar.
- **Limits:** only real trinkets equip from the case (Botania's quick-swap). The `$1` class names are compiler output;
  a Botania update can move them and the mixins then fail at launch.

## 3. What the pack changes

Every entry above is a patcherMod mixin or class except 2.6 (KubeJS tag script) and 2.14 (exposureBackport). Mixins on
mod classes use `remap = false` with the mod's own names; mixins on vanilla classes use SRG names (the pack runs
reobfuscated with no refmap).

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1.** FIXED 2026-10-19 (javadoc names `[forgiving_void]` in CEConfig). **Stale javadoc in `ForgivingVoidMixin`:** it says the dials come from
  `config/creativeengineering/forgiving_void.json` via KubeJS; they're `[forgiving_void]` in CEConfig (the bridge says
  so).
- **F2. The ghost-block fix has a ceiling.** It works for ids below 2^20 (1,048,576). The pack has 761,052 states;
  about 290,000 more (a few big block mods) and the fix stops working. Watch the state count when adding mods.
- **F3. Version-pinned fixes. Your call on how to track them.** The D&D fixes (1.2c Beta Mid-Dev), the Breezy compat
  (Supplementaries 2.4.20 + Breezy 1.0.8), the config text field (Create 0.5.1.i lambda name) and the Easy Elytra
  Takeoff slot ([gear_tuning.md](gear_tuning.md) F11) each target one mod version. Most fail loudly at launch on an
  update (`require`); a checklist in [deploy.md](deploy.md) for mod updates would catch the rest.
- **F4. Thread-safe random hides the culprit.** The off-thread code using `Level.random` was never identified; the fix
  makes it safe, but the same code may misbehave elsewhere.
- **F5. Banner compat textures need a jar rebuild** for a new pattern mod: both generators write patcherMod resources,
  so they're deliberately not in `/cegen` (`generators.json` `_readme`).
- **F6.** FIXED 2026-10-19 (javadoc added). **`CopperPickaxeItemMixin` had no javadoc**, unlike every other mixin.

### Known limits

- **D&D on a dedicated server:** the drill's and saw's "invert" client option is ignored; the default applies.
- **Creative inventory** is gone for everyone (2.19), by design.

### Rejected options

- **Dropping the broken mods:** each is used; a mixin was cheaper.
- **Real banner textures on beds and elytra:** wrong placement (2.2).

## 5. File map

All in patcherMod `src/main/java/com/SaintAlfonzo/creativeengineering/` unless noted:
- 2.1 `mixin/ForgivingVoidMixin`, `forgivingvoid/ForgivingVoidBridge`; 2.2 `mixin/BedspreadsBannerPatternMixin`, `mixin/CustomizableElytraBannerPatternMixin`, `compat/BannerPatternAtlasCompat`, [tools/gen_bedspreads_pattern_compat.py](../tools/gen_bedspreads_pattern_compat.py), [tools/gen_elytra_pattern_compat.py](../tools/gen_elytra_pattern_compat.py); 2.3 `mixin/ExcavationDrillItemMixin`, `mixin/DeforesterSawItemMixin`; 2.4 `mixin/SyncGlDebugOutputMixin`.
- 2.6 [blocktags.js](../kubejs/server_scripts/tags/blocktags.js) lines 6-16; 2.7 `compat/DetailArmorBarCompat`, `compat/ArmorOverflowRenderer`, `mixin/ArmorBarRendererAccessor`, `mixin/ArmorBarRendererOverflowTickMixin`; 2.8 `mixin/SubMenuConfigScreenMixin`, `compat/StringEntry`; 2.9 `mixin/BonemealCharmMixin`, `util/BonemealCharms`, [equipmenttags.js](../kubejs/server_scripts/tags/equipmenttags.js).
- 2.10 `util/BlockStateIdOverflow`, `mixin/SectionUpdateEncodeFixMixin`, `mixin/SectionUpdateNullStateMixin`, `mixin/BlockUpdateNullStateMixin`, `devtools/BlockStateSyncDebug`, [tools/diff_block_state_ranges.py](../tools/diff_block_state_ranges.py); 2.11 `mixin/SupplementariesBreezyCompatMixin`; 2.12 `mixin/LevelThreadSafeRandomMixin`; 2.13 `compat/AsyncMapTrades`, `compat/AsyncLocatorBridge`, `mixin/LCItemsForMapTradeMixin`, `mixin/CSSellingMapMixin`.
- 2.15 `compat/LightmansTooltipStacks`, `mixin/LCEasyScreenHelperMixin`, `mixin/LCEasyGuiGraphicsMixin`; 2.16 `mixin/JeiRecipeLayoutTooltipMixin`, `mixin/JeiRecipesGuiTooltipMixin`; 2.17 `mixin/IcebergTitleBreakMixin`; 2.18 `compat/guititle/`, `mixin/ContainerTitlePlaqueMixin`, `mixin/RenderSystemShaderTextureMixin`.
- 2.25 `compat/TrinketCaseRules`, `mixin/TrinketCaseInventoryMixin`, `mixin/TrinketCaseSlotMixin`, `mixin/TrinketCaseMenuMixin`, [kubejs/client_scripts/tooltips.js](../kubejs/client_scripts/tooltips.js) (Botania section), `config/quark-common.toml` `Allowed Screens`.
- 2.19 `mixin/CreativeMenuDisableMixin`, `mixin/XpHelperMixin`, `mixin/XpPumpUpgradeWrapperMixin`, `mixin/XPFluidManagerMixin`, `mixin/MemoryStickItemMixin`, `mixin/PneumaticCraftUtilsMixin`, `mixin/IETagUtilsMixin`, `mixin/CopperPickaxeItemMixin`, `CETiers`.

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
  its screen, by shift-click and by clicking the stack onto the case item in the inventory → all accepted; a shulker
  box and a second Trinket Case → refused. Close and reopen → contents kept.
- [ ] **FX-22 Trinket case sort and search:** open the case with mixed items → Quark's sort button and search bar
  show; sort → items grouped, none lost; the sort keybind works; search dims non-matching slots; the deposit /
  restock buttons move items in and out.
- [ ] **FX-23 Trinket case equip:** a trinket in the case still quick-equips from its screen; cobblestone doesn't.
- [ ] **FX-24 Config off:** `trinketCaseAnyItem = false`, restart → only trinkets, rods and mana items fit.

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
