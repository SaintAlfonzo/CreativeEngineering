# Client capture (queued visual tests run by a throwaway client)

## 0. At a glance

<!-- generated:status_index begin open_work sha1=1796d5070e78 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 2 open TODO rows (In Progress 1, Testing 1), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `tooling/generators-and-tests #6` | In Progress | C | Client capture for Round 2 visual tests |
| `generators-and-tests #18` | Testing | CQ-TXP-01, CQ-TXP-02, CQ-TXC-11, TXQ-OFF-01 to -05 | Paired client + own server runs and mismatch matrix finished; experimental Xvfb display (scratch testing expansion item 5, TX-31, TX-07) |
<!-- generated:status_index end open_work -->

| Part | Status |
|---|---|
| Design (this file) | DESIGNED 2026-10-24; items 2 and 4 of [plan_scratch_testing_expansion.md](plan_scratch_testing_expansion.md) built 2026-11-15 |
| Driver mod `development/creativeDevtools/members/capture` (mod id `cdevcapture`, dev-only, nested in Creative Devtools since 2026-11-01; inert without `-Dcdevcapture.plan`) | BUILT: capture steps (2.2) since 2026-11-01; readback (TX-03), tick-exact movement (TX-04), widget actions (TX-05) and the file channel (TX-02, 2.11) 2026-11-15 in `development/creativeDevtools/build/libs/creativedevtools-1.0.0.jar`; not run in game |
| Runner `tools/client_capture/run_captures.py` (plan, launch through `eval_client.py`, relaunch on crash, `serve` / `stop`) | BUILT 2026-11-15, offline-tested (plan, `--list --check`); never launched |
| Cropper `tools/client_capture/crop.py` + `crop_regions.json` | BUILT 2026-11-15, `--selftest` and a fake run pass |
| Channel client `tools/client_capture/chan.py`; `qrun.py client` driver transport | BUILT 2026-11-15, tested against a fake driver |
| Spec files `feature_writeup/testing/capture_specs/*.json` | first set 2026-11-15: `CC` (CC-05, CC-13, CC-15), `CQ-NEW` (FX-43, N-02, CQ-NEW-04a), `CQ-PTH` (CQ-PTH-03b), `_macros.json` |
| Output `test_captures/judgement/<run>/` (git-ignored, explicit `.gitignore` line) | none yet |

In-game checks are queue rows CQ-TXC-01 to -10 ([testing/scratch_queue.md](testing/scratch_queue.md), end). Open
decisions are in section 4.1. TODO: `tooling/generators-and-tests #6`. Test prefix `CC-`.

The point: Round 2 of [testing/scratch_queue.md](testing/scratch_queue.md) (the `CQ-` rows) is mostly "open this,
look at that": a JEI page, a tooltip, a WTHIT line, a block's color, a GUI. Today each needs the user at the keyboard.
With this system a session (or the user) picks the capturable rows, one throwaway client runs them all unattended in a
single-player world, and each check leaves one cropped PNG named after its test ID, next to a manifest that says what
the picture should show.

**Judgement rule (owner, 2026-11-15).** The pipeline captures and crops everything, but neither the tool nor any agent
gives a visual verdict. Every run is written to `test_captures/judgement/<run>/` (git-ignored, wiped by the owner
after judging): crops, raw frames, manifest entries (test ID, `expect`, crop, step log, reads) and `index.md` with an
empty Verdict column for the owner. Text readback (TX-03 read steps with `expect` / `reject`) may still give PASS /
FAIL by regex, in the manifest (`judge: text`) and in qrun. The owner's verdicts go back into the queue and writeups
as usual. This replaces the earlier "a session reads the PNGs" evaluation.

## 1. Overview

```
spec JSON (per queue group) ──> run_captures.py ──> scratch game dir ──> client JVM + cdevcapture driver
                                    │                                      │ loads template world
                                    │                                      │ per test: reset, steps, capture
                                    │                                      ▼
                                    │                     raw/<ID>.png + rects + progress.json + log markers
                                    ▼
                               crop.py ──> test_captures/judgement/<run>/<ID>.png + manifest.json + index.md
                                    ▼
                         owner judges the PNGs (verdict column); text reads judged by regex ──> queue, writeups
```

Three pieces, split by what each one knows:

- **Driver** (in game, Java): knows game state. Runs steps, waits for the game to settle, grabs the framebuffer, and
  records the rectangles that matter (the GUI's bounds, the tooltip's bounds) in GUI and pixel coordinates. It does
  not crop or name final files.
- **Runner** (host, Python): knows the machine. Checks the one-JVM rule, builds the game dir, launches, watches,
  restarts after a crash, then calls the cropper.
- **Cropper** (host, Python + PIL, which is installed): knows pixels. Crops each raw frame with the driver's rects plus
  the spec's crop rule, trims flat backgrounds, downsizes, renames, and writes the manifest. Because raw frames are
  kept, a bad crop is fixed by editing the spec and rerunning `crop.py` alone, without launching the game.

