// requires: creativeengineering
// Creative Engineering; moved from kubejs/server_scripts/engineering/components.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
ServerEvents.recipes(e=>{
    console.log('components starting')

    var i = global.item
    var f = global.fluid
    var R = global.NCH

    // ---------- Components ----------
    // Transition items ('component_<m>') are registered in startup_scripts/changes/transition_items.js.
    const incComponentIron = R.incomplete('component_iron') // Iron Mechanical
    const incComponentSteel = R.incomplete('component_steel') // Steel Mechanical
    const incComponentCopper = R.incomplete('component_copper') // Copper Mechanical (Fluid)
    const incComponentGold = R.incomplete('component_gold') // Gold Electrical
    const incComponentDiamond = R.incomplete('component_diamond') // Diamond Electrical
    const incComponentZinc = R.incomplete('component_zinc') // Zinc Mechanical (Intermediate Create)
    const incComponentNetherite = R.incomplete('component_netherite') // Netherite Thermal

    const incComponentAluminum = R.incomplete('component_aluminum') // Aluminum  --- Light? ---
    const incComponentSilver = R.incomplete('component_silver') // Silver
    const incComponentNickel = R.incomplete('component_nickel') // Nickel

    const incComponentUranium = R.incomplete('component_uranium') // Uranium     --- Dense? ---
    const incComponentLead = R.incomplete('component_lead') // Lead

    const incComponentBrass = R.incomplete('component_brass') // Brass Mechanical (Advanced Create)
    const incComponentElectrum = R.incomplete('component_electrum') // Electrum Electrical
    const incComponentConstantan = R.incomplete('component_constantan') // Constantan Thermal

    // Crafting table — 4 plates around a copper ingot (IE's standard component shape).
    // component_diamond has no plate, so it uses minecraft:diamond in the plate slots.
    R.remove({id:'immersiveengineering:crafting/component_iron'}, e)
    R.remove({id:'immersiveengineering:blueprint/component_iron'}, e)
    R.remove({id:'immersiveengineering:crafting/component_steel'}, e)
    R.remove({id:'immersiveengineering:blueprint/component_steel'}, e)
    R.bulk('shaped', [
        [Item.of(i.component_iron, 1), ['P P', ' C ', 'P P'], { P: i.plate_iron, C: i.rod_copper }, e, 'component_iron'],
        [Item.of(i.component_steel, 1), ['P P', ' C ', 'P P'], { P: i.plate_steel, C: i.rod_iron }, e, 'component_steel'],
        [Item.of(i.component_copper, 1), ['P P', ' C ', 'P P'], { P: i.plate_copper, C: i.rod_copper }, e, 'component_copper'],
        [Item.of(i.component_gold, 1), ['P P', ' C ', 'P P'], { P: i.any_plate_gold, C: i.wire_copper }, e, 'component_gold'],
        [Item.of(i.component_diamond, 1), ['P P', ' C ', 'P P'], { P: i.dust_diamond, C: i.component_electrum }, e, 'component_diamond'],
        [Item.of(i.component_zinc, 1), ['P P', ' C ', 'P P'], { P: i.plate_zinc, C: i.andesite_alloy }, e, 'component_zinc'],
        [Item.of(i.component_netherite, 1), ['P P', ' C ', 'P P'], { P: i.plate_netherite, C: i.component_constantan }, e, 'component_netherite'],
        [Item.of(i.component_aluminum, 1), ['P P', ' C ', 'P P'], { P: i.plate_aluminum, C: [i.ingot_nickel, i.ingot_silver] }, e, 'component_aluminum'],
        [Item.of(i.component_lead, 1), ['P P', ' C ', 'P P'], { P: i.plate_lead, C: i.ingot_uranium }, e, 'component_lead'],
        [Item.of(i.component_silver, 1), ['P P', ' C ', 'P P'], { P: i.plate_silver, C: [i.ingot_nickel, i.ingot_aluminum] }, e, 'component_silver'],
        [Item.of(i.component_nickel, 1), ['P P', ' C ', 'P P'], { P: i.plate_nickel, C: [i.ingot_aluminum, i.ingot_silver] }, e, 'component_nickel'],
        [Item.of(i.component_uranium, 1), ['P P', ' C ', 'P P'], { P: i.plate_uranium, C: i.ingot_lead }, e, 'component_uranium'],
        [Item.of(i.component_brass, 1), ['P P', ' C ', 'P P'], { P: i.plate_brass, C: i.small_spring_zinc }, e, 'component_brass'],
        [Item.of(i.component_electrum, 1), ['P P', ' C ', 'P P'], { P: i.plate_electrum, C: i.wire_silver }, e, 'component_electrum'],
        [Item.of(i.component_constantan, 1), ['P P', ' C ', 'P P'], { P: i.plate_constantan, C: i.rod_steel }, e, 'component_constantan'],
    ]);

    // Sequenced assembly — deploy 3 plates onto a copper ingot
    R.bulk('createSequencedAssembly', [
        [Item.of(i.component_iron, 1), i.rod_copper, [e.recipes.create.deploying(incComponentIron, [incComponentIron, i.plate_iron])], ['component_iron'], e, 'component_iron_sequenced_assembly', 3],
        [Item.of(i.component_steel, 1), i.rod_iron, [e.recipes.create.deploying(incComponentSteel, [incComponentSteel, i.plate_steel])], ['component_steel'], e, 'component_steel_sequenced_assembly', 3],
        [Item.of(i.component_copper, 1), i.rod_copper, [e.recipes.create.deploying(incComponentCopper, [incComponentCopper, i.plate_copper])], ['component_copper'], e, 'component_copper_sequenced_assembly', 3],
        [Item.of(i.component_gold, 1), i.wire_copper, [e.recipes.create.deploying(incComponentGold, [incComponentGold, i.any_plate_gold])], ['component_gold'], e, 'component_gold_sequenced_assembly', 3],
        [Item.of(i.component_diamond, 1), i.component_electrum, [e.recipes.create.deploying(incComponentDiamond, [incComponentDiamond, i.dust_diamond])], ['component_diamond'], e, 'component_diamond_sequenced_assembly', 3],
        [Item.of(i.component_zinc, 1), i.andesite_alloy, [e.recipes.create.deploying(incComponentZinc, [incComponentZinc, i.plate_zinc])], ['component_zinc'], e, 'component_zinc_sequenced_assembly', 3],
        [Item.of(i.component_netherite, 1), i.component_constantan, [e.recipes.create.deploying(incComponentNetherite, [incComponentNetherite, i.plate_netherite])], ['component_netherite'], e, 'component_netherite_sequenced_assembly', 3],
        [Item.of(i.component_aluminum, 1), [i.ingot_nickel, i.ingot_silver], [e.recipes.create.deploying(incComponentAluminum, [incComponentAluminum, i.plate_aluminum])], ['component_aluminum'], e, 'component_aluminum_sequenced_assembly', 3],
        [Item.of(i.component_lead, 1), i.ingot_uranium, [e.recipes.create.deploying(incComponentLead, [incComponentLead, i.plate_lead])], ['component_lead'], e, 'component_lead_sequenced_assembly', 3],
        [Item.of(i.component_silver, 1), [i.ingot_nickel, i.ingot_aluminum], [e.recipes.create.deploying(incComponentSilver, [incComponentSilver, i.plate_silver])], ['component_silver'], e, 'component_silver_sequenced_assembly', 3],
        [Item.of(i.component_nickel, 1), [i.ingot_aluminum, i.ingot_silver], [e.recipes.create.deploying(incComponentNickel, [incComponentNickel, i.plate_nickel])], ['component_nickel'], e, 'component_nickel_sequenced_assembly', 3],
        [Item.of(i.component_uranium, 1), i.ingot_lead, [e.recipes.create.deploying(incComponentUranium, [incComponentUranium, i.plate_uranium])], ['component_uranium'], e, 'component_uranium_sequenced_assembly', 3],
        [Item.of(i.component_brass, 1), i.small_spring_zinc, [e.recipes.create.deploying(incComponentBrass, [incComponentBrass, i.plate_brass])], ['component_brass'], e, 'component_brass_sequenced_assembly', 3],
        [Item.of(i.component_electrum, 1), i.wire_silver, [e.recipes.create.deploying(incComponentElectrum, [incComponentElectrum, i.plate_electrum])], ['component_electrum'], e, 'component_electrum_sequenced_assembly', 3],
        [Item.of(i.component_constantan, 1), i.rod_steel, [e.recipes.create.deploying(incComponentConstantan, [incComponentConstantan, i.plate_constantan])], ['component_constantan'], e, 'component_constantan_sequenced_assembly', 3],
    ]);

    // Engineer's Workbench blueprint (components) — 2 plates + 1 copper ingot
    R.bulk('ieBlueprint', [
        [Item.of(i.component_iron, 1), [Item.of(i.plate_iron, 2), i.ingot_copper], 'components', e, 'component_iron'],
        [Item.of(i.component_steel, 1), [Item.of(i.plate_steel, 2), i.rod_iron], 'components', e, 'component_steel'],
        [Item.of(i.component_copper, 1), [Item.of(i.plate_copper, 2), i.rod_copper], 'components', e, 'component_copper'],
        [Item.of(i.component_gold, 1), [{ id: i.any_plate_gold, count: 2 }, i.wire_copper], 'components', e, 'component_gold'],
        [Item.of(i.component_diamond, 1), [Item.of(i.dust_diamond, 2), i.component_gold], 'components', e, 'component_diamond'],
        [Item.of(i.component_zinc, 1), [Item.of(i.plate_zinc, 2), i.andesite_alloy], 'components', e, 'component_zinc'],
        [Item.of(i.component_netherite, 1), [Item.of(i.plate_netherite, 2), i.component_constantan], 'components', e, 'component_netherite'],
        [Item.of(i.component_aluminum, 1), [Item.of(i.plate_aluminum, 2), [i.ingot_nickel, i.ingot_silver]], 'components', e, 'component_aluminum'],
        [Item.of(i.component_lead, 1), [Item.of(i.plate_lead, 2), i.ingot_uranium], 'components', e, 'component_lead'],
        [Item.of(i.component_silver, 1), [Item.of(i.plate_silver, 2), [i.ingot_nickel, i.ingot_aluminum]], 'components', e, 'component_silver'],
        [Item.of(i.component_nickel, 1), [Item.of(i.plate_nickel, 2), [i.ingot_aluminum, i.ingot_silver]], 'components', e, 'component_nickel'],
        [Item.of(i.component_uranium, 1), [Item.of(i.plate_uranium, 2), i.ingot_lead], 'components', e, 'component_uranium'],
        [Item.of(i.component_brass, 1), [Item.of(i.plate_brass, 2), i.small_spring_zinc], 'components', e, 'component_brass'],
        [Item.of(i.component_electrum, 1), [Item.of(i.plate_electrum, 2), i.wire_silver], 'components', e, 'component_electrum'],
        [Item.of(i.component_constantan, 1), [Item.of(i.plate_constantan, 2), i.rod_steel], 'components', e, 'component_constantan'],
    ]);

    // Component — disassembly blueprint: screwdriver + component -> 2 plates (center not returned)
    //  Screwdriver spends 1 durability/craft.
    const COMP_METALS = ['copper', 'iron', 'gold', 'netherite', 'zinc', 'aluminum', 'lead', 'nickel', 'silver', 'uranium', 'brass', 'constantan', 'steel', 'electrum'];
    R.bulk('ieBlueprint', COMP_METALS.map(m =>
        [Item.of(i['plate_' + m], 2), [i['component_' + m]], 'disassembly_components', e, m + '_component_disassembly']
    ));
    R.ieBlueprint(Item.of(i.dust_diamond, 2), [Item.of(i.component_diamond, 1)], 'disassembly_components', e)

    // Spring — disassembly blueprint: screwdriver + spring -> rod (uncoil back to the
    // Spring Coiling Machine's input); screwdriver + small spring -> wire.
    const SPRING_METALS = ['copper', 'iron', 'gold', 'zinc', 'aluminum', 'lead', 'nickel', 'silver', 'rose_gold', 'brass', 'steel', 'constantan', 'electrum'];
    R.bulk('ieBlueprint', SPRING_METALS.map(m =>
        [Item.of(i['rod_' + m], 1), [i['spring_' + m]], 'disassembly_springs', e, m + '_spring_disassembly']
    ));
    R.bulk('ieBlueprint', SPRING_METALS.map(m =>
        [Item.of(i['wire_' + m], 1), [i['small_spring_' + m]], 'disassembly_small_springs', e, m + '_small_spring_disassembly']
    ));

    console.log('components done')
})
