# Farming for Blockheads: overlap and integration report

Investigation written 2026-11-23, nothing built. Jars read: Farming for Blockheads `26.3.0.2` (NeoForge, MC 26.3,
`DecompiledMods/farmingforblockheads-neoforge-26.3-26.3.0.2.jar`) and `11.3.5` (Fabric, MC 1.19.2,
`DecompiledMods/1.19.2-farmingforblockheads-fabric-1.19.2-11.3.5.jar`, read for what the 1.19.2 line has). License:
All Rights Reserved (BlayTheNinth, both jars; permissions page `https://mods.twelveiterations.com/permissions`). A port
is needed: the Shipping Bin exists only in the 26.3 line, and the 1.19.2 line has no bin. Initial goal (owner,
2026-11-23, scratchpad "only for shipping bin, disable the rest"): port the Shipping Bin only. The rest of the mod is
audited in sections 2 and 3, and none of it is planned. Short name in this file: FfB. Test prefix: `SHB-`
(`FFB` is taken by the oven queue rows).

Scope rule: integration adds no recipe or mechanic that is not in the mod or defined by a pack rule unless the owner
asks by name; upstream bugs are fixed.

## 0. At a glance

<!-- generated:status_index begin open_work sha1=c1ed37eba29b -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 1 open TODO row (Planned 1), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `mod-integration #7` | Planned | S C P T | Farming for Blockheads: port the Shipping Bin only (ARR, 26.3 donor art, own code) |
<!-- generated:status_index end open_work -->

| Part | Where | Status |
|---|---|---|
| Inventory (26.3): 12 blocks, 6 items, 5 entities, 3 menus; recipes 12 crafting, 551 market, 12 shipping bin | section 1, 3 | read |
| Shipping Bin mechanics (sale rule, payout, carriers) | section 3.1 | read from the decompile |
| Overlap | section 2 | no other shipping bin in the pack; the rest of FfB overlaps Hearth and Harvest, Quark, Incubation, Slice and Dice |
| Upstream bugs | B1, B2 (section 3.4) | none blocking for the bin |
| Fold-in (bin only) | section 4 | 11 steps drafted |
| Decisions | D1 to D7 (section 5) | open |
| Tests | SHB-01 to SHB-12 (section 8) | drafted |

- Jar: the 26.3 jar is the only source of the bin. It is a donor for art (Miner's Delight route, [miners_delight.md](miners_delight.md)
  D2, D12), never a mod in `mods/`. The 1.19.2 Fabric jar cannot load on Forge. A Forge 1.19.2 11.3.x jar exists
  upstream but has no bin either.
- License: ARR. Our code is written from behavior, not translated from the decompile. No upstream file is copied into
  our jar (port skill, section 2).
- Dependencies upstream: Balm 26.3 and its rule engine ("Shogi", recipe `valueEffect` and `predicate`). The pack has
  Balm `4.6.0` for 1.19.2 (Cooking for Blockheads). The port does not need Balm (section 3.1).
- Pack references: Lightman's Currency 2.2.1.3g has a compat mixin for the FfB market (inactive, FfB not installed).
  Nothing else in `kubejs/`, `development/*/src` or `item_dict.js` names `farmingforblockheads`.
- Already live: nothing.

## 1. Overview

FfB adds a seed market run by a merchant, fertilizers and fertilized farmland, a chicken nest, a feeding trough, and
in the newer line a sprinkler (six fluids), a fishing barrel, a rabbit trap, a scythe, a watering can and the
Shipping Bin. The bin is a sell box: crops go in, emeralds come out once a full load is in, and a balloon (open sky)
or a courier with a crate (under a roof) carries the load away as an effect.

