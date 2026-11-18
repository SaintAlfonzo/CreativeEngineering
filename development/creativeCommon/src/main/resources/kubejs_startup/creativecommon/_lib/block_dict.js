// priority: 1000
// Must load before every other startup script: several read global.item / blocks / fluid at
// top level, and same-priority scripts load in filesystem-walk order (differs per machine).
global.blocks = {};

// ############################## Assignments ##############################
var b = global.blocks;

b.dark_fire = 'creativemagic:dark_fire';
b.hh_four_leaf_clover = 'hearthandharvest:four_leaf_clover'; // placed Four-Leaf Clover (hearth/worldgen), no item of its own
b.sunflower_stem = 'hearthandharvest:sunflower_stem'; // Sunflower Stem block (HHBlocks), no item; was in item_dict (SQ-AZW-01)
b.sweet_berry_bush = 'minecraft:sweet_berry_bush'; // block id (the item is sweet_berries)
b.emu_nest = 'creativedelight:emu_nest';         // Creative Delight compat/incubation/nest/EmuNests.java
b.emu_egg_nest = 'creativedelight:emu_egg_nest'; // the 1-6 egg state; no item
b.duck_nest = 'creativedelight:duck_nest';         // Creative Delight compat/incubation/nest/WaterfowlNests.java
b.duck_egg_nest = 'creativedelight:duck_egg_nest'; // the 1-6 egg state; no item
b.goose_nest = 'creativedelight:goose_nest';
b.goose_egg_nest = 'creativedelight:goose_egg_nest';
b.duck_egg_crate = 'creativedelight:duck_egg_crate';   // Creative Delight compat/incubation/crate/WaterfowlEggCrates.java
b.goose_egg_crate = 'creativedelight:goose_egg_crate';
b.oven = 'creativedelight:oven';                 // Creative Common oven/OvenBlock.java
// Oven heat (feature_writeup/oven.md 2.4): non-stove sources below the oven
b.magma_block = 'minecraft:magma_block';
b.lava_cauldron = 'minecraft:lava_cauldron';
b.blaze_burner = 'create:blaze_burner';             // counts only with blaze != none (OvenHeat.isActive)
b.lit_blaze_burner = 'create:lit_blaze_burner';
b.blaze_lantern = 'quark:blaze_lantern';
b.cutting_board = 'farmersdelight:cutting_board';  // bakeware hammering (feature_writeup/oven.md 2.5)
b.cooking_pot = 'farmersdelight:cooking_pot';      // cookware pickup (cooking/kitchen #27)
b.skillet = 'farmersdelight:skillet';
b.copper_pot = 'miners_delight:copper_pot';
b.kettle = 'farmersrespite:kettle';
b.t_cookware_pickup = '#creativedelight:cookware_pickup';   // CD CookwareHandling: sneak right-click picks up, sneak left-click toggles the handle
b.stone_cutting_boards = ['farmersdelight:granite_cutting_board', 'farmersdelight:andesite_cutting_board', 'farmersdelight:diorite_cutting_board'];  // cooking/kitchen #25
b.wood_cutting_boards = ['farmersdelight:oak_cutting_board', 'farmersdelight:birch_cutting_board', 'farmersdelight:jungle_cutting_board', 'farmersdelight:acacia_cutting_board', 'farmersdelight:dark_oak_cutting_board', 'farmersdelight:mangrove_cutting_board', 'farmersdelight:crimson_cutting_board', 'farmersdelight:warped_cutting_board'];  // cooking/kitchen #38
b.cfb_counter = 'cookingforblockheads:counter';   // 15 px worktops: cutting board support (TODO cooking/kitchen #8)
b.cfb_corner = 'cookingforblockheads:corner';
// Counter toppers (Creative Common CounterToppers, TODO cooking/kitchen #20): plates, plated foods, coaster on a counter
b.dd_food_plate = 'displaydelight:food_plate';
// FD 1.3.4 backports in the Creative Delight repack (farmers_delight.md 3); unresolved until CD is installed
b.fd_pumpkin_pie = 'farmersdelight:pumpkin_pie';        // placed vanilla pie; no item ([pumpkin_pie] placeAs)
b.rope_fence = 'farmersdelight:rope_fence';
b.rope_fence_gate = 'farmersdelight:rope_fence_gate';
b.gleaming_salad_block = 'farmersdelight:gleaming_salad_block';
b.hanging_canvas_sign = 'farmersdelight:hanging_canvas_sign';
b.dd_small_food_plate = 'displaydelight:small_food_plate';
b.dd_plated_foods = 'displaydelight:*plated_*';    // every plated / small_plated food block (ctd_plated_*, plated_* ...)
b.coaster = 'brewinandchewin:coaster';               // the Brewin' port
// Milk and cow jars: glass bottles take milk out (kubejs/data/creativedelight/kubejs/cookingforblockheads/milk_jar.js, TODO food/drinks #6)
b.cow_jar = 'cookingforblockheads:cow_jar';
b.milk_jar = 'cookingforblockheads:milk_jar';
// Campfire rain (CD campfire/CampfireRain.java, keys in Common campfire/CampfireRainInfo.java)
b.campfire = 'minecraft:campfire';
b.coal_block = 'minecraft:coal_block';
b.quark_charcoal_block = 'quark:charcoal_block';
b.architects_palette_charcoal_block = 'architects_palette:charcoal_block';
// Keg + wine storage temperature (Brewin port BnCHeatSources): heat counts only while lit/active/on, blaze heaters
// only when fueled; IE machines flip `active` on the multiblock master only
b.furnace = 'minecraft:furnace';
b.blast_furnace = 'minecraft:blast_furnace';
b.smoker = 'minecraft:smoker';
b.quark_deepslate_furnace = 'quark:deepslate_furnace';
b.quark_blackstone_furnace = 'quark:blackstone_furnace';
b.keypad_furnace = 'securitycraft:keypad_furnace';
b.keypad_blast_furnace = 'securitycraft:keypad_blast_furnace';
b.keypad_smoker = 'securitycraft:keypad_smoker';
b.bonfire = 'decorative_blocks:bonfire';               // a fire block: only exists while burning
b.soul_bonfire = 'decorative_blocks:soul_bonfire';
b.brazier = 'decorative_blocks:brazier';
b.soul_brazier = 'decorative_blocks:soul_brazier';
b.siege_brazier_on = 'blocky_siege:brazier_on';        // the unlit one is a separate block
b.ie_coke_oven = 'immersiveengineering:coke_oven';
b.ie_blast_furnace = 'immersiveengineering:blast_furnace';
b.ie_alloy_smelter = 'immersiveengineering:alloy_smelter';
b.ie_furnace_heater = 'immersiveengineering:furnace_heater';
b.small_lab_furnace = 'engineersdecor:small_lab_furnace';
b.small_electrical_furnace = 'engineersdecor:small_electrical_furnace';
b.small_waste_incinerator = 'engineersdecor:small_waste_incinerator';
b.clayworks_kiln = 'clayworks:kiln';
b.air_compressor = 'pneumaticcraft:air_compressor';
b.advanced_air_compressor = 'pneumaticcraft:advanced_air_compressor';
b.vibration_chamber = 'ae2:vibration_chamber';
b.liquid_blaze_burner = 'createaddition:liquid_blaze_burner';
b.blaze_enchanter = 'create_enchantment_industry:blaze_enchanter';
b.botania_blaze_block = 'botania:blaze_block';       // Blaze Mesh fuel block, always hot
b.snow = 'minecraft:snow';                             // snow layers
b.snow_block = 'minecraft:snow_block';
b.powder_snow = 'minecraft:powder_snow';
b.powdered_sugar_block = 'creativedelight:powdered_sugar_block'; // Creative Common food/PowderedSugarBlock.java
b.thin_ice = 'immersive_weathering:thin_ice';
b.iw_permafrost = 'immersive_weathering:permafrost';
b.iw_cryosol = 'immersive_weathering:cryosol';
b.soul_soil = 'minecraft:soul_soil';
b.rich_soul_soil = 'nethersdelight:rich_soul_soil';
b.potted_mimicarnation = 'nethersdelight:potted_mimicarnation'; // CD repackedNethers (Nether's Delight 4.0 block)
b.t_flower_pots = '#minecraft:flower_pots';
b.iw_nulch = 'immersive_weathering:nulch_block';
b.polished_packed_ice = 'architects_palette:polished_packed_ice';
b.chiseled_packed_ice = 'architects_palette:chiseled_packed_ice';
// Fishing net blocks with no item (block tags in server_scripts/tags/loottags.js; ocean_delight.md 5.1)
b.kelp_plant = 'minecraft:kelp_plant';
b.tall_seagrass = 'minecraft:tall_seagrass';
b.ochre_kelp_plant = 'upgrade_aquatic:ochre_kelp_plant';
b.polar_kelp_plant = 'upgrade_aquatic:polar_kelp_plant';
b.thorny_kelp_plant = 'upgrade_aquatic:thorny_kelp_plant';
b.tongue_kelp_plant = 'upgrade_aquatic:tongue_kelp_plant';
b.corn_crop = 'corn_delight:corn_crop';            // knife harvest Corncob (CD corn_delight/loot.js)
b.tall_blue_pickerelweed ='upgrade_aquatic:tall_blue_pickerelweed';
b.tall_purple_pickerelweed = 'upgrade_aquatic:tall_purple_pickerelweed';
// Crops with their own ripe right-click pick (crops.md 2.2, Creative Common QuarkOwnPickMixin)
b.cucumber_crop = 'vintagedelight:cucumber_crop';
b.ghost_pepper_crop = 'vintagedelight:ghost_pepper_crop';
b.zucchini_crop = 'veggiesdelight:zucchini_crop';
// Vine crops no block placement can replace (CD crops/VineCropReplace, tag creativedelight:crops/vine_crops in CD
// crop_tags.js; plan_post_scratch_1115 C2, SQ-HOP-04). No items of their own; unresolved when their mod is absent
b.t_vine_crops = '#creativedelight:crops/vine_crops';
b.hops_vine = 'hearthandharvest:hops_vine';
b.jungle_grape_bush_red = 'vinery:jungle_grape_bush_red';
b.jungle_grape_bush_white = 'vinery:jungle_grape_bush_white';
b.vanilla_vine = 'neapolitan:vanilla_vine';
b.vanilla_vine_plant = 'neapolitan:vanilla_vine_plant';
b.mulberry_vine = 'upgrade_aquatic:mulberry_vine';
// Crop blocks with a soil yield row (CD creativeengineering/soil_yield.js, feature_writeup/crop_yields.md)
b.wheat_crop = 'minecraft:wheat';
b.carrots = 'minecraft:carrots';
b.potatoes = 'minecraft:potatoes';
b.beetroots = 'minecraft:beetroots';
b.cabbages = 'farmersdelight:cabbages';
b.onions = 'farmersdelight:onions';
b.tomatoes = 'farmersdelight:tomatoes';
b.stick_tomatoes = 'supplementaries:stick_tomatoes'; // FD tomato vine on a Supplementaries stick
b.rope_tomatoes = 'supplementaries:rope_tomatoes'; // FD tomato vine on a Supplementaries rope
// Placed treated sticks (CD treatedstick/, feature_writeup/treated_sticks.md); unresolved until CD is installed
b.treated_stick = 'creativedelight:treated_stick';
b.treated_stick_tomatoes = 'creativedelight:treated_stick_tomatoes'; // FD tomato vine on a treated stick
b.rice_panicles = 'farmersdelight:rice_panicles';
b.bell_peppers = 'rusticdelight:bell_peppers';
b.coffee_crop = 'rusticdelight:coffee';
b.cotton_crop = 'rusticdelight:cotton';
b.glowing_calamari_crate = 'rusticdelight:glowing_calamari_crate';   // the port's own crate (creativedelight/members #25)
b.ender_crop = 'endercrop:ender_crop';
b.tobacco_crop = 'the_dirty_stuff:tobacco_seeds';     // the tobacco plant (its seed item has the same id)
b.flax_crop = 'supplementaries:flax';
b.hemp = 'immersiveengineering:hemp';
b.cave_carrots = 'miners_delight:cave_carrots';
b.broccoli_crop = 'veggiesdelight:broccoli_crop';
b.cauliflower_crop = 'veggiesdelight:cauliflower_crop';
b.garlic_crop = 'veggiesdelight:garlic_crop';
b.sweet_potato_crop = 'veggiesdelight:sweet_potato_crop';
b.turnip_crop = 'veggiesdelight:turnip_crop';
b.oat_crop = 'vintagedelight:oat_crop';
b.peanut_crop = 'vintagedelight:peanut_crop';
b.adzuki_sprouts = 'neapolitan:adzuki_sprouts';
// Bushes and nether crops with a soil yield row (wave 2, plan_crop_yields_wave2.md 5.2 / 6)
b.strawberry_bush = 'neapolitan:strawberry_bush'; // item is strawberry_pips
b.blueberry_bush = 'fruitsdelight:blueberry_bush';
b.cranberry_bush = 'fruitsdelight:cranberry_bush';
b.lemon_tree = 'fruitsdelight:lemon_tree';
b.nether_wart_crop = 'minecraft:nether_wart'; // the planted wart (same id as the item)
b.monster_box = 'quark:monster_box';             // vault box (mob_block_tweaks.md 2.9): obsidian hardness, sneak to break
// Barbeque's Delight port (Creative Delight member barbequesdelight, barbeques_delight.md 4)
b.bbq_grill = 'barbequesdelight:grill';   // has_campfire / lit / waterlogged; in #creativecommon:campfire/rain_douses
b.bbq_basin = 'barbequesdelight:basin';   // "Ingredients Basin", 2 slots
b.bbq_tray = 'barbequesdelight:tray';     // 3 slots, stacks on any tray; basin and tray are oak, other woods bbq_<wood>_*
b.bbq_spruce_basin = 'barbequesdelight:spruce_basin';
b.bbq_birch_basin = 'barbequesdelight:birch_basin';
b.bbq_jungle_basin = 'barbequesdelight:jungle_basin';
b.bbq_acacia_basin = 'barbequesdelight:acacia_basin';
b.bbq_dark_oak_basin = 'barbequesdelight:dark_oak_basin';
b.bbq_mangrove_basin = 'barbequesdelight:mangrove_basin';
b.bbq_crimson_basin = 'barbequesdelight:crimson_basin';
b.bbq_warped_basin = 'barbequesdelight:warped_basin';
b.bbq_spruce_tray = 'barbequesdelight:spruce_tray';
b.bbq_birch_tray = 'barbequesdelight:birch_tray';
b.bbq_jungle_tray = 'barbequesdelight:jungle_tray';
b.bbq_acacia_tray = 'barbequesdelight:acacia_tray';
b.bbq_dark_oak_tray = 'barbequesdelight:dark_oak_tray';
b.bbq_mangrove_tray = 'barbequesdelight:mangrove_tray';
b.bbq_crimson_tray = 'barbequesdelight:crimson_tray';
b.bbq_warped_tray = 'barbequesdelight:warped_tray';
// Roll Delight port (Creative Delight member `roll`, roll_delight.md 3.2); unresolved until CD carries the member
b.legendary_roll_medley = 'roll_delight:legendary_roll_medley';   // FeastBlock, 8 servings
b.mythical_roll_medley = 'roll_delight:mythical_roll_medley';     // FeastBlock, 8 servings
b.wrapped_chopsticks = 'roll_delight:wrapped_chopsticks';         // holds up to 4 chopsticks
// Large Meals port (Creative Delight member `largemeals`, large_meals.md 3.2); unresolved until CD carries the member
b.omurice_block = 'largemeals:omurice_block';                       // FeastBlock, 4 servings
b.mushroom_pot_pie_block = 'largemeals:mushroom_pot_pie_block';     // FeastBlock, 4 servings
b.roasted_mutton_rack_block = 'largemeals:roasted_mutton_rack_block'; // FeastBlock, 4 servings

