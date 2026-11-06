package com.sammy.minersdelight.logic;

import com.sammy.minersdelight.MinersDelightMod;
import com.sammy.minersdelight.registry.MDBlocks;
import com.sammy.minersdelight.registry.MDItems;
import com.sammy.minersdelight.registry.MDPotions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/** Common setup: brewing, compost chances and the potted gossypium. */
public final class MDSetup {
    private MDSetup() {
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            MDPotions.registerBrewing();
            ComposterBlock.COMPOSTABLES.put(MDItems.CAVE_CARROT.get(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(MDItems.BAKED_CAVE_CARROT.get(), 0.85F);
            ComposterBlock.COMPOSTABLES.put(MDItems.GOSSYPIUM.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(MDItems.SPIDER_LEG.get(), 0.5F);
            ComposterBlock.COMPOSTABLES.put(MDItems.CRUNCHY_BAR.get(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(MDItems.NUTRITIONAL_BAR.get(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(MDItems.GOLDEN_NUTRITIONAL_BAR.get(), 0.65F);
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(MinersDelightMod.path("gossypium"), MDBlocks.POTTED_GOSSYPIUM);
        });
    }
}
