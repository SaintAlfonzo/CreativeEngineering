# WTHIT lines: the pack's WTHIT plugins (icons, growth, contents, hives, state, campfires, Jade ports, keg)

This is the hub for everything the pack adds to WTHIT's look-at overlay. Run Speed, Growth and the coaster have full
writeups elsewhere. This file covers the shared plugin wiring, the block model icon, the contents, hive, state,
campfire, sheetmetal tank, kitchen station and keg lines, and the coverage audit (section 4.2).

## 0. At a glance

| Part | Status | Full detail |
|---|---|---|
| Plugin wiring: eleven patcherMod plugins in `waila_plugins.json` (six client-only, five with a server half; the eleventh, `remainder_buffer`, added 2026-10-21) + one in the Brewin' port | BUILT 2026-10-20 (patcherMod 1.1.01 and Brewin' `-03` in `mods/` and `server_mods/`); every plugin initialises on a dedicated scratch server and a scratch client | here, 3 |
| "Run Speed" line (BlockRunner tiers, camo, sticky, sinking) | DEPLOYED (BR-44 to BR-47 pending) | [blockrunner.md](blockrunner.md) 3.8 |
| "Growth" line for the 62 plants WTHIT skips, plus "(Harvestable)" | DEPLOYED with 1.0.6; CR-59 to CR-66 pending | [crops.md](crops.md) 2.9 |
| Block model icon for every mod (272 blocks), substitute items, Forge's hit-aware pick block | BUILT 2026-10-20; DD part DEPLOYED with 1.0.4 | here, 2.2 |
| Coaster: "Coaster: 3 / 4" + the items on it | BUILT 2026-10-20 | [alcohol.md](alcohol.md) 12 |
| Jar contents: "Contents: Honey (6 / 12)", potion effects under it, goblet name only | BUILT 2026-10-20; honey and cookie lines seen in a world (scratch server) | here, 2.3 |
| Hive: "Bees: 2 / 3" on top, "Honey Level: 3 / 5" | BUILT 2026-10-20 | here, 2.4 |
| State lines: servings, slices, eggs, hatching, cauldron level, charges, pickles (213 blocks) | BUILT 2026-10-20; eggs and cauldron level seen in a world | here, 2.5 |
| Campfire: "Burns forever", "Rain: Puts it out / Going out in 6s / Safe (fuel block below)" | BUILT 2026-10-20; burn, douses and safe lines seen in a world | here, 2.6 |
| Pot, kettle, oven leftover slots: "Holding: 3 Glass Bottle, 1 Bucket", red "Waiting: leftover slots full" | BUILT 2026-10-21 (patcherMod 1.1.01, not deployed); not seen in game | here, 2.9 |
| Jade-only ports: IE sheetmetal tank contents on every part, Applied Cooking kitchen station | BUILT 2026-10-20 | here, 2.7 |
| Brewin' keg: temperature, "Fermenting: Beer 45%", "Must Be Cold" (in the Brewin' port) | BUILT 2026-10-20 | here, 2.8 |
| Vinery lines (grapevine pot, barrel, press, storage, lattice vines) | BUILT in the Vinery port's own plugin (`vinery:info`) | [alcohol.md](alcohol.md) B4.6 |

- **Pending:** WL-01 to WL-33 in game. The scratch server run (section 6) proved every plugin loads on both sides
  and showed seven lines in a world before the last round of changes; everything else is unseen.
- **Findings:** F1 to F3 resolved; the coverage audit's F4 to F10 are all built (F9 on 2026-10-21).
- **Files to open:** [waila_plugins.json](../development/patcherMod/src/main/resources/waila_plugins.json),
  [compat/](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/),
  [state_lines.json](../development/patcherMod/src/main/resources/assets/creativeengineering/state_lines.json),
  [KegWthit.java](../development/backportedBrewin/src/main/java/umpaz/brewinandchewin/integration/wthit/KegWthit.java),
  [tools/wthit_icon_audit.py](../tools/wthit_icon_audit.py).
- **Knobs:** WTHIT's plugin toggles (`config/waila/plugin_toggle.json5`) and options; the pack's `[wthit]`,
  `[surface.wthit]`, `[crop_growth]` and `[coaster]` sections in `config/creativeengineering-common.toml`.

## 1. Overview

