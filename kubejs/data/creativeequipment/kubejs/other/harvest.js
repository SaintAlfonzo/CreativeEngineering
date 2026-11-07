// requires: creativeequipment
// Harvest tier and tool per block (mining levels of the metal and ore blocks). Creative Equipment (tool progression;
// owner, creative_split.md 7.6); moved from Creative Delight's creativeengineering/harvest.js on 2026-11-04 (it came from
// the pack's server_scripts/other/harvest.js on 2026-11-01), rows unchanged. The exclusive mineable calls only touch the
// blocks listed here, and no pack or other jar's tag script names any of them, so running after the pack's tag scripts
// gives the same tags.
ServerEvents.tags('block', event => {
    var i = global.item;
    var t = global.tags
    // each block gets exactly one tier and one tool: other tiers and tools are removed
    
    //Stone tool
    t.mineable(event, null, 'stone', [
            i.crushed_block_copper,
            i.plate_block_copper,
            i.dust_block_copper,
    ], { exclusive: true })
    
    //Copper tool
    t.mineable(event, null, 'copper', [
        i.ore_iron,
        i.ds_ore_iron,
        i.raw_block_iron,
        i.crushed_block_iron,
        i.block_iron,
        i.plate_block_iron,
        i.dust_block_iron,
            
        i.ore_aluminum,
        i.ds_ore_aluminum,
        i.raw_block_aluminum,
        i.crushed_block_aluminum,
        i.block_aluminum,
        i.plate_block_aluminum,
        i.dust_block_aluminum,

    ], { exclusive: true })

    //Iron tool
    t.mineable(event, null, 'iron', [
        i.crushed_block_gold,
        i.plate_block_gold,
        i.dust_block_gold,
        
        i.crushed_block_zinc,
        i.plate_block_zinc,
        i.dust_block_zinc,
        
        i.crushed_block_lead,
        i.plate_block_lead,
        i.dust_block_lead,
        
        i.crushed_block_silver,
        i.plate_block_silver,
        i.dust_block_silver,
        
        i.crushed_block_nickel,
        i.plate_block_nickel,
        i.dust_block_nickel,
        
        i.crushed_block_uranium,
        i.plate_block_uranium,
        i.dust_block_uranium,
        
        i.dust_block_diamond,

        i.dust_block_electrum,
        i.dust_block_steel,
        i.dust_block_rose_gold,
        i.dust_block_cast_iron,
        i.dust_block_constantan,
        i.dust_block_brass,
    ], { exclusive: true })
		
    //Diamond tool
    t.mineable(event, null, 'diamond', [
        i.plate_block_netherite,
        i.sturdy_plate_block,
        i.powdered_obsidian_block
    ], { exclusive: true })
		
		//Pickaxe — solid blocks
    t.mineable(event, 'pickaxe', null, [
       	i.plate_block_copper, 
       	i.plate_block_iron, i.plate_block_aluminum, 
        i.plate_block_gold, i.plate_block_zinc, i.plate_block_lead,i.plate_block_silver, i.plate_block_nickel, i.plate_block_uranium, 
        i.plate_block_netherite, 

        // No harvest tier — unchanged from before the KubeJS migration (Java's static
        // pickaxe.json tag gave these plain mineable/pickaxe with no tool-tier requirement)
        i.plate_block_cast_iron, i.plate_block_constantan, i.plate_block_electrum, i.plate_block_steel,
    ], { exclusive: true })

    //Shovel — loose/crushed piles
    t.mineable(event, 'shovel', null, [
        i.crushed_block_copper, i.dust_block_copper,
        i.crushed_block_iron, i.crushed_block_zinc,i.dust_block_iron, i.dust_block_aluminum,
        i.crushed_block_gold,i.crushed_block_lead, i.crushed_block_silver, i.crushed_block_nickel, i.crushed_block_uranium, i.dust_block_gold, i.dust_block_zinc, i.dust_block_lead, i.dust_block_silver, i.dust_block_nickel, i.dust_block_uranium,
        i.dust_block_diamond,
        i.dust_block_rose_gold, i.dust_block_cast_iron, i.dust_block_electrum, i.dust_block_steel, i.dust_block_constantan, i.dust_block_brass,
    ], { exclusive: true })
})
