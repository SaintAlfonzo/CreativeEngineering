# Flower pots and planters (`flower_pots_matrix.md`)

TODO `blocks/framed-and-decor #6` (flower potting audit), with `#5` (botanist workbench) and `potions/enchanting #7`
(Floral Enchantment large pot with a tall flower). Goal from the scratchpad (2026-10-19, 2026-10-21): know which plants
can be potted, a tooltip label on every flower pot, every pot distinct, pots hold every flower.

## 0. At a glance

| Part | Status | Open |
|---|---|---|
| Audit: every holder's accept rule, read from bytecode (section 2) | DONE 2026-10-27 (static); live run 2026-10-27 evening | SQ-POT-05 analysis, CQ-POT-01 |
| `/dumppots` (`kubejs/server_scripts/_lib/dump_flower_pots.js`, in `/dumpall`) | BUILT 2026-10-27; SQ-POT-01 PASS 2026-10-27 evening (after two fixes: 121 potted, 101 pot-like, 543 plants) | none |
| Probe `/pottest` (`tools/flower_pot_test/pot_probe.js`, scratch-only) | BUILT 2026-10-27; SQ-POT-02 to -04 ran 2026-10-27 evening (427 probe mismatches, 6 probe exceptions: `minecraft:hanging_roots` in 6 holders; the F1 `ClassCastException` did not show) | read the mismatches (SQ-POT-05) |
| Matrix generator `tools/flower_pot_audit.py` -> [flower_pots_matrix.md](flower_pots_matrix.md) | BUILT 2026-10-27; regenerated from both dumps 2026-10-27 evening | stale since the 3.2 tweaks (2026-10-31, 1.1.06 changed planter box, Vinery box / pot and silt pot lists): rerun `/dumppots`, `/pottest all`, the audit (deep scan 2026-11-02) |
| Botanist workbench as a flower pot (section 3.1, TODO `#5`) | BUILT 2026-10-29, installed 2026-10-29 (patcherMod 1.1.04), shipped 1.1.05; r1031 (2026-10-31): mixins and own block entity PASSED (SQ-POT-06b), a placed bench holds no plant; the flower in the house benches not read, SQ-POT-06 open (client check pending) | POT-08, SQ-POT-06, CQ-POT-02 |
| Flower pot tweaks (section 3.2: framed pot before framing, Rich Hanging Pot, Large Flower Box, wall tooltips, WTHIT "Contains", box plant lists and models, silt pot and Vinery pot as planters, Vinery box centred on floors) | BUILT 2026-10-31 (patcherMod 1.1.04, backportedVinery `-07`), shipped 1.1.06 (Flower Pots bullets, with the Flower Test world type: SQ-POT-07 PASS); SQ-POT-08 PASSED r1101 (tags, recipe); in game not seen (deep scan 2026-11-02) | POT-10 to POT-16, SQ-POT-08; Floral pot rebuild planned (3.2.8) |
| Bugs found (section 3) | open, none fixed. r1102b (2026-11-02): SQ-POT-03 did not reproduce F1 (no `ClassCastException` on the planter box; `minecraft:hanging_roots` still throws `Cannot set property DirectionProperty` in 6 holders); SQ-POT-04 new finding: the Beautify (Rich) Hanging Pot loses its plant when broken (item loss) | decisions D1 to D6; F1 recheck by hand (POT-04); hanging pot plant loss not yet a finding row |
| Tooltip labels, "every pot distinct", "pots hold every flower" | not started | decisions D1, D2, D6 |

- **Regenerate:** `/dumppots` (and `/pottest all` on a scratch server), then `python3 tools/flower_pot_audit.py`.
- **Knobs:** none of the pack's own yet. Live mod configs that matter: Quark `[building] "More Potted Plants" = true`;
  Supplementaries `[building] flower_box = true`, `[building.planter] enabled = true, broken_by_sapling = true`,
  `[tweaks.hanging_flower_pots] enabled = true`, `[functional.flax] wild_flax.enabled = false`; Farmer's Delight
  `richSoilBoostChance = 0.2`.

## 1. Overview

