# incubation-nest-internals

> How Incubation 3.0.1 nests collect eggs (EggLayer interface, LayEggInNestGoal, hopper push) and the plan for an emu nest via Creative Common

Moved from agent memory 2026-11-16 (last edited there 2026-09-30). Repo status docs win over any status line here.

Investigated 2026-09-29 (javap on incubation-1.19.2-3.0.1.jar; no decompiler on PATH, ForgeFlower is in the gradle cache per [create-config-string-widget](create-config-string-widget.md)).

- `core.api.EggLayer` (getEggTimer/setEggTimer/isBirdJockey/getEggItem/getNextEggTime/getEggLayingSound). Chicken gets it via `ChickenMixin`.
- `IncubationEvents.onEntityJoinWorld`: ANY `Animal instanceof EggLayer` gets `LayEggInNestGoal` (priority 3, speed 1.0, range 16). Goal is active only when eggTimer < 400: walks to a matching nest, lays, resets the timer (so the vanilla drop is skipped). If it doesn't make it in time, the egg drops normally.
- `EmptyNestBlock` (hay_nest/twig_nest) holds a Map egg->filled nest; `BirdNestBlock(ResourceLocation egg, EmptyNestBlock, props)` auto-registers itself in that map. eggs 0..6 blockstate.
- CRASH TRAP: goal calls `emptyNest.getNest(egg).defaultBlockState()` with no null check, so an EggLayer whose egg has no registered nest NPEs.
- `BirdNestBlockEntity.serverTick` pushes 1 egg/tick into an unlocked hopper directly below; that's the "collection".
- BE type valid blocks = Blueprint `collectBlocks(BirdNestBlock.class)`, which scans all registered blocks, so other mods' BirdNestBlocks are included.
- Only chicken nests are live in this pack (Environmental/Autumnity absent, so duck/turkey nests are dead).
- Alex's Mobs `EntityEmu.timeUntilNextEgg` (public int, 6000+rand(6000), NBT EggLayTime) mirrors the chicken 1:1, so an EntityEmu mixin implementing EggLayer is all it needs.
- Hay/twig nests map chicken+duck+turkey only, so ANY new egg type NPEs on them (and chickens NPE on a nest that maps only a new egg). Fixed by mixin/LayEggInNestGoalMixin (isValidTarget m_6465_ rejects empty nests with no variant for the bird's egg).

**Built 2026-09-29 (status: nests.md):**
- Creative Common `nest/EmuNests.java`: `creativeengineering:emu_nest` (EmptyNestBlock + item, Incubation twig-nest properties + twig model/item sprite) and `emu_egg_nest` (BirdNestBlock, no item, turkey egg geometry + recoloured `emu_eggs.png`). Flammable 60/20 via Blueprint DataUtil (FireBlock.setFlammable is private at compile; Blueprint ATs it). Loot: shears keep the nest, else 1-3 oak bark (mirrors twig nest -> sticks).
- `mixin/EntityEmuEggLayerMixin` + `LayEggInNestGoalMixin`; mods.toml incubation ordering AFTER (optional). libs/: incubation, alexsmobs, citadel, blueprint.
- KubeJS: i.emu_nest, b.emu_nest/b.emu_egg_nest, `incubation:bird_nests` tag (blocktags.js), compost 0.65, recipes in other/misc_mods.js (twig nest from [twig, #forge:rods/wooden], emu nest from #immersive_weathering:bark, both hay-nest U shape; no U-shape conflicts across 8490 jar recipes).

**Duck + goose nests (Untitled Duck Mod 0.6.1), built 2026-10-05, verified on a scratch server:**
- `nest/WaterfowlNests.java`: duck_nest/duck_egg_nest, goose_nest/goose_egg_nest (same shape as EmuNests, HAY_NEST properties, item for the empty nests only). `mixin/DuckEntityEggLayerMixin` + `GooseEntityEggLayerMixin` shadow the private `eggLayTime` (NBT duckEggLayTime/gooseEggLayTime; identical 6000+rand(6000) reset), egg item via `ModItems.getDuckEgg()/getGooseEgg()`, sound via `ModSoundEvents`. mods.toml: untitledduckmod optional AFTER. libs/ gained untitledduckmod + geckolib jars (git-ignored).
- Look is a placeholder: blockstates/item models point straight at `incubation:block/hay_nest` + `incubation:block/*_chicken_egg(s)` + `incubation:item/hay_nest`. Replace with own textures later. Loot: nest drops itself (hay-style).
- KubeJS: i.duck_nest/i.goose_nest, b.duck_nest/duck_egg_nest/goose_nest/goose_egg_nest, bird_nests tag, compost 0.65, recipes in misc_mods.js (duck: ['T T','GGG'], goose: ['G G','TTT'], G = i.grass, T = i.tall_grass; the swap keeps the two U shapes distinct).
- Scratch-server proof: duck laid in duck nest, goose in goose nest (both at 1 egg, timers reset), chicken near both ignored them and laid normally. Hay nest has duck/turkey mappings for Environmental/Autumnity eggs only, so duck-mod ducks still skip hay nests via LayEggInNestGoalMixin.

**2026-10-05:** writeup is feature_writeup/nests.md (NE-01..10), incl. per-nest texture paths and the kubejs/assets hotswap route. Duck+goose nests are now in mods/ (Creative Common 1.0.5 jar), staged for 1.0.5.

**Textures 2026-10-05:** duck/goose assets (blockstates, models, nest/item/egg PNGs) now live ONLY in kubejs/assets/creativeengineering/ (removed from Creative Common source). Duck eggs on Incubation duck-egg geometry, goose on turkey geometry (matches how the user painted them). Emu assets still in the jar.

**2026-10-05 (later):** all of this moved to its own mod, see [incubation-additions-mod](incubation-additions-mod.md). Ids are now incubation_additions:*, package com.SaintAlfonzo.incubation_additions.
