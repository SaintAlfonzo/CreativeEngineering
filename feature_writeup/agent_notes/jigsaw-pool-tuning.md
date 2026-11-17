# jigsaw-pool-tuning

> Village-house (jigsaw pool piece) remove/weight system — pool_config.json + pool_tags.json applied by Creative Common PoolTuner at ServerAboutToStart LOWEST; plus loot_container_swaps.json so Lootr converts modded structure containers

Moved from agent memory 2026-11-16 (last edited there 2026-09-27). Repo status docs win over any status line here.

Built 2026-09-26 because mod village houses (IE, Create: Dynamic Village, PNC, AP...) aren't
structures. They're weighted pieces that mods inject in code into vanilla pools like
`minecraft:village/plains/houses`, so structure_config.json can't see them.

- `Creative Common worldgen/PoolTuner`: `ServerAboutToStartEvent` at LOWEST priority, after all injectors, which run at NORMAL
  priority or at mod init / through a Pools.register mixin. It reads live pools, merges `kubejs/generation_scripts/worldgen/pool_config.json`
  (game-written, keeps current/enabled edits, keeps edited-but-missing entries as `"missing": true`), resolves
  `pool_tags.json` (structure_tags syntax + optional `"pools"` glob filter), rebuilds changed pools
  (rawTemplates + templates + maxSize reset), and writes `pool_tag_report.md` with an "injected by other mods" section.
  Any error applies nothing. Fractional weights: the pool is scaled by up to x10, then gcd-reduced.
- Singleplayer: builtin pools are the same objects across world loads, and DV/PNC etc. re-append pieces on every load. A WeakHashMap
  snapshot of the first-seen weights per pool object prevents compounding and drops re-added duplicates.
- Piece key = template RL, found by field TYPE (Either / Holder<PlacedFeature>) so YUNG/MES subclasses work; `list:[..]`,
  `minecraft:empty`, `" #2"` for duplicate templates. Pool fields use SRG names with a Mojmap fallback (headless tests).
- `LootContainerSwaps` + `mixin/StructureTemplateLootSwapMixin` (@Inject RETURN on Forge's 6-arg `processBlockInfos`, a
  Forge-added method, so it has no SRG name): a listed block placed WITH a LootTable becomes a vanilla chest/barrel, which Lootr
  converts. Lootr only converts blocks in its replacement map and only RandomizableContainerBlockEntity. PNC's
  reinforced chest isn't one. Defaults: IE crate, FD cabinets and basket -> barrel; PNC reinforced_chest -> chest.
- `tools/audit_loot_containers.py` -> `loot_container_audit.md`: 2226 loot containers, and only Nullscape phantom-hive
  droppers + monobank bank blocks remain unconverted (left alone on purpose).
- Weights: DV houses are 20 EACH, vanilla houses 2-4, IE 1.
- Headless test (32 checks: tags, pools filter, fractional scaling, re-injection, error restore, empty pool,
  broken-json no-clobber, stale entries, swaps) passed using the memory `patchermod-headless-smoke-test` recipe.
  Built on 2026-09-26. **Running in game as of 2026-09-27** (report rewritten at world load: 645 pools / 3669 pieces / 12 retuned; ST-11 passed). PoolTuner logs no line of its own, the report timestamp is the evidence. Writeup: `feature_writeup/structure_tuning.md`.

Related: [structure-gen-framework](structure-gen-framework.md), [patchermod-coremod](patchermod-coremod.md).
