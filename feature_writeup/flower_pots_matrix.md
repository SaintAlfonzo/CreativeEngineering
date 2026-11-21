# Flower potting matrix (generated)

Written by `tools/flower_pot_audit.py`; do not edit by hand. Rules and decisions: [flower_pots.md](flower_pots.md).
Plant list and vanilla pot map: live `/dumppots`. Probe results: `kubejs/exported/blocks/pot_probe.json` (cells are what the probe saw).

Y = accepted, blank = refused, part = accepted by one variant only (rich vs plain planter).

## Summary: accepted plants per holder and kind

| Holder | flower (71) | tall flower (5) | botania (203) | sapling (33) | mushroom (13) | grass / fern (20) | vine (64) | crop / seed (45) | water (74) | cactus / cane (8) | hedge (0) | other (19) | All |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Flower pot | 48 | 4 | 0 | 29 | 5 | 10 | 7 | 14 | 1 | 4 | 0 | 2 | 124 |
| Framed pot | 49 | 4 | 0 | 29 | 5 | 10 | 7 | 17 | 1 | 4 | 0 | 3 | 129 |
| Hanging pot (Supp.) | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| Flower box (Supp.) | 42 | 5 | 48 | 31 | 11 | 10 | 5 | 10 | 5 | 4 | 0 | 2 | 173 |
| Planter box (AF) | 64 | 4 | 32 | 31 | 9 | 6 | 3 | 7 | 1 | 3 | 0 | 0 | 160 |
| Hanging pot (Beautify) | 13 | 4 | 0 | 0 | 0 | 2 | 5 | 1 | 0 | 0 | 0 | 0 | 25 |
| Large pot (Floral) | 13 | 4 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 17 |
| Vinery pot | 69 | 5 | 135 | 32 | 13 | 19 | 24 | 44 | 3 | 6 | 0 | 16 | 366 |
| Vinery box | 64 | 4 | 32 | 31 | 7 | 6 | 3 | 7 | 1 | 3 | 0 | 0 | 158 |
| Silt pot (Twigs) | 67 | 0 | 87 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 154 |
| Planter (Supp.) | 70 | 4 | 203 | 33 | 11 | 13 | 18 | 37 | 0 | 4 | 0 | 10 | 403 |

## Plants no flower pot takes

Plants the vanilla pot (and so the framed and hanging pots) refuses, by kind:

- **flower** (23): `minecraft:flowering_azalea_leaves`, `corn_delight:wild_corn`, `delightful:wild_salmonberries`, `farmersdelight:wild_beetroots`, `farmersdelight:wild_cabbages`, `farmersdelight:wild_carrots`, `farmersdelight:wild_onions`, `farmersdelight:wild_potatoes`, `farmersdelight:wild_tomatoes`, `fireflybushport:firefly_bush`, `hearthandharvest:wild_hops`, `miners_delight:wild_cave_carrots`, `veggiesdelight:mature_dandelion`, `veggiesdelight:wild_broccoli`, `veggiesdelight:wild_cauliflowers`, `veggiesdelight:wild_garlic`, `veggiesdelight:wild_sweet_potatoes`, `veggiesdelight:wild_turnips`, `veggiesdelight:wild_zucchinis`, `vintagedelight:wild_cucumbers`, `vintagedelight:wild_ghost_peppers`, `vintagedelight:wild_oats`, `vintagedelight:wild_peanuts`
- **tall flower** (1): `upgrade_aquatic:flowering_rush`
- **botania** (203): `botania:agricarnation`, `botania:agricarnation_chibi`, `botania:bellethorn`, `botania:bellethorn_chibi`, `botania:bergamute`, `botania:black_double_flower`, `botania:black_floating_flower`, `botania:black_mushroom`, `botania:black_mystical_flower`, `botania:black_petal`, `botania:black_shiny_flower`, `botania:blue_double_flower`, `botania:blue_floating_flower`, `botania:blue_mushroom`, `botania:blue_mystical_flower`, `botania:blue_petal`, `botania:blue_shiny_flower`, `botania:brown_double_flower`, `botania:brown_floating_flower`, `botania:brown_mushroom`, `botania:brown_mystical_flower`, `botania:brown_petal`, `botania:brown_shiny_flower`, `botania:bubbell`, `botania:bubbell_chibi`, `botania:clayconia`, `botania:clayconia_chibi`, `botania:cyan_double_flower`, `botania:cyan_floating_flower`, `botania:cyan_mushroom`, `botania:cyan_mystical_flower`, `botania:cyan_petal`, `botania:cyan_shiny_flower`, `botania:daffomill`, `botania:dandelifeon`, `botania:daybloom_motif`, `botania:dreadthorn`, `botania:endoflame`, `botania:entropinnyum`, `botania:exoflame`, `botania:fallen_kanade`, `botania:floating_agricarnation`, `botania:floating_agricarnation_chibi`, `botania:floating_bellethorn`, `botania:floating_bellethorn_chibi`, `botania:floating_bergamute`, `botania:floating_bubbell`, `botania:floating_bubbell_chibi`, `botania:floating_clayconia`, `botania:floating_clayconia_chibi`, `botania:floating_daffomill`, `botania:floating_dandelifeon`, `botania:floating_dreadthorn`, `botania:floating_endoflame`, `botania:floating_entropinnyum`, `botania:floating_exoflame`, `botania:floating_fallen_kanade`, `botania:floating_gourmaryllis`, `botania:floating_heisei_dream`, `botania:floating_hopperhock`, `botania:floating_hopperhock_chibi`, `botania:floating_hyacidus`, `botania:floating_hydroangeas`, `botania:floating_jaded_amaranthus`, `botania:floating_jiyuulia`, `botania:floating_jiyuulia_chibi`, `botania:floating_kekimurus`, `botania:floating_labellia`, `botania:floating_loonium`, `botania:floating_manastar`, `botania:floating_marimorphosis`, `botania:floating_marimorphosis_chibi`, `botania:floating_medumone`, `botania:floating_munchdew`, `botania:floating_narslimmus`, `botania:floating_orechid`, `botania:floating_orechid_ignem`, `botania:floating_pollidisiac`, `botania:floating_pure_daisy`, `botania:floating_rafflowsia`, `botania:floating_rannuncarpus`, `botania:floating_rannuncarpus_chibi`, `botania:floating_rosa_arcana`, `botania:floating_shulk_me_not`, `botania:floating_solegnolia`, `botania:floating_solegnolia_chibi`, `botania:floating_spectranthemum`, `botania:floating_spectrolus`, `botania:floating_tangleberrie`, `botania:floating_tangleberrie_chibi`, `botania:floating_thermalily`, `botania:floating_tigerseye`, `botania:floating_vinculotus`, `botania:gourmaryllis`, `botania:gray_double_flower`, `botania:gray_floating_flower`, `botania:gray_mushroom`, `botania:gray_mystical_flower`, `botania:gray_petal`, `botania:gray_shiny_flower`, `botania:green_double_flower`, `botania:green_floating_flower`, `botania:green_mushroom`, `botania:green_mystical_flower`, `botania:green_petal`, `botania:green_shiny_flower`, `botania:heisei_dream`, `botania:hopperhock`, `botania:hopperhock_chibi`, `botania:hyacidus`, `botania:hydroangeas`, `botania:hydroangeas_motif`, `botania:jaded_amaranthus`, `botania:jiyuulia`, `botania:jiyuulia_chibi`, `botania:kekimurus`, `botania:labellia`, `botania:light_blue_double_flower`, `botania:light_blue_floating_flower`, `botania:light_blue_mushroom`, `botania:light_blue_mystical_flower`, `botania:light_blue_petal`, `botania:light_blue_shiny_flower`, `botania:light_gray_double_flower`, `botania:light_gray_floating_flower`, `botania:light_gray_mushroom`, `botania:light_gray_mystical_flower`, `botania:light_gray_petal`, `botania:light_gray_shiny_flower`, `botania:lime_double_flower`, `botania:lime_floating_flower`, `botania:lime_mushroom`, `botania:lime_mystical_flower`, `botania:lime_petal`, `botania:lime_shiny_flower`, `botania:loonium`, `botania:magenta_double_flower`, `botania:magenta_floating_flower`, `botania:magenta_mushroom`, `botania:magenta_mystical_flower`, `botania:magenta_petal`, `botania:magenta_shiny_flower`, `botania:manastar`, `botania:marimorphosis`, `botania:marimorphosis_chibi`, `botania:medumone`, `botania:munchdew`, `botania:narslimmus`, `botania:nightshade_motif`, `botania:orange_double_flower`, `botania:orange_floating_flower`, `botania:orange_mushroom`, `botania:orange_mystical_flower`, `botania:orange_petal`, `botania:orange_shiny_flower`, `botania:orechid`, `botania:orechid_ignem`, `botania:pink_double_flower`, `botania:pink_floating_flower`, `botania:pink_mushroom`, `botania:pink_mystical_flower`, `botania:pink_petal`, `botania:pink_shiny_flower`, `botania:pollidisiac`, `botania:pure_daisy`, `botania:purple_double_flower`, `botania:purple_floating_flower`, `botania:purple_mushroom`, `botania:purple_mystical_flower`, `botania:purple_petal`, `botania:purple_shiny_flower`, `botania:rafflowsia`, `botania:rannuncarpus`, `botania:rannuncarpus_chibi`, `botania:red_double_flower`, `botania:red_floating_flower`, `botania:red_mushroom`, `botania:red_mystical_flower`, `botania:red_petal`, `botania:red_shiny_flower`, `botania:rosa_arcana`, `botania:shulk_me_not`, `botania:solegnolia`, `botania:solegnolia_chibi`, `botania:spectranthemum`, `botania:spectrolus`, `botania:tangleberrie`, `botania:tangleberrie_chibi`, `botania:thermalily`, `botania:tigerseye`, `botania:vinculotus`, `botania:white_double_flower`, `botania:white_floating_flower`, `botania:white_mushroom`, `botania:white_mystical_flower`, `botania:white_petal`, `botania:white_shiny_flower`, `botania:yellow_double_flower`, `botania:yellow_floating_flower`, `botania:yellow_mushroom`, `botania:yellow_mystical_flower`, `botania:yellow_petal`, `botania:yellow_shiny_flower`
- **sapling** (4): `myrtrees:rubberwood_sapling`, `vinery:apple_tree_sapling`, `vinery:cherry_sapling`, `vintagedelight:magic_peanut`
- **mushroom** (8): `farmersdelight:brown_mushroom_colony`, `farmersdelight:red_mushroom_colony`, `fungidelight:inky_cap_mushroom`, `fungidelight:inky_cap_mushroom_colony`, `fungidelight:morel_mushroom`, `fungidelight:morel_mushroom_colony`, `nethersdelight:crimson_fungus_colony`, `nethersdelight:warped_fungus_colony`
- **grass / fern** (10): `minecraft:hanging_roots`, `minecraft:small_dripleaf`, `biomesoplenty:eyebulb`, `farmersdelight:wild_rice`, `fruitsdelight:lemon_seeds`, `fruitsdelight:lemon_tree`, `immersive_weathering:frosty_fern`, `immersive_weathering:frosty_grass`, `upgrade_aquatic:beachgrass`, `upgrade_aquatic:tall_beachgrass`
- **vine** (57): `minecraft:glow_lichen`, `minecraft:sculk_vein`, `alexsmobs:stink_bottle`, `biomesoplenty:flesh_tendons`, `biomesoplenty:flesh_tendons_strand`, `floralench:allium_bush`, `floralench:allium_vine`, `floralench:azure_bluet_bush`, `floralench:azure_bluet_vine`, `floralench:blue_orchid_bush`, `floralench:blue_orchid_vine`, `floralench:cornflower_bush`, `floralench:cornflower_vine`, `floralench:dandelion_bush`, `floralench:dandelion_vine`, `floralench:lilac_bush`, `floralench:lilac_vine`, `floralench:lily_of_the_valley_bush`, `floralench:lily_of_the_valley_vine`, `floralench:orange_tulip_bush`, `floralench:orange_tulip_vine`, `floralench:oxeye_daisy_bush`, `floralench:oxeye_daisy_vine`, `floralench:peony_bush`, `floralench:peony_vine`, `floralench:pink_tulip_bush`, `floralench:pink_tulip_vine`, `floralench:poppy_bush`, `floralench:poppy_vine`, `floralench:red_tulip_bush`, `floralench:red_tulip_vine`, `floralench:rose_bush_bush`, `floralench:rose_bush_vine`, `floralench:sunflower_bush`, `floralench:sunflower_vine`, `floralench:white_tulip_bush`, `floralench:white_tulip_vine`, `floralench:wither_rose_bush`, `floralench:wither_rose_vine`, `hearthandharvest:hops`, `immersive_weathering:frost`, `immersive_weathering:ivy`, `immersive_weathering:moss_clump`, `immersive_weathering:soot`, `quark:cut_vine`, `twigs:petrified_lichen`, `upgrade_aquatic:river_leaf_pile`, `vinery:grapevine_lattice`, `vinery:grapevine_stem`, `vinery:jungle_grape_seeds_red`, `vinery:jungle_grape_seeds_white`, `vinery:red_grape_seeds`, `vinery:savanna_grape_seeds_red`, `vinery:savanna_grape_seeds_white`, `vinery:taiga_grape_seeds_red`, `vinery:taiga_grape_seeds_white`, `vinery:white_grape_seeds`
- **crop / seed** (31): `minecraft:melon`, `minecraft:pumpkin`, `corn_delight:corn_seeds`, `delightful:cantaloupe`, `delightful:cantaloupe_seeds`, `delightful:mini_melon`, `endercrop:ender_seeds`, `farmersdelight:cabbage_seeds`, `farmersdelight:onion`, `farmersdelight:tomato_seeds`, `fruitsdelight:hamimelon`, `fruitsdelight:hamimelon_seeds`, `hearthandharvest:raspberry_pips`, `immersive_weathering:weeds`, `miners_delight:cave_carrot`, `rusticdelight:bell_pepper_seeds`, `rusticdelight:coffee_beans`, `rusticdelight:cotton_seeds`, `rusticdelight:dark_bell_pepper_seeds`, `rusticdelight:pale_bell_pepper_seeds`, `veggiesdelight:broccoli_seeds`, `veggiesdelight:cauliflower_seeds`, `veggiesdelight:garlic_clove`, `veggiesdelight:sweet_potato`, `veggiesdelight:turnip_seeds`, `veggiesdelight:zucchini_seeds`, `vintagedelight:cucumber_seeds`, `vintagedelight:ghost_pepper`, `vintagedelight:ghost_pepper_seeds`, `vintagedelight:oat_seeds`, `vintagedelight:peanut`
- **water** (73): `minecraft:brain_coral`, `minecraft:brain_coral_fan`, `minecraft:bubble_coral`, `minecraft:bubble_coral_fan`, `minecraft:dead_brain_coral`, `minecraft:dead_brain_coral_fan`, `minecraft:dead_bubble_coral`, `minecraft:dead_bubble_coral_fan`, `minecraft:dead_fire_coral`, `minecraft:dead_fire_coral_fan`, `minecraft:dead_horn_coral`, `minecraft:dead_horn_coral_fan`, `minecraft:dead_tube_coral`, `minecraft:dead_tube_coral_fan`, `minecraft:fire_coral`, `minecraft:fire_coral_fan`, `minecraft:horn_coral`, `minecraft:horn_coral_fan`, `minecraft:kelp`, `minecraft:lily_pad`, `minecraft:seagrass`, `minecraft:tube_coral`, `minecraft:tube_coral_fan`, `upgrade_aquatic:acan_coral`, `upgrade_aquatic:acan_coral_fan`, `upgrade_aquatic:branch_coral`, `upgrade_aquatic:branch_coral_fan`, `upgrade_aquatic:chrome_coral`, `upgrade_aquatic:chrome_coral_fan`, `upgrade_aquatic:dead_acan_coral`, `upgrade_aquatic:dead_acan_coral_fan`, `upgrade_aquatic:dead_branch_coral`, `upgrade_aquatic:dead_branch_coral_fan`, `upgrade_aquatic:dead_chrome_coral`, `upgrade_aquatic:dead_chrome_coral_fan`, `upgrade_aquatic:dead_finger_coral`, `upgrade_aquatic:dead_finger_coral_fan`, `upgrade_aquatic:dead_moss_coral`, `upgrade_aquatic:dead_moss_coral_fan`, `upgrade_aquatic:dead_petal_coral`, `upgrade_aquatic:dead_petal_coral_fan`, `upgrade_aquatic:dead_pillow_coral`, `upgrade_aquatic:dead_pillow_coral_fan`, `upgrade_aquatic:dead_rock_coral`, `upgrade_aquatic:dead_rock_coral_fan`, `upgrade_aquatic:dead_silk_coral`, `upgrade_aquatic:dead_silk_coral_fan`, `upgrade_aquatic:dead_star_coral`, `upgrade_aquatic:dead_star_coral_fan`, `upgrade_aquatic:elder_prismarine_coral`, `upgrade_aquatic:elder_prismarine_coral_fan`, `upgrade_aquatic:elder_prismarine_coral_shower`, `upgrade_aquatic:finger_coral`, `upgrade_aquatic:finger_coral_fan`, `upgrade_aquatic:moss_coral`, `upgrade_aquatic:moss_coral_fan`, `upgrade_aquatic:ochre_kelp`, `upgrade_aquatic:petal_coral`, `upgrade_aquatic:petal_coral_fan`, `upgrade_aquatic:pillow_coral`, `upgrade_aquatic:pillow_coral_fan`, `upgrade_aquatic:polar_kelp`, `upgrade_aquatic:prismarine_coral`, `upgrade_aquatic:prismarine_coral_fan`, `upgrade_aquatic:prismarine_coral_shower`, `upgrade_aquatic:rock_coral`, `upgrade_aquatic:rock_coral_fan`, `upgrade_aquatic:silk_coral`, `upgrade_aquatic:silk_coral_fan`, `upgrade_aquatic:star_coral`, `upgrade_aquatic:star_coral_fan`, `upgrade_aquatic:thorny_kelp`, `upgrade_aquatic:tongue_kelp`
- **cactus / cane** (4): `minecraft:chorus_plant`, `endersdelight:chorusflame`, `ends_delight:chorus_succulent`, `twigs:bamboo_leaves`
- **other** (17): `minecraft:big_dripleaf`, `minecraft:moss_block`, `minecraft:spore_blossom`, `alexsmobs:banana_peel`, `delightful:salmonberry_pips`, `endersdelight:amberveil`, `endersdelight:ethereal_saffron`, `farmersdelight:rice`, `farmersdelight:sandy_shrub`, `fruitsdelight:blueberry`, `fruitsdelight:blueberry_bush`, `fruitsdelight:cranberry`, `fruitsdelight:pineapple_sapling`, `fruitsdelight:wild_pineapple`, `neapolitan:strawberry_pips`, `nethersdelight:propelplant_cane`, `supplementaries:wild_flax`

