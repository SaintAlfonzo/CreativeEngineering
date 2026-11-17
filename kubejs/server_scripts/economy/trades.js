console.log('Hello! The trade event has started!')

var i = global.item
var f = global.fluid

// Every level, 1 to 5. Not the list [1, 5]: MoreJS 0.3.0's range wrapper reads the list's first entry for both ends,
// so [1, 5] matched level 1 only.
const $MoreJSIntRange = Java.loadClass('com.almostreliable.morejs.features.villager.IntRange')
var allLevels = new $MoreJSIntRange(1, 5)

MoreJSEvents.villagerTrades((e) => {
	// Fires at server start, long after load: re-read the dict, top-level `i` is shared by every server script
	var i = global.item
	// Villager trades go through global.trades.villager (Common's trade_helper.js); professions are global.profession keys
	var t = global.trades
	var pr = global.profession

	// Mechanical Engineer
	e.removeModdedTrades([pr.mechanical_engineer], allLevels)
	// Level 1
	t.villager(e, 'mechanical_engineer', 1, { buy: [i.emerald, 2], sell: [i.andesite_alloy, 8], uses: 8, xp: 8, priceMultiplier: 0.02 })
	t.villager(e, 'mechanical_engineer', 1, { buy: [i.coal, 20], sell: i.emerald, uses: 8, xp: 8, priceMultiplier: 0.01 })
	t.villager(e, 'mechanical_engineer', 1, { buy: i.emerald, sell: [i.plate_iron, 4], uses: 10, xp: 8, priceMultiplier: 0.02 })
	t.villager(e, 'mechanical_engineer', 1, { buy: [i.raw_zinc, 6], sell: i.emerald, uses: 10, xp: 8, priceMultiplier: 0.02 })
	t.villager(e, 'mechanical_engineer', 1, { buy: [i.emerald, 5], sell: i.wrench, uses: 3, xp: 24, priceMultiplier: 0.04 })
	// Level 2
	t.villager(e, 'mechanical_engineer', 2, { buy: [i.emerald, 3], sell: i.mechanical_bearing, uses: 8, xp: 8, priceMultiplier: 0.02 })
	t.villager(e, 'mechanical_engineer', 2, { buy: [i.emerald, 6], sell: i.goggles, uses: 3, xp: 32, priceMultiplier: 0.1 })
	t.villager(e, 'mechanical_engineer', 2, { buy: [i.cog, 14], sell: i.emerald, uses: 10, xp: 8, priceMultiplier: 0.02 })
	t.villager(e, 'mechanical_engineer', 2, { buy: [i.emerald, 3], sell: [i.water_wheel, 4], uses: 10, xp: 12, priceMultiplier: 0.1 })
	t.villager(e, 'mechanical_engineer', 2, { buy: [i.emerald, 2], sell: i.basin, uses: 10, xp: 12, priceMultiplier: 0.1 })
	t.villager(e, 'mechanical_engineer', 2, { buy: [i.emerald, 2], sell: i.depot, uses: 10, xp: 10, priceMultiplier: 0.1 })
	// Level 3
	t.villager(e, 'mechanical_engineer', 3, { buy: [i.belt_connector, 10], sell: [i.emerald, 3], uses: 10, xp: 10, priceMultiplier: 0.01 })
	t.villager(e, 'mechanical_engineer', 3, { buy: [i.emerald, 6], sell: i.stressometer, uses: 3, xp: 16, priceMultiplier: 0.01 })
	// Level 4
	t.villager(e, 'mechanical_engineer', 4, { buy: [i.emerald, 8], sell: i.speedometer, uses: 4, xp: 38, priceMultiplier: 0.01 })
	t.villager(e, 'mechanical_engineer', 4, { buy: [i.large_water_wheel, 4], sell: i.emerald, uses: 6, xp: 10, priceMultiplier: 0.01 })
	t.villager(e, 'mechanical_engineer', 4, { buy: [i.cr_chute, 3], sell: i.emerald, uses: 10, xp: 10, priceMultiplier: 0.01 })
	// Level 5
	t.villager(e, 'mechanical_engineer', 5, { buy: [i.emerald, 9], sell: [i.mechanical_roller, 2], uses: 2, xp: 24, priceMultiplier: 0.01 })
	t.villager(e, 'mechanical_engineer', 5, { buy: [i.emerald, 10], sell: i.linked_controller, uses: 3, xp: 40, priceMultiplier: 0.01 })
	t.villager(e, 'mechanical_engineer', 5, { buy: [i.emerald, 3], sell: [i.display_link, 2], uses: 8, xp: 12, priceMultiplier: 0.01 })

	//Hydraulic Engineer
	e.removeModdedTrades([pr.hydraulic_engineer], allLevels)
	// Level 1
	t.villager(e, 'hydraulic_engineer', 1, { buy: [i.emerald, 2], sell: [i.plate_copper, 8], uses: 8, xp: 8, priceMultiplier: 0.02 })
	t.villager(e, 'hydraulic_engineer', 1, { buy: [i.ingot_copper, 20], sell: i.emerald, uses: 8, xp: 8, priceMultiplier: 0.01 })
	t.villager(e, 'hydraulic_engineer', 1, { buy: [i.emerald, 2], sell: [i.cr_pipe, 6], uses: 10, xp: 8, priceMultiplier: 0.02 })
	t.villager(e, 'hydraulic_engineer', 1, { buy: [i.raw_copper, 6], sell: i.emerald, uses: 10, xp: 8, priceMultiplier: 0.02 })
	t.villager(e, 'hydraulic_engineer', 1, { buy: [i.emerald, 3], sell: i.copper_diving_helmet, uses: 3, xp: 24, priceMultiplier: 0.04 })
	// Level 2
	t.villager(e, 'hydraulic_engineer', 2, { buy: [i.emerald, 2], sell: i.copper_casing, uses: 8, xp: 8, priceMultiplier: 0.1 })
	t.villager(e, 'hydraulic_engineer', 2, { buy: [i.emerald, 4], sell: i.spout, uses: 10, xp: 12, priceMultiplier: 0.1 })
	t.villager(e, 'hydraulic_engineer', 2, { buy: [i.emerald, 2], sell: i.cr_tank, uses: 10, xp: 10, priceMultiplier: 0.1 })
	t.villager(e, 'hydraulic_engineer', 2, { buy: [i.emerald, 3], sell: [i.mechanical_pump, 2], uses: 10, xp: 10, priceMultiplier: 0.1 })
	// Level 3
	t.villager(e, 'hydraulic_engineer', 3, { buy: [i.copper_casing, 4], sell: i.emerald, uses: 10, xp: 8, priceMultiplier: 0.02 })
	t.villager(e, 'hydraulic_engineer', 3, { buy: i.emerald, sell: [i.cr_tank, 3], uses: 10, xp: 10, priceMultiplier: 0.01 })
	t.villager(e, 'hydraulic_engineer', 3, { buy: [i.emerald, 4], sell: i.mechanical_mixer, uses: 8, xp: 12, priceMultiplier: 0.01 })
	// Level 4
	t.villager(e, 'hydraulic_engineer', 4, { buy: [i.emerald, 2], sell: [i.cr_fluid_valve, 3], uses: 4, xp: 16, priceMultiplier: 0.01 })
	t.villager(e, 'hydraulic_engineer', 4, { buy: i.emerald, sell: [i.large_water_wheel, 3], uses: 6, xp: 10, priceMultiplier: 0.01 })
	// Level 5
	t.villager(e, 'hydraulic_engineer', 5, { buy: i.emerald, sell: [i.honeyed_apple, 8], uses: 3, xp: 16, priceMultiplier: 0.01 })
	t.villager(e, 'hydraulic_engineer', 5, { buy: [i.emerald, 4], sell: i.copper_diving_boots, uses: 3, xp: 16, priceMultiplier: 0.02 })
	t.villager(e, 'hydraulic_engineer', 5, { buy: [i.emerald, 7], sell: i.copper_backtank, uses: 3, xp: 16, priceMultiplier: 0.02 })

	//Chef
	e.removeModdedTrades([pr.chef], allLevels)
	t.villager(e, 'chef', 1, { buy: [i.fd_rope, 9], sell: i.emerald, uses: 12, xp: 2, priceMultiplier: 0.05 })
	t.villager(e, 'chef', 1, { buy: [i.onion_farmersdelight, 9], sell: i.emerald, uses: 12, xp: 2, priceMultiplier: 0.05 })
	t.villager(e, 'chef', 1, { buy: i.emerald, sell: i.mixed_salad, uses: 12, xp: 3, priceMultiplier: 0.05 })
	t.villager(e, 'chef', 1, { buy: i.emerald, sell: i.bacon_and_eggs, uses: 12, xp: 3, priceMultiplier: 0.05 })
	t.villager(e, 'chef', 4, { buy: [i.in_dough, 9], sell: i.emerald })

	// Butcher: Canned Goods' Canned Food trade (level 2, a Forge BasicItemListing the trade filters don't see); Canned Food
	// is removed from the pack (startup_scripts/_lib/removed_items.js). BasicItemListing ignores getOffer's arguments.
	var cannedFood = Ingredient.of(i.canned_food)
	e.forEachTrades(['minecraft:butcher'], allLevels, listings => {
		listings.removeIf(listing => {
			if (String(listing.getClass().getName()) !== 'net.minecraftforge.common.BasicItemListing') return false
			var offer = listing.getOffer(null, null)
			return offer != null && cannedFood.test(offer.getResult())
		})
	})

	//Cleric
	// ender pearl trade still present, unwanted
	e.removeVanillaTrades([pr.cleric], allLevels)
	e.removeModdedTrades([pr.cleric], allLevels)
	// Level 1 - Novice
	t.villager(e, 'cleric', 1, { buy: [i.rotten_flesh, 32], sell: i.emerald, uses: 16, xp: 2, priceMultiplier: 0.05 })
	t.villager(e, 'cleric', 1, { buy: i.emerald, sell: [i.redstone, 2], uses: 12, xp: 1, priceMultiplier: 0.05 })
	// Level 2 - Apprentice
	t.villager(e, 'cleric', 2, { buy: [i.ingot_gold, 3], sell: i.emerald, uses: 12, xp: 10, priceMultiplier: 0.05 })
	t.villager(e, 'cleric', 2, { buy: i.emerald, sell: i.lapis, uses: 12, xp: 5, priceMultiplier: 0.05 })
	// Level 3 - Journeyman
	t.villager(e, 'cleric', 3, { buy: [i.rabbit_foot, 2], sell: i.emerald, uses: 12, xp: 20, priceMultiplier: 0.05 })
	t.villager(e, 'cleric', 3, { buy: [i.emerald, 4], sell: i.glowstone_block, uses: 12, xp: 10, priceMultiplier: 0.05 })
	// Level 4 - Expert
	t.villager(e, 'cleric', 4, { buy: [i.scute, 4], sell: i.emerald, uses: 12, xp: 30, priceMultiplier: 0.05 })
	t.villager(e, 'cleric', 4, { buy: [i.glass_bottle, 9], sell: i.emerald, uses: 12, xp: 30, priceMultiplier: 0.05 })
	// Level 5 - Master
	t.villager(e, 'cleric', 5, { buy: [i.nether_wart, 22], sell: i.emerald, uses: 12, xp: 30, priceMultiplier: 0.05 })
	t.villager(e, 'cleric', 5, { buy: [i.emerald, 3], sell: i.experience_bottle, uses: 12, xp: 30, priceMultiplier: 0.05 })
	t.villager(e, 'cleric', 5, { buy: [i.emerald, 4], sell: i.echo_shard, uses: 16, xp: 6, priceMultiplier: 0.1 })

	// Farmer: Veggies Delight's produce trades are Creative Delight members #2 (kubejs/data/creativedelight/kubejs/veggiesdelight/trades.js)
})

console.log('The trade event has finished!')
