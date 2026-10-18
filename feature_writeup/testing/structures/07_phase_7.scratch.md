# M7 scratchpad: Phase 7 (8.0.7, calibration runs)

## State (keep current)

- Constraints (user 2026-10-14): ignore Nether/End; ignore structure-to-structure rates; correctness only.
- Milestone: M7. Tests: ST-52 (+159, 160, 162, 167, 168 from the same lines), ST-169, ST-57, ST-67, ST-53, ST-58,
  ST-54, ST-59. ST-52 and ST-57 are data for the user (numbers per size / limits), not pass/fail.
- Server: session 4 (d6feb4e4) scratchpad srv/, same jar (build/libs 1.0.6 of 12:44). Phase 7 base toml = scratchpad
  toml_m7_base.toml = M5 base (earth_fill log ALL, meta log, placement log) + [worldgen.debug] structureDensity
  1.0 -> 4.0 + [worldgen.water_fill] log CANCELLED -> ALL. srv-only scripts zz_scratch_tick.js, zz_scratch_bmap.js.
- World st_c (seed 8675309, 4 locator threads): main Chunky square radius 800 blocks at -1400 -2592 (/zbmap r 1536:
  58% land, coasts). Switch tests: one reboot each + a fresh adjacent square.
- Running: st_c (boot Done 17:47:50, 293 s at density 4); Chunky main square started 17:48.

## Log (append-only)

- 17:43 M7 started; st_c boot (new world, density 4: spawn prep 293 s). 17:48 Chunky square r 800 at -1400 -2592 (/zbmap r 800: 68% land). Switch squares (r 400, disjoint): 200 -2592 (66%), -1400 -4200 (84%), -1400 -1000 (36%, coast), -3000 -2592 (29%).

## Results

| Test | Result | Evidence |
|---|---|---|

## Open issues
