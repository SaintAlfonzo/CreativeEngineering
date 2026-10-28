# Pool tuning report

Rewritten by patcherMod `worldgen/PoolTuner` at every world load from `pool_config.json` + `pool_tags.json`. Don't edit.

- 1424 pools, 7246 pieces
- 39 pool(s) retuned
- 1 pool(s) restored to first-seen weights (a mod re-added its pieces on this world load, or a previous edit was reverted)

## Warnings

- mod/advancedperipherals: "advancedperipherals:*" matches no pool piece

## Retuned pools

Chance = share of the pool's total weight (each draw).

### `betterfortresses:bridge`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `betterfortresses:bridge/blaze_stairs` | 5 | 3.8% | 5 | 3.8% |  |
| `betterfortresses:bridge/blaze_stairs #2` | 5 | 3.8% | 5 | 3.8% |  |
| `betterfortresses:bridge/bridge_end` | 9 | 6.8% | 9 | 6.8% |  |
| `betterfortresses:bridge/bridge_junction_2` | 10 | 7.6% | 10 | 7.6% |  |
| `betterfortresses:bridge/bridge_junction_3` | 6 | 4.5% | 6 | 4.5% |  |
| `betterfortresses:bridge/bridge_junction_4` | 3 | 2.3% | 3 | 2.3% |  |
| `betterfortresses:bridge/bridge_pillar` | 10 | 7.6% | 20 | 15.2% | current 20 |
| `betterfortresses:bridge/bridge_pillar #2` | 15 | 11.4% | 10 | 7.6% | current 10 |
| `betterfortresses:bridge/bridge_pillar #3` | 20 | 15.2% | 15 | 11.4% | current 15 |
| `betterfortresses:bridge/bridge_stairs_l` | 1 | 0.8% | 1 | 0.8% |  |
| `betterfortresses:bridge/bridge_stairs_l #2` | 1 | 0.8% | 1 | 0.8% |  |
| `betterfortresses:bridge/bridge_stairs_r` | 1 | 0.8% | 1 | 0.8% |  |
| `betterfortresses:bridge/bridge_stairs_r #2` | 1 | 0.8% | 1 | 0.8% |  |
| `betterfortresses:bridge/bridge_start` | 45 | 34.1% | 45 | 34.1% |  |

### `minecraft:village/desert/houses`

Pool scaled x3 to hold fractional weights.

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `chefsdelight:desert_chef_house` | 5 | 2.3% | 15 | 4.0% |  |
| `chefsdelight:desert_cook_house` | 3 | 1.4% | 9 | 2.4% |  |
| `cloudstorage:balloon_stand_desert` | 2 | 0.9% | 6 | 1.6% |  |
| `domesticationinnovation:desert_petshop` | 17 | 7.7% | 51 | 13.7% |  |
| `dynamicvillage:desert/desert_mech` | 20 | 9.0% | **removed** | 0.0% | mod/dynamicvillage x0, tech x0 |
| `dynamicvillage:desert/desert_miner` | 20 | 9.0% | **removed** | 0.0% | mod/dynamicvillage x0, tech x0 |
| `dynamicvillage:desert/desert_plumber` | 20 | 9.0% | **removed** | 0.0% | mod/dynamicvillage x0, tech x0 |
| `dynamicvillage:desert/desert_train` | 20 | 9.0% | **removed** | 0.0% | mod/dynamicvillage x0, tech x0 |
| `etched:village/desert/houses/desert_bard_house_1` | 2 | 0.9% | 6 | 1.6% |  |
| `farmersdelight:village/houses/desert_compost_pile` | 3 | 1.4% | 9 | 2.4% |  |
| `immersiveengineering:village/houses/desert_electrician` | 1 | 0.5% | **removed** | 0.0% | mod/immersiveengineering x0, tech x0 |
| `immersiveengineering:village/houses/desert_engineer` | 1 | 0.5% | **removed** | 0.0% | mod/immersiveengineering x0, tech x0 |
| `immersiveengineering:village/houses/desert_gunsmith` | 1 | 0.5% | **removed** | 0.0% | mod/immersiveengineering x0, tech x0 |
| `immersiveengineering:village/houses/desert_machinist` | 1 | 0.5% | **removed** | 0.0% | mod/immersiveengineering x0, tech x0 |
| `immersiveengineering:village/houses/desert_outfitter` | 1 | 0.5% | **removed** | 0.0% | mod/immersiveengineering x0, tech x0 |
| `incubation:village/desert_chicken_coop_1` | 2 | 0.9% | 6 | 1.6% |  |
| `minecraft:empty` | 5 | 2.3% | 15 | 4.0% |  |
| `minecraft:village/desert/houses/desert_animal_pen_1` | 2 | 0.9% | 6 | 1.6% |  |
| `minecraft:village/desert/houses/desert_animal_pen_2` | 2 | 0.9% | 6 | 1.6% |  |
| `minecraft:village/desert/houses/desert_armorer_1` | 1 | 0.5% | 3 | 0.8% |  |
| `minecraft:village/desert/houses/desert_butcher_shop_1` | 2 | 0.9% | 6 | 1.6% |  |
| `minecraft:village/desert/houses/desert_cartographer_house_1` | 2 | 0.9% | 6 | 1.6% |  |
| `minecraft:village/desert/houses/desert_farm_1` | 4 | 1.8% | 12 | 3.2% |  |
| `minecraft:village/desert/houses/desert_farm_2` | 4 | 1.8% | 12 | 3.2% |  |
| `minecraft:village/desert/houses/desert_fisher_1` | 2 | 0.9% | 6 | 1.6% |  |
| `minecraft:village/desert/houses/desert_fletcher_house_1` | 2 | 0.9% | 6 | 1.6% |  |
| `minecraft:village/desert/houses/desert_large_farm_1` | 11 | 5.0% | 33 | 8.9% |  |
| `minecraft:village/desert/houses/desert_library_1` | 2 | 0.9% | 6 | 1.6% |  |
| `minecraft:village/desert/houses/desert_mason_1` | 2 | 0.9% | 6 | 1.6% |  |
| `minecraft:village/desert/houses/desert_medium_house_1` | 2 | 0.9% | 6 | 1.6% |  |
| `minecraft:village/desert/houses/desert_medium_house_2` | 2 | 0.9% | 6 | 1.6% |  |
| `minecraft:village/desert/houses/desert_shepherd_house_1` | 2 | 0.9% | 6 | 1.6% |  |
| `minecraft:village/desert/houses/desert_small_house_1` | 2 | 0.9% | 6 | 1.6% |  |
| `minecraft:village/desert/houses/desert_small_house_2` | 2 | 0.9% | 6 | 1.6% |  |
| `minecraft:village/desert/houses/desert_small_house_3` | 2 | 0.9% | 6 | 1.6% |  |
| `minecraft:village/desert/houses/desert_small_house_4` | 2 | 0.9% | 6 | 1.6% |  |
| `minecraft:village/desert/houses/desert_small_house_5` | 2 | 0.9% | 6 | 1.6% |  |
| `minecraft:village/desert/houses/desert_small_house_6` | 1 | 0.5% | 3 | 0.8% |  |
| `minecraft:village/desert/houses/desert_small_house_7` | 2 | 0.9% | 6 | 1.6% |  |
| `minecraft:village/desert/houses/desert_small_house_8` | 2 | 0.9% | 6 | 1.6% |  |
| `minecraft:village/desert/houses/desert_tannery_1` | 2 | 0.9% | 6 | 1.6% |  |
| `minecraft:village/desert/houses/desert_temple_1` | 2 | 0.9% | 6 | 1.6% |  |
| `minecraft:village/desert/houses/desert_temple_2` | 2 | 0.9% | 6 | 1.6% |  |
| `minecraft:village/desert/houses/desert_tool_smith_1` | 2 | 0.9% | 6 | 1.6% |  |
| `minecraft:village/desert/houses/desert_weaponsmith_1` | 2 | 0.9% | 6 | 1.6% |  |
| `monobank:village/houses/desert_vault` | 15 | 6.8% | 8 | 2.2% | monobank/vault_houses x0.18 |
| `veggiesdelight:village/houses/desert_depot_house` | 4 | 1.8% | 6 | 1.6% | mod/veggiesdelight x0.5 |
| `vinery:vinery_desert` | 2 | 0.9% | 9 | 2.4% | current 3 |
| `wares:village/houses/desert_warehouse` | 10 | 4.5% | 30 | 8.1% |  |

### `minecraft:village/desert/streets`

Pool scaled x2 to hold fractional weights.

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `beautify:botanist_house_desert` | 1 | 1.9% | 2 | 2.6% |  |
| `minecraft:village/desert/streets/corner_01` | 3 | 5.7% | 6 | 7.8% |  |
| `minecraft:village/desert/streets/corner_02` | 3 | 5.7% | 6 | 7.8% |  |
| `minecraft:village/desert/streets/crossroad_01` | 3 | 5.7% | 6 | 7.8% |  |
| `minecraft:village/desert/streets/crossroad_02` | 3 | 5.7% | 6 | 7.8% |  |
| `minecraft:village/desert/streets/crossroad_03` | 3 | 5.7% | 6 | 7.8% |  |
| `minecraft:village/desert/streets/square_01` | 3 | 5.7% | 6 | 7.8% |  |
| `minecraft:village/desert/streets/square_02` | 3 | 5.7% | 6 | 7.8% |  |
| `minecraft:village/desert/streets/straight_01` | 4 | 7.5% | 8 | 10.4% |  |
| `minecraft:village/desert/streets/straight_02` | 4 | 7.5% | 8 | 10.4% |  |
| `minecraft:village/desert/streets/straight_03` | 3 | 5.7% | 6 | 7.8% |  |
| `minecraft:village/desert/streets/turn_01` | 3 | 5.7% | 6 | 7.8% |  |
| `monobank:village/streets/desert_straight_fix_01` | 15 | 28.3% | 5 | 6.5% | monobank/vault_streets x0.16 |
| `pneumaticcraft:villages/custom_street_desert` | 2 | 3.8% | **removed** | 0.0% | mod/pneumaticcraft x0, tech x0 |

### `minecraft:village/plains/houses`

