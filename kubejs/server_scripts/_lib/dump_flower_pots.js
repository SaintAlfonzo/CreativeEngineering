// /dumppots   ->  writes kubejs/exported/blocks/flower_pots.json
//
//   Live facts for the flower potting audit (feature_writeup/flower_pots.md, TODO `blocks/framed-and-decor #6`):
//   fullPots     the vanilla empty pot's plant block -> potted block map (Forge FlowerPotBlock.getFullPotsView),
//                which every mod's addPlant call fills; a plant goes in the vanilla pot only when its block is here
//   pottedBlocks every FlowerPotBlock in the registry: id, its empty pot, the plant it shows, class
//   potLike      every other block whose id or class says pot / planter / flower box, with its class chain, so
//                tools/flower_pot_audit.py can tell plant holders from decorative pots
//   plants       every block item that places a plant (BushBlock, vines, cactus, bamboo, coral, chorus, IPlantable,
//                or an item in a flower / sapling / mushroom tag): block, class chain, plant interfaces, the
//                flower and plant tags it is in, and its vanilla potted block (null when it can't be potted)
//
// tools/flower_pot_audit.py reads it with the scratch probe's kubejs/exported/blocks/pot_probe.json
// (tools/flower_pot_test/pot_probe.js) and writes the matrix in feature_writeup/flower_pots.md.

ServerEvents.commandRegistry(event => {
	var { commands: Commands } = event
	event.register(
		Commands.literal('dumppots')
			.requires(src => src.hasPermission(2))
			.executes(ctx => {
				var g = guardThrowable(() => dumpFlowerPots())
				if (g.ok) {
					var r = g.value
					ctx.source.sendSuccess(Text.green(`Dumped ${r.potted} potted blocks, ${r.potLike} other pot-like blocks, `
						+ `${r.plants} plants to kubejs/exported/blocks/flower_pots.json`), false)
				} else {
					console.error('[dumppots] failed: ' + g.error)
					ctx.source.sendFailure(Text.red('dumppots failed - see log: ' + g.error))
				}
				return g.ok ? 1 : 0
			})
	)
})