// Tags
b.any_bricks = '#minecraft:bricks';
b.t_oven_heat_sources = '#creativedelight:oven_heat_sources'; // OvenHeat.OVEN_HEAT_SOURCES
b.t_fd_heat_sources = '#farmersdelight:heat_sources';
b.t_fd_tray_heat_sources = '#farmersdelight:tray_heat_sources';   // fire, campfires, lava
b.t_fd_mineable_knife = '#farmersdelight:mineable/knife';
b.t_blockrunner_confectionery = '#blockrunner:surface/confectionery';   // BlockRunner speed tier of feasts and cakes (blocktags.js)
b.t_blockrunner_metal = '#blockrunner:surface/metal';                 // BlockRunner metal tier (blocktags.js runner.metal)
b.t_blockrunner_wood = '#blockrunner:surface/wood';                   // BlockRunner wood tier (blocktags.js runner.wood)
b.t_forge_fences = '#forge:fences';
b.t_forge_fence_gates = '#forge:fence_gates';
b.t_keg_heat_sources = '#brewinandchewin:heat_sources';     // Brewin port: keg + Vinery cold storage; holds FD heat_sources
b.t_keg_freeze_sources = '#brewinandchewin:freeze_sources';
b.t_own_pick_crops = '#creativedelight:crops/own_pick'; // CropParity.OWN_PICK_CROPS: Quark leaves these to their own pick
b.t_harvest_from_bottom = '#creativedelight:crops/harvest_from_bottom'; // CropParity.HARVEST_FROM_BOTTOM: Quark harvests the lower half when the top half is clicked
b.t_ghost_pepper_soil = '#vintagedelight:ghost_pepper_soil'; // Vintage port: where ghost pepper seeds plant (vintage_delight.md 2.1)
b.t_create_non_movable = '#create:non_movable';    // contraptions can't pick these up
b.t_ice = '#minecraft:ice';                          // ice, packed, blue, frosted, rare-ice
b.t_hammer_no_rotate = '#creativedelight:hammer_no_rotate'; // IECompat.HAMMER_NO_ROTATE: the IE hammer won't turn these
b.t_end_plant_soil_slow = '#creativedelight:end_plant_soil_slow'; // enders_additions: End plants survive here, grow slowly (ends_delight.md 3.1)
b.t_end_plant_soil_fast = '#creativedelight:end_plant_soil_fast'; // same, grow fast
b.t_mineable_axe = '#minecraft:mineable/axe';
b.t_mineable_pickaxe = '#minecraft:mineable/pickaxe';
b.t_climbable = '#minecraft:climbable';
b.t_nether_carver_replaceables = '#minecraft:nether_carver_replaceables'; // Nether caves carve these (Visceral Heap flesh, creativeworldgen tags/visceral_heap.js)

