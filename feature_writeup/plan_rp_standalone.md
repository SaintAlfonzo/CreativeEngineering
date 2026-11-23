# Plan: Raspberry Pie as a standalone mod, shipped from the GitHub repo (2026-11-21)

<!-- generated:status_index begin open_work sha1=206922f97b0d -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 22 open TODO rows (Planned 9, Testing 13), 1 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `food-tuning #46` | Planned | S C | RP standalone P9: isolation tests RPS-01 to -07 and RPI rerun |
| `food-tuning #51` | Planned | S C | Steady (Astringent): stops every slip effect (ice, soap), not dripleaf or pressure plates; JEI description |
| `food-tuning #52` | Planned | S C | Leaf Piercing: leaf particles of the pierced block trailing the arrow, a leaf poof where it enters |
| `food-tuning #53` | Planned | S C | Prickly II: tamed and passive mobs exempt (level I: tamed only); JEI description |
| `food-tuning #54` | Planned | S C | Lava Walker: crouch to drop into lava |
| `food-tuning #56` | Planned | S C | Display names: Astringent -> Steady, Poison Resistance -> Poison Immunity, Fire Resistance -> Fire Immunity |
| `food-tuning #57` | Planned | S C | Emptiness becomes a sub-effect of Patrol Nullifier and Phantom Bane |
| `food-tuning #58` | Planned | S C | Harmony: a new source (not adzuki, not the diet ladder), e.g. a rare passive mob; applies Emptiness and hostile mob flee |
| `raspberry-pie-publish #10` | Planned |  | Standalone Diet Log recipe takes any edible item that is not excluded; state sync stops resending every drink id |
| `food-tuning #38` | Testing | S | RP standalone P1: Jar-in-Jar bundle (Apple Pie, Carrot nested), license texts, NOTICE, mods.toml URLs, deploy and manifest updates |
| `food-tuning #39` | Testing | S C | RP standalone P2: default ladder = Apple Pie's ladder plus 10 vanilla unlockables and slots |
| `food-tuning #40` | Testing | S | RP standalone P3: diversity table loaded from any jar's solraspberrypie/diversity/*.json, ids optional, no table in the mod |
| `food-tuning #41` | Testing | S C | RP standalone P4: move Diet Log rename, recipe removal and item_dict keys out of the pack into Raspberry Pie |
| `food-tuning #42` | Testing | S | RP standalone P5: standalone defaults: Apple Pie default settings, recommended settings file, start warnings |
| `food-tuning #43` | Testing | S | RP standalone P6: Fleet-Footed mixin guard (member check, require 0, one warning) |
| `food-tuning #44` | Testing | S C | RP standalone P7: pack ladder from the scratchpad (toggles, tick markers, hover taglines, new effect ids) |
| `food-tuning #45` | Testing | S | RP standalone P8: pack diversity table moves to Creative Delight; pack sets Apple Pie values |
| `food-tuning #47` | Testing | S C | Wither Immunity effect for the Raspberry Pie pack ladder (reserved unlock 22 at 47.1) |
| `food-tuning #48` | Testing | S C | Satiated (minecraft:saturation) as the RP pack ladder's final permanent buff at 100: V10 exception for flat rungs |
| `food-tuning #49` | Testing | S C | RP ladder: a later unlock replaces its parts (Clinging hides Climbing; Recovering hides Poison Immunity and Wither Immunity) |
| `food-tuning #50` | Testing | S C | RP ladder: once a permanent buff of an effect is active, a slot holding that effect grays out with a free respec |
| `food-tuning #55` | Testing | S C | Fleet-Footed: lower field-of-view shift while its speed applies |
<!-- generated:status_index end open_work -->

Source: owner chat 2026-11-21 (after the publish-readiness review in the same session). Design of the mod itself:
[raspberry_pie.md](raspberry_pie.md). Status lives in the TODO rows (section 7), not here. P0 to P8 are built (`food-tuning #38` to `#45`, `#48`); P9 (tests) and the scratchpad follow-ups `#47`, `#49` to `#58` are listed in section 4 P7.

Goal: `solRaspberryPie` runs and is useful with only Spice of Life: Apple Pie Edition (and Carrot Edition for the Food
Dictionary), carries nothing that belongs to the pack, and is distributed from the Creative Engineering GitHub repo the
way Creative Common is. The pack layers its own food table, ladder, names and configs on top.

