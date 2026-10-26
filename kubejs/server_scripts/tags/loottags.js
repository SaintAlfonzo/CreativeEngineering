// Loot meta tags: item groups the chest-loot rules (loot/07_rules.js) tune as one unit.
// A '#tag' rule reads the tag's members from this same reload (loot/95_tag_capture.js), so
// anything added here or nested in later is picked up by the next /reload.

var t = global.tags
var i = global.item
var b = global.blocks

ServerEvents.tags('item', e => {
	// every chalk stick (nested #chalk:chalks, so new colours follow) + the chalk box
	t.add(e, i.t_loot_chalk, [i.t_chalks, i.chalk_box])

	// golden food: golden apple / carrot + Golden Food's plain golden foods (the enchanted ones and
	// the notch apple stay out: they're the rare tier)
	t.add(e, i.t_loot_golden_food, [i.golden_apple, i.golden_carrot])
	t.addMatching(e, i.t_loot_golden_food, 'goldenfood:golden_*')
})

// Oceanic Delight fishing net (ocean_delight.md 5.1, TODO dishes/ocean #1). patcherMod
// OceanicDelightFishingNetMixin sends every block in fishing_net/kelp to the kelp table and every block in
// fishing_net/sea_grass to the sea grass table; the tables (kubejs/data/oceanic_delight/loot_tables/gameplay/)
// pick their biome drops with creativeengineering:biome_tag on the biome tags below.
ServerEvents.tags('block', e => {
	t.add(e, b.t_fishing_net_kelp, [
		i.kelp, b.kelp_plant,
		i.ochre_kelp, b.ochre_kelp_plant, i.polar_kelp, b.polar_kelp_plant,
		i.thorny_kelp, b.thorny_kelp_plant, i.tongue_kelp, b.tongue_kelp_plant,
	])
	t.add(e, b.t_fishing_net_sea_grass, [
		i.seagrass, b.tall_seagrass, i.wild_sea_grape, i.sea_grape_plant,
		i.blue_pickerelweed, i.purple_pickerelweed, b.tall_blue_pickerelweed, b.tall_purple_pickerelweed,
	])
})

ServerEvents.tags('item', e => {
	// every live coral and its fan (Upgrade Aquatic's per-type tags hold both); dead and prismarine corals stay out
	;['brain', 'bubble', 'fire', 'horn', 'tube', 'acan', 'branch', 'chrome', 'finger', 'moss', 'petal',
	  'pillow', 'rock', 'silk', 'star'].forEach(c => t.add(e, i.t_fishing_net_coral, '#upgrade_aquatic:' + c + '_fans_corals'))
})

ServerEvents.tags('worldgen/biome', e => {
	// Terralith's deep_warm_ocean is in none of the vanilla ocean tags
	t.add(e, 'creativeengineering:fishing_net/warm_ocean',
		['minecraft:warm_ocean', 'minecraft:lukewarm_ocean', 'minecraft:deep_lukewarm_ocean', 'minecraft:deep_warm_ocean'])
	t.add(e, 'creativeengineering:fishing_net/shrimp_ocean',
		['#creativeengineering:fishing_net/warm_ocean', 'minecraft:ocean', 'minecraft:deep_ocean'])
	t.add(e, 'creativeengineering:fishing_net/ocean', ['#minecraft:is_ocean', 'minecraft:deep_warm_ocean'])
	t.add(e, 'creativeengineering:fishing_net/river', ['#minecraft:is_river', 'terralith:warm_river'])
})
