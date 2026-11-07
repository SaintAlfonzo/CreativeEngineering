// Creative Common (patcherMod until 2026-11-04): src/main/resources/kubejs_startup/creativecommon/func/neoCraftHelper.js
// (was kubejs/startup_scripts/func/neoCraftHelper.js; moved 2026-10-25, creative_delight.md 10.5 item 3).
// Shipped in patcherMod's jar and loaded by its startup loader (compat/kubejs/JarStartupScripts) BEFORE the
// pack's startup_scripts folder, so the dicts (global.item, global.fluid) do not exist yet when this file runs:
// read them inside functions only (at call time), never at top level. An edit needs a patcherMod rebuild.

console.log('Neo Craft Helper starting')

var $NCH_ForgeRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
var $NCH_ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')
// Config classes NCH.cfg() reads, in lookup order (creative_split.md 7.3 step 0): Common's CommonConfig first, then each
// carved jar's class, added by that jar's startup script with global.NCH.registerConfig(modId, className). Classes are
// loaded lazily on the first cfg() call that needs them; cls undefined = not tried yet, false = unavailable.
var $NCH_configClasses = [{ id: 'creativecommon', name: 'com.SaintAlfonzo.creativecommon.CommonConfig', cls: undefined }]

global.NCH = {

  // The pack's own namespace: recipe TYPES the pack registers (seasoning, dragon_breathing, ...) always use it.
  modId: 'creativeengineering',
  // Namespace of the recipe IDS the helpers write; withNamespace() changes it for one block of calls.
  _ns: 'creativeengineering',
  AXE: '#forge:tools/axes',
  strict: true, // true = throw on invalid item/fluid ids, false = warn and continue

  // ========================================================================
  // Recipe id namespace (Q24): scoped, so no helper signature changes
  // ========================================================================

	// Namespace of the next recipe id ('creativeengineering' unless inside withNamespace).
	ns() {
		return global.NCH._ns
	},

	// Runs fn with every recipe id the helpers write under ns, then restores the previous namespace
	// (nesting works). Another mod's scripts use their own namespace this way:
	//   global.NCH.withNamespace('creativedelight', () => { R.shapeless(..., e, 'oceans/thing') })
	//   -> creativedelight:shapeless/oceans/thing
	withNamespace(ns, fn) {
		if (!/^[a-z0-9_.-]+$/.test(String(ns))) throw new Error('withNamespace: invalid namespace ' + ns)
		var previous = global.NCH._ns
		global.NCH._ns = String(ns)
		try {
			return fn()
		} finally {
			global.NCH._ns = previous
		}
	},

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
    // kubejs/data/creativedelight/kubejs/creativeengineering/cultivated_meat.js history.
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

  // v unless it is null or undefined, else d. Use this, never 'v ?? <number>': this Rhino build (1902.2.3-284,
  // interpreter) returns 0 for 'nullish ?? <numeric literal>' (undefined ?? 250 -> 0; a variable or string on the
  // right works). createBottle's 'opts.mb ?? 250' wrote 0 mB, an empty fluid (TODO food/drinks #12).
  _or(v, d) {
    return v != null ? v : d
  },

  // Resolves any accepted fluid-like shape (string, Fluid.of() stack, {fluid}/{tag} object) to {id, amount}
	_resolveFluidAmount(v, defaultAmount) {
		var amt = global.NCH._or(defaultAmount, 1000)

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
    if (id === 'minecraft:air') global.NCH._absent(id, context)
    var valid = global.NCH._isTag(id)
      ? (Ingredient.of(id).itemIds.length > 0)
      : Item.exists(id)
    if (!valid) global.NCH._absent(id, context)
    if (!valid) global.NCH._warn(context, 'item/tag "' + id + '" does not exist or is empty')
    return valid
  },

  // plan_cb_publish.md M2 rule G5: a missing id whose mod is not loaded skips the whole helper call quietly. The
  // safety net at the end of this file counts it per mod instead of logging an error; Creative Common's server
  // script _lib/absent_mods.js prints one line per mod after the recipe event. A missing id of a loaded mod still
  // warns or throws as before, so pack bugs stay visible.
  _absentCounts: {},
  // Item.of / Fluid.of of a missing id give air / the empty fluid before NCH sees the id, so those count as a row for an
  // absent mod too, under their own label. Nothing in the pack passes air or the empty fluid on purpose (checked
  // 2026-11-04); in the full pack the label showing up in the [absent mods] summary means a real missing id.
  _missingStackLabel: 'unknown mod (Item.of or Fluid.of of a missing id gave air or the empty fluid)',
  _absent(id, context) {
    if (id === 'minecraft:air' || id === 'minecraft:empty') {
      var e = new Error('[NCH] ' + context + ': ' + id + ' (Item.of / Fluid.of of a missing id)')
      e.nchAbsent = global.NCH._missingStackLabel
      throw e
    }
    var ns = global.has.namespace(id)
    if (ns === null || global.has.mod(ns)) return
    var err = new Error('[NCH] ' + context + ': ' + id + ' belongs to ' + ns + ', which is not loaded')
    err.nchAbsent = ns
    throw err
  },

  // G5 for a helper whose recipe type belongs to a mod: the first line of the helper returns this when the mod is
  // not loaded, so the call is counted for that mod (same summary as _absent) instead of writing an unknown type.
  _skip() {
    for (var k = 0; k < arguments.length; k++) {
      var ns = arguments[k]
      if (global.has.mod(ns)) continue
      var counts = global.NCH._absentCounts
      counts[ns] = (counts[ns] || 0) + 1
      break
    }
    return undefined
  },

  _fluidExists(id) {
    try {
		return $NCH_ForgeRegistries.FLUIDS.containsKey($NCH_ResourceLocation.of(id, ':'))
	} catch (err) {
		return false
    }
  },

  _warnedFluidTags: {},

  _checkFluid(id, context) {
    if (!id) {
		global.NCH._warn(context, 'resolved to an undefined/empty fluid id')
		return false
    }
    if (id === 'minecraft:empty') global.NCH._absent(id, context)
    if (global.NCH._isTag(id)) {
		// once per tag: the potion bottling loop alone passes #forge:fluid/potion hundreds of times
		if (!global.NCH._warnedFluidTags[id]) {
			global.NCH._warnedFluidTags[id] = true
			console.warn('[NCH] ' + context + ': fluid tag "' + id + '" is not validated (tag support not yet implemented)')
		}
		return true
	}
    if (!global.NCH._fluidExists(id)) {
		global.NCH._absent(id, context)
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

  // A KubeJS FluidStackJS or a { fluid | amount } object.
  _isFluidLike(v) {
    return v != null && typeof v === 'object' &&
      (v.fluid !== undefined || v.amount !== undefined || typeof v.getAmount === 'function')
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

    if (global.NCH._isFluidLike(v)) {
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
      json = { type: 'pneumaticcraft:stacked_item' } // G5: only the pc* helpers pass stacked_item, and they skip without PneumaticCraft
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

  // NBT-aware ingredient / result JSON. Used by ieBlueprint (the Engineer's Workbench) and the
  // vdPickling results — every other ie* / create* helper still goes through plain _itemJson.
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
      // KubeJS hides ItemStack.getTag() from scripts; its `.nbt` bean property is the readable one
      var t = null
      if (v && typeof v.getTag === 'function') t = v.getTag()
      else if (v && typeof v.getItem === 'function' && v.nbt != null && typeof v.nbt === 'object') t = v.nbt
      if (t) { var s = String(t); if (s && s !== '{}') stackNbt = s }
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
  // opts.nbtString: write the fluid's NBT as an SNBT string
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
        // opts.nbtString: SNBT string for e.custom JSON (a FluidStackJS hands back a CompoundTag)
        if (nbt) json.nbt = opts.nbtString ? String(nbt) : nbt
      } catch (err) {}
    }

    return json
  },

  // ========================================================================
  // Recipe removal / replacement
  // ========================================================================

  // KubeJS reads a missing filter value as match-all ({output: undefined} removed about 3500 recipes, scratch
  // r1106cba), so a filter that is, or holds at any depth, undefined or null is refused (logged, nothing removed).
  remove(id, e) {
    var path = global.NCH._undefinedIn(id, 'filter')
    if (path) throw new Error('remove refused: ' + path + ' is undefined or null (a dict key that does not exist?)')
    global.NCH._absentInFilter(id, 'remove')
    global.NCH._pruneEmptyIeOutputs(e)
    e.remove(id)
  },

  // "Empty array?" (plan_cb_polish.md 3, scratch r1106cbo: 77 failures on the cooking set). kubejs-immersive-engineering
  // reads an IE output that has no "item" key through IngredientWithSize + IEApi.getPreferredStackbyMod, which throws on
  // a tag with no items. Every output-filtered remove / replaceOutput tests every recipe's outputs, so one such recipe
  // aborts the whole call. IE's own data guards on the input tag, not the output one: metalpress/plate_zinc needs
  // forge:ingots/zinc (Create fills it) but makes forge:plates/zinc (nothing fills it without the full pack). These
  // recipes cannot run anyway (IE would throw on use), so they are removed by id once per recipe event, before the first
  // filter. Only the types kubejs-ie gives a schema are read through IEApi; the rest never throw.
  _ieSchemaTypes: ['alloy', 'arc_furnace', 'blast_furnace', 'blueprint', 'bottling_machine', 'cloche', 'coke_oven',
    'crusher', 'fermenter', 'metal_press', 'mixer', 'refinery', 'sawmill', 'squeezer'],
  _iePrunedFor: null,
  _pruneEmptyIeOutputs(e) {
    if (!e || global.NCH._iePrunedFor === e) return
    global.NCH._iePrunedFor = e
    if (!global.has('immersiveengineering')) return
    var emptyCache = {}
    var tagEmpty = function (t) {
      if (emptyCache[t] === undefined) {
        try { emptyCache[t] = !(Ingredient.of('#' + t).itemIds.length > 0) } catch (err) { emptyCache[t] = true }
      }
      return emptyCache[t]
    }
    // the tag an IE output object resolves through IEApi, or null ({item} and {base_ingredient: {item}} never throw)
    var outTag = function (el) {
      if (!el || !el.isJsonObject()) return null
      var o = el.getAsJsonObject()
      if (o.has('item')) return null
      if (o.has('base_ingredient') && o.get('base_ingredient').isJsonObject()) o = o.getAsJsonObject('base_ingredient')
      return (o.has('tag') && o.get('tag').isJsonPrimitive()) ? String(o.get('tag').getAsString()) : null
    }
    var hasEmpty = function (json) {
      var keys = ['result', 'results', 'output', 'secondaries']
      for (var k = 0; k < keys.length; k++) {
        if (!json.has(keys[k])) continue
        var v = json.get(keys[k])
        var list = v.isJsonArray() ? v.getAsJsonArray() : null
        var n = list ? list.size() : 1
        for (var x = 0; x < n; x++) {
          var el = list ? list.get(x) : v
          // crusher / arc furnace secondaries: {chance, output: {...}}
          if (el.isJsonObject() && el.getAsJsonObject().has('output')) el = el.getAsJsonObject().get('output')
          var t = outTag(el)
          if (t !== null && tagEmpty(t)) return t
        }
      }
      return null
    }
    var filter = global.NCH._ieSchemaTypes.map(function (t) { return { type: 'immersiveengineering:' + t } })
    var removed = []
    var found = e.findRecipes(filter)
    var it = found.iterator()
    while (it.hasNext()) {
      var r = it.next()
      var t = r.json ? hasEmpty(r.json) : null
      if (t !== null) removed.push(String(r.getId()) + ' (#' + t + ')')
    }
    removed.forEach(s => e.remove({ id: s.substring(0, s.indexOf(' ')) }))
    if (removed.length > 0) console.info('[NCH] removed ' + removed.length + ' Immersive Engineering recipes whose output tag is empty: ' + removed.join(', '))
  },

  // G5 for filters: an input/output id (or tag) of a mod that is not loaded matches nothing; count the call for that
  // mod instead of letting KubeJS parse it. Walks plain objects and arrays (or / not / lists); Java objects pass through
  // as before. (The "Empty array?" errors this was first written for were IE output tags, _pruneEmptyIeOutputs.)
  _absentInFilter(v, context, key) {
    if (v === undefined || v === null) return
    if (typeof v === 'string' || v instanceof String) {
      if (key === 'input' || key === 'output') global.NCH._absent(String(v).replace(/^#/, ''), context)
      return
    }
    if (typeof v !== 'object') return
    if (Array.isArray(v)) {
      // a list under input/output is a choice of items: left to KubeJS (one absent entry must not drop the call)
      if (key === 'input' || key === 'output') return
      for (var n = 0; n < v.length; n++) global.NCH._absentInFilter(v[n], context, key)
      return
    }
    if (Object.prototype.toString.call(v) !== '[object Object]') return
    var keys = Object.keys(v)
    for (var k = 0; k < keys.length; k++) {
      var kk = keys[k]
      global.NCH._absentInFilter(v[kk], context, (kk === 'input' || kk === 'output') ? kk : key)
    }
  },

  _undefinedIn(v, path) {
    if (v === undefined || v === null) return path
    if (typeof v !== 'object') return null
    if (Array.isArray(v)) {
      for (var n = 0; n < v.length; n++) {
        var a = global.NCH._undefinedIn(v[n], path + '[' + n + ']')
        if (a) return a
      }
      return null
    }
    // plain JS objects only: Java objects (Ingredient, RegExp wrappers) are values, not filters to walk. Not a
    // prototype check: each script pack has its own Object.prototype, so a filter from a server script never matched
    // this file's (scratch r1106cbb; tools/rhino_scope_test)
    if (Object.prototype.toString.call(v) !== '[object Object]') return null
    var keys = Object.keys(v)
    for (var k = 0; k < keys.length; k++) {
      var b = global.NCH._undefinedIn(v[keys[k]], path + '.' + keys[k])
      if (b) return b
    }
    return null
  },

  replaceInput(filter, oldInput, newInput, e, includeAdded) {
    if (!e) throw new Error('replaceInput called without event object!')
    global.NCH._absentInFilter(filter, 'replaceInput')
    global.NCH._pruneEmptyIeOutputs(e)
    global.NCH._validateAny(oldInput, 'replaceInput old input')
    global.NCH._validateAny(newInput, 'replaceInput new input')
    e.replaceInput(filter, oldInput, newInput)
    return includeAdded ? global.NCH._replaceInAdded(filter, 'input', oldInput, newInput, e) : 0
  },

  replaceOutput(filter, oldOutput, newOutput, e, includeAdded) {
    if (!e) throw new Error('replaceOutput called without event object!')
    global.NCH._absentInFilter(filter, 'replaceOutput')
    global.NCH._pruneEmptyIeOutputs(e)
    global.NCH._validateAny(oldOutput, 'replaceOutput old output')
    global.NCH._validateAny(newOutput, 'replaceOutput new output')
    e.replaceOutput(filter, oldOutput, newOutput)
    return includeAdded ? global.NCH._replaceInAdded(filter, 'output', oldOutput, newOutput, e) : 0
  },

  // Sets the cooking time (ticks) of every loaded recipe the filter matches (smelting-style and Farmer's Delight
  // pot recipes both read `cookingtime`). Returns the number of recipes changed.
  setCookTime(filter, time, e) {
    if (!e) throw new Error('setCookTime called without event object!')
    global.NCH._pruneEmptyIeOutputs(e)
    var n = 0
    e.forEachRecipe(filter, r => { r.json.addProperty('cookingtime', time); n++ })
    return n
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

  // Item merges (perf.md 11.2 #4): swaps every [old, new] item pair in the inputs and outputs of every recipe, data
  // and script-added alike, in list order, as replaceInput / replaceOutput with includeAdded would per pair. One
  // filtered pass over the data recipes and one over the added ones (prefiltered by a single match of every old id)
  // instead of two data scans and two added walks per pair. Returns the number of recipes changed.
  replaceItems(pairs, e) {
    if (!e) throw new Error('replaceItems called without event object!')
    if (!pairs || pairs.length === 0) return 0
    pairs.forEach((p, idx) => {
      global.NCH._validateAny(p[0], 'replaceItems[' + idx + '] old')
      global.NCH._validateAny(p[1], 'replaceItems[' + idx + '] new')
    })
    var anyOld = Ingredient.of(pairs.map(p => p[0]))
    var changed = 0
    var swap = r => {
      var hit = false
      pairs.forEach(p => {
        if (r.replaceInput(p[0], p[1])) hit = true
        if (r.replaceOutput(p[0], p[1])) hit = true
      })
      if (hit) changed++
    }
    global.NCH._pruneEmptyIeOutputs(e)
    e.forEachRecipe([{ input: anyOld }, { output: anyOld }], swap)
    var it = e.addedRecipes.iterator()
    while (it.hasNext()) {
      var r = it.next()
      if (r.hasInput(anyOld) || r.hasOutput(anyOld)) swap(r)
    }
    return changed
  },

  // ========================================================================
  // Config-driven recipe values
  // ========================================================================

  // Takes a whole defaults object for a config-driven craft (keys must match the
  // CommonConfig section's Java field names exactly, e.g. potion/mb/loops/blows, not the toml
  // keys potionMb/hammerBlows) and a
  // dotted path to that section (e.g. 'CRAFTING.FELSIC_SWORD'), and returns a new object
  // with each field replaced by its CommonConfig value where available. Falls back to the
  // given default field-by-field if no registered config class (see registerConfig) holds the
  // section/field. Logs whenever a resolved value differs from its
  // default, so an override in config/creativecommon-common.toml (or a carved jar's file) shows up in the log
  // instead of silently changing recipe output. Meant to sit right next to the recipe it
  // feeds:
  //
  //   const felsicSword = R.cfg({
  //     potion: 'creativecommon:magic_resistance',
  //     mb: 250,
  //     loops: 1,
  //     blows: 3
  //   }, 'CRAFTING.FELSIC_SWORD')
  //
  // Recipes are baked at datapack-reload time (ServerEvents.recipes) - a toml edit needs
  // /reload to reach an already-generated recipe, same as any other config-driven recipe.
  // A section is looked up in every registered config class in order, so a section that moves to a carved jar keeps
  // working without a script change. 'modid:SECTION.FIELD' pins the lookup to one jar's class.
  registerConfig(modId, className) {
    if ($NCH_configClasses.some(c => c.id === modId)) {
      console.warn('[NCH.cfg] ' + modId + ' config already registered; ' + className + ' skipped')
      return
    }
    $NCH_configClasses.push({ id: modId, name: className, cls: undefined })
  },

  cfg(defaults, path) {
    var only = null
    if (path.indexOf(':') > 0) {
      only = path.substring(0, path.indexOf(':'))
      path = path.substring(path.indexOf(':') + 1)
    }
    var section = null
    var owner = null
    $NCH_configClasses.forEach(c => {
      if (section || (only && c.id !== only)) return
      if (c.cls === undefined) {
        try {
          c.cls = Java.loadClass(c.name)
        } catch (err) {
          c.cls = false
          console.warn('[NCH.cfg] ' + c.id + ' config class ' + c.name + ' not on classpath: ' + err)
        }
      }
      if (!c.cls) return
      // Rhino THROWS on a missing static field of a Java class (InternalError "has no public instance field or method
      // named X"), it does not return undefined: a section that moved to a carved jar must read as "not here" in
      // CommonConfig, or the error escapes and cfg() returns undefined (scratch_run_2026-11-01.md 7).
      var found = path.split('.').reduce((obj, seg) => {
        if (obj == null) return null
        try {
          return obj[seg]
        } catch (err) {
          return null
        }
      }, c.cls)
      if (found) {
        section = found
        owner = c.id
      }
    })
    if (!section) {
      console.warn('[NCH.cfg] no ' + (only ? only + ':' : '') + path + ' section in any registered config - using hardcoded defaults')
      return defaults
    }

    var values = {}
    Object.keys(defaults).forEach(field => {
      var configField
      try {
        configField = section[field]
      } catch (err) {
        configField = null // missing Java field: Rhino throws (see the section lookup above)
      }
      if (!configField || typeof configField.get !== 'function') {
        console.warn('[NCH.cfg] no ' + owner + ':' + path + '.' + field + ' field - using default ' + defaults[field])
        values[field] = defaults[field]
        return
      }

      var value = configField.get()
      if (value != defaults[field]) {
        console.log('[NCH.cfg] ' + owner + ':' + path + '.' + field + ' overridden: default ' + defaults[field] + ' -> ' + value)
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
  //     being used up (Creative Delight food.CanServings; the last serving rolls empty/crushed/nothing);
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
        .id(`${global.NCH.ns()}:shaped/${idSuffix}`)

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
      .id(`${global.NCH.ns()}:shapeless/${idSuffix}`)

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
    // input (see kubejs/data/creativedelight/kubejs/creativeengineering/cultivated_meat.js history) - it just corrupts the
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
  // start of every server script load, so Common's IngredientActions puts every registered action
  // back on each call; the can logic is registered by compat.KubeJSCanAction (CD-bound).
  _canServings(recipe, cans, ctx) {
    if (!cans) return
    var opts = (cans === true || typeof cans === 'string' || typeof cans === 'number' || Array.isArray(cans))
      ? { servings: cans } : cans
    var whole = opts.whole ? [].concat(opts.whole) : []
    var servings = null
    if (whole.length) {
      try { servings = Java.loadClass('com.SaintAlfonzo.creativedelight.food.CanServings') } catch (err) {
        console.warn('[NCH] ' + ctx + ': Creative Delight CanServings not available, opened cans are not refused: ' + err)
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
    var actionId = global.NCH.modId + ':can_serving'
    try {
      var actions = Java.loadClass('com.SaintAlfonzo.creativecommon.compat.kubejs.IngredientActions')
      actions.reapply()
      if (!actions.has(actionId)) {
        console.warn('[NCH] ' + ctx + ': no ' + actionId + ' ingredient action registered, cans are used up whole')
        return
      }
    } catch (err) {
      console.warn('[NCH] ' + ctx + ': IngredientActions not available, cans are used up whole: ' + err)
      return
    }
    var filters = cans === true ? [global.item.t_cans] : (Array.isArray(cans) ? cans : [cans])
    for (var f = 0; f < filters.length; f++) {
      recipe.customIngredientAction(filters[f], actionId)
    }
  },

	stonecutting(output, input, e, idOverride) {
		if (!e) throw new Error('stonecutting called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "stonecutting")
		var ctx = 'stonecutting ' + idSuffix

		global.NCH._validateAny(output, ctx + ' output')
		global.NCH._validateAny(input, ctx + ' input')

		e.stonecutting(output, input)
		.id(`${global.NCH.ns()}:stonecutting/${idSuffix}`)
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
			.id(`${global.NCH.ns()}:smithing/${idSuffix}`)
	},

	smelting(output, input, e, idOverride, xp, cookingtime, conditions) {
		if (!e) throw new Error('smelting called without event object!')
		xp = xp ?? 0
		cookingtime = global.NCH._or(cookingtime, 200)
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
		e.custom(json).id(`${global.NCH.ns()}:smelting/${idSuffix}`)
	},

	blasting(output, input, e, idOverride, xp, cookingtime, conditions) {
		if (!e) throw new Error('blasting called without event object!')
		xp = xp ?? 0
		cookingtime = global.NCH._or(cookingtime, 100)
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
		e.custom(json).id(`${global.NCH.ns()}:blasting/${idSuffix}`)
	},

	campfireCooking(output, input, e, idOverride, exp, time) {
		if (!e) throw new Error('campfireCooking called without event object!')
		exp = exp ?? 0
		time = global.NCH._or(time, 600)
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "campfirecooking")
		var ctx = 'campfireCooking ' + idSuffix

		global.NCH._validateAny(output, ctx + ' output')
		global.NCH._validateAny(input, ctx + ' input')

		e
			.campfireCooking(output, input, exp, time)
			.id(`${global.NCH.ns()}:campfirecooking/${idSuffix}`)
	},

	smoking(output, input, e, idOverride, xp, cookingtime) {
		if (!e) throw new Error('smoking called without event object!')
		xp = xp ?? 0
		cookingtime = global.NCH._or(cookingtime, 100)
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
		}).id(`${global.NCH.ns()}:smoking/${idSuffix}`)
	},

	fletching(arrowhead, shaft, fin, result, e, idOverride) {
		if (!global.has('nock_enough_arrows')) return global.NCH._skip('nock_enough_arrows')
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
			result: { item: result.item ?? result, count: global.NCH._or(result.count, 1) }
		}).id(`${global.NCH.ns()}:fletching/${idSuffix}`)
	},

  	fdCooking(input, output, e, idOverride, exp, time, container) {
		if (!global.has('farmersdelight')) return global.NCH._skip('farmersdelight')
		exp = exp ?? 0
		time = global.NCH._or(time, 200)
		if (!e) throw new Error('fdCooking called without event object!')

		var idSuffix = idOverride ?? global.NCH.toId(output, input, "fdcooking")
		var ctx = 'fdCooking ' + idSuffix

		global.NCH._validateAny(input, ctx + ' input')
		global.NCH._validateAny(output, ctx + ' output')
		if (container) global.NCH._validateAny(container, ctx + ' container')

		let r = container
			? e.recipes.farmersdelight.cooking(input, output, exp, time, container)
			: e.recipes.farmersdelight.cooking(input, output, exp, time)

		r.id(`${global.NCH.ns()}:farmersdelightcooking/${idSuffix}`)
	},

  	fdCutting(input, tool, output, e, idOverride) {
		if (!global.has('farmersdelight')) return global.NCH._skip('farmersdelight')
		if (!e) throw new Error('fdCutting called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "fdcutting")
		var ctx = 'fdCutting ' + idSuffix

		global.NCH._validateAny(input, ctx + ' input')
		global.NCH._validateAny(tool, ctx + ' tool')
		global.NCH._validateAny(output, ctx + ' output')

		e.recipes.farmersdelight
		.cutting(input, tool, output)
		.id(`${global.NCH.ns()}:farmersdelightcutting/${idSuffix}`)
	},

	// Farmer's Respite Kettle (farmersrespite:brewing) in the pack's format: Creative Delight's Kettle tank
	// (feature_writeup/farmers_respite.md K8). input: 1-2 ingredients. opts: { base (the tank fluid one brew uses, one
	// 250 mB unit; default water; 'none' for a brew that uses no fluid), container (default glass bottle), time (ticks;
	// left out = Creative Delight's [cooking] kettleBrewTicks, default 600 = 30 s), exp (0.35), tab ('drinks') }. One brew makes one result. A drink
	// tier like Respite's long / strong teas is the tea's fluid as base plus one ingredient.
	//   R.frBrewing([i.t_fruits_berries], i.berry_matcha_latte, e, 'berry_matcha_latte', { base: f.matcha_latte })
	frBrewing(input, output, e, idOverride, opts) {
		if (!global.has('farmersrespite')) return global.NCH._skip('farmersrespite')
		if (!e) throw new Error('frBrewing called without event object!')
		opts = opts || {}
		var inputs = Array.isArray(input) ? input : [input]
		var idSuffix = idOverride ?? global.NCH.toId(output, input, 'frbrewing')
		var ctx = 'frBrewing ' + idSuffix
		if (inputs.length < 1 || inputs.length > 2) global.NCH._warn(ctx, 'the kettle takes 1-2 ingredients')
		if (opts.needWater !== undefined) global.NCH._warn(ctx, 'needWater is gone: give base (a fluid, or \'none\')')
		var base = opts.base ?? global.fluid.water
		if (base !== 'none') global.NCH._checkFluid(base, ctx + ' base')

		var json = {
			type: 'farmersrespite:brewing',
			recipe_book_tab: opts.tab ?? 'drinks',
			base: base === 'none' ? 'none' : { fluid: base, count: 250 },
			ingredients: inputs.map((v, n) => global.NCH._itemJson(v, { countMode: 'none', context: ctx + ' input[' + n + ']' })),
			result: global.NCH._itemJson(output, { allowTag: false, context: ctx + ' result' }),
			container: global.NCH._itemJson(opts.container ?? global.item.glass_bottle, { allowTag: false, countMode: 'none', context: ctx + ' container' }),
			experience: global.NCH._or(opts.exp, 0.35)
		}
		// no time given: the Kettle's configured default applies (Creative Delight KettleRecipeSerializerMixin)
		if (opts.time != null) json.cookingtime = opts.time
		e.custom(json).id(`${global.NCH.ns()}:farmersrespitebrewing/${idSuffix}`)
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
	// What the recipe gets depends on CommonConfig [cooking] ovenIngredientCounts:
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
			cookingtime: global.NCH._or(opts.time, 200)
		}
		if (opts.tool) json.tool = global.NCH._itemJson(opts.tool, { countMode: 'none', context: ctx + ' tool' })
		if (opts.container)
			json.container = global.NCH._itemJson(opts.container, { allowTag: false, countMode: 'none', context: ctx + ' container' })

		e.custom(json).id(`${global.NCH.ns()}:baking/${idSuffix}`)
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
		if (!global.has('vintagedelight')) return global.NCH._skip('vintagedelight')
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
			// the jar reads output NBT (a string), e.g. Berserker's Brew's potion effects
			output: global.NCH._nbtItemJson(output, { allowTag: false, role: 'result', context: ctx + ' result' }),
			processingTime: global.NCH._or(opts.time, 2400)
		}
		if (opts.secondary)
			json.secondaryOutput = global.NCH._nbtItemJson(opts.secondary, { allowTag: false, role: 'result', context: ctx + ' secondary' })
		if (opts.container)
			json.container = global.NCH._itemJson(opts.container, { countMode: 'none', context: ctx + ' container' })

		e.custom(json).id(`${global.NCH.ns()}:pickling/${idSuffix}`)
	},

	// Barbeque's Delight (Creative Delight member barbequesdelight, barbeques_delight.md 4 step 7). The port ships no recipe
	// data; these three types come from here.
	// Skewering: right-click an Ingredients Basin slot holding Item.of(ingredient, n) with the tool (a stick) in the main
	// hand and Item.of(side, m) in the off hand; side null = the off hand must be empty.
	//   R.bbqSkewering(i.raw_beef_skewer, { id: i.t_skewering_beef, count: 2 }, { id: i.t_skewering_cabbage, count: 2 }, e, 'raw_beef_skewer')
	// Tag inputs take { id, count }: Item.of('#tag', n) is the tag's first item
	bbqSkewering(output, ingredient, side, e, idOverride, tool) {
		if (!global.has('barbequesdelight')) return global.NCH._skip('barbequesdelight')
		if (!e) throw new Error('bbqSkewering called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, ingredient, "skewering")
		var ctx = 'bbqSkewering ' + idSuffix
		var ing = global.NCH._resolveIdCount(ingredient)
		var json = {
			type: 'barbequesdelight:skewering',
			tool: global.NCH._itemJson(tool ?? 'minecraft:stick', { countMode: 'none', context: ctx + ' tool' }),
			ingredient: global.NCH._itemJson(ingredient, { countMode: 'none', context: ctx + ' ingredient' }),
			ingredientCount: Math.max(1, ing.count || 1),
			sideCount: 0,
			output: global.NCH._itemJson(output, { allowTag: false, context: ctx + ' output' })
		}
		if (side != null) {
			json.side = global.NCH._itemJson(side, { countMode: 'none', context: ctx + ' side' })
			json.sideCount = Math.max(1, global.NCH._resolveIdCount(side).count || 1)
		}
		e.custom(json).id(`${global.NCH.ns()}:skewering/${idSuffix}`)
	},

	// Grilling: a raw skewer on the Grill; time = ticks for both sides (flip at half).
	//   R.bbqGrilling(i.grilled_beef_skewer, i.raw_beef_skewer, 320, e)
	bbqGrilling(output, input, time, e, idOverride) {
		if (!global.has('barbequesdelight')) return global.NCH._skip('barbequesdelight')
		if (!e) throw new Error('bbqGrilling called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "grilling")
		var ctx = 'bbqGrilling ' + idSuffix
		e.custom({
			type: 'barbequesdelight:grilling',
			ingredient: global.NCH._itemJson(input, { countMode: 'none', context: ctx + ' input' }),
			output: global.NCH._itemJson(output, { allowTag: false, context: ctx + ' output' }),
			barbecuingTime: global.NCH._or(time, 200)
		}).id(`${global.NCH.ns()}:grilling/${idSuffix}`)
	},

	// Combine: a shapeless craft whose result keeps every cooked skewer of the grid (seasoning included) as its contents,
	// for Kebab Wrap, Kebab Sandwich and Bibimbap. inputs: one entry per grid slot (Item.of(x, 2) = two slots).
	bbqCombine(output, inputs, e, idOverride) {
		if (!global.has('barbequesdelight')) return global.NCH._skip('barbequesdelight')
		if (!e) throw new Error('bbqCombine called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, inputs, "combine")
		var ctx = 'bbqCombine ' + idSuffix
		var slots = []
		;(Array.isArray(inputs) ? inputs : [inputs]).forEach((v, idx) => {
			var count = Array.isArray(v) ? 1 : global.NCH._resolveIdCount(v).count
			var entry = global.NCH._itemJson(v, { countMode: 'none', context: ctx + ' input[' + idx + ']' })
			for (var c = 0; c < Math.max(1, count); c++) slots.push(entry)
		})
		if (slots.length < 1 || slots.length > 9)
			global.NCH._warn(ctx, 'needs 1-9 ingredient slots, got ' + slots.length)
		e.custom({
			type: 'barbequesdelight:combine',
			ingredients: slots,
			result: global.NCH._itemJson(output, { allowTag: false, context: ctx + ' result' })
		}).id(`${global.NCH.ns()}:shapeless/${idSuffix}`)
	},

	// Brewin' and Chewin' keg (backport, alcohol.md 8.6): brewinandchewin:fermenting with 4.5.0's field names.
	// output: Fluid.of(id, mB) for a drink / cheese fluid, or an item (Item.of(...)) for a food result.
	// inputs: 1-4 ingredients, one keg slot each; an array entry = any of those, Item.of(x, 2) takes two slots.
	// baseFluid: fluid id, '#tag' or Fluid.of(id, mB) (1000 mB unless given); null = an empty tank.
	// opts: { temperature: 1 cold, 2 chilly, 3 normal (default), 4 warm, 5 hot; time: ticks (default 9600);
	//   xp (default 1); category: 'drinks' (default) | 'meals' }
	//   R.bncFermenting(Fluid.of(f.beer, 1000), [i.wheat, i.dandelion, i.brown_mushroom], f.water, e, 'beer_from_water')
	bncFermenting(output, inputs, baseFluid, e, idOverride, opts) {
		if (!global.has('brewinandchewin')) return global.NCH._skip('brewinandchewin')
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

		e.custom(json).id(`${global.NCH.ns()}:fermenting/${idSuffix}`)
	},

  // ========================================================================
  // Create
  // ========================================================================

	createItemApplication(output, target, tool, e, idOverride) {
		if (!global.has('create')) return global.NCH._skip('create')
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
		e.custom(json).id(`${global.NCH.ns()}:createitemapplication/${idSuffix}`)
	},

	createHaunting(output, input, e, idOverride) {
		if (!global.has('create')) return global.NCH._skip('create')
		if (!e) throw new Error('createHaunting called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "haunting")
		var ctx = 'createHaunting ' + idSuffix

		global.NCH._validateAny(output, ctx + ' output')
		global.NCH._validateAny(input, ctx + ' input')

		e.recipes.create.haunting(output,input).id(`${global.NCH.ns()}:createhaunting/${idSuffix}`)
	},

	createSplashing(output, input, e, idOverride) {
		if (!global.has('create')) return global.NCH._skip('create')
		if (!e) throw new Error('createSplashing called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "splashing")
		var ctx = 'createSplashing ' + idSuffix

		global.NCH._validateAny(output, ctx + ' output')
		global.NCH._validateAny(input, ctx + ' input')

		e.recipes.create.splashing(output,input).id(`${global.NCH.ns()}:createsplashing/${idSuffix}`)
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
		}).id(`${global.NCH.ns()}:dragonbreathing/${idSuffix}`)
	},

	// Dreams n' Desires sanding fan (create_dd:sanding, catalyst #create_dd:fan_processing_catalysts/sanding). One input,
	// up to 12 outputs, chances allowed. opts: { exclusive } (false): one weighted pick per item instead of an
	// independent roll per output (patcherMod fan/ExclusiveResults; chances are slices of 1, any remainder gives nothing).
	//   R.ddSanding([Item.of(i.rose_quartz).withChance(0.8), Item.of(i.polished_rose_quartz).withChance(0.2)],
	//       i.rose_quartz, e, 'rose_quartz', { exclusive: true })
	ddSanding(output, input, e, idOverride, opts) {
		if (!global.has('create', 'create_dd')) return global.NCH._skip('create', 'create_dd')
		if (!e) throw new Error('ddSanding called without event object!')
		opts = opts || {}
		var idSuffix = idOverride ?? global.NCH.toId(output, input, 'ddsanding')
		var ctx = 'ddSanding ' + idSuffix

		global.NCH._validateAny(input, ctx + ' input')
		var outputs = Array.isArray(output) ? output : [output]
		if (outputs.length > 12) global.NCH._warn(ctx, 'sanding takes up to 12 outputs')
		var results = outputs.map((o, n) => {
			global.NCH._validateAny(o, ctx + ' output ' + n)
			return global.NCH._itemJson(o, { countMode: 'flat', includeChance: true, context: ctx + ' output ' + n })
		})

		var json = {
			type: 'create_dd:sanding',
			ingredients: [global.NCH._itemJson(input, { countMode: 'none', context: ctx + ' input' })],
			results: results
		}
		if (opts.exclusive) json.exclusive_results = true
		e.custom(json).id(`${global.NCH.ns()}:ddsanding/${idSuffix}`)
	},

	// Seasoning craft (patcherMod food/SeasoningRecipe): food + one of each seasoning's item, shapeless, gives the same
	// food with those seasonings added as NBT. seasonings: a global.seasoning key or an array of them
	// (development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/seasonings.js, which holds each one's item, bonus and what it combines with). Ids
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
		}).id(`${global.NCH.ns()}:shapeless/season/${idSuffix}`)
	},

	// Backpack skins (patcherMod backpackskin/): smithing a block from the skin tag onto any Sophisticated Backpack
	// keeps the backpack's data and sets its skin; the clear recipe (backpack alone in the grid) removes it.
	backpackSkin(skinTag, e, idOverride) {
		if (!e) throw new Error('backpackSkin called without event object!')
		// the serializer reads Sophisticated Backpacks classes: without the mod the recipe threw NoClassDefFoundError
		// (scratch r1106cbp SQ-CBP-08, TODO `creativebrewing/publish #23`)
		if (!global.has('sophisticatedbackpacks')) return global.NCH._skip('sophisticatedbackpacks')
		var idSuffix = idOverride ?? 'backpack_skin'
		e.custom({
			type: `${global.NCH.modId}:backpack_skin`,
			addition: { tag: skinTag }
		}).id(`${global.NCH.ns()}:smithing/${idSuffix}`)
	},

	backpackSkinClear(e, idOverride) {
		if (!e) throw new Error('backpackSkinClear called without event object!')
		if (!global.has('sophisticatedbackpacks')) return global.NCH._skip('sophisticatedbackpacks')
		var idSuffix = idOverride ?? 'backpack_skin_clear'
		e.custom({ type: `${global.NCH.modId}:backpack_skin_clear` }).id(`${global.NCH.ns()}:backpackskin/${idSuffix}`)
	},

	// Lunchboxes (patcherMod lunchbox/): Spice of Life keeps a box's food in a capability, which vanilla
	// shaped and smithing results drop. These types copy the box's contents, name, dye and skin to the result.
	//   R.lunchboxShaped(i.golden_lunchbox, [' A ', 'ABA', 'AAA'], { A: i.ingot_iron, B: i.lunchbox }, e)
	lunchboxShaped(output, pattern, keys, e, idOverride) {
		if (!e) throw new Error('lunchboxShaped called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, Object.values(keys)[0], "shaped")
		var ctx = 'lunchboxShaped ' + idSuffix
		var key = {}
		Object.keys(keys).forEach((k) => {
			key[k] = global.NCH._itemJson(keys[k], { countMode: 'none', context: ctx + ' key[' + k + ']' })
		})
		e.custom({
			type: `${global.NCH.modId}:lunchbox_shaped`,
			pattern: pattern,
			key: key,
			result: global.NCH._itemJson(output, { allowTag: false, context: ctx + ' output' })
		}).id(`${global.NCH.ns()}:shaped/${idSuffix}`)
	},

	// Smithing tier upgrade keeping the contents. A netherite-ingot addition needs Armor Trims' upgrade template.
	lunchboxSmithing(output, base, addition, e, idOverride) {
		if (!e) throw new Error('lunchboxSmithing called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, base, "smithing")
		var ctx = 'lunchboxSmithing ' + idSuffix
		e.custom({
			type: `${global.NCH.modId}:lunchbox_upgrade`,
			base: global.NCH._itemJson(base, { countMode: 'none', context: ctx + ' base' }),
			addition: global.NCH._itemJson(addition, { countMode: 'none', context: ctx + ' addition' }),
			result: global.NCH._itemJson(output, { allowTag: false, context: ctx + ' output' })
		}).id(`${global.NCH.ns()}:smithing/${idSuffix}`)
	},

	// Skin (smithing, lower-tier empty box or berry basket used up), dye (grid, leather-style mixing) and soap
	// (grid: dye off first, then the skin). The allowed skins are fixed in Java (Lunchboxes.SKINS).
	lunchboxSkin(e, idOverride) {
		if (!e) throw new Error('lunchboxSkin called without event object!')
		e.custom({ type: `${global.NCH.modId}:lunchbox_skin` }).id(`${global.NCH.ns()}:smithing/${idOverride ?? 'lunchbox_skin'}`)
	},

	lunchboxDye(e, idOverride) {
		if (!e) throw new Error('lunchboxDye called without event object!')
		e.custom({ type: `${global.NCH.modId}:lunchbox_dye` }).id(`${global.NCH.ns()}:lunchbox/${idOverride ?? 'lunchbox_dye'}`)
	},

	lunchboxSoap(e, idOverride) {
		if (!e) throw new Error('lunchboxSoap called without event object!')
		e.custom({ type: `${global.NCH.modId}:lunchbox_soap` }).id(`${global.NCH.ns()}:lunchbox/${idOverride ?? 'lunchbox_soap'}`)
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
		}).id(`${global.NCH.ns()}:elytraduplication/${idSuffix}`)
	},

	echoAssembly(out, base, key, e, potionA, potionB) {
		if (!global.has('create', 'vintageimprovements')) return global.NCH._skip('create', 'vintageimprovements')
		if (!e) throw new Error('echoAssembly called without event object!')
		var inc = global.NCH.incomplete(key)
		global.NCH.createSequencedAssembly(Item.of(out, 1), base, [
			e.recipes.create.filling(inc, [inc, Fluid.of(global.fluid.lava, 250)]),
			e.recipes.create.deploying(inc, [inc, global.item.echo_ingot]),
			e.recipes.create.deploying(inc, [inc, global.item.amethyst_dust]),
			e.custom({ type: 'vintageimprovements:vibrating', ingredients: [{ item: inc }], results: [{ item: inc }] }),
			e.recipes.create.filling(inc, [inc, Fluid.of(global.fluid.create_potion, 250, '{Potion:"' + potionA + '"}')]),
			e.recipes.create.filling(inc, [inc, Fluid.of(global.fluid.create_potion, 250, '{Potion:"' + potionB + '"}')])
		], [key], e, key + '_sequenced_assembly', 1)
	},

	// mb: the last fill (potion); reachMb: the first fill (Long Reach), mb when not given. CommonConfig
	// CRAFTING.FELSIC_<PIECE> potionMb / reachPotionMb (gear_tuning.md F2).
	felsicAssembly(out, base, key, e, potion, mb, loops, hammerBlows, reachMb) {
		if (!global.has('create', 'vintageimprovements')) return global.NCH._skip('create', 'vintageimprovements')
		if (!e) throw new Error('felsicAssembly called without event object!')
		var inc = global.NCH.incomplete(key)
		var firstMb = global.NCH._or(reachMb, mb)
		global.NCH.createSequencedAssembly(Item.of(out, 1), base, [
			e.recipes.create.filling(inc, [inc, Fluid.of(global.fluid.create_potion, firstMb, '{Bottle:"REGULAR",Potion:"creativebrewing:long_reach"}')]),
			e.recipes.create.deploying(inc, [inc, global.item.ingot_felsic]),
			e.custom({ type: 'vintageimprovements:hammering', hammerBlows: global.NCH._or(hammerBlows, 3), ingredients: [{ item: inc }], results: [{ item: inc }] }),
			e.recipes.create.filling(inc, [inc, Fluid.of(global.fluid.create_potion, mb, '{Bottle:"REGULAR",Potion:"' + potion + '"}')])
		], [key], e, key + '_sequenced_assembly', loops)
	},

	dragonAssembly(out, base, key, e, potion, mb, loops) {
		if (!global.has('create', 'vintageimprovements')) return global.NCH._skip('create', 'vintageimprovements')
		if (!e) throw new Error('dragonAssembly called without event object!')
		var inc = global.NCH.incomplete(key)
		global.NCH.createSequencedAssembly(Item.of(out, 1), base, [
			e.recipes.create.filling(inc, [inc, Fluid.of(global.fluid.dragon_breath, 250)]),
			e.recipes.create.deploying(inc, [inc, global.item.dragon_scale]),
			e.custom({ type: 'vintageimprovements:laser_cutting', ingredients: [{ item: inc }], results: [{ item: inc }] }),
			e.recipes.create.filling(inc, [inc, Fluid.of(global.fluid.create_potion, mb, '{Bottle:"LINGERING",Potion:"' + potion + '"}')])
		], [key], e, key + '_sequenced_assembly', loops)
	},

	createCrushing(output, input, e, idOverride, mirrorMilling) {
		if (!global.has('create')) return global.NCH._skip('create')
		if (!e) throw new Error('createCrushing called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "createcrushing")
		var ctx = 'createCrushing ' + idSuffix

		global.NCH._validateAny(output, ctx + ' output')
		global.NCH._validateAny(input, ctx + ' input')

		e.recipes.create.crushing(output,input).id(`${global.NCH.ns()}:createcrushing/${idSuffix}`)

		// Optional: also register the same output/input as a Millstone recipe, for
		// ores/materials that should be processable on either machine (the Millstone
		// is simpler but doesn't need Crushing Wheels). Gets its own toId-derived id
		// rather than reusing idSuffix - a "_by_createcrushing" suffix on a milling
		// recipe would be misleading.
		if (mirrorMilling) global.NCH.createMilling(output, input, e)
	},

	createMilling(output, input, e, idOverride) {
		if (!global.has('create')) return global.NCH._skip('create')
		if (!e) throw new Error('createMilling called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "createmilling")
		var ctx = 'createMilling ' + idSuffix

		global.NCH._validateAny(output, ctx + ' output')
		global.NCH._validateAny(input, ctx + ' input')

		e.recipes.create.milling(output,input).id(`${global.NCH.ns()}:createmilling/${idSuffix}`)
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
			.id(`${global.NCH.ns()}:mortar/${idSuffix}`)
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
		if (!global.has('create')) return global.NCH._skip('create')
		if (!e) throw new Error('createPressing called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "createpressing")
		var ctx = 'createPressing ' + idSuffix

		global.NCH._validateAny(output, ctx + ' output')
		global.NCH._validateAny(input, ctx + ' input')

		let r = e.recipes.create.pressing(output,input)
		r.id(`${global.NCH.ns()}:createpressing/${idSuffix}`)
	},

	createCompacting(output, input, e, idOverride, heat) {
		if (!global.has('create')) return global.NCH._skip('create')
		if (!e) throw new Error('createCompacting called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "compacting")
		var ctx = 'createCompacting ' + idSuffix

		global.NCH._validateAny(output, ctx + ' output')
		global.NCH._validateAny(input, ctx + ' input')

		let r = e.recipes.create.compacting(output,input)

		if (heat === "heated") r.heated()
		if (heat === "superheated") r.superheated()

		r.id(`${global.NCH.ns()}:createcompacting/${idSuffix}`)
	},

	createMixing(output, input, e, idOverride, heat) {
		if (!global.has('create')) return global.NCH._skip('create')
		if (!e) throw new Error('createMixing called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "mixing")
		var ctx = 'createMixing ' + idSuffix

		global.NCH._validateAny(output, ctx + ' output')
		global.NCH._validateAny(input, ctx + ' input')

		let r = e.recipes.create.mixing(output,input)

		if (heat === "heated") r.heated()
		if (heat === "superheated") r.superheated()

		r.id(`${global.NCH.ns()}:createmixing/${idSuffix}`)
	},

	createSaw(output, input, e, idOverride) {
		if (!global.has('create')) return global.NCH._skip('create')
		if (!e) throw new Error('createSaw called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "createsawing")
		var ctx = 'createSaw ' + idSuffix

		global.NCH._validateAny(output, ctx + ' output')
		global.NCH._validateAny(input, ctx + ' input')

		e.recipes.create
		.cutting(output, input)
		.id(`${global.NCH.ns()}:createsaw/${idSuffix}`)
	},

	createDeploying(output, input, e, idOverride, keepIngredient) {
		if (!global.has('create')) return global.NCH._skip('create')
		if (!e) throw new Error('createDeploying called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "deploying")
		var ctx = 'createDeploying ' + idSuffix

		global.NCH._validateAny(output, ctx + ' output')
		global.NCH._validateAny(input, ctx + ' input')
		if (keepIngredient) global.NCH._validateAny(keepIngredient, ctx + ' keepIngredient')

		var recipe = e.recipes.create
		.deploying(output, input)
		.id(`${global.NCH.ns()}:createdeploying/${idSuffix}`)

		if (keepIngredient) recipe.keepIngredient(keepIngredient)
	},


	/*
	Cutting	Mechanical Saw	inline e.recipes.create.cutting(...) (no dedicated NCH wrapper)
	Deploying	Deployer	neoCraftHelper.js:1033 createDeploying
	Pressing	Mechanical Press	inline e.recipes.create.pressing(...)
	Filling	Spout	neoCraftHelper.js:1142 createFilling

	Rolling	Rolling Mill	neoCraftHelper.js:1160 createRolling — helper exists but no recipe currently puts it inside a sequence

	Polishing	Grinder	neoCraftHelper.js:1271 viPolishing — already used as a step (crystal.js:22)
	Laser Cutting	Laser	neoCraftHelper.js:1239 viLaserCutting — helper exists, not used as a step yet
	Vibrating	Vibrating Table	neoCraftHelper.js:1218 viVibrating — helper exists, not used as a step yet
	Turning	Lathe	no NCH helper
	Vacuumizing	Vacuum Chamber (evacuate)	viVacuumizing (standalone; not used as a step yet)
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
		if (!global.has('create')) return global.NCH._skip('create')
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
		transitionalItem = transitionalItem || global.NCH.INCOMPLETE[idOverride] || global.item.incomplete

		// The steps were already added to the event by e.recipes.create.*; KubeJS drops a step only when the assembly
		// consumes it (newRecipe = false). Every path that does not build the assembly must drop them too, or they stay
		// as loose recipes (scratch r1106spf: the felsic ingot's filling step left as create:kjs_<hash> without Felsic Gear).
		var steps = Array.isArray(sequence) ? sequence : [sequence]
		var dropSteps = function () { steps.forEach(st => { try { st.newRecipe = false } catch (err) {} }) }

		try {
			global.NCH._validateAny(output, ctx + ' output')
			global.NCH._validateAny(input, ctx + ' input')
			global.NCH._validateAny(transitionalItem, ctx + ' transitionalItem')
		} catch (err) {
			dropSteps()
			throw err
		}

		if (transitionalItem === global.item.incomplete)
			console.warn(`WARN: ${idOverride} has no incomplete item - pass ['<key>'] and register it in transition_items.js`)

		// G5 for steps: a step built by the caller with Fluid.of(<absent mod's fluid>) carries the empty fluid, and Create
		// rejects the whole recipe (horror lasagna on the chain without Central Kitchen, scratch r1106cbo). Skip the recipe
		// quietly, counted in the [absent mods] summary, and drop its steps. Until 2026-11-06 night the loop only counted
		// and the assembly was still built (scratch r1106spf SQ-CBP-03b: horror lasagna kept its empty filling step).
		for (var si = 0; si < steps.length; si++) {
			if (!global.NCH._stepHasEmptyFluid(steps[si])) continue
			dropSteps()
			return global.NCH._absent('minecraft:empty', ctx + ' step[' + si + ']')
		}

		let r = e.recipes.create.sequenced_assembly(output, input, sequence)
		  .transitionalItem(transitionalItem)

		if (loops) r.loops(loops)

		r.id(`${global.NCH.ns()}:createsequencedassembly/${idOverride}`)
	},

	// true when a KubeJS recipe object (a sequenced-assembly step) has an empty fluid among its inputs. Only fluids are
	// checked: an empty item input can be a real optional slot. Any API surprise reads as "not empty".
	// kubejs-create 1902 builds filling / emptying inputs with FluidComponents.INPUT_OR_ITEM_ARRAY, so each value is a
	// com.mojang.datafixers.util.Either (left = InputFluid, right = InputItem): unwrap it before the Fluid test
	// (scratch r1106cbp SQ-CBP-03: the horror lasagna filling step was never seen). CreateInputFluid exposes
	// kjs$isEmpty, which Rhino may or may not remap to isEmpty: try both.
	_stepHasEmptyFluid(step) {
		try {
			if (!step || typeof step.inputValues !== 'function') return global.NCH._stepJsonHasEmptyFluid(step)
			var vals = step.inputValues()
			var hit = false
			var emptyOf = function (v) {
				if (typeof v.isEmpty === 'function') return v.isEmpty()
				if (typeof v['kjs$isEmpty'] === 'function') return v['kjs$isEmpty']()
				return false
			}
			var visit = function (v) {
				if (v === null || v === undefined || hit) return
				if (Array.isArray(v)) { v.forEach(visit); return }
				if (typeof v !== 'object' || typeof v.getClass !== 'function') return
				if (v.getClass().isArray()) {
					for (var n = 0; n < v.length; n++) visit(v[n])
					return
				}
				var cls = String(v.getClass().getName())
				if (cls.indexOf('datafixers.util.Either') >= 0) {
					var l = v.left(), r = v.right()
					if (l.isPresent()) visit(l.get())
					if (r.isPresent()) visit(r.get())
					return
				}
				if (cls.indexOf('Fluid') >= 0 && emptyOf(v)) hit = true
			}
			for (var k = 0; k < vals.length; k++) visit(vals[k] ? vals[k].value : null)
			return hit || global.NCH._stepJsonHasEmptyFluid(step)
		} catch (err) {
			return global.NCH._stepJsonHasEmptyFluid(step)
		}
	},

	// Second check, independent of how kubejs-create wraps the inputs: serialize the step and look for the empty fluid
	// in its JSON (the form Create rejects: {"fluid":"minecraft:empty",...}). Scratch r1106spf SQ-CBP-03b: the value walk
	// above still missed the horror lasagna's filling step on the 18:51 build.
	_stepJsonHasEmptyFluid(step) {
		try {
			if (!step) return false
			if (typeof step.serialize === 'function') step.serialize()
			var j = step.json
			if (j === undefined || j === null) return false
			return /"fluid"\s*:\s*"minecraft:empty"/.test(String(j))
		} catch (err) {
			return false
		}
	},

	createIncompleteItem(event, name, textureLocation, colorHex) {
		if (!global.has('create')) return global.NCH._skip('create')
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
		if (!global.has('create')) return global.NCH._skip('create')
		if (!e) throw new Error('createMechanicalCrafting called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, Object.values(keys)[0], "mechanicalcrafting")
		var ctx = 'createMechanicalCrafting ' + idSuffix

		global.NCH._validateAny(output, ctx + ' output')
		Object.keys(keys).forEach((k) => {
			global.NCH._validateAny(keys[k], ctx + ' key[' + k + ']')
		})

		e.recipes.create
		.mechanical_crafting(output, pattern, keys)
		.id(`${global.NCH.ns()}:createmechanicalcrafting/${idSuffix}`)
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
    .id(`${global.NCH.ns()}:createfilling/${idSuffix}`)
	},

	// Create Item Drain: input -> outputs, an item and/or a fluid.
	//   R.createEmptying([i.tankard, Fluid.of(f.beer, 250)], i.beer, e, 'beer')
	createEmptying(outputs, input, e, idOverride) {
		if (!global.has('create')) return global.NCH._skip('create')
		if (!e) throw new Error('createEmptying called without event object!')
		var outArr = Array.isArray(outputs) ? outputs : [outputs]
		var idSuffix = idOverride ?? global.NCH.toId(outArr[0], input, "createemptying")
		var ctx = 'createEmptying ' + idSuffix

		global.NCH._validateAny(outArr, ctx + ' output')

		// An input carrying NBT (Item.of(id, '{...}'), a { item, nbt } shorthand) is written as a
		// forge:partial_nbt ingredient: KubeJS's own NBT ingredients have matched nothing in this pack
		// (item_dict.js, Sample Syringe note).
		var inJson = (input && typeof input === 'object' && !Array.isArray(input) && !global.NCH._ingredientLike(input))
			? global.NCH._nbtItemJson(input, { role: 'input', context: ctx + ' input' }) : null
		if (inJson && inJson.type === 'forge:partial_nbt') {
			e.custom({
				type: 'create:emptying',
				ingredients: [inJson],
				results: outArr.map((o, idx) => global.NCH._isFluidLike(o)
					? global.NCH._fluidJson(o, { allowTag: false, nbtString: true, context: ctx + ' output[' + idx + ']' })
					: global.NCH._nbtItemJson(o, { role: 'result', countMode: 'flat', allowTag: false, context: ctx + ' output[' + idx + ']' }))
			}).id(`${global.NCH.ns()}:createemptying/${idSuffix}`)
			return
		}
		global.NCH._validateAny(input, ctx + ' input')

		e.recipes.create
		.emptying(outArr, input)
		.id(`${global.NCH.ns()}:createemptying/${idSuffix}`)
	},

	// A drink and its fluid both ways on Create: Spout (container + mb -> drink) and Item Drain (drink -> container +
	// mb). Ids createfilling/<id> and createemptying/<id>. The Farmer's Respite Kettle reads these pairs (glass bottle,
	// 250 mB) to pour and bottle its tank, so every Kettle drink has one (feature_writeup/farmers_respite.md K10).
	// opts: { container (default glass bottle), mb (250) }.
	//   R.createBottle(i.milk_coffee, f.milk_coffee, e, 'milk_coffee')
	createBottle(drink, fluid, e, idOverride, opts) {
		if (!e) throw new Error('createBottle called without event object!')
		opts = opts || {}
		var idSuffix = idOverride ?? global.NCH.toId(drink, fluid, 'createbottle')
		var container = opts.container ?? global.item.glass_bottle
		var mb = global.NCH._or(opts.mb, 250)
		global.NCH._checkFluid(fluid, 'createBottle ' + idSuffix + ' fluid')
		global.NCH.createFilling(drink, [container, Fluid.of(fluid, mb)], e, idSuffix)
		global.NCH.createEmptying([container, Fluid.of(fluid, mb)], drink, e, idSuffix)
	},

	createRolling(output, input, e, idOverride) {
		if (!global.has('createaddition')) return global.NCH._skip('createaddition')
		if (!e) throw new Error('createRolling called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "rolling")
		var ctx = 'createRolling ' + idSuffix

		const json = {
		  type: 'createaddition:rolling',
		  input: global.NCH._itemJson(input, { countMode: 'none', context: ctx + ' input' }),
		  result: global.NCH._itemJson(output, { countMode: 'flat', allowTag: false, context: ctx + ' output' })
		}
		e.custom(json).id(`${global.NCH.ns()}:createrolling/${idSuffix}`)
	},

	createCharging(output, input, e, idOverride, energy, maxChargeRate) {
		if (!global.has('createaddition')) return global.NCH._skip('createaddition')
		if (!e) throw new Error('createCharging called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, input, "charging")
		var ctx = 'createCharging ' + idSuffix

		const json = {
			type: 'createaddition:charging',
			input: global.NCH._itemJson(input, { countMode: 'flat', context: ctx + ' input' }),
			result: global.NCH._itemJson(output, { countMode: 'flat', allowTag: false, context: ctx + ' output' }),
			energy: global.NCH._or(energy, 4000),
			maxChargeRate: global.NCH._or(maxChargeRate, 200)
		}

		e.custom(json).id(`${global.NCH.ns()}:createcharging/${idSuffix}`)
	},

	createExtruding(output, ingredients, e, idOverride, catalyst, bonks) {
		if (!global.has('create_mechanical_extruder')) return global.NCH._skip('create_mechanical_extruder')
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
			requiredBonks: global.NCH._or(bonks, 1)
		}

		if (catalyst) json.catalyst = global.NCH._itemJson(catalyst, { countMode: 'flat', context: ctx + ' catalyst' })

		e.custom(json).id(`${global.NCH.ns()}:extruding/${idSuffix}`)
	},

	viVibrating(outputs, inputs, e, idOverride, processingTime) {
		if (!global.has('vintageimprovements')) return global.NCH._skip('vintageimprovements')
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
		  processingTime: global.NCH._or(processingTime, 300)
		}

		e.custom(json).id(`${global.NCH.ns()}:vivibrating/${idSuffix}`)
	},

	// Vintage Improvements Vacuum Chamber, evacuating ('vintageimprovements:vacuumizing'). Each ingredient slot holds one
	// item, so a counted input (Item.of(x, 3)) becomes that many slots. 600 ticks by default (the mod's powder snow).
	//   R.viVacuumizing(i.powdered_sugar_bucket, [i.bucket, Item.of(i.sugar, 4)], e, 'powdered_sugar_bucket')
	viVacuumizing(outputs, inputs, e, idOverride, processingTime) {
		if (!global.has('vintageimprovements')) return global.NCH._skip('vintageimprovements')
		if (!e) throw new Error('viVacuumizing called without event object!')
		if (!outputs || !inputs) throw new Error('viVacuumizing called without inputs or outputs!')

		var idSuffix = idOverride ?? global.NCH.toId(outputs, inputs, "vacuumizing")
		var ctx = 'viVacuumizing ' + idSuffix

		var slots = []
		;(Array.isArray(inputs) ? inputs : [inputs]).forEach((v, idx) => {
			var count = Array.isArray(v) ? 1 : global.NCH._resolveIdCount(v).count
			var entry = global.NCH._itemJson(v, { countMode: 'none', context: ctx + ' ingredient[' + idx + ']' })
			for (var c = 0; c < Math.max(1, count); c++) slots.push(entry)
		})

		const json = {
		  type: 'vintageimprovements:vacuumizing',
		  ingredients: slots,
		  results: (Array.isArray(outputs) ? outputs : [outputs]).map((o, idx) => {
		      return global.NCH._itemJson(o, { countMode: 'flat', allowTag: false, includeChance: true, context: ctx + ' result[' + idx + ']' })
		  }),
		  processingTime: global.NCH._or(processingTime, 600)
		}

		e.custom(json).id(`${global.NCH.ns()}:vivacuumizing/${idSuffix}`)
	},
	
	viLaserCutting(outputs, inputs, e, idOverride, energy, maxChargeRate) {
		if (!global.has('create', 'vintageimprovements')) return global.NCH._skip('create', 'vintageimprovements')
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
			energy: global.NCH._or(energy, 2000),
			maxChargeRate: global.NCH._or(maxChargeRate, 50)
		}

		e.custom(json).id(`${global.NCH.ns()}:lasercutting/${idSuffix}`)
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
		if (!global.has('vintageimprovements')) return global.NCH._skip('vintageimprovements')
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
			speed_limits: global.NCH._or(speedLimits, 1)
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
	// effect that isn't registered skips the recipe. An effect or a provider of a mod that is not loaded is left out
	// quietly (G5: counted per mod, not warned), so one absent mod's egg does not drop a vanilla effect's page.
	//   R.jeedProvider('minecraft:health_boost', [i.stal_wine, i.solaris_wine], e, 'vinery_health_boost')
	jeedProvider(effect, providers, e, idOverride) {
		if (!global.has('jeed')) return global.NCH._skip('jeed')
		if (!e) throw new Error('jeedProvider called without event object!')
		var idSuffix = idOverride ?? String(effect).replace(':', '_')
		var ctx = 'jeedProvider ' + idSuffix
		var effectKnown = global.NCH._effectExists(effect)
		if (!effectKnown) {
			global.NCH._absent(String(effect), ctx)
			global.NCH._warn(ctx, 'effect "' + effect + '" is not registered')
			return
		}
		var json = global.NCH._jeedProviderJson(providers, ctx)
		if (json.length < 1) {
			if (!json.absentOnly) global.NCH._warn(ctx, 'no providers')
			return
		}
		e.custom({
			type: 'jeed:effect_provider',
			effect: String(effect),
			providers: json
		}).id(`${global.NCH.ns()}:jeed/${idSuffix}`)
	},

	// jeed:potion_provider: items that carry a potion in NBT (`Potion`, like minecraft:potion). JEED lists
	// each one, filled with every potion that has the effect, on that effect's page.
	//   R.jeedPotionProvider([i.ce_potion_bucket, i.potion_bundle], e, 'potion_carriers')
	jeedPotionProvider(providers, e, idOverride) {
		if (!global.has('jeed')) return global.NCH._skip('jeed')
		if (!e) throw new Error('jeedPotionProvider called without event object!')
		var idSuffix = idOverride ?? 'potion_provider'
		var ctx = 'jeedPotionProvider ' + idSuffix
		var json = global.NCH._jeedProviderJson(providers, ctx)
		if (json.length < 1) {
			if (!json.absentOnly) global.NCH._warn(ctx, 'no providers')
			return
		}
		e.custom({
			type: 'jeed:potion_provider',
			providers: json
		}).id(`${global.NCH.ns()}:jeed/${idSuffix}`)
	},

	_jeedProviderJson(providers, ctx) {
		var list = Array.isArray(providers) ? providers : [providers]
		var out = []
		var absent = 0
		list.forEach((v, idx) => {
			var id = (v && typeof v.item === 'string') ? v.item : global.NCH._resolveIdCount(v).id
			// G5 per provider: an absent mod's item drops only that entry (counted), not the whole recipe
			try {
				if (!global.NCH._checkItem(id, ctx + ' provider[' + idx + ']')) return
			} catch (err) {
				if (!(err && err.nchAbsent)) throw err
				var counts = global.NCH._absentCounts
				counts[err.nchAbsent] = (counts[err.nchAbsent] || 0) + 1
				absent++
				return
			}
			out.push(global.NCH._nbtItemJson(v, { countMode: 'none', context: ctx + ' provider[' + idx + ']' }))
		})
		// every entry belonged to an absent mod: the caller skips quietly
		if (out.length < 1 && absent > 0) out.absentOnly = true // only on an empty list, which is never written
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
		if (!global.has('immersiveengineering')) return global.NCH._skip('immersiveengineering')
  if (!e) throw new Error('ieMetalPress called without event object!')
  var idSuffix = idOverride ?? global.NCH.toId(output, input, "iepressing")
  var ctx = 'ieMetalPress ' + idSuffix

  global.NCH._validateAny(output, ctx + ' output')
  global.NCH._validateAny(input, ctx + ' input')
  global.NCH._validateAny(mold, ctx + ' mold')

  e.recipes.immersiveengineeringMetalPress(output, input, mold)
    .id(`${global.NCH.ns()}:iemetalpress/${idSuffix}`)
	},

	ieCrushing(output, input, e, idOverride, secondaries, energy) {
		if (!global.has('immersiveengineering')) return global.NCH._skip('immersiveengineering')
		if (!e) throw new Error('ieCrushing called without event object!')
		secondaries = secondaries ?? []
		energy = global.NCH._or(energy, 6000)
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
		}).id(`${global.NCH.ns()}:iecrushing/${idSuffix}`)
	},

	ieAlloy(output, input0, input1, e, idOverride, time) {
		if (!global.has('immersiveengineering')) return global.NCH._skip('immersiveengineering')
		if (!e) throw new Error('ieAlloy called without event object!')
		if (!output || !input0 || !input1) throw new Error('ieAlloy called without inputs or output!')

		var idSuffix = idOverride ?? global.NCH.toId(output, input0, "alloy")
		var ctx = 'ieAlloy ' + idSuffix

		const json = {
			type: 'immersiveengineering:alloy',
			input0: global.NCH._itemJson(input0, { countMode: 'base_ingredient', context: ctx + ' input0' }),
			input1: global.NCH._itemJson(input1, { countMode: 'base_ingredient', context: ctx + ' input1' }),
			result: global.NCH._itemJson(output, { countMode: 'base_ingredient', context: ctx + ' output' }),
			time: global.NCH._or(time, 200)
		}

		e.custom(json).id(`${global.NCH.ns()}:iealloy/${idSuffix}`)
	},

	ieArcFurnace(output, input, e, idOverride, secondaries, additives, time, energy) {
		if (!global.has('immersiveengineering')) return global.NCH._skip('immersiveengineering')
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
		  time: global.NCH._or(time, 100),
		  energy: global.NCH._or(energy, 25600)
		}).id(`${global.NCH.ns()}:iearcfurnace/${idSuffix}`)
	},

	ieSqueezing(fluidOutput, input, e, idOverride, itemOutput, energy) {
		if (!global.has('immersiveengineering')) return global.NCH._skip('immersiveengineering')
		if (!e) throw new Error('ieSqueezing called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(fluidOutput || itemOutput, input, "squeezing")
		var ctx = 'ieSqueezing ' + idSuffix

		const json = {
		  type: 'immersiveengineering:squeezer',
		  input: global.NCH._itemJson(input, { countMode: 'none', context: ctx + ' input' }),
		  fluid: global.NCH._fluidJson(fluidOutput, { defaultAmount: 1000, context: ctx + ' fluid output' }),
		  energy: global.NCH._or(energy, 6000)
		}

		if (itemOutput) {
			json.output = global.NCH._itemJson(itemOutput, { countMode: 'flat', allowTag: false, context: ctx + ' item output' })
		}

		e.custom(json).id(`${global.NCH.ns()}:iesqueezing/${idSuffix}`)
	},

	// IE Mixer: fluid + up to 4 item inputs -> fluid. The input fluid must be a tag ('#forge:fluid/...'):
	// IE's FluidTagInput reads only "tag". A tag string carries no amount, so the input takes the output's amount
	// unless inputAmount is given. Counts go in as IE's base_ingredient form.
	ieMixer(fluidOutput, inputs, fluidInput, e, idOverride, energy, inputAmount) {
		if (!global.has('immersiveengineering')) return global.NCH._skip('immersiveengineering')
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
		  energy: global.NCH._or(energy, 3200)
		}

		e.custom(json).id(`${global.NCH.ns()}:iemixer/${idSuffix}`)
	},

	// IE Bottling Machine. NBT-aware like ieBlueprint: a result may be Item.of(id, '{nbt}'), an input
	// Item.of(id, '{nbt}') / { item, nbt } (forge:partial_nbt). fluid: an id, Fluid.of(...), or
	// { tag | fluid, amount, nbt } (the fluid NBT is subset-matched, patcherMod FluidTagInputMixin).
	//   R.ieBottling(Item.of(i.tipped_arrow, '{Potion:"minecraft:swiftness"}'),
	//     { tag: f.t_potion, amount: 20, nbt: '{Bottle:"LINGERING",Potion:"minecraft:swiftness"}' }, i.arrow, e, 'tipped_arrow/swiftness')
	ieBottling(results, fluid, inputs, e, idOverride, amount) {
		if (!global.has('immersiveengineering')) return global.NCH._skip('immersiveengineering')
		if (!e) throw new Error('ieBottling called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(results, inputs, "iebottling")
		var ctx = 'ieBottling ' + idSuffix

		const json = {
		  type: 'immersiveengineering:bottling_machine',
		  fluid: global.NCH._fluidJson(fluid, { defaultAmount: amount, nbtString: true, context: ctx + ' fluid' }),
		  inputs: (Array.isArray(inputs) ? inputs : [inputs]).map((i, idx) => {
		      return global.NCH._nbtItemJson(i, { countMode: 'flat', role: 'input', context: ctx + ' input[' + idx + ']' })
		  }),
		  results: (Array.isArray(results) ? results : [results]).map((r, idx) => {
		      return global.NCH._nbtItemJson(r, { countMode: 'flat', allowTag: false, role: 'result', context: ctx + ' result[' + idx + ']' })
		  })
		}

		e.custom(json).id(`${global.NCH.ns()}:iebottling/${idSuffix}`)
	},

	ieBlueprint(outputs, inputs, category, e, idOverride) {
		if (!global.has('immersiveengineering')) return global.NCH._skip('immersiveengineering')
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

	  e.custom(json).id(`${global.NCH.ns()}:blueprint/${category}/${idSuffix}`)
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
		if (!global.has('immersiveengineering')) return global.NCH._skip('immersiveengineering')
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
			energy: global.NCH._or(opts.energy, 800),
			input: global.NCH._itemJson(input, { countMode: 'none', context: ctx + ' input' }),
			result: global.NCH._itemJson(output, { countMode: 'flat', context: ctx + ' output' }),
			secondaries: [] // required by IE's serializer even when empty
		}).id(`${global.NCH.ns()}:sawmill/${idSuffix}`)
	},

	// ========================================================================
	// PneumaticCraft
	// ========================================================================

	pcPressureChamber(outputs, inputs, pressure, e, idOverride) {
		if (!global.has('pneumaticcraft')) return global.NCH._skip('pneumaticcraft')
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

		  e.custom(json).id(`${global.NCH.ns()}:pcpressurechamber/${idSuffix}`)
	},

	pcExplosionCrafting(output, input, lossRate, e, idOverride) {
		if (!global.has('pneumaticcraft')) return global.NCH._skip('pneumaticcraft')
		  if (!e) throw new Error('pcExplosionCrafting called without event object!')
		  var idSuffix = idOverride ?? global.NCH.toId(output, input, "explosion")
		  var ctx = 'pcExplosionCrafting ' + idSuffix

		  const json = {
		      type: 'pneumaticcraft:explosion_crafting',
		      input: global.NCH._itemJson(input, { countMode: 'none', context: ctx + ' input' }),
		      results: [global.NCH._itemJson(output, { countMode: 'flat', allowTag: false, context: ctx + ' output' })],
		      loss_rate: lossRate
		  }

		  e.custom(json).id(`${global.NCH.ns()}:pcexplosioncrafting/${idSuffix}`)
	},

	// NOTE: itemOutput/fluidOutput/itemInput/fluidInput are each individually guarded
	// (function tolerates any of them being missing), which technically makes them
	// "optional" by the letter of the rule - but they're kept ahead of e/idOverride
	// since they're the actual identity of the recipe, matching every other function's shape.
	pcThermoPlant(itemOutput, fluidOutput, itemInput, fluidInput, e, idOverride, options) {
		if (!global.has('pneumaticcraft')) return global.NCH._skip('pneumaticcraft')
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

		e.custom(json).id(`${global.NCH.ns()}:pcthermoplant/${idSuffix}`)
	},

	// Ender Storage's shaped type (enderstorage:create_recipe): a vanilla shaped grid whose result keeps
	// the frequency of an Ender Storage item in the grid. Keys as in shaped(); an array = any of them.
	//   R.enderStorage(i.es_ender_pouch, ['BLB', 'LPL', 'BWB'], { P: i.t_ender_pearls, ... }, e, 'ender_pouch')
	enderStorage(output, pattern, keys, e, idOverride) {
		if (!global.has('enderstorage')) return global.NCH._skip('enderstorage')
		if (!e) throw new Error('enderStorage called without event object!')
		var idSuffix = idOverride ?? global.NCH.toId(output, Object.values(keys)[0], 'enderstorage')
		var ctx = 'enderStorage ' + idSuffix
		var key = {}
		Object.keys(keys).forEach((k) => {
			key[k] = global.NCH._itemJson(keys[k], { countMode: 'none', context: ctx + ' key[' + k + ']' })
		})
		e.custom({
			type: 'enderstorage:create_recipe',
			pattern: pattern,
			key: key,
			result: global.NCH._itemJson(output, { countMode: 'flat', allowTag: false, context: ctx + ' output' })
		}).id(`${global.NCH.ns()}:enderstorage/${idSuffix}`)
	},

	// Scraping a block with an axe (the axe_scrape tool action) for items. results: one item or an array,
	// each may carry a chance (Item.of(x).withChance(0.5)).
	// opts.soot: Sooty Chimneys' soot_scraping (block = a dirty chimney; scraping cleans it). The only
	// axe scraping recipe type in the pack, so it is required for now.
	//   R.axeScraping(Item.of(i.soot).withChance(0.75), 'sootychimneys:dirty_brick_chimney', e, 'dirty_brick_chimney', { soot: true })
	axeScraping(results, block, e, idOverride, opts) {
		if (!global.has('sootychimneys')) return global.NCH._skip('sootychimneys')
		if (!e) throw new Error('axeScraping called without event object!')
		opts = opts || {}
		if (!opts.soot) throw new Error('axeScraping: only { soot: true } (Sooty Chimneys) is supported')
		var resArr = Array.isArray(results) ? results : [results]
		var idSuffix = idOverride ?? global.NCH.toId(resArr[0], block, 'axescraping')
		var ctx = 'axeScraping ' + idSuffix
		e.custom({
			type: 'sootychimneys:soot_scraping',
			chimney: global.NCH._itemJson(block, { countMode: 'none', allowTag: false, context: ctx + ' block' }),
			results: resArr.map((r, idx) => global.NCH._itemJson(r, { countMode: 'flat', allowTag: false, includeChance: true, context: ctx + ' result[' + idx + ']' })),
			tool: { type: 'sootychimneys:tool_action', action: 'axe_scrape' }
		}).id(`${global.NCH.ns()}:soot_scraping/${idSuffix}`)
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
		if (!global.has('creative_labels')) return global.NCH._skip('creative_labels')
		if (!e) throw new Error('creativeLabelsSpecial called without event object!')
		var types = ['note_dye', 'note_special', 'note_copy', 'note_erase']
		if (types.indexOf(type) < 0)
			throw new Error('creativeLabelsSpecial: unknown type "' + type + '" (one of ' + types.join(', ') + ')')
		var idSuffix = idOverride ?? type
		return e.custom({ type: 'creative_labels:' + type }).id(`${global.NCH.ns()}:creative_labels/${idSuffix}`)
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
		if (err && err.nchAbsent) {
			var counts = global.NCH._absentCounts
			counts[err.nchAbsent] = (counts[err.nchAbsent] || 0) + 1
			return undefined
		}
		console.error('[NCH] ' + key + ' failed and was skipped: ' + (err && err.message ? err.message : err))
		return undefined
	}
	}
})
