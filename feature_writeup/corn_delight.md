# Corn Delight 1.2.11 against the installed 1.0.3

**BUILT 2026-10-25, not tested.** Owner decisions are in section 2, what was built and where in section 5; the rest of the 1.2.11 diff is not taken (section 3). Needs the patcherMod 1.1.03 and Creative Delight jars installed and a restart. Installed: `corn_delight-1.0.3-1.19.2.jar` (`mods/`, `server_mods/`), 1.19.2, needs Mysterious Mountain
Lib (installed 1.2.3). Evaluated 2026-10-25 against `devmods/checkNewerVersionForContent/corn_delight-1.2.11-1.20.1.jar`
(1.20.1 / Forge, needs FD 1.3.2 and MML 1.5.15), method in [devmods_evaluation.md](devmods_evaluation.md) section 1.
Both jars unpacked in `DecompiledMods/`. Plan row: `largecontent/mod-additions #24` in [todo/TODO.md](todo/TODO.md).
Config: `config/corn_delight-common.toml`.

**Licence:** MIT in both jars. Code and art from 1.2.11 can be reused with the notice kept.

**Result in one line:** 3 new items (Corncob, Corn Popsicle, Cob Pipe), a Corncob chance from knife-harvested mature corn and
from corn milling and crushing, Classic Corn Dog carried over, corn trades for farmers and the wandering trader, and corn in the
villager food pool. Village corn farms are handled elsewhere. Nothing else from 1.2.11.

## 1. How it was read

- `python3 tools/jar_diff.py` (54 files added, 16 removed, 200 changed; en_us lang **+6 / -0 / ~0**). Most changed files
  are 1.20 reformatting: compared semantically (dropping `group`, `category`, `show_notification`, pool names), only the
  three corn processing recipes and the global loot modifier list differ.
- Classes decompiled with Vineflower and diffed (`ItemRegistry`, `CornDelight`, `CornDelightConfig`, `BlockRegistry`,
  new `villager/`, `compat/curios/`, `CobPipeItem`). SRG names checked in the 1.19.2 mappings (`f_42398_` Stick,
  `f_50546_` Coarse Dirt).
- Village farm templates read with `tools/audit_loot_containers.py`'s NBT reader.
- Removed classes are datagen providers and the code-built wild corn feature (now JSON). No content goes with them.

## 2. The plan (owner decisions, 2026-10-25)

Where it goes: the installed 1.0.3 jar stays. New items are KubeJS items in the `creativeengineering:` namespace (like
`cornmeal`), registered in Creative Delight's startup tree `kubejs_startup/corn_delight/` and keyed once in `item_dict.js`
(`i.corncob`, `i.corn_popsicle`, `i.cob_pipe`). Recipes go through NCH, tags through `global.tags`, in the
`kubejs/data/creative_delight/kubejs/corn_delight/` server tree (`// requires: corn_delight`, needs `global.CD`). MIT art from the
1.2.11 jar, notice kept. Java (cob pipe, villager hook) stays in patcherMod (Corn is not a CD member, see
[creative_delight.md](creative_delight.md) section on routing).

### 2.1 Corncob

| Part | Plan |
|---|---|
| Item | `creativeengineering:corncob`, stack 64 |
| Best kernel route | Cutting board with a knife: 1 Corncob -> 6 corn kernels. Corn itself stays 1 -> 4 ([cooking.js](../kubejs/server_scripts/cooking/cooking.js) "Cornmeal"), so the cob is the most efficient route per item. **6 is a placeholder**, confirm |
| Cornmeal routes | Corn and wild corn through Create milling, the Millstone and the IE crusher: existing cornmeal outputs plus a 10% Corncob secondary (`foodGrinding` and `ieCrushing` secondaries in the Cornmeal block). The mortar mirror is the machine average rounded down, so it gives no cob. Crates, kernel bags, kernels and PneumaticCraft get none |
| Compost | Same chance as Straw (0.3 in the Farmer's Delight compost rows), as a row in CD's `corn_delight/compost.js` beside the existing one |
| Other 1.2.11 uses (Paper, Organic Compost, fuel, IE ethanol, pig food) | not taken |

### 2.2 Corn Popsicle

One more popsicle in the `frozen/popsicle` family beside Melon, Hamimelon, Kiwi, Chocolate and Chorus Fruit
([food_families.md](food_families.md)). Shaped: corn over ice, stick (the stick comes back, as the others). Upstream values 4
nutrition, 0.2 saturation modifier are a starting point; then it takes the family rules: `f.dessert_corn_popsicle` row in
`food_dict.js`, `#forge:ice_cubes` as the ice, Food Plate placeable, can parity ([can_parity.md](can_parity.md)), and the food
tuning pass ([food.md](food.md)). Not a one-off.

### 2.3 Cob Pipe

`creativeengineering:cob_pipe`, shapeless: wooden rod + Corncob. Cosmetic, no stats. Wearable in the vanilla head armor slot
(item `getEquipmentSlot` HEAD, patcherMod) and in a Curios head slot (item tag `curios:head`, with the 1.20 renderer ported).
If a mouth or face Curios slot exists it takes the pipe first: the item is also in the `curios:mouth` and `curios:face` tags
(unused tags are harmless). The pack's Curios 5.1.6.4 defines no such slot today (only Curios' stock set plus Season HUD's
calendar slot), so that part only takes effect if a mod or the pack adds one; the pack does not add a slot for it. At build
check that quick-equip puts the pipe in the mouth slot before head when both exist. The hidden "General of the Maize"
advancement is skipped (the pack drops advancements).

