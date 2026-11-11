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

	ServerEvents.tags('item', e => {
		t.remove(e, FLOWER_TAGS, wildIn(e, 'minecraft:flowers', []))
		t.remove(e, FLOWER_TAGS, [i.t_botania_special_flowers, i.flower_crown, i.azalea_flower_pile, i.flowering_azalea_leaf_pile])

		t.add(e, 'blueprint:pig_food', [i.minecraft_carrot, i.minecraft_potato, i.minecraft_beetroot])
		t.add(e, 'blueprint:cat_food', [i.cod, i.salmon])
		t.add(e, 'blueprint:ocelot_food', [i.cod, i.salmon])
	})

	ServerEvents.tags('block', e => {
		t.remove(e, FLOWER_TAGS, wildIn(e, 'minecraft:flowers', WILD_KEEP))
	})
})()
