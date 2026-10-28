// WorldEdit lazy state ids, scratch-server probe (scratch_queue.md SQ-WE, perf.md 11.2 #1). Scratch-only: copy it into
// srv/kubejs/server_scripts as zz_we_probe.js; it is never in the pack.
//
// WorldEdit on Forge runs its // commands for players only (the console's are ignored), so this drives the same
// operations through its API: state ids, set, replace, copy with a chest and a sign, undo and redo.
//
//   /wetest   needs the region at X0..X0+7, Y0..Y0+1, Z0..Z0+3 loaded (forceload add 100 100 first); results are
//             "[wetest] PASS|FAIL|INFO <id>: <detail>" lines in logs/latest.log
(() => {
    const $WE = Java.loadClass('com.sk89q.worldedit.WorldEdit')
    const $WEAdapter = Java.loadClass('com.sk89q.worldedit.forge.ForgeAdapter')
    const $WEIds = Java.loadClass('com.sk89q.worldedit.internal.block.BlockStateIdAccess')
    const $WEVec = Java.loadClass('com.sk89q.worldedit.math.BlockVector3')
    const $WECuboid = Java.loadClass('com.sk89q.worldedit.regions.CuboidRegion')
    const $WECopy = Java.loadClass('com.sk89q.worldedit.function.operation.ForwardExtentCopy')
    const $WEOps = Java.loadClass('com.sk89q.worldedit.function.operation.Operations')
    const $WEMask = Java.loadClass('com.sk89q.worldedit.function.mask.BlockMask')
    const $WERandom = Java.loadClass('com.sk89q.worldedit.function.pattern.RandomPattern')
    const $WEBlock = Java.loadClass('net.minecraft.world.level.block.Block')
    const $WEBlockPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $WEForgeReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $WEArrayList = Java.loadClass('java.util.ArrayList')
    const $WERL = Java.loadClass('net.minecraft.resources.ResourceLocation')

    const X0 = 100, Y0 = 200, Z0 = 100, DZ = 20
    let res = { pass: 0, fail: 0 }
    function log(kind, id, detail) {
        if (kind == 'PASS') res.pass++
        if (kind == 'FAIL') res.fail++
        console.info('[wetest] ' + kind + ' ' + id + ': ' + detail)
    }
    function check(id, ok, detail) { log(ok ? 'PASS' : 'FAIL', id, detail) }
    // ForgeAdapter.adapt is overloaded on unrelated types, which Rhino calls ambiguous: name the overload
    const toWorld = $WEAdapter['adapt(net.minecraft.server.level.ServerLevel)']
    const toWEState = $WEAdapter['adapt(net.minecraft.world.level.block.state.BlockState)']
    function v(x, y, z) { return $WEVec.at(x, y, z) }
    function region(world, dz) { return new $WECuboid(world, v(X0, Y0, Z0 + dz), v(X0 + 7, Y0 + 1, Z0 + 3 + dz)) }
    function session(world) { return $WE.getInstance().newEditSessionBuilder().world(world).build() }
    function pattern(state) { let p = new $WERandom(); p.add(state, 1.0); return p }
    // vanilla state and block entity NBT of every block in the source box at offset dz
    function snapshot(level, dz) {
        let out = []
        for (let x = X0; x <= X0 + 7; x++) for (let y = Y0; y <= Y0 + 1; y++) for (let z = Z0; z <= Z0 + 3; z++) {
            let p = new $WEBlockPos(x, y, z + dz)
            let be = level.getBlockEntity(p)
            let nbt = be == null ? '' : String(be.saveWithoutMetadata())
            out.push(String(level.getBlockState(p)) + '|' + nbt)
        }
        return out
    }
    function diff(a, b) {
        let n = 0, first = ''
        for (let i = 0; i < a.length; i++) if (a[i] != b[i]) { if (n == 0) first = a[i] + ' vs ' + b[i]; n++ }
        return n == 0 ? '' : n + ' differ, first: ' + first
    }

    function run(level) {
        let world = toWorld(level)

        // SQ-WE-02/-04: the lazy id of every source state equals vanilla's id and reads back to the same state
        let bad = [], n = 0
        for (let x = X0; x <= X0 + 7; x++) for (let y = Y0; y <= Y0 + 1; y++) for (let z = Z0; z <= Z0 + 3; z++) {
            let ws = world.getBlock(v(x, y, z))
            let mc = level.getBlockState(new $WEBlockPos(x, y, z))
            let id = $WEIds.getBlockStateId(ws)
            let back = $WEIds.getBlockStateById(id)
            n++
            if (id != $WEBlock.getId(mc) || back == null || !back.equals(ws)) bad.push(String(mc) + ' id ' + id + ' vanilla ' + $WEBlock.getId(mc))
        }
        check('SQ-WE-ids', bad.length == 0, n + ' states, lazy id = vanilla id and reads back; bad: ' + bad.slice(0, 3))
        let modded = ['immersiveposts:ironpost_truss', 'createdeco:andesite_catwalk', 'quark:oak_vertical_slab']
        modded.forEach(b => {
            let blk = $WEForgeReg.BLOCKS.getValue(new $WERL(b))
            if (blk == null || String($WEForgeReg.BLOCKS.getKey(blk)) != b) { log('INFO', 'SQ-WE-04', b + ' not registered'); return }
            let states = blk.getStateDefinition().getPossibleStates()
            let wrong = 0
            for (let i = 0; i < states.size(); i++) {
                let ws = toWEState(states.get(i))
                let id = $WEIds.getBlockStateId(ws)
                if (id != $WEBlock.getId(states.get(i)) || !$WEIds.getBlockStateById(id).equals(ws)) wrong++
            }
            check('SQ-WE-04', wrong == 0, b + ': ' + states.size() + ' states, ' + wrong + ' wrong ids')
        })

        let src = snapshot(level, 0)

        // SQ-WE-03: copy the box DZ blocks south, compare states and block entities, undo, redo
        let es = session(world)
        let copy = new $WECopy(es, region(world, 0), es, v(X0, Y0, Z0 + DZ))
        $WEOps.complete(copy)
        es.close()
        check('SQ-WE-03 copy', diff(src, snapshot(level, DZ)) == '', 'copy matches the source ' + diff(src, snapshot(level, DZ)))
        let un = session(world); es.undo(un); un.close()
        let afterUndo = snapshot(level, DZ)
        let notAir = afterUndo.filter(s => s.indexOf('Block{minecraft:air}') != 0).length
        check('SQ-WE-03 undo', notAir == 0, 'undo leaves air (' + notAir + ' of ' + afterUndo.length + ' not air)')
        let re = session(world); es.redo(re); re.close()
        check('SQ-WE-03 redo', diff(src, snapshot(level, DZ)) == '', 'redo restores the copy ' + diff(src, snapshot(level, DZ)))

        // SQ-WE-02: replace the top-east oak stairs in the copy with stone, set a box of them, undo both
        let stairs = toWEState(level.getBlockState(new $WEBlockPos(X0, Y0, Z0)))
        let stone = toWEState($WEForgeReg.BLOCKS.getValue(new $WERL('minecraft:stone')).defaultBlockState())
        let list = new $WEArrayList(); list.add(stairs.toBaseBlock())
        let rs = session(world)
        let changed = rs['replaceBlocks(com.sk89q.worldedit.regions.Region,com.sk89q.worldedit.function.mask.Mask,com.sk89q.worldedit.function.pattern.Pattern)'](region(world, DZ), new $WEMask(rs, list), pattern(stone))
        rs.close()
        let st = String(level.getBlockState(new $WEBlockPos(X0, Y0, Z0 + DZ)))
        check('SQ-WE-02 replace', changed > 0 && st.indexOf('minecraft:stone') >= 0, changed + ' replaced, ' + String(stairs) + ' -> ' + st)
        let ss = session(world)
        let set = ss['setBlocks(com.sk89q.worldedit.regions.Region,com.sk89q.worldedit.function.pattern.Pattern)'](region(world, DZ), pattern(stairs))
        ss.close()
        let okSet = snapshot(level, DZ).every(s => s.indexOf(String(level.getBlockState(new $WEBlockPos(X0, Y0, Z0)))) == 0)
        check('SQ-WE-02 set', okSet, set + ' set to ' + String(level.getBlockState(new $WEBlockPos(X0, Y0, Z0))))
        let u2 = session(world); ss.undo(u2); u2.close()
        let u3 = session(world); rs.undo(u3); u3.close()
        check('SQ-WE-02 undo', diff(src, snapshot(level, DZ)) == '', 'set and replace undone back to the copy ' + diff(src, snapshot(level, DZ)))
        let clean = session(world); es.undo(clean); clean.close()

        console.info('[wetest] DONE ' + res.pass + ' pass, ' + res.fail + ' fail')
        return 1
    }

    ServerEvents.commandRegistry(event => {
        const Commands = Java.loadClass('net.minecraft.commands.Commands')
        event.register(Commands.literal('wetest').requires(src => src.hasPermission(2))
            .executes(ctx => {
                try { return run(ctx.source.getLevel()) } catch (e) { console.error('[wetest] FAIL ERR: ' + e + (e.javaException ? ' / ' + e.javaException : '')); return 0 }
            }))
    })
})()
