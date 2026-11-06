// requires: creativedevtools
// /dumpplaceable   ->  writes kubejs/exported/food/placeable.json
//
//   Everything the placeable-food catalog needs, read live after world load:
//   displayDelight   Display Delight's three item -> block maps (BlockAssociations), filled on world load
//                    from each DD block's loot drop AND the matching displaydelight:* item tag. An item
//                    is placeable through DD only when it is in a map, not merely in the tag.
//   items            every food, drink, DD-tagged item and food-looking block item: edible, use
//                    animation, block item + block id + block class chain, tags
//   blockItems       every other block item, compact (id, block class), so tools/placeable_catalog.py can
//                    widen its rules without a new dump
//
// tools/placeable_catalog.py reads it and builds feature_writeup/placeable_catalog.md plus
// kubejs/generation_scripts/food/placeable.json (the tag lists for kubejs/data/creativedelight/kubejs/creativeengineering/placeable_tags.js).

ServerEvents.commandRegistry(event => {
	var { commands: Commands } = event
	event.register(
		Commands.literal('dumpplaceable')
			.requires(src => src.hasPermission(2))
			.executes(ctx => {
				var g = guardThrowable(() => dumpPlaceable())
				if (g.ok) {
					var r = g.value
					ctx.source.sendSuccess(Text.green(`Dumped ${r.items} candidate items, ${r.dd} Display Delight links, `
						+ `${r.blockItems} other block items to kubejs/exported/food/placeable.json`), false)
				} else {
					console.error('[dumpplaceable] failed: ' + g.error)
					ctx.source.sendFailure(Text.red('dumpplaceable failed - see log: ' + g.error))
				}
				return g.ok ? 1 : 0
			})
	)
})

function dumpPlaceable() {
	var $Reg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	var $BlockItem = Java.loadClass('net.minecraft.world.item.BlockItem')
	// same words as dump_food.js FOOD_BLOCK, plus a few that catch cheese wheels, bottles and snack boxes
	var FOOD_BLOCK = /(Cake|Feast|Pie|Pizza|Pudding|Sandwich|Roast|Platter|Food|Cheese|Wheel|Bottle|Jelly|Jam|Popcorn|Nachos|Ham|Chicken|Medley|Rice|Bread|Loaf|Tart|Cookie|Wine|Drink|Booze)/

	var keyOf = (reg, obj) => { try { var k = reg.getKey(obj); return k ? String(k) : null } catch (e) { return null } }
	var each = (javaIterable, fn) => { var it = javaIterable.iterator(); while (it.hasNext()) fn(it.next()) }
	var classChain = obj => {
		var out = []
		try {
			var c = obj.getClass()
			while (c && out.length < 8) {
				var n = String(c.getName())
				if (n === 'java.lang.Object') break
				out.push(n)
				c = c.getSuperclass()
			}
		} catch (e) {}
		return out
	}
	var simple = n => n.substring(n.lastIndexOf('.') + 1)

	// item -> tags, from the live tag manager (ItemStack#getTags is shadowed by KubeJS here)
	var tagsByItem = {}
	guardThrowable(() => {
		var tm = $Reg.ITEMS.tags()
		each(tm.getTagNames(), key => {
			var tagId = String(key.location())
			each(tm.getTag(key), it => {
				var iid = keyOf($Reg.ITEMS, it)
				if (iid) (tagsByItem[iid] = tagsByItem[iid] || []).push(tagId)
			})
		})
	})

	// Display Delight: item -> DD block, per map
	var dd = { placed: {}, plate: {}, small_plate: {} }
	var ddCount = 0
	var ddLoaded = guardThrowable(() => {
		var $BA = Java.loadClass('com.jkvin114.displaydelight.init.BlockAssociations')
		var maps = { placed: $BA.blockMap, plate: $BA.plateBlockMap, small_plate: $BA.smallplateBlockMap }
		Object.keys(maps).forEach(kind => {
			each(maps[kind].entrySet(), en => {
				var iid = keyOf($Reg.ITEMS, en.getKey())
				var bid = keyOf($Reg.BLOCKS, en.getValue())
				if (iid && bid) { dd[kind][iid] = bid; ddCount++ }
			})
		})
	})
	if (!ddLoaded.ok) console.warn('[dumpplaceable] Display Delight maps not read: ' + ddLoaded.error)
	if (ddLoaded.ok && ddCount === 0) console.warn('[dumpplaceable] Display Delight maps are empty - run this after a world has loaded')

	var keys = []
	$Reg.ITEMS.getKeys().forEach(rl => keys.push(rl))
	keys.sort((a, b) => String(a) < String(b) ? -1 : String(a) > String(b) ? 1 : 0)

	var items = [], blockItems = [], total = 0, skipped = 0
	keys.forEach(rl => {
		var id = String(rl)
		if (id === 'minecraft:air') return
		total++
		var g = guardThrowable(() => {
			var item = $Reg.ITEMS.getValue(rl)
			var stack = Item.of(id)
			var tags = (tagsByItem[id] || []).sort()
			var edible = false, useAnim = null
			try { edible = !!item.isEdible() } catch (e) {}
			try { useAnim = String(item.getUseAnimation(stack)) } catch (e) {}
			var isBlock = item instanceof $BlockItem
			var block = isBlock ? item.getBlock() : null
			var blockChain = isBlock ? classChain(block) : []
			var foodBlock = blockChain.some(c => FOOD_BLOCK.test(simple(c)))
			var ddTagged = tags.some(t => t.indexOf('displaydelight:') === 0)
			var inDD = !!(dd.placed[id] || dd.plate[id] || dd.small_plate[id])

			if (!(edible || useAnim === 'DRINK' || foodBlock || ddTagged || inDD)) {
				if (isBlock) blockItems.push({ id: id, block: keyOf($Reg.BLOCKS, block), blockClass: blockChain.length ? blockChain[0] : null })
				return
			}
			var row = { id: id, mod: id.substring(0, id.indexOf(':')), name: id, edible: edible, useAnim: useAnim, blockItem: isBlock }
			try { row.name = String(stack.getHoverName().getString()) } catch (e) {}
			if (isBlock) {
				row.block = keyOf($Reg.BLOCKS, block)
				row.blockClasses = blockChain
			}
			row.classes = classChain(item)
			row.tags = tags
			items.push(row)
		})
		if (!g.ok) { skipped++; console.warn(`[dumpplaceable] skipped ${id}: ${g.error}`) }
	})

	writeExportJson('kubejs/exported/food/placeable.json', {
		total: total, skipped: skipped, ddLoaded: ddLoaded.ok && ddCount > 0,
		displayDelight: dd, items: items, blockItems: blockItems,
	})
	console.log(`[dumpplaceable] ${items.length} candidates, ${ddCount} Display Delight links, ${blockItems.length} other block items (${skipped} skipped)`)
	return { items: items.length, dd: ddCount, blockItems: blockItems.length }
}
