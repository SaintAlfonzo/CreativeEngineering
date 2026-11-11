# Spice of Life: Raspberry Pie Addition (design)

Status: **DESIGN, not built.** Owner decisions 2026-11-10 and 2026-11-11 (chat). TODO `food/types-and-tuning #21`
(selectable bonus), touches `#12` (ladder rework) and `#4` (Spice of Life config). Open questions in section 12.
Built so far: `minFoodsToActivate` 10 -> 5 in `defaultconfigs/` and `configs_edited/server_config/` (2026-11-11);
existing worlds (Workspace, Structures, New World) still have 10 in their `serverconfig/`.

## 1. What it is

A new in-tree mod that adds selectable diet bonuses on top of Spice of Life: Apple Pie Edition.

| | |
|---|---|
| Display name | Spice of Life: Raspberry Pie Addition |
| Mod id | `solraspberrypie` (permanent; the display name may change, the id may not) |
| Tree | `development/solRaspberryPie` (root `settings.gradle`, `build-logic/`) |
| Sides | both (screens, widget, overlay messages on the client; all rules on the server) |
| Depends on | `solapplepie` 2.2.0 (hard) |
| Licence | own code GPLv3; Apple Pie is LGPL-2.1, so mixins into it and linking are fine; credit Apple Pie and its forks |
| `mod_sides.md` | add a row when built |

Carrot Edition: the GitHub text the owner found is the GTNH 1.7.10 fork (LGPL-3.0, original code public domain). Our
1.19.2 jar declares `LGPL-2.1` in `mods.toml`; CurseForge says All Rights Reserved. Route: **copy no Carrot code.**
The progress line (6.3) and the Liquid Diet Log (7) are written from scratch, modelled on behaviour only, so the
licence question does not matter.

## 2. Who owns what

| Owner | Owns |
|---|---|
| Apple Pie | every diversity and diet calculation, food memory (unique foods with "meals since last eaten"), the decay window, `minFoodsToActivate`, the Diet Log book and its pages |
| Raspberry Pie | the whole bonus ladder: health, armour toughness, knockback resistance, flat effects, slot milestones, effect unlocks; applying and removing all of them; slot choices; greying foods; the check clock; messages; the inventory widget; the effect screen; the Liquid Diet Log |
| Apple Pie config | `benefitsUnparsed` and `thresholds` emptied; Apple Pie applies nothing itself |
| Apple Pie screens | show Raspberry Pie's ladder (mixins on its client pages, 6.3) |

Item names: Apple Pie's book is renamed **Diet Log** (was Diet Tracker; `names.js` in the Creative Delight tree, the
changelog bullet "called Diet Tracker" gets rewritten). The new drink book is the **Liquid Diet Log** (Raspberry Pie item).

## 3. The ladder

Every value is configurable: threshold, type, magnitude, order. One server JSON file
(`config/solraspberrypie/ladder.json`), synced to clients on login and on reload. Nested data is why it is JSON,
not a TOML string list like Apple Pie's.

Four kinds of rung, which can share thresholds:

| Kind | Example | Below the threshold | Shown on the effect screen |
|---|---|---|---|
| Health tier | +1 heart | removed | no |
| Attribute tier | armour toughness +2, knockback resistance +1 | removed | no |
| Flat effect | Speed I between slot 2 and slot 3 | removed (the "permanent buffs" in the Diet Log legend; permanent means not chosen, not never-lost) | yes, before the slots, different style |
| Slot milestone | slot 3 at diversity 30 | slot switches off, its choice is kept and greyed | yes |
| Effect unlock | Strength II at 70 | **never lost** ("one-time unlocks"): driven by the player's max diversity | in the picker |

Effect unlocks: per player we store **max diversity ever reached**, mapped onto the current ladder, so ladder edits
apply to existing players. Death and greying never lower it.

