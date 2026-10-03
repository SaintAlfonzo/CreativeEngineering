// /dumpall  ->  runs every dump command in turn, each with its default (no-argument) behaviour,
// through the real command dispatcher - so each one keeps its own defaults and chat feedback, and a
// dump that throws is reported and skipped rather than stopping the rest.
//
// Order: quick registry dumps first, world/worldgen dumps after, then the two slow ones
// (dumpclimate samples every multi_noise dimension; dumpservermods copies ~300 jars).
//
// /dumploot runs without "all" because its trade half writes the same villager_trades.json as
// /dumptrades. Adding a new /dump* command? Add it to DUMP_ALL_COMMANDS below.

;(function () {
	const DUMP_ALL_COMMANDS = [
		'dumpbiomes',
		'dumpfluids',
		'dumppotions',
		'dumpnames',
		'dumpitemsbymod',
		'dumptags',
		'dumprecipes',
		'dumpcraftingaudit',
		'dumpgear',
		'dumpstacks',
		'dumpfood',
		'dumpcompost',
		'dumpstructures',
		'dumpstructuregen',
		'dumploot',
		'dumptrades',
		'dumpclimate',
		'dumpservermods',   // patcherMod (Java): KubeJS can't copy files
	]

	function runAll(source) {
		const commands = source.getServer().getCommands()
		const started = Date.now()
		const failed = []
		DUMP_ALL_COMMANDS.forEach(cmd => {
			source.sendSuccess(Text.gray(`[dumpall] /${cmd}`), false)
			// Every dump returns >= 1 on success; the dispatcher returns 0 when a command throws an
			// Exception (after already sending the error to `source`) or isn't registered. A java.lang.Error
			// gets past the dispatcher and JS try/catch alike, hence guardThrowable (java_guard.js).
			const g = guardThrowable(() => commands.performPrefixedCommand(source, cmd))
			if (!g.ok) {
				console.error(`[dumpall] /${cmd} threw: ${g.error}`)
				source.sendFailure(Text.red(`[dumpall] /${cmd} threw: ${g.error}`))
			}
			if (!(g.ok && g.value > 0)) failed.push(cmd)
		})
		const secs = ((Date.now() - started) / 1000).toFixed(1)
		const done = DUMP_ALL_COMMANDS.length - failed.length
		if (failed.length) {
			source.sendFailure(Text.red(`[dumpall] ${done}/${DUMP_ALL_COMMANDS.length} dumps ok in ${secs}s; failed: ${failed.map(c => '/' + c).join(', ')}`))
		} else {
			source.sendSuccess(Text.green(`[dumpall] all ${done} dumps done in ${secs}s -> kubejs/exported/ (+ server_mods/)`), false)
		}
		console.info(`[dumpall] ${done}/${DUMP_ALL_COMMANDS.length} ok in ${secs}s` + (failed.length ? `, failed: ${failed.join(', ')}` : ''))
		return done
	}

	ServerEvents.commandRegistry(event => {
		const { commands: Commands } = event
		event.register(
			Commands.literal('dumpall')
				.requires(src => src.hasPermission(2))
				.executes(ctx => runAll(ctx.source))
		)
	})
})()