| Fact | 26.3.0.2 | 11.3.5 (1.19.2) |
|---|---|---|
| Mod id | `farmingforblockheads` | same |
| Blocks | 12: market, chicken nest, feeding trough, rabbit trap, sprinkler, shipping bin, fishing barrel, 5 fertilized farmland | 8: market, chicken nest, feeding trough, 5 fertilized farmland |
| Items | 6: red, green, yellow fertilizer, iron scythe ("Scythe"), watering can, fox note ("Mysterious Note") | 3 fertilizers |
| Entities | merchant, shipping balloon, shipping crate, falling shipping crate, courier | merchant |
| Effects / potions | none | none |
| Recipes | 12 `crafting_shaped`, 551 `farmingforblockheads:market` (27 mod folders), 12 `farmingforblockheads:shipping_bin` | 6 `crafting_shaped`; market entries in 11 compat files |
| Loot | 15 tables (block drops, `gameplay/shipping_bin`, `entities/falling_shipping_crate` gated on Pantry for Blockheads); GLM `rich_farmland` (bonus crop) | 8 block tables |
| Tags | 19 own, 4 `minecraft:`, 1 `c:` (`mineable/axe` lists the bin) | 2 own, 3 `minecraft:` |
| Advancements | 13 (12 recipe unlocks, `lost_in_transit`); the pack drops all advancements | none |
| Lang | en_us; bin keys: block, container, two entities, one advancement | en_us |
| Sounds | `falling` (falling crate); the rest are vanilla events | none |
| Config | merchant names, fertilizer bonus chance, market options; bin capacity is a Shogi rule (128) | merchant names, fertilizer chances, market |

## 2. Overlap

### 2.1 Shipping Bin (the port)

