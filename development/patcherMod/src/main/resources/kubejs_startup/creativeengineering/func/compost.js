// priority: 900
// patcherMod: src/main/resources/kubejs_startup/creativeengineering/func/compost.js (was
// kubejs/startup_scripts/func/compost.js; moved 2026-10-25, creative_delight.md 10.5 item 3). patcherMod's
// startup loader runs this pack before the pack's startup_scripts folder, so global.compost exists before
// compost/compost_rules.js + compost/compost_values.js call it at top level. It reads no dict at load time;
// keep it that way (the dicts load after it). An edit needs a patcherMod rebuild.
// ─────────────────────────────────────────────────────────────────────────────
//  Compost framework - global.compost. Owns the composter's chance table
//  (ComposterBlock.COMPOSTABLES, the one map vanilla, hoppers and every mod read).
//
//  Each time item tags finish loading the table is rebuilt from nothing:
//    1. clear it - every vanilla and mod registration goes
//    2. rules      (compost/compost_rules.js)   categories checked top to bottom, the first one
//                                                that matches an item gives it that category's chance
//    3. values     (compost/compost_values.js)  explicit per-item chances; beat the rules
//    4. exclusions (compost/compost_rules.js)   never compostable; beat everything
//  Tags load at server start, on /reload, and on a client when it receives the server's tags, so a
//  dedicated server and its clients build the same table from the same scripts.
//
//  A chance is a number 0-1 or a tier name from global.compost.tiers(). 0 = not compostable.
//  To change a chance: edit the data file, /kubejs reload startup_scripts, then /reload (the data files
//  stay in the pack folder; only this engine moved into patcherMod's jar).
//  /dumpcompost (server_scripts/_lib/dump_compost.js) writes kubejs/exported/compost/compost.json:
//  the table as the mods left it, the table now, and which rule or value put each item there.
//  tools/gen_compost_values.py turns that dump into compost_values.js.
// ─────────────────────────────────────────────────────────────────────────────

var $CompostComposter = Java.loadClass('net.minecraft.world.level.block.ComposterBlock')
var $CompostRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
var $CompostRegistry = Java.loadClass('net.minecraft.core.Registry')
var $CompostTagKey = Java.loadClass('net.minecraft.tags.TagKey')
var $CompostResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')
var $CompostBlockItem = Java.loadClass('net.minecraft.world.item.BlockItem')