// Visceral Heap blocks kept from Biomes O' Plenty (visceral_heap.md)
b.flesh = 'biomesoplenty:flesh';
b.porous_flesh = 'biomesoplenty:porous_flesh';
b.flesh_tendons = 'biomesoplenty:flesh_tendons';
b.flesh_tendons_strand = 'biomesoplenty:flesh_tendons_strand';
b.eyebulb = 'biomesoplenty:eyebulb';

// End food blocks (ends_delight.md 3.1)
b.end_stone = 'minecraft:end_stone';
b.tilled_end_stone = 'endercrop:tilled_end_stone';
b.chorus_crate = 'endersdelight:chorus_crate';
b.endstone_stove = 'endersdelight:endstone_stove';
b.chorus_fruit_crate = 'ends_delight:chorus_fruit_crate';
b.t_counter_supports = '#creativedelight:counter_supports'; // CounterToppers.SUPPORTS: toppers stay on these, drawn shifted
b.t_counter_toppers = '#creativedelight:counter_toppers';   // CounterToppers.TOPPERS: cutting board, plates, coaster
b.t_campfire_rain_douses = '#creativecommon:campfire/rain_douses';         // CampfireRain.RAIN_DOUSES
b.t_campfire_rain_proof_fuel = '#creativecommon:campfire/rain_proof_fuel'; // CampfireRain.RAIN_PROOF_FUEL
b.t_vein_undo_denied = '#creativecompat:vein_undo_denied'; // VeinUndo.DENIED: an Ore Excavation undo touching these is refused
b.t_villager_ladders = '#creativeworldgen:villager_ladders'; // McaVillagerPathLaddersMixin: villagers path up and down these (also need minecraft:climbable)
b.t_ores = '#forge:ores';
b.t_storage_blocks = '#forge:storage_blocks';
b.t_no_world_dye = '#creativecompat:no_world_dye'; // SupplementariesDyeBlocksMixin: dye can't recolour these in world
b.t_mirror_glass = '#creativecompat:mirror_glass';             // IPMirrorGlassMixin: flint and steel makes Immersive Portals mirrors from these
b.t_mirror_glass_panes = '#creativecompat:mirror_glass_panes'; // IPMirrorGlassMixin: the same, as glass panes
b.t_not_mirror_glass = '#creativecompat:not_mirror_glass';     // IPMirrorGlassMixin: never a mirror, even if Immersive Portals would accept the block
b.t_fishing_net_kelp = '#creativedelight:fishing_net/kelp';           // OceanicDelightFishingNetMixin: net uses the kelp table
b.t_fishing_net_sea_grass = '#creativedelight:fishing_net/sea_grass'; // OceanicDelightFishingNetMixin: net uses the sea grass table
b.t_blendable_blocks = '#c:blendable_blocks';       // Rubidium: per-vertex biome tint blending (vanilla grass gets it built in)

