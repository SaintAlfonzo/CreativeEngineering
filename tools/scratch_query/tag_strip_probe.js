// Scratch-only probe (never in the pack): copy into a scratch server's kubejs/server_scripts as zz_tag_strip_probe.js.
// Why does a removed item keep its tags (r1106cbb SQ-CBB-05: miners_delight:tentacles on the chain)? Logs what
// global.tags._ents decides for the ids in TAGSTRIP_IDS, and whether removeAllTagsFrom took them out. Lines start "[tagstrip]".
// priority: -20000
var TAGSTRIP_IDS = ['miners_delight:tentacles', 'miners_delight:baked_tentacles']
ServerEvents.tags('item', e => {
	var T = global.tags
	console.info('[tagstrip] event ' + e.getClass().getSimpleName() + ' ids=' + e.getElementIds().size())
	var all = global.removedItems
	var ents = T._ents(e, all).map(String)
	var undef = 0; all.forEach(x => { if (x == null) undef++ })
	console.info('[tagstrip] removedItems=' + all.length + ' undefined=' + undef + ' ents=' + ents.length
		+ ' tentaclesAt=' + all.map(String).indexOf('miners_delight:tentacles') + ' inEnts=' + ents.indexOf('miners_delight:tentacles'))
	TAGSTRIP_IDS.forEach(id => {
		var inRemoved = global.removedItems.some(x => String(x) == id)
		console.info('[tagstrip] ' + id + ' inRemoved=' + inRemoved + ' hasMod=' + global.has.mod(global.has.namespace(id))
			+ ' inRegistry=' + T._inRegistry(e, id) + ' ents=' + JSON.stringify(T._ents(e, [id]).map(String)))
		try { e.removeAllTagsFrom(id); console.info('[tagstrip] ' + id + ' removeAllTagsFrom ok') } catch (err) { console.error('[tagstrip] ' + id + ' ' + err) }
		console.info('[tagstrip] ' + id + ' in forge:raw_fishes after: ' + T._tagMembers(e, 'forge:raw_fishes').indexOf(id))
	})
})
