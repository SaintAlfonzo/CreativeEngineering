# Crops: soils, right-click harvest, hoe harvesting

Test prefix **CR-** · Last updated **2026-10-02** · Overall status **reference from the decompiled jars, in-game checks pending**

## 0. At a glance

| Part | Where | Status |
|---|---|---|
| Per-crop table: type, soils, right-click, hoe right-click, hoe break | §2.1 | reference; CR-01 to CR-20 check the surprising parts |
| How right-click replanting works (Quark Simple Harvest) | §2.2 | reference |
| How the hoe area mechanics work (Quark Hoe Harvesting + Simple Harvest) | §2.3 | reference |
| Soil rules (Dirt, Farmland, Rich Soil, Rich Farmland, Fertile Soil, Nulch) | §2.4 | reference |
| Pack changes to farming | §3 | DEPLOYED (configs and scripts as they are today) |
| Gaps: tobacco, hemp, grape bushes, weeds, pack hoes | §4 | open, nothing changed yet |

- **Open first:** §2.1 for "can I plant X on Y" or "does X replant on right-click". §2.2 and §2.3 explain why.
- **Knobs:** `config/quark-common.toml` sections `[tweaks.simple_harvest]` and `[tweaks.hoe_harvesting]`.
  The pack runs Quark's defaults: empty-hand harvest on, no durability cost, harvesting search on,
  villagers use simple harvest, hoe radius 2 (3x3) and 3 (5x5).
- **Why tobacco is different:** it isn't a crop class at all. It's a plain MCreator block with a
  block entity, and its placement check is hard-coded to `minecraft:farmland` (§2.5).
- **How to re-read the code:** `python3 tools/decompile_mod.py <jar> <out dir>` (§6).

## 1. Overview

Three separate systems decide how a crop behaves:

1. **The crop's own block class** decides where it can be placed and survive, how it grows, and
   whether it has its own right-click picking (berry bushes, tomatoes, grapes).
2. **The soil block** gets a say through Forge's `canSustainPlant`. Farmer's Delight Rich Soil and Rich
   Farmland, and Immersive Weathering Mulch (renamed **Fertile Soil** in the pack), each override it
   with their own rules (§2.4). A crop that skips `canSustainPlant` and checks the block below directly
   (tobacco, grape bushes) ignores those overrides.
3. **Quark** adds the harvesting conveniences. *Simple Harvest* turns a right-click on a ripe crop into
   "drop the harvest and replant". *Hoe Harvesting* makes a hoe work on an area, for right-clicks and
   for breaking.

Words used in the table:

- **Quark replant**: Quark handles the right-click. It drops the block's normal loot as if broken
  (fortune from your held item applies), takes one seed out of the drops, and puts the crop back at
  its first stage.
- **Own pick**: the block's own right-click. A bush drops fruit and drops back to an earlier stage.
  Quark isn't involved unless you're holding a hoe.
- **none**: right-click does nothing; break it to harvest.

## 2. Reference

### 2.1 Per-crop table

Soils: **Dirt** = dirt, grass and the rest of `#minecraft:dirt`. **Farmland** = vanilla farmland.
**Rich** = Rich Soil (untilled). **RichF** = Rich Soil Farmland. **Fertile** = Fertile Soil
(`immersive_weathering:mulch_block`).

Hoe columns: **Hoe RC** = right-click while holding a hoe (3x3 or 5x5, §2.3). **Hoe break** = whether
the plant is caught by the area break when you break a plant with a hoe (breaks it at any age, no replant).

**Standard crops (one block, grows in place on farmland)**

