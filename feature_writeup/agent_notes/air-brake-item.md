# air-brake-item

> creativeengineering:air_brake - hold-to-use elytra brake item + why its physics live in Creative Common, not KubeJS

Moved from agent memory 2026-11-16 (last edited there 2026-09-13). Repo status docs win over any status line here.

`creativeengineering:air_brake` - right-click and hold like a
bow while gliding to bleed off speed. Horizontal + upward motion decelerate at a steady
flat rate; downward (falling) motion is only slowed slightly, multiplicatively, with an
optional terminal-speed cap applied first.

**Why the physics are in Creative Common, not KubeJS:** elytra flight is client-predicted;
vanilla is deliberately lenient about a gliding player's reported position (see
`isFallFlying()`'s carve-out in `ServerGamePacketListenerImpl`'s movement checks). KubeJS
`server_scripts` only ever run on the logical server, so a `PlayerEvents.tick`-based
velocity change there gets silently overwritten by the client's next movement packet
before a player would ever see it slow down - same class of problem as
[magic-resistance-effect](magic-resistance-effect.md)'s read-only `EntityEvents.hurt`, different cause. Confirmed by
inspecting `TickEvent.PlayerTickEvent` dispatch: it's a plain Forge event (not a KubeJS
one), fired once per tick for whichever `Player` is actually ticking on each physical
side - the server's copy for every `ServerPlayer`, AND the client's own copy for its local
player - so a `@SubscribeEvent` handler registered on `MinecraftForge.EVENT_BUS` from
common code runs deterministically on both, no Mixin needed.

**How to apply:**
- `creativeCommon/src/main/java/.../gear/AirBrakeHandler.java` - the `TickEvent.PlayerTickEvent`
  listener (END phase). Looks up the KubeJS-registered item lazily by ResourceLocation
  (same lazy-cache pattern as [magic-resistance-effect](magic-resistance-effect.md)'s `effect()`), checks
  `isUsingItem() + getUseItem().getItem() == brake` and (config-gated) `isFallFlying()`,
  then reshapes `getDeltaMovement()`: flat subtraction for horizontal speed and
  positive-Y speed (floored at a configurable minimum), a gentle per-tick fraction removed
  for negative-Y (falling) speed after an optional terminal-speed clamp. Sets
  `hurtMarked = true` afterward purely so other observing clients get the new velocity
  synced (irrelevant to the owning client, which is authoritative over its own motion).
- `CEConfig.AirBrake` (new section in `CEConfig.java`, [patchermod-coremod](patchermod-coremod.md)) - EVERY
  deceleration number is a config knob: `enabled`, `useDurationTicks` (baked into the item
  at registration - needs a restart, not just a config save, see its own comment),
  `requireFallFlying`, `horizontalDecelerationPerTick`, `minHorizontalSpeed`,
  `upwardDecelerationPerTick`, `minUpwardSpeed`, `downwardSlowFactorPerTick`,
  `terminalDownwardSpeed`. This was an explicit ask: "follow the new config heavy style
  and expose as much logic values to config as possible" (2026-09-13, superseding the old
  `global.config` JS framework; CEConfig values: see [config-driven-recipe-values](config-driven-recipe-values.md)).
- `CreativeEngineering.java` constructor: `MinecraftForge.EVENT_BUS.register(AirBrakeHandler.class)`.
- KubeJS: `kubejs/startup_scripts/items/air_brake.js` - pure item data (`.unstackable()`,
  `.texture('creativeengineering:item/air_brake')` (real 16x16 art at
  `kubejs/assets/creativeengineering/textures/item/air_brake.png`, wired 2026-09-13,
  replacing the earlier `minecraft:item/paper` placeholder), `.useAnimation($UseAnim.BOW)` +
  `.useDuration(stack => CFG.AIR_BRAKE.useDurationTicks.get())`, both via
  `Java.loadClass(...)` since KubeJS's `ItemBuilder.useAnimation` takes a real
  `net.minecraft.world.item.UseAnim` enum constant and `useDuration` a
  `ToIntFunction<ItemStack>`, not plain strings/numbers). `i.air_brake` added to
  item_dict.js. No crafting recipe yet - only the item + effect were asked for.

