// Reserved for Creative Cultivation: all cultivated-meat code (the sample syringe, the meat cultures, the culture blocks,
// their recipes, tags, lang and textures) is marked for a future own mod, Creative Cultivation, the way potion brewing
// and alcohol are marked for Creative Brewing (creative_delight.md 29.5). Until then it lives in Creative Delight's
// creativeengineering/ trees.
//
// Moved from the pack's startup_scripts/items/cultures.js 2026-10-27, ids unchanged (they come from the item dict).
// Creative Delight startup script, loaded from the jar after the pack's startup_scripts, so the dicts exist here.
// Restart to apply.
//
// Meat cultures: the raw lab-grown precursor for each cultivated-meat line. Recipes (samples, growth, cutting, culture
// blocks): kubejs/data/creativedelight/kubejs/creativeengineering/cultivated_meat.js. Plain items, no .food().
// Textures and models: assets/creativeengineering/{textures,models}/item/.
//
// Culture blocks (<animal>_culture_block) are 9-culture storage blocks, same stats as prepared_flesh_block (the pack's
// misc_items.js): clay material, mud sound, hand-breakable. Blockstate, block model and item model are hand-authored
// under assets/creativeengineering/{blockstates,models/block,models/item}/<animal>_culture_block.json (cube_all,
// texture creativeengineering:block/culture_block/<animal>_culture_block.png; all in this jar). The builders below set the
// same texture (textureAll), since KubeJS's generated block model wins over the jar's.
// Storage block and hoe tags:
// kubejs/data/creativedelight/kubejs/creativeengineering/cultivated_meat_tags.js.

StartupEvents.registry('item', e => {
	var i = global.item
	e.create(i.culture_beef).displayName('Beef Culture')
	e.create(i.culture_pork).displayName('Pork Culture')
	e.create(i.culture_mutton).displayName('Mutton Culture')
	e.create(i.culture_poultry).displayName('Poultry Culture')
	e.create(i.culture_rabbit).displayName('Rabbit Culture')
	e.create(i.culture_cod).displayName('Cod Culture')
	e.create(i.culture_salmon).displayName('Salmon Culture')
})

StartupEvents.registry('block', e => {
	var i = global.item
	// textureAll: KubeJS generates its own block model for every block it registers, and that generated model wins over
	// the jar's hand-authored one; without it the generated model asks for the flat block/<id> texture (missing texture).
	var cultureBlocks = [
		[i.culture_block_beef, 'Beef Culture Block'],
		[i.culture_block_pork, 'Pork Culture Block'],
		[i.culture_block_mutton, 'Mutton Culture Block'],
		[i.culture_block_poultry, 'Poultry Culture Block'],
		[i.culture_block_rabbit, 'Rabbit Culture Block'],
		[i.culture_block_cod, 'Cod Culture Block'],
		[i.culture_block_salmon, 'Salmon Culture Block'],
	]
	cultureBlocks.forEach(c => {
		e.create(c[0]).displayName(c[1])
			.material('clay').soundType('mud').hardness(0.8).resistance(0.8)
			.textureAll(c[0].replace(':', ':block/culture_block/'))
			.model(c[0].replace(':', ':block/'))
	})
})
