// JEI ingredient order — the pack's definition (helper and shape: startup_scripts/func/jei_order.js,
// reference + tests: feature_writeup/jei_ordering.md). Order of every array below = the order in JEI.
// Tags made from this: server_scripts/tags/jei_order_tags.js. Sorting: patcherMod PackIngredientOrder
// (CEConfig [jei] packOrder). Apply a change: /kubejs reload startup_scripts, /kubejs reload client_scripts,
// then /reload (tags + JEI restart).
//
// First small tree (build step 2 of the writeup): Equipment > Tools > copper / iron, the IE multiblocks,
// vanilla fluids, vanilla beneficial effects. Everything else is unplaced and keeps JEI's own order after
// the placed entries of its type, until it gets a node.
const jeiI = global.item
const jeiF = global.fluid
const jeiE = global.effect
const jeiMb = global.multiblock

global.jeiOrder.define({
	types: ['item', 'multiblock', 'fluid', 'effect'],

	modOrder: [
		'minecraft',
		'create',
		'immersiveengineering',
		'pneumaticcraft',
		'creativeengineering',
		'farmersdelight',
		'everythingcopper',
		'delightful',
		'create_things_and_misc',
	],

	trees: {
		item: [
			{ id: 'equipment', children: [
				{ id: 'tools', children: [
					{ id: 'copper', sort: ['list', 'mod', 'creative'], list: [
						jeiI.copper_sword, jeiI.copper_pickaxe, jeiI.copper_axe, jeiI.copper_shovel, jeiI.copper_hoe,
						jeiI.copper_knife, jeiI.delightful_copper_knife,
					] },
					{ id: 'iron', sort: ['list', 'mod', 'creative'], list: [
						jeiI.iron_sword, jeiI.iron_pickaxe, jeiI.iron_axe, jeiI.iron_shovel, jeiI.iron_hoe,
						jeiI.iron_knife,
					] },
				] },
			] },
		],

		multiblock: [
			{ id: 'immersiveengineering', children: [
				{ id: 'stone', sort: ['list'], list: [
					jeiMb.coke_oven, jeiMb.alloy_smelter, jeiMb.blast_furnace, jeiMb.improved_blast_furnace,
				] },
				{ id: 'machines', sort: ['list', 'name'], list: [
					jeiMb.crusher, jeiMb.sawmill, jeiMb.metal_press, jeiMb.assembler, jeiMb.auto_workbench,
					jeiMb.bottling_machine, jeiMb.squeezer, jeiMb.fermenter, jeiMb.refinery, jeiMb.mixer,
					jeiMb.arc_furnace, jeiMb.excavator, jeiMb.bucket_wheel,
				] },
				{ id: 'power_storage', sort: ['list', 'name'], list: [
					jeiMb.diesel_generator, jeiMb.lightning_rod, jeiMb.silo, jeiMb.sheetmetal_tank,
				] },
			] },
		],

		fluid: [
			{ id: 'vanilla', sort: ['list', 'mod', 'creative'], list: [jeiF.water, jeiF.lava, jeiF.milk] },
		],

		effect: [
			{ id: 'beneficial', sort: ['list', 'name'], list: [
				jeiE.speed, jeiE.haste, jeiE.strength, jeiE.jump_boost, jeiE.regeneration, jeiE.resistance,
				jeiE.fire_resistance, jeiE.water_breathing, jeiE.night_vision, jeiE.invisibility,
				jeiE.health_boost, jeiE.absorption, jeiE.saturation, jeiE.instant_health, jeiE.luck,
				jeiE.slow_falling, jeiE.conduit_power, jeiE.dolphins_grace, jeiE.hero_of_the_village,
			] },
		],
	},

	packHidden: {
		// dead placeholder fluids (server_scripts/tags/fluidtags.js strips their tags); fluid_jei.js skips them
		fluid: [jeiF.tmted_melon_juice, jeiF.tmted_tomato_sauce, jeiF.tmted_apple_cider, jeiF.thatch],
	},
})
