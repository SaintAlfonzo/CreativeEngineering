# Mooblooms: Buzzier Bees reference and the More Mooblooms mod

Test prefix `MB-`. Sections 1-3 describe Buzzier Bees' moobloom, read from Buzzier Bees 5.0.1 decompiled with
`python3 tools/decompile_mod.py mods/buzzier_bees-1.19.2-5.0.1.jar <out>`. Section 4 is **More Mooblooms 1.0**
(`more_mooblooms`), this pack's own mod, which replaces it.

## 0. At a glance

| Part | Status |
|---|---|
| More Mooblooms 1.0 | `development/moreMooblooms`, jar `moreMooblooms-1.1.0.jar` in `mods/`; 1.1.0 (JEI, section 5) built 2026-10-22, boot check scratch server PASSED 2026-10-22 (SQ-GM-09), DEPLOYED with 1.1.01 (2026-10-22); the 2026-10-27 build (sunflower fix, MB-105) is in `mods/` and went to the client with 1.1.04 (2026-10-29) (rebuilt and installed in `mods/` 2026-10-11 with azaleabloom's moss spawning; DEPLOYED with pack 1.0.6 (2026-10-14), its first release). Install with `./gradlew :moreMooblooms:installToMods`, on the client and the server (same jar on both, 4.2; the deploy ships it to both). Verified on a scratch server (4.14). Rendering, JEI, feeding, grazing and the soul sand valley spawn still need a client (section 8) |
| Variants | 11 (4.4): moobloom (the default), sunbloom, rosebloom, peonybloom, onionbloom, bluebloom, orchidbloom, lilybloom, oxeyebloom, azaleabloom, witherbloom. Each grows a weighted pool of flowers; every vanilla flower is in at least one pool |
| Variant data | generated from the variant table (4.4) by `python3 tools/gen_moobloom_variants.py`; checked by `python3 tools/check_moobloom_data.py` |
| Breeding | any two mooblooms breed: 10 colour-mix combinations, otherwise same variant breeds true, otherwise a random calf (4.8) |
| Spawning | the pack's rules follow the wild flowers (2.8): 25-100% of cows become mooblooms in 18 flower biomes (set per biome), 25% in 5 flowery ones, 2% in 34 with some flowers, each biome's variants matching its flowers; `rareChance` 0.002 applies to every other cow; azaleablooms on moss in lush caves. The mod's own: rare witherblooms in soul sand valleys (4.9) |
| Buzzier Bees | its moobloom, buttercup and Sunny are removed from outside, one config switch each (4.11). BB is under the Abnormals License 1.0: nothing of BB's ships in our jar (4.0) |
| Pack side | per-biome spawn rules, cows in meadow, blooming plateau and skylands spring, `rareChance`, syringe line, `i.more_moobloom_spawn_egg`, JEED hides Sunny, Quark restock ignores the egg (4.12) |
| JEI | one egg per variant with a short info page, "Moobloom Breeding" (combinations with the miss, breeds true, random calf, each parent's food), "Moobloom Shearing" (drops per shear, the moobloom rendered with its flowers) and "Moobloom Conversion" (cow + the variant's food, feed chance, shared-food share). 1.1.0 built; the Conversion page is only in the rebuilt jar, not yet in `mods/`, not deployed (section 5) |

**Files to open first:** in `development/moreMooblooms/src/main/java/com/SaintAlfonzo/more_mooblooms/`:
`entity/Moobloom.java` (all the entity logic), `variant/MoobloomVariant.java` (the data format),
`variant/MoobloomBreeding.java`, `spawn/MoobloomSpawns.java`. For Buzzier Bees: the decompiled
`common/entity/animal/Moobloom.java`, `core/other/BBEvents.java` and `core/mixin/MobEntityMixin.java`.

**Knobs:** `config/more_mooblooms-common.toml` (4.2), the variant table (4.4), and the rule files in
`kubejs/data/more_mooblooms/moobloom_spawns/` (4.9).

**How to measure:** `/summon more_mooblooms:moobloom ~ ~ ~ {Variant:"more_mooblooms:rosebloom"}`, then
`/data get entity @e[type=more_mooblooms:moobloom,limit=1,sort=nearest] Flowers`. Shear it and compare the drops
with those three flowers.

## 1. Overview

The moobloom is Buzzier Bees' yellow cow with buttercups growing on it. The code is tiny: `Moobloom extends Cow
implements Shearable, IForgeShearable` and overrides five things. Everything else (milking, breeding, AI,
sounds, size, health) is the vanilla cow's.

The interesting part is the **Sunny** aura. Every second the moobloom gives a hidden Sunny effect to every living
thing near it. Sunny makes undead burn as if in daylight, even at night or underground. Buzzier Bees also makes
zombies, zombie villagers, strays and drowned hunt mooblooms. So a moobloom works as zombie bait that sets its
attackers on fire.

There is exactly one moobloom: the flower (buttercup), the texture and the effect (Sunny) are all hardcoded.
There is no variant field, no NBT and no data file. That is why More Mooblooms has its own moobloom (section 4).

## 2. Reference

### 2.1 Entity

| Property | Value | Where |
|---|---|---|
| Id | `buzzier_bees:moobloom`, `MobCategory.CREATURE`, 0.9 x 1.4 (cow size) | `BBEntityTypes` |
| Attributes | `Cow.createAttributes()`: 10 HP, speed 0.2 | `BBEntityTypes.onEntityAttributes` |
| Spawn rule | on ground, `MOTION_BLOCKING_NO_LEAVES`, `Animal::checkAnimalSpawnRules` (grass below, light) | `BBEntityTypes.registerEntitySpawns` |
| Breeding | wheat (cow food). Offspring always `moobloom`. Cannot breed with a cow (vanilla `canMate` needs the same class) | `getBreedOffspring` |
| Milking | bucket gives milk (inherited `Cow.mobInteract`) | vanilla |
| Model / texture | vanilla `CowModel` on its own layer `buzzier_bees:moobloom#main`; texture `buzzier_bees:textures/entity/cow/moobloom.png` | `MoobloomRenderer`, `BBModelLayers` |
| Flowers on its back | 3 buttercup block models (2 on the back, 1 on the head), adults only, hidden when invisible | `MoobloomFlowerLayer` |
| Spawn egg | `buzzier_bees:moobloom_spawn_egg`, colours `#DBA336` / `#DCDADC`; `i.moobloom_spawn_egg` | `BBItems` |
| Lang | `entity.buzzier_bees.moobloom` = "Moobloom", `effect.buzzier_bees.sunny` = "Sunny" | `en_us.json` |

### 2.2 Shearing

- Adult only (`readyForShearing`: alive and not a baby).
- Player shears go through Forge's `IForgeShearable` path (`onSheared`). Dispensers go through vanilla
  `Shearable.shear`. A Create deployer holding shears acts as a player, so it takes the Forge path.
- Result: shear sound, an explosion particle, the moobloom **turns into a plain `minecraft:cow`**
  (`convertTo(EntityType.COW, false)`, so name, health and age carry over), and 3 buttercups drop.
  Fortune is ignored.
- There is no way back: nothing turns a cow into a moobloom (no lightning or feeding conversion like the mooshroom).

### 2.3 Sunny aura and the effect

This is Buzzier Bees on its own; with More Mooblooms installed it is out of play (O3, O12, 4.11).

| What | Detail |
|---|---|
| Aura tick | server side, every 20 game ticks (`gameTime % 20 == 0`, so every moobloom fires on the same tick) |
| Range | its bounding box inflated by 7 horizontally and 3 vertically: roughly 7.5 blocks out from its centre, 3 blocks above and below |
| Targets | every `LivingEntity` in range except mooblooms (`instanceof Moobloom`): players, animals, monsters, armor stands |
| Effect | `buzzier_bees:sunny` amplifier 0, 100 ticks (5 s), ambient false, particles off, so no HUD icon either. Refreshed every second, so it lasts up to 5 s after leaving range |
| Undead burn | `MobEntityMixin` injects at the TAIL of `Mob.isSunBurnTick`, the final `return false`. A mob with Sunny counts as in sunlight unless it is in water, rain or a bubble column. There is no day or sky check, so it works at night and underground. It only affects mobs that call `isSunBurnTick` (zombies, drowned out of water, skeletons, strays, phantoms). Husks and fire-immune mobs don't burn |
| Phantoms | `BBEvents.onEntityUpdate`: a phantom whose target is a player with Sunny drops the target. So standing near a moobloom also keeps phantoms off you |
| Other Sunny sources | buttercup suspicious stew: Sunny for 12 s (`ButtercupBlock(SUNNY, 12)`, seconds x 20) |

### 2.4 Hostiles

`BBEvents.onLivingSpawned` (`EntityJoinLevelEvent`): every mob whose type is in `#buzzier_bees:moobloom_hostiles` gets
`NearestAttackableTargetGoal(Moobloom.class, mustSee = false)` at target priority 3.

Tag contents (mod default; the pack doesn't change it): `minecraft:zombie`, `minecraft:zombie_villager`,
`minecraft:stray`, `minecraft:drowned`. Skeletons and husks are not in it.

All four of those mobs burn under Sunny. So they walk into the aura and catch fire. Husks would hunt mooblooms but
never burn, which is why they are left out.

### 2.5 Spawning

| File (in the jar) | Effect |
|---|---|
| `forge/biome_modifier/add_animal/moobloom.json` | `forge:add_spawns` of moobloom, weight 12, group 4 to 4, in `#buzzier_bees:has_animal/moobloom` |
| `forge/biome_modifier/remove_animal/cow.json` | `forge:remove_spawns` of `minecraft:cow` in the same tag: mooblooms **replace** cows there |
| `tags/worldgen/biome/has_animal/moobloom.json` | `#buzzier_bees:has_feature/buttercup` |
| `tags/worldgen/biome/has_feature/buttercup.json` | `minecraft:sunflower_plains` |
| `forge/biome_modifier/add_feature/buttercup.json` | buttercup patches (64 tries, spread 6/2) once every 16 chunks on average, same tag |

So mooblooms spawn in exactly one biome: sunflower plains. The pack's biome tuning leaves sunflower plains at
multiplier 1 (`kubejs/generation_scripts/worldgen/biome_config.json`).

### 2.6 Drops, tags, advancements

- Loot `buzzier_bees:entities/moobloom` is the cow table: 0-2 leather and 1-3 beef (cooked when on fire), with
  Looting. No buttercups on death.
- Entity tags (from `kubejs/exported/tags/entity_type_tags_by_entity_type.json`): only `delightful:fatty_animals`.
- Advancement: added to `minecraft:husbandry/bred_all_animals` through a Blueprint advancement modifier.

### 2.7 Buttercup (the flower)

This is Buzzier Bees on its own; with More Mooblooms installed it is out of play (O11, 4.11).

`buzzier_bees:buttercup` (+ `potted_buttercup`): a `BlueprintFlowerBlock` that gives off pollen particles.
It makes yellow dye and Sunny suspicious stew, and has compost value 1.0 (`compost_values.js`). The Wandering Trader
sells it for 1 emerald (see [wandering_trader_catalog.md](wandering_trader_catalog.md)). Bone meal duplicates it.

### 2.8 Flower spawns per biome (generated by `tools/gen_moobloom_rules.py`, last run 2026-11-07)

Where every moobloom pool flower grows in this pack's Overworld, after the pack's wild flower tuning
([flowers.md](flowers.md), `wild_flowers.json`). This is the input for the variant mixes of the spawn rules (4.9).
It replaces the 2026-10-05 table, which predates the wild flower tuning (History).

**How it is made:** `tools/gen_moobloom_rules.py` (run with `/cdevgen run moobloom_rules`, or plain python) reads
`tools/gen_wild_flowers.py`'s `inventory(..., after=True)`, which is what [flowers_by_biome.md](flowers_by_biome.md)
is written from, and rewrites the tables between the `gen_moobloom_rules` markers below; do not edit them by hand.
Pool flowers and their `feed_weight` come from the shipped variant files; cows from `kubejs/exported/spawns.json`
(`/dumpspawns`). It covers the biome files' own flower features (as `wild_flowers.json` rescales or removes them),
Forge add-feature modifiers, and the pack's wild flower patches. Cows come from the biome files' creature
spawners plus Forge `add_spawns` / `remove_spawns` modifiers, with Buzzier Bees' sunflower plains cow removal
switched off (4.11) and the pack's `moobloom_cows.json` (4.12) included.

**Reading the numbers:**
- "Patches per chunk" = a feature's placements per chunk x this flower's share of the patch. Patches differ in
  tries and fail on sand, stone and snow, so compare rows with each other.
- "Pool flowers" = the 16 flowers in a wild variant's pool: dandelion, sunflower, orange tulip, poppy, red tulip,
  rose bush, peony, lilac, pink tulip, allium, cornflower, blue orchid, lily of the valley, white tulip, oxeye daisy,
  azure bluet. Botania mystical flowers and Buzzier Bees clovers belong to no variant and are left out. "Kinds"
  counts the pool flowers present.
- "Variant mix" = each variant's share of the biome's pool flowers. A shared flower is split by `feed_weight`
  (4.5): dandelion 9:1 moobloom/sunbloom, pink tulip 1:1 onionbloom/peonybloom. Shares under 3% are left out.
- "Per 1000 chunks" = patches per chunk summed over the Overworld, weighted by biome area (same as
  flowers_by_biome.md section 2). "Area %" = the biome's share of the Overworld surface (`biome_config.json`);
  `<0.03` means under the climate sample's resolution, not absent.

**Not covered:** azalea and flowering azalea (the only places they count are lush caves' moss patches,
underground, where cows don't spawn; azalea trees and Twigs azalea flowers on the surface aren't counted), the
wither rose (never generates), flowers on trees or in structures.

<!-- gen_moobloom_rules:2.8 begin -->
**Per flower:**

| Variant | Flower | Per 1000 chunks | Biomes | Most patches per chunk (top 6) |
|---|---|---|---|---|
| Bluebloom | cornflower | 33.2 | 35 | `old_growth_birch_forest` 1.33, `terralith:orchid_swamp` 0.60, `terralith:moonlight_grove` 0.58, `terralith:moonlight_valley` 0.58, `windswept_forest` 0.19, `meadow` 0.18 |
| Lilybloom | lily of the valley | 22.8 | 43 | `birch_forest` 0.39, `terralith:cloud_forest` 0.33, `terralith:moonlight_grove` 0.25, `terralith:moonlight_valley` 0.25, `terralith:temperate_highlands` 0.21, `terralith:yosemite_lowlands` 0.18 |
| Lilybloom | white tulip | 15.9 | 33 | `old_growth_birch_forest` 1.33, `terralith:blooming_plateau` 0.27, `terralith:blooming_valley` 0.27, `terralith:cloud_forest` 0.25, `terralith:temperate_highlands` 0.17, `terralith:skylands_spring` 0.14 |
| Moobloom, Sunbloom | dandelion | 18.2 | 61 | `terralith:skylands_summer` 1.45, `terralith:orchid_swamp` 0.60, `terralith:alpha_islands` 0.50, `terralith:alpha_islands_winter` 0.50, `meadow` 0.18, `terralith:skylands_spring` 0.15 |
| Onionbloom | allium | 14.9 | 20 | `terralith:lavender_forest` 0.80, `terralith:lavender_valley` 0.80, `terralith:sakura_grove` 0.56, `terralith:sakura_valley` 0.56, `meadow` 0.18, `terralith:highlands` 0.16 |
| Onionbloom, Peonybloom | pink tulip | 12.1 | 14 | `old_growth_birch_forest` 1.33, `terralith:sakura_grove` 0.58, `terralith:sakura_valley` 0.58, `terralith:blooming_plateau` 0.14, `terralith:blooming_valley` 0.14, `terralith:skylands_spring` 0.14 |
| Orchidbloom | blue orchid | 20.1 | 14 | `terralith:orchid_swamp` 1.93, `swamp` 0.20, `terralith:moonlight_grove` 0.18, `terralith:moonlight_valley` 0.18, `bamboo_jungle` 0.15, `jungle` 0.15 |
| Oxeyebloom | azure bluet | 11.9 | 39 | `terralith:cloud_forest` 0.25, `meadow` 0.18, `terralith:ice_marsh` 0.15, `terralith:blooming_plateau` 0.12, `terralith:blooming_valley` 0.12, `terralith:moonlight_grove` 0.12 |
| Oxeyebloom | oxeye daisy | 11.6 | 45 | `terralith:skylands_summer` 0.73, `birch_forest` 0.28, `terralith:cloud_forest` 0.25, `meadow` 0.18, `terralith:blooming_plateau` 0.12, `terralith:blooming_valley` 0.12 |
| Peonybloom | lilac | 17.9 | 9 | `old_growth_birch_forest` 3.24, `terralith:lavender_forest` 0.25, `terralith:lavender_valley` 0.25, `terralith:highlands` 0.038, `birch_forest` 0.036, `dark_forest` 0.036 |
| Peonybloom | peony | 6.7 | 17 | `old_growth_birch_forest` 0.84, `terralith:sakura_grove` 0.083, `terralith:sakura_valley` 0.083, `terralith:highlands` 0.038, `birch_forest` 0.036, `dark_forest` 0.036 |
| Rosebloom | poppy | 21.5 | 67 | `terralith:alpha_islands` 0.53, `terralith:alpha_islands_winter` 0.50, `terralith:temperate_highlands` 0.20, `meadow` 0.18, `terralith:forested_highlands` 0.12, `bamboo_jungle` 0.092 |
| Rosebloom | red tulip | 6.4 | 25 | `terralith:blooming_plateau` 0.14, `terralith:blooming_valley` 0.14, `terralith:skylands_spring` 0.14, `terralith:hot_shrubland` 0.087, `terralith:forested_highlands` 0.083, `terralith:snowy_maple_forest` 0.083 |
| Rosebloom | rose bush | 3.7 | 17 | `terralith:forested_highlands` 0.077, `terralith:snowy_maple_forest` 0.077, `terralith:temperate_highlands` 0.077, `bamboo_jungle` 0.042, `jungle` 0.042, `sparse_jungle` 0.042 |
| Sunbloom | orange tulip | 13.0 | 33 | `terralith:moonlight_valley` 1.88, `terralith:temperate_highlands` 0.17, `terralith:blooming_plateau` 0.14, `terralith:blooming_valley` 0.14, `terralith:skylands_spring` 0.14, `terralith:hot_shrubland` 0.11 |
| Sunbloom | sunflower | 3.9 | 11 | `terralith:skylands_summer` 5.82, `sunflower_plains` 0.33, `savanna` 0.042, `windswept_savanna` 0.042, `terralith:arid_highlands` 0.042, `terralith:brushland` 0.042 |

**Per biome**, every biome with 0.25 or more pool-flower patches per chunk, densest first:

| Biome | Area % | Cows | Pool flowers per chunk | Kinds | Variant mix (% of the pool flowers) |
|---|---|---|---|---|---|
| `old_growth_birch_forest` | 0.45 | yes | 8.34 | 10 | peonybloom 57, lilybloom 18, bluebloom 16, onionbloom 8 |
| `terralith:skylands_summer` | <0.03 | yes | 8.00 | 3 | sunbloom 75, moobloom 16, oxeyebloom 9 |
| `terralith:orchid_swamp` | 0.69 | yes | 3.17 | 4 | orchidbloom 61, bluebloom 19, moobloom 17 |
| `terralith:moonlight_valley` | 0.18 | yes | 3.01 | 5 | sunbloom 62, bluebloom 19, lilybloom 8, orchidbloom 6, oxeyebloom 4 |
| `terralith:blooming_plateau` | 0.27 | yes | 1.50 | 10 | lilybloom 26, oxeyebloom 17, onionbloom 14, sunbloom 10, bluebloom 10, rosebloom 10, moobloom 9, peonybloom 5 |
| `terralith:blooming_valley` | 0.22 | yes | 1.50 | 10 | lilybloom 26, oxeyebloom 17, onionbloom 14, sunbloom 10, bluebloom 10, rosebloom 10, moobloom 9, peonybloom 5 |
| `terralith:lavender_forest` | 0.45 | yes | 1.50 | 12 | onionbloom 55, peonybloom 18, lilybloom 6, oxeyebloom 6, rosebloom 6, sunbloom 3, bluebloom 3 |
| `terralith:lavender_valley` | 0.14 | yes | 1.50 | 12 | onionbloom 55, peonybloom 18, lilybloom 6, oxeyebloom 6, rosebloom 6, sunbloom 3, bluebloom 3 |
| `terralith:sakura_grove` | 0.48 | yes | 1.33 | 4 | onionbloom 64, peonybloom 28, lilybloom 8 |
| `terralith:sakura_valley` | 0.16 | yes | 1.33 | 4 | onionbloom 64, peonybloom 28, lilybloom 8 |
| `terralith:cloud_forest` | 0.41 | yes | 1.25 | 6 | lilybloom 47, oxeyebloom 40, bluebloom 10, rosebloom 3 |
| `terralith:alpha_islands` | 0.13 | yes | 1.17 | 4 | rosebloom 46, moobloom 39, lilybloom 9, sunbloom 4 |
| `terralith:alpha_islands_winter` | <0.03 | yes | 1.17 | 6 | rosebloom 43, moobloom 39, lilybloom 7, oxeyebloom 7, sunbloom 4 |
| `terralith:moonlight_grove` | 0.21 | yes | 1.14 | 4 | bluebloom 51, lilybloom 22, orchidbloom 16, oxeyebloom 11 |
| `meadow` | 0.79 | yes | 1.08 | 6 | oxeyebloom 33, bluebloom 17, onionbloom 17, rosebloom 17, moobloom 15 |
| `terralith:skylands_spring` | <0.03 | yes | 1.03 | 8 | onionbloom 21, rosebloom 16, sunbloom 15, bluebloom 14, lilybloom 14, moobloom 13, peonybloom 7 |
| `terralith:temperate_highlands` | 0.61 | yes | 0.91 | 7 | lilybloom 42, rosebloom 30, sunbloom 18, onionbloom 6, oxeyebloom 4 |
| `birch_forest` | 0.26 | yes | 0.81 | 6 | lilybloom 48, oxeyebloom 35, peonybloom 9, rosebloom 9 |
| `terralith:highlands` | 1.30 | yes | 0.69 | 8 | oxeyebloom 26, onionbloom 23, lilybloom 18, bluebloom 18, peonybloom 11, rosebloom 4 |
| `flower_forest` | 0.17 | yes | 0.64 | 14 | lilybloom 20, rosebloom 20, peonybloom 15, oxeyebloom 14, onionbloom 11, sunbloom 8, bluebloom 7, moobloom 6 |
| `sunflower_plains` | 0.11 | yes | 0.63 | 10 | sunbloom 60, moobloom 14, rosebloom 8, lilybloom 6, oxeyebloom 4 |
| `terralith:forested_highlands` | 0.64 | yes | 0.58 | 6 | rosebloom 49, bluebloom 22, lilybloom 14, oxeyebloom 14 |
| `terralith:hot_shrubland` | 0.56 | yes | 0.54 | 10 | sunbloom 31, moobloom 25, rosebloom 23, lilybloom 7, oxeyebloom 5, onionbloom 3, peonybloom 3 |
| `terralith:snowy_maple_forest` | 0.15 | yes | 0.49 | 7 | rosebloom 49, oxeyebloom 34, lilybloom 17 |
| `windswept_forest` | 0.10 | yes | 0.48 | 7 | bluebloom 40, oxeyebloom 21, lilybloom 17, rosebloom 13, onionbloom 7 |
| `terralith:yosemite_lowlands` | 0.12 | yes | 0.47 | 5 | lilybloom 39, bluebloom 27, rosebloom 16, moobloom 10, oxeyebloom 7 |
| `plains` | 1.98 | yes | 0.46 | 9 | oxeyebloom 23, moobloom 19, rosebloom 17, bluebloom 15, sunbloom 10, lilybloom 8, onionbloom 4, peonybloom 4 |
| `terralith:cold_shrubland` | 0.73 | yes | 0.46 | 10 | lilybloom 26, oxeyebloom 23, moobloom 19, rosebloom 11, sunbloom 10, onionbloom 4, peonybloom 4 |
| `terralith:rocky_shrubland` | 0.26 | yes | 0.46 | 10 | lilybloom 26, oxeyebloom 23, moobloom 19, rosebloom 11, sunbloom 10, onionbloom 4, peonybloom 4 |
| `bamboo_jungle` | 0.20 | yes | 0.38 | 6 | orchidbloom 40, rosebloom 36, sunbloom 14, peonybloom 6, moobloom 5 |
| `jungle` | 0.45 | yes | 0.38 | 6 | orchidbloom 40, rosebloom 36, sunbloom 14, peonybloom 6, moobloom 5 |
| `sparse_jungle` | 0.60 | yes | 0.38 | 6 | orchidbloom 40, rosebloom 36, sunbloom 14, peonybloom 6, moobloom 5 |
| `terralith:amethyst_canyon` | 0.07 | yes | 0.38 | 6 | orchidbloom 40, rosebloom 36, sunbloom 14, peonybloom 6, moobloom 5 |
| `terralith:amethyst_rainforest` | 0.41 | yes | 0.38 | 6 | orchidbloom 40, rosebloom 36, sunbloom 14, peonybloom 6, moobloom 5 |
| `terralith:jungle_mountains` | 0.03 | yes | 0.38 | 6 | orchidbloom 40, rosebloom 36, sunbloom 14, peonybloom 6, moobloom 5 |
| `terralith:rocky_jungle` | 0.30 | yes | 0.38 | 6 | orchidbloom 40, rosebloom 36, sunbloom 14, peonybloom 6, moobloom 5 |
| `terralith:tropical_jungle` | 0.10 | yes | 0.38 | 6 | orchidbloom 40, rosebloom 36, sunbloom 14, peonybloom 6, moobloom 5 |
| `terralith:alpine_highlands` | 0.73 | yes | 0.37 | 5 | oxeyebloom 50, bluebloom 33, onionbloom 9, rosebloom 8 |
| `terralith:yellowstone` | 4.28 | yes | 0.36 | 4 | bluebloom 46, lilybloom 30, rosebloom 21 |
| `terralith:ice_marsh` | 0.54 | yes | 0.35 | 5 | oxeyebloom 47, orchidbloom 29, lilybloom 24 |
| `dark_forest` | 1.49 | yes | 0.34 | 7 | lilybloom 43, peonybloom 21, rosebloom 17, onionbloom 16 |
| `forest` | 2.29 | yes | 0.34 | 7 | lilybloom 40, rosebloom 26, peonybloom 21, oxeyebloom 10 |
| `savanna` | 1.02 | yes | 0.30 | 5 | sunbloom 41, rosebloom 38, moobloom 21 |
| `terralith:arid_highlands` | 0.77 | yes | 0.30 | 5 | sunbloom 41, rosebloom 38, moobloom 21 |
| `terralith:brushland` | 1.32 | yes | 0.30 | 5 | sunbloom 41, rosebloom 38, moobloom 21 |
| `terralith:savanna_badlands` | 0.29 | yes | 0.30 | 5 | sunbloom 41, rosebloom 38, moobloom 21 |
| `old_growth_pine_taiga` | 0.77 | yes | 0.28 | 4 | bluebloom 44, lilybloom 30, rosebloom 22, moobloom 3 |
| `old_growth_spruce_taiga` | 0.73 | yes | 0.28 | 4 | bluebloom 44, lilybloom 30, rosebloom 22, moobloom 3 |
| `taiga` | 0.91 | yes | 0.28 | 4 | bluebloom 44, lilybloom 30, rosebloom 22, moobloom 3 |
| `terralith:haze_mountain` | 0.35 | yes | 0.28 | 4 | bluebloom 44, lilybloom 30, rosebloom 22, moobloom 3 |
| `terralith:lush_valley` | 0.09 | yes | 0.28 | 4 | bluebloom 44, lilybloom 30, rosebloom 22, moobloom 3 |
| `terralith:shield` | 0.68 | yes | 0.28 | 4 | bluebloom 44, lilybloom 30, rosebloom 22, moobloom 3 |
| `windswept_savanna` | 0.62 | yes | 0.27 | 5 | sunbloom 45, rosebloom 35, moobloom 20 |
| `savanna_plateau` | 0.71 | yes | 0.26 | 4 | rosebloom 44, sunbloom 31, moobloom 24 |
| `terralith:ashen_savanna` | 0.26 | yes | 0.26 | 4 | rosebloom 44, sunbloom 31, moobloom 24 |
| `terralith:fractured_savanna` | 0.28 | yes | 0.26 | 4 | rosebloom 44, sunbloom 31, moobloom 24 |
| `terralith:savanna_slopes` | 0.29 | yes | 0.26 | 4 | rosebloom 44, sunbloom 31, moobloom 24 |

The rest, under 0.25 pool-flower patches per chunk, grouped by the pool flowers they have:

| Pool flowers (all under 0.25 per chunk) | Biomes |
|---|---|
| none | 30: `badlands`, `desert`, `eroded_badlands`, `frozen_peaks`, `frozen_river`, `ice_spikes`, `jagged_peaks`, `mushroom_fields`, `snowy_beach`, `snowy_slopes`, `stony_peaks`, `terralith:ancient_sands`, `terralith:bryce_canyon`, `terralith:desert_canyon`, `terralith:desert_oasis`, `terralith:desert_spires`, `terralith:emerald_peaks`, `terralith:frozen_cliffs`, `terralith:glacial_chasm`, `terralith:gravel_desert`, `terralith:lush_desert`, `terralith:painted_mountains`, `terralith:red_oasis`, `terralith:sandstone_valley`, `terralith:snowy_badlands`, `terralith:stony_spires`, `terralith:volcanic_crater`, `terralith:volcanic_peaks`, `terralith:warped_mesa`, `terralith:white_mesa` |
| azure bluet, lily of the valley, oxeye daisy, white tulip | 13: `grove`, `snowy_plains`, `snowy_taiga`, `terralith:alpine_grove`, `terralith:birch_taiga`, `terralith:rocky_mountains`, `terralith:scarlet_mountains`, `terralith:siberian_grove`, `terralith:siberian_taiga`, `terralith:skylands_winter`, `terralith:snowy_shield`, `terralith:wintry_forest`, `terralith:wintry_lowlands` |
| dandelion, poppy | 10: `beach`, `river`, `stony_shore`, `windswept_gravelly_hills`, `terralith:basalt_cliffs`, `terralith:granite_cliffs`, `terralith:gravel_beach`, `terralith:warm_river`, `terralith:white_cliffs`, `terralith:windswept_spires` |
| allium, azure bluet, cornflower, dandelion, oxeye daisy, poppy | 2: `windswept_hills`, `terralith:caldera` |
| allium, blue orchid | 2: `mangrove_swamp`, `swamp` |
| azure bluet, cornflower, oxeye daisy, poppy, sunflower | 2: `terralith:steppe`, `terralith:valley_clearing` |
| dandelion, orange tulip, poppy, red tulip | 2: `wooded_badlands`, `terralith:skylands_autumn` |
| allium, azure bluet, cornflower, oxeye daisy | 1: `terralith:yosemite_cliffs` |
| azure bluet, cornflower, oxeye daisy, poppy | 1: `terralith:shield_clearing` |
| dandelion, lilac, lily of the valley, peony, poppy, rose bush | 1: `terralith:mirage_isles` |
| dandelion, orange tulip, poppy, red tulip, sunflower | 1: `terralith:shrubland` |
<!-- gen_moobloom_rules:2.8 end -->

## 3. Gaps in Buzzier Bees' moobloom

| # | Gap | Effect | How More Mooblooms closes it |
|---|---|---|---|
| G1 | The moobloom has only `delightful:fatty_animals`, while the cow also has `forge:milkable`, `supplementaries:eats_fodder`, `supplementaries:cage_baby_catchable`, `abnormals_delight:scavenges_leather`, `botania:cocoon/common`, `alexsmobs:passive_land_animals` and 7 `alexsmobs:*_targets` | Supplementaries fodder doesn't feed it, baby mooblooms can't be caged, Alex's Mobs predators ignore it, and mods that milk by tag skip it (the bucket still works) | our moobloom gets the cow's tags from the mod's compat packs (4.10) |
| G2 | The Sample Syringe matches exact type ids (`SAMPLES['minecraft:cow']`), so a moobloom can't be sampled | No cultured-beef route from mooblooms | one pack-side syringe line for `more_mooblooms:moobloom` (4.10) |
| G3 | Spawns only in sunflower plains, and replaces cows there | Rare in practice | BB's spawns are off; per-biome rules and a rare roll on every cow (4.9, 4.11) |
| G4 | The hostile goal is added on every `EntityJoinLevelEvent`, including chunk reloads and the client | A persistent zombie collects one extra copy of the goal each time its chunk reloads (harmless, just wasted scans) | BB's mooblooms never enter the world and the hostiles tag is emptied (4.11) |

## 4. More Mooblooms 1.0

### 4.0 Buzzier Bees' license and what it means for the mod

Buzzier Bees ships under the **Abnormals License 1.0** (June 2020, Team Abnormals; linked from its `mods.toml`:
`https://github.com/team-abnormals/buzzier-bees/blob/main/LICENSE.txt`). The full text the user supplied:

> **ABNORMALS LICENSE** Version 1.0, June 2020. Copyright (c) 2020 Team Abnormals. All Rights Reserved.
>
> Permissions are hereby granted below to any person having a copy of this software and/or its associated files:
>
> - **Usage** - You may use this software for private and public use. You may use this software as a library or
>   integrate it with your code with the predicate being it must be downloaded from our sources and a soft or hard
>   dependency.
> - **Copying** - You may copy this software for private use or to contribute to this software.
> - **Modification** - You may modify this software for private use or to contribute to this software.
> - **Publishing** - You may not publish copies of this software.
> - **Distributing** - You may not distribute this software.
> - **Sublicensing** - You may not sublicense this software.
> - **Selling** - You may not sell this software.
>
> **Modpack Clarification:** Permission is granted for Modpacks to include this software as long as the copy of
> this software in the pack is an unmodified public copy from our public sources and is marked as included in the
> Modpack.
>
> **Definition of 'Our Sources' in this license:** Our Sources in this license means a copy of this software or its
> associated files that come directly from the owners of this software, an example are the files uploaded by us on
> our Curse Page.
>
> The above copyright notice and these permission notices shall be included in all copies or substantial portions
> of the Software.
>
> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND [...]

**Reading.** It is open for *using* the mod: you can depend on it, interoperate with it, and ship it in a modpack.
It is closed for *redistributing* it: no published copies and no modified copies. "Software and/or its associated
files" covers the textures, models, lang and data JSON as well as the code. More Mooblooms is GPLv3 (the user's rule
for their own work), and the license forbids sublicensing, so nothing of BB's can be inside it. This is a
practical reading for planning, not legal advice. If anything is borderline, ask Team Abnormals.

| Clause | What we do | OK? |
|---|---|---|
| Usage as a library, soft or hard dependency, downloaded from their sources | BB is an **optional** (soft) dependency of More Mooblooms. The pack installs the CurseForge jar | yes |
| Modpack: unmodified public copy, marked as included | the pack ships `buzzier_bees-1.19.2-5.0.1.jar` unchanged (offline check: md5 matches CurseForge, section 6) and lists it in the pack's mod list / manifest / `mod_sides.md` | yes |
| Copying / modifying for private use | decompiling it to read how it works for this writeup. The output stays in the scratchpad and is never committed | yes |
| Publishing / distributing copies or modifications | More Mooblooms contains **no** BB code, texture, model, lang or data file, copied or edited. See the rules below | must hold |
| Sublicensing | nothing of BB's under our GPLv3 | must hold |

**Rules this puts on the implementation:**

1. **Our own moobloom entity, written from scratch.** It does not subclass, copy or mixin into BB's `Moobloom`.
   The back-flower layer is modelled on vanilla's mooshroom mushroom layer (`MushroomCowMushroomLayer`), which is
   also where BB's idea comes from. The positions, angles and scale are ours.
2. **No BB assets in our jar.** BB's yellow `moobloom.png` can't ship, so the yellow default variant (`moobloom`) has
   **our own coat** (D15), like the other ten. We don't reuse BB's buttercup model, particles or lang either.
3. **BB is changed only from outside, never patched:**
   - our **own** data files that override BB's by path (`forge:none` biome modifiers, recipe files carrying a
     `forge:false` condition, an emptied tag, advancement modifiers). Each is written fresh, never copied from BB's
     file and edited.
   - Forge events: entity join, `WandererTradesEvent`, `MobEffectEvent.Applicable`, setup.
   - mixins into **vanilla classes only** (`NaturalSpawner`, `Item`, `TagLoader`, `Mob`).

   No mixin targets a BB class, and the BB jar on disk stays the unmodified public copy the modpack clause requires.
   (Runtime-patching BB's classes would arguably count as distributing a modification inside our jar. We avoid the
   question entirely.)