function dumpFlowerPots() {
	var $Reg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	var $BlockItem = Java.loadClass('net.minecraft.world.item.BlockItem')
	var $FlowerPot = Java.loadClass('net.minecraft.world.level.block.FlowerPotBlock')
	var $Blocks = Java.loadClass('net.minecraft.world.level.block.Blocks')
	var $ImmutableMap = Java.loadClass('com.google.common.collect.ImmutableMap')

	var keyOf = (reg, obj) => { try { var k = reg.getKey(obj); return k ? String(k) : null } catch (e) { return null } }
	var each = (javaIterable, fn) => { var it = javaIterable.iterator(); while (it.hasNext()) fn(it.next()) }
	var simple = n => n.substring(n.lastIndexOf('.') + 1)
	var classChain = obj => {
		var out = []
		try {
			var c = obj.getClass()
			while (c && out.length < 10) {
				var n = String(c.getName())
				if (n === 'java.lang.Object') break
				out.push(n)
				c = c.getSuperclass()
			}
		} catch (e) {}
		return out
	}

	// plant classes by simple name (mods subclass these); a block is a plant when any is in its chain
	var PLANT_CLASSES = ['BushBlock', 'VineBlock', 'GrowingPlantBlock', 'MultifaceBlock', 'CactusBlock', 'SugarCaneBlock',
		'BambooBlock', 'BambooSaplingBlock', 'ChorusFlowerBlock', 'ChorusPlantBlock', 'BaseCoralPlantTypeBlock',
		'SporeBlossomBlock', 'HangingRootsBlock', 'BigDripleafBlock', 'BigDripleafStemBlock', 'CocoaBlock', 'StemBlock',
		'AttachedStemBlock', 'MossBlock', 'AzaleaBlock', 'MangrovePropaguleBlock']
	var IFACES = {
		plantable: 'net.minecraftforge.common.IPlantable',
		bonemealable: 'net.minecraft.world.level.block.BonemealableBlock',
		shearable: 'net.minecraftforge.common.IForgeShearable',
	}
	// java.lang.Class is blocked by KubeJS's class filter: load each interface itself and test with instanceof
	var ifaceClasses = {}
	Object.keys(IFACES).forEach(k => { guardThrowable(() => { ifaceClasses[k] = Java.loadClass(IFACES[k]) }) })

	// item -> the plant-ish tags it is in
	var TAG_RE = /(flower|sapling|mushroom|fung|plant|vine|grass|fern|crop|seed|coral|moss|cactus|bamboo|lily|leaves)/
	var tagsByItem = {}
	guardThrowable(() => {
		var tm = $Reg.ITEMS.tags()
		each(tm.getTagNames(), key => {
			var tagId = String(key.location())
			if (!TAG_RE.test(tagId)) return
			each(tm.getTag(key), it => {
				var iid = keyOf($Reg.ITEMS, it)
				if (iid) (tagsByItem[iid] = tagsByItem[iid] || []).push(tagId)
			})
		})
	})
	var POT_TAGS = /^(minecraft:(small_flowers|tall_flowers|flowers|saplings)|forge:mushrooms|botania:.*flowers)$/

	// vanilla pot: plant block -> potted block
	var fullPots = {}
	var plantToPotted = {}
	// copied into Guava first: Rhino can't call iterator() on java.util's Collections$UnmodifiableMap entry set (java.base
	// does not open java.util), Guava's own classes are in an automatic module and open
	each($ImmutableMap['copyOf(java.util.Map)']($Blocks.FLOWER_POT.getFullPotsView()).entrySet(), en => {
		var plantId = String(en.getKey())
		var potted = en.getValue().get()
		var pid = keyOf($Reg.BLOCKS, potted)
		fullPots[plantId] = pid
		plantToPotted[plantId] = pid
	})

	var pottedBlocks = []
	var potLike = []
	var POT_RE = /(^|[:_])(pot|pots|planter|planters|planter_box|flower_box|hanging_pot|flower_pot)($|_)/
	each($Reg.BLOCKS.getValues(), b => {
		var id = keyOf($Reg.BLOCKS, b)
		if (!id) return
		var chain = classChain(b)
		if (b instanceof $FlowerPot) {
			var content = null, empty = null
			guardThrowable(() => { content = keyOf($Reg.BLOCKS, b.getContent()) })
			guardThrowable(() => { empty = keyOf($Reg.BLOCKS, b.getEmptyPot()) })
			pottedBlocks.push({ id: id, emptyPot: empty, content: content, cls: simple(chain[0] || '') })
			return
		}
		var cls = chain.map(simple).join(' ')
		var path = id.substring(id.indexOf(':') + 1)
		if (POT_RE.test(':' + path) || /(FlowerPot|Planter|FlowerBox|HangingPot|LargePot)/.test(cls)) {
			var item = null
			guardThrowable(() => { var it = b.asItem(); item = it ? keyOf($Reg.ITEMS, it) : null })
			potLike.push({ id: id, item: item === 'minecraft:air' ? null : item, chain: chain.map(simple) })
		}
	})

	var plants = []
	each($Reg.ITEMS.getValues(), it => {
		if (!(it instanceof $BlockItem)) return
		var iid = keyOf($Reg.ITEMS, it)
		var b = it.getBlock()
		var bid = keyOf($Reg.BLOCKS, b)
		if (!iid || !bid) return
		var chain = classChain(b).map(simple)
		var tags = tagsByItem[iid] || []
		var plantClass = chain.some(c => PLANT_CLASSES.indexOf(c) >= 0)
		var ifs = {}
		Object.keys(ifaceClasses).forEach(k => { try { ifs[k] = b instanceof ifaceClasses[k] } catch (e) { ifs[k] = false } })
		var tagged = tags.some(t => POT_TAGS.test(t))
		if (!plantClass && !ifs.plantable && !tagged) return
		var state = b.defaultBlockState()
		var props = []
		guardThrowable(() => { each(state.getProperties(), p => props.push(String(p.getName()))) })
		plants.push({
			item: iid, block: bid, chain: chain, plantable: ifs.plantable, bonemealable: ifs.bonemealable,
			shearable: ifs.shearable, twoTall: props.indexOf('half') >= 0, props: props, tags: tags,
			vanillaPot: plantToPotted[bid] || null,
		})
	})

	writeExportJson('kubejs/exported/blocks/flower_pots.json', {
		note: 'Written by /dumppots (kubejs/server_scripts/_lib/dump_flower_pots.js); read by tools/flower_pot_audit.py',
		fullPots: fullPots, pottedBlocks: pottedBlocks, potLike: potLike, plants: plants,
	})
	return { potted: pottedBlocks.length, potLike: potLike.length, plants: plants.length }
}
