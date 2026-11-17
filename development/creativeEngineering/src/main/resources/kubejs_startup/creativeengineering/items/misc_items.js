// Creative Engineering; moved from kubejs/startup_scripts/items/misc_items.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
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
    e.create('creativeengineering:zinc_grit').displayName('Zinc Grit')
    e.create('creativeengineering:brass_grit').displayName('Brass Grit')
    e.create('creativeengineering:cast_iron_grit').displayName('Cast Iron Grit')
    e.create('creativeengineering:spool').displayName('Spool')
    e.create('creativeengineering:transmitter').displayName('Transmitter')

    // Prepared flesh keeps the id of the removed JRFTL mod (jrftl:prepared_flesh) so existing worlds keep the item.
    // Texture: assets/jrftl/textures/item/prepared_flesh.png
    e.create('jrftl:prepared_flesh').displayName('Prepared Flesh')

    e.create('creativeengineering:rose_gold_nugget').displayName('Rose Gold Nugget')
    e.create('creativeengineering:rose_gold_rod').displayName('Rose Gold Rod')
    e.create('creativeengineering:zinc_plate').displayName('Zinc Plate')

    // Wires - constantan_wire is new (item_dict.js's wire_constantan pointed at
    // vintageimprovements:constantan_wire before; repointed to this one, matching every
    // other metal's wire_X key already pointing at its own creativeengineering:X_wire)
    e.create('creativeengineering:iron_wire').displayName('Iron Wire')
    e.create('creativeengineering:zinc_wire').displayName('Zinc Wire')
    e.create('creativeengineering:gold_wire').displayName('Gold Wire')
    e.create('creativeengineering:silver_wire').displayName('Silver Wire')
    e.create('creativeengineering:nickel_wire').displayName('Nickel Wire')
    e.create('creativeengineering:brass_wire').displayName('Brass Wire')
    e.create('creativeengineering:constantan_wire')

    // The mortar and pestle moved to Creative Delight 2026-10-27 (kubejs_startup/creativedelight/bakeware.js).
    // arrow_fletching moved to Creative Equipment 2026-11-06 (kubejs_startup/creativeequipment/items/archery_items.js).
})

StartupEvents.registry('block', e => {
    e.create('creativeengineering:prepared_flesh_block')
        .displayName('Prepared Flesh Block')
        .material('clay')
        .soundType('mud')
        .hardness(0.8)
        .resistance(0.8)
        .model('creativeengineering:block/prepared_flesh_block')
})
