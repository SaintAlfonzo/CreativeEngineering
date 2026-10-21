# Brief: recipes / metal

Written 2026-10-21 for the open `recipes/metal` rows of [TODO.md](../todo/TODO.md). Reference writeup:
[recipes_metal.md](../recipes_metal.md) (test prefix **RM-**, last used RM-14; new tests below start at RM-15, take the
next free number when you add them). Layout: [README.md](README.md).

**Verified against jars** (downloaded and read 2026-10-21): Immersive Engineering 9.2.4 (`metalpress/plate_*`,
`metalpress/rod_*`, `crafting/plate_*_hammering`, `crafting/stick_*`, `crusher/ingot_gold`, `crusher/ingot_rose_gold`,
`arcfurnace/alloy_rose_gold`, `alloysmelter/electrum`), Vintage Improvements 0.2.0.3 (`pressing/*_ingot`, rose gold
tags), Immersive Posts 4.3.0 (`has_<metal>_rod`), Minecraft Comes Alive 7.5.20 (rose gold recipes and tags), Create
0.5.1.i (`splashing/red_sand`, `splashing/soul_sand`, `splashing/crushed_raw_gold`), Create Deco 1.3.3 (bar and
catwalk recipes), Dramatic Doors 3.2.7 (no rusty-door recipe JSON in the jar). Pack evidence: the scan log
`kubejs/startup_scripts/todo/recipes_to_remove.txt`. Anything else is marked unverified.

## Scope and order

| Row | Change | Work | Group |
|---|---|---|---|
| metal #5 | Polished iron bars have no recipe (Create Deco catwalks and supports uncraftable) | KubeJS, 2 lines | A: quick fixes |
| metal #2 | Hand-crafted rods | none (already every metal) | A: close |
| metal #1 | Plates made three (really four) ways | KubeJS | B: one metal-parts pass |
| metal #4 | Standardization gaps and leftovers | KubeJS | B |
| metal #3 | Rose gold in gold tags | KubeJS tags | C: decide, then tags |
| metal #6 | More gold sources | KubeJS | D: after #3 |