WTHIT 5.36.1 (Forge) draws a box at the top of the screen for the block or entity under the crosshair: icon, name,
mod, and lines from plugins. Its icon is the block's vanilla clone stack (`Block#getCloneItemStack(level, pos,
state)`), so a block with no item, or an item with no model, gets an empty slot or the purple/black cube. Its own
plugins cover vanilla only: crop growth for `CropBlock`s, a honey level number on beehives, fluid bars for the
vanilla water and lava cauldrons, and generic item / fluid / energy lists for block entities that expose Forge
capabilities. WTHIT Harvestability 2.1.0 adds the tool line. Mods that wrote their integration for Jade show
nothing under WTHIT; 2.6 to 2.8 port the ones that matter.

## 2. Reference

### 2.1 The plugins

| Plugin id | Class | Side | Needs | What it adds |
|---|---|---|---|---|
| `creativeengineering:run_speed` | `surface/WthitRunSpeed` | client | `blockrunner` | "Run Speed: 1.50× (Asphalt)" ([blockrunner.md](blockrunner.md) 3.8) |
| `creativeengineering:crop_growth` | `crops/WthitCropGrowth` | client | nothing | "Growth: 67%", "(Harvestable)" ([crops.md](crops.md) 2.9) |
| `creativeengineering:block_model_icon` | `compat/WthitBlockModelIcon` | client | nothing | the icon fix, 2.2 |
| `creativeengineering:coaster` | `compat/WthitCoaster` | client | `brewinandchewin` | coaster lines ([alcohol.md](alcohol.md) 12) |
| `creativeengineering:holder_contents` | `compat/WthitHolderContents` | client | `supplementaries` | "Contents: ...", 2.3 |
| `creativeengineering:hive` | `compat/WthitHive` | both | nothing | "Bees", "Honey Level", 2.4 |
| `creativeengineering:state_lines` | `compat/WthitStateLines` | client | nothing | servings, slices, eggs..., 2.5 |
| `creativeengineering:campfire` | `compat/WthitCampfire` | both | nothing (Unlit Campfire optional) | burn and rain lines, 2.6 |
| `creativeengineering:sheetmetal_tank` | `compat/WthitSheetmetalTank` | both (server data only) | `immersiveengineering` | tank fluid bar, 2.7 |
| `creativeengineering:kitchen_station` | `compat/WthitKitchenStation` | both | `appliedcooking`, `ae2` | online / offline, 2.7 |
| `creativeengineering:remainder_buffer` | `compat/WthitRemainderBuffer` | both | nothing (pot / kettle parts only with `farmersdelight` / `farmersrespite`) | leftover slots, 2.9 |
| `brewinandchewin:keg` (Brewin' port) | `integration/wthit/KegWthit` | both | nothing | keg lines, 2.8 |

"Both" plugins have a server half that sends data the client doesn't otherwise get; WTHIT and the two mods are on
both sides in this pack (`mod_sides.md`). Client-only plugins need nothing on the server.

### 2.2 Block model icons

**The rule** (`BlockModelIcons.verdict` plus `WthitBlockModelIcon`, any namespace):

1. If the block's vanilla clone stack has a working item model: WTHIT's core icon.
2. If `[wthit] iconExclude` lists the namespace or block id: WTHIT's icon.
3. If `[wthit] iconItems` maps the block to an item: that item. Default `create_dd:powered_flywheel=create:flywheel`
   (D&D's powered flywheel has no item and is drawn by a block-entity renderer).
4. Forge's hit-aware pick block (what middle-click uses): blocks that hold several things answer with the part looked
   at. Supplementaries' skull pile shows the upper or lower skull; WTHIT only asks the vanilla method.
5. If the block renders a baked model of its own: that model, facing north, stack count kept, in an 18 × 18 slot.
6. Otherwise WTHIT's (empty) icon.

Priority 1000, ahead of WTHIT's core icon (1100). Skull candles needed nothing: their vanilla pick item reads the
block entity (the skull) in a real world; the empty-world audit only made them look blank.

**The audit (2026-10-20, `/cedumpicons`):** 14,259 blocks: 13,829 item icons, 272 drawn as their model, 158 not
drawable, 0 errors, 0 items with a missing model. The name audit's `hasItem: false` (964) over-reports: most of
those blocks override their pick item.

| Namespace | Model icons | Examples |
|---|---|---|
| displaydelight | 70 | the item-less plates |
| colored_water | 64 | dyed water cauldrons |
| immersiveposts | 30 | post and truss parts |
| create | 17 | belt funnels, copycat bars, rope, pulley magnet, piston head, bogeys |
| delightful | 11 | placed pies |
| 33 more | 80 | coin stacks, SecurityCraft fields, Incubation nests, Central Kitchen pies, IE core sample... |

The 158 not drawable: about 130 fluid blocks (WTHIT's fluid icon covers them), air, light, portals, moving pistons,
and Create's fake track and Steam & Rails' casing collision (technical). `gen_display_delight_models.py` still writes
DD's 89 missing item models for JEI; the deploy preflight warns when they're stale.

### 2.3 Jar contents (Supplementaries jars, goblets, cages)

| In the holder | Line |
|---|---|
| a soft fluid | "Contents: Honey (6 / 12)": bottles against the tank's capacity (jar 12, `[functional.jar] capacity`) |
| a one-bottle holder (goblet) | "Contents: Water": full or empty is all there is |
| a potion | "Contents: Splash Potion of Healing (1 / 12)", then one line per effect as the potion's own tooltip writes it: "Instant Health II", "Regeneration (1:30)"; lingering durations at vanilla's ¼; beneficial blue, harmful red. `[wthit] holderPotionEffects` |
| cookies | "Contents: Cookie (5 / 12)" (WTHIT's extra plugin also shows the cookie as an item line); a mixed jar "Contents: 3 Cookie, 2 Lemon Cookie (5 / 12)" ([mixed_jars.md](mixed_jars.md), MJ-08) |
| a captured mob | "Contents: Bee" |

Client-side: the holders sync their contents for their renderers. `SoftFluidTank.capacity` is read by reflection
(Moonlight 2.3 has no getter). Off with `[wthit] holderContents`. Soups in jars: built 2026-10-22, [mixed_jars.md](mixed_jars.md) section 9
(TODO `dishes/ingredients #13`). Mixed jars: built 2026-10-21, [mixed_jars.md](mixed_jars.md).

