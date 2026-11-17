# wthit-crop-growth-line

> WTHIT \"Growth: X% / Mature\" line for plants WTHIT's PlantProvider skips (hemp, tobacco, berry bushes, grapes, fruit leaves...) - Creative Common crops/WthitCropGrowth + crop_growth.json rules; /dumpcropgrowth audit + coverage tool; status in feature_writeup/wthit_lines.md

Moved from agent memory 2026-11-16 (last edited there 2026-10-05). Repo status docs win over any status line here.

User asked (2026-10-04) to catalog every crop where WTHIT shows no growth percentage / harvestable and fix it. Confirmed silent: hemp, tobacco, strawberries, salmonberries, mint, grapes, blueberries, cranberries, lemons.

**Cause:** WTHIT 5.36.1 `PlantProvider` is registered for CropBlock/StemBlock/CocoaBlock/NetherWartBlock/SweetBerryBushBlock/SaplingBlock, but the growth line only fires for `instanceof CropBlock` and vanilla melon/pumpkin stem, wart, cocoa, sweet berry by `block ==` identity. Modded stems/bushes get nothing. wthitharvestability is tool-harvestability only, unrelated.

**Built:**
- `/dumpcropgrowth` (kubejs `_lib/dump_crop_growth.js`, in `/dumpall`) -> `kubejs/exported/crops/crop_growth_audit.json`: 290 growing blocks, 22 native, 62 ruled, 206 not crops. Must include non-int properties (hemp is an enum `growth`; fruit leaves use enum `type`, durian uses `fruit`).
- Creative Common `crops/WthitCropGrowth` (client plugin, `waila_plugins.json` entry, reuses WTHIT's `plant.crop_progress` option id, which lives in the minecraft namespace) + pure `crops/CropGrowth` (rules, maths) + `assets/creativeengineering/crop_growth.json` (first match wins, globs, `stages`, `max`, `harvest`, `empty`, `requires`). Overridable from `kubejs/assets/creativeengineering/`. CEConfig `[crop_growth]` enabled/fallback/showHarvestable.
- `tools/crop_growth_coverage.py` (UNKNOWN = new mod plant needs a rule) and `tools/test_crop_growth.sh` (38 checks). Writeup: feature_writeup/crops.md 2.9, tests CR-59..66; plan_1.0.4 row GL.

**Status:** feature_writeup/wthit_lines.md.

**Why:** WTHIT's native line should be uniform across the pack's crops.
**How to apply:** after adding mods run `/dumpcropgrowth` + the coverage script. Use the scratch server recipe (memory `scratch-server-verify`); a KubeJS `reload` does NOT re-register commandRegistry commands, restart the server after editing a command script. See memory `wthit-plugin-creative-common`, [crops-harvest-writeup](crops-harvest-writeup.md).
