// Misc decorative blocks in the creativeengineering namespace.
// Blockstate / block model / block-item model are hand-authored on disk under
// assets/creativeengineering/{blockstates,models/block,models/item}/<name>.json
// (the .model() call just points the generated blockstate at the same model; the on-disk
// files win). Textures live at creativeengineering:block/<name>.

StartupEvents.registry('block', e => {
    // Layered Obsidian — vanilla-obsidian stats (hard, blast-resistant, needs a diamond pick).
    e.create('creativeengineering:layered_obsidian')
        .displayName('Layered Obsidian')
        .material('stone')
        .soundType('stone')
        .hardness(50.0)
        .resistance(1200.0)
        .requiresTool(true)
        .model('creativeengineering:block/layered_obsidian')
        .tagBlock('minecraft:mineable/pickaxe')
        .tagBlock('minecraft:needs_diamond_tool');
});
