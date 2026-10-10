# Terrain adaptation scan: structures that could use the soft beard or template trim

Generated 2026-10-09 by `python3 tools/terrain_adaptation_scan.py` (re-run after adding or updating structure mods). What the tags do: [structure_tuning.md 3.15](structure_tuning.md). Data: `kubejs/generation_scripts/worldgen/terrain_adaptation_scan.json`.

**How it reads a structure.** It follows the start pool and every jigsaw link (up to the structure's `size`) to the templates the structure can place, and measures each rigid template (terrain-matching street pieces are left out, they follow the ground anyway) the way `TemplateTrim` does:
- **Box cols**: template width x depth, summed over the templates; **Built**: columns with a built block.
- **Pad cols**: columns with only pad (grass, dirt, sand, gravel...) and plants: a flat slab around the building.
- **Air-only cols**: columns with only saved air: a rectangular notch cut into any slope.
- **Air outside built**: air blocks trim would stop placing. **Unbuilt**: share of the box that isn't built on (what vanilla's beard flattens for nothing).
- Pad and plants are the `template_trim/*` block tags as `blocktags.js` builds them (32 pad blocks, 140 plants), resolved through the live tag export.
- **Strong**: surface structure, at least 30% of the box unbuilt and 12+ pad or air-only columns. **Mild**: 15% and 6+. Totals over multi-template structures add every reachable variant once, so a jigsaw structure with many alternatives counts each of them.

