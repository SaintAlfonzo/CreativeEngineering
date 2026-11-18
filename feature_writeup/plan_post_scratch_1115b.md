# Post-scratch plan 2: pass-through rework (written 2026-11-15)

<!-- generated:status_index begin open_work sha1=036c1eaf6b74 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 16 open TODO rows (Planned 5, In Progress 1, Testing 10), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `tooling/scratch-1115-follow-ups-2 #10` | Planned | S | In-place Compat toml save not reloaded |
| `tooling/scratch-1115-follow-ups-2 #11` | Planned | S | Pass-through bench web cube over budget |
| `tooling/scratch-1115-follow-ups-2 #12` | Planned | S | Rare Ice swallows salt dust and Salt Melt clicks on plain ice |
| `tooling/scratch-1115-follow-ups-2 #13` | Planned | S | Salt layer on melting ice drops as dust before the vanish roll |
| `tooling/scratch-1115-follow-ups-2 #16` | Planned | S | SQ-TX-18 second boot impossible: Devtools needs Compat |
| `tooling/scratch-1115-follow-ups-2 #7` | In Progress | S C | Broken config switches (timing, denseWalk, wthit passThrough) |
| `tooling/scratch-1115-follow-ups-2 #1` | Testing | S | Cobweb fall distance reset |
| `tooling/scratch-1115-follow-ups-2 #2` | Testing | S | Stopped break-group projectiles break as a fallback |
| `tooling/scratch-1115-follow-ups-2 #3` | Testing | S P | Neighbor model: 4 sides for crops, 6 faces for webs, dense 3 / 3, surrounded 6 |
| `tooling/scratch-1115-follow-ups-2 #4` | Testing | S P | Neighbor count cached per block; recount only on break or place |
| `tooling/scratch-1115-follow-ups-2 #5` | Testing | S C | Layer height blocks: leaf piles, ash, snow slow by height |
| `tooling/scratch-1115-follow-ups-2 #6` | Testing | S C | Solid threshold for leaf piles (default 2 stages below full) |
| `tooling/scratch-1115-follow-ups-2 #8` | Testing | S | Ground-level web: same-tick enter and land takes full fall damage |
| `tooling/scratch-1115-follow-ups-2 #9` | Testing | S | IW sand layer and Supplementaries fodder never slow |
| `tooling/scratch-1115-follow-ups-2 #14` | Testing | S | Arm: early loose part blocks the belt end depot |
| `tooling/scratch-1115-follow-ups-2 #15` | Testing | S | Upgrade Aquatic guard misses IW coral growth files |
<!-- generated:status_index end open_work -->

Source: owner notes of 2026-11-15 on the r1115 pass-through findings
([scratch_run_2026-11-15.md](testing/scratch_run_2026-11-15.md): SQ-PTH-03, -11, -25, -27, -28, CQ-PTH-02, -04) and
[plan_passthrough.md](plan_passthrough.md). Follows [plan_post_scratch_1115.md](plan_post_scratch_1115.md) (portals, salt,
hops); this plan is the Creative Compat pass-through owner's. All code is in
`development/creativeCompat/.../surface/` plus `CompatConfig.java`; one build, install and restart covers the plan.
Test rows: "SQ-PS15B" at the end of [scratch_queue.md](testing/scratch_queue.md) (to write with the build, not run).

**State 2026-11-15: A to E built** (Creative Compat 1.0.0, `development/creativeCompat/build/libs/creativecompat-1.0.0.jar`
15:42, not installed; headless `tools/test_passthrough.sh` ALL 292 OK). Open, waiting for owner: D0 dump (Devtools),
two D questions (section "Open"); D0 dump and E2's `/cdevreload compat` built in Devtools 15:50 (not run). Live checks: SQ-PS15B-01 to -12,
SQ-PTH-25c, -28c, CQ-PS15B-01, -02.

