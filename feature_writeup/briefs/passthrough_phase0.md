# Pass-through blocks: phase 0 findings (2026-11-14)

Phase 0 of [plan_passthrough.md](../plan_passthrough.md): read-only verification of the jars in `mods/` (nested jars
included), the 1.19.2 Mojmap/SRG jar and `srg_to_official_1.19.2.tsrg`. Method: a class index over every jar (81,977
classes, superclass chains, constant-pool grep), vineflower 1.10.1 and `javap -c -p` on the classes below, plus the
dumps `kubejs/exported/crops/crop_growth_audit.json` and `kubejs/exported/tags/*.json`. No code or config was edited.

Read the last section first: **Implications for phases 1-5**.

## 1. Hook names (SRG, for `remap = false` mixins)

### 1.1 Entity / blocks

| Target | SRG | Descriptor | Notes |
|---|---|---|---|
| `Entity#makeStuckInBlock` | `m_7601_` | `(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/phys/Vec3;)V` | plan's name confirmed |
| `Entity#checkInsideBlocks` | `m_20101_` | `()V` | plan's name confirmed; WallSlideMixin already at TAIL |
| `Entity#stuckSpeedMultiplier` | `f_19865_` | Vec3 | read only by `Entity#move` (`m_6478_`) |
| `Entity#fallDistance` | `f_19789_` | float | `resetFallDistance` = `m_183634_` |
| `Entity#isNoGravity` / `setNoGravity` | `m_20068_` / `m_20242_` | `()Z` / `(Z)V` | |
| `Entity#move` | `m_6478_` | `(MoverType;Vec3)V` | calls `checkInsideBlocks` inside |
| `Entity#xo/yo/zo` | `f_19854_/f_19855_/f_19856_` | double | |
| `Entity#isSwimming`, `isInWater`, `isEyeInFluid` | `m_6069_`, `m_20069_`, `m_204029_` | | |
| `Entity#playerTouch` | `m_6123_` | `(Player)V` | |
| `LivingEntity#handleOnClimbable` | `m_21297_` | `(Vec3)Vec3` | `onClimbable` = `m_6147_`, `travel` = `m_7023_`, `jumpFromGround` = `m_6135_` |
| `Block#entityInside` (`BlockBehaviour`) | `m_7892_` | `(BlockState;Level;BlockPos;Entity)V` | `getShape` `m_5940_`, `getCollisionShape` `m_5939_` |
| `BlockStateBase#getShape` | `m_60651_` (ctx) / `m_60808_` | | `getCollisionShape` `m_60742_` / `m_60812_` |
| `AbstractClientPlayer#getFieldOfViewModifier` | `m_108565_` | `()F` | |

### 1.2 `makeStuckInBlock` overrides and callers (grep of every jar for `m_7601_` / `makeStuckInBlock`, class files and mixin json/refmaps)

- **Overrides:** only vanilla `Spider#makeStuckInBlock` (`m_7601_`): `if (!state.is(Blocks.COBWEB)) super...`. So
  spiders are exempt **only from `minecraft:cobweb`**, not from reinforced cobweb, Fantasy Furniture webs or barbed wire.
- **No mod mixes into or overrides `m_7601_`.** No refmap or mixin config references it. No entity class other than
  Spider declares it.
- **Callers (all `entityInside`, multiplier x,y,z):**

| Block class (jar) | Multiplier | Tiered in plan |
|---|---|---|
| vanilla `WebBlock` | 0.25, 0.05, 0.25 | cobweb |
| SecurityCraft `ReinforcedCobwebBlock` | 0.25, 0.05, 0.25; **owner (OwnableBlockEntity) is skipped** | cobweb |
| Fantasy Furniture `SpiderWebBlock`, `SpiderWebMultiBlock` | 0.25, 0.05, 0.25 | cobweb |
| Create Crafts & Additions `BarbedWireBlock` | 0.25, 0.05, 0.25 for every entity; damage `BARBED_WIRE_DAMAGE` to living entities whose x+y+z delta > 0 | cobweb |
| vanilla `SweetBerryBushBlock` | 0.8, 0.75, 0.8 | bush |
| Nether's Delight `PropelplantBlock` | 0.8, 0.75, 0.8 (living); **explodes and removes itself** when a living entity enters not sneaking, and when any `Projectile` enters (server) | cane |
| IW `LeafPileBlock` | `COLLISIONS[layers-1]`, 1.0, same | leaf_pile |
| Twigs `BambooLeavesBlock` | 0.75, 1.0, 0.75 when `layers > 2` (3+), skipped for entity tag `twigs:bamboo_leaves_slow_immune` | leaf_pile |
| vanilla `PowderSnowBlock` | (out of scope) | none |
| Supplementaries `BambooSpikesBlock` | (out of scope) | none |
| Create `CrushingWheelControllerBlock`, D&D `MmbCrushingWheelControllerBlock` | (out of scope) | none |
| Creative Delight Hearth `RaspberryBushBlock` | 0.8, 0.75, 0.8 | **not listed** (berry-bush twin) |
| Floral Enchantment `VineBushBlock` (17 `*VineBushBlock` subclasses) | 0.9, 0.85, 0.9 | **not listed** |
| Farmer's Respite `WitherRootsBlock` | 0.8, 0.75, 0.8 | not listed |
| Neapolitan `BeanstalkThornsBlock` | 0.95, 0.95, 0.95 | not listed |
| Upgrade Aquatic `PickerelweedBlock` / `PickerelweedPlantBlock` / `PickerelweedDoublePlantBlock` | 0.6,1,0.6 / 0.75 / 0.75 | not listed (tall pickerelweed is a 2-tall plant) |
| Upgrade Aquatic `MulberryJamBlock`, `GuardianSpineBlock` | 0.2 / 0.25,0.5,0.25 | none (solid-ish, out) |
| Miner's Delight `StickyBasketBlock` | 0.25, 0.05, 0.25 | none |
| Creative Delight `PowderedSugarBlock` | 0.9, 1.5, 0.9 | none |
| Engineer's Decor `EdLadderBlock` | (ladder speed) | none |

