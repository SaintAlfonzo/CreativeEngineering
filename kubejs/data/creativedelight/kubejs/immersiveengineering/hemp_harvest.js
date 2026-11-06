// requires: immersiveengineering
// Hemp right-click harvest (TODO farming/crops #37, crops.md 2.1). Immersive Engineering's hemp is a two-block BushBlock
// (growth bottom0..bottom4, then a top0 block above bottom4) with no right-click of its own, and Quark's harvest search
// skips it (not a CropBlock, and a Harvestable Blocks line cannot express "break the top": the replant state would
// have to be air). Here an empty hand or a hoe on a ripe plant (either half) breaks the top for its loot (hemp fibre,
// plus soil yield); the bottom stays at bottom4 and grows a new top. A hoe covers the per-hoe area of Creative Delight's
// HoeHarvestArea (the same square as Quark's hoe harvest of other crops) and costs 1 durability per click when anything
// was harvested, like Quark's.
// Gotcha: if the clicked block is a Quark-harvestable crop, Quark's hoe area handles the click and hemp in that area is
// left alone (and the other way round when hemp is clicked).

BlockEvents.rightClicked(global.blocks.hemp, e => {
	if (!global.CD) return
	if (String(e.getHand()) !== 'MAIN_HAND') return
	var player = e.player
	var stack = e.item
	var isHoe = !stack.isEmpty() && Ingredient.of(global.item.any_hoe).test(stack)
	if (!stack.isEmpty() && !isHoe) return

	var level = e.getLevel()
	var HEMP = global.blocks.hemp

	// Position of a ripe top at or above pos, or null
	function ripeTop(pos) {
		var block = level.getBlock(pos)
		if (String(block.id) !== HEMP) return null
		var growth = String(block.properties.get('growth'))
		if (growth === 'top0') return pos
		if (growth === 'bottom4') {
			var up = level.getBlock(pos.above())
			if (String(up.id) === HEMP && String(up.properties.get('growth')) === 'top0') return pos.above()
		}
		return null
	}

	var centre = e.block.getPos()
	if (!ripeTop(centre)) return

	// The clicked plant's bottom block is the area's centre row
	var base = String(e.block.properties.get('growth')) === 'top0' ? centre.below() : centre
	var area = null
	if (isHoe) {
		try {
			area = Java.loadClass('com.SaintAlfonzo.creativedelight.crops.HoeHarvestArea').of(player, stack)
		} catch (err) {
			area = null
		}
	}
	var minX = area ? area.minX() : 0, maxX = area ? area.maxX() : 0
	var minZ = area ? area.minZ() : 0, maxZ = area ? area.maxZ() : 0

	var harvested = 0
	for (var x = minX; x <= maxX; x++) {
		for (var z = minZ; z <= maxZ; z++) {
			var top = ripeTop(base.east(x).south(z)) // east/south: one int overload each (offset is ambiguous in Rhino)
			if (!top) continue
			level.destroyBlock(top, true, player)
			harvested++
		}
	}
	if (!harvested) return

	if (isHoe && !player.isCreative()) {
		try {
			if (Java.loadClass('vazkii.quark.content.tweaks.module.SimpleHarvestModule').harvestingCostsDurability) {
				stack.hurtAndBreak(1, player, p => p.broadcastBreakEvent(e.getHand()))
			}
		} catch (err) {
			// Quark missing or field renamed: no durability cost
		}
	}
	try { player.swing(e.getHand(), true) } catch (err) { /* arm swing is cosmetic */ }
	e.cancel()
})
