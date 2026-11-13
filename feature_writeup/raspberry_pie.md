# Spice of Life: Raspberry Pie Addition (design)

Status: **BUILT 2026-11-12, server rows scratch PASSED, client rows queued** (`solRaspberryPie-1.0.0.jar` of 21:27 in `mods/`; queue section "SQ-RP",
section 14). Design decisions: owner 2026-11-10, two rounds on 2026-11-11 and two on 2026-11-12 (chat). TODO
`food/types-and-tuning #21` (selectable bonus), touches `#12` (ladder values: the pack ladder is a draft) and `#4`.
Build choices not in the design are in section 15. Waiting on rebuilds the owner does (those jars carry other unbuilt
commits): Common (`item_dict.js` key `diet_log`, `liquid_diet_log`) and Creative Delight (`names.js` "Diet Log",
`solapplepie/diet_log.js` removing Apple Pie's book + potato recipe) together; Creative Brewing (Lava Walker burn).
Existing worlds (Workspace, Structures, New World) keep their old `serverconfig/` copies of Apple Pie's settings
(benefits, `resetOnDeath`, blacklist, `minFoodsToActivate` 10): edit them by hand, or run
`python3 tools/gen_sol_drink_blacklist.py --worlds` for the blacklist.

Jar facts below were checked on 2026-11-11 in `solapplepie-1.19.2-2.2.0.jar` (package `com.tarinoita.solsweetpotato`)
and Forge 1.19.2-43.5.1. Pack Apple Pie settings: `queueSize` 256, `startDecay` 0, `endDecay` 32,
`minContribution` 0.0, `shouldForbiddenCount` true.

## 1. What it is

A new in-tree mod that adds selectable diet bonuses on top of Spice of Life: Apple Pie Edition. It is an add-on that
mixes into Apple Pie, not a fork: no Apple Pie code is copied in.

| | |
|---|---|
| Display name | Spice of Life: Raspberry Pie Addition |
| Mod id | `solraspberrypie` (permanent; the display name may change, the id may not) |
| Tree | `development/solRaspberryPie` (root `settings.gradle`, `build-logic/`) |
| Sides | both (screens, widget, overlay messages on the client; all rules on the server) |
| Depends on | `solapplepie` 2.2.0 exactly (mixins and accessors on its private members) |
| Licence | own code GPLv3; Apple Pie is LGPL-2.1, so mixins into it and linking are fine; credit Apple Pie and its forks |
| `mod_sides.md` | add a row when built |

Carrot Edition: the GitHub text the owner found is the GTNH 1.7.10 fork (LGPL-3.0, original code public domain). Our
1.19.2 jar declares `LGPL-2.1` in `mods.toml`; CurseForge says All Rights Reserved. Route: **copy no Carrot code.**
The progress line (6.3) and the Liquid Diet Log (7) are written from scratch, modelled on behaviour only, so the
licence question does not matter.

## 2. Who owns what

| Owner | Owns |
|---|---|
| Apple Pie | every diversity calculation, food memory (unique foods with "meals since last eaten", forgotten after 256 meals), the decay window, the `foodsEaten` counter and `minFoodsToActivate` value, the Diet Log book and its pages |
| Raspberry Pie | the whole bonus ladder: health, armour toughness, knockback resistance, flat effects, slot milestones, effect unlocks; the `minFoodsToActivate` gate (reads Apple Pie's counter); applying and removing every rung; slot choices; ageing foods out of the window (respec and death, 6); messages; the inventory widget; the effect screen; the Liquid Diet Log |
| Apple Pie config | `benefitsUnparsed` and `thresholds` emptied; Apple Pie applies nothing itself (verify when built that its parser accepts two empty lists) |
| Apple Pie screens | show Raspberry Pie's ladder (mixins on its client pages, 6.3) |

Item names: Apple Pie's book is renamed **Diet Log** (was Diet Tracker; `names.js` in the Creative Delight tree, the
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
| Attribute tier | armour toughness +2, knockback resistance +1 | removed | no |
| Flat effect | Speed I between slot 2 and slot 3 | removed (the "permanent buffs" in the Diet Log legend; permanent means not chosen, not never-lost) | yes, before the slots, different style |
| Slot milestone | slot 3 at diversity 30 | slot switches off (greyed), its choice is kept | yes |
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
| V10 | Refresh-unsafe effect as a flat bonus or in the pool | Absorption, Health Boost, instant effects | refilled absorption, clamped health, instant effect every 20 s (4, 9.1) | **decided 2026-11-12**: as proposed in 3.3 (recommended) |
| V11 | Same effect in a flat bonus and the pool | flat Speed I, Speed in the pool | **decided 2026-11-12**: allowed. The pack unlocks the effect as slottable first and as a flat bonus later; once the flat bonus is active the effect is hidden from the picker. A slot that already holds it keeps it (5.1, 9.5) | allow |
| V12 | Threshold higher than any player can reach | a rung at 500 | an unreachable rung | **decided 2026-11-12**: as proposed in 3.3 (recommended) |
| V13 | File missing or malformed JSON | a stray comma | server cannot build a ladder | **decided 2026-11-12** (Q-H): fall back to the built-in default ladder with a loud error |

## 4. Slot pool (default)

Slottable: Speed, Haste, Strength, Jump Boost, Night Vision, Water Breathing, Luck, Regeneration (level cap 1),
Shrinking (Fruits Delight), every pack beacon effect (vanilla beacon effects except Resistance; Better Beacons'
Long Reach, Patrol Nullifier, Phantom Bane).

Not slottable:

| Effect | Why |
|---|---|
| Resistance (any level) | owner rule; overrides the "all beacon effects" rule |
| Instant effects: Instant Health, Saturation, Instant Breath, Restfulness, Teleport | fire again on every refresh |
| Absorption, Health Boost | a refresh refills absorption / clamps health |
| Nourishment, Comfort, Saturation | remove the reason to eat varied food |
| Invisibility, Hero of the Village, Fire Resistance | remove a mechanic (mobs and PvP, villager and Lightman's Currency prices, lava) |
| Dolphin's Grace, Conduit Power | owner rule |
| Caffeinated, Rest, Stimulation, Ender Phasing, Raging | logic on expiry or removal would fire on every switch-off; check each before allowing |
| Lava Walker (Vinery) | slottable only after a Vinery change: walking on lava burns like a magma block unless the player has Fire Resistance, and dropping into lava burns as usual. Vinery is our port in Creative Brewing (`members/vinery`), so the change is ours to make. **Decided 2026-11-12:** the change goes into the Vinery port (a player-visible behaviour change there; note it in that member's writeup and the changelog when built) |
| Fleet-Footed (Alex's Mobs) | **slottable with a mixin fix (decided 2026-11-12).** +20% move speed only while sprinting and airborne, so a jump-sprint boost. Its timer state lives in fields on the shared effect object, so in multiplayer all players with it share one timer (flicker or a stuck bonus). **Fix:** a mixin moves that state into per-player data (see the Fleet-Footed fix below the table) |

Shrinking and Speed change the player (size, field of view) every frame and will be hidden from the HUD (9.5);
owner chose to allow both.

## 5. Player choices and costs

The only player choices are **which effect at which level in which slot.** Everything else follows the diet.

### 5.1 Slot rules

- **Active** slot: its milestone is met. **Greyed** slot: its milestone is not met (never reached, lost, or after
  death). A greyed slot keeps its stored choice but **cannot be interacted with**: no fill, change, level change or
  drop, and it is not a drop target on the effect screen.
- **Active filled slots are packed from slot 1 upward.** Adding an effect to any empty active slot puts it in the
  **lowest** empty active slot (auto-shift), whichever slot the player clicked or dragged onto. Greyed slots keep their
  stored choice in place and are not shifted, so a greyed empty slot can sit below greyed filled ones (12.1 Q-B,
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
| Change a slot to a different effect | age out the **8 oldest foods in the decay window** (configurable count; was 5) (6) |
| Drop (clear) a slot | age out foods from the oldest end of the window **until the highest active slot is lost**. The dropped effect is removed and the choices of the **active** slots above it shift down one slot to fill the gap; the vacated highest active slot is left **empty and greyed** (owner, 2026-11-11). Choices stored in greyed slots stay where they are (Q-B option b, 2026-11-12) |
| Swap two slots | not possible |
| Re-applying a regained slot's stored choice | automatic, free |

Drop example: slots 1-3 active with Speed, Haste, Luck; slot 4 greyed and empty. Drop slot 1: foods age out until
slot 3 is lost; Haste moves to slot 1, Luck to slot 2; slot 3 is empty and greyed.

There is no XP cost: the diversity cost is always there.

### 5.3 Why the oldest, and why the cost climbs

The newest foods are the easiest to replace (the player just had them), so the oldest go. With the pack's decay a food
eaten k meals ago is worth `complexity x (1 - k/32)` (Apple Pie's `calculateTimePenalty`), so the oldest foods are
worth little and the first respec is cheap. Aged-out foods have left the window, so the next respec takes the next 8
oldest, which are newer and worth more. Example, 32 distinct complexity-1 foods in the window (diversity 16.5):

| Respec (no eating between) | Foods aged out (meals ago) | Diversity lost | Left |
|---|---|---|---|
| 1st | 24-31 | 1.1 | 15.4 |
| 2nd | 16-23 | 3.1 | 12.3 |
| 3rd | 8-15 | 5.1 | 7.1 |
| 4th | 0-7 | 7.1 | 0 |

Eating refills the window from the new end, so the climb eases off over roughly 8 meals per respec. Repeated foods and
complexity weights change the numbers, not the shape. The first respec is meant to be a near-free correction;
serial swapping is what costs. Small windows (decided 2026-11-12): the change is **allowed** even when the respec switches off the slot being changed; the new choice is stored in the now greyed slot and shown on the confirm panel as such.

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

Respecs and death move foods **out of the decay window** to the front of the out-of-window part of the food memory,
instead of marking them grey (owner, 2026-11-11). A moved food:

- has its "meals since last eaten" raised past `endDecay` (32), so it counts `minContribution` (0 in the pack);
- keeps its order relative to the other moved foods: each moved food gets `k + (32 - smallest k in the group)`,
  capped at 255. A respec of the foods at 24-31 puts them at 32-39; death (the whole window, 0-31) puts them at 32-63;
- shows in the Diet Log history in its new place, drawn like any other old food (no grey drawing);
- counts in full again when eaten (a fresh eat resets its count to 0, as Apple Pie already does);
- is forgotten by Apple Pie once it reaches 256 meals, like any food. Nothing extra is stored, so nothing needs
  pruning.

"Oldest in the decay window" = the foods with the largest count still below 32. If fewer foods than needed are in the
window, all of them move.

This relies on `minContribution` being 0: above 0, moved foods still count a little and every cost shrinks. Handling:
Q-E: a warning at server start when it is above 0 (decided 2026-11-12).

### 6.2 Implementation

- Accessor mixin on `FoodList.uniqueFoods` (private `Map<FoodInstance, Integer>`); Raspberry Pie rewrites the counts,
  then calls Apple Pie's public `CapabilityHandler.syncFoodList(player)`, since Apple Pie only syncs on eat, respawn,
  dimension change and login.
- No extra NBT key, no diversity redirect, no tooltip mixin: Apple Pie's own diversity, Diet Log and tooltip
  (`calculateDiversityContribution(food, count)`) all read the moved count and show the right value.

### 6.3 Diet Log changes (client mixins on Apple Pie pages)

- Button on the Diet Log that opens the effect screen.
- Milestone page: Raspberry Pie's ladder instead of Apple Pie's config list (Apple's is empty).
- **Progress line** (behaviour like Carrot's food book, own code): a bar across diversity with icons at each rung:
  heart (health), knockback resistance, armour toughness, slot, flat bonus (permanent buff), one-time unlock.
  Reached rungs coloured, unreached grey, hover for the value.

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
| Food eaten | every rung re-evaluated at once: gains and losses both apply immediately (flicker accepted for now, to be judged in play: Q-A, RP-23) |
| Paid action confirmed | everything listed on the confirm panel applies at once (5.4) |
| Death | Apple Pie's `resetOnDeath` set to false; Raspberry Pie ages the whole window out (6.1) **and resets the foods-eaten counter** (the gate of `minFoodsToActivate`; decided 2026-11-12), after Apple Pie's `onClone` copy (event priority, or on respawn). On respawn every rung except one-time unlocks is off, every slot greyed with its choice kept, max diversity unchanged, and **one** message (10) |
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
| Stimulation (Create Confectionery) | ticks every tick through its "expires" procedure | none | not slottable until its procedure is read |
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
- Widget on or off is a client setting only; the server sends the same effects either way.
- **Flat effects (decided 2026-11-12):** never shown on the in-game HUD (top-right icons), at any time. In the
  inventory screen they show on the widget when it is enabled, and like any vanilla effect in the inventory's effect
  list when it is off. Slot effects follow the first bullet (hidden when the widget is on, vanilla when off).
- A flat bonus and a slot of the same effect can coexist only when the slot was filled before the flat bonus arrived
  (5.1). The effect is applied once at the higher of the two levels; the covered slot shows as covered on the effect
  screen, the widget shows the flat bonus.

## 10. Messages

On the jukebox "Now playing" line above the hotbar (overlay message, fixed colour, not rainbow):

`You {gained/lost} a bonus due to your diet: {name}`

| Colour | When |
|---|---|
| green | gained from a flat bonus, health, toughness or knockback tier |
| blue | gained from a slot (re)activating |
| red | lost, any kind |
| yellow | a slotted effect is covered by a newly added identical flat bonus (queued after the "flat bonus added" line); an effect still sustained by another bonus when its slot is lost |

**Covered slot warning** (decided 2026-11-12): only when a flat bonus is added that is **identical** (same effect and
level) to a currently slotted effect, the green "gained" line is followed (queued after it) by a yellow line: `Your
slotted {effect} is covered by a flat bonus and can be reslotted`. A flat bonus of a different level for a slotted
effect gives no warning.

**Sustained line** (decided 2026-11-12): when a slot is lost (greyed, dropped or switched off) but its effect stays
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
- Shows only what is active: flat bonuses first (own style), then active slots (effect icon, level). Greyed slots
  are not shown here (they appear greyed on the effect screen). Hover gives the tooltip
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
- Flat bonuses at the top (read only, own style), then slots in order; greyed
  slots show their stored choice and are **read only** (5.1).
- Choosing an effect: dropdown/picker grid of unlocked effects (drag from the grid onto a slot as a second option to
  evaluate in the boilerplate). Whatever empty active slot is targeted, the effect lands in the lowest empty active
  slot (5.1). Level dropdown per active slot (free).
- Paid actions open the confirm panel (5.4).

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
hand-built widgets grow too large (check its licence first). LDLib is not worth the extra jar.

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
| Q-A | Accept the flicker for now; **needs in-game testing** to see whether it is a problem (test RP-23). Fallback if it is: the margin option (a rung lost only when diversity is about 0.5 below its threshold). |
| Q-B | Option b: only active choices shift on a drop; greyed slots keep theirs (5.1, 5.2). |
| Q-C | Allow the change and store the choice greyed (5.3). |
| Q-D | An effect already slotted is not offered for another slot (5.1). |
| Q-E | Warn at server start when `minContribution` is above 0 (6.1). |
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
  widget is greyed out. The widget and the Diet Log stay readable. (This also tells the paused player why nothing
  applies.)
- **Resume:** re-evaluates the player from current state at once; effects come back and the normal gain lines show.

### 12.2 Fleet-Footed fix (decided 2026-11-12)

The bug: Fleet-Footed (+20% speed only while sprinting and airborne) keeps its state in fields on the single shared
effect object (the "last duration" it saw), and removes its speed modifier when the duration stops counting down as
expected. With several players the fields overwrite each other (flicker or a stuck bonus), and Raspberry Pie's periodic
refresh makes the duration jump, so the modifier would be dropped or never dropped even for one player.
Fix: a mixin on the effect's tick/removal code that keeps the "last duration" per entity (a map keyed by entity, or a
tag on the effect instance) and treats a refresh as a normal step, so state is no longer shared. Tested by RP-33. It
is in the default pool once the mixin is in.

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

## 14. Tests

Live rows are in [scratch_queue.md](testing/scratch_queue.md) section "SQ-RP" (fresh world). Map:

| Test | Queue row | | Test | Queue row |
|---|---|---|---|---|
| RP-01 boot, no Apple Pie benefits | SQ-RP-01, -02 | | RP-21 stale preview | CQ-RP-19 |
| RP-02, RP-03 gain and loss lines | CQ-RP-03, -23 | | RP-22 Apple Pie leftovers stripped | CQ-RP-20 |
| RP-04 fill lands lowest | CQ-RP-04 | | RP-23 flicker (Q-A, owner judges) | CQ-RP-21 |
| RP-05 free level change | CQ-RP-05 | | RP-24 drinks leave diversity alone | CQ-RP-15 |
| RP-06 change ages out 8, preview matches | CQ-RP-06 | | RP-25 books on first join | CQ-RP-01 |
| RP-07 drop shifts down | CQ-RP-07 | | RP-26 flat never on HUD | CQ-RP-12 |
| RP-08 milk, totem, /effect clear | CQ-RP-08 | | RP-27 waits for another overlay line | CQ-RP-22 |
| RP-09 aged food counts in full again | CQ-RP-09 | | RP-28 flat hides from picker | CQ-RP-23 |
| RP-10, RP-31 death | CQ-RP-10 | | RP-29 respec greys its own slot | CQ-RP-24 |
| RP-11 widget | CQ-RP-11 | | RP-30 panel lists losses | CQ-RP-06 |
| RP-12 effect display | CQ-RP-04, -12 | | RP-32 paid-action red lines | CQ-RP-07 |
| RP-13 potion of the same effect | CQ-RP-13 | | RP-33 Fleet-Footed two players | CQ-RP-26 |
| RP-14 message queue | CQ-RP-03 | | RP-34, RP-38 covered slot line | CQ-RP-23 |
| RP-15 Diet Log pages | CQ-RP-14 | | RP-35, RP-20 missing-mod entry | SQ-RP-03 |
| RP-16 Liquid Diet Log | CQ-RP-15 | | RP-36 book recipes | CQ-RP-02, SQ-RP-06 |
| RP-17 dedicated server | CQ-RP-16 | | RP-37 pause | SQ-RP-05, CQ-RP-25 |
| RP-18 greyed slots read only | CQ-RP-17 | | RP-39 sustained line | CQ-RP-23 |
| RP-19 Regeneration rhythm | CQ-RP-18 | | V13 broken ladder file | SQ-RP-04 |

Extra rows: CQ-RP-27 Lava Walker burn (Vinery port), CQ-RP-28 Shrinking in a 1-block gap (9.2).

Results: r1112 ([scratch_run_2026-11-12.md](testing/scratch_run_2026-11-12.md) 8): SQ-RP-01 to -06 PASS. The 20:47
jar did not load (`solapplepie` range `[2.2.0]` against Apple Pie's jar version `1.19.2-2.2.0`); fixed to
`[1.19.2-2.2.0]` in `mods.toml`, jar of 21:27 installed. SQ-RP-06 needs no Creative Delight rebuild: `diet_log.js`
loads from the pack's `kubejs/data` folder. Client rows CQ-RP-01 to -28 not run.

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
  the jar's built-in default (vanilla only) is `solraspberrypie/default_ladder.json`.
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
- **Recipes.** Diet Log: own recipe type `solraspberrypie:diet_book` (shapeless with an excluded tag), book + an item
  of `#solraspberrypie:diet_log_foods` (vanilla foods and `#creativedelight:food_type`) that is not in
  `#solraspberrypie:diet_log_excluded` (drinks, and carrot: book + carrot stays the Food Dictionary's). JEI lists
  drinks in its ingredient cycle (display only). Liquid Diet Log: book + `#solraspberrypie:drinks` (the pack drink
  tags, Farmer's Delight and Brewin' drink tags, potions, milk bucket). Apple Pie's book + potato is removed by
  `kubejs/data/creativedelight/kubejs/solapplepie/diet_log.js`.
- **Pause list** is kept in world data (`data/solraspberrypie_paused.dat`) so offline paused players are listed.
- **Fleet-Footed:** the shared state that matters is `removeEffectAfter` (`lastDuration` is written and read in the
  same per-entity call); the mixin keeps that countdown per entity. Applied only with Alex's Mobs.
- **Diet Log pages:** markers are coloured squares with a legend (heart/toughness/slot/flat/unlock), not icons yet.
- **Widget:** vertical beside the inventory, horizontal above or below; JEI exclusion through a JEI plugin. The
  vanilla effect list was not moved (11.1 "to evaluate").
- **Not built:** drag from the picker grid onto a slot (11.2 second option); drinks taken from placed blocks have no
  Liquid Diet Log hook (none found that are drinks).
