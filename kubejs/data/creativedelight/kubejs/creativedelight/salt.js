// requires: creativedelight
// The pack's salt routes: raw rock salt -> 4 salt, heated basin water -> salt, the Large Salt Lamp. Moved from the
// pack's kubejs/server_scripts/cooking/salt.js 2026-10-25. Salt is Vintage Delight's salt dust since the salt merge
// (food/seasoning #6, vintage_items.js); The Salt's salt only if the Vintage member is off.
ServerEvents.recipes(e => {
	if (!global.CD) return

	const R = global.NCH
	var i = global.item
	var f = global.fluid

	global.CD.recipes(() => {

		var SALT = global.CD.on('vintagedelight') ? i.salt_dust : i.salt_item

		// ============================================================
		// Raw rock salt (the salt crystals) -> salt, 4x
		// ============================================================
		// Every route that turns raw rock salt into usable salt gives 4 salt. Salt block 9 <-> 1 and the water / cauldron
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

		// Water -> salt, heated basin: 1000 mB -> 2 (was 200 mB -> 60 %; salt.md 6 S2). Cauldron evaporation is a loot
		// table override, kubejs/data/salt/loot_tables/cauldron_evaporation/: full 8, two thirds 5, one third 3.
		R.createMixing(Item.of(SALT, 2), Fluid.of(f.water, 1000), e, 'water_salt', 'heated')

		// ============================================================
		// Large Salt Lamp (The Salt's lamp, renamed in namechanges.js)
		// ============================================================
		// Vintage's default lamp outline (glowstone between two salt blocks over deepslate tiles) without its salt on
		// top, every other slot a Salt Block: 7 salt blocks, 1 glowstone, 1 deepslate tiles. Was raw rock salt block over
		// a torch over a wooden slab.
		R.remove({ id: 'salt:salt_lamp' }, e)
		R.shaped(i.salt_lamp, ['SSS', 'SGS', 'SDS'], { S: i.salt_salt_block, G: i.glowstone_block, D: i.deepslate_tiles }, e,
			'large_salt_lamp')
	})
})
