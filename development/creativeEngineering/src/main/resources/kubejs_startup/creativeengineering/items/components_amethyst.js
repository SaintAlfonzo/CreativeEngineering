// Creative Engineering; moved from kubejs/startup_scripts/items/components_amethyst.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
// Amethyst Dust and Amethyst Lens (components, Engineering rows). Split out of items/echo_tools.js on 2026-11-04 when
// the echo tools moved to Creative Equipment (creative_split.md 7.6, wave 7); rows unchanged.
var reg = global.reg.forPack('creativeengineering')
StartupEvents.registry('item', e => {
    reg.item(e, 'amethyst_dust').displayName('Amethyst Dust')
    reg.item(e, 'amethyst_lens').displayName('Amethyst Lens')
})
