// Dust pile blocks (creativeengineering:<metal>_dust_pile) - fully migrated from Java
// (patcherMod CreativeEngineeringItems used to register aluminum/copper/gold/iron/lead/
// nickel/silver/uranium/zinc/diamond as plain FallingBlocks) into KubeJS, alongside the
// alloy piles (rose_gold/cast_iron/electrum/steel/constantan). Same FallingBlock behavior
// (Material.METAL, strength 5.0/5.0, gravel sound, requires a tool) as the old Java blocks.
//
// Blockstate, block model and block-item model are hand-authored on disk under
// assets/creativeengineering/{blockstates,models/block,models/item}/<metal>_dust_pile.json
// (cube_all, textures at creativeengineering:block/dust_pile/<metal>_dust_pile.png - the
// .model() call just points at that same model; the on-disk files win). Loot table (drop
// self) is KubeJS's block-builder default, same as the old Java family. Display names
// ("X Grit Pile") come from client_scripts/namechanges.js.
//
// No mineable/tool tag here - CD server creativeengineering/harvest.js owns ALL tool-type (shovel)
// and tool-tier tags for every dust pile in one place, so tags don't fight between two files.

const DUST_PILE_METALS = [
    // Ore metals (previously Java-registered)
    'aluminum', 'copper', 'gold', 'iron', 'lead', 'nickel', 'silver', 'uranium', 'zinc', 'diamond',
    // Alloys
    'rose_gold', 'cast_iron', 'electrum', 'steel', 'constantan', 'brass',
];

StartupEvents.registry('block', e => {
    DUST_PILE_METALS.forEach(metal => {
        e.create('creativeengineering:' + metal + '_dust_pile', 'falling')
            .material('metal')
            .gravelSoundType()
            .hardness(5.0)
            .resistance(5.0)
            .requiresTool(true)
            .model('creativeengineering:block/' + metal + '_dust_pile');
    });
});
