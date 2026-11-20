# Owner notes 2026-11-19: leaf piles, dust particles, Diet Log

<!-- generated:status_index begin open_work sha1=fe3975acf7a1 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 6 open TODO rows (Testing 6), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `food-tuning #17` | Testing | C | Diet Log: last 30 meals on the first page; hover shows diversity contribution; aged-out meals hidden |
| `food-tuning #18` | Testing | C | Diet Log: reached unlocks struck through in Milestones, left off the ladder bar |
| `food-tuning #19` | Testing | C | Diet Log: diversity icon is a food matching the diversity |
| `gameplay/blockrunner #15` | Testing | C | Leaf piles drawn from inside; leaf screen overlay removed |
| `gameplay/blockrunner #16` | Testing | C | Tall leaf piles see-through |
| `gameplay/blockrunner #17` | Testing | C | Dust puffs when walking in or on dust blocks |
<!-- generated:status_index end open_work -->

Intake of the scratchpad "Idea, Notes, Changes" block (leaf / dust / food log lines) and the follow-ups given in the
2026-11-19 AskUserQuestion answers. Rows: see Open work.

## 1. Intake

| # | Item (owner's words) | Class | Found |
|---|---|---|---|
| 1 | apply food log stretch | premise -> open | Built and installed: `[dietLog] extraWidth` in `solraspberrypie-client.toml`, still 0 (plan_diet_books.md 1.5). Needs a value. |
| 2 | dust blocks cause dust particles colored like block when walking, check ash implementation, mirror to all dust | open | Supplementaries `AshLayerBlock`: client, `entityInside`, a living entity standing in it that moved horizontally spawns one ash particle at 1 in 2 ticks at its feet (`y + layers * 0.125`, x/z speed +-1/12, y speed 0.05); `fallOn` spawns `min(12, fall * 1.4)` (y speed 0.12). Which blocks count as dust: open. |
| 3 | leaf layers are always transparent inside and out (past ~6 layer currently renders solid) | premise | IW pile models switch texture by height: light (1-2 px), medium (4-10), heavy (12-16). The heavy textures have no transparent pixel (heavy_oak_leaves 0 of 256). |
| 4 | leaf overlay: misunderstood, remove on player overlay | clear | `surface/LeafOverlay` (screen tint, plan_post_scratch_1115.md G7.14) and `[surface.passthrough.layers] leafOverlay`. |
| 5 | when the players head is inside of leaf layers ... render the outside texture on the inside of the block as well ... including partial layers | open -> ruled | Was G4e "inside view", not built. |
| 6 | rp: permanent unlocks milestones text are strikethrough when unlocked, gray/black based on | open | Milestones rows: black reached, gray not (`DietLogPages.ListPage`). Note cut off. |
| 7 | remove sorting/'newest first' from diet log, only last 30 meals | open -> ruled, extended | See rulings 7 to 12. |

## 2. Owner rulings

