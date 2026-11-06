# Flower potting matrix (generated)

Written by `tools/flower_pot_audit.py`; do not edit by hand. Rules and decisions: [flower_pots.md](flower_pots.md).
Plant list and vanilla pot map: live `/dumppots`. Probe results: `kubejs/exported/blocks/pot_probe.json` (cells are what the probe saw).

Y = accepted, blank = refused, part = accepted by one variant only (rich vs plain planter).

## Summary: accepted plants per holder and kind

| Holder | flower (66) | tall flower (6) | botania (203) | sapling (33) | mushroom (13) | grass / fern (18) | vine (60) | crop / seed (45) | water (74) | cactus / cane (8) | hedge (0) | other (18) | All |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Flower pot | 40 | 4 | 0 | 29 | 5 | 10 | 6 | 14 | 1 | 4 | 0 | 2 | 115 |
| Framed pot | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 3 | 0 | 0 | 0 | 1 | 5 |
| Hanging pot (Supp.) | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| Flower box (Supp.) | 61 | 6 | 100 | 33 | 11 | 10 | 3 | 10 | 5 | 4 | 0 | 2 | 245 |
| Planter box (AF) | 58 | 0 | 84 | 33 | 9 | 1 | 1 | 0 | 1 | 1 | 0 | 0 | 188 |
| Hanging pot (Beautify) | 13 | 4 | 0 | 0 | 0 | 2 | 5 | 1 | 0 | 0 | 0 | 0 | 25 |
| Large pot (Floral) | 13 | 4 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 17 |
| Vinery pot | 0 | 6 | 16 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 22 |
| Vinery box | 55 | 0 | 68 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 123 |
| Silt pot (Twigs) | 38 | 0 | 87 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 125 |
| Planter (Supp.) | 62 | 4 | 203 | 33 | 13 | 13 | 18 | 37 | 0 | 4 | 0 | 9 | 396 |

## Plants no flower pot takes

Plants the vanilla pot (and so the framed and hanging pots) refuses, by kind:

