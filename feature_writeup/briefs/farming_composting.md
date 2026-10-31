# farming / composting: implementation brief

Written 2026-10-21 from [TODO](../todo/TODO.md) `# farming`, Sub `composting`. Feature writeup:
[composting.md](../composting.md) (test prefix `CM-`); the soil recipes are also documented in
[recipes_wood.md](../recipes_wood.md) 2.3 (`RW-`).

## Scope and order

| Row | Change | Status | Size |
|---|---|---|---|
| #2 | Nulch recipe into the compost file | Planned | small, KubeJS only |
| #4 | Seed milling to bone meal | Planned | small, KubeJS only |
| #3 | Composters in more wood types | Planned | medium: new blocks in patcherMod + textures + KubeJS recipes |

Build order:

1. **#2 and #4 together.** #2 creates the new server file `kubejs/server_scripts/wood/compost.js`; #4 puts its
   milling recipe in that file. One `/reload` to test both.
2. **#3 on its own.** It needs a patcherMod build (new blocks), textures and a restart, so it rides with the next
   patcherMod release. TODO lists it as KubeJS. That's wrong: KubeJS 6 can't register a `ComposterBlock` (see #3 Now).

Nothing here depends on other families. Related rows: `tooling/conventions #2` (the 55 duplicate keys in
`item_dict.js`, among them `mulch_*` x4, which sit in the compost block that #2 touches), `recipes/wood #2` (leftover
comments; the `sawdust.js` comment goes away with #2), `recipes/wood #3` (mulberry wood removal; it doesn't change the
#3 species list, because mulberry isn't in it).

---

## composting #2: Nulch recipe into the compost file
Status Planned / Work KubeJS / TODO `farming/composting #2` ([composting.md](../composting.md) F4)

### Goal
Move the compost, fertile-soil and nulch recipes out of `sawdust.js` into a file of their own and drop the author's
note (`//****...Integrate nulch, move to compost file`). Recipes and ids stay the same.

### Now
- [kubejs/server_scripts/wood/sawdust.js](../../kubejs/server_scripts/wood/sawdust.js):
  - lines 10-12, inside the `R.bulk('remove', ...)` at 9-18: removes `farmersdelight:organic_compost_from_rotten_flesh`,
    `farmersdelight:organic_compost_from_tree_bark` and `immersive_weathering:mulch_block`. These belong to compost too.
  - lines 60-81: compost (`i.compost` = `farmersdelight:organic_compost`) and fertile soil (`i.fertile_soil` =
    `immersive_weathering:mulch_block`), as `R.shaped` and `R.createMixing`, every one with an `idOverride`
    (`compost_manual_only`, `compost_mixing`, `fertile_soil_var_manual_only`, ... `fertile_soil_mixing_waste`).
  - line 83: the note. Line 84: `R.remove({ id: 'immersive_weathering:nulch_block' }, e)`. Line 85:
    `R.shapeless(Item.of(i.nulch, 6), [3 netherrack, 2 #immersive_weathering:scales, ash, magma cream, nether wart,
    blaze powder], e, 'nulch_manual_only')`. Line 86: `R.createMixing(... 2 netherrack, 1 scale ...)`, id `nulch_mixing`.
- Resulting ids: `creativeengineering:shaped/compost_manual_only`, `creativeengineering:createmixing/nulch_mixing`,
  `creativeengineering:shapeless/nulch_manual_only` and so on (`NCH.shaped` / `shapeless` / `createMixing` build
  `<modId>:<type>/<idSuffix>`, [neoCraftHelper.js](../../kubejs/startup_scripts/func/neoCraftHelper.js) 698, 722, 1266).
- Mulch tags `forge:mulch/{brown,bone,waste,green,fungus,fertilizer}` are filled in
  [tags/wood_tags.js](../../kubejs/server_scripts/tags/wood_tags.js) 12-35 with raw `e.add` (not `global.tags`).
- No server script file holds compost recipes today. `startup_scripts/compost/` is the composter *table*
  (`global.compost`), not recipes.
- Verified against `immersive_weathering-1.19.2-1.2.10-forge.jar`: `data/immersive_weathering/recipes/nulch_block.json`
  (shapeless: netherrack, magma cream, 2 scales, ash layer block, nether wart -> 3 nulch) and `mulch_block.json` exist.
  So both removals hit real ids.

### Build
1. Create `kubejs/server_scripts/wood/compost.js` (next to `sawdust.js`, `bark.js` and `ash.js`, which make its inputs;
   recipes_wood.md already covers "mulch soils"). Skeleton:
   ```js
   // Compost, fertile soil (IW mulch block) and nulch from the mulches, bark and nether scraps.
   // Composter table: startup_scripts/compost/. Writeups: composting.md, recipes_wood.md 2.3.
   ServerEvents.recipes(e => {
   	const R = global.NCH
   	var i = global.item
   	R.bulk('remove', [
   		[{ id: 'farmersdelight:organic_compost_from_rotten_flesh' }, e],
   		[{ id: 'farmersdelight:organic_compost_from_tree_bark' }, e],
   		[{ id: 'immersive_weathering:mulch_block' }, e],
   		[{ id: 'immersive_weathering:nulch_block' }, e],
   	])
   	// compost + fertile soil: sawdust.js 60-81 moved verbatim
   	// nulch: sawdust.js 85-86 moved verbatim
   	// seed milling: composting #4
   })
   ```
2. Cut `sawdust.js` lines 10-12, 60-81 and 83-86 into it. Keep every `idOverride` exactly, so recipe ids, JEI
   bookmarks and any advancement or quest that names them stay valid. `sawdust.js` keeps sawdust, ash, mud-to-dirt,
   creosote and pellets.
3. Drop the `//****...` note. "Integrate" is already done: the pack's nulch recipes replace IW's.
4. Optional, same touch: move the `forge:mulch/*` adds in `wood_tags.js` to `global.tags.add(e, 'forge:mulch/brown',
   [i.sawdust, i.straw])` form (CLAUDE.md convention). Leave the duplicate `mulch_*` dict keys to
   `tooling/conventions #2`.
5. Update the docs: [composting.md](../composting.md) F4 (resolved), 0 (compost and fertile soil crafting "in
   `sawdust.js`"), and 5 (file map "Related recipes"); [recipes_wood.md](../recipes_wood.md) 2.3 and file map, plus F5
   (the `sawdust.js` comment is gone); [crops.md](../crops.md) about line 761 (links `sawdust.js` for the Fertile Soil
   recipes).

### Ids and data
No new ids. All keys exist in [item_dict.js](../../kubejs/startup_scripts/_lib/item_dict.js): `i.compost` (581),
`i.fertile_soil` / `i.nulch` (641-642), `i.rich_soil`, `i.dirt`, `i.any_bark`, `i.any_scale` (582), `i.ash` (1345,
`supplementaries:ash`), `i.netherrack` (1325), `i.blaze_powder` (1331), `i.magma_cream`, `i.nether_wart`, `i.mulch_*`.

### Decisions
- **Folder:** `wood/compost.js` (recommended) or `other/compost.js`. There is no `farming/` server folder. `wood/` keeps
  the soils next to their inputs and matches recipes_wood.md. Recipe order across files doesn't matter.

### Gotchas
- `R.bulk('remove', ...)` in two files is fine. Don't leave a removed id in both files, or the log will show duplicates.
- The startup engine needs `/kubejs reload startup_scripts`, but this row is server only: `/reload` is enough.

### Tests
- **CM-09 Recipes moved:** after `/reload`, JEI shows compost (3x3 and mixer), fertile soil x6 / x3 (all 12 variants) and
  nulch x6 (shapeless and mixer) with ids unchanged (`creativeengineering:shaped/compost_manual_only`, ...); IW's own
  nulch and mulch recipes and FD's two compost recipes are still gone; no `[NCH]` warnings in the log.

---

## composting #4: Seed milling to bone meal
Status Testing (built 2026-10-30, `wood/compost.js`, SQ-CP-01 queued) / Work KubeJS / TODO `farming/composting #4` (scratchpad 2026-10-21; [composting.md](../composting.md) 4)

### Goal
A Create millstone (and so crushing wheels) turns seeds into bone meal at a small chance. That gives seeds an automated
sink that's worse than the composter. The scratchpad sets the rate: 32 seeds to 1 bone meal.

### Now
- No seed-input milling or crushing recipe in the pack except corn kernels:
  [cooking.js](../../kubejs/server_scripts/cooking/cooking.js) 112 (`createCrushing`, `#forge:seeds/corn` -> 0.333
  cornmeal, id `corn_kernels_cornmeal`) and 119 (`createMilling`, same id). Mods: of the cached jars (Create, Farmer's
  Delight, Corn Delight, Delightful, Fruits Delight, Rustic Delight and others in `scratchpad/jars`), none has a
  milling, crushing or IE-crusher recipe whose *input* is a seed. Create's wheat, grass and flax milling only *output*
  seeds. Other mods weren't checked.
- Composter rate for comparison: seeds compost at `low` = 0.3 (compost_rules.js 26-36; values rows 0.3 for vanilla
  seeds, but **1.0 for Botania's seeds**, compost_values.js 20-35). Vanilla's composter always takes the first item into
  an empty composter, then rolls the chance for the other 6 layers, so 1 + 6 / 0.3 = **about 21 seeds per bone meal**.
  32 per bone meal on the mill is worse, as asked.
- Tags, verified against the jars: `#forge:seeds` holds `corn_delight:corn_seeds` (the pack's corn kernels, Corn Delight
  `data/forge/tags/items/seeds.json`) and, through `#forge:seeds/rice`, `farmersdelight:rice` (FD
  `data/forge/tags/items/seeds.json` + `seeds/rice.json`). Rice is a cooking ingredient and kernels already mill into
  cornmeal, so both must be left out.
- Create, verified (create-1.19.2-0.5.1.i, `CrushingWheelControllerBlockEntity.findRecipe`): crushing wheels try
  CRUSHING first and fall back to MILLING. A milling recipe therefore also runs on crushing wheels, except for an item
  that has a crushing recipe (corn kernels).
- IE crusher: skip. It can't roll its main output (comment at cooking.js 120-121), so it can't do a 1/32 chance per seed.

### Build
1. Dict keys ([item_dict.js](../../kubejs/startup_scripts/_lib/item_dict.js), "Compost rule tags" block at ~1916, `t_`
   prefix for tags):
   `i.t_millable_seeds = '#creativeengineering:millable_seeds'; // seeds the millstone turns into bone meal (composting #4)`
   and `i.t_seeds_rice = '#forge:seeds/rice';`. `i.t_seeds`, `i.t_seeds_c`, `i.t_seeds_corn` (1889) and `i.bone_meal`
   (1344) exist.
2. Tag (server, `ServerEvents.tags('item')`, in [tags/wood_tags.js](../../kubejs/server_scripts/tags/wood_tags.js)
   next to the mulch tags, through `global.tags`):
   ```js
   global.tags.addMatching(e, 'creativeengineering:millable_seeds', [i.t_seeds, i.t_seeds_c], {
   	except: [i.t_seeds_corn, i.t_seeds_rice],   // kernels mill into cornmeal; rice is food
   })
   ```
   `addMatching` with `except` expands the `#` entries to their members at that point (tag_helper.js). That's fine here:
   mod seed tags come from datapacks, which load before KubeJS's tag event. If a later pack script adds to
   `#forge:seeds`, it won't reach the copy. Note that in a comment.
3. Recipe in `wood/compost.js` (from #2):
   ```js
   // Seeds -> bone meal: 1 in 32 (the composter averages ~21 seeds per bone meal). composting #4
   R.createMilling(Item.of(i.bone_meal).withChance(0.03125), i.t_millable_seeds, e, 'seeds_bone_meal')
   ```
   This gives id `creativeengineering:createmilling/seeds_bone_meal`. Create milling JSON: `{"type":"create:milling",
   "ingredients":[{"tag":"creativeengineering:millable_seeds"}],"results":[{"item":"minecraft:bone_meal","chance":0.03125}]}`.
   An all-chance result list is allowed: a failed roll gives nothing and the seed is gone. Milling time defaults
   (KubeJS Create sets it from the recipe; add `.processingTime(n)` only if the default reads wrong in JEI).
4. Add a line under composting.md 4 ("Planned" bullet -> built) and a recipe row in 2 or 3.

### Ids and data
- New tag `creativeengineering:millable_seeds`. New dict keys `t_millable_seeds`, `t_seeds_rice`.
- Recipe `creativeengineering:createmilling/seeds_bone_meal`. Mod: Create (KubeJS Create schema).

### Decisions
- **Edible seeds:** some mods tag edible seeds as `#forge:seeds`. Recommend: after the first `/dumptags item`, read
  `kubejs/exported/tags` for `creativeengineering:millable_seeds` and add any food item to `except` by id. Don't add
  code that checks edibility.
- **Botania seeds** (compost at 1.0): leave them in if they're tagged. The mill is still worse than the composter.
- **Also on IE crusher:** no (see Now).

### Gotchas
- Two milling recipes on one item: Create picks one of them in no fixed order. Before shipping, check in JEI (uses of
  wheat seeds, and of a few modded seeds) that no other milling recipe takes a member of the tag.
- The mill takes seeds one at a time. Crushing wheels roll the chance once per item of the stack.

### Tests
- **CM-10 Seed milling:** a millstone fed 320 wheat seeds -> about 10 bone meal (5 to 16 is normal); corn kernels still
  give cornmeal, never bone meal; rice isn't accepted (it isn't in the tag). JEI shows the recipe at 3.13 %.
- **CM-11 Crushing wheels:** a stack of beetroot seeds dropped into crushing wheels -> bone meal at the same rate.

---

## composting #3: Composters in more wood types
Status Planned / Work KubeJS (wrong, see Gotchas) / TODO `farming/composting #3` (scratchpad 2026-10-19: "give
composters more wood types", under "other, migrate elsewhere")

### Goal
A composter per wood species. Crafting it from that wood's slabs gives that wood's look. It works exactly like the
vanilla composter: same chance table, hoppers, comparator, bone meal output.

### Now
- Only `minecraft:composter` exists. Vanilla recipe (verified, 1.19.2 server jar `data/minecraft/recipes/composter.json`):
  shaped `"# #","# #","###"`, `#` = `#minecraft:wooden_slabs`, so any slab gives the oak-looking composter.
- No mod in the pack adds composter variants. Every Compat 2.5.36 has no composter module (verified: no
  composter class or asset in `everycomp-1.19.2-2.5.36.jar`; its module list is furniture, doors, Quark, Create and
  others). Quark 3.4-418 and Supplementaries 2.4.20 have none either (verified). The repo has no `*_composter` id.
- KubeJS 6 (1.19.2) block types (`basic`, `slab`, `stairs`, `crop`, `cardinal`, ...) have no composter type. A KubeJS
  block wouldn't be a `ComposterBlock`, so it wouldn't take items, levels or hoppers. The blocks therefore go in patcherMod,
  and only the recipes are KubeJS.
- patcherMod pattern to copy: [oven/OvenRegistry.java](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/oven/OvenRegistry.java)
  (own `DeferredRegister<Block>` / `<Item>`, `BlockItem` in `CreativeModeTab.TAB_DECORATIONS`, `register(modEventBus)`
  called from `CreativeEngineering.java` near line 95). Assets in
  `development/patcherMod/src/main/resources/assets/creativeengineering/{blockstates,models,textures,lang}`, loot in
  `data/creativeengineering/loot_tables/blocks/` (the oven has one).
- Species: the 13 in [wood_types.js](../../kubejs/server_scripts/wood/wood_types.js) 9-23 (`species` table, slab
  keys `i.slab_<name>` at item_dict.js 199-367): oak, spruce, birch, dark_oak, jungle, acacia, mangrove, crimson, warped,
  twisted (`architects_palette:twisted_slab`), ancient / blossom / azalea (`quark:<name>_planks_slab`).
- Vanilla composter loot (verified, `loot_tables/blocks/composter.json`): drops itself (explosion decay) and, when
  `level` = 8, one bone meal.
- Other places that name the composter: [item_physics_tags.js](../../kubejs/server_scripts/tags/item_physics_tags.js)
  151 (`'composter'` in the floats list); `config/waila/waila_plugins.json5` 231 (`level.composter`: WTHIT's
  composter level line, probably by `ComposterBlock` class, unverified).

### Build
1. **patcherMod `block/WoodComposters.java`** (new, registered like the oven):
   ```java
   public final class WoodComposters {
       // species with a composter of their own; oak stays minecraft:composter
       static final String[] OVERWORLD = {"spruce","birch","dark_oak","jungle","acacia","mangrove","twisted","ancient","blossom","azalea"};
       static final String[] NETHER = {"crimson","warped"};
       // for each: BLOCKS.register(name + "_composter", () -> new WoodComposterBlock(Properties.copy(Blocks.COMPOSTER), flammable))
       //           ITEMS.register(name + "_composter", () -> new WoodComposterItem(block, props.tab(TAB_DECORATIONS), flammable ? 300 : 0))
   }
   ```
   - `WoodComposterBlock extends ComposterBlock` (its constructor is public). It inherits `use`, `getContainer`
     (WorldlyContainerHolder, so hoppers work), comparator output and the `level` property. Override
     `getFlammability` / `getFireSpreadSpeed` (IForgeBlock) to 5 / 20 for overworld woods (vanilla registers the
     composter in `FireBlock.bootStrap`, which won't cover new blocks) and 0 for crimson / warped.
   - `WoodComposterItem extends BlockItem`, overriding `getBurnTime(ItemStack, RecipeType)` -> 300 (vanilla
     composter fuel) or 0 for the nether woods.
2. **Assets** (patcherMod resources):
   - `blockstates/<wood>_composter.json`: copy vanilla's multipart `composter.json` from the client jar. Change only the
     base model to `creativeengineering:block/<wood>_composter` and keep `minecraft:block/composter_contents1..7` /
     `composter_contents_ready` for the fill.
   - `models/block/<wood>_composter.json`: `{"parent":"minecraft:block/composter","textures":{"particle":...,"top":...,
     "bottom":...,"side":...,"inside":...}}`. Read the texture slot names from vanilla's `models/block/composter.json`
     (not checked here: only the server jar was opened). `models/item/<wood>_composter.json` with the block model as
     parent.
   - Textures: `textures/block/<wood>_composter_{side,top,bottom}.png` for 12 woods. Recommend a generator
     `tools/gen_composter_textures.py`: palette-map vanilla `composter_side/top/bottom` (oak tones) onto each species'
     planks palette, as Every Compat does. Sources are vanilla `oak_planks` and the species' planks texture from the
     jars (Quark, Architect's Palette, vanilla). Fetch with `fetch_mod.py`, and get the client jar for vanilla.
   - `lang/en_us.json`: `block.creativeengineering.<wood>_composter` = "Spruce Composter" etc.
3. **Data:** `data/creativeengineering/loot_tables/blocks/<wood>_composter.json`: the vanilla table with the block and
   item names swapped (bone meal pool condition `"block": "creativeengineering:<wood>_composter"`, `level` 8).
4. **Dicts:** item_dict.js next to each species block (style `i.<form>_<species>`, e.g. after `i.slab_spruce`):
   `i.composter_spruce = 'creativeengineering:spruce_composter';` ... and `i.composter_oak = i.composter` (the
   `minecraft:composter` key at 2145). [block_dict.js](../../kubejs/startup_scripts/_lib/block_dict.js): `b.composter_spruce`
   ... (for the tags in step 5). New tag keys: `i.t_composters = '#creativeengineering:composters'`,
   `i.t_composter_slabs = '#creativeengineering:composter_slabs'`.
5. **Tags** (`global.tags`): block tags `minecraft:mineable/axe` (`global.tags.mineable(e, 'axe', null, [...])`) and
   `creativeengineering:composters` (block + item, holding `minecraft:composter` and the 12). Item physics: add
   `'#creativeengineering:composters'` to the `physicsFromTag(e, 'floats', ...)` list in item_physics_tags.js 126
   (crimson / warped: check how `#minecraft:non_flammable_wood` is treated there and match it).
6. **Recipes:** add a `composter` field to each row of the `species` table in wood_types.js (`composter:
   i.composter_spruce`, oak `i.composter`). Then:
   ```js
   R.remove({ id: 'minecraft:composter' }, e)
   R.bulk('shaped', species.map(s => [s.composter, ['S S', 'S S', 'SSS'], { S: s.slab }, e, s.name + '_composter']))
   // other wooden slabs (bamboo, Botania woods, ...) still make the plain composter
   R.shaped(i.composter, ['S S', 'S S', 'SSS'], { S: i.t_composter_slabs }, e, 'composter_other_slabs')
   ```
   with the tag in the item-tag event:
   `global.tags.addMatching(e, 'creativeengineering:composter_slabs', '#minecraft:wooden_slabs', { except: species slabs except oak })`.
   Write the except list from the dict keys `i.slab_spruce` ..., not as literal ids. Optional: a shapeless "any
   composter + slab of a wood -> that wood's composter" re-skin. Not needed.

### Ids and data
- New blocks and items (patcherMod, namespace `creativeengineering`): `spruce_composter`, `birch_composter`,
  `dark_oak_composter`, `jungle_composter`, `acacia_composter`, `mangrove_composter`, `crimson_composter`,
  `warped_composter`, `twisted_composter`, `ancient_composter`, `blossom_composter`, `azalea_composter`.
- Recipes `creativeengineering:shaped/<wood>_composter` (13, oak gives `minecraft:composter`),
  `creativeengineering:shaped/composter_other_slabs`; `minecraft:composter` removed.
- Tags `creativeengineering:composters` (item + block), `creativeengineering:composter_slabs` (item).
- Verified: vanilla recipe and loot JSON (server jar 1.19.2), no variant composters in Every Compat / Quark /
  Supplementaries. Unverified: vanilla composter model texture slots and blockstate (client jar), WTHIT's handling,
  and the villager points below.

### Decisions
- **Species:** recommend the 12 non-oak species of the `species` table (the woods the pack treats as its own; the
  same list as the Create saw shapes). Bamboo, Botania and other woods keep the plain composter through
  `composter_other_slabs`.
- **Farmer job site:** vanilla ties the farmer's job site to `Blocks.COMPOSTER` (the `PoiTypes.FARMER` states), and
  the farmer's `WorkAtComposter` behaviour checks `is(Blocks.COMPOSTER)` (from vanilla source, not checked against a
  mapped jar). So variants would **not** be job sites, and farmers wouldn't compost into them. Recommend shipping them
  as non-job-site blocks first and noting it in the tooltip or the writeup. If wanted later, it takes a patcherMod
  mixin on how the FARMER POI matches states (Forge 1.19.2 builds `PoiTypes` state lookups from each `PoiType`'s
  `matchingStates`) plus one on `WorkAtComposter`. Decompile Forge's `PoiTypes` / `GameData` POI callback first. MCA
  Reborn villagers may have their own checks.
- **Textures:** generated (recommended, repeatable for new woods) or hand-drawn.

### Gotchas
- TODO says Work = KubeJS / "Recipes per wood". It's patcherMod + resources + KubeJS (proposed TODO correction).
- The pack's composter table (`ComposterBlock.COMPOSTABLES`) is static and shared, so variants take exactly the same
  items. Nothing to change in `global.compost`.
- Keep the vanilla recipe removed. Otherwise a grid of spruce slabs matches both `minecraft:composter` and
  `spruce_composter`, and the result depends on recipe order.
- Create funnels, chutes and Little Logistics hoppers on a variant: test them. Most use the Forge item handler, which
  Forge wraps around `WorldlyContainerHolder` for vanilla hoppers. Other mods' code may check `Blocks.COMPOSTER`.
- Structures that place `minecraft:composter` are unaffected.

### Tests
- **CM-12 Wood composters:** craft a spruce composter from 7 spruce slabs (JEI shows the 13 wood recipes and the
  other-slab one); fill it with seeds by hand and from a hopper; the comparator reads the level; at full it gives bone
  meal and a hopper below pulls it; breaking it full drops the composter and a bone meal.
- **CM-13 Fuel and fire:** a birch composter burns in a furnace (300 ticks); fire spreads to it; a crimson composter
  doesn't burn.
- **CM-14 Other slabs:** 7 bamboo slabs -> plain `minecraft:composter`.
- **CM-15 Farmer (decision):** an unemployed villager next to a spruce composter doesn't become a farmer (or does, if
  the job-site mixin is built).

---

## Built, needs play

| Row | Change | Status | Notes |
|---|---|---|---|
| composting #1 | Composting tests and rule checks | Complete | Marked Complete, but [composting.md](../composting.md) 7 has CM-01 to CM-08 all unchecked and the writeup and README say "pending"; looks like it should be Testing (or the boxes ticked). The "Brewin' 4.5.0 compost values" part is done ([alcohol.md](../alcohol.md) 10.16: the port's four chances are rows in `compost_values.js`, now 688 values), built 2026-10-19 but not in the deployed 1.0.9 (the deploy log's kubejs list doesn't have `compost_values.js`; committed in c82dd31, 2026-10-20) |

## Writeup corrections made

- [composting.md](../composting.md) 0: values count 684 -> 688, with the 4 Brewin' port rows marked built, not deployed;
  "Pending: nothing built and unshipped" -> names those 4 rows.
- [composting.md](../composting.md) 2.3: "684 items" -> "688 items" (the header of `compost_values.js` says 688).
- [composting.md](../composting.md) 8: added the 2026-10-19 history line for the Brewin' rows.
- [README.md](../README.md) composting row: "684 per-item values" -> "688 per-item values".
