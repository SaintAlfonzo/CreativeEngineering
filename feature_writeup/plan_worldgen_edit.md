# Worldgen edit plan (2026-11-01)

Status: IN PROGRESS. Built 2026-11-02: A1 to A5 (A5 parts a and b), B1, B5 (patcherMod and Creative Calendar installed). Next: step 3 (A7, B2 to B4 probes, A6, B6). Owner calls of 2026-11-01 are folded in. Row ids refer to
[todo/TODO.md](todo/TODO.md). Every change that touches game data adds its live checks to
[testing/scratch_queue.md](testing/scratch_queue.md) (no scratch boot until the owner says so) and a bullet to
`kubejs/assets/creativeengineering/changelog/unreleased.md` where a player can notice it. Hand-edited configs are
copied to `configs_edited/root_config/`.

## A. Config and data values (small, no code)

| # | Row | Change | Files |
|---|---|---|---|
| A1 | `worldgen/biomes #9` | `nullscape:shadowlands` multiplier 0.5 -> 0.75 (aimed End share 10.7 % -> about 16 %). Then `locate biome` / an End climate dump to confirm it appears | `kubejs/generation_scripts/worldgen/biome_config.json`; update [biome_tuning.md](biome_tuning.md) F4 table and the TODO row |
| A2 | `locate #5`, `rates-and-groups #6` | `[worldgen.locate] searchTimeoutSeconds` 300 -> 120: CEConfig default, `WorldgenConfig` fallback, toml + `configs_edited` copy, [locate.md](locate.md) knob list, structure_tuning.md 3.27 wording; drop the "300 s" changelog bullet. Rerun SQ-WG30-C05 with 120 | patcherMod (rebuild + install), toml |
| A3 | `earthfill #4` | Remove `eugene:undead_castle` from `land/highlands` in `structure_tags.json` (line about 1232 block), regenerate groups (`tools/gen_structure_groups.py`). Raise the air limit in the override `eugene:undead_castle=24,320,400` to `24,800,400` (the two cancelled castles measured 708 and 562 over air; 800 clears both). The mound limit stays 400 | `config/creativeengineering-common.toml` line about 1408 + `configs_edited` copy; `structure_tags.json` |
| A4 | `rates-and-groups #12` | Mysticrift towers x3 on their current group weight: acacia 10 -> 30, birch 8 -> 24, cherry 17 -> 51, desert 1 -> 3, end 1 -> 3, jungle 10 -> 30, mangrove 5 -> 15, oak 5 -> 15. Keep `tuned`. Edit by hand (`--fit-rates` would re-solve camps, vehicles and statues). Re-estimate with `gen_structure_groups.py --check`; the end tower's 0.0197 and the mangrove 0.038 frequencies stay as they are (biome-limited) | `structure_groups.json`, `structure_group_estimates.md` |
| A5 | `rates-and-groups #11` | Waystones: (a) lone: `[worldGen] frequency` 25 -> 60 chunks in `config/waystones-common.toml` (about 5.8x fewer; deserts get the same cut because the feature is global); (b) villages: `forceSpawnInVillages` false -> true (nearly every village gets one, near the centre); (c) ruins: new, see A6 | `waystones-common.toml` + `configs_edited` copy |
| A6 | `rates-and-groups #11` | Waystones in ruins: add a one-piece jigsaw structure `creativeengineering:waystone_ruin_part` (template `waystones:village/common/waystone`, mossy/sandy by biome is handled by `worldGenStyle = BIOME`) under `kubejs/data/`, and a meta `meta/waystone_ruins` (anchor `#creativeengineering:meta/small_ruins`, share 0.2, part: the waystone piece 4 to 12 out). Tag the part so it is not a free-standing structure. Check against `meta/ruin_scatter` (share 0.3) so the shares add to under 1 | `meta_structures.json`, `kubejs/data`, `structure_tags.json` |
| A7 | `structures/contents #1`, `netherblockswap #6` | Move the four `plan/move_to_nether` structures to the Nether, soul fire left alone there. Follow the build in [briefs/structures_contents.md](briefs/structures_contents.md) `#1` steps 1 to 5 (biome fits `nether/soul`, `nether/forest`, `nether/wastes`; tags out of the Overworld lists; start heights copied to `kubejs/data/philipsruins/...`; groups into `nether_ruins`). Then drop the `plan/move_to_nether` exception from rules 23 to 25 (the swap generator never touches Nether-owned templates, so soul fire stays). Decided 2026-11-02: ancient cities and sculk structures keep their soul fire (exceptions in rules 23 to 25 stay for them); `netherblockswap #6` closes with that call | `structure_tags.json`, `biome_fits.json`, `kubejs/data`, `nether_swap_rules.json`, `tools/gen_nether_swaps.py` |