Pool scaled x3 to hold fractional weights.

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `chefsdelight:plains_chef_house` | 5 | 2.0% | 15 | 3.6% |  |
| `chefsdelight:plains_cook_house` | 2 | 0.8% | 6 | 1.4% |  |
| `cloudstorage:balloon_stand_plains` | 2 | 0.8% | 6 | 1.4% |  |
| `domesticationinnovation:plains_petshop` | 17 | 6.9% | 51 | 12.2% |  |
| `dynamicvillage:plains/plains_mech` | 20 | 8.1% | **removed** | 0.0% | mod/dynamicvillage x0, tech x0 |
| `dynamicvillage:plains/plains_miner` | 20 | 8.1% | **removed** | 0.0% | mod/dynamicvillage x0, tech x0 |
| `dynamicvillage:plains/plains_plumber` | 20 | 8.1% | **removed** | 0.0% | mod/dynamicvillage x0, tech x0 |
| `dynamicvillage:plains/plains_train` | 20 | 8.1% | **removed** | 0.0% | mod/dynamicvillage x0, tech x0 |
| `etched:village/plains/houses/plains_bard_house_1` | 2 | 0.8% | 6 | 1.4% |  |
| `farmersdelight:village/houses/plains_compost_pile` | 5 | 2.0% | 15 | 3.6% |  |
| `immersiveengineering:village/houses/plains_electrician` | 1 | 0.4% | **removed** | 0.0% | mod/immersiveengineering x0, tech x0 |
| `immersiveengineering:village/houses/plains_engineer` | 1 | 0.4% | **removed** | 0.0% | mod/immersiveengineering x0, tech x0 |
| `immersiveengineering:village/houses/plains_gunsmith` | 1 | 0.4% | **removed** | 0.0% | mod/immersiveengineering x0, tech x0 |
| `immersiveengineering:village/houses/plains_machinist` | 1 | 0.4% | **removed** | 0.0% | mod/immersiveengineering x0, tech x0 |
| `immersiveengineering:village/houses/plains_outfitter` | 1 | 0.4% | **removed** | 0.0% | mod/immersiveengineering x0, tech x0 |
| `incubation:village/plains_chicken_coop_1` | 2 | 0.8% | 6 | 1.4% |  |
| `minecraft:empty` | 10 | 4.1% | 30 | 7.2% |  |
| `minecraft:village/plains/houses/plains_accessory_1` | 1 | 0.4% | 3 | 0.7% |  |
| `minecraft:village/plains/houses/plains_animal_pen_1` | 1 | 0.4% | 3 | 0.7% |  |
| `minecraft:village/plains/houses/plains_animal_pen_2` | 1 | 0.4% | 3 | 0.7% |  |
| `minecraft:village/plains/houses/plains_animal_pen_3` | 5 | 2.0% | 15 | 3.6% |  |
| `minecraft:village/plains/houses/plains_armorer_house_1` | 2 | 0.8% | 6 | 1.4% |  |
| `minecraft:village/plains/houses/plains_big_house_1` | 2 | 0.8% | 6 | 1.4% |  |
| `minecraft:village/plains/houses/plains_butcher_shop_1` | 2 | 0.8% | 6 | 1.4% |  |
| `minecraft:village/plains/houses/plains_butcher_shop_2` | 2 | 0.8% | 6 | 1.4% |  |
| `minecraft:village/plains/houses/plains_cartographer_1` | 1 | 0.4% | 3 | 0.7% |  |
| `minecraft:village/plains/houses/plains_fisher_cottage_1` | 2 | 0.8% | 6 | 1.4% |  |
| `minecraft:village/plains/houses/plains_fletcher_house_1` | 2 | 0.8% | 6 | 1.4% |  |
| `minecraft:village/plains/houses/plains_large_farm_1` | 4 | 1.6% | 12 | 2.9% |  |
| `minecraft:village/plains/houses/plains_library_1` | 5 | 2.0% | 15 | 3.6% |  |
| `minecraft:village/plains/houses/plains_library_2` | 1 | 0.4% | 3 | 0.7% |  |
| `minecraft:village/plains/houses/plains_masons_house_1` | 2 | 0.8% | 6 | 1.4% |  |
| `minecraft:village/plains/houses/plains_medium_house_1` | 2 | 0.8% | 6 | 1.4% |  |
| `minecraft:village/plains/houses/plains_medium_house_2` | 2 | 0.8% | 6 | 1.4% |  |
| `minecraft:village/plains/houses/plains_meeting_point_4` | 3 | 1.2% | 9 | 2.1% |  |
| `minecraft:village/plains/houses/plains_meeting_point_5` | 1 | 0.4% | 3 | 0.7% |  |
| `minecraft:village/plains/houses/plains_shepherds_house_1` | 2 | 0.8% | 6 | 1.4% |  |
| `minecraft:village/plains/houses/plains_small_farm_1` | 4 | 1.6% | 12 | 2.9% |  |
| `minecraft:village/plains/houses/plains_small_house_1` | 2 | 0.8% | 6 | 1.4% |  |
| `minecraft:village/plains/houses/plains_small_house_2` | 2 | 0.8% | 6 | 1.4% |  |
| `minecraft:village/plains/houses/plains_small_house_3` | 2 | 0.8% | 6 | 1.4% |  |
| `minecraft:village/plains/houses/plains_small_house_4` | 2 | 0.8% | 6 | 1.4% |  |
| `minecraft:village/plains/houses/plains_small_house_5` | 2 | 0.8% | 6 | 1.4% |  |
| `minecraft:village/plains/houses/plains_small_house_6` | 1 | 0.4% | 3 | 0.7% |  |
| `minecraft:village/plains/houses/plains_small_house_7` | 2 | 0.8% | 6 | 1.4% |  |
| `minecraft:village/plains/houses/plains_small_house_8` | 3 | 1.2% | 9 | 2.1% |  |
| `minecraft:village/plains/houses/plains_stable_1` | 2 | 0.8% | 6 | 1.4% |  |
| `minecraft:village/plains/houses/plains_stable_2` | 2 | 0.8% | 6 | 1.4% |  |
| `minecraft:village/plains/houses/plains_tannery_1` | 2 | 0.8% | 6 | 1.4% |  |
| `minecraft:village/plains/houses/plains_temple_3` | 2 | 0.8% | 6 | 1.4% |  |
| `minecraft:village/plains/houses/plains_temple_4` | 2 | 0.8% | 6 | 1.4% |  |
| `minecraft:village/plains/houses/plains_tool_smith_1` | 2 | 0.8% | 6 | 1.4% |  |
| `minecraft:village/plains/houses/plains_weaponsmith_1` | 2 | 0.8% | 6 | 1.4% |  |
| `monobank:village/houses/plains_vault` | 15 | 6.1% | 8 | 1.9% | monobank/vault_houses x0.18 |
| `pneumaticcraft:villages/mechanic_house_plains` | 8 | 3.3% | **removed** | 0.0% | mod/pneumaticcraft x0, tech x0 |
| `veggiesdelight:village/houses/plains_depot_house` | 4 | 1.6% | 6 | 1.4% | mod/veggiesdelight x0.5 |
| `vinery:vinery_plains` | 2 | 0.8% | 9 | 2.1% | current 3 |
| `wares:village/houses/plains_warehouse` | 10 | 4.1% | 30 | 7.2% |  |

### `minecraft:village/plains/streets`

Pool scaled x2 to hold fractional weights.

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `beautify:botanist_house_plains` | 1 | 1.5% | 2 | 1.9% |  |
| `minecraft:village/plains/streets/corner_01` | 2 | 3.1% | 4 | 3.8% |  |
| `minecraft:village/plains/streets/corner_02` | 2 | 3.1% | 4 | 3.8% |  |
| `minecraft:village/plains/streets/corner_03` | 2 | 3.1% | 4 | 3.8% |  |
| `minecraft:village/plains/streets/crossroad_01` | 2 | 3.1% | 4 | 3.8% |  |
| `minecraft:village/plains/streets/crossroad_02` | 1 | 1.5% | 2 | 1.9% |  |
| `minecraft:village/plains/streets/crossroad_03` | 2 | 3.1% | 4 | 3.8% |  |
| `minecraft:village/plains/streets/crossroad_04` | 2 | 3.1% | 4 | 3.8% |  |
| `minecraft:village/plains/streets/crossroad_05` | 2 | 3.1% | 4 | 3.8% |  |
| `minecraft:village/plains/streets/crossroad_06` | 2 | 3.1% | 4 | 3.8% |  |
| `minecraft:village/plains/streets/straight_01` | 4 | 6.2% | 8 | 7.6% |  |
| `minecraft:village/plains/streets/straight_02` | 4 | 6.2% | 8 | 7.6% |  |
| `minecraft:village/plains/streets/straight_03` | 7 | 10.8% | 14 | 13.3% |  |
| `minecraft:village/plains/streets/straight_04` | 7 | 10.8% | 14 | 13.3% |  |
| `minecraft:village/plains/streets/straight_05` | 3 | 4.6% | 6 | 5.7% |  |
| `minecraft:village/plains/streets/straight_06` | 4 | 6.2% | 8 | 7.6% |  |
| `minecraft:village/plains/streets/turn_01` | 3 | 4.6% | 6 | 5.7% |  |
| `monobank:village/streets/plains_straight_fix_01` | 15 | 23.1% | 5 | 4.8% | monobank/vault_streets x0.16 |

### `minecraft:village/savanna/houses`

