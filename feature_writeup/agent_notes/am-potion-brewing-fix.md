# am-potion-brewing-fix

> Alex's Mobs potions use Forge BrewingRecipeRegistry (regular-item-locked); fixed by re-registering modifier mixes on vanilla PotionBrewing via MoreJS

Moved from agent memory 2026-11-16 (last edited there 2026-09-07). Repo status docs win over any status line here.

Added 2026-09-06. Audit of every potion brew in the pack for modifier order-interchangeability
(lengthen / amplify / negate / splash / linger in any order).

## Findings
- **Vanilla potions**: fully interchangeable (mixes in `PotionBrewing`, matched on potion NBT
  only, container item carried through). Splash=gunpowder / linger=dragon's breath are
  `CONTAINER_MIXES`, universal.
- **Quark potions**: fine — `vazkii.quark.base.handler.BrewingHandler` appends real
  `PotionBrewing$Mix` entries + uses `addNegation`.
- **Alex's Mobs potions**: the exception. `AMEffectRegistry.init()` registers everything via
  Forge `BrewingRecipeRegistry.addRecipe` / `ProperBrewingRecipe` (only overrides `isInput`),
  input locked to the **regular** `Items.POTION` + exact NBT, and Forge `BrewingRecipe.getOutput`
  always returns a regular potion. So AM lengthener/amplifier/gazelle-horn steps do **not** apply
  once a potion is splash/lingering, and are invisible to Create's Mechanical Mixer.
  AM potions: knockback_resistance (long+strong), lava_vision (long), poison_resistance
  (long, reagent = `alexsmobs:komodo_spit` not redstone), bug_pheromones (long), soulsteal
  (long+strong), clinging (long), speed_iii (`alexsmobs:gazelle_horn` on `minecraft:strong_swiftness`,
  terminal). No FSE negations defined for any AM potion.
- **Buzzier Bees** (luck/long/strong, unluck/long/strong, sunny): `blueprint DataUtil.addMix`
  → reflectively calls vanilla `PotionBrewing.addMix`. Fine.
- **Geode Plus** (`geodeforge`): custom `IBrewingRecipe` but written correctly — `isInput` compares
  `PotionUtils.getPotion` only, `getOutput` does `new ItemStack(input.getItem())` then setPotion, so
  container item is preserved. Fine.
- **PuzzlesLib** mods (e.g. Allurement): `PotionBrewingRegistryImplForge$MixBrewingRecipe` uses
  `PotionBrewing$Mix`. Fine.
- **SecurityCraft**: brews fake water/lava buckets (non-potion outputs). N/A.
- **Botania** `data/botania/recipes/brew/*`: Botania Brewery block + Brew item system, not the
  vanilla brewing stand. Separate system, N/A.
