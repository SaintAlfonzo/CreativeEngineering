# Cans: multi-serving cans, generated per-type cans, the dyeable canned golem

This is the hub for the pack's can systems. The detail lives in two writeups that already exist: servings in
[food.md](food.md) 3.6, and which food gets which can in [can_parity.md](can_parity.md). This file gives the whole
picture, the current numbers, and the canned golem dye in full.

## 0. At a glance

| Part | Status | Full detail |
|---|---|---|
| Multi-serving cans: servings as NBT, a bar and "Servings: x / y", eat one serving at a time, return roll (empty 70 %, crushed 25 %, nothing 5 %), cans in NCH recipes one serving per craft | DEPLOYED (built 2026-09-28; shipped since 1.0.3), FO-14 to FO-22 pending | [food.md](food.md) 3.6 |
| Servings per can: 247 entries in `can_servings.json` → `#creativeengineering:cans/servings/<n>` | DEPLOYED, grows with every can | [food.md](food.md) 3.6 |
| 26 hand-written cans (raw veggie, fruit, meat) | DEPLOYED | [can_parity.md](can_parity.md), [cans.js](../development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/cans.js) |
| 202 generated cans (`gen_can_types.py` → `can_types.json`; 20 of them re-valued Canned Goods cans) | DEPLOYED with 1.0.6 (can parity), untested in game | [can_parity.md](can_parity.md) |
| Canned golem dyeable in 16 colours | DEPLOYED with 1.0.4, R4-07 pending | here, 2.3 |

- **Pending:** the D-D build of 2026-10-20 (sliced-input rule, pumpkin to fruit, Fish Meal, shulker meal, cantaloupe
  slice values) is built but not shipped: 1.0.9 deployed canning.js / can_types.json / cans.js from before it. CN-01 to CN-06 are new; the rest of the tests stay in food.md (FO-),
  can_parity.md (CP-) and plan_1.0.4.md (R4-07). Findings F1 to F4 (section 4); the open decisions are can
  parity's (`Want = ?` cells, values) and food tuning's D1 to D6.
- **Knobs:** `[canning]` in `config/creativeengineering-common.toml` (`enabled`, `emptyCanChance` 0.7,
  `crushedCanChance` 0.25, `emptyCanItem`, `crushedCanItem`; live); `can_servings.json` (`/reload`);
  `can_types.json` via `python3 tools/gen_can_types.py` (new items: restart).

## 1. Overview

