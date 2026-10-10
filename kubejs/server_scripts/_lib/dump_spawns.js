// /dumpspawns  ->  writes kubejs/exported/spawns.json
// every biome's mob spawners after all biome modifiers (the mods', then the pack's mob_spawns.json tuning),
// each mob's spawn placement, the mob categories, and what the tuning changed per biome.
// Report: python3 tools/gen_spawn_table.py -> feature_writeup/spawns_by_biome.md (feature_writeup/mob_spawns.md).
//
// The reading is patcherMod's worldgen/SpawnDump (Java): Rhino resolves WeightedRandomList.unwrap() to its own
// wrapper, so a script reads every spawn list as empty.

ServerEvents.commandRegistry(event => {
	const { commands: Commands } = event

	event.register(
		Commands.literal('dumpspawns')
			.requires(src => src.hasPermission(2))
			.executes(ctx => {
				var $SpawnDump
				try {
					$SpawnDump = Java.loadClass('com.SaintAlfonzo.creativeengineering.worldgen.SpawnDump')
				} catch (err) {
					ctx.source.sendFailure(Text.red('/dumpspawns needs patcherMod 1.0.5 with worldgen/SpawnDump (install the jar): ' + err))
					return 0
				}
				var line = String($SpawnDump.write(ctx.source.getServer()))
				ctx.source.sendSuccess(Text.green(`Dumped spawns: ${line}`), false)
				return 1
			})
	)
})
