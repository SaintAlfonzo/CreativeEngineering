// Scratch-server probe for the Trinket Case fixes (feature_writeup/fixes_compat.md 2.25, queue entries SQ-TC-00 to
// SQ-TC-02 in feature_writeup/testing/scratch_queue.md). Scratch-only: copy into a scratch server's
// kubejs/server_scripts/ (tools/creative_labels_test/scratch_server.sh), never into the pack. Run /tcprobe;
// results are "[tcprobe] ..." lines in logs/latest.log, ending with "[tcprobe] done: N fail".
//
//  rules   TrinketCaseRules.allowed and the case inventory's canPlaceItem for each test item; the case's own stack
//          is refused by its own inventory
//  slots   the menu's case slot mayPlace for each test item and never for the held (open) case; the menu is
//          IQuarkButtonAllowed and Quark accepts it
//  shift   shift-click (quickMoveStack) of cobblestone from the player inventory into the case
//  sort    a scrambled case sorted with Quark's SortingHandler.sortInventory(player, false): counts kept, order
//          grouped, contents still there when the case's NBT is read back
// With [botania] trinketCaseAnyItem = false (SQ-TC-02) the expected answers flip for the non-trinket items; the probe
// reads the config and checks against it.
(() => {
    const $TCFakeFactory = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $TCGameProfile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $TCUUID = Java.loadClass('java.util.UUID')
    const $TCForgeReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $TCRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $TCStack = Java.loadClass('net.minecraft.world.item.ItemStack')
    const $TCBox = Java.loadClass('vazkii.botania.common.item.BaubleBoxItem')
    const $TCMenu = Java.loadClass('vazkii.botania.client.gui.box.BaubleBoxContainer')
    const $TCRules = Java.loadClass('com.SaintAlfonzo.creativeengineering.compat.TrinketCaseRules')
    const $TCConfig = Java.loadClass('com.SaintAlfonzo.creativeengineering.CEConfig')
    const $TCQuarkAllowed = Java.loadClass('vazkii.quark.api.IQuarkButtonAllowed')
    const $TCTransfer = Java.loadClass('vazkii.quark.base.handler.InventoryTransferHandler')
    const $TCSort = Java.loadClass('vazkii.quark.base.handler.SortingHandler')

    // id, count, true when Botania itself accepts it (trinket, rod, mana item or curio: Shulker Box Slot makes shulker
    // boxes curios). Everything else, another trinket case included, follows trinketCaseAnyItem.
    const ITEMS = [
        ['minecraft:cobblestone', 64, false],
        ['minecraft:diamond_sword', 1, false],
        ['minecraft:bread', 64, false],
        ['botania:aura_ring', 1, true],
        ['botania:dirt_rod', 1, true],
        ['minecraft:shulker_box', 1, true],
        ['botania:bauble_box', 1, false]
    ]
    let fails = 0
    function log(s) { console.info('[tcprobe] ' + s) }
    function check(ok, what) { if (!ok) fails++; log((ok ? 'PASS ' : 'FAIL ') + what) }
    function stack(id, n) { return new $TCStack($TCForgeReg.ITEMS.getValue(new $TCRL(id)), n) }
    function idOf(st) { return String($TCForgeReg.ITEMS.getKey(st.getItem())) }
    function expected(row, anyItem) { return row[2] || anyItem }

    function fake(level) {
        let p = $TCFakeFactory.get(level, new $TCGameProfile($TCUUID.randomUUID(), 'tcprobe'))
        p.getInventory().clearContent()
        return p
    }
    // The menu checks that the case is the held item, so it goes in the main hand (hotbar slot 0).
    function openCase(p) {
        let box = stack('botania:bauble_box', 1)
        p.getInventory().setItem(0, box)
        p.getInventory().selected = 0
        let menu = new $TCMenu(9001, p.getInventory(), p.getInventory().getItem(0))
        p.containerMenu = menu
        return menu
    }

    function rules(level, anyItem) {
        let inv = $TCBox.getInventory(stack('botania:bauble_box', 1))
        ITEMS.forEach(row => {
            let st = stack(row[0], row[1]), want = expected(row, anyItem)
            check(Boolean(inv.canPlaceItem(0, st)) === want, 'rules canPlaceItem ' + row[0] + ' = ' + want)
            if (row[2] !== true) check(Boolean($TCRules.allowed(st)) === anyItem, 'rules allowed ' + row[0])
        })
        let box = stack('botania:bauble_box', 1)
        check(!$TCBox.getInventory(box).canPlaceItem(0, box), 'rules canPlaceItem of the case into itself = false')
    }

    function slots(level, anyItem) {
        let p = fake(level), menu = openCase(p)
        check(menu instanceof $TCQuarkAllowed, 'menu is IQuarkButtonAllowed')
        check(Boolean($TCTransfer.accepts(menu, p)), 'Quark InventoryTransferHandler.accepts(menu)')
        ITEMS.forEach(row => {
            let want = expected(row, anyItem)
            check(Boolean(menu.getSlot(0).mayPlace(stack(row[0], row[1]))) === want, 'slot mayPlace ' + row[0] + ' = ' + want)
        })
        // a hotbar-key swap offers the held stack itself to the slot
        check(!menu.getSlot(0).mayPlace(p.getInventory().getItem(0)), 'slot mayPlace of the open case = false')
    }

    // Menu slots: 0-23 case, 24-50 player inventory 9-35, 51-59 hotbar.
    function shift(level, anyItem) {
        let p = fake(level), menu = openCase(p)
        p.getInventory().setItem(9, stack('minecraft:cobblestone', 64))
        menu.quickMoveStack(p, 24)
        let inCase = menu.getSlot(0).getItem()
        let moved = !inCase.isEmpty() && idOf(inCase) == 'minecraft:cobblestone' && inCase.getCount() == 64
        check(moved === anyItem, 'shift-click cobblestone into case: moved = ' + moved)
        if (anyItem) check(p.getInventory().getItem(9).isEmpty(), 'shift-click left the player slot empty')
    }

    function sort(level, anyItem) {
        if (!anyItem) { log('sort skipped (config off: only trinkets fit)'); return }
        let p = fake(level), menu = openCase(p)
        let layout = [['minecraft:bread', 20], ['minecraft:cobblestone', 30], ['botania:aura_ring', 1],
            ['minecraft:bread', 30], ['minecraft:diamond_sword', 1], ['minecraft:cobblestone', 50]]
        let before = {}
        layout.forEach((row, i) => {
            menu.getSlot(i * 3).set(stack(row[0], row[1]))
            before[row[0]] = (before[row[0]] || 0) + row[1]
        })
        $TCSort.sortInventory(p, false)
        let after = {}, order = []
        for (let i = 0; i < 24; i++) {
            let st = menu.getSlot(i).getItem()
            if (st.isEmpty()) continue
            after[idOf(st)] = (after[idOf(st)] || 0) + st.getCount()
            order.push(i + ':' + st.getCount() + 'x' + idOf(st))
        }
        log('sorted: ' + order.join(' '))
        Object.keys(before).forEach(id => check(after[id] === before[id], 'sort kept ' + before[id] + ' ' + id + ' (now ' + after[id] + ')'))
        let firstEmpty = 24, lastFull = -1
        for (let i = 0; i < 24; i++) {
            if (menu.getSlot(i).getItem().isEmpty()) firstEmpty = Math.min(firstEmpty, i)
            else lastFull = i
        }
        check(lastFull < firstEmpty, 'sort packed the items to the front')
        let back = $TCBox.getInventory(p.getInventory().getItem(0)), backCount = 0
        for (let i = 0; i < 24; i++) backCount += back.getItem(i).getCount()
        let total = Object.keys(before).reduce((a, id) => a + before[id], 0)
        check(backCount === total, 'case NBT holds ' + total + ' items after sort (read back ' + backCount + ')')
    }

    ServerEvents.commandRegistry(event => {
        const { commands: Commands } = event
        event.register(Commands.literal('tcprobe').executes(ctx => {
            let level = ctx.source.getLevel()
            fails = 0
            let anyItem = Boolean($TCConfig.BOTANIA.trinketCaseAnyItem.get())
            log('trinketCaseAnyItem = ' + anyItem)
            try { rules(level, anyItem) } catch (e) { fails++; log('rules ERROR ' + e) }
            try { slots(level, anyItem) } catch (e) { fails++; log('slots ERROR ' + e) }
            try { shift(level, anyItem) } catch (e) { fails++; log('shift ERROR ' + e) }
            try { sort(level, anyItem) } catch (e) { fails++; log('sort ERROR ' + e) }
            log('done: ' + fails + ' fail')
            return 1
        }))
    })
})()