Pool scaled x3 to hold fractional weights.

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `chefsdelight:savanna_chef_house` | 5 | 2.1% | 15 | 3.7% |  |
| `chefsdelight:savanna_cook_house` | 3 | 1.2% | 9 | 2.2% |  |
| `cloudstorage:balloon_stand_savanna` | 2 | 0.8% | 6 | 1.5% |  |
| `domesticationinnovation:savanna_petshop` | 17 | 7.0% | 51 | 12.5% |  |
| `dynamicvillage:savanna/savanna_mech` | 20 | 8.3% | **removed** | 0.0% | mod/dynamicvillage x0, tech x0 |
| `dynamicvillage:savanna/savanna_miner` | 20 | 8.3% | **removed** | 0.0% | mod/dynamicvillage x0, tech x0 |
| `dynamicvillage:savanna/savanna_plumber` | 20 | 8.3% | **removed** | 0.0% | mod/dynamicvillage x0, tech x0 |
| `dynamicvillage:savanna/savanna_train` | 20 | 8.3% | **removed** | 0.0% | mod/dynamicvillage x0, tech x0 |
| `etched:village/savanna/houses/savanna_bard_house_1` | 4 | 1.7% | 12 | 2.9% |  |
| `farmersdelight:village/houses/savanna_compost_pile` | 4 | 1.7% | 12 | 2.9% |  |
| `immersiveengineering:village/houses/savanna_electrician` | 1 | 0.4% | **removed** | 0.0% | mod/immersiveengineering x0, tech x0 |
| `immersiveengineering:village/houses/savanna_engineer` | 1 | 0.4% | **removed** | 0.0% | mod/immersiveengineering x0, tech x0 |
| `immersiveengineering:village/houses/savanna_gunsmith` | 1 | 0.4% | **removed** | 0.0% | mod/immersiveengineering x0, tech x0 |
| `immersiveengineering:village/houses/savanna_machinist` | 1 | 0.4% | **removed** | 0.0% | mod/immersiveengineering x0, tech x0 |
| `immersiveengineering:village/houses/savanna_outfitter` | 1 | 0.4% | **removed** | 0.0% | mod/immersiveengineering x0, tech x0 |
| `incubation:village/savanna_chicken_coop_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:empty` | 5 | 2.1% | 15 | 3.7% |  |
| `minecraft:village/savanna/houses/savanna_animal_pen_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/savanna/houses/savanna_animal_pen_2` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/savanna/houses/savanna_animal_pen_3` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/savanna/houses/savanna_armorer_1` | 1 | 0.4% | 3 | 0.7% |  |
| `minecraft:village/savanna/houses/savanna_butchers_shop_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/savanna/houses/savanna_butchers_shop_2` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/savanna/houses/savanna_cartographer_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/savanna/houses/savanna_fisher_cottage_1` | 3 | 1.2% | 9 | 2.2% |  |
| `minecraft:village/savanna/houses/savanna_fletcher_house_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/savanna/houses/savanna_large_farm_1` | 4 | 1.7% | 12 | 2.9% |  |
| `minecraft:village/savanna/houses/savanna_large_farm_2` | 6 | 2.5% | 18 | 4.4% |  |
| `minecraft:village/savanna/houses/savanna_library_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/savanna/houses/savanna_mason_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/savanna/houses/savanna_medium_house_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/savanna/houses/savanna_medium_house_2` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/savanna/houses/savanna_shepherd_1` | 7 | 2.9% | 21 | 5.2% |  |
| `minecraft:village/savanna/houses/savanna_small_farm` | 4 | 1.7% | 12 | 2.9% |  |
| `minecraft:village/savanna/houses/savanna_small_house_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/savanna/houses/savanna_small_house_2` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/savanna/houses/savanna_small_house_3` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/savanna/houses/savanna_small_house_4` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/savanna/houses/savanna_small_house_5` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/savanna/houses/savanna_small_house_6` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/savanna/houses/savanna_small_house_7` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/savanna/houses/savanna_small_house_8` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/savanna/houses/savanna_tannery_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/savanna/houses/savanna_temple_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/savanna/houses/savanna_temple_2` | 3 | 1.2% | 9 | 2.2% |  |
| `minecraft:village/savanna/houses/savanna_tool_smith_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/savanna/houses/savanna_weaponsmith_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/savanna/houses/savanna_weaponsmith_2` | 2 | 0.8% | 6 | 1.5% |  |
| `monobank:village/houses/savanna_vault` | 15 | 6.2% | 8 | 2.0% | monobank/vault_houses x0.18 |
| `pneumaticcraft:villages/mechanic_house_savanna` | 8 | 3.3% | **removed** | 0.0% | mod/pneumaticcraft x0, tech x0 |
| `veggiesdelight:village/houses/savanna_depot_house` | 4 | 1.7% | 6 | 1.5% | mod/veggiesdelight x0.5 |
| `vinery:vinery_savanna` | 2 | 0.8% | 9 | 2.2% | current 3 |
| `wares:village/houses/savanna_warehouse` | 10 | 4.1% | 30 | 7.4% |  |

### `minecraft:village/savanna/streets`

Pool scaled x2 to hold fractional weights.

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `beautify:botanist_house_savanna` | 1 | 1.4% | 2 | 1.7% |  |
| `minecraft:village/savanna/streets/corner_01` | 2 | 2.8% | 4 | 3.4% |  |
| `minecraft:village/savanna/streets/corner_03` | 2 | 2.8% | 4 | 3.4% |  |
| `minecraft:village/savanna/streets/crossroad_02` | 1 | 1.4% | 2 | 1.7% |  |
| `minecraft:village/savanna/streets/crossroad_03` | 2 | 2.8% | 4 | 3.4% |  |
| `minecraft:village/savanna/streets/crossroad_04` | 2 | 2.8% | 4 | 3.4% |  |
| `minecraft:village/savanna/streets/crossroad_05` | 2 | 2.8% | 4 | 3.4% |  |
| `minecraft:village/savanna/streets/crossroad_06` | 2 | 2.8% | 4 | 3.4% |  |
| `minecraft:village/savanna/streets/crossroad_07` | 2 | 2.8% | 4 | 3.4% |  |
| `minecraft:village/savanna/streets/split_01` | 2 | 2.8% | 4 | 3.4% |  |
| `minecraft:village/savanna/streets/split_02` | 2 | 2.8% | 4 | 3.4% |  |
| `minecraft:village/savanna/streets/straight_02` | 4 | 5.6% | 8 | 6.7% |  |
| `minecraft:village/savanna/streets/straight_04` | 7 | 9.7% | 14 | 11.8% |  |
| `minecraft:village/savanna/streets/straight_05` | 3 | 4.2% | 6 | 5.0% |  |
| `minecraft:village/savanna/streets/straight_06` | 4 | 5.6% | 8 | 6.7% |  |
| `minecraft:village/savanna/streets/straight_08` | 4 | 5.6% | 8 | 6.7% |  |
| `minecraft:village/savanna/streets/straight_09` | 4 | 5.6% | 8 | 6.7% |  |
| `minecraft:village/savanna/streets/straight_10` | 4 | 5.6% | 8 | 6.7% |  |
| `minecraft:village/savanna/streets/straight_11` | 4 | 5.6% | 8 | 6.7% |  |
| `minecraft:village/savanna/streets/turn_01` | 3 | 4.2% | 6 | 5.0% |  |
| `monobank:village/streets/savanna_straight_fix_01` | 15 | 20.8% | 5 | 4.2% | monobank/vault_streets x0.16 |

### `minecraft:village/snowy/houses`

Pool scaled x3 to hold fractional weights.

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `chefsdelight:snowy_chef_house` | 4 | 1.8% | 12 | 3.4% |  |
| `chefsdelight:snowy_cook_house` | 3 | 1.4% | 9 | 2.5% |  |
| `cloudstorage:balloon_stand_snowy` | 2 | 0.9% | 6 | 1.7% |  |
| `domesticationinnovation:snowy_petshop` | 17 | 7.8% | 51 | 14.4% |  |
| `dynamicvillage:snowy/snowy_mech` | 20 | 9.2% | **removed** | 0.0% | mod/dynamicvillage x0, tech x0 |
| `dynamicvillage:snowy/snowy_miner` | 20 | 9.2% | **removed** | 0.0% | mod/dynamicvillage x0, tech x0 |
| `dynamicvillage:snowy/snowy_plumber` | 20 | 9.2% | **removed** | 0.0% | mod/dynamicvillage x0, tech x0 |
| `dynamicvillage:snowy/snowy_train` | 20 | 9.2% | **removed** | 0.0% | mod/dynamicvillage x0, tech x0 |
| `etched:village/snowy/houses/snowy_bard_house_1` | 4 | 1.8% | 12 | 3.4% |  |
| `farmersdelight:village/houses/snowy_compost_pile` | 3 | 1.4% | 9 | 2.5% |  |
| `immersiveengineering:village/houses/snowy_electrician` | 1 | 0.5% | **removed** | 0.0% | mod/immersiveengineering x0, tech x0 |
| `immersiveengineering:village/houses/snowy_engineer` | 1 | 0.5% | **removed** | 0.0% | mod/immersiveengineering x0, tech x0 |
| `immersiveengineering:village/houses/snowy_gunsmith` | 1 | 0.5% | **removed** | 0.0% | mod/immersiveengineering x0, tech x0 |
| `immersiveengineering:village/houses/snowy_machinist` | 1 | 0.5% | **removed** | 0.0% | mod/immersiveengineering x0, tech x0 |
| `immersiveengineering:village/houses/snowy_outfitter` | 1 | 0.5% | **removed** | 0.0% | mod/immersiveengineering x0, tech x0 |
| `incubation:village/snowy_chicken_coop_1` | 2 | 0.9% | 6 | 1.7% |  |
| `minecraft:empty` | 6 | 2.8% | 18 | 5.1% |  |
| `minecraft:village/snowy/houses/snowy_animal_pen_1` | 2 | 0.9% | 6 | 1.7% |  |
| `minecraft:village/snowy/houses/snowy_animal_pen_2` | 2 | 0.9% | 6 | 1.7% |  |
| `minecraft:village/snowy/houses/snowy_armorer_house_1` | 1 | 0.5% | 3 | 0.8% |  |
| `minecraft:village/snowy/houses/snowy_armorer_house_2` | 1 | 0.5% | 3 | 0.8% |  |
| `minecraft:village/snowy/houses/snowy_butchers_shop_1` | 2 | 0.9% | 6 | 1.7% |  |
| `minecraft:village/snowy/houses/snowy_butchers_shop_2` | 2 | 0.9% | 6 | 1.7% |  |
| `minecraft:village/snowy/houses/snowy_cartographer_house_1` | 2 | 0.9% | 6 | 1.7% |  |
| `minecraft:village/snowy/houses/snowy_farm_1` | 3 | 1.4% | 9 | 2.5% |  |
| `minecraft:village/snowy/houses/snowy_farm_2` | 3 | 1.4% | 9 | 2.5% |  |
| `minecraft:village/snowy/houses/snowy_fisher_cottage` | 2 | 0.9% | 6 | 1.7% |  |
| `minecraft:village/snowy/houses/snowy_fletcher_house_1` | 2 | 0.9% | 6 | 1.7% |  |
| `minecraft:village/snowy/houses/snowy_library_1` | 2 | 0.9% | 6 | 1.7% |  |
| `minecraft:village/snowy/houses/snowy_masons_house_1` | 2 | 0.9% | 6 | 1.7% |  |
| `minecraft:village/snowy/houses/snowy_masons_house_2` | 2 | 0.9% | 6 | 1.7% |  |
| `minecraft:village/snowy/houses/snowy_medium_house_1` | 2 | 0.9% | 6 | 1.7% |  |
| `minecraft:village/snowy/houses/snowy_medium_house_2` | 2 | 0.9% | 6 | 1.7% |  |
| `minecraft:village/snowy/houses/snowy_medium_house_3` | 2 | 0.9% | 6 | 1.7% |  |
| `minecraft:village/snowy/houses/snowy_shepherds_house_1` | 3 | 1.4% | 9 | 2.5% |  |
| `minecraft:village/snowy/houses/snowy_small_house_1` | 2 | 0.9% | 6 | 1.7% |  |
| `minecraft:village/snowy/houses/snowy_small_house_2` | 2 | 0.9% | 6 | 1.7% |  |
| `minecraft:village/snowy/houses/snowy_small_house_3` | 2 | 0.9% | 6 | 1.7% |  |
| `minecraft:village/snowy/houses/snowy_small_house_4` | 3 | 1.4% | 9 | 2.5% |  |
| `minecraft:village/snowy/houses/snowy_small_house_5` | 2 | 0.9% | 6 | 1.7% |  |
| `minecraft:village/snowy/houses/snowy_small_house_6` | 2 | 0.9% | 6 | 1.7% |  |
| `minecraft:village/snowy/houses/snowy_small_house_7` | 2 | 0.9% | 6 | 1.7% |  |
| `minecraft:village/snowy/houses/snowy_small_house_8` | 2 | 0.9% | 6 | 1.7% |  |
| `minecraft:village/snowy/houses/snowy_tannery_1` | 2 | 0.9% | 6 | 1.7% |  |
| `minecraft:village/snowy/houses/snowy_temple_1` | 2 | 0.9% | 6 | 1.7% |  |
| `minecraft:village/snowy/houses/snowy_tool_smith_1` | 2 | 0.9% | 6 | 1.7% |  |
| `minecraft:village/snowy/houses/snowy_weapon_smith_1` | 2 | 0.9% | 6 | 1.7% |  |
| `monobank:village/houses/snowy_vault` | 15 | 6.9% | 8 | 2.3% | monobank/vault_houses x0.18 |
| `veggiesdelight:village/houses/snowy_depot_house` | 4 | 1.8% | 6 | 1.7% | mod/veggiesdelight x0.5 |
| `wares:village/houses/snowy_warehouse` | 10 | 4.6% | 30 | 8.5% |  |

### `minecraft:village/snowy/streets`

Pool scaled x2 to hold fractional weights.

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `beautify:botanist_house_snowy` | 1 | 1.5% | 2 | 2.0% |  |
| `minecraft:village/snowy/streets/corner_01` | 2 | 3.1% | 4 | 4.0% |  |
| `minecraft:village/snowy/streets/corner_02` | 2 | 3.1% | 4 | 4.0% |  |
| `minecraft:village/snowy/streets/corner_03` | 2 | 3.1% | 4 | 4.0% |  |
| `minecraft:village/snowy/streets/crossroad_02` | 1 | 1.5% | 2 | 2.0% |  |
| `minecraft:village/snowy/streets/crossroad_03` | 2 | 3.1% | 4 | 4.0% |  |
| `minecraft:village/snowy/streets/crossroad_04` | 2 | 3.1% | 4 | 4.0% |  |
| `minecraft:village/snowy/streets/crossroad_05` | 2 | 3.1% | 4 | 4.0% |  |
| `minecraft:village/snowy/streets/crossroad_06` | 2 | 3.1% | 4 | 4.0% |  |
| `minecraft:village/snowy/streets/square_01` | 2 | 3.1% | 4 | 4.0% |  |
| `minecraft:village/snowy/streets/straight_01` | 4 | 6.2% | 8 | 7.9% |  |
| `minecraft:village/snowy/streets/straight_02` | 4 | 6.2% | 8 | 7.9% |  |
| `minecraft:village/snowy/streets/straight_03` | 4 | 6.2% | 8 | 7.9% |  |
| `minecraft:village/snowy/streets/straight_04` | 7 | 10.8% | 14 | 13.9% |  |
| `minecraft:village/snowy/streets/straight_06` | 4 | 6.2% | 8 | 7.9% |  |
| `minecraft:village/snowy/streets/straight_08` | 4 | 6.2% | 8 | 7.9% |  |
| `minecraft:village/snowy/streets/turn_01` | 3 | 4.6% | 6 | 5.9% |  |
| `monobank:village/streets/snowy_straight_fix_01` | 15 | 23.1% | 5 | 5.0% | monobank/vault_streets x0.16 |
| `pneumaticcraft:villages/custom_street_snowy` | 2 | 3.1% | **removed** | 0.0% | mod/pneumaticcraft x0, tech x0 |

### `minecraft:village/taiga/houses`

Pool scaled x3 to hold fractional weights.

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `chefsdelight:taiga_chef_house` | 4 | 1.7% | 12 | 3.1% |  |
| `chefsdelight:taiga_cook_house` | 3 | 1.3% | 9 | 2.3% |  |
| `cloudstorage:balloon_stand_taiga` | 2 | 0.8% | 6 | 1.5% |  |
| `domesticationinnovation:taiga_petshop` | 17 | 7.2% | 51 | 13.0% |  |
| `dynamicvillage:taiga/taiga_mech` | 20 | 8.5% | **removed** | 0.0% | mod/dynamicvillage x0, tech x0 |
| `dynamicvillage:taiga/taiga_miner` | 20 | 8.5% | **removed** | 0.0% | mod/dynamicvillage x0, tech x0 |
| `dynamicvillage:taiga/taiga_plumber` | 20 | 8.5% | **removed** | 0.0% | mod/dynamicvillage x0, tech x0 |
| `dynamicvillage:taiga/taiga_train` | 20 | 8.5% | **removed** | 0.0% | mod/dynamicvillage x0, tech x0 |
| `etched:village/taiga/houses/taiga_bard_house_1` | 4 | 1.7% | 12 | 3.1% |  |
| `farmersdelight:village/houses/taiga_compost_pile` | 4 | 1.7% | 12 | 3.1% |  |
| `immersiveengineering:village/houses/taiga_electrician` | 1 | 0.4% | **removed** | 0.0% | mod/immersiveengineering x0, tech x0 |
| `immersiveengineering:village/houses/taiga_engineer` | 1 | 0.4% | **removed** | 0.0% | mod/immersiveengineering x0, tech x0 |
| `immersiveengineering:village/houses/taiga_gunsmith` | 1 | 0.4% | **removed** | 0.0% | mod/immersiveengineering x0, tech x0 |
| `immersiveengineering:village/houses/taiga_machinist` | 1 | 0.4% | **removed** | 0.0% | mod/immersiveengineering x0, tech x0 |
| `immersiveengineering:village/houses/taiga_outfitter` | 1 | 0.4% | **removed** | 0.0% | mod/immersiveengineering x0, tech x0 |
| `incubation:village/taiga_chicken_coop_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:empty` | 6 | 2.5% | 18 | 4.6% |  |
| `minecraft:village/taiga/houses/taiga_animal_pen_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/taiga/houses/taiga_armorer_2` | 1 | 0.4% | 3 | 0.8% |  |
| `minecraft:village/taiga/houses/taiga_armorer_house_1` | 1 | 0.4% | 3 | 0.8% |  |
| `minecraft:village/taiga/houses/taiga_butcher_shop_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/taiga/houses/taiga_cartographer_house_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/taiga/houses/taiga_fisher_cottage_1` | 3 | 1.3% | 9 | 2.3% |  |
| `minecraft:village/taiga/houses/taiga_fletcher_house_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/taiga/houses/taiga_large_farm_1` | 6 | 2.5% | 18 | 4.6% |  |
| `minecraft:village/taiga/houses/taiga_large_farm_2` | 6 | 2.5% | 18 | 4.6% |  |
| `minecraft:village/taiga/houses/taiga_library_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/taiga/houses/taiga_masons_house_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/taiga/houses/taiga_medium_house_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/taiga/houses/taiga_medium_house_2` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/taiga/houses/taiga_medium_house_3` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/taiga/houses/taiga_medium_house_4` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/taiga/houses/taiga_shepherds_house_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/taiga/houses/taiga_small_farm_1` | 1 | 0.4% | 3 | 0.8% |  |
| `minecraft:village/taiga/houses/taiga_small_house_1` | 4 | 1.7% | 12 | 3.1% |  |
| `minecraft:village/taiga/houses/taiga_small_house_2` | 4 | 1.7% | 12 | 3.1% |  |
| `minecraft:village/taiga/houses/taiga_small_house_3` | 4 | 1.7% | 12 | 3.1% |  |
| `minecraft:village/taiga/houses/taiga_small_house_4` | 4 | 1.7% | 12 | 3.1% |  |
| `minecraft:village/taiga/houses/taiga_small_house_5` | 4 | 1.7% | 12 | 3.1% |  |
| `minecraft:village/taiga/houses/taiga_tannery_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/taiga/houses/taiga_temple_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/taiga/houses/taiga_tool_smith_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/taiga/houses/taiga_weaponsmith_1` | 2 | 0.8% | 6 | 1.5% |  |
| `minecraft:village/taiga/houses/taiga_weaponsmith_2` | 2 | 0.8% | 6 | 1.5% |  |
| `monobank:village/houses/taiga_vault` | 15 | 6.4% | 8 | 2.0% | monobank/vault_houses x0.18 |
| `pneumaticcraft:villages/mechanic_house_taiga` | 8 | 3.4% | **removed** | 0.0% | mod/pneumaticcraft x0, tech x0 |
| `veggiesdelight:village/houses/taiga_depot_house` | 4 | 1.7% | 6 | 1.5% | mod/veggiesdelight x0.5 |
| `vinery:vinery_taiga` | 2 | 0.8% | 11 | 2.8% | current 3.5 |
| `wares:village/houses/taiga_warehouse` | 10 | 4.2% | 30 | 7.7% |  |

