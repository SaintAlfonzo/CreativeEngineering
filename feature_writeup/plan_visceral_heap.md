# Visceral Heap plan (2026-11-09)

Status: BUILT 2026-11-09, never booted; see [visceral_heap.md](visceral_heap.md), whose section 3 lists where the build differs
from this plan (stub loader, tendon height, music, tuner skip; shares and creative tab per owner 2026-11-09). Facts below were read from the bytecode of
`DecompiledMods/BiomesOPlenty-1.19.2-17.1.2.544` and the Forge 43.3.9 jar; none has been tested in game.

Goal: the Biomes O' Plenty (BOP) Visceral Heap generates in the Nether from the real, unmodified BOP jar. No other BOP
block, item, biome, feature or recipe exists in the game. TerraBlender is not installed.

## 1. Decisions

| Topic | Decision | Why |
|---|---|---|
| Jar | Unmodified `BiomesOPlenty-1.19.2-17.1.2.544.jar` in `mods/` | A donor jar was ruled out by the owner |
| TerraBlender | Empty stub mod with id `terrablender`, nested in Creative Worldgen as a member | BOP declares it mandatory (`[2.0.1.128,)`); no other pack mod needs it |
| Registries | One filter on `DeferredRegister`, driven by an allowlist | All 15 BOP registers go through it, and filtered suppliers never run |
| Resources | Allowlist pack wrapped around BOP's mod pack | BOP's tag files name removed entries, and a missing entry makes vanilla drop the whole tag |
| Placement | A sixth entry in the Nether baseline of the biome tuner | `BiomeLayoutTuner` writes the whole Nether list to the top-priority pack, so a hand-written `the_nether.json` would lose |
| Surface | Full `noise_settings/nether.json` with the Heap rule spliced in | Vanilla builds these rules before mod blocks exist, so a mixin on `SurfaceRuleData` cannot name flesh |

