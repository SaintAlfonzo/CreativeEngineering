// requires: creativeengineering
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

	// The animal-fat torch moved to Creative Delight 2026-11-06 (creativeengineering/animal_fat_torch.js there).

	console.log('Ash done')
})
