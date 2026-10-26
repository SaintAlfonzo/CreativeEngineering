# mobs / mooblooms: implementation brief

Written 2026-10-21 from [TODO](../todo/TODO.md) `# mobs`, Sub `mooblooms`. Feature writeup:
[mooblooms.md](../mooblooms.md) (test prefix `MB-`, highest id in use MB-98). Mod source:
`development/moreMooblooms/` (More Mooblooms 1.0.0, `more_mooblooms`, GPLv3, the pack's own mod).

## Scope and order

| Row | Change | Status | Size |
|---|---|---|---|
| #4 | Skylands cows and mooblooms | Planned | small: data (rule file + one biome modifier), `/reload` + new chunks |
| #3 | Per-biome moobloom shares (D47) retune | In Progress | small to medium: data, best done through the `worldgen/flowers #2` generator |
| #5 | Azaleabloom texture | Planned | small: one PNG, then the 1.1 jar |
| #2 | More Mooblooms 1.1 JEI | BUILT 2026-10-22 (what was built: mooblooms.md 5) | medium: Java in the mod's `compat/jei/`, lang, one new public setter on the entity |

Build order:

1. **#4 first, on its own.** Pure data, no jar. It rewrites two of the rows #3 retunes (skylands spring and
   summer), so do it before #3 and treat its rows as fixed (user-set) in #3.
2. **#3 with `worldgen/flowers #2`.** That row's brief ([worldgen_flowers.md](worldgen_flowers.md) flowers #2)
   plans `tools/gen_moobloom_spawns.py`, which regenerates the rule file from the flower inventory and keeps the
   user's hand-set rows in `moobloom_spawn_tiers.json`. Retuning by hand before that generator exists is wasted:
   the file is already stale against the 2026-10-19 rarity floor and the moonlight flower conversion. #4's skylands
   rows become three `flower_biomes` entries there.
3. **#5 and #2 together, as More Mooblooms 1.1.0.** Both change the jar; one version bump, one deploy (both sides:
   it is one jar for client and server). Paint #5 first and hot-swap it through `kubejs/assets/` (no build), then
   fold it into the jar with #2.

Cross-family: `worldgen/biomes #3` (skylands at multiplier 0.15, under 0.1 % of the surface together: #4 works but
few players will find it; [worldgen_biomes.md](worldgen_biomes.md)), `worldgen/flowers #2` (generator, above),
`ui/effects #5` (its Details say Sunny is "still on the HUD from buttercup stew and mooblooms"; with More Mooblooms
neither source exists any more, see Built below).

---

## mooblooms #4: Skylands cows and mooblooms
Status Planned / Work data / TODO `mobs/mooblooms #4` (mob_spawns.md 9 Later; mooblooms.md 4.9)

### Goal
"Ensure cows in skyland, 100 % mooblooms" (scratchpad 2026-10-19) and "add mooblooms to spring-autumn skylands,
100 % even split between all available flowers on skyland" (mob_spawns.md 9 Later): every cow that spawns in
skylands spring, summer and autumn becomes a moobloom, split evenly over the variants whose flowers grow there.

### Now
- Cows per skylands biome (verified against `Terralith_1.19.3_v2.3.12.jar`, `data/terralith/worldgen/biome/skylands_*.json`
  `spawners.creature`):

  | Biome | Terralith creatures | Cows |
  |---|---|---|
  | `terralith:skylands_spring` | pig 12, chicken 10, rabbit 10 (groups 4) | none from Terralith; the pack adds cow 8 (4-4) in [moobloom_cows.json](../../kubejs/data/creativeengineering/forge/biome_modifier/moobloom_cows.json) |
  | `terralith:skylands_summer` | sheep 12, pig 10, chicken 10, cow 8 (groups 8), parrot 6 | cow 8 (8-8) |
  | `terralith:skylands_autumn` | sheep 12, cow 8 (4-4), goat 4, fox 4 | cow 8 (4-4) |
  | `terralith:skylands_winter` | goat 10, sheep 8, rabbit 4 | none |

  So spring to autumn already have cows; only winter has none. The pack's `chicken_sky` rule
  ([mob_spawns.json](../../kubejs/generation_scripts/worldgen/mob_spawns.json)) adds chickens to all four; nothing
  touches cows there (`cow_plains` scales `plains` biomes only).
