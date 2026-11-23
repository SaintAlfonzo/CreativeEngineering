// Creative Common: creativecommon:wither_immunity (TODO food-tuning #47, ruling P7), an effect for
// Raspberry Pie's pack ladder and any other source (/effect give, global.armorEffects). Registered here like Magic
// Resistance (effects/magic_resistance.js); the immunity itself is Java (effect/WitherImmunityEffect.java: Wither is not
// applicable, and ticks of a Wither already present are canceled). No potion. Common rebuild + restart.

StartupEvents.registry('mob_effect', event => {
	const SimpleColor = Java.loadClass('dev.latvian.mods.rhino.mod.util.color.SimpleColor')

	event.create(global.effect.wither_immunity, 'basic')
		.displayName(Text.of('Wither Immunity'))
		.beneficial()
		.color(new SimpleColor(0xd8d2c2)) // bone white, against the Wither's near-black
})
