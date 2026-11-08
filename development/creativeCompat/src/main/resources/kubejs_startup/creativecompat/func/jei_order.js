// priority: 900
// phase: helpers
// JEI order helper — global.jeiOrder. The pack's order for JEI's ingredient list
// (feature_writeup/jei_ordering.md). One definition, set by startup_scripts/jei/order.js, feeds both sides:
//   server  server_scripts/tags/jei_order_tags.js makes the creativecompat:jei/<path> tags from it
//           (item, fluid, mob_effect) through global.tags;
//   client  Creative Compat client kubejs/jei_order.js hands it to Compat (compat/jei/PackIngredientOrder), which sorts
//           JEI's list by type, then category path, then each node's sort chain.
// In Creative Compat since 2026-11-07 (was Common func/). Loads after the dicts (Common pack) and before the
// definition (startup_scripts, default priority).
//
// Definition shape:
//   {
//     types:    ['item', 'multiblock', 'fluid', 'effect'],    // ingredient type order; others go after, in JEI's order
//     modOrder: ['minecraft', 'create', ...],                 // shared mod order for the 'mod' sort
//     trees: {                                                // one category tree per type
//       item: [ node, ... ], fluid: [...], effect: [...], multiblock: [...],
//     },
//     packHidden: { fluid: [ids] },                           // left out of JEI on purpose (never added); the report
//                                                             // counts them as hidden by the pack
//   }
//   node: {
//     id:       'copper',                                     // path segment: lowercase a-z 0-9 _
//     children: [ node, ... ],                                // order here = the order in JEI
//     sort:     ['list', 'mod', 'creative'],                  // this node's own members; default ['mod', 'creative']
//                                                             //   list      position in `list` (unlisted after)
//                                                             //   mod       modOrder rank (unlisted mods after, by id)
//                                                             //   name      display name, A-Z
//                                                             //   creative  creative tab order
//     list:     [global.item.copper_sword, ...],              // exact ids: members (tagged too) and their order
//     from:     ['@mod', '#forge:tag', 'minecraft:*_boat'],   // more members, tag side only (global.tags.addMatching)
//     except:   [global.item.x, 'mod:*_part'],              // taken back out of `from` (ids, wildcards, '#tags'), e.g.
//                                                             //   ids another node lists, so they don't conflict
//     modOrder: [...],                                        // per-node override of the shared modOrder
//   }
// A node holds its own members before its children's. Multiblocks have no registry: their nodes place by `list`
// only. An ingredient's deepest creativecompat:jei/... tag is its place; see the writeup for conflicts.
console.log('JEI order helper starting')

global.jeiOrder = {

	TAG_ROOT: 'creativecompat:jei/',
	// definition type key -> tag registry (null: no registry, placed by `list` only)
	TAG_REGISTRY: { item: 'item', fluid: 'fluid', effect: 'mob_effect', multiblock: null },
	SORT_MODES: ['list', 'mod', 'name', 'alphabetical', 'creative'],

	_def: { types: [], modOrder: [], trees: {}, packHidden: {} },

	// set the definition (startup_scripts/jei/order.js); checks it and keeps a plain copy
	define(def) {
		const out = {
			types: this._strings(def.types),
			modOrder: this._strings(def.modOrder),
			trees: {},
			packHidden: {},
		}
		const trees = def.trees || {}
		Object.keys(trees).forEach(type => {
			if (!(type in this.TAG_REGISTRY)) console.warn('global.jeiOrder: unknown type "' + type + '" (tags only for ' + Object.keys(this.TAG_REGISTRY) + ')')
			out.trees[type] = this._nodes(type, trees[type], [])
		})
		const hidden = def.packHidden || {}
		Object.keys(hidden).forEach(type => { out.packHidden[type] = this._strings(hidden[type]) })
		this._def = out
		let n = 0
		Object.keys(out.trees).forEach(type => this.walk(type, () => n++))
		console.log('[jeiOrder] defined ' + n + ' nodes over ' + Object.keys(out.trees).length + ' types')
		return out
	},

	definition() { return this._def },

	// what Creative Compat client kubejs/jei_order.js hands to Java
	toJson() { return JSON.stringify(this._def) },

	// 'equipment/tools/copper' -> 'creativecompat:jei/equipment/tools/copper'
	tagId(path) { return this.TAG_ROOT + path },

	// fn(node, path) for every node of a type's tree, parents before children
	walk(type, fn) {
		const visit = (nodes, prefix) => nodes.forEach(node => {
			const path = prefix ? prefix + '/' + node.id : node.id
			fn(node, path)
			visit(node.children || [], path)
		})
		visit(this._def.trees[type] || [], '')
	},

	// server side: one jei tag per node with members (`from` + `list`), for a ServerEvents.tags event
	// ServerEvents.tags('item', e => global.jeiOrder.tag(e, 'item'))
	tag(event, type) {
		if (!this.TAG_REGISTRY[type]) return
		let tags = 0
		this.walk(type, (node, path) => {
			const entries = (node.from || []).concat(node.list || [])
			if (entries.length === 0) return
			global.tags.addMatching(event, this.tagId(path), entries, node.except ? { except: node.except } : undefined)
			tags++
		})
		console.log('[jeiOrder] ' + type + ': ' + tags + ' jei tags')
	},

	// ids a type leaves out of JEI on purpose (Creative Common client _lib/fluid_jei.js skips these fluids)
	packHidden(type) { return this._def.packHidden[type] || [] },

	_nodes(type, nodes, prefix) {
		const seen = {}
		return (nodes || []).map(node => {
			const id = String(node.id)
			const path = prefix.concat([id]).join('/')
			if (!/^[a-z0-9_]+$/.test(id)) console.warn('global.jeiOrder: ' + type + ' node "' + path + '" should be lowercase a-z 0-9 _ (it is a tag path)')
			if (seen[id]) console.warn('global.jeiOrder: duplicate ' + type + ' node "' + path + '", the first one is kept')
			seen[id] = true
			const out = { id: id }
			if (node.sort) {
				out.sort = this._strings(node.sort)
				out.sort.forEach(m => { if (this.SORT_MODES.indexOf(m) < 0) console.warn('global.jeiOrder: ' + type + ' node "' + path + '" has unknown sort "' + m + '"') })
			}
			if (node.modOrder) out.modOrder = this._strings(node.modOrder)
			if (node.list) {
				out.list = this._strings(node.list)
				out.list.forEach(e => { if (/^[#@\/]|[*?]/.test(e)) console.warn('global.jeiOrder: ' + type + ' node "' + path + '" list entry "' + e + '" is not an exact id (put patterns in `from`)') })
			}
			if (node.from) out.from = this._strings(node.from)
			if (node.except) out.except = this._strings(node.except)
			if (node.except && !node.from) console.warn('global.jeiOrder: ' + type + ' node "' + path + '" has `except` but no `from`')
			if (node.from && !this.TAG_REGISTRY[type]) console.warn('global.jeiOrder: ' + type + ' node "' + path + '" has `from`, but ' + type + ' has no tags (use `list`)')
			if (node.children) out.children = this._nodes(type, node.children, prefix.concat([id]))
			return out
		})
	},

	// plain JS strings (dict values can be Java strings; undefined entries are a missing dict key)
	_strings(v) {
		const arr = v == null ? [] : (Array.isArray(v) ? v : [v])
		const out = []
		arr.forEach((s, idx) => {
			if (s == null) { console.warn('global.jeiOrder: entry ' + idx + ' is undefined (missing dict key?)'); return }
			out.push(String(s))
		})
		return out
	},
}
