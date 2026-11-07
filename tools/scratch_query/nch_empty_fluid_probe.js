// requires: creativecommon
// requires: create
// Scratch-only probe (queue SQ-SPFX-03): copy into a SCRATCH server's kubejs/server_scripts/ as zz_nch_probe.js, never
// into the pack. Builds two sequenced assemblies through global.NCH.createSequencedAssembly:
//   zz_probe_empty_fluid  a filling step with Fluid.of(<an id no mod registers>): must be skipped (NCH throws its
//                         absent error, the steps are dropped), so no "Error parsing recipe" and no create:kjs_ step
//   zz_probe_good         the same assembly with water: must load
// Log lines: "[zqnch] ..." in logs/latest.log and logs/kubejs/server.log.
ServerEvents.recipes(e => {
	var R = global.NCH
	var run = function (id, fluid) {
		try {
			R.withNamespace('creativecommon', () => {
				var inc = R.incomplete('horror_lasagna')
				R.createSequencedAssembly(Item.of('minecraft:bread', 1), 'minecraft:bowl', [
					e.recipes.create.deploying(inc, [inc, 'minecraft:wheat']),
					e.recipes.create.filling(inc, [inc, Fluid.of(fluid, 250)])
				], ['horror_lasagna'], e, id, 1)
			})
			console.info('[zqnch] ' + id + ': built')
		} catch (err) {
			console.info('[zqnch] ' + id + ': skipped (' + err + ')')
		}
	}
	run('zz_probe_empty_fluid', 'zzabsent:no_such_fluid')
	run('zz_probe_good', 'minecraft:water')
})
