# CreativeEngineering2 — working notes

Forge 1.19.2 modpack (KubeJS + in-tree Java mods). Repo root is the ATLauncher instance.

## Layout

- `kubejs/startup_scripts/` (items, rules), `server_scripts/` (recipes, `_lib/` dump
  commands), `client_scripts/`. `generation_scripts/` = generated data, `exported/` = game dumps
  (large; grep, never read whole). `data/` = datapack files.
- `development/` = in-tree mods: `creativeCommon` (the shared core, mod id `creativecommon`),
  `creativeEngineering` (machines and engineering, mod id `creativeengineering`), `creativeEquipment`, `creativeMagic`,
  `creativeLabels`, `exposureBackport`, the carved Creative jars (`creativeCompat`, `creativeCalendar`, `creativeDevtools`), and three bundles: `creativeWorldgen` (worldgen and fauna; members More Mooblooms and Baby Mobs),
  `creativeDelight` (food; its ports and repacks, Veggies, Vintage, Fungi, Miner's and Display included, are members in `members/`) and `creativeBrewing` (brewing, alcohol, potions; nests the Vinery and Brewin' ports,
  `members/vinery`, `members/brewin`). Root `settings.gradle` + `build-logic/` build them all.
- `tools/*.py` = generators, listed in `tools/generators.json` and run in game with `/cdevgen`.
  `/dumpall` refreshes the dumps they read. `build_deploy.py` stages releases into `deploy/`.
- `feature_writeup/agent_notes/` = subsystem mechanics, gotchas and crash causes. New notes go there with one line
  in its index, not into memory; memory keeps only rules about working with the owner, user facts and external
  references. Index: @feature_writeup/agent_notes/README.md
- `mod_sides.md` = client/server side of every jar (add a row for each new mod).
  `configs_edited/` = backup of every hand-edited config. `feature_writeup/` = feature docs.

## Glossary and processes

Shorthand the user and docs use (spell out in player-facing text and changelog bullets):
CE Creative Engineering (pack, or the Engineering jar) | Common Creative Common (core jar) | CD Creative Delight | FD Farmer's Delight | IE Immersive Engineering |
IW Immersive Weathering | CR Create | PC PneumaticCraft | AM Alex's Mobs | D&D Create: Dreams & Desires |
S&D Create Slice & Dice | DH Distant Horizons | LC Lightman's Currency | RD Rustic Delight | SoL Spice of Life (Apple Pie) |
AE2 Applied Energistics 2 | JEI Just Enough Items | JEED Just Enough Effect Descriptions | WTHIT What The Hell Is That |
FTB Feed The Beast mods (Quests, Teams, Library) | NCH neoCraftHelper (`global.NCH`)

Processes are project skills in `.claude/skills/`: `integration-report` (new or updated mod: overlap, new content,
fold-in), `add-mod`, `release`, `port` (ports and backports), `scratch-test` (queue rows, runs, run status). When the
user asks for one, follow its skill. `intake`: whenever owner notes (scratchpad blocks, owner-notes sections, run
findings) become a plan, split and classify them and ask the open items before building.

## Preferences

- **Recipes and tags go through helpers, not hand-written events.** Recipes: `global.NCH`
  ([neoCraftHelper.js](development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/func/neoCraftHelper.js)); tags: `global.tags`
  ([tag_helper.js](development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/func/tag_helper.js)). No `e.shaped(...)` / `e.custom(...)` /
  `event.add(...)` in server scripts when a helper covers it; extend the helper instead.
- **Define each item once.** Ids live in `global.item` / `item_dict.js` (and block/fluid dicts, all in Creative Common's
  `kubejs_startup/creativecommon/_lib/`);
  scripts use those keys, not literal `namespace:id` strings.
