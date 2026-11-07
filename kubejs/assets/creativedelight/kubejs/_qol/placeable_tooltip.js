// requires: creativedelight
// Placeable food tooltip lines: Creative Delight QoL-2 (creative_delight.md 8; moved from client_scripts/ 2026-10-25),
// switch [qol] placeableTooltips (off: no lines and the mods' own lines stay). From the tags in the pack's
// kubejs/data/creativedelight/kubejs/creativeengineering/placeable_tags.js:
//   "Placeable"                     #creativeengineering:placeable
//   "Placeable (while sneaking)"    ...and #creativeengineering:placeable/sneak
//   "Placeable on plate"            #creativeengineering:placeable_on_plate        (Display Delight Food Plate)
//   "Placeable on small plate"      #creativeengineering:placeable_on_small_plate  (Display Delight Small Food Plate)
//   "Placeable on a coaster"        #creativeengineering:placeable_on_coaster      (Brewin' and Chewin' coaster)
//
// Tags sync from the server on join, so the check runs per tooltip (addAdvancedToAll) rather than on a
// list fixed when this script loads. stack.hasTag(id) is KubeJS's one-argument kjs$hasTag; vanilla
// ItemStack.is(TagKey) is ambiguous in this Rhino build.
//
// These lines replace the mods' own placement lines, which are stripped by translation key:
// Display Delight's "Can be placed while sneaking" / "Can be plated on:" / "Food Plate" / "Small Food Plate"
// (it shows them for every tagged item, even ones it never links to a block) and Fruits Delight's
// "Shift right click to place down".
// Reference: feature_writeup/placeable_food.md, catalog feature_writeup/placeable_catalog.md

ItemEvents.tooltip(e => {
	var i = global.item
	var $TranslatableContents = Java.loadClass('net.minecraft.network.chat.contents.TranslatableContents')
	var tagOf = key => String(i[key]).replace(/^#/, '')
	var T_PLACEABLE = tagOf('t_placeable')
	var T_SNEAK = tagOf('t_placeable_sneak')
	var T_PLATE = tagOf('t_placeable_on_plate')
	var T_SMALL = tagOf('t_placeable_on_small_plate')
	var T_COASTER = tagOf('t_placeable_on_coaster')
	var STRIP = {
		'item.displaydelight.tooltip.displayable': true,
		'item.displaydelight.tooltip.can_placed_on': true,
		'block.displaydelight.food_plate': true,
		'block.displaydelight.small_food_plate': true,
		'fruitsdelight.tooltip.place': true,
	}

	e.addAdvancedToAll((item, advanced, text) => {
		if (!global.CD.qol('placeableTooltips')) return
		// index 0 is the name: the Food Plate item's own name uses one of the stripped keys
		for (var n = text.size() - 1; n >= 1; n--) {
			var contents = text.get(n).getContents()
			if (contents instanceof $TranslatableContents && STRIP[String(contents.getKey())]) text.remove(n)
		}
		if (item.hasTag(T_PLACEABLE)) {
			text.add(Text.literal(item.hasTag(T_SNEAK) ? 'Placeable (while sneaking)' : 'Placeable').gray())
		}
		if (item.hasTag(T_PLATE)) text.add(Text.literal('Placeable on plate').gray())
		if (item.hasTag(T_SMALL)) text.add(Text.literal('Placeable on small plate').gray())
		if (item.hasTag(T_COASTER)) text.add(Text.literal('Placeable on a coaster').gray())
	})
})