Draft ladder shape (values to be set with `#12`): health every few tiers, toughness and knockback tiers spread
between, 4-5 slots, flat Speed I between slot 2 and 3, effect unlocks spread across the whole range with higher
levels later.

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
| Lava Walker (Vinery) | slottable only after a Vinery change: walking on lava burns like a magma block unless the player has Fire Resistance, and dropping into lava burns as usual. Vinery is our port in Creative Brewing (`members/vinery`), so the change is ours to make |
| Fleet-Footed (Alex's Mobs) | **pending (12, Q4).** +20% move speed only while sprinting and airborne, so a jump-sprint boost. Its timer state lives in fields on the shared effect object, so in multiplayer all players with it share one timer (flicker or a stuck bonus). Needs a mixin fix, or stays out |

Shrinking and Speed change the player (size, field of view) every frame and will be hidden from the HUD (9);
owner chose to allow both.

## 5. Player choices and costs

The only player choices are **which effect at which level in which slot.** Everything else follows the diet.

| Action | Cost |
|---|---|
| Fill an empty slot | free |
| Change a slot's level (any unlocked level of the same effect) | free |
| Change a slot to a different effect | grey the **5 oldest foods in the decay window** (configurable count) |
| Drop (clear) a slot | grey foods from the oldest end of the decay window **until the highest active slot is lost**; the dropped effect is removed and the choices above it shift down one slot in order; the top slot is empty when regained (approved 2026-11-11) |
| Swap two slots | not possible |
| Re-applying a regained slot's stored choice | automatic, free |

There is no XP cost: the diversity cost is always there.

Every paid action shows a confirm panel before anything happens: diversity now and after, the foods that will grey
(icons), and every slot, flat bonus and tier that will switch off. The server simulates this with Apple Pie's static
`foodDiversity(set)`, then validates and applies it on confirm.

**Grace period (not advertised):** if a paid action would make the player lose the slot being changed, that slot is
held for 5 minutes (configurable) before diversity is checked again for it.

## 6. Greying foods

### 6.1 Rule

A greyed food counts 0 toward diversity. It stays in Apple Pie's memory and shows greyed in the history. Eating that
food again un-greys it (it is a fresh eat). "Oldest in the decay window" = the foods with the largest "meals since
last eaten" that are still inside `endDecay` (32). If fewer foods than needed are in the window, all of them grey.

### 6.2 Implementation (least invasive)

- One `@Unique` set of greyed foods added to Apple Pie's `FoodList`, written into its own `serializeNBT` /
  `deserializeNBT` under a new key. Apple Pie syncs the food list to the client as that same NBT, so the client gets
  the greyed set for free.
- `FoodList.foodDiversity()` (instance) calls the static `foodDiversity(entrySet)`: redirect that one call to pass
  the entry set minus greyed foods. Every Apple Pie system that reads diversity (benefits, Diet Log pages) then sees
  the new value. Exact 0, no tiny-value trick needed.
- `addFood(item)`: inject at head, remove that food from the greyed set.
- Item tooltips (`simulateFoodAdd`) need no change: eating a greyed food counts in full again, which is what the
  tooltip already shows.

### 6.3 Diet Log changes (client mixins on Apple Pie pages)

- Button on the Diet Log that opens the effect screen.
- Milestone page: Raspberry Pie's ladder instead of Apple Pie's config list (Apple's is empty).
- History page: greyed foods drawn greyed.
- **Progress line** (behaviour like Carrot's food book, own code): a bar across diversity with icons at each rung:
  heart (health), knockback resistance, armour toughness, slot, flat bonus (permanent buff), one-time unlock.
  Reached rungs coloured, unreached grey, hover for the value.

## 7. Liquid Diet Log

- **Alcohol only** (2026-11-11). Tea, coffee, juice, milk and other drinks stay in the normal diet.
- Alcoholic drinks give **no diet credit** and do not show in the Diet Log (Apple Pie blacklist, generated).
- They are tracked in the Liquid Diet Log: a book item in Raspberry Pie with its own player data, the last 256
  drinks, history only. It gives the player **nothing**: no health, no effects. The drinks keep their own effects
  (tipsy and so on); only diet credit is removed.
- Source list: the pack's existing `#creativedelight:food_type/drink/alcohol` tag. A generator (listed in
  `tools/generators.json`) writes Apple Pie's blacklist from it, so the two never drift.
- How the book is obtained, and how the 256 are counted: Q7, Q10.

## 8. Timing

| Event | When |
|---|---|
| Gain of any rung (health, attribute, flat, slot) | immediately when the food is eaten |
| Loss of any rung | at the periodic check, every 5 minutes (configurable), per player |
| Paid action that would remove the slot being changed | grace 5 minutes, then checked |
| Death | Apple Pie's `resetOnDeath` set to false; instead Raspberry Pie ages every food in the decay window out of it (history kept, diversity 0). On respawn all rungs except one-time unlocks are off at once. Slot choices kept. Max diversity unchanged |
| Login | re-apply from current state; logout removes nothing permanent |

Gains on eat, losses on the clock: slots do not flicker, and the grace period is hidden inside the normal cadence.

## 9. Applying effects

### 9.1 Refresh

- Raspberry Pie applies every flat and slot effect itself: duration 40 s (800 ticks), **re-added unconditionally
  every 20 s** (400 ticks, owner choice), staggered across players, no particles.
- Cost: Apple Pie's own method (one effect lookup per player, per benefit, per tick, re-adding at 10 s or less left)
  is already cheap. The 20 s rule removes even that, for one packet per effect per player every 20 s.
- Both numbers stay multiples of 50 ticks so Regeneration keeps an even heal rhythm (9.2).
- Re-adding re-runs the effect's attribute modifiers (same id, so harmless for Speed, Strength, Haste, Luck); it is
  why Absorption and Health Boost are excluded.

### 9.2 Duration and per-tick logic, effect by effect

Checked in the jars (2026-11-11). "Ticks every tick" means the effect runs code each tick while active, independent of
how much time is left; only the rows marked **duration-dependent** care about our refresh.

| Effect | Logic | Refresh impact | Verdict |
|---|---|---|---|
| Speed, Haste, Strength, Luck | attribute modifiers only | none | slottable |
| Jump Boost, Water Breathing, Long Reach, Patrol Nullifier, Phantom Bane | no tick logic; other code checks "has effect" | none | slottable |
| Regeneration | **duration-dependent:** heals when time left is a multiple of 50 ticks (25 at level II) | resetting to 800 keeps the rhythm because 800 and 400 are multiples of 50; any other numbers would skip or double heals | slottable, level cap 1 |
| Night Vision | **duration-dependent (client):** the screen flickers when 10 s or less is left | never below 20 s left | slottable |
| Shrinking (Fruits Delight) | attribute modifiers, then recalculates the player's hitbox on add and on remove | each refresh removes and re-adds in one tick: the hitbox grows and shrinks again in the same tick. Test in a 1-block gap for suffocation or a push | slottable after RP test |
| Fleet-Footed (Alex's Mobs) | **duration-dependent:** saves "last duration" into a field on the shared effect object and removes its speed modifier when the duration stops changing as expected | a refresh jumps the duration, and all players share the one field | out unless fixed by mixin (Q4) |
| Lava Walker (Vinery port) | ticks every tick: lifts the player out of lava and makes the lava surface solid; no duration logic | none | slottable after the burn change (4) |
| Lava Vision (Alex's Mobs) | ticks every tick while time is left; no duration logic | none | slottable if wanted (not in the default pool) |
| Caffeinated (Farmer's Respite) | ticks every tick: rewrites the "time since rest" stat (phantom timer); Creative Delight already patches it | none from the refresh, but it controls phantoms | not slottable |
| Rest (Create Confectionery) | ticks every tick: removes phantoms within 4 blocks (named "expires" but runs every tick) | none | not slottable (removes a mechanic) |
| Stimulation (Create Confectionery) | ticks every tick through its "expires" procedure | none | not slottable until its procedure is read |
| Ender Phasing (Ender's Delight) | ticks every tick: damages the player in water or rain | none; harmful side | not slottable |
| Restfulness (Upgrade Aquatic) | instant-style tick | fires on every refresh | not slottable |

### 9.3 Effect clears ("clear all" and cures)

Found by scanning every jar in `mods/` and our own scripts (2026-11-11). Two kinds:

**Cures by item (milk):** these remove only effects that list milk as a cure. Our effects get **no cure items**, so
every source in this group skips them with no extra code.

| Source | Mod |
|---|---|
| Milk Bucket | vanilla |
| Wooden Milk Bucket | Creative Delight |
| Splash and lingering milk bottles, thrown milk bottle | Splash Milk |
| Open-ended pipe spraying milk | Create |
| Warp plate | Waystones (purpose to read) |
| `/heal` | FTB Essentials (admin) |
| Balm helper | Balm (used by Balm mods; to read) |

**Clear all:** these remove every effect regardless of cures. Blocking one means cancelling Forge's effect-removal
event for our instances (our own removals pass through a flag).

| Source | Mod | Proposed |
|---|---|---|
| Totem of Undying | vanilla | keep slot effects |
| Charm of Undying (totem in a curio slot) | Charm of Undying | keep slot effects |
| Absolution | Botania | keep slot effects |
| Canned Powdered Milk (`clear_effects`) | Creative Delight (`cans.js`) | keep slot effects |
| Collective gear helper | Collective (used by Serilum mods; to read) | keep slot effects |
| Any KubeJS script calling clear-all | KubeJS | keep slot effects |
| `/effect clear` | vanilla command (admin) | **clears them** (until the next refresh, at most 20 s) |
| Death | vanilla | clears them; respawn rules in 8 |

**Removes single effects:** Strong Hot Cocoa (Farmer's Respite) and Milk Bottle (Neapolitan) remove effects one at a
time from the player's list; whether they can pick a beneficial one is to read. The same removal event covers them:
proposed, keep slot effects.

Owner decision Q3: approve the "Proposed" column. Then the rule is one sentence: **only death and `/effect clear`
remove diet effects; everything else skips them.**

### 9.4 When a potion of the same effect is active

Vanilla keeps **one instance per effect**. When a second one arrives:

| Arriving instance | Result |
|---|---|
| higher level | it goes on top; the old one is kept underneath ("hidden") if it would outlast it |
| same level, longer | the two merge: one instance with the longer time |
| lower level, longer | it is stored underneath the current one |
| otherwise | ignored |

When the top instance runs out, the one underneath takes over with the time it has left. Our 20 s refresh is just
another "arriving instance", so the cases for us are:

| Case | Example | What happens | Rule we need |
|---|---|---|---|
| A. Potion stronger | Speed II potion while slot has Speed I | potion on top and shown; ours waits underneath and returns, hidden, when the potion ends | if the slot switches off while the potion is on top, strip ours from underneath, or it resurfaces for up to 40 s |
| B. Potion same level | Speed I potion (8 min) while slot has Speed I | merged into one instance with the potion's time; ours can no longer be told apart | on switch-off, leave it if more than 40 s is left (it is the potion's time); remove it if 40 s or less is left. Worst case a same-level potion loses its last 40 s |
| C. Potion weaker | Speed I potion while slot has Speed II | ours on top; the potion waits underneath | on switch-off, promote the potion back to the top instead of removing the effect, or the potion is lost too |
| D. Showing it | any | the HUD shows whatever is on top | hide only when the top instance is ours; a merged one (B) is shown |

A and C need access to vanilla's hidden-instance field (an accessor mixin). "Ours" means: no cure items, at most 40 s
left, the slot's level.

### 9.5 Display

- When the inventory widget is enabled, slot and flat effects are hidden from the HUD and from the inventory's effect
  list (client mixin filtering our instances); the widget shows them instead. When the widget is disabled in config,
  they show as normal effects.
- A flat bonus overridden by a slot with the same effect at a higher level: **greyed on the effect screen only**; the
  inventory widget does not show it at all. The slot's level applies.

## 10. Messages

On the jukebox "Now playing" line above the hotbar (overlay message, fixed colour, not rainbow):

`You {gained/lost} a bonus due to your diet: {name}`

| Colour | When |
|---|---|
| green | gained from a flat bonus, health, toughness or knockback tier |
| blue | gained from a slot (re)activating |
| red | lost, any kind |

Hearts, knockback resistance and toughness use the same line (`{name}` = "1 Heart", "Knockback Resistance +1", ...).
Several changes in a short time: each message shows 3 s, the rest wait in a queue; none are dropped. Hearts,
knockback resistance and toughness changes of the same kind and direction that are still waiting merge into one line
with the sum ("3 Hearts", "Armor Toughness +4"). The line is
shared with action-bar messages from other mods, which can overwrite ours (Q10).

## 11. Screens and widgets (boilerplate first, then tuned with the owner)

### 11.1 Inventory slot widget

- A bar (rectangle) attached to the survival inventory screen only (not the creative inventory, not other screens).
- Shows only what is active: flat bonuses first (own style), then active slots (effect icon, level). Inactive slots
  and overridden flat bonuses are not shown here (they appear greyed on the effect screen). Hover gives the tooltip
  (effect, level, source, threshold).
- Click: opens the effect screen.
- Lock/unlock with one key (toggle, like TrashSlot's Shift+T). Unlocked: drag it anywhere, and while dragging it
  snaps to the inventory edges horizontally and vertically at the same time, with TrashSlot's snappy feel. Locked: it
  stays put and clicks open the screen. Position and lock state saved per client (client config).
- Hidden while the Curios panel or the recipe book is open.
- JEI moves out of its way (JEI extra-area handler).
- The vanilla effect list may move to make room for a bar on the side (to evaluate in the boilerplate).
- Can be turned off in config (then effects show as normal, 9).

### 11.2 Effect screen

- Opened from: the widget, a keybind, the Diet Log button.
- Flat bonuses at the top (read only, own style; greyed when a slot overrides them), then slots in order; inactive
  slots greyed with their stored choice.
- Choosing an effect for a slot: dropdown/picker grid of unlocked effects (drag from the grid onto a slot as a second
  option to evaluate in the boilerplate); level dropdown per slot (free).
- Paid actions open the confirm panel (5).

### 11.3 Keybinds (client, rebindable)

| Action | Default |
|---|---|
| Open the effect screen | Q6 |
| Lock / unlock the inventory widget | Q6 (TrashSlot already uses T to toggle and Shift+T to lock) |

### 11.4 Other screens

Diet Log button, milestone page and progress line (6.3); Liquid Diet Log book screen (7).

### 11.5 GUI toolkit

Vanilla `Screen` / `AbstractWidget` with our own dropdown, picker grid and drag code (no new library jar). Apple
Pie's own UI kit (`UIBox`, `UILabel`, `UIStack`, `UIItemStack`, `UIImage`) for the Diet Log page changes so they keep
the book style. Cloth Config (in the pack) optional for editing settings. FTB Library's GUI is the fallback if the
hand-built widgets grow too large (check its licence first). LDLib is not worth the extra jar.

## 12. Open questions

Resolved 2026-11-11: dropping a middle slot (5), Liquid Diet scope = alcohol only (7), milk never clears diet
effects (9.3), inventory shows no greyed slots or overridden flat bonuses (9.5, 11.1), summed attribute messages
(10), lock key allows drag and snap together (11.1).

1. Does filling a slot or changing its level (player actions) show a message? If yes, blue?
2. Flat bonus and slot with the same effect at the **same** level: which one greys?
3. Approve the "Proposed" column in 9.3 (only death and `/effect clear` remove diet effects).
4. Fleet-Footed: fix with a mixin and allow, or leave out?
5. The Lava Walker change goes into the Vinery port in Creative Brewing (a player-visible behaviour change there).
   OK?
6. Default keys for "open effect screen" and "lock widget".
7. How is the Liquid Diet Log obtained (recipe)?
8. Are flat effects hidden from the HUD too when the widget is on (9.5 assumes yes), or shown normally?
9. Our messages share the action-bar line with other mods' messages (Create, sleep, ...). Is it fine if one gets
   overwritten early, or should ours wait until the line is free?
10. Liquid Diet Log memory of 256: the last 256 drinks in order (duplicates kept), or 256 unique drinks like Apple
    Pie?
11. On death, does Apple Pie's "foods eaten" counter (the gate of 5) reset too?
12. 9.4 case B: a same-level potion merged with a slot effect can lose up to its last 40 s when the slot switches
    off. Acceptable?

## 13. Tests (draft, prefix RP; written out when built)

RP-01 boot with Apple Pie, no errors; RP-02 rung gained on eat with green or blue message; RP-03 loss only at the
5-minute check, red message; RP-04 fill free; RP-05 level change free; RP-06 change effect greys 5 foods, preview
matches; RP-07 drop greys until the slot is lost; RP-08 grace hold; RP-09 greyed food eaten again counts; RP-10 death
ages the window, rungs off on respawn, unlocks and choices kept; RP-11 widget snap, lock, per-client position, hidden
with Curios and recipe book, JEI moves; RP-12 effects hidden from HUD and inventory with the widget on, shown with it
off; RP-13 potion of the same effect is not stripped; RP-14 message queue 3 s each, none lost; RP-15 Diet Log
progress line and greyed history; RP-16 drinks absent from the Diet Log, present in the Liquid Diet Log, no effect;
RP-17 dedicated server with a client.
