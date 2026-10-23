# Crops: soils, right-click harvest, hoe harvesting

Test prefix **CR-** · Last updated **2026-10-22** · Overall status **reference from the decompiled jars; tobacco parity, hoe areas and wild-crop placement (§2.8) DEPLOYED with 1.0.4; WTHIT growth line (§2.9) and Immersive Weathering soils (§2.8.1) DEPLOYED with 1.0.6 (2026-10-14); in-game checks pending**

## 0. At a glance

| Part | Where | Status |
|---|---|---|
| Per-crop table: type, soils, right-click, hoe right-click, hoe break | §2.1 | reference; CR-01 to CR-20 check the surprising parts |
| How right-click replanting works (Quark Simple Harvest) | §2.2 | reference |
| How the hoe area mechanics work (Quark Hoe Harvesting + Simple Harvest) | §2.3 | reference |
| Per-hoe harvest area sizes 1x1 to 9x9 + auto tooltip | §2.3 | DEPLOYED with 1.0.4 (2026-10-04); CR-26 to CR-30 pending |
| Soil rules (Dirt, Farmland, Rich Soil, Rich Farmland, Fertile Soil, Nulch) | §2.4 | reference |
| Crop parity layer: how a non-standard crop is brought in line (tobacco done) | §2.6 | DEPLOYED with 1.0.4 (2026-10-04); CR-01, CR-02, CR-21 to CR-25 pending |
| Pack changes to farming | §3 | DEPLOYED (tobacco parity with 1.0.4) |
| Gaps: hemp, grape bushes, weeds, pack hoes | §4 | G1 to G3 (tobacco) fixed; the rest open |
| 1.0.4 mods: Corn Delight, Farmer's Respite (tea, coffee), Fruits Delight (bushes, lemon, pineapple, hamimelon, 13 fruit trees) | §2.1 | reference from the decompiled jars 2026-10-02; G12 to G15 open; CR-31 to CR-37 |
| Wild generation: where every crop, bush and fruit tree spawns (biomes, rate, height, ground, side blocks such as Coarse Dirt, Adzuki Soil, mint ponds) | §2.7 + generated [crops_by_biome.md](crops_by_biome.md) | reference 2026-10-03; findings V1 to V12 feed the planned vegetation worldgen system; CR-38 to CR-46 |
| Pack wild-crop placement: every wild crop and fruit tree moved to the biomes you specified (`wild_crops.json`), leaf apples and AM leaf bananas off, FR Nether coffee off | §2.8 | DEPLOYED with 1.0.4 (2026-10-04); `wild_crops.json` changed again for 1.0.5, DEPLOYED with 1.0.6; not looked at in game; V13 to V20; CR-47 to CR-58 |
| WTHIT growth line for every plant WTHIT skips (hemp, tobacco, strawberries, salmonberries, mint, grapes, blueberries, cranberries, lemons and the other plants, 62 blocks in all) | §2.9 | BUILT 2026-10-04, headless-tested (38 checks, 290-block audit); in patcherMod 1.0.5 (in `mods/`), DEPLOYED with 1.0.6 (2026-10-14); jar boots clean (single player 2026-10-11); CR-59 to CR-66 |
| Extra stem fruit: melon, pumpkin and hamimelon stems grow up to 4 fruits, each later one after a longer minimum wait | §2.10 | BUILT 2026-10-22 (patcherMod 1.1.01, in `mods/` since the 2026-10-22 scratch run, md5 a4d48336 with the fix below); built, scratch server PASSED 2026-10-22 (SQ-SF-00 to SQ-SF-05, 17 of 17, `tools/farm_worldgen_probe.js`); CR-71 to CR-75 in game; F-SS1 (extra fruit ignores seasons) open |
| Wild-crops batch 2026-10-22: tea in 20 biomes, Vinery grapes under the system, wild flax off, Rustic Delight patches out of bone meal, white strawberries from Y 192 | §2.8, §4 V1, V6, V7, V20 | BUILT 2026-10-22 (data + KubeJS, not deployed); `tools/test_wild_crops.sh` passes (53 crop tags, 52 add modifiers); built, scratch server PASSED 2026-10-22 (SQ-WC-00 to SQ-WC-05); CR-83, CR-86 to CR-88 in game |
| Wild plants, bushes and trees on Immersive Weathering soils (humus, fluvisol, silt, vertisol, cryosol, rooted grass; not permafrost) | §2.8.1 | BUILT 2026-10-05, scratch-server tested (12 features x 9 grounds, before/after); the 65 `kubejs/data` overrides are DEPLOYED with 1.0.6 (2026-10-14); only new chunks change; CR-67, CR-68 |

- **Open first:** §2.1 for "can I plant X on Y" or "does X replant on right-click". §2.2 and §2.3 explain why.
- **Knobs:** `config/quark-common.toml` sections `[tweaks.simple_harvest]` and `[tweaks.hoe_harvesting]`.
  The pack runs Quark's defaults: empty-hand harvest on, no durability cost, harvesting search on,
  villagers use simple harvest, hoe radius 2 (3x3) and 3 (5x5). The pack overrides the size per hoe
  with the `creativeengineering:hoe_harvest/NxN` item tags (§2.3); Quark's radius only covers untagged hoes.
- **Tobacco:** it isn't a crop class at all. It's a plain MCreator block with a block entity, and
  its placement check is hard-coded to `minecraft:farmland` (§2.5). The pack's crop parity layer (§2.6)
  now gives it the same soils, right-click replant, hoe replant and hoe area break as wheat.
