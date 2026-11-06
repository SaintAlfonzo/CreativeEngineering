# farming / composting: implementation brief

Written 2026-10-21 from [TODO](../todo/TODO.md) `# farming`, Sub `composting`. Feature writeup:
[composting.md](../composting.md) (tests CM-01 to CM-08 and CP-01; CM-09 and up are now creative_delight.md's ids, so
new composting tests take the CP- prefix); the soil recipes are also documented in [recipes_wood.md](../recipes_wood.md)
2.3 (`RW-`). Updated 2026-10-31 (brief sweep): #2 re-scoped to the owner's recipe review, #5 added, #4 built and shipped
(moved to Built, needs play), file and line refs re-checked. Proposed test ids are placeholders `CP-new1`, ... (numbered
when queued).

## Scope and order

| Row | Change | Status | Size |
|---|---|---|---|
| #2 | Fertile Soul Soil (nulch) recipe: review, then move into the compost file | Planned | small, KubeJS only; needs the owner's pick of inputs |
| #5 | Expand organic compost (more rot items, more dirt blocks) | Planned | small, KubeJS only (tags + 4 recipe edits) |
| #3 | Composters in more wood types | Planned | medium: new blocks in patcherMod + textures + KubeJS recipes |
| #4 | Seed milling to bone meal | Testing | built 2026-10-30, shipped in 1.1.05 (Built, needs play) |

Build order:

1. **#5 and #2 together.** `kubejs/data/creativeengineering/kubejs/wood/compost.js` already exists (9 lines, made by #4). #2 moves the
   compost, fertile soil and nulch recipes into it from `sawdust.js`; #5 edits the compost recipes on the way. One
   `/reload` for both. #5 does not wait for #2's recipe review: move the compost block first, nulch when its inputs are
   picked.
2. **#3 on its own.** It needs a patcherMod build (new blocks), textures and a restart, so it rides with the next
   patcherMod release.