- **flower** (26): `minecraft:flowering_azalea_leaves`, `corn_delight:wild_corn`, `delightful:wild_salmonberries`, `farmersdelight:wild_beetroots`, `farmersdelight:wild_cabbages`, `farmersdelight:wild_carrots`, `farmersdelight:wild_onions`, `farmersdelight:wild_potatoes`, `farmersdelight:wild_tomatoes`, `fireflybushport:firefly_bush`, `immersive_weathering:azalea_flower_pile`, `immersive_weathering:flowering_azalea_leaf_pile`, `miners_delight:wild_cave_carrots`, `nethersdelight:mimicarnation`, `supplementaries:wild_flax`, `veggiesdelight:mature_dandelion`, `veggiesdelight:wild_broccoli`, `veggiesdelight:wild_cauliflowers`, `veggiesdelight:wild_garlic`, `veggiesdelight:wild_sweet_potatoes`, `veggiesdelight:wild_turnips`, `veggiesdelight:wild_zucchinis`, `vintagedelight:wild_cucumbers`, `vintagedelight:wild_ghost_peppers`, `vintagedelight:wild_oats`, `vintagedelight:wild_peanuts`
- **tall flower** (2): `farmersdelight:wild_rice`, `upgrade_aquatic:flowering_rush`
- **botania** (203): `botania:agricarnation`, `botania:agricarnation_chibi`, `botania:bellethorn`, `botania:bellethorn_chibi`, `botania:bergamute`, `botania:black_double_flower`, `botania:black_floating_flower`, `botania:black_mushroom`, `botania:black_mystical_flower`, `botania:black_petal`, `botania:black_shiny_flower`, `botania:blue_double_flower`, `botania:blue_floating_flower`, `botania:blue_mushroom`, `botania:blue_mystical_flower`, `botania:blue_petal`, `botania:blue_shiny_flower`, `botania:brown_double_flower`, `botania:brown_floating_flower`, `botania:brown_mushroom`, `botania:brown_mystical_flower`, `botania:brown_petal`, `botania:brown_shiny_flower`, `botania:bubbell`, `botania:bubbell_chibi`, `botania:clayconia`, `botania:clayconia_chibi`, `botania:cyan_double_flower`, `botania:cyan_floating_flower`, `botania:cyan_mushroom`, `botania:cyan_mystical_flower`, `botania:cyan_petal`, `botania:cyan_shiny_flower`, `botania:daffomill`, `botania:dandelifeon`, `botania:daybloom_motif`, `botania:dreadthorn`, `botania:endoflame`, `botania:entropinnyum`, `botania:exoflame`, `botania:fallen_kanade`, `botania:floating_agricarnation`, `botania:floating_agricarnation_chibi`, `botania:floating_bellethorn`, `botania:floating_bellethorn_chibi`, `botania:floating_bergamute`, `botania:floating_bubbell`, `botania:floating_bubbell_chibi`, `botania:floating_clayconia`, `botania:floating_clayconia_chibi`, `botania:floating_daffomill`, `botania:floating_dandelifeon`, `botania:floating_dreadthorn`, `botania:floating_endoflame`, `botania:floating_entropinnyum`, `botania:floating_exoflame`, `botania:floating_fallen_kanade`, `botania:floating_gourmaryllis`, `botania:floating_heisei_dream`, `botania:floating_hopperhock`, `botania:floating_hopperhock_chibi`, `botania:floating_hyacidus`, `botania:floating_hydroangeas`, `botania:floating_jaded_amaranthus`, `botania:floating_jiyuulia`, `botania:floating_jiyuulia_chibi`, `botania:floating_kekimurus`, `botania:floating_labellia`, `botania:floating_loonium`, `botania:floating_manastar`, `botania:floating_marimorphosis`, `botania:floating_marimorphosis_chibi`, `botania:floating_medumone`, `botania:floating_munchdew`, `botania:floating_narslimmus`, `botania:floating_orechid`, `botania:floating_orechid_ignem`, `botania:floating_pollidisiac`, `botania:floating_pure_daisy`, `botania:floating_rafflowsia`, `botania:floating_rannuncarpus`, `botania:floating_rannuncarpus_chibi`, `botania:floating_rosa_arcana`, `botania:floating_shulk_me_not`, `botania:floating_solegnolia`, `botania:floating_solegnolia_chibi`, `botania:floating_spectranthemum`, `botania:floating_spectrolus`, `botania:floating_tangleberrie`, `botania:floating_tangleberrie_chibi`, `botania:floating_thermalily`, `botania:floating_tigerseye`, `botania:floating_vinculotus`, `botania:gourmaryllis`, `botania:gray_double_flower`, `botania:gray_floating_flower`, `botania:gray_mushroom`, `botania:gray_mystical_flower`, `botania:gray_petal`, `botania:gray_shiny_flower`, `botania:green_double_flower`, `botania:green_floating_flower`, `botania:green_mushroom`, `botania:green_mystical_flower`, `botania:green_petal`, `botania:green_shiny_flower`, `botania:heisei_dream`, `botania:hopperhock`, `botania:hopperhock_chibi`, `botania:hyacidus`, `botania:hydroangeas`, `botania:hydroangeas_motif`, `botania:jaded_amaranthus`, `botania:jiyuulia`, `botania:jiyuulia_chibi`, `botania:kekimurus`, `botania:labellia`, `botania:light_blue_double_flower`, `botania:light_blue_floating_flower`, `botania:light_blue_mushroom`, `botania:light_blue_mystical_flower`, `botania:light_blue_petal`, `botania:light_blue_shiny_flower`, `botania:light_gray_double_flower`, `botania:light_gray_floating_flower`, `botania:light_gray_mushroom`, `botania:light_gray_mystical_flower`, `botania:light_gray_petal`, `botania:light_gray_shiny_flower`, `botania:lime_double_flower`, `botania:lime_floating_flower`, `botania:lime_mushroom`, `botania:lime_mystical_flower`, `botania:lime_petal`, `botania:lime_shiny_flower`, `botania:loonium`, `botania:magenta_double_flower`, `botania:magenta_floating_flower`, `botania:magenta_mushroom`, `botania:magenta_mystical_flower`, `botania:magenta_petal`, `botania:magenta_shiny_flower`, `botania:manastar`, `botania:marimorphosis`, `botania:marimorphosis_chibi`, `botania:medumone`, `botania:munchdew`, `botania:narslimmus`, `botania:nightshade_motif`, `botania:orange_double_flower`, `botania:orange_floating_flower`, `botania:orange_mushroom`, `botania:orange_mystical_flower`, `botania:orange_petal`, `botania:orange_shiny_flower`, `botania:orechid`, `botania:orechid_ignem`, `botania:pink_double_flower`, `botania:pink_floating_flower`, `botania:pink_mushroom`, `botania:pink_mystical_flower`, `botania:pink_petal`, `botania:pink_shiny_flower`, `botania:pollidisiac`, `botania:pure_daisy`, `botania:purple_double_flower`, `botania:purple_floating_flower`, `botania:purple_mushroom`, `botania:purple_mystical_flower`, `botania:purple_petal`, `botania:purple_shiny_flower`, `botania:rafflowsia`, `botania:rannuncarpus`, `botania:rannuncarpus_chibi`, `botania:red_double_flower`, `botania:red_floating_flower`, `botania:red_mushroom`, `botania:red_mystical_flower`, `botania:red_petal`, `botania:red_shiny_flower`, `botania:rosa_arcana`, `botania:shulk_me_not`, `botania:solegnolia`, `botania:solegnolia_chibi`, `botania:spectranthemum`, `botania:spectrolus`, `botania:tangleberrie`, `botania:tangleberrie_chibi`, `botania:thermalily`, `botania:tigerseye`, `botania:vinculotus`, `botania:white_double_flower`, `botania:white_floating_flower`, `botania:white_mushroom`, `botania:white_mystical_flower`, `botania:white_petal`, `botania:white_shiny_flower`, `botania:yellow_double_flower`, `botania:yellow_floating_flower`, `botania:yellow_mushroom`, `botania:yellow_mystical_flower`, `botania:yellow_petal`, `botania:yellow_shiny_flower`
- **sapling** (4): `myrtrees:rubberwood_sapling`, `vinery:apple_tree_sapling`, `vinery:cherry_sapling`, `vintagedelight:magic_peanut`
- **mushroom** (8): `farmersdelight:brown_mushroom_colony`, `farmersdelight:red_mushroom_colony`, `fungidelight:inky_cap_mushroom`, `fungidelight:inky_cap_mushroom_colony`, `fungidelight:morel_mushroom`, `fungidelight:morel_mushroom_colony`, `nethersdelight:crimson_fungus_colony`, `nethersdelight:warped_fungus_colony`
- **grass / fern** (8): `minecraft:hanging_roots`, `minecraft:small_dripleaf`, `fruitsdelight:lemon_seeds`, `fruitsdelight:lemon_tree`, `immersive_weathering:frosty_fern`, `immersive_weathering:frosty_grass`, `upgrade_aquatic:beachgrass`, `upgrade_aquatic:tall_beachgrass`
- **vine** (54): `minecraft:glow_lichen`, `minecraft:sculk_vein`, `alexsmobs:stink_bottle`, `floralench:allium_bush`, `floralench:allium_vine`, `floralench:azure_bluet_bush`, `floralench:azure_bluet_vine`, `floralench:blue_orchid_bush`, `floralench:blue_orchid_vine`, `floralench:cornflower_bush`, `floralench:cornflower_vine`, `floralench:dandelion_bush`, `floralench:dandelion_vine`, `floralench:lilac_bush`, `floralench:lilac_vine`, `floralench:lily_of_the_valley_bush`, `floralench:lily_of_the_valley_vine`, `floralench:orange_tulip_bush`, `floralench:orange_tulip_vine`, `floralench:oxeye_daisy_bush`, `floralench:oxeye_daisy_vine`, `floralench:peony_bush`, `floralench:peony_vine`, `floralench:pink_tulip_bush`, `floralench:pink_tulip_vine`, `floralench:poppy_bush`, `floralench:poppy_vine`, `floralench:red_tulip_bush`, `floralench:red_tulip_vine`, `floralench:rose_bush_bush`, `floralench:rose_bush_vine`, `floralench:sunflower_bush`, `floralench:sunflower_vine`, `floralench:white_tulip_bush`, `floralench:white_tulip_vine`, `floralench:wither_rose_bush`, `floralench:wither_rose_vine`, `immersive_weathering:frost`, `immersive_weathering:ivy`, `immersive_weathering:moss_clump`, `immersive_weathering:soot`, `quark:cut_vine`, `twigs:petrified_lichen`, `upgrade_aquatic:river_leaf_pile`, `vinery:grapevine_lattice`, `vinery:grapevine_stem`, `vinery:jungle_grape_seeds_red`, `vinery:jungle_grape_seeds_white`, `vinery:red_grape_seeds`, `vinery:savanna_grape_seeds_red`, `vinery:savanna_grape_seeds_white`, `vinery:taiga_grape_seeds_red`, `vinery:taiga_grape_seeds_white`, `vinery:white_grape_seeds`
- **crop / seed** (31): `minecraft:melon`, `minecraft:pumpkin`, `corn_delight:corn_seeds`, `delightful:cantaloupe`, `delightful:cantaloupe_seeds`, `delightful:mini_melon`, `endercrop:ender_seeds`, `farmersdelight:cabbage_seeds`, `farmersdelight:onion`, `farmersdelight:tomato_seeds`, `fruitsdelight:hamimelon`, `fruitsdelight:hamimelon_seeds`, `hearthandharvest:raspberry_pips`, `immersive_weathering:weeds`, `miners_delight:cave_carrot`, `rusticdelight:bell_pepper_seeds`, `rusticdelight:coffee_beans`, `rusticdelight:cotton_seeds`, `rusticdelight:dark_bell_pepper_seeds`, `rusticdelight:pale_bell_pepper_seeds`, `veggiesdelight:broccoli_seeds`, `veggiesdelight:cauliflower_seeds`, `veggiesdelight:garlic_clove`, `veggiesdelight:sweet_potato`, `veggiesdelight:turnip_seeds`, `veggiesdelight:zucchini_seeds`, `vintagedelight:cucumber_seeds`, `vintagedelight:ghost_pepper`, `vintagedelight:ghost_pepper_seeds`, `vintagedelight:oat_seeds`, `vintagedelight:peanut`
- **water** (73): `minecraft:brain_coral`, `minecraft:brain_coral_fan`, `minecraft:bubble_coral`, `minecraft:bubble_coral_fan`, `minecraft:dead_brain_coral`, `minecraft:dead_brain_coral_fan`, `minecraft:dead_bubble_coral`, `minecraft:dead_bubble_coral_fan`, `minecraft:dead_fire_coral`, `minecraft:dead_fire_coral_fan`, `minecraft:dead_horn_coral`, `minecraft:dead_horn_coral_fan`, `minecraft:dead_tube_coral`, `minecraft:dead_tube_coral_fan`, `minecraft:fire_coral`, `minecraft:fire_coral_fan`, `minecraft:horn_coral`, `minecraft:horn_coral_fan`, `minecraft:kelp`, `minecraft:lily_pad`, `minecraft:seagrass`, `minecraft:tube_coral`, `minecraft:tube_coral_fan`, `upgrade_aquatic:acan_coral`, `upgrade_aquatic:acan_coral_fan`, `upgrade_aquatic:branch_coral`, `upgrade_aquatic:branch_coral_fan`, `upgrade_aquatic:chrome_coral`, `upgrade_aquatic:chrome_coral_fan`, `upgrade_aquatic:dead_acan_coral`, `upgrade_aquatic:dead_acan_coral_fan`, `upgrade_aquatic:dead_branch_coral`, `upgrade_aquatic:dead_branch_coral_fan`, `upgrade_aquatic:dead_chrome_coral`, `upgrade_aquatic:dead_chrome_coral_fan`, `upgrade_aquatic:dead_finger_coral`, `upgrade_aquatic:dead_finger_coral_fan`, `upgrade_aquatic:dead_moss_coral`, `upgrade_aquatic:dead_moss_coral_fan`, `upgrade_aquatic:dead_petal_coral`, `upgrade_aquatic:dead_petal_coral_fan`, `upgrade_aquatic:dead_pillow_coral`, `upgrade_aquatic:dead_pillow_coral_fan`, `upgrade_aquatic:dead_rock_coral`, `upgrade_aquatic:dead_rock_coral_fan`, `upgrade_aquatic:dead_silk_coral`, `upgrade_aquatic:dead_silk_coral_fan`, `upgrade_aquatic:dead_star_coral`, `upgrade_aquatic:dead_star_coral_fan`, `upgrade_aquatic:elder_prismarine_coral`, `upgrade_aquatic:elder_prismarine_coral_fan`, `upgrade_aquatic:elder_prismarine_coral_shower`, `upgrade_aquatic:finger_coral`, `upgrade_aquatic:finger_coral_fan`, `upgrade_aquatic:moss_coral`, `upgrade_aquatic:moss_coral_fan`, `upgrade_aquatic:ochre_kelp`, `upgrade_aquatic:petal_coral`, `upgrade_aquatic:petal_coral_fan`, `upgrade_aquatic:pillow_coral`, `upgrade_aquatic:pillow_coral_fan`, `upgrade_aquatic:polar_kelp`, `upgrade_aquatic:prismarine_coral`, `upgrade_aquatic:prismarine_coral_fan`, `upgrade_aquatic:prismarine_coral_shower`, `upgrade_aquatic:rock_coral`, `upgrade_aquatic:rock_coral_fan`, `upgrade_aquatic:silk_coral`, `upgrade_aquatic:silk_coral_fan`, `upgrade_aquatic:star_coral`, `upgrade_aquatic:star_coral_fan`, `upgrade_aquatic:thorny_kelp`, `upgrade_aquatic:tongue_kelp`
- **cactus / cane** (4): `minecraft:chorus_plant`, `endersdelight:chorusflame`, `ends_delight:chorus_succulent`, `twigs:bamboo_leaves`
- **other** (16): `minecraft:big_dripleaf`, `minecraft:moss_block`, `minecraft:spore_blossom`, `alexsmobs:banana_peel`, `delightful:salmonberry_pips`, `endersdelight:amberveil`, `endersdelight:ethereal_saffron`, `farmersdelight:rice`, `farmersdelight:sandy_shrub`, `fruitsdelight:blueberry`, `fruitsdelight:blueberry_bush`, `fruitsdelight:cranberry`, `fruitsdelight:pineapple_sapling`, `fruitsdelight:wild_pineapple`, `neapolitan:strawberry_pips`, `nethersdelight:propelplant_cane`

