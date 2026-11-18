<!-- SPDX-License-Identifier: GPL-3.0-only -->
# Bug fixes that need no live diagnosis (2026-11-17)

<!-- generated:status_index begin open_work sha1=260d3a4b1aa6 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 2 open TODO rows (Testing 2), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `creativedelight/members #60` | Testing | S | Salt item click does not melt ice one stage per click (SQ-MELT-08) |
| `largecontent/mod-additions #82` | Testing | S P | Power Grid acid mixing loses its water to Creative Delight's heated water-to-salt mixing |
<!-- generated:status_index end open_work -->

Status: **planned, not built.** Scope: the open Bug rows whose cause is known or readable from code. Left out on purpose:
`gameplay/tnt-and-portal #38` and `tooling/scratch-1115-follow-ups #2` (urgent portal crash), `jei/jei-coverage #8`
(flower crown crash), and the rows whose cause is unknown and needs an in-game look first (section 4). Status is in the TODO
rows, not here. Code facts below were read 2026-11-17 from the row text and file names; re-read before editing.

Short names: DEV = `development/`; CC = `development/creativeCompat`; TP = `development/backportedTempad`;
PG = `development/backportedPowerGrid`; HH = `development/creativeDelight/members/hearth`.

## 1. Ready to build (cause known, no ruling needed)

| Phase | Row | Fix | Jar |
|---|---|---|---|
| A1 | `largecontent/mod-additions #90` | Guard `ElectroZapperRenderHandler.appliesTo` (PG) so a missing `powergrid:electrozapper` registry entry returns false instead of throwing; the handshake refusal stays out of scope | PG |
| A2 | `largecontent/mod-additions #68` | Crow: `nearbyNest` searches +-3 vertically; widen the vertical range to cover the flee height (about 6) so short carry configs stash; low priority | CD hearth |
| A3 | `textures/merges #11` | Weathered cardboard box: fix the block model UVs so the texture wraps the box and the box is full size (model JSON, no texture edits; CC `weathered_cardboard_box` blockstate and model) | CC |
| A4 | `gameplay/tnt-and-portal #29` (chronometer half) | Several chronometers cancel each other's chronitons charging: make extra ones add or be ignored (`ChronometerItem`, `TempadCapabilities`) | TP |
| A5 | `gameplay/tnt-and-portal #29` (display half) | Show Tempad time in game time | TP |
| A6 | `gameplay/tnt-and-portal #31` | Timedoor marker screen: name and color fields render before first click; color refreshes after the color select screen closes | TP |

A4 to A6 touch one jar, so one build. A5 needs the owner's reading of "game time" (section 3, Q3).

## 2. Ready to build once an owner ruling lands

| Phase | Row | Open ruling | Default if no answer |
|---|---|---|---|
| B1 | `gameplay/tnt-and-portal #3` | Item damage fraction and the cap on chain-reaction entities (F2); F3 is settled: only vanilla TNT bursts | damage 25%, keep knockback, cap 16 per chain |
| B2 | `gameplay/tnt-and-portal #15` | Exit-lift rule for wall exits. Also fix the real defect: `PortalGunExitLift` rotates an already rotated content direction, and the mixin misses portals linked in `PortalGunItem` | let Immersive Portals adjust when the exit faces sideways, lift only out of solid blocks |
| B3 | `creativedelight/members #60` | Wire a salt dust click and dispenser to `meltStep` (new behavior), or fix salt.md to the layer route | fix the writeups, no new behavior |
| B4 | `largecontent/mod-additions #71` | Hops half is built; only the rarity or trunk-search ruling remains, then rerun SQ-HH-304, SQ-HOP-06 | keep 1 in 96 chunks, rerun first |
| B5 | `largecontent/mod-additions #82` | Water-first acid mixing: acid priority, a different salt heat, or accept with a JEI note | accept, add the JEI note; the in-water recipe from D4 already exists |

## 3. Rulings and open questions (intake)

Rulings (owner, 2026-11-17): B1 item damage 50%, entity cap 32, knockback kept; B2 let Immersive Portals adjust for sideways exits;
A5 Tempad time is the in-world day clock. B3: salt hits ice and snow directly, a layer only on non-meltable blocks (BUILT 2026-11-17). B5: drop `powergrid_acid_in_water`; the salt recipe is already the Rock Salt Block catalyst mixing, so water-first no longer salts (BUILT 2026-11-17).

1. B4: keep hop rarity as built?

## 4. Left out (cause unknown or needs in-game evidence first)

- `gameplay/time #4` clock icon frozen while sleeping: no cause found offline; waits on TI-31.
- `mobs/tweaks #2` MCA villagers glitch while sleeping: waits on MT-38.
- `farming/crops #42` grass crop season lines: not reproduced; suspected WTHIT side (`ui/wthit #14`).
- `cooking/kitchen #35` knife disappears from a cutting board: no cause; the dict and kitchen tags are the only hits. Needs a log and one repro before any code. Candidate for a short read-only investigation phase.
- `pack/server #36` mob cap level 2 spawns nothing on a superflat, `pack/server #37` boot ERROR lines: need a live run or a log grep.
- `largecontent/mod-additions #83` arm leaves parts loose: matches upstream, port choice only.
- `largecontent/mod-additions #27` Farmer's Structures donor: deployment check (`farmers_upstream/` on the server), not code.

## 5. Order and rules

1. A1, A2, A3, A4 to A6 in that order; each jar rebuilt and installed alone, never while the game runs (CLAUDE.md).
2. B phases after the owner answers; defaults apply only if the owner says so.
3. Each phase: queue rows in `testing/rows/<CODE>.qb` (prefixes from test_prefix_index.md), writeup line, changelog bullet
   for player-visible fixes (A1 crash, A3, A4 to A6, B1 to B3), `python3 tools/changelog_lint.py`,
   `python3 tools/status_index.py`, then a commit per phase pushed to `agent_staging`.
4. No scratch server boot unless the owner asks; rows go to the queue.
5. Do not edit rows' plan cell by hand; use `tools/todo.py` once the owner approves this plan.
