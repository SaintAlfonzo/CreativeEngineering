// requires: vintagedelight
// Vintage Delight item changes (TODO ports/vintageport). Creative Delight startup script, loaded from the jar after the
// pack's startup_scripts, so global.item and global.removedItems exist here. Restart to apply.

// Salted cod and salmon are out (vintageport #11): hidden in JEI, tags stripped; their crafts are removed in
// data/creative_delight/kubejs/vintagedelight/recipes.js. Surstromming stays.
global.removedItems.push(global.item.salted_cod, global.item.salted_salmon)

// Ghost charcoal is edible (vintageport #13): no hunger, eaten at any time, Wither I 5 s and Fire Resistance 3 min.
// Still a furnace fuel.
ItemEvents.modification(e => {
	var $GhostFoodBuilder = Java.loadClass('net.minecraft.world.food.FoodProperties$Builder')
	var $GhostEffectInstance = Java.loadClass('net.minecraft.world.effect.MobEffectInstance')
	var $GhostRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	var $GhostRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
	var mobEffect = id => $GhostRegistries.MOB_EFFECTS.getValue(new $GhostRL(id))

	e.modify(global.item.ghost_charcoal, item => {
		item.setFoodProperties(new $GhostFoodBuilder().nutrition(0).saturationMod(0).alwaysEat()
			.effect(new $GhostEffectInstance(mobEffect('minecraft:wither'), 100, 0), 1.0)
			.effect(new $GhostEffectInstance(mobEffect('minecraft:fire_resistance'), 3600, 0), 1.0)
			.build())
	})
})
