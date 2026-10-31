// /dumpcropgrowth   ->  writes kubejs/exported/crops/crop_growth_audit.json
//
//   Every block that looks like a growing plant (an age/stage-style blockstate property, or a
//   CropBlock / StemBlock / BushBlock / BonemealableBlock class), with its non-boolean properties, class
//   chain and whether WTHIT's own PlantProvider already prints "Growth: X% / Mature" for it.
//
//   WTHIT 5.36.1 PlantProvider shows the line for: any CropBlock subclass; the vanilla melon and pumpkin
//   stems, nether wart, cocoa and sweet berry bush by exact block (a modded stem or berry bush class gets
//   nothing, because the check is `block == Blocks.X`, not instanceof).
//
// `item` (the block's item, the seed for most crops) feeds tools/crop_season_audit.py.
//
// The WTHIT plugin (Creative Delight compat/wthit/WthitCropGrowth, QoL-6) is driven from this audit: whatever lands in the
// "missing" list needs a row in its table.

ServerEvents.commandRegistry(event => {
	var { commands: Commands } = event
	event.register(
		Commands.literal('dumpcropgrowth')
			.requires(src => src.hasPermission(2))
			.executes(ctx => {
				var g = guardThrowable(() => dumpCropGrowth())
				if (g.ok) {
					var r = g.value
					ctx.source.sendSuccess(Text.green(`Dumped ${r.listed} growing blocks (${r.missing} without WTHIT growth) to kubejs/exported/crops/`), false)
				} else {
					console.error('[dumpcropgrowth] failed: ' + g.error)
					ctx.source.sendFailure(Text.red('dumpcropgrowth failed - see log: ' + g.error))
				}
				return 1
			})
	)
})

function dumpCropGrowth() {
	var $Reg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	var NATIVE_IDS = {
		'minecraft:melon_stem': 1, 'minecraft:pumpkin_stem': 1, 'minecraft:nether_wart': 1,
		'minecraft:cocoa': 1, 'minecraft:sweet_berry_bush': 1,
	}
	var CROP_NAMES = ['CropBlock', 'StemBlock', 'BushBlock', 'BonemealableBlock', 'IPlantable', 'SaplingBlock',
		'SweetBerryBushBlock', 'CocoaBlock', 'NetherWartBlock', 'VineBlock', 'GrowingPlantBlock', 'DoublePlantBlock']
	var AGE_NAME = /age|stage|grow|ripe|matur|berries|fruit|harvest|bloom|flower/i

	var each = (javaIterable, fn) => { var it = javaIterable.iterator(); while (it.hasNext()) fn(it.next()) }
	var simple = n => n.substring(n.lastIndexOf('.') + 1)

	var chainOf = block => {
		var out = [], ifaces = []
		var c = block.getClass()
		while (c && out.length < 14) {
			var n = String(c.getName())
			if (n === 'java.lang.Object') break
			out.push(n)
			var is = c.getInterfaces()
			for (var q = 0; q < is.length; q++) {
				var iname = String(is[q].getName())
				if (ifaces.indexOf(iname) < 0) ifaces.push(iname)
			}
			c = c.getSuperclass()
		}
		return { classes: out, interfaces: ifaces }
	}

	// every blockstate property with its type and values; booleans and the usual placement ones are dropped
	var SKIP_PROPS = { waterlogged: 1, facing: 1, half: 1, axis: 1, persistent: 1, snowy: 1, up: 1, down: 1, north: 1, south: 1, east: 1, west: 1, hanging: 1, powered: 1, lit: 1 }
	var growthProps = block => {
		var out = []
		each(block.getStateDefinition().getProperties(), p => {
			var name = String(p.getName())
			var type = simple(String(p.getClass().getName()))
			if (type === 'BooleanProperty' || SKIP_PROPS[name]) return
			var row = { name: name, type: type }
			if (type === 'IntegerProperty') {
				var min = null, max = null
				each(p.getPossibleValues(), v => {
					var n = Number(v)
					if (min === null || n < min) min = n
					if (max === null || n > max) max = n
				})
				row.min = min
				row.max = max
			} else {
				var vals = []
				each(p.getPossibleValues(), v => vals.push(String(p.getName(v))))
				row.values = vals
			}
			out.push(row)
		})
		return out
	}

	var keys = []
	$Reg.BLOCKS.getKeys().forEach(rl => keys.push(rl))
	keys.sort((a, b) => String(a) < String(b) ? -1 : String(a) > String(b) ? 1 : 0)

	var rows = [], total = 0, skipped = 0, missing = 0
	keys.forEach(rl => {
		var id = String(rl)
		total++
		var g = guardThrowable(() => {
			var block = $Reg.BLOCKS.getValue(rl)
			var ch = chainOf(block)
			var props = growthProps(block)
			var simpleNames = ch.classes.concat(ch.interfaces).map(simple)
			var cropLike = simpleNames.some(n => CROP_NAMES.indexOf(n) >= 0)
			var ageLike = props.some(p => AGE_NAME.test(p.name))
			if (!cropLike && !ageLike) return
			// a plant with no stage-like property (plain flowers, grass) can't show a percentage
			if (props.length === 0) return
			var isCrop = ch.classes.indexOf('net.minecraft.world.level.block.CropBlock') >= 0
			var native = isCrop || !!NATIVE_IDS[id]
			// the item that places it (the seed for a crop); Serene Seasons' tooltip reads this item's tags
			var itemKey = $Reg.ITEMS.getKey(block.asItem())
			var item = itemKey && String(itemKey) !== 'minecraft:air' ? String(itemKey) : null
			var row = {
				id: id, mod: id.substring(0, id.indexOf(':')), item: item, wthitNative: native,
				props: props, classes: ch.classes, interfaces: ch.interfaces,
				cropLike: cropLike, ageLike: ageLike,
			}
			rows.push(row)
			if (!native) missing++
		})
		if (!g.ok) { skipped++; console.warn(`[dumpcropgrowth] skipped ${id}: ${g.error}`) }
	})

	writeExportJson('kubejs/exported/crops/crop_growth_audit.json',
		{ total: total, listed: rows.length, missing: missing, skipped: skipped, blocks: rows })
	console.log(`[dumpcropgrowth] ${rows.length} growing blocks of ${total}; ${missing} without WTHIT growth (${skipped} skipped)`)
	return { total: total, listed: rows.length, missing: missing }
}
