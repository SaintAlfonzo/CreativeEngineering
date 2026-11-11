// Plantable item tags, drawn as tooltip lines by client_scripts/plantable_tooltip.js. Every item that puts a growing
// plant in the world gets one family tag; each family tag also sits in #creativecommon:plantable.
//   #creativecommon:plantable/seed           "Seed Item"       seeds, pips, beans: plant a crop, not food
//   #creativecommon:plantable/crop           "Plantable Crop"  a food that plants its own crop (carrot, berries)
//   #creativecommon:plantable/sapling        "Sapling"         saplings, propagules, azaleas
//   #creativecommon:plantable/fungi_sapling  "Fungi Sapling"   mushrooms and fungi that bone meal grows huge
//   #creativecommon:plantable/water          "Water Plant"     kelp, seagrass, sea pickle, lily pad
//   #creativecommon:plantable/vine           "Vine"            vines, ivy, twisting and weeping vines
//   #creativecommon:plantable/vertical       "Vertical Crop"   sugar cane, cactus, bamboo, chorus flower
//
// Membership comes from the item's block class (any mod's subclass counts), so a new mod's plants are covered with no
// list to keep. Flowers, grass, ferns, leaves and coral get no tag (owner decision 2026-11-01).
// Rules, first match wins:
//   1. OVERRIDES below (items whose class says nothing, or says the wrong thing)
//   2. block class: sapling, fungi sapling, water plant, vertical crop
//   3. crop blocks (CropBlock, StemBlock, nether wart, cocoa, sweet berry, cave vines), or any other BushBlock /
//      VineBlock placed by an item with its own name (wheat_seeds -> wheat): seed, or crop when the item is food
//      and its id has no "seed" / "pip".
//      Botania petals (they place buried petals) are skipped here.
//   4. remaining VineBlock / GrowingPlantHeadBlock: vine
// Reference: feature_writeup/plantable.md

ServerEvents.tags('item', e => {
	var t = global.tags
	var i = global.item
	var tagOf = (key, name) => i[key] || ('#creativecommon:plantable/' + name)
	var TAG = {
		seed: tagOf('t_plantable_seed', 'seed'),
		crop: tagOf('t_plantable_crop', 'crop'),
		sapling: tagOf('t_plantable_sapling', 'sapling'),
		fungi_sapling: tagOf('t_plantable_fungi_sapling', 'fungi_sapling'),
		water: tagOf('t_plantable_water', 'water'),
		vine: tagOf('t_plantable_vine', 'vine'),
		vertical: tagOf('t_plantable_vertical', 'vertical'),
	}

	// family -> item_dict keys; 'none' takes an item out of every family
	var OVERRIDES = {
		seed: ['adzuki_beans', 'tobacco_seeds', 'farmersdelight_rice'],  // adzuki beans turn dirt into adzuki soil
		crop: ['mulberry', 'chorus_succulent'],
		sapling: ['pineapple_sapling', 'banana_frond'],
		fungi_sapling: ['glow_shroom'],
		water: ['sea_grape_plant', 'blue_pickerelweed', 'purple_pickerelweed'],
		vine: ['ivy', 'vanilla_pods'],
		vertical: ['propelplant_cane'],
		none: ['weeds'],
	}

	var C = n => Java.loadClass('net.minecraft.world.level.block.' + n)
	var $BlockItem = Java.loadClass('net.minecraft.world.item.BlockItem')
	var $Reg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	var BY_CLASS = [
		['sapling', [C('SaplingBlock'), C('AzaleaBlock')]],  // MangrovePropaguleBlock is a SaplingBlock
		['fungi_sapling', [C('MushroomBlock'), C('FungusBlock')]],
		['water', [C('KelpBlock'), C('SeagrassBlock'), C('TallSeagrassBlock'), C('SeaPickleBlock'), C('WaterlilyBlock')]],
		['vertical', [C('SugarCaneBlock'), C('CactusBlock'), C('BambooBlock'), C('BambooSaplingBlock'), C('ChorusFlowerBlock')]],
	]
	var CROP = [C('CropBlock'), C('StemBlock'), C('NetherWartBlock'), C('CocoaBlock'), C('SweetBerryBushBlock'), C('CaveVinesBlock')]
	var $Bush = C('BushBlock'), $Vine = C('VineBlock'), $GrowingHead = C('GrowingPlantHeadBlock')
	var isAny = (obj, classes) => classes.some(c => obj instanceof c)
	var SEED_NAME = /:.*(seed|pip)/  // edible corn_delight:corn_seeds still reads "Seed Item"

	var forced = {}
	Object.keys(OVERRIDES).forEach(fam => OVERRIDES[fam].forEach(key => {
		if (i[key]) forced[i[key]] = fam
		else console.warn('[plantable] no item_dict key i.' + key)
	}))
	var petals = {}
	t._resolveIds(e, [i.t_petals]).forEach(id => { petals[id] = true })

	var lists = {}
	Object.keys(TAG).forEach(fam => { lists[fam] = [] })
	var it = $Reg.ITEMS.getKeys().iterator()
	while (it.hasNext()) {
		var rl = it.next()
		var id = String(rl)
		var fam = forced[id]
		if (!fam) {
			var item = $Reg.ITEMS.getValue(rl)
			if (!(item instanceof $BlockItem)) continue
			var block = item.getBlock()
			for (var n = 0; n < BY_CLASS.length && !fam; n++) if (isAny(block, BY_CLASS[n][1])) fam = BY_CLASS[n][0]
			if (!fam) {
				var named = id !== String($Reg.BLOCKS.getKey(block))
				if (isAny(block, CROP) || (named && (block instanceof $Bush || block instanceof $Vine) && !petals[id])) {
					var edible = false
					try { edible = !!item.isEdible() } catch (err) {}
					fam = edible && !SEED_NAME.test(id) ? 'crop' : 'seed'
				} else if (block instanceof $Vine || block instanceof $GrowingHead) fam = 'vine'
			}
		}
		if (fam && fam !== 'none') lists[fam].push(id)
	}

	var counts = []
	Object.keys(TAG).forEach(fam => {
		t.addTag(e, TAG[fam], lists[fam])
		counts.push(fam + '=' + lists[fam].length)
	})
	console.log('[plantable] ' + counts.join(', '))
})
