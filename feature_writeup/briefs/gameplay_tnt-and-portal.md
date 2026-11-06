# gameplay / tnt-and-portal: implementation brief

Written 2026-10-31 from [TODO](../todo/TODO.md) `# gameplay`, Sub `tnt-and-portal` (rows 2, 3, 5, 6 open; 1 and 4 Testing;
7 Complete). Layout per [README.md](README.md). Feature writeups: [tnt_shrapnel.md](../tnt_shrapnel.md) (test prefix
`TN-`, TN-01 to TN-10, none run) and [nether_portal.md](../nether_portal.md) (test prefix `NP-`, NP-01 to NP-12, none
run). New test ids are placeholders (`TN-newN`, `NP-newN`), numbered when queued. Source lines: scratchpad 2026-10-19
(git `2e8687f`, `change_scratchpad.md` 80-82: "tnt / fix minecart shrapnel / set item damage to fraction but keep
knockback") and the 2026-10-29 decisions in the writeups' findings.

## Scope and order

| Row | Change | Status | Size |
|---|---|---|---|
| #3 | Minecart shrapnel and damage | Bug | small to medium: repro first, then `ShrapnelEntity` / `TntShrapnelHandler` changes and 2-4 config keys |
| #2 | Lighter immune to own shrapnel | Planned | tiny: one line in `ShrapnelEntity#canHitEntity` (+ a config key), once the decision is confirmed |
| #5 | Flint and Terrasteel is still temporary | Planned | medium: item placement rule, Dark Fire survival, burning, ash, textures |
| #6 | Dark Fire lights portals in any dimension | Planned | tiny: one dimension check in `DarkFireBlock#onPlace` |
| #1, #4 | TNT shrapnel, Nether portal ignition | Testing | see "Built, needs play" |

All four open rows are patcherMod: build #2 and #3 together (one shrapnel pass, then TN-01 to TN-10 plus the new
tests), and #5 and #6 together (one Dark Fire pass, then NP-01 to NP-12; #4's diagnostic log can go in the same build
once NP-01 to NP-05 pass).

---

## tnt-and-portal #3: Minecart shrapnel and damage
Status **Bug** / Work patcherMod / TODO `gameplay/tnt-and-portal #3`

### Goal
TNT minecarts burst correctly; one damage value is a fraction of today's while knockback stays; a chain of TNT does
not flood the server with projectiles (F2); explosives other than vanilla TNT can burst (F3).

### Now
- [shrapnel/TntShrapnelHandler.java](../../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/shrapnel/TntShrapnelHandler.java)
  27-36: on `ExplosionEvent.Detonate`, server only, bursts when `explosion.getExploder()` is a `PrimedTnt` or
  `MinecartTNT`, at `explosion.getPosition()`, owner `getSourceMob()`.
- [shrapnel/ShrapnelEntity.java](../../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/shrapnel/ShrapnelEntity.java)
  (119 lines, `ThrowableItemProjectile`): `onHitEntity` 75-84 does `hurt(DamageSource.thrown(this, owner), damage)` on
  **any** entity the projectile can hit, then discards; `canHitEntity` 96-98 = vanilla rule minus the owner. Vanilla's
  rule includes minecarts, boats, armor stands and other pickable non-living entities.
- `ShrapnelSpawner.burst` (58 lines) reads `CEConfig.SHRAPNEL` each call: `count` 24, `speed` 1.4, `speedVariance`
  0.25, `verticalAngle` 15, `maxDistance` 12, `damage` 3, `item` iron nugget (CEConfig.java 582-602 and on).
- Item trigger: burning TNT and TNT minecart items explode at power 2 with shrapnel
  ([func/item_physics.js](../../kubejs/startup_scripts/func/item_physics.js) 150-151), through
  `itemphysic/ItemBurnTransform` 180-183 with `BurnBlastDamage` (explosion damage that hurts on Peaceful too).
- What is broken with minecart shrapnel is not written down anywhere; TN-05 (minecart burst) has never been run.

### Build
1. **Repro (scratch server or single player)**: TN-05 plus a line of 5 TNT minecarts, and a TNT minecart next to a
   plain minecart and an armor stand. Hypotheses, in order:
   a. shrapnel hits non-living entities: it breaks or damages nearby minecarts and boats, and hits other TNT carts,
      which then explode or drop (a chain the burst itself causes);
   b. the burst sits at the cart's base (`getPosition()` of a cart explosion is at rail height), so the downward half of
      the ring hits the rails and the floor at once and only a few pieces fly;
   c. the cart's explosion fires both the minecart burst and, if its item form burns later, a second one.