- **Vinery taiga grape bush has no `entityInside` in the installed port** (backportedVinery 1.5.5-ce-19.2-09 and the
  in-tree source): it does not slow today. `StuckInBlock`'s entry for `GrapeBushBlock$TaigaGrapeBush` is stale.
- `StuckInBlock` also lacks the raspberry bush, Floral vine bushes, wither roots, beanstalk thorns and pickerelweed.

### 1.3 IW `LeafPileBlock#entityInside` (`m_7892_`)

`layers > 3 && CommonConfigs.LEAF_PILES_SLOW && LivingEntity && !Fox && !Bee && DepthStrider == 0` -> 
`makeStuckInBlock(COLLISIONS[layers-1], 1, same)`; **inside that same branch** spruce thorns (`layers >= 6`,
`hasThorns`, server, moving >= 0.003, players with leggings skipped) deal `0.5 x (layers - 5)`. Client particles run
separately. Layer property `layers` 0-8 (IW), Twigs bamboo leaves `layers` 1-8.

## 2. Crops and plants (plan sections 1.1 and 2)

### 2.1 Tall and dense crops

| Plant | Block id(s) | Class (< parent) | Structure | Growth property, max | Upper appears when | Half-grown one-block stage? |
|---|---|---|---|---|---|---|
| Corn | `corn_delight:corn_crop` (one id, both blocks) | `CornCrop` < `HighCropBlock` (mmlib) < `BaseCropBlock` | lower `upper=false`, top `upper=true`; each block has its **own** `age` | `age` 0-7, bool `upper` | lower age >= 4 (`getGrowUpperAge()` 4) | yes (lower age 4 before the top appears) |
| Rice | `farmersdelight:rice` + `farmersdelight:rice_panicles` | `RiceBlock` < `BushBlock` (waterlogged, `LiquidBlockContainer`); `RicePaniclesBlock` < `CropBlock` | base in a water source; panicles on top | base `age` 0-3 + bool `supporting`; panicles `age` 0-3 | base at max age (3) | yes (base age 2) |
| FD tomatoes | `farmersdelight:budding_tomatoes` -> `farmersdelight:tomatoes` | `BuddingTomatoBlock` < `BuddingBushBlock`; `TomatoVineBlock` < `CropBlock` | budding is one block; at age 4 it becomes `tomatoes` age 0. Vines climb a rope above (30% per tick roll), up to 3 segments, each `ropelogged=true` | budding `age` 0-4; vine `age` 0-3 + bool `ropelogged` | n/a (stacked segments, each its own age) | no lower/upper pair |
| Supplementaries rope / stick tomatoes | `supplementaries:rope_tomatoes`, `supplementaries:stick_tomatoes` | `FarmersDelightCompatImpl$TomatoRopeBlock` / `$TomatoStickBlock` < `$TomatoLoggedBlock` < `TomatoVineBlock` | a tomato segment merged into a rope (n/s/e/w/knot props) or stick (`axis_x/axis_z`) | `age` 0-3 | as FD | no pair. **Stick tomatoes have a collision shape** (stick) |
| Creative Delight treated stick tomatoes | `creativedelight:treated_stick_tomatoes` | `TreatedStickTomatoBlock` < `TomatoVineBlock` | tomato on a treated stick, climbs | `age` 0-3, `ropelogged`, `axis_x/axis_z` | as FD | no pair. **Has a collision shape** (stick) |
| IE hemp | `immersiveengineering:hemp` (one id) | `HempBlock` < `BushBlock` | lower `growth=bottom0..bottom4`, top `growth=top0` | enum `growth`: `bottom0, bottom1, bottom2, bottom3, bottom4, top0` (lower max = `bottom4`) | lower at `bottom4` | yes (`bottom2`, `bottom3`) |
| Vintage Delight oats | `vintagedelight:oat_crop` | `OatBlock` < `CropBlock` | **one block** (not tall) | `age` 0-7 | n/a | n/a |
| Wheat | `minecraft:wheat` | `CropBlock` | one block | `age` 0-7 | n/a | n/a |
| Supplementaries flax (not in the plan) | `supplementaries:flax` | `FlaxBlock` < `CropBlock` | two blocks, `half=lower/upper`, **both halves carry the same `age`** | `age` 0-7 + `half` | age >= 4 (`DOUBLE_AGE`) | no (two blocks from age 4 = half) |
| Farmer's Respite coffee | `farmersrespite:coffee_bush` (+ upper half), `coffee_stem`, `coffee_stem_middle`, `coffee_stem_double`, `coffee_bush_top` (lower/upper) | `CoffeeBushBlock`, `CoffeeBushTopBlock` (`half`), `CoffeeStemBlock`, `CoffeeMiddleStemBlock`, `CoffeeDoubleStemBlock`, all < `BushBlock` | young bush is 2 tall; it grows into stem + 2-tall top (3 blocks), then double stem + middle stem + top (4 blocks) | stems: `age` 0-2 (+`facing`; double stem also `age1` 0-2) = **berry ripeness**, reset to 0 on harvest; bush/top: no age | always 2+ blocks | no: 2 tall from planting |

