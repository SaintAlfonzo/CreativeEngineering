# Tooltips

Test prefix **TT-** · Last updated **2026-10-05** · Overall status **DEPLOYED (new removals), in-game test pending**

## 0. At a glance

The pack's tooltip layer adds about 455 hand-written lines, removes or rewrites lines other mods ship,
and draws the lines for the pack's own frameworks (armor effects, tool effects, shields, potion
buckets, walk speed). Anything that changes a tooltip gets recorded here; the feature it describes
keeps its own writeup.

| Part | Where | Status |
|---|---|---|
| Hand-written lines (~455 entries, 16 sections) | [tooltips.js](../kubejs/client_scripts/tooltips.js) | VERIFIED (long-standing); only samples re-tested here |
| Remove IP asphalt's "Increases walking speed by 20.0%" | tooltips.js `removeTooltipKey` | DEPLOYED 2026-09-27 |
| Remove Create Compressed crushed-pile compatibility line | tooltips.js `removeTooltips` | VERIFIED (long-standing) |
| Framed / copycat lines: schematics save the block or not, and hybrid blocks | tooltips.js "Framed / Copycat Blocks" section | DEPLOYED 2026-09-27 |
| FramedBlocks renamed "Framed Copycat …" | edited resource pack `Copycat Framed Block.zip` | DEPLOYED 2026-09-27; F3+T |
| BlockRunner "Speed Multiplier" line, rewritten on camouflage blocks | BlockRunner + patcherMod `CamoSpeedTooltip` | DEPLOYED (the "no matter its camo" and "newest camo sets walk behaviors for hybrid blocks" wording is built, not yet in `mods/`) |
| Framework tooltips: armor effects, tool effects, potion buckets | dedicated client scripts | VERIFIED per their own memories |
| Placeable food lines ("Placeable", "(while sneaking)", "Placeable on plate", "Placeable on small plate"); DD + Fruits Delight placement lines removed | [placeable_tooltip.js](../kubejs/client_scripts/placeable_tooltip.js), tags from [placeable_food.md](placeable_food.md) | BUILT 2026-10-05, PL-01 to PL-08 |
| patcherMod tooltips: tier shields, vanilla shield, echo armor set bonus, D&D jetpack crash fix | patcherMod | VERIFIED / see memories |

**Open first:** [tooltips.js](../kubejs/client_scripts/tooltips.js) (helpers L1-86, colour legend L91-125, then sections).
**Knobs:** the colour legend (`col.*`); BlockRunner's `block_speed_multiplier_tooltip` in `config/blockrunner-client.toml`.
**Reload:** F3+T reloads resources and client scripts; a full restart is safest after editing helpers.

## 1. Overview: how a line gets onto an item