| Crop | Block | Dirt | Farmland | Rich | RichF | Fertile | Right-click | Hoe RC | Hoe break |
|---|---|---|---|---|---|---|---|---|---|
| Wheat, Carrots, Potatoes, Beetroots | `minecraft:wheat` etc. | no | yes | no | yes | yes | Quark replant (config list) | replant | yes |
| Cabbage | `farmersdelight:cabbages` | no | yes | no | yes | yes | Quark replant (auto-found) | replant | yes |
| Onion | `farmersdelight:onions` | no | yes | no | yes | yes | Quark replant (auto-found) | replant | yes |
| Bell Pepper, Coffee, Cotton | `rusticdelight:bell_peppers`, `coffee`, `cotton` | no | yes | no | yes | yes | Quark replant (auto-found) | replant | yes |
| Ender Crop | `endercrop:ender_crop` | no | yes, dark only | no | yes, dark only | yes, dark only | Quark replant (auto-found) | replant | yes |
| **Tobacco** | `the_dirty_stuff:tobacco_seeds` | no | **yes** | no | **no** | **no** | **none** | **no** | **no** |

- Ender Crop off end stone needs raw light 7 or less at the crop, so a roofed, unlit farm. On
  Ender Crop's Tilled End Stone it needs light 8 or more instead.
- Adzuki (`neapolitan:adzuki_sprouts`) behaves like a bush for soil (Dirt, Farmland, Rich, RichF and Fertile all
  yes, plus Adzuki Soil) but has no right-click. Break it when grown. Hoe RC no, hoe break yes.

**Two-high crops**

| Crop | Block | Dirt | Farmland | Rich | RichF | Fertile | Right-click | Hoe RC | Hoe break |
|---|---|---|---|---|---|---|---|---|---|
| Flax | `supplementaries:flax` | no | yes | no | yes | yes | Quark replant, **lower half only** | replant | yes, same height only |
| Hemp | `immersiveengineering:hemp` | no | yes | no | yes | yes | none (break the top, the bottom regrows it) | no | yes, same height only |
| Tomatoes (climb ropes) | `farmersdelight:budding_tomatoes` then `farmersdelight:tomatoes` | no | yes | no | yes | yes | bottom vine: Quark replant. Vine on a rope: own pick | bottom vines only | yes |

- **Flax:** the top half appears from stage 4. Right-click the lower half, or the farmland under it.
  Clicking the top half does nothing (§4, G9). The replant resets the lower half and the top half disappears.
- **Hemp:** the top grows once the bottom is fully grown. Breaking a top with a hoe also breaks the
  other tops at that height in the hoe's area and leaves the bottoms, so a hoe works as a hemp
  harvester if you break the top row. Breaking a bottom with a hoe breaks the bottoms around it
  and you lose those plants.
- **Tomatoes:** both resets go to stage 0, so the result is the same. Tomatoes on Supplementaries sticks and ropes
  (`supplementaries:stick_tomatoes`, `rope_tomatoes`) use the same tomato code and the same own pick.

**Water crop**

| Crop | Block | Dirt | Farmland | Rich | RichF | Fertile | Right-click | Hoe RC | Hoe break |
|---|---|---|---|---|---|---|---|---|---|
| Rice | `farmersdelight:rice`, top `farmersdelight:rice_panicles` | yes | yes | yes | yes | yes | Quark replant on the panicles (top) | replant | yes |

- Rice must sit in a full water source block, one deep, on top of the soil. The panicles grow on top
  once the bottom is ripe. Right-click the panicles. Clicking the bottom also works, because Quark
  checks the block above when the clicked block isn't harvestable.

**Stems (melon-like)**

| Crop | Block | Dirt | Farmland | Rich | RichF | Fertile | Right-click | Hoe RC | Hoe break |
|---|---|---|---|---|---|---|---|---|---|
| Pumpkin, Melon stems | `minecraft:pumpkin_stem`, `melon_stem` | no | yes | no | yes | yes | none (break the fruit) | no | **yes, stems too** |

- The fruit can land on any of the five soils next to the stem, including plain dirt and grass.
- Breaking a neighbouring crop with a hoe also breaks stems in the area. Keep stems out of hoe range,
  or break crops near them by hand.

**Bushes (picked, then regrow)**