2. Fixes by finding: (a) `canHitEntity` only `LivingEntity` targets that are not armor stands (config
   `[shrapnel] hitsNonLiving` false); (b) raise the burst origin by 0.5 for minecarts in `TntShrapnelHandler`, or clamp
   pitch to >= 0 near the ground; (c) nothing if not seen.
3. **Damage fraction, knockback kept.** Two readings of "set item damage to fraction but keep knockback" (Decisions).
   For the burning-item explosion: a `LivingHurtEvent` handler that multiplies damage from `BurnBlastDamage.INSTANCE`
   by `[shrapnel] itemBlastDamageFactor` (explosion knockback is applied separately in `Explosion#explode`, so it
   stays). For shrapnel hits: multiply in `onHitEntity` and add `target.knockback(...)` from the flight direction, since
   a smaller hit still knocks back through `hurt`.
4. **Chain cap (F2).** In `ShrapnelSpawner`: a per-level, per-tick budget (`[shrapnel] maxPerTick`, e.g. 96); a burst in a
   tick that already spawned more gets `count` scaled down (never below 4). Reset in a `LevelTickEvent` END handler.
5. **Other explosives (F3).** `[shrapnel] sources` string list of entity type ids checked against
   `explosion.getExploder().getType()`, default `minecraft:tnt`, `minecraft:tnt_minecart`; modded explosive entities
   added after a dump shows their ids (none verified: IE, Create and PneumaticCraft explosives were not checked).
6. Update tnt_shrapnel.md 2.1 / 2.2 / 4 (F2, F3 resolved), and TN-03 / TN-10 text.

### Decisions
- **Which damage the fraction applies to (TODO decision "damage fraction").** Recommendation: the burning TNT item's
  explosion (the line says "item damage"), factor 0.25: a TNT item in a campfire then shoves players and mobs but rarely
  kills. Confirm with the user; if the shrapnel was meant, use the same factor on `damage` with an explicit knockback.
