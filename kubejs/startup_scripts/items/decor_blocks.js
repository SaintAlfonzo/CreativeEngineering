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

    // Weathered Cardboard Box: stats of Dustrial Decor's cardboard box (strength 1.0, blast resistance 0.3,
    // axe). Walk speed (soft tier) and the visual sink come from blocktags.js. Texture is the weathered
    // recolour of Design and Decor's box sheet (textureswapper/cardboard).
    e.create('creativeengineering:weathered_cardboard_box')
        .displayName('Weathered Cardboard Box')
        .material('wood')
        .soundType('wool')
        .hardness(1.0)
        .resistance(0.3)
        .model('creativeengineering:block/weathered_cardboard_box')
        .tagBlock('minecraft:mineable/axe');
});
