// requires: creativedelight
// Placeable food tooltip lines: Creative Delight QoL-2 (creative_delight.md 8; moved from client_scripts/ 2026-10-25),
// switch [qol] placeableTooltips (off: no lines and the mods' own lines stay). From the tags in the pack's
// kubejs/data/creativedelight/kubejs/creativedelight/placeable_tags.js:
//   "Placeable"                     #creativedelight:placeable
//   "Placeable (while sneaking)"    ...and #creativedelight:placeable/sneak
//   "Placeable on plate"            #creativedelight:placeable_on_plate        (Display Delight Food Plate)
//   "Placeable on small plate"      #creativedelight:placeable_on_small_plate  (Display Delight Small Food Plate)
//   "Placeable on a coaster"        #creativedelight:placeable_on_coaster      (Brewin' and Chewin' coaster)
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

// A tooltip line in a col role (global.tooltips, Creative Common's client _lib; read at event time)
var tipLine = (text, role, mods) => Text.of(global.tooltips.buildTooltipLine([text, global.tooltips.col[role], mods]))

ItemEvents.tooltip(e => {
	var i = global.item
	var tagOf = key => String(i[key]).replace(/^#/, '')
	var T_PLACEABLE = tagOf('t_placeable')
	var T_SNEAK = tagOf('t_placeable_sneak')
	var T_PLATE = tagOf('t_placeable_on_plate')
	var T_SMALL = tagOf('t_placeable_on_small_plate')
	var T_COASTER = tagOf('t_placeable_on_coaster')
	var STRIP = [
		'item.displaydelight.tooltip.displayable',
		'item.displaydelight.tooltip.can_placed_on',
		'block.displaydelight.food_plate',
		'block.displaydelight.small_food_plate',
		'fruitsdelight.tooltip.place',
	]

	e.addAdvancedToAll((item, advanced, text) => {
		if (!global.CD.qol('placeableTooltips')) return
		// index 0, the name, is kept: the Food Plate item's own name uses one of the stripped keys
		global.tooltips.stripTooltipKeys(text, STRIP)
		if (item.hasTag(T_PLACEABLE)) {
			text.add(tipLine(item.hasTag(T_SNEAK) ? 'Placeable (while sneaking)' : 'Placeable', 'text'))
		}
		if (item.hasTag(T_PLATE)) text.add(tipLine('Placeable on plate', 'text'))
		if (item.hasTag(T_SMALL)) text.add(tipLine('Placeable on small plate', 'text'))
		if (item.hasTag(T_COASTER)) text.add(tipLine('Placeable on a coaster', 'text'))
	})
})
