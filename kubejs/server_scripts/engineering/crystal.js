ServerEvents.recipes(e => {

	console.log('Hello! The crystal event has started!')

	var i = global.item
	var f = global.fluid
	var R = global.NCH

	// ############################## Amethyst ###########################

	//Amethyst
	//Budding amethyst sequenced assembly
	const incBuddingAmethyst = R.incomplete('budding_amethyst')
	R.createSequencedAssembly(Item.of(i.budding_amethyst, 1), i.amethyst_block, [
		e.recipes.create.deploying(incBuddingAmethyst, [incBuddingAmethyst, i.amethyst_dust]),
		e.recipes.create.filling(incBuddingAmethyst, [incBuddingAmethyst, Fluid.of(f.create_potion, 250, '{Bottle:"REGULAR",Potion:"minecraft:thick"}')])
	], ['budding_amethyst'], e, 'budding_amethyst_sequenced_assembly', 4)

	const incAmethystLens = R.incomplete('amethyst_lens')
	R.createSequencedAssembly(Item.of(i.amethyst_lens, 1), i.amethyst_block, [
		e.recipes.create.cutting(incAmethystLens, incAmethystLens),
		R.viPolishing(incAmethystLens, incAmethystLens, e, 2)
	], ['amethyst_lens'], e, 'amethyst_lens_sequenced_assembly', 5)

	// ############################## Diamond ###########################

	//Removals
	R.bulk('remove', [
        [{ id: 'create_ultimate_factory:compacting_coalblock' }, e],
        [{ id: 'pneumaticcraft:pressure_chamber/coal_to_diamond' }, e]
    ])

	R.createCompacting(i.diamond_shard, [Item.of(i.coal_block, 9), Fluid.of(f.lava, 250)], e, 'coal_compressing', 'superheated')
	R.pcPressureChamber(i.diamond, Item.of(i.coal_block, 9), 8, e)
	R.pcPressureChamber(i.diamond_shard, Item.of(i.coal_block, 4), 8, e)

	// ############################## Echo shard ###########################

	//shaped craft
	R.shaped(Item.of(i.echo_shard, 1), [' A ','ABA', ' A '], {A: i.sculk, B: i.amethyst_shard}, e)
	// Sequenced assembly
	const incEchoShard = R.incomplete('echo_shard')
	R.createSequencedAssembly(Item.of(i.echo_shard, 1), i.amethyst_shard, [
		e.recipes.create.deploying(incEchoShard, [incEchoShard, i.sculk]),
		e.recipes.create.deploying(incEchoShard, [incEchoShard, i.sculk]),
		e.recipes.create.pressing(incEchoShard, incEchoShard)
	], ['echo_shard'], e, 'echo_shard_sequenced_assembly', 1)

	console.log('The crystal event has finished!')
})