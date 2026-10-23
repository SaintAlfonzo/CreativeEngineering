// priority: -100
// Item merges (startup_scripts/_lib/item_merges.js, feature_writeup/item_merges.md): the purged item's
// functionality moves to its survivor. Runs late so the swaps see every recipe the other scripts added.
//
//   recipes    every input and output naming a purged id now names the survivor; duplicates that leaves
//              behind are removed below
//   tags       survivors join the purged item's tags where a recipe or mob reads them (the purged id's own
//              tags are stripped by recipe_scan.js)
//   loot       any table entry for a purged id drops the survivor (LootJS, same count)
//   inventory  a purged item that still reaches a player (mod code, old chests, old worlds) turns into the
//              survivor, same count

ServerEvents.recipes(e => {
	var i = global.item
	var R = global.NCH

	var addedSwaps = 0
	global.itemMerges.forEach(m => {
		addedSwaps += R.replaceInput({ input: m[0] }, m[0], m[1], e, true)
		addedSwaps += R.replaceOutput({ output: m[0] }, m[0], m[1], e, true)
	})
	console.log('[itemMerges] ' + addedSwaps + ' swaps in script-added recipes')

	// Milk bottle. Abnormals Delight turns Farmer's Delight's bucket <-> bottle recipes off while Neapolitan is
	// loaded, so Neapolitan's two stay: bucket -> 3 bottles (output swapped above) and 3 #forge:bottles/milk ->
	// bucket (Farmer's Delight's bottle joins that tag below). Create's spout / drain and Delightful's matcha latte
	// already have a Farmer's Delight copy.
	R.remove({ id: 'create:filling/compat/neapolitan/milk_bottle' }, e)
	R.remove({ id: 'create:emptying/compat/neapolitan/milk_bottle' }, e)
	R.remove({ id: 'delightful:food/kettle/matcha_latte_neapolitan' }, e)

	// Chocolate cake. Abnormals Delight's 7 slices -> cake now makes Create Crafts & Additions' cake (output swapped
	// above); its other recipes are baking.js's (baking overhaul). The knife recipe is rewritten on the cake that stays.
	R.remove({ id: 'abnormals_delight:neapolitan/cutting/chocolate_cake' }, e)
	R.fdCutting(i.chocolate_cake, i.any_knife, Item.of(i.abnormals_delight_chocolate_cake_slice, 7), e, 'chocolate_cake')

	// Diced potatoes. Rustic Delight's potato -> 2 slices stays, More Delight's potato -> 1 diced goes. More
	// Delight's dishes read #forge:sliced_potatoes (tags below).
	R.remove({ id: 'moredelight:cutting/diced_potatoes' }, e)

	// Rocky dirt. Decorative Blocks' goes into Twigs'; its 4 dirt -> 1 and 1 -> 4 dirt recipes go (the second would
	// turn Twigs' cheap rocky dirt into dirt). Dave's is renamed Pebble Dirt (namechanges.js) and the two remaining
	// recipes swap blocks: pebbles make Pebble Dirt, cobblestone makes Rocky Dirt. Coarse dirt in place of dirt
	// doubles the output.
	R.remove({ id: 'decorative_blocks:rocky_dirt' }, e)
	R.remove({ id: 'decorative_blocks:dirt_from_rocky_dirt' }, e)
	R.remove({ id: 'davebuildingmod:rec_rocky_dirt' }, e)
	R.remove({ id: 'twigs:rocky_dirt' }, e)
	R.shaped(Item.of(i.rocky_dirt, 4), ['DP', 'PD'], { D: i.dirt, P: i.twigs_pebble }, e, 'pebble_dirt')
	R.shaped(Item.of(i.rocky_dirt, 8), ['DP', 'PD'], { D: i.coarse_dirt, P: i.twigs_pebble }, e, 'pebble_dirt_from_coarse_dirt')
	R.shapeless(Item.of(i.twigs_rocky_dirt, 4), [i.dirt, i.dirt, i.cobblestone, i.cobblestone], e, 'rocky_dirt')
	R.shapeless(Item.of(i.twigs_rocky_dirt, 8), [i.coarse_dirt, i.coarse_dirt, i.cobblestone, i.cobblestone], e, 'rocky_dirt_from_coarse_dirt')

	// Coffee. Rustic Delight's crop and drinks stay; Farmer's Respite's coffee cake, blazing chili and gambler's tea
	// now take Rustic's beans (swapped above). Removed: Respite's three kettle coffees and Central Kitchen's coffee
	// fluid recipes (the swap would make Rustic's coffee from the kettle and pour it into Respite's coffee fluid), and
	// the berry / bean / sack recipes the swap turns into loops or duplicates of Rustic's (beans -> beans, bag -> 9
	// beans, berries -> red dye next to Rustic's beans -> yellow dye).
	var FR_COFFEES = ['coffee', 'long_coffee', 'strong_coffee']
	FR_COFFEES.forEach(c => {
		R.remove({ id: 'farmersrespite:brewing/' + c }, e)
		R.remove({ id: 'create_central_kitchen:filling/' + c }, e)
		R.remove({ id: 'create_central_kitchen:emptying/' + c }, e)
	})
	R.remove({ id: 'create_central_kitchen:mixing/coffee' }, e)
	R.remove({ id: 'create_central_kitchen:milling/coffee_berries' }, e)
	R.remove({ id: 'farmersrespite:cutting/coffee_berries' }, e)
	R.remove({ id: 'farmersrespite:coffee_beans' }, e)
	R.remove({ id: 'farmersrespite:coffee_beans_sack' }, e)
	R.remove({ id: 'minecraft:red_dye' }, e)
	// The kettle type has no KubeJS schema, so the swap above can't edit it: gambler's tea is rebuilt on Rustic's beans.
	R.remove({ id: 'farmersrespite:brewing/gamblers_tea' }, e)
	R.frBrewing([i.coffee_beans, i.glow_berries], i.gamblers_tea, e, 'gamblers_tea')

	// Permafrost. Immersive Weathering's is the only one in the world (patcherMod QuarkPermafrostStyleMixin);
	// Quark's is Froststone and gets IW's craft with stone for dirt. Its bricks, slabs, stairs and walls keep Quark's.
	R.shapeless(Item.of(i.froststone, 2), [i.packed_ice, i.packed_ice, i.stone, i.stone], e, 'froststone')
})

