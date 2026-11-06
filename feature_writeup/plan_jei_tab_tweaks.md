# Plan: custom JEI tab tweaks (2026-10-26)

Status: **BUILT 2026-10-26** (owner confirmed "execute all"), not seen in game. patcherMod and moreMooblooms jars
installed since and shipped in 1.1.04 (client instance 2026-10-29; the server is on 1.1.03). World-tab data regenerated
(`python3 tools/gen_jei_world.py`, 3551 rows in 13 tabs). `jeed_providers.js` needs `/reload`. Tests: JW-19 to JW-27
([jei_world_tabs.md](jei_world_tabs.md) 4), JC-30, JC-31 ([jei_coverage.md](jei_coverage.md) 7), MB-105, EF-21; queued as
CQ-JT-01 to CQ-JT-09 in [testing/scratch_queue.md](testing/scratch_queue.md).

Deviations from the plan below:
- B1: instead of ice / cake stand-ins and dropping the crop rows, itemless blocks draw their real model (one renderer
  with B8), which also turned out to be why Create's dyed sails were missing from Block Dyeing (A3: no data change).
- A7 / B2: notes are joined into one paragraph in the category for every tab, which fixes the Burning cut-off and every
  other cut row at once; no per-row rewrite was needed (3-line rows left as written).
- B5: only Jar Storage gets the jar + faucet icon; Jar Fluids keeps its faucet icon (it never showed an empty jar).
- B7: the curve applies to every mod's JEI tanks (one mixin on JEI's `FluidTankRenderer`); a `LINEAR` option keeps
  JEI's own.
- C2: cause found and fixed in the server script (bundles need a `Uses` count to draw).
- C3: not fixed. A code and log investigation found nothing specific to the flower crown; the only heavy JEI event on
  record (2026-10-22) is the whole crafting category (16141 recipes) being opened and craftable-first sorted twice over a
  crafting menu. A change to the craftable-first cache was considered and not made: its menu check is by identity, so
  the second build likely comes from JEI's first call having no menu, which a container-id check would not stop.
  Measurement queued: CQ-JT-10.

Scope: the open [TODO](todo/TODO.md) `jei/jei-coverage` rows that tweak tabs and categories the pack already built,
plus `mobs/mooblooms #6` and `ui/effects #9`. Rows that add new coverage (`jei/jei-coverage #2`, `#6`, `#9`, `#14`,
`#15`) are out of scope. Per-row detail (file and line references, gotchas) is in
[briefs/jei_jei-coverage.md](briefs/jei_jei-coverage.md); this file is the execution order and the decisions.

Decisions (owner 2026-10-26):
- `#28` Survivality row: **keep** (accurate while the jar is in `mods/`, drops out by `requires` if it goes). Close `#28` as not needed.
- `#7` fluid amounts: **configurable, log or square root, log by default**.
- `#22` mobs: **render mobs in every world tab** that shows a spawn egg, falling back to the egg when a mob can't render safely.
- Defaults taken from the briefs (say if any is wrong): Stripping one row per log pair; Soap Washing keeps cycling;
  no basalt + water row; jar icon = jar item with the faucet drawn beside it; bubble column stand-in = water bucket.

Jars installed since; shipped in 1.1.04 (client 2026-10-29). CQ-JT-01 to CQ-JT-10 not run.

---

## Part A: generator and data

Files: `tools/jei_world/*.py`, `kubejs/assets/creativecommon/jei_world/<tab>/manual.json`, the lang overlay.
Applied by `python3 tools/gen_jei_world.py`, then F3+T in game. No jar.

| Step | TODO | Change |
|---|---|---|
| A1 | `#16` | Regenerate from the real `/dumpworldjei` dump (fixture rows go). Stripping: one row per log pair; check no `_group_by_target` merge reaches `_stripping`. |
| A2 | `#17` | Soap Washing regenerates; keeps cycling dyed glass and copper per target. Record in the TODO that soap already removes oxidation (114 `unoxidise` pairs). |
| A3 | `#18` | Block Dyeing regenerates (about 2,500 rows); add the undyed sail and `create:sail_frame` as cycling inputs in `dyeing/manual.json`. If the in-game load cost is over about 2 s, switch `_dyeing` to one row per colour set with the dye cycling (159 rows). The cost is measured in game (test JW-21). |
| A4 | `#19` | Budding Blocks: the `minecraft:barrier` "Drops nothing" output (`tab_budding.py`) becomes the drawn "Nothing" slot. |
| A5 | `#26` | `tab_drops.py` notes: "Cut with a knife.", cheese wheels "Cut with a knife; otherwise drops itself." shortened to fit, Grass Drops "Cut with shears.". |
| A6 | `#21` | Lang: "Lava Generation" becomes "Lava Interactions". No new basalt row. |
| A7 | `#11` | Text pass over every cut-off note (stripping, soap / wolf, mob transform, bucket and bottle, lightning, lava, knife and machete, jars, grass, dyeing) to the changelog style, 2 lines where possible. The arrow half of `#11` is already done (world tabs draw the Feast Plating arrow). |
| A8 | `#28` | No change (kept). |

