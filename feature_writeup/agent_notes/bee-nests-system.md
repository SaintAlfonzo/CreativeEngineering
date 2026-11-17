# bee-nests-system

> Bee nests: only from minecraft:beehive tree decorator (no bee spawns); bee_nests.json rules (chance/scale per tree via TreeFeature mixin, extra `trees`), /dumpbeenests counters, baseline calibration; status in feature_writeup/bee_nests.md

Moved from agent memory 2026-11-16 (last edited there 2026-10-10). Repo status docs win over any status line here.

Built 2026-10-10 on user request ("list of biomes beehives spawn in + frequency; system to add/boost"). Reference:
feature_writeup/bee_nests.md (BN-01..10), generated bee_nests_by_biome.md.

- Findings: bees spawn in 0 biomes; nests only via beehive decorator in Terralith/Geophilic/IW JSON (vanilla bee
  trees overridden) + Java `trees_meadow`. Terralith stripped nests from birch/dark forest, taiga, jungle, savanna.
  ~19 land biomes have nests, overworld land ~1 nest / 400 chunks. Vinery `apple_bee` is never placed.
- Offline tree counts are placement ATTEMPTS and wildly high (Terralith dark forest 214/ch est vs 9.9 measured), so
  rules are per-tree `chance`/`scale` and the generator calibrates on `bee_nests_baseline.json` (a no-rules dump).
- Java: worldgen/BeeNests (modifier creativeengineering:bee_nests, rules fold in file order, ThreadLocal tree state,
  worldgen only = WorldGenRegion so saplings untouched), TreeFeatureBeeNestsMixin (@Redirect decorators f_68187_),
  BeehiveDecoratorBeeNestsMixin (probability f_69955_ + nest count), ChunkGeneratorBeeNestsMixin (chunk count).
- `/dumpbeenests [reset]` NOT in /dumpall (would overwrite a measurement with zeros).
- Scratch-server measuring: drop DistantHorizons from the scratch mods (its server LOD gen halves Chunky speed and
  could skew counters); seed 424242 spawn is ocean, use a Chunky grid of small squares (scratchpad grid.sh pattern).
- Side finding: gen_crop_worldgen_table.placement_info / gen_wild_flowers.top_rate ignore unprefixed `"count"`
  (177 Terralith placements) and treat negative counts as real; gen_bee_nests has its own fixed rate_of. Not fixed there.

- Crash found on scratch server: Terralith leaf-only "trees" (birch/trees_forest_negative) + vanilla BeehiveDecorator
  logs().get(0) -> worldgen crash; AddedBeehiveDecorator now needs logs AND leaves. Fixed jar (13:52) verified on a
  9-square check (4,301 chunks, no crash). 6 starter rules in bee_nests.json (land 1/785 -> 1/134), awaiting user review.

**How to apply:** tune bee_nests.json, rerun the generator; refresh the baseline only with rules emptied.
Related: [mob-spawns-system](mob-spawns-system.md), [vegetation-worldgen-system](vegetation-worldgen-system.md), memory `scratch-server-verify`, memory `feature-writeup-pattern`.
