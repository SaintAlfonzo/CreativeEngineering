# Potion buckets and tipped arrows: bucket-sized potion clouds, and tipping arrows in machines

## 0. At a glance

| Part | Status |
|---|---|
| Three potion buckets (`creativeengineering:potion_bucket`, `splash_potion_bucket`, `lingering_potion_bucket`): right-click a block to burst a dome of potion clouds | DEPLOYED (in the 2026-09-27 initial commit; shipped since 1.0.3, live in 1.0.6); the dome shape was never confirmed in game |
| Dispenser bursts a bucket (`PotionBucketDispenser`) | DEPLOYED, in-game test pending |
| Recipes: bucket ↔ 4 bottles in the crafting grid, Create Spout fill and Item Drain empty, IE Bottling Machine fill (1000 mB) | DEPLOYED; never confirmed in game ([todo.txt](todo/todo.txt)) |
| JEI pages for Tipped Arrows and Potion Buckets (config `[jei]` toggles `tippedArrowCategory`, `potionBucketCategory`) | BUILT 2026-10-22, not in `mods/` yet; see [jei_coverage.md](jei_coverage.md) |
| Vanilla potion bottles leave a glass bottle when used in a crafting grid | DEPLOYED (fixed 2026-09-12); never confirmed in game |
| Vanilla-style effect tooltip on the buckets, two-layer tinted textures | DEPLOYED |
| Machine-tipped arrows: IE Bottling Machine and Create Spout, lingering `create:potion` | DEPLOYED; cheaper than the crafting-grid recipe (F1) |

- **Pending:** nothing built and unshipped. PB-01 to PB-23 have never been run. Findings F1 to F7 (section 4) are
  open; F1 and F3 need your call.
- **Files to open:** [potion_area.js](../kubejs/startup_scripts/func/potion_area.js) (all the cloud tuning),
  [server_scripts/engineering/potion_buckets.js](../kubejs/server_scripts/engineering/potion_buckets.js) (right click),
  [potion_bottling.js](../kubejs/server_scripts/engineering/potion_bottling.js) (bucket recipes, from line 23),
  [tipped_arrows.js](../kubejs/server_scripts/equipment/tipped_arrows.js).
- **Knobs:** `global.potionArea.BUCKETS` in `potion_area.js` (radius, growth, linger time, wait, effect multiplier,
  rings per bucket) and `DEFAULT_EFFECTS`. Startup script: restart. Arrow tipping amounts are in `tipped_arrows.js`
  (`/reload`).
- **Get one:** `/give @s creativeengineering:lingering_potion_bucket{Potion:"minecraft:strong_regeneration"}`.

## 1. Overview

