# Client capture (queued visual tests run by a throwaway client)

## 0. At a glance

| Part | Status |
|---|---|
| Design (this file) | DESIGNED 2026-10-24, nothing built |
| Driver mod `development/creativeDevtools/members/capture` (mod id `cdevcapture`, dev-only, nested in Creative Devtools since 2026-11-01) | not started |
| Runner `tools/client_capture/run_captures.py` | not started |
| Cropper `tools/client_capture/crop.py` | not started |
| Spec files `feature_writeup/testing/capture_specs/*.json` | not started |
| Output `test_captures/` (project root, git-ignored by the allow-list) | not started |

Open decisions are in section 4.1. TODO: `tooling/generators-and-tests #6`. Test prefix `CC-`.

The point: Round 2 of [testing/scratch_queue.md](testing/scratch_queue.md) (the `CQ-` rows) is mostly "open this,
look at that": a JEI page, a tooltip, a WTHIT line, a block's colour, a GUI. Today each needs the user at the keyboard.
With this system a session (or the user) picks the capturable rows, one throwaway client runs them all unattended in a
single-player world, and each check leaves one cropped PNG named after its test ID in `test_captures/<run>/`, next
to a manifest that says what the picture should show. A later session (or the user) reads the PNGs and writes the
PASS / FAIL into the writeups the usual way.

## 1. Overview

```
spec JSON (per queue group) ──> run_captures.py ──> scratch game dir ──> client JVM + cdevcapture driver
                                    │                                      │ loads template world
                                    │                                      │ per test: reset, steps, capture
                                    │                                      ▼
                                    │                     raw/<ID>.png + rects + progress.json + log markers
                                    ▼
                               crop.py ──> test_captures/<run>/<ID>.png + manifest.json + index.md
                                    ▼
                         evaluation (session reads PNG + "expect" text) ──> writeup ticks, queue run log
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
  "world": "flat_creative",
  "client_config": {},
  "tests": [
    {
      "id": "FX-43",
      "covers": ["CQ-NEW-02"],
      "title": "Moss follows biome grass colour",
      "expect": "Moss block and moss carpet on the left match the grass block beside them (swamp: dark olive). The item in the hotbar slot stays vanilla bright green.",
      "steps": [
        {"cmd": "fillbiome $X $Y $Z ~16 ~4 ~16 minecraft:swamp"},
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
      "expect": "Onion popcorn tooltip: AppleSkin hunger bar 2 drumsticks longer than plain popcorn's; cheese and garlic lines present.",
      "steps": [{"tooltip": "item_dict:popcorn_onion"}],
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
- `world` names a template (2.5). `client_config` is a per-group overlay of client toml values written before launch
  (2.6). Tests that need a different client config go in a different group.

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
| `tooltip` | Opens the driver's stage screen: flat `#202020` background, the item at the centre, its tooltip drawn by the normal path (so Legendary Tooltips, AppleSkin, KubeJS and our tooltip lines all run). Options: `shift`, `ctrl`, `advanced` (F3+H) |
| `hover` | Puts the virtual mouse at GUI coords or on slot N of the open screen (2.7), for tooltips inside a real GUI |
| `key` | Holds a modifier (`shift`, `ctrl`, `alt`) for the next capture |
| `reload` | F3+T resource reload, then waits `resources` |
| `macro` | Inlines a step list from `_macros.json` |

### 2.3 Capture and crop

Each `capture` entry: `name` (file suffix, empty for the main shot), `crop`, `hide`, `frames`, `pad` (default 4 GUI
px), `max_edge` (default 1024 px).

| Crop | Rect source |
|---|---|
| `full` | Whole frame |
| `gui` | Open `AbstractContainerScreen`'s `leftPos/topPos/imageWidth/imageHeight`; for JEI's recipes screen, its area through the JEI runtime |
| `tooltip` | Bounds the driver records from `RenderTooltipEvent.Pre` / `.Color` in the captured frame (pre-border; `pad` covers Legendary Tooltips' frame) |
| `auto` | Cropper trims everything matching the stage background (only meaningful after `tooltip` or another flat-background stage) |
| `hotbar`, `hud:wthit`, `hud:topleft`... | Named regions in GUI coords from `crop_regions.json` (WTHIT top-centre box, AppleSkin bars, hotbar), measured once at the fixed GUI scale |
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

`tools/client_capture/worlds/<name>/` holds a small saved world, copied fresh into the game dir's `saves/` on each
run. First template: `flat_creative`, superflat, creative, cheats on, gamerules `doDaylightCycle`, `doWeatherCycle`,
`doMobSpawning`, `doFireTick`, `randomTickSpeed 0` (raise it per test with `cmd`), `sendCommandFeedback false`,
`announceAdvancements false`. Fixed seed. A `normal` template (fixed seed, spawn on a chosen biome) comes later for
world-feel shots.

In 1.19.2 there's no quick-play launch argument, so the driver loads the world itself from the title screen
(`createWorldOpenFlows().loadLevel(...)`), then waits for the player, `chunks` and `jei`.

### 2.6 Game dir and client settings

Built by the runner in `~/.cache/ce_capture/game/`, outside the scratchpad (it gets wiped) and outside the instance
root (no stray `logs/` beside the real ones). On each run:

- `mods/`: hard links to the instance's jars (same filesystem; the client may add files, which must not land in the
  real `mods/`) plus `cdevcapture-*.jar` from the driver's `build/libs`. Optional `--jar <path>` swaps in a fresh build
  of one of our mods without touching `mods/`.