Order: A (bug fix, independent), B (projectile fallback, independent), C (neighbor model, the big change), D (layer
heights, builds on C's config shape), E (config switches). C changes the config tables, so D and E follow it.

## A. Cobweb fall distance reset (SQ-PTH-03)

Finding: a fall into a lone web at ground level reset fall distance to about 1 instead of scaling it (14.55 to 1.1 on
entering, 3 damage from the last 5 blocks). Expected about 0.85x for one web, about 0.61x for three stacked.

- Suspects, in order: `PassThrough.newCellsFallFactor` / `PassRule` `resetFall` applied to a non-dense cell
  (`denseResetsFall` must only act when the cell is dense); vanilla `Entity.makeStuckInBlock` zeroing fall distance
  before `MakeStuckInBlockMixin` cancels it.
- Fix and add a headless harness case: lone web scales, stacked webs compound, only a dense web (new threshold, C)
  resets. Live check: SQ-PTH-03 rerun with the same drop heights.
- **State: built.** Cause was vanilla's fast-fall clip in `Entity#move` (fixed in the scratch fixes of 2026-11-15,
  `PassThrough.fallResetClip`); `PassRule` resets only when dense, and `MakeStuckInBlockMixin` cancels at HEAD, so the
  second suspect never zeroes it. With C, dense = 3 of 6 faces. Harness (`PassRework1115bTest`): lone web x0.85,
  3 stacked x0.614 with no reset, 3-face web resets, 2-face web scales. Live: SQ-PS15B-01.

## B. Projectiles that stop mid-block break (SQ-PTH-11)

Finding: snowball and ender pearl in the `break` group stalled in an outer web cell (speed x0.35 per tick, 1.44 to 0.17
and on) and hung there, never breaking; the pearl never teleported.

- Rule: a `break` group projectile that has stopped (speed under a small epsilon for a few ticks, or an outer-cell stall
  of N ticks) runs its block impact at its current position, as `breakIn` does at the cell centre. This is a fallback; the
  existing break on reaching a surrounded cell stays.
- Applies to every projectile meant to stop inside a block and break (snowball, ender pearl, fireballs, wither skull,
  llama spit, shulker bullet), not to `hang` (arrows), `settle` (potions) or `exempt`.
- Config: the stall tick count, key `breakStallTicks` under `[surface.passthrough]` (default 3, 0 = off). Ender pearl:
  the teleport must run (impact at the stop position), then the pearl is discarded.
