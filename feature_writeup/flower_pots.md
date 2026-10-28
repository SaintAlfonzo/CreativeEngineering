# Flower pots and planters (`flower_pots_matrix.md`)

TODO `blocks/framed-and-decor #6` (flower potting audit), with `#5` (botanist workbench) and `potions/enchanting #7`
(Floral Enchantment large pot with a tall flower). Goal from the scratchpad (2026-10-19, 2026-10-21): know which plants
can be potted, a tooltip label on every flower pot, every pot distinct, pots hold every flower.

## 0. At a glance

| Part | Status | Open |
|---|---|---|
| Audit: every holder's accept rule, read from bytecode (section 2) | DONE 2026-10-27 (static) | live confirmation SQ-POT-01 to -05 |
| `/dumppots` (`kubejs/server_scripts/_lib/dump_flower_pots.js`, in `/dumpall`) | BUILT 2026-10-27, not run | SQ-POT-01 |
| Probe `/pottest` (`tools/flower_pot_test/pot_probe.js`, scratch-only) | BUILT 2026-10-27, not run | SQ-POT-02 to -04 |
| Matrix generator `tools/flower_pot_audit.py` -> [flower_pots_matrix.md](flower_pots_matrix.md) | BUILT 2026-10-27; matrix is a static prediction until the two dumps exist | rerun after SQ-POT-02 |
| Bugs found (section 3) | open, none fixed | decisions D1 to D6 |
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
| Large pot (Floral) | `floralench:large_pot`, 30 `large_*_pot` | either hand; always consumes the click | hardcoded `item ==` chain: 13 small vanilla flowers, sunflower, lilac, rose bush, peony; same small flower twice = `large_double_*` | tall-flower pots are a 2-block-high model in a 1-block block (enchanting #7) | the filled pot item |
| Vinery pot | `vinery:flower_pot` | main hand, not sneaking | `#minecraft:tall_flowers` only (22 live, incl. 16 Botania double flowers, wild rice, flowering rush) | 1; renderer forces `HALF=UPPER` | pot + plant |
| Vinery box | `vinery:flower_box` | front or top face, x < 0.5 picks the slot | `#vinery:small_flower` = `#minecraft:small_flowers` (loaded from `vinery_upstream/`) | 2 | box + contents |
| Silt pot (Twigs) | `twigs:<17>silt_pot` | fill with rooted dirt first (otherwise a 9-slot container) | any `FlowerBlock` (not `TallFlowerBlock`) placed on top; `FlowerBlockMixin.mayPlaceOn` | 1 real block above | pot; flower pops |
| Planter (Supp.) | `supplementaries:planter`, `planter_rich` | none: plant on top | soil: `canSustainPlant = true` for every `IPlantable`, in `#minecraft:bamboo_plantable_on`, not `#minecraft:dirt`; rich one random-ticks Farmer's Delight rich soil on the plant above | a growing tree turns it into rooted dirt | planter |

Not plant holders: Handcrafted's 16 `*_pot` (decorative, no `use`), Beautify `botanist_workbench` (decorative),
Botania apothecaries, Create depot. `supplementaries:rooty_*` blockstates are Dynamic Trees assets (not installed).

Vanilla map contents (static count): 33 vanilla + Quark 35 (`MorePottedPlantsModule` plus Chorus Vegetation, Glimmering
Weald, Ancient Wood, Blossom Trees) + Fruits Delight 13 saplings + Bushier Flowers 13 + Upgrade Aquatic 5 + Buzzier
Bees 3 + Neapolitan 3 + Rustic (CD) 5 + Respite wild tea bush (patcherMod `RespitePottedTeaBush`) + Supplementaries
flax + Twigs azalea flowers + IE hemp + Architects' twisted sapling + Nether's Delight mimicarnation + Miner's
gossypium. `/dumppots` replaces this count with the live map.

## 3. Findings

| # | Finding | Kind | Where |
|---|---|---|---|
| F1 | Right-click on an AF planter box with `fungidelight:cleaned_cap`, `cooked_cleaned_cap`, `truffle` or `truffle_slice` throws `ClassCastException` on the server (`PlanterBoxBlockEntity.placeFlower` casts to `BlockItem`; the four are plain items in `#forge:mushrooms`, which the planter tag includes) | bug, server exception | SQ-POT-03 confirms |
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
- **Botanist workbench (`#5`):** it has no block entity; making it a pot needs a new block entity and renderer.

## 5. File map

- [../kubejs/server_scripts/_lib/dump_flower_pots.js](../kubejs/server_scripts/_lib/dump_flower_pots.js): `/dumppots`
- [../tools/flower_pot_test/pot_probe.js](../tools/flower_pot_test/pot_probe.js): `/pottest` scratch probe
- [../tools/flower_pot_audit.py](../tools/flower_pot_audit.py): matrix generator, holder rules as data
- [flower_pots_matrix.md](flower_pots_matrix.md): generated matrix
- Pack code touching pots: patcherMod `compat/RespitePottedTeaBush.java`, CD rustic `logic/RDEvents.java`, backportedMiners
  `logic/MDSetup.java`, backportedVinery `core/registry/ObjectRegistry.java` (F2), `legacy/block/LegacyFlowerPotBlock.java`

## 6. Offline checks

- `python3 tools/flower_pot_audit.py`: prints plant count, per-holder totals, probe mismatches and exceptions.

## 7. In-game test suite

Scratch server entries: SQ-POT-01 to SQ-POT-05 in [testing/scratch_queue.md](testing/scratch_queue.md). In game:

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
- [ ] **POT-07 silt pot and planters:** rooted dirt into a silt pot, a Botania mystical flower on top; wheat, cactus,
  sugar cane and nether wart on a planter survive.

## 8. History

- 2026-10-27: audit. Holder rules read from bytecode (Supplementaries, FramedBlocks, Beautify, Another Furniture, Floral
  Enchantment, Handcrafted, Twigs, Botania, Quark, backportedVinery source). `/dumppots`, `/pottest` and
  `tools/flower_pot_audit.py` written; static matrix generated; findings F1 to F11; scratch entries SQ-POT-01 to -05
  queued.
- 2026-10-27 (later): first scratch run (session creativeengineering2-50, pe3) failed in setup: `/dumppots` loaded
  `java.lang.Class`, which KubeJS's class filter blocks, so `/pottest` had no plant list (12x PP-00). Fixed (interfaces
  through `Java.loadClass` + `instanceof`); SQ-POT-01 to -05 back in the queue.