- **Bringing another crop in line:** follow the recipe in §2.6.
- **Where does X grow wild:** §2.8 (the pack's rules, `wild_crops.json`), [crops_by_biome.md](crops_by_biome.md) for exact
  biome lists; §2.7 for what each mod does on its own. Regenerate with `python3 tools/gen_crop_worldgen_table.py` after
  mod, biome or `wild_crops.json` changes.
- **How to re-read the code:** `python3 tools/decompile_mod.py <jar> <out dir>` (§6).

## 1. Overview

Three separate systems decide how a crop behaves:

1. **The crop's own block class** decides where it can be placed and survive, how it grows, and
   whether it has its own right-click picking (berry bushes, tomatoes, grapes).
2. **The soil block** gets a say through Forge's `canSustainPlant`. Farmer's Delight Rich Soil and Rich
   Farmland, and Immersive Weathering Mulch (renamed **Fertile Soil** in the pack), each override it
   with their own rules (§2.4). A crop that skips `canSustainPlant` and checks the block below directly
   (tobacco, grape bushes) ignores those overrides. For tobacco the pack replaces that check with the
   `creativeengineering:crops/farmland_soils` tag (§2.6).
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
| Tobacco | `the_dirty_stuff:tobacco_seeds` | no | yes | no | yes (pack, §2.6) | yes (pack, §2.6) | Quark replant (config list, pack) | replant | yes (pack, §2.6) |

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
| Corn | `corn_delight:corn_crop` (both halves) | no | yes | no | yes | yes | Quark replant (auto-found), **lower half only** | lower halves | yes, same height only |

- **Flax:** the top half appears from stage 4. Right-click the lower half, or the farmland under it.
  Clicking the top half does nothing (§4, G9). The replant resets the lower half and the top half disappears.
- **Hemp:** the top grows once the bottom is fully grown. Breaking a top with a hoe also breaks the
  other tops at that height in the hoe's area and leaves the bottoms, so a hoe works as a hemp
  harvester if you break the top row. Breaking a bottom with a hoe breaks the bottoms around it
  and you lose those plants.
- **Corn** (Mysterious Mountain Lib `HighCropBlock`, a `CropBlock`): the top half is the same block with
  `upper=true`. It appears once the lower half reaches stage 4 and needs light 8+ or open sky, then ages on its own.
  Both halves drop corn at stage 7, else a seed. Quark only matches the lower half (G13): replanting it to stage 0
  makes the top fail its survival check, so the top breaks and drops its own corn. Clicking the top does nothing.
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
| Pumpkin, Melon, Hamimelon stems | `minecraft:pumpkin_stem`, `melon_stem`, `fruitsdelight:hamimelon_stem` | no | yes | no | yes | yes | none (break the fruit) | no | **yes, stems too** |

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
| Blueberries, Cranberries (plant the berry) | `fruitsdelight:blueberry_bush`, `cranberry_bush` | yes | yes | yes | yes | yes | own pick at stage 4: 1-2 berries (back to 2) | pick | yes |
| Lemon (two-high bush, plant Lemon Seeds) | `fruitsdelight:lemon_tree` | yes | yes | yes | yes | yes | own pick at stage 4, either half: 1-2 lemons (back to 2) | pick | yes, same height only |
| Tea (plant Tea Seeds) | `farmersrespite:small_tea_bush`, then the two-high `tea_bush` | yes | **no** | yes | **no** | yes | **shears only**: leaves by age + 2-3 sticks, back to a small bush. An axe stunts it | no (needs shears) | yes, same height only |

- Grape bushes need a full opaque block below plus their own light rules. See [vinery.md](vinery.md) §2.6.
  Farmland and Rich Farmland are not full blocks. Fertile Soil has a full-cube shape, but it isn't
  confirmed to count as opaque (CR-13).
- **Fruits Delight bushes** grow 1 in 5 random ticks at light 9+ above them. They need no farmland.
- **Tea:** the small bush becomes the two-high bush 1 in 20 random ticks when the space above is free, then ages
  0 to 3 (1 in 12 ticks). Shears give green leaves at age 0, yellow at 1, yellow + black at 2, black at 3, so you
  choose the tea by when you cut. Right-click with an axe stunts the bush: it stops aging, which holds a colour.
  Both bushes check `#minecraft:dirt` directly instead of Forge's soil hook, so Farmland and Rich Farmland refuse
  them (G12). Bone meal is off (`farmersrespite-common.toml` `enableBoneMealTeaBush = false`).
- "Pick" in the Hoe RC column means Quark clicks every bush in the hoe's area for you (§2.3).

**Hanging, wall and climbing crops**

| Crop | Block | Grows on | Right-click | Hoe RC | Hoe break |
|---|---|---|---|---|---|
| Glow Berries (plant with Glow Berry Pips) | `minecraft:cave_vines` | the underside of a block | own pick | pick, **vine tip only** | no |
| Mulberries | `upgrade_aquatic:mulberry_vine` | under logs or leaves | own pick at stage 4 (back to 1); shears trim | no | no |
| Cocoa | `minecraft:cocoa` | side of a jungle log | Quark replant (config list) | replant | no |
| Jungle grapes (wall vine) | `vinery:jungle_grape_bush_red`, `_white` (vine) | walls, like vines | own pick | no | likely yes (vines are replaceable plants) |
| Grapevine Stem / Lattice | `vinery:grapevine_stem`, `vinery:grapevine_lattice` | stem: a full solid block or another stem | own pick (stem: stage 4 back to 2; lattice: stage 3+ back to 1) | no | no |
| Vanilla | `neapolitan:vanilla_vine` | sideways from `#neapolitan:vanilla_plantable_on` (logs, leaves, dirt-like blocks, Rich Soil, Fertile Soil) | none (break for pods) | no | not checked |

**Coffee, pineapple and fruit trees (1.0.4 mods)**

| Crop | Block | Grows on | Right-click | Hoe RC | Hoe break |
|---|---|---|---|---|---|
| Coffee (plant Coffee Beans) | `farmersrespite:coffee_bush`, grows into `coffee_stem` + `coffee_bush_top`, later `coffee_stem_double` / `_middle` | basalt, polished basalt, smooth basalt, magma block | own pick from a stem at stage 2: 1 coffee berry (back to 0) | pick | yes |
| Pineapple (plant a Pineapple Sapling) | `fruitsdelight:pineapple` | **sand, red sand, coarse dirt** (`#fruitsdelight:pineapple_grows_on`) | none (break at stage 4) | no | yes |
| Fruit trees: apple, bayberry, durian, fig, hawberry, kiwi, lychee, mango, mangosteen, orange, peach, pear, persimmon | `fruitsdelight:*_sapling`, fruit on `fruitsdelight:*_leaves` | sapling: vanilla sapling soils | right-click (or walk into) fruiting leaves: drops the fruit, back to plain leaves | no | no (leaves aren't plants) |

- **Coffee drains crops (G14).** Outside the Nether a coffee plant only grows by eating a `CropBlock` within 2
  blocks: each growth step turns that crop into Wither Roots or a Wild Coffee Bush. In an ultrawarm dimension (the
  Nether) it grows without that. Bone meal is off (`enableBoneMealCoffeeBush = false`).
- **Fruit trees:** only natural leaves (`persistent=false`) flower and fruit; leaves you place never do. Flowers
  spread leaf to leaf and some decay (Fruits Delight config `flowerDecayChance`). Ripe durians also drop on their
  own as falling blocks.
- **Wild plants** (`corn_delight:wild_corn`, `farmersrespite:wild_tea_bush`, `wild_coffee_bush`,
  `fruitsdelight:wild_pineapple`) are break-for-drops worldgen plants, not crops.

**Nether and colony crops**

| Crop | Block | Grows on | Right-click | Hoe RC | Hoe break |
|---|---|---|---|---|---|
| Nether Wart | `minecraft:nether_wart` | Soul Sand, **Nulch** (`immersive_weathering:nulch_block`). Not Fertile Soil | Quark replant (config list) | replant | yes |
| Propelplant | `nethersdelight:propelplant_*` | nylium, netherrack, Rich Soul Soil | own pick of pearls | pick | yes |
| Mushroom colonies | `farmersdelight:brown_mushroom_colony`, `red_mushroom_colony`; Fungi Delight port `fungidelight:inky_cap_mushroom_colony`, `morel_mushroom_colony` (FD's class, [fungi_delight.md](fungi_delight.md)) | grow only on Rich Soil (also Rich Planter); survive on most soils in the dark | shears take one mushroom (stage −1) | no | yes |
| Fungus colonies | `nethersdelight:crimson_fungus_colony`, `warped_fungus_colony` | nylium, mycelium, Soul Soil, Rich Soul Soil, dirt | shears, same as above | no | yes |

**Column plants and pests**

| Plant | Block | Grows on | Right-click | Hoe RC | Hoe break |
|---|---|---|---|---|---|
| Sugar Cane | `minecraft:sugar_cane` | Dirt or sand next to water. **Rich Soil and Fertile Soil need no water** | none | no | yes, same height only |
| Cactus, Bamboo, Kelp | vanilla | vanilla rules | none | no | no |
| Weeds (pest) | `immersive_weathering:weeds` | survives on Dirt, Farmland, Rich Soil, Fertile, cracked and mossy blocks (not Rich Farmland); appears and spreads on its own only on open-sky vanilla Farmland (IW `block_growths/weeds*.json`, owner `minecraft:farmland`) | **Quark replant, so it plants the weed again** | replant (same problem) | yes |
| Wither Roots (pest) | `farmersrespite:wither_roots`, `wither_roots_plant` | put there by coffee on the crop it drains; survive on Dirt, Farmland, basalt, magma | none | no | yes |

### 2.2 Right-click replant (Quark Simple Harvest)

From `vazkii.quark.content.tweaks.module.SimpleHarvestModule` (Quark 3.4-418).

- **Which crops it knows:**
  - the `Harvestable Blocks` config list: wheat, carrots, potatoes, beetroots, nether wart and cocoa;
  - every **non-vanilla block that is a `CropBlock` subclass** (`Do Harvesting Search`). For these it
    records one state: the crop's default state with its age set to the property's maximum.
- **Tobacco** isn't a `CropBlock`, so the pack adds it to the config list:
  `"the_dirty_stuff:tobacco_seeds[blockstate=7]"` (§2.6).
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

| Hoe | Area | Tag |
|---|---|---|
| Wooden, Stone | 1x1 (no area) | `creativeengineering:hoe_harvest/1x1` |
| Copper, Golden | 2x2 | `hoe_harvest/2x2` |
| Iron, IE Steel | 3x3 | `hoe_harvest/3x3` |
| Diamond, Netherite, Elementium, Manasteel | 4x4 | `hoe_harvest/4x4` |
| Echo, Felsic, Dragon (end gear) | 5x5 | `hoe_harvest/5x5` |
| Any other hoe | Quark's own: 5x5 in `#quark:big_harvesting_hoes`, else 3x3 | none |

- **Where:** tags in [equipmenttags.js](../kubejs/server_scripts/tags/equipmenttags.js) "Hoe harvest area"
  (via `global.tags` + item_dict keys), read by patcherMod `crops/HoeHarvestArea`. Any size from 1x1 to 9x9
  has a tag; a hoe in several gets the largest. Tags reload with `/reload`, no rebuild.
- **Even sizes** can't be centred, so the area reaches one block further on the side the player is
  looking toward, on each axis. A 2x2 is the target block plus the blocks ahead of it. Looking straight
  along an axis, the sideways half flips with tiny head turns.
- **Odd sizes** are centred, same as Quark.
- **How:** `mixin/QuarkHoeAreaHarvestMixin` (right-click, `SimpleHarvestModule.click`) and
  `mixin/QuarkHoeAreaBreakMixin` (area break, `HoeHarvestingModule.onBlockBroken`) take over where Quark
  asks for the range, after all of Quark's own checks, and run Quark's loop over the new area. The
  empty-hand right-click stays Quark's.
- A 1x1 hoe still replants the one crop clicked, but like Quark's radius 1 it doesn't click bushes.
- **Tooltip:** every hoe with an area of 2x2 or more shows "Harvest area: NxN" (plus "toward where you
  look" when even) and "Right-click ripe crops to harvest and replant, break plants to clear", generated
  from the same size (`crops/HoeHarvestTooltip`, lang keys `creativeengineering.hoe_harvest.*`).

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
| Rich Farmland | `farmersdelight:rich_soil_farmland` | farmland crops and plains plants. Grape bushes refuse it because they check the block below directly. Tobacco accepts it through the pack's soil tag (§2.6) | same 20% boost, but only while fully moist; never reverts to dirt; can't be trampled |
| Fertile Soil | `immersive_weathering:mulch_block` | everything except Nether plants, with no tilling. Grape bushes check the block below directly, so the soil's rule doesn't apply to them. Tobacco accepts it through the pack's soil tag (§2.6) | always counts as fertile (moist-farmland speed). While soaked (water bucket, rain, touching water) and light 9+: farmland crops get a bone-meal step per soil random tick (`CropBlock`s from IW, tobacco from the pack, §2.6), stems get 10 extra random ticks, other plants nothing extra. Never dries out to dirt; can't be trampled |
| Fertile Soul Soil (Nulch; renamed 2026-10-23) | `immersive_weathering:nulch_block` | Nether Wart (and other Nether-type plants), Ghost Pepper seeds | none for crops |

Rich Soil and Fertile Soil are both in `#minecraft:dirt`. That's why plain bushes accept them through
the normal bush rule as well. Rich Farmland isn't in that tag.

### 2.5 Tobacco in detail (`the_dirty_stuff:tobacco_seeds`)

- **Class:** `net.mcreator.thedirtystuff.block.TobaccoSeedsBlock extends Block`, with a block entity.
  It isn't a `CropBlock`, isn't a bush, and isn't a Forge plant (`IPlantable`). It is a `BonemealableBlock`.
- **Placement and survival (stock):** `TobaccoSeedsBlockValidPlacementConditionProcedure` returns
  `block below == Blocks.FARMLAND`. That's an identity check on vanilla farmland, so Rich Farmland and
  Fertile Soil (both subclasses of farmland) and Rich Soil all fail. `canSurvive` is the procedure's
  only caller, so the same check covers placing, surviving and neighbour updates.
- **Growth:** the `blockstate` property goes 0 to 7. Each random tick adds +1 when light is 9 or more, with
  no chance roll, so it takes about 8 minutes at the default tick speed. It ignores soil moisture and
  Forge's crop-growth events. Bone meal adds 1 to 5 stages, depending on the stage.
- **Drops at stage 7:** 4 leaves, plus 0 to 3 more leaves with fortune, plus 1 to 2 seeds. Before stage 7:
  just the seed. A Quark replant takes one seed out of those drops, the same as wheat.
- **Stock gaps and what the pack does about each one** (details in §2.6):

| Stock behaviour | Cause | Pack fix |
|---|---|---|
| Refuses Rich Farmland and Fertile Soil | identity check on `Blocks.FARMLAND` | `TobaccoPlacementMixin`: the check becomes "vanilla farmland or `#creativeengineering:crops/farmland_soils`" |
| Dry vanilla farmland under it turns to dirt and pops it (wheat keeps its farmland) | Forge's `FarmBlock.isUnderCrops` only counts `IPlantable`s | `FarmBlockUnderCropsMixin`: `#creativeengineering:crops/farmland_crops` counts as a crop |
| No bone-meal step from soaked Fertile Soil | IW only calls `CropBlock.growCrops` | `MulchBlockCropGrowthMixin`: one `performBonemeal` under IW's own conditions |
| No right-click or hoe right-click replant | Quark only auto-finds `CropBlock`s | Quark `Harvestable Blocks` line `the_dirty_stuff:tobacco_seeds[blockstate=7]` |
| Not caught by the hoe area break | not `IPlantable`, material `PLANT` isn't replaceable | `QuarkHoeHarvestMixin`: `#creativeengineering:crops/farmland_crops` counts in `canHarvest` |
| Rich Farmland's 20% growth boost | already worked: FD bone-meals any `BonemealableBlock` | none needed |

### 2.6 Crop parity approach (how to bring any crop in line)

Built for tobacco on 2026-10-02. Use the same pieces for the next crop. The aim is for a crop to behave
like wheat on every soil in §2.4 and with both Quark mechanics in §2.2 and §2.3.

**Data, edited per crop:**

| Piece | Where | What it does |
|---|---|---|
| `creativeengineering:crops/farmland_soils` (block tag) | [blocktags.js](../kubejs/server_scripts/tags/blocktags.js), "Crop parity" | soils that count as farmland for crops whose own check is hard-coded. Today: farmland, Rich Farmland, Fertile Soil. Untilled Rich Soil stays out (G6). Vanilla farmland counts even if the tag is empty or not loaded yet |
| `creativeengineering:crops/farmland_crops` (block tag) | same | non-`CropBlock` plants treated as farmland crops: vanilla farmland under them doesn't revert to dirt, soaked Fertile Soil bone-meals them, Quark's hoe area break includes them. Today: tobacco |
| Quark `Harvestable Blocks` | [config/quark-common.toml](../config/quark-common.toml) `[tweaks.simple_harvest]` | one `"mod:block[age_prop=max]"` line per crop (optionally `,mod:block[age_prop=0]` for the replant state). Gives right-click and hoe right-click replant. Backed up in `configs_edited/root_config/` and the CreEngServer mirror |

**Code, written once** (patcherMod, `crops/CropParity` holds both tag keys and the two checks):

| Mixin | Target | Generic? |
|---|---|---|
| `FarmBlockUnderCropsMixin` | vanilla `FarmBlock.isUnderCrops` (`m_53250_`) | yes, tag-driven |
| `MulchBlockCropGrowthMixin` | IW `MulchBlock.randomTick` (`m_213898_`), head; reads IW's own mulch config | yes, tag-driven; skips `CropBlock`s (IW grows those) |
| `QuarkHoeHarvestMixin` | Quark `HoeHarvestingModule.canHarvest`, head | yes, tag-driven |
| `TobaccoPlacementMixin` | The Dirty Stuff's placement procedure | no: each crop with a hard-coded soil check needs its own small mixin that calls `CropParity.isFarmlandSoil` |

**Checklist for the next crop:**

1. Read its block class (`tools/decompile_mod.py`). Note how it checks soil, whether it's a `CropBlock`,
   `IPlantable` or `BonemealableBlock`, its age property and the max value.
2. Soils: if it uses `canSustainPlant` (most crops), Rich Farmland and Fertile Soil already work. If it
   checks the block below directly, add a placement mixin that calls `CropParity.isFarmlandSoil`.
3. If it isn't a `CropBlock`, add it to `crops/farmland_crops` (farmland drying, Fertile Soil growth,
   hoe area break) and add its ripe state to Quark `Harvestable Blocks` (replant). A `CropBlock` from
   a mod already gets all of this.
4. A crop with extra block-state properties needs one `Harvestable Blocks` line per combination,
   like cocoa's `facing`.
5. Add the crop's row to §2.1 and a CR- test.

### 2.7 Wild generation (where each crop spawns in the world)

Read from the decompiled jars and the pack's configs on 2026-10-03. **The biome lists below are the mods'
defaults.** Since §2.8 the pack moves most of these crops elsewhere: the rates, ground rules and side blocks here still
hold, but the biomes come from §2.8 (CEConfig `wildCrops` on, the default). The full biome list for every feature, plus a
crops-per-biome table, is generated into [crops_by_biome.md](crops_by_biome.md) by
`python3 tools/gen_crop_worldgen_table.py`.

**How a wild crop gets into a biome.** A placed feature reaches a biome in one of two ways:

- **The biome's own file lists it.** Terralith ships the file for every overworld biome, the vanilla
  ones included, so all the vanilla patches (berries, pumpkins, sugar cane, melons, jungle trees with
  cocoa) come from Terralith's lists. Hearths ships three Nether biomes and Nullscape the End ones.
- **A Forge biome modifier adds it**, by biome tag. Most crop mods do this. Farmer's Delight's own
  modifier type (`add_features_by_filter`, also used by Delightful and Corn Delight) can add a base
  temperature window and a deny list.

Most crop mods register their features in Java, so there's no JSON to edit. The rates below come from
the decompiled classes, and the rates that read a config say so. Every crop feature is placed at the
**surface** (heightmap) unless the table says otherwise, so none of them has a Y window of its own.
Only new chunks get changes. Existing chunks keep what they generated with.

Words used below: **rate** = patch attempts per chunk ("1/30" = one chunk in 30 on average). **Share**
= the part of the overworld surface covered by those biomes, with ocean biomes not counted
(post-tuning areas from `biome_config.json`; oceans are 41% of the overworld). Exact biome lists are in [crops_by_biome.md](crops_by_biome.md).

#### 2.7.1 Per-crop table

**Farmer's Delight wild crops** (`WildCropFeature`: 64 tries for the crop within 4 blocks, 64 for a
companion plant within 6, y ±3. Chances are in `farmersdelight-common.toml` `[world.*]`)

| Crop | Wild block | Biomes | Rate | Ground | Companion / also placed |
|---|---|---|---|---|---|
| Cabbage | `farmersdelight:wild_cabbages` | vanilla **Beach only** | 1/30 | sand | Sandy Shrub |
| Beetroot | `farmersdelight:wild_beetroots` (Sea Beet) | vanilla **Beach only** | 1/30 | sand | Sandy Shrub |
| Carrot | `farmersdelight:wild_carrots` | every overworld biome with base temperature 0.4 to 0.9 except Lush Caves and Mushroom Fields: 57 biomes (forests, plains, meadow, swamps, beach, river, 34 Terralith). Share 21% | 1/120 | `#minecraft:dirt` | Grass, plus a **Coarse Dirt floor** under the patch |
| Onion | `farmersdelight:wild_onions` | same 57 biomes as carrots | 1/120 | `#minecraft:dirt` | Allium |
| Potato | `farmersdelight:wild_potatoes` | temperature 0.1 to 0.3, not underground: 24 cold biomes (taigas, windswept hills, Terralith cold forests and mountains). Share 12% | 1/100 | `#minecraft:dirt` | Fern |
| Tomato | `farmersdelight:wild_tomatoes` | `#forge:is_hot/overworld` minus `#forge:is_wet`: 30 biomes (desert, savannas, badlands, Terralith arid and volcanic). Share 13% | 1/100 | `#farmersdelight:terrain` (dirt, grass, sand, red sand, mud...) | Dead Bush |
| Rice | `farmersdelight:wild_rice` | `#forge:is_wet/overworld`, not underground: 23 biomes (jungles, swamps, beach, Terralith wet). Share 11% | 1/20 | a full water source 1 deep with air above, on dirt or sand; 96 tries within 7 | none |
| Mushroom colonies | `farmersdelight:brown_mushroom_colony`, `red_mushroom_colony` (age 0 to 3) | `#forge:is_mushroom`: Mushroom Fields, Quark Glimmering Weald, Terralith Fungal Caves and Mirage Isles | 1/15 each | mycelium | brown / red mushrooms |

**Other Farmer's Delight add-ons**

| Crop | Wild block | Biomes | Rate | Ground | Companion / notes |
|---|---|---|---|---|---|
| Corn (Corn Delight) | `corn_delight:wild_corn` | same 57 biomes as wild carrots | 1/30 (hard-coded) | `#minecraft:dirt` | Grass and a Coarse Dirt floor, like carrots. Also village house and shipwreck chests |
| Tea (Farmer's Respite) | `farmersrespite:wild_tea_bush` | vanilla **Swamp only** | every chunk, but only 3 tries, so a bush or two | `#minecraft:dirt` | Fern |
| Coffee (Farmer's Respite) | `farmersrespite:wild_coffee_bush` | **Basalt Deltas** (Nether) | every chunk, 64 tries at a random height (y ±6) | basalt | **Wither Roots** |
| Cotton (Rustic Delight) | `rusticdelight:wild_cotton` | `#minecraft:is_forest`: forest, birch, old-growth birch, flower forest, dark forest, grove, Terralith Alpine Grove and Cloud Forest | 1/96 (`wild_cotton_chance`) | `#minecraft:dirt` | 32 tries within 6 |
| Bell Pepper (Rustic Delight) | `rusticdelight:wild_bell_peppers` | `#minecraft:is_jungle`: 3 vanilla jungles + 5 Terralith jungles | 1/15 (`wild_bell_peppers_chance`) | `#minecraft:dirt` | 48 tries within 4 |
| Coffee (Rustic Delight) | `rusticdelight:wild_coffee` | same 8 jungles | 1/15 (`wild_coffee_chance`) | `#minecraft:dirt` | 48 tries within 6. A different coffee item from Farmer's Respite's |
| Salmonberries (Delightful) | `delightful:wild_salmonberries` | `#delightful:salmonberry_spawns`: the same 8 forests as cotton | 1/35 | `#minecraft:dirt` | Grass |
| Mini Melon (Delightful) | `delightful:mini_melon` | plains, sunflower plains, meadow, snowy plains, Terralith Highlands, Alpine Highlands, Rocky Shrubland | 1/35 | `#minecraft:dirt` | 64 tries within 4 |
| Cantaloupe (Delightful) | `delightful:cantaloupe` | beach, snowy beach, Terralith Gravel Beach | 1/55 | `#delightful:cantaloupe_spawns` (dirt, grass, sand...) | 64 tries within 3 |
| Propelplant (Nether's Delight) | `nethersdelight:propelplant_*` | **Crimson Forest** | 8 per chunk, each at a random height | nylium | a cross-shaped 7x7 at the chosen height ±1, 1 in 3 spots: bushes, stems, canes, some with pearls |

Rustic Delight's three patches are flower-type features. Bone meal on grass in those forests and
jungles picks from the biome's flower features, so it can also grow wild cotton, peppers and coffee.

**Neapolitan** (biome tags `#neapolitan:has_feature/*`)

| Crop | What generates | Biomes | Rate | Ground | Also placed |
|---|---|---|---|---|---|
| Strawberries | `neapolitan:strawberry_bush`. 90% are red, ripe bushes (age 6). 10% are unripe bushes on Coarse Dirt | vanilla **Plains only** | 1/64, 512 tries within 5 | grass block or coarse dirt | **Coarse Dirt**: under the unripe bushes, and each neighbour of a bush has a 50% chance to turn |
| Vanilla | `neapolitan:vanilla_vine` growing sideways | Savanna, Savanna Plateau, Windswept Savanna (vanilla only) | 1/64 | the side of `#neapolitan:vanilla_plantable_on` (logs, leaves, dirt) | none |
| Adzuki Beans | `neapolitan:adzuki_sprouts`, ripe (age 6) | forest, birch forest, old-growth birch, flower forest, dark forest | 1/128, 256 tries within 3 | grass block | **the grass under each sprout becomes plain Dirt**. Breaking a ripe sprout turns the dirt or grass under it into **Adzuki Soil** (`neapolitan:adzuki_soil`), which grows a new sprout on its own (light 9+, 1 in 4 per random tick, or with bone meal). So the "adzuki soil" you see in the wild comes from a harvest; the worldgen itself only lays down dirt |
| Mint | `neapolitan:mint` (age 4, 1 to 4 sprouts) around a **mint pond** | windswept hills, windswept forest, windswept gravelly hills, Terralith Cloud Forest, Fractured Savanna, Haze Mountain, Jungle Mountains | 1/4 | flat grass with grass/dirt on all four sides | **a small pond (1 to about 9 water blocks)**, grass under the water turned to dirt, and often **one spruce tree** beside it |
| Banana | banana stalk 3 to 6 tall with fronds, sometimes a **Banana Bundle** | common: jungle, bamboo jungle (0.75 per chunk). Uncommon: sparse jungle (1/10). Rare: beach, stony shore (1/20) | see biomes | grass block, sand or gravel | **grass around the plant turns to Gravel**. With a bundle, 25% chance of a **chimpanzee group** (12 to 24 spawn attempts, up to 10). Bundles can release **Plantain Spiders** |

**Fruits Delight** (biome tags `#fruitsdelight:plants/*_biomes`; Terralith biomes are in them)

| Crop | What generates | Biomes | Rate | Ground |
|---|---|---|---|---|
| Blueberry | ripe `fruitsdelight:blueberry_bush` | 23 cold or wet biomes: taigas, snowy plains/taiga, grove, windswept, swamps, Terralith cold forests | 1/32, 20 tries | grass block |
| Cranberry | ripe `cranberry_bush` | 9: jungles, swamps, dark forest, Terralith jungles and Orchid Swamp | 1/32 | grass block |
| Lemon | ripe `lemon_tree` bush | 9: plains, sunflower plains, meadow, Terralith Blooming, Lavender and Moonlight biomes | 1/32 | grass block |
| Pineapple | `fruitsdelight:wild_pineapple` | vanilla **Beach only** | 1/32, 24 tries | sand |
| Hamimelon | hamimelon blocks (no stem) | desert + 5 Terralith deserts | 1/64, 24 tries | sand |

| Fruit tree | Trees per chunk | Trunk | Biomes |
|---|---|---|---|
| Lychee, Mango | 1/4 | jungle log | jungles (2 vanilla + 4 Terralith incl. Orchid Swamp) |
| Peach | 1/8 | jungle log | sparse jungle + 4 Terralith jungles |
| Bayberry | 1/15 | spruce log | swamps, Terralith jungles |
| Pear | 1/15 | birch log | birch forests, Terralith Blooming/Lavender/Moonlight |
| Apple, Fig, Kiwi, Orange | 1/20 | oak (kiwi: jungle) | forest, flower forest, Terralith Blooming/Lavender/Moonlight |
| Hawberry | 1/20 | spruce log | 16 cold biomes (taigas, Terralith cold forests) |
| Mangosteen | 1/40 | oak log | jungles and swamps |
| Persimmon | 1/50 | spruce log | 16 snowy biomes (snowy plains/taiga, grove, Terralith wintry) |
| Durian | 1/200 | jungle log | jungles and swamps |

Trees are placed at the surface on dry land only, where the sapling would survive. Only natural
(`persistent=false`) leaves flower and fruit.

**Vinery** (details in [vinery.md](vinery.md) §2.13): red/white grape bushes 1/92 in 7 vanilla
temperate biomes (forest, plains, swamp, birch, meadow, sunflower plains, river). Savanna and taiga
bushes 1/92 in the matching vanilla biomes. Jungle grape vines 1/5 in the jungles and wooded badlands.
Apple and cherry trees: 1 in 100 chunks in plains, sunflower plains, meadow and 9 Terralith biomes.
Bushes are placed at age 3 (taiga bushes at age 2), on grass blocks only. **No Terralith biome gets grape bushes.**

**Others**

| Crop | What generates | Biomes | Rate | Notes |
|---|---|---|---|---|
| Mulberries (Upgrade Aquatic) | ripe `mulberry_vine` under **river trees** | river, frozen river | about 2 trees per chunk | each leaf with air below has a 1 in 6 chance of a vine |
| Flax (Supplementaries) | `supplementaries:wild_flax` next to water | **none: switched off in the pack** (`supplementaries-common.toml` `[functional.flax.wild_flax] enabled = false`, the hand-edited copy in `configs_edited/`) | would be 1/6, 35 tries | seeds come from chest injects (dungeon, mineshaft, pillager, shipwreck), urns and villager trades |

**Vanilla crops** (via Terralith's biome files)

| Crop | Biomes | Rate | Notes |
|---|---|---|---|
| Sweet Berries | 17 biomes: taiga, both old-growth taigas, dark forest, Terralith cold forests, Shield, Yellowstone, Yosemite. Snowy Taiga: rare | 1/32 (rare 1/384) | ripe bushes on grass blocks, 96 tries |
| Pumpkin | 118 overworld biomes (nearly all land). Share 52% | 1/300 | 96 tries on grass blocks. Skylands: 1/4 |
| Melon | jungle, bamboo jungle and 5 Terralith jungles (Terralith's own patch, 1/6). Sparse jungle: 1/64 | 1/6 | vanilla's `patch_melon` isn't used by any biome |
| Sugar Cane | 103 biomes 1/6. Desert-type: every chunk. Swamps: 1/3. Badlands: 1/5. Terralith cherry biomes: every chunk | see biomes | dirt or sand next to water |
| Cocoa | on vanilla-style jungle trees in jungle, jungle edge, Terralith Violet and Hawaii-style coastal trees, skylands summer | 1 in 5 jungle trees | Bamboo Jungle's trees are mega jungle trees: no cocoa |
| Glow Berries | Lush Caves (188 vine attempts per chunk under ceilings), Terralith Underground Jungle, Skylands Summer | see biomes | below Y 256 for the cave biomes |
| Chorus Fruit | End Highlands (Nullscape's chorus features) | noise-based | |
| Bamboo, Cactus | jungles; deserts and badlands | vanilla rates | not crops, listed in crops_by_biome.md |

**No wild generation at all:** wheat (seeds from grass, village farms), hemp (IE: 10% from grass and
tall grass, not with shears), tobacco (villager trade and the mod's chest table), Ender Crop (crafted
seeds), Nether Wart (fortresses), Floral Enchantment flower bushes (crafted), flax (switched off, above),
Quark ancient fruit (ancient city chests), Alex's Mobs bananas (drop from jungle leaves; a separate item
from Neapolitan's).

#### 2.7.2 Everything else crop generation puts in the world

| Placed by | Block | Where |
|---|---|---|
| Wild carrots, wild corn | Coarse Dirt floor (64 tries within 6 blocks: any `#minecraft:dirt` block with a replaceable block above, so grass blocks too) | under and around the patch |
| Wild cabbages, sea beets | Sandy Shrub | around the patch, on sand |
| Wild onions / potatoes / tomatoes | Allium / Fern / Dead Bush | around the patch |
| Wild carrots, corn, salmonberries | Grass | around the patch |
| Wild tea | Fern | around the bush |
| FR coffee | Wither Roots | on basalt around the bushes |
| Mushroom colonies | Brown / Red Mushroom | around the colonies |
| Strawberries | Coarse Dirt | under unripe bushes and next to bushes |
| Adzuki | Dirt (grass removed); Adzuki Soil after a harvest | under each sprout |
| Mint pond | Water, Dirt, a Spruce tree | the pond and its rim |
| Banana plant | Gravel ground, Banana Bundle, Chimpanzees, Plantain Spiders (from bundles) | around the plant |
| Fruit trees (Fruits Delight) | vanilla oak/birch/spruce/jungle logs + fruit leaves; durians fall as blocks when ripe | tree |
| Vinery trees | Vinery apple/cherry logs and leaves | tree |
| River trees (UA) | river logs and leaves | river banks |
| Propelplant | propelplant stems and canes | crimson nylium |

#### 2.7.3 Heights

- Every overworld crop feature uses the **surface heightmap**, so it follows the terrain at any Y.
  Neither the mods nor the pack add Y limits. Mountain biomes get crops on their peaks if the biome is
  in the list.
- Trees and river trees use the ocean-floor heightmap plus a "no water above" filter: dry land only.
- Random height: FR coffee (anywhere in Basalt Deltas, y ±6 around the pick) and propelplant (8 random
  heights per chunk in Crimson Forest, then only on nylium within ±1 of each).
- Caves: glow berries attempt below Y 256 under a solid ceiling. Terralith Skylands Summer glow berries
  are at Y 130 to 160.
- Neapolitan's `White Strawberry minimum height`: the pack sets 192 (2026-10-22; Neapolitan's default 256 meant none
  formed in normal terrain). A strawberry bush at Y 192 or higher grows white strawberries, wild or planted; only the
  upper slopes of the highland biomes that carry wild strawberries reach that height. The item's tooltip says so.

#### 2.7.4 Other vegetation mods (not crops)

Scope for the planned vegetation system, not yet broken down per biome:

| Mod | What it adds to the world | How |
|---|---|---|
| Terralith | every overworld biome's trees, flowers, grass and the vanilla patches above | biome files |
| Geophilic Reforged | bushes (oak, birch, spruce, acacia, azalea), fallen trees, tree stumps, extra grass, coarse dirt cover, sparse bamboo and dead bushes, moss, rocks | biome modifiers on `#geophilic_reforged:gets/*` and `wood_type/*` tags |
| Quark | blossom trees (by `forge:is_snowy`, `forge:is_swamp`... tags, rarity 100 to 200), Glimmering Weald, chorus vegetation (End), fairy rings | Quark config `[world.*]` |
| Upgrade Aquatic | beachgrass dunes, searocket (beach), flowering rush (river), pickerelweed, river trees, driftwood (pack: x0.25) | biome modifiers |
| Buzzier Bees | buttercups, pink and white clover | biome modifiers |
| Botania | mystical flowers and mushrooms | biome modifiers |
| Immersive Weathering | moss and ivy patches, humus, fluvisol, cryosol and similar soil patches | biome modifiers |
| Twigs | twigs, pebbles, seashells, azalea flowers | biome modifiers |
| Myrtrees | rubber trees, 1 in 20 chunks in `#minecraft:is_jungle`, `is_forest`, `is_savanna` and swamp-hut biomes | Architectury biome modification (Java) |
| Mystical Oak Tree | the Wise Oak | biome modifier, all overworld |
| Hearths, Nullscape | Nether forest fungi and stems; End vegetation | biome files |
| Bushier Flowers | switched off by the pack (memory note `bushier-flowers-disabled`) | none |

### 2.8 Pack wild-crop placement (`wild_crops.json`)

**Status: BUILT 2026-10-03, offline-tested (`tools/test_wild_crops.sh`), DEPLOYED with 1.0.4; rules revised
2026-10-10 (DEPLOYED with 1.0.6) and 2026-10-22 (tea, grapes, flax; not deployed); not looked at in game.** Your 2026-10-03 spec
decides which biomes each wild crop and fruit tree goes to. The mods' own rates stay; only the biomes change. §2.7
describes what the mods do on their own, which is what you get with the switch off.

**How it works**

- `kubejs/generation_scripts/worldgen/wild_crops.json` gives every overworld biome a list of **traits** (climate
  `cold`/`temperate`/`warm`, `wet`/`dry`, `snowy`, ground such as `grassy`/`sandy`/`terracotta`, and shapes such as
  `forest`/`mountain`/`beach`/`cliff`/`swamp`/`jungle`). Each crop has a **rule** over those traits, for example
  `"warm wet !cliff"` or `["sandy beach", "cliff"]`. A list means "any of these", `!` means "not", and `add`/`except`
  name biomes directly. Ocean and cave biomes never match a rule. The traits come from Terralith's `c:` and `forge:`
  climate tags, base temperature and downfall, and the trees and ground each biome file places. Judgment calls are
  listed under "Choices made" below.
- `kubejs/server_scripts/worldgen/wild_crops.js` applies it at world load:
  - fills the biome tag `creativeengineering:wild_crops/<crop>`;
  - replaces each mod's own biome modifier for that crop with `forge:none` (44 of them);
  - adds one `forge:add_features` modifier per crop (46).
  - Vanilla patches that Terralith's biome files list (sweet berries, pumpkins, melons, sugar cane) are removed from
    every overworld biome by one `forge:remove_features` modifier, and the pack's copies (same configured feature,
    same rate) are added where the rule says. Forge removes after it adds, so re-adding the original id wouldn't work.
  - A feature of a mod that isn't loaded is skipped, because an unknown id stops the world loading.
  - On world load it checks the live tags against the rules and lists any overworld biome the config doesn't
    classify.
- Switch: CEConfig `[worldgen.systems] wildCrops` (patcherMod 1.0.4, rebuilt 2026-10-03). Off means every crop spawns
  where its mod puts it. Rejoin to apply. Only new chunks change.
- `python3 tools/gen_crop_worldgen_table.py` evaluates the same rules. [crops_by_biome.md](crops_by_biome.md) §3
  has each rule's exact biome list, §1 the before/after density per crop, §5 every biome with its traits and
  crops, and §6 the coverage checks.

**Pack features** (`kubejs/data/creativeengineering/worldgen/{configured,placed}_feature/wild/`). These exist
where a mod's ground test would block the spec:

| Feature | Why | Ground |
|---|---|---|
| `wild/cabbages`, `wild/beetroots` | FD's own test for sand only; cliffs are stone with grass tops | sand or `#minecraft:dirt` (FD's wild crop block survives on both); Sandy Shrub companions still need sand |
| `wild/brown_mushroom_colonies`, `wild/red_mushroom_colonies` | FD's own need mycelium; dark forests have podzol and grass | `#minecraft:mushroom_grow_block` or `#minecraft:dirt`. The colony block itself still needs light below 13 off mycelium/podzol, and the heightmap ignores leaves so the attempt lands under the canopy |
| `wild/hamimelon` | Fruits Delight's own wants plain sand; badlands are red sand and terracotta | `#minecraft:sand` or `#minecraft:terracotta` |
| `wild/cocoa_jungle_tree` | cocoa only grows on vanilla jungle trees (CocoaDecorator 0.2) | a vanilla jungle tree in 1 chunk of 3, dry land, where a jungle sapling survives |
| `wild/sweet_berries`, `wild/pumpkin`, `wild/melon`, `wild/sugar_cane` | copies of the vanilla patches (see above) | vanilla's: grass for berries, pumpkins and melons; dirt/sand next to water for cane |

Same rarities as the mods: FD's config chances as the pack sets them (30, 15), Fruits Delight's 64, vanilla's 32, 300
and 6. A change to `farmersdelight-common.toml` `[world.wild_cabbages]` or `[world.wild_beetroots]` no longer
reaches these two crops; edit the placed feature json instead.

**Where each crop goes** (exact lists: [crops_by_biome.md](crops_by_biome.md) §3, generated from the current `wild_crops.json`;
rows corrected 2026-10-21 to the 2026-10-10 rules)

| Crop | Rule | Biomes |
|---|---|---|
| Cabbage, Sea Beet | sandy beaches + cliffs | 7: beach, snowy beach, stony shore, basalt/frozen/granite/white cliffs |
| Carrot, Onion | temperate grassy, not swamp | 25: plains, meadow, every temperate forest (birch, dark, flower), Terralith flower/sakura/lavender/moonlight, steppe, clearings, Yosemite, skylands spring/autumn |
| Sweet Berries | cold grassy (snowy too) | 33: taigas, groves, snowy plains/taiga, windswept hills/forest, Terralith cold forests, highlands, shrublands, Yellowstone, Ice Marsh |
| Potato, Salmonberries | cold grassy, not snowy | 15 |
| Blueberry | cold grassy snowy | 18 |
| Pumpkin | cold grassy forest, not snowy + "autumn" | 11: taigas, windswept forest, birch taiga, cloud forest, forested highlands, haze mountain, shield, siberian taiga, Yellowstone, skylands autumn |
| Melon, Mini Melon, Vanilla, Rice, Bell Pepper, Coffee (RD) | warm wet, not cliff | 11: the 8 jungles, mangrove and orchid swamps, skylands summer |
| Cocoa | warm wet, not cliff, minus biomes that already grow cocoa trees | 3 new: bamboo jungle, mangrove swamp, orchid swamp (the 8 others keep their own trees) |
| Sugar Cane | rivers + beaches | 6: river, warm river, frozen river, beach, snowy beach, gravel beach |
| Tomato, Cotton, Cantaloupe, Corn | warm dry grassy | 12: savannas (plateau, windswept, slopes, ashen, fractured, badlands), wooded badlands, arid highlands, brushland, shrublands |
| Brown/Red Mushroom Colony | mushroom + dark forest | 2: mushroom fields, dark forest |
| Tea | temperate wet grassy + every swamp + every jungle + cherry (sakura) + dark forest, Lush Valley, Cloud Forest (2026-10-22) | 20: swamp, mangrove, orchid swamp, ice marsh, the 9 jungles, sakura grove and valley, dark forest, Lush Valley, Cloud Forest, Blooming Valley, Mirage Isles |
| Strawberries | cold grassy mountain, not snowy | 6 |
| Adzuki | temperate grassy forest | 18 |
| Mint | cold grassy mountain | 11 |
| Banana | warm wet forest | 9: common rate in jungle, bamboo jungle, tropical jungle, amethyst rainforest; uncommon rate in the other 5 |
| Lemon | warm wet forest | 9 jungles |
| Cranberry | cold wet grassy | 5: Caldera, Cloud Forest, Ice Marsh, Siberian Taiga, Yellowstone |
| Pineapple | warm beach + warm wet coastal | 4: beach, mangrove swamp, granite cliffs, tropical jungle |
| Hamimelon | warm sand or terracotta desert | 15: deserts, badlands, Terralith canyons/oases/mesas, painted mountains |
| Lychee, Mango, Peach, Orange, Kiwi trees | jungle | 9 jungles |
| Bayberry, Mangosteen, Durian trees | swamp, not cold | 3: swamp, mangrove, orchid swamp |
| Pear tree | birch | 3: birch forest, old-growth birch, birch taiga |
| Apple tree (Vinery only; Fruits Delight's apple modifier is off) | oak forest | 6: forest, flower forest, alpha islands (+ winter), temperate highlands, Yosemite lowlands |
| Fig, Hawberry trees | cold forest, not snowy | 10 |
| Rubber tree (Myrtrees, pack copy `wild/rubber_tree`) | jungle | 9 jungles |
| Cherry tree (Vinery) | mountain forest | 15 |
| Persimmon tree | snowy forest | 13 |
| Grapes, red and white bushes (Vinery, 2026-10-22) | temperate grassy, not swamp, not dark oak | 23: plains, meadow, forests, flower and birch forests, the lavender, moonlight, sakura, blooming and Yosemite biomes, steppe, valley clearing, alpha islands, Mirage Isles, skylands spring and autumn |
| Grapes, savanna bushes (Vinery) | warm dry grassy | 12: the savanna group (as tomato) |
| Grapes, taiga bushes (Vinery) | cold grassy forest, not snowy | 10: taigas, windswept forest, Cloud Forest, forested highlands, Haze Mountain, Lush Valley, Shield, Yellowstone |
| Grapes, jungle vines (Vinery) | jungle + warm wet swamp | 11: the 9 jungles, mangrove and orchid swamps |
| Wild flax (Supplementaries) | none: modifier off (2026-10-22) | 0 |

**Cold wet biomes (for cranberries):** Terralith Caldera (crater lake, tagged wet), Cloud Forest (misty mountain forest),
Ice Marsh (the cold swamp), Siberian Taiga (downfall 1.2, the wettest cold biome) and Yellowstone (hot-spring wetlands,
tagged wet). The rest of the cold biomes are moderate (taiga downfall 0.8) or dry.

**Side changes in the same pass**

- Oak and dark oak leaves no longer drop apples (`kubejs/server_scripts/loot/08_leaf_apples.js`, pre-roll like the dead
  maps). Myrtrees rubberwood leaves too since 2026-10-21 (V17).
- Alex's Mobs bananas no longer drop from jungle leaves: `config/alexsmobs.toml` `bananasDropFromLeaves = false`
  (backed up in `configs_edited/root_config/`).
- Farmer's Respite coffee no longer spawns in Basalt Deltas (its modifier is off, V18).
- Neapolitan's beach bananas (rare tier) are off.
- Vinery's cherry tree keeps its own biomes. Vinery's apple tree shares that biome tag, so only the apple modifier was
  replaced.
- Untouched: cherry trees, mulberries, glow berries, chorus, propelplant, bamboo, cactus.

**Choices made** (each is one line in `wild_crops.json`; say if you want it different)

- "Cold" includes snowy biomes unless the rule says `!snowy` (since 2026-10-10: pumpkin, potato, salmonberry, strawberry,
  fig and hawberry; blueberry is `snowy` only). This follows from your "cold non-snow
  forests" for pumpkins.
- "Warm wet" counts the jungles, mangrove and orchid swamps and skylands summer. It leaves out granite cliffs and the
  oases: Terralith tags those wet, but they are a cliff and two deserts.
- "Cliff biomes" means the coastal cliffs plus stony shore. Yosemite Cliffs is an inland mountain and is left out.
- "Sand beaches" includes the snowy beach (sand under snow). Gravel beach is left out for cabbage and beet, but it is in
  for sugar cane ("beaches").
- "Pumpkin biomes / orchards": the pack has no orchard biome (the only hit is a BYG leaf file in Separated Leaves, and
  BYG isn't installed). Terralith Skylands Autumn, which already has its own pumpkin patch, stands in. V16.
- "Strawberries in grassy mountains": since 2026-10-10 the rule is `cold grassy mountain !snowy` (6 biomes); mint is
  `cold grassy mountain` (11).
- "Swamps" for bayberry, mangosteen and durian is `swamp !cold` since 2026-10-10, so Ice Marsh is out.
- "Dark forests" means `minecraft:dark_forest` only. Temperate Highlands has some dark oaks but is a mountain.
- Mirage Isles is tagged `forge:is_mushroom` but has no mycelium, so it counts as a temperate wet forest, not a mushroom
  biome. Fungal Caves and the Glimmering Weald are caves, where a surface feature never lands.

#### 2.8.1 Immersive Weathering soils (`tools/gen_soil_compat.py`)

**Cause.** Immersive Weathering lays its soils at `raw_generation`, before any vegetation: humus in Dark Forest, fluvisol
and silt in `minecraft:swamp` + `#c:swamp` (Terralith Ice Marsh, Orchid Swamp), vertisol over red sand in badlands and
desert. All of them are in `#minecraft:dirt`, so the plants survive on them (that's why you can plant there), but many
worldgen features test the block below with `matching_blocks: minecraft:grass_block` and skip every soil block. Cryosol is
different: it is placed at `top_layer_modification`, after vegetation, and only over snow blocks, so no plant ever sees it
during generation.

Blocked before the fix, where it mattered: adzuki on Dark Forest humus; cranberry, blueberry and sweet berries on Ice Marsh
fluvisol; melon and Terralith's orchid flowers in Orchid Swamp; hamimelon on vertisol. Features that test `#minecraft:dirt`
(FD carrots/onions/potatoes, Rustic Delight, Corn Delight, tea, salmonberry, mini melon, the pack's cabbage/beet/mushroom
copies), plain `simple_block` flowers and grass, and trees that only check `would_survive` a sapling already worked.

**Fix.** Block tag `creativeengineering:worldgen/grass_like_soils` (grass block + the six soils, permafrost left out;
[blocktags.js](../kubejs/server_scripts/tags/blocktags.js)). `tools/gen_soil_compat.py` scans every mod jar for
placed/configured features whose below-block test lists `grass_block`, and writes a `kubejs/data` override where that test
becomes `any_of(original list, #grass_like_soils)`; nothing else in the file changes. 65 features: vanilla pumpkin/melon/
berry patches (Terralith's versions), 6 Vinery grape bushes, 2 Geophilic grass patches, ~50 Terralith flower/grass/tree/
mushroom features, plus Neapolitan strawberry + adzuki patches and Fruits Delight blueberry/cranberry/lemon patches (those 5
are registered in Java; the script rebuilds them from the decompiled source). Snow-layer, lake and snow-removal features are
left alone (they shape terrain). The pack's own `wild/hamimelon` takes the tag directly. A manifest
(`kubejs/generation_scripts/worldgen/soil_compat_manifest.json`) lists the files it owns; rerunning removes stale ones and
never overwrites a hand-written file. Rerun after updating Terralith, Vinery, Neapolitan, Fruits Delight or Geophilic
(`/cegen run soil_compat`).

Vinery grapes (2026-10-23): the Backported Vinery port has no worldgen JSON in its jar; it serves 1.5.5's at runtime
from the donor jars in `vinery_upstream/` as a bottom-priority pack, so the KubeJS overrides win. The script reads the
grape bush patches from that donor jar (`DONOR_JARS`, the six bush ids only; other 1.5.5 worldgen is rewritten by the
port and must not be copied raw). Before this the six overrides were 1.3.10 leftovers (36 tries; 1.5.5 has 32 and 33)
and the next run would have deleted them. The Vintage salt patch is the 66th feature.

Not reachable from JSON: Neapolitan's mint pond and banana plant test `grass_block` inside the Java feature. Neither spawns
in a biome that has an IW soil, so they are left alone. Side effects: an adzuki patch on humus turns the block under each
sprout into plain Dirt (same as it does to grass); hamimelon can now also land on the grass in Wooded Badlands.

**Tested 2026-10-05 on a scratch dedicated server** (the instance's mods + kubejs): the world loads with all 65 overrides;
`/place feature` of 12 features (pumpkin, berries, melon, adzuki, strawberry, cranberry, blueberry, lemon, white grape,
Terralith plains grass, Geophilic plains grass, hamimelon) succeeded on grass and all six soils and failed on stone and
permafrost. With the tag cut down to grass only (`/reload`), all 12 failed on every soil, which was the behaviour before
the fix.

### 2.9 WTHIT growth line (`crop_growth.json`)

WTHIT's own "Growth: 60%" / "Growth: Mature" line comes from `PlantProvider` and covers only `CropBlock` subclasses
(wheat, carrots, corn, cabbage, onions, tomatoes, flax, rice panicles...) plus five vanilla blocks matched by
identity: melon and pumpkin stem, nether wart, cocoa, sweet berry bush. A modded stem or berry bush is a different
block, so it gets nothing. Every other plant in the pack was silent. The pack adds the same line, same wording, same
"Crop progress" toggle in WTHIT's config, for all of them (patcherMod `crops/WthitCropGrowth`, rules in
`assets/creativeengineering/crop_growth.json`). Plants you can pick before they finish read
"Growth: 75% (Harvestable)" until fully grown.

**How the catalog was made:** `/dumpcropgrowth` (in `/dumpall`) walks every registered block and writes every one
with a stage-like blockstate property to `kubejs/exported/crops/crop_growth_audit.json`: 290 blocks on 2026-10-04.
22 are WTHIT-native, 62 got a rule, 206 are not crops (below). `python3 tools/crop_growth_coverage.py` checks that every
audited block is ruled, covered by the fallback or has a skip reason, and that each rule's property, stage names and
numbers fit the real block states; a plant from a newly added mod shows as UNKNOWN until it gets a rule. The 2026-10-18
audit (300 blocks) had 10 UNKNOWN, all from the Vinery port: its lattice vines (the port's own WTHIT plugin shows the grape
and ripeness) and its grass slab, both skip reasons since 2026-10-20 (0 UNKNOWN).

**Confirmed silent before the change, all now covered:** hemp, tobacco, strawberries, salmonberries, mint, grapes,
blueberries, cranberries, lemons.

**Plants that were missing the line (62 blocks)**

| Plant | Block(s) | State read | Shows "Harvestable" from | Fully grown |
|---|---|---|---|---|
| Hemp | `immersiveengineering:hemp` | enum `growth`: bottom0..bottom4, then `top0` | none | bottom4 and the top half |
| Tobacco | `the_dirty_stuff:tobacco_seeds` | `blockstate` 0-7 | none | 7 |
| Strawberries | `neapolitan:strawberry_bush` | `age` 0-6 | none | 6 |
| Mint | `neapolitan:mint` | `age` 0-4 | none | 4 |
| Adzuki | `neapolitan:adzuki_sprouts` | `age` 0-6 | none | 6 |
| Salmonberries | `delightful:salmonberry_bush` | `age` 0-4 | 3 | 4 |
| Blueberries, cranberries | `fruitsdelight:blueberry_bush`, `cranberry_bush` | `age` 0-4 | none | 4 |
| Lemon (both halves) | `fruitsdelight:lemon_tree` | `age` 0-4 | none | 4 |
| Pineapple | `fruitsdelight:pineapple` | `age` 0-4 | none | 4 |
| Hamimelon stem (modded stem, not WTHIT's vanilla pair) | `fruitsdelight:hamimelon_stem` | `age` 0-7 | none | 7 |
| Grape bushes: red, white, savanna, taiga (8 blocks) | `vinery:*_grape_bush` | `age` 0-3 | 2 | 3 |
| Jungle grapes (wall vine, 2 blocks) | `vinery:jungle_grape_bush_red`, `_white` | `age` 0-3 | none | 3 |
| Grapevine stem | `vinery:grapevine_stem` | `age` 0-4, age 0 = empty stem, no line | none | 4 |
| Grapevine lattice (jungle grapes) | `vinery:grapevine_lattice` | `age` 0-4, age 0 = empty, no line | 3 | 4 |
| Mulberry vine | `upgrade_aquatic:mulberry_vine` | `age` 0-4 | none | 4 |
| Flower bushes (17 kinds) | `floralench:*_bush` | `age` 0-3 | 2 (flowers) | 3 |
| Tea bush (both halves) | `farmersrespite:tea_bush` | `age` 0-3 | none | 3 |
| Coffee stem, middle stem | `farmersrespite:coffee_stem`, `coffee_stem_middle` | `age` 0-2 | none | 2 |
| Coffee double stem | `farmersrespite:coffee_stem_double` | `age` + `age1` (two berries, summed, 0-4) | none | both berries at 2 |
| Rice (bottom block) | `farmersdelight:rice` | `age` 0-3 | none | 3 (the panicles grow on top, WTHIT-native) |
| Budding tomatoes | `farmersdelight:budding_tomatoes` | `age` 0-4 | none | 4 (turns into `tomatoes`, WTHIT-native) |
| Mushroom and fungus colonies (4) | `farmersdelight:*_mushroom_colony`, `nethersdelight:*_fungus_colony` | `age` 0-3 | none | 3 |
| Fruit tree leaves, natural only (12) | `fruitsdelight:*_leaves` | enum `type`: leaves 0%, flowers 50%, fruits = fully grown | none | fruits |
| Durian leaves, natural only | `fruitsdelight:durian_leaves` | enum `fruit`: none, flowers, small, fruits | none | fruits |

- "Harvestable from" is the stage where the mod's own right-click already gives something (salmonberry stage 3, grapes
  stage 2, flowers stage 2); the thresholds come from §2.1 and [vinery.md](vinery.md). A plant with "none" only reads
  "Growth: Mature" at the end, like wheat.
- Fruit leaves you place yourself (`persistent = true`) never flower, so they get no line; the fruit-tree leaves only
  show it while natural. A natural leaf in the plain `leaves` stage reads "Growth: 0%".
- WTHIT-native and unchanged (22 blocks): wheat, carrots, potatoes, beetroots, cabbages, onions, tomatoes (plain,
  rope and stick), corn, flax, ender crop, bell peppers, RD coffee, cotton, rice panicles, weeds, melon and pumpkin
  stems, nether wart, cocoa, sweet berries. The plugin skips them so nothing gets two lines.
- A bush-type block (`BushBlock`, not a sapling) with an integer `age` that no rule lists still gets a line,
  fully grown at its top age (`[crop_growth] fallback`). Today it catches nothing; it is for mods added later.

**Not shown on purpose (206 blocks, each with a reason in the coverage tool):**

| Why | Examples |
|---|---|
| Saplings, `stage` 0-1 is the tree-growth roll (WTHIT shows nothing for vanilla saplings either) | all 32 saplings, propagule |
| Leaves, `distance` to a log | 22 plain leaves |
| Immersive Weathering leaf and flower piles, `layers` | 37 piles |
| Trellises, flower patches, hanging pot, sea pickle: a decoration or item count, not growth | 41 |
| Weathering state of cobblestone, stone bricks and their slabs, stairs and walls | 45 |
| Column plants and vines: `age` is a growth counter with nothing to pick by stage | sugar cane, cactus, bamboo, kelp (6), cave, twisting and weeping vines, chorus flower |
| Sliced melons and pumpkin, containers (cookie jar, Vinery basket, grapevine pot), fire, frosted ice, skunk spray, ender residue, ivy, grass spread counters, unripe cheese wheels, big dripleaf | the rest |

- **Not covered, no state to read:** Farmer's Respite `coffee_bush` and `small_tea_bush` (first stage, no blockstate
  property), the wild plants (no age at all). Cave vines carry their berries as a boolean.
- **Rendering of the percentage:** rounded like WTHIT (`2 of 3` = 67%), never 100% before the plant is fully grown.

**Knobs:** `[crop_growth]` in `config/creativeengineering-common.toml`: `enabled`, `fallback`, `showHarvestable`;
WTHIT's own "Crop progress" option also hides the added lines. To change which plants or stages count, edit
`crop_growth.json`; the copy in the jar is the default, a copy at
`kubejs/assets/creativeengineering/crop_growth.json` overrides it (F3+T reloads it).

### 2.10 Extra stem fruit

Vanilla stems stop at one fruit: when a stem grows its fruit it becomes the attached stem, which has no random tick, and
when that fruit is taken it turns back into a fully grown plain stem that grows a new one. The pack lets an attached stem
grow more fruit on its other sides. Applies to every `AttachedStemBlock`: melon, pumpkin and Fruits Delight's hamimelon.

- **Roll:** on each random tick of an attached stem, vanilla's stem growth roll (1 in 25/speed + 1, speed from soil,
  hydration and the row bonus, light 9 or more) and Forge's crop-grow event, fired with the attached stem's state.
  Serene Seasons looks the block id up in its season tags, which list `melon_stem`, `pumpkin_stem` and
  `fruitsdelight:hamimelon_stem` but no attached stem, and treats unlisted blocks as always fertile: so the first fruit
  follows the seasons (the plain stem rolls it) but the extra fruits grow in any season (finding F-SS1, §4).
- **Where:** a random free side: empty, on farmland, dirt or any soil that takes the stem (vanilla's check). Fruits next to
  the stem of the same kind count toward the cap, even ones placed by hand.
- **Minimum wait:** since the stem's last fruit. 2nd fruit 10 minutes after the 1st, 3rd 20 minutes after the 2nd, 4th 40
  minutes after the 3rd (game time, 1200 ticks a minute; counts while the chunk is unloaded too). The clock starts on the
  attached stem's first random tick, so the first wait starts about a minute after the first fruit grows (random ticks
  come every 68 seconds on average at `randomTickSpeed` 3). Without the wait a 2nd fruit would come about as fast as the
  first (a few minutes on hydrated farmland).
- **Harvesting:** when the fruit the stem points at is taken and another of its fruits is still next to it, the stem
  points at that one instead of turning back into a plain stem. The wait keeps counting from the last fruit, so after
  harvesting one of four the next one comes after the 40 minute wait. With all fruits taken the stem turns back into a
  plain stem (vanilla), its clock is forgotten, and the next first fruit follows vanilla.
- **Saved state:** the last-fruit game time per attached stem, in each dimension's
  `data/creativeengineering_stem_fruit.dat`. A stem's entry goes when the stem is broken or turns back into a plain stem.
- **Knobs:** `[stem_fruit]` in `config/creativeengineering-common.toml`: `enabled`, `maxFruits` (1-4, default 4),
  `secondFruitMinutes` (10), `thirdFruitMinutes` (20), `fourthFruitMinutes` (40). Read on every tick; no restart needed.
- **Code:** patcherMod `crops/StemExtraFruit.java` (logic, saved data) and `mixin/AttachedStemExtraFruitMixin` (adds
  the random tick and the removal hook to `AttachedStemBlock` under their SRG names, and the re-point on `updateShape`).
  The attached stem always counts as random ticking, even with `enabled = false`, since chunk sections cache the
  count of ticking blocks; a ticking stem that does nothing costs next to nothing.
- **Own fruit only** (fixed 2026-10-22, scratch run SQ-SF): an attached stem grows and re-points only at its own
  fruit. Delightful's melon slices and cantaloupe blocks extend the vanilla blocks and report the vanilla stem, so the
  first build could grow them as extra fruit, which re-pointing (it only knew the real fruit) could not follow.

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
- **Wild crop placement (built 2026-10-03, DEPLOYED with 1.0.4, rules revised for 1.0.6; not looked at in game):** §2.8. Also oak/dark oak leaves drop no apples,
  Alex's Mobs leaf bananas are off (`config/alexsmobs.toml`) and Farmer's Respite's Basalt Deltas coffee is off.
- **Bell pepper colours (built 2026-10-21, not deployed):** the ripe Rustic Delight roll was red 6 : green 1 : yellow 1
  plus 15% bonus green and yellow (red 58% of peppers). [09_crop_drops.js](../kubejs/server_scripts/loot/09_crop_drops.js)
  (pre-roll, like the leaf apples) sets the roll to 1 : 1 : 1 and appends a 15% bonus red pool, so each colour averages
  0.483 per ripe harvest. Pools are matched by their items, existing positions kept. Wild bell peppers stay red-only
  (forage hint). TODO `farming/crops #9`, CR-69, CR-70.
  - Knobs: CEConfig `[crop_drops.bell_peppers]` (patcherMod 1.1.01, server side, `/reload` applies):
    `enabled` (false = the mod's table untouched), `redWeight` / `greenWeight` / `yellowWeight` (0-100, default 1),
    `redBonusChance` / `greenBonusChance` / `yellowBonusChance` (0-1, default 0.15). Colour X per ripe harvest =
    weightX / sum of weights + bonusChanceX. The mod's own split is 6 / 1 / 1 with red bonus 0. Read through
    `global.NCH.cfg(..., 'CROP_DROPS')`; without patcherMod the script uses the same defaults.
  - Other colours: Rustic Delight **1.7.0** (July to September 2026, Forge 1.20.1, NeoForge / Fabric 1.21.x and 26.x;
    the 1.7.0 changelog, not the GitHub wiki or the Modrinth page) added Pale seeds (white, pink, orange) and Dark seeds
    (black, blue, purple), wild Pale / Dark peppers in jungles, giant pepper blocks, slices, rolls and the Bell Pepper
    Medley feast. 1.3.0 (2024-11-15) is the last 1.19.2 build, so none of it is in the pack; having them would be our own
    backport of those items.
- **Extra stem fruit (built 2026-10-22, patcherMod 1.1.01 in `build/libs` only, not tested):** up to 4 fruits per melon, pumpkin or
  hamimelon stem, each later one after a longer minimum wait (§2.10). CR-71 to CR-75.
- **Crop parity layer (built 2026-10-02, patcherMod 1.0.4, DEPLOYED with 1.0.4):** tobacco gets farmland
  soils, right-click and hoe replant and the hoe area break. See §2.6 for the tags, the Quark config line
  and the four mixins.

## 4. Limits and decisions

G1 to G3 are fixed (§2.6). The rest are open, each with a suggested fix for you to approve or reject:

- **G1 Tobacco has no right-click replant.** FIXED 2026-10-02: Quark `Harvestable Blocks` line
  `the_dirty_stuff:tobacco_seeds[blockstate=7]`, which also gives hoe-area replant.
- **G2 Tobacco refuses Rich Farmland and Fertile Soil.** FIXED 2026-10-02: `TobaccoPlacementMixin` with
  the `crops/farmland_soils` tag. Untilled Rich Soil stays out, as for every farmland crop. Also fixed
  while there: dry farmland under tobacco no longer reverts, and soaked Fertile Soil now grows it.
- **G3 Tobacco isn't caught by the hoe area break.** FIXED 2026-10-02: `QuarkHoeHarvestMixin` with the
  `crops/farmland_crops` tag.
- **G4 Hemp has no right-click.** That's IE's design: break the top and the bottom regrows it. The
  hoe area break on the top row already works as a harvester. No change suggested.
- **G5 Pack hoes are 3x3.** FIXED 2026-10-02: per-hoe sizes through the `hoe_harvest/NxN` tags (§2.3);
  echo, felsic and dragon hoes are 5x5.
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
- **G11 Tobacco ignores farmland moisture.** It grows +1 stage on every random tick at light 9+, wet or
  dry, on any accepted soil. That is its own growth rule, not a soil rule, so it is left alone. It
  still gets Rich Farmland's 20% boost and Fertile Soil's bone-meal step.
- **G12 Tea refuses Farmland and Rich Farmland.** Both tea bushes test `#minecraft:dirt` themselves, so the
  pack's farmland soils never reach them. Rich Soil and Fertile Soil work (they're in `#minecraft:dirt`). Fix if
  wanted: a placement mixin per §2.6, like tobacco.
- **G13 Corn's top half doesn't respond to right-click.** Quark records only the `upper=false` ripe state, the
  same limit as flax (G9). Click the lower half or the farmland.
- **G14 Coffee eats neighbouring crops.** Farmer's Respite design; keep coffee 3+ blocks from other crops, or grow
  it in the Nether. Turning bone meal on (`enableBoneMealCoffeeBush`) is the only config knob.
- **G15 Pineapple only grows on sand, red sand and coarse dirt.** Fruits Delight design (the tag
  `fruitsdelight:pineapple_grows_on`). Extend the tag if you want it on farmland.

**Wild generation findings (2026-10-03).** Inputs for the vegetation worldgen system. Nothing is changed
yet; each has a suggested direction for you to approve or reject.

- **V1 Several crops only reach vanilla biomes.** ADDRESSED 2026-10-03 by §2.8 for every crop in it; the Vinery grape
  bushes and jungle vines joined §2.8 on 2026-10-22 (four rules, Vinery's own rates). Their biome tags name vanilla biomes and Terralith's
  similar biomes were never added: strawberries (Plains only), tea (Swamp only), cabbage, sea beet and
  pineapple (Beach only), vanilla vine (the three vanilla savannas), all Vinery grape bushes, Neapolitan
  adzuki (5 vanilla forests). Terralith makes the vanilla biomes a small part of the world, so these crops
  are rare. Suggested: one pack-side biome list per crop, built from climate groups (the way
  `biome_merges.json` extends structure tags), with Terralith look-alikes added.
- **V2 Temperature-window crops list dead biomes.** ADDRESSED by §2.8: ocean and cave biomes never match a rule. Wild carrots, onions and corn (base temperature 0.4 to
  0.9) and tomatoes (`#forge:is_hot/overworld`) are added to oceans, rivers, Deep Dark, Dripstone Caves and
  Terralith cave biomes. Surface placement there lands on water or in another biome, so nothing grows. That's
  harmless, but it inflates the biome counts. Suggested: the system's biome lists exclude ocean and cave biomes.
- **V3 Carrot, onion and corn share one biome set.** ADDRESSED by §2.8: carrots and onions temperate grass, corn warm dry grass. They use the same 57 biomes and differ only in rate
  (corn 1/30 against 1/120). Suggested: give each its own climate or region if you want variety.
- **V4 Jungles are crowded.** Still true after §2.8, by the spec: the warm wet group (melons, mini melons, vanilla, rice,
  peppers, coffee), bananas, lemons and five fruit trees all land in the 8 jungles. Melon, cocoa, bell pepper, RD coffee, rice, banana, cranberry, jungle grapes,
  lychee, mango, mangosteen, durian, peach and bayberry trees all spawn there. Temperate land gets
  carrots, onions, corn, pumpkins and a few fruit trees.
- **V5 Two coffees and two apples.** 2026-10-03: both apple trees now spawn in oak forests until they're merged;
  Farmer's Respite coffee no longer spawns (V18). Rustic Delight coffee (jungle) and Farmer's Respite coffee (Basalt
  Deltas) are different items. Vinery apple trees (plains, meadow) and Fruits Delight apple trees (forests)
  also differ. Decide whether both stay, or unify them through tags/recipes (the item_dict rule).
- **V6 Wild flax is switched off** in `supplementaries-common.toml` (hand-edited copy in `configs_edited/`).
  Its biome tag is built at runtime by Supplementaries, so with it switched off no biome gets it. DECIDED 2026-10-22:
  it stays off. `wild_crops.json` key `flax` (no rule, no features) also turns `supplementaries:wild_flax` into
  `forge:none`, so turning the config back on does not bring it back.
- **V7 Rustic Delight patches are flower-type.** Rustic Delight registers its three wild patches with the
  `minecraft:flower` feature type instead of `minecraft:random_patch` (they place the same way). Bone meal on a grass
  block picks a random **flower** feature from the biome, so in every biome that carries a patch (cotton: warm dry
  grassy; peppers and coffee: warm wet) bone-mealing grass can grow wild cotton, wild bell peppers or wild coffee next
  to the flowers. DECIDED 2026-10-22: plain patches. The pack overrides the three configured features
  (`kubejs/data/rusticdelight/worldgen/configured_feature/wild_{cotton,bell_peppers,coffee}_feature.json`) with the same
  `minecraft:random_patch` settings (32/6/2, 48/4/4, 48/6/4 tries / xz / y, one block into air), so worldgen is
  unchanged: Rustic Delight's placed features, config chances and the §2.8 rules still apply. Bone meal no longer grows
  them (CR-43 inverted, CR-86).
- **V8 Tea is one or two bushes per Swamp chunk, every chunk.** §2.8 widens it to the three temperate wet biomes (V20). It's dense in one small biome and absent
  everywhere else.
- **V9 Rates span three orders of magnitude.** Banana plants try 0.75 times per jungle chunk, pumpkins
  once in 300 chunks and durian trees once in 200. A system-wide "per 1000 land chunks" target per crop
  (the column in [crops_by_biome.md](crops_by_biome.md)) gives one scale to tune against.
- **V10 Most crop features are Java-registered.** USED by §2.8: the mods' modifiers become `forge:none` and the pack adds
  its own; the Java rates stay. Changing their rate or biomes in place isn't possible
  from data (no JSON). The system would add its own JSON placed features for the wild blocks and remove the
  mods' features with `forge:remove_features`, or suppress them by config (FD, Delightful and Rustic Delight
  chances, Farmer's Respite chances) where a config exists.
- **V11 Vanilla `minecraft:patch_melon` is unused.** Terralith's jungles use their own `terralith:jungle/patch_melon`.
- **V12 New chunks only.** Like every worldgen change, explored chunks keep what they generated with.

**Wild crop placement findings (2026-10-03, §2.8).** Open; each has a suggested direction.

- **V13 Densities moved a lot.** The rates per attempt are the mods', but the biome areas changed. Per 1000 overworld
  chunks ([crops_by_biome.md](crops_by_biome.md) §1): sugar cane 116 to 6.7 (x0.06), pumpkin 1.7 to 0.3, persimmon 1.4 to
  0.4, rice 5.7 to 1.8, corn 7.1 to 3.1, lemon 1.5 to 0.7, mini melon 2.0 to 1.0, Vinery apple 0.7 to 0.4. Up: mint ponds
  2.2 to 25.4 (x11), strawberries 0.5 to 3.5, mushroom colonies 0.2 to 1.2 each, cantaloupe 0.3 to 1.7, fig 1.9 to 5.8,
  salmonberries 1.6 to 5.0. Suggested: a per-crop target and a placed-feature copy with its own rarity (the pack copy
  pattern in §2.8 already does this for the vanilla patches), mint and pumpkin first.
  Figures above are from 2026-10-03; with the 2026-10-10 rules ([crops_by_biome.md](crops_by_biome.md) §1): mint 12.5
  (x5.6), strawberries 0.5 (x1.1), salmonberries 3.2, fig 4.5, persimmon 0.6; sugar cane, pumpkin, rice and corn unchanged.
- **V14 Pineapples still need sand.** Fruits Delight's wild pineapple only survives on sand, red sand or coarse dirt
  (`#fruitsdelight:pineapple_grows_on`), and its feature tests for plain sand. In mangrove swamp, granite cliffs and
  tropical jungle they only appear where the coast has sand. Fix if wanted: a pack feature on that tag plus extending the
  tag with grass/mud (that also lets players plant them there).
- **V15 Ground limits elsewhere.** Cabbages and sea beets on cliffs need a dirt or grass top (stone cliffs get none), and
  their Sandy Shrubs need sand. Sugar cane can't root on gravel (gravel beach) or beside ice (frozen river). Melons and
  mini melons need grass, not mangrove mud. These biomes are in the lists as you asked; they just yield little.
- **V16 No orchard or pumpkin biome.** Pumpkins use Skylands Autumn as the "pumpkin biome". If you meant something else
  (Vinery/Fruits Delight apple biomes, flower forests), it is one `where` clause.
- **V17 Rubberwood leaves drop apples.** FIXED 2026-10-21: `myrtrees:blocks/rubberwood_leaves` is in
  `08_leaf_apples.js` TABLES. The 2026-10-18 loot dump (all mods) has no other leaf table with apples besides the
  Fruits Delight and Vinery apple trees. CR-57.
- **V18 Farmer's Respite coffee has no source now.** Its only source was the Basalt Deltas bushes: no chest loot, no
  trades. Options: merge it into Rustic Delight coffee (the item_dict/recipes route), or spawn FR coffee bushes again
  somewhere (they need basalt or magma).
- **V19 Biomes with no wild crop.** 13 land biomes match no rule: the stone and ice peaks (frozen, jagged, stony,
  emerald, volcanic crater/peaks), snowy slopes, ice spikes, glacial chasm, gravel desert, windswept gravelly hills,
  stony spires and windswept spires. All rock, ice or gravel, where none of the crop blocks survives.
- **V20 Tea is in 3 biomes.** ADDRESSED 2026-10-22: 20 biomes (every swamp and jungle, the sakura biomes, dark forest,
  Lush Valley, Cloud Forest, plus the old three). The rate is still FR's every-chunk patch, so density went from 5.2 to
  59.3 per 1000 chunks (crops_by_biome.md §1); a pack placed copy with a rarity would bring it down (wild-crops #1).

- **F-SS1 Extra stem fruit ignores the seasons.** Found 2026-10-22 (code check, Serene Seasons 8.1.0.24 decompiled):
  `StemExtraFruit` fires the crop-grow event with the attached stem's state; Serene Seasons' season tags hold no
  attached stem, and an unlisted block is always fertile. A melon stem therefore fruits once in season and then keeps
  adding fruit in winter. Fix options: add the three attached stems to the same season tags as their stems
  (`sereneseasons:summer_crops` for melon and hamimelon, `autumn_crops` for pumpkin, through `global.tags`), or fire
  the event with the plain stem's state. Decision open; SQ-SF and CR-71 to CR-75 don't cover it (CR-76 does).

Planned (scratchpad 2026-10-21):

- **Bell pepper colours.** Done 2026-10-21, see §3 (even split; only red / green / yellow exist).
- **Melon and pumpkin stages.** Smaller pumpkin / melon stages before full size, maybe with the Fantasy Furniture
  stackable pumpkin model; large 2x2 / 3x3 pumpkins are `ports/autumnityport #3` ([TODO](todo/TODO.md)
  `farming/crops #10`).

## 5. File map

- [config/quark-common.toml](../config/quark-common.toml): `[tweaks.simple_harvest]`, `[tweaks.hoe_harvesting]`, module switches `"Simple Harvest"`, `"Hoe Harvesting"`
- [config/berry_good-common.toml](../config/berry_good-common.toml): pips requirement
- [config/immersive_weathering-common.toml](../config/immersive_weathering-common.toml): `[misc]` mulch growth
- [config/farmersdelight-common.toml](../config/farmersdelight-common.toml): `richSoilBoostChance`, tomato rope settings
- [config/endercrop-common.toml](../config/endercrop-common.toml): tilled end stone, growth multipliers
- [kubejs/startup_scripts/_lib/item_dict.js](../kubejs/startup_scripts/_lib/item_dict.js): `farmland`, `rich_soil`, `rich_farmland`, `fertile_soil`, `nulch`, `tobacco_seeds`
- [kubejs/server_scripts/tags/blocktags.js](../kubejs/server_scripts/tags/blocktags.js): "Crop parity" section, `crops/farmland_soils` and `crops/farmland_crops`
- patcherMod `crops/CropParity.java` and `mixin/TobaccoPlacementMixin`, `FarmBlockUnderCropsMixin`,
  `MulchBlockCropGrowthMixin`, `QuarkHoeHarvestMixin` (compile jars for The Dirty Stuff, Quark and IW in `libs/`)
- patcherMod `crops/HoeHarvestArea.java`, `crops/HoeHarvestTooltip.java`, `mixin/QuarkHoeAreaHarvestMixin`,
  `mixin/QuarkHoeAreaBreakMixin`; [equipmenttags.js](../kubejs/server_scripts/tags/equipmenttags.js) "Hoe harvest area"
- Extra stem fruit (§2.10): patcherMod `crops/StemExtraFruit.java`, `mixin/AttachedStemExtraFruitMixin`, `CEConfig` `[stem_fruit]`
- [kubejs/client_scripts/namechanges.js](../kubejs/client_scripts/namechanges.js): Fertile Soil rename
- [kubejs/client_scripts/tooltips.js](../kubejs/client_scripts/tooltips.js): soil tooltips
- [kubejs/server_scripts/wood/sawdust.js](../kubejs/server_scripts/wood/sawdust.js): Fertile Soil recipes
- [vinery.md](vinery.md): grape bush, stem and lattice detail
- Wild generation (§2.7): [tools/gen_crop_worldgen_table.py](../tools/gen_crop_worldgen_table.py) (its `JAVA_PLACED` table restates the
  Java-registered features) → [crops_by_biome.md](crops_by_biome.md) and `kubejs/exported/worldgen/crop_worldgen.json`.
  Inputs: `kubejs/exported/biome_data.json` (`/dumpbiomes`), `kubejs/generation_scripts/worldgen/biome_config.json` (areas).
  Configs with worldgen knobs: `farmersdelight-common.toml` `[world.*]`, `delightful-common.toml` `chance_*`,
  `rusticdelight-common.toml` `wild_*_chance`, `farmersrespite-common.toml` `teaBushChance` / `coffeeBushChance`,
  `supplementaries-common.toml` `[functional.flax.wild_flax]`, `neapolitan-common.toml` (strawberry height, chimpanzees)
- WTHIT growth line (§2.9): patcherMod `crops/WthitCropGrowth.java` (plugin, entry in `waila_plugins.json`) and `crops/CropGrowth.java`
  (rules and maths), [crop_growth.json](../development/patcherMod/src/main/resources/assets/creativeengineering/crop_growth.json)
  (the file to edit), `CEConfig` `[crop_growth]`, lang key `creativeengineering.wthit.crop_harvestable`;
  [kubejs/server_scripts/_lib/dump_crop_growth.js](../kubejs/server_scripts/_lib/dump_crop_growth.js) (`/dumpcropgrowth`),
  [tools/crop_growth_coverage.py](../tools/crop_growth_coverage.py), [tools/test_crop_growth.sh](../tools/test_crop_growth.sh) +
  `tools/crop_growth_test/CropGrowthTest.java`
- Wild crop placement (§2.8): [kubejs/generation_scripts/worldgen/wild_crops.json](../kubejs/generation_scripts/worldgen/wild_crops.json)
  (biome traits + crop rules, the file to edit), [kubejs/server_scripts/worldgen/wild_crops.js](../kubejs/server_scripts/worldgen/wild_crops.js)
  (applier), `kubejs/data/creativeengineering/worldgen/configured_feature/wild/` and `placed_feature/wild/` (pack features),
  patcherMod `CEConfig` `[worldgen.systems] wildCrops` + `worldgen/WorldgenConfig.wildCropsEnabled()`,
  [kubejs/server_scripts/loot/08_leaf_apples.js](../kubejs/server_scripts/loot/08_leaf_apples.js) (leaf apples),
  [kubejs/server_scripts/loot/09_crop_drops.js](../kubejs/server_scripts/loot/09_crop_drops.js) (crop harvest table fixes: bell pepper colours),
  [config/alexsmobs.toml](../config/alexsmobs.toml) `bananasDropFromLeaves` (copy in `configs_edited/root_config/`),
  [tools/test_wild_crops.sh](../tools/test_wild_crops.sh) + `tools/wild_crops_test/RhinoWildCrops.java` (offline checks)

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
- **Wild generation (§2.7):** `python3 tools/gen_crop_worldgen_table.py` (about 1 s). On 2026-10-03 it printed
  `84 crop/feature rows, 62 crops` and two notes: `minecraft:patch_melon` and `supplementaries:wild_flax` reach no
  biome (V11, V6). It merges biome tags statically from the jars and `kubejs/data`. Tags added at runtime aren't
  seen: Supplementaries' generated tags, and KubeJS tag events (today those only touch structure tags). The Java
  feature classes read for §2.7: FD `WildCropGeneration`, `WildCropFeature`, `WildRiceFeature`,
  `AddFeaturesByFilterBiomeModifier`; FR `FRGeneration`; Delightful `DelightfulWildCropGeneration`; Corn Delight
  `WildCornGeneration`; Rustic Delight `ModConfiguredFeatures` / `ModPlacedFeatures`; Nether's Delight `NDGeneration`,
  `PropelplantFeature`; Neapolitan `NeapolitanFeatures` and `common/levelgen/feature/*`; Fruits Delight `FDTrees`,
  `FDBushes`, `FDPineapple`, `FDMelons`; Upgrade Aquatic `UAFeatures`, `RiverTreeFeature`; vanilla `VegetationPlacements`,
  `VegetationFeatures`, `TreeFeatures`, `CavePlacements` (from the mapped Forge jar in `~/.gradle/caches/forge_gradle`).
- **Wild crop placement (§2.8):** `tools/test_wild_crops.sh` (about 10 s, needs the ATLauncher libraries for Rhino's gson).
  It runs the generator (fails on any config problem), then the real `wild_crops.js` in the pack's Rhino fork with KubeJS
  stubbed: every crop tag must equal the generator's list, every data file must parse with the expected modifier types,
  CEConfig off must apply nothing, and a missing mod (neapolitan) must only drop its own entries. 2026-10-03: all four
  pass; "46 crop tags match the generator", "46 add modifiers, 44 forge:none, 1 remove modifier". The generator printed
  `78 crop/feature rows, 61 crops`, 13 land biomes with no crop (V19) and the flax note (V6). Not covered: the KubeJS
  type wrappers and Forge actually loading the modifiers, which needs the game (CR-47).
- **WTHIT growth line (§2.9):** `tools/test_crop_growth.sh` (about 5 s; compile patcherMod first with
  `./gradlew :patcherMod:compileJava --offline`). It runs 38 checks on the maths and the shipped rule file, then
  `tools/crop_growth_coverage.py` against `kubejs/exported/crops/crop_growth_audit.json`. After adding mods, run
  `/dumpcropgrowth` (or `/dumpall`) and the coverage script; an UNKNOWN row is a new plant to give a rule or a skip reason.
  Not covered offline: the WTHIT overlay itself (CR-59 to CR-66).

## 7. In-game test suite

- [ ] **CR-01 Tobacco soils:** tobacco seeds plant on Farmland, Rich Farmland and Fertile Soil. On Rich Soil and dirt they're refused.
- [ ] **CR-02 Tobacco replant:** right-click a stage-7 tobacco with an empty hand → 4+ leaves and 0-1 seeds drop, and the plant is back at stage 0. Right-click an unripe one → nothing.
- [ ] **CR-03 Wheat replant:** right-click ripe wheat with an empty hand → wheat and seeds drop (one seed fewer than breaking), and the wheat is back at stage 0.
- [ ] **CR-04 Any item harvests:** right-click ripe wheat while holding a stick or a block → it's harvested, and the block isn't placed.
- [ ] **CR-05 Hoe area size:** iron hoe right-click on a ripe field → 3x3 replanted, no durability lost. Diamond hoe → 4x4. Echo hoe → 5x5.
- [ ] **CR-06 Hoe on grass:** iron hoe right-click on grass next to ripe wheat → the grass is tilled, no harvest.
- [ ] **CR-07 Hoe break:** break one ripe wheat with an iron hoe in a half-ripe field → the 3x3 around it breaks, unripe ones included, and nothing replants.
- [ ] **CR-08 Rich Soil vs crops:** cabbage seeds on Rich Soil are refused. On Rich Farmland and Fertile Soil they plant.
- [ ] **CR-09 Flax halves:** right-click a ripe flax's top half → nothing. Its lower half → harvested, replanted, and the top half gone.
- [ ] **CR-10 Hemp hoe break:** with a row of grown hemp, break one top with a hoe → the neighbouring tops break and the bottoms stay and regrow.
- [ ] **CR-11 Rice:** right-click ripe panicles → rice drops and the panicles go back to stage 0.
- [ ] **CR-12 Berry pips on soils:** Sweet Berry Pips plant on Rich Soil, Rich Farmland and Fertile Soil. Iron hoe right-click on a ripe bush patch picks 3x3 bushes.
- [ ] **CR-13 Grape bush on Fertile Soil:** red grape seeds on Fertile Soil in full light → record whether they plant (unknown, see §2.1). On Farmland → refused.
- [ ] **CR-14 Nether Wart soils:** plants on Fertile Soul Soil (Nulch). Refused on Fertile Soil.
- [ ] **CR-14a Grape bushes on IW soils:** stand on a humus or fluvisol patch and run `/place feature vinery:red_grape_bush`, then `/place feature vinery:taiga_white_grape_bush` → bushes land on the soil blocks as well as on grass (before the fix they skipped them).
- [ ] **CR-15 Ender Crop light:** ender seeds on Rich Farmland in daylight → refused. Under a roof with no light → plant.
- [ ] **CR-16 Weeds:** right-click ripe weeds → they come back at stage 0 (G8).
- [ ] **CR-17 Tomatoes:** right-click a ripe bottom vine → replanted at stage 0. Right-click a ripe vine on a rope → tomatoes picked, back to stage 0.
- [ ] **CR-18 Stems and hoe break:** break a crop with a hoe next to a pumpkin stem → the stem breaks too.
- [ ] **CR-19 Sugar cane without water:** sugar cane on Rich Soil and on Fertile Soil with no water nearby → it plants.
- [ ] **CR-20 Glow berries hoe click:** iron hoe right-click under a ripe glow berry vine → only the bottom (tip) block in range is picked.
- [ ] **CR-21 Tobacco hoe replant:** a 3x3 of ripe tobacco, iron hoe right-click on the middle → all 9 replanted at stage 0, no durability lost.
- [ ] **CR-22 Tobacco hoe break:** break one tobacco with an iron hoe in a 3x3 patch (ripe and unripe) → the whole 3x3 breaks, nothing replants.
- [ ] **CR-23 Dry farmland keeps tobacco:** tobacco on farmland with no water in range → after a few minutes the farmland is dry but stays farmland and the tobacco stays. Bare dry farmland next to it turns to dirt.
- [ ] **CR-24 Tobacco on soaked Fertile Soil:** soak Fertile Soil (water bucket), plant tobacco in daylight → it ripens visibly faster than tobacco on dry Fertile Soil next to it.
- [ ] **CR-25 Tobacco soil swap:** tobacco on Rich Farmland; break the soil → the tobacco pops (drops a seed) as with wheat.
- [ ] **CR-26 Tooltips:** hover each hoe. Wooden/stone → no harvest lines. Copper/golden → "Harvest area: 2x2, toward where you look". Iron/steel/manasteel → "3x3". Diamond/netherite/elementium → "4x4, toward where you look". Echo/felsic/dragon → "5x5". Every one with an area also shows the right-click/break line.
- [ ] **CR-27 2x2 direction:** in a ripe 5x5 wheat field, stand south of the middle facing north-east, golden hoe right-click the middle → it plus the block north, the block east and the block north-east replant (4 total). Face north-west → the west side instead.
- [ ] **CR-28 4x4 right-click:** diamond hoe right-click the middle of a ripe 7x7 field facing north-east → 16 replanted: 1 block west/south of the middle and 2 blocks north/east.
- [ ] **CR-29 Area break:** break one wheat in a full field with a copper hoe → 4 gone (2x2). Netherite → 16. Dragon → 25. Wooden hoe → only the one broken.
- [ ] **CR-30 Wooden hoe right-click:** wooden hoe right-click a ripe wheat in a field → only that one replants. On a ripe sweet berry bush → it's picked by vanilla's own click, the neighbours aren't.

- [ ] **CR-31 Corn replant:** right-click the lower half of ripe corn → corn drops, the lower half is back at stage 0 and the top half breaks and drops its corn. Right-click the top half → nothing.
- [ ] **CR-32 Corn soils:** corn seeds plant on Farmland, Rich Farmland and Fertile Soil; refused on dirt and Rich Soil.
- [ ] **CR-33 Tea:** tea seeds plant on grass and Rich Soil, refused on Farmland (G12). Shears on a grown bush → leaves + sticks, back to a small bush; empty hand and iron hoe right-click → nothing.
- [ ] **CR-34 Coffee drain:** coffee beans on basalt next to a wheat patch in the Overworld → as the coffee grows, wheat within 2 blocks turns into Wither Roots / Wild Coffee Bush. A stem at stage 2 gives 1 berry on an empty-hand click.
- [ ] **CR-35 Fruit bushes:** blueberry bush at stage 4 → right-click gives 1-2 berries and it drops to stage 2; iron hoe right-click on a ripe patch picks 3x3. Lemon: right-click the top half of a ripe lemon tree → lemons drop.
- [ ] **CR-36 Pineapple:** pineapple sapling plants on sand and coarse dirt, refused on grass and farmland.
- [ ] **CR-37 Fruit tree:** grow an orange tree from a sapling, wait for fruit → right-click a fruiting leaf drops an orange and the leaf turns plain. Leaves placed by hand never flower.

Wild generation (§2.7). Use a new world, or chunks nobody has explored. `/locate biome <id>` finds the biome.

- [ ] **CR-38 Beach crops:** walk a vanilla `minecraft:beach` → wild cabbages and sea beets with Sandy Shrubs, wild pineapples and cantaloupes on the sand. A Terralith beach-like biome (e.g. `terralith:gravel_beach`) has cantaloupes but no cabbages, sea beets or pineapples (V1).
- [ ] **CR-39 Temperate wild crops:** in a forest or plains → wild carrot patches with a Coarse Dirt floor and Grass, wild onions with Alliums, and wild corn (about four times as common as the other two).
- [ ] **CR-40 Strawberries and adzuki:** vanilla `plains` → strawberry patches, most bushes red and ripe, a few unripe on Coarse Dirt. Vanilla `forest` → adzuki sprouts on plain Dirt. Break a ripe sprout → the dirt under it becomes Adzuki Soil and a new sprout appears later.
- [ ] **CR-41 Mint pond:** `windswept_hills` → a small pond ringed by mint, usually with one spruce beside it.
- [ ] **CR-42 Jungle:** `jungle` → banana plants on gravel patches (sometimes with chimpanzees), wild bell peppers, Rustic Delight wild coffee, melons, cocoa on jungle trees, lychee or mango trees.
- [ ] **CR-43 Bone meal flowers:** bone-meal a big grass area in a `jungle` several times → flowers only, never a wild bell pepper or wild coffee (V7, plain patches since 2026-10-22).
- [ ] **CR-44 Swamp and Nether:** vanilla `swamp` → one or two wild tea bushes with ferns in most chunks. `basalt_deltas` → wild coffee bushes and Wither Roots on basalt. `crimson_forest` → propelplant on nylium.
- [ ] **CR-45 River trees:** `river` → small river trees on the banks, some with mulberry vines hanging from the leaves.
- [ ] **CR-46 No wild flax:** search a river or beach area → no wild flax (V6).

CR-38 to CR-45 describe the mods' own spawns: since §2.8 they only hold with CEConfig `[worldgen.systems] wildCrops = false`.

Pack wild-crop placement (§2.8). Needs patcherMod 1.0.4 rebuilt 2026-10-03 in `mods/` for CR-58 (the scripts default to on
without it). New world, or unexplored chunks; `/locate biome <id>`.

- [ ] **CR-47 Loads:** create a world → it loads. `logs/latest.log` has `[wild-crops] 46 crop modifiers added, 44 mod modifiers
  off, 11 biome-file patches removed` and, after the world loads, `[wild-crops] <n> crop/biome pairs in 46 crop tags` with no
  `problems` and no "not in" warnings. `[leaf-apples] apples removed from 2 of 2 leaf tables`.
- [ ] **CR-48 Coast:** `beach` → wild cabbages, sea beets, pineapples, sugar cane. `terralith:white_cliffs` or `granite_cliffs`
  → cabbages and sea beets on grassy cliff tops (no Sandy Shrub off sand). No cantaloupes on beaches any more.
- [ ] **CR-49 Temperate:** `plains` and `forest` → wild carrots and onions; no wild corn, no strawberries in plains.
  `forest` also has adzuki sprouts and apple trees (both kinds). `birch_forest` → pear trees.
- [ ] **CR-50 Cold:** `taiga` → sweet berries, wild potatoes, salmonberries, pumpkins, fig and hawberry trees (no
  blueberries or strawberries since the 2026-10-10 rules). `snowy_taiga` → sweet berries, blueberries and persimmon trees.
- [ ] **CR-51 Cold wet:** `terralith:ice_marsh` or `yellowstone` → cranberry bushes. Jungles and swamps have none.
- [ ] **CR-52 Warm wet:** `jungle` → melons, mini melons, vanilla vines, wild rice in shallow water, bell peppers, RD wild coffee,
  bananas, lemon bushes and the five fruit trees (lychee, mango, peach, orange, kiwi). `mangrove_swamp` → a few jungle trees
  with cocoa, bayberry/mangosteen/durian trees.
- [ ] **CR-53 Warm dry:** `savanna` → wild tomatoes, cotton, cantaloupes and corn. `desert` → none of those; hamimelons on
  sand. `badlands` → hamimelons on red sand and terracotta.
- [ ] **CR-54 Mushrooms:** `dark_forest` → brown and red mushroom colonies on the shaded floor (not on leaves). `mushroom_fields`
  → colonies on mycelium.
- [ ] **CR-55 Tea and mint:** `swamp`, `mangrove_swamp` and `jungle` → wild tea bushes (CR-83 for the rest). `grove` or `terralith:highlands` → mint ponds. No mint in plains, meadow or windswept hills (2026-10-10 rules).
- [ ] **CR-56 Sugar cane only by rivers and beaches:** a desert lake or swamp has no sugar cane; a river bank has it.
- [ ] **CR-57 Leaf drops [scratch server PASSED 2026-10-22, SQ-CD-02, 2000 rolls each / game]:** break 200 oak leaves, 200 dark oak leaves and 200 Myrtrees rubberwood leaves (or
  `/loot spawn ~ ~ ~ mine ~ ~ ~` on placed leaves) → saplings and sticks, no apples; the log has
  `[leaf-apples] apples removed from 3 of 3 leaf tables`. Break jungle leaves → no Alex's Mobs bananas. Basalt Deltas → no wild coffee bushes.
- [ ] **CR-58 Switch off:** set `wildCrops = false` in `config/creativeengineering-common.toml`, new world → the log has
  `[wild-crops] off in CEConfig`; `plains` has strawberries and corn again (mod defaults). Leaf apples stay off (that's loot,
  not this switch).

- [ ] **CR-59 Hemp:** plant hemp on farmland and look at it each stage → "Growth: 0%", 25%, 50%, 75%, then "Growth: Mature" at the
  last bottom stage; the top half that grows on it reads "Growth: Mature".
- [ ] **CR-60 Tobacco:** look at tobacco at stages 0, 3 and 7 → 0%, 43%, "Mature". Right-click the stage-7 plant (CR-02): it drops to 0%.
- [ ] **CR-61 Berry bushes:** strawberry bush stage 3 → "Growth: 50%", stage 6 → "Mature". Salmonberry stage 2 → 50%, stage 3 →
  "75% (Harvestable)", stage 4 → "Mature". Mint, blueberry, cranberry and lemon (look at either half) → a percentage, "Mature" at the last stage.
- [ ] **CR-62 Grapes:** a red grape bush at stage 1, 2, 3 → 33%, "67% (Harvestable)", "Mature". An empty grapevine stem (no seed) shows no
  line; planted, it counts up from 25% to "Mature" at stage 4. A lattice with jungle grapes at stage 3 → "75% (Harvestable)".
- [ ] **CR-63 Fruit leaves:** natural apple (or any Fruits Delight) leaves in the flowering stage → "Growth: 50%", fruiting → "Mature".
  Leaves you placed yourself show no Growth line. Durian leaves: none / flowers / small / fruits → 0%, 33%, 67%, "Mature".
- [ ] **CR-64 No doubles:** wheat, cocoa, sweet berries, a melon stem and nether wart each show exactly one Growth line, and tea, coffee stems,
  colonies, rice, budding tomatoes, adzuki, mulberries, pineapple, hamimelon stem and a floral bush each show one too.
- [ ] **CR-65 Toggles:** turn WTHIT's "Crop progress" off (WTHIT config, vanilla plugin section) → the added lines go too, wheat's as well. Set
  `[crop_growth] enabled = false` → only the added lines go. `showHarvestable = false` → "(Harvestable)" disappears.
- [ ] **CR-66 Override file:** copy `crop_growth.json` to `kubejs/assets/creativeengineering/`, change salmonberry's `"harvest": 3` to 4, F3+T →
  stage 3 no longer says "(Harvestable)". Delete the copy and F3+T → it is back.
- [ ] **CR-67 Dark Forest humus:** new chunks of a Dark Forest → humus patches have adzuki sprouts, grass, flowers and mushroom
  colonies on them like the grass around (adzuki is rare, 1 in 128 chunks; `/place feature neapolitan:patch_adzuki_sprouts` on humus works).
- [ ] **CR-68 Swamps and badlands:** Ice Marsh fluvisol carries cranberry/blueberry/sweet berry bushes, Orchid Swamp fluvisol carries melons and
  orchids, badlands vertisol can carry hamimelon. Permafrost never carries any of them.
- [ ] **CR-69 Bell pepper split [scratch server PASSED 2026-10-22, SQ-CD-01, each colour 0.40 to 0.56 / game]:** `/reload` → the log has `[crop-drops] rewrote 1 of 1 crop tables`. Harvest 30+ ripe
  bell peppers (or `/setblock ~ ~ ~ rusticdelight:bell_peppers[age=7]` then `/loot spawn ~ ~1 ~ mine ~ ~ ~` repeatedly) →
  red, green and yellow each about a third, roughly 1.45 peppers per harvest. Unripe plants drop only a seed.
- [ ] **CR-70 Pepper config [game]:** in `config/creativeengineering-common.toml` `[crop_drops.bell_peppers]` set
  `redWeight = 6` and `redBonusChance = 0.0`, `/reload` → the log has `weights 6:1:1` and `[NCH.cfg] CROP_DROPS.redWeight
  overridden`; harvests are mostly red again. Set `enabled = false`, `/reload` → `rewrote 0 of 1`. Put the defaults back.
- [ ] **CR-71 Second melon [scratch server PASSED 2026-10-22, SQ-SF-01 to SQ-SF-03 / game]:** for speed set `[stem_fruit]` `secondFruitMinutes = 1`, `thirdFruitMinutes = 2`,
  `fourthFruitMinutes = 3` and `/gamerule randomTickSpeed 100`. Plant a melon stem on hydrated farmland with dirt on all
  four sides, bone-meal it until the first melon grows → within a few minutes a 2nd, 3rd and 4th melon appear on the
  other sides, one at a time, each later one taking longer. No 5th. Same for a pumpkin stem.
- [ ] **CR-72 Re-point [scratch server PASSED 2026-10-22, SQ-SF-04 / game, the stem model's bend is client]:** on a stem with 3 or 4 melons break the melon the stem bends toward → the stem bends to another
  melon (it does not straighten). Break all melons → it straightens into the grown plain stem and grows a new first melon.
- [ ] **CR-73 Wait holds [scratch server PASSED 2026-10-22, SQ-SF-02 at 1 minute / game at the defaults]:** put the defaults back (10 / 20 / 40) and `randomTickSpeed` 100 → a stem with one melon grows
  no 2nd for about 10 minutes, then grows one. Put `randomTickSpeed` back to 3.
- [x] **CR-74 Off switch [scratch server PASSED 2026-10-22, SQ-SF-05]:** `enabled = false` (or `maxFruits = 1`) → no new extra fruit grows; existing ones stay. Back on.
- [ ] **CR-75 Hamimelon [scratch server PASSED 2026-10-22, SQ-SF-01 / game]:** a Fruits Delight hamimelon stem grows extra hamimelons the same way.
- [ ] **CR-76 Seasons [game]:** in winter (`/season set winter`), a melon stem with one melon and `randomTickSpeed` 100:
  note whether extra melons still grow (expected today: yes, F-SS1; after a fix: no, or slowly as Serene Seasons'
  `out_of_season_crop_behavior` 0 allows). A plain melon stem grows its first melon slowly or not at all.
- [ ] **CR-77 Saved wait across restart [game]:** a stem with one melon, defaults (10 minutes); stop and restart the
  world after 5 minutes of play → the 2nd melon still comes about 5 minutes later, not straight away and not 10 more
  (`data/creativeengineering_stem_fruit.dat` keeps the time).

Wild-crops batch 2026-10-22 (new chunks only; rejoin a world):
- [ ] **CR-83 Tea spread [biome lists: scratch server PASSED 2026-10-22, SQ-WC-01 / game]:** `terralith:lush_valley`, `terralith:sakura_grove`, `terralith:orchid_swamp` and
  `terralith:tropical_jungle` → wild tea bushes with ferns. `plains` → none.
- [ ] **CR-87 Grapes in Terralith [scratch server PASSED 2026-10-22, SQ-WC-02 / game]:** `terralith:lavender_forest` → red and white grape bushes; `terralith:shrubland` →
  savanna grape bushes; `terralith:shield` → taiga grape bushes; `terralith:tropical_jungle` or `mangrove_swamp` →
  red and white grape vines hanging off trunks. `snowy_taiga` and `dark_forest` → no grapes.
- [ ] **CR-88 White strawberries [scratch server PASSED 2026-10-22, SQ-WC-05, y 200 white, y 191 red / game, the tooltip is client]:** plant a strawberry bush at Y 192 or higher (or find one on a highland peak) and
  let it ripen → white strawberries; one at Y 150 → red. The White Strawberries tooltip names Y 192.
- [ ] **CR-86 No bone-meal cotton, wild patches still spawn [scratch server PASSED 2026-10-22, SQ-WC-04 / game]:** bone-meal a large grass area in `savanna` many times →
  flowers only, no wild cotton. New chunks in `savanna` and `jungle` → wild cotton, bell pepper and coffee patches as
  before.
- CR-46 (no wild flax) still holds: the flax modifier is now off in the pack as well (SQ-WC-03, scratch server PASSED 2026-10-22: no wild flax in any biome).

Scratch-checkable parts go through `tools/farm_worldgen_probe.js` (queue SQ-SF, SQ-WC, SQ-CD); `[game]` parts need
the real client (rendering, tooltips, JEI, natural random ticks with a player nearby).

## 8. History

- **2026-10-02:** written from the decompiled jars (Quark 3.4-418, Farmer's Delight 1.2.4, Immersive
  Weathering 1.2.10, The Dirty Stuff 2.1, IE 9.2.4, Supplementaries 2.4.20, Neapolitan 4.1.0, Delightful
  3.4.1, Rustic Delight 1.3.0, Ender Crop 1.7.0, Upgrade Aquatic 5.1.0, Nether's Delight 3.1, Berry Good
  6.1.0, Floral Enchantment 1.4.3, Vinery 1.3.10) and Forge 43.3.9's patched vanilla classes. No pack
  changes made; gaps G1 to G10 open.
- **2026-10-02:** tobacco brought in line (G1 to G3) through the new crop parity layer (§2.6): two block
  tags, one Quark config line, four patcherMod mixins. Built into patcherMod 1.0.3 in `mods/`, not
  deployed, not boot-tested yet. G11 noted.
- **2026-10-02:** per-hoe harvest area (G5): `hoe_harvest/NxN` item tags, sizes 1x1 to 9x9 with even sizes
  biased toward the look direction, two Quark mixins and a generated tooltip (§2.3). Built into patcherMod
  1.0.3 `build/libs`, not copied to `mods/`, not deployed.
- **2026-10-02:** everything built since the 1.0.3 deploy is pack 1.0.4: the crop parity layer and the hoe
  areas ship in patcherMod 1.0.4 (the earlier "1.0.3" labels were the unbumped jar name), now in `mods/`, not
  deployed, not boot-tested. See [plan_1.0.4.md](plan_1.0.4.md).
- **2026-10-02:** 1.0.4 mods added from their decompiled jars (Corn Delight 1.0.3 + Mysterious Mountain Lib
  1.2.3, Farmer's Respite 2.0, Fruits Delight 0.5.11): corn (two-high, lower half replants), tea (shears only, no
  farmland), coffee (basalt/magma, drains nearby crops), blueberry/cranberry/lemon bushes, pineapple (sand), the
  hamimelon stem and 13 fruit trees. More Delight and Untitled Duck Mod add no crops. Gaps G12 to G15, tests CR-31
  to CR-37. No pack changes.
- **2026-10-03:** §2.7 wild generation written from the decompiled jars (Farmer's Delight 1.2.4, Farmer's Respite 2.0,
  Delightful 3.4.1, Corn Delight 1.0.3, Rustic Delight 1.3.0, Nether's Delight 3.1, Neapolitan 4.1.0, Fruits Delight 0.5.11,
  Upgrade Aquatic 5.1.0, Vinery 1.3.10, Supplementaries 2.4.20, vanilla 1.19.2) and the biome files of Terralith 2.3.12,
  Hearths 1.0.0 and Nullscape 1.2.2. New generator `tools/gen_crop_worldgen_table.py` → [crops_by_biome.md](crops_by_biome.md).
  Found: wild flax is switched off in the pack's Supplementaries config; Neapolitan's "adzuki soil" comes from harvesting
  wild sprouts (the worldgen only lays down dirt). Findings V1 to V12 and tests CR-38 to CR-46 added. No pack changes.
- **2026-10-03:** §2.8 wild crop placement built from your spec: `wild_crops.json` (traits for all 146 overworld biomes incl.
  oceans/caves, 46 crop rules), `worldgen/wild_crops.js` (biome tags, 44 mod modifiers to `forge:none`, 46 add modifiers,
  one remove modifier for the vanilla patches), 5 configured + 10 placed pack features, CEConfig `wildCrops` switch
  (patcherMod 1.0.4 rebuilt; not copied to `mods/` because the game was running). Oak/dark oak leaf apples removed
  (`loot/08_leaf_apples.js`), Alex's Mobs leaf bananas off, Farmer's Respite Nether coffee off. Generator now evaluates the
  rules, shows before/after densities, biome traits and coverage; it also learned that `minecraft:trees_jungle` grows
  cocoa (rocky jungle, jungle mountains). Offline checks `tools/test_wild_crops.sh` pass; not run in game. V1 to V3, V10
  addressed; V13 to V20 and CR-47 to CR-58 added.
- **2026-10-04:** §2.9 WTHIT growth line. Cause: WTHIT's `PlantProvider` only handles `CropBlock` subclasses and five vanilla blocks by
  identity. New `/dumpcropgrowth` audit (290 growing blocks, 22 native, 62 ruled, 206 not crops), `crop_growth.json` rules, patcherMod
  `crops/WthitCropGrowth` + `CropGrowth`, `[crop_growth]` config, coverage tool and `tools/test_crop_growth.sh` (38 checks). Built
  into `development/patcherMod/build/libs` (md5 23df38e7), not installed to `mods/`; not seen in game. Hemp's state is an enum
  (`growth`), found only after the first audit pass read integer properties alone; Durian leaves use `fruit` not `type`, found by the
  coverage tool. CR-59 to CR-66 added.
- **2026-10-05:** §2.8.1 Immersive Weathering soils. Cause: IW soils are laid before vegetation and many features test
  `grass_block` by name. New tag `creativeengineering:worldgen/grass_like_soils`, generator `tools/gen_soil_compat.py` (65
  `kubejs/data` overrides, `/cegen` id `soil_compat`), hamimelon takes the tag. Scratch-server before/after test passed. CR-67, CR-68 added.
- **2026-10-11:** status refresh. Hoe areas, tobacco parity and wild-crop placement went out with 1.0.4 (DEPLOYED
  2026-10-04). The WTHIT growth line (in patcherMod 1.0.5), the Immersive Weathering soil overrides and the
  refreshed `wild_crops.json` are STAGED for 1.0.6 (not deployed); the staged jar booted in single player without
  errors. The ghost-block fix (state id overflow) ships in the same jar.
- 2026-10-14: pack 1.0.5 was never deployed; everything above that was staged for it ships in pack **1.0.6** (STAGED
  2026-10-14, update from 1.0.4, [plan_1.0.6.md](plan_1.0.6.md)), unchanged.
- 2026-10-20: `crop_growth_coverage.py` skips the Vinery port's lattice vines and grass slab (10 UNKNOWN -> 0), found in
  the WTHIT coverage audit ([wthit_lines.md](wthit_lines.md) 4.2).
- 2026-10-21: bell pepper colours even (§3, `loot/09_crop_drops.js`, TODO `farming/crops #9`). Offline sim of the rewritten
  jar table: red 0.483, green 0.485, yellow 0.483 per ripe harvest; a second pass is a no-op. CR-69 added.
- 2026-10-21: pepper weights and bonus chances made CEConfig `[crop_drops.bell_peppers]` (patcherMod 1.1.01 rebuilt,
  not installed). Offline sim matched the formula for four configs; 6:1:1 with no red bonus reproduces the mod. The
  extra colours some pages mention are Rustic Delight 1.7.0 only (no 1.19.2 build). CR-70 added.
- 2026-10-22: extra stem fruit (§2.10): up to 4 fruits per attached stem with 10 / 20 / 40 minute minimum waits, re-point
  on harvest, CEConfig `[stem_fruit]`. patcherMod 1.1.01 built, not installed, not tested in game. CR-71 to CR-75 added.
- 2026-10-22: wild-crops batch (§2.8, TODO `farming/wild-crops #3`, `#4`): tea to 20 biomes (V20), Vinery grape bushes
  and jungle vines under the system (V1, four rules), wild flax modifier off (V6), white strawberry height 256 -> 192 plus
  a tooltip, V7 explained. The generator now reads the Vinery port's upstream jars (`vinery_upstream/`), so apple, cherry
  and grape features resolve. `tools/test_wild_crops.sh` passes. CR-83, CR-87, CR-88 added; not tested in game.
- 2026-10-22 (doc audit): §2.10 Serene Seasons claim corrected (F-SS1: extra fruit ignores seasons); jar state: the
  stem fruit classes are only in the `build/libs` jar (md5 4daad5bb), not in `mods/`. Scratch probe
  `tools/farm_worldgen_probe.js` written (stem fruit, wild-crop biome lists, white strawberries, pepper and leaf
  loot); tests tagged scratch / game; CR-76, CR-77 added.
- 2026-10-22: white strawberry height 256 (the mod default) -> 192 (doc audit: this line said 128; the toml in git and the jar default are 256). Rustic Delight wild patches overridden as plain `random_patch` (V7),
  same settings, so bone meal no longer grows them. CR-43 inverted, CR-86 added.
- 2026-10-22 (second bulk run): SQ-SF-00 to SQ-SF-05 (17 of 17 after the own-fruit fix, §2.10), SQ-WC-00 to SQ-WC-05
  and SQ-CD-00 to SQ-CD-02 PASS on the scratch server. CR-74 done; the rest keep their game halves. `[wild-crops]` also
  warns that 5 Terralith cave biomes are not in `wild_crops.json` (no crops there; not an error).