### `minecraft:village/taiga/streets`

Pool scaled x2 to hold fractional weights.

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `beautify:botanist_house_taiga` | 1 | 1.5% | 2 | 1.9% |  |
| `minecraft:village/taiga/streets/corner_01` | 2 | 3.1% | 4 | 3.8% |  |
| `minecraft:village/taiga/streets/corner_02` | 2 | 3.1% | 4 | 3.8% |  |
| `minecraft:village/taiga/streets/corner_03` | 2 | 3.1% | 4 | 3.8% |  |
| `minecraft:village/taiga/streets/crossroad_01` | 1 | 1.5% | 2 | 1.9% |  |
| `minecraft:village/taiga/streets/crossroad_02` | 1 | 1.5% | 2 | 1.9% |  |
| `minecraft:village/taiga/streets/crossroad_03` | 2 | 3.1% | 4 | 3.8% |  |
| `minecraft:village/taiga/streets/crossroad_04` | 2 | 3.1% | 4 | 3.8% |  |
| `minecraft:village/taiga/streets/crossroad_05` | 2 | 3.1% | 4 | 3.8% |  |
| `minecraft:village/taiga/streets/crossroad_06` | 2 | 3.1% | 4 | 3.8% |  |
| `minecraft:village/taiga/streets/straight_01` | 4 | 6.2% | 8 | 7.6% |  |
| `minecraft:village/taiga/streets/straight_02` | 4 | 6.2% | 8 | 7.6% |  |
| `minecraft:village/taiga/streets/straight_03` | 4 | 6.2% | 8 | 7.6% |  |
| `minecraft:village/taiga/streets/straight_04` | 7 | 10.8% | 14 | 13.3% |  |
| `minecraft:village/taiga/streets/straight_05` | 7 | 10.8% | 14 | 13.3% |  |
| `minecraft:village/taiga/streets/straight_06` | 4 | 6.2% | 8 | 7.6% |  |
| `minecraft:village/taiga/streets/turn_01` | 3 | 4.6% | 6 | 5.7% |  |
| `monobank:village/streets/taiga_straight_fix_01` | 15 | 23.1% | 5 | 4.8% | monobank/vault_streets x0.16 |

### `nullscape:dragon_skeleton`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `nullscape:skeleton/1` | 2 | 1.8% | 2 | 1.8% |  |
| `nullscape:skeleton/1 #2` | 3 | 2.7% | 3 | 2.7% |  |
| `nullscape:skeleton/1 #3` | 4 | 3.6% | 5 | 4.5% | current 5 |
| `nullscape:skeleton/1 #4` | 5 | 4.5% | 4 | 3.6% | current 4 |
| `nullscape:skeleton/2` | 2 | 1.8% | 2 | 1.8% |  |
| `nullscape:skeleton/2 #2` | 3 | 2.7% | 3 | 2.7% |  |
| `nullscape:skeleton/2 #3` | 4 | 3.6% | 5 | 4.5% | current 5 |
| `nullscape:skeleton/2 #4` | 5 | 4.5% | 4 | 3.6% | current 4 |
| `nullscape:skeleton/3` | 2 | 1.8% | 2 | 1.8% |  |
| `nullscape:skeleton/3 #2` | 3 | 2.7% | 3 | 2.7% |  |
| `nullscape:skeleton/3 #3` | 4 | 3.6% | 5 | 4.5% | current 5 |
| `nullscape:skeleton/3 #4` | 5 | 4.5% | 4 | 3.6% | current 4 |
| `nullscape:skeleton/4` | 2 | 1.8% | 2 | 1.8% |  |
| `nullscape:skeleton/4 #2` | 3 | 2.7% | 3 | 2.7% |  |
| `nullscape:skeleton/4 #3` | 4 | 3.6% | 5 | 4.5% | current 5 |
| `nullscape:skeleton/4 #4` | 5 | 4.5% | 4 | 3.6% | current 4 |
| `nullscape:skeleton/5` | 2 | 1.8% | 2 | 1.8% |  |
| `nullscape:skeleton/5 #2` | 3 | 2.7% | 3 | 2.7% |  |
| `nullscape:skeleton/5 #3` | 4 | 3.6% | 5 | 4.5% | current 5 |
| `nullscape:skeleton/5 #4` | 5 | 4.5% | 4 | 3.6% | current 4 |
| `nullscape:skeleton/6` | 2 | 1.8% | 2 | 1.8% |  |
| `nullscape:skeleton/6 #2` | 3 | 2.7% | 3 | 2.7% |  |
| `nullscape:skeleton/6 #3` | 4 | 3.6% | 5 | 4.5% | current 5 |
| `nullscape:skeleton/6 #4` | 5 | 4.5% | 4 | 3.6% | current 4 |
| `nullscape:skeleton/7` | 2 | 1.8% | 2 | 1.8% |  |
| `nullscape:skeleton/7 #2` | 3 | 2.7% | 3 | 2.7% |  |
| `nullscape:skeleton/7 #3` | 4 | 3.6% | 5 | 4.5% | current 5 |
| `nullscape:skeleton/7 #4` | 5 | 4.5% | 4 | 3.6% | current 4 |
| `nullscape:skeleton/8` | 2 | 1.8% | 2 | 1.8% |  |
| `nullscape:skeleton/8 #2` | 3 | 2.7% | 3 | 2.7% |  |
| `nullscape:skeleton/8 #3` | 4 | 3.6% | 5 | 4.5% | current 5 |
| `nullscape:skeleton/8 #4` | 5 | 4.5% | 4 | 3.6% | current 4 |

### `nullscape:dragon_skeleton_boneyard`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `nullscape:skeleton/1` | 2 | 1.8% | 2 | 1.8% |  |
| `nullscape:skeleton/1 #2` | 3 | 2.7% | 3 | 2.7% |  |
| `nullscape:skeleton/1 #3` | 4 | 3.6% | 5 | 4.5% | current 5 |
| `nullscape:skeleton/1 #4` | 5 | 4.5% | 4 | 3.6% | current 4 |
| `nullscape:skeleton/2` | 2 | 1.8% | 2 | 1.8% |  |
| `nullscape:skeleton/2 #2` | 3 | 2.7% | 3 | 2.7% |  |
| `nullscape:skeleton/2 #3` | 4 | 3.6% | 5 | 4.5% | current 5 |
| `nullscape:skeleton/2 #4` | 5 | 4.5% | 4 | 3.6% | current 4 |
| `nullscape:skeleton/3` | 2 | 1.8% | 2 | 1.8% |  |
| `nullscape:skeleton/3 #2` | 3 | 2.7% | 3 | 2.7% |  |
| `nullscape:skeleton/3 #3` | 4 | 3.6% | 5 | 4.5% | current 5 |
| `nullscape:skeleton/3 #4` | 5 | 4.5% | 4 | 3.6% | current 4 |
| `nullscape:skeleton/4` | 2 | 1.8% | 2 | 1.8% |  |
| `nullscape:skeleton/4 #2` | 3 | 2.7% | 3 | 2.7% |  |
| `nullscape:skeleton/4 #3` | 4 | 3.6% | 5 | 4.5% | current 5 |
| `nullscape:skeleton/4 #4` | 5 | 4.5% | 4 | 3.6% | current 4 |
| `nullscape:skeleton/5` | 2 | 1.8% | 2 | 1.8% |  |
| `nullscape:skeleton/5 #2` | 3 | 2.7% | 3 | 2.7% |  |
| `nullscape:skeleton/5 #3` | 4 | 3.6% | 5 | 4.5% | current 5 |
| `nullscape:skeleton/5 #4` | 5 | 4.5% | 4 | 3.6% | current 4 |
| `nullscape:skeleton/6` | 2 | 1.8% | 2 | 1.8% |  |
| `nullscape:skeleton/6 #2` | 3 | 2.7% | 3 | 2.7% |  |
| `nullscape:skeleton/6 #3` | 4 | 3.6% | 5 | 4.5% | current 5 |
| `nullscape:skeleton/6 #4` | 5 | 4.5% | 4 | 3.6% | current 4 |
| `nullscape:skeleton/7` | 2 | 1.8% | 2 | 1.8% |  |
| `nullscape:skeleton/7 #2` | 3 | 2.7% | 3 | 2.7% |  |
| `nullscape:skeleton/7 #3` | 4 | 3.6% | 5 | 4.5% | current 5 |
| `nullscape:skeleton/7 #4` | 5 | 4.5% | 4 | 3.6% | current 4 |
| `nullscape:skeleton/8` | 2 | 1.8% | 2 | 1.8% |  |
| `nullscape:skeleton/8 #2` | 3 | 2.7% | 3 | 2.7% |  |
| `nullscape:skeleton/8 #3` | 4 | 3.6% | 5 | 4.5% | current 5 |
| `nullscape:skeleton/8 #4` | 5 | 4.5% | 4 | 3.6% | current 4 |

### `pneumaticcraft:village/desert/houses`

> every piece removed; the pool now holds only minecraft:empty (a start pool that is empty generates nothing, a houses pool leaves empty lots)

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `pneumaticcraft:villages/mechanic_house_desert` | 1 | 100.0% | **removed** | - | mod/pneumaticcraft x0, tech x0 |

### `pneumaticcraft:village/snowy/houses`

> every piece removed; the pool now holds only minecraft:empty (a start pool that is empty generates nothing, a houses pool leaves empty lots)

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `pneumaticcraft:villages/mechanic_house_snowy` | 1 | 100.0% | **removed** | - | mod/pneumaticcraft x0, tech x0 |

### `subterrestrial:cabin/deepslate/base`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `subterrestrial:cabin/deepslate/base_0` | 5 | 0.8% | 100 | 15.9% | current 100 |
| `subterrestrial:cabin/deepslate/base_0 #2` | 100 | 15.9% | 5 | 0.8% | current 5 |
| `subterrestrial:cabin/deepslate/base_1` | 5 | 0.8% | 100 | 15.9% | current 100 |
| `subterrestrial:cabin/deepslate/base_1 #2` | 100 | 15.9% | 5 | 0.8% | current 5 |
| `subterrestrial:cabin/deepslate/base_lower_0` | 5 | 0.8% | 100 | 15.9% | current 100 |
| `subterrestrial:cabin/deepslate/base_lower_0 #2` | 100 | 15.9% | 5 | 0.8% | current 5 |
| `subterrestrial:cabin/deepslate/base_lower_1` | 5 | 0.8% | 100 | 15.9% | current 100 |
| `subterrestrial:cabin/deepslate/base_lower_1 #2` | 100 | 15.9% | 5 | 0.8% | current 5 |
| `subterrestrial:cabin/deepslate/base_upper_0` | 5 | 0.8% | 100 | 15.9% | current 100 |
| `subterrestrial:cabin/deepslate/base_upper_0 #2` | 100 | 15.9% | 5 | 0.8% | current 5 |
| `subterrestrial:cabin/deepslate/base_upper_1` | 5 | 0.8% | 100 | 15.9% | current 100 |
| `subterrestrial:cabin/deepslate/base_upper_1 #2` | 100 | 15.9% | 5 | 0.8% | current 5 |

