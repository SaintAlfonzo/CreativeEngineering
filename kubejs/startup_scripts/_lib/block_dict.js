// priority: 1000
// Must load before every other startup script: several read global.item / blocks / fluid at
// top level, and same-priority scripts load in filesystem-walk order (differs per machine).
global.blocks = {};

// ############################## Assignments ##############################
var b = global.blocks;

b.dark_fire = 'creativeengineering:dark_fire';
b.sweet_berry_bush = 'minecraft:sweet_berry_bush'; // block id (the item is sweet_berries)
b.emu_nest = 'creativeengineering:emu_nest';         // patcherMod nest/EmuNests.java
b.emu_egg_nest = 'creativeengineering:emu_egg_nest'; // the 1-6 egg state; no item
b.duck_nest = 'creativeengineering:duck_nest';         // patcherMod nest/WaterfowlNests.java
b.duck_egg_nest = 'creativeengineering:duck_egg_nest'; // the 1-6 egg state; no item
b.goose_nest = 'creativeengineering:goose_nest';
b.goose_egg_nest = 'creativeengineering:goose_egg_nest';

// Tags
b.any_bricks = '#minecraft:bricks';

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
