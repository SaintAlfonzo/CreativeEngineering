# echo-tier-gear

> KubeJS echo tools; CDD echo armor set bonus -> Night Vision + Phantom Bane

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Merged 2026-10-19 from 2 memories (index compaction).

## echo-tier-tools

Create: Deep Dark ships only an echo **sword**; the pack adds the 4 digging tools.

**Registration** — `kubejs/startup_scripts/items/echo_tools.js`:
- `ItemEvents.toolTierRegistry(e => e.add('echo', tier => {...}))` — `MutableToolTier` setters.
  Numbers from `create_deep_dark.item.EchoSwordItem`'s anonymous Tier: `uses 2124`,
  `speed 9.0`, `attackDamageBonus 6.0`, `enchantmentValue 16`, repair = `create_deep_dark:echo_ingot`.
  **`level` set to 4 (netherite), NOT the echo sword's own 1** — the mod's tier is
  stone-tier mining level, fine for a sword but useless on a pickaxe. Change `setLevel` to 1
  for a literal match.
- `StartupEvents.registry('item', e => e.create('creativeengineering:echo_'+t, t).tier('echo').fireResistant().group('tools').displayName(...))`
  for t in pickaxe/axe/shovel/hoe. `.tier('echo')` resolves via KubeJS' `String→Tier`
  typewrapper (`ItemBuilder.toToolTier` -> `TOOL_TIERS` map); the toolTierRegistry event
  fires before the item registry, so ordering is fine.
- Dict: `i.echo_pickaxe` … in item_dict.js after `i.echo_sword`. Physics: added the 4 to the
  `lavaproof` `add()` in item_physics_tags.js next to `i.echo_sword` ([itemphysic-fire-lava-split](itemphysic-fire-lava-split.md)).
- No recipes yet.

**Animated texture** — `echo_sword.png` is a 16×192 vertical strip of 12 frames; the
silhouette is identical every frame, only pixels painted in a fixed **12-step teal glow
ramp** change — each frame every glow pixel steps +1 along the ramp (wrapping). `.png.mcmeta`:
`{"animation":{"frametime":2,"frames":[11,10,9,8,7,6,5,4,3,2,1,0]}}` (reversed -> glow crawls
one way, 2 ticks/frame, loop). Ramp (dark->bright): `(5,16,16)(9,28,28)(11,34,34)(14,43,43)
(21,64,64)(27,82,82)(28,85,85)(32,97,97)(36,109,109)(44,131,131)(45,135,135)(49,148,148)`.
Static echo palette: outline `(11,32,32)/(33,33,33)/(45,45,45)`, handle browns `(53,26,12)/
(84,42,20)/(134,72,41)`.

`tools/echo_texture.py` (Pillow; `tools/` was recreated — it had been trashed):
- `animate <frame0.png>` — the workhorse: a 16×16 sprite you drew (glow parts in EXACT ramp
  colors) -> writes the 16×192 strip + `.png.mcmeta`, overwriting the source. Re-run after edits.
- `starter` — rough auto-convert of vanilla **diamond** tool shapes (teal gem = clean
  head/handle split) -> `kubejs/assets/creativeengineering/textures/item/echo_<tool>.png`
  frame-0 drafts. Needs `diamond_<tool>.png` beside the script (from the client jar).
Current `echo_<tool>.png` in kubejs/assets are starter output already run through `animate`
(16×192 + mcmeta) — refine the heads by hand, then `animate` again.

Preview textures with F3+T; new items need a restart. `global.printItemTexture(
'creativeengineering:echo_pickaxe')` (client_scripts/texture_tool.js) confirms the model
resolved the texture.

## echo-armor-set-bonus

Create: Deep Dark's echo armor full-set bonus lives in an **MCreator procedure with no
config**: `create_deep_dark.procedures.EchoArmorEffectProcedure.execute(Event, Entity)` —
`@SubscribeEvent` on `LivingTickEvent`, checks HEAD/CHEST/LEGS/FEET == the four
`ECHO_ARMOR_*` items, then every tick calls `LivingEntity.addEffect` twice:
`MobEffects.DAMAGE_RESISTANCE` (f_19606_) and `DAMAGE_BOOST` (f_19600_), both `60,1,false,false`.

