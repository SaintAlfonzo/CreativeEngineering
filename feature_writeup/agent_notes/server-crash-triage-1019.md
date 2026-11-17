# server-crash-triage-1019

> 2026-10-19 triage of debug/10_19/{1,2}: both watchdog kills = heap full (169-201 MiB free of 6G) + 2.1 GB swap on the ~8 GB host; stack frames are bystanders

Moved from agent memory 2026-11-16 (last edited there 2026-10-19). Repo status docs win over any status line here.

debug/10_19/1 (12:35, Spider tick in getOnPos) and debug/10_19/2 (12:58, /tp -> Quark SpiralSpiresModule.onTeleport getBlockState -> sync gen of the ungenerated destination chunk) are both 60 s ServerHangWatchdog kills. Real cause: memory. Crash-report System Details show 201/169 MiB free of a 6144 MiB heap (the 2026-10-18 crashes had 700-1000 MiB free), swap used 2150 MB of 7911, CommitLimit 11745 MB, which puts host RAM at ~7.5 GB, with Committed_AS 9.7 GB. One player flying elytra at ~10 blocks/tick into fresh terrain, 5022 chunks loaded, DH server gen at FEATURES / 2 threads / maxGenerationRequestDistance 4096. Run 2 died 19 min after boot. "Can't keep up" stalls climbed to 13-45 s before each kill. The 20 DH "Failed to decode message" errors in log 1 are shutdown fallout. Not yet confirmed by GC events: the server's JFR exit dump (perf/exit) wasn't uploaded.

Proposed (none applied as of 2026-10-19): turn DH distant generation off on the server, or cut its distance and threads; pre-generate with Chunky; view-distance 10 -> 8; get the JFR dump to confirm. Done 2026-10-19: mixin/QuarkSpiralSpiresTeleportMixin cancels SpiralSpiresModule.onTeleport outside the End (patcherMod 1.0.9 rebuilt + copied to mods/, not boot-tested).

Related: [server-crash-triage-0929](server-crash-triage-0929.md), [perf-profiling-jfr](perf-profiling-jfr.md), [perf-known-issues-fixes](perf-known-issues-fixes.md) (an earlier G1Full death spiral seen in a client profile).
- 2026-10-19 later: QuarkSpiralSpiresTeleportMixin scratch-tested (ST-205): Overworld armor-stand /tp to 300016 100 300016 took 4 ms, no chunk generated; in the End (hook kept) the same tp stalled the server 38 s and generated the target. Offered (not done): skip the hook when the target chunk isn't loaded, all dimensions.
