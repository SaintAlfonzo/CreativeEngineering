(function () {

var R = global.NCH


console.log('Hello! The metal test event has started!')

// ############################## Metal Mechanical Crafting ##############################
var seqMetals = [
	'copper', 'iron', 'gold', 'zinc', 'aluminum', 'lead', 'nickel', 'silver',
	'uranium', 'rose_gold', 'cast_iron', 'brass', 'steel', 'constantan', 'electrum'
];
var seqForms = ['ore', 'ds_ore', 'raw', 'raw_block', 'crushed', 'crushed_block', 'dust', 'dust_block', 'nugget', 'ingot', 'block', 'plate', 'plate_block', 'rod', 'wire', 'wire_coil', 'coil_block', 'spring', 'small_spring', 'component'];
var i = global.item;

// Builds a roughly-square grid pattern + keys object from a flat list of {letter, item} pairs.
// cols/rows auto-sized via ceil(sqrt(n)); leftover cells left blank (space = empty slot).
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
	for (var m = 0; m < seqMetals.length; m++) {
		var metal = seqMetals[m];
		var ingot = i['ingot_' + metal];

		if (!ingot || ingot === i.na) {
			console.log('[mechCrafting] SKIPPED ' + metal + ' — no ingot defined');
			continue;
		}

		var entries = [];
		for (var f = 0; f < seqForms.length; f++) {
			var field = seqForms[f] + '_' + metal;
			var deployItem = i[field];

			if (deployItem === undefined || deployItem === null) {
				console.log('[mechCrafting] MISSING: global.item.' + field + ' is not defined — substituting i.none');
				deployItem = i.none;
			} else if (deployItem === i.na) {
				console.log('[mechCrafting] WARNING: ' + field + ' is i.na — placing gray decal into ' + metal + ' grid');
			}

			entries.push({ letter: alphabet[f], item: deployItem });
		}
		// ingot goes in the last available slot after all forms
		entries.push({ letter: alphabet[seqForms.length], item: ingot });

		var grid = buildGrid(entries);

		R.createMechanicalCrafting(
			ingot,
			grid.pattern,
			grid.keys,
			e,
			metal + '_mech_crafting'
		);
	}

	// ########## Netherite — custom recipe ##########
	var netheriteEntries = [
		{ letter: 'A', item: i.raw_netherite },
		{ letter: 'B', item: i.fragment_netherite },
		{ letter: 'C', item: i.nugget_netherite },
		{ letter: 'D', item: i.plate_netherite },
		{ letter: 'E', item: i.rod_netherite },
		{ letter: 'F', item: i.ingot_netherite }
	];

	var netheriteGrid = buildGrid(netheriteEntries);

	R.createMechanicalCrafting(
		i.ingot_netherite,
		netheriteGrid.pattern,
		netheriteGrid.keys,
		e,
		'netherite_mech_crafting'
	);
});

console.log('Hello! The metal test event has ended!')

})();