## Probe against prediction

- framed: `minecraft:acacia_sapling` predicted yes, probe no
- hanging: `minecraft:acacia_sapling` predicted yes, probe no
- framed: `minecraft:allium` predicted yes, probe no
- hanging: `minecraft:allium` predicted yes, probe no
- framed: `minecraft:azalea` predicted yes, probe no
- hanging: `minecraft:azalea` predicted yes, probe no
- pbox: `minecraft:azalea` predicted no, probe yes
- framed: `minecraft:azure_bluet` predicted yes, probe no
- hanging: `minecraft:azure_bluet` predicted yes, probe no
- framed: `minecraft:bamboo` predicted yes, probe no
- hanging: `minecraft:bamboo` predicted yes, probe no
- framed: `minecraft:beetroot_seeds` predicted yes, probe no
- hanging: `minecraft:beetroot_seeds` predicted yes, probe no
- pbox: `minecraft:beetroot_seeds` predicted yes, probe no
- framed: `minecraft:birch_sapling` predicted yes, probe no
- hanging: `minecraft:birch_sapling` predicted yes, probe no
- framed: `minecraft:blue_orchid` predicted yes, probe no
- hanging: `minecraft:blue_orchid` predicted yes, probe no
- framed: `minecraft:brown_mushroom` predicted yes, probe no
- hanging: `minecraft:brown_mushroom` predicted yes, probe no
- framed: `minecraft:cactus` predicted yes, probe no
- hanging: `minecraft:cactus` predicted yes, probe no
- pbox: `minecraft:cactus` predicted yes, probe no
- framed: `minecraft:carrot` predicted yes, probe no
- hanging: `minecraft:carrot` predicted yes, probe no
- pbox: `minecraft:carrot` predicted yes, probe no
- framed: `minecraft:chorus_flower` predicted yes, probe no
- hanging: `minecraft:chorus_flower` predicted yes, probe no
- pbox: `minecraft:chorus_flower` predicted yes, probe no
- silt: `minecraft:chorus_flower` predicted yes, probe no
- framed: `minecraft:cocoa_beans` predicted yes, probe no
- hanging: `minecraft:cocoa_beans` predicted yes, probe no
- pbox: `minecraft:cocoa_beans` predicted yes, probe no
- framed: `minecraft:cornflower` predicted yes, probe no
- hanging: `minecraft:cornflower` predicted yes, probe no
- framed: `minecraft:crimson_fungus` predicted yes, probe no
- hanging: `minecraft:crimson_fungus` predicted yes, probe no
- framed: `minecraft:crimson_roots` predicted yes, probe no
- hanging: `minecraft:crimson_roots` predicted yes, probe no
- pbox: `minecraft:crimson_roots` predicted yes, probe no
- framed: `minecraft:dandelion` predicted yes, probe no
- hanging: `minecraft:dandelion` predicted yes, probe no
- framed: `minecraft:dark_oak_sapling` predicted yes, probe no
- hanging: `minecraft:dark_oak_sapling` predicted yes, probe no
- framed: `minecraft:dead_bush` predicted yes, probe no
- hanging: `minecraft:dead_bush` predicted yes, probe no
- pbox: `minecraft:dead_bush` predicted yes, probe no
- framed: `minecraft:fern` predicted yes, probe no
- hanging: `minecraft:fern` predicted yes, probe no
- framed: `minecraft:flowering_azalea` predicted yes, probe no
- hanging: `minecraft:flowering_azalea` predicted yes, probe no
- pbox: `minecraft:flowering_azalea` predicted no, probe yes
- framed: `minecraft:flowering_azalea_leaves` predicted no, probe yes
- planter: `minecraft:flowering_azalea_leaves` predicted no, probe yes
- framed: `minecraft:glow_berries` predicted yes, probe no
- hanging: `minecraft:glow_berries` predicted yes, probe no
- pbox: `minecraft:glow_berries` predicted yes, probe no
- pbox: `minecraft:glow_lichen` predicted yes, probe no
- framed: `minecraft:grass` predicted yes, probe no
- hanging: `minecraft:grass` predicted yes, probe no
- pbox: `minecraft:grass` predicted yes, probe no
- framed: `minecraft:jungle_sapling` predicted yes, probe no
- hanging: `minecraft:jungle_sapling` predicted yes, probe no
- framed: `minecraft:large_fern` predicted yes, probe no
- hanging: `minecraft:large_fern` predicted yes, probe no
- framed: `minecraft:lilac` predicted yes, probe no
- hanging: `minecraft:lilac` predicted yes, probe no
- pbox: `minecraft:lilac` predicted yes, probe no
- framed: `minecraft:lily_of_the_valley` predicted yes, probe no
- hanging: `minecraft:lily_of_the_valley` predicted yes, probe no
- planter: `minecraft:lily_pad` predicted yes, probe no
- framed: `minecraft:mangrove_propagule` predicted yes, probe no
- hanging: `minecraft:mangrove_propagule` predicted yes, probe no
- framed: `minecraft:melon` predicted no, probe yes
- framed: `minecraft:melon_seeds` predicted yes, probe no
- hanging: `minecraft:melon_seeds` predicted yes, probe no
- pbox: `minecraft:melon_seeds` predicted yes, probe no
- framed: `minecraft:moss_block` predicted no, probe yes
- planter: `minecraft:moss_block` predicted no, probe yes
- framed: `minecraft:nether_sprouts` predicted yes, probe no
- hanging: `minecraft:nether_sprouts` predicted yes, probe no
- pbox: `minecraft:nether_sprouts` predicted yes, probe no
- framed: `minecraft:nether_wart` predicted yes, probe no
- hanging: `minecraft:nether_wart` predicted yes, probe no
- pbox: `minecraft:nether_wart` predicted yes, probe no
- framed: `minecraft:oak_sapling` predicted yes, probe no
- hanging: `minecraft:oak_sapling` predicted yes, probe no
- framed: `minecraft:orange_tulip` predicted yes, probe no
- hanging: `minecraft:orange_tulip` predicted yes, probe no
- framed: `minecraft:oxeye_daisy` predicted yes, probe no
- hanging: `minecraft:oxeye_daisy` predicted yes, probe no
- framed: `minecraft:peony` predicted yes, probe no
- hanging: `minecraft:peony` predicted yes, probe no
- pbox: `minecraft:peony` predicted yes, probe no
- framed: `minecraft:pink_tulip` predicted yes, probe no
- hanging: `minecraft:pink_tulip` predicted yes, probe no
- framed: `minecraft:poppy` predicted yes, probe no
- hanging: `minecraft:poppy` predicted yes, probe no
- framed: `minecraft:potato` predicted yes, probe no
- hanging: `minecraft:potato` predicted yes, probe no
- pbox: `minecraft:potato` predicted yes, probe no
- framed: `minecraft:pumpkin` predicted no, probe yes
- framed: `minecraft:pumpkin_seeds` predicted yes, probe no
- hanging: `minecraft:pumpkin_seeds` predicted yes, probe no
- pbox: `minecraft:pumpkin_seeds` predicted yes, probe no
- framed: `minecraft:red_mushroom` predicted yes, probe no
- hanging: `minecraft:red_mushroom` predicted yes, probe no
- framed: `minecraft:red_tulip` predicted yes, probe no
- hanging: `minecraft:red_tulip` predicted yes, probe no
- framed: `minecraft:rose_bush` predicted yes, probe no
- hanging: `minecraft:rose_bush` predicted yes, probe no
- pbox: `minecraft:rose_bush` predicted yes, probe no
- framed: `minecraft:sea_pickle` predicted yes, probe no
- hanging: `minecraft:sea_pickle` predicted yes, probe no
- planter: `minecraft:sea_pickle` predicted yes, probe no
- planter: `minecraft:seagrass` predicted yes, probe no
- planter: `minecraft:small_dripleaf` predicted yes, probe no
- framed: `minecraft:spruce_sapling` predicted yes, probe no
- hanging: `minecraft:spruce_sapling` predicted yes, probe no
- framed: `minecraft:sugar_cane` predicted yes, probe no
- hanging: `minecraft:sugar_cane` predicted yes, probe no
- framed: `minecraft:sunflower` predicted yes, probe no
- hanging: `minecraft:sunflower` predicted yes, probe no
- pbox: `minecraft:sunflower` predicted yes, probe no
- framed: `minecraft:sweet_berries` predicted yes, probe no
- hanging: `minecraft:sweet_berries` predicted yes, probe no
- planter: `minecraft:sweet_berries` predicted yes, probe no
- framed: `minecraft:tall_grass` predicted yes, probe no
- hanging: `minecraft:tall_grass` predicted yes, probe no
- framed: `minecraft:twisting_vines` predicted yes, probe no
- hanging: `minecraft:twisting_vines` predicted yes, probe no
- framed: `minecraft:vine` predicted yes, probe no
- hanging: `minecraft:vine` predicted yes, probe no
- framed: `minecraft:warped_fungus` predicted yes, probe no
- hanging: `minecraft:warped_fungus` predicted yes, probe no
- framed: `minecraft:warped_roots` predicted yes, probe no
- hanging: `minecraft:warped_roots` predicted yes, probe no
- pbox: `minecraft:warped_roots` predicted yes, probe no
- framed: `minecraft:weeping_vines` predicted yes, probe no
- hanging: `minecraft:weeping_vines` predicted yes, probe no
- framed: `minecraft:wheat_seeds` predicted yes, probe no
- hanging: `minecraft:wheat_seeds` predicted yes, probe no
- framed: `minecraft:white_tulip` predicted yes, probe no
- hanging: `minecraft:white_tulip` predicted yes, probe no
- framed: `minecraft:wither_rose` predicted yes, probe no
- hanging: `minecraft:wither_rose` predicted yes, probe no
- framed: `architects_palette:twisted_sapling` predicted yes, probe no
- hanging: `architects_palette:twisted_sapling` predicted yes, probe no
- framed: `berry_good:glow_berry_pips` predicted yes, probe no
- hanging: `berry_good:glow_berry_pips` predicted yes, probe no
- framed: `berry_good:sweet_berry_pips` predicted yes, probe no
- hanging: `berry_good:sweet_berry_pips` predicted yes, probe no
- silt: `botania:black_floating_flower` predicted yes, probe no
- planter: `botania:black_floating_flower` predicted no, probe yes
- silt: `botania:blue_floating_flower` predicted yes, probe no
- planter: `botania:blue_floating_flower` predicted no, probe yes
- silt: `botania:brown_floating_flower` predicted yes, probe no
- planter: `botania:brown_floating_flower` predicted no, probe yes
- silt: `botania:cyan_floating_flower` predicted yes, probe no
- planter: `botania:cyan_floating_flower` predicted no, probe yes
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
- silt: `botania:gray_floating_flower` predicted yes, probe no
- planter: `botania:gray_floating_flower` predicted no, probe yes
- silt: `botania:green_floating_flower` predicted yes, probe no
- planter: `botania:green_floating_flower` predicted no, probe yes
- silt: `botania:light_blue_floating_flower` predicted yes, probe no
- planter: `botania:light_blue_floating_flower` predicted no, probe yes
- silt: `botania:light_gray_floating_flower` predicted yes, probe no
- planter: `botania:light_gray_floating_flower` predicted no, probe yes
- silt: `botania:lime_floating_flower` predicted yes, probe no
- planter: `botania:lime_floating_flower` predicted no, probe yes
- silt: `botania:magenta_floating_flower` predicted yes, probe no
- planter: `botania:magenta_floating_flower` predicted no, probe yes
- silt: `botania:orange_floating_flower` predicted yes, probe no
- planter: `botania:orange_floating_flower` predicted no, probe yes
- silt: `botania:pink_floating_flower` predicted yes, probe no
- planter: `botania:pink_floating_flower` predicted no, probe yes
- silt: `botania:purple_floating_flower` predicted yes, probe no
- planter: `botania:purple_floating_flower` predicted no, probe yes
- silt: `botania:red_floating_flower` predicted yes, probe no
- planter: `botania:red_floating_flower` predicted no, probe yes
- silt: `botania:white_floating_flower` predicted yes, probe no
- planter: `botania:white_floating_flower` predicted no, probe yes
- silt: `botania:yellow_floating_flower` predicted yes, probe no
- planter: `botania:yellow_floating_flower` predicted no, probe yes
- framed: `bushierflowers:grown_allium` predicted yes, probe no
- hanging: `bushierflowers:grown_allium` predicted yes, probe no
- framed: `bushierflowers:grown_azure_bluet` predicted yes, probe no
- hanging: `bushierflowers:grown_azure_bluet` predicted yes, probe no
- framed: `bushierflowers:grown_blue_orchid` predicted yes, probe no
- hanging: `bushierflowers:grown_blue_orchid` predicted yes, probe no
- framed: `bushierflowers:grown_cornflower` predicted yes, probe no
- hanging: `bushierflowers:grown_cornflower` predicted yes, probe no
- framed: `bushierflowers:grown_dandelion` predicted yes, probe no
- hanging: `bushierflowers:grown_dandelion` predicted yes, probe no
- framed: `bushierflowers:grown_lily` predicted yes, probe no
- hanging: `bushierflowers:grown_lily` predicted yes, probe no
- framed: `bushierflowers:grown_orange_tulip` predicted yes, probe no
- hanging: `bushierflowers:grown_orange_tulip` predicted yes, probe no
- framed: `bushierflowers:grown_oxeye_daisy` predicted yes, probe no
- hanging: `bushierflowers:grown_oxeye_daisy` predicted yes, probe no
- framed: `bushierflowers:grown_pink_tulip` predicted yes, probe no
- hanging: `bushierflowers:grown_pink_tulip` predicted yes, probe no
- framed: `bushierflowers:grown_poppy` predicted yes, probe no
- hanging: `bushierflowers:grown_poppy` predicted yes, probe no
- framed: `bushierflowers:grown_red_tulip` predicted yes, probe no
- hanging: `bushierflowers:grown_red_tulip` predicted yes, probe no
- framed: `bushierflowers:grown_white_tulip` predicted yes, probe no
- hanging: `bushierflowers:grown_white_tulip` predicted yes, probe no
- framed: `bushierflowers:grown_wither_rose` predicted yes, probe no
- hanging: `bushierflowers:grown_wither_rose` predicted yes, probe no
- framed: `buzzier_bees:buttercup` predicted yes, probe no
- hanging: `buzzier_bees:buttercup` predicted yes, probe no
- framed: `buzzier_bees:pink_clover` predicted yes, probe no
- hanging: `buzzier_bees:pink_clover` predicted yes, probe no
- framed: `buzzier_bees:white_clover` predicted yes, probe no
- hanging: `buzzier_bees:white_clover` predicted yes, probe no
- silt: `corn_delight:wild_corn` predicted yes, probe no
- silt: `delightful:wild_salmonberries` predicted yes, probe no
- planter: `endercrop:ender_seeds` predicted yes, probe no
- planter: `endersdelight:amberveil` predicted yes, probe no
- planter: `endersdelight:chorusflame` predicted yes, probe no
- planter: `endersdelight:ethereal_saffron` predicted yes, probe no
- planter: `ends_delight:chorus_succulent` predicted yes, probe no
- planter: `farmersdelight:rice` predicted yes, probe no
- silt: `farmersdelight:wild_beetroots` predicted yes, probe no
- silt: `farmersdelight:wild_cabbages` predicted yes, probe no
- silt: `farmersdelight:wild_carrots` predicted yes, probe no
- silt: `farmersdelight:wild_onions` predicted yes, probe no
- silt: `farmersdelight:wild_potatoes` predicted yes, probe no
- planter: `farmersdelight:wild_rice` predicted yes, probe no
- silt: `farmersdelight:wild_tomatoes` predicted yes, probe no
- framed: `farmersrespite:coffee_beans` predicted yes, probe no
- hanging: `farmersrespite:coffee_beans` predicted yes, probe no
- planter: `farmersrespite:coffee_beans` predicted yes, probe no
- framed: `farmersrespite:tea_seeds` predicted yes, probe no
- hanging: `farmersrespite:tea_seeds` predicted yes, probe no
- planter: `farmersrespite:tea_seeds` predicted yes, probe no
- framed: `farmersrespite:wild_coffee_bush` predicted yes, probe no
- hanging: `farmersrespite:wild_coffee_bush` predicted yes, probe no
- planter: `farmersrespite:wild_coffee_bush` predicted yes, probe no
- framed: `farmersrespite:wild_tea_bush` predicted yes, probe no
- hanging: `farmersrespite:wild_tea_bush` predicted yes, probe no
- framed: `fruitsdelight:apple_sapling` predicted yes, probe no
- hanging: `fruitsdelight:apple_sapling` predicted yes, probe no
- fbox: `fruitsdelight:apple_sapling` predicted no, probe yes
- pbox: `fruitsdelight:apple_sapling` predicted no, probe yes
- framed: `fruitsdelight:bayberry_sapling` predicted yes, probe no
- hanging: `fruitsdelight:bayberry_sapling` predicted yes, probe no
- framed: `fruitsdelight:durian_sapling` predicted yes, probe no
- hanging: `fruitsdelight:durian_sapling` predicted yes, probe no
- framed: `fruitsdelight:fig_sapling` predicted yes, probe no
- hanging: `fruitsdelight:fig_sapling` predicted yes, probe no
- framed: `fruitsdelight:hamimelon` predicted no, probe yes
- framed: `fruitsdelight:hawberry_sapling` predicted yes, probe no
- hanging: `fruitsdelight:hawberry_sapling` predicted yes, probe no
- framed: `fruitsdelight:kiwi_sapling` predicted yes, probe no
- hanging: `fruitsdelight:kiwi_sapling` predicted yes, probe no
- framed: `fruitsdelight:lychee_sapling` predicted yes, probe no
- hanging: `fruitsdelight:lychee_sapling` predicted yes, probe no
- framed: `fruitsdelight:mango_sapling` predicted yes, probe no
- hanging: `fruitsdelight:mango_sapling` predicted yes, probe no
- framed: `fruitsdelight:mangosteen_sapling` predicted yes, probe no
- hanging: `fruitsdelight:mangosteen_sapling` predicted yes, probe no
- framed: `fruitsdelight:orange_sapling` predicted yes, probe no
- hanging: `fruitsdelight:orange_sapling` predicted yes, probe no
- framed: `fruitsdelight:peach_sapling` predicted yes, probe no
- hanging: `fruitsdelight:peach_sapling` predicted yes, probe no
- framed: `fruitsdelight:pear_sapling` predicted yes, probe no
- hanging: `fruitsdelight:pear_sapling` predicted yes, probe no
- framed: `fruitsdelight:persimmon_sapling` predicted yes, probe no
- hanging: `fruitsdelight:persimmon_sapling` predicted yes, probe no
- planter: `fruitsdelight:pineapple_sapling` predicted yes, probe no
- planter: `fruitsdelight:wild_pineapple` predicted yes, probe no
- planter: `immersive_weathering:weeds` predicted yes, probe no
- framed: `immersiveengineering:seed` predicted yes, probe no
- hanging: `immersiveengineering:seed` predicted yes, probe no
- framed: `miners_delight:gossypium` predicted yes, probe no
- hanging: `miners_delight:gossypium` predicted yes, probe no
- silt: `miners_delight:gossypium` predicted yes, probe no
- silt: `miners_delight:wild_cave_carrots` predicted yes, probe no
- framed: `neapolitan:banana_frond` predicted yes, probe no
- hanging: `neapolitan:banana_frond` predicted yes, probe no
- planter: `neapolitan:banana_frond` predicted yes, probe no
- framed: `neapolitan:mint_sprout` predicted yes, probe no
- hanging: `neapolitan:mint_sprout` predicted yes, probe no
- pot: `nethersdelight:mimicarnation` predicted yes, probe no
- framed: `nethersdelight:mimicarnation` predicted yes, probe no
- hanging: `nethersdelight:mimicarnation` predicted yes, probe no
- planter: `nethersdelight:propelplant_cane` predicted yes, probe no
- framed: `quark:ancient_sapling` predicted yes, probe no
- hanging: `quark:ancient_sapling` predicted yes, probe no
- framed: `quark:blue_blossom_sapling` predicted yes, probe no
- hanging: `quark:blue_blossom_sapling` predicted yes, probe no
- framed: `quark:glow_lichen_growth` predicted yes, probe no
- hanging: `quark:glow_lichen_growth` predicted yes, probe no
- framed: `quark:glow_shroom` predicted yes, probe no
- hanging: `quark:glow_shroom` predicted yes, probe no
- framed: `quark:lavender_blossom_sapling` predicted yes, probe no
- hanging: `quark:lavender_blossom_sapling` predicted yes, probe no
- framed: `quark:orange_blossom_sapling` predicted yes, probe no
- hanging: `quark:orange_blossom_sapling` predicted yes, probe no
- framed: `quark:pink_blossom_sapling` predicted yes, probe no
- hanging: `quark:pink_blossom_sapling` predicted yes, probe no
- framed: `quark:red_blossom_sapling` predicted yes, probe no
- hanging: `quark:red_blossom_sapling` predicted yes, probe no
- framed: `quark:yellow_blossom_sapling` predicted yes, probe no
- hanging: `quark:yellow_blossom_sapling` predicted yes, probe no
- framed: `rusticdelight:wild_bell_peppers` predicted yes, probe no
- hanging: `rusticdelight:wild_bell_peppers` predicted yes, probe no
- silt: `rusticdelight:wild_bell_peppers` predicted yes, probe no
- framed: `rusticdelight:wild_coffee` predicted yes, probe no
- hanging: `rusticdelight:wild_coffee` predicted yes, probe no
- silt: `rusticdelight:wild_coffee` predicted yes, probe no
- framed: `rusticdelight:wild_cotton` predicted yes, probe no
- hanging: `rusticdelight:wild_cotton` predicted yes, probe no
- silt: `rusticdelight:wild_cotton` predicted yes, probe no
- framed: `rusticdelight:wild_dark_bell_peppers` predicted yes, probe no
- hanging: `rusticdelight:wild_dark_bell_peppers` predicted yes, probe no
- silt: `rusticdelight:wild_dark_bell_peppers` predicted yes, probe no
- framed: `rusticdelight:wild_pale_bell_peppers` predicted yes, probe no
- hanging: `rusticdelight:wild_pale_bell_peppers` predicted yes, probe no
- silt: `rusticdelight:wild_pale_bell_peppers` predicted yes, probe no
- framed: `supplementaries:flax_seeds` predicted yes, probe no
- hanging: `supplementaries:flax_seeds` predicted yes, probe no
- framed: `twigs:azalea_flowers` predicted yes, probe no
- hanging: `twigs:azalea_flowers` predicted yes, probe no
- planter: `upgrade_aquatic:beachgrass` predicted yes, probe no
- planter: `upgrade_aquatic:flowering_rush` predicted yes, probe no
- framed: `upgrade_aquatic:pink_searocket` predicted yes, probe no
- hanging: `upgrade_aquatic:pink_searocket` predicted yes, probe no
- silt: `upgrade_aquatic:pink_searocket` predicted yes, probe no
- framed: `upgrade_aquatic:river_sapling` predicted yes, probe no
- hanging: `upgrade_aquatic:river_sapling` predicted yes, probe no
- planter: `upgrade_aquatic:tall_beachgrass` predicted yes, probe no
- framed: `upgrade_aquatic:white_searocket` predicted yes, probe no
- hanging: `upgrade_aquatic:white_searocket` predicted yes, probe no
- silt: `upgrade_aquatic:white_searocket` predicted yes, probe no
- planter: `vinery:red_grape_seeds` predicted yes, probe no
- planter: `vinery:savanna_grape_seeds_red` predicted yes, probe no
- planter: `vinery:savanna_grape_seeds_white` predicted yes, probe no
- planter: `vinery:taiga_grape_seeds_red` predicted yes, probe no
- planter: `vinery:taiga_grape_seeds_white` predicted yes, probe no
- planter: `vinery:white_grape_seeds` predicted yes, probe no
- planter: `vintagedelight:ghost_pepper` predicted yes, probe no
- planter: `vintagedelight:ghost_pepper_seeds` predicted yes, probe no
- silt: `vintagedelight:wild_cucumbers` predicted yes, probe no
- silt: `vintagedelight:wild_ghost_peppers` predicted yes, probe no
- planter: `vintagedelight:wild_ghost_peppers` predicted yes, probe no
- silt: `vintagedelight:wild_oats` predicted yes, probe no
- silt: `vintagedelight:wild_peanuts` predicted yes, probe no

