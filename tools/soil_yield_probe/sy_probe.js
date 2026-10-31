// Scratch-only soil yield probe (feature_writeup/crop_yields.md, scratch_queue.md SQ-SY). Copy into a scratch
// server's kubejs/server_scripts as zz_sy_probe.js, never into the pack.
//   /syt ids                                   block and item ids the SQ-SY rows need (from global.item / global.blocks)
//   /syt roll <x> <y> <z> <n> <tool|none>       roll the block at x y z's loot table n times (Block.getDrops with the
//                                              real level, so location_check sees the blocks below); prints the mean
//                                              count per item and the largest single roll
// Place the soil and the crop first with /setblock (or /fill), so block-state syntax is the vanilla parser's.
// Output: "[syt] ..." lines in logs/latest.log.
(() => {
    const $SYArgs = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $SYInt = Java.loadClass('com.mojang.brigadier.arguments.IntegerArgumentType')
    const $SYBlock = Java.loadClass('net.minecraft.world.level.block.Block')
    const $SYPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $SYStack = Java.loadClass('net.minecraft.world.item.ItemStack')
    const $SYReg = Java.loadClass('net.minecraft.core.Registry')
    function out(s) { console.info('[syt] ' + s) }
    function overworld(server) {
        let lv = null
        server.getAllLevels().forEach(l => { if (String(l.dimension) == 'minecraft:overworld') lv = l })
        return lv
    }
    function roll(server, x, y, z, n, tool) {
        let lv = overworld(server)
        let pos = new $SYPos(x, y, z)
        let st = lv.getBlockState(pos)
        let below = [1, 2].map(k => String($SYReg.BLOCK.getKey(lv.getBlockState(pos.below(k)).getBlock()))).join(' / ')
        let stack = tool == 'none' ? $SYStack.EMPTY : Item.of(tool)
        let sum = {}, max = {}
        for (let k = 0; k < n; k++) {
            let one = {}
            $SYBlock.getDrops(st, lv, pos, lv.getBlockEntity(pos), null, stack).forEach(s => {
                let id = String($SYReg.ITEM.getKey(s.getItem()))
                one[id] = (one[id] || 0) + s.getCount()
            })
            Object.keys(one).forEach(id => {
                sum[id] = (sum[id] || 0) + one[id]
                max[id] = Math.max(max[id] || 0, one[id])
            })
        }
        let parts = Object.keys(sum).sort().map(id => id + ' mean ' + (sum[id] / n).toFixed(2) + ' max ' + max[id])
        out('roll ' + String(st) + ' over ' + below + ' tool ' + tool + ' n ' + n + ': ' + (parts.join(', ') || 'nothing'))
    }
    ServerEvents.commandRegistry(e => {
        const C = e.commands
        e.register(C.literal('syt')
            .then(C.literal('ids').executes(c => {
                let i = global.item, b = global.blocks
                ;['farmland', 'rich_farmland', 'fertile_soil', 'rich_soil', 'any_knife', 'wheat', 'rice_panicle', 'farmersdelight_rice', 'corn', 'hemp_fiber', 'flax']
                    .forEach(k => out('item.' + k + ' = ' + i[k]))
                ;['wheat_crop', 'carrots', 'rice_panicles', 'bell_peppers', 'flax_crop', 'hemp', 'corn_crop']
                    .forEach(k => out('blocks.' + k + ' = ' + b[k]))
                return 1
            }))
            .then(C.literal('roll')
                .then(C.argument('x', $SYInt.integer()).then(C.argument('y', $SYInt.integer()).then(C.argument('z', $SYInt.integer())
                    .then(C.argument('n', $SYInt.integer(1, 10000)).then(C.argument('tool', $SYArgs.greedyString()).executes(c => {
                        try {
                            roll(c.source.server, $SYInt.getInteger(c, 'x'), $SYInt.getInteger(c, 'y'), $SYInt.getInteger(c, 'z'),
                                $SYInt.getInteger(c, 'n'), $SYArgs.getString(c, 'tool'))
                        } catch (err) { out('error: ' + err) }
                        return 1
                    }))))))))
    })
})()
