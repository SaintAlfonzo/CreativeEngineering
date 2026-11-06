package com.SaintAlfonzo.creativedelight.nethers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import umpaz.nethersdelight.common.registry.NDBlocks;

/**
 * Potted Mimicarnation, Nether's Delight 4.0's flower pot block, carried over to the 3.1 repack (nethers_delight.md 4.1).
 * 3.1 already ships its blockstate, model and loot table but registers no block. Registered under the
 * {@code nethersdelight} namespace from inside the repack's own mod file, so this subscriber belongs to that mod.
 * Unlike 4.0 (which passes no empty pot, so emptying it would leave the potted block), the empty pot is the vanilla
 * flower pot and the plant is added to its map, so a Mimicarnation can be planted and taken out like a vanilla flower.
 * The {@code minecraft:flower_pots} tag entry is in the CD script nethersdelight/tags.js (global.tags).
 *
 * Creative Delight overlay of the Nether's Delight repack (GNU GPL3).
 */
@Mod.EventBusSubscriber(modid = "nethersdelight", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PottedMimicarnation {

    public static final ResourceLocation ID = new ResourceLocation("nethersdelight", "potted_mimicarnation");
    private static final ResourceLocation PLANT = new ResourceLocation("nethersdelight", "mimicarnation");

    private static FlowerPotBlock potted;

    private PottedMimicarnation() {
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.BLOCKS, helper -> {
            potted = new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, NDBlocks.MIMICARNATION,
                    BlockBehaviour.Properties.copy(Blocks.POTTED_POPPY));
            helper.register(ID, potted);
        });
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (potted != null) ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(PLANT, () -> (Block) potted);
        });
    }
}