### `subterrestrial:cabin/deepslate/bottom`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `subterrestrial:cabin/default/bottom_0` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/default/bottom_0 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/default/bottom_1` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/default/bottom_1 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/default/bottom_2` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/default/bottom_2 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/default/bottom_3` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/default/bottom_3 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |

### `subterrestrial:cabin/deepslate/top`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `subterrestrial:cabin/default/top_0` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/default/top_0 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/default/top_1` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/default/top_1 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/default/top_2` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/default/top_2 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/default/top_3` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/default/top_3 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |

### `subterrestrial:cabin/default/base`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `subterrestrial:cabin/default/base_0` | 5 | 0.8% | 100 | 15.9% | current 100 |
| `subterrestrial:cabin/default/base_0 #2` | 100 | 15.9% | 5 | 0.8% | current 5 |
| `subterrestrial:cabin/default/base_1` | 5 | 0.8% | 100 | 15.9% | current 100 |
| `subterrestrial:cabin/default/base_1 #2` | 100 | 15.9% | 5 | 0.8% | current 5 |
| `subterrestrial:cabin/default/base_lower_0` | 5 | 0.8% | 100 | 15.9% | current 100 |
| `subterrestrial:cabin/default/base_lower_0 #2` | 100 | 15.9% | 5 | 0.8% | current 5 |
| `subterrestrial:cabin/default/base_lower_1` | 5 | 0.8% | 100 | 15.9% | current 100 |
| `subterrestrial:cabin/default/base_lower_1 #2` | 100 | 15.9% | 5 | 0.8% | current 5 |
| `subterrestrial:cabin/default/base_upper_0` | 5 | 0.8% | 100 | 15.9% | current 100 |
| `subterrestrial:cabin/default/base_upper_0 #2` | 100 | 15.9% | 5 | 0.8% | current 5 |
| `subterrestrial:cabin/default/base_upper_1` | 5 | 0.8% | 100 | 15.9% | current 100 |
| `subterrestrial:cabin/default/base_upper_1 #2` | 100 | 15.9% | 5 | 0.8% | current 5 |

### `subterrestrial:cabin/default/bottom`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `subterrestrial:cabin/default/bottom_0` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/default/bottom_0 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/default/bottom_1` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/default/bottom_1 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/default/bottom_2` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/default/bottom_2 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/default/bottom_3` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/default/bottom_3 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |

### `subterrestrial:cabin/default/top`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `subterrestrial:cabin/default/top_0` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/default/top_0 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/default/top_1` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/default/top_1 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/default/top_2` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/default/top_2 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/default/top_3` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/default/top_3 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |

### `subterrestrial:cabin/desert/base`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `subterrestrial:cabin/desert/base_0` | 5 | 1.6% | 100 | 31.7% | current 100 |
| `subterrestrial:cabin/desert/base_0 #2` | 100 | 31.7% | 5 | 1.6% | current 5 |
| `subterrestrial:cabin/desert/base_lower_0` | 5 | 1.6% | 100 | 31.7% | current 100 |
| `subterrestrial:cabin/desert/base_lower_0 #2` | 100 | 31.7% | 5 | 1.6% | current 5 |
| `subterrestrial:cabin/desert/base_upper_0` | 5 | 1.6% | 100 | 31.7% | current 100 |
| `subterrestrial:cabin/desert/base_upper_0 #2` | 100 | 31.7% | 5 | 1.6% | current 5 |

### `subterrestrial:cabin/desert/bottom`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `subterrestrial:cabin/desert/bottom_0` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/desert/bottom_0 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/desert/bottom_1` | 100 | 23.8% | 100 | 23.8% |  |
| `subterrestrial:cabin/desert/bottom_2` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/desert/bottom_2 #2` | 5 | 1.2% | 5 | 1.2% |  |
| `subterrestrial:cabin/desert/bottom_2 #3` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/desert/bottom_3` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/desert/bottom_3 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |

### `subterrestrial:cabin/desert/top`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `subterrestrial:cabin/desert/top_0` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/desert/top_0 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/desert/top_1` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/desert/top_1 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/desert/top_2` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/desert/top_2 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/desert/top_3` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/desert/top_3 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |

### `subterrestrial:cabin/ice/base`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `subterrestrial:cabin/ice/base_0` | 5 | 1.6% | 100 | 31.7% | current 100 |
| `subterrestrial:cabin/ice/base_0 #2` | 100 | 31.7% | 5 | 1.6% | current 5 |
| `subterrestrial:cabin/ice/base_lower_0` | 5 | 1.6% | 100 | 31.7% | current 100 |
| `subterrestrial:cabin/ice/base_lower_0 #2` | 100 | 31.7% | 5 | 1.6% | current 5 |
| `subterrestrial:cabin/ice/base_upper_0` | 5 | 1.6% | 100 | 31.7% | current 100 |
| `subterrestrial:cabin/ice/base_upper_0 #2` | 100 | 31.7% | 5 | 1.6% | current 5 |

### `subterrestrial:cabin/ice/bottom`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `subterrestrial:cabin/taiga/bottom_0` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/taiga/bottom_0 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/taiga/bottom_1` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/taiga/bottom_1 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/taiga/bottom_2` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/taiga/bottom_2 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/taiga/bottom_3` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/taiga/bottom_3 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |

### `subterrestrial:cabin/ice/top`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `subterrestrial:cabin/taiga/top_0` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/taiga/top_0 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/taiga/top_1` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/taiga/top_1 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/taiga/top_2` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/taiga/top_2 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/taiga/top_3` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/taiga/top_3 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |

### `subterrestrial:cabin/jungle/base`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `subterrestrial:cabin/jungle/base_0` | 5 | 1.6% | 100 | 31.7% | current 100 |
| `subterrestrial:cabin/jungle/base_0 #2` | 100 | 31.7% | 5 | 1.6% | current 5 |
| `subterrestrial:cabin/jungle/base_lower_0` | 5 | 1.6% | 100 | 31.7% | current 100 |
| `subterrestrial:cabin/jungle/base_lower_0 #2` | 100 | 31.7% | 5 | 1.6% | current 5 |
| `subterrestrial:cabin/jungle/base_upper_0` | 5 | 1.6% | 100 | 31.7% | current 100 |
| `subterrestrial:cabin/jungle/base_upper_0 #2` | 100 | 31.7% | 5 | 1.6% | current 5 |

### `subterrestrial:cabin/jungle/bottom`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `subterrestrial:cabin/jungle/bottom_0` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/jungle/bottom_0 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/jungle/bottom_1` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/jungle/bottom_1 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/jungle/bottom_2` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/jungle/bottom_2 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/jungle/bottom_3` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/jungle/bottom_3 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |

### `subterrestrial:cabin/jungle/top`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `subterrestrial:cabin/jungle/top_0` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/jungle/top_0 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/jungle/top_1` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/jungle/top_1 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/jungle/top_2` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/jungle/top_2 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/jungle/top_3` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/jungle/top_3 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |

### `subterrestrial:cabin/mesa/base`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `subterrestrial:cabin/mesa/base_0` | 5 | 1.6% | 100 | 31.7% | current 100 |
| `subterrestrial:cabin/mesa/base_0 #2` | 100 | 31.7% | 5 | 1.6% | current 5 |
| `subterrestrial:cabin/mesa/base_lower_0` | 5 | 1.6% | 100 | 31.7% | current 100 |
| `subterrestrial:cabin/mesa/base_lower_0 #2` | 100 | 31.7% | 5 | 1.6% | current 5 |
| `subterrestrial:cabin/mesa/base_upper_0` | 5 | 1.6% | 100 | 31.7% | current 100 |
| `subterrestrial:cabin/mesa/base_upper_0 #2` | 100 | 31.7% | 5 | 1.6% | current 5 |

### `subterrestrial:cabin/mesa/bottom`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `subterrestrial:cabin/desert/bottom_0` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/desert/bottom_0 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/desert/bottom_1` | 100 | 23.8% | 100 | 23.8% |  |
| `subterrestrial:cabin/desert/bottom_2` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/desert/bottom_2 #2` | 5 | 1.2% | 5 | 1.2% |  |
| `subterrestrial:cabin/desert/bottom_2 #3` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/desert/bottom_3` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/desert/bottom_3 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |

### `subterrestrial:cabin/mesa/top`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `subterrestrial:cabin/desert/top_0` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/desert/top_0 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/desert/top_1` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/desert/top_1 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/desert/top_2` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/desert/top_2 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/desert/top_3` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/desert/top_3 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |

### `subterrestrial:cabin/taiga/base`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `subterrestrial:cabin/taiga/base_0` | 5 | 1.6% | 100 | 31.7% | current 100 |
| `subterrestrial:cabin/taiga/base_0 #2` | 100 | 31.7% | 5 | 1.6% | current 5 |
| `subterrestrial:cabin/taiga/base_lower_0` | 5 | 1.6% | 100 | 31.7% | current 100 |
| `subterrestrial:cabin/taiga/base_lower_0 #2` | 100 | 31.7% | 5 | 1.6% | current 5 |
| `subterrestrial:cabin/taiga/base_upper_0` | 5 | 1.6% | 100 | 31.7% | current 100 |
| `subterrestrial:cabin/taiga/base_upper_0 #2` | 100 | 31.7% | 5 | 1.6% | current 5 |

### `subterrestrial:cabin/taiga/bottom`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `subterrestrial:cabin/taiga/bottom_0` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/taiga/bottom_0 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/taiga/bottom_1` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/taiga/bottom_1 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/taiga/bottom_2` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/taiga/bottom_2 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/taiga/bottom_3` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/taiga/bottom_3 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |

### `subterrestrial:cabin/taiga/top`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `subterrestrial:cabin/taiga/top_0` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/taiga/top_0 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/taiga/top_1` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/taiga/top_1 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/taiga/top_2` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/taiga/top_2 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |
| `subterrestrial:cabin/taiga/top_3` | 5 | 1.2% | 100 | 23.8% | current 100 |
| `subterrestrial:cabin/taiga/top_3 #2` | 100 | 23.8% | 5 | 1.2% | current 5 |

### `unwrecked_ships:ship/pirate_sails`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #2` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #3` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #4` | 1 | 0.7% | 30 | 20.8% | current 30 |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #5` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #6` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #7` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #8` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #9` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #10` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #11` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #12` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #13` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #14` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #15` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #16` | 30 | 20.8% | 1 | 0.7% | current 1 |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #2` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #3` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #4` | 1 | 0.7% | 30 | 20.8% | current 30 |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #5` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #6` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #7` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #8` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #9` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #10` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #11` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #12` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #13` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #14` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #15` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #16` | 30 | 20.8% | 1 | 0.7% | current 1 |
| `unwrecked_ships:ship/sails/white_sails` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/white_sails #2` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/white_sails #3` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/white_sails #4` | 1 | 0.7% | 30 | 20.8% | current 30 |
| `unwrecked_ships:ship/sails/white_sails #5` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/white_sails #6` | 1 | 0.7% | 10 | 6.9% | current 10 |
| `unwrecked_ships:ship/sails/white_sails #7` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/white_sails #8` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/white_sails #9` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/white_sails #10` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/white_sails #11` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/white_sails #12` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/white_sails #13` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/white_sails #14` | 1 | 0.7% | 1 | 0.7% |  |
| `unwrecked_ships:ship/sails/white_sails #15` | 10 | 6.9% | 1 | 0.7% | current 1 |
| `unwrecked_ships:ship/sails/white_sails #16` | 30 | 20.8% | 1 | 0.7% | current 1 |

### `unwrecked_ships:ship/sails`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #2` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #3` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #4` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #5` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #6` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #7` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #8` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #9` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #10` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #11` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #12` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #13` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #14` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #15` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/horizontal_white_sails_primary #16` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/nether/crimson_sails` | 4 | 2.1% | 4 | 2.1% |  |
| `unwrecked_ships:ship/sails/nether/horizontal_crimson_sails_primary` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/nether/horizontal_warped_sails_primary` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/nether/vertical_crimson_sails_primary` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/nether/vertical_warped_sails_primary` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/nether/warped_sails` | 4 | 2.1% | 4 | 2.1% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #2` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #3` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #4` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #5` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #6` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #7` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #8` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #9` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #10` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #11` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #12` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #13` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #14` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #15` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/vertical_white_sails_primary #16` | 1 | 0.5% | 1 | 0.5% |  |
| `unwrecked_ships:ship/sails/white_sails` | 3 | 1.6% | 40 | 20.8% | current 40 |
| `unwrecked_ships:ship/sails/white_sails #2` | 3 | 1.6% | 40 | 20.8% | current 40 |
| `unwrecked_ships:ship/sails/white_sails #3` | 3 | 1.6% | 5 | 2.6% | current 5 |
| `unwrecked_ships:ship/sails/white_sails #4` | 3 | 1.6% | 3 | 1.6% |  |
| `unwrecked_ships:ship/sails/white_sails #5` | 3 | 1.6% | 20 | 10.4% | current 20 |
| `unwrecked_ships:ship/sails/white_sails #6` | 3 | 1.6% | 3 | 1.6% |  |
| `unwrecked_ships:ship/sails/white_sails #7` | 3 | 1.6% | 3 | 1.6% |  |
| `unwrecked_ships:ship/sails/white_sails #8` | 3 | 1.6% | 10 | 5.2% | current 10 |
| `unwrecked_ships:ship/sails/white_sails #9` | 3 | 1.6% | 3 | 1.6% |  |
| `unwrecked_ships:ship/sails/white_sails #10` | 3 | 1.6% | 3 | 1.6% |  |
| `unwrecked_ships:ship/sails/white_sails #11` | 3 | 1.6% | 3 | 1.6% |  |
| `unwrecked_ships:ship/sails/white_sails #12` | 5 | 2.6% | 3 | 1.6% | current 3 |
| `unwrecked_ships:ship/sails/white_sails #13` | 10 | 5.2% | 3 | 1.6% | current 3 |
| `unwrecked_ships:ship/sails/white_sails #14` | 20 | 10.4% | 3 | 1.6% | current 3 |
| `unwrecked_ships:ship/sails/white_sails #15` | 40 | 20.8% | 3 | 1.6% | current 3 |
| `unwrecked_ships:ship/sails/white_sails #16` | 40 | 20.8% | 3 | 1.6% | current 3 |