4. **No compile dependency on BB.** We refer to BB only by registry ids (`buzzier_bees:moobloom`,
   `buzzier_bees:buttercup`, `buzzier_bees:sunny`, ...), looked up at runtime and only when `buzzier_bees` is loaded.
   Nothing of BB goes in `libs/`.
5. This writeup describes BB's behaviour in our own words and numbers; no BB code is quoted.

### 4.1 Requirements

| ID | Requirement (user) | Where |
|---|---|---|
| O1 | At least 9 new variants | 11 variants (4.4) |
| O2 | Feeding a flower to a cow has a chance to turn it into the matching moobloom | 4.5 |
| O3 | No Sunny aura and no zombie bait on the moobloom | ours has neither (4.3); BB's is removed (4.11) |
| O4 | The moobloom has the rest of the cow's tags (G1) | 4.10 |
| O5 | The Sample Syringe takes a beef sample from a moobloom (G2) | 4.10 |
| O6 | Mooblooms don't spawn only in sunflower plains | 4.9 |
| O7 | Every variant can spawn rarely from the normal cow pool, like vanilla's pink sheep (0.164%) | `rareChance` (4.9) |
| O8 | Per-biome rules replace a percentage of cows with a specific variant or a random one | 4.9 |
| O9 | Two-stage shearing: shearing drops the flowers, it stays a moobloom and shows bare, and it regrows them after eating grass, like a sheep | 4.6 |
| O10 | Several flowers per variant, each with its own drop chance | a weighted pool per variant, a drop chance per entry (4.4, 4.6) |
| O11 | Remove the buttercup item entirely, including the Wandering Trader trade | 4.11 |
| O12 | Remove the Sunny effect entirely (JEI/JEED, the buttercup and moobloom area effect, every source) | 4.11 |
| O13 | All of it is its own mod, More Mooblooms: the mooblooms, shearing, feeding, spawning, everything is defined there, not in patcherMod or KubeJS | 4.2 |
| O14 | If Buzzier Bees is installed, the mod makes the BB changes itself (BB's moobloom, buttercup, Sunny). Without BB it does none of that and still works on its own | content sets (4.2, 4.11) |
| R1 | Final names, coats and flower mixes (the 11 coats) | 4.4 |
| R2 | Every moobloom can breed with every other one. The child is random, except for custom combinations defined in data | 4.8 |
| R3 | Spawn eggs swap to another variant after use: debug it | Quark Automatic Tool Restock; fixed in the pack (4.14) |
| R4 | A config switch that turns Sunny off completely, including Buzzier Bees' daylight burn check | `disableSunny` (4.11) |
| R5 | Wild onions grow on onionbloom like any flower when Farmer's Delight is installed | an optional pool entry (4.4) |
| R6 | A flower in several pools converts a cow to one of those variants at random, except dandelion: 90% moobloom, 10% sunbloom | `feed_weight` (4.5) |
| R7 | Witherblooms spawn with a very low chance in soul sand valleys | 4.9 |

### 4.2 Mod shape

| | |
|---|---|
| Name / id | **More Mooblooms**, `more_mooblooms`, version 1.1.0 (1.1: the JEI pages, section 5; 1.0.0 was D22, kept for the 2026-10-11 azaleabloom `spawns_in_dark_on` rebuild, by the user's choice). Folder `development/moreMooblooms`, jar `moreMooblooms-<ver>.jar`, package `com.SaintAlfonzo.more_mooblooms`, GPLv3 |
| Build | a project in the root Gradle build (`settings.gradle`, `ce.forge-mod` convention plugin), the same as Incubation Additions: `./gradlew :moreMooblooms:build`, `./gradlew :moreMooblooms:installToMods` (moves other versions to `jar_archive/displaced/`) |
| Hard dependencies | Forge, Minecraft 1.19.2 |
| Optional dependencies | `buzzier_bees` (the O14 set), `jei` (plugin), `blueprint` (advancement modifier), the tag-compat mods (4.10), `farmersdelight` (wild onions on onionbloom, 4.4) |
| Pattern | the Incubation Additions **content sets**: a core set that is always on, plus one set per optional mod. Each set's data is a hidden, required built-in pack `packs/<mod id>/`, added through `AddPackFindersEvent` only when that mod is loaded; its Java only runs then; a mixin plugin gates its mixins. Core data that a config switch controls is a pack too: `packs/more_mooblooms/<switch>/` |
| Network | channel `more_mooblooms:main`, one packet (`SyncVariantsPacket`: the variant and breeding files). Client and server need the same jar |
| Writeup / tests | this file, `MB-` |

**Config** (`config/more_mooblooms-common.toml`; the pack's copy is in `configs_edited/`):

| Section | Key | Mod default | Pack | Meaning |
|---|---|---|---|---|
| `moobloom` | `feedChance` | 0.10 | 0.10 | chance per flower fed that a cow converts (4.5) |
| | `regrowChance` | 0.25 | 0.25 | chance per meal of grass that a bare moobloom regrows (4.6) |
| | `shearBareToCow` | true | true | shearing a bare moobloom turns it into a cow; off = a bare moobloom can't be sheared |
| | `bareShearDrops` | true | true | that second shear also drops three pool flowers |
| `spawning` | `spawnSwap` | true | true | master switch for the cow swap (4.9) |
| | `rareChance` | 0 | 0.002 | chance that any naturally spawning cow becomes a random moobloom |
| | `witherbloomSpawns` | true | true | the soul sand valley witherbloom pack (4.9); next world load |
| `buzzier_bees` | 14 switches | all true | all true | one per Buzzier Bees change (4.11); only read when BB is installed |

**Content sets:**

| Set (needs) | Java | Data |
|---|---|---|
| core (always) | entity, renderer + flower layer, variant and breeding tables (reload listeners + client sync), shearing and regrowth, feeding, spawn swap (`NaturalSpawnerMixin`), spawn egg, config | jar resources: `assets/more_mooblooms/` (11 coats, lang, egg model), `data/more_mooblooms/` (variants, breeding, loot table, block tag `spawnable_on_in_dark`), `data/forge/tags/entity_types/milkable.json` |
| core switch `witherbloom_spawns` (config `witherbloomSpawns`) | spawn placement and the direct-spawn variant (4.9) are core code | `packs/more_mooblooms/witherbloom_spawns/`: biome tag, biome modifier, spawn rule (witherbloom's `spawns_in_dark_on` is in its variant file) |
| `buzzier_bees` | BB moobloom removal, trade removal, compost entry removal, creative hiding, Sunny off (4.11) | `packs/buzzier_bees/<switch>/`, one sub-pack per data switch |
| `jei` | variant egg subtypes, info pages, the "Moobloom Breeding", "Moobloom Shearing" and "Moobloom Conversion" categories, hiding the buttercup, BB's egg, Sunny and Just Enough Breeding's BB moobloom page when BB is loaded | none |
| `blueprint` | none | `packs/blueprint/`: Two by Two asks for our moobloom |
| `alexsmobs`, `supplementaries`, `abnormals_delight`, `botania`, `delightful` | none | entity type tag files adding `more_mooblooms:moobloom` to that mod's cow tags (4.10) |

### 4.3 The entity

`more_mooblooms:moobloom`: a class of our own extending vanilla `Cow` and implementing `Shearable` and
`IForgeShearable`. It is `CREATURE`, cow-sized (0.9 x 1.4), with `Cow.createAttributes()`.

| Behaviour | How |
|---|---|
| Synced and saved | `Variant` (variant id), `Sheared` (boolean), `Flowers` (the three grown block states; saved as a list of strings, synced as one string). An unknown `Variant` on load becomes the default variant with a log line. No `Variant` at all (`/summon` with NBT skips `finalizeSpawn`) picks a weighted random one |
| Flowers | each of the three positions grows its own roll from the variant's pool (4.4). Empty while bare or a calf. Rolled when it first shows flowers (spawn, egg, `/summon`, cow conversion, a calf growing up) and again, fresh, on every regrowth (D46). A saved flower that is no longer in its pool is re-rolled on load. `/summon` and egg NBT can set `Flowers` directly |
| Name | `getTypeName` -> `entity.more_mooblooms.moobloom.<variant>`, the name alone ("Sunbloom"; WTHIT, death messages) |
| Food | the variant's pool items plus its `feed` items and tags: breeding, calf growth and tempting (D6). Wheat doesn't work |
| Breeding | any two mooblooms (same class); the calf's variant comes from 4.8. It can't breed with a cow |
| Spawn egg used on a moobloom | a calf of the egg's variant (a plain egg: the clicked moobloom's). Overrides vanilla `SpawnEggItem.spawnOffspringFromSpawnEgg`, which asks the clicked mob for `getBreedOffspring` and never reads the egg's `EntityTag` |
| Milk | inherited from `Cow` (bucket) |
| No aura, no bait (O3) | there is none. BB's hostiles goal targets BB's class, so ours is never hunted |
| Loot | `more_mooblooms:entities/moobloom`: our own table with the cow drops (leather, beef, cooked when on fire) |
| Shearing (O9, O10) | 4.6 |
| Grazing (O9) | `EatBlockGoal` at priority 5; `ate()` regrows. The sheep's head-down animation on the cow model (D18) |
| Spawn placement | the cow's (`Animal.checkAnimalSpawnRules`: grass below, light), or a block that **the moobloom's variant** lists in `spawns_in_dark_on`, with no grass or light needed. The witherbloom lists soul sand and soul soil, the azaleabloom moss blocks, so only witherblooms spawn on soul blocks and only azaleablooms on moss in the dark. Two steps, because the variant isn't known at the first (4.14): the type-level placement check lets through a block that *any* variant lists; then `checkSpawnRules`, on the new moobloom, picks its variant and lets the block through only if that variant lists it |
| Variant at spawn | a swapped cow: set by the swap (4.9). A moobloom a biome modifier spawns directly: its biome's rule, else a weighted random one, picked in `checkSpawnRules` (before `finalizeSpawn`). An egg: its `EntityTag.Variant`, else a weighted random one |

The renderer is a `MobRenderer` with vanilla `CowModel` on our own model layer, the flower layer (4.7), and the
texture `<ns>:textures/entity/moobloom/<path>.png` of the variant id.

### 4.4 Variants as data

One file per variant: `data/<ns>/moobloom_variants/<path>.json`. The mod ships 11 in its jar. Any data pack (the
pack's `kubejs/data/more_mooblooms/...`) can override one by path or add new ones. A reload listener loads them in
id order and checks them against the registries; a bad file is logged and skipped. `more_mooblooms:moobloom` is
the default variant (when it's missing, the first one; with no variants at all, a built-in dandelion moobloom).

**Client sync:** `OnDatapackSyncEvent` (at login, before the recipe and tag packets, so JEI sees every egg; and
after `/reload`) sends every variant and breeding file as JSON text, and the client parses them the same way. The
client needs them for names, the egg list and colours, and the JEI pages. Each moobloom's own flowers are synced as
entity data.

```json
{
  "flowers": [
    { "state": "minecraft:rose_bush[half=upper]", "weight": 5 },
    { "state": "minecraft:poppy", "weight": 4 },
    { "state": "minecraft:red_tulip", "weight": 1 }
  ],
  "egg_colors": ["#9B221A", "#ED302C"]
}
```

| Field | Default | Meaning |
|---|---|---|
| `flowers` | required | the weighted pool: one block state string (a one-entry pool), or a list of strings and objects |
| `flowers[].state` | required | block state (`id` or `id[prop=value]`), for example `minecraft:lilac[half=upper]` |
| `flowers[].weight` | 1 | weight in each position's roll |
| `flowers[].chance` | 1.0 | drop chance on both shears (4.6) |
| `flowers[].drop` | the block's item | the item shearing drops; required for a block without an item |
| `flowers[].optional` | false | skip the entry when its block isn't registered (another mod's flower) |
| `flowers[].feed_weight` | 1 | when this entry's item is food for several variants, this variant's weight in the cow-feeding pick (4.5) |
| `feed` | none | extra food, items or `#tags` (convert, breed, calf growth). Pool items are food already |
| `scale` | 1.0 | scale of the flower models (the azalea bushes, D4) |
| `any_weight` | 1 | weight for "any" spawn rules, the rare roll and a plain egg; 0 = never wild |
| `breed_weight` | `any_weight` | weight in the random calf pick (4.8) |
| `spawns_in_dark_on` | none | block ids or `#tags` this variant may spawn on naturally with no grass or light (4.3, 4.9). Witherbloom: `["#more_mooblooms:spawnable_on_in_dark"]` (soul sand, soul soil). Azaleabloom: `["minecraft:moss_block"]` (lush caves, 4.9). Not a table column: the generator keeps it from the existing file |
| `egg_colors` | the cow egg's | spawn egg base and spot colours |

**Texture and name:** `assets/<ns>/textures/entity/moobloom/<path>.png`, 64x32 in the cow UV layout, and the lang
key `entity.more_mooblooms.moobloom.<path>` (`.<ns>.<path>` outside our namespace) holding the name alone. Pack-side
hot-swap of a coat: the same path under `kubejs/assets/more_mooblooms/`, then F3+T.

**The variant table (D30)** is the source of the shipped data: `python3 tools/gen_moobloom_variants.py` turns it
into the variant files and the breeding files (`--check` for a dry run; the cell syntax is in the script's
docstring) and then runs `development/moreMooblooms/tools/egg_colors.py`.

- **Flower pool:** the three positions (back front, back rear, head; 4.7) are fixed, but **which flower grows in
  each one is rolled from the pool**, independently per position, by weight. One rosebloom can carry two rose bushes
  and a poppy, the next three poppies.
- **The rolls are saved on the moobloom** (4.3) and redone each time the flowers grow (D46).
- **Tall flowers** use their top half (`[half=upper]`); shearing drops the full 2-block item.
- **Every vanilla flower is in at least one pool.** Dandelion (moobloom, sunbloom) and pink tulip (onionbloom,
  peonybloom) are in two; feeding one to a cow picks a variant by `feed_weight` (4.5).
- **Parent combos** are breeding combinations (4.8), chosen by colour mixing (red + white = pink, yellow + blue =
  green). A combination is an extra route: every variant except witherbloom also spawns wild (`any_weight` 1) and
  comes from feeding.

| Texture | id / name | Coat | Flower pool (weight, chance per position) | Extra food (convert, breed, calf growth) | Parent Combo A | Parent Combo B |
|---|---|---|---|---|---|---|
| `moobloom.png` | `moobloom` / Moobloom | yellow, cream spots | dandelion 1, feed `weight` 9 | | | |
| `sunbloom.png` | `sunbloom` / Sunbloom | orange-yellow, yellow spots | sunflower 5, dandelion 4, orange_tulip 1 | | moobloom+moobloom (10%) | moobloom+oxeyebloom (20%) |
| `rosebloom.png` | `rosebloom` / Rosebloom | dark red, red spots | rose_bush 5, poppy 4, red_tulip 1 | | | |
| `peonybloom.png` | `peonybloom` / Peonybloom | light pink, pale spots | peony 5, lilac 4, pink_tulip 1 | | rose+lily(35%) | |
| `onionbloom.png` | `onionbloom` / Onionbloom | purple, lilac spots | allium 6, pink_tulip 1, `farmersdelight:wild_onions` 1 (only when Farmer's Delight is installed) | | rose+blue(25%) | rose+orchid(25%) |
| `bluebloom.png` | `bluebloom` / Bluebloom | blue, light blue spots | cornflower 1 | | orchid+onion(20%) | |
| `orchidbloom.png` | `orchidbloom` / Orchidbloom | light blue, pale blue spots | blue_orchid 1 | | lily+blue(20%) | |
| `lilybloom.png` | `lilybloom` / Lilybloom | white, grey spots | lily_of_the_valley 2, white_tulip 1 | | | |
| `oxeyebloom.png` | `oxeyebloom` / Oxeyebloom | white, yellow spots | oxeye_daisy 2, azure_bluet 1 | | lily+sunbloom(50%) | lily+moobloom(20%) |
| `azaleabloom.png` | `azaleabloom` / Azaleabloom | olive, pink blossom pixels | flowering_azalea 2, azalea 1; `scale` 0.5 (D4) | | moobloom+blue(20%) | |
| `witherbloom.png` | `witherbloom` / Witherbloom | black-brown, dark spots | wither_rose 1, drop `chance` 0.1 (D43) | | | |

**About the weights and parents:**
- The weights and chances are the user's (D30).
- Each parent pair makes one variant (the generator refuses a pair twice), so no pair has two results.
- moobloom + moobloom -> sunbloom at 10%: a combination comes before "breeds true" (4.8), so two mooblooms make a
  sunbloom 10% of the time and a moobloom otherwise.

**Wild onions on onionbloom (R5, D45):** `farmersdelight:wild_onions` is an optional pool entry (weight 1, so 1 in 8
per position). With Farmer's Delight installed it grows on onionbloom's back, drops when sheared, and is onionbloom
food like any pool flower. Without it the entry is skipped and onionbloom grows allium and pink tulips (6 to 1).

**Egg colours:** `development/moreMooblooms/tools/egg_colors.py` writes `egg_colors` from each coat: the commonest
colour as the base and the commonest clearly different one (RGB distance 60 or more) as the spots. Pixels that are
identical on every coat (udder, eyes, hooves, horns) are skipped; without that, the pink udder came out as the spot
colour of the white and blue coats.

**Spawn eggs (D14):** one item, `more_mooblooms:moobloom_spawn_egg`, with `EntityTag.Variant` NBT. The creative tab
and JEI list one stack per synced variant (JEI subtypes by variant), named "<Name> Spawn Egg" and coloured from
`egg_colors`; middle-click on a moobloom gives its own egg. The moobloom variant's egg and the plain egg both read
"Moobloom Spawn Egg" (D42). A plain egg spawns a weighted random variant.

### 4.5 Feeding cows (O2, R6)

- Right-click a `minecraft:cow` (exact type: not a mooshroom, not a moobloom; D7) with any variant's food.
- Every try uses one (except in creative) and shows a particle.
- With chance `feedChance` (D6) the cow becomes a moobloom of that variant: position, rotation, health, age, name,
  persistence, no-AI, leash and vehicle carry over. It rolls its flowers (a calf stays a calf, with none), with the
  mooshroom conversion sound and a flower particle burst.
- **A flower that is food for several variants** picks one by each variant's `feed_weight` for it (default 1 each,
  so an even pick). Today: dandelion 90% moobloom (`feed_weight` 9) / 10% sunbloom (1), pink tulip 50% onionbloom /
  50% peonybloom. `check_moobloom_data.py` prints these splits.
- Items that are no variant's food do nothing and aren't used. Wheat still breeds cows.

### 4.6 Two-stage shearing (O9, O10, D8)

| State | Look | Shears | What changes it |
|---|---|---|---|
| **Grown** (`Sheared` = false) | its three flowers | drops the flowers it is carrying, shear sound, `Sheared` = true; it stays a moobloom | shearing |
| **Bare** (`Sheared` = true) | the coat, no flower models | turns back into a **cow** (`shearBareToCow`), keeping position, health, age, name, leash; with `bareShearDrops`, three pool flowers drop. With `shearBareToCow` off: nothing | eating grass |

| Shear | Drops |
|---|---|
| First (grown -> bare) | **The flowers it is carrying:** each position drops 1 of its flower's item, at that pool entry's `chance`. A rosebloom showing rose bush, rose bush, poppy drops 2 rose bushes and 1 poppy |
| Second (bare -> cow) | **Weighted by the pool:** 3 picks (one per position, D44), each at its entry's `chance`. A rosebloom averages 1.5 rose bushes, 1.2 poppies and 0.3 red tulips |

- Same items drop as one stack.
- Ordinary flowers drop 3 per shear (D38). Witherbloom's wither rose has `chance` 0.1 (D43, D5): about 0.3 per
  shear, because grass regrowth makes every variant an endless source of its flowers.
- **Starts grown:** natural and rule spawns, feeding conversions, spawn eggs, `/summon` without `Sheared:1b`, and
  calves once they grow up. Calves show no flowers.
- **Regrowth like sheep:** vanilla `EatBlockGoal` has a 1 in 1000 chance per tick to start (1 in 50 for calves). It
  eats a grass block below (which turns to dirt) or tall grass (removed if `mobGriefing` is on), then calls `ate()`.
  Our `ate()`: if bare, roll `regrowChance` (D17) and regrow with a fresh roll; a calf also ages up 60 seconds, like
  a lamb. Mooblooms graze sheared or not, so pens lose grass.
- **One code path for every shear source:** player shears (`IForgeShearable.onSheared`), dispensers
  (`Shearable.shear`) and Create deployers (they act as a player) all end in `Moobloom.shearInternal`.

### 4.7 The flower layer

Our own `RenderLayer` on the cow model, built the way vanilla's mooshroom mushroom layer is. It draws nothing when
the moobloom is a calf, invisible (outline only when it glows) or bare. Otherwise it draws the moobloom's three
synced flowers as block models at the variant's `scale`, each anchored at its base:

| Slot | Where (`SlotPose` constants in `MoobloomFlowerLayer`; tune in game, D19) |
|---|---|
| `back_front` | on the back, towards the shoulders |
| `back_rear` | on the back, towards the hips |
| `head` | on the head, attached to the head part so it moves with it |

### 4.8 Breeding (R2)

Any two mooblooms that are in love mate (they are the same class), each fed its own variant's food (D32). The
calf's variant, first match wins:

1. **Combination:** `data/<ns>/moobloom_breeding/*.json` holds rules for an unordered pair of parents. Roll the
   rule's `chance`: on a hit the calf is a weighted pick from `results`; on a miss the next steps apply. When several
   rules match a pair, the first one wins, by file name and then by order in the file (same as D11).
2. **Same variant:** two parents of the same variant breed true (D34).
3. **Random:** a weighted pick over every variant's `breed_weight` (D33). Witherbloom is 0, so it never comes from
   a random calf.

```json
{ "parents": ["more_mooblooms:rosebloom", "more_mooblooms:lilybloom"], "chance": 0.35,
  "results": [ { "variant": "more_mooblooms:peonybloom", "weight": 1 } ] }
```

A file holds one rule, or several as `{"rules": [...]}`. `chance` defaults to 0.5 (D35); a result can be a plain
variant id (weight 1). The mod ships one file per child variant, generated from the table's Parent Combo columns
(4.4). Variant ids are looked up when used, so the load order against the variant table doesn't matter; unknown
ids are logged at load.

- A calf carries no flowers; it rolls its own when it grows up.
- **JEI:** the "Moobloom Breeding" category (section 5).
- A spawn egg used on a moobloom makes a calf of the egg's variant, not a bred one (4.3).

### 4.9 Spawning (O6, O7, O8, R7)

**The cow swap** (core set, `NaturalSpawnerMixin` on vanilla `NaturalSpawner`): a cow that the natural spawner is
about to add goes through `MoobloomSpawns.swap`. If the roll hits, a moobloom is created at the cow's position and
rotation, `finalizeSpawn`ed with the chosen variant (grown, the cow's age) and added **instead** of the cow, which
has already passed every spawn check.

| Path | Spawn reason | Hook |
|---|---|---|
| Ongoing natural spawns | `NATURAL` | `@ModifyArg` on `addFreshEntityWithPassengers` in `spawnCategoryForPosition` (`m_47038_`) |
| Chunk generation herds | `CHUNK_GENERATION` | the same in `spawnMobsForChunkGeneration` (`m_220450_`); the WorldGenRegion comes through a ThreadLocal set at the method's HEAD |

**Not touched:** spawn eggs, breeding, `/summon`, mob spawners, and cows placed by structure NBT.

**Rule files:** `data/<ns>/moobloom_spawns/<name>.json`. Every file is loaded and their rules are combined:

```json
{ "rules": [ { "biomes": ["minecraft:flower_forest", "#some:tag"],
               "replace": [ { "variant": "more_mooblooms:lilybloom", "percent": 50 },
                            { "variant": "any", "percent": 10 } ] } ] }
```

**Roll order, per cow (D10):**
1. Find the biome's rule. An exact biome id beats a `#tag`, then the first tag rule wins, by file name then order
   (D11).
2. Roll 0-100 and walk the entries cumulatively. A hit gives that variant; `any` picks one by `any_weight`.
3. Otherwise roll `rareChance` (O7, D9). A hit picks one by `any_weight`.
4. Otherwise it stays a cow.

| Rule | What spawns in that biome |
|---|---|
| none | cows, with `rareChance` of a random moobloom |
| any 100% | only mooblooms, random variants |
| lilybloom 50% (format example) | 50% lilyblooms; the other 50% are cows, each with the rare roll |
| bluebloom 10% + azaleabloom 10% (format example) | 10% each, 80% cows with the rare roll |

Percentages over 100 in one rule log a warning and are scaled down to 100.

**Who controls cow spawning (D24):** the mod ships no cow rules, and `rareChance` defaults to 0. On its own, More
Mooblooms never turns a spawning cow into a moobloom. In this pack the rule file and `rareChance` are
CreativeEngineering's.

**This pack's rules (D12, D29, D47): mooblooms follow the wild flowers.** One file,
`kubejs/data/more_mooblooms/moobloom_spawns/creativeengineering.json`, written by `tools/gen_moobloom_rules.py` from
the 2.8 numbers; the flower-biome rules (and skylands autumn, lush caves) are hand-set in the `manual` block of
`kubejs/generation_scripts/worldgen/moobloom_rules.json`, which also holds the tiers. Every
rule names exact biome ids (no tags), so it beats any tag rule (D11). Two things come from 2.8:

1. **How many cows become mooblooms**, by the biome's pool-flower patches per chunk:

   | Tier | Pool flowers per chunk | Cows that become mooblooms | Biomes | Area % |
   |---|---|---|---|---|
   | Flower biome | 1.0 or more, plus `flower_forest` and `sunflower_plains` by name (D48) | 25% to 100%, set per biome (D49) | 18 | 3.7 |
   | Flowery | 0.5 to 1.0 | 25% | 5 | 3.4 |
   | Some flowers | 0.25 to 0.5 | 2% | 34 | 26.9 |
   | Few or none | under 0.25 | no rule: `rareChance` (0.2%) only, any variant | the rest | |

2. **Which variant**: the biome's variant mix (2.8, shares under 5% dropped, the rest rescaled to the tier's
   percent and rounded, so a rule always adds up to the tier). Biomes with the same mix share one rule. In the
   flower biome table the split is your hand-set share of the mooblooms; the rule file holds bloom % x split /
   the split's total (old growth birch forest: 35 x 58/100 = peonybloom 20.3% of cows). Lavender's split adds up
   to 106, so it is scaled to fit its 75%.

A cow that a rule under 100% leaves still gets the rare roll (any variant, by `any_weight`).

**Flower biomes High moobloom %, not always 100%:**

| Biomes | Bloom % | Variants, bloom % split |
|---|---|---|
| `old_growth_birch_forest` | 35% | peonybloom 58, lilybloom 18, bluebloom 16, onionbloom 8 |
| `terralith:skylands_summer` | 50% | sunbloom 75, moobloom 16, oxeyebloom 9 |
| `terralith:orchid_swamp` | 65% | orchidbloom 63, bluebloom 19, moobloom 18 |
| `terralith:moonlight_valley` | 75% | sunbloom 66, bluebloom 19, lilybloom 9, orchidbloom 6 |
| `terralith:blooming_plateau`, `terralith:blooming_valley` | 100% | even: 9 variants, every one but wither- and azaleabloom |
| `terralith:lavender_forest`, `terralith:lavender_valley` | 75% | onionbloom 75, peonybloom 15, oxeyebloom 5, rosebloom 5, lilybloom 6 |
| `terralith:sakura_grove`, `terralith:sakura_valley` | 65% | onionbloom 64, peonybloom 28, lilybloom 8 |
| `terralith:cloud_forest` | 65% | lilybloom 48, oxeyebloom 42, bluebloom 10 |
| `terralith:alpha_islands` | 35% | rosebloom 50, moobloom 50 |
| `terralith:alpha_islands_winter` | 35% | rosebloom 50, moobloom 50|
| `terralith:moonlight_grove` | 65% | bluebloom 50, lilybloom 23, orchidbloom 15, oxeyebloom 12 |
| `meadow` | 75% | even: 9 variants, every one but wither- and azaleabloom |
| `terralith:skylands_spring` | 75% | onionbloom 21, rosebloom 16, sunbloom 15, bluebloom 14, lilybloom 14, moobloom 13, peonybloom 7 |
| `flower_forest` | 100% | even: 9 variants, every one but wither- and azaleabloom |
| `sunflower_plains` | 100% | sunbloom 75, moobloom 25 |

<!-- gen_moobloom_rules:4.9 begin -->
**Flowery (25%):**

| Biomes | Variants, % of the cows that spawn |
|---|---|
| `terralith:temperate_highlands` | lilybloom 11, rosebloom 8, sunbloom 5, onionbloom 1 |
| `birch_forest` | lilybloom 12, oxeyebloom 9, peonybloom 2, rosebloom 2 |
| `terralith:highlands` | oxeyebloom 7, onionbloom 6, lilybloom 5, bluebloom 4, peonybloom 3 |
| `terralith:forested_highlands` | rosebloom 12, bluebloom 5, lilybloom 4, oxeyebloom 4 |
| `terralith:hot_shrubland` | sunbloom 9, moobloom 7, rosebloom 7, lilybloom 2 |

**Some flowers (2%):**

| Biomes | Variants, % of the cows that spawn |
|---|---|
| `terralith:snowy_maple_forest` | rosebloom 1, oxeyebloom 0.7, lilybloom 0.3 |
| `windswept_forest` | bluebloom 0.8, oxeyebloom 0.4, lilybloom 0.4, rosebloom 0.3, onionbloom 0.1 |
| `terralith:yosemite_lowlands` | lilybloom 0.8, bluebloom 0.5, rosebloom 0.3, moobloom 0.2, oxeyebloom 0.2 |
| `terralith:cold_shrubland`, `terralith:rocky_shrubland` | lilybloom 0.6, oxeyebloom 0.5, moobloom 0.4, rosebloom 0.3, sunbloom 0.2 |
| `plains` | oxeyebloom 0.5, moobloom 0.4, rosebloom 0.4, bluebloom 0.3, sunbloom 0.2, lilybloom 0.2 |
| `bamboo_jungle`, `jungle`, `sparse_jungle`, `terralith:amethyst_canyon`, `terralith:amethyst_rainforest`, `terralith:jungle_mountains`, `terralith:rocky_jungle`, `terralith:tropical_jungle` | orchidbloom 0.8, rosebloom 0.7, sunbloom 0.3, peonybloom 0.1, moobloom 0.1 |
| `terralith:alpine_highlands` | oxeyebloom 1, bluebloom 0.7, onionbloom 0.2, rosebloom 0.1 |
| `terralith:yellowstone` | bluebloom 1, lilybloom 0.6, rosebloom 0.4 |
| `terralith:ice_marsh` | oxeyebloom 0.9, orchidbloom 0.6, lilybloom 0.5 |
| `dark_forest` | lilybloom 0.9, peonybloom 0.4, rosebloom 0.4, onionbloom 0.3 |
| `forest` | lilybloom 0.8, rosebloom 0.6, peonybloom 0.4, oxeyebloom 0.2 |
| `savanna`, `terralith:arid_highlands`, `terralith:brushland`, `terralith:savanna_badlands` | sunbloom 0.8, rosebloom 0.8, moobloom 0.4 |
| `old_growth_pine_taiga`, `old_growth_spruce_taiga`, `taiga`, `terralith:haze_mountain`, `terralith:lush_valley`, `terralith:shield` | bluebloom 0.9, lilybloom 0.6, rosebloom 0.5 |
| `windswept_savanna` | sunbloom 0.9, rosebloom 0.7, moobloom 0.4 |
| `savanna_plateau`, `terralith:ashen_savanna`, `terralith:fractured_savanna`, `terralith:savanna_slopes` | rosebloom 0.9, sunbloom 0.6, moobloom 0.5 |
<!-- gen_moobloom_rules:4.9 end -->

**In numbers** (area-weighted over the biomes with cows, 38.8% of the surface, equal cow density assumed;
recounted 2026-10-11 after the flower biome retune): about 10.4 mooblooms per 100 cows (about 13 with every flower
biome at 100%; about 4.8 under the old single rule: 7 biomes, 1.8% of the surface, 100% each). Mooblooms from cows by
variant: lilybloom 14%, onionbloom 13%, rosebloom 13%, bluebloom 12%, orchidbloom 11%, peonybloom 10%, sunbloom 9%,
oxeyebloom 9%, moobloom 8%, azaleabloom 0.2% (the rare roll; its own spawns are in lush caves, below, not cows).

- **Even splits:** "every one but wither- and azaleabloom" = the 9 flower variants in equal parts (blooming plateau
  and valley, flower forest: 11.11% each; meadow: 8.33% each).
- **Azaleabloom** is in no cow rule: its flowers count only in lush caves (2.8, not covered). It spawns in lush caves
  (below), and elsewhere comes from the rare roll, feeding (azaleas) and breeding (moobloom + bluebloom).
- **Cows added for the rules (D27):** meadow, blooming plateau and skylands spring have no cows of their own;
  `kubejs/data/creativeengineering/forge/biome_modifier/moobloom_cows.json` adds them (weight 8, groups of 4).
- **Close to a line:** swamp (0.23), windswept hills and caldera (0.23), shrubland (0.24) sit just under the 2% tier.
- **Retuning:** flower-biome rules: edit the `manual` block of `moobloom_rules.json`; tiers: its `tiers`. Then run
  the generator and `/reload`; only new spawns change. After the wild flowers change, rerun the generator (section 7).
  The jar copy follows on the next `:creativeWorldgen:` build.

**Azaleablooms in lush caves (pack, 2026-10-11):** lush caves have no cows and no other creatures, so this is a
direct spawn like the witherbloom's, not a swap:
- `kubejs/data/creativeengineering/forge/biome_modifier/azaleabloom_lush_caves.json`: `forge:add_spawns`,
  `more_mooblooms:moobloom` in `minecraft:lush_caves`, weight 10, groups of 1-3. It is the biome's only creature, so
  every creature spawn attempt there picks it (the weight only matters if another creature is added).
- a rule in `creativeengineering.json`: `minecraft:lush_caves` 100% azaleabloom (the direct-spawn variant).
- azaleabloom's `spawns_in_dark_on: ["minecraft:moss_block"]` (mod data, 4.4): moss floors pass with no grass or
  light. Other cave blocks only pass on the cow's rules (animal ground plus light; with patcherMod's
  `spawn_ground`, mob_spawns.md, that includes rooted dirt and lit moss).

How common: creatures are tried every 400 ticks, in chunks near a player, at random heights, and only while the
player's creature cap (10) has room, so a lush cave a player explores fills up over a few minutes with azaleablooms
on its moss, as long as the farm animals nearby don't fill the cap. Chunk generation never spawns them (it uses the
surface biome). Needs the jar rebuilt on 2026-10-11 (still 1.0.0): with an older build dark moss fails (only the cow's ground-and-light check is left).

**Witherblooms in soul sand valleys (R7):** the core switch pack `packs/more_mooblooms/witherbloom_spawns/`
(config `witherbloomSpawns`, on by default) holds:
- the biome tag `#more_mooblooms:spawns_witherbloom` = `minecraft:soul_sand_valley` (a data pack can add biomes)
- a `forge:add_spawns` biome modifier: `more_mooblooms:moobloom`, weight 1, one at a time, in that tag
- a spawn rule, `moobloom_spawns/witherbloom.json`: 100% witherbloom in that tag

A moobloom the spawner adds directly (not a swapped cow) takes its variant from its biome's rule
(`MoobloomSpawns.directVariant`, no `rareChance`), so every one there is a witherbloom. Witherbloom's
`spawns_in_dark_on` lets it spawn on soul sand or soul soil in the dark; any other variant needs grass and light
(4.3), so a data pack that adds other variants to the tag's biomes gets no mooblooms on soul blocks. How rare: like every `CREATURE` spawn, the nether tries only every 400 ticks, in
chunks near a player, and the moobloom is weight 1 against the biome's other creatures (vanilla's strider is 60). The
nether's noise settings turn off chunk-generation spawns, so no herds generate with the terrain.

### 4.10 Tags and the beef sample (O4, O5)

- **O4 in the mod (D23):** the core data adds `more_mooblooms:moobloom` to `forge:milkable`. Each compat pack adds it
  to that mod's cow tags. Only the packs of loaded mods are active, so missing mods never see a dangling tag. The
  list comes from the cow's tags in `kubejs/exported/tags/entity_type_tags_by_entity_type.json`:

  | Pack | Tags |
  |---|---|
  | core | `forge:milkable` |
  | `alexsmobs` | `alexsmobs:passive_land_animals` and 7 `alexsmobs:*_targets` (anaconda, crimson mosquito, crocodile, froststalker, komodo dragon, snow leopard, tiger) |
  | `supplementaries` | `supplementaries:eats_fodder`, `supplementaries:cage_baby_catchable` |
  | `abnormals_delight` | `abnormals_delight:scavenges_leather` |
  | `botania` | `botania:cocoon/common` |
  | `delightful` | `delightful:fatty_animals` |

  Consequence: Alex's Mobs predators hunt mooblooms, the same as cows. Rerun the tag dump after adding mods and
  extend the lists.
- **O5 is in the pack** (the syringe is a pack item): one line in
  [sample_syringe.js](../kubejs/startup_scripts/func/sample_syringe.js):
  `SAMPLES['more_mooblooms:moobloom'] = { name: 'Moobloom', filledId: 'creativeengineering:cow_sample_syringe' }`.
  The cow syringe makes `culture_beef`.

### 4.11 The Buzzier Bees set (O14; only when `buzzier_bees` is loaded)

Every change below is ours: new files, events, or vanilla-class mixins. None of it touches the BB jar (4.0).

**Config switches (D26):** one per change, all on by default, in `[buzzier_bees]`: `disableMoobloomSpawns`,
`restoreCowSpawns`, `removeMooblooms`, `emptyHostilesTag`, `hideSpawnEgg`, `replaceTwoByTwo`,
`removeSunnyFromAllEffects`, `removeButtercupWorldgen`, `removeButtercupTrade`, `removeButtercupRecipes`,
`removeButtercupTags`, `removeButtercupCompost`, `hideButtercup`, `disableSunny`. Data overrides can't read a config
at load, so each data switch has its own sub-pack (`packs/buzzier_bees/<folder>/`), added only when it is on. The
sub-packs sit above BB's mod resources (they replace BB's files by path) and below the pack's `kubejs/data`.

**BB's moobloom (O3, O6):**

| What | How |
|---|---|
| Its spawns | `add_animal/moobloom.json` -> `{"type": "forge:none"}` (`disableMoobloomSpawns`); `remove_animal/cow.json` -> `forge:none` (`restoreCowSpawns`: cows return to sunflower plains, where the pack's sunflower plains rule turns them into mooblooms) |
| Existing BB mooblooms, its spawn egg, `/summon buzzier_bees:moobloom` | **removed, not converted** (D25): `EntityJoinLevelEvent` (server, HIGHEST) is cancelled for `buzzier_bees:moobloom`, so a new one never enters the world and one loaded from disk isn't added to its chunk (gone at the next chunk save). It never ticks, so the Sunny aura and BB shearing never happen. Logged (first 5, then powers of two) |
| Zombie bait | gone with the removal. Belt and braces: `moobloom_hostiles.json` with `"replace": true, "values": []` (`emptyHostilesTag`) |
| BB's spawn egg | hidden from the creative tab (mixin on vanilla `Item.allowedIn`, gated on a hidden-items set) and from JEI (our plugin) |
| Two by Two (`minecraft:husbandry/bred_all_animals`) | BB's Blueprint modifier asks for a bred BB moobloom, which can no longer exist, so the advancement would be impossible. Our own file at that modifier's path asks for `more_mooblooms:moobloom` instead (`replaceTwoByTwo`). Without BB, the `blueprint` set adds ours |

**Buttercup (O11):**

| Where it comes from | How the set closes it |
|---|---|
| Worldgen patches | `.../forge/biome_modifier/add_feature/buttercup.json` -> `forge:none` |
| Wandering Trader (BB adds 1 for 1 emerald) | `WandererTradesEvent` listener at `EventPriority.LOWEST` drops listings whose offer is a buttercup. The pack's MoreJS `wandererTrades` runs later at server start, so it sees the trimmed pool |
| Recipes | our own file at each path holding only a `forge:false` condition: `buzzier_bees:dyes/yellow_dye_from_buttercup`, `create:milling/compat/buzzier_bees/buttercup`, `abnormals_delight:buzzier_bees/cutting/buttercup` (overriding a missing mod's path is harmless) |
| Suspicious stew (the Sunny stew), flower boxes, planters, Vinery and so on | **Forge 1.19.2 tag files have no `remove` list** (its `TagFile` is entries + replace only), so this is code: `mixin/TagLoaderStripMixin` (vanilla `TagLoader.build`, RETURN) takes `buzzier_bees:buttercup` out of **every** item tag after all tags (KubeJS's included) are built, nested ones too: `minecraft:flowers`, `minecraft:small_flowers` (the stew recipe), `supplementaries:flower_box_plantable`, `another_furniture:planter_box_placeables`, `vinery:small_flower`, `creativecompat:physics/floats` (6 item tags on the scratch server) |
| Block tags | the same mixin strips the buttercup and potted buttercup from every block tag (8 tags, the pack's `creativeengineering:ground_match/plants` included, so no pack-side edit is needed) |
| Composting (BB registers 0.65 at setup) | removed from `ComposterBlock.COMPOSTABLES` in our `enqueueWork` and again at `ServerAboutToStartEvent`. In this pack `global.compost` rebuilds the table anyway, without a buttercup row |
| Creative tab and JEI | the same hidden-items set as BB's egg: creative mixin + JEI plugin |
| Buttercups already placed in old chunks, potted buttercups | ignored (D20) |
| Bone meal duplication (BB's event works on any flower block) | only on placed buttercups |

**Sunny (O12, R4):** with BB's moobloom removed and the buttercup gone, nothing in the pack gives Sunny.
`disableSunny` turns the effect itself off:

| Where | How |
|---|---|
| Applying it at all | a `MobEffectEvent.Applicable` listener (HIGHEST) returns DENY for `buzzier_bees:sunny`: potions, stews, mobs and `/effect give` ("Unable to apply this effect") all fail |
| Sunny already on an entity | removed in `EntityJoinLevelEvent` (players at login, mobs loaded from disk), with a log line |
| BB's undead burn check | `mixin/MobSunBurnMixin` on vanilla `Mob.isSunBurnTick` (`m_21527_`), TAIL, cancellable, **priority 900**. BB's `MobEntityMixin` injects at the same TAIL with the default 1000, and Mixin applies the lower priority first, so our callback runs first. For a mob that has Sunny it returns false (what vanilla returns there) and BB's callback never runs. It cancels only for mobs with Sunny, so other mods' callbacks there (Breezy injects at RETURN) are untouched. Order checked in the exported class (4.14) |
| Moobloom aura, buttercup stew | never run (removal, tags) |
| JEED effect page | the `jei` set removes any JEI ingredient of class `MobEffectInstance` whose effect is Sunny (no compile dependency on JEED), and the pack sets `hidden_effects = ["buzzier_bees:sunny"]` in [jeed-client.toml](../config/jeed-client.toml) (+ configs_edited) |
| How Did We Get Here | BB's advancement modifier adds Sunny to it; `removeSunnyFromAllEffects` overrides that file so the advancement stays possible |
| BB's other Sunny code (phantom drop, bees working at night) | inert when nobody has the effect. Not patched (no mixins into BB, 4.0) |

Unregistering Sunny was considered (D21) and rejected: BB's own mixin calls `hasEffect(SUNNY.get())` from every
`Mob.isSunBurnTick`, so an unregistered Sunny would crash every undead mob's tick, and stopping BB's registration
would mean patching BB.

### 4.12 What's in the pack (CreativeEngineering)

| Change | Where |
|---|---|
| Install | jar in `mods/` (`installToMods`), `mod_sides.md` row (both sides), `IN_TREE_MODS` row in `tools/build_deploy.py`, root `settings.gradle` |
| Spawning (D12, D24, D27, D47) | `kubejs/data/more_mooblooms/moobloom_spawns/creativeengineering.json` (35 per-biome rules plus lush caves, 4.9), `kubejs/data/creativeengineering/forge/biome_modifier/moobloom_cows.json` (cows, weight 8 in groups of 4, in meadow, blooming plateau and skylands spring), `.../azaleabloom_lush_caves.json` (mooblooms in lush caves, weight 10, groups of 1-3), `rareChance = 0.002` |
| Config | `config/more_mooblooms-common.toml` (+ `configs_edited/`, + the server's `configs_edited/`) |
| Beef sample (O5) | `sample_syringe.js`, one line |
| item_dict | `i.more_moobloom_spawn_egg`; `i.buttercup` and `i.moobloom_spawn_egg` (BB's) kept for scripts that name them |
| JEED | `hidden_effects = ["buzzier_bees:sunny"]` (+ `configs_edited/`) |
| Quark Automatic Tool Restock (R3) | `more_mooblooms:moobloom_spawn_egg` in "Ignored Items" of `config/quark-common.toml` (+ `configs_edited/`, + the server's) |
| Tags, compost | nothing: TagLoaderStripMixin strips the buttercup from every tag (the pack's included), and `compost_values.js` has no buttercup row |

### 4.13 Decisions

| # | Question | Answer |
|---|---|---|
| D1 | patcherMod or its own mod? | its own mod, More Mooblooms (O13) |
| D2, D42 | Display names | the name alone: "Sunbloom", "Rosebloom"; the eggs "<Name> Spawn Egg", and the moobloom variant's egg reads "Moobloom Spawn Egg", the same as the plain egg |
| D3 | Tall flowers: which half? | the top half (`[half=upper]`), the bloom |
| D4 | Azalea bushes at half scale? | yes, azaleabloom `scale` 0.5 |
| D5, D43 | Wither roses with endless regrowth | witherbloom has `any_weight` 0 (never wild, never random) and its wither rose drops at `chance` 0.1 (about 0.3 per shear) |
| D6 | `feedChance`; flower used on a failed try; babies convert? | 10%, used, yes. A variant's flowers (any of its pool, plus `feed`) also breed adults and grow calves |
| D7 | Can a moobloom switch variant when fed another flower? | no; feeding only acts on exact `minecraft:cow` |
| D8 | What does shearing a bare moobloom do? | turns it back into a cow and drops flowers again; both parts are config (`shearBareToCow`, `bareShearDrops`) |
| D9 | `rareChance`; also on cows a rule leaves? | mod default 0, the pack 0.002; yes |
| D10 | Roll per cow or per herd? | per cow |
| D11 | Rule precedence | exact biome id, then the first tag rule (file name, then order); the same for breeding rules |
| D12 | This pack's biome rules | 2026-10-08: per-biome rules from the wild flowers, in three tiers (4.9, D47). Was: one rule, 100% random mooblooms in the 7 flower-variety biomes |
| D13 | Keep Sunny on the stew? | no (O12) |
| D14 | Spawn eggs: one item + variant NBT, or one item per variant? | one item + NBT (variants are data, so they can't each register an item); every egg visible in JEI |
| D15 | Default and fallback variant | `moobloom`, with our own yellow coat (BB's can't ship) |
| D17 | `regrowChance` per meal; grazing calves age up faster? | 0.25, yes (60 s per meal) |
| D18 | Sheep grazing head-bob on the cow model? | yes |
| D19 | Three flower positions or more? | three, tuned in game |
| D20 | Old placed and potted buttercups | ignored: there shouldn't be any |
| D21 | Make Sunny do nothing by unregistering it? | no: it would crash undead mobs (4.11). R4's `disableSunny` denies it instead |
| D22 | Mod name and id | More Mooblooms, `more_mooblooms` |
| D23 | Cow tags | fixed lists in the mod's compat packs (4.10) |
| D24 | Mod default spawn rules | no cow rules, `rareChance` 0: cow spawning is the pack's. The witherbloom spawns (R7) are the mod's, behind their own switch |
| D25 | Convert BB mooblooms? | no: removed entirely, they shouldn't be in the game |
| D26 | Config switches for the BB set | one per change, all on by default |
| D27 | Blooming plateau and meadow are flower biomes with no cows | the pack adds cows to both |
| D29 | Should "any" favour the flowers that grow in the biome? | 2026-10-08: yes, as explicit per-biome variant percents from 2.8 rather than a weighted "any" (4.9). The rare roll stays flat |
| D30 | Pool flowers, weights and parents | the variant table (4.4), the user's |
| D32 | Breeding food: each parent its own flowers, or any flower? | each its own |
| D33 | Random calf: weighted over every variant, or one of the parents? | weighted by `breed_weight`; witherbloom 0 |
| D34 | Two parents of the same variant breed true? | yes, after any combination for that pair |
| D35 | Default combination `chance` | 0.5; a miss falls through to D34/D33 |
| D38 | First shear: 1 per grown flower (3 per shear) | yes, `chance` 1.0 by default |
| D39 | `disableSunny` default | on |
| D44 | Second (bare) shear: how many pool picks? | 3, one per position |
| D45 | Wild onions: food only, or in onionbloom's pool? | in the pool, optional (R5) |
| D46 | Regrowth: a fresh roll, or the same three flowers back? | a fresh roll |
| D47 | How many cows become mooblooms, per biome? | by pool-flower patches per chunk (2.8): 1.0 or more 100%, 0.5 to 1.0 25%, 0.25 to 0.5 2%, otherwise only the rare roll. Mix shares under 5% dropped. Claude's first pass, open to retuning |
| D48 | Flower forest and sunflower plains fall in the 25% tier by the numbers (0.64, 0.63) | kept at 100% by name: they are the named flower biomes, and sunflower plains is Buzzier Bees' moobloom biome. Old growth birch forest (8.3, the densest) is in, although the old rule left it out |
| D49 | Flower biomes: all 100%? | no, the user's per-biome bloom % and variant splits (2026-10-08): 100% only in blooming plateau/valley, flower forest and sunflower plains; 25% to 75% elsewhere (4.9 table) |
| R6 | A flower in several pools | random variant by default (`feed_weight` 1 each); dandelion 90% moobloom, 10% sunbloom |

### 4.14 Implementation notes and troubleshooting

**Things that weren't obvious:**
- **No tag `remove` in Forge 1.19.2.** The buttercup comes out of tags through a mixin into vanilla `TagLoader`
  (4.11), applied to every tag after KubeJS's edits.
- **`@ModifyArg`, not `@Redirect`, for the spawn swap:** Botania already has a `@ModifyArg` on the same
  `addFreshEntityWithPassengers` call in `spawnCategoryForPosition`, and a Redirect there would clash with it.
- **`/summon` with NBT skips `finalizeSpawn`**, so loading an entity tag without `Variant` also picks one, and
  `Flowers` is checked on every load.
- **The variant has to exist before the spawn checks:** both spawn paths in `NaturalSpawner` run the type-level
  placement check, create the entity, call `Mob.checkSpawnRules` on it (positioned), then `finalizeSpawn`. A
  per-variant spawn rule therefore can't live in the placement predicate (no entity yet) or in `finalizeSpawn` (too
  late). The placement predicate is a coarse filter (any variant's dark blocks), and `Moobloom.checkSpawnRules`
  picks the variant and makes the real decision.
- **Natural spawning asks `getWalkTargetValue`:** vanilla's `Mob.checkSpawnRules` needs it to be 0 or more. For
  animals it is 10 on grass and light-based elsewhere, so in a dark soul sand valley it is negative. The moobloom
  returns true for its variant's dark blocks before that check, and scores them like grass for wandering.
- **Cow feeding picks the variant from a copy of the stack:** the flower is used up before the pick, and the last
  one in a stack would otherwise read as empty (always the first variant).
- **The sync packet goes out before the recipe and tag packets**, so JEI already has every variant when it builds
  the egg list and info pages.
- **Mixin order for Sunny:** checked with `-Dmixin.debug.export=true -Dmixin.debug.export.filter=net.minecraft.world.entity.Mob`
  and `javap` on `.mixin.out/class/net/minecraft/world/entity/Mob.class`: in `m_21527_` our handler is called at
  offset 180, BB's `isInDaylight` at 218, Breezy's `balloonRefuge` at 256.

**R3, eggs "swapping" variant: Quark's Automatic Tool Restock.** Nothing in the mod writes to an egg stack. Restock
(on in `config/quark-common.toml`: "Unstackables Only" false, "Enable Loose Matching" true) refills an emptied hotbar
slot from the main inventory with the same item and ignores NBT. Every variant egg is the same item, so when the
last egg of one variant is used, Quark puts whichever moobloom egg it finds first into the slot. Ruled out:
Blueprint's `SpawnEggItemMixin` (it only sorts `minecraft:` eggs in the creative tab, and our item overrides
`fillItemCategory`). The mod can't fix this itself, because Quark matches by item; the pack lists the egg in Quark's
"Ignored Items" (4.12). To see the swap again (MB-72), take the egg out of that list.

**Scratch-server testing (dedicated server, full pack, no player):**
- A level with no players stops ticking entities after 300 ticks: `/forceload add ~ ~` before testing breeding or
  anything else that needs AI.
- Console commands queued together run in the same tick, and a dispenser fires 4 ticks after it is powered: wait
  between powering a dispenser and reading its drops.
- KubeJS/Rhino: `.unwrap()` on a Java object is Rhino's own wrapper method, so `WeightedRandomList.unwrap()` can't be
  called from a script (biome spawn lists look empty that way); `Entity.moveTo(x, y, z, yaw, pitch)` isn't found
  either (use `setPos`); a `const` inside a block in a command handler can fail with "redeclaration of var" (see
  the Rhino gotchas note).
- Log noise that isn't ours: `Couldn't read tag list minecraft:mineable/hoe ... redeco` (a bad tag file in
  ReDeco, in client logs too), Forge's "Incorrect key ... was corrected" for a new config key.

**Verified on a scratch dedicated server (2026-10-07, full pack + the 1.0 jar, Buzzier Bees and Blueprint present):**
- boots (56-63 s); all four mixins apply; 11 variants, 10 breeding rules from 7 files, 3 spawn rules (the pack's,
  the witherbloom pack's, a scratch test rule); the `more_mooblooms:witherbloom_spawns` pack is enabled with no
  biome-modifier errors
- buttercup stripped from 6 item tags, buttercup + potted buttercup from 8 block tags; 1 Wandering Trader listing removed
- `/datapack list`: the `more_mooblooms:buzzier_bees/*` sub-packs sit above `mod:buzzier_bees`; the compat packs load
- `/summon` with no `Variant` picks one; an unknown variant -> warning + moobloom; `/summon buzzier_bees:moobloom` ->
  removed
- `Flowers`: three entries from the variant's pool; a given list with one flower not in the pool -> that position
  re-rolled; calves and bare mooblooms have none; a calf rolls three when it grows up
- `/effect give ... buzzier_bees:sunny` -> "Unable to apply this effect"; Speed still applies
- dispenser shearing: first shear dropped exactly the flowers shown (2 poppies, 1 red tulip), it stayed a bare
  moobloom; second shear -> a cow with the same name and 3 pool drops
- breeding (force-loaded): two roseblooms -> a rosebloom calf; a scratch combination rule added with `/reload` ->
  its result
- spawn checks (script, scratch rule for the biome): with a lilybloom rule, soul sand and soul soil pass the
  placement filter but `checkSpawnRules` fails, grass passes; with a witherbloom rule at midnight, soul sand and soul
  soil pass; stone always fails; the variant is the rule's
- spawn swap at chunk generation (earlier build, same mixin): every moobloom generated in the flower-variety biomes,
  mixed variants, no witherbloom; every cow outside the tag
- not checked yet (need a client): rendering and flower positions, JEI pages and the breeding category, eggs,
  feeding and the dandelion split, grazing and regrowth, advancements, a witherbloom actually spawning in a soul sand
  valley, ongoing natural spawns (need a player nearby)

## 5. JEI changes

**1.1.0** (BUILT 2026-10-22, plan and decisions: [briefs/mobs_mooblooms.md](briefs/mobs_mooblooms.md) #2). All in
`compat/jei/`; JEI reads the client's synced variant and breeding tables (4.14).

- **Info page** per variant egg, short: "Grows: <pool odds>.", the cow feed line with the config chance, shears take
  the flowers and grass regrows them, the bare shear (only with `shearBareToCow`; "+3 flowers" with
  `bareShearDrops`), "Breeds and grows on its flowers, not wheat.", "Never a random calf." for `breed_weight` 0. The
  1.0 drop-chance and "Bred from" lines are gone: the two categories show them.
- **Moobloom Breeding** (`MoobloomBreedingCategory`, 150x40): parent egg + parent egg, each parent's food below it
  (pool items and feed tags, cycling), -> every outcome with its % under the slot. Pages, in order:
  - each combination rule that applies (an earlier rule for the same pair wins): each result at rule chance x
    result share, then the miss at `1 - chance`: the same kind for a same-kind pair (moobloom + moobloom: sunbloom
    10 %, moobloom 90 %), otherwise a random-calf slot;
  - "breeds true" (A + A -> A, 100 %) for every variant with no rule for itself;
  - one random-calf page: two plain eggs (tooltip "Any two different kinds with no combination") -> one slot
    cycling every variant with `breed_weight > 0`; its tooltip gives the kind's share and the overall %.
  Three outcome slots fit; a rule with more than two results would overflow the page.
- **Moobloom Shearing** (`MoobloomShearingCategory`, 150x56, catalyst shears): one page per variant, its egg ->
  each pool item (entries with the same item merged, highest first) with tooltip "<n> per shear (<p>% per spot)",
  p = sum of weight x chance / pool weight, n = 3p (both shears, 4.6); with `shearBareToCow` a cow spawn egg slot
  ("Shear it bare: back to a cow"). The page renders a client-side moobloom (one per recipe, made again when the
  level changes) with JEI's PoseStack, turning with the mouse; position i shows pool entry (i + gameTime/30) % size,
  so every flower appears. `Moobloom.setDisplayFlowers` (public wrapper of the private setter) exists for this only.
- **Moobloom Conversion** (`MoobloomConversionCategory`): one page per variant that has a cow-conversion food, a
  plain cow (calf or adult; a calf stays a calf) + the variant's food -> the moobloom rendered with its flowers.
  Tooltips give the chance per food item fed (each try uses one) and, for foods several variants share (the
  dandelion split: moobloom 90 % / sunbloom 10 %), this kind's share of the conversions. Lang keys
  `jei.more_mooblooms.conversion*`. Test: JW-15 in [jei_world_tabs.md](jei_world_tabs.md); MB-104 below.
- **Just Enough Breeding's BB page**: JEBr hardcodes a `buzzier_bees:moobloom` + wheat page keyed on BB's egg. With
  BB loaded, `onRuntimeAvailable` hides every `justenoughbreeding:breeding` recipe with BB's egg as input or output
  (lookup by recipe type id, no compile dependency). Whether the page showed before is unverified (MB-103).
- **Install:** `moreMooblooms-1.1.0.jar` (md5 c2df0ab6, built 08:58, without the Conversion category) was installed into
  `mods/` in place of 1.0.0 for the 2026-10-22 scratch run. The jar was then rebuilt in place at the same version
  (md5 3440dbc0, 16:58, with Conversion); it still has to be copied to `mods/` (`:moreMooblooms:installToMods`).
  Two different jars carry the name 1.1.0, so bump the version before the next deploy if the earlier one could have
  reached anyone; `server_mods/` still holds 1.0.0 until the next deploy. Same jar on the server: nothing in
  1.1.0 changes server behaviour, and it boots there (scratch server PASSED 2026-10-22, SQ-GM-09: `Done (`, no error naming
  `more_mooblooms` or `compat.jei`).
  MB-100 to MB-104 are client only (JEI).
- **Sunflower turned (2026-10-26, `mobs/mooblooms #6`):** the upper sunflower half is one-sided and showed its back on the
  JEI pages; `MoobloomFlowerLayer` turns a sunflower slot 180 degrees (world and JEI alike). Rebuilt in place as 1.1.0
  (third jar of that name, so bump the version before a deploy); not in `mods/`. MB-105.

### Planned from the scratchpad (2026-10-24)

Brief: [briefs/mobs_mooblooms.md](briefs/mobs_mooblooms.md). Nothing here is built.

- [TODO](todo/TODO.md) `mobs/mooblooms #6` (bug): the sunflower of a sunbloom faces backwards on the Moobloom Conversion JEI tab. The tab draws the mob
  with `MoobloomShearingCategory.renderEntity` and the flower layer rotates each slot by `slot.yaw()`; the sunflower model faces one
  fixed way. Check whether the world render is also backwards, then add 180 degrees for sunflower slots (layer or tab only).

## 6. File map

| What | Where |
|---|---|
| More Mooblooms | `development/moreMooblooms/` (README); Java in `src/main/java/com/SaintAlfonzo/more_mooblooms/`: `entity/` (Moobloom, CowFeeding, MMEntities), `variant/` (MoobloomVariant, MoobloomVariants, MoobloomBreeding), `spawn/MoobloomSpawns`, `item/MoobloomSpawnEggItem`, `client/` (renderer, model, flower layer), `compat/` (BuzzierBeesSet, `jei/`), `mixin/` (NaturalSpawnerMixin, TagLoaderStripMixin, MobSunBurnMixin; ItemHiddenMixin plus `shared/` (BuiltInPacks, GatedMixinPlugin, HiddenItems) are generated from `development/shared/`, [pack_tooling.md](pack_tooling.md) 2.8), `network/SyncVariantsPacket`, `MMConfig` |
| Mod data | `src/main/resources/data/more_mooblooms/` (`moobloom_variants/`, `moobloom_breeding/`, `loot_tables/`, `tags/blocks/spawnable_on_in_dark.json`); `src/main/resources/packs/` (`more_mooblooms/witherbloom_spawns/`, `buzzier_bees/<switch>/`, one folder per compat mod) |
| Coats | `src/main/resources/assets/more_mooblooms/textures/entity/moobloom/<variant>.png`; working copies and the flower crops in `textureswapper/moobloom/` (a local folder, not in the git repo) |
| Generators | `tools/gen_moobloom_variants.py` (variant table -> variant and breeding files), `development/moreMooblooms/tools/egg_colors.py` (egg colours from the coats) |
| Checks | `tools/check_moobloom_data.py` |
| Config | `config/more_mooblooms-common.toml` (+ `configs_edited/root_config/`) |
| Pack side | [sample_syringe.js](../kubejs/startup_scripts/func/sample_syringe.js), `kubejs/data/more_mooblooms/moobloom_spawns/creativeengineering.json`, `kubejs/data/creativeengineering/forge/biome_modifier/moobloom_cows.json` and `azaleabloom_lush_caves.json`, [jeed-client.toml](../config/jeed-client.toml), [quark-common.toml](../config/quark-common.toml) (Ignored Items), [item_dict.js](../kubejs/startup_scripts/_lib/item_dict.js) (`i.more_moobloom_spawn_egg`, `i.moobloom_spawn_egg`, `i.buttercup`) |
| Buzzier Bees jar | `mods/buzzier_bees-1.19.2-5.0.1.jar` (needs Blueprint 6.2.0); decompile with `python3 tools/decompile_mod.py mods/buzzier_bees-1.19.2-5.0.1.jar <out>` |
| BB entity, render, Sunny | `com/teamabnormals/buzzier_bees/common/entity/animal/Moobloom.java`, `client/render/entity/MoobloomRenderer.java`, `client/render/entity/layers/MoobloomFlowerLayer.java`, `core/mixin/MobEntityMixin.java` (`Mob.isSunBurnTick` TAIL), `core/other/BBEvents.java` (hostile goal, phantom repel, bone meal) |
| BB spawns, loot, config | jar `data/buzzier_bees/forge/biome_modifier/`, `data/buzzier_bees/tags/worldgen/biome/`, `data/buzzier_bees/loot_tables/entities/moobloom.json`; [buzzier_bees-common.toml](../config/buzzier_bees-common.toml) |
| BB license | `https://github.com/team-abnormals/buzzier-bees/blob/main/LICENSE.txt` (Abnormals License 1.0, quoted in 4.0) |
| Flower table (2.8), spawn rules (4.9) | `tools/gen_moobloom_rules.py` (since 2026-11-07), input `kubejs/generation_scripts/worldgen/moobloom_rules.json` (tiers, hand-set `manual` rules); earlier tables were hand translations of `tools/gen_wild_flowers.py`'s `inventory()` |

## 7. Offline checks

- `python3 tools/check_moobloom_data.py`: variants (pool entries, weights, chances, `feed_weight`, feed ids, weights,
  egg colours, texture, lang), the default variant, breeding rules (known parents and results, a pair claimed twice),
  spawn rules (biome ids against `kubejs/exported/biome_data.json`, known variants, sums of 100 or less). Prints the
  split for every flower that is food for several variants. Exit code 1 on a problem.
- `python3 tools/gen_moobloom_variants.py --check`: the shipped data matches the variant table (exit code 1 if a
  file would change).
- `python3 development/moreMooblooms/tools/egg_colors.py --check`: the egg colours match the coats.
- The moobloom's entity tags: `python3 -c "import json;d=json.load(open('kubejs/exported/tags/entity_type_tags_by_entity_type.json'));print(d['more_mooblooms']['more_mooblooms:moobloom'])"`
  (refresh with `/dumptags`).
- No BB material in our jar: `unzip -l development/moreMooblooms/build/libs/moreMooblooms-*.jar | grep -i buzzier`
  lists only our `packs/buzzier_bees/` override files, and none of them is a copy of a BB file.
- The BB jar is the unmodified public copy: `md5sum mods/buzzier_bees-1.19.2-5.0.1.jar` matches the CurseForge
  download (license, 4.0).
- `python3 tools/gen_moobloom_rules.py --check`: the rule file and the 2.8 / 4.9 tables match the wild flowers
  (exit code 1 if a file would change). Without `--check` it rewrites them; also in `/cdevgen` (`moobloom_rules`,
  dump `dumpspawns`). Built 2026-11-07 (flowers #2): its first run reproduced the hand-written rules (only the
  lavender onionbloom 53.06 rounding needed a rule: a rounded split over its bloom % comes off the largest share);
  the 2.8 tables picked up the newer biome areas and per-1000 numbers.
- Spawn rules: `python3 tools/check_moobloom_data.py` checks biome ids, variants and sums (36 rules on 2026-10-08:
  the pack's 35 plus the witherbloom pack's; 37 since 2026-10-11 with lush caves).

## 8. In-game test suite

MB-01 to MB-12 tested Buzzier Bees' own moobloom, which More Mooblooms removes, and are retired, as are MB-23, MB-28,
MB-50, MB-57 and MB-75. Tick a test once it has passed in a client; `[x]` with "scratch" passed on a scratch server.

**The entity and variants**
- [ ] **MB-74 Variants render:** `/summon more_mooblooms:moobloom ~ ~ ~ {Variant:"<id>"}` for each of the 11 -> its coat, three flowers from its pool; sunflower, peony, lilac and rose bush show their top halves; azaleabloom's bushes at half size
- [ ] **MB-20 Flower positions:** the three flowers sit on the back (front, rear) and the head, and the head one moves with the head; tune `SlotPose` if not
- [ ] **MB-24 Save/reload:** relog -> variants, bare state and each moobloom's flowers unchanged
- [ ] **MB-25 Names:** WTHIT and a death message show the variant name ("Rosebloom")
- [x] **MB-70 Bare /summon:** `/summon more_mooblooms:moobloom ~ ~ ~ {NoAI:1b}` -> still gets a random variant (scratch)
- [x] **MB-44 Bad data:** an unknown `Variant` -> a warning in latest.log and the default variant (scratch); also try an unknown biome and a rule summing 120% -> warnings, everything else loads
- [x] **MB-45 Offline:** `python3 tools/check_moobloom_data.py` passes
- [ ] **MB-86 Pool spread:** summon 20 rosebloom -> mixed flowers, roughly 50% rose bush, 40% poppy, 10% red tulip; relog -> each keeps its flowers; graze-regrow one -> a fresh roll
- [x] **MB-89 Flowers NBT:** `/summon ... {Variant:"more_mooblooms:rosebloom",Flowers:["minecraft:poppy","minecraft:dandelion","minecraft:rose_bush[half=upper]"]}` -> the dandelion position is re-rolled from the pool, the other two kept (scratch)

**Spawn eggs and JEI**
- [ ] **MB-71 JEI eggs:** JEI search "moobloom" -> 11 eggs named "<Name> Spawn Egg" in their colours, each with an info page (pool odds, food, shearing); middle-click a moobloom in creative -> its own egg
- [ ] **MB-81 JEI breeding:** JEI uses of the rosebloom egg -> "Moobloom Breeding" pages, e.g. rosebloom + lilybloom -> peonybloom 35%
- [ ] **MB-100 JEI shearing:** JEI uses of shears -> "Moobloom Shearing", 11 pages; rosebloom shows rose bush 1.5, poppy 1.2, red tulip 0.3 per shear; witherbloom wither rose 0.3; the rendered moobloom cycles through every pool flower and turns with the mouse; the cow egg slot appears (and is gone with `shearBareToCow = false` after a restart)
- [ ] **MB-101 JEI breeding outputs:** rosebloom egg -> pages for rosebloom + rosebloom -> rosebloom 100%, rosebloom + lilybloom -> peonybloom 35% / random 65%, the random-calf page (no witherbloom); each parent shows its food cycling; moobloom + moobloom -> sunbloom 10% / moobloom 90%
- [ ] **MB-102 Info text:** each egg's info page reads in a few short lines, numbers match the config
- [ ] **MB-103 No BB moobloom in JEI:** JEI search "moobloom" and BB's egg via recipe lookup -> no Just Enough Breeding page for Buzzier Bees' moobloom
- [ ] **MB-105 Sunflower faces forward (2026-10-26):** JEI -> Moobloom Conversion and Shearing pages for the sunbloom: the
  sunflower heads show their face (yellow petals, brown disc), not the green back. In the world, a sunbloom seen from
  the front shows the faces too.
- [ ] **MB-104 JEI conversion:** JEI uses of Dandelion (or a cow spawn egg's recipes) -> "Moobloom Conversion": a cow + dandelion -> moobloom 90 % / sunbloom 10 % shares in the hover, chance per food item, the rendered moobloom; a calf note on the cow slot (same as JW-15)
- [ ] **MB-72 Egg swap:** survival; a Rosebloom egg in hotbar slot 1, a Lilybloom egg in the main inventory; use the last Rosebloom egg -> slot 1 stays empty (Quark ignores the egg). With the egg taken out of Quark's "Ignored Items" -> slot 1 refills with the Lilybloom egg
- [ ] **MB-73 Egg on a moobloom:** right-click a lilybloom with a rosebloom egg -> a rosebloom calf; a plain egg -> a lilybloom calf

**Feeding and food**
- [ ] **MB-29 Feeding:** feed a cow poppies -> one used per try; at about `feedChance` it becomes a rosebloom
- [ ] **MB-30 Feeding keeps state:** a named calf cow converts -> same name, still a calf, no flowers
- [ ] **MB-31 Feeding misses:** wheat on a cow (normal cow use), a poppy on a mooshroom, a poppy on a lilybloom -> no conversion, poppy kept
- [ ] **MB-80 One flower, its variants:** feed cows each of the 17 vanilla flowers -> each converts to the variant whose pool holds it (orange tulip -> sunbloom, azure bluet -> oxeyebloom ...)
- [ ] **MB-90 Shared flowers:** convert about 20 cows with dandelions -> about 90% moobloom, 10% sunbloom; with pink tulips -> about half onionbloom, half peonybloom
- [ ] **MB-68 Flower food:** a lilybloom ignores wheat, follows and breeds on lily of the valley or white tulips; a calf fed one grows faster
- [ ] **MB-88 Wild onions:** with Farmer's Delight: onionblooms sometimes grow wild onions; shearing one drops them; wild onions convert a cow to onionbloom, breed two onionblooms and grow an onionbloom calf. Scratch server without Farmer's Delight (MB-59) -> onionbloom loads with allium and pink tulips only, no errors

**Shearing and regrowth**
- [x] **MB-85 First shear = carried flowers:** dispenser shears on a grown moobloom -> exactly the flowers in its `Flowers`, it stays a bare moobloom (scratch: 2 poppies + 1 red tulip)
- [ ] **MB-22 Player shears:** shear each grown variant by hand -> its carried flowers; it stays a moobloom with a bare back
- [x] **MB-46 Bare state:** shearing a bare moobloom -> explosion puff, it becomes a cow (same name, health, age, leash) and three pool flowers drop (scratch, by dispenser)
- [ ] **MB-87 Bare shear by weight:** shear 10 bare roseblooms back to cows -> 3 drops each, the totals near the pool weights; witherbloom -> about 0.3 wither roses per shear
- [ ] **MB-47 Regrow:** pen a sheared moobloom on grass -> within a few minutes it eats (grass turns to dirt) and grows a fresh set of flowers
- [ ] **MB-48 No grass:** pen a sheared moobloom on dirt or stone -> stays bare
- [ ] **MB-49 Save bare:** relog with a bare moobloom -> still bare
- [ ] **MB-51 Dispenser and deployer:** a Create deployer with shears -> the same drops as a player; it stays and goes bare
- [ ] **MB-52 New mooblooms grown:** a freshly converted cow, a natural spawn and a bred calf once grown -> all show flowers
- [ ] **MB-69 Grazing animation:** watch a moobloom on grass -> it lowers its head and chews like a sheep

**Breeding**
- [ ] **MB-76 Cross-breed:** a rosebloom and a bluebloom, each fed its own flower -> they mate
- [ ] **MB-77 Combination:** breed rosebloom x lilybloom 20 times -> about a third peonybloom, the rest random
- [x] **MB-78 Breeds true:** two roseblooms -> a rosebloom calf (scratch, once); repeat a few times
- [ ] **MB-79 Random pool:** breed sunbloom x rosebloom (no combination) 20 times -> mixed variants, never witherbloom
- [ ] **MB-91 Same-variant combination:** breed two mooblooms 20 times -> about 2 sunblooms, the rest mooblooms
- [ ] **MB-63 Two by Two:** breed every animal including `more_mooblooms:moobloom` -> advancement granted (BB installed) / unaffected (BB absent, no Blueprint)

**Spawning**
- [ ] **MB-38 Flower biomes:** new chunks in blooming valley and flower forest -> no cows; lavender valley, sakura grove, orchid swamp, meadow -> about two in three cows are mooblooms (65-75%); the variants match the flowers (lavender mostly onionblooms, sakura onion- and peonyblooms, orchid swamp mostly orchidblooms), meadow an even mix of the 9 flower variants, never witherbloom or azaleabloom
- [ ] **MB-36 Sunflower plains:** new chunks there -> all mooblooms, three sunblooms to one moobloom, nothing else, no BB mooblooms
- [ ] **MB-40 Few flowers:** desert-edge, snowy plains, river-side cows -> cows, with only the rare roll
- [ ] **MB-94 Flowery tier:** highlands, birch forest, forested highlands -> about one cow in four is a moobloom; birch forest ones mostly lily- and oxeyeblooms
- [ ] **MB-95 Some-flowers tier:** count over a large area of plains or taiga (`/execute if entity @e[type=more_mooblooms:moobloom]` vs cows) -> a few in 100 cows are mooblooms; taiga ones mostly blue- and lilyblooms, plains mixed
- [ ] **MB-96 Old growth birch forest:** new chunks -> about a third of the cows are mooblooms, mostly peonyblooms
- [ ] **MB-97 Skylands spring cows:** `/locate biome terralith:skylands_spring`, new chunks -> about three in four cows are mooblooms (the pack adds the cows)
- [ ] **MB-37 Rare roll:** set `rareChance` to 1.0, new chunks in a cow biome -> every cow is a moobloom, mixed variants, never witherbloom
- [ ] **MB-39 Partial rule (format check):** a temporary `kubejs/data` rule of lilybloom 50% somewhere, then count over a large area (`/execute if entity @e[type=...]`) -> about half lilyblooms; remove the rule afterwards
- [ ] **MB-42 Both spawn paths:** new chunks (chunk generation) and spawns in loaded chunks (natural) both swap
- [ ] **MB-43 /reload:** edit a `moobloom_spawns` file, `/reload` -> the new rule applies to new spawns
- [ ] **MB-41 Untouched sources:** cow spawn egg, cow breeding, `/summon minecraft:cow` -> always cows
- [ ] **MB-92 Witherblooms in soul sand valleys:** spend time in a soul sand valley (or `/forceload` an area there and wait) -> now and then a witherbloom on soul sand or soul soil, never another variant; `witherbloomSpawns = false` and a new world -> none
- [x] **MB-93 Dark placement:** a witherbloom can spawn on soul sand / soul soil without light; another variant can't (needs grass and light); nothing on stone (scratch, by script)
- [ ] **MB-98 Azaleablooms in lush caves:** 2026-10-11 jar on both sides; explore a lush cave (or `/forceload` one and wait) -> azaleablooms appear on moss blocks, in the dark too, never another variant and never on stone or clay; with an older build none on dark moss
- [ ] **MB-67 Standalone spawning:** MB-59 server (no pack data, default config) -> cows never become mooblooms on their own; witherblooms still appear in soul sand valleys

**Buzzier Bees set and standalone**
- [ ] **MB-21 BB mooblooms removed:** load a world with an existing BB moobloom -> it is gone; `/summon buzzier_bees:moobloom` and BB's egg add nothing (latest.log: `removed a Buzzier Bees moobloom`) (scratch: `/summon` removed)
- [ ] **MB-26 No aura:** stand next to a moobloom 10 s, `/data get entity @s ActiveEffects` -> no `buzzier_bees:sunny`
- [ ] **MB-27 No bait:** a zombie at night next to a moobloom -> ignores it and doesn't burn
- [ ] **MB-53 Sunny gone:** JEI and JEED search "Sunny" -> nothing
- [x] **MB-82 Sunny denied:** `/effect give` a mob Sunny -> "Unable to apply this effect" (scratch). With `disableSunny = false` and a restart, a zombie given Sunny at night burns
- [ ] **MB-83 Old Sunny stripped:** a player saved with Sunny (switch off), then switch on and relog -> the effect is gone
- [x] **MB-84 Mixin order:** exported `Mob.isSunBurnTick` -> our handler before BB's (scratch, 4.14)
- [ ] **MB-54 Buttercup gone:** JEI and creative search "buttercup" -> nothing; no yellow dye / milling / cutting recipe
- [ ] **MB-55 No buttercup worldgen:** new chunks in sunflower plains -> no buttercup patches
- [ ] **MB-56 Trader:** reroll several Wandering Traders -> never sells buttercups
- [ ] **MB-58 Composter:** a buttercup (from an old chunk) in a composter -> not accepted
- [ ] **MB-62 Hidden items:** creative tab and JEI -> no buttercup, no BB moobloom spawn egg; our egg shows one stack per variant
- [ ] **MB-60 With BB:** + Buzzier Bees + Blueprint -> the `buzzier_bees/*` sub-packs are active; `/summon buzzier_bees:moobloom` and BB's egg (via `/give`) -> nothing appears
- [ ] **MB-61 BB spawns off:** new sunflower plains chunks -> no BB mooblooms, cows there again (subject to our rules)
- [ ] **MB-64 Pack overrides us:** a `kubejs/data/more_mooblooms/moobloom_spawns/default.json` and a variant override take effect after `/reload` (pack order: KubeJS above our packs)
- [ ] **MB-65 Compat tags:** `/dumptags` -> `more_mooblooms:moobloom` in every tag from 4.10, and no compat pack loads when its mod is missing (MB-59 server)
- [ ] **MB-66 Config switches:** turn off `removeMooblooms` -> BB mooblooms can be summoned again; turn it back on -> removed. Turn off `removeButtercupRecipes` -> the yellow dye recipe returns after restart; the other removals stay
- [ ] **MB-59 Standalone:** scratch server with Forge + More Mooblooms only -> boots; feeding, shearing, regrowth, breeding work; `/datapack list` shows only `more_mooblooms:witherbloom_spawns`, no BB or compat packs

**Pack side**
- [ ] **MB-32 Fodder and cage:** Supplementaries fodder feeds a moobloom; a calf fits in a cage
- [ ] **MB-33 Predators:** an Alex's Mobs tiger near a moobloom -> it hunts it
- [ ] **MB-34 Milk tag:** a tag-based milker (if one is in the pack) milks a moobloom; the bucket still works
- [ ] **MB-35 Beef sample:** syringe on a moobloom -> "Sampled Moobloom.", the cow syringe -> crafts culture beef

## 9. History - DO NOT REMOVE

- 2026-10-05: written from the decompiled Buzzier Bees 5.0.1 jar plus the exported tag and loot dumps. Gaps G1-G4,
  add-a-moobloom design (Option A variants recommended, Option B per-type entities), decisions D1-D4 open.
- 2026-10-05: overhaul planned (section 4): O1-O8 from the user, 10 variants (9 textures in `kubejs/assets/`), architecture, data formats, spawn roll order, work steps P1-P8, decisions D1-D14, tests MB-20 to MB-45 reserved. Nothing built.
- 2026-10-05 (later): plan extended with O9-O12: two-stage shearing (stays a moobloom, regrows after eating grass via `EatBlockGoal`/`ate()`), two flowers per variant in fixed slots of BB's flower layer with per-flower chance drops, buttercup item removal (worldgen, trader, recipes, tags, JEI), Sunny effect removal (aura, stew, JEED). The buttercup moobloom becomes a dandelion variant (D15). D8/D13 answered; D15-D21 added; MB-28 retired; MB-46 to MB-58 added.
- 2026-10-05 (evening): moved the overhaul into its own mod, **More Mooblooms** (O13, O14): its own moobloom entity instead of mixins on BB's, data-driven variants synced to clients, content sets + built-in packs (Incubation Additions pattern), a Buzzier Bees set that converts BB's mooblooms and removes the buttercup and Sunny from outside, compat tag packs. Added BB's Abnormals License 1.0 (4.0) and its rules: nothing of BB's in our jar (own dandelion texture), no mixins into BB, no compile dependency. patcherMod/CEConfig/`copyTags` parts dropped. D1 answered; D14/D15/D21 revised; D22-D26 added; MB-59 to MB-66 added.
- 2026-10-05 (night): answers applied: names "<Id> Moobloom" (D2); one pack rule, 100% random mooblooms in `#creativeengineering:moobloom/flower_variety` (D12); no mod spawn rules, `rareChance` default 0 (D24); D22/D23/D25 as suggested; granular BB switches with per-switch sub-packs (D26). Added 2.8 flower spawns per biome (scratch generator on the crop-table loaders + decompiled vanilla flower features) and the per-moobloom map in 4.4 (names, textures, egg colours, flowers, suggested secondaries, drops, where the flower grows). New D27-D29, MB-38 to MB-40 reworded, MB-67 added.
- 2026-10-07: **built** (4.15). The user's answers in the 4.14 Notes column applied: bare shearing makes a cow again
  (D8), flowers are the breeding food (D6), BB mooblooms removed rather than converted (D25), corn renamed cornflower
  (D28), cows added to meadow and blooming plateau (D27), D20 ignored, D16/D29 deferred. Forge 1.19.2 has no tag
  `remove` entries, so the buttercup is stripped by a TagLoader mixin. Scratch server with the full pack: boots, all
  mixins apply, flower-variety rule verified on chunk generation. MB-21/46/60/66 reworded, MB-57 retired, MB-68..71 added.
- 2026-10-07 (evening): planned 4.16 (nothing built): variant table for the new `textureswapper/` coats (12 wild + 7 bred mixes, three explicit flower slots), `food`/`convert`/`breed_weight` fields, `_i` coat twins, breeding combination data + random calves, egg-on-moobloom calf fix, Sunny off (vanilla TAIL mixin at priority 900 ahead of BB's + effect DENY). Egg swap traced to Quark Automatic Tool Restock (same item, NBT ignored); the fix is a Quark ignored item. D30-D40 and MB-72 to MB-84 added.
- 2026-10-07 (night): 4.16 rewritten for the final textures the user put in `kubejs/assets/` (11 coats, final names = ids; not moved into the mod yet). Each of the 17 vanilla flowers sits on exactly one variant; 10 colour-mix breeding combinations; old ids remapped through a new `replaces` field; default variant becomes `moobloom`. The `_i` twins and bred-only mix coats are gone (D31, D36, D37, D40 closed; D41, D42 added). MB-74/75/79/80/81 reworded, MB-85 added, mapping note for the older tests.
- 2026-10-07 (late): flowers became a weighted pool per variant (positions fixed, each flower rolled per position and saved as `Flowers`). First shear drops the flowers actually grown; second shear rolls the pool by weight. Wild onions added to onionbloom as food through an optional tag. Shear lists dropped from the format; D43-D46 added; MB-74/80/85 reworded, MB-86 to MB-88 added.
- 2026-10-07 (night): **More Mooblooms 1.0.** Final variants built from the user's table: 11 coats with their names as ids, weighted flower pools rolled per position and saved on the moobloom (`Flowers`), first shear drops the carried flowers and the second rolls the pool, 10 parent combinations plus breeds-true and random calves, egg-on-moobloom calves, `disableSunny` (priority-900 mixin ahead of BB's burn check, effect denied and stripped). Then: wild onions as an optional pool entry, `feed_weight` for flowers in several pools (dandelion 90/10 moobloom/sunbloom), witherblooms in soul sand valleys (switch pack, dark spawn placement, direct-spawn variant from the biome rule), old-id remapping dropped (the user clears old entities by hand), version set to 1.0.0. Data now generated from the variant table (`tools/gen_moobloom_variants.py`), egg colours from the coats. Egg swap traced to Quark restock and fixed in the pack. Writeup rewritten to the 1.0 state (sections 0-8; this history kept as is). MB-23/50/75 retired, MB-89 to MB-93 added.
- 2026-10-07 (late night): dark spawning made per variant: new variant field `spawns_in_dark_on` (only witherbloom has it); the variant is now picked in `checkSpawnRules`, so soul blocks in the dark take witherblooms only. MB-93 reworded.
- 2026-10-08: 2.8 regenerated from the wild flower tuning (`gen_wild_flowers.py` inventory, per pool flower and per
  biome with variant mixes and cows). Spawns rewritten by hand from it (D12, D29 answered; D47, D48 added): 35
  per-biome rules in three tiers (100% / 25% / 2% of cows) with each biome's variant mix; the
  `creativeengineering:moobloom/flower_variety` tag and `moobloom_spawns.js` removed; cows added to skylands spring.
  About 13 mooblooms per 100 cows (was about 4.8). MB-36/38/40 reworded, MB-94 to MB-97 added. Scratch
  server (full pack, 1.0.0 jar): `loaded 36 moobloom spawn rules (57 biome ids, 1 biome tags)`, no warnings, no
  biome-modifier errors. Actual spawns not looked at.
- 2026-10-08 (later): the user set the flower biomes' bloom % (25-100%) and variant splits in the 4.9 table; the rule
  file now holds bloom % x split (lavender's 106 split scaled to 75%). D49 added; MB-36/38/96/97 reworded. About 10
  mooblooms per 100 cows. The 1.0.0 jar in mods/ was already the current build (same md5 as build/libs, no newer
  source). Not run in game.
- 2026-10-11: the user retuned the flower biome table (cloud forest and moonlight grove 65%, alpha islands 35%, meadow
  and skylands spring 75%; blooming plateau/valley, meadow and flower forest an even split). The even split first
  took all 10 non-wither variants, then, on the user's word, the 9 flower variants (azaleabloom out). Azaleablooms
  now spawn in lush caves instead: the mod (kept at 1.0.0) gives azaleabloom `spawns_in_dark_on` moss block; the pack adds the
  moobloom to lush caves (weight 10, groups of 1-3) with a 100% azaleabloom rule. About 10.4 mooblooms per 100
  cows. MB-38/97 reworded, MB-98 added. Rebuilt jar installed in mods/ with patcherMod 1.0.5;
  not run in game, server not updated.
- 2026-10-11: ships in pack 1.0.5 (staged, not deployed): `moreMooblooms-1.0.0.jar` (md5 9ced3342) with
  `config/more_mooblooms-common.toml`, `kubejs/data/more_mooblooms/moobloom_spawns/creativeengineering.json`, the two
  biome modifiers (`moobloom_cows`, `azaleabloom_lush_caves`), (also 11 loose `kubejs/assets/*bloom.png`: identical copies of the jar's textures at a path nothing reads; they ship
  but do nothing), the
  syringe line and the Quark restock ignore. Single player with the staged jar (2026-10-11 14:42) loaded 11 variants,
  37 spawn rules and 10 breeding rules without errors. The version stays 1.0.0 for this first release.
- 2026-10-14: pack 1.0.5 was never deployed; everything above that was staged for it ships in pack **1.0.6** (STAGED
  2026-10-14, update from 1.0.4, [plan_1.0.6.md](plan_1.0.6.md)), unchanged.
- 2026-10-22: **More Mooblooms 1.1.0 JEI** (section 5) BUILT, not installed: breeding pages with food and every
  outcome (combination + miss, breeds true, random calf), a shearing category with drops per shear and the rendered
  moobloom cycling its flowers, short info text, JEBr's BB moobloom page hidden. MB-71 reworded, MB-100 to MB-104
  added.
- 2026-10-22 (second bulk run): 1.1.0 installed into `mods/` in place of 1.0.0; the dedicated server boots with it
  (SQ-GM-09 PASS). MB-100 to MB-104 (JEI) stay in game.
- 2026-10-24: planned from the scratchpad: sunflower facing on the Moobloom Conversion tab (`mobs/mooblooms #6`). No code changed.
- 2026-11-07: `flowers #2` built: `tools/gen_moobloom_rules.py` (in `/cdevgen`, id `moobloom_rules`, dump
  `dumpspawns`) writes the spawn rule file and the 2.8 / 4.9 tables from the wild flowers, the variant pools and the
  cows; hand-set flower-biome rules live in `kubejs/generation_scripts/worldgen/moobloom_rules.json` `manual`. First
  run: the same spawn percents as the hand-written file (37 rules), only rule and entry order changed; 2.8 got the
  current biome areas and per-1000 numbers (unchanged tiers). No spawn behaviour change.
