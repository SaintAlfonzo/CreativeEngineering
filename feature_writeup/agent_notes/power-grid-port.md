# power-grid-port

> Create Power Grid 0.6.2 backport (development/backportedPowerGrid, mod id powergrid) - where state lives, jar rules, mechanics that are not obvious from the code

Moved from agent memory 2026-11-16 (last edited there 2026-11-13). Repo status docs win over any status line here.

Power Grid backport, ported P0 to P10 on 2026-11-13 (final gate jar -17). State: `feature_writeup/plan_power_grid.md` (P11 status), `development/backportedPowerGrid/PORT_NOTES.md`, `feature_writeup/power_grid.md` (3.5 bugs B1-B34, 8.1 owner hand list), `feature_writeup/energy_advanced.md` 8 (keep/drop/merge table, H7, D14 inputs), regression plan in `feature_writeup/testing/scratch_run_2026-11-13.md` 9 and its resume point (10).

- **Test jars only from `jar_archive/pwg_gate/`** by exact name; `build/libs` and the module's own `jar_archive/` get overwritten by later builds. Gate runs still use the archived gate jars. Since 2026-11-16 the current build is also in `mods/` (owner ruling D11, plan_process_overhaul.md P3.1, superseding plan E1).
- `port_phase` in gradle.properties is a decimal (P9 = 9.1 to 9.4); `phases.json` gates registration and resources; `tools/pg_phase_check.py` runs in the build and fails on missing client assets (added after an empty `particles/zap.json` crashed every client: 1.19.2 fails resource load for a registered particle without a description).
- Offline solver harness: `tools/test_power_grid_solver.sh` (6 upstream failures are known, listed in tools/pg_solver_test/known_failures.txt).
- Probes `tools/scratch_query/pwg_*_probe.js` (`zpwgg`, `zpwg3`...`zpwg10`); Rhino gotchas found with them are in skill scratch-test gotchas.md.
- Integration points (no fold-in during the port): empty `#forge:cardboard` (4 recipes), pack removes `create:copper_nugget` (circuit_component tag loads 27 of 28), zinc sheet clash (A3), plating deployer uses the IE copper plate.
- 2026-11-13 incident: an agent's script rewrote power_grid.md from a slice and dropped sections 3.6-9; rebuilt from file history. See memory `orchestrated-port-pattern`.

Related: memory `scratch-test-now-or-batch`, memory `patchermod-headless-smoke-test`.
