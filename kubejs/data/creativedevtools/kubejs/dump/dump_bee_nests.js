// requires: creativedevtools
// /dumpbeenests [reset]  ->  writes kubejs/exported/bee_nests.json
// trees, bee nests and chunks counted per biome as chunks generate (since the server started, or since the last
// /dumpbeenests reset), plus the bee_nests.json rules each biome got. Report: python3 tools/gen_bee_nests.py ->
// feature_writeup/bee_nests_by_biome.md section 2 (feature_writeup/bee_nests.md).
//
// Measure: /dumpbeenests reset, generate fresh terrain (fly, or Chunky in an unexplored area), then /dumpbeenests.
// Not in /dumpall: a session that generated nothing would overwrite a measurement with zeros.
// The counting is patcherMod's worldgen/BeeNests (mixins on TreeFeature, BeehiveDecorator, ChunkGenerator).

ServerEvents.commandRegistry(event => {
	const { commands: Commands } = event

	const $BeeNests = Java.loadClass('com.SaintAlfonzo.creativeengineering.worldgen.BeeNests')

	event.register(
		Commands.literal('dumpbeenests')
			.requires(src => src.hasPermission(2))
			.executes(ctx => {
				var line = String($BeeNests.write())
				ctx.source.sendSuccess(Text.green(`Dumped bee nests: ${line}`), false)
				return 1
			})
			.then(Commands.literal('reset').executes(ctx => {
				$BeeNests.reset()
				ctx.source.sendSuccess(Text.green('Bee nest counters reset: generate new chunks, then /dumpbeenests'), false)
				return 1
			}))
	)
})
