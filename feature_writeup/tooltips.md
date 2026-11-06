# Tooltips

Test prefix **TT-** · Last updated **2026-11-02** · Overall status **DEPLOYED (new removals), in-game test pending**

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
| BlockRunner "Speed Multiplier" line, rewritten on camouflage blocks | BlockRunner + patcherMod `CamoSpeedTooltip` | DEPLOYED (incl. the "no matter its camo" and "newest camo sets walk behaviors for hybrid blocks" wording) |
| Framework tooltips: armor effects, tool effects, potion buckets | dedicated client scripts | VERIFIED per their own memories |
| Stepstools: "Stand still and hold Sprint + Jump to step up", "Move, crouch or jump to step off" (the slot is Curios' "Slot: Feet"; the pack's "Worn in the Feet slot" line dropped 2026-11-02) | tooltips.js Tools section ([stepstool.md](stepstool.md)) | BUILT 2026-10-29; STL-02 |
| Tall tooltips keep their top on screen and run off the bottom (`[tooltips] keepTopOnScreen`) | patcherMod client mixin `ScreenTooltipTopMixin` | BUILT 2026-11-02 (patcherMod 1.1.07 installed), TT-39 |
| Curios "Slot: ..." line on every item that fits a Curios slot, also where the item's mod hides it (`[tooltips] curiosSlotLine`) | patcherMod `compat/CuriosSlotTooltip` | BUILT 2026-11-02 (patcherMod 1.1.07 installed), TT-40 |
| PneumaticCraft "Hold Shift" hint replaced by the item's description, wrapped, plain gray, on every PneumaticCraft item | [pnc_tooltips.js](../kubejs/startup_scripts/changes/pnc_tooltips.js) (startup, LOW-priority Forge listener) | BUILT 2026-11-02 (glycerol only since 2026-10-23), TT-41 |
| Placeable food lines ("Placeable", "(while sneaking)", "Placeable on plate", "Placeable on small plate"); DD + Fruits Delight placement lines removed | [placeable_tooltip.js](../kubejs/assets/creativedelight/kubejs/_qol/placeable_tooltip.js), tags from [placeable_food.md](placeable_food.md) | DEPLOYED with 1.0.6 (2026-10-14), PL-01 to PL-08 |
| Plantable lines ("Seed Item", "Plantable Crop", "Sapling", "Fungi Sapling", "Water Plant", "Vine", "Vertical Crop") | [plantable_tooltip.js](../kubejs/client_scripts/plantable_tooltip.js), tags from [plantable.md](plantable.md) | BUILT 2026-11-01, PT-01 to PT-06 |
| Legendary Tooltips name underline no longer crosses the spinning item model (Iceberg's title break moved after LT's model spacer) | patcherMod client mixin `IcebergTitleBreakMixin` | BUILT 2026-10-05, in patcherMod 1.0.5, DEPLOYED with 1.0.6; TT-16 |
| AppleSkin food bars in JEI recipe slots + catalysts and in Lightman's Currency trade buttons / storage tabs (those screens drew tooltips without the item stack) | patcherMod `JeiRecipeLayoutTooltipMixin`, `JeiRecipesGuiTooltipMixin`, `LCEasyScreenHelperMixin`, `LCEasyGuiGraphicsMixin`, `compat/LightmansTooltipStacks` | BUILT 2026-10-05, in patcherMod 1.0.5, DEPLOYED with 1.0.6; TT-17, TT-18. The other screens: next row |
| AppleSkin food bars in the rest of the food screens: FD cooking pot, Farmer's Respite kettle, Central Kitchen cooking/brewing guides and the Oven (finished-meal slot); Brewin' keg tank (GUI, recipe book, JEI tank over the tankard); Vinery recipe book; TrashSlot; Pretty Pipes terminal; FTB Quests task/reward/description items; Wares agreement; Patchouli books; AE2 crafting tables, pattern encoding amount slots and guidebook | patcherMod `compat/MealSlotTooltip`, `FtbTooltipStacks`, `AeTooltipStacks` + 17 client mixins (list in section 3); the Brewin' and Vinery ports | BUILT 2026-10-20 (patcherMod 1.1.01, Brewin' `-03`, Vinery `-04`), in `mods/`, not tested in game; TT-19 to TT-30 |
| Brewin' and Chewin' (backport): Heating Cask / Ice Crate keg-source lines; "Placeable on a coaster"; keg drink effect lines and the pipe-rule line ("Pipes can only drain it while it isn't fermenting", only while `pipeDrainOnlyWhenIdle` is on) on the keg item, the keg GUI's tank and the JEI Fermenting result tank | tooltips.js "Food / Farmers Delight"; placeable_tooltip.js; the port (`KegItem`, `KegScreen`, JEI `FermentingRecipeCategory`) | BUILT 2026-10-18, DEPLOYED with 1.0.8 (Brewin' `-02`, [plan_1.0.8.md](plan_1.0.8.md) AL); [alcohol.md](alcohol.md) 8.2, 8.3, 8.7, AL-49 to AL-52, AL-64 |
| Alcoholic drinks: Quark's saturation word reads "Intoxicating Drink" instead of "Disgusting Snack" on every food with the Intoxication food effect; Vinery wines list their food effects | patcherMod `food/IntoxicatingDrinkTooltip` (`[alcohol] quarkLine`); the Vinery port (`DrinkBlockItem`) | BUILT 2026-10-20 (patcherMod 1.1.01), not tested; [alcohol.md](alcohol.md) 11, AL-88. The 2026-10-20 KubeJS effect-line rename (`alcohol_tooltip.js`) was retired the same day |
| Shield Shift view "Blocks burning" / "Blocks thorns" lines | patcherMod `ShieldTooltip` ([shields.md](shields.md)) | DEPLOYED with 1.0.6; SH-04 |
| patcherMod tooltips: tier shields, vanilla shield, echo armor set bonus, D&D jetpack crash fix | patcherMod | VERIFIED / see memories |
| Creative Labels: sticky note (its text in the note's colour, then four grey use lines), label (eight grey lines), eraser (four); labelled items' lines in grey under the name (`CreativeLabels.Lines`); Sticky It's tooltip handler cancelled (its "[Shift]" / "Label:" lines, and the empty `{}` tag it put on every hovered item) | the mod: `appendHoverText` on the three items, `item/ItemLabels` (ItemTooltipEvent), `mixin/StickyItTooltipMixin` | BUILT 2026-10-20 (creativeLabels 1.0.0), not in `mods/` yet; lines in [sticky_notes.md](sticky_notes.md) 3.9 |

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
- **PneumaticCraft "Hold Shift for info" (2026-11-02, `ui/tooltips #9`; glycerol only since 2026-10-23, `#6`):** PNC's
  `TooltipEventHandler` shows each item's description (key from `ICustomTooltipName.getTranslationKey`, 96 items in
  PNC's lang) only while Shift is held, in aqua with `§` colour codes, else a "Hold Shift" line. `pnc_tooltips.js`
  (startup script, Forge listener at LOW, after PNC's NORMAL) swaps that hint for the text: wrapped by PNC's own
  `PneumaticCraftUtils.splitString` (which turns `${br}` into line breaks), colour codes stripped, gray. Holding Shift
  still shows PNC's own aqua text. Pack wording goes in `kubejs/assets/pneumaticcraft/lang/en_us.json` (glycerol:
  "Tastes sweet!").
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

**Added for Vinery** ("Vinery" section; text written 2026-10-19 for the 1.5.5 port, [alcohol.md](alcohol.md) 10.15)
- `tooltips.js` (gray): Grapevine Pot, Apple Press, Cask (plus "Sneak right-click with an empty hand to pour the juice
  out"), Grapevine Stem and Lattice (where seeds go; shears give the seed back, breaking a planted one loses it),
  apple / cherry leaves and saplings, Stackable Log. Chorus Wine: "Makes you wake up in strange places" (light purple).
- In the port (its sizes live in its code): every wine's size, "N per placed block" and which storage takes it; each
  rack's and the Wine Box's capacity ("Holds 4 wines (small or medium)"); the Wine Box's sneak line; the aging hint.
  Since 2026-10-20 the fit line names the racks: "Fits in Single, Small, and Big Wine Rack, and Wine Box" (small),
  "Fits in Single and Small Wine Rack, and Wine Box" (medium), "Fits in Single Wine Rack" (large); the racks themselves
  are "Single / Small / Big <Wood> Wine Rack" ([alcohol.md](alcohol.md) 10.18, VB-84, VB-92).
- Brewin' port, every keg heat / cold source: "Heats / Cools the surrounding environment", then "Useful for keg brewing
  and wine casking" on its own line (split 2026-10-20, [alcohol.md](alcohol.md) 10.13, 10.18, AL-77).
- The 2026-10-01 placeholders (1.3.10, incl. Kitchen Sink and Aging Barrel) are gone.

**Added for placeable food** (2026-10-05, [placeable_food.md](placeable_food.md))
- 164 foods: gray "Placeable" / "Placeable (while sneaking)" / "Placeable on plate" / "Placeable on small plate", drawn from the
  `#creativeengineering:placeable*` tags at hover time (`addAdvancedToAll`). Catalog: [placeable_catalog.md](placeable_catalog.md).

**Added by frameworks** (each documented in its own memory):
- armor effects (`armor_effects_tooltip.js`) and tool effects (`tool_effects_tooltip.js`)
- potion buckets (`potion_buckets.js`), vanilla-style effect lines
- tier shields and the vanilla shield (`ShieldTooltip` / `IronShieldTooltip`); the Shift view gained
  `Blocks burning` and `Blocks thorns` Yes/No lines 2026-10-05 (DEPLOYED with 1.0.6). Details and tests: [shields.md](shields.md) 2.6, SH-04
- precision sawblade profile (`addAdvanced`, reads NBT)

**Fixed**
- **Tall tooltips (2026-11-02, `ui/tooltips #5`).** Forge 43.3.9 `Screen.renderTooltipInternal` (`m_169383_`) clamps
  `if (y + height + 6 > screenHeight) y = screenHeight - height - 6`, so a tooltip taller than the screen got a
  negative `y` and lost its name. `ScreenTooltipTopMixin` (`@ModifyVariable` on local slot 9, the clamped `y`, just
  before `pushPose`; slot checked with javap on the Forge client jar) raises it to at least 4, so the box starts at the
  top and runs off the bottom. Legendary Tooltips and Iceberg draw their frames from this position. A short tooltip only
  moves when the mouse is within 16 px of the top of the screen. CEConfig `[tooltips] keepTopOnScreen` (default on,
  read live). Not covered: tooltips FTB Library draws with its own text renderer (text-only quest hovers), and
  Iceberg's own `Tooltips.calculateRect` path if a test shows a tall tooltip going through it (brief step 2).
- **Curios slot line (2026-11-02, `ui/tooltips #8`).** Curios 5.1.6.4 (`ClientEventHandler.onTooltip`) builds
  "Slot: Face, Head" (gold, names yellow) from `getCurioTags(item)` (the `curios:<slot>` tags plus mixins such as
  Simple Hats' head rule), but for an item with a curio capability it passes the line through
  `ICurio.getSlotsTooltip`, which a mod can override to hide it. `compat/CuriosSlotTooltip` (Forge LOW, client, only
  with Curios) adds the same line in the same place to any curio item whose tooltip has no `curios.slot` line.
  CEConfig `[tooltips] curiosSlotLine` (default on). Owner rule in [curios_slots.md](curios_slots.md) 1.
- **AppleSkin food bars (2026-10-20).** AppleSkin only adds its bars in Forge's `RenderTooltipEvent.GatherComponents`
  when the event carries an item. Screens that build an item's lines themselves and draw them with the stackless
  `renderComponentTooltip(pose, lines, x, y)` / `renderTooltip(pose, lines, image, x, y)` send `ItemStack.EMPTY`. Each fix
  draws the same lines with Forge's overload that takes the stack:
  - finished-meal slot ("Served on"): FD `CookingPotScreen`, Farmer's Respite `KettleScreen`, Central Kitchen
    `CookingGuideScreen` / `BrewingGuideScreen` (mixins Creative Delight `farmersdelight.CookingPotMealTooltipMixin` and `farmersrespite.KettleMealTooltipMixin`
    (moved from patcherMod 2026-10-27), patcherMod `CckCookingGuideMealTooltipMixin`, `CckBrewingGuideMealTooltipMixin`,
    helper patcherMod `compat/MealSlotTooltip`); the Oven
    (`OvenScreen`, direct edit).
  - Brewin' port: the keg tank tooltip (keg GUI and its recipe book) carries the item the drink pours into
    (`BnCFluidItemDisplays`). In JEI the fermenting result tank sits over the tankard icon and JEI shows the first
    slot under the mouse (the tank): `JeiRecipeLayoutTooltipMixin` now attaches a RENDER_ONLY item slot's stack under
    the same point when the hovered slot shows no item, so the tank keeps its fluid text and gets the drink's bars.
  - Vinery port recipe book result buttons (`StationRecipeBook`, direct edit).
  - TrashSlot trashed item (`TrashSlotGuiHandlerMixin`), Pretty Pipes item terminal (`PrettyPipesTerminalTooltipMixin`),
    Wares agreement items (`WaresAgreementTooltipMixin`), Patchouli book items (`PatchouliBookTooltipMixin`).
  - FTB Quests: FTB Library draws hover text with its own text renderer. `FtbTooltipStacks` remembers the stack when an
    item's tooltip is copied into that text (`GuiHelper.addStackTooltip`, the item task header, item hover events in
    descriptions); the screen wrappers then draw that list with Forge's renderer. Text-only hovers keep FTB's renderer.
  - AE2: `AEBaseScreen.drawTooltip` is text-only. Crafting CPU / plan table entries and pattern-encoding amount slots
    set a pending stack (`AeTooltipStacks`) just before it; the guidebook's `ItemTooltip` returns Forge's gathered
    components instead of plain text.
  - Side effect: JEI recipe slots, FTB item hovers and AE2 table entries now go through the normal item tooltip path,
    so they also get the JEI mod-name line / tooltip images / Legendary Tooltips frame like inventory tooltips.
- **D&D Jetpack tooltip crash:** `JetpackItemTooltipMixin` stops a crash that used to break client
  tag loading, which delayed JEI (memory `axe-tag-ghost-entries`).
- **WTHIT growth line** for hemp, tobacco, berry bushes, grapes and other plants WTHIT skips: Creative Delight `compat/wthit/WthitCropGrowth` (QoL-6), written up in [crops.md](crops.md) 2.9 (tests CR-59 to CR-66).

**Creative Labels** (2026-10-20, [sticky_notes.md](sticky_notes.md) 3.9 and 3.13)
- The mod's three items carry their own lines (`appendHoverText`, one lang key per line, grey): the sticky note's
  text in the note's colour when written, then "Right-click in the air to write" and three more use lines; the
  label's eight; the eraser's four. The exact text is in sticky_notes.md 3.9.
- Items labelled with a note show their `CreativeLabels.Lines` in grey under the name, no header
  (`item/ItemLabels`, `ItemTooltipEvent`).
- Removed: Sticky It's `TooltipRenderProcedure` is cancelled at its head (`mixin/StickyItTooltipMixin`, applied only
  while `sticky_it` is loaded). It wrote "Label:" lines from the old `LabelName1..3` tags, "[Shift]" on its own
  note and eraser, and created the empty `{}` tag on every hovered item (the bowl stacking bug). Items already
  tagged keep their `{}`.

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
- [placeable_tooltip.js](../kubejs/assets/creativedelight/kubejs/_qol/placeable_tooltip.js): placeable food lines + DD / Fruits Delight removals (tests in [placeable_food.md](placeable_food.md), PL-)
- patcherMod `food/IntoxicatingDrinkTooltip`: Quark's saturation word -> "Intoxicating Drink" on every food with the Intoxication effect (drinks listed in `global.alcohol`, [alcohol.md](alcohol.md) 11)
- [armor_effects_tooltip.js](../kubejs/client_scripts/armor_effects_tooltip.js), [tool_effects_tooltip.js](../kubejs/client_scripts/tool_effects_tooltip.js), [potion_buckets.js](../kubejs/client_scripts/potion_buckets.js)
- patcherMod: [surface/CamoSpeedTooltip.java](../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/surface/CamoSpeedTooltip.java), `shield/ShieldTooltip.java`, `shield/IronShieldTooltip.java`, `mixin/EchoArmorTooltipMixin.java`, `mixin/JetpackItemTooltipMixin.java`
- Lang: [patcherMod lang en_us.json](../development/creativeCommon/src/main/resources/assets/creativecommon/lang/en_us.json) (`creativeengineering.tooltip.*`)
- Item keys: `global.item` in [item_dict.js](../kubejs/startup_scripts/_lib/item_dict.js)
- Renames: `resourcepacks/Copycat Framed Block.zip` → `assets/framedblocks/lang/en_us.json` (backups in `configs_edited/resourcepacks/`)

## 6. Offline checks

- **Syntax:** `node --check kubejs/client_scripts/tooltips.js`.
- **Dead patterns:** after a game load, search `logs/latest.log` for `[addTooltipPattern]` lines ending in `-> 0 matches`.

## 7. In-game test suite

Hover items in JEI or the inventory. BlockRunner's Speed Multiplier line is off since 2026-11-02 (`block_speed_multiplier_tooltip = false`,
`gameplay/blockrunner #6`: walk speed shows in WTHIT only), so TT-03 to TT-05 are retired and TT-42 checks the line is gone.

