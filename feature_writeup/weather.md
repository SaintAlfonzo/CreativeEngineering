# Weather: season types for Serene Seasons

Test prefix `WE-`. The per-biome result (every biome, every sub-season, rain/snow/nothing) is generated into
[weather_by_biome.md](weather_by_biome.md) by `python3 tools/gen_weather_table.py`; this file is the design,
the decisions and the tests.

## 0. At a glance

| Part | Status |
|---|---|
| Season types (arid, frozen, dry, tropical, temperate, adaptive) | BUILT 2026-10-08, dry + temperate + oceans 2026-10-09, in patcherMod 1.0.5 (in `mods/`, md5 68509043), DEPLOYED with pack 1.0.6 (2026-10-14). Server side checked on scratch servers (sections 6.2, 6.3); client side (rendering, tint, HUD) not run yet |
| Biome lists | `kubejs/generation_scripts/worldgen/season_types.json` -> tags `creativeengineering:seasons/<type>` (`kubejs/server_scripts/tags/seasontags.js`) |
| Arid | 15 sand/terracotta biomes: no rain all year, no seasonal temperature or tint, summer crops all year. HUD: "Arid", day N of the year |
| Frozen | 24 cold land biomes plus frozen and deep frozen ocean: snow all year (held at 0.1 or colder), no melting, no seasonal tint, winter crops only. HUD: "Frozen", day N of the year |
| Dry | 11 savannas, shrublands and hot grasslands (arid_highlands, savanna_badlands): Serene Seasons' wet/dry cycle, rain in Mid Wet only (Late Winter + Early Spring), as the mod does for a savanna |
| Tropical | 11 jungles and swamps plus the warm and lukewarm oceans: wet/dry cycle, rain in every season but Mid Dry |
| Temperate | ocean, deep ocean, temperate_highlands: Serene Seasons' temperate seasons, also above 0.8 |
| Cold (3.4) | BUILT 2026-10-11, patcherMod 1.0.6. 22 biomes (taiga x4, windswept x4, highlands, forested_highlands, alpine_highlands, shield, shield_clearing, steppe, caldera, haze_mountain, yellowstone, yosemite cliffs/lowlands, cloud_forest, cold and deep cold ocean): temperate seasons, snow exactly Late Autumn to Early Spring, rain otherwise |
| Permanent seasons (3.4) | BUILT 2026-10-11, same jar. The four Terralith skylands: one season all year. Winter = frozen shown as "Winter"; summer never snows; spring/autumn split the year Early 1/4, Mid 1/2, Late 1/4 on the HUD and snow only in their Early Spring / Late Autumn (the world's winter) |
| Adaptive | 7 rivers/beaches: each column takes the nearest land biome's type and temperature |
| Untagged | Serene Seasons' own rules (tropical when on its list or warmer than 0.8, temperate otherwise; blacklisted = vanilla). Tagged biomes ignore the blacklist |
| Snow line (3.4) | BUILT 2026-10-11, patcherMod 1.0.6 in `mods/`, not run in game. `snowLineY` 320 (build height): below it height alone never turns rain into snow; at and above it temperate and cold biomes always snow (a height gauge for flyers). Frozen and permanent seasons unaffected |
| Snow seasons (3.4) | BUILT 2026-10-11, same jar. `season_types.json` `snow_seasons`: ocean, deep_ocean, meadow, basalt_cliffs, blooming_plateau, granite_cliffs, lush_valley, valley_clearing, white_cliffs snow in winter only |
| Rain and cloud height (3.4) | 2026-10-11: Pretty Rain `cloudHeight` 191 -> 350 (`config/prettyrain.json5`); Rubidium Extra `cloud_height` 192 -> 350 (`config/sodium-extra-options.json`, vanilla clouds, which are off; Distant Horizons' clouds follow the same value via `DhCloudHeightMixin`). Both client, backed up in `configs_edited/root_config/` |

**Files to open first:** `development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/seasons/SeasonTypes.java`
(everything shared, with the design comment), then the mixins `Season*Mixin.java` in `mixin/`.

**Knobs:** `season_types.json` (which biome is which, and `snow_seasons`, `/reload`); `config/creativeengineering-common.toml`
`[seasons]`: `enabled` (false = Serene Seasons alone), `adaptiveRadius` (how far rivers/beaches look for land, default 64),
`snowLineY` (default 320, the build height; -64 = vanilla height snow); `config/prettyrain.json5` `cloudHeight` (highest y
rain particles spawn at, 350); `config/sodium-extra-options.json` `cloud_height` (vanilla cloud layer, 350).

**How to measure:** stand in the biome during rain (`/weather rain`), set the season with `/season set <season>`
(Serene Seasons' command). F3 shows the biome. The probe used on the scratch server (section 6.2) prints rain,
temperature and wheat fertility per sub-season for one biome of each kind.

## 1. Overview

Serene Seasons sorts every overworld biome into two kinds. **Tropical** is its config list plus, hardcoded,
every biome warmer than 0.8; it has a wet/dry cycle. **Temperate** is the rest; it cools by up to 0.8 in winter.
That made two visible mistakes:

- **Deserts and badlands rained.** They are warmer than 0.8, so tropical, and Serene Seasons' Mid Wet rains in
  every tropical biome without checking that the biome declares no precipitation.
- **Rivers snowed next to jungles and deserts.** A river has one fixed temperature (0.5; Terralith's
  `warm_river`, which only generates in the hot band, is 0.5 too). In winter and early spring it drops below
  0.15 and snows, while the land on both banks is tropical or arid and never cools.

The pack adds two year-round types (arid, frozen), takes tropical membership from a tag, fixes the savanna
rain, and makes rivers and beaches copy the land around them.

## 2. Reference: what Serene Seasons does

Read from Serene Seasons 8.1.0.24 decompiled (`python3 tools/decompile_mod.py mods/SereneSeasons-1.19.2-8.1.0.24.jar <out>`).
This is the mod alone, before the pack's season types. The per-biome result is section 1 of
[weather_by_biome.md](weather_by_biome.md), and its 2.7 table shows every biome's cells before and after.

Serene Seasons has only two classes, tropical and temperate, plus blacklisted biomes where it does nothing. What a
biome actually does also depends on two fields the class never looks at: its declared precipitation
(`none` / `rain` / `snow`) and its base temperature. That splits the two classes into several behaviours (2.2 to 2.5).

### 2.1 Where in the code

| Where | What |
|---|---|
| `BiomeConfig.usesTropicalSeasons(holder)` | config `tropical_biomes` or base temperature > 0.8. Used by weather, temperature, grass/foliage tint, crop fertility, SeasonHUD |
| `SeasonHooks.getBiomeTemperatureInSeason` | temperate (not tropical, base <= 0.8): -0.1 / -0.2 / -0.4 / -0.8 by sub-season |
| `SeasonHooks.shouldRainInBiomeInSeason` | tropical Mid Dry: false; Mid Wet: **true, whatever the biome declares**; else declared == rain |
| `SeasonHooks.isRainingAtHook` (mixin on `Level.isRainingAt`) | gameplay rain; tropical skips the temperature check |
| `SeasonHooks.getLevelRendererPrecipitation(holder)` (client) | what `renderSnowAndRain` / `tickRain` draw; gets the biome only, no position |
| `SeasonHooks.getBiomeTemperature(Level, holder, pos)` | every seasonal temperature read: renderer rain vs snow, snow layers, ice, melting, snow golems |
| `ModFertility.isCropFertile` | tropical: summer crops all year; temperate: per season, winter crops only where cold |
| `SeasonColorUtil.applySeasonal*Colouring` | grass/foliage tint by sub-season or tropical season |

SeasonHUD 2.0.4 (`ForgeSeasonHelper`, `CurrentSeason`) asks `usesTropicalSeasons` at the player's feet and
shows Spring/Summer/Autumn/Winter or Dry/Wet with a day count.

### 2.2 Tropical: the declaration splits it in two

Tropical = config `tropical_biomes` or base above 0.8. The temperature never changes. Rain follows six tropical
seasons, each two sub-seasons long:

| Tropical season | Sub-seasons | Rain |
|---|---|---|
| Mid Dry | Late Summer, Early Autumn | never |
| Mid Wet | Late Winter, Early Spring | **always, whatever the biome declares** |
| Early Dry, Late Dry, Early Wet, Late Wet | the other 8 | the biome's declaration |

Because Mid Wet ignores the declaration and the other four follow it:

| Declares | Biomes (in this pack) | Rains in | What it looks like |
|---|---|---|---|
| rain | jungles, swamps, stony/volcanic peaks, temperate highlands | 10 of 12 (all but Mid Dry) | wet all year with a short dry spell |
| none | savannas, shrublands, brushland, deserts, badlands, mesas (24 biomes) | 2 of 12 (Mid Wet only) | dry all year with a short forced wet spell |

So a savanna and a desert behaved identically. Neither had a dry *season*: they are dry all year, and the rain they
got was Mid Wet forcing it. (No tropical biome declares snow; one would pass through unchanged.) Gameplay rain
(`isRainingAtHook`) for a tropical biome also skips the temperature check, which would only matter above a tropical
biome's snow line; in this pack none has one below the build limit.

### 2.3 Temperate: a gradient by base temperature

Temperate = everything else (base 0.8 or below). Every sub-season subtracts an offset; snow falls when the result is
below 0.15:

| Sub-seasons | Offset | Snows if base is below |
|---|---|---|
| Early, Mid, Late Winter | -0.8 | 0.95 (every temperate biome) |
| Early Spring, Late Autumn | -0.4 | 0.55 |
| Mid Spring, Mid Autumn | -0.2 | 0.35 |
| Late Spring, Early Autumn | -0.1 | 0.25 |
| Early, Mid, Late Summer | 0 | 0.15 |

So "temperate" is not one behaviour. Snowy sub-seasons out of 12, for biomes that declare rain:

| Base | Examples | Snowy sub-seasons |
|---|---|---|
| 0.55 to 0.8 | plains, forest, beach, birch forest | 3 (winter) |
| 0.35 to 0.55 | river, meadow, Terralith warm_river | 5 (adds Early Spring, Late Autumn) |
| 0.25 to 0.35 | taiga, old growth pine taiga | 7 (adds Mid Spring, Mid Autumn) |
| 0.15 to 0.25 | windswept hills, birch taiga | 9 (adds Late Spring, Early Autumn) |
| below 0.15 | snowy plains, ice spikes, peaks | 12 |

This is why rivers snowed among warmer neighbours: in Early Spring a river reads 0.1 (snow) while plains read 0.4
(rain), and a jungle or desert next to it does not cool at all.

### 2.4 Odd cases from the declaration

The declaration only decides *whether* anything falls; temperature decides rain or snow, and snow layers and ice
look at temperature only.

- **Temperate, declares none** (`terralith:rocky_mountains`, 0.3): nothing is ever drawn, yet snow layers build up
  during storms in its 7 cold sub-seasons.
- **Temperate, declares snow at base 0.2** (`terralith:siberian_taiga`, `terralith:skylands_winter`): rain in the 3
  summer sub-seasons, snow in the other 9.
- **Height** subtracts 0.05 per 40 blocks above y80 on top of the offset, so mountains snow further into spring and
  autumn than 2.3 shows.

### 2.5 Blacklisted and other dimensions

- **Blacklisted biomes** (config `blacklisted_biomes`) follow vanilla, the same all year. Server config: oceans,
  caves, mushroom fields, mirage isles. Single-player worlds get Serene Seasons' default list, which also has
  `minecraft:river` (so there the river rained all year) and lacks the Terralith caves.
- **Other dimensions:** only `minecraft:overworld` is whitelisted; everything else is vanilla.

### 2.6 Not weather, but by class too

- **Grass and foliage tint:** tropical biomes use the wet/dry palette, temperate ones the sub-season palette. A
  hardcoded list (swamp and some Biomes O' Plenty biomes) changes colour less.
- **Crop fertility:** tropical biomes grow summer crops (and unlisted crops) all year; temperate biomes by season;
  anywhere below 0.15 right now grows winter crops only. A hardcoded infertile list names only a Biomes O' Plenty
  biome, so it does nothing here.
- **Weather frequency** is world-wide, not per biome: winter clears thunder and cuts dry spells to 0.5-1.5 days,
  spring cuts them to 0.5-4 days, summer brings thunder within 0.5-1.5 days.
- **SeasonHUD** shows Wet/Dry in tropical biomes and the four seasons elsewhere.

### 2.7 Summary: the effective behaviours

| Behaviour | Class | Weather over the year |
|---|---|---|
| tropical-wet | tropical, declares rain | rain except Mid Dry |
| tropical-dry | tropical, declares none | dry except forced Mid Wet rain |
| temperate gradient | temperate, declares rain | rain, snow in 3 to 12 sub-seasons by base temperature |
| temperate-dry | temperate, declares none | nothing drawn; snow layers in cold sub-seasons |
| snow-declared warm | temperate, declares snow above 0.15 | rain in warm sub-seasons, snow otherwise |
| vanilla | blacklisted / other dimensions | fixed all year |

The pack's season types (section 3) replace tropical-dry with arid (deserts, badlands) or a real wet season
(savannas), put the snowy end of the gradient and the snow-declared biomes under frozen, and move rivers and
beaches off the gradient onto their land.

## 3. What the pack changes

### 3.1 Types

| Type | Weather | Temperature | Tint | Crops (Serene Seasons fertility) | SeasonHUD |
|---|---|---|---|---|---|
| arid | none, all year (renderer and gameplay rain) | base, no seasonal offset | none | tropical rule: summer + unlisted crops, all year | Dry icon and colour, name "Arid", day N of the year / year length |
| frozen | snow all year | at most 0.1, no offset; nothing melts | none | winter crops only | Winter icon and colour, name "Frozen", day N of the year |
| dry | wet/dry cycle, rain in Mid Wet only (Late Winter, Early Spring), whatever the biome declares | base | tropical | tropical rule | Dry / Wet |
| tropical | wet/dry cycle, rain except Mid Dry (Serene Seasons' own rule for a biome that declares rain) | base | tropical | tropical rule | Dry / Wet |
| temperate | Serene Seasons' temperate rain/snow by base temperature | seasonal offset, also above 0.8 | temperate | temperate rule | four seasons |
| adaptive | the land's | the land's | unchanged (per biome, see 4) | the land's | the land's season |

Which biomes: `season_types.json` (sand and terracotta biomes arid, judged from Terralith's surface rules, plus
painted_mountains; every savanna and the hot, dry grasslands (shrublands, brushland, arid_highlands) dry; jungles,
swamps and warm/lukewarm oceans tropical; everything below 0.15 or declaring snow frozen, frozen oceans too;
ocean, cold ocean and temperate_highlands temperate; every river, beach and shore adaptive). The generator's
section 2.1 lists biomes the file may have missed.

Any tagged biome has seasons even when Serene Seasons' `blacklisted_biomes` names it (`SeasonBiomeConfigMixin`
on `enablesSeasonalEffects`). That is what brings the oceans in: the pack config and the mod's default both
blacklist every ocean, so before this they kept vanilla weather all year.

### 3.2 Adaptive columns

`SeasonTypes.resolve(level, biome, pos)`: for a river/beach column, rings of 8 directions at 8, 16, 24, 32,
48, 64 blocks (up to `adaptiveRadius`). Each point counts if its surface biome (MOTION_BLOCKING heightmap) is
not adaptive, not ocean and not blacklisted (so not a cave). The first ring with any land decides by majority.
Unloaded chunks are skipped, never loaded or generated. Results are cached per level and 4x4 column, cleared on
tag reload; a result with skipped points is used but not cached, so it is redone once the chunks load.

Applied where Serene Seasons has a position: `getBiomeTemperature(Level, ...)`, `isRainingAtHook`, crop
fertility, SeasonHUD. The renderer only passes the biome, so `SeasonRainColumnMixin` records the column from
the call just before (`MutableBlockPos.set` in renderSnowAndRain, `getHeightmapPos` in tickRain).

### 3.3 Code map

| Mixin | Side | Target | Does |
|---|---|---|---|
| `SeasonBiomeConfigMixin` | both | `BiomeConfig.usesTropicalSeasons`, `enablesSeasonalEffects` | tagged types answer first (dry/tropical yes); any tagged biome has seasons despite the blacklist |
| `SeasonHooksTypesMixin` | both | `SeasonHooks` | snow line and snow seasons (`SeasonTypes.applySnowRules` at RETURN of `getBiomeTemperature(Level, ...)`, 3.4); oceans freeze only when frozen-tagged (`shouldFreezeWarmEnoughToRainHook`); adaptive temperature and gameplay rain; arid never rains; dry rains in Mid Wet only; arid/frozen no offset; frozen cap; temperate offset above 0.8 (redirect of `getBaseTemperature`, SRG `m_47554_`) |
| `SeasonFertilityMixin` | both | `ModFertility.isCropFertile` | adaptive biome; arid tropical rule; frozen and permanent winter winter-only; permanent seasons count as their season (`ISeasonState.getSeason` redirect) |
| `SeasonHooksClientMixin` | client | `SeasonHooks.getLevelRendererPrecipitation` | adaptive column for the renderer |
| `SeasonRainColumnMixin` | client | `LevelRenderer` renderSnowAndRain / tickRain | records the column (SRG names, no refmap) |
| `SeasonColorUtilMixin` | client | `SeasonColorUtil` | no tint for arid/frozen/permanent winter; permanent spring/summer/autumn tinted as their own sub-season |
| `SeasonHudHelperMixin` | client | SeasonHUD `ForgeSeasonHelper` | adaptive biome; arid/frozen season, day of year, year length; permanent seasons: season, Early/Mid/Late part, day and length |
| `SeasonHudCurrentSeasonMixin` | client | SeasonHUD `CurrentSeason.getTranslation(boolean)` | "Arid" / "Frozen" name (lang `desc.seasonhud.season.arid/.frozen`) |
| `PrettyRainSeasonMixin` | client | Pretty Rain `WeatherParticleSpawner` spawnParticle / getBiomeSound (@Pseudo) | both `getPrecipitation` reads answer from `SeasonTypes.precipitationAt`: adaptive column, season rule, rain or snow by seasonal temperature |

### 3.4 Snow line, cold, permanent seasons, snow seasons, rain and cloud height (2026-10-11)

Two requests the same day. First: rain drawn up to y500, snow lines at least 500 blocks (skylands_summer was
snowing), temperate oceans snowing in winter only, and a list of the biomes snowing in Early Spring or Late Autumn.
Second, after the user's review of that list (weather_by_biome.md 2.8): the list below marked cold, the rest winter
only; rain and clouds at 350; the snow line moved to build height as a gauge; permanent seasons for the skylands.

**Why skylands_summer snowed.** It was an untagged 0.5 biome, so Serene Seasons treated it as temperate: 0.1 at sea
level in Early Spring and Late Autumn (below 0.15, snow) and 0.3 in Mid Spring/Autumn. The islands float high, and
vanilla cools 0.05 per 40 blocks above y80, so at island height Mid Spring and Mid Autumn snowed too. Every
untagged biome had the same height snow: plains and forests above about y280 in Early Spring, windswept hills above
y120 in summer.

**Snow line** (`SeasonTypes.applySnowRules`, CEConfig `[seasons] snowLineY`, default 320 = overworld build height).
Applied to every temperature Serene Seasons hands out for a real level (`getBiomeTemperature(Level, ...)` at RETURN,
after the river/beach swap), so the renderer, Pretty Rain, gameplay rain, snow layers, ice and melting all agree.
- Below the line, a column that would snow (under 0.15) is checked at y80 in the same column. If y80 is warm enough to
  rain this sub-season, the column reads 0.15 (just warm enough to rain and melt). Only ever raised. Every dimension.
- At and above the line, temperate and cold biomes (tagged, or temperate by Serene Seasons' rule) read at most 0.1:
  snow in every season. No block can sit at y320, so no snow lands; falling snow only tells a flyer they are above
  build height. Overworld only (Serene Seasons' dimensions). Tropical, dry and arid biomes keep vanilla height
  cooling above the line (a jungle would need y700+).
- Frozen and permanent-season biomes are skipped (their own rules below).
- The first version (same day) used a minimum of y500 and no gauge.

**Cold** (`season_types.json` `cold`, tag `creativeengineering:seasons/cold`, 22 biomes chosen by the user): Serene
Seasons' temperate seasons (offset, tint, crops, HUD), then `SeasonTypes.seasonTemperature` holds it at 0.1 or colder
from Late Autumn to Early Spring and at 0.15 or warmer the rest of the year. Every listed biome is 0.5 or colder, so the
cap changes nothing in practice; the floor is what stops taiga (Mid Spring/Autumn) and windswept/birch taiga/yellowstone
(all of spring and autumn) snowing early. cold_ocean and deep_cold_ocean moved here from temperate; like temperate
oceans they get snow but never ice over (`vanillaIce`). windswept_savanna stays dry.

**Snow seasons** (`season_types.json` `snow_seasons`: biome -> sub-seasons it may snow in; `early_spring` ...
`late_winter` or a whole season name; `[]` = never). `seasontags.js` turns it into one tag per sub-season,
`creativeengineering:snow/no_<sub_season>`. Below the snow line, a biome in the current sub-season's tag reads 0.15
instead of anything colder. Only in Serene Seasons' dimensions; frozen and permanent seasons ignore it; rivers and
beaches follow their land's entry. Winter only: ocean, deep_ocean (first request), and the biomes from the 2.8 list the
user did not mark cold: meadow, basalt_cliffs, blooming_plateau, granite_cliffs, lush_valley, valley_clearing,
white_cliffs. (forested_highlands was briefly here, then moved to cold at the user's request.)

**Permanent seasons** (`permanent_spring|summer|autumn|winter`, one Terralith skylands biome each). No seasonal
temperature offset (`noSeasonalOffset`), then `seasonTemperature`:

| Type | Weather | HUD (SeasonHUD) | Grass tint | Crops |
|---|---|---|---|---|
| permanent_winter (skylands_winter) | as frozen: snow all year, held at 0.1, nothing melts | "Winter", day of the year | none (as frozen) | winter only (as frozen) |
| permanent_summer (skylands_summer) | rain, never snow (0.15 or warmer) | "Summer", day of the year | Mid Summer | summer |
| permanent_spring (skylands_spring) | snow in its Early Spring, rain otherwise | Early / Mid / Late Spring (with sub-seasons shown) and the day within that part; day of its year otherwise | its own part (Early/Mid/Late Spring) | spring |
| permanent_autumn (skylands_autumn) | snow in its Late Autumn, rain otherwise | Early / Mid / Late Autumn, same day rule | its own part | autumn |

Spring and autumn calendar (`SeasonTypes.permanentPart/permanentDate`): the year is split Early 1/4 (3 Serene Seasons
sub-seasons), Mid 1/2 (6), Late 1/4 (3). The snowy quarter is placed on the world's winter so the whole world's snow
lines up and the temperature code can read it from the sub-season alone: spring's year starts at the world's Early
Winter (Early Spring = world winter, Mid = world spring + summer, Late = world autumn); autumn's starts at the world's
Early Spring (Early = world spring, Mid = world summer + autumn, Late = world winter). Snow is 1/4 of the year on both.
On the server (`sub_season_duration` 10): Early and Late 30 days, Mid 60, year 120; single-player worlds (Serene
Seasons' default 8): 24, 48 and 96. Code: `SeasonHooksTypesMixin` (temperature),
`SeasonFertilityMixin` (redirects `ISeasonState.getSeason` in `isCropFertile` to the permanent season),
`SeasonColorUtilMixin` (repeats the tint call with the permanent sub-season), `SeasonHudHelperMixin` (season,
part, day, length). skylands_winter moved out of frozen; the HUD name override ("Frozen") is only for frozen.

**World generation is unchanged.** Worldgen uses vanilla temperatures, so `freeze_top_layer` still puts snow above the
vanilla line (4.3: birch_taiga 68% covered, taiga and windswept_spires on high ground). In game, Serene Seasons'
melting sees 0.15 there whenever y80 rains, so those caps melt in the first warm sub-season.

**Rain height.** Pretty Rain spawns weather particles in a 10-block sphere around the camera
(`WeatherParticleSpawner.update`). With `spawnAboveClouds` false it clamps the spawn y to `cloudHeight` (was 191).
Above about y201 every particle then spawned more than 10 blocks below the camera, was flagged out of range on its
first tick (`removeIfOOB`) and faded out at alpha 0, so nothing was visible. `cloudHeight` is now 350 (500 for a few
hours on 10-11): rain is drawn up to about y360, and with the gauge, snow falls around a flyer between y320 and y360.

Performance: the number of spawn attempts per tick is the same at any height (`particleDensity` 75, storm 150, times
the rain level). Each attempt does the same heightmap check, biome read and `SeasonTypes.precipitationAt` call wherever
the camera is; the clamp only moved the y. What changes is particle lifetime between y201 and y350: particles that used
to die on their first tick now live like ground rain (rain 50 ticks, snow 100). Up in open air there is no ground to
stop them, so rain leaves the 10-block sphere within about 20 ticks of falling (gravity 1.0) and fades out. So flying
in a storm now costs what standing in a storm on the ground costs, capped as before by `maxParticleAmount` 1500. No
new worst case. Rain sound is unchanged: Pretty Rain only plays it when the surface is within 10 blocks of the camera.
The snow line adds one Serene Seasons temperature read (at y80) for a column that would otherwise snow between y81 and
y319, and one tag lookup per temperature read.

**Cloud height.** Rubidium Extra's `cloud_height` (192 -> 350) moves the vanilla cloud layer (`MixinDimensionEffectsOverworld`
on `DimensionSpecialEffects.getCloudHeight`). But vanilla clouds are off (`options.txt` `renderClouds:"false"`, set by
Distant Horizons' `overrideVanillaGraphicsSettings`). The clouds on screen are Distant Horizons' LOD clouds
(`enableCloudRendering = true`), placed by `CloudRenderHandler` at the level's max height + 200 (y520 in the overworld)
on every frame, ignoring the vanilla value. `DhCloudHeightMixin` (client, @Pseudo, require 1) redirects that
`ILevelWrapper.getMaxHeight()` read in `CloudRenderHandler.preRender` to the vanilla cloud height - 200, so DH's clouds
sit at the vanilla height (350, Rubidium Extra) and the one setting moves both layers. The same local drives DH's
culling. Dimensions whose vanilla cloud height is NaN keep DH's own. DH's jar is in `patcherMod/libs/` for compiling;
a DH update that renames `preRender` will fail the mixin at startup (require 1), which is the signal to recheck.

## 4. Limits and decisions

- **Mountains (2026-10-09):** `stony_peaks`, `volcanic_peaks`, `volcanic_crater` tagged tropical (rain except Mid
  Dry); `rocky_mountains` tagged frozen, joining frozen/jagged peaks. rocky_mountains declares no precipitation, so
  even frozen nothing is drawn there; snow layers still settle during storms all year and never melt. No untagged
  biome is warmer than 0.8 any more.
- **`terralith:rocky_mountains`** declares no precipitation at 0.3 and is untagged: no snowfall drawn, but snow
  layers settle in cold sub-seasons. Left as is.
- **Arid or dry:** sand and terracotta biomes are arid; every savanna is dry, `savanna_badlands` included
  (terracotta, but a savanna), as is `arid_highlands` despite its name (packed mud and coarse dirt, no sand).
  `wooded_badlands` stays arid. `hot_shrubland` declares rain but is a dry grassland, so it rains in Mid Wet only.
- **Savanna rain (decided 2026-10-09):** dry biomes rain in Mid Wet only, which is what Serene Seasons does for a
  savanna (2 of 12 sub-seasons). The 10-08 build had them rain through all of Early, Mid and Late Wet; that was
  dropped. Jungles keep the mod's rule (rain except Mid Dry). How much rain that is: section 4.2.
- **Oceans have seasonal weather, but only frozen oceans get seasonal ice (decided 2026-10-09).** Serene Seasons
  blacklists oceans by default because its temperature also drives freezing. Ocean and cold ocean (base 0.5) read
  below 0.15 in Late Autumn, the three winter sub-seasons and Early Spring, so snow falls over them then; but
  `SeasonHooksTypesMixin.creativeengineering$oceanIce` answers Serene Seasons' freeze check
  (`shouldFreezeWarmEnoughToRainHook`, inside `Biome.shouldFreeze`) with vanilla's temperature for any ocean that is
  not frozen-tagged, so they never ice over. Frozen oceans keep the seasonal value, held at 0.1, so their open-water
  patches (temperature modifier `frozen`, 0.2) freeze over with time and never melt. Warm and lukewarm oceans never
  cool. Rivers and beaches are not oceans: they freeze when their land does. Lukewarm, ocean and deep ocean were not in the request; they were tagged to match their neighbours
  (lukewarm borders jungles and savannas, ocean borders plains and forests). Untag a list entry to give that ocean
  back to the blacklist.
- **Frozen is capped, not offset:** frozen biomes read min(temperature, 0.1). Height still makes peaks colder.
  `siberian_taiga` and `skylands_winter` (base 0.2) now snow all year instead of raining in summer.
- **Adaptive tint is per biome:** Serene Seasons' grass/foliage tint gets the biome but no position inside its
  colour helpers (`SeasonColorUtil`), so river and beach grass keeps the temperate tint. The resolvers that do
  have x/z are Serene Seasons' own lambdas assigned to `BiomeColors.GRASS_COLOR_RESOLVER` / `FOLIAGE_COLOR_RESOLVER`
  at client setup; patching them means targeting synthetic `lambda$` methods, which is fragile. Not done.
- **Adaptive on the renderer uses a recorded column:** the alternative, redirecting the biome lookup in
  LevelRenderer, collides with Serene Seasons' own @Redirect on the same call (a second redirect fails to apply).
- **The HUD's own setting "show tropical seasons"** is not consulted for arid/frozen: they always show.
- **SeasonHUD uses Dry/Winter icons:** its season enum can't take new entries; arid and frozen borrow the Dry and
  Winter icon and colour, with their own names.
- **Other dimensions:** Serene Seasons only runs in the overworld; the tags do nothing in the pack's own
  dimensions (vanilla rules there).
- **Worldgen** (WorldGenRegion) keeps vanilla rules, as Serene Seasons does: freeze-top-layer and snow at
  generation use the biome's own temperature.

### 4.1 What the mountains border

Measured on a scratch server with the climate dump's seed: surface biome every 64 blocks over a 19 km square
(301 x 301 columns, `getBaseHeight` + the live biome source, no chunks generated), then for each mountain biome
every biome within 256 blocks of it. The band is the climate temperature band from `/dumpclimate`. "Untagged" means
Serene Seasons' temperate rule (plains, forests, taigas, meadows). Percent of the land around it.

| Mountain | Season type | Band | Surface y | What is around it |
|---|---|---|---|---|
| `stony_peaks` | untagged (tropical by >0.8) | warm 100% | ~220 | other peaks 36% (volcanic_peaks 23%), jungles 27% (rocky_jungle, sparse, jungle), dry 26% (brushland, savannas), forest 2% |
| `volcanic_peaks` | untagged (tropical by >0.8) | warm 72%, temperate 21% | ~215 | dry 28% (brushland, savanna), jungles 21%, untagged temperate 13% (forest, spruce taiga), badlands 8%, peaks 26% (stony_peaks, frozen/jagged 5%) |
| `volcanic_crater` | untagged (tropical by >0.8) | warm | ~125 | rare (15 cells); savanna/brushland 62%, jungle 7%, lukewarm ocean 7% |
| `painted_mountains` | arid | hot 100% | ~225 | badlands 77%, stony/volcanic peaks 15% |
| `rocky_mountains` | untagged (temperate, 0.3, declares none) | temperate 51%, cold 48% | ~210 | yellowstone, meadow, forest, shield, birch taiga 64%; frozen/jagged peaks 20%; tropical/dry under 3% |
| `jagged_peaks` | frozen | temperate 84%, cold 15% | ~235 | untagged temperate 50% (meadow, dark forest, plains, taiga, yellowstone); rocky mountains / frozen peaks 32%; frozen land 2% |
| `frozen_peaks` | frozen | temperate 68%, cold 18%, frozen 13% | ~230 | untagged temperate 42% (dark forest, meadow, plains, taiga); frozen land 10% (snowy slopes, snowy plains) |
| `snowy_slopes` | frozen | temperate 55%, frozen 44% | ~160 | frozen 25% (snowy plains, frozen ocean), plains/forest/meadow 22% |
| `scarlet_mountains`, `emerald_peaks` | frozen | frozen 100% | ~240 | frozen land about 50% (snowy plains, snowy taiga, alpine grove) |
| `haze_mountain` | untagged (0.3) | warm 55%, temperate 44% | ~70 | jungle_mountains 22%, fractured savanna 15%, windswept forest 15%, valleys |
| `jungle_mountains` | tropical | warm | ~70 | haze mountain 31%, savannas 22%, river/mangrove/jungle |

So:

- **Volcanoes do sit among jungles.** volcanic_peaks borders jungles (21%) about as often as savannas and brushland
  (28%), plus badlands, and reaches into the temperate band (forest, spruce taiga) about a fifth of the time.
- **stony_peaks is hot only.** All its samples are in the warm band, and it pairs with volcanic_peaks. Its
  neighbours are jungles and savannas; it is never near snow apart from where a range runs into frozen peaks (2%).
- **The snowy peaks are not cold-band biomes.** jagged_peaks and frozen_peaks sit mostly in the temperate band, so
  half of what surrounds them is meadow, dark forest, plains and taiga, all untagged temperate, which rain in summer
  while the peaks snow all year. That's the "out of step" rows in weather_by_biome.md 2.3; height makes them cold
  anyway. Only scarlet_mountains and emerald_peaks are surrounded by frozen land.
- **rocky_mountains is the temperate/cold range.** It sits beside jagged and frozen peaks, never near jungles or
  deserts. It declares no precipitation, so nothing falls there (2.4).
- **Peaks are picked by erosion and weirdness, temperature picks which peak.** The same mountain ridge changes name
  as it crosses bands: stony_peaks/volcanic_peaks in the warm band, rocky_mountains, jagged/frozen peaks in the
  temperate and cold bands, scarlet/emerald peaks in the frozen band. That's why volcanic_peaks shows frozen/jagged
  peaks as a neighbour 5% of the time.

### 4.2 How often it rains

Rain is world-wide: the server decides when it rains, and each biome only decides what that rain looks like there
(rain, snow or nothing). A biome in a season that allows rain shows it whenever the world is raining. Vanilla
(`ServerLevel.advanceWeatherCycle`) draws a storm of 12000-24000 ticks (10-20 minutes) and then a dry spell of
12000-180000 ticks. Serene Seasons (`change_weather_frequency = true`, `RandomUpdateHandler.adjustWeatherFrequency`)
shortens the dry spell by the **temperate** season, whatever the biome:

| Temperate season | Dry spell | Storm + dry spell, on average | Share of time raining |
|---|---|---|---|
| Winter | 10-30 min | 35 min | 43% |
| Spring | 10-80 min | 60 min | 25% |
| Summer, Autumn | 10-150 min | 95 min | 16% |

The tropical calendar on top of that: Early Wet = Early + Mid Winter, Mid Wet = Late Winter + Early Spring, Late
Wet = Mid + Late Spring. So:

- **Dry biomes (savannas)** rain in Mid Wet only: Late Winter rains 43% of the time, Early Spring 25%, about a
  third of those two sub-seasons. On the server one sub-season is 26.7 hours of play (10 days of 192000 ticks), so
  that's about 11.4 h of rain in Late Winter and 6.7 h in Early Spring: roughly 18 hours of rain, as 70-odd separate
  storms, then nothing for the other 10 sub-seasons (about 11 days of play). In single-player (2.7 h sub-seasons)
  it's about 1.8 h of rain over 7 storms per year.
- **Tropical biomes (jungles)** rain whenever the world does, except in Mid Dry (Late Summer, Early Autumn).
  Across the whole wet season (6 sub-seasons) that's 34% of the time; in the dry season, 16% outside Mid Dry.
- **Temperate biomes** see the same world rain; in winter it falls as snow where the temperature is below 0.15.

Sleeping changes this: Better Days' `weatherEffect = SLEEPING` runs the weather clock at the sleep speed, and the
season clock jumps with it.

### 4.3 Cold biomes that are not frozen

Untagged biomes colder than the plains (base 0.45 or below, or sampled mostly in the cold band). Serene Seasons
treats them as temperate: they snow in winter and, the colder they are, further into spring and autumn.

**Snow at generation.** Worldgen ignores the season (it runs on `WorldGenRegion`, where Serene Seasons' hooks fall
back to vanilla). Snow is placed by vanilla's `freeze_top_layer` wherever the height-adjusted temperature is below
0.15, which for these biomes means above a snow line: y = 80 + (base - 0.15) / 0.00125, plus or minus about 8 blocks
of noise. Terralith's surface rules only place snow blocks or powder snow in biomes that are already frozen-tagged
(groves, slopes, peaks, wintry forests, snowy shield, snowy maple forest, siberian grove), and `rocky_mountains` has
its own snow features. The "Generated" column comes from the scratch server (2026-10-09): 4 chunks per biome picked
from the grid sample, generated in full, top block of every column of that biome checked. Biomes from
`shield_clearing` on (base 0.4 and up) were not generated (the server ran out of heap); the snow line puts any snow
at y280 or higher there.

| Biome | Base | Snow line | Snowy sub-seasons (sea level) | Generated: columns with snow | Where the samples were |
|---|---|---|---|---|---|
| `windswept_hills`, `windswept_forest`, `windswept_gravelly_hills` | 0.2 | y120 | 9 | none | y58-102, all below the line |
| `terralith:windswept_spires` | 0.2 | y120 | 9 | 14%, from y116 | y61-126 |
| `terralith:birch_taiga` | 0.22 | y136 | 9 | **68%**, from y132 | y121-180 (sits high) |
| `terralith:yellowstone` | 0.248 | y158 | 9 | none | y73-138 |
| `taiga` | 0.25 | y160 | 7 | 15%, from y156 | y62-171 |
| `old_growth_spruce_taiga` | 0.25 | y160 | 7 | none | y73-140 |
| `terralith:cloud_forest`, `terralith:yosemite_lowlands` | 0.25 | y160 | 7 | none | y62-91 |
| `old_growth_pine_taiga` | 0.3 | y200 | 7 | none | y132-176 |
| `terralith:haze_mountain` | 0.3 | y200 | 7 | none | y60-102 |
| `terralith:rocky_mountains` | 0.3 | y200, plus its snow features | 7 (declares none: nothing falls, layers settle, 2.4) | **87%**, from y187 | y184-250 |
| `terralith:forested_highlands` | 0.36 | y248 | 5 | none | y83-142 |
| `terralith:yosemite_cliffs` | 0.375 | y260 | 5 | none | y80-128 |
| `terralith:highlands`, `steppe`, `shield`, `lush_valley` | 0.4 | y280 | 5 | none | y56-171 |
| `terralith:alpine_highlands` | 0.45 | above y320 | 5 | none | y68-119 |
| `shield_clearing`, `valley_clearing`, `white_cliffs`, `granite_cliffs` | 0.4 | y280 | 5 | not generated | |
| `terralith:caldera` | 0.45 | above y320 | 5 | not generated | |

So at generation only birch_taiga and rocky_mountains are mostly snow-covered, taiga and windswept_spires are
snowy on their high ground, and the rest generate green. Once the world runs, Serene Seasons adds snow layers during
winter storms (and in the colder ones, spring and autumn storms) and melts them again when the temperature
climbs back over 0.15.

## 5. File map

- [season_types.json](../kubejs/generation_scripts/worldgen/season_types.json): the lists, with a `_readme`
- [seasontags.js](../kubejs/server_scripts/tags/seasontags.js): json -> biome tags via `global.tags`
- [SeasonTypes.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/seasons/SeasonTypes.java)
- Mixins: [mixin/](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/) `Season*Mixin.java`
- CEConfig `[seasons]`: [CEConfig.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/CEConfig.java)
- Lang: `development/patcherMod/src/main/resources/assets/creativeengineering/lang/en_us.json`
- Generator: [tools/gen_weather_table.py](../tools/gen_weather_table.py) -> [weather_by_biome.md](weather_by_biome.md)

## 6. Offline checks

### 6.1 Generator

`python3 tools/gen_weather_table.py` (also `/cegen run weather_by_biome`). Exit 1 when `season_types.json` names
an unknown biome or lists one biome twice. Its section 2.1 lists cold, dry, river/beach and >0.8 biomes the
file leaves untagged; 2.2 must stay empty (no Mid Wet rain in a dry biome); 2.7 is the before/after table.

### 6.2 Scratch server probe (2026-10-08)

Dedicated server from the instance's server-side mods with the new jar, fresh world, `/weather rain` given 20 s to
ramp up, then a scratch-only command (not in the pack) that found one biome of each kind near spawn and, for each of
the 12 sub-seasons, called `SeasonHooks.isRainingAtHook`, `SeasonHooks.getBiomeTemperature` and
`ModFertility.isCropFertile("minecraft:wheat")` at the surface. No mixin errors in the log.

| Probe | Resolved to | Type | ESp MSp LSp ESu MSu LSu EAu MAu LAu EWi MWi LWi | Temperature | Wheat |
|---|---|---|---|---|---|
| desert | desert | arid | `- - - - - - - - - - - -` | 2.0 all year | all year |
| badlands | badlands | arid | `- - - - - - - - - - - -` | 1.96 all year | all year |
| savanna | savanna | tropical | `R R R - - - - - - R R R` | 1.19 all year | all year |
| brushland | brushland | tropical | `R R R - - - - - - R R R` | 1.2 all year | all year |
| jungle | jungle | tropical | `R R R R R - - R R R R R` | 0.95 all year | all year |
| plains (control) | plains | untagged | `R R R R R R R R R S S S` | 0.35 to 0.75, -0.05 in winter | spring to autumn |
| snowy plains | snowy plains | frozen | `S S S S S S S S S S S S` | 0.0 all year | never |
| siberian taiga (base 0.2) | siberian taiga | frozen | `S S S S S S S S S S S S` | 0.1 all year (capped) | never |
| beach column | plains | (plains) | `R R R R R R R R R S S S` | plains' | plains' |
| terralith:warm_river | desert | arid | `- - - - - - - - - - - -` | 2.0 all year | all year |
| stony_peaks (untagged, >0.8) | stony_peaks | Serene Seasons tropical | `R R R R R - - R R R R R` | 0.88 | all year |

Not covered by this: anything client side (what the renderer draws, splashes, tint, SeasonHUD), which needs WE-01 onward.

### 6.3 Scratch server probe (2026-10-09): dry, temperate, oceans

Same setup with the 10-09 jar (pack server config, so oceans and `minecraft:river` stay on the blacklist). For each
biome and sub-season: `BiomeConfig.usesTropicalSeasons`, `enablesSeasonalEffects`,
`SeasonHooks.shouldRainInBiomeInSeason` and `getBiomeTemperatureInSeason` at y64 (each call at its own position:
the temperature cache is keyed by position, so reusing one position returns the first biome's value). No mixin
errors in the log.

| Biome | Wet/dry | Seasons | ESp MSp LSp ESu MSu LSu EAu MAu LAu EWi MWi LWi | Temperature |
|---|---|---|---|---|
| savanna, arid_highlands, hot_shrubland, savanna_badlands (dry) | yes | yes | `R - - - - - - - - - - R` | base, all year |
| jungle (tropical) | yes | yes | `R R R R R - - R R R R R` | 0.95 |
| desert, painted_mountains (arid) | no | yes | `- - - - - - - - - - - -` | base |
| temperate_highlands (temperate, 0.9) | no | yes | `R R R R R R R R R S S S` | 0.5 ... 0.9 ... 0.1 in winter |
| ocean, cold_ocean, deep_cold_ocean (temperate, blacklisted) | no | yes | `S R R R R R R R S S S S` | 0.1 ... 0.5 ... -0.3 |
| frozen_ocean, deep_frozen_ocean (frozen, blacklisted) | no | yes | snow all year (frozen_ocean declares snow, which the renderer passes through) | 0.0 to 0.1 |
| lukewarm_ocean, warm_ocean (tropical, blacklisted) | yes | yes | `R R R R R - - R R R R R` | 0.5 |
| stony_peaks (untagged, 1.0) | yes | yes | `R R R R R - - R R R R R` | 1.0 |
| dripstone_caves (untagged, blacklisted) | no | **no** | vanilla | 0.8 |

Ice, same day after the ocean-ice change, at real ocean positions from the grid sample in `MID_WINTER`
(`shouldFreezeWarmEnoughToRainHook` false = water there freezes): ocean, cold ocean, deep cold ocean read -0.3 and do
**not** freeze; frozen and deep frozen ocean read 0.1 and freeze; warm ocean 0.5, no; plains (0.0) and a river
(resolved to its land, -0.3) freeze as before. No mixin errors.

## 7. Issues

- Arid warm river rains. Cause: Pretty Rain (`particlerain`, `renderVanillaWeather`/`tickVanillaWeather` false)
  cancels `LevelRenderer.renderSnowAndRain` and `tickRain` at HEAD, so neither Serene Seasons' renderer redirects nor
  `SeasonHooksClientMixin` run. It draws rain from the biome's declared precipitation alone (`terralith:warm_river`
  declares rain). Gameplay rain (fire, cauldrons) was already right; only particles and sound were wrong, and for
  every biome, not just rivers (deserts never rained only because they declare none; temperate winters drew rain
  instead of snow). Fixed 2026-10-09 by `PrettyRainSeasonMixin` (WE-24).

## 8. In-game test suite

Set the season with `/season set <SUB_SEASON>` (Serene Seasons takes the sub-season names: `EARLY_SPRING` ...
`LATE_WINTER`; it jumps to the start of that sub-season) and force rain with `/weather rain`. Install the current patcherMod jar from
`development/patcherMod/build/libs/` first (client and server).

- [ ] **WE-01 boot:** start the game and join a world → no mixin errors in `logs/latest.log` mentioning `Season` (grep `InvalidInjection\|Season.*Mixin`)
- [ ] **WE-02 desert dry:** desert, rain, any season → no rain drawn, no rain sound, a fire on sand keeps burning
- [ ] **WE-03 badlands dry:** eroded badlands, rain, `/season set spring` (Mid Wet early on) → no rain
- [ ] **WE-04 savanna Mid Wet only (changed 10-09):** savanna, rain: `LATE_WINTER` and `EARLY_SPRING` → rain; `EARLY_WINTER`, `MID_SPRING`, `MID_SUMMER` → no rain
- [ ] **WE-05 jungle:** jungle, rain, summer → rain except in late summer / early autumn (Mid Dry)
- [ ] **WE-06 frozen summer:** snowy plains or siberian taiga, rain, `/season set summer` → snow falls; snow layers stay
- [ ] **WE-07 temperate unchanged:** plains, rain → rain in summer, snow in winter
- [ ] **WE-08 desert river:** river (or warm river) through a desert, rain, winter → no rain or snow over the water
- [ ] **WE-09 jungle river:** river through a jungle, rain, winter → rain, no snow
- [ ] **WE-10 plains river:** river through plains, rain, winter → snow, like the plains around it
- [ ] **WE-11 beach:** beach next to a desert, rain → dry; beach next to plains in winter → snow
- [ ] **WE-12 HUD arid:** SeasonHUD in a desert → Dry icon, "Arid", with day N / year length counting up
- [ ] **WE-13 HUD frozen:** SeasonHUD in snowy plains → Winter icon, "Frozen", day N / year length
- [ ] **WE-14 HUD river:** stand in a river in a desert → "Arid"; in a jungle river → Wet/Dry
- [ ] **WE-15 tint:** badlands grass and savanna-badlands trees keep one colour through `/season set` changes; plains change
- [ ] **WE-16 crops:** wheat on farmland in a desert in winter grows; wheat in snowy plains in summer does not (Serene Seasons fertility)
- [ ] **WE-18 dry grasslands:** arid highlands and hot shrubland (declares rain), rain: `LATE_WINTER` → rain; `MID_SPRING` → none. HUD shows Wet/Dry
- [ ] **WE-19 painted mountains:** rain, any sub-season → nothing falls; HUD "Arid"
- [ ] **WE-20 temperate highlands:** rain, `MID_WINTER` → snow; `MID_SUMMER` → rain. HUD shows Winter/Summer, not Wet/Dry
- [ ] **WE-21 cold ocean:** out at sea in a cold or plain ocean, rain, `MID_WINTER` → snow over the water, but no ice forms along the shore even after several minutes; `MID_SUMMER` → rain
- [ ] **WE-22 warm ocean:** rain, `MID_WINTER` → rain (no snow, no ice); `LATE_SUMMER` → nothing (Mid Dry)
- [ ] **WE-23 frozen ocean:** rain, `MID_SUMMER` → snow; open water between ice sheets freezes over with time
- [ ] **WE-24 Pretty Rain:** warm river through a desert, rain, any season → no rain particles or rain sound over the water (sand dust where the bed is sand is fine); plains, rain, `MID_WINTER` → snow particles, not rain
- [ ] **WE-25 Cool Rain:** Cool Rain (added in 1.0.5) plays its own per-block rain sounds from a HEAD inject in the client rain tick and reads the biome's own precipitation (`Biome.getPrecipitation`), not `SeasonTypes.precipitationAt`; not checked against the season types. Warm river through a desert, rain, any season → no rain sound over the water (if Cool Rain's sounds play there, it needs the same treatment as `PrettyRainSeasonMixin`); plains, rain, `MID_SUMMER` → Cool Rain's per-block sounds (leaves, metal, glass) play
- [ ] **WE-26 skylands summer:** `terralith:skylands_summer` (`/locate biome`), rain, `EARLY_SPRING`, `MID_SPRING` and `MID_WINTER` → rain every time, never snow; HUD "Summer"
- [ ] **WE-27 ocean winter only:** `minecraft:ocean` out at sea, rain: `EARLY_SPRING` and `LATE_AUTUMN` → rain; `EARLY_WINTER` → snow. `minecraft:cold_ocean` (cold): `EARLY_SPRING` and `LATE_AUTUMN` → snow, `MID_SPRING` → rain, no ice forms in any season
- [ ] **WE-28 snow line:** plains or birch forest, rain, `MID_SUMMER`: stand on the highest ground below y320 → rain, not snow; generated snow on high ground (birch taiga) melts over time
- [ ] **WE-29 frozen keeps snow:** frozen peaks or jagged peaks, rain, `MID_SUMMER` → snow at the summit
- [ ] **WE-30 rain height and gauge:** plains, rain, `MID_SUMMER`, fly (creative) up from the ground: rain around you up to y319, snow from y320 to about y360, nothing above. F3 particle count (`P:`) stays at or below the ground-level count
- [ ] **WE-31 snow seasons reload:** move a biome into `snow_seasons` (e.g. `"minecraft:plains": ["mid_winter"]`), `/reload`, rain, `EARLY_WINTER` in plains → rain
- [ ] **WE-32 cold:** taiga, rain: `LATE_AUTUMN`, `MID_WINTER`, `EARLY_SPRING` → snow; `MID_SPRING`, `MID_AUTUMN` → rain (before: snow). Windswept hills `LATE_SPRING` → rain. HUD shows the normal four seasons
- [ ] **WE-33 skylands winter:** `terralith:skylands_winter`, rain, `MID_SUMMER` → snow; HUD shows "Winter" (not "Frozen") with day N of the year; grass keeps its colour across `/season set`; wheat does not grow, a winter crop does
- [ ] **WE-34 skylands spring:** HUD with sub-seasons on: `/season set EARLY_WINTER` → "Early Spring", day 1 of 30 (24 in single-player), rain → snow; `/season set EARLY_SPRING` → "Mid Spring", day 1 of 60 (48), rain → rain; `/season set EARLY_AUTUMN` → "Late Spring", day 1 of 30 (24). Grass tint follows (Early Spring tint in world winter). Spring crops grow in world summer
- [ ] **WE-35 skylands autumn:** `/season set EARLY_SPRING` → "Early Autumn" day 1 of 30 (24 in single-player), rain; `EARLY_SUMMER` → "Mid Autumn" day 1 of 60 (48); `EARLY_WINTER` → "Late Autumn" day 1 of 30 (24), snow. HUD with sub-seasons off: "Autumn", day N of 120 (96)
- [ ] **WE-36 clouds:** overworld, look up from the ground and fly up: Distant Horizons' clouds at y350 (fly through them there; nothing at y520); rain particles reach the cloud layer. Turn vanilla clouds on (Video Settings) → vanilla layer at the same height
- [ ] **WE-17 switch off:** `[seasons] enabled = false` in creativeengineering-common.toml, rejoin → deserts rain again in Mid Wet (Serene Seasons alone)

## 7. History

- 2026-10-08: audit (rain in deserts, snow on warm rivers) and the generated weather_by_biome.md. Season types
  built: arid, frozen, tropical by tag, savanna wet season, adaptive rivers/beaches, SeasonHUD names and day of
  year, tint and fertility. Server side checked on a scratch server; client side untested.
- 2026-10-09: section 2 rewritten as a full reference of Serene Seasons on its own: the tropical split by declaration
  (savannas and deserts both rained in Mid Wet only), the temperate gradient by base temperature, the declaration
  quirks, blacklist, tint/fertility/frequency, summary of effective behaviours. Savanna wet season marked as a design
  choice awaiting confirmation.
- 2026-10-09: user decisions. New type **dry** (every savanna, shrublands, brushland, arid_highlands,
  savanna_badlands): rain in Mid Wet only, Serene Seasons' own savanna behaviour; the three-wet-season rule is gone.
  New type **temperate** (seasonal offset even above 0.8): temperate_highlands, ocean, cold ocean. painted_mountains
  arid. Oceans tagged (frozen -> frozen, warm/lukewarm -> tropical) and tagged biomes now override the blacklist.
  Rain frequency, mountain neighbours and generation snow written up in section 4.1-4.3. Scratch probe 6.3. Then: rocky_mountains frozen, stony/volcanic peaks and crater tropical; jar installed in mods/.
  Then: cold and plain oceans no longer ice over (snow only); frozen oceans still freeze. Probe in 6.3, jar reinstalled.
- 2026-10-09: "arid warm river rains" traced to Pretty Rain replacing the vanilla weather renderer (section 7).
  `PrettyRainSeasonMixin` + `SeasonTypes.precipitationAt`; jar built, not installed.
- 2026-10-11: ships in pack 1.0.5 (staged, not deployed): patcherMod 1.0.5 in `mods/` holds the season types and
  `PrettyRainSeasonMixin`; Pretty Rain and Cool Rain are new mods in the same payload. Cool Rain's rain sounds come
  from its own client mixin that reads the biome's precipitation directly, so they may ignore the season types
  (WE-25 added, not fixed).
- 2026-10-11 (later): snow line and snow seasons (3.4). `SeasonTypes.applySnowRules` from `SeasonHooksTypesMixin`,
  CEConfig `[seasons] snowLineMinY` (500), `season_types.json` `snow_seasons` -> tags `creativeengineering:snow/no_*`
  (seasontags.js, checked in the pack's Rhino). ocean and deep_ocean snow in winter only, skylands_summer never.
  Generator models both and lists the rest in weather_by_biome.md 2.8. Pretty Rain `cloudHeight` 191 -> 500.
  patcherMod 1.0.6 built and installed in `mods/` (1.0.5 moved to jar_archive/displaced); not run in game
  (WE-26..31).
- 2026-10-11 (later still): user's picks from the 2.8 list. New type **cold** (21 biomes: snow Late Autumn to Early
  Spring only, `seasonTemperature` cap/floor); the rest of the list winter only via `snow_seasons`; skylands_summer
  out of `snow_seasons`. **Permanent seasons** for the four skylands (HUD, tint, crops, calendar 1/4-1/2-1/4 with the
  snowy quarter on the world's winter). Snow line `snowLineMinY` 500 -> `snowLineY` 320 with the build-height gauge.
  Pretty Rain `cloudHeight` 500 -> 350, Rubidium Extra `cloud_height` 192 -> 350; found the visible clouds are
  Distant Horizons' at y520 (3.4). seasontags.js checked in the pack's Rhino; jar rebuilt and reinstalled (md5
  09a5f9d4), not run in game (WE-26..36).
- 2026-10-11 (end): forested_highlands moved from snow_seasons winter-only to cold (22). `DhCloudHeightMixin`: Distant
  Horizons' clouds follow the vanilla cloud height (350) instead of build height + 200. Jar reinstalled (md5 233d304b),
  not run in game.
- 2026-10-14: ships in pack **1.0.6** (STAGED 2026-10-14, update from 1.0.4; 1.0.5 was never deployed): the season types
  and Pretty Rain mixin from 1.0.5 plus the 2026-10-11 snow line, snow seasons, cold type, permanent skylands seasons
  and `DhCloudHeightMixin`, all in patcherMod 1.0.6. Not run in game (WE-26 to WE-36). [plan_1.0.6.md](plan_1.0.6.md).
