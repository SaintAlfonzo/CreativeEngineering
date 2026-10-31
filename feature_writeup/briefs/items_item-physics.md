# items / item-physics: implementation brief

Written 2026-10-21 from [TODO](../todo/TODO.md) `# items`, Sub `item-physics` (row 5 added 2026-10-24). Feature writeup:
[item_physics.md](../item_physics.md) (test prefix `IP-`, used up to IP-14; new tests here start at IP-15, check the
next free number before adding them). Related: [tnt_shrapnel.md](../tnt_shrapnel.md) (burning TNT items).

Jars checked (decompiled with vineflower or read with `javap`, under `scratchpad/decomp/` and `scratchpad/vanilla/`):
`ItemPhysic_FORGE_v1.6.7_mc1.19.2.jar` (`ItemEntityMixin`, `ItemPhysicServer`, `CommonPhysic`),
`allurement-1.19.2-3.2.1.jar` (`ItemEntityMixin`, `AllurementUtil`, the two curse enchantments, `AllurementConfig`),
`create-1.19.2-0.5.1.i.jar` (item list from its lang), the vanilla 1.19.2 client jar with Mojang's mappings
(`ItemEntity`, `FallingBlockEntity`, `PrimedTnt`, `AbstractMinecart`, `Entity` bytecode) and MCP `joined.tsrg` for
SRG names. "Verified" means read from one of them.

## Scope and order

| Row | Change | Status | Size |
|---|---|---|---|
| #3 | Allurement curses and item physics | Bug | small: one `@Inject` in the existing `ItemPhysicServerMixin` + a 40-line helper. **Cause found** |
| #2 | Classify modded items | Planned | medium: KubeJS tags only (`item_physics_tags.js`), but needs the "how far" decision and an item dump |
| #4 | Water splash from anvils and other entities | Planned | small: one common mixin on `FallingBlockEntity` |

Build order:

1. **#3 and #4 together**: one patcherMod build (both are small common mixins, no config migration).
2. **#2** after the user picks the scope; KubeJS only, `/reload` applies it. It can go before or after the jar.

Related rows: `farming/composting` ([farming_composting.md](farming_composting.md): adds `#creativeengineering:composters`
to the floats list), `baking/baked-goods` ([baking_baked-goods.md](baking_baked-goods.md): may move sugar from sinks to
floats for the culture blocks), `gameplay/tnt-and-portal #1` (burning TNT items, shrapnel).

---

## item-physics #3: Allurement curses and item physics
Status Bug / Work patcherMod / TODO `items/item-physics #3`

### Goal
Allurement's two item curses work on dropped items again: Curse of Ascension makes a dropped item float upward, Curse
of Fleeting makes it slide away from the nearest entity.

### Now
- **The row's guess is right (verified).** Allurement's `com.teamabnormals.allurement.core.mixin.ItemEntityMixin`
  injects into `ItemEntity#tick` at `INVOKE Vec3.add(DDD)`, `shift = BY, by = 2`, and calls
  `AllurementUtil.ascendItem(item)` / `repelItem(item)` when the stack has `allurement:ascension_curse` /
  `allurement:fleeting_curse`.
- In vanilla's `ItemEntity.tick` (bytecode, `bqv.k`) the only `Vec3.add(DDD)` is the gravity line inside
  `else if (!this.isNoGravity()) setDeltaMovement(getDeltaMovement().add(0, -0.04, 0))` (offset 154, guarded by
  `isNoGravity` at 138). ItemPhysic's own `ItemEntityMixin` redirects `isNoGravity()` in `tick` to `true` (and
  `isInWater` / `isInLava` to `false`), then applies gravity itself in `ItemPhysicServer.updatePre`. So the branch
  holding Allurement's hook never runs: the mixin applies (Allurement's `defaultRequire 1` would crash otherwise) but is
  dead. Both curses do nothing on a dropped item.