## B. Bug fixes (patcherMod and data)

| # | Row | Cause and fix |
|---|---|---|
| B1 | `rates-and-groups #9` | Canonical order renumbers duplicate pool pieces (`#2`, `#3`) while the stored `current` weights keep the old keys, so 223 duplicate pieces in 27 pools generate with another piece's weight. Fix: reset the 27 pure-permutation pools to their original weights in `pool_config.json` (keep the 9 deliberate vinery / beautify edits); `PoolTuner` merges by original key and warns on an edited duplicate. `canonicalOrder` stays on. Recheck `pool_tag_report.md` and ST-191. Patcher rebuild needed |
| B2 | `metas #12` | `meta/meteorite_desert_pyramid` already lists the pyramid first (`distance [0,0]`) then the meteorite (`distance [0,12]`, `overlap`, 24 tries). The AE2 meteorite carves a crater that also removes the sandstone above ground. Owner design: the pyramid is placed first and the meteorite punches through it. Fix: (1) read `MetaStructure` part placement and the meteorite's carve radius against the 21 x 21 pyramid footprint; (2) put the meteorite part at `distance [0,4]` so it lands on the pyramid; (3) run the crater as a punch: it replaces blocks inside the sphere and a core column, but leaves the rest of the above-ground pyramid (the "sphere replaces blocks it overlaps" mode `struck_monument` already uses, instead of the crater carve); (4) `metas #11`'s short-core rule stays. Test SQ-META31-04b plus a look at the crater from the ground |
| B3 | `contents #6` | Lone birch tower had no pillagers in 31 blocks of range. Probe first: is the box resolved (`spawn_boxes.json` has a tower entry) and was the monster cap full. Rerun with the cap freed (judged_w2.md). If the box resolves and the cap was the cause, close as not a bug and say so; otherwise fix the resolution in patcherMod |
| B4 | `pillagers #6` | The tower siege meta spawned the Towns and Towers mix (2 pillagers, 3 vindicators) and no evokers or illusioners. `meta_tower_siege` uses pool `large_tower` and `meta_large` covers the great-tower and stronghold sieges, so the part's own tower box probably wins over the meta box. Probe order: print which box a position inside each siege part resolves to (the siege parts first), fix the priority so the meta's box applies to its whole bounding box, then the by-hand rerun at night with a long wait for SQ-SB-05 to -08 |
| B5 | `seasons #2` | Cause identified: Cool Rain 1.0.2's client `RainMixin.tickRain` calls `Biome.getPrecipitation()` (the vanilla field) and compares it to RAIN, so its per-block rain sounds ignore `SeasonTypes.precipitationAt`. Fix: a `CoolRainSeasonMixin` in Creative Calendar beside `PrettyRainSeasonMixin` (`@Pseudo`, `@Redirect` on `m_47530_` in `RainMixin.tickRain`, answering from `SeasonTypes.precipitationAt(level, holder, pos)`), registered in `creativecalendar.mixins.json`. Client check WE-25. Built 2026-11-02 with one change: `RainMixin` is a mixin, so its `tickRain` is merged into `LevelRenderer` as `handler$<id>$tickRain`; the mixin targets `LevelRenderer` with a regex selector at priority 1500 (two redirects: keep the `getBiome` position, answer `getPrecipitation`) |
| B6 | `pillagers #5` | Old Guns (3.7.3) has no mob-equipment code, so the guns do not come from the mod itself. Find the source: grep structure templates for pillager NBT with oldguns items, loot tables/LootJS on pillagers, and other mods. Owner call: some pillagers should have old guns, so keep or add a rule (a small share, e.g. 10 % of pillagers and vindicators hold a flintlock or caplock) and add a tuning note: new `pillagers #7` "Tune pillager old-gun share" (Planned, T) |

