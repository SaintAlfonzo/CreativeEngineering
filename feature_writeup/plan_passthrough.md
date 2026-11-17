# Plan: pass-through blocks (walk-through and fall-through speed)

Status: Built, awaiting the bulk scratch run (2026-11-15: phases 0 to 7 done, rework of plan_post_scratch_1115b.md built
15:42; Creative Compat and Creative Devtools
1.0.0 built 00:40, not installed; decisions settled 2026-11-14, seven passes). TODO
`gameplay/blockrunner #10`. Feature doc when built: [blockrunner.md](blockrunner.md) (new section 3.10; tests BR-67
onward). Code owner: Creative Compat `surface/` (where BlockRunner, sinking, the WTHIT Run Speed line and the leaf
pile rule already live). Follow-up: swim speed audit, TODO `gameplay/blockrunner #11`.

## Goal

1. One system sets, per block, how fast you **walk through** it (horizontal) and **fall through** it (vertical), for
   every block you stand *in* rather than *on*: no collision box (grass, crops, ropes, cobwebs, leaf piles, bushes).
2. Some blocks slow more when **dense**: enough neighbours of the same kind around them.
3. The Immersive Weathering leaf pile rule moves into this system (today it is a one-off mixin).
4. Cobwebs and barbed wire are reworked (section 4); items and projectiles get web behaviour (sections 4.2, 5).
5. The WTHIT Run Speed line shows the combined walking speed (with the soil), the fall speed and a neighbour counter.
6. No walking speed change from BlockRunner or this system changes the field of view.

Non-goals: blocks you stand *on* (stays BlockRunner tiers); swimming (left alone, audited in `#11`); powder snow,
bamboo spikes and Create crushing wheels (they keep their own behaviour; the WTHIT line keeps showing them through
`StuckInBlock`).

## Owner rulings (2026-11-14)

Plants:
- Every planted crop: **0.75** walk. Short crops slow only from **half grown**.
- **Tall crops** slow as soon as their **lower block is half grown** (both blocks). A tall crop with no half-grown
  one-block stage slows as a whole, always.
- Dense crops: **0.50** at **6 or more** neighbours (ring): corn, hemp, rice, oats, wheat, tomatoes on vines, ropes
  and sticks. Rice: the top block (panicles) slows walking like a tall crop; **swimming is never slowed**.
- Farmer's Respite coffee stem: a normal crop.
- **Vines**: their own tier for **every vine**, 0.75; they keep their usual vine behaviour and slow you only while you
  **move horizontally through the vine's hitbox**.
