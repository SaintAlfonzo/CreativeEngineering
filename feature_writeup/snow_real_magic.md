# Snow! Real Magic: overlap and integration report

Investigation written 2026-11-24; fold-in part A built 2026-11-25 (section 4b), part B open. Jar `mods/SnowRealMagic-1.19.2-forge-6.5.4.jar` (added by the
owner 2026-11-24, not booted yet), mod id `snowrealmagic`, by Snownee, minzhi and WZD, license **MIT** (a fork or
copied code is allowed; this report proposes pack-side mixins and config only). Native Forge 1.19.2 build: no port
needed. Short name in this file: **SRM**. Test prefix `SRM-`. Code read from
`DecompiledMods/SnowRealMagic-1.19.2-forge-6.5.4/` (`src/` = `tools/decompile_mod.py` output, 59 classes).

**Scope rule:** integration adds no recipe or mechanic that is not in SRM or defined by a pack rule unless the owner
asks for it by name. Upstream bugs are fixed. Here the pack rules that apply are BlockRunner and pass-through
([blockrunner.md](blockrunner.md) 3.10, 3.11), melting ([melting.md](melting.md)), seasons ([weather_by_biome.md](weather_by_biome.md),
Creative Calendar) and the salt layer ([salt.md](salt.md), Vintage Delight in Creative Delight).

**Owner's asks (2026-11-24):** (1) catalog every feature so the owner can mark the ones to leave out (section 3.1,
column "Owner"); (2) look at running and falling into snow against the BlockRunner work (section 2.2); (3) make every
snow tweak integrate with Immersive Weathering, melting and the other mods that change the same behavior (sections
2.1, 2.3 to 2.6).

## 0. At a glance

<!-- generated:status_index begin open_work sha1=541e600d906e -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 13 open TODO rows (Testing 13), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `mod-integration #14` | Testing | S C | Snow! Real Magic: melting tags and products for the 7 SRM blocks; light-melt hook dropped |
| `mod-integration #15` | Testing | S C | Snow! Real Magic: magma within fireRange stops SRM snowfall |
| `mod-integration #16` | Testing | S C | Snow! Real Magic: walk-through, falling and held-block speed |
| `mod-integration #17` | Testing | S C | Snow! Real Magic: Immersive Weathering guard |
| `mod-integration #18` | Testing | S C | Snow! Real Magic: one pack grass spread tick; no spread from snowed grass |
| `mod-integration #19` | Testing | S C | Snow! Real Magic: Serene Seasons melt kept while SRM melting is off |
| `mod-integration #20` | Testing | S C | Snow! Real Magic: hand scoop 1 layer, shovel scoop 2 layers |
| `mod-integration #21` | Testing | S C | Snow! Real Magic: snow overlay on every snow layer |
| `mod-integration #22` | Testing | S C | Snow! Real Magic: WTHIT names snowed blocks Snowy <Block> |
| `mod-integration #23` | Testing | S C | Snow! Real Magic: thunder-only blizzards and ice from snow in water |
| `mod-integration #24` | Testing | S C | Snow! Real Magic: salt melts snow faster; salt layers do not fall or give snowballs |
| `mod-integration #25` | Testing | S C | Snow! Real Magic: right-click turns off a block's snowy look |
| `mod-integration #26` | Testing | S C | Snow! Real Magic: config and first-boot steps |
<!-- generated:status_index end open_work -->

| Part | Where | Status |
|---|---|---|
| Inventory: 7 blocks (no items), 2 block entities, 1 entity, 2 gamerules, 21 mixins, 21 common + 2 client config keys, 14 tag files, 7 loot tables | 1, 3 | read from the jar |
| Feature catalog F1 to F27 | 3.1 | marked by the owner 2026-11-24 |
| Boot blocker B1: Creative Compat's `VanillaLightMeltMixin` (`require = 1`) loses its target | 2.1, 3.3 | confirmed r1124nwr, fixed (`creative-compat-publish #11`) |
| Overlap: SRM replaces 6 vanilla snow layer methods that 7 pack mixins and Immersive Weathering already hook | 2.1 | 14 overlap rows |
| Running and falling into snow | 2.2 | 5 findings, D2 to D4 |
| Melting, seasons, Immersive Weathering, grass spread, salt layers | 2.3 to 2.6 | 19 findings |
| Upstream and interplay bugs B1 to B7 | 3.3 | 1 blocker, 6 others |
| Fold-in steps | 4, 4b | part A built 2026-11-25 (steps 1 to 9, 13; config step 10 written as a plan until the first boot); part B (scoops, overlay, WTHIT name, blizzards, salt, look toggle) open |
| Decisions D1 to D14 | 5, 5b | answered 2026-11-24 (R1 to R19) |
| Tests SRM-01 to SRM-32 | 8 | drafted |

- Dependencies: Kiwi 8 (already in `mods/` for Per-Dimension Game Rules), Forge 43.2+. Optional: Jade (not in the pack;
  WTHIT is), Rubidium (in the pack: SRM's two Rubidium renderer mixins apply), Serene Seasons (in the pack: SRM's
  seasons compat applies), TerraForged (not in the pack).
- Pack references: none (`grep snowrealmagic` over `kubejs/`, `development/*/src`, the dicts: 0 hits). No
  `mod_sides.md` row before this report (added, side `both`).
- Already live once booted (no pack work needed for these to run): every feature in 3.1 with its default config.
  The dumps in `kubejs/exported/` predate the jar (2026-11-13 dump, jar 2026-11-24).
- Config: Kiwi writes `config/snowrealmagic-common.toml` and `snowrealmagic-client.toml` on the first boot. They
  don't exist yet.

## 1. Overview and facts

SRM reworks vanilla snow layers. Snow can sit inside plants, fences, walls, slabs and stairs, falls when unsupported,
piles up in storms, melts by season, cushions falls and slows you when deep. It does this mostly by `@Overwrite` on
`SnowLayerBlock`, plus a replacement of vanilla's per-chunk snowfall and of the worldgen snow feature.

| Fact | Value |
|---|---|
| Mod id, version | `snowrealmagic` 6.5.4, MC 1.19.2, Forge `[43.2,)`, loader `[43,)` |
| License | MIT |
| Items | none (every block is `@NoItem`; the vanilla snow layer item places them) |
| Blocks | `snowrealmagic:snow` (a snow layer that holds a plant, block entity `snowrealmagic:snow`), `fence` (wooden), `fence2` (other fences), `fence_gate`, `wall`, `slab`, `stairs` (snow-topped copies that hold the original block, block entity `snowrealmagic:texture_tile`) |
| Entities | `snowrealmagic:snow` (falling snow, 0.98 x 0.001) |
| Gamerules | `snowrealmagic:blizzardStrength` (0 = off), `snowrealmagic:blizzardFrequency` (10000) |
| Effects, potions, recipes, worldgen files, trades, structures | none (worldgen changes are code: `SnowAndFreezeFeature` replaced) |
| Loot | 7 block tables (each variant drops its original block plus its snow, `snowrealmagic:normal` entry type) |
| Tags | adds its blocks to `minecraft:` `fences`, `wooden_fences`, `fence_gates`, `walls`, `slabs`, `stairs`, `snow`, `inside_step_sound_blocks`, `mineable/shovel`, `mineable/pickaxe`, the fox, goat, mooshroom, rabbit and wolf `*_spawnable_on` tags; **`snow_layer_can_survive_on` += `#minecraft:leaves`**; own tags `bottom_snow`, `containables` (small flowers, saplings, dead bush, many optional modded plants), `not_containables` (empty), `offset_y`, `cannot_accumulate_on` (hay) |
| Advancements | none (the pack drops all advancements anyway) |
| Lang | 13 files (names: the variants use the vanilla snow name) |
| Tooltips | Jade only (inert here) |
| Mixins | common 12: `SnowLayerBlock` (6 overwrites, 3 added methods), `ServerLevel.tickChunk` (redirect), `SnowAndFreezeFeature.place` (HEAD cancel), `SpreadingSnowyDirtBlock.randomTick` (HEAD cancel) and `canBeGrass`, `SnowyDirtBlock`, `DoublePlantBlock` (4), `BlockItem` (2), `Block`, Serene Seasons `RandomUpdateHandler.onWorldTick` (HEAD cancel), 2 accessors; client 9 (models, camera, render dispatch, 2 Rubidium) |

## 2. Overlap

### 2.1 Mixin overlap on `SnowLayerBlock` (the core of the integration)

Mixin applies every mixin's `@Overwrite` (main pass) before any `@Inject` or `@Redirect` (inject pass), whatever the
mod order. So SRM's overwritten bodies are what our injects land on. HEAD and RETURN injects still find their target
and run **before** SRM's code. A redirect or an INVOKE inject into the old vanilla body finds nothing.

