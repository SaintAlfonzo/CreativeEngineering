# Brief: cooking / kitchen

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# cooking`, Sub `kitchen`; swept 2026-10-31 (open rows re-checked,
built rows cut down to the table at the end). What got built is written up in [kitchen.md](../kitchen.md) (KT-, last
KT-24), [remainder_buffer.md](../remainder_buffer.md) (RB-), [stepstool.md](../stepstool.md) (STL-) and
[crops.md](../crops.md) 2.3 (hoe area, CR-, last CR-99).

Paths below are relative to `development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/` unless they
start with `kubejs/`, `config/`, `tools/`, `development/` or `feature_writeup/`.

## Scope and order

Open rows: `kitchen #11` (Planned), `kitchen #16` (Planned). Everything else in the Kitchen table is Testing (table at
the end); the full build sections of 2026-10-21 to 2026-10-25 were cut on 2026-10-31 because the writeups above now hold
the same facts.

| Step | Row | Work | Notes |
|---|---|---|---|
| 1 | `kitchen #16` scythe | patcherMod (one item class) + KubeJS tags and recipes | Mostly data: Quark's hoe harvesting and the pack's `hoe_harvest/NxN` tags already do area harvest up to 9x9 |
| 2 | `kitchen #11` decor | art + KubeJS blocks | Needs a source decision and owner art; no jar in the pack has a grinder, cutlery or baking-sheet block |

---

