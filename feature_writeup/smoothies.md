# Smoothies

Fruit smoothies and brain freeze. Built 2026-11-23 (plan_ready_now_1123.md section 9); status lives in the TODO rows.
Rulings in [plan_smoothies.md](plan_smoothies.md), fruit facts in [fruit.md](fruit.md). Replaces Neapolitan's Strawberry Banana
Smoothie (removed 2026-10-29, `baking/sweets #5`).

## 0. At a glance

<!-- generated:status_index begin open_work sha1=c92a1d961ec6 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 3 open TODO rows (Planned 1, Testing 2), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `sweets #10` | Planned | C | Smoothie sprites: set up a mass swap from the strawberry banana placeholder to per-flavor sprites |
| `sweets #8` | Testing | SQ-SMO-01 SQ-SMO-02 SQ-SMO-03 SQ-SMO-04 CQ-SMO-05 | Smoothie line: 16 fruit smoothies (2 + 1 fruit or slices + bottle), always edible, honey drink sound |
| `sweets #9` | Testing | SQ-SMO-06 SQ-SMO-07 CQ-SMO-08 | Brain freeze: cold foods eaten close together can freeze the player for 5 to 20 s with a blue action-bar message |
<!-- generated:status_index end open_work -->

- 16 smoothies (section 2): 2 x first fruit + 1 x second fruit + glass bottle, shapeless.
- Effects: first fruit at its Fruits Delight base length, second at half, amplifiers kept.
- Brain freeze (section 3): cold foods eaten close together can freeze the player for 5 to 20 s.

## 1. Overview

- Every smoothie is always edible, plays the honey bottle drinking sound and returns the glass bottle; 32 per stack.
- Every smoothie is a `drink` food type (`#creativedelight:food_type/drink`), so the Diet Log leaves it out
  (plan_ready_now_1123.md R5), and joins the `drink/juice` family at the next `/dumpfood` run of `food_families.py`.
- Fluids: one per smoothie but Mixed Berry (250 mB a bottle): Create mixing of the same fruits makes one bottle's
  worth, the Spout fills and the Item Drain empties bottles, and a Supplementaries jar holds them (soft fluids in the
  Creative Delight jar). Not canned (drinks have no cans) and not compostable (no compost rule takes drinks).
- The Neapolitan Strawberry Banana Smoothie stays removed (`baking/sweets #5`); its leftover jar fluid file is gone.
- A fruit with a slice item goes in as slices (lemon, orange, pineapple, melon, cantaloupe); others whole.
- One-fruit smoothies (Glowberry, Chorus) take 3 of the fruit.
- Sprite: Neapolitan's strawberry banana smoothie for every flavor until the mass swap (`sweets #10`).
- Hunger and saturation: 1 and 0.2 (the pack's other drinks) until the food tuning pass sets the `drink/juice` family
  rule ([food_families.md](food_families.md)).

## 2. Smoothies

Recipe = shapeless in the crafting grid, plus a glass bottle. Base lengths are the fruit's Fruits Delight jam (`_jelly`
bottle) and jello lengths ([fruit.md](fruit.md) 3.1); first fruit = base, second fruit = half base (owner, 2026-11-18).

| # | Smoothie | 2 x | 1 x | First fruit (base) | Second fruit (half) |
|---|---|---|---|---|---|
| 1 | Mixed Berry | 2 red berries | 1 blue or purple berry | per berry, section 2.1 | |
| 2 | Cran Apple | Cranberry | Apple | Shrinking 80 s | Absorption 10 s |
| 3 | Orange Mango | Orange Slice | Mango | Regeneration 20 s + Recovering 20 s | Strength III 10 s + Rage Aura 10 s |
| 4 | Kiwi Strawberry | Kiwi | Strawberries | Cycling 4 s | none |
| 5 | Glowberry | Glow Berries x3 | - | Glowing 80 s | |
| 6 | Chorus | Chorus Fruit x3 | - | Teleport (random, like eating chorus fruit) | |
| 7 | Peach Mango | Peach | Mango | Heal Aura 10 s | Strength III 10 s + Rage Aura 10 s |
| 8 | Mango Pineapple | Mango | Pineapple Slice | Strength III 20 s + Rage Aura 20 s | Speed 10 s + Sweetening 10 s |
| 9 | Melon | Melon Slice | Cantaloupe Slice | Regeneration 40 s | Health Boost 10 s |
| 10 | Cherry Lemon | Cherry | Lemon Slice | none | Haste 10 s + Refreshing 10 s |
| 11 | Blueberry Lemon | Blueberry | Lemon Slice | Night Vision 20 s + Brightening 20 s | Haste 10 s + Refreshing 10 s |
| 12 | Pear | Pear | Hawberry | Appetizing 5 min + Lozenge 30 s (owner) | |
| 13 | Cherry | Cherry | Lychee | none | Strength III 5 s |
| 14 | Raspberry Peach | Raspberry | Peach | none | Heal Aura 5 s |
| 15 | Orange Peach | Orange Slice | Peach | Regeneration 20 s + Recovering 20 s | Heal Aura 5 s |
| 16 | Hawberry | Hawberry | White Strawberries | Appetizing 20 s | none |