**2026-09-13 bug + fix - "brake does nothing":** `useAnimation`/`useDuration` only describe
an in-progress use; nothing actually STARTS one. Decompiled `kubejs-forge-1902.6.2-build.73
.jar`'s `dev.latvian.mods.kubejs.core.mixin.common.ItemMixin` (mixed into vanilla `Item`) to
confirm: it only calls `ItemUtils.startUsingItem()` on right-click when the `ItemBuilder` has
a `.use(...)` callback AND that callback returns `true` - without one, `Item#use()` falls
through to vanilla's do-nothing default and `isUsingItem()` never becomes true, so
`AirBrakeHandler` never sees the hold. Fix: added `.use((level, player, hand) => true)` to
air_brake.js. General lesson for any future hold-to-use KubeJS item: `useAnimation` +
`useDuration` + `.use(() => true)` are a package deal, not two independent knobs.

**Also added, same session - configurable "stall":** brake hard enough (horizontal speed at
or below `CEConfig.AirBrake.stallSpeedThreshold` after that tick's deceleration) and the
player is dropped out of fall-flying outright, gated on its own `stallEnabled` toggle.
Implementation snag: clearing the FALL_FLYING flag has no public API - `Entity#setSharedFlag`
is `protected`, and `AirBrakeHandler` isn't a subclass, so `stopFallFlying()` resolves that
one `Method` reflectively once (cached, `setAccessible(true)`) rather than adding a mixin for
it. Flag bit index (7) confirmed by javap-ing `LivingEntity#isFallFlying()`'s bytecode.

Compiles clean with `cd Creative Common && ./gradlew build --offline`; jar copied over
`mods/creativeengineering-1.0.0.jar`. The `.use()` fix above was confirmed working
in-game (slowdown works); the stall effect was not, at first - see next entry.

**2026-09-13 second bug - stall never fires:** `logs/latest.log` had the answer directly:
`java.lang.NoSuchMethodException: net.minecraft.world.entity.Entity.setSharedFlag(int,boolean)`
thrown from `AirBrakeHandler`'s static initializer at game load (caught, logged, silently
disabled the whole stall feature from then on - deceleration kept working since that's a
separate code path). Root cause, and a genuinely new lesson beyond what
[patchermod-coremod](patchermod-coremod.md) already documents about Mixin targets needing SRG names: **reobfJar
only rewrites actual bytecode symbolic references** (the `invokevirtual`/`getfield` constant-
pool entries behind ordinary compiled calls like `isFallFlying()`/`getDeltaMovement()` in
this same class, which line up fine) **- it cannot touch a plain String argument handed to
reflection** (`Class#getDeclaredMethod("setSharedFlag", ...)`). This runtime is SRG-
obfuscated underneath despite the dev-time jar being named `..._mapped_official_...`, so the
real member is `m_20115_`, found by grepping `creativeCommon/build/reobfJar/mappings.tsrg` for
the Mojmap name. Fixed by reflecting on `"m_20115_"` instead, with a comment recording the
general rule: **any reflection by name against a vanilla MC class in this codebase must use
the SRG name from mappings.tsrg, never the Mojmap/source name** - ordinary compiled calls
don't have this problem, only `Class#getDeclaredMethod`/`getDeclaredField`-style lookups do.
Rebuilt + redeployed; not yet re-confirmed in-game (ran out of turns before a relaunch) -
if the stall still doesn't fire after this, check `logs/latest.log` for the same
NoSuchMethodException first (mappings can shift between MCP/Forge updates), then verify
`stallEnabled`/`stallSpeedThreshold` in `config/creativeengineering-common.toml` actually
match what's expected before suspecting the tick-handler logic itself.
