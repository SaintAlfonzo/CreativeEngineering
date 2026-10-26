// kubejs/startup_scripts/func/neoCraftHelper.js

console.log('Neo Craft Helper starting')

var $NCH_ForgeRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
var $NCH_ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')
var $NCH_CEConfigClass // lazily loaded/cached by NCH.cfg() below - undefined = not tried yet, false = unavailable

var i = global.item

global.NCH = {

  modId: 'creativeengineering',
  AXE: '#forge:tools/axes',
  strict: true, // true = throw on invalid item/fluid ids, false = warn and continue

  // ========================================================================
  // Core id resolution & validation
  // ========================================================================

	toId(output, input, process) {
		const resolve = (v) => {
		  if (!v) return 'unknown'

		  if (Array.isArray(v))
		    return resolve(v[0])

		  if (typeof v === 'string')
		    return v

		  var ingId = global.NCH._ingredientId(v)
      		if (ingId) return ingId


		  // IMPORTANT: resolve ItemStacks through toJson/_resolveIdCount
		  // rather than getId(), because getId() may return
		  // "1 crushed_raw_iron" rather than an actual item ID.
		  if (typeof v.toJson === 'function') {
		    try {
		      var json = v.toJson()
		      var id = json.item ?? json.id ?? json.tag
		      if (id) return String(id)
		    } catch (err) {}
		  }

		  if (v.item && typeof v.item === 'string')
		    return v.item

		  if (v.fluid && typeof v.fluid === 'string')
		    return v.fluid

		  if (v.id && typeof v.id === 'string')
		    return v.id

		  return String(v)
		}

		const clean = (v) => {
			var id = global.NCH._cleanResolvedId(resolve(v))

			// Remove namespace on regular items/fluids, but keep it for tags
			// (a bare tag path alone is often ambiguous/not descriptive enough):
			// minecraft:granite      -> granite
			// create:red_sand        -> red_sand
			// #forge:ores/copper     -> forge_ores_copper
			var isTag = id.startsWith('#')

			if (isTag) {
				id = id.substring(1)
			} else {
				var colon = id.indexOf(':')
				if (colon >= 0) {
					id = id.substring(colon + 1)
				}
			}

			return id
				.replace(/[^a-zA-Z0-9._-]/g, '_')
				.replace(/_+/g, '_')
				.replace(/^_+|_+$/g, '')
				.toLowerCase()
		}

		const base = `${clean(input)}_to_${clean(output)}`

		return process
		  ? `${base}_by_${process.toLowerCase()}`
		  : base
	},

  _isTag(id) {
    return typeof id === 'string' && id.startsWith('#')
  },
  
  _cleanResolvedId(id) {
    if (id == null) return undefined

    id = String(id).trim()

    // Remove accidental wrapping quotes introduced by Rhino/KubeJS
    if (
      id.length >= 2 &&
      (
        (id.startsWith("'") && id.endsWith("'")) ||
        (id.startsWith('"') && id.endsWith('"'))
      )
    ) {
      id = id.substring(1, id.length - 1).trim()
    }

    // KubeJS ItemStack representations can be:
    //   "minecraft:redstone"
    //   "2x minecraft:redstone"
    //
    // Never allow the stack count to become part of the item ID.
    var stackMatch = id.match(/^(\d+)x\s+(.+)$/)

    if (stackMatch) {
      id = stackMatch[2].trim()
    }

    return id
  },

    // Resolves a Forge/KubeJS Ingredient (Ingredient.of('#tag') / Ingredient.of('item') /
  // Ingredient.of(stack)) to its canonical id: '#namespace:path' for a tag, 'namespace:path'
  // for an item. Returns undefined for anything that isn't an Ingredient (incl. ItemStacks,
  // which also have toJson()) so it's safe to call from any resolver.
  // NOTE: a multi-entry ingredient (Ingredient.of('a', 'b')) resolves to its FIRST entry only —
  // fine for validation and id-slug purposes, which is all NCH uses this for.
  _ingredientId(v) {
    if (v == null || typeof v !== 'object') return undefined

    // KubeJS Ingredient (compound / tag / .or() chains) or InputItem
    // (Ingredient.of([...]).withCount(n), Item.of('#tag', n)): resolve to a representative id.
    var likeId = global.NCH._ingredientLikeId(v)
    if (likeId) return likeId

    if (typeof v.toJson !== 'function' || typeof v.getItems !== 'function') return undefined
    if (typeof v.getCount === 'function') return undefined // ItemStack, not an Ingredient

    var parsed
    try {
      parsed = JSON.parse(String(v.toJson()))
    } catch (err) {
      return undefined
    }

    var first = Array.isArray(parsed) ? parsed[0] : parsed
    if (!first || typeof first !== 'object') return undefined

    if (typeof first.tag === 'string') return '#' + first.tag
    if (typeof first.item === 'string') return first.item
    return undefined
  },

  // Recognises a KubeJS Ingredient (incl. compound "match any" ingredients from
  // Ingredient.of(['#a','#b']) / .or() chains) or an InputItem (an Ingredient + a count, from
  // Ingredient.of(...).withCount(n)). Returns the KubeJS Ingredient, or null if v isn't one.
  _ingredientLike(v) {
    if (v == null || typeof v !== 'object' || Array.isArray(v)) return null
    var isIng = false
    try { isIng = typeof Ingredient.isIngredient === 'function' && Ingredient.isIngredient(v) } catch (err) {}
    // Already a real Ingredient (incl. custom subtypes like PartialNBTIngredient from
    // .weakNBT()/.strongNBT()) - return it as-is. Re-wrapping via Ingredient.of(v) below
    // doesn't pass a PartialNBTIngredient through cleanly: it silently comes back matching
    // zero items, which made _validateAny falsely flag every NBT-matched syringe ingredient
    // as "matches no items" and (with global.NCH.strict on) throw, discarding the whole
    // recipe before it ever reached the real e.shapeless(...)/e.shaped(...) call - see
    // server_scripts/cooking/cultivated_meat.js history.
    if (isIng) return v
    // InputItem: public `ingredient` + `count` fields, plus kjs$asIngredient()
    var isInputItem = (v.ingredient !== undefined && v.count !== undefined)
      || (typeof v.asIngredient === 'function' && typeof v.getCount !== 'function')
    if (!isInputItem) return null
    try {
      var ing = Ingredient.of(v)
      return ing || null
    } catch (err) {
      return null
    }
  },

  // A representative id ('#tag' or 'namespace:path') for an Ingredient / InputItem, or undefined.
  _ingredientLikeId(v) {
    var ing = global.NCH._ingredientLike(v)
    if (!ing) return undefined
    try {
      var parsed = JSON.parse(String(ing.toJson()))
      var first = Array.isArray(parsed) ? parsed[0] : parsed
      if (first && typeof first === 'object') {
        if (typeof first.tag === 'string') return '#' + first.tag
        if (typeof first.item === 'string') return first.item
      }
    } catch (err) {}
    try {
      var ids = ing.itemIds
      if (ids && ids.length) return String(ids[0])
    } catch (err) {}
    return undefined
  },


  // Resolves any accepted item-like shape (string, Item.of() stack, {item}/{tag}/{id} object) to {id, count}
  _resolveIdCount(v) {
    if (typeof v === 'string') return { id: v, count: 1 }
    if (v == null) return { id: undefined, count: 1 }

	// Forge/KubeJS Ingredient (tag or item)
    var ingId = global.NCH._ingredientId(v)
    if (ingId) return { id: ingId, count: 1 }

    var id
    var count

    // Preferred: ask the Forge registry directly for this stack's Item key.
    // This is the most reliable way to resolve a KubeJS ItemStack.
    if (typeof v.getItem === 'function') {
      try {
        var itemObj = v.getItem()
        var itemKey = $NCH_ForgeRegistries.ITEMS.getKey(itemObj)
        if (itemKey) id = String(itemKey)
      } catch (err) {}
    }

    // Get the stack count as early as possible.
    if (typeof v.getCount === 'function') {
      try {
        count = v.getCount()
      } catch (err) {}
    }

    if (count === undefined && typeof v.count === 'number') {
      count = v.count
    }

    // Fallback: toJson(), where available and successful.
    if (!id && typeof v.toJson === 'function') {
      try {
        var json = v.toJson()

        if (json) {
          var jsonId = json.item ?? json.id ?? json.tag

          if (typeof jsonId === 'string') {
            id = jsonId
          }
        }
      } catch (err) {}
    }

    // Last-resort explicit properties.
    if (!id && typeof v.getId === 'function') {
      try {
        var rawId = v.getId()

        if (typeof rawId === 'string') {
          id = rawId
        }
      } catch (err) {}
    }

    if (!id && typeof v.id === 'string') {
      id = v.id
    }

    if (!id && typeof v.item === 'string') {
      id = v.item
    }

    if (!id && typeof v.tag === 'string') {
      id = v.tag
    }

    // IMPORTANT:
    // Only now inspect String(v).
    //
    // KubeJS ItemStack commonly stringifies as:
    //   "minecraft:redstone"
    //   "2x minecraft:redstone"
    //
    // Do NOT assign String(v) directly to id before parsing it.
    if (!id) {
      var str = String(v).trim()
      var match = str.match(/^(\d+)x\s+(.+)$/)

      if (match) {
        if (count === undefined) {
          count = Number(match[1])
        }

        id = match[2].trim()
      } else {
        id = str
      }
    }

    if (count === undefined) {
      count = 1
    }

    id = global.NCH._cleanResolvedId(id)

    return {
      id: id,
      count: count
    }
  },

  // Resolves any accepted fluid-like shape (string, Fluid.of() stack, {fluid}/{tag} object) to {id, amount}
	_resolveFluidAmount(v, defaultAmount) {
		var amt = defaultAmount != null ? defaultAmount : 1000 // not '??': this Rhino build turns undefined ?? x into 0

		if (typeof v === 'string') return { id: v, amount: amt }
		if (v == null) return { id: undefined, amount: amt }

		var id

		// Preferred: direct Forge registry lookup via getFluid(), same reasoning as
		// _resolveIdCount's getItem() lookup - avoids Rhino bean-mapping garbage.
		if (typeof v.getFluid === 'function') {
		  try {
		    var fluidObj = v.getFluid()
		    var fluidKey = $NCH_ForgeRegistries.FLUIDS.getKey(fluidObj)
		    if (fluidKey) id = String(fluidKey)
		  } catch (err) {}
		}

		if (!id && typeof v.toJson === 'function') {
		  try {
		    var json = v.toJson()
		    if (json) {
		      var jsonId = json.fluid ?? json.id ?? json.tag
		      if (typeof jsonId === 'string') id = String(jsonId)
		    }
		  } catch (err) {}
		}

		if (!id && typeof v.getId === 'function') {
		  try {
		    var rawId = v.getId()
		    if (typeof rawId === 'string') id = rawId
		  } catch (err) {}
		}
		if (!id && typeof v.fluid === 'string') id = v.fluid
		if (!id && typeof v.tag === 'string') id = v.tag
		if (!id && typeof v.id === 'string') id = v.id

		var amount
		if (typeof v.getAmount === 'function') {
		  try { amount = v.getAmount() } catch (err) {}
		}
		if (amount === undefined && typeof v.amount === 'number') amount = v.amount

		return { id: id, amount: Number(amount != null ? amount : amt) }
	},

  _checkItem(id, context) {
    if (!id) {
      global.NCH._warn(context, 'resolved to an undefined/empty item id - check for an unset global.item key')
      return false
    }
    var valid = global.NCH._isTag(id)
      ? (Ingredient.of(id).itemIds.length > 0)
      : Item.exists(id)
    if (!valid) global.NCH._warn(context, 'item/tag "' + id + '" does not exist or is empty')
    return valid
  },

  _fluidExists(id) {
    try {
		return $NCH_ForgeRegistries.FLUIDS.containsKey($NCH_ResourceLocation.of(id, ':'))
	} catch (err) {
		return false
    }
  },

  _checkFluid(id, context) {
    if (!id) {
		global.NCH._warn(context, 'resolved to an undefined/empty fluid id')
		return false
    }
    if (global.NCH._isTag(id)) {
		console.warn('[NCH] ' + context + ': fluid tag "' + id + '" is not validated (tag support not yet implemented)')
		return true
	}
    if (!global.NCH._fluidExists(id)) {
		global.NCH._warn(context, 'fluid "' + id + '" does not exist')
		return false
    }
    return true
  },

	_warn(context, message) {
		var text = '[NCH] ' + context + ': ' + message
		if (global.NCH.strict) throw new Error(text)
		console.warn(text)
	},

  // Validates a single item-like or fluid-like value, or recurses through arrays of them.
  _validateAny(v, context) {
    if (v == null) {
		global.NCH._warn(context, 'is undefined/null')
		return
    }
    if (Array.isArray(v)) {
		v.forEach((entry, idx) => {
			global.NCH._validateAny(entry, context + '[' + idx + ']')
		})
		return
    }
    // KubeJS Ingredient / InputItem (compound tags, .or() chains, .withCount()): validate against
    // the ingredient's own match set - it has no single canonical id to resolve.
    var asIngredient = global.NCH._ingredientLike(v)
    if (asIngredient) {
      var matched
      try { matched = asIngredient.itemIds } catch (err) {}
      if (!matched || matched.length === 0)
        global.NCH._warn(context, 'ingredient / InputItem matches no items (empty tag or bad id)')
      return
    }

    var looksLikeFluid = typeof v === 'object' &&
	(v	.fluid !== undefined || v.amount !== undefined || typeof v.getAmount === 'function')
    if (looksLikeFluid) {
		global.NCH._checkFluid(global.NCH._resolveFluidAmount(v).id, context)
	} else {
		global.NCH._checkItem(global.NCH._resolveIdCount(v).id, context)
    }
  },

  // ========================================================================
  // Shared JSON shape builders (for e.custom recipes)
  // ========================================================================

  // opts.countMode: 'flat' (default) | 'base_ingredient' | 'stacked_item' | 'none'
  // opts.allowTag: default true - set false for result slots that must always be a concrete item
  // opts.includeChance: default false - carries an inline chance field through from a withChance()-style object
  // opts.context: string used in validation warnings
  _itemJson(v, opts) {
    opts = opts || {}

    // Array = "match any of these" (mirrors the array-valued shaped()/createX() key
    // convention). Forge ingredient JSON encodes that as a JSON array of item/tag
    // objects, so resolve each entry on its own (no per-entry count/chance - an
    // alternatives list has no single count to attach).
    if (Array.isArray(v)) {
      var arrCtx = opts.context || 'unnamed NCH call'
      return v.map((entry, idx) =>
        global.NCH._itemJson(entry, { allowTag: opts.allowTag, countMode: 'none', context: arrCtx + '[' + idx + ']' })
      )
    }

    var countMode = opts.countMode || 'flat'
    var allowTag = opts.allowTag !== false
    var context = opts.context || 'unnamed NCH call'

    var resolved = global.NCH._resolveIdCount(v)
    global.NCH._checkItem(resolved.id, context)

    var isTag = allowTag && global.NCH._isTag(resolved.id)
    var key = isTag ? 'tag' : 'item'
    var idValue = isTag ? resolved.id.substring(1) : resolved.id

    var json
    if (resolved.count <= 1 || countMode === 'none') {
      json = {}
      json[key] = idValue
    } else if (countMode === 'base_ingredient') {
      var base = {}
      base[key] = idValue
      json = { base_ingredient: base, count: resolved.count }
    } else if (countMode === 'stacked_item') {
      json = { type: 'pneumaticcraft:stacked_item' }
      json[key] = idValue
      json.count = resolved.count
    } else { // 'flat'
      json = {}
      json[key] = idValue
      json.count = resolved.count
    }

    // Carry an inline output chance through from a KubeJS OutputItem (Item.of(x).withChance(n))
    // or a hand-written { chance: n } object. Coerce to a primitive JS number - Rhino hands back
    // OutputItem.chance as a boxed java.lang.Double, which KubeJS's JSON serialiser drops on the
    // floor, leaving the result at an implicit 100%.
    if (opts.includeChance && v && typeof v === 'object') {
      var ch
      if (typeof v.hasChance === 'function' && typeof v.getChance === 'function') {
        try { if (v.hasChance()) ch = Number(v.getChance()) } catch (err) {}
      }
      if (ch === undefined && v.chance !== undefined) {
        var parsedChance = Number(v.chance)
        if (!isNaN(parsedChance)) ch = parsedChance
      }
      if (typeof ch === 'number' && ch >= 0 && ch < 1) json.chance = ch
    }

    return json
  },

  // NBT-aware ingredient / result JSON. Currently used ONLY by ieBlueprint (the Engineer's
  // Workbench) — every other ie* / create* helper still goes through plain _itemJson.
  //
  // Recognises, in addition to everything _itemJson handles:
  //   - a hand-written ingredient object: { type: 'forge:partial_nbt'|'forge:nbt', item, nbt }
  //     (or a full { base_ingredient: {...}, count } ) -> used verbatim
  //   - the shorthand { item: 'id', nbt: '{...}' [, count] } -> forge:partial_nbt ingredient
  //   - a KubeJS ItemStack carrying NBT, e.g. Item.of('minecraft:potion', '{Potion:"..."}')
  // opts.role: 'input' (default) -> an Ingredient / IngredientWithSize ; 'result' -> { item,count,nbt }
  _nbtItemJson(v, opts) {
    opts = opts || {}
    var role = opts.role || 'input'
    var wrap = opts.countMode === 'base_ingredient'

    // STRING type/nbt only — a KubeJS OutputItem bean-maps `.nbt` to a CompoundTag/null.
    var isRawJson = v != null && typeof v === 'object' && typeof v.getItem !== 'function'
      && (typeof v.type === 'string' || typeof v.nbt === 'string' || v.base_ingredient !== undefined)

    var stackNbt
    try {
      if (v && typeof v.getTag === 'function') {
        var t = v.getTag()
        if (t) { var s = String(t); if (s && s !== '{}') stackNbt = s }
      }
    } catch (err) {}

    // No NBT anywhere -> behave exactly like _itemJson.
    if (!isRawJson && stackNbt === undefined) {
      return global.NCH._itemJson(v, opts)
    }

    if (role === 'result') {
      if (isRawJson) return v
      var rr = global.NCH._resolveIdCount(v)
      var res = { item: rr.id, nbt: stackNbt }
      if (rr.count && rr.count > 1) res.count = rr.count
      return res
    }

    // role === 'input'
    var ing, cnt
    if (isRawJson) {
      if (v.base_ingredient !== undefined) return v
      cnt = (typeof v.count === 'number') ? v.count : 1
      if (v.type !== undefined) {
        ing = {}
        for (var k in v) { if (v.hasOwnProperty(k) && k !== 'count') ing[k] = v[k] }
      } else {
        ing = { type: 'forge:partial_nbt', item: v.item, nbt: v.nbt }
      }
    } else {
      var ri = global.NCH._resolveIdCount(v)
      cnt = ri.count || 1
      ing = { type: 'forge:partial_nbt', item: ri.id, nbt: stackNbt }
    }
    return (wrap && cnt > 1) ? { base_ingredient: ing, count: cnt } : ing
  },

  // opts.allowTag: default true
  // opts.defaultAmount: fallback amount when v doesn't carry its own (default 1000)
  // opts.context: string used in validation warnings
  _fluidJson(v, opts) {
    opts = opts || {}
    var context = opts.context || 'unnamed NCH call'
    var allowTag = opts.allowTag !== false
    var resolved = global.NCH._resolveFluidAmount(v, opts.defaultAmount)
    global.NCH._checkFluid(resolved.id, context)

    var isTag = allowTag && global.NCH._isTag(resolved.id)
    var json = {}
    json[isTag ? 'tag' : 'fluid'] = isTag ? resolved.id.substring(1) : resolved.id
    json.amount = resolved.amount

    if (v && typeof v === 'object') {
      try {
        var nbt = v.getTag ? v.getTag() : v.nbt
        if (nbt) json.nbt = nbt
      } catch (err) {}
    }

    return json
  },

  // ========================================================================
  // Recipe removal / replacement
  // ========================================================================

  remove(id, e) {
    e.remove(id)
  },

  replaceInput(filter, oldInput, newInput, e, includeAdded) {
    if (!e) throw new Error('replaceInput called without event object!')
    global.NCH._validateAny(oldInput, 'replaceInput old input')
    global.NCH._validateAny(newInput, 'replaceInput new input')
    e.replaceInput(filter, oldInput, newInput)
    return includeAdded ? global.NCH._replaceInAdded(filter, 'input', oldInput, newInput, e) : 0
  },

  replaceOutput(filter, oldOutput, newOutput, e, includeAdded) {
    if (!e) throw new Error('replaceOutput called without event object!')
    global.NCH._validateAny(oldOutput, 'replaceOutput old output')
    global.NCH._validateAny(newOutput, 'replaceOutput new output')
    e.replaceOutput(filter, oldOutput, newOutput)
    return includeAdded ? global.NCH._replaceInAdded(filter, 'output', oldOutput, newOutput, e) : 0
  },

  // KubeJS 1902.6's e.replaceInput / e.replaceOutput / e.remove only walk the recipes loaded from data
  // (originalRecipes), never the ones scripts added (addedRecipes). With includeAdded, replaceInput /
  // replaceOutput repeat the swap on the added recipes for a plain { input: X } / { output: X } filter (item
  // merges: a purged id must go everywhere). Opt-in because a swap replaces the whole matching slot: sugar ->
  // #forge:sugar would turn a [sugar, honey] slot into the tag alone. Returns the number of added recipes changed.
  _replaceInAdded(filter, key, oldValue, newValue, e) {
    if (filter == null || typeof filter !== 'object' || Array.isArray(filter)) return 0
    var keys = Object.keys(filter)
    if (keys.length !== 1 || keys[0] !== key) return 0
    var changed = 0
    var it = e.addedRecipes.iterator()
    while (it.hasNext()) {
      var r = it.next()
      if (key === 'input') {
        if (r.hasInput(filter.input) && r.replaceInput(oldValue, newValue)) changed++
      } else if (r.hasOutput(filter.output) && r.replaceOutput(oldValue, newValue)) changed++
    }
    return changed
  },

  // ========================================================================
  // Config-driven recipe values
  // ========================================================================

  // Takes a whole defaults object for a config-driven craft (keys must match the
  // CEConfig section's Java field names exactly, e.g. potion/mb/loops/blows, not the toml
  // keys potionMb/hammerBlows) and a
  // dotted path to that section (e.g. 'CRAFTING.FELSIC_SWORD'), and returns a new object
  // with each field replaced by its CEConfig value where available. Falls back to the
  // given default field-by-field if patcherMod's CEConfig isn't on the classpath or the
  // section/field doesn't resolve. Logs whenever a resolved value differs from its
  // default, so an override in config/creativeengineering-common.toml shows up in the log
  // instead of silently changing recipe output. Meant to sit right next to the recipe it
  // feeds:
  //
  //   const felsicSword = R.cfg({
  //     potion: 'creativeengineering:magic_resistance',
  //     mb: 250,
  //     loops: 1,
  //     blows: 3
  //   }, 'CRAFTING.FELSIC_SWORD')
  //
  // Recipes are baked at datapack-reload time (ServerEvents.recipes) - a toml edit needs
  // /reload to reach an already-generated recipe, same as any other config-driven recipe.
  cfg(defaults, path) {
    if ($NCH_CEConfigClass === undefined) {
      try {
        $NCH_CEConfigClass = Java.loadClass('com.SaintAlfonzo.creativeengineering.CEConfig')
      } catch (err) {
        $NCH_CEConfigClass = false
        console.warn('[NCH.cfg] patcherMod CEConfig not on classpath - config-driven recipe values will use their hardcoded defaults: ' + err)
      }
    }
    if (!$NCH_CEConfigClass) return defaults

    var section = path.split('.').reduce((obj, seg) => (obj == null ? null : obj[seg]), $NCH_CEConfigClass)
    if (!section) {
      console.warn('[NCH.cfg] no CEConfig.' + path + ' section - using hardcoded defaults')
      return defaults
    }

    var values = {}
    Object.keys(defaults).forEach(field => {
      var configField = section[field]
      if (!configField || typeof configField.get !== 'function') {
        console.warn('[NCH.cfg] no CEConfig.' + path + '.' + field + ' field - using default ' + defaults[field])
        values[field] = defaults[field]
        return
      }

      var value = configField.get()
      if (value != defaults[field]) {
        console.log('[NCH.cfg] ' + path + '.' + field + ' overridden: default ' + defaults[field] + ' -> ' + value)
      }
      values[field] = value
    })
    return values
  },

	bulk(name, paramSets) {
	if (!name) throw new Error('bulk called without function name!')
	if (!Array.isArray(paramSets)) throw new Error('bulk called without parameter sets array!')

	const func = global.NCH[name]
	if (typeof func !== 'function') {
		throw new Error('Helper function "' + name + '" does not exist!')
	}

	paramSets.forEach((params, idx) => {
		if (!Array.isArray(params)) {
		console.error('[NCH] bulk(' + name + ')[' + idx + '] encountered invalid parameter set — skipping')
		return
		}
		try {
		func.apply(global.NCH, params)
		} catch (err) {
		console.error('[NCH] bulk(' + name + ')[' + idx + '] failed: ' + err.message, err)
		}
	})
	},

  // ========================================================================
  // Vanilla-style recipes
  // ========================================================================

  // cans: a recipe that uses food cans (feature_writeup/can_parity.md, decision D-C: manual crafts only).
  //   true / filter / array of filters (item, '#tag', slot index): the cans lose one serving instead of
  //     being used up (patcherMod food.CanServings; the last serving rolls empty/crushed/nothing);
  //     true = every can slot (#creativeengineering:cans).
  //   { whole: id or [ids] }: those cans are used up whole and must be full (an opened can gives no result).
  //   { servings: <as above>, whole: [...] }: both.
  //   R.shapeless(Item.of(i.beef_cooked), [i.canned_beef], e, 'uncan_beef', null, null, true)
  // Every can recipe is manual only: its id ends in _manual_only (Create's mixer, mechanical crafter and
  // their JEI categories skip it, AllRecipeTypes.shouldIgnoreInAutomation), and it has no result unless a
  // player's menu holds the grid (IE Assembler, AE2 and other auto-crafters craft on a menu without one).
  // A slot holding a stack of full cans opens a new can per craft (the opened one goes to the
  // inventory): vanilla's remainder rule. Put the opened can in the grid on its own to drain it.
  shaped(output, pattern, keys, e, idOverride, damage, cans) {
    if (!e) throw new Error('shaped called without event object!')
    var idSuffix = global.NCH._canId(idOverride ?? global.NCH.toId(output, Object.values(keys)[0], "shaped"), cans)
    var ctx = 'shaped ' + idSuffix

    global.NCH._validateAny(output, ctx + ' output')
    Object.keys(keys).forEach((k) => {
      global.NCH._validateAny(keys[k], ctx + ' key[' + k + ']')
    })

    var recipe = e
        .shaped(output, pattern, keys)
        .id(`${global.NCH.modId}:shaped/${idSuffix}`)

    if (damage) {
        var pairs = Array.isArray(damage[0]) ? damage : [damage]
        for (var d = 0; d < pairs.length; d++) {
            recipe.damageIngredient(pairs[d][0], pairs[d][1])
        }
    }
    global.NCH._canServings(recipe, cans, ctx)
    return recipe
	},

	shapeless(output, input, e, idOverride, damage, replace, cans) {
    if (!e) throw new Error('shapeless called without event object!')
    var idSuffix = global.NCH._canId(idOverride ?? global.NCH.toId(output, input, "shapeless"), cans)
    var ctx = 'shapeless ' + idSuffix

    global.NCH._validateAny(output, ctx + ' output')
    global.NCH._validateAny(input, ctx + ' input')

    var recipe = e
      .shapeless(output, input)
      .id(`${global.NCH.modId}:shapeless/${idSuffix}`)

    if (damage) {
        var pairs = Array.isArray(damage[0]) ? damage : [damage]
        for (var d = 0; d < pairs.length; d++) {
            recipe.damageIngredient(pairs[d][0], pairs[d][1])
        }
    }

    // replace: [filter, resultItem] or [[filter, resultItem], ...] — calls this recipe's own
    // recipe.replaceIngredient(filter, resultItem).
    // NOTE: this is NOT a crafting-remainder/leftover-item hook, despite reading like one -
    // RecipeJS#replaceIngredient takes an IngredientActionFilter and is KubeJS's bulk
    // ingredient-substitution tool (e.g. a script that swaps every recipe's vanilla iron
    // ingot for a modded one), not a per-recipe "leave this behind after crafting" mechanic.
    // Passing a plain Ingredient here silently produced a recipe that could never match any
    // input (see server_scripts/cooking/cultivated_meat.js history) - it just corrupts the
    // ingredient instead of throwing. For an actual crafting remainder (bucket -> empty
    // bucket, syringe -> empty syringe), set it on the ITEM at registration time instead:
    // e.create(id).containerItem('namespace:item_id') - see startup_scripts/items/sample_syringe.js.
    if (replace) {
        var rpairs = Array.isArray(replace[0]) ? replace : [replace]
        for (var r = 0; r < rpairs.length; r++) {
            recipe.replaceIngredient(rpairs[r][0], rpairs[r][1])
        }
    }
    global.NCH._canServings(recipe, cans, ctx)
    return recipe
	},

  // Can recipes are manual only (see shaped()): the id suffix Create looks for.
  _canId(idSuffix, cans) {
    if (!cans || String(idSuffix).endsWith('_manual_only')) return idSuffix
    return idSuffix + '_manual_only'
  },

  // True when a player crafts: a crafting table / inventory grid, or any menu holding a player's
  // inventory (storage crafting terminals). Machines build the grid on a menu with no player slots.
  _playerGrid(grid) {
    if (grid.getPlayer() != null) return true
    try {
      var menu = grid.getMenu()
      if (menu == null) return false
      var slots = menu.slots
      for (var n = 0; n < slots.size(); n++) {
        var c = slots.get(n).container
        if (c != null && c.player != null) return true
      }
    } catch (err) {
      if (!global.NCH._playerGridWarned) console.warn('[NCH] can recipe player check failed, crafting-table grids only: ' + err)
      global.NCH._playerGridWarned = true
    }
    return false
  },

  // The can guard of shaped() / shapeless(): manual only, whole cans full only, then the
  // creativeengineering:can_serving ingredient action. KubeJS empties its custom-action map at the
  // start of every server script load, so the Java callback is put back here on each call; the
  // action id and the logic both live in patcherMod compat.KubeJSCanAction.
  _canServings(recipe, cans, ctx) {
    if (!cans) return
    var opts = (cans === true || typeof cans === 'string' || typeof cans === 'number' || Array.isArray(cans))
      ? { servings: cans } : cans
    var whole = opts.whole ? [].concat(opts.whole) : []
    var servings = null
    if (whole.length) {
      try { servings = Java.loadClass('com.SaintAlfonzo.creativeengineering.food.CanServings') } catch (err) {
        console.warn('[NCH] ' + ctx + ': patcherMod CanServings not available, opened cans are not refused: ' + err)
      }
    }
    recipe.modifyResult((grid, result) => {
      if (!global.NCH._playerGrid(grid)) return Item.empty
      if (servings == null) return result
      for (var w = 0; w < whole.length; w++) {
        var found = grid.findAll(Ingredient.of(whole[w]))
        for (var s = 0; s < found.size(); s++) {
          if (servings.isPartial(found.get(s))) return Item.empty   // opened: not full
        }
      }
      return result
    })
    if (!opts.servings) return
    cans = opts.servings
    var action
    try {
      action = Java.loadClass('com.SaintAlfonzo.creativeengineering.compat.KubeJSCanAction')
      action.register()
    } catch (err) {
      console.warn('[NCH] ' + ctx + ': patcherMod KubeJSCanAction not available, cans are used up whole: ' + err)
      return
    }
    var filters = cans === true ? [global.item.t_cans] : (Array.isArray(cans) ? cans : [cans])
    for (var f = 0; f < filters.length; f++) {
      recipe.customIngredientAction(filters[f], String(action.ID))
    }
  },

	stonecutting(output, input, e, idOverride) {
		if (!e) throw new Error('stonecutting called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "stonecutting")
		var ctx = 'stonecutting ' + idSuffix

		global.NCH._validateAny(output, ctx + ' output')
		global.NCH._validateAny(input, ctx + ' input')

		e.stonecutting(output, input)
		.id(`${global.NCH.modId}:stonecutting/${idSuffix}`)
	},

	smithing(output, base, addition, e, idOverride) {
		if (!e) throw new Error('smithing called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, base, "smithing")
		var ctx = 'smithing ' + idSuffix

		global.NCH._validateAny(output, ctx + ' output')
		global.NCH._validateAny(base, ctx + ' base')
		global.NCH._validateAny(addition, ctx + ' addition')

		e
			.smithing(output, base, addition)
			.id(`${global.NCH.modId}:smithing/${idSuffix}`)
	},

	smelting(output, input, e, idOverride, xp, cookingtime, conditions) {
		if (!e) throw new Error('smelting called without event object!')
		xp = xp ?? 0
		cookingtime = cookingtime ?? 200
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "smelting")
		var ctx = 'smelting ' + idSuffix

		// NOTE: result is deliberately a raw item id string here, not an { item }
		// object - that's the actual vanilla smelting recipe JSON shape.
		var resolvedOutput = global.NCH._resolveIdCount(output)
		global.NCH._checkItem(resolvedOutput.id, ctx + ' output')

		const json = {
			type: 'minecraft:smelting',
			ingredient: global.NCH._itemJson(input, { countMode: 'none', context: ctx + ' input' }),
			result: resolvedOutput.id,
			experience: xp,
			cookingtime: cookingtime
		}
		if (conditions) json.conditions = conditions
		e.custom(json).id(`${global.NCH.modId}:smelting/${idSuffix}`)
	},

	blasting(output, input, e, idOverride, xp, cookingtime, conditions) {
		if (!e) throw new Error('blasting called without event object!')
		xp = xp ?? 0
		cookingtime = cookingtime ?? 100
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "blasting")
		var ctx = 'blasting ' + idSuffix

		// NOTE: result is deliberately a raw item id string here, not an { item }
		// object - that's the actual vanilla blasting recipe JSON shape.
		var resolvedOutput = global.NCH._resolveIdCount(output)
		global.NCH._checkItem(resolvedOutput.id, ctx + ' output')

		const json = {
			type: 'minecraft:blasting',
			ingredient: global.NCH._itemJson(input, { countMode: 'none', context: ctx + ' input' }),
			result: resolvedOutput.id,
			experience: xp,
			cookingtime: cookingtime
		}
		if (conditions) json.conditions = conditions
		e.custom(json).id(`${global.NCH.modId}:blasting/${idSuffix}`)
	},

	campfireCooking(output, input, e, idOverride, exp, time) {
		if (!e) throw new Error('campfireCooking called without event object!')
		exp = exp ?? 0
		time = time ?? 600
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "campfirecooking")
		var ctx = 'campfireCooking ' + idSuffix

		global.NCH._validateAny(output, ctx + ' output')
		global.NCH._validateAny(input, ctx + ' input')

		e
			.campfireCooking(output, input, exp, time)
			.id(`${global.NCH.modId}:campfirecooking/${idSuffix}`)
	},

	smoking(output, input, e, idOverride, xp, cookingtime) {
		if (!e) throw new Error('smoking called without event object!')
		xp = xp ?? 0
		cookingtime = cookingtime ?? 100
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "smoking")
		var ctx = 'smoking ' + idSuffix

		// NOTE: result is deliberately a raw item id string here, not an { item }
		// object - that's the actual vanilla smoking recipe JSON shape.
		var resolvedOutput = global.NCH._resolveIdCount(output)
		global.NCH._checkItem(resolvedOutput.id, ctx + ' output')

		e.custom({
			type: 'minecraft:smoking',
			ingredient: global.NCH._itemJson(input, { countMode: 'none', context: ctx + ' input' }),
			result: resolvedOutput.id,
			experience: xp,
			cookingtime: cookingtime
		}).id(`${global.NCH.modId}:smoking/${idSuffix}`)
	},

	fletching(arrowhead, shaft, fin, result, e, idOverride) {
		if (!e) throw new Error('fletching called without event object!')

		// Derive a naming source that works whether arrowhead comes in as a
		// string ("#forge:..." or an item id) or as an already-built {tag}/{item}
		// ingredient object - previously this collapsed to the literal word
		// "tag" for any object shape, causing id collisions between different
		// tag-based arrowheads.
		var arrowheadName = typeof arrowhead === 'string'
			? arrowhead
			: (arrowhead && arrowhead.tag ? '#' + arrowhead.tag : (arrowhead && arrowhead.item) ?? arrowhead)

		var idSuffix = idOverride ?? global.NCH.toId(result.item ?? result, arrowheadName, "fletching")
		var ctx = 'fletching ' + idSuffix

		global.NCH._validateAny(arrowhead, ctx + ' arrowhead')
		global.NCH._validateAny(shaft, ctx + ' shaft')
		global.NCH._validateAny(fin, ctx + ' fin')
		global.NCH._validateAny(result.item ?? result, ctx + ' result')

		var toIngredient = (val) => {
			if (typeof val === 'object') return val
			if (typeof val === 'string' && val.charAt(0) === '#') {
				return { tag: val.substring(1) }
			}
			return { item: val }
		}

		e.custom({
			type: 'nock_enough_arrows:fletching',
			arrowhead: toIngredient(arrowhead),
			shaft: toIngredient(shaft),
			fin: toIngredient(fin),
			result: { item: result.item ?? result, count: result.count ?? 1 }
		}).id(`${global.NCH.modId}:fletching/${idSuffix}`)
	},

  	fdCooking(input, output, e, idOverride, exp, time, container) {
		exp = exp ?? 0
		time = time ?? 200
		if (!e) throw new Error('fdCooking called without event object!')

		var idSuffix = idOverride ?? global.NCH.toId(output, input, "fdcooking")
		var ctx = 'fdCooking ' + idSuffix

		global.NCH._validateAny(input, ctx + ' input')
		global.NCH._validateAny(output, ctx + ' output')
		if (container) global.NCH._validateAny(container, ctx + ' container')

		let r = container
			? e.recipes.farmersdelight.cooking(input, output, exp, time, container)
			: e.recipes.farmersdelight.cooking(input, output, exp, time)

		r.id(`${global.NCH.modId}:farmersdelightcooking/${idSuffix}`)
	},

  	fdCutting(input, tool, output, e, idOverride) {
		if (!e) throw new Error('fdCutting called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "fdcutting")
		var ctx = 'fdCutting ' + idSuffix

		global.NCH._validateAny(input, ctx + ' input')
		global.NCH._validateAny(tool, ctx + ' tool')
		global.NCH._validateAny(output, ctx + ' output')

		e.recipes.farmersdelight
		.cutting(input, tool, output)
		.id(`${global.NCH.modId}:farmersdelightcutting/${idSuffix}`)
	},

	// Farmer's Respite kettle (farmersrespite:brewing). input: 1-2 ingredients. opts: { container (default glass
	// bottle), time (2400), exp (0.35), needWater (true), tab ('drinks') }. A drink tier like FR's long / strong
	// teas is the drink + one ingredient with needWater false.
	//   R.frBrewing([i.matcha_latte, i.t_fruits_berries], i.berry_matcha_latte, e, 'berry_matcha_latte', { needWater: false })
	frBrewing(input, output, e, idOverride, opts) {
		if (!e) throw new Error('frBrewing called without event object!')
		opts = opts || {}
		var inputs = Array.isArray(input) ? input : [input]
		var idSuffix = idOverride ?? global.NCH.toId(output, input, 'frbrewing')
		var ctx = 'frBrewing ' + idSuffix
		if (inputs.length < 1 || inputs.length > 2) global.NCH._warn(ctx, 'the kettle takes 1-2 ingredients')

		e.custom({
			type: 'farmersrespite:brewing',
			recipe_book_tab: opts.tab ?? 'drinks',
			ingredients: inputs.map((v, n) => global.NCH._itemJson(v, { countMode: 'none', context: ctx + ' input[' + n + ']' })),
			result: global.NCH._itemJson(output, { allowTag: false, context: ctx + ' result' }),
			container: global.NCH._itemJson(opts.container ?? global.item.glass_bottle, { allowTag: false, countMode: 'none', context: ctx + ' container' }),
			experience: opts.exp ?? 0.35,
			cookingtime: opts.time ?? 2400,
			needwater: opts.needWater ?? true
		}).id(`${global.NCH.modId}:farmersrespitebrewing/${idSuffix}`)
	},

	// Hammering by hand with the Engineer's Hammer, two ways to the same result: on a Farmer's Delight cutting
	// board (the hammer is the cutting tool and loses 1 durability) and in the crafting grid (hammer + input,
	// shapeless; IE's hammer comes back damaged). Ids farmersdelightcutting/<id>_hammering and
	// shapeless/<id>_hammering. The board must be in #creativeengineering:hammer_no_rotate (blocktags.js), or the
	// hammer rotates it instead of cutting.
	//   R.hammering(i.baking_sheet, i.any_plate_iron, e, 'baking_sheet')
	hammering(output, input, e, idOverride) {
		if (!e) throw new Error('hammering called without event object!')
		var idSuffix = idOverride ? idOverride + '_hammering' : global.NCH.toId(output, input, 'hammering')
		global.NCH.fdCutting(input, global.item.ie_hammer, output, e, idSuffix)
		global.NCH.shapeless(output, [global.item.ie_hammer, input], e, idSuffix)
	},

	// Oven baking, creativeengineering:baking (patcherMod oven/, feature_writeup/oven.md 2.2): the FD
	// cooking-pot JSON plus an optional `tool` (bakeware in the oven's tool slot, never used up).
	// input: one oven slot each (an array entry = any of those). Give a repeated ingredient once with a count,
	// Item.of(i.wheat, 3) or { id: '#tag', count: 3 } for a tag, and write the original recipe's full cost.
	// What the recipe gets depends on CEConfig [cooking] ovenIngredientCounts:
	//   on:  the count stays, and the oven takes that many from the one slot (exact cost, up to 6 slots)
	//   off (default): a count becomes that many slots of 1, FD-pot style. Over 6 slots, one item is dropped
	//        from the largest repeated group until it fits (ties: the group listed first, so list filler like
	//        milk/sugar/wheat before the defining ingredient). Each drop is logged.
	// opts: { exp, time, container, tool }
	//   R.ceBaking([i.t_fruits_fig, i.pie_crust, i.eggs], Item.of(i.fig_tart, 2), e, undefined,
	//     { exp: 0.1, time: 200, tool: i.t_bakeware_pie_tin, container: i.paper })
	ceBaking(input, output, e, idOverride, opts) {
		opts = opts || {}
		if (!e) throw new Error('ceBaking called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "baking")
		var ctx = 'ceBaking ' + idSuffix

		var inputs = Array.isArray(input) ? input : [input]
		if (!global.NCH.cfg({ ovenIngredientCounts: false }, 'COOKING').ovenIngredientCounts)
			inputs = global.NCH._bakingSlots(inputs, ctx)
		if (inputs.length < 1 || inputs.length > 6)
			global.NCH._warn(ctx, 'needs 1-6 ingredients, got ' + inputs.length)

		var json = {
			type: 'creativeengineering:baking',
			ingredients: inputs.map((v, idx) =>
				global.NCH._itemJson(v, { countMode: 'flat', context: ctx + ' input[' + idx + ']' })),
			result: global.NCH._itemJson(output, { allowTag: false, context: ctx + ' result' }),
			experience: opts.exp ?? 0,
			cookingtime: opts.time ?? 200
		}
		if (opts.tool) json.tool = global.NCH._itemJson(opts.tool, { countMode: 'none', context: ctx + ' tool' })
		if (opts.container)
			json.container = global.NCH._itemJson(opts.container, { allowTag: false, countMode: 'none', context: ctx + ' container' })

		e.custom(json).id(`${global.NCH.modId}:baking/${idSuffix}`)
	},

	// ceBaking with counts off: counted inputs -> one slot per item, dropping from the largest repeated
	// group (ties: first listed) while over the oven's 6 slots.
	_bakingSlots(inputs, ctx) {
		var groups = inputs.map(v => {
			if (Array.isArray(v)) return { v: v, id: String(v[0]), count: 1 }
			var r = global.NCH._resolveIdCount(v)
			return r.count > 1 ? { v: r.id, id: r.id, count: r.count } : { v: v, id: r.id, count: 1 }
		})
		var total = () => groups.reduce((s, g) => s + g.count, 0)
		var dropped = []
		while (total() > 6) {
			var big = null
			groups.forEach(g => { if (g.count > 1 && (!big || g.count > big.count)) big = g })
			if (!big) break // all single: too many distinct ingredients, ceBaking warns
			big.count--
			dropped.push(big.id)
		}
		if (dropped.length) console.log('[NCH] ' + ctx + ': over 6 slots, dropped ' + dropped.join(', '))
		var slots = []
		groups.forEach(g => { for (var k = 0; k < g.count; k++) slots.push(g.v) })
		return slots
	},

	// Vintage Delight Pickling Jar (backport, vintage_delight.md 2.3): vintagedelight:fermenting. No heat, no fluid.
	// output: item (Item.of(...) for a count or NBT). inputs: 1-6 ingredients, one jar slot each; Item.of(x, 2) takes
	// two slots, an array entry = any of those.
	// opts: { time: ticks (default 2400, the mod's pickles); secondary: item for the second output slot;
	//   container: item the jar consumes from its container slot (e.g. a glass bottle) }
	//   R.vdPickling(Item.of(i.kimchi, 2), [i.cabbage, i.vegetables, i.kelp, i.salt], e, 'kimchi')
	vdPickling(output, inputs, e, idOverride, opts) {
		opts = opts || {}
		if (!e) throw new Error('vdPickling called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, inputs, "pickling")
		var ctx = 'vdPickling ' + idSuffix

		var slots = []
		;(Array.isArray(inputs) ? inputs : [inputs]).forEach((v, idx) => {
			var count = Array.isArray(v) ? 1 : global.NCH._resolveIdCount(v).count
			var entry = global.NCH._itemJson(v, { countMode: 'none', context: ctx + ' input[' + idx + ']' })
			for (var c = 0; c < Math.max(1, count); c++) slots.push(entry)
		})
		if (slots.length < 1 || slots.length > 6)
			global.NCH._warn(ctx, 'needs 1-6 ingredient slots, got ' + slots.length)

		var json = {
			type: 'vintagedelight:fermenting',
			ingredients: slots,
			output: global.NCH._itemJson(output, { allowTag: false, context: ctx + ' result' }),
			processingTime: opts.time ?? 2400
		}
		if (opts.secondary)
			json.secondaryOutput = global.NCH._itemJson(opts.secondary, { allowTag: false, context: ctx + ' secondary' })
		if (opts.container)
			json.container = global.NCH._itemJson(opts.container, { countMode: 'none', context: ctx + ' container' })

		e.custom(json).id(`${global.NCH.modId}:pickling/${idSuffix}`)
	},

	// Brewin' and Chewin' keg (backport, alcohol.md 8.6): brewinandchewin:fermenting with 4.5.0's field names.
	// output: Fluid.of(id, mB) for a drink / cheese fluid, or an item (Item.of(...)) for a food result.
	// inputs: 1-4 ingredients, one keg slot each; an array entry = any of those, Item.of(x, 2) takes two slots.
	// baseFluid: fluid id, '#tag' or Fluid.of(id, mB) (1000 mB unless given); null = an empty tank.
	// opts: { temperature: 1 cold, 2 chilly, 3 normal (default), 4 warm, 5 hot; time: ticks (default 9600);
	//   xp (default 1); category: 'drinks' (default) | 'meals' }
	//   R.bncFermenting(Fluid.of(f.beer, 1000), [i.wheat, i.dandelion, i.brown_mushroom], f.water, e, 'beer_from_water')
	bncFermenting(output, inputs, baseFluid, e, idOverride, opts) {
		opts = opts || {}
		if (!e) throw new Error('bncFermenting called without event object!')
		var idSuffix = idOverride != null ? idOverride : global.NCH.toId(output, inputs, "fermenting")
		var ctx = 'bncFermenting ' + idSuffix

		var slots = []
		;(Array.isArray(inputs) ? inputs : [inputs]).forEach((v, idx) => {
			var count = Array.isArray(v) ? 1 : global.NCH._resolveIdCount(v).count
			var entry = global.NCH._itemJson(v, { countMode: 'none', context: ctx + ' input[' + idx + ']' })
			for (var c = 0; c < Math.max(1, count); c++) slots.push(entry)
		})
		if (slots.length < 1 || slots.length > 4)
			global.NCH._warn(ctx, 'needs 1-4 ingredient slots, got ' + slots.length)

		var json = {
			type: 'brewinandchewin:fermenting',
			ingredients: slots,
			experience: opts.xp != null ? opts.xp : 1.0,
			fermenting_time: opts.time != null ? opts.time : 9600,
			temperature: opts.temperature != null ? opts.temperature : 3,
			unit: 'millibuckets'
		}
		if (opts.category) json.category = opts.category

		if (baseFluid != null) {
			var base = global.NCH._fluidJson(baseFluid, { context: ctx + ' base fluid' })
			json.base_fluid = {
				amount: base.amount,
				ingredient: base.tag ? { tag: '#' + base.tag } : { id: base.fluid },
				unit: 'millibuckets'
			}
		}

		var looksLikeFluid = typeof output === 'object' && (typeof output.getAmount === 'function' || output.amount !== undefined)
		if (looksLikeFluid) {
			var res = global.NCH._fluidJson(output, { allowTag: false, context: ctx + ' result' })
			json.result = { id: res.fluid, amount: res.amount }
		} else {
			json.result = global.NCH._itemJson(output, { allowTag: false, context: ctx + ' result' })
		}

		e.custom(json).id(`${global.NCH.modId}:fermenting/${idSuffix}`)
	},

  // ========================================================================
  // Create
  // ========================================================================

	createItemApplication(output, target, tool, e, idOverride) {
		if (!e) throw new Error('createItemApplication called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, target, "itemapplication")
		var ctx = 'createItemApplication ' + idSuffix

		const json = {
		type: 'create:item_application',
		ingredients: [
		global.NCH._itemJson(target, { countMode: 'none', context: ctx + ' target' }),
		global.NCH._itemJson(tool, { countMode: 'none', context: ctx + ' tool' })
		],
		results: [global.NCH._itemJson(output, { countMode: 'flat', allowTag: false, context: ctx + ' output' })]
		}
		e.custom(json).id(`${global.NCH.modId}:createitemapplication/${idSuffix}`)
	},

	createHaunting(output, input, e, idOverride) {
		if (!e) throw new Error('createHaunting called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "haunting")
		var ctx = 'createHaunting ' + idSuffix

		global.NCH._validateAny(output, ctx + ' output')
		global.NCH._validateAny(input, ctx + ' input')

		e.recipes.create.haunting(output,input).id(`${global.NCH.modId}:createhaunting/${idSuffix}`)
	},

	createSplashing(output, input, e, idOverride) {
		if (!e) throw new Error('createSplashing called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "splashing")
		var ctx = 'createSplashing ' + idSuffix

		global.NCH._validateAny(output, ctx + ' output')
		global.NCH._validateAny(input, ctx + ' input')

		e.recipes.create.splashing(output,input).id(`${global.NCH.modId}:createsplashing/${idSuffix}`)
	},

	dragonBreathing(output, input, e, idOverride) {
		if (!e) throw new Error('dragonBreathing called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "dragonbreathing")
		var ctx = 'dragonBreathing ' + idSuffix

		var isRaw = (v) => v != null && typeof v === 'object'
		&& typeof v.toJson !== 'function'
		&& (typeof v.type === 'string' || typeof v.nbt === 'string')

		var ingredient
		if (isRaw(input)) {
		ingredient = input
		} else {
		global.NCH._validateAny(input, ctx + ' input')
		ingredient = global.NCH._itemJson(input, { countMode: 'none', context: ctx + ' input' })
		}

		var outputs = Array.isArray(output) ? output : [output]
		var results = outputs.map((o, n) => {
		if (isRaw(o)) return o
		global.NCH._validateAny(o, ctx + ' output ' + n)
			return global.NCH._itemJson(o, { countMode: 'flat', includeChance: true, context: ctx + ' output ' + n })
		})

		e.custom({
			type: `${global.NCH.modId}:dragon_breathing`,
			ingredients: [ingredient],
			results: results
		}).id(`${global.NCH.modId}:dragonbreathing/${idSuffix}`)
	},

	// Seasoning craft (patcherMod food/SeasoningRecipe): food + one of each seasoning's item, shapeless, gives the same
	// food with those seasonings added as NBT. seasonings: a global.seasoning key or an array of them
	// (startup_scripts/cooking/seasonings.js, which holds each one's item, bonus and what it combines with). Ids
	// shapeless/season/<keys>_on_<food>, so the Seasoning JEI page lists them.
	season(food, seasonings, e, idOverride) {
		if (!e) throw new Error('season called without event object!')
		var keys = Array.isArray(seasonings) ? seasonings : [seasonings]
		var foodId = global.NCH._resolveIdCount(food).id
		var idSuffix = idOverride ?? (keys.join('_') + '_on_' + String(foodId).replace(/^#/, '').replace(/[^a-zA-Z0-9_]/g, '_'))
		var ctx = 'season ' + idSuffix
		var parts = []
		for (var k = 0; k < keys.length; k++) {
			var s = global.seasoning && global.seasoning[keys[k]]
			if (!s) {
				global.NCH._warn(ctx, 'unknown seasoning ' + keys[k])
				return
			}
			parts.push({ seasoning: s.id, ingredient: global.NCH._itemJson(s.item, { countMode: 'none', context: ctx + ' ' + keys[k] }) })
		}
		e.custom({
			type: `${global.NCH.modId}:seasoning`,
			food: global.NCH._itemJson(food, { countMode: 'none', context: ctx + ' food' }),
			seasonings: parts
		}).id(`${global.NCH.modId}:shapeless/season/${idSuffix}`)
	},

	// Backpack skins (patcherMod backpackskin/): smithing a block from the skin tag onto any Sophisticated Backpack
	// keeps the backpack's data and sets its skin; the clear recipe (backpack alone in the grid) removes it.
	backpackSkin(skinTag, e, idOverride) {
		if (!e) throw new Error('backpackSkin called without event object!')
		var idSuffix = idOverride ?? 'backpack_skin'
		e.custom({
			type: `${global.NCH.modId}:backpack_skin`,
			addition: { tag: skinTag }
		}).id(`${global.NCH.modId}:smithing/${idSuffix}`)
	},

	backpackSkinClear(e, idOverride) {
		if (!e) throw new Error('backpackSkinClear called without event object!')
		var idSuffix = idOverride ?? 'backpack_skin_clear'
		e.custom({ type: `${global.NCH.modId}:backpack_skin_clear` }).id(`${global.NCH.modId}:backpackskin/${idSuffix}`)
	},

	elytraDuplication(catalyst, result, e, idOverride) {
		if (!e) throw new Error('elytraDuplication called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(result, catalyst, "elytraduplication")
		var ctx = 'elytraDuplication ' + idSuffix

		global.NCH._validateAny(catalyst, ctx + ' catalyst')
		global.NCH._validateAny(result, ctx + ' result')

		e.custom({
			type: `${global.NCH.modId}:elytra_duplication`,
			catalyst: global.NCH._itemJson(catalyst, { countMode: 'none', context: ctx + ' catalyst' }),
			result: global.NCH._itemJson(result, { countMode: 'flat', allowTag: false, context: ctx + ' result' })
		}).id(`${global.NCH.modId}:elytraduplication/${idSuffix}`)
	},

	echoAssembly(out, base, key, e, potionA, potionB) {
		if (!e) throw new Error('echoAssembly called without event object!')
		var inc = global.NCH.incomplete(key)
		global.NCH.createSequencedAssembly(Item.of(out, 1), base, [
			e.recipes.create.filling(inc, [inc, Fluid.of(global.fluid.lava, 250)]),
			e.recipes.create.deploying(inc, [inc, i.echo_ingot]),
			e.recipes.create.deploying(inc, [inc, i.amethyst_dust]),
			e.custom({ type: 'vintageimprovements:vibrating', ingredients: [{ item: inc }], results: [{ item: inc }] }),
			e.recipes.create.filling(inc, [inc, Fluid.of(global.fluid.create_potion, 250, '{Potion:"' + potionA + '"}')]),
			e.recipes.create.filling(inc, [inc, Fluid.of(global.fluid.create_potion, 250, '{Potion:"' + potionB + '"}')])
		], [key], e, key + '_sequenced_assembly', 1)
	},

	felsicAssembly(out, base, key, e, potion, mb, loops, hammerBlows) {
		if (!e) throw new Error('felsicAssembly called without event object!')
		var inc = global.NCH.incomplete(key)
		global.NCH.createSequencedAssembly(Item.of(out, 1), base, [
			e.recipes.create.filling(inc, [inc, Fluid.of(global.fluid.create_potion, mb, '{Bottle:"REGULAR",Potion:"creativeengineering:long_reach"}')]),
			e.recipes.create.deploying(inc, [inc, i.felsic_ingot]),
			e.custom({ type: 'vintageimprovements:hammering', hammerBlows: hammerBlows ?? 3, ingredients: [{ item: inc }], results: [{ item: inc }] }),
			e.recipes.create.filling(inc, [inc, Fluid.of(global.fluid.create_potion, mb, '{Bottle:"REGULAR",Potion:"' + potion + '"}')])
		], [key], e, key + '_sequenced_assembly', loops)
	},

	dragonAssembly(out, base, key, e, potion, mb, loops) {
		if (!e) throw new Error('dragonAssembly called without event object!')
		var inc = global.NCH.incomplete(key)
		global.NCH.createSequencedAssembly(Item.of(out, 1), base, [
			e.recipes.create.filling(inc, [inc, Fluid.of(global.fluid.dragon_breath, 250)]),
			e.recipes.create.deploying(inc, [inc, i.dragon_scale]),
			e.custom({ type: 'vintageimprovements:laser_cutting', ingredients: [{ item: inc }], results: [{ item: inc }] }),
			e.recipes.create.filling(inc, [inc, Fluid.of(global.fluid.create_potion, mb, '{Bottle:"LINGERING",Potion:"' + potion + '"}')])
		], [key], e, key + '_sequenced_assembly', loops)
	},

	createCrushing(output, input, e, idOverride, mirrorMilling) {
		if (!e) throw new Error('createCrushing called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "createcrushing")
		var ctx = 'createCrushing ' + idSuffix

		global.NCH._validateAny(output, ctx + ' output')
		global.NCH._validateAny(input, ctx + ' input')

		e.recipes.create.crushing(output,input).id(`${global.NCH.modId}:createcrushing/${idSuffix}`)

		// Optional: also register the same output/input as a Millstone recipe, for
		// ores/materials that should be processable on either machine (the Millstone
		// is simpler but doesn't need Crushing Wheels). Gets its own toId-derived id
		// rather than reusing idSuffix - a "_by_createcrushing" suffix on a milling
		// recipe would be misleading.
		if (mirrorMilling) global.NCH.createMilling(output, input, e)
	},

	createMilling(output, input, e, idOverride) {
		if (!e) throw new Error('createMilling called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "createmilling")
		var ctx = 'createMilling ' + idSuffix

		global.NCH._validateAny(output, ctx + ' output')
		global.NCH._validateAny(input, ctx + ' input')

		e.recipes.create.milling(output,input).id(`${global.NCH.modId}:createmilling/${idSuffix}`)
	},

	// Mortar and pestle (creativeengineering:mortar_and_pestle, i.mortar_and_pestle), shapeless in the crafting table:
	// damageIngredient takes 1 durability and hands the mortar back. inputCount copies of the input (up to 8, the
	// mortar takes the 9th slot). Id mortar/<id>. TODO cooking/kitchen #10. Food grinding goes through foodGrinding() (own
	// recipes) or the generated catalog loop in cooking/mortar.js (other mods' recipes), not straight here.
	//   R.mortar(Item.of(i.cornmeal, 1), i.t_seeds_corn, e, 'corn_kernels_cornmeal', 3)
	mortar(output, input, e, idOverride, inputCount) {
		if (!e) throw new Error('mortar called without event object!')
		inputCount = inputCount || 1
		var idSuffix = idOverride ?? global.NCH.toId(output, input, 'mortar')
		var ctx = 'mortar ' + idSuffix
		if (inputCount < 1 || inputCount > 8) throw new Error(ctx + ': inputCount ' + inputCount + ' (1-8)')
		global.NCH._validateAny(output, ctx + ' output')
		global.NCH._validateAny(input, ctx + ' input')
		var ingredients = []
		for (var n = 0; n < inputCount; n++) ingredients.push(input)
		ingredients.push(global.item.mortar_and_pestle)
		e.shapeless(output, ingredients)
			.damageIngredient(global.item.mortar_and_pestle, 1)
			.id(`${global.NCH.modId}:mortar/${idSuffix}`)
	},

	// The pack's own food grinding (flour, cornmeal...): one call writes the Create crushing and Millstone recipes
	// with the same outputs, plus a mortar and pestle mirror (mortar()). The mortar gives the main output (the first
	// one) rounded down to what the machines average: E = the sum of count x chance over every output of that
	// item; E >= 1 gives floor(E) from one input, E < 1 gives 1 from ceil(1 / E) inputs (at most 8; both with 0.02
	// slack, so 0.333 takes 3). Byproducts
	// (seeds, straw) are machine-only. TODO cooking/kitchen #10.
	// opts: { crushing: false, milling: false } leaves a machine out; { mortar: false } leaves the mortar out;
	//       { mortar: { count, inputs } } sets the hand ratio instead of deriving it.
	//   R.foodGrinding([i.cornmeal, Item.of(i.cornmeal, 1).withChance(0.5), Item.of(i.corn_seeds, 1).withChance(0.25)],
	//     i.t_crops_corn, e, 'corn_cornmeal')
	foodGrinding(output, input, e, idOverride, opts) {
		if (!e) throw new Error('foodGrinding called without event object!')
		opts = opts || {}
		var outputs = Array.isArray(output) ? output : [output]
		var idSuffix = idOverride ?? global.NCH.toId(outputs[0], input, 'foodgrinding')
		var ctx = 'foodGrinding ' + idSuffix
		if (opts.crushing !== false) global.NCH.createCrushing(outputs, input, e, idSuffix)
		if (opts.milling !== false) global.NCH.createMilling(outputs, input, e, idSuffix)
		if (opts.mortar === false) return

		var stacks = outputs.map((o, idx) => global.NCH._itemJson(typeof o === 'string' ? Item.of(o) : o,
			{ countMode: 'flat', allowTag: false, includeChance: true, context: ctx + ' output[' + idx + ']' }))
		var main = stacks[0].item
		var count, inputs
		if (opts.mortar && typeof opts.mortar === 'object') {
			count = opts.mortar.count || 1
			inputs = opts.mortar.inputs || 1
		} else {
			var expected = 0
			stacks.forEach(st => { if (st.item === main) expected += (st.count || 1) * (st.chance === undefined ? 1 : st.chance) })
			if (expected >= 0.98) {
				count = Math.floor(expected + 0.02)
				inputs = 1
			} else {
				count = 1
				inputs = Math.ceil(1 / expected - 0.02)   // 0.333 -> 3, not 4
			}
		}
		if (inputs > 8) {
			console.warn('[NCH] ' + ctx + ': mortar would need ' + inputs + ' inputs for 1 ' + main + ', skipped')
			return
		}
		global.NCH.mortar(Item.of(main, count), input, e, idSuffix, inputs)
	},

	createPressing(output, input, e, idOverride) {
		if (!e) throw new Error('createPressing called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "createpressing")
		var ctx = 'createPressing ' + idSuffix

		global.NCH._validateAny(output, ctx + ' output')
		global.NCH._validateAny(input, ctx + ' input')

		let r = e.recipes.create.pressing(output,input)
		r.id(`${global.NCH.modId}:createpressing/${idSuffix}`)
	},

	createCompacting(output, input, e, idOverride, heat) {
		if (!e) throw new Error('createCompacting called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "compacting")
		var ctx = 'createCompacting ' + idSuffix

		global.NCH._validateAny(output, ctx + ' output')
		global.NCH._validateAny(input, ctx + ' input')

		let r = e.recipes.create.compacting(output,input)

		if (heat === "heated") r.heated()
		if (heat === "superheated") r.superheated()

		r.id(`${global.NCH.modId}:createcompacting/${idSuffix}`)
	},

	createMixing(output, input, e, idOverride, heat) {
		if (!e) throw new Error('createMixing called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "mixing")
		var ctx = 'createMixing ' + idSuffix

		global.NCH._validateAny(output, ctx + ' output')
		global.NCH._validateAny(input, ctx + ' input')

		let r = e.recipes.create.mixing(output,input)

		if (heat === "heated") r.heated()
		if (heat === "superheated") r.superheated()

		r.id(`${global.NCH.modId}:createmixing/${idSuffix}`)
	},

	createSaw(output, input, e, idOverride) {
		if (!e) throw new Error('createSaw called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "createsawing")
		var ctx = 'createSaw ' + idSuffix

		global.NCH._validateAny(output, ctx + ' output')
		global.NCH._validateAny(input, ctx + ' input')

		e.recipes.create
		.cutting(output, input)
		.id(`${global.NCH.modId}:createsaw/${idSuffix}`)
	},

	createDeploying(output, input, e, idOverride, keepIngredient) {
		if (!e) throw new Error('createDeploying called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "deploying")
		var ctx = 'createDeploying ' + idSuffix

		global.NCH._validateAny(output, ctx + ' output')
		global.NCH._validateAny(input, ctx + ' input')
		if (keepIngredient) global.NCH._validateAny(keepIngredient, ctx + ' keepIngredient')

		var recipe = e.recipes.create
		.deploying(output, input)
		.id(`${global.NCH.modId}:createdeploying/${idSuffix}`)

		if (keepIngredient) recipe.keepIngredient(keepIngredient)
	},


	/*
	Cutting	Mechanical Saw	inline e.recipes.create.cutting(...) (no dedicated NCH wrapper)
	Deploying	Deployer	neoCraftHelper.js:1033 createDeploying
	Pressing	Mechanical Press	inline e.recipes.create.pressing(...)
	Filling	Spout	neoCraftHelper.js:1142 createFilling

	Rolling	Rolling Mill	neoCraftHelper.js:1160 createRolling — helper exists but no recipe currently puts it inside a sequence

	Polishing	Grinder	neoCraftHelper.js:1271 viPolishing — ✅ already used as a step (crystal.js:22)
	Laser Cutting	Laser	neoCraftHelper.js:1239 viLaserCutting — helper exists, not used as a step yet
	Vibrating	Vibrating Table	neoCraftHelper.js:1218 viVibrating — helper exists, not used as a step yet
	Turning	Lathe	no NCH helper
	Vacuumizing	Vacuum Chamber (evacuate)	no NCH helper
	Pressurizing	Vacuum Chamber (pressurize)	no NCH helper
	Curving	Curving Press	no NCH helper
	Hammering	Helve Hammer	no NCH helper
	Coiling	Coiling machine	no NCH helper
	Centrifugation	Centrifuge	no NCH helper

	To Add:
	Fan processes
	Charging
	*/

	// NOTE: idOverride sits in the "first optional arg" slot for positional consistency,
	// but this function still throws if it's missing - it is not truly optional here.
	createSequencedAssembly(output, input, sequence, transitionalItem, e, idOverride, loops) {
		if (!e) throw new Error('createSequencedAssembly called without event object!')
		if (!idOverride) throw new Error('createSequencedAssembly requires an explicit idOverride - auto-generated ids are unreliable for chance-output arrays!')

		var ctx = 'createSequencedAssembly ' + idOverride

		// transitionalItem resolution:
		//   ['key']      -> look up the incomplete item registered as 'key' in transition_items.js
		//                   (decoupled from idOverride, so name the recipe whatever you want)
		//   falsy        -> fall back to INCOMPLETE[idOverride], then the shared i.incomplete
		//   anything else -> used verbatim (item id / Item.of(...) / raw ingredient object)
		if (Array.isArray(transitionalItem)) {
			var incKey = transitionalItem[0]
			transitionalItem = global.NCH.INCOMPLETE[incKey]
			if (!transitionalItem)
				throw new Error(`createSequencedAssembly ${idOverride}: no incomplete item registered for '${incKey}' - add a row to SEQUENCED_ASSEMBLIES in transition_items.js`)
		}
		transitionalItem = transitionalItem || global.NCH.INCOMPLETE[idOverride] || i.incomplete

		global.NCH._validateAny(output, ctx + ' output')
		global.NCH._validateAny(input, ctx + ' input')
		global.NCH._validateAny(transitionalItem, ctx + ' transitionalItem')

		if (transitionalItem === i.incomplete)
			console.warn(`WARN: ${idOverride} has no incomplete item - pass ['<key>'] and register it in transition_items.js`)

		let r = e.recipes.create.sequenced_assembly(output, input, sequence)
		  .transitionalItem(transitionalItem)

		if (loops) r.loops(loops)

		r.id(`${global.NCH.modId}:createsequencedassembly/${idOverride}`)
	},

	createIncompleteItem(event, name, textureLocation, colorHex) {
		if (!event) throw new Error('createIncompleteItem called without a registry event object!')
		if (typeof textureLocation !== 'string' || !textureLocation)
			throw new Error('createIncompleteItem ' + name + ' needs a texture resource path string')

		return event.create(name, 'create:sequenced_assembly')
			.texture(textureLocation)
			.color(0, colorHex || '#808080')
	},

	// incomplete-item key -> registered item id ('kubejs:incomplete_<key>')
	INCOMPLETE: {},

	// Register the incomplete/transition item for one sequenced assembly under an arbitrary
	// key (unrelated to any recipe id). Call from StartupEvents.registry('item').
	// texture: omit to auto-derive a grey-tinted copy of the output item's sprite - it looks
	//   up global.item[key] (or falls back to minecraft:<key>) and uses '<ns>:item/<path>'.
	//   Pass an explicit string when that guess is wrong (block items, mods using items/, ...).
	registerIncomplete(event, key, texture, colorHex) {
		if (!event) throw new Error('registerIncomplete called without a registry event object!')
		if (!texture) {
			var outId = String((global.item && global.item[key]) || ('minecraft:' + key)).replace(/^#/, '')
			var parts = outId.split(':')
			var ns = parts.length > 1 ? parts[0] : 'minecraft'
			var path = parts.length > 1 ? parts[1] : parts[0]
			texture = ns + ':item/' + path
		}
		var itemName = 'incomplete_' + key
		global.NCH.createIncompleteItem(event, itemName, texture, colorHex)
		var id = 'kubejs:' + itemName
		global.NCH.INCOMPLETE[key] = id
		return id
	},

	// Look up a registered incomplete item id by key - use it when building the sequence
	// steps, e.g. const inc = R.incomplete('crab_bars').
	incomplete(key) {
		var id = global.NCH.INCOMPLETE[key]
		if (!id) throw new Error(`incomplete('${key}') not registered - add a row to SEQUENCED_ASSEMBLIES in transition_items.js`)
		return id
	},

	createMechanicalCrafting(output, pattern, keys, e, idOverride) {
		if (!e) throw new Error('createMechanicalCrafting called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, Object.values(keys)[0], "mechanicalcrafting")
		var ctx = 'createMechanicalCrafting ' + idSuffix

		global.NCH._validateAny(output, ctx + ' output')
		Object.keys(keys).forEach((k) => {
			global.NCH._validateAny(keys[k], ctx + ' key[' + k + ']')
		})

		e.recipes.create
		.mechanical_crafting(output, pattern, keys)
		.id(`${global.NCH.modId}:createmechanicalcrafting/${idSuffix}`)
	},

 createFilling(output, input, e, idOverride) {
    if (!e) throw new Error('createFilling called without event object!')
    var inputArr = Array.isArray(input) ? input : [input]
    const itemInput = inputArr.find((i) => typeof i === 'string')
    const id = itemInput
        ? global.NCH.toId(output, itemInput, "createfilling")
        : global.NCH.toId(output, input, "createfilling")
    var idSuffix = idOverride ?? id
    var ctx = 'createFilling ' + idSuffix

    global.NCH._validateAny(output, ctx + ' output')
    global.NCH._validateAny(input, ctx + ' input')

    e.recipes.create
    .filling(output, input)
    .id(`${global.NCH.modId}:createfilling/${idSuffix}`)
	},

	// Create Item Drain: input -> outputs, an item and/or a fluid.
	//   R.createEmptying([i.tankard, Fluid.of(f.beer, 250)], i.beer, e, 'beer')
	createEmptying(outputs, input, e, idOverride) {
		if (!e) throw new Error('createEmptying called without event object!')
		var outArr = Array.isArray(outputs) ? outputs : [outputs]
		var idSuffix = idOverride ?? global.NCH.toId(outArr[0], input, "createemptying")
		var ctx = 'createEmptying ' + idSuffix

		global.NCH._validateAny(outArr, ctx + ' output')
		global.NCH._validateAny(input, ctx + ' input')

		e.recipes.create
		.emptying(outArr, input)
		.id(`${global.NCH.modId}:createemptying/${idSuffix}`)
	},

	createRolling(output, input, e, idOverride) {
		if (!e) throw new Error('createRolling called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "rolling")
		var ctx = 'createRolling ' + idSuffix

		const json = {
		  type: 'createaddition:rolling',
		  input: global.NCH._itemJson(input, { countMode: 'none', context: ctx + ' input' }),
		  result: global.NCH._itemJson(output, { countMode: 'flat', allowTag: false, context: ctx + ' output' })
		}
		e.custom(json).id(`${global.NCH.modId}:createrolling/${idSuffix}`)
	},

	createCharging(output, input, e, idOverride, energy, maxChargeRate) {
		if (!e) throw new Error('createCharging called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "charging")
		var ctx = 'createCharging ' + idSuffix

		const json = {
			type: 'createaddition:charging',
			input: global.NCH._itemJson(input, { countMode: 'flat', context: ctx + ' input' }),
			result: global.NCH._itemJson(output, { countMode: 'flat', allowTag: false, context: ctx + ' output' }),
			energy: energy ?? 4000,
			maxChargeRate: maxChargeRate ?? 200
		}

		e.custom(json).id(`${global.NCH.modId}:createcharging/${idSuffix}`)
	},

	createExtruding(output, ingredients, e, idOverride, catalyst, bonks) {
		if (!e) throw new Error('createExtruding called without event object!')
		if (!output || !ingredients) throw new Error('createExtruding called without output or ingredients!')

		var list = Array.isArray(ingredients) ? ingredients : [ingredients]
		var nameSource = catalyst ?? list.find((i) => typeof i === 'string' || i.item) ?? list
		var idSuffix = idOverride ?? global.NCH.toId(output, nameSource, "extruding")
		var ctx = 'createExtruding ' + idSuffix

		if (bonks !== undefined && (!Number.isInteger(bonks) || bonks < 1)) {
			global.NCH._warn(ctx + ' bonks', 'expected a positive integer, got ' + bonks)
		}

		const json = {
			type: 'create_mechanical_extruder:extruding',
			ingredients: list.map((i, idx) => {
				return (i && i.fluid !== undefined)
					? global.NCH._fluidJson(i, { context: ctx + ' ingredient[' + idx + ']' })
					: global.NCH._itemJson(i, { countMode: 'flat', context: ctx + ' ingredient[' + idx + ']' })
			}),
			result: global.NCH._itemJson(output, { countMode: 'flat', allowTag: false, context: ctx + ' output' }),
			requiredBonks: bonks ?? 1
		}

		if (catalyst) json.catalyst = global.NCH._itemJson(catalyst, { countMode: 'flat', context: ctx + ' catalyst' })

		e.custom(json).id(`${global.NCH.modId}:extruding/${idSuffix}`)
	},

	viVibrating(outputs, inputs, e, idOverride, processingTime) {
		if (!e) throw new Error('viVibrating called without event object!')
		if (!outputs || !inputs) throw new Error('viVibrating called without inputs or outputs!')

		var idSuffix = idOverride ?? global.NCH.toId(outputs, inputs, "vibrating")
		var ctx = 'viVibrating ' + idSuffix

		const json = {
		  type: 'vintageimprovements:vibrating',
		  ingredients: (Array.isArray(inputs) ? inputs : [inputs]).map((i, idx) => {
		      return global.NCH._itemJson(i, { countMode: 'none', context: ctx + ' ingredient[' + idx + ']' })
		  }),
		  results: (Array.isArray(outputs) ? outputs : [outputs]).map((o, idx) => {
		      return global.NCH._itemJson(o, { countMode: 'flat', allowTag: false, includeChance: true, context: ctx + ' result[' + idx + ']' })
		  }),
		  processingTime: processingTime ?? 300
		}

		e.custom(json).id(`${global.NCH.modId}:vivibrating/${idSuffix}`)
	},
	
	viLaserCutting(outputs, inputs, e, idOverride, energy, maxChargeRate) {
		if (!e) throw new Error('laserCutting called without event object!')
		if (!outputs || !inputs) throw new Error('laserCutting called without outputs or inputs!')

		var idSuffix = idOverride ?? global.NCH.toId(outputs, inputs, "lasercutting")
		var ctx = 'laserCutting ' + idSuffix

		const json = {
			type: 'vintageimprovements:laser_cutting',
			ingredients: (Array.isArray(inputs) ? inputs : [inputs]).map((i, idx) => {
				return global.NCH._itemJson(i, { countMode: 'none', context: ctx + ' ingredient[' + idx + ']' })
			}),
			results: (Array.isArray(outputs) ? outputs : [outputs]).map((o, idx) => {
				return global.NCH._itemJson(o, { countMode: 'flat', allowTag: false, includeChance: true, context: ctx + ' result[' + idx + ']' })
			}),
			energy: energy ?? 2000,
			maxChargeRate: maxChargeRate ?? 50
		}

		e.custom(json).id(`${global.NCH.modId}:lasercutting/${idSuffix}`)
	},

	// Vintage Improvements grinder polishing ('vintageimprovements:polishing' - the Grinder's
	// sandpaper-free polish step, e.g. rose quartz -> polished rose quartz). There's no
	// e.recipes.vintageimprovements.* namespaced accessor for this mod's recipe types (same
	// reason viVibrating/viLaserCutting above go through e.custom rather than a builder call),
	// so this goes straight to raw json like its siblings.
	//
	// Unlike viVibrating/viLaserCutting, this returns the bare recipe object instead of
	// calling .id() itself - its intended use is as one entry in a createSequencedAssembly
	// sequence array (mirrors e.recipes.create.sawing(...) etc. used bare in that array).
	// Call .id(...) yourself if you ever need it as a standalone top-level recipe.
	viPolishing(output, input, e, speedLimits, processingTime) {
		if (!e) throw new Error('viPolishing called without event object!')
		if (!output || !input) throw new Error('viPolishing called without output or input!')

		var ctx = 'viPolishing'

		const json = {
			type: 'vintageimprovements:polishing',
			ingredients: (Array.isArray(input) ? input : [input]).map((i, idx) => {
				return global.NCH._itemJson(i, { countMode: 'none', context: ctx + ' ingredient[' + idx + ']' })
			}),
			results: (Array.isArray(output) ? output : [output]).map((o, idx) => {
				return global.NCH._itemJson(o, { countMode: 'flat', allowTag: false, includeChance: true, context: ctx + ' result[' + idx + ']' })
			}),
			speed_limits: speedLimits ?? 1
		}

		if (processingTime) json.processingTime = processingTime

		return e.custom(json)
	},

	// ========================================================================
	// JEED (Just Enough Effect Descriptions)
	// ========================================================================

	// jeed:effect_provider: JEED's page for an effect lists these items as giving it (effects.md 3).
	// providers: an item / '#tag' / NBT item ({ item, nbt } or Item.of(id, '{...}')), or an array of them.
	// JEED drops the whole recipe on one unknown id, so missing items are left out with a warning, and an
	// effect that isn't registered skips the recipe.
	//   R.jeedProvider('minecraft:health_boost', [i.stal_wine, i.solaris_wine], e, 'vinery_health_boost')
	jeedProvider(effect, providers, e, idOverride) {
		if (!e) throw new Error('jeedProvider called without event object!')
		var idSuffix = idOverride ?? String(effect).replace(':', '_')
		var ctx = 'jeedProvider ' + idSuffix
		if (!global.NCH._effectExists(effect)) {
			global.NCH._warn(ctx, 'effect "' + effect + '" is not registered')
			return
		}
		var json = global.NCH._jeedProviderJson(providers, ctx)
		if (json.length < 1) {
			global.NCH._warn(ctx, 'no providers')
			return
		}
		e.custom({
			type: 'jeed:effect_provider',
			effect: String(effect),
			providers: json
		}).id(`${global.NCH.modId}:jeed/${idSuffix}`)
	},

	// jeed:potion_provider: items that carry a potion in NBT (`Potion`, like minecraft:potion). JEED lists
	// each one, filled with every potion that has the effect, on that effect's page.
	//   R.jeedPotionProvider([i.ce_potion_bucket, i.potion_bundle], e, 'potion_carriers')
	jeedPotionProvider(providers, e, idOverride) {
		if (!e) throw new Error('jeedPotionProvider called without event object!')
		var idSuffix = idOverride ?? 'potion_provider'
		var ctx = 'jeedPotionProvider ' + idSuffix
		var json = global.NCH._jeedProviderJson(providers, ctx)
		if (json.length < 1) {
			global.NCH._warn(ctx, 'no providers')
			return
		}
		e.custom({
			type: 'jeed:potion_provider',
			providers: json
		}).id(`${global.NCH.modId}:jeed/${idSuffix}`)
	},

	_jeedProviderJson(providers, ctx) {
		var list = Array.isArray(providers) ? providers : [providers]
		var out = []
		list.forEach((v, idx) => {
			var id = (v && typeof v.item === 'string') ? v.item : global.NCH._resolveIdCount(v).id
			if (!global.NCH._checkItem(id, ctx + ' provider[' + idx + ']')) return
			out.push(global.NCH._nbtItemJson(v, { countMode: 'none', context: ctx + ' provider[' + idx + ']' }))
		})
		return out
	},

	_effectExists(id) {
		try {
			return $NCH_ForgeRegistries.MOB_EFFECTS.containsKey($NCH_ResourceLocation.of(String(id), ':'))
		} catch (err) {
			return false
		}
	},

	// ========================================================================
	// Immersive Engineering
	// ========================================================================

	ieMetalPress(output, input, mold, e, idOverride) {
  if (!e) throw new Error('ieMetalPress called without event object!')
  var idSuffix = idOverride ?? global.NCH.toId(output, input, "iepressing")
  var ctx = 'ieMetalPress ' + idSuffix

  global.NCH._validateAny(output, ctx + ' output')
  global.NCH._validateAny(input, ctx + ' input')
  global.NCH._validateAny(mold, ctx + ' mold')

  e.recipes.immersiveengineeringMetalPress(output, input, mold)
    .id(`${global.NCH.modId}:iemetalpress/${idSuffix}`)
	},

	ieCrushing(output, input, e, idOverride, secondaries, energy) {
		if (!e) throw new Error('ieCrushing called without event object!')
		secondaries = secondaries ?? []
		energy = energy ?? 6000
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "iecrushing")
		var ctx = 'ieCrushing ' + idSuffix

		global.NCH._validateAny(input, ctx + ' input')
		global.NCH._validateAny(output, ctx + ' output')

		e.custom({
		  type: 'immersiveengineering:crusher',
		  input: Ingredient.of(input),
		  result: Item.of(output),
		  energy: energy,
		  secondaries: secondaries.map((s, idx) => {
		    global.NCH._validateAny(s.output, ctx + ' secondary[' + idx + ']')
		    return { chance: s.chance, output: Item.of(s.output).toJson() }
		  })
		}).id(`${global.NCH.modId}:iecrushing/${idSuffix}`)
	},

	ieAlloy(output, input0, input1, e, idOverride, time) {
		if (!e) throw new Error('ieAlloy called without event object!')
		if (!output || !input0 || !input1) throw new Error('ieAlloy called without inputs or output!')

		var idSuffix = idOverride ?? global.NCH.toId(output, input0, "alloy")
		var ctx = 'ieAlloy ' + idSuffix

		const json = {
			type: 'immersiveengineering:alloy',
			input0: global.NCH._itemJson(input0, { countMode: 'base_ingredient', context: ctx + ' input0' }),
			input1: global.NCH._itemJson(input1, { countMode: 'base_ingredient', context: ctx + ' input1' }),
			result: global.NCH._itemJson(output, { countMode: 'base_ingredient', context: ctx + ' output' }),
			time: time ?? 200
		}

		e.custom(json).id(`${global.NCH.modId}:iealloy/${idSuffix}`)
	},

	ieArcFurnace(output, input, e, idOverride, secondaries, additives, time, energy) {
		if (!e) throw new Error('ieArcFurnace called without event object!')
		additives = additives ?? []
		secondaries = secondaries ?? []
		var outputs = Array.isArray(output) ? output : [output]
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "arcsmelting")
		var ctx = 'ieArcFurnace ' + idSuffix

		global.NCH._validateAny(input, ctx + ' input')
		outputs.forEach((o, idx) => {
			global.NCH._validateAny(o, ctx + ' output[' + idx + ']')
		})
		additives.forEach((a, idx) => {
			global.NCH._validateAny(a, ctx + ' additive[' + idx + ']')
		})

		e.custom({
		  type: 'immersiveengineering:arc_furnace',
		  input: Ingredient.of(input),
		  results: outputs.map((o) => Item.of(o)),
		  additives: additives.map((a) => Ingredient.of(a)),
		  secondaries: secondaries.map((s, idx) => {
		    global.NCH._validateAny(s.output, ctx + ' secondary[' + idx + ']')
		    return { chance: s.chance, output: Item.of(s.output) }
		  }),
		  time: time ?? 100,
		  energy: energy ?? 25600
		}).id(`${global.NCH.modId}:iearcfurnace/${idSuffix}`)
	},

	ieSqueezing(fluidOutput, input, e, idOverride, itemOutput, energy) {
		if (!e) throw new Error('ieSqueezing called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(fluidOutput || itemOutput, input, "squeezing")
		var ctx = 'ieSqueezing ' + idSuffix

		const json = {
		  type: 'immersiveengineering:squeezer',
		  input: global.NCH._itemJson(input, { countMode: 'none', context: ctx + ' input' }),
		  fluid: global.NCH._fluidJson(fluidOutput, { defaultAmount: 1000, context: ctx + ' fluid output' }),
		  energy: energy ?? 6000
		}

		if (itemOutput) {
			json.output = global.NCH._itemJson(itemOutput, { countMode: 'flat', allowTag: false, context: ctx + ' item output' })
		}

		e.custom(json).id(`${global.NCH.modId}:iesqueezing/${idSuffix}`)
	},

	// IE Mixer: fluid + up to 4 item inputs -> fluid. The input fluid must be a tag ('#forge:fluid/...'):
	// IE's FluidTagInput reads only "tag". A tag string carries no amount, so the input takes the output's amount
	// unless inputAmount is given. Counts go in as IE's base_ingredient form.
	ieMixer(fluidOutput, inputs, fluidInput, e, idOverride, energy, inputAmount) {
		if (!e) throw new Error('ieMixer called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(fluidOutput, inputs, "iemixer")
		var ctx = 'ieMixer ' + idSuffix

		var result = global.NCH._fluidJson(fluidOutput, { defaultAmount: 1000, allowTag: false, context: ctx + ' result' })
		var fluid = global.NCH._fluidJson(fluidInput, { defaultAmount: inputAmount ?? result.amount, context: ctx + ' fluid input' })
		if (!fluid.tag) global.NCH._warn(ctx, 'fluid input is not a tag, IE will not read it: ' + fluid.fluid)

		const json = {
		  type: 'immersiveengineering:mixer',
		  fluid: fluid,
		  inputs: (Array.isArray(inputs) ? inputs : [inputs]).map((i, idx) => {
		      return global.NCH._itemJson(i, { countMode: 'base_ingredient', context: ctx + ' input[' + idx + ']' })
		  }),
		  result: result,
		  energy: energy ?? 3200
		}

		e.custom(json).id(`${global.NCH.modId}:iemixer/${idSuffix}`)
	},

	ieBottling(results, fluid, inputs, e, idOverride, amount) {
		if (!e) throw new Error('ieBottling called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(results, inputs, "iebottling")
		var ctx = 'ieBottling ' + idSuffix

		const json = {
		  type: 'immersiveengineering:bottling_machine',
		  fluid: global.NCH._fluidJson(fluid, { defaultAmount: amount, context: ctx + ' fluid' }),
		  inputs: (Array.isArray(inputs) ? inputs : [inputs]).map((i, idx) => {
		      return global.NCH._itemJson(i, { countMode: 'flat', context: ctx + ' input[' + idx + ']' })
		  }),
		  results: (Array.isArray(results) ? results : [results]).map((r, idx) => {
		      return global.NCH._itemJson(r, { countMode: 'flat', allowTag: false, context: ctx + ' result[' + idx + ']' })
		  })
		}

		e.custom(json).id(`${global.NCH.modId}:iebottling/${idSuffix}`)
	},

	ieBlueprint(outputs, inputs, category, e, idOverride) {
	  if (!e) throw new Error('ieBlueprint called without event object!')
	  if (!outputs || !inputs) throw new Error('ieBlueprint called without inputs or outputs!')
	  if (!category) throw new Error('ieBlueprint called without a category!')
	  if (!Array.isArray(inputs)) throw new Error('ieBlueprint requires inputs to be an array!')

	  var idSuffix = idOverride ?? global.NCH.toId(outputs, inputs, category)
	  var ctx = 'ieBlueprint/' + category + ' ' + idSuffix

	  const json = {
	      type: 'immersiveengineering:blueprint',
	      category: category,
	      // NBT-aware: an input/result may be a KubeJS Item.of('id','{nbt}'), a
	      // { item, nbt } shorthand, or a raw { type:'forge:partial_nbt'|'forge:nbt', ... }
	      // ingredient. Plain items/tags are unchanged. IE's IngredientWithSize honours
	      // forge:partial_nbt/forge:nbt natively and the workbench matches NBT-aware.
	      inputs: inputs.map((i, idx) => {
	          return global.NCH._nbtItemJson(i, { countMode: 'base_ingredient', role: 'input', context: ctx + ' input[' + idx + ']' })
	      }),
	      result: global.NCH._nbtItemJson(outputs, { countMode: 'flat', allowTag: false, role: 'result', context: ctx + ' output' })
	  }

	  e.custom(json).id(`${global.NCH.modId}:blueprint/${category}/${idSuffix}`)
	},

	// Immersive Engineering Sawmill (immersiveengineering:sawmill).
	//   output : item / tag / string / {item,count} - the main result
	//   input  : item / tag / string
	//   opts   : { energy }  (default 800; IE uses 1600 for log -> planks)
	//
	// LIMITATION: this only does the main result. Secondary outputs (sawdust / bark) and
	// the "stripped" result CANNOT be added here - kubejs-immersive-engineering's
	// SawmillRecipeSchema serialises the `secondaries` array in a shape IE's own serializer
	// rejects ("Missing stripping, expected to find a Boolean"). For those, write a datapack
	// json (see development/patcherMod/src/main/resources/data/creativeengineering/recipes/sawmill/*.json
	// or kubejs/data/creativeengineering/recipes/sawmill/<id>.json), which loads through IE
	// directly and bypasses the broken schema.
	ieSawmill(output, input, e, idOverride, opts) {
		if (!e) throw new Error('ieSawmill called without event object!')
		opts = opts || {}
		if (opts.sawdust || opts.secondaries || opts.stripped)
			throw new Error('ieSawmill: secondaries / stripped / sawdust are not supported via KubeJS '
				+ '(kjs-ie schema bug). Put this recipe in a datapack json instead: '
				+ (idOverride ? 'kubejs/data/creativeengineering/recipes/sawmill/' + idOverride + '.json' : 'a sawmill json'))

		var idSuffix = idOverride ?? global.NCH.toId(output, input, 'sawmill')
		var ctx = 'ieSawmill ' + idSuffix

		global.NCH._validateAny(input, ctx + ' input')
		global.NCH._validateAny(output, ctx + ' output')

		e.custom({
			type: 'immersiveengineering:sawmill',
			energy: opts.energy ?? 800,
			input: global.NCH._itemJson(input, { countMode: 'none', context: ctx + ' input' }),
			result: global.NCH._itemJson(output, { countMode: 'flat', context: ctx + ' output' }),
			secondaries: [] // required by IE's serializer even when empty
		}).id(`${global.NCH.modId}:sawmill/${idSuffix}`)
	},

	// ========================================================================
	// PneumaticCraft
	// ========================================================================

	pcPressureChamber(outputs, inputs, pressure, e, idOverride) {
		  if (!e) throw new Error('pcPressureChamber called without event object!')
		  if (!outputs || !inputs) throw new Error('pcPressureChamber called without inputs or outputs!')

		  var idSuffix = idOverride ?? global.NCH.toId(outputs, inputs, "pressure")
		  var ctx = 'pcPressureChamber ' + idSuffix

		  const json = {
		      type: 'pneumaticcraft:pressure_chamber',
		      inputs: (Array.isArray(inputs) ? inputs : [inputs]).map((i, idx) => {
		          return global.NCH._itemJson(i, { countMode: 'stacked_item', context: ctx + ' input[' + idx + ']' })
		      }),
		      pressure: pressure,
		      results: (Array.isArray(outputs) ? outputs : [outputs]).map((o, idx) => {
		          return global.NCH._itemJson(o, { countMode: 'flat', allowTag: false, context: ctx + ' result[' + idx + ']' })
		      })
		  }

		  e.custom(json).id(`${global.NCH.modId}:pcpressurechamber/${idSuffix}`)
	},

	pcExplosionCrafting(output, input, lossRate, e, idOverride) {
		  if (!e) throw new Error('pcExplosionCrafting called without event object!')
		  var idSuffix = idOverride ?? global.NCH.toId(output, input, "explosion")
		  var ctx = 'pcExplosionCrafting ' + idSuffix

		  const json = {
		      type: 'pneumaticcraft:explosion_crafting',
		      input: global.NCH._itemJson(input, { countMode: 'none', context: ctx + ' input' }),
		      results: [global.NCH._itemJson(output, { countMode: 'flat', allowTag: false, context: ctx + ' output' })],
		      loss_rate: lossRate
		  }

		  e.custom(json).id(`${global.NCH.modId}:pcexplosioncrafting/${idSuffix}`)
	},

	// NOTE: itemOutput/fluidOutput/itemInput/fluidInput are each individually guarded
	// (function tolerates any of them being missing), which technically makes them
	// "optional" by the letter of the rule - but they're kept ahead of e/idOverride
	// since they're the actual identity of the recipe, matching every other function's shape.
	pcThermoPlant(itemOutput, fluidOutput, itemInput, fluidInput, e, idOverride, options) {
		if (!e) throw new Error('pcThermoPlant called without event object!')
		options = options || {}
		var idSuffix = idOverride ?? global.NCH.toId(itemOutput ?? fluidOutput, itemInput ?? fluidInput, "thermoplant")
		var ctx = 'pcThermoPlant ' + idSuffix

		const toIngredient = (i) => {
			if (!i) return undefined
			return global.NCH._itemJson(i, { countMode: 'none', context: ctx + ' item input' })
		}
		const toItemResult = (o) => {
			if (!o) return undefined
			return global.NCH._itemJson(o, { countMode: 'flat', allowTag: false, context: ctx + ' item output' })
		}
		const toFluidIngredient = (f) => {
			if (!f) return undefined
			var fj = global.NCH._fluidJson(f, { context: ctx + ' fluid input' })
			fj.type = 'pneumaticcraft:fluid'
			return fj
		}
		const toFluidResult = (f) => {
			if (!f) return undefined
			return global.NCH._fluidJson(f, { allowTag: false, context: ctx + ' fluid output' })
		}

		const json = {
			type: 'pneumaticcraft:thermo_plant',
			exothermic: options.exothermic ?? false
		}
		const itemIn = toIngredient(itemInput)
		if (itemIn) json.item_input = itemIn
		const fluidIn = toFluidIngredient(fluidInput)
		if (fluidIn) json.fluid_input = fluidIn
		const itemOut = toItemResult(itemOutput)
		if (itemOut) json.item_output = itemOut
		const fluidOut = toFluidResult(fluidOutput)
		if (fluidOut) json.fluid_output = fluidOut
		if (options.pressure !== undefined) json.pressure = options.pressure
		if (options.speed !== undefined) json.speed = options.speed
		if (options.airUseMultiplier !== undefined) json.air_use_multiplier = options.airUseMultiplier
		if (options.minTemp !== undefined || options.maxTemp !== undefined) {
			json.temperature = {}
			if (options.minTemp !== undefined) json.temperature.min_temp = options.minTemp
			if (options.maxTemp !== undefined) json.temperature.max_temp = options.maxTemp
		}

		e.custom(json).id(`${global.NCH.modId}:pcthermoplant/${idSuffix}`)
	},

	// ========================================================================
	// Creative Labels (development/creativeLabels, feature_writeup/sticky_notes.md 3.6)
	// ========================================================================
	// The note recipes are Java CustomRecipes matched in code (any grid size), so their JSON is only the type and
	// the mod ships none: the pack owns the ids (creativeengineering:creative_labels/<type>). type is one of
	//   note_dye      N identical notes + 1 dye -> N notes with the dye mixed in, text kept
	//   note_special  N plain notes + 1 ink sac / glow berries / gunpowder / gold nugget -> N special notes
	//   note_copy     1 written note + 8 blank notes -> 9 copies of the written one
	//   note_erase    note + eraser -> text wiped, or a blank note back to plain (the eraser stays)
	// No item inputs to validate: the ingredients live in the Java matcher.
	//   R.creativeLabelsSpecial('note_dye', e)
	creativeLabelsSpecial(type, e, idOverride) {
		if (!e) throw new Error('creativeLabelsSpecial called without event object!')
		var types = ['note_dye', 'note_special', 'note_copy', 'note_erase']
		if (types.indexOf(type) < 0)
			throw new Error('creativeLabelsSpecial: unknown type "' + type + '" (one of ' + types.join(', ') + ')')
		var idSuffix = idOverride ?? type
		return e.custom({ type: 'creative_labels:' + type }).id(`${global.NCH.modId}:creative_labels/${idSuffix}`)
	},
}

console.log('Neo Craft Helper finished')

// Safety net: catch any error thrown inside an NCH call so one bad recipe
// can't abort the rest of this script or, worse, every listener registered
// after it in the same ServerEvents.recipes pass.
Object.keys(global.NCH).forEach((key) => {
  if (key.startsWith('_')) return
  if (typeof global.NCH[key] !== 'function') return
  const original = global.NCH[key]
  global.NCH[key] = function () {
	var args = arguments
	try {
		return original.apply(global.NCH, args)
	} catch (err) {
		console.error('[NCH] ' + key + ' failed and was skipped: ' + (err && err.message ? err.message : err))
		return undefined
	}
	}
})