ServerEvents.tags('item', e => {
	var i = global.item
	var t = global.tags
	t.add(e, i.t_crops_banana, i.neapolitan_banana)          // Alex's Mobs' monkeys read #alexsmobs:bananas
	t.add(e, i.t_bottles_milk, i.farmersdelight_milk_bottle)
	t.add(e, i.t_sliced_potatoes, i.potato_slices)
})

ServerEvents.tags('block', e => {
	// a knife on the placed cake cuts a slice off (Neapolitan put its own cake in this tag)
	global.tags.add(e, 'abnormals_delight:drops_chocolate_cake_slice', global.item.chocolate_cake)
})

LootJS.modifiers(event => {
	var mod = event.addLootTableModifier(/.*/)
	global.itemMerges.forEach(m => { mod = mod.replaceLoot(m[0], m[1], true) })
	mod.logName('CE/item_merges')
})

// Alex's Mobs' banana left a peel when eaten; Neapolitan's does now (the peel keeps its sopa de macaco recipe)
ItemEvents.foodEaten(global.item.neapolitan_banana, e => {
	if (e.player) e.player.give(global.item.banana_peel)
})

PlayerEvents.inventoryChanged(e => {
	var id = String(e.item.id)
	var merge = global.itemMerges.find(m => m[0] === id)
	if (!merge) return
	var inv = e.player.inventory
	for (var k = 0; k < inv.getContainerSize(); k++) {
		var stack = inv.getItem(k)
		if (String(stack.id) === id) inv.setItem(k, Item.of(merge[1], stack.count))
	}
})
