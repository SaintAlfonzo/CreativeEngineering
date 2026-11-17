# potion-buckets

> AoE lingering potion bucket items (global.potionArea); Java dispenser forwards to potion_area.js

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Merged 2026-10-19 from 2 memories (index compaction).

## potion-buckets

## 2026-09-12 update: fill/bottle/empty done, dynamic tint, IE bucket removed, JEI fluid list

Since the entry below was written: `engineering/potion_bottling.js` now generates the real
fill/bottle/empty recipes (create:filling, IE bottling_machine, create:emptying — one per potion
x REGULAR/SPLASH/LINGERING, 1000 mB/bucket) — the "TODO: survival recipe" note further down is
stale, ignore it. Three more pieces landed the same session, all client-side/KubeJS-only (no
patcherMod rebuild needed) — see memory `kubejs-api-notes` for the underlying API
details:

- **Per-potion dynamic tint** — `startup_scripts/items/potion_buckets.js` now calls
  `.color((stack, layer) => ...)` on each bucket's `ItemBuilder`, returning
  `SimpleColor.WHITE` for layer 0 and a blended potion color (same math as `blendColor()` below,
  inlined via a local `potionTint()`) for layer 1. This needed splitting the old single-layer
  "re-tinted vanilla water_bucket" texture into two layers: `tools/potion_bucket_texture.py` now
  writes `<id>.png` (steel body only, liquid pixels transparent) + `<id>_liquid.png` (liquid
  pixels only, recolored to neutral grey so the runtime tint multiplies cleanly), and each bucket
  id got an explicit two-layer model at
  `assets/creativeengineering/models/item/<id>.json` (`layer0`/`layer1`). All three tiers
  currently share the identical body/liquid split (same vanilla source shape) — kept as separate
  per-id files in case a tier's shape diverges later. Not yet confirmed in-game (no way to launch
  the modpack from this environment) — if the liquid layer's brightness looks off, it's the
  `lum * 1.6` scale factor in `split()`, just retune and rerun the script.
- **IE `potion_bucket` removed** — `immersiveengineering:potion_bucket` (`i.potion_bucket` in
  item_dict, real IE-registered item) added to `global.removedItems`
  (`startup_scripts/_lib/removed_items.js`), same hide-from-JEI + tooltip-flag treatment as every
  other cross-mod dead item in that list (this pack's established "completely remove" idiom —
  there's no existing precedent anywhere in the pack for stripping something from the creative
  tab too, so didn't invent one). Its supply chain was already fully cut before this session:
  IE's own `potion_bottling_list` was removed (see below), the IE Mixer's potion recipes were
  already redirected to `create:potion` by patcherMod's `PotionRecipeGeneratorsMixin` (mixer
  never produces `immersiveengineering:potion` fluid anymore), and `forge:fluid/potion` had
  `f.ie_potion` stripped from it in `tags/fluidtags.js` — so the item was already unreachable in
  survival, this just hides/flags it like the rest of the dead list.
