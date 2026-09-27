
console.log('Crafting Helper starting')

global.CE = {

  modId: 'creativeengineering',
  AXE: '#forge:tools/axes',

	toId(output, input, process) {
		const resolve = (v) => {
			if (!v) return 'unknown'
			if (Array.isArray(v))
				return resolve(v[0])
			if (typeof v === 'string')
				return v
			if (v.getId)
				return String(v.getId())
			if (v.id && typeof v.id === 'string')
				return v.id
			if (v.item)
				return String(v.item)
			if (v.fluid)
				return String(v.fluid)
			return String(v)
		}

		const clean = (v) =>
			resolve(v)
				.replace(/^.*:/, '')
				.replace(/[^a-zA-Z0-9._-]/g, '_')
				.toLowerCase()

		const base = `${clean(input)}_to_${clean(output)}`

		return process
			? `${base}_by_${process.toLowerCase()}`
			: base
	},
	
	remove(id, e) {
    e.remove(id)
  },
  
  replaceInput(filter, oldInput, newInput, e) {
  	if (!e) throw new Error('replaceInput called without event object!')
  	e.replaceInput(filter, oldInput, newInput)
  },
  
  replaceOutput(filter, oldOutput, newOutput, e) {
  	if (!e) throw new Error('replaceOutput called without event object!')
  	e.replaceOutput(filter, oldOutput, newOutput)
  },
	
	bulk(name, paramSets) {
		if (!name) throw new Error('bulk called without function name!')
		if (!Array.isArray(paramSets)) throw new Error('bulk called without parameter sets array!')

		const func = global.CE[name]

		if (typeof func !== 'function') {
			throw new Error(`Helper function '${name}' does not exist!`)
		}

		paramSets.forEach(params => {
			if (!Array.isArray(params)) {
				throw new Error('bulk encountered invalid parameter set')
			}

			func.apply(global.CE, params)
		})
	},
  
  shaped(output, pattern, keys, e, idOverride, damage) {
    if (!e) throw new Error('shaped called without event object!')
    var recipe = e
        .shaped(output, pattern, keys)
        .id(`${global.CE.modId}:shaped/${idOverride ?? global.CE.toId(output, Object.values(keys)[0], "shaped")}`)

    if (damage) {
        var pairs = Array.isArray(damage[0]) ? damage : [damage];
        for (var d = 0; d < pairs.length; d++) {
            recipe.damageIngredient(pairs[d][0], pairs[d][1]);
        }
    }

    return recipe
	},
	
	shapeless(output, input, e, idOverride, damage) {
    if (!e) throw new Error('shapeless called without event object!')
    var recipe = e
      .shapeless(output, input)
      .id(`${global.CE.modId}:shapeless/${idOverride ?? global.CE.toId(output, input, "shapeless")}`)

    if (damage) {
        // normalize to array-of-pairs so a single [item, amount] pair works too
        var pairs = Array.isArray(damage[0]) ? damage : [damage];
        for (var d = 0; d < pairs.length; d++) {
            recipe.damageIngredient(pairs[d][0], pairs[d][1]);
        }
    }

    return recipe
	},
	
	stonecutting(output, input, e, idOverride) {
		if (!e) throw new Error('stonecutting called without event object!')
		e.stonecutting(output, input)
		.id(`${global.CE.modId}:stonecutting/${idOverride ?? global.CE.toId(output, input, "stonecutting")}`)
	},
	
	smithing(output, base, addition, e, idOverride) {
		if (!e) throw new Error('smithing called without event object!')
		e
			.smithing(output, base, addition)
			.id(`${global.CE.modId}:smithing/${idOverride ?? global.CE.toId(output, base, "smithing")}`)
	},
	
	smelting(output, input, e, xp, cookingtime, conditions, idOverride) {
		if (!e) throw new Error('smelting called without event object!')
		xp = xp ?? 0
		cookingtime = cookingtime ?? 200
		const json = {
			type: 'minecraft:smelting',
			ingredient: { item: input },
			result: output,
			experience: xp,
			cookingtime: cookingtime
		}
		if (conditions) json.conditions = conditions
		e.custom(json).id(`${global.CE.modId}:smelting/${idOverride ?? global.CE.toId(output, input, "smelting")}`)
	},

	blasting(output, input, e, xp, cookingtime, conditions, idOverride) {
		if (!e) throw new Error('blasting called without event object!')
		xp = xp ?? 0
		cookingtime = cookingtime ?? 100
		const json = {
			type: 'minecraft:blasting',
			ingredient: { item: input },
			result: output,
			experience: xp,
			cookingtime: cookingtime
		}
		if (conditions) json.conditions = conditions
		e.custom(json).id(`${global.CE.modId}:blasting/${idOverride ?? global.CE.toId(output, input, "blasting")}`)
	},

	campfireCooking(output, input, e, exp, time, idOverride) {
		if (!e) throw new Error('campfireCooking called without event object!')
		exp = exp ?? 0
		time = time ?? 600
		e
			.campfireCooking(output, input, exp, time)
			.id(`${global.CE.modId}:campfirecooking/${idOverride ?? global.CE.toId(output, input, "campfirecooking")}`)
	},
	
	smoking(output, input, e, idOverride) {
    if (!e) throw new Error('createSmoking called without event object!')
    e.recipes.minecraft.smoking(output, input)
    .id(`${global.CE.modId}:smoking/${idOverride ?? global.CE.toId(output, input, "smoking")}`)
	},
	
	fletching(arrowhead, shaft, fin, result, e, idOverride) {
    if (!e) throw new Error('fletching called without event object!')

    var toIngredient = function(val) {
        if (typeof val === 'object') return val;
        if (typeof val === 'string' && val.charAt(0) === '#') {
            return { tag: val.substring(1) };
        }
        return { item: val };
    }

    e.custom({
        type: 'nock_enough_arrows:fletching',
        arrowhead: toIngredient(arrowhead),
        shaft: toIngredient(shaft),
        fin: toIngredient(fin),
        result: { item: result.item ?? result, count: result.count ?? 1 }
    }).id(`${global.CE.modId}:fletching/${idOverride ?? global.CE.toId(result.item ?? result, typeof arrowhead === 'string' ? arrowhead : 'tag', "fletching")}`)
	},
  
  fdCooking(input, output, e, exp, time, container, idOverride) {
		exp = exp ?? 0
		time = time ?? 200
		if (!e) throw new Error('fdCooking called without event object!')

		let r = container
			? e.recipes.farmersdelight.cooking(input, output, exp, time, container)
			: e.recipes.farmersdelight.cooking(input, output, exp, time)

		r.id(`${global.CE.modId}:farmersdelightcooking/${idOverride ?? global.CE.toId(output, input, "fdcooking")}`)
	},

  fdCutting(input, tool, output, e, idOverride) {
  	if (!e) throw new Error('fdCutting called without event object!')
    e.recipes.farmersdelight
    .cutting(input, tool, output)
    .id(`${global.CE.modId}:farmersdelightcutting/${idOverride ?? global.CE.toId(output, input, "fdcutting")}`)
  },
  
  createItemApplication(output, target, tool, e, idOverride) {
		if (!e) throw new Error('createItemApplication called without event object!')
		const toIngredient = (i) => (typeof i === 'string' && i.startsWith('#')) ? { tag: i.replace('#', '') } : { item: i.item ? i.item : i }
		const toResult = (o) => (typeof o === 'string') ? { item: o, count: 1 } : { item: (o.id ? o.id : o.item), count: (o.count ? o.count : 1) }
		const json = {
		  type: 'create:item_application',
		  ingredients: [
		    toIngredient(target),
		    toIngredient(tool)],
		  results: [toResult(output)]}
		e.custom(json).id(
			`${global.CE.modId}:createitemapplication/${idOverride ?? global.CE.toId(output, target, "itemapplication")}`)
	},
  
  createHaunting(output, input, e, idOverride) {
  	if (!e) throw new Error('createHaunting called without event object!')
  	e.recipes.create.haunting(output,input).id(`${global.CE.modId}:createhaunting/${idOverride ?? global.CE.toId(output, input, "haunting")}`)
	},
	
	createSplashing(output, input, e, idOverride) {
  	if (!e) throw new Error('createSplashing called without event object!')
  	e.recipes.create.splashing(output,input).id(`${global.CE.modId}:createsplashing/${idOverride ?? global.CE.toId(output, input, "splashing")}`)
	},
	
	createCrushing(output, input, e, idOverride) {
  	if (!e) throw new Error('createCrushing called without event object!')
  	e.recipes.create.crushing(output,input).id(`${global.CE.modId}:createcrushing/${idOverride ?? global.CE.toId(output, input, "createcrushing")}`)
	},
	
	createPressing(output, input, e, idOverride) {
		if (!e) throw new Error('createPressingcalled without event object!')
  	let r = e.recipes.create.pressing(output,input)
  	
  	r.id(`${global.CE.modId}:createpressing/${idOverride ?? global.CE.toId(output, input, "createpressing")}`)
	},
  
  createCompacting(output, input, e, heat, idOverride) {
  	if (!e) throw new Error('createCompacting called without event object!')
  	let r = e.recipes.create.compacting(output,input)
  	
  	if (heat === "heated") r.heated()
    if (heat === "superheated") r.superheated()
  	
  	r.id(`${global.CE.modId}:createcompacting/${idOverride ?? global.CE.toId(output, input, "compacting")}`)
	},
	
	createMixing(output, input, heat, e, idOverride) {
		if (!e) throw new Error('createMixing called without event object!')
  	let r = e.recipes.create.mixing(output,input)
  	
  	if (heat === "heated") r.heated()
    if (heat === "superheated") r.superheated()
  	
  	r.id(`${global.CE.modId}:createmixing/${idOverride ?? global.CE.toId(output, input, "mixing")}`)
	},
	
	createSaw(output, input, e, idOverride) {
		if (!e) throw new Error('createSaw called without event object!')
    e.recipes.create
    .cutting(output, input)
    .id(`${global.CE.modId}:createsaw/${idOverride ?? global.CE.toId(output, input, "createsawing")}`)
  },
  
  createDeploying(output, input, e, idOverride, keepIngredient) {
    if (!e) throw new Error('createDeploying called without event object!')
    var recipe = e.recipes.create
      .deploying(output, input)
      .id(`${global.CE.modId}:createdeploying/${idOverride ?? global.CE.toId(output, input, "deploying")}`)

    if (keepIngredient) recipe.keepIngredient(keepIngredient)

    return recipe
	},
		
	createSequencedAssembly(output, input, sequence, transitionalItem, loops, e, idOverride) {
		if (!e) throw new Error('createSequencedAssembly called without event object!')
		if (!idOverride) throw new Error('createSequencedAssembly requires an explicit idOverride — auto-generated ids are unreliable for chance-output arrays!')

		let r = e.recipes.create.sequenced_assembly(output, input, sequence)
		  .transitionalItem(transitionalItem)

		if (loops) r.loops(loops)

		r.id(`${global.CE.modId}:createsequencedassembly/${idOverride ?? global.CE.toId(output, input, "sequencedassembly")}`)
	},
	
	createMechanicalCrafting(output, pattern, keys, e, idOverride) {
		if (!e) throw new Error('createMechanicalCrafting called without event object!')
		e.recipes.create
		.mechanical_crafting(output, pattern, keys)
		.id(`${global.CE.modId}:createmechanicalcrafting/${idOverride ?? global.CE.toId(output, Object.values(keys)[0], "mechanicalcrafting")}`)
	},
  
 createFilling(output, input, e, idOverride) {
    if (!e) throw new Error('createFilling called without event object!')
    const itemInput = input.find(i => typeof i === 'string')
    const id = itemInput
        ? global.CE.toId(output, itemInput, "createfilling")
        : global.CE.toId(output, input, "createfilling")
    e.recipes.create
    .filling(output, input)
    .id(`${global.CE.modId}:createfilling/${idOverride ?? id}`)
	},
	
	createRolling(output, input, e, idOverride) {
		if (!e) throw new Error('createRolling called without event object!')
		const id = `${global.CE.modId}:createrolling/${idOverride ?? global.CE.toId(output, input, "rolling")}`
		const json = {
		  type: 'createaddition:rolling',
		  input: typeof input === 'string'
				? (input.startsWith('#')
					? { tag: input.substring(1) }
					: { item: input })
				: { item: input.id },
		  result: {
		    item: typeof output === 'string' ? output : output.getId(),
		    count: typeof output === 'string' ? 1 : output.getCount()
		  }
		}
		let r = e.custom(json)
		r.id(id)
	},
	
		createCharging(output, input, e, energy, maxChargeRate, idOverride) {
		if (!e) throw new Error('createCharging called without event object!')

		const toIngredient = (i) => {
			if (typeof i === 'string') {
				return i.startsWith('#')
					? { tag: i.substring(1) }
					: { item: i }
			}
			const id = i.id ?? i.item
			const isTag = typeof id === 'string' && id.startsWith('#')
			const json = isTag ? { tag: id.substring(1) } : { item: id }
			if (i.count && i.count > 1) json.count = i.count
			return json
		}

		const toResult = (o) => {
			if (typeof o === 'string') return { item: o, count: 1 }
			return { item: o.id ?? o.item, count: o.count ?? 1 }
		}

		const json = {
			type: 'createaddition:charging',
			input: toIngredient(input),
			result: toResult(output),
			energy: energy ?? 4000,
			maxChargeRate: maxChargeRate ?? 200
		}

		e.custom(json).id(`${global.CE.modId}:createcharging/${idOverride ?? global.CE.toId(output, input, "charging")}`)
	},
	
	createExtruding(output, ingredients, e, catalyst, idOverride) {
		if (!e) throw new Error('extruding called without event object!')
		if (!output || !ingredients) throw new Error('extruding called without output or ingredients!')

		const toIngredient = (i) => {
			if (typeof i === 'string') {
				return i.startsWith('#')
					? { tag: i.substring(1) }
					: { item: i }
			}
			if (i.fluid) {
				return { fluid: i.fluid, amount: i.amount ?? 1000 }
			}
			const id = i.item ?? i.tag ?? i.id
			const isTag = (i.tag !== undefined) || (typeof id === 'string' && id.startsWith('#'))
			const json = isTag
				? { tag: (id ?? '').replace('#', '') }
				: { item: id }
			if (i.count && i.count > 1) json.count = i.count
			return json
		}

		const toResult = (o) => {
			if (typeof o === 'string') return { item: o }
			const id = o.item ?? o.id
			const result = { item: id }
			if (o.count && o.count > 1) result.count = o.count
			return result
		}

		const json = {
			type: 'create_mechanical_extruder:extruding',
			ingredients: (Array.isArray(ingredients) ? ingredients : [ingredients]).map(toIngredient),
			result: toResult(output)
		}

		if (catalyst) json.catalyst = toIngredient(catalyst)

		// prefer an item-type ingredient or the catalyst for naming, since the fluid alone isn't very distinguishing
		const nameSource = catalyst ?? ingredients.find(i => typeof i === 'string' || i.item) ?? ingredients

		e.custom(json).id(`${global.CE.modId}:extruding/${idOverride ?? global.CE.toId(output, nameSource, "extruding")}`)
	},
	
	viVibrating(outputs, inputs, e, processingTime, idOverride) {
		if (!e) throw new Error('viVibrating called without event object!')
		if (!outputs || !inputs) throw new Error('viVibrating called without inputs or outputs!')

		const toIngredient = (i) => {
			if (typeof i === 'string' && i.startsWith('#')) return { tag: i.replace('#', '') }
			if (typeof i === 'string') return { item: i }
			if (i.tag) return { tag: i.tag.replace(/^#/, '') }
			return { item: i.id ?? i.item }
		}

		const toResult = (o) => {
			if (typeof o === 'string') return { item: o, count: 1 }
			const result = { item: o.id ?? o.item, count: o.count ?? 1 }
			if (o.chance !== undefined && o.chance < 1) result.chance = o.chance
			return result
		}

		const json = {
		  type: 'vintageimprovements:vibrating',
		  ingredients: (Array.isArray(inputs) ? inputs : [inputs]).map(toIngredient),
		  results: (Array.isArray(outputs) ? outputs : [outputs]).map(toResult),
		  processingTime: processingTime ?? 300
		}

		e.custom(json).id(`${global.CE.modId}:vivibrating/${idOverride ?? global.CE.toId(outputs, inputs, "vibrating")}`)
	},
	
	ieMetalPress(output, input, mold, e, idOverride) {
  if (!e) throw new Error('ieMetalPress called without event object!')
  e.recipes.immersiveengineeringMetalPress(output, input, mold)
    .id(`${global.CE.modId}:iemetalpress/${idOverride ?? global.CE.toId(output, input, "iepressing")}`)
	},
	
	ieCrushing(output, input, e, secondaries, energy, idOverride) {
		if (!e) throw new Error('ieCrushing called without event object!')
		secondaries = secondaries || []
		energy = energy || 6000

		e.custom({
		  type: 'immersiveengineering:crusher',
		  input: Ingredient.of(input),
		  result: Item.of(output),
		  energy: energy,
		  secondaries: secondaries.map(s => ({
		    chance: s.chance,
		    output: Item.of(s.output).toJson()
		  }))
		}).id(`${global.CE.modId}:iecrushing/${idOverride ?? global.CE.toId(output, input, "iecrushing")}`)
	},
	
	ieAlloy(output, input0, input1, e, time, idOverride) {
		if (!e) throw new Error('ieAlloy called without event object!')
		if (!output || !input0 || !input1) throw new Error('ieAlloy called without inputs or output!')

		// Extracts {id, count} from any accepted input shape (string, Item.of() stack, etc.)
		var resolveIdCount = function (i) {
			if (typeof i === 'string') {
				return { id: i, count: 1 }
			}
			var id = i.id ? i.id : (i.item ? i.item : null)
			if (!id) id = String(i)
			var count = i.count ? i.count : 1
			return { id: id, count: count }
		}

		var toIngredient = function (i) {
			var resolved = resolveIdCount(i)
			var isTag = typeof resolved.id === 'string' && resolved.id.startsWith('#')
			return isTag
				? { tag: resolved.id.replace('#', '') }
				: { item: resolved.id }
		}

		var toResult = function (o) {
			var resolved = resolveIdCount(o)
			var isTag = typeof resolved.id === 'string' && resolved.id.startsWith('#')
			var base = isTag
				? { tag: resolved.id.replace('#', '') }
				: { item: resolved.id }

			if (resolved.count > 1) {
				return { base_ingredient: base, count: resolved.count }
			}
			return base
		}

		var json = {
			type: 'immersiveengineering:alloy',
			input0: toIngredient(input0),
			input1: toIngredient(input1),
			result: toResult(output),
			time: time ?? 200
		}

		e.custom(json).id(`${global.CE.modId}:iealloy/${idOverride ?? global.CE.toId(output, input0, "alloy")}`)
	},
	
	ieArcFurnace(output, input, e, secondaries, additives, time, energy, idOverride) {
		if (!e) throw new Error('ieArcFurnace called without event object!')
		additives = additives || []
		secondaries = secondaries || []
		var outputs = Array.isArray(output) ? output : [output]

		e.custom({
		  type: 'immersiveengineering:arc_furnace',
		  input: Ingredient.of(input),
		  results: outputs.map(function(o) { return Item.of(o) }),
		  additives: additives.map(a => Ingredient.of(a)),
		  secondaries: secondaries.map(function(s) {
		    return { chance: s.chance, output: Item.of(s.output) }
		  }),
		  time: time !== undefined ? time : 100,
		  energy: energy !== undefined ? energy : 25600
		}).id(`${global.CE.modId}:iearcfurnace/${idOverride !== undefined ? idOverride : global.CE.toId(output, input, "arcsmelting")}`)
	},
	
	ieSqueezing(fluidOutput, input, e, itemOutput, energy, idOverride) {
		if (!e) throw new Error('ieSqueezing called without event object!')
		energy = energy || 6000

		const toIngredient = (i) => (typeof i === 'string' && i.startsWith('#')) ? { tag: i.replace('#', '') } : { item: i.item ? i.item : i }
		const toFluidOutput = (f) => {
		  if (typeof f === 'string') return { fluid: f, amount: 1000 }
		  return { fluid: f.getId ? f.getId() : f.fluid, amount: f.getAmount ? f.getAmount() : (f.amount ?? 1000) }
		}
		const toItemOutput = (o) => {
		  if (!o) return undefined
		  if (typeof o === 'string') return { item: o, count: 1 }
		  return { item: o.id ? o.id : o.item, count: o.count ?? 1 }
		}
		const json = {
		  type: 'immersiveengineering:squeezer',
		  input: toIngredient(input),
		  fluid: toFluidOutput(fluidOutput),
		  energy: energy
		}
		const itemOut = toItemOutput(itemOutput)
		if (itemOut) json.output = itemOut
		e.custom(json).id(`${global.CE.modId}:iesqueezing/${idOverride ?? global.CE.toId(fluidOutput || itemOutput, input, "squeezing")}`)
	},
	
	ieBottling(results, fluid, amount, inputs, e, idOverride) {
		if (!e) throw new Error('ieBottling called without event object!')

		const toItem = (i) => (typeof i === 'string') ? { item: i } : { item: i.id ? i.id : i.item, count: i.count ?? undefined }
		const toIngredient = (i) => (typeof i === 'string' && i.startsWith('#')) ? { tag: i.replace('#', '') } : toItem(i)
		const toFluidJson = (f, amt) => {
			if (typeof f === 'string' && f.startsWith('#')) return { tag: f.replace('#', ''), amount: amt }
			if (typeof f === 'string') return { fluid: f, amount: amt }
			const json = {
				fluid: f.getId ? f.getId() : f.fluid,
				amount: f.getAmount ? f.getAmount() : (f.amount ?? amt)
			}
			const nbt = f.getTag ? f.getTag() : f.nbt
			if (nbt) json.nbt = nbt
			return json
		}
		const nameOf = (v) => String(
		  typeof v === 'string' ? v : (v.item ?? v.tag ?? v.fluid ?? v)
		).replace(/^.*:/, '').replace(/^#/, '')

		const resultsArr = (Array.isArray(results) ? results : [results]).map(toItem)
		const inputsArr = (Array.isArray(inputs) ? inputs : [inputs]).map(toIngredient)

		const json = {
		  type: 'immersiveengineering:bottling_machine',
		  fluid: toFluidJson(fluid, amount),
		  inputs: inputsArr,
		  results: resultsArr
		}

		e.custom(json).id(
			`${global.CE.modId}:iebottling/${idOverride ?? global.CE.toId(results, inputs, "iebottling")}`
		)
	},
	
	ieBlueprint(outputs, inputs, category, e, idOverride) {
	  if (!e) throw new Error('ieBlueprint called without event object!')
	  if (!outputs || !inputs) throw new Error('ieBlueprint called without inputs or outputs!')
	  if (!category) throw new Error('ieBlueprint called without a category!')
	  if (!Array.isArray(inputs)) throw new Error('ieBlueprint requires inputs to be an array!')
	  // Extracts {id, count} from any accepted input shape.
	  var resolveIdCount = function (i) {
	      if (typeof i === 'string') {
	          return { id: i, count: 1 }
	      }
	      // Item.of() stacks and similar wrapper objects
	      var id = i.id ? i.id : (i.item ? i.item : null)
	      if (!id) {
	          // last resort: treat the whole thing as a stringifiable id (covers raw strings passed oddly)
	          id = String(i)
	      }
	      var count = i.count ? i.count : 1
	      return { id: id, count: count }
	  }
	  var toIngredient = function (i) {
	      var resolved = resolveIdCount(i)
	      var isTag = typeof resolved.id === 'string' && resolved.id.startsWith('#')
	      var base = isTag
	          ? { tag: resolved.id.replace('#', '') }
	          : { item: resolved.id }

	      if (resolved.count > 1) {
	          return { base_ingredient: base, count: resolved.count }
	      }
	      return base
	  }
	  var toResult = function (o) {
	      if (typeof o === 'string') return { item: o }
	      var id = o.id ? o.id : (o.item ? o.item : o)
	      var result = { item: id }
	      if (o.count && o.count > 1) result.count = o.count
	      return result
	  }
	  var json = {
	      type: 'immersiveengineering:blueprint',
	      category: category,
	      inputs: inputs.map(toIngredient),
	      result: toResult(outputs)
	  }
	  e.custom(json).id(`${global.CE.modId}:blueprint/${category}/${idOverride ? idOverride : global.CE.toId(outputs, inputs, category)}`)
	},
	
	pcPressureChamber(outputs, inputs, pressure, e, idOverride) {
		  if (!e) throw new Error('pcPressureChamber called without event object!')
		  if (!outputs || !inputs)
				throw new Error('pcPressureChamber called without inputs or outputs!')
		  const toIngredient = (i) => {
					if (typeof i === 'string' && i.startsWith('#')) return { tag: i.replace('#', '') }
					if (typeof i === 'string') return { item: i }

					const id = i.id ? i.id : (i.item ? i.item : i)
					const count = i.count ? i.count : 1

					if (count > 1) {
						  return { type: 'pneumaticcraft:stacked_item', item: id, count: count }
					}
					return { item: id }
			}
		  const toResult = (o) => (typeof o === 'string') ? { item: o, count: 1 } : { item: (o.id ? o.id : o.item), count: (o.count ? o.count : 1) }
		  const json = {
		      type: 'pneumaticcraft:pressure_chamber',
		      inputs: (Array.isArray(inputs) ? inputs : [inputs]).map(toIngredient),
		      pressure: pressure,
		      results: (Array.isArray(outputs) ? outputs : [outputs]).map(toResult)
		  }
		  e.custom(json).id(`${global.CE.modId}:pcpressurechamber/${idOverride ?? global.CE.toId(outputs, inputs, "pressure")}`)
	},

	pcExplosionCrafting(output, input, lossRate, e, idOverride) {
		  if (!e) throw new Error('pcExplosionCrafting called without event object!')
		  const toIngredient = (i) => (typeof i === 'string' && i.startsWith('#')) ? { tag: i.replace('#', '') } : { item: i.item ? i.item : i }
		  const toResult = (o) => (typeof o === 'string') ? { item: o, count: 1 } : { item: (o.id ? o.id : o.item), count: (o.count ? o.count : 1) }
		  const json = {
		      type: 'pneumaticcraft:explosion_crafting',
		      input: toIngredient(input),
		      results: [toResult(output)],
		      loss_rate: lossRate
		  }
		  e.custom(json).id(`${global.CE.modId}:pcexplosioncrafting/${idOverride ?? global.CE.toId(output, input, "explosion")}`)
	},
	
	pcThermoPlant(itemOutput, fluidOutput, itemInput, fluidInput, e, options, idOverride) {
		if (!e) throw new Error('pcThermoPlant called without event object!')
		options = options || {}
		const toIngredient = (i) => {
			if (!i) return undefined
			if (typeof i === 'string') {
				return i.startsWith('#')
					? { tag: i.substring(1) }
					: { item: i }}
			if (i.tag) return { tag: i.tag.replace(/^#/, '') }
			if (i.item) return { item: i.item }
			return i}
		const toItemResult = (o) => {
			if (!o) return undefined
			if (typeof o === 'string')
				return { item: o, count: 1 }
			return {
				item: o.item ?? o.id,
				count: o.count ?? 1}}
		const toFluidIngredient = (f) => {
			if (!f) return undefined
			let json = {
				type: 'pneumaticcraft:fluid',
				amount: f.amount ?? 1000
			}
			if (typeof f === 'string') {
				if (f.startsWith('#'))
					json.tag = f.substring(1)
				else
					json.fluid = f
			} else if (f.tag) {
				json.tag = f.tag.replace(/^#/, '')
			} else {
				json.fluid = f.fluid ?? f.id}
			return json}
		const toFluidResult = (f) => {
			if (!f) return undefined
			if (typeof f === 'string')
				return {
					fluid: f,
					amount: 1000}
			return {
				fluid: f.fluid ?? f.id,
				amount: f.amount ?? 1000}}
		const json = {
			type: 'pneumaticcraft:thermo_plant',
			exothermic: options.exothermic ?? false}
		const itemIn = toIngredient(itemInput)
		if (itemIn) json.item_input = itemIn
		const fluidIn = toFluidIngredient(fluidInput)
		if (fluidIn) json.fluid_input = fluidIn
		const itemOut = toItemResult(itemOutput)
		if (itemOut) json.item_output = itemOut
		const fluidOut = toFluidResult(fluidOutput)
		if (fluidOut) json.fluid_output = fluidOut
		if (options.pressure !== undefined)
			json.pressure = options.pressure
		if (options.speed !== undefined)
			json.speed = options.speed
		if (options.airUseMultiplier !== undefined)
			json.air_use_multiplier = options.airUseMultiplier
		if (options.minTemp !== undefined || options.maxTemp !== undefined) {
			json.temperature = {}
			if (options.minTemp !== undefined)
				json.temperature.min_temp = options.minTemp
			if (options.maxTemp !== undefined)
				json.temperature.max_temp = options.maxTemp}
		e.custom(json).id(
			`${global.CE.modId}:pcthermoplant/${
				idOverride ??
				global.CE.toId(
					itemOutput ?? fluidOutput,
					itemInput ?? fluidInput, "thermoplant"
				)
			}`
		)
	},
}

console.log('Crafting Helper finished')
