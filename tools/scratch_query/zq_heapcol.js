// Scratch-only floor and ceiling surface probe for bulk queue runs (SQ-VH-06, run r1112). Copy into a scratch server's
// kubejs/server_scripts as zz_zq_heapcol.js, never into the pack.
//   /zqh <biome id> <x1> <z1> <x2> <z2> [dim] [y1] [y2]
// Per column of the box (loaded chunks only; unloaded columns are skipped and counted), scans y1..y2 (default 1..126):
//   floor surface    a solid block with air (air or cave_air) directly above it
//   ceiling surface  a solid block with air directly below it
// Each surface counts only when the biome of its air cell is <biome id> (live noise biome, getNoiseBiome at that y:
// Nether biomes are 3D, so a column can cross several). Fluids (lava sea) are neither solid nor air.
// Output lines: "[zqh] floor <biome>: N surfaces in C columns; top ids: id n, ..."; same for "ceiling"; then
// "[zqh] floor <want>: K of N (P %)" for the expected id (default biomesoplenty:flesh, 9th argument), bedrock contacts
// (floor surface at y with bedrock at y-1; ceiling surface with bedrock at y+1), and "[zqh] done".
(() => {
    const $ZQHArgs = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $ZQHReg = Java.loadClass('net.minecraft.core.Registry')
    const $ZQHPos = Java.loadClass('net.minecraft.core.BlockPos')
    function out(s) { console.info('[zqh] ' + s) }
    function run(server, arg) {
        try {
            let a = String(arg).trim().split(/\s+/)
            let want = a[0]
            let x1 = Math.min(+a[1], +a[3]), x2 = Math.max(+a[1], +a[3]), z1 = Math.min(+a[2], +a[4]), z2 = Math.max(+a[2], +a[4])
            let dim = a[5] || 'minecraft:the_nether', y1 = a[6] ? +a[6] : 1, y2 = a[7] ? +a[7] : 126
            let expect = a[8] || 'biomesoplenty:flesh'
            let lv = null
            server.getAllLevels().forEach(l => { if (String(l.dimension) == dim) lv = l })
            if (!lv) { out('no level ' + dim); return }
            let cs = lv.getChunkSource(), src = cs.getGenerator().getBiomeSource(), sampler = cs.randomState().sampler()
            let biomeAt = function (x, y, z) {
                let h = src.getNoiseBiome(x >> 2, y >> 2, z >> 2, sampler).unwrapKey()
                return h.isPresent() ? String(h.get().location()) : 'none'
            }
            let floor = {}, ceil = {}, nf = 0, nc = 0, skipped = 0, cols = {}, floorBed = 0, ceilBed = 0, ylist = {}
            for (let x = x1; x <= x2; x++) for (let z = z1; z <= z2; z++) {
                if (!lv.hasChunk(x >> 4, z >> 4)) { skipped++; continue }
                let ids = [], kinds = []
                for (let y = y1 - 1; y <= y2 + 1; y++) {
                    let st = lv.getBlockState(new $ZQHPos(x, y, z))
                    ids.push(String($ZQHReg.BLOCK.getKey(st.getBlock())))
                    kinds.push(st.isAir() ? 'air' : (st.getFluidState().isEmpty() && st.getMaterial().isSolid() ? 'solid' : 'fluid'))
                }
                // index i <-> y = y1 - 1 + i
                for (let i = 1; i < ids.length - 1; i++) {
                    let y = y1 - 1 + i
                    if (kinds[i] != 'solid') continue
                    if (kinds[i + 1] == 'air' && biomeAt(x, y + 1, z) == want) {
                        floor[ids[i]] = (floor[ids[i]] || 0) + 1; nf++; cols[x + ',' + z] = 1
                        if (ids[i - 1] == 'minecraft:bedrock') floorBed++
                        let band = Math.floor(y / 16) * 16
                        ylist['f' + band] = (ylist['f' + band] || 0) + 1
                    }
                    if (kinds[i - 1] == 'air' && biomeAt(x, y - 1, z) == want) {
                        ceil[ids[i]] = (ceil[ids[i]] || 0) + 1; nc++; cols[x + ',' + z] = 1
                        if (ids[i + 1] == 'minecraft:bedrock') ceilBed++
                        let band = Math.floor(y / 16) * 16
                        ylist['c' + band] = (ylist['c' + band] || 0) + 1
                    }
                }
            }
            let top = function (m) { return Object.keys(m).sort((p, q) => m[q] - m[p]).slice(0, 8).map(k => k + ' ' + m[k]).join(', ') }
            let pct = function (k, n) { return n ? Math.round(1000 * k / n) / 10 : 0 }
            let ncols = Object.keys(cols).length
            out('box ' + x1 + ' ' + z1 + ' to ' + x2 + ' ' + z2 + ' y ' + y1 + '..' + y2 + ' in ' + dim + '; ' + skipped + ' columns unloaded; ' + ncols + ' columns with a ' + want + ' surface')
            out('floor ' + want + ': ' + nf + ' surfaces; top ids: ' + top(floor))
            out('ceiling ' + want + ': ' + nc + ' surfaces; top ids: ' + top(ceil))
            out('floor ' + expect + ': ' + (floor[expect] || 0) + ' of ' + nf + ' (' + pct(floor[expect] || 0, nf) + ' %); on bedrock ' + floorBed)
            out('ceiling ' + expect + ': ' + (ceil[expect] || 0) + ' of ' + nc + ' (' + pct(ceil[expect] || 0, nc) + ' %); under bedrock ' + ceilBed)
            out('surfaces by 16-block band (f floor, c ceiling): ' + Object.keys(ylist).sort().map(k => k + ' ' + ylist[k]).join(', '))
        } catch (err) { out('error: ' + err) }
        finally { out('done') }
    }
    ServerEvents.commandRegistry(e => {
        const { commands: C } = e
        e.register(C.literal('zqh')
            .then(C.argument('arg', $ZQHArgs.greedyString())
                .executes(ctx => { run(ctx.source.server, $ZQHArgs.getString(ctx, 'arg')); return 1 })))
    })
})()