- `kubejs/`, `vinery_upstream/` copied (the Vinery port refuses to load without the latter; memory
  `client-launch-for-defaults`).
- `config/`: copy of the instance's `config/` (what players run), then the group's `client_config` overlay.
  Option `--fresh-config` starts empty instead, for checks of the pack defaults.
- `options.txt` written whole: `guiScale:2`, `pauseOnLostFocus:false` (the window will not have focus),
  `renderDistance:6`, `fov:0.0`, `narrator:0`, `tutorialStep:none`, `joinedFirstServer:true`,
  `skipMultiplayerWarnings:true`, sound master 0, `fullscreen:false`, `gamma:0.5`, `entityShadows:true`, english.
- Launch: `launch_client.py`'s argument rebuild (made importable), plus `--width 1280 --height 720` and
  `-Dcdevcapture.plan=<game dir>/capture_plan.json`. No `--server`: the integrated server is the server, so the run is
  one JVM.

### 2.7 Driver internals

- Inert unless `cdevcapture.plan` is set; it's a separate dev-only jar anyway (4.1, D1).
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
  driver calls `Minecraft.stop()` itself so the runner doesn't have to kill anything.

### 2.8 Runner

```
python3 tools/client_capture/run_captures.py CQ-NEW CQ-VP            # groups
python3 tools/client_capture/run_captures.py CQ-NEW --only FX-43 FO-49
python3 tools/client_capture/run_captures.py --list                  # tests per group, capturable or manual
python3 tools/client_capture/run_captures.py --resume <run>          # continue after a crash
python3 tools/client_capture/crop.py test_captures/<run>             # re-crop only
```

1. Preflight: `pgrep -x java` must be empty (memory `one-game-instance-at-a-time`; never the full command line).
   If not, stop and say whose JVM it is (`readlink /proc/PID/cwd`). Check the driver jar exists and the spec files
   parse; resolve `item_dict:` keys from the exported dict and fail early on a missing key.
2. Build the game dir (2.6), write `capture_plan.json` (resolved steps, plots, output path).
3. Launch with `DISPLAY` (default `:1`, see D2). Poll `progress.json` and the log. Main menu ~90 s, world load
   ~60 s more.
4. Crash or hang (no progress for the per-test timeout + 60 s): kill its own recorded pid only, mark the current test
   `crashed` with the log tail, relaunch with the remaining tests. Two crashes on the same test: skip it.
5. Call `crop.py`, print the summary: captured / failed / crashed, and the run folder.

### 2.9 Output

```
test_captures/
  2026-10-24_1530_CQ-NEW/
    FX-43.png  FX-43_hotbar.png  FO-49.png ...
    manifest.json      one entry per test: id, covers, title, expect, files, status, step log,
                       log errors, jar md5s (creativeengineering, the backports), kubejs git hash, spec file
    index.md           table: ID, expect, image link, status, verdict (blank) ; opens in VS Code preview
    raw/               full frames + sidecars (delete with --no-raw; kept by default for re-crops)
  latest -> 2026-10-24_1530_CQ-NEW
```

`test_captures/` is not tracked (the `.gitignore` allow-list ignores it) and not shipped (`build_deploy.py` follows
the same ignore rules). Old runs are deleted by hand.

### 2.10 Evaluation

A session evaluating a run reads `manifest.json`, then each PNG with the Read tool against its `expect`, and writes a
`verdict` (`pass`, `fail`, `unclear`) plus a one-line note into `manifest.json` and `index.md`. Then the normal
reporting path: tick the writeup tests, add a run log entry under the queue's Run log, `fail` goes to the writeup
first (section 7 / status block). `unclear` means the shot can't decide it: fix the spec (crop, wait, angle) and
re-run that ID; it doesn't count as a pass. The cropping is what keeps this cheap: a cropped tooltip is a few hundred
pixels a side, so a 40-test run is a short read.

