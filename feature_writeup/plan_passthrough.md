# Plan: pass-through blocks (walk-through and fall-through speed)

Status: Planned, all decisions settled (2026-11-14; revised six times the same day with the owner's rulings). TODO
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
| `neighbours` | `none`, `ring` (8 around, same layer) or `cube` (26 around, 3x3x3) | enum |
| `denseFrom` | neighbour count at which the dense values apply | 1-26 |
| `denseWalk`, `denseFall` | values once dense | as above |
| `denseResetsFall` | fall damage resets every tick while dense | bool |
| `surrounded` | what a full count does: `none` or `web` (section 4.3) | enum |
| `minLayers` | blocks with a `layers` property (leaf piles, Twigs bamboo leaves): below this, ignored | 1-8 |
| `walkOnlyMoving` | walk slowdown only while moving horizontally through the block's outline (vines) | bool |
| `items` | `none` or `stick` (section 4.2) | enum |
| `projectiles` | projectiles are slowed (section 5) | bool |
| `depthStriderPasses` | Depth Strider boots ignore the tier | bool |

Plus `[surface.passthrough] enabled` (master switch, also used by the benchmark).

**Neighbours** are blocks in the **same tier tag** (rice and rice panicles count for each other; cobweb, reinforced
cobweb and barbed wire count for each other). Solid blocks never count. Ring: a field edge has 5, a corner 3, the
inside 8, so 6+ means "inside the field or one row in". Cube: only a web with webs on all sides, above and below,
reaches 26.

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
| `dense_crop` | corn, IE hemp, FD rice + rice panicles, Vintage Delight oats, wheat, tomatoes (FD tomatoes and budding tomatoes, Supplementaries rope and stick tomatoes, Creative Delight treated stick tomatoes) | 0.75 | 1.00 | `tall_half` (wheat: `half`) | ring | 6 | 0.50 / 1.00 | rice base is in water: wading drag |
| `crop` | every other planted crop (`#minecraft:crops` and modded crop blocks minus `dense_crop`), Farmer's Respite coffee stem | 0.75 | 1.00 | `half` (coffee: `tall_half`) | none | | | |
| `wild_crop` | Farmer's Delight wild crops, wild corn, wild rice, wild oats, other mods' wild crops | 0.75 | 1.00 | `any` | none | | | |
| `vine` | every vine: vanilla vines, twisting and weeping vines (plant and tip), cave vines (glow berries), Vinery grape vines, and other mods' vines (`VineBlock` and `GrowingPlantBlock` subclasses plus named ones; phase 0 list: Floral Enchantment cornflower vine, Biomes O' Plenty willow vines and spanish moss, Quark, Upgrade Aquatic, others) | 0.75 | 1.00 | `any` | none | | | `walkOnlyMoving`; climbing stays as the block has it |
| `tall_plant` | tall grass, large fern, BOP eyebulb, sunflower, lilac, rose bush, peony, Botania double flowers, other mods' 2-tall `DoublePlantBlock`s | 0.75 | 1.00 | `any` | none | | | |
| `cane` | sugar cane, propelplant cane (Nether's Delight, Supplementaries) | 0.50 | 1.00 | `any` | none | | | moves propelplant out of `bush` |
| `rope` | Farmer's Delight rope, Supplementaries rope, Beautify twine | 0.75 | 1.00 | `any` | none | | | ladder rules unchanged (3.2) |
| `cobweb` | `WebBlock` and subclasses (cobweb, SecurityCraft reinforced cobweb, Fantasy Furniture spider webs), Create Crafts & Additions barbed wire | 0.50 | 0.85 | `any` | cube | 6 | 0.35 / 0.20 | `denseResetsFall`, `surrounded = web`, `items = stick`, `projectiles`, spiders exempt |
| `bush` | sweet berry bush, Vinery taiga grape bush | 0.36 | 1.00 | `any` | none | | | today's real speed; foxes and bees exempt |
| `leaf_pile` | IW leaf piles, Twigs bamboo leaves | 0.45 | 1.00 | `any` | none | | | `minLayers = 4` (today's `slowFromLayers`), `depthStriderPasses`, foxes and bees exempt |
| `grass` | short grass, fern, other short plants | 1.00 | 1.00 | `any` | none | | | listed so it can be tuned; 1.00 = no effect |

Out: Biomes O' Plenty barley, cattail and reed.

## 3. Leaf piles and ropes

### 3.1 Leaf piles move here
- Delete `LeafPileSlowMixin` and `surface/LeafPileSlow`; remove its `creativecompat.mixins.json` entry and the
  `LeafPileSlow.set(...)` call in Creative Engineering (creative_engineering.md 2.2 and creative_compat.md note it).
- `[leaf_piles] slowFromLayers` becomes `[surface.passthrough.leaf_pile] minLayers`. On first load, carry over the
  live value (read the old key if present), then drop the old section. Update `configs_edited/`.
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
- Up to 5 of 26 neighbours: walk 0.50, fall 0.85 (a small vertical drag; fall distance x0.85 per web passed).
- 6 or more: walk 0.35, fall 0.20, fall damage reset every tick.
- 26 of 26: section 4.3.

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

### 4.3 Surrounded (26 of 26)
- Any web cell the entity overlaps has 26 web neighbours: falling stops (`dy < 0` becomes 0, fall distance 0).
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
| `break` | in a surrounded web, breaks **in the middle** of the web block: the impact is run as a block hit at the cell centre, as if the web were a full block | snowball, fireballs (small, large, dragon), wither skull, llama spit, shulker bullet, ender pearl; eggs (vanilla, Untitled Duck Mod, Alex's Mobs emu and cockroach, Quark parrot); Twigs pebble; Supplementaries bomb, thrown brick, slingshot shot; Alex's Mobs tossed item and enderiophage rocket; Botania vine ball, magic missile; PneumaticCraft micromissile and vortex; Immersive Petroleum molotov; Creative Compat shrapnel; Farmer's Delight rotten tomato; Hearth and Harvest manure; Canned Goods Mini Fizz; Create Misc and Things Blaze Ballon fire |
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