| Crop | Block | Dirt | Farmland | Rich | RichF | Fertile | Right-click | Hoe RC | Hoe break |
|---|---|---|---|---|---|---|---|---|---|
| Sweet Berries (plant with Sweet Berry Pips) | `minecraft:sweet_berry_bush` | yes | yes | yes | yes | yes | own pick (back to stage 1) | pick | yes |
| Strawberries (plant with Strawberry Pips) | `neapolitan:strawberry_bush` | yes | yes | yes | yes | yes | own pick at stage 6 (back to 1) | pick | yes |
| Salmonberries (plant with Salmonberry Pips) | `delightful:salmonberry_bush` | yes | yes | yes | yes | yes | own pick from stage 3 (back to 1) | pick | yes |
| Mint (plant with Mint Sprout) | `neapolitan:mint` | yes | yes | yes | yes | yes | own pick when grown (back to 1) | pick | yes |
| Grape bushes: red, white, savanna, taiga | `vinery:red_grape_bush` etc. | yes | **no** | yes | **no** | probably yes, CR-13 | own pick from stage 2 (back to 1) | pick | yes |
| Flower bushes (Floral Enchantment, 17 kinds) | `floralench:*_bush` | yes | yes | yes | yes | yes | own pick: stage 2 flowers, stage 3 vine items | pick | yes |

- Grape bushes need a full opaque block below plus their own light rules. See [vinery.md](vinery.md) §2.6.
  Farmland and Rich Farmland are not full blocks. Fertile Soil has a full-cube shape, but it isn't
  confirmed to count as opaque (CR-13).
- "Pick" in the Hoe RC column means Quark clicks every bush in the hoe's area for you (§2.3).

**Hanging, wall and climbing crops**

| Crop | Block | Grows on | Right-click | Hoe RC | Hoe break |
|---|---|---|---|---|---|
| Glow Berries (plant with Glow Berry Pips) | `minecraft:cave_vines` | the underside of a block | own pick | pick, **vine tip only** | no |
| Mulberries | `upgrade_aquatic:mulberry_vine` | under logs or leaves | own pick at stage 4 (back to 1); shears trim | no | no |
| Cocoa | `minecraft:cocoa` | side of a jungle log | Quark replant (config list) | replant | no |
| Jungle grapes (wall vine) | `vinery:jungle_grapes_*` vine | walls, like vines | own pick | no | likely yes (vines are replaceable plants) |
| Grapevine Stem / Lattice | `vinery:grapevine_stem`, `vinery:grapevine_lattice` | stem: a full solid block or another stem | own pick (stem: stage 4 back to 2; lattice: stage 3+ back to 1) | no | no |
| Vanilla | `neapolitan:vanilla_vine` | sideways from `#neapolitan:vanilla_plantable_on` (logs, leaves, dirt-like blocks, Rich Soil, Fertile Soil) | none (break for pods) | no | not checked |

**Nether and colony crops**

| Crop | Block | Grows on | Right-click | Hoe RC | Hoe break |
|---|---|---|---|---|---|
| Nether Wart | `minecraft:nether_wart` | Soul Sand, **Nulch** (`immersive_weathering:nulch_block`). Not Fertile Soil | Quark replant (config list) | replant | yes |
| Propelplant | `nethersdelight:propelplant_*` | nylium, netherrack, Rich Soul Soil | own pick of pearls | pick | yes |
| Mushroom colonies | `farmersdelight:brown_mushroom_colony`, `red_mushroom_colony` | grow only on Rich Soil (also Rich Planter); survive on most soils in the dark | shears take one mushroom (stage −1) | no | yes |
| Fungus colonies | `nethersdelight:crimson_fungus_colony`, `warped_fungus_colony` | nylium, mycelium, Soul Soil, Rich Soul Soil, dirt | shears, same as above | no | yes |

**Column plants and pests**

| Plant | Block | Grows on | Right-click | Hoe RC | Hoe break |
|---|---|---|---|---|---|
| Sugar Cane | `minecraft:sugar_cane` | Dirt or sand next to water. **Rich Soil and Fertile Soil need no water** | none | no | yes, same height only |
| Cactus, Bamboo, Kelp | vanilla | vanilla rules | none | no | no |
| Weeds (pest) | `immersive_weathering:weeds` | spreads onto Dirt, Farmland, Rich, Fertile, cracked and mossy blocks | **Quark replant, so it plants the weed again** | replant (same problem) | yes |

