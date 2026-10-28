// Immersive Posts materials cut, scratch-server probe (scratch_queue.md SQ-IPO, perf.md 11.3). Scratch-only: copy it
// into srv/kubejs/server_scripts as zz_ipo_probe.js; it is never in the pack.
//
//   /ipotest   builds posts 40 blocks above world spawn with a Forge fake player and logs
//              "[ipotest] PASS|FAIL|INFO <id>: <detail>" in logs/latest.log
(() => {
    const $IPFake = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $IPProfile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $IPUUID = Java.loadClass('java.util.UUID')
    const $IPDir = Java.loadClass('net.minecraft.core.Direction')
    const $IPBlockPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $IPVec3 = Java.loadClass('net.minecraft.world.phys.Vec3')
    const $IPHit = Java.loadClass('net.minecraft.world.phys.BlockHitResult')
    const $IPHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $IPBlock = Java.loadClass('net.minecraft.world.level.block.Block')
    const $IPForgeReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $IPRL = Java.loadClass('net.minecraft.resources.ResourceLocation')

    let res = { pass: 0, fail: 0 }
    function log(kind, id, detail) {
        if (kind == 'PASS') res.pass++
        if (kind == 'FAIL') res.fail++
        console.info('[ipotest] ' + kind + ' ' + id + ': ' + detail)
    }
    function check(id, ok, detail) { log(ok ? 'PASS' : 'FAIL', id, detail) }
    function block(id) {
        let rl = new $IPRL(id)
        return $IPForgeReg.BLOCKS.containsKey(rl) ? $IPForgeReg.BLOCKS.getValue(rl) : null
    }
    function idAt(level, p) { return String($IPForgeReg.BLOCKS.getKey(level.getBlockState(p).getBlock())) }
    function column(level, p, n) {
        let out = []
        for (let i = 0; i < n; i++) out.push(idAt(level, p.above(i)))
        return out.join(',')
    }
    const STEP = { north: [0, 0, -1], south: [0, 0, 1], east: [1, 0, 0], west: [-1, 0, 0], up: [0, 1, 0], down: [0, -1, 0] }
    // right-click the top (or side D) of the block at h holding the item id
    function click(player, level, h, D, itemId) {
        player.setItemInHand($IPHand.MAIN_HAND, Item.of(itemId, 1))
        // Direction.getStepX and friends are not visible to Rhino
        let s = STEP[D]
        let at = new $IPVec3(h.getX() + 0.5 + s[0] * 0.5, h.getY() + 0.5 + s[1] * 0.5, h.getZ() + 0.5 + s[2] * 0.5)
        let hit = new $IPHit(at, $IPDir.byName(D), h, false)
        return String(player.gameMode.useItemOn(player, level, player.getMainHandItem(), $IPHand.MAIN_HAND, hit))
    }

    function run(level) {
        let spawn = level.getSharedSpawnPos()
        let base = new $IPBlockPos(spawn.getX() + 4, spawn.getY() + 40, spawn.getZ() + 4)
        let p = $IPFake.get(level, new $IPProfile($IPUUID.randomUUID(), 'ipotest'))
        p.setPositionAndRotation(base.getX() + 0.5, base.getY() + 1, base.getZ() - 1.5, 0, 0)

        // SQ-IPO-01: kept vs dropped blocks registered, state count
        let kept = ['woodpost', 'ironpost', 'aluminiumpost', 'steelpost', 'copperpost', 'nickelpost']
        let dropped = ['goldpost', 'silverpost', 'netherpost', 'leadpost', 'constantanpost', 'electrumpost', 'uraniumpost',
            'concretepost', 'leadedconcretepost']
        let keptMissing = kept.filter(k => block('immersiveposts:' + k) == null || block('immersiveposts:' + k + '_truss') == null)
        let droppedPresent = dropped.filter(k => block('immersiveposts:' + k) != null || block('immersiveposts:' + k + '_truss') != null)
        check('SQ-IPO-01a', keptMissing.length == 0, 'kept posts and trusses registered; missing: ' + keptMissing)
        check('SQ-IPO-01b', droppedPresent.length == 0, 'dropped posts and trusses not registered; present: ' + droppedPresent)
        let states = $IPBlock.BLOCK_STATE_REGISTRY.size()
        log('INFO', 'SQ-IPO-01c', 'block states ' + states + ' (eval 770959, expected about 726000)')
        let ipoStates = 0
        $IPForgeReg.BLOCKS.getValues().forEach(b => {
            if (String($IPForgeReg.BLOCKS.getKey(b).getNamespace()) == 'immersiveposts') ipoStates += b.getStateDefinition().getPossibleStates().size()
        })
        log('INFO', 'SQ-IPO-01d', 'immersiveposts states ' + ipoStates + ' (expected about 29952 plus base and fences)')

        // SQ-IPO-02: an iron fence on a post base, three times; then a second post and a truss between them
        let a = base
        level.setBlock(a, block('immersiveposts:postbase').defaultBlockState(), 3)
        let r = []
        for (let i = 0; i < 3; i++) r.push(click(p, level, a, 'up', 'immersiveposts:fence_iron'))
        let col = column(level, a, 5)
        check('SQ-IPO-02a', col.indexOf('immersiveposts:ironpost') >= 0, 'post grew: ' + col + ' (clicks ' + r + ')')
        let b = a.east(3)
        level.setBlock(b, block('immersiveposts:postbase').defaultBlockState(), 3)
        for (let i = 0; i < 3; i++) click(p, level, b, 'up', 'immersiveposts:fence_iron')
        log('INFO', 'SQ-IPO-02b', 'second post: ' + column(level, b, 5))
        // a fence on the side of the top post segment toward the second post
        let top = a.above(3)
        let tr = click(p, level, top, 'east', 'immersiveposts:fence_iron')
        let between = [idAt(level, top.east(1)), idAt(level, top.east(2))]
        log('INFO', 'SQ-IPO-02c', 'side click ' + tr + ', blocks between the posts at the top: ' + between)
        // the same with a hammer on the post side (arm)
        let hr = click(p, level, a.above(2), 'east', 'immersiveengineering:hammer')
        log('INFO', 'SQ-IPO-02d', 'hammer on the post side: ' + hr + ', state ' + level.getBlockState(a.above(2)))

        // SQ-IPO-03: dropped material fences on a fresh post base do nothing and throw nothing
        let fences = ['immersiveposts:fence_silver', 'immersiveposts:fence_gold', 'minecraft:nether_brick_fence']
        fences.forEach((f, i) => {
            let c = base.south(3 + i * 2)
            level.setBlock(c, block('immersiveposts:postbase').defaultBlockState(), 3)
            let rr
            try {
                rr = click(p, level, c, 'up', f)
            } catch (e) {
                check('SQ-IPO-03', false, f + ' threw ' + e)
                return
            }
            let above = idAt(level, c.above(1))
            check('SQ-IPO-03', !/^immersiveposts:.*post/.test(above), f + ': click ' + rr + ', above the base: ' + above)
        })
        console.info('[ipotest] DONE ' + res.pass + ' pass, ' + res.fail + ' fail')
        return 1
    }

    ServerEvents.commandRegistry(event => {
        const Commands = Java.loadClass('net.minecraft.commands.Commands')
        event.register(Commands.literal('ipotest').requires(src => src.hasPermission(2))
            .executes(ctx => {
                try { return run(ctx.source.getLevel()) } catch (e) { console.error('[ipotest] FAIL ERR: ' + e); return 0 }
            }))
    })
})()
