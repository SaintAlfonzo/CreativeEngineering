// Creative Equipment; moved from kubejs/startup_scripts/ on 2026-11-04 (creative_split.md 7.6, wave 7), rows unchanged.
// Air Brake — a hold-to-use elytra brake. Right-click and hold, like drawing a bow, while
// gliding to bleed off speed: horizontal and upward motion decelerate at a steady rate,
// downward motion is only slowed slightly.
//
// The item here is pure data (id, stacking, use-animation/duration). The actual velocity
// math can't live in KubeJS: elytra flight is client-predicted, and a server-only change
// (the only side KubeJS server_scripts run on) gets overwritten by the client's next
// movement packet before a player would ever see it. It lives in Creative Equipment's Java instead -
// gear/AirBrakeHandler.java, a plain Forge TickEvent.PlayerTickEvent listener (no Mixin
// needed) that runs deterministically on both the server AND each client's own tick.
//
// Every deceleration number is a config knob - config/creativeequipment-common.toml
// under [air_brake], via Creative Equipment's CEQConfig (Forge-native, hot-reloadable through
// Create's in-game Configs screen). useDurationTicks is read on every use, not at
// registration: this script runs before Forge loads common configs, and a value read that
// early is always the default in production.

const $UseAnim = Java.loadClass('net.minecraft.world.item.UseAnim')

let CFG = null
try {
	CFG = Java.loadClass('com.SaintAlfonzo.creativeequipment.CEQConfig')
} catch (err) {
	console.warn('[air_brake] Creative Equipment CEQConfig not on classpath - falling back to a 72000-tick (bow-length) use duration: ' + err)
}
function airBrakeUseDurationTicks() {
	return CFG ? CFG.AIR_BRAKE.useDurationTicks.get() : 72000
}

StartupEvents.registry('item', event => {
	event.create('creativeequipment:air_brake')
		.unstackable()
		.group('tools')
		.displayName('Air Brake')
		.texture('creativeequipment:item/air_brake')
		.useAnimation($UseAnim.BOW)
		.useDuration(stack => airBrakeUseDurationTicks())
		// useAnimation/useDuration only describe an in-progress use - something still has
		// to START one. KubeJS's ItemMixin only calls ItemUtils.startUsingItem() on right
		// click when this callback is present AND returns true (bow/crossbow-type items do
		// the same thing internally, just in vanilla Java instead of here). Without this,
		// right-clicking never sets isUsingItem() and AirBrakeHandler never sees the hold.
		.use((level, player, hand) => true)
})
