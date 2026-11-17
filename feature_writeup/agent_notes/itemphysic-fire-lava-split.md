# itemphysic-fire-lava-split

> patcherMod mixin splitting ItemPhysic's single burn bucket into fire-vs-lava + additive float list, driven by a KubeJS startup script

Moved from agent memory 2026-11-16 (last edited there 2026-09-10). Repo status docs win over any status line here.

ItemPhysic 1.6.7 (`config/itemphysic.json`, CreativeCore `SortingList`s) has **one**
`burningItems` bucket — IN_FIRE / ON_FIRE / LAVA are indistinguishable — and one
`swimmingItems` bucket. `canPass(stack)==true` means "this hazard may destroy it".

**patcherMod** ([patchermod-coremod](patchermod-coremod.md)) splits this:

- `mixin/ItemPhysicServerMixin` — `@Redirect`s every `SortingList.canPass(ItemStack)` call in
  `ItemPhysicServer.{fireImmune,hurt,updatePre,updateFluidHeightAndDoFluidPushing}`
  (`@Mixin(remap=false)`, target string literal — `canPass` is a CreativeCore method, the
  `ItemStack` desc only disambiguates the `canPass(Object)` overload). One `route()` re-dispatches
  by `list ==` identity: `burningItems` -> `ItemPhysicSplit.burnDecision(stack, touchingLava(item))`
  (null -> fall back to `list.canPass`); `swimmingItems` -> `isFloat(stack) || list.canPass`;
  `undestroyableItems`/anything else -> untouched.
- `itemphysic/ItemPhysicSplit` — JVM-global registry: `BURN_BY_ID/TAG` (enum BURN/FIRE_ONLY/
  LAVA_ONLY/NEVER), `FLOAT_BY_ID/TAG`. `touchingLava()` = eye-in-lava OR fluidHeight>0 OR
  lava blockstate at pos/pos.below (fallback). Tags resolved live via `stack.is(TagKey)`.
  `forceLoad`ed in `CreativeEngineering.commonSetup` (late-transform safety).
- `libs/`: added `ItemPhysic_FORGE_v1.6.7` + `CreativeCore_FORGE_v2.11.28` jars.
- `mods.toml`: optional `itemphysic` dep, ordering AFTER.

**Config now (`config/itemphysic.json`, also mirrored in `configs_edited/root_config/`):**
`swimmingItems` = `{entries:[], isWhitelist:true}` (nothing floats -> all sink),
`burningItems` = `{entries:[], isWhitelist:false}` (everything burns by fire+lava).
So defaults = sink + burn; the KubeJS rules carve out exceptions.

**Canonical behavior tags** (2026-09-08, the normal knob) — `ItemPhysicSplit` holds fixed
`TagKey`s checked live via `stack.is()`, so membership updates on `/reload`, no restart:
- `creativeengineering:physics/floats` -> floats
- `creativeengineering:physics/sinks` -> force-sink (mixin checks `forcedSink()` first, wins)
- `creativeengineering:physics/fireproof` -> survives open fire, still burns/melts in lava
- `creativeengineering:physics/lavaproof` -> immune to BOTH fire and lava (wins over fireproof)
`burnDecision(stack,lava)` order: LAVAPROOF->FALSE, then FIREPROOF->`lava?TRUE:FALSE`, then the
programmatic `Burn` enum (BURN/FIRE_ONLY/LAVA_ONLY/NEVER, KubeJS-startup only). patcherMod jar
seeds the tags: `src/main/resources/data/creativeengineering/tags/items/physics/*.json`
(netherite via `#forge:` tags, boats, feathers, …). Pack rules in
`kubejs/server_scripts/tags/item_physics_tags.js` (`ServerEvents.tags('item')`): direct
`t.add` + local `physicsFromTag(e, behaviour, sourceTags[, {except,expand}])` — default mode
adds a NESTED `#tag` ref (transitive, order-safe); `PHYSICS_TAG` map aliases behavior names
(float/sink/fireproof/lavaproof/…). Its local `mc(arr)` helper resolves each bare id through `global.item` (dict) when that maps
to a CONCRETE id, else falls back to `minecraft:<id>`; namespaced strings and `#tag`-valued
dict entries pass through untouched. So bare ids in the `add(behaviour, mc([...]))` lists are
"using the dict" — but note the dict's food/crop keys are `#forge:` tags (skipped), and
`i.melon`=slice / `i.glowstone`=dust / `i.block_copper` not `i.copper_block` etc., which is
why the file keeps vanilla-style bare names + a smart `mc()` rather than literal `i.foo`.

