// Scratch-only probe (never in the pack): copy into a scratch server's kubejs/server_scripts as zz_nch_guard_probe.js.
// Checks the global.NCH.remove guard (2026-11-06, TODO creativedelight/framework #15): filters holding undefined or
// null must be refused with "remove refused: <path>" and remove nothing; a normal filter still removes. Output lines
// start with "[nchguard]"; the KubeJS "Added/removed" summary line is the count check.
ServerEvents.recipes(e => {
	if (!global.NCH) return console.error('[nchguard] global.NCH missing')
	var R = global.NCH
	var cases = [
		['top-level undefined', { output: undefined }],
		['null value', { input: null }],
		['nested in not', { output: 'minecraft:bread', not: [{ id: undefined }] }],
		['undefined filter', undefined],
	]
	cases.forEach(c => {
		R.remove(c[1], e)
		console.info('[nchguard] case done: ' + c[0])
	})
	R.remove({ id: 'minecraft:bread' }, e)
	console.info('[nchguard] normal remove of minecraft:bread done')
})
