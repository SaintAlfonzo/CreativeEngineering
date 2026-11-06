# Golden Food: donor-jar rebuild (Creative Delight member `portedGolden`)

Plan step J4 of [plan_creative_delight.md](plan_creative_delight.md), TODO row `creativedelight/members #50`. Written and
built 2026-11-02. **Status: BUILT, installed, not booted.** `development/creativeDelight/members/golden`, jar
`portedGolden-2.0.0-ce-19.2-01.jar`, nested in the Creative Delight jar (`bundle_members`; first CD install 2026-11-02
23:16 by the plan-K session, `goldenfood-1.19.2-2.0.0.jar` displaced to `jar_archive/displaced/`). Second pass the same
night (owner): the Golden Food Enchantment, its anvil recipe and the village books removed, enchanted golden foods made by
sequenced assembly (3.4); CD jar reinstalled that night. Third pass 2026-11-03 (food.md 3.16, pack scripts and CD jar
resources only, no member Java change): golden recipes on 8 nuggets (3.4a), gilded assembly 8 x (1 ingot + 125 mB)
(3.4), multiplier values (3.6), gilded names (3.7); in the CD jar installed 2026-11-03 00:08. Offline checks pass
(section 6). Last game boot 2026-10-31, so none of this has booted. Tests: section 8 (`SQ-GLD-`, `SQ-GLD2-`, `CQ-GLD-`,
`CQ-GLD2-`, queue sections SQ-CDJ6 and SQ-CDJ7). Server by hand: copy `goldenfood_upstream/` beside `mods/`.

**Licence: All rights reserved** (upstream `mods.toml`). So the route is the Miner's Delight / Oceanic one (port skill,
memory `miners-delight-backport-plan`): the unmodified upstream jar is a sha1-pinned donor in
`goldenfood_upstream/goldenfood-1.19.2-2.0.0.jar` (sha1 `4109e4d6d484892b8efc201b1a32b9361df930cd`); the member reads
its textures, models, lang, recipes and loot tables from it at load time; everything the member ships is our own GPLv3
code written from the mod's behaviour. Nothing of the upstream jar ships (`checkNoUpstreamResources`, `check_donor.py`).

## 1. Upstream inventory (goldenfood-1.19.2-2.0.0.jar)

Native Forge 1.19.2, mod id `goldenfood`, author GGWPJason, no dependencies but Forge and Minecraft, no mixins, no
config, no access transformer, no block entities.

**Classes (17):** `GoldenFood` (mod class: registers, plus log-only setup listeners), `GoldenFood$ClientModEvents`
(log only), `block/GFBlocks`, `block/custom/GoldenCakeBlock`, `block/custom/EnchantedGoldenCakeBlock`,
`block/custom/EnchantedBlockItem` (glint), `item/GFItems`, `item/custom/ModChorusFruitItem`,
`EnchantedModChorusFruitItem`, `EnchantedBowlFoodItem`, `EnchantedMilkBucketItem`, `EnchantedHoneyBottleItem` (glint),
`enchantment/GFEnchantment`, `enchantment/GFEnchantments`, `event/ModAnvilUpdateEvent` (Forge bus subscriber),
`tab/GFTabs`, `tab/GFTabs$1`.

**Items, 47 under `goldenfood` + 3 vanilla replacements:**

| Group | Ids | Class | Stack | Rarity |
|---|---|---|---|---|
| Golden foods (17) | `golden_` + melon_slice, sweet_berries, glow_berries, baked_potato, beetroot, dried_kelp, cooked_beef, cooked_porkchop, cooked_mutton, cooked_chicken, cooked_rabbit, cooked_cod, cooked_salmon, bread, cookie, pumpkin_pie; `golden_chorus_fruit` | plain item; chorus: chorus fruit with a second teleport | 64 | rare |
| Enchanted foods (17) | `enchanted_golden_` + the same, plus `enchanted_golden_carrot` | vanilla enchanted golden apple class (glint); chorus: glinting chorus with a second teleport | 1 | epic |
| Stews (3 + 3) | `(enchanted_)golden_mushroom_stew`, `_beetroot_soup`, `_rabbit_stew` | bowl food, bowl back | 1 | rare / epic |
| Milk (1 + 1) | `(enchanted_)golden_milk_bucket` | milk bucket, bucket back | 1 | rare / epic |
| Honey (1 + 1) | `(enchanted_)golden_honey_bottle` | honey bottle, bottle back | 8 / 4 | rare / epic |
| Cakes (1 + 1) | `golden_cake`, `enchanted_golden_cake` | block item (enchanted: glint); **not in the creative tab** | 1 | rare / epic |
| Vanilla replacements (3) | `minecraft:golden_apple`, `minecraft:enchanted_golden_apple`, `minecraft:golden_carrot` | registered in the `minecraft` namespace, replacing vanilla | 64 / **1** / 64 | rare / epic / rare |

