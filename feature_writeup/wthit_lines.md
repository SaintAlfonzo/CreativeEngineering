# WTHIT lines: the pack's WTHIT plugin (Run Speed, crop Growth, Display Delight icons)

This is the hub for everything the pack adds to WTHIT's look-at overlay. Two of the three parts already have full
writeups; this file covers the shared plugin wiring and the Display Delight icon in full, and points to the others.

## 0. At a glance

| Part | Status | Full detail |
|---|---|---|
| Plugin wiring: three client-only WTHIT plugins in patcherMod's `waila_plugins.json` | DEPLOYED | here, 3 |
| "Run Speed" line on the looked-at block (BlockRunner tiers, camo, sticky, sinking) | DEPLOYED (second version needed a restart; BR-44 to BR-47 pending) | [blockrunner.md](blockrunner.md) 3.8 |
| "Growth" line for the 62 plants WTHIT skips, plus "(Harvestable)" | DEPLOYED with 1.0.6; CR-59 to CR-66 pending | [crops.md](crops.md) 2.9 |
| Display Delight icon: the placed food's own model when its item can't be drawn, and 89 missing item models | DEPLOYED with 1.0.4; R4-08 to R4-10 pending | here, 2 (first written in [plan_1.0.4.md](plan_1.0.4.md) 4b) |
| Planned: Vinery lines (pot, barrel, press, wine rack) in the same plugin | not built; part of the Vinery backport | [alcohol.md](alcohol.md) B4.6 |

- **Pending:** nothing built and unshipped. WL-01 to WL-06 are new; the line-specific tests stay in their own files.
  Findings F1 to F3 (section 4); none needs a decision.
- **Files to open:** [waila_plugins.json](../development/patcherMod/src/main/resources/waila_plugins.json),
  [compat/WthitBlockModelIcon.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/WthitBlockModelIcon.java),
  [tools/gen_display_delight_models.py](../tools/gen_display_delight_models.py).
- **Knobs:** WTHIT's own plugin toggles (`config/waila/plugin_toggle.json5`: `creativeengineering:run_speed`,
  `block_model_icon`, `crop_growth`, all true), WTHIT's "Crop progress" option, and the pack's `[surface.wthit]` and
  `[crop_growth]` sections in `config/creativeengineering-common.toml`.

## 1. Overview

WTHIT 5.36.1 (Forge) draws a box at the top of the screen for the block or entity under the crosshair: icon, name,
mod, and lines from plugins. Its own plant line ("Growth: 60%") only covers vanilla `CropBlock`s and stems. Its icon
is the block's vanilla clone stack (`Block#getCloneItemStack(level, pos, state)`), so a block with no item, or an
item with no model, gets an empty slot or the purple/black missing-model cube. WTHIT Harvestability 2.1.0 adds the
tool line.

## 2. Reference

### 2.1 The three plugins

| Plugin id | Class | Needs | What it adds |
|---|---|---|---|
| `creativeengineering:run_speed` | `surface/WthitRunSpeed` | `blockrunner` | "Run Speed: 1.50× (Asphalt)" ([blockrunner.md](blockrunner.md) 3.8) |
| `creativeengineering:crop_growth` | `crops/WthitCropGrowth` | nothing | "Growth: 67%" / "Growth: Mature", "(Harvestable)" ([crops.md](crops.md) 2.9) |
| `creativeengineering:block_model_icon` | `compat/WthitBlockModelIcon` | `displaydelight` | the icon fix below |

All three are `side: client`: nothing is needed on the server, and a client without patcherMod just doesn't show them.

### 2.2 Display Delight icons

Display Delight 1.2.0 has 191 placed-food blocks:

- **89 block items with no item model** (the plated items lost theirs in a rename; newer stews, drinks and compat
  foods never had one). They showed the missing-model cube in WTHIT and JEI. `gen_display_delight_models.py` writes
  `kubejs/assets/displaydelight/models/item/<id>.json` for each, parented on the block's default-state model
  (facing north, one stack); two plated ones with no gui transform get the usual plated one. It also rewrites the
  pumpkin soup, cactus soup and pufferfish broth block models, whose faces pointed at the undefined `#missing`
  texture (3 files in `models/block/`).
- **70 blocks with no item at all** (most stackable plates: cookies, pumpkin pie, the `ad_` / `cd_` / `df_` compat
  plates; 20 of them reachable with this pack's mods). WTHIT got an empty stack. `WthitBlockModelIcon` draws the
  block's own model in the icon slot instead.

The plugin's rule, for a block in the `displaydelight` namespace only:

1. If its clone stack exists and its item model isn't the missing model, do nothing (WTHIT's core icon runs).
2. Otherwise draw the block state's model, facing pinned to north (to match DD's item models), stack count kept (a
   plate of three cookies shows three), full bright, in an 18 × 18 slot like WTHIT's item icon. Models with no gui
   transform get vanilla's block one.

It registers at priority 1000, ahead of WTHIT's core icon (1100), and returns nothing in every other case.

## 3. What the pack changes