1. The item or its block adds its own lines (`appendHoverText`). Immersive Petroleum's asphalt line comes from here.
2. Forge fires `ItemTooltipEvent`, and every listener can add, edit or remove lines:
   - BlockRunner (Speed Multiplier)
   - KubeJS `ItemEvents.tooltip` handlers (all the pack's scripts)
   - patcherMod's `CamoSpeedTooltip` (LOW priority, so it runs after BlockRunner)
   - `IronShieldTooltip`
3. In KubeJS, `e.add(id, lines)` appends lines, and `e.addAdvanced(id, fn)` gets the whole list to edit.
   Order between listeners at the same priority isn't guaranteed. That's why removals target a specific line.

## 2. Reference

**Helpers in tooltips.js**

| Helper | Does | Use for |
|---|---|---|
| `addTooltips(e, [id, [text, col], …], …)` | appends one or more coloured lines per item | most entries |
| `addTooltipPattern(e, 'ns:*_thing', {exclude:[…]}?, [text, col])` | same, for every registered item matching a wildcard; logs `[addTooltipPattern] … -> N matches` | families (ladders, runes, presents) |
| `removeTooltips(e, id, …)` | strips **every** line after the name | only when nothing else should show (it also removes BlockRunner's line) |
| `removeTooltipKey(e, 'lang.key', id, …)` | strips only lines built from one translation key | removing one mod's flavour text, keeping everything else |
| `e.addAdvanced(id, fn)` | custom logic, e.g. reading NBT | precision sawblade profile |

**Colour legend** (`col.*`, pick by role, not by look):

| Key | Colour | Role |
|---|---|---|
| `norm` | gray | general info |
| `uncm` / `rare` / `epic` / `leg` | green / dark blue / dark purple / aqua | how notable the property is |
| `eng` | gold | engineering / crafting notes |
| `good` / `warn` | dark green / dark red | upsides / downsides |

**Sections** (entries / wildcard patterns):
- Material Information (~213)
- Supplementaries (62 / 3)
- Misc Mods (43 / 4)
- Quark (32 / 10)
- End Gear (24 / 1)
- Etched/music (21)
- Create (10, plus the removals)
- Food / Farmer's Delight (8)
- Framed / Copycat Blocks (2 lines on 130 framed items, 45 Copycats+ items and 2 Create copycats; hybrid lines on 27)
- Crafting Materials (7)
- Decor, Tools, IE, Blocks, Transport, Archery, Botania (a few each)

Examples:
- Quark hollow logs → "Can be crawled through"
- every ladder family → "Right click to extend ladder…"
- bottled cloud → "Place a temporary block midair!" (rare)
- IE copper wire → solder note (eng)

## 3. What the pack changes about other mods' tooltips

**Removed**
- **Immersive Petroleum asphalt, slab, stairs:** "Increases walking speed by 20.0%" (`desc.immersivepetroleum.flavour.asphalt`).
  BlockRunner's asphalt tier sets the real speed (1.50, [blockrunner.md](blockrunner.md)) and already shows it as "Speed Multiplier: 1.5".
  IP's figure described its own 1.2 speed factor, which BlockRunner overrides. Removed with
  `removeTooltipKey` so the Speed Multiplier line and any other lines stay.
- **Create Compressed crushed ore piles** (aluminium, lead, silver, nickel, uranium): the compatibility line, via `removeTooltips`.
- **Vinery grapes and grape seeds** (all 8 of each): the per-variant flavour line ("A robust and vigorous Grape - can be found in temperate Biomes", "Heavy - needs sunlight and Coarse Dirt or Podzol nearby to grow"...).
  Vinery builds the key from the item id (`item.vinery.grape.tooltip.<id>`, `item.vinery.grapeseed.tooltip.<id>`), so
  `removeTooltipKey` now treats a key ending in `*` as a prefix. The real bush requirements are in [vinery.md §2.6](vinery.md).

- **Display Delight and Fruits Delight placement lines** ("Can be placed while sneaking", "Can be plated on:", "Food Plate", "Small Food Plate",
  "Shift right click to place down"): stripped by translation key on every item and replaced by the placeable food lines
  below. DD showed its lines from the tags alone, even for foods it never links to a block. See [placeable_food.md](placeable_food.md).

**Renamed**
- **FramedBlocks → "Framed Copycat …"** (e.g. Framed Copycat Slab, Large Framed Copycat Button, Framed Copycat Chest GUI title).
  The resource pack `Copycat Framed Block.zip` retextures framed blocks to look like Create copycats,
  and renamed them "Copycat …", which duplicated the real Create and Copycats+ copycats (two "Copycat Slab"s).
  The pack's own lang file was edited: "Copycat" → "Framed Copycat" in every block name and chest title
  (177 entries), and the two gate doors the pack missed were added. Tools keep their pack names
  ("Copycat Hammer" etc.) because nothing duplicates them.

**Rewritten**
- **BlockRunner's line on framed/copycat blocks:** patcherMod `CamoSpeedTooltip` swaps it for
  "matches its camo", "matches its camo (newest camo sets walk behaviors for hybrid blocks)" on hybrid-capable items, or "1.0, no matter its camo", depending on
  `[surface.camouflage] copyCamo` ([blockrunner.md §3.3](blockrunner.md)).
- **Create: Deep Dark echo armor set bonus:** patcherMod `EchoArmorTooltipMixin` shows the pack's
  replacement bonus (Night Vision + Phantom Bane). See memory `echo-armor-set-bonus`.

**Added for framed / copycat blocks** ("Framed / Copycat Blocks" section)
- Every framed block (not the tools or item frames): "Create schematics won't save the block inside it" (gray, `col.norm`).
  Create schematics keep only block-entity data a block marks safe; FramedBlocks' camo isn't, so
  schematic copies come out empty. FramedBlocks' own Copycat Blueprint does keep camos.
- Every Copycats+ block and Create's copycat panel/step: "Create schematics save the block inside it" (gray, `col.norm`).
- Hybrid blocks ([blockrunner.md §3.3](blockrunner.md)):
  - framed slab and panel: "Place a second one in the same space to make a hybrid block, one camo per half"
  - every framed `*double*` item: "Hybrid block: each part holds its own camo"
  - Their Speed Multiplier line reads "matches its camo (newest camo sets walk behaviors for hybrid blocks)" (your wording, 2026-09-27).

**Added for Vinery** ("Vinery" section, 2026-10-01)
- `TODO` placeholders (gray) on the Grapevine Pot, Apple Press, Aging Barrel, Grapevine Stem and Lattice,
  apple/cherry leaves and saplings, Kitchen Sink, Stackable Log and the wine storage blocks (`*_wine_rack_mid` by pattern).
  Text still to be written. The behaviour to describe is in [vinery.md](vinery.md). They show in game until replaced.

**Added for placeable food** (2026-10-05, [placeable_food.md](placeable_food.md))
- 164 foods: gray "Placeable" / "Placeable (while sneaking)" / "Placeable on plate" / "Placeable on small plate", drawn from the
  `#creativeengineering:placeable*` tags at hover time (`addAdvancedToAll`). Catalog: [placeable_catalog.md](placeable_catalog.md).

**Added by frameworks** (each documented in its own memory):
- armor effects (`armor_effects_tooltip.js`) and tool effects (`tool_effects_tooltip.js`)
- potion buckets (`potion_buckets.js`), vanilla-style effect lines
- tier shields and the vanilla shield (`ShieldTooltip` / `IronShieldTooltip`); the Shift view gained
  `Blocks burning` and `Blocks thorns` Yes/No lines 2026-10-05 (built, not in `mods/`). Details and tests: [shields.md](shields.md) 2.6, SH-04
- precision sawblade profile (`addAdvanced`, reads NBT)

**Fixed**
- **D&D Jetpack tooltip crash:** `JetpackItemTooltipMixin` stops a crash that used to break client
  tag loading, which delayed JEI (memory `axe-tag-ghost-entries`).
- **WTHIT growth line** for hemp, tobacco, berry bushes, grapes and other plants WTHIT skips: patcherMod `crops/WthitCropGrowth`, written up in [crops.md](crops.md) 2.9 (tests CR-59 to CR-66).

## 4. Limits and decisions

- **`removeTooltips` is blunt:** it also removes BlockRunner's Speed Multiplier line and other mods'
  lines. Prefer `removeTooltipKey`.
- **`removeTooltipKey` prefix keys:** a key ending in `*` matches every key starting with the rest, for mods that build keys from the item id (Vinery).
- **`removeTooltipKey` only matches lines built from a translation key.** A mod line built from plain
  text needs a match on the text instead.
- **Patterns resolve at load:** `addTooltipPattern` resolves wildcards against the item registry when
  the script loads. A pattern that logs `-> 0 matches` is dead (typo or removed mod).
- **Static lines:** the hand-written lines don't read config values. Anything that must follow a
  config (shields, camo speed) lives in Java or a dedicated script.
- **KubeJS lang can't override a resource-pack zip.** KubeJS inserts its generated resource pack
  *below* the first file-based pack, so `ClientEvents.lang` / `namechanges.js` loses to any enabled zip.
  To change a name a zip sets, edit the zip. The edited pack is copied to `configs_edited/resourcepacks/`,
  with the untouched original in `configs_edited/resourcepacks/original/`.
- **Only the slab and panel merge:** these are the only framed blocks whose right-click turns them
  into a double block (checked in FramedBlocks' code). All other framed double shapes are their own items.
- **One file per framework:** framework tooltips stay in their own files instead of tooltips.js,
  because they run after their framework registers entries and use a different line format.

## 5. File map

- [kubejs/client_scripts/tooltips.js](../kubejs/client_scripts/tooltips.js): helpers, colour legend, all hand-written lines, removals
- [placeable_tooltip.js](../kubejs/client_scripts/placeable_tooltip.js): placeable food lines + DD / Fruits Delight removals (tests in [placeable_food.md](placeable_food.md), PL-)
- [armor_effects_tooltip.js](../kubejs/client_scripts/armor_effects_tooltip.js), [tool_effects_tooltip.js](../kubejs/client_scripts/tool_effects_tooltip.js), [potion_buckets.js](../kubejs/client_scripts/potion_buckets.js)
- patcherMod: [surface/CamoSpeedTooltip.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/surface/CamoSpeedTooltip.java), `shield/ShieldTooltip.java`, `shield/IronShieldTooltip.java`, `mixin/EchoArmorTooltipMixin.java`, `mixin/JetpackItemTooltipMixin.java`
- Lang: [patcherMod lang en_us.json](../development/patcherMod/src/main/resources/assets/creativeengineering/lang/en_us.json) (`creativeengineering.tooltip.*`)
- Item keys: `global.item` in [item_dict.js](../kubejs/startup_scripts/_lib/item_dict.js)
- Renames: `resourcepacks/Copycat Framed Block.zip` → `assets/framedblocks/lang/en_us.json` (backups in `configs_edited/resourcepacks/`)

## 6. Offline checks

- **Syntax:** `node --check kubejs/client_scripts/tooltips.js`.
- **Dead patterns:** after a game load, search `logs/latest.log` for `[addTooltipPattern]` lines ending in `-> 0 matches`.

## 7. In-game test suite

Hover items in JEI or the inventory. BlockRunner's line needs `block_speed_multiplier_tooltip = true` (the pack default).

**Removals**
- [ ] **TT-01 IP asphalt:** hover Asphalt Concrete, its slab and its stairs. → No "Increases walking speed" line on any; each still shows "Speed Multiplier: 1.5".
- [ ] **TT-02 Crushed piles:** hover a Create Compressed crushed aluminium pile. → Name only.

**BlockRunner line** (moved here from the BlockRunner suite)
- [ ] **TT-03 Speed values:** asphalt (IP and D&D) → "Speed Multiplier: 1.5"; glass → "Speed Multiplier: 1.0"; stone → "Speed Multiplier: 1.05". *(was BR-06)*
- [ ] **TT-04 Camo lines** (needs the rebuilt patcherMod jar): framed cube → "Speed Multiplier: matches its camo"; framed slab, framed panel, any framed double item, Copycats+ copycat slab or byte panel → "…matches its camo (newest camo sets walk behaviors for hybrid blocks)". Needs `[surface.camouflage] copyCamo = true`. *(was BR-20)*
- [ ] **TT-05 Camo off** (needs the rebuilt patcherMod jar): set `[surface.camouflage] copyCamo = false` and save. Hover a framed cube → "Speed Multiplier: 1.0, no matter its camo". Set it back to `true`. *(was the tooltip half of BR-21)*

**Pack lines (samples)**
- [ ] **TT-06 Patterns:** a Quark hollow log → "Can be crawled through"; any Create/Quark ladder → the ladder line.
- [ ] **TT-07 Colours:** Bottled Cloud's line is dark blue (rare); an IE copper wire's solder line is gold (eng).
- [ ] **TT-08 NBT line:** an unprogrammed Precision Sawblade → "Unprogrammed — blueprint with a shape to set a profile".
**Framed / copycat blocks**
- [ ] **TT-10 Names:** search JEI for "copycat slab". → "Framed Copycat Slab" (FramedBlocks) and "Copycat Slab" (Copycats+) show as two distinct names. A placed Framed Copycat Chest's GUI title reads "Framed Copycat Chest", and the two gate doors are "Framed Copycat Gate" / "Framed Copycat Iron Gate".
- [ ] **TT-11 Schematic lines:** framed cube → gray "Create schematics won't save the block inside it"; Framed Hammer → no such line; Copycats+ copycat block and Create copycat step → gray "Create schematics save the block inside it".
- [ ] **TT-12 Hybrid lines:** framed slab and framed panel → "Place a second one in the same space to make a hybrid block, one camo per half"; Framed Copycat Double Corner → "Hybrid block: each part holds its own camo"; framed cube → neither line.
- [ ] **TT-13 Schematic claim holds:** save a Create schematic over a camo'd framed cube and a copycat block, then print it. → The framed cube comes out empty; the copycat keeps its material.

**Vinery**
- [ ] **TT-14 Grape lines gone:** hover each grape and each grape seed in JEI. → Name (and food / mod lines) only, no italic "can be found in ... Biomes" or "needs sunlight" line.
- [ ] **TT-15 Vinery placeholders:** hover the Grapevine Pot and any `*_wine_rack_mid`. → A gray "TODO" line under Vinery's own line (until the text is written). `logs/latest.log` shows `[addTooltipPattern] vinery:*_wine_rack_mid -> 8 matches`.

- [ ] **TT-09 No dead patterns:** search `logs/latest.log` for `[addTooltipPattern]`. → Every line reports more than 0 matches.

## 8. Tooltips To Add

### Items

- [] Straw- Can be collected by cutting grass with a knife
- [] Knifes- Can be used to collect straw by cutting grass
- [] Foods- Food type tooltip (Feast, Sweets, Baked, Fruit, etc)
- [] Cans- Needs appleskin mixin. (shanks, saturation, of single meal, standard appleskin render) x(number of meals in can)
- [] Chalk- Right click to mark block w/ arrow, hold right click to change mark
- [] Pretty Pipes- Use pipe wrench w/ block in off-hand to add cover

### Mods to Cover
- [] Botania
- [] Vinery + JEI (placeholders in, text TODO; behaviour reference in [vinery.md](vinery.md))
- [] Cloud Storage
- [] Brewin Chewing + JEI

## 9. History

- **Before 2026-09-27:** tooltips.js helpers, colour legend and sections built up over time (not reconstructed here).
- **2026-09-27:**
  - Writeup created.
  - `removeTooltipKey` added; IP asphalt's walking-speed line removed.
  - The BlockRunner tooltip tests moved here (BR-06, BR-20 and part of BR-21 → TT-03..05).
  - Schematic lines recoloured to the normal gray (`col.norm`) at your request.
  - "Framed / Copycat Blocks" section added (schematic + hybrid lines); FramedBlocks renamed "Framed Copycat …" by editing the resource pack; hybrid wording on the Speed Multiplier line. TT-10..13 added.
- **2026-10-01:** Vinery grape and grape seed flavour lines removed (`removeTooltipKey` prefix keys); Vinery `TODO` placeholders added. TT-14, TT-15 added. See [vinery.md](vinery.md).
- **2026-10-05:** Placeable food lines added and DD / Fruits Delight placement lines removed ([placeable_food.md](placeable_food.md), tests PL-01 to PL-08).
- **2026-10-05:** Shield Shift view adds "Blocks burning" and "Blocks thorns" Yes/No lines ([shields.md](shields.md), SH-04). Built, not in `mods/`.