Twelve kinds of block hold plants. Three rule families:

1. **The vanilla full-pots map.** Forge's `FlowerPotBlock.addPlant` map on the empty vanilla pot. The vanilla pot,
   FramedBlocks' framed pot and Supplementaries' hanging pot all read it, so they take exactly the same plants. A mod
   that registers a `potted_*` block but never calls `addPlant` adds nothing a player can make.
2. **Tag or class lists** (flower box, planter box, Vinery box and pot, Twigs silt pot): each mod's own list.
3. **Hardcoded vanilla lists** (Beautify hanging pot 25 items, Floral Enchantment large pot 17 flowers).

The planters are soil, not pots: a plant is placed on top of them as on dirt.

Static prediction (2026-10-27, 369 plant items; full table in the matrix):

| Holder | Accepts (predicted) | Botania (170) | Tall flowers (6) |
|---|---|---|---|
| Flower pot / framed pot / Supp. hanging pot | 115 | 0 | 4 (Quark) |
| Supp. flower box | 292 | 100 | 6 |
| AF planter box | 227 | 84 | 0 |
| Beautify hanging pot | 25 | 0 | 4 |
| Floral large pot | 17 | 0 | 4 |
| Vinery flower pot | 22 (tall flowers only) | 16 | 6 |
| Vinery flower box, Twigs silt pot | 123 (small flowers) | 68 | 0 |

## 2. Reference: every holder

