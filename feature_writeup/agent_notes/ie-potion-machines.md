# ie-potion-machines

> IE Refinery lingerification, IE JEI fluid NBT tooltips, Bottler NBT fluids, machine-tipped arrows

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Merged 2026-10-19 from 4 memories (index compaction).

## ie-refinery-lingerification

Added 2026-09-07 (patcherMod [patchermod-coremod](patchermod-coremod.md)). The IE **Refinery** counterpart to the
Create-mixer fluid brew [am-potion-brewing-fix](am-potion-brewing-fix.md) / `DragonBreathBrewing`: for every registered
potion, both `create:potion{REGULAR}` **and** `{SPLASH}` + `forge:fluid/dragon_breath` fluid
-> `create:potion{LINGERING}`. Coexists with the Create-mixer path and the IE-Mixer item path
(user chose coexist). No catalyst item. **1500 mB** potion in/out (matches IE-Mixer potion
amount); dragon's breath 250 mB (catalyst-style quarter-bucket); energy 120/op.

## Recipe generation — `fluidbrewing/Lingerification.java` (plain Java, not a mixin)
Mirrors IE's own `immersiveengineering:mixer_potion_list` mechanism:
- `registerListGenerator()` (idempotent) does
  `GeneratedListRecipe.LIST_GENERATORS.put(creativeengineering:lingerification_list,
  GeneratedListRecipe.RecipeListGenerator.simple(Lingerification::generate,
  RefineryRecipe.SERIALIZER.getId(), IERecipeTypes.REFINERY))`.
  Called from `CreativeEngineering.commonSetup` via `event.enqueueWork(...)` (same slot as
  `DragonBreathBrewing::build`). One-shot static put, survives `/reload`.
- Datapack file `patcherMod/.../data/creativeengineering/recipes/lingerification_list.json`
  = `{"type":"immersiveengineering:generated_list"}` (identical to IE's `mixer_potion_list.json`;
  `GeneratedListRecipe.from(id)` looks the id up in `LIST_GENERATORS`, `Preconditions.checkNotNull`).
- `generate()` builds `List<RefineryRecipe>` — `new RefineryRecipe(rl, lingeringFluid, in0, in1,
  Ingredient.EMPTY, 120)`. `in0 = new FluidTagInput(Either.right(List.of(create:potion)), 1500,
  <regular|splash>FluidStack.getTag())` (same shape as `PotionRecipeGeneratorsMixin`'s mixer
  inputs); `in1 = new FluidTagInput(TagKey forge:fluid/dragon_breath, 250)`.
- `RefineryRecipe.RECIPES` picks these up automatically: `CachedRecipeList.updateCache`
  filters `recipeManager.getRecipes()` for `IListRecipe`, flat-maps `getSubRecipes()`, keeps the
  `RefineryRecipe`-class ones. `RefineryRecipe.findRecipe` matches inputs in **either tank order**
  and `Ingredient.EMPTY` catalyst tests true against an empty catalyst slot.