## Probe against prediction

- hanging: `minecraft:acacia_sapling` predicted yes, probe no
- vpot: `minecraft:acacia_sapling` predicted no, probe yes
- vbox: `minecraft:acacia_sapling` predicted no, probe yes
- hanging: `minecraft:allium` predicted yes, probe no
- vpot: `minecraft:allium` predicted no, probe yes
- hanging: `minecraft:azalea` predicted yes, probe no
- fbox: `minecraft:azalea` predicted yes, probe no
- hanging: `minecraft:azure_bluet` predicted yes, probe no
- vpot: `minecraft:azure_bluet` predicted no, probe yes
- hanging: `minecraft:bamboo` predicted yes, probe no
- vpot: `minecraft:bamboo` predicted no, probe yes
- vbox: `minecraft:bamboo` predicted no, probe yes
- hanging: `minecraft:beetroot_seeds` predicted yes, probe no
- vpot: `minecraft:beetroot_seeds` predicted no, probe yes
- vbox: `minecraft:beetroot_seeds` predicted no, probe yes
- hanging: `minecraft:birch_sapling` predicted yes, probe no
- vpot: `minecraft:birch_sapling` predicted no, probe yes
- vbox: `minecraft:birch_sapling` predicted no, probe yes
- hanging: `minecraft:blue_orchid` predicted yes, probe no
- vpot: `minecraft:blue_orchid` predicted no, probe yes
- hanging: `minecraft:brown_mushroom` predicted yes, probe no
- vpot: `minecraft:brown_mushroom` predicted no, probe yes
- vbox: `minecraft:brown_mushroom` predicted no, probe yes
- hanging: `minecraft:cactus` predicted yes, probe no
- vpot: `minecraft:cactus` predicted no, probe yes
- vbox: `minecraft:cactus` predicted no, probe yes
- hanging: `minecraft:carrot` predicted yes, probe no
- vpot: `minecraft:carrot` predicted no, probe yes
- vbox: `minecraft:carrot` predicted no, probe yes
- hanging: `minecraft:chorus_flower` predicted yes, probe no
- vbox: `minecraft:chorus_flower` predicted no, probe yes
- silt: `minecraft:chorus_flower` predicted yes, probe no
- hanging: `minecraft:cocoa_beans` predicted yes, probe no
- vbox: `minecraft:cocoa_beans` predicted no, probe yes
- hanging: `minecraft:cornflower` predicted yes, probe no
- vpot: `minecraft:cornflower` predicted no, probe yes
- hanging: `minecraft:crimson_fungus` predicted yes, probe no
- vpot: `minecraft:crimson_fungus` predicted no, probe yes
- vbox: `minecraft:crimson_fungus` predicted no, probe yes
- hanging: `minecraft:crimson_roots` predicted yes, probe no
- vpot: `minecraft:crimson_roots` predicted no, probe yes
- vbox: `minecraft:crimson_roots` predicted no, probe yes
- hanging: `minecraft:dandelion` predicted yes, probe no
- vpot: `minecraft:dandelion` predicted no, probe yes
- hanging: `minecraft:dark_oak_sapling` predicted yes, probe no
- vpot: `minecraft:dark_oak_sapling` predicted no, probe yes
- vbox: `minecraft:dark_oak_sapling` predicted no, probe yes
- hanging: `minecraft:dead_bush` predicted yes, probe no
- vpot: `minecraft:dead_bush` predicted no, probe yes
- vbox: `minecraft:dead_bush` predicted no, probe yes
- hanging: `minecraft:fern` predicted yes, probe no
- vpot: `minecraft:fern` predicted no, probe yes
- vbox: `minecraft:fern` predicted no, probe yes
- hanging: `minecraft:flowering_azalea` predicted yes, probe no
- fbox: `minecraft:flowering_azalea` predicted yes, probe no
- framed: `minecraft:flowering_azalea_leaves` predicted no, probe yes
- vbox: `minecraft:flowering_azalea_leaves` predicted no, probe yes
- planter: `minecraft:flowering_azalea_leaves` predicted no, probe yes
- hanging: `minecraft:glow_berries` predicted yes, probe no
- fbox: `minecraft:glow_berries` predicted no, probe yes
- vbox: `minecraft:glow_berries` predicted no, probe yes
- fbox: `minecraft:glow_lichen` predicted no, probe yes
- vbox: `minecraft:glow_lichen` predicted no, probe yes
- hanging: `minecraft:grass` predicted yes, probe no
- vpot: `minecraft:grass` predicted no, probe yes
- vbox: `minecraft:grass` predicted no, probe yes
- hanging: `minecraft:jungle_sapling` predicted yes, probe no
- vpot: `minecraft:jungle_sapling` predicted no, probe yes
- vbox: `minecraft:jungle_sapling` predicted no, probe yes
- hanging: `minecraft:large_fern` predicted yes, probe no
- vpot: `minecraft:large_fern` predicted no, probe yes
- hanging: `minecraft:lilac` predicted yes, probe no
- vbox: `minecraft:lilac` predicted no, probe yes
- hanging: `minecraft:lily_of_the_valley` predicted yes, probe no
- vpot: `minecraft:lily_of_the_valley` predicted no, probe yes
- vpot: `minecraft:lily_pad` predicted no, probe yes
- planter: `minecraft:lily_pad` predicted yes, probe no
- hanging: `minecraft:mangrove_propagule` predicted yes, probe no
- vpot: `minecraft:mangrove_propagule` predicted no, probe yes
- vbox: `minecraft:mangrove_propagule` predicted no, probe yes
- framed: `minecraft:melon` predicted no, probe yes
- vpot: `minecraft:melon` predicted no, probe yes
- hanging: `minecraft:melon_seeds` predicted yes, probe no
- vpot: `minecraft:melon_seeds` predicted no, probe yes
- vbox: `minecraft:melon_seeds` predicted no, probe yes
- framed: `minecraft:moss_block` predicted no, probe yes
- planter: `minecraft:moss_block` predicted no, probe yes
- hanging: `minecraft:nether_sprouts` predicted yes, probe no
- vpot: `minecraft:nether_sprouts` predicted no, probe yes
- vbox: `minecraft:nether_sprouts` predicted no, probe yes
- hanging: `minecraft:nether_wart` predicted yes, probe no
- vpot: `minecraft:nether_wart` predicted no, probe yes
- vbox: `minecraft:nether_wart` predicted no, probe yes
- hanging: `minecraft:oak_sapling` predicted yes, probe no
- vpot: `minecraft:oak_sapling` predicted no, probe yes
- vbox: `minecraft:oak_sapling` predicted no, probe yes
- hanging: `minecraft:orange_tulip` predicted yes, probe no
- vpot: `minecraft:orange_tulip` predicted no, probe yes
- hanging: `minecraft:oxeye_daisy` predicted yes, probe no
- vpot: `minecraft:oxeye_daisy` predicted no, probe yes
- hanging: `minecraft:peony` predicted yes, probe no
- vbox: `minecraft:peony` predicted no, probe yes
- hanging: `minecraft:pink_tulip` predicted yes, probe no
- vpot: `minecraft:pink_tulip` predicted no, probe yes
- hanging: `minecraft:poppy` predicted yes, probe no
- vpot: `minecraft:poppy` predicted no, probe yes
- hanging: `minecraft:potato` predicted yes, probe no
- vpot: `minecraft:potato` predicted no, probe yes
- vbox: `minecraft:potato` predicted no, probe yes
- framed: `minecraft:pumpkin` predicted no, probe yes
- vpot: `minecraft:pumpkin` predicted no, probe yes
- hanging: `minecraft:pumpkin_seeds` predicted yes, probe no
- vpot: `minecraft:pumpkin_seeds` predicted no, probe yes
- vbox: `minecraft:pumpkin_seeds` predicted no, probe yes
- hanging: `minecraft:red_mushroom` predicted yes, probe no
- vpot: `minecraft:red_mushroom` predicted no, probe yes
- vbox: `minecraft:red_mushroom` predicted no, probe yes
- hanging: `minecraft:red_tulip` predicted yes, probe no
- vpot: `minecraft:red_tulip` predicted no, probe yes
- hanging: `minecraft:rose_bush` predicted yes, probe no
- vbox: `minecraft:rose_bush` predicted no, probe yes
- hanging: `minecraft:sea_pickle` predicted yes, probe no
- vpot: `minecraft:sea_pickle` predicted no, probe yes
- vbox: `minecraft:sea_pickle` predicted no, probe yes
- planter: `minecraft:sea_pickle` predicted yes, probe no
- vpot: `minecraft:seagrass` predicted no, probe yes
- planter: `minecraft:seagrass` predicted yes, probe no
- vpot: `minecraft:small_dripleaf` predicted no, probe yes
- planter: `minecraft:small_dripleaf` predicted yes, probe no
- hanging: `minecraft:spruce_sapling` predicted yes, probe no
- vpot: `minecraft:spruce_sapling` predicted no, probe yes
- vbox: `minecraft:spruce_sapling` predicted no, probe yes
- hanging: `minecraft:sugar_cane` predicted yes, probe no
- vpot: `minecraft:sugar_cane` predicted no, probe yes
- hanging: `minecraft:sunflower` predicted yes, probe no
- vbox: `minecraft:sunflower` predicted no, probe yes
- hanging: `minecraft:sweet_berries` predicted yes, probe no
- vpot: `minecraft:sweet_berries` predicted no, probe yes
- planter: `minecraft:sweet_berries` predicted yes, probe no
- hanging: `minecraft:tall_grass` predicted yes, probe no
- vpot: `minecraft:tall_grass` predicted no, probe yes
- hanging: `minecraft:twisting_vines` predicted yes, probe no
- hanging: `minecraft:vine` predicted yes, probe no
- hanging: `minecraft:warped_fungus` predicted yes, probe no
- vpot: `minecraft:warped_fungus` predicted no, probe yes
- vbox: `minecraft:warped_fungus` predicted no, probe yes
- hanging: `minecraft:warped_roots` predicted yes, probe no
- vpot: `minecraft:warped_roots` predicted no, probe yes
- vbox: `minecraft:warped_roots` predicted no, probe yes
- hanging: `minecraft:weeping_vines` predicted yes, probe no
- hanging: `minecraft:wheat_seeds` predicted yes, probe no
- vpot: `minecraft:wheat_seeds` predicted no, probe yes
- hanging: `minecraft:white_tulip` predicted yes, probe no
- vpot: `minecraft:white_tulip` predicted no, probe yes
- hanging: `minecraft:wither_rose` predicted yes, probe no
- vpot: `minecraft:wither_rose` predicted no, probe yes
- vpot: `alexsmobs:banana_peel` predicted no, probe yes
- hanging: `architects_palette:twisted_sapling` predicted yes, probe no
- vpot: `architects_palette:twisted_sapling` predicted no, probe yes
- vbox: `architects_palette:twisted_sapling` predicted no, probe yes
- hanging: `berry_good:glow_berry_pips` predicted yes, probe no
- hanging: `berry_good:sweet_berry_pips` predicted yes, probe no
- vpot: `berry_good:sweet_berry_pips` predicted no, probe yes
- vpot: `biomesoplenty:eyebulb` predicted no, probe yes
- planter: `biomesoplenty:eyebulb` predicted yes, probe no
- vpot: `botania:agricarnation` predicted no, probe yes
- vpot: `botania:agricarnation_chibi` predicted no, probe yes
- vpot: `botania:bellethorn` predicted no, probe yes
- vpot: `botania:bellethorn_chibi` predicted no, probe yes
- vpot: `botania:bergamute` predicted no, probe yes
- silt: `botania:black_floating_flower` predicted yes, probe no
- planter: `botania:black_floating_flower` predicted no, probe yes
- vpot: `botania:black_mushroom` predicted no, probe yes
- vbox: `botania:black_mushroom` predicted no, probe yes
- vpot: `botania:black_mystical_flower` predicted no, probe yes
- vpot: `botania:black_petal` predicted no, probe yes
- vpot: `botania:black_shiny_flower` predicted no, probe yes
- silt: `botania:blue_floating_flower` predicted yes, probe no
- planter: `botania:blue_floating_flower` predicted no, probe yes
- vpot: `botania:blue_mushroom` predicted no, probe yes
- vbox: `botania:blue_mushroom` predicted no, probe yes
- vpot: `botania:blue_mystical_flower` predicted no, probe yes
- vpot: `botania:blue_petal` predicted no, probe yes
- vpot: `botania:blue_shiny_flower` predicted no, probe yes
- silt: `botania:brown_floating_flower` predicted yes, probe no
- planter: `botania:brown_floating_flower` predicted no, probe yes
- vpot: `botania:brown_mushroom` predicted no, probe yes
- vbox: `botania:brown_mushroom` predicted no, probe yes
- vpot: `botania:brown_mystical_flower` predicted no, probe yes
- vpot: `botania:brown_petal` predicted no, probe yes
- vpot: `botania:brown_shiny_flower` predicted no, probe yes
- vpot: `botania:bubbell` predicted no, probe yes
- vpot: `botania:bubbell_chibi` predicted no, probe yes
- vpot: `botania:clayconia` predicted no, probe yes
- vpot: `botania:clayconia_chibi` predicted no, probe yes
- silt: `botania:cyan_floating_flower` predicted yes, probe no
- planter: `botania:cyan_floating_flower` predicted no, probe yes
- vpot: `botania:cyan_mushroom` predicted no, probe yes
- vbox: `botania:cyan_mushroom` predicted no, probe yes
- vpot: `botania:cyan_mystical_flower` predicted no, probe yes
- vpot: `botania:cyan_petal` predicted no, probe yes
- vpot: `botania:cyan_shiny_flower` predicted no, probe yes
- vpot: `botania:daffomill` predicted no, probe yes
- vpot: `botania:dandelifeon` predicted no, probe yes
- vpot: `botania:daybloom_motif` predicted no, probe yes
- vpot: `botania:dreadthorn` predicted no, probe yes
- vpot: `botania:endoflame` predicted no, probe yes
- vpot: `botania:entropinnyum` predicted no, probe yes
- vpot: `botania:exoflame` predicted no, probe yes
- vpot: `botania:fallen_kanade` predicted no, probe yes
- silt: `botania:floating_agricarnation` predicted yes, probe no
- planter: `botania:floating_agricarnation` predicted no, probe yes
- silt: `botania:floating_agricarnation_chibi` predicted yes, probe no
- planter: `botania:floating_agricarnation_chibi` predicted no, probe yes
- silt: `botania:floating_bellethorn` predicted yes, probe no
- planter: `botania:floating_bellethorn` predicted no, probe yes
- silt: `botania:floating_bellethorn_chibi` predicted yes, probe no
- planter: `botania:floating_bellethorn_chibi` predicted no, probe yes
- silt: `botania:floating_bergamute` predicted yes, probe no
- planter: `botania:floating_bergamute` predicted no, probe yes
- silt: `botania:floating_bubbell` predicted yes, probe no
- planter: `botania:floating_bubbell` predicted no, probe yes
- silt: `botania:floating_bubbell_chibi` predicted yes, probe no
- planter: `botania:floating_bubbell_chibi` predicted no, probe yes
- silt: `botania:floating_clayconia` predicted yes, probe no
- planter: `botania:floating_clayconia` predicted no, probe yes
- silt: `botania:floating_clayconia_chibi` predicted yes, probe no
- planter: `botania:floating_clayconia_chibi` predicted no, probe yes
- silt: `botania:floating_daffomill` predicted yes, probe no
- planter: `botania:floating_daffomill` predicted no, probe yes
- silt: `botania:floating_dandelifeon` predicted yes, probe no
- planter: `botania:floating_dandelifeon` predicted no, probe yes
- silt: `botania:floating_dreadthorn` predicted yes, probe no
- planter: `botania:floating_dreadthorn` predicted no, probe yes
- silt: `botania:floating_endoflame` predicted yes, probe no
- planter: `botania:floating_endoflame` predicted no, probe yes
- silt: `botania:floating_entropinnyum` predicted yes, probe no
- planter: `botania:floating_entropinnyum` predicted no, probe yes
- silt: `botania:floating_exoflame` predicted yes, probe no
- planter: `botania:floating_exoflame` predicted no, probe yes
- silt: `botania:floating_fallen_kanade` predicted yes, probe no
- planter: `botania:floating_fallen_kanade` predicted no, probe yes
- silt: `botania:floating_gourmaryllis` predicted yes, probe no
- planter: `botania:floating_gourmaryllis` predicted no, probe yes
- silt: `botania:floating_heisei_dream` predicted yes, probe no
- planter: `botania:floating_heisei_dream` predicted no, probe yes
- silt: `botania:floating_hopperhock` predicted yes, probe no
- planter: `botania:floating_hopperhock` predicted no, probe yes
- silt: `botania:floating_hopperhock_chibi` predicted yes, probe no
- planter: `botania:floating_hopperhock_chibi` predicted no, probe yes
- silt: `botania:floating_hyacidus` predicted yes, probe no
- planter: `botania:floating_hyacidus` predicted no, probe yes
- silt: `botania:floating_hydroangeas` predicted yes, probe no
- planter: `botania:floating_hydroangeas` predicted no, probe yes
- silt: `botania:floating_jaded_amaranthus` predicted yes, probe no
- planter: `botania:floating_jaded_amaranthus` predicted no, probe yes
- silt: `botania:floating_jiyuulia` predicted yes, probe no
- planter: `botania:floating_jiyuulia` predicted no, probe yes
- silt: `botania:floating_jiyuulia_chibi` predicted yes, probe no
- planter: `botania:floating_jiyuulia_chibi` predicted no, probe yes
- silt: `botania:floating_kekimurus` predicted yes, probe no
- planter: `botania:floating_kekimurus` predicted no, probe yes
- silt: `botania:floating_labellia` predicted yes, probe no
- planter: `botania:floating_labellia` predicted no, probe yes
- silt: `botania:floating_loonium` predicted yes, probe no
- planter: `botania:floating_loonium` predicted no, probe yes
- silt: `botania:floating_manastar` predicted yes, probe no
- planter: `botania:floating_manastar` predicted no, probe yes
- silt: `botania:floating_marimorphosis` predicted yes, probe no
- planter: `botania:floating_marimorphosis` predicted no, probe yes
- silt: `botania:floating_marimorphosis_chibi` predicted yes, probe no
- planter: `botania:floating_marimorphosis_chibi` predicted no, probe yes
- silt: `botania:floating_medumone` predicted yes, probe no
- planter: `botania:floating_medumone` predicted no, probe yes
- silt: `botania:floating_munchdew` predicted yes, probe no
- planter: `botania:floating_munchdew` predicted no, probe yes
- silt: `botania:floating_narslimmus` predicted yes, probe no
- planter: `botania:floating_narslimmus` predicted no, probe yes
- silt: `botania:floating_orechid` predicted yes, probe no
- planter: `botania:floating_orechid` predicted no, probe yes
- silt: `botania:floating_orechid_ignem` predicted yes, probe no
- planter: `botania:floating_orechid_ignem` predicted no, probe yes
- silt: `botania:floating_pollidisiac` predicted yes, probe no
- planter: `botania:floating_pollidisiac` predicted no, probe yes
- silt: `botania:floating_pure_daisy` predicted yes, probe no
- planter: `botania:floating_pure_daisy` predicted no, probe yes
- silt: `botania:floating_rafflowsia` predicted yes, probe no
- planter: `botania:floating_rafflowsia` predicted no, probe yes
- silt: `botania:floating_rannuncarpus` predicted yes, probe no
- planter: `botania:floating_rannuncarpus` predicted no, probe yes
- silt: `botania:floating_rannuncarpus_chibi` predicted yes, probe no
- planter: `botania:floating_rannuncarpus_chibi` predicted no, probe yes
- silt: `botania:floating_rosa_arcana` predicted yes, probe no
- planter: `botania:floating_rosa_arcana` predicted no, probe yes
- silt: `botania:floating_shulk_me_not` predicted yes, probe no
- planter: `botania:floating_shulk_me_not` predicted no, probe yes
- silt: `botania:floating_solegnolia` predicted yes, probe no
- planter: `botania:floating_solegnolia` predicted no, probe yes
- silt: `botania:floating_solegnolia_chibi` predicted yes, probe no
- planter: `botania:floating_solegnolia_chibi` predicted no, probe yes
- silt: `botania:floating_spectranthemum` predicted yes, probe no
- planter: `botania:floating_spectranthemum` predicted no, probe yes
- silt: `botania:floating_spectrolus` predicted yes, probe no
- planter: `botania:floating_spectrolus` predicted no, probe yes
- silt: `botania:floating_tangleberrie` predicted yes, probe no
- planter: `botania:floating_tangleberrie` predicted no, probe yes
- silt: `botania:floating_tangleberrie_chibi` predicted yes, probe no
- planter: `botania:floating_tangleberrie_chibi` predicted no, probe yes
- silt: `botania:floating_thermalily` predicted yes, probe no
- planter: `botania:floating_thermalily` predicted no, probe yes
- silt: `botania:floating_tigerseye` predicted yes, probe no
- planter: `botania:floating_tigerseye` predicted no, probe yes
- silt: `botania:floating_vinculotus` predicted yes, probe no
- planter: `botania:floating_vinculotus` predicted no, probe yes
- vpot: `botania:gourmaryllis` predicted no, probe yes
- silt: `botania:gray_floating_flower` predicted yes, probe no
- planter: `botania:gray_floating_flower` predicted no, probe yes
- vpot: `botania:gray_mushroom` predicted no, probe yes
- vbox: `botania:gray_mushroom` predicted no, probe yes
- vpot: `botania:gray_mystical_flower` predicted no, probe yes
- vpot: `botania:gray_petal` predicted no, probe yes
- vpot: `botania:gray_shiny_flower` predicted no, probe yes
- silt: `botania:green_floating_flower` predicted yes, probe no
- planter: `botania:green_floating_flower` predicted no, probe yes
- vpot: `botania:green_mushroom` predicted no, probe yes
- vbox: `botania:green_mushroom` predicted no, probe yes
- vpot: `botania:green_mystical_flower` predicted no, probe yes
- vpot: `botania:green_petal` predicted no, probe yes
- vpot: `botania:green_shiny_flower` predicted no, probe yes
- vpot: `botania:heisei_dream` predicted no, probe yes
- vpot: `botania:hopperhock` predicted no, probe yes
- vpot: `botania:hopperhock_chibi` predicted no, probe yes
- vpot: `botania:hyacidus` predicted no, probe yes
- vpot: `botania:hydroangeas` predicted no, probe yes
- vpot: `botania:hydroangeas_motif` predicted no, probe yes
- vpot: `botania:jaded_amaranthus` predicted no, probe yes
- vpot: `botania:jiyuulia` predicted no, probe yes
- vpot: `botania:jiyuulia_chibi` predicted no, probe yes
- vpot: `botania:kekimurus` predicted no, probe yes
- vpot: `botania:labellia` predicted no, probe yes
- silt: `botania:light_blue_floating_flower` predicted yes, probe no
- planter: `botania:light_blue_floating_flower` predicted no, probe yes
- vpot: `botania:light_blue_mushroom` predicted no, probe yes
- vbox: `botania:light_blue_mushroom` predicted no, probe yes
- vpot: `botania:light_blue_mystical_flower` predicted no, probe yes
- vpot: `botania:light_blue_petal` predicted no, probe yes
- vpot: `botania:light_blue_shiny_flower` predicted no, probe yes
- silt: `botania:light_gray_floating_flower` predicted yes, probe no
- planter: `botania:light_gray_floating_flower` predicted no, probe yes
- vpot: `botania:light_gray_mushroom` predicted no, probe yes
- vbox: `botania:light_gray_mushroom` predicted no, probe yes
- vpot: `botania:light_gray_mystical_flower` predicted no, probe yes
- vpot: `botania:light_gray_petal` predicted no, probe yes
- vpot: `botania:light_gray_shiny_flower` predicted no, probe yes
- silt: `botania:lime_floating_flower` predicted yes, probe no
- planter: `botania:lime_floating_flower` predicted no, probe yes
- vpot: `botania:lime_mushroom` predicted no, probe yes
- vbox: `botania:lime_mushroom` predicted no, probe yes
- vpot: `botania:lime_mystical_flower` predicted no, probe yes
- vpot: `botania:lime_petal` predicted no, probe yes
- vpot: `botania:lime_shiny_flower` predicted no, probe yes
- vpot: `botania:loonium` predicted no, probe yes
- silt: `botania:magenta_floating_flower` predicted yes, probe no
- planter: `botania:magenta_floating_flower` predicted no, probe yes
- vpot: `botania:magenta_mushroom` predicted no, probe yes
- vbox: `botania:magenta_mushroom` predicted no, probe yes
- vpot: `botania:magenta_mystical_flower` predicted no, probe yes
- vpot: `botania:magenta_petal` predicted no, probe yes
- vpot: `botania:magenta_shiny_flower` predicted no, probe yes
- vpot: `botania:manastar` predicted no, probe yes
- vpot: `botania:marimorphosis` predicted no, probe yes
- vpot: `botania:marimorphosis_chibi` predicted no, probe yes
- vpot: `botania:medumone` predicted no, probe yes
- vpot: `botania:munchdew` predicted no, probe yes
- vpot: `botania:narslimmus` predicted no, probe yes
- vpot: `botania:nightshade_motif` predicted no, probe yes
- silt: `botania:orange_floating_flower` predicted yes, probe no
- planter: `botania:orange_floating_flower` predicted no, probe yes
- vpot: `botania:orange_mushroom` predicted no, probe yes
- vbox: `botania:orange_mushroom` predicted no, probe yes
- vpot: `botania:orange_mystical_flower` predicted no, probe yes
- vpot: `botania:orange_petal` predicted no, probe yes
- vpot: `botania:orange_shiny_flower` predicted no, probe yes
- vpot: `botania:orechid` predicted no, probe yes
- vpot: `botania:orechid_ignem` predicted no, probe yes
- silt: `botania:pink_floating_flower` predicted yes, probe no
- planter: `botania:pink_floating_flower` predicted no, probe yes
- vpot: `botania:pink_mushroom` predicted no, probe yes
- vbox: `botania:pink_mushroom` predicted no, probe yes
- vpot: `botania:pink_mystical_flower` predicted no, probe yes
- vpot: `botania:pink_petal` predicted no, probe yes
- vpot: `botania:pink_shiny_flower` predicted no, probe yes
- vpot: `botania:pollidisiac` predicted no, probe yes
- vpot: `botania:pure_daisy` predicted no, probe yes
- silt: `botania:purple_floating_flower` predicted yes, probe no
- planter: `botania:purple_floating_flower` predicted no, probe yes
- vpot: `botania:purple_mushroom` predicted no, probe yes
- vbox: `botania:purple_mushroom` predicted no, probe yes
- vpot: `botania:purple_mystical_flower` predicted no, probe yes
- vpot: `botania:purple_petal` predicted no, probe yes
- vpot: `botania:purple_shiny_flower` predicted no, probe yes
- vpot: `botania:rafflowsia` predicted no, probe yes
- vpot: `botania:rannuncarpus` predicted no, probe yes
- vpot: `botania:rannuncarpus_chibi` predicted no, probe yes
- silt: `botania:red_floating_flower` predicted yes, probe no
- planter: `botania:red_floating_flower` predicted no, probe yes
- vpot: `botania:red_mushroom` predicted no, probe yes
- vbox: `botania:red_mushroom` predicted no, probe yes
- vpot: `botania:red_mystical_flower` predicted no, probe yes
- vpot: `botania:red_petal` predicted no, probe yes
- vpot: `botania:red_shiny_flower` predicted no, probe yes
- vpot: `botania:rosa_arcana` predicted no, probe yes
- vpot: `botania:shulk_me_not` predicted no, probe yes
- vpot: `botania:solegnolia` predicted no, probe yes
- vpot: `botania:solegnolia_chibi` predicted no, probe yes
- vpot: `botania:spectranthemum` predicted no, probe yes
- vpot: `botania:spectrolus` predicted no, probe yes
- vpot: `botania:tangleberrie` predicted no, probe yes
- vpot: `botania:tangleberrie_chibi` predicted no, probe yes
- vpot: `botania:thermalily` predicted no, probe yes
- vpot: `botania:tigerseye` predicted no, probe yes
- vpot: `botania:vinculotus` predicted no, probe yes
- silt: `botania:white_floating_flower` predicted yes, probe no
- planter: `botania:white_floating_flower` predicted no, probe yes
- vpot: `botania:white_mushroom` predicted no, probe yes
- vbox: `botania:white_mushroom` predicted no, probe yes
- vpot: `botania:white_mystical_flower` predicted no, probe yes
- vpot: `botania:white_petal` predicted no, probe yes
- vpot: `botania:white_shiny_flower` predicted no, probe yes
- silt: `botania:yellow_floating_flower` predicted yes, probe no
- planter: `botania:yellow_floating_flower` predicted no, probe yes
- vpot: `botania:yellow_mushroom` predicted no, probe yes
- vbox: `botania:yellow_mushroom` predicted no, probe yes
- vpot: `botania:yellow_mystical_flower` predicted no, probe yes
- vpot: `botania:yellow_petal` predicted no, probe yes
- vpot: `botania:yellow_shiny_flower` predicted no, probe yes
- hanging: `bushierflowers:grown_allium` predicted yes, probe no
- vpot: `bushierflowers:grown_allium` predicted no, probe yes
- hanging: `bushierflowers:grown_azure_bluet` predicted yes, probe no
- vpot: `bushierflowers:grown_azure_bluet` predicted no, probe yes
- hanging: `bushierflowers:grown_blue_orchid` predicted yes, probe no
- vpot: `bushierflowers:grown_blue_orchid` predicted no, probe yes
- hanging: `bushierflowers:grown_cornflower` predicted yes, probe no
- vpot: `bushierflowers:grown_cornflower` predicted no, probe yes
- hanging: `bushierflowers:grown_dandelion` predicted yes, probe no
- vpot: `bushierflowers:grown_dandelion` predicted no, probe yes
- hanging: `bushierflowers:grown_lily` predicted yes, probe no
- vpot: `bushierflowers:grown_lily` predicted no, probe yes
- hanging: `bushierflowers:grown_orange_tulip` predicted yes, probe no
- vpot: `bushierflowers:grown_orange_tulip` predicted no, probe yes
- hanging: `bushierflowers:grown_oxeye_daisy` predicted yes, probe no
- vpot: `bushierflowers:grown_oxeye_daisy` predicted no, probe yes
- hanging: `bushierflowers:grown_pink_tulip` predicted yes, probe no
- vpot: `bushierflowers:grown_pink_tulip` predicted no, probe yes
- hanging: `bushierflowers:grown_poppy` predicted yes, probe no
- vpot: `bushierflowers:grown_poppy` predicted no, probe yes
- hanging: `bushierflowers:grown_red_tulip` predicted yes, probe no
- vpot: `bushierflowers:grown_red_tulip` predicted no, probe yes
- hanging: `bushierflowers:grown_white_tulip` predicted yes, probe no
- vpot: `bushierflowers:grown_white_tulip` predicted no, probe yes
- hanging: `bushierflowers:grown_wither_rose` predicted yes, probe no
- vpot: `bushierflowers:grown_wither_rose` predicted no, probe yes
- hanging: `buzzier_bees:buttercup` predicted yes, probe no
- vpot: `buzzier_bees:buttercup` predicted no, probe yes
- hanging: `buzzier_bees:pink_clover` predicted yes, probe no
- vpot: `buzzier_bees:pink_clover` predicted no, probe yes
- hanging: `buzzier_bees:white_clover` predicted yes, probe no
- vpot: `buzzier_bees:white_clover` predicted no, probe yes
- vpot: `corn_delight:corn_seeds` predicted no, probe yes
- vpot: `corn_delight:wild_corn` predicted no, probe yes
- vpot: `delightful:cantaloupe` predicted no, probe yes
- vpot: `delightful:cantaloupe_seeds` predicted no, probe yes
- vpot: `delightful:mini_melon` predicted no, probe yes
- vpot: `delightful:salmonberry_pips` predicted no, probe yes
- vpot: `delightful:wild_salmonberries` predicted no, probe yes
- vbox: `delightful:wild_salmonberries` predicted no, probe yes
- vpot: `endercrop:ender_seeds` predicted no, probe yes
- planter: `endercrop:ender_seeds` predicted yes, probe no
- vpot: `endersdelight:amberveil` predicted no, probe yes
- planter: `endersdelight:amberveil` predicted yes, probe no
- vpot: `endersdelight:chorusflame` predicted no, probe yes
- planter: `endersdelight:chorusflame` predicted yes, probe no
- vpot: `endersdelight:ethereal_saffron` predicted no, probe yes
- planter: `endersdelight:ethereal_saffron` predicted yes, probe no
- vpot: `ends_delight:chorus_succulent` predicted no, probe yes
- planter: `ends_delight:chorus_succulent` predicted yes, probe no
- vpot: `farmersdelight:brown_mushroom_colony` predicted no, probe yes
- vpot: `farmersdelight:cabbage_seeds` predicted no, probe yes
- vpot: `farmersdelight:onion` predicted no, probe yes
- vpot: `farmersdelight:red_mushroom_colony` predicted no, probe yes
- vpot: `farmersdelight:rice` predicted no, probe yes
- planter: `farmersdelight:rice` predicted yes, probe no
- vpot: `farmersdelight:sandy_shrub` predicted no, probe yes
- vpot: `farmersdelight:tomato_seeds` predicted no, probe yes
- vpot: `farmersdelight:wild_beetroots` predicted no, probe yes
- vbox: `farmersdelight:wild_beetroots` predicted no, probe yes
- vpot: `farmersdelight:wild_cabbages` predicted no, probe yes
- vbox: `farmersdelight:wild_cabbages` predicted no, probe yes
- vpot: `farmersdelight:wild_carrots` predicted no, probe yes
- vbox: `farmersdelight:wild_carrots` predicted no, probe yes
- vpot: `farmersdelight:wild_onions` predicted no, probe yes
- vbox: `farmersdelight:wild_onions` predicted no, probe yes
- vpot: `farmersdelight:wild_potatoes` predicted no, probe yes
- vbox: `farmersdelight:wild_potatoes` predicted no, probe yes
- vpot: `farmersdelight:wild_rice` predicted no, probe yes
- planter: `farmersdelight:wild_rice` predicted yes, probe no
- vpot: `farmersdelight:wild_tomatoes` predicted no, probe yes
- vbox: `farmersdelight:wild_tomatoes` predicted no, probe yes
- hanging: `farmersrespite:coffee_beans` predicted yes, probe no
- vpot: `farmersrespite:coffee_beans` predicted no, probe yes
- planter: `farmersrespite:coffee_beans` predicted yes, probe no
- hanging: `farmersrespite:tea_seeds` predicted yes, probe no
- vpot: `farmersrespite:tea_seeds` predicted no, probe yes
- planter: `farmersrespite:tea_seeds` predicted yes, probe no
- hanging: `farmersrespite:wild_coffee_bush` predicted yes, probe no
- vpot: `farmersrespite:wild_coffee_bush` predicted no, probe yes
- planter: `farmersrespite:wild_coffee_bush` predicted yes, probe no
- hanging: `farmersrespite:wild_tea_bush` predicted yes, probe no
- vpot: `farmersrespite:wild_tea_bush` predicted no, probe yes
- vpot: `fireflybushport:firefly_bush` predicted no, probe yes
- vpot: `floralench:allium_bush` predicted no, probe yes
- vpot: `floralench:azure_bluet_bush` predicted no, probe yes
- vpot: `floralench:blue_orchid_bush` predicted no, probe yes
- vpot: `floralench:cornflower_bush` predicted no, probe yes
- vpot: `floralench:dandelion_bush` predicted no, probe yes
- vpot: `floralench:lilac_bush` predicted no, probe yes
- vpot: `floralench:lily_of_the_valley_bush` predicted no, probe yes
- vpot: `floralench:orange_tulip_bush` predicted no, probe yes
- vpot: `floralench:oxeye_daisy_bush` predicted no, probe yes
- vpot: `floralench:peony_bush` predicted no, probe yes
- vpot: `floralench:pink_tulip_bush` predicted no, probe yes
- vpot: `floralench:poppy_bush` predicted no, probe yes
- vpot: `floralench:red_tulip_bush` predicted no, probe yes
- vpot: `floralench:rose_bush_bush` predicted no, probe yes
- vpot: `floralench:sunflower_bush` predicted no, probe yes
- vpot: `floralench:white_tulip_bush` predicted no, probe yes
- vpot: `floralench:wither_rose_bush` predicted no, probe yes
- hanging: `fruitsdelight:apple_sapling` predicted yes, probe no
- vpot: `fruitsdelight:apple_sapling` predicted no, probe yes
- hanging: `fruitsdelight:bayberry_sapling` predicted yes, probe no
- vpot: `fruitsdelight:bayberry_sapling` predicted no, probe yes
- vbox: `fruitsdelight:bayberry_sapling` predicted no, probe yes
- vpot: `fruitsdelight:blueberry` predicted no, probe yes
- vpot: `fruitsdelight:blueberry_bush` predicted no, probe yes
- vpot: `fruitsdelight:cranberry` predicted no, probe yes
- hanging: `fruitsdelight:durian_sapling` predicted yes, probe no
- vpot: `fruitsdelight:durian_sapling` predicted no, probe yes
- vbox: `fruitsdelight:durian_sapling` predicted no, probe yes
- hanging: `fruitsdelight:fig_sapling` predicted yes, probe no
- vpot: `fruitsdelight:fig_sapling` predicted no, probe yes
- vbox: `fruitsdelight:fig_sapling` predicted no, probe yes
- framed: `fruitsdelight:hamimelon` predicted no, probe yes
- vpot: `fruitsdelight:hamimelon` predicted no, probe yes
- vpot: `fruitsdelight:hamimelon_seeds` predicted no, probe yes
- hanging: `fruitsdelight:hawberry_sapling` predicted yes, probe no
- vpot: `fruitsdelight:hawberry_sapling` predicted no, probe yes
- vbox: `fruitsdelight:hawberry_sapling` predicted no, probe yes
- hanging: `fruitsdelight:kiwi_sapling` predicted yes, probe no
- vpot: `fruitsdelight:kiwi_sapling` predicted no, probe yes
- vbox: `fruitsdelight:kiwi_sapling` predicted no, probe yes
- vpot: `fruitsdelight:lemon_seeds` predicted no, probe yes
- vpot: `fruitsdelight:lemon_tree` predicted no, probe yes
- hanging: `fruitsdelight:lychee_sapling` predicted yes, probe no
- vpot: `fruitsdelight:lychee_sapling` predicted no, probe yes
- vbox: `fruitsdelight:lychee_sapling` predicted no, probe yes
- hanging: `fruitsdelight:mango_sapling` predicted yes, probe no
- vpot: `fruitsdelight:mango_sapling` predicted no, probe yes
- vbox: `fruitsdelight:mango_sapling` predicted no, probe yes
- hanging: `fruitsdelight:mangosteen_sapling` predicted yes, probe no
- vpot: `fruitsdelight:mangosteen_sapling` predicted no, probe yes
- vbox: `fruitsdelight:mangosteen_sapling` predicted no, probe yes
- hanging: `fruitsdelight:orange_sapling` predicted yes, probe no
- vpot: `fruitsdelight:orange_sapling` predicted no, probe yes
- vbox: `fruitsdelight:orange_sapling` predicted no, probe yes
- hanging: `fruitsdelight:peach_sapling` predicted yes, probe no
- vpot: `fruitsdelight:peach_sapling` predicted no, probe yes
- vbox: `fruitsdelight:peach_sapling` predicted no, probe yes
- hanging: `fruitsdelight:pear_sapling` predicted yes, probe no
- vpot: `fruitsdelight:pear_sapling` predicted no, probe yes
- vbox: `fruitsdelight:pear_sapling` predicted no, probe yes
- hanging: `fruitsdelight:persimmon_sapling` predicted yes, probe no
- vpot: `fruitsdelight:persimmon_sapling` predicted no, probe yes
- vbox: `fruitsdelight:persimmon_sapling` predicted no, probe yes
- vpot: `fruitsdelight:pineapple_sapling` predicted no, probe yes
- planter: `fruitsdelight:pineapple_sapling` predicted yes, probe no
- vpot: `fruitsdelight:wild_pineapple` predicted no, probe yes
- planter: `fruitsdelight:wild_pineapple` predicted yes, probe no
- vpot: `fungidelight:inky_cap_mushroom` predicted no, probe yes
- vbox: `fungidelight:inky_cap_mushroom` predicted no, probe yes
- vpot: `fungidelight:inky_cap_mushroom_colony` predicted no, probe yes
- vpot: `fungidelight:morel_mushroom` predicted no, probe yes
- vbox: `fungidelight:morel_mushroom` predicted no, probe yes
- vpot: `fungidelight:morel_mushroom_colony` predicted no, probe yes
- hanging: `hearthandharvest:blue_mum` predicted yes, probe no
- vpot: `hearthandharvest:blue_mum` predicted no, probe yes
- hanging: `hearthandharvest:light_blue_mum` predicted yes, probe no
- vpot: `hearthandharvest:light_blue_mum` predicted no, probe yes
- hanging: `hearthandharvest:orange_mum` predicted yes, probe no
- vpot: `hearthandharvest:orange_mum` predicted no, probe yes
- hanging: `hearthandharvest:pink_mum` predicted yes, probe no
- vpot: `hearthandharvest:pink_mum` predicted no, probe yes
- hanging: `hearthandharvest:purple_mum` predicted yes, probe no
- vpot: `hearthandharvest:purple_mum` predicted no, probe yes
- vpot: `hearthandharvest:raspberry_pips` predicted no, probe yes
- hanging: `hearthandharvest:red_mum` predicted yes, probe no
- vpot: `hearthandharvest:red_mum` predicted no, probe yes
- hanging: `hearthandharvest:white_mum` predicted yes, probe no
- vpot: `hearthandharvest:white_mum` predicted no, probe yes
- vpot: `hearthandharvest:wild_hops` predicted no, probe yes
- hanging: `hearthandharvest:yellow_mum` predicted yes, probe no
- vpot: `hearthandharvest:yellow_mum` predicted no, probe yes
- vpot: `immersive_weathering:frosty_fern` predicted no, probe yes
- vpot: `immersive_weathering:frosty_grass` predicted no, probe yes
- vpot: `immersive_weathering:weeds` predicted no, probe yes
- planter: `immersive_weathering:weeds` predicted yes, probe no
- hanging: `immersiveengineering:seed` predicted yes, probe no
- vpot: `immersiveengineering:seed` predicted no, probe yes
- vpot: `miners_delight:cave_carrot` predicted no, probe yes
- hanging: `miners_delight:gossypium` predicted yes, probe no
- vpot: `miners_delight:gossypium` predicted no, probe yes
- vpot: `miners_delight:wild_cave_carrots` predicted no, probe yes
- vpot: `myrtrees:rubberwood_sapling` predicted no, probe yes
- vbox: `myrtrees:rubberwood_sapling` predicted no, probe yes
- hanging: `neapolitan:banana_frond` predicted yes, probe no
- vpot: `neapolitan:banana_frond` predicted no, probe yes
- planter: `neapolitan:banana_frond` predicted yes, probe no
- hanging: `neapolitan:mint_sprout` predicted yes, probe no
- vpot: `neapolitan:mint_sprout` predicted no, probe yes
- vpot: `neapolitan:strawberry_pips` predicted no, probe yes
- vpot: `nethersdelight:crimson_fungus_colony` predicted no, probe yes
- planter: `nethersdelight:crimson_fungus_colony` predicted yes, probe no
- hanging: `nethersdelight:mimicarnation` predicted yes, probe no
- vpot: `nethersdelight:mimicarnation` predicted no, probe yes
- vpot: `nethersdelight:propelplant_cane` predicted no, probe yes
- planter: `nethersdelight:propelplant_cane` predicted yes, probe no
- vpot: `nethersdelight:warped_fungus_colony` predicted no, probe yes
- planter: `nethersdelight:warped_fungus_colony` predicted yes, probe no
- hanging: `quark:ancient_sapling` predicted yes, probe no
- vpot: `quark:ancient_sapling` predicted no, probe yes
- vbox: `quark:ancient_sapling` predicted no, probe yes
- hanging: `quark:blue_blossom_sapling` predicted yes, probe no
- vpot: `quark:blue_blossom_sapling` predicted no, probe yes
- vbox: `quark:blue_blossom_sapling` predicted no, probe yes
- hanging: `quark:glow_lichen_growth` predicted yes, probe no
- vpot: `quark:glow_lichen_growth` predicted no, probe yes
- vbox: `quark:glow_lichen_growth` predicted no, probe yes
- hanging: `quark:glow_shroom` predicted yes, probe no
- vpot: `quark:glow_shroom` predicted no, probe yes
- vbox: `quark:glow_shroom` predicted no, probe yes
- hanging: `quark:lavender_blossom_sapling` predicted yes, probe no
- vpot: `quark:lavender_blossom_sapling` predicted no, probe yes
- vbox: `quark:lavender_blossom_sapling` predicted no, probe yes
- hanging: `quark:orange_blossom_sapling` predicted yes, probe no
- vpot: `quark:orange_blossom_sapling` predicted no, probe yes
- vbox: `quark:orange_blossom_sapling` predicted no, probe yes
- hanging: `quark:pink_blossom_sapling` predicted yes, probe no
- vpot: `quark:pink_blossom_sapling` predicted no, probe yes
- vbox: `quark:pink_blossom_sapling` predicted no, probe yes
- hanging: `quark:red_blossom_sapling` predicted yes, probe no
- vpot: `quark:red_blossom_sapling` predicted no, probe yes
- vbox: `quark:red_blossom_sapling` predicted no, probe yes
- hanging: `quark:yellow_blossom_sapling` predicted yes, probe no
- vpot: `quark:yellow_blossom_sapling` predicted no, probe yes
- vbox: `quark:yellow_blossom_sapling` predicted no, probe yes
- vpot: `rusticdelight:bell_pepper_seeds` predicted no, probe yes
- vpot: `rusticdelight:coffee_beans` predicted no, probe yes
- vpot: `rusticdelight:cotton_seeds` predicted no, probe yes
- vpot: `rusticdelight:dark_bell_pepper_seeds` predicted no, probe yes
- vpot: `rusticdelight:pale_bell_pepper_seeds` predicted no, probe yes
- hanging: `rusticdelight:wild_bell_peppers` predicted yes, probe no
- vpot: `rusticdelight:wild_bell_peppers` predicted no, probe yes
- vbox: `rusticdelight:wild_bell_peppers` predicted no, probe yes
- hanging: `rusticdelight:wild_coffee` predicted yes, probe no
- vpot: `rusticdelight:wild_coffee` predicted no, probe yes
- vbox: `rusticdelight:wild_coffee` predicted no, probe yes
- hanging: `rusticdelight:wild_cotton` predicted yes, probe no
- vpot: `rusticdelight:wild_cotton` predicted no, probe yes
- vbox: `rusticdelight:wild_cotton` predicted no, probe yes
- hanging: `rusticdelight:wild_dark_bell_peppers` predicted yes, probe no
- vpot: `rusticdelight:wild_dark_bell_peppers` predicted no, probe yes
- vbox: `rusticdelight:wild_dark_bell_peppers` predicted no, probe yes
- hanging: `rusticdelight:wild_pale_bell_peppers` predicted yes, probe no
- vpot: `rusticdelight:wild_pale_bell_peppers` predicted no, probe yes
- vbox: `rusticdelight:wild_pale_bell_peppers` predicted no, probe yes
- hanging: `supplementaries:flax_seeds` predicted yes, probe no
- vpot: `supplementaries:flax_seeds` predicted no, probe yes
- vpot: `supplementaries:wild_flax` predicted no, probe yes
- hanging: `twigs:azalea_flowers` predicted yes, probe no
- vpot: `twigs:bamboo_leaves` predicted no, probe yes
- vpot: `upgrade_aquatic:beachgrass` predicted no, probe yes
- planter: `upgrade_aquatic:beachgrass` predicted yes, probe no
- planter: `upgrade_aquatic:flowering_rush` predicted yes, probe no
- hanging: `upgrade_aquatic:pink_searocket` predicted yes, probe no
- vpot: `upgrade_aquatic:pink_searocket` predicted no, probe yes
- hanging: `upgrade_aquatic:river_sapling` predicted yes, probe no
- vpot: `upgrade_aquatic:river_sapling` predicted no, probe yes
- vbox: `upgrade_aquatic:river_sapling` predicted no, probe yes
- vpot: `upgrade_aquatic:tall_beachgrass` predicted no, probe yes
- planter: `upgrade_aquatic:tall_beachgrass` predicted yes, probe no
- hanging: `upgrade_aquatic:white_searocket` predicted yes, probe no
- vpot: `upgrade_aquatic:white_searocket` predicted no, probe yes
- vpot: `veggiesdelight:broccoli_seeds` predicted no, probe yes
- vpot: `veggiesdelight:cauliflower_seeds` predicted no, probe yes
- vpot: `veggiesdelight:garlic_clove` predicted no, probe yes
- vpot: `veggiesdelight:mature_dandelion` predicted no, probe yes
- vpot: `veggiesdelight:sweet_potato` predicted no, probe yes
- vpot: `veggiesdelight:turnip_seeds` predicted no, probe yes
- vpot: `veggiesdelight:wild_broccoli` predicted no, probe yes
- vbox: `veggiesdelight:wild_broccoli` predicted no, probe yes
- vpot: `veggiesdelight:wild_cauliflowers` predicted no, probe yes
- vbox: `veggiesdelight:wild_cauliflowers` predicted no, probe yes
- vpot: `veggiesdelight:wild_garlic` predicted no, probe yes
- vbox: `veggiesdelight:wild_garlic` predicted no, probe yes
- vpot: `veggiesdelight:wild_sweet_potatoes` predicted no, probe yes
- vbox: `veggiesdelight:wild_sweet_potatoes` predicted no, probe yes
- vpot: `veggiesdelight:wild_turnips` predicted no, probe yes
- vbox: `veggiesdelight:wild_turnips` predicted no, probe yes
- vpot: `veggiesdelight:wild_zucchinis` predicted no, probe yes
- vbox: `veggiesdelight:wild_zucchinis` predicted no, probe yes
- vpot: `veggiesdelight:zucchini_seeds` predicted no, probe yes
- vpot: `vinery:apple_tree_sapling` predicted no, probe yes
- vbox: `vinery:apple_tree_sapling` predicted no, probe yes
- vpot: `vinery:cherry_sapling` predicted no, probe yes
- vbox: `vinery:cherry_sapling` predicted no, probe yes
- vpot: `vinery:red_grape_seeds` predicted no, probe yes
- planter: `vinery:red_grape_seeds` predicted yes, probe no
- vpot: `vinery:savanna_grape_seeds_red` predicted no, probe yes
- planter: `vinery:savanna_grape_seeds_red` predicted yes, probe no
- vpot: `vinery:savanna_grape_seeds_white` predicted no, probe yes
- planter: `vinery:savanna_grape_seeds_white` predicted yes, probe no
- vpot: `vinery:taiga_grape_seeds_red` predicted no, probe yes
- planter: `vinery:taiga_grape_seeds_red` predicted yes, probe no
- vpot: `vinery:taiga_grape_seeds_white` predicted no, probe yes
- planter: `vinery:taiga_grape_seeds_white` predicted yes, probe no
- vpot: `vinery:white_grape_seeds` predicted no, probe yes
- planter: `vinery:white_grape_seeds` predicted yes, probe no
- vpot: `vintagedelight:cucumber_seeds` predicted no, probe yes
- vpot: `vintagedelight:ghost_pepper` predicted no, probe yes
- planter: `vintagedelight:ghost_pepper` predicted yes, probe no
- vpot: `vintagedelight:ghost_pepper_seeds` predicted no, probe yes
- planter: `vintagedelight:ghost_pepper_seeds` predicted yes, probe no
- vpot: `vintagedelight:magic_peanut` predicted no, probe yes
- vbox: `vintagedelight:magic_peanut` predicted no, probe yes
- vpot: `vintagedelight:oat_seeds` predicted no, probe yes
- vpot: `vintagedelight:peanut` predicted no, probe yes
- vpot: `vintagedelight:wild_cucumbers` predicted no, probe yes
- vbox: `vintagedelight:wild_cucumbers` predicted no, probe yes
- vpot: `vintagedelight:wild_ghost_peppers` predicted no, probe yes
- vbox: `vintagedelight:wild_ghost_peppers` predicted no, probe yes
- silt: `vintagedelight:wild_ghost_peppers` predicted yes, probe no
- planter: `vintagedelight:wild_ghost_peppers` predicted yes, probe no
- vpot: `vintagedelight:wild_oats` predicted no, probe yes
- vbox: `vintagedelight:wild_oats` predicted no, probe yes
- vpot: `vintagedelight:wild_peanuts` predicted no, probe yes
- vbox: `vintagedelight:wild_peanuts` predicted no, probe yes

