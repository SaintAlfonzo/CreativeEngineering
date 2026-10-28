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
// blocks): kubejs/data/creative_delight/kubejs/creativeengineering/cultivated_meat.js. Plain items, no .food().
// Textures and models: assets/creativeengineering/{textures,models}/item/.
//
// Culture blocks (<animal>_culture_block) are 9-culture storage blocks, same stats as prepared_flesh_block (the pack's
// misc_items.js): clay material, mud sound, hand-breakable. Blockstate, block model and item model are hand-authored
// under assets/creativeengineering/{blockstates,models/block,models/item}/<animal>_culture_block.json (cube_all,
// texture creativeengineering:block/culture_block/<animal>_culture_block.png). Storage block and hoe tags:
// kubejs/data/creative_delight/kubejs/creativeengineering/cultivated_meat_tags.js.

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
	e.create(i.culture_block_beef).displayName('Beef Culture Block')
		.material('clay').soundType('mud').hardness(0.8).resistance(0.8)
		.model('creativeengineering:block/beef_culture_block')
	e.create(i.culture_block_pork).displayName('Pork Culture Block')
		.material('clay').soundType('mud').hardness(0.8).resistance(0.8)
		.model('creativeengineering:block/pork_culture_block')
	e.create(i.culture_block_mutton).displayName('Mutton Culture Block')
		.material('clay').soundType('mud').hardness(0.8).resistance(0.8)
		.model('creativeengineering:block/mutton_culture_block')
	e.create(i.culture_block_poultry).displayName('Poultry Culture Block')
		.material('clay').soundType('mud').hardness(0.8).resistance(0.8)
		.model('creativeengineering:block/poultry_culture_block')
	e.create(i.culture_block_rabbit).displayName('Rabbit Culture Block')
		.material('clay').soundType('mud').hardness(0.8).resistance(0.8)
		.model('creativeengineering:block/rabbit_culture_block')
	e.create(i.culture_block_cod).displayName('Cod Culture Block')
		.material('clay').soundType('mud').hardness(0.8).resistance(0.8)
		.model('creativeengineering:block/cod_culture_block')
	e.create(i.culture_block_salmon).displayName('Salmon Culture Block')
		.material('clay').soundType('mud').hardness(0.8).resistance(0.8)
		.model('creativeengineering:block/salmon_culture_block')
})