The user can do the same by opening `index.md` in a Markdown preview.

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

- **D1. Where the driver lives.** Recommended: its own dev-only mod `development/clientCapture` (mod id `cdevcapture`),
  built by the root Gradle build, never copied into `mods/`, never deployed; `mod_sides.md` row "client, dev-only, not
  in mods/". The other option, a `devtools/capture` package in patcherMod gated on the system property, ships capture
  code and the shift/mouse mixins to every player and ties driver fixes to patcherMod releases.
- **D2. Display.** No Xvfb is installed, so the client opens a real 1280 x 720 window on `:1` for the run (as
  `client-launch-for-defaults` does today, a few minutes per group). Capture reads the framebuffer, so other windows
  on top don't matter, but the window must not be minimised (Minecraft stops rendering). Option: `sudo apt install
  xvfb` and run on a virtual display; that uses Mesa's software renderer (llvmpipe), so it's much slower with this
  modlist and colours may not match the user's GPU exactly. Recommended: the real window, try Xvfb only if the window
  becomes a nuisance.
- **D3. Config source.** Recommended default: copy of the instance's `config/` (what players run); `--fresh-config` for
  pack-default checks.

### 4.2 Decisions taken in this design

- Crop on the host, not in game: rects come from the driver, pixels from PIL, so crops can be redone without the game.
- Single-player (integrated server), never client + scratch server: that would be two JVMs.
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
- A client crash takes the run's world with it; the runner relaunches with a fresh copy of the template and the
  remaining tests (plots are allocated per test, so continuing in a new world is fine).

## 5. File map (planned)

| Path | What |
|---|---|
| `development/creativeDevtools/members/capture/` | Driver mod: `CaptureDriver` (state machine), `Steps`, `Capture` (framebuffer + sidecar), `StageScreen`, mixins `ScreenModifierMixin`, `MouseHandlerMixin` |
| `tools/client_capture/run_captures.py` | Runner |
| `tools/client_capture/crop.py` | Cropper, manifest, `index.md` |
| `tools/client_capture/crop_regions.json` | Named HUD regions in GUI coords |
| `tools/client_capture/worlds/flat_creative/` | World template |
| `tools/item_repro/launch_client.py` | Argument rebuild, made importable (`build_args(game_dir, extra)`) |
| `feature_writeup/testing/capture_specs/*.json` | Spec files per queue group, `_macros.json` |
| `test_captures/` | Output, not tracked |

## 6. Offline checks

- `run_captures.py --list --check`: every spec parses, every `item_dict:` key resolves, every ID is unique and
  exists in some writeup's test suite (grep), every `covers` names a queue row.
- `crop.py` on a stored raw frame + sidecar set under `tools/client_capture/fixtures/` gives the expected crop sizes.

## 7. In-game test suite

Run by the runner itself on a first group (CQ-NEW is small and covers a block colour, a tooltip, a HUD mod).

- [ ] CC-01 Preflight refuses to start while any `java` process is up and names its cwd; never prints a command line.
- [ ] CC-02 A run reaches the world, writes `[cdevcapture] BEGIN` / `END` for every test, and the client exits by itself.
- [ ] CC-03 `FX-43.png` shows the moss and grass at the crosshair, no chat, no hand, no toast; `FX-43_hotbar.png`
      is the hotbar only.
- [ ] CC-04 `tooltip` + `auto`: the whole Legendary Tooltips frame is inside the crop, with no background margin
      over `pad`.
- [ ] CC-05 `shift` capture shows the shift-only tooltip lines (glycerol: "Tastes sweet!" once).
- [ ] CC-06 `jei_recipes` + `gui` crops to the JEI page, and `frames` gives cycled ingredients.
- [ ] CC-07 `use` on a placed pickling jar opens its GUI; `gui` crop; `hover` on a slot shows that slot's tooltip.
- [ ] CC-08 A failing `cmd` fails only its own test (`__failed.png`, manifest status, log lines) and the run goes on.
- [ ] CC-09 Killing the client mid-run: the runner marks that test `crashed`, relaunches, finishes the rest; the
      manifest has every test once.
- [ ] CC-10 `crop.py` alone after a spec crop edit rewrites the PNGs from `raw/` without the game.
- [ ] CC-11 Other windows over the game during the run don't show in any capture.
- [ ] CC-12 Nothing from the run lands in the instance's `mods/`, `logs/`, `saves/` or `config/`.

## 8. History

- 2026-10-24: designed (this file). Nothing built.