- **Declaration:** WTHIT finds plugins through `waila_plugins.json` at the root of a mod jar; patcherMod's lists the
  three entry points with their side and required mods. A missing required mod skips that plugin only.
- **Toggles:** WTHIT writes each plugin into `config/waila/plugin_toggle.json5`; the pack leaves all three on. That
  file is player config and isn't shipped ([plan_1.0.4.md](plan_1.0.4.md) 5).
- **Text:** `creativeengineering.wthit.run_speed*` and `creativeengineering.wthit.crop_harvestable` in patcherMod's
  `lang/en_us.json`.
- **DD models** ship in the kubejs payload; the plugin in the patcherMod jar. `gen_display_delight_models.py` is in
  `tools/generators.json` (group `client`, reads `/dumpitemsbymod displaydelight`), so `/cegen run client dumps`
  regenerates them ([pack_tooling.md](pack_tooling.md)).

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1. The icon fix only covers Display Delight.** `NAMESPACES` is `Set.of("displaydelight")`. Other mods' item-less
  placed blocks keep WTHIT's empty icon. Add a namespace when one turns up.
- **F2. Plugin names in WTHIT's config screen are probably raw keys.** WTHIT labels each plugin toggle with
  `config.waila.plugin_<namespace>.<path>`; patcherMod's lang has none for the three ids. Check in WTHIT's plugin
  screen (WL-02); the fix is three lang lines.
- **F3. `--check` after a Display Delight update.** `gen_display_delight_models.py --check` exits 1 when a model is
  missing or stale; nothing runs it automatically. The plugin still covers items whose model is missing, so a DD
  update degrades to block-model icons, not purple cubes, in WTHIT (JEI would still show the cube).

### Known limits

- **Client only:** a player without patcherMod (or without WTHIT) sees none of this.
- **Facing:** the icon is always north-facing, whatever the placed block's facing.
- **Planned Vinery lines** wait on the Vinery backport (decision D6 in [alcohol.md](alcohol.md)).

### Rejected options

- **Giving the item-less DD blocks items:** would add 70 unobtainable items to JEI; drawing the block is enough.

## 5. File map

- [development/patcherMod/src/main/resources/waila_plugins.json](../development/patcherMod/src/main/resources/waila_plugins.json): the three plugins.
- [compat/WthitBlockModelIcon.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/WthitBlockModelIcon.java): DD icon.
- [surface/WthitRunSpeed.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/surface/WthitRunSpeed.java), [crops/WthitCropGrowth.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/crops/WthitCropGrowth.java): the two lines (detail in their writeups).
- [tools/gen_display_delight_models.py](../tools/gen_display_delight_models.py) → `kubejs/assets/displaydelight/models/item/` (89) and `models/block/` (3).
- `config/waila/plugin_toggle.json5`, `config/waila/waila_plugins.json5`: WTHIT's player config.
- Compile-only WTHIT jar: `development/patcherMod/libs/` (not committed).

## 6. Offline checks

- `python3 tools/gen_display_delight_models.py --check`: exits 0 when every DD item has a model and the three soup
  models are fixed (needs the DD jar in `mods/` and the `/dumpitemsbymod` export).
- The Run Speed and Growth lines have their own headless tests ([blockrunner.md](blockrunner.md) 6,
  [crops.md](crops.md): `tools/test_crop_growth.sh`, `tools/crop_growth_coverage.py`).
- The icon itself needs the game.

## 7. In-game test suite

Line-specific tests stay where they are: BR-44 to BR-47 ([blockrunner.md](blockrunner.md)), CR-59 to CR-66
([crops.md](crops.md)), R4-08 to R4-10 ([plan_1.0.4.md](plan_1.0.4.md)).

- [ ] **WL-01 Plugins load:** `logs/latest.log` has no WTHIT plugin error for any `creativeengineering:` plugin; WTHIT's
  config screen, Plugins, lists all three.
- [ ] **WL-02 Plugin names (F2):** in that list, note whether the three show readable names or raw
  `config.waila.plugin_creativeengineering...` keys.
- [ ] **WL-03 Toggles:** turn off `creativeengineering:run_speed` → no Run Speed line on asphalt; `crop_growth` → no
  Growth line on hemp (wheat still has WTHIT's own); `block_model_icon` → an item-less DD cookie plate shows an empty
  icon. Turn all three back on.
- [ ] **WL-04 DD icon, item-less plate:** place 3 cookies on a food plate → the icon shows the plate with 3 cookies,
  facing north whichever way it was placed.
- [ ] **WL-05 DD icon, modelled item:** look at a placed Plated Hamburger → its item icon (no purple cube); JEI shows
  the same model for the item.
- [ ] **WL-06 Other mods untouched:** look at a Farmer's Delight pie and a vanilla cake → WTHIT's normal icons.

## 8. History

- 2026-09-27: Run Speed line ([blockrunner.md](blockrunner.md) 8).
- 2026-10-04: Growth line and Display Delight icon fix (patcherMod 1.0.4); DD icons deployed with 1.0.4, Growth line
  with 1.0.6.
- 2026-10-18: this hub writeup; findings F1 to F3; tests WL-01 to WL-06. No code changed.
