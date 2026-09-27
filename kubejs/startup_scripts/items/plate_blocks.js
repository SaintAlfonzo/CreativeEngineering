// Plate storage blocks (creativeengineering:<metal>_plate_block) - fully migrated from Java
// (patcherMod CreativeEngineeringItems used to register these as plain solid Blocks) into
// KubeJS. Same stats as the old Java blocks (Material.METAL, strength 5.0/5.0, metal sound,
// requires a tool). Note: the copper plate block is create_compressed:copper_sheet_block,
// not part of this creativeengineering family.
//
// Blockstate, block model and block-item model are hand-authored on disk under
// assets/creativeengineering/{blockstates,models/block,models/item}/<metal>_plate_block.json
// (cube_all, textures at creativeengineering:block/plate_block/<metal>_plate_block.png - the
// .model() call just points at that same model; the on-disk files win). Loot table (drop
// self) is KubeJS's block-builder default, same as the old Java family.
//
// No mineable/tool tag here - server_scripts/other/harvest.js owns ALL tool-type (pickaxe)
// and tool-tier tags for every plate block in one place, so tags don't fight between two files.

const PLATE_BLOCK_METALS = [
    'aluminum', 'cast_iron', 'constantan', 'electrum', 'lead',
    'netherite', 'nickel', 'silver', 'steel', 'uranium', 'zinc',
];

StartupEvents.registry('block', e => {
    PLATE_BLOCK_METALS.forEach(metal => {
        e.create('creativeengineering:' + metal + '_plate_block')
            .material('metal')
            .soundType('metal')
            .hardness(5.0)
            .resistance(5.0)
            .requiresTool(true)
            .model('creativeengineering:block/' + metal + '_plate_block');
    });
});
