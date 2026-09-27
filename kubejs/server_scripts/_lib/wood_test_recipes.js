(function () {

var R = global.NCH
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

// Builds a roughly-square grid pattern + keys object from a flat list of {letter, item} pairs.
function buildGrid(entries) {
	var n = entries.length;
	var cols = Math.ceil(Math.sqrt(n));
	var rows = Math.ceil(n / cols);

	var keys = {};
	var pattern = [];
	var idx = 0;

	for (var r = 0; r < rows; r++) {
		var row = '';
		for (var c = 0; c < cols; c++) {
			if (idx < n) {
				var entry = entries[idx];
				keys[entry.letter] = entry.item;
				row += entry.letter;
				idx++;
			} else {
				row += ' ';
			}
		}
		pattern.push(row);
	}

	return { pattern: pattern, keys: keys };
}

var alphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';

ServerEvents.recipes(e => {
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
});

console.log('Hello! The wood test event has ended!')

})();
