# Crop yields and soil yield

What every crop drops when harvested ripe, and the soil yield system: extra produce for crops grown on better soil.
Soils and harvesting mechanics: [crops.md](crops.md). Bell pepper colour weights: [crops.md](crops.md) section 3.

## 0. At a glance

**README index entry, moved here 2026-11-16 (owner ruling D6).** Feature: Crop yields (every crop's ripe drops; soil yield: extra produce on Rich Farmland, Fertile Soil, Rich Soil and the soul soils, rounded bases, no Fortune on any crop, own picks and six bushes boosted, one switch per crop or bush, all on). Status: BUILT 2026-10-28, reworked 2026-11-03 (installed); wave 2 BUILT 2026-11-14 and installed; **r1115 2026-11-15: wave 2 SQ-SY-14 to -21, CQ-SY-22 built, scratch server PASSED**; adzuki replant open (`farming/crops #56`)

Second wave (rounded bases, no Fortune, own picks, bushes, nether soils, Botany Pots speed): [plan_crop_yields_wave2.md](plan_crop_yields_wave2.md), BUILT 2026-11-14 (jars built, not installed; SQ-SY-14 to -22 queued). Open: adzuki replant on Rich / Fertile Soil (plan 5.1).

| Part | Status | Where |
|---|---|---|
| Current drop yields, every overworld farmland crop | reference (loot dump 2026-10-26) | section 2.1 |
| Soil yield, overworld crops (26) | BUILT 2026-10-28; reworked 2026-11-03 to Rich +33% / Fertile Soil +66% of each crop's average, every crop on, Ender Crop out; Creative Delight jar in `mods/` 2026-11-03; SQ-SY-08 to -13 queued (the 10-28 PASS covered the old values) | section 2.4, 3, `creativedelight/soil_yield.js` |
| Wave 2: rounded bases, Fortune stripped from 77 crop tables, ripe seeds on cucumber and ghost pepper | BUILT 2026-11-14, not installed; SQ-SY-14, -15, -18 | sections 2.4, 3, 4 |
| Wave 2: own picks (tomato vines, cucumber, ghost pepper, zucchini, 6 bushes, propelplant, colony shearing) | BUILT 2026-11-14, not installed; SQ-SY-16, -17, -19 | section 4 |
| Wave 2: bushes (6) and nether crops (5) on the soil list | BUILT 2026-11-14, not installed; SQ-SY-17, -19, -20 | sections 2.2, 2.5, 4 |

Config: `config/creativedelight-common.toml` `[crop_drops.soil_yield]` (moved from `creativeengineering-common.toml` in the Creative Split) (one bonus per soil),
`[crop_drops.soil_yield.crops]` (one `true`/`false` per crop, all `true`) and `[crop_drops.soil_yield.bushes]` (six bushes, all `true`). A change needs a restart: on the 2026-10-28 scratch run an edited toml
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
| Carrots | `carrots` | multi | 2 to 5 Carrots | (the carrot is the seed) | 3.71 (base 3.5) | 4.87 | 6.02 | can't grow |
| Potatoes | `potatoes` | multi | 2 to 5 Potatoes; 2% one Poisonous Potato | (the potato is the seed) | 3.71 (base 3.5) | 4.87 | 6.02 | can't grow |
| Beetroots | `beetroots` | single | 1 Beetroot | 1 to 4 seeds (2.71) | 1.00 | 1.33 | 1.66 | can't grow |
| Cabbage | `cabbages` | single | 1 Cabbage | 1 to 4 seeds (2.71) | 1.00 | 1.33 | 1.66 | can't grow |
| Onion | `onions` | multi | 2 to 5 Onions | (the onion is the seed) | 3.71 (base 3.5) | 4.87 | 6.02 | can't grow |
| Tomatoes (bottom vine) | `tomatoes` | multi | 1 to 2 Tomatoes; 5% one Rotten Tomato | 1 seed (not on a rope) | 1.50 (base 1.5) | 2.00 | 2.49 | can't grow |
| Rice (panicles) | `rice` | single | 1 Rice Panicle (1 Rice with a knife); the bottom always drops 1 Rice | (rice is the seed) | 1.00 | 1.33 | 1.66 | 1.33 |
| Bell Pepper | `bell_peppers` | multi | 1 pepper (red, green, yellow 1:1:1), plus 15% each for one more of each colour | 1 to 2 seeds (1.5) | 1.45 (base 1.5) | 1.94 | 2.44 | can't grow |
| Coffee (Rustic) | `coffee` | multi | 1 + 1 to 4 Coffee Beans | (the bean is the seed) | 3.50 (base 3.5) | 4.66 | 5.81 | can't grow |
| Cotton | `cotton` | multi | 1 to 3 Cotton Bolls | 1 to 3 seeds (2) | 2.00 | 2.66 | 3.32 | can't grow |
| Ender Crop | `ender_crop` | n/a | 1 Ender Pearl, 10% one more | 1 seed, 10% one more (1.1) | 1.10 | out | out | out |
| Tobacco | `tobacco` | multi | 4 + 1 to 4 Tobacco Leaves (5 to 8) | 1 to 2 seeds (1.57) | 6.71 (base 6.5) | 8.86 | 11.00 | can't grow |
| Flax (lower half) | `flax` | single | 1 Flax | 1 to 3 seeds (2.14) | 1.00 | 1.33 | 1.66 | can't grow |
| Hemp (top) | `hemp` | multi | top: 1 to 4 Hemp Fibre (2.88); grown bottom: 1 to 4 (2.5) | 1 seed from each half | 2.88 (base 3) | 3.87 | 4.86 | can't grow |
| Corn (lower half) | `corn` | single | 1 Corn per half (2 per plant) | 1 to 4 seeds per half (2.71) | 1.00 | 1.33 | 1.66 | can't grow |
| Cave Carrots | `cave_carrots` | multi | 2 to 4 Cave Carrots | (the carrot is the seed) | 3.00 | 3.99 | 4.98 | can't grow |
| Broccoli | `broccoli` | single | 1 Broccoli | 1 to 4 seeds (2.71) | 1.00 | 1.33 | 1.66 | can't grow |
| Cauliflower | `cauliflower` | single | 1 Cauliflower | 1 to 4 seeds (2.71) | 1.00 | 1.33 | 1.66 | can't grow |
| Garlic | `garlic` | multi | 1 to 3 Garlic (2.14) | (unripe: 1 clove) | 2.14 (base 2) | 2.80 | 3.46 | can't grow |
| Sweet Potato | `sweet_potato` | multi | 1 to 4 Sweet Potatoes (2.71) | (the potato is the seed) | 2.71 (base 2.5) | 3.54 | 4.36 | can't grow |
| Turnip | `turnip` | single | 1 Turnip | 1 to 4 seeds (2.71) | 1.00 | 1.33 | 1.66 | can't grow |
| Zucchini | `zucchini` | multi | 1 to 4 Zucchini (2.71) | 1 seed | 2.71 (base 2.5) | 3.54 | 4.36 | can't grow |
| Cucumber | `cucumber` | single | 1 Cucumber | 1 to 4 seeds (2.71) on a ripe break since wave 2 (G); the own pick drops none | 1.00 | 1.33 | 1.66 | can't grow |
| Oats | `oats` | single | 1 Oat (Raw Oats with a knife) | 1 seed (1 to 2 with a knife) | 1.00 | 1.33 | 1.66 | can't grow |
| Peanuts | `peanuts` | multi | 2 to 5 Peanuts; 0.05% one Magic Peanut (Vintage: 1%) | (the peanut is the seed) | 3.71 (base 3.5) | 4.87 | 6.02 | can't grow |
| Adzuki | `adzuki` | multi | 2 to 3 Adzuki Beans (ripe and flowering) | (the bean is the seed) | 2.50 (base 2.5) | 3.33 | 4.15 | 3.33 |

- Fortune: as the mods ship them, each level adds one try to a binomial bonus, and one item per level (random 0 to
  level) to a `uniform_bonus_count` bonus (Rustic's crops, FD tomatoes). Since wave 2 the pack strips both from every
  crop table (section 3), so the columns hold with any tool.
- Plain farmland (and dirt for rice and adzuki) gives the "Average produce" column: `farmlandBonus` is 0 by default.
  "(base x)" = the rounded base the soil extra is sized on, where it differs from the mean.
- Ender Crop is out of the soil yield system (it only grows on tilled End Stone), so it has no soil columns.
- Other drops on top: FD's knife straw on wheat and rice, the corn knife Corncob (25%, corn_delight.md 2.4).
- Rustic rows: 1.3.0 and 1.7.0 have the same coffee, cotton and pepper tables (checked against the donor jar).

### 2.2 Soil values (defaults)

| Soil | Block | Config key | Bonus | Grows here |
|---|---|---|---|---|
| Farmland | `minecraft:farmland` | `farmlandBonus` | 0 | every farmland crop |
| Rich Farmland | `farmersdelight:rich_soil_farmland` | `richFarmlandBonus` | 0.33 | every farmland crop, rice, adzuki |
| Fertile Soil | `immersive_weathering:mulch_block` | `fertileSoilBonus` | 0.66 | everything but Nether plants |
| Rich Soil (untilled) | `farmersdelight:rich_soil` | `richSoilBonus` | 0.33 | rice, adzuki and the bushes (no farmland crop) |
| Rich Soul Soil | `nethersdelight:rich_soul_soil` | `richSoulSoilBonus` | 0.33 | ghost pepper, propelplant, fungus colonies (not nether wart) |
| Fertile Soul Soil (Nulch) | `immersive_weathering:nulch_block` | `fertileSoulSoilBonus` | 0.66 | nether wart, ghost pepper (not propelplant, not colonies) |

Rich Soil and Rich Farmland are equal on purpose (owner 2026-11-14); the two keys stay separate. The nether base soils
(Soul Sand for wart, Soul Soil for ghost pepper, netherrack or nylium for propelplant) give no bonus.

Bonus = extra produce as a share of the crop's average (0.33 = about a third more). Range 0 to 4; 0 adds no pool. Until
2026-11-03 each soil was `tries` x `chance` flat extra items (Rich 2 x 0.35, Fertile Soil 2 x 0.25), the same for every crop.

### 2.3 Enabled and disabled

| Crop switch | Default | Effect |
|---|---|---|
| any key in `[crop_drops.soil_yield.crops]` | `true` | `false`: the crop's table is untouched, same drops on every soil (section 2.1 "Average produce"). `true`: one pool per soil with a bonus above 0 is appended to the crop's table |

Keys, in order: `wheat`, `carrots`, `potatoes`, `beetroots`, `cabbages`, `onions`, `tomatoes`, `rice`, `bell_peppers`,
`coffee`, `cotton`, `tobacco`, `flax`, `hemp`, `corn`, `cave_carrots`, `broccoli`, `cauliflower`, `garlic`,
`sweet_potato`, `turnip`, `zucchini`, `cucumber`, `oats`, `peanuts`, `adzuki`, then the nether keys `nether_wart`,
`ghost_pepper`, `propelplant`, `crimson_fungus_colony`, `warped_fungus_colony`. Bushes, `[crop_drops.soil_yield.bushes]`:
`strawberry`, `blueberry`, `cranberry`, `lemon`, `sweet_berry`, `salmonberry`. A crop's switch covers its break and its
own pick. Ender Crop has no key (out). Every crop on gives the three soil columns of 2.1.

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
  mean + 0.33 or 0.66 x the rounded base (carrots 3.71 to 4.87 and 6.02). Bell pepper and tomatoes count as multi.
- **Rounded base (wave 2, owner):** the base is the crop's mean rounded to the nearest 0.5 (carrots 3.71 -> 3.5,
  tobacco 6.71 -> 6.5); the soil extra is bonus x that base. Plain farmland drops stay as the mods ship them (Q1).
- Both are one formula (section 3): extra = bonus x base, rolled as binomial(ceil(extra), extra / ceil(extra)).
- Ender Crop is out (it only grows on tilled End Stone). **Fortune never changes a crop's yield** (wave 2 part A):
  the soil pools and the own-pick hook never read it, and the crops' own Fortune terms are stripped (section 3). Other drops
  (Poisonous Potato, Magic Peanut, knife extras, seeds) are not scaled.
- Table values in 2.1 are mean + bonus x rounded base, rounded to two places (from `tools/soil_yield_check.js`).

### 2.5 Bushes and nether crops (wave 2)

Break = the loot pool (`soil_yield.js`); pick = the own right-click (or shear) through the pick mixins. Each path has its
own base (rounded mean of what that path drops). Columns: base soil / Rich / Fertile (nether: base / Rich Soul Soil /
Fertile Soul Soil).

| Plant | Switch | Break (ripe) | Break means | Pick | Pick base |
|---|---|---|---|---|---|
| Sweet berries | `bushes.sweet_berry` | 2 to 3 berries, age 3 | 2.50 / 3.33 / 4.15 | age 3: 2 to 3 (age 2 pick of 1 to 2: no extra) | 2.5 |
| Strawberries | `bushes.strawberry` | 1 red or white, age 6 | 1.00 / 1.33 / 1.66 | 1 to 2 | 1.5 |
| Blueberries | `bushes.blueberry` | 1, age 4 | 1.00 / 1.33 / 1.66 | 1 to 2, age 4 | 1.5 |
| Cranberries | `bushes.cranberry` | 1, age 4 | 1.00 / 1.33 / 1.66 | 1 to 2, age 4 | 1.5 |
| Lemon tree | `bushes.lemon` | 2 to 5, age 4, lower half only | 3.50 / 4.66 / 5.81 | 2 to 5 (`LemonTreePickYieldMixin`) | 3.5 |
| Salmonberries | `bushes.salmonberry` | pips only (no berries) | none | age 4: 2 to 3 (age 3 pick of 1: no extra) | 2.5 |
| Nether Wart | `crops.nether_wart` | 2 to 4, age 3 | 3.00 / (no Rich Soul Soil) / 4.98 | none | - |
| Ghost Pepper | `crops.ghost_pepper` | 1, age 7 | 1.00 / 1.33 / 1.66 | 1 at stage 7 (back to 5) | 1.0 |
| Propelplant | `crops.propelplant` | cane only (knife) | none | 1 to 2 pearls (berry cane or berry stem); soil under the lowest stem | 1.5 |
| Crimson / Warped Fungus Colony | `crops.*_fungus_colony` | 5 fungi, age 3 (not with shears) | 5.00 / 6.65 / (no Nulch) | shears: 1 fungus per cut | 1.0 |

Colonies only age on Rich Soul Soil (Nether's Delight 4.0 rule), so in practice every grown colony gets the +33%; Nulch
is not in their placement or growth rule, so the Fertile Soul Soil pool never fires for them. Mint, grape bushes, tea
and the flower bushes are out (plan 5.2).

## 3. What the pack changes

`kubejs/data/creativedelight/kubejs/creativedelight/soil_yield.js`, on `ServerEvents.genericLootTables` (the raw
table JSON before the game parses it, the same method as `rusticdelight/crop_drops.js`). For each crop switched on, for
each soil with a bonus above 0, one pool is appended:

- rolls: `minecraft:binomial` with extra = bonus x the crop's base (its average produce rounded to 0.5, 2.1), n =
  ceil(extra), p = extra / n; each roll gives one produce item. Base 1 at 0.33 = binomial(1, 0.33); carrots (base 3.5)
  at 0.66 = binomial(3, 0.77). The bases live in Creative Delight's `src/main/resources/creativedelight/soil_yield.json`
  (`loot`), read through `crops.SoilYieldPicks.lootBase`; the pick hook reads the same file (`picks`), so the two paths
  cannot disagree. `node tools/soil_yield_check.js` fails when a base is more than 0.5 off the loot dump's mean (a mod
  changed its table);
- conditions: the crop's ripe state (`block_state_property`) and `minecraft:location_check` with `offsetY` -1 (-2 for rice
  panicles and the hemp top) and the soil block.

