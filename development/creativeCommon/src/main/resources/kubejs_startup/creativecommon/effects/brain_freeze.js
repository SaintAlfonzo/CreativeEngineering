// Creative Common: creativecommon:brain_freeze (TODO sweets #9, feature_writeup/smoothies.md section 3). Cold foods
// (#creativecommon:brain_freeze) eaten close together roll for it; while it lasts the player stays fully frozen (the
// powdered snow overlay and vanilla freeze damage). Registered here like Wither Immunity (effects/wither_immunity.js);
// the roll and the freezing are Java (effect/BrainFreeze.java), lengths in creativecommon-common.toml [brain_freeze].
// No potion. Common rebuild + restart.

StartupEvents.registry('mob_effect', event => {
	const SimpleColor = Java.loadClass('dev.latvian.mods.rhino.mod.util.color.SimpleColor')

	event.create(global.effect.brain_freeze, 'basic')
		.displayName(Text.of('Brain Freeze'))
		.harmful()
		.color(new SimpleColor(0x8cb4f0)) // powdered snow blue
})
