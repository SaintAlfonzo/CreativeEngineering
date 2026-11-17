// tooling/conventions #7: ieBlueprint merges identical inputs into one counted ingredient; the recipe id is unchanged.
(function () {
	var fails = [], ok = 0
	function check(name, cond, detail) { if (cond) ok++; else fails.push(name + (detail ? ': ' + detail : '')) }
	function run(outputs, inputs, cat, idOverride) {
		__recipes = []
		global.NCH.ieBlueprint(outputs, inputs, cat, __E, idOverride)
		return __recipes[0]
	}
	function ids(cat, outputs, inputs) { return 'creativecommon:blueprint/' + cat + '/' + global.NCH.toId(outputs, inputs, cat) }
	global.NCH.ns = function () { return 'creativecommon' }

	// [x, x, y] -> [{base_ingredient:x, count:2}, y]
	var ins = ['minecraft:arrow', 'minecraft:arrow', '#forge:dusts/glowstone']
	var r = run('minecraft:spectral_arrow', ins, 'archery')
	check('recorded', r != null, __errors.join('; '))
	if (r) {
		var j = r.json.inputs
		check('merged length', j.length === 2, JSON.stringify(j))
		check('merged count', j[0] && j[0].count === 2 && j[0].base_ingredient && j[0].base_ingredient.item === 'minecraft:arrow', JSON.stringify(j[0]))
		check('tag kept', j[1] && j[1].tag === 'forge:dusts/glowstone' && j[1].count === undefined, JSON.stringify(j[1]))
		check('id unchanged', r.id === ids('archery', 'minecraft:spectral_arrow', ins), r.id + ' vs ' + ids('archery', 'minecraft:spectral_arrow', ins))
	}

	// a counted input plus a plain repeat add up: [{id:x,count:2}, x, tag, tag] -> [x*3, tag*2]
	r = run('minecraft:observer', [{ id: 'minecraft:cobblestone', count: 2 }, 'minecraft:cobblestone', '#forge:dusts/redstone', '#forge:dusts/redstone'], 'components')
	if (r) {
		var k = r.json.inputs
		check('counted sum', k.length === 2 && k[0].count === 3 && k[1].count === 2 && k[1].base_ingredient.tag === 'forge:dusts/redstone', JSON.stringify(k))
	} else check('counted recorded', false, __errors.join('; '))

	// no repeats -> JSON identical to the old one-per-entry form
	r = run('minecraft:piston', ['minecraft:iron_ingot', '#forge:cobblestone'], 'components', 'piston_test')
	if (r) {
		check('no repeat untouched', JSON.stringify(r.json.inputs) === JSON.stringify([{ item: 'minecraft:iron_ingot' }, { tag: 'forge:cobblestone' }]), JSON.stringify(r.json.inputs))
		check('override id', r.id === 'creativecommon:blueprint/components/piston_test', r.id)
	} else check('override recorded', false, __errors.join('; '))

	// item and tag of the same name are different ingredients
	r = run('minecraft:stick', ['minecraft:stone', '#minecraft:stone'], 'components')
	if (r) check('item vs tag not merged', r.json.inputs.length === 2, JSON.stringify(r.json.inputs))

	return (fails.length ? 'FAIL ' + fails.join(' | ') + '\n' : '') + 'nch blueprint: ' + ok + ' ok, ' + fails.length + ' failed'
})()