| Method (SRG) | SRM | Pack and other mods hooking it | Result with SRM | Proposal |
|---|---|---|---|---|
| `getCollisionShape` (`m_5939_`) | overwrite: layer n collides at n-1 px (vanilla 2(n-1) px), 8 = full; `thinnerBoundingBox` | Compat `SnowLayerPassMixin` HEAD (walk-through for living entities) | ours wins for living entities; items, projectiles and **path searches** now see SRM's thinner shape *overlaps w/ BlockRunner 3.10* | `thinnerBoundingBox = false` (D3) |
| `updateShape` (`m_7417_`) | overwrite: schedules a fall check (`snowGravity`); with gravity off it returns the state and **never breaks unsupported snow** | Compat `SnowLayerFallMixin` HEAD (hold and queue the fall) | ours first over a free block; otherwise SRM schedules its own fall *overlaps w/ FallingLayers* | `snowGravity = false` (D4); unsupported snow handled by FallingLayers (tag check, step 6) |
| `canSurvive` (`m_7898_`) | overwrite: `Hooks.canSnowSurvive` (vanilla rules plus a full 8-layer stack; tag adds leaves) | none | snow survives on leaves (F13) | D10 |
| `randomTick` (`m_213898_`) | overwrite: `Hooks.randomTick` (season melt, light melt over 11, storm piling) | IW `SnowLayerBlockMixin` HEAD (rain removes the layer), Compat `SnowLayerFallMixin` HEAD, Compat `SnowLayerPassMixin` HEAD (full layer -> snow block), CD `SaltLayerRandomTickMixin` HEAD, **Compat `VanillaLightMeltMixin` redirect of `ServerLevel.getBrightness`, `require = 1`** | the HEAD injects run, then SRM; **the redirect has no target: boot crash (B1)**; vanilla light melt comes back inside `Hooks` past our switch | step 1 |
| `canBeReplaced` (`m_6864_`) | overwrite: any block replaces 1 to 7 layers (`snowAlwaysReplaceable`) | Compat `SnowLayerReplaceMixin` HEAD (`replaceMaxLayers` 3) | ours wins for `minecraft:snow`; `snowrealmagic:snow` is not in our tag, so SRM's rule applies there *overlaps w/ 3.11 Replacing* | `snowAlwaysReplaceable = false`, tag SRM's snow (step 5) |
| `getStateForPlacement` (`m_5573_`) | overwrite: adds a layer to **any** `SnowLayerBlock` clicked (vanilla: the same block only) | Compat `SnowLayerPassMixin` RETURN (8 -> snow block) | ours still converts; cross-stacking snow on salt layers is possible (B3) | step 11 |
| `onPlace`, `tick`, `fallOn`, `stepOn`, `use` (added) | gravity tick, fall damage cut, deep-snow drag, plant-into-snow and overlay toggle | Vintage `SaltLayerBlock` overrides `onPlace` only | every `SnowLayerBlock` subclass gets them, salt layers included (B3) | step 11 |

Other pack hooks on the same paths:

| System | Pack or mod | SRM | Result | Proposal |
|---|---|---|---|---|
| Per-chunk snowfall and freezing | vanilla `ServerLevel.tickChunk`; IW injects there too (random-tick slice and TAIL, different slice: no clash) | `ServerLevelMixin` redirects vanilla's snow/freeze roll to `WorldTickHandler` (same 1 in 16) | Compat `BiomeMeltMixin` still covers freezing (`shouldFreeze` is called) but **not snow**: SRM never calls `Biome.shouldSnow`, so snow forms beside magma again | step 3 |
| Worldgen snow (`SnowAndFreezeFeature`) | none | HEAD cancel, own placement (snow inside plants) | worldgen snowy plants (F7) | D1 |
| Serene Seasons melt (`RandomUpdateHandler.onWorldTick`) | Creative Calendar `SereneSeasonsMeltThrottleMixin` HEAD (every 4th tick) | HEAD cancel: Serene Seasons' melt never runs, only its weather frequency; SRM melts by random tick instead | Calendar's throttle becomes a no-op (fine, the cost is gone) | step 4 |
| Grass spread (`SpreadingSnowyDirtBlock.randomTick`) | Calendar `GrassSpreadSeasonMixin` HEAD cancel, Neapolitan and Vinery mixins on the class, IW `ModGrassBlock` (calls super) | HEAD cancel: SRM re-implements vanilla's spread | order between HEAD injects decides whether Calendar's crop-season check still runs | step 9 |
| Melting tags | Compat `#creativecompat:melting/*` list `minecraft:snow` by id | 7 new snow blocks, not in any pack tag | snowy plants, fences, stairs never melt by fire, lava, salt or hot places | step 2 |
| Walk-through, falling, replace tags | `#creativecompat:passthrough_layers`, `falling_layers`, `replaceable_layers` list `minecraft:snow` by id | `snowrealmagic:snow` not tagged | a snowy plant is solid snow with SRM's thin collision | step 5 |
| BlockRunner tiers | KubeJS tier tags | the 6 snow-topped variants and `snowrealmagic:snow` are untiered | a snowed oak stair loses the wood tier (1.10x -> 1.00x) | step 7 |
| Pass-through tiers | `tall_plant`, `grass`, `bush`, `wild_crop` | a snowed plant is `snowrealmagic:snow` | the plant's walk rule is lost; SRM's own sweet berry stuck (0.8) returns instead of our `bush` 0.36 | step 7 |
| Immersive Weathering snowy decor | IW snowy stone, cobblestone, stone bricks in every shape (`SnowGrowth`), melted back by Compat `meltProducts` | SRM converts any `#minecraft:stairs` bottom stair, bottom slab, wall, fence | IW snowy stairs, slabs and walls get wrapped in SRM's variants: IW's un-snow and our melt no longer see them | D9, step 8 |

### 2.2 Running and falling into snow (BlockRunner)

What the pack does today ([blockrunner.md](blockrunner.md) 3.10, 3.11): snow layers 1 to 7 have no collision for living
entities. You wade through at the `layers` tier slowdown (1.0 at 1 layer, down to `snowMaxSlow` 0.4 at 7), with a
weaker jump in deep snow (`LayerJumpMixin`). Leather boots and light mobs stand on top at BlockRunner's Snow tier 0.75.
Sunk mobs climb out and path over the top. 8 layers become a snow block. Fall damage in snow is full ("snow layer"
row, audit 2026-11-19).

| SRM feature | What it does | With the pack | Finding | Proposal |
|---|---|---|---|---|
| F2 deep-snow drag (`stepOn`, needs `thinnerBoundingBox`) | standing **on** 3+ layers, horizontal velocity x (1 - 0.05 x (layers - 2)) every tick, unless sneaking or moving fast vertically | only fires for entities whose feet rest on the snow block: leather boots, light mobs, items. A wading player's feet are on the ground below, so it never fires for them | the people who walk on top get the strongest slow. A per-tick velocity multiply compounds: 0.85 per tick is about a 0.5x top speed, on top of the 0.75 Snow tier | off (D3) |
| F3 fall damage cut (`fallOn`, `snowReduceFallDamage`) | landing on a vanilla snow layer or a snowy plant: 0.2x damage at any layer count; on other `SnowLayerBlock` subclasses (salt) 1 - 0.1 x layers | `fallOn` belongs to the block you land on. Wading entities fall through and land on the ground below, so no cut. Leather boots land on top: 0.2x | the same jump into the same snow costs full damage barefoot and a fifth in leather boots; salt layers cushion against the 2026-11-19 "full damage on every surface layer" ruling | off, or a pack rule in PassThrough (D2) |
| F1 thinner collision (`thinnerBoundingBox`) | collision one layer-pixel per layer below vanilla | living entities keep our walk-through; items sink deeper; **path searches use SRM's shape**: up to 7 layers (6 px) is under a mob's step, so mobs plan paths straight into 5 to 7 layers (vanilla: a step up) | changes the documented mob pathing rule (3.11 "Mob pathing") | off (D3) |
| F14 camera fog in snow (needs `thinnerBoundingBox`) | powder-snow fog when the camera enters a snow layer's shape | only when crouching or crawling into deep snow (the eye is above 14 px otherwise) | cosmetic | goes with D3 |
| F4 falling snow (`snowGravity`) | unsupported layers fall as `snowrealmagic:snow` and stack | our `FallingLayers` already does this for snow and salt; SRM's entity lands through `Hooks.placeLayersOn` (8 layers stay a layer until a random tick turns them into a block) | two fall systems race (ours queues on the next level tick, SRM ticks 2 ticks later) | off (D4) |

Running (sprinting) has no SRM code of its own: sprint speed in snow is our walk-through multiplier x BlockRunner,
plus F2 when on top. With F1 and F2 off, running and falling into snow behave exactly as today for `minecraft:snow`.
The only new work is giving SRM's `snowrealmagic:snow` (snow in a plant) the same walk-through rules (step 5): today
it would be a solid thin slab you walk on.

### 2.3 Melting ([melting.md](melting.md))

