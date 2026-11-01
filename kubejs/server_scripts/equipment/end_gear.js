ServerEvents.recipes(e => {
    console.log('end gear starting')

    const i = global.item
    const f = global.fluid
    const R = global.NCH

    // ============================== Echo ==============================

    // out base key event potion A potion B
    R.bulk('echoAssembly', [
        [i.echo_sword, i.netherite_sword, 'echo_sword', e, 'kubejs:darkness', 'minecraft:weakness'],
        [i.echo_pickaxe, i.netherite_pickaxe, 'echo_pickaxe', e, 'kubejs:darkness', 'minecraft:weakness'],
        [i.echo_axe, i.netherite_axe, 'echo_axe', e, 'kubejs:darkness', 'minecraft:weakness'],
        [i.echo_shovel, i.netherite_shovel, 'echo_shovel', e, 'kubejs:darkness', 'minecraft:weakness'],
        [i.echo_hoe, i.netherite_hoe, 'echo_hoe', e, 'kubejs:darkness', 'minecraft:weakness'],
        [i.echo_helmet, i.netherite_helmet, 'echo_helmet', e, 'kubejs:darkness', 'minecraft:night_vision'],
        [i.echo_chestplate, i.netherite_chestplate, 'echo_chestplate', e, 'kubejs:darkness', 'minecraft:night_vision'],
        [i.echo_leggings, i.netherite_leggings, 'echo_leggings', e, 'kubejs:darkness', 'minecraft:night_vision'],
        [i.echo_boots, i.netherite_boots, 'echo_boots', e, 'kubejs:darkness', 'minecraft:night_vision'],
        [i.ce_echo_shield, i.ce_netherite_shield, 'echo_shield', e, 'kubejs:darkness', 'minecraft:night_vision']
    ])

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
    const incFelsicIngot = R.incomplete('ingot_felsic')
    R.createSequencedAssembly(Item.of(i.ingot_felsic, 1), i.plate_netherite, [
        e.recipes.create.deploying(incFelsicIngot, [incFelsicIngot, i.scrap_felsic]),
        e.recipes.create.deploying(incFelsicIngot, [incFelsicIngot, i.ingot_brass]),
        e.recipes.create.filling(incFelsicIngot, [incFelsicIngot, Fluid.of(f.dragon_breath, 250)]),
        e.recipes.create.pressing(incFelsicIngot, incFelsicIngot)
    ], ['ingot_felsic'], e, 'felsic_ingot_sequenced_assembly', 4)

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
    const felsicHelmet = R.cfg({potion: 'creativeengineering:magic_resistance', mb: 500, reachMb: 500, loops: 1, blows: 5}, 'CRAFTING.FELSIC_HELMET')
    const felsicChestplate = R.cfg({potion: 'creativeengineering:magic_resistance', mb: 500, reachMb: 500, loops: 1, blows: 5}, 'CRAFTING.FELSIC_CHESTPLATE')
    const felsicLeggings = R.cfg({potion: 'creativeengineering:magic_resistance', mb: 500, reachMb: 500, loops: 1, blows: 5}, 'CRAFTING.FELSIC_LEGGINGS')
    const felsicBoots = R.cfg({potion: 'creativeengineering:magic_resistance', mb: 500, reachMb: 500, loops: 1, blows: 5}, 'CRAFTING.FELSIC_BOOTS')
    const felsicSword = R.cfg({potion: 'creativeengineering:magic_resistance', mb: 250, reachMb: 250, loops: 1, blows: 3}, 'CRAFTING.FELSIC_SWORD')
    const felsicPickaxe = R.cfg({potion: 'creativeengineering:magic_resistance', mb: 250, reachMb: 250, loops: 1, blows: 3}, 'CRAFTING.FELSIC_PICKAXE')
    const felsicAxe = R.cfg({potion: 'creativeengineering:magic_resistance', mb: 250, reachMb: 250, loops: 1, blows: 3}, 'CRAFTING.FELSIC_AXE')
    const felsicShovel = R.cfg({potion: 'creativeengineering:magic_resistance', mb: 250, reachMb: 250, loops: 1, blows: 3}, 'CRAFTING.FELSIC_SHOVEL')
    const felsicHoe = R.cfg({potion: 'creativeengineering:magic_resistance', mb: 250, reachMb: 250, loops: 1, blows: 3}, 'CRAFTING.FELSIC_HOE')
    const felsicShield = R.cfg({potion: 'creativeengineering:magic_resistance', mb: 250, reachMb: 250, loops: 1, blows: 8}, 'CRAFTING.FELSIC_SHIELD')

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

    // out base key event potion mb loops
    R.bulk('dragonAssembly', [
        // Armor, unique potions
        [i.dragon_helmet, i.felsic_helmet, 'dragon_helmet', e, 'creativeengineering:long_magic_resistance', 250, 2],
        [i.dragon_chestplate, i.felsic_chestplate, 'dragon_chestplate', e, 'kubejs:long_resistance', 250, 4],
        [i.dragon_leggings, i.felsic_leggings, 'dragon_leggings', e, 'minecraft:long_fire_resistance', 250, 3],
        [i.dragon_boots, i.felsic_boots, 'dragon_boots', e, 'alexsmobs:long_knockback_resistance', 250, 2],

        // Tools, regen
        [i.dragon_sword, i.felsic_sword, 'dragon_sword', e, 'minecraft:strong_regeneration', 250, 3],
        [i.dragon_pickaxe, i.felsic_pickaxe, 'dragon_pickaxe', e, 'minecraft:strong_regeneration', 250, 3],
        [i.dragon_axe, i.felsic_axe, 'dragon_axe', e, 'minecraft:strong_regeneration', 250, 2],
        [i.dragon_shovel, i.felsic_shovel, 'dragon_shovel', e, 'minecraft:strong_regeneration', 250, 1],
        [i.dragon_hoe, i.felsic_hoe, 'dragon_hoe', e, 'minecraft:strong_regeneration', 250, 1],

        // Special tools, regen
        [i.dragon_crossbow, i.crossbow, 'dragon_crossbow', e, 'minecraft:strong_regeneration', 250, 2],
        [i.dragon_bow, i.bow, 'dragon_bow', e, 'minecraft:strong_regeneration', 250, 3],
        [i.dragon_horse_armor, i.diamond_horse_armor, 'dragon_horse_armor', e, 'minecraft:strong_regeneration', 250, 3],
        [i.dragon_trident, i.trident, 'dragon_trident', e, 'minecraft:strong_regeneration', 250, 2],

        // Anvil, 6x scales, resistance
        [i.dragon_anvil, i.anvil, 'dragon_anvil', e, 'kubejs:long_resistance', 250, 6],

        // Shield, resistance
        [i.ce_dragon_shield, i.ce_felsic_shield, 'dragon_shield', e, 'kubejs:long_resistance', 250, 5],

        // Air brake, slowness
        [i.air_brake, i.plate_netherite, 'air_brake', e, 'minecraft:strong_slowness', 250, 2]
    ])

    console.log('end gear done')
})
