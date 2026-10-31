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

// Pickled Sea Pickle (TODO dishes/pickles-and-ferments #1): sea pickles from the Pickling Jar, no honey (recipe in
// data/creative_delight/kubejs/vintagedelight/recipes.js). It replaces Brewin' and Chewin's Pickled Pickles, which merge
// into it (the pair is in item_merges.js here; recipes, loot and inventories swap). Values: the Pickled
// Pickles' 4 hunger, modifier 0.3, eaten fast like a Pickle. Art: Vintage's pickle (GPL) in the sea pickle's colours
// (textureswapper/palette_swap.py), kubejs/assets/creativeengineering/textures/item/pickled_sea_pickle.png (the owner's pick).
StartupEvents.registry('item', e => {
	e.create(global.item.pickled_sea_pickle).displayName('Pickled Sea Pickle').food(f => {
		f.hunger(4).saturation(0.3).fastToEat(true)
	})
})

// One salt (TODO food/seasoning #6): Vintage's pink salt dust is the pack's salt, so The Salt's salt merges into it;
// The Salt's Salt Block stays (its shape in Vintage's colours, textureswapper/salt/make_salt.py) and Vintage's merges
// into it. The salt bucket is gone (no water bucket -> salt bucket smelt). The pairs: item_merges.js here; server
// half: data/creative_delight/kubejs/vintagedelight/recipes.js.
global.removedItems.push(global.item.salt_bucket)

// Jams are mason jars only (TODO dishes/jams #1, #3, owner 2026-10-29): the jam and relish bottles are out, and Vintage's
// sweet berry and glow berry jams go too (Fruits Delight has both). Hidden in JEI, tags stripped; their recipes are
// removed in data/creative_delight/kubejs/vintagedelight/jams.js. The pepper jam jar and the relish jar stay; the relish
// jar is an ingredient, not a jam. The grape and cherry jam jars are pack additions to the Vintage port.
global.removedItems.push(global.item.sweet_berry_jam_bottle, global.item.glow_berry_jam_bottle, global.item.pepper_jam_bottle,
	global.item.relish_bottle, global.item.sweet_berry_jam_jar, global.item.glow_berry_jam_jar)
