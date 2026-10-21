# Potions and brewing: custom potions, one potion fluid for every machine, splash/lingering parity, JEI

## 0. At a glance

| Part | Status |
|---|---|
| 12 `kubejs:*` potions from vanilla effects (resistance, health boost, absorption, glowing, saturation, wither, levitation, mining fatigue, hunger, nausea, blindness, darkness) with base / long / strong brews | DEPLOYED (in the 2026-09-27 initial commit; shipped since 1.0.3, live in 1.0.6); no test ids before this writeup |
| Java-registered potions for modded effects: Long Reach, Phantom Bane (Better Beacons), Magic Resistance (pack) | DEPLOYED; Magic Resistance reagent is a placeholder |
| Brewing mixes as item-agnostic `PotionBrewing.Mix` (MoreJS): one list drives the brewing stand, Create Automatic Brewing, the Create mixer and the IE Mixer | DEPLOYED |
| Alex's Mobs fixes (modifier steps on splash/lingering, poison resistance re-based, lava vision base on both mixers) and Rustic Delight haste made item-agnostic; duplicate-recipe trim (`PotionMixDedup`) | DEPLOYED (built 2026-09-06 to 09-08), in-game check never recorded |
| Create Automatic Brewing rebuilt at load so the pack's potions are in it; item dragon's breath removed; dragon's breath **fluid** turns splash into lingering (heated mixer) | DEPLOYED |
| IE Mixer on Create's `create:potion` fluid (1500 mB), with splash and lingering parity, thick/mundane bases allowed | DEPLOYED |
| IE Refinery "Lingerification": regular or splash + 250 mB dragon's breath fluid → lingering | DEPLOYED |
| IE Bottling Machine fills bottles from `create:potion` (IE's own potion bottling removed); IE fluid inputs by id and by NBT subset (`FluidTagInputMixin`) | DEPLOYED |
| JEI: Automated Potion Mixing and Lingerification pages, potion name and effects on IE fluid slots, every fluid in the ingredient list, Potion Bundles fixes for the Spout and Item Drain pages | DEPLOYED |
| Potion Bundles drain whole (3 charges, right bottle type) | DEPLOYED; a per-drain `TEMP diagnostic` log line is still in |

- **Pending:** nothing built and unshipped. PO-01 to PO-26 have never been run; the September in-game checklist was
  never recorded either ([todo.txt](todo/todo.txt) "Recently built, never explicitly closed out"). Findings F1 to F8
  (section 4) are open; F1 and F2 need your call.
- **Files to open:** [potions.js](../kubejs/startup_scripts/cooking/potions.js) (all potions and mixes),
  [PotionRecipeGeneratorsMixin.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/PotionRecipeGeneratorsMixin.java) (IE Mixer),
  [fluidbrewing/](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/fluidbrewing/) (dragon's breath, Lingerification, dedup),
  [potion_bottling.js](../kubejs/server_scripts/engineering/potion_bottling.js).
- **Knobs:** none in the toml. Potions, reagents and durations are code in `potions.js` (restart). Batch sizes and
  energy are constants in the Java classes (rebuild). Create's own `allowBrewingInMixer` (world server config) also
  gates the dragon's breath fluid brew.
- **How to look:** JEI → any potion → R. The log at load prints `[CE potions] added N item-agnostic potion mixes`,
  `Automatic Brewing: rebuilt N recipes`, `IE Mixer: +N splash/lingering potion recipes`, `Lingerification: generated N
  refinery recipes`, `potion mixer dedup: hid N BrewingRecipe(s)`.

## 1. Overview

Out of the box in 1.19.2 (Forge 43.3.9, Create 0.5.1.i, IE 9.2.4):

- **Brewing stand:** vanilla `PotionBrewing` mixes plus Forge `BrewingRecipe`s that mods register. Mods often lock
  theirs to the regular potion item and exact NBT, so a splash or lingering version can't be lengthened.
- **Create:** one potion fluid, `create:potion`, carrying `Potion` and `Bottle` (REGULAR / SPLASH / LINGERING) NBT.
  Automatic Brewing (Mechanical Mixer over a basin) is generated from the brewing lists at 1000 mB; the Spout fills
  bottles at 250 mB; the Item Drain empties them.
- **IE:** its own `immersiveengineering:potion` fluid. The Mixer gets regular-potion recipes generated from the
  brewing lists (no splash or lingering, and nothing from a thick or mundane base); the Bottling Machine has a
  generated potion bottling list.
- **Dragon's breath:** a vanilla item used to turn splash into lingering; Create: Central Kitchen adds a
  `create_central_kitchen:dragon_breath` fluid.

The pack picks `create:potion` as the one potion fluid, routes every IE machine onto it, makes every brew work for
every bottle type on every machine, and adds potions for effects that had none.

## 2. Reference

### 2.1 Pack potions (`kubejs:*`, from vanilla effects)

Lengthen is always redstone, amplify always glowstone dust. Times are base / long / strong (amplifier).

| Potion | Base | Reagent | Base | Long | Strong |
|---|---|---|---|---|---|
| Resistance | awkward | iron block | 3:00 | 8:00 | 1:30 II |
| Health Boost | thick | golden apple | 3:00 | 8:00 | 1:30 II |
| Absorption | mundane | golden apple | 2:00 | 5:00 | 2:00 II |
| Glowing | awkward | glow ink sac | 1:30 | 4:00 | |
| Saturation | thick | golden carrot | 5 ticks | | |
| Wither | mundane | wither rose | 0:40 | 1:30 | 0:20 II |
| Levitation | awkward | shulker filet (Ender's Delight) | 0:40 | 1:30 | 0:20 II |
| Mining Fatigue | awkward | prismarine crystals | 1:30 | 4:00 | 0:30 II |
| Hunger | thick | rotten flesh | 0:30 | 1:00 | 0:15 II |
| Nausea | thick | pufferfish | 0:30 | 1:00 | |
| Blindness | mundane | ink sac | 0:30 | 1:00 | |
| Darkness | mundane | echo shard | 0:30 | 1:00 | |

No haste here: Rustic Delight already has one (2.3).

### 2.2 Java-registered potions (`creativeengineering:*`)

KubeJS's potion registry runs before any mod's mob effects exist, so potions for modded effects are a Forge
`DeferredRegister<Potion>` in patcherMod. All three are 3:00 / 8:00 / 1:30 II, from thick.

| Potion | Effect | Reagent | Class |
|---|---|---|---|
| Long Reach | `better_beacons:long_reach` | wrappist shard (Geode+) | `BetterBeaconsPotions` |
| Phantom Bane | `better_beacons:phantom_bane` | celestite shard (Geode+) | `BetterBeaconsPotions` (must be Better Beacons' real effect object: its phantom-spawn check compares by identity) |
| Magic Resistance | `creativeengineering:magic_resistance` | amethyst shard (**placeholder**) | `MagicResistancePotions` ([armor_effects.md](armor_effects.md) 2.5) |

### 2.3 Other mods' potions made to work everywhere

| Source | Steps re-added as item-agnostic mixes | Why |
|---|---|---|
| Alex's Mobs | lengthen: knockback resistance, lava vision, bug pheromones, soulsteal, clinging; komodo spit → long poison resistance; amplify: knockback resistance, soulsteal; gazelle horn + Swiftness II → Speed III | AM locks them to the regular potion item; splash/lingering couldn't be modified and the mixers couldn't see them |
| Alex's Mobs | centipede leg + Poison II → Poison Resistance | AM brews it from a poison-bottle item, which no mixer can use; AM's own stand recipe still works too |
| Alex's Mobs | Lava Vision base: Create mixer 1000 mB lava + bone serpent tooth, heated ([lava_vision.js](../kubejs/server_scripts/magic/lava_vision.js)); IE Mixer 1500 mB lava + tooth (`PotionRecipeGeneratorsMixin`) | AM brews it from a lava-bottle item |
| Rustic Delight | golden coffee beans + awkward → Haste; redstone, glowstone steps | RD's own `IBrewingRecipe` class is invisible to both mixers |
| Buzzier Bees / vanilla | four-leaf clover + awkward → Luck, redstone → long; silverfish bottle + awkward → Unluck, redstone → long | Luck had no brew. Reagent marked "TODO: better/magic ingredient" (`potions.js` line 86). Out of date: Buzzier Bees already brews Luck from the clover, so the first two rows are duplicates ([effects.md](effects.md) F6) |

### 2.4 Machines

| Machine | What it does | Batch | Set up by |
|---|---|---|---|
| Brewing stand | every mix above, every bottle type | 1 to 3 bottles | MoreJS `registerPotionBrewing` |
| Create Automatic Brewing (Mechanical Mixer + basin) | every mix and container conversion, rebuilt after MoreJS adds the pack's | 1000 mB | `potions.js` part 3 (postInit) |
| Create mixer, dragon's breath | splash + 250 mB dragon's breath fluid → lingering, **heated**; the item dragon's breath recipe is removed | 1000 mB | `DragonBreathBrewing` + `MechanicalMixerBlockEntityMixin` |
| IE Mixer | every mix in REGULAR, SPLASH and LINGERING; per potion REGULAR + gunpowder → SPLASH and SPLASH + dragon's breath item → LINGERING; thick/mundane bases | 1500 mB (into lingering: 1000 mB) | `PotionRecipeGeneratorsMixin`, `PotionHelperMixin` |
| IE Refinery, Lingerification | REGULAR or SPLASH + 250 mB `#forge:fluid/dragon_breath` → LINGERING, 120 RF/t | 1500 mB | `Lingerification` + `data/creativeengineering/recipes/lingerification_list.json` |
| IE Bottling Machine | 250 mB `create:potion` + glass bottle → potion / splash / lingering item, per potion | 250 mB | [potion_bottling.js](../kubejs/server_scripts/engineering/potion_bottling.js) |
| Create Spout / Item Drain | Create's own bottle fill and empty | 250 mB | Create |

`#forge:fluid/potion` is only `create:potion` (IE's potion fluid is taken out of it, [fluidtags.js](../kubejs/server_scripts/tags/fluidtags.js)
line 66). Potion buckets and tipped arrows on these machines: [potion_buckets.md](potion_buckets.md).

### 2.5 JEI

- **Automated Potion Mixing** (catalyst: IE Mixer): every IE Mixer recipe that outputs `create:potion`, moved off
  IE's Mixer page. Same layout as the Mixer page.
- **Lingerification** (catalyst: IE Refinery): the refinery recipes that output lingering `create:potion`, moved off
  IE's Refinery page.
- Both fall back to IE's own pages if the category fails to build (logged as "JEI category disabled").
- **Potion lines on fluid slots:** IE's Mixer, Bottling Machine, Refinery, Squeezer and Fermenter pages show the
  potion's name and effect lines on any fluid slot with a `Potion` tag, like Create's own pages.
- **Fluid list:** [fluid_jei.js](../kubejs/client_scripts/fluid_jei.js) adds every still fluid (not `flowing_*`, not
  the dead placeholders) to JEI's ingredient list, and one regular `create:potion` stack per potion.
- **Create's Automatic Brewing page** shows the dragon's breath fluid brews (re-added after the rebuild clears the
  list).

### 2.6 Potion Bundles (Potion Bundles 1.6)

- Item Drain or hose pulley on a bundle: the whole bundle, `Uses` × 250 mB (750 mB full), in the right bottle type
  (splash and lingering bundles aren't Create's splash/lingering items, so Create read them as regular), plus that
  many glass bottles (`PotionFluidHandlerMixin`).
- JEI: no fake "Spout fills a bundle" recipe (`SpoutCategoryMixin`); the Item Drain page shows the full amount
  (`ItemDrainCategoryMixin`; its bottle count still shows 1).

## 3. What the pack changes

- **One file for potions:** [potions.js](../kubejs/startup_scripts/cooking/potions.js) registers the `kubejs:*` potions,
  adds every mix, and rebuilds Automatic Brewing. A mix whose potion isn't registered logs `[CE potions] mix skipped`.
- **Why `Mix`, not `BrewingRecipe`:** Create's and IE's generators read `PotionBrewing.POTION_MIXES` for every bottle
  type; a Forge `BrewingRecipe` is item-specific. One list then feeds every machine.
- **Automatic Brewing rebuild:** Create fills `PotionMixingRecipes.ALL` / `BY_ITEM` in a static block during mod setup,
  before MoreJS adds the pack's mixes. At postInit the script calls the private `createRecipes()` /
  `sortRecipesByItem()` again by reflection and swaps the lists in, re-adds `DragonBreathBrewing.FLUID_BREWING`
  (which the clear wiped), and removes every recipe with a dragon's breath item ingredient.
- **Dedup:** Alex's Mobs steps exist both as AM's `BrewingRecipe` and as the pack's `Mix`, so both generators made
  each one twice. `PotionMixingRecipesMixin` (Create) and `PotionHelperMixin` (IE) redirect
  `BrewingRecipeRegistry.getRecipes()` to `PotionMixDedup.withoutMixDuplicates`, which drops a `BrewingRecipe` whose
  from → to pair a `Mix` already covers.
- **IE Mixer on `create:potion`:** `PotionRecipeGeneratorsMixin` redirects the input and output fluids of
  `registerPotionRecipe` to `create:potion` (water stays plain water), skips recipes with an `EMPTY` input (AM's
  bottle bases), and at the end of `initPotionRecipes` appends the splash and lingering copies, the container
  conversions and the lava vision base. `PotionHelperMixin` turns IE's `== MUNDANE` / `== THICK` skips into no-ops.
- **IE fluid inputs:** `FluidTagInputMixin` lets an IE recipe JSON name a fluid by id (`"fluid": "create:potion"`), and
  matches recipe NBT as a subset of the stack's NBT instead of exact equality (so extra keys such as
  `CustomPotionColor` don't break a match). It applies to every IE recipe with a fluid input: strictly more
  permissive.
- **Bottling:** `potion_bottling.js` removes `immersiveengineering:potion_bottling_list` (the only way to remove its
  generated sub-recipes) and writes one bottling recipe per potion × bottle type.
- **Create mixer and fluid reagents:** the mixer only looks up item reagents (`BY_ITEM`), so
  `MechanicalMixerBlockEntityMixin` appends any `FLUID_BREWING` recipe that matches the basin to
  `getMatchingRecipes()`.
- **Lingerification:** registered as an IE generated-list generator (`creativeengineering:lingerification_list`)
  pulled in by a one-line datapack recipe, so the sub-recipes are real `RefineryRecipe`s the Refinery and JEI see.
- **JEI categories:** registered in `DragonBreathingJEI` (the pack's JEI plugin); each swaps its IE parent's private
  `type` field by reflection. `JEIHelperMixin` filters the same recipes out of IE's own pages.

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1. Batch sizes differ between machines. Your call.** Create 1000 mB; IE Mixer 1500 mB, but 1000 mB for the step
  into lingering "so Create and IE stay in step"; Lingerification 1500 mB + 250 mB dragon's breath. So 250 mB of
  dragon's breath makes 1000 mB of lingering in the Create mixer and 1500 mB in the Refinery, and one dragon's breath
  bottle makes 1000 mB in the IE Mixer. Either is fine; the comment in the IE Mixer class implies they were meant to
  line up.
- **F2. Lingering from regular, only on the Refinery. Your call.** Lingerification takes regular or splash; the Create
  mixer and the IE Mixer only convert splash (vanilla's rule). Keep the Refinery as the shortcut, or drop its
  regular → lingering recipes.
- **F3. Placeholder reagents:** Magic Resistance (amethyst shard, `potions.js` line 72) and Luck (four-leaf clover,
  line 86). In [TODO](todo/TODO.md) `gear/potions #4`.
- **F4. `TEMP diagnostic` log on every bundle drain** (`PotionFluidHandlerMixin` line 62). One INFO line per bundle
  emptied. In [TODO](todo/TODO.md) `tooling/patchermod-cleanup #1`; delete it.
- **F5.** FIXED 2026-10-19 (javadoc names `potions.js` part 3). **Stale doc:** `DragonBreathBrewing`'s javadoc says the item dragon's breath mixes are stripped by
  `kubejs/startup_scripts/changes/dragon_breath.js`; there is no such file, it's `potions.js` part 3.
- **F6. Raw `e.custom` in `potion_bottling.js`.** Bottling, filling and emptying are hand-written JSON, against the
  NCH-helper rule in CLAUDE.md. `NCH.ieBottling` already exists (use it with an NBT fluid) and `NCH.createFilling`
  covers the Spout; only a `createEmptying` helper is missing (also used by [potion_buckets.md](potion_buckets.md)).
- **F7. A bottling recipe for every potion, including ones no machine can fill.** The loop skips only
  `minecraft:empty`; `minecraft:water` gets `create:potion{Potion:water}` recipes, but Create stores water bottles
  as plain water, so those never run. Harmless, they only add JEI entries.
- **F8. Saturation is 5 ticks** with no long or strong version, so it's a small instant top-up of food. Drink one while hungry
  to see how much it restores, and say if it should be stronger.

### Known limits

- **Automatic Brewing isn't rebuilt on `/reload`:** brewing lists are startup data; any change needs a restart.
- **Item-locked mod brews:** a mod brew not re-added as a `Mix` (2.3) only works on the brewing stand with the exact
  item it was written for.
- **Fluid list shows regular only:** splash and lingering `create:potion` stacks look the same and aren't added.
- **Potion Bundles' JEI drain entry still shows 1 bottle** back; the real drain gives one per charge.

### Rejected options

- **Per-potion KubeJS recipes for each machine:** hundreds of recipes per machine and per bottle type, and every new
  potion would need its own. The generators already walk the brewing lists; the mixins point them at one fluid.
- **Keeping IE's potion fluid:** two incompatible potion fluids; Create's has the bottle type, IE's doesn't.
- **Dragon's breath item in Create's mixer:** removed for the fluid version (CCK makes the fluid; the mixer can't hold
  250 mB of an item).

## 5. File map

- [startup_scripts/cooking/potions.js](../kubejs/startup_scripts/cooking/potions.js): potions, mixes, Automatic Brewing rebuild.
- [server_scripts/engineering/potion_bottling.js](../kubejs/server_scripts/engineering/potion_bottling.js): IE bottling per potion (and bucket recipes, see potion_buckets.md).
- [server_scripts/magic/lava_vision.js](../kubejs/server_scripts/magic/lava_vision.js): Create mixer lava vision base.
- [client_scripts/fluid_jei.js](../kubejs/client_scripts/fluid_jei.js): fluids in JEI's list.
- [server_scripts/tags/fluidtags.js](../kubejs/server_scripts/tags/fluidtags.js): `#forge:fluid/potion`, `#forge:fluid/dragon_breath`.
- patcherMod [fluidbrewing/](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/fluidbrewing/): `DragonBreathBrewing`, `Lingerification`, `PotionMixDedup`; run from common setup (`CreativeEngineering.java` lines 262-263).
- patcherMod [potion/](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/potion/): `BetterBeaconsPotions`, `MagicResistancePotions` (registered line 85-86).
- Mixins: [PotionRecipeGeneratorsMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/PotionRecipeGeneratorsMixin.java), [PotionHelperMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/PotionHelperMixin.java), [PotionMixingRecipesMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/PotionMixingRecipesMixin.java), [MechanicalMixerBlockEntityMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/MechanicalMixerBlockEntityMixin.java), [FluidTagInputMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/FluidTagInputMixin.java), [PotionFluidHandlerMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/PotionFluidHandlerMixin.java), [SpoutCategoryMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/SpoutCategoryMixin.java), [ItemDrainCategoryMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/ItemDrainCategoryMixin.java), [JEIHelperMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/JEIHelperMixin.java), and the IE category tooltip mixins (Mixer, BottlingMachine, Refinery, Squeezer, Fermenter `...RecipeCategoryMixin`).
- JEI: [compat/DragonBreathingJEI.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/DragonBreathingJEI.java) (registers both pages), [AutomatedPotionMixingCategory](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/AutomatedPotionMixingCategory.java), [LingerificationCategory](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/LingerificationCategory.java), [PotionFluidTooltip](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/PotionFluidTooltip.java); lang keys in patcherMod `assets/creativeengineering/lang/en_us.json`.
- Not here: the dragon-breathing fan type (backlog row 6), potion buckets and machine-tipped arrows ([potion_buckets.md](potion_buckets.md)), effects on gear ([armor_effects.md](armor_effects.md)).

## 6. Offline checks

None scripted. The log lines in section 0 are the quickest check after a boot: each count should be non-zero, and
there should be no `[CE potions] mix skipped` or `Automatic Brewing rebuild FAILED` line. All mixin targets are
`remap = false` against Create, IE and Potion Bundles jars that aren't in the repo; only a boot proves them.

## 7. In-game test suite

Creative for setup, survival-like for the machines. Use JEI (R / U) and F3+H.

- [ ] **PO-01 Load log:** after a boot, `logs/latest.log` has non-zero counts for all five lines in section 0 and no
  `mix skipped` or `FAILED` line.
- [ ] **PO-02 Pack potion, stand:** brewing stand, awkward potion + iron block → Potion of Resistance; + redstone →
  8:00; + glowstone → Resistance II 1:30.
- [ ] **PO-03 Thick and mundane bases:** thick + golden apple → Health Boost; mundane + echo shard → Darkness.
- [ ] **PO-04 Modded effect potions:** thick + wrappist shard → Long Reach; thick + celestite shard → Phantom Bane;
  drink Phantom Bane and skip three nights → no phantoms.
- [ ] **PO-05 Splash modifiers (AM):** splash Potion of Knockback Resistance + redstone in a brewing stand → long
  splash version (failed before the fix).
- [ ] **PO-06 Poison resistance re-base:** Potion of Poison II + centipede leg → Poison Resistance; AM's poison bottle
  route still works.
- [ ] **PO-07 Haste:** awkward + golden coffee beans → Haste, in the brewing stand and in the Create mixer.
- [ ] **PO-08 Automatic Brewing has pack potions:** JEI Automatic Brewing page shows awkward + iron block →
  Resistance. A heated basin with 1000 mB awkward potion + an iron block under a Mechanical Mixer brews it.
- [ ] **PO-09 No duplicate AM recipes:** JEI Automatic Brewing for long lava vision → exactly one recipe per bottle type.
- [ ] **PO-10 Dragon's breath item gone from Create:** JEI shows no Automatic Brewing recipe with a dragon's breath item.
- [ ] **PO-11 Dragon's breath fluid, Create:** heated basin, 1000 mB splash Swiftness + 250 mB dragon's breath fluid,
  mixer → 1000 mB lingering Swiftness. Unheated → nothing. JEI Automatic Brewing lists the recipe.
- [ ] **PO-12 IE Mixer regular:** IE Mixer with 1500 mB awkward `create:potion` + iron block → 1500 mB Resistance
  `create:potion`.
- [ ] **PO-13 IE Mixer splash parity:** 1500 mB splash Resistance + redstone → long splash Resistance.
- [ ] **PO-14 IE Mixer container conversions:** 1500 mB regular + gunpowder → splash; 1000 mB splash + dragon's breath
  item → 1000 mB lingering.
- [ ] **PO-15 IE Mixer thick base:** 1500 mB thick + golden apple → Health Boost (blocked before `PotionHelperMixin`).
- [ ] **PO-16 IE Mixer lava vision:** 1500 mB lava + bone serpent tooth → Lava Vision. Create mixer: 1000 mB lava +
  tooth, heated → Lava Vision.
- [ ] **PO-17 Lingerification:** IE Refinery, 1500 mB regular Resistance + 250 mB dragon's breath fluid → 1500 mB
  lingering Resistance; same from splash.
- [ ] **PO-18 IE Bottling:** Bottling Machine, `create:potion` Resistance + glass bottles → Potions of Resistance
  (250 mB each). Splash and lingering fluids give splash and lingering bottles. IE's own `immersiveengineering:potion`
  bottling recipes are gone from JEI.
- [ ] **PO-19 JEI pages:** JEI shows "Automated Potion Mixing" with the IE Mixer as catalyst and "Lingerification" with
  the Refinery; IE's own Mixer and Refinery pages have no potion recipes.
- [ ] **PO-20 JEI fluid tooltips:** hover the fluid slots on Automated Potion Mixing, Lingerification and the Bottling
  Machine page → potion name and effect lines (e.g. "Resistance (3:00)"), not just "Potion 1500 mB".
- [ ] **PO-21 JEI fluid list:** search `@create` or "Potion of" in the ingredient list → `create:potion` stacks per
  potion; dragon's breath fluid present; no `flowing_*` entries.
- [ ] **PO-22 Bundle drain:** a full splash potion bundle into an Item Drain → 750 mB splash `create:potion` + 3 glass
  bottles; the bundle is used up.
- [ ] **PO-23 Bundle JEI:** the Spout page has no recipe that fills a potion bundle; the Item Drain page shows 750 mB
  for a full bundle.
- [ ] **PO-24 Strong Magic Resistance:** thick + amethyst shard → Magic Resistance; + glowstone → Magic Resistance II.
- [ ] **PO-25 Luck:** awkward + four-leaf clover → Potion of Luck; + redstone → long Luck.
- [ ] **PO-26 Speed III:** Swiftness II + gazelle horn → Speed III (AM), on the brewing stand and in the Create mixer.

## 8. History

- 2026-09-06 to 09-08: potion fluid rework: IE Mixer, Bottler and Refinery on `create:potion`, splash/lingering parity,
  Lingerification, dedicated JEI pages, IE fluid tooltips, AM brewing fixes. Built and deployed; the in-game checklist
  was never recorded.
- By 2026-09-27 (initial git commit): all of the above plus the `kubejs:*` potions, Better Beacons and Magic Resistance
  potions, Rustic Delight haste, Luck; shipped with 1.0.3.
- 2026-10-18: this writeup; findings F1 to F8; tests PO-01 to PO-26. No code changed.
- 2026-10-19: F5 fixed (DragonBreathBrewing javadoc; source only, ships with the next patcherMod build).