| Path | Before SRM | With SRM (defaults, Serene Seasons loaded) | Proposal |
|---|---|---|---|
| Vanilla light melt (light over 11) | off (`vanillaLightMelting = false`); torches melt through the fire rule | the switch's redirect crashes (B1); once fixed, `Hooks.randomTick` melts snow over light 11 again, past the switch | redirect the light read in `snownee.snow.Hooks.randomTick` too (`@Pseudo`, step 1) |
| Fire, lava, magma, salt, hot places (Compat `Melting`) | snow layers lose layers, snow blocks vanish | unchanged for `minecraft:snow` (HEAD of `BlockStateBase.randomTick`, before the block) | add SRM's 7 blocks: product = the held block (one layer at a time on `snowrealmagic:snow`) (step 2) |
| Magma stops snowfall (`magmaStopsFreezing`) | `Biome.shouldSnow` false beside magma | SRM's snowfall skips `shouldSnow` | same magma check inside `WorldTickHandler.tick` (step 3) |
| Season melt | Serene Seasons' handler (throttled by Calendar) melts snow and ice outside winter | SRM cancels it; snow loses layers by day outside winter (1 in 2 to 1 in 8 per roll by sub-season) unless cold enough to snow; tropical biomes always; ice melts in seasonal biomes by day under open sky | accept (D7); temperatures go through `SeasonHooks.getBiomeTemperature`, so Calendar's season types (frozen, permanent winter: nothing melts) hold |
| Immersive Weathering rain melt | rain at the block above removes the whole snow layer | runs first; on `snowrealmagic:snow` it removes the block **with the plant in it** (B5) | Compat guard: on SRM blocks, set the held block back (step 8) |
| IW ice melt (`ice_icicle_and_melt` growth) | Nether and warm-biome ice | unchanged (ice is not overwritten) | none |
| Snow blocks | never melt naturally (only by the melting sources) | unchanged | see D6 (storm piling) |

### 2.4 Seasons and weather ([weather_by_biome.md](weather_by_biome.md), Creative Calendar)

- Snowfall: SRM's `coldEnoughToSnow` asks Serene Seasons' `SeasonHooks.getBiomeTemperature`, where Calendar's
  `SeasonHooksTypesMixin` applies the season types and snow line. So snowfall follows Calendar's rules.
- `accumulationWinterOnly` (default off) asks Serene Seasons' global season, not Calendar's per-type snow seasons
  (cold types snow Late Autumn to Early Spring). Leave it off, or make it ask Calendar (D6).
- Storm piling (F8, `snowAccumulationDuringSnowstorm`, on): in a thunderstorm that snows, a layer's random tick adds a
  layer (lower neighbors first) up to `snowAccumulationMaxLayers` 8. Our full-layer rule turns 8 into a snow block,
  which never melts by season, and the next storm starts a new layer on top. **Snowy biomes would grow one block of
  snow after another, every winter, for good.** D6.

### 2.5 Immersive Weathering

