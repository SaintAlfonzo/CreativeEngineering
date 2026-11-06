// Creative Engineering; moved from kubejs/startup_scripts/items/coil_blocks.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
// Wire coil blocks (creativeengineering:coil_<metal>) - the pack's own tier fillers
// alongside IE's coil_lv / coil_mv (copper / electrum). Steel has its own block here now
// (was aliased to IE's coil_hv); electrum still uses IE's coil_mv.
//
// Blockstate, block model and block-item model are hand-authored on disk under
// assets/creativeengineering/{blockstates,models/block,models/item}/coil_<metal>.json
// (cube_bottom_top, textures at creativeengineering:block/coil_block/coil_<metal>_{side,top}).
// The .model() call below just points at that same block model; the on-disk files win.

const COIL_BLOCK_METALS = [
    'aluminum', 'brass', 'constantan', 'gold', 'iron',
    'lead', 'nickel', 'silver', 'zinc', 'steel'
];

StartupEvents.registry('block', e => {
    COIL_BLOCK_METALS.forEach(metal => {
        const name = metal.charAt(0).toUpperCase() + metal.slice(1);
        e.create('creativeengineering:coil_' + metal)
            .displayName(name + ' Coil Block')
            .material('metal')
            .soundType('metal')
            .hardness(3.0)
            .resistance(6.0)
            .requiresTool(true)
            .model('creativeengineering:block/coil_' + metal)
            .tagBlock('minecraft:mineable/pickaxe')
            .tagBlock('minecraft:needs_iron_tool');
    });
});
