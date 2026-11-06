// requires: cozys_improved_cats
// Ancient fish merge (feature_writeup/item_merges.md 2.12, cozy_cats.md 10). The swap itself runs from
// _core/item_merges.js; the 8 ancient fish smelting and blasting recipes would come out of it as copies of vanilla's
// cod and salmon cooking, so they go.
ServerEvents.recipes(e => {
	if (!global.CD) return
	;[
		'ancient_cooked_cod', 'ancient_cooked_cod_blasting',
		'ancient_cooked_cod_from_smelting_ancient_cod', 'ancient_cooked_cod_from_blasting_ancient_cod',
		'ancient_cooked_salmon', 'ancient_cooked_salmon_blasting',
		'ancient_cooked_salmon_from_smelting_ancient_salmon', 'ancient_cooked_salmon_from_blasting_ancient_salmon',
	].forEach(id => global.NCH.remove({ id: 'cozys-improved-cats:' + id }, e))
})
