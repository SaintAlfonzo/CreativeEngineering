# r1102b slice 3: rows and row halves not batched

## Whole rows (23)

| ID | Reason (stale / superseded / client / needs player / jar missing) | Evidence |
|---|---|---|
| SQ-FSD-04 | stale (already run) | farmers_structures.md 4: 2026-10-26 run PASSED SQ-FSD-04 (all 7 kinds place); the contents half (chests, frames, pots) is a client look |
| SQ-FSD-06 | stale (already run) | farmers_structures.md 4: SQ-FSD-06 PASSED 2026-10-26 (carp not locatable, squids at 552 blocks); SQ-FSD-07 (s3_A) checks the sets again from the dump |
| SQ-CDB-11 | client | names (Bayberry Juice on bottle, fluid, can), JEI cacciatore cycling, jar fill shown in the GUI |
| SQ-CDB-14 | client | JEI uses page of the Sample Syringe; `$WorldRows` is a client JEI script |
| SQ-EN-05 | client | JEI pages and tooltips |
| SQ-EN-08 | client | 25 retextured items (art); the Shulker Bowl message needs a player eating by hand |
| SQ-LR-01 | client | needs a played session with Distant Horizons generating (DH is a client mod) |
| SQ-SV-02 | client | queue: needs a client connected (block change packets) |
| CQ-CL-13 | client | title screen / pause menu button |
| SQ-MG-16 | client | group C: JEI, textures, walking feel; the fan wash needs a Create fan setup by hand |
| SQ-CM-33 | client | JEI item list order |
| SQ-MG-20 | client | JEI search |
| SQ-AC-04 | client | HUD armor bar |
| SQ-CSI-02 | needs player (old save) | needs a save made before the CuriosSlots jar; every r1102b world is new. A fresh bot is SQ-CSI-01 |
| CQ-PT-01 | client | tooltip lines |
| CQ-ROLL-10 | client | JEI, names, tooltips, models |
| CQ-LGM-11 | client | JEI tabs, names, Display Delight placement, Spice of Life |
| CQ-MXS-04 | client | icon, tooltip, JEI |
| SQ-BBQ-08 | needs player | crafting grid result (bots cannot craft) and eating the wrap; recipe ids are in SQ-BBQ-02 |
| SQ-STK-05 | client | `tools/stack_sizes.py` writes feature_writeup/stack_sizes.md from the instance's client /dumpstacks (names); the sizes are SQ-STK-02 to -04 |
| SQ-RDX-02 | client | JEI search and tooltip |
| SQ-RDX-13 | needs player (old save) | needs a save with rusticdelight:pancake / fried_dough in a chest and a placed plate, made before the merge |
| SQ-NDM-08 | needs player (old save) | needs a save made with the NethersDelight-1.19-3.1 jar |

## Halves of batched rows (19)

| ID | Reason | Evidence |
|---|---|---|
| SQ-FFH-02 | stale (dump diff) | "diff against the 10-26 dump": that dump is gone and later CD work changed many ids; s3_A checks the named ids only |
| SQ-CDB-05 | client | sprites (palette swap) |
| SQ-CDB-07 | client | the milk shown in the jar |
| SQ-CDB-13 | client | not in JEI |
| SQ-CDB-15 | needs player | crafting the three by hand; s3_A reads the recipe inputs |
| SQ-EN-03 | not probed | void pepper "spreads then fruits, 3 / 1 drops": /cbetest growth counts changes only |
| SQ-SSP-01 | needs player | Slice & Dice slicer over a depot needs a powered Create setup; the cutting board half is batched |
| SQ-SSP-03 | client | JEI sprite, name, uses |
| SQ-ROLL-09 | client | anvil plank repair (CQ-ROLL-10) |
| SQ-BBQ-06 | needs player | the flip is a sneak-use (no sneak in perfBot); the unflipped burn is batched |
| SQ-RDX-03 | needs player | sneak-use puts a pancake back |
| SQ-RDX-07 | client | JEI and crafting with mixed doughs |
| SQ-RDX-08 | client | placeholder texture, name, mining speed |
| SQ-RDX-09 | client | JEI incomplete sprites; the deployer belt run |
| SQ-RDX-14 | client | incomplete pizza sprite |
| SQ-WE-03 / SQ-WE-04 | needs player | WorldEdit runs `//` commands for players only; /wetest drives the same API |
| SQ-CDFW-03 | client | placeable tooltips |
| SQ-CDFW-04 | client | crafting a can and the Servings line |
| SQ-NDM-01 | client | the mod list display name |