| # | Item (owner's words) | Class | Ruling | Date | Source |
|---|---|---|---|---|---|
| 1 | apply food log stretch | open | `extraWidth = 40`. | 2026-11-19 | AskUserQuestion 2026-11-19 |
| 2 | dust particles when walking | open | Mirror the ash: walking puffs and the landing burst, vanilla falling-dust particles of the block (block-colored). Blocks: dust layer piles (CE metal dust piles, IE sawdust, IW ash layer, Create Compressed powdered obsidian pile), full powder blocks (powdered sugar block, powdered obsidian block, IW ash block, vinteum dust block) and the 16 concrete powders. Full blocks: while walking on top. Block list = a block tag. | 2026-11-19 | AskUserQuestion 2026-11-19 |
| 3 | leaf layers always transparent | premise | Tall piles (the models that use IW's heavy texture) use a see-through texture, configurable: IW's medium texture of that leaf type (default) or the tree's own leaves texture. Done by retexturing at model bake, no texture file made. | 2026-11-19 | AskUserQuestion 2026-11-19 |
| 4 | remove the leaf overlay | clear | Remove `LeafOverlay` and its config key. | 2026-11-19 | AskUserQuestion 2026-11-19 |
| 5 | inside view of leaf piles | open | Every leaf pile (IW piles incl. generated modded ones, Twigs bamboo leaves, full bamboo leaves) also draws each face from the inside, same texture and tint, same cull face as the outside face, at every layer count. Client model wrapper. | 2026-11-19 | AskUserQuestion 2026-11-19 |
| 6 | unlock rows strikethrough | open | Milestones list: a reached one-time unlock is struck through; its color is black when the current diversity is at or above it, gray when below. | 2026-11-19 | AskUserQuestion 2026-11-19 |
| 7 | remove newest first and apple pie memory | open | The "Newest first" line and everything below it on the meals page go; the meals page itself is removed (ruling 8). | 2026-11-19 | AskUserQuestion 2026-11-19 |
| 8 | last 30 meals position | open | "Last 30 Meals" label and a 6 x 5 grid move to the first page (Apple Pie's Overview), above the current diversity line and the "need to eat" lines. Other book content stays. | 2026-11-19 | AskUserQuestion 2026-11-19 |
| 9 | respec / drop hide meals | open | A meal shows only while its food is in the decay window: a Change or Drop that ages foods out removes all their meals from the grid, which closes up (4 aged out -> 26 shown). | 2026-11-19 | AskUserQuestion 2026-11-19 |
| 10 | hover contribution | open | Hover: "N. Name", then this meal's diversity contribution (the food's value at this meal's age), then the food's total contribution (what Apple Pie counts, from its latest meal); the per-meal line is struck through when it differs from the total. | 2026-11-19 | AskUserQuestion 2026-11-19 |
| 11 | remove unlocked effects from the diet ladder | open | The Diet Ladder bar leaves out reached one-time unlocks; they stay in the Milestones list. The key is unchanged. | 2026-11-19 | AskUserQuestion 2026-11-19 |
| 12 | potato on the current diversity line matches the diversity | open | The image left of "Current Food Diversity" becomes a food icon: foods eaten (Carrot's list) ranked by their own Apple Pie complexity; the current diversity, as a share of the ladder's top milestone, picks a rank band; one food of that band shown. No eaten food in the band: a static pool per diversity range from a client config list. | 2026-11-19 | AskUserQuestion 2026-11-19 |
| 12a | static pool ranges and foods | default | Agent default, unconfirmed: ranges and foods picked by the agent in the client config. | 2026-11-19 | AskUserQuestion 2026-11-19 |

## 3. Build notes

Creative Compat (rows `gameplay/blockrunner #15` to `#17`, queue CQ-PS15B-05, -06):

- Rulings 3 and 5: `surface/LeafPileModels`, on `ModelEvent.BakingCompleted`. Pile blocks by class (IW
  `LeafPileBlock`, its `getLeafType().leaves` as the tree) and `FallingLayers.isBambooLeaves`, since block tags are not
  bound at bake. Each quad gets a copy with its vertices reversed, its normal flipped and its facing set to the
  opposite, moved 1/512 block into the block. The copy goes in the same cull list as its source, so it hides together
  with the outside face and never shares a plane with a neighboring pile's face. Tall texture: a sprite named
  `.../heavy_<x>` moves to `.../medium_<x>` (MEDIUM, falling back to the tree's particle sprite when the medium one is
  missing) or to the tree's sprite (TREE), with UVs remapped through `getUOffset` / `getU`. Client keys
  `[surface.passthrough.layers] leafInsideFaces` (true) and `leafTallTexture` (MEDIUM), read at bake (F3+T).
- Ruling 4: `LeafOverlay` and `leafOverlay` removed; Forge drops the old key from the live toml on the next load.
- Ruling 2: `surface/DustParticles`, client tick. It covers living entities within 32 blocks of the camera that moved
  sideways, in a block (layers) or on one (full blocks, 0.2 below the feet) of `#creativecompat:dust_particles`
  (blocktags.js). At 1 in 2 ticks they spawn a vanilla `DustParticleOptions` of the block's tint (BlockColors), else
  its map color, at the top of the block's shape, speed +-1/12 sideways and 0.05 up. The local player landing after a
  fall bursts `ceil(min(12, fall x 1.4))` at 0.12 up. Supplementaries ash is not in the tag (it has its own). Ash's
  rule only reaches entities the client moves, so in practice the player; ours reaches every nearby mob too (agent
  default, unconfirmed). Key `dustParticles` (true).

Raspberry Pie (rows `food-tuning #6`, `#17` to `#19`, queue CQ-RPI-57 to -61; detail in raspberry_pie.md section 12):

- Ruling 1: `extraWidth = 40` in `config/` and `configs_edited/root_config/` `solraspberrypie-client.toml`; the
  RpConfig default stays 0.
- Rulings 7 to 10: `DiversityPageMixin` redirects in Apple Pie's `DiversityPage` constructor. The 1 x 35 spacer under
  the header becomes the meals grid: 15 px icons on a 16 px step, scaled by `[dietLog.pages.meals]` (reused for the
  grid). The 1 x 10 spacer above the gate lines is dropped. Meals pages and the "Newest first" line are removed.
  Hover contributions use Apple Pie's `FoodList.calculateDiversityContribution`.
- Ruling 12: the `icon` redirect, 16 px food item. Band quantiles over all counted foods by complexity. The static
  pool `[dietLog] diversityIcons` is an agent default, unconfirmed: 0 apple/bread/carrot/sweet berries; 10 baked
  potato/cooked chicken/mushroom stew/cookie; 25, 40, 60, 80 Farmer's Delight meals rising; 100 enchanted golden
  apple.
- Open risk: under the gate, the two warning lines may reach the page number (look-check, CQ-RPI-57).