The crop's own pools are not changed (apart from the Fortune strip) and pools are only appended (Forge names pools by
position). Nether crops get the two soul soil pools, the rest the four overworld ones.

The same event, before the soil pools:

- **Fortune strip (part A).** Every table in `soil_yield.json` `fortune_free` (77 crop, bush and wild crop tables from
  the 2026-11-14 audit; melon and hamimelon left out by the owner, cantaloupe and mini melon have no Fortune) loses its
  Fortune terms: `apply_bonus` `binomial_with_bonus_count` (extra, p) becomes `set_count` binomial(extra, p) with `add`
  (the Fortune-0 roll it already was, the function's own conditions kept, as nether wart's), `uniform_bonus_count` is
  dropped. A Fortune tool now gives the same as none; the means at Fortune 0 are unchanged (checked per table and
  age by the offline check). Not in the list (owner call): fruit-bearing leaves, IW weeds, truffle dirt.
- **Ripe seeds (part G).** Vintage's cucumber and ghost pepper get one more pool: on age 7, 1 + binomial(3, 0.5714)
  seeds (1 to 4, mean 2.71), the same count their age-3 pool gives; that pool is unchanged.

Per-crop choices:

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

## 4. Own picks and nether crops (wave 2, built 2026-11-14)

Replaces the 2026-10-28 nether outline (tries / chance keys, all off), dropped on 2026-11-14 for the owner's rules in
[plan_crop_yields_wave2.md](plan_crop_yields_wave2.md) 4 and 6.

