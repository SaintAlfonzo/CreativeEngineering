// requires: creativedevtools
// requires: create
(function () {

var R = global.NCH.forPack('creativedevtools')
var i = global.item


console.log('Hello! The wood test event has started!')

// ############################## Wood Mechanical Crafting ##############################
var seqSpecies = [
	'oak', 'spruce', 'birch', 'dark_oak', 'jungle', 'acacia', 'mangrove',
	'crimson', 'warped', 'twisted', 'ancient', 'blossom', 'azalea'
];
var seqForms = [
	'slab', 'vslab', 'vplanks', 'stairs', 'fence', 'fencegate',
	'door', 'trapdoor', 'ladder', 'button', 'pressureplate', 'sign', 'railing'
];

// Grid pattern builder: global.CDV.buildGrid (_lib/build_grid.js)
var buildGrid = global.CDV.buildGrid;

var alphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';

ServerEvents.recipes(e => {
	if (!global.CDV || !global.CDV.feature('testRecipes')) return
	// creativedevtools:<type>/<suffix> recipe ids (creative_devtools.md 2)
	global.CDV.recipes(() => {
	for (var s = 0; s < seqSpecies.length; s++) {
		var species = seqSpecies[s];
		var planks = i['planks_' + species];

		if (!planks || planks === i.na) {
			console.log('[woodCrafting] SKIPPED ' + species + ' — no planks defined');
			continue;
		}

		var entries = [];
		for (var f = 0; f < seqForms.length; f++) {
			var field = seqForms[f] + '_' + species;
			var deployItem = i[field];

			if (deployItem === undefined || deployItem === null) {
				console.log('[woodCrafting] MISSING: global.item.' + field + ' is not defined — substituting i.none');
				deployItem = i.none;
			} else if (deployItem === i.na) {
				console.log('[woodCrafting] WARNING: ' + field + ' is i.na — placing gray decal into ' + species + ' grid');
			}

			entries.push({ letter: alphabet[f], item: deployItem });
		}
		// planks goes in the last available slot after all forms
		entries.push({ letter: alphabet[seqForms.length], item: planks });

		var grid = buildGrid(entries);

		R.createMechanicalCrafting(
			planks,
			grid.pattern,
			grid.keys,
			e,
			species + '_wood_mech_crafting'
		);
	}
})
});

console.log('Hello! The wood test event has ended!')

})();