// Melting (Creative Common melting/, feature_writeup/melting.md, plan_bugfix_1114.md 3.8). The tags are filled by
// Creative Common's tags/melting_tags.js; the Java side reads them (melting/MeltTags.java). Mod globs match block ids.
b.t_melt_salt = '#creativecommon:melting/salt';                 // salt sources: melt adjacent targets
b.t_melt_fire = '#creativecommon:melting/fire';                 // fire sources; a block with a lit property counts only while lit
b.t_melt_lava = '#creativecommon:melting/lava';                 // lava sources besides any block holding lava fluid
b.t_meltable = '#creativecommon:melting/meltable';              // ice and frosted ice -> water (air when ultra-warm), snow -> air
b.t_meltable_slow = '#creativecommon:melting/meltable_slow';    // packed and blue ice: one tier down, low chance
b.t_melt_ultrawarm_instant = '#creativecommon:melting/ultrawarm_instant'; // air the moment it is placed in the Nether
b.t_melt_saltable = '#creativecommon:melting/saltable';        // the only targets salt melts ([melting] saltProducts)
b.t_melt_keeps_snow = '#creativecommon:melting/keeps_snow';    // snow resting on these ignores heat and light, not salt
b.t_melt_magma = '#creativecommon:melting/magma';              // melts snow within 2 at once, stops snow and ice forming
b.t_fire = '#minecraft:fire';                                   // fire, soul fire, modded fires
b.t_campfires = '#minecraft:campfires';
b.ice = 'minecraft:ice';
b.frosted_ice = 'minecraft:frosted_ice';
b.packed_ice = 'minecraft:packed_ice';
b.blue_ice = 'minecraft:blue_ice';
b.rare_ice = 'rare-ice:rare_ice';                               // Rare Ice: ice with an item inside (in #minecraft:ice)
b.torch = 'minecraft:torch';
b.wall_torch = 'minecraft:wall_torch';
b.soul_torch = 'minecraft:soul_torch';
b.soul_wall_torch = 'minecraft:soul_wall_torch';
b.lantern = 'minecraft:lantern';
b.soul_lantern = 'minecraft:soul_lantern';
b.jack_o_lantern = 'minecraft:jack_o_lantern';
b.glowstone = 'minecraft:glowstone';
b.salt_salt_block = 'salt:salt_block';
b.raw_rock_salt_block = 'salt:raw_rock_salt_block';
b.vintage_salt_blocks = 'vintagedelight:*salt*';                // salt, salt block, bricks, pillar, smooth, lamps (SaltBlockTicks.MELTERS)
b.vintage_evaporator = 'vintagedelight:evaporator';
b.hh_salt_lick_stages = 'hearthandharvest:*licked_salt_block';  // lightly / well / heavily licked, waxed or not (HHSaltRegistry)
b.hh_waxed_salt_block = 'hearthandharvest:waxed_salt_block';
// Modded flame lights counted as fire (plan 3.8 table, dump); the non-flame ones of the same mods are left out
b.melt_fire_supplementaries_sconces = 'supplementaries:sconce*';  // every sconce and wall sconce, the sconce lever
b.melt_fire_supplementaries_lanterns = 'supplementaries:*lantern'; // brass, copper, crimson, lead, silver, wall lantern
b.melt_fire_everythingcopper_lanterns = 'everythingcopper:*lantern'; // 16 copper and copper soul lanterns, all stages
b.melt_fire_redeco_lanterns = 'redeco:*lantern';                // 16 paper lanterns, stone and deepslate lantern
b.melt_fire_ua_tooth_lantern = 'upgrade_aquatic:tooth_lantern';
b.melt_fire_framed_torch = 'framedblocks:framed_torch';
b.melt_fire_framed_wall_torch = 'framedblocks:framed_wall_torch';
b.melt_fire_framed_soul_torch = 'framedblocks:framed_soul_torch';
b.melt_fire_framed_soul_wall_torch = 'framedblocks:framed_soul_wall_torch';
b.melt_fire_fairylights_candle_lantern = 'fairylights:candle_lantern';
b.melt_fire_fairylights_oil_lantern = 'fairylights:oil_lantern';
b.melt_fire_fairylights_paper_lantern = 'fairylights:paper_lantern';
b.melt_fire_fairylights_jack_o_lantern = 'fairylights:jack_o_lantern';
b.melt_fire_ap_nether_brass_torch = 'architects_palette:nether_brass_torch';
b.melt_fire_ap_nether_brass_wall_torch = 'architects_palette:nether_brass_wall_torch';
b.melt_fire_ap_nether_brass_lantern = 'architects_palette:nether_brass_lantern';
b.melt_fire_twigs_paper_lanterns = 'twigs:*paper_lantern';       // all 5
b.melt_fire_quark_paper_lantern = 'quark:paper_lantern';
b.melt_fire_quark_paper_lantern_sakura = 'quark:paper_lantern_sakura';
b.melt_fire_quark_duskbound_lantern = 'quark:duskbound_lantern';
b.melt_fire_dd_large_lantern = 'dustrial_decor:large_lantern';
b.melt_fire_dd_large_soul_lantern = 'dustrial_decor:large_soul_lantern';
b.melt_fire_aa_tooth_lanterns = 'alexsabnormalities:*_tooth_lantern'; // bone serpent, cachalot whale, shark
b.melt_fire_sc_reinforced_lantern = 'securitycraft:reinforced_lantern';
b.melt_fire_sc_reinforced_soul_lantern = 'securitycraft:reinforced_soul_lantern';
b.melt_fire_ie_lantern = 'immersiveengineering:lantern';
b.melt_fire_db_brazier = 'decorative_blocks:brazier';
b.melt_fire_db_soul_brazier = 'decorative_blocks:soul_brazier';
b.melt_fire_bs_brazier_on = 'blocky_siege:brazier_on';
b.melt_fire_nd_propelplant_torch = 'nethersdelight:propelplant_torch';
b.melt_fire_nd_propelplant_wall_torch = 'nethersdelight:propelplant_wall_torch';
b.melt_fire_ff_necrolord_candelabra = 'fantasyfurniture:decorations/necrolord_candelabra';
b.melt_fire_ff_royal_candelabra = 'fantasyfurniture:decorations/royal_candelabra';
b.melt_fire_di_wayward_lantern = 'domesticationinnovation:wayward_lantern';