- No emoji in any file. Own work is GPL-3.0-only (SPDX). Never publish claude.ai artifacts.
- Dates are ISO 8601 everywhere (`YYYY-MM-DD`, times `YYYY-MM-DD HH:MM`), never year-less `11-14` or `Nov 14`; American spelling and `->` arrows in prose (design_rules.md DR-L4b, DR-L7).
- **Textures: human art or the owner's tools only.** Never draw, generate or pixel-edit a texture, pick its palette, or
  transform it with an ad-hoc script. Allowed: copy human art (upstream, vanilla, owner's), or run
  `textureswapper/palette_swap.py` / `combine_tex.py` / `scale_tex.py` / `upscale_sprites.py` on a source and palette
  the owner picked (ask when not given). Every derived texture gets a row in
  `feature_writeup/texture_sources/sources.csv` (source, tool, use) in the same session; rerun
  `python3 tools/texture_provenance.py --write`. Rule and audit: [texture_sources.md](feature_writeup/texture_sources.md).
- **Tables are CSV** (header row, UTF-8, Python `csv` or `tools/table_io.py`); no new TSV files.
- Hand-edited config: also copy it to `configs_edited/root_config/<same path>` (or `server_config/`).
- Backports and ports: no old-version or cross-mod conversions. Never use advancements (the pack
  drops them all at load) for rewards or unlocks.
- Checking whether the game is running: process command lines are allowed, but the game's args contain the access
  token, so redact it in the same command and never print it raw. Use
  `ps -eo pid,etime,args | grep "[j]ava" | sed -E 's/(--accessToken|--uuid|--username)[= ][^ ]+/\1 ***/g'`
  (or `pgrep -x java` for pids only, then `readlink /proc/PID/cwd`). Never run bare `pgrep -af` / `ps -f` / `ps aux`.
- **Agent-to-agent messages** (subagents, forks, SendMessage to other sessions):
  - First contact: open by stating you are an agent (name/role, which session or task spawned you), then your
    overall goal for the whole task, then the specific request. Later messages in the same exchange skip the
    identification and restate the goal only if it changed.
  - No pleasantries (no "thank you", "please", greetings, sign-offs). Plain statements: goal, context, request, expected return format.
  - Delegate using what an agent can know: split by independence, file/subsystem ownership, and what each side
    already has in context or tools. Give pointers (paths, doc names, what is already ruled out), not retyped content.
    Do not hand off a lookup you can finish in a few tool calls.
  - Delegation is provisional. If the other agent reports information that contradicts or supersedes the plan
    (the work is already done, a premise is false, a better owner exists, scope differs), adopt it: say so in
    one line, re-split the work, and do not repeat the original request. Say the same when you hold such
    information and the other agent's brief is wrong.
- **Keep context lean.** Write Bash commands to return only what the task needs (`head`/`tail`/`grep`,
  tail of build logs); grep or offset/limit big docs instead of reading them whole. When context nears
  ~150k, or a task ends / the next step is a different subsystem / debugging has become a long loop,
  add one closing line: "Good point for a new session; status is in <doc>." Say it once, keep working
  if the user continues, and have the handover docs already updated.

## Build and verify

- Build/install a mod: `./gradlew :<mod>:build --offline`, then `:<mod>:installToMods` (moves old
  jars to `jar_archive/displaced/`). A rebuild for testing is installed without asking. When the game is running,
  `installToMods` shows the owner a dialog (Install now, shown again while the game still runs / Install on exit /
  Skip); never replace a jar under a running game any other way. Every install is a row in `jar_archive/installed.csv`;
  `python3 tools/jarstamp.py pending` lists built, not installed jars (cite it instead of writing "not installed").
- **Checks.** `python3 tools/check_all.py` runs every headless test and lint (`--fast` under a minute, `--changed`
  only what the diff touches, `-v` output tails). `installToMods` runs `--fast --changed` first and refuses on a FAIL
  (`-PskipChecks` overrides, logged); `build_deploy.py` runs `--full`; the Stop hook reports. Accepted failures go in
  `tools/check_known.csv` with a reason and date.
- **Session role.** At the start of a session, and whenever the work changes, run
  `python3 tools/session_role.py plan|impl|test`. The Stop hook follows it: `plan` silent; `impl` checks and counts
  only this session's own uncommitted code files (never docs or other sessions' files); `test` counts this session's
  own uncommitted run docs and queue rows. Undeclared = `impl` with a hint.
- **Commits.** Agents commit at the end of each plan phase, and at session end for any finished work left
  uncommitted, as the repo's configured author (the owner) with the Co-Authored-By trailer, then push with
  `git push origin HEAD:agent_staging` (never `main`; the owner merges). Stage only that phase's files by path
  (`.gitignore` is an allow-list; never `git add -A` / `.`), never the owner's unrelated uncommitted work; generated
  output in its own commit. If the push is not a fast-forward, stop and ask.