### 2.2 Right-click replant (Quark Simple Harvest)

From `vazkii.quark.content.tweaks.module.SimpleHarvestModule` (Quark 3.4-418).

- **Which crops it knows:**
  - the `Harvestable Blocks` config list: wheat, carrots, potatoes, beetroots, nether wart and cocoa;
  - every **non-vanilla block that is a `CropBlock` subclass** (`Do Harvesting Search`). For these it
    records one state: the crop's default state with its age set to the property's maximum.
- **Tobacco is in neither list**, so right-click does nothing.
- **Exact state match.** A crop with extra block-state properties only matches in its default
  setting of those properties. That's why flax only matches its lower half, and why tomatoes only
  match the bottom vine and not vines on a rope. The other states fall through to the block's own
  right-click, if it has one.
- **What a harvest does:** it rolls the block's loot table with a copy of your held item (an empty hand
  counts as a stick, and your fortune level is kept). It removes **one** of the block's own item from
  the drops if it's there: wheat seeds for wheat, a cocoa bean for cocoa, an onion for onion. Then it
  sets the block back to its first stage. Crops whose block has no item of its own replant for free.
- **Held item:** `Empty Hand Harvest = true` means you don't need a hoe. Any held item that isn't a hoe
  harvests just the one block you clicked.
- **Where to click:** the crop, or the block under it. Quark tries the clicked block, then the block above.
- **Bushes:** for empty-hand clicks Quark leaves bushes alone and their own right-click runs. Quark's
  bush handling only applies with a hoe (§2.3).
- **Villagers:** farmer villagers use the same replant instead of breaking crops.
- **Opt-out tag:** `#quark:simple_harvest_blacklisted` (empty in the pack).

### 2.3 Hoe area mechanics (Quark Hoe Harvesting)

A "hoe" is any `HoeItem`, anything in `#forge:tools/hoes`, or any tool that can do the hoe-dig action. That
includes the pack's echo hoe, Dragon Hoe and Felsic Hoe.

| Hoe | Area |
|---|---|
| Diamond, Netherite, Elementium (`#quark:big_harvesting_hoes`) | 5x5 |
| Every other hoe, including echo, dragon, felsic, copper, steel, manasteel | 3x3 |

**Hoe right-click (area replant).** Click a ripe crop or the farmland under it:

- Every known crop in the area, at the clicked height and one block above, gets the §2.2 replant.
- Every **bush-type** block in the area gets a simulated click, which picks berry bushes. "Bush-type"
  means a non-vanilla bush or growing plant that takes bone meal, plus `minecraft:sweet_berry_bush` and
  `minecraft:cave_vines` from the config.
- It costs no durability (`Harvesting Costs Durability = false`).
- Clicking grass or dirt **tills it** instead. The till check runs first.
- With a hoe, nothing happens unless the clicked block or the one above is a known crop or bush.

**Hoe break (area break).** When you **break** a plant with a hoe, every plant-like block at the same
height in the hoe's area breaks too:

- "Plant-like" means any Forge plant except water and desert types, or any replaceable plant
  material. That covers crops, bushes, stems, saplings, flowers, tall grass and sugar cane. Cactus is a
  desert type and isn't included.
- It **ignores ripeness and never replants**. Unripe crops in the area are destroyed, and you only
  get their seeds back.
- It costs 1 durability per break, and only if one of the extra blocks wasn't instant-break.
- Use it for clearing a field, or for harvesting the top row of hemp, flax or sugar cane by breaking
  at that height.

### 2.4 Soil rules