// ############################## Validation ##############################

StartupEvents.postInit(() => {
	var $ForgeRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries');
	var $ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation');

	function blockExists(id) {
		try {
			return $ForgeRegistries.BLOCKS.containsKey($ResourceLocation.of(id, ':'));
		} catch (err) {
			return false;
		}
	}

	let missing = [];
	let tagEntries = 0;
	for (let key in b) {
		let id = b[key];
		if (typeof id !== 'string') {
			// Object-shaped block matchers (state predicates, etc.) — skip validation
			continue;
		}
		// A glob ('displaydelight:*plated_*') is a matcher, and ids of a mod that is not loaded are guarded where they
		// are used: neither is a dict error (scratch r1106spf).
		if (id.indexOf('*') >= 0 || !global.has(id)) continue;
		if (id.startsWith('#')) {
			// Tag-based block entries aren't validated yet — verify these manually.
			// Forge block tag validation requires a TagKey<Block> lookup that hasn't
			// been implemented/tested here; flagging rather than guessing at it.
			tagEntries++;
			continue;
		}
		if (!blockExists(id)) {
			missing.push(key + ' -> ' + id);
		}
	}
	if (tagEntries > 0) console.info('[block_dict] ' + tagEntries + ' tag entries not checked (tags load after startup)');
	if (missing.length > 0) {
		console.warn('[block_dict] Missing/invalid block IDs:\n' + missing.join('\n'));
	}
});