- **JEI fluid list** — `client_scripts/fluid_jei.js` (was `potion_fluid_jei.js`, broadened same
  session): `JEIEvents.addFluids(...)` adds one `create:potion` FluidStack
  (`{Bottle:"REGULAR",Potion:"<id>"}`, 1000 mB) per non-empty potion in `Registry.POTION`
  iteration order, so every potion's fluid form is directly browsable/searchable in JEI instead of
  only appearing inside specific recipe slots. Deliberately REGULAR-only (splash/lingering are
  visually identical, NBT-only difference) to avoid 3x near-duplicate entries — revisit if the
  user wants splash/lingering shown separately too. JEI's own ingredient-list sort setting
  (alphabetical/mod-name/etc., player-configurable) still has the final say over on-screen order;
  registry order is just the best available proxy for "potion order" from the mod side.
  **Turned out this JEI-visibility problem is much bigger than potions**: Create's own JEI plugin
  (`CreateJEI.class`, decompiled) never calls `registerIngredients` at all, and JEI itself doesn't
  auto-populate the fluid ingredient list from the fluid registry — a fluid with no bucket item
  (dragon's breath, potion, several others) is 100% invisible in JEI otherwise. Same file now also
  loops `ForgeRegistries.FLUIDS.getKeys()` (same idiom as the working `/dumpfluids` command,
  `server_scripts/_lib/dump_fluid.js`) and adds a plain 1000 mB stack of every still fluid
  (skipping `flowing_*` duplicates by id-prefix, `minecraft:empty`, `create:potion` itself, and
  the 4 fluids `tags/fluidtags.js` already calls out as "dead placeholder fluids" — tmted's
  melon_juice/tomato_sauce/apple_cider + decorative_blocks:thatch). See
  memory `kubejs-api-notes` for the underlying API.

## 2026-09-13 update: vanilla-style coloured tooltip

`client_scripts/potion_buckets.js` (item tooltip, item #4 below) rewritten to match vanilla's
`PotionUtils.addPotionTooltip` — colored "Name II (0:30)" lines (blue beneficial/neutral, red
harmful, grey "No Effects"), same wording/format as `minecraft:potion`/`splash_potion`/
`lingering_potion` bottles. Superseded the old plain-uncolored version (which existed only
because `Component.withStyle(ChatFormatting)` is the ambiguous-overload bug below) — turns out
KubeJS's own `MutableComponent.blue()`/`.red()`/`.gray()` extension methods sidestep `withStyle`
entirely; see memory `kubejs-api-notes` for the mechanism. Deliberately still skips
vanilla's attribute-modifier appendix lines (Strength/Weakness/Speed/Slowness/Luck's "+N Attack
Damage" etc.) — untested extra Java surface for a rarely-scrutinised detail.

Three items that burst an **expanding, domed stack of AreaEffectClouds** ("rings", positive
RadiusPerTick, RadiusOnUse 0) on right-click: `creativeengineering:potion_bucket` /
`splash_potion_bucket` / `lingering_potion_bucket`. splash & lingering scale area + effect
durations (effectMult) and use more rings (taller/rounder dome).

Pure KubeJS, **no patcherMod involved** (a Mixin-based true-sphere approach was tried and
ripped back out — see "Abandoned" below):
- `startup_scripts/items/potion_buckets.js` — `e.create` items, `.unstackable()`, group
  'brewing', displayName.
- `startup_scripts/func/potion_area.js` — `global.potionArea.spawn(level,x,y,z,itemId,stack,owner)`.
  `BUCKETS` (keyed by item id) holds every dial: `radius` (bottom ring), `growthPerTick`
  (expansion speed), `lingerTicks` (linger time), `waitTicks`, `effectMult`, **`rings`** (ring
  limit — how many stacked clouds form the dome). `spawn()` loops `i` from 0..rings-1, each
  ring sized/positioned like a hemisphere cross-section (`f = i/rings`,
  `radius_i = radius * sqrt(1-f^2)`, `y_i = y + radius * f`, growth scaled by the same
  fraction) — ring 0 is the full-size base disc, higher rings are smaller and higher up, so
  the stack's silhouette reads as a dome instead of a flat circle. Builds
  `net.minecraft.world.entity.AreaEffectCloud` per ring, reads potion via
  `PotionUtils.getMobEffects(stack)` (color hand-blended, see gotcha below), all rings share
  the same effects/color, `level.addFreshEntity` per ring, one shared placement sound.
- `server_scripts/engineering/potion_buckets.js` — `ItemEvents.rightClicked(id, e=>…)`; only
  fires against a block hit (`t.type === 'BLOCK'`, no air-placing); spawns at `t.hit.{x,y,z}()`
  + a flat `+0.1` Y nudge; consumes stack + returns `minecraft:bucket`; `e.success()`.
- `client_scripts/potion_buckets.js` — `ItemEvents.tooltip` → effect lines (no color styling,
  see gotcha below).
- item_dict keys `i.ce_potion_bucket` etc. (`i.potion_bucket` is IE's).
- Textures: re-tinted vanilla water_bucket, `tools/potion_bucket_texture.py`.

Buckets hold a potion like `minecraft:potion` (NBT `Potion`/`CustomPotionEffects`); blank bucket
uses `DEFAULT_EFFECTS`. `/give @s creativeengineering:lingering_potion_bucket{Potion:"minecraft:strong_regeneration"}`.

## Rhino bugs hit building this (all apply broadly, see memory `rhino-fieldandmethods-gotcha`)
1. `Vec3.x/y/z` (field + same-named `x()`/`y()`/`z()` method) fed into `new AreaEffectCloud(...)`
   silently NPE'd the whole event handler — fixed by calling `.x()`/`.y()`/`.z()` explicitly.
2. `PotionUtils.getColor(stack)` (3 same-arity overloads: Potion/ItemStack/Collection) throws
   "ambiguous method" — replaced with a hand-rolled `blendColor()` averaging
   `MobEffect.getColor()` per effect. Same bug killed `Component.withStyle(ChatFormatting)` in
   the tooltip script — originally just dropped styling there; later (2026-09-13) fixed properly
   by using KubeJS's unprefixed `MutableComponent.blue()`/`.red()`/etc. instead of `withStyle`
   at all, see the update at the top of this memory + memory `kubejs-api-notes`.
3. `Direction.getStepX()`/`Y()`/`Z()` (real, unambiguous methods) are invisible to Rhino
   reflection on a `Direction` enum instance entirely — dropped the face-normal offset for
   bucket placement, used a flat `+0.1` Y nudge instead.

## Abandoned: patcherMod Mixin for a true sphere (2026-09-10, several rounds)

Tried making a single `AreaEffectCloud` a real sphere (hit-test + particles) via
`AreaEffectCloudSphereMixin`, opt-in through a `CESphere` persistent-data tag. Went through
multiple debugging rounds — wrong `@At` owner class (vanilla's `this.getY()` call inside
`AreaEffectCloud` compiles with owner=`AreaEffectCloud`, not `Entity`, so the redirect
silently matched 0 call sites and did nothing); then a hard boot-crashing
`InvalidInjectionException` (`@Redirect` always needs the call's receiver as an **explicit
first handler parameter**, even when the receiver is `this`). Both are now-documented lessons
in [patchermod-coremod](patchermod-coremod.md) / memory `rhino-fieldandmethods-gotcha` for any *future* vanilla-class
mixin here, but even after both fixes the user still couldn't confirm the sphere actually
working in-game — **ripped out entirely** (`AreaEffectCloudSphereMixin.java` deleted, removed
from `creativeengineering.mixins.json`, jar rebuilt+reinstalled clean) in favor of the
stacked-ring approach above, which needs no Java at all and is trivially verifiable (just look
at the particles / test hit range directly, no decompiling required).

TODO / follow-ups:
- ~~**Dispenser support**~~ — done, 2026-09-12: patcherMod `PotionBucketDispenser`
  (`DispenserBlock.registerBehavior` in commonSetup) forwards to a `setHandler` callback that
  `potion_area.js` installs, which just calls `global.potionArea.spawn(...)` — no dome-logic
  duplication in Java. See [potion-buckets](potion-buckets.md).
- ~~**Survival recipe**~~ — done, 2026-09-12: `server_scripts/engineering/potion_bottling.js`'s
  `bucketRecipes()` now adds a `potion_bucket_fill` shapeless craft (1 `minecraft:bucket` + 4
  bottles of that tier matching the potion's NBT via `{type:'forge:partial_nbt', item, nbt}`,
  same idiom as `engineering/blueprints.js` -> 1 filled bucket) and its `potion_bucket_drain`
  reverse (1 filled bucket -> 4 matching bottles), one pair per potion x {REGULAR,SPLASH,
  LINGERING} tier, via `global.NCH.shapeless`. Remainders: the CE bucket items got
  `.containerItem('minecraft:bucket')` at registration (`startup_scripts/items/
  potion_buckets.js`, same proven idiom as [sample-syringe](sample-syringe.md)'s `.containerItem(...)` — safe,
  registration-time-only). The vanilla `minecraft:potion`/`splash_potion`/`lingering_potion`
  items got a crafting remainder via `ItemEvents.modification` in the same file. **Correction
  (2026-09-12):** the first attempt called `item.containerItem('minecraft:glass_bottle')` there,
  which is WRONG — decompiling the jar (see memory `tune-tools-func`) showed the modification event's
  runtime `Item` has no `containerItem` method at all; the real accessor is
  `kjs$setCraftingRemainder(Item)` → unprefixed `.setCraftingRemainder(Item)`, and it takes a
  real `Item` object, not a string. Fixed to
  `item.setCraftingRemainder(Item.of('minecraft:glass_bottle').getItem())`. Still wrapped in
  try/catch as a matter of habit, but this version should actually work — not yet confirmed
  in-game.
- No public setter for `AreaEffectCloud.reapplicationDelay` (dose-repeat interval) - not exposed
  as a dial; would need a patcherMod `@Accessor` if wanted later.
- Ring stack in-game check: PB-04 in potion_buckets.md (dome shape/feel)
  and retune `rings` per bucket once seen.

## potion-bucket-dispenser

Dispenser support for the creativeengineering potion buckets ([potion-buckets](potion-buckets.md)) is implemented via a Java→KubeJS callback bridge, not a Java reimplementation of the dome logic.

- `patcherMod/.../potion/PotionBucketDispenser.java` — registers a `DefaultDispenseItemBehavior` for each of the 3 bucket ids (via `DispenserBlock.registerBehavior`, called from `CreativeEngineering#commonSetup` through `event.enqueueWork`, since it needs the item registry fully populated). On dispense it computes the position one block in front of the dispenser's facing, then calls a static `SpawnHandler` interface (`spawn(ServerLevel, x, y, z, itemId, ItemStack)`) if one has been installed — falls back to vanilla eject-as-item behavior with a warning log if not. Returns `new ItemStack(Items.BUCKET)` to replace the dispensed bucket in the slot (buckets are `.unstackable()`, so the slot always holds exactly 1).
- `kubejs/startup_scripts/func/potion_area.js` calls `PotionBucketDispenser.setHandler(...)` once at startup with a JS lambda that just calls `global.potionArea.spawn(level, x, y, z, itemId, stack, null)` — the SAME function the right-click path (`server_scripts/engineering/potion_buckets.js`) uses. This keeps ring count/radius/color/effect tuning in exactly one place (potion_area.js), matching the pack's "centralize" preference — Java never duplicates the dome logic.

**Why this shape:** KubeJS has no API surface for `DispenserBlock.registerBehavior` (it's not something StartupEvents/ItemEvents expose), so the registration itself HAD to be Java. But the actual cloud-spawning logic was already correct and tuned in KubeJS (potion_area.js) — duplicating it in Java would let the two copies drift. Solved by making Java a thin shim: register the behavior, then call back into whatever handler KubeJS installs, via a method call (`setHandler(handler)`) rather than a static field assignment — Rhino's JS-function-to-Java-functional-interface coercion is reliably supported for method-call arguments (this is how every existing `ItemEvents.foo(id, callback)` in the pack works), but was not worth risking on a raw static-field set.

**How to apply:** if another feature needs a game hook that KubeJS can't register directly (another dispenser/dropper behavior, a block-entity capability, etc.), follow the same shape — patcherMod does ONLY the registration + the Java-only plumbing (facing, position math, stack mechanics), and forwards into a callback that KubeJS installs at startup via a `setHandler`-style method, keeping the actual game-content logic (tuning, effects, recipes) in KubeJS as the single source of truth. Verified compiling with `./gradlew compileJava --offline` in `patcherMod/`.
