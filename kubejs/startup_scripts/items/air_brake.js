// Air Brake — a hold-to-use elytra brake. Right-click and hold, like drawing a bow, while
// gliding to bleed off speed: horizontal and upward motion decelerate at a steady rate,
// downward motion is only slowed slightly.
//
// The item here is pure data (id, stacking, use-animation/duration). The actual velocity
// math can't live in KubeJS: elytra flight is client-predicted, and a server-only change
// (the only side KubeJS server_scripts run on) gets overwritten by the client's next
// movement packet before a player would ever see it. It lives in patcherMod instead -
// gear/AirBrakeHandler.java, a plain Forge TickEvent.PlayerTickEvent listener (no Mixin
// needed) that runs deterministically on both the server AND each client's own tick.
//
// Every deceleration number is a config knob - config/creativeengineering-common.toml
// under [air_brake], via patcherMod's CEConfig (Forge-native, hot-reloadable through
// Create's in-game Configs screen). useDurationTicks below is the one exception: it's
// baked into the item at registration time, so changing it needs a restart, not just a
// config save - see CEConfig.AirBrake's comment on that field.

const $UseAnim = Java.loadClass('net.minecraft.world.item.UseAnim')

let CFG = null
try {
	CFG = Java.loadClass('com.SaintAlfonzo.creativeengineering.CEConfig')
} catch (err) {
	console.warn('[air_brake] patcherMod CEConfig not on classpath - falling back to a 72000-tick (bow-length) use duration: ' + err)
}
const AIR_BRAKE_USE_DURATION_TICKS = CFG ? CFG.AIR_BRAKE.useDurationTicks.get() : 72000

StartupEvents.registry('item', event => {
	event.create('creativeengineering:air_brake')
		.unstackable()
		.group('tools')
		.displayName('Air Brake')
		.texture('creativeengineering:item/air_brake')
		.useAnimation($UseAnim.BOW)
		.useDuration(stack => AIR_BRAKE_USE_DURATION_TICKS)
		// useAnimation/useDuration only describe an in-progress use - something still has
		// to START one. KubeJS's ItemMixin only calls ItemUtils.startUsingItem() on right
		// click when this callback is present AND returns true (bow/crossbow-type items do
		// the same thing internally, just in vanilla Java instead of here). Without this,
		// right-clicking never sets isUsingItem() and AirBrakeHandler never sees the hold.
		.use((level, player, hand) => true)
})
