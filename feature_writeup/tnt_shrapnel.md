# TNT shrapnel: exploding TNT throws a ring of damaging fragments

## 0. At a glance

<!-- generated:status_index begin open_work sha1=40e9eb197e82 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 3 open TODO rows (Bug 1, Planned 1, Testing 1), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `gameplay/tnt-and-portal #3` | Bug | S P T | Minecart shrapnel and damage |
| `gameplay/tnt-and-portal #2` | Planned | S P | Lighter immune to own shrapnel |
| `gameplay/tnt-and-portal #1` | Testing | S P T | TNT shrapnel |
<!-- generated:status_index end open_work -->

| Part | Status |
|---|---|
| Lit TNT and TNT minecarts fire a burst of shrapnel when they explode (`TntShrapnelHandler`) | DEPLOYED (in the Creative Common jar shipped with 1.0.3; first committed 2026-10-02); in-game test pending |
| Burning TNT items (item physics) fire the same burst (`ItemBurnTransform`, `shrapnel = true`) | DEPLOYED |
| `creativecompat:shrapnel` entity: no gravity, flat damage, despawns after a set distance | DEPLOYED |
| `[shrapnel]` config: on/off, count, speed, spread, range, damage, item shown | DEPLOYED, live |
| Living targets only (no carts, boats, frames, armor stands), chain cap of 32 pieces in flight nearby, burning TNT item blast at half damage (B1) | Built 2026-11-18 (plan_bugfix_1117 B1); SQ-TN-11, -12, CQ-TN-13 |

- **Pending:** B1 (2026-11-18) is built, its queue rows not run; TN-01 to TN-10 have never been run. F2 and F3 are
  settled (section 4); F1 is TODO `gameplay/tnt-and-portal #2`.
- **Files to open:** [shrapnel/](../development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/shrapnel/)
  (`ShrapnelSpawner`, `ShrapnelEntity`, `TntShrapnelHandler`), `CompatConfig.Shrapnel`, `itemphysic/BurnBlastDamage`.
- **Knobs:** `config/creativecompat-common.toml` `[shrapnel]`: `maxNearby` 32 (chain cap, 0 = none), `hitsNonLiving`
  false, `itemBlastDamageFactor` 0.5, `enabled` true, `count` 24 (0-512), `speed` 1.4
  blocks/tick, `speedVariance` 0.25, `verticalAngle` 15°, `maxDistance` 12 blocks, `damage` 3, `item`
  `minecraft:iron_nugget` (what the flying piece looks like). Read at each burst; no restart.

## 1. Overview

A vanilla TNT explosion damages by distance and breaks blocks; nothing flies out of it. The pack adds a burst of
shrapnel on top, so TNT is dangerous at range in a ring around it, without changing what the explosion itself breaks
or how much it hurts. Supplementaries' spiky bomb has its own shrapnel; this is separate.

## 2. Reference

### 2.1 What triggers a burst

| Source | Burst | Owner (immune to its own shrapnel) |
|---|---|---|
| Primed TNT (lit block TNT) exploding | yes | whoever lit it (`Explosion#getSourceMob`), else none |
| TNT minecart exploding | yes | same |
| A dropped TNT or TNT minecart item destroyed by fire or lava | yes, with a power-2 no-block-damage explosion ([item_physics.md](item_physics.md) 2.3) | none |
| Creepers, beds, respawn anchors, end crystals, modded explosives | no | |

### 2.2 The burst

- `count` pieces from the explosion center, evenly spaced around the full circle with a little random jitter (a ring
  even at low counts), each at a random pitch within ±`verticalAngle` and a speed of `speed` ± `speedVariance`.
- Each piece flies straight (no gravity) until it has gone `maxDistance` blocks, hits a block (it just stops: no block
  damage, no sticking), or hits a living thing: `damage` as a thrown-projectile hit (armor applies; a raised shield
  blocks it as a projectile, [shields.md](shields.md)), then it's gone.
- It never hits its owner, item entities or spectators, and with `hitsNonLiving` false (default) nothing that is not
  alive: minecarts, boats, item frames and armor stands are passed through. Before this a burst broke nearby carts
  and turned TNT minecarts into items instead of letting them chain (the minecart half of `gameplay/tnt-and-portal #3`).
- A burst fires at most `maxNearby` minus the pieces already flying within 32 blocks, so a chain of TNT keeps about 32
  pieces in the air at once (F2). Pieces aren't saved: a restart or chunk unload removes them.
- Rendered as the `item` (an iron nugget) like a thrown item.

With the defaults: 24 pieces, about 12 blocks, 3 damage each; a mob standing close in the ring can take several.

## 3. What the pack changes

- **Entity:** `ShrapnelEntity extends ThrowableItemProjectile`, registered by `CreativeEngineeringEntities` (0.2 × 0.2,
  tracking range 32, no save, fire immune); renderer registered client-side in `CreativeEngineering.java`.
- **Spawner:** `ShrapnelSpawner.burst(level, centre, owner)`, server only, reads `CEConfig.SHRAPNEL` each call. The one
  entry point for both triggers.
