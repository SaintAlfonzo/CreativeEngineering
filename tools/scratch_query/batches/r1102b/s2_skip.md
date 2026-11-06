# r1102b slice 2: rows not batched

| ID | Reason (stale / superseded / client / needs player / jar missing) | Evidence |
|---|---|---|
| SQ-CDQ-04 | client | tooltips on hover and F3+T reload of `placeableTooltips` (client script tree `assets/creativedelight/kubejs/_qol/`) |
| SQ-CDQ-07 | client | WTHIT "Growth" lines, `wthitCropLine` read by the client overlay |
| SQ-CDQ-08 | client | JEI info pages (`// requires: jei`), F3+T |
| SQ-FFB-01 | client | needs the `/dumpfood` food export (client-only dump, gotchas.md "Logs and results") copied into the instance, then `tools/food_families.py` |
| SQ-INV-09 | client | generators read the client dumps (food, stacks, names) and write pack files; hand step after CQ-DUMP (memory `dumpall-refresh-workflow`) |
| SQ-INV-13 | client | Comfort-food count comes from the food dump (client-only) |
| SQ-INV-15 | client | `/cegen run jei_world` / `gen_jei_world.py` is an in-game generator run on the instance (writes `generated.json`), after `/dumpworldjei` |
| SQ-INV-16 | client | `/cegen run loot dumps` is an in-game generator run on the instance (writes `loot_index.json`, `loot_config.json`) |
| SQ-INV-18 | stale | no `@GameTest` / `GameTestHolder` in any dev mod (grep `development/*/src`, members too) and `tools/time_table.py` does not exist |
| SQ-RD-12 | client | creative tab, textures, names, mod list, JEI |
| SQ-RD-29 | client | models, tooltips, WTHIT crop line |
| SQ-RD-30 | client | AppleSkin and JEI (food values are checked server-side in SQ-RD-17 / -21 / -22 by `/zq food`) |
| SQ-RD-31 | superseded | cutover done: `rusticdelight-forge-1.19.2-1.3.0.jar` is in `development/creativeDelight/jar_archive/displaced/`, `mods/` holds no rusticdelight jar, the port is nested in `mods/creativedelight-1.0.0.jar`; an instance step, not a scratch row |
| SQ-RD-32 | client | crop and medley stage textures, `template_crop_cross` / `platter.png` client log lines |
| SQ-RSP-08 | needs player | Create pump rig at 16 and 256 rpm with tanks and pipes; "whole 250 mB steps, never above 1000" is watched over time (no probe reads the tank per tick); hand step or client run |
| SQ-WCD-05 | stale | "before the CD install, with the old creativedelight jar": the current CD jar is installed (`mods/creativedelight-1.0.0.jar`, 2026-11-02 14:01) and the old-jar state no longer exists |
