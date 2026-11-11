// Mixed Supplementaries jars, scratch-server probe (feature_writeup/mixed_jars.md). Scratch-only: copy it into
// srv/kubejs/server_scripts as zz_jar_probe.js (tools/creative_labels_test/scratch_server.sh setup, then cp); it is
// never in the pack.
//
//   /jartest   runs every check, logged as "[jartest] PASS|FAIL <id> <name>: <detail>", then "[jartest] DONE".
//
// Player clicks go through ServerPlayerGameMode.useItemOn on a Forge fake player (the jar's own use path);
// automation through the jar's Forge item handler (what hoppers and Create funnels use).
(() => {
    const $JFake = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $JProfile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $JUUID = Java.loadClass('java.util.UUID')
    const $JDir = Java.loadClass('net.minecraft.core.Direction')
    const $JBlockPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $JVec3 = Java.loadClass('net.minecraft.world.phys.Vec3')
    const $JHit = Java.loadClass('net.minecraft.world.phys.BlockHitResult')
    const $JHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $JForgeReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $JRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $JCaps = Java.loadClass('net.minecraftforge.common.capabilities.ForgeCapabilities')
    const $JIHH = Java.loadClass('net.minecraftforge.items.ItemHandlerHelper')
    const $JTagParser = Java.loadClass('net.minecraft.nbt.TagParser')

    const Y = -58
    let results = { pass: 0, fail: 0 }
    function check(id, name, ok, detail) {
        if (ok) results.pass++; else results.fail++
        console.info('[jartest] ' + (ok ? 'PASS' : 'FAIL') + ' ' + id + ' ' + name + ': ' + detail)
    }
    function stack(id, n) { return Item.of(id, n || 1) }
    function block(id) { return $JForgeReg.BLOCKS.getValue(new $JRL(id)) }
    let fakeN = 0
    function fake(level) {
        fakeN++
        let p = $JFake.get(level, new $JProfile($JUUID.randomUUID(), 'jar' + fakeN))
        p.getInventory().clearContent()
        return p
    }
    function click(p, level, at, st) {
        p.setItemInHand($JHand.MAIN_HAND, st)
        let hit = new $JHit(new $JVec3(at.getX() + 0.5, at.getY() + 1, at.getZ() + 0.5), $JDir.UP, at, false)
        return String(p.gameMode.useItemOn(p, level, p.getMainHandItem(), $JHand.MAIN_HAND, hit))
    }
    let nextX = 0
    function newJar(level) {
        let at = new $JBlockPos(nextX, Y, 3)
        nextX += 2
        level.setBlockAndUpdate(at, block('supplementaries:jar').defaultBlockState())
        return [at, level.getBlockEntity(at)]
    }
    function slots(be) {
        let out = []
        for (let i = 0; i < be.getContainerSize(); i++) {
            let s = be.getItem(i)
            out.push(s.isEmpty() ? '-' : String(s.getItem().getId()).replace(/^[a-z_]+:/, '') + (s.getCount() > 1 ? 'x' + s.getCount() : ''))
        }
        return out.join(',')
    }
    function total(be) { let n = 0; for (let i = 0; i < be.getContainerSize(); i++) n += be.getItem(i).getCount(); return n }
    function handler(be) { return be.getCapability($JCaps.ITEM_HANDLER).resolve().get() }

    function run(level) {
        results = { pass: 0, fail: 0 }
        nextX = 0

        // tags
        check('J1', 'apple jarable', stack('minecraft:apple').hasTag('supplementaries:cookies'), 'apple in #supplementaries:cookies')
        check('J1', 'lemon cookie family', stack('fruitsdelight:lemon_cookie').hasTag('creativedelight:jar_family/cookies'), 'in jar_family/cookies')
        check('J1', 'toast solo', stack('moredelight:toast').hasTag('supplementaries:cookies'), 'toast still jarable')

        // shape
        let [a, ja] = newJar(level)
        check('J2', 'slots', ja.getContainerSize() == 12 && ja.getMaxStackSize() == 1, 'size ' + ja.getContainerSize() + ', max ' + ja.getMaxStackSize())

        // player path: mix in family, refuse other family, take the top
        let p = fake(level)
        click(p, level, a, stack('minecraft:cookie', 4))
        click(p, level, a, stack('fruitsdelight:lemon_cookie', 4))
        check('J3', 'cookies mix', total(ja) == 2 && String(ja.getItem(1).getItem().getId()) == 'fruitsdelight:lemon_cookie', slots(ja))
        click(p, level, a, stack('minecraft:apple', 4))
        check('J3', 'apple refused in cookie jar', total(ja) == 2, slots(ja))
        click(p, level, a, Item.empty)
        let got = p.getMainHandItem()
        check('J4', 'take top layer', total(ja) == 1 && String(got.getItem().getId()) == 'fruitsdelight:lemon_cookie', 'hand ' + got + ', jar ' + slots(ja))

        // automation: fills in order, caps at capacity, comparator
        let h = handler(ja)
        let left = $JIHH.insertItem(h, stack('minecraft:cookie', 20), false)
        check('J5', 'handler fill', total(ja) == 12 && left.getCount() == 9 && ja.isFull(), 'left ' + left.getCount() + ', ' + slots(ja))
        let sig = level.getBlockState(a).getAnalogOutputSignal(level, a)
        check('J5', 'comparator full', sig == 15, 'signal ' + sig)
        let [b, jb] = newJar(level)
        let hb = handler(jb)
        let r5 = hb.insertItem(5, stack('minecraft:cookie'), false)
        let r0 = hb.insertItem(0, stack('minecraft:cookie'), false)
        check('J6', 'handler order', r5.getCount() == 1 && r0.isEmpty() && total(jb) == 1, 'slot5 left ' + r5.getCount() + ', ' + slots(jb))
        let r1 = hb.insertItem(1, stack('minecraft:apple'), false)
        check('J6', 'handler refuses other family', r1.getCount() == 1 && total(jb) == 1, slots(jb))
        let sig2 = level.getBlockState(b).getAnalogOutputSignal(level, b)
        check('J6', 'comparator 1 of 12', sig2 == 1 + Math.floor(14 * 1 / 12), 'signal ' + sig2)

        // old one-stack jars split on load
        let [c, jc] = newJar(level)
        jc.load($JTagParser.parseTag('{Items:[{Slot:0b,id:"minecraft:cookie",Count:5b}]}'))
        check('J7', 'legacy stack split', total(jc) == 5 && jc.getItem(4).getCount() == 1 && jc.getItem(5).isEmpty(), slots(jc))

        // a gap from a middle extraction closes on save
        let [d, jd] = newJar(level)
        click(p, level, d, stack('minecraft:cookie'))
        click(p, level, d, stack('farmersdelight:honey_cookie'))
        click(p, level, d, stack('minecraft:cookie'))
        handler(jd).extractItem(0, 1, false)
        jd.saveWithoutMetadata()
        check('J8', 'gap closes', slots(jd).startsWith('honey_cookie,cookie,-'), slots(jd))

        // other families, solo items
        let [e, je] = newJar(level)
        click(p, level, e, stack('create_factory:caramel_apple'))
        click(p, level, e, stack('create:honeyed_apple'))
        click(p, level, e, stack('minecraft:cookie'))
        check('J9', 'candied apples mix, cookie refused', total(je) == 2, slots(je))
        let [f, jf] = newJar(level)
        click(p, level, f, stack('moredelight:toast'))
        click(p, level, f, stack('moredelight:toast'))
        click(p, level, f, stack('minecraft:cookie'))
        check('J9', 'toast with toast only', total(jf) == 2, slots(jf))
        let [g, jg] = newJar(level)
        click(p, level, g, stack('minecraft:sweet_berries'))
        click(p, level, g, stack('vinery:red_grape'))
        click(p, level, g, stack('goldenfood:golden_sweet_berries'))
        click(p, level, g, stack('create:chocolate_glazed_berries'))
        check('J9', 'berries mix, candied berry refused', total(jg) == 3, slots(jg))

        // picked-up jar keeps every layer
        let item = level.getBlockState(d).getBlock().getJarItem(jd)
        let items = item.getNbt().getCompound('BlockEntityTag').getList('Items', 10)
        check('J10', 'jar item keeps layers', items.size() == 2, String(items))

        console.info('[jartest] DONE: ' + results.pass + ' pass, ' + results.fail + ' fail')
        return 1
    }

    ServerEvents.commandRegistry(event => {
        const Commands = Java.loadClass('net.minecraft.commands.Commands')
        event.register(
            Commands.literal('jartest')
                .requires(src => src.hasPermission(2))
                .executes(ctx => {
                    try { return run(ctx.source.getLevel()) } catch (err) {
                        console.info('[jartest] FAIL ERR: ' + err + (err.javaException ? ' / ' + err.javaException : ''))
                        return 0
                    }
                })
        )
    })
})()
