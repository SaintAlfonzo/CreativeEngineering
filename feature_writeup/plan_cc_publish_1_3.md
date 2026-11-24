# Plan: Creative Common CurseForge update (1.3.0)

<!-- generated:status_index begin open_work sha1=2b3d05b26720 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 4 open TODO rows (Planned 2, Testing 2), 5 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `creative-common-publish #8` | Planned | S C | P7 tests: CC13 run block (solo, optional-mod, full pack sets) and new client rows |
| `creative-common-publish #9` | Planned |  | P8 release: tag creativecommon-1.3.0, upload Common first, then the dependents |
| `creative-common-publish #5` | Testing |  | P4 writeups: creative_engineering.md 7.6 Update to 1.3.0, 7.5 closed, touched writeups, project page |
| `creative-common-publish #6` | Testing |  | P5 changelogs: CurseForge file changelog for Common, pack bullets, changelog_lint |
<!-- generated:status_index end open_work -->

Source: owner request (chat 2026-11-21): update Creative Common on CurseForge so the Creative jars that require it can
be uploaded; bump the version, list the client rows, fix texture sources, writeups, project page, ISO and spelling
checks, mod sides, ship the whole change set since the last tag, then run the solo and optional-mod tests. Earlier gate
and history: [creative_engineering.md](creative_engineering.md) 7.1 to 7.5, TODO `creativecommon/publish #1` to `#5`.
Status lives in TODO rows (created in P0), not here.

## 1. Intake

| # | Item | Class | Note |
|---|---|---|---|
| I1 | Version number | open | `creativecommon-1.2.0` is tagged (2026-11-13) but 41 commits and about 220 files changed since; it is unknown whether 1.2.0 is on CurseForge |
| I2 | Cut-off | clear | owner: include the entire update set, nothing reverted |
| I3 | Dependent jars' Common range | open | 12 jars declare `[1.1.0,)` (Labels `*`, Barbeques `[1.0.0,)`); an old Common would pass the check and then lack the dict keys and helpers they read |
| I4 | Texture rows | clear | the 36 wood composter textures are covered by one glob row in `sources.csv` (status `confirm`, owner look-check pending); no missing row |
| I5 | ISO dates | clear | `iso_dates_docs`: 11 auto-fixable `(owner 2026-11-21` -> `(owner, 2026-11-21` (glass_shards.md, food_rules.csv); 9 unresolved on the owner list; 45 ambiguous left as is |
| I6 | Spelling | premise | `spelling_docs` passed on 2026-11-21 22:40 (earlier FAIL was one pending word, now gone); recheck at the end |
| I7 | Dedicated-server rows | clear | stay dropped from the gate (owner, 2026-11-08) |
| I8 | CurseForge changelog | clear | deferred 2026-11-13; written in P5 |

## 2. Owner rulings

