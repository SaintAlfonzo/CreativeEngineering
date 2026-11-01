# Brief: engineering / sawmill-and-components

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# engineering`, Sub `sawmill-and-components`. Reference writeup:
[engineering.md](../engineering.md) (2.1 sawblade routing, 2.2 Dragon Breathing, 2.3 components, 2.4 coils, F1-F9,
EN-01 to EN-18). Neighbours: [recipes_metal.md](../recipes_metal.md) (plates / rods / wires, F1 F4 F6),
[recipes_engineering.md](../recipes_engineering.md) 2.2-2.3 (mechanisms, redstone, Gates, Project Red; F7, F11).
Code: [components.js](../../kubejs/server_scripts/engineering/components.js),
[metal_base_materials.js](../../kubejs/server_scripts/metal/metal_base_materials.js) 687-759 (coils),
[blueprints.js](../../kubejs/server_scripts/engineering/blueprints.js), [mechanisms.js](../../kubejs/server_scripts/engineering/mechanisms.js),
[redstone.js](../../kubejs/server_scripts/engineering/redstone.js), [metaltags.js](../../kubejs/server_scripts/tags/metaltags.js),
[generation_scripts/sawmill.js](../../kubejs/generation_scripts/sawmill.js), [wood_types.js](../../kubejs/server_scripts/wood/wood_types.js),
patcherMod `sawmill/`, `mixin/Sawmill*Mixin.java`.

Jars checked (scratchpad cache): ImmersiveEngineering 9.2.4-170, kubejs-forge 1902.6.2-build.73,
kubejs-immersive-engineering 1902.2.3, create 0.5.1.i, vintageimprovements 0.2.0.3, JEI 11.8.1.1034, gates 1.8.0,
ProjectRed 4.19.0-beta+33 (core, integration, fabrication, transmission), createaddition 1.2.3 and ~20 other Create
add-ons for the tube scan. "Verified" below means read from those jars (JSON or decompiled class); "unverified" means
from code comments or general knowledge.

Updated 2026-10-31 (brief sweep): every file:line and dict line below re-checked against the pack (same jar versions in
`mods/`); #7 closed, #14 added, #2 and the related rows updated for work done since. **Test ids:** EN-19 to EN-21 now
belong to the D&D rose quartz sanding tests (engineering.md 7), so this brief's proposed ids are placeholders
`EN-new1`, `EN-new2`, ... (proposed, numbered when queued).

## Scope and order

Open rows: #5 (Bug), #2, #3, #4, #6, #8, #9, #10, #11, #12, #13, #14 (Planned). #1 is Testing (table at the end).
#7 (bucket -> shulker shell eats buckets) was decided 2026-10-29: keep, nothing to build (TODO Complete); its section is
removed.

1. **Cleanups, one sitting (KubeJS, `/reload`):** #6 tier comments, #5 disassembly comments / tool, #4 one centre per
   component. All in `components.js` and `metal_base_materials.js`. If #8 is
   built right after, #4 and #5 become columns of its table; they are small enough to do first anyway.
2. **Sawmill:** #2 (move the 211 recipes into a KubeJS data helper), then #3 (JEI shows the blade; patcherMod). #3 does
   not need #2, but #2 settles the recipe ids #3 tests against.
3. **Metal chain generator:** #8, then #9 (conductivity alternatives read #8's table). #8 also closes
   `recipes/metal #1` (plates), `#2` (hand rods), `#4` (gaps) and the lead-spring dict duplicate in
   `tooling/conventions #2`; decide those rows' questions as #8's table rows.
4. **Redstone tier:** #11 (tube tag) -> #12 (redstone mechanism uses the tag) -> #13 (gates use the mechanism).
5. **#10 last:** the row doesn't say what the cover is; needs your answer first (options below).
6. **#14 any time:** smaller structural-rope connectors are a patcherMod block plus a model and texture; independent of
   the rest (the model / texture decision first).