**Removals**
- [ ] **TT-01 IP asphalt:** hover Asphalt Concrete, its slab and its stairs. → No "Increases walking speed" line on any; no Speed Multiplier line either (2026-11-02).
- [ ] **TT-02 Crushed piles:** hover a Create Compressed crushed aluminium pile. → Name only.

**BlockRunner line** (moved here from the BlockRunner suite)
- ~~**TT-03 Speed values:**~~ retired 2026-11-02 (line off). asphalt (IP and D&D) → "Speed Multiplier: 1.5"; glass → "Speed Multiplier: 1.0"; stone → "Speed Multiplier: 1.05". *(was BR-06)*
- ~~**TT-04 Camo lines**~~ retired 2026-11-02 (line off). (needs the rebuilt patcherMod jar): framed cube → "Speed Multiplier: matches its camo"; framed slab, framed panel, any framed double item, Copycats+ copycat slab or byte panel → "…matches its camo (newest camo sets walk behaviors for hybrid blocks)". Needs `[surface.camouflage] copyCamo = true`. *(was BR-20)*
- ~~**TT-05 Camo off**~~ retired 2026-11-02 (line off). (needs the rebuilt patcherMod jar): set `[surface.camouflage] copyCamo = false` and save. Hover a framed cube → "Speed Multiplier: 1.0, no matter its camo". Set it back to `true`. *(was the tooltip half of BR-21)*

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
- ~~**TT-15 Vinery placeholders**~~ moved to VB-84 to VB-86 ([alcohol.md](alcohol.md) 10.15): the text is written.

