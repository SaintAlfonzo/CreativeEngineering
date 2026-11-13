// Scratch-only probe for the Create: Power Grid port, phase P9c rows (SQ-PWG-76 onward, plan_power_grid.md P9c: acid
// fluid, block and bucket, the etching chain, the incomplete circuit, the arm and deployer mixins, the battery
// assembly). Copy into a scratch server's kubejs/server_scripts as zz_pwg_p9c_probe.js next to the P2 to P9b probes
// (zpwg9: board; zpwg9b: fp); never into the pack. Uses its own Forge fake player ("[zpwg9c]"). No chunk loads here:
// `forceload add` the area first (Rhino deadlock). Builds real Create machines (creative motors, belts, deployers,
// press, spout, basins on creative blaze burners, mechanical arms) so the rows run the survival chain.
//   check                         acid fluid / flowing / block / bucket, the four P9c items and their classes, tags,
//                                 the P9c recipes, RecipeApplierMixin unit check (etching recipe on a tagged stack)
//   fp X Y Z                      moves the fake player to stand at X Y Z looking straight down
//   acid X Y Z                    sets an acid source block
//   count X Y Z R                 acid source and flowing blocks within R (layers Y-1 to Y+1), farthest flow distance
//   pick X Y Z / pour X Y Z       fake player at X Y+1 Z looking down: uses an empty bucket on the acid at X Y Z /
//                                 uses an acid bucket on the block below X Y Z (acid should appear at X Y Z)
//   pit X Y Z                     glass pen around X Y Z (two high), acid source at X Y Z, an AI villager inside;
//                                 hurt and death of entities in pens print "[zpwg9c] hurt ..." / "death ..."
//   depot X Y Z [ITEM N]          places a depot (optionally with ITEM x N in it)
//   put X Y Z ITEM N              inserts ITEM x N into the item handler at X Y Z (depot, basin, deployer)
//   schem X Y Z BX BY BZ [hand]   a circuit_schematic carrying the design of the board at B, into the handler at X Y Z
//                                 or, with "hand", into the main hand of the deployer at X Y Z
//   inc X Y Z BX BY BZ            an incomplete_circuit carrying the board's design, into the handler at X Y Z
//   show X Y Z                    item handler slots (Schematic components, SequencedAssembly step, Assembly
//                                 inserted/total), fluid tanks, deployer held item, belt items (position, locks)
//   deployer X Y Z ITEM N         deployer facing down at X Y Z, creative motor on its shaft side, ITEM x N in hand
//                                 (ITEM "none": empty hand)
//   line X Y Z unetched|battery   belt X..X+9 at Z (shafts, creative motor at X Y Z-1), end depot at X+10; machines
//                                 two above the belt: unetched = deployers at X+2 (empty_circuit), X+4 (create:dough),
//                                 X+6 (create:copper_sheet), press at X+8; battery = deployers at X+2
//                                 (create:copper_sheet), X+4 (powergrid:zinc_sheet), spout at X+6 with 1000 mB acid
//   lineck X Y Z                  speeds of the belt and machines, belt direction (flips the belt motor if it runs west)
//   feed X Y Z ITEM [BX BY BZ]    puts ITEM (or, with a board, a circuit_schematic / incomplete_circuit of its design:
//                                 ITEM "schem" / "inc") on the belt start at X Y Z
//   loop X Y Z on|off             every 5 ticks: an item on the line's end depot (X+10) with a SequencedAssembly tag is
//                                 put back on the belt start; any other item ends the loop ("loop done <item>")
//   basin X Y Z                   creative blaze burner (kindled) at Y-1, basin at Y, mixer at Y+2, motor at Y+3
//   bfill X Y Z FLUID MB          fills the fluid handler at X Y Z (basin, spout)
//   take SX SY SZ TX TY TZ        moves every extractable stack from the handler at S into the handler at T
//   arm AX AY AZ OX OY OZ IX IY IZ [JX JY JZ]   mechanical arm at A (motor under it, 64 RPM): DEPOSIT at O (depot
//                                 or belt), TAKE at I (and J)
//   arm2 AX AY AZ O1.. O2.. I.. J..   as arm with two DEPOSIT points in this order (B25: a depot first, then a belt)
//   armunit X Y Z ITEM N          ArmInteractionPoint.create at the depot X Y Z, insert(ITEM x N, simulate): the
//                                 ArmInteractionPointMixin unit check (remainder N-1 when the depot holds an
//                                 incomplete circuit that needs ITEM)
//   same X Y Z BX BY BZ           compares the Schematic of the stack at X Y Z with the board's safe NBT
// Output: "[zpwg9c] ..." lines in logs/latest.log, then "[zpwg9c] done".
(() => {
    const $Args = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $FR = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $RL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $Pos = Java.loadClass('net.minecraft.core.BlockPos')
    const $Dir = Java.loadClass('net.minecraft.core.Direction')
    const $Hand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $Fake = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $Profile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $UUID = Java.loadClass('java.util.UUID')
    const $Compound = Java.loadClass('net.minecraft.nbt.CompoundTag')
    const $ListTag = Java.loadClass('net.minecraft.nbt.ListTag')
    const $ItemStack = Java.loadClass('net.minecraft.world.item.ItemStack')
    const $ItemEntity = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $AABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $Caps = Java.loadClass('net.minecraftforge.common.capabilities.ForgeCapabilities')
    const $FluidUtil = Java.loadClass('net.minecraftforge.fluids.FluidUtil')
    const $TagKey = Java.loadClass('net.minecraft.tags.TagKey')
    const $Registry = Java.loadClass('net.minecraft.core.Registry')
    const $BEB = Java.loadClass('com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour')
    const $Scroll = Java.loadClass('com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour')
    const $DBI = Java.loadClass('com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour')
    const $KBE = Java.loadClass('com.simibubi.create.content.kinetics.base.KineticBlockEntity')
    const $Belt = Java.loadClass('com.simibubi.create.content.kinetics.belt.BeltBlockEntity')
    const $BeltItem = Java.loadClass('com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem')
    const $Deployer = Java.loadClass('com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity')
    const $ArmPoint = Java.loadClass('com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint')
    const $Applier = Java.loadClass('com.simibubi.create.foundation.recipe.RecipeApplier')
    const PROFILE = new $Profile($UUID.fromString('5a7c0e57-0b0e-4a11-9d51-70e9a1c0f00c'), '[zpwg9c]')
    const P9C_ITEMS = ['acid_bucket', 'empty_circuit', 'unetched_circuit', 'incomplete_unetched_circuit', 'incomplete_circuit']
    const P9C_RECIPES = ['mixing/acid', 'mixing/etched_circuit_board', 'sequenced_assembly/unetched_circuit_board',
        'cutting/empty_circuit_slabs', 'deploying/schematic_copy', 'sequenced_assembly/battery']
    const P9D_RECIPES = ['sequenced_assembly/electrical_gizmo', 'deploying/electrobaton_boosting', 'deploying/portable_drill_boosting']
    let PG = null
    function pg() {
        if (PG === null) PG = {
            Board: Java.loadClass('org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity'),
            Incomplete: Java.loadClass('org.patryk3211.powergrid.circuits.circuitboard.IncompleteCircuitItem'),
            Schematic: Java.loadClass('org.patryk3211.powergrid.circuits.schematic.CircuitSchematic'),
            Damage: Java.loadClass('org.patryk3211.powergrid.collections.ModdedDamageTypes'),
            SeqItem: Java.loadClass('com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem'),
        }
        return PG
    }
    const $FRtag = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    // ItemStack.is(TagKey) is ambiguous in Rhino (r1113 pwg16): ask Forge's tag manager instead
    function fluidInTag(f, tagKey) {
        try { return $FRtag.FLUIDS.tags().getTag(tagKey).contains(f) } catch (e1) { return 'error ' + e1 }
    }
    function inTag(it, tagKey) {
        try { return $FRtag.ITEMS.tags().getTag(tagKey).contains(it) } catch (e1) { return 'error ' + e1 }
    }
    function out(s) { console.info('[zpwg9c] ' + s) }
    // ModdedFluids.acid() is not reachable from Rhino (r1113 pwg15: "no public instance field or method named acid"):
    // the registry by id instead
    function acidFluid() { return $FR.FLUIDS.getValue(new $RL('powergrid', 'acid')) }
    function acidFlowing() { return $FR.FLUIDS.getValue(new $RL('powergrid', 'flowing_acid')) }
    function lvlOf(ent) { try { return ent.getLevel() } catch (e1) { return ent.level } }
    function pos(x, y, z) { return new $Pos(x, y, z) }
    function rl(s) { return s.indexOf(':') >= 0 ? new $RL(s) : new $RL('powergrid', s) }
    function fmt(x) { return (Math.round(x * 1e3) / 1e3) }
    function block(id) { return $FR.BLOCKS.getValue(rl(id)) }
    function item(id) { return $FR.ITEMS.getValue(rl(id)) }
    function idOf(stack) { return String($FR.ITEMS.getKey(stack.getItem())) }
    function fake(level) { return $Fake.get(level, PROFILE) }
    // game time: ServerLevel.getGameTime is not reachable from Rhino here (r1113, as in pwg_p3_probe.js)
    function gtime(level) {
        try { let g = level.getLevelData().getGameTime(); if (g != null) return g } catch (e1) {}
        try { return level.getServer().getTickCount() } catch (e2) {}
        return 'ms' + Date.now()
    }
    // ENTITY_TYPES.getKey(e.getType()) returned minecraft:pig for every entity in Rhino (r1113 pwg10b): use getEncodeId
    function eidOf(e) {
        try { let k = e.getEncodeId(); if (k != null) return String(k) } catch (e1) {}
        try { return String(e.getType().toShortString()) } catch (e2) {}
        return String($FR.ENTITY_TYPES.getKey(e.getType()))
    }
    function setProp(state, name, value) {
        let p = null
        state.getProperties().forEach(q => { if (q.getName() == name) p = q })
        if (p == null) throw 'no property ' + name
        let v = p.getValue(value)
        if (!v.isPresent()) throw 'bad value ' + name + '=' + value
        return state.setValue(p, v.get())
    }
    function place(level, P, id, props) {
        let st = block(id).defaultBlockState()
        if (props) for (let k in props) st = setProp(st, k, props[k])
        level.setBlock(P, st, 3)
        return level.getBlockState(P)
    }
    function stackTag(stack) {
        let t = stack.save(new $Compound())
        return t.contains('tag') ? t.getCompound('tag') : new $Compound()
    }
    function stackWithTag(id, n, tag) {
        let c = new $Compound()
        c.putString('id', String(rl(id)))
        c.putByte('Count', n)
        if (tag != null) c.put('tag', tag)
        return $ItemStack.of(c)
    }
    function stackLine(stack) {
        if (stack.isEmpty()) return 'empty'
        let tag = stackTag(stack)
        let s = idOf(stack) + ' x' + stack.getCount()
        if (tag.contains('Schematic')) s += ' Schematic components=' + tag.getCompound('Schematic').getList('Components', 10).size()
        if (tag.contains('SequencedAssembly')) {
            let sa = tag.getCompound('SequencedAssembly')
            s += ' SequencedAssembly step=' + sa.getInt('Step') + ' progress=' + fmt(sa.getFloat('Progress'))
        }
        if (tag.contains('Assembly')) {
            let as = tag.getCompound('Assembly')
            s += ' Assembly inserted=' + as.getInt('Inserted') + '/' + as.getInt('Total') + ' missing=' + as.getCompound('Missing')
        }
        return s
    }
    function cap(level, P, which) {
        let be = level.getBlockEntity(P)
        if (be == null) return null
        let c = be.getCapability(which, null)
        if (c == null || !c.isPresent()) return null
        return c.resolve().get()
    }
    function boardAt(level, P) {
        let be = level.getBlockEntity(P)
        if (!(be instanceof pg().Board)) throw 'no circuit board at ' + P
        return be
    }
    function schemTag(level, B) {
        let copy = new (pg().Schematic)(boardAt(level, B).getSchematic())
        return copy.serializeSafeNbt()
    }
    function designStack(level, kind, B) {
        let t = new $Compound()
        t.put('Schematic', schemTag(level, B))
        return stackWithTag(kind == 'inc' ? 'incomplete_circuit' : 'circuit_schematic', 1, t)
    }
    function insertInto(level, P, stack) {
        let h = cap(level, P, $Caps.ITEM_HANDLER)
        if (h == null) { out('put: no item handler at ' + P); return stack }
        let rest = stack.copy()
        for (let i = 0; i < h.getSlots() && !rest.isEmpty(); i++) rest = h.insertItem(i, rest, false)
        out('put ' + stackLine(stack) + ' -> ' + P.getX() + ' ' + P.getY() + ' ' + P.getZ() + ' rest=' + stackLine(rest))
        return rest
    }
    function fill(level, P, fluid, mb) {
        let h = cap(level, P, $Caps.FLUID_HANDLER)
        if (h == null) { out('fill: no fluid handler at ' + P); return }
        if (!$FR.FLUIDS.containsKey(rl(fluid))) { out('fill: unknown fluid ' + fluid); return }
        // r1113 pwg15: h.fill(FluidStack) took 0 mB (KubeJS's FluidStack type wrapper empties a Forge FluidStack on its
        // way into Java, as pwg_p6_probe.js found); write the first tank of the SmartFluidTankBehaviour NBT instead
        // (basin "InputTanks", spout "Tanks"; entries {TankContent, Level}) and reload the block entity
        let be = level.getBlockEntity(P), via = 'nbt'
        try {
            let tag = be.saveWithoutMetadata()
            let key = tag.contains('InputTanks') ? 'InputTanks' : (tag.contains('Tanks') ? 'Tanks' : null)
            if (key == null) tag.getAllKeys().forEach(k => { if (key == null && String(k).match(/Tanks$/)) key = String(k) })
            if (key == null) { out('fill: no tank list in the NBT at ' + P + ' keys=' + tag.getAllKeys()); return }
            let list = tag.getList(key, 10)
            if (list.size() == 0) { out('fill: empty tank list ' + key + ' at ' + P); return }
            let ftag = new $Compound()
            ftag.putString('FluidName', String(rl(fluid)))
            ftag.putInt('Amount', mb)
            list.getCompound(0).put('TankContent', ftag)
            be.load(tag)
            be.setChanged()
            try { be.sendData() } catch (e5) { }
            via = 'nbt ' + key
        } catch (err) { out('fill: nbt error ' + err); return }
        let n = 0
        for (let i = 0; i < h.getTanks(); i++) {
            let fs = h.getFluidInTank(i)
            if (!fs.isEmpty() && String($FR.FLUIDS.getKey(fs.getFluid())) == String(rl(fluid))) n += fs.getAmount()
        }
        out('fill ' + String(rl(fluid)) + ' ' + n + '/' + mb + ' at ' + P.getX() + ' ' + P.getY() + ' ' + P.getZ() + ' via ' + via)
    }
    function show(level, P) {
        let be = level.getBlockEntity(P)
        out('show ' + level.getBlockState(P) + ' t=' + gtime(level))
        let h = cap(level, P, $Caps.ITEM_HANDLER)
        if (h != null) {
            let parts = []
            for (let i = 0; i < h.getSlots(); i++) { let s = h.getStackInSlot(i); if (!s.isEmpty()) parts.push(i + ':' + stackLine(s)) }
            out('items ' + (parts.length ? parts.join(' | ') : 'none'))
        }
        let f = cap(level, P, $Caps.FLUID_HANDLER)
        if (f != null) {
            let parts = []
            for (let i = 0; i < f.getTanks(); i++) {
                let fs = f.getFluidInTank(i)
                parts.push(i + ':' + (fs.isEmpty() ? 'empty' : String($FR.FLUIDS.getKey(fs.getFluid())) + ' ' + fs.getAmount()))
            }
            out('tanks ' + parts.join(' '))
        }
        if (be instanceof $Deployer) {
            let p = be.getPlayer()
            out('deployer hand ' + (p == null ? 'no player yet' : stackLine(p.getMainHandItem())))
        }
        if (be instanceof $Belt) {
            let c = be.getControllerBE()
            if (c != null) {
                let parts = []
                c.getInventory().getTransportedItems().forEach(t => parts.push(stackLine(t.stack) + ' @' + fmt(t.beltPosition)
                    + (t.lockedExternally ? ' lockedExternally' : '') + (t.locked ? ' locked' : '')))
                out('belt facing=' + c.getMovementFacing() + ' speed=' + fmt(c.getSpeed()) + ' items ' + (parts.length ? parts.join(' | ') : 'none'))
            }
        }
        if (be instanceof $KBE && !(be instanceof $Belt)) out('speed=' + fmt(be.getSpeed()))
    }
    function motor(level, P, facing, rpm) {
        place(level, P, 'create:creative_motor', { facing: facing })
        let s = $BEB.get(level.getBlockEntity(P), $Scroll.TYPE)
        if (s != null) s.setValue(rpm)
    }
    // a kinetic block whose shaft faces NORTH: try each value of the given property until hasShaftTowards(NORTH)
    function placeShaftNorth(level, P, id, base, prop, values) {
        let last = null
        for (let i = 0; i < values.length; i++) {
            let props = {}
            for (let k in base) props[k] = base[k]
            props[prop] = values[i]
            let st = place(level, P, id, props)
            last = st
            if (st.getBlock().hasShaftTowards(level, P, st, $Dir.NORTH)) return st
        }
        out('warn: no ' + prop + ' value of ' + id + ' gives a shaft to the north; kept ' + last)
        return last
    }
    function deployer(level, P, itemId, n) {
        // r1113 pwg15: the motor goes first (a machine placed before its source stayed at speed 0); Create's property
        // is axis_along_first (AXIS_ALONG_FIRST_COORDINATE is the field name)
        motor(level, P.north(), 'south', 64)
        placeShaftNorth(level, P, 'create:deployer', { facing: 'down' }, 'axis_along_first', ['true', 'false'])
        HOLD.push({ pos: P, stack: itemId == null ? null : new $ItemStack(item(itemId), n), left: 40 })
        out('deployer ' + level.getBlockState(P) + (itemId == null ? '' : ' hand (next ticks) ' + itemId + ' x' + n))
    }
    // deployer fake players exist after their first tick: hand items are given by the tick handler
    let HOLD = []
    let LOOPS = []
    let PENS = []
    function handTick(level) {
        HOLD = HOLD.filter(h => {
            let be = level.getBlockEntity(h.pos)
            if (!(be instanceof $Deployer)) { out('hold: no deployer at ' + h.pos); return false }
            let p = be.getPlayer()
            if (p == null) { h.left--; if (h.left <= 0) out('hold: deployer player never appeared at ' + h.pos); return h.left > 0 }
            if (h.stack != null) {
                // the deployer's item handler sets the hand and the synced held item (DeployerItemHandler.set)
                let ih = cap(level, h.pos, $Caps.ITEM_HANDLER)
                let rest = ih == null ? h.stack : ih.insertItem(0, h.stack.copy(), false)
                if (!rest.isEmpty()) p.setItemInHand($Hand.MAIN_HAND, h.stack)
            }
            out('hold deployer ' + h.pos.getX() + ' ' + h.pos.getY() + ' ' + h.pos.getZ() + ' hand ' + stackLine(p.getMainHandItem()))
            return false
        })
    }
    function feedBelt(level, P, stack) {
        let be = level.getBlockEntity(P)
        let rest = stack
        if (be != null) {
            let dbi = $BEB.get(be, $DBI.TYPE)
            if (dbi != null) rest = dbi.handleInsertion(stack.copy(), $Dir.DOWN, false)
        }
        if (!rest.isEmpty()) {
            let e = new $ItemEntity(level, P.getX() + 0.5, P.getY() + 0.9, P.getZ() + 0.5, rest)
            e.setDeltaMovement(0, 0, 0)
            level.addFreshEntity(e)
            out('feed ' + stackLine(stack) + ' dropped as an item entity (belt input refused)')
        } else out('feed ' + stackLine(stack) + ' inserted on the belt at ' + P.getX() + ' ' + P.getY() + ' ' + P.getZ())
    }
    function loopTick(level) {
        LOOPS = LOOPS.filter(L => {
            L.wait--
            if (L.wait > 0) return true
            L.wait = 5
            L.left -= 5
            let h = cap(level, L.end, $Caps.ITEM_HANDLER)
            if (h == null) { out('loop: no end depot'); return false }
            let s = h.getStackInSlot(0)
            if (!s.isEmpty()) {
                if (stackTag(s).contains('SequencedAssembly')) {
                    let got = h.extractItem(0, 64, false)
                    L.cycles++
                    out('loop ' + L.cycles + ' back to start: ' + stackLine(got) + ' t=' + gtime(level))
                    feedBelt(level, L.start, got)
                } else {
                    out('loop done ' + stackLine(s) + ' cycles=' + L.cycles + ' t=' + gtime(level))
                    return false
                }
            }
            if (L.left <= 0) { out('loop timeout (end depot ' + stackLine(s) + ')'); return false }
            return true
        })
    }
    ServerEvents.tick(e => {
        if (HOLD.length == 0 && LOOPS.length == 0) return
        try {
            let level = e.server.overworld()
            handTick(level)
            loopTick(level)
        } catch (err) { out('tick error ' + err); HOLD = []; LOOPS = [] }
    })
    function inPen(ent) {
        for (let i = 0; i < PENS.length; i++) {
            let p = PENS[i]
            if (Math.abs(ent.getX() - (p.getX() + 0.5)) < 2 && Math.abs(ent.getZ() - (p.getZ() + 0.5)) < 2 && Math.abs(ent.getY() - p.getY()) < 3) return true
        }
        return false
    }
    function srcLine(src) {
        let acid = false
        try { let a = pg().Damage.ACID.simpleDamageSource(); acid = src === a || src.equals(a) } catch (e1) { }
        let mid
        try { mid = src.getMsgId() } catch (e2) { try { mid = src.msgId } catch (e3) { mid = String(src) } }
        return 'source=' + mid + ' acidSource=' + acid
    }
    EntityEvents.hurt(e => {
        if (PENS.length == 0) return
        try {
            let ent = e.getEntity()
            if (!inPen(ent)) return
            out('hurt ' + eidOf(ent) + ' ' + srcLine(e.getSource()) + ' amount=' + fmt(e.getDamage())
                + ' health=' + fmt(ent.getHealth()) + ' t=' + gtime(lvlOf(ent)))
        } catch (err) { out('hurt watcher error ' + err) }
    })
    EntityEvents.death(e => {
        if (PENS.length == 0) return
        try {
            let ent = e.getEntity()
            if (!inPen(ent)) return
            out('death ' + eidOf(ent) + ' ' + srcLine(e.getSource()) + ' t=' + gtime(lvlOf(ent)))
        } catch (err) { out('death watcher error ' + err) }
    })
    function check(level) {
        let fl = $FR.FLUIDS
        out('fluids acid=' + fl.containsKey(rl('acid')) + ' flowing_acid=' + fl.containsKey(rl('flowing_acid'))
            + ' block acid=' + $FR.BLOCKS.containsKey(rl('acid')) + ' fluid type=' + $FR.FLUID_TYPES.get().getKey(acidFluid().getFluidType())
            + ' description=' + acidFluid().getFluidType().getDescriptionId())
        let src = acidFluid(), flow = acidFlowing()
        out('acid source=' + fl.getKey(src) + ' isSource=' + src.isSource(src.defaultFluidState()) + ' flowing=' + fl.getKey(flow)
            + ' bucket=' + $FR.ITEMS.getKey(src.getBucket()) + ' block=' + $FR.BLOCKS.getKey(src.defaultFluidState().createLegacyBlock().getBlock()))
        P9C_ITEMS.forEach(id => {
            let reg = $FR.ITEMS.containsKey(rl(id))
            let line = 'item ' + id + ' registered=' + reg
            if (reg) {
                let st = item(id).getDefaultInstance()
                line += ' maxStack=' + st.getMaxStackSize() + ' incompleteCircuitItem=' + (st.getItem() instanceof pg().Incomplete)
                    + ' sequencedAssemblyItem=' + (st.getItem() instanceof pg().SeqItem)
            }
            out(line)
        })
        let contained = $FluidUtil.getFluidContained(item('acid_bucket').getDefaultInstance())
        out('acid_bucket contains ' + (contained.isPresent() ? fl.getKey(contained.get().getFluid()) + ' ' + contained.get().getAmount() : 'nothing'))
        let holder = $TagKey.create($Registry.ITEM_REGISTRY, rl('circuit_schematic_holder'))
        out('circuit_schematic_holder ' + ['circuit_board', 'circuit_schematic', 'unetched_circuit', 'incomplete_unetched_circuit', 'incomplete_circuit']
            .map(id => id + '=' + inTag(item(id), holder)).join(' '))
        let ftag = $TagKey.create($Registry.FLUID_REGISTRY, rl('acid'))
        out('fluid tag powergrid:acid source=' + fluidInTag(src, ftag) + ' flowing=' + fluidInTag(flow, ftag))
        let rm = level.getRecipeManager()
        out('recipes P9c ' + P9C_RECIPES.map(r => r + '=' + rm.byKey(rl(r)).isPresent()).join(' '))
        out('recipes P9d (absent) ' + P9D_RECIPES.map(r => r + '=' + rm.byKey(rl(r)).isPresent()).join(' '))
        try {
            let recipe = rm.byKey(rl('mixing/etched_circuit_board')).orElse(null)
            let t = new $Compound(), sch = new $Compound()
            sch.putByte('Marker', 7)
            t.put('Schematic', sch)
            // overloaded (ItemEntity / ItemStack, same arity): call by signature
            let outs = $Applier['applyRecipeOn(net.minecraft.world.item.ItemStack,net.minecraft.world.item.crafting.Recipe)'](stackWithTag('unetched_circuit', 1, t), recipe)
            let o = outs.size() > 0 ? outs.get(0) : $ItemStack.EMPTY
            let ot = stackTag(o)
            out('mixin RecipeApplier ' + stackLine(o) + ' schematicCopied=' + (ot.contains('Schematic') && ot.getCompound('Schematic').getByte('Marker') == 7))
        } catch (err) { out('mixin RecipeApplier error ' + err) }
        try {
            let a = pg().Damage.ACID.simpleDamageSource()
            out('damage acid ' + srcLine(a))
        } catch (err) { out('damage error ' + err) }
    }
    function count(level, P, r) {
        let src = 0, flowing = 0, far = 0
        for (let dx = -r; dx <= r; dx++) for (let dz = -r; dz <= r; dz++) for (let dy = -1; dy <= 1; dy++) {
            let fs = level.getFluidState(pos(P.getX() + dx, P.getY() + dy, P.getZ() + dz))
            if (fs.isEmpty()) continue
            let key = String($FR.FLUIDS.getKey(fs.getType()))
            if (key == 'powergrid:acid') src++
            else if (key == 'powergrid:flowing_acid') flowing++
            else continue
            far = Math.max(far, Math.abs(dx) + Math.abs(dz))
        }
        out('count acid source=' + src + ' flowing=' + flowing + ' farthest=' + far + ' t=' + gtime(level))
    }
    function fpAt(level, x, y, z) {
        let p = fake(level)
        // moveTo(5 numbers) resolves to teleportTo in Rhino and throws (r1113, pwg_p7_probe.js): absMoveTo
        // absMoveTo(x, y, z, yaw, pitch) sets the rotation too; setYRot / setXRot are not reachable on FakePlayer (pwg15)
        try { p.absMoveTo(x + 0.5, y, z + 0.5, 0, 90) } catch (e1) { p.teleportTo(x + 0.5, y, z + 0.5); out('fp: absMoveTo failed, no rotation ' + e1) }
        return p
    }
    function bucketUse(level, P, held, what) {
        let p = fpAt(level, P.getX(), P.getY() + 1, P.getZ())
        let stack = item(held).getDefaultInstance()
        p.setItemInHand($Hand.MAIN_HAND, stack)
        let res = stack.getItem().use(level, p, $Hand.MAIN_HAND)
        let got = res.getObject()
        out(what + ' result=' + res.getResult() + ' -> ' + stackLine(got) + ' fluid at ' + P.getX() + ' ' + P.getY() + ' ' + P.getZ() + ' = '
            + String($FR.FLUIDS.getKey(level.getFluidState(P).getType())) + ' block=' + level.getBlockState(P))
    }
    function pit(level, P) {
        for (let dx = -1; dx <= 1; dx++) for (let dz = -1; dz <= 1; dz++) for (let dy = 0; dy <= 2; dy++) {
            if (dx == 0 && dz == 0) continue
            level.setBlock(pos(P.getX() + dx, P.getY() + dy, P.getZ() + dz), block('minecraft:glass').defaultBlockState(), 3)
        }
        level.setBlock(pos(P.getX(), P.getY() + 3, P.getZ()), block('minecraft:glass').defaultBlockState(), 3)
        level.setBlock(P, acidFluid().defaultFluidState().createLegacyBlock(), 3)
        PENS.push(P)
        let v = $FR.ENTITY_TYPES.getValue(rl('minecraft:villager')).create(level)
        try { v.absMoveTo(P.getX() + 0.5, P.getY() + 0.1, P.getZ() + 0.5, 0, 0) } catch (e1) { v.setPos(P.getX() + 0.5, P.getY() + 0.1, P.getZ() + 0.5) }
        v.setPersistenceRequired()
        level.addFreshEntity(v)
        out('pit acid at ' + P.getX() + ' ' + P.getY() + ' ' + P.getZ() + ' villager health=' + fmt(v.getHealth()) + ' t=' + gtime(level))
    }
    function line(level, X, Y, Z, kind) {
        let start = pos(X, Y, Z), end = pos(X + 9, Y, Z)
        place(level, start, 'create:shaft', { axis: 'z' })
        place(level, end, 'create:shaft', { axis: 'z' })
        $BeltItem.createBelts(level, start, end)
        motor(level, pos(X, Y, Z - 1), 'south', 32)
        place(level, pos(X + 10, Y, Z), 'create:depot')
        if (kind == 'unetched') {
            deployer(level, pos(X + 2, Y + 2, Z), 'powergrid:empty_circuit', 16)
            deployer(level, pos(X + 4, Y + 2, Z), 'create:dough', 16)
            deployer(level, pos(X + 6, Y + 2, Z), 'create:copper_sheet', 16)
            motor(level, pos(X + 8, Y + 2, Z - 1), 'south', 64)
            placeShaftNorth(level, pos(X + 8, Y + 2, Z), 'create:mechanical_press', {}, 'facing', ['north', 'south', 'east', 'west'])
        } else if (kind == 'battery') {
            deployer(level, pos(X + 2, Y + 2, Z), 'create:copper_sheet', 16)
            deployer(level, pos(X + 4, Y + 2, Z), 'powergrid:zinc_sheet', 16)
            place(level, pos(X + 6, Y + 2, Z), 'create:spout')
            fill(level, pos(X + 6, Y + 2, Z), 'powergrid:acid', 1000)
        } else if (kind != 'plain') throw 'line kind unetched|battery|plain'
        out('line ' + kind + ' belt ' + level.getBlockState(start) + ' .. ' + level.getBlockState(end))
    }
    function lineck(level, X, Y, Z) {
        let be = level.getBlockEntity(pos(X, Y, Z))
        if (!(be instanceof $Belt)) { out('lineck: no belt at start'); return }
        let c = be.getControllerBE()
        let facing = c == null ? 'none' : String(c.getMovementFacing())
        out('lineck belt facing=' + facing + ' speed=' + fmt(be.getSpeed()))
        if (facing != 'east') {
            let s = $BEB.get(level.getBlockEntity(pos(X, Y, Z - 1)), $Scroll.TYPE)
            if (s != null) { s.setValue(-s.getValue()); out('lineck flipped the belt motor to ' + s.getValue() + ' (run lineck again after 5 ticks)') }
        }
        for (let dx = 2; dx <= 8; dx += 2) {
            let m = level.getBlockEntity(pos(X + dx, Y + 2, Z))
            if (m != null) out('lineck X+' + dx + ' ' + level.getBlockState(pos(X + dx, Y + 2, Z)).getBlock() + (m instanceof $KBE ? ' speed=' + fmt(m.getSpeed()) : ''))
        }
    }
    function basin(level, X, Y, Z) {
        place(level, pos(X, Y - 1, Z), 'create:blaze_burner', { blaze: 'kindled' })
        let bb = level.getBlockEntity(pos(X, Y - 1, Z))
        let t = bb.saveWithFullMetadata()
        t.putBoolean('isCreative', true)
        bb.load(t)
        place(level, pos(X, Y, Z), 'create:basin')
        motor(level, pos(X, Y + 3, Z), 'down', 64)
        place(level, pos(X, Y + 2, Z), 'create:mechanical_mixer')
        out('basin at ' + X + ' ' + Y + ' ' + Z + ' burner ' + level.getBlockState(pos(X, Y - 1, Z)) + ' creative=' + bb.isCreative())
    }
    function take(level, S, T) {
        let hs = cap(level, S, $Caps.ITEM_HANDLER)
        if (hs == null) { out('take: no handler at source'); return }
        let moved = 0
        for (let i = 0; i < hs.getSlots(); i++) {
            let got = hs.extractItem(i, 64, false)
            if (got.isEmpty()) continue
            let rest = insertInto(level, T, got)
            moved++
            if (!rest.isEmpty()) { out('take: target refused ' + stackLine(rest) + ', dropped'); level.addFreshEntity(new $ItemEntity(level, T.getX() + 0.5, T.getY() + 1.2, T.getZ() + 0.5, rest)) }
        }
        out('take moved ' + moved + ' stack(s)')
    }
    function arm(level, A, outs, ins) {
        motor(level, A.below(), 'up', 64)
        place(level, A, 'create:mechanical_arm')
        let list = new $ListTag()
        let addPoint = (P, mode) => {
            let pt = $ArmPoint.create(level, P, level.getBlockState(P))
            if (pt == null) { out('arm: no interaction point type for ' + level.getBlockState(P)); return }
            let t = pt.serialize(A)
            t.putString('Mode', mode)
            list.add(t)
            out('arm point ' + t)
        }
        ins.forEach(P => addPoint(P, 'TAKE'))
        outs.forEach(P => addPoint(P, 'DEPOSIT'))
        let be = level.getBlockEntity(A)
        let tag = be.saveWithFullMetadata()
        tag.put('InteractionPoints', list)
        be.load(tag)
        out('arm at ' + A.getX() + ' ' + A.getY() + ' ' + A.getZ() + ' points=' + list.size())
    }
    function run(level, a) {
        let cmd = a[0]
        if (cmd == 'check') { check(level); return }
        let X = parseInt(a[1]), Y = parseInt(a[2]), Z = parseInt(a[3])
        let P = pos(X, Y, Z)
        if (cmd == 'fp') { fpAt(level, X, Y, Z); out('fp at ' + X + ' ' + Y + ' ' + Z) }
        else if (cmd == 'acid') { level.setBlock(P, acidFluid().defaultFluidState().createLegacyBlock(), 3); out('acid ' + level.getBlockState(P) + ' t=' + gtime(level)) }
        else if (cmd == 'count') count(level, P, parseInt(a[4]))
        else if (cmd == 'pick') bucketUse(level, P, 'minecraft:bucket', 'pick')
        else if (cmd == 'pour') bucketUse(level, P, 'powergrid:acid_bucket', 'pour')
        else if (cmd == 'pit') pit(level, P)
        else if (cmd == 'depot') {
            place(level, P, 'create:depot')
            if (a[4]) insertInto(level, P, new $ItemStack(item(a[4]), parseInt(a[5] || '1')))
            out('depot ' + level.getBlockState(P))
        } else if (cmd == 'put') insertInto(level, P, new $ItemStack(item(a[4]), parseInt(a[5] || '1')))
        else if (cmd == 'schem' || cmd == 'inc') {
            let B = pos(parseInt(a[4]), parseInt(a[5]), parseInt(a[6]))
            let st = designStack(level, cmd, B)
            if (a[7] == 'hand') HOLD.push({ pos: P, stack: st, left: 40 })
            else insertInto(level, P, st)
        } else if (cmd == 'show') show(level, P)
        else if (cmd == 'deployer') deployer(level, P, (a[4] == null || a[4] == 'none') ? null : a[4], parseInt(a[5] || '16'))
        else if (cmd == 'line') line(level, X, Y, Z, a[4])
        else if (cmd == 'lineck') lineck(level, X, Y, Z)
        else if (cmd == 'feed') {
            let st
            if (a.length >= 8) st = designStack(level, a[4], pos(parseInt(a[5]), parseInt(a[6]), parseInt(a[7])))
            else st = new $ItemStack(item(a[4]), 1)
            feedBelt(level, P, st)
        } else if (cmd == 'loop') {
            LOOPS = LOOPS.filter(L => !(L.start.getX() == X && L.start.getY() == Y && L.start.getZ() == Z))
            if (a[4] != 'off') LOOPS.push({ start: P, end: pos(X + 10, Y, Z), wait: 5, left: 20 * 600, cycles: 0 })
            out('loop ' + (a[4] != 'off' ? 'on' : 'off') + ' at ' + X + ' ' + Y + ' ' + Z)
        } else if (cmd == 'basin') basin(level, X, Y, Z)
        else if (cmd == 'bfill') fill(level, P, a[4], parseInt(a[5]))
        else if (cmd == 'take') take(level, P, pos(parseInt(a[4]), parseInt(a[5]), parseInt(a[6])))
        else if (cmd == 'arm') {
            let O = pos(parseInt(a[4]), parseInt(a[5]), parseInt(a[6]))
            let ins = [pos(parseInt(a[7]), parseInt(a[8]), parseInt(a[9]))]
            if (a.length >= 13) ins.push(pos(parseInt(a[10]), parseInt(a[11]), parseInt(a[12])))
            arm(level, P, [O], ins)
        } else if (cmd == 'arm2') {
            let n = a.slice(4).map(v => parseInt(v))
            arm(level, P, [pos(n[0], n[1], n[2]), pos(n[3], n[4], n[5])], [pos(n[6], n[7], n[8]), pos(n[9], n[10], n[11])])
        } else if (cmd == 'armunit') {
            let pt = $ArmPoint.create(level, P, level.getBlockState(P))
            if (pt == null) { out('armunit: no point type at ' + P); return }
            let st = new $ItemStack(item(a[4]), parseInt(a[5] || '2'))
            let rest = pt.insert(st.copy(), true)
            out('armunit ' + stackLine(st) + ' simulate remainder=' + stackLine(rest) + ' depot ' + stackLine(cap(level, P, $Caps.ITEM_HANDLER).getStackInSlot(0)))
        } else if (cmd == 'same') {
            let B = pos(parseInt(a[4]), parseInt(a[5]), parseInt(a[6]))
            let h = cap(level, P, $Caps.ITEM_HANDLER)
            let s = h == null ? $ItemStack.EMPTY : h.getStackInSlot(0)
            let t = stackTag(s)
            out('same ' + stackLine(s) + ' design=' + (t.contains('Schematic') && t.getCompound('Schematic').equals(schemTag(level, B))))
        } else out('unknown subcommand ' + cmd)
    }
    ServerEvents.commandRegistry(e => {
        const C = e.commands
        e.register(C.literal('zpwg9c').requires(s => s.hasPermission(2))
            .then(C.argument('args', $Args.greedyString()).executes(ctx => {
                try {
                    run(ctx.source.getLevel(), String($Args.getString(ctx, 'args')).split(' '))
                } catch (err) { out('error ' + err) }
                out('done')
                return 1
            })))
    })
})()