Related rows elsewhere: `recipes/engineering #6` (Create add-on machines on components, after #8),
`recipes/magic #4` (magic components), `recipes/progression #3` (Testing, built 2026-10-31: the three recycle sawmill
JSONs now load from `kubejs/data/creativeengineering/recipes/sawmill/`; #2 folds them in), `recipes/wood #1` (twisted
wood), `tooling/conventions #1` (Testing, built 2026-10-27: the 12 precision-sawblade blueprints are an `R.ieBlueprint`
loop in `blueprints.js` 200-218; still open here: the literal shape tags in `engineeringtags.js` 21-35 and the raw
vibrating step in `mechanisms.js` 40).

---

## sawmill-and-components #5: Disassembly tools
Status Bug / Work KubeJS / `engineering/sawmill-and-components #5` (all line refs re-checked 2026-10-31)

### Goal
The disassembly recipes and their comments agree: either the tool is spent per craft, or the comments say the tool is
paid once in the blueprint.

### Now
- Each disassembly blueprint item already costs its tool once: `blueprints.js` 48-76 (wire coils, coil blocks: wire
  cutter in the grid), 78-121 (components, springs, small springs: screwdriver). The crafting table returns the wire
  cutter with 1 damage (IE `WirecutterItem.getCraftingRemainingItem`, verified) and eats the screwdriver.
- The per-craft recipes take no tool, but the comments say they do:
  - `components.js` 91-92 "screwdriver + component -> 2 plates ... Screwdriver spends 1 durability/craft"; recipes 94-97.
  - `components.js` 99-100 "screwdriver + spring -> rod; screwdriver + small spring -> wire"; recipes 102-107
    (not in engineering.md F6).
  - `metal_base_materials.js` 704-706 "wire cutters + 6 wire coils -> 3 wire": recipe 708 takes 4 coils, no cutter.
    Line 700 "3 wire + 1 spool -> 6 coil": code 702 is 6 wire + spool -> 8.
  - 727-728 "wire cutters + 1 coil block -> 4 wire coils ... Wirecutter spends 1 durability": recipe 730, no cutter.
  - 740 "-> 4 HV coils": code 742 makes 8. 746 "wire cutters + 8 HV coils": recipes 748-749, no cutter.
  - Only the HV coil block disassembly, line 759, lists `i.wirecutter`.
- IE's blueprint crafting returns crafting remainders: `BlueprintCraftingRecipe.consumeInputs` swaps a fully used
  stack for `getCraftingRemainingItem` (verified). So a wire cutter input in a blueprint works today (1 damage per craft).
  IE's `ScrewdriverItem` has no durability and no remainder (verified): as an input it would be eaten.

### Build
Option A (recommended, comments only):
1. Rewrite the comments above to say "the blueprint item costs the tool once; the workbench recipe takes no tool", and
   fix the counts (700: "6 wire + 1 spool -> 8 coils"; 704: "4 wire coils -> 3 wire, the exact reverse of 6 -> 8";
   740: "-> 8 HV coils").
2. Drop `i.wirecutter` from line 759 so the HV coil block matches the other 12 coil blocks.

Option B (the tool really is spent):
1. Wire cutter: add `i.wirecutter` to the inputs at 708, 730, 748, 749 (and keep 759). Nothing else needed.
2. Screwdriver: give it a remainder of itself in a startup script, as `potion_buckets.js` 77-85 does for potions:
   `ItemEvents.modification(e => e.modify(i.screwdriver, it => it.setCraftingRemainder(Item.of(i.screwdriver).getItem())))`
   (`kjs$setCraftingRemainder(Item)` exists on ItemKJS in 1902.6.2, verified). Then add `i.screwdriver` to 95, 97, 103,
   106. Side effect: the screwdriver also stops being eaten by the blueprint crafts in `blueprints.js`. It has no
   durability, so it is reusable forever (no wear).

### Ids and data
`i.wirecutter` = `immersiveengineering:wirecutter`, `i.screwdriver` = `immersiveengineering:screwdriver` (dict lines
1569-1570). Blueprint categories `disassembly_components`, `disassembly_springs`, `disassembly_small_springs`,
`disassembly_wire_coils`, `disassembly_coil_blocks`; recipe ids `creativeengineering:blueprint/<category>/<id>`.

### Decisions
- A or B. **Recommend A:** the tool cost already sits in the blueprint, B needs a startup change for the screwdriver
  and only adds clicks to a bulk route. If #8 is built, put the choice in its generator once.

### Gotchas
- With B, each tool counts as one input of the recipe; IE needs the tool in the workbench's input slots (it has 5-6).

### Tests
- EN-new1 Disassembly tool: workbench with the coil-block disassembly blueprint, 1 copper coil block (+ wire cutter if B)
  -> 4 copper wire coils; with B the cutter comes back 1 damage lower.

---

## sawmill-and-components #2: Sawmill recipes off the manual pipeline
Status Planned / Work KubeJS / patcherMod / `engineering/sawmill-and-components #2` (re-checked 2026-10-31: 211 files,
generation copy and jar copy still identical)

### Goal
The 211 profiled sawmill recipes are built at load time from one species table of dict keys; no node script, no copies
in the patcherMod jar.

### Now
- `kubejs/generation_scripts/sawmill.js` (358 lines; TODO plan at lines 1-8) writes JSONs to
  `generation_scripts/recipes/sawmill/` (211 files, identical to patcherMod's
  `src/main/resources/data/creativeengineering/recipes/sawmill/`, checked with `diff -rq` 2026-10-21). Literal ids
  throughout. File name = recipe id: `<input path>-<output path, '/' -> '_'>`, e.g.
  `creativeengineering:sawmill/oak_planks-oak_stairs`, `twisted_planks-q_architects_palette_vertical_twisted_planks`.
- Per species: log and wood -> 6 planks (1600 RF, bark `stripping: true` + sawdust, `stripped` = the stripped log/wood);
  stripped log / wood -> 6 planks (800 RF, sawdust); planks -> each shape (800 RF, sawdust, `"blade"` from
  `profileFor`, slabs and vertical slabs ×2). Crimson / warped use their scales and roots instead of bark and sawdust.
  13 species: 9 vanilla (no planks -> slab recipe: the profile-less default is IE's own
  `immersiveengineering:sawmill/<wood>_slab`, verified), twisted (no sign), ancient / azalea / blossom (with slab).
- Five JSONs in `kubejs/data/creativeengineering/recipes/sawmill/`: two non-wood (`layered_obsidian_sawing`,
  `rose_quartz_sawing`) and, since 2026-10-31, `recycle_wooden_slab` / `_stairs` / `_door` (2 / 3 / 4 sticks plus
  sawdust; moved there from the never-loaded `kubejs/recipes/recycle/` under `recipes/progression #3`, Testing). They
  load as plain datapack JSON.
- `wood_types.js` 8-22 already has the species table (planks + 13 shape keys per species, from `item_dict.js`), but as a
  local `var` inside `ServerEvents.recipes`; it also removes IE's door / stairs recycling (41-44) and Quark
  Engineering's sawmill recipes (47-52; the comment at 51 is a leftover). `bark.js` 90-95 removes IE's log recipes.
- Why JSON: `NCH.ieSawmill` (neoCraftHelper.js 2302-2330; the helper now ships in patcherMod's jar,
  `development/patcherMod/src/main/resources/kubejs_startup/creativeengineering/func/`, and its error already points at
  a sawmill JSON) goes through kubejs-immersive-engineering's
  `SawmillRecipeSchema`, which re-serializes secondaries in a shape IE rejects. Verified in kubejs 1902.6.2
  `RecipesEventJS` / `RecipeJS.createRecipe`: a datapack recipe nobody changed is handed to the game as IE's own parsed
  object (`originalRecipe`); only new (`e.custom`) or modified recipes are re-serialized. So JSON from
  `ServerEvents.highPriorityData` behaves exactly like the jar's JSON, `"blade"` included (read by
  `SawmillRecipeSerializerMixin` from the JSON).