### 2.2 Resulting growth-rule table (plan 1.1)

`half` means `value / max >= 0.5` (int ages: age >= ceil(max/2)).

| Block(s) | Rule | Threshold | Notes |
|---|---|---|---|
| `minecraft:wheat`, `vintagedelight:oat_crop`, other one-block `CropBlock`s | `half` | age >= 4 of 7 | oats are one block, not `tall_half` |
| `corn_delight:corn_crop` | `tall_half` | lower (`upper=false`) age >= 4 | top block's own age starts at 0, so the top must look down; the top only exists once the lower is at 4, so the top always slows |
| `farmersdelight:rice` | `half` | age >= 2 of 3 | base block; wading drag |
| `farmersdelight:rice_panicles` | `any` (equivalent to `tall_half`) | | panicles exist only on a max-age base |
| `immersiveengineering:hemp` | `tall_half` on enum | lower `growth` in {bottom2, bottom3, bottom4}; `top0` looks down | enum ordinal / 4 |
| `supplementaries:flax` | `half` | age >= 4 of 7 | both halves carry the age; the top only exists at 4+ |
| `farmersdelight:budding_tomatoes` | `half` | age >= 2 of 4 | |
| `farmersdelight:tomatoes`, `supplementaries:rope_tomatoes`, `supplementaries:stick_tomatoes`, `creativedelight:treated_stick_tomatoes` | `any` (proposed) | | a vine segment is already a grown plant; its `age` is fruit ripeness and resets to 0 on harvest (FD `use`), so `half`/`tall_half` would switch a picked field off |
| Farmer's Respite coffee (`coffee_bush`, `coffee_bush_top`, `coffee_stem`, `coffee_stem_middle`, `coffee_stem_double`) | `any` | | **no half-grown one-block stage** (2 tall from planting); stem age is berry ripeness |
| Wild crops | `any` | | no growth stage |

Tall crops with no half-grown one-block stage (slow as a whole, always): Farmer's Respite coffee; flax behaves the
same under `half`. Corn, rice and hemp all have a half-grown one-block stage.

### 2.3 Other tall plants and 2-tall bushes found (not in the plan's lists)

- 2-tall bushes/crops: Farmer's Respite `tea_bush` / `small_tea_bush` (DoubleBlockHalf), Fruits Delight
  `DoubleBushBlock` / `DoubleFruitBushBlock` (e.g. `fruitsdelight:lemon_tree`, already `surface/flora`), Vinery grape
  bushes (`red_grape_bush`, `white_grape_bush`, savanna and taiga: `GrapeBushBlock` < `BushBlock`, `age` 0-3,
  `Properties.copy(SWEET_BERRY_BUSH)` so no collision, no slowdown today).
- `minecraft:crops` (35) misses many modded crops (Hearth and Harvest, Creative Delight), so phase 4 needs the
  `crop_growth_audit` `cropLike` list or explicit ids, not the tag alone.

### 2.4 Wild crops

`farmersdelight:wild_crops` (22): `corn_delight:wild_corn`, `delightful:wild_salmonberries`, `farmersdelight:wild_*`
(beetroots, cabbages, carrots, onions, potatoes, rice, tomatoes), `miners_delight:wild_cave_carrots`,
`oceanic_delight:wild_sea_grape`, `rusticdelight:wild_*` (bell_peppers, coffee, cotton, dark_bell_peppers,
pale_bell_peppers), `veggiesdelight:wild_*` (broccoli, cauliflowers, garlic, sweet_potatoes, turnips, zucchinis).
**Not in the tag:** `vintagedelight:wild_oats`, `wild_cucumbers`, `wild_ghost_peppers`, `wild_peanuts`,
`farmersrespite:wild_coffee_bush`, `wild_tea_bush` (a `TallGrassBlock` subclass), `fruitsdelight:wild_pineapple`,
`hearthandharvest:wild_hops`. `farmersdelight:wild_rice` is 2 tall (`WildRiceBlock` < `DoublePlantBlock`).

### 2.5 Canes

- Nether's Delight: **four** ids use `PropelplantBlock`, all `noCollission`: `nethersdelight:propelplant_stem`,
  `propelplant_berry_stem`, `propelplant_cane`, `propelplant_berry_cane` (torches are `TorchBlock`). Walking in
  without sneaking makes it explode and vanish, so its walk value matters only while sneaking.
- **Supplementaries has no propelplant.** `minecraft:sugar_cane` is plain vanilla.

### 2.6 Vines

