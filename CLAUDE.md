# CreativeEngineering2 — working notes

Forge 1.19.2 modpack (KubeJS + in-tree Java mods). Repo root is the ATLauncher instance.

## Layout

- `kubejs/startup_scripts/` (items, `func/` helpers, dicts), `server_scripts/` (recipes, `_lib/` dump
  commands), `client_scripts/`. `generation_scripts/` = generated data, `exported/` = game dumps
  (large; grep, never read whole). `data/` = datapack files.
- `development/` = in-tree mods: `patcherMod` (main mixin/coremod, mod id `creativeengineering`),
  `creativeLabels`, `incubationAdditions`, `moreMooblooms`, `babyMobs`, `exposureBackport`, and two bundles:
  `creativeDelight` (food; its ports and repacks, Veggies, Vintage, Fungi, Miner's and Display included, are members in `members/`) and `creativeBrewing` (brewing, alcohol, potions; nests the Vinery and Brewin' ports,
  `members/vinery`, `members/brewin`). Root `settings.gradle` + `build-logic/` build them all.
- `tools/*.py` = generators, listed in `tools/generators.json` and run in game with `/cegen`.
  `/dumpall` refreshes the dumps they read. `build_deploy.py` stages releases into `deploy/`.
- `mod_sides.md` = client/server side of every jar (add a row for each new mod).
  `configs_edited/` = backup of every hand-edited config. `feature_writeup/` = feature docs.

## Glossary and processes

Shorthand the user and docs use (spell out in player-facing text and changelog bullets):
CE Creative Engineering (pack, patcherMod) | CD Creative Delight | FD Farmer's Delight | IE Immersive Engineering |
IW Immersive Weathering | CR Create | PC PneumaticCraft | AM Alex's Mobs | D&D Create: Dreams & Desires |
S&D Create Slice & Dice | DH Distant Horizons | LC Lightman's Currency | RD Rustic Delight | SoL Spice of Life (Apple Pie) |
AE2 Applied Energistics 2 | JEI Just Enough Items | JEED Just Enough Effect Descriptions | WTHIT What The Hell Is That |
FTB Feed The Beast mods (Quests, Teams, Library) | NCH neoCraftHelper (`global.NCH`)

Processes are project skills in `.claude/skills/`: `integration-report` (new or updated mod: overlap, new content,
fold-in), `add-mod`, `release`, `port` (ports and backports), `scratch-test` (queue rows, runs, run status). When the
user asks for one, follow its skill.

## Preferences

- **Recipes and tags go through helpers, not hand-written events.** Recipes: `global.NCH`
  ([neoCraftHelper.js](development/patcherMod/src/main/resources/kubejs_startup/creativeengineering/func/neoCraftHelper.js)); tags: `global.tags`
  ([tag_helper.js](development/patcherMod/src/main/resources/kubejs_startup/creativeengineering/func/tag_helper.js)). No `e.shaped(...)` / `e.custom(...)` /
  `event.add(...)` in server scripts when a helper covers it; extend the helper instead.
- **Define each item once.** Ids live in `global.item` / `item_dict.js` (and block/fluid dicts);
  scripts use those keys, not literal `namespace:id` strings.
- No emoji in any file. Own work is GPLv3. Never publish claude.ai artifacts.
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
  jars to `jar_archive/displaced/`). Never replace a jar while the game is running; if unsure, ask.
- Logic tests without the game: plain `javac`/`java` harness (patcherMod `build/classes`, Mojmap, an
  `FMLEnvironment` stub first on the classpath). Live checks: throwaway dedicated server in the
  scratchpad. Details: memory `patchermod-headless-smoke-test`, skill `scratch-test`.
- **Scratch server tests default to the queue.** Add every live check a change needs to
  [scratch_queue.md](feature_writeup/testing/scratch_queue.md) in the same session; only boot a scratch server when the
  user says to. Writing rows and running them: skill `scratch-test`.
- Script-only change: `/reload` is enough for server scripts; startup scripts, items and any
  patcherMod change need a full restart.
- The shared helpers (`neoCraftHelper.js`, `tag_helper.js`, `compost.js`) live in patcherMod's jar
  (`src/main/resources/kubejs_startup/`, loaded before `startup_scripts/`): an edit needs a patcherMod rebuild, install
  and restart. Creative Delight (`development/creativeDelight`) and Creative Brewing (`development/creativeBrewing`) nest
  their members; build/install via `:creativeDelight:` / `:creativeBrewing:`, never a member's own task.
- Add a mod, release, port, scratch tests: skills `add-mod`, `release`, `port`, `scratch-test`.

## Debugging

When the user reports an in-game symptom, grep `logs/latest.log` first (`Error occurred while
handling event`, the mod/feature name) before reasoning from code. It is the real log of their play.

## Done means

- The feature's writeup in `feature_writeup/` and its test list are updated in the same session.
- **Player changelog for every significant change.** Any change a player can notice (gameplay, recipes,
  values, items, UI, fixes to player-visible bugs) gets a bullet in the same session in the working list
  `kubejs/assets/creativeengineering/changelog/unreleased.md`, under a family named after the feature (`## Lunchboxes`); lone changes go under a broad catch-all heading and move into a feature family once a second change touches the same thing.
  Follow [changelog.md](feature_writeup/changelog.md) section 3.6: terse, facts only, no flavor or benefit
  clauses, old value in brackets, fixes in past tense, no ids/file names/config keys/emoji. Rewrite or merge
  existing bullets when later work changes them. Never edit a deployed version's file. Same-behaviour ports and
  dev-only changes get no bullet. Run `python3 tools/changelog_lint.py` after editing. State in the hand-over
  whether a bullet was added (or why not).
- Status lives in the repo docs, not memory: [feature_writeup/README.md](feature_writeup/README.md)
  (index + status legend), [todo/TODO.md](feature_writeup/todo/TODO.md) (open work),
  `plan_*.md` and [deploy.md](feature_writeup/deploy.md) (releases).
- Hand over with the exact test IDs to run in game, and any jar still waiting to be copied to `mods/`.
