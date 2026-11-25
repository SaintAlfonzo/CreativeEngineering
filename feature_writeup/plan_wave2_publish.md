# Plan: wave 2 publish (Creative Common, Creative Compat, Raspberry Pie)

<!-- SPDX-License-Identifier: GPL-3.0-only -->

Owner request (chat 2026-11-23): a second publish-readiness wave on top of [plan_wave1_publish.md](plan_wave1_publish.md).
Gameplay changes leave Creative Common for Creative Compat, Creative Delight and Creative Loot. Common keeps the library,
the effects, Performance and stability, and bug fixes. The Common and Compat project pages list every feature and tweak on
its own. Raspberry Pie gets four fixes. Pages: [project_pages/](project_pages/); audit:
[publish_readiness.md](publish_readiness.md).

## 0. Intake (owner's words, 2026-11-23)

<!-- generated:status_index begin open_work sha1=7a5476e33310 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 10 open TODO rows (Testing 10), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `creative-common-publish #17` | Testing | S | Wave 2 carve to Creative Compat: melting (with End no-fire and the Saltable tooltip), Quark glass shard drops, sack sneak pick-up, Create: Gravity low gravity, server-start fix and ItemPhysic tie; [perf] gravityLimitDimensions stays in Common (W2-4); one-time config carry-over (W2-8) |
| `creative-common-publish #18` | Testing | S | Wave 2 carve to Creative Delight: the 12 wooden composters (ids to Delight's namespace, old ids swapped, W2-9) and [bees] honeycombMultiplier |
| `creative-common-publish #19` | Testing | S | Wave 2 carve to Creative Loot: [wandering_trader] spawn and offers with both mixins; Trade Sync and the trade helper stay in Common (W2-2) |
| `creative-common-publish #20` | Testing | D | Common project page in the W2-6 layout (family headers, a bold name and a spoiler per feature) after the wave 2 carve; moved features removed |
| `creative-compat-publish #10` | Testing | D | Compat project page in the W2-6 layout, with the features moved from Common and Raspberry Pie (melting, glass shards, sack pick-up, low gravity, Fleet-Footed) |
| `raspberry-pie-publish #25` | Testing | S C | Fleet-Footed fix and fleetFootedFov move from Raspberry Pie to Creative Compat (W2-3); config value carried over; Raspberry Pie page and writeup drop them |
| `raspberry-pie-publish #26` | Testing | S | Death and paid slot actions (effect change, slot drop) remove the aged-out foods from Apple Pie's memory instead of keeping them past the decay window |
| `raspberry-pie-publish #27` | Testing | C | Food Dictionary: add a Hardest sort (complexity descending) next to Easiest |
| `raspberry-pie-publish #28` | Testing | C | Diet Log extraWidth default 48 [47] |
| `raspberry-pie-publish #29` | Testing | D | Project page: command arguments in square brackets, no &lt; escapes (W2-7) |
<!-- generated:status_index end open_work -->

| # | Item (owner's words) | Class | Found |
|---|---|---|---|
| I1 | "creative compat and common page to list each change to mods individual features/ tweaks and a dropdown for each under family headers" | open | CurseForge spoilers have no title bar; layout asked (W2-6) |
| I2 | "glass shard drops ... moved to compat" | clear | Common loot modifier, Quark-gated |
| I3 | "sack sneak pickup ... moved to compat" | clear | `creativecommon/storage/SneakPickup.java` |
| I4 | "no fire in the end ... moved to compat" | clear | `EndFireMixin`, `[melting] endNoFire` |
| I5 | "fleet footed fix and fov change ... moved to compat" | premise | Both are in Raspberry Pie, not Common: `solraspberrypie/mixin/FleetFootedMixin.java`, `client/FleetFootedFov.java`, `RpConfig` `fleetFootedFov` (W2-3) |
| I6 | "create gravity ... moved to compat" | conflict | Its dimension limit is listed under Performance and stability, which the owner keeps in Common (I10; W2-4) |
| I7 | "melting mechanics ... moved to compat" | clear | `melting/`, `MeltingBlockStateMixin`, `BiomeMeltMixin`, `VanillaLightMeltMixin`, `RareIceLightMeltMixin`, `[melting]` |
| I8 | "and any other gameplay changes i missed ... (ask for my review on missed first)" | open | Asked: pack switches, chat reporting, orphan item tabs, interface (W2-1) |
| I9 | "new composters, bee hive yield, moved to delight" | clear | `composter/` (12 woods), `BeehiveHoneycombMixin`, `[bees]` |
| I10 | "wandering trader changes moved to trade" | open | There is no Creative Trade jar: `WanderingTraderSpawnerMixin`, `WanderingTraderTradesMixin`, `[wandering_trader]` (W2-2) |
| I11 | "keep Performance and stability, all effects in common" | clear | |
| I12 | "dying or clearing a slot/respecing in rp can remove the foods from memory, they are no longer shown as grayed in history so keeping them is unneeded" | clear | Today death and paid slot actions move foods past the decay window but keep them in Apple Pie's memory (`logic/FoodWindow.java`, `ServerEvents.java:129`, `SlotActions.java`) |
| I13 | "the sol carrot book should have easiest/hardest sorting, hardest seems to be missing" | premise | `client/FoodDictionarySort.java` has EASIEST (complexity ascending) and no HARDEST; confirmed missing |
| I14 | "rp curseforge command lines render w/ &lt;, etc." | open | `project_pages/raspberry_pie.md` Commands spoiler uses `&lt;players&gt;` inside code; asked the notation (W2-7) |
| I15 | "set diet log extra width to 48 pixels" | clear | `RpConfig` `extraWidth` default is 47 |

## 1. Owner rulings

| # | Item (owner's words) | Class | Ruling | Date | Source |
|---|---|---|---|---|---|
| W2-1 | I8 other gameplay changes | open | All stay in Common: the pack switches (advancements, recipe book, creative menu), chat reporting and signing off, orphan item tabs, and the interface features (changelog viewer, loading tips, mod display names) | 2026-11-23 | AskUserQuestion 2026-11-23 |
| W2-2 | I10 trade | open | The wandering trader settings and their two mixins move to Creative Loot. Trade Sync and the trade helper stay in Common as library code | 2026-11-23 | AskUserQuestion 2026-11-23 |
| W2-3 | I5 Fleet-Footed | premise | The Fleet-Footed fix and its field-of-view scale move from Raspberry Pie to Creative Compat. Raspberry Pie keeps only diet features; standalone Raspberry Pie users without Compat get Alex's Mobs' bug back | 2026-11-23 | AskUserQuestion 2026-11-23 |
| W2-4 | I6 Create: Gravity | conflict | Split: low gravity for items, blocks and projectiles, the server-start loading fix and the ItemPhysic tie move to Compat. The per-dimension limit on its entity ticks (`[perf] gravityLimitDimensions`) stays in Common under Performance and stability | 2026-11-23 | AskUserQuestion 2026-11-23 |
| W2-5 | I11 | clear | Performance and stability, bug fixes, swap lists, KubeJS helpers and every effect stay in Common | 2026-11-23 | chat 2026-11-23 |
| W2-6 | I1 page layout | open | Family as a `###` header; each feature or tweak is a bold name line followed by its own spoiler holding the whole description. Applies to Common and Compat | 2026-11-23 | AskUserQuestion 2026-11-23 |
| W2-7 | I14 command notation | open | Command arguments in square brackets: `/solrp reset [players]` | 2026-11-23 | AskUserQuestion 2026-11-23 |
| W2-8 | I2, I3, I4, I7 | clear | Moved config keys keep the player's values: a one-time carry-over from `creativecommon-common.toml` into the new jar's file, as in PR-CC-27 | 2026-11-23 | AskUserQuestion 2026-11-23 (agent default confirmed) |
| W2-9 | I9 composter ids | clear | The 12 composters take Creative Delight's namespace (ids follow the owning mod); old Common ids swapped in saves | 2026-11-23 | AskUserQuestion 2026-11-23 (agent default confirmed) |

## 2. Phases

Rows: see the Open work block (generated). Order:

1. Common -> Compat carve: melting with End no-fire, glass shards, sack pick-up, Create: Gravity low gravity (W2-4). Then
   Raspberry Pie -> Compat: Fleet-Footed (W2-3). One code owner (`impl` session) for Common, Compat and Raspberry Pie.
2. Common -> Delight (composters, beehive yield) and Common -> Loot (wandering trader).
3. Raspberry Pie fixes: memory removal (I12), Hardest sort (I13), extraWidth 48 (I15).
4. Pages: Common and Compat rebuilt in the W2-6 layout from the moved state; Raspberry Pie commands (W2-7) and Fleet-Footed
   removed from its page. Changelog bullets for each player-visible move or fix.

Creative Delight and Creative Loot are not in a publish wave (D3, publish_readiness.md 2a). Moving the composters and the
wandering trader there removes them from a standalone Common.

## 3. History

- 2026-11-23: intake of the owner's wave 2 notes; rulings W2-1 to W2-7 asked, W2-8 and W2-9 agent defaults.
- 2026-11-23: W2-8 and W2-9 confirmed by the owner. Execution started together with wave 1 (one impl session; phase
  order in plan_wave1_publish.md section 4). The scratch run (W-P7) is the owner's to start; this session stops after W-P6.
- 2026-11-24: all rows built and Testing: carve to Compat (`#17`, `raspberry-pie-publish #25`; Compat class MovedKeys
  carries [melting] and the Fleet-Footed field of view over), to Creative Delight (`#18`; composter ids
  creativedelight:<wood>_composter, old ids swapped by common_composters.json; [bees] carried over), to Creative Loot
  (`#19`; first creativeloot-common.toml, carried over); Raspberry Pie `#26` to `#29`; pages `#20`, `creative-compat-publish
  #10`. Release jars in plan_wave1_publish.md W-P6. configs_edited still holds [melting], [bees] and [wandering_trader]
  under Common's file until the first boot moves the live sections; copy them to the new files' backups then.
