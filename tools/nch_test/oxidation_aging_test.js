// SPDX-License-Identifier: GPL-3.0-only
// Creative Oxidation P7 (plan_creative_oxidation.md): runs the real aging script
// (kubejs/data/creativeengineering/kubejs/creativeoxidation/aging.js, evaluated before this file with a ServerEvents stub)
// against the real NCH and checks the recipe counts and the JSON of the staged route (_createNbtRoute): the
// creativeoxidation:staged input, the stage NBT result, the water ingredient. Plain recipes go through the
// e.recipes.create stub and are checked by type and id.
(function () {
	var fails = [], ok = 0
	function check(name, cond, detail) { if (cond) ok++; else fails.push(name + (detail ? ': ' + detail : '')) }
	__recipes = []; __errors = []
	__serverRecipes.forEach(function (fn) { fn(__E) })
	var byId = {}
	__recipes.forEach(function (r) { byId[r.id] = r })
	var ids = Object.keys(byId)
	var count = function (prefix) { return ids.filter(function (id) { return id.indexOf(prefix) === 0 }).length }
	check('no NCH error', __errors.length === 0, __errors.join('; '))
	check('no duplicate id', ids.length === __recipes.length, ids.length + ' ids, ' + __recipes.length + ' recipes')
	// a + b: 10 copper chains by spout (old jar) and 11 by fan (Copper Splashing), 3 steps each
	check('a: 30 copper spout recipes', ids.filter(function (id) { return /^creativeoxidation:createfilling\/[a-z_]+$/.test(id) }).length === 30)
	check('b: 33 copper splashing recipes', ids.filter(function (id) { return /^creativeoxidation:createsplashing\/[a-z_]+$/.test(id) }).length === 33)
	check('b: Copper Splashing names kept', !!byId['creativeoxidation:createsplashing/copper_block_exposed']
		&& !!byId['creativeoxidation:createsplashing/cut_copper_vertical_slab_oxidized'])
	// c: 32 staged items x 3 steps x (spout + fan)
	check('c: 96 staged spout recipes', count('creativeoxidation:createfilling/staged/') === 96, count('creativeoxidation:createfilling/staged/'))
	check('c: 96 staged splashing recipes', count('creativeoxidation:createsplashing/staged/') === 96, count('creativeoxidation:createsplashing/staged/'))
	// d: 11 iron chains x 3 steps x 2
	check('d: 33 iron spout recipes', count('creativeoxidation:createfilling/iron/') === 33, count('creativeoxidation:createfilling/iron/'))
	check('d: 33 iron splashing recipes', count('creativeoxidation:createsplashing/iron/') === 33, count('creativeoxidation:createsplashing/iron/'))

	var f = byId['creativeoxidation:createfilling/staged/create/fluid_pipe_exposed']
	var fj = f && f.json
	check('staged filling is custom JSON', fj && fj.type === 'create:filling', JSON.stringify(fj))
	if (fj) {
		var ing = fj.ingredients[0]
		check('staged input', ing.type === 'creativeoxidation:staged' && ing.item === 'create:fluid_pipe' && ing.stage === 'unaffected'
			&& ing.waxed === false && ing.count === undefined, JSON.stringify(ing))
		check('water 250 mB', fj.ingredients[1].fluid === 'minecraft:water' && fj.ingredients[1].amount === 250, JSON.stringify(fj.ingredients[1]))
		check('stage NBT result', fj.results.length === 1 && fj.results[0].item === 'create:fluid_pipe'
			&& fj.results[0].nbt === '{CreativeOxidationStage:"exposed"}', JSON.stringify(fj.results))
	}
	var s = byId['creativeoxidation:createsplashing/staged/createdeco/copper_coinstack_oxidized']
	var sj = s && s.json
	check('staged splashing: weathered -> oxidized coin stack', sj && sj.type === 'create:splashing'
		&& sj.ingredients[0].stage === 'weathered' && sj.ingredients[0].item === 'createdeco:copper_coinstack'
		&& sj.results[0].nbt === '{CreativeOxidationStage:"oxidized"}', JSON.stringify(sj))
	var p = byId['creativeoxidation:createfilling/iron/iron_door_exposed']
	check('iron door spout stays on kubejs-create', p && p.kind === 'create.filling' && p.out === 'immersive_weathering:exposed_iron_door'
		&& p.in[0] === 'minecraft:iron_door', JSON.stringify(p))
	var c = byId['creativeoxidation:createfilling/copper_block_weathered']
	check('old-jar copper block spout', c && c.out === 'minecraft:weathered_copper' && c.in[0] === 'minecraft:exposed_copper', JSON.stringify(c))
	check('no staged recipe for a waxed input', !__recipes.some(function (r) { return r.json && r.json.ingredients
		&& r.json.ingredients.some(function (x) { return x.waxed === true }) }))
	return (fails.length ? 'FAIL ' + fails.join(' | ') + '\n' : '') + 'oxidation aging: ' + ok + ' ok, ' + fails.length + ' failed ('
		+ __recipes.length + ' recipes)'
})()
