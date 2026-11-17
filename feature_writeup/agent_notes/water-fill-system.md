# water-fill-system

> Water fill = earth fill reversed for opt-in water structures (cap water into dry ground / air); CEConfig [worldgen.water_fill]; status in feature_writeup/structure_tuning.md

Moved from agent memory 2026-11-16 (last edited there 2026-10-09). Repo status docs win over any status line here.

Built 2026-10-08 on request ("extend earth fill to water structures, cap blocks/air replaced with water"). Writeup: feature_writeup/structure_tuning.md 3.14, tests ST-56..60.

- Lives inside `worldgen/EarthFill.filter` (same mixin, same predicted terrain cache, earth check first). Template water = fluid type `Fluids.WATER` (water, waterlogged, kelp, seagrass), ALL layers, cached in LOCAL_WATER.
- Terrain Kind gained SEABED (solid whose column top non-air block is water, `topIsWater`); earth fill treats it as solid, water fill ignores it. Water into SOLID (dry land) -> maxIntoGround 32, into AIR -> maxIntoAir 16.
- **Opt-in** tag `creativeengineering:water_fill/structures` (kubejs worldgen/water_fill.js), because jungle temples/strongholds/ancient city/village farms carry water on purpose. `water_fill/ponds` (aquacultures x3, swamp fisherman house) gets default override `#...ponds=-1,16`.
- Used `Fluids.WATER` not `FluidTags.WATER`: fluid tags aren't bound headless (tests failed), and it's stricter.
- Jigsaw pieces sit 1 below start_height (groundLevelDelta), so T&T ocean outpost water plate is y 57-62, flush with the sea.
- Fixed in passing: EarthFillTest expected maxOverAir 256 but code default is 128 (toml still says 256).
- Not done: no game/scratch server run, jar only in patcherMod/build/libs (1.0.5), defaults are a guess -> ST-57 log=ALL calibration.

Related: [earth-fill-system](earth-fill-system.md), [water-biome-rules-ipo-driftwood](water-biome-rules-ipo-driftwood.md), memory `scratch-server-verify`.
