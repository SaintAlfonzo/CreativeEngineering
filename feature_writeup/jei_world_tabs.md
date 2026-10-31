# JEI world-interaction tabs

JEI tabs for things players do in the world that no recipe describes: weathering, lava generation, stripping,
knife drops, mob bottling and so on. The candidate list is [jei_coverage.md](jei_coverage.md) section 4; this file
is the build.

## 0. At a glance

| Part | Status |
|---|---|
| Framework: one generic category, rows from resource files, KubeJS and Java providers (patcherMod `compat/jei/world/`) | BUILT 2026-10-22, seen in game. Text pass, arrow, effect and Nothing slots, jar colours: BUILT 2026-10-22 (patcherMod 1.1.01 rebuilt in place), not in `mods/` |
| 19 tabs (section 2) | BUILT 2026-10-22, rows generated or hand-written; not seen in game |
| `/dumpworldjei` (patcherMod `devtools/WorldJeiDump`, in `/dumpall`) | BUILT; run 2026-10-26 (`tool_pairs.json` and `mob_buckets.json` are real dumps, no `_fixture`), tool tabs regenerated from it the same day |
| Moobloom Conversion category (More Mooblooms 1.1.0, rebuilt in place) | BUILT 2026-10-22, in `mods/` (`moreMooblooms-1.1.0`, 2026-10-27 22:50), not seen in game |
| Tab tweaks ([plan_jei_tab_tweaks.md](plan_jei_tab_tweaks.md)): blocks and mobs drawn in slots, notes as one paragraph, one Jar Storage / Jar Fluids row per potion, jar + faucet icon, regenerated tool tabs, "Cut" wording, Lava Interactions, concrete 30 s | BUILT 2026-10-26, shipped in 1.1.04 (client 2026-10-29; server still 1.1.03), not seen in game. Tests JW-19 to JW-27 |
| Grape Planting category (Backported Vinery 1.5.5-ce-19.2-04, rebuilt in place) | BUILT 2026-10-22, seen in game; slow/fast text rebuilt 2026-10-22, in `mods/` since (Vinery `-06` installed 2026-10-31 00:41). TODO `alcohol/vineryport #23` |

**Before a release:** run `/dumpworldjei` then `/cegen run jei_world` (or `python3 tools/gen_jei_world.py`), so
the fixture rows are replaced. The generator exits 1 and prints a WARNING while the fixture or a dump is missing.

## 1. How it works

- **Tabs:** `WorldTab` (id, icon candidates, catalysts). Title lang key `creativeengineering.jei.world.<id>`. One class,
  `WorldInteractionCategory`, draws every tab: up to 3 inputs, an optional action slot over the arrow (tool, held item,
  trigger stand-in such as a lightning rod), Feast Plating's arrow, up to 6 outputs (chance under the slot and in the
  tooltip), 3 note lines. A tab with no rows doesn't show.
