ServerEvents.recipes(e => {

console.log('Hello! The metal base event has started!')
console.log('EVENT:', e)

const R = global.NCH
var f = global.fluid
var i = global.item;

// ============================================================
// Bulk removals
// ============================================================
R.bulk('remove', [
	[{id:i.billet_industrial_iron}, e],
	[{id:i.billet_cast_iron}, e],
	[{id:'projectred_core:red_iron_comp'}, e],
	[{id:i.red_electrum_ingot}, e],
	[{id:i.rusty_iron_ingot}, e],
	[{id:'dustrial_decor:rusty_iron_ingot_from_block'}, e],
	[{id:'dustrial_decor:rusty_iron_ingot_from_nugget'}, e],
	[{id:i.rusty_iron_nugget}, e],
	[{id:'dustrial_decor:rusty_iron_nugget_from_ingot'}, e],
	[{id:i.rusty_iron_door}, e],
	[{id:i.rusty_iron_trapdoor}, e],
	[{id:i.rusty_iron_block}, e],
	[{id:i.rusty_sheet_metal}, e],
	[{id:'dustrial_decor:ingot_from_rusty_sheet_metal'}, e],
	[{id:i.rusty_sheet_metal_door}, e],
	[{id:i.rusty_sheet_metal_trapdoor}, e],
	[{id:i.rusty_sheet_metal_treading}, e],
	[{id:i.rusty_sheet_metal_walling}, e],
	[{id:i.rusty_sheet_metal_siding}, e],
	[{id:i.rusty_sheet_metal_paneling}, e],
	[{id:i.rusty_sheet_metal_plating}, e],
]);

// ============================================================
// Bulk input replacements
// ============================================================
R.bulk('replaceInput', [
	[{ mod: 'dustrial_decor' }, i.billet_industrial_iron, i.block_industrial_iron, e],
	[{ mod: 'dustrial_decor' }, i.billet_cast_iron, i.ingot_cast_iron, e],
]);

// ============================================================
// Red alloy -> red electrum, higher efficiency mixing
// (removes for this pair already covered by the bulk remove above)
// ============================================================
R.shapeless(Item.of(i.red_electrum_ingot, 1), [Item.of(i.ingot_electrum, 1), Item.of(i.redstone, 8)], e)
R.createMixing(Item.of(i.red_electrum_ingot, 1), [Item.of(i.ingot_electrum, 1), Item.of(i.redstone, 4)], e, undefined, "heated")
R.bulk('ieArcFurnace', [
	[i.red_electrum_ingot, i.ingot_electrum, e, undefined, undefined, [i.redstone, i.redstone, i.redstone]],
]);

// ============================================================
// Rust -> clean washing recipes (bulk createSplashing)
// ============================================================
R.bulk('createSplashing', [
	[i.rusty_iron_door, i.iron_door, e],
	[i.rusty_iron_trapdoor, i.iron_trapdoor, e],
	[i.rusty_iron_block, i.block_iron, e],
	[i.rusty_sheet_metal_door, i.sheet_metal_door, e],
	[i.rusty_sheet_metal_trapdoor, i.sheet_metal_trapdoor, e],
	[i.rusty_sheet_metal_treading, i.sheet_metal_treading, e],
	[i.rusty_sheet_metal_walling, i.sheet_metal_walling, e],
	[i.rusty_sheet_metal_siding, i.sheet_metal_siding, e],
	[i.rusty_sheet_metal_paneling, i.sheet_metal_paneling, e],
	[i.rusty_sheet_metal_plating, i.sheet_metal_plating, e],
	// Broken? Item still craftable
	[i.tall_rusty_iron_door, i.tall_iron_door, e],
]);

// ============================================================
// Gilded blackstone deploying
// ============================================================
R.createDeploying(i.gilded_blackstone, [i.blackstone, i.block_gold], e);

R.bulk('ieCrushing', [
	[Item.of(i.nugget_gold, 18), i.gilded_blackstone, e, undefined, undefined, 6000],
]);

// ============================================================
// Spool crafting
// ============================================================
R.shaped(Item.of(i.spool,8), ['A','B','A'], {A: i.plate_iron,B: i.nugget_iron}, e)
const incSpool = R.incomplete('spool')
R.createSequencedAssembly(
	Item.of(i.spool, 12), i.plate_iron, [
		e.recipes.create.deploying(incSpool, [incSpool, i.nugget_iron]),
		e.recipes.create.deploying(incSpool, [incSpool, i.plate_iron]),
	], ['spool'], e, 'spool_sequenced_assembly', 1
);
R.ieBlueprint(Item.of(i.spool, 16), [Item.of(i.plate_iron, 2), i.nugget_iron],'electricalcomponents', e)

// ============================================================
// Chain sequenced assembly
// ============================================================
const incChain = R.incomplete('chain')
R.createSequencedAssembly(
	Item.of(i.chain, 4), i.ingot_iron, [
		e.recipes.create.deploying(incChain, [incChain, i.nugget_iron])
			.id('createdeploying/chain_step1_nugget'),
		e.recipes.create.deploying(incChain, [incChain, i.ingot_iron])
			.id('createdeploying/chain_step2_ingot'),
		e.recipes.create.deploying(incChain, [incChain, i.nugget_iron])
			.id('createdeploying/chain_step3_nugget'),
	], ['chain'], e, 'chain_sequenced_assembly', 1
);

// ============================================================
// Steel wool
// ============================================================
R.shaped(Item.of(i.steel_wool, 1), ['WWW', 'WWW'], {W: i.wire_steel}, e)
R.createMixing(Item.of(i.steel_wool, 1), [Item.of(i.wire_steel, 4)], e, 'steel_wool_mixing', 'n/a')

// ============================================================
// Misc
// ============================================================

//Swap redstone in wind_vane for iron rod
R.replaceInput({ id: 'supplementaries:wind_vane' },
  i.redstone, i.rod_iron, e)

// Rods in iron frame
R.replaceInput({ id: 'suppsquared:metal_frame' },
  i.nugget_iron, i.rod_iron, e)


console.log('The misc metal event has finished!')
})
