# items / item-physics: implementation brief

Written 2026-10-21 from [TODO](../todo/TODO.md) `# items`, Sub `item-physics` (row 5 added 2026-10-24; rows 6 and 7 and the
2026-10-31 sweep: #3 and #4 built, #2 re-based on the family design). Feature writeup:
[item_physics.md](../item_physics.md) (test prefix `IP-`, used: IP-01 to IP-15, IP-21; new tests here are placeholders
`IP-newN`, numbered when queued). Related: [tnt_shrapnel.md](../tnt_shrapnel.md) (burning TNT items),
[fixes_compat.md](../fixes_compat.md) 2.32 (falling block splash).

Jars checked (decompiled with vineflower or read with `javap`, under `scratchpad/decomp/` and `scratchpad/vanilla/`):
`ItemPhysic_FORGE_v1.6.7_mc1.19.2.jar` (`ItemEntityMixin`, `ItemPhysicServer`, `CommonPhysic`),
`allurement-1.19.2-3.2.1.jar`, `create-1.19.2-0.5.1.i.jar`, `alexsmobs-1.21.1.jar` (`EffectLavaVision`, read
2026-10-28 for item_physics.md 10.4), the vanilla 1.19.2 client jar with Mojang's mappings and MCP `joined.tsrg` for
SRG names. "Verified" means read from one of them.

## Scope and order

| Row | Change | Status | Size |
|---|---|---|---|
| #2 | Classify modded items (families) | Planned | medium: rerun the audit on the 10-30 dump, settle 9.6, then a generator mode writes `physics/family/*` tags; KubeJS maps families to behaviour |
| #6 | Light items skim on water; lavaproof items float on lava | Planned | small-medium: one `Buoyancy` query, extend the existing `updatePre` TAIL inject, a new tag, a config section |
| #5 | Golden egg gets gravity | Planned | tiny once reproduced: one tag line; folds into #2 (egg -> `organic` family) |
| #7 | Player speed in lava; Lava Vision speed | Planned | investigation, then a small `LivingEntity#travel` mixin |
| #4 | Water splash from falling blocks | built 2026-10-28 (TODO still says Planned) | see Built, needs play |
| #1, #3 | Fire / lava split; Allurement curses | Testing | see Built, needs play |

Build order:

1. **#2 first**: it decides which items skim (foliage, fibre, light_powder) and which float on lava (refractory), so #6
   reads its tags. #5 rides along (one manual override or the `organic` family).
2. **#6** in one patcherMod build; **#7** can share that build if its investigation is done by then.

Related rows: `farming/composting` ([farming_composting.md](farming_composting.md): `#creativeengineering:composters` in
the floats list), `baking/baked-goods` ([baking_baked-goods.md](baking_baked-goods.md): sugar may move to floats),
`gameplay/tnt-and-portal #1` (burning TNT items, shrapnel).

---

## item-physics #2: Classify modded items
Status **Planned** / Work KubeJS / tools / TODO `items/item-physics #2` (item_physics.md 9, F4)

### Goal
Every item gets exactly one of the 21 material families of [item_physics.md](../item_physics.md) 9.2, and the family
fixes its water and fire behaviour through the existing four physics tags. Nothing falls to the default by accident:
89% of items (17,650 of 19,743 in the tag-only run) are in no physics tag today and sink and burn.

### Now
- **Audit built 2026-10-28, report only:** `/dumpphysics`
  ([kubejs/server_scripts/_lib/dump_physics.js](../../kubejs/server_scripts/_lib/dump_physics.js)) and
  [tools/item_physics_audit.py](../../tools/item_physics_audit.py) (568 lines, generator `item_physics_audit` in
  [tools/generators.json](../../tools/generators.json) 466-471, "report only") write
  [item_physics_audit.md](../item_physics_audit.md) and `kubejs/generation_scripts/physics/item_physics_families.json`.
- **The dump has been run since** (checked 2026-10-31): `kubejs/exported/physics/item_physics.json`, dated
  2026-10-30 00:41, `total 19547`, `skipped 0`, `lavaSurvivors 1410`. The audit was **not** rerun on it: the report
  (2026-10-28 20:26) still says "not dumped yet", so the material rung (block Material and SoundType) is unused and the
  650 unclassified are the tag-only figure. SQ-IP-16 has no run-log entry.
- The tag script, [item_physics_tags.js](../../kubejs/server_scripts/tags/item_physics_tags.js) (400 lines):
  `physicsFromTag` (50, a private copy of `global.tags.add` that calls `event.add` itself, F6); the user's notes at
  91-95 ("Adjustments: Totem sinks, crossbow sinks, armor stand sinks, shields sink, trident is fireproof" and the
  "Not done yet" Create / flour / treated-wood list). Today `armor_stand` (127), `crossbow` (131) and
  `totem_of_undying` (170) and `shield` (132) are in **floats**, the opposite of the note; `trident` is in no tag.
- No `physics/family/*` tag exists (`kubejs/data/creativeengineering/tags/items/physics/` is absent).

### Build
1. **Rerun the audit** on the existing dump (`/cegen run item_physics_audit`, or `python3 tools/item_physics_audit.py`)
   and check the IP-21 / SQ-IP-16 pass line: header "dump yes", unclassified below 650, `minecraft:netherite_scrap`
   under `fire_resistant_item`. If the dump is stale against `mods/` (new jars since 10-30), `/dumpphysics` again first.
2. **Settle 9.6 with the user** (nether wood fireproof, ice floats, machines fireproof, skim, stone scope) and add the
   user's own adjustments from lines 91-92 as manual overrides (totem, crossbow, armor stand, shield sink; trident
   fireproof). Record each in item_physics.md 9.6.
