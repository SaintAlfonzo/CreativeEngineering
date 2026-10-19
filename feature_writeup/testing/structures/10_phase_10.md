# M10: Phase 10, profiling (8.0.10)

2026-10-19. Live notes: [10_phase_10.scratch.md](10_phase_10.scratch.md). The measured table is in
[locate.md](../../locate.md) 3.7 ("Measured 2026-10-19"); LO-10 is ticked in locate.md 7.

## 1. Setup

- **Server and world:** the same scratch server (patcherMod 1.0.7 jar, whose worldgen equals 1.0.8's). It ran the
  instance's own toml (density 1, logs at their defaults), so the times are the pack's own. Seed 8675309, a fresh
  world.
- **JFR:** set up as in `server/LaunchServer.sh` (stackdepth 256, `perf/ce-server.jfc`), with `maxsize` cut to 2 GB
  because the disk is 97% full.
- **Area:** a Chunky square of radius 64 chunks at 2144 -2592 (92% land): 16,641 chunks in 24.5 min, about 11 chunks
  a second.
- **Tools:** `tools/perf/perf_report.py` for the HTML report, plus a small digest of `jfr print --events
  minecraft.ChunkGeneration` for medians and p95 values (scratchpad `chunkgen.py`).

## 2. Results

| Test | Result | Evidence |
|---|---|---|
| LO-10 | PASSED | Per-status times written into locate.md 3.7 |

**Worker time per status** during the pregen (6,639 s in all, about 4.5 cores busy on average):

| Status | Share | Mean ms | Notes |
|---|---|---|---|
| noise | 38.8% | 95 | |
| features | 24.4% | 75 | |
| light | 18.8% | 75 | The 3.7 estimate said "small" at the stage level |
| structure_starts | 6.4% | 1.2 | 358,852 calls over 22,801 chunks; the median call takes 0 ms |
| surface | 4.1% | 10 | |
| full | 3.5% | 14 | |
| biomes | 2.9% | 6 | |
| carvers | 1.0% | 2.4 | |

`noise`, `features` and `light` make up 82%. The events carry no Y, so how much of each stage the bottom band takes
is still an estimate.

**CPU by mod on the worldgen workers:**

| Mod | Share | Where |
|---|---|---|
| vanilla/Forge | 75.7% | |
| creativeengineering | 6.8% | the earth-fill terrain prediction is 62% of it, meta-structure assembly 11% |
| blueprint | 3.7% | biome lookup |
| twigs | 3.1% | almost all in `ObeliskStructure#canGenerate` |
| copycats | 2.9% | |
| tectonic | 1.7% | |
| volcanic_caverns | 1.6% | |
| ferritecore | 1.3% | |

Distant Horizons' LOD builder threads took 34.9% of all CPU samples during the pregen, on their own threads.

## 3. Findings (no bugs)

- **Structure starts are rebuilt about 16 times per chunk** (up to 23). This is the placement-priority neighbour
  pass and the meta predictions, which M7 also saw as doubled earth-fill lines. The repeats are almost free (the
  whole stage is 6.4%), but every rebuild also reruns the earth-fill terrain prediction, about 4% of worldgen CPU.
- **twigs' obelisk `canGenerate`** costs 3.1% of worldgen CPU by itself. The obelisk is in `group/nether_ruins`;
  its check runs on every slot attempt.
- **`perf_report.py`:** the chunk-generation table appears only in the HTML (`summary.json` has no `chunkgen` key),
  and `--out` needs a file name, not a directory.

## 4. Values changed

None in the instance. The scratch JVM args were put back and the JFR repository deleted (disk space). The
recording is `p10.jfr` in this session's scratchpad.

## 5. Follow-ups for you

1. If worldgen speed matters: the twigs obelisk check and the repeated earth-fill prediction (a cache per chunk
   would cover the rebuilds) are the two pack-side hot spots. Everything else is vanilla, Distant Horizons, or
   mods we don't control.
2. This ends the scratch run (8.0.1 to 8.0.10). The client list (8.0.11) is yours.
