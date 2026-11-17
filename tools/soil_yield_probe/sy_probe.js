// Scratch-only soil yield probe (feature_writeup/crop_yields.md, scratch_queue.md SQ-SY). Copy into a scratch
// server's kubejs/server_scripts as zz_sy_probe.js, never into the pack.
//   /syt ids                                   block and item ids the SQ-SY rows need (from global.item / global.blocks)
//   /syt roll <x> <y> <z> <n> <tool|none>       roll the block at x y z's loot table n times (Block.getDrops with the
//                                              real level, so location_check sees the blocks below); prints the mean
//                                              count per item, the largest single roll and the total over n rolls
//   /syt pick <x> <y> <z> <n> <tool|none>       right-click the block at x y z n times as a Forge fake player holding
//                                              tool (shears for colonies); before each click the block and the one
//                                              above get their starting states back (ripe again); prints the mean count
//                                              per dropped item (wave 2 own picks, plan_crop_yields_wave2.md 4)
//   /syt props <block id>                      state properties and their values
//   tool for roll / pick: none, an item id, or <item id>@fortune=N (Fortune N enchanted)
//   /syt potset <x> <y> <z> <soil id> <seed id>   put soil and seed straight into a Botany Pot (right-click opens its GUI)
//   /syt pot <x> <y> <z>                       Botany Pots block entity: growth time, required growth time (soil modifier
//                                              applied), harvestable, soil and crop (SQ-SY-21)
// Place the soil and the crop first with /setblock (or /fill), so block-state syntax is the vanilla parser's.
// Output: "[syt] ..." lines in logs/latest.log.
(() => {
    const $SYArgs = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $SYInt = Java.loadClass('com.mojang.brigadier.arguments.IntegerArgumentType')
    const $SYBlock = Java.loadClass('net.minecraft.world.level.block.Block')
    const $SYPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $SYStack = Java.loadClass('net.minecraft.world.item.ItemStack')
    const $SYReg = Java.loadClass('net.minecraft.core.Registry')
    const $SYFake = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $SYHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $SYHit = Java.loadClass('net.minecraft.world.phys.BlockHitResult')
    const $SYVec = Java.loadClass('net.minecraft.world.phys.Vec3')
    const $SYDir = Java.loadClass('net.minecraft.core.Direction')
    const $SYAABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $SYItemEntity = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $SYRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    function out(s) { console.info('[syt] ' + s) }
    // tool: none, an item id, or <item id>@fortune=N (Item.of with an NBT string threw out of the command, r1115)
    function toolStack(tool) {
        if (tool == 'none') return $SYStack.EMPTY
        let m = /^([^@]+)(?:@fortune=(\d+))?$/.exec(tool)
        if (!m) throw 'bad tool ' + tool
        let st = new $SYStack($SYReg.ITEM.get(new $SYRL(m[1])), 1)
        // KubeJS's enchant(Enchantment, int) returns an enchanted copy and leaves st as it was (r1115: Fortune never applied)
        if (m[2]) { let e = st.enchant($SYReg.ENCHANTMENT.get(new $SYRL('minecraft:fortune')), parseInt(m[2])); if (e != null) st = e }
        return st
    }
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
        let stack = toolStack(tool)
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
        let parts = Object.keys(sum).sort().map(id => id + ' mean ' + (sum[id] / n).toFixed(2) + ' max ' + max[id] + ' total ' + sum[id])
        out('roll ' + String(st) + ' over ' + below + ' tool ' + tool + (stack.isEmpty() ? '' : ' nbt ' + stack.nbt) + ' n ' + n + ': ' + (parts.join(', ') || 'nothing'))
    }
    function pick(server, x, y, z, n, tool) {
        let lv = overworld(server)
        let pos = new $SYPos(x, y, z)
        let st0 = lv.getBlockState(pos), up0 = lv.getBlockState(pos.above())
        let below = [1, 2].map(k => String($SYReg.BLOCK.getKey(lv.getBlockState(pos.below(k)).getBlock()))).join(' / ')
        let fake = $SYFake.getMinecraft(lv)
        let box = new $SYAABB(pos).inflate(1.5)
        let sum = {}, max = {}
        for (let k = 0; k < n; k++) {
            lv.setBlock(pos, st0, 2)
            lv.setBlock(pos.above(), up0, 2)
            fake.setItemInHand($SYHand.MAIN_HAND, toolStack(tool))
            st0.use(lv, fake, $SYHand.MAIN_HAND, new $SYHit($SYVec.atCenterOf(pos), $SYDir.UP, pos, false))
            let one = {}
            lv.getEntitiesOfClass($SYItemEntity, box).forEach(en => {
                let s = en.getItem()
                let id = String($SYReg.ITEM.getKey(s.getItem()))
                one[id] = (one[id] || 0) + s.getCount()
                en.discard()
            })
            Object.keys(one).forEach(id => {
                sum[id] = (sum[id] || 0) + one[id]
                max[id] = Math.max(max[id] || 0, one[id])
            })
        }
        lv.setBlock(pos, st0, 2)
        lv.setBlock(pos.above(), up0, 2)
        let parts = Object.keys(sum).sort().map(id => id + ' mean ' + (sum[id] / n).toFixed(2) + ' max ' + max[id] + ' total ' + sum[id])
        out('pick ' + String(st0) + ' over ' + below + ' tool ' + tool + ' n ' + n + ': ' + (parts.join(', ') || 'nothing'))
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
            .then(C.literal('props')
                .then(C.argument('id', $SYArgs.greedyString()).executes(c => {
                    try {
                        let b = $SYReg.BLOCK.get(new $SYRL($SYArgs.getString(c, 'id')))
                        let o = []
                        b.getStateDefinition().getProperties().forEach(p => o.push(p.getName() + '=' + String(p.getPossibleValues())))
                        out('props ' + $SYArgs.getString(c, 'id') + ': ' + o.join('; '))
                    } catch (err) { out('error: ' + err) }
                    return 1
                })))
            .then(C.literal('potset')
                .then(C.argument('x', $SYInt.integer()).then(C.argument('y', $SYInt.integer()).then(C.argument('z', $SYInt.integer())
                    .then(C.argument('items', $SYArgs.greedyString()).executes(c => {
                        try {
                            let lv = overworld(c.source.server)
                            let be = lv.getBlockEntity(new $SYPos($SYInt.getInteger(c, 'x'), $SYInt.getInteger(c, 'y'), $SYInt.getInteger(c, 'z')))
                            if (be == null) { out('potset: no block entity'); return 1 }
                            let ids = $SYArgs.getString(c, 'items').split(' ')
                            let inv = be.getInventory()
                            inv.setItem(0, toolStack(ids[0]))
                            inv.setItem(1, toolStack(ids[1]))
                            inv.setChanged()
                            out('potset ' + ids.join(' ') + ': soil ' + inv.getSoilStack() + ' crop ' + inv.getCropStack())
                        } catch (err) { out('error: ' + err) }
                        return 1
                    }))))))
            .then(C.literal('pot')
                .then(C.argument('x', $SYInt.integer()).then(C.argument('y', $SYInt.integer()).then(C.argument('z', $SYInt.integer())
                    .executes(c => {
                        try {
                            let lv = overworld(c.source.server)
                            let be = lv.getBlockEntity(new $SYPos($SYInt.getInteger(c, 'x'), $SYInt.getInteger(c, 'y'), $SYInt.getInteger(c, 'z')))
                            if (be == null) { out('pot: no block entity'); return 1 }
                            let f = (o, k) => { try { return String(o[k]()) } catch (err) { return 'err ' + err } }
                            let inv = be.getInventory()
                            out('pot ' + $SYInt.getInteger(c, 'x') + ' ' + $SYInt.getInteger(c, 'y') + ' ' + $SYInt.getInteger(c, 'z') +
                                ': growth ' + f(be, 'getGrowthTime') + ' required ' + f(inv, 'getRequiredGrowthTime') +
                                ' harvestable ' + f(be, 'isCropHarvestable') + ' soil ' + f(inv, 'getSoilStack') + ' crop ' + f(inv, 'getCropStack'))
                        } catch (err) { out('error: ' + err) }
                        return 1
                    })))))
            .then(C.literal('pick')
                .then(C.argument('x', $SYInt.integer()).then(C.argument('y', $SYInt.integer()).then(C.argument('z', $SYInt.integer())
                    .then(C.argument('n', $SYInt.integer(1, 10000)).then(C.argument('tool', $SYArgs.greedyString()).executes(c => {
                        try {
                            pick(c.source.server, $SYInt.getInteger(c, 'x'), $SYInt.getInteger(c, 'y'), $SYInt.getInteger(c, 'z'),
                                $SYInt.getInteger(c, 'n'), $SYArgs.getString(c, 'tool'))
                        } catch (err) { out('error: ' + err) }
                        return 1
                    })))))))
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
