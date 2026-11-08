# Plan: farming, cooking and homesteading batch (2026-11-07)

**Status 2026-11-07 night: BUILT except `kitchen #36` (double fridges, not started) and F2 (random-tick audit done as a
brief, [briefs/random_tick_audit.md](briefs/random_tick_audit.md), fixes await owner approval). Nothing installed, nothing
run in game. Jars to build and install at a restart: Common, Engineering, Delight (built), Compat (built), Brewing,
Worldgen. Scratch rows: queue section "SQ-FCK farming and cooking batch". Open owner questions: Supplementaries Doormat
has no recipe after the flax removal (give one or hide); Floral Enchantment flower bushes never grow naturally (stop ticks
or bridge). Notes: A4 bone broth already fits the copper pot (no recipe added, SQ-FCK-03); A6 restored the 0.0005 that
1.2.00 shipped (source regression from the split merge); A8 uses a 3-try random patch (1.19.2 multiface has no block
count); A12 added 21 feasts (Gleaming Salad already had a can) and the generator also caught up 15 cans of newer foods.**

Source: owner message 2026-11-07 against [TODO.md](todo/TODO.md). Open questions are marked
**Q1..Q29** and are answered in the owner's reply; each task says which Q blocks it. Effort: S under an hour, M a
session, L several sessions.

## Part A: decided, ready to build (data / KubeJS / small Java)

| # | TODO row | Change | Where | Effort | Notes |
|---|---|---|---|---|---|
| A1 | `farming/crops #4` | Weeds: no natural growth on Rich Soil, Rich Farmland, Fertile Soil; hand planting allowed | none (verify) | S | Already true per [briefs/farming_crops.md](briefs/farming_crops.md): IW spawns weeds only with vanilla farmland as owner block, so rich soils never grow them on their own. The brief's planned hand-planting mixin is dropped. Add a test row only. Q1 |
| A2 | `flowers #3` | Bushier Flowers never generate | data + docs | S | The 5 `forge:none` biome-modifier overrides exist. Gap is "an update adds a file and bushes return": add a startup check that logs and lists any `bushierflowers` biome modifier not overridden. Q2 |
| A3 | `flowers #6` | Rich / fertile dandelion grow times | config | S | Swap already shipped in 1.1.06 (Fertile 10 min, Rich 20 min). Q3 |
| A4 | `cooking/kitchen #32` | Copper pot tooltip | KubeJS lang | S | Text from the row: cooks some of what a cooking pot can, 4 ingredients max. Bone broth line dropped unless Q4 says otherwise |
| A5 | `cans/canning #21` | Oatmeal can additive: sugar | KubeJS / data | S | Change the additive in the canned oatmeal recipe to sugar; regenerate if the row is generated (`tools/gen_can_types.py`) |
| A6 | `farming/crops #40` | Magic peanut drop 0.05% | Vintage member loot | S | `peanut_crop.json` random_chance 0.01 -> 0.0005. Read as 0.05 percent. Q5 |
| A7 | `farming/crops #41` | 3 Rich Soil -> 1 Fertile Soil | KubeJS (NCH) | S | Recipe through `global.NCH`; check the current Fertile Soil recipe and JEI for clashes |
| A8 | `flowers #4` | Glow lichen 3x spawn rate, bigger patches | datapack override | S-M | Override vanilla `placed_feature/glow_lichen` count x3 and `configured_feature/glow_lichen` `number_of_blocks` up. Terralith / other mods may replace the feature in their biomes: check with `/place`. Q6 |
| A9 | `mobs/bees #3` | Honeycomb shearing 3x | patcherMod mixin + config | M | Vanilla gives 3 per shear, so 9. `@ModifyArg` on `BeehiveBlock.dropHoneycomb`, config `[bees] honeycombMultiplier`. Needs a Creative Common rebuild and restart. Q7 |
| A10 | `farming/wild-crops #5` | Third, most common cantaloupe patch (normal and mini) | data (`wild_crops.json`) | S-M | Copy the existing cantaloupe rules, higher rate; scratch-queue a density check. Q8 |
| A11 | `farming/crops #25` | Mini melon: note only | docs | S | Record: plant growth and seed shape mirror the Mini Cantaloupe. Row stays Planned, no build |
| A12 | `cans/canning #17` | All current feasts cannable | tools + KubeJS | M | 20 missing feast blocks listed in [briefs/cans_canning.md](briefs/cans_canning.md): add one row each to `gen_can_types.py`, `/cdevgen`, restart. Sprites are a separate art task. Q9 |
| A13 | `farming/crops #17` | Water bucket / bottle wets farmland, Rich Farmland, Fertile Soil | patcherMod or CD | M | Use on the block: bucket returns an empty bucket (water consumed), bottle returns a glass bottle. Same wetness mechanic as the watering can. Q10 |
| A14 | `farming/crops #30` | Well vs watering can bug | none | S | Investigated, see Part D |
| A15 | Botany Pots growth 8x slower | all Botany Pots crops | KubeJS recipes event | S-M | Details in Part C. Q15 to Q17 |

