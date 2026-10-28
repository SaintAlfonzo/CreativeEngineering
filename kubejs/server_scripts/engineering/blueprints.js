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

    //Disassembly blueprints — split by type of component being disassembled (was one
    //'disassembly' blueprint covering everything, which overloaded the workbench's recipe list)

    //Disassemble Wire Coils
    R.shaped(Item.of(i.blueprint, '{blueprint:"disassembly_wire_coils"}'),['abc', 'ddd', 'ppp'],{
        a: i.any_wire_coil,
        b: i.wirecutter,
        c: i.any_wire_coil,
        d: i.dye_blue,
        p: i.paper
    },e,'blueprint_disassembly_wire_coils')
    R.shaped(Item.of(i.blueprint, '{blueprint:"disassembly_wire_coils"}'),['abc', ' s '],{
        a: i.any_wire_coil,
        b: i.wirecutter,
        c: i.any_wire_coil,
        s: i.empty_schematic
    },e,'blueprint_disassembly_wire_coils_schematic')

    //Disassemble Coil Blocks
    R.shaped(Item.of(i.blueprint, '{blueprint:"disassembly_coil_blocks"}'),['abc', 'ddd', 'ppp'],{
        a: i.any_coil_block,
        b: i.wirecutter,
        c: i.any_coil_block,
        d: i.dye_blue,
        p: i.paper
    },e,'blueprint_disassembly_coil_blocks')
    R.shaped(Item.of(i.blueprint, '{blueprint:"disassembly_coil_blocks"}'),['abc', ' s '],{
        a: i.any_coil_block,
        b: i.wirecutter,
        c: i.any_coil_block,
        s: i.empty_schematic
    },e,'blueprint_disassembly_coil_blocks_schematic')

    //Disassemble Components
    R.shaped(Item.of(i.blueprint, '{blueprint:"disassembly_components"}'),['abc', 'ddd', 'ppp'],{
        a: i.any_component,
        b: i.screwdriver,
        c: i.any_component,
        d: i.dye_blue,
        p: i.paper
    },e,'blueprint_disassembly_components')
    R.shaped(Item.of(i.blueprint, '{blueprint:"disassembly_components"}'),['abc', ' s '],{
        a: i.any_component,
        b: i.screwdriver,
        c: i.any_component,
        s: i.empty_schematic
    },e,'blueprint_disassembly_components_schematic')

    //Disassemble Springs
    R.shaped(Item.of(i.blueprint, '{blueprint:"disassembly_springs"}'),['abc', 'ddd', 'ppp'],{
        a: i.any_spring,
        b: i.screwdriver,
        c: i.any_spring,
        d: i.dye_blue,
        p: i.paper
    },e,'blueprint_disassembly_springs')
    R.shaped(Item.of(i.blueprint, '{blueprint:"disassembly_springs"}'),['abc', ' s '],{
        a: i.any_spring,
        b: i.screwdriver,
        c: i.any_spring,
        s: i.empty_schematic
    },e,'blueprint_disassembly_springs_schematic')

    //Disassemble Small Springs
    R.shaped(Item.of(i.blueprint, '{blueprint:"disassembly_small_springs"}'),['abc', 'ddd', 'ppp'],{
        a: i.any_small_spring,
        b: i.screwdriver,
        c: i.any_small_spring,
        d: i.dye_blue,
        p: i.paper
    },e,'blueprint_disassembly_small_springs')
    R.shaped(Item.of(i.blueprint, '{blueprint:"disassembly_small_springs"}'),['abc', ' s '],{
        a: i.any_small_spring,
        b: i.screwdriver,
        c: i.any_small_spring,
        s: i.empty_schematic
    },e,'blueprint_disassembly_small_springs_schematic')

    //Electrical Component blueprint
    R.shaped(Item.of(i.blueprint, '{blueprint:"electricalcomponents"}'),['abc', 'ddd', 'ppp'],{
        a: i.ingot_electrum,
        b: i.coil_block_steel,
        c: i.component_electronic,
        d: i.dye_blue,
        p: i.paper
    },e,'blueprint_electricalcomponents')
    R.shaped(Item.of(i.blueprint, '{blueprint:"electricalcomponents"}'),['abc', ' s '],{
        a: i.ingot_electrum,
        b: i.coil_block_steel,
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
        a: i.plate_steel,
        b: i.sawblade,
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

    // Engineer's Workbench: sawblade + one block of the shape + 2 steel plates -> a precision sawblade
    // with that Profile (the sawmill recipes it unlocks are in generation_scripts/sawmill.js)
    ;[
        ['button', i.any_wooden_buttons],
        ['door', i.any_wooden_doors],
        ['fence', i.any_wooden_fences],
        ['fence_gate', i.any_fence_gates],
        ['ladder', i.any_ladders],
        ['pressure_plate', i.any_wooden_pressure_plates],
        ['railing', i.any_railings],
        ['sign', i.any_sign],
        ['stairs', i.any_wooden_stairs],
        ['trapdoor', i.any_wooden_trapdoors],
        ['vertical_planks', i.any_vertical_planks],
        ['vertical_slab', i.any_wooden_vertical_slabs],
    ].forEach(([profile, shape]) => {
        R.ieBlueprint(Item.of(i.precision_sawblade, '{Profile:"' + profile + '"}'),
            [i.sawblade, shape, { id: i.any_plate_steel, count: 2 }], 'precision_sawblades', e, 'precision_sawblade_' + profile)
    })


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
        'precision_sawblades',
        'disassembly_wire_coils',
        'disassembly_coil_blocks',
        'disassembly_components',
        'disassembly_springs',
        'disassembly_small_springs'
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
