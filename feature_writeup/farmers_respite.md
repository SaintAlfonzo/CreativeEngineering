# Farmer's Respite 2.1.2 against the installed 2.0

**BUILT 2026-10-25 (Creative Delight 1.0.0 + patcherMod 1.1.03, installed in `mods/`); Shipped in 1.1.03 (deployed to server and players 2026-10-26); scratch (deep scan 2026-11-02): SQ-RSP-01 to -04, -09 to -11, -13 PASSED (r1101, r1102b), -05 to -07, -12, -15 open (probe could not load the Kettle; pot drop unproven), -08 needs a player, -14 not run; CQ-RSP-01 and RSP- rows not run in game.** Installed: `FarmersRespite-1.19-2.0.jar` (`mods/`, `server_mods/`, `development/patcherMod/libs/`), 1.19.2.
Evaluated 2026-10-25 against `devmods/checkNewerVersionForContent/farmersrespite-1.20.1-2.1.2.jar` (1.20.1 / Forge, mods.toml
says `1.20.1-2.1`), method in [devmods_evaluation.md](devmods_evaluation.md) section 1. Both jars unpacked in `DecompiledMods/`.
Plan row: `largecontent/mod-additions #21` in [todo/TODO.md](todo/TODO.md).

**Licence:** All Rights Reserved (owner-supplied 2026-10-25; both jars' `mods.toml` say MIT). Do not port 2.1 code or art; data facts
can be rewritten in our own scripts.

**Result in one line:** no new item or block. The pack patches the 2.0 Kettle into a 4-level fluid tank (one unit per brew, one item out) with
drink fluids, GUI transfer boxes, pipes, right-click fill / drain and Create filling / emptying for every drink, plus a potted wild tea bush
fix, two cake-slice effect changes and a death-message fix (section 2).

## 1. How it was read

- `python3 tools/jar_diff.py` (150 files added, 83 removed, 237 changed). Lang en_us **+34 / -2**: 30 `fluid_type` names (one per drink),
  `mB`, and the 1.20 renames of the creative tab and the Wither Roots death message. No new item or block model; only the two
  Kettle GUI textures change.
- Recipes, loot tables, tags and the biome modifier compared semantically (`group`, `recipe_book_tab`, wrappers dropped):
  **0 recipes outside `brewing/` and 0 loot tables change in content.** The 30 `brewing/` recipes are replaced by 30
  fluid-based ones plus 93 new filling / emptying / pouring recipes.
- Classes decompiled with Vineflower (`~/.cache/ce-tools/vineflower-1.10.1.jar`, `-only=umpaz/farmersrespite/common`), diffed
  with `diff -w`. Food values parsed to ticks (2.1 writes durations as `ticksFromMinutesDuration` / `ticksFromSecondsDuration`).

1.20 share: damage type as data, creative tab API, `MapColor`, worldgen moved from code (`FRGeneration`, config
`teaBushChance` / `coffeeBushChance`, both removed) to datapack JSON, advancements (dropped by the pack).

## 2. Decisions (owner, 2026-10-25)

The owner's notes replace the earlier "do not port" proposal. The pack keeps the installed 2.0 jar and **patches** its Kettle (Creative
Delight `farmersrespite` compartment, next to the three Remainder mixins) to take the useful parts of 2.1's fluid design without
2.1's 4-bottle yield. All code and art are our own (licence above); only data facts from 2.1 (which drinks, bottle <-> 250 mB) are reused.
Items marked **(detail)** are my reading of the notes where they were short; correct them before the build.

### 2.1 The patched Kettle

