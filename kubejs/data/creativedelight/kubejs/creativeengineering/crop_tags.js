// requires: creativedelight
// Crop tags: crop parity soils and crops, hoe harvest area.
// Moved from the pack's server_scripts/tags/blocktags.js and equipmenttags.js 2026-11-01 (wave 4), rows
// unchanged. Only adds: running after the pack's tag scripts gives the same contents (nothing removes from these tags).
ServerEvents.tags('block', e => {
	if (!global.CD) return
	var t = global.tags
	var item = global.item

	// ── Crop parity (patcherMod crops/CropParity, feature_writeup/crops.md) ──
	// Soils that count as farmland for crops whose own check is hard-coded to vanilla farmland
	// (tobacco via mixin/TobaccoPlacementMixin). Untilled Rich Soil stays out, as for every farmland crop.
	t.add(e, 'creativeengineering:crops/farmland_soils', [item.farmland, item.rich_farmland, item.fertile_soil])
	// Non-CropBlock plants treated as farmland crops: vanilla farmland under them doesn't dry back to
	// dirt, soaked Fertile Soil bone-meals them, Quark's hoe area break includes them. Their right-click
	// replant is a separate line in config/quark-common.toml "Harvestable Blocks".
	t.add(e, 'creativeengineering:crops/farmland_crops', item.tobacco_seeds)

	// Farming batch 2026-11-07 (plan_farming_cooking_batch.md C1, Q1, Q13; briefs/farming_crops.md G8, G15, crops #4).
	// G8: Quark's right-click never harvests or replants weeds (break them instead); also stops villagers replanting them.
	if (global.has(item.weeds)) t.add(e, 'quark:simple_harvest_blacklisted', item.weeds)
	// Q1: weeds can be hand-planted on Rich Farmland. Only WeedsBlock.mayPlaceOn reads this tag; natural weed growth
	// stays on vanilla farmland only (IW block_growths owner block), so rich soils still never grow weeds by themselves.
	if (global.has(item.weeds)) t.add(e, 'immersive_weathering:fertile_blocks', item.rich_farmland)
	// G15: pineapples (planted and wild) also grow on the farmland soils, besides sand, red sand and coarse dirt.
	if (Platform.isLoaded('fruitsdelight'))
		t.add(e, 'fruitsdelight:pineapple_grows_on', '#creativeengineering:crops/farmland_soils')
})

ServerEvents.tags('item', e => {
	if (!global.CD) return
	var i = global.item

	// Hoe harvest area: side of the square Quark's hoe right-click (harvest + replant) and hoe
	// area break cover. Read by patcherMod crops.HoeHarvestArea, which also writes the
	// "Harvest area: NxN" tooltip. Any size 1x1 to 9x9 works; the largest tag wins. An even
	// area reaches one block further toward where the player looks. A hoe in none of these
	// keeps Quark's own 3x3 / 5x5 (config/quark-common.toml [tweaks.hoe_harvesting]).
	global.tags.add(e, 'creativeengineering:hoe_harvest/1x1', [i.wooden_hoe, i.stone_hoe])
	global.tags.add(e, 'creativeengineering:hoe_harvest/2x2', [i.copper_hoe, i.golden_hoe])
	global.tags.add(e, 'creativeengineering:hoe_harvest/3x3', [i.iron_hoe, i.hoe_steel])
	global.tags.add(e, 'creativeengineering:hoe_harvest/4x4',
		[i.diamond_hoe, i.netherite_hoe, i.elementium_hoe, i.manasteel_hoe])
	global.tags.add(e, 'creativeengineering:hoe_harvest/5x5', [i.echo_hoe, i.felsic_hoe, i.dragon_hoe])
})
