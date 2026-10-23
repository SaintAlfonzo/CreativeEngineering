// TODO cooking/kitchen #5 probe: do returned serve items (bowl, paper, bottle) carry a tag that ordinary ones don't?
// Scratch-only: copy into a scratch server's kubejs/server_scripts (tools/creative_labels_test/scratch_server.sh),
// never into the pack. Run /kstest; results are "[kstest] ..." lines in logs/latest.log.
//
//  recipes  every loaded recipe whose result is a bowl, paper, glass bottle or bucket and has a tag
//  eat      a Forge fake player eats foods that hand back a container (stack of 1 and of 2): the path a real
//           player takes is ItemStack.finishUsingItem, then ForgeEventFactory.onItemUseFinish (the same order as
//           LivingEntity.completeUsingItem); the returned stack and every container in the inventory are logged
//  feasts   feast blocks and the popcorn box served to the end with right-clicks, then the leftover broken: the
//           containers handed over or dropped are logged
(() => {
    const $KSFakeFactory = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $KSGameProfile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $KSUUID = Java.loadClass('java.util.UUID')
    const $KSDir = Java.loadClass('net.minecraft.core.Direction')
    const $KSBlockPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $KSVec3 = Java.loadClass('net.minecraft.world.phys.Vec3')
    const $KSAABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $KSHit = Java.loadClass('net.minecraft.world.phys.BlockHitResult')
    const $KSHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $KSItemEntity = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $KSForgeReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $KSRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $KSEvents = Java.loadClass('net.minecraftforge.event.ForgeEventFactory')
    const $KSBlocks = Java.loadClass('net.minecraft.world.level.block.Blocks')

    const CONTAINERS = ['minecraft:bowl', 'minecraft:paper', 'minecraft:glass_bottle', 'minecraft:bucket']
    const Y = -58

    function log(s) { console.info('[kstest] ' + s) }
    function idOf(st) { return String($KSForgeReg.ITEMS.getKey(st.getItem())) }
    // getNbt() is the stack's tag or null; an empty {} tag is what would stop a plain stack from merging
    function tagStr(st) { let t = st.getNbt(); return t == null ? 'none' : (t.isEmpty() ? 'EMPTY {}' : String(t)) }
    function isContainer(st) { return !st.isEmpty() && CONTAINERS.indexOf(idOf(st)) >= 0 }
    function pos(x, y, z) { return new $KSBlockPos(x, y, z) }

    let fakeN = 0
    function fake(level) {
        fakeN++
        let p = $KSFakeFactory.get(level, new $KSGameProfile($KSUUID.randomUUID(), 'ks' + fakeN))
        p.getInventory().clearContent()
        return p
    }
    function inventoryContainers(p) {
        let out = []
        let inv = p.getInventory()
        for (let i = 0; i < inv.getContainerSize(); i++) {
            let st = inv.getItem(i)
            if (isContainer(st)) out.push('slot ' + i + ' ' + st.getCount() + 'x ' + idOf(st) + ' tag ' + tagStr(st))
        }
        return out.length ? out.join('; ') : 'none'
    }

    function recipes(server) {
        let n = 0, tagged = 0
        server.getRecipeManager().getRecipes().forEach(r => {
            let res
            try { res = r.getResultItem() } catch (e) { return }
            if (res == null || res.isEmpty() || !isContainer(res)) return
            n++
            if (res.getNbt() != null) {
                tagged++
                log('recipe ' + r.getId() + ' (' + r.getType() + ') -> ' + idOf(res) + ' tag ' + tagStr(res))
            }
        })
        log('recipes: ' + n + ' make a container, ' + tagged + ' of them with a tag')
    }

    function eatOne(level, id, count) {
        let p = fake(level)
        let st = Item.of(id, count)
        if (st.isEmpty()) { log('eat ' + id + ': not loaded'); return }
        p.setItemInHand($KSHand.MAIN_HAND, st)
        let held = p.getMainHandItem()
        let copy = held.copy()
        let res = held.finishUsingItem(level, p)
        res = $KSEvents.onItemUseFinish(p, copy, 0, res)
        if (res !== held) p.setItemInHand($KSHand.MAIN_HAND, res)
        log('eat ' + count + 'x ' + id + ' (creative ' + p.getAbilities().instabuild + '): returned ' + res.getCount() + 'x '
            + idOf(res) + ' tag ' + tagStr(res) + ' | containers: ' + inventoryContainers(p))
    }

    function eat(level) {
        let foods = ['minecraft:mushroom_stew', 'minecraft:beetroot_soup', 'farmersdelight:beef_stew',
            'farmersdelight:tomato_sauce', 'farmersdelight:hot_cocoa', 'corn_delight:caramel_popcorn',
            'corn_delight:popcorn', 'minecraft:honey_bottle', 'minecraft:milk_bucket']
        foods.forEach(id => { eatOne(level, id, 1); eatOne(level, id, 2) })
        let plain = Item.of('minecraft:bowl')
        log('Item.of(minecraft:bowl) tag ' + tagStr(plain))
    }

    function useOn(p, level, h, st) {
        p.setItemInHand($KSHand.MAIN_HAND, st)
        let hit = new $KSHit(new $KSVec3(h.getX() + 0.5, h.getY() + 1, h.getZ() + 0.5), $KSDir.UP, h, false)
        return String(p.gameMode.useItemOn(p, level, p.getMainHandItem(), $KSHand.MAIN_HAND, hit))
    }
    function droppedContainers(level, h) {
        let out = []
        level.getEntitiesOfClass($KSItemEntity, new $KSAABB(h).inflate(3)).forEach(e => {
            let st = e.getItem()
            if (isContainer(st)) out.push(st.getCount() + 'x ' + idOf(st) + ' tag ' + tagStr(st))
            e.discard()
        })
        return out.length ? out.join('; ') : 'none'
    }

    // serve with servingItem until the block is gone or 12 clicks, then break what's left
    function feast(level, x, blockId, servingItem) {
        let b = $KSForgeReg.BLOCKS.getValue(new $KSRL(blockId))
        if (b == null || b == $KSBlocks.AIR) { log('feast ' + blockId + ': not loaded'); return }
        let h = pos(x, Y, 0)
        level.setBlockAndUpdate(h.below(), $KSBlocks.STONE.defaultBlockState())
        level.setBlockAndUpdate(h, b.defaultBlockState())
        let p = fake(level)
        let clicks = 0
        while (clicks < 12 && level.getBlockState(h).getBlock() == b) {
            let r = useOn(p, level, h, servingItem ? Item.of(servingItem, 16) : Item.empty)
            clicks++
            if (r == 'PASS' || r == 'FAIL') break
        }
        let left = level.getBlockState(h).getBlock() == b ? String(level.getBlockState(h)) : 'gone'
        log('feast ' + blockId + ' after ' + clicks + ' clicks (' + left + '): inventory containers: ' + inventoryContainers(p)
            + ' | dropped: ' + droppedContainers(level, h))
        if (left != 'gone') {
            p.gameMode.destroyBlock(h)
            log('feast ' + blockId + ' broken: dropped ' + droppedContainers(level, h) + ' | inventory ' + inventoryContainers(p))
        }
        level.setBlockAndUpdate(h, $KSBlocks.AIR.defaultBlockState())
    }

    function feasts(level) {
        let x = 0
        feast(level, x += 4, 'corn_delight:popcorn_box', null)
        feast(level, x += 4, 'farmersdelight:roast_chicken_block', 'minecraft:bowl')
        feast(level, x += 4, 'farmersdelight:shepherds_pie_block', 'minecraft:bowl')
        feast(level, x += 4, 'farmersdelight:stuffed_pumpkin_block', 'minecraft:bowl')
        feast(level, x += 4, 'farmersdelight:honey_glazed_ham_block', 'minecraft:bowl')
        feast(level, x += 4, 'farmersdelight:rice_roll_medley_block', 'minecraft:bowl')
    }

    ServerEvents.commandRegistry(event => {
        const { commands: Commands } = event
        event.register(Commands.literal('kstest').executes(ctx => {
            let level = ctx.source.getLevel()
            try { recipes(ctx.source.getServer()) } catch (e) { log('recipes ERROR ' + e) }
            try { eat(level) } catch (e) { log('eat ERROR ' + e) }
            try { feasts(level) } catch (e) { log('feasts ERROR ' + e) }
            log('done')
            return 1
        }))
    })
})()
