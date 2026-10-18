// Oven — PLACEHOLDER block only. The working oven (cooking-pot-style baking, heat from below,
// stove heat on top) is planned in feature_writeup/oven.md; until it is built this block has no
// block entity, no recipes, no crafting recipe and no heat-source tag. Get it from creative/JEI.
//
// 'cardinal' gives `facing` (front faces the player on placement, like the FD stove); `lit` is
// added now so the blockstate already matches the planned Java block (oven.md 3.1) and old
// placements keep their state when it lands. Nothing sets lit=true yet.
//
// Blockstate / block models / item model are hand-authored on disk under
// assets/creativeengineering/{blockstates,models/block,models/item}/oven*.json (the on-disk
// files win over KubeJS's generated cardinal blockstate). The models point at Farmer's Delight's
// stove textures (farmersdelight:block/stove_*) until the oven gets its own art.

StartupEvents.registry('block', e => {
    // Stats copy the FD stove (Properties.copy(Blocks.BRICKS)): hardness 2, resistance 6, pickaxe.
    e.create('creativeengineering:oven', 'cardinal')
        .displayName('Oven')
        .material('stone')
        .soundType('stone')
        .hardness(2.0)
        .resistance(6.0)
        .requiresTool(true)
        .property(BlockProperties.LIT)
        .defaultState(state => state.set(BlockProperties.LIT, false))
        .tagBlock('minecraft:mineable/pickaxe')
});