Vanilla has drinkable, splash and lingering bottles. A lingering bottle makes one flat `AreaEffectCloud` (radius 3,
shrinking, 30 s, quarter-length effects). Tipped arrows are made only in the crafting grid: 8 arrows around a
lingering potion → 8 tipped arrows (Arrow of X with one eighth of the potion's duration).

The pack adds a bucket-sized potion container in three tiers that bursts a bigger, short-lived, growing cloud where
you click, and lets Create and IE machines tip arrows from the potion fluid ([potions.md](potions.md)).

## 2. Reference

### 2.1 Buckets

Unstackable, Brewing tab, carry the potion as vanilla bottles do (`Potion`, `CustomPotionEffects`,
`CustomPotionColor` NBT). Not drinkable. Leave a `minecraft:bucket` when used.

| Bucket | Bottom radius | Growth / tick | Lasts | Wait | Effect × | Rings |
|---|---|---|---|---|---|---|
| Potion Bucket | 1.0 | 0.030 | 35 ticks (1.75 s) | 16 | 1.0 | 1 |
| Splash Potion Bucket | 1.5 | 0.040 | 50 ticks (2.5 s) | 8 | 1.1 | 2 |
| Lingering Potion Bucket | 2.0 | 0.050 | 100 ticks (5 s) | 4 | 1.2 | 4 |

- **Rings:** one `AreaEffectCloud` per ring. Ring *i* of *n* sits `radius × i/n` above the click with radius
  `radius × sqrt(1 − (i/n)²)` and proportionally slower growth, so the stack reads as a dome. Every ring lasts the
  bucket's full time and carries the same effects.
- **Effects:** each effect at `duration × effectMult`; instant effects unscaled. The pack adds them to the clouds as
  custom effects, which (as I read vanilla's `AreaEffectCloud`) apply at full length, not the quarter a vanilla
  lingering bottle gives.
- **Blank bucket** (no potion NBT, only from `/give`): Night Vision 8 s.
- **Colour:** `CustomPotionColor`, else the average of the effect colours.
- **Sound:** a bucket-empty splash, once per burst.

### 2.2 Using one

- **Right click:** main hand, against a block only (never in the air). Spawns at the hit point + 0.1 up; you are the
  clouds' owner. Non-creative: the bucket becomes an empty bucket. 1 s cooldown on the item (the item is read before
  the stack shrinks; until 2026-10-23 it was read after and landed on air, so survival had no cooldown). A bucket of a
  potion with no effects (awkward, thick, mundane, water) does nothing and is kept; only a bucket with no potion NBT
  at all bursts the Night Vision default.
- **Dispenser:** bursts at the centre of the block in front, + 0.1 up, with no owner; the dispenser keeps the empty
  bucket. If the KubeJS handler failed to install, it ejects the bucket like vanilla and logs a warning.

### 2.3 Making and emptying them

One bucket = 1000 mB = 4 bottles. The tier must match the bottle type (splash bottles make a splash bucket).

| Route | In | Out |
|---|---|---|
| Crafting (shapeless) | empty bucket + 4 bottles of the same potion | potion bucket + 4 glass bottles back |
| Crafting (shapeless) | potion bucket + 4 glass bottles | 4 bottles + empty bucket back |
| Create Spout | empty bucket + 1000 mB `create:potion` | potion bucket |
| Create Item Drain (`create:emptying`) | potion bucket | empty bucket + 1000 mB `create:potion` |
| IE Bottling Machine | empty bucket + 1000 mB `#forge:fluid/potion` | potion bucket |

One recipe per potion per bucket type for each route (15 per potion), for potions with effects only (2026-10-23).
Water gets no recipes at all (Create never holds it as `create:potion`); awkward, thick and mundane keep their 3 IE
bottle recipes but get no bucket recipes. Machine tipped arrows also skip no-effect potions.

### 2.4 Tooltip

Vanilla bottle style: one line per effect, "Name II (0:30)", blue for beneficial, red for harmful; "No Effects" on a
blank bucket. No attribute lines (vanilla's "+20% Speed" appendix). Durations are the potion's, not × `effectMult`
(F2).

### 2.5 Machine-tipped arrows

Lingering `create:potion` only; one recipe pair per potion.

| Route | In | Out | Potion per 8 arrows |
|---|---|---|---|
| Crafting grid (vanilla) | 8 arrows + 1 lingering bottle | 8 tipped arrows | 250 mB (and now a glass bottle back) |
| IE Bottling Machine | 1 arrow + **20 mB** | 1 tipped arrow | 160 mB |
| Create Spout | 1 arrow + **25 mB** | 1 tipped arrow | 200 mB |

## 3. What the pack changes

- **Items:** KubeJS ([startup_scripts/items/potion_buckets.js](../kubejs/startup_scripts/items/potion_buckets.js)):
  `.color()` tints layer 1 (the liquid) with the potion colour; `.containerItem('minecraft:bucket')` is the crafting
  remainder for the drain recipe. Textures and models: `textures/item/<id>.png` + `<id>_liquid.png`,
  [tools/potion_bucket_texture.py](../tools/potion_bucket_texture.py).
- **Bottle remainder:** `ItemEvents.modification` sets a glass-bottle crafting remainder on `minecraft:potion`,
  `splash_potion` and `lingering_potion` (`setCraftingRemainder`; the first try used `containerItem`, which doesn't
  exist on a modified item). This is global: every crafting-grid recipe that uses up a potion bottle now hands the
  bottle back (F3).
- **Cloud logic:** one function, `global.potionArea.spawn` ([potion_area.js](../kubejs/startup_scripts/func/potion_area.js)),
  used by both the right click and the dispenser. An earlier single spherical cloud by mixin was removed for the ring
  stack (pure KubeJS, no rebuild).
- **Dispenser:** KubeJS can't register a dispenser behaviour, so patcherMod's `PotionBucketDispenser` registers one per
  bucket in common setup and calls a handler that `potion_area.js` installs at startup.
- **Rhino workarounds** (all commented in place): colour blended by hand because `PotionUtils.getColor` is an
  ambiguous overload; `getOrCreateTag()` because `getTag()` isn't visible; `getLevel()` / `x()` methods instead of
  the same-named fields; a fixed 0.1 nudge because `Direction.getStepX()` isn't found; the bucket id list read from
  `global.potionArea.BUCKETS` so no top-level name collides across server scripts.
- **Tipped arrows:** [tipped_arrows.js](../kubejs/server_scripts/equipment/tipped_arrows.js) loops every potion; the
  bottling recipe is hand-written JSON, the Spout one goes through `R.createFilling`. Vanilla's
  `crafting_special_tippedarrow` stays.

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1.** FIXED 2026-10-19: discount kept on purpose, comment rewritten ([plan_1.0.9.md](plan_1.0.9.md)). **Machine tipping is cheaper than crafting, and the comment says otherwise. Your call.** The file header says
  "SAME potion cost as the hand craft": bottler 8 arrows per 250 mB, Spout 31 mB per arrow. The code is 1 arrow per
  20 mB (bottler) and 25 mB (Spout): 36% and 20% cheaper than the grid. Either set 31 mB on both (31.25 floors to 31),
  or keep the discount and fix the comment.
- **F2.** FIXED 2026-10-19: the tooltip shows the applied duration via `global.potionArea.scaled` ([plan_1.0.9.md](plan_1.0.9.md)). **Tooltip durations ignore `effectMult`.** A lingering bucket gives 1.2 × what its tooltip says, a splash bucket
  1.1 ×. Scale the tooltip or add a "× 1.2" line.
- **F3. The bottle remainder is global. Your call.** Added for the bucket fill recipe, but it also changes vanilla
  tipped arrows (you now get the glass bottle back) and any modded grid recipe that eats a potion. Farmer's Delight's
  stations already return bottles ([remainder_buffer.md](remainder_buffer.md)), so most of the pack already behaves
  this way. Keep, or scope it.
- **F4. Raw `e.custom` for the bucket and arrow machine recipes** (Create filling/emptying, IE bottling), against the
  NCH-helper rule. Same fix as [potions.md](potions.md) F6.
- **F5.** FIXED 2026-10-23 (TODO `potions/potion-buckets #5`, with the Night Vision fallback bug). **Recipes for potions no machine holds.** 15 bucket recipes and 2 arrow recipes per registered potion, water,
  awkward, thick and mundane included. Water never exists as `create:potion` (Create uses plain water), so those
  never run; the arrow ones give vanilla's Arrow of Splashing / uncraftable arrows. Harmless JEI noise.
- **F6. Dispensed clouds have no owner.** Kills from a dispensed harming or poison bucket credit nobody, and
  owner-checking mods treat it as ambient.
- **F7. The dome and the textures were never checked in game** ([todo.txt](todo/todo.txt): shape and feel of the ring
  stack, and the liquid layer's brightness, `lum * 1.6` in the texture tool).

### Known limits

- **Not throwable:** a bucket only bursts against a block, like placing water.
- **Off hand does nothing** (main hand only).
- **Cooldown 1 s** on the item, so a hotbar of buckets can't be chained faster. Probably creative only: in survival
  the handler shrinks the (unstackable) bucket to 0 before `addCooldown(stack.getItem(), 20)`, and an empty stack's
  `getItem()` is air (read from code 2026-10-21, not seen in game; PB-09, [gear_potion-buckets brief](briefs/potions_potion-buckets.md)).
- **Effects stack per ring:** a mob inside several rings is re-dosed by each one; for instant effects (Harming,
  Healing) that multiplies the hit.

### Rejected options

- **One spherical cloud via mixin:** built and removed (memory notes `patchermod-coremod`, `potion-buckets`); the ring
  stack needs no Java.
- **A fluid-handler capability on the buckets:** would have let machines fill them generically, but KubeJS items
  can't carry one; hence one recipe per potion.
- **`containerItem` on vanilla potions:** not available on a modified item in this KubeJS build; `setCraftingRemainder`
  is.

## 5. File map

- [startup_scripts/items/potion_buckets.js](../kubejs/startup_scripts/items/potion_buckets.js): items, tint, bottle remainder.
- [startup_scripts/func/potion_area.js](../kubejs/startup_scripts/func/potion_area.js): `global.potionArea`, dispenser handler install.
- [server_scripts/engineering/potion_buckets.js](../kubejs/server_scripts/engineering/potion_buckets.js): right click.
- [server_scripts/engineering/potion_bottling.js](../kubejs/server_scripts/engineering/potion_bottling.js): `bucketRecipes()`.
- [client_scripts/potion_buckets.js](../kubejs/client_scripts/potion_buckets.js): tooltip ([tooltips.md](tooltips.md)).
- [server_scripts/equipment/tipped_arrows.js](../kubejs/server_scripts/equipment/tipped_arrows.js): machine tipping.
- [potion/PotionBucketDispenser.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/potion/PotionBucketDispenser.java): dispenser (`CreativeEngineering.java` line 290).
- Textures: `kubejs/assets/creativeengineering/textures/item/{potion,splash_potion,lingering_potion}_bucket[_liquid].png`; [tools/potion_bucket_texture.py](../tools/potion_bucket_texture.py).
- `item_dict.js`: `i.ce_potion_bucket`, `i.ce_splash_potion_bucket`, `i.ce_lingering_potion_bucket`.

## 6. Offline checks

None. The startup log should have `[potion_area] dispenser handler installed` and `[CE potions] potion bucket
dispenser behaviors registered`, and `potion bottling done (N recipes)` / `tipped arrow tipping done (N potions x 2
recipes)` with N > 0.

## 7. In-game test suite

- [ ] **PB-01 Looks:** `/give` each bucket with `{Potion:"minecraft:swiftness"}` → light-blue liquid, bucket body
  untinted; a blank bucket's liquid is white.
- [ ] **PB-02 Tooltip:** hover a strong regeneration lingering bucket → "Regeneration II (0:22)" in blue (vanilla
  bottle wording); a blank bucket → grey "No Effects".
- [ ] **PB-03 Burst, regular:** right-click the ground with a Swiftness potion bucket → one small cloud grows for under
  2 s; standing in it gives Swiftness. The bucket becomes an empty bucket.
- [ ] **PB-04 Dome:** lingering bucket on flat ground → four stacked rings, widest at the bottom, the top ring about 1.5
  blocks up, fading after 5 s. Record how it looks (F7).
- [ ] **PB-05 Full-length effects:** stand in a Swiftness (3:00) lingering bucket's cloud → about 3:36 of Swiftness
  (3:00 × 1.2), not 0:45.
- [ ] **PB-06 Air click does nothing:** right-click the sky → nothing, bucket kept.
- [ ] **PB-07 Off hand:** bucket in the off hand, right-click a block → nothing.
- [ ] **PB-08 Creative keeps it:** in creative the bucket isn't used up.
- [ ] **PB-09 Cooldown:** two clicks within a second → the second does nothing.
- [ ] **PB-10 Dispenser:** dispenser with a poison bucket, powered → cloud in front of it; the dispenser now holds an
  empty bucket.
- [ ] **PB-11 Fill by hand:** crafting grid, empty bucket + 4 Potions of Swiftness → Swiftness Potion Bucket, 4 glass
  bottles left in the grid. 3 Swiftness + 1 Healing → no result.
- [ ] **PB-12 Tier must match:** empty bucket + 4 splash Swiftness → splash bucket, not a regular one.
- [ ] **PB-13 Drain by hand:** Swiftness bucket + 4 glass bottles → 4 Potions of Swiftness, empty bucket left in the
  grid.
- [ ] **PB-14 Spout:** 1000 mB lingering Swiftness `create:potion`, Spout over an empty bucket on a depot → lingering
  Swiftness bucket.
- [ ] **PB-15 Item Drain:** that bucket into an Item Drain → 1000 mB lingering Swiftness + empty bucket.
- [ ] **PB-16 IE Bottling:** Bottling Machine with 1000 mB `create:potion` and an empty bucket → potion bucket.
- [ ] **PB-17 Bottle remainder:** vanilla tipped arrow craft (8 arrows + lingering potion) → 8 tipped arrows and a glass
  bottle stays in the grid.
- [ ] **PB-18 Bottler tipping:** Bottling Machine with lingering Poison `create:potion` and arrows → Arrows of Poison, 20
  mB each.
- [ ] **PB-19 Spout tipping:** Spout with lingering Poison over arrows → one tipped arrow per 25 mB.
- [ ] **PB-20 JEI:** look up Arrow of Poison → crafting, Bottling Machine and Spout recipes; look up the potion bucket →
  crafting, Spout, Bottling Machine; U on it → drain and Item Drain.
- [ ] **PB-21 No noise:** JEI: Arrow of Awkward and Water Potion Bucket have no recipe; Swiftness bucket recipes are
  still there; the `potion bottling done (N recipes)` log count drops.
- [ ] **PB-22 No-effect bucket:** `/give @s creativeengineering:potion_bucket{Potion:"minecraft:awkward"}`, right-click
  a block: nothing happens, bucket kept. A bucket with no NBT still bursts Night Vision.
- [ ] **PB-23 Survival cooldown:** survival, two Swiftness buckets in the hotbar, burst one and switch to the other
  within a second: the second is on cooldown (white sweep on the slot).

## 8. History

- By 2026-09-12: potion buckets, dome of rings (replacing a spherical-cloud mixin), dispenser support, bucket ↔ bottle
  recipes; bottle remainder fixed to `setCraftingRemainder` 2026-09-12.
- By 2026-09-27 (initial git commit): all of the above plus machine-tipped arrows; shipped with 1.0.3.
- 2026-10-18: this writeup; findings F1 to F7; tests PB-01 to PB-20. No code changed.
- 2026-10-19: F1 (comment now states the machine discount as intended: grid 31.25, Spout 25, Bottler 20 mB an arrow) and F2 (tooltip durations x `effectMult`) fixed for 1.0.9 ([plan_1.0.9.md](plan_1.0.9.md)). Not run in game.
- 2026-10-23: survival cooldown fixed (`potions/potion-buckets #4`); no-effect potion buckets no longer burst Night
  Vision and are kept, and no bucket / tipped arrow recipes are made for no-effect potions (`#5`, F5). Tests PB-21 to
  PB-23. Dispensed no-effect buckets are still emptied (the dispenser path ignores the empty result). Not run in game.