| Item / system | Overlaps with | Problem | Proposal |
|---|---|---|---|
| `farmingforblockheads:shipping_bin` | nothing: no block that sells items for currency (searched `item_dict.js`, exports, Hearth and Harvest, Storage Delight, Lightman's Currency) | none | port |
| Crop selling | vanilla farmer villager buys wheat, potato, carrot, beetroot, pumpkin, melon; Wares delivery agreements; Lightman's Currency trader blocks (player-run) | bin pays without a trade limit or restock | keep upstream rates, which pay less than the farmer (table below); D3 |
| Payout currency | emeralds (vanilla trades, wandering trader); Lightman's Currency coins | which currency the pack wants | D2 |
| Shipping balloon / courier | none (MCA villagers do not deliver) | none | port as effects (D5) |

Emeralds per item at upstream values against the 1.19.2 vanilla farmer (base buy trades, before the pack's
Dynamic Villager Trades changes):

| Crop | Bin value | Bin: items per emerald | Farmer: items per emerald |
|---|---|---|---|
| Wheat | 4 | 32 | 20 |
| Carrot | 5 | 25.6 | 22 |
| Potato | 5 | 25.6 | 26 |
| Beetroot | 6 | 21.3 | 15 |
| Pumpkin | 8 | 16 | 6 |
| Melon (block) | 8 | 16 | 4 |
| Melon slice, sugar cane | 2 | 64 | no trade |
| Glow berries, nether wart | 6 | 21.3 | no trade |
| Kelp, sweet berries | 1 | 128 | no trade |

### 2.2 Rest of the mod (audit only, nothing planned)

| FfB content | Overlaps with | Note |
|---|---|---|
| Market + merchant (551 seed, sapling, flower, soil, spawn-egg listings for emerald) | Wandering trader catalog ([wandering_trader.md](wandering_trader.md)), seeds from wild crops ([crops.md](crops.md)) | would undercut the wild-crop and trader routes; spawn eggs for sale break the mob rules |
| Red / green / yellow fertilizer, 5 fertilized farmlands, `rich_farmland` GLM | Farmer's Delight rich soil, Botania fertilizer, IE fertilizer, Slice and Dice fertilizer fluid, Hearth and Harvest fertilizer bag, Create tree fertilizer | sixth fertilizer system; GLM doubles crops outside the yield rules ([crop_yields.md](crop_yields.md)) |
| Chicken nest | Incubation hay and twig nests, Creative Delight duck, emu, goose nests, Hearth and Harvest nest | duplicate egg collector |
| Feeding trough | Quark feeding trough, Hearth and Harvest trough | third trough |
| Sprinkler (water, lava, honey, slime, snow, sulfur) | Hearth and Harvest sprinkler, Slice and Dice sprinkler, Create Things and Misc sprinkler | fourth sprinkler |
| Watering can | Hearth and Harvest watering cans (6 tiers) | duplicate |
| Iron scythe | MCA scythe (a weapon), hoe area harvest ([hoe-harvest-area](agent_notes/hoe-harvest-area.md)) | duplicate area harvest |
| Fishing barrel (auto-fishing with a rod) | Oceanic Delight fishing net | different mechanic, still auto-fishing |
| Rabbit trap | none found | new, small |
| Fox note | none | flavor item from the fox event |

The scratchpad ruling ("disable the rest") holds for the audit as well: every other block has a pack equivalent
except the rabbit trap and the fishing barrel. Those two are left out unless the owner asks for them by name.

## 3. New content

### 3.1 Shipping Bin mechanics (26.3.0.2, from the decompile)

- **Block:** horizontal `facing` (placed facing the player), wood sound, strength 2.0, axe tool (`mineable/axe`),
  drops itself (survives explosion). Hitbox is a crate frame (open top, 2 px posts). Right-click with any item or
  none opens the menu.
- **Container:** 12 slots: 6 input (insert allowed), 6 output (take only). Shift-click from the player inventory goes
  to the inputs. Hoppers and pipes see all 12 slots, with insertion only into 0 to 5.
- **Value:** each input stack matching a `farmingforblockheads:shipping_bin` recipe (`input` ingredient, `value`
  int >= 0) adds `count x value`. Items with no recipe are never sold and never counted. Capacity is 128 points
  (Shogi rule `shipping_bin/capacity`, minimum 1).
- **Sale cycle:** server tick. While total value < capacity nothing happens. At >= capacity a 20-tick progress bar
  fills; then every input stack that has a recipe is removed entirely and the loot table
  `farmingforblockheads:gameplay/shipping_bin` rolls with the shipment value and the sold stacks as context. Default
  table: emeralds, `floor(value / 128)` (linear level-based value `1/128` per point). Outputs go to the output slots;
  overflow is buffered in the block entity, and selling is blocked until the buffer drains. After a sale a 100-tick
  cooldown. Unknown items stay in the inputs.
- **Display:** the block entity renders up to 12 input items at 0.36 scale inside the crate, the count rising with
  `value / capacity`.
- **GUI:** two 3x2 grids, a fill bar (value / capacity) and a sale-progress bar (sprites
  `container/shipping_bin/fill_progress`, `sale_progress`).
- **Carriers (effect only, no item transport):** sky visible above the bin -> a Shipping Balloon spawns, inflates for
  20 ticks, rises 0.12 blocks/tick with a slow drift (direction changes every 6000 ticks) for 172 blocks, then puffs
  smoke and is removed. Hitting it pops it into a Falling Shipping Crate (loot only with Pantry for Blockheads,
  plus the `lost_in_transit` advancement for an arrow). No sky -> a Shipping Crate appears on the bin; after 40 ticks
  a Courier (zombie-shaped mob, no AI, invulnerable, random merchant name) teleports in beside it, reaches at tick
  60 and teleports out with the crate at tick 80 (Enderman teleport sound, portal particles).
- **Sales history:** every sale is recorded per chunk and recipe (`shipping_bin_sales` saved data, recent window
  1,728,000 ticks = 24 days, all-time totals). Only the Shogi effects `recently_sold` / `total_sold` read it, for
  pack makers to scale prices; no shipped recipe uses them.
- **Recipe:** shaped `IWI / WEW / IGI`: iron ingot, any log, emerald, green wool.
- **Shipped values:** 12 vanilla crops (table in 2.1).

### 3.2 Format changes for 1.19.2

| 26.3 | 1.19.2 port |
|---|---|
| `Recipe<SingleRecipeInput>`, `RecipeMap`, codec serializer | `Recipe<Container>` with a JSON serializer; type `farmingforblockheads:shipping_bin` kept |
| `valueEffect` / `predicate` Shogi fields, sales saved data | dropped (no Shogi on 1.19.2; no shipped recipe uses them) (D4) |
| Loot number provider `shipment_value`, conditions `shipment_value` / `shipment_contains`, custom context set | 1.19.2 loot number providers and conditions register through `Registry.LOOT_NUMBER_PROVIDER_TYPE` / `LOOT_CONDITION_TYPE`; or pay in code (D4) |
| `ValueInput` / `ValueOutput` NBT, `preRemoveSideEffects` | `CompoundTag` load/save, `onRemove` drop |
| Item model `items/shipping_bin.json`, GUI sprites atlas | our own `models/item` parent file; GUI blits textures by path |
| Block model `format_version` key, all rotations angle 0 | loads on 1.19.2 unchanged (extra key ignored) |
| Entity render states, `ShippingBalloonModel`, `ShippingCrateModel` | own 1.19.2 `EntityModel` classes written from the shapes; textures from the donor |

### 3.3 Art in the donor

Block `shipping_bin`, `shipping_bin_front`, `shipping_bin_lid`, `shipping_bin_box`; GUI `shipping_bin` and two
sprites; entity `shipping_balloon`, `merchant` (courier skin). Read from the donor jar under the upstream ids; no
texture is drawn or edited ([texture_sources.md](texture_sources.md)).

### 3.4 Upstream bugs

- **B1 Remainder lost.** A sale removes every sellable stack but pays `floor(value / 128)`: 255 points pay 1
  emerald and 127 points vanish. Only full loads sell, so the loss is at most 127 points per sale (about one
  emerald). Upstream design or bug is unclear (D3).
- **B2 Falling crate drops nothing** without Pantry for Blockheads, so shooting a balloon only destroys the effect.
  Harmless (the emeralds are already paid).

## 4. Fold-in, in build order (Shipping Bin only)

0. **Donor jar.** `blockheads_upstream/farmingforblockheads-neoforge-26.3-26.3.0.2.jar` (or the single upstream
   folder once it exists, [upstream-jar-distribution-policy](agent_notes/upstream-jar-distribution-policy.md)), sha1-pinned. The donor
   resource pack exposes only `assets/farmingforblockheads/{textures,models/block,blockstates}/shipping_bin*`,
   `textures/entity/{shipping_balloon,merchant}.png`, `textures/gui/shipping_bin.png` and the bin sprites. The
   donor's data (551 market recipes of an unknown type, advancements) is never loaded. `mod_sides.md` row and
   `build_deploy.py` category for the donor.
1. **Code home: Creative Delight, `farm/shippingbin/`** (D6), registering under the `farmingforblockheads` namespace
   (Ender's route, [arr-mod-overrides-route](agent_notes/arr-mod-overrides-route.md)): block, block item, block entity, menu, screen,
   recipe type and serializer, balloon / crate / courier entities and renderers. Build guard like CfB's
   `checkNoArrResources` against the donor's paths.
2. **Ids:** `farmingforblockheads:shipping_bin` in `item_dict.js` (and the block dict); Creative Common rebuild.
3. **Tags** via `global.tags`: `minecraft:mineable/axe`; the pack's wooden-container tags if the storage audit lists
   them ([storage_audit.md](storage_audit.md)).
4. **Recipes** via `global.NCH`: the crafting recipe (logs as `#minecraft:logs`); a new NCH helper
   `NCH.shippingBin(input, value)` for the value rows (extend the helper, no `e.custom`). Values: the 12 upstream rows,
   plus whatever D3 adds.
5. **Payout** per D2 (loot table `farmingforblockheads:gameplay/shipping_bin` in our data under the upstream id, or
   coins).
6. **Loot:** block drop table (our own file, same content as upstream). Mob loot: none.
7. **Names and lang** ([names.md](names.md)): `block.`, `container.`, `entity.` keys for the bin, balloon, crate,
   courier, falling crate under `assets/farmingforblockheads/lang/` in our jar (our own text); courier names from
   our config list (D5).
8. **Block audit:** BlockRunner tier, Item Physic, WTHIT (show stored value / capacity), JEI category "Shipping"
   listing each value row ([jei_coverage.md](jei_coverage.md)); tooltip on the bin item ([tooltips.md](tooltips.md)).
9. **Stack size:** 64 (block); no food, can, compost, crop, worldgen, potion or effect step (the bin adds none).
10. **Release:** player changelog bullet under a new `## Shipping Bin` family, TODO row, README index row, queue rows
    `SQ-SHB-*` / `CQ-SHB-*`.

None: item merges (no item overlaps), potions, effects, food values, food families, cans, compost, crops / seasons,
worldgen, trades and chests.

Ordering: 0 before 1 (art paths); 1 before 2 to 8; 2 before 4 (dict keys); D2 and D3 before 4 and 5.

## 5. Decisions for the owner

| # | Question | Recommendation | Default if not answered |
|---|---|---|---|
| D1 | Route: port the bin alone, or install FfB 1.19.2 (Forge 11.3.x) with everything else disabled and add the bin beside it | Port alone: 1.19.2 FfB has no bin, so installing it adds five duplicate systems (section 2.2) to switch off for nothing | port alone |
| D2 | Payout: emeralds (upstream) or Lightman's Currency coins | Emeralds: upstream behavior; the pack's other crop sales (farmer, trader, Wares) are in emeralds | emeralds |
| D3 | Values: upstream 12 vanilla crops only, or a value for every pack crop (Farmer's Delight, Creative Delight members, Hearth and Harvest, Vinery ...) generated from a rule (e.g. by crop yield or food value); keep or carry the remainder (B1) | Upstream 12 first; a pack-wide value table is a new mechanic and needs the owner's rule; keep B1 as upstream | 12 rows, remainder lost |
| D4 | Drop the Shogi rule fields and the per-chunk sales history (price scaling hook) | Drop: nothing in the shipped data uses them | dropped |
| D5 | Carriers: balloon + crate + courier (upstream), balloon only, or particles only | All three: they are the feature's look; courier names from a config list | all three |
| D6 | Code home | Creative Delight `farm/` (farming and food routing; same author family as the CfB compartment); Creative Compat is for fixes only | Creative Delight |
| D7 | Rabbit trap or fishing barrel later (the two FfB blocks with no pack equivalent) | Not now | not planned |

## 6. File map (planned)

- `development/creativeDelight/src/main/java/com/SaintAlfonzo/creativedelight/farm/shippingbin/` block, block entity,
  menu, recipe, entities.
- `development/creativeDelight/src/main/java/com/SaintAlfonzo/creativedelight/client/shippingbin/` screen and
  renderers.
- `development/creativeDelight/src/main/resources/data/farmingforblockheads/` loot tables (block, gameplay payout).
- `development/creativeDelight/src/main/resources/assets/farmingforblockheads/lang/en_us.json`, `models/item/shipping_bin.json`.
- `blockheads_upstream/` donor jar; donor loader filter list.
- NCH helper extension in `neoCraftHelper.js`; recipe rows in a server script.

## 7. Offline checks (once built)

- `python3 tools/check_all.py --fast --changed`; donor build guard (no donor path in our jar).
- Headless: recipe JSON parse of the 12 value rows; payout math `floor(value / 128)` for 127, 128, 255, 256.

## 8. In-game test suite (drafted)

- [ ] **SHB-01 Craft and place:** craft iron, log, emerald, green wool in `IWI / WEW / IGI`; place facing west -> bin faces the player, art shows, axe breaks it fast and it drops itself.
- [ ] **SHB-02 Below capacity:** put 31 wheat (124 points) in -> fill bar near full, no sale, items stay.
- [ ] **SHB-03 One load:** add 1 wheat (128) -> sale bar fills in 1 s, all wheat removed, 1 emerald in the outputs.
- [ ] **SHB-04 Remainder:** put 63 wheat (252) -> 1 emerald, all 63 wheat gone (B1 as decided in D3).
- [ ] **SHB-05 Unsellable item:** put cobblestone plus 32 wheat -> wheat sold, cobblestone stays in its slot.
- [ ] **SHB-06 Output full:** fill all 6 output slots with emeralds, sell a load -> extra emeralds buffered, next sale waits until the outputs are emptied.
- [ ] **SHB-07 Slots:** shift-click crops from the inventory -> they go to the inputs; placing an item in an output slot is refused.
- [ ] **SHB-08 Hopper:** hopper above feeds inputs; hopper below pulls emeralds -> automated selling works.
- [ ] **SHB-09 Balloon:** sell under open sky -> balloon inflates, rises out of sight, smoke at the top; hit it -> it pops and a crate falls, no item drops.
- [ ] **SHB-10 Courier:** sell under a roof -> crate on the bin, a named courier appears beside it, reaches, both teleport away; the courier takes no damage.
- [ ] **SHB-11 Break with contents:** break a bin with inputs, outputs and a buffered overflow -> every item drops.
- [ ] **SHB-12 JEI and WTHIT:** JEI shows the bin recipe and the 12 value rows; WTHIT shows the stored value.

## 9. History

- 2026-11-23: investigation written (26.3.0.2 decompiled with `tools/decompile_mod.py`, 11.3.5 inventory), nothing
  built.
