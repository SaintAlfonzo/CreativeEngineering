# wild-flowers-system

> Wild flower tuning: wild_flowers.json (Botania palettes per biome, pack flower patches, existing-feature scale/remove, Flower Patch share/sizes) applied by worldgen/wild_flowers.js; flowers.md (FL-); patcherMod switch source only

Moved from agent memory 2026-11-16 (last edited there 2026-10-08). Repo status docs win over any status line here.

2026-10-07, user asked for flower spawn tuning (Botania colors matched to biomes, fill bare biomes, realistic flowers),
then mid-build: Flower Patch spawning must also be configurable by density/frequency.

- Config `kubejs/generation_scripts/worldgen/wild_flowers.json`; traits come from wild_crops.json `biomes` (shared
  table). Runtime `kubejs/server_scripts/worldgen/wild_flowers.js` (highPriorityData: generates configured/placed
  features + modifiers + forge:none; tags via global.tags). About 100 Botania palette+tier features, 378 files.
- Existing Terralith flower features are rewritten from a generated snapshot `flower_features.json`
  (`tools/gen_wild_flowers.py`, /cdevgen id `wild_flowers`); KubeJS can't read jars. Regenerate after mod updates or
  gen_soil_compat.py (the snapshot embeds soil-compat overrides); the test fails when stale.
- Flower Patch: weights per provider type; dual_noise_provider (meadow, orchid swamp) can't be converted.
- Buttercup deliberately absent (More Mooblooms removes it pack-wide); clovers taken over from Buzzier Bees.
- Writeup feature_writeup/flowers.md (FL-01..14), generated report flowers_by_biome.md; test tools/test_wild_flowers.sh
  (Rhino run == generator, incl. byte-equal Flower Patch rewrites).
- CEConfig `[worldgen.systems] wildFlowers` added to patcherMod source, jar NOT rebuilt; script treats missing method as on.

**How to apply:** tune in wild_flowers.json, rerun the generator and read flowers_by_biome.md sections 1-3 (keep all 16
Botania colors findable). Related: [vegetation-worldgen-system](vegetation-worldgen-system.md), [iw-soil-wild-plants](iw-soil-wild-plants.md), [moobloom-variants](moobloom-variants.md),
memory `scratch-server-verify`, memory `kubejs-headless-rhino-harness`.
