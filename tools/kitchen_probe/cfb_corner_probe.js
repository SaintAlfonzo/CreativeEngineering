// TODO cooking/kitchen #15 probe: storage in Cooking for Blockheads' kitchen corner (patcherMod CfbCornerStorage).
// Scratch-only: copy into a scratch server's kubejs/server_scripts (tools/creative_labels_test/scratch_server.sh),
// never into the pack. Run /kctest; results are "[kctest] PASS|FAIL ..." lines in logs/latest.log.
(() => {
    const $KCFakeFactory = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $KCGameProfile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $KCUUID = Java.loadClass('java.util.UUID')
    const $KCDir = Java.loadClass('net.minecraft.core.Direction')
    const $KCBlockPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $KCVec3 = Java.loadClass('net.minecraft.world.phys.Vec3')
    const $KCAABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $KCHit = Java.loadClass('net.minecraft.world.phys.BlockHitResult')
    const $KCHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $KCItemEntity = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $KCForgeReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $KCRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $KCBlocks = Java.loadClass('net.minecraft.world.level.block.Blocks')
    const $KCBlockEntity = Java.loadClass('net.minecraft.world.level.block.entity.BlockEntity')
    const $KCCaps = Java.loadClass('net.minecraftforge.common.capabilities.ForgeCapabilities')
    const $KCItemProvider = Java.loadClass('net.blay09.mods.cookingforblockheads.api.capability.IKitchenItemProvider')

    const Y = -58
    function log(kind, name, detail) { console.info('[kctest] ' + kind + ' ' + name + ': ' + detail) }
    function check(name, ok, detail) { log(ok ? 'PASS' : 'FAIL', name, detail) }
    function idOf(st) { return String($KCForgeReg.ITEMS.getKey(st.getItem())) }
    function blockId(level, p) { return String($KCForgeReg.BLOCKS.getKey(level.getBlockState(p).getBlock())) }
    function pos(x, y, z) { return new $KCBlockPos(x, y, z) }
    function set(level, p, id) { level.setBlockAndUpdate(p, $KCForgeReg.BLOCKS.getValue(new $KCRL(id)).defaultBlockState()) }

    let fakeN = 0
    function fake(level) {
        fakeN++
        let p = $KCFakeFactory.get(level, new $KCGameProfile($KCUUID.randomUUID(), 'kc' + fakeN))
        p.getInventory().clearContent()
        return p
    }
    // right-click face D of block h holding st (null = empty hand), standing 1.5 blocks south of it
    function click(p, level, h, D, st) {
        p.setPositionAndRotation(h.getX() + 0.5, h.getY(), h.getZ() + 2.0, 180, 30)
        p.setItemInHand($KCHand.MAIN_HAND, st == null ? Item.empty : st)
        let s = { up: [0.5, 1, 0.5], south: [0.5, 0.5, 1], north: [0.5, 0.5, 0] }[D]
        let hit = new $KCHit(new $KCVec3(h.getX() + s[0], h.getY() + s[1], h.getZ() + s[2]), $KCDir.byName(D), h, false)
        return String(p.gameMode.useItemOn(p, level, p.getMainHandItem(), $KCHand.MAIN_HAND, hit))
    }
    // ServerPlayer#closeContainer is hidden from Rhino; doCloseContainer resets to the inventory menu
    function closeMenu(p) { try { p.doCloseContainer() } catch (e) { p.containerMenu = p.inventoryMenu } }
    function drops(level, h) {
        let out = {}
        level.getEntitiesOfClass($KCItemEntity, new $KCAABB(h).inflate(3)).forEach(e => {
            let id = idOf(e.getItem())
            out[id] = (out[id] || 0) + e.getItem().getCount()
            e.discard()
        })
        return out
    }

    function run(level) {
        for (let x = -2; x <= 12; x++) for (let z = -2; z <= 4; z++) {
            set(level, pos(x, Y - 1, z), 'minecraft:stone')
            for (let y = Y; y <= Y + 3; y++) level.setBlockAndUpdate(pos(x, y, z), $KCBlocks.AIR.defaultBlockState())
        }
        let h = pos(0, Y, 0)
        set(level, h, 'cookingforblockheads:corner')
        let be = level.getBlockEntity(h)
        let c = be.getContainer()
        check('KC-01 corner has storage', c != null && c.getContainerSize() == 27, c == null ? 'none' : c.getContainerSize() + ' slots')

        // GUI
        let p = fake(level)
        let r = click(p, level, h, 'south', null)
        let menu = p.containerMenu
        let menuName = String(menu.getClass().getSimpleName())
        check('KC-02 right-click opens a chest', (r == 'CONSUME' || r == 'SUCCESS') && menuName == 'ChestMenu' && menu.slots.size() == 27 + 36,
            'use ' + r + ', menu ' + menuName + ' ' + menu.slots.size() + ' slots')
        closeMenu(p)

        // what's put in shows in the menu and is saved
        c.setItem(0, Item.of('minecraft:carrot', 12))
        c.setItem(26, Item.of('minecraft:bowl', 5))
        let tag = be.saveWithFullMetadata()
        let caps = tag.getCompound('ForgeCaps')
        check('KC-03a saved', caps.contains('creativeengineering:corner_storage'), String(caps.getAllKeys()))
        let copy = $KCBlockEntity.loadStatic(h, level.getBlockState(h), tag)
        let cc = copy.getContainer()
        check('KC-03b loaded back', idOf(cc.getItem(0)) == 'minecraft:carrot' && cc.getItem(0).getCount() == 12
            && idOf(cc.getItem(26)) == 'minecraft:bowl', cc.getItem(0) + ' / ' + cc.getItem(26))

        // hoppers (Balm's item handler) and the cooking table (kitchen item provider)
        let handler = be.getCapability($KCCaps.ITEM_HANDLER, $KCDir.UP).orElse(null)
        check('KC-04 item handler', handler != null && handler.getSlots() == 27 && handler.getStackInSlot(0).getCount() == 12,
            handler == null ? 'none' : handler.getSlots() + ' slots')
        let provider = be.getProvider($KCItemProvider)
        check('KC-05 kitchen item provider', provider != null, String(provider))

        // block items still place on top; dye still recolours
        let r2 = click(p, level, h, 'up', Item.of('farmersdelight:cutting_board'))
        check('KC-06 board placed on top, no GUI', blockId(level, h.above()) == 'farmersdelight:cutting_board'
            && String(p.containerMenu.getClass().getSimpleName()) != 'ChestMenu', 'use ' + r2 + ', above ' + blockId(level, h.above()))
        closeMenu(p)
        level.setBlockAndUpdate(h.above(), $KCBlocks.AIR.defaultBlockState())
        let r3 = click(p, level, h, 'south', Item.of('minecraft:red_dye'))
        let st = level.getBlockState(h)
        check('KC-07 dye still recolours', String(st).indexOf('has_color=true') >= 0, 'use ' + r3 + ', ' + st)
        closeMenu(p)

        // break: contents drop
        drops(level, h)
        level.destroyBlock(h, true)
        let d = drops(level, h)
        check('KC-08 contents drop when broken', d['minecraft:carrot'] == 12 && d['minecraft:bowl'] == 5, JSON.stringify(d))
    }

    ServerEvents.commandRegistry(event => {
        const { commands: Commands } = event
        event.register(Commands.literal('kctest').executes(ctx => {
            try { run(ctx.source.getLevel()) } catch (e) { log('FAIL', 'run', 'ERROR ' + e) }
            log('INFO', 'done', '')
            return 1
        }))
    })
})()