### `villagesandpillages:village_witch/road_and_house`

| piece | original | chance | final | chance | why |
|---|---:|---:|---:|---:|---|
| `villagesandpillages:village_witch/house/medium/medium_house_01` | 1 | 1.7% | 3 | 5.0% | current 3 |
| `villagesandpillages:village_witch/house/medium/medium_house_01 #2` | 1 | 1.7% | 1 | 1.7% |  |
| `villagesandpillages:village_witch/house/medium/medium_house_01 #3` | 1 | 1.7% | 1 | 1.7% |  |
| `villagesandpillages:village_witch/house/medium/medium_house_01 #4` | 2 | 3.3% | 2 | 3.3% |  |
| `villagesandpillages:village_witch/house/medium/medium_house_01 #5` | 3 | 5.0% | 1 | 1.7% | current 1 |
| `villagesandpillages:village_witch/house/medium/medium_house_02` | 1 | 1.7% | 3 | 5.0% | current 3 |
| `villagesandpillages:village_witch/house/medium/medium_house_02 #2` | 1 | 1.7% | 1 | 1.7% |  |
| `villagesandpillages:village_witch/house/medium/medium_house_02 #3` | 1 | 1.7% | 1 | 1.7% |  |
| `villagesandpillages:village_witch/house/medium/medium_house_02 #4` | 2 | 3.3% | 2 | 3.3% |  |
| `villagesandpillages:village_witch/house/medium/medium_house_02 #5` | 3 | 5.0% | 1 | 1.7% | current 1 |
| `villagesandpillages:village_witch/house/small/small_house_01` | 1 | 1.7% | 1 | 1.7% |  |
| `villagesandpillages:village_witch/house/small/small_house_01 #2` | 1 | 1.7% | 1 | 1.7% |  |
| `villagesandpillages:village_witch/house/small/small_house_01 #3` | 1 | 1.7% | 2 | 3.3% | current 2 |
| `villagesandpillages:village_witch/house/small/small_house_01 #4` | 2 | 3.3% | 1 | 1.7% | current 1 |
| `villagesandpillages:village_witch/house/small/small_house_01 #5` | 2 | 3.3% | 2 | 3.3% |  |
| `villagesandpillages:village_witch/house/small/small_house_02` | 1 | 1.7% | 1 | 1.7% |  |
| `villagesandpillages:village_witch/house/small/small_house_02 #2` | 1 | 1.7% | 2 | 3.3% | current 2 |
| `villagesandpillages:village_witch/house/small/small_house_02 #3` | 1 | 1.7% | 1 | 1.7% |  |
| `villagesandpillages:village_witch/house/small/small_house_02 #4` | 1 | 1.7% | 1 | 1.7% |  |
| `villagesandpillages:village_witch/house/small/small_house_02 #5` | 1 | 1.7% | 1 | 1.7% |  |
| `villagesandpillages:village_witch/house/small/small_house_02 #6` | 2 | 3.3% | 1 | 1.7% | current 1 |
| `villagesandpillages:village_witch/house/small/small_house_03` | 1 | 1.7% | 1 | 1.7% |  |
| `villagesandpillages:village_witch/house/small/small_house_03 #2` | 1 | 1.7% | 1 | 1.7% |  |
| `villagesandpillages:village_witch/house/small/small_house_03 #3` | 2 | 3.3% | 2 | 3.3% |  |
| `villagesandpillages:village_witch/house/small/small_house_03 #4` | 2 | 3.3% | 2 | 3.3% |  |
| `villagesandpillages:village_witch/road/crossroad/crossroad_four_way` | 2 | 3.3% | 2 | 3.3% |  |
| `villagesandpillages:village_witch/road/crossroad/crossroad_one_way` | 1 | 1.7% | 1 | 1.7% |  |
| `villagesandpillages:village_witch/road/crossroad/crossroad_one_way_broken` | 1 | 1.7% | 1 | 1.7% |  |
| `villagesandpillages:village_witch/road/crossroad/crossroad_one_way_broken #2` | 2 | 3.3% | 2 | 3.3% |  |
| `villagesandpillages:village_witch/road/crossroad/crossroad_one_way_damaged` | 1 | 1.7% | 1 | 1.7% |  |
| `villagesandpillages:village_witch/road/crossroad/crossroad_one_way_damaged #2` | 2 | 3.3% | 2 | 3.3% |  |
| `villagesandpillages:village_witch/road/crossroad/crossroad_three_way` | 1 | 1.7% | 1 | 1.7% |  |
| `villagesandpillages:village_witch/road/crossroad/crossroad_three_way #2` | 1 | 1.7% | 1 | 1.7% |  |
| `villagesandpillages:village_witch/road/crossroad/crossroad_three_way #3` | 2 | 3.3% | 2 | 3.3% |  |
| `villagesandpillages:village_witch/road/crossroad/crossroad_two_way` | 1 | 1.7% | 1 | 1.7% |  |
| `villagesandpillages:village_witch/road/crossroad/crossroad_two_way #2` | 2 | 3.3% | 2 | 3.3% |  |
| `villagesandpillages:village_witch/road/road_long` | 1 | 1.7% | 1 | 1.7% |  |
| `villagesandpillages:village_witch/road/road_long #2` | 1 | 1.7% | 1 | 1.7% |  |
| `villagesandpillages:village_witch/road/road_medium` | 1 | 1.7% | 1 | 1.7% |  |
| `villagesandpillages:village_witch/road/road_medium #2` | 2 | 3.3% | 2 | 3.3% |  |
| `villagesandpillages:village_witch/road/road_short` | 3 | 5.0% | 3 | 5.0% |  |

## Pieces injected by other mods

Pieces whose namespace differs from their pool's, per pool. This is where mod village houses show up.

### beautify

- `minecraft:village/desert/streets` (pool total weight 53): `beautify:botanist_house_desert` 1 -> 2
- `minecraft:village/plains/streets` (pool total weight 65): `beautify:botanist_house_plains` 1 -> 2
- `minecraft:village/savanna/streets` (pool total weight 72): `beautify:botanist_house_savanna` 1 -> 2
- `minecraft:village/snowy/streets` (pool total weight 65): `beautify:botanist_house_snowy` 1 -> 2
- `minecraft:village/taiga/streets` (pool total weight 65): `beautify:botanist_house_taiga` 1 -> 2

### chefsdelight

- `minecraft:village/desert/houses` (pool total weight 222): `chefsdelight:desert_chef_house` 5 -> 15, `chefsdelight:desert_cook_house` 3 -> 9
- `minecraft:village/plains/houses` (pool total weight 246): `chefsdelight:plains_chef_house` 5 -> 15, `chefsdelight:plains_cook_house` 2 -> 6
- `minecraft:village/savanna/houses` (pool total weight 242): `chefsdelight:savanna_chef_house` 5 -> 15, `chefsdelight:savanna_cook_house` 3 -> 9
- `minecraft:village/snowy/houses` (pool total weight 217): `chefsdelight:snowy_chef_house` 4 -> 12, `chefsdelight:snowy_cook_house` 3 -> 9
- `minecraft:village/taiga/houses` (pool total weight 236): `chefsdelight:taiga_chef_house` 4 -> 12, `chefsdelight:taiga_cook_house` 3 -> 9

### cloudstorage

- `minecraft:village/desert/houses` (pool total weight 222): `cloudstorage:balloon_stand_desert` 2 -> 6
- `minecraft:village/plains/houses` (pool total weight 246): `cloudstorage:balloon_stand_plains` 2 -> 6
- `minecraft:village/savanna/houses` (pool total weight 242): `cloudstorage:balloon_stand_savanna` 2 -> 6
- `minecraft:village/snowy/houses` (pool total weight 217): `cloudstorage:balloon_stand_snowy` 2 -> 6
- `minecraft:village/taiga/houses` (pool total weight 236): `cloudstorage:balloon_stand_taiga` 2 -> 6

### domesticationinnovation

- `minecraft:village/desert/houses` (pool total weight 222): `domesticationinnovation:desert_petshop` 17 -> 51
- `minecraft:village/plains/houses` (pool total weight 246): `domesticationinnovation:plains_petshop` 17 -> 51
- `minecraft:village/savanna/houses` (pool total weight 242): `domesticationinnovation:savanna_petshop` 17 -> 51
- `minecraft:village/snowy/houses` (pool total weight 217): `domesticationinnovation:snowy_petshop` 17 -> 51
- `minecraft:village/taiga/houses` (pool total weight 236): `domesticationinnovation:taiga_petshop` 17 -> 51

### dynamicvillage

- `minecraft:village/desert/houses` (pool total weight 222): `dynamicvillage:desert/desert_mech` 20 -> 0, `dynamicvillage:desert/desert_miner` 20 -> 0, `dynamicvillage:desert/desert_plumber` 20 -> 0, `dynamicvillage:desert/desert_train` 20 -> 0
- `minecraft:village/plains/houses` (pool total weight 246): `dynamicvillage:plains/plains_mech` 20 -> 0, `dynamicvillage:plains/plains_miner` 20 -> 0, `dynamicvillage:plains/plains_plumber` 20 -> 0, `dynamicvillage:plains/plains_train` 20 -> 0
- `minecraft:village/savanna/houses` (pool total weight 242): `dynamicvillage:savanna/savanna_mech` 20 -> 0, `dynamicvillage:savanna/savanna_miner` 20 -> 0, `dynamicvillage:savanna/savanna_plumber` 20 -> 0, `dynamicvillage:savanna/savanna_train` 20 -> 0
- `minecraft:village/snowy/houses` (pool total weight 217): `dynamicvillage:snowy/snowy_mech` 20 -> 0, `dynamicvillage:snowy/snowy_miner` 20 -> 0, `dynamicvillage:snowy/snowy_plumber` 20 -> 0, `dynamicvillage:snowy/snowy_train` 20 -> 0
- `minecraft:village/taiga/houses` (pool total weight 236): `dynamicvillage:taiga/taiga_mech` 20 -> 0, `dynamicvillage:taiga/taiga_miner` 20 -> 0, `dynamicvillage:taiga/taiga_plumber` 20 -> 0, `dynamicvillage:taiga/taiga_train` 20 -> 0

### etched

- `minecraft:village/desert/houses` (pool total weight 222): `etched:village/desert/houses/desert_bard_house_1` 2 -> 6
- `minecraft:village/desert/zombie/houses` (pool total weight 70): `etched:village/desert/houses/desert_bard_house_1` 2
- `minecraft:village/plains/houses` (pool total weight 246): `etched:village/plains/houses/plains_bard_house_1` 2 -> 6
- `minecraft:village/plains/zombie/houses` (pool total weight 85): `etched:village/plains/houses/plains_bard_house_1` 2
- `minecraft:village/savanna/houses` (pool total weight 242): `etched:village/savanna/houses/savanna_bard_house_1` 4 -> 12
- `minecraft:village/savanna/zombie/houses` (pool total weight 76): `etched:village/savanna/houses/savanna_bard_house_1` 4
- `minecraft:village/snowy/houses` (pool total weight 217): `etched:village/snowy/houses/snowy_bard_house_1` 4 -> 12
- `minecraft:village/snowy/zombie/houses` (pool total weight 69): `etched:village/snowy/houses/snowy_bard_house_1` 4
- `minecraft:village/taiga/houses` (pool total weight 236): `etched:village/taiga/houses/taiga_bard_house_1` 4 -> 12
- `minecraft:village/taiga/zombie/houses` (pool total weight 78): `etched:village/taiga/houses/taiga_bard_house_1` 4

### farmersdelight