Rejected: real TerraBlender (global biome source mixins, untested against the pack's locate and structure mixins);
adding the rose quartz buds to the allowlist (section 3, row 8 avoids it).

Accepted difference from BOP: BOP puts the Heap in Warped Forest's exact climate slot inside its own TerraBlender regions.
Without regions the Heap needs its own climate point, so large-scale layout differs. Biome contents and surface rules match.

Intended difference from BOP (owner, 2026-11-09): BOP silently drops the part of a floor-to-ceiling tendon outside the chunk
write window, leaving a strand that ends abruptly, and lets tendons cross into neighbouring biomes. Here the tendon's end
point is pulled back to the window edge or the biome edge, whichever comes first (section 3, row 9).

## 2. Stub member (`development/creativeWorldgen/members/terrablenderstub`)

- Same shape as `members/babymobs`: `build.gradle` with `ce.forge-mod`, `gradle.properties`, `LICENSE.txt`.
- Properties: `mod_id=terrablender`, `mod_version=2.0.1.128`, `jar_name=terrablenderStub`,
  `mod_group_id=com.SaintAlfonzo.terrablenderstub`, `bundled_in=:creativeWorldgen`, GPL-3.0-only,
  display name `TerraBlender stub (Creative Worldgen)`.
- Use a plain version with no qualifier. It is the lowest version BOP accepts, so a real TerraBlender should win if one
  is ever present (test VH-11).
- `mods.toml` with `modLoader="lowcodefml"` and no classes, so no code runs. If Forge rejects that, use `javafml` with one
  empty `@Mod("terrablender")` class.
- Register it in `settings.gradle` (the `moreMooblooms`/`babyMobs` map) and in `bundle_members` of creativeWorldgen.
- No `mod_sides.md` row of its own, matching the other members.

## 3. Containment mixins (creativeWorldgen)

New package `mixin/visceralheap/`, plus `visceralheap/HeapAllowlist.java`. Gate every BOP-targeting mixin on
`biomesoplenty` through `WorldgenMixinPlugin`, and pin the BOP version with its `modRange`, because row 8 names a synthetic
lambda. Add the BOP jar to `development/creativeCommon/libs` to compile.

| # | Target | Change | Reason |
|---|---|---|---|
| 1 | `DeferredRegister.addEntries` (HEAD) | When `modid` is `biomesoplenty`, remove entries not in `HeapAllowlist` | Single gate for all registries. Match on registry and name, since names such as `hair` repeat across registries |
| 2 | `ResourcePackLoader.createPackForMod` (RETURN) | For BOP, return `HeapPackResources` (section 5) | Hides everything else in the jar |
| 3 | `ModBiomes.setupTerraBlender` | `@Overwrite` to empty | Removes every TerraBlender reference. Cancelling is not enough, the bytecode verifier would still load the region classes |
| 4 | `ModVanillaCompat.setup` | Cancel | 255 eager lookups. First list any Heap lines and re-add them through pack helpers |
| 5 | `ColorHandler.registerItemColors`, `registerBlockColors` | Cancel | 22 eager lookups. Confirm no Heap block is tinted |
| 6 | `EntityRendererHandler.onRegisterRenderers` | Cancel | 3 eager lookups, for entities that are filtered out |
| 7 | `ParticleFactoryHandler.registerParticleFactories` | `@Overwrite`, registering the 4 kept particles | The 5th, glowworm, is filtered out and would throw. The mixin must repeat `@SubscribeEvent`: `@Overwrite` drops the target's annotations (without it the client crashed at load with `Redundant texture list for particle biomesoplenty:dripping_blood`, fixed 2026-11-09) |
| 8 | `FleshTendonFeature.lambda$new$0` | `@Redirect` `RegistryObject.get`: if not present, return `Blocks.AIR` | Its placement predicate reads 4 rose quartz bud blocks at worldgen time and would throw |
| 9 | `FleshTendonFeature.m_142674_` | `@Redirect` the `BlockPos` offset call (`m_7918_`) that picks the end point and pull that point back. `@Inject` before the first `quadratic` call to cancel when the pull leaves no usable end point | Owner decision: tendons stop at the chunk write window or the biome edge, whichever comes first, instead of being chopped or crossing into another biome |

`BOPSurfaceRuleData` is left unused: its static init looks up removed blocks, and only row 3's method reached it.

Row 9 details:

- The curve's control point is directly below the end point, so from above a tendon is a straight line from start to end.
  Keeping that line inside both limits keeps the whole strand inside them, and the end point moves before BOP's ceiling
  search, so the strand still attaches to the ceiling at its new column.
- The redirect walks that line outward from the start, one block at a time, at the start's height. It stops at the first
  column that fails either test, and the last column that passed becomes the end point.
  - Window test: BOP's private `respectsCutoff` (`@Shadow`) at the column and at X and Z plus or minus 1, including diagonals.
    Flesh balls reach 1 block sideways, so this margin keeps a ball at the edge whole. Tendon columns hang straight down
    and need no margin.
  - Biome test: `getBiome` at the column is `biomesoplenty:visceral_heap`.
- The window alone never leaves less than 15 blocks of sideways reach, because the start is always in the center chunk. A
  tendon pulled only by the window is never cancelled.
- A biome edge close to the start can leave a very short tendon. BOP's minimum is 8 blocks sideways, so if the pulled end
  point is under 8 blocks from the start on both X and Z, the redirect returns the start position and the inject returns
  `false` before any block is written. BOP's own code can never produce an end point in the start's column, so that value is
  unambiguous.
- The walk draws no random numbers. BOP's existing rule still applies: a tendon whose end column is solid at the start's
  height places nothing.
- If the level is not a `WorldGenRegion`, return BOP's end point unchanged.
- Scope is `flesh_tendon` only. `hanging_flesh_tendons` are single columns with no cutoff check.

## 4. Registry allowlist

| Registry | Names |
|---|---|
| biome | `visceral_heap` |
| block | `blood`, `flesh`, `porous_flesh`, `pus_bubble`, `flesh_tendons`, `flesh_tendons_strand`, `eyebulb`, `hair` |
| item | The block items of the above that BOP registers, and `blood_bucket` |
| fluid | `blood`, `flowing_blood` |
| fluid type | `blood` |
| feature | `bone_spine`, `flesh_tendon`, `hanging_flesh_tendon` |
| configured feature | `blood_lake`, `blood_spring`, `eyebulb`, `flesh_tendon`, `hair`, `hanging_flesh_tendon`, `nether_bone_spine`, `porous_flesh`, `pus_bubbles` |
| placed feature | As above, with `hanging_flesh_tendons` (plural) |
| particle | `dripping_blood`, `falling_blood`, `landing_blood`, `pus` |
| sound event | `music.nether.visceral_heap` |

The biome's remaining features and its carver are vanilla.

## 5. Resource allowlist (`HeapPackResources extends PathPackResources`)

Override `hasResource`, `getResource`, `getResources` and `getNamespaces` to serve only:

- `data/biomesoplenty/loot_tables/blocks/` for the 7 solid blocks
- `data/biomesoplenty/tags/blocks/flesh.json` and `data/biomesoplenty/tags/fluids/blood.json` (both name only kept entries)
- `assets/biomesoplenty/` blockstates, block and item models and textures for the kept blocks, the blood fluid textures,
  the 4 particle files and their textures, `lang/`, `sounds.json`, and the Heap music file

All other namespaces in the jar are dropped: `minecraft`, `forge`, `sereneseasons`, `toughasnails`. Kept files stay in the
jar under BOP's own ids; nothing is copied out.

## 6. Pack data

Put files that name BOP ids in `development/creativeWorldgen/src/main/resources/packs/biomesoplenty/`, so
`compat/ModDataPacks` loads them only when BOP is present.

1. **Biome tags** (`replace: false`, Heap only): `minecraft:is_nether`, `minecraft:has_structure/bastion_remnant`,
   `forge:is_hot/nether`, `forge:is_dry/nether`.
2. **Axe tag**: `flesh`, `flesh_tendons`, `flesh_tendons_strand`, `eyebulb` through `global.tags`, with ids in the dicts.
   These are the only mineable entries BOP gives the kept blocks.
3. **Surface rule**: new generator `tools/gen_visceral_heap.py`, listed in `tools/generators.json`. Take vanilla's
   `noise_settings/nether.json` from the data generator reports of `server-1.19.2.jar`, then insert at index 3 of the
   top-level sequence (after the two bedrock rules and the `belowTop(5)` netherrack rule, where BOP inserts its rules):

   ```json
   {"type": "minecraft:condition",
    "if_true": {"type": "minecraft:biome", "biome_is": ["biomesoplenty:visceral_heap"]},
    "then_run": {"type": "minecraft:sequence", "sequence": [
      {"type": "minecraft:condition",
       "if_true": {"type": "minecraft:stone_depth", "offset": 0, "add_surface_depth": true, "secondary_depth_range": 0, "surface_type": "ceiling"},
       "then_run": {"type": "minecraft:block", "result_state": {"Name": "biomesoplenty:flesh"}}},
      {"type": "minecraft:condition",
       "if_true": {"type": "minecraft:stone_depth", "offset": 0, "add_surface_depth": true, "secondary_depth_range": 0, "surface_type": "floor"},
       "then_run": {"type": "minecraft:block", "result_state": {"Name": "biomesoplenty:flesh"}}}
    ]}}
   ```

   No jar in `mods/` and nothing in `kubejs/data` overrides this file today.
4. **Placement**:
   - Add `biomesoplenty:visceral_heap` to `VANILLA_NETHER` in `tools/gen_biome_config.py`: temperature 0.45, humidity 0.5,
     other axes 0, offset 0.2. On the existing 100,000-point Nether sample this point alone gives the Heap about 5.8 %,
     between Crimson and Warped.
   - Run the generator, then set `target_pct` on the Heap in `biome_config.json`. Owner set 5 on 2026-11-09, with all five Nether targets (visceral_heap.md section 4).
   - The other Nether multipliers are measured against shares that now include the Heap. Compare `result_pct` before and
     after, and adjust if needed.
   - Change `BiomeLayoutTuner` to skip entries whose biome is not registered. The baseline ships in the Worldgen jar, so
     without this a Worldgen jar running without BOP would fail to load worlds.

## 7. Order of work

1. With the game closed, run the `add-mod` steps: jar in `mods/`, `mod_sides.md` row (both sides), jar in
   `development/creativeCommon/libs`.
2. Build the stub member and sections 3 to 5. Confirm a boot reaches the main menu with only allowlisted BOP entries.
3. Add section 6 and run the generators.
4. In game: `/dumpall`, `/cdevgen run all dumps`, then `/dumpclimate` so the sample includes the new layout.
5. Rerun the readers of biome lists: `gen_spawn_table.py`, `gen_weather_table.py`, `gen_structure_groups.py`,
   `structure_biome_coverage.py`. Run the `integration-report` skill for the kept blocks.
6. Add rows VH-01 onward to `testing/scratch_queue.md`.
7. Write `feature_writeup/visceral_heap.md`, add the TODO and README rows, add a changelog bullet to `unreleased.md`, and run
   `python3 tools/changelog_lint.py`.

## 8. Test list (for `scratch_queue.md`)

- **VH-01 boot:** server and client start; `latest.log` has no mixin errors, no `Couldn't load tag` naming `biomesoplenty`.
- **VH-02 registries:** the `biomesoplenty` namespace holds exactly the section 4 entries.
- **VH-03 no TerraBlender:** the mod list shows the stub; no TerraBlender classes or BOP region classes are loaded.
- **VH-04 locate:** `/locate biome biomesoplenty:visceral_heap` in a new world finds one.
- **VH-05 share:** after `/dumpclimate`, the Heap's `result_pct` is near `target_pct`.
- **VH-06 surface:** Heap floor and ceiling are flesh; bedrock layers are intact at top and bottom.
- **VH-07 features:** flesh tendons, bone spines, blood lakes, eyebulbs and hair generate over many chunks with no worldgen exception.
- **VH-08 client:** blocks have textures; blood renders with particles; the Heap music plays.
- **VH-09 data:** vanilla `is_nether` still holds the 5 vanilla biomes; flesh drops itself; axe mines it at tool speed.
- **VH-10 Nether structures:** fortresses and bastions place in the Heap; Blueprint's `ModdedBiomeSource` wrapper shows no errors.
- **VH-11 real TerraBlender present:** with a real jar beside the stub, record which loads and whether the boot fails.
- **VH-12 existing save:** load a pre-change world and enter new Nether chunks; record whether the Heap appears (section 9).
- **VH-13 Worldgen without BOP:** Creative Worldgen alone loads a world with vanilla's five Nether biomes.
- **VH-14 tendon pull:** fly a generated Heap at chunk borders and along its biome edges; every `flesh_tendon` strand runs unbroken from floor to ceiling, flesh balls are whole, no strand crosses into another biome, and density looks like the middle of the biome away from the edges.

## 9. Open questions

1. **Existing saves.** The `biome_config.json` readme says a world keeps the layout stored at creation. The comment in
   `RegistryLoaderPresetOverworldMixin` says data pack dimensions replace the stored one in loaded worlds too. VH-12 settles
   which is true for the Nether. Generated chunks stay as they are in either case.
2. **Standalone Creative Worldgen.** The stub nests in every copy of the jar. If Worldgen is ever published alone, it would
   supply a fake `terrablender` to any modpack that installs it. VH-11 shows whether a real TerraBlender overrides it. If it
   does not, the stub has to move to a separate jar shipped only with this pack.
3. **Heap share.** 6 % is a starting value. BOP's own share depended on region weights (13 and 2) that have no equivalent here.
4. **Biome test height.** Row 9 checks the biome at the tendon's start height. If Nether biomes turn out to vary with height
   in this pack, a strand could still enter another biome higher up. VH-14 covers this.
