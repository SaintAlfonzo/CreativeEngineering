// Plantable item tooltip line, one per item, from the family tags built in server_scripts/tags/plantabletags.js:
//   "Seed Item"       #creativeengineering:plantable/seed
//   "Plantable Crop"  #creativeengineering:plantable/crop
//   "Sapling"         #creativeengineering:plantable/sapling
//   "Fungi Sapling"   #creativeengineering:plantable/fungi_sapling
//   "Water Plant"     #creativeengineering:plantable/water
//   "Vine"            #creativeengineering:plantable/vine
//   "Vertical Crop"   #creativeengineering:plantable/vertical
//
// Tags sync from the server on join, so the check runs per tooltip (addAdvancedToAll), as placeable_tooltip.js does.
// stack.hasTag(id) is KubeJS's one-argument kjs$hasTag.
// Reference: feature_writeup/plantable.md

ItemEvents.tooltip(e => {
	var i = global.item
	var tagOf = (key, name) => String(i[key] || ('#creativeengineering:plantable/' + name)).replace(/^#/, '')
	var LINES = [
		[tagOf('t_plantable_seed', 'seed'), 'Seed Item'],
		[tagOf('t_plantable_crop', 'crop'), 'Plantable Crop'],
		[tagOf('t_plantable_sapling', 'sapling'), 'Sapling'],
		[tagOf('t_plantable_fungi_sapling', 'fungi_sapling'), 'Fungi Sapling'],
		[tagOf('t_plantable_water', 'water'), 'Water Plant'],
		[tagOf('t_plantable_vine', 'vine'), 'Vine'],
		[tagOf('t_plantable_vertical', 'vertical'), 'Vertical Crop'],
	]
	var T_ANY = String(i.t_plantable || 'creativeengineering:plantable').replace(/^#/, '')

	e.addAdvancedToAll((item, advanced, text) => {
		if (!item.hasTag(T_ANY)) return
		for (var n = 0; n < LINES.length; n++) {
			if (item.hasTag(LINES[n][0])) {
				text.add(Text.literal(LINES[n][1]).darkGreen())
				return
			}
		}
	})
})