### Build
1. **Species table to startup.** New `kubejs/startup_scripts/_lib/wood_species.js` (`// priority: 900`, after
   `item_dict.js` at 1000) defining `global.woodSpecies`: the 13 rows of `wood_types.js` 9-21 plus `log`, `wood`,
   `log_stripped`, `wood_stripped`, `bark` (dict keys `log_oak`, `wood_oak`, `log_oak_stripped`, `wood_oak_stripped`,
   `bark_oak`, lines 380-450 all exist), `byproduct` (`i.sawdust`; `i.crimson_roots` / `i.warped_roots` for the stems),
   and flags `ieSlabDefault: true` (the 9 vanilla), `removeIeRecycling` (today's `sawmill: true`), `quarkSawmill`.
   `wood_types.js` then reads `global.woodSpecies` instead of its local table.
2. **NCH helper for data events.** Add to neoCraftHelper.js, next to `ieSawmill` (a patcherMod resource: rebuild,
   install, restart; the `kubejs/startup_scripts/` copy no longer exists):
   ```js
   // ServerEvents.highPriorityData only: writes an immersiveengineering:sawmill JSON, secondaries and "blade" intact.
   ieSawmillData(event, output, input, opts) {
     opts = opts || {}
     var id = opts.id || global.NCH.toId(output, input, 'sawmill')
     var ctx = 'ieSawmillData ' + id
     global.NCH._validateAny(input, ctx + ' input'); global.NCH._validateAny(output, ctx + ' output')
     var json = { type: 'immersiveengineering:sawmill', energy: opts.energy ?? 800,
       input: global.NCH._itemJson(input, { countMode: 'none', context: ctx }),
       result: global.NCH._itemJson(output, { countMode: 'flat', context: ctx }),
       secondaries: (opts.secondaries || []).map(s => ({ output: global.NCH._itemJson(s[0], { countMode: 'none', context: ctx }), stripping: !!s[1] })) }
     if (opts.stripped) json.stripped = global.NCH._itemJson(opts.stripped, { countMode: 'none', context: ctx })
     if (opts.blade) json.blade = opts.blade
     event.add(global.NCH.modId + ':recipes/sawmill/' + id + '.json', JSON.stringify(json))
   },
   ```
   (`event.add(path, string)` as `worldgen/wild_flowers.js` 518 and `worldgen/bee_nests.js` 253 use it.)
3. **Generator script** `kubejs/server_scripts/wood/sawmill_data.js`, IIFE like the worldgen scripts:
   `ServerEvents.highPriorityData(event => global.woodSpecies.forEach(s => ...))`. Profiles by form key, not regex:
   `{ stairs:'stairs', fencegate:'fence_gate', fence:'fence', door:'door', trapdoor:'trapdoor',
   pressureplate:'pressure_plate', button:'button', sign:'sign', railing:'railing', ladder:'ladder',
   vslab:'vertical_slab', vplanks:'vertical_planks', slab:'' }`. Skip forms whose dict value is `i.na`; skip `slab`
   when `ieSlabDefault`. Build the old ids (`<input path>-<output path with / -> _>`) so the new files shadow the jar
   copies path for path (KubeJS's high-priority pack sits above mod jars).
4. Optional: move the 5 `kubejs/data/.../sawmill/` JSONs into the same script under their current ids. They already
   load (the recycle ones since 2026-10-31), so this is only for one source; skip it if the JSONs are easier to read.
5. Optional, same files: generate the `creativeengineering:vertical_planks` / `railings` tags
   (`engineeringtags.js` 21-35, literal ids) from `global.woodSpecies` (`vplanks`, `railing`) through `global.tags.add`.
   (The 12 blueprint JSONs are DONE: `blueprints.js` 200-218 loop, 2026-10-27, `tooling/conventions #1`.)
6. Check, then delete: `generation_scripts/sawmill.js`, `generation_scripts/recipes/sawmill/`, the 211 jar copies
   (patcherMod rebuild + `mods/` swap); fix the comments that name the generator (`blueprints.js` 201,
   `engineeringtags.js` 20); update engineering.md 2.1 / 3 / 5 / 6.

### Ids and data
- Inputs/outputs all in `item_dict.js` (planks / shape keys 253-434, logs / bark 435-519, `i.sawdust` 1587 =
  `immersiveengineering:dust_wood`, `i.precision_sawblade` 5538, `i.sawblade` 593).
- Expected diff vs today: none in ids or content (twisted sign is `i.na`, matching the generator). Count: 9 × 16 + 16 +
  3 × 17 = 211.

### Decisions
- Recycle recipes: settled, kept (built 2026-10-31 as `creativeengineering:sawmill/recycle_wooden_*`).
- Move the 5 hand JSONs into the script (step 4)? **Recommend leave them as JSON** unless step 3 is being written anyway.

### Gotchas
- A later `replaceInput` / `replaceOutput` whose filter matches these recipes (e.g. on planks or `#forge:dusts/wood`)
  marks them changed, and KubeJS then re-serializes them through the broken schema: secondaries and `"blade"` are lost.
  No such filter exists today (grep `replaceInput({ input` / `{ output`: only sugar and flour in cooking.js); note it at
  the helper.
- `highPriorityData` runs on world load; whether it re-runs on `/reload` was not checked here: look for the generator's
  log line after a `/reload`. If it doesn't, sawmill edits need a restart (same as the jar today).
- `R.remove` / `replaceInput` only see datapack recipes (verified: `RecipesEventJS.recipeStream` walks
  `originalRecipes`), and these JSONs are datapack recipes, so `R.remove({id:'creativeengineering:sawmill/...'})` works
  on them.

### Tests
- EN-new2 Generated sawmill: `/dumprecipes creativeengineering` before and after; `recipes/by_mod/creativeengineering.json`
  lists the same 216 `immersiveengineering:sawmill` ids (211 generated + 5 hand JSONs). Then EN-01, EN-02, EN-05 again (blade and bark survive).

---

## sawmill-and-components #3: JEI doesn't show the sawmill blade
Status Planned / Work patcherMod / KubeJS / `engineering/sawmill-and-components #3` (re-checked 2026-10-31: no blade
sync or JEI slot added yet)

### Goal
Every sawmill page in JEI shows which blade it needs: the plain IE sawblade for the default, the programmed precision
sawblade for a profiled recipe; U on a stairs blade lists the stairs recipes.

### Now
- IE's `SawmillRecipeCategory` (verified, decompiled): `setRecipe(IRecipeLayoutBuilder, SawmillRecipe, IFocusGroup)`
  adds the input at (3, 7), stripped output (47, 7), main output (95, 7), secondaries from (47, 29) / (91, 29);
  background 114×26 plus padding (2, 36, 2, 12), so (3, 29) and the strip below y 47 are free. `draw(...)` draws the
  arrows only. No blade anywhere.
- The blade is **not synced to the client**: `SawmillRecipeMixin` javadoc says so, `SawmillRecipeSerializerMixin`
  hooks `readFromJson` only. On a dedicated server the client's `SawmillRecipe` objects come from
  `fromNetwork`, so their blade is `""`; JEI runs on the client. (Single player may show it, since the integrated
  server's recipe objects can reach the client unserialized; don't rely on it.)
- The precision sawblade has no JEI subtype: JEI treats all 12 programmed blades as one ingredient.
- The KubeJS tooltip (`client_scripts/tooltips.js` 451-459) already prints "Sawmill profile: Stairs" on any stack with
  `{Profile}`.
- Precedent for JEI-category mixins: `ItemDrainCategoryMixin`, `SpoutCategoryMixin` in the `client` list of
  `creativeengineering.mixins.json` (list opens at line 221; the two at 232-233). The sawmill mixins sit in the common
  list at 63-65.

### Build
1. **Sync the blade** (common mixin list): in `SawmillRecipeSerializerMixin` add
   `@Inject(method = "toNetwork(Lnet/minecraft/network/FriendlyByteBuf;Lblusunrize/immersiveengineering/api/crafting/SawmillRecipe;)V", at = @At("RETURN"))`
   -> `buffer.writeUtf(((SawmillRecipeExt) recipe).creativeengineering$getBlade())`, and
   `@Inject(method = "fromNetwork(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/FriendlyByteBuf;)Lblusunrize/immersiveengineering/api/crafting/SawmillRecipe;", at = @At("RETURN"))`
   -> `setBlade(buffer.readUtf())` (read even when the return value is null, to keep the buffer aligned). Method names
   are the typed overloads, unmapped (`javap` on the jar: `fromNetwork` / `toNetwork` plus SRG bridges `m_8005_` /
   `m_6178_`), so `remap = false` as in the existing mixin. Fix the "not synced" javadoc.
2. **Blade slot** (client mixin, new `IESawmillCategoryMixin` on
   `blusunrize.immersiveengineering.common.util.compat.jei.sawmill.SawmillRecipeCategory`, `remap = false`):
   `@Inject(method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lblusunrize/immersiveengineering/api/crafting/SawmillRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V", at = @At("TAIL"))`:
   blade empty -> `new ItemStack(IE sawblade)`; else a `PRECISION_SAWBLADE` stack with `Profile` set; add
   `builder.addSlot(RecipeIngredientRole.CATALYST, 3, 29).addItemStack(stack)` (`CATALYST` exists in JEI 11.8.1,
   verified). The slot tooltip then carries the KubeJS "Sawmill profile" line.
3. **Subtype** in a patcherMod JEI plugin (`compat/DragonBreathingJEI.java` is an `@JeiPlugin`; the newer ones sit in
   `compat/jei/`, e.g. `SmithingTemplateJEI.java`):
   `registerItemSubtypes(r -> r.registerSubtypeInterpreter(CreativeEngineeringItems.PRECISION_SAWBLADE.get(), (stack, ctx) -> SawbladeRouting.profileOf(stack)))`,
   so focus on a stairs blade finds only `blade == "stairs"` recipes.
4. Rebuild patcherMod; it changes the recipe packet, so server and every client need the same jar (they always do).

### Ids and data
`creativeengineering:precision_sawblade` (patcherMod `item/CreativeEngineeringItems.java` 15), `{Profile:"<shape>"}`,
`SawbladeRouting.PROFILE_KEY`; IE `immersiveengineering:sawblade`.

### Decisions
- Slot (above) or a JEI info page on the blade (`JEIEvents.information` in a client script, no Java)? **Recommend the
  slot**: the info page can only list the profiles, not which output each one cuts.

### Gotchas
- Without step 1 the slot shows the plain sawblade on every page on the server; test on the server, not in single
  player.
- The precision blade's class is in package `...items` while the file sits in `item/` (javac accepts it); import from
  `com.SaintAlfonzo.creativeengineering.items`.

### Tests
- EN-04 (rewrite): JEI on oak planks (U) on the server: each sawmill page shows the blade in the slot below the input.
- EN-new3 Blade focus: U on a stairs precision sawblade -> only the 13 planks -> stairs pages.

---

## sawmill-and-components #4: One centre per component
Status Planned / Work KubeJS / `engineering/sawmill-and-components #4` (owner asked for more info 2026-10-30; line refs
re-checked 2026-10-31)

### Goal
Each component has one centre item across crafting, sequenced assembly and blueprint.

### Now
- Iron: crafting `components.js` 36 and assembly 55 use `i.rod_copper`; blueprint 74 `i.ingot_copper`. IE's own
  removed recipes used a copper ingot in both crafting and blueprint (verified, IE `crafting/component_iron`,
  `blueprint/component_iron`), so the blueprint line is IE's leftover.
- Diamond: crafting 40 and assembly 59 `i.component_electrum`; blueprint 78 `i.component_gold`.
- Stale comments: 29 ("4 plates around a copper ingot"), 30 ("uses minecraft:diamond in the plate slots": it uses
  `i.dust_diamond`), 53 ("deploy 3 plates onto a copper ingot": 1 plate per loop, 3 loops, onto the centre), 72
  ("2 plates + 1 copper ingot").

### Build
1. Line 74: `i.ingot_copper` -> `i.rod_copper`. Line 78: `i.component_gold` -> `i.component_electrum`.
2. Fix comments 29, 30, 53, 72 ("centre per component, table in engineering.md 2.3").
3. engineering.md 2.3 table: drop the two **(F4)** marks; mark F4 fixed.

### Decisions
- **Recommend copper rod and electrum component.** The more information asked for: the copper rod and the electrum
  component are what two of the three routes (crafting table and sequenced assembly) already use, so only the workbench
  blueprint changes; the copper ingot there is IE's own leftover from the recipe the pack removed. For diamond, the
  electrum component keeps the chain order (diamond sits above electrum, the Electrical tier), while the gold component
  would let the diamond component skip the electrum step. Cost change on the blueprint: iron component needs 1 copper
  rod (a quarter ingot by machine) instead of 1 ingot; diamond component needs one electrum component instead of one gold
  component.

### Tests
- EN-13 covers it: add "JEI on the iron component: all three routes take a copper rod".

---

## sawmill-and-components #6: Tier names in comments
Status Planned / Work KubeJS / `engineering/sawmill-and-components #6` (decided 2026-10-29, see Decisions)

### Now
`components.js` 18 `// Aluminum  --- Light? ---`, 22 `// Uranium     --- Dense? ---`. Already decided elsewhere:
display names "Aluminum / Silver / Nickel Light Component", "Lead / Uranium Heavy Component"
(`startup_scripts/items/components.js` 7-11), and tags `forge:components/light` / `forge:components/heavy`
(`metaltags.js` 94-99).

### Build
1. Comments: replace the two with `// Aluminum Light` / `// Uranium Heavy` (and label 19-20, 23 the same way).
2. Display names (owner, 2026-10-29: when the usage becomes clear, keep the adjective order correct, or add a joking
   tooltip about the grammar). Either:
   - rename in `startup_scripts/items/components.js` 7-11 to "Light Aluminum Component" ... "Heavy Uranium Component"
     (size before material, as English orders adjectives); a startup change, needs a restart; or
   - keep "Aluminum Light Component" and add a one-line tooltip on the five items through `tooltips.js`'s
     `addTooltips` (e.g. "Yes, the adjectives are in the wrong order", `col.eng`).
3. Close the row; engineering.md F5 -> fixed.

### Decisions
- Rename or tooltip, and when: **recommend waiting for `recipes/engineering #6`** (the first real uses of the light /
  heavy components), then rename; the tags `forge:components/light` / `heavy` stay either way.

---

## sawmill-and-components #8: Metal component chains generated
Status Planned / Work KubeJS / `engineering/sawmill-and-components #8`

### Goal
One 2-D table (metal × form: exists / generated / hand-made) drives every part recipe from ingot to component, with
one set of machines and yields per step; foreign recipes for the same steps are removed; the odd ones are flagged and
written by hand.

### Now
- The dict already holds the chain per metal (`item_dict.js` "Component Chain" blocks from line 821): keys
  `<form>_<metal>` for ore, ds_ore, raw, raw_block, crushed, crushed_block, dust, dust_block, nugget, ingot, block,
  plate, plate_block, rod, wire, wire_coil (alias coil_), coil_block (alias wire_coil_block_), spring, small_spring,
  component; `i.none` (`createdeco:gray_decal`) = doesn't exist, `i.na` (`minecraft:barrier`) = not applicable.
  `server_scripts/_lib/metal_test_recipes.js` 9-13 walks the same matrix. Effective matrix (last assignment wins), parts columns:

  | metal | plate | plate_block | rod | wire | coil | coil block | spring | small spring | component |
  |---|---|---|---|---|---|---|---|---|---|
  | copper iron gold zinc aluminum silver nickel | Y | Y | Y | Y | Y | Y | Y | Y | Y |
  | lead | Y | Y | Y | Y | Y | Y | Y (dup key) | Y (dup key) | Y |
  | uranium | Y | Y | Y | none | none | none | none | none | Y |
  | brass steel constantan electrum | Y | Y | Y | Y | Y | Y | Y | Y | Y |
  | rose gold | Y | na | Y | Y | na | na | Y | Y | none |
  | cast iron | Y | Y | Y | none | none | none | none | none | none |
  | netherite | Y | Y | none | none | none | none | none | none | Y |
  | diamond | (dust as plate) | | | | | | | | Y |

- **Dict duplicates in the chain:** `spring_lead` / `small_spring_lead` are `i.none` at 947-948 and VI's lead springs at
  4360-4361 (still both, 2026-10-31; `tooling/conventions #2` did not take them). `raw_netherite` is single now
  (`minecraft:netherite_scrap`, 1063). The pack also removes VI's lead
  coiling (`metal_base_materials.js` 156-157), so lead springs have no recipe, yet `metaltags.js` 71-73 tags them and
  `components.js` 101-107 makes lead spring disassembly blueprints. Decide lead springs in the table.
- Part recipes today: `metal_base_materials.js` 577-759 (plate blocks, Create pressing for 4 metals, IE press for gold,
  hand rods for zinc, rolling / press rods and wires for 9 / 8 metals, wire cutter wires, coils, coil blocks, HV), plus
  per-metal removal blocks 36-184; components and disassembly in `components.js`. Metal lists repeated 6 times:
  `COIL_METALS` (`metal_base_materials.js` 688, `metaltags.js` 80, `startup_scripts/changes/transition_items.js` 169),
  `COMP_METALS` (`components.js` 93), `COMPONENT_METALS` (`metaltags.js` 88, `transition_items.js` 190),
  `SPRING_METALS` (`components.js` 101), `SPRINGS` (`metaltags.js` 56).
- Foreign duplicates (corrected 2026-10-31): IE's coil crafts are **already removed**, `metal_base_materials.js` 57-63
  (`crafting/coil_lv`, `coil_mv`, `coil_hv`, `wirecoil_copper`, `wirecoil_electrum`, `wirecoil_steel`,
  `wirecoil_structure_steel`), as are the cutter wires for copper (49), aluminum (140) and lead (151). Still loaded:
  `crafting/wire_electrum` / `wire_steel` (cutter), 11 `plate_*_hammering` (gold's removed at 109), and 8 IE
  `metalpress/plate_*` (gold's removed at 110); the file's own header (lines 3-20) lists the plate gaps. Confirm in JEI
  (EN-15).
- Components are consumed almost nowhere: only `i.wire_coil_block_steel` in the `electricalcomponents` blueprint item
  (`blueprints.js` 126, 133); `recipes/engineering #6` is where they get uses.

### Build
1. **Clean the dict** first: delete 947-948 or 4360-4361 (the netherite duplicate is gone).
2. **Table** `kubejs/startup_scripts/_lib/metal_chain.js` (`// priority: 990`): `global.metalChain` with
   - `forms`: the parts columns above (start at ingot; leave ore -> ingot processing, lines 185-575, as it is: it is
     already tabled and balanced, recipes_metal 2.2);
   - `metals`: one row string per metal, e.g. `copper: 'Y Y Y Y Y Y Y Y Y'`, `uranium: 'Y Y Y - - - - - Y'`, with
     `H` = exists, made by hand elsewhere (diamond component, HV coil and block, any chain you want to tweak);
   - `centre`: per metal component centre (the #4 table: `iron: 'rod_copper'`, `aluminum: ['ingot_nickel','ingot_silver']`, ...)
     and `part` overrides (`diamond: 'dust_diamond'`).
   Validation at load: a `Y` cell must resolve to a real item (not `i.na` / `i.none`), else warn with the key.
3. **Startup consumers read it:** `transition_items.js` 169-193 registers `coil_<m>`, `coil_block_<m>`, `component_<m>`
   from the table instead of its own lists (a new `Y` gets its incomplete item; needs a restart).
4. **NCH generator** `NCH.metalChain(e, table)` (or a server script `metal/metal_chain.js` calling existing helpers per
   link). One link = one source form -> one target form, with its machine set:

   | Link | Recipes (helper -> JSON type) | Yield today |
   |---|---|---|
   | ingot -> plate | `createPressing` (create:pressing), `ieMetalPress` + `i.mold_plate` (immersiveengineering:metal_press), `shapeless` [`i.ie_hammer`, ingot] | 1 |
   | plate ↔ plate_block | `shapeless` ×2 | 9 |
   | ingot -> rod | `createRolling` (createaddition:rolling), `ieMetalPress` + `i.mold_rod`, `shaped` 2 ingots (F4) | 4 / 4 / 4 |
   | plate -> wire | `shapeless` [plate, `i.wirecutter`] 2, `createRolling` 4, `ieMetalPress` + `i.mold_wire` 4 | |
   | wire -> coil | `shaped` 4 + spool -> 4, `createSequencedAssembly` 3 loops -> 4, `ieBlueprint` `electricalcomponents` 6 + spool -> 8, disassembly 4 -> 3 | |
   | coil -> coil block | `shaped` 8 + iron rod, assembly 6 loops, blueprint 4 + rod, disassembly -> 4 | |
   | rod -> spring, wire -> small spring | **new `NCH.viCoiling(output, input, e, idOverride, opts)`**: `{type:'vintageimprovements:coiling', ingredients:[..], results:[..], processingTime:120 (wire 60), springColor:'828282'}` (shape verified on VI's `coiling/iron_rod.json`); spring disassembly blueprints | 1 |
   | plates + centre -> component | `shaped` `P P / _C_ / P P`, assembly 3 loops, blueprint 2 + centre, disassembly -> 2 | |

   For each generated cell, first remove the foreign recipes of that step:
   `R.remove({ input: i[src + '_' + m], output: i[dst + '_' + m] }, e)`. This only touches datapack recipes (verified:
   KubeJS 1902.6.2 `remove` walks `originalRecipes`, never script-added ones), so order against the generator doesn't
   matter. Add a `dryRun` flag that logs `e.forEachRecipe(filter, r => r.getId())` instead of removing, and run it once.
5. Delete the replaced blocks in `metal_base_materials.js` (577-759) and `components.js` (35-107), keep the `H` rows
   (HV coil 733-759, diamond component) as hand code next to the table; `metaltags.js` 77-99 loops the table.

### Ids and data
Every id is a dict key `<form>_<metal>`; spool `i.spool` (`creativeengineering:spool`), molds `i.mold_plate` /
`i.mold_rod` / `i.mold_wire`, tools `i.ie_hammer`, `i.wirecutter`. Recipe ids: keep today's suffixes
(`<m>_wire_coil`, `<m>_coil_block`, `component_<m>`, `..._sequenced_assembly`, `..._disassembly`) so JEI bookmarks and
quest references survive.

### Decisions
- Scope: parts only (recommended) or ore processing too? **Parts only**; ore tables are complete.
- Plates (recipes_metal F1): **all three machines for every metal with a plate** (Create press, IE press, hammer).
- Hand rods (recipes_metal F4): **every metal, 2 ingots -> 1** (worse than the machines' 4), or none.
- Lead springs: **`none`** (no coiling recipe today) unless you want lead springs.
- Disassembly tool: #5's answer.

### Gotchas
- A removal filter on `{input: ingot, output: plate}` also drops other mods' plate recipes that use the ingot plus
  something else; review the dry-run list before switching it on.
- `wire_coil_steel` is IE's structural steel rope; the electrical HV coil is `i.wirecoil_hv`. Keep HV `H`.
- Copper / electrum coil blocks are IE's `coil_lv` / `coil_mv`; generating them is fine, but they are also IE
  multiblock parts (no recipe change affects that).

### Tests
- Offline: a small `tools/metal_chain_check.py` that parses `item_dict.js` (last assignment wins) and the table and
  prints the matrix, flagging `Y` on `i.none` / `i.na` and duplicate keys.
- EN-new4 Chain coverage: JEI on aluminum plate, rod, wire, coil, coil block, component: each shows its machine set and
  only `creativeengineering:` recipes (plus recycling).
- EN-new5 Removed duplicates: JEI on an aluminum plate shows no IE `metalpress/plate_aluminum` or
  `plate_aluminum_hammering`; the LV coil block still shows no IE `crafting/coil_lv`.

---

## sawmill-and-components #9: Wire alternatives by conductivity
Status Planned / Work KubeJS / `engineering/sawmill-and-components #9` (owner asked for more info 2026-10-30; see
Decisions)

### Goal
Recipes that need a wire, wire coil or coil block of one metal also accept the other metals of the same conductivity
class, so the 9 pack coils and 10 coil blocks have uses.

### Now
- The pack's coils and coil blocks are consumed nowhere (grep `server_scripts`; only the steel coil block in a blueprint
  craft). Tags exist per metal: `forge:wire_coils/<m>`, `forge:coil_blocks/<m>`, `forge:wires/<m>` (`metaltags.js`
  10-25, 80-86).
- Consumers (verified in the IE and Create Crafts & Additions jars):
  - `#forge:wires/copper`: IE `blueprint/electron_tube`, `crafting/electric_lantern`, revolver bayonet / magazine
    upgrades, `wirecoil_copper`; CCA `copper_spool`.
  - `immersiveengineering:wirecoil_copper`: IE `coil_lv`, `furnace_heater`, `powerpack`.
  - `immersiveengineering:coil_lv`: IE `dynamo`, `thermoelectric_generator`, `charging_station`, `turntable`, shield
    magnet upgrade. `coil_mv`: `generator`, `transformer`, `tesla_coil`, `railgun`. `coil_hv`: `transformer_hv`.
  - `#forge:wires/electrum`: IE `blueprint/component_electronic`, powerpack tesla upgrade; CCA electrum spool / amulet /
    accumulator. `#forge:wires/aluminum`: IE `component_electronic_adv`, `connector_bundled`, `wirecoil_redstone`,
    `wirecoil_steel`. `#forge:wires/iron`: CCA barbed wire, small light connector. `#forge:wires/gold`: CCA gold spool,
    accumulator.
  - Pack: `power.js` 23 (vacuum tube, copper wire), 33 (portable energy interface, LV coil block), 39 (accumulator, LV
    and MV coil blocks).

### Build
1. Conductivity classes in `global.metalChain` (#8) or a small `global.conductors` table (approximate room-temperature
   conductivity, % IACS, general knowledge, unverified): silver 105, copper 100, gold 70, aluminum 61, (electrum, an
   Au-Ag alloy, lower than either; treat as gold class), brass 28, zinc 27, nickel 22, iron 17, steel 3-15, lead 8,
   constantan 3.5 (a resistance alloy: heating, the "Thermal" component). Suggested classes: **high** silver, copper,
   gold, electrum; **mid** aluminum, brass, zinc, nickel; **low** iron, steel, lead; **resistive** constantan.
2. Tags through `global.tags.add`: `creativeengineering:conductors/<class>/wires`, `.../wire_coils`, `.../coil_blocks`
   from the table; dict keys `i.t_conductors_high_wires` etc. (`i.t_` prefix as `i.t_bakeware`, dict line 42).
3. Alternatives, two kinds:
   - Same class, same count: `R.replaceInput({ id }, '#forge:wires/copper', i.t_conductors_high_wires, e)` on a curated
     id list (IE / CCA ids above). Pack recipes (power.js) take the tag directly: `replaceInput` doesn't reach script
     recipes (verified, `originalRecipes` only).
   - Lower class, more of it: a second recipe per consumer (`R.shaped` copy with ×2 coil blocks / a mid-class tag), id
     suffix `_mid_conductor`. Only for the handful worth it (dynamo, thermoelectric generator, furnace heater).
   - Resistive: constantan coil / coil block in heaters (`furnace_heater`, the IE thermoelectric generator's hot side
     is a block, not a recipe) as a better-than-copper alternative.
4. Never let a cheaper metal make IE's own LV / MV coil block (`coil_lv` = copper coil block): alternatives are for
   consumers, not for the copper / electrum items themselves.

### Decisions
- Whether at all (the row's question). **Recommend yes, same-class tags first** (cheap, no new recipes), the ×2
  lower-class recipes only for 3-5 machines. The more information asked for, in player terms: today a copper LV
  machine (dynamo, thermoelectric generator, charging station) takes exactly copper coils, and the pack's silver, gold
  and electrum coils have no use at all. Same-class tags let any high conductor stand in one for one (silver is rarer
  than copper, so nobody loses); a mid conductor (aluminum, brass, zinc, nickel) only through a second recipe that
  takes twice the coils. Nothing changes IE's wire networks: LV / MV / HV cable stays copper / electrum / steel; this
  is recipe inputs only. Effort: one tag table plus about 10 `replaceInput` ids for step 1; each x2 recipe is one
  `R.shaped` copy.
- Which consumer ids: start with the IE LV list and CCA spools above.

### Gotchas
- Multiblock structures (IE lightning rod, etc.) check exact blocks; tags don't change them.
- After #8 the copper wire coil recipe is the pack's; don't replace its own input with the tag.

### Tests
- EN-new6 Conductor tag: JEI on the IE dynamo shows a silver coil block alternative (high class); an aluminum one only
  in the ×2 recipe.

---

## sawmill-and-components #10: IE wire connector cover
Status Planned / Work KubeJS / patcherMod / `engineering/sawmill-and-components #10` (still undecided 2026-10-31)

### Goal
Unknown: the row says "a cover for IE wire connectors", decision "what it is". Ask before building.

### Now / options
- **IE Feedthrough Insulator** (`immersiveengineering:feedthrough`, built in): passes a wire through a solid block
  with no hole; "created with any normal solid block in the middle, by right-clicking either of two connectors with an
  Engineer's Hammer" (IE manual `wiring.txt`, verified). No recipe; nothing to build except telling players (tooltip on
  the connectors in `client_scripts/tooltips.js`, IE section from line 442).
- **Cosmetic cover block** (KubeJS startup block, thin plate with a hole): can't share the block space with the
  connector, so it only frames it from a neighbouring block. Low value.
- **Camouflage on the connector** (patcherMod): right-click a connector with a block to store a `BlockState` in the
  connector's block entity and render it as a casing. Needs mixins on IE's connector block entity (NBT, sync) and its
  model / renderer; large and fragile. Framed Blocks / Create copycats can't hold a connector.

### Decisions
- **Recommend:** confirm whether the feedthrough is what you mean; if yes, add a connector tooltip and close. If you
  want connectors hidden inside walls or casings, say so and this becomes a patcherMod feature with its own writeup.

### Tests
- EN-new7 Feedthrough: two LV connectors on either side of a stone block, hammer one -> a feedthrough; power passes.

---

## sawmill-and-components #11: Electron and vacuum tube tag
Status Planned / Work KubeJS / `engineering/sawmill-and-components #11`

### Goal
Recipes take "an electron tube" (Create's electron tube or IE's vacuum tube) through one tag, except visual uses
(nixie tubes) and high-tier IE recipes.

### Now
- Items: `i.electron_tube` = `create:electron_tube` (dict 1496), `i.vacuum_tube` = `immersiveengineering:electron_tube`
  ("Vacuum Tube" in IE's lang; dict 730, now the only key). No tag (grep `electron_tubes`: none, 2026-10-31).
- Mod recipes using `create:electron_tube` (verified in jars: create, createaddition, create_dd, railways,
  create_tweaked_controllers, vintageimprovements; none found in the other Create add-ons scanned):
  create `crafting/kinetics/` adjustable_chain_gearshift, clockwork_bearing, contraption_controls, controller_rail,
  deployer, display_board, mechanical_crafter, mechanical_roller, **nixie_tube**, sequenced_gearshift, smart_chute,
  smart_fluid_pipe, track_signal; `crafting/logistics/` brass_funnel, brass_tunnel, content_observer, stockpile_switch;
  `create_dd:mechanical_crafting/handheld_nozzle`; `create_tweaked_controllers:crafting/tweaked_linked_controller`;
  `createaddition:crafting/redstone_relay`, `createaddition:mechanical_crafting/tesla_coil`;
  `railways:crafting/semaphore`; `vintageimprovements:mechanical_crafting/laser`. (Some of these the pack replaces.)
- Using the IE vacuum tube: IE `blueprint/component_electronic_adv`, `crafting/charging_station`, `crafting/logic_unit`,
  powerpack induction and revolver electro upgrades; pack `transport.js` 140 (Little Logistics vessel charger).
- Pack recipes on `i.electron_tube`: `transport.js` 53-54 (controller rail), `power.js` 28-29 (capacitor),
  `redstone.js` 30-32 (copper bulb), `mechanisms.js` 38, 45 (redstone mechanism). The tube itself: `redstone.js`
  103-106 (deploying, arc furnace, blueprint ×2); vacuum tube `power.js` 22-23 (IE blueprint removed, pack blueprint ×3).

### Build
1. Tag `creativeengineering:electron_tubes` = [`i.electron_tube`, `i.vacuum_tube`] in `engineeringtags.js` via
   `global.tags.add`; dict key `i.t_electron_tubes = '#creativeengineering:electron_tubes'`.
2. Mod recipes: `R.bulk('replaceInput', ids.map(id => [{ id }, i.electron_tube, i.t_electron_tubes, e]))` with the
   Create list minus `create:crafting/kinetics/nixie_tube` (and `display_board` if you count it as visual).
3. Vacuum tube, low tier only: same with `i.vacuum_tube` on `immersiveengineering:crafting/charging_station` and the
   pack's vessel charger (edit `transport.js` 140 directly). Keep `component_electronic_adv`, `logic_unit` and the
   upgrades on the IE vacuum tube.
4. Pack scripts: put `i.t_electron_tubes` in the recipes listed above by editing them (`replaceInput` doesn't reach
   script recipes, verified).
5. JEI: tag inputs cycle both tubes; nothing else.

### Decisions
- Which recipes count as visual: **nixie tube only**; `display_board` takes the tag.
- Which vacuum-tube recipes are low tier: **charging station + vessel charger**.

### Gotchas
- A recipe the pack already removed and re-added under a `creativeengineering:` id needs the script edit, not the
  replace (controller rail: the pack's `transport.js` 53).
- `sequenced_assembly` steps inside mod recipes are covered by `replaceInput` (KubeJS walks the whole JSON).

### Tests
- EN-new8 Tube tag: craft a deployer with an IE vacuum tube; the nixie tube still needs a Create electron tube.

---

## sawmill-and-components #12: Redstone mechanism
Status Planned / Work KubeJS / `engineering/sawmill-and-components #12`

### Goal
The redstone mechanism (`vintageimprovements:redstone_module`, renamed "Redstone Mechanism" in
`client_scripts/namechanges.js` 113)
becomes a low-tier part made from low-tier redstone pieces, gives cheaper bulk routes to low-tier redstone, and stands
in for redstone dust in high-tier components.

### Now
- `mechanisms.js` 33-45: VI's assembly removed; pack assembly on `i.any_plate_gold`, 2 loops: deploy copper rod,
  electron tube, redstone, VI vibrating (raw `e.custom`, line 40), deploy redstone randomizer, deploy rose quartz plate;
  shared `i.incomplete` steps with VI's own `i.i_redstone_mechanism` transitional item. Blueprint
  (`redstonecomponents`): 2 copper rods, tube, 2 redstone, randomizer, 2 rose quartz plates, gold plate. Used by the
  mechanical crafter (`process.js` 55). recipes_engineering F7 already asks for an `R.incomplete` key and a vibrating step
  helper.
- Low-tier redstone today, one per craft: repeater (`redstone.js` 151-159), comparator 181-189, redstone link 132-141
  (×2 / ×4 from a transmitter), Create logistics (pulse repeater / extender, powered latch / toggle latch, content
  observer, stockpile switch, redstone contact: Create's own crafting, verified in the jar).

### Build
1. Read of the row: the mechanism's recipe uses "rose quartz plate, electron tube tag, 2-3 repeats". Proposed:
   assembly on `i.rose_quartz_plate`, steps deploy `i.t_electron_tubes` (#11), deploy `i.redstone`, press; 2 loops
   (drop the randomizer and the vibrating step, both mid-tier gates); blueprint `redstonecomponents`: 2 rose quartz
   plates + tube + 2 redstone -> 1. Confirm before building (the vibrating step is the only thing tying it to VI).
2. Transitional item: add `['redstone_mechanism', 'vintageimprovements:item/redstone_module']` to
   `SEQUENCED_ASSEMBLIES` in `transition_items.js` and pass `['redstone_mechanism']`; if a vibrating step stays, add
   `NCH.viVibratingStep(inc, e)` returning the `e.custom` step without an id (F7).
3. Bulk routes, ids `creativeengineering:shaped/<item>_from_mechanism` etc. (numbers are proposals):
   repeater ×6 (mechanism + 3 smooth stone slabs), comparator ×4 (+ quartz), redstone link ×8 (mechanism + transmitter +
   2 andesite casings), Create pulse repeater / extender / powered latch / toggle latch ×4, content observer and
   stockpile switch ×2. Shaped + blueprint (`redstonecomponents`) each.
4. High tier: replace redstone dust with the mechanism via `replaceInput` by id in IE `blueprint/component_electronic`
   (`#forge:dusts/redstone`, verified JSON), IE `crafting/logic_unit`, and in the pack's high components if you want
   (Gold / Electrum Electrical centres are wires today).

### Ids and data
`i.redstone_mechanism` (dict 1710), `i.i_redstone_mechanism` (19), `i.rose_quartz_plate` (1489,
`create_things_and_misc:rose_quartz_sheet`), `i.redstone_link` (1497), `i.repeater`, `i.comparator`, `i.transmitter`
(1501), `i.andesite_casing`; Create logistics keys exist for `i.pulse_repeater` (4982), `i.powered_latch` (4979),
`i.content_observer` (4676), `i.stockpile_switch` (5072); check `pulse_extender` / `powered_toggle_latch` and add keys
if missing.

### Decisions
- Mechanism recipe as proposed in step 1? **Recommend yes**, low tier means no vibrating table.
- Bulk yields: **start with the numbers above**, tune after play.
- Which high-tier recipes swap dust for a mechanism: **IE electronic component and logic unit**.

### Gotchas
- The mechanical crafter (`process.js` 55) needs a mechanism; a cheaper mechanism makes it cheaper too.
- `R.replaceInput` only on mod recipes; the pack's own repeater / link recipes are script recipes: add new ones.

### Tests
- EN-new9 Mechanism: assemble one on a rose quartz plate in 2 loops; a mechanism + 3 slabs -> 6 repeaters.

---

## sawmill-and-components #13: Gates and Project Red
Status Planned / Work KubeJS / `engineering/sawmill-and-components #13`

### Goal
Basic gates stay simple and vanilla-like but expensive at scale; Project Red's gates are complex to craft by hand
but much cheaper at scale, for real computing.

### Now
- **Project Red is already in the pack:** Core, Fabrication, Illumination, Integration, Transmission 4.19.0-beta+33
  (`pack/MODLIST.md` 324-328). `redstone.js` 240-253 removes only Core's misc items (draw plate, coils, motor, woven
  cloth, sail) and the CB Microblock saws; the gates are untouched. Integration's gates are shaped crafts from
  `projectred_core:plate` (smooth stone smelted ×2), `anode` (3 redstone + 3 plates -> 3), `cathode`,
  `conductive_plate` (redstone + plate), e.g. `and_gate`: `ACA / CCC / PWP` (verified). Fabrication = IC workbench /
  lithography (designs whole circuits as one gate: the at-scale route). Dict has them (`i.anode` 6544 ...
  `i.and_gate` 6605 = PR's, `i.gates_and_gate` 6876 = the Gates mod's).
- **Redstone Gates** (`gates` 1.8.0): `redstone.js` 145-149 removes NAND / NOR / XOR / XNOR and 231-238 the wireless
  parts; left: AND / OR / NOT (3 torches + iron ingot + 3 stone, verified), RS flip-flop, clocks, rotary switch, fast /
  slow repeater, rain / thunder detector (the pack's).

### Build
1. Basic gates (Gates mod): keep its AND / OR / NOT / flip-flop / clocks as they are (vanilla-like, one per craft, no
   bulk routes). Nothing to build unless you want NAND / NOR / XOR / XNOR back.
2. Project Red, "complex by hand": `R.replaceInput` on the `projectred_integration:<gate>` recipe ids, swapping the
   conductive plate for a redstone mechanism (#12) or the tube tag (#11), so one gate by hand costs a mechanism.
3. "Cheap at scale": a blueprint (`redstonecomponents`) and a sequenced assembly per gate family that output ×4-8 from
   plates + anodes + cathodes + one mechanism; PR Fabrication stays as PR ships it (its ICs are the cheapest per gate).
   Generate from a list of the ~34 gate ids in `projectred_integration` (`recipes/*.json` names, verified).
4. Make the two AND / OR / NOT sets distinct in JEI names if confusing (`namechanges.js`).

### Decisions
- "add Project Red" in the row is done; the open question is how far to change PR's crafts. **Recommend** steps 2-3
  for the PR gates only, PR Core intermediates (anode, cathode, plates) unchanged.
- Keep both mods' AND / OR / NOT? **Recommend yes** (Gates = cheap single gates, PR = bundled / IC builds).

### Gotchas
- PR gates are multipart (CB Multipart) items; recipe changes are plain crafting, no NBT.
- `replaceInput` of `projectred_core:conductive_plate` by id only, not globally: Transmission's wires use it too.

### Tests
- EN-new10 PR gate: JEI on `projectred_integration:and_gate` shows the hand craft with a mechanism and the ×N blueprint;
  Gates mod AND gate unchanged.

---

## sawmill-and-components #14: Smaller hemp coil connectors
Status Planned / Work KubeJS / new jar (recommended: patcherMod) / `engineering/sawmill-and-components #14` (scratchpad
2026-10-22; decision: model and texture)

### Goal
A small connector for IE's Hemp Rope Coil, so ropes (clotheslines, rope bridges, banner lines, ship rigging) can hang
from a hook-sized fitting instead of IE's chunky Structural Connector.

### Now
- The rope: `i.wirecoil_structure_rope` = `immersiveengineering:wirecoil_structure_rope` (dict 1621, "Hemp Rope Coil"),
  wire category `STRUCTURE`, like the steel structure rope `i.wire_coil_steel` (dict 1171; its IE craft is removed at
  `metal_base_materials.js` 61, the pack makes it in the `COIL_METALS` loop).
- The only block that takes it: `i.connector_structural` = `immersiveengineering:connector_structural` (dict 3959).
  Verified in IE 9.2.4-170 (`javap`): `ConnectorStructuralBlockEntity extends ImmersiveConnectableBlockEntity`, has a
  public `(BlockEntityType, BlockPos, BlockState)` constructor, `canConnectCable` accepts any wire whose category is
  `"STRUCTURE"`, `getConnectionOffset` puts the wire end 0.5 out from the face (constants 0.03125 / 0.5), bounds come
  from `getBlockBounds`, rotation by hammer (`hammerUseSide`, `rotation` field). Model is an OBJ
  (`models/block/connector/connector_structural.obj.ie`).
- No mod in the pack adds a second structural connector (grep of the dict: only `connector_structural`). Supplementaries
  ropes are blocks, not IE wires.
- KubeJS 6 cannot register a block entity that joins IE's wire network, so "KubeJS" in the row can only mean the recipe.

### Build
1. **patcherMod** (recommended over a new jar: patcherMod already compiles against IE for the sawmill mixins). New
   `ie/RopeHookBlock` (a thin directional block, `Properties.copy(Blocks.IRON_BARS)`, no collision box beyond the
   hook) and `ie/RopeHookBlockEntity extends ConnectorStructuralBlockEntity` using the 3-arg constructor with its own
   `BlockEntityType`. Override `getConnectionOffset` (wire end about 0.15 out from the face instead of 0.5) and
   `getBlockBounds` (a 4x4x3 px box). Keep `canConnectCable` as inherited (rope and steel rope both fit; restrict to
   `wirecoil_structure_rope`'s type if you want hemp only). Register next to the oven (`OvenRegistry.register(modEventBus)`,
   `CreativeEngineering.java` 99).
2. Check before coding (unverified): how IE's block (`ConnectorBlock` / `IEEntityBlock`) wires up placement facing and
   the BE ticker, and whether the wire renderer needs the block to be in an IE tag or registry for connection
   rendering. Decompile IE's `MetalDevices` / `Connectors` registration for `connector_structural` first.
3. **Model and texture** (the row's decision): a JSON model (no OBJ needed), e.g. an iron eye hook or a wooden belaying
   peg, 16x16 texture. Variants by material are optional (iron hook, wooden peg).
4. Dict `i.rope_hook = 'creativeengineering:rope_hook'` (and `b.rope_hook` in block_dict.js), lang, loot table (drops
   itself), `global.tags.mineable(e, 'pickaxe', ...)`.
5. Recipe through `global.NCH`: e.g. `R.shaped(Item.of(i.rope_hook, 4), [' N ', 'N N', ' N '], { N: i.nugget_iron }, e,
   'rope_hook')` plus an `R.ieBlueprint` in `electricalcomponents` or a new small category. Cheaper than the
   Structural Connector, since it is cosmetic.

### Ids and data
New `creativeengineering:rope_hook` (block, item, BE type; name proposed). IE ids above verified in the jar.

### Decisions
- Model: **recommend one iron eye hook** first (reads as "small connector", tints well); wooden peg later if wanted.
- Steel rope too, or hemp only? **Recommend both** (inherited; no reason to forbid it).
- New jar vs patcherMod: **patcherMod** (one less jar, IE already on its compile path).

### Gotchas
- Wire sag and length come from the wire type, not the connector: a smaller connector does not shorten max length.
- `getConnectionOffset` is called on both client and server; keep it pure (no level reads).
- The rope's render origin follows the offset: test both ends on a hook and on a Structural Connector.

### Tests
- EN-new11 Rope hook: place two hooks 8 blocks apart, connect them with a Hemp Rope Coil; the rope starts at the hook
  (not half a block out); a hook to a Structural Connector also connects; an LV wire coil is refused; breaking a hook
  drops it and the rope.

---

## Built, needs play

| Row | Status | Notes |
|---|---|---|
| #1 Engineering features ([engineering.md](../engineering.md) 7) | Testing | Open tiers S C P: EN-01 to EN-18 still unrun (2026-10-31); sawblade routing never checked in game. EN-01's default (oak planks -> slabs) is IE's own `immersiveengineering:sawmill/oak_slab`; the pack generates no vanilla planks -> slab. IE's coil crafts (`coil_lv`, `wirecoil_copper`, ...) are removed (`metal_base_materials.js` 57-63); its electrum / steel cutter wires, 11 hammer plates and 8 press plates still load, so JEI shows them beside the pack's (EN-15 / EN-16: note it; #8 removes them). Lead springs have no recipe but have disassembly blueprints (#8). |

## Writeup corrections made

- [engineering.md](../engineering.md) section 3: the routing classes' force-load is at `CreativeEngineering.java`
  lines 262-269, not 243-250.
- [engineering.md](../engineering.md) F9 `#13`: noted that Project Red's five modules are already in the pack.
