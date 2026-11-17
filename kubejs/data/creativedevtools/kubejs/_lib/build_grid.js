// requires: creativedevtools
// priority: 100
// Shared by the test recipe scripts (create/metal_test_recipes.js, create/wood_test_recipes.js): builds a roughly
// square grid pattern and keys object from a flat list of {letter, item} pairs. Columns and rows are sized by
// ceil(sqrt(n)); leftover cells stay blank (a space is an empty slot).
//   var grid = global.CDV.buildGrid(entries)   -> { pattern: ['ABC', 'DE '], keys: { A: ..., ... } }
global.CDV.buildGrid = function (entries) {
	var n = entries.length
	var cols = Math.ceil(Math.sqrt(n))
	var rows = Math.ceil(n / cols)

	var keys = {}
	var pattern = []
	var idx = 0

	for (var r = 0; r < rows; r++) {
		var row = ''
		for (var c = 0; c < cols; c++) {
			if (idx < n) {
				var entry = entries[idx]
				keys[entry.letter] = entry.item
				row += entry.letter
				idx++
			} else {
				row += ' '
			}
		}
		pattern.push(row)
	}

	return { pattern: pattern, keys: keys }
}
