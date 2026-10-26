# Brief: food / types-and-tuning

Written 2026-10-21 from [TODO](../todo/TODO.md) `# food`, rows with Sub `types-and-tuning`. Reference writeup:
[food.md](../food.md) (sections 2.3, 3.5, 3.8, 4.2, 4.3) and the generated [food_catalog.md](../food_catalog.md).
Mod facts marked **(jar)** were read from the decompiled jars on 2026-10-21 (Canned Goods 1.19.2-3, Golden Foods
2.0.0, Spice of Life Apple Pie 2.2.0, Farmer's Delight 1.2.4, KubeJS 1902.6.2-build.73, Delightful 3.4.1, Abnormals
Delight 4.1.2, Untitled Duck Mod 0.6.1, the Brewin' backport sources in `development/backportedBrewin`). Values marked
**(dump)** come from the 2026-10-20 `/dumpfood` via food_catalog.md. `kubejs/exported/` is not in the repo: any
step that runs `tools/food_catalog.py` or `tools/gen_can_types.py` needs a fresh `/dumpfood` first.

## Scope and order

Open rows: #1, #3, #5, #6, #8, #10, #12 (Planned), #2, #4 (Bug), #9 (In Progress). #11 is Testing (table at the end).

**State on 2026-10-21 that changes the rows:**

- `setFoodProperties` works in both forms (the old "ambiguous overload" risk in food.md 3.5 is gone): cans.js uses
  the consumer form, food_values.js and alcohol_drinks.js pass a built `FoodProperties`; the 10-20 dump shows the
  results (Canned Steak 16 / 0.8, Cantaloupe Slice 3 / 0.3, alcohol 1 / 0).
- The consumer form starts from the item's own properties **(jar)**: `ItemKJS.kjs$setFoodProperties(Consumer)` does
  `new FoodBuilder(existing)`, which copies meat, alwaysEdible, fast and effects. So cans.js's re-valued Canned Goods
  cans keep Canned Goods' meat flag: the dump still shows Canned Cookies `MF`, Canned Cooked Cod / Cooked Salmon /
  Honey Bottles `M` (row #2).
- Half of row #2 is already done by `tools/gen_can_types.py`: 20 Canned Goods cans are `existing` entries in
  can_types.json and get parity values at startup. 18 Canned Goods food cans and the 3 soda cans still carry the
  mod's values; 3 of them can be obtained (row #2).
- The generated enchanted golden cans copy Golden Foods' broken modifier: 2 hunger / **8.0** per serving, 10
  servings (can_types.json `canned_enchanted_golden_*`). Row #3 fixes the source; rerun the generator after it.
- Farmer's Respite is installed now (since 2026-10-02), so 10 of Spice of Life's default complexity entries apply;
  only `largemeals` is missing (row #4).
- `#forge:eggs` has 12 items, not 10 (Untitled Duck Mod adds duck and goose eggs) **(jar)**; the Brewin' 4.5.0 port
  already uses `#forge:eggs` in the omelet and egg grog (row #6).

**Build groups and order:**

1. **Quick fixes, no export needed** (one session, one restart):
   #4 SoL config file -> #2 Canned Goods leftovers (cans.js flags, hide / re-value, trade swap) -> #3 golden values
   -> #6 egg migration -> #10 unused meats -> #9 culture food values.
2. **Export round:** `/dumpfood` on the restarted game, then `gen_can_types.py` (picks up #3's golden values and
   #9's cultures), then `food_catalog.py`.
3. **#1 tuning system**: A tool report -> B startup apply -> C SoL complexity map. #8 (soups) is the first content
   pass through it; then the rest of the types.
4. **#12 SoL pace and benefits** on top of #1's complexities (it changes the same toml as #4).
5. **#5 placed pies and cakes** (patcherMod), any time after #1C (it needs complexities for non-food block items).

Rows of other subfamilies that depend on this: `cans/canning #1`, `#3` (can values follow #1), `cans/canning #4`
(effect passthrough, waits on D6), `dishes/meals #9` (gold food cost, look again after #3).

---

## types-and-tuning #4: Spice of Life config
Status Bug (the pack config is in place: `defaultconfigs/solapplepie-server.toml` 2026-10-22, same file as `configs_edited/server_config/`, new-world values checked on the scratch server by SQ-CF-01; the speed benefit stays open for the tier rework) / Work config / `food/types-and-tuning #4`

### Goal
One pack-level Spice of Life server config that every new world gets, with the broken benefit fixed.

### Now
- No `solapplepie-server.toml` anywhere in the repo (`config/` has only `solapplepie-client.toml`). The mod
  registers it as `ModConfig.Type.SERVER` **(jar)**, so it lives in `saves/<world>/serverconfig/` and new worlds get
  the mod defaults.
- `tools/build_deploy.py` lines 48-49, 103, 386-389: everything in `configs_edited/server_config/` is merged into
  `defaultconfigs/` on deploy (configs_edited wins). Today that folder holds seasonhud, sereneseasons, unlitcampfire.
- Mod defaults **(jar, `SOLSweetPotatoConfig.Server`)**: queueSize 32, startDecay 0, endDecay 32,
  minContribution 0, defaultContribution 1.0, minFoodsToActivate 0, resetOnDeath false,
  limitProgressionToSurvival false, shouldForbiddenCount true, blacklist / whitelist empty (glob patterns).
  17 thresholds `2,5,7,10,15,20,25,30,35,40,45,50,60,70,80,90,100`, 17 benefit strings (11th:
  `attribute,generic.max_health,1;attribute,generic.speed,2`).
- `attribute,generic.speed,2`: `AttributeBenefit.createAttribute` looks up `minecraft:generic.speed`, gets null and
  marks the benefit invalid, silently **(jar)**. The +1 max health on the same threshold still applies (separate
  benefit). Real name: `generic.movement_speed` (base 0.1, so `,2` would be 20x speed: never use it as is).
- Complexity list defaults: 4 `largemeals:*` (not installed: `ComplexityParser` warns `Item is not food`), 10
  `farmersrespite:*` (installed, now live: 6 teas at 3, two slices at 4, blazing chili 5), vanilla cooked porkchop / beef 2, golden carrot 2,
  golden apple 4, notch apple 10, FD cake slice and three pie slices 4.
- Workspace world was hand-edited (food.md 2.3: queue 1000, minFoods 10, resetOnDeath, 31 benefits for 17
  thresholds). Its file isn't in the repo.

### Build
1. Write `configs_edited/server_config/solapplepie-server.toml` with the full default layout (generate it by
   starting a fresh single-player world once and copying `saves/<new world>/serverconfig/solapplepie-server.toml`, so
   the section names and comments are the mod's own; the server spec pushes `[Benefits]`, `[Filtering]`,
   `[Miscellaneous]`, `[Advanced]`, `[Complexity]` **(jar)**).
2. Change only: the 11th benefit -> `attribute,generic.max_health,1`; drop the 4 `largemeals` complexity lines. Keep
   everything else at default until #1 / #12 decide values (the file is where #12 lands).
3. Existing worlds: copy the file into each world's `serverconfig/` by hand (WorldC, Workspace if wanted, and the
   dedicated server world). defaultconfigs only seeds worlds that have no copy yet. Note it in the deploy notes.
4. food.md 2.3 "Config is per world" and the at-a-glance "Knobs today" line: point at the new file.

### Ids and data
Config keys verified in the jar: `thresholds`, `benefitsUnparsed`, `complexityUnparsed`, `queueSize`, `startDecay`,
`endDecay`, `minContribution`, `defaultContribution`, `minFoodsToActivate`, `resetOnDeath`,
`limitProgressionToSurvival`, `shouldForbiddenCount`, `blacklist`, `whitelist`. Section names: from a generated file.

### Decisions
- Keep Workspace's resetOnDeath / minFoods 10? Recommend the defaults (false / 0) pack-wide for now; #12 revisits.

### Gotchas
- A server config is synced to clients on login (`ConfigHandler.syncConfig`), so clients need nothing.
- The benefit list must have exactly one entry per threshold or the extra entries are ignored.

### Tests
- FO-29 new world: `serverconfig/solapplepie-server.toml` equals the pack file; the log has no
  `Item is not food: largemeals` lines; the food book (Benefits page) lists +1 max health at the 11th threshold.

---

## types-and-tuning #2: Canned Goods data broken
Status Testing (fixed 2026-10-21, see the TODO row) / Work data / `food/types-and-tuning #2`

### Goal
No can in play with Canned Goods' broken values (modifiers 1 to 42, crossed ids, meat flags, amplified effects).

### Now
- **Re-valued (built):** 20 Canned Goods cans are `existing: true` in
  [can_types.json](../../kubejs/generation_scripts/food/can_types.json) (tropical fish, pufferfish, poisonous potato,
  spider eye, rotten flesh, chorus, cookies, baked potato, dried kelp, beef, pork, chicken, cooked mutton, cooked
  rabbit, fish, cooked salmon, rabbit stew, mushrooms, bread, honey bottles) and get their values in
  [cans.js](../../development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/cans.js) lines 105-120 (`ceCanFood`, lines 74-81).
- **Bug in that override:** `ceCanFood` only *adds* flags (`if (c.meat) f.meat()`), and the consumer form starts from
  Canned Goods' own properties, so Canned Goods' meat flag stays on `canned_cookies`, `canned_fish`,
  `canned_cooked_salmon`, `canned_honey_bottles` (dump: Canned Cookies `MF`, the others `M`; can_types says
  `meat: false`).
- **Not re-valued** (Canned Goods values **(jar)**, servings from can_servings.json):

  | Can (`canned_goods:`) | Food h / mod | Code effect per serving | Source in play |
  |---|---|---|---|
  | `canned_food` | 12 / 28 | none | **butcher level 3 trade, 3 emeralds** (`CannedGoodsModTrades`) |
  | `canned_golden_apples` | 8 / 10, meat | Regeneration III 5 s, Absorption II 120 s | **wandering trader** ([tradetags.js](../../kubejs/server_scripts/tags/tradetags.js) line 16, [wandering_trades.js](../../kubejs/server_scripts/economy/wandering_trades.js) line 62) |
  | `canned_golden_carrot` ("Canned Golden Carrots") | 12 / 28 | none | **wandering trader** (same tag) |
  | `canned_enchanted_apples` | 8 / 10, meat | Regen III 20 s, Resistance II, Fire Res II 300 s, Absorption V 120 s | none |
  | `canned_suspicious_stew` | 26 / 42, meat | 9 effects 11 s incl. **Saturation II** | none |
  | `canned_cake` 28 / 4, `canned_carrots` ("Canned Pumpkin Pies") 16 / 8, `canned_pumpkin_pie` ("Canned Golden Carrots") 12 / 28, `canned_beetroot_soup` ("Canned Bread") 12 / 14, `canned_apples` 8 / 5, `canned_beets` 2 / 3, `canned_potatos` 2 / 1, `canned_melon_slice` 4 / 2, `canned_sweet_berries` 4 / 3, `canned_glow_berries` 4 / 1 (+ Glowing 3 s) | | | none (recipes removed, canning.js lines 92-104) |
  | `hot_hot_hotpot` | 3 / 3, always | Regen II, Speed II, Fire Res II 75 s | none (recipe removed) |
  | `soda`, `reef_cola`, `oculemonade` | 0 / 0, always | Speed II 30 s; Water Breathing II 30 s; **Speed II** (Oculemonade calls `SodaPlayerFinishesUsingItemProcedure`) | crafting (kept) |

  The pack has its own replacements for every no-source can (`creativeengineering:canned_apple`, `canned_beetroot`,
  `canned_carrot`, `canned_potato`, `canned_melon`, `canned_sweet_berry`, `canned_glow_berry`, `canned_ce_beetroot_soup`,
  `canned_ce_golden_carrot`, `canned_golden_apple`, `canned_enchanted_golden_apple`, `canned_pumpkin_pie_filling`).
  None of the leftovers is hidden: they still show in JEI and the creative tab.
- Crossed names only matter for items that stay visible (lang `item.canned_goods.canned_carrots` = "Canned Pumpkin
  Pies", `canned_pumpkin_pie` = "Canned Golden Carrots", `canned_beetroot_soup` = "Canned Bread").

### Build
1. **cans.js `ceCanFood`**: set every flag explicitly so the existing cans lose Canned Goods' ones:
   ```js
   f.hunger(c.hunger).saturation(c.modifier)
   f.meat(!!c.meat); f.fastToEat(!!c.fast); f.alwaysEdible(!!c.always)
   ```
   (`FoodBuilder.meat(boolean)` etc. exist **(jar)**.) Harmless for the generated cans (fresh builders).
2. **Hide the 13 no-source cans** by adding their item_dict keys to `global.removedItems` in
   [removed_items.js](../../kubejs/startup_scripts/_lib/removed_items.js) (the pack's removal path: JEI hide in
   `client_scripts/jeichanges.js` line 5, tags stripped in `server_scripts/_lib/recipe_scan.js` line 50, red "REMOVED
   ITEM" tooltip). Keys: `canned_enchanted_apples`, `canned_suspicious_stew`, `canned_carrots`, `canned_pumpkin_pie`,
   `canned_beetroot_soup`, `canned_apples`, `canned_beets`, `canned_potatos`, `canned_melon_slice`,
   `canned_sweet_berries`, `canned_glow_berries`, `hot_hot_hotpot`, and `canned_cake` **unless** `cans/canning #15`
   takes it (its brief recommends reusing `canned_cake`: then leave it out of the list). Then move those keys from
   food_types.json `types.canned` / `canned` into `excluded` with reason `"removed item (Canned Goods leftover)"` and
   drop them from can_servings.json (via `tools/gen_can_types.py`'s preserved entries, not by hand, if the tool
   rewrites the file; check its `SERVINGS` write).
3. **Wandering trader**: in tradetags.js line 16 swap `[i.canned_golden_apples, i.canned_golden_carrot]` for the pack's
   `[i.canned_golden_apple, i.canned_ce_golden_carrot]`, then hide the two Canned Goods golden cans too (step 2).
   This also settles D4 for golden cans: the pack's golden cans take effects from food properties, no code.
4. **Butcher's `canned_food`**: keep the trade (it's the only Canned Goods can a villager sells) and give it values:
   an EXPLICIT `existing` row in `tools/gen_can_types.py` (section `raw_meat`, fixed meal like the raw meat cans
   5 / 3 points, meat, 2 servings) so it is re-valued like the other 20. Alternative: hide it and remove the trade with
   MoreJS in [trades.js](../../kubejs/server_scripts/economy/trades.js) (`e.removeModdedTrades(['minecraft:butcher'], [3, 3])`
   removes every modded level-3 butcher trade, so check the others first).
5. **Soda cans**: values are right (drinks, 0 food). Oculemonade: decide (below); if it gets its own effect, a
   patcherMod mixin on `net.mcreator.cannedgoods.item.OculemonadeItem#m_5922_` (`finishUsingItem`, remap = false)
   `@Redirect` of the `SodaPlayerFinishesUsingItemProcedure.execute(Entity)` call to apply the new effect.
6. Rerun `gen_can_types.py` only if step 4 is taken (needs the export); restart.

### Ids and data
All ids above are in item_dict (lines 12143-12183) **(jar for values and effects)**. Trade: Canned Goods registers it
in `VillagerTradesEvent` for `VillagerProfession.BUTCHER`, level index 2 (= level 3), 3 emeralds, 10 uses **(jar)**.

### Decisions
- Hide vs re-value the no-source cans: recommend hide (the pack already has its own can for each; re-valuing 13
  duplicates adds JEI noise).
- Oculemonade effect: recommend Night Vision 30 s (the name is "eye lemonade"); or accept it as a lime soda.
- `canned_food`: recommend re-value (step 4).

### Gotchas
- The servings mixin (patcherMod `food/CanServings`) runs the can's own `finishUsingItem` per serving, so code effects
  apply per serving; hiding the golden Canned Goods cans removes the amplified ones from play.
- Old copies already in chests and inventories stay edible with the old values; the tooltip flags them.
- `existing` overrides need a restart (startup event).

### Tests
- FO-30: hold Canned Cookies, AppleSkin / `/dumpfood`: no meat flag (wolves refuse it); Canned Cooked Cod likewise.
- FO-31: JEI search "Canned Pumpkin Pies", "Canned Bread", "Canned Notch Apples", "Hot Hot Hotpot" -> no results;
  `/give` one -> red REMOVED tooltip.
- FO-32: a wandering trader's golden listing sells `creativeengineering:canned_golden_apple` or
  `canned_ce_golden_carrot` (10 servings), never a Canned Goods golden can.
- FO-33: butcher level 3 sells Canned Food at 5 hunger / 3 saturation points per serving (AppleSkin).

---

## types-and-tuning #3: Golden food changes vanilla values
Status Planned / Work data / `food/types-and-tuning #3` (decision D2)

### Goal
Golden Foods! stops changing vanilla values: enchanted foods 4 / 1.2 like the notch apple, golden carrot back to
vanilla.

### Now
- Golden Foods replaces three vanilla items through a `minecraft` DeferredRegister **(jar, `GFItems`)**:
  `minecraft:golden_apple` (4 / 1.2, Regen II 5 s, Absorption I 120 s, always: same as vanilla),
  `minecraft:enchanted_golden_apple` (**8 / 8.0**, Regen II 20 s, Resistance I 300 s, Fire Res I 300 s, Absorption IV
  120 s, always, **stacksTo(1)**; vanilla is 4 / 1.2, stacks to 64) and `minecraft:golden_carrot` (**4 / 1.2 + the
  golden apple's two effects + always edible**; vanilla 6 / 1.2, no effects, not always edible).
- Its 23 `goldenfood:enchanted_golden_*` foods are 8 / 8.0 with the notch effects; the 23 golden ones 4 / 1.2 with
  the golden apple effects (dump; list in food_catalog.md `golden` / `golden/enchanted`).
  `immersive_weathering:golden_moss_clump` (4 / 0.6) and `enchanted_golden_moss_clump` (6 / 0.8) are a different
  mod and already sane.
- Stack size: the pack's stack policy sets the enchanted foods to 16 (`stack_sizes.json`).
- SoL: notch apple 10 and golden carrot 2 from the config list; the 23 enchanted foods 7.02 by formula.
- Generated cans copy the source: `canned_enchanted_golden_*` are 2 / 8.0 per serving (32 saturation points),
  `canned_ce_golden_carrot` 1 / 1.2 with golden-apple effects.

### Build
1. Write the values where #1 will keep them: if #1 isn't built yet, a startup file
   `kubejs/startup_scripts/cooking/golden_values.js` on `ItemEvents.modification` (same shape as
   [food_values.js](../../kubejs/startup_scripts/cooking/food_values.js)); when #1 lands, move the entries into
   `food_tuning.json` `items` and delete the file. Sketch:
   ```js
   ItemEvents.modification(e => {
     var i = global.item
     global.foodTypes /* or read food_types.json */['golden/enchanted'].forEach(key => {
       if (key === 'enchanted_golden_moss_clump') return
       e.modify(i[key], item => item.setFoodProperties(f => { f.hunger(4); f.saturation(1.2) }))
     })
     e.modify(i.golden_carrot, item => item.setFoodProperties(f => {
       f.hunger(6); f.saturation(1.2); f.alwaysEdible(false)
       f.removeEffect(/* MobEffect */ regen); f.removeEffect(absorption)
     }))
   })
   ```
   Read the key list from food_types.json (`JsonIO.readString`, as cans.js does) instead of hard-coding 24 keys.
   `removeEffect` takes a `MobEffect` (load through `ForgeRegistries.MOB_EFFECTS`, as alcohol_drinks.js does), or
   build a fresh vanilla `FoodProperties$Builder` like alcohol_drinks.js lines 61-82.
2. SoL: leave the config values (notch apple 10, golden carrot 2) until #1C sets per-type complexities.
3. Export round, then `gen_can_types.py`: the enchanted cans become 1 / 1.2, the golden carrot can 2 / 1.2 with no
   effects (or keep effects, per the decision).
4. food.md 4.2 finding 3 -> fixed; can_parity.md 2.17 note.

### Decisions
- D2 enchanted foods: recommend 4 / 1.2 (vanilla) and keep the notch effects.
- Golden carrot: recommend fully vanilla (6 / 1.2, no effects, not always edible): it costs 8 nuggets (the pack makes
  it from rose gold nuggets, `dishes/meals #9`), a ninth of a golden apple, so golden-apple effects on it are cheap
  healing. Alternative: 6 / 1.2 and keep the effects.
- Stack size of the notch apple: leave the policy's 16.

### Gotchas
- `minecraft:golden_carrot` and `minecraft:enchanted_golden_apple` are Golden Foods' objects; `e.modify` on the
  vanilla id reaches them (one registry entry), no special handling.
- The golden cans' effects follow the source (D-B in cans.md); recheck them after this row.

### Tests
- FO-34: AppleSkin on an Enchanted Golden Apple and an Enchanted Golden Cookie: 4 hunger, saturation 9.6; Golden
  Carrot 6 hunger, no effects, can't be eaten at full hunger.

---

## types-and-tuning #6: Vanilla egg -> `#forge:eggs` in recipes
Status Planned / Work KubeJS / `food/types-and-tuning #6`

### Goal
Every food recipe that takes the vanilla egg takes any egg (cooking plan: "any egg to fried egg").

### Now (rescan of the jars and pack scripts, 2026-10-21)
`#forge:eggs` = `minecraft:egg` (FD), `minecraft:turtle_egg` (Supplementaries), `alexsmobs:crocodile_egg`,
`emu_egg`, `terrapin_egg`, 5 `quark:egg_parrot_*` (Quark Delight, optional entries), `untitledduckmod:duck_egg`,
`goose_egg` **(jar)**. `i.egg` / `i.eggs` already point at `#forge:eggs` (item_dict lines 1947-1950);
`i.minecraft_egg` is the vanilla egg (line 2243).

Recipes that still take `minecraft:egg` as an item:

| Recipe id | Type | Where | Change |
|---|---|---|---|
| `creativeengineering:smelting/...`, `smoking/...`, `campfirecooking/...` fried egg | furnace / smoker / campfire | [cooking.js](../../kubejs/server_scripts/cooking/cooking.js) lines 383-385, input `i.minecraft_egg` | edit the input to `i.eggs` |
| `creativeengineering:quiche_from_mushroom` | crafting shaped | cooking.js lines 200-202, key `e: i.minecraft_egg` | edit to `i.eggs` |
| `brewinandchewin:quiche_from_bacon` | crafting shaped | Brewin' backport `data/brewinandchewin/recipes/quiche_from_bacon.json` | replaceInput |
| `rusticdelight:cooking/fried_egg_from_cooking_oil` | `farmersdelight:cooking` | Rustic Delight jar | replaceInput (Slice & Dice's mixer copy follows) |
| `create_factory:cake_paste` | `create:mixing` | Create: Factory jar | replaceInput |
| `create_deep_dark:compacting_echo_cake_base` | `create:compacting` | Create Deep Dark jar | replaceInput |
| `farmersrespite:coffee_cake` | crafting shaped | Farmer's Respite jar (new since the 09-28 list) | replaceInput |
| `fruitsdelight:fig_pudding` | crafting shaped | Fruits Delight jar (new) | replaceInput |
| `incubation:chicken_egg_crate` | crafting shaped | Incubation | **no** (storage block) |
| `mca:potion_of_feminity`, `potion_of_masculinity` | crafting shaped | MCA | no (not food) |

Already fine: Brewin' `cooking/vegetable_omelet` and `fermenting/egg_grog_from_milk` use `#forge:eggs` in the 4.5.0
port; `dread_nog_from_egg_grog` takes a turtle egg; Delightful's `minecraft:pumpkin_pie` override, FD's fried rice /
baked cod stew, the pack's oven recipes ([baking.js](../../kubejs/data/creative_delight/kubejs/creativeengineering/baking.js)) and cooking.js
lines 23, 30, 92, 140, 214, 224 use `i.eggs`. `tmted:wheat_dough_from_egg` and vanilla `minecraft:cake` are removed
(cooking.js lines 90, 357). Jars scanned: the ~40 food and kitchen mods (list in the session's
`scratchpad/egg_scan.py` run); IE has no egg recipe. Not scanned: the other ~360 mods.

### Build
1. cooking.js: change the four own recipes' egg to `i.eggs`.
2. cooking.js "Protein / Egg" block, after line 385:
   ```js
   R.bulk('replaceInput', [
     [{ id: 'brewinandchewin:quiche_from_bacon' }, i.minecraft_egg, i.eggs, e],
     [{ id: 'rusticdelight:cooking/fried_egg_from_cooking_oil' }, i.minecraft_egg, i.eggs, e],
     [{ id: 'create_factory:cake_paste' }, i.minecraft_egg, i.eggs, e],
     [{ id: 'create_deep_dark:compacting_echo_cake_base' }, i.minecraft_egg, i.eggs, e],
     [{ id: 'farmersrespite:coffee_cake' }, i.minecraft_egg, i.eggs, e],
     [{ id: 'fruitsdelight:fig_pudding' }, i.minecraft_egg, i.eggs, e],
   ])
   ```
   (`NCH.replaceInput(filter, old, new, e)` at neoCraftHelper.js line 593; `R.bulk` pattern as in
   `magic/storage.js` line 11.)
3. Before building, confirm the list with a fresh `/dumpfood`: `kubejs/exported/food/recipes.json` keeps the raw
   ingredient JSON, so `grep -l '"minecraft:egg"'` style filtering on it catches mods not scanned here (IE excepted).
4. food.md 3.8 -> built, with this table.

### Ids and data
All ids verified in the jars or the backport sources, 2026-10-21. Recipe types: shaped crafting, `farmersdelight:cooking`,
`create:mixing`, `create:compacting`.

### Decisions
- Turtle and parrot eggs in `#forge:eggs`: recommend keep them (Supplementaries and Quark Delight add them on
  purpose; turtle eggs are rare enough, parrot eggs come from fed parrots). If not wanted: `global.tags.remove(event,
  i.eggs, [i.turtle_egg, i.egg_parrot_blue, ...])` (tag_helper.js line 19) in a tag script, which also changes the egg
  stack rule (`tools/gen_stack_sizes.py`).
- The oven's `pumpkin_pie_duck_egg` recipe (baking.js line 81) is now a duplicate path for duck eggs; keep it (it is
  a cheaper mirror of Untitled Duck Mod's own recipe).

### Gotchas
- `replaceInput` needs KubeJS to understand the recipe type: shaped crafting and Create types are native (KubeJS
  Create), `farmersdelight:cooking` comes from KubeJS Delight. If JEI still shows the vanilla egg after `/reload` for
  one of them, remove it and re-add through `R.fdCooking` / `R.createMixing` / `R.createCompacting` with the same id
  suffix (`_manual_only` for a pot recipe whose Slice & Dice copy must not double, food.md 3.7).
- The fried egg is the only `#forge:cooked_eggs` source besides Incubation's (removed); this widens it to every egg.

### Tests
- FO-35: JEI uses of Duck Egg and Emu Egg show Fried Egg (furnace, smoker, campfire, cooking-oil pot), both quiches,
  coffee cake, fig pudding, cake paste (mixer), echo cake base (compacting). JEI uses of Egg still show the chicken
  egg crate; Duck Egg doesn't.

---

## types-and-tuning #10: Remove unused meats from recipes and view
Status Planned / Work KubeJS / `food/types-and-tuning #10`

### Goal
Meats with no source in this pack (and the dishes that need them) are out of JEI and out of the food types.

### Now (checked against the jars and configs)
- **Venison, no source:** Delightful `venison_chops`, `cooked_venison_chops` (only from Twilight Forest / Goodall /
  Naturalist compat recipes, none installed; [delightful-common.toml](../../config/delightful-common.toml) lines 96,
  131 still `true`); Abnormals Delight `venison_shanks`, `cooked_venison_shanks`, `seared_venison`,
  `venison_with_bamboo_shoots` (every recipe has `forge:mod_loaded environmental`, not installed) **(jar)**.
- **Goat (chevon), disabled:** Delightful `raw_goat`, `cooked_goat` are `false` in delightful-common.toml (lines 94,
  120), so the goat drop (`delightful:raw_goat` loot modifier, condition `delightful:enabled`) and recipes are off,
  but the items are registered and typed (dump: Raw / Cooked Chevon in `raw/meat`, `cooked/meat`).
- **Duck (Abnormals Delight), no recipe:** `duck_fillet`, `cooked_duck_fillet`, `duck_noodles`,
  `passion_fruit_glazed_duck`: all recipes need Environmental (or Atmospheric) **(jar)**. Untitled Duck Mod's
  `untitledduckmod:raw_duck` is in `#forge:raw_duck` **(jar)**, so the duck line could be wired instead of removed.
- Item keys (all exist): item_dict lines 529 `venison_chops_cooked`, 11280 `cooked_goat`, 11339 `raw_goat`, 11365
  `venison_chops`, 12112 `cooked_duck_fillet`, 12115 `cooked_venison_shanks`, 12116 `duck_fillet`, 12117
  `duck_noodles`, 12124 `passion_fruit_glazed_duck`, 12129 `seared_venison`, 12133 `venison_shanks`, 12134
  `venison_with_bamboo_shoots`, 12496 `raw_duck`.
- Removal path: `global.removedItems` (removed_items.js) -> JEI hide, tag strip, REMOVED tooltip.

### Build
1. delightful-common.toml: `venison_chops = false`, `cooked_venison_chops = false` (drops their smelting /
   smoking / campfire recipes at the source).
2. removed_items.js: add the venison (6) and goat (2) keys. Leave the duck items out if the decision is to wire them.
3. Duck wiring (recommended): cooking.js, FD cutting `R.fdCutting(i.raw_duck, i.any_knife, Item.of(i.duck_fillet, 2), e, 'duck_fillet')`,
   furnace / smoker / campfire `cooked_duck_fillet` through `R.smelting` / `R.smoking` / `R.campfireCooking` (FD's
   0.35 xp, 200 / 100 / 600 ticks), and re-add `duck_noodles` (`i.duck_noodles`, item_dict line 12117) as
   `R.fdCooking([i.raw_duck, i.pasta, i.carrot, i.vegetables], Item.of(i.duck_noodles, 1), e, 'duck_noodles', 1.0, 200, i.bowl)`
   (the jar recipe: `#forge:raw_duck`, `#forge:pasta`, carrot, `#forge:vegetables`; `i.raw_duck` is the only raw duck,
   or add `i.t_raw_duck = '#forge:raw_duck'` to item_dict). `passion_fruit_glazed_duck` also needs Atmospheric's
   passion fruit: remove it (removed_items.js).
4. food_types.json: move the removed keys to `excluded` (reason `"removed: no source in the pack"`); run
   `food_catalog.py --check` after the next export. can_parity.md: the chevon rows are already `/` (2026-10-13).
5. Find "other unused meats" with the next export: every `raw/meat*` and `raw/fish*` item that is the output of no
   recipe in `recipes.json` and drops from no entity (`/kubejs` loot check or JEI "Recipes" on each). Candidates
   to look at: Alex's Delight cuts (all come from Alex's Mobs animals, present), Nether's Delight strider / hoglin
   (present).

### Decisions
- Which meats: recommend venison (all 6) and goat (2) removed, duck wired to Untitled Duck Mod.
- Goat: alternatively turn it on (`raw_goat = true`, `cooked_goat = true`) since goats exist; the cooking plan says
  remove. Recommend remove (goats are rare mountain mobs, and there is no chevon dish).

### Gotchas
- `recipe_scan.js` logs every recipe that still references a removed item at startup: read it after the change;
  conditional recipes that don't load won't show.
- Removing items from food_types.json changes the `food_type` tag counts in FO-02 / FO-06.

### Tests
- FO-36: JEI search "venison", "chevon" -> nothing. Raw Duck on a cutting board with a knife -> 2 Raw Duck Fillets;
  a fillet cooks in the furnace.

---

## types-and-tuning #9: Cultivated meat cultures have no nutrition
Status In Progress / Work KubeJS / `food/types-and-tuning #9`

### Goal
The 7 cultures are (poor) food, and the Cooking Pot can grow them, less efficiently than the mixer.

### Now
- Items: [cultures.js](../../kubejs/startup_scripts/items/cultures.js) lines 15-23, plain items; line 6 says "no
  .food(), add nutrition once the cooking recipes are designed". Keys `i.culture_beef` ... `i.culture_salmon`
  (item_dict lines 92-98), blocks 100-106.
- Recipes ([cultivated_meat.js](../../kubejs/data/creative_delight/kubejs/creativeengineering/cultivated_meat.js), DEPLOYED per
  recipes_cooking.md 2.5): syringe + `#creativeengineering:culture_base` + slime -> 1 culture (lines 20-26); heated
  mixing 1 culture + 250 mB diluted bonemeal + 250 mB plant oil -> 3 (lines 28-34); cutting board culture -> 4 raw
  beef / pork / mutton, 3 chicken / rabbit, 8 cod / salmon slices (lines 36-42); blocks 9 <-> 1. So the "cooking
  recipes" the comment waits for exist; cultures.js's header ("still WIP over there") is stale.
- Not typed in food_types.json (not edible yet), not in can_parity.md.

### Build
1. cultures.js: add `.food(f => f.hunger(1).saturation(0.1).meat().effect('minecraft:hunger', 600, 0, 0.8))` to the
   five meat cultures, the same without `.meat()` on cod / salmon (wolves). Values per the decision. Fix the header
   comment.
2. Pot cultivation in cultivated_meat.js: `R.fdCooking([i.culture_beef, i.bone_meal, i.t_seeds], Item.of(i.culture_beef, 2), e, 'beef_culture_pot_manual_only', 0.35, 400)`
   (`R.fdCooking(input, output, e, id, exp, time, container)`, neoCraftHelper.js line 926)
   per culture (2 per craft vs the mixer's 3, no fluids). The `_manual_only` suffix stops Slice & Dice from turning
   it into a second, fluid-free mixer recipe (food.md 3.7).
3. Export round: `food_catalog.py` types the cultures (expect `raw/meat`, cod / salmon `raw/fish`; move them to
   `hazard` if the decision is "eat in a pinch"), gives them nothing new in item_dict (keys exist).
4. can_parity.md: add the 7 as `/` rows (no can) so the worksheet stays complete.

### Decisions
- Values: recommend 1 / 0.1 + 80% Hunger 30 s (a raw culture is worse than the 12+ hunger it cuts into); type
  `hazard`, so tuning doesn't band it as raw meat.
- Pot recipe inputs: recommend culture + bone meal + a sugar or seed "feed" item, 2 out, 20 s.

### Gotchas
- Startup change: restart. The cultures' SoL complexity by formula is tiny (0.02), fine.

### Tests
- RC-19 (recipes_cooking.md prefix): eat a Beef Culture -> 1 hunger, usually Hunger; a heated Cooking Pot with a culture
  + bone meal + feed -> 2 cultures; JEI shows no mixer copy of it.

---

## types-and-tuning #1: Food tuning system
Status Planned / Work KubeJS / data / tool / `food/types-and-tuning #1` (D1 to D6)

### Goal
Every food's hunger, saturation, flags, effects and Spice of Life value follow its type (31 types,
food_types.json), with named exceptions; cans follow their source.

### Now
- Design: food.md 3.5 (one config, startup apply, server SoL map, starting bands per type, canned parity rule).
  Decisions D1 to D6 (4.3) are open.
- Pieces that exist: types and tags (foodtypetags.js), `tools/food_catalog.py` (classifier, `--check`, catalog,
  `kubejs/exported/food/catalog.json` per food with values, effects, code effects, SoL now and by formula), three
  value writers already at startup: cans.js (generated cans + 20 Canned Goods overrides), food_values.js (whole-food
  copies, 1 entry), alcohol_drinks.js (44 drinks: 1 / 0, Intoxication, Nausea).
- Canned parity has moved: it is no longer the 3.5 rule but `tools/gen_can_types.py` SECTIONS (per-section
  multipliers and servings, can_parity.md 3.1, 4.2), owned by `cans/canning #1` / `#3`. What #1 still owes cans:
  tuned source values (they flow in on the next `gen_can_types.py` run) and the SoL value per can (D5).
- SoL internals **(jar)**: `ConfigHandler.complexityMap` is a public static `Map<FoodInstance, Double>`, replaced at
  `ServerStartingEvent` from the config; `FoodList.getComplexity(FoodInstance)` reads the map first, then the formula
  `q = (h + h*mod)/2`, `q/5` below 5, else `1 + 4 log10(q - 4)` (times `defaultContribution`). Clients get the map
  on login (`ConfigHandler.syncConfig(player)`, `ConfigMessage`). The map accepts non-food items (the parser refuses
  them, the map doesn't), which #5 uses.

### Build
**A. Config and report (tool, no game):**
1. `kubejs/generation_scripts/food/food_tuning.json`:
   ```json
   { "_readme": ["..."],
     "types": { "soup": { "hunger": [6, 14], "modifier": [0.6, 0.8], "sol": 1.5,
                          "effects": [{ "effect": "farmersdelight:comfort", "secondsPerHunger": 15 }] }, ... },
     "items": { "carrot_soup": { "hunger": 10 }, "enchanted_golden_apple": { "hunger": 4, "modifier": 1.2, "sol": 2 },
                "cantaloupe_slice": { "copy": "pumpkin_slice" }, "beer": { "exempt": true } },
     "rules": { "maxModifier": 1.2, "alwaysEdibleTypes": ["golden", "golden/enchanted", "drink", "sweet"] } }
   ```
   Keys are item_dict keys (never ids). `types` inherit down the `/` tree (a `raw/meat/sliced` entry overrides
   `raw/meat`). `drink/alcohol` is `exempt` as a type (alcohol_drinks.js owns it). Fold food_values.js's one copy and
   #3's golden values into `items`, then delete those files' entries.
2. `tools/food_catalog.py --report`: per type, every food outside its band (value, band, distance) and the hard-rule
   breaks (modifier > 1.2 outside golden, always-edible outside the listed types); `--report --seed` writes the
   accepted ones into `items` as `exempt` (D1 review-and-exempt). Add SoL columns: complexity now, by formula after
   tuning, and the per-type value.
3. Add a diet simulator to the report (the table in food.md 2.3: k foods of mean complexity c, steady state
   `c (k - k(k-1)/(2*endDecay))`, tiers reached against the toml thresholds) so #12's ladder can be checked offline.

**B. Apply values (startup):**
4. `kubejs/startup_scripts/cooking/food_tuning.js` on `ItemEvents.modification`. Read food_tuning.json and
   food_types.json with `JsonIO.readString` (as cans.js does). For each typed key: resolve the type chain, merge
   type defaults <- item overrides, skip `exempt`, and write with a fresh vanilla builder so flags are explicit
   (alcohol_drinks.js lines 61-82 is the pattern; the consumer form would keep old flags, see "State"):
   ```js
   var B = Java.loadClass('net.minecraft.world.food.FoodProperties$Builder')
   e.modify(id, item => {
     var old = item.getFoodProperties(); if (!old) return
     var b = new B().nutrition(v.hunger).saturationMod(v.modifier)
     if (v.meat) b.meat(); if (v.always) b.alwaysEat(); if (v.fast) b.fast()
     keepOrReplaceEffects(old, v, b)   // copy old pairs unless v.effects replaces them
     item.setFoodProperties(b.build())
   })
   ```
   Only clamp when D1 says snap; with review-and-exempt the script applies `items` values and the hard rules only,
   and the bands stay a report. Log `[food tuning] N foods changed, M exempt, K skipped`.
5. Order with the other writers: cans.js re-values the Canned Goods cans and must stay the only writer for
   `canned` (skip the type here). alcohol (exempt). food_values.js removed. All are startup `ItemEvents.modification`
   handlers, so whichever registers last wins on a shared item: keep the sets disjoint instead of relying on order.
6. tools/build_deploy.py KUBEJS_CLASSES (lines 193-197): add
   `('generation_scripts/food/food_tuning.json', 'runtime')`, or the deploy preflight fails.

**C. Spice of Life values (server):**
7. `kubejs/server_scripts/cooking/sol_complexity.js`:
   ```js
   var SOL = { CH: Java.loadClass('com.tarinoita.solsweetpotato.ConfigHandler'),
               FI: Java.loadClass('com.tarinoita.solsweetpotato.tracking.FoodInstance') }
   function ceFillSol(server) {
     var map = SOL.CH.complexityMap   // already holds the toml list; ours overrides per item
     each typed key -> map.put(new SOL.FI(Item.of(id).item), value)
     server.players.forEach(p => SOL.CH.syncConfig(p))
   }
   ServerEvents.loaded(e => ceFillSol(e.server))       // after SoL's ServerStartingEvent rebuild
   if (Utils.server) ceFillSol(Utils.server)            // /reload re-runs scripts; refill
   ```
   The dump already loads these classes (dump_food.js lines 64-66), so the class filter allows them. Remove the
   per-item complexity lines from the toml (#4) once the script owns them, or the two disagree on restart order
   only for a moment (the script runs last).
8. Cans (D5): each can's value = its source's per-type SoL value (from food_types.json `canned`), so a can no longer
   beats its source; D5 stays "a can counts as a separate food" (no way to merge in the mod).

**D. Content passes:** #8 soups first (small, shows the loop works), then the rest by type, each as a reviewed
`items` diff. Workflow: `/dumpfood` -> `food_catalog.py --report` -> edit food_tuning.json -> restart (values) or
`/reload` (SoL only) -> `/dumpfood` -> compare -> `gen_can_types.py` (cans follow) -> restart.

### Ids and data
Effects: `farmersdelight:comfort`, `farmersdelight:nourishment`, `minecraft:hunger`. food_tuning.json names them by
effect_dict key (`global.effect`, `startup_scripts/_lib/effect_dict.js`): `ef.hunger` exists (line 38); the
Farmer's Delight ones are not in the dict yet: add `ef.comfort = 'farmersdelight:comfort'` and
`ef.nourishment = 'farmersdelight:nourishment'` under a new "Farmer's Delight" heading. Type tag keys
`i.t_food_*` (item_dict, generated block). SoL classes and fields verified in the jar.

### Decisions (recommendations)
- D1: review-and-exempt (as food.md says). Hard rules applied, bands reported.
- D2: yes (row #3).
- D3: take per-type complexities; benefits trimmed and the ladder rescaled under #12 (one decision with #12).
- D4: moot for golden cans once row #2 hides the Canned Goods golden cans; keep the hazard cans' code effects
  (pufferfish, spider eye, poisonous potato, rotten flesh: that is their point).
- D5: accept; value cans at the source's SoL.
- D6: 30% Hunger 30 s on `raw/meat` and `raw/meat/sliced` (Farmer's Delight's own raw-meat rule); not on raw fish
  (sushi and the fish cans use it raw). Pairs with `cans/canning #4` (raw can effect passthrough).

### Gotchas
- A startup change needs a restart and then a new `/dumpfood` before the generator: two restarts per value round.
- Spice of Life reads food properties live for the formula, so SoL values left to the formula move with the tuning.
- FD's vanilla-soup effects come from `config/farmersdelight-common.toml` (event-applied), not food properties: the
  tuning must not add a second Comfort to `minecraft:*_stew` / `*_soup`.
- New `item_dict` keys need a restart before the tag and tuning scripts see them.

### Tests
- FO-37 log: `[food tuning] ... changed` with no warnings; FO-38 AppleSkin on one food per type matches
  food_tuning.json; FO-39 `/dumpfood` + `food_catalog.py --report` shows no hard-rule breaks; FO-40 SoL tooltip on a
  meal shows the per-type value (client got the synced map).

---

## types-and-tuning #8: Soup and stew values from their ingredients
Status Planned / Work data / `food/types-and-tuning #8` (part of #1)

### Goal
Each soup's hunger and saturation follow what goes into it; outliers marked first, then changed.

### Now
31 foods in `soup` (dump). Main recipes from the jars, with known ingredient hunger (catalog values; tags and
non-food items count 0):

| Soup | Now h / mod | Pot recipe ingredients | Known hunger |
|---|---|---|---:|
| Beef Stew (FD) | 12 / 0.8 | `#forge:raw_beef`, carrot, potato | 7 |
| Chicken Soup (FD) | 14 / 0.75 | raw chicken, carrot, `#forge:salad_ingredients`, `#forge:vegetables` | ~9 |
| Vegetable Soup (FD) | 12 / 0.8 | carrot, potato, beetroot, salad ingredient | ~7 |
| Fish Stew (FD) | 12 / 0.8 | raw fish, tomato sauce (4), onion | ~8 |
| Pumpkin Soup (FD) | 14 / 0.75 | 3 pumpkin slices, salad ingredient, raw pork, milk | ~11 |
| Noodle Soup (FD) | 14 / 0.75 | pasta, cooked egg, dried kelp, raw pork | ~10 |
| Baked Cod Stew (FD) | 14 / 0.75 | cod, potato, egg, tomato (pack: + bone broth, shapeless, cooking.js line 23) | ~6 |
| Rabbit Stew (vanilla, FD pot) | 10 / 0.6 | baked potato, rabbit, carrot, mushroom | 11 |
| Mushroom / Beetroot Soup | 6 / 0.6 | 2 mushrooms / 3 beetroot | 0 / 3 |
| Carrot Soup (More Delight) | 12 / 0.8 | milk, 2 carrots | 6 |
| Acacia Blossom Soup (Alex's Delight) | 12 / 0.8 | 2 onions, 2 raw pasta, acacia blossom | 4 |
| Corn Soup (Corn Delight) | 10 / 0.9 | corn, salad ingredient, any raw meat or mushroom, milk | ~7 |
| Sinigang (Delightful) | 15 / 0.9 | raw fish, tomato sauce, onion, raw pork, ginger, citrus | ~13 |
| Chorus / Endermite Stew (End's Delight, 2 bowls each) | 4 / 0.5, 4 / 0.4 | 4 chorus fruit + flower / enderman loot + mite crust | 4 / 1 |
| Bayberry Soup (Fruits Delight) | 1 / 0.2, drink, bottle | 4 bayberries | ~8 |
| Sopa de Macaco (Alex's Mobs) | 5 / 0.4 | banana peel, mushroom, bone | 0 |

The rest (adzuki stew, bell pepper soup, blazing chili, bone broth, cavern explorer stew, creamy onion soup, fig chicken
stew, kangaroo stew, miso, strider moss stew, suspicious stew) need tag resolution: do them with the tool.

### Build
1. food_catalog.py `--report soups`: for every soup recipe in `recipes.json`, sum the ingredients' hunger and
   saturation points, resolving a tag to the median of its members from `kubejs/exported/tags/item_tags.json`, and
   giving non-food ingredients a virtual value (mushroom 1, milk 2, bone 0) from a small table in food_tuning.json
   (`"ingredientValues"`). Print soup value, ingredient sum, ratio; flag ratio outside 1.2-1.8.
2. Proposed rule (to confirm with the report): hunger = round(1.5 x ingredient hunger), clamped to the soup band
   6-14; modifier from the band by tier. Today's ratios sit near 1.5 for FD's core soups (beef 12/7, fish 12/8);
   outliers high: carrot soup, acacia blossom soup, baked cod stew; low: rabbit stew, bayberry soup.
3. Write the accepted values into food_tuning.json `items`; mark deliberate ones `exempt` (End's Delight stews are
   drink-animation snacks; bayberry soup is a bottle drink: move it to `drink` in food_types.json instead).

### Decisions
- Rule multiplier: recommend 1.5, with the band clamp.
- Bayberry Soup: recommend re-type as `drink` (bottle, drink animation) and leave its values.
- Pack recipe changes (bone broth in shapeless soups, `dishes/ingredients #4`) change the sums: run the report after
  those land too.

### Gotchas
- Two recipes for one soup (pot vs shapeless): use the pot recipe (the "full" one).
- Soup cans follow on the next `gen_can_types.py` run (section `soup`, mult 1.25).

### Tests
- FO-41: `food_catalog.py --report soups` prints no flagged soup after the change; AppleSkin on Carrot Soup shows the
  new value.

---

## types-and-tuning #12: Spice of Life much slower
Status Planned / Work config / KubeJS / `food/types-and-tuning #12`

### Goal
Spice of Life takes about twice as long; a shorter chain (about 12 tiers) that ends at triple max health (60, i.e.
+40 health); low-level effects spread along it instead of Strength III / Haste II at the top.

### Now
Defaults in #4 (17 tiers, +16 health total, Strength up to III, Haste II, Speed II, Luck II, armor toughness 6, full
knockback resistance). food.md 3.5's ladder (1 to 26) was sized for the per-type complexities with queue 32.

### Build
1. In the #4 toml: `queueSize = 64`, `endDecay = 64` (twice as many distinct meals remembered and needed), keep
   `startDecay = 0`. With food.md 3.5's per-type values (mean complexity about 1.5 for a varied kitchen diet, 2.5 for
   meals), steady-state diversity is `c (k - k(k-1)/128)`: FD kitchen 12 foods about 17, chef 32 foods about 40,
   64 distinct foods about 53.
2. Thresholds (12): `2, 4, 6, 8, 11, 14, 18, 22, 27, 32, 38, 45`. That gives: steak only 0, vanilla 5 foods 1,
   FD kitchen 6, chef 11, 64 foods 12, versus 11 / 17 for the kitchen / chef on the 3.5 ladder: about half the pace.
3. Benefits (12, +40 max health in total, low-level effects; `effect,<id>,0` is level I):
   ```
   attribute,generic.max_health,2
   attribute,generic.max_health,2;effect,haste,0
   attribute,generic.max_health,4
   attribute,generic.max_health,4;effect,luck,0
   attribute,generic.max_health,4
   attribute,generic.max_health,4;effect,speed,0
   attribute,generic.max_health,4
   attribute,generic.max_health,4;effect,strength,0
   attribute,generic.max_health,4;attribute,generic.armor_toughness,1
   attribute,generic.max_health,4;attribute,generic.knockback_resistance,0.25
   attribute,generic.max_health,2;effect,resistance,0
   attribute,generic.max_health,2;effect,regeneration,0
   ```
4. Check it offline with #1's diet simulator before shipping; then copy into existing worlds' serverconfig.

### Decisions
- Chain length and the effect list (with D3): recommend the 12 above. Permanent Regeneration I at the top is strong
  (benefit effects are kept on constantly); alternative: drop it and give the last tier +4 health instead.
- `resetOnDeath`: recommend false (Workspace had true); with +40 health on the line a death reset is harsh.

### Gotchas
- Raising `queueSize` keeps old food lists (they only grow); lowering it later trims them on the next eat
  (`FoodList.addFood` drops entries aged past the size) **(jar)**.
- Benefits are recomputed at login (`BenefitsHandler.onPlayerLogin`) and on every eat **(jar)**: after a config
  change, rejoin.
- Max-health benefits rescale current health (`AttributeBenefit.applyTo`, unless First Aid is loaded) **(jar)**.

### Tests
- FO-42: fresh player, eat 6 different FD meals: the food book shows 2 or 3 tiers; `/kill` keeps them (resetOnDeath
  false).

---

## types-and-tuning #5: Placed pies and non-vanilla cakes don't count for SoL
Status Testing / Work patcherMod / `food/types-and-tuning #5`

BUILT 2026-10-22 (patcherMod jar in `mods/` since 11:06, not run; scratch server PASSED 2026-10-22 for load and map (SQ-SOL-01), the bite itself is client-only FO-43), differing from the build plan below: the mixin is on `ServerPlayerGameMode#useItemOn` (m_7179_;
server only, so no side check and Create's deployer never reaches it), not `BlockStateBase#use`; cakes without a
slice item count as `farmersdelight:cake_slice` (mirrors the vanilla cake; SoL drops non-food items on load, so a
cake block item can't be recorded); map at `generation_scripts/food/placed_food_slices.json`. See food.md 4.2
finding 5, FO-43.

### Goal
A bite from a placed pie or cake counts as eating its slice, like the vanilla cake does.

### Now (verified in the jars)
- SoL counts `LivingEntityUseItemEvent.Finish` of an edible item, plus `FoodTracker.onCakeBlockEaten`: a
  `RightClickBlock` at LOWEST priority on exactly `Blocks.CAKE`, main hand, `player.canEat(false)`, server side,
  which fires `ForgeEventFactory.onItemUseFinish` with `farmersdelight:cake_slice`. Candle cakes don't count.
- FD `PieBlock.use` (`m_6227_`): a knife cuts a slice, otherwise `consumeBite` (protected): if `canEat(false)`, eats
  the slice item's food, applies its effects, bites + 1 or removes the block. Subclasses: FD apple / chocolate pie,
  sweet berry cheesecake, Brewin' quiche, Delightful salmonberry pie, End's Delight chorus pie, Farmer's Respite rose
  hip pie, Upgrade Aquatic mulberry pie, `minecraft:pumpkin_pie` (Central Kitchen makes it a PieBlock).
- Cakes each have their own eat code: Golden Foods `GoldenCakeBlock.eat` (static, `getFoodData().eat(4, 1.2)`),
  Neapolitan `FlavoredCakeBlock`, Create Crafts & Additions `CACakeBlock`, Farmer's Respite `CoffeeCakeBlock`, plus
  candle variants.
- `FoodTracker.updateFoodList(Item, Player)` is public static: one call records a food and updates benefits.
- Placed-food list: food_catalog.md "Placed food" (31 blocks from an older export; the 10-20 dump counted 139 food
  blocks incl. candle cakes and Display Delight).

### Build
1. patcherMod mixin on `net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase#m_60664_`
   (`use(Level, Player, InteractionHand, BlockHitResult)`, remap = false; Create's deployer calls the same method,
   so the SRG name is confirmed): `@Inject` at HEAD stores the state, at RETURN, if the result `consumesAction()`,
   the level is server side, the player is not a `FakePlayer`, the hand is MAIN_HAND, the block is in the map below
   and not `minecraft:cake`, and the state at the pos changed (bites property + 1, or the block is gone or became the
   plain cake from a candle cake), call `FoodTracker.updateFoodList(item, player)`.
2. Map, data-driven: `kubejs/generation_scripts/food/placed_food_sol.json` `{ block key: item key }`, read by a small
   Java loader (patcherMod already reads pack JSON elsewhere; follow that) or filled from KubeJS through a static
   setter. Pies default to `PieBlock#getPieSliceItem()` when the block is an FD `PieBlock` (no map entry needed);
   cakes map to their slice (`farmersdelight:cake_slice` for candle cakes, Abnormals Delight's Neapolitan cake slices,
   `farmersrespite:coffee_cake_slice`), or to the cake block item when there is no slice (golden cakes, Create C&A
   cakes).
3. Block items aren't food, so their SoL value comes only from the complexity map: #1C puts them there (e.g. the
   golden cake's slice-equivalent value).
4. SoL as a compile-only dependency: copy `solapplepie-1.19.2-2.2.0.jar` into `development/patcherMod/libs/` (the
   flatDir compileOnly tree), and guard the call with `ModList.get().isLoaded("solapplepie")`.

### Decisions
- Whether to fix (the row asks): recommend yes; it is one mixin and makes pies a real SoL food.
- Feasts (served into bowls) stay out: their servings are eaten as items and already count.

### Gotchas
- `use` runs on both sides; check `!level.isClientSide`.
- Knife on a pie cuts a slice (no bite): the state check (bites + 1 without a slice drop) can't tell the two apart
  by state alone, since cutting also adds a bite: also require the held item not in `#forge:tools/knives`
  (FD's `ModTags.KNIVES`).
- Don't double-count `minecraft:cake`: SoL already records it.

### Tests
- FO-43 (as built, in food.md 7): bite a placed Apple Pie and a Golden Cake; the food book's eaten list shows Slice
  of Apple Pie and Slice of Cake; a knife on the pie gives a slice and records nothing.

---

## Built, needs play

| Row | What | Note |
|---|---|---|
| `food/types-and-tuning #11` | Food tests FO-02 to FO-28: type tags, canned recipes, servings, fried eggs, egg stacks | All shipped (1.0.2 to 1.0.6). FO-02 expected counts are from 2026-10-02 (now about 1016 foods, 252 sources; note added). FO-14 to FO-22 numbers updated by the canning brief (Canned Beef holds 3). FO-28: 12 eggs in `#forge:eggs` now, the three `/give` checks still hold. Proposed new tests FO-29 to FO-43 are in the sections above. |

## Proposed TODO.md corrections

- `#4` Details: "complexity list names two missing mods" -> "complexity list names `largemeals` (missing; Farmer's
  Respite is installed since 10-02, its 10 entries apply)".
- `#2` Details: add "20 cans already re-valued by can_types.json `existing`; left: meat flag kept by cans.js
  `ceCanFood`, 13 no-source leftovers, wandering-trader golden cans, butcher Canned Food"; Decisions "fixed by row 1
  or by hand first" -> "hide leftovers, re-value canned_food".
- `#6` Details: "omelet, quiches, egg grog, dread nog" -> "fried eggs (3 own + Rustic oil), both quiches
  (`creativeengineering:quiche_from_mushroom`, `brewinandchewin:quiche_from_bacon`), cake paste, echo cake base,
  coffee cake, fig pudding (omelet and egg grog already `#forge:eggs`; dread nog takes a turtle egg)".
- `#9` Details: "culture items are plain items until the cooking recipes exist" -> "cutting / mixing recipes exist
  (cultivated_meat.js); left: food values and the pot route".
- `#10` Details: add "venison (Delightful 2, Abnormals Delight 4) has no source; goat is disabled in
  delightful-common.toml; Abnormals Delight duck has no recipe (wire to Untitled Duck Mod)".
- `#3` Details: add "generated enchanted golden cans copy the 8.0 modifier (rerun gen_can_types.py after)".

## Writeup corrections made

- [food.md](../food.md) header: status "BUILT, restart pending" -> DEPLOYED (1.0.2 on), FO-02 to FO-28 not run, link
  to this brief; last updated 2026-10-21.
- food.md 0 at-a-glance: type tags, canning.js, multi-serving cans, raw canning and fried eggs rows "BUILT ... needs
  a restart" -> DEPLOYED.
- food.md 3.2 and 3.7 headings "(built, restart pending)" -> "(DEPLOYED with 1.0.2)".
- food.md 3.5: the "ambiguous overload, prove it first" risk replaced by: both forms are in use and confirmed by the
  10-20 dump; the consumer form keeps old flags.
- food.md 3.8: `#forge:eggs` 10 -> 12 items (duck, goose), with a pointer to this brief's rescan (Brewin' port ids).
- food.md 3.9: the egg rule covers 12 eggs, not 10.
- food.md 4.2 finding 4: Farmer's Respite is installed now, its entries apply; only `largemeals` is dead; unknown
  attributes are dropped silently.
- food.md FO-02: note that the expected counts are now about 1016 foods and 252 canned sources.
