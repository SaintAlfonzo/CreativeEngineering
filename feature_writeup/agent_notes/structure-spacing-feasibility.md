# structure-spacing-feasibility

> Feasibility rounds: edge-distance predictor rejected (needed higher frequencies), user chose merged group grids (structure-groups); meta structures (composite 8-chunk cap, satellites beyond) were pinned for later

Moved from agent memory 2026-11-16 (last edited there 2026-09-28). Repo status docs win over any status line here.

User asked (2026-09-27) whether we can force minimum edge distances between structure groups (village/village,
village/ruin-tower-camp, T&T town/fort vs village, underground/underground, cloud/cloud) and encourage grouping
(cloud clusters, villages just outside towns, towers near villages, same-type ruin clusters). Outcome: the user picked shared group grids, built the same day as [structure-groups](structure-groups.md); meta structures are pinned for later (plan in feature_writeup/structure_tuning.md section 4).

Verified facts (javap, 1.19.2 official mappings):
- Vanilla `exclusion_zone` = ONE other set, checked via `hasStructureChunkInRange` -> `isStructureChunk`
  (grid+frequency only, no biome check) so it over-suppresses; start chunk, not edge.
- `ChunkGenerator.lambda$createStructures$18`: multi-member set = weighted pick with WorldgenRandom
  setLargeFeatureSeed, falls through to the next member when `tryGenerateStructure` fails.
- Jigsaw pieces are assembled lazily (JigsawPlacement.addPieces lambda); max_distance_from_center <= 128.
- `StructureCheck.canCreateStructure` (private) is the /locate + explorer-map path; hook it too.
- Cloud Storage structures are custom types (not jigsaw), sets 96/72, so same-set clouds are already >= 72 chunks apart.
- Pack gap: towns_and_towers:towns excludes minecraft:villages but NOT themissingvillages' minecraft:village set.

**User REJECTED raising any structure frequency** (the current tuning "works pretty great"). Preferred direction
(same day): additive META STRUCTURES = new structures composed of existing ones (2-4 sky temples, balloon + temples,
fort + 3-5 towers, fort + 1-2 villages, camp/ruin bunches), each with its own set/tuning, plus removal-only
"separation groups" for min edge gaps between dissimilar types. Verified: createReferences range is 8 chunks, so a
single-StructureStart composite must fit within ~8 chunks of its start chunk (clouds 7-17 blocks, T&T forts about 35 core,
towers 13-29 all fit; fort + full villages (jigsaw radius 80) does NOT, so it needs anchor + satellite starts via a custom
placement). findNearestMapStructure only handles ConcentricRings/RandomSpread placements. Cloud Storage's
SkyTempleStructure builds from context.chunkPos + random, so it can be re-run at an offset. "Clouds" = cloudstorage:sky_temple.

Earlier design (frequency-raising part rejected): a pure-function predictor (mirrors the vanilla selection, cached, thread-safe for DH), tiers so
lower tiers only check higher ones, Matérn type-II thinning within a tier, hash-based cluster centers for grouping,
and a /structuremap preview command as the tuning tool and test oracle. Only new chunks are affected.
Related: [structure-gen-framework](structure-gen-framework.md), [jigsaw-pool-tuning](jigsaw-pool-tuning.md), [dead-structure-locate-hang](dead-structure-locate-hang.md).

2026-10-11: meta structures built as single-start composites, see [meta-structures](meta-structures.md).
