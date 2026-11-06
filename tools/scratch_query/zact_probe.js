// Scratch-only fake-player actions for queue rows that need a kill or a survival break by a player (perfBot has no
// attack or break command). Copy into a scratch server's kubejs/server_scripts as zz_zact_probe.js, never into the pack.
//   /zact kill X Y Z TYPE N HELD [fire]   summons N mobs of TYPE one at a time at X Y Z; a Forge fake player holding HELD
//                                         ("none" = empty hand) hits each for 1000 (fire: set burning first); sums the
//                                         item drops within 4 blocks
//   /zact break X Y Z HELD                a fake player (survival) holding HELD breaks the block at X Y Z through
//                                         ServerPlayerGameMode.destroyBlock (fires BreakEvent, harvest checks, tool
//                                         damage); sums the item drops within 3 blocks
//   /zact useon X Y Z NAME HITY           player NAME (a perfBot bot) uses its main-hand item on the top face of X Y Z
//                                         with the hit point at (X+0.5, Y+HITY, Z+0.5), e.g. HITY 0.9 for a block whose
//                                         slot box sits below the top (perfBot's `pbot use` hits y+1.0); logs the result
// Output: "[zact] kill|break ...: <item> xN, ..." lines in logs/latest.log, then "[zact] done". Load the chunk first
// (forceload): no chunk loads here (Rhino deadlock, scratch-test gotchas).
(() => {
    const $ZAFake = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $ZAProfile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $ZAUUID = Java.loadClass('java.util.UUID')
    const $ZAPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $ZAHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $ZAReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $ZARL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $ZADamage = Java.loadClass('net.minecraft.world.damagesource.DamageSource')
    const $ZAItemEntity = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $ZAAABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $ZAVec3 = Java.loadClass('net.minecraft.world.phys.Vec3')
    const $ZAHit = Java.loadClass('net.minecraft.world.phys.BlockHitResult')
    const $ZADir = Java.loadClass('net.minecraft.core.Direction')
    let zaN = 0
    function zaOut(s) { console.info('[zact] ' + s) }
    function zaFake(level, held) {
        zaN++
        let p = $ZAFake.get(level, new $ZAProfile($ZAUUID.randomUUID(), 'zact' + zaN))
        p.getInventory().clearContent()
        if (held && held != 'none') p.setItemInHand($ZAHand.MAIN_HAND, Item.of(held, 1))
        return p
    }
    function zaBox(x, y, z, r) { return new $ZAAABB(x - r, y - r, z - r, x + r + 1, y + r + 1, z + r + 1) }
    function zaDrops(level, box, clear) {
        let out = {}
        level.getEntitiesOfClass($ZAItemEntity, box).forEach(e => {
            let st = e.getItem(), id = String($ZAReg.ITEMS.getKey(st.getItem()))
            out[id] = (out[id] || 0) + st.getCount()
            if (clear) e.discard()
        })
        return out
    }
    function zaShow(o) { return Object.keys(o).sort().map(k => k + ' x' + o[k]).join(', ') || 'none' }
    function zaRun(src, line) {
        let a = line.trim().split(/\s+/), level = src.getLevel()
        try {
            let x = parseInt(a[1]), y = parseInt(a[2]), z = parseInt(a[3])
            if (a[0] == 'kill') {
                let type = a[4], n = parseInt(a[5]), held = a[6] || 'none', fire = a[7] == 'fire'
                let box = zaBox(x, y, z, 4)
                zaDrops(level, box, true)
                let p = zaFake(level, held), dead = 0
                for (let k = 0; k < n; k++) {
                    let m = $ZAReg.ENTITY_TYPES.getValue(new $ZARL(type)).create(level)
                    m.setPos(x + 0.5, y, z + 0.5)
                    level.addFreshEntity(m)
                    if (fire) m.setSecondsOnFire(10)
                    p.setItemInHand($ZAHand.MAIN_HAND, held == 'none' ? Item.of('minecraft:air') : Item.of(held, 1))
                    m.attack($ZADamage.playerAttack(p), 1000)
                    if (!m.isAlive()) dead++
                    m.discard()
                }
                zaOut('kill ' + type + ' x' + n + ' held ' + held + (fire ? ' fire' : '') + ' dead ' + dead + ': ' + zaShow(zaDrops(level, box, true)))
            } else if (a[0] == 'break') {
                let held = a[4] || 'none', pos = new $ZAPos(x, y, z)
                let box = zaBox(x, y, z, 3)
                zaDrops(level, box, true)
                let before = String($ZAReg.BLOCKS.getKey(level.getBlockState(pos).getBlock()))
                let p = zaFake(level, held)
                let ok = p.gameMode.destroyBlock(pos)
                let after = String($ZAReg.BLOCKS.getKey(level.getBlockState(pos).getBlock()))
                let tool = p.getMainHandItem()
                zaOut('break ' + x + ' ' + y + ' ' + z + ' ' + before + ' held ' + held + ': ' + ok + ', now ' + after +
                    (tool.isEmpty() ? '' : ', tool damage ' + tool.getDamageValue()) + ', drops ' + zaShow(zaDrops(level, box, true)))
            } else if (a[0] == 'useon') {
                let p = src.getServer().getPlayerList().getPlayerByName(a[4]), hy = parseFloat(a[5])
                if (p == null) zaOut('useon: no player ' + a[4])
                else {
                    let pos = new $ZAPos(x, y, z)
                    let hit = new $ZAHit(new $ZAVec3(x + 0.5, y + hy, z + 0.5), $ZADir.UP, pos, false)
                    let held = String($ZAReg.ITEMS.getKey(p.getMainHandItem().getItem()))
                    let r = p.gameMode.useItemOn(p, level, p.getMainHandItem(), $ZAHand.MAIN_HAND, hit)
                    zaOut('useon ' + x + ' ' + y + ' ' + z + ' ' + a[4] + ' held ' + held + ' hit y+' + hy + ': ' + r)
                }
            } else zaOut('unknown action ' + a[0])
        } catch (err) { zaOut('error: ' + err) }
        zaOut('done')
    }
    ServerEvents.commandRegistry(event => {
        const { commands: C, arguments: A } = event
        event.register(C.literal('zact').requires(s => s.hasPermission(2))
            .then(C.argument('line', A.GREEDY_STRING.create(event)).executes(ctx => {
                zaRun(ctx.source, String(A.GREEDY_STRING.getResult(ctx, 'line'))); return 1
            })))
    })
})()
