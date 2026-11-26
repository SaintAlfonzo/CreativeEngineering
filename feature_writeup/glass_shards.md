# Glass shards: every glass block and pane breaks into shards of its color

Creative Compat (moved out of Creative Common 2026-11-24, wave 2 carve, TODO `creative-common-publish #17`; ruling P1 of `#12`, the Forge list entry in Common, is superseded: the entry ships in Compat). See [creative_compat.md](creative_compat.md).

Test prefix `GSH-`. TODO `gameplay/misc #22`; plan [plan_wood_bucket_batch_1121.md](plan_wood_bucket_batch_1121.md) R3, D1.

## 0. At a glance

<!-- generated:status_index begin open_work sha1=faae0af58cce -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 1 open TODO row (Testing 1), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `creative-common-publish #12` | Testing | S | Glass shards loot modifier: ship the Forge list entry in Common or move to Creative Loot |
<!-- generated:status_index end open_work -->

| Part | Status |
|---|---|
| Global loot modifier `creativecompat:glass_shards` (Creative Compat `loot/GlassShardsModifier`) | BUILT 2026-11-21 (Creative Common); moved to Creative Compat 2026-11-24 |
| Definition and `glm_entries.json` fragment (read by Creative Loot's GlmMerge) | BUILT 2026-11-21 |
| Forge list entry in Compat (`replace: false`; was in Common until 2026-11-24), so it loads without Creative Loot (PR-CC-03, P1) | BUILT 2026-11-22 |
| Shards in 12 mid-tier chest tables (Creative Loot `07_rules.js`, P1) | BUILT 2026-11-22 |

- **Files to open:** [GlassShardsModifier.java](../development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/loot/GlassShardsModifier.java),
  [glass_shards.json](../development/creativeCompat/src/main/resources/data/creativecompat/loot_modifiers/glass_shards.json).
- **Knobs:** `block_min` / `block_max` (1 to 3) and `pane_min` / `pane_max` (1 to 2) in the definition; data reload.
- **Needs:** Quark (the shard items). Without Quark nothing changes. Creative Loot is optional: Compat ships the Forge list entry, and GlmMerge keeps the entry when another jar's list replaces Forge's.

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

- Creative Compat registers the codec `creativecompat:glass_shards` (`GLOBAL_LOOT_MODIFIER_SERIALIZERS`).
- `data/creativecompat/loot_modifiers/glass_shards.json` holds the counts. Two lists name it: Forge's
  `data/forge/loot_modifiers/global_loot_modifiers.json` with `replace: false` (so Compat alone loads it; ruling P1
  (owner, 2026-11-22), an exemption from plan_rules_fixes.md section 5 and the DR-P15 lint) and the fragment
  `data/creativecompat/glm_entries.json` (Creative Loot's GlmMerge; a duplicate id is dropped).
- Chest loot (Creative Loot, ruling P1): `loot/07_rules.js` adds one pool to 12 mid-tier vanilla structure chests
  (simple dungeon, mineshaft, desert pyramid, jungle temple, pillager outpost, ruined portal, shipwreck supply, big
  underwater ruin, igloo, woodland mansion, stronghold corridor and crossing): 30% per chest, 1 to 3 shards, weights
  dirty 20, clear 14, each of the 16 colors 1 (colored about one roll in three). The table list is the agent's pick of
  "mid tier" (between village houses and treasure rooms); the owner may widen it.

## 4. Limits and decisions

- D1 counts (owner confirmed 2026-11-21, R13). Quark's own 2 to 4 for vanilla glass is kept rather than overridden, so
  vanilla stained glass gives more than modded glass. Rejected: replacing Quark's drops (an override of 17 vanilla
  tables or a GLM that strips them) for one number.
- No fortune bonus on the added shards.
- Glass that already drops itself without Silk Touch keeps that drop (a shard would be a loss). Ask the owner if those
  should break into shards too.
- Quark's "Glass Shard" switch off still leaves the shard items registered, so the modifier keeps adding them.

## 5. File map

- [loot/GlassShardsModifier.java](../development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/loot/GlassShardsModifier.java), registered in `CreativeCompat.java`.
- [loot_modifiers/glass_shards.json](../development/creativeCompat/src/main/resources/data/creativecompat/loot_modifiers/glass_shards.json), [glm_entries.json](../development/creativeCompat/src/main/resources/data/creativecompat/glm_entries.json).
- [global_loot_modifiers.json](../development/creativeCompat/src/main/resources/data/forge/loot_modifiers/global_loot_modifiers.json) (Forge list).
- Chest pool: [07_rules.js](../kubejs/data/creativeloot/kubejs/loot/07_rules.js) (glass shards block).
- GlmMerge: [creative_loot.md](creative_loot.md).

## 6. Offline checks

`python3 tools/design_rules_audit.py glm` (every fragment entry has a definition).

## 7. In-game test suite

Queue: SQ-GSH-01 to SQ-GSH-12 (-10 Compat alone, -11 with Creative Loot, -12 Common alone: no modifier; [rows/GSH.qb](testing/rows/GSH.qb)), one row per test.

- [ ] **GSH-01 Merge:** boot -> the `[GlmMerge]` line counts one more fragment entry; no `skipped: creativecompat:glass_shards`.
- [ ] **GSH-02 Modded pane:** break a Create Framed red stained framed glass pane by hand -> 1 to 2 red shards.
- [ ] **GSH-03 Modded block, no color:** break a Create framed glass (or a Botania mana glass) -> 1 to 3 clear shards,
  unless the block drops itself.
- [ ] **GSH-04 Vanilla pane:** break a blue stained glass pane -> 1 to 2 blue shards; a plain glass pane -> clear shards.
- [ ] **GSH-05 No double:** break vanilla glass and red stained glass -> only Quark's 2 to 4 shards, no second stack.
- [ ] **GSH-06 Silk Touch:** Silk Touch pickaxe on each of the above -> the block, no shards.
- [ ] **GSH-07 Tinted:** break tinted glass by hand -> 1 to 3 dirty shards, no tinted glass; a Create Framed tinted pane ->
  1 to 2 dirty shards; Silk Touch -> the block.
- [ ] **GSH-08 Without Creative Loot:** Compat and Quark only -> GSH-04 and GSH-07 give the same shards.
- [ ] **GSH-09 Chest loot:** 40 rolls of the simple dungeon table -> dirty and clear shards in about 12, colored fewer.

## 8. History

- 2026-11-21: built (plan_wood_bucket_batch_1121.md P2 item 3), Creative Common jar.
- 2026-11-24: moved to Creative Compat with the serializer id, definition, Forge list and fragment (wave 2 carve, TODO `creative-common-publish #17`); Common ships none. Old saves hold no id of it (loot modifiers are data).
- 2026-11-22: Forge list entry in Common and the mid-tier chest pool in Creative Loot (publish_readiness.md PR-CC-03, ruling P1).

Scratch run r1126q (2026-11-26, [scratch_run_2026-11-26.md](testing/scratch_run_2026-11-26.md) 7.2): SQ-GSH-08 PASS (loads without Creative Loot, shard loot expects matched, no GlmMerge).
