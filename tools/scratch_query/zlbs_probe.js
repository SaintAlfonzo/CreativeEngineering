// Scratch-only Creative Labels probe for the SQ-LBS rows (feature_writeup/testing/scratch_queue.md, "SQ-LBS Creative
// Labels: labels by surface, centred notes"; run r1103 batches/r1103/sB_*.qb). Copy into a scratch server's
// kubejs/server_scripts as zz_zlbs_probe.js, never into the pack. Needs creativeLabels 1.0.1 (FaceContent offX / offY /
// labelSize) for the parts; `dump` also runs on 1.0.0.
//   /zlbs labels    a label (sneak + right-click, the usable-host path) on the north side of each surface host;
//                   checks the face Place (inset / offX / offY / size) against the SQ-LBS-01 table
//   /zlbs refused   a label on door, trapdoor, bottom slab top, stairs side / front / top, cauldron top, pressure
//                   plate top, poppy: no face_paper in the cell and the held label count unchanged (SQ-LBS-02)
//   /zlbs centre    a written sticky note on the north side of each centre-note host (spot C only, offY per the
//                   SQ-LBS-03 table), then a second note on the same side and a note on top, both refused
//   /zlbs refit     after labels and centre: each host set to air; the blank label / written note drop at the cell
//                   and the cell is air (SQ-LBS-04)
//   /zlbs dump x0 z0 x1 z1   every face_paper block entity in the box (y -62..-50): "[zlbs] be x y z <saved NBT>"
//                   (SQ-LBS-05: compare the lines of the 1.0.0 boot with the 1.0.1 boot)
// Output: "[zlbs] PASS|FAIL|INFO <part> <host>: <detail>", then "[zlbs] DONE <part>: N pass, M fail".
// Layout: superflat, hosts float at y -58 in x 300..360; labels z 20, refused z 26 (doors and stairs), centre z 32.
(() => {
    const $ZLFake = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $ZLProfile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $ZLUUID = Java.loadClass('java.util.UUID')
    const $ZLDir = Java.loadClass('net.minecraft.core.Direction')
    const $ZLPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $ZLVec3 = Java.loadClass('net.minecraft.world.phys.Vec3')
    const $ZLAABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $ZLHit = Java.loadClass('net.minecraft.world.phys.BlockHitResult')
    const $ZLHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $ZLItemEntity = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $ZLBlocks = Java.loadClass('net.minecraft.world.level.block.Blocks')
    const $ZLReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $ZLRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $ZLCompound = Java.loadClass('net.minecraft.nbt.CompoundTag')

    const Y = -58
    const STEP = { north: [0, 0, -1], south: [0, 0, 1], east: [1, 0, 0], west: [-1, 0, 0], up: [0, 1, 0], down: [0, -1, 0] }
    const OPP = { north: 'south', south: 'north', east: 'west', west: 'east', up: 'down', down: 'up' }
    // SQ-LBS-01 table: host, inset, offX, offY, size
    const LABEL_HOSTS = [
        ['supplementaries:jar', 0.1875, 0, 0, 10], ['supplementaries:sack', 0.125, 0, -1, 10],
        ['trashcans:item_trash_can', 0.125, 0, 0, 10], ['trashcans:liquid_trash_can', 0.125, 0, 0, 10],
        ['trashcans:energy_trash_can', 0.125, 0, 0, 10], ['trashcans:ultimate_trash_can', 0.125, 0, 0, 10],
        ['vintagedelight:fermenting_jar', 0.203, 0, 0, 10], ['botanypots:terracotta_botany_pot', 0.125, 0, -4, 8],
        ['minecraft:cauldron', 0, 0, 0, 10], ['minecraft:composter', 0, 0, 0, 10], ['minecraft:stone', 0, 0, 0, 10],
        ['minecraft:chest', 0.0625, 0, 0, 10]
    ]
    // SQ-LBS-03 table: host, offY of the centre note
    const CENTRE_HOSTS = [
        ['supplementaries:jar', -1], ['supplementaries:sack', -2], ['trashcans:item_trash_can', -1.25],
        ['trashcans:liquid_trash_can', -1.25], ['trashcans:energy_trash_can', -1.25], ['trashcans:ultimate_trash_can', -1.25],
        ['vintagedelight:fermenting_jar', -1.25], ['botanypots:terracotta_botany_pot', -4]
    ]
    const TOL = 0.002
    let results = { pass: 0, fail: 0 }

    function log(kind, part, host, detail) {
        if (kind == 'PASS') results.pass++
        if (kind == 'FAIL') results.fail++
        console.info('[zlbs] ' + kind + ' ' + part + ' ' + host + ': ' + detail)
    }
    function check(part, host, ok, detail) { log(ok ? 'PASS' : 'FAIL', part, host, detail) }
    function pos(x, y, z) { return new $ZLPos(x, y, z) }
    function dir(n) { return $ZLDir.byName(n) }
    function blockId(level, p) { return String($ZLReg.BLOCKS.getKey(level.getBlockState(p).getBlock())) }
    function air(level, p) { level.setBlockAndUpdate(p, $ZLBlocks.AIR.defaultBlockState()) }
    // 'minecraft:oak_stairs[facing=north,half=bottom]' through KubeJS's block container (default state without [])
    function setSpec(level, p, spec) {
        let i = spec.indexOf('['), props = {}
        if (i >= 0) {
            let parts = spec.substring(i + 1, spec.length - 1).split(',')
            for (let k = 0; k < parts.length; k++) { let kv = parts[k].split('='); props[kv[0]] = kv[1] }
        }
        level.getBlock(p).set(i >= 0 ? spec.substring(0, i) : spec, props)
    }
    let fakeN = 0
    function fake(level) {
        fakeN++
        let p = $ZLFake.get(level, new $ZLProfile($ZLUUID.randomUUID(), 'zlbs' + fakeN))
        p.getInventory().clearContent()
        return p
    }
    function facePoint(h, D, u, v) {
        let x = h.getX(), y = h.getY(), z = h.getZ()
        if (D == 'north') return new $ZLVec3(x + u, y + v, z)
        if (D == 'south') return new $ZLVec3(x + u, y + v, z + 1)
        if (D == 'west') return new $ZLVec3(x, y + v, z + u)
        if (D == 'east') return new $ZLVec3(x + 1, y + v, z + u)
        if (D == 'down') return new $ZLVec3(x + u, y, z + v)
        return new $ZLVec3(x + u, y + 1, z + v)
    }
    function cellOf(h, D) { let s = STEP[D]; return pos(h.getX() + s[0], h.getY() + s[1], h.getZ() + s[2]) }
    function click(p, level, h, D, st, sneak) {
        p.setItemInHand($ZLHand.MAIN_HAND, st)
        p.setShiftKeyDown(!!sneak)
        let hit = new $ZLHit(facePoint(h, D, 0.5, 0.5), dir(D), h, false)
        let r = p.gameMode.useItemOn(p, level, p.getMainHandItem(), $ZLHand.MAIN_HAND, hit)
        p.setShiftKeyDown(false)
        return String(r)
    }
    function be(level, cell) {
        let b = level.getBlockEntity(cell)
        if (b == null || String(b.getClass().getSimpleName()) != 'FacePaperBlockEntity') return null
        return b
    }
    function faceOf(level, h, D) { let b = be(level, cellOf(h, D)); return b == null ? null : b.face(dir(OPP[D])) }
    function near(a, b) { return Math.abs(a - b) <= TOL }
    function placeStr(fc) { return fc == null ? 'no face' : fc.inset + '/' + fc.offX + '/' + fc.offY + '/' + fc.labelSize }
    function note(text) {
        let st = Item.of('creative_labels:sticky_note', 4)
        let cl = new $ZLCompound()
        cl.putString('Text', text)
        st.getOrCreateTag().put('CreativeLabels', cl)
        return st
    }
    function drops(level, cell) {
        let box = new $ZLAABB(cell.getX() - 1, cell.getY() - 1.5, cell.getZ() - 1, cell.getX() + 2, cell.getY() + 1.5, cell.getZ() + 2)
        let list = level.getEntitiesOfClass($ZLItemEntity, box), o = []
        for (let i = 0; i < list.size(); i++) {
            let st = list.get(i).getItem(), t = st.getNbt()
            o.push(st.getCount() + 'x ' + String($ZLReg.ITEMS.getKey(st.getItem())) + (t == null || t.isEmpty() ? '' : String(t)))
        }
        return o
    }
    function killDrops(level, cell) {
        let box = new $ZLAABB(cell.getX() - 1, cell.getY() - 1.5, cell.getZ() - 1, cell.getX() + 2, cell.getY() + 1.5, cell.getZ() + 2)
        let list = level.getEntitiesOfClass($ZLItemEntity, box)
        for (let i = 0; i < list.size(); i++) list.get(i).discard()
    }
    function labelHost(i) { return pos(300 + 4 * i, Y, 20) }
    function centreHost(i) { return pos(300 + 4 * i, Y, 32) }

    function partLabels(level) {
        let p = fake(level)
        LABEL_HOSTS.forEach((h, i) => {
            let hp = labelHost(i)
            air(level, cellOf(hp, 'north'))
            setSpec(level, hp, h[0])
            let r = click(p, level, hp, 'north', Item.of('creative_labels:label', 4), true)
            let fc = faceOf(level, hp, 'north')
            let ok = fc != null && fc.hasLabel() && near(fc.inset, h[1]) && near(fc.offX, h[2]) && near(fc.offY, h[3]) && near(fc.labelSize, h[4])
            check('labels', h[0], ok, 'click ' + r + ', label ' + (fc != null && fc.hasLabel()) + ', place ' + placeStr(fc)
                + ' (want ' + h.slice(1).join('/') + '), labels left ' + p.getMainHandItem().getCount())
        })
        p.discard()
    }

    function partRefused(level) {
        let p = fake(level)
        // [name, host specs [[dx, dy, spec]], clicked host offset, side]
        const CASES = [
            ['oak_door north', [[0, 0, 'minecraft:oak_door[half=lower,facing=north]'], [0, 1, 'minecraft:oak_door[half=upper,facing=north]']], 'north'],
            ['oak_trapdoor north', [[0, 0, 'minecraft:oak_trapdoor[facing=north,half=bottom,open=false]']], 'north'],
            ['oak_slab bottom top', [[0, 0, 'minecraft:oak_slab[type=bottom]']], 'up'],
            ['oak_stairs side (east)', [[0, 0, 'minecraft:oak_stairs[facing=north,half=bottom]']], 'east'],
            ['oak_stairs front (south)', [[0, 0, 'minecraft:oak_stairs[facing=north,half=bottom]']], 'south'],
            ['oak_stairs top', [[0, 0, 'minecraft:oak_stairs[facing=north,half=bottom]']], 'up'],
            ['cauldron top', [[0, 0, 'minecraft:cauldron']], 'up'],
            ['oak_pressure_plate top', [[0, 0, 'minecraft:oak_pressure_plate']], 'up'],
            ['poppy north', [[0, 0, 'minecraft:poppy']], 'north']
        ]
        CASES.forEach((c, i) => {
            let hp = pos(300 + 4 * i, Y, 26)
            // poppy and the pressure plate need a block under them
            if (c[0].indexOf('poppy') == 0) setSpec(level, pos(hp.getX(), Y - 1, hp.getZ()), 'minecraft:grass_block')
            if (c[0].indexOf('pressure') >= 0) setSpec(level, pos(hp.getX(), Y - 1, hp.getZ()), 'minecraft:stone')
            air(level, cellOf(hp, c[2]))
            c[1].forEach(s => setSpec(level, pos(hp.getX() + s[0], hp.getY() + s[1], hp.getZ()), s[2]))
            let r = click(p, level, hp, c[2], Item.of('creative_labels:label', 4), true)
            let cell = cellOf(hp, c[2])
            let cid = blockId(level, cell), left = p.getMainHandItem().getCount()
            check('refused', c[0], cid.indexOf('face_paper') < 0 && left == 4,
                'click ' + r + ', cell ' + cid + ', labels left ' + left + ', host now ' + blockId(level, hp))
        })
        p.discard()
    }

    function partCentre(level) {
        let p = fake(level)
        CENTRE_HOSTS.forEach((h, i) => {
            let hp = centreHost(i)
            air(level, cellOf(hp, 'north'))
            air(level, cellOf(hp, 'up'))
            setSpec(level, hp, h[0])
            let r1 = click(p, level, hp, 'north', note('centre ' + i), true)
            let fc = faceOf(level, hp, 'north')
            let n1 = fc == null ? 0 : fc.noteCount(), m1 = fc == null ? 0 : fc.noteMask()
            check('centre', h[0] + ' first', n1 == 1 && m1 == 4 && near(fc.offY, h[1]),
                'click ' + r1 + ', notes ' + n1 + ', mask ' + m1 + ' (C = 4), place ' + placeStr(fc) + ' (want offY ' + h[1] + ')')
            let r2 = click(p, level, hp, 'north', note('second ' + i), true)
            let n2 = faceOf(level, hp, 'north') == null ? 0 : faceOf(level, hp, 'north').noteCount()
            check('centre', h[0] + ' second', n2 == 1, 'click ' + r2 + ', notes ' + n2)
            let r3 = click(p, level, hp, 'up', note('top ' + i), true)
            let ft = faceOf(level, hp, 'up')
            let n3 = ft == null ? 0 : ft.noteCount()
            check('centre', h[0] + ' top', n3 == 0, 'click ' + r3 + ', top cell ' + blockId(level, cellOf(hp, 'up')) + ', notes ' + n3)
        })
        p.discard()
    }

    function partRefit(level) {
        function one(hp, host, want) {
            let cell = cellOf(hp, 'north')
            let had = blockId(level, cell)
            killDrops(level, cell)
            air(level, hp)
            let d = drops(level, cell), cid = blockId(level, cell)
            let hit = d.filter(s => s.indexOf(want) >= 0 && (want != 'creative_labels:sticky_note' || s.indexOf('Text') >= 0))
            let blank = want != 'creative_labels:label' || hit.every(s => s.indexOf('{') < 0)
            check('refit', host, had.indexOf('face_paper') >= 0 && hit.length == 1 && blank && cid == 'minecraft:air',
                'cell before ' + had + ', after ' + cid + ', drops ' + (d.join(' | ') || 'none'))
        }
        LABEL_HOSTS.forEach((h, i) => one(labelHost(i), 'label on ' + h[0], 'creative_labels:label'))
        CENTRE_HOSTS.forEach((h, i) => one(centreHost(i), 'note on ' + h[0], 'creative_labels:sticky_note'))
    }

    function dump(level, a) {
        let x0 = Math.min(+a[1], +a[3]), x1 = Math.max(+a[1], +a[3]), z0 = Math.min(+a[2], +a[4]), z1 = Math.max(+a[2], +a[4])
        let n = 0
        for (let x = x0; x <= x1; x++) for (let y = -62; y <= -50; y++) for (let z = z0; z <= z1; z++) {
            let b = be(level, pos(x, y, z))
            if (b == null) continue
            n++
            console.info('[zlbs] be ' + x + ' ' + y + ' ' + z + ' ' + String(b.saveWithoutMetadata()))
        }
        log('INFO', 'dump', x0 + ' ' + z0 + ' ' + x1 + ' ' + z1, n + ' face_paper block entities')
    }

    function run(server, a) {
        results = { pass: 0, fail: 0 }
        let level = null
        server.getAllLevels().forEach(l => { if (String(l.dimension.location()) == 'minecraft:overworld') level = l })
        try {
            if (a[0] == 'labels') partLabels(level)
            else if (a[0] == 'refused') partRefused(level)
            else if (a[0] == 'centre') partCentre(level)
            else if (a[0] == 'refit') partRefit(level)
            else if (a[0] == 'dump') dump(level, a)
            else log('INFO', 'unknown', a[0], 'parts: labels refused centre refit dump')
        } catch (e) {
            log('FAIL', a[0], 'ERR', 'exception: ' + e + (e.javaException ? ' / ' + e.javaException : ''))
        }
        console.info('[zlbs] DONE ' + a[0] + ': ' + results.pass + ' pass, ' + results.fail + ' fail')
        return 1
    }

    ServerEvents.commandRegistry(event => {
        const Commands = Java.loadClass('net.minecraft.commands.Commands')
        const StringArg = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
        event.register(Commands.literal('zlbs').requires(src => src.hasPermission(2))
            .then(Commands.argument('args', StringArg.greedyString())
                .executes(ctx => run(ctx.source.getServer(), String(StringArg.getString(ctx, 'args')).trim().split(/\s+/)))))
    })
})()
