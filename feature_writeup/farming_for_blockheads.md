# Farming for Blockheads: overlap and integration report

Investigation written 2026-11-23, nothing built. Jars read: Farming for Blockheads `26.3.0.2` (NeoForge, MC 26.3,
`DecompiledMods/farmingforblockheads-neoforge-26.3-26.3.0.2.jar`) and `11.3.5` (Fabric, MC 1.19.2,
`DecompiledMods/1.19.2-farmingforblockheads-fabric-1.19.2-11.3.5.jar`, read for what the 1.19.2 line has). License:
All Rights Reserved (BlayTheNinth, both jars; permissions page `https://mods.twelveiterations.com/permissions`). A port
is needed: the Shipping Bin exists only in the 26.3 line, and the 1.19.2 line has no bin. Initial goal (owner,
2026-11-23, scratchpad "only for shipping bin, disable the rest"): port the Shipping Bin only. The rest of the mod is
audited in sections 2 and 3; the rabbit trap was added to the port on 2026-11-24 (section 2.3), the rest is not planned. Short name in this file: FfB. Test prefix: `SHB-`
(`FFB` is taken by the oven queue rows).

Scope rule: integration adds no recipe or mechanic that is not in the mod or defined by a pack rule unless the owner
asks by name; upstream bugs are fixed.

## 0. At a glance

<!-- generated:status_index begin open_work sha1=96345f30619a -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 4 open TODO rows (Planned 4), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `mod-integration #7` | Planned | S C P T | Farming for Blockheads: port the Shipping Bin only (ARR, 26.3 donor art, own code) |
| `mod-integration #8` | Planned | C | Courier Spawn Point: final texture (temporary: vanilla cartography table look) |
| `mod-integration #9` | Planned | S | Courier Spawn Point: final recipe (temporary: chest surrounded by 8 trapdoors of any kind) |
| `mod-integration #10` | Planned | S C P T | Farming for Blockheads: port the rabbit trap (pack catch list, biome catch tables, tooltip) |
<!-- generated:status_index end open_work -->

| Part | Where | Status |
|---|---|---|
| Inventory (26.3): 12 blocks, 6 items, 5 entities, 3 menus; recipes 12 crafting, 551 market, 12 shipping bin | section 1, 3 | read |
| Shipping Bin mechanics (sale rule, payout, carriers) | section 3.1 | read from the decompile |
| Overlap | section 2 | no other shipping bin in the pack; the rest of FfB overlaps Hearth and Harvest, Quark, Incubation, Slice and Dice |
| Upstream bugs | B1, B2 (section 3.4) | none blocking for the bin |
| Fold-in (bin and rabbit trap) | section 4 | 13 steps drafted |
| Decisions | D1 to D11 (section 5) | ruled 2026-11-23 and 2026-11-24 (5.1) except D7b (fishing barrel), D10, D11 (trap details); D7c pinned |
| Tests | SHB-01 to SHB-27 (section 8) | drafted |

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
| Fishing barrel (auto-fishing with a rod) | fishing rods with the Stardew Fishing minigame; Creative Delight fishing net (hand tool on kelp and sea grass, no fish) | passive rod fishing skips the minigame |
| Rabbit trap | none found | new, small |
| Fox note | none | flavor item from the fox event |

The scratchpad ruling ("disable the rest") holds for the audit as well: every other block has a pack equivalent
except the rabbit trap and the fishing barrel. Those two are left out unless the owner asks for them by name.

### 2.3 Rabbit trap, fishing barrel, scythe (investigated 2026-11-24, owner request)

**Rabbit trap** (`farmingforblockheads:rabbit_trap`, 26.3 only). Recipe `PP / SCP`: planks, sticks, one carrot
(the carrot is not bait; nothing is consumed). A wooden box trap, horizontal facing, rendered by a block entity
(set-up and snap animation).
- Player within 64 blocks: every 10 ticks, the nearest animal in the `rabbit_trap_capturable` entity tag (armadillo,
  cat, chicken, frog, rabbit) within 8 blocks (2 up/down) is pathed to the trap; touching it captures it (full NBT
  saved, the mob is removed, trapdoor sound, poof). The block switches to `triggered`.
