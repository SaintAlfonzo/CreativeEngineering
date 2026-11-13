// Scratch-only probe for the Create: Power Grid port, phase P4 rows (SQ-PWG-35 onward and SQ-PWG-07,
// plan_power_grid.md P4: motors, generator set, rheostat, variac, servo). Copy into a scratch server's
// kubejs/server_scripts as zz_pwg_p4_probe.js next to zz_pwg_p3_probe.js (place, w, src, res, scroll, be, recipes) and the
// two P2 probes; never into the pack. Reads server-side values only.
// No chunk loads here: `forceload add` the area first (Rhino deadlock, scratch-test gotchas).
//   check                               registry and glue: stress provider values (capacity, impact, generated RPM) for
//                                       the motors, servo and generator parts, the clutch display source and redstone
//                                       converter entry, the spinning_rotor source (the KineticBlockEntityAccessor
//                                       mixin shows as accessor=true in kin on any kinetic block)
//   kin X Y Z                           kinetic readout of the block at X Y Z: speed, theoretical and generated speed,
//                                       network capacity and stress (SU), overstressed; rotor assembly (RPM, inertia,
//                                       segments, mechanical power, controller); clutch (mode, load, redstone out,
//                                       stress sum); commutator (voltage, current, power); induction rotor field;
//                                       winding current and field; tuned blocks (arm, ratio); terminals; goggle lines
//   wind X1 Y1 Z1 X2 Y2 Z2 SHAFTAXIS    a winding from 1 to 2 as the copper coil item makes it between two shafts of
//                                       axis SHAFTAXIS (x, y or z) at 1 and 2 (the shafts are not needed; the line must
//                                       be straight and not along SHAFTAXIS)
//   clutch X Y Z generator|motor        clutch mode (the scroll option; same as zpwg3 scroll X Y Z 0|1)
//   hp NAME                             health and position of living entities with custom name NAME (all loaded)
// Output: "[zpwg4] ..." lines in logs/latest.log, then "[zpwg4] done".
(() => {
    const $Args = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $FR = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $RL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $Pos = Java.loadClass('net.minecraft.core.BlockPos')
    const $Dir = Java.loadClass('net.minecraft.core.Direction')
    const $Axis = Java.loadClass('net.minecraft.core.Direction$Axis')
    const $AxisDir = Java.loadClass('net.minecraft.core.Direction$AxisDirection')
    const $BEB = Java.loadClass('com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour')
    const $Scroll = Java.loadClass('com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour')
    const $KBE = Java.loadClass('com.simibubi.create.content.kinetics.base.KineticBlockEntity')
    const $GKBE = Java.loadClass('com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity')
    const $Stress = Java.loadClass('com.simibubi.create.content.kinetics.BlockStressValues')
    const $Display = Java.loadClass('com.simibubi.create.content.redstone.displayLink.AllDisplayBehaviours')
    const $Goggles = Java.loadClass('com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation')
    const $Living = Java.loadClass('net.minecraft.world.entity.LivingEntity')
    const $List = Java.loadClass('java.util.ArrayList')
    let PG = null
    function pg() {
        if (PG === null) PG = {
            Electric: Java.loadClass('org.patryk3211.powergrid.electricity.base.ElectricBehaviour'),
            Thermal: Java.loadClass('org.patryk3211.powergrid.electricity.base.ThermalBehaviour'),
            Rotor: Java.loadClass('org.patryk3211.powergrid.kinetics.generator.rotor.RotorBehaviour'),
            Clutch: Java.loadClass('org.patryk3211.powergrid.kinetics.generator.clutch.GeneratorClutchBlockEntity'),
            Commutator: Java.loadClass('org.patryk3211.powergrid.kinetics.generator.inductionrotor.CommutatorBlockEntity'),
            Induction: Java.loadClass('org.patryk3211.powergrid.kinetics.generator.inductionrotor.InductionRotorBlockEntity'),
            Winding: Java.loadClass('org.patryk3211.powergrid.kinetics.generator.winding.WindingBlockEntity'),
            Tuned: Java.loadClass('org.patryk3211.powergrid.kinetics.base.TunedBlockEntity'),
            Rheostat: Java.loadClass('org.patryk3211.powergrid.kinetics.rheostat.RheostatBlockEntity'),
            Variac: Java.loadClass('org.patryk3211.powergrid.kinetics.variac.VariacBlockEntity'),
            Motor: Java.loadClass('org.patryk3211.powergrid.kinetics.motor.ElectricMotorBlockEntity'),
            CsMotor: Java.loadClass('org.patryk3211.powergrid.kinetics.motor.ConstantSpeedMotorBlockEntity'),
            Servo: Java.loadClass('org.patryk3211.powergrid.kinetics.servo.ServoBlockEntity'),
            Accessor: Java.loadClass('org.patryk3211.powergrid.mixin.KineticBlockEntityAccessor'),
            RCReg: Java.loadClass('org.patryk3211.powergrid.electricity.redstoneconverter.RedstoneConverterRegistry'),
            Damage: Java.loadClass('org.patryk3211.powergrid.collections.ModdedDamageTypes'),
        }
        return PG
    }
    function out(s) { console.info('[zpwg4] ' + s) }
    // game time: ServerLevel.getGameTime is not reachable from Rhino here (r1113, as in pwg_p3_probe.js)
    function gtime(level) {
        try { return level.getLevelData().getGameTime() } catch (e1) {}
        try { return level.getServer().getTickCount() } catch (e2) {}
        return 'ms' + Date.now()
    }
    function pos(x, y, z) { return new $Pos(x, y, z) }
    function block(id) { return $FR.BLOCKS.getValue(new $RL(id)) }
    function fmt(x) { return (Math.round(x * 1000) / 1000) }
    function setProp(state, name, value) {
        let p = null
        state.getProperties().forEach(q => { if (q.getName() == name) p = q })
        if (p == null) throw 'no property ' + name
        let v = p.getValue(value)
        if (!v.isPresent()) throw 'bad value ' + name + '=' + value
        return state.setValue(p, v.get())
    }
    function check() {
        for (let id of ['electric_motor', 'constant_speed_motor', 'servo', 'generator_clutch', 'generator_induction_rotor',
            'generator_large_induction_rotor', 'generator_commutator', 'generator_vertical_commutator', 'variac', 'rheostat',
            'hv_switch']) {
            let b = block('powergrid:' + id)
            let rpm = $Stress.getGeneratedRPM(b)
            out('stress ' + id + ' capacity=' + $Stress.getCapacity(b) + ' impact=' + $Stress.getImpact(b)
                + ' hasCapacity=' + $Stress.hasCapacity(b) + ' hasImpact=' + $Stress.hasImpact(b)
                + ' rpm=' + (rpm == null ? 'none' : rpm.getFirst() + '..' + rpm.getSecond()))
        }
        // sourcesOf is overloaded (Block, BlockState, BlockEntity, level + pos): pick the Block one by signature (r1113)
        try {
            let gc = block('powergrid:generator_clutch'), n
            try { n = $Display['sourcesOf(net.minecraft.world.level.block.Block)'](gc).size() }
            catch (e1) { n = $Display['sourcesOf(net.minecraft.world.level.block.state.BlockState)'](gc.defaultBlockState()).size() }
            out('display clutch=' + ($Display.getSource(new $RL('powergrid:clutch')) != null) + ' sources on generator_clutch=' + n)
        } catch (err) { out('display error ' + err) }
        try { out('redstone converter clutch=' + pg().RCReg.REGISTRY.containsKey(block('powergrid:generator_clutch'))) }
        catch (err) { out('redstone converter error ' + err) }
        try {
            let src = pg().Damage.SPINNING_ROTOR.simpleDamageSource()
            // getMsgId / isBypassArmor are not reachable by name from Rhino here (r1113): fall back to the fields
            let mid, bp
            try { mid = src.getMsgId() } catch (e1) { mid = src.msgId }
            try { bp = src.isBypassArmor() } catch (e2) { bp = src.bypassArmor }
            out('damage spinning_rotor msgId=' + mid + ' bypassArmor=' + bp)
        } catch (err) { out('damage error ' + err) }
    }
    function kin(level, p) {
        let be = level.getBlockEntity(p)
        out('kin ' + level.getBlockState(p) + ' t=' + gtime(level))
        if (be == null) { out('kin none'); return }
        if (be instanceof $KBE) {
            let line = 'speed=' + fmt(be.getSpeed()) + ' theoretical=' + fmt(be.getTheoreticalSpeed())
            if (be instanceof $GKBE) line += ' generated=' + fmt(be.getGeneratedSpeed())
            line += ' overstressed=' + be.isOverStressed() + ' impact=' + fmt(be.calculateStressApplied())
                + ' capacityAdded=' + fmt(be.calculateAddedStressCapacity()) + ' accessor=' + (be instanceof pg().Accessor)
            out(line)
            if (be.hasNetwork()) {
                let n = be.getOrCreateNetwork()
                out('network capacitySU=' + fmt(n.calculateCapacity()) + ' stressSU=' + fmt(n.calculateStress())
                    + ' members=' + n.getSize() + ' sources=' + n.sources.size())
            } else out('network none')
        }
        let rb = $BEB.get(be, pg().Rotor.TYPE)
        if (rb != null) {
            out('rotor rpm=' + fmt(rb.getAngularVelocity()) + ' inertia=' + fmt(rb.getInertia()) + ' segments='
                + rb.getSegmentCount() + ' sized=' + rb.getLimitedSize() + ' controller=' + rb.isController()
                + ' power=' + fmt(rb.getControllerOrThis().power) + ' force=' + fmt(rb.getControllerOrThis().prevForce))
        }
        if (be instanceof pg().Clutch) {
            let mode = $BEB.get(be, $Scroll.TYPE)
            out('clutch mode=' + (mode == null ? '?' : (mode.getValue() == 0 ? 'generator' : 'motor')) + ' load='
                + fmt(be.getLoad()) + ' redstoneOut=' + be.getRedstoneOutput() + ' stressSum=' + fmt(be.stressSum()))
        }
        if (be instanceof pg().Commutator) {
            let s = be.getAssemblySource()
            out('commutator V=' + (s == null ? 'none' : fmt(s.getVoltage())) + ' I=' + fmt(be.getCurrent()) + ' P='
                + fmt(be.getPower()))
        }
        if (be instanceof pg().Induction) out('induction field=' + fmt(be.totalField.get()))
        if (be instanceof pg().Winding) {
            let f = be.fieldStrengthCalc()
            out('winding I=' + fmt(be.windingCurrent()) + ' field=' + (f == null ? 'none' : fmt(f.get())))
        }
        if (be instanceof pg().Tuned) {
            let line = 'tuned arm=' + fmt(be.arm.getValue())
            if (be instanceof pg().Rheostat || be instanceof pg().Variac) line += ' ratio=' + fmt(be.getRatio())
            out(line)
        }
        if (be instanceof pg().Motor || be instanceof pg().CsMotor || be instanceof pg().Servo)
            out('motor torque=' + fmt(be.torque()))
        let eb = $BEB.get(be, pg().Electric.TYPE)
        if (eb != null) {
            let vs = []
            for (let i = 0; i < 8; i++) if (eb.hasTerminal(i)) vs.push('T' + i + '=' + fmt(eb.getTerminal(i).getVoltage()))
            out('terminals ' + vs.join(' '))
        }
        let th = $BEB.get(be, pg().Thermal.TYPE)
        if (th != null) out('thermal T=' + fmt(th.getTemperature()) + ' overheated=' + th.isOverheated())
        let sc = $BEB.get(be, $Scroll.TYPE)
        if (sc != null && !(be instanceof pg().Clutch)) out('scroll ' + sc.getValue())
        if (be instanceof $Goggles) {
            // dedicated server: gauges and some kinetic tooltips call Minecraft.getInstance() (RuntimeDistCleaner ERROR)
            let lines = new $List()
            try {
                be.addToGoggleTooltip(lines, true)
                lines.forEach(c => out('goggles ' + String(c.getString()).trim()))
            } catch (err) { out('goggles skipped (client-only tooltip)') }
        }
    }
    function axisOf(n) { return n == 'x' ? $Axis.X : (n == 'y' ? $Axis.Y : $Axis.Z) }
    // WindingItem.useOn between two shafts, without the item and the shafts.
    function wind(level, p1, p2, shaftAxis) {
        let dx = p2.getX() - p1.getX(), dy = p2.getY() - p1.getY(), dz = p2.getZ() - p1.getZ()
        let nz = (dx != 0 ? 1 : 0) + (dy != 0 ? 1 : 0) + (dz != 0 ? 1 : 0)
        if (nz != 1) throw 'not a straight line'
        let place = dx != 0 ? $Axis.X : (dy != 0 ? $Axis.Y : $Axis.Z)
        if (place == shaftAxis) throw 'line is along the shaft axis'
        let along
        if (place.isHorizontal() && shaftAxis.isHorizontal()) along = false
        else if (place.isHorizontal() && shaftAxis.isVertical()) along = true
        else along = shaftAxis == $Axis.Z
        let len = Math.abs(dx + dy + dz)
        let start = (dx + dy + dz) > 0 ? p1 : p2
        let off = $Dir.fromAxisAndDirection(place, $AxisDir.POSITIVE)
        let base = block('powergrid:winding').defaultBlockState()
        base = setProp(base, 'axis', String(place.getName()))
        base = setProp(base, 'axis_along_first', String(along))
        for (let i = 0; i <= len; i++) {
            let part = i == 0 ? '0' : (i == len ? '2' : '1')
            // BlockPos.relative(Direction, int) is ambiguous in Rhino (BlockPos and Vec3i overloads, r1113) and Direction.getStepX is not reachable: step along the axis by hand
            level.setBlockAndUpdate(new $Pos(start.getX() + (place == $Axis.X ? i : 0), start.getY() + (place == $Axis.Y ? i : 0), start.getZ() + (place == $Axis.Z ? i : 0)), setProp(base, 'part', part))
        }
        out('wind ' + (len + 1) + ' blocks axis=' + place.getName() + ' along=' + along + ' magnetic='
            + block('powergrid:winding').getMagneticAxis(base).getName())
    }
    function run(level, a) {
        let cmd = a[0]
        if (cmd == 'check') { check(); return }
        if (cmd == 'hp') {
            let n = 0
            level.getAllEntities().forEach(e => {
                if (!(e instanceof $Living) || !e.hasCustomName()) return
                if (String(e.getCustomName().getString()) != a[1]) return
                n++
                out('hp ' + a[1] + ' health=' + fmt(e.getHealth()) + ' alive=' + e.isAlive() + ' at ' + fmt(e.getX()) + ' '
                    + fmt(e.getY()) + ' ' + fmt(e.getZ()))
            })
            out('hp count ' + n)
            return
        }
        let P = pos(parseInt(a[1]), parseInt(a[2]), parseInt(a[3]))
        if (cmd == 'kin') kin(level, P)
        else if (cmd == 'wind') wind(level, P, pos(parseInt(a[4]), parseInt(a[5]), parseInt(a[6])), axisOf(a[7]))
        else if (cmd == 'clutch') {
            let s = $BEB.get(level.getBlockEntity(P), $Scroll.TYPE)
            if (s == null) throw 'no clutch at ' + P
            s.setValue(a[4] == 'motor' ? 1 : 0)
            out('clutch ' + a[4])
        } else out('unknown subcommand ' + cmd)
    }
    ServerEvents.commandRegistry(e => {
        const C = e.commands
        e.register(C.literal('zpwg4').requires(s => s.hasPermission(2))
            .then(C.argument('args', $Args.greedyString()).executes(ctx => {
                try {
                    run(ctx.source.getLevel(), String($Args.getString(ctx, 'args')).split(' '))
                } catch (err) { out('error ' + err) }
                out('done')
                return 1
            })))
    })
})()
