// Scratch-only: /ztrades <profession> <level> logs "[ztrades] <profession> L<level>: cost[, cost2] -> result" for every offer
// of a freshly summoned villager (getOffers builds that level's trades; the class VillagerTrades itself can't be read
// from Rhino). /ztrades wandering 0 does the same for a wandering trader. Scratch queue SQ-FGD-11, SQ-VP-11 (VD-32).
// Copy into a scratch server's kubejs/server_scripts as zz_trades.js, never into the pack.
(() => {
    const $TReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $TRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $TData = Java.loadClass('net.minecraft.world.entity.npc.VillagerData')
    function st(s) { return s.isEmpty() ? '' : s.getCount() + ' ' + String($TReg.ITEMS.getKey(s.getItem())) }
    function run(src, prof, lvl, n) {
        let level = src.getLevel()
        for (let k = 0; k < n; k++) {
            let e
            if (prof == 'wandering') e = $TReg.ENTITY_TYPES.getValue(new $TRL('minecraft:wandering_trader')).create(level)
            else {
                e = $TReg.ENTITY_TYPES.getValue(new $TRL('minecraft:villager')).create(level)
                let p = $TReg.VILLAGER_PROFESSIONS.getValue(new $TRL(prof.indexOf(':') < 0 ? 'minecraft:' + prof : prof))
                e.setVillagerData(e.getVillagerData().setProfession(p).setLevel(lvl))
            }
            e.setPos(0.5, -60, 40.5)
            let lines = []
            e.getOffers().forEach(o => lines.push(st(o.getBaseCostA()) + (o.getCostB().isEmpty() ? '' : ' + ' + st(o.getCostB())) + ' -> ' + st(o.getResult())))
            console.info('[ztrades] ' + prof + ' L' + lvl + ' #' + k + ': ' + lines.join(' | '))
            e.discard()
        }
    }
    ServerEvents.commandRegistry(event => {
        const { commands: Commands, arguments: Arguments } = event
        event.register(Commands.literal('ztrades').then(Commands.argument('args', Arguments.GREEDY_STRING.create(event)).executes(ctx => {
            let a = String(Arguments.GREEDY_STRING.getResult(ctx, 'args')).trim().split(/\s+/)
            run(ctx.source, a[0], Number(a[1] || 1), Number(a[2] || 1)); return 1
        })))
    })
})()
