# placement-priority

> patcherMod PlacementPriority: ranked groups, lower start dropped if it would overlap a higher one (predicted, order-independent); companions = whole starts around a meta (town_villages); status in feature_writeup/structure_tuning.md

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Built 2026-10-12 on request ("implement order b ... bigger settlements win over villages ... town+village"). Reference: feature_writeup/structure_tuning.md 3.22, tests ST-140..146.

- Ranks: structure_groups.json `priority` (groups + `#priority/big_settlements` 100). JS stages {ranks, companions} to PlacementPriority.stage.
- Filter: ModifyVariable on tryGenerateStructure's StructureStart (slot 15) -> predicts higher-ranked sets' slots within 8 chunks by replaying the set's weighted pick on a scratch ProtoChunk via an invoker (ChunkGeneratorPlacementPriorityMixin implements PlacementPriority.Generator); 3D piece-box overlap -> INVALID_START. Cache per (generator, set, chunk); depth cap 3; isolated() saves/restores StructureFrequencies VETO/ANCHOR + MetaSlots FORCED.
- Companions: createStructures (m_223164_) RETURN; companion mode skips frequency/veto and MetaSlots conversion; rank = group rank - 1. meta/town_villages has empty parts (allowed only with companions).
- Shaping (same day): priority.shape groups (settlements) -> PlacementPriority.shape cuts predicted higher boxes out of the jigsaw free space (JigsawPlacementPriorityMixin, ModifyVariable argsOnly at m_227210_ HEAD; lambda$addPieces has no SRG entry, so hook the named assembler). CURRENT threadlocal set at tryGenerateStructure HEAD, saved in isolated().
- Gotchas: non-jigsaw / YUNG jigsaw losers still drop whole; companions invisible to /locate and way signs; estimates exclude companions/drops.
- Existing overlap rate noted as an estimate (~1/3 of villages), not measured.

Related: [meta-structures](meta-structures.md), [structure-groups](structure-groups.md), [plan-7-1-execution](plan-7-1-execution.md).
- Peaceful radius (2026-10-12, same day): structure_groups.json priority.peaceful {groups, structures, hostile}; big settlements 8 / settlements 5 / remote 3 chunks, square XZ from piece boxes; hostile = pillager_settlements, surface_dungeons, witch_huts, entrance_dungeons (65). Hostile start predicts peace sets + companions within radius+8 and drops (dropPeace log "within N chunk(s) of peaceful"); peaceful structures skip hostile ones in higher() so the checks never predict each other. JS planPeaceful, staged as "peace". MetaTest 70 (withinRadius). 1.0.6 jar rebuilt 22:45, not in mods/. ST-153..157.