| IW feature | SRM | Result | Proposal |
|---|---|---|---|
| Rain melts snow layers (mixin HEAD) | overwrite of the same method | still runs (HEAD) | B5 fix for SRM's blocks |
| Snowy stone, cobble, stone bricks and their stairs, slabs, walls (`SnowGrowth`, `Snowy.NORMAL_TO_SNOWY`) | SRM wraps any bottom stair, bottom slab, wall or fence under snowfall | IW's snowy shapes and their plain sources get wrapped in `snowrealmagic:stairs` etc.; IW's own snowy growth and un-snow, and our `meltProducts`, no longer match | `#snowrealmagic:not_containables` += IW's snowy and frosty blocks and the blocks IW snows (D9) |
| Frosty glass, grass, fern; frost; icicles; thin ice | not touched | none | none |
| `ModGrassBlock` (IW's grass, age and path wear) | SRM cancels the vanilla spread body; IW calls super first, then its own logic | IW's wear still runs; spread is SRM's copy | none |
| Leaf piles | SRM's leaves tag lets snow sit on leaves | snow on leaves, then on leaf piles below when leaves decay | D10 |
| `tickChunk` injects | SRM redirects a different slice | no clash | none |

### 2.6 Salt layers (Vintage Delight in Creative Delight)

`SaltLayerBlock extends SnowLayerBlock` and overrides only `getCloneItemStack`, `onPlace`, `canBeReplaced` and
`getCollisionShape`. So salt layers get SRM's `updateShape`, `canSurvive`, `getStateForPlacement` (cross-stacking),
`tick` (SRM's fall turns a salt layer into a falling **snow** entity that lands as snow), `fallOn` (cushion), `stepOn`
(drag) and `use`. SRM's right-click handler (`GameEvents.onItemUse`) takes any `SnowVariant`, salt layers included:
a shovel right-click or a sneak right-click **turns a salt layer into snowballs**. B3, step 11.

## 3. New content

### 3.1 Feature catalog (owner marks "leave out")

Default = SRM's default. Recommendation = this report's proposal for the pack. In the Owner column, write `keep` or `out`
(or a value). A feature marked `out` is switched off by its config key, a tag or a pack mixin (the Off route column).

| # | Feature | What a player sees | Default | Off route | Recommendation | Owner |
|---|---|---|---|---|---|---|
| F1 | Thinner snow collision | items and arrows sink one layer-pixel per layer deeper; mobs path into deep snow | on | `thinnerBoundingBox = false` | out (2.2) | out |
| F2 | Deep-snow drag | slowed when standing on top of 3+ layers | on (with F1) | `thinnerBoundingBox = false` | out (2.2) | out |
| F3 | Snow cushions falls | landing on snow: a fifth of the damage | on | `snowReduceFallDamage = false` | out, or a pack rule (D2) | out (full damage; later pack rule, R2) |
| F4 | Snow falls when unsupported | snow drops off a removed block and stacks below | on | `snowGravity = false` | out (ours does it) | keep (SRM falls snow, R3) |
| F5 | Any block replaces snow up to 7 layers | placing a block into deep snow replaces it | on | `snowAlwaysReplaceable = false` | out (ours: up to 3) | out (ours, 3 layers) |
| F6a | Snow inside plants (by hand) | place snow on grass, flowers, saplings, mushrooms, berry bushes, tall plants: the plant stays inside the snow | on | `placeSnowInBlock = false` | keep | keep |
| F6b | Snow on fences, gates, walls, bottom slabs, bottom stairs (by hand) | snow sits on top of them | on | `placeSnowInBlock` (shared with F6a) or `not_containables` tag | keep, IW snowy blocks excluded (D9) | keep (IW snowy excluded, R8) |
| F6c | Snowfall does F6a and F6b | snowfall snows plants and fences in place, villages and structures included | on | `placeSnowInBlockNaturally = false` (plants too), or the `not_containables` tag for blocks only | D8 | keep, plants and blocks |
| F7 | Worldgen snowy plants | new chunks in cold biomes generate plants inside snow | on | `replaceWorldFeature = false` | keep | keep |
| F8 | Storm piling | thunder-snowstorms add layers up to 8 | on | `snowAccumulationDuringSnowstorm = false` | keep with a cap (D6) | keep, cap 8 (R5) |
| F8b | Snowfall piling | every snowfall adds layers | off | `snowAccumulationDuringSnowfall` | off | off |
| F9 | Season melt | outside winter, snow loses layers by day | on (Serene Seasons) | `snowNeverMelt = true` (also stops F10) | keep (D7) | out (Serene Seasons melt kept, R6) |
| F10 | Light melt over 11 | sea lanterns, redstone lamps melt snow again | on, no key | pack mixin (step 1) | out (pack rule: off) | out |
| F11 | Snowfall rewrite | snow forms in and under open plants down to 5 blocks | on, no key | none | keep (needed by F6c) | keep |
| F12 | Grass stays grass under snow | grass under deep snow stays grass, shows snowy under snowy plants | on | `sustainGrassIfLayerMoreThanOne = false` | keep | keep |
| F13 | Snow on leaves | snow lies on tree canopies | on (tag) | remove leaves from `#minecraft:snow_layer_can_survive_on` | D10 | keep |
| F14 | Snow fog for the camera | powder-snow fog with the camera inside snow | on (with F1) | `thinnerBoundingBox = false` | goes with F1 | out (with F1) |
| F15 | Shovel scoop | right-click snow with a shovel: one layer off, one snowball, 1 durability | on, no key | pack mixin | keep (snow only, B3) | keep, 2 layers per click (R10) |
| F16 | Hand scoop | sneak right-click with an empty hand: one layer -> one snowball | on | `sneakSnowball = false` | keep (snow only, B3) | keep, 1 layer per click (R10) |
| F17 | Plant into snow | placing a plant, fence, slab or stair into a snow layer keeps the snow around it | on (with F6) | `placeSnowInBlock = false` | keep | keep |
| F18 | Overlay toggle (see R19 for the right-click) | empty-hand right-click on snow (not over snowy ground) switches how the snow renders | on, no key | pack mixin | D11 | toggle out; overlay always on, every snow layer (R11) |
| F19 | Mobs spawn on deep snow | spawns allowed up to 8 layers; foxes, goats, mooshrooms, rabbits, wolves spawn on snowy plants | on | `mobSpawningMaxLayers` | keep | keep, 8 |
| F20 | Snowy foliage models | some snowed plants render with snow-covered variants | on (client) | `snowVariants = false` (client toml) | keep | keep |
| F20b | Particles through leaves | snowfall particles pass leaves | on (client) | `particleThroughLeaves` | keep | keep (default, not asked) |
| F21 | Blizzards | falling-snow entities drop from the sky in rain | off (gamerule 0) | gamerule | off (needs F4) | on, thunder only, strength 2, frequency 3500 (R13) |
| F22 | Snow into water makes ice | falling snow landing in water freezes it | on (needs F4) | `snowMakingIce = false` | goes with F4 | keep at a 20% chance (R13) |
| F23 | Uninstall helper | random ticks turn snowed blocks back to normal | off | `retainOriginalBlocks` | off (note for removal) | off |
| F24 | Debug spawn command | `/` command for spawn tests | off | `debug.mobSpawningCommand` | off | off |
| F25 | Jade tooltip | Jade line | inert (no Jade) | none | none | none |
| F26 | Loot and pick block | breaking a snowed block drops the block and its snow; pick block gives the block | on | none | keep | keep (default, not asked) |
| F27 | Tall plants in snow | two-block plants keep their upper half with the lower half snowed | on | none | keep | keep (default, not asked) |

### 3.2 Blocks

| Block | Holds | Break drops | Tier and tag state in the pack |
|---|---|---|---|
| `snowrealmagic:snow` | any containable plant (block entity) | the plant and the snow | none of melting, walk-through, falling, replaceable, BlockRunner *overlaps w/ minecraft:snow* |
| `snowrealmagic:fence`, `fence2`, `fence_gate`, `wall`, `slab`, `stairs` | the original block (block entity) | the original block and the snow | untiered for BlockRunner and melting *overlaps w/ IW snowy stairs, slabs, walls* |

### 3.3 Bugs

- **B1 (pack, boot blocker):** `creativecompat` `VanillaLightMeltMixin` redirects `ServerLevel.m_45517_` inside
  `SnowLayerBlock.m_213898_` with `require = 1`. SRM's overwrite leaves only a call to `Hooks.randomTick`, so the
  redirect finds no target and Mixin stops the game when `SnowLayerBlock` loads (start of `Blocks`). Confirmed in
  scratch run r1124nwr (2026-11-24 20:26, `InvalidInjectionException`) and **fixed** the same day under TODO
  `creative-compat-publish #11`: `CompatMixinPlugin` skips the `SnowLayerBlock` target when `snowrealmagic` is loaded
  (IceBlock keeps it). With R6 (`snowNeverMelt = true`) SRM's own light melt is off too, so step 1's `Hooks` redirect
  is no longer needed.
  Since 2026-11-24 (`creative-compat-publish #11`) `CompatMixinPlugin` skips the `SnowLayerBlock` target when
  `snowrealmagic` is loaded (one INFO line; ice keeps the light melt), so the boot no longer stops. Step 1's `@Pseudo`
  redirect into `Hooks.randomTick` (pack light melt for SRM snow) is still open.
- **B2 (upstream):** `ShapeCaches.get` logs through `snownee.jade.Jade`. Without Jade an error in a shape lookup
  becomes `NoClassDefFoundError` and hides the real one. Error path only.
- **B3 (interplay):** salt layers get SRM's snow behavior (2.6): snowballs from salt, a salt layer falling as snow,
  fall cushion and drag, and snow and salt stacking into each other (SRM's `getStateForPlacement` takes any
  `SnowLayerBlock`; to confirm in game, SRM-15).
- **B4 (interplay):** `snowrealmagic:snow` keeps SRM's `Hooks.canSnowSurvive` and, with `snowGravity = false`,
  `updateShape` no longer breaks unsupported snow at all. FallingLayers must hold or drop it (step 6).
- **B5 (interplay):** IW's rain melt calls `removeBlock` on `snowrealmagic:snow` and deletes the plant inside it. Fixed 2026-11-25 (`IwSnowMeltSrmMixin`, 4b step 8).
- **B6 (interplay):** HEAD-cancel order on `SpreadingSnowyDirtBlock.randomTick` (SRM vs Calendar's crop-season
  grass check) is not fixed by priority. If SRM's runs first, grass spreads out of season. Fixed 2026-11-25 (Calendar at priority 800, Compat's grass tick at 900, 4b step 9).
- **B7 (upstream, gated):** SRM's collision cache keys on state and layer count, not the entity. A walk-through rule
  that depends on the entity (ours) must bypass it for `snowrealmagic:snow` (step 5), or a cached shape serves the
  wrong entity. Fixed 2026-11-25 (`SrmSnowCollisionMixin`, 4b step 5).

## 4. Fold-in, in build order

0. **Dumps.** `/dumpall` after the first boot ([dumpall-refresh-workflow](agent_notes/dumpall-refresh-workflow.md)).
   Then `/cdevgen` for the BlockRunner scan and the tag dumps. Before that boot, either build step 1 or set
   `[mixins] vanillaLightMelt = false` (B1).
1. **Light melt (Compat, `melting/`).** Done in part by `creative-compat-publish #11` (target skipped); the `Hooks` redirect below is dropped by R6 (`snowNeverMelt = true`). Original plan: `VanillaLightMeltMixin`: `require = 0` on the `SnowLayerBlock` target (IceBlock
   keeps 1), and a `@Pseudo` redirect on `snownee.snow.Hooks.randomTick`'s `getBrightness` read through
   `Melting.vanillaLight`, gated in `CompatMixinPlugin` on `snowrealmagic` (as Rare Ice is). Keep SRM's `< 10`
   snowfall light checks in `WorldTickHandler` (they only decide where snow forms).
2. **Melting tags and products.** `block_dict.js` keys for the 7 SRM blocks (Common's `_lib`, rebuild Common).
   `melting_tags.js`: add them to `meltable`, `saltable`, `ultrawarm_instant`, guarded by `global.has('snowrealmagic')`.
   `Melting.meltedState`: an SRM `SnowVariant` melts to `getRaw` (the held block), one layer at a time on
   `snowrealmagic:snow`.
3. **Magma vs SRM snowfall.** A Compat `@Pseudo` HEAD inject on `snownee.snow.WorldTickHandler.tick` (or on its
   `Hooks.convert` call) that skips the column when `Melting` reports magma within `fireRange`. That is the same
   check `BiomeMeltMixin` makes.
4. **Seasons.** No code if D7 = accept. Note in [weather_by_biome.md](weather_by_biome.md) and Calendar's perf note that
   Serene Seasons' melt handler is canceled by SRM, so `seasonsMeltIntervalTicks` does nothing while SRM is
   installed.
5. **Walk-through for `snowrealmagic:snow`.** Tag it in `passthrough_layers`, `falling_layers` and `replaceable_layers`
   (`passthrough_tags.js`). Compat mixin on `EntitySnowLayerBlock.getCollisionShape` HEAD: for a living entity, our
   collision for the snow part OR the held plant's own collision, without SRM's cache (B7). WTHIT "3 of 8 layers" line
   as for snow.
6. **Falling (`FallingLayers`).** With `snowGravity = false`, make `holdAndQueue` cover unsupported snow on any
   support loss (B4). `withLayers` for `snowrealmagic:snow` drops the held plant as an item before the fall (a plant
   cannot fall with snow).
7. **BlockRunner and pass-through read the held block.** Same route as 3.3 camouflage ("walks like its camo"): for SRM
   variants the tier is the held block's (via `SnowVariant.getRaw` or the block entity), so a snowed oak stair stays
   Wood. For `snowrealmagic:snow` the walk-through tier is the plant's (bush, tall plant), and the lower of the snow and
   plant slowdowns wins (pass-through rule "lowest walk wins"). `MakeStuckInBlockMixin` sees SRM's berry stuck under
   the state `snowrealmagic:snow`: key it by the held block too, so our `bush` 0.36 replaces SRM's 0.8.
   `tools/blockrunner_scan.py --audit` after the dump.
8. **Immersive Weathering.** `#snowrealmagic:not_containables` += IW `snowy_*`, `frosty_*`, and IW's
   `Snowy.NORMAL_TO_SNOWY` source blocks (the stone, cobble and stone brick shapes), via `global.tags`, per D9. Compat
   guard on IW's `SnowLayerBlockMixin.melt` (or a HEAD inject before it): on an SRM block, set the held block back
   instead of `removeBlock` (B5).
9. **Grass spread order.** Calendar's `GrassSpreadSeasonMixin` gets `priority` below 1000 so it is applied first and
   its HEAD callback sits before SRM's (verify the order in the boot log's `Mixing` lines). The same check applies
   to the Neapolitan and Vinery mixins: confirm they still fire (SRM-18).
10. **Config.** First boot writes `config/snowrealmagic-common.toml` and `-client.toml`. Set the values from the
    owner's marks (3.1) and D2 to D11. Copy the result to `configs_edited/root_config/config/`.
11. **Salt layers (CD, `members/vintage`).** `SaltLayerBlock`: override `tick` (no SRM fall), `fallOn` (vanilla),
    `stepOn` (none), `use` (vanilla) and `getStateForPlacement` (only its own block). A CD or Compat `@Pseudo` HEAD
    inject on `snownee.snow.GameEvents.onItemUse(Player, Level, InteractionHand, BlockHitResult)` returns `PASS` for
    any block that is not SRM's or vanilla snow (no snowballs from salt).
12. **Fall damage (D2).** `snowReduceFallDamage = false`. If the owner wants a cushion, it becomes a `layers`-tier
    rule in `PassThrough.beforeLanding` (fall factor by layer count, the same for wading and standing on top). No
    SRM code.
13. **Mob pathing.** With `thinnerBoundingBox = false`, path searches see vanilla's shape again and 3.11's "Mob
    pathing" stands. Nothing to build. If D3 keeps F1, `SnowLayers.openForPath` must take SRM's shape into account.
14. **Names, tooltips, JEI.** The variants have no items. WTHIT shows "Snow" for `snowrealmagic:snow`; a WTHIT line
    naming the held block (Compat WTHIT plugin) is optional (D12). JEI: nothing (no items, no recipes).
15. **Release.** `mod_sides.md` row (done in this pass, `both`). Changelog bullets for every kept feature under a new
    `## Snow` family once built ([changelog.md](changelog.md) 3.6). TODO row and plan row. Mob spawns
    ([mob_spawns](agent_notes/mob-spawns-system.md)): SRM's spawnable tags add only its own snow block, so no change.

None: items, recipes, potions, effects, food, cans, compost, crops (SRM holds crops only if tagged `containables`;
`#minecraft:crops` are not containable by class), structures (worldgen F7 only touches plants; F6c by snowfall
touches structure fences and stairs later, D8), trades, loot (SRM's tables drop the held block; no pack table
involved).

Ordering: step 1 (or the `vanillaLightMelt = false` stopgap) before the first boot. Step 0 before 2, 5 and 7 (they
need the dump). Step 10 (config) before testing 2.2. Steps 2, 5 and 7 need the `block_dict.js` keys (Common rebuild).
Step 11 is independent (CD).

## 4b. Built 2026-11-25 (part A: `mod-integration #14` to `#19`, `#26`)

Rows `mod-integration #14` to `#19` and `#26`, read with the rulings in 5b. Jars: Creative Compat (most of it),
Creative Calendar (seasons, grass order), Creative Common (`block_dict.js` keys). Mixins on SRM classes are `@Pseudo`
and gated on `snowrealmagic` in `CompatMixinPlugin` (range `[6.5.4,6.6)`); SRM types are touched only in
`compat/SnowRealMagic` (an inner class loaded when SRM is).

| Step | Row | Built |
|---|---|---|
| 1 Light melt | #14 | dropped by R6: SRM's light melt is off with `snowNeverMelt = true`; the B1 boot fix stands |
| 2 Melting | #14 | the 7 SRM blocks in `meltable`, `saltable`, `ultrawarm_instant` (melting_tags.js, `global.has`); `Melting.meltTo` swaps a product that is not the same block for the held block, so a snowy plant loses layers one at a time and then leaves the plant, a snow-topped fence melts back to the fence; salt the same. Placement: `onPlaced` schedules a 1-tick check for SRM blocks (SRM fills the block entity after `onPlace`), and the scheduled tick applies the ultra-warm rule (SRM-22) |
| 3 Magma | #15 | `SrmWorldTickMixin`: both `Hooks.convert` calls in `WorldTickHandler.tick` skipped where `Melting.magmaBlocksFreezing`; ice forming already went through `Biome.shouldFreeze` |
| 4 Seasons | #19 | R6: Calendar `SereneSeasonsSrmMeltMixin` (priority 1100) plus `CalendarMixinPlugin.postApply`: SRM's merged `srm_onWorldTick` call in Serene Seasons' `onWorldTick` becomes POP2 (boot line `Snow! Real Magic loaded: Serene Seasons still melts snow and ice`); Calendar's throttle unchanged; `meltInChunk` treats SRM's snowy plant as snow and leaves the plant. SRM's own ice melt off (`SrmWorldTickMixin`, `snowAndIceMeltInWarmBiomes` redirect to false) |
| 5 Walk-through | #16 | `snowrealmagic:snow` in `passthrough_layers` (so in `replaceable_layers`, which takes that tag) and `falling_layers` (pack fall still leaves snow to SRM, R3), not in `passthrough_layers_open`; `SrmSnowCollisionMixin` HEAD of `EntitySnowLayerBlock.getCollisionShape` answers our walk-through shape (joined with the held plant's collision) before SRM's per-state cache (B7); WTHIT's layer line works as for snow (no code) |
| 6 Falling | - | R3, built 2026-11-24 (SQ-CMP-38); salt half is step 11 (part B) |
| 7 Held block speed | #16 | `PassThrough.evaluate`: a snowy plant adds its plant's tier at the same cell (lowest walk wins; neighbor count taken directly); SRM's 0.8 berry stuck is canceled by the existing stuck hook because the snowy plant is now tiered. `CamoSpeed.speedCamo`: a snow-topped fence, gate, wall, slab or stairs walks like its held block (not tied to `copyCamo`); WTHIT Run Speed reads the same seam |
| 8 Immersive Weathering | #17 | `#snowrealmagic:not_containables` += IW `snowy_*`, `frosty_*` and the vanilla stone, cobblestone and stone brick slabs, stairs and walls IW snows (Compat blocktags.js); `IwSnowMeltSrmMixin` (SnowLayerBlock randomTick HEAD, priority 900, needs both mods): rain on a snowy plant sets the plant back before IW removes the block (B5) |
| 9 Grass | #18 | R17: `SrmGrassSpreadMixin` (SpreadingSnowyDirtBlock randomTick HEAD, priority 900, before SRM's 1000) runs the whole tick: `canBeGrass` (SRM's snow rule) -> dirt; snow on top (`#minecraft:snow`, `#snowrealmagic:bottom_snow`) -> no spread; vanilla spread with Vinery's `trySpread` at each try and `canPropagate` (Neapolitan); new grass snowy under `bottom_snow`. Calendar's `GrassSpreadSeasonMixin` moved to priority 800 so its season check runs first (B6) |
| 10 Config | #26 | `config/snowrealmagic-common.toml` does not exist yet (first boot writes it): values below, to set and copy after that boot. Compat `[surface.passthrough.layers] snowFullBecomesBlock = false` (R5) set in `config/creativecompat-common.toml` and its `configs_edited/root_config/config/` copy |
| 13 Pathing | #26 | nothing to build with `thinnerBoundingBox = false` |

Config plan for `config/snowrealmagic-common.toml` (step 10; Kiwi writes the file on the first boot, then set these
and copy it to `configs_edited/root_config/config/snowrealmagic-common.toml`): `thinnerBoundingBox = false` (R1),
`snowReduceFallDamage = false` (R2), `snowGravity = true` (R3, default), `snowAlwaysReplaceable = false` (R4),
`snowAccumulationMaxLayers = 8` (R5, default), `snowNeverMelt = true` (R6), `mobSpawningMaxLayers = 8` (R14, default),
`placeSnowInBlock = true`, `placeSnowInBlockNaturally = true`, `replaceWorldFeature = true`,
`sustainGrassIfLayerMoreThanOne = true`, `sneakSnowball = true` (defaults kept, R7, R10, R15), `snowMakingIce = true`
(default; the 20% roll is Compat's, R13). The blizzard gamerules are world defaults set by Compat (4c), not toml keys. Client toml: defaults (`snowVariants`, `particleThroughLeaves` on).

Defaults taken where the rulings are silent: the grass rule applies only while SRM is loaded (without it vanilla's
spread with Calendar's and Vinery's hooks runs as before); "snow on top" counts any `#minecraft:snow` block, so a
salt layer does not stop spread; the plant inside a snowy plant takes no baked neighbor count; Serene Seasons melts
the snowy plant whole (its rule removes all layers of plain snow at once) but not SRM's snow-topped fences and stairs
(it melts only snow layers); SRM's snowfall storm piling is not magma-checked (magma's 2-block instant melt covers it).
`tools/blockrunner_scan.py --audit` waits for the first `/dumpall` with SRM (step 0).

## 4c. Built 2026-11-25 (part B: `mod-integration #20` to `#25`)

Rows `mod-integration #20` to `#25`, read with 5b. Jars: Creative Compat (all but salt's block rules) and Creative
Delight (`members/vintage` `SaltLayerBlock`). Same rules as part A: SRM types only in `compat/SnowRealMagic` and
`compat/SnowRealMagicClient`, mixins `@Pseudo` with `require = 0`, gated on `snowrealmagic` in `CompatMixinPlugin`.

| Rule | Row | Built |
|---|---|---|
| R10 Scoops | #20 | `SrmUseMixin` (HEAD of `GameEvents.onItemUse`, the click SRM runs from `RightClickBlock` before any block's own click) calls `SnowRealMagic.onUse`: any shovel (`hasCorrectToolForDrops` on snow, SRM's test) takes 2 layers through SRM's `onShovel` twice, a snowball per layer, 1 durability; a 1-layer pile or a snow cap gives 1. Sneak (any hand): SRM's own 1-layer hand scoop |
| R11 Drip sides | #21 | client `SrmRenderMixin` on SRM's `SnowClient` (its vanilla and Rubidium chunk hooks both end there): `shouldRedirect` also takes plain `minecraft:snow`, and `SnowRealMagicClient.render` draws the snow layer in the solid pass and SRM's `block/overlay` model on the block below in the cutout-mipped pass (plain snow is put in both passes at client setup); a snowy plant draws the drip sides without the toggle click. Not under another snow layer or a block with the `snowy` property (snowy grass), SRM's own exclusions, and not when all four sides of the block below are hidden (SRM draws the sides unculled) |
| R12 WTHIT | #22 | `WthitSnowy` (`waila_plugins.json`, needs `snowrealmagic`): the name line of SRM's blocks reads `creativecompat.wthit.snowy` "Snowy %s" with the held block's name; a snowy plant holding nothing keeps "Snow" |
| R13 Blizzards | #23 | `SrmGameruleDefaultsMixin` (`CoreModule` static init): gamerule defaults `blizzardStrength` 2, `blizzardFrequency` 3500 for new worlds (no pack gamerule mechanism exists; `/gamerule` still changes them). `SrmWorldTickMixin`: the strength read is 0 unless thundering (rain keeps SRM's plain snowfall, which a nonzero strength replaces); `doBlizzard` skips a column whose top snow has 8 layers. `SrmFallingSnowMixin`: falling snow in water turns it to ice 20% of the time (`SnowRealMagic.ICE_CHANCE`), else splashes away; SRM's `snowMakingIce` still switches it off |
| R18 + step 11 Salt | #24 | Compat `[melting] saltMeltSnowLayers` 4 (was 3); new `saltSourceSnowLayerOdds = [55, 35, 10]`: a melt by a salt block removes 1, 2 or 3 layers by those weights (`Melting.rollLayers`). Vintage `SaltLayerBlock` overrides `tick` (no SRM fall), `fallOn` (full damage), `stepOn` (no drag), `use` (PASS) and `getStateForPlacement` (only onto salt). `SrmUseMixin`: a snow-layer block that is not snow is never scooped. `SrmHooksMixin`: SRM's storm piling (`Hooks.randomTick`) skips salt, and falling snow landing on salt adds nothing (`placeLayersOn` returns false) |
| R19 Look off | #25 | A plain click with both hands empty, not sneaking, turns the snowy look off: SRM's blocks get a flag on their render options (`SrmLookOptionsMixin`, saved and synced in `saveState`/`loadState` as `CreativeCompatLookOff`, `SrmLookSaveMixin`); plain snow becomes SRM's snowy plant holding nothing with the flag (the conversion SRM's own toggle makes); IW's snowy stone, cobblestone and bricks become the plain block. With the flag the client draws the held block's plain model and the snow layer, no snowy variant, drip sides or snow cap. New snow turns it back on: `Hooks.placeLayersOn` (storm piling, falling snow landing), a layer placed by hand (`SrmSnowPlaceLookMixin`, RETURN of `SnowLayerBlock.getStateForPlacement`) and snowfall on the column (`SrmWorldTickMixin`, after `handlePrecipitation`); a snowy plant holding nothing goes back to plain snow then. IW `block_snowcover.json` `growth_chance` 0.5 (was 0.1), `kubejs/data/immersive_weathering/block_growths/` |

Defaults taken where the rulings are silent:

- R19 plain snow: the flag lives in SRM's block entity (plain snow turns into SRM's snowy plant holding nothing, as SRM's
  own toggle does), not a new block state property on `minecraft:snow`. Same look, no change to every snow layer's
  states in Compat 1.0.0; owner to confirm (5b said "blockstate is default").
- R19 has no "on" click: only new snow turns the look back on. Plain snow over snowy grass, another snow layer, or a
  full 8-layer stack has no drip sides, so the click does nothing there. The IW stone swap gives no snowball.
- R13: blizzards are gated by the strength read in `WorldTickHandler.tick`, not a cancel in `doBlizzard`, since a
  nonzero strength replaces SRM's plain snowfall entirely; thunder brings falling snow instead of plain snowfall. A
  blizzard column already at 8 layers gets none (an overflow layer can still form once).
- R13: the 20% chance is a constant, not a config key; the gamerules are world defaults, so existing worlds keep 0
  and 10000 until `/gamerule` sets them.
- Salt: storm piling and falling snow landing on salt are stopped too (B3, beyond the step 11 list).
- R12: no `[wthit]` toggle for the name.

## 5. Decisions for the owner

| # | Question | Recommendation | Default if not answered |
|---|---|---|---|
| D1 | Which features in 3.1 are left out? | as the Recommendation column | as recommended |
| D2 | Falling into snow: keep full damage, or cushion by depth for everyone? | full damage (matches the 2026-11-19 layer ruling); SRM's cut off | full damage |
| D3 | SRM's thinner collision and deep-snow drag | off: our walk-through already slows by depth, the drag compounds per tick and only hits people on top, and the thinner shape changes mob pathing | off |
| D4 | Which falling-snow system? | ours (`FallingLayers`); SRM `snowGravity = false` (blizzards and snow-into-water ice go with it) | ours |
| D5 | Replacing snow: our 3 layers or SRM's 7? | ours (3) | ours |
| D6 | Storm piling vs "8 layers -> snow block": endless snow towers in snowy biomes | `snowAccumulationMaxLayers = 7` (storms pile to 7, never into a block); alternative: storms off | max 7 |
| D7 | Season melt: accept SRM replacing Serene Seasons' melt handler? | accept (it uses Calendar's temperatures; one random-tick path instead of a per-tick chunk shuffle) | accept |
| D8 | Should snowfall snow up fences, walls, slabs and stairs in place (villages, structures, outdoor builds), or plants only? The `not_containables` tag stops both hand and snowfall, so a by-hand-only split needs a pack mixin | owner taste: all (SRM default), plants only (tag the four block tags `not_containables`), or by hand only (mixin) | all (SRM default) |
| D9 | Immersive Weathering's snowy stone shapes: whose snow? | IW's: its snowy and frosty blocks and their sources not containable by SRM | IW's |
| D10 | Snow on leaves? | owner taste; costs nothing in code | on (SRM default) |
| D11 | Empty-hand overlay toggle (F18) | out: a hidden cosmetic toggle that converts a plain snow layer into SRM's block | out |
| D12 | WTHIT line naming the plant or block inside the snow | yes, small | no |
| D13 | Shovel and hand scoops (F15, F16) | keep, vanilla snow and SRM blocks only | keep |
| D14 | How deep may snow be for mobs to spawn on it (`mobSpawningMaxLayers`, 1 to 8)? | keep SRM's 8 | 8 |