## Probe exceptions

- fbox: `minecraft:hanging_roots`: dev.latvian.mods.rhino.WrappedException: Wrapped java.lang.IllegalArgumentException: Cannot set property DirectionProperty{name=facing, claz
- pbox: `minecraft:hanging_roots`: dev.latvian.mods.rhino.WrappedException: Wrapped java.lang.IllegalArgumentException: Cannot set property DirectionProperty{name=facing, claz
- beautify: `minecraft:hanging_roots`: dev.latvian.mods.rhino.WrappedException: Wrapped java.lang.IllegalArgumentException: Cannot set property DirectionProperty{name=facing, claz
- vpot: `minecraft:hanging_roots`: dev.latvian.mods.rhino.WrappedException: Wrapped java.lang.IllegalArgumentException: Cannot set property DirectionProperty{name=facing, claz
- planter: `minecraft:hanging_roots`: dev.latvian.mods.rhino.WrappedException: Wrapped java.lang.IllegalArgumentException: Cannot set property DirectionProperty{name=facing, claz
- planter: `minecraft:hanging_roots`: dev.latvian.mods.rhino.WrappedException: Wrapped java.lang.IllegalArgumentException: Cannot set property DirectionProperty{name=facing, claz

## Full matrix

### minecraft (90)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `acacia_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `allium` | flower | Y |  |  | Y | Y | Y | Y |  | Y | Y | Y |
| `azalea` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `azure_bluet` | flower | Y |  |  | Y | Y | Y | Y |  | Y | Y | Y |
| `bamboo` | cactus / cane | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `beetroot_seeds` | crop / seed | Y |  |  | Y |  |  |  |  |  |  | Y |
| `big_dripleaf` | other |  |  |  |  |  |  |  |  |  |  |  |
| `birch_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `blue_orchid` | flower | Y |  |  | Y | Y | Y | Y |  | Y | Y | Y |
| `brain_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `brain_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `brown_mushroom` | mushroom | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `bubble_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `bubble_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `cactus` | cactus / cane | Y |  |  | Y |  |  |  |  |  |  | Y |
| `carrot` | crop / seed | Y |  |  | Y |  |  |  |  |  |  | Y |
| `chorus_flower` | cactus / cane | Y |  |  | Y |  |  |  |  |  |  |  |
| `chorus_plant` | cactus / cane |  |  |  |  |  |  |  |  |  |  |  |
| `cocoa_beans` | crop / seed | Y |  |  | Y |  |  |  |  |  |  |  |
| `cornflower` | flower | Y |  |  | Y | Y | Y | Y |  | Y | Y | Y |
| `crimson_fungus` | mushroom | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `crimson_roots` | grass / fern | Y |  |  | Y |  |  |  |  |  |  | Y |
| `dandelion` | flower | Y |  |  | Y | Y | Y | Y |  | Y | Y | Y |
| `dark_oak_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `dead_brain_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_brain_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_bubble_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_bubble_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_bush` | grass / fern | Y |  |  | Y |  |  |  |  |  |  | Y |
| `dead_fire_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_fire_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_horn_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_horn_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_tube_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `dead_tube_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `fern` | grass / fern | Y |  |  | Y | Y | Y |  |  |  |  | Y |
| `fire_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `fire_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `flowering_azalea` | flower | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `flowering_azalea_leaves` | flower |  | Y |  | Y | Y |  |  |  |  |  | Y |
| `glow_berries` | vine | Y |  |  |  |  | Y |  |  |  |  |  |
| `glow_lichen` | vine |  |  |  |  |  | Y |  |  |  |  |  |
| `grass` | grass / fern | Y |  |  | Y |  | Y |  |  |  |  | Y |
| `hanging_roots` | grass / fern |  |  |  |  |  |  |  |  |  |  |  |
| `horn_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `horn_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `jungle_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `kelp` | water |  |  |  |  |  |  |  |  |  |  |  |
| `large_fern` | grass / fern | Y |  |  | Y |  |  |  |  |  |  | Y |
| `lilac` | tall flower | Y |  |  | Y |  | Y | Y | Y |  |  | Y |
| `lily_of_the_valley` | flower | Y |  |  | Y | Y | Y | Y |  | Y | Y | Y |
| `lily_pad` | water |  |  |  |  |  |  |  |  |  |  |  |
| `mangrove_propagule` | flower | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `melon` | crop / seed |  | Y |  |  |  |  |  |  |  |  | Y |
| `melon_seeds` | crop / seed | Y |  |  | Y |  |  |  |  |  |  | Y |
| `moss_block` | other |  | Y |  |  |  |  |  |  |  |  | Y |
| `nether_sprouts` | grass / fern | Y |  |  | Y |  |  |  |  |  |  | Y |
| `nether_wart` | crop / seed | Y |  |  | Y |  |  |  |  |  |  | Y |
| `oak_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `orange_tulip` | flower | Y |  |  | Y | Y | Y | Y |  | Y | Y | Y |
| `oxeye_daisy` | flower | Y |  |  | Y | Y | Y | Y |  | Y | Y | Y |
| `peony` | tall flower | Y |  |  | Y |  | Y | Y | Y |  |  | Y |
| `pink_tulip` | flower | Y |  |  | Y | Y | Y | Y |  | Y | Y | Y |
| `poppy` | flower | Y |  |  | Y | Y | Y | Y |  | Y | Y | Y |
| `potato` | crop / seed | Y |  |  | Y |  |  |  |  |  |  | Y |
| `pumpkin` | crop / seed |  | Y |  |  |  |  |  |  |  |  | Y |
| `pumpkin_seeds` | crop / seed | Y |  |  | Y |  |  |  |  |  |  | Y |
| `red_mushroom` | mushroom | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `red_tulip` | flower | Y |  |  | Y | Y | Y | Y |  | Y | Y | Y |
| `rose_bush` | tall flower | Y |  |  | Y |  | Y | Y | Y |  |  | Y |
| `sculk_vein` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `sea_pickle` | water | Y |  |  | Y | Y |  |  |  |  |  |  |
| `seagrass` | water |  |  |  |  |  |  |  |  |  |  |  |
| `small_dripleaf` | grass / fern |  |  |  |  |  |  |  |  |  |  |  |
| `spore_blossom` | other |  |  |  |  |  |  |  |  |  |  |  |
| `spruce_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `sugar_cane` | cactus / cane | Y |  |  | Y |  |  |  |  |  |  | Y |
| `sunflower` | tall flower | Y |  |  | Y |  | Y | Y | Y |  |  | Y |
| `sweet_berries` | crop / seed | Y |  |  | Y |  | Y |  |  |  |  |  |
| `tall_grass` | grass / fern | Y |  |  | Y |  |  |  |  |  |  | Y |
| `tube_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `tube_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `twisting_vines` | vine | Y |  |  | Y |  | Y |  |  |  |  |  |
| `vine` | vine | Y |  |  | Y |  | Y |  |  |  |  |  |
| `warped_fungus` | mushroom | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `warped_roots` | grass / fern | Y |  |  | Y |  |  |  |  |  |  | Y |
| `weeping_vines` | vine | Y |  |  | Y |  | Y |  |  |  |  |  |
| `wheat_seeds` | crop / seed | Y |  |  | Y |  |  |  |  |  |  | Y |
| `white_tulip` | flower | Y |  |  | Y | Y | Y | Y |  | Y | Y | Y |
| `wither_rose` | flower | Y |  |  | Y | Y | Y | Y |  | Y | Y | Y |

### alexsmobs (2)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `banana_peel` | other |  |  |  |  |  |  |  |  |  |  | Y |
| `stink_bottle` | vine |  |  |  |  |  |  |  |  |  |  |  |

### architects_palette (1)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `twisted_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |

### berry_good (2)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `glow_berry_pips` | vine | Y |  |  |  |  |  |  |  |  |  |  |
| `sweet_berry_pips` | crop / seed | Y |  |  |  |  |  |  |  |  |  | Y |

### botania (203)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `agricarnation` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `agricarnation_chibi` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `bellethorn` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `bellethorn_chibi` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `bergamute` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `black_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `black_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `black_mushroom` | botania |  |  |  | Y | Y |  |  |  |  |  | Y |
| `black_mystical_flower` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `black_petal` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `black_shiny_flower` | botania |  |  |  |  |  |  |  |  |  | Y | Y |
| `blue_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `blue_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `blue_mushroom` | botania |  |  |  | Y | Y |  |  |  |  |  | Y |
| `blue_mystical_flower` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `blue_petal` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `blue_shiny_flower` | botania |  |  |  |  |  |  |  |  |  | Y | Y |
| `brown_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `brown_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `brown_mushroom` | botania |  |  |  | Y | Y |  |  |  |  |  | Y |
| `brown_mystical_flower` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `brown_petal` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `brown_shiny_flower` | botania |  |  |  |  |  |  |  |  |  | Y | Y |
| `bubbell` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `bubbell_chibi` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `clayconia` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `clayconia_chibi` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `cyan_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `cyan_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `cyan_mushroom` | botania |  |  |  | Y | Y |  |  |  |  |  | Y |
| `cyan_mystical_flower` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `cyan_petal` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `cyan_shiny_flower` | botania |  |  |  |  |  |  |  |  |  | Y | Y |
| `daffomill` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `dandelifeon` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `daybloom_motif` | botania |  |  |  |  |  |  |  |  |  | Y | Y |
| `dreadthorn` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `endoflame` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `entropinnyum` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `exoflame` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `fallen_kanade` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
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
| `gourmaryllis` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `gray_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `gray_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `gray_mushroom` | botania |  |  |  | Y | Y |  |  |  |  |  | Y |
| `gray_mystical_flower` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `gray_petal` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `gray_shiny_flower` | botania |  |  |  |  |  |  |  |  |  | Y | Y |
| `green_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `green_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `green_mushroom` | botania |  |  |  | Y | Y |  |  |  |  |  | Y |
| `green_mystical_flower` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `green_petal` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `green_shiny_flower` | botania |  |  |  |  |  |  |  |  |  | Y | Y |
| `heisei_dream` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `hopperhock` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `hopperhock_chibi` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `hyacidus` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `hydroangeas` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `hydroangeas_motif` | botania |  |  |  |  |  |  |  |  |  | Y | Y |
| `jaded_amaranthus` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `jiyuulia` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `jiyuulia_chibi` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `kekimurus` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `labellia` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `light_blue_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `light_blue_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `light_blue_mushroom` | botania |  |  |  | Y | Y |  |  |  |  |  | Y |
| `light_blue_mystical_flower` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `light_blue_petal` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `light_blue_shiny_flower` | botania |  |  |  |  |  |  |  |  |  | Y | Y |
| `light_gray_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `light_gray_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `light_gray_mushroom` | botania |  |  |  | Y | Y |  |  |  |  |  | Y |
| `light_gray_mystical_flower` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `light_gray_petal` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `light_gray_shiny_flower` | botania |  |  |  |  |  |  |  |  |  | Y | Y |
| `lime_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `lime_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `lime_mushroom` | botania |  |  |  | Y | Y |  |  |  |  |  | Y |
| `lime_mystical_flower` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `lime_petal` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `lime_shiny_flower` | botania |  |  |  |  |  |  |  |  |  | Y | Y |
| `loonium` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `magenta_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `magenta_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `magenta_mushroom` | botania |  |  |  | Y | Y |  |  |  |  |  | Y |
| `magenta_mystical_flower` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `magenta_petal` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `magenta_shiny_flower` | botania |  |  |  |  |  |  |  |  |  | Y | Y |
| `manastar` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `marimorphosis` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `marimorphosis_chibi` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `medumone` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `munchdew` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `narslimmus` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `nightshade_motif` | botania |  |  |  |  |  |  |  |  |  | Y | Y |
| `orange_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `orange_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `orange_mushroom` | botania |  |  |  | Y | Y |  |  |  |  |  | Y |
| `orange_mystical_flower` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `orange_petal` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `orange_shiny_flower` | botania |  |  |  |  |  |  |  |  |  | Y | Y |
| `orechid` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `orechid_ignem` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `pink_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `pink_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `pink_mushroom` | botania |  |  |  | Y | Y |  |  |  |  |  | Y |
| `pink_mystical_flower` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `pink_petal` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `pink_shiny_flower` | botania |  |  |  |  |  |  |  |  |  | Y | Y |
| `pollidisiac` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `pure_daisy` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `purple_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `purple_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `purple_mushroom` | botania |  |  |  | Y | Y |  |  |  |  |  | Y |
| `purple_mystical_flower` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `purple_petal` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `purple_shiny_flower` | botania |  |  |  |  |  |  |  |  |  | Y | Y |
| `rafflowsia` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `rannuncarpus` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `rannuncarpus_chibi` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `red_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `red_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `red_mushroom` | botania |  |  |  | Y | Y |  |  |  |  |  | Y |
| `red_mystical_flower` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `red_petal` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `red_shiny_flower` | botania |  |  |  |  |  |  |  |  |  | Y | Y |
| `rosa_arcana` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `shulk_me_not` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `solegnolia` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `solegnolia_chibi` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `spectranthemum` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `spectrolus` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `tangleberrie` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `tangleberrie_chibi` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `thermalily` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `tigerseye` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `vinculotus` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `white_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `white_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `white_mushroom` | botania |  |  |  | Y | Y |  |  |  |  |  | Y |
| `white_mystical_flower` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `white_petal` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `white_shiny_flower` | botania |  |  |  |  |  |  |  |  |  | Y | Y |
| `yellow_double_flower` | botania |  |  |  | Y |  |  |  | Y |  |  | Y |
| `yellow_floating_flower` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `yellow_mushroom` | botania |  |  |  | Y | Y |  |  |  |  |  | Y |
| `yellow_mystical_flower` | botania |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `yellow_petal` | botania |  |  |  |  |  |  |  |  |  |  | Y |
| `yellow_shiny_flower` | botania |  |  |  |  |  |  |  |  |  | Y | Y |

### bushierflowers (13)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `grown_allium` | flower | Y |  |  | Y | Y |  |  |  | Y | Y | Y |
| `grown_azure_bluet` | flower | Y |  |  | Y | Y |  |  |  | Y | Y | Y |
| `grown_blue_orchid` | flower | Y |  |  | Y | Y |  |  |  | Y | Y | Y |
| `grown_cornflower` | flower | Y |  |  | Y | Y |  |  |  | Y | Y | Y |
| `grown_dandelion` | flower | Y |  |  | Y | Y |  |  |  | Y | Y | Y |
| `grown_lily` | flower | Y |  |  | Y | Y |  |  |  | Y | Y | Y |
| `grown_orange_tulip` | flower | Y |  |  | Y | Y |  |  |  | Y | Y | Y |
| `grown_oxeye_daisy` | flower | Y |  |  | Y | Y |  |  |  | Y | Y | Y |
| `grown_pink_tulip` | flower | Y |  |  | Y | Y |  |  |  | Y | Y | Y |
| `grown_poppy` | flower | Y |  |  | Y | Y |  |  |  | Y | Y | Y |
| `grown_red_tulip` | flower | Y |  |  | Y | Y |  |  |  | Y | Y | Y |
| `grown_white_tulip` | flower | Y |  |  | Y | Y |  |  |  | Y | Y | Y |
| `grown_wither_rose` | flower | Y |  |  | Y | Y |  |  |  | Y | Y | Y |

### buzzier_bees (3)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `buttercup` | flower | Y |  |  |  |  |  |  |  |  | Y | Y |
| `pink_clover` | flower | Y |  |  | Y | Y |  |  |  | Y | Y | Y |
| `white_clover` | flower | Y |  |  | Y | Y |  |  |  | Y | Y | Y |

### corn_delight (2)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `corn_seeds` | crop / seed |  |  |  |  |  |  |  |  |  |  | Y |
| `wild_corn` | flower |  |  |  |  |  |  |  |  |  |  | Y |

### delightful (5)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `cantaloupe` | crop / seed |  |  |  |  |  |  |  |  |  |  | Y |
| `cantaloupe_seeds` | crop / seed |  |  |  |  |  |  |  |  |  |  | Y |
| `mini_melon` | crop / seed |  |  |  |  |  |  |  |  |  |  | Y |
| `salmonberry_pips` | other |  |  |  |  |  |  |  |  |  |  | Y |
| `wild_salmonberries` | flower |  |  |  | Y | Y |  |  |  | Y |  | Y |

### endercrop (1)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `ender_seeds` | crop / seed |  |  |  |  |  |  |  |  |  |  |  |

### endersdelight (3)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `amberveil` | other |  |  |  |  |  |  |  |  |  |  |  |
| `chorusflame` | cactus / cane |  |  |  |  |  |  |  |  |  |  |  |
| `ethereal_saffron` | other |  |  |  |  |  |  |  |  |  |  |  |

### ends_delight (1)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `chorus_succulent` | cactus / cane |  |  |  |  |  |  |  |  |  |  |  |

### farmersdelight (14)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `brown_mushroom_colony` | mushroom |  |  |  | Y |  |  |  |  |  |  | Y |
| `cabbage_seeds` | crop / seed |  |  |  |  |  |  |  |  |  |  | Y |
| `onion` | crop / seed |  |  |  |  |  |  |  |  |  |  | Y |
| `red_mushroom_colony` | mushroom |  |  |  | Y |  |  |  |  |  |  | Y |
| `rice` | other |  |  |  |  |  |  |  |  |  |  |  |
| `sandy_shrub` | other |  |  |  |  |  |  |  |  |  |  | Y |
| `tomato_seeds` | crop / seed |  |  |  |  |  |  |  |  |  |  | Y |
| `wild_beetroots` | flower |  |  |  | Y | Y |  |  |  | Y |  | Y |
| `wild_cabbages` | flower |  |  |  | Y | Y |  |  |  | Y |  | Y |
| `wild_carrots` | flower |  |  |  | Y | Y |  |  |  | Y |  | Y |
| `wild_onions` | flower |  |  |  | Y | Y |  |  |  | Y |  | Y |
| `wild_potatoes` | flower |  |  |  | Y | Y |  |  |  | Y |  | Y |
| `wild_rice` | tall flower |  |  |  | Y |  |  |  | Y |  |  |  |
| `wild_tomatoes` | flower |  |  |  | Y | Y |  |  |  | Y |  | Y |

### farmersrespite (4)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `coffee_beans` | crop / seed | Y |  |  |  |  |  |  |  |  |  |  |
| `tea_seeds` | crop / seed | Y |  |  |  |  |  |  |  |  |  |  |
| `wild_coffee_bush` | grass / fern | Y |  |  |  |  |  |  |  |  |  |  |
| `wild_tea_bush` | grass / fern | Y |  |  |  |  |  |  |  |  |  | Y |

### fireflybushport (1)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `firefly_bush` | flower |  |  |  |  |  |  |  |  |  | Y | Y |

### floralench (34)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `allium_bush` | vine |  |  |  |  |  |  |  |  |  |  | Y |
| `allium_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `azure_bluet_bush` | vine |  |  |  |  |  |  |  |  |  |  | Y |
| `azure_bluet_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `blue_orchid_bush` | vine |  |  |  |  |  |  |  |  |  |  | Y |
| `blue_orchid_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `cornflower_bush` | vine |  |  |  |  |  |  |  |  |  |  | Y |
| `cornflower_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `dandelion_bush` | vine |  |  |  |  |  |  |  |  |  |  | Y |
| `dandelion_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `lilac_bush` | vine |  |  |  |  |  |  |  |  |  |  | Y |
| `lilac_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `lily_of_the_valley_bush` | vine |  |  |  |  |  |  |  |  |  |  | Y |
| `lily_of_the_valley_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `orange_tulip_bush` | vine |  |  |  |  |  |  |  |  |  |  | Y |
| `orange_tulip_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `oxeye_daisy_bush` | vine |  |  |  |  |  |  |  |  |  |  | Y |
| `oxeye_daisy_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `peony_bush` | vine |  |  |  |  |  |  |  |  |  |  | Y |
| `peony_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `pink_tulip_bush` | vine |  |  |  |  |  |  |  |  |  |  | Y |
| `pink_tulip_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `poppy_bush` | vine |  |  |  |  |  |  |  |  |  |  | Y |
| `poppy_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `red_tulip_bush` | vine |  |  |  |  |  |  |  |  |  |  | Y |
| `red_tulip_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `rose_bush_bush` | vine |  |  |  |  |  |  |  |  |  |  | Y |
| `rose_bush_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `sunflower_bush` | vine |  |  |  |  |  |  |  |  |  |  | Y |
| `sunflower_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `white_tulip_bush` | vine |  |  |  |  |  |  |  |  |  |  | Y |
| `white_tulip_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `wither_rose_bush` | vine |  |  |  |  |  |  |  |  |  |  | Y |
| `wither_rose_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |

### fruitsdelight (22)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `apple_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `bayberry_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `blueberry` | other |  |  |  |  |  |  |  |  |  |  | Y |
| `blueberry_bush` | other |  |  |  |  |  |  |  |  |  |  | Y |
| `cranberry` | other |  |  |  |  |  |  |  |  |  |  | Y |
| `durian_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `fig_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `hamimelon` | crop / seed |  | Y |  |  |  |  |  |  |  |  | Y |
| `hamimelon_seeds` | crop / seed |  |  |  |  |  |  |  |  |  |  | Y |
| `hawberry_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `kiwi_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `lemon_seeds` | grass / fern |  |  |  |  |  |  |  |  |  |  | Y |
| `lemon_tree` | grass / fern |  |  |  |  |  |  |  |  |  |  | Y |
| `lychee_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `mango_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `mangosteen_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `orange_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `peach_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `pear_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `persimmon_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `pineapple_sapling` | other |  |  |  |  |  |  |  |  |  |  |  |
| `wild_pineapple` | other |  |  |  |  |  |  |  |  |  |  |  |

### fungidelight (4)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `inky_cap_mushroom` | mushroom |  |  |  | Y | Y |  |  |  |  |  | Y |
| `inky_cap_mushroom_colony` | mushroom |  |  |  | Y | Y |  |  |  |  |  | Y |
| `morel_mushroom` | mushroom |  |  |  | Y | Y |  |  |  |  |  | Y |
| `morel_mushroom_colony` | mushroom |  |  |  | Y | Y |  |  |  |  |  | Y |

### hearthandharvest (1)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `raspberry_pips` | crop / seed |  |  |  |  |  |  |  |  |  |  | Y |

### immersive_weathering (9)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `azalea_flower_pile` | flower |  |  |  | Y |  |  |  |  |  |  |  |
| `flowering_azalea_leaf_pile` | flower |  |  |  | Y |  |  |  |  |  |  |  |
| `frost` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `frosty_fern` | grass / fern |  |  |  |  |  |  |  |  |  |  | Y |
| `frosty_grass` | grass / fern |  |  |  |  |  |  |  |  |  |  | Y |
| `ivy` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `moss_clump` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `soot` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `weeds` | crop / seed |  |  |  |  |  |  |  |  |  |  |  |

### immersiveengineering (1)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `seed` | crop / seed | Y |  |  |  |  |  |  |  |  |  | Y |

### miners_delight (3)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `cave_carrot` | crop / seed |  |  |  |  |  |  |  |  |  |  | Y |
| `gossypium` | flower | Y |  |  |  |  |  |  |  |  |  | Y |
| `wild_cave_carrots` | flower |  |  |  |  |  |  |  |  |  |  | Y |

### myrtrees (1)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `rubberwood_sapling` | sapling |  |  |  | Y | Y |  |  |  |  |  | Y |

### neapolitan (3)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `banana_frond` | other | Y |  |  | Y |  |  |  |  |  |  |  |
| `mint_sprout` | other | Y |  |  | Y |  |  |  |  |  |  | Y |
| `strawberry_pips` | other |  |  |  |  |  |  |  |  |  |  | Y |

### nethersdelight (4)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `crimson_fungus_colony` | mushroom |  |  |  |  |  |  |  |  |  |  | Y |
| `mimicarnation` | flower |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `propelplant_cane` | other |  |  |  |  |  |  |  |  |  |  |  |
| `warped_fungus_colony` | mushroom |  |  |  |  |  |  |  |  |  |  | Y |

### quark (10)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `ancient_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `blue_blossom_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `cut_vine` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `glow_lichen_growth` | vine | Y |  |  |  | Y |  |  |  |  |  | Y |
| `glow_shroom` | mushroom | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `lavender_blossom_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `orange_blossom_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `pink_blossom_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `red_blossom_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `yellow_blossom_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |

### rusticdelight (10)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `bell_pepper_seeds` | crop / seed |  |  |  |  |  |  |  |  |  |  | Y |
| `coffee_beans` | crop / seed |  |  |  |  |  |  |  |  |  |  | Y |
| `cotton_seeds` | crop / seed |  |  |  |  |  |  |  |  |  |  | Y |
| `dark_bell_pepper_seeds` | crop / seed |  |  |  |  |  |  |  |  |  |  | Y |
| `pale_bell_pepper_seeds` | crop / seed |  |  |  |  |  |  |  |  |  |  | Y |
| `wild_bell_peppers` | flower | Y |  |  | Y | Y |  |  |  | Y |  | Y |
| `wild_coffee` | flower | Y |  |  | Y | Y |  |  |  | Y |  | Y |
| `wild_cotton` | flower | Y |  |  | Y | Y |  |  |  | Y |  | Y |
| `wild_dark_bell_peppers` | flower | Y |  |  | Y | Y |  |  |  | Y |  | Y |
| `wild_pale_bell_peppers` | flower | Y |  |  | Y | Y |  |  |  | Y |  | Y |

### supplementaries (2)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `flax_seeds` | crop / seed | Y |  |  | Y |  |  |  |  |  |  | Y |
| `wild_flax` | flower |  |  |  | Y |  |  |  |  |  |  | Y |

### twigs (3)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `azalea_flowers` | flower | Y |  |  | Y | Y |  |  |  | Y |  |  |
| `bamboo_leaves` | cactus / cane |  |  |  |  |  |  |  |  |  |  | Y |
| `petrified_lichen` | vine |  |  |  |  |  |  |  |  |  |  |  |

### upgrade_aquatic (57)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `acan_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `acan_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `beachgrass` | grass / fern |  |  |  | Y |  |  |  |  |  |  |  |
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
| `pink_searocket` | flower | Y |  |  | Y | Y |  |  |  | Y |  | Y |
| `polar_kelp` | water |  |  |  | Y |  |  |  |  |  |  |  |
| `prismarine_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `prismarine_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `prismarine_coral_shower` | water |  |  |  |  |  |  |  |  |  |  |  |
| `river_leaf_pile` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `river_sapling` | sapling | Y |  |  | Y | Y |  |  |  |  |  | Y |
| `rock_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `rock_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `silk_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `silk_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `star_coral` | water |  |  |  |  |  |  |  |  |  |  |  |
| `star_coral_fan` | water |  |  |  |  |  |  |  |  |  |  |  |
| `tall_beachgrass` | grass / fern |  |  |  | Y |  |  |  |  |  |  |  |
| `thorny_kelp` | water |  |  |  | Y |  |  |  |  |  |  |  |
| `tongue_kelp` | water |  |  |  | Y |  |  |  |  |  |  |  |
| `white_searocket` | flower | Y |  |  | Y | Y |  |  |  | Y |  | Y |

### veggiesdelight (13)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `broccoli_seeds` | crop / seed |  |  |  |  |  |  |  |  |  |  | Y |
| `cauliflower_seeds` | crop / seed |  |  |  |  |  |  |  |  |  |  | Y |
| `garlic_clove` | crop / seed |  |  |  |  |  |  |  |  |  |  | Y |
| `mature_dandelion` | flower |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `sweet_potato` | crop / seed |  |  |  |  |  |  |  |  |  |  | Y |
| `turnip_seeds` | crop / seed |  |  |  |  |  |  |  |  |  |  | Y |
| `wild_broccoli` | flower |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `wild_cauliflowers` | flower |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `wild_garlic` | flower |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `wild_sweet_potatoes` | flower |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `wild_turnips` | flower |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `wild_zucchinis` | flower |  |  |  | Y | Y |  |  |  | Y | Y | Y |
| `zucchini_seeds` | crop / seed |  |  |  |  |  |  |  |  |  |  | Y |

### vinery (12)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `apple_tree_sapling` | sapling |  |  |  | Y | Y |  |  |  |  |  | Y |
| `cherry_sapling` | sapling |  |  |  | Y | Y |  |  |  |  |  | Y |
| `grapevine_lattice` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `grapevine_stem` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `jungle_grape_seeds_red` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `jungle_grape_seeds_white` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `red_grape_seeds` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `savanna_grape_seeds_red` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `savanna_grape_seeds_white` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `taiga_grape_seeds_red` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `taiga_grape_seeds_white` | vine |  |  |  |  |  |  |  |  |  |  |  |
| `white_grape_seeds` | vine |  |  |  |  |  |  |  |  |  |  |  |

### vintagedelight (10)

| Plant | Kind | Flower pot | Framed pot | Hanging pot (Supp.) | Flower box (Supp.) | Planter box (AF) | Hanging pot (Beautify) | Large pot (Floral) | Vinery pot | Vinery box | Silt pot (Twigs) | Planter (Supp.) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `cucumber_seeds` | crop / seed |  |  |  |  |  |  |  |  |  |  | Y |
| `ghost_pepper` | crop / seed |  |  |  |  |  |  |  |  |  |  |  |
| `ghost_pepper_seeds` | crop / seed |  |  |  |  |  |  |  |  |  |  |  |
| `magic_peanut` | sapling |  |  |  | Y | Y |  |  |  |  |  | Y |
| `oat_seeds` | crop / seed |  |  |  |  |  |  |  |  |  |  | Y |
| `peanut` | crop / seed |  |  |  |  |  |  |  |  |  |  | Y |
| `wild_cucumbers` | flower |  |  |  | Y | Y |  |  |  | Y |  | Y |
| `wild_ghost_peppers` | flower |  |  |  | Y | Y |  |  |  | Y |  |  |
| `wild_oats` | flower |  |  |  | Y | Y |  |  |  | Y |  | Y |
| `wild_peanuts` | flower |  |  |  | Y | Y |  |  |  | Y |  | Y |

