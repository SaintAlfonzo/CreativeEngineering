// requires: vinery
// Vinery port recipe additions (feature_writeup/alcohol.md 10.5, alcohol/vineryport #17). Moved from the pack's
// server_scripts/cooking/alcohol.js 2026-11-01 (creative_brewing.md 4.5), rows unchanged. JEED providers for the wine
// effects: ../creativeengineering/jeed_providers.js. Recipe ids are creativebrewing:<type>/<suffix> (were creativeengineering:).
ServerEvents.recipes(e => {
if (!global.CB || !global.CB.on('vinery')) return
global.CB.recipes(() => {
var i = global.item
var R = global.NCH

//######################### Vinery banner #########################
// Advancements are gone (alcohol.md 10.2), so wine_somelier no longer hands it out. Any banner over a nether star,
// ringed by the 7 most complex wines (alcohol.md 10.5: scored by recipe depth, juice region and ingredients).
// Wines ignore their age NBT here, so any vintage works. Upstream's smithing recipe (banner + netherite ingot -> 2)
// still copies it.
R.shaped(i.vinery_standard, ['JMA', 'PBC', 'LNH'], {
	J: i.jellie_wine, M: i.bottle_mojang_noir, A: i.aegis_wine, P: i.apple_wine,
	C: i.cristel_wine, L: i.lilitu_wine, H: i.chenet_wine, B: i.any_banner, N: i.nether_star
}, e, 'vinery_standard')

//######################### Stained Glass (Vinery window) #########################
// Renamed "Stained Glass" / "Stained Glass Pane" in the port (alcohol/vineryport #17). Upstream makes each from glass or
// panes and dyes; this adds the vanilla pane shape, 6 blocks -> 16 panes.
R.shaped(Item.of(i.window, 16), ['WWW', 'WWW'], { W: i.window_block }, e, 'vinery_window_from_block')

})
})
