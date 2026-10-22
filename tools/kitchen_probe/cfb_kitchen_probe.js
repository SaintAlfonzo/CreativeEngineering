// TODO food/kitchen #8 probe: Cooking for Blockheads oven fuel, sink water, FD cutting board on a counter.
// Scratch-only: copy into a scratch server's kubejs/server_scripts (tools/creative_labels_test/scratch_server.sh),
// never into the pack. Run /kttest; results are "[kttest] PASS|FAIL|INFO ..." lines in logs/latest.log, and
// the oven's burn check reports a few seconds later.
(() => {
    const $KTFakeFactory = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $KTGameProfile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $KTUUID = Java.loadClass('java.util.UUID')
    const $KTDir = Java.loadClass('net.minecraft.core.Direction')
    const $KTBlockPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $KTVec3 = Java.loadClass('net.minecraft.world.phys.Vec3')
    const $KTAABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $KTHit = Java.loadClass('net.minecraft.world.phys.BlockHitResult')
    const $KTHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $KTItemEntity = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $KTForgeReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $KTRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $KTBlocks = Java.loadClass('net.minecraft.world.level.block.Blocks')
    const $KTCfbOven = Java.loadClass('net.blay09.mods.cookingforblockheads.tile.OvenBlockEntity')
    const $KTCEConfig = Java.loadClass('com.SaintAlfonzo.creativeengineering.CEConfig')
    const $KTDirProp = Java.loadClass('net.minecraft.world.level.block.state.properties.DirectionProperty')

    const Y = -58
    function log(kind, name, detail) { console.info('[kttest] ' + kind + ' ' + name + ': ' + detail) }
    function check(name, ok, detail) { log(ok ? 'PASS' : 'FAIL', name, detail) }
    function idOf(st) { return String($KTForgeReg.ITEMS.getKey(st.getItem())) }
    function blockId(level, p) { return String($KTForgeReg.BLOCKS.getKey(level.getBlockState(p).getBlock())) }
    function pos(x, y, z) { return new $KTBlockPos(x, y, z) }
    function set(level, p, id) { level.setBlockAndUpdate(p, $KTForgeReg.BLOCKS.getValue(new $KTRL(id)).defaultBlockState()) }

    let fakeN = 0
    function fake(level) {
        fakeN++
        let p = $KTFakeFactory.get(level, new $KTGameProfile($KTUUID.randomUUID(), 'kt' + fakeN))
        p.getInventory().clearContent()
        return p
    }
    // right-click the top of block h holding st (null = empty hand); the player stands south of it, looking north
    function clickTop(p, level, h, st, x, z) {
        p.setPositionAndRotation(h.getX() + 0.5, h.getY() + 1, h.getZ() + 1.8, 180, 45)
        p.setItemInHand($KTHand.MAIN_HAND, st == null ? Item.empty : st)
        let hit = new $KTHit(new $KTVec3(h.getX() + (x || 0.5), h.getY() + 1, h.getZ() + (z || 0.5)), $KTDir.UP, h, false)
        return String(p.gameMode.useItemOn(p, level, p.getMainHandItem(), $KTHand.MAIN_HAND, hit))
    }
    function drops(level, h) {
        let out = []
        level.getEntitiesOfClass($KTItemEntity, new $KTAABB(h).inflate(3)).forEach(e => {
            out.push(e.getItem().getCount() + 'x ' + idOf(e.getItem()))
            e.discard()
        })
        return out.join(', ') || 'none'
    }

    function tag(level) {
        let ids = Ingredient.of('#balm:cooking_oil').itemIds
        check('KT-tag #balm:cooking_oil', String(ids).indexOf('rusticdelight:cooking_oil') >= 0, String(ids))
        check('KT-01a oil is oven fuel', $KTCfbOven.isItemFuel(Item.of('rusticdelight:cooking_oil')), 'isItemFuel(cooking_oil)')
        check('KT-01b coal burns too', $KTCfbOven.isItemFuel(Item.of('minecraft:coal')), 'isItemFuel(coal) (ovenRequiresCookingOil = false)')
    }

    function oven(level, server) {
        let h = pos(0, Y, 0)
        set(level, h, 'cookingforblockheads:oven')
        let be = level.getBlockEntity(h)
        let c = be.getContainer()
        let layout = []
        for (let s = 0; s < c.getContainerSize(); s++) layout.push(s)
        log('INFO', 'KT-01 oven container', c.getContainerSize() + ' slots')
        // CfB 13 layout: 0-2 input, 3 fuel, 4-6 output, 7-15 processing, 16-19 tools
        c.setItem(3, Item.of('rusticdelight:cooking_oil', 3))
        // keep it cooking past the first oil (264 ticks): 9 in the oven + a stack feeding in
        for (let s = 7; s <= 15; s++) c.setItem(s, Item.of('minecraft:beef'))
        c.setItem(0, Item.of('minecraft:beef', 64))
        function bottles() {
            let n = 0
            for (let s = 4; s <= 6; s++) if (idOf(c.getItem(s)) == 'minecraft:glass_bottle') n += c.getItem(s).getCount()
            return n
        }
        function outputs() { let o = []; for (let s = 4; s <= 6; s++) o.push(c.getItem(s).getCount() + 'x ' + idOf(c.getItem(s))); return o.join(', ') }
        server.scheduleInTicks(60, () => {
            let fuel = c.getItem(3)
            check('KT-01 oven burns oil', be.currentItemBurnTime == 264 && fuel.getCount() == 2,
                'burnTime ' + be.furnaceBurnTime + '/' + be.currentItemBurnTime + ', fuel slot ' + fuel.getCount() + 'x ' + idOf(fuel))
            check('KT-04a bottle back for the 1st oil', bottles() == 1, 'outputs ' + outputs())
        })
        server.scheduleInTicks(330, () => {
            let fuel = c.getItem(3)
            check('KT-04b bottle back for the 2nd oil', fuel.getCount() == 1 && bottles() == 2,
                'fuel ' + fuel.getCount() + 'x ' + idOf(fuel) + ', outputs ' + outputs())
        })
    }

    function sink(level) {
        let h = pos(4, Y, 0)
        set(level, h, 'cookingforblockheads:sink')
        let p = fake(level)
        let r = clickTop(p, level, h, Item.of('minecraft:bucket'))
        let held = p.getMainHandItem()
        let inv = []
        for (let s = 0; s < p.getInventory().getContainerSize(); s++) {
            let st = p.getInventory().getItem(s)
            if (!st.isEmpty()) inv.push(idOf(st))
        }
        check('KT-02 sink fills a bucket', inv.indexOf('minecraft:water_bucket') >= 0,
            'use ' + r + ', hand ' + idOf(held) + ', inventory ' + inv.join(', '))
    }

    function board(level) {
        let h = pos(8, Y, 0)
        set(level, h, 'cookingforblockheads:counter')
        let p = fake(level)
        let r = clickTop(p, level, h, Item.of('farmersdelight:cutting_board'))
        let above = blockId(level, h.above())
        check('KT-03a cutting board on a counter', above == 'farmersdelight:cutting_board', 'use ' + r + ', above: ' + above)
        if (above != 'farmersdelight:cutting_board') return
        let b = h.above()
        let r2 = clickTop(p, level, b, Item.of('minecraft:beef'))
        let r3 = clickTop(p, level, b, Item.of('farmersdelight:iron_knife'))
        let got = drops(level, b)
        check('KT-03b board cuts on a counter', got.indexOf('farmersdelight:minced_beef') >= 0,
            'place beef ' + r2 + ', knife ' + r3 + ', dropped ' + got)
        // model offset: default (0, -1 px, 0); then X = 1, Z = 2 px turned with the counter's facing
        let off = level.getBlockState(b).getOffset(level, b)
        check('KT-05a board drawn 1 px lower', Math.abs(off.y() + 1 / 16) < 1e-6 && off.x() == 0 && off.z() == 0, String(off))
        let K = $KTCEConfig.KITCHEN
        K.cuttingBoardOffsetX.set(1.0)
        K.cuttingBoardOffsetZ.set(2.0)
        let facingProp = level.getBlockState(h).getBlock().getStateDefinition().getProperty('facing')
        let want = { north: [1, 2], south: [-1, -2], east: [-2, 1], west: [2, -1] }
        Object.keys(want).forEach(f => {
            level.setBlock(h, level.getBlockState(h).setValue(facingProp, $KTDir.byName(f)), 2)
            let o = level.getBlockState(b).getOffset(level, b)
            let ok = Math.abs(o.x() * 16 - want[f][0]) < 1e-6 && Math.abs(o.z() * 16 - want[f][1]) < 1e-6
            check('KT-05b offset turns with a ' + f + ' counter', ok, 'px ' + (o.x() * 16) + ', ' + (o.y() * 16) + ', ' + (o.z() * 16))
        })
        K.cuttingBoardOffsetX.set(0.0)
        K.cuttingBoardOffsetZ.set(0.0)
        let stone = level.getBlockState(b).getOffset(level, h.above(5))
        check('KT-05c no offset off a counter', stone.y() == 0, String(stone))
    }

    ServerEvents.commandRegistry(event => {
        const { commands: Commands } = event
        event.register(Commands.literal('kttest').executes(ctx => {
            let level = ctx.source.getLevel()
            let server = ctx.source.getServer()
            for (let x = -2; x <= 12; x++) for (let z = -2; z <= 4; z++) {
                set(level, pos(x, Y - 1, z), 'minecraft:stone')
                for (let y = Y; y <= Y + 3; y++) level.setBlockAndUpdate(pos(x, y, z), $KTBlocks.AIR.defaultBlockState())
            }
            try { tag(level) } catch (e) { log('FAIL', 'tag', 'ERROR ' + e) }
            try { oven(level, server) } catch (e) { log('FAIL', 'oven', 'ERROR ' + e) }
            try { sink(level) } catch (e) { log('FAIL', 'sink', 'ERROR ' + e) }
            try { board(level) } catch (e) { log('FAIL', 'board', 'ERROR ' + e) }
            log('INFO', 'done', 'oven results follow at 3 s and 16 s')
            return 1
        }))
    })
})()
