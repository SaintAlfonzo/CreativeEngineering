# Brief: worldgen / seasons

Written 2026-10-21 from [todo/TODO.md](../todo/TODO.md) `# worldgen`, Sub `seasons`. Feature writeup:
[weather.md](../weather.md) (prefix `WE-`). Code: `development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/`
(paths below are relative to it). Both rows are client-side patcherMod work, so test them in a client, not on a
scratch server.

## Scope and order

| Row | Status | What is left | Order |
|---|---|---|---|
| seasons #2 | Bug | Cool Rain's rain tick ignores the season types | **first**: one in-game look decides between a patch and nothing. Do it together with the WE- client run (seasons #1) |
| seasons #3 | Planned | river and beach grass/foliage tint per position | second. Independent of #2; same mixin config, so one jar for both |
| seasons #1 | Testing | WE-01 to WE-36, client never run | the client session that runs these is also #2's step 0 |

---

## seasons #2: Cool Rain may ignore season types

Status Bug · Work patcherMod · TODO `worldgen / seasons #2` (WE-25)

### Goal

Rain sounds and splash particles follow the season types, like `PrettyRainSeasonMixin` already makes Pretty Rain's do:
- no rain over arid ground, or in a tropical biome's Mid Dry;
- no rain sounds where it snows (temperate in winter, frozen, cold).

### Now

**What Cool Rain does.** Verified by decompiling `coolrain-1.19.2-Forge-1.0.2.jar`, class
`com.leclowndu93150.coolrain.mixin.RainMixin`. It is the mod's only mixin, client side, `coolrain.mixins.json`, no
priority set.
- `@Inject(method = "tickRain" [m_109693_], at = HEAD, cancellable = true)` on `LevelRenderer`.
- While the level's rain level is above 0, it does the whole rain tick itself and then **always** calls `ci.cancel()`.
- For up to `100 * rain^2` random columns within 10 blocks, it reads
  `levelReader.getBiome(heightmapPos).value()` (`m_204166_`, `m_203334_`) and tests
  `biome.getPrecipitation() == RAIN` (`m_47530_`).
- If the test passes, it spawns a `RAIN` splash, or `SMOKE` on lava, magma or a lit campfire, and plays per-block rain
  sounds: metal, glass, fabric, foliage, water, lava, amethyst, note block.

So the bug is wider than WE-25 says. Cool Rain checks **neither the season types nor temperature**:
- a warm river through a desert (declares RAIN) gets rain sounds;
- plains in winter get rain sounds under falling snow;
- jungles in Mid Dry get rain sounds;
- frozen biomes that declare rain (siberian_taiga, base 0.2) get them all year.

**Pretty Rain has the same hook.** Verified: `Pretty Rain-1.19.2-Forge-1.1.1.jar`,
`com.leclowndu93150.particlerain.mixin.LevelRendererMixin`. Its handler is also a HEAD inject on `tickRain`, and it
cancels when `tickVanillaWeather` is false, which is the pack setting (`config/prettyrain.json5:67`).
- The handler plays Pretty Rain's rain, snow and sandstorm ambience through `WeatherParticleSpawner.getBiomeSound`.
  `PrettyRainSeasonMixin` (`mixin/PrettyRainSeasonMixin.java:38-44`) already makes those season-correct.
- `tickVanillaWeather` is read nowhere else in Pretty Rain (checked in the jar).

**Only one of the two handlers ever runs.** Both mixins are at default priority 1000, both cancel, and which one runs
first depends on mixin config load order. **Unknown until looked at in game.**
- If Pretty Rain runs first: Cool Rain is inert. No wrong sounds, but no Cool Rain sounds either.
- If Cool Rain runs first: Cool Rain's sounds are wrong (above), and Pretty Rain's snow and sandstorm ambience never
  plays.
- Either way, Serene Seasons' `tickRain` redirects and `SeasonRainColumnMixin`'s `tickRain` part
  (`mixin/SeasonRainColumnMixin.java:30-31`) never run while it rains.

**Mod sides.** Cool Rain is a both-sides mod: a strict network channel `coolrain:main` ([plan_1.0.5.md](../plan_1.0.5.md)
3, row "Cool Rain"). Removing it means removing it from the server and every client in one deploy.

