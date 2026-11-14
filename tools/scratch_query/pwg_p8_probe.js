// Scratch-only probe for the Create: Power Grid port, phase P8 rows (SQ-PWG-62 onward, plan_power_grid.md P8: display
// and control). Copy into a scratch server's kubejs/server_scripts as zz_pwg_p8_probe.js next to the P2 to P7 probes
// (zpwg3: place, w, src, be, scroll; zpwg4: kinetics); never into the pack. Uses one Forge fake player ("[zpwg8]").
// No chunk loads here: `forceload add` the area first (Rhino deadlock). Values that only exist on the client (CRT trace,
// bell sound, plotter pen, value boards) are client rows; this probe reads the server side.
//   check                         registries and glue: P8 blocks, items, block entities, the punch card menu; the CRT's
//                                 encased variants
//                                 (Create EncasingRegistry); CC: Tweaked loaded; the display module type option keys
//   fp X Y Z                      put the fake player at X Y Z (survival, empty hands, not sneaking)
//   hold ITEM [N]                 main hand = N of ITEM (fresh stack)
//   holdcard HEX32 [NAME]         main hand = punch card with Data = the 16 bytes of HEX32 (and a custom name)
//   sneak on|off                  fake player sneaking (shift key and crouching pose)
//   use X Y Z FACE DX DY DZ       the fake player looks at (X+DX, Y+DY, Z+DZ) on face FACE of the block at X Y Z and
//                                 uses the held item there (ServerPlayerGameMode.useItemOn: fires the Forge right-click
//                                 event, so Create's value box handler and ForgeEvents.recordInteractingItem run as for a
//                                 player); result, block after, held item after
//   plot X Y Z [N]                plotter: speed, speed requirement, voltage range index, sample buffer size, head,
//                                 colour, the N samples before the head (NBT, as saved), terminal voltages
//   md X Y Z                      modular display: per slot the module (serialized: type:index:halfClick:colour), the
//                                 blanking page flag, the slot temperature; the module type option value; terminals
//   mdtype X Y Z SLOT TYPE        module type option (0 0-9, 1 9-0, 2 1-0, 3 hex, 4 symbols, 5 alphabet) on SLOT, the
//                                 way the value board sets it (lastHitSlot, then the scroll value and its callback)
//   reader X Y Z                  punch card reader: block entity class (forge substitute), card, Angle, redstone output, comparator, speed, terminals T0..T8
//   card X Y Z HEX32 [up|side]    insertCard of a card with Data HEX32 from the top (default) or the facing side
//   extract X Y Z                 extractCard: the item returned
//   bell X Y Z                    alarm bell: volume and pitch (server side, from the coil current), terminals
//   crt X Y Z                     CRT (plain or encased): block, Color (NBT), terminals T0..T6
//   periph X Y Z                  the CC: Tweaked peripheral CCBridge builds for the block entity at X Y Z: type and the
//                                 value of each getter, called as a computer would
//   conv X Y Z V|clear            RedstoneConverterPeripheral.setValue(V) or clearValue, then the converter's override
//   dlink LX LY LZ TX TY TZ MODE  display link at L (attached to the gauge it faces away from): target TX TY TZ, source the
//                                 gauge's Power Grid display source, Mode (0 bar, 1 absolute, 2 polarized, 3 custom);
//                                 gathers and sends once, then prints the link and the board text (`board`)
//   board X Y Z                   Create display board at X Y Z: the text of each line (from its saved data)
//   drops X Y Z R                 item entities within R: id, count, punch card Data
//   nbtall X Y Z                  the five P8 ponder structures (bell, crt, modular_display, plotter, punch_cards), loaded
//                                 as pwgponder:<name> from the scratch datapack of SQ-PWG-46 (built from the -10 jar) and
//                                 placed 12 blocks apart east of X; each block compared with the file (EXPECTED, generated
//                                 from the jar's files as in zpwg5): name, stored properties, block entity; then a summary
// Output: "[zpwg8] ..." lines in logs/latest.log, then "[zpwg8] done".
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
    const $ItemStack = Java.loadClass('net.minecraft.world.item.ItemStack')
    const $ItemEntity = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $Anchor = Java.loadClass('net.minecraft.commands.arguments.EntityAnchorArgument$Anchor')
    const $GameType = Java.loadClass('net.minecraft.world.level.GameType')
    const $Pose = Java.loadClass('net.minecraft.world.entity.Pose')
    const $Fake = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $Profile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $UUID = Java.loadClass('java.util.UUID')
    // ModList is blocked by the KubeJS class filter (r1113): Platform.isLoaded instead
    const $TagParser = Java.loadClass('net.minecraft.nbt.TagParser')
    const $Compound = Java.loadClass('net.minecraft.nbt.CompoundTag')
    const $BEB = Java.loadClass('com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour')
    const $Scroll = Java.loadClass('com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour')
    const $Encasing = Java.loadClass('com.simibubi.create.content.decoration.encasing.EncasingRegistry')
    const $Kinetic = Java.loadClass('com.simibubi.create.content.kinetics.base.KineticBlockEntity')
    const $SPS = Java.loadClass('net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings')
    const $Entity = Java.loadClass('net.minecraft.world.entity.Entity')
    const EXPECTED = {"bell":{"size":[5,3,5],"p":[["minecraft:white_concrete",{}],["minecraft:snow_block",{}],["minecraft:stripped_spruce_log",{"axis":"y"}],["powergrid:alarm_bell",{"facing":"south"}]],"b":[[0,0,0,0,0],[0,0,1,1,0],[0,0,2,0,0],[0,0,3,1,0],[0,0,4,0,0],[1,0,0,1,0],[1,0,1,0,0],[1,0,2,1,0],[1,0,3,0,0],[1,0,4,1,0],[2,0,0,0,0],[2,0,1,1,0],[2,0,2,0,0],[2,0,3,1,0],[2,0,4,0,0],[3,0,0,1,0],[3,0,1,0,0],[3,0,2,1,0],[3,0,3,0,0],[3,0,4,1,0],[4,0,0,0,0],[4,0,1,1,0],[4,0,2,0,0],[4,0,3,1,0],[4,0,4,0,0],[2,1,3,2,0],[2,2,3,2,0],[2,2,2,3,1]],"e":0},"crt":{"size":[3,2,3],"p":[["minecraft:white_concrete",{}],["minecraft:snow_block",{}],["powergrid:crt",{"facing":"north"}]],"b":[[0,0,0,0,0],[0,0,1,1,0],[0,0,2,0,0],[1,0,0,1,0],[1,0,1,0,0],[1,0,2,1,0],[2,0,0,0,0],[2,0,1,1,0],[2,0,2,0,0],[1,1,1,2,1]],"e":0},"modular_display":{"size":[5,2,5],"p":[["minecraft:white_concrete",{}],["minecraft:snow_block",{}],["powergrid:wire_connector",{"facing":"down"}],["powergrid:modular_display",{"facing":"north"}]],"b":[[0,0,0,0,0],[0,0,1,1,0],[0,0,2,0,0],[0,0,3,1,0],[0,0,4,0,0],[1,0,0,1,0],[1,0,1,0,0],[1,0,2,1,0],[1,0,3,0,0],[1,0,4,1,0],[2,0,0,0,0],[2,0,1,1,0],[2,0,2,0,0],[2,0,3,1,0],[2,0,4,0,0],[3,0,0,1,0],[3,0,1,0,0],[3,0,2,1,0],[3,0,3,0,0],[3,0,4,1,0],[4,0,0,0,0],[4,0,1,1,0],[4,0,2,0,0],[4,0,3,1,0],[4,0,4,0,0],[0,1,3,2,1],[1,1,4,2,1],[2,1,2,3,1],[3,1,4,2,1]],"e":0},"plotter":{"size":[7,3,5],"p":[["minecraft:white_concrete",{}],["minecraft:snow_block",{}],["minecraft:stripped_spruce_log",{"axis":"y"}],["create:metal_girder",{"waterlogged":"false","top":"true","bottom":"true","x":"false","z":"false","axis":"y"}],["create:large_cogwheel",{"waterlogged":"false","axis":"x"}],["create:cogwheel",{"waterlogged":"false","axis":"x"}],["create:shaft",{"waterlogged":"false","axis":"x"}],["powergrid:plotter",{"facing":"north"}],["powergrid:wire_connector",{"facing":"down"}],["powergrid:light_fixture",{"facing":"up","power":"0","axis_along_first":"true"}]],"b":[[1,0,0,0,0],[1,0,1,1,0],[1,0,2,0,0],[1,0,3,1,0],[1,0,4,0,0],[2,0,0,1,0],[2,0,1,0,0],[2,0,2,1,0],[2,0,3,0,0],[2,0,4,1,0],[3,0,0,0,0],[3,0,1,1,0],[3,0,2,0,0],[3,0,3,1,0],[3,0,4,0,0],[4,0,0,1,0],[4,0,1,0,0],[4,0,2,1,0],[4,0,3,0,0],[4,0,4,1,0],[5,0,0,0,0],[5,0,1,1,0],[5,0,2,0,0],[5,0,3,1,0],[5,0,4,0,0],[3,1,4,2,0],[1,1,2,3,0],[5,1,2,3,0],[0,0,2,4,1],[6,0,2,4,1],[0,1,3,5,1],[1,1,3,6,1],[2,1,2,5,1],[2,1,3,5,1],[3,1,2,7,1],[3,1,3,6,1],[4,1,3,6,1],[5,1,3,6,1],[6,1,3,5,1],[1,2,2,8,1],[3,2,4,9,1],[5,2,2,8,1]],"e":0},"punch_cards":{"size":[6,3,5],"p":[["minecraft:white_concrete",{}],["minecraft:snow_block",{}],["minecraft:stripped_spruce_log",{"axis":"y"}],["create:metal_girder",{"waterlogged":"false","top":"true","bottom":"true","x":"false","z":"false","axis":"y"}],["create:large_cogwheel",{"waterlogged":"false","axis":"x"}],["powergrid:punch_card_reader",{"facing":"north"}],["create:shaft",{"waterlogged":"false","axis":"x"}],["create:cogwheel",{"waterlogged":"false","axis":"x"}],["powergrid:wire_connector",{"facing":"down"}],["powergrid:light_fixture",{"facing":"up","power":"0","axis_along_first":"true"}]],"b":[[0,0,0,0,0],[0,0,1,1,0],[0,0,2,0,0],[0,0,3,1,0],[0,0,4,0,0],[1,0,0,1,0],[1,0,1,0,0],[1,0,2,1,0],[1,0,3,0,0],[1,0,4,1,0],[2,0,0,0,0],[2,0,1,1,0],[2,0,2,0,0],[2,0,3,1,0],[2,0,4,0,0],[3,0,0,1,0],[3,0,1,0,0],[3,0,2,1,0],[3,0,3,0,0],[3,0,4,1,0],[4,0,0,0,0],[4,0,1,1,0],[4,0,2,0,0],[4,0,3,1,0],[4,0,4,0,0],[2,1,4,2,0],[0,1,3,3,0],[4,1,3,3,0],[5,0,3,4,1],[2,1,2,5,1],[3,1,2,6,1],[4,1,2,6,1],[5,1,2,7,1],[0,2,3,8,1],[2,2,4,9,1],[4,2,3,8,1]],"e":0}}
    const PROFILE = new $Profile($UUID.fromString('5a7c0e57-0b0e-4a11-9d51-70e9a1c0f008'), '[zpwg8]')
    let PG = null
    function pg() {
        if (PG === null) PG = {
            Electric: Java.loadClass('org.patryk3211.powergrid.electricity.base.ElectricBehaviour'),
            Plotter: Java.loadClass('org.patryk3211.powergrid.kinetics.plotter.PlotterBlockEntity'),
            Display: Java.loadClass('org.patryk3211.powergrid.electricity.modulardisplay.ModularDisplayBlockEntity'),
            ModuleType: Java.loadClass('org.patryk3211.powergrid.electricity.modulardisplay.DisplayModuleType'),
            Reader: Java.loadClass('org.patryk3211.powergrid.kinetics.punchcard.PunchCardReaderBlockEntity'),
            ReaderBlock: Java.loadClass('org.patryk3211.powergrid.kinetics.punchcard.PunchCardReaderBlock'),
            Bell: Java.loadClass('org.patryk3211.powergrid.electricity.bell.AlarmBellBlockEntity'),
            CRT: Java.loadClass('org.patryk3211.powergrid.electricity.crt.CRTBlockEntity'),
            BEs: Java.loadClass('org.patryk3211.powergrid.collections.ModdedBlockEntities'),
            Blocks: Java.loadClass('org.patryk3211.powergrid.collections.ModdedBlocks'),
            Sources: Java.loadClass('org.patryk3211.powergrid.collections.ModdedDisplaySources'),
            Gauge: Java.loadClass('org.patryk3211.powergrid.electricity.gauge.GaugeBlockEntity'),
        }
        return PG
    }
    let CC = null
    function cc() {
        if (CC === null) CC = {
            Voltage: Java.loadClass('org.patryk3211.powergrid.compat.cc.gauges.VoltageGaugePeripheral'),
            Current: Java.loadClass('org.patryk3211.powergrid.compat.cc.gauges.CurrentGaugePeripheral'),
            Power: Java.loadClass('org.patryk3211.powergrid.compat.cc.gauges.PowerGaugePeripheral'),
            Energy: Java.loadClass('org.patryk3211.powergrid.compat.cc.gauges.EnergyMeterPeripheral'),
            Battery: Java.loadClass('org.patryk3211.powergrid.compat.cc.battery.BatteryPeripheral'),
            Clutch: Java.loadClass('org.patryk3211.powergrid.compat.cc.clutch.GeneratorClutchPeripheral'),
            Converter: Java.loadClass('org.patryk3211.powergrid.compat.cc.redstone.RedstoneConverterPeripheral'),
        }
        return CC
    }
    function out(s) { console.info('[zpwg8] ' + s) }
    function gtime(level) {
        try { return level.getLevelData().getGameTime() } catch (e1) {}
        return 'ms' + Date.now()
    }
    function pos(x, y, z) { return new $Pos(x, y, z) }
    // class name from String(obj) (Class@hash for objects without toString): no getClass() in probes (gotchas.md)
    function simpleName(o) { let s = String(o); let m = s.match(/([A-Za-z0-9_$]+)@[0-9a-f]+$/); return m ? m[1].replace(/^.*\$/, '') : s }
    function fmt(x) { return (Math.round(x * 1e4) / 1e4) }
    function idOf(stack) { return String($FR.ITEMS.getKey(stack.getItem())) }
    function blockId(state) { return String($FR.BLOCKS.getKey(state.getBlock())) }
    function item(id) { return $FR.ITEMS.getValue(new $RL(id)) }
    function fake(level) { return $Fake.get(level, PROFILE) }
    // ItemStack.getTag is not reachable from Rhino on these stacks (scratch-test gotchas): read the saved stack
    function stackTag(stack) {
        let t = stack.save(new $Compound())
        return t.contains('tag') ? t.getCompound('tag') : new $Compound()
    }
    function hexBytes(hex) {
        let b = []
        for (let i = 0; i < 16; i++) {
            let v = parseInt(hex.substr(i * 2, 2), 16)
            if (isNaN(v)) v = 0
            b.push(v > 127 ? v - 256 : v)
        }
        return b
    }
    function cardStack(hex, name) {
        let snbt = '{id:"powergrid:punch_card",Count:1b,tag:{Data:[B;' + hexBytes(hex).map(v => v + 'b').join(',') + ']'
            + (name ? ',display:{Name:\'{"text":"' + name + '"}\'}' : '') + '}}'
        return $ItemStack.of($TagParser.parseTag(snbt))
    }
    function dataHex(tag) {
        if (!tag.contains('Data')) return 'none'
        let arr = tag.getByteArray('Data')
        let s = ''
        for (let i = 0; i < arr.length; i++) { let v = arr[i] & 255; s += (v < 16 ? '0' : '') + v.toString(16) }
        return s
    }
    function terminals(be, n) {
        let eb = $BEB.get(be, pg().Electric.TYPE)
        if (eb == null) return 'no electric behaviour'
        let vs = []
        for (let i = 0; i < n; i++) if (eb.hasTerminal(i)) vs.push('T' + i + '=' + fmt(eb.getTerminal(i).getVoltage()))
        return vs.join(' ')
    }
    function speedOf(be) { return be instanceof $Kinetic ? fmt(be.getSpeed()) : 'n/a' }
    function check(level) {
        for (let id of ['plotter', 'alarm_bell', 'crt', 'andesite_encased_crt', 'brass_encased_crt', 'punch_card_reader', 'modular_display'])
            out('block powergrid:' + id + ' registered=' + $FR.BLOCKS.containsKey(new $RL('powergrid', id))
                + ' item=' + $FR.ITEMS.containsKey(new $RL('powergrid', id)))
        for (let id of ['display_module', 'punch_card', 'incomplete_punch_card'])
            out('item powergrid:' + id + ' registered=' + $FR.ITEMS.containsKey(new $RL('powergrid', id)))
        for (let id of ['plotter', 'alarm_bell', 'crt', 'punch_card_reader', 'modular_display'])
            out('block entity powergrid:' + id + ' registered=' + $FR.BLOCK_ENTITY_TYPES.containsKey(new $RL('powergrid', id)))
        out('menu powergrid:punch_card registered=' + $FR.MENU_TYPES.containsKey(new $RL('powergrid', 'punch_card')))
        let crt = pg().Blocks.CRT.get()
        let vs = []
        $Encasing.getVariants(crt).forEach(b => vs.push(String($FR.BLOCKS.getKey(b))))
        out('crt encased variants=' + vs.join(','))
        out('computercraft loaded=' + Platform.isLoaded('computercraft'))
        let keys = []
        // values() is a Java array: index it (no JS forEach on Java arrays)
        let mts = pg().ModuleType.values()
        for (let i = 0; i < mts.length; i++) keys.push(mts[i].ordinal() + ':' + mts[i].getTranslationKey())
        out('display module types ' + keys.join(' | '))
    }
    function fp(level, a) {
        let p = fake(level)
        p.setGameMode($GameType.SURVIVAL)
        // moveTo(5 numbers) resolves to the 3-argument teleportTo in Rhino and throws (r1113 pwg10b): absMoveTo
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
        // Create's value box handler ray-traces from the player's eyes (RaycastHelper), so the player looks at the point
        p.lookAt($Anchor.EYES, hitVec)
        let hit = new $BHR(hitVec, $Dir.byName(face), P, false)
        // r1113 pwg12r SQ-PWG-63: every click landed in slot 3, the slot a block-centre hit gives; print what Java got
        let hl = hit.getLocation()
        out('use hit loc=' + fmt(hl.x() - P.getX()) + ',' + fmt(hl.y() - P.getY()) + ',' + fmt(hl.z() - P.getZ()) + ' dir=' + hit.getDirection() + ' pos=' + hit.getBlockPos().toShortString())
        // r1113 pwg16 SQ-PWG-63: Create 0.5.1's ValueSettingsInputHandler takes EVERY right-click of a FakePlayer on a
        // block with a value box (testHit is skipped for fake players) and ScrollValueBehaviour.onShortInteract replays it
        // as BlockState.use with a block-CENTRE hit, so every click landed in slot 3. Real players are not affected. For
        // the modular display the probe calls the block's use with its own hit (the value box path is a client check).
        let direct = blockId(level.getBlockState(P)) == 'powergrid:modular_display'
        if (direct) out('use direct BlockState.use (fake player bypasses Create value box handler)')
        let res = direct ? level.getBlockState(P).use(level, p, $Hand.MAIN_HAND, hit) : p.gameMode.useItemOn(p, level, held, $Hand.MAIN_HAND, hit)
        let now = p.getMainHandItem()
        out('use ' + before + ' on ' + blockId(level.getBlockState(P)) + ' face=' + face + ' at ' + fmt(dx) + ',' + fmt(dy) + ',' + fmt(dz)
            + ' sneaking=' + p.isShiftKeyDown() + ' result=' + res + ' block after=' + level.getBlockState(P)
            + ' held after=' + idOf(now) + 'x' + now.getCount())
    }
    function plot(level, P, n) {
        let be = level.getBlockEntity(P)
        if (!(be instanceof pg().Plotter)) { out('plot: no plotter at ' + P); return }
        let tag = be.saveWithoutMetadata()
        let samples = tag.getList('Samples', 5)
        let head = tag.getInt('Head')
        let len = samples.size()
        let last = []
        for (let i = n; i >= 1; i--) if (len > 0) last.push(fmt(samples.getFloat(((head - i) % len + len) % len)))
        let sc = $BEB.get(be, $Scroll.TYPE)
        out('plot speed=' + speedOf(be) + ' fulfilled=' + be.isSpeedRequirementFulfilled() + ' range=' + (sc == null ? '?' : sc.getValue())
            + ' buffer=' + len + ' head=' + head + ' color=' + tag.getInt('Color') + ' t=' + gtime(level))
        out('plot last ' + n + ': ' + last.join(' '))
        out('plot terminals ' + terminals(be, 2))
    }
    function md(level, P) {
        let be = level.getBlockEntity(P)
        if (!(be instanceof pg().Display)) { out('md: no modular display at ' + P); return }
        let tag = be.saveWithoutMetadata()
        let slots = tag.getList('slots', 8)
        let rbp = tag.getIntArray('rbp')
        for (let i = 0; i < 4; i++) {
            let s = i < slots.size() ? String(slots.getString(i)) : ''
            out('md slot ' + i + ' module=' + (s == '' ? 'empty' : s) + ' blankingRemoved=' + (i < rbp.length ? rbp[i] : '?')
                + ' temp=' + fmt(tag.getFloat('SlotTemp_' + i)))
        }
        let sc = $BEB.get(be, $Scroll.TYPE)
        out('md option=' + (sc == null ? '?' : sc.getValue()) + ' lastHitSlot=' + be.lastHitSlot + ' t=' + gtime(level))
        out('md terminals ' + terminals(be, 9))
    }
    function mdtype(level, P, slot, type) {
        let be = level.getBlockEntity(P)
        if (!(be instanceof pg().Display)) { out('mdtype: no modular display at ' + P); return }
        be.lastHitSlot = slot
        let sc = $BEB.get(be, $Scroll.TYPE)
        sc.setValue(type)
        out('mdtype slot ' + slot + ' -> ' + type)
        md(level, P)
    }
    function reader(level, P) {
        let be = level.getBlockEntity(P)
        if (!(be instanceof pg().Reader)) { out('reader: none at ' + P); return }
        let tag = be.saveWithoutMetadata()
        let card = be.currentItem()
        let st = level.getBlockState(P)
        out('reader class=' + simpleName(be) + ' card=' + (card.isEmpty() ? 'none' : idOf(card) + ' data=' + dataHex(stackTag(card)))
            + ' angle=' + fmt(tag.getFloat('Angle')) + ' redstone=' + be.getRedstoneOutput()
            + ' comparator=' + st.getAnalogOutputSignal(level, P) + ' speed=' + speedOf(be) + ' t=' + gtime(level))
        out('reader terminals ' + terminals(be, 9))
    }
    function card(level, P, hex, side) {
        let be = level.getBlockEntity(P)
        if (!(be instanceof pg().Reader)) { out('card: no reader at ' + P); return }
        let d = side == 'side' ? level.getBlockState(P).getValue(pg().ReaderBlock.HORIZONTAL_FACING) : $Dir.UP
        let ok = be.insertCard(cardStack(hex, null), d)
        out('card insert from ' + d + ' ok=' + ok)
        reader(level, P)
    }
    function extract(level, P) {
        let be = level.getBlockEntity(P)
        if (!(be instanceof pg().Reader)) { out('extract: no reader at ' + P); return }
        let s = be.extractCard()
        out('extract ' + (s.isEmpty() ? 'nothing' : idOf(s) + ' data=' + dataHex(stackTag(s))))
    }
    function bell(level, P) {
        let be = level.getBlockEntity(P)
        if (!(be instanceof pg().Bell)) { out('bell: none at ' + P); return }
        out('bell volume=' + fmt(be.getVolume()) + ' pitch=' + fmt(be.getPitch()) + ' terminals ' + terminals(be, 2) + ' t=' + gtime(level))
    }
    function crtInfo(level, P) {
        let be = level.getBlockEntity(P)
        if (!(be instanceof pg().CRT)) { out('crt: none at ' + P + ' (' + blockId(level.getBlockState(P)) + ')'); return }
        let tag = be.saveWithoutMetadata()
        out('crt block=' + level.getBlockState(P) + ' color=' + tag.getInt('Color'))
        out('crt terminals ' + terminals(be, 7))
    }
    // the same mapping as CCBridge's peripheral provider
    function peripheralFor(be) {
        let t = be.getType(), B = pg().BEs
        if (t == B.VOLTAGE_METER.get()) return cc().Voltage.of(be)
        if (t == B.CURRENT_METER.get()) return cc().Current.of(be)
        if (t == B.POWER_METER.get()) return cc().Power.of(be)
        if (t == B.GENERATOR_CLUTCH.get()) return new (cc().Clutch)(be)
        if (t == B.ENERGY_METER.get()) return new (cc().Energy)(be)
        if (t == B.MULTIBLOCK_BATTERY.get()) return new (cc().Battery)(be)
        if (t == B.REDSTONE_CONVERTER.get()) return new (cc().Converter)(be)
        return null
    }
    function periph(level, P) {
        let be = level.getBlockEntity(P)
        if (be == null) { out('periph: no block entity at ' + P); return }
        let p = peripheralFor(be)
        if (p == null) { out('periph: none for ' + blockId(level.getBlockState(P))); return }
        let vals = []
        for (let m of ['voltage', 'current', 'power', 'energy', 'getValue', 'maxRange', 'rangePercentage', 'capacity', 'chargePercentage', 'powerDraw', 'mode', 'load', 'rpm']) {
            try { if (typeof p[m] === 'function') vals.push(m + '=' + p[m]()) } catch (err) { vals.push(m + '=error ' + err) }
        }
        out('periph type=' + p.getType() + ' ' + vals.join(' ') + ' t=' + gtime(level))
        if (be instanceof pg().Gauge) out('periph gauge value=' + fmt(be.getValue()) + ' max=' + fmt(be.getMaxValue()))
    }
    function conv(level, P, v) {
        let be = level.getBlockEntity(P)
        let p = be == null ? null : peripheralFor(be)
        if (p == null || !(p instanceof cc().Converter)) { out('conv: no redstone converter at ' + P); return }
        if (v == 'clear') p.clearValue(); else p.setValue(parseFloat(v))
        out('conv override=' + be.signalOverride + ' t=' + gtime(level))
    }
    function board(level, P) {
        let be = level.getBlockEntity(P)
        if (be == null) { out('board: no block entity at ' + P); return }
        let tag = be.saveWithoutMetadata()
        let s = String(tag.toString())
        out('board ' + blockId(level.getBlockState(P)) + ' nbt=' + (s.length > 900 ? s.substring(0, 900) + '...' : s))
    }
    function dlink(level, L, T, mode) {
        let be = level.getBlockEntity(L)
        if (be == null) { out('dlink: no display link at ' + L); return }
        let src = be.getSourcePosition()
        let srcBe = level.getBlockEntity(src)
        be.target(T)
        be.activeSource = pg().Sources.ELECTRIC_GAUGE
        let cfg = new $Compound()
        cfg.putInt('Mode', mode)
        be.setSourceConfig(cfg)
        be.targetLine = 0
        be.updateGatheredData()
        be.tickSource()
        out('dlink source pos=' + src + ' (' + blockId(level.getBlockState(src)) + (srcBe instanceof pg().Gauge ? ' value=' + fmt(srcBe.getValue()) : '')
            + ') target=' + be.getTargetPosition() + ' activeTarget=' + (be.activeTarget == null ? 'none' : simpleName(be.activeTarget)))
        board(level, T)
    }
    function drops(level, P, r) {
        let list = level.getEntitiesOfClass($ItemEntity, new $AABB(P.getX() - r, P.getY() - r, P.getZ() - r, P.getX() + r + 1, P.getY() + r + 1, P.getZ() + r + 1))
        out('drops ' + list.size())
        list.forEach(e => {
            let s = e.getItem()
            let tag = stackTag(s)
            out('drop ' + idOf(s) + ' x' + s.getCount() + (idOf(s) == 'powergrid:punch_card' ? ' data=' + dataHex(tag) : ''))
        })
    }
    function stateProps(state) {
        let m = {}
        state.getProperties().forEach(p => { m[String(p.getName())] = String(p.getName(state.getValue(p))) })
        return m
    }
    // the zpwg5 structure check (pwg_p5_probe.js), for the P8 files
    function nbtCheck(level, name, x, y, z) {
        let ex = EXPECTED[name]
        let opt = level.getServer().getStructureManager().get(new $RL('pwgponder', name))
        if (!opt.isPresent()) { out('nbt ' + name + ' missing (datapack not loaded?)'); return 'bad' }
        let t = opt.get()
        let size = t.getSize()
        let sizeOk = size.getX() == ex.size[0] && size.getY() == ex.size[1] && size.getZ() == ex.size[2]
        let origin = new $Pos(x, y, z)
        t.placeInWorld(level, origin, origin, new $SPS().setKnownShape(true), level.getRandom(), 2)
        let badName = [], dropped = [], rejected = [], noBe = 0, n = 0
        ex.b.forEach(b => {
            n++
            let pe = ex.p[b[3]], want = pe[0], props = pe[1]
            let bp = new $Pos(x + b[0], y + b[1], z + b[2])
            let st = level.getBlockState(bp)
            let got = String($FR.BLOCKS.getKey(st.getBlock()))
            if (got != want) { if (badName.length < 6) badName.push(want + '->' + got); return }
            let have = stateProps(st)
            Object.keys(props).forEach(k => {
                let v = props[k]
                let tag = want.split(':')[1] + '.' + k
                if (have[k] === undefined) { if (dropped.indexOf(tag) < 0) dropped.push(tag) }
                else if (have[k] != v) { let r = tag + '=' + v + '(got ' + have[k] + ')'; if (rejected.indexOf(r) < 0) rejected.push(r) }
            })
            if (b[4] == 1 && level.getBlockEntity(bp) == null) noBe++
        })
        let ents = level.getEntitiesOfClass($Entity, new $AABB(x - 1, y - 1, z - 1, x + ex.size[0] + 1, y + ex.size[1] + 1, z + ex.size[2] + 1)).size()
        let verdict = (!sizeOk || badName.length > 0 || dropped.length > 0 || rejected.length > 0 || noBe > 0) ? 'bad' : 'clean'
        out('nbt ' + name + ' ' + verdict + ' blocks=' + n + ' size=' + (sizeOk ? 'ok' : 'WRONG') + ' badName=[' + badName.join(' ')
            + '] droppedProps=[' + dropped.join(' ') + '] rejectedValues=[' + rejected.join(' ') + '] missingBE=' + noBe
            + ' entities=' + ents + '/' + ex.e)
        return verdict
    }
    function nbtall(level, x, y, z) {
        let c = { clean: 0, bad: 0, error: 0 }, i = 0
        Object.keys(EXPECTED).forEach(name => {
            let v
            try { v = nbtCheck(level, name, x + 12 * i, y, z) } catch (err) { out('nbt ' + name + ' error ' + err); v = 'error' }
            c[v]++
            i++
        })
        out('nbt summary files=' + i + ' clean=' + c.clean + ' bad=' + c.bad + ' error=' + c.error)
    }
    function run(level, a) {
        let cmd = a[0]
        if (cmd == 'check') { check(level); return }
        if (cmd == 'fp') { fp(level, a); return }
        if (cmd == 'hold') {
            let s = item(a[1]).getDefaultInstance()
            if (a.length > 2) s.setCount(parseInt(a[2]))
            fake(level).setItemInHand($Hand.MAIN_HAND, s)
            out('hold ' + idOf(fake(level).getMainHandItem()) + 'x' + fake(level).getMainHandItem().getCount())
            return
        }
        if (cmd == 'holdcard') { fake(level).setItemInHand($Hand.MAIN_HAND, cardStack(a[1], a.length > 2 ? a[2] : null)); out('holdcard data=' + dataHex(stackTag(fake(level).getMainHandItem()))); return }
        // a fake player never ticks, so its pose (isCrouching, which the modular display reads) is set here too
        if (cmd == 'sneak') { let fpl = fake(level); fpl.setShiftKeyDown(a[1] == 'on'); fpl.setPose(a[1] == 'on' ? $Pose.CROUCHING : $Pose.STANDING); out('sneak ' + fpl.isShiftKeyDown() + ' crouching=' + fpl.isCrouching()); return }
        let P = pos(parseInt(a[1]), parseInt(a[2]), parseInt(a[3]))
        if (cmd == 'use') use(level, P, a[4], parseFloat(a[5]), parseFloat(a[6]), parseFloat(a[7]))
        else if (cmd == 'plot') plot(level, P, a.length > 4 ? parseInt(a[4]) : 10)
        else if (cmd == 'md') md(level, P)
        else if (cmd == 'mdtype') mdtype(level, P, parseInt(a[4]), parseInt(a[5]))
        else if (cmd == 'reader') reader(level, P)
        else if (cmd == 'card') card(level, P, a[4], a.length > 5 ? a[5] : 'up')
        else if (cmd == 'extract') extract(level, P)
        else if (cmd == 'bell') bell(level, P)
        else if (cmd == 'crt') crtInfo(level, P)
        else if (cmd == 'periph') periph(level, P)
        else if (cmd == 'conv') conv(level, P, a[4])
        else if (cmd == 'board') board(level, P)
        else if (cmd == 'dlink') dlink(level, P, pos(parseInt(a[4]), parseInt(a[5]), parseInt(a[6])), a.length > 7 ? parseInt(a[7]) : 2)
        else if (cmd == 'drops') drops(level, P, parseInt(a[4]))
        else if (cmd == 'nbtall') nbtall(level, P.getX(), P.getY(), P.getZ())
        else out('unknown subcommand ' + cmd)
    }
    ServerEvents.commandRegistry(e => {
        const C = e.commands
        e.register(C.literal('zpwg8').requires(s => s.hasPermission(2))
            .then(C.argument('args', $Args.greedyString()).executes(ctx => {
                try {
                    run(ctx.source.getLevel(), String($Args.getString(ctx, 'args')).split(' '))
                } catch (err) { out('error ' + err) }
                out('done')
                return 1
            })))
    })
})()