- No player nearby: the chance to catch something rises with the time left alone, `min(0.95, ticks / 24000)`, rolled
  every 1200 ticks. A hit first rolls the loot table `gameplay/rabbit_trap` (only the fox note, chance 0.0005, a
  renamed paper "I stole your carrot."), else spawns a fresh animal from `rabbit_trap_spawnable` (same 5 types,
  10% baby). So an unattended trap creates animals from nothing (a passive mob farm), up to one per trap per
  catch.
- Right-click a triggered trap: the catch is released in front of it and the trap pops off as an item with 1 damage
  (8 uses, a durability bar on the item). Breaking it also releases the catch.
- Pack fit: no equivalent (Incubation, Quark and Hearth and Harvest have no live-capture trap; animal pens use leads
  and Quark's feeding trough). Armadillo does not exist on 1.19.2.
- Port cost: block, block entity, renderer with two animations, entity-tag reads, item durability through NBT (no
  data components on 1.19.2). Medium. **Ruled: port (owner, 2026-11-24, D7).**

#### 2.3.1 Rabbit trap: catch list for the pack (2026-11-24)

The trap only handles `Animal` subclasses (checked in the spawn dump `kubejs/exported/spawns.json`, superclasses
column). Rule used: a small animal that walks on the ground and could fit a wooden box trap. Proposed tag
`farmingforblockheads:rabbit_trap_capturable` (via `global.tags`), 18 types:

| Entity | Source | Note |
|---|---|---|
| `minecraft:rabbit`, `minecraft:chicken`, `minecraft:frog`, `minecraft:cat` | vanilla (upstream list minus armadillo) | |
| `cozys-improved-cats:improved_cat` | Cozy's Improved Cats | the pack's cat (subclass of Cat), spawns in 4 biomes |
| `minecraft:ocelot` | vanilla | wild jungle cat, same size as a cat |
| `untitledduckmod:duck` | Untitled Duck Mod | |
| `quark:crab` | Quark | |
| `alexsmobs:jerboa`, `platypus`, `skunk`, `roadrunner`, `banana_slug`, `rain_frog`, `mudskipper`, `tasmanian_devil`, `raccoon`, `warped_toad` | Alex's Mobs | warped toad only in warped forests (Nether) |

Left out: fox (the trap's own joke: the fox steals the carrot and leaves the Mysterious Note); flying birds (parrot,
blue jay, hummingbird, sunbird, toucan, seagull, potoo, both crows); geese, emu, anteater, capuchin and larger
(do not fit); rattlesnake (venomous); axolotl, turtle, tadpole (water); cockroach and flies (insects); bunfungus (not
an `Animal`); bees (fly). **Tamed or owned animals** (cats, raccoons, mudskippers, warped toads with an owner) are
not caught (recommendation D10), so players' pets never vanish into a trap.

#### 2.3.2 Rabbit trap: catch tables by biome (2026-11-24)

Unattended catches (nobody within 64 blocks; chance `min(0.95, ticks alone / 24000)`, rolled every 1200 ticks and
once on chunk reload for the time away) roll an animal from the trap's biome instead of upstream's fixed 5-type
list. Table: [rabbit_trap_catches.csv](farming_for_blockheads/rabbit_trap_catches.csv), built 2026-11-24 from the
spawn dump (dumped 2026-11-19, after the pack's spawn tuning): for each biome, every catchable type in its natural
spawn lists, with the biome's spawn weight and its share. 116 of 169 biomes have catches; the other 53 (oceans,
caves, most of the Nether, the End, AE2 storage) catch nothing, so only the fox note can come out there. Examples:

| Biome | Catches (share) |
|---|---|
| Plains | chicken 42 %, raccoon 42 %, duck 17 % |
| Forest | chicken, raccoon, Tasmanian devil 24 % each, skunk 17 %, duck 10 % |
| Desert | jerboa 34 %, rain frog 29 %, roadrunner 26 %, rabbit 11 % |
| Swamp | chicken 38 %, frog 38 %, duck 23 % |
| Snowy plains | rabbit 59 %, raccoon 29 %, duck 12 % |
| Jungle | chicken 77 %, duck 15 %, ocelot 8 % |

At build time a generator (`tools/rabbit_trap_catches.py`, listed in `tools/generators.json`) writes the table as data
(`data/farmingforblockheads/rabbit_trap/biomes.json` in Creative Delight) from the dump, so it follows spawn tuning
after each `/dumpall`. Baby chance stays 10 %. One catch per trap until a player releases it, so traps cannot fill
an area with animals.

#### 2.3.3 Rabbit trap: behavior and tooltip

Behavior (for the writeup and JEI info page):
- Place it anywhere. While a player is within 64 blocks it lures the nearest catchable animal within 8 blocks and
  snaps shut when the animal steps in.
- Left alone, it slowly catches an animal that lives in the biome (higher chance the longer it is left; 95 % after a
  full day alone).
- A sprung trap shows the catch. Right-click it to release the animal; the trap pops off as an item and loses one of
  its 8 uses. Breaking it also releases the catch.
- Very rarely a fox gets there first and leaves a Mysterious Note.

Tooltip draft (item; Creative Common tooltip helper, [tooltips.md](tooltips.md)):
- "Catches small animals: rabbits, chickens, frogs, cats, ducks, crabs and more."
- "Lures animals within 8 blocks while you are near; catches local animals while left alone."
- "Right-click a sprung trap to release the catch. 8 uses."
- Shift: the full catch list by name (from the tag, so it stays in sync).

**Fishing barrel** (`farmingforblockheads:fishing_barrel`, 26.3 only). Recipe `PSP / PNP / PRP`: planks, string,
nautilus shell, redstone. A barrel with a rod slot and 8 storage slots; it faces water.
- Needs a fishing rod (tag `fishing_rods`: vanilla rod and `c:tools/fishing_rods`) in slot 0 and water in the block
  in front of it, one down. Fishes every `100 + random(0..500) - 100 x Lure` ticks (minimum 40; 5 to 30 s), rolling
  the vanilla `gameplay/fishing` table with the rod as tool (Luck of the Sea applies), 1 rod durability per catch,
  rod break effect. Catches go to the 8 slots (overflow buffered, fishing stops while full); hoppers can pull them.
  A 36-tick catch animation (bob, splash).
- Textures: [fishing_barrel_textures.png](farming_for_blockheads/fishing_barrel_textures.png) (front with the rod
  hole, side, back, bottom, GUI; 8x and 2x preview from the donor jar, not pack art).
- Pack fit: little overlap with the fishing net. The net ([ocean_delight.md](ocean_delight.md) 5.1, Creative Delight
  `FishingNetItem`) is a hand tool, not a passive catcher: right-click a kelp stalk (with kelp above) or sea grass /
  sea grape / pickerelweed; each search costs 1 of 92 uses with a 2.5 s cooldown and rolls the pack's net tables by
  biome: clay, kelp, scute everywhere, shrimp in oceans, caviar in rivers, coral in warm oceans, ocean treasure
  (nautilus shell 2 %, heart of the sea 0.2 %), 5 % junk; kelp searches sometimes break the stalk and spawn a
  pufferfish. It gives no fish. The barrel is passive rod fishing (fish, treasure, junk from the vanilla table) and
  skips the Stardew Fishing minigame and Fish On The Line, which the pack runs for hand fishing. A rod barrel gives
  treasure (enchanted books, saddles, name tags) without the minigame. Iron Fishing Rods' rods would need to be in the rod tag. Cozy Cats' GLM on
  `gameplay/fishing` would apply to it.
- Port cost: block, block entity, menu and screen, renderer animation. Medium.

**Scythe** (`farmingforblockheads:iron_scythe`, "Scythe", 26.3 only). Recipe `III / SI / S` (vanilla hoe-like),
iron hoe stats (attack speed -3.0). Right-click on a crop or its farmland, or breaking a mature crop, breaks every
mature `CropBlock` in a 3x3 around it through the player's normal block breaking (drops, no replant). Only one
tier.
- Existing scythe in the pack: only `mca:scythe`, MCA's reaper weapon (not a farm tool). The pack's own scythe is
  already planned: `cooking/kitchen #16` (area by tier wood 3, iron 5, diamond 7, netherite 9, replants like
  Farmer's Delight right-click harvest, built on `crops/HoeHarvestArea`). Pack hoes already harvest 1x1 to 5x5 through
  Quark ([hoe-harvest-area](agent_notes/hoe-harvest-area.md)), so the FfB iron scythe (3x3, no replant) is weaker
  than an iron hoe's planned area and gives nothing new.
- Recommendation: no FfB scythe. Its iron scythe art (donor texture `item/iron_scythe`) can serve as the iron tier's
  art for kitchen #16 if the owner wants it (an art source, not a texture edit).
- **Pinned for later (owner, 2026-11-24, D7c):** the owner wants scythes, but they clash with the hoe area harvest.
  Owner's idea: take the area harvest off hoes and give it to scythes. Recommendation when it is picked up (needs a
  ruling then; it changes [crops.md](crops.md) CR-26 to CR-30 and the `hoe_harvest/NxN` tags): hoes keep tilling and a
  1x1 right-click harvest; scythes (kitchen #16, wood to netherite) take the per-tier areas (the `hoe_harvest/NxN`
  tags move to the scythes, the Quark mixins read them from the held scythe) and replant; scythes do not till. That
  gives each tool one job and keeps a single area system. Recorded on kitchen #16; nothing changes until then.

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
| Loot number provider `shipment_value`, conditions `shipment_value` / `shipment_contains`, custom context set | 1.19.2 loot number providers and conditions register through `Registry.LOOT_NUMBER_PROVIDER_TYPE` / `LOOT_CONDITION_TYPE`; kept so later prices live in the loot table (ruling D2, D3) |
| `ValueInput` / `ValueOutput` NBT, `preRemoveSideEffects` | `CompoundTag` load/save, `onRemove` drop |
| Item model `items/shipping_bin.json`, GUI sprites atlas | our own `models/item` parent file; GUI blits textures by path |
| Block model `format_version` key, all rotations angle 0 | loads on 1.19.2 unchanged (extra key ignored) |
| Entity render states, `ShippingBalloonModel`, `ShippingCrateModel` | own 1.19.2 `EntityModel` classes written from the shapes; textures from the donor |

### 3.3 Art in the donor

Block `shipping_bin`, `shipping_bin_front`, `shipping_bin_lid`, `shipping_bin_box`; GUI `shipping_bin` and two
sprites; entity `shipping_balloon`, `merchant` (courier skin). Read from the donor jar under the upstream ids; no
texture is drawn or edited ([texture_sources.md](texture_sources.md)).

### 3.4 Upstream bugs

- **B1 Remainder lost (fixed by ruling D3a: partial sales, credit kept).** A sale removes every sellable stack but pays `floor(value / 128)`: 255 points pay 1
  emerald and 127 points vanish. Only full loads sell, so the loss is at most 127 points per sale (about one
  emerald). Upstream design or bug is unclear (D3).
- **B2 Falling crate drops nothing** without Pantry for Blockheads, so shooting a balloon only destroys the effect.
  Harmless (the emeralds are already paid).

## 4. Fold-in, in build order (Shipping Bin and rabbit trap)

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
5. **Payout** (ruling D2): loot table `farmingforblockheads:gameplay/shipping_bin` in our data under the upstream id,
   emeralds at `floor(value / 128)`; the loot context carries the shipment value and the sold stacks so Lightman's
   prices and per-crop rules can be added there later.
5a. **Courier** (ruling D5, D5a, D5b): config `courierMode` (`walk_in_out` default, `walk_out`, `at_bin`),
   `courierWalkDistance`, `courierSpawnSearchRadius` (default 96), `courierWalkTimeoutTicks` (default 1200); the Courier Spawn Point block (id in `item_dict.js`, recipe
   via `global.NCH`, art per D8, texture_sources row); walking pathfinds on the ground and falls back to the
   configured distance (or `at_bin`) when no path is found; the courier's follow range is raised to the search radius
   plus 16 (vanilla path search stops at the follow range) and paths longer than 32 blocks are walked in legs; the
   walk timer (ruling D9) teleports a late courier to the bin and ends a late exit in place; invisibility drink then despawn with the crate. MCA
   villager skin for the courier once MCA textures are added. Config backup in `configs_edited/`.
6. **Loot:** block drop table (our own file, same content as upstream). Mob loot: none.
7. **Names and lang** ([names.md](names.md)): `block.`, `container.`, `entity.` keys for the bin, balloon, crate,
   courier, falling crate under `assets/farmingforblockheads/lang/` in our jar (our own text); courier names from
   our config list (D5).
8. **Block audit:** BlockRunner tier, Item Physic, WTHIT (show stored value / capacity), JEI category "Shipping"
   listing each value row ([jei_coverage.md](jei_coverage.md)); tooltip on the bin item ([tooltips.md](tooltips.md)).
8a. **Rabbit trap** (ruling D7): block (facing, triggered), block entity (catch NBT, unattended timer, lure every 10
   ticks), renderer with the set-up and snap animations, item durability 8 through NBT (1.19.2 has no data
   components; stack size 1 once used), recipe `PP / SCP` via `global.NCH`, catch tag via `global.tags` (2.3.1),
   biome data from `tools/rabbit_trap_catches.py` (2.3.2), fox note loot table, tooltip and JEI info (2.3.3),
   donor art `rabbit_trap`, `rabbit_trap_bottom`, `rabbit_trap_front`. Tamed animals skipped (D10).
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
| D7 | Rabbit trap (section 2.3) | Ruled 2026-11-24: port, pack catch list, biome tables, tooltip (5.1) | ruled |
| D7b | Fishing barrel (section 2.3): port, and if so: treasure loot or the fishing-net tables | Not now: overlaps the fishing net and skips the fishing minigame; if wanted, roll vanilla `gameplay/fishing` minus treasure | not planned |
| D7c | Scythe (section 2.3) | Pinned 2026-11-24 for later: no FfB scythe; kitchen #16 scythes take the area harvest from hoes (2.3) | pinned |
| D10 | Rabbit trap and tamed animals: catch owned pets or not | Not caught (an owned cat, raccoon, mudskipper or warped toad is skipped) | not caught |
| D11 | Rabbit trap catch list (2.3.1): the 18 types | As listed | as listed |
| D8 | Courier Spawn Point art and recipe | Ruled 2026-11-24 (5.1): temporary; final art and recipe are TODO rows | temporary |
| D9 | Courier Spawn Point reach and walk timeout | Ruled 2026-11-24 (5.1) | ruled |

### 5.1 Owner rulings (owner, 2026-11-23 and 2026-11-24)

| # | Ruling |
|---|---|
| D1 | Port the bin alone: our own code under the `farmingforblockheads` namespace, the 26.3 jar as an art donor, FfB itself not installed. |
| D2 | Emeralds for now. Later, Lightman's Currency prices defined in the payout loot table. So the payout stays a loot table: port the `shipment_value` number provider and the `shipment_value` / `shipment_contains` conditions (section 3.2), never a payout in code. |
| D3 | The 12 vanilla value rows for now; all crops later, also through the loot table. |
| D3a | (2026-11-24) A sale takes only sellable increments and leaves the rest (replaces upstream B1): with total value V and capacity C, the sale pays `floor(V / C)` loads and removes only enough items to cover `floor(V / C) x C`, taking whole items slot by slot (input slots 0 to 5, `ceil(needed / value)` items from each until covered). Everything else stays in the inputs. The last item taken can overshoot by less than one item's value (at most 7 points with the shipped values); the overshoot is kept as stored credit in the bin (saved with the block, counted in the fill bar, used by the next sale), so no value is lost. Example: 31 carrots (155 points) -> 1 emerald, 26 carrots taken (130 points), 5 carrots stay, 2 points credit. |
| D4 | Follows from D2 and D3: Shogi fields and the sales history dropped; the loot context (value, sold stacks) kept. |
| D5 | All three carriers (balloon, crate, courier). The courier uses MCA villager textures once those are added (until then the donor `merchant` skin). It no longer teleports out: at its despawn point it stops, drinks an invisibility potion (wandering-trader drink animation and sound) and despawns with the crate. Courier mode is a config option with three values: `walk_in_out` (default: arrives on foot, takes the crate, walks back out), `walk_out` (appears beside the bin, walks away), `at_bin` (appears beside the bin, drinks on the spot). |
| D5a | Walking distance is a config value (blocks between the bin and the courier's spawn and despawn point). |
| D5b | New block: **Courier Spawn Point** (`farmingforblockheads:courier_spawn`, own id). In a walking mode the courier spawns at and walks back to the nearest such block in range (D9) instead of a point at the configured distance, so players can lay out a path for it. Without one in range the configured distance applies. Art and recipe: D8. |
| D8 | (2026-11-24) Temporary art: the vanilla cartography table look (the block model names `minecraft:block/cartography_table`'s textures; no texture copied or edited). Temporary recipe: a chest surrounded by 8 trapdoors of any kind (`#minecraft:trapdoors`, wooden and iron). Both are marked for update: TODO rows `mod-integration` "Courier Spawn Point: final texture" and "Courier Spawn Point: final recipe". |
| D9 | (2026-11-24) Spawn point search radius is a config value with a high default spanning several chunks: `courierSpawnSearchRadius` default 96 blocks (6 chunks), range 0 to 256 (0 = spawn points off). Walk timer: `courierWalkTimeoutTicks` (default 1200, 60 s). A courier that has not reached the bin when the timer runs out is teleported to the bin (Enderman teleport sound and portal particles, as upstream's exit). On the way out the same timer applies; when it runs out the courier drinks and despawns where it stands. The timer counts on the bin's block entity in game time, so a courier frozen in an unloaded chunk still times out. |
| D6 | Code home: Creative Delight (default taken; not asked). |
| D5c | (2026-11-24) A courier that came from a Courier Spawn Point walks back to that block to drink and despawn, when spawn points are enabled (`courierSpawnSearchRadius` above 0), the block is still there and a path exists; otherwise it walks out to the configured distance (or drinks in place in `at_bin` mode). The walk timer (D9) still applies to the way back. |
| D7 | (2026-11-24) Port the rabbit trap. Update the catch list for the pack (2.3.1), add an item tooltip that describes the behavior (2.3.3), and catch from per-biome tables when left alone (2.3.2). |
| D7c | (2026-11-24) Scythe pinned for a later recommendation (2.3); owner leans toward moving the hoe area harvest to scythes. |

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
- [ ] **SHB-04 Partial sale:** put 63 wheat (252) -> 1 emerald, 32 wheat taken, 31 stay; put 31 carrots (155) in an empty bin -> 1 emerald, 26 carrots taken, 5 stay, the fill bar shows 27 points (25 + 2 credit).
- [ ] **SHB-05 Unsellable item:** put cobblestone plus 32 wheat -> wheat sold, cobblestone stays in its slot.
- [ ] **SHB-06 Output full:** fill all 6 output slots with emeralds, sell a load -> extra emeralds buffered, next sale waits until the outputs are emptied.
- [ ] **SHB-07 Slots:** shift-click crops from the inventory -> they go to the inputs; placing an item in an output slot is refused.
- [ ] **SHB-08 Hopper:** hopper above feeds inputs; hopper below pulls emeralds -> automated selling works.
- [ ] **SHB-09 Balloon:** sell under open sky -> balloon inflates, rises out of sight, smoke at the top; hit it -> it pops and a crate falls, no item drops.
- [ ] **SHB-10 Courier (default walk_in_out):** sell under a roof, no spawn point nearby -> crate on the bin, a named courier walks in from about the configured distance, takes the crate, walks back out, stops, drinks invisibility and vanishes with the crate; it takes no damage.
- [ ] **SHB-11 Break with contents:** break a bin with inputs, outputs and a buffered overflow -> every item drops.
- [ ] **SHB-12 JEI and WTHIT:** JEI shows the bin recipe and the 12 value rows; WTHIT shows the stored value.
- [ ] **SHB-13 Courier modes:** set `courierMode` to `walk_out`, then `at_bin` -> courier appears beside the bin and walks away / drinks on the spot.
- [ ] **SHB-14 Walk distance:** set `courierWalkDistance` to 4 and to 20 -> the courier's spawn and despawn points move to match.
- [ ] **SHB-15 Courier Spawn Point:** place a spawn point 10 blocks from the bin -> the courier spawns on it, walks to the bin and back to it before drinking; remove it -> the configured distance applies again.
- [ ] **SHB-16 No path:** wall the bin in on all sides (roof, no gap) -> the courier falls back to `at_bin` behavior, no stuck courier remains.
- [ ] **SHB-17 Far spawn point:** place a spawn point 90 blocks away (inside 96), stay near the bin -> the courier comes from it; a spawn point at 100 blocks is ignored (configured distance applies).
- [ ] **SHB-18 Walk timeout:** put the spawn point behind a long maze, sell -> after 60 s the courier teleports to the bin with the Enderman sound; set `courierWalkTimeoutTicks` to 200 -> it teleports after 10 s.
- [ ] **SHB-19 Courier Spawn Point block:** craft a chest surrounded by 8 trapdoors (mix oak, spruce and iron) -> 1 Courier Spawn Point with the cartography table look.
- [ ] **SHB-20 Credit carried:** after SHB-04's carrots, add carrots until a second sale -> the 2 credit points count toward it.
- [ ] **SHB-21 Trap lure:** place a rabbit trap in a plains with chickens nearby, stand 10 blocks away -> the nearest chicken walks in, the trap snaps (sound, poof) and shows it.
- [ ] **SHB-22 Release:** right-click the sprung trap -> the chicken steps out in front, the trap pops off as an item with 7 of 8 uses left; after 8 uses it is gone.
- [ ] **SHB-23 Catch list:** tame a cat, lead a fox, an untamed ocelot and a duck past a trap -> the duck and the ocelot are caught, the fox and the tamed cat are not.
- [ ] **SHB-24 Biome table:** place traps in a desert and a snowy plains, leave the area for a full day (or set game time ahead), come back -> desert traps hold jerboas, rain frogs, roadrunners or rabbits; snowy ones rabbits, raccoons or ducks.
- [ ] **SHB-25 No catches:** a trap in a deep ocean or the End left for a day -> stays empty (rarely a Mysterious Note).
- [ ] **SHB-26 Tooltip:** hover the trap item -> three behavior lines; with Shift the catch list by name.
- [ ] **SHB-27 Courier returns to its spawn point:** with a Courier Spawn Point 30 blocks away, sell under a roof -> the courier walks from it, takes the crate, walks back to it and drinks there; break the spawn point while it walks in -> it leaves to the configured distance instead.

## 9. History

- 2026-11-23: investigation written (26.3.0.2 decompiled with `tools/decompile_mod.py`, 11.3.5 inventory), nothing
  built.
- 2026-11-23: owner rulings D1 to D5b (section 5.1): bin alone, emeralds now and loot-table prices later, 12 vanilla
  rows now, all three carriers with a walking courier, an invisibility exit, a configurable walk distance and a
  Courier Spawn Point block; D8, D9 open.
- 2026-11-24: rulings D3a (partial sales, credit), D8 (temporary cartography table look, chest + 8 trapdoors
  recipe), D9 (search radius 96, walk timeout teleport); rabbit trap, fishing barrel and scythe investigated (2.3, D7 to
  D7c open).
- 2026-11-24: rabbit trap ruled in (catch list 2.3.1, biome tables 2.3.2 with the draft CSV, tooltip 2.3.3); courier
  returns to its spawn point (D5c); fishing barrel textures shown and the fishing net described (2.3); scythe pinned
  (D7c).