## Part B: restore azalea wood (`farming/trees #7`)

Reverses `farming/trees #5`: remove `removedWoods.azalea` (100 ids), Quark Azalea Wood module back on in
`config/quark-common.toml` (and `configs_edited/`), restore the azalea rows in `wood_types.js`, `bark.js`,
`engineeringtags.js`, `fabric.js`, the CD `kitchen.js` rack list and `RackWoods`, un-hide the 6 Railways tracks, and
regenerate generated lists naming azalea. Azalea tree trunk goes back to the azalea log (Quark does this when the module is
on). Test RW-29 is replaced. Effort M. Needs restart. Q11 (what happens to items already converted to oak), Q12 (release state).

## Part C: breakdowns

### C1. Crop findings G8 to G15 (`farming/crops #1`) and parity (`farming/crops #2`)

| Finding | Problem | Fix | Effort | Recommended |
|---|---|---|---|---|
| G8 | Right-click replants ripe weeds | add `immersive_weathering:weeds` to `#quark:simple_harvest_blacklisted` | S data | build |
| G9 | Flax top half ignores right-click | Quark rejects the forwarded click; needs a Quark mixin or a CD script that redirects the hit to the lower half | M | build with G13 as one fix |
| G12 | Tea refuses Farmland / Rich Farmland | placement mixin calling `CropParity.isFarmlandSoil`, same as tobacco | M | build |
| G13 | Corn top half ignores right-click | same cause as G9 | M | with G9 |
| G14 | Coffee eats nearby crops | Farmer's Respite design | none | reject (Q13) |
| G15 | Pineapple only on sand / coarse dirt | add farmland soils to `fruitsdelight:pineapple_grows_on` | S data | Q13 |

Parity (#2) status per crop: tobacco done; hemp done (#37); tomato done (#29); weeds = G8; flax = G9; corn = G13; tea = G12;
pineapple = G15; coffee = G14 rejected; grapes = Vinery's own placement rule (our port, so a small mixin or block-class
change; Q13). When G8, G9, G12, G13, G15 and grapes are built, #2 closes.

### C2. `farming/crops #15` "other plants in random growth"

The note names no system. Candidates and what each would do: (a) soil boost, Rich Soil 20% and Fertile Soil via
`MulchBlockCropGrowthMixin`, extended to non-`CropBlock` plants such as saplings, berry bushes, stems, flowers; (b)
Immersive Weathering growths (moss, ivy, weeds spread); (c) stem extra fruit. Q14.

### C3. Wild crops #3 and #4 decisions (from crops.md section 4)

`#3` Biome gaps: V19 13 rock / ice / gravel biomes with no crop (accept, or add rock-tolerant plants); V16 no orchard or
pumpkin biome (Skylands Autumn stands in); V4 jungles crowded (accept, or move some fruit trees to temperate); V3 carrot /
onion / corn share 57 biomes (accept, or split by climate); V20 tea is done.
`#4` Duplicates: V5 two coffees and two apples (keep both, or merge through item_dict and recipes); V18 Farmer's Respite
coffee has no source (merge into Rustic coffee, add a source, or leave); berries in the trader. V6, V7, V1 done. Q18 to Q21.

### C4. Flowers #2 and #5, defined

`#2` Moobloom rules follow flowers: a generator `tools/gen_moobloom_rules.py` that reads `wild_flowers.json` and the
moobloom table, rewrites `moobloom_spawns/creativeengineering.json` (36 rules) and the mooblooms.md 2.8 table, keeping
hand-set rules from a `manual` block; registered in `tools/generators.json`, run with `/cdevgen`. Effort M. Q22.
`#5` Merge azalea flowers: IW's and Twigs' flowers become one item. Steps: pick the canonical item, remap the other through
the item-merge system, one texture (IW's), one placement (Twigs'), both recipe sets (the tea tag already holds both). Effort
M. Q23.

