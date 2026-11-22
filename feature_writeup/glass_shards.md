# Glass shards: every glass block and pane breaks into shards of its color

Ships in Creative Common 1.3.0 ([creative_engineering.md](creative_engineering.md) 7.6).

Test prefix `GSH-`. TODO `gameplay/misc #22`; plan [plan_wood_bucket_batch_1121.md](plan_wood_bucket_batch_1121.md) R3, D1.

## 0. At a glance

<!-- generated:status_index begin open_work sha1=ba133888da4a -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 0 open TODO rows (none), 0 Complete.
<!-- generated:status_index end open_work -->

| Part | Status |
|---|---|
| Global loot modifier `creativecommon:glass_shards` (Creative Common `loot/GlassShardsModifier`) | BUILT 2026-11-21 (Creative Common) |
| Definition and `glm_entries.json` fragment (read by Creative Loot's GlmMerge) | BUILT 2026-11-21 |

- **Files to open:** [GlassShardsModifier.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/loot/GlassShardsModifier.java),
  [glass_shards.json](../development/creativeCommon/src/main/resources/data/creativecommon/loot_modifiers/glass_shards.json).
- **Knobs:** `block_min` / `block_max` (1 to 3) and `pane_min` / `pane_max` (1 to 2) in the definition; data reload.
- **Needs:** Creative Loot (GlmMerge reads the fragment) and Quark (the shard items). Without Quark nothing changes.

## 1. Overview

Quark's Glass Shard module (on, `config/quark-common.toml` "Glass Shard") overrides the loot tables of vanilla glass,
the 16 stained glass blocks and its dirty glass: 2 to 4 shards of the color without Silk Touch (fortune bonus, capped at
4, explosion decay). Panes and every modded glass still break into nothing. The pack extends shards to every block in
`#forge:glass` and `#forge:glass_panes` (owner R3: all glass, modded included).

## 2. Reference

Rule, per block break (loot context with a block state, the block's own table):

1. The block is glass when its block or its item is in `#forge:glass` or `#forge:glass_panes` (114 blocks and 131
   panes in the 2026-11 dump: Create, Create Framed, Botania, Quark, vanilla and more). A pane is in `#forge:glass_panes`.
2. Silk Touch on the tool: unchanged (the table gives the block).
3. The drops already hold a shard (`#quark:shards`) or the block's own item: unchanged. This keeps Quark's tables for
   vanilla glass (no double drop) and leaves glass that always drops itself (framed glass that is not silk-only) as it
   is. Exception, tinted glass (`#forge:glass/tinted`, `#forge:glass_panes/tinted` or `tinted` in the id): there is no
   tinted shard, so it loses its self-drop and gives Quark's dirty shard, same counts (owner, 2026-11-21).
4. Otherwise: block 1 to 3 shards, pane 1 to 2. Color from `#forge:glass/<color>` or `#forge:glass_panes/<color>`,
   else a color word in the id (`red_framed_glass` -> red, longest color name first so `light_blue` wins over `blue`),
   else the clear shard. Explosions keep each shard at 1 in radius.

Examples: red stained glass pane -> 1 to 2 red shards. Create Framed `horizontal_red_stained_framed_glass` (if silk-only)
-> 1 to 3 red shards. Create `oak_window_pane` -> clear shards. Vanilla glass -> Quark's 2 to 4 clear shards (unchanged).

## 3. What the pack changes

- Creative Common registers the codec `creativecommon:glass_shards` (`GLOBAL_LOOT_MODIFIER_SERIALIZERS`).
- `data/creativecommon/loot_modifiers/glass_shards.json` holds the counts; `data/creativecommon/glm_entries.json` lists
  it (plan_rules_fixes.md section 5: the pack's jars never ship the Forge list).

## 4. Limits and decisions

- D1 counts (owner confirmed 2026-11-21, R13). Quark's own 2 to 4 for vanilla glass is kept rather than overridden, so
  vanilla stained glass gives more than modded glass. Rejected: replacing Quark's drops (an override of 17 vanilla
  tables or a GLM that strips them) for one number.
- No fortune bonus on the added shards.
- Glass that already drops itself without Silk Touch keeps that drop (a shard would be a loss). Ask the owner if those
  should break into shards too.
- Quark's "Glass Shard" switch off still leaves the shard items registered, so the modifier keeps adding them.

## 5. File map

- [loot/GlassShardsModifier.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/loot/GlassShardsModifier.java), registered in `CreativeCommon.java`.
- [loot_modifiers/glass_shards.json](../development/creativeCommon/src/main/resources/data/creativecommon/loot_modifiers/glass_shards.json), [glm_entries.json](../development/creativeCommon/src/main/resources/data/creativecommon/glm_entries.json).
- GlmMerge: [creative_loot.md](creative_loot.md).

## 6. Offline checks

`python3 tools/design_rules_audit.py glm` (every fragment entry has a definition).

## 7. In-game test suite

Queue: SQ-GSH-01 to SQ-GSH-07 ([rows/GSH.qb](testing/rows/GSH.qb)), one row per test.

- [ ] **GSH-01 Merge:** boot -> the `[GlmMerge]` line counts one more fragment entry; no `skipped: creativecommon:glass_shards`.
- [ ] **GSH-02 Modded pane:** break a Create Framed red stained framed glass pane by hand -> 1 to 2 red shards.
- [ ] **GSH-03 Modded block, no color:** break a Create framed glass (or a Botania mana glass) -> 1 to 3 clear shards,
  unless the block drops itself.
- [ ] **GSH-04 Vanilla pane:** break a blue stained glass pane -> 1 to 2 blue shards; a plain glass pane -> clear shards.
- [ ] **GSH-05 No double:** break vanilla glass and red stained glass -> only Quark's 2 to 4 shards, no second stack.
- [ ] **GSH-06 Silk Touch:** Silk Touch pickaxe on each of the above -> the block, no shards.
- [ ] **GSH-07 Tinted:** break tinted glass by hand -> 1 to 3 dirty shards, no tinted glass; a Create Framed tinted pane ->
  1 to 2 dirty shards; Silk Touch -> the block.

## 8. History

- 2026-11-21: built (plan_wood_bucket_batch_1121.md P2 item 3), Creative Common jar.
