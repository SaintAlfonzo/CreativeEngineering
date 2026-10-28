# Crop yields and soil yield

What every crop drops when harvested ripe, and the soil yield system: extra produce for crops grown on better soil.
Soils and harvesting mechanics: [crops.md](crops.md). Bell pepper colour weights: [crops.md](crops.md) section 3.

## 0. At a glance

| Part | Status | Where |
|---|---|---|
| Current drop yields, every overworld farmland crop | reference (loot dump 2026-10-26) | section 2.1 |
| Soil yield, overworld crops (27) | BUILT 2026-10-28, off for every crop by default; live check queued (SQ-SY) | section 3, `creativeengineering/soil_yield.js` |
| Soil yield, nether crops | Planned, outline only (TODO `farming/crops #24`) | section 4 |

Config: `config/creativeengineering-common.toml` `[crop_drops.soil_yield]` (tries and chance per soil) and
`[crop_drops.soil_yield.crops]` (one `true`/`false` per crop, all `false`). A change needs `/reload`.

Install: patcherMod (the new config section) and the Creative Delight scripts. The script only runs with Creative Delight
loaded (`global.CD`).

## 1. Overview

Every crop's harvest comes from its loot table. Most ripe crops drop one produce item and a few seeds. Vanilla and most
Delight crops add a binomial bonus: 3 tries at 57% (`binomial_with_bonus_count`, extra 3, p 0.571), each Fortune level
adding one try. On carrots, potatoes, onions, peanuts, sweet potatoes and zucchini that bonus is on the produce; on
wheat, beetroot, cabbage, broccoli, cauliflower, turnip, corn and the seed crops it is on the seeds.

The soil under the crop changes growth speed only (Rich Soil's 20% bone meal chance, Fertile Soil's soaked boost, see
[crops.md](crops.md) 2.4). Out of the box no soil changes how much a crop drops. The soil yield system adds that.

## 2. Reference

### 2.1 Current drop yields

