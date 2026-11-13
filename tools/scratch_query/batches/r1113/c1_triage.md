# r1113 chunk 1 triage (chunk1.txt, 66 row IDs)

Batched: 20 rows in 4 files (3 boots plus the second boot of an old-save pair). Not batchable: 46.
Earlier reasons reused from r1102b `s1_skip.md`, `s2_skip.md`, `s3_skip.md` and r1103 `sB_skip.md`, checked again
2026-11-13 where the prerequisite could have changed (jars, commands, tools).

| ID | decision | reason |
|---|---|---|
| SQ-PR-01 | not batchable | client: Pretty Rain particles and a client crash; server half was SQ-PR-00 |
| SQ-SK-03 | not batchable | client: single-player world creation (integrated server, `singleplayer` key) |
| SQ-SNF-02 | batched in c1_a1.qb | recheck only: PASS 2026-10-26 (queue line 1508); ids moved since (serializer `creativedelight:seasoning`) |
| SQ-PS-02 | batched in c1_a1.qb | recheck only: PASS 2026-10-25 (queue line 1600); item now `creativedelight:powdered_sugar` |
| SQ-KS-02 | not batchable | recorded PASS 2026-10-25 (queue line 1627, "`kubejs/data` reloads"); nothing since changed the KubeJS build |
| SQ-KS-03 | not batchable | recorded PASS 2026-10-25 (line 1627: one runs, `kubejs/data` wins) |
| SQ-KS-04 | not batchable | recorded PASS 2026-10-25 (line 1627: `requires: not_a_mod` skipped) |
| SQ-KS-05 | not batchable | superseded: the row says covered by SQ-CDT-01 / -02 (both PASS 10-25) |
| SQ-CDJ-06 | not batchable | server half recorded PASS (lines 1599, 1628); the mod-list half is client |
| SQ-CDT-04 | not batchable | recorded PASS (line 1621); a rerun needs the `-Pcd_test_suite` cdtest jar, still not built (no `*cdtest*` in creativeDelight build/libs) |
| SQ-CDT-05 | not batchable | recorded PASS (line 1621); the mirror is frozen since the cooking scripts moved (README: stale by design) |
| SQ-VTD4-09 | not batchable | client: JEI page, cut-face texture, peanut tree look |
| SQ-FDB-08 | not batchable | needs a real player: off-hand beef with a skillet use underwater, pass is an action-bar message |
| SQ-FDB-12 | not batchable | client: particles, models, icons, tooltips, placement message |
| SQ-RD-12 | not batchable | client: creative tab, textures, names, mod list, JEI |
| SQ-RD-29 | not batchable | client: models, tooltips, WTHIT line |
| SQ-RD-30 | not batchable | client: AppleSkin and JEI (server food values were SQ-RD-17 / -21 / -22) |
| SQ-RD-31 | not batchable | superseded: cutover done (1.3.0 jar displaced, port nested in CD; baked since 11-10) |
| SQ-RD-32 | not batchable | client: crop and medley stage textures, client log lines |
| SQ-FFB-01 | not batchable | needs the client-only `/dumpfood` export in the instance, then an instance generator run |
| SQ-CDQ-04 | not batchable | client: tooltips and F3+T of a client script |
| SQ-CDQ-07 | not batchable | client: WTHIT overlay lines |
| SQ-CDQ-08 | not batchable | client: JEI info pages |
| SQ-WCD-05 | not batchable | stale: "before the CD install with the old jar"; that state no longer exists |
| SQ-RSP-08 | not batchable | needs a Create pump rig at two speeds watched over time (no probe reads the tank per tick): hand step or client |
| SQ-INV-09 | not batchable | instance generator chain after the client dumps (writes pack files; memory `dumpall-refresh-workflow`) |
| SQ-INV-13 | not batchable | offline count from the client-only food dump |
| SQ-INV-15 | not batchable | instance generator run (`/cdevgen run jei_world` writes `generated.json`), not a scratch check |
| SQ-INV-16 | not batchable | instance generator run (`/cdevgen run loot dumps` writes `loot_index.json`, `loot_config.json`) |
| SQ-INV-18 | not batchable | prerequisite missing: no `@GameTest` in any dev mod and no `tools/time_table.py` (checked 11-13) |
| SQ-SY-09 | batched in c1_a1.qb | `/syt roll` 2000 per soil |
| SQ-SY-10 | batched in c1_a1.qb | `/syt roll` 2000 per soil |
| SQ-SY-11 | batched in c1_a1.qb | `/syt roll` 2000 per soil, adzuki also on rich soil |
| SQ-SY-12 | batched in c1_a1.qb | `/syt roll` 2000 per soil plus the `[soil-yield]` crop list |
| SQ-SY-13 | batched in c1_a1.qb | 2 x 10000 rolls; sy_probe now prints totals (a 0.05 % mean shows as 0.00) |
| SQ-FSD-04 | not batchable | already PASS 2026-10-26 (farmers_structures.md 4); the contents half is a client look |
| SQ-FSD-06 | not batchable | already PASS 2026-10-26 (farmers_structures.md 4) |
| SQ-CDB-11 | not batchable | client: names, JEI cycling, jar GUI |
| SQ-CDB-14 | not batchable | client: JEI uses page (client JEI script) |
| SQ-EN-05 | not batchable | client: JEI pages and tooltips |
| SQ-EN-08 | not batchable | client: 25 retextures; the Shulker Bowl message needs a hand eat |
| SQ-LR-01 | not batchable | client: needs Distant Horizons generating (client mod) |
| SQ-SV-02 | not batchable | client: block-change packets to a connected client |
| SQ-MG-16 | batched in c1_a1.qb | server half only (washing-only recipes, weathered box tags); textures, JEI, walking feel stay client |
| SQ-CM-33 | batched in c1_a1.qb | server half only (no tag error, 140 members); JEI order stays client |
| SQ-MG-20 | not batchable | client: JEI search |
| SQ-AC-04 | not batchable | client: HUD armor bar (the server attribute is SQ-AC-02) |
| SQ-CSI-02 | not batchable | needs a save made before the IMC slots existed; every makeable save (installed or deploy/latest jars) already has them |
| SQ-BBQ-08 | not batchable | needs a real player: bots cannot craft, and the wrap eat effects need a hand eat |
| SQ-STK-05 | not batchable | client: `/dumpstacks` names, then `stack_sizes.py` writes a repo doc |
| SQ-STK-06 | batched in c1_a1.qb | syrup stack, pickle recipe and a real jar run, cabbage cutting |
| SQ-RDX-02 | not batchable | client: JEI search and tooltip |
| SQ-RDX-13 | not batchable | old save not makeable: needs pre-11-01 jars (Rustic pancakes before the merge); deploy/latest is 1.2.02 (11-09) |
| SQ-NDM-08 | not batchable | old save not makeable: the 3.1 jar exists (server_mods/) but every CD jar since 11-01 nests 4.0 under the same mod id |
| SQ-VTD8-03 | batched in c1_a1.qb | three jars started at boot, checked after 6.5+ min |
| SQ-PST-12 | batched in c1_b1.qb | group B: jungle and plains pads at y 200, perfBot bots for random ticks |
| SQ-CUP-05 | not batchable | old save not makeable: cups removed 11-02, no jar or release before that is kept in a bootable set |
| SQ-GLD-15 | not batchable | obsolete (row says so; replaced by SQ-GLD-16) |
| SQ-GLD-16 | batched in c1_g1.qb + c1_g2.qb | old-save pair: boot 1 on the installed jars (CD 11-10 still has the stews), boot 2 after oldsave_swap.sh; JEI half stays client |
| SQ-SALT2-02 | batched in c1_a1.qb | campfire-heated cauldrons, randomTickSpeed 1000 with bot c1rt nearby, bot harvest, zcount |
| SQ-SALT2-03 | batched in c1_a1.qb | `zq rdetail` |
| SQ-SALT2-04 | batched in c1_a1.qb | `zq tagsof` |
| SQ-SALT2-05 | batched in c1_a1.qb | KubeJS log line and `zq makes` / `zq recipes` |
| SQ-SALT2-06 | batched in c1_a1.qb | overworld and Nether cases, bots in both dimensions for random ticks |
| SQ-SALT2-07 | batched in c1_a1.qb | 24 level-1 butchers (vanilla + MCA), `/sa offers` |
| SQ-SALT2-08 | batched in c1_a1.qb | same boot: salt-common.toml restored from `.moved` and edited before the first boot; AppleSkin value and the `/reload` half stay open |

