---
name: add-mod
description: Add (or remove/replace) a third-party mod jar in the pack - sides row, dumps, generators, writeups. Use when a new jar goes into mods/ or client_mods/.
---

# Add a mod

1. **Game closed** before any jar moves (CLAUDE.md process check; ask if unsure).
2. **Jar placement.** Server-capable jars in `mods/`, client-only in `client_mods/`. Replacing a version: take the old
   jar out (ask where if unclear). Two jars with one mod id stop the deploy build.
3. **`mod_sides.md` row** (client / server / both). Clients get everything the server gets. A jar without a row
   fails `tools/dump_server_mods.py` and the deploy preflight.
4. **Pinned fixes.** If the mod is in [deploy.md](../../../feature_writeup/deploy.md) "Mod updates (version-pinned fixes)", recheck
   the fix and update `PINNED_MODS` in `tools/build_deploy.py`.
5. **Configs.** Any config edited by hand: copy to `configs_edited/root_config/<path>` or `server_config/`.
6. **Dumps and generators** (in game): `/dumpall`, then `/cdevgen run all dumps`; order and hand steps in memory
   `dumpall-refresh-workflow` (food_catalog misfiles, BlockRunner tiers, jei_coverage UNCOVERED, item_dict
   mapping, crops.md, compost `gen_compost_values.py --merge`).
7. **Early checks**: `logs/latest.log` for `Failed to parse`, mixin errors, the mod id.
8. **Content mod** (items, food, systems): run the `integration-report` skill before any integration work.
9. **Docs, same session**: writeup row or section; TODO entry; player changelog bullet in
   `kubejs/assets/creativecommon/changelog/unreleased.md` if players notice it (new mod = yes), then
   `python3 tools/changelog_lint.py`.
10. Removing a mod: same steps in reverse; drop its `mod_sides.md` row, grep the namespace in `kubejs/` and
    `development/` for dead references, note the removal in the changelog.