### 2.4 Mature corn with a knife

Breaking mature corn with a knife (no Silk Touch) has a 25% chance of a Corncob. A `farmersdelight:add_item`-style global loot
modifier is not available here, so it is a pack loot rule beside the pumpkin one ([farmers_delight.md](farmers_delight.md)
"Pumpkin with a knife"). It gives a Corncob, not Straw: corn still gives no straw.

### 2.5 Recipes carried over

Corn Dog (cooked meat + cornbread batter) is in the jar already. Classic Corn Dog already has a pot recipe (batter, cooked meat,
Tomato Sauce); add the 1.2.11 grid recipe, Corn Dog + Tomato Sauce, through NCH. Mekanism recipes skipped (not in the pack).

### 2.6 Trades and villager food

| Change | Plan |
|---|---|
| Farmer, Novice | buys corn for 1 emerald (upstream 16-32 or 32-48 a trade; default 24), through `trades.js` in CD's `corn_delight/` like the Veggies farmer trades |
| Farmer, Apprentice | sells Cornbread and Tortillas (upstream 6-16 for 6-20 emeralds; default 8 each for 8 emeralds, adjust at build) |
| Wandering Trader | sells Corn (the crop), and the Popcorn Box in RARE (upstream meant it as rare, it landed in the common list by a bug). Kernels are already sold in COMMON ([wandering_trades.js](../kubejs/server_scripts/economy/wandering_trades.js)). Prices follow the pack's `t.sell` conventions |
| Villager food pool | Corn joins the food villagers pick up and share, **and counts as breeding food**: it gets a food-point value in `Villager.FOOD_POINTS` so a villager holding or being given corn becomes willing to breed (the value matching the vanilla carrot, 1, unless upstream differs). Same method as Vintage and Veggies for the pickup list (they combine with the current `Villager.WANTED_ITEMS`; see [vintage_delight.md](vintage_delight.md) 18 Mob audit); the `FOOD_POINTS` map is new for the pack, so check no other mod replaces it |

### 2.7 Not done here

Village corn farms: handled elsewhere (not in this plan).

## 3. 1.2.11 changes not taken

