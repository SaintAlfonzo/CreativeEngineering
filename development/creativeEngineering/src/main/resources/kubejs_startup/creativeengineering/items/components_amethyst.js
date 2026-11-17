// Creative Engineering. Amethyst Dust and Amethyst Lens (components, Engineering rows).
var reg = global.reg.forPack('creativeengineering')
StartupEvents.registry('item', e => {
    reg.item(e, 'amethyst_dust').displayName('Amethyst Dust')
    reg.item(e, 'amethyst_lens').displayName('Amethyst Lens')
})
