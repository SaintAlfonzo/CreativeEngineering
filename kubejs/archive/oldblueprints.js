ServerEvents.recipes(e => {

console.log('Hello! The blueprint event has started!')

const R = global.NCH
var i = global.item;

//Arrow blueprint
R.shaped(Item.of(i.blueprint, '{blueprint:"archery"}'),['abc', 'ddd', 'ppp'],{
    a: i.echoing_arrow,
    b: i.ethereal_arrow,
    c: i.reinforced_arrow,
    d: i.dye_blue,
    p: i.paper
},e,'blueprint_archery')
R.shaped(Item.of(i.blueprint, '{blueprint:"archery"}'),['abc', ' s '],{
    a: i.echoing_arrow,
    b: i.ethereal_arrow,
    c: i.reinforced_arrow,
  	s: i.empty_schematic
},e,'blueprint_archery_schematic')

//Banner blueprint
R.shaped(Item.of(i.blueprint, '{blueprint:"bannerpatterns"}'),['a', 's'],{
    a: i.any_banner,
  	s: i.empty_schematic
},e,'blueprint_banner_schematic')

//Bullet blueprint
R.shaped(Item.of(i.blueprint, '{blueprint:"bullet"}'),['abc', ' s '],{
    a: i.gunpowder,
    b: i.empty_casing,
    c: i.gunpowder,
  	s: i.empty_schematic
},e,'blueprint_bullet_schematic')

//Component blueprint
R.shaped(Item.of(i.blueprint, '{blueprint:"components"}'),['abc', ' s '],{
    a: i.ingot_copper,
    b: i.ingot_aluminum,
    c: i.ingot_iron,
  	s: i.empty_schematic
},e,'blueprint_component_schematic')

//Electrical Component blueprint
R.shaped(Item.of(i.blueprint, '{blueprint:"electricalcomponents"}'),['abc', 'ddd', 'ppp'],{
    a: i.ingot_electrum,
    b: i.wire_coil_block_steel,
    c: i.component_electronic,
    d: i.dye_blue,
    p: i.paper
},e,'blueprint_electricalcomponents')
R.shaped(Item.of(i.blueprint, '{blueprint:"electricalcomponents"}'),['abc', ' s '],{
    a: i.ingot_electrum,
    b: i.wire_coil_block_steel,
    c: i.component_electronic,
    s: i.empty_schematic
},e,'blueprint_electricalcomponents_schematic')

//Mold Component blueprint
R.remove({id:'immersiveengineering:crafting/blueprint_molds'}, e)
R.shaped(Item.of(i.blueprint, '{blueprint:"molds"}'),[' a ', 'ddd', 'ppp'],{
    a: i.any_plate,
    d: i.dye_blue,
    p: i.paper
},e,'blueprint_mold')
R.shaped(Item.of(i.blueprint, '{blueprint:"molds"}'),['a', 's'],{
    a: i.any_plate,
    s: i.empty_schematic
},e,'blueprint_mold_schematic')

//Rail blueprint
R.shaped(Item.of(i.blueprint, '{blueprint:"railtransport"}'),['abc', 'ddd', 'ppp'],{
    a: i.detector_rail,
    b: i.activator_rail,
    c: i.powered_rail,
    d: i.dye_blue,
    p: i.paper},e,'blueprint_railtransport')
R.shaped(Item.of(i.blueprint, '{blueprint:"railtransport"}'),['abc', ' s '],{
   	a: i.detector_rail,
    b: i.activator_rail,
    c: i.powered_rail,
    s: i.empty_schematic},e,'blueprint_railtransport_schematic')
    
//Redstone blueprint
R.shaped(Item.of(i.blueprint, '{blueprint:"redstonecomponents"}'),['abc', 'ddd', 'ppp'],{
    a: i.quartz,
    b: i.redstone,
    c: i.rose_quartz_plate,
    d: i.dye_blue,
    p: i.paper
},e,'blueprint_redstonecomponents')
R.shaped(Item.of(i.blueprint, '{blueprint:"redstonecomponents"}'),['abc', ' s '],{
    a: i.quartz,
    b: i.redstone,
    c: i.rose_quartz_plate,
    s: i.empty_schematic
},e,'blueprint_redstonecomponents_schematic')

//Scope components blueprint
R.shaped(Item.of(i.blueprint, '{blueprint:"controlengineering:scope_components"}'),['abc', ' s '],{
    a: i.comparator,
    b: i.bus_relay,
    c: i.repeater,
    s: i.empty_schematic
},e,'blueprint_scope_components_schematic')

//Precision sawblade blueprint (Engineer's Workbench recipes for profiled precision sawblades)
R.shaped(Item.of(i.blueprint, '{blueprint:"precision_sawblades"}'),['abc', 'ddd', 'ppp'],{
    a: i.sawblade,
    b: i.plate_steel,
    c: i.rod_steel,
    d: i.dye_blue,
    p: i.paper
},e,'blueprint_precision_sawblades')
R.shaped(Item.of(i.blueprint, '{blueprint:"precision_sawblades"}'),['abc', ' s '],{
    a: i.sawblade,
    b: i.plate_steel,
    c: i.rod_steel,
    s: i.empty_schematic
},e,'blueprint_precision_sawblades_schematic')


//Blueprint duplication
var blueprintTypes = [
	'archery',
	'bannerpatterns',
	'bullet',
	'components',
	'electricalcomponents',
	'molds',
	'railtransport',
	'redstonecomponents',
	'controlengineering:scope_components',
	'precision_sawblades'
];
blueprintTypes.forEach(function(type) {
	var nbt = '{blueprint:"' + type + '"}';
	var idSafe = type.replace(/[^a-z0-9_]/gi, '_').toLowerCase();
 
	R.shapeless(
		Item.of(i.blueprint, 2, nbt),
		[
			{ type: 'forge:partial_nbt', item: i.blueprint, nbt: nbt },
			i.empty_schematic
		],
		e,
		'blueprint_duplicate_' + idSafe
	);
});

    
console.log('The blueprint event has finished!')
})