- **Notes:** joined into one paragraph (a note with no end punctuation gets a full stop), 160 px wide, 3 lines, then
  cut; hovering the note area of a cut row shows the full text. Since 2026-10-26 no generated or manual row runs past 3
  lines (estimated with the font's glyph widths); the Java-provider rows (Burning, Campfire) are not measured. Write them in the
  changelog style (section 3.6 of [changelog.md](changelog.md)): terse facts, the tool is already in the action slot
  so don't repeat it ("Right-click: washes the dye out.", not "Right-click with soap to ..."), aim for 2 lines.
- **Drawn slots (`WorldSlotRenderer`, 2026-10-26):** an entity slot (its spawn egg, or a named name tag) draws the mob
  itself, feet on the slot floor, turning; the stack stays the egg, so lookups and the tooltip are unchanged. A mob that
  can't be created on the client or throws while drawing falls back to the egg for the session (log `[jei_world] can't
  draw`); MCA mobs always do. An itemless block (frosted ice, candle cakes, crops, Create's dyed sails, tall seagrass) is
  a Barrier named after the block and draws the block's model (crops at their highest age); a block with no model stays
  the Barrier. Fire is flint and steel, a bubble column a water bucket. Both are marked by NBT keys
  `CreativeEngineeringJeiEntity` / `CreativeEngineeringJeiBlock` set in `WorldRow`.
- **Per-potion rows (`PotionRows`, 2026-10-26):** the generator writes the potion soft fluid's jar rows once with
  `"per_potion": true`; at JEI start each becomes one row per potion (water, awkward, thick, mundane and effectless
  potions left out): bottles with the potion (normal, splash, lingering, cycling together with the jar of that bottle
  kind), the jar with Moonlight's `NBT:{Potion, Bottle}` and the potion's colour, Create's potion fluid with its tag.
  Row format addition: `{"stacks": [{"item", "nbt"}, ...]}` (each stack its own NBT); `"nbt"` also applies to a fluid.
- **Drawn-only slots:** air outputs are an empty slot, tooltip "Nothing" (was a named barrier); `{"effect": id}` draws
  the mob effect's icon (Campfire Rules' Comfort). Both are drawn by the category, so JEI can't look them up.
- **Jars:** filled jar icons carry `CachedColor` in `FluidHolder` (the soft fluid's `color`, -1 for none or
  `no_tint`); Supplementaries' jar item renderer reads the colour only from there, and without it the liquid was black.
- **Row format:** JSON, documented in `WorldRow.java`. Ingredients are items, item tags, blocks, block tags, fluids or
  entities (shown as spawn eggs, a named name tag when there is none). Rows for missing mods drop out (`requires`, or
  ids that don't resolve).
- **Where rows come from**, merged per tab (`WorldInteractionJEI`):
  1. `kubejs/assets/creativeengineering/jei_world/<tab>/generated.json`, written by `tools/gen_jei_world.py` from the
     mod jars' data (kubejs/data on top), the loot dump and `/dumpworldjei`. One module per group in `tools/jei_world/`.
  2. `.../<tab>/manual.json`, hand-written, for hardcoded mod behaviour (each row checked in the mod jar).
  3. `WorldRows`: `add` from KubeJS startup (the sample syringe, from `global.sampleSyringe` in
     `startup_scripts/func/sample_syringe.js`) and Java providers read when JEI starts (`BurnRows`, `CampfireRows`).
  The files are client resources, so the tabs work on a dedicated server too.
- **Apply a change:** generator or manual.json edit -> F3+T and rejoin; KubeJS startup or Java -> restart.

## 2. Tabs

| Tab | Source | Rows (2026-10-22) |
|---|---|---|
| Weathering and Growth | Immersive Weathering `block_growths` + its tag-paired built-in rules (`tab_weathering.py`) | 82 |
| Lightning Conversion | IW lightning growths, copper de-oxidising | 9 |
| Lava Interactions (was Lava Generation) | IW `fluid_generators`, Create and Dreams and Desires fluid interactions, vanilla | 25 |
| Concrete Hardening | IE liquid concrete (manual); sets after 30 s (patcherMod `IEConcreteSetTimeMixin`, was 32) | 2 |
| Salt Dissolving and Melting | Salt mod (manual, tags) | 7 |
| Stripping | `/dumpworldjei` axe strip pairs, IW bark (`tab_tools.py`); one row per log pair | 85 (2026-10-26) |
| Waxing and Scraping | honeycomb, axe wax-off and scrape, IW rust, everythingcopper, steel wool | 503 (2026-10-26) |
| Block Dyeing | Supplementaries dye-on-block colour sets, Create sails, seats, valve handles | 2177 (2026-10-26) |
| Soap Washing | Supplementaries soap on blocks, sheep, collars, signs; one row per target, sources cycle | 347 (2026-10-26) |
| Jar Storage | soft fluids (jars + the pack's own), cookie families, mob capture (`tab_drops.py`); potions one row each (`PotionRows`); icon: jar with a small faucet | 143 + potions |
| Jar Fluids | pouring from jars, Supplementaries faucet sources; potions one row each | 132 + potions |
| Knife and Machete Drops | active global loot modifiers needing a knife or machete | 40 |
| Grass Drops | grass and ground-plant loot tables, IE hemp seeds, straw | 24 |
| Sample Syringe | KubeJS, one row per filled syringe | 7 |
| Mob Bottling and Bucketing | `/dumpworldjei` Bucketable mobs (`tab_mobs.py`) + Buzzier Bees bottles, duck sack, Quark, UA squid | 7 + dump |
| Mob Transformations | manual: vanilla, Supplementaries horses, Survivality, Alex's Mobs, Quark, MCA | 23 |
| Burning | `ItemBurnTransform` replace rules (no explosions), fireproof tags, Supplementaries ash | about 7 |
| Campfire Rules | `CampfireRain` + CEConfig + tags, Unlit Campfire (pack default config), Tinted Campfires | up to 10 |
| Budding Blocks | amethyst, AE2 certus, Geode Forge, Quark corundum (`tab_budding.py`) | 64 |
| Moobloom Conversion (More Mooblooms) | variants' feed items and weights, `feedChance` | 1 per variant |
| Grape Planting (Backported Vinery) | grape types: bush or vine, stem or lattice, soil, yield | 16 |

Left out on purpose: IW frost growths with chance 0; Salt cluster growth and evaporation (Salt's own pages); Create
pipe-spill collisions; Farmer's Delight bark (cutting board only); candle-skull washing; modded fish buckets in jars
(not in data); Delightful features turned off in its config; Unlit Campfire's magma cream rule (campfires burn forever).

## 3. Known limits and findings

- Unlit Campfire's config is server-side; `CampfireRows` reads `defaultconfigs/unlitcampfire-server.toml`, so an
  edited world config shows the pack default.
- **Mod behaviour found while building** (shown as it really behaves, not fixed): Salt's melting places water only in
  ultra-warm dimensions, elsewhere the block becomes air (its config comment says water); two IW growths use
  `sky_access`, not a valid tick source; Backported Vinery: shearing a ripe stem drops seed and grapes but only steps
  the stem back one stage, so repeated shearing may duplicate seeds (1.3.10 emptied it). TODO rows open: `jei/jei-coverage #6`
  (salt, IW), `alcohol/vineryport #24` (stem shearing). The salt rework in 1.1.04 (salt no longer melts ice or snow when
  used or dispensed) may change the salt half: recheck before fixing.
- Unverified details (check in game): concrete now sets after 30 s (32 s before the mixin, by the javap reading); DnD milkshake-lava pairing order and chances;
  powder snow per faucet pull; bed and framed-sign soap behaviour; whether Create seats and valve handles use the dye;
  MCA zombie-kill conversion path.
- The axe dump simulates on the world-bottom block under spawn with a fake player; doors skip the Forge event because
  everythingcopper's listener writes the other half even when simulating.

### Tab tweaks (built 2026-10-26, [plan_jei_tab_tweaks.md](plan_jei_tab_tweaks.md))

Briefs: [briefs/jei_jei-coverage.md](briefs/jei_jei-coverage.md). TODO rows `jei/jei-coverage #7`, `#10`, `#11`,
`#16` to `#27`, `mobs/mooblooms #6`, `ui/effects #9`.

- Tool tabs regenerated from the real `/dumpworldjei` dump (`#16`, `#17`, `#18`): Stripping one row per log pair; Soap
  Washing keeps one row per target with the sources cycling (soap already takes off copper oxidation, the `unoxidise`
  pairs); Block Dyeing 2177 rows (load cost not measured yet, JW-21; fallback: one row per colour set with the dye
  cycling). Create's dyed sails were Barriers because they have no item: they now draw as blocks (`#18`, `#19`).
- Itemless blocks draw their model (`#19`); the Budding "Drops nothing" output is the "Nothing" slot. Deviation from the
  plan: the ice / cake stand-ins and dropping the crop rows were replaced by drawing the real block, which also fixes the
  sails.
- Notes joined into one paragraph (`#20`, `#11`): nothing is cut any more. The 3-line rows were left as they are.
- Lava Generation is titled Lava Interactions (`#21`); no basalt + water row (no such rule).
- Mobs drawn instead of spawn eggs in every world tab (`#22`, owner 2026-10-26: every tab, egg fallback).
- Potions in jars one row per potion (`#23`, folds in `#10`).
- "Cut with a knife / shears" (`#26`); concrete 30 s (`#27`); the Survivality row stays (`#28`, owner 2026-10-26).
- Jar Storage icon: jar with a small faucet (`#25`, Create's two-item icon; the jar alone without Create). Jar Fluids
  keeps the faucet.

## 4. Tests

Install patcherMod 1.1.01, moreMooblooms-1.1.0 and backportedVinery-1.5.5-ce-19.2-04 (all rebuilt 2026-10-22) first.

- [ ] **JW-01 Dump:** `/dumpworldjei` -> "2/2 parts"; `kubejs/exported/world_jei/tool_pairs.json` has no `_fixture` and
  `"errors": 0`; no door or block appeared at the world bottom under spawn. Then `/cegen run jei_world`: no WARNING
  lines; F3+T and rejoin.
- [ ] **JW-02 Log:** `latest.log` has `[jei_world] N rows in 19 tabs` and no `[jei_world]` warnings, and no
  `sample_syringe: JEI world rows not added`.
- [ ] **JW-03 Weathering:** uses of Brain Coral Block -> Weathering and Growth page: fan 77%, coral 21%, wet sponge 2%,
  Warm Ocean in the input tooltip. Uses of Oak Leaves -> leaf-pile row cycling leaves and piles together.
- [ ] **JW-04 Lightning:** uses of Lightning Rod -> Lightning Conversion: four copper rows, sand -> vitrified sand with
  40% fulgurite.
- [ ] **JW-05 Lava:** uses of Lava Bucket -> Lava Generation: andesite (water beside plus diorite), Create honey ->
  limestone, a DnD milkshake row.
- [ ] **JW-06 Concrete and Salt:** recipes for IE Concrete -> 2 Concrete Hardening rows. Uses of Salt Block -> 7 Salt rows,
  the melted-away output an empty slot whose tooltip reads "Nothing" (no barrier).
- [ ] **JW-07 Stripping:** uses of Oak Log -> Stripping: Stripped Oak Log plus Oak Bark.
- [ ] **JW-08 Waxing:** recipes for Exposed Cut Iron -> steel wool only, no axe; Weathered Cut Iron -> axe and steel
  wool. Uses of Honeycomb -> copper, Create copper and IW iron waxing.
- [ ] **JW-09 Dyeing and soap:** uses of Red Dye -> Red Wool / Glass / Terracotta pages, no sack or shulker box page, one
  Create sail page and one seat page. Uses of Soap -> terracotta from coloured terracotta (no glazed), sticky piston ->
  piston.
- [ ] **JW-10 Jars:** uses of Honey Bottle -> Jar Storage "1 of 12" with honey 250 mB, and Jar Fluids pouring a honey jar
  into a glass bottle. Filled jars draw their liquid in colour, not black: water blue, honey golden, lava orange. Uses of Cookie -> the cookie family row. Uses of Faucet -> beehive
  and water cauldron rows.
- [ ] **JW-11 Drops:** Knife and Machete Drops for a pig -> ham 50%, not burning, looting note, knife and machete. Uses of
  Grass -> 3 Grass Drops rows (hand: seeds 12.5%, hemp 10%; knife: straw 20%; shears: grass).
- [ ] **JW-12 Mobs:** Sample Syringe tab 7 rows, the cow row cycles cow and moobloom eggs. Mob Bottling: axolotl and
  Alex's Mobs buckets, stradpole with a lava bucket. Mob Transformations: the creeper output shows Charged.
- [ ] **JW-13 Burning and campfires:** Burning: glass -> dirty glass 100%, wood -> ash 25%, the ash row. Campfire Rules:
  the rain row says 8 s, the coal block row says it stays lit. The comfort row's output is the Comfort effect icon (no
  name tag), tooltip "Comfort" plus the seconds; the shovel row reads "Right-click with a shovel: puts it out."
- [ ] **JW-14 Budding:** AE2 flawless budding with Silk Touch shows the flawed block; the Geode Forge quartz row cycles
  basalt and blackstone.
- [ ] **JW-15 Moobloom Conversion:** uses of Dandelion -> moobloom 90% / sunbloom 10% shares in the hover.
- [ ] **JW-16 Grape Planting:** U on Red Grape Seeds opens Grape Planting; taiga page shows the soil line; jungle pages
  show the vine and lattice. Bush pages read "Plantable on any solid block, grows slowly at light level 10 or more",
  stem pages "grows quickly at light level 9 or more"; no percentages, no line cut off.
- [ ] **JW-18 Notes and arrow:** every world tab uses Feast Plating's arrow. Page through Stripping (oak log with bark),
  Soap Washing (wolf), Jar Fluids (faucet rows), Mob Transformations (zombie villager, MCA villager), Knife and Machete
  Drops (a pig), Grass Drops, Lightning, Lava Generation, Block Dyeing (Create sail): no note runs past its third line.
  If one does, hovering the notes shows the whole text.
- [ ] **JW-17 Dedicated server:** join CreEngServer: the tabs show the same rows as single player.

Tab tweaks (2026-10-26; install the patcherMod jar built 2026-10-26 first):

- [ ] **JW-19 Stripping:** uses of Oak Log -> Stripping: Oak Log -> Stripped Oak Log + Oak Bark on its own page; paging
  shows one log type per page, nothing cycles.
- [ ] **JW-20 Soap:** uses of Soap -> a "washes the dye out" glass row cycling the colours; an oxidised copper row
  ("takes off wax and all oxidation").
- [ ] **JW-21 Block Dyeing:** note the world-join time against before (latest.log `[jei_world] N rows`); uses of Red Dye ->
  a Create sail page whose input cycles the dyed sails drawn as sails (not Barriers). If JEI start is more than about
  2 s slower, switch `_dyeing` to one row per colour set.
- [ ] **JW-22 Blocks drawn:** page Weathering and Growth (frosted ice row), Lava Interactions (bubble column: water
  bucket), Knife and Machete Drops (a candle cake draws as the cake with its candle), Grass Drops (tall seagrass): no
  Barrier icon anywhere; hovering names the block. Budding Blocks: the "drops nothing" row has an empty slot, tooltip
  "Nothing".
- [ ] **JW-23 Mobs drawn:** Mob Transformations, Mob Bottling and Jar Storage mob capture draw the mobs turning in their
  slots; hover still names the mob and R/U on the slot opens the egg's pages. An MCA villager slot shows the egg; no
  `[jei_world] can't draw` errors for vanilla mobs.
- [ ] **JW-24 Potions in jars:** uses of a Healing potion -> a Jar Storage row with Healing bottles (normal / splash /
  lingering cycling with the jar), the jar drawn red, Create potion fluid tooltip names Healing; Jar Fluids has a
  Healing row; no "Any potion" or Swiftness row left.
- [ ] **JW-25 Jar icon:** Jar Storage shows the jar with a small faucet in the category list and tab header.
- [ ] **JW-26 Text:** Knife and Machete Drops on a cheese wheel reads "Cut with a knife; drops itself otherwise."; Grass
  Drops "Cut with shears."; the lava tab is titled Lava Interactions; the Burning notes fit (no cut text); joined notes
  read as sentences (a full stop between notes).
- [ ] **JW-27 Concrete:** place IE liquid concrete on flat ground and time it: it sets after about 30 s (was about
  32 s); the tab says 30 s.
