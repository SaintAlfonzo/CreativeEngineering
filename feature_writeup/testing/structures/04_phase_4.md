# M4: Phase 4, config switches (8.0.4)

2026-10-14. Live notes: [04_phase_4.scratch.md](04_phase_4.scratch.md). Result lines are inline in
[structure_tuning.md](../../structure_tuning.md) section 8, with a summary above "8.0.5 Phase 5".

## 1. Setup

- Same scratch server as M2 and M3 (patcherMod `build/libs` 1.0.6 of 01:28, seed 8675309).
- Baseline toml for this phase = the instance defaults plus three log switches (earth fill `ALL`, meta structures,
  placement priority). Structure density went back to 1 (it was 4 in M2 and M3). Every switch was put back to this
  baseline after its test.
- Worlds:
  - `st_d` for rejoin and log tests;
  - `st_e` and `st_d` for the terrain adaptation comparison (the same new area generated in both);
  - a new `st_g` for ST-109.
- The log-only switches that don't affect each other (ST-46, 37, 38, 55, 60, 75, 83) ran in one boot instead of one
  reboot each. Each has its own log line, so nothing is ambiguous. In total: 10 boots, 11 s each on an existing
  world, 98 s for a new one.
- File-edit tests edited the scratch server's copy of `kubejs/generation_scripts/worldgen`, then restored it from
  the instance. ST-25 has to run the generator in the instance. Its folder was backed up first and restored with
  md5 checks (all 33 files identical, nothing new left). That's not done with git, because those files carry
  uncommitted work.

## 2. Results

| Test | Result | Evidence |
|---|---|---|
| ST-46 | PASSED | Biome rules "off in CEConfig" line |
| ST-37 | PASSED | Frequencies "off in CEConfig" line, no below-1 roll line |
| ST-38 | PASSED | "Pool tuning is off" in the pool report; 24 of 25 generated plains villages get Dynamic Village houses again |
| ST-55 | PASSED | No earth-fill lines over 18 new starts that it normally logs |
| ST-60 | PASSED | Water fill off: no lines; on: lines only for tagged structures, none for a jungle temple in the same area |
| ST-65 | PARTLY | Own beard flattens the ring around tagged structures (550 vs 153 and 559 vs 363 columns at floor height); template-trim half not run |
| ST-75 | PASSED | "0 meta structure(s) (22 left out)", report marks, no meta starts |
| ST-83 | PASSED | Fits "off" line; mini clock towers found in plains and snowy plains (their own list) |
| ST-24 | PASSED | Typo -> ERROR "unknown field" + "nothing applied", no frequency line, report lists it |
| ST-36 | PASSED | Groups off: WARN + "GROUPS OFF, 0 groups"; the 1 set written is an all-disabled standalone set |
| ST-109 | PASSED | New world: replacements left out, swamp hut not found, YUNG hut found |
| ST-21 | PASSED | Sky temple disabled and not found; balloon found |
| ST-23 | PASSED | Boats spacing 60 live; live check 0 mismatches over 178 sets |
| ST-25 | PASSED (rate part skipped) | Generator moved the glacial hut to remote; loads clean; it locates; files restored |
| ST-13 | PASSED | Plains pet shop "removed" in its pool |
| ST-35 | PASSED (locate spread skipped) | x0.25: frequency 0.25 on every grid, lone village 1 per 96 chunks instead of 48 |
| ST-34 | PASSED, [game] fly left | x4: every group grid halved; 3 subterrestrial sets stay at their densest grid (named) |
| ST-76 | SKIPPED | No old jar in `mods/` |

## 3. Bugs

None found in Phase 4.

## 4. Notes for you

- **ST-65:** the `templateTrim = false` half (pads and notches come back) needs a third world. I left it for the
  client list.
- **Outdated counts in some test texts** (newer counts confirmed instead):
  - ST-36 expects 0 sets written; it is 1, the all-disabled Revamped Shipwrecks set (by design);
  - ST-34 expects "all exact, 9 groups, 126 sets"; it is 15 groups, 214 sets, plus 3 subterrestrial sets at their
    densest grid;
  - ST-23 names boats 41/21; it is now 48/28.
- **Live check, section 3:** 149 live structures are in no set, all as designed (structure_tuning.md 1.x table):
  - 133 Terralith pointer structures;
  - the 7 ruined portals (removed on purpose; ST-24's portal was found only because nothing was applied);
  - vanilla shipwrecks (Revamped Shipwrecks empties their set);
  - `eugene:castle` (split in 7.4.4);
  - the rest. The 142 in that table plus the newer ones.

## 5. Values changed and restored

- Scratch server only: system switches, density, water-fill log, structure_groups.json / pool_config.json edits
  (all restored). Instance: ST-25's three files were changed by the generator and restored byte for byte.
- No jar installed, nothing deployed.
