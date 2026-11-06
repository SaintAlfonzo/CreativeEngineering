# TNT shrapnel: exploding TNT throws a ring of damaging fragments

## 0. At a glance

| Part | Status |
|---|---|
| Lit TNT and TNT minecarts fire a burst of shrapnel when they explode (`TntShrapnelHandler`) | DEPLOYED (in the patcherMod jar shipped with 1.0.3; first committed 2026-10-02); in-game test pending |
| Burning TNT items (item physics) fire the same burst (`ItemBurnTransform`, `shrapnel = true`) | DEPLOYED |
| `creativeengineering:shrapnel` entity: no gravity, flat damage, despawns after a set distance | DEPLOYED |
| `[shrapnel]` config: on/off, count, speed, spread, range, damage, item shown | DEPLOYED, live |

- **Pending:** nothing built and unshipped. TN-01 to TN-10 have never been run. Findings F1 to F4 (section 4); F1
  needs your call.
- **Files to open:** [shrapnel/](../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/shrapnel/)
  (`ShrapnelSpawner`, `ShrapnelEntity`, `TntShrapnelHandler`), `CEConfig.Shrapnel`.
- **Knobs:** `config/creativeengineering-common.toml` `[shrapnel]`: `enabled` true, `count` 24 (0-512), `speed` 1.4
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

- `count` pieces from the explosion centre, evenly spaced around the full circle with a little random jitter (a ring
  even at low counts), each at a random pitch within ±`verticalAngle` and a speed of `speed` ± `speedVariance`.
- Each piece flies straight (no gravity) until it has gone `maxDistance` blocks, hits a block (it just stops: no block
  damage, no sticking), or hits a living thing: `damage` as a thrown-projectile hit (armor applies; a raised shield
  blocks it as a projectile, [shields.md](shields.md)), then it's gone.
- It never hits its owner, item entities or spectators. Pieces aren't saved: a restart or chunk unload removes them.
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
- **F2. Chain reactions multiply entities.** Every TNT in a chain fires its own 24 pieces: 50 TNT is 1,200 projectiles
  for about half a second. Not measured; lower `count` if a TNT crater lags the server.
- **F3. Only vanilla TNT.** Modded explosives (IE, Create, PneumaticCraft) don't burst; adding one is a line in
  `TntShrapnelHandler`.
- **F4. Code committed late.** The shrapnel package was in the shipped jar but entered git only with the 2026-10-02
  commit; the item-physics script referenced it from the initial commit.

### Known limits

- **No block damage** from shrapnel, by design.
- **Shrapnel damage is flat:** no falloff with distance.

### Rejected options

- **More explosion damage instead:** the point is reach, not more damage in the blast radius.

## 5. File map

- patcherMod [shrapnel/](../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/shrapnel/): `CreativeEngineeringEntities` (registration), `ShrapnelEntity`, `ShrapnelSpawner`, `TntShrapnelHandler`; `CEConfig.Shrapnel`; listener and renderer registration in `CreativeEngineering.java`.
- Item trigger: `itemphysic/ItemBurnTransform`, [func/item_physics.js](../kubejs/startup_scripts/func/item_physics.js).

## 6. Offline checks

None.

## 7. In-game test suite

Use a flat test area, armor off, a few zombies or armor stands around.

- [ ] **TN-01 Burst:** light TNT with flint and steel → a ring of iron-nugget fragments flies out roughly level and
  stops at about 12 blocks.
- [ ] **TN-02 Damage:** zombies 6-10 blocks away (outside the blast) take about 3 damage per hit.
- [ ] **TN-03 Owner immune (F1):** stand 8 blocks away from TNT you lit → no shrapnel damage; stand at the same spot when
  a redstone-lit TNT goes off → you're hit.
- [ ] **TN-04 Shield:** raise an iron shield facing the TNT → shrapnel blocked (shield sound), 60 % of each hit gets through (iron: physical 40 %).
- [ ] **TN-05 Minecart:** a TNT minecart over an activator rail → burst.
- [ ] **TN-06 Burning TNT item:** throw a TNT item into fire → a small explosion, no block damage, and a burst.
- [ ] **TN-07 Not creepers:** a creeper explodes → no shrapnel.
- [ ] **TN-08 Config:** `count = 64`, `maxDistance = 30`, `item = "minecraft:flint"` → a denser, longer burst of flint;
  `enabled = false` → none. Put them back.
- [ ] **TN-09 Blocks:** shrapnel hitting glass and leaves → nothing broken.
- [ ] **TN-10 Chain (F2):** a 5 × 5 × 2 block of TNT → note any lag spike.

## 8. History

- Before 2026-09-27: shrapnel built with the item-physics burn transforms; shipped in the 1.0.3 jar.
- 2026-10-02: shrapnel package first committed.
- 2026-10-18: this writeup; findings F1 to F4; tests TN-01 to TN-10. No code changed.
