// Plain items and the prepared flesh block, migrated from Java (patcherMod
// CreativeEngineeringItems used to register these as bare Item/Block with no custom
// behavior) into KubeJS.
//
// Item models are KubeJS's own auto-generated item/generated + layer0 (same as
// component_*/wirecoil_* in components.js) - no hand-authored model needed, just the
// texture at assets/creativeengineering/textures/item/<id>.png.
//
// prepared_flesh_block's blockstate/block model/item model ARE hand-authored on disk
// under assets/creativeengineering/{blockstates,models/block,models/item}/prepared_flesh_block.json
// (cube_all, texture at creativeengineering:block/prepared_flesh_block - the .model() call
// just points at that same model; the on-disk files win), matching decor_blocks.js's
// convention. No requiresTool() - the original Java block didn't call
// requiresCorrectToolForDrops() either, so it stays hand-breakable.

StartupEvents.registry('item', e => {
    e.create('creativeengineering:arrow_fletching')
    e.create('creativeengineering:zinc_grit')
    e.create('creativeengineering:brass_grit')
    e.create('creativeengineering:cast_iron_grit')
    e.create('creativeengineering:spool')
    e.create('creativeengineering:transmitter')

    e.create('creativeengineering:rose_gold_nugget')
    e.create('creativeengineering:rose_gold_rod')
    e.create('creativeengineering:zinc_plate')

    // Wires - constantan_wire is new (item_dict.js's wire_constantan pointed at
    // vintageimprovements:constantan_wire before; repointed to this one, matching every
    // other metal's wire_X key already pointing at its own creativeengineering:X_wire)
    e.create('creativeengineering:iron_wire')
    e.create('creativeengineering:zinc_wire')
    e.create('creativeengineering:gold_wire')
    e.create('creativeengineering:silver_wire')
    e.create('creativeengineering:nickel_wire')
    e.create('creativeengineering:brass_wire')
    e.create('creativeengineering:constantan_wire')
})

StartupEvents.registry('block', e => {
    e.create('creativeengineering:prepared_flesh_block')
        .material('clay')
        .soundType('mud')
        .hardness(0.8)
        .resistance(0.8)
        .model('creativeengineering:block/prepared_flesh_block')
})
