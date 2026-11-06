// requires: creativedevtools
// /dumpitemsbymod <mod1>[,<mod2>,...]   ->  writes kubejs/exported/items_by_mod/<mod>.json
// Also callable directly from other scripts: dumpItemsByMod(['immersiveengineering', 'create'])
// Pass no mods (empty array / omit the command arg) to dump every mod's items.

ServerEvents.commandRegistry(event => {
	const { commands: Commands, arguments: Arguments } = event

	event.register(
		Commands.literal('dumpitemsbymod')
			.requires(src => src.hasPermission(2))
			.executes(ctx => {
				const n = dumpItemsByMod([])
				ctx.source.sendSuccess(Text.green(`Dumped ${n} items (all mods) to kubejs/exported/items_by_mod/`), false)
				return 1
			})
			.then(
				Commands.argument('mods', Arguments.GREEDY_STRING.create(event))
					.executes(ctx => {
						const mods = Arguments.GREEDY_STRING.getResult(ctx, 'mods')
							.split(/[,\s]+/).map(s => s.trim().toLowerCase()).filter(Boolean)
						const n = dumpItemsByMod(mods)
						ctx.source.sendSuccess(Text.green(`Dumped ${n} items for [${mods.join(', ')}] to kubejs/exported/items_by_mod/`), false)
						return 1
					})
			)
	)
})

// mods: array of mod ids to include, e.g. ['immersiveengineering', 'create'].
// Empty array (or omitted) dumps every mod present in the item registry.
function dumpItemsByMod(mods) {
	const ForgeRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	const reg = ForgeRegistries.ITEMS

	const wanted = (mods || []).map(m => String(m).toLowerCase())
	const wantAll = wanted.length === 0

	const byMod = {}
	let total = 0

	const ids = []
	reg.getKeys().forEach(rl => ids.push(rl.toString()))
	ids.sort()

	ids.forEach(id => {
		if (id === 'minecraft:air') return
		const ns = id.substring(0, id.indexOf(':'))
		if (!wantAll && wanted.indexOf(ns) === -1) return

		;(byMod[ns] = byMod[ns] || []).push(id)
		total++
	})

	Object.keys(byMod).forEach(ns => {
		writeExportJson(`kubejs/exported/items_by_mod/${ns}.json`, { items: byMod[ns] })
	})
	console.log(`[dumpitemsbymod] wrote ${total} items across ${Object.keys(byMod).length} mod(s) to kubejs/exported/items_by_mod/`)
	return total
}