Stack is the upstream value. In the pack (`tools/gen_stack_sizes.py`, `stack_sizes.json`; plan_scratch_bugfix_1103 7.1, checked
2026-11-03, already so): every golden and gilded food, stew and honey, the golden apple, the enchanted golden apple and the golden
carrot stack to 16; the golden and gilded milk buckets and cakes stay 8 (bucket and placed-cake rules win). The 6 stews are vanilla
`BowlFoodItem`s, which hand back one bowl for the whole stack, so they are in Farmer's Delight's `soupItemList`
(`farmersdelight-common.toml`, FD `SoupItemMixin`) like the vanilla stews (SQ-GLD-15).

Food: every golden food (and the replaced golden apple and golden carrot) is 4 hunger, 1.2 saturation modifier,
Regeneration II 5 s, Absorption I 2 min, always edible. Every enchanted food (and the replaced enchanted golden apple)
is 8 hunger, **8.0** saturation modifier, Regeneration II 20 s, Resistance I 5 min, Fire Resistance 5 min, Absorption IV
2 min, always edible. The live dump (`kubejs/exported/food/foods.json`) confirms the vanilla replacements are active.

**Blocks (2):** `golden_cake`, `enchanted_golden_cake`: vanilla cake class (state `bites` 0 to 6), wool material,
strength 0.5, wool sound. No loot tables (a cake drops nothing).

**Enchantment (1, removed in the rebuild, 3.4):** `goldenfood:gf_enchantment` "Golden Food Enchantment": very rare,
max level 1, category vanishable, main hand, cost 30 to 35, not treasure (table, librarians and random books could offer
it). No effect on gear; its only use was the anvil recipe.

**Creative tab:** label `gf_tab` ("Golden Food!"), icon the golden cake item.

**Resources:** 2 blockstates, 14 block models (cake + 6 slices each), 48 item models (one stray `gluttony` model, no
item), 8 block and 26 item textures (an unused `golden_apple.png`), English lang (also keys for the three vanilla ids
under `goldenfood`, unused), a generator script `assets/goldenfood/python/0_GEN.py` (not a resource).

**Data (all in the `minecraft` namespace):** 24 shaped recipes, each 8 gold ingots around the base food (golden cake
around a cake); `minecraft:golden_carrot` replaces vanilla's recipe (ingots instead of nuggets). 16 copies of the
vanilla village chest tables (`chests/village/*`): each is vanilla's table with one extra entry, the enchanted book
(checked against the vanilla tables 2026-11-02, all 16). No tags, no advancements, no GLMs.

## 2. The member

`development/creativeDelight/members/golden/`, Gradle project `:portedGolden`, package
`com.SaintAlfonzo.creativedelight.golden`, shared sources `packs,upstream` (`DonorJars`, `BuiltInPacks`).

| Our class | Upstream behaviour it carries |
|---|---|
| `GoldenFoodMod` | mod class; verifies the donor first (stops the game with the missing / wrong-file message), registers, adds the two built-in packs |
| `upstream/UpstreamJars` | sha1 pin and donor reader (the line `tools/build_deploy.py` reads) |
| `upstream/GoldenTranslator` | donor `assets/` (minus the python script) and `data/` minus the 16 village tables |
| `registry/GoldenItems` | all 47 + 3 items with upstream classes, stacks, rarities, food |
| `registry/GoldenBlocks`, `content/GoldenCakeBlock` | both cakes in one class with a `Slice` (hunger, saturation, effects) |
| `registry/GoldenTab` | `gf_tab` |
| `content/GoldenChorusFruitItem` | golden / enchanted chorus (reach 32 / 64, cooldown 10 / 5 ticks, glint) |
| `content/ShinyItems` | glinting block item, bowl food, milk bucket, honey bottle |