**Own picks.** A pick that drops its produce itself never rolls a loot table. Each pick block's `use` (SRG `m_6227_`)
drops through `Block.popResource` (`m_49840_`); a `@ModifyArg` on that call hands the stack to
`crops.SoilYieldPicks.withSoilExtra(level, pos, stack)`, which looks up the block at `pos` in `soil_yield.json` `picks`,
checks the ripe state where the pick also runs unripe, the switch, walks down through the row's `below` blocks, reads
the soil's bonus and grows the stack by binomial(ceil(extra), extra / ceil(extra)), extra = bonus x the pick's base.
Server side only; Fortune is never read. One mixin per mod package (the CD mixin plugin gates a package on its mod):

| Mixin (`mixin.<pkg>.`) | Targets | Pick | Soil |
|---|---|---|---|
| `vintagedelight.VintagePickSoilYieldMixin` | `CucumberBlock`, `GhostPepperBlock` | stage 7: cucumber 1 to 2 (base 1.5), ghost pepper 1 (1.0) | below |
| `veggiesdelight.VeggiesPickSoilYieldMixin` | `ZucchiniCropBlock` | stage 7: 1 to 2 (1.5) | below |
| `farmersdelight.TomatoPickSoilYieldMixin` | `TomatoVineBlock` (also Supplementaries' stick and rope tomatoes, which inherit its `use`) | 1 to 2 tomatoes (1.5), ordinal 0 only (the 5% rotten tomato gets nothing) | under the bottom vine (walk through `tomatoes`, `stick_tomatoes`, `rope_tomatoes`) |
| `neapolitan.StrawberryPickSoilYieldMixin` | `StrawberryBushBlock` | 1 to 2 red or white (1.5) | below |
| `fruitsdelight.FruitBushPickSoilYieldMixin` | `FruitBushBlock` (blueberry, cranberry rows only) | 1 to 2 (1.5) | below |
| `fruitsdelight.LemonTreePickYieldMixin` (second handler) | `DoubleFruitBushBlock` (lemon row only) | 2 to 5 (3.5) | under the lower half |
| `delightful.SalmonberryPickSoilYieldMixin` | `SalmonberryBushBlock` | age 4 only: 2 to 3 (2.5) | below |
| `nethersdelight.NethersPickSoilYieldMixin` | `PropelplantBerryCaneBlock`, `PropelplantBerryStemBlock`, `FungusColonyBlock` | pearls 1 to 2 (1.5); shear 1 fungus (1.0) | propelplant: under the lowest cane or stem; colony: below |
| `minecraft.SweetBerryPickSoilYieldMixin` | `SweetBerryBushBlock` | age 3 only: 2 to 3 (2.5) | below |

The owner-less `m_49840_` target is deliberate: javac names the calling class as the owner of an unqualified inherited
static call (`CucumberBlock.m_49840_`), so a `Block`-owned target would miss.

**Ghost pepper pick (plan F).** The class has no bug: the pick drops 1 pepper at stage 7, returns it to stage 5, and the
crop holds only on `#vintagedelight:ghost_pepper_soil`. What it lacked was the soil bonus, now given by the hook.

**Nether soil audit (2026-11-14, read from the classes and tag dump).**

| Crop | Base soil (no bonus) | Rich Soul Soil | Fertile Soul Soil (Nulch) |
|---|---|---|---|
| Nether Wart | Soul Sand | refused (vanilla `mayPlaceOn`) | accepted, +66% |
| Ghost Pepper | Soul Soil | +33% | +66% |
| Propelplant | netherrack, `#nylium` | +33% | refused (needs code, not a tag) |
| Fungus colonies | none: they age only on Rich Soul Soil | +33% (their only growth soil) | not placeable |

## 5. Limits and findings

- **Own picks** get the soil extra through the pick mixins since wave 2 (section 4). A pick of another mod not in
  `soil_yield.json` `picks` (mint, grapes, tea, flower bushes) gets nothing. Rice panicles' right-click is Quark's
  replant, which rolls the table.
- **One soil per harvest.** Only the block directly under the crop (or 2 under for rice and hemp) counts; there is no
  area effect.
- **No Fortune anywhere in crop yield** (wave 2 part A): soil pools, pick hook and the stripped crop tables.
- **F1 (upstream):** Vintage's cucumber and ghost pepper tables put the seeds bonus on age 3 of an age-7 crop. The age-3
  pool stays (a half-grown break drops 2 to 5 seeds at Fortune 0); a ripe break now also drops 1 to 4 (part G).
- **Cave carrots (plan L, 2026-11-14):** Miner's Delight member `CaveCarrotBlock` is a plain `CropBlock`: farmland only
  (vanilla farmland, Rich Farmland, Fertile Soil; not Rich Soil, stone or dirt); survives at light 8 or sky, grows at
  light 9 or more, no cave exemption. Ripe 2 to 4 (3.0), no Fortune. Wild cave carrots (`WildCaveCarrotBlock`) sit on
  stone, deepslate or dirt in caves (Creative Worldgen `packs/miners_delight`). Kept in the soil list; no cave soil to add.
- **Adzuki (plan 5.1):** a mature break turns only Dirt, Coarse Dirt or Grass into Adzuki Soil; Rich Soil, Rich Farmland
  and Fertile Soil stay and need replanting. Open owner call: a replant mixin so they also regrow, or keep as is.
- **Botany Pots** never get soil yield (pools fire on world blocks only); their soils only change growth speed
  ([botany_pots.md](botany_pots.md)).

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

2026-11-14 (wave 2):

- `./gradlew :creativeDelight:build --offline` and `:creativeCommon:build --offline` pass. The CD jar has
  `crops/SoilYieldPicks`, the eight `*PickSoilYieldMixin` classes and `creativedelight/soil_yield.json`; the helper's
  calls are reobfuscated to SRG.
- `node tools/soil_yield_check.js` (new; runs the real script against the loot dump with the real dicts, every switch
  on): all PASS. 36 crop rows resolve; every base within 0.5 of the dump mean; Fortune gone from 76 of the 77
  `fortune_free` tables (`hearthandharvest:blocks/hops_vine` is newer than the 11-13 dump) with the same Fortune-0 mean
  per item and age; melon and hamimelon untouched; ripe cucumber and ghost pepper seeds 2.71. The 2.1 and 2.5 means are
  its output.
- Not checked offline: the pick mixins apply (needs a boot; `require = 1` stops the game loading if a target method
  or call is missing).

## 7. In-game tests (SY-)

Scratch run r1115 (2026-11-15, [scratch_run_2026-11-15.md](testing/scratch_run_2026-11-15.md) 8): wave 2 SQ-SY-14 to -21 PASS (`35 crops`, row fixed; sweet potato ripe at `age=5`), CQ-SY-22 PASS (soil tooltips +10 % / +20 %).

Run as SQ-SY-01 to SQ-SY-07 in [testing/scratch_queue.md](testing/scratch_queue.md) (SY-n = SQ-SY-n). All PASS on
2026-10-28 ([run log](testing/scratch_queue_run_2026-10-28.md)), measured with `tools/soil_yield_probe/sy_probe.js`
(`/syt roll`, 400 to 1500 loot rolls per case on placed blocks): wheat 1.00 / 1.71 / 1.49 per plant on farmland /
Rich Farmland / Fertile Soil, carrots 3.71 / 4.42 / 4.20, rice and knife rice extra on Rich Soil only, corn extra on
the lower half only, hemp from the top, flax from the lower half, bell peppers still 1:1:1, `richFarmlandTries = 0`
removes the Rich Farmland pool. Those values were replaced on 2026-11-03; the rework is SQ-SY-08 to -13 (queued).
Wave 2 is SQ-SY-14 to -22 (queued 2026-11-14; run after -08 to -13).

## 8. History

- 2026-11-14: wave 2 built ([plan_crop_yields_wave2.md](plan_crop_yields_wave2.md)): bases rounded to 0.5 and moved to
  `soil_yield.json`; Fortune stripped from 77 crop tables; ripe seeds on cucumber and ghost pepper; own-pick soil extra
  (9 mixins); six bushes and five nether crops on the soil list with Rich Soul Soil +33% and Fertile Soul Soil +66%;
  Botany Pots Rich x1.10 (Rich Farmland was x1.25), Fertile Soil x1.20 (new pot soil). Jars built, not installed.

- 2026-11-03: reworked (owner): per-soil bonus as a share of each crop's average (Rich Farmland and Rich Soil +33%,
  Fertile Soil +66%) replaces tries x chance; every crop switched on; Ender Crop out. Magic Peanut chance 1% to 0.05% (briefly 5% the same day).

- 2026-10-28 night: scratch server PASS (SQ-SY-01 to -07); config changes need a restart (doc corrected).
- 2026-10-28: built (owner, scratchpad "better soils increases extra yield chance"): overworld crops, off by default per
  crop; nether crops outlined only.

## Owner notes (scratchpad 2026-11-07)

- Tilled End Stone integrates with all End crops (Ender Crop is the only one tied to it). Row `farming/crops #45`.