| Soil | Block | Accepts | Growth effect |
|---|---|---|---|
| Dirt, Grass | `#minecraft:dirt` | bushes and other "plains" plants. Not farmland crops | none |
| Farmland | `minecraft:farmland` | farmland crops, bushes | moist farmland speeds crop growth; dries and reverts to dirt; can be trampled |
| Rich Soil | `farmersdelight:rich_soil` | anything that **isn't** a farmland crop, Nether plant or water plant. So bushes, saplings, flowers, rice and sugar cane, but **not** wheat, cabbage, flax, hemp or tobacco | 20% chance per random tick to bone-meal the plant above (`richSoilBoostChance = 0.2`); turns a mushroom above into a colony |
| Rich Farmland | `farmersdelight:rich_soil_farmland` | farmland crops and plains plants. Grape bushes and tobacco refuse it because they check the block below directly | same 20% boost, but only while fully moist; never reverts to dirt; can't be trampled |
| Fertile Soil | `immersive_weathering:mulch_block` | everything except Nether plants, with no tilling. Grape bushes and tobacco check the block below directly, so the soil's rule doesn't apply to them | always counts as fertile (moist-farmland speed). While soaked (water bucket, rain, touching water) and light 9+: farmland crops get a bone-meal step per soil random tick, stems get 10 extra random ticks, other plants nothing extra. Never dries out to dirt; can't be trampled |
| Nulch | `immersive_weathering:nulch_block` | Nether Wart (and other Nether-type plants) | none for crops |

Rich Soil and Fertile Soil are both in `#minecraft:dirt`. That's why plain bushes accept them through
the normal bush rule as well. Rich Farmland isn't in that tag.

### 2.5 Tobacco in detail (`the_dirty_stuff:tobacco_seeds`)

- **Class:** `net.mcreator.thedirtystuff.block.TobaccoSeedsBlock extends Block`, with a block entity.
  It isn't a `CropBlock`, isn't a bush, and isn't a Forge plant.
- **Placement and survival:** `TobaccoSeedsBlockValidPlacementConditionProcedure` returns
  `block below == Blocks.FARMLAND`. It's an identity check on vanilla farmland, so Rich Farmland and
  Fertile Soil (both subclasses of farmland) and Rich Soil all fail. If the farmland under it turns to
  dirt, the tobacco pops.
- **Growth:** the `blockstate` property goes 0 to 7. Each random tick adds +1 when light is 9 or more, with
  no chance roll, so it takes about 8 minutes at the default tick speed. It ignores soil fertility
  and Forge's crop-growth events. Bone meal adds 1 to 5 stages, depending on the stage.
- **Drops at stage 7:** 4 leaves, plus 0 to 3 more leaves with fortune, plus 1 to 2 seeds. Before stage 7:
  just the seed.
- **Why the three mechanics miss it:**
  - **Right-click:** Quark only auto-finds `CropBlock` classes, and tobacco isn't in the config list.
  - **Hoe RC:** same reason as right-click.
  - **Hoe break:** it isn't a Forge plant, and its material (`PLANT`) isn't replaceable.

## 3. What the pack changes

- **Fertile Soil** is a rename of Immersive Weathering's Mulch Block ([namechanges.js](../kubejs/client_scripts/namechanges.js)).
  The pack adds its own recipes from Rich Soil plus mulches ([sawdust.js](../kubejs/server_scripts/wood/sawdust.js))
  and soil tooltips ([tooltips.js](../kubejs/client_scripts/tooltips.js), Rich Soil and Fertile Soil lines).
- **Quark Simple Harvest and Hoe Harvesting** are enabled with default settings.
- **Berry Good:** sweet berries and glow berries only plant from pips (`Sweet Berries require pips` and
  `Glow Berries require pips` both true).
- **Immersive Weathering mulch growth:** `mulch_grows_crops = true`, bone-meal style for crops,
  `mulch_growth_random_ticks = 10` for stems.
- **Bushier Flowers** growth is disabled, see the bushier-flowers notes in memory. It isn't a crop here.
- Nothing in KubeJS or patcherMod touches crop placement or harvesting today.

## 4. Limits and decisions

Open gaps, none fixed yet. Each has a suggested fix, for you to approve or reject:

- **G1 Tobacco has no right-click replant.** Fix (config only): add
  `"the_dirty_stuff:tobacco_seeds[blockstate=7],the_dirty_stuff:tobacco_seeds[blockstate=0]"` to
  Quark's `Harvestable Blocks`. That also gives it hoe-area replant. Copy the edited config to
  `configs_edited/` and the server (backup rule).
