// Breeding and pollination tags (feature_writeup/breeding_audit.md, owner decisions).
//
// Bees breed with items in #minecraft:flowers and pollinate blocks in the block tag #minecraft:flowers; the
// hummingbird eats the item tag. Rules here:
//  - no wild plant breeds bees or feeds hummingbirds (every item whose path starts with wild_ leaves the item tags;
//    that item step is Creative Compat's flower_tags.js);
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

	// Block tags that nest a flower tag for another reason (ground match, chalk) keep the removed entries, added to
	// them straight. The item side of this (the removal from the flower item tags, the floating, planter box and
	// flower box tags) is Creative Compat's kubejs/data/creativecompat/kubejs/tags/flower_tags.js.
	function keep(e, pairs) {
		pairs.forEach(function (p) { if (global.has(p[0]) && p[1].length) t.add(e, p[0], p[1]) })
	}

	ServerEvents.tags('item', e => {
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