| Holder | Ids | Interaction | Accept rule (class read) | Slots, tall plants | Break |
|---|---|---|---|---|---|
| Flower pot | `minecraft:flower_pot` + every `*:potted_*` | either hand, empty pot | block of the held `BlockItem` is a key of `getFullPotsView()` | 1; Quark pots lilac, peony, rose bush, sunflower, tall grass, large fern in one block | pot + plant |
| Framed pot | `framedblocks:framed_flower_pot` | either hand; camo first (plants fall through) | same map (`FramedFlowerPotBlock.use`) | 1; hangs (DOWN face) when Supplementaries is loaded | frame, camo, plant |
| Hanging pot (Supp.) | `supplementaries:hanging_flower_pot` | made by clicking the underside of a ceiling with an EMPTY vanilla pot; either hand | same map; filled pot + empty hand returns the plant | 1 | pot + plant |
| Flower box (Supp.) | `supplementaries:flower_box` | main hand; hit position picks the slot; empty hand takes back | `BlockItem` in `#supplementaries:flower_box_plantable` (273 live) or a `FlowerPotHandler` special model (cactus, azalea, flax seeds and 22 Quark plants: crops, seeds, vines, sea pickle, chorus) | 3; double plants draw both halves at 0.625 scale | box + contents |
| Planter box (AF) | `another_furniture:<9 woods>_planter_box` | either hand; hit picks left or right | `#another_furniture:planter_box_placeables` (262 live) minus `planter_box_banned` (empty) | 2; no tall flower is in the tag | box + contents; no take-back by hand |
| Hanging pot (Beautify) | `beautify:hanging_pot` | main hand; bone meal (not over a sturdy block) grows it, shears harvest | hardcoded 25 vanilla items (`HangingPot.validFlowers`): 13 small flowers, 4 tall flowers, vine, glow lichen, weeping and twisting vines, glow berries, sweet berries, grass, fern | 1 (state `potflower`) | pot + plant (+1 if grown) |
| Botanist workbench (Beautify, pack) | `beautify:botanist_workbench` | either hand; a click that changes nothing passes (blocks still place against it) | same map as the vanilla pot, read on every click | 1; the plant sits in the bench's own pot at 2/3 size | bench + plant |
| Large pot (Floral) | `floralench:large_pot`, 30 `large_*_pot` | either hand; always consumes the click | hardcoded `item ==` chain: 13 small vanilla flowers, sunflower, lilac, rose bush, peony; same small flower twice = `large_double_*` | tall-flower pots are a 2-block-high model in a 1-block block (enchanting #7) | the filled pot item |
| Vinery pot | `vinery:flower_pot` | main hand, not sneaking | `#minecraft:tall_flowers` only (22 live, incl. 16 Botania double flowers, wild rice, flowering rush) | 1; renderer forces `HALF=UPPER` | pot + plant |
| Vinery box | `vinery:flower_box` | front or top face, x < 0.5 picks the slot | `#vinery:small_flower` = `#minecraft:small_flowers` (loaded from `vinery_upstream/`) | 2 | box + contents |
| Silt pot (Twigs) | `twigs:<17>silt_pot` | fill with rooted dirt first (otherwise a 9-slot container) | any `FlowerBlock` (not `TallFlowerBlock`) placed on top; `FlowerBlockMixin.mayPlaceOn` | 1 real block above | pot; flower pops |
| Planter (Supp.) | `supplementaries:planter`, `planter_rich` | none: plant on top | soil: `canSustainPlant = true` for every `IPlantable`, in `#minecraft:bamboo_plantable_on`, not `#minecraft:dirt`; rich one random-ticks Farmer's Delight rich soil on the plant above | a growing tree turns it into rooted dirt | planter |

Not plant holders: Handcrafted's 16 `*_pot` (decorative, no `use`), Botania apothecaries, Create depot. `supplementaries:rooty_*` blockstates are Dynamic Trees assets (not installed).

Vanilla map contents (static count): 33 vanilla + Quark 35 (`MorePottedPlantsModule` plus Chorus Vegetation, Glimmering
Weald, Ancient Wood, Blossom Trees) + Fruits Delight 13 saplings + Bushier Flowers 13 + Upgrade Aquatic 5 + Buzzier
Bees 3 + Neapolitan 3 + Rustic (CD) 5 + Respite wild tea bush (patcherMod `RespitePottedTeaBush`) + Supplementaries
flax + Twigs azalea flowers + IE hemp + Architects' twisted sapling + Nether's Delight mimicarnation + Miner's
gossypium. `/dumppots` replaces this count with the live map.

## 3. Findings

| # | Finding | Kind | Where |
|---|---|---|---|
| F1 | ~~Right-click on an AF planter box with `fungidelight:cleaned_cap`, `cooked_cleaned_cap`, `truffle` or `truffle_slice` throws `ClassCastException` on the server (`PlanterBoxBlockEntity.placeFlower` casts to `BlockItem`; the four are plain items in `#forge:mushrooms`, which the planter tag includes)~~ Struck 2026-11-02: not reproduced in r1102b SQ-POT-03 | ~~bug, server exception~~ not reproduced | SQ-POT-03 (r1102b: no `ClassCastException`) |
| F2 | `vinery:potted_apple_tree_sapling` and `potted_cherry_tree_sapling` are registered but nothing calls `addPlant`: the saplings do nothing on a pot | bug, in-tree (backportedVinery) | SQ-POT-02 |
| F3 | No Botania flower (mystical, shiny, double, floating, special, chibi, mushrooms: 170 items) goes in any one-block pot; only the flower box, planter box, Vinery box and pot, and silt pot take some | gap | D1 |
| F4 | `supplementaries:planter_rich_soul` is never registered: Supplementaries checks the mod id `nethers_delight`, the installed Nether's Delight is `nethersdelight` | gap (mod compat) | D5 |
| F5 | Wild crops (Farmer's, Veggies, Vintage Delight, Delightful salmonberries; 20 items) are flowers but pot only in the boxes; Rustic's 5 wild plants and Respite's wild tea bush do pot | gap | D1 |
| F6 | `buzzier_bees:buttercup` is in no flower tag (pink and white clover are): the boxes, the Vinery box and bee-breeding tags miss it; it does pot | gap (tag) | D3 |
| F7 | `supplementaries:wild_flax` is a small flower with no pot entry (worldgen off in config, item still exists) | gap | D1 |
| F8 | Floral large pot with a tall flower draws 2 blocks high in a 1-block space (`potions/enchanting #7`) | known | D4 |
| F9 | Vinery pot renderer forces `HALF=UPPER` on whatever tall flower it holds; every live `#minecraft:tall_flowers` entry should be a double plant (wild rice, flowering rush, Botania double flowers): a client check | risk | CQ-POT-01 |
| F10 | Supplementaries' hanging pot can only be made from an empty vanilla pot: a framed pot hangs by itself, no other pot hangs | note | none |
| F11 | Glow lichen, mangrove roots, beachgrass, kelp variants, mushroom colonies, rubberwood sapling, magic peanut (Vintage) have no pot | gap | D1 |

### 3.1 Botanist workbench as a flower pot (pack)

Beautify's `BotanistWorkbench` is a plain `HorizontalDirectionalBlock`. `mixin/BotanistWorkbenchPotMixin` (`@Pseudo`)
adds `EntityBlock`, `use` and `onRemove`; the block entity `creativeengineering:botanist_pot`
(`compat/BotanistPotBlockEntity`) stores the plant's block id (NBT `Plant`).

- **Plant list:** `compat/BotanistPot.use` follows vanilla `FlowerPotBlock.use` against the vanilla pot's
  `getFullPotsView()` map, read on every click, so any plant a mod adds with `addPlant` works here too. Empty hand (or
  any item that is not pottable) on a full bench takes the plant back; breaking the bench drops it.
- **Look:** `compat/BotanistPotClient` wraps every workbench state model at bake time. For the stored plant it takes
  the quads of the map's `potted_*` block model, drops those whose sprite is used by the empty `minecraft:flower_pot`
  model (pot and dirt), scales them to 2/3 about the plant base (pot centre, y4) and moves that point onto the bench's
  pot (x11.5 y15 z10.5 in the north model), then turns it with the bench's facing. Models come from the current bake
  and the quad cache dies with the wrapper, so resource-pack potted models show after F3+T. Tinted plants (fern,
  grass) keep their colour: plant tint indices are moved up by 1000 and handed to the potted block's colour handler.
- **Structures:** `mixin/StructureTemplateBotanistPotMixin` (on Forge's 6-argument `processBlockInfos`, like the loot
  swap) gives each template workbench without NBT a random flower: map keys in the `minecraft` namespace, in
  `#minecraft:flowers`, with a pot entry, not the wither rose. Benches in chunks generated before this change stay
  empty.
- **Not covered:** the `flower_pot_audit.py` matrix and `/pottest` have no workbench column (it takes exactly the
  vanilla pot's column).

### 3.2 Flower pot tweaks (scratchpad 2026-10-31, built 2026-10-31)

1. **Framed pot before framing** (`mixin/FramedFlowerPotUnframedMixin`). With no frame, FramedBlocks treated any
   block item as a frame attempt; a plant is not a valid frame, so the click was used up ("block not valid") and never
   reached the pot. Now a plant the vanilla pot takes goes in first, by the framed pot's own steps.
2. **Box plant models** (`compat/PlantBoxModels`, `PlantBoxModelsClient`, `SuppFlowerBox`). Supplementaries keeps a
   list of special flower box models (`FlowerPotHandler`: crops, seeds, cactus, chorus, nether wart, tall flowers...).
   The pack adds glow berries (Quark's potted cave vines texture on Supplementaries' cross) and glow lichen (Beautify's
   two crossed lichen planes) to it, so the Large Flower Box takes and draws both. Another Furniture's planter box
   renderer draws every plant that has such a model with it (`mixin/AnotherFurniturePlanterRenderMixin`, client);
   the Vinery box and pot do the same in the port (`legacy/client/PlantRenderer`).
3. **Plant lists** (`kubejs/server_scripts/tags/misctags.js`). `#another_furniture:planter_box_placeables` gains beetroot
   seeds, cactus, carrot, chorus flower, cocoa beans, crimson and warped roots, dead bush, grass, fern, lilac, melon
   seeds, peony, potato, pumpkin seeds, rose bush, sunflower, nether sprouts, nether wart, glow berries, glow lichen.
   `#creativeengineering:not_in_flower_boxes` (azalea, flowering azalea) goes in AF's `planter_box_banned` and the
   Supplementaries box refuses it (`mixin/SuppFlowerBoxPlantsMixin`; boxes already holding one keep it).
   `#creativeengineering:flower_box_plants` = the planter box list minus that tag, the Vinery box's list.
4. **Botanist workbench** takes glow lichen (not in the vanilla pot map): `BotanistPot.pottedFor` returns the glow lichen
   block itself, drawn from `creativeengineering:block/flower_box/glow_lichen_pot` (base at the pot soil, y4).
5. **Soil holders.** A Twigs silt pot filled with rooted dirt sustains any plant on top, like a Supplementaries planter
   (`mixin/SiltPotSoilMixin`, `canSustainPlant` true facing up). Silt pot items say "Use Rooted Dirt on it to fill it;
   plants then grow on top" (`tooltips.js`). The Vinery flower pot takes any `IPlantable` block
   item plus `#minecraft:tall_flowers`, never azalea; non-double plants are no longer forced to the upper half.
6. **Vinery flower box** (port `-07`): state `wall` (default true, so placed boxes keep their look), false when placed
   on a floor: shape, model (`vinery:block/flower_box_centered`, built in memory by `UpstreamTranslator`) and plants
   move 4.5 px forward to the centre of the block.
7. **Names, looks, tooltips.** Supplementaries' Flower Box is the Large Flower Box; Beautify's hanging pot is the Rich
   Hanging Pot (`namechanges.js`), its soil repainted in Farmer's Delight rich soil colours on all 51 pot textures
   (`kubejs/assets/beautify/textures/blocks/`, generated from the 1.4.3 textures: the 6x6 soil patch at x0-5 y32-37),
   recipe rope / flower pot / rich soil (`other/flowers.js`; was rope over a flower pot), and WTHIT names it "Rich Pot"
   when it stands on the ground. "Can be placed on walls" on the Large Flower Box, the Vinery box (replacing Vinery's
   "Can be placed") and every `*_planter_box` (`tooltips.js` Flower Pots).
8. **WTHIT "Contains: Poppy"** (`compat/WthitPotContents`, `[wthit] potContents`): vanilla and modded potted blocks,
   framed pot, Supplementaries hanging pot and flower box, AF planter boxes (Every Compat woods too), Beautify hanging
   pot (`potflower` index into `HangingPot.validFlowers`), botanist workbench, Floral large pots (from the block id),
   Vinery pot and box. Same plant twice: "2 Poppy". A silt pot shows "Rooted Dirt: Yes / No"; planters show nothing
   (the plant is its own block).
9. **Floral Enchantment large pot: hidden 2026-10-31** (all 31 `floralench:large_*pot` items in `global.removedItems`,
   recipe removed in `other/flowers.js`; placed ones still work). **Replacement planned, not built.** User decision 2026-10-31: a new pack block with the Floral pot
   texture that takes small flowers inside like the flower box (box logic, Supplementaries models) and grows tall
   flowers on top like a planter. Estimate in TODO `potions/enchanting #7`.

## 4. Decisions (open, owner)

- **D1 "pots hold every flower":** which plants get a vanilla `potted_*` block (map entry + model)? Options: (a) all
  F3/F5/F7/F11 plants in a new patcherMod or CD registration pass (Botania alone is 170 blocks plus models), (b) only
  small flowers and saplings (mystical 16, shiny 16, special flowers, wild crops), (c) leave it to the flower box.
- **D2 tooltip label on every pot:** one line per holder naming what it takes (for example "Holds 1 flower or sapling",
  "Holds 3 plants", "Tall flowers only"), through the tooltip system (`tooltips.md`). Needs the wording per holder.
- **D3 buttercup tag:** add `buzzier_bees:buttercup` to `#minecraft:small_flowers` (via `global.tags`)? It would then
  enter the boxes, the silt pot, bee breeding and anything else that reads the tag.
- **D4 Floral tall-flower pots:** disable the 4 tall-flower variants or the whole large pot (enchanting #7).
- **D5 soul planter:** register it ourselves (patcherMod mixin on Supplementaries' compat check) or drop it.
- **D6 F1 fix:** add the four Fungi Delight items to `#another_furniture:planter_box_banned` (smallest), or take them
  out of `#forge:mushrooms` (affects recipes).
- **Botanist workbench (`#5`):** decided 2026-10-29 and built (section 3.1): the plant model only (Beautify's pot
  and dirt stay), 2/3 size, structure-placed benches start with a random vanilla flower.

## 5. File map

- [../kubejs/server_scripts/_lib/dump_flower_pots.js](../kubejs/server_scripts/_lib/dump_flower_pots.js): `/dumppots`
- [../tools/flower_pot_test/pot_probe.js](../tools/flower_pot_test/pot_probe.js): `/pottest` scratch probe
- [../kubejs/data/creativedevtools/kubejs/flowerworld/flower_world.js](../kubejs/data/creativedevtools/kubejs/flowerworld/flower_world.js) (Creative Devtools since 2026-11-01, dev only): the Flower Test
  world builder (every plant some holder takes x one block per holder type, one table along +z, axis of plants on
  grass, hanging holders one block up under glass). Runs by itself on the first start of a world made with the
  "Flower Test" world type; `/flowerworld` (op) rebuilds it in any world
- patcherMod `compat/PlantBoxModels`, `PlantBoxModelsClient`, `SuppFlowerBox`, `WthitPotContents`; mixins
  `FramedFlowerPotUnframedMixin`, `SuppFlowerBoxPlantsMixin`, `SiltPotSoilMixin`, `AnotherFurniturePlanterRenderMixin`
  (client); models `assets/creativeengineering/models/block/flower_box/` (section 3.2)
- backportedVinery `legacy/block/LegacyFlowerBoxBlock`, `LegacyFlowerPotBlock`, `legacy/client/PlantRenderer`,
  `SupplementariesFlowerModels`, `forge/upstream/UpstreamTranslator` (centred box model)
- [../kubejs/server_scripts/tags/misctags.js](../kubejs/server_scripts/tags/misctags.js) flower box tags;
  [../kubejs/client_scripts/tooltips.js](../kubejs/client_scripts/tooltips.js) Flower Pots;
  [../kubejs/client_scripts/namechanges.js](../kubejs/client_scripts/namechanges.js); Rich Hanging Pot recipe in
  [../kubejs/server_scripts/other/flowers.js](../kubejs/server_scripts/other/flowers.js)
- [../development/creativeDevtools/src/main/resources/data/creativeengineering/worldgen/world_preset/flower_test.json](../development/creativeDevtools/src/main/resources/data/creativeengineering/worldgen/world_preset/flower_test.json) (Creative Devtools):
  the "Flower Test" world type (flat plains, no features or structures, bedrock / stone / dirt / grass, the stack
  the builder checks for), listed in the World Type button through `#minecraft:normal`
- [../tools/flower_pot_audit.py](../tools/flower_pot_audit.py): matrix generator, holder rules as data
- [flower_pots_matrix.md](flower_pots_matrix.md): generated matrix
- Botanist workbench pot: patcherMod `compat/BotanistPot.java`, `BotanistPotBlockEntity.java`, `BotanistPotClient.java`,
  `mixin/BotanistWorkbenchPotMixin.java`, `mixin/StructureTemplateBotanistPotMixin.java`
- Pack code touching pots: patcherMod `compat/RespitePottedTeaBush.java`, CD rustic `logic/RDEvents.java`, backportedMiners
  `logic/MDSetup.java`, backportedVinery `core/registry/ObjectRegistry.java` (F2), `legacy/block/LegacyFlowerPotBlock.java`

## 6. Offline checks

- `python3 tools/flower_pot_audit.py`: prints plant count, per-holder totals, probe mismatches and exceptions.

## 7. In-game test suite

- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): FAIL SQ-POT-03: `minecraft:hanging_roots` on 6 holders throws `Cannot set property DirectionProperty` (needs a stack trace in game; old F1 not reproduced); FAIL SQ-POT-04: `beautify:hanging_pot` loses its plant when broken (item loss).

Scratch server entries: SQ-POT-01 to SQ-POT-05 in [testing/scratch_queue.md](testing/scratch_queue.md). In game:

- [ ] **POT-09 Flower Test world type (dev instance with Creative Devtools):** Create New World, More World Options, World Type cycles to "Flower Test";
  on entering, chat says "Flower Test: N plants x 12 holders built" within ~30 s; you stand at -5 -5 in front of one
  unbroken table (header signs at z = -2, rows every 2 blocks along +z, no second section); the Supplementaries and
  Beautify hanging pots hang one block above the grass under glass; no Handcrafted pot column; no melon, pumpkin or
  moss rows, no Floral large pot column; the silt pot column is filled with rooted dirt and has plants on top; no
  animals in the table; leave and rejoin → no rebuild message; a normal Superflat world gets no table.
- [ ] **POT-10 framed pot before framing:** place a framed flower pot, click it with a poppy → the poppy goes in (no
  "block not valid" message); then frame it with a block → frame applied, poppy stays; empty hand takes the poppy out.
- [ ] **POT-11 planter box plants:** an oak planter box takes beetroot seeds, cactus, carrot, chorus flower, cocoa
  beans, crimson roots, dead bush, grass, lilac, nether wart, glow berries, glow lichen; each looks like the same plant
  in the Large Flower Box (no tiny seedling, no missing-model cube; lilac whole, not two halves); grass is green.
  Azalea and flowering azalea are refused by the planter box, the Large Flower Box and the Vinery box.
- [ ] **POT-12 Large Flower Box and botanist workbench:** the box is named Large Flower Box and takes glow berries and
  glow lichen (drawn as small cross plants); a botanist workbench takes glow lichen, drawn in its small pot, and gives it
  back on an empty-hand click.
- [ ] **POT-13 Rich Hanging Pot:** named Rich Hanging Pot; recipe rope, flower pot, rich soil in a column (the old
  rope-over-pot recipe is gone); soil is dark rich soil in every flower variant; hanging under a block WTHIT says
  Rich Hanging Pot, standing on the ground it says Rich Pot.
- [ ] **POT-14 tooltips and WTHIT:** "Can be placed on walls" on the Large Flower Box, the Vinery flower box (Vinery's
  own "Can be placed" gone) and an oak planter box; WTHIT shows "Contains: Poppy" on a vanilla pot with a poppy, a
  framed pot, a Supplementaries hanging pot, the Large Flower Box ("Poppy, Dandelion"), a planter box, a Rich Hanging
  Pot, a botanist workbench, a Floral large double poppy pot ("2 Poppy"), a Vinery pot and box; nothing on an empty pot.
  A silt pot item says "Use Rooted Dirt on it to fill it; plants then grow on top"; WTHIT on a silt pot says
  "Rooted Dirt: No", after a rooted dirt click "Rooted Dirt: Yes". Floral large pots are not in JEI or creative
  search and have no recipe; one placed before still takes and shows its flowers.
- [ ] **POT-17 sunflower:** an oak planter box and a Vinery flower box take a sunflower, drawn whole.
- [ ] **POT-15 Vinery box and pot:** a Vinery flower box placed on the floor sits in the middle of the block (model,
  outline, plants), against a wall at the back as before; an old box keeps its place; it takes everything the planter
  box takes. The Vinery flower pot takes an oak sapling, wheat seeds, cactus, a fern, a rose bush (both halves).
- [ ] **POT-16 silt pot as soil:** fill a silt pot with rooted dirt; wheat seeds, an oak sapling, sugar cane, cactus and
  a poppy each plant on top and stay after a block update; an unfilled silt pot takes none.
- [ ] **POT-01 vanilla pot map:** with the matrix open, put 10 plants marked Y in a flower pot and 5 marked blank → Y ones
  plant, blank ones do nothing.
- [ ] **POT-02 framed and hanging pots:** the same 5 Y plants in a framed pot and a Supplementaries hanging pot (empty
  pot clicked under a ceiling); break each → pot and plant drop.
- [ ] **POT-03 flower box:** a Botania mystical flower, a sapling and a sunflower in the 3 slots; empty hand takes one
  back; the sunflower shows both halves.
- [ ] **POT-04 planter box:** 2 flowers; a truffle on it → nothing happens and latest.log has no `ClassCastException`
  (fails until D6).
- [ ] **POT-05 Beautify and Floral:** a rose bush in each; a modded flower in each is refused.
- [ ] **POT-06 Vinery pot and box:** a Botania double flower and wild rice in the pot (client draws both, no crash);
  2 small flowers in the box.
- [ ] **POT-08 botanist workbench:** a poppy, an oak sapling, a fern and a Quark-potted lilac each go in (one at a
  time, empty hand takes each back); the plant sits in the bench's small pot at 2/3 size, turns with all four
  facings, the fern is green; a Botania mystical flower is refused and still places as a block against the bench;
  break a filled bench → bench and plant drop; F3+T keeps the plant; a botanist house in new chunks has a flower in
  its bench.
- [ ] **POT-07 silt pot and planters:** rooted dirt into a silt pot, a Botania mystical flower on top; wheat, cactus,
  sugar cane and nether wart on a planter survive.

## 8. History

- 2026-10-29: botanist workbench made a flower pot (section 3.1), patcherMod 1.1.04 built and installed. POT-08,
  SQ-POT-06, CQ-POT-02 added.

- 2026-10-27: audit. Holder rules read from bytecode (Supplementaries, FramedBlocks, Beautify, Another Furniture, Floral
  Enchantment, Handcrafted, Twigs, Botania, Quark, backportedVinery source). `/dumppots`, `/pottest` and
  `tools/flower_pot_audit.py` written; static matrix generated; findings F1 to F11; scratch entries SQ-POT-01 to -05
  queued.
- 2026-10-27 (later): first scratch run (session creativeengineering2-50, pe3) failed in setup: `/dumppots` loaded
  `java.lang.Class`, which KubeJS's class filter blocks, so `/pottest` had no plant list (12x PP-00). Fixed (interfaces
  through `Java.loadClass` + `instanceof`); SQ-POT-01 to -05 back in the queue.
- 2026-10-27 (evening scratch run, queue Run log): SQ-POT-01 PASS after two `/dumppots` fixes; SQ-POT-02 to -04 ran
  and regenerated the matrix (427 probe mismatches, 6 exceptions); SQ-POT-05 analysis and CQ-POT-01 open.
- 2026-10-29: `saves/flower test` built with `/flowerworld` (284 plants x 17 holders, 6 sections of 48 rows along +x)
  for hand-picking which plants other pots should take. Live fill counts equal pot_probe.json; Supplementaries'
  hanging pot takes the vanilla map (115) once its block entity holds an empty pot (the probe's 0 was its setup)
  and the botanist workbench takes the same 115.
- 2026-10-31: scratchpad notes translated into TODO rows (`blocks/framed-and-decor #11`). No code changed.
- 2026-10-31: Flower Test world type (patcherMod preset, builds on first start), builder moved from
  `tools/flower_pot_test/` into `kubejs/server_scripts/_lib/` so it ships. Table is one line along +z (was 6 sections
  of 48 rows along +x); hanging holders and their glass one block up; rows no longer need the dev-only
  `pot_probe.json` (bush-type block items, cactus, bamboo, hanging roots added as candidates, a candidate no
  non-planter holder takes is cleared); generated mobs killed at the end. patcherMod 1.1.04 built, not installed
  (game was running). POT-09, SQ-POT-07 added.
- 2026-10-31 (later): flower pot tweaks from the scratchpad (section 3.2) built: patcherMod 1.1.04 and backportedVinery
  `-07`, neither installed (game running). Flower Test world: Handcrafted pots out, melon / pumpkin / moss blocks out
  (13 holders). Candle-like pot placement skipped (user). POT-10 to POT-16, SQ-POT-08 added.
- 2026-10-31 (later): Flower Test silt pot filled by a rooted dirt click before planting; Floral large pots hidden and
  out of the Flower Test world (12 holders); sunflower in the planter box list; silt pot tooltip and WTHIT
  "Rooted Dirt" line. patcherMod 1.1.04 rebuilt, not installed. POT-17 added.
- 2026-11-01: the Flower Test world builder, its preset and lang key moved to Creative Devtools (dev only,
  creative_devtools.md 2); players no longer get the world type.