- Live check: SQ-PTH-11 rerun from z 995.5, 994.5 and 996.2.
- **State: built.** `PassProjectiles.stallBreaks` counts ticks in a row under 0.05 b/t (after the web slowdown) in any
  web, `PassState.stallTicks`; at `breakStallTicks` the impact runs as a block hit on the web at the projectile's own
  position (`breakIn(.., false)`), then the server discards it (the pearl's own `onHit` teleports first). Key synced
  (PassValues format 2). Harness: SQ-PTH-11 snowball breaks on web tick 6 (stalls on 4), 0 = off, moving resets the
  count, hovering pearl breaks on its 3rd stalled tick. Live: SQ-PS15B-02.

## C. Neighbor counting: fewer cells, cached per block

Owner decisions (2026-11-15). Today: crops count a ring of 8 (same layer), webs a cube of 26, dense at `denseFrom` 6,
surrounded at 26 of 26, recomputed on every evaluation (SQ-PTH-25: web cube 2029 ns against a 1000 ns budget; SQ-PTH-28:
+0.57 ms for 200 cows in corn, +3.17 ms for 500 items in webs).

### C1. What counts

| Tier shape | Counted cells | Max | Dense from | Surrounded at |
|---|---|---|---|---|
| Horizontal standard crop (dense_crop) | the 4 side-adjacent cells (N, S, E, W), same layer | 4 | **3** | none |
| Cobweb and barbed wire | the 6 face-adjacent cells | 6 | **3** (dense web) | **6** (surrounded web) |

- `PassTier.Neighbours` becomes `NONE`, `SIDES` (4) and `FACES` (6); `RING` and `CUBE` go. A config file holding the old
  names is read as the nearest new one (`RING` to `SIDES`, `CUBE` to `FACES`) by a one-time migration like
  `LeafPileMigration`, and the old `denseFrom` value is replaced by the new default when it equals the old default (6).
- New defaults: dense_crop `denseFrom` 3; cobweb `denseFrom` 3, `surrounded` at 6 (new key `surroundedAt`, default 6,
  clamped to the tier's max). `PassValues` sync format changes with it (version the packet or bump the sync id so a
  mismatched client falls back to vanilla instead of misreading).
- Surrounded now means all 6 faces, so a web with open air above or below is dense but not surrounded; update
  plan_passthrough.md 4.3 ("26 of 26") and the web tooltips and WTHIT "Neighbors: N" line (max shown changes).

### C2. Bake the count into the block, recheck only on change

- Each counted block keeps its neighbor count in a cache keyed by block position (per level), computed once on first
  use. It is **not** recomputed per tick.
- Invalidate (recount the block and its counted neighbors) only when a neighbor is **broken or planted/placed**:
  block place, block break, explosion and piston/fluid replacement events, and `neighborChanged` for tier blocks.
- **Auto-harvest and replant do not invalidate.** A crop reset to age 0 by harvest (Create harvester, farmer, right-click
  harvest) is a state change of the same block, not a break or a place; the hook must key on the block type changing,
  not on the state changing. Tier membership, not growth, is what counts.
- Growth rule vs neighbours: a freshly planted crop **counts as a neighbor** even below its growth stage. Slowdown on a
  block applies only if that block itself is at the growth level its tier asks for (`growth` HALF / TALL_HALF). So a
  young crop next to a grown one adds to the grown one's density but is not slowed itself.
- Storage: **saved in chunk data** (owner, 2026-11-15). A chunk capability on `LevelChunk`, attached with
  `AttachCapabilitiesEvent<LevelChunk>` (never a mixin on `CapabilityProvider`: see memory
  mixin-capabilityprovider-crash) and serialized into the chunk NBT. Sparse: one entry per counted block
  (packed chunk-local position to count, 3 bits), only for tier blocks of a tier with density, so field-sized chunks stay
  small. Entries for a block that is gone are dropped on the invalidating event and on save. A chunk with no entry for a
  tier block (older world, first load after the update) is counted once on first use and written back; the data is
  derivable, so a missing or stale entry is never wrong, only a recount. Config change to the counted shape (`SIDES` to
  `FACES`) bumps a data version stored with the entry set so old counts are discarded.
- Client: not synced and not saved; the client counts from its own loaded blocks into the same capability (unsaved) and
  invalidates on the same block-update packets (client side of place/break). Server and client must agree (plan_passthrough.md 1.4); test at a chunk border.
- Expected result: `tick_web_cube` and the field cases fall to a map lookup; target under the 1000 ns budget, and PT01
  and PT04 under +0.5 ms. Record the new numbers in blockrunner.md 3.10 and perf_suite.md 12.

Live checks: SQ-PS15B rows for dense crop 3 of 4, web 3 of 6 dense, web 6 of 6 surrounded and hanging, harvest and
replant leaving the cached count, break and plant updating it, a young crop adding density without being slowed;
re-run SQ-PTH-25 and -28.

**State C1: built.** `PassTier.Neighbours` NONE / SIDES / FACES (RING and CUBE stay as deprecated aliases only, so
Creative Devtools' bench still compiles; the config accepts the three new names); `surroundedAt` (default 6, clamped);
denseFrom 3 for every tier; `surface/PassConfigMigration` (runs after `LeafPileMigration`, before Forge loads the
file): per table without `surroundedAt`, RING -> SIDES, CUBE -> FACES, denseFrom 6 -> 3. Sync: message id 1 (an older
client ignores it and keeps its own values), first field the format version 2 (another version switches the system
off on that client). WTHIT `Neighbors: 3 of 4`; tooltips are WTHIT only (no item tooltip names a count).

**State C2: built.** `PassCountData` (fastutil map, packed chunk-local key, count + shape bit), `PassCounts` (chunk
capability `creativecompat:pass_counts` via `AttachCapabilitiesEvent<LevelChunk>`, saved on the server with the tag
hash as data version, entries of gone blocks left out on save; client copy unsaved), `PassChunkHolder` +
`mixin/LevelChunkPassMixin` (field on the chunk; RETURN of `setBlockState`, which covers place, break, explosion,
piston, fluid and the client's block update packets). Keyed on the **tier** at the position changing (harvest,
replant and growth keep counts; a same-tier swap too). Chunk load drops its edge entries and the loaded neighbors'
facing edges (client: the whole set). Deviation: a shape change (SIDES / FACES) is caught per entry (shape bit) rather
than by a set-wide version; same effect, no mass discard. The per-tick `PassCountCache` is no longer read by movement;
its `clear()` (the bench's cold cases) now makes every chunk recount. Harness: count data, keying, growth rule.
Perf numbers: not measured (SQ-PTH-25c, -28c).

## D. Layer height slowdown: every layered block

New block category **layer height blocks** (block tag `creativecompat:passthrough_layers`; the max layer is read from the
block's own layers property, not assumed to be 8). All layered blocks use one rule, replacing the leaf_pile tier's
`minLayers` / flat `walk` and `SurfaceTier.SNOW`'s layer handling.

### D1. Which blocks (found by scanning every blockstate in `mods/` for a layers-style property, 2026-11-15)

Updated 2026-11-17 for F1 and plan_post_scratch_1115.md G7.4 (built; was "in" for every row but the last two).

| Block | Layers | Treatment |
|---|---|---|
| Vanilla `minecraft:snow` (SnowLayerBlock; not in a jar, by class) | 1-8 | in: walk-through 1 to 7 (no collision, `snowMaxSlow` 0.4 at 7); 8 becomes a snow block; leather boots stand on top (BlockRunner `snow`) |
| Immersive Weathering leaf piles: oak, spruce, birch, jungle, acacia, dark oak, mangrove, azalea, flowering azalea | 0-8 | in, leaf layer: walk-through at every height, `leafMaxSlow` 0.6 at 8; on top BlockRunner `leaves` |
| Immersive Weathering `azalea_flower_pile` | 0-8 | in, leaf layer (as above) |
| Immersive Weathering `ash_layer_block` | 1-8 | out (F1): dust, not walk-through |
| Immersive Weathering `sand_layer_block`, `red_sand_layer_block` | 1-8 | out (F1): dust, not walk-through (closes the SQ-PS15B-10 finding: collision = outline, never inside) |
| Twigs `bamboo_leaves` | 1-4 | in, leaf layer: walk-through at every height, 0.6 at 4 (no override); on top BlockRunner `leaves` |
| Supplementaries `ash` | 1-8 | out (F1): dust |
| Supplementaries `fodder` | 1-8 | out (F1): soft, not walk-through |
| Immersive Engineering `sawdust` | 1-9 | out (F1): dust |
| Create Deco coinstacks (brass, cast iron, copper, gold, iron, netherite, zinc) | 1-8 | out (owner, 2026-11-15) |
| Control Engineering `logic_cabinet` (height 0-1), Copycats layers (own property) | n/a | out: not stacked-layer terrain |

This is a scan of blockstate keys; blocks defined only in code can be missed. Step D0 is the authoritative list: a
Devtools dump (or a `/cdevgen` generator) of every registered block with an integer property named `layers`, `layer`,
`height`, `snow_layers` or `thickness`, with min, max and collision height at each value, written to
`kubejs/exported/`, diffed against this table before the tag is built. Anything the dump finds that is not in the table
goes to the owner as a question.

### D2. Rule

- Let `max` be the block's max layer and `solidBelowFull` (config, default **2**) the number of stages below full at
  which the block stops being walk-through. Layers `>= max - solidBelowFull` act whole (6, 7, 8 of 8): no pass-through
  slowdown, normal collision and stand-on rules. **Highest walk-through layer `H = max - solidBelowFull - 1`** (5 of 8;
  sawdust, max 9: H = 6).
- Walk-through slowdown by height, one step per layer: 1 layer = none; from 2 layers the walk value falls in equal steps
  and reaches `layerMaxSlow` (default **0.5**) **at H, the highest walk-through layer**, not at full height (owner,
  2026-11-15). `walk(l) = 1 - (1 - layerMaxSlow) * (l - 1) / (H - 1)` for `2 <= l <= H`.
- **Per-block override** (owner, 2026-11-15): Twigs bamboo leaves are walk-through up to one layer below max (layers 1
  to 3 of 4, layer 4 acts whole; slowdown 0.5 at layer 3). Config list `[surface.passthrough.layers] solidBelowFullOverrides`,
  entries `blockid=value`, default `["twigs:bamboo_leaves=0"]` (0 = only the full block acts whole, so H = max - 1).
  Any other block can be added the same way.
- `solidBelowFull` 0 = only a full block acts whole; larger values make piles solid earlier; a value that leaves H under
  2 means the block never slows (acts as normal terrain). Clamped to each block's max.
- Config: `[surface.passthrough.layers]` with `layerMaxSlow`, `solidBelowFull`, `minLayers` (lower bound, default 2) and
  the old `leaf_pile` values migrated into it (`LeafPileMigration` extended). `depthStriderPasses` stays.
- Snow and ash: one rule for every layered block, so BlockRunner's snow tier keeps only the full snow block and powder
  snow. Confirm there is no double slowdown (BlockRunner `SNOW` 3+ layers today).
- Tooltip / WTHIT: the Run Speed line shows the height-derived value and the layer count.
- Live checks: walk through 1 to 8 layer piles of each family; 0.5 at 5 of 8 and not before; 6, 7, 8 act whole at the
  default; a threshold change applies after a config reload (E).

**State D: built, D0 open.** Tier `LAYERS` (key `layers`, tag `creativecompat:passthrough_layers`, 44 blocks in the tag
sim) replaces `leaf_pile`; `PassRule.layerWalk` / `highestWalkThrough`, `PassThrough.layerWalk` (max from the block's
own `layers` property), `PassValues.solidBelowFull(block)` with `solidBelowFullOverrides`. The layer table holds only
`layerMaxSlow`, `solidBelowFull`, `minLayers`, `solidBelowFullOverrides`, `fall`, `depthStriderPasses`.
`PassConfigMigration` drops `[surface.passthrough.leaf_pile]` and carries its edited values (minLayers unless 4, fall,
depthStriderPasses, walk as layerMaxSlow). BlockRunner: `runner.snow` is now snow block, powder snow, coarse snow
(blocktags.js); `SurfaceTier.SNOW` text updated. WTHIT: the walk line adds "3 of 8 layers"; a pile that acts whole
gets the Run Speed line. Exempt tag renamed `passthrough_exempt/layers` (foxes, bees). Harness: formula table, H per
max, overrides, thresholds, snow through `cellAt`. **D0: built 2026-11-15 15:50, not run** (Devtools build/libs, not
installed): `/cdevtest layerdump` writes `kubejs/exported/passthrough_layers_dump.json` (property, min, max, collision
and outline height per value) and classes each block `in_tag` / `d1_out` / `new` against the live tag and the D1 out
rows; queue row SQ-TX-17. Prediction from a blockstate scan of `mods/` (nested jars included): the same 25 blockstate
ids as D1 plus vanilla snow, so the dump is expected to settle only blocks whose property is invisible to a blockstate
scan: the five Copycats layer blocks (`copycat_layer`, `_half_layer`, `_slope_layer`, `_stacked_half_layer`,
`_vertical_half_layer`, code-defined states; D1 already says out) and any code-only block. Every `class=new` line goes
to the owner.

## E. Broken config switches

r1115 rows where a saved config value did not take effect: SQ-PTH-27 (`[debug.passthrough] timing = false` kept
logging), CQ-PTH-02 (server `denseWalk` saved while walking, speed unchanged), CQ-PTH-04 (client
`[surface.wthit] passThrough = false`, line stayed after 8 s). Scratch notes put these on the Forge config watcher
being parked by ModernFix's NightConfigWatchThrottler (live-edit gotcha in the scratch-test skill), but that has not been
shown to be the whole cause, and the owner reports the switches as broken.

- Step 1, investigate (report before code): for each switch, does the value take effect on a plain restart, does
  `ModConfigEvent.Reloading` reach `PassValues.onConfig` / `SurfaceSpeeds.onConfigReload` / the WTHIT and Debug readers,
  and does anything cache the raw value (a static final, a hook-time `ConfigValue.get()` snapshot, `PassSync` sending the
  stale table). Also test with ModernFix's throttler disabled to separate the two causes.
- Step 2, fix whatever step 1 finds in our code: read config through the reload event into plain fields, resend the
  synced tables on reload, make the debug and WTHIT switches read live. If the cause is only the throttler, add a command
  (`/cdevreload compat`, in Creative Devtools) that re-reads the config and fires the same path, and say so in the
  config comment; do not edit ModernFix's config.
- Also covers any switch the new keys add (`breakStallTicks`, `surroundedAt`, `layerMaxSlow`, `solidBelowFull`): each
  gets a reload test.
- Configs mirrored to `configs_edited/root_config/` for any hand-edited default.

**State E1 (investigation, 2026-11-15).** Plain restart: the values apply (r1115 boots 2 to 4 started with `timing =
false` and logged no timing lines). Our readers: `PassTiming.on` reads `[debug.passthrough] timing` every tick;
`WthitRunSpeed` reads `passThrough` on every overlay refresh; `PassValues.onConfig` drops the snapshot on Loading and
Reloading and re-sends it (`PassSync`); `SurfaceSpeeds.onConfigReload` re-pushes BlockRunner; no static copies. Forge's
`ConfigWatcher` calls `afterReload` (value caches) before firing Reloading. The cause is the watcher: night-config
3.6.7 `FileWatcher` acts only on `ENTRY_MODIFY` with a count of 1, so a save by rename (`sed -i`, atomic-save editors)
or two folded writes is never seen. ModernFix's `NightConfigWatchThrottler` (5.18.1) only parks the watcher 1 s per
pass (bytecode read); it delays, it does not block. Not tested with the throttler off (no scratch boots this round).

**State E2: built (Compat side).** `CompatConfigReload.reload()` re-reads the file, corrects it like Forge, resets
the caches and posts `ModConfigEvent.Reloading` on Compat's bus, so every reader acts as on a watched save. Config
comments ([surface.passthrough], [surface.wthit] passThrough, [debug.passthrough] timing) name the command. **Open,
waiting for the Devtools owner:** `/cdevreload compat` (spec in the round report). New keys covered: SQ-PS15B-12.
**E2 Devtools side built 2026-11-15 15:50 (not installed):** `/cdevreload compat` (server, op 3) and
`/cdevreloadclient compat` (client command) call `CompatConfigReload.reload()`; registered only with Compat loaded.
Rows SQ-TX-18, CQ-TXR-01; SQ-PS15B-12 can run on this build.

## Writeups and docs to update

plan_passthrough.md (sections 1, 2, 3.1, 4.3, 5, 7, 10: neighbor model, layer model, break fallback, perf numbers),
blockrunner.md 3.10, the pass-through tooltip text in `PassThroughFormat`, [TODO.md](todo/TODO.md) family
"Scratch 1115 Follow-ups 2". Player changelog bullets in
`kubejs/assets/creativecompat/changelog/unreleased.md` per CLAUDE.md under a pass-through family: web and crop
density counts (old values in brackets), layered blocks slow by height, projectiles that stop in a web break, falling
into a single cobweb no longer resets fall damage (past tense). Run `python3 tools/changelog_lint.py`.

## F. Layer decisions of 2026-11-16 (planned, not built)

Owner decisions (scratchpad.md "layers"; foxes and bees stay exempt from every layered block, keep). State: **planned,
not built.** Order: F1, F2, F3, F4, then update the D table, harness, SQ-PS15B-10/-11, changelog bullets, layers tooltip.

**State F: built 2026-11-17 (lane L1 of plan_exec_1117.md), as superseded by plan_post_scratch_1115.md G7.4.** F1 as
written; F2 replaced by G7.4 (no `solidBelowFull` at all: snow 1 to 7 walk-through, 8 a snow block; leaf layers every
height; keys `snowMaxSlow`, `leafMaxSlow`, `snowWalkThrough`, `snowFullBecomesBlock`); F3: G7.4's on-top speeds (snow
layers in `runner.snow`, leaf piles and bamboo leaves in `runner.leaves`; BlockRunner reads the block under the feet,
so no double slowdown); F4 done (D table, harness, SQ-PS15B-10/-11, changelog, WTHIT). Creative Compat build/libs,
not installed; headless ALL 292 OK.

- F1. Tag `creativecompat:passthrough_layers` holds only snow layers, IW leaf piles (azalea flower pile included) and
  Twigs bamboo leaves. Remove IW sand, red sand and ash layers, Supplementaries ash and fodder, IE sawdust (and salt
  layers): dust material, no walk-through, they stay in their BlockRunner tiers. Files: `passthrough_tags.js`, D table.
- F2. Leaf layers never act whole: no `solidBelowFull` for them (H = max). Snow keeps `solidBelowFull`. Drop the
  `twigs:bamboo_leaves=0` default override. Files: `PassRule`, `PassThrough.layerWalk`, `PassValues`, `CompatConfig`.
- F3. BlockRunner: bamboo leaves out of `runner.leaves` (layered tier only); snow layers stay out of `runner.snow` (no
  double slowdown); check each layered block's BlockRunner material (snow layers standing-on at 6 to 8 layers has no
  surface tier: open question). Files: `blocktags.js`.
- F4. Update D table (D1/D2), harness cases (leaf H = max, snow default, no overrides), queue rows, changelog (layer
  bullets), WTHIT layers text.
- Already edited this turn for F (not reverted): PassRule, PassThrough, PassValues, CompatConfig, PassTier (Compat
  `surface/`), `passthrough_tags.js`, `blocktags.js`, `PassThroughTest.java`. The Compat jar is not rebuilt; the harness
  has 1 known FAIL (overrides default) until F4.

The 2026-11-16 scratchpad write-up of the layer items not covered by F1 to F4 is
[plan_post_scratch_1115.md](plan_post_scratch_1115.md) G4. It also notes two conflicts with F2 and F3: snow layers in
`runner.snow`, and snow being walk-through without leather boots. Owner answer 2026-11-16 (G7.4, G7.5): separate on-top and
walk-through tiers; snow layers walk-through at 1 to 7, and layer 8 turns into a snow block (replaces F2's `solidBelowFull` for snow);
leaf layers walk-through at every height; dust layers not walk-through; every layer falls and stacks.

Open notes, owner TODO, not built (what each would touch):
- Full snow layer becomes powder snow: snow block placement / `SnowLayerBlock` mixin, Compat.
- IW drops Twigs bamboo leaves rarely, stacking to a full block: IW leaf-drop code, Compat mixin, new loot.
- Layered snow melts by layer: Creative Common `Melting` (snow melt by layer may already exist; check first).
- Powder-snow-like overlay and mild thorny damage in leaf layers: client overlay and `entityInside` in Compat.
- Seeing out from inside leaf layers: leaf layer model / render, client.

## Open (waiting for owner)

- D0: dump built (`/cdevtest layerdump`, SQ-TX-17); after the run, rule on each `class=new` block (expected: Copycats
  layers, already out in D1, plus any code-only block).
- Layered blocks still in a BlockRunner surface tier: IW sand, red sand and ash layers, Supplementaries ash, IE
  sawdust (`dust` 0.85), Supplementaries fodder (`soft` 0.88), Twigs bamboo leaves (`leaves` 0.78) get both the tier
  speed and the height slowdown at 3 to H layers. Take them out of their surface tiers like snow layers, or keep both?
  Settled (G7.4, built 2026-11-17): dust layers and fodder left the layer tier; snow and leaf layers keep a surface
  tier as their on-top speed only.
- Foxes and bees were exempt from leaf piles; the exemption is per tier, so it now covers snow, sand and ash layers
  too. Keep, or narrow it to leaf piles?
- E2: `/cdevreload compat` built in Creative Devtools 2026-11-15 (install with Compat).

## Done means

Headless harness cases for A, B (stall detection), C (count, invalidation keying, growth rule) and D (formula,
threshold); jars built and listed as waiting to be copied to `mods/` (Compat, plus Devtools if the reload command is
added); queue rows written; no scratch run until the owner says so.