Read from `kubejs/exported/loot_tables/` (dump of 2026-10-26, with the pack's bell pepper weights). Ripe harvest, broken
or right-click replanted, no Fortune. "Average produce" is the mean count of the produce item per ripe harvest; the
soil columns are that mean with the soil yield switched on for the crop at the default soil values (section 2.2). "can't
grow" = the crop refuses that soil (untilled Rich Soil takes no farmland crop).

| Crop | Switch (`crops`) | Ripe harvest, no Fortune | Seeds | Average produce | Rich Farmland | Fertile Soil | Rich Soil |
|---|---|---|---|---|---|---|---|
| Wheat | `wheat` | 1 Wheat | 1 to 4 seeds (2.71) | 1.00 | 1.70 | 1.50 | can't grow |
| Carrots | `carrots` | 2 to 5 Carrots | (the carrot is the seed) | 3.71 | 4.41 | 4.21 | can't grow |
| Potatoes | `potatoes` | 2 to 5 Potatoes; 2% one Poisonous Potato | (the potato is the seed) | 3.71 | 4.41 | 4.21 | can't grow |
| Beetroots | `beetroots` | 1 Beetroot | 1 to 4 seeds (2.71) | 1.00 | 1.70 | 1.50 | can't grow |
| Cabbage | `cabbages` | 1 Cabbage | 1 to 4 seeds (2.71) | 1.00 | 1.70 | 1.50 | can't grow |
| Onion | `onions` | 2 to 5 Onions | (the onion is the seed) | 3.71 | 4.41 | 4.21 | can't grow |
| Tomatoes (bottom vine) | `tomatoes` | 1 to 2 Tomatoes; 5% one Rotten Tomato | 1 seed (not on a rope) | 1.50 | 2.20 | 2.00 | can't grow |
| Rice (panicles) | `rice` | 1 Rice Panicle (1 Rice with a knife); the bottom always drops 1 Rice | (rice is the seed) | 1.00 | 1.70 | 1.50 | 1.70 |
| Bell Pepper | `bell_peppers` | 1 pepper (red, green, yellow 1:1:1), plus 15% each for one more of each colour | 1 to 2 seeds (1.5) | 1.45 | 2.15 | 1.95 | can't grow |
| Coffee (Rustic) | `coffee` | 1 + 1 to 4 Coffee Beans | (the bean is the seed) | 3.50 | 4.20 | 4.00 | can't grow |
| Cotton | `cotton` | 1 to 3 Cotton Bolls | 1 to 3 seeds (2) | 2.00 | 2.70 | 2.50 | can't grow |
| Ender Crop | `ender_crop` | 1 Ender Pearl, 10% one more | 1 seed, 10% one more (1.1) | 1.10 | 1.80 | 1.60 | can't grow |
| Tobacco | `tobacco` | 4 + 1 to 4 Tobacco Leaves (5 to 8) | 1 to 2 seeds (1.57) | 6.71 | 7.41 | 7.21 | can't grow |
| Flax (lower half) | `flax` | 1 Flax | 1 to 3 seeds (2.14) | 1.00 | 1.70 | 1.50 | can't grow |
| Hemp (top) | `hemp` | top: 1 to 4 Hemp Fibre (2.88); grown bottom: 1 to 4 (2.5) | 1 seed from each half | 2.88 | 3.58 | 3.38 | can't grow |
| Corn (lower half) | `corn` | 1 Corn per half (2 per plant) | 1 to 4 seeds per half (2.71) | 1.00 | 1.70 | 1.50 | can't grow |
| Cave Carrots | `cave_carrots` | 2 to 4 Cave Carrots | (the carrot is the seed) | 3.00 | 3.70 | 3.50 | can't grow |
| Broccoli | `broccoli` | 1 Broccoli | 1 to 4 seeds (2.71) | 1.00 | 1.70 | 1.50 | can't grow |
| Cauliflower | `cauliflower` | 1 Cauliflower | 1 to 4 seeds (2.71) | 1.00 | 1.70 | 1.50 | can't grow |
| Garlic | `garlic` | 1 to 3 Garlic (2.14) | (unripe: 1 clove) | 2.14 | 2.84 | 2.64 | can't grow |
| Sweet Potato | `sweet_potato` | 1 to 4 Sweet Potatoes (2.71) | (the potato is the seed) | 2.71 | 3.41 | 3.21 | can't grow |
| Turnip | `turnip` | 1 Turnip | 1 to 4 seeds (2.71) | 1.00 | 1.70 | 1.50 | can't grow |
| Zucchini | `zucchini` | 1 to 4 Zucchini (2.71) | 1 seed | 2.71 | 3.41 | 3.21 | can't grow |
| Cucumber | `cucumber` | 1 Cucumber | none when ripe (section 5, F1) | 1.00 | 1.70 | 1.50 | can't grow |
| Oats | `oats` | 1 Oat (Raw Oats with a knife) | 1 seed (1 to 2 with a knife) | 1.00 | 1.70 | 1.50 | can't grow |
| Peanuts | `peanuts` | 2 to 5 Peanuts; 1% one Magic Peanut | (the peanut is the seed) | 3.71 | 4.41 | 4.21 | can't grow |
| Adzuki | `adzuki` | 2 to 3 Adzuki Beans (ripe and flowering) | (the bean is the seed) | 2.50 | 3.20 | 3.00 | 3.20 |

- Fortune: each level adds one try to a binomial bonus, and one item per level (random 0 to level) to a
  `uniform_bonus_count` bonus (Rustic's crops, FD tomatoes). The soil yield pools ignore Fortune.
- Plain farmland (and dirt for rice and adzuki) gives the "Average produce" column: `farmlandTries` is 0 by default.
- Ender Crop grows on farmland soils only in the dark (crops.md 2.1); its pools apply the same way.
- Other drops on top: FD's knife straw on wheat and rice, the corn knife Corncob (25%, corn_delight.md 2.4).
- Rustic rows: 1.3.0 and 1.7.0 have the same coffee, cotton and pepper tables (checked against the donor jar).

### 2.2 Soil values (defaults)

| Soil | Block | Config keys | Tries | Chance | Average extra | Most extra | Grows here |
|---|---|---|---|---|---|---|---|
| Farmland | `minecraft:farmland` | `farmlandTries`, `farmlandChance` | 0 | 0.0 | 0 | 0 | every farmland crop |
| Rich Farmland | `farmersdelight:rich_soil_farmland` | `richFarmlandTries`, `richFarmlandChance` | 2 | 0.35 | 0.70 | 2 | every farmland crop, rice, adzuki |
| Fertile Soil | `immersive_weathering:mulch_block` | `fertileSoilTries`, `fertileSoilChance` | 2 | 0.25 | 0.50 | 2 | everything but Nether plants |
| Rich Soil (untilled) | `farmersdelight:rich_soil` | `richSoilTries`, `richSoilChance` | 2 | 0.35 | 0.70 | 2 | rice and adzuki only (no farmland crop) |

Average extra = tries x chance. Chance of at least one extra = 1 - (1 - chance)^tries: 58% on Rich Farmland and Rich
Soil, 44% on Fertile Soil. Fertile Soil is set lower than Rich Farmland because its soaked growth boost is the stronger of
the two (crops.md 2.4). Tries range 0 to 8, chance 0 to 1. A soil at 0 tries or chance 0 adds no pool.

### 2.3 Enabled and disabled

| Crop switch | Default | Effect |
|---|---|---|
| any key in `[crop_drops.soil_yield.crops]` | `false` | `false`: the crop's table is untouched, same drops on every soil (section 2.1 "Average produce"). `true`: one pool per soil with tries above 0 is appended to the crop's table |

Keys, in order: `wheat`, `carrots`, `potatoes`, `beetroots`, `cabbages`, `onions`, `tomatoes`, `rice`, `bell_peppers`,
`coffee`, `cotton`, `ender_crop`, `tobacco`, `flax`, `hemp`, `corn`, `cave_carrots`, `broccoli`, `cauliflower`, `garlic`,
`sweet_potato`, `turnip`, `zucchini`, `cucumber`, `oats`, `peanuts`, `adzuki`. Switching every crop on gives the three
soil columns of 2.1.

## 3. What the pack changes

`kubejs/data/creative_delight/kubejs/creativeengineering/soil_yield.js`, on `ServerEvents.genericLootTables` (the raw
table JSON before the game parses it, the same method as `rusticdelight/crop_drops.js`). For each crop switched on, for
each soil with tries above 0, one pool is appended:

- rolls: `minecraft:binomial` with n = tries, p = chance; each roll gives one produce item;
- conditions: the crop's ripe state (`block_state_property`) and `minecraft:location_check` with `offsetY` -1 (-2 for rice
  panicles and the hemp top) and the soil block.

The crop's own pools are not changed and pools are only appended (Forge names pools by position). Per-crop choices:

| Crop | Pool goes on | Why |
|---|---|---|
| Rice | the panicles, 2 above the soil | the panicles hold the harvest; the rice block stands in water on the soil. With a knife a roll gives Rice, as the table's own knife rule |
| Hemp | the top (`growth=top0`), 2 above the soil | players cut the top and the bottom regrows; breaking the bottom also breaks the top, so a plant gets its pool once |
| Corn | the lower half (`upper=false`) | both halves drop corn; one pool per plant |
| Flax | the lower half | only the lower half drops flax |
| Tomatoes | the bottom vine | a vine on a rope has no soil below it |
| Bell Pepper | red, green or yellow per roll, 1:1:1 | same colours as the main roll; the pools have 3 entries and no `random_chance`, so `crop_drops.js` never takes one for its own bonus pools |
| Oats | Oat, Raw Oats with a knife | as the table's own knife rule |

Key lists: crop blocks in `global.blocks` (block_dict.js "Crop blocks with a soil yield row"), produce and soils in
`global.item`. CEConfig `CropDrops.SoilYield` holds the soil values and one `BooleanValue` per crop
(`SoilYield.CROPS`, same keys as the script's table); the script reads them through `NCH.cfg` and `SoilYield.cropOn`.
A crop the script lists but the config lacks is logged and stays off.

Log line on every reload: `[soil-yield] no crop switched on: ...` or `[soil-yield] N crops (...), soils: rich farmland
2 x 0.35, ...`.

## 4. Planned: nether crops (outline, not built)

TODO `farming/crops #24`. Owner: outline only.

| Crop | Block | Table now (ripe, no Fortune) | Soils | Better soil |
|---|---|---|---|---|
| Nether Wart | `minecraft:nether_wart` | 2 to 4 Nether Wart (age 3) | Soul Sand, Nulch | Nulch (Fertile Soul Soil) |
| Ghost Pepper (Vintage port) | `vintagedelight:ghost_pepper_crop` | 1 Ghost Pepper (age 7); seeds bonus at age 3 only (F1) | `#vintagedelight:ghost_pepper_soil`: Soul Soil, Nulch, Rich Soul Soil | Nulch, Rich Soul Soil |
| Propelplant (Nether's Delight) | `nethersdelight:propelplant_*` | pearls by its own pick | nylium, netherrack, Rich Soul Soil | Rich Soul Soil |
| Fungus colonies (Nether's Delight) | `nethersdelight:crimson_fungus_colony`, `warped_fungus_colony` | fungi by shears / break | nylium, mycelium, Soul Soil, Rich Soul Soil, dirt | Rich Soul Soil (already grows them into colonies) |

Plan:

1. Same pools and script; a second soil list `[crop_drops.soil_yield.nether]`: `soulSandTries/Chance` and
   `soulSoilTries/Chance` (base, 0), `nulchTries/Chance`, `richSoulSoilTries/Chance`; crop switches `nether_wart`,
   `ghost_pepper`, `propelplant`, `crimson_fungus_colony`, `warped_fungus_colony` in the same `crops` section, all off.
2. Soil keys: `i.nulch` exists; add `i.soul_sand`/check `i.soul_soil`, and `b.rich_soul_soil` (block_dict, oven heat) or
   an item key; crop block keys `b.nether_wart`, `b.ghost_pepper_crop` (exists), the propelplant and colony blocks.
3. Decide per crop before building:
   - Nether Wart: pool on age 3; produce Nether Wart. Straightforward.
   - Ghost Pepper: its right-click is an own pick (crops.md 2.2), so the pool only applies to breaking, as for cucumber.
   - Propelplant: the pearls come from the berry cane's own pick, not its table; a table pool would only reach a broken
     berry cane. Needs a choice: table only, or a patcherMod hook on the pick (out of the data-only approach).
   - Fungus colonies: the colony grows on the soil but is harvested by shears for fungi; decide which age counts as
     ripe and whether shears and break both count.
4. Run the loot dump check (section 6) on the nether tables, then add the rows to 2.1 and tests SQ-SY-08 onwards.

## 5. Limits and findings

- **Own picks get nothing.** A crop picked by its own right-click (FD tomato vines on ropes, Vintage cucumber and ghost
  pepper, Veggies zucchini; crops.md 2.2) does not roll its table, so the soil has no effect on that pick. Breaking the
  crop and Quark's right-click replant do roll it.
- **One soil per harvest.** Only the block directly under the crop (or 2 under for rice and hemp) counts; there is no
  area effect.
- **No Fortune on the soil pools**, by choice: the extra stays a soil effect.
- **F1 (upstream, not changed):** Vintage's cucumber and ghost pepper tables put the seeds bonus on age 3 (not ripe), so a
  ripe one drops no seeds and a half-grown one drops 1 to 4. Left as the mod ships it; noted for `farming/crops #3`.
- **Cave carrots** are a `CropBlock`: farmland soils only (crops.md has no row for them yet).

## 6. Offline checks

2026-10-28:

- `./gradlew :patcherMod:build --offline` passes.
- Headless CEConfig spec test (javac harness, memory `patchermod-headless-smoke-test`): 27 crop switches, all `false`;
  one set `true` reads back `true`; an unknown key gives `null`; soil defaults 2 x 0.35 / 2 x 0.25; the bell pepper
  section still reads `enabled = true`, weight 1.
- Node harness over `soil_yield.js` with the real item and block dicts and every crop forced on: 27 tables, 3 pools each,
  no undefined item, block or soil id; the rice pool has the knife alternative and `offsetY` -2.

## 7. In-game tests (SY-)

Queued in [testing/scratch_queue.md](testing/scratch_queue.md) as SQ-SY-01 to SQ-SY-07 (SY-n = SQ-SY-n).

## 8. History

- 2026-10-28: built (owner, scratchpad "better soils increases extra yield chance"): overworld crops, off by default per
  crop; nether crops outlined only.