- `minecraft:village/desert/houses` (pool total weight 222): `farmersdelight:village/houses/desert_compost_pile` 3 -> 9
- `minecraft:village/plains/houses` (pool total weight 246): `farmersdelight:village/houses/plains_compost_pile` 5 -> 15
- `minecraft:village/savanna/houses` (pool total weight 242): `farmersdelight:village/houses/savanna_compost_pile` 4 -> 12
- `minecraft:village/snowy/houses` (pool total weight 217): `farmersdelight:village/houses/snowy_compost_pile` 3 -> 9
- `minecraft:village/taiga/houses` (pool total weight 236): `farmersdelight:village/houses/taiga_compost_pile` 4 -> 12

### immersiveengineering

- `minecraft:village/desert/houses` (pool total weight 222): `immersiveengineering:village/houses/desert_electrician` 1 -> 0, `immersiveengineering:village/houses/desert_engineer` 1 -> 0, `immersiveengineering:village/houses/desert_gunsmith` 1 -> 0, `immersiveengineering:village/houses/desert_machinist` 1 -> 0, `immersiveengineering:village/houses/desert_outfitter` 1 -> 0
- `minecraft:village/plains/houses` (pool total weight 246): `immersiveengineering:village/houses/plains_electrician` 1 -> 0, `immersiveengineering:village/houses/plains_engineer` 1 -> 0, `immersiveengineering:village/houses/plains_gunsmith` 1 -> 0, `immersiveengineering:village/houses/plains_machinist` 1 -> 0, `immersiveengineering:village/houses/plains_outfitter` 1 -> 0
- `minecraft:village/savanna/houses` (pool total weight 242): `immersiveengineering:village/houses/savanna_electrician` 1 -> 0, `immersiveengineering:village/houses/savanna_engineer` 1 -> 0, `immersiveengineering:village/houses/savanna_gunsmith` 1 -> 0, `immersiveengineering:village/houses/savanna_machinist` 1 -> 0, `immersiveengineering:village/houses/savanna_outfitter` 1 -> 0
- `minecraft:village/snowy/houses` (pool total weight 217): `immersiveengineering:village/houses/snowy_electrician` 1 -> 0, `immersiveengineering:village/houses/snowy_engineer` 1 -> 0, `immersiveengineering:village/houses/snowy_gunsmith` 1 -> 0, `immersiveengineering:village/houses/snowy_machinist` 1 -> 0, `immersiveengineering:village/houses/snowy_outfitter` 1 -> 0
- `minecraft:village/taiga/houses` (pool total weight 236): `immersiveengineering:village/houses/taiga_electrician` 1 -> 0, `immersiveengineering:village/houses/taiga_engineer` 1 -> 0, `immersiveengineering:village/houses/taiga_gunsmith` 1 -> 0, `immersiveengineering:village/houses/taiga_machinist` 1 -> 0, `immersiveengineering:village/houses/taiga_outfitter` 1 -> 0

### incubation

- `minecraft:village/desert/houses` (pool total weight 222): `incubation:village/desert_chicken_coop_1` 2 -> 6
- `minecraft:village/plains/houses` (pool total weight 246): `incubation:village/plains_chicken_coop_1` 2 -> 6
- `minecraft:village/savanna/houses` (pool total weight 242): `incubation:village/savanna_chicken_coop_1` 2 -> 6
- `minecraft:village/snowy/houses` (pool total weight 217): `incubation:village/snowy_chicken_coop_1` 2 -> 6
- `minecraft:village/taiga/houses` (pool total weight 236): `incubation:village/taiga_chicken_coop_1` 2 -> 6

### ipo

- `minecraft:pillager_outpost/base_plates` (pool total weight 8): `ipo:pillager_outpost/base_plate` 1

### minecraft

- `eugene:iron_golem` (pool total weight 1): `minecraft:village/common/iron_golem` 1
- `kaisyn:ships/village_ocean/base_plate` (pool total weight 3): `minecraft:empty` 2
- `kaisyn:village/badlands_pueblo/decor` (pool total weight 13): `minecraft:empty` 1
- `kaisyn:village/birch_forest_romanian/decor` (pool total weight 13): `minecraft:empty` 7
- `kaisyn:village/exclusives/iberian/decor` (pool total weight 5): `minecraft:empty` 3
- `kaisyn:village/exclusives/iberian/houses` (pool total weight 35): `minecraft:empty` 4
- `kaisyn:village/exclusives/rustic/houses` (pool total weight 19): `minecraft:empty` 6
- `kaisyn:village/exclusives/swedish/houses` (pool total weight 25): `minecraft:empty` 5
- `kaisyn:village/exclusives/tudor/decor` (pool total weight 5): `minecraft:empty` 3
- `kaisyn:village/exclusives/tudor/houses` (pool total weight 26): `minecraft:empty` 3
- `kaisyn:village/flower_forest_japanese/decor` (pool total weight 7): `minecraft:empty` 5
- `kaisyn:village/forest_ruins/houses` (pool total weight 66): `minecraft:empty` 3
- `kaisyn:village/jungle_tribal/decor` (pool total weight 17): `feature:minecraft:pile_hay` 1, `feature:minecraft:pile_melon` 1, `minecraft:empty` 7
- `kaisyn:village/meadow_swiss/decor` (pool total weight 3): `minecraft:empty` 2
- `kaisyn:village/old_growth_taiga_polish/decor` (pool total weight 8): `feature:minecraft:pile_hay` 1, `minecraft:empty` 5
- `kaisyn:village/old_growth_taiga_polish/houses` (pool total weight 37): `minecraft:empty` 3
- `kaisyn:village/snowy_taiga_viking/decor` (pool total weight 14): `minecraft:empty` 10
- `kaisyn:village/sparse_jungle_polynesian/decor` (pool total weight 6): `minecraft:empty` 5
- `kaisyn:village/sparse_jungle_polynesian/houses` (pool total weight 70): `minecraft:empty` 10
- `revamped_shipwrecks:shipwreck/dark_oak_jungle/alternatives/backend_spacer` (pool total weight 24): `minecraft:empty` 12
- `revamped_shipwrecks:shipwreck/dark_oak_jungle/alternatives/front_spacer` (pool total weight 24): `minecraft:empty` 12
- `revamped_shipwrecks:shipwreck/dark_oak_jungle/alternatives/loose_debris` (pool total weight 60): `minecraft:empty` 37
- `revamped_shipwrecks:shipwreck/dark_oak_jungle/alternatives/scattered_loose_debris_spacer` (pool total weight 15): `minecraft:empty` 10
- `revamped_shipwrecks:shipwreck/dark_oak_spruce/alternatives/backend_spacer` (pool total weight 24): `minecraft:empty` 12
- `revamped_shipwrecks:shipwreck/dark_oak_spruce/alternatives/front_spacer` (pool total weight 24): `minecraft:empty` 12
- `revamped_shipwrecks:shipwreck/dark_oak_spruce/alternatives/loose_debris` (pool total weight 60): `minecraft:empty` 37
- `revamped_shipwrecks:shipwreck/dark_oak_spruce/alternatives/scattered_loose_debris_spacer` (pool total weight 15): `minecraft:empty` 10
- `revamped_shipwrecks:shipwreck/jungle_spruce/alternatives/backend_spacer` (pool total weight 24): `minecraft:empty` 12
- `revamped_shipwrecks:shipwreck/jungle_spruce/alternatives/front_spacer` (pool total weight 24): `minecraft:empty` 12
- `revamped_shipwrecks:shipwreck/jungle_spruce/alternatives/loose_debris` (pool total weight 60): `minecraft:empty` 37
- `revamped_shipwrecks:shipwreck/jungle_spruce/alternatives/scattered_loose_debris_spacer` (pool total weight 15): `minecraft:empty` 10
- `revamped_shipwrecks:shipwreck/nests` (pool total weight 18): `minecraft:empty` 3
- `revamped_shipwrecks:shipwreck/oak_birch/alternatives/backend_spacer` (pool total weight 24): `minecraft:empty` 12
- `revamped_shipwrecks:shipwreck/oak_birch/alternatives/front_spacer` (pool total weight 24): `minecraft:empty` 12
- `revamped_shipwrecks:shipwreck/oak_birch/alternatives/loose_debris` (pool total weight 60): `minecraft:empty` 37
- `revamped_shipwrecks:shipwreck/oak_birch/alternatives/scattered_loose_debris_spacer` (pool total weight 15): `minecraft:empty` 10
- `revamped_shipwrecks:shipwreck/oak_spruce/alternatives/backend_spacer` (pool total weight 24): `minecraft:empty` 12
- `revamped_shipwrecks:shipwreck/oak_spruce/alternatives/front_spacer` (pool total weight 24): `minecraft:empty` 12
- `revamped_shipwrecks:shipwreck/oak_spruce/alternatives/loose_debris` (pool total weight 60): `minecraft:empty` 37
- `revamped_shipwrecks:shipwreck/oak_spruce/alternatives/scattered_loose_debris_spacer` (pool total weight 15): `minecraft:empty` 10
- `revamped_shipwrecks:shipwreck/sails` (pool total weight 7): `minecraft:empty` 1
- `revamped_shipwrecks:shipwreck/spruce_dark_oak/alternatives/backend_spacer` (pool total weight 24): `minecraft:empty` 12
- `revamped_shipwrecks:shipwreck/spruce_dark_oak/alternatives/front_spacer` (pool total weight 24): `minecraft:empty` 12
- `revamped_shipwrecks:shipwreck/spruce_dark_oak/alternatives/loose_debris` (pool total weight 60): `minecraft:empty` 37
- `revamped_shipwrecks:shipwreck/spruce_dark_oak/alternatives/scattered_loose_debris_spacer` (pool total weight 15): `minecraft:empty` 10
- `revamped_shipwrecks:shipwreck/spruce_jungle/alternatives/backend_spacer` (pool total weight 24): `minecraft:empty` 12
- `revamped_shipwrecks:shipwreck/spruce_jungle/alternatives/front_spacer` (pool total weight 24): `minecraft:empty` 12
- `revamped_shipwrecks:shipwreck/spruce_jungle/alternatives/loose_debris` (pool total weight 60): `minecraft:empty` 37
- `revamped_shipwrecks:shipwreck/spruce_jungle/alternatives/scattered_loose_debris_spacer` (pool total weight 15): `minecraft:empty` 10
- `revamped_shipwrecks:shipwreck/spruce_oak/alternatives/backend_spacer` (pool total weight 24): `minecraft:empty` 12
- `revamped_shipwrecks:shipwreck/spruce_oak/alternatives/front_spacer` (pool total weight 24): `minecraft:empty` 12
- `revamped_shipwrecks:shipwreck/spruce_oak/alternatives/loose_debris` (pool total weight 60): `minecraft:empty` 37
- `revamped_shipwrecks:shipwreck/spruce_oak/alternatives/scattered_loose_debris_spacer` (pool total weight 15): `minecraft:empty` 10
- `skyvillages:skyvillage_deco` (pool total weight 5): `minecraft:empty` 5
- `terralith:village/baby_villager` (pool total weight 1): `minecraft:village/desert/villagers/baby` 1
- `terralith:village/desert/baby_villager` (pool total weight 1): `minecraft:village/desert/villagers/baby` 1
- `terralith:village/desert/houses` (pool total weight 80): `minecraft:empty` 40
- `terralith:village/desert/houses_extra` (pool total weight 81): `minecraft:empty` 40
- `terralith:village/fortified/starts` (pool total weight 4): `minecraft:empty` 1
- `unwrecked_ships:sailors/common_villager` (pool total weight 2): `minecraft:empty` 1
- `unwrecked_ships:sailors/nether/common_piglin` (pool total weight 2): `minecraft:empty` 1
- `unwrecked_ships:sailors/nether/common_zombie_pigman` (pool total weight 2): `minecraft:empty` 1
- `villagesandpillages:village_witch/decoration` (pool total weight 145): `minecraft:empty` 100

### monobank

- `minecraft:village/desert/houses` (pool total weight 222): `monobank:village/houses/desert_vault` 15 -> 8
- `minecraft:village/desert/streets` (pool total weight 53): `monobank:village/streets/desert_straight_fix_01` 15 -> 5
- `minecraft:village/plains/houses` (pool total weight 246): `monobank:village/houses/plains_vault` 15 -> 8
- `minecraft:village/plains/streets` (pool total weight 65): `monobank:village/streets/plains_straight_fix_01` 15 -> 5
- `minecraft:village/savanna/houses` (pool total weight 242): `monobank:village/houses/savanna_vault` 15 -> 8
- `minecraft:village/savanna/streets` (pool total weight 72): `monobank:village/streets/savanna_straight_fix_01` 15 -> 5
- `minecraft:village/snowy/houses` (pool total weight 217): `monobank:village/houses/snowy_vault` 15 -> 8
- `minecraft:village/snowy/streets` (pool total weight 65): `monobank:village/streets/snowy_straight_fix_01` 15 -> 5
- `minecraft:village/taiga/houses` (pool total weight 236): `monobank:village/houses/taiga_vault` 15 -> 8
- `minecraft:village/taiga/streets` (pool total weight 65): `monobank:village/streets/taiga_straight_fix_01` 15 -> 5

### pneumaticcraft