## Id fixes (from the current dicts and the 11-10 dumps)

- SQ-SNF-02: serializer `creativeengineering:seasoning` -> `creativedelight:seasoning`; recipe ids already `creativedelight:shapeless/season/...`.
- SQ-PS-02: `creativeengineering:powdered_sugar` -> `creativedelight:powdered_sugar` (Common `item_dict.js`).
- SQ-CM-33: tag `creativecommon:jei/storage_delight` -> `creativecompat:jei/storage_delight` (140 members in the 11-10 tag dump).
- SQ-MG-16: Old box `design_decor:cardboard_box`, Weathered box `creativecompat:weathered_cardboard_box`, Dustrial box
  `dustrial_decor:cardboard_box`; washing recipes live in `kubejs/data/creativecompat/kubejs/other/misc_mods.js`.
- SQ-SY rows: Fertile Soil = `immersive_weathering:mulch_block`, Rich Farmland = `farmersdelight:rich_soil_farmland`,
  tobacco block `the_dirty_stuff:tobacco_seeds[blockstate=7]`, adzuki `neapolitan:adzuki_sprouts[age=6,flowering=true]`,
  Ender Crop `endercrop:ender_crop[age=7]`, peanut `vintagedelight:peanut_crop[age=7]` (Magic Peanut chance 0.0005 in the loot dump).
- SQ-STK-06 / SQ-VTD8-03: salt = `vintagedelight:salt_dust` (only member of `#forge:salt`), zucchini `veggiesdelight:zucchini`;
  the pickle recipe is `creativedelight:pickling/pickle_from_fermenting`, 2400 t.
- SQ-SALT2-02: the salt cauldron is `salt:salt_cauldron[level=1..3]`; its empty-hand use drops the
  `salt:cauldron_evaporation/salt_level_1|_2|full` tables (3 / 5 / 8 `vintagedelight:salt_dust`).

## Probes

- Changed: `tools/soil_yield_probe/sy_probe.js`: each item now also prints `total N` after `max M` (SQ-SY-13 needs the
  Magic Peanut count; a 0.0005 mean prints as 0.00). No other batch used `/syt`.
- Used unchanged: `zq_probe.js` (prep_srv.sh), `tools/fungi_probe/zcount.js`, `sA_probe.js` (`/sa offers`), `zp_probe.js`,
  `s5_ratio.py` (read from $INST), `oldsave_swap.sh`.
