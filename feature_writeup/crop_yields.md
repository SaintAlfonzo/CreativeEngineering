# Crop yields and soil yield

What every crop drops when harvested ripe, and the soil yield system: extra produce for crops grown on better soil.
Soils and harvesting mechanics: [crops.md](crops.md). Bell pepper colour weights: [crops.md](crops.md) section 3.

## 0. At a glance

| Part | Status | Where |
|---|---|---|
| Current drop yields, every overworld farmland crop | reference (loot dump 2026-10-26) | section 2.1 |
| Soil yield, overworld crops (26) | BUILT 2026-10-28; reworked 2026-11-03 to Rich +33% / Fertile Soil +66% of each crop's average, every crop on, Ender Crop out; Creative Delight jar in `mods/` 2026-11-03; SQ-SY-08 to -13 queued (the 10-28 PASS covered the old values) | section 2.4, 3, `creativeengineering/soil_yield.js` |
| Soil yield, nether crops | Planned, outline only (TODO `farming/crops #24`) | section 4 |

Config: `config/creativedelight-common.toml` `[crop_drops.soil_yield]` (moved from `creativeengineering-common.toml` in the Creative Split) (one bonus per soil) and
`[crop_drops.soil_yield.crops]` (one `true`/`false` per crop, all `true`). A change needs a restart: on the 2026-10-28 scratch run an edited toml
was not reread by the running server, and `/reload` kept the old switches.

Install: Creative Delight (CDConfig holds the section; jar in `mods/` 2026-11-03) and its scripts. Every crop is `true` in `config/` and
`configs_edited/` since 2026-11-03 (old `*Tries` / `*Chance` keys removed there; Forge drops them from an older toml). The
script only runs with Creative Delight loaded (`global.CD`).

## 1. Overview

Every crop's harvest comes from its loot table. Most ripe crops drop one produce item and a few seeds. Vanilla and most
Delight crops add a binomial bonus: 3 tries at 57% (`binomial_with_bonus_count`, extra 3, p 0.571), each Fortune level
adding one try. On carrots, potatoes, onions, peanuts, sweet potatoes and zucchini that bonus is on the produce; on
wheat, beetroot, cabbage, broccoli, cauliflower, turnip, corn and the seed crops it is on the seeds.

The soil under the crop changes growth speed only (Rich Soil's 20% bone meal chance, Fertile Soil's soaked boost, see
[crops.md](crops.md) 2.4). Out of the box no soil changes how much a crop drops. The soil yield system adds that.

Magic Peanut: the pack raises its chance on a ripe peanut harvest to 0.05% (Vintage ships 1%; 5% for a few hours on 2026-11-03), in the Vintage member's
`peanut_crop` table.

## 2. Reference

### 2.1 Current drop yields