- [ ] **TT-16 Underline (1.0.5):** hover a diamond chestplate and a netherite sword in the inventory, over a few launches → the line under the name sits below the spinning model, never across it
- [ ] **TT-17 AppleSkin in JEI (1.0.5):** open the JEI recipe page of bread, hover the output slot and the catalyst column's crafting table → the bread slot shows AppleSkin's hunger/saturation bar; the recipe slots also show tooltip images (bundles), like the item list
- [ ] **TT-18 AppleSkin in Lightman's Currency (1.0.5):** a trader selling cooked beef → hover the trade button → the food bar shows under the item lines
- [ ] **TT-19 Cooking pot meal slot:** cook a stew in a Farmer's Delight cooking pot with no bowl in the container slot, hover the finished-meal slot → name, "Served on: Bowl" line and AppleSkin's food bar
- [ ] **TT-20 Kettle and Oven:** same in a Farmer's Respite kettle (tea) and in the Oven's finished slot → food bar under the "Served on" line
- [ ] **TT-21 Central Kitchen guides:** open a Cooking Guide set to a cooking pot meal, hover its result slot → food bar
- [ ] **TT-22 Keg tank:** fill a keg with beer, hover the tank in the keg GUI → fluid lines plus the tankard's food bar; the recipe book's tank preview the same
- [ ] **TT-23 JEI keg recipe:** JEI Fermenting page of beer, hover the result tank and the tankard drawn inside it → fluid text, and the food bar of the drink
- [ ] **TT-24 Vinery recipe book:** open a Vinery station's recipe book, hover a wine result → food values shown
- [ ] **TT-25 TrashSlot:** trash a cooked steak, hover the trash slot → food bar
- [ ] **TT-26 Pretty Pipes terminal:** store bread in a pipe network, hover it in the item terminal → stored count on the first line and the food bar
- [ ] **TT-27 FTB Quests:** hover an item task (no custom title) for a food, a food item reward, and a food item linked in a quest description → food bar on each; hovering a quest button still shows FTB's plain tooltip
- [ ] **TT-28 Wares:** open a delivery agreement requesting food, hover the requested item → food bar
- [ ] **TT-29 Patchouli:** in any Patchouli book page that shows a food item, hover it → food bar, and the "(Shift for recipe)" line still there
- [ ] **TT-30 AE2:** request a crafting job for bread; hover bread in the crafting plan and in the CPU status table → food bar; in the pattern encoding terminal, put bread in a processing input and hover it → food bar with the amount lines
**Tall tooltips, Curios slots, PneumaticCraft (2026-11-02)**
- [ ] **TT-39 Tall tooltip:** at GUI scale 4 (or a small window), hover an item with a very long tooltip (a shulker box
  full of named items, or a PneumaticCraft module such as the Logistics Module) → the name is at the top of the screen,
  the end runs off the bottom. Set `[tooltips] keepTopOnScreen = false` → vanilla (name cut off). A short tooltip near
  the bottom of the screen still moves up to fit.