## 5b. Owner rulings (2026-11-24)

Answered in one prompt pass (owner, 2026-11-24). They supersede the recommendations in sections 2 to 5 where they
differ; section 4 is read with these changes. The 3.1 "Owner" column carries the per-feature result.

| # | Ruling | Effect on the fold-in |
|---|---|---|
| R1 | SRM's thinner collision and deep-snow drag off (D3) | `thinnerBoundingBox = false`; step 13 stands as written |
| R2 | Fall damage into snow stays full for now (D2). **Later:** a pack fall damage reduction for passable layers (snow, leaf piles, like thin cobwebs) and for some BlockRunner materials (dust, snow, soft, ...) | `snowReduceFallDamage = false`; the later feature is TODO `blockrunner #21` (BlockRunner), not part of this integration |
| R3 | **SRM's falling snow for snow** (D4 differs from the recommendation); FallingLayers keeps salt, sawdust, bamboo leaves, fodder | `snowGravity = true`; **built 2026-11-24** (TODO `creative-compat-publish`, SQ-CMP-38): `FallingLayers.falls` skips `minecraft:snow` and `snowrealmagic` blocks while SRM is loaded (code gate, the tag is unchanged so the pack fall still covers snow without SRM; SQ-CMP-24); step 6 becomes: Vintage `SaltLayerBlock` overrides `tick` so SRM never falls salt (step 11); check that SRM's fall and FallingLayers never both act on one block (SRM-09) |
| R4 | Replacing snow: ours, up to 3 layers (D5) | `snowAlwaysReplaceable = false`; step 5 tags as written |
| R5 | **The "8 layers -> snow block" rule is dropped**; storm piling stays at SRM's cap of 8 (D6 differs) | Compat `[surface.passthrough.layers] snowFullBecomesBlock = false` (key already exists; `configs_edited` copy). An 8-layer stack stays a layer: SRM's piling filter never stacks on a full layer, so no towers. Walk-through stays 1 to 7; 8 layers have vanilla's 14 px collision (with R1). Update [blockrunner.md](blockrunner.md) 3.10 and 3.11 (snow row: "8 stays a layer"); changelog bullet when built |
| R6 | **Keep Serene Seasons' melt, drop SRM's melting, keep the pack's melting settings** (D7 differs) | `snowNeverMelt = true` (stops SRM's season and light melt in `Hooks.randomTick`). New Calendar mixin: Serene Seasons' `RandomUpdateHandler.onWorldTick` runs despite SRM's HEAD cancel (a HEAD hook applied first that runs the handler body through invokers, then lets SRM cancel the rest), Calendar's throttle kept. SRM's ice melt in `WorldTickHandler` (`snowAndIceMeltInWarmBiomes` path) canceled by a pack mixin so ice melts only by Serene Seasons and IW, as today. Serene Seasons' melt must also take SRM's snow blocks (melt to the held block); to check in its `meltInChunk` when built. Step 4 replaced |
| R7 | Snowfall snows plants and blocks in place (D8: SRM default) | no `not_containables` beyond R8 |
| R8 | IW's snow for IW's snowy shapes (D9) | step 8 as written |
| R9 | Snow on leaves kept (D10) | none |
| R10 | Both scoops kept (D13): **hand scoop 1 layer, shovel scoop 2 layers** per click (one snowball per layer); salt never scooped | Compat `@Pseudo` mixin on `snownee.snow.GameEvents.onItemUse`: shovel path takes 2 layers (2 snowballs, 1 durability per click; a 1-layer pile gives 1), hand path 1; non-snow blocks pass (step 11) |
| R11 | **Overlay always on for every snow layer** (vanilla and SRM's); the empty-hand toggle click is dropped (D11 differs) | Compat client: SRM blocks draw the overlay by default (mixin on `SnowBlockEntity` options or `SnowClient` render check); a render hook draws SRM's `block/overlay` model (the author's `snow_overlay.png`, no new texture) on the four sides of the block under every vanilla snow layer, Rubidium path included; the empty-hand click does nothing on snow (sneak scoop still works). Screenshot check CQ row when built |
| R12 | WTHIT names the snowed block as "Snowy <Block>" (for example "Snowy Oak Stairs") (D12) | Compat WTHIT plugin: the block name line for SRM's blocks; lang key `creativecompat.wthit.snowy` = "Snowy %s" |
| R13 | **Blizzards on, thunder only, strength 2, frequency 3500; snow falling into water turns it to ice with a 20% chance** | pack default gamerules `snowrealmagic:blizzardStrength 2`, `blizzardFrequency 3500` (where the pack sets default gamerules); Compat `@Pseudo` HEAD hook on `WorldTickHandler.doBlizzard`: only while thundering; `snowMakingIce = true` with a Compat redirect in `FallingSnowEntity.tick` that rolls 20% |
| R14 | Mobs spawn on snow up to 8 layers (D14) | none |
| R15 | Remaining features kept: worldgen snowy plants, grass under snow, snowy foliage models, snow by hand in plants | none |
| R16 | Every pack-side fix built: magma vs SRM snowfall (step 3), IW rain keeps the plant (step 8), snowed blocks keep the held block's speed (step 7), plus the light-melt boot fix, melting tags, salt guards | as written |
| R17 | **One pack grass spread tick**, and **grass with snow on top never spreads** | step 9 replaced: a Compat HEAD hook on `SpreadingSnowyDirtBlock.randomTick` applied before SRM's does the whole tick and cancels: covered -> dirt (SRM's snow survival rule, vanilla's water rule kept), Calendar's season check, Neapolitan's block rule (`canBeGrass`), Vinery's slab spread, and no spread at all from a grass block with any snow layer or SRM snow block on it; new grass is snowy under any `#snowrealmagic:bottom_snow`. Boot log confirms the order |
| R18 | Salt melts snow faster (D13 follow-up): Salt Melt and the salt dispenser **4 layers** per use (was 3); a salt block touching snow removes **1 layer 55%, 2 layers 35%, 3 layers 10%** per melt (was 1) | `[melting] saltMeltSnowLayers = 4`; new `[melting]` odds key for the touching salt block (Compat `Melting`); [melting.md](melting.md) 1 and 4, changelog bullets (salt is a live feature) |
| R19 | **A plain right-click (no sneak, empty hand) turns off a block's snowy look; the snow itself stays.** Only new snow (a layer placed, snowfall or storm piling adding a layer, falling snow landing) turns the look back on. Applies to snowy plants, snow-topped fences, gates, walls, slabs, stairs, the snow-drip sides under a plain snow layer (R11), and IW snowy stone, cobblestone and bricks. Sneak right-click stays the 1-layer hand scoop (R10). **IW snowy stone, cobblestone and bricks convert from plain much faster once snow falls** | Compat: a per-block "snow look off" flag (SRM blocks: a field in SRM's block entity options, saved; plain snow layers: a block state property of our own, owner 2026-11-24 "blockstate is default"), cleared by `Hooks.placeLayersOn`, `Hooks.convert` and the vanilla snow placement path. Client: SRM's snowy foliage variant, the drip overlay and the snow cap model skip while the flag is set; the snow layer's own blocks, layers and collision are unchanged. IW snowy stone has no snow layers of its own, so "look off" turns it back into the plain block; IW's growth re-snows it. IW `block_snowcover.json` (builtin `snowy_stones`, tick source `snow`) `growth_chance` 0.1 -> **0.5** (confirmed, owner 2026-11-24), as a pack data override. Open at build (assumption stands): what "snowy look" on a snowy plant hides besides the variant texture and the drip (the snow layer model stays visible, since the snow stays) |

Snow by layer after these rulings: mining with a shovel takes the stack (1 snowball per layer); the shovel scoop 2
layers per click, the hand scoop 1, both with snowballs; Salt Melt or the dispenser 4 layers; a touching salt block
1.55 layers per melt on average; fire and lava 1 layer per melt; magma within 2 all at once; IW rain the whole stack;
Serene Seasons by its own rule outside winter.

## 6. File map

Built (part A, 2026-11-25), under `development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/`:

- `compat/SnowRealMagic.java`: `LOADED`, `isVariant`, `isSnowLayer`, `held` (the held block), `snowOnTop`, Vinery's
  slab spread handle; `compat/CompatMixinPlugin.java` (gates, range `[6.5.4,6.6)`).
- `mixin/SrmWorldTickMixin.java` (magma, SRM ice melt off), `mixin/SrmSnowCollisionMixin.java` (walk-through before
  SRM's cache), `mixin/SrmGrassSpreadMixin.java` (the grass tick), `mixin/IwSnowMeltSrmMixin.java` (IW rain keeps the
  plant); `resources/creativecompat.mixins.json`.
- `melting/Melting.java` (`meltTo` held block, deferred placement check), `surface/PassThrough.java` (`heldPlant`),
  `surface/CamoSpeed.java` (SRM held block speed).
- `kubejs/data/creativecompat/kubejs/tags/melting_tags.js`, `passthrough_tags.js`, `blocktags.js` (`not_containables`).
- Creative Common `kubejs_startup/creativecommon/_lib/block_dict.js` (`b.srm_*`, `b.t_srm_not_containables`).
- Creative Calendar `mixin/SereneSeasonsSrmMeltMixin.java`, `compat/CalendarMixinPlugin.java` (`dropSrmMeltCancel`,
  needs Serene Seasons too), `mixin/GrassSpreadSeasonMixin.java` (priority 800), `build.gradle` (SRM compileOnly).
- `config/creativecompat-common.toml` (`snowFullBecomesBlock = false`) and its `configs_edited/root_config/config/` copy.

Built (part B, 2026-11-25, 4c), same tree:

- `compat/SnowRealMagic.java` (`onUse`, look flag `lookOff`/`lookOn`/`placedLookOn`, `notSnowLayer`,
  `fullSnowAbove`, constants), `compat/SnowRealMagicClient.java` (drip sides, look-off render, client setup),
  `compat/SrmLookFlag.java`, `compat/WthitSnowy.java`, `resources/waila_plugins.json`, lang `creativecompat.wthit.snowy`.
- `mixin/SrmUseMixin.java`, `SrmHooksMixin.java`, `SrmFallingSnowMixin.java`, `SrmGameruleDefaultsMixin.java`,
  `SrmSnowPlaceLookMixin.java`, `SrmLookOptionsMixin.java`, `SrmLookSaveMixin.java`, client `SrmRenderMixin.java`;
  `SrmWorldTickMixin.java` (blizzards, snowfall turns the look on); `CreativeCompat.java` (client setup).
- `melting/Melting.java` (`rollLayers`, salt source odds), `CompatConfig.java` (`saltMeltSnowLayers` 4,
  `saltSourceSnowLayerOdds`), `config/creativecommon-common.toml` (the live `[melting]` Compat copies on its next boot)
  and `configs_edited/root_config/config/creativecompat-common.toml`; `tools/melting_test` checks.
- Creative Delight `members/vintage/.../block/custom/SaltLayerBlock.java`.
- `kubejs/data/immersive_weathering/block_growths/block_snowcover.json` (0.5).

Still waiting for the first boot: `config/snowrealmagic-common.toml` and `-client.toml` (4b step 10 values, plus
`snowMakingIce = true`, the default).

## 7. Offline checks (once built)

- `python3 tools/check_all.py --fast --changed` (Java compile, tag helper lint, privacy, changelog lint).
- `tools/blockrunner_scan.py --audit`: the 7 SRM blocks are tiered (by held block) or listed as handled.
- Boot log: no `Critical injection failure`, `Mixing VanillaLightMeltMixin` / `SrmLightMeltMixin` present, the
  `[melting] tags:` line counts the SRM blocks, no `@Overwrite conflict` naming `SnowLayerBlock`.

## 8. In-game test suite (drafted)

- [ ] **SRM-01 Boot:** (queue SQ-SRM-01)**:** start the client and a dedicated server with SRM and the fixed Compat -> no crash; `latest.log` has `SereneSeasons detected. Overriding melting behavior.` and no injection failure.
- [ ] **SRM-02 Light melt off:** 3 snow layers beside a sea lantern and a redstone lamp, wait 5 minutes (`randomTickSpeed` 100) -> they stay; beside a torch -> they lose layers (fire rule).
- [ ] **SRM-03 Snowy plant melts:** (queue SQ-SRM-02)**:** place snow on a poppy (one `snowrealmagic:snow`), put a torch beside it -> the snow melts layer by layer and the poppy is left standing.
- [ ] **SRM-04 Magma vs SRM snowfall:** (queue SQ-SRM-07)**:** a magma block in a snowy plains clearing, `/weather thunder` for 10 minutes at `randomTickSpeed` 100 -> no snow within 3 of it; snow beyond.
- [ ] **SRM-05 Wading:** walk and sprint through 2, 5 and 7 layers of snow barefoot -> the same speeds as before SRM (BR walk-through values); WTHIT shows "N of 8 layers".
- [ ] **SRM-06 Standing on top:** the same in leather boots -> Snow tier 0.75, no extra drag (F2 off).
- [ ] **SRM-07 Wading a snowy plant:** (queue CQ-SRM-01, with SRM-17, SRM-24)**:** walk through snow on tall grass and on a sweet berry bush -> walk-through like snow, the bush's 0.36 where it is lower; no SRM 0.8 stuck.
- [ ] **SRM-08 Fall into snow:** drop 10 blocks into 7 layers barefoot and in leather boots, and onto bare grass -> the same damage all three times (D2 = full).
- [ ] **SRM-09 Falling snow (R3):** break the block under 5 layers on a pillar -> one fall as SRM's falling snow, the layers land and merge; no FallingLayers entity for snow; a salt layer on a pillar falls as salt (FallingLayers), never as snow. Queued as SQ-CMP-38 (2026-11-24).
- [ ] **SRM-10 Replace:** place cobblestone into 3 and 4 layers of snow and into a snowy poppy -> 3 replaced, 4 on top (`replaceMaxLayers` 3).
- [ ] **SRM-11 Storm cap (R5):** snowy plains, `/weather thunder` 30 minutes at `randomTickSpeed` 100 -> snow piles to 8 layers and stays a layer (no snow block, nothing stacks above it); 8 layers are stood on, 1 to 7 waded.
- [ ] **SRM-12 Season melt (R6):** (queue SQ-SRM-06)**:** Calendar spring in a temperate biome, 3 layers and ice outdoors -> Serene Seasons melts them as before SRM (its handler runs, throttled); snow beside a sea lantern does not melt; in a frozen-type biome -> they stay.
- [ ] **SRM-13 IW rain melt on a snowy plant:** (queue SQ-SRM-04)**:** rain on a snowy dandelion in a warm biome -> the snow goes, the dandelion stays.
- [ ] **SRM-14 IW snowy stairs:** IW snowy stone stairs beside snow in a storm -> they stay IW's block (not wrapped by SRM); a torch beside them melts them to stone stairs.
- [ ] **SRM-15 Salt layers (R10):** (queue SQ-SRM-10)**:** right-click a salt layer with a shovel and sneak right-click it with an empty hand -> no snowballs, no layer taken; break the block under a salt layer -> it falls as salt (FallingLayers), lands as salt; place a snow layer item on a salt layer -> no snow added to the salt; fall 10 blocks onto 6 salt layers -> full damage.
- [ ] **SRM-16 Scoops (R10):** (queue SQ-SRM-09, SQ-SRM-10)**:** shovel right-click on 5 layers of snow -> 3 layers, 2 snowballs, 1 durability; on 1 layer -> gone, 1 snowball; sneak empty-hand on 3 layers -> 2 layers, 1 snowball in hand; a plain empty-hand click -> nothing.
- [ ] **SRM-17 Snowed wood stair speed:** walk on a snow-topped oak stair -> WTHIT and speed Wood 1.10x (held block's tier).
- [ ] **SRM-18 Grass spread season:** Calendar out-of-season grass, dirt beside grass, 20 minutes at `randomTickSpeed` 100 -> spread slowed as before SRM (Calendar's check runs); Neapolitan and Vinery grass behavior unchanged.
- [ ] **SRM-19 Mob pathing:** a zombie chasing across 6 layers of snow -> it treats the pile as a step up (3.11), as before SRM.
- [ ] **SRM-20 Worldgen:** new snowy plains chunks -> grass and flowers under snow (`snowrealmagic:snow`), no errors in the log.
- [ ] **SRM-21 Snowed fence drops:** break a snow-topped oak fence and a snowed poppy -> the fence or poppy plus the snow; pick block gives the fence or poppy.
- [ ] **SRM-22 Nether snow:** (queue SQ-SRM-03)**:** place snow on a plant in the Nether -> it fizzes away (ultra-warm instant), the plant stays.
- [ ] **SRM-23 Rubidium render:** client with Rubidium, snowy foliage on and off (`snowVariants`) -> no missing models, no render errors.
- [ ] **SRM-24 Dedicated server sync:** player on a dedicated server wades 5 layers and stands on a snowy poppy -> no rubber-banding (client and server agree on collision).
- [ ] **SRM-25 Overlay (R11):** (queue CQ-SRM-02)**:** client: a snow layer on stone, on dirt and a snowy poppy -> the block under each shows SRM's snow-drip sides; with Rubidium on too; screenshot for the owner.
- [ ] **SRM-26 Blizzard (R13):** (queue SQ-SRM-08, SQ-SRM-13)**:** snowy plains, `/weather rain` 10 minutes -> no falling snow from the sky; `/weather thunder` 10 minutes -> falling-snow chunks of 1 to 2 layers land; `/gamerule snowrealmagic:blizzardStrength` reads 2, frequency 3500 on a new world.
- [ ] **SRM-27 Snow into water (R13):** (queue SQ-SRM-12)**:** drop 20 falling-snow chunks into still water (break their support over a pool) -> about 4 turn the water to ice, the rest add nothing.
- [ ] **SRM-28 Snowy grass does not spread (R17):** (queue SQ-SRM-05)**:** a grass block with 1 snow layer on it beside dirt, 20 minutes at `randomTickSpeed` 100 -> the dirt stays dirt; the same without snow -> it spreads; grass spreads onto a Vinery dirt slab; grass under 5 layers stays grass.
- [ ] **SRM-29 Salt odds (R18):** (queue SQ-SRM-14)**:** Salt Melt on 8 layers -> 4 layers; a salt block beside 8 layers, log 40 melts -> about 55% take 1 layer, 35% 2, 10% 3.
- [ ] **SRM-30 WTHIT name (R12):** (queue CQ-SRM-02)**:** look at a snow-topped oak stair and a snowy poppy -> "Snowy Oak Stairs", "Snowy Poppy".
- [ ] **SRM-31 Snow look off (R19):** (queue SQ-SRM-11, CQ-SRM-02)**:** empty-hand right-click a snowy poppy, a snow-topped oak stair, a plain 3-layer snow on stone and IW snowy cobblestone -> each loses its snowy look (no snowy texture, no drip sides; the snow layers stay, walk-through unchanged; the cobblestone becomes plain cobblestone); place one snow layer on each (or let snow fall) -> the look comes back; sneak right-click still scoops 1 layer.
- [ ] **SRM-32 IW snowy stone rate (R19):** a 9x9 cobblestone floor in a snowy biome, `/weather rain` for 5 minutes at `randomTickSpeed` 3 -> noticeably more snowy cobblestone than with IW's default 0.1 (count both runs).

## 9. History

- 2026-11-24: investigation written from the 6.5.4 jar and decompiled code, nothing built. B1 found before the first
  boot. Feature catalog F1 to F27 for the owner's marks.
- 2026-11-24, later: owner rulings R1 to R18 (section 5b); every decision answered, catalog marked. Nothing built.
- 2026-11-24, later: R19 (right-click turns the snowy look off; IW snowy stone converts 5x faster).
- 2026-11-24, later: confirmed R19 rate 0.5 and block state flag, R11 overlay as ruled, SRM default particles; fall damage reduction pinned as `blockrunner #21`.
- 2026-11-24, later: R3's snow half built (owner: "snow falling should be handled by SRM now, use pack feature if not
  downloaded"): Compat's `FallingLayers.falls` leaves snow to SRM when it is loaded, logs `Snow! Real Magic loaded: snow
  falls by its snowGravity` once; `SnowLayerFallMixin` still applies (its HEAD inject holds salt over a free block, so
  SRM's `updateShape` body never schedules a fall for it). Left for step 11: SRM's added `tick` and `onPlace` still run on
  salt, so a fall check SRM scheduled before the support went could still drop a salt layer as snow (rare; SQ-CMP-38).
- 2026-11-25: fold-in part A built (section 4b; `mod-integration #14` to `#19`, `#26`): melting to the held block,
  magma vs SRM snowfall, walk-through and held-block speed, IW guard, one grass tick, Serene Seasons' melt kept;
  `snowFullBecomesBlock = false`; SRM config values written as a plan (its toml appears on the first boot). Queue rows
  SQ-SRM-01 to -07, CQ-SRM-01.
- 2026-11-25, later: fold-in part B built (section 4c; `mod-integration #20` to `#25`): shovel scoop 2 layers, drip
  sides under every snow layer, snowy look off by an empty-hand click, WTHIT "Snowy" names, thunder-only blizzards
  with 2 / 3500 defaults, 20% ice from snow in water, salt odds 55/35/10 and Salt Melt 4 layers, salt layers keep
  vanilla rules under SRM, IW snowy stone growth 0.5. Queue rows SQ-SRM-08 to -14, CQ-SRM-02.
