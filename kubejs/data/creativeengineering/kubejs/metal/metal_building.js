// requires: creativeengineering
// Creative Engineering; moved from kubejs/server_scripts/metal/metal_building.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
ServerEvents.recipes(e => {

console.log('Hello! The metal building event has started!')

const R = global.NCH
var i = global.item;

//Remove unneeded
R.bulk('remove', [
	[{id:'everythingcopper:copper_bars'}, e],
	[{id:'createdeco:polished_iron_bars_from_iron_ingot_stonecutting'}, e],
	[{id:'create:copper_bars_from_ingots_copper_stonecutting'}, e],
	[{id:'create:brass_bars_from_ingots_brass_stonecutting'}, e],
	[{id:'create:andesite_bars_from_andesite_alloy_stonecutting'}, e],
	[{id:'createdeco:copper_bars_from_copper_ingot_stonecutting'}, e],
	[{id:'quark:building/crafting/gold_bars'}, e],
	[{id:'createdeco:gold_bars_from_gold_ingot_stonecutting'}, e],
	[{id:'createdeco:cast_iron_bars_from_cast_iron_ingot_stonecutting'}, e],
	[{id:'createdeco:zinc_bars_from_zinc_ingot_stonecutting'}, e],
	[{id:'createdeco:andesite_bars_from_andesite_alloy_stonecutting'}, e],
	[{id:'createdeco:brass_bars_from_brass_ingot_stonecutting'}, e],
	[{id:'createdeco:netherite_bars_from_netherite_ingot_stonecutting'}, e],
]);

//Bars
//Copper, 3 variants
R.viLaserCutting(Item.of(i.copper_bars, 64),i.block_copper, e, 'copper_bar_laser_cutting', 16000, 800)
const incCopperBars = R.incomplete('copper_bars')
R.createSequencedAssembly(Item.of(i.copper_bars, 12), i.rod_copper, [e.recipes.create.deploying(incCopperBars, [incCopperBars, i.rod_copper])], ['copper_bars'], e, 'copper_bars_sequenced_assembly', 5);
R.shaped(Item.of(i.copper_bars, 9), ['RRR', 'RRR'], {R: i.rod_copper}, e)
R.shaped(Item.of(i.ornate_copper_bars, 3), ['B', 'B', 'B'], {B: i.copper_bars}, e, 'copper_bars_ornate') 
R.shaped(Item.of(i.copper_bars, 3), ['B', 'B', 'B'], {B: i.ornate_copper_bars}, e, 'copper_bars_deornate')
R.shaped(Item.of(i.raw_copper_bars, 5), ['B B', ' B ', 'B B'], {B: i.copper_bars}, e, 'copper_bars_raw') 
R.shaped(Item.of(i.copper_bars, 5), ['B B', ' B ', 'B B'], {B: i.raw_copper_bars}, e, 'copper_bars_deraw')

//Iron
R.viLaserCutting(Item.of(i.iron_bars, 64),i.block_iron, e, 'iron_bar_laser_cutting', 16000, 800)
const incIronBars = R.incomplete('iron_bars')
R.createSequencedAssembly(Item.of(i.iron_bars, 12), i.rod_iron, [e.recipes.create.deploying(incIronBars, [incIronBars, i.rod_iron])], ['iron_bars'], e, 'iron_bars_sequenced_assembly', 5);
R.shaped(Item.of(i.iron_bars, 9), ['RRR', 'RRR'], {R: i.rod_iron}, e)

R.viLaserCutting(Item.of(i.cast_iron_bars, 64),i.block_cast_iron, e, 'cast_iron_bar_laser_cutting', 16000, 800)
const incCastIronBars = R.incomplete('cast_iron_bars')
R.createSequencedAssembly(Item.of(i.cast_iron_bars, 12), i.rod_cast_iron, [e.recipes.create.deploying(incCastIronBars, [incCastIronBars, i.rod_cast_iron])], ['cast_iron_bars'], e, 'cast_iron_bars_sequenced_assembly', 5);
R.shaped(Item.of(i.cast_iron_bars, 9), ['RRR', 'RRR'], {R: i.rod_cast_iron}, e)

R.viLaserCutting(Item.of(i.gold_bars, 64),i.block_gold, e, 'gold_bar_laser_cutting', 16000, 800)
const incGoldBars = R.incomplete('gold_bars')
R.createSequencedAssembly(Item.of(i.gold_bars, 12), i.rod_gold, [e.recipes.create.deploying(incGoldBars, [incGoldBars, i.rod_gold])], ['gold_bars'], e, 'gold_bars_sequenced_assembly', 5);
R.shaped(Item.of(i.gold_bars, 9), ['RRR', 'RRR'], {R: i.rod_gold}, e)
R.shaped(Item.of(i.ornate_gold_bars, 3), ['B', 'B', 'B'], {B: i.gold_bars}, e, 'gold_bars_ornate') 
R.shaped(Item.of(i.gold_bars, 3), ['B', 'B', 'B'], {B: i.ornate_gold_bars}, e, 'gold_bars_deornate') 

R.viLaserCutting(Item.of(i.zinc_bars, 64),i.block_zinc, e, 'zinc_bar_laser_cutting', 16000, 800)
const incZincBars = R.incomplete('zinc_bars')
R.createSequencedAssembly(Item.of(i.zinc_bars, 12), i.rod_zinc, [e.recipes.create.deploying(incZincBars, [incZincBars, i.rod_zinc])], ['zinc_bars'], e, 'zinc_bars_sequenced_assembly', 5);
R.shaped(Item.of(i.zinc_bars, 9), ['RRR', 'RRR'], {R: i.rod_zinc}, e)

R.viLaserCutting(Item.of(i.andesite_bars, 64),i.block_andesite_alloy, e, 'andesite_bar_laser_cutting', 16000, 800)
R.shaped(Item.of(i.ornate_andesite_bars, 3), ['B', 'B', 'B'], {B: i.andesite_bars}, e, 'andesite_bars_ornate') 
R.shaped(Item.of(i.andesite_bars, 3), ['B', 'B', 'B'], {B: i.ornate_andesite_bars}, e, 'andesite_bars_deornate') 

R.viLaserCutting(Item.of(i.brass_bars, 64),i.block_brass, e, 'brass_bar_laser_cutting', 16000, 800)
const incBrassBars = R.incomplete('brass_bars')
R.createSequencedAssembly(Item.of(i.brass_bars, 12), i.rod_brass, [e.recipes.create.deploying(incBrassBars, [incBrassBars, i.rod_brass])], ['brass_bars'], e, 'brass_bars_sequenced_assembly', 5);
R.shaped(Item.of(i.brass_bars, 9), ['RRR', 'RRR'], {R: i.rod_brass}, e)
R.shaped(Item.of(i.ornate_brass_bars, 3), ['B', 'B', 'B'], {B: i.brass_bars}, e, 'brass_bars_ornate') 
R.shaped(Item.of(i.brass_bars, 3), ['B', 'B', 'B'], {B: i.ornate_brass_bars}, e, 'brass_bars_deornate') 

R.viLaserCutting(Item.of(i.netherite_bars, 64),i.block_netherite, e, 'netherite_bar_laser_cutting', 16000, 800)

R.viLaserCutting(Item.of(i.framed_bars, 8), i.framed_cube, e, 'framed_bar_laser_cutting', 2500, 400)

console.log('The metal building event has finished!')
})
