// requires: creativecompat
// Creative Compat. Item-physics behavior tags (Creative Compat ItemPhysicSplit). Runs on /reload; dropped items pick
// up membership changes on the next tick, no restart.
//
//   creativecompat:physics/floats      -> floats in water
//   creativecompat:physics/skims       -> rests on the water surface (light items; implies floats)
//   creativecompat:physics/sinks       -> force-sink (wins over floats and skims)
//   creativecompat:physics/fireproof   -> survives open fire, still burns in lava
//   creativecompat:physics/lavaproof   -> immune to BOTH open fire and lava (wins over fireproof); skims on lava
//                                         unless it is in physics/lava_sinks (heavy blocks)
//
// Every item is in exactly one material family, creativecompat:physics/family/<family>. The family tags are generated
// by tools/item_physics_audit.py (/cdevgen run item_physics_audit) into kubejs/data/creativecompat/tags/items/physics/
// (copied into the Creative Compat jar), together with physics/lava_sinks; a family decision that differs from the
// rules goes in tools/item_physics_overrides.csv. This script maps the families to behavior
// (feature_writeup/item_physics.md 9.2, owner rulings 9.6). An untagged item (a mod added since the last generator
// run) sinks and burns.

console.log('item physics tags starting')

// family -> the behavior tags it joins. Families not listed (technical, unclassified) keep the default: sink, burn.
const PHYSICS_FAMILIES = {
	'creativecompat:physics/floats': ['ice', 'wood', 'nether_wood', 'produce', 'food', 'organic', 'packed',
		'foliage', 'fibre', 'light_powder'],
	'creativecompat:physics/skims': ['foliage', 'fibre', 'light_powder'],                                  // R4
	'creativecompat:physics/fireproof': ['metal', 'machine', 'masonry', 'earth', 'crystal', 'container', 'powder'],
	'creativecompat:physics/lavaproof': ['refractory', 'nether_wood'],                                     // R1, R5
	// burn transforms, consumed by Creative Compat ItemBurnTransform (outcomes: startup script
	// creativecompat/item_physics.js): to_ash leaves supplementaries:ash (25% per item), explosive a blast of 1.0 with
	// no block damage
	'creativecompat:burn/to_ash': ['wood'],                                                                // R18 (D2)
	'creativecompat:burn/explosive': ['explosive'],                                                        // R19 (D3)
}

ServerEvents.tags('item', e => {
	const t = global.tags
	const i = global.item
	// Creative Delight's wood composters: kept here so they float even when the generator's dump predates them
	// (item-physics #8); tools/item_physics_overrides.csv puts the same ids in the same families
	if (global.has('creativedelight')) {
		t.add(e, 'creativecompat:physics/family/nether_wood', [i.composter_crimson, i.composter_warped])
		t.add(e, 'creativecompat:physics/family/wood', [
			i.composter_spruce, i.composter_birch, i.composter_dark_oak, i.composter_jungle, i.composter_acacia,
			i.composter_mangrove, i.composter_twisted, i.composter_ancient, i.composter_blossom, i.composter_azalea,
		])
	}
	Object.keys(PHYSICS_FAMILIES).forEach(target => {
		t.add(e, target, PHYSICS_FAMILIES[target].map(f => '#creativecompat:physics/family/' + f))
	})

	// force-sink: wins over any float source (the families, other scripts, ItemPhysic's own list). D4 keeps the heavy
	// powders here; the poisonous potato and the ender pearl and eye were owner picks before the families.
	t.add(e, 'creativecompat:physics/sinks', [
		i.gunpowder, i.redstone, i.glowstone, i.blaze_powder, i.blaze_rod,
		i.poisonous_potato, i.ender_pearl, i.ender_eye,
	])
})

console.log('item physics tags finished')