## Part B: patcherMod Java

Files: `development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/compat/jei/` (and `world/`),
`CEConfig.java`, one new IE mixin. One `./gradlew :creativeCommon:build --offline`, plus a headless harness check of the
pure logic (stand-in map, fluid scale curve, note joining).

| Step | TODO | Change |
|---|---|---|
| B1 | `#19` | `WorldRow.blockStack`: stand-in map before the Barrier fallback: `frosted_ice`, `rare-ice:rare_ice` to Ice; `*_candle_cake` to Cake; `bubble_column` to Water Bucket; hover keeps the block name. Rows whose only block is a crop with no item (rice panicles, FD tomatoes, `stick_tomatoes`, attached hamimelon stem) are dropped. Rows that become identical after the swap are merged. |
| B2 | `#20` | `BurnRows`: one joined note string per row so the wrapped text stays within 3 lines. |
| B3 | `#23` (+ `#10`) | New Java provider (like `BurnRows`, `WorldRows.add` at JEI start): one Jar Storage row per registered potion (skip water, awkward, thick, mundane): bottle / splash / lingering with the potion, the potion fluid with its `Potion` tag, jar coloured by `PotionUtils.getColor`. The generic "Any potion" row is removed from `tab_drops.py` (part A regenerate). `#10` (black jar liquid) was fixed 2026-10-22 and is in `mods/`; checked again on these rows. |
| B4 | `#24` | Shared arrow helper (the Feast Plating `GUI` sprite 82,128 24x17) used by `RowPagesCategory` (Tipped Arrows, Potion Buckets) and `SeasoningCategory`; arrow and output x shifted to fit the wider sprite. |
| B5 | `#25` | Jar Storage and Jar Fluids category icon: a drawable of the jar item with the faucet item drawn small at its lower right (like the Canning icon). |
| B6 | `#27` | Mixin on IE `ConcreteFluid` (javap first; `require = 0`, IE guard like the pack's other IE mixins): set timer 31 to 30. `concrete_hardening/manual.json` note becomes "Sets after about 30 s". |
| B7 | `#7` | Config `[jei] fluidAmountScale = "log"` (`log` or `sqrt`). Mixin or wrapper on how fluid tanks in the pack's own categories and the world tabs draw their fill, so small amounts stay visible and 250 / 1000 / 8000 mB look distinct. Which JEI fluid renderers it can reach (the pack's categories only, or every mod's tank) is checked first and written in the writeup. |
| B8 | `#22` | A custom slot renderer for entity ingredients in `WorldInteractionCategory`: draws the living mob (scaled to the slot, slow turn) in every world tab that shows a spawn egg (Mob Transformations, Mob Bottling and Bucketing, Jar Storage mob capture). The slot's ingredient stays the spawn egg, so JEI lookups and the tooltip still work. Falls back to the egg when the entity can't be created on the client, throws while rendering, or is on a skip list (MCA villagers until shown safe). Entity drawing reused from More Mooblooms' JEI code. |

## Part C: other jars and investigations

| Step | TODO | Change |
|---|---|---|
| C1 | `mobs/mooblooms #6` | Moobloom Conversion tab: the sunflower renders facing backwards. Fix the flower's rotation in moreMooblooms' JEI renderer; rebuild `:moreMooblooms:build`. |
| C2 | `ui/effects #9` | Potion bundles missing from JEI effect provider pages: find why JEED's potion provider skips the bundle items (likely how the bundle stores its potion NBT, or the item isn't seen as a potion holder), then fix in `jeed_providers.js` (server script, `/reload`) or a patcherMod provider. |
| C3 | `jei/jei-coverage #8` | Flower crown uses freeze the game for about 20 s: find the cause (likely a recipe lookup over a huge ingredient set, possibly the pack's cycling or world-tab code) from a JFR / thread dump on a scratch client when allowed, otherwise from the code. Fix if it is the pack's code; otherwise report what causes it. |

---

## Done-means (same session)

- [jei_world_tabs.md](jei_world_tabs.md), [jei_coverage.md](jei_coverage.md), the brief, [mooblooms.md](mooblooms.md),
  [effects.md](effects.md) and [TODO](todo/TODO.md) updated; `#28` closed as kept, `#10` folded into `#23`.
- Tests: the briefs' proposed JW-21 to JW-25 and JC-30, plus new IDs for B6 to B8 and C1 to C3, in the writeups and
  [testing/scratch_queue.md](testing/scratch_queue.md) (no scratch server boot unless asked).
- Player changelog bullets (JEI family) in `kubejs/assets/creativecommon/changelog/unreleased.md`;
  `python3 tools/changelog_lint.py`.
- Hand over: test IDs and the jars waiting for `mods/` (patcherMod, moreMooblooms).
