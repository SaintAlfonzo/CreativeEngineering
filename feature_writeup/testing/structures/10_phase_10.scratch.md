# M10 scratchpad: Phase 10 (8.0.10, profiling)

## State (keep current)

- Test: LO-10 (locate.md 7): JFR recording of a Chunky pregen (radius 64 chunks) -> per-status chunk generation
  times, written into locate.md 3.7.
- Server: same scratch server, patcherMod 1.0.7 jar (worldgen = 1.0.8). Toml = the instance's own (density 1, logs at
  their defaults) so timings are the pack's. JFR as server/LaunchServer.sh (stackdepth 256, perf/ce-server.jfc,
  repository perf/jfr-repo) but maxage 3h / maxsize 2g (disk 97% full, 14 GB free). user_jvm_args.base = the plain one.
- World st_10 (seed 8675309, normal), fresh. Area: Chunky square r 1024 blocks (64 chunks) at 2144 -2592 (92% land,
  Phase 6), 16641 chunks.
- Running: nothing; Phase 10 done 08:30. Recording: scratchpad p10.jfr (376 MB); report perfout/p10_pregen.html.

## Log (append-only)

- 07:45 boot with JFR.
- 07:48:20 Done (94.5 s, JFR recording 1 started at 0.33 s). 07:48:29 Chunky r 1024 at 2144 -2592 -> 08:13:33
  finished, 16641 chunks, ~24:30, 10-12 cps. 08:13:44 `jcmd <pid> JFR.dump name=ce` -> p10.jfr 376 MB. Stopped;
  user_jvm_args.txt restored (plain -Xmx6G); perf/jfr-repo deleted (disk).
- perf_report.py p10.jfr --mods srv/mods --out perfout/p10_pregen.html: window 1520 s; Server thread top:
  minecraft/forge 77.6%, lootr 8.1%, chunky 5.9%, kubejs 2.8%. Thread groups: Worker-Main 49.6% of samples,
  DH-LOD Builder 34.9%, DH-Update Propagator 7.2%, Server thread 7.1%. Its "Chunk generation" table is only in the
  HTML (summary.json has no chunkgen key). `--out` must be a file path, not a directory.
- Own digest (scratchpad chunkgen.py over `jfr print --events minecraft.ChunkGeneration`; JFR clock is UTC-1h vs
  local here: cut at 06:48:29): 629,884 events, all overworld. Pregen worker time 6,639 s: noise 2,577 s 38.8%
  (27,133 calls, mean 95 ms), features 1,617 s 24.4% (21,520, 75 ms), light 1,249 s 18.8% (16,641, 75 ms),
  structure_starts 422 s 6.4% (358,852 calls over 22,801 chunks, median 0.00 ms, max 8.4 s), surface 275 s 4.1%,
  full 235 s 3.5%, biomes 195 s 2.9%, carvers 66 s 1.0%, rest ~4 s. Boot (spawn prep): 202 s, noise 45%,
  structure_starts 24% (one 11.7 s start at chunk [-1, -1]).
- structure_starts repeats: all on Worker-Main; 22,801 distinct chunks = the 129 x 129 square + an 11-chunk fringe;
  22,194 judged more than once, up to 23 times (placement-priority neighbour pass / meta prediction rebuilding nearby
  starts, seen in M7 as doubled earth-fill lines). Cheap: the repeats return in ~0 ms.
- Worker-Main by mod: minecraft/forge 75.7 (PalettedContainer#get 12%, ImprovedNoise 5%), creativeengineering 6.8
  (EarthFill predicted-terrain lambda 62%, MetaStructure#generate 11%), blueprint 3.7 (BiomeUtil
  OriginalModdedBiomeProvider#getNoiseBiome 99%), twigs 3.1 (ObeliskStructure#canGenerate 99%), copycats 2.9
  (customOcclusion), tectonic 1.7, volcanic_caverns 1.6, ferritecore 1.3. DH-LOD Builder: distanthorizons 98.3%.
- Written into locate.md 3.7 ("Measured 2026-10-19") + LO-10 ticked.

## Results

| Test | Result | Evidence |
|---|---|---|
| LO-10 | PASSED | Per-status times in locate.md 3.7: noise 38.8%, features 24.4%, light 18.8%, structure starts 6.4% |

## Open issues

1. Not bugs, leads for perf: twigs ObeliskStructure#canGenerate 3.1% of worldgen CPU; EarthFill terrain prediction
   ~4% (runs again on each repeated start build); structure starts rebuilt ~16x per chunk (cheap); DH LOD builders
   35% of CPU during pregen (server-side LODs).
2. perf_report.py: summary.json lacks the chunk-generation table (HTML only); --out wants a file name.