## Probe exceptions

- none

## Full matrix

### minecraft (90)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `acacia_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `allium` | flower | Y | Y |  | Y | Y | Y | Y | Y | Y | Y | Y |
| `azalea` | sapling | Y | Y |  |  |  |  |  |  |  |  | Y |
| `azure_bluet` | flower | Y | Y |  | Y | Y | Y | Y | Y | Y | Y | Y |
| `bamboo` | cactus / cane | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `beetroot_seeds` | crop / seed | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `big_dripleaf` | other |  |  |  |  |  |  |  |  |  |  |  |
| `birch_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `blue_orchid` | flower | Y | Y |  | Y | Y | Y | Y | Y | Y | Y | Y |
| `brain_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `brain_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `brown_mushroom` | mushroom | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `bubble_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `bubble_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `cactus` | cactus / cane | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `carrot` | crop / seed | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `chorus_flower` | cactus / cane | Y | Y |  | Y | Y |  |  |  | Y |  |  |
| `chorus_plant` | cactus / cane |  |  |  |  |  |  |  |  |  |  |  |
| `cocoa_beans` | crop / seed | Y | Y |  | Y | Y |  |  |  | Y |  |  |
| `cornflower` | flower | Y | Y |  | Y | Y | Y | Y | Y | Y | Y | Y |
| `crimson_fungus` | mushroom | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `crimson_roots` | grass / fern | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `dandelion` | flower | Y | Y |  | Y | Y | Y | Y | Y | Y | Y | Y |
| `dark_oak_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `dead_brain_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_brain_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_bubble_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_bubble_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_bush` | grass / fern | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `dead_fire_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_fire_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_horn_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_horn_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_tube_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_tube_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `fern` | grass / fern | Y | Y |  | Y | Y | Y |  | Y | Y |  | Y |
| `fire_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `fire_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `flowering_azalea` | flower | Y | Y |  |  |  |  |  |  |  |  | Y |
| `flowering_azalea_leaves` | flower |  | Y |  | Y | Y |  |  |  | Y |  | Y |
| `glow_berries` | vine | Y | Y |  | Y | Y | Y |  |  | Y |  |  |
| `glow_lichen` | vine |  |  |  | Y | Y | Y |  |  | Y |  |  |
| `grass` | grass / fern | Y | Y |  | Y | Y | Y |  | Y | Y |  | Y |
| `hanging_roots` | grass / fern |  |  |  |  |  |  |  |  |  |  |  |
| `horn_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `horn_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `jungle_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `kelp` | water |  |  |  |  |  |  |  |  |  |  |  |
| `large_fern` | grass / fern | Y | Y |  | Y |  |  |  | Y |  |  | Y |
| `lilac` | tall flower | Y | Y |  | Y | Y | Y | Y | Y | Y |  | Y |
| `lily_of_the_valley` | flower | Y | Y |  | Y | Y | Y | Y | Y | Y | Y | Y |
| `lily_pad` | water |  |  |  |  |  |  |  | Y |  |  |  |
| `mangrove_propagule` | flower | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `melon` | crop / seed |  | Y |  |  |  |  |  | Y |  |  | Y |
| `melon_seeds` | crop / seed | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `moss_block` | other |  | Y |  |  |  |  |  |  |  |  | Y |
| `nether_sprouts` | grass / fern | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `nether_wart` | crop / seed | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `oak_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `orange_tulip` | flower | Y | Y |  | Y | Y | Y | Y | Y | Y | Y | Y |
| `oxeye_daisy` | flower | Y | Y |  | Y | Y | Y | Y | Y | Y | Y | Y |
| `peony` | tall flower | Y | Y |  | Y | Y | Y | Y | Y | Y |  | Y |
| `pink_tulip` | flower | Y | Y |  | Y | Y | Y | Y | Y | Y | Y | Y |
| `poppy` | flower | Y | Y |  | Y | Y | Y | Y | Y | Y | Y | Y |
| `potato` | crop / seed | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `pumpkin` | crop / seed |  | Y |  |  |  |  |  | Y |  |  | Y |
| `pumpkin_seeds` | crop / seed | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `red_mushroom` | mushroom | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `red_tulip` | flower | Y | Y |  | Y | Y | Y | Y | Y | Y | Y | Y |
| `rose_bush` | tall flower | Y | Y |  | Y | Y | Y | Y | Y | Y |  | Y |
| `sculk_vein` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `sea_pickle` | water | Y | Y |  | Y | Y |  |  | Y | Y |  |  |
| `seagrass` | water |  |  |  |  |  |  |  | Y |  |  |  |
| `small_dripleaf` | grass / fern |  |  |  |  |  |  |  | Y |  |  |  |
| `spore_blossom` | other |  |  |  |  |  |  |  |  |  |  |  |
| `spruce_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `sugar_cane` | cactus / cane | Y | Y |  | Y |  |  |  | Y |  |  | Y |
| `sunflower` | tall flower | Y | Y |  | Y | Y | Y | Y | Y | Y |  | Y |
| `sweet_berries` | crop / seed | Y | Y |  | Y |  | Y |  | Y |  |  |  |
| `tall_grass` | grass / fern | Y | Y |  | Y |  |  |  | Y |  |  | Y |
| `tube_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `tube_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `twisting_vines` | vine | Y | Y |  | Y |  | Y |  |  |  |  |  |
| `vine` | vine | Y | Y |  | Y |  | Y |  |  |  |  |  |
| `warped_fungus` | mushroom | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `warped_roots` | grass / fern | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `weeping_vines` | vine | Y | Y |  | Y |  | Y |  |  |  |  |  |
| `wheat_seeds` | crop / seed | Y | Y |  | Y |  |  |  | Y |  |  | Y |
| `white_tulip` | flower | Y | Y |  | Y | Y | Y | Y | Y | Y | Y | Y |
| `wither_rose` | flower | Y | Y |  | Y | Y | Y | Y | Y | Y | Y | Y |

