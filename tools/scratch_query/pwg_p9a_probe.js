// Scratch-only probe for the Create: Power Grid port, phase P9a rows (SQ-PWG-67 onward and SQ-PWG-06,
// plan_power_grid.md P9a: components on boards, the solver's non-linear models). Copy into a scratch server's
// kubejs/server_scripts as zz_pwg_p9a_probe.js next to the P2 to P8 probes (zpwg3: place, w, src, be, recipes); never
// into the pack. Uses one Forge fake player ("[zpwg9]"). No chunk loads here: `forceload add` the area first (Rhino
// deadlock). Boards are built here from component ids and traces (the design table is P9b), so every run has the same
// circuit; terminals are the connectors in the order listed per design (and the thyratron's top-cap anode last).
//   check                         registries and glue: circuit_board block, item and block entity; the P9a items; the
//                                 components registry (powergrid:components, ids); the component_items table read by
//                                 the reload listener (entry count, id -> item [tag]) and the item -> component lookup
//                                 for every entry; tag powergrid:circuit_component; the two P9a packets (ordinals)
//   board X Y Z NAME [p=v ...]    circuit board (facing north, flat) at X Y Z with design NAME; p=v sets a property of
//                                 the design's main part (property path in namespace powergrid, e.g. resistor_value=10)
//                                 Designs (terminals):
//                                   resistor   T0 - resistor (100 ohm) - T1
//                                   diode      T0 anode, T1 cathode (diode facing LEFT)
//                                   npn        T0 collector, T1 base, T2 emitter
//                                   pnp        T0 emitter, T1 base, T2 collector
//                                   triode     T0 anode, T1 grid, T2 cathode, T3 / T4 heater
//                                   pentode    as triode, T5 screen
//                                   thyratron  T0 grid, T1 cathode, T2 / T3 heater, T4 anode (top cap)
//                                   regulator  T0 anode, T1 cathode (regulator tube, holding 60 V)
//                                   relay_rs   T0 - redstone relay (input on the board's south side) - T1
//                                   switch     T0 - switch - T1
//                                   capacitor  T0 - capacitor (100 uF) - T1
//   ponder X Y Z NAME             upstream's own board from its ponder structure: resistor (legacy full-pixel traces)
//                                 or thyratron (version 2 traces); same placement as board
//   bmap X Y Z                    the board's node bundles: per bundle its pads (component@x,y#pad, * = external), and
//                                 per terminal its voltage
//   bcomp X Y Z                   per component: id, position, properties, destroyed flag, wires (class, current,
//                                 voltage), and the board's thermal units (temperature) and cooling multiplier
//   bset X Y Z CX CY PROP VALUE   property PROP of the component at CX CY set from text, then the board rebakes
//   buse X Y Z CX CY              the interactable component at CX CY used by the fake player (as a click on it)
//   net X Y Z                     the board's network: converged, node count, paused
//   watch X Y Z N                 samples the board's terminals and its network every tick for N ticks, then prints
//                                 the first tick the network reported converged and the tick the terminals settled
//                                 (within 1 mV for 20 ticks); one watch at a time
//   repair X Y Z                  CircuitBoardBlockEntity.repairBroken (the creative sneak-use action)
//   pick X Y Z                    the pick-block stack of the board: item, component count in its Schematic tag; it
//                                 goes into the fake player's main hand
//   fp X Y Z / mode creative|survival / sneak on|off / hold ITEM
//   use X Y Z FACE DX DY DZ       the fake player uses the held item on face FACE of the block at X Y Z (hit at +DX DY DZ)
//   drops X Y Z R                 item entities within R: id, count, Schematic tag (component count) and Thermal tag
// Output: "[zpwg9] ..." lines in logs/latest.log, then "[zpwg9] done".
(() => {
    const $Args = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $FR = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $RL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $Pos = Java.loadClass('net.minecraft.core.BlockPos')
    const $Dir = Java.loadClass('net.minecraft.core.Direction')
    const $AABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $Vec3 = Java.loadClass('net.minecraft.world.phys.Vec3')
    const $BHR = Java.loadClass('net.minecraft.world.phys.BlockHitResult')
    const $Hand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $ItemEntity = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $Anchor = Java.loadClass('net.minecraft.commands.arguments.EntityAnchorArgument$Anchor')
    const $GameType = Java.loadClass('net.minecraft.world.level.GameType')
    const $Pose = Java.loadClass('net.minecraft.world.entity.Pose')
    const $Fake = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $Profile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $UUID = Java.loadClass('java.util.UUID')
    const $TagParser = Java.loadClass('net.minecraft.nbt.TagParser')
    const $Compound = Java.loadClass('net.minecraft.nbt.CompoundTag')
    const $TagKey = Java.loadClass('net.minecraft.tags.TagKey')
    const $Registry = Java.loadClass('net.minecraft.core.Registry')
    const $BEB = Java.loadClass('com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour')
    const PROFILE = new $Profile($UUID.fromString('5a7c0e57-0b0e-4a11-9d51-70e9a1c0f009'), '[zpwg9]')
    const PONDER = {"resistor": "{Components:[{X:1,Y:6,Id:\"powergrid:connector\",UUID:[I;1041321876,1855603957,-1706645182,-1255366667]},{X:12,Y:6,Id:\"powergrid:connector\",UUID:[I;-208072676,555500087,-1632757891,1867284548]},{X:6,Y:6,Id:\"powergrid:resistor\",UUID:[I;-445258675,-1868019786,-1531555441,2096787229],Properties:{\"powergrid:resistor_value\":100.0f,\"powergrid:orientation\":2}}],Front:[L;0L,4358358539387797504L],Back:[L;],Name:\"Circuit\"}", "thyratron": "{Components:[{X:10,Y:0,Id:\"powergrid:voltage_gauge\",UUID:[I;-993432274,977685389,-1364020577,-1721200711],Properties:{\"powergrid:voltage_gauge_max\":50.0f,\"powergrid:redstone_level\":0,\"powergrid:orientation\":1}},{X:4,Y:0,Id:\"powergrid:voltage_gauge\",UUID:[I;-1582006569,-907327426,-1629105151,-766590212],Properties:{\"powergrid:voltage_gauge_max\":50.0f,\"powergrid:redstone_level\":0,\"powergrid:orientation\":1}},{X:1,Y:5,Id:\"powergrid:resistor\",UUID:[I;-1058938929,-901492825,-1343248645,-480942236],Properties:{\"powergrid:resistor_value\":500.0f,\"powergrid:orientation\":2}},{X:0,Y:1,Id:\"powergrid:connector\",UUID:[I;-1787015956,2028423222,-1678277314,-419922120]},{X:11,Y:5,Id:\"powergrid:connector\",UUID:[I;1329112828,1576355308,-1700530056,-1598123024]},{X:5,Y:13,Id:\"powergrid:connector\",UUID:[I;720825111,-542486830,-1474702153,973283427]},{X:9,Y:13,Id:\"powergrid:connector\",UUID:[I;-1616207675,-1094103641,-1323152173,1674221088]},{X:7,Y:9,Id:\"powergrid:thyratron\",UUID:[I;-33299878,2103332163,-1821905081,307362478],Properties:{\"powergrid:thyratron_mu\":20.0f,\"powergrid:thyratron_vb\":60.0f,\"powergrid:tube_heater_voltage\":6.0f,\"powergrid:orientation\":0}}],Front:[L;4909562625928494848L,4909024896075580450L,9133370414240646176L,4672995722304L,0L,0L,562949953421312L,844424930131968L,844424930131968L,1632542839013376L,4294967296L,7148067094528L,3298585214976L,3298585214976L,1099528404992L],Back:[L;0L,0L,216173881625411584L],Name:\"Thyratron\",Version:2,FullPixelTraces:0b}"}
    let PG = null
    function pg() {
        if (PG === null) PG = {
            Electric: Java.loadClass('org.patryk3211.powergrid.electricity.base.ElectricBehaviour'),
            Board: Java.loadClass('org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity'),
            Schematic: Java.loadClass('org.patryk3211.powergrid.circuits.schematic.CircuitSchematic'),
            Placed: Java.loadClass('org.patryk3211.powergrid.circuits.schematic.PlacedComponent'),
            Registry: Java.loadClass('org.patryk3211.powergrid.circuits.components.ComponentRegistry'),
            RegistryImpl: Java.loadClass('org.patryk3211.powergrid.circuits.components.forge.ComponentRegistryImpl'),
            Interactable: Java.loadClass('org.patryk3211.powergrid.circuits.components.IInteractableComponent'),
            Packets: Java.loadClass('org.patryk3211.powergrid.collections.ModdedPackets'),
            Blocks: Java.loadClass('org.patryk3211.powergrid.collections.ModdedBlocks'),
        }
        return PG
    }
    function out(s) { console.info('[zpwg9] ' + s) }
    function pos(x, y, z) { return new $Pos(x, y, z) }
    function rl(s) { return s.indexOf(':') >= 0 ? new $RL(s) : new $RL('powergrid', s) }
    function fmt(x) { return (Math.round(x * 1e4) / 1e4) }
    function simpleName(o) { let s = String(o); let m = s.match(/([A-Za-z0-9_$]+)@[0-9a-f]+$/); return m ? m[1].replace(/^.*\$/, '') : s }
    function idOf(stack) { return String($FR.ITEMS.getKey(stack.getItem())) }
    function item(id) { return $FR.ITEMS.getValue(rl(id)) }
    function fake(level) { return $Fake.get(level, PROFILE) }
    function stackTag(stack) {
        let t = stack.save(new $Compound())
        return t.contains('tag') ? t.getCompound('tag') : new $Compound()
    }

    // ---- designs: [component id, x, y, {property: value}] and traces [h|v, row/column, from, to] on the front layer.
    // Connector (3x3) pad at +1,+1. Footprints as Components (orientation RIGHT unless set).
    const LEFT_C = ['connector', 0, 6], RIGHT_C = ['connector', 11, 6]
    const TWO_PAD_3 = [['h', 7, 1, 6], ['h', 7, 8, 12]]           // 3-wide part at 6,6: pads 6,7 and 8,7
    const TWO_PAD_5 = [['h', 7, 1, 5], ['h', 7, 9, 12]]           // 5-wide part at 5,6: pads 5,7 and 9,7
    const TUBE = [['h', 5, 1, 6], ['v', 6, 5, 6], ['h', 5, 8, 12], ['v', 8, 5, 6], ['v', 7, 7, 12], ['h', 8, 1, 6], ['h', 8, 8, 12]]
    const DESIGNS = {
        resistor: { parts: [LEFT_C, RIGHT_C, ['resistor', 5, 6, {}]], traces: TWO_PAD_5 },
        diode: { parts: [LEFT_C, RIGHT_C, ['diode', 5, 6, { orientation: 'LEFT' }]], traces: TWO_PAD_5 },
        relay_rs: { parts: [LEFT_C, RIGHT_C, ['redstone_relay', 5, 6, { orientation: 'DOWN' }]], traces: TWO_PAD_5 },
        switch: { parts: [LEFT_C, RIGHT_C, ['switch', 5, 6, {}]], traces: [['h', 7, 1, 5], ['h', 7, 8, 12]] },
        capacitor: { parts: [LEFT_C, RIGHT_C, ['capacitor', 6, 6, {}]], traces: TWO_PAD_3 },
        regulator: { parts: [LEFT_C, RIGHT_C, ['regulator_tube', 6, 6, {}]], traces: TWO_PAD_3 },
        // BJT at 6,6 (3x2): pads 6,6 / 8,6 / 7,7; connectors left (0,4), bottom (6,11), right (11,4)
        npn: { parts: [['connector', 0, 4], ['connector', 6, 11], ['connector', 11, 4], ['bjt_npn', 6, 6, {}]], traces: TUBE.slice(0, 5) },
        pnp: { parts: [['connector', 0, 4], ['connector', 6, 11], ['connector', 11, 4], ['bjt_pnp', 6, 6, {}]], traces: TUBE.slice(0, 5) },
        // tube at 6,6 (3x3): anode 6,6, grid 8,6, cathode 7,7, heaters 6,8 and 8,8
        triode: { parts: [['connector', 0, 4], ['connector', 11, 4], ['connector', 6, 11], ['connector', 0, 7], ['connector', 11, 7], ['triode', 6, 6, {}]], traces: TUBE },
        // pentode: screen pad 7,6 goes up to a connector at 6,0
        pentode: { parts: [['connector', 0, 4], ['connector', 11, 4], ['connector', 6, 11], ['connector', 0, 7], ['connector', 11, 7], ['connector', 6, 0], ['pentode', 6, 6, {}]], traces: TUBE.concat([['v', 7, 1, 6]]) },
        // thyratron at 6,6: grid 7,6 up to 6,0; cathode 7,7 down; heaters as the triode; anode is the tube's top cap
        thyratron: { parts: [['connector', 6, 0], ['connector', 6, 11], ['connector', 0, 7], ['connector', 11, 7], ['thyratron', 6, 6, {}]], traces: [['v', 7, 1, 6], ['v', 7, 7, 12], ['h', 8, 1, 6], ['h', 8, 8, 12]] },
    }
    // PlacedComponent.getEntry is overloaded (ResourceLocation / ComponentProperty): call it by signature
    function entry(placed, id) { return placed['getEntry(net.minecraft.resources.ResourceLocation)'](id) }
    function placedOf(id, props) {
        let c = pg().Registry.get(rl(id))
        let p = new (pg().Placed)(c, 0, 0, null)
        Object.keys(props || {}).forEach(k => {
            let e = entry(p, rl(k))
            if (e == null) throw 'no property ' + k + ' on ' + id
            e.setValue(String(props[k]))
        })
        return p
    }
    function design(name, extra) {
        let d = DESIGNS[name]
        if (!d) throw 'unknown design ' + name
        let s = new (pg().Schematic)()
        d.parts.forEach((part, i) => {
            let props = part.length > 3 ? Object.assign({}, part[3]) : {}
            if (i == d.parts.length - 1) Object.assign(props, extra)
            s.placeComponent(placedOf(part[0], props), part[1], part[2])
        })
        d.traces.forEach(t => {
            if (t[0] == 'h') s.front().addHorizontalLine(t[1], t[2], t[3])
            else s.front().addVerticalLine(t[1], t[2], t[3])
        })
        s.rebuildPads()
        s.setName('zpwg9 ' + name)
        return s
    }
    function boardAt(level, P) {
        let be = level.getBlockEntity(P)
        if (!(be instanceof pg().Board)) throw 'no circuit board at ' + P
        return be
    }
    function placeBoard(level, P, schematic) {
        let st = pg().Blocks.CIRCUIT_BOARD.get().defaultBlockState()
        level.setBlock(P, st, 3)
        let be = boardAt(level, P)
        be.setSchematic(schematic)
        out('board at ' + P.getX() + ' ' + P.getY() + ' ' + P.getZ() + ' state=' + level.getBlockState(P) + ' components='
            + be.getSchematic().components().size() + ' terminals=' + be.terminalCount())
        return be
    }
    function terminals(be) {
        let eb = $BEB.get(be, pg().Electric.TYPE)
        if (eb == null) return 'no electric behaviour'
        let vs = []
        for (let i = 0; i < be.terminalCount(); i++) if (eb.hasTerminal(i)) vs.push('T' + i + '=' + fmt(eb.getTerminal(i).getVoltage()))
        return vs.join(' ')
    }
    function partName(placed) { return String(pg().Registry.getId(placed.component).getPath()) + '@' + placed.x + ',' + placed.y }
    function bmap(level, P) {
        let be = boardAt(level, P)
        let s = be.getSchematic()
        let i = 0
        s.findNodeBundles().forEach(b => {
            let names = []
            b.forEach(n => names.push(partName(n.placed()) + '#' + n.pad() + (n.placed().component.isExternalNode(n.pad()) ? '*' : '')))
            names.sort()
            out('bundle ' + (i++) + ': ' + names.join(' '))
        })
        out('terminals ' + terminals(be))
    }
    function wireInfo(w) {
        let s = simpleName(w)
        try { s += ' I=' + fmt(w.current()) } catch (e1) {}
        try { s += ' V=' + fmt(w.potentialDifference()) } catch (e2) {}
        return s
    }
    function bcomp(level, P) {
        let be = boardAt(level, P)
        be.getSchematic().components().forEach(p => {
            let props = []
            p.component.getProperties().forEach(prop => {
                let id = prop.id()
                let e = entry(p, id)
                if (e != null) props.push(String(id.getPath()) + '=' + e.stringValue())
            })
            let ws = []
            p.wires.forEach(w => ws.push(wireInfo(w)))
            out('part ' + partName(p) + ' destroyed=' + p.destroyed + ' props[' + props.join(' ') + '] wires[' + ws.join(' | ') + ']')
        })
        let tag = be.saveWithoutMetadata()
        let th = tag.getCompound('Thermal')
        let ts = []
        th.getAllKeys().forEach(k => ts.push(String(k).substring(0, 8) + '=' + fmt(th.getFloat(k))))
        out('thermal ' + ts.join(' ') + ' cooling=' + fmt(be.totalCoolingFactorMultiplier))
        out('terminals ' + terminals(be))
    }
    function componentAt(be, cx, cy) {
        let p = be.getSchematic().getComponent(cx, cy)
        if (p == null) throw 'no component at ' + cx + ',' + cy
        return p
    }
    function networkOf(be) {
        let eb = $BEB.get(be, pg().Electric.TYPE)
        let nodes = eb.getExternalNodes()
        if (!nodes.isEmpty() && nodes.get(0).getNetwork() != null) return [eb, nodes.get(0).getNetwork()]
        let inner = eb.getInternalNodes()
        let net = null
        inner.forEach(n => { if (net == null && n.getNetwork) net = n.getNetwork() })
        return [eb, net]
    }
    function net(level, P) {
        let r = networkOf(boardAt(level, P))
        out('net paused=' + r[0].isPaused() + ' external=' + r[0].getExternalNodes().size() + ' internal=' + r[0].getInternalNodes().size()
            + (r[1] == null ? ' network=none' : ' converged=' + r[1].isConverged() + ' networkNodes=' + r[1].getNodes().size()))
    }
    let WATCH = null
    ServerEvents.tick(e => {
        if (WATCH == null || WATCH.left <= 0) return
        try {
            WATCH.left--
            WATCH.tick++
            let be = WATCH.level.getBlockEntity(WATCH.pos)
            if (!(be instanceof pg().Board)) { out('watch: board gone'); WATCH.left = 0; return }
            let r = networkOf(be)
            let conv = r[1] != null && r[1].isConverged()
            if (conv && WATCH.firstConv < 0) WATCH.firstConv = WATCH.tick
            let eb = r[0]
            let vs = []
            for (let i = 0; i < be.terminalCount(); i++) vs.push(eb.hasTerminal(i) ? eb.getTerminal(i).getVoltage() : 0)
            let same = WATCH.last != null && vs.length == WATCH.last.length && vs.every((v, i) => Math.abs(v - WATCH.last[i]) <= 1e-3)
            WATCH.stable = same && conv ? WATCH.stable + 1 : 0
            if (WATCH.stable == 1) WATCH.stableFrom = WATCH.tick - 1
            if (WATCH.stable >= 20 && WATCH.settled < 0) WATCH.settled = WATCH.stableFrom
            WATCH.last = vs
            if (WATCH.left == 0) out('watch end ticks=' + WATCH.tick + ' firstConverged=' + WATCH.firstConv + ' settledAt='
                + WATCH.settled + ' terminals ' + vs.map((v, i) => 'T' + i + '=' + fmt(v)).join(' '))
        } catch (err) { out('watch error ' + err); WATCH.left = 0 }
    })
    function check(level) {
        out('block powergrid:circuit_board registered=' + $FR.BLOCKS.containsKey(rl('circuit_board')) + ' item=' + $FR.ITEMS.containsKey(rl('circuit_board'))
            + ' blockEntity=' + $FR.BLOCK_ENTITY_TYPES.containsKey(rl('circuit_board')))
        let ids = ['pins', 'relay', 'relay_dpdt', 'resistor', 'redstone_relay', 'diode', 'vfet', 'bjt_npn', 'bjt_pnp', 'capacitor', 'potentiometer',
            'triode', 'pentode', 'thyratron', 'regulator_tube', 'barretter_tube', 'varistor', 'incomplete_bjt_npn', 'incomplete_bjt_pnp']
        let missing = ids.filter(i => !$FR.ITEMS.containsKey(rl(i)))
        out('items p9a=' + (ids.length - missing.length) + '/' + ids.length + (missing.length ? ' missing=' + missing.join(',') : ''))
        let later = ['circuit_design_table', 'circuit_schematic', 'acid_bucket', 'empty_circuit', 'unetched_circuit', 'incomplete_circuit',
            'incomplete_unetched_circuit', 'integrated_circuit', 'incomplete_electrical_gizmo']
        out('items of P9b to P9d registered=' + later.filter(i => $FR.ITEMS.containsKey(rl(i))).join(',') + ' (expect none)')
        let reg = pg().RegistryImpl.REGISTRY
        let keys = []
        reg.getKeys().forEach(k => keys.push(String(k.getPath())))
        keys.sort()
        out('components ' + keys.length + ': ' + keys.join(' '))
        let table = pg().Registry.serverItems()
        let rows = [], bad = []
        table.forEach((id, ent) => {
            let it = String($FR.ITEMS.getKey(ent.item()))
            let tag = ent.tag().isPresent() ? ' [#' + ent.tag().get().location() + ']' : ''
            rows.push(String(id.getPath()) + '->' + it + tag)
            let back = pg().Registry.getComponentId(level, ent.item().getDefaultInstance())
            if (back == null || String(back) != String(id)) bad.push(String(id.getPath()) + '(lookup ' + back + ')')
        })
        rows.sort()
        out('component_items ' + rows.length + ': ' + rows.join(' '))
        out('component_items item lookup mismatches=' + bad.length + (bad.length ? ' ' + bad.join(' ') : ''))
        let nug = pg().Registry.getComponentId(level, item('create:copper_nugget').getDefaultInstance())
        out('copper nugget -> ' + nug)
        let tk = $TagKey.create($Registry.ITEM_REGISTRY, rl('circuit_component'))
        let n = 0
        let members = []
        $FR.ITEMS.tags().getTag(tk).forEach(i => { n++; members.push(String($FR.ITEMS.getKey(i))) })
        out('tag powergrid:circuit_component size=' + n)
        // r1113 pwg12 SQ-PWG-67: size 27 against the jar's 28 entries; name what is missing
        let want = ['potentiometer', 'pentode', 'current_gauge', 'barretter_tube', 'copper_coil', 'display_module', 'bjt_npn', 'bjt_pnp',
            'triode', 'lv_switch', 'lv_button', 'voltage_gauge', 'relay_dpdt', 'lv_light_bulb', 'resistor', 'neon_bulb', 'relay', 'vfet',
            'varistor', 'capacitor', 'fuse_holder', 'wire_connector', 'redstone_relay', 'thyratron', 'regulator_tube', 'diode']
            .map(x => 'powergrid:' + x).concat(['create:copper_nugget', 'minecraft:paper'])
        let tagMissing = want.filter(x => members.indexOf(x) < 0)
        let tagExtra = members.filter(x => want.indexOf(x) < 0)
        out('tag powergrid:circuit_component missing=[' + tagMissing.join(' ') + '] extra=[' + tagExtra.join(' ') + ']')
        let pk = pg().Packets.values()
        let names = []
        for (let i = 0; i < pk.length; i++) names.push(pk[i].ordinal() + ':' + pk[i].name())
        out('packets ' + names.slice(-4).join(' '))
    }
    function fp(level, a) {
        let p = fake(level)
        p.setGameMode($GameType.SURVIVAL)
        // moveTo(5 numbers) resolves to the 3-argument teleportTo in Rhino and throws (r1113, pwg_p7_probe.js): absMoveTo
        let mx = parseFloat(a[1]) + 0.5, my = parseFloat(a[2]), mz = parseFloat(a[3]) + 0.5
        try { p.absMoveTo(mx, my, mz, 0, 0) } catch (e1) { p.teleportTo(mx, my, mz) }
        p.setOnGround(true)
        p.setShiftKeyDown(false)
        p.setPose($Pose.STANDING)
        p.getInventory().clearContent()
        out('fp at ' + fmt(p.getX()) + ' ' + fmt(p.getY()) + ' ' + fmt(p.getZ()))
    }
    function use(level, P, face, dx, dy, dz) {
        let p = fake(level)
        let held = p.getMainHandItem()
        let before = idOf(held) + 'x' + held.getCount()
        let hitVec = new $Vec3(P.getX() + dx, P.getY() + dy, P.getZ() + dz)
        p.lookAt($Anchor.EYES, hitVec)
        let hit = new $BHR(hitVec, $Dir.byName(face), P, false)
        let res = p.gameMode.useItemOn(p, level, held, $Hand.MAIN_HAND, hit)
        let now = p.getMainHandItem()
        out('use ' + before + ' on ' + level.getBlockState(P) + ' face=' + face + ' result=' + res + ' held after=' + idOf(now) + 'x' + now.getCount())
    }
    function drops(level, P, r) {
        let n = 0
        level.getEntitiesOfClass($ItemEntity, new $AABB(P.getX() - r, P.getY() - r, P.getZ() - r, P.getX() + r + 1, P.getY() + r + 1, P.getZ() + r + 1)).forEach(e => {
            let s = e.getItem()
            let t = stackTag(s)
            let sch = t.contains('Schematic') ? ' Schematic components=' + t.getCompound('Schematic').getList('Components', 10).size() : ''
            out('drop ' + idOf(s) + ' x' + s.getCount() + sch + (t.contains('Thermal') ? ' Thermal' : ''))
            n++
        })
        out('drops ' + n)
    }
    function run(level, a) {
        let cmd = a[0]
        if (cmd == 'check') { check(level); return }
        if (cmd == 'fp') { fp(level, a); return }
        if (cmd == 'mode') { fake(level).setGameMode(a[1] == 'creative' ? $GameType.CREATIVE : $GameType.SURVIVAL); out('mode ' + a[1]); return }
        if (cmd == 'sneak') { let f = fake(level); f.setShiftKeyDown(a[1] == 'on'); f.setPose(a[1] == 'on' ? $Pose.CROUCHING : $Pose.STANDING); out('sneak ' + f.isShiftKeyDown()); return }
        if (cmd == 'hold') { fake(level).setItemInHand($Hand.MAIN_HAND, item(a[1]).getDefaultInstance()); out('hold ' + idOf(fake(level).getMainHandItem())); return }
        let P = pos(parseInt(a[1]), parseInt(a[2]), parseInt(a[3]))
        if (cmd == 'board') {
            let extra = {}
            a.slice(5).forEach(kv => { let q = kv.split('='); extra[q[0]] = q[1] })
            let be = placeBoard(level, P, design(a[4], extra))
            bmap(level, P)
        } else if (cmd == 'ponder') {
            if (!PONDER[a[4]]) throw 'no ponder board ' + a[4]
            let s = pg().Schematic.fromNbt($TagParser.parseTag(PONDER[a[4]]))
            placeBoard(level, P, s)
            bmap(level, P)
        } else if (cmd == 'bmap') bmap(level, P)
        else if (cmd == 'bcomp') bcomp(level, P)
        else if (cmd == 'bset') {
            let be = boardAt(level, P)
            let p = componentAt(be, parseInt(a[4]), parseInt(a[5]))
            let e = entry(p, rl(a[6]))
            if (e == null) throw 'no property ' + a[6] + ' on ' + partName(p)
            e.setValue(a[7])
            be.setSchematic(be.getSchematic())
            out('bset ' + partName(p) + ' ' + a[6] + '=' + entry(boardAt(level, P).getSchematic().getComponent(parseInt(a[4]), parseInt(a[5])), rl(a[6])).stringValue())
        } else if (cmd == 'buse') {
            let be = boardAt(level, P)
            let p = componentAt(be, parseInt(a[4]), parseInt(a[5]))
            if (!(p.component instanceof pg().Interactable)) throw partName(p) + ' is not interactable'
            let res = p.component.use(be, p, fake(level))
            out('buse ' + partName(p) + ' result=' + res)
        } else if (cmd == 'net') net(level, P)
        else if (cmd == 'watch') {
            boardAt(level, P)
            WATCH = { level: level, pos: P, left: parseInt(a[4]), tick: 0, firstConv: -1, stable: 0, stableFrom: -1, settled: -1, last: null }
            out('watch started for ' + a[4] + ' ticks')
        } else if (cmd == 'repair') { boardAt(level, P).repairBroken(); out('repair done') }
        else if (cmd == 'pick') {
            let st = level.getBlockState(P)
            let s = st.getBlock().getCloneItemStack(level, P, st)
            let t = stackTag(s)
            out('pick ' + idOf(s) + ' Schematic=' + t.contains('Schematic') + ' components=' + (t.contains('Schematic') ? t.getCompound('Schematic').getList('Components', 10).size() : 0))
            fake(level).setItemInHand($Hand.MAIN_HAND, s)
        } else if (cmd == 'use') use(level, P, a[4], parseFloat(a[5]), parseFloat(a[6]), parseFloat(a[7]))
        else if (cmd == 'drops') drops(level, P, parseInt(a[4]))
        else out('unknown subcommand ' + cmd)
    }
    ServerEvents.commandRegistry(e => {
        const C = e.commands
        e.register(C.literal('zpwg9').requires(s => s.hasPermission(2))
            .then(C.argument('args', $Args.greedyString()).executes(ctx => {
                try {
                    run(ctx.source.getLevel(), String($Args.getString(ctx, 'args')).split(' '))
                } catch (err) { out('error ' + err) }
                out('done')
                return 1
            })))
    })
})()