- Moobloom rules in [creativeengineering.json](../../kubejs/data/more_mooblooms/moobloom_spawns/creativeengineering.json):
  line 5 `terralith:skylands_summer` (50 %: sunbloom 37.5, moobloom 8, oxeyebloom 4.5) and line 27
  `terralith:skylands_spring` (75 %: onionbloom 15.75, rosebloom 12, sunbloom 11.25, bluebloom 10.5, lilybloom 10.5,
  moobloom 9.75, peonybloom 5.25). Both are the user's D49 flower-biome rows (mooblooms.md 4.9). No autumn rule:
  autumn cows only get `rareChance` 0.002.
- Pool flowers that grow there (verified in the Terralith jar plus the pack's `wild_flowers.json`):
  - spring: `terralith:skylands/spring/flowers` = allium, cornflower, dandelion, orange tulip, pink tulip, red tulip,
    white tulip; plus `minecraft:flower_default` (dandelion, poppy) -> **7 variants**: moobloom, sunbloom (dandelion,
    orange tulip), rosebloom (red tulip, poppy), peonybloom (pink tulip), onionbloom (allium, pink tulip),
    bluebloom (cornflower), lilybloom (white tulip). Matches mooblooms.md 2.8's spring mix.
  - summer: `terralith:skylands/summer/tall_flowers` = sunflower 9, dandelion 2, oxeye daisy 1 -> **3 variants**:
    sunbloom, moobloom, oxeyebloom.
  - autumn: `flower_default` (dandelion, poppy) plus the pack's `savanna_flowers` patch (orange tulip 3, dandelion 2,
    red tulip 2, poppy 1; `where` includes `autumn`) -> **3 variants**: moobloom, sunbloom, rosebloom.
  - winter (not asked for): `snow_flowers` (azure bluet, white tulip, lily of the valley, oxeye daisy) -> oxeyebloom,
    lilybloom.

### Build
1. In `creativeengineering.json`, replace the line 5 and line 27 rules and add an autumn rule (exact biome ids, so
   they win over any tag rule, D11):
   ```json
   { "biomes": ["terralith:skylands_spring"],
     "replace": [ {"variant": "more_mooblooms:moobloom", "percent": 14.28}, {"variant": "more_mooblooms:sunbloom", "percent": 14.28},
                  {"variant": "more_mooblooms:rosebloom", "percent": 14.28}, {"variant": "more_mooblooms:peonybloom", "percent": 14.29},
                  {"variant": "more_mooblooms:onionbloom", "percent": 14.29}, {"variant": "more_mooblooms:bluebloom", "percent": 14.29},
                  {"variant": "more_mooblooms:lilybloom", "percent": 14.29} ] },
   { "biomes": ["terralith:skylands_summer"],
     "replace": [ {"variant": "more_mooblooms:sunbloom", "percent": 33.34}, {"variant": "more_mooblooms:moobloom", "percent": 33.33},
                  {"variant": "more_mooblooms:oxeyebloom", "percent": 33.33} ] },
   { "biomes": ["terralith:skylands_autumn"],
     "replace": [ {"variant": "more_mooblooms:moobloom", "percent": 33.34}, {"variant": "more_mooblooms:sunbloom", "percent": 33.33},
                  {"variant": "more_mooblooms:rosebloom", "percent": 33.33} ] }
   ```
   Spring and autumn have different mixes, so they stay separate rules; summer and autumn have the same count but
   different variants.
2. Cows: spring to autumn need nothing. If winter should have cows too (Decisions), add
   `"terralith:skylands_winter"` to the `biomes` list of `moobloom_cows.json` (cow 8, groups of 4, same as spring).
