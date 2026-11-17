# display-delight-icons

> Display Delight is the pack's 1.7.0 port (backportedDisplay); links by block name; pack DD tools go through tools/display_delight_jar.py; status in feature_writeup/display_delight.md

Moved from agent memory 2026-11-16 (last edited there 2026-10-26). Repo status docs win over any status line here.

Since 2026-10-25 the pack runs its own port of Display Delight 1.7.0 (`development/creativeDelight/members/display`, a CD member since 2026-11-02, mod id
`displaydelight`, upstream package `com.jkvin114.displaydelight` kept because patcherMod's DisplayDelightDishes links
against `FoodBlock` and `BlockAssociations.addToMap`). Status and tests (DDP-): feature_writeup/display_delight.md.

Gotchas:
- 1.7.0 links a display block to its food by block NAME (prefix -> mod, then plated_/small_plated_), not by loot table.
  `md_` is remapped to `miners_delight`. Food blocks have no loot tables; a block that has one (patcherMod dishes) drops
  only that (port guard against double drops).
- Resources come from `tools/port_resources.py` in the mod folder (also writes `DisplayFoods.java` from
  `tools/display_blocks.json`); it leaves out blocks for absent mods (EXCLUDED_PREFIXES) and for foods in
  `removed_items.js`. Rerun it after adding a mod or removing a food.
- Pack DD generators (names, models, placeable catalog, dd dishes) find the jar and links via `tools/display_delight_jar.py`.
  The old 92 kubejs model overrides for 1.2.0 were deleted; `gen_display_delight_models.py` now prunes.
- `tools/lang_index.py` reads nested Creative Delight member jars and `*_upstream/` jars; before that FD and Miner's
  names were invisible to offline tools.
- The ~620 log warnings for missing `_2`/`_3` variants seen with 1.2.0 were unreachable stack states; recheck under the port.

Related: memory `wthit-plugin-patchermod`, [missing-names-catalog](missing-names-catalog.md), memory `creative-delight-routing`.