**Comprehensive vanilla 1.19.2 pass done 2026-09-09**
(~380 lines): nested vanilla/forge tag families do the bulk (planks/logs/wool/flowers/
saplings/candles/beds/banners/fishes/dyes/leather/string -> floats; glass/sand/gravel/
sandstone/dirt/concrete/terracotta/raw_materials/ingots/nuggets/rails/anvil/music_discs ->
fireproof; #forge:stone+cobblestone+ores+obsidian+end_stones+netherrack + all
#create:stone_types + shulker_boxes + soul_fire_base_blocks -> lavaproof), then explicit id
lists + `mc()`/`cross()`/`COPPER` builders for the rest. Header comment lists every
gray-area call (whole stone family -> lavaproof per pre-existing pack choice; quartz ->
lavaproof; ice/snow/sculk -> fireproof; sugar sinks / bone_meal floats; honey_bottle
fireproof; hay_block/shield/wooden_tools/turtle_helmet float; tnt/torches/campfires/coal/
redstone_torch+lamp/target/nether_wart left DEFAULT-burn). Verify with `/dumptags item`
([dump-tags-script](dump-tags-script.md)) -> grep `creativeengineering:physics/` in
`kubejs/exported/tags/item_tags.json`. Category/umbrella tags (`creativeengineering:phys/*`, e.g.
`storage_block_metal_meltable`, `heat_immune`) built in `server_scripts/tags/categorytags.js`
(loads first, `c`<`i`) then nested in via `physicsFromTag`. Tags are exact ids — NO prefix
matching (`#forge:storage_blocks/raw` ≠ `.../raw_iron`).

**Programmatic API (startup-only):** `kubejs/startup_scripts/func/item_physics.js` defines
`global.itemPhysics` (`.float/.sink/.unburnable/.burnable/.fireOnly/.lavaOnly`, ids and/or `#tag`)
— for id one-offs; needs restart.

**Burn-transform (leave something behind when fire/lava destroys a dropped item):**
`itemphysic/ItemBurnTransform` (JVM-global, parallels ItemPhysicSplit: `BY_ID` +
insertion-ordered `BY_TAG`, `Rule{kind REPLACE|EXPLODE, result, keepCount, power, chance}`,
`ruleFor(stack)`, `apply(item, stackCopy, rule)`). Hook = a 2nd `@Redirect` in
`ItemPhysicServerMixin` on `ItemEntity.m_146870_()` (discard) inside `ItemPhysicServer.hurt` —
when `source.isFire()` (fire/lava/hot-floor) it runs the rule then discards as normal.
**Gotcha:** a `@Redirect` handler wanting the enclosing method's params must take the
redirected call's receiver THEN *all* of them, even when redundant — signature is
`(ItemEntity discardTarget, ItemEntity item, DamageSource source, float amount)`
(discardTarget == item). Omitting the redundant first `item` = MixinApplyError on boot,
which cascades into ItemPhysic's COMMON_SETUP failing. REPLACE spawns a fresh ItemEntity (`keepCount` -> roll `chance`
per item in the stack); EXPLODE = `level.explode(null, BurnBlastDamage.INSTANCE, null, x,y,z, power, false, Explosion.BlockInteraction.NONE)`
(9-arg overload; entity damage + knockback, no block break, no fire). `BurnBlastDamage`
(`itemphysic/BurnBlastDamage.java`, `extends DamageSource("explosion")` + `setExplosion()`,
NO `setScalesWithDifficulty()`) — vanilla's explosion source scales with difficulty and
`Player.hurt` zeroes difficulty-scaled damage on **Peaceful**, so a Peaceful survival player
took knockback but no damage; this hurts players on every difficulty (Blast Protection still
applies). Test worlds: `WorkspaceNPD` is Peaceful, `Workspace` Easy. API: `global.itemPhysics.burnReplace(idOrTag,
resultId, chance=1, perItem=true)` / `.burnExplode(idOrTag, power=3, chance=1)` /
`.burnExclude(...idOrTag)` (never transform — checked before any rule; `EXCLUDE_ID`/`EXCLUDE_TAG`
in ItemBurnTransform) / `.burnClear`. The spawned-entity `PRODUCT_TAG` scoreboard flag only
stops the *immediate* re-loop — a re-dropped end product is a fresh unflagged entity, so
`burnExclude` the results (dirty_glass/pane/shard, ash) is the real fix.
Current rules (in item_physics.js): `#forge:glass`->`quark:dirty_glass`, `#forge:glass_panes`->
`quark:dirty_glass_pane` (both 1.0/perItem — glass is fireproof so LAVA-only),
`#creativeengineering:burn/to_ash`->`supplementaries:ash` 0.25/perItem,
tnt & tnt_minecart explode 4.0, gunpowder 2.0, spare `#creativeengineering:burn/explosive` 2.5.
Bucket membership (`creativeengineering:burn/to_ash` = planks/logs/wood shapes,
`burn/explosive` empty) is reload-able in `server_scripts/tags/item_physics_tags.js`; rule
params are startup-only.

Build/deploy: `cd patcherMod && ./gradlew build --offline` -> copy jar to `mods/`.

**Mechanics read from ItemPhysic 1.6.7 source (2026-10-28):**
- `ItemPhysicServer.hurt` returns early on `!item.canBeHurtBy(source)`: vanilla fire-resistant items survive fire AND
  lava whatever the split says (the split can't make them burn).
- Water float = +min(0.04, 0.1-vy)/tick with NO vertical drag in fluids (dive ~2.8 from a 3-block drop, endless bob).
  Float branch skipped in lava (`!fluid.is(LAVA)`); viscosity = FluidType viscosity/1000 (lava 6). Surface-float
  design and the sim live in item_physics.md section 10.
- Family classifier + `/dumpphysics`: `tools/item_physics_audit.py` (design: item_physics.md section 9).
- Alex's Mobs Lava Vision (`EffectLavaVision`) has an empty effect tick: fog only, no movement change.
