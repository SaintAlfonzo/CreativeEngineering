// /dumpcompost  ->  writes kubejs/exported/compost/compost.json
//
//   original   the composter table as vanilla + mods registered it, before global.compost rebuilt it
//   now        the table after the rebuild, each item with its chance, source ("value" = listed in
//              compost_values.js, "rule" = a category in compost_rules.js) and category
//   excluded   items a value or rule (or the original table) would compost, blocked by an exclusion
//
// The framework is compost.js in patcherMod's jar (development/patcherMod/src/main/resources/kubejs_startup/creativeengineering/func/); tools/gen_compost_values.py reads this dump.

ServerEvents.commandRegistry(event => {
	var { commands: Commands } = event
	event.register(
		Commands.literal('dumpcompost')
			.requires(src => src.hasPermission(2))
			.executes(ctx => {
				var g = guardThrowable(() => {
					var r = global.compost.report()
					writeExportJson('kubejs/exported/compost/compost.json', r)
					return r
				})
				if (g.ok) {
					var r = g.value
					ctx.source.sendSuccess(Text.green(`Dumped compost table: ${Object.keys(r.now).length} compostable now, `
						+ `${Object.keys(r.original).length} before rebuild, ${Object.keys(r.excluded).length} excluded `
						+ `-> kubejs/exported/compost/compost.json`), false)
				} else {
					console.error('[dumpcompost] failed: ' + g.error)
					ctx.source.sendFailure(Text.red('dumpcompost failed - see log: ' + g.error))
				}
				return 1
			})
	)
})
