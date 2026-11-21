// creativebrewing/publish #25: a bare tag in a result slot (allowTag false) is refused, so the helper call is skipped and
// logged; a concrete item and a tag in an input slot still work.
(function () {
	var fails = [], ok = 0
	function check(name, cond, detail) { if (cond) ok++; else fails.push(name + (detail ? ': ' + detail : '')) }
	global.NCH.ns = function () { return 'creativecommon' }

	__recipes = []; __errors = []
	global.NCH.elytraDuplication('#forge:dough', '#forge:dough/wheat', __E, 'tag_result')
	check('tag result refused', __recipes.length === 0, JSON.stringify(__recipes.map(r => r.json)))
	check('refusal logged', __errors.length === 1 && __errors[0].indexOf('result slot') >= 0, __errors.join('; '))

	__recipes = []; __errors = []
	global.NCH.elytraDuplication('#forge:dough', 'create:dough', __E, 'item_result')
	check('item result kept', __recipes.length === 1 && __recipes[0].json.result.item === 'create:dough', JSON.stringify(__recipes.map(r => r.json)))
	check('tag input kept', __recipes.length === 1 && __recipes[0].json.catalyst.tag === 'forge:dough', JSON.stringify(__recipes.map(r => r.json)))
	check('no error', __errors.length === 0, __errors.join('; '))

	return (fails.length ? 'FAIL ' + fails.join(' | ') + '\n' : '') + 'nch result tag: ' + ok + ' ok, ' + fails.length + ' failed'
})()
