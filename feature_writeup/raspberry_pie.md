# Spice of Life: Raspberry Pie Addition (design)

Status: generated (feature_writeup/README.md index, the TODO rows above; install state `python3 tools/jarstamp.py`);
queue sections SQ-RP, RPI and RPS (section 14). Design decisions: owner 2026-11-10, two rounds on 2026-11-11 and two on 2026-11-12 (chat). TODO
`food/types-and-tuning #21` (selectable bonus), touches `#12` (ladder values: the pack ladder is a draft) and `#4`.
Build choices not in the design are in section 15. Waiting on rebuilds the owner does (those jars carry other unbuilt
commits): Creative Brewing (Lava Walker burn). The Diet Log name and Apple Pie's book + potato recipe removal moved
from Creative Delight into this jar on 2026-11-21 (12.9), and the `item_dict.js` keys `diet_log`, `liquid_diet_log`,
`food_dictionary` were deleted from Common.
Existing worlds (Workspace, Structures, New World) keep their old `serverconfig/` copies of Apple Pie's settings
(benefits, `resetOnDeath`, blacklist, `minFoodsToActivate` 10): edit them by hand, or run
`python3 tools/gen_sol_drink_blacklist.py --worlds` for the blacklist.

Jar facts below were checked on 2026-11-11 in `solapplepie-1.19.2-2.2.0.jar` (package `com.tarinoita.solsweetpotato`)
and Forge 1.19.2-43.5.1. Pack Apple Pie settings: `queueSize` 256, `startDecay` 0, `endDecay` 32 (30 from 2026-11-19 to 2026-11-20),
`minContribution` 0.0, `shouldForbiddenCount` true.

## 0. At a glance

<!-- generated:status_index begin open_work sha1=67e10741c793 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 54 open TODO rows (Planned 4, Testing 50), 4 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `food-tuning #15` | Planned |  | Food Dictionary: hand-made type sort mirroring the JEI food family order (after that is done) |
| `food-tuning #46` | Planned | S C | RP standalone P9: isolation tests RPS-01 to -07 and RPI rerun |
| `food-tuning #58` | Planned | S C | Harmony: a new source (not adzuki, not the diet ladder), e.g. a rare passive mob; applies Emptiness and hostile mob flee |
| `food/types-and-tuning #35` | Planned | C | Food log texture instead of the fluid log texture |
| `food-tuning #2` | Testing | S P | Diet window 30 meals (endDecay 32 -> 30), respec ages out 5 (was 8) |
| `food-tuning #3` | Testing | S C | Diet Log: Apple Pie food history pages removed; last 30 meals list (duplicates shown) |
| `food-tuning #4` | Testing | C | Diversity shown rounded down (4.97 showed 5 before milestone 5) |
| `food-tuning #5` | Testing | C | Diet Log: per-page x, y, scale config; ladder and milestones one line lower |
| `food-tuning #6` | Testing | C | Diet Log: book background stretched by repeating its center strip, configurable width |
| `food-tuning #7` | Testing | C | Diet Log: mouse wheel flips pages |
| `food-tuning #8` | Testing | C | Diet Ladder bar: even spacing, last achieved milestone + next 4 |
| `food-tuning #9` | Testing | C | Diet Log icons: locked unlocks and unreached permanent buffs alternate their icon and the effect; unlocked effect gray with colored frame; slot rungs metal frame, no effect; active buffs green in Milestones |
| `food-tuning #10` | Testing | C | Gate text: N more unique food(s) to activate diet |
| `food-tuning #11` | Testing | C | Effect screen: smaller Change/Drop label text (buttons unchanged); hover shows how many foods age out |
| `food-tuning #13` | Testing | C | Food Dictionary: uneaten sort configurable (mod+name, name, easiest, hardest) |
| `food-tuning #14` | Testing | S C | Food Dictionary: eaten list 3-state sort button (newest unlock, latest eaten, uneaten order) |
| `food-tuning #20` | Testing | S C | Diet window back to 32 meals (endDecay 30 -> 32); meals grid 8 x 4 |
| `food-tuning #21` | Testing | C | Diet Log meals grid: label centered; hover shows food total only when it differs, before the struck-through meal value |
| `food-tuning #22` | Testing | C | Diet Log 25% wider by default (was 50%); Ladder and Milestones content 17.5% in from each side; meals grid icons 20% larger; first-page diversity and gate text scaled to fit on one line each |
| `food-tuning #23` | Testing | C | Diet Log freezes briefly on every open: measure and cache |
| `food-tuning #24` | Testing | C | Liquid Diet Log pauses the game in single player |
| `food-tuning #25` | Testing | S P | Effect unlocks (and their messages) wait until the unique-food gate first opens |
| `food-tuning #26` | Testing | C | Diet Log diversity icon: eaten non-drink food closest to the window's average meal value (newest on a tie); else seeds, moss, rotten flesh, spider eye or dandelion leaf |
| `food-tuning #28` | Testing | C | Food tooltip line Diversity: X (contributes Y); diversity printed with two decimals, rounded down, everywhere |
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
| `food-tuning #51` | Testing | S C | Steady (Astringent): stops every slip effect (ice, soap), not dripleaf or pressure plates; JEI description |
| `food-tuning #52` | Testing | S C | Leaf Piercing: leaf particles of the pierced block trailing the arrow, a leaf poof where it enters |
| `food-tuning #53` | Testing | S C | Prickly II: tamed and passive mobs exempt (level I: tamed only); JEI description |
| `food-tuning #54` | Testing | S C | Lava Walker: crouch to drop into lava |
| `food-tuning #55` | Testing | S C | Fleet-Footed: lower field-of-view shift while its speed applies |
| `food-tuning #56` | Testing | S C | Display names: Astringent -> Steady, Poison Resistance -> Poison Immunity, Fire Resistance -> Fire Immunity |
| `food-tuning #57` | Testing | S C | Emptiness holds Patrol Nullifier and Phantom Bane as sub-effects |
| `food-tuning #59` | Testing | C | Diet Log polish: gate warning on one line, Ladder page bar and key lower (stacked icons clear the Highest line), Milestones text column after the widest value, Diet Log and Food Dictionary tooltip lines |
| `food-tuning #60` | Testing | S C | Pack ladder milestones are whole numbers, distinct values kept distinct (gen_rp_ladder.py decimals 0, least-distance spread) |
| `food-tuning #61` | Testing | C | Bug fixed: food tooltips were an empty zero-size frame on launches where Iceberg's title break ran before the Raspberry Pie reorder (rows landed in the Legendary Tooltips title) |
| `food/types-and-tuning #21` | Testing | S C P T | Spice of Life selectable unlock bonus |
| `food/types-and-tuning #29` | Testing | C | Raspberry Pie item tooltip: "Doesn't run programs" |
| `food/types-and-tuning #30` | Testing | S | Raspberry Pie: player reset command |
| `food/types-and-tuning #32` | Testing | C | Diet Log progress bar: windowed milestones |
| `food/types-and-tuning #33` | Testing | S P | Extra hearts at 0 diversity; damage when max health is lowered |
| `food/types-and-tuning #36` | Testing | C P | Minimum foods to activate shown wrong in the book |
| `food/types-and-tuning #37` | Testing | C | Diet Log milestone icons and colors missing |
| `food/types-and-tuning #38` | Testing | C | Apple Pie pages still at the end of the Diet Log |
<!-- generated:status_index end open_work -->

## 1. What it is

A new in-tree mod that adds selectable diet bonuses on top of Spice of Life: Apple Pie Edition. It is an add-on that
mixes into Apple Pie, not a fork: no Apple Pie code is copied in.

| | |
|---|---|
| Display name | Spice of Life: Raspberry Pie Addition |
| Mod id | `solraspberrypie` (permanent; the display name may change, the id may not) |
| Tree | `development/solRaspberryPie` (root `settings.gradle`, `build-logic/`) |
| Sides | both (screens, widget, overlay messages on the client; all rules on the server) |
| Depends on | `solapplepie` 2.2.0 exactly (mixins and accessors on its private members); nested in the jar since 2026-11-21 with Carrot Edition 1.14.0 (optional), section 12.9 |
| License | own code GPL-3.0-only; Apple Pie and Carrot are LGPL-2.1 (checked at the source, plan_rp_standalone.md P0) and ship nested unmodified with their license texts and a `NOTICE`; credit Apple Pie and its forks |
| `mod_sides.md` | row `solraspberrypie` (both); the nested ids take its side |

Carrot Edition: the GitHub text the owner found is the GTNH 1.7.10 fork (LGPL-3.0, original code public domain). Our
1.19.2 jar declares `LGPL-2.1` in `mods.toml`; CurseForge says All Rights Reserved. Route: **copy no Carrot code.**
The progress line (6.3) and the Liquid Diet Log (7) are written from scratch, modeled on behavior only, so the
license question does not matter.

## 2. Who owns what

| Owner | Owns |
|---|---|
| Apple Pie | every diversity calculation, food memory (unique foods with "meals since last eaten", forgotten after 256 meals), the decay window, the `foodsEaten` counter and `minFoodsToActivate` value, the Diet Log book and its pages |
| Raspberry Pie | the whole bonus ladder: health, armor toughness, knockback resistance, flat effects, slot milestones, effect unlocks; the `minFoodsToActivate` gate (reads Apple Pie's counter); applying and removing every rung; slot choices; ageing foods out of the window (respec and death, 6); messages; the inventory widget; the effect screen; the Liquid Diet Log |
| Apple Pie config | `benefitsUnparsed` and `thresholds` emptied; Apple Pie applies nothing itself (verify when built that its parser accepts two empty lists) |
| Apple Pie screens | show Raspberry Pie's ladder (mixins on its client pages, 6.3) |

Item names: Apple Pie's book is renamed **Diet Log** (was Diet Tracker; by this jar's lang overrides since 2026-11-21, 12.9, `names.js` in the Creative Delight tree before; the
changelog bullet "called Diet Tracker" gets rewritten). The new drink book is the **Liquid Diet Log** (Raspberry Pie item).

## 3. The ladder