## 2. Reference

### 2.1 Spec file

One JSON file per queue group in `feature_writeup/testing/capture_specs/` (`CQ-NEW.json`, `CQ-VP.json`, ...), next
to the queue that names them. Specs are test data, so they live with the tests. Shared step lists live in
`capture_specs/_macros.json`.

```json
{
  "group": "CQ-NEW",
  "world": "flat_swamp",
  "biome": "minecraft:swamp",
  "client_config": {},
  "tests": [
    {
      "id": "FX-43",
      "covers": ["CQ-NEW-02"],
      "title": "Moss follows biome grass colour",
      "expect": "Moss block and moss carpet on the left match the grass block beside them (swamp: dark olive). The item in the hotbar slot stays vanilla bright green.",
      "steps": [
        {"cmd": "setblock $X+2 $Y $Z+4 minecraft:moss_block"},
        {"cmd": "setblock $X+3 $Y $Z+4 minecraft:grass_block"},
        {"cmd": "setblock $X+4 $Y $Z+4 minecraft:moss_carpet"},
        {"cmd": "give @s minecraft:moss_block"},
        {"look": {"from": "$X+3 $Y+2 $Z", "at": "$X+3 $Y $Z+4"}},
        {"wait": "chunks"}
      ],
      "capture": [
        {"name": "",        "crop": "center:200x120", "hide": ["chat", "hand", "crosshair"]},
        {"name": "hotbar",  "crop": "hotbar"}
      ]
    },
    {
      "id": "FO-49",
      "covers": ["CQ-NEW-03"],
      "expect": "Popcorn tooltip: the AppleSkin hunger bar is drawn under the text lines.",
      "steps": [{"tooltip": "item_dict:popcorn"}],
      "capture": [{"crop": "auto"}]
    }
  ]
}
```

- `id` is the writeup test ID (`FX-43`), so the file name is the test name a writeup already uses. `covers` lists
  the queue row(s) it serves. A test with two captures gets `FX-43.png` and `FX-43_hotbar.png`.
- `expect` is required: the evaluator compares against it, not against the steps. Write it the way a writeup test's
  expected result reads.
- `$X $Y $Z` are the test's plot origin (2.4). `item_dict:<key>` resolves through the exported item dict (the
  define-once rule), so specs don't carry literal ids where a key exists; a literal id is allowed for vanilla and
  for items the dict lacks.
- `world` names the world (2.5); `biome` (default `minecraft:plains`), `seed` and `origin_y` (default -60, the
  superflat standing height) set how the driver creates it. 1.19.2 has no `/fillbiome`, so a biome is per group: a
  test that needs another biome goes in another group (the world name must differ too, or the old world is reused
  within a game dir; the runner deletes `saves/<world>` before each launch). `client_config` is a per-group overlay,
  `{"config/<file>.toml": {"<section>": {"<key>": value}}}`, written before launch (2.6). Groups with different
  worlds or client configs are run separately.
- Read steps (2.2) may carry `"expect"` / `"reject"` (Java regex): a capture-free test whose reads all carry them is
  judged PASS / FAIL by text; any test with a `capture` entry goes to the owner.

### 2.2 Steps

Kept small. Anything bigger is a command, or a server-side probe the pack already has (`/vtdtest`, `/fdtest`), run
through `cmd`.

