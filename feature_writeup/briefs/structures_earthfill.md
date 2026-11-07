# structures / earthfill: implementation brief

Build plan for every open row: "Build plan (2026-10-30)" below.

Written 2026-10-21 from [todo/TODO.md](../todo/TODO.md) `# structures`, rows `structures/earthfill` #2, #3, #4, #6, #7
(open) and #1, #5, #8 (Testing). Reference for how earth fill works today: [structure_tuning.md](../structure_tuning.md)
3.13 (earth fill), 3.14 (water fill), 3.15 (terrain adaptation, sunk starts), 7.5 (the 1.0.9 changes). Code:
`development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/EarthFill.java` (870 lines, below
`EarthFill.java:<line>`), `worldgen/TerrainAdaptation.java`, `worldgen/WorldgenConfig.java`, `CEConfig.java` (class
`EarthFill` at 1929), `mixin/ChunkGeneratorEarthFillMixin.java`. Tags: `kubejs/server_scripts/worldgen/earth_fill.js`,
`terrain_adaptation.js`, `water_fill.js` (all through `global.tags.add`). Instance config:
`config/creativeengineering-common.toml` `[worldgen.earth_fill]` at line 1387 (`overrides` 1405, `maxWaterSurplus` 1449).

Proposed test ids ST-206 to ST-214 are provisional: other structure briefs written the same day may propose the same
numbers, so renumber against structure_tuning.md 8 (highest today: ST-205) when adding them.

None of these rows touches recipes, items or the `global.NCH` / `_lib/*_dict.js` dictionaries: they are worldgen data
(structure tags, which go through `global.tags.add` as today), CEConfig values and patcherMod Java.

## Build plan (2026-10-30)

