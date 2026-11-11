// requires: creativedevtools
// /dumppaintings  ->  writes kubejs/exported/paintings.json
//
// Every registered painting variant (vanilla + mods), with its size in pixels (16 per block) and
// whether it is in #minecraft:placeable (the random-placement / Cycle Paintings pool; the old
// earth/wind/fire/water are registered but not placeable).
// tools/gen_immersive_paintings.py reads this to copy every variant into Immersive Paintings'
// Datapacks page (kubejs/data/creativecompat/paintings/).

ServerEvents.commandRegistry(event => {
	const { commands: Commands } = event
	const $PaintRegistry = Java.loadClass('net.minecraft.core.Registry')
	const $PaintingVariantTags = Java.loadClass('net.minecraft.tags.PaintingVariantTags')

	event.register(
		Commands.literal('dumppaintings')
			.requires(src => src.hasPermission(2))
			.executes(ctx => {
				var g = guardThrowable(() => {
					var reg = ctx.source.getServer().registryAccess().registryOrThrow($PaintRegistry.PAINTING_VARIANT_REGISTRY)
					var placeable = {}
					reg.getTagOrEmpty($PaintingVariantTags.PLACEABLE).forEach(h => {
						h.unwrapKey().ifPresent(k => { placeable[String(k.location())] = true })
					})
					var paintings = {}
					reg.entrySet().forEach(e => {
						var paintId = String(e.getKey().location())
						var v = e.getValue()
						paintings[paintId] = { width: v.getWidth(), height: v.getHeight(), placeable: !!placeable[paintId] }
					})
					var out = { count: Object.keys(paintings).length, paintings: paintings }
					writeExportJson('kubejs/exported/paintings.json', out)
					return out
				})
				if (g.ok) {
					ctx.source.sendSuccess(Text.green(`Dumped ${g.value.count} painting variants -> kubejs/exported/paintings.json`), false)
				} else {
					console.error('[dumppaintings] failed: ' + g.error)
					ctx.source.sendFailure(Text.red('dumppaintings failed - see log: ' + g.error))
				}
				return 1
			})
	)
})
