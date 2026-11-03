// requires: vintagedelight
// Vintage Delight item changes (TODO ports/vintageport). Creative Delight startup script, loaded from the jar after the
// pack's startup_scripts, so global.item and global.removedItems exist here. Restart to apply.

// Salted cod and salmon are out (vintageport #11): hidden in JEI, tags stripped; their crafts are removed in
// data/creativedelight/kubejs/vintagedelight/recipes.js. Surstromming stays.
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
// data/creativedelight/kubejs/vintagedelight/recipes.js). It replaces Brewin' and Chewin's Pickled Pickles, which merge
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
// half: data/creativedelight/kubejs/vintagedelight/recipes.js.
global.removedItems.push(global.item.salt_bucket)

// Jams are mason jars only (TODO dishes/jams #1, #3, owner 2026-10-29): the jam and relish bottles are out, and Vintage's
// sweet berry and glow berry jams go too (Fruits Delight has both). Hidden in JEI, tags stripped; their recipes are
// removed in data/creativedelight/kubejs/vintagedelight/jams.js. The pepper jam jar and the relish jar stay; the relish
// jar is an ingredient, not a jam. The grape and cherry jam jars are pack additions to the Vintage port.
global.removedItems.push(global.item.sweet_berry_jam_bottle, global.item.glow_berry_jam_bottle, global.item.pepper_jam_bottle,
	global.item.relish_bottle, global.item.sweet_berry_jam_jar, global.item.glow_berry_jam_jar)

// Mason jars only (decided 2026-11-02, TODO dishes/jams #1, items/jars #9): the vinegar and apple sauce bottles go too;
// their jars stay with their own food values (the owner reverted the jam-value change, 2026-11-02). The bottles merge into the jars (item_merges.js here: recipes, loot and inventories swap; the merge also lists
// them as removed items); their bottle <-> jar crafts are removed in data/creativedelight/kubejs/vintagedelight/jams.js.

// Bottle jams become mason jar jams (dishes/jams #1): Fruits Delight's 22 jams and Upgrade Aquatic's mulberry jam leave
// an empty mason jar, eaten (Fruits Delight's FDFoodItem returns its crafting remainder) or used in a craft. Upgrade
// Aquatic's eat-return is hard-coded: CD mixin upgrade_aquatic/MulberryJamMasonJarMixin. Their recipes take the jar
// instead of the glass bottle (jams.js). The Fruits Delight jam ids come from the item dict (every fruitsdelight:*_jelly).
ItemEvents.modification(e => {
	var i = global.item
	var jar = Item.of(i.empty_mason_jar).getItem()
	var jams = Object.keys(i).filter(k => /_jelly$/.test(k) && String(i[k]).indexOf('fruitsdelight:') === 0).map(k => i[k])
	if (Platform.isLoaded('upgrade_aquatic')) jams.push(i.mulberry_jam_bottle)
	jams.forEach(id => {
		if (!Platform.isLoaded(String(id).split(':')[0])) return
		e.modify(id, item => item.setCraftingRemainder(jar))
	})
})
