// requires: botanypots
// Botany Pots growth (feature_writeup/botany_pots.md; plan_farming_cooking_batch.md A15 / C5, owner Q15 to Q17).
// Every pot crop grows BOTANY_GROWTH_MULT times slower, flowers (seed in #minecraft:flowers, wild crops excluded)
// BOTANY_FLOWER_MULT times slower. Soils are untouched, so their growthModifier still stacks on top. Pot fertilizers (bone meal is the
// only one in the pack) are removed. One pass over every loaded botanypots:crop recipe, so crops other mods (or
// future jars) add are covered too, and JEI shows the new times.
global.BOTANY_GROWTH_MULT = 8
global.BOTANY_FLOWER_MULT = 2

ServerEvents.recipes(e => {
	var i = global.item
	var R = global.NCH
	if (!R.scaleRecipeInt) {
		console.error('[botany_pots] global.NCH.scaleRecipeInt missing: Creative Common is older than this script; pot growth left at the mod\'s values')
		return
	}

	var flowerIds = {}
	Ingredient.of(i.t_flowers).itemIds.forEach(id => { flowerIds[String(id)] = true })
	// wild crops (wild tomatoes, wild coffee...) sit in #minecraft:flowers too, but drop crops: crop speed
	if (i.t_wild_crops) Ingredient.of(i.t_wild_crops).itemIds.forEach(id => { delete flowerIds[String(id)] })

	// true when the crop's seed (item, tag, or a list of either) names a flower
	var isFlower = function (seed) {
		if (!seed) return false
		if (seed.isJsonArray()) {
			var arr = seed.getAsJsonArray()
			for (var n = 0; n < arr.size(); n++) if (isFlower(arr.get(n))) return true
			return false
		}
		if (!seed.isJsonObject()) return false
		var o = seed.getAsJsonObject()
		if (o.has('item')) return flowerIds[String(o.get('item').getAsString())] === true
		if (o.has('tag')) {
			var hit = false
			Ingredient.of('#' + o.get('tag').getAsString()).itemIds.forEach(id => { if (flowerIds[String(id)]) hit = true })
			return hit
		}
		return false
	}

	var flowers = 0
	var changed = R.scaleRecipeInt({ type: 'botanypots:crop' }, 'growthTicks', json => {
		if (isFlower(json.get('seed'))) { flowers++; return global.BOTANY_FLOWER_MULT }
		return global.BOTANY_GROWTH_MULT
	}, e)
	R.remove({ type: 'botanypots:fertilizer' }, e)
	console.info('[botany_pots] growthTicks scaled on ' + changed + ' pot crops (' + flowers + ' flowers x' +
		global.BOTANY_FLOWER_MULT + ', the rest x' + global.BOTANY_GROWTH_MULT + '); pot fertilizers removed')
})
