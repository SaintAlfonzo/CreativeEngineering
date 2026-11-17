// Breeding and pollination tags (feature_writeup/breeding_audit.md, owner decisions 2026-11-10).
//
// Bees breed with items in #minecraft:flowers and pollinate blocks in the block tag #minecraft:flowers; the
// hummingbird eats the item tag. Rules here:
//  - no wild plant breeds bees or feeds hummingbirds (every item whose path starts with wild_ leaves the item tags);
//  - only wild onions, carrots and potatoes stay pollinable (block tag); every other wild plant block leaves it;
//  - Botania special flowers (functional flowers, chibis) do not breed bees; Botania's own mixin still lets bees
//    pollinate them; shiny flowers were never in a tag and stay out;
//  - the flower crown and the azalea leaf / flower piles (Immersive Weathering) are not bee food.
// Pig, cat and ocelot: Blueprint's food tags carry the vanilla tables (Blueprint merges them with vanilla).
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {
	var t = global.tags
	var i = global.item

	var WILD_KEEP = [i.wild_onions, i.wild_carrots, i.wild_potatoes]
	var FLOWER_TAGS = ['minecraft:flowers', 'minecraft:small_flowers', 'minecraft:tall_flowers']

	// ids in a flower tag whose path starts with wild_ (resolved through nested tags)
	function wildIn(e, tag, keep) {
		return t._tagMembers(e, tag).filter(function (id) {
			return /^[^:]+:wild_/.test(id) && keep.indexOf(id) < 0
		})
	}

	// mobs/bees #4 (plan_small_fixes_1116.md P7): tags that nest a flower tag for another reason (plants, floating items,
	// planter boxes) keep the removed entries, added to them straight. Computed here, before the removal, because the
	// jar scripts that own those tags cannot see what this script removed.
	function members(e, tag, ids) {
		var all = t._tagMembers(e, tag)
		return ids.filter(function (id) { return all.indexOf(id) >= 0 })
	}
	function keep(e, pairs) {
		pairs.forEach(function (p) { if (global.has(p[0]) && p[1].length) t.add(e, p[0], p[1]) })
	}

	ServerEvents.tags('item', e => {
		var notBees = [i.t_botania_special_flowers, i.flower_crown, i.azalea_flower_pile, i.flowering_azalea_leaf_pile]
		var wild = wildIn(e, 'minecraft:flowers', [])
		// planter boxes: only the wild plants that were small flowers (no Botania functional flowers, crowns or piles)
		var smallOut = members(e, 'minecraft:small_flowers', wild)
		keep(e, [
			['creativecompat:physics/floats', wild.concat(notBees)],
			['another_furniture:planter_box_placeables', smallOut],
			['creativecompat:flower_box_plants', smallOut],
		])
		t.remove(e, FLOWER_TAGS, wild)
		t.remove(e, FLOWER_TAGS, notBees)

		t.add(e, 'blueprint:pig_food', [i.minecraft_carrot, i.minecraft_potato, i.minecraft_beetroot])
		t.add(e, 'blueprint:cat_food', [i.cod, i.salmon])
		t.add(e, 'blueprint:ocelot_food', [i.cod, i.salmon])
	})

	ServerEvents.tags('block', e => {
		var wild = wildIn(e, 'minecraft:flowers', WILD_KEEP)
		// template_trim/plants nests ground_match/plants
		keep(e, [['creativeworldgen:ground_match/plants', wild], ['chalk:chalk_cannot_draw_on', wild]])
		t.remove(e, FLOWER_TAGS, wild)
	})
})()