| Group | Ids | Class | Climbable | Notes |
|---|---|---|---|---|
| vanilla | `minecraft:vine`, `twisting_vines(_plant)`, `weeping_vines(_plant)`, `cave_vines(_plant)` | `VineBlock`, `GrowingPlant*` | yes (tag) | cave vines `age` 0-25 |
| Vinery grape vines | `vinery:jungle_grape_bush_red`, `jungle_grape_bush_white` | `GrapeVineBlock` < `VineBlock` | yes (`minecraft:climbable`, `minecraft:fall_damage_resetting`) | `Properties.copy(SWEET_BERRY_BUSH)`: no collision; outline = vanilla vine faces (1/16 thick); `age` 0-3, `sterilized` |
| Vinery stems | `vinery:grapevine_stem` (`PaleStemBlock`), `vinery:grapevine_lattice` (`LegacyLatticeStemBlock`, climbable) | Vinery's own `StemBlock` < `Block` | lattice yes | **solid** (`getGrapevineSettings()` has no `noCollission`): not pass-through |
| Floral Enchantment | 17 `floralench:*_vine` (allium ... wither_rose) | `VineBaseBlock` / `WitherVineBaseBlock` < `VineBlock` | yes | the "cornflower vine" of the plan is one of 17. Separate `floralench:*_bush` blocks (`VineBushBlock`) call `makeStuckInBlock` 0.9 |
| Quark | `quark:cut_vine` | `CutVineBlock` < `QuarkVineBlock` < `VineBlock` | yes | |
| Botania | `botania:solid_vine` | `SolidVineBlock` < `VineBlock` | yes | |
| Biomes O' Plenty | `spanish_moss(_plant)`, `glowworm_silk(_strand)`, `flesh_tendons(_strand)`, `hanging_cobweb(_strand)` | `SpanishMoss*`, `HangingStrand*` < `GrowingPlant*` | flesh tendons only | `hanging_cobweb` is a strand, not a `WebBlock`, no slowdown today |
| IW | `immersive_weathering:ivy` | `IvyBlock` < `MultifaceBlock` | yes | |
| Upgrade Aquatic | `upgrade_aquatic:mulberry_vine` (`Block`, `age` 0-4); kelp (`UAKelp*`, underwater) | | no | |
| Neapolitan | `neapolitan:vanilla_vine(_plant)` | `VanillaVine*` < `Block` | no | |
| Other climbables | `minecraft:hanging_roots` | | yes | |
| Not vines | `vintagedelight:magic_vine(_block)` | `RotatedPillarBlock` | yes (tag) | solid log-like block |

### 2.7 2-tall plants (`DoublePlantBlock` and subclasses; blockstates with `half=upper`)

- Vanilla: `tall_grass`, `large_fern` (plain `DoublePlantBlock`), `sunflower`, `lilac`, `rose_bush`, `peony`
  (`TallFlowerBlock`), `small_dripleaf`, `tall_seagrass` (underwater).
- Biomes O' Plenty: `eyebulb` (`EyebulbBlock` < `DoublePlantBlockBOP`), `blue_hydrangea`, `goldenrod`, `icy_iris`,
  `tall_lavender`, `sea_oats`, `watergrass`, `brimstone_cluster`; out by ruling: `barley`, `cattail`, `reed`.
- Botania: 16 `*_double_flower` (`BotaniaDoubleFlowerBlock`).
- Upgrade Aquatic: `flowering_rush`, `tall_beachgrass`, `tall_blue_pickerelweed`, `tall_purple_pickerelweed`
  (pickerelweed calls `makeStuckInBlock` 0.75 today).
- Farmer's Delight `wild_rice` (wild crop). Not plants: waystones, sharestones, wells, chimes, doors, curtains.

### 2.8 Ropes (plan 3.2)

| Rope | Id | Class | Collision | Climbable | `entityInside` |
|---|---|---|---|---|---|
| Farmer's Delight | `farmersdelight:rope` | `RopeBlock` < `IronBarsBlock` | none (`Shapes.empty()`) | tag `minecraft:climbable` | none; outline = bars post + connections; `tied_to_bell` |
| Supplementaries | `supplementaries:rope` | `RopeBlock` < `WaterBlock` (waterloggable) | **outline shape**, except **empty for living entities** when `up`, or when `down` and the entity is not standing on the knot top | `isLadder` only when `down` and (`up` or entity y - pos y < 0.8125); a rope end with no `down` is **not** a ladder | breaks itself (server, drop) when a vanilla `Arrow` enters |
| Quark (not in the plan) | `quark:rope` (module enabled, `Rope = true`) | `RopeBlock` < `QuarkBlock` (waterloggable) | none | `isLadder` true | none |
| Beautify | `beautify:rope` (**there is no "twine"**) | `Rope` < `ChainBlock` | **chain collision** (thin post; registry uses `noOcclusion`, not `noCollission`) | `isLadder` true | none |

Props: Supplementaries `north/south/east/west/up/down`, `distance` 0-7, `knot`, `waterlogged`. Horizontal Supplementaries
ropes (no up/down) collide for living entities, so only vertical runs are walk-through.

## 3. Cobwebs (plan 4)

| Id | Class (< parent) | Collision |
|---|---|---|
| `minecraft:cobweb` | `WebBlock` | none |
| `securitycraft:reinforced_cobweb` | `ReinforcedCobwebBlock` < `BaseReinforcedBlock` < `OwnableBlock` < `BaseEntityBlock` | empty; owner exempt |
| `fantasyfurniture:decorations/spider_web_small`, `fantasyfurniture:decorations/spider_web_wide` | `SpiderWebBlock` < `BaseBlock`, `SpiderWebMultiBlock` < `BaseMultiBlock` | |
| `createaddition:barbed_wire` | `BarbedWireBlock` < `Block` | |

**None of the modded webs extends `WebBlock`.** Not webs: `dustrial_decor:barbed_iron_bars`,
`barbed_chain_link_fence`; BOP `hanging_cobweb` (a strand).

## 4. Items and XP orbs (plan 4.2)

- Vanilla `ItemEntity#tick` (`m_8119_`): water/lava/`!isNoGravity()` -> `+(0,-0.04,0)`, then `move(SELF, motion)`
  (which calls `checkInsideBlocks`), then friction. Items already receive vanilla web slowdown through `move`.
  Stuck items still age: `age` (`f_31985_`) reaches Forge `lifespan` (6000) and despawns.