| Step | Does |
|---|---|
| `cmd` | Runs a command on the integrated server as the player (`performPrefixedCommand` with the player's source, op level 4); logs the result code; a 0 result fails the test unless `"allow_fail": true` |
| `look` | `{"from": pos, "at": pos}` or `{"from": pos, "yaw": a, "pitch": b}`: teleports and sets rotation exactly |
| `wait` | Ticks (`20`), or `chunks` (every section in render distance compiled), `screen:<class>`, `jei` (JEI runtime ready), `resources` (no reload in progress). Always followed by 3 settle frames |
| `slot` | Selects a hotbar slot |
| `use` | Right-clicks the targeted block or the held item through `gameMode.useItemOn` / `useItem` (opens real GUIs: pickling jar, kettle, oven) |
| `attack` | Left-click on the target (one swing) |
| `screen` | `inventory`, `creative:<tab>`, `jei_recipes:<item>`, `jei_uses:<item>`, `jei_category:<uid>`, `close` |
| `tooltip` | Opens the driver's stage screen: flat `#202020` background, the item at the center, its tooltip drawn by the normal path (so Legendary Tooltips, AppleSkin, KubeJS and our tooltip lines all run). Options: `shift`, `ctrl`, `advanced` (F3+H) |
| `hover` | Puts the virtual mouse at GUI coords or on slot N of the open screen (2.7), for tooltips inside a real GUI |
| `key` | Holds a modifier (`shift`, `ctrl`, `alt`) for the next capture |
| `reload` | F3+T resource reload, then waits `resources` |
| `macro` | Inlines a step list from `_macros.json` |
| `look` without `from` | `{"yaw": a, "pitch": b}` turns the player where it stands (no teleport, no flight) |

`cmd` logs `[cdevcapture] CMD /<command> -> <result>`. When the client joined a server instead of a single-player
world (plan `world.join = server`, 2.11), `cmd` is sent as a chat command and its result is not known.

**Readback (TX-03, `Reads.java`).** Each logs `[cdevcapture] READ <kind> <text>` (one line per value) and adds the
lines to the test's `reads`. Optional `"expect"` / `"reject"` (Java regex, `find`) fail the step on no match / a match.

| Step | READ line |
|---|---|
| `{"assert_screen": "<class or simple name>" \| "none"}` | `assert_screen <open screen class>`; fails on mismatch |
| `{"screen_info": true}` | `screen_info <class> title "<title>"` |
| `{"slot_item": N}` | `slot_item N <id> x<count> [nbt <snbt>]` or `empty`; slot of the open container, else player inventory index |
| `{"tooltip_text": "<id>" \| {"slot": N} \| "held", "shift", "ctrl", "alt", "advanced", "nbt", "count"}` | `tooltip_text <id> <i>: <line>` per line, from `getTooltipLines` (Forge tooltip event included, virtual modifiers held) |
| `{"player_pos": true}` | `player_pos x y z yaw a pitch b ground <bool> dim <dim>` |
| `{"player_motion": true}` | `player_motion v dx dy dz moved dx dy dz hspeed h bps b` (velocity, last tick's real move) |
| `{"held": true}` | `held main <id> x<n> off <id> x<n>` |
| `{"effects": true}` | `effects <id> amp a dur d` per effect, or `none` |
| `{"target": true}` | `target block <id> at x y z`, `target entity <type>`, `target miss` |
| `{"widgets": true}` | `widgets <i> <class> "<message>" x y w h [value "<text>"] [inactive]` (the index TX-05 uses) |

**Movement (TX-04, `Moves.java`).** Keys are held through the game's key mappings (normal input path), counted in
client ticks. Pressed at the end of the tick the step starts in, so logged tick 1 is the first tick of input. Every
tick logs `READ move <n> pos x y z moved dx dy dz hspeed h bps b ground <bool>`; the end logs
`READ <kind> ticks N dist D bpt S dy Y from x y z to x y z` (`expect` works on all of these lines).

| Step | Does |
|---|---|
| `{"walk": N, "keys": "forward sprint", "after": M}` | holds `forward back left right jump sneak sprint use attack` for N ticks, logs M more released |
| `{"sneak": N}` | `walk` N with `sneak` |
| `{"jump": N}` | jump held 1 tick, N ticks logged |
| `{"fly": true \| false}` | creative flight on / off (`look` with `from` turns flight on; use `fly: false` or survival before walking) |

Toggle-mode sprint / sneak mappings are pressed on the edges only. A walk step's timeout is its own length + 20 ticks.

**Widget actions (TX-05, `Widgets.java`).** Resolved from the open screen's widget list (`children()`, nested
containers flattened), never from window pixels; clicks go through the screen's own `mouseClicked` /
`mouseReleased` at the center, and the virtual mouse is parked there.

| Step | Does |
|---|---|
| `{"click_slot": N, "button": 0, "shift": false}` | clicks slot N of the open container (shift-click through the virtual Shift) |
| `{"click_widget": REF, "button": 0}` | REF: `"label"` (message, exact then substring, case-insensitive), `N` (index in `READ widgets`), `{"class": "EditBox", "n": 0}` |
| `{"type_into": REF \| null, "text": "...", "clear": true, "enter": false}` | clicks REF (null: keep the focused widget), clears an EditBox, types each character through the screen's `charTyped`; the note has the EditBox value |
| `{"scroll": d, "at": REF}` or `{"scroll": d, "x": gx, "y": gy}` | `mouseScrolled` there (default screen center) |
| `{"click": {"x": gx, "y": gy} \| {"px": wx, "py": wy}, "button": 0}` | raw click in GUI coordinates or window pixels |
| `{"press": "escape" \| "e" \| "enter" \| "key.inventory", "shift": false}` | a key: to the open screen (`keyPressed` / `keyReleased`); with no screen through its key mapping, `escape` opens the pause menu. F3 combinations are not keys here (`reload` covers F3+T) |

### 2.3 Capture and crop

Each `capture` entry: `name` (file suffix, empty for the main shot), `crop`, `hide`, `frames`, `pad` (default 4 GUI
px), `max_edge` (default 1024 px).

| Crop | Rect source |
|---|---|
| `full` | Whole frame |
| `gui` | Open `AbstractContainerScreen`'s `leftPos/topPos/imageWidth/imageHeight`; for JEI's recipes screen, its area through the JEI runtime |
| `tooltip` | Bounds the driver records from `RenderTooltipEvent.Pre` / `.Color` in the captured frame (pre-border; `pad` covers Legendary Tooltips' frame) |
| `auto` | Cropper trims everything matching the stage background (only meaningful after `tooltip` or another flat-background stage) |
| `hotbar`, `hud:wthit`, `hud:topleft`... | Named regions in GUI coords from `crop_regions.json` (WTHIT top-center box, AppleSkin bars, hotbar), measured once at the fixed GUI scale |
| `center:WxH` | Box of W x H GUI px around the crosshair (block looks) |
| `rect:x,y,w,h` | Explicit GUI coords |
| `slot:N` | One slot of the open screen, for texture checks |

All rects are in GUI coordinates and converted with the frame's GUI scale, so a different window size doesn't break
specs. `hide` takes `chat`, `hand`, `crosshair`, `hud`, `toasts`, `debug`. Toasts and chat are cleared before every
capture whatever `hide` says (a recipe-unlock toast landing on a JEI page is the most common way to spoil a shot).

`frames: [n, interval]` takes n shots `interval` ticks apart (`FX-43_f1.png`...). Use it where JEI cycles tag
ingredients or a block animates.

### 2.4 Test isolation

- Each test gets its own plot: test index i -> origin `(i * 48, 64, 0)` in the flat world, so tests can't see each
  other's blocks and a failed test leaves nothing in the next one's view. The runner allocates the plots.
- Before each test: close the screen, release keys, clear inventory and effects, `/time set 6000`, `/weather clear`,
  creative mode, clear chat and toasts. After it: nothing (plots are not reused within a run).
- A step that throws or times out (default 30 s per step, 120 s per test) fails the test: the driver saves the frame
  it has as `raw/<ID>__failed.png`, logs why, and moves on.

### 2.5 World templates

The driver makes the world itself when `saves/<world>` is absent (`TestWorld.createFlat`): default superflat layers,
the group's `biome`, no structures, the group's `seed` (default 1), creative with cheats, gamerules `doDaylightCycle`,
`doWeatherCycle`, `doMobSpawning`, `doFireTick`, patrol and trader spawning off, `randomTickSpeed 0` (raise it per
test with `cmd`), `sendCommandFeedback false`, `announceAdvancements false`. The runner deletes `saves/<world>` before
every launch, so each launch starts from a fresh world. A hand-made template in `tools/client_capture/worlds/<name>/`
is copied in instead when it exists (none yet; a `normal` template with a chosen biome comes later for world-feel
shots).

In 1.19.2 there's no quick-play launch argument, so the driver loads the world itself from the title screen
(`createWorldOpenFlows()`), then waits for the player, `chunks` and `jei`.

### 2.6 Game dir and client settings

Default `~/.cache/ce_capture/game/` (`--game DIR` for another), outside the scratchpad and the instance root. On
each launch the runner:

- runs `tools/perf/eval/eval_client.py setup GAME`: `mods/` hard links to the instance's jars, `kubejs/`,
  `config/`, `defaultconfigs/`, `resourcepacks/` copied, `*_upstream` folders symlinked, the user's `options.txt`;
- deletes the two perf-eval scripts that setup adds (`zz_perfeval_drive.js` switches to spectator and teleports);
- swaps in the driver: unlinks the hard-linked `creativedevtools-*.jar` and copies
  `development/creativeDevtools/build/libs/creativedevtools-1.0.0.jar` (the capture driver is nested in it); every
  `--jar PATH` replaces the same mod's jar the same way (unlink first, so the instance's `mods/` is never written
  through a link);
- `--fresh-config`: empty `config/` (pack-default checks); then the group's `client_config` overlay;
- sets in `options.txt`: `guiScale:2`, `renderDistance:6`, `simulationDistance:6`, `pauseOnLostFocus:false`,
  `fullscreen:false`, `overrideWidth:1280`, `overrideHeight:720`, `narrator:0`, `tutorialStep:none`,
  `joinedFirstServer:true`, `skipMultiplayerWarnings:true`, `soundCategory_master:0.0`, `gamma:0.5`, `fov:0.0`,
  `lang:en_us`, `maxFps:120`;
- launches with `eval_client.py launch` (the user's `-Xmx`, a JFR recording, `DISPLAY` default `:1`, the one-JVM
  check) and passes `-Dcdevcapture.plan=<plan>` through `JAVA_TOOL_OPTIONS` (eval_client has no JVM-argument hook).
  No `--server` for runs: the integrated server is the server, so a run is one JVM.

### 2.7 Driver internals

- Inert unless `cdevcapture.plan` is set. Nested in Creative Devtools (`META-INF/jarjar/cdevcapture-0.1.0.jar`), so
  it is in the instance's `mods/` too, doing nothing there; the runner swaps in the fresh `build/libs` jar.
- Runs on the client tick as a state machine (one step at a time, never blocking the render thread). `cmd` is sent to
  the integrated server with `server.execute(...)` and the step waits for its result.
- Capture: after the settle frames, at the end of a frame (`TickEvent.RenderTickEvent` phase END),
  `Screenshot.takeScreenshot(mainRenderTarget)` gives the framebuffer as a `NativeImage` (already
  flipped). It's written to `raw/<ID>[_name].png` with a sidecar `<ID>[_name].json`: GUI scale, frame size, the
  recorded rects (gui, tooltip, slot), open screen class, player pos and look, test step timings. Reading the
  framebuffer rather than the X window means another window over the game doesn't spoil the shot.
- Mixins (the reason this is a mod and not a client script): `Screen.hasShiftDown/hasControlDown/hasAltDown` return
  the virtual key state; `MouseHandler.xpos()/ypos()` return the virtual mouse while a `hover` is active, so slot
  tooltips render without moving the real cursor; a `RenderHandEvent` cancel for `hide: hand`.
- Log markers: `[cdevcapture] BEGIN <ID>`, `[cdevcapture] STEP <n> <kind> ok|fail <why>`, `[cdevcapture] END <ID> ok|fail`.
  The runner slices `logs/latest.log` between markers and keeps any `ERROR`, `Exception` or
  `Error occurred while handling event` lines per test in the manifest.
- `progress.json` is rewritten after each test (done IDs, current ID). At the end: `[cdevcapture] DONE`, then the
  driver calls `Minecraft.stop()` itself so the runner doesn't have to kill anything. With a `channel` in the plan it
  enters phase SERVE instead and stays up (2.11) until a `stop` request.
- Files: `CaptureDriver` (phases BOOT, CREATING, JOINING, SETUP, TESTS, SERVE, DONE), `CapturePlan`, `Steps` (2.2
  capture steps, dispatch), `Reads` (TX-03), `Moves` (TX-04), `Widgets` (TX-05), `Channel` (TX-02), `Capture`,
  `StageScreen`, `TestWorld`, `VirtualInput`, mixins `ScreenModifierMixin`, `MouseHandlerMixin`.

### 2.8 Runner

```
python3 tools/client_capture/run_captures.py --list [GROUP ...] [--check]   # tests per group; --check validates
python3 tools/client_capture/run_captures.py CQ-NEW                         # a run
python3 tools/client_capture/run_captures.py CQ-NEW --only FX-43,N-02 [--jar PATH]... [--fresh-config]
python3 tools/client_capture/run_captures.py CQ-NEW --plan-only             # run folder + plan, no launch
python3 tools/client_capture/run_captures.py --resume <run dir>             # continue after the runner stopped
python3 tools/client_capture/run_captures.py --recrop <run dir>             # specs re-read, crop.py, no game
python3 tools/client_capture/run_captures.py serve [--join HOST:PORT --ownsrv PID]   # driver serves the channel
python3 tools/client_capture/run_captures.py serve ... --drop 'creativeLabels-*.jar' --config 'config/x-client.toml:general:a=5'
                                                 # per launch: unlink jars from GAME/mods, set a toml key (raw value)
python3 tools/client_capture/run_captures.py CQ-NEW --display xvfb         # EXPERIMENTAL own Xvfb (TX-07), not colour rows
python3 tools/client_capture/run_captures.py stop                           # serving client quits
```

1. Specs resolved: macros inlined, `$X $Y $Z` (with `+N` / `-N`) replaced by the plot origin, `item_dict:<key>`
   resolved from `item_dict.js` (`pack_paths.parse_item_dict`); unknown step kinds, duplicate IDs, missing `expect`
   and unknown keys stop the run. `--check` also requires every `covers` to be a queue row and warns when a test ID
   is in no writeup.
2. Preflight: `tools/scratch_query/game_jvms.sh` must print nothing (it prints `pid cwd` only, never a command line);
   the driver jar must exist.
3. Game dir (2.6), `capture_plan.json` with the remaining tests, launch. Polls `progress.json`, `results.jsonl` and
   the pid. Boot (no progress or `loading`) may take 900 s; after that no change for 180 s (test timeout + 60 s) is a
   hang.
4. Crash (pid gone before `done`) or hang (own pid killed, TERM then KILL): `latest.log` copied as
   `client_<n>.log`, `hs_err_pid<pid>.log` and new `crash-reports/` copied to `crash/`, the running test marked
   `crashed` with the log tail, relaunch with the tests not yet done. Two crashes on one test: `skipped`. Two crashes
   before any test: the run stops. At most tests + 3 launches.
5. `crop.py <run>` writes the crops, `manifest.json` and `index.md`, and prints the counts.

`serve` (TX-02): the same game dir with a plan that has no tests and `"channel": GAME/cdev_channel`; returns once the
driver's heartbeat says SERVE and leaves the client up for `qrun.py client GAME ...`. `--join HOST:PORT` (with
`--ownsrv PID` for the caller's own scratch server) launches with `--server` and sets `world.join = server`, so the
driver skips the world load and sends `cmd` as chat commands (item 5 paired runs build on this; one-JVM carve-out D2
of the plan applies only when one driver starts and stops both).

### 2.9 Output

```
test_captures/judgement/          git-ignored (allow-list plus an explicit line); wiped by the owner after judging
  2026-11-15_1530_CQ-NEW/
    FX-43.png  FX-43_hotbar.png ...   crops (frames: <ID>_f1.png ...)
    manifest.json      per test: id, covers, title, expect, group, spec, status (ok / fail / crashed / skipped /
                       missing), error, files, raw, reads, steps, log_errors, crash, crop_errors,
                       judge (owner / text), verdict (empty for owner rows; PASS / FAIL for text rows), verdict_by;
                       plus jar md5s (creative*.jar in the game dir) and the git hash with the kubejs change count
    index.md           ID, covers, expect, images, driver status, judge, Verdict (blank) + a Readback list
    run.json  capture_plan.json  results.jsonl  progress.json  runner.json  client_<n>.log  crash/
    raw/               full frames + sidecars, <ID>__failed.png for failed tests (kept for --recrop)
  latest -> 2026-11-15_1530_CQ-NEW
  qrun_<RES name>/     shots from qrun.py client on the driver (2.11): <ROW>_<NAME>.png + sidecar + manifest.json
```

`build_deploy.py` follows the same ignore rules, so nothing here ships.

### 2.10 Evaluation

Owner rule (section 0): the owner opens `index.md` (Markdown preview) and fills the Verdict column (`pass`, `fail`,
`unclear` + a note) for every `owner` row; no agent or tool writes a visual verdict. `text` rows already carry PASS /
FAIL from their read steps. A session then carries the verdicts into the queue's Run log and the writeup tests
(`fail` goes to the writeup first), and the owner wipes the run folder. `unclear` means the shot can't decide it: fix
the spec (crop, wait, angle) and re-run that ID (`--only`); it doesn't count as a pass.

### 2.11 Channel and qrun client mode (TX-02)

File channel between a host tool and the driver, polled once per client tick (`Channel.java`, `tools/client_capture/chan.py`).
Item 5 (paired client + server batches) builds on it.

| Path (under `GAME/cdev_channel/`) | Writer | Content |
|---|---|---|
| `in/<seq>.json` | host, written as `<seq>.json.tmp` then renamed | `{"seq", "op": "run" \| "ping" \| "stop", "id": "<row>", "reset": false, "timeout_ticks": 2400, "steps": [<step objects of 2.2>]}` |
| `out/<seq>.json` | driver, `.tmp` then atomic rename | `{"seq", "id", "status": "ok" \| "fail", "error", "files": [raw png names], "reads": [READ texts], "steps": [step log], "ticks"}`; `ping` / `stop` reply `{"seq", "op", "status": "ok"}` |
| `state.json` | driver, every 20 client ticks | `{"phase": "BOOT" \| ... \| "SERVE", "current", "seq" (-1 idle), "tick", "time" (ms)}` |
| `raw/` | driver | frames of `capture` steps |
| `pid` | `run_captures.py serve` | the client pid |

- `seq` is a number (chan.py uses epoch milliseconds); requests run one at a time, lowest `seq` first; the driver
  deletes a request file when it takes it. A step failure ends that request (`status: fail`, `<id>__failed.png` in
  `raw/`), never the client.
- `reset: true` runs the per-test reset (close screen, release keys, creative, clear, effects, noon, clear weather)
  first. After every request the driver releases virtual modifiers, the virtual mouse and the movement keys.
- The host treats a heartbeat older than 60 s as a dead or hung client (`ChannelError`). Log markers per request:
  `[cdevcapture] BEGIN <id> seq <n>`, `STEP ...`, `READ ...`, `CMD ...`, `END <id> ok|fail`.

`qrun.py client GAME BATCH RES` uses the channel whenever `GAME/cdev_channel/` exists (`--xdotool` forces the window
transport; a stale heartbeat exits with a message instead of typing into a window): `>` runs `cmd` (with
`allow_fail`; judged by the row's log checks), `key` becomes `press` steps (`F3+t` = `reload`), `click X Y` a
window-pixel `click`, `shot NAME` a `capture` copied to `test_captures/judgement/qrun_<RES name>/` (row ends LOOK,
owner judges), and the new directive `do JSON` sends any step object or list (a failed step FAILs the row). Each
directive waits for the driver's reply, so pace sleeps are skipped. READ lines are in `latest.log`, so `expect`,
`count` and `waitlog` judge them as usual.

## 3. What the pack changes

Nothing for players. The driver is a separate jar that only the runner puts into its own game dir. Queue changes:

- Round 2 rows in [testing/scratch_queue.md](testing/scratch_queue.md) get a `Capture` column: `spec` (in a spec
  file), `manual` (feel, movement, timing, sound: stays a hand test), or `partial` (some shots, rest by hand).
- When a session adds a client-side queue row, it writes the spec entry in the same session if the row is
  capturable. Same rule as the queue itself: written now, run later in a bulk session.

What is not capturable, and stays manual: walking feel (BlockRunner, leaf piles), replant feel, timing (cooldowns,
golden egg odds), sound, anything that needs a real player's survival play over minutes. Command-reachable state
(a cooldown bar, a config-raised chance) can sometimes be staged and captured instead; decide per row.

## 4. Limits and decisions

### 4.1 Open decisions (user)

- D1 (closed 2026-11-01): the driver is a member of Creative Devtools (`members/capture`, mod id `cdevcapture`),
  inert without the system property.
- D2 (closed 2026-11-15): real 1280 x 720 window on `:1` by default (capture reads the framebuffer, so windows on top
  don't matter, but the window must not be minimized). Xvfb is installed; the experimental `--display xvfb` (TX-07,
  built 2026-11-16, not run in game: CQ-TXC-11) starts an own Xvfb per launch through `eval_client.start_xvfb`
  (`GAME/xvfb.pid`, a detached reaper stops it when the client pid is gone) and records `display` in `run.json`. Only
  for tests that cannot run otherwise (unattended blocks); never for color rows.
- D3 (default taken): copy of the instance's `config/`; `--fresh-config` for pack-default checks.
- D4 (open, owner): test IDs for queue rows that have no writeup test ID. The first spec set uses the queue ID with a
  letter (`CQ-NEW-04a`); alternative: give each such row a writeup test ID first.
- D5 (open, owner): `tooltip_text` reads `getTooltipLines` (Forge tooltip event included) but not
  `RenderTooltipEvent.GatherComponents` additions or image components (AppleSkin bars); those stay captures. Is that
  the right split for the names / tooltip rows, or should a later TX-09 dump hook the render path too?

### 4.2 Decisions taken in this design

- Crop on the host, not in game: rects come from the driver, pixels from PIL, so crops can be redone without the game.
- Single-player (integrated server) for capture runs. Client + own scratch server only through `serve --join` under
  the plan's D2 carve-out (one driver starts and stops both, item 5).
- File names are writeup test IDs, not queue IDs: the queue rows are temporary, the writeup IDs are permanent.
- A flat stage screen for item tooltips instead of hovering in the creative inventory: deterministic place, `auto`
  crop works, and it still runs the real tooltip events.
- No pixel-diff against stored "golden" images. Visual tests here are one-off checks of new work, not regression
  suites, and mod updates change rendering anyway. Could be added later per test (`"golden": true`) if a regression
  set appears.
- Rejected: screenshots by `xdotool` + ImageMagick `import` of the X window (needs focus and no overlap, knows nothing
  about GUI bounds); a KubeJS client script driver (no way to fake shift or mouse position, JEI runtime access is
  awkward in Rhino).

### 4.3 Known risks

- JEI recipe screen bounds: confirm which API call gives its area in JEI 11.8 (fallback: `full` crop plus a named
  region).
- `RenderTooltipEvent` bounds before Legendary Tooltips' frame: covered by `pad`; verify on the first run (CC-04).
- Recipe-unlock and advancement toasts: the pack drops advancements, but recipe toasts still fire; cleared before
  each shot.
- A client crash takes the run's world with it; the runner relaunches with a fresh world and the remaining tests
  (plots are allocated per test, so continuing in a new world is fine).
- `tooltip_text` and the `key` step cover tooltip code that asks `Screen.hasShiftDown()`; code polling GLFW directly
  sees no Shift.
- Movement keys set through `KeyMapping.setDown` assume no real keyboard events reach the window during a hold (the
  window has no focus in a run); a toggle-mode sprint / sneak already on at the start is flipped off.
- `press` with no screen open goes through `KeyMapping.click` (handled on the next tick); F3 debug combinations are not
  reachable.

## 5. File map

| Path | What |
|---|---|
| `development/creativeDevtools/members/capture/` | Driver (2.7 files) |
| `tools/client_capture/run_captures.py` | Runner, `serve`, `stop` |
| `tools/client_capture/crop.py` | Cropper, manifest, `index.md`, `--selftest` |
| `tools/client_capture/crop_regions.json` | Named HUD regions in GUI coords (expressions of W, H) |
| `tools/client_capture/chan.py` | Channel host side (library + CLI: `state`, `ping`, `stop`, `run`) |
| `tools/client_capture/worlds/<name>/` | Optional world templates (none yet; the driver creates flat worlds) |
| `tools/perf/eval/eval_client.py`, `tools/item_repro/launch_client.py` | Game dir setup and launch (reused as is) |
| `tools/scratch_query/qrun.py` | `DriverClient` transport, `do` directive (2.11) |
| `feature_writeup/testing/capture_specs/*.json` | Spec files per queue group, `_macros.json` |
| `test_captures/judgement/` | Output for the owner, not tracked |

## 6. Offline checks

Done 2026-11-15 (scratchpad, no game):

- `run_captures.py --list --check`: the first spec set parses, item_dict keys resolve, IDs unique, `covers` are queue
  rows; one warning (CQ-NEW-04a is in no writeup, D4). `--plan-only` gives plots 48 apart at y -60 and `$X+2` /
  `$X.5` substituted.
- `crop.py --selftest`: `auto`, `tooltip`, `gui`, `center:`, `slot:`, `rect:`, `hotbar`, `full` + `max_edge` give the
  expected sizes on a synthetic 1280 x 720 frame. A fake run (results, runner crash entry, log with an ERROR line)
  gives judge `owner` / verdict empty for the capture test, `text` / PASS for the read test, `skipped` with the crash
  reason, the ERROR line under the right test; `--recrop` rebuilds it.
- Channel: a Python fake driver speaking 2.11; `qrun.py client` on it: `>` + `do` read with expect PASS, a failing
  walk expect FAIL with the driver's error, `key` / `click` / `shot` (copied to the judgement folder, LOOK), a forced
  step failure FAIL; `chan.py ping` / `stop`; a stale heartbeat raises `ChannelError`. `qrun.py lint` accepts `do`.
- Driver Java compiles in `:creativeDevtools:build`; its behavior in game is CQ-TXC-01 to -10.

## 7. In-game test suite

Run by the runner itself on a first group (CQ-NEW is small and covers a block color, a tooltip, a HUD mod).

- [ ] **CC-01:** Preflight refuses to start while any `java` process is up and names its cwd; never prints a command line.
- [ ] **CC-02:** A run reaches the world, writes `[cdevcapture] BEGIN` / `END` for every test, and the client exits by itself.
- [ ] **CC-03:** `FX-43.png` shows the moss and grass at the crosshair, no chat, no hand, no toast; `FX-43_hotbar.png`
      is the hotbar only.
- [ ] **CC-04:** `tooltip` + `auto`: the whole Legendary Tooltips frame is inside the crop, with no background margin
      over `pad`.
- [ ] **CC-05:** `shift` capture shows the shift-only tooltip lines (glycerol: "Tastes sweet!" once).
- [ ] **CC-06:** `jei_recipes` + `gui` crops to the JEI page, and `frames` gives cycled ingredients.
- [ ] **CC-07:** `use` on a placed pickling jar opens its GUI; `gui` crop; `hover` on a slot shows that slot's tooltip.
- [ ] **CC-08:** A failing `cmd` fails only its own test (`__failed.png`, manifest status, log lines) and the run goes on.
- [ ] **CC-09 Killing the client mid-run:** the runner marks that test `crashed`, relaunches, finishes the rest; the
      manifest has every test once.
- [ ] **CC-10:** `crop.py` alone after a spec crop edit rewrites the PNGs from `raw/` without the game.
- [ ] **CC-11:** Other windows over the game during the run don't show in any capture.
- [ ] **CC-12:** Nothing from the run lands in the instance's `mods/`, `logs/`, `saves/` or `config/`.
- [ ] **CC-13 Readback:** `player_pos`, `held`, `effects`, `slot_item`, `assert_screen`, `widgets` log `READ` lines; the
      CC group's text test is PASS in the manifest.
- [ ] **CC-14:** `walk` 40 ticks logs 41 `READ move` lines and a summary; open-ground survival walk averages about
      0.2 b/t; two identical walks agree to 0.001 b/t (tick-exact).
- [ ] **CC-15 Anvil:** `click_slot` shift-moves paper in, `type_into` the name field, the result slot reads "Capture".
- [ ] **CC-16:** `serve` reaches SERVE; `qrun.py client` on it runs `>`, `do`, `key Escape`, `shot` with no xdotool; the
      shot lands in `test_captures/judgement/qrun_<RES>/`.
- [ ] **CC-17 Killing the served client mid-batch:** qrun ABORTs within 60 s of the last heartbeat (or the crash watch
      flags it first).
- [ ] **CC-18:** `click_widget` by label and by index, `scroll`, `press e` (no screen: inventory opens) on vanilla screens.

## 8. History

- 2026-10-24: designed (this file). Nothing built.
- 2026-11-15: runner, cropper, channel client and first spec set built (plan_scratch_testing_expansion items 2 and 4:
  TX-01, TX-03, TX-02, TX-04, TX-05); driver gained read, movement and widget steps and the channel; judgement rule
  (owner judges all pixels in `test_captures/judgement/`). Not run in game: CQ-TXC-01 to -10.
