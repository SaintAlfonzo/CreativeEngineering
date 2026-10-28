// Flower potting audit probe (feature_writeup/flower_pots.md 7, TODO `blocks/framed-and-decor #6`). Scratch-only:
// copy it into srv/kubejs/server_scripts/zz_pot_probe.js; it is never in the pack.
//
//   /pottest list          the holder ids it knows
//   /pottest <holder|all>  every plant from kubejs/exported/blocks/flower_pots.json (/dumppots, run it first) into
//                          that holder; all = every holder, one per few ticks
//
// For each holder and plant: a fresh holder on stone at a fixed spot, a survival fake player clicks it 4 times with the
// plant (4 points of the clicked face, so the 2- and 3-slot holders fill), each click's change of block state, block
// entity NBT and the block above is recorded; then the player breaks the holder with a diamond pickaxe and the drops are
// collected. Soil holders (planters, filled silt pot) are clicked on top like a player planting, and the plant above
// is checked with canSurvive. Results: kubejs/exported/blocks/pot_probe.json (tools/flower_pot_audit.py reads it) and
// "[pottest] PASS|FAIL|INFO <id> ..." lines in logs/latest.log.
(() => {
    const $PTFake = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $PTProfile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $PTUUID = Java.loadClass('java.util.UUID')
    const $PTDir = Java.loadClass('net.minecraft.core.Direction')
    const $PTPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $PTVec3 = Java.loadClass('net.minecraft.world.phys.Vec3')
    const $PTAABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $PTHit = Java.loadClass('net.minecraft.world.phys.BlockHitResult')
    const $PTHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $PTItemEntity = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $PTBlocks = Java.loadClass('net.minecraft.world.level.block.Blocks')
    const $PTReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $PTRL = Java.loadClass('net.minecraft.resources.ResourceLocation')

    const Y = 200          // holder row in open sky (full light for crops on planters, any world type); stone under it
    const CHUNK = 30       // plants per tick
    // face: the side clicked; pts: (u, v) points on it; ceiling: stone above (hanging holders)
    const TOP = [[0.25, 0.5], [0.75, 0.5], [0.5, 0.25], [0.5, 0.75]]
    const SIDE = [[0.25, 0.5], [0.75, 0.5], [0.5, 0.3], [0.5, 0.7]]
    const HOLDERS = [
        { id: 'minecraft:flower_pot', kind: 'pot', face: 'up', pts: TOP },
        { id: 'framedblocks:framed_flower_pot', kind: 'pot', face: 'up', pts: TOP },
        { id: 'supplementaries:hanging_flower_pot', kind: 'pot', face: 'north', pts: SIDE, ceiling: true },
        { id: 'supplementaries:flower_box[facing=north,floor=true]', kind: 'pot', face: 'up', pts: [[0.17, 0.5], [0.5, 0.5], [0.83, 0.5], [0.5, 0.2]] },
        { id: 'beautify:hanging_pot', kind: 'pot', face: 'up', pts: TOP },
        { id: 'another_furniture:oak_planter_box[facing=north]', kind: 'pot', face: 'up', pts: TOP },
        { id: 'floralench:large_pot', kind: 'pot', face: 'up', pts: TOP },
        { id: 'vinery:flower_pot', kind: 'pot', face: 'up', pts: TOP },
        { id: 'vinery:flower_box', kind: 'pot', face: 'up', pts: TOP },
        { id: 'twigs:silt_pot[filled=true]', kind: 'soil', face: 'up', pts: [[0.5, 0.5]] },
        { id: 'supplementaries:planter', kind: 'soil', face: 'up', pts: [[0.5, 0.5]] },
        { id: 'supplementaries:planter_rich', kind: 'soil', face: 'up', pts: [[0.5, 0.5]] },
    ]
    const X0 = 300, Z0 = 300

    function log(kind, id, detail) { console.info('[pottest] ' + kind + ' ' + id + ': ' + detail) }
    function rl(id) { return new $PTRL(id) }
    function block(id) { return $PTReg.BLOCKS.getValue(rl(id)) }
    function key(reg, o) { try { return String(reg.getKey(o)) } catch (e) { return '?' } }
    function pos(x, y, z) { return new $PTPos(x, y, z) }
    function baseId(spec) { let i = spec.indexOf('['); return i >= 0 ? spec.substring(0, i) : spec }
    function setSpec(level, p, spec) {
        let i = spec.indexOf('[')
        let props = {}
        if (i >= 0) spec.substring(i + 1, spec.length - 1).split(',').forEach(kv => { let a = kv.split('='); props[a[0]] = a[1] })
        level.getBlock(p).set(baseId(spec), props)
    }
    function air(level, p) { level.setBlockAndUpdate(p, $PTBlocks.AIR.defaultBlockState()) }
    // block state + block entity NBT; the block above only for soil holders (a refused plant can place itself on a
    // pot's top, which is not the pot taking it)
    function sig(level, p, withAbove) {
        let st = String(level.getBlockState(p))
        let be = level.getBlockEntity(p)
        let nbt = ''
        if (be != null) { try { nbt = String(be.saveWithoutMetadata()) } catch (e) { nbt = 'nbt?' } }
        return st + (nbt ? ' ' + nbt : '') + (withAbove ? ' | above ' + String(level.getBlockState(p.above())) : '')
    }
    function items(level, p) {
        return level.getEntitiesOfClass($PTItemEntity, new $PTAABB(p.getX() - 3, p.getY() - 3, p.getZ() - 3, p.getX() + 4, p.getY() + 5, p.getZ() + 4))
    }
    function killItems(level, p) { let l = items(level, p); for (let i = 0; i < l.size(); i++) l.get(i).discard() }
    function drops(level, p) {
        let l = items(level, p), out = []
        for (let i = 0; i < l.size(); i++) { let st = l.get(i).getItem(); out.push(st.getCount() + 'x ' + key($PTReg.ITEMS, st.getItem())) }
        return out
    }
    function facePoint(p, D, u, v) {
        let x = p.getX(), y = p.getY(), z = p.getZ()
        if (D == 'north') return new $PTVec3(x + u, y + v, z)
        return new $PTVec3(x + u, y + 1, z + v)
    }

    let fakeN = 0
    function fake(level) {
        fakeN++
        let p = $PTFake.get(level, new $PTProfile($PTUUID.randomUUID(), 'pot' + fakeN))
        p.getInventory().clearContent()
        return p
    }

    function plantList() {
        try {
            let d = JSON.parse(String(JsonIO.readString('kubejs/exported/blocks/flower_pots.json')))
            return d.plants
        } catch (e) { return null }
    }

    // one holder x one plant -> { n: changed clicks, first: state after the first change, drops, survive, err }
    function trial(level, p, H, x, plant) {
        let at = pos(x, Y, Z0)
        for (let dy = -1; dy <= 3; dy++) air(level, pos(x, Y + dy, Z0))
        killItems(level, at)
        level.setBlockAndUpdate(pos(x, Y - 1, Z0), $PTBlocks.STONE.defaultBlockState())
        if (H.ceiling) level.setBlockAndUpdate(at.above(), $PTBlocks.STONE.defaultBlockState())
        setSpec(level, at, H.id)
        let res = { n: 0, first: null, drops: [], survive: null, placedAbove: null, err: null, clicks: [] }
        if (key($PTReg.BLOCKS, level.getBlockState(at).getBlock()) != baseId(H.id)) { res.err = 'holder did not place'; return res }
        let soil = H.kind == 'soil'
        let before = sig(level, at, soil)
        let g = guardThrowable(() => {
            for (let k = 0; k < H.pts.length; k++) {
                p.setItemInHand($PTHand.MAIN_HAND, Item.of(plant.item, 4))
                let pt = H.pts[k]
                let hit = new $PTHit(facePoint(at, H.face, pt[0], pt[1]), $PTDir.byName(H.face), at, false)
                let r = String(p.gameMode.useItemOn(p, level, p.getMainHandItem(), $PTHand.MAIN_HAND, hit))
                let after = sig(level, at, soil)
                res.clicks.push(r)
                if (after != before) { res.n++; if (res.first == null) res.first = after }
                before = after
            }
        })
        if (!g.ok) res.err = String(g.error)
        let upState = level.getBlockState(at.above())
        if (soil) res.survive = upState.isAir() ? null : !!upState.canSurvive(level, at.above())
        else if (!H.ceiling && !upState.isAir()) res.placedAbove = String(upState)
        killItems(level, at)
        p.setItemInHand($PTHand.MAIN_HAND, Item.of('minecraft:diamond_pickaxe'))
        guardThrowable(() => p.gameMode.destroyBlock(at))
        res.drops = drops(level, at)
        killItems(level, at)
        for (let dy = -1; dy <= 3; dy++) air(level, pos(x, Y + dy, Z0))
        return res
    }

    let out = { holders: {} }
    function runHolder(server, level, hi, done) {
        let H = HOLDERS[hi]
        let plants = plantList()
        if (plants == null) { log('FAIL', 'PP-00', 'no kubejs/exported/blocks/flower_pots.json: run /dumppots first'); done(); return }
        let p = fake(level)
        let x = X0 + hi * 4
        let rows = {}
        let i = 0
        let step = () => {
            for (let k = 0; k < CHUNK && i < plants.length; k++, i++) rows[plants[i].item] = trial(level, p, H, x, plants[i])
            if (i < plants.length) { server.scheduleInTicks(1, step); return }
            out.holders[H.id] = { kind: H.kind, rows: rows }
            summarise(H, rows, plants)
            p.discard()
            done()
        }
        step()
    }

    function summarise(H, rows, plants) {
        let acc = 0, errs = []
        plants.forEach(pl => { let r = rows[pl.item]; if (r.n > 0 || (r.survive === true)) acc++; if (r.err) errs.push(pl.item + ' (' + r.err.substring(0, 120) + ')') })
        log('INFO', 'PP-SUM', H.id + ': ' + acc + ' of ' + plants.length + ' plants accepted')
        log(errs.length ? 'FAIL' : 'PASS', 'PP-03', H.id + ' no exception' + (errs.length ? ': ' + errs.join('; ') : ''))
        let base = baseId(H.id)
        // PP-01 / PP-02: vanilla, framed and hanging pots take exactly the vanilla full-pots map
        if (base == 'minecraft:flower_pot' || base == 'framedblocks:framed_flower_pot' || base == 'supplementaries:hanging_flower_pot') {
            let bad = []
            plants.forEach(pl => {
                let r = rows[pl.item]
                if ((pl.vanillaPot != null) != (r.n > 0)) bad.push(pl.item + (pl.vanillaPot ? ' refused' : ' taken'))
                if (base == 'minecraft:flower_pot' && pl.vanillaPot && r.first && r.first.indexOf(pl.vanillaPot) < 0) bad.push(pl.item + ' -> ' + r.first.substring(0, 80))
            })
            log(bad.length ? 'FAIL' : 'PASS', base == 'minecraft:flower_pot' ? 'PP-01' : 'PP-02', H.id + ' matches the vanilla map' + (bad.length ? ': ' + bad.join(', ') : ''))
        }
        // PP-04: an accepted plant comes back when the holder breaks
        if (H.kind == 'pot') {
            let lost = []
            plants.forEach(pl => {
                let r = rows[pl.item]
                if (r.n > 0 && !r.drops.some(d => d.indexOf(' ' + pl.item) >= 0) && r.drops.length < 2) lost.push(pl.item + ' [' + r.drops.join(', ') + ']')
            })
            log(lost.length ? 'FAIL' : 'PASS', 'PP-04', H.id + ' returns the plant on break' + (lost.length ? ': ' + lost.join('; ') : ''))
        }
    }

    function runAll(server, level, list) {
        let k = 0
        let next = () => {
            if (k >= list.length) {
                writeExportJson('kubejs/exported/blocks/pot_probe.json', out)
                log('INFO', 'PP-DONE', list.length + ' holders, wrote kubejs/exported/blocks/pot_probe.json')
                return
            }
            let hi = list[k++]
            server.scheduleInTicks(2, () => runHolder(server, level, hi, next))
        }
        next()
    }

    ServerEvents.commandRegistry(event => {
        const Commands = Java.loadClass('net.minecraft.commands.Commands')
        const StringArg = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
        event.register(
            Commands.literal('pottest')
                .requires(src => src.hasPermission(2))
                .then(Commands.argument('holder', StringArg.greedyString())
                    .executes(ctx => {
                        let which = String(StringArg.getString(ctx, 'holder'))
                        let level = ctx.source.getLevel()
                        let server = ctx.source.getServer()
                        if (which == 'list') { HOLDERS.forEach((H, i) => log('INFO', 'PP-LIST', i + ' ' + H.id)); return 1 }
                        let list = []
                        HOLDERS.forEach((H, i) => { if (which == 'all' || baseId(H.id) == which) list.push(i) })
                        if (!list.length) { log('FAIL', 'PP-00', 'unknown holder ' + which); return 0 }
                        out = { holders: {} }
                        runAll(server, level, list)
                        return 1
                    })))
    })
})()