- `minecraft:village/desert/streets` (pool total weight 53): `pneumaticcraft:villages/custom_street_desert` 2 -> 0
- `minecraft:village/plains/houses` (pool total weight 246): `pneumaticcraft:villages/mechanic_house_plains` 8 -> 0
- `minecraft:village/savanna/houses` (pool total weight 242): `pneumaticcraft:villages/mechanic_house_savanna` 8 -> 0
- `minecraft:village/snowy/streets` (pool total weight 65): `pneumaticcraft:villages/custom_street_snowy` 2 -> 0
- `minecraft:village/taiga/houses` (pool total weight 236): `pneumaticcraft:villages/mechanic_house_taiga` 8 -> 0

### veggiesdelight

- `minecraft:village/desert/houses` (pool total weight 222): `veggiesdelight:village/houses/desert_depot_house` 4 -> 6
- `minecraft:village/plains/houses` (pool total weight 246): `veggiesdelight:village/houses/plains_depot_house` 4 -> 6
- `minecraft:village/savanna/houses` (pool total weight 242): `veggiesdelight:village/houses/savanna_depot_house` 4 -> 6
- `minecraft:village/snowy/houses` (pool total weight 217): `veggiesdelight:village/houses/snowy_depot_house` 4 -> 6
- `minecraft:village/taiga/houses` (pool total weight 236): `veggiesdelight:village/houses/taiga_depot_house` 4 -> 6

### vinery

- `minecraft:village/desert/houses` (pool total weight 222): `vinery:vinery_desert` 2 -> 9
- `minecraft:village/plains/houses` (pool total weight 246): `vinery:vinery_plains` 2 -> 9
- `minecraft:village/savanna/houses` (pool total weight 242): `vinery:vinery_savanna` 2 -> 9
- `minecraft:village/taiga/houses` (pool total weight 236): `vinery:vinery_taiga` 2 -> 11

### wares

- `minecraft:village/desert/houses` (pool total weight 222): `wares:village/houses/desert_warehouse` 10 -> 30
- `minecraft:village/plains/houses` (pool total weight 246): `wares:village/houses/plains_warehouse` 10 -> 30
- `minecraft:village/savanna/houses` (pool total weight 242): `wares:village/houses/savanna_warehouse` 10 -> 30
- `minecraft:village/snowy/houses` (pool total weight 217): `wares:village/houses/snowy_warehouse` 10 -> 30
- `minecraft:village/taiga/houses` (pool total weight 236): `wares:village/houses/taiga_warehouse` 10 -> 30

## Tags

### mod/immersiveengineering x0 (25 pieces)

`immersiveengineering:village/houses/desert_electrician`, `immersiveengineering:village/houses/desert_engineer`, `immersiveengineering:village/houses/desert_gunsmith`, `immersiveengineering:village/houses/desert_machinist`, `immersiveengineering:village/houses/desert_outfitter`, `immersiveengineering:village/houses/plains_electrician`, `immersiveengineering:village/houses/plains_engineer`, `immersiveengineering:village/houses/plains_gunsmith`, `immersiveengineering:village/houses/plains_machinist`, `immersiveengineering:village/houses/plains_outfitter`, `immersiveengineering:village/houses/savanna_electrician`, `immersiveengineering:village/houses/savanna_engineer`, `immersiveengineering:village/houses/savanna_gunsmith`, `immersiveengineering:village/houses/savanna_machinist`, `immersiveengineering:village/houses/savanna_outfitter`, `immersiveengineering:village/houses/snowy_electrician`, `immersiveengineering:village/houses/snowy_engineer`, `immersiveengineering:village/houses/snowy_gunsmith`, `immersiveengineering:village/houses/snowy_machinist`, `immersiveengineering:village/houses/snowy_outfitter`, `immersiveengineering:village/houses/taiga_electrician`, `immersiveengineering:village/houses/taiga_engineer`, `immersiveengineering:village/houses/taiga_gunsmith`, `immersiveengineering:village/houses/taiga_machinist`, `immersiveengineering:village/houses/taiga_outfitter`

### mod/dynamicvillage x0 (20 pieces)

`dynamicvillage:desert/desert_mech`, `dynamicvillage:desert/desert_miner`, `dynamicvillage:desert/desert_plumber`, `dynamicvillage:desert/desert_train`, `dynamicvillage:plains/plains_mech`, `dynamicvillage:plains/plains_miner`, `dynamicvillage:plains/plains_plumber`, `dynamicvillage:plains/plains_train`, `dynamicvillage:savanna/savanna_mech`, `dynamicvillage:savanna/savanna_miner`, `dynamicvillage:savanna/savanna_plumber`, `dynamicvillage:savanna/savanna_train`, `dynamicvillage:snowy/snowy_mech`, `dynamicvillage:snowy/snowy_miner`, `dynamicvillage:snowy/snowy_plumber`, `dynamicvillage:snowy/snowy_train`, `dynamicvillage:taiga/taiga_mech`, `dynamicvillage:taiga/taiga_miner`, `dynamicvillage:taiga/taiga_plumber`, `dynamicvillage:taiga/taiga_train`

### mod/pneumaticcraft x0 (7 pieces)

`pneumaticcraft:villages/custom_street_desert`, `pneumaticcraft:villages/custom_street_snowy`, `pneumaticcraft:villages/mechanic_house_desert`, `pneumaticcraft:villages/mechanic_house_plains`, `pneumaticcraft:villages/mechanic_house_savanna`, `pneumaticcraft:villages/mechanic_house_snowy`, `pneumaticcraft:villages/mechanic_house_taiga`

### mod/advancedperipherals x0 (0 pieces)


### tech x0 (52 pieces)

`immersiveengineering:village/houses/desert_electrician`, `immersiveengineering:village/houses/desert_engineer`, `immersiveengineering:village/houses/desert_gunsmith`, `immersiveengineering:village/houses/desert_machinist`, `immersiveengineering:village/houses/desert_outfitter`, `immersiveengineering:village/houses/plains_electrician`, `immersiveengineering:village/houses/plains_engineer`, `immersiveengineering:village/houses/plains_gunsmith`, `immersiveengineering:village/houses/plains_machinist`, `immersiveengineering:village/houses/plains_outfitter`, `immersiveengineering:village/houses/savanna_electrician`, `immersiveengineering:village/houses/savanna_engineer`, `immersiveengineering:village/houses/savanna_gunsmith`, `immersiveengineering:village/houses/savanna_machinist`, `immersiveengineering:village/houses/savanna_outfitter`, `immersiveengineering:village/houses/snowy_electrician`, `immersiveengineering:village/houses/snowy_engineer`, `immersiveengineering:village/houses/snowy_gunsmith`, `immersiveengineering:village/houses/snowy_machinist`, `immersiveengineering:village/houses/snowy_outfitter`, `immersiveengineering:village/houses/taiga_electrician`, `immersiveengineering:village/houses/taiga_engineer`, `immersiveengineering:village/houses/taiga_gunsmith`, `immersiveengineering:village/houses/taiga_machinist`, `immersiveengineering:village/houses/taiga_outfitter`, `dynamicvillage:desert/desert_mech`, `dynamicvillage:desert/desert_miner`, `dynamicvillage:desert/desert_plumber`, `dynamicvillage:desert/desert_train`, `dynamicvillage:plains/plains_mech`, `dynamicvillage:plains/plains_miner`, `dynamicvillage:plains/plains_plumber`, `dynamicvillage:plains/plains_train`, `dynamicvillage:savanna/savanna_mech`, `dynamicvillage:savanna/savanna_miner`, `dynamicvillage:savanna/savanna_plumber`, `dynamicvillage:savanna/savanna_train`, `dynamicvillage:snowy/snowy_mech`, `dynamicvillage:snowy/snowy_miner`, `dynamicvillage:snowy/snowy_plumber`, `dynamicvillage:snowy/snowy_train`, `dynamicvillage:taiga/taiga_mech`, `dynamicvillage:taiga/taiga_miner`, `dynamicvillage:taiga/taiga_plumber`, `dynamicvillage:taiga/taiga_train`, `pneumaticcraft:villages/custom_street_desert`, `pneumaticcraft:villages/custom_street_snowy`, `pneumaticcraft:villages/mechanic_house_desert`, `pneumaticcraft:villages/mechanic_house_plains`, `pneumaticcraft:villages/mechanic_house_savanna`, `pneumaticcraft:villages/mechanic_house_snowy`, `pneumaticcraft:villages/mechanic_house_taiga`

### mod/chefsdelight x1 (10 pieces)

`chefsdelight:desert_chef_house`, `chefsdelight:desert_cook_house`, `chefsdelight:plains_chef_house`, `chefsdelight:plains_cook_house`, `chefsdelight:savanna_chef_house`, `chefsdelight:savanna_cook_house`, `chefsdelight:snowy_chef_house`, `chefsdelight:snowy_cook_house`, `chefsdelight:taiga_chef_house`, `chefsdelight:taiga_cook_house`

### mod/domesticationinnovation x1 (5 pieces)

`domesticationinnovation:desert_petshop`, `domesticationinnovation:plains_petshop`, `domesticationinnovation:savanna_petshop`, `domesticationinnovation:snowy_petshop`, `domesticationinnovation:taiga_petshop`

### mod/monobank x1 (22 pieces)

`monobank:village/houses/desert_vault`, `monobank:village/houses/plains_vault`, `monobank:village/houses/plains_vault_01`, `monobank:village/houses/savanna_vault`, `monobank:village/houses/snowy_vault`, `monobank:village/houses/taiga_vault`, `monobank:village/monobank/desert_locked`, `monobank:village/monobank/desert_unlocked`, `monobank:village/monobank/empty`, `monobank:village/monobank/plains_locked`, `monobank:village/monobank/plains_unlocked`, `monobank:village/monobank/savanna_locked`, `monobank:village/monobank/savanna_unlocked`, `monobank:village/monobank/snowy_locked`, `monobank:village/monobank/snowy_unlocked`, `monobank:village/monobank/taiga_locked`, `monobank:village/monobank/taiga_unlocked`, `monobank:village/streets/desert_straight_fix_01`, `monobank:village/streets/plains_straight_fix_01`, `monobank:village/streets/savanna_straight_fix_01`, `monobank:village/streets/snowy_straight_fix_01`, `monobank:village/streets/taiga_straight_fix_01`

### monobank/vault_houses x0.18 (5 pieces)

`monobank:village/houses/desert_vault`, `monobank:village/houses/plains_vault`, `monobank:village/houses/savanna_vault`, `monobank:village/houses/snowy_vault`, `monobank:village/houses/taiga_vault`

### monobank/vault_streets x0.16 (5 pieces)

`monobank:village/streets/desert_straight_fix_01`, `monobank:village/streets/plains_straight_fix_01`, `monobank:village/streets/savanna_straight_fix_01`, `monobank:village/streets/snowy_straight_fix_01`, `monobank:village/streets/taiga_straight_fix_01`

### mod/farmersdelight x1 (5 pieces)

`farmersdelight:village/houses/desert_compost_pile`, `farmersdelight:village/houses/plains_compost_pile`, `farmersdelight:village/houses/savanna_compost_pile`, `farmersdelight:village/houses/snowy_compost_pile`, `farmersdelight:village/houses/taiga_compost_pile`

### mod/veggiesdelight x0.5 (5 pieces)

`veggiesdelight:village/houses/desert_depot_house`, `veggiesdelight:village/houses/plains_depot_house`, `veggiesdelight:village/houses/savanna_depot_house`, `veggiesdelight:village/houses/snowy_depot_house`, `veggiesdelight:village/houses/taiga_depot_house`

### mod/waystones x1 (2 pieces)

`waystones:village/common/waystone`, `waystones:village/desert/waystone`

### mod/cloudstorage x1 (5 pieces)

`cloudstorage:balloon_stand_desert`, `cloudstorage:balloon_stand_plains`, `cloudstorage:balloon_stand_savanna`, `cloudstorage:balloon_stand_snowy`, `cloudstorage:balloon_stand_taiga`

### mod/etched x1 (5 pieces)

`etched:village/desert/houses/desert_bard_house_1`, `etched:village/plains/houses/plains_bard_house_1`, `etched:village/savanna/houses/savanna_bard_house_1`, `etched:village/snowy/houses/snowy_bard_house_1`, `etched:village/taiga/houses/taiga_bard_house_1`

### mod/incubation x1 (6 pieces)

`incubation:village/chickens_1`, `incubation:village/desert_chicken_coop_1`, `incubation:village/plains_chicken_coop_1`, `incubation:village/savanna_chicken_coop_1`, `incubation:village/snowy_chicken_coop_1`, `incubation:village/taiga_chicken_coop_1`

### mod/beautify x1 (5 pieces)

`beautify:botanist_house_desert`, `beautify:botanist_house_plains`, `beautify:botanist_house_savanna`, `beautify:botanist_house_snowy`, `beautify:botanist_house_taiga`

### mod/wares x1 (5 pieces)

`wares:village/houses/desert_warehouse`, `wares:village/houses/plains_warehouse`, `wares:village/houses/savanna_warehouse`, `wares:village/houses/snowy_warehouse`, `wares:village/houses/taiga_warehouse`
