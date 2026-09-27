ServerEvents.recipes(e => {
console.log('potion bottling starting')

// ── Remove IE's own potion bottling ───────────────────────────────────────────
// IE generates one bottling recipe per potion (its immersiveengineering:potion fluid ->
// potion item, regular only) and packs them into a single generated_list container recipe.
// Removing the container drops every expanded sub-recipe (they aren't individual RecipeManager
// entries, so this is the only way to remove them).
global.NCH.remove({ id: 'immersiveengineering:potion_bottling_list' }, e)

// ── Bottle Create's potion fluid ─────────────────────────────────────────────
// create:potion (in #forge:fluid/potion) + glass bottle -> the matching potion / splash
// potion / lingering potion item, one concrete recipe per potion x bottle type so each
// shows correctly in JEI. The fluid nbt is subset-matched by patcherMod's FluidTagInputMixin,
// so extra keys on the fluid stack (CustomPotionColor, ...) don't break the match.
var i = global.item
var R = global.NCH
var POTIONS = Java.loadClass('net.minecraft.core.Registry').POTION
var BOTTLES = [
	['REGULAR',   'minecraft:potion'],
	['SPLASH',    'minecraft:splash_potion'],
	['LINGERING', 'minecraft:lingering_potion']
]

// ── Fill / bottle / drain the CE potion buckets ──────────────────────────────
// creativeengineering:{potion,splash_potion,lingering_potion}_bucket carry their contents as
// plain {Potion:"..."} NBT (no fluid-handler capability, see potion_buckets.js), so — same as
// the glass-bottle recipes above — each container needs one concrete recipe per potion x
// bucket type rather than a generic capability-based fill/drain. One bucket = 1000 mB, vs
// 250 mB for a bottle. Create's actual reverse-of-filling type is "create:emptying" (there is
// no "create:draining" type in this Create version).
var BUCKETS = [
	['REGULAR',   i.ce_potion_bucket],
	['SPLASH',    i.ce_splash_potion_bucket],
	['LINGERING', i.ce_lingering_potion_bucket]
]

function bucketRecipes(pid, idBase) {
	BUCKETS.forEach(function (bt, idx) {
		var bottle = bt[0], bucketItem = bt[1]
		var bottleItem = BOTTLES[idx][1] // BUCKETS/BOTTLES are parallel arrays, same REGULAR/SPLASH/LINGERING order
		var fluidNbt = '{Bottle:"' + bottle + '",Potion:"' + pid + '"}'
		var bucketNbt = '{Potion:"' + pid + '"}'
		var idSuffix = idBase + '_' + bottle.toLowerCase()

		// Shapeless craft: empty bucket + 4 potion bottles -> potion bucket. The bottle
		// ingredient is matched by partial NBT (same idiom as the blueprint recipes in
		// engineering/blueprints.js: {type:'forge:partial_nbt', item, nbt}) so only bottles of
		// THIS potion count - not just any bottle of this tier's item - actually match. The 4
		// bottles leave glass bottles behind automatically via the containerItem set on
		// minecraft:potion/splash_potion/lingering_potion below - no separate result slot needed
		// (vanilla shapeless only supports one). The bucket itself is consumed outright (its own
		// containerItem is minecraft:bucket, for the REVERSE recipe below, not this one).
		var bottleIngredient = { type: 'forge:partial_nbt', item: bottleItem, nbt: bucketNbt }
		R.shapeless(
			Item.of(bucketItem, bucketNbt),
			['minecraft:bucket', bottleIngredient, bottleIngredient, bottleIngredient, bottleIngredient],
			e,
			'potion_bucket_fill/' + idSuffix
		)

		// Reverse: filled potion bucket -> 4 potion bottles. The bucket leaves a plain
		// minecraft:bucket behind via its own containerItem (startup_scripts/items/potion_buckets.js).
		R.shapeless(
			Item.of(bottleItem, 4, bucketNbt),
			[{ type: 'forge:partial_nbt', item: bucketItem, nbt: bucketNbt }, Item.of(i.glass_bottle, 4)],
			e,
			'potion_bucket_drain/' + idSuffix
		)

		// Create filling: empty bucket + create:potion fluid -> filled potion bucket
		e.custom({
			type: 'create:filling',
			ingredients: [
				{ item: 'minecraft:bucket' },
				{ amount: 1000, fluid: 'create:potion', nbt: fluidNbt }
			],
			results: [{ item: bucketItem, nbt: bucketNbt }]
		}).id('creativeengineering:create_filling/potion_bucket/' + idSuffix)

		// IE bottling machine: same fill, on the bottling machine instead of the Spout
		e.custom({
			type: 'immersiveengineering:bottling_machine',
			fluid: { tag: 'forge:fluid/potion', amount: 1000, nbt: fluidNbt },
			inputs: [{ item: 'minecraft:bucket' }],
			results: [{ item: bucketItem, nbt: bucketNbt }]
		}).id('creativeengineering:bottling/potion_bucket/' + idSuffix)

		// Create emptying ("draining"): filled potion bucket -> empty bucket + create:potion fluid
		e.custom({
			type: 'create:emptying',
			ingredients: [{ item: bucketItem, nbt: bucketNbt }],
			results: [
				{ item: 'minecraft:bucket' },
				{ amount: 1000, fluid: 'create:potion', nbt: fluidNbt }
			]
		}).id('creativeengineering:create_emptying/potion_bucket/' + idSuffix)

		n += 5
	})
}

var n = 0
var it = POTIONS.keySet().iterator()
while (it.hasNext()) {
	var pid = it.next().toString()
	if (pid === 'minecraft:empty') continue
	var idBase = pid.split(':').join('_').split('/').join('_')

	BOTTLES.forEach(function (bt) {
		var bottle = bt[0], outItem = bt[1]
		e.custom({
			type: 'immersiveengineering:bottling_machine',
			fluid: { tag: 'forge:fluid/potion', amount: 250, nbt: '{Bottle:"' + bottle + '",Potion:"' + pid + '"}' },
			inputs: [{ item: 'minecraft:glass_bottle' }],
			results: [{ item: outItem, nbt: '{Potion:"' + pid + '"}' }]
		}).id('creativeengineering:bottling/create_potion/' + idBase + '_' + bottle.toLowerCase())
		n++
	})

	bucketRecipes(pid, idBase)
}

console.log('potion bottling done (' + n + ' recipes)')
})