global.compost = {

	TIERS: {},          // name -> chance
	CATEGORIES: [],     // [{ name, chance, match }], checked in order
	VALUES: {},         // item id -> { chance, category }
	EXCLUDES: [],       // [{ raw, kind: 'id' | 'tag' | 'regex', value }]
	last: null,         // report of the last apply (see report())

	// ── data-file API ──────────────────────────────────────────────────────

	// global.compost.tiers({ low: 0.3, medium: 0.5 }) - named chances usable anywhere a chance goes
	tiers(map) {
		for (var k in map) this.TIERS[k] = map[k]
	},

	// global.compost.category('seeds', 0.3, { tags: [...], ids: [/re/], blocks: [...], items: [...],
	//                                         edible: true | false, except: [...] })
	//   tags    '#ns:tag' (or item_dict tag keys)        item is in any of them
	//   ids     regexes tested against the full item id  ('farmersdelight:tomato_seeds')
	//   blocks  block classes (short name = net.minecraft.world.level.block.X, or a full class name);
	//           the item is a BlockItem whose block is an instance of one of them
	//   items   item classes / interfaces (full class names); the item is an instance of one
	//   edible  true = food only, false = non-food only, left out = either
	//   except  ids, '#tags' or regexes this category never takes (the item can still match a later one)
	// An item matches when it hits any of tags / ids / blocks / items and passes edible and except.
	category(name, chance, match) {
		match = match || {}
		this.CATEGORIES.push({
			name: name,
			chance: chance,
			tags: this._arr(match.tags).map(t => this._tagName(t)),
			ids: this._arr(match.ids),
			blocks: this._classes(this._arr(match.blocks), 'net.minecraft.world.level.block.'),
			items: this._classes(this._arr(match.items), 'net.minecraft.world.item.'),
			edible: match.edible,
			except: this._arr(match.except).map(e => this._matcher(e)),
		})
	},

	// global.compost.values('seeds', [[i.wheat_seeds, 0.3], [i.melon_seeds, 'low'], ...])
	// The category name only groups the file and labels /dumpcompost; the chance is what counts.
	// opts.when (optional): a function checked at every apply; while it returns false the rows count as unlisted
	// (another mod's switch, e.g. Creative Delight's [qol] compost, read after the configs load).
	values(category, rows, opts) {
		var when = opts && typeof opts.when === 'function' ? opts.when : null
		this._arr(rows).forEach(row => {
			var id = row[0]
			if (typeof id !== 'string' || id.length === 0) {
				console.warn('[compost] values(' + category + '): row with no item id (undefined item_dict key?) -> ' + row)
				return
			}
			if (id.charAt(0) === '#') {
				console.warn('[compost] values(' + category + '): ' + id + ' is a tag; values take items, use a rule for tags')
				return
			}
			if (this.VALUES[id]) console.warn('[compost] ' + id + ' listed twice (' + this.VALUES[id].category + ', ' + category + '); the later one wins')
			this.VALUES[id] = { chance: row[1], category: category, when: when }
		})
	},

	// global.compost.exclude([i.tobacco_seeds, '#some:tag', /tobacco.*seed/]) - never compostable
	exclude(entries) {
		this._arr(entries).forEach(e => {
			if (e === undefined || e === null) {
				console.warn('[compost] exclude(): undefined entry (undefined item_dict key?)')
				return
			}
			this.EXCLUDES.push(this._matcher(e))
		})
	},

	// ── engine ─────────────────────────────────────────────────────────────

	// Resolves a number or tier name to a chance in [0, 1]; NaN when it can't.
	chanceOf(v) {
		if (typeof v === 'string') v = this.TIERS[v]
		v = Number(v)
		if (isNaN(v)) return NaN
		return Math.max(0, Math.min(1, v))
	},

	apply(cause) {
		var started = Date.now()
		var table = $CompostComposter.COMPOSTABLES
		// The first apply sees the table as vanilla + mods left it. Kept in global (survives a startup
		// script reload) because every later apply only ever sees our own table.
		if (!global.compostOriginal) global.compostOriginal = this._snapshot(table)

		var tagSets = this._tagSets()
		var ctx = this._ruleContext(tagSets)
		var byId = {}
		var entries = {}      // id -> { chance, source: 'value' | 'rule', category }
		var excluded = {}     // id -> what it would have been
		var bad = []

		var it = $CompostRegistries.ITEMS.getEntries().iterator()
		while (it.hasNext()) {
			var en = it.next()
			var id = String(en.getKey().location())
			if (id === 'minecraft:air') continue
			var item = en.getValue()
			byId[id] = item

			var entry = null
			var val = this.VALUES[id]
			if (val && val.when && !this._whenOn(val.when)) val = null
			if (val) {
				var c = this.chanceOf(val.chance)
				if (isNaN(c)) bad.push(id + ' -> ' + val.chance)
				else entry = { chance: c, source: 'value', category: val.category }
			}
			if (!entry) {
				var cat = this._firstCategory(id, item, ctx)
				if (cat) entry = { chance: cat.chance, source: 'rule', category: cat.name }
			}
			var wasCompostable = global.compostOriginal[id] !== undefined
			if ((entry || wasCompostable) && this._excluded(id, tagSets)) {
				excluded[id] = entry || { chance: global.compostOriginal[id], source: 'original', category: null }
				continue
			}
			if (entry && entry.chance > 0) entries[id] = entry
		}

		var missing = []
		for (var vid in this.VALUES) if (!byId[vid]) missing.push(vid)

		table.clear()
		var fromValues = 0
		var fromRules = 0
		for (var eid in entries) {
			table.put(byId[eid], entries[eid].chance)
			if (entries[eid].source === 'value') fromValues++
			else fromRules++
		}

		this.last = {
			cause: String(cause),
			ms: Date.now() - started,
			entries: entries,
			excluded: excluded,
			missing: missing,
			bad: bad,
		}
		console.info('[compost] ' + this.last.cause + ': ' + (fromValues + fromRules) + ' compostable ('
			+ fromValues + ' from values, ' + fromRules + ' from rules), ' + Object.keys(excluded).length + ' excluded, '
			+ Object.keys(global.compostOriginal).length + ' before rebuild, ' + this.last.ms + ' ms')
		if (missing.length) console.warn('[compost] values for items that do not exist: ' + missing.join(', '))
		if (bad.length) console.warn('[compost] values with no usable chance (unknown tier?): ' + bad.join(', '))
		return this.last
	},

	// Everything /dumpcompost writes. Chances rounded to 3 places (the table stores floats).
	report() {
		var r3 = v => Math.round(Number(v) * 1000) / 1000
		var original = {}
		var now = {}
		var excluded = {}
		var o = global.compostOriginal || {}
		for (var a in o) original[a] = r3(o[a])
		var last = this.last || { entries: {}, excluded: {}, missing: [], bad: [] }
		for (var b in last.entries) {
			var e = last.entries[b]
			now[b] = { chance: r3(e.chance), source: e.source, category: e.category }
		}
		for (var c in last.excluded) {
			var x = last.excluded[c]
			excluded[c] = { chance: r3(x.chance), source: x.source, category: x.category }
		}
		return {
			_readme: 'original = the composter table as vanilla + mods registered it (before global.compost rebuilt it). '
				+ 'now = the table after the rebuild: source "value" = compost_values.js, "rule" = a category in compost_rules.js. '
				+ 'excluded = would be compostable, blocked by an exclusion.',
			cause: last.cause,
			categories: this.CATEGORIES.map(k => ({ name: k.name, chance: this.chanceOf(k.chance) })),
			tiers: this.TIERS,
			original: original,
			now: now,
			excluded: excluded,
			missing_values: last.missing,
			bad_values: last.bad,
		}
	},

	// ── internals ──────────────────────────────────────────────────────────

	// Rules never take an item with a crafting remainder (bowl and bottle food): composting it would
	// eat the container. List one in compost_values.js to compost it anyway.
	// Rhino costs about a microsecond per lookup, and this runs for every item in the game, so the
	// per-category tag lookups are folded into one (ctx.tagHits) and one combined regex (ctx.idGate)
	// decides whether the per-category id patterns are worth testing at all.
	_firstCategory(id, item, ctx) {
		var tagHits = ctx.tagHits[id]
		var idGate = ctx.idGate !== null && ctx.idGate.test(id)
		var classHits = null
		var edible = null
		var container = null
		for (var n = 0; n < this.CATEGORIES.length; n++) {
			var cat = this.CATEGORIES[n]
			var hit = tagHits !== undefined && tagHits[n] === true
			for (var r = 0; !hit && idGate && r < cat.ids.length; r++) if (cat.ids[r].test(id)) hit = true
			if (!hit && (cat.items.length || cat.blocks.length)) {
				if (classHits === null) classHits = this._classHits(item, ctx.classCache)
				hit = classHits[n] === true
			}
			if (!hit) continue
			if (cat.edible !== undefined) {
				if (edible === null) edible = !!item.isEdible()
				if (edible !== cat.edible) continue
			}
			if (cat.except.some(m => this._matches(m, id, ctx.tagSets))) continue
			if (container === null) container = !!item.hasCraftingRemainingItem()
			if (container) return null
			var chance = this.chanceOf(cat.chance)
			if (isNaN(chance)) {
				console.warn('[compost] category ' + cat.name + ' has no usable chance: ' + cat.chance)
				continue
			}
			return { name: cat.name, chance: chance }
		}
		return null
	},

	_ruleContext(tagSets) {
		var tagHits = {}      // id -> { categoryIndex: true } for each category one of its tags hits
		var sources = []
		var ignoreCase = false
		for (var n = 0; n < this.CATEGORIES.length; n++) {
			var cat = this.CATEGORIES[n]
			cat.tags.forEach(t => {
				for (var id in tagSets[t]) (tagHits[id] = tagHits[id] || {})[n] = true
			})
			cat.ids.forEach(re => {
				sources.push('(?:' + re.source + ')')
				if (re.ignoreCase) ignoreCase = true
			})
		}
		return {
			tagSets: tagSets,
			tagHits: tagHits,
			idGate: sources.length ? new RegExp(sources.join('|'), ignoreCase ? 'i' : '') : null,
			classCache: {},
		}
	},

	// { categoryIndex: true } for each category whose items / blocks classes this item hits. Thousands
	// of items share a handful of item + block classes (every slab is a SlabBlock), so the instanceof
	// checks run once per class pair and are cached for the rest of this rebuild.
	_classHits(item, cache) {
		var block = item instanceof $CompostBlockItem ? item.getBlock() : null
		var key = String(item.getClass().getName()) + '|' + (block ? String(block.getClass().getName()) : '')
		var hits = cache[key]
		if (hits) return hits
		hits = {}
		for (var n = 0; n < this.CATEGORIES.length; n++) {
			var cat = this.CATEGORIES[n]
			for (var ic = 0; ic < cat.items.length; ic++) if (item instanceof cat.items[ic]) hits[n] = true
			for (var bc = 0; block && bc < cat.blocks.length; bc++) if (block instanceof cat.blocks[bc]) hits[n] = true
		}
		cache[key] = hits
		return hits
	},

	_excluded(id, tagSets) {
		return this.EXCLUDES.some(m => this._matches(m, id, tagSets))
	},

	_matches(m, id, tagSets) {
		if (m.kind === 'id') return m.value === id
		if (m.kind === 'tag') return !!tagSets[m.value][id]
		return m.value.test(id)
	},

	// Every tag any rule or exclusion names -> { id: true }. Read straight off the item registry,
	// which holds the tags that just loaded.
	_tagSets() {
		var names = {}
		this.CATEGORIES.forEach(c => {
			c.tags.forEach(t => names[t] = true)
			c.except.forEach(m => { if (m.kind === 'tag') names[m.value] = true })
		})
		this.EXCLUDES.forEach(m => { if (m.kind === 'tag') names[m.value] = true })
		var sets = {}
		for (var name in names) {
			var set = {}
			var key = $CompostTagKey.create($CompostRegistry.ITEM_REGISTRY, $CompostResourceLocation.of(name, ':'))
			var it = $CompostRegistry.ITEM.getTagOrEmpty(key).iterator()
			var count = 0
			while (it.hasNext()) {
				set[String($CompostRegistries.ITEMS.getKey(it.next().value()))] = true
				count++
			}
			if (count === 0) console.warn('[compost] tag #' + name + ' is empty or missing')
			sets[name] = set
		}
		return sets
	},

	// COMPOSTABLES -> { id: chance }. Keys are ItemLike; a mod that put a Block in is normalised to its item.
	_snapshot(table) {
		var out = {}
		var it = table.object2FloatEntrySet().iterator()
		while (it.hasNext()) {
			var en = it.next()
			var item = en.getKey().asItem()
			out[String($CompostRegistries.ITEMS.getKey(item))] = en.getFloatValue()
		}
		return out
	},

	_matcher(e) {
		if (e instanceof RegExp) return { raw: String(e), kind: 'regex', value: e }
		var s = String(e)
		if (s.charAt(0) === '#') return { raw: s, kind: 'tag', value: this._tagName(s) }
		return { raw: s, kind: 'id', value: s.indexOf(':') < 0 ? 'minecraft:' + s : s }
	},

	_tagName(t) {
		var s = String(t)
		return s.charAt(0) === '#' ? s.substring(1) : s
	},

	_classes(names, pkg) {
		var out = []
		names.forEach(n => {
			var full = n.indexOf('.') < 0 ? pkg + n : n
			try {
				out.push(Java.loadClass(full))
			} catch (err) {
				console.warn('[compost] class not found, rule part skipped: ' + full)
			}
		})
		return out
	},

	_whenOn(fn) {
		try {
			return !!fn()
		} catch (err) {
			console.warn('[compost] a values() when check threw, rows skipped: ' + err)
			return false
		}
	},

	_arr(v) {
		if (v === undefined || v === null) return []
		return Array.isArray(v) ? v : [v]
	},
}

// Rebuild on every tag load. Registered once per game session: a startup-script reload would otherwise
// stack a second listener, and this one always calls whatever global.compost is current.
if (!global.compostListening) {
	global.compostListening = true
	ForgeEvents.onEvent('net.minecraftforge.event.TagsUpdatedEvent', event => {
		try {
			global.compost.apply(event.getUpdateCause())
		} catch (err) {
			console.error('[compost] rebuild failed, composter table left as it was: ' + err)
		}
	})
}