- Straw from knife-harvested corn (replaced by 2.4's Corncob chance).
- Comfort -> Nourishment on Creamed Corn, Corn Soup, Creamy Corn Drink (Comfort kept, [farmers_delight.md](farmers_delight.md) 2.5).
- Wild corn patch frequency, `genCornLootModifier` removal, own creative tab, de / ja / kk lang, Curios entity data, 1.20 tags
  (`villager_plantable_seeds`, `chicken_food`, `pig_food`), Mekanism recipes.
- Corncob extras: Paper, Organic Compost, fuel, ethanol, pig food.

## 4. Tests

In game (client), after installing both jars and a restart. The server halves are queued as SQ-CN in
[scratch_queue.md](testing/scratch_queue.md).

- [ ] CN-01 Corncob in JEI; cutting board + knife gives 6 kernels.
- [ ] CN-02 Mill, Millstone, IE crusher on corn: cornmeal as before, Corncob about 1 in 10 over 50 runs; wild corn in the mill
  and Millstone the same; mortar gives no cob. (Wild corn in the IE crusher gives kernels only, as before: no cob.)
- [ ] CN-03 Composter accepts a Corncob at the Straw chance (0.3).
- [ ] CN-04 Mature corn broken with a knife drops a Corncob about 1 in 4; with a hand, no cob; immature corn none; a Silk Touch
  knife none.
- [ ] CN-05 Corn Popsicle: the recipe takes Ice Cubes; eating it gives the stick back (not in Creative); 4 nutrition. Sneak +
  right click it on a block top places "Placed Corn Popsicle" on Display Delight's tray, an empty hand takes it back.
- [ ] CN-06 Cob Pipe crafts (Corncob + stick). Right click with no helmet: it goes in the Face slot (the pack's slot), the
  Face slot full: on the head; with a helmet and Face full: a Curios Head slot if one exists. It fits the armor head slot by
  hand and shift click. Drawn on the head from the head slot and from Face / Head curios (3D pipe, flat in the GUI).
- [ ] CN-07 Corn Dog + Tomato Sauce in the grid gives a Classic Corn Dog (bowl back).
- [ ] CN-08 Farmer trades (buy 24 corn; sell 8 cornbread / 8 tortillas for 8 emeralds); wandering trader corn (common) and
  Popcorn Box (rare).
- [ ] CN-09 A villager picks up corn from the ground, and two villagers with corn in reach breed.
- [ ] CN-10 `logs/latest.log` after boot: `[corn-delight] corn is villager food`, no KubeJS error from `corn_delight/`.
- [ ] CN-11 After `/dumpall`: `python3 tools/food_catalog.py --check` and `python3 tools/food_families.py` list the Corn
  Popsicle under `frozen/popsicle` (classify it there if not), then `tools/gen_can_types.py` for the family's can rule
  (section 5 open items).

## 5. Built (2026-10-25)

| Part | Where | Notes |
|---|---|---|
| Items | CD startup `kubejs_startup/corn_delight/corn_items.js`; keys in `item_dict.js` (`i.corncob`, `i.corn_popsicle`, `i.cob_pipe`), `food_dict.js` (`f.dessert_corn_popsicle`), `block_dict.js` (`b.corn_crop`) | KubeJS items, `creativeengineering:` namespace |
| Art | `kubejs/assets/creativeengineering/textures/item/` (`corncob`, `corn_popsicle`, `cob_pipe`, `cob_pipe_uv`), MIT notice `corn_delight_art_NOTICE.txt` there | The pipe model is `models/item/cob_pipe.json`: Forge `separate_transforms`, 3D (`cob_pipe_3d.json`, 1.2.11's model) in hand and on the head, flat sprite in the GUI, on the ground and in frames. 1.2.11 did this with MML's `MultiModelBakedModel`, which MML 1.2.3 lacks |
| Compost | CD startup `corn_delight/compost.js` | 0.3 |
| Recipes | `kubejs/data/creative_delight/kubejs/corn_delight/recipes.js` (cob cutting, popsicle, pipe, Classic Corn Dog grid); cob secondary in `creativeengineering/recipes.js` (corn and wild corn `foodGrinding`) and `immersiveengineering/recipes.js` (corn crusher) | `foodGrinding` keeps byproducts machine-only, so the mortar gives no cob. Wild corn in the IE crusher is a kernel recipe, not a cornmeal route, so it gets no cob |
| Tags | `corn_delight/tags.js`: `curios:head`, `curios:mouth`; `curios:face` is in `server_scripts/tags/curiostags.js` | The pack now has a Face slot ([curios_slots.md](curios_slots.md)); it is the "mouth or face" slot of 2.3 |
| Knife loot | `corn_delight/loot.js` (LootJS, `CE/corn_knife_corncob`) | `corn_crop` at age 7, as 1.2.11's straw modifier (either half) |
| Trades | `corn_delight/trades.js` (MoreJS) | Fixed prices: 24 corn -> 1 emerald (Novice, 16 uses); 8 emeralds -> 8 Cornbread / 8 Tortillas (Apprentice); wanderer corn 1 emerald COMMON, Popcorn Box 2 emeralds RARE |
| Head slot | patcherMod `mixin/MobCobPipeSlotMixin` (`Mob.getEquipmentSlotForItem`, SRG `m_147233_`) | Armor slot, shift click, dispensers. Mobs that pick up gear may wear it too, as with 1.2.11's `Equipable` |
| Right click, Curios | patcherMod `compat/CornDelightCompat` (+ `CornDelightCurios`): mouth, face, empty head slot, then Curios head; `compat/CobPipeRenderer` (client, 1.2.11's renderer ported to 1.19.2) | Done in a right-click handler, not a Curios capability, so the order is the pack's |
| Villager food | `CornDelightCompat.addVillagerFood` (common setup) | Corn in `WANTED_ITEMS` and `FOOD_POINTS` (1). `FOOD_POINTS` was not new: the Veggies port already extends it; both copy the current value, so they combine. Written by Unsafe (no refmap, so no `@Accessor`) |
| Food Plate | patcherMod `displaydelight_dishes.json` entry `placed_corn_popsicle` (`plate`), files from `tools/gen_dd_dishes.py` | The pack's own route for a food Display Delight has no block for (the sprite on DD's tray), not DD's Food Plate block item. The generator now also reads a KubeJS item's sprite from `kubejs/assets`, takes an entry `name`, and searches `server_mods/` (End's Delight is only there today) |

Open:

- Food type and can: the popsicle needs a `/dumpfood` before `tools/food_catalog.py` can put it in `frozen/popsicle` and
  `tools/gen_can_types.py` can give it a can. No popsicle has a can yet (the family's R1 rule is planned), so it waits with
  them. Values (4 / 0.2) are upstream's until the food tuning pass.
- `gen_dd_dishes.py --check` reports the 9 older dish models stale: the installed Display Delight 1.7.0 names the tray
  textures `displaydelight:block/tray_*` where the files say `farmers3d/tray_*`. Both textures exist; left as they are (not
  part of this work). Rerunning the generator rewrites them.
- 6 kernels from a cob is still the placeholder (2.1).