Ids: `creativedelight:<name>_smoothie` (`mixed_berry`, `cran_apple`, `orange_mango`, `kiwi_strawberry`, `glowberry`,
`chorus`, `peach_mango`, `mango_pineapple`, `melon`, `cherry_lemon`, `blueberry_lemon`, `pear`, `cherry`,
`raspberry_peach`, `orange_peach`, `hawberry`). A smoothie whose fruit mod is absent has no recipe; an effect whose mod
is absent is left out. Cherry takes `#forge:fruits/cherry`, which now holds Vinery's cherry (it was empty).

Every smoothie: always edible, honey drink sound, returns the glass bottle, counts toward brain freeze, sprite =
the strawberry banana smoothie placeholder (owner, 2026-11-18; per-flavor sprites: TODO `sweets #10`). Cherry Lemon, Cherry and Raspberry Peach get nothing from their
first fruit (cherry and raspberry have no effect in the pack).

### 2.1 Mixed Berry effects

2 red + 1 blue or purple, any berries of the slot's color ([fruit.md](fruit.md) section 4). Each berry adds its effect at half base; a berry
used twice adds up to the full base (owner, 2026-11-18). The item stores the result and shows it in its tooltip, so different
mixes do not stack together. Slot tags: `#creativedelight:berries/red`, `/blue`, `/purple` and `/blue_or_purple` (the
third slot). The craft writes `SmoothieEffects` ("effect id|ticks|amplifier" entries joined with `;`, empty for a mix
that adds nothing, shown as "No effects"); a Mixed Berry from the creative menu has none and gives nothing. Mixed
Berry has no fluid: a fluid could not keep the effects.

| Slot | Berry | Effect per berry (half base) | Used twice |
|---|---|---|---|
| red | Cranberry | Shrinking 40 s | Shrinking 80 s |
| red | Hawberry | Appetizing 10 s | Appetizing 20 s |
| red | Raspberry, Strawberries, Sweet Berries, Cherry | none | none |
| blue | Blueberry | Night Vision 10 s + Brightening 10 s | - |
| purple | Bayberry | Leaf Piercing 10 s | - |
| purple | Mulberry | none | - |

Every possible effect set (red pair x third berry): any of {none, Shrinking 40 s, Shrinking 80 s, Appetizing 10 s,
Appetizing 20 s, Shrinking 40 s + Appetizing 10 s} from the red pair, plus one of {Night Vision 10 s + Brightening 10 s,
Leaf Piercing 10 s, none} from the third berry: 18 effect sets, from no effect at all (e.g. 2 raspberry + mulberry) to
Shrinking 40 s + Appetizing 10 s + Night Vision 10 s + Brightening 10 s (cranberry + hawberry + blueberry).

## 3. Brain freeze

Owner (2026-11-17, 2026-11-18):
- Every cold food (tag below) eaten while another was eaten in the last minute rolls once: the 1st roll 20%, +20% per
  further roll in the window (20, 40, 60, 80, 100%).