### alexsmobs (2)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `banana_peel` | other |  |  |  |  |  |  |  | Y |  |  | Y |
| `stink_bottle` | vine |  |  |  |  |  |  |  |  |  |  |  |

### architects_palette (1)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `twisted_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |

### berry_good (2)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `glow_berry_pips` | vine | Y | Y |  |  |  |  |  |  |  |  |  |
| `sweet_berry_pips` | crop / seed | Y | Y |  |  |  |  |  | Y |  |  | Y |

### biomesoplenty (3)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `eyebulb` | grass / fern |  |  |  |  |  |  |  | Y |  |  |  |
| `flesh_tendons` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `flesh_tendons_strand` | vine |  |  |  |  |  |  |  |  |  |  |  |

### botania (203)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `agricarnation` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `agricarnation_chibi` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `bellethorn` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `bellethorn_chibi` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `bergamute` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `black_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `black_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `black_mushroom` | botania |  |  |  | Y | Y |  |  | Y | Y |  | Y |
| `black_mystical_flower` | botania |  |  |  | Y | Y |  |  | Y | Y | Y | Y |
| `black_petal` | botania |  |  |  |  |  |  |  | Y |  |  | Y |
| `black_shiny_flower` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `blue_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `blue_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `blue_mushroom` | botania |  |  |  | Y | Y |  |  | Y | Y |  | Y |
| `blue_mystical_flower` | botania |  |  |  | Y | Y |  |  | Y | Y | Y | Y |
| `blue_petal` | botania |  |  |  |  |  |  |  | Y |  |  | Y |
| `blue_shiny_flower` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `brown_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `brown_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `brown_mushroom` | botania |  |  |  | Y | Y |  |  | Y | Y |  | Y |
| `brown_mystical_flower` | botania |  |  |  | Y | Y |  |  | Y | Y | Y | Y |
| `brown_petal` | botania |  |  |  |  |  |  |  | Y |  |  | Y |
| `brown_shiny_flower` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `bubbell` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `bubbell_chibi` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `clayconia` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `clayconia_chibi` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `cyan_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `cyan_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `cyan_mushroom` | botania |  |  |  | Y | Y |  |  | Y | Y |  | Y |
| `cyan_mystical_flower` | botania |  |  |  | Y | Y |  |  | Y | Y | Y | Y |
| `cyan_petal` | botania |  |  |  |  |  |  |  | Y |  |  | Y |
| `cyan_shiny_flower` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `daffomill` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `dandelifeon` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `daybloom_motif` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `dreadthorn` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `endoflame` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `entropinnyum` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `exoflame` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `fallen_kanade` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `floating_agricarnation` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_agricarnation_chibi` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_bellethorn` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_bellethorn_chibi` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_bergamute` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_bubbell` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_bubbell_chibi` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_clayconia` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_clayconia_chibi` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_daffomill` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_dandelifeon` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_dreadthorn` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_endoflame` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_entropinnyum` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_exoflame` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_fallen_kanade` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_gourmaryllis` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_heisei_dream` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_hopperhock` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_hopperhock_chibi` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_hyacidus` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_hydroangeas` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_jaded_amaranthus` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_jiyuulia` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_jiyuulia_chibi` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_kekimurus` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_labellia` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_loonium` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_manastar` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_marimorphosis` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_marimorphosis_chibi` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_medumone` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_munchdew` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_narslimmus` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_orechid` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_orechid_ignem` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_pollidisiac` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_pure_daisy` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_rafflowsia` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_rannuncarpus` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_rannuncarpus_chibi` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_rosa_arcana` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_shulk_me_not` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_solegnolia` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_solegnolia_chibi` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_spectranthemum` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_spectrolus` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_tangleberrie` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_tangleberrie_chibi` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_thermalily` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_tigerseye` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `floating_vinculotus` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `gourmaryllis` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `gray_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `gray_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `gray_mushroom` | botania |  |  |  | Y | Y |  |  | Y | Y |  | Y |
| `gray_mystical_flower` | botania |  |  |  | Y | Y |  |  | Y | Y | Y | Y |
| `gray_petal` | botania |  |  |  |  |  |  |  | Y |  |  | Y |
| `gray_shiny_flower` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `green_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `green_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `green_mushroom` | botania |  |  |  | Y | Y |  |  | Y | Y |  | Y |
| `green_mystical_flower` | botania |  |  |  | Y | Y |  |  | Y | Y | Y | Y |
| `green_petal` | botania |  |  |  |  |  |  |  | Y |  |  | Y |
| `green_shiny_flower` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `heisei_dream` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `hopperhock` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `hopperhock_chibi` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `hyacidus` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `hydroangeas` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `hydroangeas_motif` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `jaded_amaranthus` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `jiyuulia` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `jiyuulia_chibi` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `kekimurus` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `labellia` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `light_blue_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `light_blue_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `light_blue_mushroom` | botania |  |  |  | Y | Y |  |  | Y | Y |  | Y |
| `light_blue_mystical_flower` | botania |  |  |  | Y | Y |  |  | Y | Y | Y | Y |
| `light_blue_petal` | botania |  |  |  |  |  |  |  | Y |  |  | Y |
| `light_blue_shiny_flower` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `light_gray_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `light_gray_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `light_gray_mushroom` | botania |  |  |  | Y | Y |  |  | Y | Y |  | Y |
| `light_gray_mystical_flower` | botania |  |  |  | Y | Y |  |  | Y | Y | Y | Y |
| `light_gray_petal` | botania |  |  |  |  |  |  |  | Y |  |  | Y |
| `light_gray_shiny_flower` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `lime_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `lime_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `lime_mushroom` | botania |  |  |  | Y | Y |  |  | Y | Y |  | Y |
| `lime_mystical_flower` | botania |  |  |  | Y | Y |  |  | Y | Y | Y | Y |
| `lime_petal` | botania |  |  |  |  |  |  |  | Y |  |  | Y |
| `lime_shiny_flower` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `loonium` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `magenta_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `magenta_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `magenta_mushroom` | botania |  |  |  | Y | Y |  |  | Y | Y |  | Y |
| `magenta_mystical_flower` | botania |  |  |  | Y | Y |  |  | Y | Y | Y | Y |
| `magenta_petal` | botania |  |  |  |  |  |  |  | Y |  |  | Y |
| `magenta_shiny_flower` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `manastar` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `marimorphosis` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `marimorphosis_chibi` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `medumone` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `munchdew` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `narslimmus` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `nightshade_motif` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `orange_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `orange_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `orange_mushroom` | botania |  |  |  | Y | Y |  |  | Y | Y |  | Y |
| `orange_mystical_flower` | botania |  |  |  | Y | Y |  |  | Y | Y | Y | Y |
| `orange_petal` | botania |  |  |  |  |  |  |  | Y |  |  | Y |
| `orange_shiny_flower` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `orechid` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `orechid_ignem` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `pink_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `pink_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `pink_mushroom` | botania |  |  |  | Y | Y |  |  | Y | Y |  | Y |
| `pink_mystical_flower` | botania |  |  |  | Y | Y |  |  | Y | Y | Y | Y |
| `pink_petal` | botania |  |  |  |  |  |  |  | Y |  |  | Y |
| `pink_shiny_flower` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `pollidisiac` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `pure_daisy` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `purple_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `purple_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `purple_mushroom` | botania |  |  |  | Y | Y |  |  | Y | Y |  | Y |
| `purple_mystical_flower` | botania |  |  |  | Y | Y |  |  | Y | Y | Y | Y |
| `purple_petal` | botania |  |  |  |  |  |  |  | Y |  |  | Y |
| `purple_shiny_flower` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `rafflowsia` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `rannuncarpus` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `rannuncarpus_chibi` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `red_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `red_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `red_mushroom` | botania |  |  |  | Y | Y |  |  | Y | Y |  | Y |
| `red_mystical_flower` | botania |  |  |  | Y | Y |  |  | Y | Y | Y | Y |
| `red_petal` | botania |  |  |  |  |  |  |  | Y |  |  | Y |
| `red_shiny_flower` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `rosa_arcana` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `shulk_me_not` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `solegnolia` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `solegnolia_chibi` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `spectranthemum` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `spectrolus` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `tangleberrie` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `tangleberrie_chibi` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `thermalily` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `tigerseye` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `vinculotus` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `white_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `white_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `white_mushroom` | botania |  |  |  | Y | Y |  |  | Y | Y |  | Y |
| `white_mystical_flower` | botania |  |  |  | Y | Y |  |  | Y | Y | Y | Y |
| `white_petal` | botania |  |  |  |  |  |  |  | Y |  |  | Y |
| `white_shiny_flower` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |
| `yellow_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `yellow_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `yellow_mushroom` | botania |  |  |  | Y | Y |  |  | Y | Y |  | Y |
| `yellow_mystical_flower` | botania |  |  |  | Y | Y |  |  | Y | Y | Y | Y |
| `yellow_petal` | botania |  |  |  |  |  |  |  | Y |  |  | Y |
| `yellow_shiny_flower` | botania |  |  |  |  |  |  |  | Y |  | Y | Y |

