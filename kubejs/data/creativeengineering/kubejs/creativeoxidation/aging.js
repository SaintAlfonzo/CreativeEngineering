// requires: creativeengineering
// requires: creativeoxidation
// SPDX-License-Identifier: GPL-3.0-only
// Creative Oxidation (Creative Engineering member; feature_writeup/plan_creative_oxidation.md P7): copper and iron age
// by water, one stage per recipe: unaffected -> exposed -> weathered -> oxidized (iron: rusted). Waxed items never age.
// Ids creativeoxidation:<kind>/<name>. Authored here (the Creative Engineering server script tree, one folder per
// member as Creative Brewing's members), copied into the Creative Engineering jar.
//
//   a. Spout 250 mB water on vanilla and Create copper (the 30 recipes of the dropped create_oxidized 0.1.1 jar)
//   b. Fan splashing on vanilla and Create copper (Create: Copper Splashing port, moved from
//      kubejs/server_scripts/other/feature_ports.js, recipes unchanged; ids were creativecompat:createsplashing/)
//   c. Staged Create, Create Deco and Design n' Decor copper blocks: spout and fan splashing with the
//      creativeoxidation:staged input (R10: "unaffected" matches only an item with no stage tag) and the stage in the
//      result's NBT (CreativeOxidationStage); 32 items x 3 stages x 2 = 192 recipes
//   d. Iron rusting (I11): Immersive Weathering's chain plain -> exposed -> weathered -> rusted for cut and plate iron
//      (block, slab, stairs, vertical slab) and iron bars, door, trapdoor: spout and fan splashing, 33 x 2 = 66 recipes
ServerEvents.recipes(e => {
	var i = global.item
	var f = global.fluid
	var R = global.NCH.forPack('creativeoxidation')
	var water = () => Fluid.of(f.water, 250)
	var count = 0

	// a + b: vanilla and Create copper. The spout set is the old jar's 10 shapes; splashing adds Quark's cut copper
	// vertical slab (the dict drops it when Quark is absent).
	var spoutShapes = ['copper_shingle_slab', 'copper_shingle_stairs', 'copper_shingles', 'copper_tile_slab', 'copper_tile_stairs',
		'copper_tiles', 'cut_copper', 'cut_copper_slab', 'cut_copper_stairs']
	var copperChains = [['copper_block', [i.block_copper, i.exposed_copper, i.weathered_copper, i.oxidized_copper], true]]
	spoutShapes.concat(['cut_copper_vertical_slab']).forEach(shape => {
		copperChains.push([shape, [i[shape], i['exposed_' + shape], i['weathered_' + shape], i['oxidized_' + shape]], shape !== 'cut_copper_vertical_slab'])
	})
	var stepNames = ['exposed', 'weathered', 'oxidized']
	copperChains.filter(c => global.has(c[1])).forEach(c => {
		var name = c[0], chain = c[1], spout = c[2]
		for (var s = 0; s < 3; s++) {
			// b keeps the Copper Splashing ids' names (copper_block_exposed, <shape>_exposed)
			R.createSplashing(Item.of(chain[s + 1]), chain[s], e, name + '_' + stepNames[s]); count++
			if (spout) { R.createFilling(Item.of(chain[s + 1]), [chain[s], water()], e, name + '_' + stepNames[s]); count++ }
		}
	})

	// c: staged copper blocks (the items of OxidationTargets; glass and encased fluid pipes have no item of their own,
	// the coin stack block is placed by the coin stack item)
	var stagedItems = [i.copper_casing, i.cr_pipe, i.mechanical_pump, i.smart_fluid_pipe, i.cr_fluid_valve, i.copper_valve_handle,
		i.cr_tank, i.hose_pulley, i.item_drain, i.spout, i.portable_fluid_interface, i.steam_engine, i.copper_ladder,
		i.ornate_copper_bars, i.copper_scaffolding, i.copper_door,
		i.blue_copper_lamp, i.copper_bars, i.copper_bars_overlay, i.copper_catwalk, i.copper_catwalk_stairs, i.copper_coinstack,
		i.copper_hull, i.copper_mesh_fence, i.copper_sheet_metal, i.copper_support, i.green_copper_lamp, i.locked_copper_door,
		i.red_copper_lamp, i.yellow_copper_lamp,
		i.copper_large_chain, i.copper_light]
	var stages = ['unaffected', 'exposed', 'weathered', 'oxidized']
	stagedItems.filter(id => global.has(id)).forEach(id => {
		// staged/<namespace>/<path>_<stage>: Create and Create Deco both have copper_bars
		var name = id.replace(':', '/')
		for (var s = 0; s < 3; s++) {
			var input = () => ({ type: 'creativeoxidation:staged', item: id, stage: stages[s], waxed: false })
			var output = () => ({ item: id, nbt: '{CreativeOxidationStage:"' + stages[s + 1] + '"}' })
			R.createFilling(output(), [input(), water()], e, 'staged/' + name + '_' + stages[s + 1]); count++
			R.createSplashing(output(), input(), e, 'staged/' + name + '_' + stages[s + 1]); count++
		}
	})

	// d: iron rusting, Immersive Weathering's ids
	var ironChains = [['cut_iron', i.cut_iron], ['plate_iron', i.immersive_weathering_plate_iron], ['iron_bars', i.iron_bars],
		['iron_door', i.iron_door], ['iron_trapdoor', i.iron_trapdoor]]
	;['cut_iron', 'plate_iron'].forEach(base => {
		['slab', 'stairs', 'vertical_slab'].forEach(part => ironChains.push([base + '_' + part, i[base + '_' + part]]))
	})
	var rustNames = ['exposed', 'weathered', 'rusted']
	ironChains.forEach(c => {
		var name = c[0]
		var chain = [c[1], i['exposed_' + name], i['weathered_' + name], i['rusted_' + name]]
		// the vertical slabs are Quark's: the dict drops them when Quark is absent
		if (!global.has(chain)) return
		for (var s = 0; s < 3; s++) {
			R.createFilling(Item.of(chain[s + 1]), [chain[s], water()], e, 'iron/' + name + '_' + rustNames[s]); count++
			R.createSplashing(Item.of(chain[s + 1]), chain[s], e, 'iron/' + name + '_' + rustNames[s]); count++
		}
	})

	console.log('Creative Oxidation aging: ' + count + ' recipes')
})