## 1. Rulings (owner, 2026-11-21)

| # | Ruling |
|---|---|
| A1 | Apple Pie is LGPL-2.1 (the license the jar declares). Raspberry Pie ships Apple Pie and Carrot Edition as nested submods (Forge Jar-in-Jar, like Creative Delight and Creative Brewing). |
| A2 | The Liquid Diet Log texture stays a copy of Apple Pie's Diet Log book for now. |
| A3 | Move into Raspberry Pie: the Diet Log rename, `diet_log.js`, the `item_dict.js` keys only if Raspberry Pie itself uses them. The default configs: Raspberry Pie's own default ladder moves into the jar; Apple Pie's settings file stays in the pack's `defaultconfigs` (D4, ruled). |
| A4 | Default Raspberry Pie ladder = Apple Pie's own default ladder with 10 vanilla effects from the pack's custom ladder mixed in as unlockables. |
| A5 | The ladder in the scratchpad ("rp changes", todo/scratchpad.md) is the pack's ladder, implemented by the pack. |
| A6 | Table entries are optional at id loading (an id whose mod is absent is skipped, not an error). |
| A7 | Raspberry Pie does not override Apple Pie's values; the pack does. |
| A8 | Add a guard to Fleet-Footed (section 4, P6). |
| A9 | The owner will make a few tweaks and run tests on the mod, including one in isolation (no pack). |

## 2. What the audit found (checked 2026-11-21)

