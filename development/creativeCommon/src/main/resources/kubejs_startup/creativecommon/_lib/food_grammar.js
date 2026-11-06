// priority: 990
// Grammar and mask check for the Creative Delight subdicts (creative_delight.md 13). Loads after item_dict.js
// (1000) and before food_dict.js (980).
//
// A subdict is a mask over global.item: every value is an i.xxx value, so items stay defined once in item_dict.js
// and the subdict only gives them a development name. global.food (food_dict.js) is the first; global.tool (tool_dict.js) and
// global.misc (misc_dict.js) follow (2026-11-02).
//
// Food key: <category>[_<form>][_<state>]_<name>, e.g. veggie_slice_cooked_potato. Parts are matched longest first,
// so ice_cream beats a shorter entry and a name may hold underscores. Raw is implicit (no state).
// tools/food_dict.py reads the three lists below: keep them plain one-line string arrays.

global.foodGrammar = {
	categories: ['fruit', 'veggie', 'mushroom', 'nut', 'grain', 'dough', 'meat', 'fish', 'egg', 'dairy', 'spice', 'sauce', 'ingredient', 'cake', 'pie', 'cookie', 'pastry', 'candy', 'ice_cream', 'dessert', 'sandwich', 'salad', 'soup', 'snack', 'dish', 'feast', 'drink', 'alcohol', 'golden', 'can', 'hazard'],
	forms: ['slice', 'diced', 'ground', 'cut', 'chop', 'fillet', 'strip', 'leaf', 'patty', 'wedge', 'wheel', 'flour', 'juice', 'jam', 'square', 'burger', 'roll', 'wrap', 'toast', 'serving', 'enchanted'],
	states: ['cooked', 'dried', 'smoked', 'pickled', 'fermented'],
}

// Tool and misc keys: <category>_<name>, no forms or states (tool_dict.js, misc_dict.js; tools/kitchen_dicts.py reads
// these lists too). misc `item` is the catch-all.
global.toolGrammar = {
	categories: ['knife', 'machete', 'skillet', 'kettle', 'pot', 'stove', 'oven', 'grill', 'board', 'basket', 'bowl', 'cup', 'jar', 'bottle', 'mold', 'net', 'tray', 'basin', 'plate', 'watering_can', 'station'],
	forms: [],
	states: [],
}
global.miscGrammar = {
	categories: ['crate', 'bag', 'seed', 'wild', 'colony', 'spawn_egg', 'hat', 'compost', 'soil', 'crop', 'sapling', 'leaves', 'wood', 'block', 'material', 'item'],
	forms: [],
	states: [],
}

global.subdict = {
	// key -> { category, form, state, name } or null when the key does not fit the grammar
	parse: function (key, grammar) {
		function take(rest, list) {
			var best = null
			for (var n = 0; n < list.length; n++) {
				var w = list[n]
				if (rest.indexOf(w + '_') === 0 && (best === null || w.length > best.length)) best = w
			}
			return best
		}
		var cat = take(key, grammar.categories)
		if (cat === null) return null
		var rest = key.substring(cat.length + 1)
		var form = take(rest, grammar.forms)
		if (form !== null) rest = rest.substring(form.length + 1)
		var state = take(rest, grammar.states)
		if (state !== null) rest = rest.substring(state.length + 1)
		if (!/^[a-z0-9]+(_[a-z0-9]+)*$/.test(rest)) return null
		return { category: cat, form: form, state: state, name: rest }
	},

	// Throws when a mask entry is undefined (missing general key), is not a global.item value, appears twice, or
	// (with a grammar) does not parse. Returns itemId -> subdict key.
	check: function (dict, label, grammar) {
		var general = {}
		var gi = global.item
		for (var gk in gi) general[gi[gk]] = true
		var problems = []
		var index = {}
		for (var key in dict) {
			var v = dict[key]
			if (v === undefined || v === null) problems.push(key + ': general key missing (i.xxx is undefined)')
			else if (!general[v]) problems.push(key + ': ' + v + ' is not a global.item value')
			else if (index[v] !== undefined) problems.push(key + ': ' + v + ' already named ' + index[v])
			else index[v] = key
			if (grammar && global.subdict.parse(key, grammar) === null) problems.push(key + ': does not fit <category>[_<form>][_<state>]_<name>')
		}
		if (problems.length) throw new Error(label + ' dict: ' + problems.length + ' problem(s): ' + problems.join('; '))
		return index
	},
}
