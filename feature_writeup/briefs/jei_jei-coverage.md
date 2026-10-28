# jei / jei-coverage: implementation brief

Written 2026-10-24 from [todo/TODO.md](../todo/TODO.md) `# jei`, Sub `jei-coverage` (rows 14 to 28, from the scratchpad
of 2026-10-24; rows 1 to 13 predate this brief). Layout per [README.md](README.md). Writeups:
[jei_world_tabs.md](../jei_world_tabs.md) (the world-interaction tabs, test prefix `JW-`, last id JW-18) and
[jei_coverage.md](../jei_coverage.md) (test prefix `JC-`, last id JC-27).

## Shared findings (read once, the sections below refer to them)

- **Machinery.** One category class draws every world tab: patcherMod `compat/jei/world/WorldInteractionCategory`
  (3 note lines, 160 px wide, hover shows the cut text; arrow is Feast Plating's `GUI` sprite 82,128,24x17). Tabs are
  the `WorldTab` enum (id, icon candidates, catalysts); the title is lang key `creativeengineering.jei.world.<id>`
  in patcherMod `lang/en_us.json` lines 8-26. Rows are JSON in `kubejs/assets/creativeengineering/jei_world/<tab>/`:
  `generated.json` (written by `tools/gen_jei_world.py`, modules in `tools/jei_world/tab_*.py`, run in game with
  `/cegen run jei_world`) and hand-written `manual.json`; Java providers `BurnRows` and `CampfireRows` add rows at JEI
  start. Row format is documented in `WorldRow.java`. Apply: generator or json edit -> F3+T and rejoin; Java -> restart.
- **The tool dump has been run.** `kubejs/exported/world_jei/tool_pairs.json` (725 KB, 2026-10-22 21:34) has no
  `"_fixture"` key and `"errors": 0`, but the `generated.json` files (21:20) were not regenerated after it: stripping
  still has 3 rows, soaping 4, dyeing 5, waxing 20. The TODO's "fixture 3 rows" counts are therefore stale; run
  `/cegen run jei_world` before judging rows 16 to 18.
- **Stand-ins.** An itemless block becomes a Barrier stack renamed to the block (`WorldRow.blockStack`, line 203;
  fire gets flint and steel); `WorldInteractionJEI.icon` falls back to a Barrier too.
- Rows in `jei_world` that cite another mod drop out when the mod's id does not resolve (`requires`).

---

## jei-coverage #14: No JEI recipe for soup cups
Status **Planned** / Work KubeJS / data / TODO `jei/jei-coverage #14`

### Goal
Every Miner's Delight soup cup is either reachable in JEI (how it is made, how it is filled or poured) or hidden.

### Now
- Installed `miners_delight-1.19.2-1.1.2.jar` has cup items (`beef_stew_cup`, `cave_soup_cup`, `noodle_soup_cup`, ...,
  `milk_cup`, `powder_snow_cup`, `copper_cup`) and only 25 recipes; the only cup recipes are `copper_cup`, Create
  filling / emptying for milk and water cups.
- Soup cups come from `data/miners_delight/cup_conversion/default_conversions.json` (bowl -> cup pairs, e.g.
  `farmersdelight:beef_stew` -> `miners_delight:beef_stew_cup`) read by the Java class `CupConversionReloadListener`:
  not a recipe type, so JEI cannot list it. What triggers the conversion (a copper pot with a cup as container, or a
  right-click) is unverified: read `CupConversionReloadListener` and its users first.
- The jar is the OLD one (`largecontent/mod-additions #7`: compare with 1.4.5, which adds `onion_soup_cup`,
  `bone_broth_cup`, `insect_stew` + cup; [miners_delight.md](../miners_delight.md) 10).

### Build
1. Decide with the user what a cup is for (see Decisions), after reading the trigger in the jar.
2. If kept: a JEI description on the cups (`kubejs/client_scripts/jei_info.js`, `e.addItem(...)`), or a world tab row
   (`jei_world/<tab>/manual.json`) per bowl -> cup pair, generated from the conversions json by a `tab_*.py` module.
3. If unused: add the cup keys to `global.item` (item_dict) and to `kubejs/startup_scripts/_lib/removed_items.js`.

### Decisions
- Keep (describe the conversion) or remove? Recommend: read the trigger first; if it is a plain right-click of the cup
  on a pot, keep and describe in one `jei_info.js` entry; the pack's soups-in-jars already cover bowl storage.

### Gotchas
Do not add recipes that Miner's Delight does not have: a fake JEI category must match real behaviour. Row `#7` of
`largecontent/mod-additions` (the jar upgrade) may change the whole cup list; do this after it.

### Tests (JC-)
- JC-28 (proposed): JEI uses of a beef stew cup shows the description or recipe; or the cup is absent from JEI and
  `latest.log` has no `REMOVED ITEM` recipe warnings for it.

---

## jei-coverage #15: Better JEI screens for Vintage Delight
Status **Planned** / Work patcherMod / KubeJS / TODO `jei/jei-coverage #15`

### Goal
List what JEI shows for Vintage Delight's machines and jar recipes today, then give each recipe type a usable page.

### Now
- The port `backportedVintage-0.1.6-ce-19.2-03.jar` ships `compat/JEIVintagePlugin` + `FermentingCategory` only: the
  Pickling (Fermenting) Jar. Its other recipe folders: `jam` (15), `cooking`, `cutting`, `create`,
  `immersiveengineering`, `fermenting` (24), `chef_hats`, `salt_lamps`.
- Mechanics with no JEI page ([vintage_delight.md](../vintage_delight.md) 2.3): salt growth and the Evaporator, the
  Cheese Mold (curds -> wheel on a random tick), mason jars, Lush Grass bone meal, Organic Mash. These are world
  interactions, the kind the world tabs cover.
- `jam` recipe type: which block or station uses it is unverified (read the jar's recipe serializer).

### Build
1. Dump first: after the Vintage jar is loaded, `/dumprecipes` and read `recipe_types.json` for `vintagedelight:*`; list
   each type with the page JEI shows (open JEI uses of one item per type).
2. Per type without a page: a `RowPagesCategory`-style category or a world-tab row. For the Cheese Mold and salt
   growth, hand-written `manual.json` rows in a new world tab (`WorldTab` entry + lang key + `jei_world/<id>/`).
3. Ties to `ports/vintageport #2` (the dump-driven steps), which runs `/dumpall` anyway.

### Decisions
Which recipe types deserve a tab. Recommend only those the player cannot guess (cheese mold, salt growth, jam type);
the fermenting category already exists.

### Tests (JC-)
- JC-29 (proposed): each listed Vintage recipe type has a JEI page; page text shows the container and processing time.

---

## jei-coverage #16: Stripping tab cycles through logs
Status **Testing** (BUILT 2026-10-26, [plan_jei_tab_tweaks.md](../plan_jei_tab_tweaks.md)) / Work patcherMod / tool / TODO `jei/jei-coverage #16`

### Goal
The Stripping tab shows its rows the way the user wants (see Decisions) instead of cycling through log types in one row.

### Now
- `tools/jei_world/tab_tools.py` `_stripping`: one row per pair in the dump's `strip` list (83 pairs, 81 targets), input
  `block(p['from'])`, output the stripped block, plus the Immersive Weathering bark. The three rows the user saw are the
  stale fixture rows in `jei_world/stripping/generated.json` (see Shared findings), so the "cycles" look may come from
  a different cause: a row whose input is a `block_tag`/`blocks` list cycles by design.
- Before regenerating, open JEI uses of Oak Log and note what actually cycles.

### Build
1. Run `/cegen run jei_world` with the real dump (no code change); check the tab again.
2. If a row still cycles several logs: in `_stripping`, key rows by `(to, bark)`, never merge inputs (each pair is
   already separate; a merge would come from `_group_by_target`, used only for soap).
3. If the user wants the opposite (one cycling row): group `from` by `to` through `_group_by_target` and use `blocks(...)`.

### Decisions
Ambiguous scratchpad note (one row per log, or one cycling row). Recommend one row per pair (81 pages is acceptable:
each page also shows its own bark drop and the page count is how the tab already works).

### Tests (JW-)
- JW-19 (proposed): Stripping tab after the regenerate: Oak Log -> Stripped Oak Log + Oak Bark on its own page; page
  count equals the dump's `strip` pairs.

---

## jei-coverage #17: Soap Washing tab cycles dyed glass and all copper
Status **Testing** (BUILT 2026-10-26, [plan_jei_tab_tweaks.md](../plan_jei_tab_tweaks.md)) / Work patcherMod / tool / TODO `jei/jei-coverage #17`

### Goal
Settle how the Soap Washing tab groups its rows and whether soap removes copper oxidation.

### Now
- `tab_tools.py` `_soaping` groups by target (`_group_by_target`): every dyed glass colour is the input list of one
  "-> glass" row and every copper variant the input list of one "-> copper block" row (input `blocks(froms)`, so the row
  cycles). The dump has 2,736 soap pairs: 2,400 `color`, 221 `name` (waxed to plain), 114 `unoxidise`, 1 `piston`.
- **The oxidation question is already answered by the data:** the `unoxidise` rule (`oxidized_copper`,
  `weathered_copper`, ... -> `copper_block`) is Supplementaries' soap and its note reads "Right-click: takes off wax and
  all oxidation." The generator already emits it (soaping/generated.json has the exposed copper row).
- `soap_blacklist` in the dump config: `minecraft:glazed_terracotta`.

### Build
1. Regenerate (`/cegen run jei_world`) and look at the tab.
2. Decide the grouping; if one row per colour is wanted, drop `_group_by_target` for `how == 'color'` only.
3. No code for oxidation: it already works in the data; if the user wants a different behaviour it is a Supplementaries
   config / mixin question (`supplementaries-common.toml` `functional.soap`).

### Decisions
Cycle (current, fewer pages) or split. Recommend keep cycling for dyed glass and copper (they differ only by input) and
close the oxidation question as "already true".

### Tests (JW-)
- JW-20 (proposed): Uses of Soap -> a "washes the dye out" glass row cycling all 16 colours; an oxidised copper row
  cycling exposed, weathered, oxidized and waxed variants.

---

## jei-coverage #18: Block Dyeing tab cycles every dye input
Status **Testing** (BUILT 2026-10-26, [plan_jei_tab_tweaks.md](../plan_jei_tab_tweaks.md)) / Work patcherMod / tool / TODO `jei/jei-coverage #18`

### Goal
Every dye input cycles in the Block Dyeing tab (not a subset); Create sails show up. Performance checked first.

### Now
- `tab_tools.py` `_dyeing`: for each of the dump's 159 colour sets and each of 16 colours one row (target colour block,
  sources `blocks(members, label='Any other colour')`, action tag `forge:dyes/<colour>`): up to about 2,500 rows when
  regenerated, against 5 rows in the stale `generated.json`; `manual.json` has 49 hand rows (Create sails, seats, valve
  handles: `CREATE_SELF_DYED` skips those sets in the generator, line 32).
- `manual.json` already holds `create:*_sail` rows, so "sails do not show" is not a missing row. Unverified cause: check
  the in-game tab after regenerating (the row may show only the colour target, not the undyed `create:white_sail` /
  `create:sail_frame` input).
- Pages cost: JEI builds a layout per row; the Moobloom and cycling-crafting code has timing logs (`[jei cycle] built in
  N ms`) to copy.

### Build
1. Regenerate, then time JEI start and the page flip on Block Dyeing (log `[jei_world] N rows in N tabs`).
2. If too slow: merge the 16 colours into rows with the dye as a cycling ingredient (`action: tag`), one row per
   colour set (159) instead of 16 per set.
3. Sails: add the undyed sail and `sail_frame` as cycling inputs in `jei_world/dyeing/manual.json`.

### Decisions
Whether the cost is acceptable. Recommend measure after the regenerate; fall back to step 2 above 2 s of added load.

### Tests (JW-)
- JW-21 (proposed): load time, Block Dyeing page count, Uses of Red Dye -> a Create sail page cycling the other colours.

---

## jei-coverage #19: World tab blocks render as barriers
Status **Testing** (BUILT 2026-10-26, [plan_jei_tab_tweaks.md](../plan_jei_tab_tweaks.md)) / Work patcherMod / TODO `jei/jei-coverage #19`

### Goal
No world tab shows a Barrier: each block gets a displayable stand-in, or its row is dropped.

### Now
- Cause (`WorldRow.blockStack`, line 189-206): a block with no item and no fluid becomes a renamed Barrier. The listed
  blocks have no item: `minecraft:frosted_ice` (in `block_growth/manual.json` ice melting row), `rare-ice:rare_ice`
  (in `#minecraft:ice`, which the "icicle" row uses as a `block_tag`; the Rare Ice jar's tag file adds it; loot drops
  `minecraft:ice` with Silk Touch), `minecraft:bubble_column` (lava_generation/generated.json), `*_candle_cake`
  (knife_drops/generated.json), `farmersdelight:rice_panicles` / the rice crop, `minecraft:tall_seagrass`
  (grass_drops), `farmersdelight:tomatoes`, `supplementaries:stick_tomatoes`, `fruitsdelight:attached_hamimelon_stem`.
  Which tab lists the tomato and hamimelon blocks is unverified (a grep of `jei_world/*.json` finds none; probably
  block tags expanded at runtime). The Budding Blocks tab's barrier is `tools/jei_world/tab_budding.py` line 124
  (`item('minecraft:barrier', label='Drops nothing')`).
- Air outputs already draw an empty "Nothing" slot (see jei_world_tabs.md 1).

### Build
1. Add a stand-in map in `WorldRow.blockStack`: `frosted_ice` and `rare_ice` -> `minecraft:ice`; `bubble_column` -> water
   bucket or `minecraft:blue_ice` (decide), `*_candle_cake` -> `minecraft:cake`; crops -> their seed or fruit item;
   keep the block name as hover text (already done for the Barrier).
2. Budding: replace the barrier with the `Nothing` slot (`"air": true`-style output, per the `Nothing` slot docs in
   `WorldRow.java`).
3. Rows that cannot be told from another row after the stand-in (ice vs frosted ice) get merged or dropped.

### Decisions
Per block: stand-in or drop. Recommend: ice family -> Ice; candle cakes -> Cake; rice, tomato, hamimelon -> drop the row
unless it teaches something not on the item's own page.

### Tests (JW-)
- JW-22 (proposed): scan every world tab page (Uses of Ice, Lava Bucket, Knife, Grass, Amethyst Cluster): no Barrier.

---

## jei-coverage #20: Burning tab text is cut off
Status **Testing** (BUILT 2026-10-26, [plan_jei_tab_tweaks.md](../plan_jei_tab_tweaks.md)) / Work patcherMod / TODO `jei/jei-coverage #20`

### Goal
Burning rows show their notes on whole lines, nothing cut.

### Now
- `compat/jei/world/BurnRows.java` adds two separate note strings per rule row (lines 88-89: "Burned by <where>: leaves
  this." and "The result burns if dropped back in.") and two for the ash row (125-127). Each string starts a new line, so
  a wrapped first note plus the second hits the 3-line cut (`NOTE_LINES = 3`, `WorldInteractionCategory` line 59).

### Build
1. In `BurnRows` join each row's notes into one string (lines 88-89 and 126-127) so they flow on one paragraph.
2. Re-check the longest "where" ("fire or lava"); shorten the words if it still reaches line 4.

### Gotchas
Java provider: needs a restart, not F3+T.

### Tests (JW-)
- JW-18 (existing): add Burning to the paged list; no row cut. JW-13 covers the rows.

---

## jei-coverage #21: Lava Generation tab becomes Lava Interactions
Status **Testing** (BUILT 2026-10-26, [plan_jei_tab_tweaks.md](../plan_jei_tab_tweaks.md)) / Work patcherMod / data / TODO `jei/jei-coverage #21`

### Goal
The tab is called Lava Interactions; the basalt generation row shows its water interaction.

### Now
- Tab id `lava_generation` ("Lava Generation", `lang/en_us.json` line 10). Rows: `generated.json` 14 (IW
  `fluid_generators` via `tab_weathering.py`) + `manual.json` 11 (vanilla, Create, DnD milkshakes, sulfuric acid).
- Basalt rows today: lava over basalt (flowing only, IW generated), lava over soul soil next to blue ice -> basalt
  (manual), and the IW smooth basalt row (water beside, blue ice touching, soul soil below). There is no
  "lava beside water -> basalt" row; vanilla/Create's own water+lava -> cobblestone/stone/obsidian may already be
  listed (check `manual.json`).

### Build
1. Lang: change the title text only (keep the id so rows keep loading); add the title to `names` if wanted.
2. Basalt + water: add a manual row only if the user wants it (Decisions); otherwise leave.
3. Rows from `engineering/dreams-and-desires #7`, `#8` (milkshake, sulfuric acid) change when those are tuned: regenerate
   or edit `manual.json` after them.

### Decisions
Does basalt get a water row (basalt next to water, or basalt to basalt)? Recommend: no new row until an actual vanilla or
mod rule exists (unverified that water + lava ever makes basalt without blue ice and soul soil).

### Tests (JW-)
- JW-05 (existing, edit): the tab title reads "Lava Interactions".

---

## jei-coverage #22: Mob eggs render as mobs in JEI screens?
Status **Testing** (BUILT 2026-10-26, [plan_jei_tab_tweaks.md](../plan_jei_tab_tweaks.md)) / Work patcherMod / TODO `jei/jei-coverage #22`

### Goal
Where a world tab shows a mob as its spawn egg, draw the mob instead (if wanted).

### Now
- `WorldRow.entityStack` (line 215-224) turns an entity into its spawn egg or a named name tag; used by Mob
  Transformations, Mob Bottling, Sample Syringe and the jar-capture rows. JEI has no entity ingredient type.
- Mob drawing already exists in the More Mooblooms jar: `MoobloomShearingCategory.renderEntity` (line 138; scales to
  34 px, follows the mouse). JEI 11.8.1 is installed (`IRecipeSlotBuilder.setCustomRenderer` takes an
  `IIngredientRenderer`); unverified which JEI builds in `WorldInteractionCategory.setRecipe`.

### Build
1. Add a custom renderer for item slots whose stack is an entity stand-in (mark it with an NBT tag set in
   `entityStack`, e.g. `CE_Entity`), create the entity from the id on first draw and cache it per level, as
   `MoobloomConversionCategory.entity` does.
2. Keep the egg as the lookup ingredient so recipes by mob still work; only the drawing changes.

### Decisions
Whether to do it. Recommend yes for Mob Transformations only (a creeper -> charged creeper reads better as mobs) and
keep eggs elsewhere; skip if a mob without a renderer-safe constructor (MCA villagers) crashes.

### Gotchas
Creating client entities in `ClientLevel` for modded mobs can throw; wrap in try/catch and fall back to the egg.

### Tests (JW-)
- JW-23 (proposed): Mob Transformations page draws the mob; MCA villager page falls back to the egg without an error log.

---

## jei-coverage #23: Jar Storage tab shows potion fluid as an uncraftable potion
Status **Testing** (BUILT 2026-10-26, [plan_jei_tab_tweaks.md](../plan_jei_tab_tweaks.md)) / Work patcherMod / TODO `jei/jei-coverage #23`

### Goal
Jar Storage shows one row per potion type with a real potion fluid and bottle, not "uncraftable".

### Now
- `tools/jei_world/tab_drops.py` `filled_ing` (183-187) emits one row for all potions: bottle input `items:
  [potion, splash, lingering]` with NBT `{Potion:"minecraft:swiftness"}` labelled "Any potion", output jar with
  `moonlight:potion` fluid and `create:potion` 250 mB with no NBT (the "uncraftable potion" fluid).
- Related `jei/jei-coverage #10` (jars with black liquid; the jar colour comes from `CachedColor`, fixed by `jar_color`).
- The pack already builds per-potion pages in Java: `PotionPagesJEI` (Tipped Arrows, Potion Buckets), a model for
  looping over `ForgeRegistries.POTIONS`.

### Build
1. Drop the generic potion row from `tab_drops.py` and add a Java provider (like `BurnRows`: `WorldRows.add` at JEI
   start) that loops over the registered potions (skip water, awkward, thick, mundane as `N-06` does) and builds, per
   potion, the bottle (`PotionUtils.setPotion`), the potion fluid with the `Potion` tag, and the jar colour
   (`PotionUtils.getColor`).
2. One row per potion type; a long list pages like the Potion Buckets tab.

### Decisions
All potions or only those a jar can hold (`preserved_tags_from_item` means one potion kind per jar). Recommend all.

### Tests (JW-)
- JW-24 (proposed): Uses of a Healing potion -> Jar Storage row with Healing in colour, not Swiftness, fluid tooltip names Healing.

---

## jei-coverage #24: Update the JEI arrow in potion tipping, seasoning and potion buckets
Status **Testing** (BUILT 2026-10-26, [plan_jei_tab_tweaks.md](../plan_jei_tab_tweaks.md)) / Work patcherMod / data / TODO `jei/jei-coverage #24`

### Goal
Potion Tipped Arrows, Potion Buckets and Seasoning pages use the same arrow as Feast Plating and the world tabs.

### Now
- `RowPagesCategory` (Tipped Arrows, Potion Buckets) and `SeasoningCategory` both draw JEI's crafting-table arrow:
  `createDrawable(CRAFTING_GUI, 90, 35, 22, 15)` (RowPagesCategory line 78, SeasoningCategory line 61).
- `FeastPlatingCategory` (line 112) and `WorldInteractionCategory` use `createDrawable(GUI, 82, 128, 24, 17)`.

### Build
1. Copy the `GUI` arrow draw into both classes (24 x 17; shift `ARROW_X` constants by 2 px, `OUTPUT_X` by the width
   difference: `RowPagesCategory` line 41-42, `SeasoningCategory` line 40-41).
2. Seasoning later splits into a category per seasoning type (`food/seasoning #1`): keep the arrow in one shared helper.

### Tests (JC-)
- JC-30 (proposed): the three tabs show the new arrow, slots still aligned.

---

## jei-coverage #25: Empty jar JEI icon becomes jar with spout
Status **Testing** (BUILT 2026-10-26, [plan_jei_tab_tweaks.md](../plan_jei_tab_tweaks.md)) / Work patcherMod / resource pack / TODO `jei/jei-coverage #25`

### Goal
The empty jar icon in the jar JEI categories is replaced by a jar with a spout.

### Now
- Jar categories: Jar Storage (icon `supplementaries:jar`, catalyst the jar) and Jar Fluids (icon
  `supplementaries:faucet`, `WorldTab` lines 23-24); the jar item in rows is `JAR = 'supplementaries:jar'`
  (`tab_drops.py` line 29).
- There is no "jar with spout" item in this pack (unverified: grep of `kubejs/assets` finds nothing).

### Build
Options, pick one (Decisions): (a) a drawable icon: jar item with the faucet item drawn beside it, via an `IDrawable`
passed to `createRecipeCategory`; (b) a texture in a resource pack and a custom item model; (c) swap the icon for the
tinted jar filled with water.

### Decisions
What "jar with spout" is. Recommend (a): no new assets, uses two real items.

### Tests (JW-)
- JW-25 (proposed): both jar tabs show the new icon in the category list and the tab header.

---

## jei-coverage #26: Knife and Machete Drops: "cut", not "broken"
Status **Testing** (BUILT 2026-10-26, [plan_jei_tab_tweaks.md](../plan_jei_tab_tweaks.md)) / Work patcherMod / lang / TODO `jei/jei-coverage #26`

### Goal
The Knife and Machete Drops tab reads "Cut with a knife", not "Broken"; the cheese note is short.

### Now
- The words are generator text in `tools/jei_world/tab_drops.py`: `notes()` lines 481 and 485 ("Broken with {what}; without,
  drops itself." for Brewin' cheese wheels, "Broken with {what}.") and lines 664 / 667 (Grass Drops: "Broken with a
  knife.", "Broken with shears."). In `knife_drops/generated.json`: 15 x "Broken with a knife.", 2 x "...; without,
  drops itself." (cheese wheels).

### Build
1. `notes()`: "Cut with a knife." (blocks) and the cheese wheel note "Cut with a knife; drops itself otherwise." or shorter
   ("Knife: wedges. Otherwise the wheel."). Keep Grass Drops shears as "Broken"? Recommend "Cut with shears".
2. Regenerate with `/cegen run jei_world`.

### Tests (JW-)
- JW-11 (existing, edit): the wording on a pig row is unchanged (kill); a wheel row and a grass row say "Cut".

---

## jei-coverage #27: Concrete hardening 30 s, not 31
Status **Testing** (BUILT 2026-10-26, [plan_jei_tab_tweaks.md](../plan_jei_tab_tweaks.md)) / Work config / patcherMod / TODO `jei/jei-coverage #27`

### Goal
Liquid concrete sets in 30 s and the tab says 30 s.

### Now
- `jei_world/concrete_hardening/manual.json` note: "Sets after about 31 s". Its `_note` records IE 9.2.4-170
  `ConcreteFluid.tick` (javap): tick delay 20, a timer state counting up per fluid tick, set at 31. `grep -i concrete
  config/immersiveengineering-common.toml` finds no setting, so the value is hardcoded.

### Build
1. A patcherMod mixin on `ConcreteFluid` changing the timer limit (31 -> 30). `javap` the target method first.
2. Edit the `manual.json` note to "Sets after about 30 s" (one row has it, the thin-flow row may too).
3. Changing only the note would make the tab lie (the fluid still sets at 31), so the mixin is the real fix.

### Gotchas
Mixin targets an IE class by name: `require = 0` and guard with the mod id (the pack's other IE mixins show the pattern).

### Tests (JW-)
- JW-06 (existing, edit): place IE liquid concrete, time the set at 30 s; tab text agrees.

---

## jei-coverage #28: Remove Survivality from Mob Transformations
Status **Complete** (kept, owner 2026-10-26) / Work patcherMod / TODO `jei/jei-coverage #28`

### Goal
Mob Transformations lists no Survivality rows.

### Now
- `jei_world/mob_transformation/manual.json` row 7 (the only Survivality row: horse + lightning -> zombie horse,
  note "Survivality. Not on Peaceful.", `requires: ["survivality"]`). 23 rows in the file.
- **The TODO premise does not match the repo:** `survivality-1.19.2-1.2.4-forge.jar` is still in `mods/` and in
  `mod_sides.md`, and `ports/featureporting #6` says port Survivality tweaks "one at a time if it breaks something", not
  remove it. The row drops out of JEI by itself when the jar is removed (`requires`).

### Build
Delete row 7 from `manual.json` (the file is the source; no generator writes it). Update the writeup's tab table.

### Decisions
Is Survivality leaving the pack (then also remove the jar and the `mod_sides.md` row), or only the JEI row? Recommend
confirm; if it stays, the row is accurate and should stay.

### Tests (JW-)
- JW-12 (existing, edit): Mob Transformations shows no Survivality row.