- MineColonies `ItemMagicPotion`/`DruidPotionEntity` = bespoke non-brewable, out of scope.
- Create's `PotionMixingRecipes.createRecipes()` reads `PotionBrewing.POTION_MIXES` +
  `CONTAINER_MIXES` + `BrewingRecipeRegistry.getRecipes()` filtered to `instanceof
  net.minecraftforge.common.brewing.BrewingRecipe` (AM's `ProperBrewingRecipe` extends it, so AM
  regular potions already reach the Mixer; GeodePlus's does NOT extend it). Not rebuilt on /reload.

**Conclusion: Alex's Mobs is the ONLY mod whose potions can't have lengthener/amplifier/upgrade
applied to their splash & lingering forms. No vanilla gap.**

## 2026-09-07 — ALL potion logic consolidated into `kubejs/startup_scripts/cooking/potions.js`

One file now does everything. Deleted: `startup_scripts/cooking/potion_brewing.js`,
`startup_scripts/changes/autobrewing_blacklist.js`, `server_scripts/cooking/potion_mixing.js`,
`server_scripts/magic/potion_mixing.js` (the last was a stale dup causing 30 "Duplicate recipe"
warnings). No potion server scripts remain.

`potions.js` structure:
- `CUSTOM_POTIONS` table — `{effect, dur, long?, strong?:[dur,amp], base, reagent}` per family.
  12 families: resistance, health_boost, absorption, glowing, saturation (dur 5t), wither,
  levitation, mining_fatigue, hunger, nausea, blindness, darkness. **reach / phantom_bane
  (better_beacons) dropped.** levitation brew reagent = `endersdelight:shulker_filet` (concrete
  item, not the `#endersdelight:shulker_loot` tag anymore). Lengthen = redstone, amplify =
  glowstone dust (constants).
- `AM_POTION_MIXES` — the 9 Alex's Mobs modifier steps, unchanged.
- `StartupEvents.registry('potion')` — registers `kubejs:<name>` / `long_` / `strong_` from the table.
- `MoreJSEvents.registerPotionBrewing` — `event.addPotionBrewing(Ingredient.of(reagent), from, to)`
  for every custom base/long/strong step + the AM steps. Real `PotionBrewing$Mix` = item-agnostic,
  so it also drives Create Automatic Brewing + Create Mixer + IE Mixer. Log: `[CE potions] added N`.
- `StartupEvents.postInit` — (a) **reflectively rebuild** `PotionMixingRecipes.ALL`/`BY_ITEM`
  by re-invoking the private `createRecipes()` + `sortRecipesByItem(List)` (via
  `Class.forName(...).getDeclaredMethod(...).setAccessible(true)`), then (b) the old
  dragon's-breath blacklist. **The rebuild is REQUIRED** — verified 2026-09-07 that Create's
  `PotionMixingRecipes.<clinit>` runs during mod setup, *before* MoreJS (FMLCommonSetup) adds
  our mixes, so the custom `kubejs:*` potions were absent from Automatic Brewing. Create's JEI
  category is populated ONLY from `PotionMixingRecipes.ALL` (see memory `create-automatic-brewing`),
  and `createRecipes()` reads `PotionBrewing.POTION_MIXES` (`f_43494_`) — the same list MoreJS
  `getMixes()` appends to — so a postInit rebuild picks everything up. Log line:
  `[CE potions] Automatic Brewing: rebuilt N recipes`. If it ever logs `rebuild FAILED`
  (reflective access blocked), fall back to a patcherMod `@Invoker` on those two methods.

**Machine coverage — nothing else needed:**
- Brewing stand: the Mix entries.
- Create Automatic Brewing + Mechanical Mixer: 1000 mB, automatic — BUT only after the
  postInit reflective rebuild above (Create's `<clinit>` runs too early to see the MoreJS
  mixes on its own). The old `potion_mixing.js` `create:mixing` recipes were a 1500 mB
  work-around for this and showed as a separate JEI "Mixing" entry, not Automatic Brewing.
- IE Mixer multiblock: IE's `mixer_potion_list` (`PotionRecipeGenerators` /
  `PotionHelper.applyToAllPotionRecipes`) is generated from the same `PotionBrewing.POTION_MIXES`.
  patcherMod redirects it — see below. **1500 mB batches** for regular + splash (deliberately
  larger than the 1000 mB brewing-stand / Create), **but lingering-output recipes run at 1000 mB**
  (`CREATIVEENGINEERING$LINGERING_AMOUNT`, keyed on `toType == LINGERING` in
  `PotionRecipeGeneratorsMixin.creativeengineering$addMixer`) — one dragon's-breath bottle
  converts a bucket of splash potion; Create/IE in step for lingering.
- Splash / lingering: vanilla `CONTAINER_MIXES` + patcherMod dragon-breath fluid brew.

