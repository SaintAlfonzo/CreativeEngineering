// Creative Engineering; moved from kubejs/server_scripts/wood/ash.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
ServerEvents.recipes(e => {
	console.log('Ash starting')

	var i = global.item
	const R = global.NCH

    //Apply soot to sooty chimney scraping
	const SCRAPING = [
		[i.dirty_brick_chimney, 0.75],
		[i.dirty_cobblestone_chimney, 0.75],
		[i.dirty_mud_brick_chimney, 0.75],
		[i.dirty_stone_brick_chimney, 0.75],
		[i.dirty_copper_chimney, 0.5],
		[i.dirty_iron_chimney, 0.5],
		[i.dirty_terracotta_chimney, 0.5],
	]
	SCRAPING.forEach(([chimney, chance]) => {
		const name = chimney.split(':')[1]
		R.remove({ id: `sootychimneys:soot_scraping/${name}` }, e)
		R.axeScraping(Item.of(i.soot).withChance(chance), chimney, e, name, { soot: true })
	})

	R.remove({id: 'delightful:torch_from_animal_oil_bottle'}, e)
	R.shaped(Item.of(i.torch, 12),['f', 's'],{s: i.stick,f: i.t_animal_fat},e, 'torch_crafting_animal_fat');

	console.log('Ash done')
})
