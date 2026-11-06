// requires: vintagedelight
// Jams (TODO dishes/jams #1, #3, #4; owner 2026-10-29; feature_writeup/briefs/dishes_jams.md). Jams are mason jars
// only: the jam and relish bottles and Vintage's sweet berry and glow berry jams are removed items (CD startup
// vintagedelight/vintage_items.js); their recipes go here. The relish jar stays as an ingredient
// (#creativeengineering:food_type/ingredient), not a jam. New mason jar jams: green grape, red grape, cherry, salmonberry and raspberry (pack
// additions to the Vintage port), from the pot: 4 fruit + sugar + lemon slice -> 1 jar (dishes/jams #2 pattern; 2 jars until
// dishes/jams #5, 2026-11-02), in an empty mason jar. Slice & Dice copies the pot recipe to the heated mixer.
ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('vintagedelight')) return
	var t = global.tags
	var i = global.item

	// Relish is a condiment, not a jam: out of the jam tags (and so out of jam-into-vinegar fermenting)
	t.remove(e, [i.t_jam_jars, i.t_savory_jam], i.relish_jar)
	// The new jars are jams for every jam slot (nut butter and jam sandwich, PB and J)
	t.add(e, i.t_forge_jams, [i.green_grape_jam_jar, i.red_grape_jam_jar, i.cherry_jam_jar, i.salmonberry_jam_jar, i.raspberry_jam_jar])
	// Pack jam line (dishes/jams #8): the new jars are jams, the bowls of jello are jellos (Fruits Delight's tag), the nut
	// milk jar is milk as the bottle is
	t.add(e, i.t_forge_jams, [i.ancient_fruit_jam_jar, i.banana_jam_jar, i.mulberry_jam_jar, i.strawberry_jam_jar])
	if (global.CD.on('fruitsdelight')) t.add(e, i.t_fruitsdelight_jello, packJams().map(j => j.jello))
	t.add(e, i.milk, i.nut_milk_jar)
})