### bushierflowers (13)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `grown_allium` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |
| `grown_azure_bluet` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |
| `grown_blue_orchid` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |
| `grown_cornflower` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |
| `grown_dandelion` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |
| `grown_lily` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |
| `grown_orange_tulip` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |
| `grown_oxeye_daisy` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |
| `grown_pink_tulip` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |
| `grown_poppy` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |
| `grown_red_tulip` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |
| `grown_white_tulip` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |
| `grown_wither_rose` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |

### buzzier_bees (3)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `buttercup` | flower | Y | Y |  |  |  |  |  | Y |  | Y | Y |
| `pink_clover` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |
| `white_clover` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |

### corn_delight (2)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `corn_seeds` | crop / seed |  |  |  |  |  |  |  | Y |  |  | Y |
| `wild_corn` | flower |  |  |  |  |  |  |  | Y |  | Y | Y |

### delightful (5)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `cantaloupe` | crop / seed |  |  |  |  |  |  |  | Y |  |  | Y |
| `cantaloupe_seeds` | crop / seed |  |  |  |  |  |  |  | Y |  |  | Y |
| `mini_melon` | crop / seed |  |  |  |  |  |  |  | Y |  |  | Y |
| `salmonberry_pips` | other |  |  |  |  |  |  |  | Y |  |  | Y |
| `wild_salmonberries` | flower |  |  |  |  | Y |  |  | Y | Y | Y | Y |