| # | Item | Class | Ruling | Date | Source |
|---|---|---|---|---|---|
| R1 | Cut-off | clear | Include the entire update set since `creativecommon-1.2.0` | 2026-11-21 | owner chat |
| R2 | Version | decided | 1.3.0: new library API and gameplay content since 1.2.0, and 1.2.0 may already be published | 2026-11-21 | owner chat |
| R3 | Dependents' minimum | decided | Every dependent's Common range becomes `[1.3.0,)` in the same wave | 2026-11-21 | owner chat |
| R4 | Server rows | clear | Dedicated-server rows stay out of the gate | 2026-11-08 | owner |
| R5 | Glass shards (PR-CC-03, P1) | clear | Common ships the Forge loot modifier list entry; Creative Loot adds shards to mid-tier chests (mostly dirty or clear, a chance of colored) | 2026-11-22 | owner |
| R6 | Potions of 12 vanilla effects (PR-CC-11, P2) | clear | Stay in Common; documented | 2026-11-22 | owner |
| R7 | Wither Immunity (food-tuning #47, P7) | clear | Registered in Common as `creativecommon:wither_immunity` | 2026-11-22 | owner |

## 3. Phases

Order: P0 and P1 first (version and numbers are read by everything), then P2 to P5 in any order, P6 builds, P7 tests,
P8 releases. One code owner (Common); phases P2 to P5 are doc and data edits.

### P0 TODO rows
Add rows under `creativecommon/publish` with `tools/todo.py add --family` for P1 to P8 and set the plan field to this
file. Add the run block to a new `feature_writeup/testing/rows/CC13.qb` (prefix check against
[test_prefix_index.md](testing/test_prefix_index.md): CC13 is new, add its row first).

### P1 Version bump
- `development/creativeCommon/gradle.properties` `mod_version` 1.2.0 -> 1.3.0 (R2).
- Dependents (R3): `[[dependencies.*]] modId="creativecommon"` `versionRange` `[1.1.0,)` -> `[1.3.0,)` in Equipment,
  Engineering, Brewing, Delight, Loot, Compat, Devtools, Calendar, Worldgen, Magic; Labels `*` and Barbeques
  `[1.0.0,)` get the same range if they read Common's dicts (check; otherwise leave and say why). Delight members that
  repack upstream keep the upstream range.
- No dependent jar built before Common 1.3.0 is installed (rebuild order: Common first).
- `mod_sides.md` row (line 117) 1.2.0 -> 1.3.0; `pack/MODLIST.md` and `pack/manifest.json` follow at the next deploy
  (they still name 1.1.0).
- Tag `creativecommon-1.3.0` only at P8, on the clean commit that built the uploaded jar.

### P2 Texture sources
- Owner look-check of the 12 wood composters (side texture: compost gaps take the planks' darkest shades; rows
  `status=confirm`, plan_wood_bucket_batch_1121 P0). On approval set the rows to ok in `sources.csv`, then
  `python3 tools/texture_provenance.py --write`; on rejection the swap is rerun by the owner's tool with another palette
  (ask which).
- Check no other texture or model added since 1.2.0 lacks a row (the diff shows only the 36 composter files; models,
  blockstates and loot tables are data, not textures).
- Nothing is drawn or edited by hand.

### P3 ISO dates and spelling
- Run the fixer behind `iso_dates_docs` (11 changes), then the 9 unresolved on the owner list: list them for the
  owner, fix as ruled. 45 ambiguous stay.
- The 9 unresolved (listed 2026-11-22; agent classification: all number ranges or line-number spans, none a date):
  briefs/recipes_cooking.md:49 (line span), plan_1.0.3.md:526 (count range), plan_owner_notes_1119.md:25 (pixel
  range), plan_rules_fixes.md:657 (line span), plan_rules_fixes.md:658 twice (line spans), split/F_pack_scripts.md:298
  (line span), testing/structures/02_new_mods_tests.md:71 (number range), todo/rows/kitchen_and_cooking.csv:69 (line
  span). Owner decides; not accepted in check_known.csv.
- Rerun `spelling_docs`; fix or add to the allowlist as the lint says.
- Accepted leftovers go in `tools/check_known.csv` with reason and date. Exit: `check_all.py --fast --changed` shows no
  FAIL, so `installToMods` is not refused.

### P4 Writeups and project page
- [creative_engineering.md](creative_engineering.md): new 7.6 "Update to 1.3.0" (change table since 1.2.0 with the
  tested column filled from P7: melting and salt, effect level caps, config-folder watcher, wooden composters and
  related blocks, lang overrides, NCH helpers, dict API changes, loot tables), and 7.5 closed. Mention the
  retired `diet_log`, `liquid_diet_log`, `food_dictionary` dict keys (moved to Raspberry Pie).
- Feature writeups the new content touches: effects.md (level caps), the melting / salt writeup, composter writeup,
  config watcher (agent note if mechanics worth keeping).
- [project_pages/creative_common.md](project_pages/creative_common.md): features added since 1.2.0, the dependency
  list from `mods.toml`, the requirement note that Creative jars need Common 1.3.0 or later.
- Update TODO rows via `tools/todo.py`, `features.csv` (jars, queue codes), then `python3 tools/status_index.py`.

### P5 Changelogs
- CurseForge file changelog for Common (the first one; terse, facts only, no ids), kept in the project page folder.
- Pack player changelog: bullets for every player-visible item since 1.2.0 that is not already in
  `kubejs/assets/creativecommon/changelog/unreleased.md`; `python3 tools/changelog_lint.py`.

### P6 Build and install
- `./gradlew :creativeCommon:build --offline`, check `build/libs/creativecommon-1.3.0.jar`, `installToMods` (dialog if
  the game runs), then rebuild and install the dependents against it in order. `python3 tools/jarstamp.py pending`
  must list nothing for the upload set.
- Commit by path, push `agent_staging`.

### P7 Tests
All rows are queued in `CC13.qb`; a scratch server is booted only when the owner says so (CLAUDE.md).

Solo and optional-mod sets (server, queued):

| Set | Rows | What it shows |
|---|---|---|
| Common alone (`cc_only alone`, empty config) | SQ-CMV-02, SQ-CMV-04, SQ-CCO-01, -04, -15, -16 | boots, reloads and stops clean; vanilla defaults; no pack lists |
| Common with each optional mod present | SQ-CCO-02, -03, -05, -06, -07, -08, -09, -10, -11 | each optional mixin applies and works |
| Common with each optional mod absent | SQ-CCO-12a to -12f, -14 | six mod sets boot clean; absent mods' mixins skipped |
| Common + Compat + Calendar | SQ-CC12-02, SQ-CIF-04, -04b | no class missing across jars |
| Full pack | SQ-CC12-03, SQ-CMV-01, SQ-CPP-01 to -05, SQ-CC-01 to -16, SQ-CGE, SQ-CDNS | the pack's own load, swap lists, gravity, NCH recipes |
| New since 1.2.0 | SQ-MELT, SQ-SALT, SQ-LCA, composter rows | rerun on the 1.3.0 jar; existing rows, nothing new unless a gap shows |

Client rows (owner, last status from the queue):

| Row | Set | Last result | Shows |
|---|---|---|---|
| CQ-CMV-02 | full pack | PASS r1108 | Mods screen description, links, no dependency error |
| CQ-CMV-03 | full pack | open | panorama, creative menu switch, no toasts |
| CQ-CC-01 | full pack | partial | launch, loading tips, Mixin apply grep |
| CQ-CC-02 | full pack | partial | new world loading screen tip bar |
| CQ-CC-03 | full pack | not run | tip wrap at GUI scale 4, `enabled = false`, panorama |
| CQ-CC-04 | full pack | not run | `[mod_names] overrides` rename in JEI, WTHIT, Mods screen |
| CQ-CC-05 | full pack | not run | portal mirror glass on tinted glass variants |
| CQ-CC-06 | full pack | not run | changelog viewer from title and pause menus |
| CQ-CCB-02 | full pack | not run | JEI: fluid list, pack ingredient order, no handler errors |
| CQ-CCB-03 | Common + Compat, no JEI | FAIL r1107 (Tempad jar) | no class error without JEI; needs the Tempad jar |
| CQ-CCO-02, -03 | Common alone | partial | End low gravity, survival and falling blocks |
| CQ-CPP-01 | full pack | not run | drink fluid hover, Mods screen descriptions |
| CQ-CIF-01 | full pack | never | JEI World tab, craftable-first, WTHIT state lines |
| CQ-SPFX-02 | no Immersive Portals | partial | the no-portals launch half |
| CQ-CGE rows | Common alone / full | 5 PASS, 5 not run | gravity for falling blocks, items, TNT (client half only) |
| CQ-CLOG-02 | full pack | not run | no log noise for absent mods |
| CQ-CHK-01 | full pack | never in queue state, owner PASS 2026-11-13 | chat key; confirm the queue row is closed |
| new rows (CC13) | full pack | to write | config watcher (edit a Common config file in game and see the reload), melt and salt tooltip, level-capped effect tooltip, composter look in world |

Dropped: dedicated-server rows (R4).

Any FAIL or BATCH-FAULT: fix in the run if it is env or tooling, otherwise a TODO row and back to P6 for that jar.

### P8 Release
- Clean commit; tag `creativecommon-1.3.0`; push `agent_staging`; the owner merges.
- Upload Common first (CurseForge page text from P4, changelog from P5), then the dependents (own plans,
  [plan_cb_publish.md](plan_cb_publish.md) for Brewing). Dependents go up only after Common 1.3.0 is public.
- `pack/MODLIST.md` and `pack/manifest.json` are the next deploy's job ([release skill](../.claude/skills/release/SKILL.md)).

## 4. Hand-over format
Exact row ids to run per set, jars still waiting for `mods/`, whether a changelog bullet was added, and the list of
unresolved ISO dates.