- **TNT trigger:** `TntShrapnelHandler` listens to Forge's `ExplosionEvent.Detonate` (after the explosion's damage is
  worked out) and fires only when the explosion's direct exploder is a `PrimedTnt` or `MinecartTNT`.
- **Item trigger:** `global.itemPhysics.burnExplode('minecraft:tnt', 2.0, 1.0, true)` (and the TNT minecart) in
  [func/item_physics.js](../kubejs/startup_scripts/func/item_physics.js); the last argument opts into shrapnel.

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1. The player who lit the TNT is immune to its shrapnel. Your call.** The owner is excluded from hits. TNT lit by
  redstone, a dispenser, fire or another explosion has no owner and hits everyone. Keep (no self-damage for the
  lighter), or include the owner. Owner 2026-10-29: "keep (shrapnel hurts the igniter)"; the two halves disagree
  (the code excludes the owner, `ShrapnelEntity#canHitEntity`), so confirm which is meant (TODO `gameplay/tnt-and-portal #2`).
- **F2. Chain reactions multiply entities.** Every TNT in a chain fired its own 24 pieces: 50 TNT was 1,200
  projectiles for about half a second. Settled (owner, 2026-11-17): cap 32 pieces in flight near a burst
  (`maxNearby`), built 2026-11-18.
- **F3. Only vanilla TNT.** Modded explosives (IE, Create, PneumaticCraft) don't burst. Settled (owner, 2026-11-17):
  only vanilla TNT and TNT minecarts burst; no change.
- **F5. Burning TNT item damage.** The item-physics blast of a TNT item in fire or lava hurts at
  `itemBlastDamageFactor` (owner, 2026-11-17: 50%) through a `LivingHurtEvent` on `BurnBlastDamage`; the knockback is
  applied apart from the damage in `Explosion#explode`, so it is unchanged. Built 2026-11-18.
- **F4. Code committed late.** The shrapnel package was in the shipped jar but entered git only with the 2026-10-02
  commit; the item-physics script referenced it from the initial commit.

### Known limits

- **No block damage** from shrapnel, by design.
- **Shrapnel damage is flat:** no falloff with distance.

### Rejected options

- **More explosion damage instead:** the point is reach, not more damage in the blast radius.

## 5. File map

- Creative Common [shrapnel/](../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/shrapnel/): `CreativeEngineeringEntities` (registration), `ShrapnelEntity`, `ShrapnelSpawner`, `TntShrapnelHandler`; `CEConfig.Shrapnel`; listener and renderer registration in `CreativeEngineering.java`.
- Item trigger: `itemphysic/ItemBurnTransform`, [func/item_physics.js](../kubejs/startup_scripts/func/item_physics.js).

## 6. Offline checks

None.

## 7. In-game test suite

Use a flat test area, armor off, a few zombies or armor stands around.

- [ ] **TN-01 Burst:** light TNT with flint and steel -> a ring of iron-nugget fragments flies out roughly level and
  stops at about 12 blocks.
- [ ] **TN-02 Damage:** zombies 6-10 blocks away (outside the blast) take about 3 damage per hit.
- [ ] **TN-03 Owner immune (F1):** stand 8 blocks away from TNT you lit -> no shrapnel damage; stand at the same spot when
  a redstone-lit TNT goes off -> you're hit.
- [ ] **TN-04 Shield:** raise an iron shield facing the TNT -> shrapnel blocked (shield sound), 60 % of each hit gets through (iron: physical 40 %).
- [ ] **TN-05 Minecart:** a TNT minecart over an activator rail -> burst.
- [ ] **TN-06 Burning TNT item:** throw a TNT item into fire -> a small explosion, no block damage, and a burst.
- [ ] **TN-07 Not creepers:** a creeper explodes -> no shrapnel.
- [ ] **TN-08 Config:** `count = 64`, `maxDistance = 30`, `item = "minecraft:flint"` -> a denser, longer burst of flint;
  `enabled = false` -> none. Put them back.
- [ ] **TN-09 Blocks:** shrapnel hitting glass and leaves -> nothing broken.
- [ ] **TN-10 Chain (F2):** a 5 × 5 × 2 block of TNT -> note any lag spike.
- [ ] **TN-11 Carts and stands (queue SQ-TN-11):** a TNT minecart burst leaves nearby minecarts and armor stands whole;
  zombies are hit.
- [ ] **TN-12 Chain cap (queue SQ-TN-12):** five TNT in one tick -> at most 32 pieces in flight.
- [ ] **TN-13 Item blast (queue CQ-TN-13):** a TNT item in a campfire hurts about half as much, same push.

## 8. History

- Before 2026-09-27: shrapnel built with the item-physics burn transforms; shipped in the 1.0.3 jar.
- 2026-10-02: shrapnel package first committed.
- 2026-10-18: this writeup; findings F1 to F4; tests TN-01 to TN-10. No code changed.
- 2026-11-18: B1 of plan_bugfix_1117 (TODO `gameplay/tnt-and-portal #3`): shrapnel hits living things only, chain cap `maxNearby` 32, burning TNT item blast at `itemBlastDamageFactor` 0.5 with knockback kept; F2, F3 settled; TN-11 to TN-13, queue rows in `testing/rows/TN.qb`.