Read from `kubejs/exported/loot_tables/` (dump of 2026-10-26, with the pack's bell pepper weights). Ripe harvest, broken
or right-click replanted, no Fortune. "Average produce" is the mean count of the produce item per ripe harvest; the
soil columns are that mean with the soil yield at the default values (section 2.4).
"can't grow" = the crop refuses that soil (untilled Rich Soil takes no farmland crop). "out" = excluded from soil yield.

| Crop | Switch (`crops`) | Type | Ripe harvest, no Fortune | Seeds | Average produce | Rich Farmland | Fertile Soil | Rich Soil |
|---|---|---|---|---|---|---|---|---|
| Wheat | `wheat` | single | 1 Wheat | 1 to 4 seeds (2.71) | 1.00 | 1.33 | 1.66 | can't grow |
| Carrots | `carrots` | multi | 2 to 5 Carrots | (the carrot is the seed) | 3.71 | 4.93 | 6.16 | can't grow |
| Potatoes | `potatoes` | multi | 2 to 5 Potatoes; 2% one Poisonous Potato | (the potato is the seed) | 3.71 | 4.93 | 6.16 | can't grow |
| Beetroots | `beetroots` | single | 1 Beetroot | 1 to 4 seeds (2.71) | 1.00 | 1.33 | 1.66 | can't grow |
| Cabbage | `cabbages` | single | 1 Cabbage | 1 to 4 seeds (2.71) | 1.00 | 1.33 | 1.66 | can't grow |
| Onion | `onions` | multi | 2 to 5 Onions | (the onion is the seed) | 3.71 | 4.93 | 6.16 | can't grow |
| Tomatoes (bottom vine) | `tomatoes` | multi | 1 to 2 Tomatoes; 5% one Rotten Tomato | 1 seed (not on a rope) | 1.50 | 2.00 | 2.49 | can't grow |
| Rice (panicles) | `rice` | single | 1 Rice Panicle (1 Rice with a knife); the bottom always drops 1 Rice | (rice is the seed) | 1.00 | 1.33 | 1.66 | 1.33 |
| Bell Pepper | `bell_peppers` | multi | 1 pepper (red, green, yellow 1:1:1), plus 15% each for one more of each colour | 1 to 2 seeds (1.5) | 1.45 | 1.93 | 2.41 | can't grow |
| Coffee (Rustic) | `coffee` | multi | 1 + 1 to 4 Coffee Beans | (the bean is the seed) | 3.50 | 4.66 | 5.81 | can't grow |
| Cotton | `cotton` | multi | 1 to 3 Cotton Bolls | 1 to 3 seeds (2) | 2.00 | 2.66 | 3.32 | can't grow |
| Ender Crop | `ender_crop` | n/a | 1 Ender Pearl, 10% one more | 1 seed, 10% one more (1.1) | 1.10 | out | out | out |
| Tobacco | `tobacco` | multi | 4 + 1 to 4 Tobacco Leaves (5 to 8) | 1 to 2 seeds (1.57) | 6.71 | 8.92 | 11.14 | can't grow |
| Flax (lower half) | `flax` | single | 1 Flax | 1 to 3 seeds (2.14) | 1.00 | 1.33 | 1.66 | can't grow |
| Hemp (top) | `hemp` | multi | top: 1 to 4 Hemp Fibre (2.88); grown bottom: 1 to 4 (2.5) | 1 seed from each half | 2.88 | 3.83 | 4.78 | can't grow |
| Corn (lower half) | `corn` | single | 1 Corn per half (2 per plant) | 1 to 4 seeds per half (2.71) | 1.00 | 1.33 | 1.66 | can't grow |
| Cave Carrots | `cave_carrots` | multi | 2 to 4 Cave Carrots | (the carrot is the seed) | 3.00 | 3.99 | 4.98 | can't grow |
| Broccoli | `broccoli` | single | 1 Broccoli | 1 to 4 seeds (2.71) | 1.00 | 1.33 | 1.66 | can't grow |
| Cauliflower | `cauliflower` | single | 1 Cauliflower | 1 to 4 seeds (2.71) | 1.00 | 1.33 | 1.66 | can't grow |
| Garlic | `garlic` | multi | 1 to 3 Garlic (2.14) | (unripe: 1 clove) | 2.14 | 2.85 | 3.55 | can't grow |
| Sweet Potato | `sweet_potato` | multi | 1 to 4 Sweet Potatoes (2.71) | (the potato is the seed) | 2.71 | 3.60 | 4.50 | can't grow |
| Turnip | `turnip` | single | 1 Turnip | 1 to 4 seeds (2.71) | 1.00 | 1.33 | 1.66 | can't grow |
| Zucchini | `zucchini` | multi | 1 to 4 Zucchini (2.71) | 1 seed | 2.71 | 3.60 | 4.50 | can't grow |
| Cucumber | `cucumber` | single | 1 Cucumber | none when ripe (section 5, F1) | 1.00 | 1.33 | 1.66 | can't grow |
| Oats | `oats` | single | 1 Oat (Raw Oats with a knife) | 1 seed (1 to 2 with a knife) | 1.00 | 1.33 | 1.66 | can't grow |
| Peanuts | `peanuts` | multi | 2 to 5 Peanuts; 0.05% one Magic Peanut (Vintage: 1%) | (the peanut is the seed) | 3.71 | 4.93 | 6.16 | can't grow |
| Adzuki | `adzuki` | multi | 2 to 3 Adzuki Beans (ripe and flowering) | (the bean is the seed) | 2.50 | 3.33 | 4.15 | 3.33 |

- Fortune: each level adds one try to a binomial bonus, and one item per level (random 0 to level) to a
  `uniform_bonus_count` bonus (Rustic's crops, FD tomatoes). The soil yield pools ignore Fortune.
- Plain farmland (and dirt for rice and adzuki) gives the "Average produce" column: `farmlandTries` is 0 by default.
- Ender Crop is out of the soil yield system (it only grows on tilled End Stone), so it has no soil columns.
- Other drops on top: FD's knife straw on wheat and rice, the corn knife Corncob (25%, corn_delight.md 2.4).
- Rustic rows: 1.3.0 and 1.7.0 have the same coffee, cotton and pepper tables (checked against the donor jar).

### 2.2 Soil values (defaults)

| Soil | Block | Config key | Bonus | Grows here |
|---|---|---|---|---|
| Farmland | `minecraft:farmland` | `farmlandBonus` | 0 | every farmland crop |
| Rich Farmland | `farmersdelight:rich_soil_farmland` | `richFarmlandBonus` | 0.33 | every farmland crop, rice, adzuki |
| Fertile Soil | `immersive_weathering:mulch_block` | `fertileSoilBonus` | 0.66 | everything but Nether plants |
| Rich Soil (untilled) | `farmersdelight:rich_soil` | `richSoilBonus` | 0.33 | rice and adzuki only (no farmland crop) |

Bonus = extra produce as a share of the crop's average (0.33 = about a third more). Range 0 to 4; 0 adds no pool. Until
2026-11-03 each soil was `tries` x `chance` flat extra items (Rich 2 x 0.35, Fertile Soil 2 x 0.25), the same for every crop.

### 2.3 Enabled and disabled

| Crop switch | Default | Effect |
|---|---|---|
| any key in `[crop_drops.soil_yield.crops]` | `true` | `false`: the crop's table is untouched, same drops on every soil (section 2.1 "Average produce"). `true`: one pool per soil with a bonus above 0 is appended to the crop's table |

Keys, in order: `wheat`, `carrots`, `potatoes`, `beetroots`, `cabbages`, `onions`, `tomatoes`, `rice`, `bell_peppers`,
`coffee`, `cotton`, `tobacco`, `flax`, `hemp`, `corn`, `cave_carrots`, `broccoli`, `cauliflower`, `garlic`,
`sweet_potato`, `turnip`, `zucchini`, `cucumber`, `oats`, `peanuts`, `adzuki`. Ender Crop has no key (out). Every crop on gives the three soil columns of 2.1.

### 2.4 Soil yield rules (decided and built 2026-11-03)

Rich soils raise a crop's mean yield by about a third, Fertile Soil by about two thirds (the reverse of the old order,
where Fertile Soil was the weaker soil).

| Soil | Mean yield vs plain farmland | Grows here |
|---|---|---|
| Farmland | x1.00 | every farmland crop |
| Rich Farmland | x1.33 | every farmland crop, rice, adzuki |
| Rich Soil (untilled) | x1.33 | rice and adzuki only |
| Fertile Soil | x1.66 | everything but Nether plants |

- Single harvest crops (Type `single`: a fixed one-item produce, such as wheat, cabbage): a 33% (Rich) or 66% (Fertile)
  chance of one extra harvest. Mean = 1.33 or 1.66.
- Multi harvest crops (Type `multi`: a variable count, such as carrots 2 to 5): the count is shifted up so the mean is
  base x 1.33 or x 1.66 (carrots 3.71 to 4.93 and 6.16). Bell pepper and tomatoes count as multi.
- Both are one formula (section 3): extra = bonus x base, rolled as binomial(ceil(extra), extra / ceil(extra)).
- Ender Crop is out (it only grows on tilled End Stone). Fortune is still ignored by the soil pools. Other drops
  (Poisonous Potato, Magic Peanut, knife extras, seeds) are not scaled.
- Table values in 2.1 are base mean x 1.33 and x 1.66, rounded to two places.

## 3. What the pack changes

`kubejs/data/creativedelight/kubejs/creativeengineering/soil_yield.js`, on `ServerEvents.genericLootTables` (the raw
table JSON before the game parses it, the same method as `rusticdelight/crop_drops.js`). For each crop switched on, for
each soil with a bonus above 0, one pool is appended:

- rolls: `minecraft:binomial` with extra = bonus x the crop's `base` (its average produce, 2.1, written in the script),
  n = ceil(extra), p = extra / n; each roll gives one produce item. Base 1 at 0.33 = binomial(1, 0.33); carrots at 0.66 =
  binomial(3, 0.816). A base must be re-read from the loot dump if a mod changes the crop's table;
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
`global.item`. CDConfig (Creative Delight; was CEConfig) `CropDrops.SoilYield` holds the soil values and one `BooleanValue` per crop
(`SoilYield.CROPS`, same keys as the script's table); the script reads them through `NCH.cfg` and `SoilYield.cropOn`.
A crop the script lists but the config lacks is logged and stays off.

Log line on every script load (boot and `/reload`; the switch values only change on a restart): `[soil-yield] no crop switched on: ...` or `[soil-yield] N crops (...), soils: rich farmland
+33%, fertile soil +66%, rich soil +33%`.

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

2026-11-03 (rework):

- `./gradlew :creativeDelight:build --offline` passes; the jar has `CDConfig$SoilYield` with the four `*Bonus` keys and the
  Vintage member's `peanut_crop` table at chance 0.0005.
- Node check of the script's `rolls()` over its 26 `base` values: every crop's farmland + pool mean matches the 2.1
  columns (wheat binomial(1, 0.33) / (1, 0.66); carrots (2, 0.6122) / (3, 0.8162); tobacco (3, 0.7381) / (5, 0.8857)).

2026-10-28:

- `./gradlew :creativeCommon:build --offline` passes.
- Headless CEConfig spec test (javac harness, memory `patchermod-headless-smoke-test`): 27 crop switches, all `false`;
  one set `true` reads back `true`; an unknown key gives `null`; soil defaults 2 x 0.35 / 2 x 0.25; the bell pepper
  section still reads `enabled = true`, weight 1.
- Node harness over `soil_yield.js` with the real item and block dicts and every crop forced on: 27 tables, 3 pools each,
  no undefined item, block or soil id; the rice pool has the knife alternative and `offsetY` -2.

## 7. In-game tests (SY-)

Run as SQ-SY-01 to SQ-SY-07 in [testing/scratch_queue.md](testing/scratch_queue.md) (SY-n = SQ-SY-n). All PASS on
2026-10-28 ([run log](testing/scratch_queue_run_2026-10-28.md)), measured with `tools/soil_yield_probe/sy_probe.js`
(`/syt roll`, 400 to 1500 loot rolls per case on placed blocks): wheat 1.00 / 1.71 / 1.49 per plant on farmland /
Rich Farmland / Fertile Soil, carrots 3.71 / 4.42 / 4.20, rice and knife rice extra on Rich Soil only, corn extra on
the lower half only, hemp from the top, flax from the lower half, bell peppers still 1:1:1, `richFarmlandTries = 0`
removes the Rich Farmland pool. Those values were replaced on 2026-11-03; the rework is SQ-SY-08 to -13 (queued).

## 8. History

- 2026-11-03: reworked (owner): per-soil bonus as a share of each crop's average (Rich Farmland and Rich Soil +33%,
  Fertile Soil +66%) replaces tries x chance; every crop switched on; Ender Crop out. Magic Peanut chance 1% to 0.05% (briefly 5% the same day).

- 2026-10-28 night: scratch server PASS (SQ-SY-01 to -07); config changes need a restart (doc corrected).
- 2026-10-28: built (owner, scratchpad "better soils increases extra yield chance"): overworld crops, off by default per
  crop; nether crops outlined only.
