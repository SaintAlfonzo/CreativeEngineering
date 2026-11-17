# first-tick-postprocess-water

> 1.19.2 LevelChunk.postProcessGeneration runs when a chunk first becomes TICKING (not at FULL/pregen) and ticks aquifer water marks; cause of cave plants dropping as items; how to scan saves for it

Moved from agent memory 2026-11-16 (last edited there 2026-10-27). Repo status docs win over any status line here.

- `ChunkMap.prepareTickingChunk` -> `LevelChunk.postProcessGeneration` (SRG m_62812_): ticks each fluid in `PostProcessing`
  (about 900 marks per chunk, mostly aquifer / carver water) plus saved spring `fluid_ticks`. Pregen (Chunky) leaves them
  pending, so all the flooding happens when a player first comes near. Flowing water washes cave plants out with drops.
- Fix built 2026-10-27: Creative Common `worldgen/FirstTickWater`: during a configurable settling window
  (`[worldgen.first_tick_water] windowTicks`) the water flows as in vanilla but `Block.popResource` (private Supplier
  overload, SRG m_152440_, every block drop) is skipped. The owner rejected the first version (water flowing around
  plants): **why:** they want vanilla water movement, only no item litter. Status: perf.md 10.6, TODO `perf #14`.
- Offline evidence method: parse region files of a pregenerated world, filter Status full + InhabitedTime 0, read
  `PostProcessing` / `fluid_ticks` / `block_ticks` and decode section palettes (bits = max(4, bitlen(len-1)), no long
  spanning). WorldG was the usable world; Workspace has near-empty region files.
- SRG ids: FlowingFluid tick m_6292_, spreadTo m_6364_, canHoldFluid m_75972_ (unused now). Obf names are all `a`: match by descriptor
  (cgx Level, cgy LevelAccessor, cgd BlockGetter, gt BlockPos, cvo BlockState, dpv FluidState, dpu Fluid, gy Direction).

Related: [earth-fill-system](earth-fill-system.md), [perf-known-issues-fixes](perf-known-issues-fixes.md).
