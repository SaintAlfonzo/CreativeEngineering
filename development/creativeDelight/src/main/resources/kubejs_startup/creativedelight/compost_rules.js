// priority: 900
// Moved from the pack's startup_scripts/compost/compost_rules.js 2026-10-25, rules unchanged. Priority 900: before
// every other Creative Delight startup file (after cd_core.js), so the tiers exist for anything that reads them at
// load time. Rule order is the order of the category() calls below, and no other script registers categories.
// Startup script in the CD jar: an edit needs a CD rebuild and restart.
// Compost rules - which items count as logically compostable, and at what chance.
// Engine and full syntax: compost.js in patcherMod's jar (development/patcherMod/src/main/resources/kubejs_startup/creativeengineering/func/). Per-item chances: <mod>/compost_values.js (generated) and <mod>/compost.js in this tree.
//
// Categories are checked top to bottom; the FIRST one an item matches gives its chance. Anything
// listed in a values file skips the rules and uses its own chance. Exclusions beat both.
// Chance = a number 0-1 or a tier name below; one "layer" of the composter per success, 7 to fill.
// /dumpcompost shows the result.

;(function () {
	var i = global.item
	var C = global.compost

	// Vanilla's five levels (ComposterBlock.bootStrap)
	C.tiers({
		low: 0.3,        // seeds, saplings, leaves, grass
		medium: 0.5,     // tall grass, vines, cactus, melon slice
		high: 0.65,      // flowers, crops, mushrooms, lily pad
		very_high: 0.85, // bread, baked potato, hay, mushroom blocks
		full: 1.0,       // cake, pumpkin pie
	})

	// ── categories, first match wins ────────────────────────────────────────

	// Seeds first: planting items that are not food. Carrots and potatoes also plant a crop, but
	// they are edible, so they fall through to crops.
	C.category('seeds', 'low', {
		tags: [i.t_seeds, i.t_seeds_c],
		ids: [/:(.*_)?seeds?(_|$)/],
		blocks: ['CropBlock', 'StemBlock'],
		edible: false,
		except: [
			/_bag$/,            // rusticdelight seed bags (storage blocks)
			/seed_pouch/,       // quark seed pouch
			/seed_oil/,         // createaddition seed oil bucket
		],
	})

	C.category('saplings', 'low', {
		tags: [i.t_saplings],
		ids: [/:(.*_)?(sapling|propagule)$/],
		blocks: ['SaplingBlock', 'MangrovePropaguleBlock'],
	})

	C.category('leaves', 'low', {
		tags: [i.t_leaves],
		ids: [/(_leaves|_leaf_pile|_leaf_carpet)$/],
		blocks: ['LeavesBlock'],
	})

	// Every flower at full: vanilla and mod flowers, Botania's mystical, floating and functional flora,
	// petals and petal blocks, Floral Enchantments' flower bushes, vines and baskets.
	C.category('flowers', 'full', {
		tags: [i.t_flowers, i.t_petals, i.t_botania_floating_flowers],
		ids: [/:(.*_)?petals?$/, /_petal_block$/, /^floralench:.*_(bush|vine|basket)$/, /^floralench:floral_basket$/],
		blocks: ['FlowerBlock', 'TallFlowerBlock'],
	})

	C.category('mushrooms', 'high', {
		tags: [i.mushrooms],
		blocks: ['MushroomBlock', 'FungusBlock'],
	})

	// Every dough, as Farmer's Delight's wheat dough and raw pasta (owner 2026-11-01): Create, oat, cucumber, sweet
	// potato, sourdough, cookie and ginger doughs. Before crops so a dough typed as raw veggie keeps this value.
	C.category('doughs', 'very_high', {
		tags: [i.any_dough],
		ids: [/dough$/],
	})

	// Fruit, vegetables, grain, berries, nuts - raw produce.
	C.category('crops', 'high', {
		tags: [i.t_crops, i.vegetables, i.fruit, i.t_berries, i.t_berries_c, i.t_grain, i.t_nuts, i.t_tea_leaves,
			i.t_food_raw_veggie, i.t_food_raw_veggie_sliced, i.t_food_raw_fruit],
		// edible items that plant something (carrot, potato, glow berries, cocoa beans, nether wart)
		blocks: ['CropBlock', 'CocoaBlock', 'SweetBerryBushBlock', 'CaveVinesBlock', 'NetherWartBlock', 'StemGrownBlock'],
	})

	// Baked goods and cooked plant food. Meat, fish, dairy, eggs, soups and meals never match a rule.
	C.category('baked', 'very_high', {
		tags: [i.t_food_baked, i.t_food_cooked_veggie, i.t_food_cooked_veggie_sliced, i.t_food_cooked_fruit],
		ids: [/:(.*_)?(bread|cookies?)(_|$)/],
		except: [/molasses/],
	})

	C.category('cakes_pies', 'full', {
		ids: [/:(.*_)?(pie|cake)(_|$)/],
		blocks: ['CakeBlock'],
		except: [
			/blaze_cake/, /echo_cake/, /slime_cake/, /powder_cake/, // not food
			/_hat/, /molasses/, /plated/, // plated = food on a plate
		],
	})

	// Bulk plant matter: hay, straw, thatch, bark, mulch, sawdust, mushroom blocks, packed plant blocks.
	C.category('plant_matter', 'medium', {
		tags: [i.t_straw, i.any_bark],
		ids: [/:(.*_)?(sawdust|mulch|bark|straw|hay|thatch)(_|$)/, /(kelp|pickerelweed|flax|sugar_cane|cactus|vanilla_pod|wart)_block$/],
		blocks: ['HayBlock', 'HugeMushroomBlock', 'MossBlock'],
		except: [/_hat/, /creosote/, /hay_nest/, /reinforced/],
	})

	// Anything else that grows: grass, ferns, vines, kelp, cactus, cane, bamboo, lily pads, roots,
	// plus any mod plant (Forge IPlantable on the item or its block).
	C.category('plants', 'medium', {
		ids: [/:(.*_)?(grass|fern|vines?|kelp|seagrass|lichen|roots|moss)$/, /:moss_carpet$/],
		blocks: ['BushBlock', 'VineBlock', 'GrowingPlantBlock', 'CactusBlock', 'SugarCaneBlock', 'BambooBlock',
			'BambooSaplingBlock', 'ChorusFlowerBlock', 'ChorusPlantBlock', 'SporeBlossomBlock', 'BigDripleafBlock',
			'BigDripleafStemBlock', 'HangingRootsBlock', 'GlowLichenBlock', 'net.minecraftforge.common.IPlantable'],
		items: ['net.minecraftforge.common.IPlantable'],
	})

	// ── exclusions: never compostable, even when listed in a values file ─────

	C.exclude([
		/^botania:world_seed$/,                      // botania's world seed is a teleport item, not a seed
		/^botania:\w+_grass$/,                       // botania's altered grass blocks (soil, not plants)
		/muddy_mangrove_roots/, /petrified/,         // mud and stone
		/^canned_goods:/,                            // food in a metal can
		/^goldenfood:/, /:(enchanted_)?golden_(?!seeds)/, i.t_food_golden, // gold (botania's golden seeds are not)
		/:incomplete_/,                              // sequenced-assembly intermediates
		/^endercrop:/,                               // ender pearl crop
	])
})()