- **G2 Tobacco refuses Rich Farmland and Fertile Soil.** The check is hard-coded, so this needs a
  patcherMod mixin on `TobaccoSeedsBlockValidPlacementConditionProcedure.execute`. It would accept a block
  tag (for example `creativeengineering:tobacco_soil` = farmland, rich soil farmland, mulch block, built
  through `global.tags`), which matches the other farmland crops. Leave untilled Rich Soil out, as for
  every other farmland crop.
- **G3 Tobacco isn't caught by the hoe area break.** It would need a mixin into Quark's `canHarvest` or
  into the tobacco block. Low value; leave it unless asked.
- **G4 Hemp has no right-click.** That's IE's design: break the top and the bottom regrows it. The
  hoe area break on the top row already works as a harvester. No change suggested.
- **G5 Pack hoes are 3x3.** Echo, Dragon and Felsic hoes are end-game but not in
  `#quark:big_harvesting_hoes`. Fix: add them through `global.tags` if you want 5x5.
- **G6 Untilled Rich Soil never takes farmland crops.** That's Farmer's Delight's design: till it into Rich
  Farmland. Not a bug.
- **G7 Grape bushes refuse Farmland and Rich Farmland.** That's Vinery's full-opaque-block rule ([vinery.md](vinery.md)).
- **G8 Right-clicking ripe Weeds replants them.** Weeds are a `CropBlock`, so Quark's search picks them up. Fix if
  wanted: add `immersive_weathering:weeds` to `#quark:simple_harvest_blacklisted` (the tag also blocks the
  replant step), or just break weeds.
- **G9 Flax top half doesn't respond to right-click.** Flax forwards the click to its lower half, but
  Quark rejects the forwarded click because the hit position is still the top block. Click the lower
  half or the farmland.
- **G10 The hoe area break destroys unripe crops.** That's Quark's design. Use the hoe right-click for
  harvesting and the hoe break only for clearing.

## 5. File map

- [config/quark-common.toml](../config/quark-common.toml): `[tweaks.simple_harvest]`, `[tweaks.hoe_harvesting]`, module switches `"Simple Harvest"`, `"Hoe Harvesting"`
- [config/berry_good-common.toml](../config/berry_good-common.toml): pips requirement
- [config/immersive_weathering-common.toml](../config/immersive_weathering-common.toml): `[misc]` mulch growth
- [config/farmersdelight-common.toml](../config/farmersdelight-common.toml): `richSoilBoostChance`, tomato rope settings
- [config/endercrop-common.toml](../config/endercrop-common.toml): tilled end stone, growth multipliers
- [kubejs/startup_scripts/_lib/item_dict.js](../kubejs/startup_scripts/_lib/item_dict.js): `rich_soil`, `rich_farmland`, `fertile_soil`, `nulch`
- [kubejs/client_scripts/namechanges.js](../kubejs/client_scripts/namechanges.js): Fertile Soil rename
- [kubejs/client_scripts/tooltips.js](../kubejs/client_scripts/tooltips.js): soil tooltips
- [kubejs/server_scripts/wood/sawdust.js](../kubejs/server_scripts/wood/sawdust.js): Fertile Soil recipes
- [vinery.md](vinery.md): grape bush, stem and lattice detail

## 6. Offline checks

Everything in §2 was read from decompiled classes (2026-10-02). To re-check after a mod update:

- `python3 tools/decompile_mod.py mods/<jar> <out dir>`. The classes that matter:
  - Quark: `SimpleHarvestModule`, `HoeHarvestingModule`
  - Farmer's Delight: `RichSoilBlock`, `RichSoilFarmlandBlock`, `RiceBlock`, `TomatoVineBlock`
  - Immersive Weathering: `MulchBlock` and the mixins `SelfMulchMixin` / `SelfNulchMixin`
  - The Dirty Stuff: `TobaccoSeedsBlock` and its `procedures/TobaccoSeeds*`
  - each crop's block class
- Forge's own soil logic is in `Block.canSustainPlant` and `IPlantable.getPlantType` in
  `libraries/net/minecraftforge/forge/1.19.2-43.3.9/forge-1.19.2-43.3.9-client.jar` (patched vanilla classes).