### endercrop (1)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `ender_seeds` | crop / seed |  |  |  |  |  |  |  | Y |  |  |  |

### endersdelight (3)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `amberveil` | other |  |  |  |  |  |  |  | Y |  |  |  |
| `chorusflame` | cactus / cane |  |  |  |  |  |  |  | Y |  |  |  |
| `ethereal_saffron` | other |  |  |  |  |  |  |  | Y |  |  |  |

### ends_delight (1)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `chorus_succulent` | cactus / cane |  |  |  |  |  |  |  | Y |  |  |  |

### farmersdelight (14)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `brown_mushroom_colony` | mushroom |  |  |  | Y |  |  |  | Y |  |  | Y |
| `cabbage_seeds` | crop / seed |  |  |  |  |  |  |  | Y |  |  | Y |
| `onion` | crop / seed |  |  |  |  |  |  |  | Y |  |  | Y |
| `red_mushroom_colony` | mushroom |  |  |  | Y |  |  |  | Y |  |  | Y |
| `rice` | other |  |  |  |  |  |  |  | Y |  |  |  |
| `sandy_shrub` | other |  |  |  |  |  |  |  | Y |  |  | Y |
| `tomato_seeds` | crop / seed |  |  |  |  |  |  |  | Y |  |  | Y |
| `wild_beetroots` | flower |  |  |  |  | Y |  |  | Y | Y | Y | Y |
| `wild_cabbages` | flower |  |  |  |  | Y |  |  | Y | Y | Y | Y |
| `wild_carrots` | flower |  |  |  |  | Y |  |  | Y | Y | Y | Y |
| `wild_onions` | flower |  |  |  |  | Y |  |  | Y | Y | Y | Y |
| `wild_potatoes` | flower |  |  |  |  | Y |  |  | Y | Y | Y | Y |
| `wild_rice` | grass / fern |  |  |  |  |  |  |  | Y |  |  |  |
| `wild_tomatoes` | flower |  |  |  |  | Y |  |  | Y | Y | Y | Y |