- **Count (TODO decision "count").** Recommendation: keep 24 per burst and cap per tick at 96 (four TNT's worth);
  chains still look dense.
- **Non-living targets.** Recommendation: skip them (no shrapnel-broken carts, frames, boats).

### Gotchas
- `ExplosionEvent.Detonate` fires after the affected entity list is built: spawning shrapnel there is fine, but any
  damage change must not touch the explosion's own list.
- Config is read at each burst: new keys appear in the toml only after the new jar runs once.

### Tests (TN-)
- TN-05 and TN-10 cover the repro. **TN-new1**: a TNT minecart next to a plain minecart and an armor stand -> the
  plain cart and the stand are not broken by shrapnel; zombies around take hits. **TN-new2**: a TNT item thrown into a
  campfire next to you -> knocked back about as before, about a quarter of the damage. **TN-new3**: 50 TNT chain ->
  no more than `maxPerTick` shrapnel entities alive per tick (count with `/kill @e[type=creativeengineering:shrapnel]`
  output right after).

---

## tnt-and-portal #2: Lighter immune to own shrapnel
Status **Planned** / Work patcherMod / TODO `gameplay/tnt-and-portal #2`

### Goal
Settle whether the player who lit the TNT is hit by its shrapnel, and make the code match.

### Now
- `ShrapnelEntity#canHitEntity` (96-98) excludes `getOwner()`; the owner is `Explosion#getSourceMob()`, the player who
  lit the fuse (TntShrapnelHandler 35). TNT lit by redstone, a dispenser, fire or another explosion has no owner.
- tnt_shrapnel.md F1, owner 2026-10-29: "keep (shrapnel hurts the igniter)": "keep" reads as "keep the code", the
  parenthesis as the opposite. TN-03 tests the immunity.

### Build
1. Confirm the meaning with the user.
2. If the igniter should be hit: `canHitEntity` drops the owner test behind `[shrapnel] hitsOwner` (default true); the
   owner stays the damage source's indirect entity (kill credit, death message).
3. Rewrite TN-03 and tnt_shrapnel.md 2.1's Owner column and F1.

### Decisions
- **Recommendation: the igniter is hit** (the parenthesis states the effect the owner wants; it also stops a player
  using TNT as a safe ranged weapon). Keep the config key so it can be flipped without a build.

### Gotchas
- Pieces spawn at the explosion centre, not at the owner, so there is no point-blank self-hit at spawn.

### Tests (TN-)
- **TN-new4** (replaces TN-03): stand 8 blocks from TNT you lit -> you are hit; `hitsOwner = false` -> you are not.

---

## tnt-and-portal #5: Flint and Terrasteel is still temporary
Status **Planned** / Work patcherMod (+ textures) / TODO `gameplay/tnt-and-portal #5`

### Goal
Flint and Terrasteel places Dark Fire only on solid blocks, and Dark Fire becomes its own fire: a faster animation
running the other way, no spreading, very fast burning of wood blocks with a high ash chance, sustained by every fire
and soul-fire sustain block (more magic sustain blocks to come).

### Now
- [item/FlintAndTerraSteelItem.java](../../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/item/FlintAndTerraSteelItem.java)
  (56 lines): `useOn` 32-55 places Dark Fire in the space in front of the clicked face if it is air, any face, any
  block; javadoc line 17 "TEMPORARY testing behavior: places TerraFireBlock on any air block" (that class no longer
  exists). Recipe flint + terrasteel ingot ([progression.js](../../kubejs/server_scripts/magic/progression.js) 8,
  `i.flint_and_terrasteel` item_dict.js 752).
- [block/DarkFireBlock.java](../../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/block/DarkFireBlock.java)
  (115 lines, `BaseFireBlock`, 1.0 fire damage): `canBurn` false (24); `canSurvive` 29-43 next to any sturdy face;
  `onPlace` 56-87 finds a portal shape and fires `PortalSpawnEvent` (no `super`); `updateShape` 92-114 removes it when
  it can't survive. `BaseFireBlock` has no random tick and never spreads (that is `FireBlock`), so "no spreading" is
  already true. In `#minecraft:fire` (blocktags.js 19, `b.dark_fire` block_dict.js 9).
- Textures: the KubeJS copies win (`kubejs/assets/creativeengineering/textures/block/dark_fire_0.png` / `_1.png`,
  mcmeta `frametime 2`); the patcherMod copies under the same names are different images (nether_portal.md 5).
- Ash: `i.ash = 'supplementaries:ash'` (item_dict.js 1292, Supplementaries' ash layer block), `i.soot` IW soot (147).
- Sustain tags in 1.19.2: `#minecraft:infiniburn_overworld` (magma, netherrack, Quark blaze lantern and charcoal block,
  SecurityCraft netherrack, live dump), `_nether`, `_end`, and `#minecraft:soul_fire_base_blocks`.

### Build
1. **Item**: `useOn` places only when the clicked face is sturdy (`isFaceSturdy(level, clickedPos, face)`) and the
   space in front is air; else `FAIL` with no durability use. Fix the javadoc (no `TerraFireBlock`, no "TEMPORARY").
2. **Sustain tag**: `#creativeengineering:dark_fire_base` (block tag via `global.tags` in blocktags.js) nesting the three
   infiniburn tags and `#minecraft:soul_fire_base_blocks`, plus magic blocks the user names later. Dict key
   `b.t_dark_fire_base`.
3. **Dark Fire life**: give it an `AGE` property (0-15, like fire) and a scheduled tick (`tick`, 30-40 ticks like
   `FireBlock`): on a `dark_fire_base` block below it never ages; elsewhere it ages and goes out at 15. Keep the
   "any sturdy face" survival so a frame can be lit from the side (portal lighting runs in `onPlace` first).
4. **Burning wood**: in the same tick, for each neighbour in `#minecraft:logs_that_burn` / `#minecraft:planks` (or a pack
   tag `#creativeengineering:dark_fire_burns`), with a high chance remove it and place Supplementaries ash (`i.ash`
   layer) with chance ~0.7, else air. Does not create new Dark Fire (no spreading).
5. **Animation**: in the KubeJS texture copies, reverse the frame order of both strips and set `frametime 1` (faster);
   keep the patcherMod copies in step or delete them (the KubeJS ones are what shows).
6. Docs: nether_portal.md 2.2 / 2.3 rows, F1 resolved; tests below.

### Ids and data
Verified: `creativeengineering:dark_fire`, `creativeengineering:flint_and_terrasteel`, `supplementaries:ash`,
`immersive_weathering:soot`, the infiniburn tag members. Vanilla `FireBlock.AGE`, `getFireTickDelay` (30-39) for the
tick rate; the SRG of `BaseFireBlock#tick` is not needed (own override in the pack's class).

### Decisions
- **"Only on solid blocks": any sturdy face or only the top.** Recommendation: any sturdy face (a frame is often lit
  from its side), matching today's survival rule.
- **Dark Fire off a sustain block: burns out or breaks at once.** Recommendation: burns out over ~30 s (age), like
  fire, so lighting a portal on obsidian still works and the flame lingers a moment.
- **Ash or soot.** Recommendation: Supplementaries ash (a placeable layer block; whether the pack already uses it as a
  burnt-wood result is unverified).

### Gotchas
- Obsidian is not a sustain block: a Dark Fire left in a frame that failed to form (open frame) ages out, which is
  wanted.
- Adding `AGE` changes the block's states: Dark Fire already placed in worlds loads with the default age, fine.
- The fire tag makes portal detection treat Dark Fire as interior; burning neighbours must not touch obsidian.

### Tests (NP-)
- NP-09 / NP-10 are replaced by: **NP-new1**: Flint and Terrasteel on air in front of a fence or a slab top (not
  sturdy) -> nothing; on stone -> Dark Fire. **NP-new2**: Dark Fire on netherrack stays; on stone it goes out within
  a minute. **NP-new3**: Dark Fire next to an oak log and planks -> they burn away in seconds, mostly leaving ash;
  no new fire appears. **NP-new4**: the flame animates faster and in the other direction than before.

---

## tnt-and-portal #6: Dark Fire lights portals in any dimension
Status **Planned** / Work patcherMod / TODO `gameplay/tnt-and-portal #6`

### Goal
Dark Fire opens a Nether portal only in the Overworld (decided 2026-10-29).

### Now
- `DarkFireBlock#onPlace` (56-87) calls `PortalShape.findPortalShape(level, pos, PortalShape::isValid, Axis.X)` (tries
  X then Z) and `ForgeEventFactory.onTrySpawnPortal` with no dimension check; `PortalFailureHandler`
  ([dimension/PortalFailureHandler.java](../../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/dimension/PortalFailureHandler.java)
  48-59) creates the portal for Dark Fire. Vanilla fire only fires the event in the Overworld and the Nether
  (`BaseFireBlock#inPortalDimension`), so ordinary fire in the End does nothing at all.
- NP-11 (End frame) has never been run.

### Build
1. In `DarkFireBlock#onPlace`: return before the shape search unless `level.dimension() == Level.OVERWORLD`.
2. Nether side: an ordinary flint and steel there still ruins the frame (PortalFailureHandler); Dark Fire there now
   just burns. Destination portals made by travelling are unaffected (`PortalForcer` fires no event).
3. nether_portal.md F2 resolved, NP-11 rewritten.

### Decisions
- **Nether-side relighting.** With Overworld only, a player whose Nether portal is broken cannot relight it in the
  Nether. Recommendation: keep the decision but say it in the Flint and Terrasteel tooltip / the writeup; confirm with
  the user that a player stuck in the Nether is acceptable (they can rebuild from the Overworld side only).

### Gotchas
- Mods with their own dimensions (Twilight-like, mining dims) count as "not Overworld" by this check, as intended.

### Tests (NP-)
- **NP-new5** (replaces NP-11): a frame in the End and one in the Nether, Dark Fire -> no portal; the same frame in
  the Overworld -> portal.

---

## Built, needs play

| Row | Change | Status | Note |
|---|---|---|---|
| #1 | TNT shrapnel | Testing | Shipped since 1.0.3; TN-01 to TN-10 never run (TN-03 changes with #2); [tnt_shrapnel.md](../tnt_shrapnel.md) |
| #4 | Nether portal ignition | Testing | Shipped since 1.0.3; NP-01 to NP-12 never run (NP-09 to NP-11 change with #5 / #6); remove the `PortalFailureHandler` diagnostic log (52) after NP-01 to NP-05; [nether_portal.md](../nether_portal.md) F5 |
