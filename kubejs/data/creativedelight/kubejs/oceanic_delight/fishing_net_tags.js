// requires: creativedelight
// requires: oceanic_delight
// Oceanic Delight fishing net tags.
// Moved from the pack's server_scripts/tags/loottags.js 2026-11-01 (creative_split.md 7.6, wave 4), rows
// unchanged. Only adds: running after the pack's tag scripts gives the same contents (nothing removes from these tags).
;(function () {
	var t = global.tags
	var i = global.item
	var b = global.blocks

	// Oceanic Delight fishing net (ocean_delight.md 5.1, 5.6; TODO dishes/ocean #1). The net (Creative Delight's
	// Oceanic rebuild, FishingNetItem) searches every block in fishing_net/kelp with the kelp table and every block in
	// fishing_net/sea_grass with the sea grass table; the tables (kubejs/data/creativedelight/loot_tables/gameplay/
	// fishing_net/, shipped in the CD jar too) pick their biome drops with creativeengineering:biome_tag on the biome
	// tags below.
	ServerEvents.tags('block', e => {
		if (!global.CD || !global.CD.on('oceanic_delight')) return
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
		if (!global.CD || !global.CD.on('oceanic_delight')) return
		// every live coral and its fan (Upgrade Aquatic's per-type tags hold both); dead and prismarine corals stay out
		;['brain', 'bubble', 'fire', 'horn', 'tube', 'acan', 'branch', 'chrome', 'finger', 'moss', 'petal',
		  'pillow', 'rock', 'silk', 'star']
			.forEach(c => t.add(e, i.t_fishing_net_coral, '#upgrade_aquatic:' + c + '_fans_corals'))
	})

	ServerEvents.tags('worldgen/biome', e => {
		if (!global.CD || !global.CD.on('oceanic_delight')) return
		// Terralith's deep_warm_ocean is in none of the vanilla ocean tags
		t.add(e, 'creativeengineering:fishing_net/warm_ocean',
			['minecraft:warm_ocean', 'minecraft:lukewarm_ocean', 'minecraft:deep_lukewarm_ocean', 'minecraft:deep_warm_ocean'])
		t.add(e, 'creativeengineering:fishing_net/shrimp_ocean',
			['#creativeengineering:fishing_net/warm_ocean', 'minecraft:ocean', 'minecraft:deep_ocean'])
		t.add(e, 'creativeengineering:fishing_net/ocean', ['#minecraft:is_ocean', 'minecraft:deep_warm_ocean'])
		t.add(e, 'creativeengineering:fishing_net/river', ['#minecraft:is_river', 'terralith:warm_river'])
	})
})()
