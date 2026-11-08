// priority: 1000
// Must load before every other startup script: several read global.item / blocks / fluid at
// top level, and same-priority scripts load in filesystem-walk order (differs per machine).
global.blocks = {};

// ############################## Assignments ##############################
var b = global.blocks;

b.dark_fire = 'creativeengineering:dark_fire';
b.sweet_berry_bush = 'minecraft:sweet_berry_bush'; // block id (the item is sweet_berries)
b.emu_nest = 'creativedelight:emu_nest';         // Creative Delight compat/incubation/nest/EmuNests.java
b.emu_egg_nest = 'creativedelight:emu_egg_nest'; // the 1-6 egg state; no item
b.duck_nest = 'creativedelight:duck_nest';         // Creative Delight compat/incubation/nest/WaterfowlNests.java
b.duck_egg_nest = 'creativedelight:duck_egg_nest'; // the 1-6 egg state; no item
b.goose_nest = 'creativedelight:goose_nest';
b.goose_egg_nest = 'creativedelight:goose_egg_nest';
b.duck_egg_crate = 'creativedelight:duck_egg_crate';   // Creative Delight compat/incubation/crate/WaterfowlEggCrates.java
b.goose_egg_crate = 'creativedelight:goose_egg_crate';
b.oven = 'creativeengineering:oven';                 // patcherMod oven/OvenBlock.java
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
// Counter toppers (patcherMod CounterToppers, TODO cooking/kitchen #20): plates, plated foods, coaster on a counter
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
b.powdered_sugar_block = 'creativeengineering:powdered_sugar_block'; // patcherMod food/PowderedSugarBlock.java
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
// Crops with their own ripe right-click pick (crops.md 2.2, patcherMod QuarkOwnPickMixin)
b.cucumber_crop = 'vintagedelight:cucumber_crop';
b.ghost_pepper_crop = 'vintagedelight:ghost_pepper_crop';
b.zucchini_crop = 'veggiesdelight:zucchini_crop';
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
b.t_oven_heat_sources = '#creativeengineering:oven_heat_sources'; // OvenHeat.OVEN_HEAT_SOURCES
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
b.t_own_pick_crops = '#creativeengineering:crops/own_pick'; // CropParity.OWN_PICK_CROPS: Quark leaves these to their own pick
b.t_harvest_from_bottom = '#creativeengineering:crops/harvest_from_bottom'; // CropParity.HARVEST_FROM_BOTTOM: Quark harvests the lower half when the top half is clicked
b.t_ghost_pepper_soil = '#vintagedelight:ghost_pepper_soil'; // Vintage port: where ghost pepper seeds plant (vintage_delight.md 2.1)
b.t_create_non_movable = '#create:non_movable';    // contraptions can't pick these up
b.t_ice = '#minecraft:ice';                          // ice, packed, blue, frosted, rare-ice
b.t_hammer_no_rotate = '#creativeengineering:hammer_no_rotate'; // IECompat.HAMMER_NO_ROTATE: the IE hammer won't turn these
b.t_end_plant_soil_slow = '#creativedelight:end_plant_soil_slow'; // enders_additions: End plants survive here, grow slowly (ends_delight.md 3.1)
b.t_end_plant_soil_fast = '#creativedelight:end_plant_soil_fast'; // same, grow fast
b.t_mineable_axe = '#minecraft:mineable/axe';
b.t_mineable_pickaxe = '#minecraft:mineable/pickaxe';

// End food blocks (ends_delight.md 3.1)
b.end_stone = 'minecraft:end_stone';
b.tilled_end_stone = 'endercrop:tilled_end_stone';
b.chorus_crate = 'endersdelight:chorus_crate';
b.endstone_stove = 'endersdelight:endstone_stove';
b.chorus_fruit_crate = 'ends_delight:chorus_fruit_crate';
b.t_counter_supports = '#creativeengineering:counter_supports'; // CounterToppers.SUPPORTS: toppers stay on these, drawn shifted
b.t_counter_toppers = '#creativeengineering:counter_toppers';   // CounterToppers.TOPPERS: cutting board, plates, coaster
b.t_campfire_rain_douses = '#creativecommon:campfire/rain_douses';         // CampfireRain.RAIN_DOUSES
b.t_campfire_rain_proof_fuel = '#creativecommon:campfire/rain_proof_fuel'; // CampfireRain.RAIN_PROOF_FUEL
b.t_vein_undo_denied = '#creativeengineering:vein_undo_denied'; // VeinUndo.DENIED: an Ore Excavation undo touching these is refused
b.t_villager_ladders = '#creativeengineering:villager_ladders'; // McaVillagerPathLaddersMixin: villagers path up and down these (also need minecraft:climbable)
b.t_ores = '#forge:ores';
b.t_storage_blocks = '#forge:storage_blocks';
b.t_no_world_dye = '#creativeengineering:no_world_dye'; // SupplementariesDyeBlocksMixin: dye can't recolour these in world
b.t_mirror_glass = '#creativecompat:mirror_glass';             // IPMirrorGlassMixin: flint and steel makes Immersive Portals mirrors from these
b.t_mirror_glass_panes = '#creativecompat:mirror_glass_panes'; // IPMirrorGlassMixin: the same, as glass panes
b.t_fishing_net_kelp = '#creativeengineering:fishing_net/kelp';           // OceanicDelightFishingNetMixin: net uses the kelp table
b.t_fishing_net_sea_grass = '#creativeengineering:fishing_net/sea_grass'; // OceanicDelightFishingNetMixin: net uses the sea grass table
b.t_blendable_blocks = '#c:blendable_blocks';       // Rubidium: per-vertex biome tint blending (vanilla grass gets it built in)

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
