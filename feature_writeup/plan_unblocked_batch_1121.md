# Plan: unblocked batch 2026-11-21

<!-- generated:status_index begin open_work sha1=fa6595ea9047 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 21 open TODO rows (Planned 3, Pinned 1, In Progress 6, Testing 11), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `baking/baked-goods #30` | Planned | S C P | Baking dish bakeware |
| `baking/baked-goods #40` | Planned | S C | Fruit scones: a new baked family, one item per fruit |
| `dairy-fats-and-batter #3` | Planned | S C | Whale oil lamp fuel: oil tank, burn rate, refill, config |
| `placeable-food #1` | Pinned | none | List other mods' non-food plates and coasters for review and possible removal |
| `baking/baked-goods #2` | In Progress | S C P T | Muffins as a decent food source |
| `baking/baked-goods #23` | In Progress | S C P | Oatmeal cookies from oat dough |
| `baking/baked-goods #36` | In Progress | S C P | Dessert cookware and recipe treatments |
| `dishes/ingredients #7` | In Progress | S C P | Batter uses |
| `dishes/ingredients #8` | In Progress | S C P | Produce fixes |
| `dishes/ingredients #9` | In Progress | S C P T | Cheese |
| `baking/baked-goods #18` | Testing | S C P | Brownie and cupcake baking types |
| `baking/baked-goods #22` | Testing | SQ-FF-22 OV-39 | Chorus cookie from the uncanny cookie |
| `baking/baked-goods #35` | Testing | SQ-FF-25 OV-42 | Investigate brownie tray and adzuki bun family |
| `blocks/flower-pots #1` | Testing | S C P | Flower pots placeable like candles |
| `dairy-fats-and-batter #1` | Testing | SQ-FO-17 CQ-FO-63 | Whale oil lamp |
| `dishes/ingredients #17` | Testing | S C P | Tofu from mushrooms |
| `dishes/ingredients #21` | Testing | S C P | Potato and sweet potato compatibility |
| `dishes/ingredients #27` | Testing | S C P | Potato dough |
| `dishes/ingredients #35` | Testing | S P | Nut milk usable in most milk recipes |
| `food/placeable #6` | Testing | S C P | Coaster and small plate compatibility |
| `textures/food #10` | Testing | C | Palette swap overhaul before the swapper goes in a GUI |
<!-- generated:status_index end open_work -->

Source: owner request (chat 2026-11-21) to build the feature families with no open decisions together with the
one-decision families, after the decisions were asked. Selection method: open TODO rows (Planned, In Progress, Bug)
whose Decisions field is `none`, blank or "Decided"; families with 3 or more open rows. TODO rows: section 4; status
lives in the rows, not here.

## 1. Intake

| # | Item | Class | Note |
|---|---|---|---|
| I1 | Skewer food type (`dishes/meals #13`): which foods, recipes | open | asked |
| I2 | Coaster and small plate compatibility (`food/placeable #6`): "define what these compatibilities would mean" | open | asked twice; meanings A to C explained |
| I3 | Flower pots placeable like candles (`blocks/flower-pots #1`): pot heights, models | open | asked (height); models not asked |
| I4 | Palette swap overhaul (`textures/food #10`): design | open | asked |
| I5 | Creative Brewing CurseForge publish (`creativebrewing/publish #3`) | clear | not a decision, a release effort with its own plan |
| I6 | Breads and Dough (`baking/baked-goods #17`), gnocchi (`dishes/meals #33`) | premise | "decisions" text was a build note and a stale check; rows treated as unblocked |

## 2. Owner rulings

