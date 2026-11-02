// requires: vintagedelight
// Jams (TODO dishes/jams #1, #3, #4; owner 2026-10-29; feature_writeup/briefs/dishes_jams.md). Jams are mason jars
// only: the jam and relish bottles and Vintage's sweet berry and glow berry jams are removed items (CD startup
// vintagedelight/vintage_items.js); their recipes go here. The relish jar stays as an ingredient
// (#creativeengineering:food_type/ingredient), not a jam. New mason jar jams: green grape, red grape and cherry (pack
// additions to the Vintage port), from the pot: 4 fruit + sugar + lemon slice -> 1 jar (dishes/jams #2 pattern; 2 jars until
// dishes/jams #5, 2026-11-02), in an empty mason jar. Slice & Dice copies the pot recipe to the heated mixer.
ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('vintagedelight')) return
	var t = global.tags
	var i = global.item

	// Relish is a condiment, not a jam: out of the jam tags (and so out of jam-into-vinegar fermenting)
	t.remove(e, [i.t_jam_jars, i.t_savory_jam], i.relish_jar)
	// The new jars are jams for every jam slot (nut butter and jam sandwich, PB and J)
	t.add(e, i.t_forge_jams, [i.green_grape_jam_jar, i.red_grape_jam_jar, i.cherry_jam_jar])
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

		//######################### Vinegar from any jam #########################
		// TODO dishes/pickles-and-ferments #11 (2026-11-02): the jam bottle -> vinegar bottle fermenting recipe goes (its
		// jam bottles are removed items); the jar recipe takes any jam: Vintage's jam jars (apple sauce, honey, pepper
		// jam), the new mason jar jams and Fruits Delight's jams (#forge:jams). Upstream time and organic mash kept.
		R.remove({ id: 'vintagedelight:fermenting/vinegar_from_jam_bottles' }, e)
		R.remove({ id: 'vintagedelight:fermenting/vinegar_from_jam' }, e)
		R.vdPickling(i.vinegar_jar, [[i.t_jam_jars, i.t_forge_jams], i.mushrooms], e, 'vinegar_from_jam',
			{ time: 2400, secondary: Item.of(i.organic_mash, 3) })

		//######################### Grape and cherry jam #########################
		;[[i.t_white_grapes, i.green_grape_jam_jar], [i.t_red_grapes, i.red_grape_jam_jar], [i.cherry, i.cherry_jam_jar]]
			.forEach(p => {
				var fruit = p[0]
				R.fdCooking([fruit, fruit, fruit, fruit, i.any_sugar, i.lemon_slice], p[1], e,
					String(p[1]).split(':')[1], 1.0, 400, i.empty_mason_jar)
			})
	})
})