### farmersrespite (4)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `coffee_beans` | crop / seed | Y | Y |  |  |  |  |  | Y |  |  |  |
| `tea_seeds` | crop / seed | Y | Y |  |  |  |  |  | Y |  |  |  |
| `wild_coffee_bush` | grass / fern | Y | Y |  |  |  |  |  | Y |  |  |  |
| `wild_tea_bush` | grass / fern | Y | Y |  |  |  |  |  | Y |  |  | Y |

### fireflybushport (1)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `firefly_bush` | flower |  |  |  |  |  |  |  | Y |  | Y | Y |

### floralench (34)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `allium_bush` | vine |  |  |  |  |  |  |  | Y |  |  | Y |
| `allium_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `azure_bluet_bush` | vine |  |  |  |  |  |  |  | Y |  |  | Y |
| `azure_bluet_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `blue_orchid_bush` | vine |  |  |  |  |  |  |  | Y |  |  | Y |
| `blue_orchid_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `cornflower_bush` | vine |  |  |  |  |  |  |  | Y |  |  | Y |
| `cornflower_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `dandelion_bush` | vine |  |  |  |  |  |  |  | Y |  |  | Y |
| `dandelion_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `lilac_bush` | vine |  |  |  |  |  |  |  | Y |  |  | Y |
| `lilac_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `lily_of_the_valley_bush` | vine |  |  |  |  |  |  |  | Y |  |  | Y |
| `lily_of_the_valley_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `orange_tulip_bush` | vine |  |  |  |  |  |  |  | Y |  |  | Y |
| `orange_tulip_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `oxeye_daisy_bush` | vine |  |  |  |  |  |  |  | Y |  |  | Y |
| `oxeye_daisy_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `peony_bush` | vine |  |  |  |  |  |  |  | Y |  |  | Y |
| `peony_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `pink_tulip_bush` | vine |  |  |  |  |  |  |  | Y |  |  | Y |
| `pink_tulip_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `poppy_bush` | vine |  |  |  |  |  |  |  | Y |  |  | Y |
| `poppy_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `red_tulip_bush` | vine |  |  |  |  |  |  |  | Y |  |  | Y |
| `red_tulip_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `rose_bush_bush` | vine |  |  |  |  |  |  |  | Y |  |  | Y |
| `rose_bush_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `sunflower_bush` | vine |  |  |  |  |  |  |  | Y |  |  | Y |
| `sunflower_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `white_tulip_bush` | vine |  |  |  |  |  |  |  | Y |  |  | Y |
| `white_tulip_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `wither_rose_bush` | vine |  |  |  |  |  |  |  | Y |  |  | Y |
| `wither_rose_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |

### fruitsdelight (22)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `apple_sapling` | sapling | Y | Y |  |  |  |  |  | Y |  |  | Y |
| `bayberry_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `blueberry` | other |  |  |  |  |  |  |  | Y |  |  | Y |
| `blueberry_bush` | other |  |  |  |  |  |  |  | Y |  |  | Y |
| `cranberry` | other |  |  |  |  |  |  |  | Y |  |  | Y |
| `durian_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `fig_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `hamimelon` | crop / seed |  | Y |  |  |  |  |  | Y |  |  | Y |
| `hamimelon_seeds` | crop / seed |  |  |  |  |  |  |  | Y |  |  | Y |
| `hawberry_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `kiwi_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `lemon_seeds` | grass / fern |  |  |  |  |  |  |  | Y |  |  | Y |
| `lemon_tree` | grass / fern |  |  |  |  |  |  |  | Y |  |  | Y |
| `lychee_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `mango_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `mangosteen_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `orange_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `peach_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `pear_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `persimmon_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `pineapple_sapling` | other |  |  |  |  |  |  |  | Y |  |  |  |
| `wild_pineapple` | other |  |  |  |  |  |  |  | Y |  |  |  |

### fungidelight (4)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `inky_cap_mushroom` | mushroom |  |  |  | Y | Y |  |  | Y | Y |  | Y |
| `inky_cap_mushroom_colony` | mushroom |  |  |  | Y | Y |  |  | Y |  |  | Y |
| `morel_mushroom` | mushroom |  |  |  | Y | Y |  |  | Y | Y |  | Y |
| `morel_mushroom_colony` | mushroom |  |  |  | Y | Y |  |  | Y |  |  | Y |

### hearthandharvest (11)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `blue_mum` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |
| `hops` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `light_blue_mum` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |
| `orange_mum` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |
| `pink_mum` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |
| `purple_mum` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |
| `raspberry_pips` | crop / seed |  |  |  |  |  |  |  | Y |  |  | Y |
| `red_mum` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |
| `white_mum` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |
| `wild_hops` | flower |  |  |  |  |  |  |  | Y |  | Y | Y |
| `yellow_mum` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |

### immersive_weathering (7)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `frost` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `frosty_fern` | grass / fern |  |  |  |  |  |  |  | Y |  |  | Y |
| `frosty_grass` | grass / fern |  |  |  |  |  |  |  | Y |  |  | Y |
| `ivy` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `moss_clump` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `soot` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `weeds` | crop / seed |  |  |  |  |  |  |  | Y |  |  |  |

### immersiveengineering (1)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `seed` | crop / seed | Y | Y |  |  |  |  |  | Y |  |  | Y |

### miners_delight (3)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `cave_carrot` | crop / seed |  |  |  |  |  |  |  | Y |  |  | Y |
| `gossypium` | flower | Y | Y |  |  |  |  |  | Y |  | Y | Y |
| `wild_cave_carrots` | flower |  |  |  |  |  |  |  | Y |  | Y | Y |

### myrtrees (1)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `rubberwood_sapling` | sapling |  |  |  | Y | Y |  |  | Y | Y |  | Y |

### neapolitan (3)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `banana_frond` | other | Y | Y |  | Y |  |  |  | Y |  |  |  |
| `mint_sprout` | other | Y | Y |  | Y |  |  |  | Y |  |  | Y |
| `strawberry_pips` | other |  |  |  |  |  |  |  | Y |  |  | Y |

### nethersdelight (4)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `crimson_fungus_colony` | mushroom |  |  |  |  |  |  |  | Y |  |  |  |
| `mimicarnation` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |
| `propelplant_cane` | other |  |  |  |  |  |  |  | Y |  |  |  |
| `warped_fungus_colony` | mushroom |  |  |  |  |  |  |  | Y |  |  |  |

### quark (10)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `ancient_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `blue_blossom_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `cut_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `glow_lichen_growth` | vine | Y | Y |  |  | Y |  |  | Y | Y |  | Y |
| `glow_shroom` | mushroom | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `lavender_blossom_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `orange_blossom_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `pink_blossom_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `red_blossom_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `yellow_blossom_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |

### rusticdelight (10)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `bell_pepper_seeds` | crop / seed |  |  |  |  |  |  |  | Y |  |  | Y |
| `coffee_beans` | crop / seed |  |  |  |  |  |  |  | Y |  |  | Y |
| `cotton_seeds` | crop / seed |  |  |  |  |  |  |  | Y |  |  | Y |
| `dark_bell_pepper_seeds` | crop / seed |  |  |  |  |  |  |  | Y |  |  | Y |
| `pale_bell_pepper_seeds` | crop / seed |  |  |  |  |  |  |  | Y |  |  | Y |
| `wild_bell_peppers` | flower | Y | Y |  |  | Y |  |  | Y | Y | Y | Y |
| `wild_coffee` | flower | Y | Y |  |  | Y |  |  | Y | Y | Y | Y |
| `wild_cotton` | flower | Y | Y |  |  | Y |  |  | Y | Y | Y | Y |
| `wild_dark_bell_peppers` | flower | Y | Y |  |  | Y |  |  | Y | Y | Y | Y |
| `wild_pale_bell_peppers` | flower | Y | Y |  |  | Y |  |  | Y | Y | Y | Y |

### supplementaries (2)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `flax_seeds` | crop / seed | Y | Y |  | Y |  |  |  | Y |  |  | Y |
| `wild_flax` | other |  |  |  |  |  |  |  | Y |  |  | Y |

### twigs (3)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `azalea_flowers` | vine | Y | Y |  |  |  |  |  |  |  |  |  |
| `bamboo_leaves` | cactus / cane |  |  |  |  |  |  |  | Y |  |  | Y |
| `petrified_lichen` | vine |  |  |  |  |  |  |  |  |  |  |  |

### upgrade_aquatic (57)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `acan_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `acan_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `beachgrass` | grass / fern |  |  |  | Y |  |  |  | Y |  |  |  |
| `branch_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `branch_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `chrome_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `chrome_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_acan_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_acan_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_branch_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_branch_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_chrome_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_chrome_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_finger_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_finger_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_moss_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_moss_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_petal_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_petal_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_pillow_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_pillow_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_rock_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_rock_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_silk_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_silk_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_star_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_star_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `elder_prismarine_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `elder_prismarine_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `elder_prismarine_coral_shower` | water |  |  |  |  |  |  |  |  |  |  |  |
| `finger_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `finger_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `flowering_rush` | tall flower |  |  |  | Y |  |  |  | Y |  |  |  |
| `moss_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `moss_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `ochre_kelp` | water |  |  |  | Y |  |  |  |  |  |  |  |
| `petal_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `petal_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `pillow_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `pillow_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `pink_searocket` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |
| `polar_kelp` | water |  |  |  | Y |  |  |  |  |  |  |  |
| `prismarine_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `prismarine_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `prismarine_coral_shower` | water |  |  |  |  |  |  |  |  |  |  |  |
| `river_leaf_pile` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `river_sapling` | sapling | Y | Y |  | Y | Y |  |  | Y | Y |  | Y |
| `rock_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `rock_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `silk_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `silk_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `star_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `star_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `tall_beachgrass` | grass / fern |  |  |  | Y |  |  |  | Y |  |  |  |
| `thorny_kelp` | water |  |  |  | Y |  |  |  |  |  |  |  |
| `tongue_kelp` | water |  |  |  | Y |  |  |  |  |  |  |  |
| `white_searocket` | flower | Y | Y |  | Y | Y |  |  | Y | Y | Y | Y |

### veggiesdelight (13)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `broccoli_seeds` | crop / seed |  |  |  |  |  |  |  | Y |  |  | Y |
| `cauliflower_seeds` | crop / seed |  |  |  |  |  |  |  | Y |  |  | Y |
| `garlic_clove` | crop / seed |  |  |  |  |  |  |  | Y |  |  | Y |
| `mature_dandelion` | flower |  |  |  | Y | Y |  |  | Y | Y | Y | Y |
| `sweet_potato` | crop / seed |  |  |  |  |  |  |  | Y |  |  | Y |
| `turnip_seeds` | crop / seed |  |  |  |  |  |  |  | Y |  |  | Y |
| `wild_broccoli` | flower |  |  |  |  | Y |  |  | Y | Y | Y | Y |
| `wild_cauliflowers` | flower |  |  |  |  | Y |  |  | Y | Y | Y | Y |
| `wild_garlic` | flower |  |  |  |  | Y |  |  | Y | Y | Y | Y |
| `wild_sweet_potatoes` | flower |  |  |  |  | Y |  |  | Y | Y | Y | Y |
| `wild_turnips` | flower |  |  |  |  | Y |  |  | Y | Y | Y | Y |
| `wild_zucchinis` | flower |  |  |  |  | Y |  |  | Y | Y | Y | Y |
| `zucchini_seeds` | crop / seed |  |  |  |  |  |  |  | Y |  |  | Y |

### vinery (12)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `apple_tree_sapling` | sapling |  |  |  | Y | Y |  |  | Y | Y |  | Y |
| `cherry_sapling` | sapling |  |  |  | Y | Y |  |  | Y | Y |  | Y |
| `grapevine_lattice` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `grapevine_stem` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `jungle_grape_seeds_red` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `jungle_grape_seeds_white` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `red_grape_seeds` | vine |  |  |  |  |  |  |  | Y |  |  |  |
| `savanna_grape_seeds_red` | vine |  |  |  |  |  |  |  | Y |  |  |  |
| `savanna_grape_seeds_white` | vine |  |  |  |  |  |  |  | Y |  |  |  |
| `taiga_grape_seeds_red` | vine |  |  |  |  |  |  |  | Y |  |  |  |
| `taiga_grape_seeds_white` | vine |  |  |  |  |  |  |  | Y |  |  |  |
| `white_grape_seeds` | vine |  |  |  |  |  |  |  | Y |  |  |  |

### vintagedelight (10)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `cucumber_seeds` | crop / seed |  |  |  |  |  |  |  | Y |  |  | Y |
| `ghost_pepper` | crop / seed |  |  |  |  |  |  |  | Y |  |  |  |
| `ghost_pepper_seeds` | crop / seed |  |  |  |  |  |  |  | Y |  |  |  |
| `magic_peanut` | sapling |  |  |  | Y | Y |  |  | Y | Y |  | Y |
| `oat_seeds` | crop / seed |  |  |  |  |  |  |  | Y |  |  | Y |
| `peanut` | crop / seed |  |  |  |  |  |  |  | Y |  |  | Y |
| `wild_cucumbers` | flower |  |  |  |  | Y |  |  | Y | Y | Y | Y |
| `wild_ghost_peppers` | flower |  |  |  |  | Y |  |  | Y | Y |  |  |
| `wild_oats` | flower |  |  |  |  | Y |  |  | Y | Y | Y | Y |
| `wild_peanuts` | flower |  |  |  |  | Y |  |  | Y | Y | Y | Y |