**A precedent exists for patching another mod's merged handler.** `compat/CreativeEngineeringMixinPlugin.java:67-89`
does it for Fruits Delight:
- an empty anchor mixin with a high priority, `mixin/FruitsDelightLeafCheckMixin.java` (`priority = 5000`), applies
  after Fruits Delight's mixin;
- the plugin's `postApply` then edits the merged `handler$...` method in place, with same-stack-effect instruction swaps
  so no stack-map frames change.

### Build

0. **Find the order (in game, no code).** Plains, `/weather rain`, `/season set MID_SUMMER`, stand on an iron block or
   under glass:
   - Cool Rain's metal or glass "ping" plays -> **Cool Rain runs first**.
   - Only plain rain sound -> Pretty Rain first.

   To be sure, launch once with `-Dmixin.debug.export=true` and read
   `.mixin.out/class/net/minecraft/client/renderer/LevelRenderer.class` with
   `javap -c -p`. The first `handler$...$tickRain` call in `m_109693_` is the winner.
1. **If Pretty Rain runs first:** nothing is wrong with the season types. Close the bug and go to the decision below
   (Cool Rain does nothing; drop it or leave it).
2. **If Cool Rain runs first** (or to make Cool Rain run regardless: set `tickVanillaWeather: true`, see Decisions):
   1. **Anchor mixin.** `mixin/CoolRainSeasonAnchorMixin.java`: an empty `@Mixin(value = LevelRenderer.class,
      priority = 5000)`, added to the `"client"` list of `src/main/resources/creativeengineering.mixins.json` (around
      `:164-170`, next to `PrettyRainSeasonMixin`).
   2. **Plugin.** In `CreativeEngineeringMixinPlugin.postApply`, when `mixinClassName` ends with the anchor's name, call
      `patchCoolRainHandler(targetClass)`.
      - Find the method whose name starts with `handler$` and ends with `$tickRain`, **and** that carries the
        `@MixinMerged` annotation (`Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;`, check both
        `visibleAnnotations` and `invisibleAnnotations`) with `mixin = "com.leclowndu93150.coolrain.mixin.RainMixin"`.
      - Pretty Rain's handler is also `handler$...$tickRain`, so the name alone isn't enough.
      - In that method, make two swaps with the same stack effect:
        - `INVOKEINTERFACE net/minecraft/world/level/LevelReader.m_204166_ (Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;`
          becomes `INVOKESTATIC com/SaintAlfonzo/creativeengineering/seasons/CoolRainHooks.biome
          (Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;` (itf false);
        - `INVOKEVIRTUAL net/minecraft/world/level/biome/Biome.m_47530_ ()Lnet/minecraft/world/level/biome/Biome$Precipitation;`
          becomes `INVOKESTATIC .../CoolRainHooks.precipitation (Lnet/minecraft/world/level/biome/Biome;)Lnet/minecraft/world/level/biome/Biome$Precipitation;`.
      - If either call is missing, change nothing and log one WARN (a Cool Rain update), as the Fruits Delight patch does.
   3. **Hooks** (`seasons/CoolRainHooks.java`, client-only use; the render thread only, so plain static fields are fine):

      ```java
      public static Holder<Biome> biome(LevelReader r, BlockPos p) { Holder<Biome> h = r.getBiome(p); lastPos = p; lastBiome = h; return h; }
      public static Biome.Precipitation precipitation(Biome b) {
          ClientLevel level = Minecraft.getInstance().level;
          if (level == null || lastBiome == null || lastBiome.value() != b) return b.getPrecipitation();
          return SeasonTypes.precipitationAt(level, lastBiome, lastPos);   // RAIN / SNOW / NONE, season- and temperature-aware
      }
      ```

      `SeasonTypes.precipitationAt` (`seasons/SeasonTypes.java:455-464`) does the following:
      - resolves an adaptive river or beach to its land;
      - applies the season rule (`SeasonHooks.shouldRainInBiomeInSeason`);
      - turns RAIN into SNOW when it is too cold (`warmEnoughToRainHook`).

      Cool Rain's `== RAIN` test then fails for arid, dry season and snow columns. Sounds and splashes stop exactly where
      Pretty Rain draws no rain.
3. Update weather.md:
   - 3.3 code map row;
   - 7 Issues: Cool Rain, with the order found in step 0;
   - WE-25's text and the new tests.