Every value is configurable: threshold, type, magnitude, order. One JSON file per world,
`serverconfig/solraspberrypie/ladder.json`, seeded on first load from `defaultconfigs/solraspberrypie/ladder.json`
(the same per-world pattern as Apple Pie's own settings). Read **once at server start**; edits apply after a restart
(owner, 2026-11-11), so there is no reload command or listener. Synced to each client on login. Nested data is why it
is JSON, not a TOML string list like Apple Pie's.

Four kinds of rung, which can share thresholds:

| Kind | Example | Below the threshold | Shown on the effect screen |
|---|---|---|---|
| Health tier | +1 heart | removed | no |
| Attribute tier | armor toughness +2, knockback resistance +1 | removed | no |
| Flat effect | Speed I between slot 2 and slot 3 | removed (the "permanent buffs" in the Diet Log legend; permanent means not chosen, not never-lost) | yes, before the slots, different style |
| Slot milestone | slot 3 at diversity 30 | slot switches off (grayed), its choice is kept | yes |
| Effect unlock | Strength II at 70 | **never lost** ("one-time unlocks"): driven by the player's max diversity | in the picker |

Effect unlocks: per player we store **max diversity ever reached**, mapped onto the current ladder, so ladder edits
apply to existing players. Death and ageing foods out never lower it.

Below `minFoodsToActivate` (Apple Pie's `foodsEaten` counter, 5 in the pack) no rung is active.

Draft ladder shape (values to be set with `#12`): health every few tiers, toughness and knockback tiers spread
between, 4-5 slots, flat Speed I between slot 2 and 3, effect unlocks spread across the whole range with higher
levels later.

### 3.1 Attribute modifiers and Apple Pie leftovers

- Apple Pie 2.2.0 applies its attribute benefits with `addPermanentModifier` and a **random UUID per config parse**.
  Permanent modifiers are saved in player data. Apple Pie removes them on logout, but a crash or kill while a player
  is online saves them, and once `benefitsUnparsed` is empty Apple Pie can no longer find them. Raspberry Pie strips
  any Apple Pie attribute modifier (matched by modifier name) from a player on every login, so existing worlds are
  cleaned up too.
- Raspberry Pie's own health, toughness and knockback tiers use **transient** modifiers with fixed UUIDs, re-applied
  on login, respawn and dimension change. Nothing it adds is written to player data, so removing the mod leaves no
  stuck hearts.

### 3.2 Effects that are not loaded

Revised 2026-11-12: **no heart swap.** The ladder is defined by the pack (the modpack ships its own
`ladder.json`), and Raspberry Pie ships only a **simple default ladder** that uses vanilla effects. A ladder entry
naming an effect that is not registered (its mod removed, or a typo) is **skipped with a warning** in the log listing
every skipped entry. A stored slot choice naming a missing effect is cleared (the slot is empty; filling is free).

### 3.3 Ladder validation (owner to choose per row)

What the server checks when it reads `ladder.json` at start. Each row: what can go wrong, an example, what a player
would see if nothing checked it, and the proposed handling. "Warn" = one log line at start naming the entry.

| # | Case | Example | Unchecked result | Proposed |
|---|---|---|---|---|
| V1 | Effect id not registered | Better Beacons removed, `betterbeacons:long_reach` unlock | crash or a dead rung | **decided 2026-11-12**: skip the entry, warn (3.2; the +1 heart idea was dropped) |
| V2 | Attribute id not registered | a modded attribute typed into a tier | crash or dead rung | **decided 2026-11-12**: as proposed in 3.3 (recommended) |
| V3 | Unlock for an effect that is not in the slot pool | Resistance II unlock (Resistance is never slottable) | an unlock that can never be used, shown in the picker | **decided 2026-11-12**: as proposed in 3.3 (recommended) |
| V4 | Pool effect with no unlock anywhere | Luck in the pool, no Luck rung | Luck can never be picked | **decided 2026-11-12**: as proposed in 3.3 (recommended) |
| V5 | Unlock level above the effect's cap | Regeneration II unlock, cap I | picker offers a level the slot refuses | **decided 2026-11-12**: as proposed in 3.3 (recommended) |
| V6 | Level gap | Strength II at 50, Strength I at 70 | between 50 and 70 the player has II but not I; the free level change (5) has nothing to step down to | **decided 2026-11-12**: as proposed in 3.3 (recommended) |
| V7 | Slot milestones out of order or on the same threshold | slot 3 at 20, slot 2 at 30 | slot 3 active while slot 2 is not, breaking bottom-up filling (5) | **decided 2026-11-12**: as proposed in 3.3 (recommended) |
| V8 | Fewer slots than before the restart | 5 slots -> 4, a player has a choice in slot 5 | a choice in a slot that no longer exists | **decided 2026-11-12**: as proposed in 3.3 (recommended) |
| V9 | Unlock moved higher, or removed, after players earned it | Strength II moved 70 -> 90, a player at max 80 has it slotted | an unlock "never lost" is lost through an edit | **decided 2026-11-12** (Q-G): keep a slotted unlock until the player changes it |
| V10 | Refresh-unsafe effect as a flat bonus or in the pool | Absorption, Health Boost, instant effects | refilled absorption, clamped health, instant effect every 20 s (4, 9.1) | **decided 2026-11-12**: as proposed in 3.3 (recommended). Exception (owner, 2026-11-21): `minecraft:saturation` (Satiated) is allowed as a flat rung, never in the pool; it ticks every tick while active, so a refresh only extends it |
| V11 | Same effect in a flat bonus and the pool | flat Speed I, Speed in the pool | **decided 2026-11-12**: allowed. The pack unlocks the effect as slottable first and as a flat bonus later; once the flat bonus is active the effect is hidden from the picker. A slot that already holds it keeps it (5.1, 9.5) | allow |
| V12 | Threshold higher than any player can reach | a rung at 500 | an unreachable rung | **decided 2026-11-12**: as proposed in 3.3 (recommended) |
| V14 | Unlock hidden by an early flat rung | flat Speed I at 1, first slot at 5, Speed I unlock | the unlock can never be slotted | warn (PR-RP-11, 2026-11-22) |
| V13 | File missing or malformed JSON | a stray comma | server cannot build a ladder | **decided 2026-11-12** (Q-H): fall back to the built-in default ladder with a loud error |

**File format.** `pool`: entries `{"effect", "max_level"}` (cap, default 1). `rungs`: `{"threshold", "type"}` plus, per
type, `health` `amount` (health points, 2 = one heart); `attribute` `attribute`, `amount`; `slot` nothing; `flat` and
`unlock` `effect`, `level` (default 1). Optional fields (plan_rp_standalone.md P7, 2026-11-21), all ignored by older
copies of the file:

| Field | On | Meaning |
|---|---|---|
| `"toggle": "on"` / `"off"` | a pool entry or a flat rung | the effect is a toggle: the player switches it on or off (free, any time it is granted) instead of changing its level; the value is the state for a player who never switched it. The switch is per effect and player (saved, kept on death, cleared by `/solrp reset`), so a flat and a slotted copy of one effect share it. Pool and flat values for one effect disagreeing: the flat rung's wins (one warning); any other value reads as `on` (one warning) |
| `"replaces": ["<effect id>", ...]` | a pool entry | once this effect is unlocked (any level), the listed pool effects leave the picker; a slot holding one is covered (grayed, not applied, changing it is free) and one yellow chat line names the pair at the unlock (`food-tuning #49`). Ids not in the pool are ignored (quietly when their mod is absent) |
| `"tagline": "<lang key>"` | a flat or unlock rung | a short hover line under the effect's name in the effect screen (flat icons, slots, picker) and the inventory widget. An unlock rung's tagline covers its level and the levels above it that have none; a flat rung's covers its own level. No field = no line |

A toggle effect that is switched off stays granted: a flat one keeps its icon (dimmed, a red bar under it; green when
on), a slotted one keeps its slot (dimmed, the slot's On/Off button replaces the level buttons); neither is applied.
The default ladder uses none of these fields. The pack's file is generated (`tools/gen_rp_ladder.py`, section 12.10).

## 4. Slot pool (default)

Two ladders exist. The jar's built-in default (`default_ladder.json`, used only when no `defaultconfigs` copy exists)
pools eight vanilla effects: Jump Boost (cap III), Night Vision, Water Breathing, Luck, Regeneration, Fire Resistance,
Slow Falling, Glowing (12.12). The pack's ladder (`defaultconfigs/solraspberrypie/ladder.json`, generated, 12.10) pools
the pack's effects, Fire Resistance included as an unlock. The rules below decide what either may pool.

Slottable: Speed, Haste, Strength, Jump Boost, Night Vision, Water Breathing, Luck, Regeneration (level cap 1),
Shrinking (Fruits Delight), every pack beacon effect (vanilla beacon effects except Resistance; Better Beacons'
Long Reach, Patrol Nullifier, Phantom Bane).

Not slottable:

| Effect | Why |
|---|---|
| Resistance (any level) | owner rule; overrides the "all beacon effects" rule |
| Instant effects: Instant Health, Saturation, Instant Breath, Restfulness, Teleport | fire again on every refresh |
| Absorption, Health Boost | a refresh refills absorption / clamps health |
| Nourishment, Comfort, Saturation | remove the reason to eat varied food (Saturation, shown as Satiated, is allowed as a permanent buff only, V10 exception, owner 2026-11-21: the pack ladder's last rung at 100) |
| Invisibility, Hero of the Village | remove a mechanic (mobs and PvP, villager and Lightman's Currency prices). Fire Resistance was on this row; it is an unlock on both ladders since 2026-11-21 |
| Dolphin's Grace, Conduit Power | owner rule |
| Caffeinated, Rest, Ender Phasing, Raging | logic on expiry or removal would fire on every switch-off; check each before allowing. Stimulation was read 2026-11-23 and is slottable (its expiry only removes Slowness and Mining Fatigue) |
| Lava Walker (Vinery) | slottable only after a Vinery change: walking on lava burns like a magma block unless the player has Fire Resistance, and dropping into lava burns as usual. Vinery is our port in Creative Brewing (`members/vinery`), so the change is ours to make. **Decided 2026-11-12:** the change goes into the Vinery port (a player-visible behavior change there; note it in that member's writeup and the changelog when built) |
| Fleet-Footed (Alex's Mobs) | **slottable with a mixin fix (decided 2026-11-12).** +20% move speed only while sprinting and airborne, so a jump-sprint boost. Its timer state lives in fields on the shared effect object, so in multiplayer all players with it share one timer (flicker or a stuck bonus). **Fix:** a mixin moves that state into per-player data (see the Fleet-Footed fix below the table) |

Shrinking and Speed change the player (size, field of view) every frame and will be hidden from the HUD (9.5);
owner chose to allow both.

## 5. Player choices and costs

The only player choices are **which effect at which level in which slot.** Everything else follows the diet.

### 5.1 Slot rules

- **Active** slot: its milestone is met. **Grayed** slot: its milestone is not met (never reached, lost, or after
  death). A grayed slot keeps its stored choice but **cannot be interacted with**: no fill, change, level change or
  drop, and it is not a drop target on the effect screen.
- **Active filled slots are packed from slot 1 upward.** Adding an effect to any empty active slot puts it in the
  **lowest** empty active slot (auto-shift), whichever slot the player clicked or dragged onto. Grayed slots keep their
  stored choice in place and are not shifted, so a grayed empty slot can sit below grayed filled ones (12.1 Q-B,
  decided 2026-11-12 as option b).
- One effect per slot. An effect already in a slot is not offered for another slot (Q-D, decided 2026-11-12), and an
  effect with an active flat bonus is hidden from the picker (V11). **A slot that already holds an effect when a flat
  bonus for it is added keeps it** (decided 2026-11-12): it is covered, not cleared, so a player hovering around the
  flat bonus threshold keeps the choice; if the flat bonus is lost the slot effect is active again. The effect is
  applied once, at the higher of the two levels. The player can re-slot it (change it) as normal.
- Filling a slot or changing its level shows **no message** (decided 2026-11-12).

### 5.2 Actions

| Action | Cost |
|---|---|
| Fill an empty active slot | free; lands in the lowest empty active slot (5.1) |
| Change a slot's level (any unlocked level of the same effect) | free |
| Change a slot to a different effect | age out the **5 oldest foods in the decay window** (configurable `respecFoods`; 8 from 2026-11-13 to 2026-11-19, 5 before) (6) |
| Drop (clear) a slot | age out foods from the oldest end of the window **until the highest active slot is lost, and only that slot** (a food that alone would also take a lower slot is kept and the next oldest goes; the `/solrp diversity set` Adjustment entry is lowered to just under the highest slot's milestone instead, 12.16). The dropped effect is removed and the choices of the **active** slots above it shift down one slot to fill the gap; the vacated highest active slot is left **empty and grayed** (owner, 2026-11-11). Choices stored in grayed slots stay where they are (Q-B option b, 2026-11-12) |
| Swap two slots | not possible |
| Re-applying a regained slot's stored choice | automatic, free |

Drop example: slots 1-3 active with Speed, Haste, Luck; slot 4 grayed and empty. Drop slot 1: foods age out until
slot 3 is lost; Haste moves to slot 1, Luck to slot 2; slot 3 is empty and greyed.

There is no XP cost: the diversity cost is always there.

### 5.3 Why the oldest, and why the cost climbs

The newest foods are the easiest to replace (the player just had them), so the oldest go. With the pack's decay a food
eaten k meals ago is worth `complexity x (1 - k/30)` (Apple Pie's `calculateTimePenalty`), so the oldest foods are
worth little and the first respec is cheap. Aged-out foods have left the window, so the next respec takes the next 5
oldest, which are newer and worth more. Example, 30 distinct complexity-1 foods in the window (diversity 15.5;
window 30 and respec 5 since 2026-11-19, plan_diet_books.md 1.1):

| Respec (no eating between) | Foods aged out (meals ago) | Diversity lost | Left |
|---|---|---|---|
| 1st | 25-29 | 0.5 | 15.0 |
| 2nd | 20-24 | 1.3 | 13.7 |
| 3rd | 15-19 | 2.2 | 11.5 |
| 4th | 10-14 | 3.0 | 8.5 |
| 5th | 5-9 | 3.8 | 4.7 |
| 6th | 0-4 | 4.7 | 0 |

Eating refills the window from the new end, so the climb eases off over roughly 5 meals per respec. Repeated foods and
complexity weights change the numbers, not the shape. The first respec is meant to be a near-free correction;
serial swapping is what costs. Small windows (decided 2026-11-12): the change is **allowed** even when the respec switches off the slot being changed; the new choice is stored in the now grayed slot and shown on the confirm panel as such.

### 5.4 Confirm panel

Every paid action shows a confirm panel before anything happens: diversity now and after, the foods that will age out
(icons), and **a list of every effect, slot, flat bonus and tier that will be lost** (owner, 2026-11-12: a respec shows the lost effects before it is accepted). An effect that stays active through an existing flat bonus is shown in yellow as `{effect} effect sustained by existing bonus`, not as lost. Everything listed happens **on confirm**, at once
(no grace period, no later check).

- The server builds the preview: a copy of the player's food memory with the new counts, run through Apple Pie's
  static `foodDiversity(set)`. Never `simulateFoodAdd` (it is a different calculation for "eat one more food").
- The preview carries a state stamp (a counter Raspberry Pie bumps on every eat, paid action and death). On confirm
  the server compares it; if anything changed in between, it rejects the confirm and sends a fresh preview.

## 6. Ageing foods out

### 6.1 Rule

Respecs and death **remove the aged-out foods from Apple Pie's food memory** (raspberry-pie-publish #26, 2026-11-23;
until then they were moved past `endDecay` and kept). An aged-out food:

- counts nothing and leaves the Diet Log (its meals drop off the Last meals grid);
- counts in full again when eaten, as a new food.

"Oldest in the decay window" = the foods with the largest count still below `endDecay`. If fewer foods than needed are
in the window, all of them go. Foods past the window that were never aged out stay in Apple Pie's memory as before.
Q-E (a warning at server start when `minContribution` is above 0, decided 2026-11-12) still applies: foods past the
window then count a little.

### 6.2 Implementation

- Accessor mixin on `FoodList.uniqueFoods` (private `Map<FoodInstance, Integer>`); Raspberry Pie removes the aged-out
  entries (`FoodWindow.ageOut`), then calls Apple Pie's public `CapabilityHandler.syncFoodList(player)`, since Apple Pie only syncs on eat, respawn,
  dimension change and login.
- No extra NBT key, no diversity redirect, no tooltip mixin: Apple Pie's own diversity, Diet Log and tooltip
  (`calculateDiversityContribution(food, count)`) all read the moved count and show the right value.

### 6.3 Diet Log changes (client mixins on Apple Pie pages)

- Button on the Diet Log that opens the effect screen.
- Milestone page: Raspberry Pie's ladder instead of Apple Pie's config list (Apple's is empty).
- **Progress line** (behavior like Carrot's food book, own code): a bar across diversity with icons at each rung:
  heart (health), knockback resistance, armor toughness, slot, flat bonus (permanent buff), one-time unlock.
  Reached rungs colored, unreached gray, hover for the value. Since 12.4 the bar covers a window (the last milestone
  reached and the next few), icons as listed in 12.4 #37 and since 2026-11-17 in 12.5; Apple Pie's own benefit pages are removed.
- **Single page, scaled** (12.5): the book is drawn at client config `[dietLog] bookScale` (default 1.25), shrunk to
  fit small windows. The double page view (12.4 #31) is removed.
- **Since 2026-11-19** (12.6): pages diversity (with the last 30 meals grid), Diet Ladder, Milestones (Apple Pie's
  food history pages removed); each page type placed and sized by client config `[dietLog.pages.*]`; the background stretchable by
  `[dietLog] extraWidth`; the mouse wheel flips pages.
- **Since 2026-11-20** (12.7): the book 50% wider by default, the first page's diversity and gate lines fitted, an
  8 x 4 grid of the last 32 meals, a new diversity icon rule, two-decimal diversity everywhere, one timing line per open.
  Later the same day (owner notes): the book 25% wider instead of 50%, Ladder and Milestones content 17.5% in from
  each side, meals grid icons 20% larger, active buffs green in the Milestones list, permanent buffs alternate their
  icon with the effect until reached, meal hover drawn as plain text (fixes the missing hover). Then: permanent buffs
  cycle reached too, "Full meal" hover line, a wider max-diversity mark, reached unlock numbers not struck,
  `/solrp reset` with no player resets the caller.

## 7. Liquid Diet Log

Rewritten 2026-11-12 (was alcohol only).

- **Every drink** is out of the normal diet: juice, tea, coffee, milk, wine and other alcohol, potions and the like.
  A drink has **0 diversity** and **no effect on food diversity**: no diet credit, it does not age other foods, does
  not count toward `minFoodsToActivate` (the foods-eaten counter), and does not show in the Diet Log.
- Why a blacklist alone is not enough: with `shouldForbiddenCount` true, Apple Pie's `addFood` still ages every
  remembered food by one meal (and bumps `foodsEaten`) when a blacklisted item is eaten; it only skips adding the item.
  32 drinks in a row would take diversity to 0. The flag is global, so it stays. Raspberry Pie injects at the head of
  `addFood` and returns early for items in the drink tags. The generated blacklist stays too, so Apple Pie's tooltip
  marks those items as not counting.
- They are tracked in the Liquid Diet Log: a book item in Raspberry Pie with its own player data, **unique drinks**,
  the last 256 (like Apple Pie's own log, not a plain history of every sip), history only. It gives the player
  **nothing**: no health, no effects. The drinks keep their own effects (tipsy and so on).
- Recipe `solraspberrypie:liquid_diet_log_manual_only`: a book and any drink in `#solraspberrypie:drinks`. The
  tag holds canned drinks, so the id carries `_manual_only` (Create keeps it out of automation, the pack's can guard;
  renamed 2026-11-20 from `liquid_diet_log`, r1120nv SQ-CNP-01, TODO `canning #22`).
- Source list: the pack's drink tags (`#creativedelight:food_type/drink/...`, alcohol and the non-alcoholic ones;
  which tags cover tea, juice, potions and milk is to be checked when built, add any missing). A generator (listed in
  `tools/generators.json`) writes Apple Pie's blacklist from them, so the two never drift. Like `minFoodsToActivate`,
  the blacklist only reaches new worlds through `defaultconfigs/`; existing worlds need their `serverconfig/` edited.
- To verify when built: Apple Pie only tracks items whose `isEdible()` is true (`FoodTracker.onFoodEaten`). List which
  drink-tag items are edible; any that are not (potions, for one) never reached Apple Pie and need no fix, but still
  need the Liquid Diet Log hook. The log hooks `LivingEntityUseItemEvent.Finish`; drinks taken from a placed block (if
  any exist) need their own hook.
- **Obtaining the books** (decided 2026-11-12): the **Liquid Diet Log** is crafted from a book and any drink (an item
  in the drink tags); the **Diet Log** (food) is crafted from a book and any food item. Both are shapeless. Check
  against Apple Pie's own Diet Log recipe when built (replace or add alongside). Both books also go into the **starting
  player inventory** (given once to a new player; the mechanism for starting items is checked when built). Anyone who
  loses a book can craft another, so nothing extra is needed for existing worlds or after death.

## 8. Timing

Diversity only changes when a food is eaten, on a confirmed paid action, and on death; never with time. Rungs are
therefore evaluated at exactly those moments. There is **no periodic loss check and no grace period** (both dropped
2026-11-11).

| Event | What happens |
|---|---|
| Food eaten | every rung re-evaluated at once: gains and losses both apply immediately (flicker accepted for now, to be judged in play: Q-A, RPI-23) |
| Paid action confirmed | everything listed on the confirm panel applies at once (5.4) |
| Death | Apple Pie's `resetOnDeath` set to false; Raspberry Pie ages the whole window out (6.1) **and resets the foods-eaten counter** (the gate of `minFoodsToActivate`; decided 2026-11-12), after Apple Pie's `onClone` copy (event priority, or on respawn). On respawn every rung except one-time unlocks is off, every slot grayed with its choice kept, max diversity unchanged, and **one** message (10) |
| Login | rungs re-evaluated and re-applied from current state; transient modifiers mean logout leaves nothing behind |
| Ladder edit | applies at the next server start (3) |

## 9. Applying effects

### 9.1 Refresh

- Raspberry Pie applies every flat and slot effect itself: duration 40 s (800 ticks), **re-added unconditionally
  every 20 s** (400 ticks, owner choice), staggered across players, no particles. Regeneration's duration is set by
  the rule in 9.2.1.
- Cost: Apple Pie's own method (one effect lookup per player, per benefit, per tick; duration 300, re-added at 200
  ticks or less) is already cheap. The 20 s rule removes even that, for one packet per effect per player every 20 s.
- Re-adding re-runs the effect's attribute modifiers (same id, so harmless for Speed, Strength, Haste, Luck); it is
  why Absorption and Health Boost are excluded.
- Identifying our instance: each one carries the vanilla cure list (milk) **plus a marker item** of Raspberry Pie's in
  its curative items. Nothing ever cures with the marker; it only lets the server tell ours apart (9.4).

### 9.2 Duration and per-tick logic, effect by effect

"Ticks every tick" means the effect runs code each tick while active, independent of how much time is left; only the
rows marked **duration-dependent** care about our refresh.

| Effect | Logic | Refresh impact | Verdict |
|---|---|---|---|
| Speed, Haste, Strength, Luck | attribute modifiers only | none | slottable |
| Jump Boost, Water Breathing, Long Reach, Patrol Nullifier, Phantom Bane | no tick logic; other code checks "has effect" | none | slottable |
| Regeneration | **duration-dependent:** heals when time left is a multiple of 50 ticks (25 at level II) | a reset off the 20 s rhythm gives an early heal; fixed by 9.2.1 | slottable, level cap 1 |
| Night Vision | **duration-dependent (client):** the screen flickers when 10 s or less is left | never below 20 s left | slottable |
| Shrinking (Fruits Delight) | attribute modifiers, then recalculates the player's hitbox on add and on remove | each refresh removes and re-adds in one tick: the hitbox grows and shrinks again in the same tick. Test in a 1-block gap for suffocation or a push | slottable after RP test |
| Fleet-Footed (Alex's Mobs) | **duration-dependent:** saves "last duration" into a field on the shared effect object and removes its speed modifier when the duration stops changing as expected | a refresh jumps the duration, and all players share the one field | slottable once the mixin fix is in (see below) |
| Lava Walker (Vinery port) | ticks every tick: lifts the player out of lava and makes the lava surface solid; no duration logic | none | slottable after the burn change (4) |
| Lava Vision (Alex's Mobs) | ticks every tick while time is left; no duration logic | none | slottable if wanted (not in the default pool) |
| Caffeinated (Farmer's Respite) | ticks every tick: rewrites the "time since rest" stat (phantom timer); Creative Delight already patches it | none from the refresh, but it controls phantoms | not slottable |
| Rest (Create Confectionery) | ticks every tick: removes phantoms within 4 blocks (named "expires" but runs every tick) | none | not slottable (removes a mechanic) |
| Stimulation (Create Confectionery) | ticks every tick through its "expires" procedure | none | slottable (procedure read 2026-11-23: expiry removes only Slowness and Mining Fatigue, so switching it off is safe) |
| Ender Phasing (Ender's Delight) | ticks every tick: damages the player in water or rain | none; harmful side | not slottable |
| Restfulness (Upgrade Aquatic) | instant-style tick | fires on every refresh | not slottable |

### 9.2.1 Regeneration rhythm

How vanilla does it: every tick, Regeneration I checks the **time left**; if it is a multiple of 50 it heals 1 HP,
then the time left drops by 1. It does not count time since the last heal.

On the plain 20 s rhythm this is fine. Added at 800, it heals at 800, 750, ... 450 left. The re-add comes when 400 are
left and resets to 800, which heals on the next tick: 50 ticks after the previous heal (49 if the re-add runs before
the effect's tick in the same game tick). Even rhythm.

What breaks it: any reset that does **not** land on that rhythm. All of these reset the time left to 800 at an
arbitrary point:

- the next-tick re-add after milk, a totem or `/effect clear` (9.3);
- filling a slot, regaining a slot, login, respawn;
- a same-level potion merging, or our instance resurfacing after a stronger potion ends (9.4).

800 is a multiple of 50, so the very next tick heals, however long it has been since the last heal: the gap is
anywhere from 1 to 50 ticks. Effect: **one early heal (1 HP) per off-rhythm reset.** Never two heals in one tick (one
check per tick), never a skipped heal. Drinking milk over and over (32 ticks a drink, re-added the tick after) heals
about every 33 ticks instead of 50: a small, farmable boost.

Fix (part of the plan): Raspberry Pie keeps a per-player Regeneration anchor (the game tick of the first add). Every
add or re-add uses duration `800 - ((now - anchor) mod 50)`, so the next heal always falls on the original 50-tick
rhythm, whatever caused the reset. The plain 20 s refresh is unchanged by this (it already lands on the rhythm). The
"keep both numbers multiples of 50" rule is no longer needed for correctness.

### 9.3 Effect clears and cures

**Nothing is cancelled.** Milk, totems, `/effect clear` and every other cure or clear remove diet effects as usual;
Raspberry Pie **re-adds them on the next tick** (owner, 2026-11-11).

How: Forge fires `MobEffectEvent.Remove` for clear-all (`removeAllEffects`, used by totems and `/effect clear`),
single removals (`removeEffect`) and item cures (`curePotionEffects`, milk) (checked in Forge 43.5.1). When the
removed instance is ours (marker, 9.1), the player is flagged and every active diet effect is re-added the next tick.
Our own removals (switch-off) set a flag so they do not trigger a re-add. Mods that edit the effect map directly skip
the event; the 20 s refresh restores those within 20 s.

| Source | Mod | Result |
|---|---|---|
| Milk Bucket | vanilla | removed, back next tick |
| Wooden Milk Bucket | Creative Delight | removed, back next tick |
| Splash and lingering milk bottles, thrown milk bottle | Splash Milk | removed, back next tick |
| Open-ended pipe spraying milk | Create | removed, back next tick |
| Warp plate | Waystones (purpose to read) | removed, back next tick |
| `/heal` | FTB Essentials (admin) | removed, back next tick |
| Balm helper | Balm (used by Balm mods; to read) | removed, back next tick |
| Totem of Undying | vanilla | removed, back next tick (see below) |
| Charm of Undying (totem in a curio slot) | Charm of Undying | removed, back next tick |
| Absolution | Botania | removed, back next tick |
| Canned Powdered Milk (`clear_effects`) | Creative Delight (`cans.js`) | removed, back next tick |
| Collective gear helper | Collective (used by Serilum mods; to read) | event or 20 s refresh |
| Any KubeJS script calling clear-all | KubeJS | removed, back next tick |
| Strong Hot Cocoa, Milk Bottle (single removals) | Farmer's Respite, Neapolitan | removed, back next tick |
| `/effect clear` | vanilla command (admin) | removed, back next tick (admin escape: `/solrp pause <player>`, 12.3) |
| Death | vanilla | gone with the old player entity; respawn rules in 8, no re-add |

Effects of the one-tick gap: Speed and Shrinking change for one tick (field of view, hitbox; include in the Shrinking
gap test), Night Vision may flash dark for a frame. Totem: the totem adds Regeneration II (900 ticks) after clearing,
so our Regeneration I re-add is ignored (lower and shorter) and comes back underneath it at a later refresh (9.4).

### 9.4 When a potion of the same effect is active

Vanilla keeps **one instance per effect**. When a second one arrives:

| Arriving instance | Result |
|---|---|
| higher level | it goes on top; the old one is kept underneath ("hidden") if it would outlast it |
| same level, longer | the two merge: one instance with the longer time |
| lower level, longer | it is stored underneath the current one |
| otherwise | ignored |

When the top instance runs out, the one underneath takes over with the time it has left. A merge copies time, level
and the hidden instance into the existing instance but **keeps the existing instance's cure list** (checked in
43.5.1), so the marker stays with whichever instance came first. Our 20 s refresh is just another "arriving instance".

"Ours" on the server: the instance (or a hidden one) carries our marker and has 800 ticks or less left. Marker with
more than 800 left = ours merged with a potion (case B). No marker but matching an active diet effect and level with
800 or less left = a potion's instance our refresh merged into; treated as ours on switch-off.

| Case | Example | What happens | Rule |
|---|---|---|---|
| A. Potion stronger | Speed II potion while slot has Speed I | potion on top and shown; ours waits underneath and returns, hidden, when the potion ends | if the slot switches off while the potion is on top, strip ours from underneath, or it resurfaces for up to 40 s |
| B. Potion same level | Speed I potion (8 min) while slot has Speed I | merged into one instance with the potion's time | on switch-off, leave it if more than 40 s is left (it is the potion's time); remove it if 40 s or less is left. Worst case a same-level potion loses its last 40 s (Q12) |
| C. Potion weaker | Speed I potion while slot has Speed II | ours on top; the potion waits underneath | on switch-off, promote the potion back to the top instead of removing the effect, or the potion is lost too |
| D. Showing it | any | the HUD shows whatever is on top | 9.5 |

A and C need an accessor mixin on vanilla's hidden-instance field.

### 9.5 Display

- The client cannot see cure lists (effect packets carry only id, level, time left and flags). So the server sends the
  client its list of active diet effects (effect and level) in Raspberry Pie's own packet. With the widget enabled, the
  client hides a top instance when its effect and level match that list and it has 800 ticks or less left. A merged
  instance with a potion's longer time (case B) or a stronger potion on top (case A) does not match and shows normally.
- This covers the vanilla HUD and the inventory's effect list. Other mods that draw their own effect HUD are not
  filtered.
- **Permanent column (2026-11-20, 12.7):** with Creative Common loaded, diet effects (the same match as above) count
  as permanent in Common's two-column inventory effect list (they show there when the widget is off). Registered by
  reflection (`PermanentEffects.addSource`), so Raspberry Pie has no dependency on Common.
- Widget on or off is a client setting only; the server sends the same effects either way.
- **Flat effects (decided 2026-11-12):** never shown on the in-game HUD (top-right icons), at any time. In the
  inventory screen they show on the widget when it is enabled, and like any vanilla effect in the inventory's effect
  list when it is off. Slot effects follow the first bullet (hidden when the widget is on, vanilla when off).
- A flat bonus and a slot of the same effect can coexist only when the slot was filled before the flat bonus arrived
  (5.1). The effect is applied once at the higher of the two levels; the covered slot shows as covered on the effect
  screen, the widget shows the flat bonus.

## 10. Messages

On the jukebox "Now playing" line above the hotbar (overlay message, fixed color, not rainbow):

`You {gained/lost} a bonus due to your diet: {name}`

| Color | When |
|---|---|
| green | gained from a flat bonus, health, toughness or knockback tier |
| blue | gained from a slot (re)activating |
| red | lost, any kind |
| yellow | a slotted effect is covered by a newly added identical flat bonus (queued after the "flat bonus added" line); an effect still sustained by another bonus when its slot is lost |

**Covered slot warning** (decided 2026-11-12): only when a flat bonus is added that is **identical** (same effect and
level) to a currently slotted effect, the green "gained" line is followed (queued after it) by a yellow line: `Your
slotted {effect} is covered by a flat bonus and can be reslotted`. A flat bonus of a different level for a slotted
effect gives no warning.

**Sustained line** (decided 2026-11-12): when a slot is lost (grayed, dropped or switched off) but its effect stays
active through an existing flat bonus, no red "lost" line is queued; a yellow line shows instead: `{effect} effect
sustained by existing bonus`. The confirm panel's list of lost effects (5.4) uses the same yellow text for that entry
instead of listing it as lost.

Hearts, knockback resistance and toughness use the same line (`{name}` = "1 Heart", "Knockback Resistance +1", ...).
Several changes in a short time: each message shows 3 s, the rest wait in a queue; none are dropped. Hearts,
knockback resistance and toughness changes of the same kind and direction that are still waiting merge into one line
with the sum ("3 Hearts", "Armor Toughness +4"). The line is shared with action-bar messages from other mods; **ours wait until the line is free** (decided
2026-11-12): the client shows the next queued line only when no other overlay message is on screen, and never
overwrites another mod's. The 3 s per line still applies once shown.

**Death:** no per-rung lines. One red message on respawn: `You lost every diet bonus due to death`, shown only if
something was active. Any queued lines from before the death are cleared.

Losses from a confirmed paid action were already listed on the confirm panel; they **also queue red lines** (Q-J, decided 2026-11-12); evaluate in play whether it is too noisy.

## 11. Screens and widgets (boilerplate first, then tuned with the owner)

### 11.1 Inventory slot widget

- A bar (rectangle) attached to the survival inventory screen only (not the creative inventory, not other screens).
- Shows only what is active: flat bonuses first (own style), then active slots (effect icon, level). Grayed slots
  are not shown here (they appear grayed on the effect screen). Hover gives the tooltip
  (effect, level, source, threshold).
- Click: opens the effect screen.
- Lock/unlock with Shift+T (toggle, the same key as TrashSlot's; both widgets lock and unlock together by default). Unlocked: drag it anywhere, and while dragging it
  snaps to the inventory edges horizontally and vertically at the same time, with TrashSlot's snappy feel. Locked: it
  stays put and clicks open the screen. Position and lock state saved per client (client config).
- Hidden while the Curios panel or the recipe book is open.
- JEI moves out of its way (JEI extra-area handler).
- The vanilla effect list may move to make room for a bar on the side (to evaluate in the boilerplate).
- Can be turned off in config (then effects show as normal, 9.5).

### 11.2 Effect screen

- Opened from: the widget, a keybind, the Diet Log button.
- Flat bonuses at the top (read only, own style), then slots in order; grayed
  slots show their stored choice and are **read only** (5.1).
- Choosing an effect: dropdown/picker grid of unlocked effects (drag from the grid onto a slot as a second option to
  evaluate in the boilerplate). Whatever empty active slot is targeted, the effect lands in the lowest empty active
  slot (5.1). Level dropdown per active slot (free).
- Paid actions open the confirm panel (5.4).
- Layout since 2026-11-17 (12.5): slots side by side, a progress bar under them in place of each slot's "needs
  diversity" line.

### 11.3 Keybinds (client, rebindable)

| Action | Default |
|---|---|
| Open the effect screen | P (decided 2026-11-12; the clash with vanilla's Social Interactions key is accepted, rebindable) |
| Lock / unlock the inventory widget | Shift+T (decided 2026-11-12; shares the key with TrashSlot's lock on purpose, so one press toggles both widgets; rebindable) |

### 11.4 Other screens

Diet Log button, milestone page and progress line (6.3); Liquid Diet Log book screen (7).

### 11.5 GUI toolkit

Vanilla `Screen` / `AbstractWidget` with our own dropdown, picker grid and drag code (no new library jar). Apple
Pie's own UI kit (`UIBox`, `UILabel`, `UIStack`, `UIItemStack`, `UIImage`) for the Diet Log page changes so they keep
the book style. Cloth Config (in the pack) optional for editing settings. FTB Library's GUI is the fallback if the
hand-built widgets grow too large (check its license first). LDLib is not worth the extra jar.

## 12. Open questions

### 12.0 Decided 2026-11-12

| Ref | Decision |
|---|---|
| Q1 | Filling a slot or changing its level shows no message. |
| Q2 | Superseded by Q-M below: the effect is hidden from the picker while a flat bonus is active, but a slot that already holds it keeps it (V11, 5.1). |
| Q5 | Lava Walker's burn change goes into the Vinery port in Creative Brewing. |
| Q6 | P opens the effect screen; Shift+T locks or unlocks the widget. |
| Q7 | Liquid Diet Log: book + any drink; Diet Log: book + any food item; both also in the starting inventory (7). |
| Q8 | Flat effects are hidden from the in-game HUD always; shown on the widget in the inventory (if enabled), else like vanilla (9.5). |
| Q9 | Our messages wait until the line is free (10). |
| Q10 | Liquid Diet Log holds unique drinks, and logs non-alcoholic drinks too (7). |
| new | All drinks (juice, tea, wine, alcohol, potions) have 0 diversity and no effect on food diversity (7). |
| Q11 | The foods-eaten counter resets on death (8). |
| Q12 | 9.4 case B loss of up to 40 s is acceptable. |
| Q-A | Accept the flicker for now; **needs in-game testing** to see whether it is a problem (test RPI-23). Fallback if it is: the margin option (a rung lost only when diversity is about 0.5 below its threshold). |
| Q-B | Option b: only active choices shift on a drop; grayed slots keep theirs (5.1, 5.2). |
| Q-C | Allow the change and store the choice grayed (5.3). |
| Q-D | An effect already slotted is not offered for another slot (5.1). |
| Q-E | Warn at server start when `minContribution` is above 0 (6.1; an INFO line since 12.16, aged-out foods leave the memory). |
| Q-G | Keep a slotted unlock until the player changes it, even if a ladder edit moved it out of reach. |
| Q-H | A broken or missing ladder file falls back to the built-in default ladder with a loud error. |
| Q-I | Admin pause, resume and pause-list commands; persist by default, effects stop applying, data keeps updating, effect interaction blocked, PAUSED icon (12.3). |
| Q-J | Paid-action losses also queue red lines; evaluate in play. |
| Q-K | Accept: a retuned ladder does not reach existing worlds on its own; note it in the release steps. No `ladderVersion` field. |
| Q-L | Accept oldest-first regardless of value (the confirm panel shows it). |
| Q4 | Fleet-Footed is allowed and fixed by a mixin (below). |
| Q-F | Dropped: no heart swap. The pack defines the ladder, Raspberry Pie ships a simple default ladder; an unregistered effect entry is skipped with a warning (3.2). |
| Q-M | A slot keeps its effect when a flat bonus for it is added on top; yellow warning line queued after the "flat bonus added" line (5.1, 10). |
| Q-N | P clash with vanilla accepted; Shift+T toggles both widgets (TrashSlot and ours) by default. |
| V2-V8, V10, V12 | Recommended handling in 3.3 chosen. |
| new | A respec lists the effects it will lose before the player accepts (5.4). |

### 12.1 Still open

Nothing is open in the design. Remaining work is the ladder values (`#12`, set by the pack) and the build.

### 12.3 Pause and resume commands (decided 2026-11-12)

- `/solrp pause <player>` and `/solrp resume <player>` (mirrored, resume undoes pause). Both need the same permission
  level as vanilla `/effect`. `/solrp pause list` lists every currently paused player (new command, for forgotten
  pauses).
- **Persistence:** a pause persists across relog and server restart by default (stored in the player's Raspberry Pie
  data). A config option can make it end on relog instead.
- **What pause does:** Raspberry Pie stops **applying** effects for that player: no re-add, no effect gains or losses
  applied, no effect messages. It removes nothing by itself, so an admin then uses `/effect clear` and the diet
  effects stay gone while paused.
- **What keeps running:** all data and dictionaries still update while paused: foods and drinks eaten (the Diet Log and
  Liquid Diet Log), max diversity, the foods-eaten counter, and death handling (the window ageing out and the counter
  reset still happen on death).
- **Effect interaction is blocked:** while paused, every effect interaction is disabled: no slot fill, change, level
  change, drop or respec, so no paid action runs on state that is not being applied.
- **Feedback to the player:** the effect screen's slot select shows a **PAUSED** icon, and every icon on the inventory
  widget is grayed out. The widget and the Diet Log stay readable. (This also tells the paused player why nothing
  applies.)
- **Resume:** re-evaluates the player from current state at once; effects come back and the normal gain lines show.

### 12.2 Fleet-Footed fix (decided 2026-11-12)

The bug: Fleet-Footed (+20% speed only while sprinting and airborne) keeps its state in fields on the single shared
effect object (the "last duration" it saw), and removes its speed modifier when the duration stops counting down as
expected. With several players the fields overwrite each other (flicker or a stuck bonus), and Raspberry Pie's periodic
refresh makes the duration jump, so the modifier would be dropped or never dropped even for one player.
Fix: a mixin on the effect's tick/removal code that keeps the "last duration" per entity (a map keyed by entity, or a
tag on the effect instance) and treats a refresh as a normal step, so state is no longer shared. Tested by RPI-33. It
is in the default pool once the mixin is in.

Versions tested: Alex's Mobs 1.21.1 (the pack's jar; `EffectFleetFooted` with `lastDuration`, `removeEffectAfter`,
`m_6742_`). Since 2026-11-21 (12.9 P6) the fix checks the loaded class's members instead of a version range, so a
later Alex's Mobs with the same members keeps it and one with different members turns it off with one warning.

## 13. Implementation hurdles (largest first)

Not decisions; effort notes for planning.

1. UI is most of the work: snapping draggable widget, JEI exclusion, Curios and recipe-book detection, picker and
   dropdowns with auto-shift, confirm panel with icons, Diet Log page mixins in Apple Pie's UI kit, Liquid Diet Log
   screen.
2. Effect identity across vanilla's hidden-instance stack (9.4 A-C), the next-tick re-add (9.3) and the client display
   filter (9.5).
3. Network: ladder sync, active diet effects list, slot state, preview request and reply with state stamp, confirm,
   max diversity for the progress line.
4. Own player capability (max diversity, slot choices, Regeneration anchor, state stamp, Liquid Diet Log), copied on
   death and on the End exit clone.
5. Mixins and accessors into Apple Pie private members pin the mod to 2.2.0 exactly.

### 12.3a Diversity and ladder commands (built 2026-11-23)

`/solrp diversity set|get` and `/solrp ladder reset` (permission 2): 12.14.

## 14. Tests

Scratch run r1115 (2026-11-15, [scratch_run_2026-11-15.md](testing/scratch_run_2026-11-15.md) 8): SQ-RPI-07 PASS (diet reset clears the Apple Pie food list), SQ-RPI-08 PASS (boot WARN about Apple Pie's own benefits; max health 20, only Carrot's modifier).

Live rows are in [scratch_queue.md](testing/scratch_queue.md) section "SQ-RP" (fresh world). Map:

| Test | Queue row | | Test | Queue row |
|---|---|---|---|---|
| RPI-01 boot, no Apple Pie benefits | SQ-RPI-01, -02 | | RPI-21 stale preview | CQ-RPI-19 |
| RPI-02, RPI-03 gain and loss lines | CQ-RPI-03, -23 | | RPI-22 Apple Pie leftovers stripped | CQ-RPI-20 |
| RPI-04 fill lands lowest | CQ-RPI-04 | | RPI-23 flicker (Q-A, owner judges) | CQ-RPI-21 |
| RPI-05 free level change | CQ-RPI-05 | | RPI-24 drinks leave diversity alone | CQ-RPI-15 |
| RPI-06 change ages out 8, preview matches | CQ-RPI-06 | | RPI-25 books on first join | CQ-RPI-01 |
| RPI-07 drop shifts down | CQ-RPI-07 | | RPI-26 flat never on HUD | CQ-RPI-12 |
| RPI-08 milk, totem, /effect clear | CQ-RPI-08 | | RPI-27 waits for another overlay line | CQ-RPI-22 |
| RPI-09 aged food counts in full again | CQ-RPI-09 | | RPI-28 flat hides from picker | CQ-RPI-23 |
| RPI-10, RPI-31 death | CQ-RPI-10 | | RPI-29 respec grays its own slot | CQ-RPI-24 |
| RPI-11 widget | CQ-RPI-11 | | RPI-30 panel lists losses | CQ-RPI-06 |
| RPI-12 effect display | CQ-RPI-04, -12 | | RPI-32 paid-action red lines | CQ-RPI-07 |
| RPI-13 potion of the same effect | CQ-RPI-13 | | RPI-33 Fleet-Footed two players | CQ-RPI-26 |
| RPI-14 message queue | CQ-RPI-03 | | RPI-34, RPI-38 covered slot line | CQ-RPI-23 |
| RPI-15 Diet Log pages | CQ-RPI-14 | | RPI-35, RPI-20 missing-mod entry | SQ-RPI-03 |
| RPI-16 Liquid Diet Log | CQ-RPI-15 | | RPI-36 book recipes | CQ-RPI-02, SQ-RPI-06 |
| RPI-17 dedicated server | CQ-RPI-16 | | RPI-37 pause | SQ-RPI-05, CQ-RPI-25 |
| RPI-18 grayed slots read only | CQ-RPI-17 | | RPI-39 sustained line | CQ-RPI-23 |
| RPI-19 Regeneration rhythm | CQ-RPI-18 | | V13 broken ladder file | SQ-RPI-04 |

Extra rows: CQ-RPI-27 Lava Walker burn (Vinery port), CQ-RPI-28 Shrinking in a 1-block gap (9.2). 12.4 rows: SQ-RPI-07
(reset), SQ-RPI-08 (stale Apple Pie benefits inert), CQ-RPI-29 to -40 (tooltip, Food Dictionary, unique gate, pages,
icons, window, double page, health rule, backfill).

12.15 rows (2026-11-23): SQ-RPS-11 (isolated Diet Log recipe loads), CQ-RPS-12 (any-food Diet Log, a dedicated
server's table on the client, drink list kept between syncs), SQ-RPS-13 (pack exclusion tag, R5); `tools/test_rp_ladder.sh` ALL 55 OK.

12.16 rows (2026-11-23): CQ-RPS-14 (drop after `/solrp diversity set` keeps the lower slots, aged-out foods leave the memory), CQ-RPS-15 (Hardest sort, Diet Log width 48); CQ-RPI-90 covers `#21`; `tools/test_rp_ladder.sh` ALL 59 OK.

12.14 rows (2026-11-23): CQ-RPI-83 to -91 (diversity set raise and lower, closed gate, relog, ladder reset, Stimulation, numeral, reached icons, diversity get).

Results: r1112 ([scratch_run_2026-11-12.md](testing/scratch_run_2026-11-12.md) 8): SQ-RPI-01 to -06 PASS. The 20:47
jar did not load (`solapplepie` range `[2.2.0]` against Apple Pie's jar version `1.19.2-2.2.0`); fixed to
`[1.19.2-2.2.0]` in `mods.toml`, jar of 21:27 installed. SQ-RPI-06 needs no Creative Delight rebuild: `diet_log.js`
loads from the pack's `kubejs/data` folder. Client rows CQ-RPI-01 to -28 not run.
Scratch run r1123rp (2026-11-23, [scratch_run_2026-11-23.md](testing/scratch_run_2026-11-23.md) 11, jar
911197e7c.2611222309, publish gate): 16 server rows PASS: SQ-RPS-01, -04, -06, -07, -08, -10 (isolation set,
plan_rp_standalone.md 6) and SQ-RPI-01 to -08, -14 (boot half), -16; pack ladder `82 rungs, 5 slots, 38 pool effects`.
Client rows (all `@draft`) not run; the owner's 2026-11-23 ruling "UI looks good" covers the UI-look ones (run doc 11).

## 15. Build notes (2026-11-12)

Code: `development/solRaspberryPie` (package `com.SaintAlfonzo.solraspberrypie`): `ladder/` (JSON loader with the
3.3 checks), `data/RpData` (capability), `logic/` (`DietEngine` rungs and messages, `EffectApplier` marker and 9.4
cases, `SlotActions` fill/level/respec, `FoodWindow` ageing, `Drinks`, `ServerEvents`, `PauseRegistry`), `net/`,
`client/` (effect screen, inventory bar, message queue, Diet Log pages, Liquid Diet Log screen), `mixin/`.

Choices made while building (owner may revisit):

- **Ladder file format.** `pool` = list of `{effect, max_level}`; `rungs` = list of `{threshold, type, ...}` with type
  `health` (`amount` in health points, 2 = one heart), `attribute` (`attribute`, `amount`), `flat` (`effect`,
  `level`), `slot`, `unlock` (`effect`, `level`). The pack file is `defaultconfigs/solraspberrypie/ladder.json`
  (draft values for `#12`: old Apple Pie hearts, toughness and knockback carried over; 5 slots at 2, 10, 30, 50, 75;
  flat Speed I at 20, so Speed is capped at I in the pool: V11 hides it from the picker once the flat bonus is on);
  the jar's built-in default (vanilla only) is `solraspberrypie/default_ladder.json` (Apple Pie's ladder since 2026-11-21, 12.9).
- **Validation (3.3) as built:** V1/V2 skip, one warning listing every skipped entry; V3 drop the unlock, warn; V4
  warn; V5 clamp to the cap, warn; V6 warn (a higher unlock grants the lower levels); V7 warn on equal slot
  thresholds, slots numbered by threshold then file order; V8 extra stored choices dropped at login (logged); V10
  instant effects, Absorption, Health Boost and Saturation refused in the pool and as flat bonuses; V12 warn above an
  upper bound (the most complex food in every window position); V13 built-in ladder with a boxed error.
- **Messages.** A slot rung names its stored effect ("Speed I"), or "Effect Slot N" when empty. Reaching a one-time
  unlock gives a green "Unlocked for your effect slots: ..." line (not in the design table). Changing a slot's effect
  gives no line for the swapped effect (Q1 extended to change); losses from the diversity drop do (Q-J).
- **Effects** are added ambient (beacon frame), no particles. Pause also freezes hearts and attribute tiers (nothing
  applied or removed while paused); resume announces every active bonus as gained.
- **Health.** Transient max-health is lost on logout, so health is saved at logout and restored after login (and
  across the End exit clone).
- **Books on first join** use the vanilla Leave Game stat (0 = new player), plus a flag in Raspberry Pie's data, so
  existing players never get them. Server config `giveBooksOnFirstJoin`.
- **Recipes.** Diet Log: own recipe type `solraspberrypie:diet_book` (shapeless with an excluded tag), book + any
  edible item (ingredient type `solraspberrypie:edible`, since 2026-11-23, 12.15; was the 17-food tag
  `diet_log_foods`) that is not in `#solraspberrypie:diet_log_excluded` (drinks, and carrot: book + carrot stays the
  Food Dictionary's). JEI cycles every edible item outside that tag. The pack adds drinks, hazard foods and other
  non-foods to the tag (12.15). Liquid Diet Log: book + `#solraspberrypie:drinks` (the pack drink
  tags, Farmer's Delight and Brewin' drink tags, potions, milk bucket). Apple Pie's book + potato is removed by
  `RecipeManagerMixin` in this jar (12.9; `kubejs/data/creativedelight/kubejs/solapplepie/diet_log.js` before 2026-11-21).
- **Pause list** is kept in world data (`data/solraspberrypie_paused.dat`) so offline paused players are listed.
- **Fleet-Footed:** the shared state that matters is `removeEffectAfter` (`lastDuration` is written and read in the
  same per-entity call); the mixin keeps that countdown per entity. Applied only with Alex's Mobs.
- **Diet Log pages:** markers were colored squares with a legend (and never drew: 12.4 #37); icons and a windowed
  bar since 2026-11-13 (12.4).
- **Widget:** vertical beside the inventory, horizontal above or below; JEI exclusion through a JEI plugin. The
  vanilla effect list was not moved (11.1 "to evaluate").
- **Not built:** drag from the picker grid onto a slot (11.2 second option); drinks taken from placed blocks have no
  Liquid Diet Log hook (none found that are drinks).

### 12.4 Owner notes (scratchpad 2026-11-13; built 2026-11-13 except the art)

TODO `food/types-and-tuning #29` to `#38`. Built 2026-11-13 (owner "go", defaults below); jars installed: Raspberry
Pie 17:57, Creative Delight 17:58, Carrot Edition 1.14.0. Tests: SQ-RPI-07, -08, CQ-RPI-29 to -40 (section 14).

Done already: the book names (Diet Log, Food Dictionary; `food_dictionary.md`).

Root cause of #33, #36 and #38: the local worlds (New World, Structures, Workspace) still had Apple Pie's old
`serverconfig/solapplepie-server.toml` (17 benefit thresholds with max health, `minFoodsToActivate = 10`,
`resetOnDeath = true`, no drink blacklist). Replaced with the pack default on 2026-11-13 (old copies kept in the
session scratchpad only). Worlds elsewhere (the dedicated server, players' old worlds) keep their copy until it is
replaced by hand; Raspberry Pie now makes Apple Pie's benefits inert whatever the file says.

- **#29 tooltip (built):** Raspberry Pie and its slice show a gray italic "Doesn't run programs" (Creative Delight
  `hearthandharvest/client.js`).
- **#30 reset (built):** `/solrp reset <players>` (permission 2, like pause). Clears Apple Pie's food memory and
  foods-eaten counter (and so the unique-food count), highest diversity (so every one-time unlock), slot choices and
  the Liquid Diet Log. Keeps pause state and the books-given flag. Applies at once; the player gets "Your diet was
  reset". There was no stored cost history to clear (costs come from `respecFoods`).
- **#31 double page (removed 2026-11-17, G7.12: option and code gone; see 12.5):** client config `[dietLog] doublePage` (on). When on and the screen is at
  least 380 px wide, the book moves left by half a page and a second book on the right shows the first page of foods
  out of the decay window ("Older foods"); the left food pages become "Counting foods" (in the window) followed by the
  older foods from page 2 on. Narrow screens fall back to one book.
- **#32 windowed progress bar (built):** the bar covers a window of the ladder: from a little before the last milestone
  reached (35 % of the gap to the one before it) to the Nth milestone ahead (client config `[dietLog] progressAhead`,
  default 3). Values are printed under both ends, "..." when more lie ahead, "<" when earlier ones exist, ">" in purple
  when the highest-ever mark lies past the window. Past the last milestone the window runs a little beyond it.
- **#33 extra hearts (fixed):** Apple Pie's `BenefitsHandler.updateBenefits` and `tickBenefits` are canceled
  (`BenefitsHandlerMixin`); the server-start warning stays. Max-health rule as built: a drop takes away only health
  above the new maximum (a full player stays full at the lower maximum, a hurt player below it keeps their health); a
  rise heals nothing. No hurt animation (health is set, not damaged).
- **#34 "carrot book not found" (fixed):** the Carrot Edition jar had never been copied to `mods/` (food_dictionary.md
  setup); now installed.
- **#35 art (placeholder accepted 2026-11-13, owner: the copy of the Diet Log book stays until a custom texture is made; all other 12.4 defaults kept: progressAhead 3, uniqueFoodsToActivate true, /solrp reset scope):** the Liquid Diet Log's item texture is still an exact copy of Apple Pie's Diet Log book. The
  owner draws a liquid version, or picks a palette for `palette_swap.py` on the Diet Log book.
- **#36 minimum foods (fixed, plus a switch):** stale world config (above). New server config
  `uniqueFoodsToActivate` (default true): the gate counts **different** foods eaten since Apple Pie's counter last
  reset (death, `/solrp reset`, `/foodlist clear`). Kept as a set on Apple Pie's FoodList (mixin, NBT key
  `solrp_gateFoods` in Apple Pie's capability, so it syncs and is copied with it); a list saved before the key
  existed is backfilled with the remembered foods whose meals-since count is below `foodsEaten`. The gate, the slot
  preview, the P screen, the Diet Ladder page and Apple Pie's diversity page ("Need N more different foods") all read
  it. Network protocol version 2.
- **#37 icons and colors (fixed):** cause: the progress page's `render` never called `super.render`, so its child
  markers (bar and legend) were never drawn. Icons now (vanilla sprites and models, no new texture): heart (health),
  item model per attribute (netherite chestplate toughness, shield knockback resistance, feather movement speed, iron
  sword attack damage, rabbit foot luck, else vanilla's armor point), slot box (with the chosen effect's sprite),
  effect sprite (flat and unlock). Each has a frame in its kind color when reached; unreached icons are greyed. The
  Milestones list uses the same icons (rows 12 px).
- **#38 Apple Pie pages (fixed):** every Apple Pie benefits page is removed from the Diet Log, whatever the config.

### 12.5 Screens after the 2026-11-16 rulings (built 2026-11-17)

Plan: [plan_post_scratch_1115.md](plan_post_scratch_1115.md) G5 as amended by G6-23, G6-24, G7.12; TODO
`tooling/scratch-1115-follow-ups #24`; tests CQ-RPI-41 to -46 (CQ-RPI-33 and -36 superseded).

- **Single page (G5a, G6-23):** the double page view and its client option `[dietLog] doublePage` are removed, with the
  "Counting foods" / "Older foods" split it brought: the food history is Apple Pie's one list again. The owner's client
  config was edited by hand (mirror in `configs_edited/root_config/`); other clients drop the key when Forge corrects
  the file.
- **Book scale (G5b):** client config `[dietLog] bookScale` (0.5 to 3.0, default 1.25). Applied as a model-view scale
  around the screen center while the Diet Log renders, so Apple Pie's fixed-pixel UI kit, its item models and the
  Diet Bonuses button grow together; the mouse is mapped back for hovers and clicks (`mouseClicked` override). Fit
  rule (replaces the 380 px rule of #31): above 1.0 the scale shrinks so the book (186 px wide) and the button (118 px
  below center) stay 4 px inside the window, but never below 1.0. At 1.25 that needs 241 x 303 GUI pixels; GUI scale 4
  on 1080p (480 x 270) gives about 1.11. Tooltips are drawn inside the scale and can reach past the window edge.
- **Effect screen (P key, G5c, G6-24):** slots in a row of cells (up to 92 px each; the panel widens to fit, never
  past the window less 16 px, never under 276 px). Each cell: "Slot N", effect icon, name (cut to the cell), -/+, then
  Change/Drop (Choose when empty). Hover a cell for the name and its "Slot N (diversity X)" or "needs diversity X".
  Permanent buff icons start 14 px after their label. Under the slots a full-width bar: one segment per slot, a tick
  and the slot's diversity at each segment's right end, "0" at the left; the green fill follows the current diversity
  piece by piece and is full at the last slot's diversity.
- **Icons (G5d, G7.12):** client config `[icons]`, each a list tried in order, first loaded item wins, NBT allowed in
  braces: `unlock` (Monobank `replacement_lock`, then an Awkward Potion), `permanentBuff` (Create Recycle
  `create_crush_everything:diamond_shard`, then a diamond), `slot` (Supplementaries Squared `suppsquared:metal_frame`,
  then an item frame). Used on the bar, the Milestones list and the key; when no listed item loads, the old icon
  (effect sprite, slot box). Attributes keep their item icons (netherite chestplate, shield, ...). The key has six
  rows: Hearts, Toughness, Knockback resistance (the shield, missing before), Effect slot, Permanent buff, One-time
  unlock.
- **Icon rulings (G11 `#24`, owner 2026-11-17; built 2026-11-17):** one-time unlocks and permanent buffs keep the item
  icons on the bar and in the Milestones list (named by hover and the list text). Slot rungs keep the chosen effect's
  sprite: a slot holding an effect shows that effect's sprite (grayed when the slot is not reached) and its hover and
  list text read "Effect Slot N: <effect>"; an empty slot shows the `[icons] slot` item. The key's Effect slot entry
  cycles through the loaded slot-pool effects in pool order, one per second, looping (`legendSlotEffect`); with no
  loaded pool effect it shows the slot item. Assumed (not ruled): the cycle period of 1 second, fixed in code (no
  config); the pool order is the ladder file's order; the slot item stays the icon for empty slots. Test CQ-RPI-46.

### 12.6 Owner notes 2026-11-18 (built 2026-11-19)

Plan and rulings: [plan_diet_books.md](plan_diet_books.md) (window 30 meals, respec 5, last-30-meals list replaces the
food history, even-spaced ladder of 5, Diet Log icon changes; Food Dictionary sorting). TODO `food-tuning #2` to `#15`
(`#15` later). Choices made while building: plan section 5. Tests SQ-RPI-09, CQ-RPI-47 to -56 (drafts until the
owner's look-check; CQ-RPI-53 supersedes CQ-RPI-46).

- **Rules:** Apple Pie `endDecay` 30 (pack `defaultconfigs/`, the three local worlds; back to 32 in 12.7); `respecFoods` default 5.
- **Last meals:** Raspberry Pie keeps the last `endDecay` meals eaten, newest first, duplicates kept (NBT
  `solrp_meals` on Apple Pie's FoodList; old saves backfilled from the memory). Since 2026-11-19
  (plan_owner_notes_1119.md rulings 7 to 10) they are an icon grid (6 x 5 "Last 30 Meals"; 8 x 4 "Last 32 Meals" since 12.7, newest top left) on the
  first page, above the diversity line; the meals pages are gone. A meal shows only while its food is in the decay
  window (memory count below `endDecay`), so a Change or Drop removes the aged foods' meals and the grid closes up.
  Hover: "N. Name", this meal's contribution (Apple Pie's `calculateDiversityContribution` at the meal's log index,
  struck through when it differs) and the food's total (at its memory count). Apple Pie's "Food Eaten" pages are
  removed.
- **Diversity icon** (ruling 12; replaced in 12.7): Apple Pie's image left of "Current Food Diversity" is a food: share = diversity /
  the ladder's top milestone; edible items ranked by Apple Pie complexity, band = the share +-0.05 quantiles; one
  Carrot-eaten food of that band, picked by the diversity's tenth (stable). None: client config `[dietLog]
  diversityIcons` ("minDiversity=item,item,..."; agent defaults, unconfirmed).
- **Milestones and ladder** (rulings 6, 11): active buffs (reached rungs other than one-time unlocks) have green row
  text; a reached one-time unlock's text is struck through in the Milestones list (its milestone number is not), black
  while the current diversity is at or above it, gray below; the Diet Ladder bar leaves reached unlocks out (key
  unchanged).
- **Diversity printed rounded down** (two decimals everywhere since 12.7) to one decimal by Raspberry Pie (Diet Ladder page, effect screen, confirm
  panel); Apple Pie's own diversity page keeps its two decimals.
- **Page layout:** client config `[dietLog.pages.diversity|meals|ladder|milestones]` `x`, `y`, `scale`, replacing
  `contentScale`. Defaults: diversity unscaled; meals 0.85; ladder and milestones 0.85, one line (10 px) lower.
- **Wider book:** `[dietLog] extraWidth` (default 0; 93 = 50% in 12.7, 47 = 25% since 2026-11-20): the background's center column is repeated, the pages widen,
  the page arrows move out by half; the fit rule includes it.
- **Mouse wheel** flips one page per notch (Diet Log and Food Dictionary).
- **Ladder bar:** five evenly spaced milestone values: the last reached and the next four (the last five near the
  top), or a 0 origin and the first five; fill and max mark proportional between neighbors; every point labeled.
  `progressAhead` removed.
- **Icons:** a locked one-time unlock alternates the lock item and the effect sprite each second (gray frame), and so
  does a permanent buff, reached (its colored frame) or not (gray frame, grayed), with the diamond shard and the effect; a
  reached one shows the effect sprite grayed in a purple frame whatever the current diversity. Slot rungs show the
  metal frame item in the slot color (grayed when the slot is not active), hover "Effect Slot N" with no effect;
  effects show in slots only on the effect screen and the inventory widget (reverses the 12.5 icon ruling). The key
  is unchanged except the slot row stops cycling.
- **Gate text:** "N more unique foods to activate diet" (singular for 1); the diversity page "N more unique
  food(s)" / "to activate diet".
- **Effect screen:** Change and Drop keep their 18 px buttons with labels at 0.85 scale; hover shows "N foods age
  out" (Change: `respecFoods` capped at the window; Drop: the drop rule of 5.2), counts synced with the state.

### 12.7 Owner notes 2026-11-19, round 2 (built 2026-11-20)

Plan and rulings: [plan_diet_books_r2.md](plan_diet_books_r2.md) (intake of the scratchpad "rasp pie" block). TODO
`food-tuning #20` to `#29`, `tooltips #19`, `#20`, `interface #24`. Queue rows follow the owner's look-check.

- **Window back to 32 meals:** Apple Pie `endDecay` 32 (pack `defaultconfigs/`, `configs_edited/`, the three local
  worlds); the meals grid is 8 x 4 ("Last 32 Meals"), 18 px icons on a 19 px step (20% up from 15 / 16), the meals page scale shrinks it if needed.
- **Meals grid:** title (and "No meals yet") centered over the grid. Hover: "N. Name"; "Full meal: X diversity" (the
  food's base diversity, Apple Pie complexity); "Food total: X" only when it
  differs from this meal (compared as printed); "This meal: Y", struck through when the total line is there. Drawn
  as a plain text tooltip (no item stack): with the stack it went through the item tooltip handlers (Legendary
  Tooltips, AppleSkin, Raspberry Pie's food rows) and was not drawn at all on the Overview page.
- **Width and first page:** `[dietLog] extraWidth` default 47 (a quarter of Apple Pie's 186 px book; the pack's client
  config set to 47, was 93, before that 40). Ladder and Milestones content (titles, bar, key, rows) keeps 17.5% of the
  page width free on each side (`DietLogPages.SIDE_INSET`), so it stays off the book's edges. "Current Food Diversity" and the gate lines ("N more unique foods" / "to activate diet") are
  swapped for `FitLabel`s after Apple Pie builds the page: one common text scale, the largest up to 0.85 at which every
  one fits the page width less 20 px a side (divided by the diversity page's own scale); each stays on its own line.
- **Diversity printed** with two decimals, rounded down (`Icons.diversity`, epsilon 1e-6 so 1.40 stays 1.40): Diet
  Log (Apple Pie's own value too, via a redirect of its `String.format`), Diet Ladder page, effect screen ("Diversity:
  X (highest Y)"), confirm panel ("Diversity: X -> Y"), meals hover, food tooltips.
- **Diversity icon:** the average Apple Pie complexity (base diversity) of the meals in the window (the grid's meals,
  repeats counted); the Carrot-eaten food that is not a drink with the closest complexity, ties by Apple Pie memory
  count (most recently eaten), then id. No meal in the window, nothing eaten or no Carrot: one loaded item of client
  config `[dietLog] diversityIconFallback` (pumpkin, mixed, oat, corn and sunflower seeds, Miner's moss, Immersive
  Weathering moss clump, rotten flesh, spider eye, dandelion leaf), picked by the diversity's tenth. The old
  `diversityIcons` key and its all-items complexity scan are gone (the key was renamed so old configs pick up the new
  default; Forge drops the old key).
- **Open timing:** each Diet Log open logs `Diet Log open: pages X ms (diversity icon Y ms, meals grid Z ms), first
  frame W ms` at INFO in latest.log (`DietLogPages.OpenTiming`). The owner's log decides further caching.
- **Liquid Diet Log** pauses the game in single player.
- **Unlock gate:** saved flag `gateOpened` (RpData NBT). `maxDiversity` climbs only once the gate (Apple Pie's
  minFoodsToActivate, unique foods) has been passed once; later gate closings (death resets Apple Pie's counter) do
  not stop it. Unlocks and their messages follow `maxDiversity`, so they wait too. Old saves: missing flag = opened when
  `maxDiversity` > 0. `/solrp reset` clears it.
- **Food Dictionary sorts:** uneaten `MOD_NAME`, `NAME`, `EASIEST`, `HARDEST` (12.16), `HIGHEST_FOOD` (nutrition), `HIGHEST_SATURATION`
  (nutrition x modifier x 2), `HIGHEST_DIVERSITY` (complexity), `CUSTOM`; eaten button `NEWEST_UNLOCK`,
  `LATEST_EATEN`, `HIGHEST_FOOD`, `HIGHEST_SATURATION`, `HIGHEST_DIVERSITY`, `CUSTOM` (icons golden carrot, clock,
  cooked beef, rabbit stew, suspicious stew, book and quill). Ties fall back to the uneaten order. `CUSTOM` reads
  client config `[foodDictionary] customOrder` (item ids, listed first in order); the button skips it while the list
  is empty (`food-tuning #15`). A config value not in the list resets to the default on load.
- **Food tooltips** (`client/FoodTooltips`, GatherComponents at LOWEST): order name, AppleSkin's food row, the
  diversity line, Quark's saturation word, the rest. Diversity line on every food Apple Pie counts (edible, not a
  drink, `shouldCount`): "Diversity: X", or "Diversity: X (contributes Y)" while its memory count is inside the window.
  Apple Pie's own bottom line ("Food Diversity: X" uneaten, "Diversity Contribution: X" eaten) is removed by its
  localized label prefix (since 2026-11-20, owner); its "Last eaten: N meals ago" and "does not contribute" lines stay.
  Quark's shank row is off by config (`[client.improved_tooltips] "Food Tooltips" = false`, "Show Saturation" kept;
  backed up in `configs_edited/`). AppleSkin's saturation icons sit on the shank row after the shanks
  (`AppleSkinTooltipRendererMixin`, AppleSkin 2.4.2 only) unless client config `[tooltips] saturationOwnLine`.
  Mechanics: [agent note](agent_notes/food-tooltip-rows.md).

### 12.8 Diversity table (plan_food_diversity_1121.md, built 2026-11-21)

Raspberry Pie sets every food's diversity from a table instead of Apple Pie's formula and config list:
the pack's table is `solraspberrypie/diversity/creativedelight.json` in Creative Delight's jar since 2026-11-21 (12.9;
was `/solraspberrypie/diversity.json` in this jar) (`values`: item -> diversity; `families`: id -> members, first member is
the head), generated by `tools/food_diversity.py` from the rules in `kubejs/generation_scripts/food/food_diversity.json`
and the `/dumpfood` export. `Diversity` reads it once on the server; a client on a remote server gets the server's table
by packet (12.15, since 2026-11-23; before, each side read its own jars); a food missing from the table keeps Apple Pie's value. `FoodListMixin` returns the table value from
`FoodList.getComplexity` and rewrites the item argument of `addFood`, `simulateFoodAdd`, `getLastEaten` and `hasEaten`
to the family head, so a family is one diet entry. `#solraspberrypie:no_diet` (chocolate coins) is out of the diet like
a drink but never logged. Workflow: `/dumpfood` -> `python3 tools/food_diversity.py --report` -> rebuild and install
Creative Delight -> restart. Rules, tier tops and fitted values: plan_food_diversity_1121.md sections 3 and 6; the last step scales each tier so the
best diet peaks at 100, high meals and feasts at about 72 (3/4 of the ladder), gilded and dragon food the last quarter (R15, food_diversity_audit.md 1.1).

### 12.9 Standalone mod (plan_rp_standalone.md, built 2026-11-21)

Raspberry Pie runs with only Forge and its own jar; the pack layers its table, ladder, names and Apple Pie settings on
top. Rulings and phase list: [plan_rp_standalone.md](plan_rp_standalone.md); status in its TODO rows.

- **Bundle (P1).** `solRaspberryPie-<ver>.jar` is a `ce.forge-bundle` jar: Apple Pie 2.2.0 and Carrot Edition 1.14.0
  are nested unmodified under `META-INF/jarjar/` (`bundle_external_jars` in `gradle.properties`, each file pinned by
  sha1, so a changed library stops the build). Metadata ranges: Apple Pie `[1.19.2-2.2.0]`, Carrot `[1.19.2-1.14.0,)`.
  Carrot stays optional in `mods.toml` (D6) and its mixins stay gated to 1.14.0. A loose copy of either in `mods/` is
  picked by Forge's Jar-in-Jar selection instead of the nested one (RPS-04); `build_deploy.py` refuses a stage with a
  loose copy (duplicate mod id) and `gen_pack_manifest.py` lists the nested jars under the bundle and leaves a loose
  copy out. The jar carries `solRaspberryPie-LICENSE.txt` (GPL-3.0-only), `solRaspberryPie-LGPL-2.1-solapplepie.txt`,
  `solRaspberryPie-LGPL-2.1-solcarrot.txt` (the upstream `LICENSE.txt` files) and `solRaspberryPie-NOTICE-nested-LGPL.txt` (names,
  authors, versions, sha1, "unmodified", upstream tag / commit links, the Liquid Diet Log art credit). `mods.toml` has
  `displayURL` and `issueTrackerURL` (the GitHub repo). The compile jars in `libs/` stay git-ignored; `libs.csv` records
  them and `python3 tools/rp_libs.py fetch` downloads them sha1-checked on a fresh clone (12.12).
- **Default ladder (P2; reworked after the owner rulings of 2026-11-21).** `solraspberrypie/default_ladder.json` (used
  when no `defaultconfigs/solraspberrypie/ladder.json` exists, so never in the pack) is Apple Pie 2.2.0's own default
  ladder (`SOLSweetPotatoConfig`) scaled to a top of 50, every threshold halved: with vanilla foods and Apple Pie's
  default settings (`queueSize` 32, `endDecay` 32) about 56.2 is the most a diet reaches (best vanilla food in all 32
  positions), so Apple Pie's 100 scale left 11 rungs unreachable (owner: default max diversity 50). Rungs: half a heart
  (`health` 1) at 2.5, 3.5, 5, 7.5, 10, 12.5, 15, 17.5, 20, 22.5, 25, 30, 35, 40, 45, 50; armor toughness +2 at 7.5, 25,
  40; knockback resistance +1 at 20; Apple Pie's always-on effects kept as flat rungs (owner): Speed I 1, Haste I 3.5,
  Strength I 5, Regeneration I 10, Speed II 12.5, Luck I 17.5, Haste II 30, Strength II 35, Strength III 45, Luck II 50
  (its second Strength I, at 30 on its scale, adds nothing and is left out, as is its `generic.speed` entry, which names
  no attribute). Four slots at 5, 15, 27.5, 40. Thirteen one-time unlocks with the D10 falling density (ratio 2.0, 5 to
  50): Speed I 5, Haste I 7.9, Strength I 10.8, Jump Boost I 13.9, Night Vision 17.1, Water Breathing 20.4, Luck I 23.8,
  Regeneration I 27.5, Fire Resistance 31.4, Jump Boost II 35.5, Haste II 39.9, Slow Falling 44.7, Glowing 50. The three
  added unlocks (Jump Boost II, Haste II, Slow Falling) and that order are agent defaults, **unconfirmed**. A flat effect
  hides an unlock of the same effect from the picker (V11) where they overlap; accepted (owner). Pool: the 11 effects,
  cap II for Haste and Jump Boost, I otherwise. Fire Resistance is allowed here, unlike the pack pool in section 4.
  Expected start line then: `Diet ladder: 47 rungs, 4 slots, 11 pool effects` (superseded by the 44-rung ladder in 12.15), no warning (V12 included). Headless test
  `tools/test_rp_ladder.sh` (in `check_all.py`) checks that and the shape.
- **Table loader (P3, P8).** `Diversity` reads every `solraspberrypie/diversity/*.json` in any loaded mod file (Forge
  `ModList` mod files, so any jar can ship one), merged in mod-id then file-name order; a later file wins an id both
  list, logged as one INFO line per file pair with the count. Ids not in the item registry (their mod is absent) are
  skipped with one INFO count in the summary line (A6), never a warning per id. No file anywhere: one INFO line and
  Apple Pie's values stand (A7). Raspberry Pie's jar ships no table; the pack's table (1,658 ids, vanilla foods
  included, so the pack sets the Apple Pie values it wants, A7) is Creative Delight's
  `src/main/resources/solraspberrypie/diversity/creativedelight.json`, written by `tools/food_diversity.py`
  (`generators.json` output and apply step updated). Same file shape (`values`, `families`). The headless test covers
  the merge, a family with an absent head, no table, and the pack file on vanilla ids.
- **Names and recipe (P4).** The pack wiring moved into the jar, no KubeJS needed. Names: the lang file carries
  `solraspberrypie.override.<key>` entries ("Diet Log" for `item.solapplepie.food_book`, "Food Dictionary" for
  `item.solcarrot.food_book`, "Open Diet Log" for `key.solapplepie.open_food_book`) and `ClientLanguageMixin` copies
  each onto its key after every lang file is merged (`LangOverrides`). Plain keys of another namespace in our lang file
  (the plan's first idea) do not work: the client merges namespaces in HashMap order, and with few mods `solapplepie`
  and `solcarrot` load after `solraspberrypie` and keep "Food Book". Recipe: `RecipeManagerMixin` (priority 900, ahead
  of KubeJS's 1100 HEAD cancel) drops `solapplepie:food_book` from the recipe map before parsing, so it also runs on a
  dedicated server without KubeJS. Creative Delight's `names.js` lines and `solapplepie/diet_log.js` are deleted;
  Common's `item_dict.js` keys `diet_log`, `liquid_diet_log` and `food_dictionary` are deleted (nothing reads them once
  `names.js` stops; `food_dictionary` went too for the same reason, beyond the plan's two).
- **Apple Pie's own settings (P5).** Raspberry Pie writes no Apple Pie file (D4); the pack's
  `defaultconfigs/solapplepie-server.toml` stays the recommended set (`queueSize` 256, `minFoodsToActivate` 5, empty
  benefit lists, drink blacklist). `ApplePieSettings.report()` at server start replaces the three single warnings: one
  boxed WARN block names each setting it cannot work with (`minContribution` above 0, `resetOnDeath` true, `startDecay`
  above `endDecay`), and INFO lines note the ones that only change behavior: `endDecay` not below `queueSize` (Apple
  Pie's defaults, 32 and 32: aged-out foods land at 32, count nothing and are forgotten at the next meal, so the Diet
  Log keeps no out-of-window history; costs are unchanged) and a non-empty Apple Pie benefit list (kept off by
  `BenefitsHandlerMixin`; was a warning saying they apply on top, which they do not). Apple Pie's defaults therefore
  give no warning. `minFoodsToActivate` 0 opens the gate at once (the unique-food counter is then unused). Headless
  test: a respec of the two oldest window foods and a death on the default settings cost exactly what those foods
  contributed.
- **Fleet-Footed guard (P6).** `FleetFootedMixin` stays gated on `alexsmobs`. `RpMixinPlugin` also reads the target
  class (`MixinService` bytecode provider) and applies the mixin only when `EffectFleetFooted` has the int field
  `lastDuration` (shadowed) and `applyEffectTick(LivingEntity, int)` (`m_6742_`, the injection point); otherwise one
  warning names what is missing and the fix is off. The injection keeps `require = 0`, and the handler runs in a try:
  a failure logs one warning, flags the fix off and hands every later tick back to Alex's Mobs' own code (the tick is
  canceled only after the per-entity logic succeeded). Tested with Alex's Mobs 1.21.1 (12.2). An Alex's Mobs update
  can only turn the fix off, never crash the boot or a tick.

- **Diet marker art (owner, 2026-11-21).** The marker item's model points at vanilla `minecraft:item/map` (no copy); its own
  agent-made `diet_marker.png` (a flat square, a texture-rule breach) is retired. JEI hides the item (`RpJeiPlugin`).

### 12.10 Pack ladder (plan_rp_standalone.md P7, built 2026-11-21)

The scratchpad "rp changes" ladder as the pack's `defaultconfigs/solraspberrypie/ladder.json`, generated by
`tools/gen_rp_ladder.py` (`/cdevgen run rp_ladder`) from `kubejs/generation_scripts/food/rp_ladder.csv` (one row per
effect: sub-ladder, position, group, effect id, level, toggle, tagline text, `built` or `reserved`) and
`rp_ladder.json` (`densityRatio` 2.0 and the spans). It also writes the configs_edited backup and the tagline texts
(`kubejs/assets/solraspberrypie/lang/en_us.json`, keys `solraspberrypie.tagline.<namespace>.<effect>.<level>`); a flat rung
takes the tagline of the unlock row with its effect and level. The jar's default ladder is untouched. Every ladder
effect but Poison Immunity has a tagline (owner, 2026-11-23, `raspberry-pie-publish #23`; Steady's is "No slipping;
does not trigger plates, tripwires or dripleaf", `#22`). The generator also writes the same tagline keys as the last
block of the jar's own `assets/solraspberrypie/lang/en_us.json` (owner, 2026-11-23: the jar ships them too), keeping
the jar's other keys and layout.

- **Whole-number milestones** (owner, 2026-11-22). `rp_ladder.json` `decimals` 0: every threshold is computed
  unrounded, then each distinct value gets a whole number, strictly increasing, at the least total distance from the
  unrounded values (`spread` in the generator; ties round half up). Values that only differ in the decimals are kept
  apart, not merged: where two would round to the same number, one moves to the nearest free number (18.1 and 18.2 are 18 and 19).
  Reserved positions take part, so filling one later moves nothing. Values that were already equal (a pair, a group,
  the 2.5 slot and unlock) stay equal. Existing worlds keep their copy of the old ladder.
- **Shape.** 10 hearts (+1 each, 20 at the top) at 100/11 steps (9 to 91); armor toughness +1 with knockback
  resistance +0.1 at 17, 50, 83; no movement speed. Permanent (flat) effects 25 to 100 and unlockables 2.5 to 97.5 with
  the D10 falling density (positions in plan_food_diversity_1121.md section 7). A grouped heading (Brightening, Immunity,
  Dig, Water, Lava, Mobs, Spider, Eating) is one position with several unlocks. Five effect slots evenly spaced from the
  first unlock: 3, 22, 42, 61, 80 (agent default; count and start in `rp_ladder.json`). Start line on a pack server:
  about `Diet ladder: 83 rungs, 5 slots, 39 pool effects` (2026-11-23). Stimulation sits at position 21 with Clarity in the Immunity group (owner, 2026-11-23, its recorded scratchpad position; it was dropped 2026-11-21 while unread; 12.14).
- **Toggles** (defaults ruled by the owner, 2026-11-21). Flat: Night Vision, Phantom Bane, Double Jump on; Invisibility off. Pool:
  Night Vision and Steady (`fruitsdelight:astringent`) on; Glowing, Slow Falling, Magnet off. Phantom Bane is a toggle when slotted too
  (the switch is per effect).
- **Reserved position** (kept in the CSV, left out of the file): Wither Immunity (unlock 22, 47.1: no effect id yet,
  `food-tuning #47`). Satiated (`minecraft:saturation`) is in at 100 through the V10 exception.
- **Ladder bar ticks.** The Diet Log's Ladder bar draws a tick at each milestone of its window: the bar's `<` color
  (gray) once reached, its `>` color (purple) before; shorter than the highest-diversity mark.
- Headless: `tools/test_rp_ladder.sh` parses the generated file on vanilla registries (modded entries skipped in one
  line, hearts, pairs, slots within 1 of their spacing and whole, every threshold whole, toggles, every tagline key has
  its text) and runs `gen_rp_ladder.py --check`.

### 12.11 Owner notes 2026-11-22 (built 2026-11-22)

- **Gate on one line.** The Overview page's two warning lines ("N more unique foods" / "to activate diet", or Apple
  Pie's "Need to eat N more times" / "to activate benefits" without the unique gate) are one label: the first
  `Localization` call returns both texts joined by a space and the second label's `addChild` (ordinal 4) is dropped
  (`DiversityPageMixin`). The gate label gets its own fit scale (at most the shared one), so its length does not shrink
  "Current Food Diversity".
- **Ladder page lower.** The bar sits at 72 and the key at 92 book pixels from the page top (were 66 and 86), so three
  icons stacked on one milestone clear the "Highest" line; the gate text under the key moves with it.
- **Milestones columns.** Icon at the left, the value left aligned 3 px after it, then the text left aligned at one
  column for the whole ladder: after the widest milestone value plus 6 px.
- **Book tooltips.** A gray line under the name: Diet Log "Your recent meals, food diversity and diet ladder.", Food
  Dictionary "Every food you have ever eaten." (`FoodTooltips.bookLine`, ItemTooltipEvent; lang
  `solraspberrypie.tooltip.book.<namespace>`). The Liquid Diet Log keeps its own line.
- **Empty food tooltips (fixed).** Every food tooltip was an empty zero-size frame on some launches: Iceberg's title
  break and our reorder are both LOWEST listeners, and when Iceberg ran first the food rows landed above the break, so
  Legendary Tooltips took them for title lines. `FoodTooltips.afterTitle` now inserts after the break when it is
  already there. Mechanics: [agent note](agent_notes/food-tooltip-rows.md).

### 12.12 Publish fixes and scratchpad follow-ups (built 2026-11-22)

Plan: plan_rp_standalone.md section 10 (rulings P5, P6, P8, P9 of 2026-11-22). Rows `raspberry-pie-publish #2` to `#6`,
`food-tuning #49`, `#50`, `#55`.

- **Default unlocks.** Speed I, Haste I, Haste II and Strength I left (Apple Pie's flat rungs grant those effects before
  the first slot, so they could never be slotted); Jump Boost III came in; ten unlocks at 5 Regeneration I, 8.8 Jump
  Boost I, 12.8 Luck I, 17.1 Night Vision, 21.5 Water Breathing, 26.3 Fire Resistance, 31.4 Jump Boost II, 36.9 Slow
  Falling, 43 Jump Boost III, 50 Glowing. Start line `Diet ladder: 44 rungs, 4 slots, 8 pool effects`. V14 (3.3).
- **Replacements and covered slots.** `replaces` (3.3). A slot is covered when an active permanent buff holds its
  effect or an unlocked effect replaces it: grayed icon and name, hover "Covered by a permanent buff" or "Replaced by
  X" and "Changing this slot is free"; the Change button and picker say Free, the confirm panel ages out no foods. A
  replaced effect in a slot is not applied; a flat-covered one is held by the flat buff. The inventory widget leaves
  covered slots out. Network version 5 (the ladder carries the replacements).
- **Fleet-Footed view.** `ComputeFovModifierEvent` (LOW): when Alex's Mobs' Fleet-Footed speed modifier is on the
  player, the widening it causes is scaled by client config `effects.fleetFootedFov` (0.25; 1.0 = vanilla).
- **Table loader.** Each `solraspberrypie/diversity/*.json` is read on its own: a wrong shape skips the file (one error
  line), a value that is not a finite number of 0 or more is skipped (one warning count), the table is always assigned.
- **Others.** Hover debug class deleted; `require = 0` on the HUD and inventory effect-list redirects; Apple Pie
  leftover modifiers are stripped only when the name starts with the attribute's own id; Change and Drop counts cached
  per player while the window, active slots, ladder and `respecFoods` are unchanged; client icon and diversity-icon
  defaults are vanilla (the pack's values stay in `config/solraspberrypie-client.toml`); NOTICE renamed
  `NOTICE-nested-LGPL.txt`; `tools/rp_libs.py` (`fetch`, `check`, `sources DIR`).
- **Existing worlds** keep their `serverconfig/solraspberrypie/ladder.json`: the pack ladder's new `replaces` entries
  reach a world only after its copy is replaced from `defaultconfigs/` (PR-RP-18).

### 12.13 Ladder effects outside the jar (built 2026-11-23)

Plan: plan_rp_standalone.md (scratchpad rows, wave 2). Rows `food-tuning #51` to `#54`, `#56`, `#57`. Effect code is
Creative Common's (Prickly's is the Creative Delight hearth member's, Lava Walker's the Creative Brewing Vinery
member's); no Raspberry Pie code changed. Names only: every effect and potion id stays.

- **Steady** (`fruitsdelight:astringent`, `#51`). Fruits Delight already caps block friction at 0.6 (ice, soap blocks,
  slime). Common's `effect.SteadyRules` refuses Immersive Engineering's Slippery and Neapolitan's Slipping while Steady
  lasts and clears them when Steady is added. A light step: wooden, stone and weighted pressure plates and tripwires
  ignore the holder, big dripleaf does not tilt (`mixin/SteadyBlockTriggersMixin` on `Entity.isIgnoringBlockTriggers`,
  `SteadyWeightedPlateMixin`, `SteadyDripleafMixin`). Fruits Delight's Sliding (boats) is not a slip effect and stays.
  Modded pressure plates that do not ask `isIgnoringBlockTriggers` still trigger.
- **Leaf Piercing particles** (`#52`). `mixin/LeafPiercingTrailMixin` (end of every arrow tick, server) hands
  `effect.LeafPiercingTrail` the stretch the arrow flew: an arrow tagged `fruitsdelight:leaf_piercing` sends a puff of
  the leaf block's particles where it enters a leaf block (12) and a thin trail (2 every half block) while inside.
  After it leaves the leaves it keeps trailing the last leaf's particles until it stops moving (stuck in a block or
  spent on a hit) or 5 seconds (100 ticks) after it first entered leaves (owner, 2026-11-23); a stopped projectile
  never restarts its trail. Arrows and tridents only (Fruits Delight lets only those through leaves).
- **Prickly II** (`#53`). Level I spares tamed animals; from level II passive mobs are spared too: a mob that is not
  a monster and is not targeting the holder (an angered iron golem or bee is hurt). JEI text names both levels.
  Creative Delight hearth member `PricklyEffect.isPassive`.
- **Lava Walker crouch** (`#54`). Crouching skips the lift and the solid surface, so the walker sinks and the lava burns
  as usual; sneaking no longer spares the magma-block burn on the surface. vinery.md has the Vinery port note.
- **Names** (`#56`). Astringent -> Steady, Poison Resistance -> Poison Immunity (Alex's Mobs effect and potions), Fire
  Resistance -> Fire Immunity (effect, potion, splash, lingering, arrow). Common client script
  `effects/effect_names.js` adds each key under the namespace of the jar that ships it, so the KubeJS pack wins in that
  namespace's lang stack. It also writes the Steady and Leaf Piercing JEI texts. The Poison Resistance enchantment keeps
  its name.
- **Emptiness** (`#57`). Botania's Emptiness holds Patrol Nullifier and Phantom Bane as sub-effects (the scratchpad's
  "Emptiness a super of patrol nullifier and phantom bane"; Harmony, `#58`, will hold Emptiness). `effect.EmptinessParts`
  adds both hidden for Emptiness's time (skipped when a visible copy lasts as long) and removes the hidden copies when
  Emptiness is removed early; `CompoundEffects` lists them under "When Applied:" and in JEI.

### 12.14 Owner notes 2026-11-23 (built 2026-11-23)

TODO `food-tuning #63`; rows CQ-RPI-83 to -91 (`rows/RP.qb` section 7).

- **Reached permanent icons are steady** (owner, 2026-11-23). A reached permanent buff shows its effect icon alone in
  the kind-colored frame on the Ladder bar and Milestones; unreached ones still alternate diamond shard and effect
  (`DietLogPages.drawIcon`). Supersedes the food-tuning #9 round-4 note "permanent buffs cycle reached too".
- **No numeral on single-level effects.** An effect whose highest level anywhere on the loaded ladder (pool
  `max_level`, flat and unlock rungs) is 1 shows no numeral ("Night Vision", not "Night Vision I"); Speed I stays.
  `Ladder.maxLevel` + `DietEngine.effectName(Ladder, id, level)` build every name: ladder bar, Milestones, effect
  screen, inventory widget, chat lines, confirm panel. Clients use the ladder the server sent.
- **`/solrp diversity set <players> <value>`** (permission 2). Clears the player's food window and adds one
  "Adjustment" entry worth exactly that diversity (hidden item `solraspberrypie:diet_adjustment`: not edible, no tab,
  hidden in JEI, paper model), so current and highest diversity both become the value. Highest can go down: unlocks
  above are lost and slot choices drop to the highest still-unlocked level or empty. The entry ages out like a meal and
  shows in the Diet Log meal list. Saved and synced in Apple Pie's FoodList NBT key `solrp_adjust`
  (`FoodInstanceMixin` carries it and lets Apple Pie decode the non-edible item; `FoodListMixin.getComplexity` returns
  it). With the unique-food gate closed the reply says how many foods are missing; bonuses wait for the gate.
- **`/solrp diversity get <player>`** prints current and highest.
- **`/solrp ladder reset`** moves the world's `serverconfig/solraspberrypie/ladder.json` to
  `ladder.json.YYYY-MM-DD_HHMM.bak`, seeds a fresh copy from `defaultconfigs` (or the jar default), loads it, re-sends it
  and re-evaluates every online player (as a login). Addresses PR-RP-18 ([publish_readiness.md](publish_readiness.md))
  by command, not by auto-update.
- **Stimulation back on the pack ladder** (owner, 2026-11-23): unlock position 21 in the Immunity group with Clarity
  (threshold 45), tagline "Slowness and Mining Fatigue immunity"; the replace pair Stimulation -> Refreshing, Sweetening
  is written. Its expiry procedure (`StimulationEffectExpiresProcedure`, read from the 1.0.9 jar) only removes Slowness
  and Mining Fatigue, so a switch-off is safe (section 4, 9.2 updated). Ladder now 83 rungs, 5 slots, 39 pool effects;
  `tools/test_rp_ladder.sh` ALL 51 OK.
- **Ladder updates reach unedited worlds** (owner, 2026-11-23; PR-RP-18, `raspberry-pie-publish #11`). Beside the
  world's copy, `ladder.json.seeded` holds the SHA-1 of the ladder as seeded. At world load a copy that differs from the
  current default is replaced (old copy kept as a dated `.bak`, one log line) when it still matches that hash; an edited
  copy, or one seeded before the hash existed, is kept with one warning, and `/solrp ladder reset` replaces it. A copy
  equal to the default gets its hash written, so it is tracked from then on. Row SQ-RPI-92.
- **Log privacy** (PR-RP-22). Log lines name `<world>/serverconfig/...` and `defaultconfigs/...` instead of absolute
  paths and say "a player" instead of the player's name.
- **Fleet-Footed holds Feather Feet** (owner, 2026-11-23; scratchpad "holds feather feet as a super"). Creative Common's
  `effect.HeldEffectParts` (was `EmptinessParts`, now a holder -> parts table) adds Botania's Feather Feet hidden for
  Fleet-Footed's time and removes the hidden copy when Fleet-Footed is removed early; `CompoundEffects` lists it under
  "When Applied:", in the JEI effect hover and in the JEED text. Fleet-Footed stays an unlock at 79. Row CQ-RPI-93.
- **Why the recorded ladders never showed** (Workspace world): it ran its 2026-11-12 ladder copy (38 rungs); its copy
  was replaced from `defaultconfigs` (backup kept in the save, untracked). The Structures world still has the
  2026-11-22 copy (81 rungs): run `/solrp ladder reset` there. Toggles for single-level effects were already in the
  pack ladder (12.10).

### 12.15 Publish fixes: recipe, sync, table (built 2026-11-23)

TODO `raspberry-pie-publish #10`, `#13` ([plan_ready_now_1123.md](plan_ready_now_1123.md) section 3); rows SQ-RPS-11,
CQ-RPS-12 (`rows/RPS.qb` section 6). Network version 6 (client and server jars must match).

- **Diet Log from any food** (PR-RP-12, owner 2026-11-22). The recipe's food slot is the ingredient
  `{"type": "solraspberrypie:edible", "exclude": "solraspberrypie:diet_log_excluded"}` (`recipe/EdibleIngredient`):
  any item with food properties outside the tag. The `diet_log_foods` tag is gone. In the pack it takes foods only
  (owner ruling R5, 2026-11-23): `kubejs/data/creativedelight/kubejs/solapplepie/diet_log.js` adds
  `#creativedelight:food_type/drink`, `/hazard` (rotten flesh, spider eye, poisonous potato, pufferfish and the modded
  hazards), `#solraspberrypie:no_diet`, the lunchboxes, cat food, Everything Copper's copper nugget and Create Central
  Kitchen's unfinished assembly items to the exclusion tag (row SQ-RPS-13). Edible items with a drinking animation
  that are in no drink tag (Hot Cocoa, the coffees, Prickly Pear Juice, Strawberry Banana Smoothie, Mulberry Jam
  Bottle) still count as foods, so the Diet Log takes them.
- **State sync** (PR-RP-13). `DietEngine.sync` only marks the player; the state goes once at the end of the player's
  tick, so a food, a drink or a slot action sends one packet (a slot action sent two). The drink list rides on a
  state packet only when it changed (`RpData.drinksVersion`; a new player entity sends it once); the client keeps its
  last list otherwise. The Change and Drop counts stay cached per food window (12.12). Headless timing (encode +
  decode, a state with 256 drinks): 48.8 us and 9,626 bytes per packet before, 3.9 us and 518 bytes without the list;
  a slot action went from 2 packets (about 98 us, 19 KB) to 1 (about 4 us, 0.5 KB).
- **Server table on the client** (PR-RP-17, owner 2026-11-23). `S2CDiversity` carries the server's values and
  families on `OnDatapackSyncEvent` (login, and every player on `/reload`); the pack table is about 70 KB. A client
  with no integrated server never reads its own jars' tables (Apple Pie's values until the packet); it drops ids it
  does not know (one INFO line `Diversity table from the server: ...`) and forgets the table on leaving. Singleplayer
  and LAN hosts share the integrated server's table and ignore the packet.

### 12.16 Publish fixes: drop, memory, config, perf (built 2026-11-23)

TODO `raspberry-pie-publish #12`, `#14` to `#17`, `#19`, `#21`, `#24`, `#26` to `#28` ([plan_wave1_publish.md](plan_wave1_publish.md)
W-P3, [plan_wave2_publish.md](plan_wave2_publish.md) I12, I13, I15); rows CQ-RPS-14, CQ-RPS-15 (`rows/RPS.qb` section 8).

- **Drop loses one slot** (`#24`). The cause: after `/solrp diversity set` the window holds one Adjustment entry worth
  the whole diversity, so ageing out the oldest food took every slot. A drop now ages out foods from the oldest end
  until the highest active slot is lost and only that one: a food that alone would also take a lower slot is kept
  and the next oldest goes; the Adjustment entry is lowered to just under the highest slot's milestone (0.01 under,
  or half the gap to the next slot when that is smaller) and stays in the window. When no order loses exactly one
  slot, foods go from the oldest end as before. The hover count counts the lowered entry.
- **Aged-out foods leave Apple Pie's memory** (`#26`): 6.1. The server-start check follows: `minContribution` above 0
  is an INFO line now, not part of the warning block (aged-out foods count nothing whatever it is; only foods past the
  window still count a little), and the `endDecay` not below `queueSize` INFO line is gone (nothing is kept past the
  window any more). Apple Pie's defaults still give no warning.
- **Reached permanent buff icon** (`#21`): already steady since `food-tuning #63` (12.14; row CQ-RPI-90); the scratchpad
  note predates that build. No code change.
- **Food Dictionary Hardest** (`#27`): uneaten sort `HARDEST`, base diversity descending, then hunger descending, then
  name (Easiest mirrored).
- **Diet Log `extraWidth` default 48** (`#28`; pack config too).
- **refreshInterval** (`#14`, PR-RP-19): used as at most `effectDuration` - 20 ticks; a server start with a larger value
  logs one warning.
- **Lock-free table lookups** (`#15`, PR-RP-20): the diversity table is one volatile object (values, families and the
  family-head cache), read without a lock; per frame no config-icon key or stack copy (`Icons.configured`), the
  inventory widget's cells rebuilt only on a new state, the effect filters allocate a list only when they leave an
  effect out.
- **Missing capability** (`#16`, PR-RP-21): `RpData.get` keeps a detached instance per player entity without the
  capability (one warning per entity class), not a new throwaway object per call.
- **mods.toml** (`#17`, PR-RP-23): the mod list icon is `[modproperties] catalogueItemIcon` (read by the mod list mod of that name); optional
  dependency rows for AppleSkin (client) and Alex's Mobs.
- **Jar tags** (`#12`, PR-RP-16): the `#creativedelight:food_type/drink` entry of `solraspberrypie:drinks` is already
  optional (`required: false`); kept, like the Farmer's Delight and Brewin' entries.
- **Sources** (`#19`, PR-RP-25): SPDX header on all 68 files; four history phrases in comments reworded.
- `tools/test_rp_ladder.sh` ALL 59 OK (ageing out removes foods; the drop keeps a heavy oldest food).

## History

Lines moved out of code comments (2026-11-22, plan_rules_fixes.md section 17):

- 2026-11-21: Satiated (minecraft:saturation) is the one instant effect allowed as a flat rung (owner, 2026-11-21): it ticks every tick while active, so a refresh only extends it (raspberry_pie.md 3.3, 4) (development/solRaspberryPie/src/main/java/com/SaintAlfonzo/solraspberrypie/ladder/LadderLoader.java)
