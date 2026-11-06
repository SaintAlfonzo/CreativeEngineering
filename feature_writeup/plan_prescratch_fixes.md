# Pre-scratch fix pass (planned 2026-11-02)

**Status 2026-11-02 evening: executed.** Every row is fixed, built, or investigated with a probe/test row; TODO rows moved
to Testing (except `gameplay/time #4`, still Bug pending CQ-NEW-01, and `biomes/netherblocks #3`, not needed: no flower
edit). Queue: "Pre-scratch fix pass" at the end of [scratch_queue.md](testing/scratch_queue.md) (SQ-PST, SQ-PSD, SQ-PSM,
SQ-PSF, SQ-PSW, SQ-PSU, 52 rows), not run. Jars built and installed 2026-11-02: patcherMod, Creative Compat, Creative
Calendar, Creative Delight, Creative Brewing. After the first boot: `/cegen` for the item physics and WTHIT silk-touch
generators (azalea wood ids).
Premises that were wrong: crops #31, #32, #34, #38 and item-physics #4 were already built (10-28 / 10-31); oats #35 shipped
in 1.1.06; baby squid (#21) is not a missing rule but a low chance by design; there are no D&D glowstone geodes; the time
and season code is in Creative Calendar, crops in Creative Delight, potions in Creative Brewing.
Owner decisions 2026-11-02, applied the same day: oats #35 kept open for a visual sign-off (CQ-PSD-09); azalea logs
become oak logs, leaves and trees stay (as built); azalea racks pulled from Creative Delight; modded grasses slowed in
winter too (SQ-PST-06 to -11); no cap change for the birch tower, box spawn attempts now start within the box height
instead (`NaturalSpawnerBoxHeightMixin`, SQ-PSM-03); baby rates unchanged; air under a spire cancels it (as built);
Vintage sweet berry and glow berry jams removed (as built); the vinegar and apple sauce jar food values reverted.
Jars reinstalled 14:08: patcherMod, Creative Calendar, Creative Delight.

Goal: fix or build the rows below before the next scratch run. Each row gets a scratch queue row (not run), a writeup
update, a changelog bullet where a player can notice it, and a TODO status change to Testing.
Rows are TODO ids; briefs are in `feature_writeup/briefs/`.

## Working rules for this pass
- Reproduce or locate first (grep `logs/latest.log`, the row's run log), then change code. No guessing from the row text.
- Batch by build target so there are few restarts: **A** patcherMod (one rebuild/install), **B** KubeJS/data/lang
  (`/reload` or restart), **C** Creative Delight / other jars, **D** tools/queue only.
- Never install a jar while the game runs. Hand-edited configs are copied to `configs_edited/`.

## Phase 1: bugs (investigate, then fix)

| Row | First step | Likely fix | Target |
|---|---|---|---|
| `mobs/pillagers #6` | Probe `spawn_boxes.json` for the siege, camp, ship and witch parts; compare to the ST-323 mix | Correct the box config or its resolution order | A/B |
| `structures/contents #6` | Check box resolution plus monster cap at the birch tower (judged_w2.md) | Exempt box spawns from the cap, or a distance fix | A |
| `mobs/tweaks #21` | Find why baby squid, glow squid and dolphin never spawn (`babyMobs`, spawn data) | Add them to babyMobs' spawn rule | A |
| `farming/trees #4` | Check leaf `distance` and log tag for mulberry in the log/leaf tags | Add oak logs to the mulberry leaf support tag | B |
| `gameplay/time #7` | Recheck at 20 s: sync lag or no client refresh | If a bug, resync season on the date command packet | A |
| `gameplay/time #4` | Needs more investigation: TI-31 in game (night speed, client time, clock property) | After evidence only | A |
| `gameplay/misc #19`, `#20` | `config/curios-client.toml`, `kubejs/server_scripts/tags/curiostags.js`, `item_dict.js` | Remove mouth slot, register foot slot, add face slot to the pipe, fix face render | B + config |
| `ui/names #12` | `namechanges.js:308`; find the winning lang key (tobacco mod lang, priority) | Add the right key | B |
| `potions/effects #13` | Better Beacons `PhantomSpawnerMixin` | Higher-priority inject that skips only that player | A |

## Phase 2: farming

| Row | Plan | Target |
|---|---|---|
| `farming/crops #19` | Village farms use the Rustic crops' wild-crop biome rules | C |
| `#28` | Grasses winter-only offseason; check how season types treat spreading | A/config |
| `#29` + `#38` | Hoe area harvest picks tomatoes on ropes and sticks; treated sticks placeable and growing tomatoes | A |
| `#35` | Oats: recolour of the wheat texture, taller and bushier with variable height | B + assets |
| `#37` | Hemp harvests by right click | B |
| `#31` | Watering can fills from wells and cauldrons | A/B |
| `#32` | Create spout gun wets farmland | A |
| `#34` | Watering particles on berry bushes | A |
| `#20` | Stems connect only to the fruit they grew (multifruit) | A |
| `#21` | Melon stems drop fertile-soil rule; audit other melon-like crops | B |
| `wild-crops #7` | Raspberries and blueberries share spawn biomes (data); rerun the wild crop generator | B/D |
| `trees #5` | Remove azalea wood; swap azalea tree for oak (check leftovers in worldgen, tags, JEI) | B |

## Phase 3: worldgen and structures
- `biomes/netherblocks #3`: rerun `gen_wild_flowers.py` with `/cegen` after any flower edit (do last, after flower rows).
- `#4`: scan the Overworld for glowstone (D&D geodes, structures, features, ores); fix hits.
- `structures/contents #1`: tag `plan/move_to_nether` structures into Nether grouping.
- `#5`: Quark End spire air-under-footprint check cancels placement.
- `#8`: find why the ocean monument has no mobs (guardians, elder guardians).
- `structures/earthfill #18`: queue rows already written (SQ-EFB-00 to -16); confirm they are in `scratch_queue.md`. `maxWaterSurplus` set to 0.5 in both config copies 2026-11-02 (was 0.0); SQ-EFB-05 must run with 0.5, update its row.

## Phase 4: mobs, gear, potions, mechanics
- `mobs/tweaks #18`: squid flee from whales from a far distance (decided 2026-11-02; squid are the fleeing mob).
- `gear/tuning #14`: trims limited to standard armor; verify appearance.
- `potions/potion-buckets #3`: dispensed clouds credit the dispenser's owner.
- `merges/materials #3`: prepared rotten flesh block and item; find why no custom texture shows.
- `gameplay/blockrunner #6`: drop the block runner item tooltip, keep WTHIT.
- `items/item-physics #4`: splash particles when anvils and other entities hit water.
- Jam jars (decided 2026-11-02, covers `dishes/jams #1`, `items/jars #8` and `#9`): jam bottles convert to mason jars, jam jars dropped.

## Phase 5: close-out
- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): PASS SQ-PSD-02, -03, -04, -05, -06d, -06e, -06f, SQ-PSF-01, -03, -05, -06, -07b, -08a, SQ-PSM-01, -03, -04, -06a, -08, SQ-PST-01, -02, -06, -07, SQ-PSU-01, -02, -05, -08; FAIL SQ-PSU-06: lemon and orange jam-from-slices pot recipes return a glass bottle, not the mason jar (CD `fruitsdelight/recipes.js` 50, 59); OPEN (no verdict, rerun row written) SQ-PSD-01, -06, -06b, -06c, SQ-PSF-07, -08b, SQ-PSM-09, SQ-PSU-04, -09; rerun pending (batch fault) SQ-PSD-07, SQ-PSF-02, -04, -08, -09, SQ-PSM-02, -05, SQ-PST-03, -08, -09, -10, -11, -11b, SQ-PSU-03, -07; not run SQ-PSM-06, -06b, -07.
- **r1103 2026-11-03** (scratch server; verdicts in `perf/runs/r1103/` FINAL.md and the slice judged.md): PASS SQ-PSF-08a, -08, -08b, SQ-PSU-07, SQ-PSD-01, SQ-PSF-02, -04, -07, SQ-PST-03, -09, SQ-PSM-06a, -06, -06b, -07; FAIL SQ-PST-08, -10, -11, -11b: Botania vivid grass; Vintage lush grass; IW rooted grass; IW grass over mycelium: no winter slowdown (creativeCalendar `ModdedGrassSpreadSeasonMixin` 33-52; `require = 0` hides a target that does not apply); SQ-PSU-04 OPEN: the control pass spawned no phantoms near either bot in 6 min, undecided.

- Queue rows in `scratch_queue.md` for each change; no scratch server booted.
- Update writeups, TODO statuses, `unreleased.md` bullets, `python3 tools/changelog_lint.py`.
- Hand over with test ids and jars waiting for `mods/`.

