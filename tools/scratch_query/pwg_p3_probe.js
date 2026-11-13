// Scratch-only probe for the Create: Power Grid port, phase P3 rows (SQ-PWG-24 onward, plan_power_grid.md P3). Copy into
// a scratch server's kubejs/server_scripts as zz_pwg_p3_probe.js next to zz_pwg_grid_probe.js (P2 probe: loop, read,
// set, wires, overheated) and zz_pwg_probe.js; never into the pack. Builds circuits from P2 and P3 blocks joined by
// hanging wires made the way the wire item makes them, and reads server-side values.
// No chunk loads here: `forceload add` the area first (Rhino deadlock, scratch-test gotchas).
//   place X Y Z ID [prop=value ...]     set a block (block state properties optional, e.g. facing=up axis=x)
//   w X1 Y1 Z1 T1 X2 Y2 Z2 T2 [WIRE]    hanging wire from terminal T1 of the block at 1 to terminal T2 at 2 (default
//                                       powergrid:wire)
//   src X Y Z V [FREQ]                  creative voltage source at X Y Z set to V volts (FREQ > 0: AC amplitude V)
//   res X Y Z IDX                       resistor scroll index on a creative resistor or a power resistor (0.1 ohm = 18,
//                                       1 = 27, 10 = 36, 100 = 45, 1 k = 54; value = (i % 9 + 1) * 10^(i / 9 - 3))
//   scroll X Y Z VALUE                  any scroll value (creative motor RPM, spark gap, carbon pile trim)
//   recipes NS                          every loaded recipe whose id is in namespace NS: id, type, output
//   xfmr X Y Z N1 N2 [ITEM]             wind a transformer: primary terminals 0-1 N1 turns, secondary 2-3 N2 turns
//   be X Y Z                            block, state and readout: terminal voltages, gauge value, temperature, and per
//                                       device: coils, battery energy, energy meter, fuse state and rating, field
//                                       strength, FE per side, scroll value, goggle lines (server side: lang keys, values)
//   fuse X Y Z AMPS|reset               fuse holder: set the rating (A) or reset a blown fuse
//   fe X Y Z SIDE AMOUNT                receiveEnergy(AMOUNT) on the Forge Energy capability of the block at X Y Z from SIDE
//   feget X Y Z SIDE                    stored FE and capacity on that side, with the game time
//   drop X Y Z ITEM N                   N item entities of ITEM at the block centre of X Y Z (one entity each)
//   count X Y Z R ITEM                  item entities of ITEM (sum of counts) in a box of radius R
// Output: "[zpwg3] ..." lines in logs/latest.log, then "[zpwg3] done".
(() => {
    const $Args = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $FR = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $RL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $Pos = Java.loadClass('net.minecraft.core.BlockPos')
    const $Dir = Java.loadClass('net.minecraft.core.Direction')
    const $AABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $Stack = Java.loadClass('net.minecraft.world.item.ItemStack')
    const $ItemEntity = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $BEB = Java.loadClass('com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour')
    const $Scroll = Java.loadClass('com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour')
    const $Caps = Java.loadClass('net.minecraftforge.common.capabilities.ForgeCapabilities')
    const $Goggles = Java.loadClass('com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation')
    const $List = Java.loadClass('java.util.ArrayList')
    let PG = null
    function pg() {
        if (PG === null) PG = {
            Hanging: Java.loadClass('org.patryk3211.powergrid.electricity.wire.HangingWireEntity'),
            Endpoint: Java.loadClass('org.patryk3211.powergrid.electricity.wire.BlockWireEndpoint'),
            Electric: Java.loadClass('org.patryk3211.powergrid.electricity.base.ElectricBehaviour'),
            Thermal: Java.loadClass('org.patryk3211.powergrid.electricity.base.ThermalBehaviour'),
            Transformer: Java.loadClass('org.patryk3211.powergrid.electricity.transformer.TransformerBlockEntity'),
            Battery: Java.loadClass('org.patryk3211.powergrid.electricity.battery.BatteryBlockEntity'),
            Fuse: Java.loadClass('org.patryk3211.powergrid.electricity.fuse.FuseHolderBlockEntity'),
            FuseState: Java.loadClass('org.patryk3211.powergrid.electricity.fuse.FuseState'),
            Switch: Java.loadClass('org.patryk3211.powergrid.electricity.electricswitch.SwitchBlockEntity'),
            Magnet: Java.loadClass('org.patryk3211.powergrid.electricity.electromagnet.ElectromagnetBlockEntity'),
            Gauge: Java.loadClass('org.patryk3211.powergrid.electricity.gauge.GaugeBlockEntity'),
            EnergyMeter: Java.loadClass('org.patryk3211.powergrid.electricity.gauge.EnergyMeterBlockEntity'),
        }
        return PG
    }
    // game time: ServerLevel.getGameTime is not reachable from Rhino here (r1113); fall back through the server tick count
    function gtime(level) {
        try { return level.getLevelData().getGameTime() } catch (e1) {}
        try { return level.getServer().getTickCount() } catch (e2) {}
        return 'ms' + Date.now()
    }
    function out(s) { console.info('[zpwg3] ' + s) }
    function pos(x, y, z) { return new $Pos(x, y, z) }
    function block(id) { return $FR.BLOCKS.getValue(new $RL(id)) }
    function item(id) { return $FR.ITEMS.getValue(new $RL(id)) }
    function dir(name) { return $Dir.byName(name) }
    function ohms(i) { return (i % 9 + 1) * Math.pow(10, Math.floor(i / 9) - 3) }
    function setProp(state, name, value) {
        let p = null
        state.getProperties().forEach(q => { if (q.getName() == name) p = q })
        if (p == null) throw 'no property ' + name
        let v = p.getValue(value)
        if (!v.isPresent()) throw 'bad value ' + name + '=' + value
        return state.setValue(p, v.get())
    }
    function wire(level, p1, t1, p2, t2, wireId) {
        let dx = p1.getX() - p2.getX(), dy = p1.getY() - p2.getY(), dz = p1.getZ() - p2.getZ()
        let count = Math.max(1, Math.round(Math.sqrt(dx * dx + dy * dy + dz * dz) * 0.5))
        let e = pg().Hanging.create(level, new (pg().Endpoint)(p1, t1), new (pg().Endpoint)(p2, t2), new $Stack(item(wireId), count), null)
        if (!level.tryAddFreshEntityWithPassengers(e)) throw 'wire spawn failed ' + p1 + ' -> ' + p2
        return e
    }
    function fmt(x) { return (Math.round(x * 1e6) / 1e6) }
    function describe(level, p) {
        let be = level.getBlockEntity(p)
        let st = level.getBlockState(p)
        out('be ' + st)
        if (be == null) { out('be none'); return }
        let eb = $BEB.get(be, pg().Electric.TYPE)
        if (eb != null) {
            let vs = []
            for (let i = 0; i < 8; i++) if (eb.hasTerminal(i)) vs.push('T' + i + '=' + fmt(eb.getTerminal(i).getVoltage()))
            out('terminals ' + vs.join(' ') + ' paused=' + eb.isPaused())
        }
        let th = $BEB.get(be, pg().Thermal.TYPE)
        if (th != null) out('thermal T=' + fmt(th.getTemperature()) + ' overheated=' + th.isOverheated())
        if (be instanceof pg().Gauge) out('gauge ' + be.getValue())
        if (be instanceof pg().Transformer) {
            let a = be.getPrimary(), b = be.getSecondary()
            out('coils primary=' + (a.isDefined() ? a.getTerminal1() + '-' + a.getTerminal2() + 'x' + a.getTurns() : 'none')
                + ' secondary=' + (b.isDefined() ? b.getTerminal1() + '-' + b.getTerminal2() + 'x' + b.getTurns() : 'none'))
        }
        if (be instanceof pg().Battery) out('battery energy=' + fmt(be.getEnergy()) + ' capacity=' + fmt(be.getCapacity())
            + ' percent=' + fmt(be.getPercent()))
        if (be instanceof pg().Magnet) out('field ' + fmt(be.getFieldStrength()))
        if (be instanceof pg().Fuse) {
            let s = $BEB.get(be, $Scroll.TYPE)
            out('fuse state=' + st.getValues().toString() + ' rating=' + (s == null ? '?' : s.getValue()))
        }
        if (be instanceof pg().EnergyMeter) out('meter energy=' + fmt(be.getEnergy()))
        if (be instanceof $Goggles) {
            // gauges read Minecraft.getInstance() in their tooltip: client-only, skip on a dedicated server (r1113)
            try {
                let lines = new $List()
                be.addToGoggleTooltip(lines, true)
                lines.forEach(c => out('goggles ' + String(c.getString()).trim()))
            } catch (err) { out('goggles skipped (client-only tooltip)') }
        }
        let sc = $BEB.get(be, $Scroll.TYPE)
        if (sc != null && !(be instanceof pg().Fuse)) out('scroll ' + sc.getValue())
        for (let d of ['up', 'down', 'north', 'south', 'east', 'west']) {
            let cap = be.getCapability($Caps.ENERGY, dir(d))
            if (cap.isPresent()) { let s = cap.resolve().get(); out('fe ' + d + ' ' + s.getEnergyStored() + '/' + s.getMaxEnergyStored()) }
        }
    }
    function run(level, a) {
        let cmd = a[0]
        let X = parseInt(a[1]), Y = parseInt(a[2]), Z = parseInt(a[3])
        let P = pos(X, Y, Z)
        if (cmd == 'place') {
            let st = block(a[4]).defaultBlockState()
            for (let i = 5; i < a.length; i++) { let kv = a[i].split('='); st = setProp(st, kv[0], kv[1]) }
            level.setBlock(P, st, 3)
            out('placed ' + level.getBlockState(P))
        } else if (cmd == 'w') {
            let e = wire(level, P, parseInt(a[4]), pos(parseInt(a[5]), parseInt(a[6]), parseInt(a[7])), parseInt(a[8]), a[9] ? a[9] : 'powergrid:wire')
            out('wire ' + e.getEncodeId() + ' R=' + e.getResistance())
        } else if (cmd == 'src') {
            level.setBlock(P, block('powergrid:creative_voltage_source').defaultBlockState(), 3)
            let be = level.getBlockEntity(P)
            let f = a[5] ? parseFloat(a[5]) : 0
            if (f > 0) be.setValue(parseFloat(a[4]), f, 0); else be.setValue(parseFloat(a[4]))
            be.notifyUpdate()
            out('src ' + be.getValue())
        } else if (cmd == 'res') {
            let s = $BEB.get(level.getBlockEntity(P), $Scroll.TYPE)
            if (s == null) throw 'no scroll behaviour at ' + P
            s.setValue(parseInt(a[4]))
            out('res idx=' + a[4] + ' ohms=' + ohms(parseInt(a[4])))
        } else if (cmd == 'scroll') {
            let s = $BEB.get(level.getBlockEntity(P), $Scroll.TYPE)
            if (s == null) throw 'no scroll behaviour at ' + P
            s.setValue(parseInt(a[4]))
            out('scroll ' + s.getValue())
        } else if (cmd == 'recipes') {
            let n = 0
            level.getRecipeManager().getRecipes().forEach(r => {
                let id = String(r.getId())
                if (id.indexOf(a[1] + ':') != 0) return
                n++
                out('recipe ' + id + ' type=' + r.getType() + ' output=' + r.getResultItem())
            })
            out('recipes ' + n)
        } else if (cmd == 'xfmr') {
            let be = level.getBlockEntity(P)
            let it = item(a[6] ? a[6] : 'powergrid:wire')
            be.makePrimary(0, 1, parseInt(a[4]), it)
            be.makeSecondary(2, 3, parseInt(a[5]), it)
            out('xfmr ' + a[4] + ':' + a[5])
        } else if (cmd == 'be') {
            describe(level, P)
        } else if (cmd == 'fuse') {
            let be = level.getBlockEntity(P)
            if (a[4] == 'reset') out('fuse reset ' + be.resetFuse())
            else { $BEB.get(be, $Scroll.TYPE).setValue(parseInt(a[4])); out('fuse rating ' + a[4]) }
        } else if (cmd == 'fe' || cmd == 'feget') {
            let be = level.getBlockEntity(P)
            let cap = be == null ? null : be.getCapability($Caps.ENERGY, dir(a[4]))
            if (cap == null || !cap.isPresent()) { out('fe none on ' + a[4]); return }
            let s = cap.resolve().get()
            if (cmd == 'fe') out('fe received ' + s.receiveEnergy(parseInt(a[5]), false))
            out('fe stored ' + s.getEnergyStored() + '/' + s.getMaxEnergyStored() + ' t=' + gtime(level))
        } else if (cmd == 'drop') {
            let n = parseInt(a[5])
            for (let i = 0; i < n; i++) {
                // the 8-argument constructor sets the motion (setDeltaMovement(3 numbers) is ambiguous in Rhino, r1113)
                let e = new $ItemEntity(level, X + 0.5, Y + 0.2, Z + 0.5, new $Stack(item(a[4]), 1), 0.0, 0.0, 0.0)
                level.addFreshEntity(e)
            }
            out('dropped ' + n + ' ' + a[4])
        } else if (cmd == 'count') {
            let r = parseInt(a[4]), n = 0, ents = 0
            let it = item(a[5])
            level.getEntitiesOfClass($ItemEntity, new $AABB(X - r, Y - r, Z - r, X + r + 1, Y + r + 1, Z + r + 1)).forEach(e => {
                if (e.getItem().getItem() == it) { n += e.getItem().getCount(); ents++ }
            })
            out('count ' + a[5] + ' ' + n + ' entities=' + ents)
        } else out('unknown subcommand ' + cmd)
    }
    ServerEvents.commandRegistry(e => {
        const C = e.commands
        e.register(C.literal('zpwg3').requires(s => s.hasPermission(2))
            .then(C.argument('args', $Args.greedyString()).executes(ctx => {
                try {
                    run(ctx.source.getLevel(), String($Args.getString(ctx, 'args')).split(' '))
                } catch (err) { out('error ' + err) }
                out('done')
                return 1
            })))
    })
})()
