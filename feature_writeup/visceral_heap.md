# Visceral Heap (Biomes O' Plenty, one biome)

Test prefix: `VH-` (scratch rows `SQ-VH-`, `CQ-VH-`). TODO: `worldgen/biomes #10`. Plan and decisions:
[plan_visceral_heap.md](plan_visceral_heap.md). Bundle: [creative_worldgen.md](creative_worldgen.md).

Status: BUILT and installed 2026-11-09. Never booted; every live check is queued.

## 1. What it is

The Biomes O' Plenty Visceral Heap generates in the Nether from the unmodified BOP jar. No other BOP block, item, biome,
feature or recipe exists. TerraBlender is not installed; an empty mod with its id satisfies BOP's dependency.

## 2. Parts

| Part | Where |
|---|---|
| BOP jar | `mods/`, and `development/creativeCommon/libs` to compile |
| TerraBlender stub | Worldgen member `members/terrablenderstub`, id `terrablender`, version 2.0.1.128 |
| Registry and file allowlists | `visceralheap/HeapAllowlist`, `visceralheap/HeapPackResources` |
| Containment mixins | `mixin/visceralheap/`, gated on BOP 17.1.2.544 exactly in `WorldgenMixinPlugin` |
| Biome and worldgen tags, surface rule | `packs/biomesoplenty/`, on only while BOP is loaded |
| Surface rule generator | `tools/gen_visceral_heap.py`, input `vanilla_nether_noise_settings.json` |
| Placement | Sixth entry of `VANILLA_NETHER` in `tools/gen_biome_config.py`, ``target_pct` per Nether biome in `biome_config.json` (section 4) |
| Axe and climbable tags | `kubejs/data/creativeworldgen/kubejs/tags/visceral_heap.js`, ids in `block_dict.js` |
| Recipes (crush, blood, cultivation) | `kubejs/data/creativeworldgen/kubejs/recipes/visceral_heap.js`; ids in `item_dict.js` / `fluid_dict.js`; result pools from Creative Engineering (`fan/ResultPools`, [engineering.md](engineering.md)) |

A newer BOP jar fails the version gate, so the mixins are skipped with one warning and BOP then needs real TerraBlender. Bump
the range in `WorldgenMixinPlugin` only after rechecking the targets, row 9 above all.

## 3. Differences from the plan, found while building

- **Stub loader.** The plan preferred `lowcodefml`. The stub uses `javafml` with an empty `@Mod` class, the plan's fallback,
  because the language provider's version check could not be confirmed without a boot.
- **Creative tab (dropped 2026-11-09).** The pack does not use BOP's tab, so no mixin touches its icon.
- **Tendon height.** BOP's end point is `start.offset(dx, start.getY(), dz)`, so its Y is twice the start's, and a solid end
  column does not stop the tendon. The redirect keeps BOP's Y argument and moves only X and Z. The sideways walk is unchanged.
- **Music.** The Heap's sound event has five tracks, not one; all five stay visible. `sounds.json` is served with only that
  event, so BOP's other sounds log no missing-file warnings.
- **Tuner skip.** "Skip biomes that are not registered" would remove every data pack biome (Terralith's are in no static
  registry at that point). `BiomeLayoutTuner` skips only `OPTIONAL_NAMESPACES` (`biomesoplenty`) while that mod is absent, and
  the layout cache key includes the absent list.

## 4. Nether shares

Owner targets 2026-11-09, set as `target_pct` in `biome_config.json`: Heap 5 %, Nether Wastes 30 %, Soul Sand Valley 20 %,
Crimson Forest 15 %, Warped Forest 15 %, Basalt Deltas 15 %. NOT yet solved: the targets were written after the offline
solve below, which used the old multipliers (Heap 6 %, Wastes 47.1 %, Soul Sand 18.1 %, Crimson 14.0 %, Basalt 10.6 %,
Warped 4.3 %). The jar installed 23:14 has the tab mixin removed and these targets. Wastes at 30 % and Warped at 15 % may
hit the solver's penalty range (offset 0.6) and note as unreachable; SQ-VH-05 records the real result.

## 5. Tests

| ID | Check | Row |
|---|---|---|
| VH-01 | Server boot: no mixin errors or bad `biomesoplenty` tags | SQ-VH-01 |
| VH-01c | Client boot to the main menu | CQ-VH-01 |
| VH-02 | Registries hold exactly the allowlist | SQ-VH-02 |
| VH-03 | Stub loaded, no TerraBlender or region classes | SQ-VH-03 |
| VH-04 | `/locate biome` finds the Heap | SQ-VH-04 |
| VH-05 | `/dumpclimate` share near the target | SQ-VH-05 |
| VH-06 | Flesh floor and ceiling, bedrock intact | SQ-VH-06 |
| VH-07 | Features generate over many chunks, no exception | SQ-VH-07 |
| VH-08 | Textures, blood, particles, music | CQ-VH-02 |
| VH-09 | Vanilla `is_nether` intact, flesh drops, axe speed | SQ-VH-08 |
| VH-10 | Fortresses and bastions place; Blueprint wrapper clean | SQ-VH-09 |
| VH-11 | Real TerraBlender beside the stub | SQ-VH-10 |
| VH-12 | Existing save: does the Heap appear in new chunks | SQ-VH-11 |
| VH-13 | Worldgen without BOP | SQ-VH-12 |
| VH-14 | Tendons whole, inside the Heap | CQ-VH-03 |
| VH-15 | Flesh walk speeds, porous flesh honey jump ([blockrunner.md](blockrunner.md) BR-57) | CQ-VH-04 |
| VH-16 | Tendons and strand climbable, strand standable | CQ-FLR-01 |
| VH-17 | Flesh and porous flesh crush totals per pool (EN-22) | SQ-FLR-02, -03 |
| VH-18 | JEI crushing and milling pool slots (EN-23) | CQ-FLR-02 |
| VH-19 | Pus bubble 25 % slimeball, hair 50 % string | SQ-FLR-04 |
| VH-20 | Blood mixes and both cultivation recipes, all three oils | SQ-FLR-05 |
| VH-21 | Eyebulb drop places | CQ-FLR-03 |

## 6. Still to do

Needs the game: `/dumpall`, `/cdevgen run all dumps`, `/dumpclimate` (plan section 7 step 4), then the biome list readers
`gen_spawn_table.py`, `gen_weather_table.py`, `gen_structure_groups.py`, `structure_biome_coverage.py` and the
`integration-report` skill for the kept blocks (step 5).

Walk speed (2026-11-10, owner): flesh is in BlockRunner's spongy tier (0.60), porous flesh in very sticky (0.45) with
honey's jump factor 0.5 instead of slime's feel ([blockrunner.md](blockrunner.md) 3.5). 

Recipes (2026-11-10, [plan_flesh_recipes.md](plan_flesh_recipes.md), TODO `worldgen/biomes #11`): BUILT, jars NOT
installed (game open). Crushing wheels and millstone turn flesh and porous flesh into drops from four pools (meat, bone,
eye, misc; plan section 3) through Create result pools; pus bubble gives a slimeball at 25 %, hair string at 50 %.
Heated mixing renders flesh (250 mB blood, bone at 25 %), porous flesh (500 mB) and tendons (250 mB). Cultivation:
2 porous + 2 flesh + 1000 mB of any `#forge:plantoil` gives 2 porous + 3 flesh; 2 tendons + 1 flesh + 250 mB blood gives
1 porous flesh. Tendons and the strand are climbable (not villager ladders). Item physics for the Heap items stays
deferred (plan section 6).