## C. Owner calls of 2026-11-02 (build)

| # | Row | Change |
|---|---|---|
| C1 | `terrain #2` | Calibrate `[worldgen.earth_fill] beardFootprint` in the scratch run (SQ-TER-05, ST-304): run `tools/earth_fill_sim.sh <seed> <n> footprint` against the live mound limits, compare outposts (32 % missed, 23 % newly over the limit) and villages (42 to 53 % missed, 2 to 18 % over), tune the mound limits so the newly-over share is acceptable, then turn `beardFootprint` on in the toml, the CEConfig default and `configs_edited`. Pruning by it is decided by the calibration result and recorded in the row |
| C2 | `terrain #7` | Accepted: flooded cabins under oceans are fine when the whole cave is flooded. The only defect is a large hole in the seabed, and SQ-WG30-D01 passed (no seabed hole over any find). Ocean cabin stays out of `seabed_clearance`. Close the row (Complete after the next D01 check); note the decision in structure_tuning.md 3.26. No build |
| C3 | `netherblockswap #4` | Crimson -> mangrove, warped -> dark oak: K28 trapdoor, K29 button, K33 slab to mangrove; K30 pressure plate, K31 trapdoor, K35 stairs to dark oak; K36 stripped warped hyphae to stripped dark oak wood. Add rules to `nether_swap_rules.json` / `tools/gen_nether_swaps.py`, regenerate, swap count rises by the 7 rows (about 107 blocks) |
| C4 | `metas` shares (pillager) | `pillager_camp_ruin` 0.15 -> 0.25, `pillager_camp_ruins` 0.15 -> 0.25 (camp anchor total 0.20 + 0.25 + 0.25 + 0.10 = 0.80, under 1); `pillager_great_tower_siege` 0.50 -> 0.30. Confirmed 2026-11-02: the pillager metas only, the general ruin metas stay |
| C5 | `meteor_shower` | Share 0.50 -> 0.33 (about a third lower) |
| C6 | meteorites any height | The `meta/meteorites` parts and the AE2 meteorite starts may spawn at any height, not only at the surface. Step 1: read what limits height now (the `meta/meteorites` tag members' start heights and `terrain_adaptation`, and the metas that carry them: `meteor_shower`, `struck_monument`, `meteorite_village`, `pillager_camp_meteorite`, `underwater_meteorite`); step 2: allow a wide height range (underground, surface, air) for the free-standing meteorites only; the metas keep surface or anchor placement. Confirmed 2026-11-02: free-standing meteorites only. Related to `sky #1` and `sky #3`, which stay out of this plan |

## C2. Planned rows with no decision needed (folded in)

| # | Row | Work |
|---|---|---|
| N1 | `biomes/netherblocks #3` | Rerun `gen_wild_flowers.py` after the Nether flower changes of this plan and commit the output |
| N2 | `biomes/netherblocks #4` | Scan the pack for Overworld glowstone: D&D glowstone geodes (`engineering/dreams-and-desires #5`), structures (rule 21 already swaps them), features, ores. Fix hits with a feature removal or a block swap; record the scan in nether_worldgen.md |
| N3 | `structures/contents #5` | Quark End spires: air under the spike's base footprint cancels the spike (owner 2026-11-02; built in patcherMod `QuarkSpiralSpireFootprintMixin`, Quark had no check of its own) |
| N4 | `mobs/spawns #3` | Alex's Mobs and duck / goose parity: biome and frequency comparison from the analysis base in mob_spawns.md 10.1; output is a table and a fix list, then apply the obvious fixes |
| N5 | `mobs/spawns #4` | Lone villagers in plains, temperate hills, fields and meadows (spawn rule in the mob spawn config) |
| N6 | `mobs/spawns #5` | Allay spawns in pillager cages, mystical biomes and more structures (spawn boxes and biome rules) |
| N7 | `mobs/spawns #6` | Fireflies: verify the Firefly Bush port in a swamp at night (tick cost of the per-tick block reschedule), jar catching, jarring near a bush at night; fix the cost if it shows |
| N8 | `mobs/pillagers #2` | End pillagers and the End tower spawn with diving gear; the End tower gets a spawn box (its spawners were removed in `structures/contents #3`) |
| N9 | `mobs/mooblooms #4` | Skylands: cows in all seasons including winter, spring to autumn skylands 100 % mooblooms split evenly over the available flowers. Depends on `worldgen/biomes #3` (skylands at under 0.25 % of the surface): build the data now, it only matters once skylands reach; decide on `biomes #3` separately |
| N10 | `locate #3` | Distant Horizons pregen on the final server world: a deploy-step note in deploy.md and the release skill, no code |
| N11 | `structures/testing #1`, `#3` | Test lists only (client round 8.0.11; superflat and the client clock mixin): add them to the client queue, no build |

## C3. Information (already handed over)

- `terrain #2`, `terrain #7`, `netherblockswap #4` and the meta share table: see the hand-over messages.

## D. Order of work

1. A1, A2, A3, A4, A5 (config/data only; no restart needed beyond new chunks).
2. B1 and B5 (patcherMod / Creative Calendar rebuild, one install and one restart for both).
3. A7 (data, large) then B2, B3, B4 (probes first), then A6 and B6.
4. C3, C4, C5, C6, N1 to N3, N8 (data and small patcherMod); C1 runs inside the scratch session (calibration needs the world).
5. N4 to N7, N9 to N11 (spawn rules, investigations, notes).
6. TODO updates, writeups, scratch queue, changelog, `python3 tools/changelog_lint.py`.

## E. Tests to queue (built part: SQ-WGE-00 to 08 and CQ-WGE-07a to c in scratch_queue.md, 2026-11-02; the rest below are still to write)

SQ-TER-05 (beardFootprint calibration, then enable), SQ-NBS-4 (K28 to K36 swaps), SQ-META-SHARES (camp ruins, great tower, meteor shower), SQ-METEOR-HEIGHT, SQ-MOB-N4 to N8, SQ-WG-BIOME-SHADOW (End census), SQ-WG30-C05 (120 s), SQ-EF-06b (castle, new air limit), SQ-STRUCT-MYST3X (estimates and census),
SQ-WAYSTONE-1 (lone spacing, village share, ruin waystones), SQ-POOL-1 (weights after B1), SQ-META31-04b (pyramid punch),
SQ-SB-03b/04b (birch tower, siege box), CQ-WE-25 (Cool Rain), NETHER-MOVE (four structures in the Nether, census).

## F. Not in this plan

Rows needing a template source, values or a design pass: `rates-and-groups #1`, `#2`, `#4`, `#8`, `rates #1`, `sky #1`, `sky #3`, `metas #2` to `#4`, `contents #2`, `#4`, `new #1` to `#3`, `biomes #2` to `#4`, `#6`, `#7`, `netherblocks #2`, `seasons #3`, `terrain #4`, `locate #2`, `pillagers #1` (lone pillagers), `#3`, `#4`, `mooblooms #3`, `mobs/spawns #7`.

## G. Decisions that remain

None. All calls of 2026-11-02 are recorded in sections A and C.