### C5. Botany Pots (investigated)

Jar BotanyPots 9.0.44, no config, no global knob, no pack scripts today. Growth is `growthTicks` per crop recipe (about
1245 in the jar, more from installed mods) and `growthModifier` per soil. Plan: one KubeJS `ServerEvents.recipes` pass that
multiplies `growthTicks` of every `botanypots:crop` recipe by `global.BOTANY_GROWTH_MULT` (8). Covers future crops and other
mods, JEI shows the new values. Conflict: `mod-additions #35` says x0.2 and wants flowers faster; Q15 to Q17.
Lemon tree cut-off: the Fruits Delight lemon crop displays only the lower half of the two-block tree (`half` property).
Options: lower half only (S, tree stays short) or a one-block model that stacks both halves (M). Q24.

## Part D: Well and watering can (`farming/crops #30`)

Investigated. The can code drains the well through the Forge fluid handler (`WateringCanItem.tryFill`, simulate then
execute). Just a Well's tank is a plain `FluidTank` whose `drain` works and which refills over time (`fillTick`). The
r1102b run saw the can take the well from 1000 to 0 mB. The r1101 "86000 mB unchanged" reading came from a `Tank:{}` NBT
merge the well ignores. No code defect found, so there is nothing to patch. Action: change the row to Testing, add a
scratch-queue row for a clean live check (fill can, read `Amount` before and after, wait for the refill).

## Part E: Farmer's Delight containers keep contents on pickup (`cooking/kitchen #40`)

From the TODO row and its brief. Kettle keeps fluid, ingredients and output; cooking pot and copper pot keep input and
output; overflow slots drop on break. The picked-up item stores the block-entity data in item NBT, shows the enchant glint,
and does not stack (max stack 1 when it holds contents). Needs: a loot-table copy-nbt or a break hook in the CD farmers
member, an item mixin for glint and stack size, a place-from-item restore, WTHIT / tooltip line "Holds: ...", interaction
with the remainder buffers (`kitchen #1`..`#14`) and the cookware pickup toggle (`#27`). Effort L. Q25 to Q26.

## Part F: investigations (no build until you choose)

| Item | Finding | Effort | Recommended |
|---|---|---|---|
| Double fridges connect (`kitchen #36`) | CfB fridge is a closed jar; "double" today is 2 tall. Needs a client-only wrapper on the baked model / renderer in the CD member, neighbour detection left and right, 2-4 new models, separate inventories kept | M-L | cosmetic only, shared door optional. Q27 |
| Faucet lines up with non-full blocks (`kitchen #26`) | Supplementaries faucet (jar). Stream drawn at a fixed length by the tile renderer. A renderer mixin can shorten the stream by the target block's shape top. Depth / centring needs a table | S-M | shape-based height first. Q28 |
| Wall-hung skillet (`kitchen #39`) | FD `SkilletBlock` cannot take a new property by mixin. Needs a new block `hanging_skillet` (facing, shape, model, loot, lang), a `SkilletItem.useOn` hook when a wall face is clicked and the space above is clear, item NBT round trip. Hung skillet does not cook | M | Q29 |

## Done-means checklist per task

Writeup row updated, scratch-queue rows added (not run), changelog bullet in `unreleased.md` where a player sees it, TODO
row status, `configs_edited/` copy for any config. Jars wait for a restart window; none are installed by this plan.

## Owner decisions (2026-11-07, answers to Q1 to Q29; these supersede the rows above where they differ)