**Executed 2026-10-30 (phases 0 to 3, plus D3 / D4).** Owner decisions: D1 cover all structures (not only earth
bases), D2 close #1, #2, #6, D3 and D4 prune instead of cancel, including metas near coasts (only the offending part
goes). Built into patcherMod 1.1.04 (installed in `mods/`), EarthFillTest 91 and MetaTest 76 green; nothing run in
game yet (queue SQ-EF-01 to -08). Feature writeup: [structure_tuning.md](../structure_tuning.md) 3.28 (ST-235 to
ST-242). TODO rows: #13 (phase 1), #14 (phase 2), #16 (D3, D4), #3, #4 built; #1, #2 archived; #15 (phase 4) built later the same day (3.29).
Differences from the plan below:
- E4 is not a bug (earth below the beard's reach does hang); not changed.
- E12's silent config fallback was left alone: `WorldgenConfig` falls back on purpose before the config loads and in
  the headless tests, so a warning there would fire on every early read.
- Phase 2 builds the code footprint (D1), not the data tag; the spans only cover structures in the
  `surface_structures` step and pieces reaching above sea level less `baseLayers`, so underground and Nether giants
  read no terrain. New structure tag `earth_fill/wet_ok` for stilted huts.
- Phase 2 step 1 (measure first) became the scratch entries SQ-EF-03 and -07, after the build.
- D3 / D4 (phase 4 in the plan) were built now because the owner decided them and phase 2 makes villages wetter.
- #3: `startPool` is SRG `f_227605_`; the `f_227607_` in the #3 section below is `maxDepth`. Only `ancient_crypt` was
  tagged; the seabed ruins wait for SQ-EF-05.
- #4: override set; fit narrowing waits for SQ-EF-06.

Replaces the 2026-10-21 "Scope and order" table. Inputs: every `structures/earthfill` TODO row, the SQ-WG30 results
(scratch_queue.md, 2026-10-30), and a full read of `EarthFill.java` (923 lines today; line numbers below are today's).
Nothing in this section is built yet unless it says so.

### State of every row today

| Row | Real state | Action |
|---|---|---|
| #1 water / plinths / `avoidCoasts` | Deployed 1.0.9, ST-199..205 passed | close |
| #2 `maxWaterSurplus` 0 | SQ-WG30-E01 PASS (6 too-wet cancels inland, all at 1.76x or wetter; 0 on the coast) | close; value stays 0 |
| #9 `log = ALL` vs all-off overrides | SQ-WG30-A01 PASS; A03 partial. **Fix incomplete**: see E2 below | keep open for E2 |
| #10 sunk starts and the beard | SQ-WG30-A02 partial (camp flush). **Fix incomplete**: see E3 below | keep open for E3 |
| #6 modded limits | ST-52 did the calibration in Phase 7 | close, tick ST-52 |
| #7 hot spots | Deployed with 1.1.04 (client); server still 1.1.03 | close when the server takes 1.1.04 |
| #3 sunk-start candidates | Not started | phase 3 |
| #4 undead castle | Not started | phase 3 |
| #5 stone bases | Drum tower + End House; SQ-EN-07 earth half never run | phase 3 |
| #8 water fill | ST-57 data exists (07_phase_7.md 2.4), unticked | tick ST-57 after reading it |
| #11 desert structures in water | Not started; **the row's recommended fix is backwards** (see phase 2) | phase 2 |
| #12 camps / ruins floating in water | Not started; probably the same cause as #11 | phase 2 |

Stale facts corrected with this pass: the 1.1.04 jar in `mods/` is byte-identical to `build/libs` (so "built, not
installed" in #2, #9, #10 and `structures/metas #5` is no longer true); #2's "Decisions" and #11's "keep 0.5" predate
the value 0.

### Phase 0: bookkeeping (docs only)
Close #1, #2, #6 (tick ST-52), tick ST-57 for #8 if its 07_phase_7.md data has no wrong cancel. Update #9 and #10
to point at E2 and E3.

### Phase 1: correctness fixes (patcherMod, one build, all offline-testable)

| Id | Defect (verified by reading) | Fix | EarthFillTest case |
|---|---|---|---|
| E1 | `earthUnlimited` (627): `o.c() < 0` is true for a two-number override, because a missing mound is `UNSET` = `Integer.MIN_VALUE`. `"id=-1,-1"` (meant: lift water and air, keep the size's mound limit) skips the whole check (mound and footprint share included) | `o.c() != Override.UNSET && o.c() < 0` | `x=-1,-1` over a mound and a wet footprint -> cancelled; `x=-1,-1,-1` -> kept |
| E2 | `log = ALL` still changes the world for an all -1 override on a `prune` structure: `earthCheck` (881) returns the check under ALL, `filter` (729) then calls `prune`, which never looks at the override (803) and drops pieces on the global `pruneMaxHanging` / `pruneMaxMound`. At CANCELLED nothing is pruned. Same class of bug as #9 | in `filter`, prune only when `!earthUnlimited(structure)`; `prune` still logs what it would drop under ALL, suffixed `(unlimited, not applied)` | unlimited village + `log = ALL` + an offending piece -> piece count unchanged |
| E3 | `prune` builds each piece's `EarthPiece` at `box.minY()` (835), while `cancels` uses `minY + sink` (#10). A sunk structure in `earth_fill/prune` gets per-piece verdicts against a beard `sink` blocks too low. No structure is both today; latent | use `minY + sink` in `prune` too | sunk piece pruned on the same numbers `cancels` gives it |
| E4 | `countEarth` (233): `deeper = fills + (y - bottom) + 2` reaches <= 0 when `sink >= beardReach + 2`; then every earth block counts as hanging. Latent at `beardReach` 6 (sink 4 max today) | clamp to 1 (`Math.max(1, ...)`) and warn at load when a sunk start's sink >= `beardReach` | `beardReach` 2, sink 4 -> earth 1 block over a 1-block gap not counted |
| E5 | Overrides and tags fail silently: ids are compared as raw strings (595), so `village_plains=...` without a namespace never matches; `earthTags` (526) takes a block id as a tag with no warning; unsorted `sizes.bounds` put starts in the wrong bucket (`sizeOf` 611) | at parse: default namespace `minecraft`, warn on an id not in the structure registry or an empty tag, sort `bounds` and warn | parse cases for each |
| E6 | `LOCAL_WATER` (316-322) is not cleared on `TagsUpdated` / `/reload`; old templates stay referenced until server stop | clear it with `LOCAL` | none (memory) |

Then: changelog bullet only if E2 shows in normal play (it does not: only `log = ALL` changes); E1 can, if anyone uses a
two-number override, and the pack's `overrides` list is empty, so no player-visible change. Expect no bullet.

### Phase 2: structures earth fill cannot see (root of #11 and #12)

Earth fill only cancels, it never fills. It counts only blocks in `earth_fill/earth` (dirt, sand, gravel, clay, path)
in the bottom 3 layers of `PoolElementStructurePiece` (Single/List elements) and `TemplateStructurePiece` pieces, and
the footprint share uses only pieces that have such earth. So these are never judged over water:
- legacy pieces (vanilla `desert_pyramid`, `jungle_pyramid`, `swamp_hut`, igloo top piece; many older mods);
- templates with no earth at the bottom: wooden or wool camp floors, sandstone and brick plates (desert temples,
  watchtowers, tent camps), anything on a stone base not in `earth_fill/stone_base`;
- custom pool elements that do not extend `SinglePoolElement`.

So #11's recommendation (raise `maxOverWater` on the desert group) would keep *more* flooded starts, not fewer. The fix
is to judge these structures, not to relax a limit.

1. **Measure first** (scratch, `log = ALL`): reproduce a flooded pyramid and a floating camp / ruin; for each, log
   whether earth fill saw it at all. Small patcherMod help: under `log = ALL`, one line per start with zero counted
   earth (`no earth: N pieces, legacy L`), so the log shows what is invisible. This also answers whether the
   2026-10-28 client-scratch result is representative (#12's decision).
2. **Base footprint for earthless starts** (patcherMod, decision D1): for a start with no counted earth, take each
   non-terrain-matching piece's bounding box at its bottom layer (legacy pieces: `getBoundingBox().minY()`), read the
   predicted terrain there, and apply the same `tooWet` share. Air is not judged (pads that hang are a beard /
   terrain question, and legacy pieces often fill down themselves, like the mansion). Exempt and `water_fill`
   structures are skipped as today.
3. **Cheaper data-only alternative** (decision D1): add sandstone, terracotta and planks-based bases per structure
   group through the existing `earth_fill/stone_base` route (a new block tag `earth_fill/desert_base` for the desert
   group). This doesn't cover legacy pieces.
4. Then set per-structure overrides only where a measured structure needs them (#12's original ask).

Player-visible: yes (fewer flooded structures): changelog bullet under the earth-fill family when built.

### Phase 3: data rows (one scratch run with phases 1 and 2)
- **#3** as in its section below: hardening in `sinkOf` (refuse terrain-matching start pools), look in game, tag
  `ancient_crypt` (+ seabed ruins after the look).
- **#4** undead castle: measure per fit, then override `eugene:undead_castle=24,320,400` and drop `land/highlands`
  if most cancels are there. With E1 fixed, a two-number override is also safe now.
- **#5** stone bases: look at the drum tower in the #4 run; run SQ-EN-07's earth half (End House, `log = ALL`).

### Phase 4: robustness (decide case by case; each needs real village data)

**Built 2026-10-30 (TODO #15, structure_tuning.md 3.29, ST-297 to ST-299).** Owner ask with it: metas pruned to raise
their placement chance, wet pieces of every jigsaw pruned. Done: wet-only pruning for every surface jigsaw
(`pruneWetJigsaws`, tag `earth_fill/no_prune`); E9 meta parts and anchors pruned without `keepShortParts`; E7 floor
`footprintMinWater` 9 (the E01 sample sizes were not in the scratch notes, so the floor went in as a default); E8 identity
removal of junctions plus unlinked drops; E10 `(predicted)` tag (LocateStats already skipped predictions); E11 earth fill
and priority merged into one injector instead of `priority` values; E12 footprint column dedup. Left as notes: template
processors, the config fallback (see 3.28), volatile caches, `SHARED` lifetime.

| Id | Issue | Status |
|---|---|---|
| E7 | `tooWet` has no minimum sample: 1 water / 0 ground cancels. A cliff-edge start whose layer is mostly air can drop on one wet cell | plausible; check the six E01 cancels' sample sizes, then add a floor (e.g. water >= 9) if any is tiny |
| E8 | `links` (650-675) matches parent jigsaws by x/z only; no match = parent -1, so a dropped house leaves a dangling junction (beard at the street edge) and its children stay. `pj.remove(k)` assumes vanilla junction order (custom placers differ) | plausible; GameTest or `log = ALL` dump of one pruned village per pool family (vanilla, T&T, Veggies) |
| E9 | Meta parts are never pruned (`filterParts` 740-755): a village inside a meta goes whole where it would prune alone; the javadoc says "as it would be alone" | design gap; decide D3 |
| E10 | PlacementPriority predictions run the full chain: `CANCELLED` lines can log twice per chunk and `LocateStats.reject(EARTH_FILL)` may count predictions | check `LocateStats` for scratch-chunk filtering; tag prediction lines `(predicted)` |
| E11 | Four `ModifyVariable` STORE injectors share slot 15 (BiomeRules, EarthFill, PlacementPriority, SeabedClearance); EarthFill must run before PlacementPriority, held only by mixins.json order | add a comment in the json and an explicit `priority` on the two mixins |
| E12 | Other small items: footprint dedup by (x, y, z) counts a column twice for stacked pieces; template processors ignored (earth a processor removes still counts); `WorldgenConfig` falls back to defaults on any read error with no log line; the two-field volatile caches are not atomic on reload; `SHARED` caches (about 15-20 MB each) per grid only freed at stop | note; fix the silent config fallback (one warn line) with phase 1 |

Village on a lake (the open #2 decision, step 4 of its section) belongs here too: per-piece share in `prune`.

### Decisions for the owner
- **D1 (phase 2):** base footprint in code (covers legacy and every earthless start; recommended) or the data-only
  desert base tag (narrower, no Java).
- **D2:** close #1, #2, #6 now (recommended).
- **D3:** prune village parts inside metas (E9) or keep cancelling the meta part whole (today).
- **D4:** village on a lake: prune wet pieces or keep cancelling whole.

### Tests (provisional ids; renumber against structure_tuning.md 8 when built)
- Offline: the EarthFillTest cases in the phase 1 table (`tools/test_worldgen_tools.sh`).
- Scratch run (12_earth_fill_camps.md 2 recipe, seed 8675309, density 4, `log = ALL`, inland square 2144 -2592 and a
  desert square): E1 two-number override keeps the mound limit; E2 ST-54 rerun with a village unlimited: piece count
  equals the CANCELLED-level world; phase 2 no-earth lines listed, flooded pyramid cancelled (ST-229); #3 ST-209..211;
  #4 ST-212; SQ-EN-07 earth half. Queue entries go into scratch_queue.md when each phase is built.

Out of scope, adjacent: SQ-WG30-D03 FAIL (seabed clearance ignores aquifer water, `structures/terrain #5`) is the same
"structure in water" symptom underground, but a different system.

## Bugs A and B (fixed 2026-10-30 as #9 and #10; see E2 and E3 above for what they missed)

**Bug A: `log = ALL` changes which starts are cancelled.** An override with every limit -1 (for example
`"#creativeengineering:type/village=-1,-1,-1"`) is meant to skip earth fill entirely, footprint share included (CEConfig
comment at `CEConfig.java:1974`, structure_tuning 3.13). That only holds at `log = CANCELLED`/`OFF`:
- `EarthFill.java:828` `earthCheck` returns null for an unlimited override only when `log != ALL`.
- `EarthFill.java:727` skips the count only when `limits.none() && surplus < 0 && log != ALL`; with `surplus` 0.5 it
  goes on, and `EarthFill.java:730-732` computes the footprint and cancels when it is too wet.
So with `log = ALL` an unlimited structure can be cancelled by the footprint share; with the shipped `log = CANCELLED`
it never is. ST-54 passed on 1.0.7 (no footprint share yet). Fix: in `EarthCheck.cancels`, compute
`boolean unlimited = earthUnlimited(structure)` and pass it into the decision: `wet = !unlimited && f != null &&
f.tooWet(surplus)` (still log the footprint numbers under ALL). Add an `EarthFillTest` check: unlimited override +
`log = ALL` + a wet footprint -> kept. Rerun ST-54 with `log = ALL` afterwards.

**Bug B: the beard reach is over-estimated for sunk starts.** `TerrainAdaptation.rigid` (`TerrainAdaptation.java:404`)
raises a sunk start's beard ground level by its sink (`groundLevelDelta += sink`). The beard fills `beardReach` (6) below
its ground level, so under the sunk piece's bottom it now fills only `6 - sink` blocks. `EarthCheck.cancels`
(`EarthFill.java:721`) still builds `new EarthPiece(earth, box.minY(), beard)`, so `countEarth` assumes 6 blocks of fill
under the real bottom: for the 6 Campsite camps (sink 4) earth over a 3 to 6 block gap is counted as filled though the
beard reaches 2. The mound test (`openBelow(x, bottom - 1, ...)`) has the same offset. Fix: `new EarthPiece(earth,
box.minY() + sink, beard)` (the `Layer` on the next line already uses `box.minY() + sink`); `countEarth`'s
`deeper = fills + (y - bottom) + 2` then becomes `fills - (bottom - y) + 2` for earth below the raised bottom, which
is the right remaining reach. Test: an `EarthFillTest` case with a sunk piece (bottom 60, sink 4, earth at y 60 over a
4-block gap) -> counted over air (today: not counted). Expect slightly more campsite cancels on slopes; ST-199 should
still pass.

---

## structures/earthfill #2: `maxWaterSurplus` first value
Status Testing (built 2026-10-30, value 0) · Work config · TODO `structures/earthfill #2`

### Goal
Settle the footprint water share threshold (`[worldgen.earth_fill] maxWaterSurplus`, shipped 0.5 in 1.0.9) from real
cancels, and decide what happens to a village on a lake (today it goes as a whole).

### Now
- Rule: `EarthFill.Footprint.tooWet` (`EarthFill.java:249-252`): cancel when water > ground x (1 + surplus).
  Footprint = every column of every earth piece's bounding box at `minY + sink` (`EarthFill.java:259-275`, layer built
  at 722). Not for `water_fill/structures` (`EarthFill.java:836`).
- Config: `CEConfig.java:1966-1975` (default 0.5, range -1 to 1000), read live by `WorldgenConfig.earthMaxWaterSurplus()`
  (`WorldgenConfig.java:112`); instance toml line 1449.
- Data so far (12_earth_fill_camps.md 2, diag3, 2,601 inland chunks, 972 starts): 21 "too wet", 14 by the share alone.
  Cancelled ratios (water / ground): 739/0 (sunflower plains village), 1,691/497 = 3.4 (birch forest village), 2,123/66
  (drum tower), 255/0 (windmill), 505/244 = 2.07 (big horse stable), T&T outposts, desert watchtower, a Mysticrift
  tower. Kept starts with any footprint water: 5 of 926, the wettest 470/425 = 1.11.
- So on this sample any surplus between 0.11 and 1.07 gives the same result. 0.5 sits in the gap.
- Pruning (`EarthFill.pruned`, `EarthFill.java:769-811`) drops village pieces over deep water or mounds, but has no
  share rule, so a village whose houses stand on a shallow lake (beard-filled) is cancelled whole by the share.

### Build
1. Fix bugs A and B above first.
2. Scratch run (setup in "Scope and order"), coast square included (the inland square had almost no shore cases).
   `grep '\[earth-fill\].*footprint' logs/latest.log` and tabulate water/ground for kept and cancelled starts, by
   structure. For every share-only cancel (`too wet` with water/air/mound under their limits), teleport to the chunk
   and look (or probe with the scratch `/zcol` KubeJS probe from 12_earth_fill_camps.md 2): open water or a lake
   island = right; a shore with a small pond = wrong.
3. Keep 0.5 unless a wrong cancel shows a ratio under 1.5 (then raise to 1.0) or a kept island camp shows one over 1.5
   (then lower to 0.25). Edit both the instance toml (line 1449) and the CEConfig default (`CEConfig.java:1975`) so new
   tomls match; the toml alone is enough for the pack.
4. Villages on lakes (decision below): if wanted, add a per-piece share to pruning. In `EarthFill.pruned` (loop at
   `EarthFill.java:778-787`) compute `footprint(List.of(layerOf(piece, sink)), terrain, baseLayers)` per piece and mark
   it offending when `tooWet(surplus)` and `water >= pruneMinWetColumns` (new config, say 16, so a 3x3 well over a
   puddle is not dropped). The `pruned` signature needs the surplus and sink; `EarthCheck.prune` (750) has both.
   Log reason `(... footprint W water / G ground)`.

### Decision and value (2026-10-30)
User: "set aggressive cancel value". BUILT: `maxWaterSurplus` 0.5 -> **0.0** (instance toml, CEConfig default
`defineInRange("maxWaterSurplus", 0.0, ...)`, `WorldgenConfig.earthMaxWaterSurplus()` fallback 0.0; patcherMod built,
not installed). Why 0:
- The rule is `water > ground x (1 + surplus)` and only runs for surplus >= 0 (`tooWet`), so 0 is the strictest value
  the code allows: any footprint with more water than dry ground cancels. A value under 0 turns the check off; a
  stricter rule (cancel below a water majority) would need a code change, not a config value.
- On the diag3 sample every cancel was already at 2.07 or wetter (739 / 0, 1,691 / 497, 2,123 / 66, 255 / 0, 505 / 244).
  Of the 5 kept starts with footprint water, only the wettest (a shore village, 470 / 425 = 1.11) crosses 1.0; it is
  the one extra cancel 0 makes on that sample. The other kept shore villages (113 / 462 and similar) stay.
- Villages still prune house by house first (`EarthFill.pruned`), so the share is judged on what is left; a village
  standing mostly on a lake still goes as a whole (step 4 above not built).
- Interplay: `structures/earthfill #11` (desert structures in water) gets more of its flooded starts cancelled by this
  value. An override with every limit -1 skips the share too (`structures/earthfill #9` makes that hold under
  `log = ALL` as well), which is the way to exempt one structure from 0.
- Fallback if the run shows wrong cancels (a shore with a pond cancelled): 0.25, then 0.5.
Test: scratch queue SQ-WG30-E01.

### Ids and data
Config keys only: `[worldgen.earth_fill] maxWaterSurplus`, new `pruneMinWetColumns` (if step 4). Structures named
above come from the diag3 log (in-repo summary in 12_earth_fill_camps.md 2). Nothing verified against a jar.

### Decisions
- **Value:** keep 0.5 (recommended) unless step 2 finds a wrong cancel. The sample has a wide gap between kept and
  cancelled ratios.
- **Village on a lake:** prune wet pieces (step 4, recommended: matches how pruning handles deep water) or keep
  cancelling the whole village (today; simpler, villages are common).

### Gotchas
- The footprint is the piece's bounding box, not its built columns: a house with a big yard over a pond reads wetter
  than its walls. For trimmed structures (`template_trim`) consider `TemplateTrim.columns(t).footprint(true)` instead
  of the box (same as the soft beard footprint) if the run shows yard-driven cancels.
- Config is read live, but Forge only re-reads the file on change; the `avoidCoasts` test showed a value not reloading
  for 10 minutes (12_earth_fill_camps.md 2). Restart the scratch server between values.
- Meta structures are judged part by part (`filterParts`, `EarthFill.java:690-705`): a wet part cancels the whole meta
  and the slot falls back to the plain anchor.

### Tests
- ST-206 Share value [scratch]: the run of step 2; every share-only cancel probed; none on a shore with mostly dry
  footprint.
- ST-207 Unlimited override with `log = ALL` (bug A) [scratch]: `overrides = ["#creativeengineering:type/village=-1,-1,-1"]`,
  `log = ALL`: no village logs `CANCELLED`, the footprint numbers still print.
- ST-208 Wet piece pruned (only with step 4) [scratch]: a village at a lake shore logs `pruned K of N` with a
  footprint reason and keeps its dry part.

---

## structures/earthfill #3: Sunk-start candidates not tagged
Status Testing (built 2026-10-30, TODO row) · Work data · TODO `structures/earthfill #3`

### Goal
Decide which other structures with a negative start height get the `creativeengineering:terrain_adaptation/sunk_start`
tag (beard levels the ground at the projected surface instead of at the sunk bottom), and tag them.

### Now
- Tag list: `kubejs/server_scripts/worldgen/terrain_adaptation.js:98-101` (the 6 Campsite camps).
- Code: `TerrainAdaptation.sinkOf` (`TerrainAdaptation.java:233-246`) returns `-start_height` only for a
  `JigsawStructure` with a constant absolute start height below 0 and a `project_start_to_heightmap`; anything else is
  logged "has no constant start height below 0 projected to a heightmap; ignored" (161). `rigid()` (395-414) adds the
  sink to every Rigid of the start; earth fill's footprint layer uses it too (`EarthFill.java:722, 838`).
- Vanilla's Beardifier only adds a beard for **rigid** pool pieces (and junctions); terrain-matching pieces get none.
  JigsawPlacement places a rigid child of a terrain-matching parent at the surface height of its jigsaw (not relative
  to the parent). So for a structure whose start piece is terrain-matching, nothing is sunk under a beard, and raising
  every rigid child by the sink would bury their floors by that many blocks. (1.19.2 JigsawPlacement/Beardifier
  behaviour from memory of the vanilla source; not re-read here, the vanilla jar isn't in this session.)

Candidates, read from the jars (verified: `PhilipsRuins1.19.2-10.8.jar`, `Towns-and-Towers-v.1.10-_FORGE-1.19.2_.jar`;
none is overridden in `kubejs/data`):

| Structure | start_height / heightmap | adaptation | Start piece | Template bottom | In earth_fill/exempt | Verdict |
|---|---|---|---|---|---|---|
| `philipsruins:ancient_crypt` | -5 / OCEAN_FLOOR_WG | beard_box | `philipsruins:crypt_start`, rigid (pool json has comments, lenient parse) | 8x7x9 entrance, only y 5-6 above ground when sunk 5 | no | **tag** (beard_box at the sunk bottom digs a 5-deep pit around the entrance; tagged, the entrance roof sits at the surface) |
| `philipsruins:antiquus_crypta` | -5 / OCEAN_FLOOR_WG | beard_box | `philipsruins:antiquus_crypta_main`, **terrain_matching** | - | no | **don't tag** (start piece has no beard; children would be buried) |
| `philipsruins:ocean_fortress` | -5 / OCEAN_FLOOR_WG | beard_box | `ocean_fortress_main`, rigid, 24x21x43, y 0 = 909 sand | sand plate meant to sit in the seabed | yes | tag after a look (seabed pit vs half-buried) |
| `philipsruins:ocean_ruins` | -3 / OCEAN_FLOOR_WG | beard_thin | 12 rigid ruins, y 0 dirt/gravel/sand | buried base | yes | tag after a look (same) |
| `philipsruins:ancient_ruins` | -1 / OCEAN_FLOOR_WG | beard_thin | 22 rigid ruins, y 0 = grass + dirt | 1 layer sunk | no | optional (a 1-block dip ring today; tag if it shows) |
| `philipsruins:rare_ruin` | -1 / WORLD_SURFACE_WG | beard_thin | **terrain_matching** | - | no | **don't tag** (also `plan/move_to_nether`) |
| `philipsruins:end_gate_fortress` | -1 / MOTION_BLOCKING | beard_thin | (End) | - | no | optional, End only, low value |
| `towns_and_towers:exclusives/village_nilotic` | -1 / WORLD_SURFACE_WG | beard_thin | `kaisyn:village/exclusives/nilotic/base_plate`, legacy, **terrain_matching** | - | no | **don't tag** (houses are surface-projected children; tagging buries every house floor 1 block) |

### Build
1. Optional hardening (patcherMod, recommended so a wrong tag can't bury houses): in `sinkOf`, read the start pool's
   elements (`JigsawStructure` field `startPool`, SRG `f_227607_`; read it the same way `START_HEIGHT` is read at 230)
   and return 0 with a warning when any element's `getProjection()` is `TERRAIN_MATCHING`. Further option: apply the
   sink only to pieces whose jigsaw chain to the start is rigid-to-rigid (EarthFill already builds the tree with
   `EarthFill.links`, 600-625); not needed for the tags recommended here.
2. Look in game first (`/place structure philipsruins:ancient_crypt` on a hillside and on flat ground, the same for
   `ocean_fortress` and `ocean_ruins` on a seabed; F3 heights of the ground ring against the template's top layer).
3. Add the chosen ids to the `sunk_start` list in `terrain_adaptation.js:98-101`, with a comment per group (crypt
   entrance; seabed ruins). Rejoin, check the load line `[terrain-adaptation] world load: ... N sunk starts` (ST-200's
   line) has the new count and no "ignored" warning.
4. Update structure_tuning.md 3.15 "Sunk starts" paragraph (the "Opt-in, because ... were not looked at" list).

### Ids and data
Structure ids above (verified in the jars). Tag `creativeengineering:terrain_adaptation/sunk_start`. Fields:
`JigsawStructure.startHeight` `f_227608_`, `projectStartToHeightmap` `f_227610_` (used today); `startPool`
`f_227607_` (unverified SRG name: check with `javap -p` on the mapped class or the mappings before using it).

### Decisions
- **Which to tag:** `ancient_crypt` yes; `ocean_ruins` and `ocean_fortress` after the look (recommend yes: their sand /
  gravel bottoms are made to be buried, and the beard pit just widens the hole); `ancient_ruins` only if the 1-block
  ring shows; never the three terrain-matching starts.
- **Hardening (step 1):** recommended, small.

### Gotchas
- Bug B (top of this file) matters more as sinks grow: at sink 5 the beard fills only 1 block under the crypt's
  bottom, and earth fill today assumes 6.
- The seabed ruins are in `earth_fill/exempt` (`earth_fill.js:16`), so earth fill never judges them; the tag changes
  only their beard.
- Sink raises the beard of every rigid child too; the crypt's underground rooms are rigid children of a rigid start,
  so they get a beard level 5 above their floor. Underground that fills a little around the rooms (ground that is
  solid anyway); check one crypt's rooms for filled corridors next to them.
- New chunks only.

### Tests
- ST-209 Crypt entrance flush [scratch]: a tagged `ancient_crypt` on a slope: the ground around the entrance is at the
  entrance roof's level, no pit; its rooms open.
- ST-210 Seabed ruins half buried [scratch] (if tagged): the seabed around an `ocean_ruins` piece is at its original
  height, the ruin's bottom 3 layers in the sand.
- ST-211 Terrain-matching start refused [scratch] (with step 1): tag `village_nilotic` on a scratch server only -> the
  load log warns and ignores it.

---

## structures/earthfill #4: Undead castle cancelled by earth fill
Status Testing (built 2026-10-30, TODO row) · Work data · TODO `structures/earthfill #4`

### Goal
Get `eugene:undead_castle` to survive often enough to be found, without floating pads.

### Now
- Structure: `kubejs/data/eugene/worldgen/structure/undead_castle.json` (jigsaw, `beard_box`, start height 0 on
  `WORLD_SURFACE_WG`, one-piece pool `kubejs/data/eugene/worldgen/template_pool/undead_castle.json` ->
  `eugene:plains_undead_castle`). Group `surface_dungeons` (`kubejs/generation_scripts/worldgen/structure_groups.json`
  929-936: weight 1, frequency 0.026314, `fit_base` 1 / 0.0255). Fits: `land/plains`, `land/highlands`, `land/taiga`
  (`structure_tags.json`, e.g. `land/highlands` from line 1177); also `dungeon/surface_modded`.
- Template (read from `kubejs/data/eugene/structures/plains_undead_castle.nbt`): 43x28x45, all 1,935 columns hold a
  block at y 0; 2,495 earth blocks in the bottom 3 layers (y 0: 1,563 dirt/grass), so it is `huge` (bounds 2,000).
  1,091 columns have a built block, 844 are pad only (the courtyard and an apron round the walls): a walled compound.
- Scratch M2 (02_new_mods_tests.scratch.md 142-146): 5 rolls, 4 cancelled: over air after the beard 683 / 314 / 407 /
  514 (huge limit 320), mound columns 448 / 264 / 0 / 341 (limit 128). Only one ([184, -158]) was cancelled on the
  mound limit alone (air 314). Terrain relief under two of them 30 and 12 to 15 blocks. The fifth was kept and then
  dropped by the peaceful radius. Phase 7 (07_phase_7.md 2.1) adds that a mound column nearly always comes with air
  or water, so relaxing the mound limit alone would have saved 1 of the 4.

### Build
Option set (pick in Decisions):
1. **Override** (config): `[worldgen.earth_fill] overrides = ["eugene:undead_castle=24,320,400"]` in
   `config/creativeengineering-common.toml:1405` (water 24 = the global default, air 320 = the huge default, mound 400).
   Saves mound-only cancels; leaves real overhangs cancelled.
2. **Narrow the fits** (data): remove `eugene:undead_castle` from `land/highlands` in `structure_tags.json`, then
   `python3 tools/gen_structure_groups.py --fit-rates` to re-solve its frequency over the remaining fits (it has a
   `fit_base`, so the run starts from 1 / 0.0255; check the diff of every other fitted member, as for the fresh
   watchtower row in rates-and-groups), then `tools/test_structure_groups.sh`.
3. **Compensate the rate** (data): raise its frequency by the measured survival (e.g. x2.5 at 40% survival, capped at
   1.0). The fit solver can't see earth fill, so add a note in its `structure_groups.json` entry and keep `fit_base`
   as is; `--fit-rates` would overwrite a hand value, so set `fit_keep`-style protection or re-apply after each run
   (check `tools/gen_structure_groups.py` 1265-1420 for how hand values survive).
4. Before choosing, measure: scratch run with `log = ALL` at density 4 over two r 400 squares in plains/taiga/highlands,
   `grep 'eugene:undead_castle' latest.log`. The log line has no biome, and chunks stopped at `structure_starts` hold
   placeholder biomes, so read the biome source at each chunk with a scratch KubeJS probe calling `getNoiseBiome` (the
   trick from 02_new_mods_tests.md 6), and tabulate cancels per fit. Optional small patcherMod help: add the start's
   biome to the `[earth-fill]` log line under `log = ALL` (`EarthFill.java:734-742`).

### Ids and data
`eugene:undead_castle`, `eugene:plains_undead_castle` (kubejs/data, verified by reading the files). Override syntax:
`"<id>=<water>,<air>[,<mound>]"` (`EarthFill.parseOverrides`, `EarthFill.java:514-528`).

### Decisions
- **Recommended:** 4 (measure), then 1 (mound 400) + 2 (drop `land/highlands` if most cancels are there). Don't relax
  the air limit: over-air 400 to 700 is a floating pad you can see.
- Alternative: accept the rarity (it is a hostile surface dungeon with a boss; rare is fine) and close the row.

### Gotchas
- The kept M2 castle stood in `alpine_highlands`, so highlands is not hopeless; decide on the per-fit numbers.
- Switching the castle to `terrain_adaptation/soft` does not help earth fill: the soft beard shares `beardReach`, and its
  footprint is still the whole box (every column has a block). `template_trim` would drop the 844 pad columns (the
  courtyard becomes natural slope) and move it to `large` (1,338 earth in built columns), whose limits (160 / 64) are
  tighter: worse.
- The drum tower (`eugene:ruined_drum_tower`, stone-base tag, row #5) has the same problem in Phase 7 (huge, 1 of 1
  cancelled at 1,366 air) and in diag3 (2 cancelled too wet); look at both in the same run.

### Tests
- ST-212 Castle survives [scratch]: after the change, in the measurement squares at least half of the rolled castles
  are kept, and none of the kept ones has earth over more than 6 blocks of air (probe one on a slope).

---

## structures/earthfill #6: Earth-fill limits for modded structures
Status Planned · Work config · TODO `structures/earthfill #6`

### Goal
Calibrate `[worldgen.earth_fill.sizes]`, `maxOverWater` and overrides against modded structures.

### Now
Already done: ST-52 ran on the scratch server 2026-10-19 (Phase 7, [07_phase_7.md](../testing/structures/07_phase_7.md)
2.1, structure_tuning.md ST-52): main square r 800, density 4, `log = ALL`, prune working. Default limits
cancel 9% of small and 12% of medium surface starts, 1 of 12 large (carp fish pond, air 209) and the one huge (drum
tower); underground cancels are mostly Subterrestrial ocean cabins in open sea (correct). The log's conclusion:
"Nothing here argues for a change except the drum tower". The only things left are the 1.0.9 footprint share (#2) and
the two huge structures (#4, #5).

### Build
Nothing to build. Close the row, or fold it into #2 (the next `log = ALL` run). If kept: in the #2 run, also tabulate
per-size percentiles again (the 1.0.9 jar changed the count only through the footprint), and look at the carp pond
(`farmers_structures:aquacultures_carp`, `none` adaptation, start height -4): it is `large` and was cancelled on air
because `none` counts every earth block of its sunk dirt bowl; the terrain brief
([structures_terrain.md](structures_terrain.md) #1) proposes soft + trim + sunk_start for it.

### Decisions
- Close (recommended) or merge into #2.

### Tests
- ST-52 itself is still unticked in structure_tuning.md (it is a DATA test); tick it when the row closes.

---

## structures/earthfill #7: Worldgen hot spots
Status Testing (A and B built 2026-10-27, perf.md 10.8; SQ-WGP-03, -04 PASSED 2026-10-27) · Work patcherMod · TODO `structures/earthfill #7`

### Goal
Remove the two pack-side worldgen costs Phase 10 found: twigs' obelisk check (3.1% of worldgen CPU) and the repeated
earth-fill terrain prediction (about 4%: 62% of creativeengineering's 6.8%).

### Now
- Phase 10 ([10_phase_10.md](../testing/structures/10_phase_10.md) 2-3, locate.md 3.7): JFR of an Overworld Chunky
  pregen. `structure_starts` runs about 16 times per chunk (placement-priority predictions and meta predictions rebuild
  nearby starts); each rebuild reruns `EarthFill.predicted` -> `ChunkGenerator#getBaseColumn` per 2x2 cell.
- `EarthFill.predicted` (`EarthFill.java:451-472`) caches columns per start only (a new `Long2ObjectOpenHashMap` per
  call, from `filter` 678 and `filterParts` 699). `PlacementPriority` caches its predictions per (generator, set,
  chunk) (`PlacementPriority.java:316-320`), but the real generation of that chunk later builds the start again and
  reads the terrain again.
- Twigs (verified: `twigs-forge-1.19.2-3.0.1.jar`, `com/ninni/twigs/world/gen/structures/ObeliskStructure`, decompiled
  to scratchpad `decomp/twigs_obelisk/`): `findGenerationPoint` (`m_214086_`) calls `canGenerate` first, which reads
  `getBaseColumn` for all 121 columns of an 11x11 square (and scans y 80 down to sea level in each) **before** vanilla's
  biome test. Its biomes are `minecraft:nether_wastes`, `minecraft:crimson_forest`
  (`data/twigs/tags/worldgen/biome/has_structure/bloodstone_obelisk.json`). In the pack it is in `group/nether_ruins`
  (`structure_groups.json` 1718: weight 194, frequency 1.0; spacing 14, separation 7). The profile was an Overworld
  pregen, so the set's slots are tried in the Overworld and every obelisk pick pays the 121 columns (1.19.2's
  `ChunkGenerator#createStructures` walks every structure set; inferred from the profile, the vanilla source wasn't
  re-read here).
- A helper already answers "can this structure spawn in this dimension": `MetaSlots.canSpawnAmong(Structure,
  Set<Holder<Biome>>)` (`MetaSlots.java:391-395`, biome rules applied; fits are inside `Structure#biomes()` via
  `StructureBiomesMixin`).

### Build
**A. Skip structures that can't spawn in this dimension** (generic; fixes the obelisk and every other Nether/End
member tried in the Overworld):
1. New `worldgen/DimensionFilter` (or a method in `StructureFrequencies`): `static boolean impossible(ChunkGenerator g,
   Structure s)`, cached in a `Map<BiomeSource, Map<Structure, Boolean>>` (WeakHashMap outer, IdentityHashMap inner),
   value `!MetaSlots.canSpawnAmong(s, g.getBiomeSource().possibleBiomes())`. Clear on `ServerStoppedEvent` and
   `TagsUpdatedEvent` (biome rules and fits are rebuilt at ServerAboutToStart).
2. Hook: a `@Inject(method = TRY_GENERATE, at = @At("HEAD"), cancellable = true)` returning false, in
   `ChunkGeneratorStructureFrequencyMixin` (it already has a cancellable HEAD inject at line 52) or a new mixin with the
   same `TRY_GENERATE` descriptor string as `ChunkGeneratorEarthFillMixin.java:29-32`. Return false = the pick failed,
   exactly what the biome test does later.
3. Exclusions: `MetaStructure` (its biomes are the anchor's; `canSpawnAmong` on the meta may be wrong) and the
   replacements of `MetaSlots` (they take another structure's biomes: `MetaSlots.narrowBiomes`). Skip the shortcut for
   both (`s instanceof MetaStructure || MetaSlots.isReplacement(s)`; add that accessor if missing).
4. Narrow fallback if A is judged too broad: a mixin on `com.ninni.twigs.world.gen.structures.ObeliskStructure`
   `m_214086_` HEAD that returns `Optional.empty()` when the noise biome at the chunk's middle (quart coordinates, y at
   sea level) is not in `this.biomes()`. Needs `"twigs"` as a soft target (`@Pseudo` or a mixin plugin check) so the jar
   doesn't hard-depend on twigs.

**B. Cache the predicted terrain across starts:**
1. In `EarthFill`, replace the per-start map with a bounded cache per `(ChunkGenerator, RandomState)`: key
   `ChunkPos.asLong(cx, cz)` of the grid cell centre, value a compact column: `byte[]` of `Kind` per y (384 bytes for
   -64..320) plus `top` and `topWater`. `Kind.SEABED` is derived at read time from `topWater` as today.
2. Bounded: Guava `CacheBuilder.newBuilder().maximumSize(65_536).build()` (Guava ships with Minecraft; about 26 MB at
   400 bytes a column) inside a `WeakHashMap<RandomState, Cache>` guarded by `synchronized`, or a striped
   `ConcurrentHashMap` with a clear-when-full rule. Worldgen runs on several threads: the cache must be thread-safe.
3. Key the cache on `sampleGrid` too (a config change mid-run must not mix grids) and clear it on `ServerStoppedEvent`.
4. `predicted(...)` keeps its signature; it reads through the cache. The `LevelHeightAccessor` passed today is the chunk
   (min/max build height); the cache must store min y with the column (prediction scratch chunks share the level's
   height).
5. Don't use it for DH's clipped chunks: `getBaseColumn` is not clipped (DistantClip only clamps noise for marked
   chunks), so sharing is safe, but check that `DistantClip.clamp` doesn't see the prediction's accessor as a marked
   chunk.

### Ids and data
Twigs: class `com.ninni.twigs.world.gen.structures.ObeliskStructure`, method `m_214086_(Structure.GenerationContext)`
(SRG, verified in the decompile), `canGenerate(GenerationContext, BlockPos)` (static). Structure `twigs:bloodstone_obelisk`.
Vanilla: `ChunkGenerator#tryGenerateStructure` `m_223104_`, `getBaseColumn` `m_214184_` (as used by twigs).

### Decisions
- **Whether worldgen speed matters** (the row's own decision): the two together are about 7% of worldgen CPU on the
  worker threads, while DH's LOD threads take 35% of all CPU during a pregen. Recommended: do A (small, generic, also
  cuts other-dimension structures from the Overworld's 16x rebuilds), do B only if a later profile still shows
  `EarthFill.predicted` near the top.
- A generic (recommended) vs the twigs-only mixin.

### Gotchas
- A must not change results: anything it skips would have failed the biome test anyway. Verify with
  `/cwglocate census` counts before/after on the same seed, and `tools/test_structure_groups.sh`.
- `StructureCheck`/`/locate` don't go through `tryGenerateStructure`; the locate path has its own dimension check
  (locate.md 7.3.2 dimension check).
- Twigs' `canGenerate` also runs in the Nether, where it is needed; A leaves that alone.
- B: the prediction must be exactly what the uncached version returns, or placement-priority predictions and real
  generation disagree (structure_tuning 3.22 relies on predictions being exact). It is, as long as the key includes
  the generator, RandomState and grid.

### Tests
- ST-213 Same starts with the filter [scratch]: two fresh worlds, same seed, Chunky r 64 at 2144 -2592, filter on/off:
  identical `/cwglocate census` member counts; the JFR shows no `ObeliskStructure#canGenerate` in the Overworld.
- ST-214 Terrain cache exact [scratch] (if B): `log = ALL` earth-fill lines identical with the cache on and off over
  the same square; JFR share of `EarthFill.predicted` down.
- Offline: an `EarthFillTest` case that the cached and uncached `Terrain` answer the same for a fake generator.

---

## structures/earthfill #11: Desert structures spawn in water
Status Testing (fixed through `structures/earthfill #14`, 2026-10-30) / Work data / config / TODO `structures/earthfill #11`

### Goal
Pyramids and other desert structures no longer start flooded.

### Now
- **The row names the wrong knob.** `maxWaterSurplus` (default 0.5, `CEConfig.java:1966-1975`, read by
  `WorldgenConfig.earthMaxWaterSurplus()` line 116, rule `EarthFill.Footprint.tooWet` and its use at `EarthFill.java:836`) makes
  the start CANCEL when the footprint's water outnumbers its ground; raising it lets more water-covered starts through, which
  is the opposite of clearing the water. Config comment: `config/creativeengineering-common.toml` lines 1472-1480.
- The knob that fills water is `[worldgen.earth_fill] maxOverWater` (default 24; "most earth blocks that may stand in water the
  beard won't fill", `earthMaxOverWater()` line 115): raising it lets the earth fill and the beard cover more water instead of
  cancelling. `maxWaterSurplus` is global only; `maxOverWater` has per-structure and per-tag overrides:
  `overrides = ["<structure id or #tag>=<water>,<air>,<mound>"]` (`EarthFill.parseOverrides` line 514, `Override(key, a, b, c)`,
  a = water, b = air, c = mound; `-1` = no limit).
- The desert temples are the group tag `group/desert_temples` (structure_tuning.md 3.24: `betterdeserttemples:desert_temple` and
  `minecraft:desert_pyramid`); other desert structures are in `structure_groups.json` (`village_desert`,
  `philipsruins:desert_structures`, the desert watchtower and pillager outpost). Whether the flooded pyramids are cancelled-then-
  missing or placed in water is the first thing to find out (the earth-fill log, `[earth-fill]` lines in `latest.log`).

### Build
1. Reproduce a flooded pyramid with the earth-fill log on (`[worldgen.debug]` / `log = ALL` as in
   `testing/structures/12_earth_fill_camps.md`); read its water / air / mound numbers.
2. Add a per-tag override line for the desert group, e.g. `"#creativeengineering:group/desert_temples=100000,-1,-1"` (a large
   `maxOverWater`, the other limits unchanged), in `config/creativeengineering-common.toml` and `configs_edited/root_config/config/`.
3. If the water share (`maxWaterSurplus`) is what cancels them, the override format has no slot for it: extend `Override`
   with a fourth value (surplus) and its parser, `EarthFill.java:509-526`, or raise the global default.

### Decisions
Which limit and which scope: the row says "the new value; one desert-wide override or a per-structure one". Recommend `maxOverWater`
through one tag override on the desert groups (the machinery exists), keeping the global surplus at 0.5.

### Gotchas
New chunks only. `/place structure` is not checked by earth fill. Related: `structures/rates-and-groups #10` (pyramid separation).

### Tests (ST-)
- ST-229 (proposed): census of 30 desert pyramid starts (scratch server, new chunks): none stands in standing water; the log
  shows the fill instead of a cancel.

---

## Built, needs play

| Row | Status | Notes from this pass |
|---|---|---|
| #1 Earth fill on water, camp plinths, `avoidCoasts` | Testing | DEPLOYED with 1.0.9 (2026-10-19). Two bugs in it, A and B at the top of this file |
| #5 Stone bases are opt-in | Testing | Only `eugene:ruined_drum_tower` (`earth_fill.js:33-35`). The drum tower is cancelled often (Phase 7: huge, 1 of 1; diag3: 2 too wet): look at it with #4. Candidate for the tag later: `eugene:piglin_camp` (Nether; plan_1.0.6.md 2.1) |
| #8 Water fill | Testing | ST-56, 58, 59, 60 passed; ST-57 has data (07_phase_7.md 2.4) and is still unticked |

## Writeup corrections made

- [structure_tuning.md](../structure_tuning.md) 3.13 footprint paragraph: added that an all -1 override skips the share
  only below `log = ALL` (bug A).
- [structure_tuning.md](../structure_tuning.md) 7.5 heading: added "DEPLOYED with 1.0.9 2026-10-19".
- [structure_tuning.md](../structure_tuning.md) 7 Todo, "Terrain adaptation leftovers": the modded earth-fill
  calibration ran as ST-52 in Phase 7; only the 1.0.9 footprint share needs its own run.
- [testing/structures/02_new_mods_tests.md](../testing/structures/02_new_mods_tests.md) 4 item 2: "(patcherMod 1.0.7,
  not built or shipped)" -> "(patcherMod 1.0.7, DEPLOYED 2026-10-18)".
- (structure_tuning.md 3.23's "None is in `mods/`" and 12_earth_fill_camps.md 3's "Not deployed" were stale too; another
  pass corrected both the same day before this one got to them.)
