// Scratch-only probe for the Wise Oak (scratch_queue.md SQ-WO, wise_oak.md 8). Copy into a scratch server's
// kubejs/server_scripts as zz_wo_probe.js, never into the pack. Coordinates are integers in the overworld; load the
// chunk first. NAME is a perfBot bot (`pbot spawn`).
//   /zwo state X Y Z           the block state at X Y Z, plus the overworld game time
//   /zwo punch NAME X Y Z      the bot hits the tree (WiseOakTile.onAttack, what a left-click runs); /pbot has no left-click
//   /zwo drops X Y Z R         item entities within R of X Y Z, summed by id, then discarded
//   /zwo stacks X Y Z R        item entities within R of X Y Z, one per line (id, count), left in place
//   /zwo skip N                advance the overworld game time by N ticks (timers and the talk cooldown come due)
// Output: "[zwo] ..." lines, then "[zwo] done". Written 2026-11-09, not run yet: one live call each before a batch relies on it.
(() => {
    const $Args = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $Pos = Java.loadClass('net.minecraft.core.BlockPos')
    const $AABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $ItemEnt = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $Reg = Java.loadClass('net.minecraft.core.Registry')
    function out(s) { console.info('[zwo] ' + s) }
    function items(lv, a) {
        let x = parseInt(a[1]), y = parseInt(a[2]), z = parseInt(a[3]), r = parseInt(a[4])
        return lv.getEntitiesOfClass($ItemEnt, new $AABB(x - r, y - r, z - r, x + r + 1, y + r + 1, z + r + 1))
    }
    function idOf(st) { return String($Reg.ITEM.getKey(st.getItem())) }
    function run(server, line) {
        try {
            let a = String(line).trim().split(/\s+/), what = a[0], lv = server.overworld()
            if (what == 'state') {
                let pos = new $Pos(parseInt(a[1]), parseInt(a[2]), parseInt(a[3]))
                out(a[1] + ' ' + a[2] + ' ' + a[3] + ' ' + lv.getBlockState(pos) + ' game=' + lv.getLevelData().getGameTime())
            } else if (what == 'punch') {
                let p = server.getPlayerList().getPlayerByName(a[1])
                let pos = new $Pos(parseInt(a[2]), parseInt(a[3]), parseInt(a[4]))
                lv.getBlockEntity(pos).onAttack(lv.getBlockState(pos), lv, pos, p)
                out('punch ' + lv.getBlockState(pos))
            } else if (what == 'drops') {
                let sum = {}
                items(lv, a).forEach(e => {
                    let st = e.getItem(), id = idOf(st)
                    sum[id] = (sum[id] || 0) + st.getCount()
                    e.discard()
                })
                let keys = Object.keys(sum).sort()
                out('drops ' + (keys.length ? keys.map(k => k + ' x' + sum[k]).join(', ') : 'none'))
            } else if (what == 'stacks') {
                let n = 0
                items(lv, a).forEach(e => { n++; out('stack ' + idOf(e.getItem()) + ' x' + e.getItem().getCount()) })
                out('stacks ' + n)
            } else if (what == 'skip') {
                let n = parseInt(a[1]), t0 = lv.getLevelData().getGameTime()
                lv.getLevelData().setGameTime(t0 + n)
                out('skip ' + n + ': game time ' + t0 + ' -> ' + lv.getLevelData().getGameTime())
            } else out('unknown ' + what)
        } catch (err) { out('error: ' + err) }
        out('done')
    }
    ServerEvents.commandRegistry(e => {
        const { commands: C } = e
        e.register(C.literal('zwo').then(C.argument('line', $Args.greedyString()).executes(ctx => {
            run(ctx.source.server, $Args.getString(ctx, 'line'))
            return 1
        })))
    })
})()
