// Scratch-only probe for run r1103 slice A (tools/scratch_query/batches/r1103/sA_*.qb). Copy into a scratch server's
// kubejs/server_scripts as zz_sA_probe.js, never into the pack. NAME = a perfBot bot (or any online player).
// Output: "[sa] ..." lines in logs/latest.log, then "[sa] done".
//   /sa canuse NAME                 the main-hand item's use() at the bot's current food level (full hunger: a food
//                                   that cannot always be eaten FAILs and the bot is not using it); stops using after
//   /sa alwayseat ITEM              FoodProperties.canAlwaysEat of the item
//   /sa cd NAME ITEM                cooldown of ITEM on the bot: game time, on cooldown, percent left (two readings a
//                                   few ticks apart give the length: ticks = dt / dpct)
//   /sa anvil NAME                  an anvil menu with the main-hand item left and the off-hand item right (copies, the
//                                   bot keeps both): result item and level cost (Forge's AnvilUpdateEvent runs inside)
//   /sa enchtable ITEM LEVEL N      N enchanting-table rolls (EnchantmentHelper.selectEnchantment, no treasure) on ITEM
//                                   at LEVEL; one line per roll, then how many rolls named a goldenfood enchantment
//   /sa enchreg ID                  is the enchantment id registered
(() => {
    const $SAArgs = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $SAReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $SARL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $SAHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $SAStack = Java.loadClass('net.minecraft.world.item.ItemStack')
    function out(s) { console.info('[sa] ' + s) }
    function idOf(st) { return st.isEmpty() ? 'empty' : st.getCount() + ' ' + String($SAReg.ITEMS.getKey(st.getItem())) }
    function item(id) { return $SAReg.ITEMS.getValue(new $SARL(id)) }
    function player(server, name) {
        let p = server.getPlayerList().getPlayerByName(name)
        if (p == null) out('no player ' + name)
        return p
    }
    function run(server, what, a) {
        try {
            if (what == 'canuse') {
                let p = player(server, a[0]); if (p == null) return
                let held = p.getMainHandItem(), before = idOf(held)
                let res = held.use(p.getLevel(), p, $SAHand.MAIN_HAND)
                out('canuse ' + a[0] + ' ' + before + ' food ' + p.getFoodData().getFoodLevel() + ': ' + res.getResult()
                    + ' using=' + p.isUsingItem())
                p.stopUsingItem()
            } else if (what == 'alwayseat') {
                let fp = item(a[0]).getFoodProperties()
                out('alwayseat ' + a[0] + ': ' + (fp == null ? 'not a food' : fp.canAlwaysEat()))
            } else if (what == 'cd') {
                let p = player(server, a[0]); if (p == null) return
                let it = item(a[1]), cds = p.getCooldowns()
                out('cd ' + a[0] + ' ' + a[1] + ' t=' + p.getLevel().getGameTime() + ' on=' + cds.isOnCooldown(it)
                    + ' pct=' + cds.getCooldownPercent(it, 0))
            } else if (what == 'anvil') {
                let p = player(server, a[0]); if (p == null) return
                const $SAAnvil = Java.loadClass('net.minecraft.world.inventory.AnvilMenu')
                const $SAAccess = Java.loadClass('net.minecraft.world.inventory.ContainerLevelAccess')
                let left = p.getMainHandItem().copy(), right = p.getOffhandItem().copy()
                let m = new $SAAnvil(0, p.getInventory(), $SAAccess.create(p.getLevel(), p.blockPosition()))
                m.getSlot(0).set(left)
                m.getSlot(1).set(right)
                m.createResult()
                out('anvil ' + a[0] + ' ' + idOf(left) + ' + ' + idOf(right) + ' -> ' + idOf(m.getSlot(2).getItem())
                    + ' cost ' + m.getCost())
            } else if (what == 'enchtable') {
                const $SAEH = Java.loadClass('net.minecraft.world.item.enchantment.EnchantmentHelper')
                const $SARand = Java.loadClass('net.minecraft.util.RandomSource')
                let n = parseInt(a[2]), lvl = parseInt(a[1]), hits = 0
                for (let k = 0; k < n; k++) {
                    let list = $SAEH.selectEnchantment($SARand.create(), new $SAStack(item(a[0])), lvl, false), ids = []
                    list.forEach(ei => {
                        let id = String($SAReg.ENCHANTMENTS.getKey(ei.enchantment))
                        ids.push(id + ' ' + ei.level)
                        if (id.indexOf('goldenfood:') == 0) hits++
                    })
                    out('enchtable ' + a[0] + ' L' + lvl + ' #' + k + ': ' + ids.join(', '))
                }
                out('enchtable ' + a[0] + ' L' + lvl + ': ' + n + ' rolls, goldenfood ' + hits)
            } else if (what == 'enchreg') {
                out('enchreg ' + a[0] + ': ' + $SAReg.ENCHANTMENTS.containsKey(new $SARL(a[0])))
            } else out('unknown query ' + what)
        } catch (err) { out('error: ' + err) }
        finally { out('done') }
    }
    ServerEvents.commandRegistry(e => {
        const { commands: C } = e
        e.register(C.literal('sa')
            .then(C.argument('what', $SAArgs.word())
                .then(C.argument('rest', $SAArgs.greedyString())
                    .executes(ctx => {
                        run(ctx.source.server, $SAArgs.getString(ctx, 'what'), String($SAArgs.getString(ctx, 'rest')).trim().split(/\s+/))
                        return 1
                    }))))
    })
})()
