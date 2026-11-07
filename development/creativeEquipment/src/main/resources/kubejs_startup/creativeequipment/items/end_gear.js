// Creative Equipment; moved from kubejs/startup_scripts/items/end.js on 2026-11-04. Felsic Scrap, the End gear
// material (recipes: kubejs/data/creativeequipment/kubejs/equipment/end_gear.js).
StartupEvents.registry('item', e => {
    e.create('creativeengineering:felsic_scrap').displayName('Felsic Scrap')

    // End gear and air brake sequenced assembly incomplete items ('kubejs:incomplete_<key>', ids unchanged; recipes in
    // kubejs/data/creativeequipment/kubejs/equipment/end_gear.js: echoAssembly, felsicAssembly, dragonAssembly). Moved
    // from Creative Engineering's changes/transition_items.js on 2026-11-06 (scratch r1106cbp SQ-CBP-08: without
    // Engineering the felsic ingot assembly failed to parse, TODO `creativebrewing/publish #22`).
    // Row: [key, texture?, colorHex?]; texture omitted = grey copy of global.item[key]'s sprite.
    // Create: Deep Dark and Felsic Gear keep their sprites under 'items/' (not 'item/'), so those rows name the texture;
    // the four creativeengineering:echo_* tools and every dragonloot item auto-derive.
    const END_GEAR_ASSEMBLIES = [
        // echo
        ['echo_sword',        'create_deep_dark:items/echo_sword'],
        ['echo_pickaxe'],
        ['echo_axe'],
        ['echo_shovel'],
        ['echo_hoe'],
        ['echo_helmet',       'create_deep_dark:items/echo_shard_helmet'],
        ['echo_chestplate',   'create_deep_dark:items/echo_shard_chestplate'],
        ['echo_leggings',     'create_deep_dark:items/echo_shard_leggings'],
        ['echo_boots',        'create_deep_dark:items/echo_shard_boots'],
        ['echo_shield',       'minecraft:entity/shield_base'],
        // felsic
        ['ingot_felsic',      'felsic_gear:items/felsic_ingot'],
        ['felsic_helmet',     'felsic_gear:items/felsic_helmet'],
        ['felsic_chestplate', 'felsic_gear:items/felsic_chestplate'],
        ['felsic_leggings',   'felsic_gear:items/felsic_leggings'],
        ['felsic_boots',      'felsic_gear:items/felsic_boots'],
        ['felsic_sword',      'felsic_gear:items/felsic_sword'],
        ['felsic_pickaxe',    'felsic_gear:items/felsic_pickaxe'],
        ['felsic_axe',        'felsic_gear:items/felsic_axe'],
        ['felsic_shovel',     'felsic_gear:items/felsic_shovel'],
        ['felsic_hoe',        'felsic_gear:items/felsic_hoe'],
        ['felsic_shield',     'minecraft:entity/shield_base'],
        // dragon
        ['dragon_sword'],
        ['dragon_pickaxe'],
        ['dragon_axe'],
        ['dragon_shovel'],
        ['dragon_hoe'],
        ['dragon_helmet'],
        ['dragon_chestplate'],
        ['dragon_leggings'],
        ['dragon_boots'],
        ['dragon_bow'],
        ['dragon_crossbow'],
        ['dragon_horse_armor'],
        ['dragon_trident'],
        ['dragon_anvil'],
        ['dragon_shield',     'minecraft:entity/shield_base'],
        // air brake (its assembly sits with the dragon rows in end_gear.js)
        ['air_brake'],
    ]
    END_GEAR_ASSEMBLIES.forEach(([key, texture, color]) => global.NCH.registerIncomplete(e, key, texture, color))
    console.log(`[creativeequipment] end gear transition items (${END_GEAR_ASSEMBLIES.length})`)
})