3. `python3 tools/check_moobloom_data.py` (sums must be at most 100; it prints `37 spawn rules` today, 38 after).
4. Docs: mooblooms.md 4.9 flower-biome table (summer row 50 % -> 100 %, spring 75 % -> 100 %, new autumn row; note
   "D50: skylands spring to autumn 100 %, even split, the user 2026-10-19"), 4.12 row for `moobloom_cows.json` if
   winter is added, "In numbers" (the skylands are ~0.06 % of the surface, so the total barely moves), MB-97;
   mob_spawns.md 9 Later: tick the line.
5. If `worldgen/flowers #2` has landed first: put the three rows into `moobloom_spawn_tiers.json` `flower_biomes`
   as `{"bloom": 100, "split": "even"}` instead, where "even" must then mean "the variants whose flowers grow in
   this biome", not the fixed 9 flower variants of blooming valley / meadow (see Decisions).

### Ids and data
Variant ids as above (all in `development/moreMooblooms/src/main/resources/data/more_mooblooms/moobloom_variants/`).
Biome ids verified in the Terralith jar; the `#terralith:skylands` tag holds exactly the four seasons (there is a
fifth biome file `terralith:skylands` that is in no tag and not in `biome_config.json`, so it never generates).

### Decisions
- **"Even split over available flowers": per variant or per flower?** Recommend per variant (above): it matches
  how the other "even" rules are written (blooming valley, meadow, flower forest). Per flower would weight a
  variant by how many of its flowers grow there (spring: rosebloom 25 %, onionbloom 18.75 %, peonybloom 6.25 % ...).
- **Winter cows?** The row says "cows in skylands", but spring to autumn already have them. Recommend adding plain
  cows to winter only if the user wants winter mooblooms too; otherwise leave winter as is (goats, sheep, rabbits).
- **Summer/spring were the user's own D49 splits (50 % / 75 %).** This row replaces them at the user's own request;
  record it as a new decision rather than editing D49's text.

### Gotchas
- Percentages over 100 in one rule log a warning and are scaled down; 7 x 14.29 = 100.03, hence the 14.28 / 14.29 mix.
- Only new spawns change, and chunk-generation herds only in new chunks. Skylands are hard to find
  (`worldgen/biomes #3`): test with `/locate biome`.
- `check_moobloom_data.py` can't check biome ids offline here (no `kubejs/exported/biome_data.json` in the repo).

### Tests
- **MB-97 (reword):** skylands spring, new chunks -> every cow is a moobloom, the 7 spring variants in roughly equal numbers.
- **MB-99 Skylands summer and autumn:** `/locate biome terralith:skylands_summer` / `_autumn`, new chunks -> no cows;
  summer an even mix of sunbloom / moobloom / oxeyebloom, autumn of moobloom / sunbloom / rosebloom.
- Offline: `check_moobloom_data.py` OK with 38 rules.

---

## mooblooms #3: Per-biome moobloom shares (D47)
Status In Progress / Work data / TODO `mobs/mooblooms #3` (mooblooms.md 4.9, 4.13 D47-D49)

### Goal
Settle how many cows become mooblooms in each biome, and which variants. First pass is in; the user retunes after
playing.

### Now
- One rule file, [creativeengineering.json](../../kubejs/data/more_mooblooms/moobloom_spawns/creativeengineering.json):
  35 per-biome rules plus lush caves (100 % azaleabloom); the mod's switch pack adds the witherbloom rule. Offline
  check 2026-10-21: `python3 tools/check_moobloom_data.py` -> `11 variants, 10 breeding rules, 37 spawn rules`, OK.
- Flower-biome tier (D49): the user's per-biome bloom % and splits (mooblooms.md 4.9, retuned 2026-10-11). The
  25 % and 2 % tiers are still Claude's first pass (D47), made by hand from the 2.8 tables.
- Stale since 2026-10-19 (from [worldgen_flowers.md](worldgen_flowers.md) flowers #2): 2.8's Area % (rarity floor,
  1.0.9) and the moonlight flower patch conversion; so the "about 10.4 mooblooms per 100 cows" figure has moved too.
- In-game counts not taken yet: MB-38, MB-94, MB-95, MB-96 open.

