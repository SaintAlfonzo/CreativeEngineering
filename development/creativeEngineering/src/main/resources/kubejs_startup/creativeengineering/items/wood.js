// Creative Engineering; moved from kubejs/startup_scripts/items/wood.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
var reg = global.reg.forPack('creativeengineering')
StartupEvents.registry('item', e => {
    reg.item(e, 'wood_pellets')
        .displayName('Wood Pellets')
        .burnTime(3200) 
})