## kitchen #16: Scythe
Status Planned / Work patcherMod / TODO `cooking/kitchen #16` (split from #10, 2026-10-21)

### Goal
A scythe per tier that only harvests: right-click ripe crops in an N x N square and replant them, with a bigger
area than the hoe of the same tier (wood 3, iron 5, diamond 7, netherite 9 in the row).

### Now
- **Area harvest already exists for hoes** ([crops.md](../crops.md) 2.3): Quark's Simple Harvest (right-click, replant)
  and Hoe Harvesting (break an area), sized per item by the tags `creativeengineering:hoe_harvest/<N>x<N>`, N 1 to 9
  ([crops/HoeHarvestArea.java](../../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/crops/HoeHarvestArea.java),
  `MAX_SIZE = 9`; largest tag wins; even sizes reach toward the look). Mixins `mixin/QuarkHoeAreaHarvestMixin`
  (`SimpleHarvestModule.click`) and `mixin/QuarkHoeAreaBreakMixin` (`HoeHarvestingModule.onBlockBroken`). Tooltip
  `crops/HoeHarvestTooltip` ("Harvest area: NxN", lang `creativeengineering.hoe_harvest.*`), registered at
  `CreativeEngineering.java` 221, works for any item `HoeHarvestArea.size` accepts.
- **What counts as a hoe** (Quark 3.4-418 `HoeHarvestingModule.isHoe`, read from the bytecode 2026-10-31):
  `instanceof HoeItem`, or in `#forge:tools/hoes`, or `canPerformAction(stack, ToolActions.HOE_DIG)`. Anything else
  gets size 1.
- Hoe sizes today ([equipmenttags.js:39-43](../../kubejs/server_scripts/tags/equipmenttags.js)): wood / stone 1, copper /
  gold 2, iron / IE steel 3, diamond / netherite / elementium / manasteel 4, echo / felsic / dragon 5. Untagged hoes
  use Quark's radius (`config/quark-common.toml` `[tweaks.hoe_harvesting]` 2 / 3).
- `[tweaks.simple_harvest]`: empty-hand harvest on, "Harvesting Costs Durability" false.
- No scythe item: only `mca:scythe` (`i.scythe`, item_dict.js 11818, MCA's reaper weapon). Leave it alone.

### Build
1. **Item class** `crops/ScytheItem.java`: `extends TieredItem`, `canPerformAction` true for `ToolActions.HOE_DIG`
   only (Quark then treats it as a hoe; no `HOE_TILL`, so it does not till and is not in `#forge:tools/hoes`).
   Register four in `item/CreativeEngineeringItems.java`: `creativeengineering:wooden_scythe`, `iron_scythe`,
   `diamond_scythe`, `netherite_scythe` (vanilla tiers, `Tiers.WOOD` ... `NETHERITE`; netherite `fireResistant()`).
   Durability (if the decision below keeps it): add one `hurtAndBreak` per click, not per crop, in the
   `QuarkHoeAreaHarvestMixin` replay, only for `ScytheItem` (hoes keep Quark's no-durability setting).
2. Dict keys in `kubejs/startup_scripts/_lib/item_dict.js` beside the hoes: `i.wooden_scythe`, `i.iron_scythe`,
   `i.diamond_scythe`, `i.netherite_scythe`.
3. Tags through `global.tags` in `equipmenttags.js` "Hoe harvest area": `hoe_harvest/3x3` wood, `5x5` iron, `7x7`
   diamond, `9x9` netherite. Nothing else in Java reads sizes.
4. Recipes through `global.NCH` in `kubejs/server_scripts/equipment/` (a new `scythe.js`, beside `stepstool.js`):
   `R.shaped(i.iron_scythe, ['MMS', '  S', '  S'], { M: i.ingot_iron, S: i.stick }, e, 'iron_scythe')` per tier
   (`i.planks`, `i.ingot_iron`, `i.diamond`); netherite by `R.smithing(i.netherite_scythe, i.diamond_scythe,
   i.ingot_netherite, e, 'netherite_scythe')` (NCH `smithing`, line 941).
5. Lang names and item models / textures (owner art, `kubejs/assets/creativeengineering/textures/item/`); the hoe
   tooltip lines appear by themselves.
6. crops.md 2.3 gets a scythe paragraph and the CR tests; changelog bullet under a Farming or Crops heading.

### Ids and data
New: `creativeengineering:{wooden,iron,diamond,netherite}_scythe`. Existing: tags `creativeengineering:hoe_harvest/NxN`,
`Tags.Items.TOOLS_HOES` (`#forge:tools/hoes`, `i.any_hoe`), Quark `HoeHarvestingModule.isHoe` (verified), NCH
`shaped` / `smithing` (verified). Texture source unverified (owner art or a recolour).

### Decisions
- Areas per tier (row: "areas per tier vs the hoe_harvest tags"): **the row's 3 / 5 / 7 / 9** (each two sizes above
  the same tier's hoe, odd so it centres). Copper / gold / end-gear scythes: not now.
- KubeJS item in `#forge:tools/hoes` instead of a Java class: **no**. It needs no Java, but every `i.any_hoe` recipe
  slot (FD salvaging, `farmersdelight/recipes.js` 64-74) would take it, and other mods treat that tag as tilling hoes.
- Durability: **1 per harvest click** (a 9x9 netherite scythe lasts 2031 clicks); Quark's own setting stays off for hoes.

### Gotchas
- `HoeHarvestArea.size` returns 1 when Quark's Hoe Harvesting module is off; the scythe then does nothing special.
- A 9x9 click replants 81 crops in one tick through Quark's loop; watch for lag on Create harvester farms (none expected).
- Do not reuse the `i.scythe` key; it is MCA's.

### Tests (proposed, crops.md prefix)
- CR-new1 (proposed, numbered when queued): each scythe's tooltip shows its area; right-click ripe wheat harvests and replants that square,
  unripe crops stay.
- CR-new2 (proposed, numbered when queued): a scythe does not till grass or dirt and is not accepted in an FD salvaging recipe.
- Scratch: a probe that gives each scythe and checks `HoeHarvestArea.size` (3 / 5 / 7 / 9) and the recipe ids.

---

## kitchen #11: Kitchen decor
Status Planned / Work KubeJS / TODO `cooking/kitchen #11`

### Goal
Decorative meat grinder, pots, pans, baking sheets and silverware for kitchens.

### Now (jars checked 2026-10-31)
- No jar in `mods/` has a grinder, cutlery, silverware, frying-pan or baking-sheet block (blockstate scan of every jar).
  The only kitchen-ware blocks: Farmer's Delight's skillet and cooking pot (real stations, nested in Creative Delight;
  `i.skillet` item_dict.js 11657), Veggies Delight's `lasagna_pan` (a food block), Handcrafted 2.0.6 cups, plates,
  bowls, thin / medium / thick / wide pots (flower-pot shapes), Beautify's `hanging_pot` (flowers), Display Delight
  plates.
- Fantasy Furniture 6.7.0 decorations: bowls, platters, tankards, chalices, cookie jar, muffins. Another Furniture 2.1.4,
  Interiors 0.5.6, Decorative Blocks 3.0.0: nothing kitchen-ware. Farmhouse Decorations is in `disabledmods/`.
- The pack's bakeware (`creativeengineering:baking_sheet`, `muffin_tin`, `pie_tin`, `cake_pan`) are plain items, defined in
  [bakeware.js](../../development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/bakeware.js)
  (Creative Delight jar, lines 15-18 onward).
- Pack decor blocks: [decor_blocks.js](../../kubejs/startup_scripts/items/decor_blocks.js) (31 lines; hand-authored
  blockstate and models on disk, `.model()` points at them).

### Build
1. Owner art first (models and textures). Then KubeJS blocks in `kubejs/startup_scripts/items/decor_blocks.js` (or a
   sibling `kitchen_decor.js` once it passes ~5 blocks): `creativeengineering:meat_grinder`, `frying_pan_decor`,
   `cutlery_set`, `baking_sheet_stack`, each `.notSolid()` with a `box(...)` shape; models under
   `kubejs/assets/creativeengineering/{blockstates,models/block,models/item}/`. Keys in `item_dict.js` and
   `block_dict.js`.
2. Placed bakeware (optional): a block form of each bakeware that stays in `#creativeengineering:bakeware/<kind>`; the
   oven's tool slot tests the tag only. That is a Creative Delight change (bakeware.js lives in the CD jar: rebuild).
3. Recipes through `R.shaped` / `R.shapeless`; a "Decorative" tooltip via `addTooltips` in
   [tooltips.js](../../kubejs/client_scripts/tooltips.js) (line 15).

### Ids and data
New ids above (to add). Verified 2026-10-31: no existing block to reuse. FD skillet / pot are real stations, not decor.

### Decisions
- Source (row: "source"): **own KubeJS blocks with owner art**; nothing in the pack to borrow.
- "Pots and pans": **count FD's skillet and cooking pot as the pans and pots** (they already place); build only the
  grinder, cutlery and baking-sheet stack.

### Gotchas
- KubeJS blocks with custom models need the model and texture in `kubejs/assets`, which ships to the client.
- Decor in the CD jar would need a CD rebuild; keeping it in the pack folder needs only a restart.

### Tests (proposed, kitchen.md prefix)
- KT-new1 (proposed, numbered when queued): each decor block places, shows its model, has its shape, and breaks back into itself.

---

## Built, needs play

| Row | Change | Open tiers | Tests | Writeup |
|---|---|---|---|---|
| kitchen #1 | Remainder buffer on pot, kettle, oven | C P | RB-01 to RB-14 | [remainder_buffer.md](../remainder_buffer.md) |
| kitchen #3 | Remainder buffer WTHIT "Holding: ..." line | C P | WL-32, WL-33 | [remainder_buffer.md](../remainder_buffer.md) 3.3, [wthit_lines.md](../wthit_lines.md) 2.9 |
| kitchen #4 | Duplicate bowl (no duplication; recycled bowl) | P | RB-13 | [remainder_buffer.md](../remainder_buffer.md) 3.1 |
| kitchen #5 | Returned serve items don't stack (Sticky It! `{}` tag) | C P | SN-48 | [sticky_notes.md](../sticky_notes.md) 3.13 |
| kitchen #6 | Fridge (2-tall box on top) | C P | KT-06, CQ-TX-03 | [kitchen.md](../kitchen.md) 2.4 |
| kitchen #7 | Fridge sort item loss (`QuarkSafeSort`) | C P | FS-01 to FS-05 | [testing/fridge_sort/plan.md](../testing/fridge_sort/plan.md) |
| kitchen #8 | Counters, stove, sink (oven fuel, sink water, cutting board) | C P | KT-01 to KT-05 | [kitchen.md](../kitchen.md) 2.1-2.3 |
| kitchen #10 | Bamboo bowl, mortar and pestle grinding | P | KT-07, KT-08, KT-13, RC-19 | [kitchen.md](../kitchen.md) 2.6 |
| kitchen #12 | Dough block + green dye -> slime block (heated mixer) | S~ P | KT-18 | [kitchen.md](../kitchen.md) 3 |
| kitchen #13 | Plating delay `[cooking] recycleShowTicks` | C P | RB-14 | [remainder_buffer.md](../remainder_buffer.md) 3.1 |
| kitchen #14 | Oven overflow buffer client sync | C P | RB-12 | [remainder_buffer.md](../remainder_buffer.md) 3.4 |
| kitchen #15 | Corner cabinet storage (CfB floor corner) | C P | KT-09 to KT-12 | [kitchen.md](../kitchen.md) 2.5 |
| kitchen #17 | Stepstool (worn, one block higher) | S C P | SQ-STL-01, STL-01 to STL-16 | [stepstool.md](../stepstool.md) |
| kitchen #18 | Lava bottles in jars (Moonlight soft fluid override) | C P | jar drawn in a client | [mixed_jars.md](../mixed_jars.md) (planned block, line 72) |
| kitchen #20 | Plates and coasters on the Blockhead counter | S~ C P | SQ-KT rerun, KT-15 to KT-17 | [kitchen.md](../kitchen.md) 2.3 |
| kitchen #21 | Coaster tag: models or cuts (304 tagged, 208 models) | S C P | SQ-KT-21, KT-19, KT-20 | [kitchen.md](../kitchen.md) 2.3 |
| kitchen #22 | CfB in Creative Delight (blockheadsAdditions); wood spice and tool racks | S C P | SQ-KT-22, KT-22 to KT-24 | [kitchen.md](../kitchen.md) 2.7, 9 |
| textures/art #1 | Fridge texture (was kitchen #19; box drawn by code since #22) | C P | KT-06, CQ-TX-03 | [kitchen.md](../kitchen.md) 2.4 |

## Writeup corrections made

- [remainder_buffer.md](../remainder_buffer.md) 4 (Risks), "Known bug ... oven": "Not looked into yet" -> the likely
  cause (update tag without `Remainders`), pointing here. (2026-10-21)
- [remainder_buffer.md](../remainder_buffer.md) 0: keg row "(`build/libs/`, not installed)" -> the `-03` jar is in
  `pack/manifest.json`. (2026-10-21)
- [remainder_buffer.md](../remainder_buffer.md): a one-line marker before the second, stale copy of the file (from
  line 272, the 2026-10-18 feasibility draft that says "Code: not started"). It marks the copy as superseded by
  sections 0-8 above it. The copy is left in place for the user to delete. (2026-10-21; the copy is still there on
  2026-10-31, from line 340)