### Ids and data

- Mod ids `coolrain` (Cool Rain Reforged 1.0.2) and `particlerain` (Pretty Rain 1.1.1); verified against the jars.
- Mixin targets: vanilla `LevelRenderer#m_109693_` (`tickRain(Camera)`), and Cool Rain's merged handler. Owner and
  descriptor strings above come from Cool Rain's decompiled source and refmap, which are SRG names in the jar, as at
  runtime.
- `@MixinMerged` being present on the merged handler, and its retention, are **unverified**: confirm with the
  `mixin.debug.export` dump from step 0.
- Configs: `config/prettyrain.json5` (`tickVanillaWeather`, `:67`; client; backed up in `configs_edited/root_config/`
  per weather.md 3.4).

### Decisions

- **If Pretty Rain wins (Cool Rain inert): drop Cool Rain, or keep it?** Recommendation: drop it in the next
  both-sides deploy. It does nothing, and it holds a strict channel that blocks clients without it. Keep it only if the
  user wants to try `tickVanillaWeather: true` for its block sounds.
- **If Cool Rain wins: patch it, or drop it?** Patching keeps the per-block sounds. Pretty Rain's snow and sandstorm
  ambience stays lost, because Cool Rain cancels first. Dropping it brings that ambience back and is already
  season-correct (WE-24). Recommendation: **drop Cool Rain**, unless the user values the block sounds over
  snow/sand ambience. In that case, patch.
- **Make the order deterministic with `tickVanillaWeather: true`?** Then Pretty Rain never touches `tickRain`, and Cool
  Rain (patched) always runs. That only makes sense together with the patch, and it also loses Pretty Rain's
  snow/sand ambience. Recommendation: not on its own.

### Gotchas

- Cool Rain's splash particles are separate from Pretty Rain's (`doSplashParticles`). With Cool Rain running there may
  be double splashes. Look at that in the same session.