## patcherMod mixins (rebuilt + deployed 2026-09-07)
- **`PotionRecipeGeneratorsMixin`** (existing) — `CREATIVEENGINEERING$AMOUNT` = **1500 mB**
  (pack design; IE stock + Create Automatic Brewing are 1000). Briefly set to 1000 then reverted
  2026-09-07 — user wants the IE-Mixer multiblock to brew in 1500 mB batches specifically.
  2 redirects on `registerPotionRecipe`: output →
  `create:potion{Bottle:REGULAR}`, input → by-id `create:potion` with same NBT (except
  `from==Potions.WATER` keeps `#minecraft:water`).
- **`PotionHelperMixin`** (NEW) — `IE PotionHelper.applyToAllPotionRecipes` hard-skips any mix
  whose OUTPUT potion `== Potions.MUNDANE` (`f_43600_`) or `== Potions.THICK` (`f_43601_`), so
  the IE Mixer could never make `create:potion{thick/mundane}` and every thick/mundane-based
  brew (health_boost, absorption, hunger, nausea, blindness, darkness, wither, saturation) was
  uncraftable there. Fix: 2 `@Redirect` on the `GETSTATIC` of those SRG fields → `return null`
  (output never null → neither `==` matches → branch skipped). `remap=false`, SRG field target
  strings, `opcode = Opcodes.GETSTATIC` — same shape as `PneumaticCraftUtilsMixin`. In
  `creativeengineering.mixins.json` `mixins`.

## 2026-09-07 (session 2) — IE splash/lingering parity + AM bottle-base fixes

Five holes closed (all rebuilt + deployed):
- **`potions.js` postInit `.class` bug** — `PMR.class` / `.class` is Nashorn syntax; KubeJS Rhino
  (`Java.loadClass` → NativeJavaClass) has no `.class` (hunts a `$class` nested class, throws).
  Fix: `PMR.__javaObject__` (Rhino's real-`java.lang.Class` accessor, verified in rhino jar
  `NativeJavaClass.get`). Also `const` inside the `try` → "redeclaration of var" (same Rhino quirk
  as the while-loop note) → function-scoped `var`. The whole `postInit` block was silently dead
  before this.
- **dragon-breath lingering gone from Create Automatic Brewing JEI** — `DragonBreathBrewing.build()`
  (commonSetup) appends 94 fluid recipes to `PMR.ALL`; the postInit rebuild's `PMR.ALL.clear()`
  (runs later) wiped them. Fix: `potions.js` re-appends `DragonBreathBrewing.FLUID_BREWING` after
  the rebuild (they carry no item ingredient → dragon_breath blacklist loop skips them). The
  Create *mixer* itself was fine all along (`MechanicalMixerBlockEntityMixin` reads FLUID_BREWING).