- [ ] **TT-40 Curios slots:** hover a Stepstool → "Slot: Feet" (no "Worn in the Feet slot"); a Simple Hats mask →
  "Slot: Face, Head" (any order); the Falconry Glove → "Slot: Hands"; a Sophisticated Backpack and a Lightman's wallet →
  their slot. No item shows the "Slot:" line twice. A non-curio item (stone) has none.
- [ ] **TT-41 PneumaticCraft:** without Shift, hover the Air Canister → its description in gray, wrapped, no "Hold
  Shift" line; the Pressure Gauge Module → the formula and text in gray (no blue or aqua), paragraphs on their own
  lines; Glycerol → "Tastes sweet!" in gray. Holding Shift → PNC's own aqua text.
- [ ] **TT-42 No Speed Multiplier line:** hover asphalt, stone and a framed cube. → No "Speed Multiplier" line; look at the
  placed blocks with WTHIT → the walk speed line is still there (`gameplay/blockrunner #6`).

- [ ] **TT-09 No dead patterns:** search `logs/latest.log` for `[addTooltipPattern]`. → Every line reports more than 0 matches.

## 8. Tooltips To Add

### Items

- [] Straw- Can be collected by cutting grass with a knife
- [] Knifes- Can be used to collect straw by cutting grass
- [] Foods- Food type tooltip (Feast, Sweets, Baked, Fruit, etc)
- [] Cans- Needs appleskin mixin. (shanks, saturation, of single meal, standard appleskin render) x(number of meals in can)
- [] Chalk- Right click to mark block w/ arrow, hold right click to change mark (Chalk 1.6.4 code: it is sneak + right-click that opens the symbol picker; plan in [briefs/ui_tooltips.md](briefs/ui_tooltips.md))
- [] Pretty Pipes- Use pipe wrench w/ block in off-hand to add cover
- [x] Lunchboxes- number of slots: `Slots: N` under the name, BUILT 2026-10-29 (patcherMod `lunchbox/LunchboxTooltip`, [lunchboxes.md](lunchboxes.md) LB-02; scratchpad 2026-10-21, [TODO](todo/TODO.md) `ui/tooltips #2`)
- [] AE2 cables- how to make covers, once the covers are hidden from JEI (scratchpad 2026-10-21, `jei/jei-coverage #3`)
- [] Flower pots- a label on every pot, every pot distinct (scratchpad 2026-10-21, `blocks/framed-and-decor #6`)