- `item_dict.js` keys `diet_log` and `liquid_diet_log` (lines 13209, 13218) are used nowhere else: no KubeJS script reads
  them, and Raspberry Pie's Java never reads `item_dict`. Both keys are removed from Creative Common, none moves
  (A3's condition is not met by either).
- The rename lives in Creative Delight's `names.js` (the two food-book lines, comment at about line 127); the recipe
  removal in Creative Delight's `solapplepie/diet_log.js`; the third piece of Apple Pie wiring in the pack,
  `kubejs/data/creativedelight/kubejs/solapplepie/food_types.js`, is deleted in the working tree already and stays
  pack-side (a pack food-type file).
- `src/main/resources/solraspberrypie/diversity.json` (1,658 ids, about 1,500 from pack mods, scale fitted to a 120
  ceiling) is unconditional and overrides Apple Pie's value for every listed food, vanilla included.
- `default_ladder.json` in the jar tops out at diversity 15 (the old scale). Apple Pie's own default ladder is 17
  rungs at 2, 5, 7, 10, 15, 20, 25, 30, 35, 40, 45, 50, 60, 70, 80, 90, 100 (read from its decompiled config class).
  Apple Pie's defaults for `queueSize` (32), `minFoodsToActivate` (0) and `endDecay` (32) differ from the pack's
  (256, 5, 32); Raspberry Pie's ageing-out was built against the pack values.
- The jar already gates the Alex's Mobs mixin (`RpMixinPlugin`: "FleetFootedMixin" needs `alexsmobs`) but not against an
  Alex's Mobs version whose class or fields differ.

## 3. Where each piece ends up

| Piece | Now | After |
|---|---|---|
| Diet Log (and Food Dictionary) names | Creative Delight `names.js` | Raspberry Pie `assets/solraspberrypie/lang/en_us.json` (keys `item.solapplepie.food_book`, `item.solcarrot.food_book`; lang merges across namespaces, mind [forge-shared-asset-path](agent_notes/forge-shared-asset-path.md)) |
| Remove Apple Pie's book + potato recipe | Creative Delight `diet_log.js` (KubeJS) | Raspberry Pie, in Java: drop the recipe id from the recipe manager's load (works without KubeJS). The Creative Delight file is deleted |
| `i.diet_log`, `i.liquid_diet_log` | Creative Common `item_dict.js` | deleted (unused); Common rebuild |
| Ladder default | `defaultconfigs/solraspberrypie/ladder.json` (pack draft, 38 rungs) and a jar default topping at 15 | jar default = Apple Pie default ladder + 10 vanilla unlockables and slots (section 4, P2); pack `defaultconfigs/solraspberrypie/ladder.json` becomes the scratchpad ladder (P7) |
| Apple Pie settings (`endDecay`, `queueSize`, `minFoodsToActivate`, drink blacklist) | pack `defaultconfigs/solapplepie-server.toml` | stays in the pack's `defaultconfigs` (ruled D4); Raspberry Pie seeds nothing and only warns at start about settings it cannot work with |
| Diversity table | in the jar, unconditional | out of the jar; Raspberry Pie reads every `solraspberrypie/diversity/*.json` it finds on the classpath (any mod's jar), ids optional; the pack's table ships in Creative Delight (food code is Delight's) |
| Nested Apple Pie, Carrot | loose jars in `mods/` | Jar-in-Jar inside `solRaspberryPie-<ver>.jar`; loose copies leave `mods/` |

## 4. Phases

Order: P0 and P1 first (no code), then P2 to P6 in the mod, P7 to P8 in the pack, P9 tests. One code owner per phase.

**P0 License and notice check (before any release).** Open the upstream repos at the exact tags of the two jars and
confirm a LICENSE file says LGPL-2.1 (the jars' `mods.toml` does; the CurseForge page for Apple Pie was recorded as All
Rights Reserved in raspberry_pie.md section 1, which is a conflict the owner has ruled on but nobody has verified at the
source). Record the result and date here. Also check the license of the book art (Diet Log, Food Dictionary textures)
in each repo, since the Liquid Diet Log copy depends on it.

P0 result (checked 2026-11-21 against GitHub, public API and raw files):
- Apple Pie: upstream `https://github.com/txnimc/Spice-of-Life-Apple-Pie` (the jar's `issueTrackerURL`,
  `tarinoita/Spice-of-Life-Sweet-Potato-Edition`, is gone; `tarinoita/Spice-of-Life-Sweet-Potato` is the older 1.18
  fork). Tag `2.2.0` exists, `gradle.properties` there says `mod_version = 2.2.0`, `minecraft_version = 1.19.2`;
  `LICENSE.txt` at the tag is the GNU LGPL 2.1 text; GitHub reports LGPL-2.1 for the repo.
- Carrot: upstream `https://github.com/Cazsius/Spice-of-Life-Carrot-Edition`, no tags; branch `1.19` (head
  `ddb1729`, 2023-01-24) has `mod_version = 1.14.0`, `mc_version = 1.19.2` and `LICENSE.txt` = LGPL 2.1.
- Book art: neither repo carries a separate asset license or an art note in its README, so the textures fall under the
  repo LICENSE (LGPL-2.1). `liquid_diet_log.png` is byte-identical (sha1 `33173875...`) to Apple Pie's
  `assets/solapplepie/textures/items/food_book.png` at tag 2.2.0 and in the 2.2.0 jar.
- The CurseForge "All Rights Reserved" label in raspberry_pie.md section 1 conflicts with the source; the repo license
  and the jar's `mods.toml` (`license = "LGPL-2.1"`) agree, and A1 stands. `NOTICE` links the tag (Apple Pie) and
  the branch head commit (Carrot) as the source offer.

**P1 Bundle build.** Switch `development/solRaspberryPie` from `ce.forge-mod-refmap` to the bundle plugin
(`build-logic/.../ce.forge-bundle.gradle`) with Apple Pie and Carrot as unmodified nested jars under
`META-INF/jarjar/`, version ranges as today (`[1.19.2-2.2.0]`, Carrot `[1.19.2-1.14.0,)`; Carrot becomes mandatory in
metadata only if the Food Dictionary mixins stop being gated, otherwise stays nested but unused when absent). Add to the
jar: `LICENSE` (GPL-3.0-only for our code), each nested mod's LGPL-2.1 text and a `NOTICE` (names, authors, upstream
URL, the exact version, "unmodified", where the source is). `libs/` jars for compiling move from the pack's `mods/` to a
tracked-by-sha1 download step or stay git-ignored as now. Add `displayURL` and `issueTrackerURL` (the GitHub repo) to
`mods.toml`. Update `mod_sides.md`, `pack/manifest.json` and `MODLIST.md` generation, and `build_deploy.py` categories so
the nested jars are not also staged loose.

**P2 Default ladder.** Replace `default_ladder.json` with: Apple Pie's 17 thresholds and its default benefit list
(read from `SOLSweetPotatoConfig`, kept as health, toughness, knockback and other attribute rungs), plus the unlock and
slot rungs below. Ten vanilla unlockables spread across the ladder, taken from the scratchpad list (see D2), and
effect slots so the unlocks are usable. Validate with the existing 3.3 checks; add a headless test that the default
ladder loads with no warning.

P2 as built after the owner rulings of 2026-11-21 (raspberry_pie.md 12.9): Apple Pie's ladder with its flat effects,
every threshold halved to a top of 50 (vanilla foods on Apple Pie's defaults reach about 56); slots 5, 15, 27.5, 40;
13 unlocks at 5 to 50 with the D10 falling density; the three added unlocks (Jump Boost II, Haste II, Slow Falling) are
an agent default, kept (owner, 2026-11-22, D9 in publish_readiness.md); Haste II then left with the four unusable unlocks (section 10). Start line now `Diet ladder: 44 rungs, 4 slots, 8 pool effects`, no warning.

**P3 Table loader.** `Diversity` reads `solraspberrypie/diversity/*.json` from every classpath root (`getResources`),
merges them (later file wins on a conflict, logged), skips ids not registered (one INFO line with the count, never a
warning per id), and the jar's own copy is deleted. With no file found nothing is overridden and Apple Pie's values stand
(A7). Families keep the same file shape. The generator `tools/food_diversity.py` writes to Creative Delight's tree.

**P4 Move the wiring.** Names, recipe removal and key cleanup per section 3. The Liquid Diet Log recipe and tags
stay in the jar. The recipe removal also runs on a dedicated server with no KubeJS.

**P5 Standalone defaults.** With only Apple Pie's own defaults (`queueSize` 32, `minFoodsToActivate` 0): check the
ageing-out cost (6.1) and the unique-food gate still behave, and log a loud warning at server start naming any Apple Pie
setting Raspberry Pie cannot work with (today only `minContribution` above 0 is warned). The pack's `defaultconfigs` copy
of Apple Pie's file stays the source of the recommended values (D4).

**P6 Fleet-Footed guard.** `FleetFootedMixin` stays gated on `alexsmobs`; add (a) `require = 0` and a try on the
fields it touches, (b) a mixin-plugin check that `EffectFleetFooted` has the expected members at the loaded version, and
skip the mixin with one warning when they differ, (c) a version range note in the writeup for the Alex's Mobs versions
tested. Result: an Alex's Mobs update can only disable the fix, never crash the game or the server.

**P7 Pack ladder.** Implement the scratchpad ladder as the pack's `defaultconfigs/solraspberrypie/ladder.json`
(sub-ladders in food_diversity plan section 7). Needs from Raspberry Pie: a toggle switch for flat or unlocked effects
(Night Vision, Glowing, Phantom Bane, Invisibility, Double Jump, Steady, off by default where the scratchpad says), a
tick marker on the Ladder bar per milestone (colored like the page arrows), and an unlock hover tagline. Needs from the
pack: effect ids for Leaf Piercing, Steady, Prickly, Magnet, Double Jump, the immunity groups (plan_food_diversity_1121.md
O3). **Spacing (D10):** unlockables and permanent effects are not evenly spaced. Their density falls linearly along the ladder, so the first stretch holds `densityRatio` (default 2.0, configurable) times the effects per diversity point that the last stretch does. Thresholds are the inverse of the cumulative density at equally spaced points, endpoints fixed (unlockables 2.5 to 97.5, 39 of them; permanent effects 25 to 100, 12 of them; the default ladder's 10 unlockables 10 to 100). A new generator `tools/gen_rp_ladder.py` (listed in `tools/generators.json`) reads `densityRatio` from a key in the generator's own config and writes the thresholds into the pack's `ladder.json`; the ladder file itself stays plain thresholds, so Raspberry Pie needs no code for it. At 2.0 the first gap is about half the last gap (unlockables 1.9 -> 3.7, permanent 5.2 -> 9.6). Hearts and the toughness / knockback pairs stay evenly spaced (they are stat increases, not effects). O3 ruled (owner, 2026-11-21): the ladder is built now with the effects that already exist; every missing effect gets its own TODO row (built later in Creative Common, which owns effects) and keeps a reserved rung, left out of the file until its id exists. Effect slots stay, evenly spaced along the ladder (not the falling density). O4 ruled (permanent effects start at 25). **Built 2026-11-21** (raspberry_pie.md 3.3 file format, 12.10): `tools/gen_rp_ladder.py` from `kubejs/generation_scripts/food/rp_ladder.csv` + `rp_ladder.json`; toggles, Ladder bar ticks, taglines in the jar. Every scratchpad effect but two has a registered id (Leaf Piercing, Steady = `fruitsdelight:astringent`, Prickly = `hearthandharvest:prickly`, Magnet and Double Jump = Vinery, Refreshing, Sweetening, Clarity, Stimulation, Recovering all exist). **Reserved position (kept, left out of ladder.json until filled):** unlockable 22 at 47.1 Wither Immunity (no id; `food-tuning #47`). **Owner rulings 2026-11-21:** tick colors gray/purple confirmed; Satiated in at 100 through a V10 exception for `minecraft:saturation` flat rungs (`food-tuning #48`); slots 5, evenly spaced from 2.5 (2.5, 22, 41.5, 61, 80.5) kept; toggles off by default = Invisibility, Glowing, Slow Falling, Magnet, every other toggle on; Fire Resistance stays an unlock; Stimulation leaves the unlock ladder (section 4). The ruling asked for vanilla replacements for Stimulation and Invisibility: Invisibility was never on the unlock ladder (only the permanent one, kept), and no vanilla positive effect is free for Stimulation's spot (Speed, Haste, Strength, Jump Boost, Night Vision, Water Breathing, Luck, Regeneration, Fire Resistance, Slow Falling and Glowing are all on it; Resistance, Invisibility, Absorption, Health Boost, Instant Health, Saturation, Hero of the Village, Dolphin's Grace and Conduit Power are refused by raspberry_pie.md section 4), so Clarity holds position 21 alone (agent default, **unconfirmed**). Start line `Diet ladder: 81 rungs, 5 slots, 37 pool effects`. Unbuilt scratchpad items: `food-tuning #49` to `#58`.

**P8 Pack table.** Move `diversity.json` and the generator output into Creative Delight. The pack also sets the Apple Pie
values it wants through that table, so the mod is neutral (A7).

**P9 Tests.** Section 6.

## 5. Open decisions, with the agent default taken

| # | Question | Agent default (all ruled) |
|---|---|---|
| D1 | Where the value-override mechanism lives | **Ruled (owner, 2026-11-21): agent default kept.** In Raspberry Pie, data-driven and empty without data (not a mixin in Creative Common). Alternative: a Common-owned mixin into Apple Pie, which splits Apple Pie wiring across two jars |
| D2 | Which 10 vanilla effects are the default unlocks | **Ruled (owner, 2026-11-21): agent default kept.** Speed I, Haste I, Strength I, Jump Boost I, Night Vision, Water Breathing, Luck I, Regeneration I, Fire Resistance, Glowing; at about every 10 diversity from 10 to 100 on Apple Pie's scale. Resistance, Invisibility, Absorption, Health Boost and instant effects stay out (rules in raspberry_pie.md section 4) |
| D3 | Default slots | **Ruled (owner, 2026-11-21): agent default kept.** 4 slots at 10, 30, 55, 80 on Apple Pie's scale |
| D4 | Apple Pie settings file | **Ruled (owner, 2026-11-21): the pack's `defaultconfigs` stays.** Raspberry Pie writes no Apple Pie file; the jar-seeding idea is dropped |
| D5 | Do the Food Dictionary names move too | **Ruled (owner, 2026-11-21): agent default kept.** Yes, "Food Dictionary" moves with "Diet Log" (Raspberry Pie owns the Carrot book mixins) |
| D6 | Carrot nested and mandatory? | **Ruled (owner, 2026-11-21): agent default kept.** Nested; stays optional in `mods.toml` terms so Raspberry Pie still loads if the nested Carrot is replaced or removed |
| D7 | Version number for the first public build | **Ruled (owner, 2026-11-21): agent default kept.** 1.0.0 stays; the nested jars carry their own versions |
| D8 | Test prefix for the isolation rows | **Ruled (owner, 2026-11-21): agent default kept.** New prefix `RPS` (isolation), row added to test_prefix_index.md first |
| D10 | Falling effect density | **Ruled (owner, 2026-11-21): the start of the chain has about 2x (configurable `densityRatio`) the effects per point as the end**, applied to unlockables and permanent effects (P7 above); positions in plan_food_diversity_1121.md section 7. Also applies (owner, 2026-11-21) to the 10 default-ladder unlockables, at 10, 18, 26, 34, 43, 53, 63, 74, 86, 100 (D2's "about every 10" becomes these) |
| D9 | What "fleet food" means | **Ruled (owner, 2026-11-21): Fleet-Footed**, the Alex's Mobs effect (`FleetFootedMixin`); P6 stands |

## 6. Tests

Rows go into `feature_writeup/testing/rows/RPS.qb` (isolation) and the existing `RPI.qb`; nothing is booted until you
say so.

| Id | Where | Check |
|---|---|---|
| RPS-01 | isolated, server | mod set = Forge + `solRaspberryPie` only (nested Apple Pie and Carrot): boots, no missing-dependency, no mixin error, one `Diet ladder:` line, the default ladder loads without a warning |
| RPS-02 | isolated, client | same set: Diet Log named Diet Log, Food Dictionary named Food Dictionary, Apple Pie's book + potato recipe gone, book + food and book + drink recipes work |
| RPS-03 | isolated | no table found: foods keep Apple Pie's values; tooltips and the books agree |
| RPS-04 | isolated | a loose Apple Pie 2.2.0 jar also in `mods/`: one copy loads, no duplicate-mod crash |
| RPS-05 | isolated | Apple Pie's own settings at its defaults (`queueSize` 32, `minFoodsToActivate` 0): eat, respec, drop, die; ageing-out and the gate behave |
| RPS-06 | server | with Alex's Mobs 1.19.2 present: Fleet-Footed mixin applies; with a stubbed class missing a field: skipped with one warning, no crash |
| RPS-07 | pack | table split: a table file with an unknown id loads, count logged once, other ids apply |
| RPI-* | pack | the existing 65 client rows, rerun against the final jar (after P2 to P8) |

## 7. TODO rows

Family Food Tuning, `food-tuning #37` (P0) to `#46` (P9), one row per phase in order. Status in the rows only.

## 8. Can Raspberry Pie be distributed from the pack's GitHub repo, like Creative Common?

Short answer: yes for the GitHub repo and a GitHub release, once P0 to P1 are done. This is a license reading, not legal
advice, and the one fact it depends on (P0) has not been checked at the source.

| Part | Verdict | Why and what it needs |
|---|---|---|
| Our code (`solRaspberryPie`) | Yes | GPL-3.0-only, the repo's root `LICENSE`. Same as Creative Common |
| Mixing into Apple Pie and Carrot at runtime | Yes | Separate works in separate jars; nothing of theirs is copied into our classes (the mod already states this) |
| Nesting Apple Pie and Carrot jars in our jar | Yes if both are LGPL-2.1 | The LGPL-2.1 terms to meet: ship the license text and notices; give the source or a written offer (link the exact upstream tags in `NOTICE`); keep the library replaceable (Jar-in-Jar keeps it a separate jar a user can swap). Ship the jars unmodified. Blocked if P0 finds the upstream repos say otherwise or an All Rights Reserved page controls |
| Contradiction with the donor-jar policy | Needs a recorded exception | [upstream-jar-distribution-policy](agent_notes/upstream-jar-distribution-policy.md) says the pack never hosts a third-party jar. That note is about ARR donor jars; A1 is a deliberate, license-backed exception for two LGPL mods. Update the note and `deploy.md` when ruled final |
| Liquid Diet Log texture (copy of Apple Pie art) | Yes, with attribution | Needs the art's license from P0, a `NOTICE` line, and rows in `texture_sources/sources.csv` (there is none for it or `diet_marker.png` today); the rule allows copies of upstream art |
| Pack contents (table, ladder, names, recipes) | Yes | Ours; only the Apple Pie values stay Apple Pie's |
| CurseForge or Modrinth | CurseForge (owner, 2026-11-22, publish_readiness.md D9) | Modrinth possibly later. Their moderation looks at nested jars and at the CurseForge page's All Rights Reserved text (P0 found LGPL-2.1 at the source) |
| Repo state | Public (owner, 2026-11-22) | The GitHub repo is the source location named in `mods.toml` and the NOTICE |
| Per-release needs | Yes | Tag, a standalone changelog (the pack's `unreleased.md` is player-facing for the pack, not for a separate mod), README with install steps and the isolation tests, `mods.toml` URLs |

## 9. Agent defaults in the Raspberry Pie docs

Collected for the owner to rule or keep. **All confirmed (owner, 2026-11-22, P9):** R5, R6, R9, R14, D1 to D6 of
plan_food_diversity_1121.md, the `diversityIconFallback` list (now the pack's value in `config/solraspberrypie-client.toml`;
the jar default is vanilla, PR-RP-15) and the shared Diet Log icon. The "confirm" cells below are kept as the record.

| Source | Default | Ruling needed |
|---|---|---|
| plan_food_diversity_1121.md R5 | Pepper rolls follow the medley color groups, one food per group | confirm |
| R6 | Covering families: per base food all chocolate coverings are one entry; candies are two entries (12 chocolate-filled, 4 caramel-filled) | confirm |
| R9 | Small values for non-food inputs (0.1 to 0.25, containers and gold 0); a dish never scores below its best food input | confirm |
| R14 | Low-only ceiling 20 (was 16.5); full table steps evenly 120 to 20 | confirm |
| D1 (that plan) | Pack curve for the base values: q 0-2 -> 0-1, 2-5 -> 1-3, 5-12 -> 3-6, 12-30 -> 6-9 | confirm |
| D2 (that plan) | Golden and gilded = source food x a fitted multiplier, then the floors; enchanted golden apple 6.78 | confirm |
| D3 (that plan) | Cooked dragon meat 6.84, dragon dishes 7.69 to 11 | confirm |
| D4 (that plan) | Non-food input values (ice, mushrooms, blaze powder 0.25; bone meal, sugar 0.15) and "a dish never below its best food input + 0.1" | confirm |
| D5 (that plan) | A family's value is its best member, capped at the lowest member's tier top | confirm |
| D6 (that plan) | Family meals show in the Diet Log as the family head | confirm |
| plan_food_diversity_1121.md section 7, O4 | Permanent effects start at diversity 40 ("early-mid") | superseded: ruled 25 (row O4 below) |
| plan_owner_notes_1119.md 12a | Static diversity-icon pool by diversity band (apple, bread, carrot, sweet berries at 0 up to enchanted golden apple at 100); replaced in 12.7 by `diversityIconFallback` (pumpkin, mixed, oat, corn and sunflower seeds, Miner's moss, moss clump, rotten flesh, spider eye, dandelion leaf), also an agent pick | confirm the fallback list |
| plan_owner_notes_1119.md ruling 2 (not Raspberry Pie) | Dust particles reach every nearby mob, not only the player | listed for completeness |
| raspberry_pie.md 12.5 | Key's slot-effect cycle period 1 second, fixed in code | superseded by the 12.6 key; close |
| This plan D1 to D9 | section 5 | all ruled 2026-11-21 |
| O4 | Permanent effects start at 25 (owner, 2026-11-21; was 40 assumed). Spread of 12 effects over 25 to 100, every 6.8; the ladder sub-table in plan_food_diversity_1121.md section 7 and the P7 pack ladder use it | ruled |

## 10. Publish fixes and scratchpad follow-ups (2026-11-22)

Rulings (owner, 2026-11-22): P5 the release attaches source zips of the pinned LGPL commits (PR-RP-08); P6 the four
unusable default unlocks are swapped for vanilla effects that work standalone, listed for review (PR-RP-11); P8 Harmony
(`food-tuning #58`): an agent audits rare passive mobs and returns a short list, the owner picks, nothing is built; P9
section 9 confirmed. Rows: `raspberry-pie-publish #2` to `#6`, `food-tuning #49`, `#50`, `#55`.

Built in the jar (raspberry_pie.md 12.12):

- **Default unlocks (P6).** Speed I, Haste I, Haste II and Strength I could never be slotted (Apple Pie's flat rungs grant
  Speed, Haste and Strength before the first slot). No allowed vanilla effect is left outside the pool (section 4 of
  raspberry_pie.md refuses Resistance, Invisibility, Absorption, Health Boost, the instant effects, Hero of the Village,
  Dolphin's Grace and Conduit Power), so the four leave and Jump Boost III comes in; the ten unlocks are re-spread with
  the D10 falling density over 5 to 50 (agent default, **for review**):
  old 5 Speed I, 7.9 Haste I, 10.8 Strength I, 13.9 Jump Boost I, 17.1 Night Vision, 20.4 Water Breathing, 23.8 Luck I,
  27.5 Regeneration I, 31.4 Fire Resistance, 35.5 Jump Boost II, 39.9 Haste II, 44.7 Slow Falling, 50 Glowing ->
  new 5 Regeneration I, 8.8 Jump Boost I, 12.8 Luck I, 17.1 Night Vision, 21.5 Water Breathing, 26.3 Fire Resistance,
  31.4 Jump Boost II, 36.9 Slow Falling, 43 Jump Boost III, 50 Glowing. Regeneration and Luck unlock before their flat
  rungs (10, 17.5); once the flat rung arrives, the slot is covered and changes for free (#50). New check V14 warns
  about an unlock whose flat rung comes no later than the first slot.
- **Replacements (#49).** Pool field `"replaces": [ids]`: once the effect is unlocked the named effects leave the picker,
  a slot holding one is covered and no longer applied, one yellow chat line names the pair. The pack ladder gets them
  from `rp_ladder.json` `replaces` through `gen_rp_ladder.py`: Clinging replaces Climbing, Recovering replaces Poison
  Immunity and Wither Immunity, Stimulation replaces Refreshing and Sweetening (written
  only while Stimulation is on the unlock ladder; it is not, per the 2026-11-21 ruling, and the 2026-11-22 scratchpad
  ruling "Stimulation replaces Refreshing and Sweetening when unlocked" needs Stimulation back in `rp_ladder.csv`: owner).
- **Covered slots (#50).** A slot whose effect is held by an active permanent buff or replaced by an unlock shows grayed
  with "Covered by a permanent buff" or "Replaced by X" and "Changing this slot is free"; its Change ages out no foods.
- **Fleet-Footed field of view (#55).** Client config `effects.fleetFootedFov` (default 0.25): the share of the view
  widening that Fleet-Footed's speed bonus causes; other speed changes widen it as usual.
- **Publish fixes.** PR-RP-04 table loader skips a wrong-shape file with one error line and bad values with one count;
  PR-RP-05 hover debug deleted; PR-RP-02 `tools/rp_libs.py fetch` (sha1-checked, `libs.csv`); PR-RP-07 notice renamed
  `NOTICE-nested-LGPL.txt` with the own copyright moved out; PR-RP-08 `tools/rp_libs.py sources DIR` (`lgpl_sources.csv`)
  and the release step below; PR-RP-09 `require = 0` on both effect-list redirects; PR-RP-10 only names that start with
  the attribute's own id are stripped; PR-RP-13 the Change and Drop counts are cached per window; PR-RP-15 client icon
  defaults vanilla (the pack keeps its values in the tracked `config/solraspberrypie-client.toml`).

Release step (PR-RP-08): for each Raspberry Pie GitHub release, run `python3 tools/rp_libs.py sources <dir>` and attach
`solapplepie-1.19.2-2.2.0-sources.zip` and `SoL-Carrot-1.19.2-1.14.0-sources.zip` next to the jar; the NOTICE names
the release page as the source location. A clean clone builds after `python3 tools/rp_libs.py fetch`.

Not in this jar (wave 2): `#51` Steady, `#52` Leaf Piercing particles, `#53` Prickly II exemptions, `#56` display names,
`#57` Emptiness as a super-effect of Patrol Nullifier and Phantom Bane (effect code, Creative Common); `#54` Lava Walker
crouch (Creative Brewing, Vinery member). `#47` done 2026-11-22: Common 1.3.0 registers
`creativecommon:wither_immunity`; the reserved unlock 22 (48) is filled and Recovering replaces it too. Still open from
PR-RP-07 to -15: PR-RP-12 (Diet Log recipe takes any edible item standalone) and PR-RP-13's drink id resend. PR-RP-14
(writeup install line, section 4 for two ladders) is done in raspberry_pie.md.

Harmony (#58, P8) shortlist from `kubejs/exported/spawns.json` (overworld, share of its spawn category per biome):
Allay (7 biomes, about 2% share; peaceful item helper), Alex's Mobs Potoo (1 biome, night bird), Alex's Mobs Mimic
Octopus (1 biome, warm ocean; the closest to a koi), Alex's Mobs Bunfungus (2 mushroom biomes), More Mooblooms'
Moobloom (2 biomes). Owner picks.
