# r1102b worldgen slice: offline rows

## SQ-WGE-00: FAIL (one clause), 2026-11-02 14:4x, batch writer

| Check | Result |
|---|---|
| `bash tools/test_pool_merge/run.sh` | ALL PASS, 6 ok lines |
| `python3 tools/gen_structure_groups.py --check` | passes, no warning (it rewrites `structure_group_estimates.md` by design) |
| `python3 tools/changelog_lint.py` | 0 errors, 54 warnings, none for `unreleased.md` (all bullet-length warnings in deployed files) |
| JSON load of pool_config / structure_groups / structure_tags / biome_config | all load |
| `diff config/waystones-common.toml configs_edited/root_config/waystones-common.toml` | empty |
| `diff config/creativeengineering-common.toml configs_edited/root_config/creativeengineering-common.toml` | **not empty**: 127 lines, all `<` (only in `config/`), no `>` |

The CE diff is four blocks that exist only in the live `config/` file: `seasonsMeltIntervalTicks`, `[seasons]`,
`[time]` (with `[time.day]`, `[time.seasons]`, `[time.wine]`), the `cropSeasons` / `cropOutOfSeason` tooltip keys and
`[crop_seasons]`. All of them are now in `config/creativecalendar-common.toml` (Creative Split wave 1, Creative
Calendar). The backup was cleaned; the live file still carries the orphaned sections. No worldgen value differs.
Fix (owner): delete those blocks from `config/creativeengineering-common.toml` (the mod no longer reads them), or
re-copy the cleaned file over it, then the diff is empty. Not done here (config/ is out of this agent's scope).

Not run here (they are rows in w_1_B.qb): SQ-EFB-00, SQ-EFB-17 and SQ-PSW-00. Seen while checking the output format
for the batch regexes: `tools/test_worldgen_tools.sh` does not compile today (`EarthFillTest.java:424` declares
`basin` twice; `BiomeLayoutCacheTest` and `MassPlacerTest` import `worldgen.WorldgenFiles`, now
`creativeengineering.util.WorldgenFiles`), so SQ-EFB-00 and -17 will FAIL until the tests are fixed.