- On a hit: the Brain Freeze effect (`creativecommon:brain_freeze`, harmful) for the length below and a blue action-bar
  message ("Brain freeze!"). While the effect lasts the frozen ticks are held at full, so the vanilla powdered snow
  overlay draws and the vanilla freeze damage (half a heart every 2 s) and slowdown apply; when it ends the frost thaws
  over about 3.5 s. Vanilla rules stay: leather armor and freeze-immune mobs take no freeze damage, milk clears the
  effect (kept, owner 2026-11-23). The effect is what makes the HUD icon (Immersive Weathering's icicle, owner pick
  2026-11-23) and the JEED page possible (plan_ready_now_1123.md section 9).
- Length by the number of rolls up to and including the hit: 1 -> 5 s, 2 -> 8 s, 3 -> 11 s, 4 -> 14 s, 5 or more ->
  20 s. Configurable in `creativecommon-common.toml` `[brain_freeze]`: `enabled`, `windowSeconds` (60), `chanceStep`
  (0.2), `lengths` ([5, 8, 11, 14, 20]; the last entry covers every later roll); read live (backup in `configs_edited/`
  when hand-edited).
- The first cold food after a gap longer than the window starts the count and does not roll; the window runs from
  the previous cold food, so it slides. Counts are per player, on the server, and dropped at logout.
- A hit resets the count (owner, 2026-11-18). Fire Resistance does not block it (vanilla freeze damage ignores it).