- **Wild crops: 0.75.**
- 2-tall plants (tall grass, large fern, Biomes O' Plenty eyebulb, double flowers): **0.75**, no density.
- Sugar cane and propelplant cane: **0.50**, no density.
- Biomes O' Plenty crops are out (barley, cattail, reed).

Ropes and falling (revised 2026-11-14, fourth pass):
- Rope: **0.75 walk**, no fall or descent slowdown (vanilla ladder rules), no density.
- A fall slowdown also **scales the fall distance** that counts for fall damage, by its factor per block passed (webs).
- **Vines** (every vine): **0.75**, only while moving horizontally through the vine; vine behaviour otherwise unchanged.

Cobwebs and barbed wire (one tier):
- Speeds as proposed (0.50 walk / 0.85 fall; dense 0.35 / 0.20); neighbours in the full **3x3x3**; dense from **6**;
  dense **resets fall damage**; 26 of 26 **stops falling**. **Climbing and descending match the slowdown.**
- "Surrounded" counts when **any** web cell the entity overlaps is surrounded.
- **Items stop in every web**, even a single one, at a random point along their path through it.

Projectiles:
- All projectiles take the web slowdown **in every direction**; **modded bullets are exempt** from everything here.
- A surrounded web does **not** stop projectiles in general; they keep the dense web speed, except:
  - snowball, fireball, wither skull, llama spit, shulker bullet, ender pearl: **break in the middle** of the web
    block;
  - fishing bobber: **stuck in the middle**;
  - firework rocket: **stays in the web** until its flight time ends, then explodes or despawns;
  - thrown potion: **loses all horizontal motion** and **sinks slowly** down the middle of the web;
  - arrows: **hang where they are** on the tick they are in the surrounded web.
- Fast arrows skipping single webs is fine (not fixed).
- Fishing bobbers and Domestication Innovation's feather on a stick **snag in any web**, not only a surrounded one.
- **Every pickable arrow** (in a web or in the ground) despawns after **10 minutes**; at **128 per chunk** the oldest
  drops to the vanilla minute, and despawns at once if it is already past it. Special arrows **do not apply their
  effect** in a web; pickable ones come back **as themselves** (tipped, spectral, Nock Enough Arrows types, rope
  arrows). Skeleton, Infinity and multishot arrows stay unpickable.
- Snagged pickarangs and chakrams are **picked up like arrows** and **drop as items** when the web breaks.
- Rope walk slowdown does not apply while climbing.
- Quark pickarangs and the Botania thorn chakram: `snag` (both can end up dropped instead of returned).
- Blocky Siege munitions: exempt. Every unlisted projectile gets a defined group (section 5.1).
- WTHIT names the soil only when its speed is not 1.00.

Who is slowed, display, settled points:
- Villagers and every mob except spiders; horses too. Creative walking yes, creative flight no.
- Soil and pass-through speeds combine; WTHIT shows the **combined walk speed** and names the **soil and its tier**.
- No field of view change from surface or pass-through speeds.
- Items hanging in webs (and what it does to web farms): kept.
- Benchmark: the proposed budget is fine for now; a real performance check in normal play is needed (section 7.3).

Seventh pass, after phase 0 ([passthrough_phase0.md](briefs/passthrough_phase0.md)), these override the sections below:
- Tag namespace: `creativecompat:passthrough/<tier>` (not `blockrunner:`).
- Non-Loyalty tridents and thrown tools (Dragon Loot trident, Hearth cleaver and pitchfork): 10-minute life and count
  toward the 128, like pickable arrows. Loyalty ones still never despawn and do not count.
- Quark rope joins `rope`. Beautify has no twine (`beautify:rope` is a solid chain): out.
- Tomatoes (FD, Supplementaries rope/stick, CD treated stick) and Farmer's Respite coffee: growth `any`. Budding
  tomatoes `half`. Vintage oats: one-block `half`.
- Supplementaries flax: out.
- One leaf threshold for all leaves: IW leaf piles and Twigs bamboo leaves both `minLayers = 4` (Twigs moves from 3).
- `bush` gains the Hearth and Harvest raspberry bush and every Vinery grape bush (the taiga bush slows newly).
- BOP `hanging_cobweb` (and strand): `vine` tier.
- Unlisted plants that slow today (Floral Enchantment bushes, Farmer's Respite wither roots, Neapolitan beanstalk
  thorns, Upgrade Aquatic pickerelweed): left outside the system, as today.
- Other phase 0 implications (modded webs listed by id, projectile injectors beyond `checkInsideBlocks`, hemp/corn tops
  look down) are taken as written in the brief.

## Where things stand

- BlockRunner only reads the block **under** your feet (`getBlockStateOn`, 0.2 below). A block with no collision
  never registers, so crops and grass have no speed today.
- Slowdowns from inside a block come from each block's `entityInside` calling `Entity#makeStuckInBlock`. That call
  zeroes momentum every tick, so the real walking speed is about 0.45 x the multiplier: cobweb and barbed wire 0.25
  (0.11x), berry bush and Nether's Delight propelplant 0.8 (0.36x), Twigs bamboo leaves 0.75 (0.34x), IW leaf piles
  0.992-0.997 (0.45x).
- **Items in vanilla webs** get `(0.25, 0.05, 0.25)` with momentum zeroed every tick: gravity's 0.04 becomes 0.002
  blocks per tick, so an item creeps down about one block every 25 seconds and then drops out. The new rule is the
  same idea taken to its end (the item stops for good), not its inverse.
- Leaf piles: [LeafPileSlowMixin](../development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/mixin/LeafPileSlowMixin.java)
  changes IW's `layers > 3` to `CompatConfig [leaf_piles] slowFromLayers - 1`;
  [LeafPileSlow](../development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/surface/LeafPileSlow.java)
  carries the value to the WTHIT line.
- WTHIT: [StuckInBlock](../development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/surface/StuckInBlock.java)
  is a display-only table of every `makeStuckInBlock` caller. Horizontal only.
- [WallSlideMixin](../development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/mixin/WallSlideMixin.java)
  already hooks TAIL of `Entity#checkInsideBlocks` (`m_20101_`), where vanilla runs `entityInside`.
- **Field of view:** BlockRunner overrides `AbstractClientPlayer#getFieldOfViewModifier` and, with
  `disable_field_of_view_changes = true` (the pack's client setting), recomputes it without its own modifier
  (`FieldOfViewHelper`, by UUID). The pack's camouflage and sticky-facing swaps reuse that UUID, so they are covered.
  A new modifier with its own UUID would not be.
- **Projectiles ignore cobwebs today** (1.19.2 bytecode): `AbstractArrow#tick` and `ThrowableProjectile#tick` move
  with `setPos` and then call `checkInsideBlocks`. The web's `makeStuckInBlock` runs, but its multiplier is only read
  by `Entity#move`, which they never call. The hook still runs for them.
- **Barbed wire today** (Create Crafts & Additions 1.2.3): exactly the cobweb's `makeStuckInBlock(0.25, 0.05, 0.25)`
  for every entity, plus 2.0 damage (1 heart, `barbed_wire_damage = 2.0`) with a sound to living entities that moved
  that tick (up to 1 heart per half second). **After this plan:** the cobweb tier's speeds; the damage stays.
- **Swimming:** `LivingEntity#travel` in water uses a fixed 0.02 acceleration; the movement-speed attribute only counts
  through Depth Strider, so a speed modifier does nothing in water.
- **Ladders** (ropes are climbable): while inside a climbable block, vanilla caps the fall at 0.15 blocks per tick and
  **sets the fall distance to 0** every tick (`LivingEntity#handleOnClimbable`). Sneaking holds you still.
- **Supplementaries rope** has its own collision shape method, `isLadder` and `entityInside` (phase 0 reads them).

## 1. Model

A **pass-through tier** is a block tag `blockrunner:passthrough/<tier>` (filled in `blocktags.js` with
`global.tags` `t.addMatching`, like the surface tiers) plus a config block `[surface.passthrough.<tier>]`:

| Key | Meaning | Range |
|---|---|---|
| `walk` | horizontal speed multiplier while inside | 0.05-1.5 |
| `fall` | multiplier on downward speed each tick, and on fall distance per block passed | 0.0-1.0 |
| `growth` | `any`, `half` (own block half grown) or `tall_half` (lower block half grown, section 1.1) | enum |
| `neighbours` | `none`, `sides` (the 4 side-adjacent cells, same layer) or `faces` (the 6 face-adjacent cells); was `ring` (8) / `cube` (26) until 2026-11-15, an old file is migrated | enum |
| `denseFrom` | neighbour count at which the dense values apply (default 3) | 1-6 |
| `surroundedAt` | count that makes a `surrounded = web` block surrounded, clamped to the shape's max (default 6) | 1-6 |
| `denseWalk`, `denseFall` | values once dense | as above |
| `denseResetsFall` | fall damage resets every tick while dense | bool |
| `surrounded` | what a count of `surroundedAt` does: `none` or `web` (section 4.3) | enum |
| `walkOnlyMoving` | walk slowdown only while moving horizontally through the block's outline (vines) | bool |
| `items` | `none` or `stick` (section 4.2) | enum |
| `projectiles` | projectiles are slowed (section 5) | bool |
| `depthStriderPasses` | Depth Strider boots ignore the tier | bool |

Plus `[surface.passthrough] enabled` (master switch, also used by the benchmark) and `breakStallTicks` (section 5.1,
default 3). The layer tier's table `[surface.passthrough.layers]` has its own keys (section 2.1): `layerMaxSlow`,
`solidBelowFull`, `minLayers`, `solidBelowFullOverrides`, `fall`, `depthStriderPasses`.

**Neighbours** are blocks in the **same tier tag** (rice and rice panicles count for each other; cobweb, reinforced
cobweb and barbed wire count for each other). Solid blocks never count. A neighbour counts whatever its growth: a
freshly planted crop adds density to a grown one, but is itself slowed only once it passes its own growth rule.
Sides (since 2026-11-15, plan_post_scratch_1115b.md C1): a field's inside has 4, an edge 3, a corner 2, so 3+ means
"inside the field or on its edge, not a corner". Faces: a web with webs on 3 of its 6 faces is dense; all 6 (open
neither above nor below) is surrounded.

**Counts are baked into the chunk** (plan_post_scratch_1115b.md C2, `surface/PassCounts`): computed once on first
use, kept in a chunk capability (saved on the server, one entry per counted block; the client keeps its own unsaved
copy), dropped only when a block of another tier replaces one in or next to the cell (break, plant, place,
explosion, piston, fluid). Harvest and replant (age reset of the same block) keep them. A chunk that loads drops its
edge entries and its loaded neighbours' facing ones; a tag reload makes every chunk recount.

**Which block counts:** each tick, every tier block whose cell the entity's box overlaps (deflated 1e-7, like vanilla)
and whose **outline shape** the box touches, and that passes its growth rule. Several blocks at once (feet in corn,
head in a web): the lowest `walk` and the lowest `fall` win, each with its own density.

**Exempt:** entity-type tags `creativecompat:passthrough_exempt/<tier>`: cobweb = spiders and cave spiders; berry bush
and leaf pile = foxes and bees (vanilla and IW rules, kept). Every other mob, villagers and horses included, is slowed.
`creativecompat:passthrough_exempt/all` = modded bullets (section 5.1). Also skipped: spectators, **flying** players
(creative or not), elytra gliding, and **swimming** (eyes in a fluid, or the swimming pose).

### 1.1 Growth rules

- `half`: the block's `age` over its maximum is at least 0.5. Blocks without `age` use their own growth property
  (phase 0 table); a block with no growth property counts as grown.
- `tall_half`, for tall crops: the **lower** block's growth decides for both blocks. The upper block looks down to its
  lower block (one lookup).
- A tall crop with **no half-grown one-block stage** (it is already two blocks tall before the lower block is half
  grown, or the lower block has no readable growth): both blocks slow whenever the crop exists, at every age. Phase 0
  lists which crops fall in this case.
- Wild crops: `any` (they have no growth stage).

### 1.2 How the numbers are applied

- **Walk, on land:** a transient `MOVEMENT_SPEED` modifier, `MULTIPLY_TOTAL`, value `walk - 1`, its own UUID, added
  while inside and removed on leaving (BlockRunner's pattern). It multiplies with the surface tier under you. Momentum
  is kept, so 0.75 means 0.75x walking speed. Creative players who are walking get it; flying ones do not.
- **Walk, wading** (feet in water, eyes out: a rice paddy): the speed attribute does nothing in water, so the walk
  value is applied as a per-tick horizontal drag of the same strength instead. Swimming is untouched.
- **Fall:** if moving down, `dy x= fall` each tick. **Fall distance:** multiplied by `fall` **once per tier block the
  entity passes through** (entering a new cell), not every tick, so falling through one lone web gives x0.85, through
  three webs about x0.61. Dense with `denseResetsFall`: `fallDistance = 0` every tick.
- **Neutralise the old slowdown:** a mixin on `Entity#makeStuckInBlock` (`m_7601_`) at HEAD cancels the call when the
  state is in any pass-through tier. One hook covers vanilla, IW, Twigs, berry bushes, webs and barbed wire; their
  other `entityInside` effects (berry, thorn and wire damage) stay.

### 1.3 Field of view

The pass-through modifier is taken out of the field of view the same way BlockRunner takes out its own: a Compat
client mixin at RETURN of BlockRunner's `FieldOfViewHelper` path (or of `AbstractClientPlayer#getFieldOfViewModifier`
when BlockRunner's option is off) that recomputes the speed part without the pass-through UUID. Walking into a field,
onto asphalt or onto a camouflage block never changes the field of view. Sprinting and potions still do (vanilla).

### 1.4 Server and client must agree

Player movement runs on the client. Tags sync already; the config values do not (`[surface]` is a common config).
The server sends its pass-through values on login and on every config save (Compat's existing channel if there is
one, else a new one in `surface/`), and the client uses the server's values while connected. Projectiles and items
are simulated on both sides, so they use the same values; the random item stop point (4.2) is server-only and synced
by the item's normal position updates.

## 2. Tiers

| Tier | Members | walk | fall | growth | Neighbours | Dense from | denseWalk / denseFall | Other |
|---|---|---|---|---|---|---|---|---|
| `dense_crop` | corn, IE hemp, FD rice + rice panicles, Vintage Delight oats, wheat, tomatoes (FD tomatoes and budding tomatoes, Supplementaries rope and stick tomatoes, Creative Delight treated stick tomatoes) | 0.75 | 1.00 | `tall_half` (wheat: `half`) | sides (was ring) | 3 of 4 (was 6 of 8) | 0.50 / 1.00 | rice base is in water: wading drag |
| `crop` | every other planted crop (`#minecraft:crops` and modded crop blocks minus `dense_crop`), Farmer's Respite coffee stem | 0.75 | 1.00 | `half` (coffee: `tall_half`) | none | | | |
| `wild_crop` | Farmer's Delight wild crops, wild corn, wild rice, wild oats, other mods' wild crops | 0.75 | 1.00 | `any` | none | | | |
| `vine` | every vine: vanilla vines, twisting and weeping vines (plant and tip), cave vines (glow berries), Vinery grape vines, and other mods' vines (`VineBlock` and `GrowingPlantBlock` subclasses plus named ones; phase 0 list: Floral Enchantment cornflower vine, Biomes O' Plenty willow vines and spanish moss, Quark, Upgrade Aquatic, others) | 0.75 | 1.00 | `any` | none | | | `walkOnlyMoving`; climbing stays as the block has it |
| `tall_plant` | tall grass, large fern, BOP eyebulb, sunflower, lilac, rose bush, peony, Botania double flowers, other mods' 2-tall `DoublePlantBlock`s | 0.75 | 1.00 | `any` | none | | | |
| `cane` | sugar cane, propelplant cane (Nether's Delight, Supplementaries) | 0.50 | 1.00 | `any` | none | | | moves propelplant out of `bush` |
| `rope` | Farmer's Delight rope, Supplementaries rope, Beautify twine | 0.75 | 1.00 | `any` | none | | | ladder rules unchanged (3.2) |
| `cobweb` | `WebBlock` and subclasses (cobweb, SecurityCraft reinforced cobweb, Fantasy Furniture spider webs), Create Crafts & Additions barbed wire, IE razor wire | 0.50 | 0.85 | `any` | faces (was cube) | 3 of 6 (was 6 of 26) | 0.35 / 0.20 | `denseResetsFall`, `surrounded = web` at 6 of 6 (was 26 of 26), `items = stick`, `projectiles`, spiders exempt |
| `bush` | sweet berry bush, Vinery taiga grape bush | 0.36 | 1.00 | `any` | none | | | today's real speed; foxes and bees exempt |
| `layers` (was `leaf_pile`) | layer height blocks, tag `creativecompat:passthrough_layers` (2.1) | by height | 1.00 | `any` | none | | | `minLayers = 2`, `depthStriderPasses`, foxes and bees exempt |
| `grass` | short grass, fern, other short plants | 1.00 | 1.00 | `any` | none | | | listed so it can be tuned; 1.00 = no effect |

Out: Biomes O' Plenty barley, cattail and reed.

### 2.1 Layer height blocks (plan_post_scratch_1115b.md D, 2026-11-15)

One rule for every stacked-layer block; replaces the leaf_pile tier (flat 0.45 from 4 layers) and BlockRunner's snow
layers (the `snow` surface tier keeps the snow block, coarse snow and powder snow). Members: vanilla snow layers; every
Immersive Weathering leaf pile (azalea flower pile included), IW ash, sand and red sand layers; Twigs bamboo leaves;
Supplementaries ash and fodder; IE sawdust. Out (owner): Create Deco coin stacks, Control Engineering logic cabinet,
Copycats layers.

- `max` = the block's own highest `layers` value (snow 8, sawdust 9, bamboo leaves 4); `solidBelowFull` (default 2,
  per-block `solidBelowFullOverrides`, default `twigs:bamboo_leaves=0`). Layers `>= max - solidBelowFull` act whole:
  no slowdown, normal collision and the Run Speed line. Highest walk-through layer `H = max - solidBelowFull - 1`.
- `walk(l) = 1 - (1 - layerMaxSlow) * (l - 1) / (H - 1)` for `2 <= l <= H`; one layer 1.0; `H < 2` never slows.
  Defaults, 8 layers: 1.0, 0.875, 0.75, 0.625, 0.5 for 1 to 5 layers, 6 to 8 whole. Sawdust: 0.5 at 6. Bamboo leaves:
  0.75 at 2, 0.5 at 3, 4 whole.
- WTHIT: `Walk Through: 0.75x (3 of 8 layers)`.

## 3. Leaf piles and ropes

### 3.1 Leaf piles move here
- Delete `LeafPileSlowMixin` and `surface/LeafPileSlow`; remove its `creativecompat.mixins.json` entry and the
  `LeafPileSlow.set(...)` call in Creative Engineering (creative_engineering.md 2.2 and creative_compat.md note it).
- `[leaf_piles] slowFromLayers` becomes `[surface.passthrough.leaf_pile] minLayers`. On first load, carry over the
  live value (read the old key if present), then drop the old section. Update `configs_edited/`.
- 2026-11-15 (plan_post_scratch_1115b.md D): the leaf_pile tier is replaced by the layer tier (2.1).
  `surface/PassConfigMigration` (after `LeafPileMigration`) drops `[surface.passthrough.leaf_pile]` and carries its
  edited values into `[surface.passthrough.layers]` (`minLayers` unless the old default 4, `fall`,
  `depthStriderPasses`, `walk` as `layerMaxSlow`).
- IW's `leaf_piles_slow` no longer matters (its `makeStuckInBlock` is cancelled); say so in creative_compat.md.
- IW spruce thorns (6+ layers) stay IW's.

### 3.2 Ropes
- Walking through a rope: 0.75x. Climbing, sliding and sneaking on it: vanilla ladder rules, no extra slowdown (the
  fall-through and descent slowdowns were dropped 2026-11-14), and the 0.75 does **not** apply while you are climbing
  (owner). "Climbing" = on the rope and moving up or down, sneaking on it, or not touching the ground.
- Fall damage: vanilla already resets the fall distance every tick you are inside a climbable block, so falling past
  a rope cancels fall damage as before.

## 4. Cobwebs and barbed wire

### 4.1 Mobs and players
Vanilla's slowdown is cancelled by the generic hook. Instead (barbed wire the same, plus its damage):
- Up to 2 of the 6 face neighbours: walk 0.50, fall 0.85 (a small vertical drag; fall distance x0.85 per web passed;
  vanilla's fast-fall reset clip leaves such webs out, scratch fixes 2026-11-15).
- 3 or more: walk 0.35, fall 0.20, fall damage reset every tick.
- 6 of 6: section 4.3.

### 4.2 Items and XP orbs: stick in every web
- Every tick an item or orb is inside a web, it rolls a **stick chance** (`[surface.passthrough.cobweb]
  itemStickChance`, default 0.35 per tick). On a hit, it stops at a **random point along this tick's path**
  inside the web cell (start of the tick to end of the tick, clipped to the cell).
- If the tick would carry it **out** of the web and no roll has hit yet, it stops **just inside the exit point**.
  So every item that enters a web ends up stuck in it, at a random depth along its arc.
- An item that **starts** a tick inside a web with no motion (spawned there, or the web was placed on it) sticks at
  once where it is.
- Stuck: no gravity, no motion, until the web is broken (then it falls normally). Hoppers and players pick it up as
  usual where it hangs. A flag on the entity (persisted in its save data, so stuck items stay stuck after a reload)
  drives a hook on the gravity step (`ItemEntity#tick` checks `isNoGravity()` before the move).
- **ItemPhysic** replaces item ticking on the server
  ([ItemPhysicServerMixin](../development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/mixin/ItemPhysicServerMixin.java)
  already patches it): phase 0 finds where it applies gravity and hooks that too.
- Fast items can cross a whole web in one tick: the path is clipped to the cell, so they still stick (unlike arrows,
  items get the path check because the owner wants every item to stop).

### 4.3 Surrounded (6 of 6, was 26 of 26 until 2026-11-15)
- Any web cell the entity overlaps has webs on all 6 faces (`surroundedAt`, default 6): falling stops (`dy < 0`
  becomes 0, fall distance 0). A web open above or below is dense but not surrounded.
- **Climbing and descending match the slowdown:** jump climbs at ladder speed x the dense walk value (0.2 x 0.35 =
  0.07 blocks per tick); sneak descends at ladder slide speed x the same value (0.15 x 0.35, about 0.05 blocks per
  tick). No input: you hang. Sideways: 0.35x.
- Mobs hang; they climb only while jumping (pathing up).

## 5. Projectiles

### 5.1 Behaviour groups
Each projectile gets one behaviour, chosen by an entity-type tag `creativecompat:passthrough/projectile/<group>`; with
no tag, a default by its base class (arrow-like -> `hang`, throwable -> `break`, self-propelled -> `break`, other ->
`slow`). Everything except `exempt` takes the web slowdown (whole velocity x `walk`, plus x `fall` downward; dense
values when dense) in every web. The group decides what happens in a **surrounded** web (`snag`: in **any** web):

| Group | What happens | Members |
|---|---|---|
| `break` | in a surrounded web, breaks **in the middle** of the web block: the impact is run as a block hit at the cell centre, as if the web were a full block. Fallback (plan_post_scratch_1115b.md B): stopped in **any** web (under 0.05 b/t for `breakStallTicks` ticks in a row, default 3, 0 = off), the impact runs **where it stopped** (an ender pearl teleports there), then it is discarded | snowball, fireballs (small, large, dragon), wither skull, llama spit, shulker bullet, ender pearl; eggs (vanilla, Untitled Duck Mod, Alex's Mobs emu and cockroach, Quark parrot); Twigs pebble; Supplementaries bomb, thrown brick, slingshot shot; Alex's Mobs tossed item and enderiophage rocket; Botania vine ball, magic missile; PneumaticCraft micromissile and vortex; Immersive Petroleum molotov; Creative Compat shrapnel; Farmer's Delight rotten tomato; Hearth and Harvest manure; Canned Goods Mini Fizz; Create Misc and Things Blaze Ballon fire |
| `settle` | in a surrounded web, loses all horizontal motion, moves to the cell's centre line and **sinks slowly** (dense fall speed) down the middle; breaks when it hits a block or leaves the webs and lands | thrown potions (splash, lingering); experience bottle; Create Enchantment Industry hyper experience bottle; Splash Milk bottle; Botania ender air bottle |
| `snag` | in **any** web, stuck in the middle of the web block it entered (centre of the cell), held until pulled back, picked up or the web breaks | fishing bobber; Domestication Innovation feather on a stick; Quark pickarangs (all kinds); Botania thorn chakram |
| `linger` | in a surrounded web, **stays** (stopped at the point it entered the surrounded cell) until its flight time ends, then explodes (if it has stars) or despawns | firework rocket |
| `hang` | in a surrounded web, **hangs where it is** on that tick (5.3) | arrows, spectral and tipped arrows, tridents, Dragon Loot dragon trident, Nock Enough Arrows' 25 arrows, Neapolitan bananarrow, Alex's Mobs shark tooth arrow, Supplementaries rope arrow, Quark torch arrow, Hearth and Harvest thrown cleaver, horseshoe, pitchfork and thrown tools, Create Misc and Things neon tube |
| `slow` | dense slowdown only, never stopped | Botania throwable copy (no caller found in Botania 440.2; kept slowed only); any future projectile with no tag and no matching default |
| `exempt` | not slowed at all, anywhere (modded bullets and munitions, owner ruling) | Old Guns bullet and rocket; Immersive Engineering revolver shots (all kinds), railgun shot, chemthrower shot, sawblade; SecurityCraft turret bullet and IMS bomb; Mysterious Mountain Lib bullet; Create potato cannon shot; Create Misc and Things spout gun shots; Power Grid zap; Botania mana burst, Babylon weapon, falling star; Blocky Siege munitions (all 13); PneumaticCraft tumbling block |

The table comes from a scan of every jar in `mods/` and their nested jars against vanilla's class tree (2026-11-14:
126 projectile classes; base classes `Projectile`, `ImprovedProjectileEntity` (Moonlight), `IEProjectileEntity`,
`AbstractPickarang` and `ThrownTool` are covered through their subclasses; `TrainScenes$1` in Steam Rails is a
ponder-scene helper, not a real projectile). Every class has a group; none is left to the fallback. Rerun after adding
mods; phase 4 makes it a tool.

Why the newly defined ones sit where they do:
- Rotten tomato, manure, Mini Fizz (its hit spawns an entity), Blaze Ballon fire: thrown or fired objects that burst on
  impact, so `break`.
- Hearth and Harvest thrown tools and the neon tube: arrow-based items that are picked back up, so `hang`.
- Pickarangs: Quark drops the pickarang as an item when it cannot reach its thrower; the chakram spawns an item when
  it has no thrower. Both can end up dropped, so `snag` (owner rule). Snagged, they do not return by themselves: they
  are **picked up like an arrow** (walk into them, given back as the same item with its damage and enchantments), and
  **drop as items when the web breaks**. Snagged pickarangs and chakrams never despawn (they are tools).
- SecurityCraft IMS bomb: security-device ammunition, like the turret bullet, so `exempt`.

### 5.2 Notes
- Ender pearl `break` teleports the thrower to the middle of the surrounded web, where falling is stopped (they hang
  there; climbing out at web speed).
- `settle` potions break against the block they land on, so the splash is below the webs, not inside them.
- Fishing bobber `snag`: the line can be reeled in (vanilla pulls the bobber back); it does not hook the web.
- Feather on a stick `snag`: the feather stays in the web, so pets chasing it go to the web.
- Firework `linger`: a crossbow rocket's explosion damages whoever is near the web when it ends.
- Loyalty tridents returning (`noPhysics`) are skipped. Slower arrows hit softer (vanilla damage scales with speed).
- Fast arrows can cross a single web in one tick and skip it: accepted (owner).

### 5.3 Arrows hanging in webs (`hang` group)
- On the tick it is in a surrounded web, the arrow stops where it is and goes into vanilla's stuck-in-ground state
  (`inGround`, `lastState` = the web): it falls when the web breaks.
- **No on-hit effect:** the arrow's block-hit code never runs (explosive, teleportation, hookshot, drill, growing,
  paint, party, rope arrows and the rest do nothing). The arrow stays whole.
- **Not pickable:** arrows from skeletons (and other mobs), Infinity arrows and multishot side arrows stay
  unpickable, as in vanilla (`pickup` is not `ALLOWED`). They hang and despawn on vanilla's minute.
- **Pickable arrows come back as themselves:** a tipped arrow keeps its potion, a spectral arrow stays spectral, every
  Nock Enough Arrows arrow stays its own type, a Supplementaries rope arrow keeps its remaining rope, a trident or
  thrown tool keeps its damage and enchantments. Where a mod's pickup item strips the type (returns a plain arrow),
  a web-stuck arrow returns its own item instead: phase 0 reads every `hang`-group class's `getPickupItem` and lists
  the ones that need the fix.

### 5.4 Arrow despawn: 10 minutes, 128 per chunk (every pickable arrow, in a web or in the ground)
- **Every arrow players can pick up** (`pickup == ALLOWED`), stuck in a web or in a block, despawns after **10
  minutes** (`[surface.passthrough] arrowLifeTicks`, 12000) instead of vanilla's 1 minute. Unpickable arrows keep the
  minute.
- **Cap: 128 long-life arrows per chunk** (`arrowLongLifeCap`). When a 129th arrow sticks in a chunk, the **oldest**
  long-life arrow there (highest `life`) is demoted to vanilla's 1-minute timer: if it has already been stuck for more
  than 1 minute (`life >= 1200`), it despawns at once; otherwise it despawns when its `life` reaches 1200.
- Implementation: a mixin on `AbstractArrow#tickDespawn` swaps the 1200 for 12000 unless the arrow is flagged
  short-life (flag saved in the arrow's data, so it survives a reload). A server-side per-level map, chunk ->
  long-life arrows, is updated when an arrow sticks, is picked up, despawns, starts falling or unloads with its chunk,
  and refilled from the chunk's arrows when the chunk loads.
- **Never-despawn projectiles stay that way:** vanilla `ThrownTrident#tickDespawn` skips despawning for pickable
  tridents (a player's trident never despawns), and the Dragon Loot trident and Hearth and Harvest thrown tools may do
  the same (phase 0). The 10-minute rule and the cap do not apply to them, so no thrown trident or tool is ever lost
  to it. They do not count toward the 128.
- Loyalty tridents: vanilla starts the return a few ticks after a trident sticks, so a hanging loyalty trident flies
  back to its owner as usual.

## 6. WTHIT Run Speed line

For a pass-through block, up to three lines, from the same rule code the movement uses:

- `Walk Through: 0.40x (Dense neighbors) on Farmland (Tilled)`: the **combined** speed, this block's walk value x the
  speed of the block it sits on. The soil's name and tier are added only when the soil's speed is not 1.00 (crops on
  dirt read `Walk Through: 0.75x`).
- `Fall Through: 0.20x (Dense neighbors)`; `Fall Through: stopped` in a surrounded web; hidden when 1.00
  (`hideNormal`).
- `Neighbors: 7` for tiers that count neighbours.
- "(Dense neighbors)" goes only on a speed that density actually lowered.
- A crop below its growth rule: no line. Exempt for the viewer (Depth Strider on a leaf pile): no line.
- Vine: walk line noted "while moving through".

`StuckInBlock` keeps only the blocks not in any tier (powder snow, bamboo spikes). Formatting goes in a plain class
(`PassThroughFormat`) so the headless harness can test it. New switch `[surface.wthit] passThrough` (default true).
The count runs on the client against the looked-at block, once per WTHIT refresh.

## 7. Performance

### 7.1 Micro benchmark: `/cdevbench passthrough [calls]` (Creative Devtools, dev-only)
- Builds fixtures in a far-off flat area: a 16x16 `dense_crop` field, a 5x5x5 web cube, a sparse field (every other
  block), plain air.
- Times, after a warmup, over `calls` (default 1,000,000) with results summed so the JIT cannot drop them: ring count
  (8 lookups), cube count (26 lookups), and the full per-entity tick (`PassThrough.tick`) for an entity in air (the
  fast path), a lone crop, the middle of the field, the middle of the web cube, and a 2x2-cell mob (horse) in the
  field; one item stick roll with its path clip.
- Reports ns per call (median and 99th percentile of 20 batches) to chat and to
  `kubejs/exported/perf/passthrough_bench.json`.

### 7.2 Macro: perf suite scenario ([perf_suite.md](perf_suite.md), `tools/perf/suite`, bot players)
- Scenes: 200 cows penned in a 32x32 corn field; 50 villagers working a wheat field; 100 zombies in a 9x9x5 web
  block; 500 dropped items thrown into webs; 100 arrows a second into a web wall; a bot player walking circles in the
  field.
- Each scene measured with `[surface.passthrough] enabled` true and false, same seed and window.
- Budget (accepted for now): micro under 1 microsecond per entity-tick in the dense field and web cube; macro under
  0.5 ms added mean tick time at 500 affected entities. If it fails: cache the count per block position for the
  current tick (herds share cells), then cheaper cube counts for mobs.
- 2026-11-15 (plan_post_scratch_1115b.md C): both done further: counts are 4 or 6 cells and baked into the chunk
  (1.0 "Counts are baked"), so `tick_web_cube` and the field cases fall to a map lookup after the first entity. Target:
  under the 1000 ns budget, PT01 and PT04 under +0.5 ms. Not measured yet: reruns SQ-PTH-25c, SQ-PTH-28c
  (scratch_queue.md SQ-PS15B); the numbers go in blockrunner.md 3.10 and perf_suite.md 12. The bench's first-entity
  cases call `PassCountCache.clear()`, which now also makes every chunk recount, so they stay cold.

### 7.3 Real play (owner: needed)
- Dev config `[debug.passthrough] timing` (off by default): every 200 ticks, logs the hook's total and maximum time,
  the number of entities that were inside a tier block, and how many neighbour counts ran, on the server and the
  client separately.
- The owner plays a normal session with it on (farm, village, a web build). `tools/perf/log_timings.py` (or a small
  new parser beside it) reads those lines from `logs/latest.log` and reports the share of tick time. A JFR recording
  of the same session (perf.md) confirms where the time goes. Results go in blockrunner.md 3.10 and decide whether 7.2's
  caching is built.

## 8. Phases

### Phase 0: verify (no edits)
- Read (`javap` / vineflower): `WebBlock#entityInside`, `Spider#makeStuckInBlock`, IW `LeafPileBlock#entityInside`;
  every tall crop's two-block structure and growth property (Corn Delight `corn_crop`, FD `RiceBlock` /
  `RicePaniclesBlock`, FD tomatoes and budding tomatoes, Supplementaries rope and stick tomatoes, IE hemp `growth`,
  Vintage `oat_crop`, Farmer's Respite coffee stem) and the growth-rule table (1.1) that results; Vinery grape vine
  blocks (climbable or not, outline); wild crop ids; which ids use Nether's Delight `PropelplantBlock`; BOP eyebulb and
  other double plants; the three ropes (climbable or not, Supplementaries `RopeBlock` collision and `entityInside`);
  ItemPhysic's server item tick; `AbstractArrow` stuck fields; `ThrowableProjectile#onHit`;
  `AbstractHurtingProjectile#tick`; `FishingHook` states; `FireworkRocketEntity` lifetime; BlockRunner
  `FieldOfViewHelper`; each arrow's pickup item and pickup rule; Quark pickarang and Botania chakram drop paths.
- Confirm nothing else mixes into `makeStuckInBlock` (grep jars for `m_7601_`).
- Check whether Compat or Common already has a network channel to reuse.

### Phase 1: core (Creative Compat)
- `surface/PassTier` (enum, config keys and defaults as section 2), `surface/PassThrough` (block -> tier cache
  cleared on `TagsUpdatedEvent`, outline and growth tests, ring and cube counts, the rule result), config sections.
- One TAIL hook on `checkInsideBlocks` calling `WallSlide` then `PassThrough` (rename `WallSlideMixin` to
  `InsideBlocksMixin`, or a second injector in it).
- `MakeStuckInBlockMixin`, the movement modifier, wading drag, fall drag and per-block fall distance, swimming and
  flight skips, vine moving check.
- Field of view fix (1.3), covering BlockRunner's modifier and the new one.
- Sync packet (1.4).
- Headless tests: rule results per tier at 0, 5, 6, 8 (ring) and 0, 5, 6, 25, 26 (cube); growth rules at ages 0,
  half, max for one- and two-block crops; fall distance after 1 and 3 blocks; `minLayers`; exempt tags; modifier
  math; item stick point (always inside the cell, exit clamp); config migration of `slowFromLayers`.

### Phase 2: cobwebs, items, projectiles
- Item and orb sticking (vanilla `ItemEntity`, `ExperienceOrb`, ItemPhysic's path), saved flag.
- Dense fall reset; surrounded hang, climb and descend.
- Projectile groups (5.1) with their default-by-class fallback and tags.

### Phase 3: leaf piles and ropes
- Section 3.

### Phase 4: tags and tools
- `blocktags.js`: the eleven `blockrunner:passthrough/*` tags with `t.addMatching`, `t.overlaps` so a block sits in
  one tier only (tomato ropes in `dense_crop`, not `rope`). Entity-type tags (exempt, projectile groups) through
  `global.tags`.
- `tools/blockrunner_scan.py --audit`: list no-collision blocks with no pass-through tier (report only).
- The projectile scan as a tool (`tools/projectile_scan.py`, jar class tree, lists projectiles with no group tag) and a
  generator entry.

### Phase 5: WTHIT
- Section 6. `WthitRunSpeed` asks `PassThrough` first, then `StuckInBlock`.

### Phase 6: performance
- Section 7: the Devtools command, the perf suite plan file, the timing log and its parser; scratch queue rows.

### Phase 7: close out
- blockrunner.md 3.10 (model, tiers, ropes, cobwebs, items, projectiles, field of view, WTHIT, performance), section 4
  limits, file map, BR-67.. tests; creative_compat.md config table; mob_block_tweaks.md MT-25 points here.
- Scratch queue rows (skill `scratch-test`): stuck slowdown cancelled for tiered blocks, items stick in a single web
  and stay stuck after a restart, arrow hang and pickup, each projectile group in a surrounded web, config migration,
  synced values on join, the benchmark.
- Player changelog family `## Walking through plants and webs`: arrows last 10 minutes (was 1), crops, dense crops, tall plants, canes, vines, ropes,
  cobwebs and barbed wire (old behaviour in brackets), items in webs, projectiles in webs, no field of view change on
  fast blocks; then `python3 tools/changelog_lint.py`.
- Build `:creativeCompat:build` (and `:creativeDevtools:`), install when the game is closed, restart.

## 9. In-game tests (numbered BR-67 onward when written)

- Wheat field: freshly planted no slow; half grown, the edge row 0.75x, two rows in 0.50x; WTHIT `Neighbors: 5`, then
  `Neighbors: 8` with `(Dense neighbors)`, and the walk line reads the combined speed "on Farmland (Tilled)".
- Corn: slows (both blocks) once the lower block is half grown, not before.
- Carrots: half grown 0.75x. Wild rice: 0.75x. Tall grass, a rose bush: 0.75x. Sugar cane 0.50x.
- Tomatoes on ropes in a 3x3 block: dense 0.50x. Coffee: 0.75x, no counter.
- Grape vines: slowed only while walking through; standing in them, climbing them as before.
- Rope: walking through 0.75x; climbing, sliding and sneaking as vanilla.
- Vines (vanilla vine, twisting vines, cave vines, grape vines): 0.75x while walking through, climbing unchanged.
- Rice paddy: wading slowed; swimming in deep water beside it normal.
- Creative: walking through corn slowed, flying through it not.
- Single web: walk about half speed, fall a little slow, fall damage cut by 0.85 per web. A web with 6+ neighbours:
  land from a height without damage. A 3x3x3 cube: drop in, stop in the middle; jump climbs slowly, sneak sinks slowly,
  no input hangs; sideways out.
- Barbed wire: same speeds as a web, still hurts while moving.
- Spiders and cave spiders through webs at full speed; a villager, a zombie and a ridden horse slowed.
- Items thrown into a single web stop inside it at different depths; one dropped straight through stops near the
  bottom; break the web, they fall; after a restart, still stuck. Also with ItemPhysic.
- Surrounded web: a tipped arrow hangs and is picked up as the same tipped arrow; an explosive arrow hangs without
  exploding; a hanging arrow is still there after 5 minutes; a snowball and an ender pearl break in the middle; a
  splash potion sinks down the middle and splashes below; a firework hangs then pops; an Old Guns bullet and a Blocky
  Siege cannonball fly straight through unslowed.
- Single web: a fishing bobber, a feather on a stick and a pickarang snag in the middle.
- Arrow despawn: a player's arrow in the ground is still there after 5 minutes, gone after 10; a skeleton's arrow is
  gone after 1 minute and cannot be picked up from a web. With `arrowLongLifeCap` set to 3 in one chunk: a fourth
  arrow makes the oldest despawn at once if it is over a minute old, else after its first minute. A thrown trident
  never despawns.
- Picked up from a web: a tipped arrow, a spectral arrow, a Nock Enough Arrows explosive arrow and a rope arrow all
  come back as themselves. A snagged pickarang is picked up by walking into it; breaking the web drops it.
- Leaf pile: 3 layers no slow; 4 layers 0.45x; Depth Strider none.
- Berry bush: same as before, still hurts.
- Field of view unchanged walking onto asphalt, onto honey, into a field, into a web.
- Two players: no rubber-banding in a field after changing `dense_crop.denseWalk` on the server.

## 10. Open decisions (owner)

Settled 2026-11-14 (kept for the record): seedlings and tall crop growth, barbed wire, field of view, fluids,
projectile direction and exceptions, cobweb speeds and dense count, items stick in every web, surrounded cell (any
overlapped web), climbing in a surrounded web (matches the slowdown), tunnelling (accepted), villagers and horses
(slowed), stacking (combine), WTHIT (combined walk speed with the soil), creative (walk slowed, flight not), tomato
vines and ropes (dense crop), coffee (crop), wild crops (0.75), web farms (kept), benchmark budget (fine for now).

Also settled in the fourth pass: rope (0.75 walk, no fall or descent slowdown), vines (all, 0.75), item stick chance
(0.35 default), modded projectile groups, pickarang and chakram (`snag`), bobber and feather (snag in any web), special
arrows (no effect, picked up as fired), arrow despawn (10 minutes with a cap), Blocky Siege (exempt), soil naming
(only when not 1.00).

Settled in the fifth pass: rope walk does not apply while climbing; skeleton, Infinity and multishot arrows stay
unpickable; pickable special arrows come back as themselves; every pickable arrow (web or ground) lasts 10 minutes,
128 per chunk, the oldest demoted to the minute (or despawned at once if past it); snagged pickarangs and chakrams are
picked up like arrows and drop when the web breaks.

Settled in the sixth pass (owner confirmed the defaults): tridents and never-despawn thrown tools are exempt from the
10-minute rule and the per-chunk count; the 10 minutes is pack-wide for every pickable arrow, with its own changelog
line; Infinity, skeleton and multishot arrows simply cannot be picked up (vanilla rule kept).

**No open decisions.** Ready for phase 0.

Open from plan_post_scratch_1115b.md (2026-11-15), waiting for owner:
- D0: the authoritative list of layered blocks needs a Devtools dump of every block with an integer `layers` /
  `layer` / `height` / `snow_layers` / `thickness` property (min, max, collision height per value); the tag is built
  from the blockstate scan (2.1) until then.
- Double slowdown on layered blocks still in a BlockRunner surface tier: IW sand, red sand and ash layers,
  Supplementaries ash and IE sawdust (`dust`, 0.85), Supplementaries fodder (`soft`, 0.88), Twigs bamboo leaves
  (`leaves`, 0.78) multiply with the height slowdown at 3 to H layers (BlockRunner registers from 3 layers). Snow
  layers were taken out of `snow` as the plan says; take these out of their surface tiers too, or keep both?
- Foxes and bees were exempt from leaf piles; the exempt tag is per tier, so they now walk through every layered
  block unslowed (snow, sand, ash too). Keep, or narrow it?

## 11. Projectile scan (2026-11-14, for reference)

Jars with projectiles (top-level and nested): vanilla (19 classes), Botania (8), Immersive Engineering (8),
Immersive Petroleum (1), Quark (6), SecurityCraft (2), Alex's Mobs (5), Power Grid (1), Blocky Siege (13), Create (1),
Create Enchantment Industry (1), Create Misc and Things (7), Creative Compat (1), Domestication Innovation (1), Dragon
Loot (1), Moonlight (base class), Mysterious Mountain Lib (1), Neapolitan (1), Nock Enough Arrows (25), Old Guns (2),
PneumaticCraft (3), Splash Milk (1), Supplementaries (4), Twigs (1), Untitled Duck Mod (1), Hearth and Harvest backport
(5), Canned Goods repack (1), Farmer's Delight repack (1), Steam Rails (ponder helper only). Script: a class-file
parser over the jars plus the 1.19.2 SRG client jar, walking superclasses to `Projectile`; phase 4 turns it into
`tools/projectile_scan.py`.

## Risks and notes

- One tier per block, checked with `t.overlaps`; a block in two tiers gets a random one per reload.
- The `makeStuckInBlock` cancel is keyed by tag: a tiered block loses all of its stuck slowdown. Powder snow stays
  out for that reason.
- Mobs in a surrounded web with nothing to climb stay there until the web is broken.
- Mob pathing does not avoid slow blocks (vanilla does not avoid webs either). Out of scope.
- Cost scales with the cells an entity overlaps (a horse covers 2x2); section 7 measures it.
- A projectile from a mod added later has no group tag and falls back to its base-class default; the scan tool flags
  it.

## Execution status

- Phase 0 done 2026-11-14: findings in briefs/passthrough_phase0.md
- Phase 1 done 2026-11-14 (Creative Compat 1.0.0, build/libs 2026-11-14, not installed; nothing changes in game until
  the phase 4 tags exist). Files in `development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/`:
  - new `surface/`: `PassTier` (11 tiers, defaults per section 2 + seventh pass, nested enums `Growth`, `Neighbours`,
    `Surround`, `ItemRule`, record `Values`), `PassRule` (one block: `cell(tier, values, neighbours)` -> `Cell`),
    `PassResult` (per-entity tick: lowest walk / fall with tier, dense flag and neighbour count each, `surrounded` +
    `surroundedCell`, `resetFall`, `projectiles`, the counted cells), `PassMath` (growth, modifier, wading, fall,
    attribute / FOV math, `stickPoint` for 4.2), `PassGrowth`, `PassThrough`, `PassState` + `PassHolder` (per-entity
    state, duck interface on `Entity`), `PassOwner` (SecurityCraft owner pass by reflection on `IOwnable`),
    `PassValues` (config snapshot, server copy on clients), `PassSync` + `PassSyncClient` (channel
    `creativecompat:surface`, message 0, login + save), `PassFov` + `BlockRunnerFov`.
  - mixins: `WallSlideMixin` renamed `InsideBlocksMixin` (TAIL `m_20101_`: WallSlide, then PassThrough; adds the
    `PassHolder` field), new `MakeStuckInBlockMixin` (HEAD `m_7601_`), client `PassFovMixin` (`m_108565_` STORE
    ordinal 2) and `BlockRunnerFovMixin` (gated on blockrunner). `CompatConfig.Surface.Passthrough`
    (`[surface.passthrough]` enabled, arrowLifeTicks 12000, arrowLongLifeCap 128, one table per tier with all 15
    keys incl. `itemStickChance`). Wiring in `CreativeCompat` (TagsUpdatedEvent -> `PassThrough.clearCache`,
    `PassValues::onConfig` on the mod bus, login send, client logout reset).
  - API for phases 2, 3, 5: `PassThrough.evaluate(entity, PassValues.of(level.isClientSide), result)`,
    `PassThrough.state(entity, create).result` (this tick's result after the hook), `cellAt(level, pos, state,
    values)` (one block, no entity: WTHIT), `tierOf`, `neighbours`, `grown`, `skipsEntity`, `exempt`, tag getters,
    `MODIFIER_UUID`; `PassValues.tier(t)`, `.arrowLifeTicks`, `.arrowLongLifeCap`; `PassMath.stickPoint`.
  - Deviations: (1) per-block growth `any` (tomato segments, coffee) is a block tag
    `creativecompat:passthrough_growth_any` for phase 4 to fill, not a config key; (2) a different same-tier block
    right below also makes a block "upper" under `tall_half` (rice panicles on rice); (3) neighbours count by tag
    only, no growth test; (4) fall distance: cells side by side on one layer are one block passed (lowest factor),
    each new layer multiplies; (5) wading drag assumes vanilla water keep 0.8 (0.9 sprinting), Depth Strider not
    modelled; (6) `minLayers` range 1-9 (9 = never) so phase 3 can carry `slowFromLayers = 9`; (7) movement effects
    only for living entities, but the stuck cancel covers every entity (items, orbs, bobbers, fireworks lose vanilla
    web slowdown once tagged: phase 2); (8) FOV: BlockRunner's `shouldRemoveBlockSpeedModifier` answers false except
    when `PassFov` asks, so only `PassFovMixin` recomputes (skips BlockRunner's UUID when BlockRunner would, ours
    always, not while scoping), handler order irrelevant; (9) spiders, foxes and bees exempt only by the entity tags
    (no Java fallback); (10) the `slowFromLayers` migration and its test stay in phase 3 (orchestrator split);
    `stickPoint` is written and tested now, unwired.
  - Tests: `tools/test_passthrough.sh` (headless, Bootstrap + fake corn / hemp / rice blocks): ALL 118 OK. Live
    checks queued: SQ-PTH-01 to -04, CQ-PTH-01, -02 (scratch_queue.md). No changelog bullet (no player-visible
    change until phase 4 tags).
- Phase 2 done 2026-11-15 (Creative Compat 1.0.0, build/libs 2026-11-15 00:17, not installed). Files in
  `development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/`:
  - new `surface/`: `PassItems` (4.2: path cells of `items = stick` tiers, one roll per tick, `PassMath.stickAlong`,
    NoGravity + held position, release when the cell is no longer a stick web), `PassProjectiles` (5.1/5.2: group
    lookup cached per entity type, web slowdown, break / settle / snag / linger / hang, pickarang and chakram pickup by
    touch and drop on web break, `otherEntity` for minecarts, boats, falling blocks, TNT), `PassGroup` (enum, tag paths,
    base-class defaults by class name), `PassArrows` (5.4: limit at `tickDespawn` HEAD, per-level per-chunk long-life
    sets, demotion of the highest `life`), `PassSave` (NBT key `CreativeCompatPass`: stuck item cell/point/old
    NoGravity, arrow `ShortLife`), duck interfaces `PassArrow`, `PassSnagItem`, `PassImpact`. Edited: `PassThrough`
    (non-living dispatch, `surroundedMotion`, surrounded fall reset, `leave` clears the result, `scratch()`,
    `clearCache` clears the group cache), `PassResult.surroundedTier`, `PassState` (item, snag, linger, break, arrow
    fields), `PassMath` (`clip`, `stickAlong`, `surroundedVertical`, `despawnLimit`, `oldestIndex`).
  - mixins: `EntityPassMixin` (Entity `m_20240_` RETURN save, `m_20258_` TAIL load, `m_6123_` HEAD touch),
    `LivingClimbMixin` (LivingEntity `m_21297_` RETURN, shadow `f_20899_`), `ArrowPassMixin` (AbstractArrow
    `m_6901_` HEAD, shadows `f_36697_` / `f_36703_` / `f_36696_` / `m_7941_`), `FireworkPassMixin` (`m_8119_` RETURN,
    shadow `m_37088_`), `ProjectilePassMixin` (shadow `m_6532_`; a duck class, since the annotation processor rejects an
    SRG `@Invoker`), gated in `CompatMixinPlugin`: `QuarkPickarangPassMixin`, `QuarkTorchArrowPickupMixin` (quark),
    `BotaniaChakramPassMixin` (botania). `CreativeCompat` registers `PassArrows::onLeave`
    (`EntityLeaveLevelEvent`) and `::onLevelUnload`.
  - Deviations: (1) base-class defaults also give the vanilla classes their table group (FishingHook `snag`, firework
    `linger`, thrown potion and XP bottle `settle`, llama spit and shulker bullet `break`) and match Quark
    `AbstractPickarang` / Botania `ThornChakramEntity` by name (`snag`), so vanilla works before the tags; (2) other
    non-living entities (minecarts, boats, falling blocks, primed TNT) get the `slow` treatment in projectile tiers
    (the stuck cancel took vanilla's web slowdown from them); (3) items react only to `items = stick` tiers; (4) item
    path = the entity's position point (an item whose box only grazes a web beside it does not stick); touching webs
    on the path are one run, the exit clamp is at the run's exit; a path over 8 blocks counts as a teleport; (5) a stuck
    item's position is held every tick, so stuck orbs do not home (collected on touch); the client keeps no stuck
    state, it zeroes motion in a stick web and holds still once the synced NoGravity arrives; (6) saved under the
    entity's own NBT key, not Forge persistent data; (7) `break` fires Forge's ProjectileImpactEvent first and
    discards on the server if the projectile's own `onHit` did not; (8) `hang` also sets `shakeTime` 7 and clears crit
    like a vanilla block hit, no hit sound; an arrow that later falls and lands runs its block hit then; (9) a `hang`
    tag on a non-arrow acts as `slow`; (10) snag, linger and settle let go when the web cell is gone; the snag state is
    not saved (a reloaded pickarang re-snags from the cell centre); the chakram's `tickCount` is held at 50 and a set
    return cleared; (11) arrow sets are lazy: join on the first despawn tick in a chunk, leave on
    `EntityLeaveLevelEvent`, stale members pruned when the set overflows; (12) climb and descend act through
    `handleOnClimbable` (land travel), so flying mobs only get the 0.35 walk; (13) torch arrow fix only matters with
    Quark `Extinguish On Miss` on (pack off); neon tube (empty pickup item) is unpickable: vanilla minute, not counted.
  - Tags phase 4 must fill (all `creativecompat:`). Block: `passthrough/cobweb` = `minecraft:cobweb`,
    `securitycraft:reinforced_cobweb`, `fantasyfurniture:decorations/spider_web_small`, `.../spider_web_wide`,
    `createaddition:barbed_wire`. Entity types: `passthrough_exempt/cobweb` = spider, cave spider;
    `passthrough_exempt/all` and `passthrough/projectile/exempt` = the 5.1 `exempt` row (required: IE revolver,
    railgun, chemthrower and sawblade shots extend AbstractArrow and would default to `hang`; Botania mana burst,
    babylon weapon and falling star are throwables and would default to `break`); `passthrough/projectile/break`,
    `settle`, `snag`, `linger`, `hang`, `slow` = their 5.1 rows (vanilla members optional; required where the base
    class gives another group: Create Enchantment Industry hyper experience bottle, Splash Milk, Botania ender air
    bottle -> `settle`; Botania throwable copy -> `slow`; Hearth thrown tools, horseshoe and the neon tube stay `hang`
    by default). The projectile scan tool should report every class whose default differs from its 5.1 row.
  - Tests: `tools/test_passthrough.sh` ALL 168 OK (50 new: multi-cell stick path incl. 2000 simulated falls all
    sticking, surrounded climb speeds, despawn limits and oldest pick, 21 group defaults, group tag ids). Live checks
    queued: SQ-PTH-05 to -15 (scratch_queue.md). Changelog: one bullet under `## Gameplay` (arrow despawn, the only
    change visible without the phase 4 tags); phase 7 moves it into `## Walking through plants and webs`.
- Phase 3 done 2026-11-15 (Creative Compat 1.0.0, build/libs 2026-11-15 00:27, not installed). Deleted
  `mixin/LeafPileSlowMixin`, `surface/LeafPileSlow`, their `creativecompat.mixins.json` and `CompatMixinPlugin` entries,
  `CompatConfig.LeafPiles` / `LEAF_PILES` and the `LeafPileSlow.set(...)` call. New `surface/LeafPileMigration` (plain
  text pass on the toml, called from the `CreativeCompat` constructor after the section carries, before
  `registerConfig`; logs the move). `[leaf_piles]` stays in `CompatConfig.SECTIONS` so a very old file is carried and
  then migrated. `configs_edited/root_config/creativecompat-common.toml`: `[leaf_piles]` dropped (value was 4 = the
  default; the live file is migrated by the jar on first boot, its value is also 4). Docs: creative_compat.md (leaf
  pile note, IW `leaf_piles_slow` still gates spruce thorns), mob_block_tweaks.md.
  - Ropes (3.2): phase 1 already covers it (rope tier fall 1.0, no density; `noWalkWhileClimbing` via `onClimbable`, so
    Quark rope, always a ladder, is covered once phase 4 tags it). No change.
  - Thorns: IW's thorn damage sits in its own `entityInside` branch (`layers > 3` again now the mixin is gone,
    `leaf_piles_slow`); `MakeStuckInBlockMixin` cancels only `makeStuckInBlock`, so thorns keep working (SQ-PTH-18).
  - Deviations: (1) the `LeafPileSlow.set` call was in Creative Compat's `CreativeCompat`, not Creative Engineering
    (surface/ moved 11-06); Engineering unchanged, not rebuilt; (2) which value wins: the old `slowFromLayers`, unless
    it is the old default 4 and `minLayers` already exists (a phase 1 jar edit is kept); an out-of-range or unreadable
    old value is dropped; with no pass-through table yet, the table is appended with `minLayers` only and Forge fills
    the rest on correction (tested).
- Phase 5 done 2026-11-15 (same build). New `surface/PassThroughFormat` (plain: `lines(cell, values, soil,
  hideNormal)` -> `Line(kind, value, stopped, dense, whileMoving, soil)`, `number`, `english`), `RunSpeed.soil`
  (`Soil(state, info)`), `[surface.wthit] passThrough` (default true), 7 lang keys `creativecompat.wthit.pass_through.*`
  in `assets/creativecompat/lang/en_us.json`. `WthitRunSpeed` asks pass-through first: a block in a non-inert tier gets
  Walk Through / Fall Through / Neighbors lines (WTHIT line ids `creativecompat:pass_walk`, `pass_fall`,
  `pass_neighbours`) or nothing, never the Run Speed line. `RunSpeed.format` now uses `PassThroughFormat.number`.
  `StuckInBlock` reduced to untiered blocks: powder snow, bamboo spikes, plus the stuck plants left outside the system
  (Floral Enchantment vine bushes 0.9, Farmer's Respite wither roots 0.8, Neapolitan beanstalk thorns 0.95, Upgrade
  Aquatic pickerelweed 0.6 / 0.75); the stale taiga grape bush entry and the IW `leaf_piles_slow` reflection are gone.
  - Deviations: (1) the soil is the first standable block below, looking past up to two more pass-through blocks (the
    upper half of a tall crop names the farmland); (2) the viewer's exemption covers the exempt tags, Depth Strider and
    the SecurityCraft owner; (3) an inert tier (grass by default) is treated as untiered (Run Speed path); (4) the soil
    tier name follows `showTier`; (5) the Neighbors line shows whatever `hideNormal` says; (6) Miner's Delight sticky
    basket, UA mulberry jam / guardian spine and CD powdered sugar (brief 1.2 "none") are not added to `StuckInBlock`.
  - Tests: `tools/test_passthrough.sh` ALL 203 OK (35 new: migration fixtures incl. Forge correction after an append,
    a copy of the live config run twice, spec keys; 15 format cases incl. cellAt on a web cube; StuckInBlock). Live
    checks queued: SQ-PTH-16 to -18, CQ-PTH-03, -04. No changelog bullet: nothing changes for players until the
    phase 4 tags (then leaf piles, bamboo leaves from 4 layers, and the WTHIT lines go in phase 7's family).
- Phase 4 done 2026-11-15 (tags and tools; no jar built or installed by this phase: the tags ship in the Creative
  Compat jar, so they take effect with the Compat owner's next `:creativeCompat:build` and the install).
  - Tags: new `kubejs/data/creativecompat/kubejs/tags/passthrough_tags.js` (Compat's own script tree, copied into its
    jar; Compat's tags have lived there since 11-01, and the `creativecompat:` namespace follows the owning mod),
    not `kubejs/server_scripts/tags/blocktags.js`. Block event: the eleven `creativecompat:passthrough/<tier>` tiers
    (specific tiers first, `crop`, `tall_plant` and `grass` take out every tier filled before them with `except`),
    `creativecompat:passthrough_growth_any` (FD, Supplementaries rope and stick, CD treated stick tomatoes; the five
    Farmer's Respite coffee blocks), `t.overlaps` over the eleven. Entity event: `passthrough_exempt/cobweb` (spider,
    cave spider), `/bush` and `/leaf_pile` (fox, bee), the seven `passthrough/projectile/<group>` tags (5.1 rows, vanilla
    included; `slow` empty), `passthrough_exempt/all` = `#...projectile/exempt`, `t.overlaps` over the groups. Ids with
    a dict key use it (`b.*`, `i.*`, about 50); entity ids are literals (there is no entity dict). Sizes: bush 8, cane
    5, cobweb 5, crop 41, dense_crop 11, grass 119, leaf_pile 37, rope 3, tall_plant 26, vine 35, wild_crop 30,
    growth_any 9; projectiles break 30, settle 5, snag 6, linger 1, hang 38, exempt 35.
  - `global.tags` needed no change (registry-agnostic; `entity_type` events already use it, Creative Worldgen
    `entitytags.js`), so no Creative Common edit or rebuild.
  - Tools: new `tools/projectile_scan.py` (class-file parser over mods/ + nested jars + the Mojmap jar; entity ids
    paired from registration bytecode, checked against jar lang keys and the dumps; tags from `/dumptags` or
    `--tags`; exit 3 on an untagged projectile or an id in two groups; writes `tools/projectile_scan.json`); new
    `tools/passthrough_tag_sim.js` (node: runs tag_helper + passthrough_tags.js on the dumps, reports unknown ids, dead
    patterns, overlaps, writes members for `--tiers` / `--tags`); `tools/blockrunner_scan.py --audit` gains the
    pass-through section (no-collision blocks with no tier, plant-like classes per mod, the rest counted; `--tiers`);
    `generators.json`: new `projectile_scan` (dumps `dumptags entity_type`), `blockrunner_audit` also dumps
    `dumptags block` and highlights the pass-through line.
  - Offline runs (dumps of 2026-11-15): sim: 0 unknown ids, 0 dead patterns, 0 overlaps (320 block ids, 115 entity
    ids). projectile_scan: 114 concrete projectile classes in 26 jars, all with an id (two read from source, not in
    any lang file: `creativecompat:shrapnel`, `hearthandharvest:manure_projectile`), 0 untagged, 0 doubled; 41 tags
    differ from the base-class default, all intended (35 exempt, enderiophage rocket, Mini Fizz and Blaze Ballon fire
    `break`, hyper experience bottle, Splash Milk and ender air bottle `settle`). Audit: 1647 no-collision blocks, 272
    plant-like ones untiered: Botania flowers (103) and UA / vanilla corals and kelp (124, underwater), the ruled-out
    stuck plants (Floral bushes 17, wither roots 2, beanstalk thorns, pickerelweed 2), mushrooms and fungus colonies
    (10), glow lichen, IW moss, and single ones: Delightful salmonberry bush, Fruits Delight cranberry bush, Firefly
    Bush, Vintage magic peanut, IE razor wire, oceanic sea grape plant, Twigs petrified lichen.
  - Not registered in this pack (Biomes O' Plenty only registers its flesh blocks and eyebulb): `hanging_cobweb(_strand)`,
    `spanish_moss`, `glowworm_silk`, BOP double plants; the vine row keeps a regex for them (matches flesh tendons
    today), the BOP tall plant row was dropped. Not projectiles (abstract, never registered): Mysterious Mountain Lib
    `EntityBullet`, Botania `ThrowableCopyEntity` (so the `exempt` and `slow` rows lose them). Plan 5.1's "25" Nock
    Enough Arrows arrows are 26.
  - Deviations: (1) file location above; (2) `crop` adds planted crops outside `#minecraft:crops`: nether wart,
    vanilla attached stems, FR tea bush and small tea bush, Delightful cantaloupe, Hearth sunflower stem, Neapolitan
    strawberry bush, mint and adzuki sprouts, Fruits Delight pineapple; (3) Hearth `hops_vine` is a `VineBlock`: `vine`,
    not `crop`; (4) `wild_crop` includes FR `wild_coffee_bush` (a `WitherRootsBlock`: its own 0.8 stuck slowdown is
    replaced by 0.75); (5) `grass` (1.00) = short grass, fern, dead bush, roots, sprouts, IW frosty grass and weeds,
    UA beachgrass, FD sandy shrub, `#minecraft:small_flowers`, `#minecraft:saplings` (minus azaleas, which collide),
    flower patches, Miner's gossypium, minus the ruled-out stuck plants; (6) `tall_plant` = `#minecraft:tall_flowers` +
    tall grass, large fern, small dripleaf, eyebulb, UA tall beachgrass (tall pickerelweed out); (7) leaf piles by regex
    (every IW `LeafPileBlock`, the azalea flower pile included; UA's Blueprint river leaf pile out); (8) Twigs bamboo
    leaves lose their own immune list (ocelot, panda, parrot now slowed; bees and foxes exempt as planned); (9)
    Supplementaries wild flax left out with flax; (10) Blocky Siege and Nock Enough Arrows listed by id, not `@mod`.
  - Live checks queued: SQ-PTH-19 to -24 (scratch_queue.md). Changelog: no bullet in this phase (the tags make the
    whole system player-visible; phase 7 writes the `## Walking through plants and webs` family).
  - Owner questions: Delightful salmonberry bush and Fruits Delight cranberry bush in `bush` or `crop` (untiered now)?
    Supplementaries wild flax in `wild_crop`? IE razor wire in `cobweb` like barbed wire? Botania flowers and
    mushroom colonies in `grass` (1.00, tunable only)?
- Phase 6 done 2026-11-15 (Creative Compat 1.0.0 and Creative Devtools 1.0.0, build/libs 2026-11-15 00:40, not
  installed). This is the first Compat jar with `data/creativecompat/kubejs/tags/passthrough_tags.js` inside (checked
  with `unzip -l`). Headless `tools/test_passthrough.sh` ALL 203 OK after the edits.
  - Compat: new `surface/PassTiming` (`[debug.passthrough] timing`, new `CompatConfig.Debug` / `DEBUG`, default false):
    `InsideBlocksMixin` times `PassThrough.afterInsideBlocks` while on; `PassThrough.neighbours` counts; server and
    client tick START/END listeners (client one registered on `Dist.CLIENT` only) log every 200 ticks
    `[passthrough-timing] side=<server|client> ticks calls total_us max_us inside inside_peak counts tick_us`.
    `PassItems.pathStop(...)` split out of `afterInsideBlocks` (no behaviour change; the bench calls it).
    `PassValues.refresh()` (rebuild the snapshot and re-send now).
  - Devtools: new `perf/PassBenchCommand` (registered in `CreativeDevtools` when `creativecompat` is loaded):
    `/cdevbench passthrough [calls]` (op 3, default 1,000,000, 20 batches, warmup max(10000, calls/5)), 10 cases
    (`ring_count`, `ring_count_edge`, `cube_count`, `tick_air`, `tick_lone_crop`, `tick_field`, `tick_sparse`,
    `tick_web_cube`, `tick_horse_field`, `item_roll`), median / p99 / min / mean ns, `enabled` and `tagged` flags, to
    chat (field and web-cube medians green or red against 1000 ns) and `kubejs/exported/perf/passthrough_bench.json`;
    fixture blocks restored. `/cdevbench passthrough enabled [<bool>]` shows or sets the master switch (set, save,
    `PassValues.refresh`).
  - Perf suite plan `tools/perf/suite/plans/passthrough.py` (group P, PT00 to PT06 a/b, PT99; perf_suite.md 5.1);
    commands checked offline against `rcon.MAX_COMMAND` (longest 151). Not run.
  - Parser `tools/perf/passthrough_timing.py` (per side: share of tick time overall and worst window, ms per tick,
    mean and longest call, inside per tick and peak, counts per tick; `--json`); tested on a synthetic log.
  - Deviations: (1) fixtures go 12 blocks under the build limit above the caller, inside loaded chunks (a far-off area
    would need a chunk load and ticket on the server thread); the command refuses if the 57x21 area is not loaded;
    (2) fixture mobs are created but never added to the level; (3) the item case times the real `pathStop` (cells,
    roll, clip) without sticking the item, so every call is the same first-tick case; (4) suite: the switch is flipped
    with the new `enabled` subcommand on twin sites rather than two runs; the bot walks a 16x96 corn strip in a line
    (one `pbot path` target) instead of circles; arrows come from five repeating command blocks
    (`enable-command-block` in the group props) into a 9x9x9 web block (a 9x9x5 wall has too few surrounded cells);
    villagers get composters as job sites; (5) new parser file instead of extending `log_timings.py` (that one reads
    startup and join times only); (6) the timing `inside` counts entity-ticks (and stuck items), with the per-tick
    peak, rather than distinct entities.
  - Live checks queued: SQ-PTH-25 to -28, CQ-PTH-05. Also fixed the `qrun.py toml` syntax (SECTION KEY, not dotted) in
    SQ-PTH-13, -14, -16. No numbering collisions in the SQ-PTH / CQ-PTH rows (28 rows, all distinct).
- Phase 7 done 2026-11-15. blockrunner.md: new 3.10 (model, tiers with seventh-pass values and tag sizes, ropes,
  cobwebs, items, projectiles, arrows, field of view, WTHIT, performance, collected deviations, owner questions),
  section 4 pass-through limits (and the Twigs bamboo leaves and leaf pile decision lines updated), file map, BR-67 to
  BR-88 (plan 9 updated for the rulings: non-Loyalty tridents now despawn at 10 minutes, Twigs bamboo leaves at 4
  layers and ocelots slowed, taiga grape bush), at-a-glance row, history. creative_compat.md: pass-through config
  table and history line; mob_block_tweaks.md MT-25 points to BR-83 / BR-87; creative_devtools.md `PassBenchCommand`
  row; perf_suite.md 5.1 and file map; README blockrunner row and TODO `gameplay/blockrunner #10` (Testing; built,
  scratch test queued, untested).
  - Changelog: new family `## Walking through plants and webs` in `unreleased.md` (20 bullets: crops, dense crops, wild
    and tall plants, canes, vines, ropes, three cobweb bullets, taiga grape bush, Twigs bamboo leaves 4 layers [3] and
    0.45x [about 0.35x] with ocelots, pandas and parrots slowed, mobs, items in webs, four projectile bullets, field of
    view, WTHIT lines); the 10-minute arrow bullet moved in from `## Gameplay`. `changelog_lint.py`: 0 errors, no
    warning in `unreleased.md`.
  - Deviations: (1) no Twigs "bamboo leaves no longer spare" wording on foxes and bees (they stay exempt, as before);
    (2) the field of view bullet states the fact for the new slowdown only (BlockRunner's own FOV handling was already
    off in the pack); (3) BR-88 holds the owner's real-play timing check (plan 7.3).
  - Open for the owner (from phase 4, repeated in blockrunner.md 3.10): salmonberry and cranberry bush tier, wild flax,
    IE razor wire as barbed wire, Botania flowers and mushroom colonies in `grass`.
- Owner follow-up 2026-11-15 (orchestrator): salmonberry and cranberry bushes in `bush`; IE razor wire in `cobweb`;
  Botania mystical, shiny, special flowers and mushrooms in `grass`; wild flax stays out; sticky basket, mulberry jam,
  guardian spine and powdered sugar added to `StuckInBlock` (WTHIT line only). Also Compat `VineShearing` leaves the
  Hearth hops vine to its own shears handling (scratch run r1115, SQ-HOP-05). Tag sim clean (bush 10, cobweb 6, grass
  203); Compat rebuilt 11:31, harness 203 OK. No open owner questions.
- Scratch fixes 2026-11-15 (after scratch run r1115 phase 11; Creative Compat 1.0.0 and Creative Devtools 1.0.0,
  build/libs 2026-11-15 14:39 (Compat rebuilt 14:59 for the bush ruling), not installed; both builds include another session's uncommitted edits in those mods,
  e.g. Compat `mixin/IPExitLiftMixin`, Devtools `cdevplace/CatalogueRules`).
  - SQ-PTH-03 (lone web zeroed the fall distance): not our code. Vanilla `Entity#move` clips any move of a block or
    more per tick against `#minecraft:fall_damage_resetting` (cobweb, sweet berry bush, climbables) and water and
    resets the fall distance on a hit; a 20-block fall crosses the web at about 1.5 b/t. Fix: `InsideBlocksMixin`
    redirects that `Level#clip` (`m_45547_` in `m_6478_`, `require = 0`) to `PassThrough.fallResetClip`, which redoes
    the clip leaving out blocks whose tier scales the fall (fall below 1.0, not dense with `denseResetsFall`), unless
    climbable, exempt or the SecurityCraft owner's. Owner ruling 2026-11-15 ("no reset"): bush-tier blocks (sweet
    berry included) are left out too, so bushes never cancel fall damage (rebuilt 14:59, changelog bullet added).
    Climbables, water and dense webs keep vanilla's reset.
  - SQ-PTH-11 (owner ruling 2026-11-15): a `break` projectile slower than 0.05 b/t (after the tick's web slowdown) in
    any web breaks at that web cell's centre (`PassProjectiles.BREAK_STALL_SPEED`, constant: a config key would need a
    sync packet field). Other groups unchanged.
  - Performance (SQ-PTH-25, -28; plan 7.2 fallback): JFR of pt2 showed `tierOf`'s ConcurrentHashMap lookup as a third
    (PT01) to half (PT03) of the hook, and PT04's cost as the held items' own zero-length `Entity#move` (entity
    collision queries), not the hook. New `PassTierSlot` + mixin `BlockStatePassMixin` (tier slot on every block
    state, generation bumped by `clearCache`), `PassCountCache` (per-thread per-tick counts by position and tier),
    `PassThrough.countRegion` (all of an entity's missing counts from one block read), exempt once per tier per
    entity, full-block outlines skip the box walk; `PassItems.heldMove` at HEAD of `Entity#move` (in
    `InsideBlocksMixin`) skips a held item's or orb's move and only rereads its cell (full web test only when the
    state or config snapshot changed). Devtools bench: 12 cases (`tick_field`, `tick_web_cube`, `tick_horse_field`
    empty the count cache per call = first entity in its cells; new `tick_field_shared`, `tick_web_cube_shared`).
    Estimates, not measured in game: web cube about 0.8 to 1.0 us cold, field about 0.4 us, horse about 1.2 us; PT01
    about +0.1 to +0.25 ms, PT04 about 0 or below. `[debug.passthrough] timing` no longer sees held items (their
    move, and so the hook, is skipped).
  - CQ-PTH-03 (rope 0.69x): not a fault. Vanilla `LivingEntity#handleOnClimbable` caps horizontal speed at 0.15 b/t
    on every climbable block; 0.75 x 0.216 = 0.162 is above the cap, so the cap wins (vines show the same 0.69x).
  - Tests: `tools/test_passthrough.sh` ALL 226 OK (23 new in `PassFixesTest`: vanilla clip repro and the fixed clip on
    a lone web, 3 stacked webs, dense cube, sweet berry bush (vanilla hit, fixed miss), a ladder behind a web and a
    bush; fall distance 14.55 -> 12.37; stall
    threshold and the SQ-PTH-11 snowball breaking on its 4th web tick; region counts = per-cell counts over 400 random
    worlds, block reads 36 vs 52 and 16 vs 32; count cache ticks, levels, tiers, 3000 cells). Changelog: the break
    bullet of `## Walking through plants and webs` now covers stopping in any web (lint clean). Reruns queued:
    SQ-PTH-03b, -06b, -11c, -25b, -28b, CQ-PTH-03b.
- 2026-11-15 15:01: the phase 12 reruns (SQ-PTH-03b, -06b, -11c, -25b, -28b, CQ-PTH-03b) were stopped by the owner
  before the first boot; none ran, all stay pending in scratch_queue.md. Resume point: scratch_run_2026-11-15.md
  section 7 "Phase 12" (jar copies in perf/runs/r1115/pth_jars2/: Compat 14:59, Devtools 14:39). The fixes for the
  phase 11 FAILs are built and headless-tested (226 OK) but not yet verified in game.
- 2026-11-15 rework (plan_post_scratch_1115b.md A to E; Creative Compat 1.0.0, build/libs 2026-11-15 15:42, not
  installed; carries another session's uncommitted Compat edits, e.g. `mixin/IPExitLiftMixin`). Neighbour model
  sides 4 / faces 6, dense from 3, surrounded at 6 (`surroundedAt`); counts baked into the chunk (`PassCounts`,
  `PassCountData`, `mixin/LevelChunkPassMixin`, chunk capability `creativecompat:pass_counts`); layer tier `layers`
  (tag `creativecompat:passthrough_layers`, 2.1) replaces `leaf_pile`; `breakStallTicks` (5.1); sync message 1, format 2;
  `PassConfigMigration` (RING / CUBE, denseFrom 6, leaf_pile -> layers); `CompatConfigReload` for `/cdevreload compat`
  (E). BlockRunner `snow` tier without snow layers (blocktags.js). Tests: `tools/test_passthrough.sh` ALL 292 OK (new
  `PassRework1115bTest`). Live checks: SQ-PS15B rows in scratch_queue.md. Open owner questions: section 10.
