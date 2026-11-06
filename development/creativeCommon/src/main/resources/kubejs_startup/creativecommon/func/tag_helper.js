// Tag helper — global.tags. Registry-agnostic: pass the event from
// ServerEvents.tags('item' | 'block' | 'fluid' | 'entity_type' | ...).
// patcherMod: src/main/resources/kubejs_startup/creativeengineering/func/tag_helper.js (was
// kubejs/startup_scripts/func/tag_helper.js; moved 2026-10-25, creative_delight.md 10.5 item 3). Loaded by
// patcherMod's startup loader before the pack's startup_scripts folder. An edit needs a patcherMod rebuild.
console.log('Tag Helper starting')

global.tags = {

    // plain wrapper over event.add — one or more tags × one or more entries, no chain expansion.
	// t.add(e, 'forge:wires/all_metal', 'mod:thing')
	// t.add(e, ['forge:springs', 'forge:springs/iron'], ['a', 'b'])
	add(event, tags, entries) {
		const ents = this._ents(event, entries)
		this._arr(tags).forEach(tag => {
			const clean = this._clean(tag)
			ents.forEach(id => event.add(clean, id))
		})
	},

	// mirror of add() (same as removeTags without opts)
	remove(event, tags, entries) {
		const ents = this._ents(event, entries)
		this._arr(tags).forEach(tag => {
			const clean = this._clean(tag)
			ents.forEach(id => event.remove(clean, id))
		})
	},


	// bulk add by pattern — one or more tags × entries that may use wildcards.
	// t.addMatching(e, 'minecraft:concrete', 'minecraft:*_concrete')
	// t.addMatching(e, 'mod:bricks', ['minecraft:brick_*', '#minecraft:stone_bricks', '@quark'], { except: 'minecraft:brick_wall' })
	// '*' = any run of chars inside one id half (never crosses ':'), '?' = one char, no ':' = minecraft.
	// Entries without wildcards pass through as-is (ids, '#tags', '@mod', /regex/).
	// '#tag' entries go in as nested references (live: they pick up members other scripts add later).
	// except: ids, wildcards or '#tags' removed afterwards. A nested reference can't be filtered, so
	// when except is given every '#tag' entry is expanded to its current members instead.
	addMatching(event, tags, entries, opts) {
		opts = opts || {}
		const expand = this._arr(opts.except).length > 0
		const except = this._resolveIds(event, opts.except)
		const ents = []
		this._ents(event, entries).forEach(entry => {
			const s = String(entry)
			if (s.charAt(0) !== '#' || !expand) { ents.push(this._glob(entry)); return }
			const members = this._tagMembers(event, s)
			if (members.length === 0 && !this._isPreTag(event)) console.warn('global.tags.addMatching: ' + s + ' has no members yet (typo, or filled by a later script?)')
			members.forEach(id => ents.push(id))
		})
		this._arr(tags).forEach(tag => {
			const clean = this._clean(tag)
			ents.forEach(id => event.add(clean, id))
			except.forEach(id => event.remove(clean, id))
		})
	},

	// 'minecraft:*_concrete' -> '/^minecraft:[^:]*_concrete$/' (KubeJS parses '/.../' strings as regex
	// and matches them against every registry id); non-wildcard entries are returned unchanged.
	_glob(entry) {
		const s = String(entry)
		const c = s.charAt(0)
		if (c === '#' || c === '@' || c === '/' || !/[*?]/.test(s)) return entry
		const full = s.indexOf(':') < 0 ? 'minecraft:' + s : s
		return '/^' + full.replace(/[.+^${}()|[\]\\]/g, '\\$&').replace(/\*/g, '[^:]*').replace(/\?/g, '[^:]') + '$/'
	},

	_arr(v) { return v == null ? [] : (Array.isArray(v) ? v : [v]) },

	// Entry list with ids of absent mods left out (plan_cb_publish.md M2 rule G5): an id is dropped when its
	// namespace's mod is not loaded and the event's registry does not hold it (an id another mod registers under
	// that namespace stays). Counted per mod in _absentCounts; Creative Common's server script _lib/absent_mods.js
	// prints one line per mod. '#tags', '/regex/' and '*' globs pass through unchanged.
	_absentCounts: {},
	_ents(event, v) {
		const arr = this._arr(v)
		let out = null
		for (let k = 0; k < arr.length; k++) {
			const s = arr[k] == null ? '' : String(arr[k])
			const c = s.charAt(0)
			let keep = true
			if (s !== '' && c !== '#' && c !== '/' && s.indexOf('*') < 0) {
				const ns = global.has.namespace(s)
				if (ns !== null && !global.has.mod(ns) && !this._inRegistry(event, s)) {
					keep = false
					this._absentCounts[ns] = (this._absentCounts[ns] || 0) + 1
				}
			}
			if (!keep && out === null) out = arr.slice(0, k)
			else if (keep && out !== null) out.push(arr[k])
		}
		return out === null ? arr : out
	},
	_inRegistry(event, id) {
		if (this._regEvent !== event) {
			this._regEvent = event
			this._regIds = this._registryIds(event)
		}
		return this._regIds[id] === true
	},
	_clean(t) { return String(t).replace(/^#/, '') },

	// 'forge:fluid/chocolate/dark'
	//   -> ['forge:fluid/chocolate/dark', 'forge:fluid/chocolate', 'forge:fluid']
	// single-segment tags (e.g. 'minecraft:logs') just return themselves.
	tagChain(tag) {
		const clean = this._clean(tag)
		const ci = clean.indexOf(':')
		const ns = ci >= 0 ? clean.substring(0, ci) : 'minecraft'
		const path = ci >= 0 ? clean.substring(ci + 1) : clean
		const parts = path.split('/')
		const out = []
		for (let i = parts.length; i >= 1; i--) out.push(ns + ':' + parts.slice(0, i).join('/'))
		return out
	},

	// ── 1) remove ALL tags from one or more entries ──────────────────────────
	// TAGS.clearAll(e, 'mod:item')  /  TAGS.clearAll(e, ['a', 'b', '@somemod'])
	clearAll(event, entries) {
		this._ents(event, entries).forEach(id => event.removeAllTagsFrom(id))
	},

	// ── 2) remove SPECIFIC tag(s) from entr(ies), optionally the whole chain ──
	// TAGS.removeTags(e, 'forge:dyes/red', 'chalk:red_chalk', { parents: true })
	//   -> removes from 'forge:dyes/red' AND 'forge:dyes'
	removeTags(event, tags, entries, opts) {
		opts = opts || {}
		const ents = this._ents(event, entries)
		this._arr(tags).forEach(tag => {
			const chain = opts.parents ? this.tagChain(tag) : [this._clean(tag)]
			chain.forEach(t => ents.forEach(id => event.remove(t, id)))
		})
	},

	// wipe a tag entirely (every member)
	// TAGS.wipeTag(e, 'forge:wires')
	wipeTag(event, tags) {
		this._arr(tags).forEach(t => event.removeAll(this._clean(t)))
	},

	// ── 3) add entr(ies) to a tag + every parent tag in the chain ────────────
	// TAGS.addTag(e, 'forge:fluid/chocolate/dark', 'create_confectionery:black_chocolate')
	//   -> adds to  forge:fluid/chocolate/dark, forge:fluid/chocolate, forge:fluid
	// pass { parents: false } for just the one tag.
	addTag(event, tag, entries, opts) {
		opts = opts || {}
		const chain = opts.parents === false ? [this._clean(tag)] : this.tagChain(tag)
		const ents = this._ents(event, entries)
		chain.forEach(t => ents.forEach(id => event.add(t, id)))
	},

	addFluid(event, tag, fluids, opts) { this.addTag(event, tag, fluids, opts) }, // alias for readability

	// ── bonus: material family  items -> forge:<type> + forge:<type>/<material>
	// TAGS.materialFamily(e, 'dusts', 'cobalt', ['mod:cobalt_dust'])
	materialFamily(event, type, material, entries) {
		this._ents(event, entries).forEach(id => {
			event.add('forge:' + type, id)
			event.add('forge:' + type + '/' + material, id)
		})
	},

	// ── bonus: block mining tool + level in one call (ServerEvents.tags('block'))
	// TAGS.mineable(e, 'pickaxe', 'iron', ['mod:block'])   level: null|stone|copper|iron|diamond|gold|netherite
	// tool null = set only the level. opts.exclusive: also take the blocks out of every other tool's
	// mineable tag (when tool is given) and every other level's needs_ tag (when level is given).
	//   TAGS.mineable(e, null, 'copper', ['mod:block'], { exclusive: true })
	mineable(event, tool, level, blocks, opts) {
		opts = opts || {}
		const tools = ['pickaxe', 'shovel', 'axe', 'hoe']
		const needs = { stone: 'minecraft:needs_stone_tool', copper: 'creativeengineering:needs_copper_tool',
			iron: 'minecraft:needs_iron_tool', diamond: 'minecraft:needs_diamond_tool',
			gold: 'forge:needs_gold_tool', netherite: 'forge:needs_netherite_tool' }
		if (level && !needs[level]) throw new Error('global.tags.mineable: unknown level "' + level + '"')
		this._ents(event, blocks).forEach(id => {
			if (tool) event.add('minecraft:mineable/' + tool, id)
			if (needs[level]) event.add(needs[level], id)
			if (!opts.exclusive) return
			if (tool) tools.forEach(other => { if (other !== tool) event.remove('minecraft:mineable/' + other, id) })
			if (needs[level]) Object.keys(needs).forEach(other => { if (other !== level) event.remove(needs[other], id) })
		})
	},

    	// Storage block ("Block of X"). Call from BOTH ServerEvents.tags('block', ...)
	// and ServerEvents.tags('item', ...) — it self-detects the registry via event.getType().
	//
	//   TAGS.storageBlock(e, 'cobalt', 'mymod:cobalt_block')
	//   TAGS.storageBlock(e, 'steel', ['mymod:steel_block'], { beacon: true, level: 'iron' })
	//   TAGS.storageBlock(e, 'flesh', 'creativeengineering:prepared_flesh_block', { tool: 'hoe', level: null })
	//
	// opts: tool  (block mining tool, default 'pickaxe'; null = don't add a mineable tag)
	//       level ('stone'|'iron'|'diamond'|'gold'|'netherite'|null, default null)
	//       beacon (add to minecraft:beacon_base_blocks, default false)
	storageBlock(event, material, ids, opts) {
		opts = opts || {}
		const isBlock = String(event.getType()).endsWith(':block')
		const needs = {
			stone: 'minecraft:needs_stone_tool', iron: 'minecraft:needs_iron_tool',
			diamond: 'minecraft:needs_diamond_tool', gold: 'forge:needs_gold_tool',
			netherite: 'forge:needs_netherite_tool',
		}

		this._ents(event, ids).forEach(id => {
			event.add('forge:storage_blocks', id)
			event.add('forge:storage_blocks/' + material, id)

			if (isBlock) {
				const tool = opts.tool === undefined ? 'pickaxe' : opts.tool
				if (tool) event.add('minecraft:mineable/' + tool, id)
				if (needs[opts.level]) event.add(needs[opts.level], id)
				if (opts.beacon) event.add('minecraft:beacon_base_blocks', id)
			}
		})
	},

    // forge:<type>, forge:<type>/all_metal, forge:<type>/<metal>   (rods/wires/plates/springs convention)
	// TAGS.metalPart(e, 'wires', 'zinc', 'mod:zinc_wire')            add
	// TAGS.metalPart(e, 'rods',  'copper', 'mod:copper_rod', 'remove')
	// metal omitted -> just forge:<type> (+ /all_metal)
	metalPart(event, type, metal, ids, mode) {
		const fn = mode === 'remove' ? (t, id) => event.remove(t, id) : (t, id) => event.add(t, id)
		const tags = ['forge:' + type, 'forge:' + type + '/all_metal']
		if (metal) tags.push('forge:' + type + '/' + metal)
		this._ents(event, ids).forEach(id => tags.forEach(t => fn(t, id)))
	},

	// Warn about ids sitting in more than one of the given tags (nested tags followed) — for tag
	// sets meant to be exclusive, e.g. BlockRunner tiers, where BlockRunner applies tag values with
	// putIfAbsent over a HashSet so a block in two tiers gets a random one per reload.
	// Call AFTER the adds, in the same event; only sees what's been added so far. Returns the count.
	//   t.overlaps(e, ['blockrunner:surface/wood', 'blockrunner:surface/metal'], 'BlockRunner tier')
	overlaps(event, tags, label) {
		label = label || 'tag'
		const seen = {}
		this._arr(tags).forEach(tag => {
			const clean = this._clean(tag)
			this._tagMembers(event, clean).forEach(id => {
				const list = seen[id] = seen[id] || []
				if (list.indexOf(clean) < 0) list.push(clean)
			})
		})
		if (this._isPreTag(event)) return 0
		let n = 0
		Object.keys(seen).forEach(id => {
			if (seen[id].length > 1) { n++; console.warn(label + ' overlap: ' + id + ' is in ' + seen[id].join(' + ')) }
		})
		console.log(label + ' overlaps checked: ' + Object.keys(seen).length + ' ids, ' + n + ' overlap(s)')
		return n
	},

	// Look-alike copies join their original's tag: for every member of each tag, rename(id) returns
	// the id (or a list of ids, or null) of blocks that imitate it, e.g. SecurityCraft's reinforced
	// blocks. Only registered ids are added, so guessed names are fine. Call after the originals are
	// in; like overlaps(), it only sees what's been added so far.
	//   t.mirror(e, 'blockrunner:surface/wood', id => id.replace('minecraft:', 'securitycraft:reinforced_'))
	mirror(event, tags, rename) {
		const known = this._registryIds(event)
		this._arr(tags).forEach(tag => {
			const clean = this._clean(tag)
			this._tagMembers(event, clean).forEach(id => {
				this._arr(rename(id)).forEach(copy => { if (copy && copy !== id && known[copy]) event.add(clean, copy) })
			})
		})
	},

	// every registered id of the event's registry, as a lookup object
	_registryIds(event) {
		const arr = event.getElementIds().toArray()
		const out = {}
		for (let i = 0; i < arr.length; i++) out[String(arr[i])] = true
		return out
	},

	// KubeJS runs tag scripts first as a "pre" pass (PreTagEventJS) with no tag data. There,
	// getObjectIds() returns [] and marks the pre pass invalid, so KubeJS re-runs these scripts on
	// the real tag load instead of replaying queued adds. Keep calling it (except/overlaps need
	// the re-run); just don't warn about the empty results.
	_isPreTag(event) { return String(event.getClass().getSimpleName()) === 'PreTagEventJS' },

	// current resolved member ids of a tag (nested tags followed), as JS strings
	_tagMembers(event, tag) {
		const list = event.get(this._clean(tag)).getObjectIds()
		const out = []
		for (let i = 0; i < list.size(); i++) out.push(String(list.get(i)))
		return out
	},

	// ids and '#tags' -> flat list of ids
	_resolveIds(event, entries) {
		const out = []
		this._ents(event, entries).forEach(entry => {
			const s = String(entry)
			if (s.charAt(0) === '#') this._tagMembers(event, s).forEach(id => out.push(id))
			else out.push(this._glob(entry))
		})
		return out
	},

}

console.log('Tag Helper finished')