Home: Creative Common (`effect/BrainFreeze.java`, the roll in `effect/BrainFreezeRoll.java`; effects and the cold-food tag
are Common's, plan_ready_now_1123.md section 9; was planned for Creative Delight). The `#creativecommon:brain_freeze`
item tag lists what counts; Creative Delight fills it (`smoothie_tags.js`, through `global.tags`), so families join by
tag, not code.

What counts (owner 2026-11-18: all proposals plus ice cubes, which are edible):

| Family | Items |
|---|---|
| Smoothies | all 16 (section 2) |
| Ice cream `frozen/ice_cream` | 11 (Neapolitan 7, Delightful 2, Fungi Delight truffle, ...) |
| Popsicle `frozen/popsicle` | 8 (melon, cantaloupe, kiwi, chocolate, chorus, corn, ...) |
| Shaved ice `frozen/shaved_ice` | 1 (Cantaloupe Shaved Ice) |
| Milkshakes (`drink/milk`, cold ones only; hot chocolate excluded) | 10 (Neapolitan 6, Delightful 2, Fruits Delight mango, ...) |
| Iced Berry Matcha Latte | `delightful:berry_matcha_latte` |
| Ice Cubes | `neapolitan:ice_cubes` (0 hunger, always edible) |

Not counted: Frozen Flesh (`hostiledelight:frozen_flesh`, a mob drop) and Ice Sickle (`immersive_weathering:ice_sickle`).
The tag nests `#creativedelight:food_family/frozen` (ice cream, popsicles, shaved ice), so new frozen foods join with
their family; the milkshakes, the latte and the ice cubes are listed by id; the smoothies through `#creativedelight:smoothies`.

## 4. Limits and decisions

- Rage Aura stays on the three mango smoothies, as on every Fruits Delight mango food (owner, 2026-11-18); it is marked to
  move to another food later (ghost pepper candidate, [fruit.md](fruit.md) section 5).
- Pear smoothie is the "eat at any hunger" drink: Appetizing 5 min (Fruits Delight's own foods give 10 to 20 s).
- Rejected: capping Strength III to I and fixed 60 s / 30 s lengths (2026-11-17 draft), replaced by the Fruits Delight
  ratios. Rejected: a fixed Mixed Berry effect set.
- All rulings with dates: [plan_smoothies.md](plan_smoothies.md) section 1.

## 5. File map

| What | Where |
|---|---|
| Table (`global.smoothies`), items, fluids, drink callbacks (bottle, Mixed Berry effects, Chorus teleport) | CD `kubejs_startup/creativedelight/smoothies.js` |
| Grid recipes, Mixed Berry NBT, mixing, Spout and Item Drain | `kubejs/data/creativedelight/kubejs/creativedelight/smoothies.js` (in the CD jar) |
| Tags: smoothies, honey sound, berry colors, `forge:fruits/cherry`, `#creativecommon:brain_freeze` members, fluid tags | `kubejs/data/creativedelight/kubejs/creativedelight/smoothie_tags.js` |
| Mixed Berry tooltip | `kubejs/assets/creativedelight/kubejs/creativedelight/smoothie_tooltip.js` |
| Honey drink sound | CD `mixin/minecraft/ItemStackHoneyDrinkMixin.java`, `food/HoneyDrinks.java` |
| Jar soft fluids | CD `data/creativedelight/moonlight/soft_fluids/*_smoothie.json` (hand-written; `tools/gen_soup_soft_fluids.py` keeps them) |
| Sprites | CD `assets/creativedelight/textures/item/<id>.png`, copies of Neapolitan's strawberry banana smoothie (sources.csv) |
| Ids | Common `_lib/item_dict.js`, `fluid_dict.js`, `food_dict.js` (`f.drink_<name>_smoothie`), `effect_dict.js` |
| Food type `drink` | `kubejs/generation_scripts/food/food_types.json` -> `food_catalog.py --members` |
| Brain freeze | Common `effect/BrainFreeze.java`, `effect/BrainFreezeRoll.java`, `CommonConfig.BrainFreezeConfig`, startup `effects/brain_freeze.js`, lang `effect.creativecommon.brain_freeze(.description)`, `message.creativecommon.brain_freeze`, icon `textures/mob_effect/brain_freeze.png` (vanilla Slowness copy, art_needed_2026-11-22.csv), empty tag `data/creativecommon/tags/items/brain_freeze.json` |

## 6. Offline checks

- `tools/test_brain_freeze.sh` (in `check_all.py`): `BrainFreezeRoll` against `tools/brain_freeze_test/BrainFreezeRollTest.java`:
  no roll on the first food, misses and hits on the 20 % ladder, the length table (5, 8, 11, 14, 20, then 20), the reset
  on a hit, the window edge and slide, a custom config, chance step 0.
- `./gradlew :creativeCommon:build :creativeDelight:build --offline` (2026-11-23).

## 7. Tests

Prefix `SMO-`. Queue rows: `feature_writeup/testing/rows/SMO.qb` (SQ-SMO-01 to -04, -06, -07; client CQ-SMO-05, -08).

| ID | Check |
|---|---|
| SMO-01 | 16 smoothies at 32 per stack; `#creativedelight:smoothies` and `#creativedelight:honey_drink_sound` 16 each; in `food_type/drink` and the Diet Log exclusion; `#forge:fruits/cherry` holds Vinery's cherry; 16 grid, 15 mixing, 15 filling recipes (SQ-SMO-01) |
| SMO-02 | Cran Apple drinks at full hunger: Shrinking 80 s, Absorption 10 s, the bottle comes back (SQ-SMO-02; PASS r1124nw 2026-11-24) |
| SMO-03 | Pear: Appetizing 5 min, Lozenge 30 s; Mango Pineapple: Strength III 20 s, Rage Aura 20 s, Speed and Sweetening 10 s (SQ-SMO-03) |
| SMO-04 | Chorus Smoothie teleports like chorus fruit (SQ-SMO-04) |
| SMO-05 | Mixed Berry: the tooltip lists the berries' effects (2 cranberry + blueberry: Shrinking 1:20, Night Vision and Brightening 0:10; 2 raspberry + mulberry: No effects), different mixes do not stack, drinking gives them; honey sound; placeholder sprite (CQ-SMO-05) |
| SMO-06 | `#creativecommon:brain_freeze` holds smoothies, ice cream, popsicles, the cold milkshakes, the latte and ice cubes, not Frozen Flesh; the effect exists (SQ-SMO-06) |
| SMO-07 | Two cold foods within a minute with chance step 1.0: Brain Freeze 5 s, frozen ticks at full, then thawing (SQ-SMO-07) |
| SMO-08 | In a client: frost overlay, freeze damage, blue "Brain freeze!" above the hotbar, HUD icon, JEED page (CQ-SMO-08) |

## 8. History

- 2026-11-18: written from plan_smoothies.md (smoothie table, Mixed Berry, brain freeze); Rage Aura kept, berries used
  twice add up, a brain freeze resets the count (owner).
- 2026-11-23: built (plan_ready_now_1123.md P8): the 16 smoothies, fluids, jars, tags and recipes in Creative Delight;
  brain freeze moved to Creative Common as an effect with a `[brain_freeze]` config block; tests SMO-01 to -08.
