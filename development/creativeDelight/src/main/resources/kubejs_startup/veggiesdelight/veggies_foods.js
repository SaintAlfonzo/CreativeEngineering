// requires: veggiesdelight
// Veggies Delight food value changes. Creative Delight startup script, loaded from the jar after the pack's
// startup_scripts (veggies_delight.md). Hunger, saturation modifier and effects not named here stay. Restart to apply.
ItemEvents.modification(e => {
	var i = global.item
	var $VeggiesRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	var $VeggiesRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
	var effect = id => $VeggiesRegistries.MOB_EFFECTS.getValue(new $VeggiesRL(id))

	// Coleslaw: 11 hunger (was 8), modifier 0.6 kept (scratchpad 2026-10-27)
	e.modify(i.coleslaw, it => it.setFoodProperties(fb => fb.hunger(11)))

	// Fermented Garlic Honey: Regeneration II 5 s and Health Boost II 30 s (were Regeneration I 5 s, Health Boost I
	// 5 s; TODO dishes/pickles-and-ferments #10, owner 2026-10-27: Health Boost II)
	e.modify(i.fermented_garlic_honey, it => it.setFoodProperties(fb => {
		fb.removeEffect(effect('minecraft:regeneration'))
		fb.removeEffect(effect('minecraft:health_boost'))
		fb.effect('minecraft:regeneration', 5 * 20, 1, 1.0)
		fb.effect('minecraft:health_boost', 30 * 20, 1, 1.0)
	}))
})