Not a line but the tooltip box: large item tooltips should run off the bottom of the screen, not the top (scratchpad
2026-10-21, `ui/tooltips #5`): BUILT 2026-11-02, section 3 "Fixed".

### Mods to Cover
- [] Botania
- [x] Vinery: block, wine and storage lines (2026-10-19, [alcohol.md](alcohol.md) 10.15)
- [] Cloud Storage
- [x] Brewin Chewing + JEI: cask / crate, coaster, keg effect and pipe lines (2026-10-18, [alcohol.md](alcohol.md) 8)

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
- **2026-10-11:** rows for the tooltip fixes in the patcherMod 1.0.5 jar (Legendary Tooltips underline, AppleSkin bars in JEI
  and Lightman's Currency) with TT-16 to TT-18; placeable lines and the shield Shift lines marked STAGED for 1.0.6
  (not deployed). The camo-speed wording row was stale: it shipped long ago.
- 2026-10-14: pack 1.0.5 was never deployed; everything above that was staged for it ships in pack **1.0.6** (STAGED
  2026-10-14, update from 1.0.4, [plan_1.0.6.md](plan_1.0.6.md)), unchanged.
- **2026-10-18:** Brewin' and Chewin' backport lines: Heating Cask and Ice Crate (tooltips.js), "Placeable on a coaster"
  (fifth placeable tag, [placeable_food.md](placeable_food.md)), and in the port the drink's effect lines plus the pipe
  rule on the keg item, keg tank and JEI result tank ([alcohol.md](alcohol.md) 8.2, 8.3, 8.7).
- 2026-10-19: the Brewin' lines above STAGED in pack 1.0.8 ([plan_1.0.8.md](plan_1.0.8.md) AL).
- 2026-10-19 late: Vinery text written for the port (`tooltips.js` block lines and Chorus Wine; wine size and storage
  capacity lines in the port), placeholders and the dead grape key removals dropped. TT-15 retired to VB-84 to VB-86
  ([alcohol.md](alcohol.md) 10.15).
- 2026-10-20: "Intoxicating Drink" replaces the Intoxication line on every alcoholic drink; Vinery wines list their food
  effects ([alcohol.md](alcohol.md) 11, AL-88).
- 2026-10-20 (later): AppleSkin food bars added to the remaining food screens and JEI's keg tank (section 3 "Fixed",
  TT-19 to TT-30). Built into patcherMod 1.1.01, Brewin' `-03` and Vinery `-04`, installed in `mods/`, not tested in game.
- 2026-10-20: test round: the Vinery fit lines were clunky, so they now name the racks, renamed Single / Small / Big
  Wine Rack; the Brewin' heat / cold line puts "Useful for keg brewing and wine casking" on its own line
  ([alcohol.md](alcohol.md) 10.18; VB-84, VB-92, AL-77).
- 2026-10-31: scratchpad notes translated into TODO rows (`ui/tooltips #12`). No code changed.
- 2026-11-02: `ui/tooltips #5` tall tooltips keep their top (`ScreenTooltipTopMixin`), `#8` Curios "Slot:" line on
  every curio item (`CuriosSlotTooltip`), both CEConfig `[tooltips]`; `#9` every PneumaticCraft "Hold Shift" hint
  replaced by the description in plain gray (`pnc_tooltips.js`, was glycerol only, in aqua); stepstools' "Worn in the
  Feet slot" line dropped. patcherMod 1.1.07 built and installed. `#12` (tree fruit season lines) was already built
  with `farming/crops #5` on 2026-10-30. TT-39 to TT-41.
