// Creative Engineering. Plain items and the prepared flesh block, migrated from Java (Creative Common
// CreativeEngineeringItems used to register these as bare Item/Block with no custom behavior) into KubeJS.
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

var reg = global.reg.forPack('creativeengineering')
StartupEvents.registry('item', e => {
    reg.item(e, 'zinc_grit').displayName('Zinc Grit')
    reg.item(e, 'brass_grit').displayName('Brass Grit')
    reg.item(e, 'cast_iron_grit').displayName('Cast Iron Grit')
    reg.item(e, 'spool').displayName('Spool')
    reg.item(e, 'transmitter').displayName('Transmitter')

    // Prepared flesh keeps the id of the removed JRFTL mod (jrftl:prepared_flesh) so existing worlds keep the item.
    // Texture: assets/jrftl/textures/item/prepared_flesh.png
    reg.item(e, 'jrftl:prepared_flesh').displayName('Prepared Flesh')

    reg.item(e, 'rose_gold_nugget').displayName('Rose Gold Nugget')
    reg.item(e, 'rose_gold_rod').displayName('Rose Gold Rod')
    reg.item(e, 'zinc_plate').displayName('Zinc Plate')

    // Wires - constantan_wire is new (item_dict.js's wire_constantan pointed at
    // vintageimprovements:constantan_wire before; repointed to this one, matching every
    // other metal's wire_X key already pointing at its own creativeengineering:X_wire)
    reg.item(e, 'iron_wire').displayName('Iron Wire')
    reg.item(e, 'zinc_wire').displayName('Zinc Wire')
    reg.item(e, 'gold_wire').displayName('Gold Wire')
    reg.item(e, 'silver_wire').displayName('Silver Wire')
    reg.item(e, 'nickel_wire').displayName('Nickel Wire')
    reg.item(e, 'brass_wire').displayName('Brass Wire')
    reg.item(e, 'constantan_wire')

})

StartupEvents.registry('block', e => {
    reg.block(e, 'prepared_flesh_block')
        .displayName('Prepared Flesh Block')
        .material('clay')
        .soundType('mud')
        .hardness(0.8)
        .resistance(0.8)
        .model('creativeengineering:block/prepared_flesh_block')
})