| # | Decision |
|---|---|
| K1 | **Tank size:** the Kettle's fluid level goes from 0-3 to **0-4** (`water_level` max 4). One level = one bottle = 250 mB, so the tank is 1000 mB like 2.1. Placed 2.0 Kettles keep their 0-3 level and stay valid |
| K2 | **Brew unchanged in size:** a brew uses **one fluid unit** (one level) and makes **one item** in the display slot, as in 2.0. The 4x yield is not taken |
| K3 | **Any usable fluid:** the tank accepts water and every fluid that is the `base` of some Kettle recipe (the teas, long and strong teas, the other drink fluids). One fluid type at a time; a different fluid is refused until the tank is empty. The stored fluid is saved in the block entity **(detail: new NBT key; level stays the blockstate property so models and the 2.0 water texture keep working, tinted by the fluid)** |
| K4 | **Two GUI boxes** (new slots beside the container slot) to move fluid with bottles: a **fill box** and an **empty box**. Clicking or placing a valid item in one box converts it and puts the converted item in the other box: a full drink bottle in the fill box pours one unit into the tank and the empty glass bottle appears in the other box; an empty glass bottle in the empty box takes one unit and the full drink bottle appears in the other box. The transfer **keeps its direction** (keeps converting the stack, one unit per step, while the tank has room or fluid) until a valid item is put in the other box, which flips it. The boxes accept only valid items (a drink bottle of a fluid the tank can hold, a glass bottle), so **shift-click is safe**. Both boxes can hold items at once (owner 2026-10-25); the converted item needs room in the other box, else the transfer pauses |
| K5 | **Long and strong teas:** with the tea in the tank, the Kettle brews the long / strong version from 1 unit of the base tea plus the milk or honey item, one result per brew (2.1 does the same with 4). The tank input in K4 is what makes this reachable by hand |
| K6 | **Pumps:** the Kettle exposes a fluid handler on every side, so pipes and pumps fill it (valid fluids only, K3) and drain it. Pumps fill **in increments of 250 mB** (owner 2026-10-25): the handler accepts the largest multiple of 250 it is offered that fits, so the tank always holds whole units and level = stored mB / 250 exactly. Draining likewise moves whole units |
| K7 | **Right-click fill and drain:** a filled fluid container right-clicked on the Kettle pours into the tank (water bucket fills the tank, water bottle or drink bottle adds one level); an empty glass bottle right-clicked takes one level as the drink bottle; an empty bucket takes a full tank of a bucketable fluid. Same rules as K3 and K4 (valid fluid, same type, room) |
| K13 | **Bottles (owner 2026-10-25):** a brew takes its glass bottle from the **plating slot** (the container slot of 2.0). Extra bottles in the input slots are not used; they move to the **output (remainder) buffer** ([remainder_buffer.md](remainder_buffer.md)) like other leftover containers |
| K8 | **Recipe format:** `farmersrespite:brewing` is reimplemented to match 2.1's shape: a `base` fluid (fluid id and amount, 250 mB per brew) instead of `needwater`, then `ingredients`, `container`, `cookingtime`, `experience`, and a **single item** `result`. The 30 stock recipes are rewritten in the new format by data (water base for the plain teas, the tea fluid as base for long / strong, the juice or milk drink fluid as base for the recipes that took a drink item). `NCH.frBrewing` gets a `base` option (default water; `needWater: false` is replaced by an explicit base or none); the pack's own `frBrewing` calls (gambler's tea, berry matcha latte, the drinks #7 moves) are migrated in the same change |
| K9 | **Drink fluids:** every drink the Kettle can brew or hold gets a fluid, built on the pack's existing drink-fluid chain (the potion-like ciders, JEI drink-fluid effect lines, [item_merges.md](item_merges.md) 2.6) rather than a second registry. **All the coffee and tea is implemented in the Kettle (owner 2026-10-25):** every Farmer's Respite tea and drink plus every Rustic Delight coffee and the Fruits Delight teas get a fluid and a Kettle `base` recipe from the start; the full list is the first build step |
| K10 | **Filling and emptying for every drink:** `create:filling` (glass bottle + 250 mB -> drink) and `create:emptying` (drink -> glass bottle + 250 mB) for each drink fluid, through an NCH helper, not hand-written events |
| K11 | **Tooltip and WTHIT:** the Kettle item tooltip lists the stored fluid and level (replaces the stored-drink line in `KettleMealTooltipMixin`); the WTHIT block line shows the fluid name and "n / 4" next to the brew progress and the existing "Holding" leftover lines ([wthit_lines.md](wthit_lines.md) 2.9) |
| K12 | **Water cup filling:** Create filling and emptying for `miners_delight:water_cup` (cup <-> 250 mB water). The earlier "skip, no cup" no longer applies: the Miner's Delight backport has the cup (`MDItems.WATER_CUP`) |

Interplay with the open plan: [TODO.md](todo/TODO.md) `food/drinks #7` (6 ingredient slots) and `#8` (juice / tea rule) now build on
this tank instead of the item-slot design; the plan row needs a pass once K1 to K12 are built. Teas in both Kettle and pot stay "Kettle is
2x as efficient" through K2 plus the cheaper ingredient count, not through yield.

### 2.2 Wild Tea Bush in a flower pot

2.0 registers `potted_wild_tea_bush` but never calls `FlowerPotBlock.addPlant`, so the bush does nothing on a pot. **Build:** a patcherMod
common-setup call registering the tea bush (coffee is purged). Player-visible: changelog bullet.

### 2.3 Food effect changes (take 2.1's values)

| Food | 2.0 | New |
|---|---|---|
| Slice of Coffee Cake | 2 / 0.1, Speed 20 s, Caffeinated 20 s | 2 / 0.1, Caffeinated 30 s (no Speed) |
| Rose Hip Pie Slice | 3 / 0.3, Speed 20 s, Regeneration 20 s | 3 / 0.3, Regeneration 20 s (no Speed) |

Through the pack's food-tuning script (find the existing cake-slice entries first). Build check: Coffee Cake
survived the coffee purge (`baking.js` still names `coffee_cake` and `rose_hip_pie`). Player-visible: changelog bullets.

### 2.4 Death message

Lang override on `death.attack.farmersrespite.witherRoots`: "while" (typo fix). Add `death.attack.farmersrespite.witherRoots.player`
(2.0 has no such key and vanilla uses it when the victim has a kill credit, so that death shows the raw key today): "%1$s wasn't careful
while gardening whilst fighting %2$s". Resource-pack lang, so it follows the lang priority rules ([resource-pack-lang-priority] memory).

### 2.5 Left as is

| Change | Why |
|---|---|
| Wild tea worldgen change (swamp tag, 1 in 25) | The pack places tea itself (`wild_crops.json`, [crops.md](crops.md) 2.8) |
| Coffee bush worldgen | FR coffee purged into Rustic Delight |
| `damage_type` data, creative tab, tag key API, `LevelReader` signatures, advancements | 1.20 only |
| Other recipes, loot tables, red dye from coffee berries | Unchanged in 2.1 |
| Long / strong teas sharing the base tea's name and texture | `food/drinks #2` stays the pack's own job |

## 3. Build order

1. Drink fluid list (K9) and the fluid-capable Kettle block entity (K1, K3, K6) with the `base` recipe type (K8); migrate the 30 recipes and `frBrewing` callers.
2. GUI boxes (K4) and right-click (K7).
3. Create filling / emptying helper and recipes (K10, K12).
4. Tooltip and WTHIT (K11).
5. Pot fix (2.2), effect changes (2.3), death message (2.4): small, independent, can ship first.

Build rules: Creative Delight `farmersrespite` compartment for the Kettle patch (own mixins and menu code, nothing copied); recipes through
NCH (extend the helper); items and fluids defined once in the dicts; hand-edited configs copied to `configs_edited/`. Needs a full restart; a
patcherMod rebuild if the helpers change.

## 4. Details settled (owner, 2026-10-25)

1. K4: both boxes can hold items at once; direction stays latched until the other box gets a valid item.
2. K6: pumps fill in whole 250 mB units when they can.
3. K13: a brew uses a bottle from the plating slot; spare bottles in the input slots go to the output buffer.
4. K9: all coffee and tea are brewed in the Kettle from the start.

The drink list and recipes are in section 5 (decisions 3, 4); the Rustic coffees keep all their own recipes (owner 2026-10-25), the Kettle recipe is added.

## 5. Build (2026-10-25)

All of section 2 is built. Where each piece lives:

| Part | Where |
|---|---|
| Tank, brewing, boxes, pipes (K1-K6, K13) | Creative Delight `mixin/farmersrespite/KettleTankMixin` (block entity), `KettleBlockMixin` (`water_level` 0-4, right-click, placing a kept tank), `KettleMenuTankMixin`, `KettleDispenseMixin`; rules in `compat/farmersrespite/KettleTank` |
| Recipe format (K8) | `KettleRecipeSerializerMixin` + `compat/farmersrespite/KettleBases`; NCH `frBrewing` option `base` (`needWater` gone, warns) |
| Which bottles pour (K3, K4, K7) | `compat/farmersrespite/KettleDrinks`: read from Create filling / emptying recipes (glass bottle <-> 250 mB, one item, untagged fluid); water bottle hard-wired. A drink with no such pair can't go in the tank |
| Recipes (K8, K9) | `kubejs/data/creativedelight/kubejs/farmersrespite/kettle.js` (48 Kettle recipes); gambler's tea is in `rusticdelight/coffee_merge.js` (the coffee merge, item_merges.md 1.1) |
| Fluids (K9) | 10 new pack fluids in `development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/fluids.js` (`f.milk_coffee` ... `f.berry_matcha_latte`), tags in `fluidtags.js`; Central Kitchen's tea, cider, cocoa, melon and coffee fluids reused |
| Spout / drain (K10, K12) | NCH `createBottle` (filling + emptying), called in `kettle.js` and `miners_delight/recipes.js` (water cup). BROKEN: every `creativedelight:createfilling/*` fails to load (`Error parsing recipe`, fluid `minecraft:empty`; 12 in the owner's `logs/kubejs/server.log` 2026-10-29, also the 2026-10-28 scratch boots), so the Spout half is missing; the emptying half was broken the same way (`latest.log` 2026-10-31: JEI `Found a broken recipe ... createemptying/coffee`, `Can't modify the empty stack`). **FIXED 2026-11-01 (patcherMod 1.1.07 in `mods/` 2026-11-02, not deployed; changelog bullet in unreleased; TODO `food/drinks #12`)**: cause was not the startup scope but this Rhino build (interpreter, 1902.2.3-284) returning 0 for `nullish ?? <numeric literal>`, so `opts.mb ?? 250` gave 0 mB and `Fluid.of(fluid, 0)` the empty stack (checked in a headless Rhino run: `undefined ?? 250` -> 0, `undefined ?? 'x'` -> 'x'). New `NCH._or(v, d)`; every numeric `?? N` default in `neoCraftHelper.js` (27) goes through it. Test SQ-RSP-09 / RSP-07 |
| GUI, tooltip, WTHIT, JEI (K11) | `KettleScreenTankMixin` + `compat/farmersrespite/client/KettleTankRender`; `KettleItemTooltipMixin`; `compat/wthit/WthitKettleTank`; `KettleJeiBaseMixin` (base fluid slot in the bar) |
| Pot, slices, death text (2.2-2.4) | patcherMod `compat/RespitePottedTeaBush`; `startup_scripts/cooking/food_values.js`; `kubejs/assets/farmersrespite/lang/en_us.json` |

Decisions taken during the build (correct them if wrong):

1. **Brew time stays 10 s** (owner, 2026-11-01). 2.0 reads `brewingtime`; every recipe (Respite's, NCH's) writes
   `cookingtime`, so every brew took the 200-tick default. The serializer now reads `cookingtime` (2.1's key), and
   every rewritten recipe says 200 (2.1's 2400 would make one bottle take 2 minutes). **Bug, fixed 2026-11-01:** the
   1.1.04 jar played on 10-31 wrote `opts.time ?? 200`, which this Rhino evaluates to 0 (the `??` bug of K10), so
   every recipe had `cookingtime: 0` and brewed in 1 tick. `NCH._or` (in patcherMod 1.1.06 and later) fixes it.
2. **Recipes that took milk** (hot cocoa's milk-bottle container, Delightful's matcha latte and builder's tea, End's
   chorus fruit milk tea) use milk (`minecraft:milk`) as their base and bottle in glass.
3. **Rustic coffees:** Coffee = water + 2 roasted beans; Milk and Dark Coffee build on Coffee in the tank (+ milk,
   + 2 beans); Honey, Chocolate, Syrup, Pumpkin, Cherry Blossom build on Milk Coffee (+ the flavour). Rustic Coffee uses
   Central Kitchen's coffee fluid (Respite's coffee is purged, so it was free).
4. **Fruits Delight teas:** two-fruit teas = leaves + 1 fruit; Hawberry and Lychee Cherry (four fruit in the pot) = 2
   fruit, no leaves / spore blossom (the Kettle has two slots).
5. **The Kettle item keeps its tank** (type and level) when broken, as K11's tooltip implies; 2.0 lost the water.
6. **Box speed:** one bottle per 4 ticks. A pointer right of a box marks the latched direction.
7. **Builder's tea** keeps Central Kitchen's 2 bottles per brew (on water). Azalea tea is not live (no Ecologics) and
   gets no recipe.

## 6. Tests

- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): PASS SQ-RSP-02, -03, -04, -10, -11, -13; OPEN (no verdict, rerun row written) SQ-RSP-12; rerun pending (batch fault) SQ-RSP-05, -06, -07, -15; not run SQ-RSP-14.

Scratch (deep scan 2026-11-02): SQ-RSP-01 PASS (r1101 boot), -09 PASS (r1101), -02, -03, -04, -10, -11, -13 PASS r1102b (`perf/runs/r1102b/s2/judged.md`; -02 now 84 recipes, not 48). Open: -05, -06, -07, -15 (`item replace block` cannot load the Kettle: probe fault, rerun with a bot), -12 (flower pot drop not seen; bot too close), -08 (needs a player), -14 (old-jar boot failed beside CD). CQ-RSP-01 (client) not run. In game:

- RSP-01 Kettle takes water by bucket (level 4) and refuses a tea on top of water; empties to 4 water bottles.
- RSP-02 Pour 2 Black Tea, then honey + plating bottle: Strong Black Tea, level 1, honey bottle to a leftover slot.
- RSP-03 Long Black Tea from black tea in the tank + milk bottle.
- RSP-04 Fill box: 4 Black Tea pour in one by one, 4 empties appear in the empty box; put glass bottles in the empty box: direction flips, bottles fill from the tank; shift-click a tea goes to the fill box; a stone block can't go in either box.
- RSP-05 Glass bottle placed in an ingredient slot moves to a leftover slot; the brew still plates from the plating slot.
- RSP-06 Create pump fills in 250 mB steps (a 100 mB offer is refused, 600 mB gives 500); a pump pulls it empty from below and each side.
- RSP-07 Spout and Item Drain: each new drink fluid both ways; Water Cup <-> Copper Cup + 250 mB water.
- RSP-08 Right-click: water bottle +1, glass bottle takes one as the drink, empty bucket takes a full tank of water.
- RSP-09 GUI bar: four steps in the fluid's colour, tooltip "Black Tea: 2 / 4" and mB.
- RSP-10 Kettle item tooltip "Tank: Water, 3 / 4"; placing it back gives level 3.
- RSP-11 WTHIT "Tank: Black Tea 2 / 4" beside the Holding line.
- RSP-12 JEI Kettle page shows the base fluid in the bar; Rustic coffees and the five Fruits Delight teas listed.
- RSP-13 Wild Tea Bush goes into a flower pot and back out.
- RSP-14 Slice of Coffee Cake: Caffeinated 0:30, no Speed; Rose Hip Pie Slice: Regeneration only.
- RSP-15 Wither Roots death message: "while gardening", and the "whilst fighting" version when a mob hit you.
- RSP-16 A world with a 2.0 Kettle at 3 water: unchanged and brews.
- RSP-17 A heated Kettle with water and 2 black tea leaves takes 10 s (not 1 tick) to make one Black Tea.

## Owner drink rules (2026-11-02)

Coffee in the Kettle builds the same milk and flavoured coffees as the pot, step by step or straight from the coffee tank; new fruit teas and the heated-mixer variants follow [creative_delight_drinks.md](creative_delight_drinks.md). Nothing built; TODO `food/drinks #15`.
