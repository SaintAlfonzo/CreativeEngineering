# creativebrewing / bundle: implementation brief

Written 2026-10-31 from [TODO](../todo/TODO.md) `# Creative Brewing`, Sub `bundle` (row 2 open at Testing). Feature
writeup: [creative_brewing.md](../creative_brewing.md) (test prefix `CBR-`, last CBR-05; scratch `SQ-CBJ-`). The moves
that use the bundle are briefed in [creativebrewing_move.md](creativebrewing_move.md).

## Scope and order

| Step | Row | Work | Notes |
|---|---|---|---|
| - | `bundle #2` bundle jar nesting Vinery and Brewin', framework for the moves | none left to build | Built and installed; needs the scratch boot and play (table below) |

State checked 2026-10-31: `mods/creative_brewing-1.0.0.jar` (16:55, same as `development/creativeBrewing/build/libs/`,
no source newer than it) is installed; `backportedVinery-1.5.5-ce-19.2-06.jar` and `backportedBrewin-4.5.0-ce-19.2-04.jar`
are in `development/creativeBrewing/jar_archive/displaced/`, not in `mods/`. `mod_sides.md` 437 has the Creative Brewing
row. Changelog bullets for the new names are in `unreleased.md` (lines 70-71). The writeup's status line ("not
installed", creative_brewing.md top and 5) and the TODO row's "Not installed" predate the install.

## Built, needs play

| Row | Change | Open tiers | Tests | Writeup |
|---|---|---|---|---|
| bundle #2 | `creative_brewing-1.0.0.jar` nests Vinery `-08` and Brewin' `-05`; `global.CB`, `creative_brewing-common.toml`, `CBMixinPlugin`, script trees; installed 2026-10-31 16:55, not booted | S C | SQ-CBJ-01 to -05; CBR-01 to CBR-05 in game | [creative_brewing.md](../creative_brewing.md) 0-2, 5, 6 |
