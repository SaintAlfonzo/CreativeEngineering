// Scratch-only probe for run r1103 slice A (tools/scratch_query/batches/r1103/sA_*.qb). Copy into a scratch server's
// kubejs/server_scripts as zz_sA_probe.js, never into the pack. NAME = a perfBot bot (or any online player).
// Output: "[sa] ..." lines in logs/latest.log, then "[sa] done".
//   /sa canuse NAME                 the main-hand item's use() at the bot's current food level (full hunger: a food
//                                   that cannot always be eaten FAILs and the bot is not using it); stops using after
//   /sa alwayseat ITEM              FoodProperties.canAlwaysEat of the item
//   /sa cd NAME ITEM                cooldown of ITEM on the bot: probe tick counter, on cooldown, percent left
//   /sa eatcd NAME                  eat the main-hand item (as /zp eat), then count the ticks until its cooldown ends:
//                                   "[sa] eatcd NAME ITEM: ate, moved D blocks, ..." at once, then "[sa] eatcd NAME ITEM:
//                                   cooldown N ticks" when it ends (the qrun pace is 2 s, so a later /sa cd cannot see a
//                                   5 to 10 tick cooldown; r1103 fix)
//   /sa anvil NAME                  an anvil menu with the main-hand item left and the off-hand item right (copies, the
//                                   bot keeps both): result item and level cost (Forge's AnvilUpdateEvent runs inside)
//   /sa enchtable ITEM LEVEL N      N enchanting-table rolls (EnchantmentHelper.selectEnchantment, no treasure) on ITEM
//                                   at LEVEL; one line per roll, then how many rolls named a goldenfood enchantment
//   /sa enchreg ID                  is the enchantment id registered
//   /sa ack NAME                    answer the bot's pending teleport (a perfBot sends no ServerboundAcceptTeleportation
//                                   packet, so after any teleport connection.awaitingPositionFromClient stays set and
//                                   Forgiving Void treats the player as "teleporting" forever; r1103 SQ-CWG-09): tries
//                                   ids 0 to 4095, the matching one clears it; skips when nothing is pending (vanilla
//                                   would disconnect the bot)
//   /sa offers TAG                  every overworld entity with scoreboard tag TAG that has getOffers(): builds the
//                                   offers (as opening the trade screen does) and prints each one (r1103: data get
//                                   Offers on a fresh villager reads nothing, and MCA swaps minecraft:villager)
(() => {
    const $SAArgs = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $SAReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $SARL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $SAHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $SAStack = Java.loadClass('net.minecraft.world.item.ItemStack')
    // r1103 fix: Rhino throws "redeclaration of var" when a const sits inside a branch of run() called twice: every
    // class is loaded once here
    const $SAAnvil = Java.loadClass('net.minecraft.world.inventory.AnvilMenu')
    const $SAAccess = Java.loadClass('net.minecraft.world.inventory.ContainerLevelAccess')
    const $SAEH = Java.loadClass('net.minecraft.world.item.enchantment.EnchantmentHelper')
    const $SARand = Java.loadClass('net.minecraft.util.RandomSource')
    const $SAEvents = Java.loadClass('net.minecraftforge.event.ForgeEventFactory')
    const $SAAckTp = Java.loadClass('net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket')
    // r1103 fix: ServerLevel.getGameTime() is not reachable from KubeJS here; the probe counts server ticks itself
    let ticks = 0
    let watchers = []
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
                out('cd ' + a[0] + ' ' + a[1] + ' t=' + ticks + ' on=' + cds.isOnCooldown(it)
                    + ' pct=' + cds.getCooldownPercent(it, 0))
            } else if (what == 'eatcd') {
                let p = player(server, a[0]); if (p == null) return
                let held = p.getMainHandItem()
                if (held.isEmpty()) { out('eatcd ' + a[0] + ': empty hand'); return }
                let it = held.getItem(), id = String($SAReg.ITEMS.getKey(it)), copy = held.copy()
                let x0 = p.getX(), z0 = p.getZ()
                let res = held.finishUsingItem(p.getLevel(), p)
                res = $SAEvents.onItemUseFinish(p, copy, 0, res)
                if (res !== held) p.setItemInHand($SAHand.MAIN_HAND, res)
                let dx = p.getX() - x0, dz = p.getZ() - z0
                out('eatcd ' + a[0] + ' ' + id + ': ate, moved ' + Math.sqrt(dx * dx + dz * dz).toFixed(1)
                    + ' blocks, on cooldown ' + p.getCooldowns().isOnCooldown(it))
                watchers.push({ name: a[0], item: it, id: id, start: ticks })
            } else if (what == 'anvil') {
                let p = player(server, a[0]); if (p == null) return
                let left = p.getMainHandItem().copy(), right = p.getOffhandItem().copy()
                let m = new $SAAnvil(0, p.getInventory(), $SAAccess.create(p.getLevel(), p.blockPosition()))
                m.getSlot(0).set(left)
                m.getSlot(1).set(right)
                m.createResult()
                out('anvil ' + a[0] + ' ' + idOf(left) + ' + ' + idOf(right) + ' -> ' + idOf(m.getSlot(2).getItem())
                    + ' cost ' + m.getCost())
            } else if (what == 'enchtable') {
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
            } else if (what == 'ack') {
                let p = player(server, a[0]); if (p == null) return
                let c = p.connection
                // vanilla disconnects a player that acks with nothing pending: read Forgiving Void's accessor first
                // (its mixin adds getAwaitingPositionFromClient to the listener); without it, ack anyway
                if (typeof c.getAwaitingPositionFromClient === 'function' && c.getAwaitingPositionFromClient() == null) {
                    out('ack ' + a[0] + ': nothing pending, at ' + Math.round(p.getX()) + ' ' + Math.round(p.getY()) + ' '
                        + Math.round(p.getZ()))
                    return
                }
                for (let id = 0; id < 4096; id++) c.handleAcceptTeleportPacket(new $SAAckTp(id))
                out('ack ' + a[0] + ': sent ids 0 to 4095, at ' + Math.round(p.getX()) + ' ' + Math.round(p.getY()) + ' '
                    + Math.round(p.getZ()) + ' in ' + p.getLevel().dimension)
            } else if (what == 'offers') {
                let lvl = server.overworld(), it = lvl.getAllEntities().iterator(), found = 0
                while (it.hasNext()) {
                    let en = it.next()
                    if (!en.getTags().contains(a[0])) continue
                    if (typeof en.getOffers !== 'function') { out('offers ' + a[0] + ': ' + en.getType() + ' has no offers'); continue }
                    found++
                    let offers = en.getOffers()
                    out('offers ' + a[0] + ' ' + en.getType() + ': ' + offers.size() + ' offer(s)')
                    for (let k = 0; k < offers.size(); k++) {
                        let o = offers.get(k)
                        out('offer ' + k + ': ' + idOf(o.getCostA()) + (o.getCostB().isEmpty() ? '' : ' + ' + idOf(o.getCostB()))
                            + ' -> ' + idOf(o.getResult()) + ', maxUses ' + o.getMaxUses() + ', xp ' + o.getXp())
                    }
                }
                if (!found) out('offers ' + a[0] + ': no merchant with that tag')
            } else out('unknown query ' + what)
        } catch (err) { out('error: ' + err) }
        finally { out('done') }
    }
    ServerEvents.tick(e => {
        ticks++
        if (!watchers.length) return
        let keep = []
        watchers.forEach(w => {
            let p = e.server.getPlayerList().getPlayerByName(w.name)
            let n = ticks - w.start
            if (p == null) { out('eatcd ' + w.name + ' ' + w.id + ': player gone after ' + n + ' ticks'); out('done'); return }
            if (!p.getCooldowns().isOnCooldown(w.item)) { out('eatcd ' + w.name + ' ' + w.id + ': cooldown ' + n + ' ticks'); out('done') }
            else if (n > 400) { out('eatcd ' + w.name + ' ' + w.id + ': still on cooldown after 400 ticks'); out('done') }
            else keep.push(w)
        })
        watchers = keep
    })
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
