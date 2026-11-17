// requires: creativeengineering
// Creative Engineering.
ServerEvents.recipes(e => {
	console.log('Ash starting')

	var i = global.item
	const R = global.NCH.forPack('creativeengineering')

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


	console.log('Ash done')
})