3. **Generator mode** in `item_physics_audit.py` (`--write-tags`, a second `generators.json` entry or a changed
   `apply`): one JSON item tag per family, `creativeengineering:physics/family/<family>`, under
   `kubejs/data/creativeengineering/tags/items/physics/family/`; reads the manual-override list so every item is in
   exactly one family tag. Write ids, not nested tags, so a reload never depends on tag order.
4. **Map families to behaviour** in `item_physics_tags.js` through `global.tags` only:
   `t.add(e, PHYSICS_TAG.floats, ['#creativeengineering:physics/family/wood', ...])`, sinks / fireproof / lavaproof the
   same way (table 9.2); skim families go to the new `physics/skims` tag of #6 (and to floats until #6 ships). Replace
   `physicsFromTag` (F6) and shrink the hand lists to the override list.
5. Update item_physics.md 0, 2.2, 9.4 (new figures), 9.5 (built), F4.

### Ids and data
- Tags: `creativeengineering:physics/{floats,sinks,fireproof,lavaproof}` (ItemPhysicSplit 49-52, verified), new
  `creativeengineering:physics/family/<21 names>`.
- Dict keys for the overrides (item_dict, verified 2026-10-31): `i.armor_stand` 2109, `i.crossbow` 2249, `i.shield`
  2760, `i.totem_of_undying` 2821, `i.trident` 2824.

### Decisions
- The five in 9.6: recommendations stand (yes, yes, fireproof, skim for foliage / fibre / light powder, keep stone).
- **Ids or nested tags in the family files:** recommend ids (generated, so size is not a cost; no ordering risk).
- **Ship families before #6?** Recommend yes: skim families simply float until #6 lands.

### Gotchas
- Rhino: no spread in array literals; build lists with `.concat()` (the file notes it).
- `global.tags.addMatching`'s `except` expands `#tag` entries into ids; keep live `#tags` in a separate `t.add`.
- Tag membership is live on `/reload`; the burn-transform API (`global.itemPhysics`) is startup-only.
- `sinks` beats `floats`, `lavaproof` beats `fireproof` (item_physics.md 2.1): an override must remove an item from the
  losing tag's source, not only add it to the winner.

### Tests
- IP-21 (exists; SQ-IP-16): dump + audit pass line.
- **IP-new1 Families applied:** after the generator, drop one item per family (oak log, iron ingot, AE2 drive, brick,
  sand, amethyst, glass, potion, redstone, ice, crimson planks, carrot, bread, bone, oak leaves, wool, flour) into water,
  a campfire and lava; each matches table 9.2.
