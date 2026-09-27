var $ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')

var HARVEST_TIERS = [
    'minecraft:needs_stone_tool',
    'creativeengineering:needs_copper_tool',
    'minecraft:needs_iron_tool',
    'minecraft:needs_diamond_tool'
]

var TOOL_TYPES = [
    'minecraft:mineable/pickaxe',
    'minecraft:mineable/shovel',
    'minecraft:mineable/axe',
    'minecraft:mineable/hoe'
]

function harvestSwap(event, targetTag, blockIds) {
    if (!Array.isArray(blockIds)) blockIds = [blockIds]
    blockIds.forEach(function(id) {
        var rl = $ResourceLocation.of(id, ':')
        HARVEST_TIERS.forEach(function(tier) {
            if (tier !== targetTag && event.get(tier).getObjectIds().contains(rl)) {
                event.remove(tier, id)
            }
        })
        event.add(targetTag, id)
    })
}

function setToolType(event, targetTag, blockIds) {
    if (!Array.isArray(blockIds)) blockIds = [blockIds]
    blockIds.forEach(function(id) {
        var rl = $ResourceLocation.of(id, ':')
        TOOL_TYPES.forEach(function(tag) {
            if (tag !== targetTag && event.get(tag).getObjectIds().contains(rl)) {
                event.remove(tag, id)
            }
        })
        event.add(targetTag, id)
    })
}

ServerEvents.tags('block', event => {
    var i = global.item;
    
    //Stone tool
    harvestSwap(event, 'minecraft:needs_stone_tool', [
            i.crushed_block_copper,
            i.plate_block_copper,
            i.dust_block_copper,
    ])
    
    //Copper tool
    harvestSwap(event, 'creativeengineering:needs_copper_tool', [
        i.ore_iron,
        i.deepslate_ore_iron,
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

    ])

    //Iron tool
    harvestSwap(event, 'minecraft:needs_iron_tool', [
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
    ])
		
    //Diamond tool
    harvestSwap(event, 'minecraft:needs_diamond_tool', [
        i.plate_block_netherite,
        i.sturdy_plate_block,
        i.powdered_obsidian_block
    ])
		
		//Pickaxe — solid blocks
    setToolType(event, 'minecraft:mineable/pickaxe', [
       	i.plate_block_copper, 
       	i.plate_block_iron, i.plate_block_aluminum, 
        i.plate_block_gold, i.plate_block_zinc, i.plate_block_lead,i.plate_block_silver, i.plate_block_nickel, i.plate_block_uranium, 
        i.plate_block_netherite, 

        // No harvest tier — unchanged from before the KubeJS migration (Java's static
        // pickaxe.json tag gave these plain mineable/pickaxe with no tool-tier requirement)
        i.plate_block_cast_iron, i.plate_block_constantan, i.plate_block_electrum, i.plate_block_steel,
    ])

    //Shovel — loose/crushed piles
    setToolType(event, 'minecraft:mineable/shovel', [
        i.crushed_block_copper, i.dust_block_copper,
        i.crushed_block_iron, i.crushed_block_zinc,i.dust_block_iron, i.dust_block_aluminum,
        i.crushed_block_gold,i.crushed_block_lead, i.crushed_block_silver, i.crushed_block_nickel, i.crushed_block_uranium, i.dust_block_gold, i.dust_block_zinc, i.dust_block_lead, i.dust_block_silver, i.dust_block_nickel, i.dust_block_uranium,
        i.dust_block_diamond,
        i.dust_block_rose_gold, i.dust_block_cast_iron, i.dust_block_electrum, i.dust_block_steel, i.dust_block_constantan, i.dust_block_brass,
    ])
})
