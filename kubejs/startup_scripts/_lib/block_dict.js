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

// Tags
b.any_bricks = '#minecraft:bricks';
b.t_oven_heat_sources = '#creativeengineering:oven_heat_sources'; // OvenHeat.OVEN_HEAT_SOURCES
b.t_fd_heat_sources = '#farmersdelight:heat_sources';
b.t_fd_tray_heat_sources = '#farmersdelight:tray_heat_sources';   // fire, campfires, lava
b.t_hammer_no_rotate = '#creativeengineering:hammer_no_rotate'; // IECompat.HAMMER_NO_ROTATE: the IE hammer won't turn these

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
