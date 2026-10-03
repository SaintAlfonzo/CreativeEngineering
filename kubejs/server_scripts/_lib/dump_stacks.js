// /dumpstacks   ->  writes kubejs/exported/stacks/stack_sizes.json
//
//   Every item whose max stack size (as the game sees it now, after KubeJS / config edits) is not 64:
//   stack size, the item's own base value when a stack-aware override differs, gear kind (armor /
//   tool / damageable, so the writeup can leave unstackable gear out), edible / use animation,
//   crafting remainder, block item, class chain and tags.
//
// tools/stack_sizes.py reads it and builds feature_writeup/stack_sizes.md.

ServerEvents.commandRegistry(event => {
	var { commands: Commands } = event
	event.register(
		Commands.literal('dumpstacks')
			.requires(src => src.hasPermission(2))
			.executes(ctx => {
				var g = guardThrowable(() => dumpStacks())
				if (g.ok) {
					var r = g.value
					ctx.source.sendSuccess(Text.green(`Dumped ${r.listed} of ${r.total} items (stack size not 64) to kubejs/exported/stacks/`), false)
				} else {
					console.error('[dumpstacks] failed: ' + g.error)
					ctx.source.sendFailure(Text.red('dumpstacks failed - see log: ' + g.error))
				}
				return 1
			})
	)
})

function dumpStacks() {
	var $Reg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	var $BlockItem = Java.loadClass('net.minecraft.world.item.BlockItem')
	var GEAR_CLASSES = {
		armor: ['ArmorItem', 'HorseArmorItem', 'ElytraItem'],
		tool: ['TieredItem', 'ProjectileWeaponItem', 'ShieldItem', 'TridentItem', 'FishingRodItem',
			'ShearsItem', 'FlintAndSteelItem', 'FoodOnAStickItem'],
	}
	var gearClasses = {}
	Object.keys(GEAR_CLASSES).forEach(kind => {
		gearClasses[kind] = []
		GEAR_CLASSES[kind].forEach(n => {
			try { gearClasses[kind].push(Java.loadClass('net.minecraft.world.item.' + n)) } catch (e) { console.warn('[dumpstacks] no class ' + n) }
		})
	})
	var $Wearable = null
	try { $Wearable = Java.loadClass('net.minecraft.world.item.Wearable') } catch (e) {}

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

	var gearOf = (item, stack) => {
		var kinds = Object.keys(gearClasses)
		for (var k = 0; k < kinds.length; k++) {
			if (gearClasses[kinds[k]].some(c => item instanceof c)) return kinds[k]
		}
		if ($Wearable && item instanceof $Wearable) return 'armor'
		var dmg = 0
		try { dmg = stack.getMaxDamage() } catch (e) {}
		return dmg > 0 ? 'damageable' : null
	}

	var remainderOf = (item, stack) => {
		var g = guardThrowable(() => {
			var r = item.getCraftingRemainingItem(stack)
			return (r && !r.isEmpty()) ? keyOf($Reg.ITEMS, r.getItem()) : null
		})
		return (g.ok && g.value && g.value !== 'minecraft:air') ? g.value : null
	}

	var keys = []
	$Reg.ITEMS.getKeys().forEach(rl => keys.push(rl))
	keys.sort((a, b) => String(a) < String(b) ? -1 : String(a) > String(b) ? 1 : 0)

	var rows = [], total = 0, skipped = 0
	keys.forEach(rl => {
		var id = String(rl)
		if (id === 'minecraft:air') return
		total++
		var g = guardThrowable(() => {
			var item = $Reg.ITEMS.getValue(rl)
			var stack = Item.of(id)
			var size = stack.getMaxStackSize()
			if (size === 64) return
			var row = { id: id, mod: id.substring(0, id.indexOf(':')), name: id, stackSize: size }
			try { row.name = String(stack.getHoverName().getString()) } catch (e) {}
			var base = guardThrowable(() => item.getMaxStackSize())
			if (base.ok && base.value != null && base.value !== size) row.baseStackSize = base.value
			row.gear = gearOf(item, stack)
			try { var md = stack.getMaxDamage(); if (md > 0) row.maxDamage = md } catch (e) {}
			try { row.edible = !!item.isEdible() } catch (e) { row.edible = false }
			try { row.useAnim = String(item.getUseAnimation(stack)) } catch (e) {}
			row.remainder = remainderOf(item, stack)
			row.blockItem = item instanceof $BlockItem
			row.classes = classChain(item)
			if (row.blockItem) row.blockClasses = classChain(item.getBlock())
			row.tags = (tagsByItem[id] || []).sort()
			rows.push(row)
		})
		if (!g.ok) { skipped++; console.warn(`[dumpstacks] skipped ${id}: ${g.error}`) }
	})

	writeExportJson('kubejs/exported/stacks/stack_sizes.json', { total: total, listed: rows.length, skipped: skipped, items: rows })
	console.log(`[dumpstacks] ${rows.length} of ${total} items have a stack size other than 64 (${skipped} skipped)`)
	return { total: total, listed: rows.length }
}
