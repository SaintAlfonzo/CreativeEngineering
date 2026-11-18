// requires: creativedelight
// The pack's salt routes: rock salt -> 4 salt, Rock Salt Block <-> 4 rock salt, heated basin water over a Rock Salt Block
// -> salt, the Large Salt Lamp, the Salt Melt. Salt
// is Vintage Delight's salt dust since the salt merge (food/seasoning #6, vintage_items.js); The Salt's salt only if
// the Vintage member is off.
ServerEvents.recipes(e => {
	if (!global.CD) return

	const R = global.NCH.forPack('creativedelight')
	var i = global.item
	var f = global.fluid
	var b = global.blocks

	global.CD.recipes(() => {

		var SALT = global.CD.on('vintagedelight') ? i.salt_dust : i.salt_item

		// ============================================================
		// Rock salt (the salt crystals, salt:raw_rock_salt) -> salt, 4x
		// ============================================================
		// Every route that turns rock salt into usable salt gives 4 salt. Salt Dust Block 9 <-> 1 and the water / cauldron
		// routes (not from crystals) are unchanged. The mortar craft comes from mortar_catalog.json (count 4 there).

		var SALT_PER_RAW = 4

		R.remove({ id: 'salt:salt_from_raw_rock_salt' }, e)
		R.remove({ id: 'create__the_salt:salt_create_crushing' }, e)
		R.remove({ id: 'create__the_salt:salt_create_milling' }, e)
		R.remove({ id: 'create__the_salt:salt_create_recipe' }, e)

		// Hand, hammer (cutting board and grid, NCH.hammering) and mortar give 4; crushing and milling 4 plus 1 at 50 %
		// (salt.md 6 S6).
		var MACHINE_SALT = [Item.of(SALT, SALT_PER_RAW), Item.of(SALT, 1).withChance(0.5)]
		R.shapeless(Item.of(SALT, SALT_PER_RAW), [i.raw_rock_salt], e, 'raw_rock_salt_salt')
		R.hammering(Item.of(SALT, SALT_PER_RAW), i.raw_rock_salt, e, 'raw_rock_salt_salt')
		R.createCrushing(MACHINE_SALT, i.raw_rock_salt, e, 'raw_rock_salt_salt')
		R.createMilling(MACHINE_SALT, i.raw_rock_salt, e, 'raw_rock_salt_salt')

		// Rock Salt Block <-> 4 rock salt (upstream 9; plan_post_scratch_1115 G2b): one block is one salt lick, worn in 4
		// stages, each stage refilled by 1 rock salt. The block id is block_dict's (no item_dict key yet).
		var ROCK_SALT_BLOCK = b.raw_rock_salt_block
		R.remove({ id: 'salt:raw_rock_salt' }, e)
		R.remove({ id: 'salt:raw_rock_salt_block' }, e)
		R.shaped(ROCK_SALT_BLOCK, ['RR', 'RR'], { R: i.raw_rock_salt }, e, 'rock_salt_block')
		R.shapeless(Item.of(i.raw_rock_salt, 4), [ROCK_SALT_BLOCK], e, 'rock_salt_from_block')

		// Water -> salt, heated basin over a Rock Salt Block (kept): 1000 mB -> 2 salt (G2c, G6-11; was water alone, so
		// water poured first into any heated basin turned to salt). Cauldron evaporation is a loot table override,
		// kubejs/data/salt/loot_tables/cauldron_evaporation/: full 8, two thirds 5, one third 3.
		R.createMixing([ROCK_SALT_BLOCK, Item.of(SALT, 2)], [ROCK_SALT_BLOCK, Fluid.of(f.water, 1000)], e,
			'rock_salt_water_salt', 'heated')

		// ============================================================
		// Large Salt Lamp (The Salt's lamp, renamed in namechanges.js)
		// ============================================================
		// 7 rock salt around glowstone over a wooden slab (G2d; was 7 Salt Dust Blocks and deepslate tiles).
		R.remove({ id: 'salt:salt_lamp' }, e)
		R.shaped(i.salt_lamp, ['SSS', 'SGS', 'SDS'], { S: i.raw_rock_salt, G: i.glowstone_block, D: i.wood_slab }, e,
			'large_salt_lamp')

		// ============================================================
		// Salt Melt (salt member, 64 uses; plan_post_scratch_1115 B6, salt.md 7): 2 salt + 2 raw rock salt
		// ============================================================
		R.shapeless(i.salt_melt, [SALT, SALT, i.raw_rock_salt, i.raw_rock_salt], e, 'salt_melt')
	})
})
