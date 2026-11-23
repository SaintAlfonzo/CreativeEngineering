// requires: creativecompat
// requires: botanypots
// Botany Pots growth (feature_writeup/botany_pots.md; plan_farming_cooking_batch.md A15 / C5, owner Q15 to Q17).
// Every pot crop grows BOTANY_GROWTH_MULT times slower, flowers (seed in #minecraft:flowers, wild crops excluded)
// BOTANY_FLOWER_MULT times slower. Soils are untouched, so their growthModifier still stacks on top. Pot fertilizers (bone meal is the
// only one in the pack) are removed. One pass over every loaded botanypots:crop recipe, so crops other mods (or
// future jars) add are covered too, and JEI shows the new times. The added
// Fertile Soil pot soil is creativecompat:<type>/fertile_soil.
global.BOTANY_GROWTH_MULT = 8
global.BOTANY_FLOWER_MULT = 2
// Better soils speed pot growth only, never pot yield (plan_crop_yields_wave2.md 8): Rich Soil and Rich Soil Farmland
// x1.10, Fertile Soil x1.20 (new pot soil, the mod has none for it). Plain farmland and dirt keep the mod's values.
global.BOTANY_SOIL_SPEED = { rich: 1.10, fertile: 1.20 }

ServerEvents.recipes(e => {
	var i = global.item
	var R = global.NCH.forPack('creativecompat')
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
	// Not plantable in pots (owner ruling, crops #59, plan_owner_rows_1120.md R20): glow berries, hanging roots and
	// every vine. Mixed Seeds and the Berry Good glow berry pips have no pot crop (botany_pot_crops.csv). No water plants
	// are added; Botany Pots' own ones stay.
	;['botanypots:minecraft/crop/glow_berry_bush', 'botanypots:minecraft/crop/hanging_roots', 'botanypots:minecraft/crop/vines',
		'botanypots:minecraft/crop/twisting_vines', 'botanypots:minecraft/crop/weeping_vines'].forEach(id => R.remove({ id: id }, e))

	if (!R.setRecipeNumber || !R.botanySoil) {
		console.error('[botany_pots] global.NCH.setRecipeNumber / botanySoil missing: Creative Common is older than this script; pot soils left at the mod\'s values')
		return
	}
	var sp = global.BOTANY_SOIL_SPEED
	var soils = R.setRecipeNumber({ id: 'botanypots:farmersdelight/soil/rich_soil' }, 'growthModifier', sp.rich, e) +
		R.setRecipeNumber({ id: 'botanypots:farmersdelight/soil/rich_soil_farmland' }, 'growthModifier', sp.rich, e)
	if (Platform.isLoaded('immersive_weathering')) {
		R.botanySoil(i.fertile_soil, ['dirt', 'farmland'], sp.fertile, e, 'fertile_soil')
		soils++
	}
	console.info('[botany_pots] ' + soils + ' soils set (Rich x' + sp.rich + ', Fertile x' + sp.fertile + ')')
	console.info('[botany_pots] growthTicks scaled on ' + changed + ' pot crops (' + flowers + ' flowers x' +
		global.BOTANY_FLOWER_MULT + ', the rest x' + global.BOTANY_GROWTH_MULT + '); pot fertilizers removed')
})
