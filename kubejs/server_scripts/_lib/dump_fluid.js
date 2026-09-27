// /dumpfluidtags  ->  writes kubejs/exported/fluid_tags.json
// { "namespace:fluid": ["#forge:fluid/...", ...], ... }  — every fluid id, its tags (or [] if none)

ServerEvents.commandRegistry(event => {
	const { commands: Commands } = event

	event.register(
		Commands.literal('dumpfluids')
			.requires(src => src.hasPermission(2))
			.executes(ctx => {
				const n = dumpFluidTags()
				ctx.source.sendSuccess(Text.green(`Dumped ${n} fluids to kubejs/exported/fluids.json`), false)
				return 1
			})
	)
})

function dumpFluidTags() {
	const ForgeRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	const reg = ForgeRegistries.FLUIDS
	const tagMgr = reg.tags() // ITagManager<Fluid>; null before world load

	const byFluid = {}

	// seed every fluid id with an empty tag list
	reg.getKeys().forEach(rl => {
		byFluid[rl.toString()] = []
	})

	// fluid tag membership lives in Forge's tag manager, not the registry holder
	if (tagMgr) {
		var names = tagMgr.getTagNames().iterator()
		while (names.hasNext()) {
			var tagKey = names.next()
			var tag = '#' + tagKey.location().toString()
			var members = tagMgr.getTag(tagKey).iterator()
			while (members.hasNext()) {
				var fid = reg.getKey(members.next()).toString()
				if (byFluid[fid]) byFluid[fid].push(tag)
			}
		}
	}

	// sort tags per fluid, and sort fluids by id
	const sorted = {}
	Object.keys(byFluid).sort().forEach(fid => {
		sorted[fid] = byFluid[fid].sort()
	})

	writeExportJson('kubejs/exported/fluids.json', sorted)
	console.log(`[dumpfluids] wrote ${Object.keys(sorted).length} fluids`)
	return Object.keys(sorted).length
}