### Build
1. Wait for, or build, `tools/gen_moobloom_spawns.py` (`worldgen/flowers #2`). Move the D49 table and #4's skylands
   rows into its `moobloom_spawn_tiers.json`; the tier thresholds (1.0 / 0.5 / 0.25) and percents (flower / 25 / 2)
   become its `tiers`.
2. Regenerate, diff against the current file, show the changed rows to the user.
3. Play test (Tests), collect the user's retune, edit the tiers JSON (not the rule file), regenerate, `/reload`.
4. When the user accepts the shares: move this row to Testing, close D47 in mooblooms.md 4.13 ("user-confirmed
   <date>"), refresh 4.9 "In numbers".

Without the generator (fallback): edit the rule file directly and `/reload`; rules are `{"biomes": [...], "replace":
[{"variant": ..., "percent": ...}]}`, first match by exact id (D11). Keep sums at 100 or under.

### Decisions
- **Retune by hand now, or after the generator?** Recommend after: by-hand rows would be overwritten by the first
  generator run anyway.
- **Keep the 2 % tier?** It covers 26.9 % of the surface (34 biomes) and gives most of the "few in 100" mooblooms in
  ordinary biomes. Ask the user after MB-95.

### Gotchas
- Equal cow density is assumed in every "per 100 cows" figure; `cow_plains` doubles cows in plains biomes, so plains
  mooblooms are under-counted in the averages.
- Biomes that gain or lose cows (mob_spawns rows) change where rules matter; a rule in a cow-less biome does nothing.

### Tests
MB-38, MB-94, MB-95, MB-96 (existing). Counting: stand in the biome after it generates and run
`/execute if entity @e[type=more_mooblooms:moobloom,distance=..200]` and the same for `minecraft:cow`.

---

## mooblooms #5: Azaleabloom texture
Status Planned / Work new jar / TODO `textures/art #2` (scratchpad 2026-10-19)

### Goal
"Grey parts to brown; more azalea rings" on the azaleabloom coat.

### Now
- Coat: `development/moreMooblooms/src/main/resources/assets/more_mooblooms/textures/entity/moobloom/azaleabloom.png`,
  64x32, cow UV layout, olive base `#6C8031` (853 px) with lighter / darker greens, and about nine 3x3 pink-purple
  "rings" (`#D07BE3`, `#BA62CE`, `#9E5088`: the flowering-azalea blossoms).
- The grey parts (read from the PNG 2026-10-21) are the pixels every coat shares (the cow template):
  horns (u 22-25, v 0-3), muzzle on the head front (u 7-12, v 11-13), hooves (bottom row v 31 of the leg strip, and
  the leg undersides u 8-11, v 16-19), plus an unused muzzle copy at u 53-58, v 11-13 that the cow model never
  draws. Eyes are black `#000000` / `#13110C`; keep them.
- Hot-swap path (mooblooms.md 4.4): `kubejs/assets/more_mooblooms/textures/entity/moobloom/azaleabloom.png`, F3+T,
  client only, no build. The working copies folder `textureswapper/moobloom/` named in mooblooms.md 6 is not in the
  repo (local to the user's machine).

### Build
1. Repaint horns, muzzle and hooves in azalea-stem browns (take the palette from vanilla
   `minecraft:block/azalea_plant.png` / `azalea_side.png` so it reads as the same plant); keep eyes and the pink udder.
2. Add more 3x3 rings, mostly on the body sides and back (u 18-62, v 4-32), so the coat reads as flowering azalea.
3. Test through the hot-swap path; when the user likes it, copy it into the mod's assets and delete the
   `kubejs/assets` copy (it would otherwise ship as a dead duplicate, as the 11 loose coats did until 2026-10-14).
4. `python3 development/moreMooblooms/tools/egg_colors.py` (write), then `--check`; then
   `python3 tools/gen_moobloom_variants.py --check` (egg colours live in the variant JSON).
5. Ships with #2 as 1.1.0.

### Gotchas
- **`egg_colors.py` masks "pixels identical on every coat"** (`shared_pixels`, egg_colors.py:30). Repainting
  azaleabloom's horns and hooves takes those pixels out of the mask for **all** coats, so other coats' egg colours
  can shift (the pink udder once became the spot colour of the white and blue coats). Run `--check` and look at all
  11 lines; if others change, make the mask "identical on all coats but one" or a fixed template mask.

### Tests
- **MB-104 Azaleabloom coat:** `/summon more_mooblooms:moobloom ~ ~ ~ {Variant:"more_mooblooms:azaleabloom"}` ->
  brown horns, muzzle and hooves, more pink rings; the egg colours still look right for all 11 eggs in JEI.

---

## mooblooms #2: More Mooblooms 1.1 JEI
Status BUILT 2026-10-22, jar not installed (as built: mooblooms.md 5) / Work new jar / TODO `mobs/mooblooms #2` (mooblooms.md 5)

### Goal
mooblooms.md 5: show breeding outputs, show shearing outputs with each flower's chance and the entity rendered with
its flowers, better (short, human) info text.

### Now
- [MoreMoobloomsJeiPlugin.java](../../development/moreMooblooms/src/main/java/com/SaintAlfonzo/more_mooblooms/compat/jei/MoreMoobloomsJeiPlugin.java):
  subtypes per variant egg (line 64), the breeding category (72), catalyst = the plain egg (77), `registerRecipes`
  (82): breeding recipes + one info page per variant egg; `breedingRecipes()` (116) makes one recipe per
  combination result only; `poolOdds` (135); `onRuntimeAvailable` (164) hides the buttercup, BB's egg and Sunny.
- [MoobloomBreedingCategory.java](../../development/moreMooblooms/src/main/java/com/SaintAlfonzo/more_mooblooms/compat/jei/MoobloomBreedingCategory.java):
  132x26, parent egg + parent egg -> calf egg, chance text. No food, no entity render, no "breeds true" or "random"
  pages.
- Lang: `src/main/resources/assets/more_mooblooms/lang/en_us.json` lines 16-25 (`jei.more_mooblooms.*`).
- Entity API for a render: `Moobloom.setVariant` (public), `getFlowers()` reads the synced `DATA_FLOWERS`; the
  setter `setFlowers` (Moobloom.java:164) is **private** and `refreshFlowers` returns on the client, so a JEI render
  can't set flowers today. The flower layer (`client/MoobloomFlowerLayer.java:46`) draws whatever `getFlowers()`
  returns when `showsFlowers()`.
- Drop maths (Moobloom.java `grownFlowerDrops` / `poolDrops`, 4.6): both shears give, per item, an expected
  `3 x (sum of that item's entry weights / poolWeight) x chance`; the first shear depends on the flowers actually
  grown, the second rolls the pool. So one "per shear" number per item is right for both.
- **Just Enough Breeding** (`justenoughbreeding-forge-1.19.2-1.4.0.jar`, client, in the pack; verified by
  decompiling): its entity list is hardcoded per mod (`config/integrated/*Integration`), no config or data, so it
  can't be taught our moobloom. With Buzzier Bees loaded it registers a page for **`buzzier_bees:moobloom` + wheat**
  (`BuzzierBeesIntegration.init`, gated in `JEBIntegration` line 263), keyed on BB's spawn egg: a page for a mob
  More Mooblooms removes. Whether JEI still shows it after our plugin hides BB's egg is unverified (check in game).
  Its entity render (`utils/Utils.renderEntity`) is the model to copy: it draws with the category's PoseStack and
  re-creates the entity every 3 s.

### Build
1. **Entity:** add `public void setDisplayFlowers(List<BlockState> states)` to `Moobloom` (wraps the private
   `setFlowers`, client-safe: `SynchedEntityData.set` works on a client-side entity). Comment: JEI render only.
2. **Shearing category** `compat/jei/MoobloomShearingCategory.java` (new), one recipe per variant:
   ```java
   public record Recipe(MoobloomVariant variant, ItemStack egg, List<ItemStack> drops, List<Double> perShear) {}
   // setRecipe: INPUT egg slot; catalyst shears (registerRecipeCatalysts: Items.SHEARS); one OUTPUT slot per
   //   distinct drop item (merge like poolOdds), .addTooltipCallback(...) -> "%s per shear (%s%% per spot)";
   //   when shearBareToCow: an OUTPUT slot with the cow spawn egg + tooltip "second shear: back to a cow"
   // draw: render a client Moobloom (MMEntities.MOOBLOOM.get().create(mc.level), setVariant, setDisplayFlowers)
   //   with the pool cycling every ~1.5 s: slot i shows pool entry (i + tick/30) % pool.size(), so every flower
   //   appears; copy JEBr's PoseStack-based render (not InventoryScreen.renderEntityInInventory, which ignores the
   //   PoseStack JEI hands to draw()).
   ```
   Cache one entity per recipe (JEBr keeps one and recreates it every 3000 ms).
3. **Breeding outputs** in `MoobloomBreedingCategory` / `breedingRecipes()`:
   - add each parent's food as a slot under its egg (`food(v)` already exists in the plugin);
   - combination pairs: keep the combo page, and add the miss outcome on the same page (same variant -> that
     variant at `1 - chance`, e.g. moobloom + moobloom: sunbloom 10 %, moobloom 90 %; otherwise "random");
   - 11 "breeds true" pages (A + A -> A, minus any combo for that pair, via `MoobloomBreeding.ruleFor`);
   - one "random calf" page: two plain eggs -> one slot cycling every variant with `breed_weight > 0`, the % per
     variant in the tooltip (`breed_weight / total`; witherbloom 0, never).
   Grow the background (132x26 -> about 150x46) for the food slots.
4. **Hide JEBr's BB moobloom page** in `onRuntimeAvailable` (only when BB is loaded), with no compile dependency on
   JEBr (API verified in `jei-1.19.2-forge-11.8.1.1034.jar`):
   ```java
   IRecipeManager rm = runtime.getRecipeManager();
   rm.getRecipeType(new ResourceLocation("justenoughbreeding", "breeding")).ifPresent(type -> {
       ItemStack bbEgg = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("buzzier_bees", "moobloom_spawn_egg")));
       IFocus<ItemStack> f = runtime.getJeiHelpers().getFocusFactory().createFocus(RecipeIngredientRole.INPUT, VanillaTypes.ITEM_STACK, bbEgg);
       hide(rm, type, f);   // generic helper: rm.createRecipeLookup(type).limitFocus(List.of(f)).includeHidden().get().toList() -> rm.hideRecipes(type, list)
   });
   ```
   Only if the in-game check shows the page.
5. **Info text** (lang lines 17-25): rewrite short, e.g. pool: "Grows: 50% Rose Bush, 40% Poppy, 10% Red Tulip.";
   feed: "Feed a cow one of these: 10% chance each to turn it into this moobloom."; shear: "Shears: takes its
   flowers, it goes bare. Eats grass to regrow."; bare: "Shear it bare: back to a cow (+3 flowers)."; breed: "Breeds
   and grows on its flowers, not wheat." Drop the explanations the new pages now show (bred-from lines can go once the
   breeding pages show it). Show the user the new text before building.
6. Version: `mod_version=1.1.0` in `development/moreMooblooms/gradle.properties`; `./gradlew :moreMooblooms:build`,
   `installToMods`; `tools/build_deploy.py` picks the new jar up (`IN_TREE_MODS`, line 111). Channel `PROTOCOL` "2"
   (SyncVariantsPacket.java:27) can stay: the packet doesn't change.
7. Docs: mooblooms.md 0 (JEI row), 4.2 (version), 5 (replace the plan with what was built), 6 file map, 9 History;
   README of the mod (the `jei` row).

### Decisions
- **Breeding: our own category, or teach JEBr?** Own category (recommended; JEBr has no extension point).
- **Shearing page per variant, or one page per variant x shear stage?** One page per variant with the bare-shear
  outcome as an extra slot (recommended: 11 pages, not 22).
- **Show all 66 parent pairs?** No (recommended): combination pairs, breeds-true and one random page cover every
  outcome without 66 near-identical pages.

### Gotchas
- JEI builds its recipes from the client's synced variant table (sent before recipes/tags, 4.14); the new pages
  must read `MoobloomVariants.values(true)` like the existing code.
- The entity render needs `Minecraft.getInstance().level`; JEI recipe pages only draw in a world, but guard for null.
- `Moobloom.getVariant()` falls back to the default variant for an unknown id: create the entity, then
  `setVariant` before the first render frame.
- Optional pool entries (onionbloom's `farmersdelight:wild_onions`) are already skipped at load when the mod is
  missing, so `v.pool()` is safe to list.

### Tests
- **MB-100 JEI shearing:** JEI uses of shears -> "Moobloom Shearing", 11 pages; rosebloom shows rose bush 1.5,
  poppy 1.2, red tulip 0.3 per shear; witherbloom wither rose 0.3; the rendered moobloom cycles through every pool
  flower; the cow egg slot appears (and is gone with `shearBareToCow = false` after a restart).
- **MB-101 JEI breeding outputs:** rosebloom egg -> pages for rosebloom + rosebloom -> rosebloom, rosebloom +
  lilybloom -> peonybloom 35 % / random 65 %, the random-calf page (no witherbloom); each parent shows its food.
- **MB-102 Info text:** each egg's info page reads in a few short lines, numbers match the config.
- **MB-103 No BB moobloom in JEI:** JEI search "moobloom" -> no Just Enough Breeding page for Buzzier Bees' moobloom.
- MB-71, MB-81 (existing) still pass.

---

## mooblooms #6: Sunflowers face backwards on the Moobloom JEI tab
Status **Bug** / Work patcherMod (the More Mooblooms mod) / TODO `mobs/mooblooms #6`

### Goal
The sunflower on a sunbloom shown in the Moobloom Conversion tab faces the viewer.

### Now
- Sunbloom's flowers (`moobloom_variants/sunbloom.json`): `minecraft:sunflower[half=upper]` (weight 5), dandelion,
  orange tulip. The sunflower head faces one fixed direction in its block model (east), so a mob turned to the viewer
  shows it from behind.
- The tab draws the mob through `MoobloomShearingCategory.renderEntity` (line 138; body yaw `(x - mouseX)/2 + 35`, a
  `ZP 180` flip) with `moobloom.setDisplayFlowers(states)` (`MoobloomConversionCategory.entity` line 117). The flowers
  are drawn by `client/MoobloomFlowerLayer.renderSlot` (line 66-86): translate by the slot, `YP` rotation by
  `slot.yaw()`, scale `(-s, -s, s)`.
- Whether the same backwards sunflower shows in the world (and in the Shearing / Breeding tabs) is unverified: look first.

### Build
1. Compare a sunbloom in the world with the three JEI tabs. If only JEI is wrong, the cause is the tab's body yaw
   (35 degrees plus the viewer flip); if all are wrong, it is the layer.
2. Layer fix: in `renderSlot`, when the state is a sunflower (`state.is(Blocks.SUNFLOWER)`), add 180 to `slot.yaw()`.
   Tab-only fix: rotate the sunflower slots in `renderEntity` by 180.
3. Rebuild and install the jar (`./gradlew :moreMooblooms:build --offline`, `:installToMods`) with the game closed.

### Decisions
None; fix at the layer if the world is wrong too.

### Tests (MB-)
- MB-105 (proposed): sunbloom in the world and on all three Moobloom JEI tabs: the sunflower head faces the viewer or
  the cow's front, consistently.

---

## Built, needs play

| Row | Change | Status | Notes |
|---|---|---|---|
| #1 | More Mooblooms 1.0 | Testing | DEPLOYED with pack 1.0.6 (2026-10-14), jar 1.0.0 unchanged since. Offline checks pass 2026-10-21 (`check_moobloom_data.py` OK, `gen_moobloom_variants.py --check` 0 files, `egg_colors.py --check` clean). Client tests per mooblooms.md 8. Nothing stale found in the row |

## Writeup corrections made

- [mooblooms.md](../mooblooms.md) 6 (file map, Coats row): `textureswapper/moobloom/` marked as a local folder, not in the git repo.
- [mooblooms.md](../mooblooms.md) 5: "Still planned, for 1.1" now links this brief (#2).
