// Scratch-only wild crop census probe (run r1116 phase 4: SQ-PS15-11, -11b, -12, -12b, -12c). Copy into a scratch
// server's kubejs/server_scripts as zz_zcen.js, never into the pack. Loaded chunks only (forceload the box first):
// nothing here loads or generates a chunk (gotchas.md, Rhino / KubeJS probes).
//   /zcen scan X1 Z1 X2 Z2 ID@KEY,ID@KEY,... [ylo]
//       Every chunk whose centre lies in the box: surface biome at the chunk centre (WORLD_SURFACE height), whether
//       that biome is in each crop rule tag creativedelight:wild_crops/KEY, and every block ID from ylo (default 40)
//       up to the column's WORLD_SURFACE height. Lines:
//       "[zcen] box ... chunks=N skipped=S", "[zcen] biomes ...", "[zcen] tag KEY size=Z tagchunks=T",
//       "[zcen] id ID key=KEY blocks=B chunks=C patches=P intag_chunks=I intag_patches=J" (patch = blocks joined
//       when within 6 blocks horizontally; '-' over 3000 blocks), then "[zcen] scan done".
//   /zcen grid FEATURE ID,ID X Z N STEP MODE
//       N x N points STEP apart centred on X Z. At each point a column within 6 blocks is chosen; MODE ground or canopy
//       needs a canopy column (top MOTION_BLOCKING block is leaves, at least 3 above the NO_LEAVES top, ground in
//       #minecraft:dirt); ground = origin at the NO_LEAVES height (air above the ground), canopy = origin one above the
//       leaves. MODE yNN = origin at that y on the grid point itself (no column search). The configured FEATURE is
//       placed there (as /place feature) and the IDs are counted in x/z +-8, y origin-12..origin+12 before and after.
//       Lines "[zcen] pt I x y z mode=M ok=true|false placed=D at=<block at origin before> cover=<block above ground>",
//       "[zcen] grid FEATURE MODE: points=P with_column=C ok=K placed_points=Q placed_blocks=B", "[zcen] grid done".
(() => {
    const $ZCArgs = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $ZCReg = Java.loadClass('net.minecraft.core.Registry')
    const $ZCRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $ZCPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $ZCTagKey = Java.loadClass('net.minecraft.tags.TagKey')
    const $ZCHm = Java.loadClass('net.minecraft.world.level.levelgen.Heightmap$Types')
    const $ZCRand = Java.loadClass('net.minecraft.util.RandomSource')
    const $ZCBTags = Java.loadClass('net.minecraft.tags.BlockTags')
    function zcOut(s) { console.info('[zcen] ' + s) }
    let zcBlockId = function (st) { return String($ZCReg.BLOCK.getKey(st.getBlock())) }
    let zcPatches = function (pts) {
        // single-link clusters, 6 blocks horizontally
        let lab = [], n = 0
        for (let i = 0; i < pts.length; i++) lab.push(-1)
        for (let i = 0; i < pts.length; i++) {
            if (lab[i] >= 0) continue
            lab[i] = n
            let stack = [i]
            while (stack.length) {
                let k = stack.pop()
                for (let j = 0; j < pts.length; j++) {
                    if (lab[j] >= 0) continue
                    if (Math.abs(pts[j][0] - pts[k][0]) <= 6 && Math.abs(pts[j][1] - pts[k][1]) <= 6) { lab[j] = n; stack.push(j) }
                }
            }
            n++
        }
        return { n: n, lab: lab }
    }
    let zcScan = function (server, a) {
        let lv = server.overworld()
        let x1 = Math.min(+a[0], +a[2]), x2 = Math.max(+a[0], +a[2]), z1 = Math.min(+a[1], +a[3]), z2 = Math.max(+a[1], +a[3])
        let ylo = a[5] ? +a[5] : 40
        let pairs = String(a[4]).split(',').map(s => { let p = s.split('@'); return { id: p[0], key: p[1] || '' } })
        let reg = server.registryAccess().registryOrThrow($ZCReg.BIOME_REGISTRY)
        let tagSets = {}
        pairs.forEach(p => {
            if (!p.key || tagSets[p.key]) return
            let tk = $ZCTagKey.create($ZCReg.BIOME_REGISTRY, new $ZCRL('creativedelight:wild_crops/' + p.key))
            let set = {}, n = 0
            reg.getTagOrEmpty(tk).forEach(h => { set[String(h.unwrapKey().get().location())] = 1; n++ })
            tagSets[p.key] = { set: set, size: n, chunks: 0 }
        })
        let want = {}
        pairs.forEach(p => {
            if (!$ZCReg.BLOCK.containsKey(new $ZCRL(p.id))) zcOut('warn no such block ' + p.id)
            want[p.id] = { key: p.key, blocks: 0, chunks: {}, pts: [] }
        })
        let biomes = {}, chunkBiome = {}, nChunks = 0, skipped = 0
        for (let cx = Math.ceil((x1 - 8) / 16); cx * 16 + 8 <= x2; cx++) for (let cz = Math.ceil((z1 - 8) / 16); cz * 16 + 8 <= z2; cz++) {
            if (!lv.hasChunk(cx, cz)) { skipped++; continue }
            nChunks++
            let ch = lv.getChunk(cx, cz)
            let hc = ch.getHeight($ZCHm.WORLD_SURFACE, 8, 8)
            let bid = String(lv.getBiome(new $ZCPos(cx * 16 + 8, hc, cz * 16 + 8)).unwrapKey().get().location())
            biomes[bid] = (biomes[bid] || 0) + 1
            let ck = cx + ',' + cz
            chunkBiome[ck] = bid
            Object.keys(tagSets).forEach(k => { if (tagSets[k].set[bid]) tagSets[k].chunks++ })
            let m = new $ZCPos.MutableBlockPos()
            for (let lx = 0; lx < 16; lx++) for (let lz = 0; lz < 16; lz++) {
                let top = ch.getHeight($ZCHm.WORLD_SURFACE, lx, lz)
                for (let y = ylo; y <= top + 1; y++) {
                    m.set(cx * 16 + lx, y, cz * 16 + lz)
                    let st = ch.getBlockState(m)
                    if (st.isAir()) continue
                    let w = want[zcBlockId(st)]
                    if (w === undefined) continue
                    w.blocks++
                    w.chunks[ck] = 1
                    if (w.pts.length < 20000) w.pts.push([cx * 16 + lx, cz * 16 + lz, ck])
                }
            }
        }
        zcOut('box ' + x1 + ' ' + z1 + ' ' + x2 + ' ' + z2 + ' chunks=' + nChunks + ' skipped=' + skipped)
        let bl = Object.keys(biomes).sort((p, q) => biomes[q] - biomes[p]).map(b => b + ' ' + biomes[b])
        zcOut('biomes ' + bl.join(', '))
        Object.keys(tagSets).forEach(k => zcOut('tag ' + k + ' size=' + tagSets[k].size + ' tagchunks=' + tagSets[k].chunks))
        pairs.forEach(p => {
            let w = want[p.id], ts = tagSets[p.key]
            let cl = w.pts.length <= 3000 ? zcPatches(w.pts) : { n: '-', lab: w.pts.map(q => -1) }
            let inChunks = Object.keys(w.chunks).filter(ck => ts && ts.set[chunkBiome[ck]]).length
            let inPatch = {}
            for (let i = 0; i < w.pts.length; i++) if (ts && cl.lab[i] >= 0 && ts.set[chunkBiome[w.pts[i][2]]]) inPatch[cl.lab[i]] = 1
            let where = []
            let seen = {}
            for (let i = 0; i < w.pts.length && where.length < 12; i++) {
                if (cl.lab[i] < 0 || seen[cl.lab[i]]) continue
                seen[cl.lab[i]] = 1
                where.push(w.pts[i][0] + ' ' + w.pts[i][1] + ' ' + chunkBiome[w.pts[i][2]])
            }
            zcOut('id ' + p.id + ' key=' + p.key + ' blocks=' + w.blocks + ' chunks=' + Object.keys(w.chunks).length + ' patches=' + cl.n
                + ' intag_chunks=' + inChunks + ' intag_patches=' + Object.keys(inPatch).length + (where.length ? ' at=' + where.join(' | ') : ''))
        })
    }
    let zcCount = function (lv, ids, o) {
        let n = 0, m = new $ZCPos.MutableBlockPos()
        for (let x = o.getX() - 8; x <= o.getX() + 8; x++) for (let z = o.getZ() - 8; z <= o.getZ() + 8; z++) {
            if (!lv.hasChunk(x >> 4, z >> 4)) continue
            for (let y = o.getY() - 12; y <= o.getY() + 12; y++) {
                m.set(x, y, z)
                if (ids[zcBlockId(lv.getBlockState(m))]) n++
            }
        }
        return n
    }
    let zcColumn = function (lv, x0, z0) {
        // nearest canopy column within 6 blocks: leaves on top, ground (dirt tag) at least 3 below
        for (let r = 0; r <= 6; r++) for (let dx = -r; dx <= r; dx++) for (let dz = -r; dz <= r; dz++) {
            if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue
            let x = x0 + dx, z = z0 + dz
            if (!lv.hasChunk(x >> 4, z >> 4)) continue
            let mb = lv.getHeight($ZCHm.MOTION_BLOCKING, x, z), nl = lv.getHeight($ZCHm.MOTION_BLOCKING_NO_LEAVES, x, z)
            if (mb - nl < 3) continue
            if (!lv.getBlockState(new $ZCPos(x, mb - 1, z)).is($ZCBTags.LEAVES)) continue
            if (!lv.getBlockState(new $ZCPos(x, nl - 1, z)).is($ZCBTags.DIRT)) continue
            return { x: x, z: z, ground: nl, canopy: mb }
        }
        return null
    }
    let zcGrid = function (server, a) {
        let lv = server.overworld()
        let fid = a[0], ids = {}
        String(a[1]).split(',').forEach(i => { ids[i] = 1 })
        let X = +a[2], Z = +a[3], N = +a[4], step = +a[5], mode = String(a[6])
        let cf = server.registryAccess().registryOrThrow($ZCReg.CONFIGURED_FEATURE_REGISTRY).get(new $ZCRL(fid))
        if (cf == null) { zcOut('grid no configured feature ' + fid); return }
        let gen = lv.getChunkSource().getGenerator(), rnd = $ZCRand.create()
        let pts = 0, withCol = 0, okN = 0, placedPts = 0, placedBlocks = 0
        for (let i = 0; i < N; i++) for (let j = 0; j < N; j++) {
            pts++
            let gx = X + (i - (N - 1) / 2) * step, gz = Z + (j - (N - 1) / 2) * step, o = null, cover = '-'
            if (mode.charAt(0) == 'y') {
                if (lv.hasChunk(gx >> 4, gz >> 4)) o = new $ZCPos(gx, +mode.substring(1), gz)
            } else {
                let c = zcColumn(lv, gx, gz)
                if (c) {
                    o = new $ZCPos(c.x, mode == 'canopy' ? c.canopy : c.ground, c.z)
                    cover = zcBlockId(lv.getBlockState(new $ZCPos(c.x, c.ground, c.z)))
                }
            }
            if (o == null) { zcOut('pt ' + pts + ' ' + gx + ' - ' + gz + ' mode=' + mode + ' no column'); continue }
            withCol++
            let at = zcBlockId(lv.getBlockState(o))
            let before = zcCount(lv, ids, o)
            let ok = cf.place(lv, gen, rnd, o)
            let d = zcCount(lv, ids, o) - before
            if (ok) okN++
            if (d > 0) { placedPts++; placedBlocks += d }
            zcOut('pt ' + pts + ' ' + o.getX() + ' ' + o.getY() + ' ' + o.getZ() + ' mode=' + mode + ' ok=' + ok + ' placed=' + d + ' at=' + at + ' cover=' + cover)
        }
        zcOut('grid ' + fid + ' ' + mode + ': points=' + pts + ' with_column=' + withCol + ' ok=' + okN + ' placed_points=' + placedPts + ' placed_blocks=' + placedBlocks)
    }
    let zcRun = function (server, what, arg) {
        let a = arg ? String(arg).trim().split(/\s+/) : []
        try {
            if (what == 'scan') zcScan(server, a)
            else if (what == 'grid') zcGrid(server, a)
            else zcOut('unknown ' + what)
        } catch (err) { zcOut('error ' + err) }
        finally { zcOut(what + ' done') }
    }
    ServerEvents.commandRegistry(e => {
        const { commands: C } = e
        e.register(C.literal('zcen')
            .then(C.argument('what', $ZCArgs.word())
                .then(C.argument('arg', $ZCArgs.greedyString())
                    .executes(ctx => { zcRun(ctx.source.server, $ZCArgs.getString(ctx, 'what'), $ZCArgs.getString(ctx, 'arg')); return 1 }))))
    })
})()
