// requires: creativeequipment
// Creative Equipment; moved from kubejs/server_scripts/equipment/dragon_autorepair.js on 2026-11-04 (creative_split.md 7.6, wave 7), rows unchanged.
// Passive slow self-repair for dragon-tier gear (#creativeengineering:dragon_gear -
// server_scripts/tags/equipmenttags.js) while worn/held - the same "repairs very slowly
// over time when in use" idiom as Allurement's `allurement:reforming` enchantment, but
// built in-house rather than silently forcing that mod's real enchantment onto our gear:
// it's a REAL enchantment level (would surface in the item's enchant tooltip unless we
// then did MORE work to hide it), its rate is that mod's own config (shared with any
// real reforming book a player finds/crafts, so retuning one retunes both), and it
// stops working the moment Allurement isn't installed. ItemStack's damage is a plain
// public field via getDamageValue()/setDamageValue() - no read-only-event problem like
// magic_resistance's damage reduction had, so this needs no patcherMod/Java at all.
//
// dragon_horse_armor is not repaired: as in vanilla, HorseArmorItem has no durability
// (max damage 0). Its Regeneration comes from armor_effects.js mount().
//
// Knobs live in config/creativeengineering-common.toml under [dragon_gear.autorepair],
// via patcherMod's CEConfig (Forge's native config format - editable live through
// Create's in-game Configs screen, no restart needed).

// Rhino gotcha (documented elsewhere in this pack): ALL server_scripts share one
// top-level scope, so a same-named top-level const/var in another file silently wins
// or throws depending on load order. $EquipmentSlot/ARMOR_SLOTS (armor_effects.js) and
// $ResourceLocation (harvest.js) are already taken - every name below is prefixed to
// stay unique rather than risk a collision.

;(function () {
	let CFG = null
	try {
		CFG = Java.loadClass('com.SaintAlfonzo.creativeequipment.CEQConfig')
	} catch (err) {
		console.warn('[dragon_autorepair] Creative Equipment CEQConfig not on classpath - autorepair disabled: ' + err)
		return
	}

	const $DragonEqSlot = Java.loadClass('net.minecraft.world.entity.EquipmentSlot')

	// ItemStack.is(Item) / ItemStack.is(TagKey) are same-arity overloads of unrelated
	// types - this Rhino build can't disambiguate a raw tag argument between them
	// (see [[rhino-fieldandmethods-gotcha]]), so tag membership goes through a KubeJS
	// Ingredient's .test() (a plain unambiguous Predicate<ItemStack>) instead of .is().
	const DRAGON_GEAR_INGREDIENT = Ingredient.of('#creativeengineering:dragon_gear')
	const DRAGON_ARMOR_SLOTS = [$DragonEqSlot.HEAD, $DragonEqSlot.CHEST, $DragonEqSlot.LEGS, $DragonEqSlot.FEET]

	function repairIfDragonGear(stack, amount) {
		if (!stack || stack.isEmpty() || !DRAGON_GEAR_INGREDIENT.test(stack)) return
		if (!stack.isDamageableItem() || !stack.isDamaged()) return
		stack.setDamageValue(Math.max(0, stack.getDamageValue() - amount))
	}

	PlayerEvents.tick(event => {
		if (!CFG.DRAGON_GEAR.autorepairEnabled.get()) return
		var player = event.getPlayer()
		if (!player || player.getLevel().isClientSide()) return

		// One shared, real tick counter (the server's own) instead of a module-level
		// counter we'd have to increment ourselves - stays correct regardless of how
		// many players are online, and needs no mutable top-level state in this file.
		var intervalTicks = CFG.DRAGON_GEAR.autorepairIntervalTicks.get()
		if (player.getLevel().getServer().getTickCount() % intervalTicks !== 0) return

		var amount = CFG.DRAGON_GEAR.autorepairAmountPerInterval.get()
		DRAGON_ARMOR_SLOTS.forEach(slot => repairIfDragonGear(player.getItemBySlot(slot), amount))
		repairIfDragonGear(player.getMainHandItem(), amount)
		repairIfDragonGear(player.getOffhandItem(), amount)
	})
})()
