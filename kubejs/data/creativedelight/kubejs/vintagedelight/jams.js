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
})

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
		;[i.vinegar, i.apple_sauce_bottle].forEach(id => R.remove({ output: id }, e))

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
		var jamFruits = [[i.t_white_grapes, i.green_grape_jam_jar], [i.t_red_grapes, i.red_grape_jam_jar], [i.cherry, i.cherry_jam_jar]]
		if (global.CD.on('delightful')) jamFruits.push([i.t_fruits_salmonberries, i.salmonberry_jam_jar])
		if (global.CD.on('hearthandharvest')) jamFruits.push([i.t_fruits_raspberries, i.raspberry_jam_jar])
		jamFruits.forEach(p => {
				var fruit = p[0]
				R.fdCooking([fruit, fruit, fruit, fruit, i.any_sugar, i.lemon_slice], p[1], e,
					String(p[1]).split(':')[1], 1.0, 400, i.empty_mason_jar)
			})
	})
})
