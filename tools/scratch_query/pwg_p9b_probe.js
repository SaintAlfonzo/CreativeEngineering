// Scratch-only probe for the Create: Power Grid port, phase P9b rows (SQ-PWG-72 onward, plan_power_grid.md P9b: the
// circuit design table, the editor's server side, the circuit schematic item, the four menus). Copy into a scratch
// server's kubejs/server_scripts as zz_pwg_p9b_probe.js next to the P2 to P9a probes (zpwg3: place, w, src, be,
// recipes; zpwg9: board, bmap, bcomp); never into the pack. Uses its own Forge fake player ("[zpwg9b]"). No chunk loads
// here: `forceload add` the area first (Rhino deadlock). The editor itself is client-only (CQ-PWG rows); this probe runs
// the two C2S packets' server side (`apply(player)`, after an encode / decode round trip of the packet) with the fake
// player, so the row tests what a client's editor would send.
//   check                         design table block, item and block entity type; circuit_schematic item and its
//                                 circuit_schematic_holder tag; the four menu types; the packets after COMPONENT_ITEMS
//   fp X Y Z / mode creative|survival / sneak on|off / hold ITEM      the fake player (as zpwg9)
//   tbe X Y Z                     design table: block state (facing, powered, light), isPowered, wire power, the
//                                 schematic (name, components, pads), slots 0 to 2 (item, count, Schematic components)
//   slot X Y Z N ITEM [COUNT]     puts ITEM in table slot N (0 load, 1 save, 2 output); ITEM "air" clears it
//   move X Y Z FROM TO            moves the stack in table slot FROM to slot TO (tags kept; TO is overwritten)
//   menus X Y Z [BX BY BZ]        builds the table's three menus (index 0 table, 1 edit, 2 load) and, with a board,
//                                 CircuitBoardEditMenu for the fake player on the server: type id, class, slot count
//   fromboard X Y Z BX BY BZ NAME   SaveSchematicC2SPacket (editor save) carrying a copy of the board's schematic and
//                                 NAME, round-tripped through its wire format, applied as the fake player
//   write X Y Z / load X Y Z      SaveSchematicC2SPacket without a schematic: write to the item in slot 1 (output
//                                 slot 2) / read the item in slot 0
//   screen X Y Z IDX              ChangeScreenC2SPacket (0 table, 1 edit, 2 load) applied as the fake player; prints
//                                 the menu the fake player then has
// After every packet the fake player's menu is closed again; closing a table menu returns the table's slot items to
// the fake player (upstream: CircuitDesignTableMenu.removed), so set slots after fromboard / screen, before write.
//   itemrt X Y Z                  schematic item NBT round trip of the table's schematic: toItemStack, stack saved to
//                                 NBT and read back, CircuitSchematic.fromStack; compares the safe NBT of both
//   toboard X Y Z BX BY BZ        the circuit_schematic in table slot 2: creative sneak-use of the item (returns a
//                                 circuit board carrying the schematic), then placed by the fake player on top of the
//                                 block below BX BY BZ (facing north as seen from the south)
//   bedit BX BY BZ CX CY PROP VALUE NAME   board editor save: a copy of the board's schematic with PROP of the part
//                                 at CX CY set to VALUE, sent with NAME as SaveSchematicC2SPacket to the board
//   cord SX SY SZ TX TY TZ        powers a table with a copper cord as a player plugs one: the fake player holds
//                                 powergrid:copper_cord, clicks the cord socket of a powergrid:socket at S placed
//                                 facing=down (lying on the floor; socket on its top, hit from above), then the socket
//                                 on the back (south face) of a table at T placed facing=north; prints both results and
//                                 the cord entities near T. Wire the socket's terminals 0 / 1 to a source (zpwg3 w)
//   buse BX BY BZ                 the fake player (hands emptied) uses the board block on its top centre: creative
//                                 opens CircuitBoardEditMenu (upstream), survival does not
// Output: "[zpwg9b] ..." lines in logs/latest.log, then "[zpwg9b] done".
(() => {
    const $Args = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $FR = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $RL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $Pos = Java.loadClass('net.minecraft.core.BlockPos')
    const $Dir = Java.loadClass('net.minecraft.core.Direction')
    const $Vec3 = Java.loadClass('net.minecraft.world.phys.Vec3')
    const $BHR = Java.loadClass('net.minecraft.world.phys.BlockHitResult')
    const $Hand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $Anchor = Java.loadClass('net.minecraft.commands.arguments.EntityAnchorArgument$Anchor')
    const $GameType = Java.loadClass('net.minecraft.world.level.GameType')
    const $Pose = Java.loadClass('net.minecraft.world.entity.Pose')
    const $Fake = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $Fakeplayer = Java.loadClass('net.minecraftforge.common.util.FakePlayer')
    const $AABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $Profile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $UUID = Java.loadClass('java.util.UUID')
    const $Compound = Java.loadClass('net.minecraft.nbt.CompoundTag')
    const $ItemStack = Java.loadClass('net.minecraft.world.item.ItemStack')
    const $TagKey = Java.loadClass('net.minecraft.tags.TagKey')
    const $Registry = Java.loadClass('net.minecraft.core.Registry')
    const $FBB = Java.loadClass('net.minecraft.network.FriendlyByteBuf')
    // io.netty classes are blocked by the KubeJS class filter (r1113 pwg15): without a buffer the C2S packets are applied
    // as built, with no encode / decode round trip (the row says so in its output: bytes=none)
    let $Unpooled = null
    try { $Unpooled = Java.loadClass('io.netty.buffer.Unpooled') } catch (e0) { $Unpooled = null }
    const PROFILE = new $Profile($UUID.fromString('5a7c0e57-0b0e-4a11-9d51-70e9a1c0f00b'), '[zpwg9b]')
    const MENUS = ['circuit_design_table', 'circuit_design_table_edit', 'circuit_design_table_load', 'circuit_board_edit']
    let PG = null
    function pg() {
        if (PG === null) PG = {
            Table: Java.loadClass('org.patryk3211.powergrid.circuits.editor.CircuitDesignTableBlockEntity'),
            Board: Java.loadClass('org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity'),
            BoardEditMenu: Java.loadClass('org.patryk3211.powergrid.circuits.editor.CircuitBoardEditMenu'),
            Schematic: Java.loadClass('org.patryk3211.powergrid.circuits.schematic.CircuitSchematic'),
            Registry: Java.loadClass('org.patryk3211.powergrid.circuits.components.ComponentRegistry'),
            Save: Java.loadClass('org.patryk3211.powergrid.network.packets.SaveSchematicC2SPacket'),
            Change: Java.loadClass('org.patryk3211.powergrid.network.packets.ChangeScreenC2SPacket'),
            Packets: Java.loadClass('org.patryk3211.powergrid.collections.ModdedPackets'),
        }
        return PG
    }
    const $FRtag = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    // ItemStack.is(TagKey) is ambiguous in Rhino (r1113 pwg16): ask Forge's tag manager instead
    function inTag(it, tagKey) {
        try { return $FRtag.ITEMS.tags().getTag(tagKey).contains(it) } catch (e1) { return 'error ' + e1 }
    }
    function out(s) { console.info('[zpwg9b] ' + s) }
    function pos(x, y, z) { return new $Pos(x, y, z) }
    function rl(s) { return s.indexOf(':') >= 0 ? new $RL(s) : new $RL('powergrid', s) }
    function fmt(x) { return (Math.round(x * 1e4) / 1e4) }
    function simpleName(o) { let s = String(o); let m = s.match(/([A-Za-z0-9_$]+)@[0-9a-f]+$/); return m ? m[1].replace(/^.*\$/, '') : s }
    function idOf(stack) { return String($FR.ITEMS.getKey(stack.getItem())) }
    function item(id) { return $FR.ITEMS.getValue(rl(id)) }
    function fake(level) { return $Fake.get(level, PROFILE) }
    function stackLine(stack) {
        if (stack.isEmpty()) return 'empty'
        let t = stack.save(new $Compound())
        let tag = t.contains('tag') ? t.getCompound('tag') : new $Compound()
        let sch = tag.contains('Schematic') ? ' Schematic components=' + tag.getCompound('Schematic').getList('Components', 10).size() : ''
        let name = stack.hasCustomHoverName() ? ' name="' + stack.getHoverName().getString() + '"' : ''
        return idOf(stack) + ' x' + stack.getCount() + sch + name
    }
    function schLine(s) {
        if (s == null) return 'schematic null'
        let n = s.getName()
        return 'schematic name=' + (n == null ? 'null' : '"' + n + '"') + ' components=' + s.components().size()
    }
    function tableAt(level, P) {
        let be = level.getBlockEntity(P)
        if (!(be instanceof pg().Table)) throw 'no circuit design table at ' + P
        return be
    }
    function boardAt(level, P) {
        let be = level.getBlockEntity(P)
        if (!(be instanceof pg().Board)) throw 'no circuit board at ' + P
        return be
    }
    function menuLine(p) {
        let m = p.containerMenu
        if (m == null || m == p.inventoryMenu) return 'menu=inventory'
        let type = null
        try { type = $FR.MENU_TYPES.getKey(m.getType()) } catch (e) { type = '?' }
        return 'menu=' + type + ' class=' + simpleName(m) + ' slots=' + m.slots.size()
    }
    // C2S packets: encode, decode (the server's view of the bytes), then the server side as the fake player
    function roundTrip(packet, cls) {
        if ($Unpooled == null) return [packet, 'none']
        let buf = new $FBB($Unpooled.buffer())
        packet.encode(buf)
        let n = buf.readableBytes()
        let decoded = new cls(buf)
        return [decoded, n]
    }
    // r1113 pwg16: the menu stayed open after closeContainer (a far screen packet showed the table menu left from the
    // last near one); close, fall back to doCloseContainer, and say so when a menu is still open
    // r1113 pwg17: closeContainer is not visible on a FakePlayer from Rhino ("Cannot find function"); doCloseContainer
    // (ServerPlayer: menu removed, inventoryMenu back, no packet) closed every menu. Only that call now.
    function closeMenu(p) {
        try { p.doCloseContainer() } catch (e3) { out('close: doCloseContainer threw ' + e3) }
        if (p.containerMenu != null && p.containerMenu != p.inventoryMenu) out('close: ' + menuLine(p) + ' still open after doCloseContainer')
    }
    function applyAs(level, decoded, what) {
        let p = fake(level)
        try {
            decoded.apply(p)
            out(what + ' applied ' + menuLine(p))
        } catch (err) {
            out(what + ' apply threw ' + err + ' (after the schematic part; ' + menuLine(p) + ')')
        }
        closeMenu(p)
    }
    function check(level) {
        out('block powergrid:circuit_design_table registered=' + $FR.BLOCKS.containsKey(rl('circuit_design_table'))
            + ' item=' + $FR.ITEMS.containsKey(rl('circuit_design_table'))
            + ' blockEntity=' + $FR.BLOCK_ENTITY_TYPES.containsKey(rl('circuit_design_table')))
        out('item powergrid:circuit_schematic registered=' + $FR.ITEMS.containsKey(rl('circuit_schematic'))
            // Item.toString() is the registry path in 1.19.2 and getClass() is filtered: instanceof the loaded class
            + ' class=' + (item('circuit_schematic') instanceof Java.loadClass('org.patryk3211.powergrid.circuits.schematic.CircuitSchematicItem') ? 'CircuitSchematicItem' : 'other(' + String(item('circuit_schematic')) + ')') + ' maxStack=' + item('circuit_schematic').getMaxStackSize())
        let tag = $TagKey.create($Registry.ITEM_REGISTRY, rl('circuit_schematic_holder'))
        out('tag powergrid:circuit_schematic_holder has circuit_schematic=' + inTag(item('circuit_schematic'), tag)
            + ' circuit_board=' + inTag(item('circuit_board'), tag))
        out('menus ' + MENUS.map(m => m + '=' + $FR.MENU_TYPES.containsKey(rl(m))).join(' '))
        let later = ['acid_bucket', 'empty_circuit', 'unetched_circuit', 'incomplete_circuit', 'incomplete_unetched_circuit', 'integrated_circuit', 'incomplete_electrical_gizmo']
        out('items of P9c and P9d registered=' + later.filter(i => $FR.ITEMS.containsKey(rl(i))).join(',') + ' (expect none)')
        let pk = pg().Packets.values()
        let names = []
        for (let i = 0; i < pk.length; i++) names.push(pk[i].ordinal() + ':' + pk[i].name())
        out('packets ' + names.slice(-4).join(' '))
    }
    function tbe(level, P) {
        let be = tableAt(level, P)
        let st = level.getBlockState(P)
        out('table at ' + P.getX() + ' ' + P.getY() + ' ' + P.getZ() + ' state=' + st + ' light=' + st.getLightEmission()
            + ' isPowered=' + be.isPowered() + ' power=' + fmt(be.power()))
        out('table ' + schLine(be.getSchematic()) + ' displayName="' + be.getDisplayName().getString() + '" schematicName="' + be.getSchematicName() + '"')
        let inv = be.getInventory()
        for (let i = 0; i < 3; i++) out('slot ' + i + ' ' + stackLine(inv.getItem(i)))
    }
    function fp(level, a) {
        let p = fake(level)
        p.setGameMode($GameType.SURVIVAL)
        let mx = parseFloat(a[1]) + 0.5, my = parseFloat(a[2]), mz = parseFloat(a[3]) + 0.5
        try { p.absMoveTo(mx, my, mz, 0, 0) } catch (e1) { p.teleportTo(mx, my, mz) }
        p.setOnGround(true)
        p.setShiftKeyDown(false)
        p.setPose($Pose.STANDING)
        p.getInventory().clearContent()
        out('fp at ' + fmt(p.getX()) + ' ' + fmt(p.getY()) + ' ' + fmt(p.getZ()))
    }
    function menus(level, P, a) {
        let be = tableAt(level, P)
        let p = fake(level)
        for (let i = 0; i < 3; i++) {
            try {
                let m = be.createMenu(100 + i, p.getInventory(), p, i)
                out('menu ' + i + ' ' + (m == null ? 'null' : $FR.MENU_TYPES.getKey(m.getType()) + ' class=' + simpleName(m) + ' slots=' + m.slots.size()))
            } catch (err) { out('menu ' + i + ' threw ' + err) }
        }
        if (a.length >= 7) {
            let board = boardAt(level, pos(parseInt(a[4]), parseInt(a[5]), parseInt(a[6])))
            try {
                let m = pg().BoardEditMenu.create(103, p.getInventory(), board)
                out('menu board ' + $FR.MENU_TYPES.getKey(m.getType()) + ' class=' + simpleName(m) + ' slots=' + m.slots.size())
            } catch (err) { out('menu board threw ' + err) }
        }
    }
    function boardStackToPlace(level, stack, B) {
        let p = fake(level)
        p.setItemInHand($Hand.MAIN_HAND, stack)
        let below = B.below()
        let hitVec = new $Vec3(below.getX() + 0.5, below.getY() + 1.0, below.getZ() + 0.5)
        try { p.absMoveTo(B.getX() + 0.5, B.getY(), B.getZ() + 3.5, 180, 30) } catch (e1) { p.teleportTo(B.getX() + 0.5, B.getY(), B.getZ() + 3.5) }
        p.lookAt($Anchor.EYES, hitVec)
        let hit = new $BHR(hitVec, $Dir.UP, below, false)
        let res = p.gameMode.useItemOn(p, level, stack, $Hand.MAIN_HAND, hit)
        out('place ' + stackLine(stack) + ' result=' + res + ' -> ' + level.getBlockState(B))
    }
    // r1113 pwg17: eye and hit are plain JS [x, y, z] arrays. Vec3.x / .y / .z are field-and-method pairs in Rhino: passed
    // into a Java call they throw an NPE that escapes the try (the "unexpected error" after cord step 4 in pwg16 and pwg17)
    function lookFrom(p, eye, hit) {
        let hitVec = new $Vec3(hit[0], hit[1], hit[2])
        // absMoveTo takes the feet; the eye is 1.62 above them (standing)
        try { p.absMoveTo(eye[0], eye[1] - 1.62, eye[2], 0, 0) } catch (e1) { p.teleportTo(eye[0], eye[1] - 1.62, eye[2]) }
        p.setPose($Pose.STANDING)
        p.setShiftKeyDown(false)
        p.lookAt($Anchor.EYES, hitVec)
    }
    function cord(level, a) {
        // r1113 pwg16: this command ended with "An unexpected error occurred" and no print; a step line before each call
        // shows which one throws (the error escapes the script's catch)
        let S = pos(parseInt(a[1]), parseInt(a[2]), parseInt(a[3]))
        let T = pos(parseInt(a[4]), parseInt(a[5]), parseInt(a[6]))
        out('cord step 1 load CordItem')
        let Cord = Java.loadClass('org.patryk3211.powergrid.electricity.wire.powercord.CordItem')
        out('cord step 2 fake player')
        let p = fake(level)
        out('cord step 3 hold copper_cord')
        p.setItemInHand($Hand.MAIN_HAND, new $ItemStack(item('copper_cord'), 4))
        let sHit = [S.getX() + 0.5, S.getY() + 3 / 16, S.getZ() + 0.5]
        out('cord step 4 look at the socket')
        lookFrom(p, [sHit[0], sHit[1] + 1.5, sHit[2] + 0.01], sHit)
        out('cord step 5 CordItem.useOn socket')
        let r1 = null
        try { r1 = Cord.useOn(p, $Hand.MAIN_HAND, S, $Dir.UP) } catch (e1) { out('cord step 5 threw ' + e1) }
        out('cord socket ' + level.getBlockState(S) + ' result=' + (r1 == null ? 'null' : r1.isPresent() + '/' + r1.value()) + ' stack=' + stackLine(p.getMainHandItem())
            + ' tag=' + (function (t) { return t.contains('tag') ? String(t.getCompound('tag')) : 'none' })(p.getMainHandItem().save(new $Compound())))
        let tHit = [T.getX() + 0.5, T.getY() + 7 / 16, T.getZ() + 14 / 16]
        out('cord step 6 look at the table')
        lookFrom(p, [tHit[0], tHit[1], tHit[2] + 2.0], tHit)
        out('cord step 7 CordItem.useOn table')
        let r2 = null
        try { r2 = Cord.useOn(p, $Hand.MAIN_HAND, T, $Dir.SOUTH) } catch (e2) { out('cord step 7 threw ' + e2) }
        out('cord table ' + level.getBlockState(T) + ' result=' + (r2 == null ? 'null' : r2.isPresent() + '/' + r2.value()) + ' stack=' + stackLine(p.getMainHandItem()))
        let box = new ($AABB)(Math.min(S.getX(), T.getX()) - 2, Math.min(S.getY(), T.getY()) - 2, Math.min(S.getZ(), T.getZ()) - 2,
            Math.max(S.getX(), T.getX()) + 3, Math.max(S.getY(), T.getY()) + 3, Math.max(S.getZ(), T.getZ()) + 3)
        let names = []
        level.getEntities(null, box).forEach(e => { if (!(e instanceof $Fakeplayer)) names.push(simpleName(e)) })
        out('cord entities near: ' + (names.length ? names.join(' ') : 'none'))
    }
    function run(level, a) {
        let cmd = a[0]
        if (cmd == 'check') { check(level); return }
        if (cmd == 'cord') { cord(level, a); return }
        if (cmd == 'fp') { fp(level, a); return }
        if (cmd == 'mode') { fake(level).setGameMode(a[1] == 'creative' ? $GameType.CREATIVE : $GameType.SURVIVAL); out('mode ' + a[1] + ' creative=' + fake(level).isCreative()); return }
        if (cmd == 'sneak') { let f = fake(level); f.setShiftKeyDown(a[1] == 'on'); f.setPose(a[1] == 'on' ? $Pose.CROUCHING : $Pose.STANDING); out('sneak ' + f.isShiftKeyDown()); return }
        if (cmd == 'hold') { fake(level).setItemInHand($Hand.MAIN_HAND, item(a[1]).getDefaultInstance()); out('hold ' + idOf(fake(level).getMainHandItem())); return }
        let P = pos(parseInt(a[1]), parseInt(a[2]), parseInt(a[3]))
        if (cmd == 'tbe') tbe(level, P)
        else if (cmd == 'slot') {
            let be = tableAt(level, P)
            let n = parseInt(a[4])
            let st = a[5] == 'air' ? $ItemStack.EMPTY : new $ItemStack(item(a[5]), a.length > 6 ? parseInt(a[6]) : 1)
            be.getInventory().setItem(n, st)
            out('slot ' + n + ' set ' + stackLine(be.getInventory().getItem(n)))
        } else if (cmd == 'move') {
            let inv = tableAt(level, P).getInventory()
            let from = parseInt(a[4]), to = parseInt(a[5])
            inv.setItem(to, inv.getItem(from).copy())
            inv.setItem(from, $ItemStack.EMPTY)
            out('move ' + from + ' -> ' + to + ' now ' + stackLine(inv.getItem(to)))
        } else if (cmd == 'menus') menus(level, P, a)
        else if (cmd == 'fromboard') {
            let be = tableAt(level, P)
            let board = boardAt(level, pos(parseInt(a[4]), parseInt(a[5]), parseInt(a[6])))
            let name = a.slice(7).join(' ')
            let copy = new (pg().Schematic)(board.getSchematic())
            let before = schLine(be.getSchematic())
            let rt = roundTrip(new (pg().Save)(be, name, copy), pg().Save)
            out('fromboard packet bytes=' + rt[1] + ' board ' + schLine(copy))
            applyAs(level, rt[0], 'fromboard')
            out('fromboard table before: ' + before + ' after: ' + schLine(tableAt(level, P).getSchematic()))
        } else if (cmd == 'write' || cmd == 'load') {
            let be = tableAt(level, P)
            let rt = roundTrip(new (pg().Save)(be, cmd == 'load'), pg().Save)
            out(cmd + ' packet bytes=' + rt[1] + ' creative=' + fake(level).isCreative())
            applyAs(level, rt[0], cmd)
            tbe(level, P)
        } else if (cmd == 'screen') {
            let be = tableAt(level, P)
            let rt = roundTrip(new (pg().Change)(be, parseInt(a[4])), pg().Change)
            out('screen ' + a[4] + ' packet bytes=' + rt[1])
            applyAs(level, rt[0], 'screen ' + a[4])
        } else if (cmd == 'itemrt') {
            let be = tableAt(level, P)
            let s = be.getSchematic()
            let stack = s.toItemStack()
            let saved = stack.save(new $Compound())
            let back = $ItemStack.of(saved)
            let read = pg().Schematic.fromStack(back)
            let same = read != null && read.serializeSafeNbt().equals(s.serializeSafeNbt())
            out('itemrt stack ' + stackLine(stack) + ' -> ' + stackLine(back) + ' read ' + schLine(read) + ' safeNbtEqual=' + same
                + ' tagBytes=' + String(saved).length)
        } else if (cmd == 'toboard') {
            let be = tableAt(level, P)
            let B = pos(parseInt(a[4]), parseInt(a[5]), parseInt(a[6]))
            let sch = be.getInventory().getItem(2)
            if (sch.isEmpty()) throw 'table slot 2 is empty (run write first)'
            let p = fake(level)
            p.setGameMode($GameType.CREATIVE)
            p.setShiftKeyDown(true)
            p.setPose($Pose.CROUCHING)
            let held = sch.copy()
            p.setItemInHand($Hand.MAIN_HAND, held)
            let res = held.getItem().use(level, p, $Hand.MAIN_HAND)
            let boardStack = res.getObject()
            out('toboard use ' + stackLine(held) + ' result=' + res.getResult() + ' -> ' + stackLine(boardStack))
            p.setShiftKeyDown(false)
            p.setPose($Pose.STANDING)
            boardStackToPlace(level, boardStack, B)
            let placed = level.getBlockEntity(B)
            if (placed instanceof pg().Board) out('toboard board ' + schLine(placed.getSchematic()) + ' terminals=' + placed.terminalCount())
            else out('toboard no board entity at ' + B)
        } else if (cmd == 'bedit') {
            let board = boardAt(level, P)
            let copy = new (pg().Schematic)(board.getSchematic())
            let placed = copy.getComponent(parseInt(a[4]), parseInt(a[5]))
            if (placed == null) throw 'no part at ' + a[4] + ' ' + a[5]
            let e = placed['getEntry(net.minecraft.resources.ResourceLocation)'](rl(a[6]))
            if (e == null) throw 'no property ' + a[6]
            e.setValue(a[7])
            let name = a.slice(8).join(' ')
            let rt = roundTrip(new (pg().Save)(board, name, copy), pg().Save)
            out('bedit packet bytes=' + rt[1] + ' creative=' + fake(level).isCreative())
            applyAs(level, rt[0], 'bedit')
            let after = boardAt(level, P).getSchematic()
            let ap = after.getComponent(parseInt(a[4]), parseInt(a[5]))
            out('bedit board ' + schLine(after) + ' ' + a[6] + '=' + (ap == null ? 'missing' : ap['getEntry(net.minecraft.resources.ResourceLocation)'](rl(a[6])).stringValue()))
        } else if (cmd == 'buse') {
            boardAt(level, P)
            let p = fake(level)
            p.setItemInHand($Hand.MAIN_HAND, $ItemStack.EMPTY)
            p.setItemInHand($Hand.OFF_HAND, $ItemStack.EMPTY)
            p.setShiftKeyDown(false)
            p.setPose($Pose.STANDING)
            let hitVec = new $Vec3(P.getX() + 0.5, P.getY() + 0.0625, P.getZ() + 0.5)
            let hit = new $BHR(hitVec, $Dir.UP, P, false)
            let res = level.getBlockState(P).use(level, p, $Hand.MAIN_HAND, hit)
            out('buse creative=' + p.isCreative() + ' result=' + res + ' ' + menuLine(p))
            closeMenu(p)
        } else out('unknown subcommand ' + cmd)
    }
    ServerEvents.commandRegistry(e => {
        const C = e.commands
        e.register(C.literal('zpwg9b').requires(s => s.hasPermission(2))
            .then(C.argument('args', $Args.greedyString()).executes(ctx => {
                try {
                    run(ctx.source.getLevel(), String($Args.getString(ctx, 'args')).split(' '))
                } catch (err) { out('error ' + err) }
                out('done')
                return 1
            })))
    })
})()
