// Scratch-only biome grid search for bulk queue runs (SQ-FGD-12m, run r1102b). Copy into a scratch server's
// kubejs/server_scripts as zz_zq_biomegrid.js, never into the pack.
//   /zqb <biome id> <x> <z> <r> [step] [win] [dim]
// Samples the live biome source on a grid (every <step> blocks, default 16 = one point per chunk) within <r> blocks of
// x z at y 64, then finds the square of half-width <win> blocks (default 150) holding the most matching points.
// Pure noise evaluation (getNoiseBiome with the chunk source's sampler): no chunk is loaded or generated, so it is safe
// on ungenerated land (gotchas.md, Rhino / KubeJS probes).
// Output: "[zqb] grid <biome>: H of N points match; best centre X Z with K of W points (S %)" then "[zqb] done".
(() => {
    const $ZQBArgs = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    function out(s) { console.info('[zqb] ' + s) }
    function run(server, arg) {
        try {
            let a = String(arg).trim().split(/\s+/)
            let want = a[0], x0 = Math.floor(+a[1]), z0 = Math.floor(+a[2]), r = Math.floor(+a[3])
            let step = a[4] ? Math.max(4, Math.floor(+a[4])) : 16, win = a[5] ? Math.floor(+a[5]) : 150
            let dim = a[6] || 'minecraft:overworld', lv = null
            server.getAllLevels().forEach(l => { if (String(l.dimension) == dim) lv = l })
            if (!lv) { out('no level ' + dim); return }
            let cs = lv.getChunkSource(), src = cs.getGenerator().getBiomeSource(), sampler = cs.randomState().sampler()
            let n = Math.floor(r / step), side = 2 * n + 1, grid = [], hits = 0
            for (let i = 0; i < side; i++) {
                let row = []
                for (let j = 0; j < side; j++) {
                    let x = x0 + (i - n) * step, z = z0 + (j - n) * step
                    let h = src.getNoiseBiome(x >> 2, 16, z >> 2, sampler).unwrapKey()
                    let ok = h.isPresent() && String(h.get().location()) == want ? 1 : 0
                    row.push(ok); hits += ok
                }
                grid.push(row)
            }
            // summed-area table for the best window
            let S = []
            for (let i = 0; i <= side; i++) { S.push([]); for (let j = 0; j <= side; j++) S[i].push(0) }
            for (let i = 1; i <= side; i++) for (let j = 1; j <= side; j++)
                S[i][j] = grid[i - 1][j - 1] + S[i - 1][j] + S[i][j - 1] - S[i - 1][j - 1]
            let w = Math.floor(win / step), best = -1, bi = n, bj = n
            for (let i = w; i < side - w; i++) for (let j = w; j < side - w; j++) {
                let k = S[i + w + 1][j + w + 1] - S[i - w][j + w + 1] - S[i + w + 1][j - w] + S[i - w][j - w]
                if (k > best) { best = k; bi = i; bj = j }
            }
            let cells = (2 * w + 1) * (2 * w + 1)
            out('grid ' + want + ': ' + hits + ' of ' + side * side + ' points match; best centre ' + (x0 + (bi - n) * step) + ' '
                + (z0 + (bj - n) * step) + ' with ' + Math.max(best, 0) + ' of ' + cells + ' points (' + Math.round(100 * Math.max(best, 0) / cells) + ' %)')
        } catch (err) { out('error: ' + err) }
        finally { out('done') }
    }
    ServerEvents.commandRegistry(e => {
        const { commands: C } = e
        e.register(C.literal('zqb')
            .then(C.argument('arg', $ZQBArgs.greedyString())
                .executes(ctx => { run(ctx.source.server, $ZQBArgs.getString(ctx, 'arg')); return 1 })))
    })
})()