| # | Item (owner's words) | Class | Ruling | Date | Source |
|---|---|---|---|---|---|
| R1 | Skewer food type | open | "Defer": left out of this plan | 2026-11-21 | AskUserQuestion 2026-11-21 |
| R2 | Coaster compatibility | open | "a, mark other non food related plates for review and possible removal": other mods' items go on our coaster (tag entries plus a model file each); other mods' non-food plates are listed for review and possible removal | 2026-11-21 | AskUserQuestion 2026-11-21 |
| R3 | Pot height | open | Second pot 2 px taller than the first | 2026-11-21 | AskUserQuestion 2026-11-21 |
| R4 | Pot models | open | Two offset vanilla pot cuboids, no new art; height a constant so it retunes (owner, 2026-11-22: confirmed) | 2026-11-22 | owner, 2026-11-22 |
| R5 | Palette swap design | open | Fix the three known faults only: non-monotonic palettes, interpolation when the palette is larger than the texture, even rank spacing. No full redesign; owner reviews the outputs | 2026-11-21 | AskUserQuestion 2026-11-21 |
| R6 | Creative Brewing CurseForge publish | clear | Left out; stays in `plan_cb_publish.md` | 2026-11-21 | agent default, unconfirmed |
| R7 | `baking/baked-goods #22` Chorus Cookie | open | Bakes like the other cookies: cookie dough + 2 chorus fruit grain -> 12 on the baking sheet, no Uncanny Cookie; the 8 Uncanny Cookies + grain -> 8 hand craft goes (owner, 2026-11-22; replaces a same-day Uncanny Cookie bake ruling) | 2026-11-22 | AskUserQuestion 2026-11-22 |
| R8 | `baking/baked-goods #35` Adzuki Bun | open | Moves to the oven: flour + roasted adzuki beans + milk on the baking sheet -> 2; the grid recipe goes (owner, 2026-11-22) | 2026-11-22 | AskUserQuestion 2026-11-22 |
| R9 | `baking/baked-goods #2` muffins on paper | open | The paper sequenced assembly makes 6 (was 4); the oven stays 4 (owner, 2026-11-22) | 2026-11-22 | AskUserQuestion 2026-11-22 |
| R10 | `baking/baked-goods #36` Strawberry Scones | open | 2 per bake (was 1) (owner, 2026-11-22) | 2026-11-22 | AskUserQuestion 2026-11-22 |
| R11 | `blocks/flower-pots #2` pottable tooltip | open | A "Pottable" line on every plant item that can go in a pot, not on pot items (owner, 2026-11-22) | 2026-11-22 | AskUserQuestion 2026-11-22 |
| R12 | `dishes/ingredients #15` churn | open | Pinned: churn wanted later; nothing built (owner, 2026-11-22) | 2026-11-22 | AskUserQuestion 2026-11-22 |
| R13 | `dairy-fats-and-batter #1` whale oil lamp | open | The model starts from the vanilla lantern (texture copied as-is); the recolor waits for an owner palette (owner, 2026-11-22) | 2026-11-22 | AskUserQuestion 2026-11-22 |
| R14 | `dishes/ingredients #9` cheese | open | 4 wedges -> 1 wheel, matching the cut yield (owner, 2026-11-22) | 2026-11-22 | AskUserQuestion 2026-11-22 |
| R15 | `dairy-fats-and-batter #3` whale oil lamp fuel | open | All config values: tank 1000 mB, full when crafted; burns 1 mB per 24 ticks; refill with a whale oil bottle (250 mB) or bucket (1000 mB); goes out when empty; keeps its oil when broken (owner, 2026-11-22) | 2026-11-22 | AskUserQuestion 2026-11-22 |

## 3. Phases

Order is by independence; each phase is committed on its own. Rows in section 4.

| Phase | Scope | Owner of code | Notes |
|---|---|---|---|
| P1 | Flower Pots: 12 buildable rows plus `blocks/flower-pots #1` | KubeJS / Creative Delight (Java for the two-pot block) | Look-check the first two-pot model with a client capture before test rows |
| P2 | Dairy, Fats and Batter (7) and Produce and Potatoes (4) | KubeJS / Creative Delight | Overlaps the uncommitted food diversity round 4 in the working tree: land that first or build on top of it |
| P3 | Baking Types and Bakeware (7) and Cookies and Muffins (4) | KubeJS / Creative Delight | Same overlap as P2 |
| P4 | Coaster tag additions (`food/placeable #6`) and the non-food plate review list | KubeJS tags via `global.tags` | The review list is a report for the owner, no removal until the owner picks |
| P5 | Palette swap faults (`textures/food #10`) | `textureswapper/palette_swap.py` | Tool code only. Per the texture rule no texture is regenerated by the agent; the owner reruns it on their palettes and reviews |
| P6 | Structure Testing (3), Texture Tooling (3), Process (4) | docs, tools, scratch queue | `textures/tools #2` to `#4` need the owner's source and palette; ask before touching any texture |

## 4. TODO rows

Dairy, Fats and Batter: `dairy-fats-and-batter #1`, `#2`; `dishes/ingredients #9`, `#15`, `#27`, `#7`, `#35`.
Produce and Potatoes: `dishes/ingredients #17`, `#8`, `#21`, `#28`.
Baking Types and Bakeware: `baking/baked-goods #30`, `#35`, `#36`, `#40`, `#18`, `#20`, `#39`.
Cookies and Muffins: `baking/baked-goods #2`, `#22`, `#23`, `#32`.
Flower Pots: `blocks/flower-pots #1` to `#5`, `#7`, `#8`, `#10`, `#11`, `#12`, `#13`, `#15`, `#16`.
Placeable and textures: `food/placeable #6` and its review row, `textures/food #10`.
Structure Testing: `structures/testing #1`, `#3`, `#4`. Texture Tooling: `textures/tools #2` to `#4`.
Process: `process #11`, `#12`, `tooling/process #6`, `#7`.
Left out: `dishes/meals #13` (deferred), `creativebrewing/publish #3` (own plan).

## 5. Open items

- R6 is an agent default, unconfirmed.
- The P3 build calls and the open owner questions listed here earlier are answered by R7 to R12 (owner, 2026-11-22).
- Waiting on owner art or palettes (texture rule): baking dish (`baking/baked-goods #30`), oatmeal raisin cookie
  (`#23`), five new muffins (`#2`), seven fruit scones (`#40`), strawberry scone and sweet potato muffin retextures
  (`#36`, `#32`), plain donut (`#39`, placeholder allowed per S2, see
  [art_needed_2026-11-22.csv](texture_sources/art_needed_2026-11-22.csv)), whale oil lamp recolor (R13) and cauldron
  (`dairy-fats-and-batter #2`).
- Gnocchi `dishes/meals #33`: status checked 2026-11-22 (row log): Gnocchi is Veggies' potato noodle renamed, potato
  dough exists (`dishes/ingredients #27`); only the two noodle textures (owner art) are open.
