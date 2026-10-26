# creativedelight / framework briefs

Written 2026-10-25 from [creative_delight.md](../creative_delight.md). Order: 1, 2, 3 (finish and install what is built), then
4 to 8 (what every member and QoL move needs), 9 whenever. Row 1 is a queue entry only; nothing is booted until the owner says so.

## framework #1: Run the framework scratch tests
Status Testing / scratch / `creativedelight/framework #1`
### Goal
Know that the built framework works in a game JVM before anything moves into it.
### Now
Queue entries SQ-KS, SQ-CDJ-01 to -04 and -06, SQ-CDT-01 to -10 in [scratch_queue.md](../testing/scratch_queue.md). The `-cdtest.jar` build and
`/cdtest` commands: `tools/creative_delight_test/README`.
### Build
Nothing to build. When the owner says run: one scratch boot for SQ-CDT-01 to -07 and SQ-CDJ-01 to -04 and -06, then the stop-and-restart rows (-08 to -10).
### Tests
CDT-01 to CDT-07 (writeup 19.2). Record results in the queue's run table and tick the writeup.

## framework #2: Install patcherMod and Creative Delight together
Status Planned / mods/
### Goal
The live pack runs the loader, the helpers in patcherMod's jar and the three repacks.
### Now
`development/patcherMod/build/libs/` holds the helpers the pack folder no longer has; `creative_delight-1.0.0.jar` nests Ocean's, Oceanic and End's.
### Build
1. Confirm the game is not running (CLAUDE.md redacted `ps` form). 2. `./gradlew :patcherMod:installToMods :creativeDelight:installToMods --offline`
(CD's moves `oceansdelight-*`, `oceanic_delight-*`, `ends_delight-*` to `jar_archive/displaced/`). 3. Delete the three member rows from `mod_sides.md` (B9).
4. Hand over: the jars in `mods/` and what to check on first boot (`latest.log` for `[jar-startup]`).
### Gotchas
Both jars or neither. A stale upstream jar beside CD is caught by the duplicate-id check in `build_deploy.py`, not by the game.
### Tests
CDT-01, CDT-07 in the real pack; the pack's own recipe count unchanged (compare `/dumpall` before and after).

## framework #3: Check the three MIT notices
Status Planned / docs
### Goal
Correct copyright lines in the repack notices.
### Build
Ocean's: GitHub repo. End's and Oceanic: CurseForge pages. Fix `NOTICE-upstream-MIT.txt` in `members/{oceans,ends,oceanic}`; rebuild; no code change.
### Decisions
If an author cannot be confirmed, keep the `mods.toml` author and say so in the notice.

## framework #4: Build the food dict
Status In Progress / KubeJS / tool (food half built 2026-10-25)
### Goal
`global.food` (`f`), `global.tool` (`t`), `global.misc` (`m`): masks over `global.item`, keys `<category>[_<form>][_<state>]_<name>`.
### Now
Food dict built: `_lib/food_grammar.js` (lists and `global.subdict.parse/check`), `_lib/food_dict.js` (1542 keys), `tools/food_dict.py`
(lint, coverage, reverse index, `/cegen run food_dict`), `tools/test_food_dict.sh`. As built, final lists and decisions: writeup 13.5.
### Build (left)
1. `tool_dict.js` and `misc_dict.js` with the same check (`global.subdict.check(t, 'tool')`); key rules first (writeup 13.3 examples).
2. Pilot Veggies (about 89 items): rewrite its cooking calls to `f.*` (CD scripts alias `f`; pack-folder scripts `fd`), diff recipes with framework #6.
### Gotchas
`f` is the fluid alias in the pack's startup and server folders; `food_dict.js` keeps its `f` inside a function. The dict file is kept
by hand: never re-run `--seed`; new foods come from `tools/food_dict.py` proposals (`--append`). A food with no general key goes in `item_dict.js` first.
### Tests
CDT-08 (PASSED offline, live SQ-FD-01 queued), CDT-09 pilot recipes identical (open), CDT-10 coverage (PASSED: 0 missing).

## framework #5: CD config keys and the `global.CD` API
Status Planned / new jar
### Goal
One config file with switches per QoL feature and optional integration, read the same way everywhere.
### Now
`CDConfig` has `[members]` only; `kubejs_startup/creative_delight/cd_core.js` exposes `global.CD.on(id)`.
### Build
Add `[qol]` and `[integrations]` sections in `CDConfig`; `global.CD.qol(id)` and `global.CD.integration(id)`. Add each key in the session that builds its first user (QoL rows), and mirror the generated TOML to `configs_edited/root_config/config/creative_delight-common.toml` if hand-edited.
### Gotchas
COMMON config loads after startup scripts: read inside callbacks only. Server scripts re-read on `/reload`, startup scripts need a restart.
### Tests
Extend `/cdtest config`: every key present with its default; a key set false skips its script.

## framework #6: Move-diff harness
Status Planned / tool
### Goal
Prove a move lost nothing.
### Build
`development/creativeDelight/tools/move_diff.py`: snapshot = registry names (jar listing), recipe ids and counts per type (`exported/`), tag contents, loot table ids. `snap before` / `snap after`, `diff` fails on any removed id and on any recipe whose type, result or ingredients changed; recipe id namespace change `creativeengineering:` to `creative_delight:` is expected (Q24) and mapped.
### Gotchas
Dumps are large: parse, never print. Needs one `/dumpall` in game per snapshot; the "before" snapshot must be taken before the first move of a member.
### Tests
CDT-11: harness on two identical dumps passes; on a dump with one recipe removed fails.

## framework #7: Generators write into the CD tree
Status In Progress / tool. Mechanism built 2026-10-25 as `outRoot` + `tools/gen_out.py` (creative_delight.md 28.2); no generator moved (JSON read by instance path, decision open).
### Build
Per generator in `tools/generators.json` an `out_root` (default unchanged); member-row generators (cans, food types, rules) set it to `kubejs/data/creative_delight/kubejs/`. The jar build already fails on an empty tree, so add a generated-file header the build allows.
### Gotchas
Generated files in the jar's tree are `dev` class in `build_deploy.py`; a generator run after a jar build is not in that jar.

## framework #8: Compartment pattern for Java
Status Planned / new jar
### Goal
CD classes that touch another mod load only when that mod is present.
### Build
CD mixin plugin `shouldApplyMixin` keyed by package (`...compat.farmersdelight`, `...compat.cooking_for_blockheads`, `...compat.wthit`, `...compat.jei`) with a `ModList`/`LoadingModList` check; compartment event handlers registered from a loaded-check, never a static reference on the main path. Pattern: patcherMod `FruitsDelightLeafCheckMixin`.
### Tests
CDT-12: boot with each optional mod removed in turn (scratch queue): CD loads, no `ClassNotFoundException`, compartment's feature inactive.

## framework #9: Donor jars downloaded by the build
Status Pinned / tool. Writeup 15. Keep `UpstreamJars` sha1 pin; download verifies the same sha1 and refuses otherwise. Nothing to do until the owner lifts the pin.
