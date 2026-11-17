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
// Since r1115 also "[zqh] blocks <biome>: ..." (every non-air block whose own cell is in the biome, top ids, then the
// netherrack / flesh / porous_flesh / quartz / gold ore / bedrock counts), "[zqh] air <biome>: ..." (air ids),
// "[zqh] interior ..." (the same for blocks whose 27 surrounding quart cells are all the biome) and every floor and
// ceiling id ("[zqh] floor all ids", "ceiling all ids").
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
            // 10th argument (r1115): an id whose interior blocks are listed (first 12): position, zoomed biome
            // (level.getBiome, what the surface rule reads), the nearest biomesoplenty:visceral_heap cell within 16 blocks
            let where = a[9] || null, wl = []
            let lv = null
            server.getAllLevels().forEach(l => { if (String(l.dimension) == dim) lv = l })
            if (!lv) { out('no level ' + dim); return }
            let cs = lv.getChunkSource(), src = cs.getGenerator().getBiomeSource(), sampler = cs.randomState().sampler()
            let bcache = {}
            let biomeAt = function (x, y, z) {
                let k = (x >> 2) + ',' + (y >> 2) + ',' + (z >> 2)
                let c = bcache[k]
                if (c !== undefined) return c
                let h = src.getNoiseBiome(x >> 2, y >> 2, z >> 2, sampler).unwrapKey()
                c = h.isPresent() ? String(h.get().location()) : 'none'
                bcache[k] = c
                return c
            }
            // r1115: every block of the scan whose own cell is in <biome id> (SQ-VH-06 "every non-air block", -06b
            // cave_air, -06c ores), not only the surfaces
            let blocks = {}, nb = 0, airIds = {}, inner = {}, ni = 0
            // interior: the block's quart cell and all 26 neighbour cells are <biome id>, so the surface rule's fuzzed
            // biome lookup (BiomeManager zoom, up to about 2 blocks off the quart grid) cannot read another biome there
            let interior = function (x, y, z) {
                for (let dx = -4; dx <= 4; dx += 4) for (let dy = -4; dy <= 4; dy += 4) for (let dz = -4; dz <= 4; dz += 4)
                    if (biomeAt(x + dx, y + dy, z + dz) != want) return false
                return true
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
                    if (biomeAt(x, y, z) == want) {
                        if (kinds[i] == 'air') airIds[ids[i]] = (airIds[ids[i]] || 0) + 1
                        else {
                            blocks[ids[i]] = (blocks[ids[i]] || 0) + 1; nb++
                            if (interior(x, y, z)) {
                                inner[ids[i]] = (inner[ids[i]] || 0) + 1; ni++
                                if (where && ids[i] == where) wl.push([x, y, z])
                            }
                        }
                    }
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
            let topN = function (m, n) { return Object.keys(m).sort((p, q) => m[q] - m[p]).slice(0, n).map(k => k + ' ' + m[k]).join(', ') }
            out('blocks ' + want + ': ' + nb + ' non-air in the biome; top ids: ' + topN(blocks, 14))
            out('blocks ' + want + ' netherrack ' + (blocks['minecraft:netherrack'] || 0) + ', flesh ' + (blocks['biomesoplenty:flesh'] || 0)
                + ', porous_flesh ' + (blocks['biomesoplenty:porous_flesh'] || 0) + ', quartz_ore ' + (blocks['minecraft:nether_quartz_ore'] || 0)
                + ', gold_ore ' + (blocks['minecraft:nether_gold_ore'] || 0) + ', bedrock ' + (blocks['minecraft:bedrock'] || 0))
            out('air ' + want + ': ' + topN(airIds, 4))
            out('interior ' + want + ': ' + ni + ' non-air; netherrack ' + (inner['minecraft:netherrack'] || 0) + ', flesh ' + (inner['biomesoplenty:flesh'] || 0)
                + ', porous_flesh ' + (inner['biomesoplenty:porous_flesh'] || 0) + '; top ids: ' + topN(inner, 20))
            let buckets = {}
            wl.forEach((q, qi) => {
                let zb = lv.getBiome(new $ZQHPos(q[0], q[1], q[2])).unwrapKey()
                let near = 99
                for (let dx = -16; dx <= 16; dx += 4) for (let dy = -16; dy <= 16; dy += 4) for (let dz = -16; dz <= 16; dz += 4) {
                    if (biomeAt(q[0] + dx, q[1] + dy, q[2] + dz) == 'biomesoplenty:visceral_heap') near = Math.min(near, Math.max(Math.abs(dx), Math.abs(dy), Math.abs(dz)))
                }
                let bk = near > 16 ? 'over16' : 'le' + near
                buckets[bk] = (buckets[bk] || 0) + 1
                if (qi < 12) out('where ' + where + ' ' + q.join(' ') + ' zoomed ' + (zb.isPresent() ? zb.get().location() : 'none') + ', heap cell within ' + near)
            })
            if (where) out('where ' + where + ' ' + wl.length + ' interior blocks by distance to the nearest Heap cell: ' + Object.keys(buckets).sort().map(k => k + ' ' + buckets[k]).join(', '))
            out('floor all ids ' + want + ': ' + topN(floor, 30))
            out('ceiling all ids ' + want + ': ' + topN(ceil, 30))
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
