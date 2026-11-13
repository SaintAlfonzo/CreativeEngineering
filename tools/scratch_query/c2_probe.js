// Scratch-only probe for run r1113 chunk 2 (tools/scratch_query/batches/r1113/c2_*.qb). Copy into a scratch server's
// kubejs/server_scripts as zz_c2_probe.js, never into the pack. Output: "[zc2] ..." lines in logs/latest.log, then
// "[zc2] done". NAME = a perfBot bot (or any online player).
//   /zc2 state x y z                     block state, block entity type id and the shape's max y (empty shape: none)
//   /zc2 inv x y z                       block entity type, every item list in its NBT ("Items" lists at the top level,
//                                        in "Inventory" / "inventory" / "ItemHandler" / "Items" compounds and in each
//                                        ForgeCaps entry) as "key[slot:count id ...]", and its numeric top-level tags
//                                        (CookTime, BurnTime, fermenting_jar.progress, ...)
//   /zc2 watch x y z ticks every         /zc2 inv every <every> server ticks until <ticks>, lines "[zc2] t=N ..."
//   /zc2 useat NAME x y z hx hy hz [face] [both]   NAME uses the main-hand item on the block at x y z with the hit
//                                        point at x+hx y+hy z+hz (perfBot's /pbot use always hits the face centre);
//                                        "both" then tries the off hand when the main hand did not consume the click
//                                        (what the client does); prints the results and both hands after
//   /zc2 attrs <item id>                 attribute modifiers of a fresh stack per equipment slot (tune_tools / tune_armor)
//   /zc2 cap x y z face|null             the block entity's item handler as automation sees it from that face: slot
//                                        count, then each non-empty slot with what a 64 extract would take (simulated)
//   /zc2 air NAME N                      air supply set to N (max 300); N = -1 only reads it
//   /zc2 reg item|block|fluid|block_entity_type <regex>   count of registry ids matching the regex, first 40 listed
(() => {
    const $C2Args = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $C2FR = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $C2RL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $C2Stack = Java.loadClass('net.minecraft.world.item.ItemStack')
    const $C2Pos = Java.loadClass('net.minecraft.core.BlockPos')
    const $C2Dir = Java.loadClass('net.minecraft.core.Direction')
    const $C2Axis = Java.loadClass('net.minecraft.core.Direction$Axis')
    const $C2Hand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $C2Vec = Java.loadClass('net.minecraft.world.phys.Vec3')
    const $C2Hit = Java.loadClass('net.minecraft.world.phys.BlockHitResult')
    const $C2Slot = Java.loadClass('net.minecraft.world.entity.EquipmentSlot')
    const $C2Caps = Java.loadClass('net.minecraftforge.common.capabilities.ForgeCapabilities')
    function out(s) { console.info('[zc2] ' + s) }
    function sid(st) { return st.isEmpty() ? 'empty' : st.getCount() + ' ' + String($C2FR.ITEMS.getKey(st.getItem())) }
    function items(list) {
        let o = []
        for (let k = 0; k < list.size(); k++) {
            let c = list.getCompound(k)
            o.push((c.contains('Slot') ? c.getInt('Slot') : k) + ':' + c.getInt('Count') + ' ' + c.getString('id'))
        }
        return o.join(' ')
    }
    // item lists of one compound: its own "Items" list, or the handler compounds inside it
    function lists(prefix, tag, o) {
        if (tag.contains('Items', 9)) o.push(prefix + '[' + items(tag.getList('Items', 10)) + ']')
        ;['Inventory', 'inventory', 'ItemHandler', 'Items', 'Remainders', 'Container'].forEach(k => {
            if (tag.contains(k, 10)) {
                let c = tag.getCompound(k)
                if (c.contains('Items', 9)) o.push(prefix + k + '[' + items(c.getList('Items', 10)) + ']')
                else if (c.contains('id', 8)) o.push(prefix + k + '[' + c.getInt('Count') + ' ' + c.getString('id') + ']')
            }
        })
    }
    function inv(level, x, y, z) {
        let pos = new $C2Pos(x, y, z), be = level.getBlockEntity(pos)
        let state = String(level.getBlockState(pos))
        if (be == null) return 'no block entity, state ' + state
        let tag = be.saveWithoutMetadata(), o = []
        lists('', tag, o)
        if (tag.contains('ForgeCaps', 10)) {
            let caps = tag.getCompound('ForgeCaps')
            caps.getAllKeys().forEach(k => { if (caps.contains(k, 10)) lists('cap ' + k + ' ', caps.getCompound(k), o) })
        }
        let nums = []
        tag.getAllKeys().forEach(k => {
            let t = tag.getTagType(k)
            if (t >= 1 && t <= 6) nums.push(k + '=' + tag.get(k))
        })
        return 'be ' + String($C2FR.BLOCK_ENTITY_TYPES.getKey(be.getType())) + ' | ' + (o.length ? o.join(' | ') : 'no items')
            + ' | ' + nums.join(' ') + ' | state ' + state
    }
    function run(server, src, a) {
        let what = a[0]
        try {
            let level = src.getLevel()
            if (what == 'state') {
                let pos = new $C2Pos(+a[1], +a[2], +a[3]), st = level.getBlockState(pos), be = level.getBlockEntity(pos)
                let sh = st.getShape(level, pos)
                out('state ' + a[1] + ' ' + a[2] + ' ' + a[3] + ': ' + st + ' be=' + (be == null ? 'none' : $C2FR.BLOCK_ENTITY_TYPES.getKey(be.getType()))
                    + ' maxY=' + (sh.isEmpty() ? 'none' : sh.max($C2Axis.Y)))
            } else if (what == 'inv') {
                out('inv ' + a[1] + ' ' + a[2] + ' ' + a[3] + ': ' + inv(level, +a[1], +a[2], +a[3]))
            } else if (what == 'watch') {
                let x = +a[1], y = +a[2], z = +a[3], ticks = +a[4], every = Math.max(1, +a[5])
                for (let t = 0; t <= ticks; t += every) {
                    let tt = t
                    server.scheduleInTicks(tt, () => {
                        try { out('t=' + tt + ' ' + inv(level, x, y, z)) } catch (err) { out('watch error: ' + err) }
                        if (tt + every > ticks) out('watch done')
                    })
                }
            } else if (what == 'useat') {
                let p = server.getPlayerList().getPlayerByName(a[1])
                if (p == null) { out('no player ' + a[1]); return }
                let pos = new $C2Pos(+a[2], +a[3], +a[4])
                let dir = $C2Dir.byName(a[8] || 'up') || $C2Dir.UP
                let hit = new $C2Hit(new $C2Vec(+a[2] + +a[5], +a[3] + +a[6], +a[4] + +a[7]), dir, pos, false)
                let r = p.gameMode.useItemOn(p, p.getLevel(), p.getMainHandItem(), $C2Hand.MAIN_HAND, hit)
                let s = 'useat ' + a[1] + ' main ' + r
                if (a[9] == 'both' && !r.consumesAction()) {
                    let r2 = p.gameMode.useItemOn(p, p.getLevel(), p.getOffhandItem(), $C2Hand.OFF_HAND, hit)
                    s += ', off ' + r2
                }
                out(s + '; main now ' + sid(p.getMainHandItem()) + ', off now ' + sid(p.getOffhandItem()))
            } else if (what == 'attrs') {
                let it = $C2FR.ITEMS.getValue(new $C2RL(a[1]))
                if (it == null || String($C2FR.ITEMS.getKey(it)) != a[1]) { out('attrs ' + a[1] + ': no such item'); return }
                let st = new $C2Stack(it, 1)
                $C2Slot.values().forEach(sl => {
                    let mm = st.getAttributeModifiers(sl)
                    if (mm.isEmpty()) return
                    let o = []
                    mm.entries().forEach(e => {
                        let m = e.getValue()
                        o.push(String($C2FR.ATTRIBUTES.getKey(e.getKey())) + ' ' + m.getOperation() + ' ' + m.getAmount())
                    })
                    out('attrs ' + a[1] + ' ' + sl + ': ' + o.join('; '))
                })
                out('attrs ' + a[1] + ' maxDamage ' + st.getMaxDamage())
            } else if (what == 'cap') {
                let pos = new $C2Pos(+a[1], +a[2], +a[3]), be = level.getBlockEntity(pos)
                let dir = a[4] == 'null' ? null : $C2Dir.byName(a[4])
                if (be == null) { out('cap: no block entity'); return }
                let h = be.getCapability($C2Caps.ITEM_HANDLER, dir).resolve().orElse(null)
                if (h == null) { out('cap ' + a[4] + ': no item handler'); return }
                let o = []
                for (let k = 0; k < h.getSlots(); k++) {
                    let st = h.getStackInSlot(k), ex = h.extractItem(k, 64, true)
                    if (!st.isEmpty() || !ex.isEmpty()) o.push(k + ':' + sid(st) + ' extract ' + (ex.isEmpty() ? 0 : ex.getCount()))
                }
                out('cap ' + a[1] + ' ' + a[2] + ' ' + a[3] + ' ' + a[4] + ': ' + h.getSlots() + ' slots; ' + (o.length ? o.join(', ') : 'all empty'))
            } else if (what == 'air') {
                let p = server.getPlayerList().getPlayerByName(a[1])
                if (p == null) { out('no player ' + a[1]); return }
                if (+a[2] >= 0) p.setAirSupply(+a[2])
                out('air ' + a[1] + ' ' + p.getAirSupply())
            } else if (what == 'reg') {
                let reg = { item: $C2FR.ITEMS, block: $C2FR.BLOCKS, fluid: $C2FR.FLUIDS, block_entity_type: $C2FR.BLOCK_ENTITY_TYPES }[a[1]]
                if (!reg) { out('reg: unknown registry ' + a[1]); return }
                let re = new RegExp(a[2]), ids = []
                reg.getKeys().forEach(k => { let s = String(k); if (re.test(s)) ids.push(s) })
                ids.sort()
                out('reg ' + a[1] + ' ' + a[2] + ': ' + ids.length + (ids.length ? ' (' + ids.slice(0, 40).join(' ') + ')' : ''))
            } else out('unknown query ' + what)
        } catch (err) { out('error: ' + err) }
        finally { if (what != 'watch') out('done') }
    }
    ServerEvents.commandRegistry(e => {
        const { commands: C } = e
        e.register(C.literal('zc2').requires(s => s.hasPermission(2))
            .then(C.argument('args', $C2Args.greedyString())
                .executes(ctx => {
                    run(ctx.source.server, ctx.source, String($C2Args.getString(ctx, 'args')).split(' '))
                    return 1
                })))
    })
})()
