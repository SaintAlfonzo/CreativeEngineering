# server-crash-triage-0929

> 2026-09-29 triage of external-server debug/ (7 crash reports 2026-09-26..2026-09-29, jar 1.0.1): campfire spawn-scan hang, DH CPU starvation, level.random race, loot-tuning/trades bugs; actions and the 1.0.2 fixes at the end

Moved from agent memory 2026-11-16 (last edited there 2026-09-29). Repo status docs win over any status line here.

Triage of `debug/` (external server, homeservera, i7-8565U 4c/8t, -Xmx6G, running 1.0.1). 7 reports = 6 incidents; 3 reports with NO "Server thread" in the dump are post-crash zombies (DH non-daemon threads keep the JVM up, watchdog kills it ~60 s later). The 2026-09-29 11:12 one means a real crash ~11:11 whose report wasn't provided.

Open at triage time (2026-09-29; actions below):
- **No Hostiles Around Campfire** (2026-09-28 19:37 watchdog): Collective `FABFunctions.getBlockEntitiesAroundPosition` calls `Level.getChunk(x,z)` for chunks `c-n..c+n-1`, n=ceil(radius/16), on EVERY hostile CheckSpawn; config radius 48 -> n=3 forces sync chunk gen at the loaded edge. Radius 32 (n=2) stays inside chunks vanilla guarantees loaded around an entity-ticking chunk. Alternatively a patcherMod mixin to skip unloaded chunks.
- **Distant Horizons server gen** (config/DistantHorizons.toml: numberOfThreads=6, threadRunTimeRatio 1.0, distantGeneratorMode FEATURES): 5-8 DH threads RUNNABLE vs <=1 Worker-Main at every hang; FEATURES mode runs mod Feature.place on DH threads.
- **level.random race** (2026-09-27 22:10 "Accessing LegacyRandomSource from multiple threads"): victim Regrowth MoveEntityEvent line 430 = `entity.level.random.nextDouble()`; the off-thread user isn't recorded (1.19.2 CAS failure only dumps the loser). Candidate fix: mixin Level.<init> RandomSource.create() -> createThreadSafe(), and/or DH SURFACE mode.

FIXED (in c6a3433, server has c566438/1.0.1): loot/05_tuning.js bare `for (k = ...)` (82 tables failed on server, 17 vs 176 rewritten); economy/trades.js top-level `i` clobbered -> "Sell item cannot be empty" after removeModdedTrades (Mechanical Engineer left with no trades). Both are memory `rhino-fieldandmethods-gotcha` shared-scope bugs.

Fixed earlier: [dnd-drill-saw-server-crash](dnd-drill-saw-server-crash.md), [dead-structure-locate-hang](dead-structure-locate-hang.md). Related: [deploy-pipeline](deploy-pipeline.md), [creengserver-dedicated-server](creengserver-dedicated-server.md).

**Actions 2026-09-29 (same day):** user removed No Hostiles Around Campfire (jar gone; I removed its config, mod_sides row, server_mods copy, regenerated pack/; Collective stays, 6 other Serilum mods need it). DH `numberOfThreads` 6 -> 2, FEATURES kept (user choice), backed up per memory `configs-edited-backup`. `mixin/LevelThreadSafeRandomMixin` redirects every RandomSource#create (m_216327_) in Level.<init> to createThreadSafe (m_216337_); built into build/libs 1.0.1 and bytecode-checked, NOT copied to mods/ (that jar also carries untested [multi-serving-cans](multi-serving-cans.md)), not boot-tested. User also added async-locator-forge-1.19.2-1.3.0.jar (server row added) - covers the /locate + explorer-map hang class.

Shipped in pack 1.0.2 (scratch-server boot passed, so the require=1 Level mixin applies).
