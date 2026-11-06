# blocks / dyeing: implementation brief

Written 2026-10-31 from [TODO](../todo/TODO.md) `## Dyeing` (`blocks/dyeing #1` In Progress, #2 to #5 Planned).
Feature writeup: [dyeing.md](../dyeing.md) (audit section 2, owner decisions and build plan section 3; test prefix `DY-`,
DY-01 to DY-14 written, none run). New tests are placeholders `DY-newN` (proposed, numbered when queued).

**Checked 2026-10-31:** Supplementaries 2.4.20, Moonlight 2.3.7, Colored Water 1.19.2-1.3.7, Glow Ink Plus 1.2.0,
Chalk 1.6.4, CC: Tweaked 1.101.4, Create 0.5.1.i (all in `mods/`). Dumps: `kubejs/exported/world_jei/tool_pairs.json`
(2026-10-26: 160 colour sets, `dye_blocked`, `soap`, `soap_washable`), `kubejs/exported/tags/*` and `fluids.json`
(2026-10-30). Nothing of dyeing.md 3.8 is built (no soap hook, no new tags, no changelog bullet).

## Scope and order

| Step | Row | Work | Notes |
|---|---|---|---|
| 1 | `dyeing #1` steps 1-2 (disables, soap targets) | KubeJS tags + patcherMod | Small; fixes player-visible soap results (glass to white) |
| 2 | `dyeing #1` step 3 (dye with block-entity carry-over) | patcherMod | Needs the DY-07 dupe check before shipping |
| 3 | `dyeing #4` Glow dye item | KubeJS item + recipes, patcherMod for the in-world glow step | Shares the glow wool code of `dyeing #1` step 5; build with it |
| 4 | `dyeing #1` steps 4-7 | patcherMod adapters, generator, JEI tabs | Largest part |
| 5 | `dyeing #2` Coloured water fan dyeing | patcherMod fan type + generated recipes | Independent; reuses the colour-set dump |
| 6 | `dyeing #5` Cauldron washing | KubeJS event or patcherMod | Small, independent |
| 7 | `dyeing #3` Placeable dyed string | patcherMod blocks + KubeJS recipes | Independent; new items |

Conventions: tags through `global.tags` (block keys in `kubejs/startup_scripts/_lib/block_dict.js`), recipes through
`global.NCH`, item ids in `global.item`. Dyeing is not food, so the KubeJS parts stay in the pack's `server_scripts/`
(not Creative Delight). Each built step gets a bullet under a `## Dyeing` family in `unreleased.md` (dyeing.md 3.8).

---

## dyeing #1: Block dyeing and soaping audit
Status **In Progress** (audit done 2026-10-27, build not started) · Work: patcherMod / KubeJS / docs · id `blocks/dyeing #1`

### Goal
Carry out the owner decisions of dyeing.md 3 (3.1 disables, 3.2 dye with carry-over, 3.3 soap targets, 3.4 soap
adapters, 3.5 new families, 3.6 unclean soap, 3.7 tool interactions, 3.8a glow and chalk), in the order of the 3.8 plan.

### Now
- In-world dye: Supplementaries `[tweaks.dye_blocks] enabled = true` (`config/supplementaries-common.toml:451-453`),
  limited by patcherMod
  [SupplementariesDyeBlocksMixin.java:32-46](../../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/SupplementariesDyeBlocksMixin.java)
  (HEAD of `DyeBehavior.tryPerformingAction`: cancels for any state with a block entity or in
  `#creativeengineering:no_world_dye`).
- `b.t_no_world_dye` ([block_dict.js:167](../../kubejs/startup_scripts/_lib/block_dict.js#L167)) is filled with `[]`
  in [blocktags.js:75-78](../../kubejs/server_scripts/tags/blocktags.js#L75-L78).
- Soap: Supplementaries `[functional.soap]`, `clean_blacklist = ["minecraft:glazed_terracotta"]`
  (`supplementaries-common.toml:169-172`); the logic is Supplementaries' `SoapWashableHelper` (S1-S3). The pack has no
  soap hook yet.
- Audit inputs: the `/dumpworldjei` dump (devtool `WorldJeiToolPairs.java`), JEI world tabs
  `kubejs/assets/creativeengineering/jei_world/{dyeing,soaping}/{generated,manual}.json`, generator
  `tools/gen_jei_world.py` (`/cegen run jei_world`, generators.json:450).

### Build (dyeing.md 3.8; steps in order)
1. **Disables (3.1).** Tags (blocktags.js, next to line 78): `t.add(e, b.t_no_world_dye, [...])` with
   `#botania:mystical_flowers`, `#botania:double_mystical_flowers`, `#botania:shiny_flowers`, `#botania:floating_flowers`,
   `#botania:mini_flowers`, the Botania mushrooms and petal blocks, buried petals (dump has all these tags); a new
   `b.t_no_world_soap = '#creativeengineering:no_world_soap'` with the same list. patcherMod: soap mixin on
   `SoapWashableHelper` (HEAD, cancel for `no_world_soap`, banners with any pattern layer, flags with patterns); the dye
   mixin also cancels on patterned banners; Colored Water's dye interaction off (mixin on its use handler; find the
   class with `javap` on the jar); CC: Tweaked `TileTurtle` water-bucket wash off (W1).
2. **Soap targets (3.3).** In the same soap hook: toolbox → `create:brown_toolbox` (contents kept: copy the block
   entity as S1 does), valve handle → `create:copper_valve_handle`, every glass colour set → its clear member (never
   the white one). Glass sets: derive from `color_sets` in the dump (members whose base id ends in `glass`/`glass_pane`).
3. **Dye with carry-over (3.2).** Replace the block-entity skip in `SupplementariesDyeBlocksMixin` with a copying swap
   for an allow-list tag `#creativeengineering:world_dye_keeps_data` (shulker boxes, sacks, presents, sharestones,
   wells, silt pots, Tinted Campfires, ProjectRed lamps, pet beds, Framed glass doors, single-colour banners and flags,
   Create toolboxes and valve handles). Save the BE with `saveWithoutMetadata()`, set the new state with flag 2 and no
   drops (`level.setBlock(pos, newState, 2 | 16)`; remove the old BE first so containers don't spill), load the tag into
   the new BE. Canvas sign stays text-only. DY-07 before shipping.
4. **Soap adapters (3.4).** One small class per mod in patcherMod `compat/soap/` (CfB, Create belt / nixie / flap
   display, Steam Rails, PneumaticCraft, framed target, CC: Tweaked, cat collars), each `boolean tryWash(level, pos,
   state, player)`, called from the soap hook. Each mod guarded by `ModList.isLoaded`.
5. **New families (3.5)** as Moonlight colour sets or own adapters; glow wool two-step and glow ink on wool / carpet
   (1 ink per block). Shares code with `dyeing #4`.
6. **Unclean pairs and tools (3.6, 3.7).** A generator `tools/gen_unclean_pairs.py` (listed in generators.json, reads
   `block_tags_by_block.json` and the registry dump) writes `kubejs/generation_scripts/dyeing/unclean_pairs.json`
   (mossy / overgrown / rusty / dirty / sandy / ashen / muddy / floral → clean twin, shapes kept); patcherMod loads it as
   data and the soap hook applies it. Shears on overgrown and floral, hoe on muddy mangrove roots: a tool-action hook
   (`PlayerInteractEvent.RightClickBlock`), no drops.
7. **JEI world tabs:** `/dumpworldjei`, then `/cegen run jei_world`; manual rows for anything the dump can't see.
8. Changelog bullets per step (`## Dyeing`), dyeing.md 3 marked built per step, DY tests queued.

### Ids and data
- Verified: the files and config keys above; Botania tags (dump). Create ids `create:brown_toolbox`,
  `create:copper_valve_handle` (Create 0.5.1.i registry names, not re-checked in the dump).
- Unverified: Colored Water's interaction class; Moonlight's set-registration API for 3.5 (check `BlocksColorAPI` in
  the Moonlight 2.3.7 jar).

### Decisions (open, dyeing.md 3.8a)
- **Crushed glow dye ratio and recipe:** see `dyeing #4` D1.
- **Chalk on wool:** recommend any wool or carpet (the chalk sets the colour, like dye), one chalk use per block.
- **Glow chalk colours and light level:** recommend the same 16 colours as Chalk, marks at light 7 (dyeing.md suggests 7).
- **Glow chalk marks and soap:** recommend yes, soap removes them like plain marks.

### Gotchas
- The sack loss of 2026-10-20 is why the block-entity skip exists; step 3 must remove the old BE without dropping its
  contents and must not run on blocks outside the allow-list.
- Supplementaries' soap copies block-entity data already (S1); the new soap hook must not double-copy.
- Banners: the crafting-grid recipes (banner + dye, banner + soap) are out of scope and keep working on any banner.

### Tests
Existing: DY-01 to DY-06 (dyeing.md 2.9), DY-07 to DY-14 (3.9). None queued in `scratch_queue.md` yet: queue DY-07,
-08, -10, -12 as scratch-server checks (block swaps and tags read on the server), the rest as client checks.

---

## dyeing #2: Coloured water dyeing
Status **Planned** · Work: KubeJS (really patcherMod fan type + generated recipes) · id `blocks/dyeing #2`

### Goal
A Create fan (bulk washing) recipe through the thickest coloured water that dyes any dyeable item or block to that
colour. "The extra dye in the recipe becomes end stone dust" (TODO wording, see D2).

### Now
- Colored Water 1.19.2-1.3.7: per colour three strengths, plain (water bucket + 1 dye), condensed (`condense_*`,
  water bucket + 2 dyes) and luminous (`luminous_*`, `luminous_condense_*`, with glow ink: recipes `luminous_*2.json`),
  each a fluid `colored_water:<strength_><colour>_fluid`, a bucket and a cauldron block (live `fluids.json`; jar
  `data/colored_water/recipes/condense_red.json`). "Thickest" = `condense_<colour>`.
- The coloured fluids are in no fluid tag (`fluid_tags.json`: `minecraft:water` = water and flowing water only), so
  Create's splashing never runs on them: a fan through coloured water does nothing today.
- The in-world dye interaction of coloured water is to be disabled (dyeing.md 3.1, `dyeing #1` step 1); this row is
  separate.
- Model for a custom fan type: patcherMod `fan/DragonBreathingFanProcessingType.java`, `DragonBreathingRecipe(Type)`
  (memory `create-fan-processing-type`); NCH has `R.dragonBreathing` (neoCraftHelper.js:1341) and `R.createSplashing`
  (1330) as patterns for a helper.
- Colour sets for "any dyeable item or block": `color_sets` in `tool_pairs.json` (160 sets, colour → id maps).
- No end stone dust item exists in the pack (display_names dump has none).

### Build
1. patcherMod `fan/ColoredWaterFanProcessingType` (one type per colour, or one type whose recipe carries the colour):
   `isValidAt` = fluid at the position is `colored_water:condense_<c>_fluid` (source or flowing); priority above
   SPLASHING (400) so plain-water recipes don't also fire; particles in the colour. Recipe type
   `creativeengineering:dyeing_<c>` or one `creativeengineering:fan_dyeing` with a `color` field (recommend one type with
   a field: 1 serializer, 1 JEI category).
2. NCH helper `fanDyeing(output, input, color, e, idOverride)` next to `dragonBreathing` (patcherMod jar file: rebuild +
   restart).
3. Generator `tools/gen_fan_dyeing.py` (generators.json entry, dump `dumpworldjei`): for every colour set, every member
   (and the base) → the `<c>` member, as items; writes `kubejs/generation_scripts/dyeing/fan_dyeing.json`; a server
   script `kubejs/server_scripts/other/fan_dyeing.js` loops it through `R.fanDyeing`. Also wool, carpet, terracotta,
   glass, concrete powder, beds, candles, banners (plain only).
4. JEI: a category for the new type (patcherMod JEI plugin) showing the fluid as catalyst.
5. Fluid use: none per item (as splashing). D3.

### Ids and data
Verified: fluid ids, recipe shapes, tags (above). New: recipe type id, ~2000 generated recipes (160 sets x up to 16).

### Decisions
- **D1 Scope.** Items on belts / in the air only (Create fan), or also entities (sheep) through `affectEntity`?
  Recommend items only, plus sheep (cheap, fun).
- **D2 "The extra dye becomes end stone dust".** The meaning is unclear and the item does not exist. Readings: (a) the
  second dye of the condensed water is lost and the recipe gives a by-product; (b) a typo for another dust. Recommend:
  ask the owner; build without a by-product.
- **D3 Fluid drain.** Recommend none (like water splashing); the cost is the 2 dyes per bucket.

### Gotchas
- Plain coloured water (1 dye) and luminous variants: only `condense_*` processes (per the row). Luminous condensed
  could give the glow version (with `dyeing #4`); leave it out until #4 is decided.
- 2000 recipes in JEI: hide them behind one category; don't add each as a world-tab row.

### Tests
(proposed, numbered when queued)
- DY-new1 fan through condensed red water: white wool, a blue concrete powder and a white candle on a belt come out red;
  plain red water does nothing.
- DY-new2 JEI shows the fan dyeing category with the condensed water as catalyst.

---

## dyeing #3: Placeable recoloured string
Status **Planned** · Work: patcherMod / KubeJS · id `blocks/dyeing #3`

### Goal
Dyed string (16 colours) that places as a string line, like tripwire, in its colour.

### Now
- No dyed string in the pack (display_names dump: only `minecraft:string`, Botania `mana_string` and `red_string`).
- Vanilla string places `minecraft:tripwire`; `TripWireBlock` connects only to itself and `TripWireHookBlock`.
- Dye items: `i.dye_<colour>` ([item_dict.js:2060-](../../kubejs/startup_scripts/_lib/item_dict.js#L2060)).

### Build
1. patcherMod: 16 blocks `creativeengineering:<c>_string` (class `DyedStringBlock extends TripWireBlock`, decorative:
   no hook signal, D1), 16 `ItemNameBlockItem`s with the same id placing them; blockstates and models copied from vanilla
   tripwire with a `tintindex`, tinted by a `BlockColor` / `ItemColor` per colour (one texture). Loot table: drops itself.
2. Item dict keys `i.<c>_string` (generated by the dict tools or hand block), tags `#forge:string` (so recipes take it)
   and `#creativeengineering:dyed_string` via `global.tags`.
3. Recipes (`kubejs/server_scripts/other/`): `R.shapeless(Item.of(i.<c>_string, 8), [8 x i.string, i.dye_<c>], e)`;
   soap or water back to plain string (with `dyeing #5`).
4. Shears cut it without a drop change; lang names "<Colour> String".

### Ids and data
New: `creativeengineering:<colour>_string` (item and block). Verified: no conflict in the dump.

### Decisions
- **D1 Tripwire function.** Recommend decorative only (no hook signal): dyed string connects only to the same colour.
- **D2 Into `#forge:string`?** Recommend yes, so bows, wool and Fairy Lights crafts take it; check it doesn't reach
  the Supplementaries / Farmer's Delight rope recipes in an unwanted way.

### Gotchas
- `TripWireBlock.shouldConnectTo` checks `this`: subclass instances connect to their own block only (wanted).
- If dyed string joins `#forge:string`, any recipe that turns string into wool (vanilla 4 string → white wool, if it
  reads the tag) would undo the colour: check the live recipes and give coloured wool or exclude the dyed tag.

### Tests
(proposed, numbered when queued)
- DY-new1 craft 8 red string; place a line between two blocks: it connects and draws red; a tripwire hook on it gives no signal.

---

## dyeing #4: Glow dye item
Status **Planned** · Work: KubeJS / data · id `blocks/dyeing #4`

### Goal
A glow dye with a better glow-to-dye ratio than glow ink sacs, which dyes and adds the glow in one use. Owner answers
(dyeing.md 3.8a): the item is **crushed glow ink dye**, made by crushing glow ink, one glow ink per dye to start; it dyes
like a normal dye and gives the glow version in one step. Also chalk dyeing wool / carpet and glow chalk (3.8a).

### Now
- Glow ink: `i.glow_ink = 'minecraft:glow_ink_sac'` (item_dict.js:1297).
- Glow Ink Plus 1.2.0: `glow_ink_plus:glow_<colour>_wool` and `_carpet` (16 x 2), 33 recipes, e.g.
  `glow_red_wool.json` shapeless `[red_wool, glow_ink_sac]` → 1. Two steps today: dye the wool, then add glow ink.
- Chalk 1.6.4: `i.t_chalks = '#chalk:chalks'` (item_dict.js:177), `i.chalk_red = 'chalk:red_chalk'` etc.
- Pack crush helpers: `R.createCrushing`, `R.createMilling` (neoCraftHelper.js:1542, 1560), `R.ieCrushing` (2139).

### Build
1. Items (D2): a base `creativeengineering:crushed_glow_ink_dye` (the owner's name, key `i.crushed_glow_ink_dye`) and
   16 coloured `creativeengineering:glow_<c>_dye` (keys `i.glow_dye_<c>`), registered in KubeJS
   `StartupEvents.registry('item')` (pack startup, not CD: dyeing is not food), textures = the dye sprite with a glow
   overlay. The coloured ones are what does the one-step dye + glow on a block.
2. Recipes: `R.createMilling(Item.of(i.crushed_glow_ink_dye, N), i.glow_ink, e)` and `R.ieCrushing(...)`; then
   `R.shapeless(Item.of(i.glow_dye_<c>, 2), [i.crushed_glow_ink_dye, i.dye_<c>], e)` (D1 ratio).
3. Grid: `[<c> glow dye, white_wool]` → `glow_ink_plus:glow_<c>_wool` (and carpet, 8 + 1 → 8) via NCH.
4. In world (with `dyeing #1` step 5): patcherMod hook: a glow dye on wool / carpet swaps to the glow block; the
   Supplementaries dye behaviour must see it as a dye of its colour (`#forge:dyes/<c>` tag on the glow dyes) for other
   colour sets.
5. Chalk on wool / glow chalk (3.8a): patcherMod, same hook; glow chalk as 16 new items only if D3 says so.

### Ids and data
New: `creativeengineering:crushed_glow_ink_dye`, `creativeengineering:glow_<colour>_dye` (16). Verified: Glow Ink Plus ids and recipe shape.

### Decisions
- **D1 Ratio.** Owner: one glow ink per dye to start. Recommend 1 glow ink → 2 crushed glow ink dye (milling), each +
  a dye → 2 coloured glow dyes: 4 glow blocks per ink, against 1 today.
- **D2 Item or fluid.** Recommend item (16 coloured), no fluid; a glow fluid can come with `dyeing #2` luminous water.
- **D3 Glow chalk.** Recommend 16 colours like Chalk, light level 7 marks, soap removes them.

### Gotchas
- Tagging the glow dyes `#forge:dyes` makes every dye recipe take them (cheap-glow loophole is fine, they cost more
  than a dye).
- Glow Ink Plus' own 2-step recipes stay.

### Tests
Existing: DY-14 (dyeing.md 3.9: glow dye on wool and carpet, chalk, glow chalk). (proposed, numbered when queued)
- DY-new1 mill a glow ink sac: N crushed glow ink dye; it + red dye → 2 red glow dyes; red glow dye + white wool → glowing red wool.

---

## dyeing #5: Water in a cauldron for washing recipes
Status **Planned** · Work: KubeJS / patcherMod · id `blocks/dyeing #5`

### Goal
Right-click a water cauldron with some items to wash them, for decor washing recipes that today need a Create fan.
Not progression: e.g. Old and Weathered cardboard boxes.

### Now
- Decor washing recipes are Create splashing only:
  [misc_mods.js:63-64](../../kubejs/server_scripts/other/misc_mods.js#L63-L64) (Dustrial Decor cardboard box → Old
  Cardboard Box `i.cardboard_box` = `design_decor:cardboard_box` → Weathered `i.weathered_cardboard_box` =
  `creativeengineering:weathered_cardboard_box`, `merges/materials #1`), the rust → clean bulk list
  ([misc_metal_crafts.js:55-57](../../kubejs/data/creativeengineering/kubejs/metal/misc_metal_crafts.js#L55-L57)), feature ports
  (`feature_ports.js:45`), cornmeal → raw tortilla (CD `corn_delight/recipes.js:39`).
- No pack code uses a cauldron interaction (no `CauldronInteraction` / `water_cauldron` in kubejs or patcherMod).
- JEI world tabs exist for other interactions (`kubejs/assets/creativeengineering/jei_world/<kind>/manual.json`).

### Build
1. A list `global.cauldronWashing = [[input, output], ...]` in a startup file, filled by NCH: add an optional flag to
   `R.createSplashing` (`{ cauldron: true }`) that also records the pair, so one call defines both routes. Mark the decor
   ones (cardboard boxes; rusty to clean decor metal, D1).
2. Server: `BlockEvents.rightClicked('minecraft:water_cauldron', e => ...)` in `kubejs/server_scripts/other/cauldron_washing.js`:
   held item in the list → give the output (same count, or one per click, D2), lower the level
   (`LayeredCauldronBlock.lowerFillLevel` via a Java call, or set `level` state - 1), splash sound, cancel.
3. JEI world tab rows: `jei_world/cauldron_washing/manual.json` (or generated from the list).

### Ids and data
Verified: the recipe lines and dict keys above.

### Decisions
- **D1 Which items.** Recommend the cardboard boxes and the decor rust → clean pairs; not the cornmeal tortilla (food,
  CD) and not ore washing (progression).
- **D2 Per click.** Recommend the whole held stack per click and one water level per click (vanilla washes one item per
  level; a stack is kinder for decor).
- **D3 KubeJS or patcherMod.** Recommend KubeJS (`BlockEvents.rightClicked`), no Java needed.

### Gotchas
- Vanilla `CauldronInteraction.WATER` already handles leather, banners and shulker boxes; don't cancel those items.
- The event fires for both hands: act on the main hand only, or the off hand washes too.

### Tests
(proposed, numbered when queued)
- DY-new1 right-click a full water cauldron with 16 Old Cardboard Boxes: 16 Weathered Cardboard Boxes, cauldron level 2.