- **IP-new2 Adjustments:** totem, crossbow, armor stand, shield sink; a trident survives a campfire.

---

## item-physics #6: Light items skim on water; lavaproof items float on lava
Status **Planned** / Work patcherMod / TODO `items/item-physics #6` (item_physics.md 10)

### Goal
Light items (foliage, fibre, light powder) land on water and stay on the surface (dip about 0.1 block, no bob); the
other floaters settle without bobbing forever; items that survive lava float on top of it instead of sinking.

### Now
- ItemPhysic's floaters dive 2.8 blocks from a 3-block drop and bob 0.35 for good (no vertical drag in fluids); in lava
  every item sinks at -0.0033 a tick and floating is never applied (item_physics.md 10.1, F8, F9).
- [ItemPhysicServerMixin.java](../../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/ItemPhysicServerMixin.java)
  already has a TAIL inject on `updatePre` (53-56) that calls `AllurementItemCurses.tick(item)`; the redirects at
  58-79 route ItemPhysic's lists through `ItemPhysicSplit`.
- [ItemPhysicSplit.java](../../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/itemphysic/ItemPhysicSplit.java):
  tag keys 48-52, `forcedSink` 154, `isFloat` 159, `touchingLava` 179. No buoyancy concept beyond float / sink.
- No `[itemphysic]` section in CEConfig (only a label at CEConfig.java 3417) or in the toml.
- Simulated constants (scratchpad `float_sim.py`, 10.2): skim drag 0.45 / k 0.12 / rest 0.05 with entry look-ahead;
  damped float 0.75 / 0.06 / 0.15; lava float 0.35 / 0.10 / 0.08.

### Build
1. `ItemPhysicSplit`: `SKIMS = itemTag(TAG_NS, "physics/skims")` and
   `enum Buoyancy { SKIM, FLOAT, SINK }`, `static Buoyancy water(ItemStack)` (sinks wins, then skims, then `isFloat`),
   `static boolean floatsOnLava(ItemStack)` (`LAVAPROOF`).
2. `ItemPhysicServerMixin`: extend the TAIL inject (keep the Allurement call first). Read the fluid from
   `ItemPhysicServer.fluid` (public ThreadLocal set at the start of `updatePre`, per 10.3; re-check the field name in the
   jar). In water: SKIM or (config) damped FLOAT apply `vy = vy * drag + clamp((restY - y) * k, +-0.06)`, `restY` from
   the top fluid block's height (walk up at most 4 blocks). In lava with `floatsOnLava`: the lava spring.
   Out of fluid: entry look-ahead when `CommonPhysic.getFluid(item, true)` says the block below is fluid and this tick's
   fall passes the surface: clamp `vy` so the item lands on it.
3. CEConfig `[itemphysic]`: `skimDrag`, `skimSpring`, `skimRestDepth`, `dampedFloat` (bool, default true) with its three,
   `lavaFloat` (bool) with its three; cached `DoubleValue`s (read every item tick). Run the jar once, then copy the toml
   to `configs_edited/root_config/creativeengineering-common.toml`.