- Tag contents (`#minecraft:dirt`, `#quark:big_harvesting_hoes`, `#quark:simple_harvest_blacklisted`) come
  from `kubejs/exported/tags/block_tags.json` and `item_tags.json`, refreshed by `/dumptags`.

## 7. In-game test suite

- [ ] **CR-01 Tobacco soils:** tobacco seeds on Farmland plant. On Rich Farmland, Fertile Soil and Rich Soil they're refused.
- [ ] **CR-02 Tobacco harvest:** right-click a stage-7 tobacco (empty hand, then an iron hoe) → nothing. Breaking it drops 4+ leaves and 1-2 seeds.
- [ ] **CR-03 Wheat replant:** right-click ripe wheat with an empty hand → wheat and seeds drop (one seed fewer than breaking), and the wheat is back at stage 0.
- [ ] **CR-04 Any item harvests:** right-click ripe wheat while holding a stick or a block → it's harvested, and the block isn't placed.
- [ ] **CR-05 Hoe area size:** iron hoe right-click on a ripe field → 3x3 replanted, no durability lost. Diamond hoe → 5x5. Echo hoe → 3x3.
- [ ] **CR-06 Hoe on grass:** iron hoe right-click on grass next to ripe wheat → the grass is tilled, no harvest.
- [ ] **CR-07 Hoe break:** break one ripe wheat with an iron hoe in a half-ripe field → the 3x3 around it breaks, unripe ones included, and nothing replants.
- [ ] **CR-08 Rich Soil vs crops:** cabbage seeds on Rich Soil are refused. On Rich Farmland and Fertile Soil they plant.
- [ ] **CR-09 Flax halves:** right-click a ripe flax's top half → nothing. Its lower half → harvested, replanted, and the top half gone.
- [ ] **CR-10 Hemp hoe break:** with a row of grown hemp, break one top with a hoe → the neighbouring tops break and the bottoms stay and regrow.
- [ ] **CR-11 Rice:** right-click ripe panicles → rice drops and the panicles go back to stage 0.
- [ ] **CR-12 Berry pips on soils:** Sweet Berry Pips plant on Rich Soil, Rich Farmland and Fertile Soil. Iron hoe right-click on a ripe bush patch picks 3x3 bushes.
- [ ] **CR-13 Grape bush on Fertile Soil:** red grape seeds on Fertile Soil in full light → record whether they plant (unknown, see §2.1). On Farmland → refused.
- [ ] **CR-14 Nether Wart soils:** plants on Nulch. Refused on Fertile Soil.
- [ ] **CR-15 Ender Crop light:** ender seeds on Rich Farmland in daylight → refused. Under a roof with no light → plant.
- [ ] **CR-16 Weeds:** right-click ripe weeds → they come back at stage 0 (G8).
- [ ] **CR-17 Tomatoes:** right-click a ripe bottom vine → replanted at stage 0. Right-click a ripe vine on a rope → tomatoes picked, back to stage 0.
- [ ] **CR-18 Stems and hoe break:** break a crop with a hoe next to a pumpkin stem → the stem breaks too.
- [ ] **CR-19 Sugar cane without water:** sugar cane on Rich Soil and on Fertile Soil with no water nearby → it plants.
- [ ] **CR-20 Glow berries hoe click:** iron hoe right-click under a ripe glow berry vine → only the bottom (tip) block in range is picked.

## 8. History

- **2026-10-02:** written from the decompiled jars (Quark 3.4-418, Farmer's Delight 1.2.4, Immersive
  Weathering 1.2.10, The Dirty Stuff 2.1, IE 9.2.4, Supplementaries 2.4.20, Neapolitan 4.1.0, Delightful
  3.4.1, Rustic Delight 1.3.0, Ender Crop 1.7.0, Upgrade Aquatic 5.1.0, Nether's Delight 3.1, Berry Good
  6.1.0, Floral Enchantment 1.4.3, Vinery 1.3.10) and Forge 43.3.9's patched vanilla classes. No pack
  changes made; gaps G1 to G10 open.