- `AllurementUtil.ascendItem` (verified): `motion.y = (0.25 - motion.y) * 0.3`. `repelItem`: nearest `LivingEntity`
  within 5 blocks; push of 0.1 horizontally away from it (x0.2 in the air, +0.25 up on the ground).
- Allurement config (`config/allurement-common.toml` 58-64): both curses enabled. Its item-entity hook doesn't check
  the switches (only the enchantments' availability does).
- patcherMod already mixes into `team.creative.itemphysic.server.ItemPhysicServer`:
  [mixin/ItemPhysicServerMixin.java](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/ItemPhysicServerMixin.java)
  (`@Mixin(value = ItemPhysicServer.class, remap = false)`, redirects in `fireImmune`, `hurt`, `updatePre` (57),
  `updateFluidHeightAndDoFluidPushing`). ItemPhysic is a compile-only jar in `development/patcherMod/libs/`.
- `ItemPhysicServer.updatePre(ItemEntity, RandomSource)` (static, verified): adds -0.04 gravity out of fluids, or the
  fluid force in one; one return at the end; it may `hurt` (and so discard) the item in lava / fire.

### Build
1. New `itemphysic/AllurementCurses.java`:
   - resolve both enchantments once by id from `ForgeRegistries.ENCHANTMENTS` (`allurement:ascension_curse`,
     `allurement:fleeting_curse`); null when Allurement is absent, then do nothing;
   - `static void tick(ItemEntity item)`: return if `item.isRemoved()`; read levels with
     `EnchantmentHelper.getItemEnchantmentLevel(ench, stack)` (`getTagEnchantmentLevel` in Allurement's code is the same
     method); apply the two formulas above (copy them, credit `AllurementUtil` in the javadoc). No compile dependency on
     Allurement.
2. In `ItemPhysicServerMixin` add:
   ```java
   @Inject(method = "updatePre", at = @At("RETURN"), remap = false)
   private static void creativeengineering$allurementCurses(ItemEntity item, RandomSource rand, CallbackInfo ci) {
       if (CommonPhysic.getFluid(item) == null) AllurementCurses.tick(item); // vanilla: the hook sat in the no-fluid branch
   }
   ```
   (`team.creative.itemphysic.common.CommonPhysic.getFluid(ItemEntity)`, verified). It runs on both sides, as
   vanilla's tick and Allurement's hook do.
3. item_physics.md: new 3.x "Allurement curses" paragraph and a finding (F7: cause and fix); File map gets the helper.

### Ids and data
- Enchantment ids (verified): `allurement:ascension_curse`, `allurement:fleeting_curse`.
- Classes (verified): `team.creative.itemphysic.server.ItemPhysicServer#updatePre(ItemEntity, RandomSource)`,
  `team.creative.itemphysic.common.CommonPhysic#getFluid(ItemEntity)`.

### Decisions
- **In water too?** Vanilla only ran the curses out of fluids (the hook was in the gravity branch). Recommendation:
  keep that (the sketch's `getFluid == null`); an ascending item in water would fight ItemPhysic's float/sink rule.

### Gotchas
- `@Inject` into a static method needs a static handler; `remap = false` as for the other ItemPhysic hooks.
- Don't call Allurement's `AllurementUtil` directly: a compile dependency for two one-liners isn't worth it, and
  reflection would load it on every item tick.

### Tests
- **IP-15 Curse of Ascension:** `/give @s minecraft:stone{Enchantments:[{id:"allurement:ascension_curse",lvl:1}]}`,
  drop it on open ground -> it rises; in water it follows the normal float/sink rule.
- **IP-16 Curse of Fleeting:** same with `allurement:fleeting_curse`, walk toward the dropped item -> it slides away;
  a plain stone doesn't.

---

## item-physics #4: Water splash from anvils and other entities
Status Planned / Work patcherMod / TODO `items/item-physics #4`

### Goal
A falling anvil (and sand, gravel, concrete powder, any falling block) that drops into water splashes like a mob
does: splash particles, bubbles and the splash sound.

### Now
- Vanilla splashes from `Entity#doWaterSplashEffect` (`m_5841_`), called from `updateInWaterStateAndDoWaterCurrentPushing`
  (`aZ`) on the first tick an entity touches water (`!wasTouchingWater && !firstTick`), reached through `baseTick`
  or `updateInWaterStateAndDoFluidPushing` (`aY`).
- **Which entities miss it (verified in the bytecode):** `FallingBlockEntity.tick` (`bqu.k`, SRG `m_8119_`) never calls
  `super.tick()`, `baseTick` or `aY`, so falling blocks never splash (and never know they are in water).
  `PrimedTnt.tick` calls `aY` (splashes), `AbstractMinecart.tick` calls `aY` (splashes). `ItemEntity` calls
  `super.tick()`; ItemPhysic overrides `updateFluidHeightAndDoFluidPushing` (`m_204031_`) and returns "touching", so
  dropped items splash too (small: the splash scales with the entity's width and speed). Projectiles, boats and mobs
  go through `baseTick`.
- So "anvils and other entities" is in practice falling blocks. Modded entities that skip `baseTick` are unknown;
  Create contraptions are the likeliest (not checked).
- `doWaterSplashEffect` (vanilla): sound volume from speed (fast -> "high speed splash"), `1 + width*20` bubble and
  splash particles at the water surface. It runs on both sides: particles on the client, the sound broadcast from the
  server.

### Build
1. New common mixin `mixin/FallingBlockSplashMixin.java` (add to `"mixins"` in creativeengineering.mixins.json):
   ```java
   @Mixin(FallingBlockEntity.class)
   public abstract class FallingBlockSplashMixin extends Entity {
       protected FallingBlockSplashMixin(EntityType<?> t, Level l) { super(t, l); }

       @Inject(method = "m_8119_", at = @At("TAIL"), remap = false)
       private void creativeengineering$splash(CallbackInfo ci) {
           if (!CEConfig.<switch>.get()) return;
           boolean inWater = this.level.getFluidState(this.blockPosition()).is(FluidTags.WATER);
           if (inWater && !this.wasTouchingWater && !this.firstTick) this.doWaterSplashEffect();
           this.wasTouchingWater = inWater; // unused by FallingBlockEntity otherwise
       }
   }
   ```
   (`wasTouchingWater` `f_19798_`, `firstTick` `f_19803_`, `doWaterSplashEffect` `m_5841_`, all protected in `Entity`,
   so reachable from a mixin that extends it; mojmap names in source, reobf maps them. Check the existing patcherMod
   mixins for the house style on `remap` with SRG method names: e.g. `ServerLevelTimePauseMixin` uses
   `method = "m_8809_", remap = false`.)
2. Config switch: a boolean in CEConfig, e.g. a new small `[splash] fallingBlocks = true` section, or a line in an
   existing "fixes"-like section; read every tick, so keep it a cached `BooleanValue`.
3. item_physics.md: a short 3.x "Splashes" paragraph (it is about entities in water, the nearest writeup).

### Ids and data
- SRG names (verified with MCP `joined.tsrg`): `FallingBlockEntity.tick` `m_8119_`, `Entity.doWaterSplashEffect`
  `m_5841_`, `Entity.wasTouchingWater` `f_19798_`, `Entity.firstTick` `f_19803_`.

### Decisions
- **Which entities.** Recommendation: falling blocks only (the verified gap). If a modded entity is seen not
  splashing in play, add it the same way.
- **Also make falling blocks slow in water?** Out of scope; vanilla lets them fall through water at full speed.

### Gotchas
- `blockPosition()` is the entity's feet block; a block falling fast (about 2 blocks a tick at terminal speed) can
  skip a 1-deep pool between ticks. Acceptable; if wanted, also check the block above.
- `doWaterSplashEffect` uses the entity's width (falling blocks are 0.98): about 20 particles, like a mob.
- Concrete powder converts on landing in water (vanilla, its own raytrace); the splash just happens first.

### Tests
- **IP-17 Anvil splash:** drop an anvil and a block of sand into a 3-deep pool from 10 blocks up -> a splash and the
  splash sound as each hits the surface; on land, nothing new.

---

## item-physics #2: Classify modded items
Status Planned / Work KubeJS / TODO `items/item-physics #2` (item_physics.md F4)

**Superseded 2026-10-28 by the family design, [item_physics.md](../item_physics.md) section 9** (21 material families, the
`/dumpphysics` dump and `tools/item_physics_audit.py`). The Create / flour / treated-wood rules below are covered by the
metal, light_powder and wood families; the user's adjustments (totem, crossbow, armor stand, shield, trident) become
manual overrides.

### Goal
Modded items get sensible water and fire behaviour instead of all falling to the default (sink, burn in fire and
lava), starting with the "Future" list the user wrote in the tag file.

### Now
- Default (config/itemphysic.json): `swimmingItems` whitelist empty (nothing floats), `burningItems` blacklist empty
  (everything burns). Tags `#creativeengineering:physics/{floats,sinks,fireproof,lavaproof}` change it (2.1 of the
  writeup); `sinks` beats `floats`, `lavaproof` beats `fireproof`.
- The tag script, [kubejs/server_scripts/tags/item_physics_tags.js](../../kubejs/server_scripts/tags/item_physics_tags.js):
  - lines 116-123, the user's notes, not done:
    "Adjustments: Totem sinks, crossbow sinks, armor stand sinks, shields sink, trident is fireproof" and "Not done
    yet: Most create components will sink and be fireproof, belts will float, flour/cinder flour float. Behavior
    carries to compacted items. Treated wood will sink. Add items from all other mods!";
  - today `armor_stand` (152), `crossbow` (156), `shield` (157) and `totem_of_undying` (195) are in **floats**, the
    opposite of the note; `trident` is in no tag;
  - `#forge:ingots`, `#forge:nuggets`, `#forge:raw_materials` are already fireproof (209), so every modded metal
    ingot already survives fire;
  - the stone family and `#create:stone_types/*` are lavaproof (283-), so Create's stone blocks already survive lava.
  - `physicsFromTag` (50-82) duplicates `global.tags.add` / `addMatching` and calls `event.add` itself (F6). No call
    in the file passes `except` or `expand`.
- Create 0.5.1 has 671 item / block ids (lang, verified): metal parts (shafts, cogwheels, casings, gearboxes,
  mechanisms, sheets, pipes, tanks, funnels, chutes...), wooden or cloth ones (sails, seats, water wheels,
  windows), foods (bar of chocolate, chocolate-glazed berries, honeyed apple, builder's tea, sweet roll, blaze cake,
  dough, wheat flour, cinder flour) and stone (already lavaproof).
- Dict keys present: `i.belt_connector` (175), `i.cinder_flour` (1328), `i.wheat_flour_pile` (154),
  `i.cinder_flour_pile` (5127), `i.flour = '#forge:flour/wheat'` (1884), `i.t_flour_corn` (1886), `i.armor_stand`
  (2020), `i.crossbow` (2160), `i.shield` (2671), `i.totem_of_undying` (2732), `i.trident` (2735). No key for
  `#forge:flour` (all flours) or `#forge:treated_wood`.

### Build
1. **Dict keys** (item_dict.js, tag-key style `i.t_*`): `i.t_flour = '#forge:flour'`, `i.t_treated_wood =
   '#forge:treated_wood'`, `i.t_treated_wood_slab = '#forge:treated_wood_slab'`, and keys for any single ids below
   that lack one (Create seats / sails / water wheels have tags or wildcards; see 3).
2. **Adjustments** (the user's own list, no decision needed): add `i.totem_of_undying`, `i.crossbow`, `i.armor_stand`,
   `i.shield` to `sinks` (sinks wins over floats; or delete them from the floats lists at 152-157 / 195, cleaner),
   `i.trident` to `fireproof`.
3. **The "Future" list:**
   ```js
   // Create components: sink (default) and fireproof; wood, cloth and food parts keep burning
   t.addMatching(e, PHYSICS_TAG.fireproof, ['@create', '@create_dd', '@createaddition'], { except: [
       '#create:seats', 'create:*_sail', 'create:sail_frame', 'create:water_wheel', 'create:large_water_wheel',
       '/^create:(oak|spruce|birch|jungle|acacia|dark_oak|mangrove|crimson|warped)_window(_pane)?$/', i.belt_connector,
       '/^create:(bar_of_chocolate|chocolate_glazed_berries|honeyed_apple|builders_tea|sweet_roll|blaze_cake.*|dough|wheat_flour|cinder_flour)$/',
   ] })
   t.add(e, PHYSICS_TAG.floats, [i.belt_connector, i.t_flour, i.cinder_flour, i.wheat_flour_pile, i.cinder_flour_pile])
   t.add(e, PHYSICS_TAG.sinks, [i.t_treated_wood, i.t_treated_wood_slab, '/^immersiveengineering:treated_/'])
   ```
   (The mod list in `@...` is the "how far" decision. The except list is a starting point: check it against
   `kubejs/exported/items_by_mod/create.json` after `/kubejs export` or `/cegen run ... dumps`.)
4. **"Behaviour carries to compacted items."** Create: Compressed and similar storage blocks follow their content:
   `create_compressed:*_pile` of flour float (above); compressed metal blocks are in `#forge:storage_blocks/*` ->
   add `'#forge:storage_blocks'` to fireproof (metal storage blocks are already fireproof by id for vanilla only, 2.2),
   minus any organic storage block (`#forge:storage_blocks/{wheat,...}` if the dump shows some).
5. **F6:** replace `physicsFromTag` by `t.add(e, target, sources.map(s => '#' + s))` (nothing uses its other options)
   and delete the function, so the file only uses `global.tags`.
6. **Beyond Create (scope decision):** apply the same three rules per mod family once picked: metal machine parts ->
   fireproof (IE: `@immersiveengineering` minus treated wood, hemp, foods; PneumaticCraft: `@pneumaticcraft` minus
   foods), wood / cloth / paper items -> floats (most already through `#minecraft:planks` etc.), foods stay default.
7. Update item_physics.md 2.2 (what's in the tags), F4 (done / scope), and replace the "Not done yet" comment at
   item_physics_tags.js 119-123 with what was done.

### Ids and data
- Verified: the Create ids named above exist in Create 0.5.1's lang; `create:belt_connector` is the belt item.
- Verified in Create 0.5.1: item tag `create:seats` (also `casing`, `toolboxes`, `valve_handles`, `stone_types/*`);
  sails are `create:<colour>_sail` + `create:sail_frame`; windows are the nine wood types plus
  `create:ornate_iron_window` (metal, so it stays fireproof: the except regex names the wood ones only).
- Unverified: whether IE puts treated wood into `#minecraft:planks` (if not, treated wood already sinks and step 3's
  sinks line is harmless). Check with `/kubejs export` or a `/dumptags` export.

### Decisions
- **How far (the row's question).** A: Create + the user's adjustments only (recommended first step, it is what the
  note asks for). B: also IE, PneumaticCraft, Create addons, by the same rules. C: every mod ("Add items from all other
  mods!"), which needs a per-mod pass over the item dump. Recommendation: A now, B in the same sitting if the dump makes
  it quick; C as a running task with the "rules" above.
- **Create cogwheels and shafts** contain planks (cogwheel) or are andesite (shaft); the note says "most create
  components sink and fireproof". Recommendation: follow the note, cogwheels included.
- **Flour in fire.** Floats, but burns (default). Recommendation: keep burning (flour is flammable).

### Gotchas
- A Rhino quirk the file already notes: no spread in array literals; build lists with `.concat()`.
- `addMatching`'s `except` expands every `#tag` entry into ids (tag_helper.js 36-55), so put `#tags` you want live
  in a separate `t.add` call.
- Tag membership is live on `/reload`; the burn-transform API (`global.itemPhysics`) is startup-only.

### Tests
- **IP-18 Create parts:** drop a cogwheel, a shaft and an andesite casing into a campfire -> survive; into lava ->
  burn; a belt in water -> floats; a Create sail in fire -> burns.
- **IP-19 Adjustments:** a totem, a crossbow, an armor stand and a shield in water -> sink; a trident in fire ->
  survives.
- **IP-20 Flour and treated wood:** Create wheat flour and a flour pile in water -> float; IE treated planks -> sink.

---

## item-physics #5: Golden egg gets gravity
Status **Planned** / Work patcherMod / tags / TODO `items/item-physics #5`

### Goal
The goose's golden egg (`vintagedelight:golden_egg`, `i.golden_egg`) behaves as an ordinary dropped item: it falls with
gravity and floats on water.

### Now
- The egg is dropped by `incubationAdditions` `goldenegg/GoldenGooseEgg.drop` (line 58-98): a plain
  `bird.spawnAtLocation(item)` then `setPos` onto the chosen spot. No `setNoGravity` or velocity edit appears in the
  incubationAdditions code (grep), so gravity itself is not removed there. What the user saw (hovering, or sinking) is
  not reproduced: unverified.
- Item physics classes: [item_physics_tags.js](../../kubejs/server_scripts/tags/item_physics_tags.js). Eggs float:
  `'egg', 'turtle_egg', 'frogspawn'` (line 193, in the vanilla list of `physicsFromTag(e, 'floats', ...)`);
  anything untagged sinks and burns. `golden_egg` is in none of the four tags, so it sinks. `i.golden_egg` is item_dict
  line 13836; the block is `GoldenEggBlock` in the Vintage port jar (a block item).
- ItemPhysic's own flat-lying render and physics (`ItemPhysicServerMixin`, `ItemPhysicSplit` in patcherMod) may treat a
  block item differently: unverified.

### Build
1. Reproduce: drop a golden egg in water and over a pit; note whether it falls and whether it floats.
2. Floats: add `i.golden_egg` to the `floats` group with the eggs (a `add('floats', ...)` with the key, since the file
   lists vanilla by short name; modded items use `i.` keys elsewhere in the file).
3. Fire: gold items are `fireproof` (`gold_block`, `golden_*`); decide whether the egg is too (Decisions).
4. If gravity really is missing in game, read `GoldenEggBlock` / the item class in the Vintage jar for `isNoGravity` or
   a custom `ItemEntity`.

### Decisions
Fireproof or lavaproof as well? Recommend fireproof like the gold block (it drops 8 to 32 gold nuggets when broken).

### Gotchas
Tag changes need only `/reload`; a mixin change needs a restart. Only the tag edit is expected.

### Tests (IP-)
- IP-15 (proposed): goose or `/give` egg: falls off a ledge, floats in water, survives open fire.

---

## Built, needs play

| Row | Change | Status | Note |
|---|---|---|---|
| #1 | Fire / lava split, float / sink tags, burn transforms | Testing | Code matches the writeup (F1 if-chain at `ItemPhysicSplit` 128-133). item_physics.md still called F1 a live crash in two places and cited a wrong force-load line: fixed (below). IP-01 to IP-14 open |

## Writeup corrections made

- [item_physics.md](../item_physics.md) 0, first table row: "a recurring crash in it is unfixed (F1)" -> fixed in
  1.0.9.
- [item_physics.md](../item_physics.md) 0, Pending: "F1 is a live crash bug" -> fixed in 1.0.9, IP-13 checks it.
- [item_physics.md](../item_physics.md) 4, F1: "**The `ItemPhysicSplit$1` crash is still in the code.** `burnDecision`
  switches on" -> "Was (before the fix): `burnDecision` switched on" (the FIXED note before it already said so).
- [item_physics.md](../item_physics.md) 3: force-load at `CreativeEngineering.java` line 254 -> 273.