### 2.4 Hive bees and honey

Every `BeehiveBlock` (beehive, bee nest, Upgrade Aquatic's driftwood and river beehives). The server half writes
`getOccupantCount()` (vanilla never syncs a hive's bees to the client).

- **"Bees: 2 / 3" on top.** WTHIT's own occupant-names line ("2 Bee", on in the pack's player config) is replaced by
  it; with no names line (option off, or no bees in) it takes the honey line's slot and honey moves under it. The
  order is always Bees, then Honey Level. `[wthit] hiveBees`.
- **"Honey Level: 3 / 5".** Still follows WTHIT's "Show Beehive Honey Level" option. `[wthit] hiveHoneyMax`.

### 2.5 State lines

`compat/WthitStateLines` (client), rules in `assets/creativeengineering/state_lines.json` (copy to
`kubejs/assets/creativeengineering/` to override; pure logic in `compat/StateLines`). Each rule: `blocks` (ids, globs,
`#tags`), `except`, an integer `property`, a `label` (lang `creativeengineering.wthit.state.<label>`) and a `mode`.
Rules apply in order; a later rule whose label or property the block already shows is skipped.

| Line | Blocks (2026-10-20 dry run) | Mode |
|---|---|---|
| "Servings: 3 / 4" | 10 feasts: FD (roast chicken, ham, shepherd's pie, rice roll, stuffed pumpkin), Corn Delight, Fruits Delight, Nether's and End's Delight stuffed blocks | count (servings left) |
| "Servings: 4 / 6" | Rustic Delight's 3 pancake stacks | taken (servings eaten) |
| "Slices: 2 / 4" | Brewin' cheese wheels and pizza | plus_one (a last slice at 0) |
| "Slices: 5 / 7" | 38 cakes and pies (`bites`): vanilla cake, FD, Delightful, Central Kitchen, Neapolitan, Create Additions, Farmer's Respite, Supplementaries, Golden Food | taken |
| "Eggs: 3 / 6" | 12: Incubation and Incubation Additions egg nests, turtle egg, Alex's Mobs crocodile and terrapin eggs | count |
| "Hatching: 1 / 2" | turtle egg, crocodile and terrapin eggs | count |
| "Level: 2 / 3" | 142 cauldrons (`*cauldron*`, not the vanilla water and lava cauldrons, which have WTHIT's fluid bar): Fruits Delight, Colored Water, Neapolitan, salt, SecurityCraft reinforced, powder snow | count |
| "Charges: 2 / 4", "Pickles: 3 / 4" | respawn anchor, sea pickle | count |

Off with `[wthit] stateLines`, one line at a time in `[wthit.state_lines]`.

### 2.6 Campfires

`compat/WthitCampfire` (both sides), lit `CampfireBlock`s only:

- **Burn line:** Unlit Campfire's own, which it only wrote for Jade, in its words: "Burns forever" (the pack sets
  `campfireLitTime = 0`) or "Burn time: 42s".
- **Rain line** (the pack's rule, [feature_porting.md](feature_porting.md) 2.2): only on campfires rain can
  douse (`#creativeengineering:campfire/rain_douses`, the plain campfire): "Rain: Puts it out"; red "Rain: Going out in
  6s" while it rains on it (`[campfire] rainOutTicks` minus the ticks so far); green "Rain: Safe (fuel block below)" on
  a `#campfire/rain_proof_fuel` block. Soul and Tinted campfires get no rain line.

Off with `[wthit] campfireBurn` and `campfireRain`.

### 2.7 Jade-only ports: IE sheetmetal tank, Applied Cooking kitchen station

- **Sheetmetal tank** (`compat/WthitSheetmetalTank`, server data only): only the tank's two fluid ports expose a Forge
  fluid handler, so WTHIT's fluid bar showed on those two blocks only. This sends the master tank as WTHIT
  `FluidData` at priority 1000 (ahead of WTHIT's Forge provider at 2000): every part shows the same bar, the ports
  don't show it twice. WTHIT's fluid options apply.
- **IE multiblock icons** need nothing: IE only overrides Forge's pick block (which returns the original block); WTHIT
  asks the vanilla one, which already gives the multiblock's own item. IE's hemp growth is the Growth line's.
- **ME Kitchen Station** (`compat/WthitKitchenStation`): green "Device Online" + "Connected to ME Security Terminal
  at x, y, z", or red "Device Offline", in Applied Cooking's own words. `[wthit] kitchenStation`.
- Delightful's Jade line (salmonberry growth) is the Growth line's already.

### 2.8 Brewin' keg (in the Brewin' port)

`umpaz.brewinandchewin.integration.wthit.KegWthit` with its own `waila_plugins.json` entry (both sides, like the
Vinery port's plugin); `KegBlockEntity#getFermentTime()` added for it. The keg only syncs to clients when its
inventory changes, so the server sends: temperature, and for a matching recipe its result, wanted temperature,
progress.

- "Temperature: Warm" (the keg GUI's names: Cold, Chilly, Normal, Warm, Hot), always.
- "Fermenting: Beer 45%" while a recipe matches at the right temperature.
- "Beer: Must Be Cold" in red when the temperature is wrong (the recipe book's words).

The keg's items and tank already show through WTHIT's Forge lines. Off with `wthitKeg` in brewinandchewin-client.toml, or WTHIT's plugin list ("Keg Fermentation").

### 2.9 Pot, kettle and oven leftover slots

`compat/WthitRemainderBuffer` (built 2026-10-21, F9, [TODO](todo/TODO.md) `cooking/kitchen #3`). The FD cooking pot,
the Farmer's Respite kettle and the oven keep ingredient leftovers in two slots ([remainder_buffer.md](remainder_buffer.md)).
The server sends what they hold, so the line doesn't depend on the client's copy:

- "Holding: 3 Glass Bottle, 1 Bucket" while either slot holds something.
- Red "Waiting: leftover slots full" while a craft is held back because its leftovers wouldn't fit
  (`[cooking] stallWhenFull`). It clears within 2 ticks of a slot being emptied.

The oven's leftover slots may also show in WTHIT's own Forge item line (its item handler includes them on every
face); unchecked in game. `[wthit] remainderBuffer`.

## 3. What the pack changes

- **Declaration:** `waila_plugins.json` at the root of patcherMod (eleven entries) and of the Brewin' port (one). A
  missing required mod skips that plugin only.
- **Toggles:** WTHIT writes each plugin into `config/waila/plugin_toggle.json5` (player config, not shipped).
- **Switches:** every change to WTHIT's overlay can be turned off on its own (all default on). In
  `config/creativeengineering-common.toml` unless noted:

  | Change | Switch |
  |---|---|
  | Model icon for item-less blocks (Display Delight included) | `[wthit] iconBlockModel` |
  | Forge's hit-aware pick block before the model | `[wthit] iconForgePick` |
  | Substitute items (`iconItems` list) | `[wthit] iconSubstitutes` |
  | Blocks that keep WTHIT's icon | `[wthit] iconExclude` (list) |
  | Jar / goblet / cage Contents line | `[wthit] holderContents` |
  | Potion effects under it | `[wthit] holderPotionEffects` |
  | Goblet shows the fluid only | `[wthit] holderSingleName` |
  | "Bees: 2 / 3" | `[wthit] hiveBees` |
  | Bees in place of WTHIT's names line, above honey | `[wthit] hiveBeesOnTop` |
  | "Honey Level: 3 / 5" | `[wthit] hiveHoneyMax` |
  | All state lines / one at a time | `[wthit] stateLines`; `[wthit.state_lines] servings`, `slices`, `eggs`, `hatch`, `level`, `charges`, `pickles` |
  | Campfire burn line | `[wthit] campfireBurn` |
  | Campfire rain line | `[wthit] campfireRain` |
  | Sheetmetal tank bar on every part (server side) | `[wthit] sheetmetalTank` |
  | Kitchen station lines | `[wthit] kitchenStation` |
  | Pot / kettle / oven leftover lines | `[wthit] remainderBuffer` |
  | Keg lines | `wthitKeg` in `brewinandchewin-client.toml` (the port) |
  | Coaster lines | `[coaster] wthit` ([alcohol.md](alcohol.md) 12) |
  | Growth line | `[crop_growth] enabled` |
  | Run Speed line | `[surface.wthit] enabled` |

  WTHIT's own plugin toggles (`config/waila/plugin_toggle.json5`) turn a whole plugin off as well.
  `[debug.wthit] dumpIconsAtTitle` runs the icon dump at the title screen.
- **Text:** `creativeengineering.wthit.*` and `config.waila.plugin_creativeengineering.*` in patcherMod's lang;
  `brewinandchewin.wthit.keg.*` and `config.waila.plugin_brewinandchewin*` in the port's. Unlit Campfire, Applied
  Cooking, the keg GUI and WTHIT's own keys are reused where they exist.
- **Compile-only jars:** `development/patcherMod/libs/` gained Moonlight, Unlit Campfire and Applied Cooking (WTHIT,
  Supplementaries and IE were there); the Brewin' port compiles against patcherMod's WTHIT jar. MC calls are
  checked with `javap` after reobf.

## 4. Limits and decisions

### 4.1 Findings (2026-10-18), resolved

- **F1. The icon fix only covered Display Delight.** FIXED: any namespace (2.2).
- **F2. Plugin names in WTHIT's config screen were raw keys.** FIXED (lang). WL-02.
- **F3. Nothing ran `gen_display_delight_models.py --check` after a DD update.** FIXED: deploy preflight warning.

### 4.2 Coverage audit (2026-10-20)

| # | Hole | Status |
|---|---|---|
| F4 | Feasts and cakes: servings and bites never shown | BUILT, 2.5 |
| F5 | Eggs in nests, hatch stage | BUILT, 2.5 |
| F6 | Cauldron levels (no WTHIT line but vanilla water / lava) | BUILT, 2.5 |
| F7 | Jade-only integrations (Unlit Campfire, IE tank, Applied Cooking) | BUILT, 2.6 and 2.7; IE icons and Delightful needed nothing |
| F8 | Brewin' keg fermentation progress | BUILT, 2.8 |
| F9 | Kitchen remainder buffer "Holding: ..." line | BUILT 2026-10-21, 2.9 |
| F10 | Blank icons left | BUILT: skull pile by Forge pick, powered flywheel by `iconItems`; skull candles were fine; fake track and casing collision are technical |

Checked and fine: every hive; every Supplementaries holder; the Vinery port's blocks (its own plugin); the Growth
line's coverage tool (0 UNKNOWN after the Vinery lattice / grass slab skips); Forge-capability block entities (WTHIT's
generic lines); AE2, Cooking for Blockheads, Storage Drawers, PneumaticCraft, SecurityCraft, Waystones, Corpse and
TreeChop ship WTHIT plugins.

### Known limits

- **Client only for the client plugins;** the server-half lines also need the pack's mod on the server.
- **Model icon facing** is always north.
- **Cookie jars** show the cookie twice (WTHIT's item line and the Contents line).
- **Hive order** is decided by a guess at whether WTHIT wrote its names line (its option on and bees in); a hive whose
  bees the server can't name could show the order the other way round.
- **`/cedumpicons` probes an empty world** with no hit: it over-counts model icons and can't see Forge-pick or
  block-entity pick items.
- **Keg progress** stops at 99% and reads 0% while waiting for room for leftovers.

### Rejected options

- **Giving item-less DD blocks items:** 70 unobtainable items in JEI.
- **A namespace allow-list for the icon:** the generic rule broke nothing in the audit.
- **Keg line in patcherMod by reflection** (the coaster's way): the keg's state is private; the port is the pack's own
  code and the Vinery port already carries its own WTHIT plugin.
- **A literal Unlit Campfire port:** in this pack every campfire burns forever, so the useful line is the rain rule.

## 5. File map

- [waila_plugins.json](../development/patcherMod/src/main/resources/waila_plugins.json) (patcherMod),
  [waila_plugins.json](../development/backportedBrewin/src/main/resources/waila_plugins.json) (Brewin' port).
- [compat/](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/):
  `WthitBlockModelIcon` + `BlockModelIcons` (icon), `WthitHolderContents`, `WthitHive`, `WthitStateLines` +
  `StateLines`, `WthitCampfire`, `WthitSheetmetalTank`, `WthitKitchenStation`, `WthitCoaster`,
  `WthitRemainderBuffer`.
- [state_lines.json](../development/patcherMod/src/main/resources/assets/creativeengineering/state_lines.json).
- [KegWthit.java](../development/backportedBrewin/src/main/java/umpaz/brewinandchewin/integration/wthit/KegWthit.java).
- [devtools/WthitIconDump.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/devtools/WthitIconDump.java) → `kubejs/exported/wthit_icons.json` → [tools/wthit_icon_audit.py](../tools/wthit_icon_audit.py).
- [tools/gen_display_delight_models.py](../tools/gen_display_delight_models.py); `check_display_delight_models` in [tools/build_deploy.py](../tools/build_deploy.py).
- `surface/WthitRunSpeed`, `crops/WthitCropGrowth`: the two older lines.

## 6. Offline and scratch checks

- **Icon audit:** `/cedumpicons` (client command) or `[debug.wthit] dumpIconsAtTitle`, then
  `python3 tools/wthit_icon_audit.py` (`--md`, `--all`).
- **State lines:** a headless test of `StateLines` against the shipped json (14 checks: all modes, cauldron except,
  rule matching) passed 2026-10-20; a dry run over `display_names.json` gives the counts in 2.5.
- `python3 tools/gen_display_delight_models.py --check`; `python3 tools/crop_growth_coverage.py` (0 UNKNOWN).
- **Scratch server + client, 2026-10-20** (superflat, test blocks set with `/setblock`, in-game screenshots taken by a
  scratch-only client script): every plugin initialised on the dedicated server (`[WTHIT] Initializing common plugin
  ... keg / hive / campfire / sheetmetal_tank / kitchen_station`) and on the client. Seen in a world: "Contents:
  Honey (6 / 12)", "Contents: Cookie (5 / 12)", "Eggs: 3 / 6", "Level: 2 / 3", "Burns forever", "Rain: Puts it out",
  "Rain: Safe (fuel block below)". The same run showed the cheese wheel's double line (fixed since) and fed the
  goblet, hive-order and potion-effect changes; the run ended before those, and the rest of the list, were shot.

## 7. In-game test suite

Line-specific tests elsewhere: BR-44 to BR-47 ([blockrunner.md](blockrunner.md)), CR-59 to CR-66
([crops.md](crops.md)), R4-08 to R4-10 ([plan_1.0.4.md](plan_1.0.4.md)), AL-91 to AL-96 (coaster,
[alcohol.md](alcohol.md)). Commands below assume creative and a cheat-enabled world (or a server console).

**Wiring**

- [ ] **WL-01 Plugins load:** no WTHIT plugin error in `logs/latest.log`; WTHIT's config screen, Plugins, lists the ten
  `creativeengineering:` plugins and `brewinandchewin:keg`.
- [ ] **WL-02 Plugin names:** they read Run Speed, Block Model Icons, Crop Growth, Coaster Drinks, Jar Contents, Hive
  Bees and Honey, Servings Slices Eggs and Levels, Campfire Burn and Rain, Sheetmetal Tank Contents, ME Kitchen
  Station, Keg Fermentation; no raw `config.waila.plugin_...` keys.
- [ ] **WL-03 Toggles:** turning a plugin off in WTHIT's list removes its lines (spot-check `holder_contents`, `hive`,
  `state_lines`, `campfire`, `brewinandchewin:keg`); turn them back on.

**Icons**

- [ ] **WL-04 DD icon, item-less plate:** 3 cookies on a food plate → the icon shows the plate with 3 cookies.
- [ ] **WL-05 DD icon, modelled item:** a placed Plated Hamburger → its item icon (no purple cube).
- [ ] **WL-06 Other mods untouched:** a Farmer's Delight pie and a vanilla cake → WTHIT's normal icons.
- [ ] **WL-07 Model icon outside DD:** a dyed Colored Water cauldron, an Immersive Posts post, a placed Delightful pie
  → their own models in the icon slot. A placed backpack → its backpack item.
- [ ] **WL-08 Exclude list:** `"colored_water"` in `[wthit] iconExclude`, restart → the dyed cauldron's icon is empty.
  Remove it.
- [ ] **WL-09 Substitute item:** look at a D&D powered flywheel (a furnace engine's flywheel while running) → Create's
  flywheel as the icon.
- [ ] **WL-10 Forge pick:** a Supplementaries skull pile (two skulls stacked) → the icon is the skull looked at; look at
  the other half → the other skull. A skull with candles → the skull.

**Holders**

- [ ] **WL-11 Jar fluid:** 6 bottles of honey in a jar → "Contents: Honey (6 / 12)"; fill to 12 → "(12 / 12)".
- [ ] **WL-12 Goblet:** a goblet of water → "Contents: Water", no numbers.
- [ ] **WL-13 Jar potion:** pour Splash Potions of Healing II into a jar → "Contents: Splash Potion of Healing (1 / 12)"
  and a blue "Instant Health II" line under it. A jar of Potions of Regeneration (long) → "Regeneration (1:30)".
  Lingering Potion of Regeneration (long) → "Regeneration (0:22)" (¼). A harmful potion (Poison) → a red line.
- [ ] **WL-14 Potion effects off:** `[wthit] holderPotionEffects = false` → only the Contents line.
- [ ] **WL-15 Cookie jar and cage:** 5 cookies → "Contents: Cookie (5 / 12)". A bee in a jar or cage → "Contents: Bee".

**Hives**

- [ ] **WL-16 Hive with bees:** a beehive with 2 bees inside (`/setblock ~ ~ ~ minecraft:beehive[honey_level=3]{Bees:[{EntityData:{id:"minecraft:bee"},MinOccupationTicks:100000},{EntityData:{id:"minecraft:bee"},MinOccupationTicks:100000}]}`)
  → "Bees: 2 / 3" first, "Honey Level: 3 / 5" under it, no "2 Bee" line.
- [ ] **WL-17 Empty nest:** a bee nest with no bees → "Bees: 0 / 3" first, then "Honey Level: n / 5".
- [ ] **WL-18 Hive options:** WTHIT's "Show Beehive Occupants" off → still Bees then Honey. "Show Beehive Honey Level"
  off → Bees only. `[wthit] hiveBees = false` → WTHIT's own lines back (names, "Honey Level: 3 / 5").
- [ ] **WL-19 Hive on the server:** WL-16 on the dedicated server (the count comes from the server half).

**State lines**

- [ ] **WL-20 Feast:** a Roast Chicken block, take one serving → "Servings: 3 / 4"; Rustic Delight honey pancakes, eat
  two → "Servings: 4 / 6".
- [ ] **WL-21 Cakes and cheese:** a cake with two slices eaten → "Slices: 5 / 7"; an FD apple pie, one slice → "Slices:
  3 / 4"; a Brewin' flaxen cheese wheel, cut twice → "Slices: 2 / 4" and no "Servings" line.
- [ ] **WL-22 Eggs:** an Incubation hay nest with 3 eggs → "Eggs: 3 / 6"; a turtle egg cluster → "Eggs" and "Hatching".
- [ ] **WL-23 Cauldrons:** a dyed water cauldron two-thirds full → "Level: 2 / 3"; a vanilla water cauldron → only
  WTHIT's fluid bar, no Level line; a Fruits Delight juice cauldron and a powder snow cauldron → Level lines.
- [ ] **WL-24 Others and override:** a respawn anchor with 2 charges → "Charges: 2 / 4"; 3 sea pickles → "Pickles: 3 /
  4". Copy `state_lines.json` to `kubejs/assets/creativeengineering/`, delete the pickle rule, F3+T → the Pickles line
  is gone. Remove the copy.

**Campfires**

- [ ] **WL-25 Campfire lines:** a lit campfire on grass → "Burns forever", "Rain: Puts it out"; on a coal block → green
  "Rain: Safe (fuel block below)"; a soul campfire → "Burns forever" only.
- [ ] **WL-26 Rain countdown:** `/weather rain` beside the open-sky campfire → red "Rain: Going out in 8s" counting down,
  then it goes out and the lines disappear.

**Jade ports and keg**

- [ ] **WL-27 Sheetmetal tank:** build and fill an IE sheetmetal tank → the same fluid bar on every part, including the
  two ports (once each).
- [ ] **WL-28 Kitchen station:** an ME Kitchen Station on its own → red "Device Offline"; tied to an ME Security
  Terminal on a grid → green "Device Online" and "Connected to ME Security Terminal at x, y, z".
- [ ] **WL-29 Keg:** an empty keg → "Temperature: Normal" (near a heat source "Warm"/"Hot"). Load a beer recipe at a
  valid temperature → "Fermenting: Beer n%" rising; move it somewhere the recipe can't run (wrong temperature) → red
  "Beer: Must Be ..." (the requirement the recipe book shows).
- [ ] **WL-30 Keg on the server:** WL-29 on the dedicated server.

**Kitchen leftover slots** (patcherMod 1.1.01)

- [ ] **WL-32 Holding line:** cook hot cocoa with a milk bucket in an FD pot → "Holding: 1 Bucket". Bake a milk-bottle
  recipe in the oven → "Holding: 1 Glass Bottle" (note whether WTHIT's own item line lists the bottle as well). An
  empty pot shows no line.
- [ ] **WL-33 Waiting line:** fill both of a pot's leftover slots with different items (cook with a milk bucket, then
  a honey bottle recipe, or use `/data`), then load a recipe that leaves a third kind → red "Waiting: leftover slots
  full"; take one slot out → the red line goes and the pot cooks. Repeat once on the dedicated server.

**Switches**

- [ ] **WL-31 Every switch:** set each switch in the section 3 table to false in turn (or all at once, then back) and
  restart: its line or icon goes and the others stay. Expected when off: `iconBlockModel` → empty icon on an item-less
  DD plate; `iconForgePick` → skull pile icon empty; `iconSubstitutes` → powered flywheel icon empty;
  `holderPotionEffects` → no effect lines; `holderSingleName` → goblet "Water (1 / 1)"; `hiveBeesOnTop` → WTHIT's
  "2 Bee" line back and Bees at the end; `[wthit.state_lines] slices` → no Slices on a cake while feasts keep Servings;
  `campfireBurn` / `campfireRain` → that line only; `sheetmetalTank` → the bar only on the two ports; `kitchenStation`
  → no station lines; `remainderBuffer` → no Holding / Waiting lines; `wthitKeg` (brewinandchewin-client.toml) → no keg lines. Set them all back to true.

## 8. History

- 2026-09-27: Run Speed line ([blockrunner.md](blockrunner.md) 8).
- 2026-10-04: Growth line and Display Delight icon fix (patcherMod 1.0.4).
- 2026-10-18: this hub writeup; findings F1 to F3; tests WL-01 to WL-06.
- 2026-10-20: plugin names in lang (F2); coaster plugin ([alcohol.md](alcohol.md) 12).
- 2026-10-20: TODO `ui/wthit #2` to `#4`: icon for every namespace (F1) with `/cedumpicons` and the audit tool; jar
  contents line; hive lines; DD model check in the deploy preflight (F3); coverage audit F4 to F10.
- 2026-10-20 (later): TODO `ui/wthit #5` to `#8` built: state lines (F4 to F6), campfire, sheetmetal tank and kitchen
  station (F7), keg in the Brewin' port (F8), Forge pick + `iconItems` (F10). Scratch server + client run (section 6).
  From the user's review of the first shots: goblet shows the fluid only, potion effects under jar potions, the hive's
  Bees line takes the place of WTHIT's names line, the cheese wheel's duplicate Servings line fixed (one line per
  property). patcherMod 1.1.01 and Brewin' `-03` rebuilt and installed in `mods/` and `server_mods/` (old jars in
  each project's `jar_archive/displaced/`). Tests rewritten as WL-01 to WL-30.
- 2026-10-20 (later): an on/off switch for every change (section 3 table: new `iconBlockModel`, `iconForgePick`,
  `iconSubstitutes`, `holderSingleName`, `hiveBeesOnTop`, `[wthit.state_lines]` per line, `campfireBurn` /
  `campfireRain` (replacing `campfire`), `sheetmetalTank`, the port's `wthitKeg`). Headless spec check: every key
  present, all true. Jars rebuilt and installed again. WL-31.
- 2026-10-21: F9 built: pot / kettle / oven leftover lines (2.9, `compat/WthitRemainderBuffer`, `[wthit]
  remainderBuffer`, TODO `cooking/kitchen #3`). patcherMod 1.1.01 rebuilt and installed in `mods/` and `server_mods/`.
  WL-32, WL-33.