- `SeasonTypes.precipitationAt` runs up to about 100 times a tick while raining (Cool Rain's sample count). That is
  the same order as Pretty Rain's spawner calls. The adaptive lookup is cached per 4x4 column (`resolve`, `:382-441`).
- A Cool Rain update that renames its handler or moves the biome read makes the patch silently skip: the WARN line is
  the signal. Grep `latest.log` for it in WE-01.

### Tests

WE-25 stays, with its second half reworded to the outcome of step 0. New:
- **WE-37 Cool Rain order:** plains, rain, `MID_SUMMER`, on an iron block -> note whether Cool Rain's metal sound plays
  (records which handler wins; see Build step 0).
- **WE-38 Cool Rain snow:** (patched build, or Cool Rain dropped) plains, rain, `MID_WINTER` -> snow falls, no rain
  sound and no rain splash on blocks.
- **WE-39 Cool Rain dry season:** jungle, rain, `LATE_SUMMER` (Mid Dry) -> no rain sound or splash; `MID_SPRING` -> Cool
  Rain's foliage sound under leaves.

---

## seasons #3: Adaptive tint per position

Status Planned · Work patcherMod · TODO `worldgen / seasons #3`

### Goal

River and beach grass and foliage take the tint of the land the column adapts to: no seasonal tint next to deserts,
frozen or permanent-winter land; tropical tint next to jungles and savannas. Today they always take the temperate
sub-season tint (weather.md 4, "Adaptive tint is per biome").

### Now

- **How Serene Seasons tints.** Verified: Serene Seasons 8.1.0.24, decompiled
  `sereneseasons/handler/season/SeasonColorHandlers`.
  - `registerGrassAndFoliageColorHandlers()` (private static) saves the vanilla resolvers and then **assigns** two
    lambdas to `BiomeColors.GRASS_COLOR_RESOLVER` and `FOLIAGE_COLOR_RESOLVER` (`f_108789_`, `f_108790_`).
  - Each lambda gets `(Biome, x, z)`. It looks up the holder, picks the provider
    (`BiomeConfig.usesTropicalSeasons(holder) ? calendar.getTropicalSeason() : calendar.getSubSeason()`, where
    `calendar = SeasonHandler.getClientSeasonTime()`), then calls
    `SeasonColorUtil.applySeasonalGrassColouring/FoliageColouring(provider, holder, original)`.
  - It is called from `SereneSeasons.clientSetup` through `enqueueWork(() -> SeasonColorHandlers.setup())`, so it runs
    on the main thread, before any level exists.
- **What the pack does.** `mixin/SeasonColorUtilMixin.java:28-48` HEAD-injects both `SeasonColorUtil` methods:
  - year-round types return the original colour;
  - permanent seasons re-call with their own sub-season.

  It has no x/z. An adaptive biome gets `usesTropicalSeasons` = `SeasonTypes.wetDryCycle(ADAPTIVE)`, false
  (`mixin/SeasonBiomeConfigMixin.java:24-28`), so it always takes the temperate tint.
- **The column logic exists already.** `SeasonTypes.resolve(level, biome, pos)` (`seasons/SeasonTypes.java:382-441`):
  - rings out to `adaptiveRadius`;
  - skips unloaded chunks, and results with skipped points aren't cached;
  - caches per level and 4x4 column, under `synchronized`.

**Finding: no `lambda$` targeting is needed.** The TODO calls this fragile because the obvious hook is Serene Seasons'
synthetic lambdas. Instead, wrap the resolver field right after Serene Seasons assigns it:
- record the column in a ThreadLocal;
- delegate to Serene Seasons' resolver;
- let `SeasonColorUtilMixin`, which already intercepts the colour call, read the column.

Every hook is on a named method.

### Build

1. **Column holder.** In `seasons/SeasonTypes.java`, or a new `seasons/TintColumn.java`:
   `ThreadLocal<long[]>` or two ints, plus a depth flag; `set(x, z)`, `clear()`, `get()`. It must be a ThreadLocal:
   Rubidium builds chunk meshes and blends biome colours on worker threads.
2. **Wrap the resolvers.** New client mixin `mixin/SeasonColorHandlersMixin.java`:
   `@Mixin(value = SeasonColorHandlers.class, remap = false)`,
   `@Inject(method = "registerGrassAndFoliageColorHandlers", at = @At("RETURN"), remap = false, require = 1)`:

   ```java
   ColorResolver grass = BiomeColors.GRASS_COLOR_RESOLVER, foliage = BiomeColors.FOLIAGE_COLOR_RESOLVER;
   BiomeColors.GRASS_COLOR_RESOLVER = (b, x, z) -> { TintColumn.set((int) x, (int) z); try { return grass.getColor(b, x, z); } finally { TintColumn.clear(); } };
   // same for foliage
   ```

   `BiomeColors.GRASS_COLOR_RESOLVER` is a public static non-final field; Serene Seasons writes it too. Add the mixin to
   the `"client"` list in `creativeengineering.mixins.json`.
3. **Use the column.** In both `SeasonColorUtilMixin` handlers, before the existing checks:
   - read `TintColumn.get()`; if it is set and `biome.is(SeasonTypes.ADAPTIVE)`:
     - `land = SeasonTypes.resolve(level, biome, new BlockPos(x, 64, z))`;
     - if `land != biome`:
       - `SeasonTypes.yearRound(land)` -> return `originalColour`;
       - `permanentTint(tagged(land), level)` -> use that sub-season;
       - otherwise use the land's provider (`BiomeConfig.usesTropicalSeasons(land) ? calendar.getTropicalSeason() :
         calendar.getSubSeason()`).
     - If the provider differs from the one passed in, `cir.setReturnValue(SeasonColorUtil.applySeasonal...(landProvider,
       biome, originalColour))`.
   - Keep `biome` (the river), not `land`, in that call. The river's own base colour and `lessColorChange` stay; only
     the season overlay comes from the land.
   - **Clear the column before the re-call** (or check the depth flag), or the nested call resolves again.
4. **Optional: birch leaves.** Serene Seasons' birch handler (`registerBirchColorHandler`, a block colour lambda with
   `pos`) reads `level.getBiome(pos)` and `usesTropicalSeasons`. Same fix: a `@Redirect` would have to target the
   lambda, so skip it unless birch on river banks looks wrong.
5. Update weather.md:
   - 3.2: the tint now uses the column;
   - 3.3 code map row;
   - 4: remove "Adaptive tint is per biome ... Not done".

### Ids and data

- Serene Seasons classes, verified in the jar: `sereneseasons.handler.season.SeasonColorHandlers`
  (`registerGrassAndFoliageColorHandlers`, private static `()V`), `sereneseasons.util.SeasonColorUtil`,
  `sereneseasons.handler.season.SeasonHandler.getClientSeasonTime()`, `sereneseasons.config.BiomeConfig`.
- Vanilla: `net.minecraft.client.renderer.BiomeColors` `GRASS_COLOR_RESOLVER` / `FOLIAGE_COLOR_RESOLVER` (SRG
  `f_108789_` / `f_108790_`), `net.minecraft.world.level.ColorResolver#getColor(Biome, double, double)`.
- No tags or items. The adaptive list is `kubejs/generation_scripts/worldgen/season_types.json` -> tag
  `creativeengineering:seasons/adaptive` (`kubejs/server_scripts/tags/seasontags.js`).

### Decisions

- **Whether to do it** (the TODO's decision). Recommendation: **yes**. The wrapper is about 60 lines, uses no synthetic
  `lambda$` target, and reuses `resolve`. The risk listed in the TODO no longer applies.
- **Off the render thread, call `resolve` or only read its cache?** `resolve` reads `ClientLevel` heightmaps and
  biomes, `hasChunk`/`getHeight`/`getNoiseBiome`, from mesh worker threads. These are reads of arrays the main thread
  replaces when chunks unload: a benign race in practice, and vanilla's own off-thread code does similar reads.
  Recommendation: call `resolve` directly, and watch WE-41 for exceptions. If any show up, use a cache-only lookup off
  the main thread and fall back to the river's own tint.

### Gotchas

- **Order.** `ClientLevel` builds its `tintCaches` keyed by resolver identity when it is constructed, and so does
  Rubidium's colour cache. The wrap must happen before the first level loads. Serene Seasons' setup runs at client
  setup, and the RETURN inject runs with it, so this holds. Don't wrap later, for example on level load.
- **Meshes built before the neighbouring chunks load** get an incomplete `resolve`: the river's own tint, not cached.
  They keep it until that section is re-meshed. Expect a few river sections with the old tint right after joining; F3+A
  fixes them. Acceptable.
- **Changing the season doesn't re-mesh by itself** in Serene Seasons either. Same behaviour as today.
- **Contention.** `resolve`'s cache lock is taken once per tint call in adaptive biomes, from several mesh threads.
  Rubidium computes colours per column per section, so this is a few thousand lookups per section at worst. Check FPS
  near large rivers (WE-41).
- **Distant Horizons' LOD colours** may or may not go through `BiomeColors` (unverified). If they do, they follow too.

### Tests

- **WE-40 river tint:** river through a desert, `/season set MID_WINTER` then `MID_SUMMER` -> bank grass keeps one
  colour (like the desert's, no seasonal shift). River through a jungle -> tropical shift. River through plains ->
  temperate shift as before. A beach next to snowy plains -> no tint change.
- **WE-41 tint cost and safety:** fly along a large river with Rubidium for 2 minutes -> no exception in `latest.log`,
  FPS within noise of the same flight with `[seasons] enabled = false`.

---

## Built, needs play

| Row | Status | Note |
|---|---|---|
| seasons #1 Season types | Testing | Correct: the client side has never been run, WE-01 to WE-36. Fixed in passing (below): weather.md 8 still said to install the jar from `build/libs` (it has been deployed since 1.0.6), and README listed only WE-01 to WE-25. WE-17 sits after WE-36 in the list; harmless. The client session that runs these should also do seasons #2 step 0 (WE-37). |

## Writeup corrections made

- `feature_writeup/weather.md` 8 intro: "Install the current patcherMod jar from `build/libs` first" -> the deployed
  pack (patcherMod 1.0.6 or later, live since 1.0.6) has everything.
- `feature_writeup/weather.md`: the second "## 7. History" heading -> "## 9. History" (two sections were numbered 7).
- `feature_writeup/weather.md` History 2026-10-14: "STAGED 2026-10-14" -> "DEPLOYED 2026-10-14", the date in
  `deploy/DEPLOYED.json`.
- `feature_writeup/README.md` weather row: "patcherMod 1.0.5 code ... WE-01 to WE-25 pending" -> 1.0.5 + 1.0.6 code,
  WE-01 to WE-36 pending.