Suggested order: **A** (one `/reload`), then **B** as one pass over `metal_base_materials.js` (#1's plate table and
#4's dust-recycle and leftovers touch the same sections), with `recipes/engineering #6`'s laser-cut plates
([recipes_engineering.md](recipes_engineering.md)) using the same metal list. **C** next: it changes what counts as
gold, which `food/meals #9` ([food_meals.md](food_meals.md)) depends on and which #6 should be balanced against. **D**
last.

---

## metal #1: Plates made three ways

Status Planned · Work KubeJS · TODO `recipes/metal #1` (F1)

### Goal
Every metal with a plate gets the same set of plate recipes, all from the pack, and no press makes a removed item.

### Now
Per metal today (pack lines in [metal_base_materials.js](../../kubejs/server_scripts/metal/metal_base_materials.js)):

| Route | Pack's own | Mod recipes still live |
|---|---|---|
| Create pressing | copper, iron, gold, zinc (lines 610-615) | **`vintageimprovements:pressing/<m>_ingot` for aluminum, lead, silver, nickel, uranium, zinc, constantan, cast_iron, rose_gold** (type `create:pressing`, **jar**); none removed. Their outputs are VI sheets, which are in `global.removedItems` (`i.aluminum_sheet` etc.) except rose gold, whose VI sheet *is* the pack's `i.plate_rose_gold`. Zinc therefore has two Create pressing recipes on the same input. Listed by the old scan log (lines 98-145) |
| IE metal press (plate mold) | gold (618-620; IE's `metalpress/plate_gold` removed line 110) | `immersiveengineering:metalpress/plate_<m>` for copper, iron, zinc, aluminum, lead, silver, nickel, uranium, steel, constantan, electrum, brass, rose_gold (tag outputs, `forge:plates/<m>`, **jar**) |
| Hammer + ingot (shapeless) | gold, zinc (585, 589) | `immersiveengineering:crafting/plate_<m>_hammering` for copper, iron, aluminum, lead, nickel, silver, uranium, steel, constantan, electrum (**jar**; gold's removed line 109) |
| Plate ↔ plate block (9:1) | all 9 base metals (578-606) | Create Compressed for copper / iron / gold / brass |

- The removed Create sheets (`create:pressing/{copper,iron,gold}_ingot`, lines 45, 88, 106) are already replaced.
  Create's own brass pressing (`create:pressing/brass_ingot` → `create:brass_sheet` = `i.plate_brass`) is the pack's
  brass plate, so brass is fine.
- Dict keys follow `i.<part>_<metal>` (`i.plate_aluminum`, `i.ingot_aluminum`, `i.block_aluminum`,
  `i.plate_block_aluminum`, lines 828-1265); missing parts are `i.na` (`minecraft:barrier`) or `i.none`
  (`createdeco:gray_decal`), dict lines 8-9.

### Build
1. Add one metal list at the top of the plate section (or a shared `global.METAL_PARTS` in a startup `_lib` file if
   `recipes/engineering #6` and `engineering/sawmill-and-components #8` will reuse it; recommend the shared list, names
   only, ids still from the dict):
   ```js
   var PLATE_METALS = ['copper', 'iron', 'gold', 'zinc', 'aluminum', 'lead', 'silver', 'nickel', 'uranium',
                       'steel', 'constantan', 'electrum', 'brass', 'cast_iron', 'rose_gold']
   var has = id => id && id !== i.na && id !== i.none
   ```
2. Remove every mod plate recipe for those metals (one `R.bulk('remove', ...)` built from the list):
   `vintageimprovements:pressing/<m>_ingot` (all nine that exist), `immersiveengineering:metalpress/plate_<m>`,
   `immersiveengineering:crafting/plate_<m>_hammering`, and `create:pressing/brass_ingot` only if brass moves to the
   pack's recipe too. Removing an id that doesn't exist is harmless, so the loop can try every metal.
3. Add the pack's three routes for every metal where `has(i['plate_' + m]) && has(i['ingot_' + m])`:
   ```js
   R.bulk('createPressing', PLATE_METALS.filter(ok).map(m => [i['plate_' + m], i['ingot_' + m], e]))
   R.bulk('ieMetalPress',   PLATE_METALS.filter(ok).map(m => [i['plate_' + m], i['ingot_' + m], i.mold_plate, e]))
   R.bulk('shapeless',      PLATE_METALS.filter(ok).map(m => [i['plate_' + m], [i.ie_hammer, i['ingot_' + m]], e]))
   ```
   and delete the hand-written lines 585, 589, 610-620 they replace.
4. Plate ↔ plate block: extend lines 578-606 to the same loop where `has(i['plate_block_' + m])` (steel, constantan,
   electrum, cast iron and netherite have pack plate blocks: `creativeengineering:<m>_plate_block`).
5. With `recipes/engineering #6`: laser cutting `i['block_' + m]` → plates in the same loop.
6. Rewrite the header audit item 1 (lines 6-11) as a short note of the new state; writeup 2.3 Plate row, F1 fixed,
   RM-07.

### Ids and data
`i.mold_plate` (1611), `i.ie_hammer` (1601), the plate / ingot / block keys above. New recipe ids come from `toId`
(`createpressing/<ingot>_to_<plate>_by_createpressing` etc.); pass explicit ids (`'plate_' + m + '_pressing'`) if
stable ids matter for JEI ordering or tests.

### Decisions
- **IE for 8 + Create for the missing 5, or all on pack recipes?** Recommend all on pack recipes from one list: it
  fixes the VI removed-sheet outputs in the same stroke, gives every metal the same three routes, and makes the plate
  set a data change. The IE-press-only option would still have to remove the nine VI pressing recipes.
- **Alloys in the list?** Recommend yes (steel, constantan, electrum, brass, cast iron, rose gold); they already have
  plates and IE / VI recipes, just not the pack's.
- **Hammer yield:** keep 1 ingot → 1 plate (IE's), so the hand route isn't worse than the press.

### Gotchas
- IE's own press recipes output a tag (`forge:plates/<m>`) resolved by IE's mod preference; the pack's use the dict
  item, so after the change the plate a press gives can only differ if a dict id differs from IE's pick (check zinc:
  `creativeengineering:zinc_plate`, tagged `forge:plates/zinc` in `metaltags.js` line 22).
- `R.shapeless` with the hammer: the IE hammer is a damageable container item, so it survives (as IE's own recipes).
- Line 211 removes `minecraft:pressing/zinc_sheet`, an id that can't exist (wrong namespace); harmless, delete it in
  the same pass.

### Tests
- **RM-15 Plates everywhere:** for aluminium, steel and cast iron: JEI plate → Create press, IE press, hammer, all pack
  ids; put an aluminium ingot under a mechanical press → IE aluminium plate (not a hidden VI sheet). Replaces RM-07.

---

## metal #2: Hand-crafted rods

Status Planned · Work KubeJS · TODO `recipes/metal #2` (F4)

### Goal
Decide whether every metal gets a crafting-table rod.

### Now
Already every base metal, 2 ingots → 4 rods (all **jar**-verified except the pack's):

| Metal | Recipe |
|---|---|
| zinc | pack, `metal_base_materials.js` line 623 |
| iron, aluminium, steel | `immersiveengineering:crafting/stick_{iron,aluminum,steel}` |
| copper, gold, lead, silver, nickel, uranium, constantan, electrum | `immersiveposts:has_<metal>_rod` (Immersive Posts 4.3.0; gold's ingot tag is swapped to `i.ingot_gold` at line 121) |

Not covered: brass (`createaddition:brass_rod`), cast iron (`vintageimprovements:cast_iron_rod`), rose gold
(`creativeengineering:rose_gold_rod`). The writeup's F4 and the code header (line 15: "2 ingot → 1 rod", "line 558")
were wrong; F4 is corrected.

### Build
1. Nothing required. Optionally add the three alloys for parity in the same shape, through `R.bulk('shaped', ...)`:
   `[Item.of(i['rod_' + m], 4), ['A', 'A'], { A: i['ingot_' + m] }, e, 'rod_' + m + '_hand']` for brass, cast_iron,
   rose_gold.
2. Fix header line 15 (or drop the header, metal #4).
3. Close the row (or mark it Complete after RM-16).

### Ids and data
`i.rod_<m>` / `i.ingot_<m>` (dict 828-1266). No new keys.

### Decisions
- **Every metal or none?** It is already "every metal" by mods; recommend keeping it and adding the three alloys.
  "None" would mean removing 11 mod recipes for no gain.

### Gotchas
- Immersive Posts' recipes use `#forge:ingots/<metal>`; with MCA's tag, `has_gold_rod` would take rose gold ingots if
  line 121 didn't swap the input (see metal #3).

### Tests
- **RM-16 Hand rods:** 2 lead ingots in a column → 4 lead rods; same for copper and nickel.

---

## metal #3: Rose gold in gold tags

Status Planned · Work data (KubeJS tags) · TODO `recipes/metal #3` (F5, RM-11)

### Goal
Decide where rose gold may stand in for gold, and make the tags say exactly that.

### Now
- Pack tags ([metaltags.js](../../kubejs/server_scripts/tags/metaltags.js)): line 15 rose gold rod in
  `forge:rods/gold` (and `forge:rods`, `forge:rods/all_metal`), line 40 rose gold nugget in `forge:nuggets/gold`, line
  42 the MCA rose gold block in `forge:storage_blocks/gold` (with `t.addTag`, so the parents too).
- **MCA's own tags** (**jar**): `data/forge/tags/items/ingots/gold.json` = `mca:rose_gold_ingot`,
  `dusts/gold.json` = `mca:rose_gold_dust`. So rose gold already counts as gold for every `#forge:ingots/gold` and
  `#forge:dusts/gold` recipe, independent of the pack.
- Consequences (recipes **jar**-verified, the outcome in game unverified):
  - IE crusher `immersiveengineering:crusher/ingot_gold`: `#forge:ingots/gold` → `#forge:dusts/gold`; IE picks the
    output by mod preference, presumably IE's gold dust, so rose gold ingot → gold dust → gold ingot (a conversion).
  - IE arc furnace `arcfurnace/dust_gold` takes `#forge:dusts/gold`: rose gold dust → gold ingot directly.
  - IE alloy `alloysmelter/electrum`: `#forge:ingots/gold` + silver → 2 electrum, so rose gold makes electrum.
  - Pack recipes on the tags: golden carrot / apple / enchanted apple (`cooking/golden.js` lines 11-23, via
    `i.any_nugget_gold`, `i.any_ingot_gold`, `i.any_block_gold`), powered rails and shields (`i.any_rod_gold`,
    `transport.js` 46-47, `equipment/shield.js`).
- Rose gold is cheap: tuff crushing gives 10 % rose gold nugget next to 10 % gold nugget
  (`engineering/resource_blocks.js` line 146); MCA's own recipes are block ↔ ingot ↔ dust only.
- Beacons: the item tag doesn't make a beacon base (that is the block tag `minecraft:beacon_base_blocks`); the
  writeup's "beacon bases" note is corrected.
- `forge:ingots/rose_gold` and `forge:dusts/rose_gold` are empty, so IE's `arcfurnace/alloy_rose_gold` (3 copper +
  `#forge:ingots/gold` → 4 rose gold) and `crusher/ingot_rose_gold` are switched off by their `tag_empty` conditions.
  VI does tag `forge:plates/rose_gold`, `rods/rose_gold`, `wires/rose_gold` (**jar**).
- [food_meals.md](food_meals.md) `food/meals #9` wants rose gold out of the golden foods and recommends a local
  pure-gold tag there, "since rose gold rods in `forge:rods/gold` may be intended".

### Build
Recommended: rose gold is its own metal; only the rods stay interchangeable.
1. `metaltags.js`: delete lines 40 and 42; add rose gold to its own tags and take MCA's entries out of the gold ones:
   ```js
   t.remove(e, 'forge:ingots/gold', i.ingot_rose_gold)
   t.remove(e, 'forge:dusts/gold', i.dust_rose_gold)
   t.addTag(e, 'forge:ingots/rose_gold', i.ingot_rose_gold)
   t.addTag(e, 'forge:dusts/rose_gold', i.dust_rose_gold)
   t.addTag(e, 'forge:nuggets/rose_gold', i.nugget_rose_gold)
   t.addTag(e, 'forge:storage_blocks/rose_gold', i.block_rose_gold)
   ```
   Keep line 15 (rods) if powered rails and shields should take rose gold rods (recommended: yes, that is the one
   place it reads as intended).
2. Check what the new rose gold tags switch on: IE's `arcfurnace/alloy_rose_gold` (3 copper + 1 gold → 4 rose gold)
   and `crusher/ingot_rose_gold` load once `forge:ingots/rose_gold` has a member. Recommend keeping the crusher and
   removing the alloy (`R.remove({ id: 'immersiveengineering:arcfurnace/alloy_rose_gold' }, e)`) unless rose gold
   should become craftable; it would make a cheap gold multiplier for the rods.
3. `metal_base_materials.js` line 121 (`immersiveposts:has_gold_rod` input swap) becomes unnecessary; leave it or
   delete it.
4. Then `food/meals #9` step 1 is "nothing to do" (its own brief says so).
5. Writeup: F5 rewritten as built, 2.3 tags paragraph, RM-11.

### Ids and data
`i.ingot_rose_gold` (`mca:rose_gold_ingot`, 1146), `i.dust_rose_gold` (`mca:rose_gold_dust`, 1143),
`i.nugget_rose_gold` (`creativeengineering:rose_gold_nugget`, 1145), `i.block_rose_gold` (`mca:rose_gold_block`, 1147),
`i.rod_rose_gold` (1150). The tag helper has `remove` (`tag_helper.js` line 19) and `addTag` (line 110).

### Decisions
- **Keep rose gold in the block tag?** (the row's question) Recommend no: the block tag only makes the enchanted golden
  apple and IE's storage crusher take it. Recommend also taking it out of the ingot, dust and nugget gold tags (above),
  and keeping only the rod tag.
- **Fix in the tags or locally in golden.js (`food/meals #9`)?** Recommend the tags: MCA's ingot and dust tags make
  the conversion through IE's crusher and arc furnace, which a local golden.js change can't stop.

### Gotchas
- Tag removal must run after MCA's tags load; `ServerEvents.tags('item')` runs after datapack tags, so `t.remove` works.
- Removing a tag member doesn't change recipes already baked until `/reload` (tags reload first, then recipes); a
  full `/reload` covers both.
- Every mod recipe on `#forge:ingots/gold` or `#forge:dusts/gold` loses rose gold as a substitute, not only the three
  IE ones above; check JEI uses of the rose gold ingot before and after the change (RM-17) and note what disappears.

### Tests
- **RM-17 Rose gold apart:** `/kubejs hand` on a rose gold ingot → `forge:ingots/rose_gold`, not `forge:ingots/gold`;
  rose gold ingot in the IE crusher → rose gold dust; IE alloy kiln rose gold + silver → nothing; golden carrot does not
  take rose gold nuggets; powered rail still takes rose gold rods. Replaces RM-11.

---

## metal #4: Metal standardization gaps

Status Planned · Work KubeJS · TODO `recipes/metal #4` (F6, F7)

### Goal
Close the remaining gaps in the metal scripts and remove the leftovers.

### Now
- **Plates:** metal #1. **Rods:** already complete (metal #2).
- **Dust → ingot (F6):** the pack's own only for zinc: smelting line 490, blasting 510, arc furnace 522 (the header's
  "lines 458/471/476" are stale). The other 8 use IE's `immersiveengineering:arcfurnace/dust_<m>` and IE's
  `smelting/ingot_<m>_from_dust` (per the header audit; unverified per metal).
- **Header audit** (lines 3-29): written as a chat reply ("Want me to fill these in?"); items 1-3 partly wrong (see
  metal #1, #2).
- **Tall rusty iron door** ([misc_metal_crafts.js](../../kubejs/server_scripts/metal/misc_metal_crafts.js) lines
  68-69): `createSplashing(i.tall_rusty_iron_door, i.tall_iron_door)` with "// Broken? Item still craftable". Note the
  direction: every row of the splashing block (lines 57-70) turns a **clean** item into its rusty one (washing rusts
  it); the writeup said the reverse and is corrected. Dramatic Doors makes its tall and short doors through runtime
  recipes (no JSON for the rusty doors in the jar), so "still craftable" probably means Dramatic Doors' own tall rusty
  door recipe (from two rusty iron doors) survives; unverified, check JEI.
- **Log line:** `misc_metal_crafts.js` line 3 says "The metal base event has started!" (copy of the base file's;
  line 128 says "misc metal").
- Also found: Create Deco's shaped bars (`createdeco:{copper,gold,zinc,brass,cast_iron,andesite,netherite}_bars`, 6
  ingots → 16, **jar**) are not removed; only the stonecutter ones are (`metal_building.js` lines 9-23). They sit next
  to the pack's 6 rods → 9 (3 bars per ingot by hand rods, 2.7 by Create Deco), so no exploit, just an extra route.
- Dead code: `replaceOutput({ mod: 'createsifter' }, ...)` (line 81; Create Sifter is not in the pack) and the
  `minecraft:pressing/zinc_sheet` removal (line 211).

### Build
1. Dust → ingot: in the metal #1 loop, add `R.smelting`, `R.blasting`, `R.ieArcFurnace` for `i['dust_' + m]` →
   `i['ingot_' + m]` for the 9 base metals and remove IE's `arcfurnace/dust_<m>` and `smelting/ingot_<m>_from_dust` /
   `blasting/...` ids (check the exact ids with `/kubejs` or JEI first; IE 9.2.4 names them under
   `immersiveengineering:smelting/` and `blasting/`, unverified per metal). Or leave IE's (Decisions).
2. Replace the header comment with a 5-line summary of what the file does (sections and the metal list).
3. Rusty doors: check JEI `dramaticdoors:tall_rusty_iron_door`. If a Dramatic Doors crafting recipe makes it, remove it
   by output and mod, keeping the washing: `R.remove({ output: i.tall_rusty_iron_door, mod: 'dramaticdoors' }, e)`;
   do the same for `i.short_rusty_iron_door` and the two rusty sheet-metal doors, and add washing rows for the short
   and tall pairs (`[i.short_rusty_iron_door, i.short_iron_door, e]`, `[i.tall_rusty_sheet_metal_door,
   i.tall_sheet_metal_door, e]`, `[i.short_rusty_sheet_metal_door, i.short_sheet_metal_door, e]`). Drop the "Broken?"
   comment.
4. `misc_metal_crafts.js` line 3: "The misc metal event has started!". Delete line 81 (`createsifter`) and line 211.
5. Create Deco bars: leave (Decisions) or remove the seven `createdeco:<m>_bars` shaped ids in the
   `metal_building.js` removal list.
6. Writeup: F6, F7 fixed; 2.4 bars note; RM-13 already corrected.

### Ids and data
Door keys: `i.tall_rusty_iron_door` 1290, `i.tall_iron_door` 1300, `i.short_rusty_iron_door` 11412,
`i.short_iron_door` 11398, `i.tall_rusty_sheet_metal_door` 11474, `i.tall_sheet_metal_door` 11475,
`i.short_rusty_sheet_metal_door` 11413, `i.short_sheet_metal_door` 11414. Dust keys `i.dust_<m>` (e.g. `i.dust_zinc`
= `creativeengineering:zinc_grit`, 909).

### Decisions
- **Pack dust → ingot for all, or leave IE's?** Recommend leaving IE's (they work and give the same 1:1) unless the
  metal #1 pass is open anyway; then do all 9 for uniformity.
- **The header question** ("plate: keep IE press for the 8 or replace"): answered by metal #1 (replace).
- **Create Deco 6-ingot bars:** recommend removing them, so bars come only from rods and the laser (one design).

### Gotchas
- `R.remove({ output, mod })`: a removal filter by output also hits the pack's splashing recipe if `mod` is left out;
  keep the `mod` filter.
- Dramatic Doors' runtime recipes might not exist at `ServerEvents.recipes` time if they are injected later; if the
  removal doesn't take, report it and use a Dramatic Doors config option instead (unverified).

### Tests
- **RM-18 Rust doors:** wash a tall iron door → tall rusty iron door; JEI tall rusty iron door → washing only.
- **RM-19 Dust recycle (if built):** smelt aluminium dust → aluminium ingot by a `creativeengineering:` id.

---

## metal #5: Polished iron bars stonecutting removed

Status Planned · Work KubeJS · TODO `recipes/metal #5`

### Goal
`createdeco:polished_iron_bars` has a recipe again.

### Now
- [metal_building.js](../../kubejs/server_scripts/metal/metal_building.js) line 11 removes
  `createdeco:polished_iron_bars_from_iron_ingot_stonecutting` (1 iron ingot → 4, **jar**), the item's only real
  recipe; `createdeco:iron_bars_from_panel` turns the overlay back into bars, and the overlay is made from the bars.
- Create Deco uses polished iron bars in `createdeco:iron_catwalk` (3), `iron_catwalk_stairs` and `iron_support` (4),
  **jar**. So iron catwalks, catwalk stairs and iron supports are uncraftable today. This is closer to a bug than a
  question.
- The other bars get three pack routes (laser ×64, assembly ×12, 6 rods → 9).

### Build
1. In `metal_building.js` after line 40, tie polished bars to iron bars both ways by stonecutting:
   ```js
   R.stonecutting(Item.of(i.polished_iron_bars, 1), i.iron_bars, e, 'polished_iron_bars_from_iron_bars')
   R.stonecutting(Item.of(i.iron_bars, 1), i.polished_iron_bars, e, 'iron_bars_from_polished_iron_bars')
   ```
   so every iron-bar route (laser, assembly, rods) also feeds the polished ones, at the same cost.
2. Writeup 2.4: add a "Polished iron" row; test.

### Ids and data
`i.polished_iron_bars` (7312), `i.iron_bars` (704). `R.stonecutting(output, input, e, idOverride)`
(`neoCraftHelper.js` line 781).

### Decisions
- **Intended?** Recommend no: the removal was part of the "stonecutter bars" sweep, but this one had no replacement and
  it breaks the catwalks. Stonecutting from iron bars (not ingots) keeps the pack's "bars from rods" cost.

### Gotchas
- A stonecutter loop (A → B, B → A) is fine at 1:1; don't give either direction more than 1.

### Tests
- **RM-20 Polished bars:** stonecutter iron bars → polished iron bars and back; craft an iron catwalk.

---

## metal #6: More gold sources

Status Planned · Work KubeJS · TODO `recipes/metal #6` (scratchpad 2026-10-19, "Panning?")

### Goal
Decide whether gold needs another source, and if so add "panning".

### Now
Gold sources in the pack:
- Ore (overworld, nether gold ore), raw gold, crushed ore (the ore tables, writeup 2.2).
- Create washing already pans: `create:splashing/red_sand` → 3 gold nuggets at 12.5 % (+ 5 % dead bush),
  `splashing/soul_sand` → 2 % gold nugget, `splashing/crushed_raw_gold` → 9 nuggets (**jar**); none removed.
- Tuff crushing: 10 % gold nugget (and 10 % rose gold nugget), `resource_blocks.js` line 146.
- Ochrum: compacting 6 gold nuggets + 2 gravel + lava (line 55, a net loss when crushed back: 52 % crushed gold or 5
  nuggets), but **extruding** lava + water over a gold dust block gives ochrum for free (line 60, Create Mechanical
  Extruder), a renewable late source.
- Gilded blackstone (deploy a gold block, crush to 18 nuggets: a loss), piglins, loot (Lootr chests:
  [loot_lootr.md](loot_lootr.md) notes a drum tower with 54 gold ingots).
- Sinks: brass hand and deployers, mechanical crafters (4 ingots), redstone mechanism plates, powered rails, golden
  food, electrum.
- No sifting mod is installed (`createsifter` is only named in a dead `replaceOutput`, metal #4).

### Build
Recommended, KubeJS only, in `misc_metal_crafts.js` or a "Panning" section of `resource_blocks.js`:
1. Raise early panning a little: gravel washing with a gold chance. Create's fan uses the first matching recipe, so
   **replace** Create's `create:splashing/gravel` (25 % flint, 12.5 % iron nugget, **jar**) rather than adding a second:
   `R.remove({ id: 'create:splashing/gravel' }, e)` and
   `R.createSplashing([Item.of(i.flint).withChance(0.25), Item.of(i.nugget_iron).withChance(0.125), Item.of(i.nugget_gold).withChance(0.05)], i.gravel, e, 'gravel_panning')`
   (`_validateAny` walks arrays, so an array output validates).
2. Optionally the same for `minecraft:sand`: replace `create:splashing/sand` (25 % clay ball, **jar**) with clay 25 % +
   gold nugget 3 %, keeping red sand as the best pan.
3. A hand gold pan (new item, `ItemEvents.rightClicked` on water with gravel in the off hand) only if early game gold
   is still short after 1-2 sessions of play; that is new content (texture, item, script), out of this row's KubeJS
   scope.

### Ids and data
`i.gravel` (1332), `i.sand` (1335), `i.red_sand` (1336), `i.nugget_gold` (883), `i.flint`, `i.nugget_iron` (855),
`i.clay_ball` (check the key name in the dict). Create recipe ids `create:splashing/gravel`, `create:splashing/sand`
(**jar**).
`R.createSplashing(output, input, e, idOverride)` takes an array output (it passes it to
`e.recipes.create.splashing`).

### Decisions
- **Whether?** Recommend a small yes: gravel and sand panning at 5 %, after metal #3 (which removes rose gold as a
  gold stand-in and so raises demand for real gold). Re-check after a play session.

### Gotchas
- Two splashing recipes on one input clash like the crushing ones (`recipes/engineering #2`); always replace Create's.
- Don't give soul sand more gold: it is already a quartz source.

### Tests
- **RM-21 Panning:** wash 64 gravel → about 3 gold nuggets, flint and iron nuggets as before.

---

## Built, needs play

None in this subfamily (no Testing / Complete rows).

## Writeup corrections made

- [recipes_metal.md](../recipes_metal.md) "Files to open": header comment is lines 3-29 and partly stale (was "3-41,
  still accurate").
- [recipes_metal.md](../recipes_metal.md) 2.5 Rust and RM-13: washing turns clean blocks rusty, not the reverse.
- [recipes_metal.md](../recipes_metal.md) F1: Vintage Improvements' `pressing/<metal>_ingot` recipes are live and make
  removed VI sheets.
- [recipes_metal.md](../recipes_metal.md) F4 and 2.3 Rod row: IE's iron / aluminium hand rods give 4 (not 1), and
  Immersive Posts' `has_<metal>_rod` covers the other six, so every base metal has a hand rod.
- [recipes_metal.md](../recipes_metal.md) F5: MCA's own ingot and dust gold tags added; "beacon bases" claim corrected
  (block tag, untouched).
