// Scratch-only probe for the Power Grid port rows (SQ-PWG, scratch_queue.md). Copy into a scratch server's
// kubejs/server_scripts as zz_pwg_probe.js, never into the pack.
//   /zpwg ns <namespace>     registry entries in that namespace for blocks, items, entity types, block entity types,
//                            menus, recipe types, recipe serializers, sound events, fluids (one line each, then total)
//   /zpwg mods <text>        loaded mod ids containing text, with version
// Output: "[zpwg] ..." lines in logs/latest.log, then "[zpwg] done".
ServerEvents.commandRegistry(e => {
    const C = e.commands
    const $Args = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $FR = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const out = s => console.info('[zpwg] ' + s)
    const run = (a) => {
        try {
            if (a[0] == 'ns') {
                let regs = { block: $FR.BLOCKS, item: $FR.ITEMS, entity_type: $FR.ENTITY_TYPES,
                    block_entity_type: $FR.BLOCK_ENTITY_TYPES, menu: $FR.MENU_TYPES, recipe_type: $FR.RECIPE_TYPES,
                    recipe_serializer: $FR.RECIPE_SERIALIZERS, sound_event: $FR.SOUND_EVENTS, fluid: $FR.FLUIDS }
                let total = 0
                for (let name in regs) {
                    let ids = []
                    regs[name].getKeys().forEach(k => { if (String(k.getNamespace()) == a[1]) ids.push(String(k)) })
                    total += ids.length
                    out('ns ' + a[1] + ' ' + name + ': ' + ids.length + (ids.length ? ' (' + ids.slice(0, 20).join(' ') + ')' : ''))
                }
                out('ns ' + a[1] + ' total: ' + total)
            } else if (a[0] == 'mods') {
                // ModList is blocked by the KubeJS class filter; Platform.getMods() is KubeJS's own view
                let mods = Platform.getMods()
                mods.keySet().forEach(k => {
                    let id = String(k)
                    if (id.indexOf(a[1]) >= 0) out('mod ' + id + ' ' + mods.get(k).getVersion())
                })
            } else out('unknown subcommand ' + a[0])
        } catch (err) { out('error ' + err) }
        out('done')
    }
    e.register(C.literal('zpwg').requires(s => s.hasPermission(2))
        .then(C.argument('args', $Args.greedyString()).executes(ctx => {
            run(String($Args.getString(ctx, 'args')).split(' ')); return 1
        })))
})