- **ItemPhysic 1.6.7** (`team.creative.itemphysic.mixin.ItemEntityMixin`, common side, both client and server):
  `@Redirect`s `ItemEntity#tick`'s `isInWater()`, `isInLava()` -> false and `isNoGravity()` -> **true**, and
  `@Inject`s `ItemPhysicServer.updatePre(item, random)` at the `isInWater()` call. **Gravity is applied in
  `ItemPhysicServer.updatePre`**: `if (fluid == null) { if (!item.isNoGravity()) motion += (0,-0.04,0) } else
  {fluid buoyancy}`. The vanilla `move` still runs afterwards. Hook point: `ItemPhysicServer.updatePre` (static,
  unobfuscated, `remap = false`; Compat's `ItemPhysicServerMixin` already injects at its TAIL), either HEAD-cancel
  when stuck, or simply rely on `isNoGravity()`.
- Simplest common route for both paths: while stuck, `setNoGravity(true)` + zero motion (vanilla `NoGravity` NBT
  persists), with an own saved flag so the web check can restore it. ItemPhysic reads the real `isNoGravity()`.
- `ExperienceOrb#tick` (`m_8119_`): `!isNoGravity()` -> `-0.03`; it then **pulls toward `followingPlayer`
  (`f_20771_`, `scanForEntities` `m_147103_`)** before `move`; a stuck orb needs that pull suppressed or allowed
  (design choice). `age` `f_20767_` despawns at 6000.

## 5. Projectiles (plan 5)

### 5.1 Movement and hook order

| Class | Tick order | Web slowdown via `checkInsideBlocks` TAIL hook |
|---|---|---|
| `AbstractArrow#tick` (`m_8119_`) | top: `inGround = true` only when the block at `blockPosition()` has a **collision** box containing the position; stuck branch: `if (lastState != blockstate && shouldFall()) startFalling(); else if (!client) tickDespawn();` else: raycast, `onHit`, x0.99 drag, gravity 0.05 (`!isNoGravity()`), `setPos`, then `checkInsideBlocks` | hook runs **after** `setPos`: changing motion affects the next tick. Not called while `inGround` |
| `ThrowableProjectile#tick` | raycast (`ProjectileUtil.getHitResult`, collider clip: webs never hit), `onHit`, **`checkInsideBlocks`**, then next pos = pos + motion, drag 0.99/0.8, gravity `getGravity()` (`m_7139_`), `setPos` | hook runs **before** this tick's step: slowdown applies at once |
| `AbstractHurtingProjectile#tick` | same as throwable, then motion = (motion + power) x `getInertia()` (`m_6884_`) | same; fireballs re-add `xPower/yPower/zPower` every tick |
| `FishingHook#tick` | gravity -0.03, **`move(SELF)`** (hook runs inside), x0.92 | bobbers already get vanilla web slowdown through `move` |
| `FireworkRocketEntity#tick` | `motion = motion x 1.15 + (0,0.04,0)`, `move(SELF, v)`, then **`setDeltaMovement(v)`** (restores motion), raycast `onHit` | **motion changes made in the hook are overwritten**: needs its own hook (after the `setDeltaMovement`) |
| Quark `AbstractPickarang#tick` | `super.tick()` (Entity), `checkImpact` (collider clip), `setPos(pos+motion)`; **never calls `checkInsideBlocks` or `move`** | **hook never runs**: needs a mixin on `AbstractPickarang#m_8119_` |
| Botania `ThornChakramEntity#tick` | saves motion, `super.tick()` (throwable), then **restores the saved motion** unless it bounced | slowdown applies to this tick's step only; motion restored after |

### 5.2 AbstractArrow (`net/minecraft/world/entity/projectile/AbstractArrow`)

| Member | SRG | Type / notes |
|---|---|---|
| `inGround` | `f_36703_` | boolean, saved as `inGround` |
| `lastState` | `f_36696_` | BlockState, saved as `inBlockState` |
| `life` | `f_36697_` | int, despawn counter |
| `pickup` | `f_36705_` | `AbstractArrow$Pickup` (DISALLOWED, ALLOWED, CREATIVE_ONLY) |
| `inGroundTime` | `f_36704_` | int |
| `shakeTime` | `f_36706_` | int |
| `tickDespawn` | `m_6901_` | `()V`: `life++; if (life >= 1200) discard();` (protected) |
| `getPickupItem` | `m_7941_` | `()ItemStack` |
| `onHitBlock` | `m_8060_` | `(BlockHitResult)V` |
| `shouldFall` / `startFalling` | `m_36798_` / `m_36799_` | `shouldFall = inGround && level.noCollision(AABB(pos).inflate(0.06))`: **true inside a web**, so a hanging arrow stays only because `lastState == blockstate` |
| `tryPickup` | `m_142470_` | `(Player)Z` |
| `isNoPhysics` | `m_36797_` | |
| `tick` | `m_8119_` | |

Hang = set `inGround = true`, `lastState = web state`, zero motion; breaking the web makes `lastState != state` and
`shouldFall()` true -> `startFalling()`. Both sides must set it (the client runs `tick` and `onHit` too).

### 5.3 Other vanilla projectiles

| Member | SRG |
|---|---|
| `ThrownTrident#tickDespawn` | `m_6901_`: `if (pickup != ALLOWED \|\| loyalty <= 0) super.tickDespawn();` |
| `ThrownTrident` fields | `dealtDamage` `f_37556_`, `ID_LOYALTY` `f_37558_`, `tridentItem` `f_37555_`, `clientSideReturnTridentTickCount` `f_37557_`; `getPickupItem` `m_7941_` |
| `ThrowableProjectile#tick`, `getGravity` | `m_8119_`, `m_7139_` |
| `Projectile#onHit`, `onHitBlock`, `getOwner` | `m_6532_`, `m_8060_`, `m_37282_` (protected `onHit`: needs an invoker) |
| `AbstractHurtingProjectile#tick`, `getInertia` | `m_8119_`, `m_6884_` |
| `FishingHook` | `currentState` `f_37095_` (enum `FishingHook$FishHookState`: `FLYING`, `HOOKED_IN_ENTITY`, `BOBBING`), `hookedIn` `f_37094_`, `life` `f_37103_` (counts only `onGround`, discard at 1200), `outOfWaterTime` `f_37100_`, `tick` `m_8119_`. FLYING -> stops when `onGround \|\| horizontalCollision` |
| `FireworkRocketEntity` | `life` `f_37022_`, `lifetime` `f_37023_` (`10 x flight + rand(6) + rand(7)`, saved `LifeTime`), `explode` `m_37080_`; server: `if (life > lifetime) explode()` (explode handles no-star rockets as a plain discard) |
| `ExperienceOrb#tick` | `m_8119_` |
| `ItemEntity#tick` | `m_8119_` |

Feather on a stick: Domestication Innovation `FeatherEntity` < `FishingHook` (FishingHook tick + extra x0.8). DI's
`FishingHookMixin` redirects inside `FishingHook#tick` and `shouldStopFishing` (no conflict seen). Stardew Fishing
also mixes into a bobber class.

### 5.4 Pickup items of the `hang` group (plan 5.1 / 5.3)

| Class | `getPickupItem` | Fix needed for a web hang |
|---|---|---|
| vanilla `Arrow`, `SpectralArrow`, `ThrownTrident` | tipped/plain, spectral, trident copy | no |
| Nock Enough Arrows, 25 classes (`com.itayfeder.nock_enough_arrows.arrows.*`) | `new ItemStack(ItemInit.<OWN>_ARROW)`; Paint keeps its colour | no |
| Alex's Mobs `EntitySharkToothArrow` (< `Arrow`) | own item | no |
| Dragon Loot `DragonTridentEntity` | `tridentStack.copy()` | no |
| Creative Delight Hearth `ThrownTool` (cleaver, pitchfork), `ThrownHorseshoe` | tool / shoe copy | no |
| Neapolitan `Bananarrow` | `impacted ? Items.ARROW : bananarrow` (`impacted` set on a hit) | no (a web hang never sets `impacted`) |
| Supplementaries `RopeArrowEntity` | `charges != 0 ? rope arrow (damage = max - charges) : Items.ARROW` | no (charges are spent only on block hit) |
| Quark `TorchArrow` | `extinguishOnMiss ? Items.ARROW : torch_arrow` | only if `Extinguish On Miss` becomes true (pack value **false**) |
| Create Misc and Things `NeonTubeEntity` | **`ItemStack.EMPTY`** | it can never be picked up: treat as unpickable (short life), not a "picked back up" item |

Despawn overrides: `DragonTridentEntity#tickDespawn` and Hearth `ThrownTool#tickDespawn` are the vanilla trident rule
(skip only when pickable **and loyalty > 0**). `ThrownHorseshoe` and every other class use the 1-minute rule.

### 5.5 Pickarang and chakram (snag)

- Quark `AbstractPickarang` (< `Projectile`): `liveTime` > type `timeout` -> `setReturning()` (synced `RETURNING`),
  then `noPhysics = true`, flies to the owner, collects items, gives itself to the original slot or inventory.
  **Drop path:** owner null, dead or not a `Player` -> (server) lift out of walls, `spawnAtLocation(stack, 0)`,
  `discard()`. Block hits use a `COLLIDER` clip, so webs are never hit (no harvest).
- Botania `ThornChakramEntity` (< `ThrowableProjectile`): after 16 bounces or `tickCount > 60` -> return to the
  thrower (`setEntityToReturnTo`), `dropAndKill()` (new `ItemEntity` at its position, discard) when the thrower is
  null or within distance^2 < 2.0. Gravity `m_7139_` overridden.
- A snag must stop both timers (pickarang `liveTime`/return, chakram `tickCount > 60`) and hold position; the
  pickup-by-touch and drop-on-web-break paths are new code (neither entity has a `playerTouch` pickup).

## 6. BlockRunner field of view (plan 1.3)

BlockRunner 4.2.2 (`fuzs.blockrunner`):
- Client mixin `AbstractClientPlayerMixin`: `@ModifyVariable(method = "getFieldOfViewModifier", at = @At(value =
  "STORE", ordinal = 2), ordinal = 0)` on the float `f` (the store after `f *= (speed / walkSpeed + 1) / 2`).
  Refmap: `Lnet/minecraft/client/player/AbstractClientPlayer;m_108565_()F`. Returns
  `FieldOfViewHelper.shouldRemoveBlockSpeedModifier(this) ? getFieldOfViewModifierWithoutBlockSpeed(this) : f`.
- `FieldOfViewHelper.shouldRemoveBlockSpeedModifier(Player)`: false unless client config `disableFieldOfViewChanges`;
  false when first person **and scoping**; else `attributes.hasModifier(MOVEMENT_SPEED, SPEED_MODIFIER_BLOCK_SPEED_UUID)`.
  So it recomputes **only while BlockRunner's own modifier is present**.
- `getFieldOfViewModifierWithoutBlockSpeed(Player)`: `(flying ? 1.1 : 1.0) x ((skipped speed / walkingSpeed + 1) / 2)`;
  `calculateAttributeValueSkipping(AttributeInstance, UUID...)` is `private static` varargs and rebuilds ADDITION,
  MULTIPLY_BASE, MULTIPLY_TOTAL from the remaining modifiers, then `attribute.sanitizeValue`.
- UUID: `BlockSpeedManager.SPEED_MODIFIER_BLOCK_SPEED_UUID` = `23237052-61AD-11EB-AE93-0242AC130002`, operation
  **ADDITION** (`factor x base - base`), the same one CamoSpeed and StickyFacing reuse.
- Hook point for a second UUID: a Compat client mixin on `AbstractClientPlayer#m_108565_` with the same
  `@ModifyVariable` STORE ordinal 2 (lower priority than BlockRunner's so it runs after), recomputing the value
  skipping both UUIDs whenever either modifier is present (BlockRunner's own option on or off). Alternative:
  `@ModifyArg` on the `calculateAttributeValueSkipping` call in `FieldOfViewHelper` plus a HEAD override of
  `shouldRemoveBlockSpeedModifier`; that covers only the option-on case.

## 7. Network channel (plan 1.4)

- Creative Compat: `compat/SackBundle.CHANNEL` = `creativecompat:sack_bundle`, protocol `"1"` with strict
  `PROTOCOL::equals` on both sides, message id 0 (`Rotate`). Reusable (id 1), but feature-named.
- Creative Common: `trader/TradeSync.CHANNEL` = `creativecommon:trade_sync`, `acceptMissingOr("1")`, id 0
  (`Lists`, PLAY_TO_CLIENT). Lives in another jar.
- Nothing else in `creativeCompat`, `creativeCommon` or `creativeDevtools`. Recommendation: a new
  `creativecompat:surface` channel in `surface/` (or id 1 on `sack_bundle` if a second channel is unwanted).

## 8. Tag plumbing (plan 1 / phase 4)

- File: `kubejs/server_scripts/tags/blocktags.js`, section "BlockRunner walk speed" (line ~62): `var runner = {tier:
  'blockrunner:surface/<tier>'}`; fills with `t.addMatching(e, runner.<tier>, [ids, '#tag', '@mod', '/regex/',
  'mod:*glob'], opts)`; 1.00 tiers use `auditLeft(tier)` (`{ except: [other tiers, #minecraft:dirt,
  #minecraft:flowers, #minecraft:crops, ...] }`); end: `t.remove(e, runnerTags, '/_(button|pressure_plate)$/')`
  then `t.overlaps(e, runnerTags.concat(basicTiers), 'BlockRunner tier')` (line ~611). Helper: Creative Common
  `kubejs_startup/creativecommon/func/tag_helper.js` (`addMatching` line 39, `overlaps` line 234).
- The section's comment still says the multipliers live in `creativeengineering-common.toml [surface.speed]`; they
  are in `config/creativecompat-common.toml` `[surface]` (`CompatConfig`, pushed at line ~469) since 2026-11-06.
- Coffee stems are already in `blockrunner:surface/flora` (explicit `/^farmersrespite:coffee_stem[a-z_]*$/`); surface
  tiers and pass-through tiers are separate sets, so this is not an overlap.
- Compat's own tags already use `creativecompat:` (`creativecompat:sinking`, `creativecompat:wall_slide`).

## Implications for phases 1-5 (contradictions and changes to the plan)

1. **Trident despawn premise is false.** Vanilla `ThrownTrident#tickDespawn` skips despawn only when `pickup ==
   ALLOWED` **and loyalty > 0**. A player's non-loyalty trident despawns after 1 minute today. Dragon Loot's trident
   and Hearth's cleaver/pitchfork copy that rule exactly. As written (5.4 "tridents exempt"), non-loyalty tridents
   and thrown tools would keep the 1-minute timer while plain arrows get 10. Test "A thrown trident never despawns"
   holds only with Loyalty. **Owner:** should non-loyalty tridents and thrown tools get the 10-minute rule (and count
   toward the 128)? Suggested: exempt only what already never despawns (loyalty + pickable); everything else
   pickable follows 5.4.
2. **Modded webs are not `WebBlock` subclasses.** Tier membership must be by id: `minecraft:cobweb`,
   `securitycraft:reinforced_cobweb`, `fantasyfurniture:decorations/spider_web_small`, `.../spider_web_wide`,
   `createaddition:barbed_wire`. Any "WebBlock and subclasses" default in code finds only vanilla.
3. **Spider exemption widens:** vanilla exempts spiders only from `minecraft:cobweb`; a tier-wide exempt tag frees
   them in reinforced webs, Fantasy webs and barbed wire too (barbed wire damage still applies). Fine if intended.
4. **Reinforced cobweb owner pass-through** lives in its `entityInside`; once `makeStuckInBlock` is cancelled the
   cobweb tier slows the owner. Phase 1/2 must keep it (owner check via `OwnableBlockEntity#isOwnedBy`, or a
   per-block hook).
5. **Vintage oats are one block** (`OatBlock` < `CropBlock`, age 0-7): rule `half`, not `tall_half`.
6. **Tomatoes have no lower/upper pair**; segment age is ripeness and resets to 0 on harvest. Proposed rule `any` for
   `tomatoes`, `rope_tomatoes`, `stick_tomatoes`, `treated_stick_tomatoes`; `half` for `budding_tomatoes`. Stick
   and treated-stick tomatoes have a collision shape (thin stick), so the "no collision box" premise does not hold
   for them (the cell/outline test still works).
7. **Coffee is `any`, not `tall_half`:** it is 2+ blocks from planting and its stems' `age` is berry ripeness.
   Members: `coffee_bush`, `coffee_bush_top`, `coffee_stem`, `coffee_stem_middle`, `coffee_stem_double`.
8. **Hemp growth is an enum** (`growth`: `bottom0..bottom4`, `top0`); the `half` code needs an enum-ordinal path,
   and `top0` must look down (`tall_half`). Corn's top has its own `age` from 0: must look down.
9. **Supplementaries flax** is an unlisted 2-tall crop (`half` works on it as is). Decide `crop` or `dense_crop`.
10. **No Supplementaries propelplant.** Nether's Delight has four `PropelplantBlock` ids (stem, berry_stem, cane,
    berry_cane); walking in without sneaking explodes and removes the plant, and any projectile does too. The cane
    tier value is felt only while sneaking. Plan text "propelplant cane (Nether's Delight, Supplementaries)" needs
    fixing.
11. **Beautify "twine" is `beautify:rope`**, a `ChainBlock` with chain collision. **Quark rope (`quark:rope`) is a
    fourth rope** (enabled, no collision, always a ladder): add it to the rope tier or rule it out.
12. **Supplementaries rope** collides for living entities except on vertical runs; it is a ladder only with `down`
    connected (a hanging rope's bottom block is not climbable); vanilla arrows **break it** on entry (server).
13. **Vinery taiga grape bush does not slow today** (no `entityInside` in the installed port; `StuckInBlock` entry
    stale). Putting it in `bush` (0.36) adds a new slowdown, not "today's real speed". The other grape bushes and the
    Hearth raspberry bush (0.8 stuck, berry-bush twin) are unlisted; raspberry arguably belongs in `bush`.
14. **Grape vines:** only `vinery:jungle_grape_bush_red/white` are vines (no collision, climbable, vine-face
    outline). `grapevine_stem` and `grapevine_lattice` are solid blocks: out of every tier.
15. **Vine list for phase 4** is section 2.6 (17 Floral vines, Quark cut vine, Botania solid vine, BOP spanish moss,
    glowworm silk, flesh tendons, IW ivy, Neapolitan vanilla vine, UA mulberry vine, hanging roots). `magic_vine`
    is not a vine. **BOP `hanging_cobweb`**: vine tier or cobweb tier (owner)?
16. **Twigs bamboo leaves** slow from 3 layers today (`layers > 2`) with their own immune tag
    `twigs:bamboo_leaves_slow_immune`; `leaf_pile` `minLayers = 4` and fox/bee exemption change both. Keep a
    per-block layer threshold or accept the change.
17. **IW `leaf_piles_slow` still matters:** spruce thorn damage sits inside the same `if` as the slowdown, so
    turning that IW option off also stops thorns. Removing `LeafPileSlowMixin` returns IW's own gate to `layers > 3`,
    so thorns gate on 4+ regardless of `minLayers`. Correct the creative_compat.md note planned in 3.1.
18. **Hook coverage gaps:** `FireworkRocketEntity` restores its motion after `move`, Quark pickarangs never call
    `checkInsideBlocks`, and Botania's chakram restores its motion after `super.tick()`. Each needs its own injector
    (firework: after the `setDeltaMovement` following `move`; pickarang: `AbstractPickarang#m_8119_`; chakram:
    TAIL of its `m_8119_`). For arrows the hook runs after `setPos`, so slowdown lands one tick later (fine).
19. **Items, orbs, bobbers and fireworks use `Entity#move`**, so they already get vanilla web slowdown today
    (plan's "projectiles ignore cobwebs" is right for arrows and throwables only). Cancelling `makeStuckInBlock` for
    tiered blocks removes it from them too, so PassThrough must apply its own values to them.
20. **Item stick hook:** vanilla gravity is in `ItemEntity#tick` behind `isNoGravity()`; ItemPhysic forces that call
    to true and applies gravity in `ItemPhysicServer.updatePre` behind the real `isNoGravity()`. Using
    `setNoGravity(true)` + zero motion while stuck covers both without a new ItemPhysic mixin. Stuck items and orbs
    still despawn at age 6000 (5 minutes); orbs still home on players unless suppressed.
21. **Neon tube** (`NeonTubeEntity`) returns `ItemStack.EMPTY`: it is never picked up; treat as unpickable (1-minute
    rule) in `hang`.
22. **FOV:** BlockRunner recomputes only while its own UUID is present and its client option is on; a pass-through
    modifier alone would show in the FOV. Phase 1 needs its own `m_108565_` STORE-ordinal-2 modifier (section 6).
23. **Unlisted plants with their own `makeStuckInBlock`** (Floral `*_bush` 0.9, FR wither roots 0.8, Neapolitan
    beanstalk thorns 0.95, UA pickerelweed 0.6/0.75): untiered they keep vanilla stuck behaviour; `StuckInBlock`
    lacks them for WTHIT. Decide per block in phase 4 (owner).
24. **Tag namespace:** the plan's `blockrunner:passthrough/<tier>` follows the legacy `blockrunner:surface/*`
    pattern, but the ids-follow-owning-mod rule (2026-11-09) and Compat's existing `creativecompat:*` block tags
    suggest `creativecompat:passthrough/<tier>`. **Owner** to pick before phase 4.
25. **`minecraft:crops` is incomplete** (35 entries); the `crop` tier must be filled from explicit ids / the crop audit,
    and wild crops from `farmersdelight:wild_crops` plus the 8 untagged wild plants (2.4).

## Open questions for the owner

- Trident and thrown-tool despawn (implication 1).
- Tag namespace `blockrunner:` vs `creativecompat:` (24).
- Quark rope in the rope tier (11); flax `crop` or `dense_crop` (9); taiga grape bush and raspberry bush in `bush`,
  other grape bushes (13); BOP hanging cobweb (15); Twigs bamboo leaves threshold (16); unlisted stuck plants (23).
- Tomato rule `any` and coffee rule `any` (6, 7) replace the plan's `tall_half` for those blocks.
