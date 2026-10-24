// priority: 1000
// Must load before every other startup script: several read global.item / blocks / fluid at
// top level, and same-priority scripts load in filesystem-walk order (differs per machine).
global.blocks = {};

// ############################## Assignments ##############################
var b = global.blocks;

b.dark_fire = 'creativeengineering:dark_fire';
b.sweet_berry_bush = 'minecraft:sweet_berry_bush'; // block id (the item is sweet_berries)
b.emu_nest = 'incubation_additions:emu_nest';         // incubationAdditions nest/EmuNests.java
b.emu_egg_nest = 'incubation_additions:emu_egg_nest'; // the 1-6 egg state; no item
b.duck_nest = 'incubation_additions:duck_nest';         // incubationAdditions nest/WaterfowlNests.java
b.duck_egg_nest = 'incubation_additions:duck_egg_nest'; // the 1-6 egg state; no item
b.goose_nest = 'incubation_additions:goose_nest';
b.goose_egg_nest = 'incubation_additions:goose_egg_nest';
b.duck_egg_crate = 'incubation_additions:duck_egg_crate';   // incubationAdditions crate/WaterfowlEggCrates.java
b.goose_egg_crate = 'incubation_additions:goose_egg_crate';
b.oven = 'creativeengineering:oven';                 // patcherMod oven/OvenBlock.java
// Oven heat (feature_writeup/oven.md 2.4): non-stove sources below the oven
b.magma_block = 'minecraft:magma_block';
b.lava_cauldron = 'minecraft:lava_cauldron';
b.blaze_burner = 'create:blaze_burner';             // counts only with blaze != none (OvenHeat.isActive)
b.lit_blaze_burner = 'create:lit_blaze_burner';
b.blaze_lantern = 'quark:blaze_lantern';
b.cutting_board = 'farmersdelight:cutting_board';  // bakeware hammering (feature_writeup/oven.md 2.5)
b.cfb_counter = 'cookingforblockheads:counter';   // 15 px worktops: cutting board support (TODO cooking/kitchen #8)
b.cfb_corner = 'cookingforblockheads:corner';
// Counter toppers (patcherMod CounterToppers, TODO cooking/kitchen #20): plates, plated foods, coaster on a counter
b.dd_food_plate = 'displaydelight:food_plate';
b.dd_small_food_plate = 'displaydelight:small_food_plate';
b.dd_plated_foods = 'displaydelight:*plated_*';    // every plated / small_plated food block (ctd_plated_*, plated_* ...)
b.coaster = 'brewinandchewin:coaster';               // the Brewin' port
// Milk and cow jars: glass bottles take milk out (server_scripts/cooking/milk_jar.js, TODO food/drinks #6)
b.cow_jar = 'cookingforblockheads:cow_jar';
b.milk_jar = 'cookingforblockheads:milk_jar';
// Campfire rain (patcherMod campfire/CampfireRain.java)
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
b.thin_ice = 'immersive_weathering:thin_ice';
b.iw_permafrost = 'immersive_weathering:permafrost';
b.iw_cryosol = 'immersive_weathering:cryosol';
b.soul_soil = 'minecraft:soul_soil';
b.rich_soul_soil = 'nethersdelight:rich_soul_soil';
b.iw_nulch = 'immersive_weathering:nulch_block';
b.polished_packed_ice = 'architects_palette:polished_packed_ice';
b.chiseled_packed_ice = 'architects_palette:chiseled_packed_ice';

// Tags
b.any_bricks = '#minecraft:bricks';
b.t_oven_heat_sources = '#creativeengineering:oven_heat_sources'; // OvenHeat.OVEN_HEAT_SOURCES
b.t_fd_heat_sources = '#farmersdelight:heat_sources';
b.t_fd_tray_heat_sources = '#farmersdelight:tray_heat_sources';   // fire, campfires, lava
b.t_keg_heat_sources = '#brewinandchewin:heat_sources';     // Brewin port: keg + Vinery cold storage; holds FD heat_sources
b.t_keg_freeze_sources = '#brewinandchewin:freeze_sources';
b.t_ghost_pepper_soil = '#vintagedelight:ghost_pepper_soil'; // Vintage port: where ghost pepper seeds plant (vintage_delight.md 2.1)
b.t_ice = '#minecraft:ice';                          // ice, packed, blue, frosted, rare-ice
b.t_hammer_no_rotate = '#creativeengineering:hammer_no_rotate'; // IECompat.HAMMER_NO_ROTATE: the IE hammer won't turn these
b.t_counter_supports = '#creativeengineering:counter_supports'; // CounterToppers.SUPPORTS: toppers stay on these, drawn shifted
b.t_counter_toppers = '#creativeengineering:counter_toppers';   // CounterToppers.TOPPERS: cutting board, plates, coaster
b.t_campfire_rain_douses = '#creativeengineering:campfire/rain_douses';         // CampfireRain.RAIN_DOUSES
b.t_campfire_rain_proof_fuel = '#creativeengineering:campfire/rain_proof_fuel'; // CampfireRain.RAIN_PROOF_FUEL
b.t_vein_undo_denied = '#creativeengineering:vein_undo_denied'; // VeinUndo.DENIED: an Ore Excavation undo touching these is refused
b.t_ores = '#forge:ores';
b.t_storage_blocks = '#forge:storage_blocks';
b.t_no_world_dye = '#creativeengineering:no_world_dye'; // SupplementariesDyeBlocksMixin: dye can't recolour these in world
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
    for (let key in b) {
        let id = b[key];
        if (typeof id !== 'string') {
            // Object-shaped block matchers (state predicates, etc.) — skip validation
            continue;
        }
        if (id.startsWith('#')) {
            // Tag-based block entries aren't validated yet — verify these manually.
            // Forge block tag validation requires a TagKey<Block> lookup that hasn't
            // been implemented/tested here; flagging rather than guessing at it.
            console.warn('[block_dict] Tag entry "' + key + ' -> ' + id + '" is not validated (tag support not yet implemented)');
            continue;
        }
        if (!blockExists(id)) {
            missing.push(key + ' -> ' + id);
        }
    }
    if (missing.length > 0) {
        console.warn('[block_dict] Missing/invalid block IDs:\n' + missing.join('\n'));
    }
});
