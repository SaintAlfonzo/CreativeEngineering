# locate-writeup

> feature_writeup/locate.md (LO-): locate misses, prediction folding, non-blocking loads, StructureCheck race, /cwglocate batch+census, deep-band deferral; current status and tests live in that file

Moved from agent memory 2026-11-16 (last edited there 2026-10-13). Repo status docs win over any status line here.

User, 2026-10-12: put the locate findings and plans in a new writeup, don't implement. Wants: as many rules as possible folded into the "could it start here?" prediction (biomes first), a cheap misses-before-hit log, `/cwglocate batch` + `census` run through a multithreaded Async Locator, testing with more than 1 locator thread, and an investigation of deferring the bottom 64 to 96 blocks.

Key facts found (1.19.2 + AL 1.3.0, decompiled):
- Vanilla's prediction (StructureCheck#canCreateStructure) is only findGenerationPoint: no biome test, no set frequency, no exclusion_zone. The search uses getPotentialStructureChunk, not isStructureChunk.
- Under AL, a miss's getChunk is still handed to the server thread, which blocks in managedBlock. Fix idea: getChunkFuture + join on the locator thread.
- StructureCheck maps are plain HashMaps. Already racy with 1 AL thread; needs a synchronized mixin before asyncLocatorThreads > 1.
- AL catches no exceptions: a throwing search never completes.
- Rule order to fold: A set freq/exclusion, B biome (generalize MetaSlots.fits), C conservative group pick, D surface check (jigsaw only), E earth fill/priority via PlacementPriority's predictor (switchable). Exact or conservative only. Census false negatives must be 0.
- Deep band: measure with JFR ChunkGeneration first. Then a DH-only skip (FEATURES mode, cave culling 60) and Chunky pregen. A real retrogen band is not recommended.

Related: memory `async-locator-everywhere`, [plan-7-1-execution](plan-7-1-execution.md), [placement-priority](placement-priority.md), [meta-structures](meta-structures.md), [perf-profiling-jfr](perf-profiling-jfr.md).

- 2026-10-12 decisions (locate.md section 4): fold stages A (set freq, exclusion) + B (biome) only; C-E on hold. Build 3.2-3.6. DH generation clipped below y 48 with a fake seabed: floors hover (smooth seeded noise +-4) around y 8 in deep oceans and y 48 in other oceans, rest stone; no predicted surface (user likes the depth contrast); real chunks untouched. Final server world gets a Chunky pregen. Retrogen (option C) not built. Tests LO-01..12.
- 2026-10-12 BUILT: worldgen/LocatePrediction (stage A in StructureCheckFrequencyMixin HEAD, stage B via MetaSlots.predictGenerationPoint plain branch; MetaSlots.fits moved here), LocateLoads (ticket level 33+dist(STRUCTURE_STARTS), getChunkFuture, falls back to vanilla), StructureCheckThreadSafeMixin (maps swapped at <init>, prediction outside lock), LocateStats (+census reasons from earth fill/surface/priority/frequency/veto/meta mixins, tryGenerateStructure RETURN), LocateCensus, LocateCommand (/cwglocate batch|census|stats|status|stop), compat/AsyncLocatorBridge (pool via reflection), DistantClip + 3 mixins (mark in createNoiseChunk/fillFromNoise by StructureManager level class name DhLitWorldGenRegion; clamp NoiseSettings in fillFromNoise and NoiseChunk.forChunk; fill band at doFill RETURN). Config [worldgen.locate] + [worldgen.distant]. LocateTest = test_worldgen_tools.sh step 8.
- Gotcha: @Accessor/@Invoker with SRG names fail the mixin AP ("Could not locate @Accessor target"); use reflection (LocateReflect, ObfuscationReflectionHelper + mojmap fallback). @Shadow with SRG names compiles fine.
- Gotcha: vanilla's biome step creates and caches the NoiseChunk before fillFromNoise, so a noise clip must mark the chunk at createNoiseChunk too, or sampler and fill disagree on cell indices.
- 2026-10-14 Phase 5 (05_phase_5.md): folds proven exact (same positions on/off), batch/stats/stop OK. LO-05 FAILED: LocateLoads.offThread calls getChunkFuture INSIDE a server task; on the server thread getChunkFuture managedBlocks until the chunk is done, so "non-blocking" loads stall ticks like vanilla (max gaps 16 s). Fix: add ticket in the task, call getChunkFuture from the search thread (or getChunkFutureMainThread in the task). Not fixed; 1.0.7 candidate, user decides. Also /cwglocate stop labels never-started rows "error: CancellationException".
- 2026-10-14 Phase 6: CENSUS BUG: LocateCensus.enumerate lists a region's slots set by set; runRegion predicts then loads per slot, so the first set at a chunk (buried_treasures, every chunk) loads it and all later sets count "known" (142971/179710 on fresh land). Fix: group slots by chunk, predict all sets, load once. "built"/shares are valid. Not fixed.
