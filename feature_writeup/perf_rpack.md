# Resource pack bench: what each resource pack costs (RP-)

Stress tests for the client's resource packs. An offline scan says what each pack adds (atlas pixels, animated
textures, model elements, dead files) and which blocks and entities it changes. A throwaway client then runs every
pack configuration in one launch: it swaps the selected packs in game, times the reload, builds a dense scene of
the pack's own blocks in the End void, and measures chunk rebuild time and frame times. A pack's cost is the
difference between "all packs" and "all packs but this one".

## 0. At a glance

| Part | Status |
|---|---|
| Offline scan `tools/perf/rpack/rp_scan.py` | BUILT 2026-10-29, run on the 18 enabled and 11 disabled packs (section 3) |
| In-game bench `rp_bench.js` + driver `rp_bench.py` (plan, setup, drive) | BUILT 2026-10-29; first full client run 2026-10-29 (23 configs, 0 errors; section 9), after three bench fixes (9.1) |
| Report `rp_report.py` | BUILT 2026-10-29, run on run 1 (section 9.2) |

Dev-only: nothing ships, no player changelog bullet. The bench script goes only into a throwaway client's
`kubejs/client_scripts/`, never the instance's.

## 1. Why a client bench

Resource packs are client-only, so the perf suite's dedicated server (perf_suite.md) never sees them. What a pack can
cost in this pack's setup (1.19.2, Forge, Rubidium, ModernFix, render distance 8, mipmaps 4):

- **Reload time** at launch and on F3+T: the pack's files are listed, read and parsed; its models are baked.
- **Block atlas size.** The user's latest.log has the block atlas at 8192x4096 with mip 4 (about 171 MB of VRAM).
  Sprites are packed into power-of-two sizes, so a few hundred extra pixels can double it to 8192x8192. Only the
  live run can tell (the stitcher's packing is not reproducible offline).
- **Mip level.** One stitched texture whose size has a low power-of-two factor (12x12, 24x16) lowers the mip level of
  the whole atlas ("Texture X with size WxH limits mip level" in latest.log).
- **Animated textures** upload their changed frame every tick it changes (every tick when interpolated).
- **Model complexity:** more elements means more quads to mesh (chunk rebuild time) and to draw (frame time).
- **Entity and block-entity textures** (chests, mobs, armor) cost per frame where they are drawn.
- **Dead weight:** files 1.19.2 never reads still get listed and unzipped.

## 2. How to run

Offline, any time (2-5 s):

```
python3 tools/perf/rpack/rp_scan.py [--all]     # perf/reports/rpack-scan.txt + .json; --all adds the disabled packs
python3 tools/perf/rpack/rp_bench.py plan       # configs, scenes and the time estimate, no game
python3 tools/perf/rpack/selftest/run_selftest.py   # chain check without the game (section 6)
```

Live (a client window opens on DISPLAY :1 for about an hour; only with the owner's go-ahead, and the one-game-JVM
rule holds: `eval_client.py launch` refuses while a game JVM is up):

```
G=<scratchpad>/rpclient; R=perf/reports/rpbench-1
python3 tools/perf/rpack/rp_bench.py setup $G [--world saves/WorldG]   # copies mods, kubejs, config, resourcepacks, the world
setsid nohup python3 tools/perf/rpack/rp_bench.py drive $G $R > $R.out 2>&1 &
python3 tools/perf/rpack/rp_report.py $R        # $R/rpack-report.txt
```

`run` = setup + drive. Options: `--packs fancy,lootr` (only those targets), `--builtin` (mod-provided optional packs
too, such as `quark:emote_resources`), `--repeat 2` (two leave-one-out passes, the second in reverse, for noise),
`--every K` (an "all" drift check every K configs, default 6), `--quick` (5 s samples, one rebuild),
`--no-scenes` (reload times only, about 25 min). Without `--world` the smallest world in `saves/` other than
Workspace is copied (the scenes are built in the End void, so any world works).

## 3. Offline scan

`rp_scan.py` compares each pack with what it replaces: the mod jars in `mods/`, vanilla's assets (Forge's
`libraries/net/minecraft/client/1.19.2-20220805.130853/client-...-extra.jar`; `downloads/1.19.2.jar` is a mod, not
vanilla) and the packs below it in options.txt order. Columns:

| Column | Meaning |
|---|---|
| atlas px | Block atlas pixels added over the texture replaced (frame size for animations). 1.19.2 stitches only textures a model uses: a texture counts when one of the pack's models uses it, or a mod / vanilla model the pack does not replace does |
| ups | Atlas textures larger than the one they replace |
| mip | Candidates that would lower the atlas mip level if stitched (the live run's latest.log is the check) |
| anim, anim px/s | Animated stitched textures; pixels uploaded per second beyond what the replaced texture uploaded |
| elems | Model elements added over the models replaced |
| dead | Files never read: shadowed by a higher pack, a namespace no mod has, OptiFine-only (`optifine/`, CTM, CIT, CEM, `.properties`), or outside `assets/` (1.20.2 overlay folders, `__MACOSX`) |
| blocks, ents | Ids the pack changes: texture -> mod models using it -> blockstates using those models, plus the pack's own blockstates; entities from `textures/entity/<name>` (checked in game) |

### 3.1 First scan, 2026-10-29 (18 enabled packs)

All enabled packs together add about 93,000 block atlas pixels (about 0.5 MB with mipmaps, under 0.3% of the
8192x4096 atlas) and about 93,000 animated pixels per second (a fraction of one 16x16 texture per frame each). On
paper no enabled pack is expensive; the live run checks whether that is enough to step the atlas size.

| Pack | Notable |
|---|---|
| Create Computers | 60,416 atlas px (largest), 7 animations (2 new, interpolated); 38 dead files outside `assets/` |
| Fancy Crops | 3 textures doubled to 32x32 (wheat stage 7, attached melon and pumpkin stems); +230 model elements (largest); 15 crop blocks |
| Create-Energistics | 11 stitched animations, made interpolated (20,480 px/s); 101 dead files for 4 mods not in the pack (ae2wtlib, arseng, expatternprovider, ae2insertexportcard) |
| Squirming Silverfish | 8 new animations on the infested blocks (40,960 px/s, the most) |
| Created Pretty Pipes, Create-style fluid pipes | mip candidates (fanblades 12x12, fluid pipe 24x16); the block atlas still gets mip 4, so not stitched |
| Engineers GUIs | 30 dead OptiFine files (401 KB) |
| Lower Shield | 11 dead `__MACOSX` files |

Disabled packs, if they were turned on: IE Reimmersed +92,092 atlas px with 7 upscaled textures; Modded Omelet
4,721 dead files; Clear Glass Pack: 924 of its 966 files are OptiFine CTM (dead), its 36 plain glass textures
apply; Fancy Beds: 112 files sit in a `26-2/` overlay meant for pack format 86 and up (dead here), its 32 bed
textures apply; Quark Programmer Art 72,704 animated px/s.

## 4. In-game bench

### 4.1 Run shape

`rp_bench.py setup` builds the client dir with `tools/perf/eval/eval_client.py setup` (mods hard-linked; kubejs,
config, resourcepacks and options.txt copied), removes the eval scripts, runs the scan, writes the plan as
`kubejs/client_scripts/zz_rpbench_plan.js` (`global.rpbenchPlan`) and copies `rp_bench.js` as `zz_rpbench.js`.
options.txt gets render distance 6, FOV 70, vsync off, frame cap 260 (uncapped), no pause on lost focus.
`drive` launches the client (JFR from JVM start, the user's -Xmx), clicks Singleplayer -> first world -> Play on the
854x480 window, waits for `[rpbench] done` in logs/kubejs/client.log, dumps the JFR and copies the logs.

Configs, in order: `all` (the user's selection) with every scene; `none` (only the kept packs: vanilla,
mod_resources, the generated packs, vinery_upstream_assets, incubation_additions) with every scene; one `-<pack>`
per target with that pack's scene and the control scene; an `all` drift check (control + vista) every 6 configs;
a final `all` with every scene. 23 configs and about 1 hour with the 18 enabled packs (the first reload sets the
real pace).

### 4.2 Scenes

Built on the integrated server in the End at x 500, z 0, y 100 (void between the main island and the outer islands;
the start checks the site is empty and logs `site`). A 48 x 48 smooth stone floor; the pack's blocks on every other
column and row, up to 6 states per block spread over its state list (so crops show several ages, pipes several
shapes), the list repeated to fill up to 3 layers. Set with flags 2 | 16 (no neighbour updates). Entities: up to 96
summoned frozen (NoAI, NoGravity, Silent, Invulnerable). Random ticks, fire spread, mob spawning, daylight and
weather are off. The camera is a spectator 14 blocks back and 18 up, pitch 40, GUI hidden.

- `pack:<name>`: what the scan found plus `tools/perf/rpack/scene_extra.json` (Lootr's chests, silverfish, an
  armor stand in iron armor for CE2 Iron Armor Fix). Packs with nothing placeable (GUI, sound, particle,
  first-person shield) get no scene; their reload is still measured.
- `control`: stone, oak planks, glass (dropped if a target retextures them), for drift.
- `union`: every target's blocks together.
- `vista`: the overworld at 0 140 0, yaw 45, pitch 30 (terrain, sky, the user's world).

### 4.3 Measurements

| Kind | How |
|---|---|
| reload | `PackRepository.setSelected` then `Minecraft.reloadResourcePacks()`; ms until the future is done (tick resolution) |
| atlas sizes, mip limits | latest.log `Created: WxHxM <atlas>-atlas` and `limits mip level` lines inside the reload's wall-clock window |
| rebuild | `levelRenderer.allChanged()` (Rubidium reloads its renderer) until `SodiumWorldRenderer.isTerrainRenderComplete()`; 2 runs |
| frames | `Minecraft.getFrameTimer()` ring buffer read every tick for 10 s after a 3 s warm-up: mean, p50, p95, p99, max ms |
| chunk memory | `SodiumWorldRenderer.getMemoryDebugStrings()` per scene |
| heap | the JFR recording (`jdk.GCHeapSummary`), by the reload's wall-clock window |

Each result is one `[rpbench] <kind> {json}` line in logs/kubejs/client.log (kinds: start, site, reload, built,
scene, config_end, timeout, error, done).

## 5. Report

`rp_report.py RES` writes `rpack-report.txt` and `.json`: errors and timeouts, unknown blocks or entities per scene;
every reload with its block atlas size and VRAM of all atlases; drift (control and vista in each `all`); all vs none
for every scene; and per pack: reload seconds added, frame mean, p99 and rebuild change in its own scene, block atlas
without it, the scan's columns, and a verdict. A cost is flagged only above the noise: reload above 2 sd of the `all`
reloads and 1 s; frame above 2 sd of the control drift and 3%; rebuild above 10%; any block atlas size change.

## 6. Offline selftest

`python3 tools/perf/rpack/selftest/run_selftest.py [DIR]`: scan, plan (`--quick`), then `rp_bench.js` under node
with a mocked client, integrated server, Block wrapper, frame timer and Sodium renderer (`selftest/js_harness.js`),
then a synthetic latest.log in which `-Fancy Crops v1.2` has a smaller block atlas, then the report. Checks: every
config reloaded, no errors, the atlas flag on Fancy Crops, no false frame flag. PASS 2026-10-29 (23 configs, 83 scene
results). Rhino differences from node are not covered; the live run is the test for those.

## 7. Limits and decisions

- One client launch, packs swapped in game: a relaunch per config would be about 2.5 min each and add launch noise.
  The in-game reload is what F3+T and the pack screen do; the first launch's reload is not split by pack.
- Render distance 6 and the End void keep the frame dominated by the scene; the vista scene covers the real world.
- Item textures, GUI, particles, sounds and first-person models are not measured in frames (no scene); their cost
  shows in reload time and the atlas only.
- Frame timing includes the bench's own per-tick script (the same in every config).
- Rubidium only for rebuilds: without it `rebuild_ms` is just the 3-tick wait (about 150) and means nothing.

## 8. Live test list (client queue CQ-RP-)

- [x] **RP-01 bench runs** (PASS 2026-10-29, run 1 attempt 2, after the 9.1 fixes): `rp_bench.py run <G> perf/reports/rpbench-1` -> `[rpbench] start` with `sodium: true`,
  `site` 0 non-air samples, every config has a `reload` and its `scene` lines, `done` with `failed: 0`; no
  `error` lines; `built` lists no unknown blocks other than ones the scan predicted from removed mods.
- [ ] **RP-02 report** (PARTIAL 2026-10-29: atlas and reload-delta checks PASS; drift 3.3% and reload sd 11% miss narrowly, 9.2): `rp_report.py` -> the control drift under 3%; `all` reload sd under 10% of the mean; every
  target has a reload delta; the block atlas size of `all` matches the user's latest.log (8192x4096).
- [x] **RP-03 findings recorded** (2026-10-29, 9.2; TODO `pack/server #33`, `#34`): the per-pack verdicts and the all vs none table copied into this file
  (section 9), with any pack that steps the atlas, lowers the mip level or costs over 3% of frame time listed as a
  TODO row for the owner.

## 9. Results

### 9.1 Run 1, 2026-10-29: run log

Results dir `perf/reports/rpbench-1`, client dir `<scratchpad>/rpclient`. Log of decisions and errors, written as
they happen:

- Before launch: the client JFR had `maxage=60m`, shorter than the run (launch + about an hour), so the start would
  have been dropped. `eval_client.launch` now takes `maxage` / `maxsize`; `drive` passes 240m / 4g. `drive` also
  launches with `nodebuglog` now (ATLauncher's debug log config logs everything and skews client timings; the mixin
  debug flags were already stripped by `launch_client.build_args`).
- 12:45 launched. The owner joined the world by hand before `drive` clicked; `drive` waits for "Resource reload
  finished" in latest.log (300 s timeout), which never matched in this launch, so its blind clicks would have
  landed in game. Decision: driver stopped at 12:50, the rest of the drive steps (wait, JFR dump, log copy, client
  stop) done by hand. TODO: `drive` should skip the clicks when `[rpbench] start` is already in client.log.
- 12:49 blocking bug: `s.getLevel(Level.END)` is ambiguous in Rhino (`MinecraftServer.getLevel(ResourceKey)` vs
  KubeJS's `kjs$getLevel(ResourceLocation)`); the site check failed and every scene build would have. Fixed:
  `rpbEnd(s)` picks the End from `getAllLevels()` by dimension id. Selftest PASS. A resource reload does not re-run
  client scripts, so the fixed script is restarted with `/kubejs reload client_scripts` (handlers are replaced,
  `RPB` starts over); the first run's lines before the restart are ignored.
- Superseded: the owner is not available to type the command, so the run restarts from a fresh client instead.
  For unattended runs `rp_bench.js` now opens `saves/PerfEval` itself from the title screen
  (`createWorldOpenFlows().loadLevel`, logs `title` and `opening`); `drive` waits for those lines in client.log and
  clicks only when the world did not open by itself. ("Resource reload finished" is a debug-level line, gone with
  `nodebuglog`, which is why the first drive never clicked.)
- 12:52 attempt 0 died: native SIGSEGV on the render thread (`RenderTarget.blitToScreen` -> `glDrawElements`, fault
  address 0x4) 14 s after the bench's first teleport to the End, right after Distant Horizons shut down its
  overworld LOD renderer and set up the End one ("Vertex Attribute (GL43+)" lines are DH's). Fits DH leaving GL
  state bound across its renderer setup; one occurrence, not reproduced. No hs_err in the instance, so not seen in
  the owner's play. Kept: `perf/reports/rpbench-1/attempt0/` (logs), hs_err + its JFR in the scratch client dir.
  Decision (owner unavailable): DH rendering and its LOD generator off in the bench client only
  (`client.quickEnableRendering` / `quickEnableWorldGenerator = false` in its DistantHorizons.toml). DH is the same in
  every config so it adds nothing to a pack's delta, and its LOD threads are frame-time noise; the cost is that
  `vista` is no longer the owner's exact view. TODO row added: check whether a player changing dimension with DH
  on can hit this crash.
- 12:58 attempt 1 launched (DH off). The script's title-screen open never fired: KubeJS 6 posts `ClientEvents.tick`
  only while `Minecraft.player` is set (checked in `KubeJSClientEventHandler.clientTick`), so a client script cannot
  act on the title screen. Removed again. `drive` now clicks 60 s after "Sound engine started", checks the click by
  "Starting integrated minecraft server" in latest.log, and retries once. New `drive --attach` takes over a client
  that is already up; used at 13:09 on attempt 1's client, which was waiting at the title screen.
- 13:12 blocking bug 2: `l.dimension()` fails in Rhino ("not a function, it is object": the name is taken by a
  property). Fixed: `rpbEnd` reads `String(l.dimension)` and accepts `minecraft:the_end` (KubeJS's
  `getDimension()`) or the ResourceKey form, and logs the strings it saw when it finds no End. Restarted the bench at
  13:13 by typing `/kubejs reload client_scripts` into the window with xdotool (works once a player exists); the
  second `start` came at 13:14 with no lines from the first run after it.
- 13:16 attempt 1 died the same way as attempt 0 (SIGSEGV in `blitToScreen` -> `glDrawElements`, address 0x4) 2 s
  after the teleport to the End, right after DH's "Vertex Attribute (GL43+)" lines, with DH rendering off in config:
  DH builds its GL objects on every level load regardless. 2 of 2 End entries crashed; the overworld join, which
  logged the same DH lines with the GUI shown, did not. Hypothesis: DH binds its own VAO / buffer on level load,
  Minecraft's cached bound-buffer state goes stale, and with the GUI hidden (the bench hides it before the
  teleport) the first draw after that is the screen blit, which then runs `glDrawElements` with no element buffer.
  Decision (owner unavailable): the DH jar is removed from the bench client's `mods/` (hard links, the instance is
  untouched). DH is the same in every config, so pack deltas are unaffected; `vista` has no LODs. The crash is
  `pack/server #33` in the TODO, with the F1 + dimension change repro to check.
- 13:19 attempt 2 launched: fresh client dir, DH jar removed (patcherMod's `DhCloudHeightMixin` is `@Pseudo`, so
  the missing target is fine). Attempts 0 and 1 kept in `perf/reports/rpbench-1/attempt0|1/` (logs), hs_err files
  and JFR repos in the scratchpad. 13:21 the driver's click opened the world.
- 13:22 `start`, `site` 0 non-air samples (the `rpbEnd` fix works). 13:27 config `all` done, 16 scenes, no
  errors, no crash on the End teleports (DH removed). Reload 48 s. Finding on the method: `rebuild_ms` is 100-101 in
  every End scene (vista 150): the wait is checked once per client tick, so 2 ticks is the floor and these small
  scenes rebuild inside it. The rebuild column cannot tell packs apart in this run; frames and reloads are
  unaffected. Not fixed mid-run (it would change the run); TODO for the next run: time the rebuild per frame
  (render-thread nanoTime) or use bigger scenes.
- 13:35 report tried on a snapshot after 2 configs: it runs. Fixed: packs whose `-pack` config has not run yet read
  "not run" instead of "no measurable cost" (a cut-short run would have read as all clear). Early numbers: block
  atlas 8192x4096 (215 MB all atlases) in both `all` and `none`; reload `all` 38.3 s, the switch to `none` 71.7 s
  (removing every pack reloaded slower than the full set: the reload cost depends on the change, not only the
  final set; compare with the `-pack` reloads).

### 9.2 Run 1 results (attempt 2, 13:19-14:00, DH removed)

`perf/reports/rpbench-1/rpack-report.txt`, the JFR report `perf/reports/rpbench-1-client.html` (whole run, 2449 s,
startup included). 23 of 23 configs, 83 scene samples, 0 errors, 0 timeouts, 78 of 78 scene builds with no missing
block or entity.

**Verdict: no enabled resource pack has a cost worth acting on.**

- **Atlases:** every atlas has the same size in all 23 configs, `none` included: block atlas 8192x4096x4, 215 MB for
  all atlases. The packs' pixels (scan: about 93,000) fit in the atlas's free space; no pack steps it or lowers its
  mip level. The only "limits mip level" line is `davebuildingmod:blocks/ascii_4_color` (7x9, from dbExtended), on
  every reload, and it hits a small 512x256 atlas (logged next to Upgrade Aquatic's glow squid atlas, which is
  built at mip 0), not the block atlas.
- **Reloads:** `all` 38.3, 30.3, 33.0, 30.5 s (mean 33.0, sd 3.7; the first is JIT warm-up). No pack's
  leave-one-out reload is outside about +-3 s. The outliers belong to the change, not to the pack: the switch to
  `none` took 71.7 s and the next one back (`-CE2 Iron Armor Fix`) 47.3 s, because a reload costs by how much changes;
  `-IE Breaker Switch Sound` 37.3 s (a sound pack change reloads the sound engine). JFR: the reload workers spend 75%
  in vanilla code and 13.5% in ModernFix's resource cache, with no pack code in the profile. Reload time comes from the
  mods' own assets, not the packs.
- **Frames:** control drift sd 3.3%, with a downward trend over the run (0.54 -> 0.50 ms), so a pack scene measured
  in the first and last `all` configs reads about 3% slow against the middle `-pack` configs. Every per-pack p99 is
  +6 to +26% for the same reason (the first config's p99 is warm-up). Adjusted for that, only one pack stays above
  noise:
  - **CE2 Iron Armor Fix**, flagged +7.5%: scene / control ratio 4.50 and 4.66 with the pack, 4.32 without, 4.35 in
    `none`, so about +5% (0.1 ms per frame) with 96 iron-armored armor stands in view. Its two textures are vanilla
    size (64x32), so the cause is not the texture; unexplained, negligible in play. TODO `pack/server #34`.
  - All vs none per scene: largest +4.8% (the same armor scene), vista +2.4%, everything else within +-3.2%.
- **Rebuild:** not usable in this run (9.1, 13:22: every End scene at the 2-tick floor).
- **RP-02 criteria:** atlas matches the owner's latest.log (PASS); every target has a reload delta (PASS); control
  drift 3.3% (target under 3%) and `all` reload sd 11% of the mean (target under 10%) miss narrowly, both from the
  first config's warm-up. Next run: a warm-up config (one reload + control scene) before the first measured `all`,
  and `--repeat 2`.

**Other findings from the JFR (whole run, client):**

- Render thread (`main`, 46.9% of samples): JEI 12.2%, its top method `MMCQ$VBox#avg` (the colour quantizer for
  JEI's colour search), which runs on each ingredient-list rebuild (every reload and world join).
  `jei-client.ini` has `ColorSearchMode = REQUIRE_PREFIX`; DISABLED would skip the colour pass at the cost of `^colour`
  search. Owner's call, not changed.
- CullThread (Entity Culling) 5.5% of all samples, 89% of it in `ArrayOcclusionCache#resetCache`.
- Server thread (integrated): modernfix 15.3%, alexsmobs 8.4%, creativeengineering 6.2% of its samples (an idle
  End / overworld bench world; not a play measurement).

### 9.3 Next run (not queued)

`rp_bench.py run <G> perf/reports/rpbench-2 --repeat 2` with the DH jar removed from the client dir after setup (or
a `--no-dh` option), after adding a warm-up config and a per-frame rebuild timer (9.1, 9.2). Worth doing only when a
pack is added or the owner turns one of the disabled packs on (section 3.1: IE Reimmersed +92,092 atlas px is the one
that could step the atlas).

## 10. File map

| File | Role |
|---|---|
| `tools/perf/rpack/rp_scan.py` | offline scan, writes `rpack-scan.txt` / `.json` |
| `tools/perf/rpack/rp_bench.py` | plan, client setup, drive (launch, click, wait, collect) |
| `tools/perf/rpack/rp_bench.js` | the in-game bench (scratch-only client script) |
| `tools/perf/rpack/scene_extra.json` | hand-kept scene additions and no-scene reasons |
| `tools/perf/rpack/rp_report.py` | report from a results dir |
| `tools/perf/rpack/selftest/` | `run_selftest.py`, `js_harness.js` (node mocks) |
| `tools/perf/eval/eval_client.py` | client dir setup and launch (reused) |

## 11. History

- 2026-10-29: built (scan, bench, driver, report, selftest). First scan: section 3.1.
- 2026-10-29: run 1 (9.1 log, 9.2 results): three bench fixes (End lookup twice in Rhino, unattended world open), JFR
  kept for the whole run, DH removed from the bench client after two render-thread crashes on End entry. No enabled
  pack costs anything worth acting on.