Nothing here depends on other families. Related rows: `recipes/wood #2` (leftover comments; the `sawdust.js` note goes
away with #2), `recipes/wood #3` (mulberry wood removal; mulberry isn't in #3's species list).

---

## composting #2: Fertile Soul Soil (Nulch) recipe: review, then move into the compost file
Status Planned / Work KubeJS / TODO `farming/composting #2` ([composting.md](../composting.md) F4). Decision: the new
inputs (owner, 2026-10-23).

### Goal
Fertile Soul Soil (`immersive_weathering:nulch_block`, renamed 2026-10-23) gets a recipe that fits its place: the Nether
counterpart of Fertile Soil and a ghost pepper soil. Then it and the compost / fertile soil recipes move out of
`sawdust.js` into `wood/compost.js`, and the author's note goes.

### Now
- [kubejs/data/creativeengineering/kubejs/wood/sawdust.js](../../kubejs/data/creativeengineering/kubejs/wood/sawdust.js) (89 lines, re-checked
  2026-10-31):
  - lines 10-12, inside the `R.bulk('remove', ...)` at 9-18: removes `farmersdelight:organic_compost_from_rotten_flesh`,
    `farmersdelight:organic_compost_from_tree_bark` and `immersive_weathering:mulch_block`.
  - lines 60-81: compost (`i.compost` = `farmersdelight:organic_compost`) and fertile soil (`i.fertile_soil` =
    `immersive_weathering:mulch_block`), `R.shaped` and `R.createMixing` with `idOverride`s (`compost_manual_only`,
    `compost_bark_manual_only`, `compost_mixing`, `compost_mixing_bark`, `fertile_soil_var_manual_only`, ...
    `fertile_soil_mixing_waste`).
  - line 83: the note. 84: `R.remove({ id: 'immersive_weathering:nulch_block' }, e)`. 85: `R.shapeless(Item.of(i.nulch,
    6), [3 netherrack, 2 i.any_scale, ash, magma cream, nether wart, blaze powder], e, 'nulch_manual_only')`. 86:
    `R.createMixing(... 2 netherrack, 1 scale ...)`, id `nulch_mixing`.
- [kubejs/data/creativeengineering/kubejs/wood/compost.js](../../kubejs/data/creativeengineering/kubejs/wood/compost.js): one recipe, seed milling (#4).
- The Fertile Soil ladder (what the review compares against): FD organic compost (pack recipe: dirt + rot / bone /
  brown mulch, `sawdust.js` 61-65) -> rich soil -> **Fertile Soil**: 3 rich soil + one of each of the six mulches -> 6, or
  3 rich soil + 3 of one mulch + green, fungus, fertilizer -> 3 (67-81). Mulch tags `forge:mulch/{brown,bone,waste,
  green,fungus,fertilizer}` in [tags/wood_tags.js](../../kubejs/data/creativeengineering/kubejs/tags/wood_tags.js) 11-16, already through
  `global.tags` (the 2026-10-21 optional step is done).
- The Nether ladder today: Nether's Delight soul compost (its own two recipes, not touched by the pack, verified in
  `NethersDelight-1.19-3.1.jar`: soul soil + 2 twisting vines + 4 bone meal + 2 hoglin hide, or + 4 warped roots) matures
  into **rich soul soil** (`nethersdelight:rich_soul_soil`; ND ships no crafting recipe for it). Fertile Soul Soil is
  made from netherrack, not from rich soul soil, so it skips the ladder.
- Its jobs: ghost pepper soil (`#vintagedelight:ghost_pepper_soil` = soul soil, rich soul soil, nulch,
  `kubejs/data/creativedelight/kubejs/vintagedelight/tags.js` 48); tooltip "Grows nether crops faster"
  (`client_scripts/tooltips.js` 498); name and planting line `client_scripts/namechanges.js` 245-249; compost value 1.0
  (Creative Delight `kubejs_startup/immersive_weathering/compost_values.js` 95).
- Recipe ids come out as `creativeengineering:<type>/<idSuffix>` (`NCH.shaped` 798, `shapeless` 822, `createMixing`
  1665 in [neoCraftHelper.js](../../development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/func/neoCraftHelper.js)).

### Build
1. **Review (owner).** Proposed recipe, mirroring Fertile Soil one rung up the Nether ladder:
   - shaped `SSS / ROW / GFZ` -> 6 Fertile Soul Soil, `S` = rich soul soil, the six slots from Nether mulch tags
     (`creativeengineering:nether_mulch/<slot>`, through `global.tags.add` in `wood_tags.js`):
     brown = `i.any_scale` (IW crimson / warped scales, the stems' bark), bone = `forge:mulch/bone`, waste = rotten
     flesh / hoglin hide, green = nether wart, warped wart block, twisting / weeping vines, fungus = crimson / warped
     fungus, roots, fertilizer = ash, blaze powder, magma cream;
   - the 3-output "3 of one mulch" variants and `createMixing` copies with 2 rich soul soil, as Fertile Soil has.
   - Drop the netherrack recipe (85-86): netherrack-based soil undercuts rich soul soil.
2. Create the tags and dict keys: `i.rich_soul_soil = 'nethersdelight:rich_soul_soil'` (only `b.rich_soul_soil`
   exists, block_dict.js 85), `i.hoglin_hide = 'nethersdelight:hoglin_hide'`, `i.nether_mulch_<slot> =
   '#creativeengineering:nether_mulch/<slot>'`; `i.twisting_vines` (2835), `i.weeping_vines` (2867),
   `i.crimson_fungus` (2246), `i.warped_fungus` (2842), `i.crimson_roots` (2248), `i.warped_roots` (2845),
   `i.nether_wart_block` (2583), `i.warped_wart_block` (2846) exist.
3. **Move** into `wood/compost.js`: the three removals (10-12) plus 84, the compost and fertile soil block (60-81) with
   every `idOverride` unchanged, and the new nulch recipes (ids `nulch_manual_only`, `nulch_mixing` kept for the main
   pair so JEI bookmarks survive; `nulch_<slot>_manual_only` etc. for the variants). Drop the note (83).
4. Docs: composting.md F4 resolved, 3 and 5 (file map "Related recipes" -> `wood/compost.js`); recipes_wood.md 2.3 and
   file map, F5; crops.md about line 761 (links `sawdust.js` for the Fertile Soil recipes). Changelog bullet (Fertile
   Soul Soil recipe changed, old inputs in brackets).

### Ids and data
Keys exist: `i.compost` (639), `i.fertile_soil` / `i.nulch` (677-678), `i.rich_soil` (675), `i.dirt` (680),
`i.any_bark` (638), `i.any_scale` (640), `i.mulch_*` (632-637), `i.ash` (1292, `supplementaries:ash`), `i.netherrack`
(1275), `i.blaze_powder` (1279), `i.magma_cream` / `i.nether_wart` (641-642), `i.soul_soil` (1289), `i.bonemeal`
(1291). New: `i.rich_soul_soil`, `i.hoglin_hide`, the `nether_mulch` tag keys. ND ids verified in the jar.

### Decisions
- **Inputs:** recommend the rich-soul-soil + Nether mulch recipe above (owner's "a Nether mulch?"). Simpler fallback:
  keep netherrack, swap it for rich soul soil 1 : 1 and keep the five other inputs.
- **Soul compost:** leave ND's two recipes as they are (recommended; ND's own ladder, and #5 only covers FD compost).
- **Folder:** `wood/compost.js` (exists).

### Gotchas
- Don't leave a removed id in both files (duplicate removal warnings).
- Server only: `/reload` is enough; the new dict keys are startup (restart) unless added before the session.
- Fertile Soul Soil's compost value (1.0) and the ghost pepper tag need no change.

### Tests
- CP-new1 Recipes moved: after `/reload`, JEI shows compost (3x3 and mixer), fertile soil x6 / x3 (all 12 variants) with
  ids unchanged; IW's mulch / nulch recipes and FD's two compost recipes stay gone; no `[NCH]` warning.
- CP-new2 Fertile Soul Soil: JEI on it shows only the rich-soul-soil recipes (crafting and mixer); a ghost pepper seed
  plants on it.

---

## composting #5: Expand organic compost
Status Planned / Work KubeJS / TODO `farming/composting #5` (scratchpad 2026-10-31; [composting.md](../composting.md))

### Goal
The pack's organic compost recipes take more rot items (rotten tomato, rotten cherry, organic mash) and more dirt
blocks (any dirt, podzol, moss block) in the dirt slot.

### Now
- The four compost recipes, `sawdust.js` 61-65 (moving to `compost.js` with #2): shaped `DWO / WOR / ORR` (and the bark
  version `DWO / WOB / OBB`), `D` = `i.dirt`, `W` = `i.mulch_waste` (the rot slot), `O` = `i.mulch_bone`, `R` =
  `i.mulch_brown` or `i.any_bark`; `createMixing` copies take 1 dirt + 2 each.
- `forge:mulch/waste` = rotten flesh, spider eye, poisonous potato (`wood_tags.js` 13). It is also the waste slot of
  four Fertile Soil recipes (67-81).
- The three new rot items, all with dict keys: `i.rotten_tomato` = `farmersdelight:rotten_tomato` (11652),
  `i.rotten_cherry` = `vinery:rotten_cherry` (11276; Vinery port nested in `creativebrewing-1.0.0.jar`, registered in
  `members/vinery/.../ObjectRegistry.java` 107), `i.organic_mash` = `vintagedelight:organic_mash` (14468; Vintage port,
  a byproduct of jam -> vinegar fermenting).
- Vintage Delight ships its own compost recipes that the pack does not remove (verified in
  `backportedVintage-0.1.6-ce-19.2-09.jar`): `vintagedelight:organic_compost_from_rotten_flesh_mash` and
  `organic_compost_from_tree_bark_mash` (dirt + 2 organic mash + straw + bone meal ... -> organic compost), the VD twins
  of the two FD recipes `sawdust.js` 10-11 removes. `organic_compost_tree_fertilizer` (flowers + mash -> Create tree
  fertilizer) is a different output.
- Dirt: `#minecraft:dirt` (dump `kubejs/exported/tags/item_tags.json`) = dirt, coarse dirt, grass block, moss block,
  mud, muddy mangrove roots, mycelium, podzol, rooted dirt. Keys: `i.dirt` (680), `i.coarse_dirt` (2219),
  `i.rooted_dirt` (2740), `i.podzol` (2645), `i.moss` = moss block (661).

### Build
1. Rot slot: add the three to `forge:mulch/waste` in `wood_tags.js` 13 (`global.tags.add`, dict keys). That widens the
   Fertile Soil waste slot too, which reads the same ("rot"); recommended over a second tag.
2. Dirt slot: new item tag `creativeengineering:compost_dirt` = `i.dirt`, `i.coarse_dirt`, `i.rooted_dirt`,
   `i.podzol`, `i.moss` (`global.tags.add` in `wood_tags.js`), dict key `i.t_compost_dirt`; swap `D: i.dirt` and the
   mixer's `Item.of(i.dirt, 1)` for it in the 4 compost recipes (ids unchanged).
3. Remove VD's two compost twins in the same `R.bulk('remove', ...)`
   (`vintagedelight:organic_compost_from_rotten_flesh_mash`, `..._from_tree_bark_mash`): organic mash now goes through
   the pack recipe.
4. composting.md 3 ("What the pack changes") gets a line; changelog bullet under a compost family (more rot items and
   dirt blocks in Organic Compost; Vintage Delight's own compost recipes removed).

### Ids and data
New tag `creativeengineering:compost_dirt` (item), key `i.t_compost_dirt`. Changed tag `forge:mulch/waste`. Removed
`vintagedelight:organic_compost_from_rotten_flesh_mash`, `vintagedelight:organic_compost_from_tree_bark_mash` (verified
in the jar).

### Decisions
- Grass block, mycelium, mud in the dirt slot? **Recommend no** (the row names dirt, podzol and moss; grass / mycelium
  need silk touch and mud is a different material). Using all of `#minecraft:dirt` is the one-line alternative.
- Remove VD's two compost recipes? **Recommend yes** (same reason the FD pair went).
- Moss block is also in `forge:mulch/green`: fine, it can fill either slot.

### Gotchas
- A shaped recipe with a tag in `D` and the mixer's `Item.of(tag, 1)`: pass the tag key as the ingredient
  (`global.NCH` validates `#` keys); check the `[NCH]` log line after `/reload`.
- Spoiled food items from other mods (`*_rotten`, spoiled jars) are not covered; add them by key when found.

### Tests
- CP-new3 Rot slot: JEI on rotten tomato, rotten cherry and organic mash each list the organic compost recipes (and the
  Fertile Soil waste ones); VD's two compost recipes are gone.
- CP-new4 Dirt slot: craft organic compost with podzol, coarse dirt, rooted dirt and a moss block in the dirt slot;
  grass block is refused.

---

## composting #3: Composters in more wood types
Status Planned / Work patcherMod / resource pack / KubeJS / TODO `farming/composting #3` (scratchpad 2026-10-19; refs
re-checked 2026-10-31, still no composter variant in any jar in `mods/`)

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
- patcherMod pattern to copy: [oven/OvenRegistry.java](../../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/oven/OvenRegistry.java)
  (own `DeferredRegister<Block>` / `<Item>`, `BlockItem` in `CreativeModeTab.TAB_DECORATIONS`, `register(modEventBus)`
  called from `CreativeEngineering.java` line 99). Assets in
  `development/creativeCommon/src/main/resources/assets/creativeengineering/{blockstates,models,textures,lang}`, loot in
  `data/creativeengineering/loot_tables/blocks/` (the oven has one).
- Species: the 13 in [wood_types.js](../../kubejs/data/creativeengineering/kubejs/wood/wood_types.js) 8-22 (`species` table, slab
  keys `i.slab_<name>` at item_dict.js 254-422): oak, spruce, birch, dark_oak, jungle, acacia, mangrove, crimson, warped,
  twisted (`architects_palette:twisted_slab`), ancient / blossom / azalea (`quark:<name>_planks_slab`).
- Vanilla composter loot (verified, `loot_tables/blocks/composter.json`): drops itself (explosion decay) and, when
  `level` = 8, one bone meal.
- Other places that name the composter: [item_physics_tags.js](../../kubejs/server_scripts/tags/item_physics_tags.js)
  126 (`'composter'` in the floats list opened at 102); `config/waila/waila_plugins.json5` 231 (`level.composter`: WTHIT's
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
   `minecraft:composter` key at 2234). [block_dict.js](../../kubejs/startup_scripts/_lib/block_dict.js): `b.composter_spruce`
   ... (for the tags in step 5). New tag keys: `i.t_composters = '#creativeengineering:composters'`,
   `i.t_composter_slabs = '#creativeengineering:composter_slabs'`.
5. **Tags** (`global.tags`): block tags `minecraft:mineable/axe` (`global.tags.mineable(e, 'axe', null, [...])`) and
   `creativeengineering:composters` (block + item, holding `minecraft:composter` and the 12). Item physics: add
   `'#creativeengineering:composters'` to the `physicsFromTag(e, 'floats', ...)` list in item_physics_tags.js 102
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
- The pack's composter table (`ComposterBlock.COMPOSTABLES`) is static and shared, so variants take exactly the same
  items. Nothing to change in `global.compost`.
- Keep the vanilla recipe removed. Otherwise a grid of spruce slabs matches both `minecraft:composter` and
  `spruce_composter`, and the result depends on recipe order.
- Create funnels, chutes and Little Logistics hoppers on a variant: test them. Most use the Forge item handler, which
  Forge wraps around `WorldlyContainerHolder` for vanilla hoppers. Other mods' code may check `Blocks.COMPOSTER`.
- Structures that place `minecraft:composter` are unaffected.

### Tests
- **CP-new5 Wood composters:** craft a spruce composter from 7 spruce slabs (JEI shows the 13 wood recipes and the
  other-slab one); fill it with seeds by hand and from a hopper; the comparator reads the level; at full it gives bone
  meal and a hopper below pulls it; breaking it full drops the composter and a bone meal.
- **CP-new6 Fuel and fire:** a birch composter burns in a furnace (300 ticks); fire spreads to it; a crimson composter
  doesn't burn.
- **CP-new7 Other slabs:** 7 bamboo slabs -> plain `minecraft:composter`.
- **CP-new8 Farmer (decision):** an unemployed villager next to a spruce composter doesn't become a farmer (or does, if
  the job-site mixin is built).

---

## Built, needs play

| Row | Change | Status | Notes |
|---|---|---|---|
| composting #4 | Seed milling to bone meal | Testing | Built 2026-10-30, shipped in 1.1.05 (changelog `1.1.05.md` 140): `wood/compost.js` mills `#creativeengineering:millable_seeds` into bone meal at 15 % (millstone and crushing wheels), corn kernels and rice left out; [composting.md](../composting.md) 3. Server half SQ-CP-01 PASSED r1031 (2026-10-31); open tiers P T (millstone in play). |

## Writeup corrections made

- 2026-10-21: [composting.md](../composting.md) 0 and 2.3 value counts 684 -> 688 (Brewin' port rows), 8 history line,
  [README.md](../README.md) composting row. Not edited by the 2026-10-31 sweep (reported instead): composting.md 0 and 3
  still say seed milling is unreleased, though it shipped in 1.1.05; 5 still links `startup_scripts/func/compost.js`,
  which now ships in patcherMod's jar (`kubejs_startup/creativeengineering/func/compost.js`).