- NBT-carrying `create:potion` inputs match via the already-shipped `FluidTagInputMixin` subset
  check (shared Bottler/Mixer/**Refinery** `FluidTagInput.testIgnoringAmount`) — see
  [ie-potion-machines](ie-potion-machines.md).

## JEI "Lingerification" category (client) — parallel to [am-potion-brewing-fix](am-potion-brewing-fix.md)'s Automated Potion Mixing
- `compat/LingerificationCategory` **extends `RefineryRecipeCategory`**; ctor reflection-swaps the
  `private final IERecipeCategory.type` field to
  `RecipeType<>(creativeengineering:lingerification, RefineryRecipe.class)` and overwrites
  `title` (exactly `AutomatedPotionMixingCategory`'s trick). `static volatile boolean ACTIVE`.
  `isLingerification(r)` = output fluid isSame `AllFluids.POTION` && tag `Bottle == "LINGERING"`
  (within `RefineryRecipe.RECIPES` those are only ever ours).
- `compat/DragonBreathingJEI` (the pack's existing `@JeiPlugin`) — third category wired next to
  `potionMixing`: `registerCategories` (try/catch -> `ACTIVE`), `registerRecipes`
  (`RefineryRecipe.RECIPES.getRecipes(level)` filtered by `isLingerification` ->
  `addRecipes(TYPE, ...)`), `registerRecipeCatalysts`
  (`new ItemStack(IEBlocks.Multiblocks.REFINERY)`).
- `mixin/JEIHelperMixin` — SECOND `@Redirect` (first is the Mixer one). IE's `registerRecipes`
  adds the Refinery page as `getFiltered(RefineryRecipe.RECIPES, <iePredicate>)` — **`getFiltered`,
  not `getRecipes`** (like the Mixer). `@Slice(from = FIELD RefineryRecipe.RECIPES)` +
  **`@At(... getFiltered, ordinal = 0)`** + drop `isLingerification` entries when
  `LingerificationCategory.ACTIVE`. **`ordinal = 0` is mandatory** — a from-only slice runs to
  method end, and IE calls `getFiltered` again for Arc Furnace / Bottling Machine / Mixer after
  the Refinery; without the ordinal the redirect hijacks those too and the handler's
  `for (RefineryRecipe r : all)` throws `CCE`, aborting the rest of `JEIHelper.registerRecipes`
  (every IE JEI page after the Refinery disappears). Same `ordinal = 0` added to the Mixer
  redirect defensively (Mixer is currently last, so it worked without it).
- `mixin/RefineryRecipeCategoryMixin` (client; `creativeengineering.mixins.json` `client`) — adds
  potion name + effect lines to fluid-slot tooltips carrying a `Potion` tag. **TWO redirects**:
  (1) `@Redirect addTooltipCallback` — refinery `setRecipe` has only ONE such call and it's on the
  **output** slot; (2) `@Redirect IRecipeSlotBuilder.addIngredients(IIngredientType,List)` — the
  two fluid **input** slots (`input0`,`input1`) have NO `addTooltipCallback` at all, they end in
  `addIngredients(FLUID, getMatchingFluidStacks())`; catalyst uses `addItemStacks`, output uses
  singular `addIngredient`, so exactly 2 hits. Shared callback lambda extracted to
  `compat/PotionFluidTooltip.POTION_EFFECTS` (used by Mixer/Bottler/Refinery/Squeezer/Fermenter
  category mixins). See [ie-potion-machines](ie-potion-machines.md).
- lang key `creativeengineering.jei.lingerification` = "Lingerification".

## Status
Lingerification confirmed working in-game 2026-09-07. Two follow-up fixes the same
day:
1. `JEIHelperMixin` `ordinal = 0` (above) — first cut broke every IE JEI page after the Refinery
   (bottling machine, arc furnace, IE mixer). Fixed.
2. `PotionRecipeGeneratorsMixin.creativeengineering$addMixer` — the 1000 mB "lingering" batch was
   also applied to `lingering + redstone/glowstone → modified lingering` steps; now gated
   `toType == LINGERING && fromType != LINGERING` so only the splash->lingering *conversion* is
   1000, modifiers are 1500. See [am-potion-brewing-fix](am-potion-brewing-fix.md).
Re-verify after a client restart (status: feature_writeup/potions.md).

## ie-jei-fluid-nbt-tooltips

IE's JEI categories render `create:potion` fluid slots with only IE's generic name + amount. The
pack routes lots of potion chemistry through `create:potion` NBT fluid ([ie-potion-machines](ie-potion-machines.md),
[ie-potion-machines](ie-potion-machines.md), [am-potion-brewing-fix](am-potion-brewing-fix.md)), so patcherMod client mixins append
the potion display name + per-effect duration/amplifier lines (Create-style,
`PotionFluidHandler.addPotionTooltip`) to those slots.

## Shared callback — `compat/PotionFluidTooltip.java` (not a mixin)
`public static final IRecipeSlotTooltipCallback POTION_EFFECTS` — reads the slot's displayed
`ForgeTypes.FLUID_STACK`; if it has a `Potion` tag, sets tooltip line 0 to `fs.getDisplayName()`
and calls `PotionFluidHandler.addPotionTooltip(fs, tooltip, 1f)`. **Pure no-op for any fluid
without a `Potion` tag** -> safe to attach to every fluid slot. Loaded lazily on first JEI view;
no `forceLoad` (the category mixins already ref Create classes fine).

## The category mixins (all `@Mixin(remap=false)`, `creativeengineering.mixins.json` `client`)
How IE builds each fluid slot in `setRecipe` decides the injection point:

| category | how the mixin attaches `POTION_EFFECTS` | slots covered |
|---|---|---|
| `MixerRecipeCategoryMixin` | `@Redirect addTooltipCallback` — 2 calls, one per fluid slot | input + output |
| `BottlingMachineRecipeCategoryMixin` | `@Redirect addTooltipCallback` — 1 call, on the fluid **input** | input |
| `RefineryRecipeCategoryMixin` | `@Redirect addTooltipCallback` (1 call = **output**) **+** `@Redirect IRecipeSlotBuilder.addIngredients(IIngredientType,List)` (2 calls = `input0`,`input1`; catalyst uses `addItemStacks`, output uses singular `addIngredient`) | input0 + input1 + output |
| `SqueezerRecipeCategoryMixin` | `@Redirect addTooltipCallback` — 1 call, fluid **output** | output (inert — squeezer = plant/seed oil) |
| `FermenterRecipeCategoryMixin` | `@Redirect addTooltipCallback` — 1 call, fluid **output** | output (inert — fermenter = ethanol) |

Redirect handler shape everywhere: `return slot.addTooltipCallback(ieCallback).addTooltipCallback(PotionFluidTooltip.POTION_EFFECTS);`
(for the Refinery `addIngredients` one: `return slot.addIngredients(type, ingredients).addTooltipCallback(POTION_EFFECTS);`
— returns the same `IRecipeSlotBuilder` so IE's follow-up `.setOverlay(..)` on input0 still works).

Squeezer/Fermenter are consistency-only (no potion recipes for them in the pack) — fine to drop
if IE ever changes those categories and they break.

## Status
Built 2026-09-07 (`./gradlew build --offline` clean, no refmap — JEI isn't obfuscated,
IE targets use literal names). Client-only; needs a client restart to verify. Verify: hover the
`create:potion` input on a Bottling Machine per-potion recipe / a Lingerification recipe -> potion
name + effect line shown.

## ie-bottler-nbt-fluids

Added 2026-09-06 (patcherMod [patchermod-coremod](patchermod-coremod.md)). Lets the IE Bottling Machine bottle
`create:potion` fluid into potion / splash potion / lingering potion items.

## Architecture (after the 2026-09-06 rework)

Started as a fancy patcherMod `dynamic_output` feature (5 mixins + `PotionFluidHandler.fillBottle`).
**That was ripped out** — a plain KubeJS loop generating one concrete recipe per potion is simpler,
shows properly in JEI, and needs no per-recipe Java. What remains:

### patcherMod — `mixin/FluidTagInputMixin` only (`@Mixin(FluidTagInput.class, remap=false)`)
`FluidTagInput` is IE's shared fluid-input predicate (Bottler + Mixer + Refinery). Two tweaks:
1. **Subset NBT match** — `@Redirect` the single `CompoundTag.equals` in `testIgnoringAmount`
   -> `NbtUtils.compareNbt(recipeNbt, stackTag, true)`. So a recipe `nbt:{Bottle,Potion}` matches
   a `create:potion` stack that also carries `CustomPotionColor` etc. nbt-less recipes unchanged.
   **This is the essential piece.**
2. Fluid-by-id in `deserialize` (`"fluid":"<id>"` -> `Either.right(List.of(rl))`) — **dormant**,
   nothing uses it, and kjs-ie blocks `"fluid"` keys anyway (see below). Kept, harmless.

Built into patcherMod 1.0.0. The old bottling mixins
(`MultiblockProcessBottlingMixin`, `BottlingMachineRecipe(Serializer)Mixin`,
`BottlingMachineBlockEntityMixin`, `bottling/` package, the 3 `forceLoad`s) are **deleted**.

### KubeJS — `kubejs/server_scripts/engineering/potion_bottling.js`
- `e.remove({id:'immersiveengineering:potion_bottling_list'})` — IE's own potion bottling is a
  single `generated_list` container recipe (`PotionRecipeGenerators.getPotionBottlingRecipes()`,
  `immersiveengineering:potion` fluid -> potion, regular only). Sub-recipes aren't individual
  RecipeManager entries; `CachedRecipeList` expands the container. Remove container = all gone.
  (Parallel: `immersiveengineering:mixer_potion_list` for IE-mixer potion mixing — leave for the
  mixer task.)
- Loop `Java.loadClass('net.minecraft.core.Registry').POTION.keySet()` (skip `minecraft:empty`),
  per potion × {REGULAR->potion, SPLASH->splash_potion, LINGERING->lingering_potion} emit
  `e.custom({type:'immersiveengineering:bottling_machine',
  fluid:{tag:'forge:fluid/potion', amount:250, nbt:'{Bottle:"..",Potion:"<id>"}'},
  inputs:[{item:'minecraft:glass_bottle'}], results:[{item:<out>, nbt:'{Potion:"<id>"}'}]})`.
  Fully static, one match per fluid stack (nbt disambiguates), covers every registered potion.

### Other recipes
- `redstone.js` (~line 12): `R.ieBottling(i.redstone, {tag:f.t_potion, amount:20,
  nbt:'{Bottle:"REGULAR",Potion:"minecraft:strength"}'}, i.cinder_flour, e,
  'redstone_from_cinder_flour_potion', 250)` — **confirmed working in-game + shows in JEI**.
  New dict key `f.t_potion = '#forge:fluid/potion'` in `fluid_dict.js`.
- `mob_resources.js:28-29` — creosote -> prepared_flesh(_block) `R.ieBottling` uncommented.
- JEI: `BottlingMachineRecipeCategoryMixin` adds the potion name + effect lines to the
  `create:potion` input slot tooltip — see [ie-potion-machines](ie-potion-machines.md).

## kjs-ie interception rules (learned 2026-09-06)

kubejs-immersive-engineering intercepts **every** `immersiveengineering:*` recipe (raw JSON too):
1. `IERecipeJS.readInputFluid` **hard-throws** on a by-id `"fluid"` key -> recipe silently fails
   to register. **Always use a fluid `#tag` in IE recipes**, never `"fluid":"<id>"`.
2. For cleanly-parsed recipes (tag fluid), `deserialize()` leaves `RecipeJS.json` untouched and
   `getOriginalRecipe()` re-serializes it verbatim to IE's serializer — so **unknown keys and
   `nbt` sub-keys survive** (same mechanism [sawblade-recipe-routing](sawblade-recipe-routing.md)'s `"blade"` relies on).
   Result-item `nbt` and fluid-input `nbt` both pass through fine.

## Status

- `redstone_from_cinder_flour_potion` + `create:potion` bottle-filling: **working in-game** (user
  confirmed). Create *filling* with `create:potion` nbt also confirmed.
- 2026-09-06 rework (remove IE potion bottling + generate per-potion + strip patcherMod dynamic
  code).

## IE Mixer -> create:potion  — DONE (see [am-potion-brewing-fix](am-potion-brewing-fix.md) "2026-09-07")

`PotionRecipeGeneratorsMixin` (output + input redirects) + `PotionHelperMixin` (un-skip
thick/mundane bases) shipped; amount now 1000 mB. All pack potion brewing/mixing logic
consolidated into `kubejs/startup_scripts/cooking/potions.js`. The draft below is the
original plan, kept for context.

## (original plan) Next — IE Mixer -> create:potion  (plan refined 2026-09-06)

`~/.claude/plans/ie-mixer-create-potion-mixing.md` — **much simpler than the old draft.**
`IETags.fluidPotion` is `forge:fluid/potion` (`forgeLoc("potion")`), which the pack tag
(`fluidtags.js`) already fills with `create:potion`; IE's own `PotionRecipeGenerators` mixer
recipes therefore already *accept* `create:potion` as input via the subset-NBT `FluidTagInputMixin`.
Only the **output** is wrong. Fix = ONE mixin: `@Mixin(PotionRecipeGenerators.class)` `@Redirect`
the `PotionFluid.getFluidStackForType(to,1000)` call in `registerPotionRecipe` ->
`PotionFluidHandler.getFluidFromPotion(potion, BottleType.REGULAR, amount)` (create:potion).
KEEP `immersiveengineering:mixer_potion_list` (we want those recipes, just redirected). No
findRecipe/getRecipeForId/JEI mixins, no recipe generation. Makes `immersiveengineering:potion`
fluid survival-unobtainable (bottling list already gone). Chemthrower potion spray + potion
bullets keep working (shared tag / item-based).

**2026-09-06: TWO redirects in `mixin/PotionRecipeGeneratorsMixin`** (both on
`registerPotionRecipe`):
1. `getFluidStackForType(to,1000)` (OUTPUT) -> `PotionFluidHandler.getFluidFromPotion(to,REGULAR,1000)`
   = `create:potion{Bottle:REGULAR,Potion:to}`.
2. `PotionHelper.getFluidTagForType(from,1000)` (INPUT) -> by-id `create:potion` FluidTagInput
   (`new FluidTagInput(Either.right(List.of("create:potion")), 1000, {Bottle:REGULAR,Potion:from})`),
   EXCEPT `from==Potions.WATER` keeps `new FluidTagInput(FluidTags.WATER, 1000)` (awkward base step).
   Redirect #2 added after the user reported the IE mixer still *displayed/took* IE potion inputs —
   the `#forge:fluid/potion` tag worked functionally but JEI showed `immersiveengineering:potion`
   first and the create:potion output didn't visually chain. Now in == out shape.

Registered in `creativeengineering.mixins.json`, `./gradlew build --offline` clean (reobf maps
`Potions.WATER`->`f_43599_`, `FluidTags.WATER`->`f_13131_`), jar in `mods/`. Needs restart +
verify (JEI `immersiveengineering:mixer` shows create:potion both sides & chains; water + nether
wart -> create:potion awkward; feed that awkward output back + glowstone -> night vision).
Amounts already matched Create (both 1000 mB). `immersiveengineering:potion` now never produced
*or* consumed. `fluidtags.js` cleanup still optional/not-done.

## Engineer's Workbench NBT ingredients — DONE 2026-09-06

IE `IngredientWithSize` / vanilla `Ingredient` already honor `forge:partial_nbt`/`forge:nbt`
everywhere (Workbench `getMaxCrafted`/`consumeInputs` are NBT-aware); the only blocker was
`NCH._itemJson` stripping NBT. Fix (workbench-only, per user): new private helper
`NCH._nbtItemJson(v, opts)` in `neoCraftHelper.js` (right after `_itemJson`) handles raw
`{type:'forge:partial_nbt'|'forge:nbt',item,nbt}` objects, `{item,nbt}` shorthand, and KubeJS
`Item.of('id','{nbt}')` (via `v.getTag()`); falls back to `_itemJson` when no NBT. `ieBlueprint`
now maps inputs/result through `_nbtItemJson` (`role:'input'|'result'`). **No other ie\*/create\*
helper touched.** Enabled: `redstone.js` `thunder_detector_ie_workbench` (mundane potion) +
`archery.js` `dousing_arrow` (water bottle). Plan: `~/.claude/plans/ie-nbt-ingredients.md`
(other processes surveyed there, deferred). Server-script change -> `/reload`.

## tipped-arrow-machine-tipping

Added 2026-09-06. `kubejs/server_scripts/engineering/tipped_arrows.js` — mirrors vanilla's
`crafting_special_tippedarrow` (8 arrows + 1 lingering potion -> 8 tipped arrows, still enabled)
into machines, one pair per registered potion (loops `Java.loadClass('net.minecraft.core.Registry').POTION`,
skips `minecraft:empty`), same as [ie-potion-machines](ie-potion-machines.md)'s `potion_bottling.js`.

- **IE Bottling Machine**: `e.custom({type:'immersiveengineering:bottling_machine', fluid:{tag:'forge:fluid/potion',
  amount:250, nbt:'{Bottle:"LINGERING",Potion:<id>}'}, inputs:[{item:'minecraft:arrow',count:8}],
  results:[{item:'minecraft:tipped_arrow',count:8,nbt:'{Potion:<id>}'}]})` — exact hand-craft ratio
  8:250:8. kjs-ie `BottlingRecipeSchema` honors `count` on InputItem/OutputItem; fluid subset-NBT
  match via patcherMod `FluidTagInputMixin`.
- **Create Spout / filling**: `R.createFilling(Item.of('minecraft:tipped_arrow', '{Potion:<id>}'),
  [Item.of('minecraft:arrow'), Fluid.of(f.create_potion, 31, '{Bottle:"LINGERING",Potion:<id>}')], e, 'tipped_arrow/<idBase>')`.
  Spout fills ONE item per op (`FillingBySpout.fillItem` shrinks stack by 1), so 250/8=31.25 is floored
  to **31 mB/arrow** (~0.8% player-favorable vs hand craft — Create can't express the exact ratio).

`R.ieBottling` / `_itemJson` can't emit result NBT -> raw `e.custom` used for the bottler (same as
potion_bottling.js). Exact-NBT match on the Create side means potions carrying extra NBT
(CustomPotionColor) won't match — fine for vanilla lingering potions.

Server-script only, `/reload` picks it up.