- **IE Mixer had NO splash/lingering, of anything** — IE `PotionHelper.applyToAllPotionRecipes`
  only walks `POTION_MIXES` (potion→potion) + `Items.POTION`-output BrewingRecipes; never
  `CONTAINER_MIXES` or splash/lingering forms. Fix: `PotionRecipeGeneratorsMixin` new
  `@Inject(method="initPotionRecipes", at=@At("RETURN"), cancellable)` — appends, as
  `create:potion` NBT fluids @1500 mB: SPLASH+LINGERING of every `POTION_MIXES` step, plus
  per-potion `CONTAINER_MIXES` conversions (`REGULAR+gunpowder→SPLASH`,
  `SPLASH+dragon_breath ITEM→LINGERING`). ~400 recipes. **This is the dragon's-breath ITEM's
  only use** (Create side stays fluid-based; asymmetry is deliberate — IE Mixer is 1 fluid + items,
  can't do fluid+fluid). Reads `PotionBrewing.POTION_MIXES`/`CONTAINER_MIXES` (`f_43494_`/`f_43495_`)
  by **reflection** (SRG name, Mojmap fallback) — `@Accessor` of MC fields fails the AP validator
  in this no-refmap project (see [patchermod-coremod](patchermod-coremod.md)).
- **lava_vision / poison_resistance uncraftable in both mixers** — AM registers their base brews
  from non-potion items (`alexsmobs:lava_bottle`, `alexsmobs:poison_bottle`), so
  `PotionUtils.getPotion` = EMPTY → Create Phase D skips them, IE builds an unobtainable
  `create:potion{Potion:"minecraft:empty"}` input. Fix: `PotionRecipeGeneratorsMixin` new
  `@Inject(method="registerPotionRecipe", at=@At("HEAD"), cancellable)` cancels when
  `from == Potions.EMPTY`; then:
  - **lava_vision** → explicit recipes in `kubejs/server_scripts/magic/lava_vision.js`:
    `create:mixing` 1000 mB `minecraft:lava` + `alexsmobs:bone_serpent_tooth` (heated) and
    `immersiveengineering:mixer` 1500 mB lava + tooth → `create:potion{alexsmobs:lava_vision}`.
    IE mixer `result` NBT must be an SNBT **string** (`ApiUtils.jsonDeserializeFluidStack` reads
    `tag` via `GsonHelper.getAsString`).
  - **poison_resistance** → re-based onto Potion of Poison II: added
    `['alexsmobs:centipede_leg','minecraft:strong_poison','alexsmobs:poison_resistance']` to
    `AM_POTION_MIXES` in `potions.js`. Now a real `PotionBrewing$Mix` → brews everywhere +
    gets splash/lingering parity free. AM's poison-bottle stand recipe still works.
  - `long_lava_vision` / `long_poison_resistance` were never broken (their `from` is a real potion).

New files: `mixin` additions in `PotionRecipeGeneratorsMixin` (no new mixin class — `@Accessor`
route abandoned), `kubejs/server_scripts/magic/lava_vision.js`.

### round 2b (same day) — from user testing feedback
- **`const DBB` in the nested `try`** → `redeclaration of var` (yet again). Moved to the
  function-scoped `var` line. This is why the dragon-breath fluid re-add silently failed and
  "fluid dragon breath mixing with create was totally missing" from JEI. `var` now, works.
- **lava_vision IE mixer recipe can't be a KubeJS `e.custom`** — kjs-ie ships
  `MixerRecipeSchema` (`result`=OutputFluid, `fluid`=InputFluid, `inputs`, `energy`); `e.custom`
  routes through it and the `create:potion` NBT is stripped off `result`. Moved the IE recipe
  into `PotionRecipeGeneratorsMixin` (`creativeengineering$addLavaVision`, appended in the
  parity inject): `new FluidTagInput(FluidTags.LAVA, 1500)` + `bone_serpent_tooth` →
  `create:potion{lava_vision}`. `lava_vision.js` now only does the Create `R.createMixing`
  (KubeJS's *create* schema handles NBT fluids fine).
- **duplicate modifier recipe per AM potion** (the "extra redstone mixing recipe") — AM registers
  each modifier step as a Forge `BrewingRecipe` AND the pack re-adds it as a `PotionBrewing.Mix`
  (`AM_POTION_MIXES`); Create phase 4 + IE loop 2 both also walk `BrewingRecipeRegistry` → dup.
  Fix: `PotionMixDedup.withoutMixDuplicates()` filters `BrewingRecipeRegistry.getRecipes()` to
  drop any `instanceof BrewingRecipe` whose `from>to` potion pair a `PotionBrewing.Mix` covers.
  Applied via `@Redirect` on the `getRecipes()` INVOKE in **both** `PotionHelperMixin`
  (`applyToAllPotionRecipes`, IE) and new **`PotionMixingRecipesMixin`** (`createRecipes`, Create).
  Only ever touches AM recipes (vanilla/Quark/BuzzierBees are `PotionBrewing.Mix` / non-`BrewingRecipe`
  `VanillaBrewingRecipe`, never filtered). Reagents in `AM_POTION_MIXES` match AM's so nothing lost.
- **IE Mixer JEI showed only fluid name, no potion effect lines** — Create's own JEI categories add
  those via `CreateRecipeCategory` (checks `fs.getFluid().isSame(AllFluids.POTION)` → sets
  `tooltip[0] = fs.getDisplayName()` + `PotionFluidHandler.addPotionTooltip(fs, tooltip, 1f)`); it's
  NOT on the fluid type, so IE categories don't get it. Fix: **`MixerRecipeCategoryMixin`** (client),
  `@Redirect` on the 2 `IRecipeSlotBuilder.addTooltipCallback(...)` calls in `setRecipe` — chains an
  extra callback that runs the same logic whenever the slot fluid has a `Potion` NBT key.

### round 3 — dedicated JEI category
- **Lingering IE-mixer recipes now run at 1000 mB**, everything else stays 1500 —
  `CREATIVEENGINEERING$LINGERING_AMOUNT` in `PotionRecipeGeneratorsMixin.creativeengineering$addMixer`,
  branch on `toType == LINGERING`. So one dragon's-breath bottle = one bucket of splash → lingering.
- **"Automated Potion Mixing" JEI category** (like Create's Automatic Brewing, separate from the
  real IE Mixer page). `compat/AutomatedPotionMixingCategory` **extends** IE's `MixerRecipeCategory`
  (reuses all slot layout / tank art / the tooltip mixin) — `IERecipeCategory.getRecipeType()` is
  `final` so the `private final type` field is swapped by **reflection** in the ctor (non-static
  final instance field → writable after `setAccessible(true)` on Java 17), plus `this.title`.
  `isPotionMixer(r)` = `r.fluidOutput.getFluid().isSame(AllFluids.POTION.get())`.
  Registered in the existing `DragonBreathingJEI` `@JeiPlugin` (categories + recipes filtered by
  `isPotionMixer` + IE Mixer multiblock as catalyst). **`JEIHelperMixin`** (client) `@Redirect`s the
  `getFiltered(MixerRecipe.RECIPES, …)` call in `JEIHelper.registerRecipes` (pinned by a `@Slice`
  from the `MixerRecipe.RECIPES` GETSTATIC) to also drop `isPotionMixer` recipes → they leave IE's
  Mixer page. Lang key `creativeengineering.jei.automated_potion_mixing`.
  - **Crash gotcha (fixed after 2 crashes):** that `@Redirect` handler must be **non-static**
    (matches `registerRecipes`) AND keep the receiver param — final working signature
    `private List<MixerRecipe> h(JEIHelper self, CachedRecipeList list, Predicate ieFilter)`.
    `private static` → `'static' modifier ... does not match target`; non-static without `self` →
    `unexpected argument type CachedRecipeList at index 0, expected JEIHelper`. Either failure fails
    the whole `required:true` config → hard crash at JEI common_setup.
  - `AutomatedPotionMixingCategory.ACTIVE` (volatile bool, set in `DragonBreathingJEI.registerCategories`
    inside a `try/catch(Throwable)`) — the category ctor's reflection failing now degrades gracefully
    (potions stay in IE's Mixer page, `JEIHelperMixin` skips its filter) instead of crashing.

Potion display names: `kubejs/assets/kubejs/lang/en_us.json` — every `long_`/`strong_` variant
needs its own `item.minecraft.{potion,splash_potion,lingering_potion,tipped_arrow}.effect.<path>`
key (KubeJS uses the registry PATH as the potion name; vanilla shares the base name).
`dump_potions.js` (`/dumppotions`) → `kubejs/exported/{mob_effects,potions}.json`.

## Note: needs a restart (patcherMod jar changed → full restart, not just /reload).
In-game verify:
- Brewing stand brews awkward+iron_block → Potion of Resistance, thick+golden_apple → Health Boost,
  levitation via awkward + shulker filet.
- JEI: custom potions show in Create **Automatic Brewing** (not a separate Mixing recipe) @ 1000 mB.
- IE Mixer: water+redstone→create:potion{mundane}, then +golden_apple→absorption; wither (Decay)
  long/strong chain now completes.
- AM: brew lava_vision → linger → +redstone still lengthens.
