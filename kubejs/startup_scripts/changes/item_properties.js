// Item property overrides for other mods' items (ItemEvents.modification runs at startup; restart to apply).
// Calls are unprefixed (setMaxStackSize, not kjs$setMaxStackSize), see func/tune_tools.js.

ItemEvents.modification(e => {
	var i = global.item

	// Every raw egg in #forge:eggs stacks to 64 (vanilla egg and the parrot eggs were 16).
	// Listed by key: tags aren't loaded yet at startup.
	var eggs = [
		i.minecraft_egg, i.turtle_egg,
		i.crocodile_egg, i.emu_egg, i.terrapin_egg,
		i.egg_parrot_blue, i.egg_parrot_green, i.egg_parrot_grey, i.egg_parrot_red_blue, i.egg_parrot_yellow_blue,
	]
	eggs.forEach(id => {
		try {
			e.modify(id, item => item.setMaxStackSize(64))
		} catch (err) {
			console.warn('[item_properties] could not set stack size on ' + id + ': ' + err)
		}
	})
})
