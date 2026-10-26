// The pack's salt routes: raw rock salt -> 4 salt, heated basin water -> salt. Moved from the pack's
// kubejs/server_scripts/cooking/salt.js 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	const R = global.NCH
	var i = global.item
	var f = global.fluid

	global.CD.recipes(() => {

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

		R.shapeless(Item.of(i.salt_item, SALT_PER_RAW), [i.raw_rock_salt], e, 'raw_rock_salt_salt')
		R.createCrushing(Item.of(i.salt_item, SALT_PER_RAW), i.raw_rock_salt, e, 'raw_rock_salt_salt')
		R.createMilling(Item.of(i.salt_item, SALT_PER_RAW), i.raw_rock_salt, e, 'raw_rock_salt_salt')

		// Water -> salt, heated basin: 60 % (was 20 %). Cauldron evaporation is a loot table override,
		// kubejs/data/salt/loot_tables/cauldron_evaporation/salt_full.json (bonus salt 50 %, was 25 %).
		R.createMixing(Item.of(i.salt_item, 1).withChance(0.6), Fluid.of(f.water, 200), e, 'water_salt', 'heated')
	})
})
