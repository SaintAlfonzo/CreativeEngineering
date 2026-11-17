# biome-lookup-ties

> Vanilla multi-noise biome lookup is thread-history dependent on ties (Terralith deep_caves vs deep_dark); predictions must accept every tied biome

Moved from agent memory 2026-11-16 (last edited there 2026-10-28). Repo status docs win over any status line here.

`Climate.RTree#search` (1.19.2) starts from a per-thread `lastResult` and keeps it when another entry is equally near.
Where parameter boxes overlap at the same offset, `getNoiseBiome` at one point returns different biomes on different
threads. In this pack: Terralith `cave/deep_caves` and `minecraft:deep_dark` (depth 1.05-1.2, erosion < -0.375, both
offset 0). This caused the census false negatives for `terralith:underground/old_refinery` / `mining_outpost` (found
2026-10-27).

**Why:** any "certain no" computed from one biome sample (locate prediction, biome-factor rolls) can disagree with
the worldgen thread.
**How to apply:** for prediction-side biome tests use `worldgen/TiedBiomes.any` (walks the parameter list only on a
fail). Never "fix" generation itself (that would change worldgen). Generation-side checks (`MetaStructure` biome tests)
stay single-sample. Related: [locate-writeup](locate-writeup.md), [structure-gen-framework](structure-gen-framework.md).
