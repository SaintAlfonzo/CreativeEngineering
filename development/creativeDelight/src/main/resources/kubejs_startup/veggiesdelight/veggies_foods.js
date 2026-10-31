// requires: veggiesdelight
// Veggies Delight food value changes. Creative Delight startup script, loaded from the jar after the pack's
// startup_scripts (veggies_delight.md). Hunger, saturation modifier and effects not named here stay. Restart to apply.
ItemEvents.modification(e => {
	var i = global.item
	var $VeggiesRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	var $VeggiesRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
	var effect = id => $VeggiesRegistries.MOB_EFFECTS.getValue(new $VeggiesRL(id))

	// Coleslaw: 12 hunger, modifier 0.6 kept = 14.4 saturation (upstream 8 / 9.6; 11 from 2026-10-27). Rule (TODO
	// dishes/meals #42, owner 2026-10-28): both above the sum of its inputs, zucchini 3 / 2.4 + potato 1 / 0.6 +
	// carrot 3 / 3.6 + cabbage 2 / 1.6 = 9 / 8.2.
	e.modify(i.coleslaw, it => it.setFoodProperties(fb => fb.hunger(12)))

	// Fermented Garlic Honey: Regeneration II 5 s and Health Boost II 30 s (were Regeneration I 5 s, Health Boost I
	// 5 s; TODO dishes/pickles-and-ferments #10, owner 2026-10-27: Health Boost II)
	e.modify(i.fermented_garlic_honey, it => it.setFoodProperties(fb => {
		fb.removeEffect(effect('minecraft:regeneration'))
		fb.removeEffect(effect('minecraft:health_boost'))
		fb.effect('minecraft:regeneration', 5 * 20, 1, 1.0)
		fb.effect('minecraft:health_boost', 30 * 20, 1, 1.0)
	}))
})