**Choosing tags** (add ids to `kubejs/server_scripts/worldgen/terrain_adaptation.js`, then rejoin; new chunks only):
- `soft` + `template_trim`: a building on a pad. The pad and the air around it go, the ground is levelled only under the building and blends out in 4 blocks (+ `skirtBump`).
- `soft` alone: keeps the template's pad and courtyards (they are levelled too) but rounds and shortens the skirt. For walled compounds and forts whose yards should stay flat.
- `template_trim` alone: for `none` structures (and YUNG's, which ignore the mode tags): no more air notches or floating pad, nothing else changes.
- The suggestion column is a starting point from the numbers only; look at a structure with `/ceplace` or `/locate` before tagging a whole family.

| Verdict | Structures |
|---|---|
| Strong candidates | 3 |
| Mild candidates | 5 |
| Settlements (review) | 31 |
| Compact surface structures (nothing to gain) | 16 |
| Already tagged | 97 |
| Ships, rafts and sky structures (earth_fill/exempt) | 23 |
| Not surface (buried, seabed, sky, underground, Nether, End, mostly water) | 194 |
| Terrain-matching only (already follow the ground) | 6 |
| Built in code (no templates) | 5 |
| Templates not found | 3 |

## 1. Strong candidates

Surface structures whose templates are mostly pad or saved air. These look like the ballista did: a flat rectangle (and with `none`, a notch) the size of the whole template. "Worst template" is in the JSON.

| Structure | Adaptation | Templates | Box cols | Built | Pad cols | Air-only cols | Air outside built | Unbuilt | Suggested tags |
|---|---|---|---|---|---|---|---|---|---|
| `terralith:spire` | none | 6 | 23381 | 13019 | 841 | 5555 | 180,302 | 44% | template_trim (+ soft if the pad hangs on slopes) |
| `farmers_structures:aquacultures_common` | beard_thin | 1 | 400 | 157 | 109 | 50 | 693 | 61% | soft + template_trim |
| `farmers_structures:aquacultures_carp` | none | 1 | 400 | 267 | 87 | 1 | 177 | 33% | template_trim (+ soft if the pad hangs on slopes) |

## 2. Mild candidates

Some pad or air around the building; trim and soft would help a little.

| Structure | Adaptation | Templates | Box cols | Built | Pad cols | Air-only cols | Air outside built | Unbuilt | Suggested tags |
|---|---|---|---|---|---|---|---|---|---|
| `philipsruins:level_three_ruins` | beard_thin | 11 | 3801 | 3000 | 801 | 0 | 9,688 | 21% | soft + template_trim |
| `philipsruins:start_nether_ruin` | beard_thin | 5 | 1803 | 1291 | 512 | 0 | 5,736 | 28% | soft + template_trim |
| `old_swamp_fisherman_house:fisherman_house` | beard_thin | 1 | 476 | 353 | 123 | 0 | 1,676 | 26% | soft + template_trim |
| `philipsruins:desert_structures` | bury | 1 | 100 | 72 | 28 | 0 | 0 | 28% | template_trim (keep bury) |
| `simply_houses:taiga_forge` | beard_thin | 1 | 598 | 481 | 0 | 6 | 12 | 20% | soft + template_trim |

## 3. Settlements (review)

Villages and other jigsaw settlements with terrain-matching streets. Their houses often carry small pads; tagging the structure changes every house in it, so look at one first (`/ceplace structures <id>`).

| Structure | Adaptation | Templates | Box cols | Built | Pad cols | Air-only cols | Air outside built | Unbuilt | Suggested tags |
|---|---|---|---|---|---|---|---|---|---|
| `minecraft:village_taiga` | beard_thin | 161 | 11506 | 9334 | 689 | 1477 | 11,466 | 19% | review: soft + trim for the houses changes the whole village |
| `minecraft:village_savanna` | beard_thin | 159 | 11610 | 9347 | 1291 | 785 | 12,035 | 20% | review: soft + trim for the houses changes the whole village |
| `terralith:fortified_village` | beard_thin | 27 | 6742 | 4783 | 1531 | 428 | 72,642 | 29% | review: soft + trim for the houses changes the whole village |
| `towns_and_towers:village_forest` | beard_thin | 34 | 2766 | 1030 | 683 | 1050 | 7,719 | 63% | review: soft + trim for the houses changes the whole village |
| `minecraft:village_plains` | beard_thin | 113 | 7709 | 6377 | 674 | 581 | 6,716 | 17% | review: soft + trim for the houses changes the whole village |
| `terralith:fortified_desert_village` | beard_thin | 24 | 6164 | 4926 | 1234 | 4 | 16,745 | 20% | review: soft + trim for the houses changes the whole village |
| `minecraft:village_snowy` | beard_thin | 99 | 5057 | 3835 | 869 | 269 | 5,977 | 24% | review: soft + trim for the houses changes the whole village |
| `minecraft:village_desert` | beard_thin | 94 | 5141 | 3947 | 585 | 468 | 5,056 | 23% | review: soft + trim for the houses changes the whole village |
| `towns_and_towers:village_meadow` | beard_thin | 36 | 2417 | 1646 | 127 | 644 | 6,359 | 32% | review: soft + trim for the houses changes the whole village |
| `towns_and_towers:village_old_growth_taiga` | beard_thin | 36 | 2108 | 1398 | 73 | 637 | 4,629 | 34% | review: soft + trim for the houses changes the whole village |
| `towns_and_towers:village_jungle` | beard_thin | 50 | 2218 | 1590 | 148 | 480 | 4,398 | 28% | review: soft + trim for the houses changes the whole village |
| `towns_and_towers:village_beach` | beard_thin | 19 | 941 | 384 | 80 | 477 | 17,386 | 59% | review: soft + trim for the houses changes the whole village |
| `towns_and_towers:village_sparse_jungle` | beard_thin | 42 | 1571 | 1042 | 110 | 419 | 4,077 | 34% | review: soft + trim for the houses changes the whole village |
| `towns_and_towers:exclusives/village_iberian` | beard_thin | 48 | 2187 | 1723 | 52 | 412 | 2,792 | 21% | review: soft + trim for the houses changes the whole village |
| `towns_and_towers:village_birch_forest` | beard_thin | 41 | 2287 | 1823 | 124 | 340 | 7,023 | 20% | review: soft + trim for the houses changes the whole village |
| `towns_and_towers:village_snowy_taiga` | beard_thin | 39 | 1426 | 964 | 129 | 333 | 3,502 | 32% | review: soft + trim for the houses changes the whole village |
| `towns_and_towers:village_flower_forest` | beard_thin | 58 | 3185 | 2757 | 175 | 253 | 2,568 | 13% | review: soft + trim for the houses changes the whole village |
| `towns_and_towers:village_swamp` | beard_thin | 36 | 2147 | 1797 | 63 | 287 | 4,010 | 16% | review: soft + trim for the houses changes the whole village |
| `towns_and_towers:village_mushroom_fields` | beard_thin | 36 | 819 | 522 | 56 | 237 | 2,234 | 36% | review: soft + trim for the houses changes the whole village |
| `towns_and_towers:village_badlands` | beard_thin | 53 | 1981 | 1711 | 7 | 263 | 2,518 | 14% | review: soft + trim for the houses changes the whole village |
| `towns_and_towers:exclusives/village_classic` | beard_thin | 15 | 927 | 660 | 30 | 237 | 1,739 | 29% | review: soft + trim for the houses changes the whole village |
| `towns_and_towers:village_sunflower_plains` | beard_thin | 28 | 781 | 516 | 101 | 164 | 4,718 | 34% | review: soft + trim for the houses changes the whole village |
| `towns_and_towers:exclusives/village_mediterranean` | beard_thin | 39 | 1717 | 1476 | 12 | 229 | 3,593 | 14% | review: soft + trim for the houses changes the whole village |
| `towns_and_towers:exclusives/village_nilotic` | beard_thin | 12 | 604 | 365 | 0 | 239 | 1,393 | 40% | review: soft + trim for the houses changes the whole village |
| `towns_and_towers:village_wooded_badlands` | beard_thin | 17 | 724 | 509 | 0 | 215 | 1,324 | 30% | review: soft + trim for the houses changes the whole village |
| `towns_and_towers:exclusives/village_swedish` | beard_thin | 39 | 1156 | 949 | 8 | 199 | 1,723 | 18% | review: soft + trim for the houses changes the whole village |
| `towns_and_towers:village_grove` | beard_thin | 17 | 600 | 393 | 61 | 146 | 2,609 | 34% | review: soft + trim for the houses changes the whole village |
| `towns_and_towers:exclusives/village_tudor` | beard_thin | 33 | 1300 | 1115 | 7 | 178 | 1,162 | 14% | review: soft + trim for the houses changes the whole village |
| `towns_and_towers:exclusives/village_rustic` | beard_thin | 41 | 1248 | 1076 | 28 | 144 | 1,354 | 14% | review: soft + trim for the houses changes the whole village |
| `towns_and_towers:village_snowy_slopes` | beard_thin | 6 | 379 | 287 | 0 | 92 | 909 | 24% | review: soft + trim for the houses changes the whole village |
| `towns_and_towers:exclusives/village_wandering_trader_camp` | beard_thin | 15 | 211 | 129 | 20 | 62 | 446 | 39% | review: soft + trim for the houses changes the whole village |

## 3b. Ships, rafts and sky structures

They float on the sea or in the sky (`earth_fill/exempt`). At sea, trim would stop their saved air from replacing sea water around the hull: the water would stay where the template had air. Not suggested by default; the numbers are here if you want to try it on one.

| Structure | Adaptation | Templates | Box cols | Built | Pad cols | Air-only cols | Air outside built | Unbuilt | Suggested tags |
|---|---|---|---|---|---|---|---|---|---|
| `towns_and_towers:village_ocean` | none | 11 | 7870 | 964 | 0 | 6906 | 13,128 | 88% | template_trim |
| `unwrecked_ships:nether_ship` | none | 62 | 8825 | 2685 | 0 | 3545 | 28,858 | 70% | template_trim |
| `unwrecked_ships:ship` | none | 67 | 8957 | 3312 | 0 | 2762 | 20,176 | 63% | template_trim |
| `philipsruins:ocean_fortress` | beard_box | 4 | 3300 | 2445 | 853 | 2 | 2 | 26% | soft + template_trim |
| `skyvillages:skyvillage` | none | 20 | 3595 | 2739 | 8 | 847 | 25,226 | 24% | template_trim (+ soft if the pad hangs on slopes) |
| `illagerwarship:warship` | none | 1 | 1200 | 506 | 0 | 694 | 32,618 | 58% | template_trim |
| `antiquetradingship:antique_trading_ship` | none | 1 | 1152 | 482 | 0 | 670 | 19,430 | 58% | template_trim |
| `philipsruins:ocean_ruins` | beard_thin | 12 | 1843 | 1163 | 632 | 0 | 262 | 37% | soft + template_trim |
| `illager_viking_longship:illager_longship` | none | 1 | 759 | 357 | 0 | 402 | 9,648 | 53% | template_trim |
| `mes:starlight_voyager` | none | 1 | 512 | 219 | 0 | 293 | 7,032 | 57% | template_trim |
| `towns_and_towers:wreckage_ocean` | none | 1 | 196 | 43 | 0 | 153 | 459 | 78% | template_trim |
| `blocky_siege:pillager_ship` | none | 1 | 308 | 212 | 0 | 96 | 1,728 | 31% | template_trim |
| `towns_and_towers:pillager_outpost_ocean` | none | 22 | 4124 | 4104 | 0 | 20 | 0 | 0% | template_trim |
| `farmers_structures:sushis` | none | 1 | 132 | 107 | 0 | 13 | 33 | 19% | template_trim |
| `villager_rafts:raft_3` | none | 1 | 77 | 67 | 0 | 10 | 140 | 13% | template_trim |
| `revamped_shipwrecks:shipwreck` | none | 1637 | 108205 | 69931 | 0 | 8 | 16 | 35% | template_trim |
| `hopo:underwater_warm_small_ruins` | none | 40 | 3787 | 3677 | 1 | 6 | 6 | 3% | template_trim (+ soft if the pad hangs on slopes) |
| `villager_rafts:raft_1` | none | 1 | 35 | 29 | 0 | 6 | 54 | 17% | template_trim |
| `villager_rafts:raft_2` | none | 1 | 35 | 29 | 0 | 6 | 54 | 17% | template_trim |
| `hopo:underwater_fossils` | none | 20 | 2766 | 2003 | 0 | 0 | 0 | 28% | template_trim |
| `hopo:underwater_ruins` | beard_thin | 25 | 6366 | 6315 | 0 | 0 | 0 | 1% | soft + template_trim |
| `hopo:underwater_small_ruins` | none | 40 | 2408 | 2408 | 0 | 0 | 0 | 0% | template_trim |
| `revamped_shipwrecks:shipwreck_beached` | none | 752 | 47776 | 34592 | 0 | 0 | 0 | 28% | template_trim |

## 4. Already tagged

In `worldgen/terrain_adaptation.js` now, with their numbers for reference.

| Structure | Adaptation | Templates | Box cols | Built | Pad cols | Air-only cols | Air outside built | Unbuilt | Why |
|---|---|---|---|---|---|---|---|---|---|
| `takesapillage:bastille` | beard_box | 19 | 20331 | 10152 | 9423 | 756 | 283,241 | 50% | already in terrain_adaptation/soft |
| `minecraft:pillager_outpost` | beard_thin | 24 | 5871 | 3489 | 1 | 2020 | 36,089 | 41% | already in template_trim, terrain_adaptation/soft |
| `takesapillage:pillager_camp` | beard_box | 24 | 2636 | 1120 | 1237 | 279 | 35,089 | 57% | already in terrain_adaptation/soft |
| `farmers_structures:cooks` | beard_thin | 33 | 4741 | 2267 | 844 | 399 | 2,515 | 52% | already in template_trim, terrain_adaptation/soft |
| `towns_and_towers:exclusives/pillager_outpost_rustic` | beard_thin | 4 | 1227 | 600 | 168 | 459 | 12,913 | 51% | already in template_trim, terrain_adaptation/soft |
| `towns_and_towers:pillager_outpost_desert` | beard_thin | 4 | 1053 | 514 | 311 | 228 | 9,305 | 51% | already in template_trim, terrain_adaptation/soft |
| `towns_and_towers:pillager_outpost_old_growth_taiga` | beard_thin | 4 | 1228 | 708 | 317 | 203 | 8,467 | 42% | already in template_trim, terrain_adaptation/soft |
| `simply_houses:forest_fort` | beard_thin | 2 | 1276 | 617 | 356 | 35 | 395 | 52% | already in template_trim, terrain_adaptation/soft |
| `towns_and_towers:pillager_outpost_grove` | beard_thin | 4 | 641 | 279 | 271 | 91 | 5,783 | 56% | already in template_trim, terrain_adaptation/soft |
| `towns_and_towers:pillager_outpost_jungle` | beard_thin | 5 | 965 | 628 | 78 | 259 | 9,877 | 35% | already in template_trim, terrain_adaptation/soft |
| `towns_and_towers:exclusives/pillager_outpost_oriental` | beard_thin | 14 | 742 | 429 | 184 | 129 | 13,003 | 42% | already in template_trim, terrain_adaptation/soft |
| `towns_and_towers:pillager_outpost_sunflower_plains` | beard_thin | 4 | 732 | 431 | 85 | 216 | 10,341 | 41% | already in template_trim, terrain_adaptation/soft |
| `towns_and_towers:exclusives/pillager_outpost_iberian` | beard_thin | 4 | 815 | 516 | 99 | 200 | 6,025 | 37% | already in template_trim, terrain_adaptation/soft |
| `watchtower_building:fresh_watchtower` | beard_box | 12 | 976 | 685 | 224 | 64 | 2,748 | 30% | already in template_trim, terrain_adaptation/soft |
| `villager_circus_tent:villager_circus_tent` | beard_thin | 1 | 841 | 577 | 264 | 0 | 6,072 | 31% | already in template_trim, terrain_adaptation/soft |
| `watchtower_building:abandoned_watchtower` | beard_box | 23 | 1101 | 839 | 224 | 35 | 1,290 | 24% | already in template_trim, terrain_adaptation/soft |
| `villager_fletcher_house:villager_fletcher_house` | beard_thin | 1 | 456 | 200 | 256 | 0 | 3,767 | 56% | already in template_trim, terrain_adaptation/soft |
| `bighorsestable:big_horse_stable` | beard_thin | 1 | 782 | 527 | 255 | 0 | 3,732 | 33% | already in template_trim, terrain_adaptation/soft |
| `hobbit_house_:hobbit_house` | beard_thin | 1 | 468 | 240 | 228 | 0 | 3,091 | 49% | already in template_trim, terrain_adaptation/soft |
| `towns_and_towers:pillager_outpost_snowy_plains` | beard_thin | 5 | 533 | 329 | 49 | 155 | 7,547 | 38% | already in template_trim, terrain_adaptation/soft |
| `villager_butcher_house:butcher_house` | beard_thin | 1 | 528 | 332 | 196 | 0 | 3,235 | 37% | already in template_trim, terrain_adaptation/soft |
| `towns_and_towers:pillager_outpost_swamp` | beard_thin | 4 | 364 | 174 | 62 | 128 | 6,258 | 52% | already in template_trim, terrain_adaptation/soft |
| `towns_and_towers:exclusives/pillager_outpost_mediterranean` | beard_thin | 5 | 541 | 362 | 121 | 58 | 1,121 | 33% | already in template_trim, terrain_adaptation/soft |
| `towns_and_towers:pillager_outpost_wooded_badlands` | beard_thin | 7 | 435 | 260 | 0 | 175 | 1,100 | 40% | already in template_trim, terrain_adaptation/soft |
| `towns_and_towers:pillager_outpost_forest` | beard_thin | 4 | 453 | 288 | 55 | 110 | 6,083 | 36% | already in template_trim, terrain_adaptation/soft |
| `farmers_structures:cooks_moss` | beard_thin | 3 | 768 | 398 | 124 | 39 | 269 | 48% | already in template_trim, terrain_adaptation/soft |
| `sawmillhouse:sawmill` | beard_thin | 1 | 361 | 199 | 162 | 0 | 2,350 | 45% | already in template_trim, terrain_adaptation/soft |
| `villager_farmer_house:villager_farmer_house` | beard_thin | 1 | 567 | 405 | 162 | 0 | 2,677 | 29% | already in template_trim, terrain_adaptation/soft |
| `villager_country_house:country_house` | beard_thin | 1 | 702 | 541 | 161 | 0 | 3,057 | 23% | already in template_trim, terrain_adaptation/soft |
| `gothic_church:church` | beard_thin | 1 | 527 | 373 | 154 | 0 | 4,992 | 29% | already in template_trim, terrain_adaptation/soft |
| `towns_and_towers:pillager_outpost_sparse_jungle` | beard_thin | 7 | 389 | 236 | 0 | 153 | 990 | 39% | already in template_trim, terrain_adaptation/soft |
| `illager_siege_weapons:trebuchet` | beard_thin | 1 | 231 | 91 | 0 | 140 | 2,100 | 61% | already in template_trim, terrain_adaptation/none |
| `terralith:glacial_hut` | beard_thin | 6 | 417 | 291 | 111 | 15 | 1,675 | 30% | already in template_trim, terrain_adaptation/soft |
| `villager_gold_washing_facility:villager_gold_washing_facility` | beard_thin | 1 | 378 | 253 | 125 | 0 | 1,494 | 33% | already in template_trim, terrain_adaptation/soft |
| `towns_and_towers:pillager_outpost_beach` | beard_thin | 4 | 395 | 271 | 20 | 104 | 1,426 | 31% | already in template_trim, terrain_adaptation/soft |
| `towns_and_towers:pillager_outpost_snowy_beach` | beard_thin | 4 | 395 | 275 | 22 | 98 | 1,371 | 30% | already in template_trim, terrain_adaptation/soft |
| `towns_and_towers:pillager_outpost_mushroom_fields` | beard_thin | 4 | 383 | 270 | 0 | 113 | 5,830 | 30% | already in template_trim, terrain_adaptation/soft |
| `villager_forge:villager_forge` | beard_thin | 1 | 408 | 299 | 109 | 0 | 1,717 | 27% | already in template_trim, terrain_adaptation/soft |
| `villager_archer_camp:villager_archer_camp` | beard_thin | 1 | 240 | 142 | 98 | 0 | 670 | 41% | already in template_trim, terrain_adaptation/soft |
| `saloon:saloon` | beard_thin | 1 | 399 | 304 | 95 | 0 | 2,280 | 24% | already in template_trim, terrain_adaptation/soft |
| `towns_and_towers:pillager_outpost_taiga` | beard_thin | 4 | 364 | 271 | 0 | 93 | 3,056 | 26% | already in template_trim, terrain_adaptation/soft |
| `plains_villager_tents:big_villager_tent` | beard_thin | 1 | 322 | 235 | 87 | 0 | 840 | 27% | already in template_trim, terrain_adaptation/soft |
| `villager_tannery:tannery` | beard_thin | 1 | 240 | 154 | 86 | 0 | 1,093 | 36% | already in template_trim, terrain_adaptation/soft |
| `villager_flower_house:flower_house` | beard_thin | 1 | 361 | 282 | 79 | 0 | 1,291 | 22% | already in template_trim, terrain_adaptation/soft |
| `illager_siege_weapons:catapult` | beard_thin | 1 | 144 | 69 | 0 | 75 | 900 | 52% | already in template_trim, terrain_adaptation/none |
| `snowy_tents:snowy_tent` | beard_thin | 1 | 143 | 68 | 75 | 0 | 534 | 52% | already in template_trim, terrain_adaptation/soft |
| `forest_ruins:ruin_3` | beard_thin | 1 | 238 | 170 | 68 | 0 | 519 | 29% | already in template_trim, terrain_adaptation/soft |
| `towns_and_towers:exclusives/pillager_outpost_swedish` | beard_thin | 4 | 363 | 295 | 0 | 68 | 2,405 | 19% | already in template_trim, terrain_adaptation/soft |
| `greek_villager_house:greek_house` | beard_thin | 1 | 270 | 203 | 67 | 0 | 785 | 25% | already in template_trim, terrain_adaptation/soft |
| `mysticrift_pillagers_towers_aris:acacia_pillager_tower` | beard_thin | 1 | 225 | 164 | 61 | 0 | 1,231 | 27% | already in template_trim, terrain_adaptation/soft |
| `mysticrift_pillagers_towers_aris:birch_pillager_tower` | beard_thin | 1 | 225 | 165 | 60 | 0 | 1,211 | 27% | already in template_trim, terrain_adaptation/soft |
| `mysticrift_pillagers_towers_aris:cherry_pillager_tower` | beard_thin | 1 | 225 | 165 | 60 | 0 | 1,225 | 27% | already in template_trim, terrain_adaptation/soft |
| `mysticrift_pillagers_towers_aris:end_pillager_tower` | beard_thin | 1 | 225 | 165 | 60 | 0 | 0 | 27% | already in template_trim, terrain_adaptation/soft |
| `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` | beard_thin | 1 | 225 | 165 | 60 | 0 | 1,220 | 27% | already in template_trim, terrain_adaptation/soft |
| `smallhorsestable:horse_stable` | beard_thin | 1 | 144 | 84 | 60 | 0 | 578 | 42% | already in template_trim, terrain_adaptation/soft |
| `mysticrift_pillagers_towers_aris:jungle_pillager_tower` | beard_thin | 1 | 225 | 168 | 57 | 0 | 1,157 | 25% | already in template_trim, terrain_adaptation/soft |
| `mysticrift_pillagers_towers_aris:mangrove_pillager_tower` | beard_thin | 1 | 225 | 168 | 57 | 0 | 1,145 | 25% | already in template_trim, terrain_adaptation/soft |
| `mysticrift_pillagers_towers_aris:desert_pillager_tower` | beard_thin | 1 | 225 | 169 | 56 | 0 | 1,172 | 25% | already in template_trim, terrain_adaptation/soft |
| `towns_and_towers:pillager_outpost_badlands` | beard_thin | 4 | 263 | 207 | 0 | 56 | 1,218 | 21% | already in template_trim, terrain_adaptation/soft |
| `beautify:botanist_house_desert` | beard_thin | 1 | 132 | 80 | 0 | 52 | 259 | 39% | already in template_trim, terrain_adaptation/soft |
| `villager_cartographer_house:villager_cartographer_house` | beard_thin | 1 | 221 | 172 | 49 | 0 | 907 | 22% | already in template_trim, terrain_adaptation/soft |
| `villager_cleric_house:villager_cleric_house` | beard_thin | 1 | 252 | 204 | 48 | 0 | 814 | 19% | already in template_trim, terrain_adaptation/soft |
| `towns_and_towers:pillager_outpost_savanna` | beard_thin | 4 | 172 | 127 | 2 | 43 | 1,422 | 26% | already in template_trim, terrain_adaptation/soft |
| `mini_clock_tower:mini_clock_tower` | beard_thin | 1 | 121 | 79 | 42 | 0 | 699 | 35% | already in template_trim, terrain_adaptation/soft |
| `forest_ruins:ruin_5` | beard_thin | 1 | 121 | 81 | 40 | 0 | 262 | 33% | already in template_trim, terrain_adaptation/soft |
| `oldvillagerstatue:old_villager_statue` | beard_thin | 1 | 108 | 69 | 39 | 0 | 574 | 36% | already in template_trim, terrain_adaptation/soft |
| `desert_village_temple:village_temple` | beard_thin | 1 | 841 | 803 | 38 | 0 | 1,140 | 4% | already in template_trim, terrain_adaptation/soft |
| `beautify:botanist_house_savanna` | beard_thin | 1 | 132 | 96 | 0 | 36 | 179 | 27% | already in template_trim, terrain_adaptation/soft |
| `illager_siege_weapons:ballista` | beard_thin | 1 | 91 | 56 | 0 | 35 | 210 | 38% | already in template_trim, terrain_adaptation/none |
| `desert_watchtower:watchtower` | beard_thin | 1 | 81 | 47 | 34 | 0 | 846 | 42% | already in template_trim, terrain_adaptation/soft |
| `towns_and_towers:pillager_outpost_meadow` | beard_thin | 4 | 172 | 139 | 1 | 32 | 1,107 | 19% | already in template_trim, terrain_adaptation/soft |
| `small_villager_greenhouse:small_villager_greenhouse` | beard_thin | 1 | 135 | 103 | 32 | 0 | 272 | 24% | already in template_trim, terrain_adaptation/soft |
| `beautify:botanist_house_plains` | beard_thin | 1 | 132 | 101 | 0 | 31 | 216 | 24% | already in template_trim, terrain_adaptation/soft |
| `beautify:botanist_house_snowy` | beard_thin | 1 | 132 | 101 | 0 | 31 | 247 | 24% | already in template_trim, terrain_adaptation/soft |
| `beautify:botanist_house_taiga` | beard_thin | 1 | 132 | 101 | 0 | 31 | 216 | 24% | already in template_trim, terrain_adaptation/soft |
| `forest_ruins:ruin_4` | beard_thin | 1 | 110 | 79 | 31 | 0 | 165 | 28% | already in template_trim, terrain_adaptation/soft |
| `villager_windmill:windmill` | beard_thin | 1 | 255 | 225 | 30 | 0 | 759 | 12% | already in template_trim, terrain_adaptation/soft |
| `villager_fish_shop:fish_shop` | beard_thin | 1 | 135 | 106 | 29 | 0 | 406 | 22% | already in template_trim, terrain_adaptation/soft |
| `forgotten_church:forgotten_church` | beard_thin | 1 | 190 | 162 | 28 | 0 | 748 | 15% | already in template_trim, terrain_adaptation/soft |
| `illager_siege_weapons:siege_tower` | beard_thin | 1 | 133 | 105 | 0 | 28 | 476 | 21% | already in template_trim, terrain_adaptation/none |
| `towns_and_towers:pillager_outpost_snowy_taiga` | beard_thin | 4 | 172 | 144 | 0 | 28 | 875 | 16% | already in template_trim, terrain_adaptation/soft |
| `villager_fishermans_hut:villager_fishermans_hut` | beard_thin | 1 | 210 | 183 | 27 | 0 | 405 | 13% | already in template_trim, terrain_adaptation/soft |
| `llama_stable:llama_stable` | beard_thin | 1 | 195 | 170 | 25 | 0 | 322 | 13% | already in template_trim, terrain_adaptation/soft |
| `animal_barn:barn` | beard_thin | 1 | 575 | 551 | 24 | 0 | 543 | 4% | already in template_trim, terrain_adaptation/soft |
| `small_cat_statues:cat_3` | beard_thin | 1 | 77 | 53 | 24 | 0 | 288 | 31% | already in template_trim, terrain_adaptation/soft |
| `forest_ruins:ruin_2` | beard_thin | 1 | 56 | 36 | 20 | 0 | 192 | 36% | already in template_trim, terrain_adaptation/soft |
| `illager_siege_weapons:battering_ram` | beard_thin | 1 | 60 | 42 | 0 | 18 | 126 | 30% | already in template_trim, terrain_adaptation/none |
| `forest_ruins:ruin_7` | beard_thin | 1 | 56 | 39 | 17 | 0 | 108 | 30% | already in template_trim, terrain_adaptation/soft |
| `ancient_enchantment_temple:temple` | beard_thin | 1 | 225 | 209 | 16 | 0 | 265 | 7% | already in template_trim, terrain_adaptation/soft |
| `forest_ruins:ruin_1` | beard_thin | 1 | 84 | 69 | 15 | 0 | 111 | 18% | already in template_trim, terrain_adaptation/soft |
| `small_cat_statues:cat_2` | beard_thin | 1 | 40 | 25 | 15 | 0 | 120 | 38% | already in template_trim, terrain_adaptation/soft |
| `illager_siege_weapons:reinforced_battering_ram` | beard_thin | 1 | 119 | 105 | 0 | 14 | 70 | 12% | already in template_trim, terrain_adaptation/none |
| `small_cat_statues:cat_1` | beard_thin | 1 | 40 | 26 | 14 | 0 | 126 | 35% | already in template_trim, terrain_adaptation/soft |
| `forest_ruins:ruin_6` | beard_thin | 1 | 63 | 50 | 13 | 0 | 106 | 21% | already in template_trim, terrain_adaptation/soft |
| `forest_watchtower:forest_watchtower` | beard_thin | 1 | 81 | 78 | 3 | 0 | 69 | 4% | already in template_trim, terrain_adaptation/soft |
| `small_viking_villager_house:small_viking_house` | beard_thin | 1 | 221 | 219 | 2 | 0 | 28 | 1% | already in template_trim, terrain_adaptation/soft |
| `old_illager_statue:old_illager_statue` | beard_thin | 1 | 20 | 20 | 0 | 0 | 0 | 0% | already in template_trim, terrain_adaptation/soft |

## 5. Compact surface structures

Built edge to edge; neither tag would change much.

| Structure | Adaptation | Templates | Box cols | Built | Pad cols | Air-only cols | Air outside built | Unbuilt | Why |
|---|---|---|---|---|---|---|---|---|---|
| `towns_and_towers:pillager_outpost_snowy_slopes` | beard_thin | 4 | 496 | 445 | 0 | 51 | 1,920 | 10% | 10% unbuilt |
| `towns_and_towers:pillager_outpost_birch_forest` | beard_thin | 4 | 444 | 409 | 9 | 26 | 981 | 8% | 8% unbuilt |
| `betterwitchhuts:witch_hut` | none | 5 | 381 | 308 | 0 | 4 | 6 | 19% | 19% unbuilt |
| `philipsruins:badlands_start_dungeon` | beard_thin | 2 | 436 | 432 | 0 | 4 | 27 | 1% | 1% unbuilt |
| `towns_and_towers:exclusives/pillager_outpost_classic` | beard_thin | 4 | 532 | 529 | 0 | 3 | 0 | 1% | 1% unbuilt |
| `towns_and_towers:exclusives/pillager_outpost_tudor` | beard_thin | 4 | 228 | 225 | 0 | 3 | 0 | 1% | 1% unbuilt |
| `towns_and_towers:pillager_outpost_flower_forest` | beard_thin | 4 | 172 | 169 | 0 | 3 | 0 | 2% | 2% unbuilt |
| `betterwitchhuts:witch_circle` | none | 3 | 83 | 61 | 0 | 2 | 0 | 26% | 26% unbuilt |
| `simply_houses:forest_chapel` | beard_thin | 1 | 375 | 262 | 0 | 2 | 14 | 30% | 30% unbuilt |
| `simply_houses:forest_house` | beard_thin | 1 | 575 | 389 | 0 | 0 | 0 | 32% | 32% unbuilt |
| `simply_houses:forest_stonemason` | beard_thin | 1 | 525 | 365 | 0 | 0 | 0 | 30% | 30% unbuilt |
| `simply_houses:plains_house` | beard_thin | 2 | 508 | 368 | 0 | 0 | 0 | 28% | 28% unbuilt |
| `simply_houses:plains_mansion` | beard_thin | 1 | 1056 | 862 | 0 | 0 | 0 | 18% | 18% unbuilt |
| `simply_houses:taiga_house` | beard_thin | 1 | 460 | 419 | 0 | 0 | 0 | 9% | 9% unbuilt |
| `simply_houses:taiga_inn` | beard_thin | 1 | 550 | 453 | 0 | 0 | 0 | 18% | 18% unbuilt |
| `simply_houses:taiga_woodcutter` | beard_thin | 1 | 475 | 413 | 0 | 0 | 0 | 13% | 13% unbuilt |

## 6. Not surface structures

Their air carves rooms, their water belongs, or they float on purpose: trim would break them. Left alone.

| Structure | Adaptation | Templates | Box cols | Built | Pad cols | Air-only cols | Air outside built | Unbuilt | Why |
|---|---|---|---|---|---|---|---|---|---|
| `minecraft:bastion_remnant` | none | 163 | 26095 | 17749 | 0 | 5896 | 21,529 | 32% | nether: its air and ground are part of the design |
| `philipsruins:ancient_ruins` | beard_thin | 46 | 3729 | 2634 | 1078 | 1 | 1 | 29% | seabed: its air and ground are part of the design |
| `mes:phantom_citadel` | none | 1 | 1406 | 539 | 0 | 867 | 30,345 | 62% | end: its air and ground are part of the design |
| `villagesandpillages:village_witch` | yung custom (carves true, beards false, kernel 24) | 27 | 2131 | 1524 | 0 | 607 | 4,725 | 28% | fixed height: its air and ground are part of the design |
| `betterdeserttemples:desert_temple` | none | 195 | 28698 | 27699 | 0 | 579 | 6,604 | 4% | buried: its air and ground are part of the design |
| `mes:enderkeep_courtyard` | beard_thin | 1 | 1548 | 1049 | 0 | 499 | 9,980 | 32% | end: its air and ground are part of the design |
| `betterfortresses:fortress` | yung custom (carves true, beards false, kernel 24) | 149 | 9481 | 8766 | 0 | 445 | 5,702 | 8% | fixed height: its air and ground are part of the design |
| `philipsruins:sculk_dungeon` | beard_thin | 4 | 2726 | 2330 | 0 | 396 | 4,108 | 14% | underground: its air and ground are part of the design |
| `mes:endscraps` | beard_thin | 4 | 343 | 53 | 103 | 187 | 1,422 | 84% | end: its air and ground are part of the design |
| `betterstrongholds:stronghold` | none | 83 | 6840 | 6571 | 0 | 259 | 897 | 4% | underground: its air and ground are part of the design |
| `philipsruins:ancient_towers` | beard_thin | 9 | 898 | 671 | 227 | 0 | 0 | 25% | seabed: its air and ground are part of the design |
| `mes:astral_meteorite` | beard_thin | 1 | 312 | 123 | 13 | 176 | 1,701 | 61% | end: its air and ground are part of the design |
| `mes:mythic_garden` | beard_thin | 1 | 361 | 178 | 79 | 104 | 2,087 | 51% | end: its air and ground are part of the design |
| `nullscape:phantom_nest` | none | 12 | 1330 | 917 | 0 | 145 | 3,418 | 31% | end: its air and ground are part of the design |
| `mes:enderwatch_tower` | beard_thin | 1 | 289 | 149 | 0 | 140 | 6,020 | 48% | end: its air and ground are part of the design |
| `hearths:warped_tower` | none | 6 | 438 | 313 | 0 | 125 | 280 | 28% | nether: its air and ground are part of the design |
| `farmers_structures:undergardens_sea` | none | 5 | 2716 | 2067 | 0 | 122 | 393 | 24% | underground: its air and ground are part of the design |
| `mes:monolith` | beard_thin | 3 | 363 | 243 | 0 | 120 | 3,000 | 33% | end: its air and ground are part of the design |
| `terralith:underground/frosted_dungeon` | beard_box | 1 | 625 | 508 | 0 | 116 | 1,332 | 19% | underground: its air and ground are part of the design |
| `mes:enderskog` | beard_thin | 1 | 182 | 68 | 0 | 114 | 1,026 | 63% | end: its air and ground are part of the design |
| `betterdungeons:small_nether_dungeon` | none | 83 | 1431 | 1073 | 0 | 112 | 158 | 25% | nether: its air and ground are part of the design |
| `mes:manuscript_shrine` | beard_thin | 1 | 210 | 108 | 0 | 102 | 919 | 49% | end: its air and ground are part of the design |
| `betterjungletemples:jungle_temple` | yung none | 118 | 38138 | 36456 | 0 | 95 | 167 | 4% | buried: its air and ground are part of the design |
| `hearths:crimson_tower` | none | 4 | 340 | 246 | 0 | 94 | 187 | 28% | nether: its air and ground are part of the design |
| `philipsruins:ancient_crypt` | beard_box | 8 | 1858 | 1776 | 82 | 0 | 309 | 4% | seabed: its air and ground are part of the design |
| `terralith:underground/sunken_tower` | beard_box | 1 | 210 | 132 | 53 | 25 | 929 | 37% | underground: its air and ground are part of the design |
| `farmers_structures:cooks_nether_2` | none | 1 | 480 | 394 | 0 | 77 | 440 | 18% | nether: its air and ground are part of the design |
| `farmers_structures:aethers` | none | 3 | 1070 | 601 | 0 | 56 | 145 | 44% | fixed height: its air and ground are part of the design |
| `mes:placid_prairie` | none | 2 | 170 | 122 | 0 | 48 | 651 | 28% | end: its air and ground are part of the design |
| `betterdungeons:skeleton_dungeon` | none | 58 | 2728 | 2443 | 0 | 40 | 180 | 10% | underground: its air and ground are part of the design |
| `mes:enderpin_spikes` | none | 2 | 50 | 17 | 0 | 33 | 298 | 66% | end: its air and ground are part of the design |
| `mes:ender_spire` | beard_thin | 1 | 49 | 17 | 0 | 32 | 544 | 65% | end: its air and ground are part of the design |
| `betterdungeons:zombie_dungeon` | none | 69 | 1999 | 1841 | 0 | 30 | 34 | 8% | fixed height: its air and ground are part of the design |
| `terralith:underground/old_refinery` | beard_box | 1 | 253 | 226 | 0 | 25 | 355 | 11% | underground: its air and ground are part of the design |
| `mes:enderbloom_grove` | beard_thin | 1 | 90 | 66 | 0 | 24 | 336 | 27% | end: its air and ground are part of the design |
| `mes:ruined_pillar` | beard_thin | 3 | 57 | 41 | 0 | 16 | 37 | 28% | end: its air and ground are part of the design |
| `betterdungeons:small_dungeon` | none | 16 | 527 | 508 | 0 | 13 | 17 | 4% | underground: its air and ground are part of the design |
| `mes:astral_hideaway` | beard_thin | 1 | 70 | 58 | 0 | 12 | 108 | 17% | end: its air and ground are part of the design |
| `philipsruins:underground_structures` | beard_thin | 10 | 2312 | 2302 | 10 | 0 | 48 | 0% | underground: its air and ground are part of the design |
| `terralith:underground/oak_cabin` | beard_box | 2 | 150 | 119 | 0 | 10 | 36 | 21% | underground: its air and ground are part of the design |
| `terralith:underground/witch_hut` | beard_box | 1 | 154 | 144 | 0 | 10 | 123 | 6% | underground: its air and ground are part of the design |
| `terralith:underground_cabin` | beard_box | 2 | 150 | 119 | 0 | 10 | 36 | 21% | underground: its air and ground are part of the design |
| `philipsruins:sealedruinedghaststatue1` | beard_thin | 3 | 109 | 81 | 0 | 9 | 39 | 26% | nether: its air and ground are part of the design |
| `farmers_structures:aquacultures_beach` | none | 1 | 504 | 393 | 0 | 5 | 12 | 22% | fixed height: its air and ground are part of the design |
| `philipsruins:start_nether_dungeon` | beard_thin | 7 | 2189 | 2186 | 0 | 3 | 25 | 0% | nether: its air and ground are part of the design |
| `philipsruins:ancient_dungeon` | bury | 13 | 1030 | 1028 | 2 | 0 | 12 | 0% | underground: its air and ground are part of the design |
| `nullscape:dragon_skeleton` | none | 8 | 15761 | 2417 | 0 | 1 | 1 | 85% | end: its air and ground are part of the design |
| `farmers_structures:cooks_nether_1` | none | 1 | 400 | 361 | 0 | 0 | 0 | 10% | nether: its air and ground are part of the design |
| `hearths:netherrack_spiral` | none | 2 | 21 | 20 | 0 | 0 | 0 | 5% | nether: its air and ground are part of the design |
| `minecraft:ancient_city` | beard_box | 53 | 24098 | 16820 | 0 | 0 | 0 | 30% | underground: its air and ground are part of the design |
| `philipsruins:antiquus_crypta` | beard_box | 4 | 985 | 985 | 0 | 0 | 0 | 0% | seabed: its air and ground are part of the design |
| `philipsruins:bone_dungeon` | beard_thin | 7 | 2212 | 2212 | 0 | 0 | 0 | 0% | underground: its air and ground are part of the design |
| `philipsruins:end_gate_fortress` | beard_thin | 4 | 1696 | 1696 | 0 | 0 | 0 | 0% | end: its air and ground are part of the design |
| `philipsruins:end_ruins` | beard_thin | 4 | 675 | 538 | 0 | 0 | 0 | 20% | end: its air and ground are part of the design |
| `philipsruins:lost_soul_city` | beard_thin | 5 | 4571 | 4571 | 0 | 0 | 0 | 0% | underground: its air and ground are part of the design |
| `philipsruins:lost_soul_dungeon` | beard_thin | 5 | 2627 | 2627 | 0 | 0 | 0 | 0% | underground: its air and ground are part of the design |
| `philipsruins:nether_lava_ruins` | beard_thin | 3 | 522 | 522 | 0 | 0 | 0 | 0% | nether: its air and ground are part of the design |
| `supplementaries:way_sign` | beard_thin | 1 | 49 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:underground/giant_bee_hive` | bury | 2 | 393 | 308 | 0 | 0 | 0 | 22% | underground: its air and ground are part of the design |
| `terralith:underground/mining_outpost` | beard_box | 2 | 157 | 141 | 0 | 0 | 0 | 10% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_badlands` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_bamboo_jungle` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_beach` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_birch_forest` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_cold_ocean` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_dark_forest` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_deep_cold_ocean` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_deep_frozen_ocean` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_deep_lukewarm_ocean` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_deep_ocean` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_deep_warm_ocean` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_desert` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_dripstone_caves` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_eroded_badlands` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_flower_forest` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_forest` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_frozen_ocean` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_frozen_peaks` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_frozen_river` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_grove` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_ice_spikes` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_jagged_peaks` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_jungle` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_lukewarm_ocean` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_lush_caves` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_meadow` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_mushroom_fields` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_ocean` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_old_growth_birch_forest` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_old_growth_pine_taiga` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_old_growth_spruce_taiga` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_plains` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_river` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_savanna` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_savanna_plateau` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_snowy_beach` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_snowy_plains` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_snowy_slopes` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_snowy_taiga` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_sparse_jungle` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_stony_peaks` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_stony_shore` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_sunflower_plains` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_swamp` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_taiga` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_warm_ocean` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_windswept_forest` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_windswept_gravelly_hills` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_windswept_hills` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_windswept_savanna` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/minecraft_wooded_badlands` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_alpha_islands` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_alpha_islands_winter` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_alpine_grove` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_alpine_highlands` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_amethyst_canyon` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_amethyst_rainforest` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_ancient_sands` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_arid_highlands` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_ashen_savanna` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_basalt_cliffs` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_birch_taiga` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_blooming_plateau` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_blooming_valley` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_brushland` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_bryce_canyon` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_caldera` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_cloud_forest` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_cold_shrubland` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_desert_canyon` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_desert_oasis` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_desert_spires` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_emerald_peaks` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_forested_highlands` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_fractured_savanna` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_frozen_cliffs` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_glacial_chasm` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_granite_cliffs` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_gravel_beach` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_gravel_desert` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_haze_mountain` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_highlands` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_hot_shrubland` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_ice_marsh` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_jungle_mountains` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_lavender_forest` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_lavender_valley` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_lush_valley` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_mirage_isles` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_moonlight_grove` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_moonlight_valley` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_mountain_steppe` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_orchid_swamp` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_painted_mountains` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_red_oasis` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_rocky_jungle` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_rocky_mountains` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_rocky_shrubland` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_sakura_grove` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_sakura_valley` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_sandstone_valley` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_savanna_badlands` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_savanna_slopes` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_scarlet_mountains` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_shield` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_shield_clearing` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_shrubland` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_siberian_grove` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_siberian_taiga` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_skylands` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_skylands_autumn` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_skylands_spring` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_skylands_summer` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_skylands_winter` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_snowy_badlands` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_snowy_maple_forest` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_snowy_shield` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_steppe` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_stony_spires` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_temperate_highlands` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_tropical_jungle` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_valley_clearing` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_volcanic_crater` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_volcanic_peaks` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_warm_river` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_warped_mesa` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_white_cliffs` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_white_mesa` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_windswept_spires` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_wintry_forest` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_yellowstone` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_yosemite_cliffs` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `terralith:zpointer/terralith_yosemite_lowlands` | none | 1 | 1 | 0 | 0 | 0 | 0 | 100% | underground: its air and ground are part of the design |
| `towns_and_towers:mimic_desert` | none | 3 | 642 | 618 | 0 | 0 | 0 | 4% | underground: its air and ground are part of the design |

## 7. Terrain-matching only, built in code, or no templates found

| Structure | Type | Note |
|---|---|---|
| `philipsruins:badlands_structures` | `minecraft:jigsaw` | every template is terrain_matching: placed column by column on the ground, no beard |
| `philipsruins:field_stone_ruins` | `minecraft:jigsaw` | every template is terrain_matching: placed column by column on the ground, no beard |
| `philipsruins:field_stone_ruins_rocks` | `minecraft:jigsaw` | every template is terrain_matching: placed column by column on the ground, no beard |
| `philipsruins:level_one_ruins` | `minecraft:jigsaw` | every template is terrain_matching: placed column by column on the ground, no beard |
| `philipsruins:pumpkin_ruins` | `minecraft:jigsaw` | every template is terrain_matching: placed column by column on the ground, no beard |
| `philipsruins:rare_ruin` | `minecraft:jigsaw` | every template is terrain_matching: placed column by column on the ground, no beard |
| `betterdungeons:spider_dungeon` | `betterdungeons:spider_dungeon` | built in code, no templates to read |
| `cloudstorage:big_balloon` | `cloudstorage:big_balloon` | built in code, no templates to read |
| `cloudstorage:sky_temple` | `cloudstorage:sky_temple` | built in code, no templates to read |
| `minecraft:shipwreck` | `minecraft:shipwreck` | built in code, no templates to read |
| `minecraft:shipwreck_beached` | `minecraft:shipwreck` | built in code, no templates to read |
| `philipsruins:level_two_ruins` | `minecraft:jigsaw` | no rigid template found (missing philipsruins:ruin_grave) |
| `philipsruins:level_two_ruins_pool` | `minecraft:jigsaw` | no rigid template found (missing philipsruins:deep_ruin14) |
| `twigs:bloodstone_obelisk` | `twigs:obelisk` | no rigid template found (missing twigs:bloodstone_obelisk) |
| `minecraft:igloo` | vanilla | template piece, adaptation none, sits on the surface |
| `minecraft:desert_pyramid` | vanilla | code-built (blocked by YUNG's Better Desert Temples) |
| `minecraft:jungle_pyramid` | vanilla | code-built (blocked by YUNG's Better Jungle Temples) |
| `minecraft:swamp_hut` | vanilla | code-built (blocked: Better Witch Huts) |
| `minecraft:mansion` | vanilla | template pieces, adaptation none; its own foundation code fills under it |
| `minecraft:monument` | vanilla | code-built, underwater |
| `minecraft:shipwreck` | vanilla | template piece, water |
| `minecraft:shipwreck_beached` | vanilla | template piece, beach |
| `minecraft:ocean_ruin_cold` | vanilla | template pieces, seabed |
| `minecraft:ocean_ruin_warm` | vanilla | template pieces, seabed |
| `minecraft:buried_treasure` | vanilla | one chest underground |
| `minecraft:mineshaft` | vanilla | code-built, underground |
| `minecraft:mineshaft_mesa` | vanilla | code-built, underground |
| `minecraft:stronghold` | vanilla | code-built, underground |
| `minecraft:fortress` | vanilla | code-built, Nether |
| `minecraft:end_city` | vanilla | template pieces, End |
| `minecraft:nether_fossil` | vanilla | template, Nether |
| `minecraft:ruined_portal` | vanilla | removed in this pack (structure_groups) |