Canned Goods (with Canned Goods' golem and drinks) makes cans: eat one, get an empty can back. Its values were sized
for 2 foods, its recipes ignored the pack's food types, and only some foods could be canned. The pack:

1. lets one can hold several servings (each the full food value) and gives back an empty can, a crushed can or nothing
   at the end;
2. generates a can for every food type section from the food catalog, so cans exist for raw, cooked and prepared food,
   with values worked out from what's inside;
3. makes the canned golem dyeable.

## 2. Reference

### 2.1 Servings (summary of food.md 3.6)

- **Count:** `can_servings.json` maps item_dict keys to servings; `cantags.js` turns it into
  `#creativeengineering:cans/servings/<n>`. Current spread (2026-10-21): 4 servings 67 cans, 10 servings 50 (golden), 3 servings 46,
  8 servings 35, 2 servings 25, 5 servings 12, 6 servings 9, 1 serving 3 (sodas: return roll only).
- **State:** the `CanServings` NBT int on an opened can only; full cans have no NBT and stack to 64. Opened cans show a
  durability-style bar and every multi-serving can shows `Servings: x / y`.
- **Eating:** `ItemStackCanServingsMixin` on `ItemStack#finishUsingItem` runs the can's own code on a copy (food,
  effects, Canned Goods' procedures), then takes a serving from the can in hand if it's open, else from the most-eaten
  open can of the same kind in the inventory, else opens one from the stack. Creative takes nothing.
- **Recipes:** `R.shaped` / `R.shapeless` with the `cans` argument attach the KubeJS ingredient action
  `creativeengineering:can_serving` (`compat/KubeJSCanAction`): one serving per craft, return roll on the last.

### 2.2 Generated cans (summary of can_parity.md)

`tools/gen_can_types.py` reads the food catalog and writes `kubejs/generation_scripts/food/can_types.json`; `cans.js`
registers the items, `canning.js` (`canSection()`) the recipes. 202 cans in 16 sections:

| Section | Cans | Section | Cans |
|---|---|---|---|
| golden | 50 | bread | 8 |
| soup | 27 | salad | 8 |
| raw fruit | 17 | pie | 9 |
| cooked veggie | 15 | feast | 9 |
| cooked meat | 15 | hazard | 4 |
| raw fish | 12 | special | 2 |
| cookies | 12 | drink | 2 |
| meal | 11 | raw meat | 1 |

A meal is the food it holds times a multiplier, effects copied with scaled durations; a sliced form shares the whole
item's slots and count, and no recipe makes a partly full can ([can_parity.md](can_parity.md) 4.2). Sprites per section or per can
(`textureswapper/canned/newcan`). Values for meal, pie, feast and drink cans are placeholders until the food tuning
system is built ([food.md](food.md) 3.5).

### 2.3 Dyeable canned golem

- **Use:** right-click a Canned Goods canned golem with any dye (vanilla or `#forge:dyes/<colour>`) → it takes that
  colour; the dye is used up unless in creative. Red is the default and keeps the mod's own texture.
- **State:** synced entity data, saved as NBT `Color` (DyeColor id), so it survives reloads and shows to every player.
- **Textures:** the other 15 colours in `kubejs/assets/creativeengineering/textures/entity/canned_golem/<colour>.png`.
- **Code:** `CannedGolemColorMixin` adds the data, the interaction and the save fields straight onto Canned Goods'
  `CannedGolemEntity` (it overrides none of those methods); `CannedGolemRendererMixin` swaps the texture;
  `compat/DyeableCannedGolem` holds the shared accessor and texture lookup.
- **Getting a golem:** Canned Goods' own recipe, which needs crushed cans, which only exist through the return roll
  (2.1).

## 3. What the pack changes

See food.md 3.3 and 3.6 (Canned Goods recipes removed, empty-can recipes, servings) and can_parity.md (generation,
removals, sprites). Code: patcherMod `food/CanServings`, `food/CanServingsTooltip`, `mixin/ItemStackCanServingsMixin`,
`compat/KubeJSCanAction`; KubeJS `items/cans.js`, `cooking/canning.js`, `tags/cantags.js`, NCH's `cans` argument;
`tools/gen_can_types.py`.

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1.** FIXED 2026-10-19 (3.6 now gives 247 cans, 1 to 10 servings, and the canning recipes). **food.md 3.6 was out of date on numbers.** It says "the 34 food cans, canned_food and the Hot Hot Hotpot hold 2"
  and "No food canning recipe exists yet"; today 247 cans have serving counts from 1 to 10 and canning recipes exist
  for every section. The mechanics described there are current.
- **F2. Values are placeholders for four sections** (meal, pie, feast, drink) and hand-written raw cans don't carry
  effects ([TODO](todo/TODO.md) `cans/canning #1` and `#4`, can_parity.md).
- **F3. Golden cans hold 10 servings each** (50 cans): each serving is a quarter of the golden food's value
  (SECTIONS `golden` mult 0.25) with its effects at 0.25x duration (corrected 2026-10-21: this said "a full golden
  food's value"). Worth a look when tuning runs.
- **F4. Servings only work in NCH crafting recipes.** Create mixing, mechanical crafting and other mods' recipes use up
  the whole can (food.md 3.6 limits).

### Decisions (2026-10-20)

Settled in [can_parity.md](can_parity.md) 4; none built yet.

- **D-A. Own mod.** Canning moves to a new mod, Creative Canning, built like the alcohol mod and Creative Labels. The
  26 hand-written cans are rebuilt there with their effects passed through (closes the effects half of F2).
- **D-B. Golden cans.** 10 servings, 0.25x nutrition, 0.333x effect length, still following golden food effect changes
  (revises F3). 10 servings and 0.25x nutrition are what is built; only the effect length (0.25x -> 0.333x) is left. Later golden effect changes need a recheck of these cans.
- **D-C. Crafting.** Cans are used only in manual shaped or shapeless crafts for now (F4 stays a limit by design).
- **D-D. Recipes.** BUILT 2026-10-20 (not run in game): every can whose whole item and slice are both edible is defined
  by the whole food, and the slice takes the same slots at the same count (slicing first is the 2x+ boost); no recipe
  makes a partly full can. Pumpkin is a fruit can (3 servings); pumpkin, melon, cantaloupe, hamimelon and pineapple are
  slice-only (the whole can't be eaten); the Cantaloupe Slice copies the Pumpkin Slice's food values. Still to build:
  the Neapolitan banana taking over the Alex's Mobs banana's behaviour, and an opened filling can no longer making a
  pie. Detail in [can_parity.md](can_parity.md) 4.1, 4.2.
- **D-E. Removed cans.** No remap for cans removed after shipping.

Suggested build order: recipe changes in D-D (KubeJS only), then the golden numbers, then the Creative Canning split.

**Priorities (scratchpad 2026-10-21):** can textures ([TODO](todo/TODO.md) `cans/canning #13`: 48 of the 235 built cans
have their own sprite) and can crafts (`cans/canning #14`: recipes for the remaining wanted cans). Also wanted: one
canned cake with 4 servings (`cans/canning #15`, [can_parity.md](can_parity.md) 2.23).

### Known limits

Listed in food.md 3.6: a grid slot with a stack of full cans opens a new one per craft; auto-feeders that call the item
directly eat a whole can; a dedicated-server client guesses nothing for a last-serving craft until the server answers.

### Planned from the scratchpad (2026-10-24)

- [TODO](todo/TODO.md) `cans/canning #17`: every feast is cannable, as the nine existing feast cans are (see `dishes/meals #5`).
  Not canned yet by the generator: the pizzas (Brewin', Vintage cheese and meat, vegan), Veggies Delight's stuffed bell peppers
  feast, quiche and Oceanic Delight's plated blocks (unverified which count as feasts). Plan:
  [cans_canning](briefs/cans_canning.md) `canning #17`. Open: none beyond keeping the section's 6-serving rule. Tests CN-07 and
  CN-08 are proposed there and not added to section 7 until built.

## 5. File map

- patcherMod: [food/CanServings.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/food/CanServings.java), [food/CanServingsTooltip.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/food/CanServingsTooltip.java), `mixin/ItemStackCanServingsMixin`, `compat/KubeJSCanAction`, `compat/DyeableCannedGolem`, `mixin/CannedGolemColorMixin`, `mixin/CannedGolemRendererMixin`; `CEConfig.Canning`.
- KubeJS: [items/cans.js](../development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/cans.js), [cooking/canning.js](../kubejs/data/creative_delight/kubejs/creativeengineering/canning.js), [tags/cantags.js](../kubejs/data/creative_delight/kubejs/creativeengineering/can_tags.js), `generation_scripts/food/{can_servings.json, can_types.json}`, `assets/creativeengineering/textures/entity/canned_golem/`.
- Tools: [tools/gen_can_types.py](../tools/gen_can_types.py).

## 6. Offline checks

`python3 tools/gen_can_types.py --check` (prints what would change, writes nothing) and
`python3 tools/food_catalog.py --check` (exit 1 on problems).

## 7. In-game test suite

Servings: FO-14 to FO-22 ([food.md](food.md)). Can recipes and removals: CP-01 to CP-14 ([can_parity.md](can_parity.md)).
Golem: R4-07 ([plan_1.0.4.md](plan_1.0.4.md)).

- [ ] **CN-01 Generated can:** JEI on a generated soup can (e.g. canned beef stew) → its canning recipe; eat one serving
  → the stew's food value, `Servings: x / y` drops by one.
- [ ] **CN-02 Golden can:** eat a golden can → golden-food effects each serving; 10 servings.
- [ ] **CN-03 Crushed can source:** empty 20 cans → about 5 crushed cans; craft a canned golem from them.
- [ ] **CN-04 Golem colours:** dye a golem each of the 16 colours in turn → each texture shows; relog → colour kept.
- [ ] **CN-05 Golem dye used:** survival, dye a golem → one dye gone; creative → none gone.
- [ ] **CN-06 Golem multiplayer:** another player sees the dyed colour.

## 8. History

- 2026-09-28: multi-serving cans ([food.md](food.md) 8).
- 2026-09-29 to 10-04: hand-written raw cans, then the generated sections (113 cans) and 13 Canned Goods cans re-valued.
- 2026-10-02: canned golem dye (1.0.4).
- 2026-10-13: can parity: 11 raw cans removed, every remaining row generated (202 generated cans) ([can_parity.md](can_parity.md)).
- 2026-10-18: this hub; findings F1 to F4; tests CN-01 to CN-06. No code changed.
- 2026-10-19: F1 fixed: food.md 3.6 numbers brought up to date.
- 2026-10-20: decisions D-A to D-E recorded from can parity section 4. No code changed.
- 2026-10-20: D-D sliced-input alignment built (gen_can_types.py, canning.js, food_values.js); see can_parity.md 8.
- 2026-10-20: raw fish cans use a Fish Meal (4, 2) instead of the Meat Meal ([can_parity.md](can_parity.md) 4.2).
- 2026-10-20: pufferfish on the Fish Meal, shulker on its own (2, 1) x 4 meal; value-vs-goal per can in [can_parity.md](can_parity.md) 4.3.
- 2026-10-24: planned `cans/canning #17` (all feasts cannable) added; no code changed.
