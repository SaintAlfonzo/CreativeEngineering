// Flower Test world builder (feature_writeup/flower_pots.md 7). Ships with the pack.
//
//   World type "Flower Test" (patcherMod data/creativeengineering/worldgen/world_preset/flower_test.json): on the
//   first server start of such a world (flat stack bedrock, stone, dirt, grass) the table below builds itself.
//   /flowerworld   (op) builds it again in the current world
//
// One table along +z: every plant (rows) x every plant holder type (columns; Handcrafted's pots are decor, not
// holders, and stay out). Rows: plants from
// kubejs/exported/blocks/pot_probe.json when it exists (dev only), every bush-type block item, cactus, bamboo,
// hanging roots, every #minecraft:flowers item, grass, fern, tall grass, large fern, vine; never melon, pumpkin or
// moss blocks. A row stays only when some
// holder other than the two Supplementaries planters took the plant (#minecraft:flowers and the grass row always
// stay). Columns: one block per holder type (variants that only differ by wood, colour or material are left out; Floral
// Enchantment's large pot is hidden in the pack and left out).
// Each cell is a fresh holder clicked by a survival fake player with the plant, so a cell is full only when the live
// game accepts that plant there; anything a refused click places next to the holder is removed again. Hanging
// holders sit one block up under glass so they do not rest on the ground. Axis: the plant itself on grass at the
// start of each row (forced in place when it cannot be planted on grass; an item frame for items with no visible
// block), a sign with its name two blocks further out. Header signs name each column.
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
    const $FWBush = Java.loadClass('net.minecraft.world.level.block.BushBlock')
    const $FWFlat = Java.loadClass('net.minecraft.world.level.levelgen.FlatLevelSource')

    const Y = -60          // superflat surface (grass at -61)
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
        { id: 'twigs:silt_pot', kind: 'soil', face: 'up', pts: [[0.5, 0.5]], fill: 'minecraft:rooted_dirt' },
        { id: 'supplementaries:planter', kind: 'soil', face: 'up', pts: [[0.5, 0.5]] },
        { id: 'supplementaries:planter_rich', kind: 'soil', face: 'up', pts: [[0.5, 0.5]] },
    ]
    const W = STEP * HOLDERS.length + 2   // table width past the axis
    const PRESET_LAYERS = 'minecraft:bedrock*1,minecraft:stone*1,minecraft:dirt*1,minecraft:grass_block*1'
    const KEEP = ['minecraft:grass', 'minecraft:fern', 'minecraft:tall_grass', 'minecraft:large_fern', 'minecraft:vine']
    const EXTRA = ['minecraft:cactus', 'minecraft:bamboo', 'minecraft:hanging_roots']
    // whole blocks some box takes as a plant; not plants, left out of the table
    const EXCLUDE = ['minecraft:melon', 'minecraft:pumpkin', 'minecraft:carved_pumpkin', 'minecraft:jack_o_lantern',
        'minecraft:moss_block', 'minecraft:moss_carpet']

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

    // candidates in table order; keep[id] = row stays even when no holder takes the plant
    function rowList() {
        let set = {}, keep = {}
        try {
            let d = JSON.parse(String(JsonIO.readString('kubejs/exported/blocks/pot_probe.json')))
            Object.keys(d.holders).forEach(h => {
                if (h.indexOf('supplementaries:planter') == 0) return
                let rows = d.holders[h].rows
                Object.keys(rows).forEach(i => { if (rows[i].n > 0 || rows[i].survive === true) set[i] = 1 })
            })
        } catch (e) { log('no pot_probe.json, bush scan only') }
        let itr = $FWReg.ITEMS.getValues().iterator()
        while (itr.hasNext()) {
            let it = itr.next()
            if (it instanceof $FWBlockItem && it.getBlock() instanceof $FWBush) set[key($FWReg.ITEMS, it)] = 1
        }
        EXTRA.forEach(i => { set[i] = 1 })
        Ingredient.of('#minecraft:flowers').getItemIds().forEach(i => { set[String(i)] = 1; keep[String(i)] = 1 })
        KEEP.forEach(i => { set[i] = 1; keep[i] = 1 })
        EXCLUDE.forEach(i => { delete set[i] })
        let ids = Object.keys(set).filter(i => key($FWReg.ITEMS, $FWReg.ITEMS.getValue(rl(i))) == i)
        ids.sort((a, b) => {
            let na = a.split(':')[0], nb = b.split(':')[0]
            if (na != nb) { if (na == 'minecraft') return -1; if (nb == 'minecraft') return 1; return na < nb ? -1 : 1 }
            return a < b ? -1 : a > b ? 1 : 0
        })
        return { ids: ids, keep: keep }
    }

    function clearRow(server, level, z) {
        server.runCommandSilent('fill ' + (-STEP - 1) + ' ' + Y + ' ' + z + ' ' + W + ' ' + (Y + 3) + ' ' + z + ' minecraft:air')
        let l = level.getEntitiesOfClass($FWItemEntity, new $FWAABB(-STEP - 1, Y - 2, z - 1, W + 1, Y + 6, z + 2))
        for (let i = 0; i < l.size(); i++) l.get(i).discard()
        server.runCommandSilent('kill @e[type=minecraft:glow_item_frame,x=' + (-STEP - 1) + ',y=' + Y + ',z=' + z + ',dx=' + (W + STEP + 1) + ',dy=3,dz=0]')
    }

    // the flat stack of the Flower Test preset
    function isPresetWorld(level) {
        let gen = level.getChunkSource().getGenerator()
        if (!(gen instanceof $FWFlat)) return false
        let l = gen.settings().getLayersInfo(), parts = []
        for (let i = 0; i < l.size(); i++) parts.push(key($FWReg.BLOCKS, l.get(i).getBlockState().getBlock()) + '*' + l.get(i).getHeight())
        return parts.join(',') == PRESET_LAYERS
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
        let at = pos(x, H.ceiling ? Y + 1 : Y, z)   // hanging holders one up, glass above them
        let guard = []   // spots a refused click may place into: above (non-soil) and the clicked side
        if (H.ceiling) setState(level, at.above(), $FWBlocks.GLASS.defaultBlockState())
        setSpec(level, at, H.id)
        if (key($FWReg.BLOCKS, level.getBlockState(at).getBlock()) != baseId(H.id)) { log('holder did not place: ' + H.id); return false }
        if (H.heldPot) {
            let be = level.getBlockEntity(at)
            if (be != null) { be.setHeldBlock($FWBlocks.FLOWER_POT.defaultBlockState()); be.setChanged() }
        }
        // fill: a silt pot is soil only once a player fills it with rooted dirt; clicked like a player before planting
        // (forced to filled=true if the click did not take)
        if (H.fill) {
            click(level, fp, at, 'up', 0.5, 0.5, H.fill)
            if (!level.getBlockState(at.above()).isAir()) setState(level, at.above(), $FWBlocks.AIR.defaultBlockState())
            if (String(level.getBlockState(at)).indexOf('filled=true') < 0) { log('fill click did not take: ' + H.id); setSpec(level, at, H.id + '[filled=true]') }
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
        let rows = rowList(), ids = rows.ids
        let z1 = ids.length * STEP + 2
        server.runCommandSilent('forceload add ' + (-STEP - 4) + ' ' + (-STEP - 4) + ' ' + (W + 2) + ' ' + z1)
        ;['doDaylightCycle false', 'doWeatherCycle false', 'randomTickSpeed 0', 'doMobSpawning false', 'doFireTick false',
            'mobGriefing false', 'doTraderSpawning false', 'doPatrolSpawning false', 'doInsomnia false'].forEach(g => server.runCommandSilent('gamerule ' + g))
        server.runCommandSilent('time set 6000')
        server.runCommandSilent('weather clear')
        server.runCommandSilent('setworldspawn -5 ' + Y + ' -5')
        server.runCommandSilent('tp @a -5 ' + Y + ' -5')
        log(ids.length + ' candidate rows x ' + HOLDERS.length + ' holders, area 0..' + W + ' x 0..' + z1)
        let fp = $FWFake.get(level, new $FWProfile($FWUUID.randomUUID(), 'flowerworld'))
        fp.getInventory().clearContent()
        let counts = HOLDERS.map(() => 0), axis = { planted: 0, forced: 0, frame: 0 }, dropped = 0
        let i = 0, row = 0
        let step = () => {
            if (i == 0) {
                HOLDERS.forEach((H, c) => {
                    let b = $FWReg.BLOCKS.getValue(rl(baseId(H.id)))
                    sign(server, pos(STEP * (c + 1), Y, -STEP), 8, { translate: b.getDescriptionId() }, { text: baseId(H.id).split(':')[0] })
                })
            }
            for (let k = 0; k < 2 && i < ids.length; k++, i++) {
                let id = ids[i], z = row * STEP
                let it = $FWReg.ITEMS.getValue(rl(id))
                sign(server, pos(-STEP, Y, z), 4, { translate: it.getDescriptionId() }, { text: id.split(':')[0] })
                let a = axisPlant(server, level, fp, 0, z, id)
                let took = HOLDERS.map((H, c) => cell(level, fp, H, STEP * (c + 1), z, id))
                killItems(level, -STEP - 2, z - 1, W + 1, z + 2)
                if (!rows.keep[id] && !took.some((t, c) => t && HOLDERS[c].id.indexOf('supplementaries:planter') != 0)) {
                    clearRow(server, level, z)
                    dropped++
                    continue
                }
                axis[a]++
                took.forEach((t, c) => { if (t) counts[c]++ })
                row++
            }
            if (i < ids.length) { server.scheduleInTicks(1, step); return }
            fp.discard()
            HOLDERS.forEach((H, c) => log('holder ' + H.id + ': ' + counts[c] + ' of ' + row))
            log('rows ' + row + ' (' + dropped + ' candidates no holder took), axis planted ' + axis.planted + ', forced ' + axis.forced + ', item frames ' + axis.frame)
            // mobs the plains chunks spawned while generating
            server.runCommandSilent('kill @e[type=!minecraft:player,type=!minecraft:glow_item_frame]')
            server.runCommandSilent('forceload remove all')
            server.tell('Flower Test: ' + row + ' plants x ' + HOLDERS.length + ' holders built')
            log('DONE')
        }
        server.scheduleInTicks(40, step)
    }

    // first start of a Flower Test world: build once (the first header sign marks a built table)
    ServerEvents.loaded(event => {
        let server = event.server, level = server.overworld()
        if (!isPresetWorld(level)) return
        if (key($FWReg.BLOCKS, level.getBlockState(pos(STEP, Y, -STEP)).getBlock()) == 'minecraft:oak_sign') return
        log('Flower Test world without a table: building')
        build(server, level)
    })

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