- Logic tests without the game: plain `javac`/`java` harness (creativeCommon `build/classes`, Mojmap, an
  `FMLEnvironment` stub first on the classpath). Live checks: throwaway dedicated server in the
  scratchpad. Details: memory `patchermod-headless-smoke-test`, skill `scratch-test`.
- **Scratch server tests default to the queue.** Add every live check a change needs to its rows file
  `feature_writeup/testing/rows/<CODE>.qb` in the same session (rendered into
  [scratch_queue.md](feature_writeup/testing/scratch_queue.md) by `status_index.py`); only boot a scratch server when the
  user says to. What a run needs: `qrun.py plan`. Writing rows and running them: skill `scratch-test`.
- Script-only change: `/reload` is enough for server scripts; startup scripts, items and any
  Java change need a full restart.
- The shared helpers (`neoCraftHelper.js`, `tag_helper.js`, `compost.js`, `trade_helper.js`) and the dicts
  (`_lib/item_dict.js` and siblings) live in Creative Common's jar (`src/main/resources/kubejs_startup/creativecommon/`,
  loaded before `startup_scripts/`); its server and client helpers are in `kubejs/data/creativecommon/kubejs/_lib/` and
  `kubejs/assets/creativecommon/kubejs/_lib/`, copied into the jar. An edit to any of them (including generator output
  into a dict) needs a `:creativeCommon:` rebuild, install and restart. Creative Delight (`development/creativeDelight`) and Creative Brewing (`development/creativeBrewing`) nest
  their members; build/install via `:creativeDelight:` / `:creativeBrewing:`, never a member's own task.
- Add a mod, release, port, scratch tests: skills `add-mod`, `release`, `port`, `scratch-test`.

## Debugging

When the user reports an in-game symptom, grep `logs/latest.log` first (`Error occurred while
handling event`, the mod/feature name) before reasoning from code. It is the real log of their play.

## Done means

- The feature's writeup in `feature_writeup/` and its test list are updated in the same session.
- **Player changelog for every significant change.** Any change a player can notice (gameplay, recipes,
  values, items, UI, fixes to player-visible bugs) gets a bullet in the same session in the working list
  `kubejs/assets/creativecommon/changelog/unreleased.md`, under a family named after the feature (`## Lunchboxes`); lone changes go under a broad catch-all heading and move into a feature family once a second change touches the same thing.
  Follow [changelog.md](feature_writeup/changelog.md) section 3.6: terse, facts only, no flavor or benefit
  clauses, old value in brackets, fixes in past tense, no ids/file names/config keys/emoji. Rewrite or merge
  existing bullets when later work changes them. Never edit a deployed version's file. Same-behaviour ports and
  dev-only changes get no bullet. Run `python3 tools/changelog_lint.py` after editing. State in the hand-over
  whether a bullet was added (or why not).
- Status lives in the repo docs, not memory or agent notes: [feature_writeup/README.md](feature_writeup/README.md)
  (index + status legend), [todo/TODO.md](feature_writeup/todo/TODO.md) (open work),
  `plan_*.md` and [deploy.md](feature_writeup/deploy.md) (releases). **A TODO row is the only place a task's status is
  written**: rows live in `feature_writeup/todo/rows/<category>.csv` and change only through `tools/todo.py`
  (`add --family`, `set ID status=...`, `log ID "built ..."`, `move`); TODO.md is generated from them and
  `todo/layout.md`. Plans hold design, order and rulings and cite row ids (`plan` field); writeups describe the feature
  (their Open work block is generated); install state is generated, never typed. A change also updates its row in
  `feature_writeup/status/features.csv` when jars or queue codes change (the status word is derived, `override` only
  with a dated reason), then runs `python3 tools/status_index.py`. TODO.md, the README index status, the feature
  status words, the Open work blocks and the queue coverage block are generated: never hand-edit them or inside
  `generated:status_index` markers, and never paste run results into README cells.
- New tests and queue rows take their prefix from [test_prefix_index.md](feature_writeup/testing/test_prefix_index.md)
  (a new feature adds its row first).
- Hand over with the exact test IDs to run in game, and any jar still waiting to be copied to `mods/`.