| Q | Decision | Build impact |
|---|---|---|
| Q1 | Weeds may also be hand-planted on Rich Farmland | Rich Farmland is in none of IW's weed soil tags, so a weed cannot be placed there today. Add it to the IW placement tag (data) and confirm natural spawning stays off (the spawn owner block is vanilla farmland only) |
| Q2 | Keep the 5 `forge:none` overrides only | A2 startup check dropped |
| Q3 | Dandelion grow times: Rich Soil 10 min, Fertile Soil 5 min | Config `creativedelight-common.toml` `richSoilTicks` / `fertileSoilTicks` (was 20 / 10 min); `configs_edited/` copy; changelog bullet with old values |
| Q4 | Copper pot tooltip plus bone broth cookable in the copper pot | Tooltip line and a copper pot recipe for bone broth (check the 4-ingredient limit) |
| Q5 | Magic peanut drop 0.05% | 0.01 -> 0.0005 |
| Q6 | Glow lichen 3x spawn count, 3x patch size | placed feature count x3, configured feature `number_of_blocks` x3 |
| Q7 | Honeycomb 6 per shear (not 9), configurable | Mixin multiplier 2 on vanilla's 3; config key |
| Q8 | Third cantaloupe patch: same biomes, most common, normal and mini | `wild_crops.json` |
| Q9 | Can all 20 missing feasts, 6 servings each | `gen_can_types.py` rows |
| Q10 | Water bucket soaks a 3x3 area; bottle single block; water consumed | Container returned empty |
| Q11, Q12 | Items already converted stay oak; 1.2.00 is live | Restore azalea wood; unreleased changelog bullet "Restored azalea wood" under Azalea Wood; do not edit 1.2.00.md |
| Q13 | Build G8, G9 + G13 (corn top half only), G12, G15; grapes get a tooltip saying they do not grow on farmland | Flax is removed (new item F1), so no flax fix. G14 resolved by the coffee merge (C6) |
| Q14 | `farming/crops #15` becomes a random-tick audit: list plants and blocks that need no random ticks but receive them, and stop the ticks | Investigation first (new item F2) |
| Q15 to Q17 | Botany Pots: crops 8x slower, flowers 2x slower, bone meal removed, 8x applies to base growth and soil modifiers stack | One recipes pass; flower crops identified by category / tag |
| Q18 | Rock, ice, gravel biomes: accept | `farming/wild-crops #3` V19 closed |
| Q19 | Jungles stay crowded (intentionally fertile); cold biomes have fewer, temperate and hot dry about equal | V4 closed |
| Q20 | Carrot, onion, corn shared biomes: accept | V3 closed |
| Q21 | Apples: keep Vinery's; merge Fruits Delight's apple into it | Item merge (remap FD apple, tree and sapling to Vinery's); check what the FD apple tree becomes |
| Q21b | Coffee: merge Farmer's Respite coffee into Rustic Delight coffee | FR bush removed: no Nether spawn, no crop eating; recipes and tags remapped |
| Q22 | Moobloom rules: build the generator | `tools/gen_moobloom_rules.py`, add to `generators.json` |
| Q23 | Azalea flowers: Immersive Weathering's survives | Twigs' remapped; Twigs placement kept |
| Q24 | Lemon tree: lower half only in the pot | Override `fruitsdelight:recipes/botanypots/lemon.json`; confirm in game |
| Q25, Q26 | Filled FD containers do not stack, show the glint and a contents tooltip; overflow slots drop on break | Item NBT plus glint mixin |
| Q27 | Double fridges: connect, mirror the doors, keep separate inventories | Model wrapper plus door mirror (FLIPPED); medium to large |
| Q28 | Faucet: move horizontally to match the inner face of the container and down so the spout bottom meets the block bottom where needed; start with the Supplementaries jar | Renderer mixin driven by the target's inner shape; start with the jar, then other containers and the kettle |
| Q29 | Hung skillet: same item, auto on a wall click, space above clear | New `hanging_skillet` block |
| Flax | Remove flax | Item F1 |

### New items

- **F1: Remove flax.** Supplementaries flax crop, seeds, wild flax (already off), and everything made from it, merged into the
  rope / string route of `merges/materials #2` ("Flax goes"). Needs the item list and the recipes that use flax; investigate
  before building. Drops the G9 flax half.
- **F2: Random-tick audit.** Scan the block registry for blocks that have a `randomTick` override or `isRandomlyTicking`
  true but do nothing useful (full-grown crops, decorative plants, blocks whose tick is empty), list them with their mod,
  and stop the ticks for the ones that are safe (Mixin on `isRandomlyTicking`, or the block's own flag). Output: a list for
  you to approve before any change. Effort M.
- **C6: Coffee merge.** FR coffee beans, bush, tea and coffee recipes remapped to Rustic Delight coffee; the Nether basalt
  spawn is already off. Same merge mechanism as the other item merges.
