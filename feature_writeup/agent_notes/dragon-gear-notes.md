# dragon-gear-notes

> assembly keeps enchants/name, dragon_gear-tag autorepair, elytra rocket takeoff, dragon scale elytra duplication

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Merged 2026-10-19 from 4 memories (index compaction).

## gear-assembly-nbt-preservation

Create's `create:sequenced_assembly` recipe (used by `NCH.echoAssembly` / `felsicAssembly` /
`dragonAssembly` in [end_gear.js](kubejs/server_scripts/equipment/end_gear.js) for the
netherite→echo→felsic→dragon gear progression) has **zero NBT passthrough by design**: decompiled
`SequencedAssemblyRecipe.advance(ItemStack)` builds every intermediate "incomplete" stack from the
recipe's own transitional-item template, and the final step's `rollResult()` builds the finished
item from the result-pool template — neither ever reads the real stack being processed. So
enchanting/naming a netherite tool or armor piece before running it through the assembly chain
silently lost that NBT at the very first step.

Fixed via `patcherMod/src/main/java/.../mixin/SequencedAssemblyRecipeMixin.java` (registered in
`creativeengineering.mixins.json`, common list): `@Inject` at `RETURN` of
`SequencedAssemblyRecipe.advance` (Create's own method, real name, `remap = false` — see
[patchermod-coremod](patchermod-coremod.md)). `advance(ItemStack input)`'s `input` argument IS the real evolving stack
(confirmed via `ProcessingRecipe.rollResults()`, which substitutes `forcedResult.get()` — i.e. this
method's return value — directly as the actual output item stack). On step 0, if `input` carries
any of `Enchantments`/`StoredEnchantments`/`display`/`RepairCost`/`Unbreakable`/`AttributeModifiers`,
those are copied into a `CEStash` compound tag riding along on every intermediate item, then
spliced onto the real final item's tag once `advance` stops returning a `SequencedAssembly`-tagged
stack (i.e. the recipe is done).

Scoped by content, not recipe id: only fires when the input actually carries one of those keys,
which in this pack only enchanted/named tools and armor do — every other
`createSequencedAssembly` recipe (ingots, redstone components, food, ...) is untouched, so no
per-recipe allowlist was needed. Deliberately does NOT carry over `Damage` (durability) — a fresh
full-durability item on tier-up, matching what was asked ("enchantments and name").

Built + copied to `mods/creativeengineering-1.0.0.jar` 2026-09-16; not yet confirmed in an actual
play session (see [patchermod-coremod](patchermod-coremod.md) for the build/copy/md5sum ritual and the "game must not
be running when you copy" gotcha).

## dragon-gear-autorepair

`kubejs/server_scripts/equipment/dragon_autorepair.js` passively restores durability over
time to any item in the `#creativeengineering:dragon_gear` tag
(`kubejs/server_scripts/tags/equipmenttags.js`) while worn/held, via `PlayerEvents.tick`
(armor slots + both hands) and a second `LevelEvents.tick` walk for
`dragonloot:dragon_horse_armor` specifically (worn in `AbstractHorse`'s own inventory slot
401, not a player `EquipmentSlot` — same pattern as [armor-tool-effects](armor-tool-effects.md)).
Rate/interval/on-off are patcherMod `CEConfig.DRAGON_GEAR.autorepair*` knobs. Defaults (baked 2026-09-26): 50 durability every 200 ticks (10 s).

**Gap found and fixed 2026-09-24:** the tag only listed the 4 armor pieces + 5 hand tools
originally. Every other output of `end_gear.js`'s `dragonAssembly` bulk call (see
[dragon-gear-notes](dragon-gear-notes.md)) — `dragon_crossbow`, `dragon_bow`, `dragon_trident`,
`dragon_anvil`, `dragon_horse_armor`, plus the custom `ce_dragon_shield` and `air_brake` —
was silently excluded from autorepair even though it went through the same tier-up
recipe. Tag membership is **not** derived from the recipe list; it's a separate hand-
maintained array, so a new `dragonAssembly` entry needs a matching tag entry or it just
never regenerates.

**How to apply:** whenever a new item is added to `end_gear.js`'s `dragonAssembly` bulk
call, add it to the `dragon_gear` tag in the same PR/session. If the new item is worn by a
non-player entity (future mob armor etc.), it also needs its own `LevelEvents.tick` walk
like the horse-armor one — the player-tick pass can never see it.

## dragon-elytra-rocket-takeoff

**Problem:** DragonLoot's `upgraded_dragon_chestplate` glides (jump-in-air only) via its own
`LivingEntity#updateFallFlying` + `Player#tryToStartFallFlying` mixins — it is a plain
`ArmorItem`, NOT an `ElytraItem`, and adds no "flight modifier" attribute. **Easy Elytra
Takeoff** gates its firework-rocket takeoff on
`ForgeElytraEventHelper.isWearingAnElytra(player)`, which only accepts `instanceof ElytraItem`
in an armour slot (+ a Curios "flight modifier" attribute ≥ 1). So the chestplate never gets
rocket-takeoff, only jump-in-air.

**Fix:** `patcherMod/.../mixin/EasyElytraTakeoffHelperMixin.java` — `@Mixin(ForgeElytraEventHelper.class,
remap=false)`, `@Inject` at HEAD of `isWearingAnElytra` (cancellable): if any armour-slot stack
is in `#creativeengineering:elytra_chestplates` OR `stack.canElytraFly(player)` (Forge), return
true. Once EET's flow starts it force-sets the fall-flying flag for ~6 ticks and DragonLoot's
own `updateFallFlying` mixin sustains the glide — so nothing else is needed.

Wiring: added to `creativeengineering.mixins.json` `"mixins"`; optional `easyelytratakeoff`
dep (`mandatory=false`, AFTER) in mods.toml; `easyelytratakeoff-1.19.2-4.2.jar` added to
`patcherMod/libs/` (Mixin AP validates `@Mixin(value=X.class)` targets at compile). Tag
populated in `kubejs/server_scripts/tags/equipmenttags.js`:
`e.add('creativeengineering:elytra_chestplates', 'dragonloot:upgraded_dragon_chestplate')`
(reload-able — add more modded elytra-chestplates there). Build/deploy per [patchermod-coremod](patchermod-coremod.md).

If EET is ever removed, pull `EasyElytraTakeoffHelperMixin` from the config + `libs/` too.

**Follow-up 2026-09-10 — "rocket only works sometimes" (glass block / falling):** EET's
`ElytraEvent.onFirework` (in `easyelytratakeoff_common_forge`) has a second gate after the
`isFallFlying` check: an `inAir` scan of the 3x3 layer at `player.blockPosition().below().below()`
— if every block there is vanilla `Blocks.AIR` it returns PASS (no takeoff). So the dragon
chestplate rocket-took-off only on thick terrain / at a jump apex, not on a lone glass block
over air or mid-fall. Fix: `mixin/EasyElytraTakeoffGroundCheckMixin` —
`@ModifyVariable(method="onFirework", at=@At("LOAD"), index=8, remap=false)` (local 8 = `inAir`
in easyelytratakeoff-1.19.2-4.2, single load), forces `inAir=false` when
`ElytraChestplates.isWorn(player)` (tag-only; vanilla elytra keep stock behaviour). Added to
`creativeengineering.mixins.json`. Tag key now centralised in
`com/SaintAlfonzo/creativeengineering/util/ElytraChestplates.java` (`TAG` + `isWorn`); both EET
mixins read it. `collective-1.19.2-7.64.jar` added to `patcherMod/libs/` (compile-only — Mixin
AP resolves `ElytraEvent`'s `collective` refs).

**Enderiophage rocket (alexsmobs, `ItemEnderiophageRocket`) — investigated, NOT yet done:**
reusable modded rocket. Its `use()` only spawns `EntityEnderiophageRocket` when
`player.isFallFlying()` is already true — else returns PASS, i.e. it has NO takeoff of its own.
EET won't help it either: `onFirework`'s first gate is
`!item.equals(Items.FIREWORK_ROCKET) && (!name.contains("booster_") || name.contains("_empty"))
→ PASS`, and the enderiophage rocket is neither a vanilla firework nor a "booster_" item, so
EET bails immediately. To give it takeoff: either (a) a patcherMod `@Inject` at HEAD of
`ItemEnderiophageRocket.use` that, when `!isFallFlying` and wearing `#…:elytra_chestplates`
(and not `inAir`-blocked, or ignoring that), sets the fall-flying flag + spawns the entity +
returns success; or (b) widen EET's `onFirework` name gate to also accept
`alexsmobs:enderiophage_rocket` AND relax its booster re-fire path — messier, and EET's tick
spawns a vanilla `FireworkRocketEntity` from the main-hand stack (would be a dud for a
non-firework). Option (a) is cleaner. See [patchermod-coremod](patchermod-coremod.md).

## dragon-scale-elytra-duplication

Combine any Elytra + `dragonloot:dragon_scale` (the pack's own dragon scale, not Quark's -
Quark's `quark:dragon_scale` is unobtainable, see memory `item-dict-mechanics`/removed_items.js)
in a crafting grid: the Elytra you put in comes back untouched (enchantments + durability
intact, never consumed), the scale is consumed, and a brand-new plain `minecraft:elytra`
is produced as the crafting output.

**Why:** Quark's own `quark:elytra_duplication` CustomRecipe hardcodes both the catalyst
item (`DragonScalesModule.dragon_scale`, a Java static field = Quark's own item) and the
output (`new ItemStack(Items.ELYTRA)`) in Java, so it can't be pointed at a different mod's
dragon scale via config/datapack. Reverse-engineered its bytecode (matches/assemble/
getRemainingItems) via `javap -p -c` against `DecompiledMods/Quark-3.4-418` (no decompiled
.java sources ship, only .class) since there's no source jar.

**How to apply:** Re-implemented as a generic `creativeengineering:elytra_duplication`
vanilla `CustomRecipe` in patcherMod, reading `catalyst` (Ingredient) and `result`
(ItemStack) from recipe JSON instead of hardcoding either:
- `patcherMod/src/main/java/.../gear/ElytraDuplicationRecipe.java` — matches() requires
  exactly one `ElytraItem` instance + one catalyst match (anything else in the grid fails
  it); `getRemainingItems()` returns `stack.copy()` for the Elytra slot (this is the actual
  duplication trick) and leaves everything else EMPTY; `assemble()`/`getResultItem()`
  return the JSON-defined result.
- `gear/ElytraDuplicationSerializers.java` — registers just the serializer; recipe TYPE
  stays vanilla's shared `minecraft:crafting` (matches how vanilla's own ArmorDyeRecipe/
  BookCloningRecipe work), no new RecipeType needed.
- `CreativeEngineering.java` constructor calls `ElytraDuplicationSerializers.register(modEventBus)`
  alongside the other patcherMod recipe-type registrations ([patchermod-coremod](patchermod-coremod.md)).
- KubeJS: `global.NCH.elytraDuplication(catalyst, result, e, idOverride)` in neoCraftHelper.js
  (same file/pattern as `dragonBreathing`) emits the datapack JSON via `e.custom(...)`.
- Wired up: `kubejs/server_scripts/equipment/end_gear.js` "Dragon" section —
  `R.elytraDuplication(i.dragon_scale, i.elytra, e, 'dragon_scale_elytra')`.

Compiles clean with `cd patcherMod && ./gradlew build --offline`; jar copied over
`mods/creativeengineering-1.0.0.jar`. (no JEI category added for
it, unlike Quark's own `ElytraDuplicationExtension` — cosmetic-only gap, recipe works
without one).
