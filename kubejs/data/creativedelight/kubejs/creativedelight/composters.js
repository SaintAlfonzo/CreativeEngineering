// requires: creativedelight
// Wood composters (TODO farming/composting #3, feature_writeup/composting.md 6; blocks: Creative Delight
// farm/composter/WoodComposters; moved from Creative Common in the wave 2 carve, plan_wave2_publish.md W2-9). Each species' slabs make that wood's composter. Vanilla's own minecraft:composter recipe
// (#minecraft:wooden_slabs, mixed species too) stays, so one species' slabs match both and Polymorph offers the choice
// (it lists by translation key, so creativedelight's wood composter before minecraft's; plan_wood_bucket_batch_1121 R20). Each wood
// composter crafts back into the vanilla one (shapeless, 1 to 1). Tags:
// #creativedelight:composters (block + item: vanilla's and the 12), axe mining.
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {
var i = global.item

// [composter, slab]; the modded composters and their slabs exist only with their mods (Architect's Palette, Quark)
var species = [
	[i.composter_spruce, i.slab_spruce],
	[i.composter_birch, i.slab_birch],
	[i.composter_dark_oak, i.slab_dark_oak],
	[i.composter_jungle, i.slab_jungle],
	[i.composter_acacia, i.slab_acacia],
	[i.composter_mangrove, i.slab_mangrove],
	[i.composter_crimson, i.slab_crimson],
	[i.composter_warped, i.slab_warped],
]
var loaded = species.slice()
if (global.has('architects_palette')) loaded.push([i.composter_twisted, i.slab_twisted])
if (global.has('quark')) loaded.push([i.composter_ancient, i.slab_ancient], [i.composter_blossom, i.slab_blossom], [i.composter_azalea, i.slab_azalea])
// only loaded composters: an absent mod's composter would make the NCH shapeless recipe and the tag entries fail
var composters = [i.composter].concat(loaded.map(s => s[0]))

ServerEvents.recipes(e => {
	var R = global.NCH.forPack('creativedelight')
	R.bulk('shaped', loaded.map(s => [s[0], ['S S', 'S S', 'SSS'], { S: s[1] }, e]))
	// any wood composter back to the vanilla one, 1 to 1 (plan_wood_bucket_batch_1121.md R17)
	R.bulk('shapeless', composters.slice(1).map(c => [i.composter, [c], e]))
})

ServerEvents.tags('item', e => {
	var t = global.tags
	t.add(e, i.t_composters, composters)
})

ServerEvents.tags('block', e => {
	var t = global.tags
	t.add(e, i.t_composters, composters)
	t.mineable(e, 'axe', null, composters.slice(1))
})
})()
