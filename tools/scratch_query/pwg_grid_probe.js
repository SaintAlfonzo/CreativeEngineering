// Scratch-only probe for the Create: Power Grid port, phase P2 rows (SQ-PWG-12 onward, plan_power_grid.md P2). Copy into
// a scratch server's kubejs/server_scripts as zz_pwg_grid_probe.js, never into the pack. Builds test circuits from the
// P2 blocks (creative voltage source, creative resistor, gauges, wire connectors) joined by hanging wires made the way a
// player's wire item makes them (HangingWireEntity.create between block terminals), and reads the server-side values.
// No chunk loads here: `forceload add` the area first (Rhino deadlock, scratch-test gotchas). Blocks go at Y (put Y one
// above the floor). Resistor values are the creative resistor's scroll steps: RIDX 18 = 0.1 ohm, 27 = 1, 36 = 10,
// 45 = 100, 54 = 1 k (value = (i % 9 + 1) * 10^(i / 9 - 3)).
//   loop X Y Z V RIDX [WIRE] [LEN]  source at X, current gauge at X+3, resistor at X+3+LEN (LEN default 3), voltage gauge
//                                   2 blocks south of the resistor across it; wires source+ -> gauge -> resistor -> source-
//                                   (WIRE default powergrid:wire; the gauge-to-resistor wire is LEN blocks long)
//   read X Y Z [LEN]                values of a loop: source V, current gauge, voltage gauge, resistor R, wires and their
//                                   resistance, the current gauge's own series resistance (Rgauge, config), expected I =
//                                   V / (R + Rpath + Rgauge), Rpath = current-path wires (not the voltage gauge's sense
//                                   wires), and a second line drop vs I x (Rpath + Rgauge) with pass (0.5 %); prints `read overheated <part>` if a part has overheated
//   set X Y Z V                     set the creative source at X Y Z to V volts (the /powergrid source command needs a player)
//   cut X Y Z [LEN]                 kill the gauge-to-resistor wire of a loop, picked by its two endpoints (opens it, as
//                                   wire cutters do)
//   wires X Y Z R                   power grid wire entities in a box: count, type, temperature, overheated, resistance
//   items X Y Z R                   item entities in a box (burnt wires must drop nothing)
//   ladder X Y Z N [WIRE]           N creative resistors (1 k) in series, rows of 20 two blocks apart, rows 3 apart (south),
//                                   source at X Y Z, return path through wire connectors on the west edge; about 2N nodes
//   grids X Y Z COUNT N             COUNT separate ladders of N, side by side (east, 50 blocks apart)
//   overheat X Y Z                  12 V source and a 0.1 ohm resistor at X+3 on copper wire (about 120 A, copper is rated
//                                   80 A), no gauge in the loop; read it with `ohm X+3 Y Z`
//   ohm X Y Z                       current through the creative resistor at X Y Z from its terminal voltages
//   overheated X Y Z R              overheated components in a box with their stored block and data keys
//   net X Y Z                       the electric behaviour at X Y Z: paused, node count, network size
//   watch X Y Z R [T]               arm a server tick watcher on the wires in a box for T ticks (300): first tick each
//                                   wire shows overheated=true (the flag lives about 5 ticks, sampling misses it)
//   watched                         the watcher's result: overheatedWires, firstOverheatedTick, pass (at least one)
//   chunk X Y Z                     the chunk holding block X Z: loaded, block ticking (ticket level <= 32), entity
//                                   ticking (<= 31); never loads it
// Output: "[zpwgg] ..." lines in logs/latest.log, then "[zpwgg] done".
(() => {
    const $Args = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $FR = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $RL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $Pos = Java.loadClass('net.minecraft.core.BlockPos')
    const $AABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $Stack = Java.loadClass('net.minecraft.world.item.ItemStack')
    const $ItemEntity = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $BEB = Java.loadClass('com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour')
    const $Scroll = Java.loadClass('com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour')
    // Power Grid classes load on first use, so the probe still registers on a boot without the jar
    let PG = null
    function pg() {
        if (PG === null) PG = {
            Hanging: Java.loadClass('org.patryk3211.powergrid.electricity.wire.HangingWireEntity'),
            BaseWire: Java.loadClass('org.patryk3211.powergrid.electricity.wire.BaseWireEntity'),
            Endpoint: Java.loadClass('org.patryk3211.powergrid.electricity.wire.BlockWireEndpoint'),
            Electric: Java.loadClass('org.patryk3211.powergrid.electricity.base.ElectricBehaviour'),
            Overheated: Java.loadClass('org.patryk3211.powergrid.electricity.overheated.OverheatedComponentBlockEntity'),
            RValues: Java.loadClass('org.patryk3211.powergrid.config.ResistanceValues'),
        }
        return PG
    }
    function out(s) { console.info('[zpwgg] ' + s) }
    function pos(x, y, z) { return new $Pos(x, y, z) }
    function box(x, y, z, r) { return new $AABB(x - r, y - r, z - r, x + r + 1, y + r + 1, z + r + 1) }
    function block(id) { return $FR.BLOCKS.getValue(new $RL(id)) }
    function item(id) { return $FR.ITEMS.getValue(new $RL(id)) }
    function place(level, x, y, z, id) {
        level.setBlock(pos(x, y, z), block(id).defaultBlockState(), 3)
        return level.getBlockEntity(pos(x, y, z))
    }
    function ohms(i) { return (i % 9 + 1) * Math.pow(10, Math.floor(i / 9) - 3) }
    function setResistor(level, p, idx) {
        let be = level.getBlockEntity(p)
        let s = $BEB.get(be, $Scroll.TYPE)
        if (s == null) throw 'no scroll behaviour at ' + p
        s.setValue(idx)
    }
    // A hanging wire between two block terminals, sized as the wire item sizes it (0.5 items per metre, at least 1).
    function wire(level, p1, t1, p2, t2, wireId) {
        let dx = p1.getX() - p2.getX(), dy = p1.getY() - p2.getY(), dz = p1.getZ() - p2.getZ()
        let count = Math.max(1, Math.round(Math.sqrt(dx * dx + dy * dy + dz * dz) * 0.5))
        let e = pg().Hanging.create(level, new (pg().Endpoint)(p1, t1), new (pg().Endpoint)(p2, t2), new $Stack(item(wireId), count), null)
        if (!level.tryAddFreshEntityWithPassengers(e)) throw 'wire spawn failed ' + p1 + ' -> ' + p2
        return e
    }
    function loopParts(x, y, z, len) {
        let rx = x + 3 + len
        return { src: pos(x, y, z), cg: pos(x + 3, y, z), res: pos(rx, y, z), vg: pos(rx, y, z + 2) }
    }
    function wiresIn(level, b) {
        let o = []
        level.getEntitiesOfClass(pg().BaseWire, b).forEach(e => o.push(e))
        return o
    }
    function buildLoop(level, x, y, z, v, ridx, wireId, len) {
        let p = loopParts(x, y, z, len)
        let src = place(level, x, y, z, 'powergrid:creative_voltage_source')
        place(level, p.cg.getX(), y, z, 'powergrid:current_gauge')
        place(level, p.res.getX(), y, z, 'powergrid:creative_resistor')
        place(level, p.vg.getX(), y, p.vg.getZ(), 'powergrid:voltage_gauge')
        src.setValue(v)
        src.notifyUpdate()
        setResistor(level, p.res, ridx)
        wire(level, p.src, 0, p.cg, 0, wireId)
        wire(level, p.cg, 1, p.res, 0, wireId)
        wire(level, p.res, 1, p.src, 1, wireId)
        wire(level, p.res, 0, p.vg, 0, wireId)
        wire(level, p.res, 1, p.vg, 1, wireId)
        return p
    }
    function loaded(level, p) { return level.getChunkSource().hasChunk(p.getX() >> 4, p.getZ() >> 4) }
    function readLoop(level, x, y, z, len) {
        let p = loopParts(x, y, z, len)
        // never load a chunk from here: report a part in an unloaded chunk instead
        let gone = []
        ;[['source', p.src], ['current_gauge', p.cg], ['resistor', p.res], ['voltage_gauge', p.vg]].forEach(b => { if (!loaded(level, b[1])) gone.push(b[0]) })
        if (gone.length > 0) { out('read unloaded ' + gone.join(',')); return }
        let src = level.getBlockEntity(p.src), cg = level.getBlockEntity(p.cg), res = level.getBlockEntity(p.res), vg = level.getBlockEntity(p.vg)
        if (src == null || cg == null || res == null || vg == null) { out('read missing block entity'); return }
        let hot = []
        ;[['source', src], ['current_gauge', cg], ['resistor', res], ['voltage_gauge', vg]].forEach(b => { if (b[1] instanceof pg().Overheated) hot.push(b[0]) })
        if (hot.length > 0) { out('read overheated ' + hot.join(',')); return }
        let V = src.getValue(), R = res.getValue(), I = cg.getValue(), Vr = vg.getValue()
        let Rg = pg().RValues.get(block('powergrid:current_gauge'))
        let ws = wiresIn(level, new $AABB(x - 1, y - 2, z - 1, p.res.getX() + 2, y + 3, z + 3))
        let rw = 0, rp = 0
        // the voltage gauge's two sense wires carry no current: the current path is the wires with no end on the gauge
        let onVg = e => e instanceof pg().Endpoint && e.getPos().equals(p.vg)
        ws.forEach(w => {
            rw += w.getResistance()
            if (!onVg(w.getEndpoint1()) && !onVg(w.getEndpoint2())) rp += w.getResistance()
        })
        let drop = V - Vr, expDrop = I * (rp + Rg)
        let err = expDrop == 0 ? 0 : Math.abs(drop - expDrop) / Math.abs(expDrop) * 100
        out('read V=' + V + ' I=' + I + ' Vr=' + Vr + ' R=' + R + ' wires=' + ws.length + ' Rwires=' + rw
            + ' Rpath=' + rp + ' Rgauge=' + Rg + ' expectedI=' + (V / (R + rp + Rg)) + ' drop=' + drop)
        out('read drop=' + drop + ' expectedDrop=' + expDrop + ' (I x (Rpath + Rgauge)) err%=' + err.toFixed(3)
            + ' pass=' + (err <= 0.5))
    }
    function ladder(level, x, y, z, n, wireId) {
        let perRow = 20
        let src = place(level, x, y, z, 'powergrid:creative_voltage_source')
        src.setValue(100)
        src.notifyUpdate()
        let prev = pos(x, y, z), prevT = 0, k = 0, last = null
        for (let i = 0; i < n; i++) {
            let row = Math.floor(i / perRow), col = i % perRow
            let cx = (row % 2 == 0) ? x + 2 + col * 2 : x + 2 + (perRow - 1 - col) * 2
            let p = pos(cx, y, z + row * 3)
            place(level, cx, y, z + row * 3, 'powergrid:creative_resistor')
            setResistor(level, p, 54)
            wire(level, prev, prevT, p, 0, wireId)
            prev = p; prevT = 1; last = p
        }
        // return path: connectors on the west edge (x - 2), at most 9 blocks apart, back to the source's minus terminal
        let rows = Math.floor((n - 1) / perRow) + 1
        let zEnd = z + (rows - 1) * 3
        let c = pos(x - 2, y, zEnd)
        place(level, x - 2, y, zEnd, 'powergrid:wire_connector')
        wire(level, last, 1, c, 0, wireId)
        let cz = zEnd
        while (cz > z) {
            let nz = Math.max(z, cz - 9)
            let c2 = pos(x - 2, y, nz)
            if (nz != z) place(level, x - 2, y, nz, 'powergrid:wire_connector')
            if (nz == z) { wire(level, c, 0, pos(x, y, z), 1, wireId) } else { wire(level, c, 0, c2, 0, wireId); c = c2 }
            cz = nz
        }
        if (zEnd == z) wire(level, c, 0, pos(x, y, z), 1, wireId)
        return rows
    }
    function run(level, a) {
        let cmd = a[0]
        let X = parseInt(a[1]), Y = parseInt(a[2]), Z = parseInt(a[3])
        if (cmd == 'loop') {
            let len = a[7] ? parseInt(a[7]) : 3
            buildLoop(level, X, Y, Z, parseFloat(a[4]), parseInt(a[5]), a[6] && a[6] != '-' ? a[6] : 'powergrid:wire', len)
            out('loop built at ' + X + ' ' + Y + ' ' + Z + ' V=' + a[4] + ' R=' + ohms(parseInt(a[5])) + ' len=' + len)
        } else if (cmd == 'read') {
            readLoop(level, X, Y, Z, a[4] ? parseInt(a[4]) : 3)
        } else if (cmd == 'set') {
            let be = level.getBlockEntity(pos(X, Y, Z))
            be.setValue(parseFloat(a[4]))
            be.notifyUpdate()
            out('set ' + be.getValue())
        } else if (cmd == 'cut') {
            let len = a[4] ? parseInt(a[4]) : 3
            let p = loopParts(X, Y, Z, len)
            // only the wire from the gauge's terminal 1 to the resistor's terminal 0 (by endpoint, not by box)
            let ws = wiresIn(level, new $AABB(X - 1, Y - 2, Z - 1, p.res.getX() + 2, Y + 3, Z + 3)).filter(w => {
                let a = w.getEndpoint1(), b = w.getEndpoint2()
                let is = (e, q, t) => e instanceof pg().Endpoint && e.getPos().equals(q) && e.getTerminal() == t
                return (is(a, p.cg, 1) && is(b, p.res, 0)) || (is(a, p.res, 0) && is(b, p.cg, 1))
            })
            ws.forEach(w => w.kill())
            out('cut ' + ws.length)
        } else if (cmd == 'wires') {
            let ws = wiresIn(level, box(X, Y, Z, parseInt(a[4])))
            out('wires ' + ws.length)
            ws.slice(0, 12).forEach(w => out('wire ' + w.getEncodeId() + ' T=' + w.getTemperature() + ' overheated=' + w.isOverheated()
                + ' R=' + w.getResistance() + ' at ' + Math.round(w.getX()) + ' ' + Math.round(w.getY()) + ' ' + Math.round(w.getZ())))
        } else if (cmd == 'items') {
            let n = 0
            level.getEntitiesOfClass($ItemEntity, box(X, Y, Z, parseInt(a[4]))).forEach(e => {
                n++; out('item ' + e.getItem().getItem() + ' x' + e.getItem().getCount())
            })
            out('items ' + n)
        } else if (cmd == 'ladder') {
            let rows = ladder(level, X, Y, Z, parseInt(a[4]), a[5] ? a[5] : 'powergrid:wire')
            out('ladder built n=' + a[4] + ' rows=' + rows)
        } else if (cmd == 'grids') {
            let count = parseInt(a[4]), n = parseInt(a[5])
            for (let g = 0; g < count; g++) ladder(level, X + g * 50, Y, Z, n, 'powergrid:wire')
            out('grids built count=' + count + ' n=' + n)
        } else if (cmd == 'overheat') {
            // no gauge in this loop (a current gauge, rated 35 W, overheats before the wire): source at X, creative
            // resistor 0.1 ohm at X+3, two copper wires; read the current with `ohm X Y Z` (resistor terminal voltages)
            let src = place(level, X, Y, Z, 'powergrid:creative_voltage_source')
            place(level, X + 3, Y, Z, 'powergrid:creative_resistor')
            src.setValue(12)
            src.notifyUpdate()
            setResistor(level, pos(X + 3, Y, Z), 18)
            wire(level, pos(X, Y, Z), 0, pos(X + 3, Y, Z), 0, 'powergrid:wire')
            wire(level, pos(X + 3, Y, Z), 1, pos(X, Y, Z), 1, 'powergrid:wire')
            out('overheat loop built at ' + X + ' ' + Y + ' ' + Z)
        } else if (cmd == 'ohm') {
            // current through the resistor at X Y Z from its terminal voltages: I = (V0 - V1) / R
            let be = level.getBlockEntity(pos(X, Y, Z))
            if (be == null || be instanceof pg().Overheated) { out('ohm ' + (be == null ? 'none' : 'overheated')); return }
            let eb = $BEB.get(be, pg().Electric.TYPE)
            let v0 = eb.getTerminal(0).getVoltage(), v1 = eb.getTerminal(1).getVoltage(), R = be.getValue()
            out('ohm V0=' + v0 + ' V1=' + v1 + ' R=' + R + ' I=' + ((v0 - v1) / R))
        } else if (cmd == 'overheated') {
            let r = parseInt(a[4]), n = 0
            for (let dx = -r; dx <= r; dx++) for (let dy = -r; dy <= r; dy++) for (let dz = -r; dz <= r; dz++) {
                let be = level.getBlockEntity(pos(X + dx, Y + dy, Z + dz))
                if (be == null || !(be instanceof pg().Overheated)) continue
                n++
                let st = be.getOriginalState(), d = be.getOriginalData()
                out('overheated at ' + (X + dx) + ' ' + (Y + dy) + ' ' + (Z + dz) + ' original=' + (st == null ? 'null' : st.getBlock())
                    + ' data=' + (d == null ? 'null' : d.getAllKeys()))
            }
            out('overheated ' + n)
        } else if (cmd == 'watch') {
            // arm a server tick watcher on the wires in a box for T ticks (default 300): records the first tick each
            // wire shows overheated=true (the flag lives about 5 ticks before the wire is removed, too short to sample)
            WATCH = { level: level, box: box(X, Y, Z, parseInt(a[4])), left: a[5] ? parseInt(a[5]) : 300, tick: 0, seen: {}, maxWires: 0 }
            out('watch armed ' + WATCH.left + ' ticks')
        } else if (cmd == 'watched') {
            if (WATCH == null) { out('watched none'); return }
            let ids = Object.keys(WATCH.seen)
            let first = ids.length == 0 ? -1 : Math.min.apply(null, ids.map(k => WATCH.seen[k]))
            out('watched ticks=' + WATCH.tick + ' active=' + (WATCH.left > 0) + ' maxWires=' + WATCH.maxWires
                + ' overheatedWires=' + ids.length + ' firstOverheatedTick=' + first + ' pass=' + (ids.length >= 1))
        } else if (cmd == 'chunk') {
            // chunk state at block X Z (the Y argument is ignored), without loading it
            let p = pos(X, 0, Z)
            let cx = X >> 4, cz = Z >> 4
            let has = level.getChunkSource().hasChunk(cx, cz)
            let blocks = has && level.shouldTickBlocksAt(p)
            let entities = has && level.isPositionEntityTicking(p)
            out('chunk ' + cx + ' ' + cz + ' loaded=' + has + ' blockTicking=' + blocks + ' entityTicking=' + entities)
        } else if (cmd == 'net') {
            let be = level.getBlockEntity(pos(X, Y, Z))
            let eb = be == null ? null : $BEB.get(be, pg().Electric.TYPE)
            if (eb == null) { out('net none'); return }
            let nodes = eb.getExternalNodes()
            let net = nodes.isEmpty() ? null : nodes.get(0).getNetwork()
            out('net paused=' + eb.isPaused() + ' external=' + nodes.size() + ' internal=' + eb.getInternalNodes().size()
                + ' networkNodes=' + (net == null ? 'none' : net.getNodes().size()))
        } else out('unknown subcommand ' + cmd)
    }
    let WATCH = null
    ServerEvents.tick(e => {
        if (WATCH == null || WATCH.left <= 0) return
        try {
            WATCH.left--
            WATCH.tick++
            let ws = wiresIn(WATCH.level, WATCH.box)
            if (ws.length > WATCH.maxWires) WATCH.maxWires = ws.length
            ws.forEach(w => {
                let k; try { k = String(w.getId()) } catch (e1) { try { k = String(w.getUUID()) } catch (e2) { k = String(w) } } // getStringUUID is not reachable from Rhino (r1113)
                if (w.isOverheated() && WATCH.seen[k] === undefined) WATCH.seen[k] = WATCH.tick
            })
        } catch (err) { out('watch error ' + err); WATCH.left = 0 }
    })
    ServerEvents.commandRegistry(e => {
        const C = e.commands
        e.register(C.literal('zpwgg').requires(s => s.hasPermission(2))
            .then(C.argument('args', $Args.greedyString()).executes(ctx => {
                try {
                    run(ctx.source.getLevel(), String($Args.getString(ctx, 'args')).split(' '))
                } catch (err) { out('error ' + err) }
                out('done')
                return 1
            })))
    })
})()