**Patch:** `patcherMod/.../mixin/EchoArmorEffectProcedureMixin.java` — one `@ModifyArg`
on the `m_7292_(MobEffectInstance)Z` call inside `execute(...Event;...Entity;)V`
(`remap=false`, SRG target, Mojmap body reobf'd). Only fires after the set check, so it
just rewrites the arg: DAMAGE_RESISTANCE -> `MobEffects.NIGHT_VISION` (duration bumped to
300 ticks so it doesn't strobe — proc keeps resetting to 60, and NV flashes client-side
under ~200), DAMAGE_BOOST -> `ForgeRegistries.MOB_EFFECTS.getValue(better_beacons:phantom_bane)`
(null-guarded -> falls back to Strength if better_beacons absent). "Phantom Bane" is a
`better_beacons` (CerbonsBetterBeacons) effect, NOT Alex's Mobs.

Wiring: added to `creativeengineering.mixins.json` `"mixins"` array; optional
`create_deep_dark` dep (`mandatory=false`, `ordering="AFTER"`) in mods.toml; **added
`create_deep_dark-1.7.0-forge-1.19.2.jar` to `patcherMod/libs/`** (the Mixin AP validates
`@Mixin(value=X.class)` targets at compile — same reason ItemPhysic/Create jars are in
libs). Build/deploy per [patchermod-coremod](patchermod-coremod.md): `./gradlew build --offline` -> copy jar to
`mods/`. To change the effects, edit the two branches in the mixin.

**Tooltip follow-up (2026-09-10):** the armor's own hardcoded tooltip ("With full set on:
Resistance II (infinite) / Strength II (infinite)", duplicated verbatim in
`EchoArmorItem.Boots/Chestplate/Leggings/Helmet#appendHoverText`, `m_7373_`) still described
the ORIGINAL bonus — the mixin above only changes the runtime effect, not the item's own
tooltip text. Fixed with a second mixin, `EchoArmorTooltipMixin` — one `@Mixin` listing all
four nested classes, two `@ModifyConstant`/`@Constant(stringValue = ...)` handlers matching
the two effect-name string literals exactly and swapping them to "Night Vision" / "Phantom
Bane" (both still "(infinite)" — the underlying effect is still reapplied every tick while
the full set is worn). `better_beacons`'s own `en_us.json` names the effect
`effect.better_beacons.phantom_bane` -> "Phantom Bane" (no apostrophe).

**Echo tool on-hit effects (2026-09-10):** `create_deep_dark:echo_sword` applies Weakness II
+ Darkness II (5 sec., ticks=100/amplifier=1) to whatever it hits — CDD's own
`EchoSwordEffectProcedure`, a `LivingAttackEvent` listener hardcoded to that one item, also
documented in the sword's own tooltip. The 4 `creativeengineering:echo_*` tools (pickaxe/axe/
shovel/hoe) had no such effect despite requiring the same two potions to craft
(`server_scripts/magic/end_gear.js`). Added `server_scripts/magic/echo_tool_effects.js` —
`EntityEvents.hurt` listener checking `event.getSource().getEntity().getMainHandItem()
.getItem().getId()` against the 4 tool ids, then `target.addEffect(new
MobEffectInstance(MobEffects.WEAKNESS/DARKNESS, 100, 1))`. Deliberately NOT done via
`Item#hurtEnemy` (the KubeJS `ItemBuilder.hurtEnemy` hook) — `SwordItem`/`DiggerItem` override
`hurtEnemy` to call `stack.hurtAndBreak(1|2, ...)` for durability, and `HoeItem` doesn't
override it at all (hoes take no durability loss from attacking, vanilla-normal); replacing
the whole method per-tool would mean re-implementing (and risking) that durability logic.
`EntityEvents.hurt` only piggybacks the debuff, untouched otherwise. Matching tooltip lines
("On hit, inflicts to target: Weakness II / Darkness II (5 sec.)") added via `.tooltip(Text.of(...))`
in `startup_scripts/items/echo_tools.js`. NOTE: `.getId()` not `.kjs$getId()` — see
memory `tune-tools-func`'s `@RemapPrefixForJS("kjs$")` finding, applies to every `ItemKJS`
accessor including `getId`.
