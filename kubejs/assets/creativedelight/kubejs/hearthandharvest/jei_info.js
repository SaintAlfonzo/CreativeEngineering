// requires: creativedelight
// requires: jei
// requires: hearthandharvest
// requires: rusticdelight
// Hearth and Harvest JEI information pages: the sap chain is block interactions, not recipes (dishes/ingredients #41;
// SapCauldronBlock in the hearth member). Creative Delight QoL-6, switch [qol] jeiInfoPages.
JEIEvents.information(e => {
	if (!global.CD.qol('jeiInfoPages')) return
	var i = global.item

	var boiling = Text.of('Empty a Sap Bucket into a cauldron over fire or a lit block (campfire, furnace). It boils down in two steps of 24 seconds, then a Glass Bottle takes one Syrup and empties the cauldron.')
	e.addItem(i.syrup, [
		Text.of('Made from sap: a Tree Tapper on a tappable log fills with sap over time.'),
		boiling,
		Text.of('A heated Create mixer also turns 1000 mB of sap into a bottle of Syrup.'),
	])
	e.addItem(i.sap_bucket, [boiling])
})