// Pack jam line (TODO dishes/jams #8, owner 2026-11-03; Vintage port jam/PackJams.java). Fruits Delight's jam family for
// the jams it does not have: jar, jam block, jello block, bowl of jello, jam and melted jello fluids, cauldrons (Java).
// fruit: the pot and mixer fruit (null: the jar keeps its own pot recipe); on: the mods the fruit needs.
function packJams() {
	var i = global.item
	var f = global.fluid
	var on = id => Platform.isLoaded(id)
	// G5 begin: a row is used only while its ok is true (filter below); its ids then reach NCH and global.tags only
	return [
		{ n: 'ancient_fruit', fruit: i.ancient_fruit, ok: on('quark'), jar: i.ancient_fruit_jam_jar, block: i.ancient_fruit_jam_block,
			jelloBlock: i.ancient_fruit_jello_block, jello: i.ancient_fruit_jello, jam: f.ancient_fruit_jam, melted: f.ancient_fruit_jello },
		{ n: 'banana', fruit: i.t_fruits_banana, ok: true, jar: i.banana_jam_jar, block: i.banana_jam_block,
			jelloBlock: i.banana_jello_block, jello: i.banana_jello, jam: f.banana_jam, melted: f.banana_jello },
		{ n: 'cherry', fruit: i.cherry, ok: on('vinery'), jar: i.cherry_jam_jar, block: i.cherry_jam_block,
			jelloBlock: i.cherry_jello_block, jello: i.cherry_jello, jam: f.cherry_jam, melted: f.cherry_jello, potDone: true },
		{ n: 'green_grape', fruit: i.t_white_grapes, ok: on('vinery'), jar: i.green_grape_jam_jar, block: i.green_grape_jam_block,
			jelloBlock: i.green_grape_jello_block, jello: i.green_grape_jello, jam: f.green_grape_jam, melted: f.green_grape_jello, potDone: true },
		{ n: 'mulberry', fruit: i.mulberry, ok: on('upgrade_aquatic'), jar: i.mulberry_jam_jar, block: i.vintagedelight_mulberry_jam_block,
			jelloBlock: i.mulberry_jello_block, jello: i.mulberry_jello, jam: on('create_central_kitchen') ? f.mulberry_jam : null,
			melted: f.mulberry_jello },
		// Pepper jam keeps Vintage's pot recipe (5 pickled peppers + sugar); the mixer takes the same
		{ n: 'pepper', fruit: i.pickled_pepper, fruitCount: 5, noLemon: true, ok: true, jar: i.pepper_jam_jar, block: i.pepper_jam_block,
			jelloBlock: i.pepper_jello_block, jello: i.pepper_jello, jam: f.pepper_jam, melted: f.pepper_jello, potDone: true },
		{ n: 'raspberry', fruit: i.t_fruits_raspberries, ok: global.CD.on('hearthandharvest'), jar: i.raspberry_jam_jar,
			block: i.raspberry_jam_block, jelloBlock: i.raspberry_jello_block, jello: i.raspberry_jello, jam: f.raspberry_jam,
			melted: f.raspberry_jello, potDone: true },
		{ n: 'red_grape', fruit: i.t_red_grapes, ok: on('vinery'), jar: i.red_grape_jam_jar, block: i.red_grape_jam_block,
			jelloBlock: i.red_grape_jello_block, jello: i.red_grape_jello, jam: f.red_grape_jam, melted: f.red_grape_jello, potDone: true },
		{ n: 'salmonberry', fruit: i.t_fruits_salmonberries, ok: global.CD.on('delightful'), jar: i.salmonberry_jam_jar,
			block: i.salmonberry_jam_block, jelloBlock: i.salmonberry_jello_block, jello: i.salmonberry_jello, jam: f.salmonberry_jam,
			melted: f.salmonberry_jello, potDone: true },
		{ n: 'strawberry', fruit: i.t_fruits_strawberry, ok: on('neapolitan'), jar: i.strawberry_jam_jar, block: i.strawberry_jam_block,
			jelloBlock: i.strawberry_jello_block, jello: i.strawberry_jello, jam: f.strawberry_jam, melted: f.strawberry_jello }
	]
	// G5 end
}

ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('vintagedelight')) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Removed jams #########################
		// Bottles <-> jar crafts and the two berry jam pots, then anything else that still makes a removed jam
		;['sweet_berry_jam', 'glow_berry_jam', 'pepper_jam', 'relish'].forEach(n => {
			R.remove({ id: 'vintagedelight:jam/' + n + '_jar' }, e)
			R.remove({ id: 'vintagedelight:jam/' + n + '_jar_deconstruct' }, e)
		})
		R.remove({ id: 'vintagedelight:cooking/sweet_berry_jam_jar' }, e)
		R.remove({ id: 'vintagedelight:cooking/glow_berry_jam_jar' }, e)
		;[i.sweet_berry_jam_bottle, i.glow_berry_jam_bottle, i.pepper_jam_bottle, i.relish_bottle, i.sweet_berry_jam_jar,
			i.glow_berry_jam_jar].forEach(id => R.remove({ output: id }, e))

		//######################### Mason jars only (decided 2026-11-02) #########################
		// items/jars #9: the vinegar and apple sauce bottles merge into their jars (CD startup item_merges.js). Their bottle
		// <-> jar crafts go first, or the merge's swap would turn them into jar -> 3 jars.
		;['vinegar', 'apple_sauce'].forEach(n => {
			R.remove({ id: 'vintagedelight:jam/' + n + '_jar' }, e)
			R.remove({ id: 'vintagedelight:jam/' + n + '_jar_deconstruct' }, e)
		})
		;[i.vinegar_bottle, i.apple_sauce_bottle].forEach(id => R.remove({ output: id }, e))

		// items/jars #8: honey bottle and honey jar convert 1 to 1 (were 3 bottles + a jar, and a jar + 3 bottles -> 3).
		// The bottle's remainder is the glass bottle, the jar's the empty mason jar. The honey jar stays placeable.
		R.remove({ id: 'vintagedelight:honey_jar' }, e)
		R.remove({ id: 'vintagedelight:honey_jar_deconstruct' }, e)
		R.shapeless(i.honey_jar, [i.honey_bottle, i.empty_mason_jar], e, 'honey_jar_from_bottle')
		R.shapeless(i.honey_bottle, [i.honey_jar, i.glass_bottle], e, 'honey_bottle_from_jar')

		// dishes/jams #1: bottle jams are mason jar jams. Every recipe making a Fruits Delight jam or Upgrade Aquatic's
		// mulberry jam takes an empty mason jar where it took a glass bottle (pot container, Spout, crafting), and every
		// recipe emptying one gives the jar back (Item Drain). The eat and craft remainder is the startup half
		// (vintage_items.js; mulberry: CD mixin). Script-added recipes included (orange and lemon jam from slices).
		var bottleJams = []
		if (global.CD.on('fruitsdelight')) bottleJams.push(i.t_fruitsdelight_jelly)
		if (Platform.isLoaded('upgrade_aquatic')) bottleJams.push(i.mulberry_jam_bottle)
		bottleJams.forEach(jam => {
			R.replaceInput({ output: jam }, i.glass_bottle, i.empty_mason_jar, e, true)
			R.replaceOutput({ input: jam }, i.glass_bottle, i.empty_mason_jar, e, true)
		})
		// Jar audit (plan_scratch_bugfix_1103 1.3, mod jar recipe scan 2026-11-03): Handcrafted's Berry Jam Jar (decor) was the
		// last jam recipe taking a glass bottle.
		if (Platform.isLoaded('handcrafted'))
			R.replaceInput({ id: 'handcrafted:berry_jam_jar' }, i.glass_bottle, i.empty_mason_jar, e)

		//######################### Vinegar from any jam #########################
		// TODO dishes/pickles-and-ferments #11 (2026-11-02): the jam bottle -> vinegar bottle fermenting recipe goes (its
		// jam bottles are removed items); the jar recipe takes any jam: Vintage's jam jars (apple sauce, honey, pepper
		// jam), the new mason jar jams and Fruits Delight's jams (#forge:jams). Upstream time and organic mash kept.
		R.remove({ id: 'vintagedelight:fermenting/vinegar_from_jam_bottles' }, e)
		R.remove({ id: 'vintagedelight:fermenting/vinegar_from_jam' }, e)
		R.vdPickling(i.vinegar_jar, [[i.t_jam_jars, i.t_forge_jams], i.mushrooms], e, 'vinegar_from_jam',
			{ time: 2400, secondary: Item.of(i.organic_mash, 3) })

		//######################### Grape, cherry, salmonberry and raspberry jam #########################
		// Salmonberry and raspberry (TODO dishes/jams #7, Vintage port -13): same pattern
		// (the fruit from Delightful and Hearth and Harvest, so only while those are on)
		var jamFruits = [[i.t_white_grapes, i.green_grape_jam_jar], [i.t_red_grapes, i.red_grape_jam_jar], [i.cherry, i.cherry_jam_jar]] // G5 (fdCooking only)
		if (global.CD.on('delightful')) jamFruits.push([i.t_fruits_salmonberries, i.salmonberry_jam_jar])
		if (global.CD.on('hearthandharvest')) jamFruits.push([i.t_fruits_raspberries, i.raspberry_jam_jar])
		jamFruits.forEach(p => {
				var fruit = p[0]
				R.fdCooking([fruit, fruit, fruit, fruit, i.any_sugar, i.lemon_slice], p[1], e,
					String(p[1]).split(':')[1], 1.0, 400, i.empty_mason_jar)
			})

		//######################### Pack jam line (dishes/jams #8) #########################
		// Upgrade Aquatic's mulberry jam bottle and block merge into the mulberry jam jar and jam block (swaps
		// merges_upgrade_aquatic_jams.json). Their bottle crafts and Create Central Kitchen's bottle and 250 mB recipes go;
		// the jar follows the jam rules below. Central Kitchen's bucket <-> block and 1000 mB compacting stay.
		// G5 begin: recipe ids for R.remove
		;['upgrade_aquatic:mulberry_jam_bottle', 'upgrade_aquatic:mulberry_jam_bottle_from_block', 'upgrade_aquatic:mulberry_jam_block',
			'create_central_kitchen:crafting/mulberry_jam_bottles_from_bucket', 'create_central_kitchen:crafting/mulberry_jam_bucket',
			'create_central_kitchen:emptying/mulberry_jam_bottle', 'create_central_kitchen:filling/mulberry_jam_bottle',
			'create_central_kitchen:mixing/mulberry_jam'].forEach(id => R.remove({ id: id }, e))
		// G5 end

		// Per jam, Fruits Delight's family: pot 4 fruit + sugar + lemon slice -> 1 jar (the new jams; the older jars have
		// theirs above); 8 jars <-> 1 jam block, 8 bowls of jello <-> 1 jello block. Create: Spout and Item Drain at 125 mB
		// a jar or bowl, 1000 mB compacts to a block; heated mixing: the pot's ingredients + 100 mB water -> 125 mB jam, a
		// slime ball + 125 mB jam -> 125 mB melted jello. The cauldron chain is in the Vintage port (JamCauldrons).
		packJams().filter(j => j.ok).forEach(j => {
			var fruits = []
			for (var k = 0; k < (j.fruitCount || 4); k++) fruits.push(j.fruit)
			var pot = fruits.concat(j.noLemon ? [i.any_sugar] : [i.any_sugar, i.lemon_slice]) // G5 (pot only reaches NCH)
			if (!j.potDone) R.fdCooking(pot, j.jar, e, j.n + '_jam_jar', 1.0, 400, i.empty_mason_jar)

			var eight = (id) => [id, id, id, id, id, id, id, id]
			R.shapeless(j.block, eight(j.jar), e, j.n + '_jam_block')
			R.shapeless(Item.of(j.jar, 8), [j.block].concat(eight(i.empty_mason_jar)), e, j.n + '_jam_jar_from_block')
			R.shapeless(j.jelloBlock, eight(j.jello), e, j.n + '_jello_block')
			R.shapeless(Item.of(j.jello, 8), [j.jelloBlock].concat(eight(i.bowl)), e, j.n + '_jello_from_block')

			if (!Platform.isLoaded('create')) return
			R.createBottle(j.jello, j.melted, e, j.n + '_jello', { container: i.bowl, mb: 125 })
			R.createCompacting(j.jelloBlock, Fluid.of(j.melted, 1000), e, j.n + '_jello_block')
			if (!j.jam) return
			R.createBottle(j.jar, j.jam, e, j.n + '_jam_jar', { container: i.empty_mason_jar, mb: 125 })
			if (j.n !== 'mulberry') R.createCompacting(j.block, Fluid.of(j.jam, 1000), e, j.n + '_jam_block')
			R.createMixing(Fluid.of(j.jam, 125), pot.concat([Fluid.of(global.fluid.water, 100)]), e, j.n + '_jam', 'heated')
			R.createMixing(Fluid.of(j.melted, 125), [i.slime, Fluid.of(j.jam, 125)], e, j.n + '_jello', 'heated')
		})

		//######################### Nut milk jar (dishes/jams #8) #########################
		// The bottle's pot recipe with an empty mason jar; bottle and jar convert 1 to 1 (as honey)
		R.fdCooking([i.peanut, i.peanut, i.sugar], i.nut_milk_jar, e, 'nut_milk_jar', 2.0, 400, i.empty_mason_jar)
		R.shapeless(i.nut_milk_jar, [i.nut_milk_bottle, i.empty_mason_jar], e, 'nut_milk_jar_from_bottle')
		R.shapeless(i.nut_milk_bottle, [i.nut_milk_jar, i.glass_bottle], e, 'nut_milk_bottle_from_jar')
	})
})
