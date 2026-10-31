// Flower test world builder (feature_writeup/flower_pots.md 7). Scratch-only: copy it into
// srv/kubejs/server_scripts/zz_flower_world.js on a superflat scratch server; it is never in the pack.
//
//   /flowerworld   grid of every plant (rows) x every plant holder type (columns), built on the flat surface
//
// Rows: every plant some holder took in kubejs/exported/blocks/pot_probe.json (planters excluded), every
// #minecraft:flowers item, grass, fern, tall grass, large fern, vine. Columns: one block per holder type (variants that
// only differ by wood, colour or material are left out). Each cell is a fresh holder clicked by a survival fake player
// with the plant, so a cell is full only when the live game accepts that plant there; anything a refused click
// places next to the holder is removed again. Axis: the plant itself on grass at the start of each row (forced in
// place when it cannot be planted on grass; an item frame for items with no visible block), a sign with its name
// two blocks further out. Header signs name each column. Rows come in sections of SECTION, side by side along +x.
(() => {
    const $FWFake = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $FWProfile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $FWUUID = Java.loadClass('java.util.UUID')
    const $FWDir = Java.loadClass('net.minecraft.core.Direction')
    const $FWPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $FWVec3 = Java.loadClass('net.minecraft.world.phys.Vec3')
    const $FWAABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $FWHit = Java.loadClass('net.minecraft.world.phys.BlockHitResult')
    const $FWHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $FWItemEntity = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $FWBlocks = Java.loadClass('net.minecraft.world.level.block.Blocks')
    const $FWBlockItem = Java.loadClass('net.minecraft.world.item.BlockItem')
    const $FWProps = Java.loadClass('net.minecraft.world.level.block.state.properties.BlockStateProperties')
    const $FWHalf = Java.loadClass('net.minecraft.world.level.block.state.properties.DoubleBlockHalf')
    const $FWReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $FWRL = Java.loadClass('net.minecraft.resources.ResourceLocation')

    const Y = -60          // superflat surface (grass at -61)
    const SECTION = 48     // rows per section
    const STEP = 2         // block spacing of rows and columns
    const TOP = [[0.25, 0.5], [0.75, 0.5], [0.5, 0.25], [0.5, 0.75]]
    const SIDE = [[0.25, 0.5], [0.75, 0.5], [0.5, 0.3], [0.5, 0.7]]
    // kind soil: the plant goes on top. heldPot: Supplementaries' hanging pot starts holding an empty vanilla pot
    // (a player makes it by clicking a ceiling with one); a bare setblock holds air and refuses every plant.
    const HOLDERS = [
        { id: 'minecraft:flower_pot', face: 'up', pts: TOP },
        { id: 'framedblocks:framed_flower_pot', face: 'up', pts: TOP },
        { id: 'supplementaries:hanging_flower_pot', face: 'north', pts: SIDE, ceiling: true, heldPot: true },
        { id: 'beautify:hanging_pot', face: 'up', pts: TOP, ceiling: true },
        { id: 'beautify:botanist_workbench[facing=south]', face: 'up', pts: TOP },
        { id: 'supplementaries:flower_box[facing=north,floor=true]', face: 'up', pts: [[0.17, 0.5], [0.5, 0.5], [0.83, 0.5], [0.5, 0.2]] },
        { id: 'another_furniture:oak_planter_box[facing=north]', face: 'up', pts: TOP },
        { id: 'vinery:flower_pot', face: 'up', pts: TOP },
        { id: 'vinery:flower_box', face: 'up', pts: TOP },
        { id: 'floralench:large_pot', face: 'up', pts: TOP },
        { id: 'twigs:silt_pot[filled=true]', kind: 'soil', face: 'up', pts: [[0.5, 0.5]] },
        { id: 'handcrafted:terracotta_thin_pot', face: 'up', pts: TOP },
        { id: 'handcrafted:terracotta_medium_pot', face: 'up', pts: TOP },
        { id: 'handcrafted:terracotta_thick_pot', face: 'up', pts: TOP },
        { id: 'handcrafted:terracotta_wide_pot', face: 'up', pts: TOP },
        { id: 'supplementaries:planter', kind: 'soil', face: 'up', pts: [[0.5, 0.5]] },
        { id: 'supplementaries:planter_rich', kind: 'soil', face: 'up', pts: [[0.5, 0.5]] },
    ]
    const W = STEP * HOLDERS.length + 8   // section width: sign, axis, columns, gap

    function log(s) { console.info('[flowerworld] ' + s) }
    function rl(id) { return new $FWRL(id) }
    function key(reg, o) { try { return String(reg.getKey(o)) } catch (e) { return '?' } }
    function pos(x, y, z) { return new $FWPos(x, y, z) }
    function baseId(spec) { let i = spec.indexOf('['); return i >= 0 ? spec.substring(0, i) : spec }
    function setSpec(level, p, spec) {
        let i = spec.indexOf('[')
        let props = {}
        if (i >= 0) spec.substring(i + 1, spec.length - 1).split(',').forEach(kv => { let a = kv.split('='); props[a[0]] = a[1] })
        level.getBlock(p).set(baseId(spec), props)
    }
    function setState(level, p, st) { level.setBlock(p, st, 18) }   // clients + known shape: no neighbour checks
    function sig(level, p) {
        let st = String(level.getBlockState(p))
        let be = level.getBlockEntity(p)
        let nbt = ''
        if (be != null) { try { nbt = String(be.saveWithoutMetadata()) } catch (e) { nbt = 'nbt?' } }
        return st + ' ' + nbt
    }
    function facePoint(p, D, u, v) {
        let x = p.getX(), y = p.getY(), z = p.getZ()
        if (D == 'north') return new $FWVec3(x + u, y + v, z)
        return new $FWVec3(x + u, y + 1, z + v)
    }
    function click(level, fp, at, face, u, v, item) {
        fp.setItemInHand($FWHand.MAIN_HAND, Item.of(item, 64))
        let hit = new $FWHit(facePoint(at, face, u, v), $FWDir.byName(face), at, false)
        return guardThrowable(() => fp.gameMode.useItemOn(fp, level, fp.getMainHandItem(), $FWHand.MAIN_HAND, hit))
    }
    function sign(server, p, rot, l1, l2) {
        let t = s => "'" + JSON.stringify(s).replace(/'/g, '') + "'"
        server.runCommandSilent('setblock ' + p.getX() + ' ' + p.getY() + ' ' + p.getZ() + ' minecraft:oak_sign[rotation=' + rot + ']'
            + '{Text1:' + t(l1) + ',Text2:' + t(l2) + '}')
    }
    function frame(server, p, item) {
        server.runCommandSilent('summon minecraft:glow_item_frame ' + p.getX() + ' ' + p.getY() + ' ' + p.getZ()
            + ' {Facing:1b,Fixed:1b,Invulnerable:1b,Item:{id:"' + item + '",Count:1b}}')
    }
    function killItems(level, x0, z0, x1, z1) {
        let l = level.getEntitiesOfClass($FWItemEntity, new $FWAABB(x0, Y - 2, z0, x1, Y + 6, z1))
        for (let i = 0; i < l.size(); i++) l.get(i).discard()
    }

    function rowList() {
        let set = {}
        try {
            let d = JSON.parse(String(JsonIO.readString('kubejs/exported/blocks/pot_probe.json')))
            Object.keys(d.holders).forEach(h => {
                if (h.indexOf('supplementaries:planter') == 0) return
                let rows = d.holders[h].rows
                Object.keys(rows).forEach(i => { if (rows[i].n > 0 || rows[i].survive === true) set[i] = 1 })
            })
        } catch (e) { log('no pot_probe.json: ' + e) }
        Ingredient.of('#minecraft:flowers').getItemIds().forEach(i => { set[String(i)] = 1 })
        ;['minecraft:grass', 'minecraft:fern', 'minecraft:tall_grass', 'minecraft:large_fern', 'minecraft:vine'].forEach(i => { set[i] = 1 })
        let ids = Object.keys(set).filter(i => key($FWReg.ITEMS, $FWReg.ITEMS.getValue(rl(i))) == i)
        ids.sort((a, b) => {
            let na = a.split(':')[0], nb = b.split(':')[0]
            if (na != nb) { if (na == 'minecraft') return -1; if (nb == 'minecraft') return 1; return na < nb ? -1 : 1 }
            return a < b ? -1 : a > b ? 1 : 0
        })
        return ids
    }

    // the plant on grass: planted by a click when it can be, else forced (both halves of a double plant); a block
    // with no outline (vine, lichen with no faces) or a plain item becomes an item frame on the grass
    function axisPlant(server, level, fp, x, z, id) {
        let at = pos(x, Y, z), ground = pos(x, Y - 1, z)
        click(level, fp, ground, 'up', 0.5, 0.5, id)
        if (!level.getBlockState(at).isAir()) return 'planted'
        let it = $FWReg.ITEMS.getValue(rl(id))
        if (it instanceof $FWBlockItem) {
            let st = it.getBlock().defaultBlockState()
            if (!st.getShape(level, at).isEmpty()) {
                if (st.hasProperty($FWProps.DOUBLE_BLOCK_HALF)) {
                    setState(level, at, st.setValue($FWProps.DOUBLE_BLOCK_HALF, $FWHalf.LOWER))
                    setState(level, at.above(), st.setValue($FWProps.DOUBLE_BLOCK_HALF, $FWHalf.UPPER))
                } else setState(level, at, st)
                return 'forced'
            }
        }
        frame(server, at, id)
        return 'frame'
    }

    // one holder x one plant; returns true when the holder took it
    function cell(level, fp, H, x, z, id) {
        let at = pos(x, Y, z)
        let guard = []   // spots a refused click may place into: above (non-soil) and the clicked side
        if (H.ceiling) setState(level, at.above(), $FWBlocks.GLASS.defaultBlockState())
        setSpec(level, at, H.id)
        if (key($FWReg.BLOCKS, level.getBlockState(at).getBlock()) != baseId(H.id)) { log('holder did not place: ' + H.id); return false }
        if (H.heldPot) {
            let be = level.getBlockEntity(at)
            if (be != null) { be.setHeldBlock($FWBlocks.FLOWER_POT.defaultBlockState()); be.setChanged() }
        }
        if (H.kind != 'soil' && !H.ceiling) guard.push(at.above())
        if (H.face == 'north') guard.push(at.north())
        let keep = guard.map(g => level.getBlockState(g))
        let before = sig(level, at), changed = false
        for (let k = 0; k < H.pts.length; k++) {
            click(level, fp, at, H.face, H.pts[k][0], H.pts[k][1], id)
            guard.forEach((g, gi) => { if (String(level.getBlockState(g)) != String(keep[gi])) setState(level, g, keep[gi]) })
            if (key($FWReg.BLOCKS, level.getBlockState(at).getBlock()) == 'minecraft:air') { setSpec(level, at, H.id); break }
            if (sig(level, at) != before) changed = true
        }
        if (H.kind == 'soil') {
            let up = level.getBlockState(at.above())
            if (up.isAir()) return false
            if (!up.canSurvive(level, at.above())) {
                setState(level, at.above(), $FWBlocks.AIR.defaultBlockState())
                if (up.hasProperty($FWProps.DOUBLE_BLOCK_HALF)) setState(level, at.above().above(), $FWBlocks.AIR.defaultBlockState())
                return false
            }
            return true
        }
        return changed
    }

    function build(server, level) {
        let ids = rowList()
        let sections = Math.ceil(ids.length / SECTION)
        let x1 = sections * W, z1 = SECTION * STEP + 2
        server.runCommandSilent('forceload add -6 -6 ' + x1 + ' ' + z1)
        ;['doDaylightCycle false', 'doWeatherCycle false', 'randomTickSpeed 0', 'doMobSpawning false', 'doFireTick false',
            'mobGriefing false', 'doTraderSpawning false', 'doPatrolSpawning false', 'doInsomnia false'].forEach(g => server.runCommandSilent('gamerule ' + g))
        server.runCommandSilent('time set 6000')
        server.runCommandSilent('weather clear')
        server.runCommandSilent('setworldspawn -5 ' + Y + ' -5')
        log(ids.length + ' rows x ' + HOLDERS.length + ' holders, ' + sections + ' sections, area 0..' + x1 + ' x 0..' + z1)
        let fp = $FWFake.get(level, new $FWProfile($FWUUID.randomUUID(), 'flowerworld'))
        fp.getInventory().clearContent()
        let counts = HOLDERS.map(() => 0), axis = { planted: 0, forced: 0, frame: 0 }
        let i = 0
        let step = () => {
            if (i == 0) {
                for (let s = 0; s < sections; s++) HOLDERS.forEach((H, c) => {
                    let b = $FWReg.BLOCKS.getValue(rl(baseId(H.id)))
                    sign(server, pos(s * W + STEP * (c + 1), Y, -STEP), 8, { translate: b.getDescriptionId() }, { text: baseId(H.id).split(':')[0] })
                })
            }
            for (let k = 0; k < 2 && i < ids.length; k++, i++) {
                let id = ids[i]
                let xb = Math.floor(i / SECTION) * W, z = (i % SECTION) * STEP
                let it = $FWReg.ITEMS.getValue(rl(id))
                sign(server, pos(xb - STEP, Y, z), 4, { translate: it.getDescriptionId() }, { text: id.split(':')[0] })
                axis[axisPlant(server, level, fp, xb, z, id)]++
                HOLDERS.forEach((H, c) => { if (cell(level, fp, H, xb + STEP * (c + 1), z, id)) counts[c]++ })
                killItems(level, xb - 4, z - 1, xb + W, z + 2)
            }
            if (i < ids.length) { server.scheduleInTicks(1, step); return }
            fp.discard()
            HOLDERS.forEach((H, c) => log('holder ' + H.id + ': ' + counts[c] + ' of ' + ids.length))
            log('axis planted ' + axis.planted + ', forced ' + axis.forced + ', item frames ' + axis.frame)
            server.runCommandSilent('forceload remove all')
            log('DONE')
        }
        server.scheduleInTicks(40, step)
    }

    ServerEvents.commandRegistry(event => {
        const Commands = Java.loadClass('net.minecraft.commands.Commands')
        event.register(
            Commands.literal('flowerworld')
                .requires(src => src.hasPermission(2))
                .executes(ctx => {
                    build(ctx.source.getServer(), ctx.source.getServer().overworld())
                    return 1
                }))
    })
})()
