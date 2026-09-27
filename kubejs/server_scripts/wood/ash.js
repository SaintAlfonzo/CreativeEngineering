ServerEvents.recipes(e => {
	console.log('Ash starting')

	var i = global.item
	const R = global.NCH

	i.any_animal_fat = '#forge:animal_fat';

    //Apply soot to sooty chimney scraping
	const SCRAPING = [
		['dirty_brick_chimney', 0.75],
		['dirty_cobblestone_chimney', 0.75],
		['dirty_mud_brick_chimney', 0.75],
		['dirty_stone_brick_chimney', 0.75],
		['dirty_copper_chimney', 0.5],
		['dirty_iron_chimney', 0.5],
		['dirty_terracotta_chimney', 0.5],
	]
	SCRAPING.forEach(([chimney, chance]) => {
		const id = `sootychimneys:soot_scraping/${chimney}`
		e.remove({ id: id })
		e.custom({
			type: 'sootychimneys:soot_scraping',
			chimney: { item: `sootychimneys:${chimney}` },
			results: [{ chance: chance, item: i.soot }],
			tool: { type: 'sootychimneys:tool_action', action: 'axe_scrape' },
		}).id(`creativeengineering:soot_scraping/${chimney}`)
	})

	R.remove({id: 'delightful:torch_from_animal_oil_bottle'}, e)
	R.shaped(Item.of(i.torch, 12),['f', 's'],{s: i.stick,f: i.any_animal_fat},e, 'torch_crafting_animal_fat');

	console.log('Ash done')
})