4. KubeJS: fill `physics/skims` from the foliage / fibre / light_powder families (#2) or, if #2 is not in yet, from the
   leaves / flowers / wool / paper / feathers lines of the floats list, through `global.tags`.
5. Writeup: item_physics.md 10 (built), 2.1 (third water behaviour), 5 (file map).

### Ids and data
- New tag `creativeengineering:physics/skims` (item). Existing `physics/lavaproof`.
- ItemPhysic members (from item_physics.md 10.3, verified there 2026-10-28): `ItemPhysicServer.fluid`,
  `CommonPhysic.getFluid(ItemEntity, boolean)`.

### Decisions
- **Damped float for the other floaters:** recommend on (it ends the endless bob; heavier floaters still dip and rise).
- **Lava float for every lavaproof item:** recommend yes (lava is dense; the refractory family is the lavaproof set).
- **Skim on currents:** keep ItemPhysic's 0.3 inflate in `updateFluidHeightAndDoFluidPushing`, so currents still push
  skimmers (10.3). Recommend keep.

### Gotchas
- The inject runs on both sides (item ticks are simulated on the client and tags are synced): keep it deterministic,
  no randomness, or the client and server positions drift until the next sync.
- `updatePre` may already have discarded the item (lava / fire `hurt`): return when `item.isRemoved()`.
- `sinks` must still win: a sinking item never gets the spring.
- Waterlogged blocks and bubble columns report water: test an item on a waterlogged slab and in a column.

### Tests
- **IP-new3 Skim:** drop oak leaves, wool and flour from 3 blocks into still water: each stops near the surface
  (no visible dive), no bob after 5 s; a current carries them.
- **IP-new4 Damped float:** oak planks from 3 blocks: one dip under a block deep, then still.
- **IP-new5 Lava float:** cobblestone and a netherite ingot into a lava lake: they float on top and survive; an iron
  ingot sinks and burns.
- **IP-new6 Sinks unchanged:** gunpowder and a cogwheel into water sink as before.

---

## item-physics #7: Player speed in lava; Lava Vision gives water-like lava speed
Status **Planned** / Work patcherMod / TODO `items/item-physics #7` (item_physics.md 10.4)

### Goal
Investigate, then possibly: players move slower in lava than today, and Lava Vision (`alexsmobs:lava_vision`) lets them
move through lava about as fast as through water.

### Now
- Lava Vision only changes fog: `EffectLavaVision`'s effect tick is empty (read 2026-10-28, item_physics.md 10.4).
- The full netherite set gives Fire Resistance, Lava Vision and Knockback Resistance through
  [armor_effects.js](../../kubejs/startup_scripts/func/armor_effects.js) 227-234 (verified 2026-10-31). Lava Vision
  potions brew from a lava bottle + bone serpent tooth ([lava_vision.js](../../kubejs/server_scripts/magic/lava_vision.js),
  [potions.js](../../kubejs/startup_scripts/cooking/potions.js) 126).
- Vanilla `LivingEntity#travel` (unverified this session): in lava `moveRelative(0.02)` then the motion is scaled by
  0.5 (0.5 / 0.8 / 0.5 when shallow); in water by 0.8 (0.9 sprinting) plus Depth Strider and Forge's `SWIM_SPEED`
  attribute on the water branch. Whether Forge 1.19.2 also applies `SWIM_SPEED` in lava, or routes lava through
  `FluidType#move`, is unverified.
- Nothing in patcherMod touches player fluid movement (grep `SWIM_SPEED`, `isInLava` in mixins: none).

### Build
1. **Read** the Forge-patched `LivingEntity#travel` (SRG `m_7023_`) in the 1.19.2 Forge jar: the lava branch, whether
   `SWIM_SPEED` or `FluidType.move` covers it. Write the finding into item_physics.md 10.4.
2. **Lava Vision speed:** a common mixin on `LivingEntity#travel`, `@ModifyArg` on the lava branch's `moveRelative`
   speed and `@ModifyConstant` on its 0.5 drag: when the entity has `alexsmobs:lava_vision` (resolved once from
   `ForgeRegistries.MOB_EFFECTS`, null when AM is absent), use the water values (0.02 * swim speed, drag 0.8).
   If Forge's `FluidType` path applies, hook `ForgeMod.LAVA_TYPE` behaviour there instead.
3. **Slower lava without it (optional, per decision):** scale the lava drag by a config factor `[lava_movement]
   playerDrag` (default 0.5 = vanilla), players only.
4. Config switch `[lava_movement] lavaVisionSwimming = true`; writeup section 10.4 -> built.

### Ids and data
- Effect `alexsmobs:lava_vision` (verified in AM 1.21.1 and the pack's scripts). SRG names to check in `joined.tsrg`:
  `LivingEntity.travel`, `Entity.moveRelative`, `Entity.isInLava`.

### Decisions
- **`SWIM_SPEED` attribute or a `travel` mixin:** recommend the mixin; an attribute modifier from the effect would also
  speed water, and the Forge lava branch may ignore it.
- **Needs Fire Resistance too?** Recommend no: the effect alone gives speed; without fire protection the player still
  burns, which is the cost.
- **Mobs or players only:** recommend every `LivingEntity` with the effect (it is effect-driven, so only potion users
  and netherite wearers get it); the slower-lava factor players only.
- **Lower plain lava speed:** recommend leaving vanilla until the user tries the Lava Vision change.

### Gotchas
- Movement runs on the client for the local player: the mixin must be common (both sides) and read the effect from the
  client entity (effects are synced).
- Elytra and riding branches of `travel` must stay untouched; inject only in the lava branch.

### Tests
- **IP-new7 Lava Vision swim:** in a lava pool with Fire Resistance, time 10 blocks of swimming without and with Lava
  Vision: with it, about the water time.
- **IP-new8 Netherite set:** full netherite armour in lava: same speed as IP-new7 with the effect.

---

## item-physics #5: Golden egg gets gravity
Status **Planned** / Work patcherMod / tags / TODO `items/item-physics #5`

### Goal
The goose's golden egg item (`vintagedelight:golden_egg`, `i.golden_egg`) behaves as an ordinary dropped item: it falls
with gravity and floats on water.

### Now
- Since 2026-10-25 the goose lays it as a block (Vintage's Golden Egg falls like sand) and drops the item only in a
  closed 1x1 pen: `incubationAdditions` `goldenegg/GoldenGooseEgg.lay` (62-122; block path 97-108, item path 112-117:
  `bird.spawnAtLocation(item)` then `setPos`). No `setNoGravity` or velocity edit (verified 2026-10-31).
- Physics: eggs float (`'egg', 'turtle_egg', 'frogspawn'`, [item_physics_tags.js](../../kubejs/server_scripts/tags/item_physics_tags.js)
  168); `golden_egg` is in none of the four tags, so it sinks and burns. `i.golden_egg` is item_dict 14437.
- The cause of "no gravity" is unreproduced.

### Build
1. Reproduce: `/give` the egg, drop it over a pit and into water; also let a goose lay one in a 1x1 pen.
2. Floats: `t.add(e, PHYSICS_TAG.floats, [i.golden_egg])` (or the `organic` family from #2).
3. Fireproof per the decision.
4. Only if gravity is really missing: read the Vintage port's golden egg item class (`development/backportedVintage`)
   for `isNoGravity` or a custom `ItemEntity`.

### Decisions
Fireproof too? Recommend fireproof like the gold block (it breaks into gold nuggets).

### Gotchas
Tag changes need only `/reload`; a mixin change needs a restart. Only the tag edit is expected.

### Tests
- **IP-new9 Golden egg:** falls off a ledge, floats in water, survives a campfire.

---

## Built, needs play

| Row | Change | Status | Note |
|---|---|---|---|
| #1 | Fire / lava split, float / sink tags, burn transforms | Testing | [item_physics.md](../item_physics.md) 0-3; IP-01 to IP-14 open (IP-13 checks the `$1` crash fixed in 1.0.9) |
| #3 | Allurement curses and item physics | Testing | Built 2026-10-27 (`AllurementItemCurses`, TAIL inject `ItemPhysicServerMixin` 53-56); item_physics.md history 2026-10-27. Shipped to the client in 1.1.04; the server runs 1.1.05 since 2026-10-31 (`deploy/DEPLOYED.json`), so it is live there too. IP-15, SQ-IP-15 open |
| #4 | Water splash from falling blocks | built 2026-10-28 (TODO says Planned) | `FallingBlockWaterSplashMixin` (client list), [fixes_compat.md](../fixes_compat.md) 2.32; 1.1.04 changelog Fixed bullet. FX-49, CQ-FBS-01 to -03 open. The planned server-side config switch was not built (client-only mixin, no switch) |

## Writeup corrections made

- 2026-10-21: [item_physics.md](../item_physics.md) 0 and 4 (F1 called live in two places, wrong force-load line) fixed.
- 2026-10-31: none made (the sweep edits briefs only). Stale in the writeup: item_physics.md 0 (row 5 and Pending) and
  9.4 say `/dumpphysics` has not run, but `kubejs/exported/physics/item_physics.json` exists (2026-10-30 00:41).
