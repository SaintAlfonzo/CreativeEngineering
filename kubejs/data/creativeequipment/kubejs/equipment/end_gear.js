// requires: creativeequipment
// Creative Equipment. Recipe ids are creativeequipment:<type>/<suffix> (global.CEQ.recipes).
ServerEvents.recipes(e => {
global.CEQ.recipes(() => {
	console.log('end gear starting')

	const i = global.item
	const f = global.fluid
	const R = global.NCH.forPack('creativeequipment')

	// ============================== Echo ==============================

	// Defaults + config override (CEQConfig CRAFTING.<KEY>, e.g. ECHO_SWORD): [out, base, key, potion B]
	const echoRows = [
		[i.echo_sword, i.netherite_sword, 'echo_sword', global.effect.weakness],
		[i.echo_pickaxe, i.netherite_pickaxe, 'echo_pickaxe', global.effect.weakness],
		[i.echo_axe, i.netherite_axe, 'echo_axe', global.effect.weakness],
		[i.echo_shovel, i.netherite_shovel, 'echo_shovel', global.effect.weakness],
		[i.echo_hoe, i.netherite_hoe, 'echo_hoe', global.effect.weakness],
		[i.echo_helmet, i.netherite_helmet, 'echo_helmet', global.effect.night_vision],
		[i.echo_chestplate, i.netherite_chestplate, 'echo_chestplate', global.effect.night_vision],
		[i.echo_leggings, i.netherite_leggings, 'echo_leggings', global.effect.night_vision],
		[i.echo_boots, i.netherite_boots, 'echo_boots', global.effect.night_vision],
		[i.ce_echo_shield, i.ce_netherite_shield, 'echo_shield', global.effect.night_vision]
	]
	// out base key event potion A potion B lavaMb potionAMb potionBMb loops
	R.bulk('echoAssembly', echoRows.map(([out, base, key, potionB]) => {
		var echoCfg = R.cfg({potionA: 'creativecommon:darkness', potionB: potionB, lavaMb: 250, potionAMb: 250, potionBMb: 250, loops: 1}, 'CRAFTING.' + key.toUpperCase())
		return [out, base, key, e, echoCfg.potionA, echoCfg.potionB, echoCfg.lavaMb, echoCfg.potionAMb, echoCfg.potionBMb, echoCfg.loops]
	}))

	R.bulk('remove', [
		[{ id: 'create_deep_dark:sequences_assembly_sword_filling' }, e],
		[{ id: 'create_deep_dark:sequences_assembly_sword' }, e],
		[{ id: 'create_deep_dark:sequences_assembly_helmet_filling' }, e],
		[{ id: 'create_deep_dark:sequences_assembly_helmet' }, e],
		[{ id: 'create_deep_dark:sequences_assembly_chestplate_filling' }, e],
		[{ id: 'create_deep_dark:sequences_assembly_chestplate' }, e],
		[{ id: 'create_deep_dark:sequences_assembly_leggings_filling' }, e],
		[{ id: 'create_deep_dark:sequences_assembly_leggings' }, e],
		[{ id: 'create_deep_dark:sequences_assembly_boots_filling' }, e],
		[{ id: 'create_deep_dark:sequences_assembly_boots' }, e]
	])

	// ============================== Felsic ==============================

	// Felsic ingot new recipe
	R.bulk('remove', [
		[{ id: 'felsic_gear:felsic_block_recipe' }, e],
		[{ id: 'felsic_gear:felsic_ingot_recipe' }, e]
	])
	// Felsic Gear's ingot and Central Kitchen's dragon breath fluid: without either the steps would be built with air
	// or the empty fluid (scratch r1106spf, SQ-CBP-08b), so the assembly needs both mods.
	if (global.has(i.ingot_felsic, f.dragon_breath)) {
		var felsicIngotCfg = R.cfg({breathMb: 250, loops: 4}, 'CRAFTING.FELSIC_INGOT')
		var incFelsicIngot = R.incomplete('ingot_felsic')
		R.createSequencedAssembly(Item.of(i.ingot_felsic, 1), i.plate_netherite, [
			e.recipes.create.deploying(incFelsicIngot, [incFelsicIngot, i.scrap_felsic]),
			e.recipes.create.deploying(incFelsicIngot, [incFelsicIngot, i.ingot_brass]),
			e.recipes.create.filling(incFelsicIngot, [incFelsicIngot, Fluid.of(f.dragon_breath, felsicIngotCfg.breathMb)]),
			e.recipes.create.pressing(incFelsicIngot, incFelsicIngot)
		], ['ingot_felsic'], e, 'felsic_ingot_sequenced_assembly', felsicIngotCfg.loops)
	}

	// Remove existing felsic smithing
	R.bulk('remove', [
		[{ id: 'felsic_gear:felsic_helmet_recipe' }, e],
		[{ id: 'felsic_gear:felsic_chestplate_recipe' }, e],
		[{ id: 'felsic_gear:felsic_leggings_recipe' }, e],
		[{ id: 'felsic_gear:felsic_boots_recipe' }, e],
		[{ id: 'felsic_gear:felsic_sword_recipe' }, e],
		[{ id: 'felsic_gear:felsic_pickaxe_recipe' }, e],
		[{ id: 'felsic_gear:felsic_axe_recipe' }, e],
		[{ id: 'felsic_gear:felsic_shovel_recipe' }, e],
		[{ id: 'felsic_gear:felsic_hoe_recipe' }, e]
	])

	// Felsic default values + config override
	const felsicHelmet = R.cfg({potion: global.effect.magic_resistance, mb: 500, reachMb: 500, loops: 1, blows: 5}, 'CRAFTING.FELSIC_HELMET')
	const felsicChestplate = R.cfg({potion: global.effect.magic_resistance, mb: 500, reachMb: 500, loops: 1, blows: 5}, 'CRAFTING.FELSIC_CHESTPLATE')
	const felsicLeggings = R.cfg({potion: global.effect.magic_resistance, mb: 500, reachMb: 500, loops: 1, blows: 5}, 'CRAFTING.FELSIC_LEGGINGS')
	const felsicBoots = R.cfg({potion: global.effect.magic_resistance, mb: 500, reachMb: 500, loops: 1, blows: 5}, 'CRAFTING.FELSIC_BOOTS')
	const felsicSword = R.cfg({potion: global.effect.magic_resistance, mb: 250, reachMb: 250, loops: 1, blows: 3}, 'CRAFTING.FELSIC_SWORD')
	const felsicPickaxe = R.cfg({potion: global.effect.magic_resistance, mb: 250, reachMb: 250, loops: 1, blows: 3}, 'CRAFTING.FELSIC_PICKAXE')
	const felsicAxe = R.cfg({potion: global.effect.magic_resistance, mb: 250, reachMb: 250, loops: 1, blows: 3}, 'CRAFTING.FELSIC_AXE')
	const felsicShovel = R.cfg({potion: global.effect.magic_resistance, mb: 250, reachMb: 250, loops: 1, blows: 3}, 'CRAFTING.FELSIC_SHOVEL')
	const felsicHoe = R.cfg({potion: global.effect.magic_resistance, mb: 250, reachMb: 250, loops: 1, blows: 3}, 'CRAFTING.FELSIC_HOE')
	const felsicShield = R.cfg({potion: global.effect.magic_resistance, mb: 250, reachMb: 250, loops: 1, blows: 8}, 'CRAFTING.FELSIC_SHIELD')

	R.bulk('felsicAssembly', [
		[i.felsic_helmet, i.echo_helmet, 'felsic_helmet', e, felsicHelmet.potion, felsicHelmet.mb, felsicHelmet.loops, felsicHelmet.blows, felsicHelmet.reachMb],
		[i.felsic_chestplate, i.echo_chestplate, 'felsic_chestplate', e, felsicChestplate.potion, felsicChestplate.mb, felsicChestplate.loops, felsicChestplate.blows, felsicChestplate.reachMb],
		[i.felsic_leggings, i.echo_leggings, 'felsic_leggings', e, felsicLeggings.potion, felsicLeggings.mb, felsicLeggings.loops, felsicLeggings.blows, felsicLeggings.reachMb],
		[i.felsic_boots, i.echo_boots, 'felsic_boots', e, felsicBoots.potion, felsicBoots.mb, felsicBoots.loops, felsicBoots.blows, felsicBoots.reachMb],
		[i.felsic_sword, i.echo_sword, 'felsic_sword', e, felsicSword.potion, felsicSword.mb, felsicSword.loops, felsicSword.blows, felsicSword.reachMb],
		[i.felsic_pickaxe, i.echo_pickaxe, 'felsic_pickaxe', e, felsicPickaxe.potion, felsicPickaxe.mb, felsicPickaxe.loops, felsicPickaxe.blows, felsicPickaxe.reachMb],
		[i.felsic_axe, i.echo_axe, 'felsic_axe', e, felsicAxe.potion, felsicAxe.mb, felsicAxe.loops, felsicAxe.blows, felsicAxe.reachMb],
		[i.felsic_shovel, i.echo_shovel, 'felsic_shovel', e, felsicShovel.potion, felsicShovel.mb, felsicShovel.loops, felsicShovel.blows, felsicShovel.reachMb],
		[i.felsic_hoe, i.echo_hoe, 'felsic_hoe', e, felsicHoe.potion, felsicHoe.mb, felsicHoe.loops, felsicHoe.blows, felsicHoe.reachMb],
		[i.ce_felsic_shield, i.ce_echo_shield, 'felsic_shield', e, felsicShield.potion, felsicShield.mb, felsicShield.loops, felsicShield.blows, felsicShield.reachMb]
	])

	// ============================== Dragon ==============================

	// Elytra duplication
	R.elytraDuplication(i.dragon_scale, i.elytra, e, 'dragon_scale_elytra')

	// Remove default smithing
	R.bulk('remove', [
		[{ id: 'dragonloot:dragon_helmet' }, e],
		[{ id: 'dragonloot:dragon_chestplate' }, e],
		[{ id: 'dragonloot:dragon_leggings' }, e],
		[{ id: 'dragonloot:dragon_boots' }, e],
		[{ id: 'dragonloot:dragon_sword' }, e],
		[{ id: 'dragonloot:dragon_pickaxe' }, e],
		[{ id: 'dragonloot:dragon_axe' }, e],
		[{ id: 'dragonloot:dragon_shovel' }, e],
		[{ id: 'dragonloot:dragon_hoe' }, e],
		[{ id: 'dragonloot:dragon_anvil' }, e],
		[{ id: 'dragonloot:dragon_trident' }, e],
		[{ id: 'dragonloot:dragon_crossbow' }, e],
		[{ id: 'dragonloot:dragon_bow' }, e],
		[{ id: 'dragonloot:dragon_horse_armor' }, e]
	])

	// Defaults + config override (CEQConfig CRAFTING.<KEY>: DRAGON_HELMET ... DRAGON_SHIELD, AIR_BRAKE):
	// [out, base, key, potion, loops]
	const dragonRows = [
		// Armor, unique potions
		[i.dragon_helmet, i.felsic_helmet, 'dragon_helmet', 'creativecommon:long_magic_resistance', 2],
		[i.dragon_chestplate, i.felsic_chestplate, 'dragon_chestplate', 'creativecommon:long_resistance', 4],
		[i.dragon_leggings, i.felsic_leggings, 'dragon_leggings', 'minecraft:long_fire_resistance', 3],
		[i.dragon_boots, i.felsic_boots, 'dragon_boots', 'alexsmobs:long_knockback_resistance', 2],

		// Tools, regen
		[i.dragon_sword, i.felsic_sword, 'dragon_sword', 'minecraft:strong_regeneration', 3],
		[i.dragon_pickaxe, i.felsic_pickaxe, 'dragon_pickaxe', 'minecraft:strong_regeneration', 3],
		[i.dragon_axe, i.felsic_axe, 'dragon_axe', 'minecraft:strong_regeneration', 2],
		[i.dragon_shovel, i.felsic_shovel, 'dragon_shovel', 'minecraft:strong_regeneration', 1],
		[i.dragon_hoe, i.felsic_hoe, 'dragon_hoe', 'minecraft:strong_regeneration', 1],

		// Special tools, regen
		[i.dragon_crossbow, i.crossbow, 'dragon_crossbow', 'minecraft:strong_regeneration', 2],
		[i.dragon_bow, i.bow, 'dragon_bow', 'minecraft:strong_regeneration', 3],
		[i.dragon_horse_armor, i.diamond_horse_armor, 'dragon_horse_armor', 'minecraft:strong_regeneration', 3],
		[i.dragon_trident, i.trident, 'dragon_trident', 'minecraft:strong_regeneration', 2],

		// Anvil, 6x scales, resistance
		[i.dragon_anvil, i.anvil, 'dragon_anvil', 'creativecommon:long_resistance', 6],

		// Shield, resistance
		[i.ce_dragon_shield, i.ce_felsic_shield, 'dragon_shield', 'creativecommon:long_resistance', 5],

		// Air brake, slowness
		[i.air_brake, i.plate_netherite, 'air_brake', 'minecraft:strong_slowness', 2]
	]
	// out base key event potion mb loops breathMb
	R.bulk('dragonAssembly', dragonRows.map(([out, base, key, potion, loops]) => {
		var dragonCfg = R.cfg({potion: potion, breathMb: 250, mb: 250, loops: loops}, 'CRAFTING.' + key.toUpperCase())
		return [out, base, key, e, dragonCfg.potion, dragonCfg.mb, dragonCfg.loops, dragonCfg.breathMb]
	}))

	console.log('end gear done')
})
})
