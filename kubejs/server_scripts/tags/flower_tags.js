// requires: creativecompat
// Pack script: moved out of Creative Compat (creative-compat-publish #13). The flower tags' item side. Wild plants, Botania special flowers, the flower crown and the azalea
// leaf piles leave #minecraft:flowers, #minecraft:small_flowers and #minecraft:tall_flowers (bees and hummingbirds,
// feature_writeup/breeding_audit.md). Tags that nest a flower tag for another reason (floating items, planter boxes,
// flower boxes) keep what leaves, added to them straight. The members are read and the removal made in this one
// handler, so the order is inside this file. The block side is in animal_tags.js (Creative Worldgen's ground match
// and chalk tags).

ServerEvents.tags('item', e => {
	var t = global.tags
	var i = global.item
	var FLOWER_TAGS = ['minecraft:flowers', 'minecraft:small_flowers', 'minecraft:tall_flowers']

	var notBees = []
	if (global.has(i.t_botania_special_flowers)) notBees.push(i.t_botania_special_flowers)
	if (global.has(i.flower_crown)) notBees.push(i.flower_crown, i.azalea_flower_pile, i.flowering_azalea_leaf_pile)
	// ids in a flower tag whose path starts with wild_ (resolved through nested tags); the item side removes every one
	var wild = t._tagMembers(e, 'minecraft:flowers').filter(function (id) { return /^[^:]+:wild_/.test(id) })
	// planter and flower boxes: only the wild plants that were small flowers
	var small = t._tagMembers(e, 'minecraft:small_flowers')
	var smallOut = wild.filter(function (id) { return small.indexOf(id) >= 0 })

	t.add(e, 'creativecompat:physics/floats', wild.concat(notBees))
	if (global.has('another_furniture') && smallOut.length) t.add(e, 'another_furniture:planter_box_placeables', smallOut)
	if (smallOut.length) t.add(e, 'creativecompat:flower_box_plants', smallOut)

	t.remove(e, FLOWER_TAGS, wild)
	t.remove(e, FLOWER_TAGS, notBees)
})