Ids, the `bites` property and the tab label are upstream's, so old worlds (placed cakes, items in inventories) load
unchanged. The removed `goldenfood:gf_enchantment` is in CD `data/creativedelight/swaps/removed.json` (`removed`, plan
I1 format), so a world that has it loads without the missing-registry warning; an old book keeps the unknown id in its
NBT and shows no enchantment line.

## 3. Behaviour (matched)

### 3.1 Foods
As section 1. Upstream quirks kept: golden and enchanted milk buckets are milk (clear all effects); their food values
and effects never apply (milk's use never "eats"). Honey bottles also clear Poison. Fix 2026-11-03 (SQ-GLD-03): the milk
buckets cleared nothing before: `MilkBucketItem` cures with its own stack and Forge cures only effects whose curative
items hold that stack (a vanilla milk bucket). `ShinyItems.GoldenMilk` (golden) and `Milk` (enchanted, glint) cure as a
vanilla milk bucket first. The 280 t Haste / Speed II / Strength / Luck / Regeneration I bundle seen in r1103 is not from
these items (golden milk has no food effects yet showed it freshly applied; it refreshed at 279 t across drinks 8 s
apart): something near that bot; SQ-GLD-03 reruns on a fresh bot.

### 3.2 Cakes
Right-click eats a slice even when not hungry (no hunger check, unlike vanilla cake). Golden slice: 4 hunger, 1.2
saturation, Regeneration II 5 s, **Absorption II** 2 min (the item foods give Absorption I). Enchanted slice: 8 hunger,
8.0 saturation, Regeneration II 20 s, Resistance I and Fire Resistance 5 min, Absorption IV 2 min. Seven slices.
Candles: since 2026-11-03 (plan_scratch_bugfix_1103 7.2) a candle does nothing on a golden or enchanted golden cake
(`GoldenCakeBlock.use` passes; candle kept, cake unchanged). Upstream turned an uneaten golden cake into a **vanilla**
candle cake (the golden cake lost). The golden cakes join the candle integration with every cake, TODO
`baking/baked-goods #38` (Q1).

### 3.3 Chorus fruit
Eating first does the vanilla chorus teleport (16 blocks), then a second one: up to `reach` tries, each up to `reach`
blocks sideways and from 8 below to `reach - 9` above, clamped to the dimension's logical height; the cooldown becomes
10 ticks (golden) or 5 ticks (enchanted) instead of 20. A cancelled teleport event stops it and keeps the 20-tick cooldown.

### 3.4 Enchanted golden foods: sequenced assembly (owner, 2026-11-02)
Upstream made them on an anvil: a Golden Food Enchantment book plus a golden food, either slot, 1 level (24 pairs:
golden apple, golden carrot and the 22 goldenfood golden items but the cake). Owner decision 2026-11-02: the
enchantment is removed (not registered; id in the swap list `removed`) and so is the anvil route. Every enchanted
golden (gilded, 3.7) food is made like the gilded apple (food_families.md `golden/enchanted`): a Create sequenced
assembly on its golden form, **8 loops of deploy 1 gold ingot (`#forge:ingots/gold`, rose gold counts), fill 125 mB
liquid experience, press** (`global.CD.food` `goldenEnchant`; loops and mB in `global.CD.FOOD.gilded`). Owner
2026-11-03 (TODO `food/types-and-tuning #22`); until then it was 4 loops of a gold block
(`#forge:storage_blocks/gold`) and 250 mB. The helper change covers the gilded apple too (`minecraft/golden.js`).
- Recipes: CD `kubejs/data/creativedelight/kubejs/goldenfood/golden.js` (`// requires: goldenfood`), 23 calls:
  `minecraft:golden_carrot` to `goldenfood:enchanted_golden_carrot` and each of the 22 goldenfood golden items to its
  `enchanted_` form. The apple's own call stays in `minecraft/golden.js`. Recipe ids end in `_sequenced_assembly`
  (cd_food.js `suffix`).
- Incomplete items: 23 new `SEQUENCED_ASSEMBLIES` rows in `kubejs/startup_scripts/changes/transition_items.js`
  (`kubejs:incomplete_enchanted_golden_<food>`), each naming the golden form's sprite (the enchanted models reuse it).
  Startup script: needs a restart.
- The enchanted golden cake has no recipe, as upstream (it never had an anvil pair or a crafting recipe); creative only.
- Without Create no enchanted golden food can be made (the helper skips the recipe).

### 3.4a Golden foods: 8 gold nuggets (owner 2026-11-02, built 2026-11-03)
Every goldenfood golden food, the golden cake included, is its input food in a ring of 8 gold nuggets
(`#forge:nuggets/gold`, rose gold counts), through `goldenGild` in CD `goldenfood/golden.js` (23 calls). The helper
removes upstream's shaped recipe (8 gold ingots around the food, served from the donor data) and adds the family's
heated mixer (food + 6 nuggets) and Arc Furnace (food + 5) routes. Golden carrot: already 8 nuggets
(`minecraft/golden.js`). **Not changed: the golden apple** (vanilla's 8 gold ingots, `minecraft/golden.js`): the rule
names golden food in general but "replaces whatever the goldenfood recipes use today"; the apple's recipe is vanilla's,
so it waits for the owner. The golden milk bucket and honey bottle leave their bucket / bottle in the grid, as upstream.

### 3.5 Data and pack placement
Recipes come from the donor through the in-memory data pack (`goldenfood_upstream_data`, fixed at the bottom: KubeJS
and datapacks override). The pack's recipe scripts replace the golden carrot recipe as before (no
`minecraft:golden_carrot` recipe in the dump). The 16 village chest copies are not served (second pass): vanilla's own
tables load, so Forge runs other mods' injections natively. The `DONOR_DATA_JARS` row for Golden Food in
`tools/gen_loot_config.py` is gone again and the loot index was regenerated offline (no village table is listed as
overridden any more), so creativeworldgen loot `07_rules.js` `useVanilla(..., { winner: /goldenfood/ })` and the three
`restoreInjection` calls (they act only on overridden tables) are now no-ops, kept for safety. 2026-11-03
(plan_scratch_bugfix_1103 7.3): only Chalk's restore is kept; Botania's and CC:Tweaked's are gone, and their native
injections are removed after the roll (`10_removals.js`, LootJS `@botania` / `@computercraft` in `minecraft:chests/village/*`).

### 3.6 Values: multipliers of the input food (owner 2026-11-02, built 2026-11-03)
CD startup `kubejs_startup/goldenfood/golden_values.js` (priority -50, `ItemEvents.modification`): a golden food's
hunger = input hunger x 2, a gilded food's = input x 4, both capped at 20; the saturation modifier is the input's
(so saturation points scale with hunger). Multipliers and cap: `kubejs/generation_scripts/food/food_tuning.json`
`golden` (`golden`, `gilded`, `maxHunger`; the file is new, the rest of food.md 3.5 is not built; deploy class
`runtime`). The input's current properties are read at modification time. Effects, always-edible and stacks kept.
Covered: golden / gilded apple and carrot plus the 21 goldenfood pairs with a food input (46 items). Not covered:
golden and gilded milk bucket (milk is no food; their values never apply), the two cakes (slice values in
`GoldenCakeBlock`, Java; the cake's input slice is 2 / 0.1, which would make the golden slice 4 / 0.1: owner call),
and the pack's other golden foods (glistering melon slice, golden moss clump, nutritional bar, roll, coffee beans).
With vanilla inputs (hunger golden / gilded, modifier kept): apple 8 / 16 (0.3), carrot 6 / 12 (0.6), melon slice
4 / 8, sweet and glow berries 4 / 8 (0.1), chorus fruit 8 / 16, baked potato 10 / 20, beetroot 2 / 4, dried kelp
2 / 4, cooked beef and porkchop 16 / 20 (0.8), mutton and salmon 12 / 20 (0.8), chicken 12 / 20, rabbit, cod and
bread 10 / 20 (0.6), cookie 4 / 8 (0.1), pumpkin pie 16 / 20 (0.3), mushroom stew and beetroot soup 12 / 20 (0.6),
rabbit stew 20 / 20, honey bottle 12 / 20 (0.1). Before: every golden 4 hunger / 1.2, every enchanted 8 / 8.0.
The generated cans of these foods copy the catalog values: `tools/gen_can_types.py` needs a `/dumpfood` after the
restart first (it reads `kubejs/exported/food/catalog.json`); not rerun yet (its `--check` on 2026-11-03 already
showed 5 other members' scripts pending from parallel work).

### 3.7 Gilded names (owner 2026-11-02, built 2026-11-03)
Client lang `kubejs/assets/creativedelight/kubejs/goldenfood/names.js`: every item_dict id with an
`enchanted_golden_` path is "Gilded" + the rest in title case (Golden Food's 24, the apple, Immersive Weathering's
moss clump), their cans "Canned Gilded ...", their incomplete items "Incomplete Gilded ...". Ids unchanged. Canned
Goods' own `canned_enchanted_apples` ("Canned Notch Apples") is not renamed.

### 3.8 Crates (TODO `food/types-and-tuning #17`, not built)
Only Quark's golden apple and golden carrot crates exist. The bases that have a crate or sack are beetroot (Farmer's
Delight, Quark), sweet berries and glow berries (Quark sacks), chorus fruit (Ender's / End's Delight crates). Open:
which foods (brief recommends six raw-produce golden foods), crate or sack form, and the textures (art; Quark's golden
crate texture is Quark's, not ours to copy).

## 4. Pack code naming Golden Food (unchanged, still valid)
`item_dict.js` (45 ids), CD `kubejs/data/creativedelight/kubejs/goldenfood/placeable.js` and `food_types.js`
(`requires: goldenfood`), CD startup `goldenfood/can_types.js`, `can_servings.js`, `compost_rules.js`,
`tradetags.js` (`goldenfood:golden_*`), `blocktags.js` (cake regex), creativeworldgen `loottags.js`, `05_tuning.js`,
`07_rules.js`, moonlight soft fluids for the six stews, the coaster model. All address ids and the mod id, which are
unchanged. No pack code names an upstream class.

## 5. Not matched / differences
- Upstream's log-only listeners (client setup, common setup, server starting) are not carried.
- The enchantment, the anvil recipe and the village chest books are removed by owner decision (3.4), not matched.
- Nothing else known. Old worlds: same ids; the item classes differ, but no item or block stores class-specific NBT.

## 6. Verification (2026-11-02, offline)
- `./gradlew :portedGolden:build --offline`: passes (includes `checkNoUpstreamResources`).
- `python3 development/creativeDelight/members/golden/tools/check_donor.py`: 0 problems. 47 goldenfood + 3 minecraft
  items against the dump's 47 and the donor's models and lang; 2 blocks, `bites` 0 to 6 against the blockstates; no
  enchantment registered and `goldenfood:gf_enchantment` in `removed.json`; 24 `goldenEnchant` assemblies (23 in
  goldenfood/golden.js + the apple), each with a `SEQUENCED_ASSEMBLIES` row; every `goldenfood:` id in the served
  donor data exists; the built jar (23 entries) and the one nested in `creativedelight-1.0.0.jar` have no `assets/` or
  `data/`, no `net/ggwpgaming/` class, no donor bytes.
- Second pass: `./gradlew :creativeDelight:build --offline` passes; `node --check` on `goldenfood/golden.js` and
  `transition_items.js`; `python3 tools/gen_loot_config.py` rerun (village overrides gone).
- `tools/build_deploy.py` reads the pin through the new `UPSTREAM_DONORS` row (`goldenfood_upstream`).
- Not booted (another game instance was running). Live checks: section 8.

## 7. Open questions for the owner
- Q1: answered 2026-11-02, changed 2026-11-03: no candle on the golden cakes until the full candle integration with every cake, TODO `baking/baked-goods #38`.
- Q2: answered 2026-11-02: the enchantment is removed; enchanted golden foods by sequenced assembly (3.4).

## 8. Tests

- **r1103 2026-11-03** (scratch server; verdicts in `perf/runs/r1103/` FINAL.md and the slice judged.md): PASS SQ-GLD-01o, SQ-GLD2-03, SQ-GLD-01, -02, -04, -05, -06, -07, -08, -11, -12, -13, -10a, -10b, SQ-GLD2-01, -02, SQ-GLD-14, -09; FAIL SQ-GLD-03: golden honey / milk add an unknown 280 t Haste, Speed II, Strength, Luck, Regeneration bundle that golden milk does not clear; it comes from a use-finish event handler, not the item (`GoldenItems.java`; any `LivingEntityUseItemEvent.Finish` handler).
Scratch queue section "SQ-GLD Golden Food donor rebuild": SQ-GLD-01 to -05, -07, -09, -10, CQ-GLD-01, -02; SQ-GLD-06
and SQ-GLD-08 rewritten for the removal; SQ-GLD-11 to -14 the assemblies and the removed id.
Scratch queue section SQ-CDJ7 (2026-11-03): SQ-GLD2-01 (nugget recipes), -02 (gilded assembly), -03 (values), -05
(gilded cans, after `/dumpfood` and `gen_can_types.py`); CQ-GLD2-04 (gilded names, client).
